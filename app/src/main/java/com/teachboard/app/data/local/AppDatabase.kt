package com.teachboard.app.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

/**
 * Room singleton database `teachboard.db` (version 1, PRD Section 19.6).
 */
@Database(
  entities = [
    SessionEntity::class,
    LayerEntity::class,
    CanvasObjectEntity::class,
    OcrScanEntity::class
  ],
  version = 1,
  exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
  abstract fun sessionDao(): SessionDao
  abstract fun layerDao(): LayerDao
  abstract fun canvasObjectDao(): CanvasObjectDao
  abstract fun ocrScanDao(): OcrScanDao

  companion object {
    @Volatile
    private var instance: AppDatabase? = null

    fun get(context: Context): AppDatabase {
      return instance ?: synchronized(this) {
        instance ?: Room.databaseBuilder(
          context.applicationContext,
          AppDatabase::class.java,
          "teachboard.db"
        ).fallbackToDestructiveMigration(false)
          .build()
          .also { instance = it }
      }
    }
  }
}
