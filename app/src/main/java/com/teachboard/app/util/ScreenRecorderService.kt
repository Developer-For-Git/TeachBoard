package com.teachboard.app.util

import android.app.Activity
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.hardware.display.DisplayManager
import android.hardware.display.VirtualDisplay
import android.media.MediaRecorder
import android.media.projection.MediaProjection
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.IBinder
import android.os.ParcelFileDescriptor
import android.provider.MediaStore
import androidx.core.app.NotificationCompat
import com.teachboard.app.R
import com.teachboard.app.domain.model.RecordingQuality
import com.teachboard.app.ui.theme.AppConfig
import com.teachboard.app.ui.theme.FreeV1RecordingLimitPolicy
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class RecordingState(
  val isRecording: Boolean = false,
  val isPaused: Boolean = false,
  val elapsedSeconds: Int = 0,
  val lastSavedUri: Uri? = null,
  val errorMessage: String? = null
)

object RecorderController {
  private val _state = MutableStateFlow(RecordingState())
  val state: StateFlow<RecordingState> = _state.asStateFlow()

  internal fun update(block: (RecordingState) -> RecordingState) = _state.update(block)
  fun clearError() = _state.update { it.copy(errorMessage = null) }
}

/**
 * Foreground Service for unlimited Screen Recording (PRD 19.1 #10, 19.3, 19.7).
 * Saves H.264 + AAC MP4 up to 8 Mbps @ 30 fps directly into Movies/TeachBoard via MediaStore.
 */
class ScreenRecorderService : Service() {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var timerJob: Job? = null
  private var projection: MediaProjection? = null
  private var virtualDisplay: VirtualDisplay? = null
  private var recorder: MediaRecorder? = null
  private var outputPfd: ParcelFileDescriptor? = null
  private var outputUri: Uri? = null

  private val projectionCallback = object : MediaProjection.Callback() {
    override fun onStop() = stopAndCleanup()
  }

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_START -> {
        val code = intent.getIntExtra(EXTRA_RESULT_CODE, Activity.RESULT_CANCELED)
        val data = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
          intent.getParcelableExtra(EXTRA_RESULT_DATA, Intent::class.java)
        } else {
          @Suppress("DEPRECATION")
          intent.getParcelableExtra(EXTRA_RESULT_DATA)
        }
        val audio = intent.getBooleanExtra(EXTRA_RECORD_AUDIO, false)
        val qName = intent.getStringExtra(EXTRA_QUALITY) ?: RecordingQuality.P720.name
        val quality = runCatching { RecordingQuality.valueOf(qName) }.getOrDefault(RecordingQuality.P720)
        if (code == Activity.RESULT_OK && data != null) {
          startForegroundNotification()
          beginRecording(code, data, audio, quality)
        } else {
          stopSelf()
        }
      }
      ACTION_TOGGLE_PAUSE -> togglePause()
      ACTION_STOP -> stopAndCleanup()
    }
    return START_NOT_STICKY
  }

  private fun startForegroundNotification() {
    val mgr = getSystemService(NotificationManager::class.java)
    mgr.createNotificationChannel(
      NotificationChannel(CHANNEL_ID, getString(R.string.tool_record), NotificationManager.IMPORTANCE_LOW)
    )
    val stopPending = PendingIntent.getService(
      this, 2001,
      Intent(this, ScreenRecorderService::class.java).apply { action = ACTION_STOP },
      PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )
    val notif: Notification = NotificationCompat.Builder(this, CHANNEL_ID)
      .setSmallIcon(R.drawable.ic_launcher_foreground)
      .setContentTitle(getString(R.string.app_name))
      .setContentText(getString(R.string.recorder_notification_text))
      .setOngoing(true)
      .addAction(0, getString(R.string.recorder_stop), stopPending)
      .build()

    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PROJECTION)
    } else {
      startForeground(NOTIF_ID, notif)
    }
  }

  private fun beginRecording(code: Int, data: Intent, recordAudio: Boolean, quality: RecordingQuality) {
    try {
      val dm = resources.displayMetrics
      val aspect = dm.widthPixels.toFloat() / dm.heightPixels.coerceAtLeast(1)
      val recH = (quality.heightPx.coerceAtMost(dm.heightPixels) / 16) * 16
      val recW = (((recH * aspect).toInt()) / 16).coerceAtLeast(16) * 16

      val fileName = "${AppConfig.ALBUM_NAME}_${System.currentTimeMillis()}.mp4"
      val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          put(MediaStore.Video.Media.RELATIVE_PATH, "${Environment.DIRECTORY_MOVIES}/${AppConfig.ALBUM_NAME}")
          put(MediaStore.Video.Media.IS_PENDING, 1)
        }
      }
      val uri = contentResolver.insert(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, values)
        ?: throw IllegalStateException("Cannot create video file")
      outputUri = uri
      val pfd = contentResolver.openFileDescriptor(uri, "rw")
        ?: throw IllegalStateException("Cannot open video descriptor")
      outputPfd = pfd

      val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) MediaRecorder(this)
      else @Suppress("DEPRECATION") MediaRecorder()

      mr.apply {
        if (recordAudio) setAudioSource(MediaRecorder.AudioSource.MIC)
        setVideoSource(MediaRecorder.VideoSource.SURFACE)
        setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
        setVideoEncoder(MediaRecorder.VideoEncoder.H264)
        if (recordAudio) {
          setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
          setAudioEncodingBitRate(128_000)
          setAudioSamplingRate(44_100)
        }
        setVideoSize(recW, recH)
        setVideoFrameRate(quality.fps)
        setVideoEncodingBitRate(quality.bitrateBps)
        setOutputFile(pfd.fileDescriptor)
        prepare()
      }
      recorder = mr

      val mgr = getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
      val proj = mgr.getMediaProjection(code, data) ?: throw IllegalStateException("MediaProjection unavailable")
      proj.registerCallback(projectionCallback, null)
      projection = proj

      virtualDisplay = proj.createVirtualDisplay(
        "TeachBoardRecorder", recW, recH, dm.densityDpi,
        DisplayManager.VIRTUAL_DISPLAY_FLAG_AUTO_MIRROR, mr.surface, null, null
      )
      mr.start()
      RecorderController.update { RecordingState(isRecording = true) }
      startTimer()
    } catch (e: Exception) {
      RecorderController.update { it.copy(isRecording = false, errorMessage = e.localizedMessage ?: "Recording failed") }
      stopAndCleanup()
    }
  }

  private fun startTimer() {
    timerJob?.cancel()
    val limit = FreeV1RecordingLimitPolicy.maxDurationSeconds
    timerJob = scope.launch {
      while (true) {
        delay(1000L)
        val cur = RecorderController.state.value
        if (!cur.isRecording) break
        if (!cur.isPaused) {
          val next = cur.elapsedSeconds + 1
          RecorderController.update { it.copy(elapsedSeconds = next) }
          if (limit != null && next >= limit) { stopAndCleanup(); break }
        }
      }
    }
  }

  private fun togglePause() {
    val mr = recorder ?: return
    val cur = RecorderController.state.value
    if (!cur.isRecording) return
    runCatching {
      if (cur.isPaused) { mr.resume(); RecorderController.update { it.copy(isPaused = false) } }
      else { mr.pause(); RecorderController.update { it.copy(isPaused = true) } }
    }
  }

  private fun stopAndCleanup() {
    timerJob?.cancel()
    val wasRecording = RecorderController.state.value.isRecording
    runCatching { recorder?.stop() }
    runCatching { recorder?.reset() }
    runCatching { recorder?.release() }
    recorder = null
    runCatching { virtualDisplay?.release() }; virtualDisplay = null
    runCatching { projection?.unregisterCallback(projectionCallback) }
    runCatching { projection?.stop() }; projection = null
    runCatching { outputPfd?.close() }; outputPfd = null

    val saved = outputUri
    if (saved != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      runCatching {
        val values = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
        contentResolver.update(saved, values, null, null)
      }
    }
    outputUri = null

    RecorderController.update {
      it.copy(isRecording = false, isPaused = false, lastSavedUri = if (wasRecording) saved else it.lastSavedUri)
    }
    stopForeground(STOP_FOREGROUND_REMOVE)
    stopSelf()
  }

  override fun onDestroy() {
    scope.cancel()
    super.onDestroy()
  }

  companion object {
    const val CHANNEL_ID = "teachboard_recorder"
    const val NOTIF_ID = 4401
    const val ACTION_START = "com.teachboard.app.recorder.START"
    const val ACTION_TOGGLE_PAUSE = "com.teachboard.app.recorder.TOGGLE_PAUSE"
    const val ACTION_STOP = "com.teachboard.app.recorder.STOP"
    const val EXTRA_RESULT_CODE = "result_code"
    const val EXTRA_RESULT_DATA = "result_data"
    const val EXTRA_RECORD_AUDIO = "record_audio"
    const val EXTRA_QUALITY = "quality"
  }
}
