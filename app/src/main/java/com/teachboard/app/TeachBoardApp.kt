package com.teachboard.app

import android.app.Application
import com.teachboard.app.data.PrefsRepository
import com.teachboard.app.data.local.AppDatabase
import com.teachboard.app.domain.ink.DigitalInkManager
import com.teachboard.app.domain.ocr.OcrManager

/**
 * Application singleton owning Room database, DataStore preferences, and ML Kit managers.
 * Simple constructor wiring — no Hilt (PRD Section 19.2).
 */
class TeachBoardApp : Application() {

  val database: AppDatabase by lazy { AppDatabase.get(this) }
  val prefs: PrefsRepository by lazy { PrefsRepository(this) }
  val digitalInkManager: DigitalInkManager by lazy { DigitalInkManager() }
  val ocrManager: OcrManager by lazy { OcrManager(this, database.ocrScanDao()) }
}
