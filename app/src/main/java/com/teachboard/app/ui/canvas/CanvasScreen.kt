package com.teachboard.app.ui.canvas

import android.Manifest
import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.RectF
import android.media.projection.MediaProjectionManager
import android.net.Uri
import android.os.Build
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Redo
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.BookmarkBorder
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Gesture
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.ImageSearch
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import kotlinx.coroutines.delay
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageContractOptions
import com.canhub.cropper.CropImageOptions
import com.teachboard.app.R
import com.teachboard.app.domain.export.Exporters
import com.teachboard.app.domain.model.CanvasObject
import com.teachboard.app.domain.model.FaceCamShape
import com.teachboard.app.domain.model.RecordingQuality
import com.teachboard.app.domain.model.Serialization
import com.teachboard.app.domain.model.StrokePoint
import com.teachboard.app.domain.model.ToolType
import com.teachboard.app.domain.model.ToolbarAction
import com.teachboard.app.ui.canvas.geometry.GeoStampKind
import com.teachboard.app.domain.ocr.OcrOutput
import com.teachboard.app.domain.ocr.OcrScript
import com.teachboard.app.domain.stickers.StickerGroup
import com.teachboard.app.ui.theme.AppConfig
import com.teachboard.app.ui.theme.TeachColors
import com.teachboard.app.util.RecorderController
import com.teachboard.app.util.ScreenRecorderService
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private enum class ActiveDialog {
  NONE,
  BOARD_COLOR,
  BRUSH,
  SHAPES,
  ERASER,
  SMART_QUICK,
  SMART_WRITING_LANGS,
  TEXT,
  FORMULA,
  TABLE_POPOVER,
  TABLE_EDIT,
  LAYERS,
  PAGES,
  OCR,
  WEB_IMAGE,
  STICKERS_GENERAL,
  STICKERS_SCIENCE,
  LASER,
  GEOMETRY,
  RECORD,
  SHARE_EXPORT,
  SHORTCUTS,
  CUSTOMIZE_TOOLBAR,
  CLEAR_CONFIRM
}

@Composable
fun CanvasScreen(
  viewModel: CanvasViewModel,
  onNavigateHome: () -> Unit,
  onOpenHelp: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val ui by viewModel.ui.collectAsState()
  val loadedPrefs by viewModel.loadedUserPrefs.collectAsState()
  val prefs = loadedPrefs ?: com.teachboard.app.data.UserPrefs()
  val inkModelState by viewModel.inkModelState.collectAsState()
  val downloadedInkTags by viewModel.downloadedInkTags.collectAsState()
  val recentOcrScans by viewModel.recentOcrScans.collectAsState()
  val recordingState by RecorderController.state.collectAsState()

  var activeDialog by remember { mutableStateOf(ActiveDialog.NONE) }
  var toolbarVisible by remember { mutableStateOf(true) }
  var pagePillExpanded by remember { mutableStateOf(true) }
  var canvasViewRef by remember { mutableStateOf<TeachCanvasView?>(null) }
  var selectionScreenBounds by remember { mutableStateOf<RectF?>(null) }
  var toastMessage by remember { mutableStateOf<String?>(null) }
  var showHoldGuide by remember { mutableStateOf(false) }
  var showSmartShapesGuide by remember { mutableStateOf(false) }
  var showRecolorPalette by remember { mutableStateOf(false) }
  var sideDockExpanded by remember { mutableStateOf(true) }
  var sideDockOnLeft by remember { mutableStateOf(false) }

  LaunchedEffect(ui.selectedUids) {
    showRecolorPalette = false
  }

  var placeWorldX by remember { mutableFloatStateOf(180f) }
  var placeWorldY by remember { mutableFloatStateOf(240f) }
  var editingObject by remember { mutableStateOf<CanvasObject?>(null) }

  var ocrScript by remember { mutableStateOf(OcrScript.AUTO) }
  var ocrResult by remember { mutableStateOf<OcrOutput?>(null) }
  var ocrProcessing by remember { mutableStateOf(false) }

  var pendingCameraUri by remember { mutableStateOf<Uri?>(null) }
  var pendingRecQuality by remember { mutableStateOf(RecordingQuality.P1080) }
  var pendingRecAudio by remember { mutableStateOf(true) }
  var pendingRecFaceCam by remember { mutableStateOf(false) }
  var pendingRecCamShape by remember { mutableStateOf(FaceCamShape.ROUND) }

  // True full-screen immersive canvas (hides system bars while drawing, restores cleanly on exit!)
  DisposableEffect(context) {
    val window = (context as? Activity)?.window
    val controller = window?.let { WindowCompat.getInsetsController(it, it.decorView) }
    controller?.apply {
      systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
      hide(WindowInsetsCompat.Type.systemBars())
    }
    onDispose {
      controller?.show(WindowInsetsCompat.Type.systemBars())
      if (prefs.autoSaveEnabled) {
        viewModel.saveBoardNow(canvasViewRef?.width ?: 1080, canvasViewRef?.height ?: 1920)
      }
    }
  }

  // Re-enforce immersive full-screen bars whenever dialogs/tour dismiss so no gesture bar lingers
  LaunchedEffect(activeDialog, prefs.tourVersionCompleted) {
    if (activeDialog == ActiveDialog.NONE) {
      val window = (context as? Activity)?.window
      window?.let {
        WindowCompat.getInsetsController(it, it.decorView).apply {
          systemBarsBehavior = WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
          hide(WindowInsetsCompat.Type.systemBars())
        }
      }
    }
  }

  // Auto-collapse left-edge page pill after 2.2s on initial open (also collapses immediately on first touch!)
  LaunchedEffect(Unit) {
    delay(2200L)
    pagePillExpanded = false
  }

  // Trigger one-time "Hold to move" animated guide the first time an object is selected
  LaunchedEffect(ui.selectedUids, loadedPrefs) {
    if (loadedPrefs != null && ui.selectedUids.isNotEmpty() && "hold_to_move" !in prefs.hintsDismissed && prefs.tourVersionCompleted >= AppConfig.CURRENT_TOUR_VERSION) {
      showHoldGuide = true
    }
  }

  LaunchedEffect(ui.snackbarMessage) {
    val msg = ui.snackbarMessage ?: return@LaunchedEffect
    toastMessage = msg
    delay(2200L)
    if (toastMessage == msg) toastMessage = null
    viewModel.clearMessage()
  }
  LaunchedEffect(recordingState.errorMessage) {
    val err = recordingState.errorMessage ?: return@LaunchedEffect
    toastMessage = err
    delay(2600L)
    if (toastMessage == err) toastMessage = null
    RecorderController.clearError()
  }
  LaunchedEffect(recordingState.lastSavedUri) {
    if (recordingState.lastSavedUri != null && !recordingState.isRecording) {
      viewModel.showMessage("Recording saved to Movies/${AppConfig.ALBUM_NAME}")
    }
  }

  // ===== Activity Result Launchers =====

  val imagePicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    uri?.let { viewModel.addImageFromUri(it) }
  }
  val videoPicker = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
    uri?.let { viewModel.setFloatingVideoUri(it) }
  }
  val pdfPicker = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    uri?.let { viewModel.importPdf(it) }
  }
  val cameraPictureLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicture()) { ok ->
    val uri = pendingCameraUri
    if (ok && uri != null) viewModel.addImageFromUri(uri)
    pendingCameraUri = null
  }
  val cameraPermissionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    if (granted) {
      val (_, uri) = Exporters.createCameraUri(context)
      pendingCameraUri = uri
      cameraPictureLauncher.launch(uri)
    } else viewModel.showMessage("Camera permission denied")
  }
  val ocrCropLauncher = rememberLauncherForActivityResult(CropImageContract()) { result ->
    if (result.isSuccessful) {
      val uri = result.uriContent ?: return@rememberLauncherForActivityResult
      scope.launch {
        ocrProcessing = true
        val bmp = Exporters.decodeAndResizeImage(context, uri, maxSide = 1920)
        if (bmp != null) {
          ocrResult = viewModel.runOcr(bmp, ocrScript)
          bmp.recycle()
        } else viewModel.showMessage("Could not read cropped image")
        ocrProcessing = false
      }
    }
  }
  val ocrCameraPermLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
    if (granted) {
      val opts = CropImageOptions().apply {
        imageSourceIncludeCamera = true
        imageSourceIncludeGallery = false
      }
      ocrCropLauncher.launch(CropImageContractOptions(null, opts))
    } else viewModel.showMessage("Camera permission denied")
  }
  val projectionLauncher = rememberLauncherForActivityResult(ActivityResultContracts.StartActivityForResult()) { res ->
    val data = res.data
    if (res.resultCode == Activity.RESULT_OK && data != null) {
      val intent = Intent(context, ScreenRecorderService::class.java).apply {
        action = ScreenRecorderService.ACTION_START
        putExtra(ScreenRecorderService.EXTRA_RESULT_CODE, res.resultCode)
        putExtra(ScreenRecorderService.EXTRA_RESULT_DATA, data)
        putExtra(ScreenRecorderService.EXTRA_RECORD_AUDIO, pendingRecAudio)
        putExtra(ScreenRecorderService.EXTRA_QUALITY, pendingRecQuality.name)
      }
      ContextCompat.startForegroundService(context, intent)
      if (pendingRecFaceCam) viewModel.setFaceCam(true, pendingRecCamShape)
    }
  }
  val recordPermissionsLauncher = rememberLauncherForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
    val mpm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
    projectionLauncher.launch(mpm.createScreenCaptureIntent())
  }

  fun launchCameraToBoard() {
    if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
      val (_, uri) = Exporters.createCameraUri(context)
      pendingCameraUri = uri
      cameraPictureLauncher.launch(uri)
    } else cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
  }

  fun handleToolbarAction(action: ToolbarAction) {
    when (action) {
      ToolbarAction.SAVE -> viewModel.saveBoardNow(canvasViewRef?.width ?: 1080, canvasViewRef?.height ?: 1920)
      ToolbarAction.SHARE -> activeDialog = ActiveDialog.SHARE_EXPORT
      ToolbarAction.UNDO -> viewModel.undo()
      ToolbarAction.REDO -> viewModel.redo()
      ToolbarAction.CLEAR -> activeDialog = ActiveDialog.CLEAR_CONFIRM
      ToolbarAction.PEN -> {
        if (ui.activeTool.isBrush) activeDialog = ActiveDialog.BRUSH
        else viewModel.selectTool(ui.lastBrushTool)
      }
      ToolbarAction.SMART -> activeDialog = ActiveDialog.SMART_QUICK
      ToolbarAction.SHAPES -> {
        if (ui.activeTool.isShape) activeDialog = ActiveDialog.SHAPES
        else {
          viewModel.selectTool(ui.lastShapeTool)
          activeDialog = ActiveDialog.SHAPES
        }
      }
      ToolbarAction.ERASER -> {
        if (ui.activeTool == ToolType.Eraser) activeDialog = ActiveDialog.ERASER
        else viewModel.selectTool(ToolType.Eraser)
      }
      ToolbarAction.BOARD -> activeDialog = ActiveDialog.BOARD_COLOR
      ToolbarAction.TEXT -> {
        editingObject = null
        val vw = (canvasViewRef?.width ?: 1080).toFloat()
        val vh = (canvasViewRef?.height ?: 1920).toFloat()
        placeWorldX = (vw * 0.32f - ui.panX) / ui.zoom
        placeWorldY = (vh * 0.42f - ui.panY) / ui.zoom
        activeDialog = ActiveDialog.TEXT
      }
      ToolbarAction.FORMULA -> {
        editingObject = null
        val vw = (canvasViewRef?.width ?: 1080).toFloat()
        val vh = (canvasViewRef?.height ?: 1920).toFloat()
        placeWorldX = (vw * 0.28f - ui.panX) / ui.zoom
        placeWorldY = (vh * 0.42f - ui.panY) / ui.zoom
        activeDialog = ActiveDialog.FORMULA
      }
      ToolbarAction.TABLE -> {
        editingObject = null
        val vw = (canvasViewRef?.width ?: 1080).toFloat()
        val vh = (canvasViewRef?.height ?: 1920).toFloat()
        placeWorldX = (vw * 0.22f - ui.panX) / ui.zoom
        placeWorldY = (vh * 0.36f - ui.panY) / ui.zoom
        activeDialog = ActiveDialog.TABLE_POPOVER
      }
      ToolbarAction.LAYERS -> activeDialog = ActiveDialog.LAYERS
      ToolbarAction.IMAGE -> imagePicker.launch("image/*")
      ToolbarAction.VIDEO -> videoPicker.launch("video/*")
      ToolbarAction.WEB_IMAGE -> activeDialog = ActiveDialog.WEB_IMAGE
      ToolbarAction.CAMERA -> launchCameraToBoard()
      ToolbarAction.OCR -> activeDialog = ActiveDialog.OCR
      ToolbarAction.IMPORT_PDF -> pdfPicker.launch(arrayOf("application/pdf"))
      ToolbarAction.STICKERS -> activeDialog = ActiveDialog.STICKERS_GENERAL
      ToolbarAction.SCIENCE -> activeDialog = ActiveDialog.STICKERS_SCIENCE
      ToolbarAction.LASER -> {
        if (ui.activeTool == ToolType.Laser) activeDialog = ActiveDialog.LASER
        else viewModel.selectTool(ToolType.Laser)
      }
      ToolbarAction.GEOMETRY -> activeDialog = ActiveDialog.GEOMETRY
      ToolbarAction.CALCULATOR -> viewModel.setShowCalculator(!ui.showCalculator)
      ToolbarAction.TIMER -> viewModel.setShowTimer(!ui.showTimer)
      ToolbarAction.RECORD -> {
        if (recordingState.isRecording) {
          context.startService(Intent(context, ScreenRecorderService::class.java).apply { this.action = ScreenRecorderService.ACTION_STOP })
          viewModel.setFaceCam(false)
        } else activeDialog = ActiveDialog.RECORD
      }
      ToolbarAction.LASSO -> viewModel.selectTool(ToolType.Lasso)
      ToolbarAction.HAND -> viewModel.selectTool(ToolType.Hand)
      ToolbarAction.CUSTOMIZE -> activeDialog = ActiveDialog.CUSTOMIZE_TOOLBAR
      ToolbarAction.HELP -> onOpenHelp()
    }
  }

  val callbacks = remember(viewModel) {
    object : CanvasCallbacks {
      override fun onCommitStroke(tool: ToolType, points: List<StrokePoint>, isEraser: Boolean) {
        viewModel.commitStroke(tool, points, isEraser)
      }
      override fun onCommitShape(tool: ToolType, start: StrokePoint, end: StrokePoint) {
        viewModel.commitShape(tool, start, end)
      }
      override fun onStrokeEraseAt(x: Float, y: Float, radius: Float) {
        viewModel.strokeEraseAt(x, y, radius)
      }
      override fun onSmartInkStroke(points: List<StrokePoint>) {
        viewModel.addSmartInkStroke(points)
      }
      override fun onTapPlaceObject(tool: ToolType, worldX: Float, worldY: Float) {
        editingObject = null
        placeWorldX = worldX; placeWorldY = worldY
        activeDialog = when (tool) {
          ToolType.Text -> ActiveDialog.TEXT
          ToolType.Formula -> ActiveDialog.FORMULA
          ToolType.Table -> ActiveDialog.TABLE_POPOVER
          else -> ActiveDialog.NONE
        }
      }
      override fun onSelectionChanged(selectedUids: Set<String>) = viewModel.setSelection(selectedUids)
      override fun onMoveSelection(dx: Float, dy: Float) = viewModel.moveSelection(dx, dy)
      override fun onScaleSelection(anchorX: Float, anchorY: Float, factor: Float) = viewModel.scaleSelection(anchorX, anchorY, factor)
      override fun onScaleSelectionAxes(anchorX: Float, anchorY: Float, sx: Float, sy: Float) = viewModel.scaleSelectionAxes(anchorX, anchorY, sx, sy)
      override fun onRotateSelection(anchorX: Float, anchorY: Float, degrees: Float) = viewModel.rotateSelection(anchorX, anchorY, degrees)
      override fun onFlipSelectionHorizontal() = viewModel.flipSelectionHorizontal()
      override fun onDoubleTapObject(obj: CanvasObject) {
        editingObject = obj
        val origin = obj.points.firstOrNull()
        if (origin != null) { placeWorldX = origin.x; placeWorldY = origin.y }
        activeDialog = when (obj.type) {
          ToolType.Text -> ActiveDialog.TEXT
          ToolType.Formula -> ActiveDialog.FORMULA
          ToolType.Table -> ActiveDialog.TABLE_EDIT
          else -> ActiveDialog.NONE
        }
      }
      override fun onViewportChanged(scale: Float, panX: Float, panY: Float) = viewModel.updateViewport(scale, panX, panY)
      override fun onSelectionScreenBoundsChanged(bounds: RectF?) { selectionScreenBounds = bounds }
      override fun onCanvasTouched() { pagePillExpanded = false }
      override fun onStampLine(a: StrokePoint, b: StrokePoint) = viewModel.stampGeometryLine(a, b)
      override fun onStampArc(needle: StrokePoint, start: StrokePoint, end: StrokePoint) = viewModel.stampGeometryArc(needle, start, end)
      override fun onStampAngle(pivot: StrokePoint, armEnd: StrokePoint, baseEnd: StrokePoint, kind: GeoStampKind) = viewModel.stampGeometryAngle(pivot, armEnd, baseEnd, kind)
      override fun onDismissGeometry() = viewModel.setGeometryTool(null)
    }
  }

  val session = ui.session
  val totalPages = session?.pageCount ?: 1
  val isPdfMode = !session?.pdfPath.isNullOrBlank()
  val isCurrentPageBookmarked = session?.pdfBookmarks?.contains(ui.pageIndex) == true
  val isCurrentPageHidden = session?.pdfHiddenPages?.contains(ui.pageIndex) == true

  Box(modifier = Modifier.fillMaxSize()) {
    // 1. Full-bleed Canvas
    CanvasBoard(
      modifier = Modifier.fillMaxSize(),
      backgroundColor = session?.backgroundColor ?: TeachColors.BoardWhite,
      backgroundStyle = session?.backgroundStyle ?: com.teachboard.app.domain.model.BackgroundStyle.PLAIN,
      pdfBitmap = if (isCurrentPageHidden) null else ui.pdfBitmap,
      layers = ui.layers,
      objects = ui.objects,
      activeTool = ui.activeTool,
      eraserMode = ui.eraserMode,
      brushColor = ui.brushColor,
      fillColor = ui.fillColor,
      isFilled = ui.isFilled,
      strokeWidth = ui.strokeWidth,
      alpha = ui.alpha,
      laserColor = ui.laserColor,
      laserSize = ui.laserSize,
      palmRejection = prefs.palmRejection,
      stylusOnly = prefs.stylusOnly,
      zoom = ui.zoom,
      panX = ui.panX,
      panY = ui.panY,
      selectedUids = ui.selectedUids,
      lockedObjectUids = ui.lockedObjectUids,
      smartInkPreviewStrokes = ui.smartInkStrokes,
      activeGeometryTool = ui.activeGeometryTool,
      callbacks = callbacks,
      onViewReady = { canvasViewRef = it }
    )

    // 2. Top-Left Back Circle + Left-Edge Collapsible Vertical Page Pill (with spring animation!)
    Column(
      modifier = Modifier
        .align(Alignment.TopStart)
        .statusBarsPadding()
        .padding(start = 12.dp, top = 8.dp)
        .animateContentSize(spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium)),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      IconButton(
        onClick = {
          viewModel.saveBoardNow(canvasViewRef?.width ?: 1080, canvasViewRef?.height ?: 1920)
          onNavigateHome()
        },
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(Color(0xB85F656D))
      ) {
        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White, modifier = Modifier.size(22.dp))
      }

      if (!pagePillExpanded) {
        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0x999AA0A8),
          modifier = Modifier
            .size(42.dp)
            .clickable { pagePillExpanded = true }
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text("${ui.pageIndex + 1}/$totalPages", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }
        }
      } else {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = Color(0xE633363A),
          shadowElevation = 6.dp
        ) {
          Column(
            modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            IconButton(
              onClick = { viewModel.goToPage(ui.pageIndex - 1) },
              enabled = ui.pageIndex > 0,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Prev", tint = if (ui.pageIndex > 0) Color.White else Color(0x66FFFFFF), modifier = Modifier.size(18.dp))
            }
            Text(
              "${ui.pageIndex + 1}/$totalPages",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              modifier = Modifier.clickable { pagePillExpanded = false }.padding(vertical = 2.dp)
            )
            IconButton(
              onClick = { viewModel.goToPage(ui.pageIndex + 1) },
              enabled = ui.pageIndex < totalPages - 1,
              modifier = Modifier.size(32.dp)
            ) {
              Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Next", tint = if (ui.pageIndex < totalPages - 1) Color.White else Color(0x66FFFFFF), modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { viewModel.addPage() }, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.Add, contentDescription = "Add Page", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { pagePillExpanded = false; activeDialog = ActiveDialog.PAGES }, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.GridView, contentDescription = "All Pages", tint = Color(0xFF3B82F6), modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }

    // 3. Top-Center Status (Recording Pill, PDF Controls, Zoom Pill only when zoomed, Saved Toast)
    Column(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .statusBarsPadding()
        .padding(top = 16.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
      if (recordingState.isRecording) {
        RecordingFloatingPill(
          elapsedSeconds = recordingState.elapsedSeconds,
          isPaused = recordingState.isPaused,
          onTogglePause = {
            context.startService(Intent(context, ScreenRecorderService::class.java).apply { action = ScreenRecorderService.ACTION_TOGGLE_PAUSE })
          },
          onStop = {
            context.startService(Intent(context, ScreenRecorderService::class.java).apply { action = ScreenRecorderService.ACTION_STOP })
            viewModel.setFaceCam(false)
          }
        )
      } else if (isPdfMode) {
        Surface(shape = RoundedCornerShape(22.dp), color = TeachColors.DarkPillBg) {
          Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = { viewModel.togglePdfBookmark() }, modifier = Modifier.size(34.dp)) {
              Icon(if (isCurrentPageBookmarked) Icons.Default.Bookmark else Icons.Default.BookmarkBorder, contentDescription = "Bookmark", tint = if (isCurrentPageBookmarked) TeachColors.AccentOrange else Color.White, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { viewModel.togglePdfHideCurrentPage() }, modifier = Modifier.size(34.dp)) {
              Icon(if (isCurrentPageHidden) Icons.Default.VisibilityOff else Icons.Default.Visibility, contentDescription = "Hide PDF", tint = Color.White, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { activeDialog = ActiveDialog.SHARE_EXPORT }, modifier = Modifier.size(34.dp)) {
              Icon(Icons.Default.PictureAsPdf, contentDescription = "Export PDF", tint = Color.White, modifier = Modifier.size(18.dp))
            }
          }
        }
      }

      if (abs(ui.zoom - 1f) > 0.03f) {
        Surface(
          shape = RoundedCornerShape(18.dp),
          color = TeachColors.DarkPillBg,
          modifier = Modifier.clickable { viewModel.resetZoom() }
        ) {
          Text(
            "${(ui.zoom * 100).roundToInt()}%  ×",
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp)
          )
        }
      }

      AnimatedVisibility(
        visible = toastMessage != null,
        enter = fadeIn() + slideInVertically { -it / 2 },
        exit = fadeOut() + slideOutVertically { -it / 2 }
      ) {
        toastMessage?.let { msg ->
          Surface(
            shape = RoundedCornerShape(22.dp),
            color = Color(0xEB1E2024),
            shadowElevation = 6.dp,
            modifier = Modifier.clickable { toastMessage = null }
          ) {
            Text(
              text = msg,
              color = Color.White,
              fontSize = 13.sp,
              fontWeight = FontWeight.Medium,
              modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
          }
        }
      }
    }

    // 4. Top-Right Palm Rejection Toggle Circle + Subtle "Saved ✓" Pill
    Column(
      modifier = Modifier
        .align(Alignment.TopEnd)
        .statusBarsPadding()
        .padding(end = 12.dp, top = 8.dp),
      horizontalAlignment = Alignment.End,
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      IconButton(
        onClick = { viewModel.toggleStylusOnly() },
        modifier = Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(if (prefs.stylusOnly) Color(0xFF2B7FFF) else Color(0xB85F656D))
      ) {
        Icon(Icons.Default.Edit, contentDescription = "Palm Rejection", tint = Color.White, modifier = Modifier.size(22.dp))
      }

      AnimatedVisibility(visible = ui.showSavedToast, enter = fadeIn(), exit = fadeOut()) {
        Surface(shape = RoundedCornerShape(20.dp), color = Color(0xE6E7F9ED), shadowElevation = 2.dp) {
          Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically) {
            Text(stringResource(R.string.saved_toast), color = Color(0xFF16A34A), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            Spacer(Modifier.width(3.dp))
            Icon(Icons.Default.Check, contentDescription = null, tint = Color(0xFF16A34A), modifier = Modifier.size(14.dp))
          }
        }
      }
    }

    // 4a. Permanent Side Hand & Quick-Shortcut Dock (right edge by default, swappable to left edge!)
    if (ui.activeGeometryTool == null) {
      val handActive = ui.activeTool == ToolType.Hand
      val extraShortcuts = remember(prefs.shortcutTools) {
        prefs.shortcutTools.filter { it != ToolbarAction.HAND }
      }
      Column(
        modifier = Modifier
          .align(if (sideDockOnLeft) Alignment.TopStart else Alignment.TopEnd)
          .statusBarsPadding()
          .padding(
            start = if (sideDockOnLeft) 12.dp else 0.dp,
            end = if (sideDockOnLeft) 0.dp else 12.dp,
            top = if (sideDockOnLeft) 130.dp else 60.dp
          ),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
      // Collapse/Expand chevron square button
      Surface(
        shape = RoundedCornerShape(13.dp),
        color = Color(0xFF5D739C),
        shadowElevation = 3.dp,
        modifier = Modifier
          .size(42.dp)
          .clickable { sideDockExpanded = !sideDockExpanded }
      ) {
        Box(contentAlignment = Alignment.Center) {
          Icon(
            imageVector = if (sideDockExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
            contentDescription = if (sideDockExpanded) "Collapse side dock" else "Expand side dock",
            tint = Color.White,
            modifier = Modifier.size(22.dp)
          )
        }
      }

      // Vertical frosted pill with PERMANENT Hand button + extra shortcuts + utility buttons
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color(0xF5F7F8FC),
        shadowElevation = 4.dp,
        modifier = Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(18.dp))
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 4.dp, vertical = 5.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          // PERMANENT Hand / Pan button (1-tap toggle between Hand & previous Brush!)
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(RoundedCornerShape(11.dp))
              .background(if (handActive) Color(0xFF2B6CF6) else Color(0xFFDCE3F2))
              .clickable {
                if (handActive) viewModel.selectTool(ui.lastBrushTool)
                else viewModel.selectTool(ToolType.Hand)
              },
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.PanTool,
              contentDescription = "Hand",
              tint = if (handActive) Color.White else Color(0xFF334E80),
              modifier = Modifier.size(21.dp)
            )
          }

          AnimatedVisibility(visible = sideDockExpanded) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              // User's pinned custom shortcuts (if any)
              extraShortcuts.forEach { action ->
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(Color(0xFFE8ECF6))
                    .clickable { handleToolbarAction(action) },
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    toolbarIconFor(action),
                    contentDescription = action.name,
                    tint = Color(0xFF334E80),
                    modifier = Modifier.size(18.dp)
                  )
                }
              }

              // Subtle divider
              Box(
                modifier = Modifier
                  .width(24.dp)
                  .height(1.dp)
                  .background(Color(0xFFCBD5E1))
              )

              // Swap side (Right <-> Left)
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFECEAF8))
                  .clickable { sideDockOnLeft = !sideDockOnLeft },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.SwapHoriz,
                  contentDescription = "Swap side",
                  tint = Color(0xFF5B4B9E),
                  modifier = Modifier.size(18.dp)
                )
              }

              // Add / Edit shortcuts (+)
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFE8ECF6))
                  .clickable { activeDialog = ActiveDialog.SHORTCUTS },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Add,
                  contentDescription = "Shortcuts",
                  tint = Color(0xFF334E80),
                  modifier = Modifier.size(18.dp)
                )
              }

              // Toggle bottom toolbar visibility (Eye / Eye-off)
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFF1F2F6))
                  .clickable { toolbarVisible = !toolbarVisible },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  if (toolbarVisible) Icons.Default.VisibilityOff else Icons.Default.Visibility,
                  contentDescription = if (toolbarVisible) "Hide toolbar" else "Show toolbar",
                  tint = Color(0xFF64748B),
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }
      }
    }
  }

    // 4b. Floating Object-Tracked Selection Action Buttons (Directly ABOVE Selected Object!)
    val firstSelectedObj = remember(ui.selectedUids, ui.objects) {
      ui.objects.firstOrNull { it.objectUid in ui.selectedUids }
    }
    val isSelectionLocked = ui.selectedUids.isNotEmpty() && ui.selectedUids.all { it in ui.lockedObjectUids }
    val showEditBtn = firstSelectedObj != null &&
      (firstSelectedObj.type == ToolType.Text || firstSelectedObj.type == ToolType.Formula || firstSelectedObj.type == ToolType.Table)
    val selBox = selectionScreenBounds
    if (ui.selectedUids.isNotEmpty() && selBox != null) {
      val density = LocalDensity.current
      val btnCount = 4 + (if (showEditBtn) 1 else 0)
      val baseBarDp = (btnCount * 40 + (btnCount - 1) * 8).toFloat()
      val barWidthPx = with(density) { (if (showRecolorPalette) maxOf(baseBarDp, 308f) else baseBarDp).dp.toPx() }
      val barHeightPx = with(density) { 42.dp.toPx() }
      val gapPx = with(density) { 12.dp.toPx() }
      val vw = (canvasViewRef?.width ?: 1080).toFloat()
      val vh = (canvasViewRef?.height ?: 1920).toFloat()
      val desiredX = (selBox.centerX() - barWidthPx / 2f).coerceIn(16f, (vw - barWidthPx - 16f).coerceAtLeast(16f))
      val topMin = if (vh < 1300f) 64f else 110f
      val bottomReserve = if (vh < 1300f) 185f else 280f
      val desiredY = (selBox.top - barHeightPx - gapPx).coerceIn(topMin, (vh - bottomReserve).coerceAtLeast(topMin))

      Column(
        modifier = Modifier
          .offset { IntOffset(desiredX.roundToInt(), desiredY.roundToInt()) },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Row(
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          if (showEditBtn) {
            val editObj = firstSelectedObj
            FloatingActionCircle(
              bg = Color(0xFF2B6CF6),
              icon = Icons.Default.Edit,
              contentDescription = "Edit"
            ) {
              showRecolorPalette = false
              editingObject = editObj
              editObj.points.firstOrNull()?.let { placeWorldX = it.x; placeWorldY = it.y }
              activeDialog = when (editObj.type) {
                ToolType.Text -> ActiveDialog.TEXT
                ToolType.Formula -> ActiveDialog.FORMULA
                else -> ActiveDialog.TABLE_EDIT
              }
            }
          }
          FloatingActionCircle(bg = Color(0xFF2B6CF6), icon = Icons.Default.ContentCopy, contentDescription = "Copy") {
            showRecolorPalette = false
            viewModel.duplicateSelection()
          }
          FloatingActionCircle(
            bg = if (isSelectionLocked) Color(0xFF475569) else Color(0xFF2B6CF6),
            icon = if (isSelectionLocked) Icons.Default.Lock else Icons.Default.LockOpen,
            contentDescription = "Lock"
          ) {
            showRecolorPalette = false
            viewModel.toggleLockSelection()
          }
          FloatingActionCircle(
            bg = if (showRecolorPalette) Color(0xFF1D4ED8) else Color(0xFF2B6CF6),
            icon = Icons.Default.Palette,
            contentDescription = "Recolor"
          ) {
            showRecolorPalette = !showRecolorPalette
          }
          FloatingActionCircle(bg = Color(0xFFEF4444), icon = Icons.Default.Close, contentDescription = "Delete") {
            showRecolorPalette = false
            viewModel.deleteSelection()
          }
        }

        // Sleek Floating Recolor Swatch Pill (pops right below the Palette button!)
        AnimatedVisibility(
          visible = showRecolorPalette,
          enter = fadeIn() + scaleIn(initialScale = 0.90f),
          exit = fadeOut() + scaleOut(targetScale = 0.90f)
        ) {
          val activeColor = firstSelectedObj?.color ?: ui.brushColor
          Surface(
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 10.dp,
            modifier = Modifier.border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(24.dp))
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 7.dp),
              horizontalArrangement = Arrangement.spacedBy(8.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              TeachColors.BrushSwatches.forEach { swatch ->
                val isSel = activeColor == swatch
                val isWhite = swatch == 0xFFFFFFFF.toInt()
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(Color(swatch))
                    .border(
                      width = if (isSel) 2.5.dp else if (isWhite) 1.dp else 0.dp,
                      color = if (isSel) Color(0xFF2B6CF6) else Color(0xFFCBD5E1),
                      shape = CircleShape
                    )
                    .clickable {
                      viewModel.recolorSelection(swatch)
                      showRecolorPalette = false
                    },
                  contentAlignment = Alignment.Center
                ) {
                  if (isSel) {
                    Icon(
                      Icons.Default.Check,
                      contentDescription = null,
                      tint = if (TeachColors.isDarkColor(swatch)) Color.White else Color(0xFF111827),
                      modifier = Modifier.size(16.dp)
                    )
                  }
                }
              }
            }
          }
        }
      }
    }

    // 5. Draggable Overlays
    if (ui.showCalculator) {
      CalculatorOverlay(
        onPutOnBoard = { txt ->
          viewModel.addTextObject(Serialization.RichTextMeta(txt, bold = true))
          viewModel.setShowCalculator(false)
        },
        onClose = { viewModel.setShowCalculator(false) }
      )
    }
    if (ui.showTimer) TimerOverlay(onClose = { viewModel.setShowTimer(false) })
    ui.floatingVideoUri?.let { uri -> FloatingVideoOverlay(uri) { viewModel.setFloatingVideoUri(null) } }
    if (ui.showFaceCam) FaceCamBubble(ui.faceCamShape) { viewModel.setFaceCam(false) }

    // 6. Bottom Stack: Smart Shapes Offer / Smart Writing Status / Selection Bar / PDF Quick Bar / Frosted Toolbar
    Column(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      // Smart Shapes Visual Preview Capsule (Snapped Geometric Miniature vs Original Freehand Amber Stroke!)
      AnimatedVisibility(
        visible = ui.smartShapeOffer != null,
        enter = fadeIn() + scaleIn(initialScale = 0.90f) + slideInVertically { it / 2 },
        exit = fadeOut() + scaleOut(targetScale = 0.90f)
      ) {
        ui.smartShapeOffer?.let { offer ->
          Surface(
            shape = RoundedCornerShape(18.dp),
            color = Color(0xEB222428),
            shadowElevation = 8.dp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
          ) {
            Row(
              modifier = Modifier.padding(6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              offer.candidates.take(2).forEachIndexed { idx, cand ->
                val active = idx == 0
                Box(
                  modifier = Modifier
                    .size(width = 50.dp, height = 42.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (active) Color(0xFF2B6CF6) else Color(0xFF33363C))
                    .clickable { viewModel.acceptSmartShape(cand) }
                    .padding(7.dp)
                ) {
                  Canvas(modifier = Modifier.fillMaxSize()) {
                    drawMiniContours(cand.geometry, Color.White)
                  }
                }
              }
              Box(modifier = Modifier.width(1.dp).height(26.dp).background(Color(0x44FFFFFF)))
              // Keep my original hand-drawn stroke miniature (warm amber)
              Box(
                modifier = Modifier
                  .size(width = 50.dp, height = 42.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color(0xFF33363C))
                  .clickable { viewModel.keepSmartShapeAsDrawn() }
                  .padding(7.dp)
              ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                  drawMiniContours(listOf(com.teachboard.app.domain.model.Contour(false, offer.originalStroke.points)), Color(0xFFE8B268))
                }
              }
            }
          }
        }
      }

      // Smart Writing active pill
      AnimatedVisibility(
        visible = ui.activeTool == ToolType.SmartWriting,
        enter = fadeIn() + slideInVertically { it / 2 },
        exit = fadeOut()
      ) {
        Surface(shape = RoundedCornerShape(20.dp), color = TeachColors.Primary, modifier = Modifier.padding(bottom = 6.dp)) {
          Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 5.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(
              if (ui.smartRecognizing) stringResource(R.string.recognizing) else "Smart Writing (${prefs.smartWritingLanguage})",
              color = Color.White,
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              modifier = Modifier.clickable { activeDialog = ActiveDialog.SMART_WRITING_LANGS }
            )
            if (ui.smartInkStrokes.isNotEmpty() && !ui.smartRecognizing) {
              Text(stringResource(R.string.convert_now), color = Color(0xFFFDE047), fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.clickable { viewModel.convertSmartInkNow() })
            }
          }
        }
      }

      // PDF Quick Annotation Bar
      if (isPdfMode) {
        Surface(shape = RoundedCornerShape(22.dp), color = TeachColors.DarkPillBg, modifier = Modifier.padding(bottom = 6.dp)) {
          Row(modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(onClick = { viewModel.selectTool(ToolType.Pen) }, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.Draw, contentDescription = "Pen", tint = if (ui.activeTool == ToolType.Pen) TeachColors.PrimaryDark else Color.White, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { viewModel.selectTool(ToolType.Highlighter) }, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.Highlight, contentDescription = "Highlighter", tint = if (ui.activeTool == ToolType.Highlighter) TeachColors.PrimaryDark else Color.White, modifier = Modifier.size(18.dp))
            }
            IconButton(onClick = { viewModel.selectTool(ToolType.Eraser) }, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.CleaningServices, contentDescription = "Eraser", tint = if (ui.activeTool == ToolType.Eraser) TeachColors.Danger else Color.White, modifier = Modifier.size(18.dp))
            }
            TeachColors.PdfQuickColors.forEach { c ->
              Box(modifier = Modifier.size(22.dp).clip(CircleShape).background(Color(c)).border(if (ui.brushColor == c) 2.dp else 0.dp, Color.White, CircleShape).clickable { viewModel.setBrushColor(c) })
            }
          }
        }
      }

      // Bottom Frosted Light Toolbar with Pinned "> Collapse" Button (rock-solid flush to bottom — zero jump/gap!)
      if (ui.activeGeometryTool == null) {
        if (toolbarVisible) {
          Surface(
            color = Color(0xFFF3F4F6),
            shape = RoundedCornerShape(topStart = 18.dp, topEnd = 18.dp),
            shadowElevation = 10.dp,
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Row(
                modifier = Modifier
                  .weight(1f)
                  .horizontalScroll(rememberScrollState())
                  .padding(start = 6.dp, end = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(2.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                val visibleActions = remember(prefs.toolbarOrder, prefs.hiddenToolbarTools) {
                  prefs.toolbarOrder.filter { it !in prefs.hiddenToolbarTools }
                }
                visibleActions.forEach { action ->
                  if (action == ToolbarAction.CUSTOMIZE) {
                    Box(modifier = Modifier.padding(horizontal = 4.dp).width(1.dp).height(34.dp).background(Color(0xFFD1D5DB)))
                  }
                  val isSelected = when (action) {
                    ToolbarAction.PEN -> ui.activeTool.isBrush
                    ToolbarAction.SMART -> ui.activeTool == ToolType.SmartWriting || ui.smartShapesEnabled
                    ToolbarAction.SHAPES -> ui.activeTool.isShape
                    ToolbarAction.ERASER -> ui.activeTool == ToolType.Eraser
                    ToolbarAction.LASER -> ui.activeTool == ToolType.Laser
                    ToolbarAction.LASSO -> ui.activeTool == ToolType.Lasso
                    ToolbarAction.HAND -> ui.activeTool == ToolType.Hand
                    ToolbarAction.CALCULATOR -> ui.showCalculator
                    ToolbarAction.TIMER -> ui.showTimer
                    ToolbarAction.RECORD -> recordingState.isRecording
                    ToolbarAction.GEOMETRY -> ui.activeGeometryTool != null
                    else -> false
                  }
                  val enabled = when (action) {
                    ToolbarAction.UNDO -> ui.canUndo
                    ToolbarAction.REDO -> ui.canRedo
                    else -> true
                  }
                  val dotColor: Color? = when (action) {
                    ToolbarAction.PEN -> Color(ui.brushColor)
                    ToolbarAction.LASER -> Color(ui.laserColor)
                    ToolbarAction.BOARD -> Color(session?.backgroundColor ?: TeachColors.BoardWhite)
                    else -> null
                  }
                  val penLabelOverride: String? = if (action == ToolbarAction.PEN) {
                    if (!ui.activeTool.isBrush) "Brushes"
                    else when (ui.activeTool) {
                      ToolType.Marker -> "Marker"
                      ToolType.Highlighter -> "Highlight"
                      ToolType.Brush -> "Brush"
                      ToolType.Calligraphy -> "Callig."
                      else -> "Pen"
                    }
                  } else null
                  ToolbarItemButton(
                    action = action,
                    selected = isSelected,
                    enabled = enabled,
                    colorDot = dotColor,
                    smartShapesMode = ui.smartShapesEnabled,
                    labelOverride = penLabelOverride,
                    onClick = { handleToolbarAction(action) }
                  )
                }
              }

              // Pinned right-side "> Collapse" card button
              Surface(
                modifier = Modifier
                  .padding(end = 6.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { toolbarVisible = false },
                color = Color(0xFFE0E3E8),
                shadowElevation = 2.dp
              ) {
                Column(
                  modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Collapse", tint = Color(0xFF1F2937), modifier = Modifier.size(22.dp))
                  Text(stringResource(R.string.collapse), fontSize = 10.sp, color = Color(0xFF1F2937), fontWeight = FontWeight.Medium)
                }
              }
            }
          }
        } else {
          // Compact expand pill on bottom-right edge when toolbar is collapsed
          Box(modifier = Modifier.fillMaxWidth().padding(end = 10.dp, bottom = 10.dp)) {
            Surface(
              modifier = Modifier
                .align(Alignment.BottomEnd)
                .clip(RoundedCornerShape(14.dp))
                .clickable { toolbarVisible = true },
              color = Color(0xE6E0E3E8),
              shadowElevation = 6.dp
            ) {
              Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = "Expand toolbar", tint = Color(0xFF1F2937), modifier = Modifier.padding(10.dp).size(24.dp))
            }
          }
        }
      } else {
        // Modal Geometry Mode: all other tools disabled until user dismisses instrument via ✕
        Surface(
          shape = RoundedCornerShape(22.dp),
          color = Color(0xF01E293B),
          shadowElevation = 8.dp,
          modifier = Modifier.padding(bottom = 14.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            Icon(
              Icons.Default.Straighten,
              contentDescription = null,
              tint = Color(0xFFFFCE54),
              modifier = Modifier.size(18.dp)
            )
            Text(
              text = "${ui.activeGeometryTool?.name?.replace('_', ' ')?.lowercase()?.replaceFirstChar { it.uppercase() } ?: "Ruler"} active • Tap ✕ on ruler to finish",
              color = Color.White,
              fontSize = 13.sp,
              fontWeight = FontWeight.SemiBold
            )
          }
        }
      }
    }
  }

  // ===== Active Dialogs =====

  when (activeDialog) {
    ActiveDialog.BOARD_COLOR -> BoardColorDialog(
      currentColor = session?.backgroundColor ?: TeachColors.BoardWhite,
      currentStyle = session?.backgroundStyle ?: com.teachboard.app.domain.model.BackgroundStyle.PLAIN,
      onApply = { c, s -> viewModel.updateBackground(c, s) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.BRUSH -> BrushDialog(
      currentTool = if (ui.activeTool.isBrush) ui.activeTool else ui.lastBrushTool,
      currentColor = ui.brushColor,
      currentSize = ui.strokeWidth,
      currentAlpha = ui.alpha,
      smartShapesEnabled = ui.smartShapesEnabled,
      onSelectBrush = { viewModel.selectTool(it) },
      onColorChange = { viewModel.setBrushColor(it) },
      onSizeChange = { viewModel.setStrokeWidth(it) },
      onAlphaChange = { viewModel.setAlpha(it) },
      onSmartShapesChange = { viewModel.setSmartShapesEnabled(it) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.SMART_QUICK -> SmartQuickPopup(
      selectedLanguageTag = prefs.smartWritingLanguage,
      isWritingActive = ui.activeTool == ToolType.SmartWriting,
      isSmartShapesActive = ui.smartShapesEnabled,
      onSelectWriting = { viewModel.selectTool(ToolType.SmartWriting) },
      onToggleSmartShapes = {
        val next = !ui.smartShapesEnabled
        viewModel.setSmartShapesEnabled(next)
        if (next) {
          viewModel.selectTool(ToolType.Pen)
          if ("smart_shapes_guide" !in prefs.hintsDismissed) showSmartShapesGuide = true
        }
        viewModel.showMessage(if (next) "Smart Shapes on" else "Smart Shapes off")
      },
      onOpenLanguagePicker = { activeDialog = ActiveDialog.SMART_WRITING_LANGS },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.SMART_WRITING_LANGS -> SmartWritingDialog(
      languages = viewModel.digitalInk.supportedLanguages,
      selectedTag = prefs.smartWritingLanguage,
      downloadedTags = downloadedInkTags,
      modelState = inkModelState,
      bufferedStrokeCount = ui.smartInkStrokes.size,
      onSelectLanguage = { viewModel.selectSmartWritingLanguage(it) },
      onConvertNow = { viewModel.convertSmartInkNow() },
      onClearBuffer = { viewModel.clearSmartInkBuffer() },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.SHAPES -> ShapesDialog(
      selectedShape = if (ui.activeTool.isShape) ui.activeTool else ui.lastShapeTool,
      isFilled = ui.isFilled,
      strokeColor = ui.brushColor,
      fillColor = ui.fillColor,
      alpha = ui.alpha,
      onSelectShape = { viewModel.selectTool(it) },
      onFillToggle = { viewModel.setIsFilled(it) },
      onFillColorChange = { viewModel.setFillColor(it) },
      onAlphaChange = { viewModel.setAlpha(it) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.ERASER -> EraserDialog(
      mode = ui.eraserMode,
      size = ui.strokeWidth,
      onModeChange = { viewModel.setEraserMode(it) },
      onSizeChange = { viewModel.setStrokeWidth(it) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.TEXT -> TextEditorDialog(
      initialMeta = Serialization.decodeRichText(editingObject?.textContent),
      initialColor = editingObject?.color ?: ui.brushColor,
      initialFontSize = editingObject?.fontSize ?: ui.fontSize,
      onConfirm = { meta, color, size ->
        viewModel.addTextObject(meta, placeWorldX, placeWorldY, color, size, editingObject?.objectUid)
      },
      onDismiss = { editingObject = null; activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.FORMULA -> FormulaDialog(
      initialLatex = editingObject?.textContent.orEmpty(),
      initialColor = editingObject?.color ?: ui.brushColor,
      initialSize = editingObject?.fontSize ?: 44f,
      onConfirm = { latex, color, size ->
        viewModel.addFormulaObject(latex, placeWorldX, placeWorldY, color, size, editingObject?.objectUid)
      },
      onDismiss = { editingObject = null; activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.TABLE_POPOVER -> TableGridPopover(
      onInsertTable = { r, c ->
        val emptyCells = List(r) { List(c) { "" } }
        viewModel.addTableObject(r, c, emptyCells, placeWorldX, placeWorldY, ui.brushColor)
      },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.TABLE_EDIT -> TableDialog(
      initialPayload = editingObject?.textContent,
      initialColor = editingObject?.color ?: ui.brushColor,
      onConfirm = { r, c, cells, color ->
        viewModel.addTableObject(
          rows = r,
          cols = c,
          cells = cells,
          x = placeWorldX,
          y = placeWorldY,
          borderColor = color,
          headerColor = color,
          replaceUid = editingObject?.objectUid
        )
      },
      onDismiss = { editingObject = null; activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.LAYERS -> LayersDialog(
      layers = ui.layers,
      activeLayerId = ui.activeLayerId,
      onSelect = { viewModel.selectLayer(it) },
      onAdd = { viewModel.addLayer() },
      onToggleVisible = { viewModel.toggleLayerVisible(it) },
      onToggleLock = { viewModel.toggleLayerLocked(it) },
      onReorder = { layer, up -> viewModel.reorderLayer(layer, up) },
      onClear = { viewModel.clearLayer(it) },
      onDelete = { viewModel.deleteLayer(it) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.PAGES -> PagesDialog(
      pageCount = totalPages,
      currentPage = ui.pageIndex,
      bookmarks = session?.pdfBookmarks ?: emptySet(),
      onSelectPage = { viewModel.goToPage(it) },
      onAddPage = { viewModel.addPage() },
      onDuplicatePage = { viewModel.duplicateCurrentPage() },
      onDeletePage = { viewModel.deletePage(it) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.OCR -> OcrDialog(
      selectedScript = ocrScript,
      onSelectScript = { ocrScript = it },
      ocrResult = ocrResult,
      isProcessing = ocrProcessing,
      recentScans = recentOcrScans,
      onTakePhoto = {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
          val opts = CropImageOptions().apply { imageSourceIncludeCamera = true; imageSourceIncludeGallery = false }
          ocrCropLauncher.launch(CropImageContractOptions(null, opts))
        } else ocrCameraPermLauncher.launch(Manifest.permission.CAMERA)
      },
      onChooseGallery = {
        val opts = CropImageOptions().apply { imageSourceIncludeCamera = false; imageSourceIncludeGallery = true }
        ocrCropLauncher.launch(CropImageContractOptions(null, opts))
      },
      onInsertText = { txt -> viewModel.addTextObject(Serialization.RichTextMeta(txt)) },
      onCopyText = { txt ->
        val cm = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        cm.setPrimaryClip(ClipData.newPlainText("OCR Text", txt))
        viewModel.showMessage("Copied to clipboard")
      },
      onShareText = { txt ->
        context.startActivity(Intent.createChooser(Intent(Intent.ACTION_SEND).apply { type = "text/plain"; putExtra(Intent.EXTRA_TEXT, txt) }, "Share text"))
      },
      onDeleteScan = { viewModel.deleteOcrScan(it) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.WEB_IMAGE -> WikimediaSearchDialog(
      onPickImage = { viewModel.addWikimediaImage(it) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.STICKERS_GENERAL -> StickerPickerDialog(
      group = StickerGroup.GENERAL,
      initialColor = ui.brushColor,
      onPick = { id, col -> viewModel.addStickerObject(id, col) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.STICKERS_SCIENCE -> StickerPickerDialog(
      group = StickerGroup.SCIENCE,
      initialColor = ui.brushColor,
      onPick = { id, col -> viewModel.addStickerObject(id, col) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.LASER -> LaserDialog(
      currentColor = ui.laserColor,
      currentSize = ui.laserSize,
      onColorChange = { viewModel.setLaserColor(it) },
      onSizeChange = { viewModel.setLaserSize(it) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.GEOMETRY -> GeometryPickerDialog(
      activeTool = ui.activeGeometryTool,
      onSelect = { viewModel.setGeometryTool(it) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.RECORD -> ScreenRecorderDialog(
      onStartRecording = { q, audio, faceCam, shape ->
        pendingRecQuality = q; pendingRecAudio = audio; pendingRecFaceCam = faceCam; pendingRecCamShape = shape
        val needed = mutableListOf<String>()
        if (audio) needed.add(Manifest.permission.RECORD_AUDIO)
        if (faceCam) needed.add(Manifest.permission.CAMERA)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) needed.add(Manifest.permission.POST_NOTIFICATIONS)
        val missing = needed.filter { ContextCompat.checkSelfPermission(context, it) != PackageManager.PERMISSION_GRANTED }
        if (missing.isNotEmpty()) recordPermissionsLauncher.launch(missing.toTypedArray())
        else {
          val mpm = context.getSystemService(Context.MEDIA_PROJECTION_SERVICE) as MediaProjectionManager
          projectionLauncher.launch(mpm.createScreenCaptureIntent())
        }
      },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.SHARE_EXPORT -> ShareExportDialog(
      pageCount = totalPages,
      currentPage = ui.pageIndex,
      onSavePng = { viewModel.exportCurrentPagePng(canvasViewRef?.width ?: 1080, canvasViewRef?.height ?: 1920) },
      onSharePng = {
        viewModel.shareCurrentPagePng(canvasViewRef?.width ?: 1080, canvasViewRef?.height ?: 1920) { intent -> context.startActivity(intent) }
      },
      onExportPdf = { pages, share ->
        viewModel.exportPdfPages(pages, share) { intent -> context.startActivity(intent) }
      },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.SHORTCUTS -> ShortcutsDialog(
      selectedTools = prefs.shortcutTools,
      onSave = { viewModel.updateShortcutTools(it) },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.CUSTOMIZE_TOOLBAR -> CustomizeToolbarDialog(
      currentOrder = prefs.toolbarOrder,
      hiddenTools = prefs.hiddenToolbarTools,
      onSave = { order, hidden -> viewModel.saveToolbarCustomization(order, hidden) },
      onReset = { viewModel.resetToolbarCustomization() },
      onDismiss = { activeDialog = ActiveDialog.NONE }
    )
    ActiveDialog.CLEAR_CONFIRM -> AlertDialog(
      onDismissRequest = { activeDialog = ActiveDialog.NONE },
      title = { Text(stringResource(R.string.clear_page_title)) },
      text = { Text(stringResource(R.string.clear_page_confirm)) },
      confirmButton = {
        TextButton(onClick = { viewModel.clearCurrentPage(); activeDialog = ActiveDialog.NONE }) {
          Text(stringResource(R.string.clear), color = TeachColors.Danger)
        }
      },
      dismissButton = {
        TextButton(onClick = { activeDialog = ActiveDialog.NONE }) { Text(stringResource(R.string.cancel)) }
      }
    )
    ActiveDialog.NONE -> Unit
  }

  if (loadedPrefs != null && prefs.tourVersionCompleted < AppConfig.CURRENT_TOUR_VERSION) {
    OnboardingTourDialog(onFinish = { viewModel.completeTour() })
  } else if (showSmartShapesGuide) {
    SmartShapesGuideDialog(onDismiss = {
      showSmartShapesGuide = false
      viewModel.dismissHint("smart_shapes_guide")
    })
  } else if (showHoldGuide) {
    HoldToMoveGuideDialog(onDismiss = {
      showHoldGuide = false
      viewModel.dismissHint("hold_to_move")
    })
  }
}

@Composable
private fun FloatingActionCircle(
  bg: Color,
  icon: ImageVector,
  contentDescription: String,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(38.dp)
      .shadow(5.dp, CircleShape)
      .clip(CircleShape)
      .background(bg)
      .border(2.dp, Color.White, CircleShape)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Icon(icon, contentDescription = contentDescription, tint = Color.White, modifier = Modifier.size(18.dp))
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMiniContours(
  contours: List<com.teachboard.app.domain.model.Contour>,
  color: Color
) {
  val all = contours.flatMap { it.points }
  if (all.size < 2) return
  var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
  var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
  for (p in all) {
    if (p.x < minX) minX = p.x; if (p.y < minY) minY = p.y
    if (p.x > maxX) maxX = p.x; if (p.y > maxY) maxY = p.y
  }
  val spanX = (maxX - minX).coerceAtLeast(1f)
  val spanY = (maxY - minY).coerceAtLeast(1f)
  val scale = minOf(size.width / spanX, size.height / spanY) * 0.84f
  val offX = (size.width - spanX * scale) / 2f
  val offY = (size.height - spanY * scale) / 2f
  val stroke = Stroke(width = 3.2f, cap = StrokeCap.Round, join = StrokeJoin.Round)
  for (c in contours) {
    if (c.points.size < 2) continue
    val path = Path().apply {
      moveTo(offX + (c.points[0].x - minX) * scale, offY + (c.points[0].y - minY) * scale)
      for (i in 1 until c.points.size) {
        lineTo(offX + (c.points[i].x - minX) * scale, offY + (c.points[i].y - minY) * scale)
      }
      if (c.closed) close()
    }
    drawPath(path, color, style = stroke)
  }
}

@Composable
private fun ToolbarItemButton(
  action: ToolbarAction,
  selected: Boolean,
  enabled: Boolean,
  colorDot: Color?,
  smartShapesMode: Boolean = false,
  labelOverride: String? = null,
  onClick: () -> Unit
) {
  val icon = toolbarIconFor(action)
  val label = labelOverride ?: toolbarLabelFor(action)
  val isUtility = action == ToolbarAction.CUSTOMIZE || action == ToolbarAction.HELP
  val isSmartBlueActive = action == ToolbarAction.SMART && selected
  val fg = when {
    !enabled -> Color(0x55374151)
    isSmartBlueActive -> Color.White
    isUtility -> Color(0xFF1D4ED8)
    else -> Color(0xFF1F2937)
  }

  Box(
    modifier = Modifier
      .widthIn(min = 52.dp)
      .clip(RoundedCornerShape(11.dp))
      .background(
        when {
          isSmartBlueActive -> Color(0xFF1D4ED8)
          selected -> Color(0xFFDCDFE5)
          else -> Color.Transparent
        }
      )
      .clickable(enabled = enabled, onClick = onClick)
      .padding(vertical = 7.dp, horizontal = 5.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box {
        Icon(icon, contentDescription = label, tint = fg, modifier = Modifier.size(23.dp))
        // Smart circular badge: △ when Smart Shapes is ON, Aa when Smart Writing
        if (action == ToolbarAction.SMART) {
          if (isSmartBlueActive) {
            Box(
              modifier = Modifier
                .align(Alignment.TopStart)
                .offset(x = (-6).dp, y = (-4).dp)
                .size(8.dp)
                .clip(CircleShape)
                .background(Color(0xFF22C55E))
            )
          }
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .offset(x = 8.dp, y = (-4).dp)
              .size(16.dp)
              .clip(CircleShape)
              .background(if (isSmartBlueActive) Color(0xFF3B82F6) else Color(0xFF2563EB)),
            contentAlignment = Alignment.Center
          ) {
            Text(
              if (smartShapesMode) "△" else "Aa",
              fontSize = 7.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White
            )
          }
        }
        // OCR clean green "AI" badge
        if (action == ToolbarAction.OCR) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .offset(x = 8.dp, y = (-4).dp)
              .clip(RoundedCornerShape(6.dp))
              .background(Color(0xFF0D9488))
              .padding(horizontal = 3.dp),
            contentAlignment = Alignment.Center
          ) {
            Text("AI", fontSize = 7.sp, fontWeight = FontWeight.Bold, color = Color.White)
          }
        }
        // Live color dot on Pen, Laser & Board (top-right, never overlaps label!)
        if (colorDot != null && (action == ToolbarAction.PEN || action == ToolbarAction.LASER || action == ToolbarAction.BOARD)) {
          Box(
            modifier = Modifier
              .align(Alignment.TopEnd)
              .offset(x = 8.dp, y = (-4).dp)
              .size(10.dp)
              .clip(CircleShape)
              .background(colorDot)
              .border(1.dp, if (action == ToolbarAction.BOARD) Color(0xFF64748B) else Color.White, CircleShape)
          )
        }
      }
      Spacer(Modifier.height(3.dp))
      Text(text = label, fontSize = 10.sp, color = fg, maxLines = 1, softWrap = false)
    }

    // Crisp bottom-right diagonal triangle ◢ on tools with options (Pen, Smart, Eraser, Laser)
    if (action == ToolbarAction.PEN || action == ToolbarAction.SMART || action == ToolbarAction.ERASER || action == ToolbarAction.LASER) {
      Canvas(modifier = Modifier.align(Alignment.BottomEnd).padding(3.dp).size(7.dp)) {
        val tri = Path().apply {
          moveTo(size.width, 0f)
          lineTo(size.width, size.height)
          lineTo(0f, size.height)
          close()
        }
        drawPath(tri, Color(0xFF6B7280))
      }
    }
  }
}

@Composable
private fun DraggableShortcutBar(
  tools: List<ToolbarAction>,
  onTrigger: (ToolbarAction) -> Unit
) {
  var offsetX by remember { mutableFloatStateOf(16f) }
  var offsetY by remember { mutableFloatStateOf(260f) }

  Surface(
    modifier = Modifier
      .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
      .pointerInput(Unit) {
        detectDragGestures { change, drag -> change.consume(); offsetX += drag.x; offsetY += drag.y }
      },
    shape = RoundedCornerShape(22.dp),
    color = TeachColors.DarkPillBg,
    shadowElevation = 6.dp
  ) {
    Column(modifier = Modifier.padding(vertical = 6.dp, horizontal = 4.dp), verticalArrangement = Arrangement.spacedBy(4.dp), horizontalAlignment = Alignment.CenterHorizontally) {
      tools.forEach { action ->
        IconButton(onClick = { onTrigger(action) }, modifier = Modifier.size(34.dp)) {
          Icon(toolbarIconFor(action), contentDescription = action.name, tint = Color.White, modifier = Modifier.size(18.dp))
        }
      }
    }
  }
}

internal fun toolbarIconFor(action: ToolbarAction): ImageVector = when (action) {
  ToolbarAction.SAVE -> Icons.Default.Save
  ToolbarAction.SHARE -> Icons.Default.Share
  ToolbarAction.UNDO -> Icons.AutoMirrored.Filled.Undo
  ToolbarAction.REDO -> Icons.AutoMirrored.Filled.Redo
  ToolbarAction.CLEAR -> Icons.Default.DeleteOutline
  ToolbarAction.PEN -> Icons.Default.Edit
  ToolbarAction.SMART -> Icons.Default.AutoFixHigh
  ToolbarAction.SHAPES -> Icons.Default.Category
  ToolbarAction.ERASER -> Icons.Default.CleaningServices
  ToolbarAction.BOARD -> Icons.Default.FormatColorFill
  ToolbarAction.TEXT -> Icons.Default.TextFields
  ToolbarAction.FORMULA -> Icons.Default.Functions
  ToolbarAction.TABLE -> Icons.Default.TableChart
  ToolbarAction.LAYERS -> Icons.Default.Layers
  ToolbarAction.IMAGE -> Icons.Default.Image
  ToolbarAction.VIDEO -> Icons.Default.VideoLibrary
  ToolbarAction.WEB_IMAGE -> Icons.Default.ImageSearch
  ToolbarAction.CAMERA -> Icons.Default.CameraAlt
  ToolbarAction.OCR -> Icons.Default.DocumentScanner
  ToolbarAction.IMPORT_PDF -> Icons.Default.PictureAsPdf
  ToolbarAction.STICKERS -> Icons.Default.EmojiEmotions
  ToolbarAction.SCIENCE -> Icons.Default.Science
  ToolbarAction.LASER -> Icons.Default.FlashlightOn
  ToolbarAction.GEOMETRY -> Icons.Default.SquareFoot
  ToolbarAction.CALCULATOR -> Icons.Default.Calculate
  ToolbarAction.TIMER -> Icons.Default.Timer
  ToolbarAction.RECORD -> Icons.Default.RadioButtonChecked
  ToolbarAction.LASSO -> Icons.Default.Gesture
  ToolbarAction.HAND -> Icons.Default.PanTool
  ToolbarAction.CUSTOMIZE -> Icons.Default.Settings
  ToolbarAction.HELP -> Icons.AutoMirrored.Filled.HelpOutline
}

@Composable
private fun toolbarLabelFor(action: ToolbarAction): String = when (action) {
  ToolbarAction.SAVE -> stringResource(R.string.tool_save)
  ToolbarAction.SHARE -> stringResource(R.string.tool_share)
  ToolbarAction.UNDO -> stringResource(R.string.tool_undo)
  ToolbarAction.REDO -> stringResource(R.string.tool_redo)
  ToolbarAction.CLEAR -> stringResource(R.string.tool_clear)
  ToolbarAction.PEN -> stringResource(R.string.tool_pen)
  ToolbarAction.SMART -> stringResource(R.string.tool_smart)
  ToolbarAction.SHAPES -> stringResource(R.string.tool_shapes)
  ToolbarAction.ERASER -> stringResource(R.string.tool_eraser)
  ToolbarAction.BOARD -> stringResource(R.string.tool_board)
  ToolbarAction.TEXT -> stringResource(R.string.tool_text)
  ToolbarAction.FORMULA -> stringResource(R.string.tool_formula)
  ToolbarAction.TABLE -> stringResource(R.string.tool_table)
  ToolbarAction.LAYERS -> stringResource(R.string.tool_layers)
  ToolbarAction.IMAGE -> stringResource(R.string.tool_image)
  ToolbarAction.VIDEO -> stringResource(R.string.tool_video)
  ToolbarAction.WEB_IMAGE -> stringResource(R.string.tool_web_image)
  ToolbarAction.CAMERA -> stringResource(R.string.tool_camera)
  ToolbarAction.OCR -> stringResource(R.string.tool_ocr)
  ToolbarAction.IMPORT_PDF -> stringResource(R.string.tool_pdf)
  ToolbarAction.STICKERS -> stringResource(R.string.tool_stickers)
  ToolbarAction.SCIENCE -> stringResource(R.string.tool_science)
  ToolbarAction.LASER -> stringResource(R.string.tool_laser)
  ToolbarAction.GEOMETRY -> stringResource(R.string.tool_geometry)
  ToolbarAction.CALCULATOR -> stringResource(R.string.tool_calculator)
  ToolbarAction.TIMER -> stringResource(R.string.tool_timer)
  ToolbarAction.RECORD -> stringResource(R.string.tool_record)
  ToolbarAction.LASSO -> stringResource(R.string.tool_lasso)
  ToolbarAction.HAND -> stringResource(R.string.tool_hand)
  ToolbarAction.CUSTOMIZE -> stringResource(R.string.tool_customize)
  ToolbarAction.HELP -> stringResource(R.string.tool_help)
}
