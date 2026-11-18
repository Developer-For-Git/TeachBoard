package com.teachboard.app.domain.ink

import com.google.mlkit.common.model.DownloadConditions
import com.google.mlkit.common.model.RemoteModelManager
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognition
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModel
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognitionModelIdentifier
import com.google.mlkit.vision.digitalink.recognition.DigitalInkRecognizerOptions
import com.google.mlkit.vision.digitalink.recognition.Ink
import com.teachboard.app.domain.model.StrokePoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

enum class InkModelState {
  NOT_DOWNLOADED, DOWNLOADING, READY, FAILED
}

data class InkLanguage(
  val tag: String,
  val label: String
)

/**
 * Smart Writing manager powered by on-device ML Kit Digital Ink Recognition 19.0.0 (PRD 19.1 #6 & 19.5).
 * Supports all 18 UI languages from Section 10 with on-demand downloads and offline recognition.
 */
class DigitalInkManager {

  private val modelManager: RemoteModelManager by lazy { RemoteModelManager.getInstance() }

  private val _modelState = MutableStateFlow(InkModelState.NOT_DOWNLOADED)
  val modelState: StateFlow<InkModelState> = _modelState.asStateFlow()

  private val _downloadedTags = MutableStateFlow<Set<String>>(emptySet())
  val downloadedTags: StateFlow<Set<String>> = _downloadedTags.asStateFlow()

  // Exact 18 languages from PRD Section 19.1 #6
  val supportedLanguages: List<InkLanguage> = listOf(
    InkLanguage("en-US", "English"),
    InkLanguage("es", "Español"),
    InkLanguage("fr", "Français"),
    InkLanguage("de", "Deutsch"),
    InkLanguage("pt-BR", "Português"),
    InkLanguage("it", "Italiano"),
    InkLanguage("ru", "Русский"),
    InkLanguage("tr", "Türkçe"),
    InkLanguage("ar", "العربية"),
    InkLanguage("hi", "हिन्दी"),
    InkLanguage("bn", "বাংলা"),
    InkLanguage("ur", "اردو"),
    InkLanguage("id", "Bahasa Indonesia"),
    InkLanguage("vi", "Tiếng Việt"),
    InkLanguage("zh-Hans", "简体中文"),
    InkLanguage("ja", "日本語"),
    InkLanguage("ko", "한국어"),
    InkLanguage("sw", "Kiswahili")
  )

  suspend fun refreshDownloaded() = withContext(Dispatchers.IO) {
    val ready = mutableSetOf<String>()
    for (lang in supportedLanguages) {
      if (isDownloaded(lang.tag)) ready.add(lang.tag)
    }
    _downloadedTags.value = ready
  }

  suspend fun isDownloaded(tag: String): Boolean = withContext(Dispatchers.IO) {
    runCatching {
      val id = resolveIdentifier(tag) ?: return@withContext false
      val model = DigitalInkRecognitionModel.builder(id).build()
      modelManager.isModelDownloaded(model).await()
    }.getOrDefault(false)
  }

  suspend fun downloadModel(tag: String): Boolean = withContext(Dispatchers.IO) {
    _modelState.value = InkModelState.DOWNLOADING
    val ok = runCatching {
      val id = resolveIdentifier(tag) ?: return@withContext false
      val model = DigitalInkRecognitionModel.builder(id).build()
      if (!modelManager.isModelDownloaded(model).await()) {
        modelManager.download(model, DownloadConditions.Builder().build()).await()
      }
      true
    }.getOrDefault(false)

    _modelState.value = if (ok) InkModelState.READY else InkModelState.FAILED
    if (ok) _downloadedTags.value = _downloadedTags.value + tag
    ok
  }

  suspend fun recognize(strokes: List<List<StrokePoint>>, languageTag: String): Result<String> =
    withContext(Dispatchers.IO) {
      if (strokes.isEmpty()) return@withContext Result.failure(IllegalArgumentException("Empty strokes"))
      runCatching {
        val id = resolveIdentifier(languageTag)
          ?: DigitalInkRecognitionModelIdentifier.fromLanguageTag("en-US")!!
        val model = DigitalInkRecognitionModel.builder(id).build()
        if (!modelManager.isModelDownloaded(model).await()) {
          _modelState.value = InkModelState.DOWNLOADING
          modelManager.download(model, DownloadConditions.Builder().build()).await()
        }
        _modelState.value = InkModelState.READY
        _downloadedTags.value = _downloadedTags.value + languageTag

        val inkBuilder = Ink.builder()
        for (stroke in strokes) {
          if (stroke.isEmpty()) continue
          val sb = Ink.Stroke.builder()
          for (pt in stroke) {
            sb.addPoint(Ink.Point.create(pt.x, pt.y, pt.t))
          }
          inkBuilder.addStroke(sb.build())
        }

        val recognizer = DigitalInkRecognition.getClient(
          DigitalInkRecognizerOptions.builder(model).build()
        )
        try {
          val res = recognizer.recognize(inkBuilder.build()).await()
          val text = res.candidates.firstOrNull()?.text?.trim().orEmpty()
          if (text.isBlank()) throw IllegalStateException("No text recognized")
          text
        } finally {
          recognizer.close()
        }
      }.onFailure {
        _modelState.value = InkModelState.FAILED
      }
    }

  private fun resolveIdentifier(tag: String): DigitalInkRecognitionModelIdentifier? {
    return runCatching { DigitalInkRecognitionModelIdentifier.fromLanguageTag(tag) }.getOrNull()
      ?: if (tag == "zh-Hans") runCatching { DigitalInkRecognitionModelIdentifier.fromLanguageTag("zh-Hani-CN") }.getOrNull()
      else null
  }
}
