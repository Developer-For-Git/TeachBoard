package com.teachboard.app.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface SessionDao {
  @Query("SELECT * FROM sessions ORDER BY updatedAt DESC")
  fun observeAll(): Flow<List<SessionEntity>>

  @Query("SELECT * FROM sessions ORDER BY updatedAt DESC")
  suspend fun getAll(): List<SessionEntity>

  @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
  suspend fun getById(id: String): SessionEntity?

  @Query("SELECT * FROM sessions WHERE id = :id LIMIT 1")
  fun observeById(id: String): Flow<SessionEntity?>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(session: SessionEntity)

  @Query("UPDATE sessions SET title = :title, updatedAt = :updatedAt WHERE id = :id")
  suspend fun rename(id: String, title: String, updatedAt: Long)

  @Query("DELETE FROM sessions WHERE id = :id")
  suspend fun deleteById(id: String)

  @Query("DELETE FROM sessions")
  suspend fun deleteAll()
}

@Dao
interface LayerDao {
  @Query("SELECT * FROM layers WHERE sessionId = :sessionId ORDER BY zOrder ASC")
  fun observeForSession(sessionId: String): Flow<List<LayerEntity>>

  @Query("SELECT * FROM layers WHERE sessionId = :sessionId ORDER BY zOrder ASC")
  suspend fun getForSession(sessionId: String): List<LayerEntity>

  @Query("SELECT * FROM layers")
  suspend fun getAll(): List<LayerEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(layer: LayerEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(layers: List<LayerEntity>)

  @Query("DELETE FROM layers WHERE id = :layerId")
  suspend fun deleteById(layerId: String)

  @Query("DELETE FROM layers WHERE sessionId = :sessionId")
  suspend fun deleteForSession(sessionId: String)

  @Query("DELETE FROM layers")
  suspend fun deleteAll()
}

@Dao
interface CanvasObjectDao {
  @Query("SELECT * FROM canvas_objects WHERE sessionId = :sessionId AND pageIndex = :pageIndex ORDER BY zIndex ASC")
  fun observeForPage(sessionId: String, pageIndex: Int): Flow<List<CanvasObjectEntity>>

  @Query("SELECT * FROM canvas_objects WHERE sessionId = :sessionId AND pageIndex = :pageIndex ORDER BY zIndex ASC")
  suspend fun getForPage(sessionId: String, pageIndex: Int): List<CanvasObjectEntity>

  @Query("SELECT * FROM canvas_objects WHERE sessionId = :sessionId ORDER BY pageIndex ASC, zIndex ASC")
  suspend fun getForSession(sessionId: String): List<CanvasObjectEntity>

  @Query("SELECT * FROM canvas_objects")
  suspend fun getAll(): List<CanvasObjectEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsert(obj: CanvasObjectEntity)

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun upsertAll(objects: List<CanvasObjectEntity>)

  @Query("DELETE FROM canvas_objects WHERE objectUid = :uid")
  suspend fun deleteByUid(uid: String)

  @Query("DELETE FROM canvas_objects WHERE objectUid IN (:uids)")
  suspend fun deleteByUids(uids: List<String>)

  @Query("DELETE FROM canvas_objects WHERE sessionId = :sessionId AND pageIndex = :pageIndex")
  suspend fun deletePage(sessionId: String, pageIndex: Int)

  @Query("DELETE FROM canvas_objects WHERE sessionId = :sessionId AND layerId = :layerId")
  suspend fun deleteLayerObjects(sessionId: String, layerId: String)

  @Query("DELETE FROM canvas_objects WHERE sessionId = :sessionId")
  suspend fun deleteForSession(sessionId: String)

  @Query("DELETE FROM canvas_objects")
  suspend fun deleteAll()
}

@Dao
interface OcrScanDao {
  @Query("SELECT * FROM ocr_scans ORDER BY createdAt DESC LIMIT 20")
  fun observeRecent(): Flow<List<OcrScanEntity>>

  @Query("SELECT * FROM ocr_scans ORDER BY createdAt DESC")
  suspend fun getAll(): List<OcrScanEntity>

  @Insert(onConflict = OnConflictStrategy.REPLACE)
  suspend fun insert(scan: OcrScanEntity)

  @Query("DELETE FROM ocr_scans WHERE id = :id")
  suspend fun deleteById(id: Long)

  @Query("DELETE FROM ocr_scans WHERE id NOT IN (SELECT id FROM ocr_scans ORDER BY createdAt DESC LIMIT 20)")
  suspend fun trimToLast20()

  @Query("DELETE FROM ocr_scans")
  suspend fun deleteAll()
}
