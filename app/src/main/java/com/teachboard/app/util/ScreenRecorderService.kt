package com.teachboard.app.util

import android.app.ActivityManager
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.graphics.Bitmap
import android.graphics.Canvas
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
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class RecordingState(
  val isRecording: Boolean = false,
  val isPaused: Boolean = false,
  val elapsedSeconds: Int = 0,
  val lastSavedUri: Uri? = null,
  val lastSavedSizeBytes: Long = 0L,
  val errorMessage: String? = null
)

fun interface BoardFrameSource {
  fun drawFrame(canvas: Canvas, frameWidth: Int, frameHeight: Int)
}

object RecorderController {
  private val _state = MutableStateFlow(RecordingState())
  val state: StateFlow<RecordingState> = _state.asStateFlow()

  @Volatile
  var frameSource: BoardFrameSource? = null

  @Volatile
  var faceCamBitmapSupplier: (() -> Bitmap?)? = null

  internal fun update(block: (RecordingState) -> RecordingState) = _state.update(block)
  fun clearError() = _state.update { it.copy(errorMessage = null) }
  fun clearLastSaved() = _state.update { it.copy(lastSavedUri = null, lastSavedSizeBytes = 0L) }
}

/**
 * Native Canvas-based Whiteboard Recorder Service.
 * Re-draws the whiteboard into video frames and encodes directly to MP4 via MediaCodec.
 * No screen capture permission (MediaProjection) needed. 100% Free & Unlimited.
 */
class ScreenRecorderService : Service() {

  private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
  private var timerJob: Job? = null
  private var encoder: WhiteboardVideoEncoder? = null
  private var outputPfd: ParcelFileDescriptor? = null
  private var outputUri: Uri? = null

  override fun onBind(intent: Intent?): IBinder? = null

  override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
    when (intent?.action) {
      ACTION_START -> {
        val audio = intent.getBooleanExtra(EXTRA_RECORD_AUDIO, true)
        val qName = intent.getStringExtra(EXTRA_QUALITY) ?: RecordingQuality.P1080.name
        val sessionId = intent.getStringExtra(EXTRA_SESSION_ID)
        val quality = runCatching { RecordingQuality.valueOf(qName) }.getOrDefault(RecordingQuality.P1080)
        startForegroundNotification()
        beginRecording(audio, quality, sessionId)
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
      startForeground(NOTIF_ID, notif, ServiceInfo.FOREGROUND_SERVICE_TYPE_SPECIAL_USE)
    } else {
      startForeground(NOTIF_ID, notif)
    }
  }

  private fun beginRecording(recordAudio: Boolean, quality: RecordingQuality, sessionId: String? = null) {
    // 1. Safety check: free space < 150 MB -> refuse start
    val freeSpace = Environment.getDataDirectory().usableSpace
    if (freeSpace < 150L * 1024 * 1024) {
      RecorderController.update { it.copy(errorMessage = "Not enough free space on the phone") }
      stopSelf()
      return
    }

    try {
      val dm = resources.displayMetrics
      val aspect = dm.widthPixels.toFloat() / dm.heightPixels.coerceAtLeast(1)

      // Device profile: 30 fps for strong phone (3.5 GB+ RAM, not low RAM, API 28+), 24 fps otherwise
      val am = getSystemService(Context.ACTIVITY_SERVICE) as? ActivityManager
      val isLowRam = am?.isLowRamDevice == true
      val memInfo = ActivityManager.MemoryInfo()
      am?.getMemoryInfo(memInfo)
      val totalRamGb = memInfo.totalMem.toDouble() / (1024 * 1024 * 1024)
      val isStrongDevice = !isLowRam && totalRamGb >= 3.4 && Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
      val targetFps = if (isStrongDevice) 30 else 24

      // Dimensions clamped to multiples of 16
      val targetShortSide = when (quality) {
        RecordingQuality.P480 -> 480
        RecordingQuality.P720 -> 720
        RecordingQuality.P1080 -> if (isStrongDevice) 1080 else 720
      }
      val isLandscape = aspect > 1.0f
      val recW: Int
      val recH: Int
      if (isLandscape) {
        recH = (targetShortSide / 16) * 16
        recW = (((recH * aspect).toInt()) / 16).coerceAtLeast(16) * 16
      } else {
        recW = (targetShortSide / 16) * 16
        recH = (((recW / aspect).toInt()) / 16).coerceAtLeast(16) * 16
      }

      // Bitrate calculation
      val targetBitrate = when (quality) {
        RecordingQuality.P480 -> 1_980_000
        RecordingQuality.P720 -> 4_420_000
        RecordingQuality.P1080 -> if (isStrongDevice) 7_460_000 else 4_420_000
      }

      val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.US).format(Date())
      val cleanId = if (!sessionId.isNullOrBlank()) sessionId.replace("-", "").take(8) else ""
      val prefix = if (cleanId.isNotBlank()) "TeachBoard_$cleanId" else "TeachBoard"
      val fileName = "${prefix}_$timeStamp.mp4"

      val values = ContentValues().apply {
        put(MediaStore.Video.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Video.Media.MIME_TYPE, "video/mp4")
        if (!sessionId.isNullOrBlank()) {
          put(MediaStore.Video.Media.DESCRIPTION, "sessionId:$sessionId")
        }
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

      val enc = WhiteboardVideoEncoder(
        pfd = pfd,
        width = recW,
        height = recH,
        fps = targetFps,
        bitrateBps = targetBitrate,
        recordAudio = recordAudio,
        frameSource = RecorderController.frameSource,
        onStorageExhausted = {
          scope.launch {
            RecorderController.update { it.copy(errorMessage = "The phone is almost full. The recording was stopped and saved.") }
            stopAndCleanup()
          }
        },
        onFileLimitReached = {
          scope.launch {
            RecorderController.update { it.copy(errorMessage = "The recording file is very big. It was stopped and saved.") }
            stopAndCleanup()
          }
        }
      )
      enc.start()
      encoder = enc

      RecorderController.update { RecordingState(isRecording = true, isPaused = false, elapsedSeconds = 0) }
      startTimer()
    } catch (e: Exception) {
      RecorderController.update { it.copy(isRecording = false, errorMessage = e.localizedMessage ?: "Could not start recording on this phone") }
      stopAndCleanup()
    }
  }

  private fun startTimer() {
    timerJob?.cancel()
    timerJob = scope.launch {
      while (true) {
        delay(1000L)
        val cur = RecorderController.state.value
        if (!cur.isRecording) break
        if (!cur.isPaused) {
          val next = cur.elapsedSeconds + 1
          RecorderController.update { it.copy(elapsedSeconds = next) }

          // Storage monitor: stop if free space < 40 MB
          val free = Environment.getDataDirectory().usableSpace
          if (free < 40L * 1024 * 1024) {
            RecorderController.update { it.copy(errorMessage = "The phone is almost full. The recording was stopped and saved.") }
            stopAndCleanup()
            break
          }
        }
      }
    }
  }

  private fun togglePause() {
    val enc = encoder ?: return
    val cur = RecorderController.state.value
    if (!cur.isRecording) return
    val nextPaused = !cur.isPaused
    enc.setPaused(nextPaused)
    RecorderController.update { it.copy(isPaused = nextPaused) }
  }

  private fun stopAndCleanup() {
    timerJob?.cancel()
    val wasRecording = RecorderController.state.value.isRecording

    var totalFrames = 0L
    var writtenBytes = 0L
    encoder?.let { enc ->
      totalFrames = enc.stop()
      writtenBytes = enc.getWrittenBytes()
    }
    encoder = null
    runCatching { outputPfd?.close() }
    outputPfd = null

    val saved = outputUri
    if (saved != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      runCatching {
        val values = ContentValues().apply { put(MediaStore.Video.Media.IS_PENDING, 0) }
        contentResolver.update(saved, values, null, null)
      }
    }
    outputUri = null

    // If zero frames produced, treat as failure and remove file
    if (totalFrames <= 0L && saved != null) {
      runCatching { contentResolver.delete(saved, null, null) }
      RecorderController.update {
        it.copy(isRecording = false, isPaused = false, errorMessage = "A recording problem happened. Nothing was saved.")
      }
    } else {
      RecorderController.update {
        it.copy(
          isRecording = false,
          isPaused = false,
          lastSavedUri = if (wasRecording) saved else it.lastSavedUri,
          lastSavedSizeBytes = writtenBytes
        )
      }
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
    const val EXTRA_RECORD_AUDIO = "record_audio"
    const val EXTRA_QUALITY = "quality"
    const val EXTRA_SESSION_ID = "session_id"
  }
}
