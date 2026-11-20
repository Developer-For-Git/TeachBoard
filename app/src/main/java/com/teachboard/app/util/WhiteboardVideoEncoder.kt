package com.teachboard.app.util

import android.graphics.Color
import android.media.AudioFormat
import android.media.AudioRecord
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.media.MediaMuxer
import android.media.MediaRecorder
import android.os.ParcelFileDescriptor
import android.os.SystemClock
import android.view.Surface
import java.nio.ByteBuffer
import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import kotlin.concurrent.thread

/**
 * Native Canvas-based Video Encoder that records the whiteboard directly into MP4
 * using MediaCodec (H.264 + AAC) and MediaMuxer.
 *
 * No MediaProjection / OS screen capture permission is required.
 * Fits and centers the whiteboard on a solid black frame (#000000).
 */
class WhiteboardVideoEncoder(
  private val pfd: ParcelFileDescriptor,
  val width: Int,
  val height: Int,
  val fps: Int,
  val bitrateBps: Int,
  private val recordAudio: Boolean,
  private val frameSource: BoardFrameSource?,
  private val onStorageExhausted: () -> Unit,
  private val onFileLimitReached: () -> Unit
) {

  private val isRunning = AtomicBoolean(false)
  private val isPaused = AtomicBoolean(false)
  private val muxerStarted = AtomicBoolean(false)
  private val videoEosReached = AtomicBoolean(false)
  private val audioEosReached = AtomicBoolean(false)
  private val totalFramesEncoded = AtomicLong(0L)

  private val muxerLock = Any()
  private lateinit var muxer: MediaMuxer
  private lateinit var videoCodec: MediaCodec
  private lateinit var inputSurface: Surface
  private var videoTrackIndex = -1

  // Audio recording & encoding
  private var audioCodec: MediaCodec? = null
  private var audioRecord: AudioRecord? = null
  private var audioTrackIndex = -1
  private var hasAudio = false

  private var drawThread: Thread? = null
  private var videoDrainThread: Thread? = null
  private var audioRecordThread: Thread? = null
  private var audioDrainThread: Thread? = null

  private var writtenBytes = AtomicLong(0L)
  private val maxFileBytes = 3_940_000_000L // ~3.67 GB safety limit

  fun start() {
    // 1. Setup Video Codec
    val videoFormat = MediaFormat.createVideoFormat(MediaFormat.MIMETYPE_VIDEO_AVC, width, height).apply {
      setInteger(MediaFormat.KEY_COLOR_FORMAT, MediaCodecInfo.CodecCapabilities.COLOR_FormatSurface)
      setInteger(MediaFormat.KEY_BIT_RATE, bitrateBps)
      setInteger(MediaFormat.KEY_FRAME_RATE, fps)
      setInteger(MediaFormat.KEY_I_FRAME_INTERVAL, 1)
    }
    videoCodec = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_VIDEO_AVC)
    videoCodec.configure(videoFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
    inputSurface = videoCodec.createInputSurface()
    videoCodec.start()

    // 2. Setup Audio Codec (if audio requested)
    if (recordAudio) {
      runCatching {
        val audioFormat = MediaFormat.createAudioFormat(MediaFormat.MIMETYPE_AUDIO_AAC, 44100, 2).apply {
          setInteger(MediaFormat.KEY_BIT_RATE, 128_000)
          setInteger(MediaFormat.KEY_AAC_PROFILE, MediaCodecInfo.CodecProfileLevel.AACObjectLC)
          setInteger(MediaFormat.KEY_MAX_INPUT_SIZE, 16384)
        }
        val ac = MediaCodec.createEncoderByType(MediaFormat.MIMETYPE_AUDIO_AAC)
        ac.configure(audioFormat, null, null, MediaCodec.CONFIGURE_FLAG_ENCODE)
        ac.start()

        val minBuf = AudioRecord.getMinBufferSize(44100, AudioFormat.CHANNEL_IN_STEREO, AudioFormat.ENCODING_PCM_16BIT)
        val ar = AudioRecord(
          MediaRecorder.AudioSource.MIC,
          44100,
          AudioFormat.CHANNEL_IN_STEREO,
          AudioFormat.ENCODING_PCM_16BIT,
          maxOf(minBuf * 2, 4096)
        )
        if (ar.state == AudioRecord.STATE_INITIALIZED) {
          ar.startRecording()
          audioCodec = ac
          audioRecord = ar
          hasAudio = true
        } else {
          ac.stop()
          ac.release()
        }
      }
    }

    // 3. Setup Muxer
    muxer = MediaMuxer(pfd.fileDescriptor, MediaMuxer.OutputFormat.MUXER_OUTPUT_MPEG_4)
    isRunning.set(true)
    isPaused.set(false)

    // 4. Start Video Drain Thread
    videoDrainThread = thread(name = "TeachBoardVideoDrain", priority = Thread.MAX_PRIORITY) {
      drainVideo()
    }

    // 5. Start Audio Threads (if audio active)
    if (hasAudio) {
      audioRecordThread = thread(name = "TeachBoardAudioRecord") {
        recordAudioLoop()
      }
      audioDrainThread = thread(name = "TeachBoardAudioDrain") {
        drainAudio()
      }
    }

    // 6. Start Drawing Loop Thread (ticking at exact frame rate)
    drawThread = thread(name = "TeachBoardDrawLoop", priority = Thread.NORM_PRIORITY + 1) {
      drawLoop()
    }
  }

  fun setPaused(paused: Boolean) {
    isPaused.set(paused)
  }

  private fun drawLoop() {
    val frameIntervalMs = (1000L / fps).coerceAtLeast(16L)
    while (isRunning.get()) {
      val frameStartTime = SystemClock.uptimeMillis()

      if (!isPaused.get()) {
        try {
          val canvas = inputSurface.lockHardwareCanvas()
          try {
            // Rule 1: Solid black background (#000000)
            canvas.drawColor(Color.BLACK)

            // Rule 2: Fit, centered whiteboard without UI overlays
            frameSource?.drawFrame(canvas, width, height)
          } finally {
            inputSurface.unlockCanvasAndPost(canvas)
          }
        } catch (_: Exception) {
          if (!isRunning.get()) break
        }
      }

      val elapsed = SystemClock.uptimeMillis() - frameStartTime
      val sleep = frameIntervalMs - elapsed
      if (sleep > 0) {
        try {
          Thread.sleep(sleep)
        } catch (_: InterruptedException) {
          break
        }
      }
    }
  }

  private fun drainVideo() {
    val bufferInfo = MediaCodec.BufferInfo()
    val startTime = SystemClock.uptimeMillis()

    while (isRunning.get() || !videoEosReached.get()) {
      val outIndex = runCatching { videoCodec.dequeueOutputBuffer(bufferInfo, 10_000L) }.getOrDefault(-1)

      if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
        val newFormat = videoCodec.outputFormat
        videoTrackIndex = muxer.addTrack(newFormat)
        checkStartMuxer()
      } else if (outIndex >= 0) {
        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
          videoEosReached.set(true)
        }

        // Safety fallback: if audio format took > 3s, start muxer with video only!
        if (!muxerStarted.get() && (SystemClock.uptimeMillis() - startTime) > 3000L && videoTrackIndex >= 0) {
          synchronized(muxerLock) {
            if (!muxerStarted.get()) {
              muxer.start()
              muxerStarted.set(true)
            }
          }
        }

        val encodedData = videoCodec.getOutputBuffer(outIndex)
        if (encodedData != null && bufferInfo.size > 0 && muxerStarted.get()) {
          encodedData.position(bufferInfo.offset)
          encodedData.limit(bufferInfo.offset + bufferInfo.size)

          synchronized(muxerLock) {
            runCatching {
              muxer.writeSampleData(videoTrackIndex, encodedData, bufferInfo)
              val cur = writtenBytes.addAndGet(bufferInfo.size.toLong())
              if (cur >= maxFileBytes) onFileLimitReached()
            }
          }
          totalFramesEncoded.incrementAndGet()
        }

        runCatching { videoCodec.releaseOutputBuffer(outIndex, false) }
        if (videoEosReached.get()) break
      }
    }
  }

  private fun recordAudioLoop() {
    val record = audioRecord ?: return
    val codec = audioCodec ?: return
    val pcmChunk = ByteArray(2048)

    while (isRunning.get()) {
      if (isPaused.get()) {
        try { Thread.sleep(30) } catch (_: InterruptedException) { break }
        continue
      }

      val readBytes = record.read(pcmChunk, 0, pcmChunk.size)
      if (readBytes > 0 && isRunning.get()) {
        val inIndex = runCatching { codec.dequeueInputBuffer(10_000L) }.getOrDefault(-1)
        if (inIndex >= 0) {
          val inBuffer = codec.getInputBuffer(inIndex)
          inBuffer?.clear()
          inBuffer?.put(pcmChunk, 0, readBytes)
          val ptsUs = System.nanoTime() / 1000L
          codec.queueInputBuffer(inIndex, 0, readBytes, ptsUs, 0)
        }
      }
    }

    // Send audio EOS
    val inIndex = runCatching { codec.dequeueInputBuffer(10_000L) }.getOrDefault(-1)
    if (inIndex >= 0) {
      codec.queueInputBuffer(inIndex, 0, 0, System.nanoTime() / 1000L, MediaCodec.BUFFER_FLAG_END_OF_STREAM)
    }
  }

  private fun drainAudio() {
    val codec = audioCodec ?: return
    val bufferInfo = MediaCodec.BufferInfo()

    while (isRunning.get() || !audioEosReached.get()) {
      val outIndex = runCatching { codec.dequeueOutputBuffer(bufferInfo, 10_000L) }.getOrDefault(-1)

      if (outIndex == MediaCodec.INFO_OUTPUT_FORMAT_CHANGED) {
        val newFormat = codec.outputFormat
        audioTrackIndex = muxer.addTrack(newFormat)
        checkStartMuxer()
      } else if (outIndex >= 0) {
        if ((bufferInfo.flags and MediaCodec.BUFFER_FLAG_END_OF_STREAM) != 0) {
          audioEosReached.set(true)
        }

        val encodedData = codec.getOutputBuffer(outIndex)
        if (encodedData != null && bufferInfo.size > 0 && muxerStarted.get()) {
          encodedData.position(bufferInfo.offset)
          encodedData.limit(bufferInfo.offset + bufferInfo.size)

          synchronized(muxerLock) {
            runCatching {
              muxer.writeSampleData(audioTrackIndex, encodedData, bufferInfo)
              writtenBytes.addAndGet(bufferInfo.size.toLong())
            }
          }
        }

        runCatching { codec.releaseOutputBuffer(outIndex, false) }
        if (audioEosReached.get()) break
      }
    }
  }

  private fun checkStartMuxer() {
    synchronized(muxerLock) {
      if (!muxerStarted.get()) {
        val videoReady = videoTrackIndex >= 0
        val audioReady = !hasAudio || audioTrackIndex >= 0
        if (videoReady && audioReady) {
          muxer.start()
          muxerStarted.set(true)
        }
      }
    }
  }

  fun stop(): Long {
    isRunning.set(false)

    // Signal video end of stream
    runCatching { videoCodec.signalEndOfInputStream() }
    runCatching { audioRecord?.stop() }

    // Wait for threads to flush
    runCatching { drawThread?.join(1000L) }
    runCatching { audioRecordThread?.join(500L) }
    runCatching { videoDrainThread?.join(2000L) }
    runCatching { audioDrainThread?.join(1000L) }

    // Teardown codecs and surfaces
    runCatching { videoCodec.stop() }
    runCatching { videoCodec.release() }
    runCatching { audioCodec?.stop() }
    runCatching { audioCodec?.release() }
    runCatching { audioRecord?.release() }
    runCatching { inputSurface.release() }

    // Finalize MP4 muxer
    synchronized(muxerLock) {
      if (muxerStarted.get()) {
        runCatching { muxer.stop() }
      }
      runCatching { muxer.release() }
    }

    return totalFramesEncoded.get()
  }

  fun getWrittenBytes(): Long = writtenBytes.get()
}
