package com.teachboard.app.ui.canvas

import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teachboard.app.TeachBoardApp
import com.teachboard.app.data.UserPrefs
import com.teachboard.app.data.local.toEntity
import com.teachboard.app.domain.export.Exporters
import com.teachboard.app.domain.export.LazyPdfCache
import com.teachboard.app.domain.formula.FormulaParser
import com.teachboard.app.domain.ink.InkModelState
import com.teachboard.app.domain.model.BackgroundStyle
import com.teachboard.app.domain.model.BoardLayer
import com.teachboard.app.domain.model.BoardSession
import com.teachboard.app.domain.model.CanvasObject
import com.teachboard.app.domain.model.Contour
import com.teachboard.app.domain.model.EraserMode
import com.teachboard.app.domain.model.FaceCamShape
import com.teachboard.app.domain.model.GeometryToolType
import com.teachboard.app.domain.model.OcrScanRecord
import com.teachboard.app.domain.model.Serialization
import com.teachboard.app.domain.model.StrokePoint
import com.teachboard.app.domain.model.ToolType
import com.teachboard.app.domain.model.ToolbarAction
import com.teachboard.app.domain.model.UndoCommand
import com.teachboard.app.domain.ocr.OcrOutput
import com.teachboard.app.domain.ocr.OcrScript
import com.teachboard.app.domain.search.WikimediaImage
import com.teachboard.app.domain.search.WikimediaSearch
import com.teachboard.app.domain.smartshapes.ShapeClassifier
import com.teachboard.app.domain.smartshapes.SmartShapeCandidate
import com.teachboard.app.ui.canvas.geometry.GeoStampKind
import com.teachboard.app.ui.canvas.geometry.GeoStampPath
import com.teachboard.app.ui.theme.AppConfig
import com.teachboard.app.ui.theme.TeachColors
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.sin
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.util.UUID
import kotlin.math.max
import kotlin.math.min

data class SmartShapeOffer(
  val sourceObjectUid: String,
  val originalStroke: CanvasObject,
  val candidates: List<SmartShapeCandidate>
)

data class CanvasUiState(
  val session: BoardSession? = null,
  val pageIndex: Int = 0,
  val layers: List<BoardLayer> = emptyList(),
  val activeLayerId: String = "",
  val objects: List<CanvasObject> = emptyList(),
  val pdfBitmap: Bitmap? = null,
  val pdfLoading: Boolean = false,
  val activeTool: ToolType = ToolType.Pen,
  val lastBrushTool: ToolType = ToolType.Pen,
  val lastShapeTool: ToolType = ToolType.Rect,
  val eraserMode: EraserMode = EraserMode.PIXEL_INK,
  val brushColor: Int = TeachColors.BrushBlack,
  val fillColor: Int? = null,
  val isFilled: Boolean = false,
  val strokeWidth: Float = 12f,
  val alpha: Float = 1f,
  val fontSize: Float = 44f,
  val laserColor: Int = TeachColors.LaserRed,
  val laserSize: Float = 16f,
  val smartShapesEnabled: Boolean = false,
  val smartShapeOffer: SmartShapeOffer? = null,
  val smartInkStrokes: List<List<StrokePoint>> = emptyList(),
  val smartRecognizing: Boolean = false,
  val zoom: Float = 1f,
  val panX: Float = 0f,
  val panY: Float = 0f,
  val selectedUids: Set<String> = emptySet(),
  val lockedObjectUids: Set<String> = emptySet(),
  val canUndo: Boolean = false,
  val canRedo: Boolean = false,
  val activeGeometryTool: GeometryToolType? = null,
  val showCalculator: Boolean = false,
  val showTimer: Boolean = false,
  val floatingVideoUri: Uri? = null,
  val showFaceCam: Boolean = false,
  val faceCamShape: FaceCamShape = FaceCamShape.ROUND,
  val showSavedToast: Boolean = false,
  val snackbarMessage: String? = null
)

class CanvasViewModel(
  private val app: TeachBoardApp,
  private val sessionId: String
) : ViewModel() {

  private val db = app.database
  private val prefsRepo = app.prefs
  val digitalInk = app.digitalInkManager
  val ocrManager = app.ocrManager

  private val _ui = MutableStateFlow(CanvasUiState())
  val ui: StateFlow<CanvasUiState> = _ui.asStateFlow()

  val loadedUserPrefs: StateFlow<UserPrefs?> = prefsRepo.prefs.stateIn(
    viewModelScope,
    SharingStarted.Eagerly,
    null
  )

  val userPrefs: StateFlow<UserPrefs> = prefsRepo.prefs.stateIn(
    viewModelScope,
    SharingStarted.WhileSubscribed(5000),
    UserPrefs()
  )

  val inkModelState: StateFlow<InkModelState> = digitalInk.modelState
  val downloadedInkTags: StateFlow<Set<String>> = digitalInk.downloadedTags

  val recentOcrScans: StateFlow<List<OcrScanRecord>> = db.ocrScanDao().observeRecent()
    .map { list -> list.map { it.toDomain() } }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  private val undoStack = ArrayDeque<UndoCommand>()
  private val redoStack = ArrayDeque<UndoCommand>()
  private val pendingDbWrites = java.util.concurrent.atomic.AtomicInteger(0)

  private var pageObjectsJob: Job? = null
  private var smartWritingJob: Job? = null
  private var pdfLoadJob: Job? = null

  init {
    viewModelScope.launch {
      val initialPrefs = prefsRepo.prefs.first()
      _ui.update {
        it.copy(
          activeTool = initialPrefs.defaultBrush,
          lastBrushTool = initialPrefs.defaultBrush,
          brushColor = initialPrefs.defaultBrushColor,
          strokeWidth = initialPrefs.defaultBrushSize,
          smartShapesEnabled = initialPrefs.smartShapesEnabled
        )
      }
      loadSession()
      digitalInk.refreshDownloaded()
    }
  }

  private suspend fun loadSession() {
    val existing = withContext(Dispatchers.IO) { db.sessionDao().getById(sessionId)?.toDomain() }
    val session = existing ?: run {
      val p = prefsRepo.prefs.first()
      val fresh = BoardSession(
        id = sessionId,
        title = "Untitled Board",
        backgroundColor = p.defaultBackgroundColor,
        backgroundStyle = p.defaultBackgroundStyle
      )
      withContext(Dispatchers.IO) { db.sessionDao().upsert(fresh.toEntity()) }
      fresh
    }

    var layers = withContext(Dispatchers.IO) { db.layerDao().getForSession(sessionId).map { it.toDomain() } }
    if (layers.isEmpty()) {
      val defaultLayer = BoardLayer(sessionId = sessionId, name = "Layer 1", zOrder = 0)
      withContext(Dispatchers.IO) { db.layerDao().upsert(defaultLayer.toEntity()) }
      layers = listOf(defaultLayer)
    }

    // Ensure brush contrasts with board color on initial open
    val isDark = TeachColors.isDarkColor(session.backgroundColor)
    val startBrush = _ui.value.brushColor
    val adjustedBrush = if (isDark && TeachColors.isDarkColor(startBrush)) TeachColors.BrushWhite else startBrush

    _ui.update {
      it.copy(
        session = session,
        layers = layers,
        activeLayerId = layers.first().id,
        brushColor = adjustedBrush
      )
    }

    // Observe session metadata & layers
    viewModelScope.launch {
      db.sessionDao().observeById(sessionId).collect { ent ->
        if (ent != null) {
          val dom = ent.toDomain()
          _ui.update { it.copy(session = dom) }
        }
      }
    }
    viewModelScope.launch {
      db.layerDao().observeForSession(sessionId).collect { ents ->
        val doms = ents.map { it.toDomain() }
        if (doms.isNotEmpty()) {
          _ui.update { cur ->
            val active = if (doms.any { it.id == cur.activeLayerId }) cur.activeLayerId else doms.first().id
            cur.copy(layers = doms, activeLayerId = active)
          }
        }
      }
    }

    observePage(0)
  }

  private fun observePage(pageIndex: Int) {
    // Cancel previous page observer before subscribing to the new page (PRD 19.5)
    pageObjectsJob?.cancel()
    undoStack.clear()
    redoStack.clear()
    _ui.update {
      it.copy(
        pageIndex = pageIndex,
        selectedUids = emptySet(),
        smartShapeOffer = null,
        canUndo = false,
        canRedo = false
      )
    }
    pageObjectsJob = viewModelScope.launch {
      // Load initial snapshot immediately, then only apply Room updates when no optimistic write is in flight
      val initial = withContext(Dispatchers.IO) {
        db.canvasObjectDao().getForPage(sessionId, pageIndex).map { it.toDomain() }
      }
      _ui.update { it.copy(objects = initial) }
      db.canvasObjectDao().observeForPage(sessionId, pageIndex).collect { ents ->
        if (pendingDbWrites.get() == 0) {
          val doms = ents.map { it.toDomain() }
          _ui.update { it.copy(objects = doms) }
        }
      }
    }
    loadPdfPageIfNeeded(pageIndex)
  }

  private fun loadPdfPageIfNeeded(pageIndex: Int) {
    pdfLoadJob?.cancel()
    val path = _ui.value.session?.pdfPath
    if (path.isNullOrBlank()) {
      _ui.update { it.copy(pdfBitmap = null, pdfLoading = false) }
      return
    }
    val file = File(path)
    if (!file.exists()) {
      _ui.update { it.copy(pdfBitmap = null, pdfLoading = false) }
      return
    }
    pdfLoadJob = viewModelScope.launch {
      _ui.update { it.copy(pdfLoading = true) }
      val bmp = LazyPdfCache.renderPage(file, pageIndex, maxLongSide = 1600)
      _ui.update { it.copy(pdfBitmap = bmp, pdfLoading = false) }
      val total = _ui.value.session?.pageCount ?: 1
      LazyPdfCache.prefetchNeighbors(file, pageIndex, total)
    }
  }

  // ===== Tool & Drawing State =====

  fun selectTool(tool: ToolType) {
    _ui.update { cur ->
      cur.copy(
        activeTool = tool,
        lastBrushTool = if (tool.isBrush) tool else cur.lastBrushTool,
        lastShapeTool = if (tool.isShape) tool else cur.lastShapeTool,
        selectedUids = if (tool == ToolType.Lasso) cur.selectedUids else emptySet(),
        smartShapeOffer = null
      )
    }
  }

  fun setEraserMode(mode: EraserMode) = _ui.update { it.copy(eraserMode = mode) }
  fun setBrushColor(color: Int) = _ui.update { it.copy(brushColor = color) }
  fun setFillColor(color: Int?) = _ui.update { it.copy(fillColor = color) }
  fun setIsFilled(filled: Boolean) = _ui.update { it.copy(isFilled = filled) }
  fun setStrokeWidth(width: Float) = _ui.update { it.copy(strokeWidth = width.coerceIn(2f, 72f)) }
  fun setAlpha(alpha: Float) = _ui.update { it.copy(alpha = alpha.coerceIn(0.1f, 1f)) }
  fun setFontSize(size: Float) = _ui.update { it.copy(fontSize = size.coerceIn(14f, 140f)) }
  fun setLaserColor(color: Int) = _ui.update { it.copy(laserColor = color) }
  fun setLaserSize(size: Float) = _ui.update { it.copy(laserSize = size.coerceIn(4f, 40f)) }

  fun setSmartShapesEnabled(enabled: Boolean) {
    _ui.update { it.copy(smartShapesEnabled = enabled, smartShapeOffer = if (enabled) it.smartShapeOffer else null) }
    viewModelScope.launch { prefsRepo.setSmartShapesEnabled(enabled) }
  }

  fun toggleStylusOnly() {
    val next = !userPrefs.value.stylusOnly
    viewModelScope.launch { prefsRepo.setStylusOnly(next) }
    showMessage(if (next) "Palm Rejection (Stylus-Only): ON" else "Palm Rejection (Stylus-Only): OFF")
  }

  fun updateViewport(scale: Float, panX: Float, panY: Float) {
    _ui.update {
      it.copy(
        zoom = scale.coerceIn(AppConfig.MIN_ZOOM, AppConfig.MAX_ZOOM),
        panX = panX,
        panY = panY
      )
    }
  }

  fun resetZoom() = _ui.update { it.copy(zoom = 1f, panX = 0f, panY = 0f) }

  fun zoomBy(factor: Float) {
    _ui.update { cur ->
      cur.copy(zoom = (cur.zoom * factor).coerceIn(AppConfig.MIN_ZOOM, AppConfig.MAX_ZOOM))
    }
  }

  // ===== Commit Strokes, Shapes & Smart Shapes =====

  private fun isActiveLayerLocked(): Boolean {
    val cur = _ui.value
    val layer = cur.layers.firstOrNull { it.id == cur.activeLayerId }
    return layer?.locked == true
  }

  private fun nextZIndex(): Int = (_ui.value.objects.maxOfOrNull { it.zIndex } ?: 0) + 1

  fun commitStroke(tool: ToolType, points: List<StrokePoint>, isEraser: Boolean) {
    if (points.isEmpty()) return
    if (isActiveLayerLocked()) {
      showMessage("Active layer is locked")
      return
    }
    val cur = _ui.value
    val obj = CanvasObject(
      sessionId = sessionId,
      pageIndex = cur.pageIndex,
      layerId = cur.activeLayerId,
      type = if (isEraser) ToolType.Eraser else tool,
      color = cur.brushColor,
      strokeWidth = cur.strokeWidth,
      alpha = cur.alpha,
      isEraser = isEraser,
      zIndex = nextZIndex(),
      points = points
    )
    // F-03 Smart Shapes: auto-snap IMMEDIATELY into crisp geometry + offer visual capsule to keep as drawn!
    if (!isEraser && tool == ToolType.Pen && cur.smartShapesEnabled && points.size >= 2) {
      val candidates = ShapeClassifier.classify(points)
      val best = candidates.firstOrNull()
      if (best != null) {
        val snapped = obj.copy(
          type = best.toolType,
          geometry = best.geometry,
          isFilled = cur.isFilled,
          fillColor = cur.fillColor,
          updatedAt = System.currentTimeMillis()
        )
        addObjectWithUndo(snapped)
        _ui.update {
          it.copy(
            smartShapeOffer = SmartShapeOffer(snapped.objectUid, obj, candidates),
            selectedUids = setOf(snapped.objectUid)
          )
        }
        return
      }
    }

    addObjectWithUndo(obj)
    _ui.update { it.copy(smartShapeOffer = null) }
  }

  fun acceptSmartShape(candidate: SmartShapeCandidate) {
    val offer = _ui.value.smartShapeOffer ?: return
    val current = _ui.value.objects.firstOrNull { it.objectUid == offer.sourceObjectUid } ?: return
    val replaced = current.copy(
      type = candidate.toolType,
      geometry = candidate.geometry,
      isFilled = _ui.value.isFilled,
      fillColor = _ui.value.fillColor,
      updatedAt = System.currentTimeMillis()
    )
    replaceObjectsWithUndo(listOf(current), listOf(replaced))
    _ui.update { it.copy(smartShapeOffer = null, selectedUids = setOf(replaced.objectUid)) }
  }

  fun keepSmartShapeAsDrawn() {
    val offer = _ui.value.smartShapeOffer ?: return
    val current = _ui.value.objects.firstOrNull { it.objectUid == offer.sourceObjectUid } ?: return
    replaceObjectsWithUndo(listOf(current), listOf(offer.originalStroke))
    _ui.update { it.copy(smartShapeOffer = null, selectedUids = emptySet()) }
  }

  fun dismissSmartShapeOffer() = _ui.update { it.copy(smartShapeOffer = null) }

  fun commitShape(tool: ToolType, start: StrokePoint, end: StrokePoint) {
    if (isActiveLayerLocked()) {
      showMessage("Active layer is locked")
      return
    }
    val cur = _ui.value
    val obj = CanvasObject(
      sessionId = sessionId,
      pageIndex = cur.pageIndex,
      layerId = cur.activeLayerId,
      type = tool,
      color = cur.brushColor,
      fillColor = cur.fillColor,
      isFilled = cur.isFilled,
      strokeWidth = cur.strokeWidth,
      alpha = cur.alpha,
      zIndex = nextZIndex(),
      points = listOf(start, end)
    )
    addObjectWithUndo(obj)
    // Auto-select newly drawn shape so user can immediately move/resize/copy/lock/delete it!
    _ui.update { it.copy(selectedUids = setOf(obj.objectUid)) }
  }

  fun commitGeometryContour(contour: Contour) {
    if (isActiveLayerLocked()) {
      showMessage("Active layer is locked")
      return
    }
    val cur = _ui.value
    val obj = CanvasObject(
      sessionId = sessionId,
      pageIndex = cur.pageIndex,
      layerId = cur.activeLayerId,
      type = ToolType.Line,
      color = cur.brushColor,
      strokeWidth = cur.strokeWidth,
      alpha = cur.alpha,
      zIndex = nextZIndex(),
      geometry = listOf(contour),
      points = contour.points
    )
    addObjectWithUndo(obj)
  }

  fun stampGeometryLine(a: StrokePoint, b: StrokePoint) {
    if (isActiveLayerLocked()) return
    val cur = _ui.value
    val obj = CanvasObject(
      sessionId = sessionId,
      pageIndex = cur.pageIndex,
      layerId = cur.activeLayerId,
      type = ToolType.Line,
      color = cur.brushColor,
      strokeWidth = cur.strokeWidth.coerceAtLeast(2.5f),
      alpha = cur.alpha,
      zIndex = nextZIndex(),
      points = listOf(a, b)
    )
    addObjectWithUndo(obj)
  }

  fun stampGeometryArc(needle: StrokePoint, start: StrokePoint, end: StrokePoint) {
    if (isActiveLayerLocked()) return
    val cur = _ui.value
    val r = hypot(start.x - needle.x, start.y - needle.y)
    val a0 = Math.toDegrees(atan2((start.y - needle.y).toDouble(), (start.x - needle.x).toDouble())).toFloat()
    val a1 = Math.toDegrees(atan2((end.y - needle.y).toDouble(), (end.x - needle.x).toDouble())).toFloat()
    val sweep = GeoStampPath.normSweep(a1 - a0)
    val steps = 36
    val pts = (0..steps).map { i ->
      val rad = Math.toRadians((a0 + sweep * (i.toDouble() / steps)).toDouble())
      StrokePoint(needle.x + (cos(rad) * r).toFloat(), needle.y + (sin(rad) * r).toFloat())
    }
    val contour = Contour(closed = false, points = pts)
    val obj = CanvasObject(
      sessionId = sessionId,
      pageIndex = cur.pageIndex,
      layerId = cur.activeLayerId,
      type = ToolType.Circle,
      color = cur.brushColor,
      strokeWidth = cur.strokeWidth.coerceAtLeast(2.5f),
      alpha = cur.alpha,
      zIndex = nextZIndex(),
      geometry = listOf(contour),
      points = pts
    )
    addObjectWithUndo(obj)
  }

  fun stampGeometryAngle(pivot: StrokePoint, armEnd: StrokePoint, baseEnd: StrokePoint, kind: GeoStampKind) {
    if (isActiveLayerLocked()) return
    val cur = _ui.value
    val r = hypot(armEnd.x - pivot.x, armEnd.y - pivot.y)
    val a0 = Math.toDegrees(atan2((baseEnd.y - pivot.y).toDouble(), (baseEnd.x - pivot.x).toDouble())).toFloat()
    val a1 = Math.toDegrees(atan2((armEnd.y - pivot.y).toDouble(), (armEnd.x - pivot.x).toDouble())).toFloat()
    val sweep = GeoStampPath.normSweep(a1 - a0)

    val obj = when (kind) {
      GeoStampKind.Angle -> {
        // 1. Two clean straight rays meeting at pivot (baseEnd -> pivot -> armEnd)
        val raysContour = Contour(closed = false, points = listOf(baseEnd, pivot, armEnd))
        // 2. Angle indicator arc near vertex (radius = r * 0.35)
        val arcR = (r * 0.35f).coerceAtLeast(18f)
        val arcSteps = 20
        val arcPts = (0..arcSteps).map { i ->
          val rad = Math.toRadians((a0 + sweep * (i.toDouble() / arcSteps)).toDouble())
          StrokePoint(pivot.x + (cos(rad) * arcR).toFloat(), pivot.y + (sin(rad) * arcR).toFloat())
        }
        val indicatorContour = Contour(closed = false, points = arcPts)
        CanvasObject(
          sessionId = sessionId,
          pageIndex = cur.pageIndex,
          layerId = cur.activeLayerId,
          type = ToolType.RightAngle,
          color = cur.brushColor,
          strokeWidth = cur.strokeWidth.coerceAtLeast(2.5f),
          alpha = cur.alpha,
          zIndex = nextZIndex(),
          geometry = listOf(raysContour, indicatorContour),
          points = listOf(baseEnd, pivot, armEnd) + arcPts
        )
      }
      GeoStampKind.Arc -> {
        val steps = 36
        val pts = (0..steps).map { i ->
          val rad = Math.toRadians((a0 + sweep * (i.toDouble() / steps)).toDouble())
          StrokePoint(pivot.x + (cos(rad) * r).toFloat(), pivot.y + (sin(rad) * r).toFloat())
        }
        CanvasObject(
          sessionId = sessionId,
          pageIndex = cur.pageIndex,
          layerId = cur.activeLayerId,
          type = ToolType.Circle,
          color = cur.brushColor,
          strokeWidth = cur.strokeWidth.coerceAtLeast(2.5f),
          alpha = cur.alpha,
          zIndex = nextZIndex(),
          geometry = listOf(Contour(closed = false, points = pts)),
          points = pts
        )
      }
      GeoStampKind.Sector -> {
        val steps = 36
        val pts = mutableListOf(pivot)
        for (i in 0..steps) {
          val rad = Math.toRadians((a0 + sweep * (i.toDouble() / steps)).toDouble())
          pts.add(StrokePoint(pivot.x + (cos(rad) * r).toFloat(), pivot.y + (sin(rad) * r).toFloat()))
        }
        pts.add(pivot)
        val fillCol = cur.fillColor ?: cur.brushColor
        CanvasObject(
          sessionId = sessionId,
          pageIndex = cur.pageIndex,
          layerId = cur.activeLayerId,
          type = ToolType.Sector,
          color = cur.brushColor,
          fillColor = fillCol,
          isFilled = true,
          strokeWidth = cur.strokeWidth.coerceAtLeast(2.5f),
          alpha = cur.alpha,
          zIndex = nextZIndex(),
          geometry = listOf(Contour(closed = true, points = pts)),
          points = pts
        )
      }
    }
    addObjectWithUndo(obj)
  }

  fun strokeEraseAt(x: Float, y: Float, radius: Float) {
    if (isActiveLayerLocked()) return
    val cur = _ui.value
    val hits = cur.objects.filter { obj ->
      obj.layerId == cur.activeLayerId && ObjectPainter.hitTest(obj, x, y, radius)
    }
    if (hits.isNotEmpty()) {
      removeObjectsWithUndo(hits)
    }
  }

  // ===== Smart Writing (F-05) =====

  fun addSmartInkStroke(points: List<StrokePoint>) {
    if (points.isEmpty()) return
    if (isActiveLayerLocked()) {
      showMessage("Active layer is locked")
      return
    }
    _ui.update { it.copy(smartInkStrokes = it.smartInkStrokes + listOf(points)) }
    smartWritingJob?.cancel()
    smartWritingJob = viewModelScope.launch {
      delay(AppConfig.SMART_WRITING_PAUSE_MS)
      convertSmartInkNow()
    }
  }

  fun convertSmartInkNow() {
    smartWritingJob?.cancel()
    val strokes = _ui.value.smartInkStrokes
    if (strokes.isEmpty()) return
    viewModelScope.launch {
      _ui.update { it.copy(smartRecognizing = true) }
      val lang = userPrefs.value.smartWritingLanguage
      val res = digitalInk.recognize(strokes, lang)
      _ui.update { it.copy(smartRecognizing = false, smartInkStrokes = emptyList()) }
      res.onSuccess { recognized ->
        var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
        var maxY = -Float.MAX_VALUE
        for (s in strokes) {
          for (p in s) {
            if (p.x < minX) minX = p.x
            if (p.y < minY) minY = p.y
            if (p.y > maxY) maxY = p.y
          }
        }
        val estFontSize = ((maxY - minY) * 0.72f).coerceIn(28f, 96f)
        addTextObject(
          meta = Serialization.RichTextMeta(text = recognized),
          x = minX,
          y = minY,
          color = _ui.value.brushColor,
          fontSize = estFontSize
        )
      }.onFailure {
        showMessage("Could not recognize handwriting. Try again or download language model.")
      }
    }
  }

  fun clearSmartInkBuffer() {
    smartWritingJob?.cancel()
    _ui.update { it.copy(smartInkStrokes = emptyList(), smartRecognizing = false) }
  }

  fun selectSmartWritingLanguage(tag: String) {
    viewModelScope.launch {
      prefsRepo.setSmartWritingLanguage(tag)
      if (!digitalInk.isDownloaded(tag)) {
        digitalInk.downloadModel(tag)
      }
    }
  }

  // ===== Rich Objects (Text, Formula, Table, Image, Sticker) =====

  fun addTextObject(
    meta: Serialization.RichTextMeta,
    x: Float = 180f,
    y: Float = 240f,
    color: Int = _ui.value.brushColor,
    fontSize: Float = _ui.value.fontSize,
    replaceUid: String? = null
  ) {
    if (meta.text.isBlank()) return
    if (isActiveLayerLocked()) { showMessage("Active layer is locked"); return }
    val cur = _ui.value
    val encoded = Serialization.encodeRichText(meta)
    val existing = replaceUid?.let { uid -> cur.objects.firstOrNull { it.objectUid == uid } }
    if (existing != null) {
      val updated = existing.copy(
        textContent = encoded,
        color = color,
        fontSize = fontSize,
        updatedAt = System.currentTimeMillis()
      )
      replaceObjectsWithUndo(listOf(existing), listOf(updated))
    } else {
      val obj = CanvasObject(
        sessionId = sessionId,
        pageIndex = cur.pageIndex,
        layerId = cur.activeLayerId,
        type = ToolType.Text,
        color = color,
        fontSize = fontSize,
        zIndex = nextZIndex(),
        points = listOf(StrokePoint(x, y)),
        textContent = encoded
      )
      addObjectWithUndo(obj)
      _ui.update { it.copy(selectedUids = setOf(obj.objectUid)) }
    }
  }

  fun addFormulaObject(
    latex: String,
    x: Float = 180f,
    y: Float = 260f,
    color: Int = _ui.value.brushColor,
    fontSize: Float = 44f,
    replaceUid: String? = null
  ) {
    if (latex.isBlank()) return
    if (isActiveLayerLocked()) { showMessage("Active layer is locked"); return }
    val cur = _ui.value
    val (w, h) = FormulaParser.measure(latex, fontSize)
    val existing = replaceUid?.let { uid -> cur.objects.firstOrNull { it.objectUid == uid } }
    if (existing != null) {
      val origin = existing.points.firstOrNull() ?: StrokePoint(x, y)
      val updated = existing.copy(
        textContent = latex,
        color = color,
        fontSize = fontSize,
        points = listOf(origin, StrokePoint(origin.x + w, origin.y + h)),
        updatedAt = System.currentTimeMillis()
      )
      replaceObjectsWithUndo(listOf(existing), listOf(updated))
    } else {
      val obj = CanvasObject(
        sessionId = sessionId,
        pageIndex = cur.pageIndex,
        layerId = cur.activeLayerId,
        type = ToolType.Formula,
        color = color,
        fontSize = fontSize,
        zIndex = nextZIndex(),
        points = listOf(StrokePoint(x, y), StrokePoint(x + w, y + h)),
        textContent = latex
      )
      addObjectWithUndo(obj)
      _ui.update { it.copy(selectedUids = setOf(obj.objectUid)) }
    }
  }

  fun addTableObject(
    rows: Int,
    cols: Int,
    cells: List<List<String>>,
    x: Float = 140f,
    y: Float = 220f,
    borderColor: Int = _ui.value.brushColor,
    headerColor: Int = TeachColors.AccentBlue.hashCode(),
    replaceUid: String? = null
  ) {
    if (isActiveLayerLocked()) { showMessage("Active layer is locked"); return }
    val cur = _ui.value
    val payload = Serialization.encodeTablePayload(rows, cols, cells)
    val w = (cols * 140f).coerceIn(240f, 880f)
    val h = (rows * 68f).coerceIn(140f, 760f)
    val existing = replaceUid?.let { uid -> cur.objects.firstOrNull { it.objectUid == uid } }
    if (existing != null) {
      val updated = existing.copy(
        textContent = payload,
        color = borderColor,
        fillColor = headerColor,
        updatedAt = System.currentTimeMillis()
      )
      replaceObjectsWithUndo(listOf(existing), listOf(updated))
    } else {
      val obj = CanvasObject(
        sessionId = sessionId,
        pageIndex = cur.pageIndex,
        layerId = cur.activeLayerId,
        type = ToolType.Table,
        color = borderColor,
        fillColor = headerColor,
        strokeWidth = 3f,
        fontSize = 24f,
        zIndex = nextZIndex(),
        points = listOf(StrokePoint(x, y), StrokePoint(x + w, y + h)),
        textContent = payload
      )
      addObjectWithUndo(obj)
      _ui.update { it.copy(selectedUids = setOf(obj.objectUid)) }
    }
  }

  fun addStickerObject(stickerId: String, color: Int = _ui.value.brushColor) {
    if (isActiveLayerLocked()) { showMessage("Active layer is locked"); return }
    val cur = _ui.value
    val centerWorldX = (360f - cur.panX) / cur.zoom
    val centerWorldY = (480f - cur.panY) / cur.zoom
    val half = 110f
    val obj = CanvasObject(
      sessionId = sessionId,
      pageIndex = cur.pageIndex,
      layerId = cur.activeLayerId,
      type = ToolType.Sticker,
      color = color,
      zIndex = nextZIndex(),
      points = listOf(
        StrokePoint(centerWorldX - half, centerWorldY - half),
        StrokePoint(centerWorldX + half, centerWorldY + half)
      ),
      textContent = stickerId
    )
    addObjectWithUndo(obj)
    _ui.update { it.copy(selectedUids = setOf(obj.objectUid)) }
  }

  fun addImageFromUri(uri: Uri, attribution: String? = null) {
    viewModelScope.launch {
      val copied = Exporters.copyUriToImagesDir(app, uri)
      if (copied == null) {
        showMessage("Failed to load image")
        return@launch
      }
      val (file, w, h) = copied
      insertLocalImageFile(file, w, h, attribution)
    }
  }

  fun addWikimediaImage(img: WikimediaImage) {
    viewModelScope.launch {
      val isSvg = img.fullUrl.lowercase().endsWith(".svg") || img.title.lowercase().endsWith(".svg")
      val targetUrl = if (isSvg) img.thumbUrl else img.fullUrl
      val file = WikimediaSearch.downloadToImagesDir(app, targetUrl)
        ?: WikimediaSearch.downloadToImagesDir(app, img.thumbUrl)
      if (file == null) {
        showMessage("Download failed — check internet connection")
        return@launch
      }
      val opts = android.graphics.BitmapFactory.Options().apply { inJustDecodeBounds = true }
      android.graphics.BitmapFactory.decodeFile(file.absolutePath, opts)
      val w = if (opts.outWidth > 0) opts.outWidth else 480
      val h = if (opts.outHeight > 0) opts.outHeight else 360
      insertLocalImageFile(file, w, h, img.attribution)
    }
  }

  private fun insertLocalImageFile(file: File, w: Int, h: Int, attribution: String?) {
    if (isActiveLayerLocked()) { showMessage("Active layer is locked"); return }
    val cur = _ui.value
    val maxBox = 480f
    val scale = min(maxBox / max(w, 1), maxBox / max(h, 1))
    val dw = (w * scale).coerceAtLeast(120f)
    val dh = (h * scale).coerceAtLeast(120f)
    val cx = (960f - cur.panX) / cur.zoom.coerceAtLeast(0.1f)
    val cy = (540f - cur.panY) / cur.zoom.coerceAtLeast(0.1f)
    val obj = CanvasObject(
      sessionId = sessionId,
      pageIndex = cur.pageIndex,
      layerId = cur.activeLayerId,
      type = ToolType.Image,
      zIndex = nextZIndex(),
      points = listOf(
        StrokePoint(cx - dw / 2f, cy - dh / 2f),
        StrokePoint(cx + dw / 2f, cy + dh / 2f)
      ),
      imagePath = file.absolutePath,
      textContent = attribution
    )
    addObjectWithUndo(obj)
    selectTool(ToolType.Lasso)
    _ui.update { it.copy(selectedUids = setOf(obj.objectUid)) }
  }

  // ===== Selection Operations =====

  fun setSelection(uids: Set<String>) = _ui.update { cur ->
    val keepOffer = cur.smartShapeOffer?.takeIf { it.sourceObjectUid in uids }
    cur.copy(selectedUids = uids, smartShapeOffer = keepOffer)
  }

  fun toggleLockSelection() {
    val sel = _ui.value.selectedUids
    if (sel.isEmpty()) return
    _ui.update { cur ->
      val allLocked = sel.all { it in cur.lockedObjectUids }
      val nextLocked = if (allLocked) cur.lockedObjectUids - sel else cur.lockedObjectUids + sel
      cur.copy(lockedObjectUids = nextLocked)
    }
  }

  fun moveSelection(dx: Float, dy: Float) {
    val cur = _ui.value
    val sel = cur.selectedUids.filterNot { it in cur.lockedObjectUids }.toSet()
    if (sel.isEmpty()) return
    val before = cur.objects.filter { it.objectUid in sel }
    if (before.isEmpty()) return
    val after = before.map { ObjectPainter.translate(it, dx, dy) }
    replaceObjectsWithUndo(before, after)
  }

  fun scaleSelection(anchorX: Float, anchorY: Float, factor: Float) {
    val cur = _ui.value
    val sel = cur.selectedUids.filterNot { it in cur.lockedObjectUids }.toSet()
    if (sel.isEmpty()) return
    val before = cur.objects.filter { it.objectUid in sel }
    if (before.isEmpty()) return
    val after = before.map { ObjectPainter.scale(it, anchorX, anchorY, factor) }
    replaceObjectsWithUndo(before, after)
  }

  fun scaleSelectionAxes(anchorX: Float, anchorY: Float, sx: Float, sy: Float) {
    val cur = _ui.value
    val sel = cur.selectedUids.filterNot { it in cur.lockedObjectUids }.toSet()
    if (sel.isEmpty()) return
    val before = cur.objects.filter { it.objectUid in sel }
    if (before.isEmpty()) return
    val after = before.map { ObjectPainter.scaleAxes(it, anchorX, anchorY, sx, sy) }
    replaceObjectsWithUndo(before, after)
  }

  fun rotateSelection(anchorX: Float, anchorY: Float, degrees: Float) {
    val cur = _ui.value
    val sel = cur.selectedUids.filterNot { it in cur.lockedObjectUids }.toSet()
    if (sel.isEmpty()) return
    val before = cur.objects.filter { it.objectUid in sel }
    if (before.isEmpty()) return
    val after = before.map { ObjectPainter.rotate(it, anchorX, anchorY, degrees) }
    replaceObjectsWithUndo(before, after)
  }

  fun flipSelectionHorizontal() {
    val cur = _ui.value
    val sel = cur.selectedUids.filterNot { it in cur.lockedObjectUids }.toSet()
    if (sel.isEmpty()) return
    val before = cur.objects.filter { it.objectUid in sel }
    if (before.isEmpty()) return
    var minX = Float.MAX_VALUE
    var maxX = -Float.MAX_VALUE
    before.forEach {
      val b = ObjectPainter.getBounds(it)
      if (b.left < minX) minX = b.left
      if (b.right > maxX) maxX = b.right
    }
    val cx = (minX + maxX) / 2f
    val after = before.map { ObjectPainter.flipHorizontal(it, cx) }
    replaceObjectsWithUndo(before, after)
  }

  fun saveToolbarCustomization(order: List<ToolbarAction>, hidden: Set<ToolbarAction>) {
    viewModelScope.launch { prefsRepo.setToolbarCustomization(order, hidden) }
  }

  fun resetToolbarCustomization() {
    viewModelScope.launch { prefsRepo.resetToolbarCustomization() }
    showMessage("Toolbar reset to defaults")
  }

  fun deleteSelection() {
    val cur = _ui.value
    val sel = cur.selectedUids
    if (sel.isEmpty()) return
    val targets = cur.objects.filter { it.objectUid in sel }
    if (targets.isEmpty()) return
    removeObjectsWithUndo(targets)
    _ui.update { it.copy(selectedUids = emptySet(), lockedObjectUids = it.lockedObjectUids - sel) }
  }

  fun duplicateSelection() {
    val sel = _ui.value.selectedUids
    if (sel.isEmpty()) return
    val originals = _ui.value.objects.filter { it.objectUid in sel }
    val newUids = mutableSetOf<String>()
    var z = nextZIndex()
    for (o in originals) {
      val copy = ObjectPainter.translate(o, 40f, 40f).copy(
        id = 0L,
        objectUid = UUID.randomUUID().toString(),
        zIndex = z++,
        updatedAt = System.currentTimeMillis()
      )
      addObjectWithUndo(copy)
      newUids.add(copy.objectUid)
    }
    _ui.update { it.copy(selectedUids = newUids) }
  }

  fun recolorSelection(newColor: Int) {
    val sel = _ui.value.selectedUids
    if (sel.isEmpty()) return
    val before = _ui.value.objects.filter { it.objectUid in sel }
    if (before.isEmpty()) return
    val after = before.map {
      it.copy(
        color = newColor,
        fillColor = if (it.type == ToolType.Table) newColor else it.fillColor,
        updatedAt = System.currentTimeMillis()
      )
    }
    replaceObjectsWithUndo(before, after)
  }

  // ===== Synchronous Optimistic State + 60-Step Undo/Redo (Zero-Flicker 120fps Ink!) =====

  private fun pushUndo(cmd: UndoCommand) {
    undoStack.addLast(cmd)
    while (undoStack.size > AppConfig.MAX_UNDO_STEPS) undoStack.removeFirst()
    redoStack.clear()
    syncUndoFlags()
  }

  private fun syncUndoFlags() {
    _ui.update { it.copy(canUndo = undoStack.isNotEmpty(), canRedo = redoStack.isNotEmpty()) }
  }

  private fun addObjectWithUndo(obj: CanvasObject) {
    pushUndo(UndoCommand.Add(obj))
    // Instant synchronous in-memory append (zero flicker on stroke end!)
    _ui.update { cur -> cur.copy(objects = cur.objects + obj, smartShapeOffer = null) }
    pendingDbWrites.incrementAndGet()
    viewModelScope.launch(Dispatchers.IO) {
      try {
        db.canvasObjectDao().upsert(obj.toEntity())
        touchSession()
      } finally {
        pendingDbWrites.decrementAndGet()
      }
    }
  }

  private fun removeObjectsWithUndo(targets: List<CanvasObject>) {
    if (targets.isEmpty()) return
    pushUndo(UndoCommand.Remove(targets))
    val removeIds = targets.map { it.objectUid }.toSet()
    _ui.update { cur ->
      cur.copy(
        objects = cur.objects.filterNot { it.objectUid in removeIds },
        smartShapeOffer = cur.smartShapeOffer?.takeUnless { it.sourceObjectUid in removeIds }
      )
    }
    pendingDbWrites.incrementAndGet()
    viewModelScope.launch(Dispatchers.IO) {
      try {
        db.canvasObjectDao().deleteByUids(targets.map { it.objectUid })
        touchSession()
      } finally {
        pendingDbWrites.decrementAndGet()
      }
    }
  }

  private fun replaceObjectsWithUndo(before: List<CanvasObject>, after: List<CanvasObject>) {
    pushUndo(UndoCommand.Replace(before, after))
    val byUid = after.associateBy { it.objectUid }
    _ui.update { cur ->
      cur.copy(objects = cur.objects.map { o -> byUid[o.objectUid] ?: o })
    }
    pendingDbWrites.incrementAndGet()
    viewModelScope.launch(Dispatchers.IO) {
      try {
        db.canvasObjectDao().upsertAll(after.map { it.toEntity() })
        touchSession()
      } finally {
        pendingDbWrites.decrementAndGet()
      }
    }
  }

  fun undo() {
    val cmd = undoStack.removeLastOrNull() ?: return
    redoStack.addLast(cmd)
    syncUndoFlags()
    _ui.update { cur ->
      val nextObjs = when (cmd) {
        is UndoCommand.Add -> cur.objects.filterNot { it.objectUid == cmd.obj.objectUid }
        is UndoCommand.Remove -> (cur.objects + cmd.objects).sortedBy { it.zIndex }
        is UndoCommand.Replace -> {
          val byUid = cmd.before.associateBy { it.objectUid }
          cur.objects.map { byUid[it.objectUid] ?: it }
        }
      }
      cur.copy(objects = nextObjs, smartShapeOffer = null, selectedUids = emptySet())
    }
    pendingDbWrites.incrementAndGet()
    viewModelScope.launch(Dispatchers.IO) {
      try {
        when (cmd) {
          is UndoCommand.Add -> db.canvasObjectDao().deleteByUid(cmd.obj.objectUid)
          is UndoCommand.Remove -> db.canvasObjectDao().upsertAll(cmd.objects.map { it.toEntity() })
          is UndoCommand.Replace -> db.canvasObjectDao().upsertAll(cmd.before.map { it.toEntity() })
        }
        touchSession()
      } finally {
        pendingDbWrites.decrementAndGet()
      }
    }
  }

  fun redo() {
    val cmd = redoStack.removeLastOrNull() ?: return
    undoStack.addLast(cmd)
    syncUndoFlags()
    _ui.update { cur ->
      val nextObjs = when (cmd) {
        is UndoCommand.Add -> (cur.objects + cmd.obj).sortedBy { it.zIndex }
        is UndoCommand.Remove -> {
          val rem = cmd.objects.map { it.objectUid }.toSet()
          cur.objects.filterNot { it.objectUid in rem }
        }
        is UndoCommand.Replace -> {
          val byUid = cmd.after.associateBy { it.objectUid }
          cur.objects.map { byUid[it.objectUid] ?: it }
        }
      }
      cur.copy(objects = nextObjs)
    }
    pendingDbWrites.incrementAndGet()
    viewModelScope.launch(Dispatchers.IO) {
      try {
        when (cmd) {
          is UndoCommand.Add -> db.canvasObjectDao().upsert(cmd.obj.toEntity())
          is UndoCommand.Remove -> db.canvasObjectDao().deleteByUids(cmd.objects.map { it.objectUid })
          is UndoCommand.Replace -> db.canvasObjectDao().upsertAll(cmd.after.map { it.toEntity() })
        }
        touchSession()
      } finally {
        pendingDbWrites.decrementAndGet()
      }
    }
  }

  fun clearCurrentPage() {
    val current = _ui.value.objects
    if (current.isEmpty()) return
    removeObjectsWithUndo(current)
    _ui.update { it.copy(selectedUids = emptySet(), smartShapeOffer = null) }
  }

  // ===== Board Background & Textures =====

  fun updateBackground(color: Int, style: BackgroundStyle) {
    val session = _ui.value.session ?: return
    val updated = session.copy(
      backgroundColor = color,
      backgroundStyle = style,
      updatedAt = System.currentTimeMillis()
    )
    // Auto-flip brush contrast if moving from light <-> dark board
    val nowDark = TeachColors.isDarkColor(color)
    val curBrush = _ui.value.brushColor
    val newBrush = when {
      nowDark && TeachColors.isDarkColor(curBrush) -> TeachColors.BrushWhite
      !nowDark && curBrush == TeachColors.BrushWhite -> TeachColors.BrushBlack
      else -> curBrush
    }
    _ui.update { it.copy(session = updated, brushColor = newBrush) }
    viewModelScope.launch(Dispatchers.IO) { db.sessionDao().upsert(updated.toEntity()) }
  }

  // ===== Layers (Up to 10, PRD F-17) =====

  fun selectLayer(layerId: String) = _ui.update { it.copy(activeLayerId = layerId) }

  fun addLayer() {
    val cur = _ui.value.layers
    if (cur.size >= AppConfig.MAX_LAYERS_PER_BOARD) {
      showMessage("Maximum ${AppConfig.MAX_LAYERS_PER_BOARD} layers per board")
      return
    }
    val nextOrder = (cur.maxOfOrNull { it.zOrder } ?: -1) + 1
    val layer = BoardLayer(
      sessionId = sessionId,
      name = "Layer ${cur.size + 1}",
      zOrder = nextOrder
    )
    viewModelScope.launch(Dispatchers.IO) {
      db.layerDao().upsert(layer.toEntity())
      withContext(Dispatchers.Main) { _ui.update { it.copy(activeLayerId = layer.id) } }
    }
  }

  fun toggleLayerVisible(layer: BoardLayer) {
    viewModelScope.launch(Dispatchers.IO) {
      db.layerDao().upsert(layer.copy(visible = !layer.visible).toEntity())
    }
  }

  fun toggleLayerLocked(layer: BoardLayer) {
    viewModelScope.launch(Dispatchers.IO) {
      db.layerDao().upsert(layer.copy(locked = !layer.locked).toEntity())
    }
  }

  fun renameLayer(layer: BoardLayer, newName: String) {
    val clean = newName.trim().ifBlank { return }
    viewModelScope.launch(Dispatchers.IO) {
      db.layerDao().upsert(layer.copy(name = clean).toEntity())
    }
  }

  fun clearLayer(layer: BoardLayer) {
    val targets = _ui.value.objects.filter { it.layerId == layer.id }
    if (targets.isNotEmpty()) removeObjectsWithUndo(targets)
  }

  fun deleteLayer(layer: BoardLayer) {
    if (_ui.value.layers.size <= 1) {
      showMessage("At least one layer is required")
      return
    }
    viewModelScope.launch(Dispatchers.IO) {
      db.canvasObjectDao().deleteLayerObjects(sessionId, layer.id)
      db.layerDao().deleteById(layer.id)
    }
  }

  fun reorderLayer(layer: BoardLayer, moveUp: Boolean) {
    val sorted = _ui.value.layers.sortedBy { it.zOrder }.toMutableList()
    val idx = sorted.indexOfFirst { it.id == layer.id }
    val targetIdx = if (moveUp) idx + 1 else idx - 1
    if (idx < 0 || targetIdx !in sorted.indices) return
    val tmp = sorted[idx]
    sorted[idx] = sorted[targetIdx]
    sorted[targetIdx] = tmp
    val updated = sorted.mapIndexed { i, l -> l.copy(zOrder = i) }
    viewModelScope.launch(Dispatchers.IO) {
      db.layerDao().upsertAll(updated.map { it.toEntity() })
    }
  }

  // ===== Pages & PDF Mode =====

  fun goToPage(index: Int) {
    val count = _ui.value.session?.pageCount ?: 1
    val clamped = index.coerceIn(0, (count - 1).coerceAtLeast(0))
    if (clamped != _ui.value.pageIndex) observePage(clamped)
  }

  fun addPage() {
    val session = _ui.value.session ?: return
    val newCount = session.pageCount + 1
    val updated = session.copy(pageCount = newCount, updatedAt = System.currentTimeMillis())
    viewModelScope.launch(Dispatchers.IO) {
      db.sessionDao().upsert(updated.toEntity())
      withContext(Dispatchers.Main) { observePage(newCount - 1) }
    }
  }

  fun duplicateCurrentPage() {
    val session = _ui.value.session ?: return
    val curIdx = _ui.value.pageIndex
    val curObjs = _ui.value.objects
    val newPageIdx = session.pageCount
    val updated = session.copy(pageCount = newPageIdx + 1, updatedAt = System.currentTimeMillis())
    viewModelScope.launch(Dispatchers.IO) {
      db.sessionDao().upsert(updated.toEntity())
      val clones = curObjs.map {
        it.copy(id = 0L, objectUid = UUID.randomUUID().toString(), pageIndex = newPageIdx)
          .toEntity()
      }
      if (clones.isNotEmpty()) db.canvasObjectDao().upsertAll(clones)
      withContext(Dispatchers.Main) { observePage(newPageIdx) }
    }
  }

  fun deletePage(index: Int) {
    val session = _ui.value.session ?: return
    if (session.pageCount <= 1) return
    viewModelScope.launch(Dispatchers.IO) {
      db.canvasObjectDao().deletePage(sessionId, index)
      val remaining = db.canvasObjectDao().getForSession(sessionId).map { ent ->
        if (ent.pageIndex > index) ent.copy(pageIndex = ent.pageIndex - 1) else ent
      }
      db.canvasObjectDao().deleteForSession(sessionId)
      if (remaining.isNotEmpty()) db.canvasObjectDao().upsertAll(remaining)
      val newCount = (session.pageCount - 1).coerceAtLeast(1)
      db.sessionDao().upsert(session.copy(pageCount = newCount, updatedAt = System.currentTimeMillis()).toEntity())
      withContext(Dispatchers.Main) {
        observePage(min(index, newCount - 1))
      }
    }
  }

  fun importPdf(uri: Uri) {
    val session = _ui.value.session ?: return
    viewModelScope.launch {
      _ui.update { it.copy(pdfLoading = true) }
      val copied = Exporters.copyPdfToSessionDir(app, sessionId, uri)
      if (copied == null) {
        _ui.update { it.copy(pdfLoading = false) }
        showMessage("Could not import PDF")
        return@launch
      }
      val pageCount = LazyPdfCache.getPageCount(copied).coerceAtLeast(1)
      val updated = session.copy(
        pdfPath = copied.absolutePath,
        pageCount = max(session.pageCount, pageCount),
        backgroundColor = TeachColors.BoardWhite,
        backgroundStyle = BackgroundStyle.PLAIN,
        updatedAt = System.currentTimeMillis()
      )
      withContext(Dispatchers.IO) { db.sessionDao().upsert(updated.toEntity()) }
      _ui.update { it.copy(session = updated) }
      observePage(0)
      showMessage("Imported $pageCount PDF pages")
    }
  }

  fun togglePdfBookmark() {
    val session = _ui.value.session ?: return
    val p = _ui.value.pageIndex
    val next = if (p in session.pdfBookmarks) session.pdfBookmarks - p else session.pdfBookmarks + p
    val updated = session.copy(pdfBookmarks = next, updatedAt = System.currentTimeMillis())
    _ui.update { it.copy(session = updated) }
    viewModelScope.launch(Dispatchers.IO) { db.sessionDao().upsert(updated.toEntity()) }
  }

  fun togglePdfHideCurrentPage() {
    val session = _ui.value.session ?: return
    val p = _ui.value.pageIndex
    val next = if (p in session.pdfHiddenPages) session.pdfHiddenPages - p else session.pdfHiddenPages + p
    val updated = session.copy(pdfHiddenPages = next, updatedAt = System.currentTimeMillis())
    _ui.update { it.copy(session = updated) }
    viewModelScope.launch(Dispatchers.IO) { db.sessionDao().upsert(updated.toEntity()) }
  }

  // ===== OCR (F-21) =====

  suspend fun runOcr(bitmap: Bitmap, script: OcrScript): OcrOutput {
    val output = ocrManager.recognize(bitmap, script)
    if (output.text.isNotBlank()) {
      ocrManager.saveScan(sessionId, bitmap, output.text, output.script)
    }
    return output
  }

  fun deleteOcrScan(id: Long) {
    viewModelScope.launch(Dispatchers.IO) { db.ocrScanDao().deleteById(id) }
  }

  // ===== Overlays & Helpers =====

  fun setGeometryTool(tool: GeometryToolType?) = _ui.update { it.copy(activeGeometryTool = tool) }
  fun setShowCalculator(show: Boolean) = _ui.update { it.copy(showCalculator = show) }
  fun setShowTimer(show: Boolean) = _ui.update { it.copy(showTimer = show) }
  fun setFloatingVideoUri(uri: Uri?) = _ui.update { it.copy(floatingVideoUri = uri) }
  fun setFaceCam(show: Boolean, shape: FaceCamShape = _ui.value.faceCamShape) =
    _ui.update { it.copy(showFaceCam = show, faceCamShape = shape) }

  fun updateShortcutTools(tools: List<ToolbarAction>) {
    viewModelScope.launch { prefsRepo.setShortcutTools(tools.take(AppConfig.MAX_SHORTCUTS)) }
  }

  fun dismissHint(key: String) {
    viewModelScope.launch { prefsRepo.dismissHint(key) }
  }

  fun completeTour() {
    viewModelScope.launch { prefsRepo.setTourCompleted(AppConfig.CURRENT_TOUR_VERSION) }
  }

  // ===== Save, Thumbnail, PNG & PDF Export =====

  fun saveBoardNow(viewWidth: Int = 1080, viewHeight: Int = 1920) {
    val session = _ui.value.session ?: return
    viewModelScope.launch {
      withContext(Dispatchers.IO) {
        val updated = session.copy(updatedAt = System.currentTimeMillis())
        db.sessionDao().upsert(updated.toEntity())
        val thumbW = 540
        val thumbH = ((viewHeight.toFloat() / viewWidth.coerceAtLeast(1)) * thumbW).toInt().coerceIn(320, 720)
        val bmp = Exporters.renderPage(
          session = updated,
          layers = _ui.value.layers,
          objects = _ui.value.objects,
          widthPx = thumbW,
          heightPx = thumbH,
          pdfBitmap = _ui.value.pdfBitmap
        )
        Exporters.saveThumbnail(app, sessionId, bmp)
        bmp.recycle()
      }
      _ui.update { it.copy(showSavedToast = true) }
      delay(1800)
      _ui.update { it.copy(showSavedToast = false) }
    }
  }

  fun exportCurrentPagePng(viewWidth: Int = 1080, viewHeight: Int = 1920) {
    val session = _ui.value.session ?: return
    viewModelScope.launch {
      val w = (viewWidth * 1.5f).toInt().coerceIn(1080, 2400)
      val h = (viewHeight * 1.5f).toInt().coerceIn(1080, 3200)
      val bmp = withContext(Dispatchers.Default) {
        Exporters.renderPage(session, _ui.value.layers, _ui.value.objects, w, h, _ui.value.pdfBitmap)
      }
      val uri = Exporters.exportPngToGallery(app, bmp, session.title)
      bmp.recycle()
      showMessage(if (uri != null) "Saved PNG to Pictures/${AppConfig.ALBUM_NAME}" else "PNG export failed")
    }
  }

  fun shareCurrentPagePng(viewWidth: Int = 1080, viewHeight: Int = 1920, onIntentReady: (Intent) -> Unit) {
    val session = _ui.value.session ?: return
    viewModelScope.launch {
      val w = (viewWidth * 1.5f).toInt().coerceIn(1080, 2160)
      val h = (viewHeight * 1.5f).toInt().coerceIn(1080, 2880)
      val bmp = withContext(Dispatchers.Default) {
        Exporters.renderPage(session, _ui.value.layers, _ui.value.objects, w, h, _ui.value.pdfBitmap)
      }
      val intent = Exporters.shareBitmapIntent(app, bmp, "board_${session.id}_p${_ui.value.pageIndex + 1}", "Share whiteboard")
      bmp.recycle()
      if (intent != null) onIntentReady(intent) else showMessage("Share failed")
    }
  }

  fun exportPdfPages(pageIndices: List<Int>, shareAfter: Boolean, onShareReady: (Intent) -> Unit = {}) {
    val session = _ui.value.session ?: return
    if (pageIndices.isEmpty()) return
    viewModelScope.launch {
      showMessage("Exporting PDF (${pageIndices.size} pages)…")
      val file = Exporters.exportPdf(app, db, session, pageIndices, saveToDownloads = true)
      if (file == null) {
        showMessage("PDF export failed")
      } else if (shareAfter) {
        onShareReady(Exporters.shareFileIntent(app, file, "application/pdf", "Share PDF"))
      } else {
        showMessage("Saved PDF to Downloads/${AppConfig.ALBUM_NAME}")
      }
    }
  }

  private suspend fun touchSession() {
    val s = _ui.value.session ?: return
    db.sessionDao().upsert(s.copy(updatedAt = System.currentTimeMillis()).toEntity())
  }

  fun showMessage(msg: String) = _ui.update { it.copy(snackbarMessage = msg) }
  fun clearMessage() = _ui.update { it.copy(snackbarMessage = null) }

  companion object {
    fun factory(app: TeachBoardApp, sessionId: String): ViewModelProvider.Factory =
      object : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
          return CanvasViewModel(app, sessionId) as T
        }
      }
  }
}
