package com.teachboard.app.ui.canvas

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Matrix
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.SystemClock
import android.util.LruCache
import android.view.HapticFeedbackConstants
import android.view.MotionEvent
import android.view.View
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.viewinterop.AndroidView
import com.teachboard.app.domain.model.BackgroundStyle
import com.teachboard.app.domain.model.BoardLayer
import com.teachboard.app.domain.model.CanvasObject
import com.teachboard.app.domain.model.EraserMode
import com.teachboard.app.domain.model.GeometryToolType
import com.teachboard.app.domain.model.StrokePoint
import com.teachboard.app.domain.model.ToolType
import com.teachboard.app.ui.canvas.geometry.GeoCallbacks
import com.teachboard.app.ui.canvas.geometry.GeoController
import com.teachboard.app.ui.canvas.geometry.GeoInstrument
import com.teachboard.app.ui.canvas.geometry.GeoStampKind
import com.teachboard.app.ui.canvas.geometry.P
import com.teachboard.app.ui.theme.AppConfig
import com.teachboard.app.ui.theme.TeachColors
import java.io.File
import kotlin.math.atan2
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min

interface CanvasCallbacks {
  fun onCommitStroke(tool: ToolType, points: List<StrokePoint>, isEraser: Boolean)
  fun onCommitShape(tool: ToolType, start: StrokePoint, end: StrokePoint)
  fun onStrokeEraseAt(x: Float, y: Float, radius: Float)
  fun onSmartInkStroke(points: List<StrokePoint>)
  fun onTapPlaceObject(tool: ToolType, worldX: Float, worldY: Float)
  fun onSelectionChanged(selectedUids: Set<String>)
  fun onMoveSelection(dx: Float, dy: Float)
  fun onScaleSelection(anchorX: Float, anchorY: Float, factor: Float)
  fun onScaleSelectionAxes(anchorX: Float, anchorY: Float, sx: Float, sy: Float)
  fun onRotateSelection(anchorX: Float, anchorY: Float, degrees: Float)
  fun onFlipSelectionHorizontal()
  fun onDoubleTapObject(obj: CanvasObject)
  fun onViewportChanged(scale: Float, panX: Float, panY: Float)
  fun onSelectionScreenBoundsChanged(bounds: RectF?)
  fun onCanvasTouched()
  fun onStampLine(a: StrokePoint, b: StrokePoint) = Unit
  fun onStampArc(needle: StrokePoint, start: StrokePoint, end: StrokePoint) = Unit
  fun onStampAngle(pivot: StrokePoint, armEnd: StrokePoint, baseEnd: StrokePoint, kind: GeoStampKind) = Unit
  fun onDismissGeometry() = Unit
}

@Composable
fun CanvasBoard(
  modifier: Modifier = Modifier,
  backgroundColor: Int,
  backgroundStyle: BackgroundStyle,
  pdfBitmap: Bitmap?,
  layers: List<BoardLayer>,
  objects: List<CanvasObject>,
  activeTool: ToolType,
  eraserMode: EraserMode,
  brushColor: Int,
  fillColor: Int?,
  isFilled: Boolean,
  strokeWidth: Float,
  alpha: Float,
  laserColor: Int,
  laserSize: Float,
  palmRejection: Boolean,
  stylusOnly: Boolean,
  zoom: Float,
  panX: Float,
  panY: Float,
  selectedUids: Set<String>,
  lockedObjectUids: Set<String>,
  smartInkPreviewStrokes: List<List<StrokePoint>>,
  activeGeometryTool: GeometryToolType? = null,
  callbacks: CanvasCallbacks,
  onViewReady: (TeachCanvasView) -> Unit = {}
) {
  AndroidView(
    modifier = modifier,
    factory = { ctx -> TeachCanvasView(ctx).also(onViewReady) },
    update = { view ->
      view.callbacks = callbacks
      view.updateState(
        bgColor = backgroundColor,
        bgStyle = backgroundStyle,
        pdf = pdfBitmap,
        layers = layers,
        objects = objects,
        tool = activeTool,
        eraserMode = eraserMode,
        brushColor = brushColor,
        fillColor = fillColor,
        isFilled = isFilled,
        strokeWidth = strokeWidth,
        alpha = alpha,
        laserColor = laserColor,
        laserSize = laserSize,
        palmRejection = palmRejection,
        stylusOnly = stylusOnly,
        zoom = zoom,
        panX = panX,
        panY = panY,
        selectedUids = selectedUids,
        lockedObjectUids = lockedObjectUids,
        smartInkPreviewStrokes = smartInkPreviewStrokes,
        activeGeometryTool = activeGeometryTool
      )
    }
  )
}

/**
 * High-performance 120fps Whiteboard View:
 * - Incremental offscreen ink bitmap painting (paints ONLY newly appended strokes in <0.2ms!).
 * - Zero-flicker synchronous commit handoff with CanvasViewModel.
 * - Direct object manipulation (move, 4-corner resize, rotate, double-tap edit) in ANY tool mode.
 * - "Hold to move" 340ms long-press gesture with haptic feedback to grab any object while in Pen mode.
 * - Clean 2-finger pan/zoom (0.3x–6x) + palm rejection + S-Pen eraser button + 1s fading laser trail.
 */
class TeachCanvasView(context: Context) : View(context) {

  var callbacks: CanvasCallbacks? = null

  private var bgColor: Int = TeachColors.BoardWhite
  private var bgStyle: BackgroundStyle = BackgroundStyle.PLAIN
  private var pdfBitmap: Bitmap? = null
  private var layers: List<BoardLayer> = emptyList()
  private var objects: List<CanvasObject> = emptyList()
  private var activeTool: ToolType = ToolType.Pen
  private var eraserMode: EraserMode = EraserMode.PIXEL_INK
  private var brushColor: Int = TeachColors.BrushBlack
  private var fillColor: Int? = null
  private var isFilled: Boolean = false
  private var strokeWidth: Float = 12f
  private var alpha: Float = 1f
  private var laserColor: Int = TeachColors.LaserRed
  private var laserSize: Float = 16f
  private var palmRejection: Boolean = true
  private var stylusOnly: Boolean = false
  private var selectedUids: Set<String> = emptySet()
  private var lockedObjectUids: Set<String> = emptySet()
  private var smartInkPreviewStrokes: List<List<StrokePoint>> = emptyList()
  private var activeGeometryTool: GeometryToolType? = null

  private val geoCallbacks = object : GeoCallbacks {
    override fun onDismiss() {
      callbacks?.onDismissGeometry()
    }

    override fun onStampLine(a: P, b: P) {
      callbacks?.onStampLine(StrokePoint(a.x, a.y), StrokePoint(b.x, b.y))
    }

    override fun onStampArc(needle: P, start: P, end: P) {
      callbacks?.onStampArc(StrokePoint(needle.x, needle.y), StrokePoint(start.x, start.y), StrokePoint(end.x, end.y))
    }

    override fun onStampAngle(pivot: P, armEnd: P, baseEnd: P, kind: GeoStampKind) {
      callbacks?.onStampAngle(
        StrokePoint(pivot.x, pivot.y),
        StrokePoint(armEnd.x, armEnd.y),
        StrokePoint(baseEnd.x, baseEnd.y),
        kind
      )
    }
  }

  val geo = GeoController(geoCallbacks)

  private var viewScale: Float = 1f
  private var viewPanX: Float = 0f
  private var viewPanY: Float = 0f

  // Incremental committed ink bitmap cache
  private var committedBitmap: Bitmap? = null
  private var committedDirty: Boolean = true
  private var paintedObjects: List<CanvasObject> = emptyList()
  private var paintedSelectedUids: Set<String> = emptySet()
  private var paintedViewportKey: Int = 0
  private var paintedLayersKey: Int = 0
  private var paintedBgDark: Boolean = false

  // Image cache (24 MB cap)
  private val imageCache = object : LruCache<String, Bitmap>(24 * 1024) {
    override fun sizeOf(key: String, value: Bitmap): Int = (value.byteCount / 1024).coerceAtLeast(1)
  }

  // Live drawing / interaction state
  private val livePoints = mutableListOf<StrokePoint>()
  private var liveStartPoint: StrokePoint? = null
  private var liveEndPoint: StrokePoint? = null
  private var activePointerId: Int = MotionEvent.INVALID_POINTER_ID
  private var isTemporaryEraser: Boolean = false
  private var isTwoFingerGesturing: Boolean = false
  private var prevPinchDist: Float = 0f
  private var prevMidX: Float = 0f
  private var prevMidY: Float = 0f
  private var handLastX: Float = 0f
  private var handLastY: Float = 0f

  // Direct selection manipulation state (works in ANY tool mode!)
  private val lassoPathPoints = mutableListOf<StrokePoint>()
  private var draggingSelection: Boolean = false
  private var scalingSelection: Boolean = false
  private var scalingWidthOnly: Boolean = false
  private var scalingHeightOnly: Boolean = false
  private var rotatingSelection: Boolean = false
  private var dragLastWorldX: Float = 0f
  private var dragLastWorldY: Float = 0f
  private var liveDragDx: Float = 0f
  private var liveDragDy: Float = 0f
  private var liveScaleFactor: Float = 1f
  private var liveScaleX: Float = 1f
  private var liveScaleY: Float = 1f
  private var liveRotateDeg: Float = 0f
  private var transformAnchorX: Float = 0f
  private var transformAnchorY: Float = 0f
  private var transformDirX: Float = 1f
  private var transformDirY: Float = 1f
  private var transformStartDist: Float = 1f
  private var transformStartAngle: Float = 0f
  private var lastReportedBounds: RectF? = null

  // Hold-to-move long-press detector
  private var downScreenX: Float = 0f
  private var downScreenY: Float = 0f
  private var holdCandidateUid: String? = null
  private var holdMovedTooFar: Boolean = false
  private val holdToMoveRunnable = Runnable {
    val uid = holdCandidateUid ?: return@Runnable
    if (!holdMovedTooFar && !isTwoFingerGesturing && activePointerId != MotionEvent.INVALID_POINTER_ID) {
      // Cancel tiny dot, trigger haptic, grab object immediately!
      livePoints.clear()
      liveStartPoint = null
      liveEndPoint = null
      performHapticFeedback(HapticFeedbackConstants.LONG_PRESS)
      callbacks?.onSelectionChanged(setOf(uid))
      selectedUids = setOf(uid)
      draggingSelection = uid !in lockedObjectUids
      liveDragDx = 0f
      liveDragDy = 0f
      invalidate()
    }
  }

  private var lastTapTime: Long = 0L
  private var lastTapX: Float = 0f
  private var lastTapY: Float = 0f
  private var lastStylusSeenMs: Long = 0L

  // Laser fading trail (1000ms)
  private data class LaserDot(val x: Float, val y: Float, val timeMs: Long)
  private val laserTrail = mutableListOf<LaserDot>()

  // Eraser live circle cursor
  private var cursorWorldX: Float? = null
  private var cursorWorldY: Float? = null

  private val viewMatrix = Matrix()
  private val invMatrix = Matrix()

  private val selectionStrokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    color = 0xFF94A3B8.toInt()
    strokeWidth = 3f
    pathEffect = DashPathEffect(floatArrayOf(12f, 8f), 0f)
  }
  private val selectionFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
    color = 0x102563EB
  }
  private val handleShadowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
    color = 0x30000000
  }
  private val handlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
    color = 0xFF2B6CF6.toInt()
  }
  private val handleWhitePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
    color = 0xFFFFFFFF.toInt()
  }
  private val handleBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    color = 0xFFFFFFFF.toInt()
    strokeWidth = 4f
  }
  private val handleIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    color = 0xFFFFFFFF.toInt()
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
    strokeWidth = 3.2f
  }
  private val greenBorderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    color = 0xFF16A34A.toInt()
    strokeWidth = 3.2f
  }
  private val greenIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    color = 0xFF16A34A.toInt()
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
    strokeWidth = 2.8f
  }
  private val eraserCursorFill = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
    color = TeachColors.EraserCursorFill
  }
  private val eraserCursorStroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    color = TeachColors.EraserCursorStroke
    strokeWidth = 2.5f
  }
  private val laserGlowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.STROKE
    strokeCap = Paint.Cap.ROUND
    strokeJoin = Paint.Join.ROUND
  }
  private val laserCorePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
  }

  private val rebuildCacheRunnable = Runnable {
    committedDirty = true
    invalidate()
  }

  fun updateState(
    bgColor: Int,
    bgStyle: BackgroundStyle,
    pdf: Bitmap?,
    layers: List<BoardLayer>,
    objects: List<CanvasObject>,
    tool: ToolType,
    eraserMode: EraserMode,
    brushColor: Int,
    fillColor: Int?,
    isFilled: Boolean,
    strokeWidth: Float,
    alpha: Float,
    laserColor: Int,
    laserSize: Float,
    palmRejection: Boolean,
    stylusOnly: Boolean,
    zoom: Float,
    panX: Float,
    panY: Float,
    selectedUids: Set<String>,
    lockedObjectUids: Set<String>,
    smartInkPreviewStrokes: List<List<StrokePoint>>,
    activeGeometryTool: GeometryToolType? = null
  ) {
    val viewportChanged = viewScale != zoom || viewPanX != panX || viewPanY != panY
    this.bgColor = bgColor
    this.bgStyle = bgStyle
    this.pdfBitmap = pdf
    this.layers = layers
    this.objects = objects
    this.activeTool = tool
    this.eraserMode = eraserMode
    this.brushColor = brushColor
    this.fillColor = fillColor
    this.isFilled = isFilled
    this.strokeWidth = strokeWidth
    this.alpha = alpha
    this.laserColor = laserColor
    this.laserSize = laserSize
    this.palmRejection = palmRejection
    this.stylusOnly = stylusOnly
    this.selectedUids = selectedUids
    this.lockedObjectUids = lockedObjectUids
    this.smartInkPreviewStrokes = smartInkPreviewStrokes
    if (!isTwoFingerGesturing) {
      this.viewScale = zoom
      this.viewPanX = panX
      this.viewPanY = panY
    }

    val vpKey = listOf((viewScale * 1000).toInt(), (viewPanX * 10).toInt(), (viewPanY * 10).toInt()).hashCode()
    val layerKey = layers.hashCode()
    val isDark = TeachColors.isDarkColor(bgColor)

    val canIncrement = !committedDirty &&
      committedBitmap != null &&
      !committedBitmap!!.isRecycled &&
      vpKey == paintedViewportKey &&
      layerKey == paintedLayersKey &&
      isDark == paintedBgDark &&
      selectedUids == paintedSelectedUids &&
      objects.size > paintedObjects.size &&
      isPrefixMatch(paintedObjects, objects)

    if (canIncrement) {
      // Fast-path incremental paint: draw ONLY the newly appended unselected stroke(s) in <0.2ms!
      val bmp = committedBitmap!!
      val c = Canvas(bmp)
      c.save()
      c.translate(viewPanX, viewPanY)
      c.scale(viewScale, viewScale)
      val visibleLayerIds = layers.filter { it.visible }.map { it.id }.toSet()
      for (i in paintedObjects.size until objects.size) {
        val obj = objects[i]
        if (obj.objectUid !in selectedUids && (visibleLayerIds.isEmpty() || obj.layerId in visibleLayerIds)) {
          ObjectPainter.drawObject(c, obj, isDark, ::resolveImage)
        }
      }
      c.restore()
      paintedObjects = objects
    } else if (vpKey != paintedViewportKey || layerKey != paintedLayersKey || isDark != paintedBgDark || selectedUids != paintedSelectedUids || objects != paintedObjects) {
      if (viewportChanged && isTwoFingerGesturing) {
        scheduleCacheRebuild()
      } else {
        committedDirty = true
      }
    }
    if (this.activeGeometryTool != activeGeometryTool) {
      this.activeGeometryTool = activeGeometryTool
      if (activeGeometryTool != null) {
        val inst = when (activeGeometryTool) {
          GeometryToolType.RULER -> GeoInstrument.Ruler
          GeometryToolType.PROTRACTOR -> GeoInstrument.Protractor
          GeometryToolType.COMPASS -> GeoInstrument.Compass
          GeometryToolType.SET_SQUARE_30_60,
          GeometryToolType.SET_SQUARE_45_45 -> GeoInstrument.SetSquare
        }
        val vw = if (width > 0) width.toFloat() else 1920f
        val vh = if (height > 0) height.toFloat() else 1080f
        geo.open(inst, vw, vh, viewScale, viewPanX, viewPanY)
      } else {
        geo.close()
      }
    }
    invalidate()
  }

  private fun isPrefixMatch(oldList: List<CanvasObject>, newList: List<CanvasObject>): Boolean {
    if (oldList.size > newList.size) return false
    for (i in oldList.indices) {
      if (oldList[i] !== newList[i] && oldList[i].objectUid != newList[i].objectUid) return false
      if (oldList[i].updatedAt != newList[i].updatedAt) return false
    }
    return true
  }

  private fun scheduleCacheRebuild() {
    removeCallbacks(rebuildCacheRunnable)
    postDelayed(rebuildCacheRunnable, 100L)
  }

  private fun resolveImage(path: String): Bitmap? {
    imageCache.get(path)?.let { if (!it.isRecycled) return it }
    val f = File(path)
    if (!f.exists()) return null
    val boundsOpts = BitmapFactory.Options().apply { inJustDecodeBounds = true }
    BitmapFactory.decodeFile(f.absolutePath, boundsOpts)
    val maxDim = maxOf(boundsOpts.outWidth, boundsOpts.outHeight)
    var sampleSize = 1
    while (maxDim / sampleSize > 2048) {
      sampleSize *= 2
    }
    val decodeOpts = BitmapFactory.Options().apply {
      inSampleSize = sampleSize
      inPreferredConfig = Bitmap.Config.ARGB_8888
    }
    val bmp = runCatching { BitmapFactory.decodeFile(f.absolutePath, decodeOpts) }.getOrNull() ?: return null
    imageCache.put(path, bmp)
    return bmp
  }

  override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
    super.onSizeChanged(w, h, oldw, oldh)
    committedBitmap?.recycle()
    committedBitmap = null
    committedDirty = true
    if (activeGeometryTool != null && !geo.active && w > 0 && h > 0) {
      val inst = when (activeGeometryTool!!) {
        GeometryToolType.RULER -> GeoInstrument.Ruler
        GeometryToolType.PROTRACTOR -> GeoInstrument.Protractor
        GeometryToolType.COMPASS -> GeoInstrument.Compass
        GeometryToolType.SET_SQUARE_30_60,
        GeometryToolType.SET_SQUARE_45_45 -> GeoInstrument.SetSquare
      }
      geo.open(inst, w.toFloat(), h.toFloat(), viewScale, viewPanX, viewPanY)
    }
  }

  override fun onDetachedFromWindow() {
    removeCallbacks(rebuildCacheRunnable)
    removeCallbacks(holdToMoveRunnable)
    committedBitmap?.recycle()
    committedBitmap = null
    super.onDetachedFromWindow()
  }

  private fun ensureCommittedBitmap() {
    if (width <= 0 || height <= 0) return
    val existing = committedBitmap
    val bmp = if (existing == null || existing.isRecycled || existing.width != width || existing.height != height) {
      existing?.recycle()
      committedDirty = true
      Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888).also { committedBitmap = it }
    } else existing

    if (!committedDirty) return
    committedDirty = false

    bmp.eraseColor(0x00000000)
    val c = Canvas(bmp)
    c.save()
    c.translate(viewPanX, viewPanY)
    c.scale(viewScale, viewScale)

    val visibleLayerIds = layers.filter { it.visible }.map { it.id }.toSet()
    val layerOrder = layers.associate { it.id to it.zOrder }
    val sorted = objects
      .filter { visibleLayerIds.isEmpty() || it.layerId in visibleLayerIds }
      .sortedWith(compareBy<CanvasObject> { layerOrder[it.layerId] ?: 0 }.thenBy { it.zIndex })

    val isDark = TeachColors.isDarkColor(bgColor)
    for (obj in sorted) {
      // Selected objects render live in onDraw so moving/resizing/stretching/rotating has ZERO double-ghost!
      if (obj.objectUid !in selectedUids) {
        ObjectPainter.drawObject(c, obj, isDark, ::resolveImage)
      }
    }
    c.restore()

    paintedObjects = objects
    paintedSelectedUids = selectedUids
    paintedViewportKey = listOf((viewScale * 1000).toInt(), (viewPanX * 10).toInt(), (viewPanY * 10).toInt()).hashCode()
    paintedLayersKey = layers.hashCode()
    paintedBgDark = isDark
  }

  override fun onDraw(canvas: Canvas) {
    super.onDraw(canvas)
    val w = width.toFloat(); val h = height.toFloat()
    if (w <= 0f || h <= 0f) return

    // 1. Background + Texture + PDF
    canvas.save()
    canvas.translate(viewPanX, viewPanY)
    canvas.scale(viewScale, viewScale)

    val invScale = 1f / viewScale.coerceAtLeast(0.05f)
    val worldVisible = RectF(-viewPanX * invScale, -viewPanY * invScale, (w - viewPanX) * invScale, (h - viewPanY) * invScale)
    val pdfDest = pdfBitmap?.takeIf { !it.isRecycled }?.let { bmp ->
      val s = min(w / bmp.width, h / bmp.height)
      val dw = bmp.width * s; val dh = bmp.height * s
      RectF((w - dw) / 2f, (h - dh) / 2f, (w + dw) / 2f, (h + dh) / 2f)
    }
    Background.draw(canvas, worldVisible, bgColor, bgStyle, pdfBitmap, pdfDest)
    canvas.restore()

    // 2. Cached Ink + Live Stroke inside ink-only layer (CLEAR eraser never touches background!)
    ensureCommittedBitmap()
    val liveIsPixelEraser = (activeTool == ToolType.Eraser || isTemporaryEraser) &&
      eraserMode == EraserMode.PIXEL_INK && livePoints.isNotEmpty()

    val saveLayerCount = if (liveIsPixelEraser) canvas.saveLayer(0f, 0f, w, h, null) else -1
    committedBitmap?.let { bmp ->
      if (!bmp.isRecycled) canvas.drawBitmap(bmp, 0f, 0f, null)
    }

    canvas.save()
    canvas.translate(viewPanX, viewPanY)
    canvas.scale(viewScale, viewScale)
    val isDark = TeachColors.isDarkColor(bgColor)

    // Smart Writing buffered strokes
    if (smartInkPreviewStrokes.isNotEmpty()) {
      for (s in smartInkPreviewStrokes) {
        ObjectPainter.drawStrokePoints(canvas, ToolType.Pen, s, brushColor, strokeWidth, 0.85f, isDark)
      }
    }

    // Live selected objects render here (zero double-ghost during drag/resize/stretch/rotate!)
    if (selectedUids.isNotEmpty()) {
      val visibleLayerIds = layers.filter { it.visible }.map { it.id }.toSet()
      canvas.save()
      if (draggingSelection) canvas.translate(liveDragDx, liveDragDy)
      if (scalingSelection) canvas.scale(liveScaleFactor, liveScaleFactor, transformAnchorX, transformAnchorY)
      if (scalingWidthOnly || scalingHeightOnly) canvas.scale(liveScaleX, liveScaleY, transformAnchorX, transformAnchorY)
      if (rotatingSelection) canvas.rotate(liveRotateDeg, transformAnchorX, transformAnchorY)
      for (obj in objects) {
        if (obj.objectUid in selectedUids && (visibleLayerIds.isEmpty() || obj.layerId in visibleLayerIds)) {
          ObjectPainter.drawObject(canvas, obj, isDark, ::resolveImage)
        }
      }
      canvas.restore()
    }

    // Live stroke / eraser / shape
    if (liveIsPixelEraser) {
      ObjectPainter.drawEraserStroke(canvas, livePoints, strokeWidth * 4f)
    } else if (livePoints.isNotEmpty() && (activeTool.isBrush || activeTool == ToolType.SmartWriting)) {
      val drawTool = if (activeTool == ToolType.SmartWriting) ToolType.Pen else activeTool
      ObjectPainter.drawStrokePoints(canvas, drawTool, livePoints, brushColor, strokeWidth, alpha, isDark)
    } else if (activeTool.isShape && liveStartPoint != null && liveEndPoint != null) {
      ShapeRenderer.drawShape(
        canvas = canvas,
        type = activeTool,
        left = liveStartPoint!!.x,
        top = liveStartPoint!!.y,
        right = liveEndPoint!!.x,
        bottom = liveEndPoint!!.y,
        color = brushColor,
        strokeWidth = strokeWidth,
        isFilled = isFilled,
        fillColor = fillColor,
        alpha = alpha
      )
    }

    canvas.restore()
    if (saveLayerCount >= 0) canvas.restoreToCount(saveLayerCount)

    // 3. UI Overlays: Lasso marquee, Selection Box + 4 Corner Handles + Rotate Handle, Eraser Cursor, Laser Trail
    canvas.save()
    canvas.translate(viewPanX, viewPanY)
    canvas.scale(viewScale, viewScale)

    if (lassoPathPoints.size >= 2) {
      val lp = Path().apply {
        moveTo(lassoPathPoints[0].x, lassoPathPoints[0].y)
        for (i in 1 until lassoPathPoints.size) lineTo(lassoPathPoints[i].x, lassoPathPoints[i].y)
        close()
      }
      canvas.drawPath(lp, selectionFillPaint)
      canvas.drawPath(lp, selectionStrokePaint)
    }

    if (selectedUids.isNotEmpty()) {
      val selBounds = computeSelectionBounds()
      if (selBounds != null) {
        val isLocked = selectedUids.all { it in lockedObjectUids }
        val selectedObjs = objects.filter { it.objectUid in selectedUids }
        val isTextOnly = selectedObjs.isNotEmpty() && selectedObjs.all { it.type == ToolType.Text || it.type == ToolType.Formula }
        val showStretchHandles = selectedObjs.isNotEmpty() && selectedObjs.any { it.type == ToolType.Table || it.type.isShape || it.type == ToolType.Image || it.type == ToolType.Sticker }

        val pad = 10f / viewScale
        val rawBox = RectF(selBounds.left - pad, selBounds.top - pad, selBounds.right + pad, selBounds.bottom + pad)
        val liveBounds = when {
          draggingSelection -> RectF(
            selBounds.left + liveDragDx,
            selBounds.top + liveDragDy,
            selBounds.right + liveDragDx,
            selBounds.bottom + liveDragDy
          )
          scalingSelection -> RectF(
            transformAnchorX + (selBounds.left - transformAnchorX) * liveScaleFactor,
            transformAnchorY + (selBounds.top - transformAnchorY) * liveScaleFactor,
            transformAnchorX + (selBounds.right - transformAnchorX) * liveScaleFactor,
            transformAnchorY + (selBounds.bottom - transformAnchorY) * liveScaleFactor
          )
          scalingWidthOnly || scalingHeightOnly -> RectF(
            transformAnchorX + (selBounds.left - transformAnchorX) * liveScaleX,
            transformAnchorY + (selBounds.top - transformAnchorY) * liveScaleY,
            transformAnchorX + (selBounds.right - transformAnchorX) * liveScaleX,
            transformAnchorY + (selBounds.bottom - transformAnchorY) * liveScaleY
          )
          else -> selBounds
        }

        canvas.save()
        if (rotatingSelection) canvas.rotate(liveRotateDeg, transformAnchorX, transformAnchorY)

        val box = RectF(liveBounds.left - pad, liveBounds.top - pad, liveBounds.right + pad, liveBounds.bottom + pad)
        selectionStrokePaint.strokeWidth = 2.6f / viewScale
        selectionStrokePaint.color = if (isLocked) 0xFF64748B.toInt() else 0xFF94A3B8.toInt()
        canvas.drawRect(box, selectionFillPaint)
        canvas.drawRect(box, selectionStrokePaint)

        if (!isLocked) {
          val inv = 1f / viewScale
          val btnR = 23f * inv
          val anchorR = 11f * inv
          val greenR = 18f * inv

          // Top-left: crisp white anchor node with blue border
          handleBorderPaint.color = 0xFF2B6CF6.toInt()
          handleBorderPaint.strokeWidth = 3.5f * inv
          canvas.drawCircle(box.left, box.top, anchorR, handleWhitePaint)
          canvas.drawCircle(box.left, box.top, anchorR, handleBorderPaint)

          // Top-right: elevated blue Flip Horizontal (⇄) handle for shapes/tables/images/stickers
          if (!isTextOnly) {
            drawBlueHandleButton(canvas, box.right, box.top, btnR, inv) { cx, cy, r ->
              handleIconPaint.strokeWidth = 2.8f * inv
              // Top arrow right
              canvas.drawLine(cx - r * 0.42f, cy - r * 0.18f, cx + r * 0.42f, cy - r * 0.18f, handleIconPaint)
              canvas.drawLine(cx + r * 0.18f, cy - r * 0.38f, cx + r * 0.42f, cy - r * 0.18f, handleIconPaint)
              canvas.drawLine(cx + r * 0.18f, cy + r * 0.02f, cx + r * 0.42f, cy - r * 0.18f, handleIconPaint)
              // Bottom arrow left
              canvas.drawLine(cx + r * 0.42f, cy + r * 0.20f, cx - r * 0.42f, cy + r * 0.20f, handleIconPaint)
              canvas.drawLine(cx - r * 0.18f, cy, cx - r * 0.42f, cy + r * 0.20f, handleIconPaint)
              canvas.drawLine(cx - r * 0.18f, cy + r * 0.40f, cx - r * 0.42f, cy + r * 0.20f, handleIconPaint)
            }
          }

          // Bottom-left: elevated blue Rotate (↻) handle
          drawBlueHandleButton(canvas, box.left, box.bottom, btnR, inv) { cx, cy, r ->
            handleIconPaint.strokeWidth = 2.9f * inv
            val arcR = r * 0.40f
            canvas.drawArc(cx - arcR, cy - arcR, cx + arcR, cy + arcR, -45f, 275f, false, handleIconPaint)
            // Arrow head at top-right of arc
            val ax = cx + arcR * 0.72f
            val ay = cy - arcR * 0.72f
            canvas.drawLine(ax, ay, ax - r * 0.26f, ay - r * 0.04f, handleIconPaint)
            canvas.drawLine(ax, ay, ax + r * 0.04f, ay - r * 0.26f, handleIconPaint)
          }

          // Bottom-right: elevated blue Diagonal Resize (⤢) handle
          drawBlueHandleButton(canvas, box.right, box.bottom, btnR, inv) { cx, cy, r ->
            handleIconPaint.strokeWidth = 2.9f * inv
            val d = r * 0.38f
            canvas.drawLine(cx - d, cy + d, cx + d, cy - d, handleIconPaint)
            // Top-right arrowhead
            canvas.drawLine(cx + d, cy - d, cx + d - r * 0.28f, cy - d, handleIconPaint)
            canvas.drawLine(cx + d, cy - d, cx + d, cy - d + r * 0.28f, handleIconPaint)
            // Bottom-left arrowhead
            canvas.drawLine(cx - d, cy + d, cx - d + r * 0.28f, cy + d, handleIconPaint)
            canvas.drawLine(cx - d, cy + d, cx - d, cy + d - r * 0.28f, handleIconPaint)
          }

          // Green independent stretch handles (Right ↔ and Bottom ↕) for Tables & Shapes
          if (showStretchHandles && box.width() > 60f * inv && box.height() > 60f * inv) {
            drawGreenEdgeHandle(canvas, box.right, box.centerY(), greenR, inv, horizontal = true)
            drawGreenEdgeHandle(canvas, box.centerX(), box.bottom, greenR, inv, horizontal = false)
          }
        }
        canvas.restore()

        // Compute transformed screen-space bounds so floating Copy/Lock/Edit/Delete buttons track right above object!
        val screenBox = computeTransformedScreenBounds(rawBox)
        reportSelectionBounds(screenBox)
      } else {
        reportSelectionBounds(null)
      }
    } else {
      reportSelectionBounds(null)
    }

    if ((activeTool == ToolType.Eraser || isTemporaryEraser) && cursorWorldX != null && cursorWorldY != null) {
      val r = (if (eraserMode == EraserMode.PIXEL_INK) strokeWidth * 2f else strokeWidth * 1.4f).coerceAtLeast(10f)
      canvas.drawCircle(cursorWorldX!!, cursorWorldY!!, r, eraserCursorFill)
      canvas.drawCircle(cursorWorldX!!, cursorWorldY!!, r, eraserCursorStroke)
    }

    drawLaserTrail(canvas)
    if (geo.active) {
      geo.draw(canvas)
    }
    canvas.restore()
  }

  private fun drawLaserTrail(canvas: Canvas) {
    if (laserTrail.isEmpty()) return
    val now = SystemClock.uptimeMillis()
    val lifetime = 1000L
    laserTrail.removeAll { now - it.timeMs > lifetime }
    if (laserTrail.isEmpty()) return

    val glowRadius = (laserSize * 0.7f).coerceIn(4f, 24f)
    laserGlowPaint.color = laserColor
    laserGlowPaint.maskFilter = BlurMaskFilter(glowRadius, BlurMaskFilter.Blur.NORMAL)
    for (i in 1 until laserTrail.size) {
      val a = laserTrail[i - 1]; val b = laserTrail[i]
      val fade = (1f - (now - b.timeMs).toFloat() / lifetime.toFloat()).coerceIn(0f, 1f)
      laserGlowPaint.strokeWidth = laserSize * (0.4f + 0.6f * fade)
      laserGlowPaint.alpha = (fade * 220f).toInt()
      canvas.drawLine(a.x, a.y, b.x, b.y, laserGlowPaint)
    }
    laserGlowPaint.maskFilter = null

    val head = laserTrail.last()
    val headFade = (1f - (now - head.timeMs).toFloat() / lifetime.toFloat()).coerceIn(0f, 1f)
    laserCorePaint.color = 0xFFFFFFFF.toInt()
    laserCorePaint.alpha = (headFade * 255f).toInt()
    canvas.drawCircle(head.x, head.y, (laserSize * 0.45f).coerceAtLeast(4f), laserCorePaint)
    postInvalidateOnAnimation()
  }

  private fun drawBlueHandleButton(
    canvas: Canvas,
    cx: Float,
    cy: Float,
    radius: Float,
    invScale: Float,
    drawGlyph: (Float, Float, Float) -> Unit
  ) {
    canvas.drawCircle(cx, cy + 3f * invScale, radius + 1.5f * invScale, handleShadowPaint)
    handlePaint.color = 0xFF2B6CF6.toInt()
    canvas.drawCircle(cx, cy, radius, handlePaint)
    handleBorderPaint.color = 0xFFFFFFFF.toInt()
    handleBorderPaint.strokeWidth = 3.6f * invScale
    canvas.drawCircle(cx, cy, radius, handleBorderPaint)
    drawGlyph(cx, cy, radius)
  }

  private fun drawGreenEdgeHandle(
    canvas: Canvas,
    cx: Float,
    cy: Float,
    radius: Float,
    invScale: Float,
    horizontal: Boolean
  ) {
    canvas.drawCircle(cx, cy + 2f * invScale, radius + 1f * invScale, handleShadowPaint)
    canvas.drawCircle(cx, cy, radius, handleWhitePaint)
    greenBorderPaint.strokeWidth = 3f * invScale
    canvas.drawCircle(cx, cy, radius, greenBorderPaint)
    greenIconPaint.strokeWidth = 2.6f * invScale
    val d = radius * 0.42f
    val tip = radius * 0.24f
    if (horizontal) {
      canvas.drawLine(cx - d, cy, cx + d, cy, greenIconPaint)
      canvas.drawLine(cx - d, cy, cx - d + tip, cy - tip, greenIconPaint)
      canvas.drawLine(cx - d, cy, cx - d + tip, cy + tip, greenIconPaint)
      canvas.drawLine(cx + d, cy, cx + d - tip, cy - tip, greenIconPaint)
      canvas.drawLine(cx + d, cy, cx + d - tip, cy + tip, greenIconPaint)
    } else {
      canvas.drawLine(cx, cy - d, cx, cy + d, greenIconPaint)
      canvas.drawLine(cx, cy - d, cx - tip, cy - d + tip, greenIconPaint)
      canvas.drawLine(cx, cy - d, cx + tip, cy - d + tip, greenIconPaint)
      canvas.drawLine(cx, cy + d, cx - tip, cy + d - tip, greenIconPaint)
      canvas.drawLine(cx, cy + d, cx + tip, cy + d - tip, greenIconPaint)
    }
  }

  private fun computeTransformedScreenBounds(worldBox: RectF): RectF {
    val m = Matrix()
    if (draggingSelection) m.postTranslate(liveDragDx, liveDragDy)
    if (scalingSelection) m.postScale(liveScaleFactor, liveScaleFactor, transformAnchorX, transformAnchorY)
    if (scalingWidthOnly || scalingHeightOnly) m.postScale(liveScaleX, liveScaleY, transformAnchorX, transformAnchorY)
    if (rotatingSelection) m.postRotate(liveRotateDeg, transformAnchorX, transformAnchorY)
    m.postScale(viewScale, viewScale)
    m.postTranslate(viewPanX, viewPanY)
    val out = RectF(worldBox)
    m.mapRect(out)
    return out
  }

  private fun reportSelectionBounds(bounds: RectF?) {
    val prev = lastReportedBounds
    val changed = when {
      prev == null && bounds == null -> false
      prev == null || bounds == null -> true
      else -> kotlin.math.abs(prev.left - bounds.left) > 1.5f ||
        kotlin.math.abs(prev.top - bounds.top) > 1.5f ||
        kotlin.math.abs(prev.right - bounds.right) > 1.5f ||
        kotlin.math.abs(prev.bottom - bounds.bottom) > 1.5f
    }
    if (changed) {
      lastReportedBounds = bounds?.let { RectF(it) }
      post { callbacks?.onSelectionScreenBoundsChanged(lastReportedBounds?.let { RectF(it) }) }
    }
  }

  private fun computeSelectionBounds(): RectF? {
    var union: RectF? = null
    for (obj in objects) {
      if (obj.objectUid in selectedUids) {
        val b = ObjectPainter.getBounds(obj)
        if (union == null) union = RectF(b) else union.union(b)
      }
    }
    return union
  }

  private fun screenToWorld(sx: Float, sy: Float): StrokePoint {
    viewMatrix.reset()
    viewMatrix.setScale(viewScale, viewScale)
    viewMatrix.postTranslate(viewPanX, viewPanY)
    viewMatrix.invert(invMatrix)
    val pts = floatArrayOf(sx, sy)
    invMatrix.mapPoints(pts)
    return StrokePoint(pts[0], pts[1], 0.5f, SystemClock.uptimeMillis())
  }

  private fun shouldRejectTouch(event: MotionEvent, pointerIndex: Int): Boolean {
    val toolType = event.getToolType(pointerIndex)
    if (toolType == MotionEvent.TOOL_TYPE_STYLUS || toolType == MotionEvent.TOOL_TYPE_ERASER) {
      lastStylusSeenMs = SystemClock.uptimeMillis()
      return false
    }
    if (stylusOnly && toolType == MotionEvent.TOOL_TYPE_FINGER) return true
    if (palmRejection && toolType == MotionEvent.TOOL_TYPE_FINGER) {
      // Reject accidental palm touches if a stylus was active within the last 2s, or if touch area is huge (>240px)
      if (lastStylusSeenMs > 0L && (SystemClock.uptimeMillis() - lastStylusSeenMs) < 2000L) return true
      val major = event.getTouchMajor(pointerIndex)
      if (major > 240f) return true
    }
    return false
  }

  /**
   * Checks if touch lands on an active selection handle/body (works in ANY tool mode!).
   */
  private fun tryStartSelectionManipulation(world: StrokePoint): Boolean {
    if (selectedUids.isEmpty()) return false
    val selBounds = computeSelectionBounds() ?: return false
    val pad = 10f / viewScale
    val box = RectF(selBounds.left - pad, selBounds.top - pad, selBounds.right + pad, selBounds.bottom + pad)
    val tol = 38f / viewScale
    val isLocked = selectedUids.all { it in lockedObjectUids }
    val selectedObjs = objects.filter { it.objectUid in selectedUids }
    val isTextOnly = selectedObjs.isNotEmpty() && selectedObjs.all { it.type == ToolType.Text || it.type == ToolType.Formula }
    val showStretchHandles = selectedObjs.isNotEmpty() && selectedObjs.any { it.type == ToolType.Table || it.type.isShape || it.type == ToolType.Image || it.type == ToolType.Sticker }

    if (!isLocked) {
      // 1. Top-right Flip Horizontal (⇄) 1-tap handle
      if (!isTextOnly && hypot(world.x - box.right, world.y - box.top) <= tol) {
        performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
        callbacks?.onFlipSelectionHorizontal()
        return true
      }

      // 2. Bottom-left Rotate (↻) drag handle
      if (hypot(world.x - box.left, world.y - box.bottom) <= tol) {
        rotatingSelection = true
        transformAnchorX = selBounds.centerX()
        transformAnchorY = selBounds.centerY()
        transformStartAngle = Math.toDegrees(atan2((world.y - transformAnchorY).toDouble(), (world.x - transformAnchorX).toDouble())).toFloat()
        liveRotateDeg = 0f
        return true
      }

      // 3. Bottom-right Diagonal Resize (⤢) pins Top-Left anchor; Top-Left (○) pins Bottom-Right anchor!
      val hitBottomRight = hypot(world.x - box.right, world.y - box.bottom) <= tol
      val hitTopLeft = !hitBottomRight && hypot(world.x - box.left, world.y - box.top) <= tol
      if (hitBottomRight || hitTopLeft) {
        scalingSelection = true
        if (hitBottomRight) {
          transformAnchorX = selBounds.left
          transformAnchorY = selBounds.top
        } else {
          transformAnchorX = selBounds.right
          transformAnchorY = selBounds.bottom
        }
        val dx0 = world.x - transformAnchorX
        val dy0 = world.y - transformAnchorY
        transformStartDist = hypot(dx0, dy0).coerceAtLeast(16f)
        transformDirX = dx0 / transformStartDist
        transformDirY = dy0 / transformStartDist
        liveScaleFactor = 1f
        return true
      }

      // 4. Green right-center Width Stretch (↔) handle
      if (showStretchHandles && hypot(world.x - box.right, world.y - box.centerY()) <= tol) {
        scalingWidthOnly = true
        transformAnchorX = selBounds.left
        transformAnchorY = selBounds.centerY()
        transformStartDist = (world.x - transformAnchorX).coerceAtLeast(24f)
        liveScaleX = 1f
        liveScaleY = 1f
        return true
      }

      // 5. Green bottom-center Height Stretch (↕) handle
      if (showStretchHandles && hypot(world.x - box.centerX(), world.y - box.bottom) <= tol) {
        scalingHeightOnly = true
        transformAnchorX = selBounds.centerX()
        transformAnchorY = selBounds.top
        transformStartDist = (world.y - transformAnchorY).coerceAtLeast(24f)
        liveScaleX = 1f
        liveScaleY = 1f
        return true
      }
    }

    // Check inside selected bounding box (double-tap edits, drag moves!)
    if (box.contains(world.x, world.y)) {
      val now = SystemClock.uptimeMillis()
      val isDoubleTap = (now - lastTapTime < 350L) && hypot(world.x - lastTapX, world.y - lastTapY) < 44f / viewScale
      lastTapTime = now; lastTapX = world.x; lastTapY = world.y
      if (isDoubleTap) {
        val hit = objects.firstOrNull { it.objectUid in selectedUids }
        if (hit != null && (hit.type == ToolType.Text || hit.type == ToolType.Formula || hit.type == ToolType.Table)) {
          callbacks?.onDoubleTapObject(hit)
          return true
        }
      }
      if (!isLocked) {
        draggingSelection = true
        dragLastWorldX = world.x
        dragLastWorldY = world.y
        liveDragDx = 0f
        liveDragDy = 0f
      }
      return true
    }

    return false
  }

  @SuppressLint("ClickableViewAccessibility")
  override fun onTouchEvent(event: MotionEvent): Boolean {
    val action = event.actionMasked

    if (geo.active) {
      val bx = (event.x - viewPanX) / viewScale
      val by = (event.y - viewPanY) / viewScale
      val used = when (action) {
        MotionEvent.ACTION_DOWN -> geo.onDown(bx, by, viewScale)
        MotionEvent.ACTION_MOVE -> geo.onMove(bx, by, viewScale)
        MotionEvent.ACTION_UP,
        MotionEvent.ACTION_CANCEL -> geo.onUp(bx, by, viewScale)
        else -> false
      }
      if (used) {
        invalidate()
        return true
      }
      if (event.pointerCount >= 2) {
        // Fall through to 2-finger pan/zoom below
      } else {
        return true
      }
    }

    // Two-finger pan & pinch zoom
    if (event.pointerCount >= 2) {
      removeCallbacks(holdToMoveRunnable)
      when (action) {
        MotionEvent.ACTION_POINTER_DOWN -> {
          cancelLiveStroke()
          isTwoFingerGesturing = true
          prevPinchDist = spacing(event)
          prevMidX = (event.getX(0) + event.getX(1)) / 2f
          prevMidY = (event.getY(0) + event.getY(1)) / 2f
        }
        MotionEvent.ACTION_MOVE -> {
          if (isTwoFingerGesturing) {
            val dist = spacing(event)
            val midX = (event.getX(0) + event.getX(1)) / 2f
            val midY = (event.getY(0) + event.getY(1)) / 2f
            if (prevPinchDist > 12f && dist > 12f) {
              val factor = dist / prevPinchDist
              val newScale = (viewScale * factor).coerceIn(AppConfig.MIN_ZOOM, AppConfig.MAX_ZOOM)
              val ratio = newScale / viewScale
              viewPanX = midX - (midX - viewPanX) * ratio + (midX - prevMidX)
              viewPanY = midY - (midY - viewPanY) * ratio + (midY - prevMidY)
              viewScale = newScale
            } else {
              viewPanX += (midX - prevMidX)
              viewPanY += (midY - prevMidY)
            }
            prevPinchDist = dist; prevMidX = midX; prevMidY = midY
            callbacks?.onViewportChanged(viewScale, viewPanX, viewPanY)
            scheduleCacheRebuild()
            invalidate()
          }
        }
        MotionEvent.ACTION_POINTER_UP -> {
          if (event.pointerCount <= 2) {
            isTwoFingerGesturing = false
            committedDirty = true
            invalidate()
          }
        }
      }
      return true
    }

    if (isTwoFingerGesturing) {
      if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
        isTwoFingerGesturing = false
        committedDirty = true
        invalidate()
      }
      return true
    }

    when (action) {
      MotionEvent.ACTION_DOWN -> {
        if (activeTool != ToolType.Hand && shouldRejectTouch(event, 0)) return false
        callbacks?.onCanvasTouched()
        activePointerId = event.getPointerId(0)
        val sx = event.x; val sy = event.y
        downScreenX = sx; downScreenY = sy
        holdMovedTooFar = false
        holdCandidateUid = null
        removeCallbacks(holdToMoveRunnable)

        val world = screenToWorld(sx, sy).copy(pressure = event.pressure.coerceIn(0.1f, 1f))
        val toolType = event.getToolType(0)
        isTemporaryEraser = toolType == MotionEvent.TOOL_TYPE_ERASER ||
          (toolType == MotionEvent.TOOL_TYPE_STYLUS && (event.buttonState and MotionEvent.BUTTON_STYLUS_PRIMARY) != 0)

        // Direct manipulation of active selection works in ANY tool mode!
        if (activeTool != ToolType.Hand && activeTool != ToolType.Laser && !isTemporaryEraser) {
          if (tryStartSelectionManipulation(world)) {
            invalidate()
            return true
          }
          // Tapped outside selected box -> clear selection immediately and proceed!
          if (selectedUids.isNotEmpty() && activeTool != ToolType.Lasso) {
            callbacks?.onSelectionChanged(emptySet())
            selectedUids = emptySet()
          }
        }

        // Schedule "Hold to move" (320ms) on ANY placed non-brush object in any drawing/editing tool mode!
        if (activeTool != ToolType.Hand && activeTool != ToolType.Laser && activeTool != ToolType.Lasso && !isTemporaryEraser) {
          val hit = objects.asReversed().firstOrNull { obj ->
            !obj.type.isBrush && !obj.isEraser && ObjectPainter.hitTest(obj, world.x, world.y, 22f / viewScale)
          }
          if (hit != null) {
            holdCandidateUid = hit.objectUid
            dragLastWorldX = world.x
            dragLastWorldY = world.y
            postDelayed(holdToMoveRunnable, 320L)
          }
        }

        when {
          activeTool == ToolType.Hand -> { handLastX = sx; handLastY = sy }
          activeTool == ToolType.Laser -> {
            laserTrail.add(LaserDot(world.x, world.y, SystemClock.uptimeMillis()))
            postInvalidateOnAnimation()
          }
          activeTool == ToolType.Text || activeTool == ToolType.Formula || activeTool == ToolType.Table -> {
            callbacks?.onTapPlaceObject(activeTool, world.x, world.y)
          }
          activeTool == ToolType.Lasso -> handleLassoDown(world)
          activeTool == ToolType.Eraser || isTemporaryEraser -> {
            cursorWorldX = world.x; cursorWorldY = world.y
            if (eraserMode == EraserMode.STROKE) {
              callbacks?.onStrokeEraseAt(world.x, world.y, strokeWidth * 1.4f)
            } else {
              livePoints.clear(); livePoints.add(world)
            }
            invalidate()
          }
          activeTool.isShape -> {
            liveStartPoint = world; liveEndPoint = world; invalidate()
          }
          activeTool.isBrush || activeTool == ToolType.SmartWriting -> {
            livePoints.clear(); livePoints.add(world); invalidate()
          }
        }
        return true
      }

      MotionEvent.ACTION_MOVE -> {
        val idx = event.findPointerIndex(activePointerId)
        if (idx < 0) return true
        val sx = event.getX(idx); val sy = event.getY(idx)
        if (hypot(sx - downScreenX, sy - downScreenY) > 16f) {
          holdMovedTooFar = true
          removeCallbacks(holdToMoveRunnable)
        }
        val world = screenToWorld(sx, sy).copy(pressure = event.getPressure(idx).coerceIn(0.1f, 1f))

        // Live smooth selection transform
        if (draggingSelection) {
          liveDragDx += world.x - dragLastWorldX
          liveDragDy += world.y - dragLastWorldY
          dragLastWorldX = world.x; dragLastWorldY = world.y
          invalidate()
          return true
        }
        if (scalingSelection) {
          val projected = (world.x - transformAnchorX) * transformDirX + (world.y - transformAnchorY) * transformDirY
          liveScaleFactor = (projected / transformStartDist).coerceIn(0.25f, 4.0f)
          invalidate()
          return true
        }
        if (scalingWidthOnly) {
          val curDist = (world.x - transformAnchorX).coerceAtLeast(20f)
          liveScaleX = (curDist / transformStartDist).coerceIn(0.25f, 4.0f)
          liveScaleY = 1f
          invalidate()
          return true
        }
        if (scalingHeightOnly) {
          val curDist = (world.y - transformAnchorY).coerceAtLeast(20f)
          liveScaleX = 1f
          liveScaleY = (curDist / transformStartDist).coerceIn(0.25f, 4.0f)
          invalidate()
          return true
        }
        if (rotatingSelection) {
          val curAng = Math.toDegrees(atan2((world.y - transformAnchorY).toDouble(), (world.x - transformAnchorX).toDouble())).toFloat()
          val raw = curAng - transformStartAngle
          // Subtle magnetic snap every 45° within 4°
          val nearest45 = kotlin.math.round(raw / 45f) * 45f
          liveRotateDeg = if (kotlin.math.abs(raw - nearest45) < 4f) nearest45 else raw
          invalidate()
          return true
        }

        when {
          activeTool == ToolType.Hand -> {
            viewPanX += sx - handLastX; viewPanY += sy - handLastY
            handLastX = sx; handLastY = sy
            callbacks?.onViewportChanged(viewScale, viewPanX, viewPanY)
            scheduleCacheRebuild()
            invalidate()
          }
          activeTool == ToolType.Laser -> {
            laserTrail.add(LaserDot(world.x, world.y, SystemClock.uptimeMillis()))
            postInvalidateOnAnimation()
          }
          activeTool == ToolType.Lasso -> {
            lassoPathPoints.add(world); invalidate()
          }
          activeTool == ToolType.Eraser || isTemporaryEraser -> {
            cursorWorldX = world.x; cursorWorldY = world.y
            if (eraserMode == EraserMode.STROKE) callbacks?.onStrokeEraseAt(world.x, world.y, strokeWidth * 1.4f)
            else appendWithHistory(event, idx, livePoints)
            invalidate()
          }
          activeTool.isShape -> {
            liveEndPoint = world; invalidate()
          }
          activeTool.isBrush || activeTool == ToolType.SmartWriting -> {
            appendWithHistory(event, idx, livePoints); invalidate()
          }
        }
        return true
      }

      MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
        removeCallbacks(holdToMoveRunnable)
        val cancelled = action == MotionEvent.ACTION_CANCEL
        cursorWorldX = null; cursorWorldY = null

        if (!cancelled) {
          when {
            draggingSelection -> {
              if (hypot(liveDragDx, liveDragDy) > 2f) callbacks?.onMoveSelection(liveDragDx, liveDragDy)
              draggingSelection = false; liveDragDx = 0f; liveDragDy = 0f
            }
            scalingSelection -> {
              if (kotlin.math.abs(liveScaleFactor - 1f) > 0.02f) callbacks?.onScaleSelection(transformAnchorX, transformAnchorY, liveScaleFactor)
              scalingSelection = false; liveScaleFactor = 1f
            }
            scalingWidthOnly -> {
              if (kotlin.math.abs(liveScaleX - 1f) > 0.02f) callbacks?.onScaleSelectionAxes(transformAnchorX, transformAnchorY, liveScaleX, 1f)
              scalingWidthOnly = false; liveScaleX = 1f; liveScaleY = 1f
            }
            scalingHeightOnly -> {
              if (kotlin.math.abs(liveScaleY - 1f) > 0.02f) callbacks?.onScaleSelectionAxes(transformAnchorX, transformAnchorY, 1f, liveScaleY)
              scalingHeightOnly = false; liveScaleX = 1f; liveScaleY = 1f
            }
            rotatingSelection -> {
              if (kotlin.math.abs(liveRotateDeg) > 1.5f) callbacks?.onRotateSelection(transformAnchorX, transformAnchorY, liveRotateDeg)
              rotatingSelection = false; liveRotateDeg = 0f
            }
            activeTool == ToolType.Hand -> committedDirty = true
            activeTool == ToolType.Lasso -> handleLassoUp()
            activeTool == ToolType.Eraser || isTemporaryEraser -> {
              if (eraserMode == EraserMode.PIXEL_INK && livePoints.isNotEmpty()) {
                val simplified = ObjectPainter.simplifyPoints(livePoints.toList(), 1.2f)
                callbacks?.onCommitStroke(ToolType.Eraser, simplified, isEraser = true)
              }
            }
            activeTool.isShape -> {
              val s = liveStartPoint; val e = liveEndPoint
              if (s != null && e != null && hypot(e.x - s.x, e.y - s.y) > 8f) {
                callbacks?.onCommitShape(activeTool, s, e)
              }
            }
            activeTool == ToolType.SmartWriting -> {
              if (livePoints.isNotEmpty()) callbacks?.onSmartInkStroke(livePoints.toList())
            }
            activeTool.isBrush -> {
              if (livePoints.isNotEmpty()) {
                val simplified = ObjectPainter.simplifyPoints(livePoints.toList(), 1.1f)
                callbacks?.onCommitStroke(activeTool, simplified, isEraser = false)
              }
            }
          }
        } else {
          draggingSelection = false; scalingSelection = false; scalingWidthOnly = false; scalingHeightOnly = false; rotatingSelection = false
          liveDragDx = 0f; liveDragDy = 0f; liveScaleFactor = 1f; liveScaleX = 1f; liveScaleY = 1f; liveRotateDeg = 0f
        }
        cancelLiveStroke()
        return true
      }
    }
    return super.onTouchEvent(event)
  }

  private fun appendWithHistory(event: MotionEvent, pointerIdx: Int, out: MutableList<StrokePoint>) {
    for (h in 0 until event.historySize) {
      val wp = screenToWorld(event.getHistoricalX(pointerIdx, h), event.getHistoricalY(pointerIdx, h))
        .copy(pressure = event.getHistoricalPressure(pointerIdx, h).coerceIn(0.1f, 1f), t = event.getHistoricalEventTime(h))
      if (out.isEmpty() || hypot(wp.x - out.last().x, wp.y - out.last().y) >= 1.2f) out.add(wp)
    }
    val wp = screenToWorld(event.getX(pointerIdx), event.getY(pointerIdx))
      .copy(pressure = event.getPressure(pointerIdx).coerceIn(0.1f, 1f), t = event.eventTime)
    if (out.isEmpty() || hypot(wp.x - out.last().x, wp.y - out.last().y) >= 1.2f) out.add(wp)
  }

  private fun handleLassoDown(world: StrokePoint) {
    val hit = objects.asReversed().firstOrNull { ObjectPainter.hitTest(it, world.x, world.y, 24f / viewScale) }
    if (hit != null) {
      callbacks?.onSelectionChanged(setOf(hit.objectUid))
      selectedUids = setOf(hit.objectUid)
      if (hit.objectUid !in lockedObjectUids) {
        draggingSelection = true
        dragLastWorldX = world.x; dragLastWorldY = world.y
        liveDragDx = 0f; liveDragDy = 0f
      }
    } else {
      callbacks?.onSelectionChanged(emptySet())
      lassoPathPoints.clear(); lassoPathPoints.add(world)
    }
    invalidate()
  }

  private fun handleLassoUp() {
    if (lassoPathPoints.size >= 3) {
      var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
      var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
      for (p in lassoPathPoints) {
        minX = min(minX, p.x); minY = min(minY, p.y)
        maxX = max(maxX, p.x); maxY = max(maxY, p.y)
      }
      val marquee = RectF(minX, minY, maxX, maxY)
      val selected = objects.filter { obj ->
        !obj.isEraser && obj.type != ToolType.Eraser && RectF.intersects(marquee, ObjectPainter.getBounds(obj))
      }.map { it.objectUid }.toSet()
      callbacks?.onSelectionChanged(selected)
    }
    lassoPathPoints.clear()
    invalidate()
  }

  private fun cancelLiveStroke() {
    livePoints.clear()
    liveStartPoint = null
    liveEndPoint = null
    activePointerId = MotionEvent.INVALID_POINTER_ID
    isTemporaryEraser = false
    invalidate()
  }

  private fun spacing(event: MotionEvent): Float =
    hypot(event.getX(0) - event.getX(1), event.getY(0) - event.getY(1))
}
