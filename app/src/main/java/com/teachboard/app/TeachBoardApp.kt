package com.teachboard.app

import android.app.Application
import coil.ImageLoader
import coil.ImageLoaderFactory
import com.teachboard.app.data.PrefsRepository
import com.teachboard.app.data.local.AppDatabase
import com.teachboard.app.domain.ink.DigitalInkManager
import com.teachboard.app.domain.ocr.OcrManager
import com.teachboard.app.ui.theme.AppConfig
import okhttp3.OkHttpClient

/**
 * Application singleton owning Room database, DataStore preferences, and ML Kit managers.
 * Implements ImageLoaderFactory to supply Coil with a custom OkHttpClient setting the
 * descriptive User-Agent required by Wikimedia Commons and external image CDNs.
 */
class TeachBoardApp : Application(), ImageLoaderFactory {

  val database: AppDatabase by lazy { AppDatabase.get(this) }
  val prefs: PrefsRepository by lazy { PrefsRepository(this) }
  val digitalInkManager: DigitalInkManager by lazy { DigitalInkManager() }
  val ocrManager: OcrManager by lazy { OcrManager(this, database.ocrScanDao()) }

  override fun newImageLoader(): ImageLoader {
    val okHttpClient = OkHttpClient.Builder()
      .addNetworkInterceptor { chain ->
        val req = chain.request().newBuilder()
          .header("User-Agent", AppConfig.USER_AGENT)
          .build()
        chain.proceed(req)
      }
      .build()

    return ImageLoader.Builder(this)
      .okHttpClient(okHttpClient)
      .crossfade(true)
      .build()
  }
}
