package com.teachboard.app.domain.model

import com.teachboard.app.ui.theme.TeachColors
import java.util.UUID

/**
 * Board texture / background styles matching PRD Section 4.4 (11 textures).
 */
enum class BackgroundStyle {
  PLAIN,
  GRID,
  LINES,
  DOTS,
  CHALK,
  ISOMETRIC,
  GRAPH,
  MUSIC,
  DIAMOND,
  NOTEBOOK,
  WHITEBOARD;

  companion object {
    fun fromNameOrDefault(name: String): BackgroundStyle =
      runCatching { valueOf(name) }.getOrDefault(PLAIN)
  }
}

data class StrokePoint(
  val x: Float,
  val y: Float,
  val pressure: Float = 0.5f,
  val t: Long = 0L
)

data class Contour(
  val closed: Boolean,
  val points: List<StrokePoint>
)

data class EraserSwipe(
  val width: Float,
  val points: List<StrokePoint>
)

data class BoardSession(
  val id: String = UUID.randomUUID().toString(),
  val title: String,
  val createdAt: Long = System.currentTimeMillis(),
  val updatedAt: Long = System.currentTimeMillis(),
  val pageCount: Int = 1,
  val backgroundColor: Int = TeachColors.BoardWhite,
  val backgroundStyle: BackgroundStyle = BackgroundStyle.PLAIN,
  val pdfPath: String? = null,
  val pdfHiddenPages: Set<Int> = emptySet(),
  val pdfBookmarks: Set<Int> = emptySet()
)

data class BoardLayer(
  val id: String = UUID.randomUUID().toString(),
  val sessionId: String,
  val name: String = "Layer 1",
  val zOrder: Int = 0,
  val visible: Boolean = true,
  val locked: Boolean = false
)

/**
 * Unified object model persisted in Room `canvas_objects` (PRD 19.3, 19.6 & F-01).
 * Covers Stroke, Shape, Eraser-journal, Text, Formula, Table, Image, and Sticker.
 */
data class CanvasObject(
  val id: Long = 0L,
  val objectUid: String = UUID.randomUUID().toString(),
  val sessionId: String,
  val pageIndex: Int = 0,
  val layerId: String,
  val type: ToolType = ToolType.Pen,
  val color: Int = TeachColors.BrushBlack,
  val fillColor: Int? = null,
  val isFilled: Boolean = false,
  val strokeWidth: Float = 20f,
  val alpha: Float = 1f,
  val isEraser: Boolean = false,
  val zIndex: Int = 0,
  val points: List<StrokePoint> = emptyList(),
  val geometry: List<Contour> = emptyList(),
  val eraserStrokes: List<EraserSwipe> = emptyList(),
  val textContent: String? = null, // Text string, LaTeX formula, Table payload, or Sticker ID
  val imagePath: String? = null,   // Local file path in filesDir/images/ (plus optional attribution in textContent)
  val fontSize: Float = 28f,
  val updatedAt: Long = System.currentTimeMillis()
)

/**
 * Compact command-pattern Undo/Redo step (PRD 19.1 #2 — 60-step cap).
 * Stores only the small delta (added/removed/replaced objects), never full page snapshots.
 */
sealed class UndoCommand {
  data class Add(val obj: CanvasObject) : UndoCommand()
  data class Remove(val objects: List<CanvasObject>) : UndoCommand()
  data class Replace(val before: List<CanvasObject>, val after: List<CanvasObject>) : UndoCommand()
}

data class OcrScanRecord(
  val id: Long = 0L,
  val sessionId: String?,
  val imageThumb: String?,
  val recognizedText: String,
  val language: String,
  val createdAt: Long
)
