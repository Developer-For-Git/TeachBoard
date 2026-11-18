package com.teachboard.app.data.local

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.teachboard.app.domain.model.BackgroundStyle
import com.teachboard.app.domain.model.BoardLayer
import com.teachboard.app.domain.model.BoardSession
import com.teachboard.app.domain.model.CanvasObject
import com.teachboard.app.domain.model.OcrScanRecord
import com.teachboard.app.domain.model.Serialization
import com.teachboard.app.domain.model.ToolType

/**
 * Exact Room schema from PRD Section 19.6 (`teachboard.db`, v1).
 */
@Entity(tableName = "sessions")
data class SessionEntity(
  @PrimaryKey val id: String,
  val title: String,
  val createdAt: Long,
  val updatedAt: Long,
  val pageCount: Int,
  val backgroundColor: Int,
  val backgroundStyle: String,
  val pdfPath: String? = null,
  val pdfHiddenPages: String = "",
  val pdfBookmarks: String = ""
) {
  fun toDomain(): BoardSession = BoardSession(
    id = id,
    title = title,
    createdAt = createdAt,
    updatedAt = updatedAt,
    pageCount = pageCount,
    backgroundColor = backgroundColor,
    backgroundStyle = BackgroundStyle.fromNameOrDefault(backgroundStyle),
    pdfPath = pdfPath,
    pdfHiddenPages = Serialization.decodeIntSetCsv(pdfHiddenPages),
    pdfBookmarks = Serialization.decodeIntSetCsv(pdfBookmarks)
  )

  companion object {
    fun fromDomain(s: BoardSession): SessionEntity = SessionEntity(
      id = s.id,
      title = s.title,
      createdAt = s.createdAt,
      updatedAt = s.updatedAt,
      pageCount = s.pageCount,
      backgroundColor = s.backgroundColor,
      backgroundStyle = s.backgroundStyle.name,
      pdfPath = s.pdfPath,
      pdfHiddenPages = Serialization.encodeIntSetCsv(s.pdfHiddenPages),
      pdfBookmarks = Serialization.encodeIntSetCsv(s.pdfBookmarks)
    )
  }
}

@Entity(
  tableName = "layers",
  indices = [Index(value = ["sessionId"])]
)
data class LayerEntity(
  @PrimaryKey val id: String,
  val sessionId: String,
  val name: String,
  val zOrder: Int,
  val visible: Int,
  val locked: Int
) {
  fun toDomain(): BoardLayer = BoardLayer(
    id = id,
    sessionId = sessionId,
    name = name,
    zOrder = zOrder,
    visible = visible != 0,
    locked = locked != 0
  )

  companion object {
    fun fromDomain(l: BoardLayer): LayerEntity = LayerEntity(
      id = l.id,
      sessionId = l.sessionId,
      name = l.name,
      zOrder = l.zOrder,
      visible = if (l.visible) 1 else 0,
      locked = if (l.locked) 1 else 0
    )
  }
}

@Entity(
  tableName = "canvas_objects",
  indices = [
    Index(value = ["sessionId", "pageIndex"]),
    Index(value = ["objectUid"], unique = true)
  ]
)
data class CanvasObjectEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val objectUid: String,
  val sessionId: String,
  val pageIndex: Int,
  val layerId: String,
  val type: String,
  val color: Int,
  val fillColor: Int?,
  val isFilled: Int,
  val strokeWidth: Float,
  val alpha: Float,
  val isEraser: Int,
  val zIndex: Int,
  val points: String,
  val geometry: String?,
  val eraserStrokes: String?,
  val textContent: String?,
  val imagePath: String?,
  val fontSize: Float,
  val updatedAt: Long
) {
  fun toDomain(): CanvasObject = CanvasObject(
    id = id,
    objectUid = objectUid,
    sessionId = sessionId,
    pageIndex = pageIndex,
    layerId = layerId,
    type = ToolType.fromNameOrDefault(type),
    color = color,
    fillColor = fillColor,
    isFilled = isFilled != 0,
    strokeWidth = strokeWidth,
    alpha = alpha,
    isEraser = isEraser != 0,
    zIndex = zIndex,
    points = Serialization.decodePoints(points),
    geometry = Serialization.decodeGeometry(geometry),
    eraserStrokes = Serialization.decodeEraserStrokes(eraserStrokes),
    textContent = textContent,
    imagePath = imagePath,
    fontSize = fontSize,
    updatedAt = updatedAt
  )

  companion object {
    fun fromDomain(o: CanvasObject): CanvasObjectEntity = CanvasObjectEntity(
      id = o.id,
      objectUid = o.objectUid,
      sessionId = o.sessionId,
      pageIndex = o.pageIndex,
      layerId = o.layerId,
      type = o.type.name,
      color = o.color,
      fillColor = o.fillColor,
      isFilled = if (o.isFilled) 1 else 0,
      strokeWidth = o.strokeWidth,
      alpha = o.alpha,
      isEraser = if (o.isEraser) 1 else 0,
      zIndex = o.zIndex,
      points = Serialization.encodePoints(o.points),
      geometry = Serialization.encodeGeometry(o.geometry),
      eraserStrokes = Serialization.encodeEraserStrokes(o.eraserStrokes),
      textContent = o.textContent,
      imagePath = o.imagePath,
      fontSize = o.fontSize,
      updatedAt = o.updatedAt
    )
  }
}

@Entity(tableName = "ocr_scans")
data class OcrScanEntity(
  @PrimaryKey(autoGenerate = true) val id: Long = 0L,
  val sessionId: String?,
  val imageThumb: String?,
  val recognizedText: String,
  val language: String,
  val createdAt: Long
) {
  fun toDomain(): OcrScanRecord = OcrScanRecord(
    id = id,
    sessionId = sessionId,
    imageThumb = imageThumb,
    recognizedText = recognizedText,
    language = language,
    createdAt = createdAt
  )
}

fun BoardSession.toEntity(): SessionEntity = SessionEntity.fromDomain(this)
fun BoardLayer.toEntity(): LayerEntity = LayerEntity.fromDomain(this)
fun CanvasObject.toEntity(): CanvasObjectEntity = CanvasObjectEntity.fromDomain(this)
