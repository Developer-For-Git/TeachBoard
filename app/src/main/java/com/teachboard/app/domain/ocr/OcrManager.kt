package com.teachboard.app.domain.ocr

import android.content.Context
import android.graphics.Bitmap
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.chinese.ChineseTextRecognizerOptions
import com.google.mlkit.vision.text.devanagari.DevanagariTextRecognizerOptions
import com.google.mlkit.vision.text.japanese.JapaneseTextRecognizerOptions
import com.google.mlkit.vision.text.korean.KoreanTextRecognizerOptions
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.teachboard.app.data.local.OcrScanDao
import com.teachboard.app.data.local.OcrScanEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream

enum class OcrScript(val label: String, val supportedOnDevice: Boolean = true) {
  AUTO("Auto"),
  LATIN("Latin"),
  DEVANAGARI("Devanagari"),
  CHINESE("Chinese"),
  JAPANESE("Japanese"),
  KOREAN("Korean"),
  ARABIC("Arabic", supportedOnDevice = false)
}

data class OcrOutput(
  val text: String,
  val script: OcrScript,
  val supported: Boolean = true,
  val message: String? = null
)

/**
 * Clean OCR interface so a Pro cloud engine can plug in later without changing UI (PRD 6.9 & 9).
 */
interface OcrEngine {
  suspend fun recognize(bitmap: Bitmap, script: OcrScript = OcrScript.AUTO): OcrOutput
}

/**
 * On-device ML Kit Text Recognition manager (PRD 19.1 #7 & 19.9):
 * - Auto, Latin, Devanagari, Chinese, Japanese, Korean (on-device, offline, unlimited)
 * - Arabic / unsupported scripts honestly report "Not supported yet"
 * - Downscales image to max 1920 px before OCR
 * - Saves last 20 scans to Room `ocr_scans` + thumbnails in `filesDir/ocr/`
 */
class OcrManager(
  private val context: Context,
  private val ocrScanDao: OcrScanDao
) : OcrEngine {

  override suspend fun recognize(bitmap: Bitmap, script: OcrScript): OcrOutput =
    withContext(Dispatchers.IO) {
      if (!script.supportedOnDevice) {
        return@withContext OcrOutput(
          text = "",
          script = script,
          supported = false,
          message = "Not supported yet"
        )
      }
      val scaled = downscale(bitmap, maxSide = 1920)
      try {
        if (script == OcrScript.AUTO) {
          val latin = runSingle(scaled, OcrScript.LATIN)
          if (latin.isNotBlank()) return@withContext OcrOutput(latin, OcrScript.LATIN)
          for (fb in listOf(OcrScript.DEVANAGARI, OcrScript.CHINESE, OcrScript.JAPANESE, OcrScript.KOREAN)) {
            val txt = runCatching { runSingle(scaled, fb) }.getOrDefault("")
            if (txt.isNotBlank()) return@withContext OcrOutput(txt, fb)
          }
          OcrOutput("", OcrScript.LATIN, message = "Hold steady, good light")
        } else {
          val txt = runSingle(scaled, script)
          OcrOutput(
            text = txt,
            script = script,
            message = if (txt.isBlank()) "Hold steady, good light" else null
          )
        }
      } catch (e: Exception) {
        OcrOutput("", script, supported = true, message = e.localizedMessage ?: "OCR failed")
      } finally {
        if (scaled !== bitmap && !scaled.isRecycled) scaled.recycle()
      }
    }

  suspend fun saveScan(sessionId: String?, bitmap: Bitmap, text: String, script: OcrScript) =
    withContext(Dispatchers.IO) {
      if (text.isBlank()) return@withContext
      val thumbPath = runCatching {
        val dir = File(context.filesDir, "ocr").apply { mkdirs() }
        val file = File(dir, "scan_${System.currentTimeMillis()}.jpg")
        val thumb = downscale(bitmap, maxSide = 260)
        FileOutputStream(file).use { out -> thumb.compress(Bitmap.CompressFormat.JPEG, 82, out) }
        if (thumb !== bitmap && !thumb.isRecycled) thumb.recycle()
        file.absolutePath
      }.getOrNull()

      ocrScanDao.insert(
        OcrScanEntity(
          sessionId = sessionId,
          imageThumb = thumbPath,
          recognizedText = text,
          language = script.label,
          createdAt = System.currentTimeMillis()
        )
      )
      ocrScanDao.trimToLast20()
    }

  private suspend fun runSingle(bitmap: Bitmap, script: OcrScript): String {
    val recognizer = when (script) {
      OcrScript.CHINESE -> TextRecognition.getClient(ChineseTextRecognizerOptions.Builder().build())
      OcrScript.DEVANAGARI -> TextRecognition.getClient(DevanagariTextRecognizerOptions.Builder().build())
      OcrScript.JAPANESE -> TextRecognition.getClient(JapaneseTextRecognizerOptions.Builder().build())
      OcrScript.KOREAN -> TextRecognition.getClient(KoreanTextRecognizerOptions.Builder().build())
      else -> TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    }
    return try {
      recognizer.process(InputImage.fromBitmap(bitmap, 0)).await().text.trim()
    } finally {
      recognizer.close()
    }
  }

  private fun downscale(source: Bitmap, maxSide: Int): Bitmap {
    val largest = maxOf(source.width, source.height)
    if (largest <= maxSide) return source
    val s = maxSide.toFloat() / largest.toFloat()
    return Bitmap.createScaledBitmap(
      source,
      (source.width * s).toInt().coerceAtLeast(1),
      (source.height * s).toInt().coerceAtLeast(1),
      true
    )
  }
}
