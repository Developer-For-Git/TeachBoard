package com.teachboard.app.ui.canvas

import android.graphics.Bitmap
import android.media.RingtoneManager
import android.net.Uri
import android.view.ViewGroup
import java.io.File
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.automirrored.filled.RotateRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.OpenInNew
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PushPin
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VerticalSplit
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import com.teachboard.app.domain.model.ToolType
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.teachboard.app.R
import com.teachboard.app.domain.calc.ExpressionEvaluator
import com.teachboard.app.domain.export.LazyPdfCache
import com.teachboard.app.domain.model.Contour
import com.teachboard.app.domain.model.FaceCamShape
import com.teachboard.app.domain.model.GeometryToolType
import com.teachboard.app.domain.model.PdfViewMode
import com.teachboard.app.domain.model.StrokePoint
import com.teachboard.app.ui.theme.TeachColors
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.HorizontalDivider
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.Dp
import kotlin.math.roundToInt
import kotlin.math.sin

// ===== 1. Geometry Box Bottom-Docked Card (Matching reverse-engineered spec) =====

@Composable
fun GeometryPickerDialog(
  activeTool: GeometryToolType?,
  onSelect: (GeometryToolType?) -> Unit,
  onDismiss: () -> Unit
) {
  val cfg = LocalConfiguration.current
  val minSide = kotlin.math.min(cfg.screenWidthDp, cfg.screenHeightDp).toFloat()
  val sheetW = (cfg.screenWidthDp * 0.84f).coerceIn(300f, 430f)
  val bottomInset = (minSide * 0.22f).coerceIn(92f, 104f)
  val iconSize = (minSide * 0.066f).coerceIn(24f, 32f)
  val tileW = (sheetW - 27f) / 4f

  Box(
    modifier = Modifier
      .fillMaxSize()
      .clickable(
        interactionSource = remember { MutableInteractionSource() },
        indication = null,
        onClick = onDismiss
      )
  ) {
    Column(
      modifier = Modifier.fillMaxSize(),
      verticalArrangement = Arrangement.Bottom,
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      Surface(
        modifier = Modifier
          .width(sheetW.dp)
          .padding(bottom = bottomInset.dp),
        shape = RoundedCornerShape(14.dp),
        color = Color.White,
        shadowElevation = 6.dp
      ) {
        Column {
          // Amber wash gradient header
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Brush.horizontalGradient(listOf(Color(0x73FFCE54), Color(0x1FFFCE54))))
              .padding(horizontal = 10.dp, vertical = 7.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(18.dp)
                .clip(RoundedCornerShape(6.dp))
                .background(Color(0xFFB45309)),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                painter = painterResource(R.drawable.ic_geo_box),
                contentDescription = null,
                modifier = Modifier.size(11.dp),
                tint = Color.White
              )
            }
            Spacer(Modifier.width(6.dp))
            Text(
              text = stringResource(R.string.tb_geometry_box),
              color = Color(0xFF0F172A),
              fontSize = 12.sp,
              fontWeight = FontWeight.W800
            )
          }

          HorizontalDivider(
            thickness = 1.dp,
            color = Color(0xFFB45309).copy(alpha = 0.14f)
          )

          // 4 Tiles Row: Ruler · Protractor · Compass · Set Square
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 6.dp, vertical = 7.dp),
            horizontalArrangement = Arrangement.spacedBy(5.dp)
          ) {
            GeometryBoxTile(
              icon = R.drawable.ic_geo_ruler,
              label = stringResource(R.string.tb_geo_ruler),
              selected = activeTool == GeometryToolType.RULER,
              iconSize = iconSize.dp,
              tileW = tileW.dp
            ) {
              onSelect(if (activeTool == GeometryToolType.RULER) null else GeometryToolType.RULER)
              onDismiss()
            }

            GeometryBoxTile(
              icon = R.drawable.ic_geo_protractor,
              label = stringResource(R.string.tb_geo_protractor),
              selected = activeTool == GeometryToolType.PROTRACTOR,
              iconSize = iconSize.dp,
              tileW = tileW.dp
            ) {
              onSelect(if (activeTool == GeometryToolType.PROTRACTOR) null else GeometryToolType.PROTRACTOR)
              onDismiss()
            }

            GeometryBoxTile(
              icon = R.drawable.ic_geo_compass,
              label = stringResource(R.string.tb_geo_compass),
              selected = activeTool == GeometryToolType.COMPASS,
              iconSize = iconSize.dp,
              tileW = tileW.dp
            ) {
              onSelect(if (activeTool == GeometryToolType.COMPASS) null else GeometryToolType.COMPASS)
              onDismiss()
            }

            val isSetSquareSel = activeTool == GeometryToolType.SET_SQUARE_30_60 || activeTool == GeometryToolType.SET_SQUARE_45_45
            GeometryBoxTile(
              icon = R.drawable.ic_geo_setsquare,
              label = stringResource(R.string.tb_geo_setsquare),
              selected = isSetSquareSel,
              iconSize = iconSize.dp,
              tileW = tileW.dp
            ) {
              onSelect(if (isSetSquareSel) null else GeometryToolType.SET_SQUARE_45_45)
              onDismiss()
            }
          }
        }
      }
    }
  }
}

@Composable
fun GeometryBoxTile(
  icon: Int,
  label: String,
  selected: Boolean,
  iconSize: Dp,
  tileW: Dp,
  onClick: () -> Unit
) {
  val measurer = rememberTextMeasurer()
  val density = LocalDensity.current
  val limitPx = with(density) { (tileW - 8.dp).toPx() }
  val style = TextStyle(fontWeight = FontWeight.W600, color = Color(0xFF0F172A))
  val size = remember(label, limitPx) {
    var sp = 13f
    while (sp > 8f && measurer.measure(label, style.copy(fontSize = sp.sp)).size.width > limitPx) {
      sp -= 0.5f
    }
    sp
  }
  val fitsOneLine = remember(label, limitPx, size) {
    measurer.measure(label, style.copy(fontSize = size.sp)).size.width <= limitPx
  }

  Column(
    modifier = Modifier
      .width(tileW)
      .clip(RoundedCornerShape(11.dp))
      .background(if (selected) Color(0x29FFCE54) else Color(0xFFFBFAF7))
      .border(
        width = 1.25.dp,
        color = if (selected) Color(0xFFB45309) else Color(0xFFE2E8F0),
        shape = RoundedCornerShape(11.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 2.dp, vertical = 7.dp),
    horizontalAlignment = Alignment.CenterHorizontally
  ) {
    Icon(
      painter = painterResource(icon),
      contentDescription = label,
      modifier = Modifier.size(iconSize),
      tint = Color(0xFFB45309)
    )
    Spacer(Modifier.height(4.dp))
    Text(
      text = label,
      modifier = Modifier.fillMaxWidth(),
      color = Color(0xFF0F172A),
      fontSize = size.sp,
      fontWeight = FontWeight.W600,
      textAlign = TextAlign.Center,
      maxLines = if (fitsOneLine) 1 else 2,
      lineHeight = (size * 1.4f).sp
    )
    if (selected) {
      Spacer(Modifier.height(4.dp))
      Box(
        modifier = Modifier
          .size(14.dp)
          .clip(CircleShape)
          .background(Color(0xFFB45309)),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          modifier = Modifier.size(9.dp),
          tint = Color.White
        )
      }
    }
  }
}

// ===== Realistic Translucent Amber Acrylic Styling Constants =====
private val AcrylicAmberFill = Color(0xE8FBF2CC)
private val AcrylicAmberBorder = Color(0xFF8B6B32)
private val AcrylicTickColor = Color(0xFF3E2C11)
private val AcrylicTextColor = Color(0xFF261906)
private val AcrylicAccentOrange = Color(0xFFEA580C)
private val AcrylicArmColor = Color(0xFF5A4A1C)

@Composable
private fun GeometryActionCircle(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  contentDescription: String,
  backgroundColor: Color,
  iconColor: Color = Color.White,
  sizeDp: Int = 36,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .size(sizeDp.coerceAtLeast(36).dp)
      .shadow(3.dp, CircleShape)
      .clip(CircleShape)
      .background(backgroundColor)
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Icon(
      icon,
      contentDescription = contentDescription,
      tint = iconColor,
      modifier = Modifier.size((sizeDp.coerceAtLeast(36) * 0.54f).dp)
    )
  }
}

private fun rotatePointAround(px: Float, py: Float, cx: Float, cy: Float, deg: Float): Offset {
  val rad = Math.toRadians(deg.toDouble())
  val cos = cos(rad).toFloat()
  val sin = sin(rad).toFloat()
  val dx = px - cx
  val dy = py - cy
  return Offset(cx + dx * cos - dy * sin, cy + dx * sin + dy * cos)
}

@Composable
fun GeometryBoxOverlay(
  tool: GeometryToolType,
  zoom: Float,
  panX: Float,
  panY: Float,
  onDrawContour: (Contour) -> Unit,
  onClose: () -> Unit
) {
  var offsetX by remember(tool) { mutableFloatStateOf(240f) }
  var offsetY by remember(tool) { mutableFloatStateOf(90f) }
  var rotationDeg by remember(tool) { mutableFloatStateOf(0f) }
  var scaleFactor by remember(tool) { mutableFloatStateOf(1.0f) }
  var angleArmDeg by remember(tool) { mutableFloatStateOf(90f) }
  var rulerLengthDp by remember(tool) { mutableFloatStateOf(380f) }
  var compassRadiusDp by remember(tool) { mutableFloatStateOf(120f) }
  var currentToolType by remember(tool) { mutableStateOf(tool) }

  val density = LocalDensity.current.density

  fun screenToWorld(sx: Float, sy: Float): StrokePoint =
    StrokePoint((sx - panX) / zoom.coerceAtLeast(0.1f), (sy - panY) / zoom.coerceAtLeast(0.1f))

  Box(
    modifier = Modifier
      .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
  ) {
    when (currentToolType) {
      GeometryToolType.PROTRACTOR -> {
        ProtractorOverlay(
          scale = scaleFactor,
          rotationDeg = rotationDeg,
          angleArmDeg = angleArmDeg,
          density = density,
          onMove = { dx, dy -> offsetX += dx; offsetY += dy },
          onRotateDelta = { dDeg -> rotationDeg = (rotationDeg + dDeg + 360f) % 360f },
          onAngleArmChange = { angleArmDeg = it },
          onCycleScale = {
            scaleFactor = when {
              scaleFactor < 0.95f -> 1.0f
              scaleFactor < 1.2f -> 1.25f
              else -> 0.85f
            }
          },
          onClose = onClose,
          onDrawAngle = {
            val w = 360f * scaleFactor * density
            val h = 230f * scaleFactor * density
            val boxCenterX = offsetX + w / 2f
            val boxCenterY = offsetY + h / 2f
            val cx = w / 2f
            val cy = h - 42f * density * scaleFactor
            val r = (w / 2f) - 18f * density * scaleFactor
            val armRad = Math.toRadians(-angleArmDeg.toDouble())

            val originScreen = rotatePointAround(offsetX + cx, offsetY + cy, boxCenterX, boxCenterY, rotationDeg)
            val baseScreen = rotatePointAround(offsetX + cx + r, offsetY + cy, boxCenterX, boxCenterY, rotationDeg)
            val armScreen = rotatePointAround(offsetX + cx + (cos(armRad) * r).toFloat(), offsetY + cy + (sin(armRad) * r).toFloat(), boxCenterX, boxCenterY, rotationDeg)

            val arcPts = (0..24).map { i ->
              val a = Math.toRadians(-angleArmDeg.toDouble() * (i / 24.0))
              val sc = rotatePointAround(offsetX + cx + (cos(a) * r * 0.45f).toFloat(), offsetY + cy + (sin(a) * r * 0.45f).toFloat(), boxCenterX, boxCenterY, rotationDeg)
              screenToWorld(sc.x, sc.y)
            }

            val pts = listOf(screenToWorld(baseScreen.x, baseScreen.y), screenToWorld(originScreen.x, originScreen.y), screenToWorld(armScreen.x, armScreen.y)) + arcPts
            onDrawContour(Contour(closed = false, points = pts))
          },
          onDrawArc = {
            val w = 360f * scaleFactor * density
            val h = 230f * scaleFactor * density
            val boxCenterX = offsetX + w / 2f
            val boxCenterY = offsetY + h / 2f
            val cx = w / 2f
            val cy = h - 42f * density * scaleFactor
            val r = (w / 2f) - 18f * density * scaleFactor
            val steps = 36
            val pts = (0..steps).map { i ->
              val a = Math.toRadians(-angleArmDeg.toDouble() * (i.toDouble() / steps))
              val sc = rotatePointAround(offsetX + cx + (cos(a) * r).toFloat(), offsetY + cy + (sin(a) * r).toFloat(), boxCenterX, boxCenterY, rotationDeg)
              screenToWorld(sc.x, sc.y)
            }
            onDrawContour(Contour(closed = false, points = pts))
          },
          onDrawSector = {
            val w = 360f * scaleFactor * density
            val h = 230f * scaleFactor * density
            val boxCenterX = offsetX + w / 2f
            val boxCenterY = offsetY + h / 2f
            val cx = w / 2f
            val cy = h - 42f * density * scaleFactor
            val r = (w / 2f) - 18f * density * scaleFactor
            val originScreen = rotatePointAround(offsetX + cx, offsetY + cy, boxCenterX, boxCenterY, rotationDeg)
            val steps = 32
            val pts = mutableListOf(screenToWorld(originScreen.x, originScreen.y))
            for (i in 0..steps) {
              val a = Math.toRadians(-angleArmDeg.toDouble() * (i.toDouble() / steps))
              val sc = rotatePointAround(offsetX + cx + (cos(a) * r).toFloat(), offsetY + cy + (sin(a) * r).toFloat(), boxCenterX, boxCenterY, rotationDeg)
              pts.add(screenToWorld(sc.x, sc.y))
            }
            pts.add(screenToWorld(originScreen.x, originScreen.y))
            onDrawContour(Contour(closed = true, points = pts))
          }
        )
      }

      GeometryToolType.RULER -> {
        val hPx = 76f * density
        RulerOverlay(
          lengthDp = rulerLengthDp,
          rotationDeg = rotationDeg,
          density = density,
          onMove = { dx, dy -> offsetX += dx; offsetY += dy },
          onResizeLength = { dWidth -> rulerLengthDp = (rulerLengthDp + dWidth).coerceIn(240f, 620f) },
          onRotateDelta = { dDeg -> rotationDeg = (rotationDeg + dDeg + 360f) % 360f },
          onClose = onClose,
          onDrawTopEdge = {
            val wPx = rulerLengthDp * density
            val boxCenterX = offsetX + wPx / 2f
            val boxCenterY = offsetY + hPx / 2f
            val p1 = rotatePointAround(offsetX + 64f * density, offsetY + 6f * density, boxCenterX, boxCenterY, rotationDeg)
            val p2 = rotatePointAround(offsetX + wPx - 40f * density, offsetY + 6f * density, boxCenterX, boxCenterY, rotationDeg)
            onDrawContour(Contour(closed = false, points = listOf(screenToWorld(p1.x, p1.y), screenToWorld(p2.x, p2.y))))
          },
          onDrawBottomEdge = {
            val wPx = rulerLengthDp * density
            val boxCenterX = offsetX + wPx / 2f
            val boxCenterY = offsetY + hPx / 2f
            val p1 = rotatePointAround(offsetX + 64f * density, offsetY + hPx - 6f * density, boxCenterX, boxCenterY, rotationDeg)
            val p2 = rotatePointAround(offsetX + wPx - 40f * density, offsetY + hPx - 6f * density, boxCenterX, boxCenterY, rotationDeg)
            onDrawContour(Contour(closed = false, points = listOf(screenToWorld(p1.x, p1.y), screenToWorld(p2.x, p2.y))))
          }
        )
      }

      GeometryToolType.COMPASS -> {
        val widthDp = (compassRadiusDp * 2f + 80f).coerceAtLeast(200f)
        val heightDp = 290f
        val w = widthDp * density
        val h = heightDp * density
        val boxCenterX = offsetX + w / 2f
        val boxCenterY = offsetY + h / 2f

        val hingeX = w / 2f
        val hingeY = 70f * density
        val needleX = hingeX - (compassRadiusDp * 0.85f * density)
        val pencilX = hingeX + (compassRadiusDp * 0.85f * density)
        val footY = hingeY + 170f * density

        val needleScreen = rotatePointAround(offsetX + needleX, offsetY + footY, boxCenterX, boxCenterY, rotationDeg)
        val circleRadiusPx = pencilX - needleX

        CompassOverlay(
          radiusDp = compassRadiusDp,
          rotationDeg = rotationDeg,
          density = density,
          onMove = { dx, dy -> offsetX += dx; offsetY += dy },
          onRadiusChange = { dRadius -> compassRadiusDp = (compassRadiusDp + dRadius).coerceIn(40f, 200f) },
          onRotateDelta = { dDeg -> rotationDeg = (rotationDeg + dDeg + 360f) % 360f },
          onClose = onClose,
          onDrawCircle = {
            val steps = 64
            val pts = (0..steps).map { i ->
              val a = 2.0 * PI * (i.toDouble() / steps)
              val px = needleScreen.x + (cos(a) * circleRadiusPx).toFloat()
              val py = needleScreen.y + (sin(a) * circleRadiusPx).toFloat()
              screenToWorld(px, py)
            }
            onDrawContour(Contour(closed = true, points = pts))
          },
          onDrawArc = {
            val steps = 36
            val startRad = Math.toRadians(rotationDeg.toDouble())
            val pts = (0..steps).map { i ->
              val a = startRad + (PI * (i.toDouble() / steps))
              val px = needleScreen.x + (cos(a) * circleRadiusPx).toFloat()
              val py = needleScreen.y + (sin(a) * circleRadiusPx).toFloat()
              screenToWorld(px, py)
            }
            onDrawContour(Contour(closed = false, points = pts))
          }
        )
      }

      GeometryToolType.SET_SQUARE_30_60, GeometryToolType.SET_SQUARE_45_45 -> {
        val is45 = currentToolType == GeometryToolType.SET_SQUARE_45_45
        val w = (if (is45) 230f else 260f) * scaleFactor * density
        val h = (if (is45) 230f else 170f) * scaleFactor * density
        val boxCenterX = offsetX + w / 2f
        val boxCenterY = offsetY + h / 2f

        SetSquareOverlay(
          is45 = is45,
          scale = scaleFactor,
          rotationDeg = rotationDeg,
          density = density,
          onMove = { dx, dy -> offsetX += dx; offsetY += dy },
          onRotateDelta = { dDeg -> rotationDeg = (rotationDeg + dDeg + 360f) % 360f },
          onResizeScale = { dScale -> scaleFactor = (scaleFactor + dScale).coerceIn(0.7f, 1.6f) },
          onToggleType = {
            currentToolType = if (is45) GeometryToolType.SET_SQUARE_30_60 else GeometryToolType.SET_SQUARE_45_45
          },
          onClose = onClose,
          onDrawTriangle = {
            val pLeftTop = rotatePointAround(offsetX + 10f * density, offsetY + 10f * density, boxCenterX, boxCenterY, rotationDeg)
            val pLeftBottom = rotatePointAround(offsetX + 10f * density, offsetY + h - 10f * density, boxCenterX, boxCenterY, rotationDeg)
            val pRightBottom = rotatePointAround(offsetX + w - 10f * density, offsetY + h - 10f * density, boxCenterX, boxCenterY, rotationDeg)

            val pts = listOf(
              screenToWorld(pLeftTop.x, pLeftTop.y),
              screenToWorld(pLeftBottom.x, pLeftBottom.y),
              screenToWorld(pRightBottom.x, pRightBottom.y)
            )
            onDrawContour(Contour(closed = true, points = pts))
          },
          onDrawHypotenuse = {
            val pLeftTop = rotatePointAround(offsetX + 10f * density, offsetY + 10f * density, boxCenterX, boxCenterY, rotationDeg)
            val pRightBottom = rotatePointAround(offsetX + w - 10f * density, offsetY + h - 10f * density, boxCenterX, boxCenterY, rotationDeg)

            val pts = listOf(
              screenToWorld(pLeftTop.x, pLeftTop.y),
              screenToWorld(pRightBottom.x, pRightBottom.y)
            )
            onDrawContour(Contour(closed = false, points = pts))
          }
        )
      }
    }
  }
}

// ===== 1A. High-Fidelity Protractor Overlay =====
@Composable
private fun ProtractorOverlay(
  scale: Float,
  rotationDeg: Float,
  angleArmDeg: Float,
  density: Float,
  onMove: (Float, Float) -> Unit,
  onRotateDelta: (Float) -> Unit,
  onAngleArmChange: (Float) -> Unit,
  onCycleScale: () -> Unit,
  onClose: () -> Unit,
  onDrawAngle: () -> Unit,
  onDrawArc: () -> Unit,
  onDrawSector: () -> Unit
) {
  val widthDp = 360f * scale
  val heightDp = 230f * scale
  val widthPx = widthDp * density
  val heightPx = heightDp * density

  val cx = widthPx / 2f
  val cy = heightPx - 42f * density * scale
  val outerR = (widthPx / 2f) - 18f * density * scale
  val innerWindowR = outerR * 0.44f

  Box(
    modifier = Modifier
      .size(widthDp.dp, heightDp.dp)
      .rotate(rotationDeg)
      .pointerInput(Unit) {
        detectDragGestures { change, drag ->
          change.consume()
          onMove(drag.x, drag.y)
        }
      }
  ) {
    // Acrylic Protractor Body with Dual Scale & Number Markings
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvas = drawContext.canvas.nativeCanvas

      // 1. Outer Arch Path
      val archPath = Path().apply {
        // Base plate rounded bottom
        moveTo(cx - outerR, cy)
        lineTo(cx - outerR, cy + 28f * density * scale)
        quadraticTo(cx - outerR, cy + 38f * density * scale, cx - outerR + 10f * density * scale, cy + 38f * density * scale)
        lineTo(cx + outerR - 10f * density * scale, cy + 38f * density * scale)
        quadraticTo(cx + outerR, cy + 38f * density * scale, cx + outerR, cy + 28f * density * scale)
        lineTo(cx + outerR, cy)
        // Outer dome
        arcTo(
          rect = androidx.compose.ui.geometry.Rect(cx - outerR, cy - outerR, cx + outerR, cy + outerR),
          startAngleDegrees = 0f,
          sweepAngleDegrees = -180f,
          forceMoveTo = false
        )
        close()
      }

      drawPath(archPath, AcrylicAmberFill)
      drawPath(archPath, AcrylicAmberBorder, style = Stroke(width = 2.5f * density))

      // 2. Inner Transparent Window (Cutout)
      val windowPath = Path().apply {
        moveTo(cx - innerWindowR, cy)
        lineTo(cx + innerWindowR, cy)
        arcTo(
          rect = androidx.compose.ui.geometry.Rect(cx - innerWindowR, cy - innerWindowR, cx + innerWindowR, cy + innerWindowR),
          startAngleDegrees = 0f,
          sweepAngleDegrees = -180f,
          forceMoveTo = false
        )
        close()
      }
      drawPath(windowPath, Color(0x33FFFFFF))
      drawPath(windowPath, Color(0x668B6B32), style = Stroke(width = 1.8f * density))

      // 3. Center Origin Pivot Circle and Crosshair
      drawCircle(Color(0xFF8B6B32), radius = 18f * density * scale, center = Offset(cx, cy), style = Stroke(1.5f * density))
      drawCircle(AcrylicAccentOrange, radius = 3.5f * density, center = Offset(cx, cy))
      drawLine(Color(0xFF8B6B32), Offset(cx - 18f * density * scale, cy), Offset(cx + 18f * density * scale, cy), strokeWidth = 1.5f * density)
      drawLine(Color(0xFF8B6B32), Offset(cx, cy - 18f * density * scale), Offset(cx, cy), strokeWidth = 1.5f * density)

      // 4. Center 90° Dashed Line
      drawLine(
        color = AcrylicAccentOrange.copy(alpha = 0.75f),
        start = Offset(cx, cy - 18f * density * scale),
        end = Offset(cx, cy - outerR),
        strokeWidth = 2f * density
      )

      // 5. Degree Tick Marks and Numbers (Dual Scale: Outer 0-180, Inner 180-0)
      val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        color = android.graphics.Color.parseColor("#2A1C08")
        textSize = (9.5f * scale * density).coerceAtLeast(16f)
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
      }
      val innerTextPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        color = android.graphics.Color.parseColor("#4A3716")
        textSize = (8.5f * scale * density).coerceAtLeast(14f)
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.NORMAL)
      }
      val accentTextPaint = android.graphics.Paint(textPaint).apply {
        color = android.graphics.Color.parseColor("#EA580C")
      }

      for (deg in 0..180) {
        val rad = Math.toRadians(-deg.toDouble())
        val cosA = cos(rad).toFloat()
        val sinA = sin(rad).toFloat()

        val is10 = deg % 10 == 0
        val is5 = deg % 5 == 0 && !is10
        val tickLen = when {
          is10 -> 14f * density * scale
          is5 -> 8.5f * density * scale
          else -> 4.5f * density * scale
        }

        val xOuter = cx + cosA * outerR
        val yOuter = cy + sinA * outerR
        val xInner = cx + cosA * (outerR - tickLen)
        val yInner = cy + sinA * (outerR - tickLen)

        val tickColor = if (deg == 90) AcrylicAccentOrange else AcrylicTickColor
        drawLine(
          color = tickColor,
          start = Offset(xOuter, yOuter),
          end = Offset(xInner, yInner),
          strokeWidth = if (is10) 2.2f * density else 1.2f * density
        )

        // Draw dual degree numbers every 10 degrees
        if (is10) {
          val outerNumR = outerR - 20f * density * scale
          val innerNumR = outerR - 38f * density * scale

          val numX1 = cx + cosA * outerNumR
          val numY1 = if (deg == 0 || deg == 180) cy - 4f * density else cy + sinA * outerNumR + 4f * density

          val numX2 = cx + cosA * innerNumR
          val numY2 = cy + sinA * innerNumR + 4f * density

          val paint1 = if (deg == 90) accentTextPaint else textPaint
          val paint2 = if (deg == 90) accentTextPaint else innerTextPaint

          canvas.drawText("$deg", numX1, numY1, paint1)
          if (deg != 0 && deg != 180) {
            canvas.drawText("${180 - deg}", numX2, numY2, paint2)
          }
        }
      }

      // 6. Baseline Millimeter & Centimeter Ticks
      for (mm in -60..60) {
        val xMm = cx + mm * (outerR / 65f)
        if (xMm in (cx - outerR + 4f)..(cx + outerR - 4f)) {
          val isCm = mm % 10 == 0
          val isHalfCm = mm % 5 == 0 && !isCm
          val len = if (isCm) 9f * density else if (isHalfCm) 6f * density else 3.5f * density
          drawLine(
            color = AcrylicTickColor,
            start = Offset(xMm, cy),
            end = Offset(xMm, cy + len),
            strokeWidth = 1f * density
          )
        }
      }

      // 7. Interactive Angle Needle Arm
      val armRad = Math.toRadians(-angleArmDeg.toDouble())
      val armCos = cos(armRad).toFloat()
      val armSin = sin(armRad).toFloat()
      val needleTipX = cx + armCos * (outerR + 18f * density * scale)
      val needleTipY = cy + armSin * (outerR + 18f * density * scale)

      drawLine(
        color = AcrylicArmColor,
        start = Offset(cx, cy),
        end = Offset(needleTipX, needleTipY),
        strokeWidth = 3.5f * density
      )
    }

    // Interactive Drag Bead at the tip of the angle needle
    val armRad = Math.toRadians(-angleArmDeg.toDouble())
    val beadR = outerR + 18f * density * scale
    val beadScreenX = (cx + cos(armRad).toFloat() * beadR) / density
    val beadScreenY = (cy + sin(armRad).toFloat() * beadR) / density

    Box(
      modifier = Modifier
        .offset { IntOffset((beadScreenX * density - 18f * density).roundToInt(), (beadScreenY * density - 18f * density).roundToInt()) }
        .size(36.dp)
        .shadow(4.dp, CircleShape)
        .clip(CircleShape)
        .background(Color(0xFF6B5822))
        .border(2.5.dp, Color.White, CircleShape)
        .pointerInput(Unit) {
          detectDragGestures { change, _ ->
            change.consume()
            val touchPos = change.position
            // Compute angle relative to center (cx, cy)
            val globalTouchX = beadScreenX * density + touchPos.x - 18f * density
            val globalTouchY = beadScreenY * density + touchPos.y - 18f * density
            val dx = globalTouchX - cx
            val dy = globalTouchY - cy
            var deg = Math.toDegrees(atan2(-dy.toDouble(), dx.toDouble())).toFloat()
            if (deg < 0f) deg = 0f
            if (deg > 180f) deg = 180f
            onAngleArmChange(deg)
          }
        },
      contentAlignment = Alignment.Center
    ) {
      Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(Color.White))
    }

    // Live Angle Pill Badge on the needle
    val badgeR = outerR * 0.72f
    val badgeX = (cx + cos(armRad).toFloat() * badgeR) / density
    val badgeY = (cy + sin(armRad).toFloat() * badgeR) / density
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = Color(0xFF6B5822),
      shadowElevation = 3.dp,
      modifier = Modifier
        .offset { IntOffset((badgeX * density - 26f * density).roundToInt(), (badgeY * density - 12f * density).roundToInt()) }
    ) {
      Text(
        text = "${angleArmDeg.roundToInt()}°",
        color = Color.White,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
      )
    }

    // Bottom Tray Action Buttons: Close, Draw Angle, Draw Arc, Draw Sector, Resize, Rotate
    Row(
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .offset(y = 8.dp)
        .padding(horizontal = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // 1. Red ✖ Close Button
      GeometryActionCircle(
        icon = Icons.Default.Close,
        contentDescription = "Close",
        backgroundColor = Color(0xFFEF4444),
        onClick = onClose
      )

      // 2. Teal ∠ Draw Angle
      GeometryActionCircle(
        icon = Icons.Default.Create,
        contentDescription = "Draw Angle",
        backgroundColor = Color(0xFF0D9488),
        onClick = onDrawAngle
      )

      // 3. Purple ⌒ Draw Arc
      GeometryActionCircle(
        icon = Icons.Default.Timer,
        contentDescription = "Draw Arc",
        backgroundColor = Color(0xFF8B5CF6),
        onClick = onDrawArc
      )

      // 4. Amber ◺ Draw Sector
      GeometryActionCircle(
        icon = Icons.Default.SquareFoot,
        contentDescription = "Draw Sector",
        backgroundColor = Color(0xFFD97706),
        onClick = onDrawSector
      )

      // 5. Pink ↕ Resize/Scale
      GeometryActionCircle(
        icon = Icons.Default.Add,
        contentDescription = "Scale Tool",
        backgroundColor = Color(0xFFEC4899),
        onClick = onCycleScale
      )

      // 6. Blue ↻ Rotate Handle (Tap +15°, or drag to rotate)
      Box(
        modifier = Modifier
          .size(34.dp)
          .shadow(3.dp, CircleShape)
          .clip(CircleShape)
          .background(Color(0xFF2563EB))
          .pointerInput(Unit) {
            detectDragGestures { change, drag ->
              change.consume()
              onRotateDelta(drag.x * 0.5f)
            }
          }
          .clickable { onRotateDelta(15f) },
        contentAlignment = Alignment.Center
      ) {
        Icon(
          Icons.AutoMirrored.Filled.RotateRight,
          contentDescription = "Rotate",
          tint = Color.White,
          modifier = Modifier.size(19.dp)
        )
      }
    }
  }
}

// ===== 1B. High-Fidelity Ruler Overlay =====
@Composable
private fun RulerOverlay(
  lengthDp: Float,
  rotationDeg: Float,
  density: Float,
  onMove: (Float, Float) -> Unit,
  onResizeLength: (Float) -> Unit,
  onRotateDelta: (Float) -> Unit,
  onClose: () -> Unit,
  onDrawTopEdge: () -> Unit,
  onDrawBottomEdge: () -> Unit
) {
  val heightDp = 76f
  val widthPx = lengthDp * density
  val heightPx = heightDp * density

  Box(
    modifier = Modifier
      .size(lengthDp.dp, heightDp.dp)
      .rotate(rotationDeg)
      .pointerInput(Unit) {
        detectDragGestures { change, drag ->
          change.consume()
          onMove(drag.x, drag.y)
        }
      }
  ) {
    // Acrylic Ruler Body
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvas = drawContext.canvas.nativeCanvas

      val rRect = androidx.compose.ui.geometry.RoundRect(
        0f, 0f, widthPx, heightPx,
        androidx.compose.ui.geometry.CornerRadius(14f * density, 14f * density)
      )
      val path = Path().apply { addRoundRect(rRect) }
      drawPath(path, AcrylicAmberFill)
      drawPath(path, AcrylicAmberBorder, style = Stroke(2.5f * density))

      val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        color = android.graphics.Color.parseColor("#261906")
        textSize = 12f * density
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
      }
      val labelPaint = android.graphics.Paint(textPaint).apply {
        textSize = 10f * density
        color = android.graphics.Color.parseColor("#8B6B32")
      }

      // Top Edge: Inches (in)
      canvas.drawText("in", 46f * density, 22f * density, labelPaint)
      val inchStepPx = 54f * density
      var inch = 0
      var inchX = 64f * density
      while (inchX <= widthPx - 40f * density) {
        drawLine(AcrylicTickColor, Offset(inchX, 0f), Offset(inchX, 16f * density), strokeWidth = 2.2f * density)
        canvas.drawText("$inch", inchX, 28f * density, textPaint)

        // Subdivisions: 1/4, 1/2, 3/4
        val q1 = inchX + inchStepPx * 0.25f
        val q2 = inchX + inchStepPx * 0.50f
        val q3 = inchX + inchStepPx * 0.75f
        if (q1 < widthPx - 40f * density) drawLine(AcrylicTickColor, Offset(q1, 0f), Offset(q1, 8f * density), strokeWidth = 1.2f * density)
        if (q2 < widthPx - 40f * density) drawLine(AcrylicTickColor, Offset(q2, 0f), Offset(q2, 12f * density), strokeWidth = 1.6f * density)
        if (q3 < widthPx - 40f * density) drawLine(AcrylicTickColor, Offset(q3, 0f), Offset(q3, 8f * density), strokeWidth = 1.2f * density)

        inch++
        inchX += inchStepPx
      }

      // Bottom Edge: Centimeters (cm)
      canvas.drawText("cm", 46f * density, heightPx - 18f * density, labelPaint)
      val cmStepPx = 21.5f * density
      var cm = 0
      var cmX = 64f * density
      while (cmX <= widthPx - 40f * density) {
        val isCm = true
        drawLine(AcrylicTickColor, Offset(cmX, heightPx), Offset(cmX, heightPx - 15f * density), strokeWidth = 2f * density)
        canvas.drawText("$cm", cmX, heightPx - 18f * density, textPaint)

        // Millimeter ticks
        for (m in 1..9) {
          val mx = cmX + m * (cmStepPx / 10f)
          if (mx < widthPx - 40f * density) {
            val len = if (m == 5) 9f * density else 5f * density
            drawLine(AcrylicTickColor, Offset(mx, heightPx), Offset(mx, heightPx - len), strokeWidth = 1.1f * density)
          }
        }
        cm++
        cmX += cmStepPx
      }
    }

    // Left End: Red ✖ Close Button
    Box(
      modifier = Modifier
        .align(Alignment.CenterStart)
        .padding(start = 8.dp)
    ) {
      GeometryActionCircle(
        icon = Icons.Default.Close,
        contentDescription = "Close",
        backgroundColor = Color(0xFFEF4444),
        sizeDp = 30,
        onClick = onClose
      )
    }

    // Right End: Length Grip Handle `|||` and Blue `↻` Rotate Button
    Row(
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
      // Length stretch handle
      Box(
        modifier = Modifier
          .width(22.dp)
          .height(48.dp)
          .clip(RoundedCornerShape(6.dp))
          .background(Color(0xFFE2C88F))
          .border(1.dp, Color(0xFF8B6B32), RoundedCornerShape(6.dp))
          .pointerInput(Unit) {
            detectDragGestures { change, drag ->
              change.consume()
              onResizeLength(drag.x / density)
            }
          },
        contentAlignment = Alignment.Center
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp), horizontalAlignment = Alignment.CenterHorizontally) {
          Box(modifier = Modifier.width(3.dp).height(24.dp).background(Color(0xFF8B6B32)))
          Box(modifier = Modifier.width(3.dp).height(24.dp).background(Color(0xFF8B6B32)))
        }
      }

      // Blue Rotate Handle
      Box(
        modifier = Modifier
          .size(32.dp)
          .shadow(3.dp, CircleShape)
          .clip(CircleShape)
          .background(Color(0xFF2563EB))
          .pointerInput(Unit) {
            detectDragGestures { change, drag ->
              change.consume()
              onRotateDelta(drag.y * 0.5f)
            }
          }
          .clickable { onRotateDelta(15f) },
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", tint = Color.White, modifier = Modifier.size(18.dp))
      }
    }

    // Quick Draw Action Chips in the center strip of the ruler
    Row(
      modifier = Modifier
        .align(Alignment.Center)
        .padding(horizontal = 60.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xDD0F172A),
        shadowElevation = 2.dp,
        modifier = Modifier.clickable(onClick = onDrawTopEdge)
      ) {
        Text(
          "✏️ in",
          color = Color(0xFF4ADE80),
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
        )
      }
      Surface(
        shape = RoundedCornerShape(8.dp),
        color = Color(0xDD0F172A),
        shadowElevation = 2.dp,
        modifier = Modifier.clickable(onClick = onDrawBottomEdge)
      ) {
        Text(
          "✏️ cm",
          color = Color(0xFF67E8F9),
          fontSize = 11.sp,
          fontWeight = FontWeight.Bold,
          modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
        )
      }
    }
  }
}

// ===== 1C. High-Fidelity Compass Overlay =====
@Composable
private fun CompassOverlay(
  radiusDp: Float,
  rotationDeg: Float,
  density: Float,
  onMove: (Float, Float) -> Unit,
  onRadiusChange: (Float) -> Unit,
  onRotateDelta: (Float) -> Unit,
  onClose: () -> Unit,
  onDrawCircle: () -> Unit,
  onDrawArc: () -> Unit
) {
  val widthDp = (radiusDp * 2f + 80f).coerceAtLeast(200f)
  val heightDp = 290f
  val widthPx = widthDp * density
  val heightPx = heightDp * density

  val hingeX = widthPx / 2f
  val hingeY = 70f * density
  val needleX = hingeX - (radiusDp * 0.85f * density)
  val pencilX = hingeX + (radiusDp * 0.85f * density)
  val footY = hingeY + 170f * density

  Box(
    modifier = Modifier
      .size(widthDp.dp, heightDp.dp)
      .rotate(rotationDeg)
      .pointerInput(Unit) {
        detectDragGestures { change, drag ->
          change.consume()
          onMove(drag.x, drag.y)
        }
      }
  ) {
    // Mechanical Compass Rendering
    Canvas(modifier = Modifier.fillMaxSize()) {
      // 1. Top Knurled Handle Grip
      drawRoundRect(
        color = Color(0xFF334155),
        topLeft = Offset(hingeX - 7f * density, hingeY - 50f * density),
        size = Size(14f * density, 34f * density),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(6f * density, 6f * density)
      )

      // 2. Central Hinge Disc
      drawCircle(Color(0xFF475569), radius = 22f * density, center = Offset(hingeX, hingeY))
      drawCircle(Color(0xFF94A3B8), radius = 17f * density, center = Offset(hingeX, hingeY), style = Stroke(2f * density))

      // 3. Left Needle Arm
      drawLine(Color(0xFF475569), Offset(hingeX - 8f * density, hingeY), Offset(needleX, footY - 14f * density), strokeWidth = 7f * density)
      // Steel needle tip
      drawLine(Color(0xFFE2E8F0), Offset(needleX, footY - 14f * density), Offset(needleX, footY), strokeWidth = 3f * density)
      drawCircle(Color(0xFF1E293B), radius = 4f * density, center = Offset(needleX, footY))

      // 4. Right Pencil Arm
      drawLine(Color(0xFF475569), Offset(hingeX + 8f * density, hingeY), Offset(pencilX, footY - 30f * density), strokeWidth = 7f * density)
      // Pencil barrel
      drawRoundRect(
        color = Color(0xFF2563EB),
        topLeft = Offset(pencilX - 5f * density, footY - 32f * density),
        size = Size(10f * density, 26f * density),
        cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f * density, 3f * density)
      )
      // Pencil wooden cone and lead tip
      val tipPath = Path().apply {
        moveTo(pencilX - 5f * density, footY - 6f * density)
        lineTo(pencilX + 5f * density, footY - 6f * density)
        lineTo(pencilX, footY)
        close()
      }
      drawPath(tipPath, Color(0xFFE2C88F))
      drawCircle(Color(0xFF2563EB), radius = 3.5f * density, center = Offset(pencilX, footY))

      // 5. Cross-brace and Radius Guide
      drawLine(Color(0x88475569), Offset(hingeX - 30f * density, hingeY + 80f * density), Offset(hingeX + 30f * density, hingeY + 80f * density), strokeWidth = 3f * density)
    }

    // Blue Rotate Handle right at the hinge center
    Box(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .offset(y = 54.dp)
        .size(32.dp)
        .shadow(3.dp, CircleShape)
        .clip(CircleShape)
        .background(Color(0xFF2563EB))
        .pointerInput(Unit) {
          detectDragGestures { change, drag ->
            change.consume()
            onRotateDelta(drag.x * 0.6f)
          }
        }
        .clickable { onRotateDelta(15f) },
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", tint = Color.White, modifier = Modifier.size(18.dp))
    }

    // Red Close Button on Pencil Arm
    Box(
      modifier = Modifier
        .align(Alignment.TopCenter)
        .offset(x = 36.dp, y = 78.dp)
    ) {
      GeometryActionCircle(
        icon = Icons.Default.Close,
        contentDescription = "Close",
        backgroundColor = Color(0xFFEF4444),
        sizeDp = 28,
        onClick = onClose
      )
    }

    // Spread Adjustment Slider Handle on the right leg
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = Color(0xFF0F172A),
      shadowElevation = 3.dp,
      modifier = Modifier
        .align(Alignment.Center)
        .offset(y = 10.dp)
        .pointerInput(Unit) {
          detectDragGestures { change, drag ->
            change.consume()
            onRadiusChange(drag.x / density)
          }
        }
    ) {
      Row(modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
        Text("↔ r=${(radiusDp / 24f).toString().take(3)} cm", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Bold)
      }
    }

    // Action Buttons at the bottom
    Surface(
      shape = RoundedCornerShape(16.dp),
      color = TeachColors.DarkPillBg,
      shadowElevation = 4.dp,
      modifier = Modifier
        .align(Alignment.BottomCenter)
        .offset(y = 12.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Box(
          modifier = Modifier
            .size(28.dp)
            .clip(CircleShape)
            .background(Color(0xFFEF4444))
            .clickable(onClick = onClose),
          contentAlignment = Alignment.Center
        ) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
        }
        TextButton(onClick = onDrawCircle) {
          Text("⭕ Draw Circle", color = Color(0xFF4ADE80), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
        TextButton(onClick = onDrawArc) {
          Text("⌒ Draw Arc", color = Color(0xFF67E8F9), fontSize = 11.sp, fontWeight = FontWeight.Bold)
        }
      }
    }
  }
}

// ===== 1D. High-Fidelity Set Square Overlay =====
@Composable
private fun SetSquareOverlay(
  is45: Boolean,
  scale: Float,
  rotationDeg: Float,
  density: Float,
  onMove: (Float, Float) -> Unit,
  onRotateDelta: (Float) -> Unit,
  onResizeScale: (Float) -> Unit,
  onToggleType: () -> Unit,
  onClose: () -> Unit,
  onDrawTriangle: () -> Unit,
  onDrawHypotenuse: () -> Unit
) {
  val widthDp = (if (is45) 240f else 270f) * scale
  val heightDp = (if (is45) 240f else 180f) * scale
  val widthPx = widthDp * density
  val heightPx = heightDp * density

  Box(
    modifier = Modifier
      .size(widthDp.dp, heightDp.dp)
      .rotate(rotationDeg)
      .pointerInput(Unit) {
        detectDragGestures { change, drag ->
          change.consume()
          onMove(drag.x, drag.y)
        }
      }
  ) {
    // Acrylic Triangle Body with Scales and Cutout
    Canvas(modifier = Modifier.fillMaxSize()) {
      val canvas = drawContext.canvas.nativeCanvas

      // Outer Triangle Path
      val triPath = Path().apply {
        moveTo(10f * density, 10f * density)
        lineTo(10f * density, heightPx - 10f * density)
        lineTo(widthPx - 10f * density, heightPx - 10f * density)
        close()
      }
      drawPath(triPath, AcrylicAmberFill)
      drawPath(triPath, AcrylicAmberBorder, style = Stroke(2.5f * density))

      // Inner Cutout Triangle
      val inPadX = widthPx * 0.22f
      val inPadY = heightPx * 0.22f
      val inTriPath = Path().apply {
        moveTo(10f * density + inPadX, 10f * density + inPadY * 1.6f)
        lineTo(10f * density + inPadX, heightPx - 10f * density - inPadY)
        lineTo(widthPx - 10f * density - inPadX * 1.6f, heightPx - 10f * density - inPadY)
        close()
      }
      drawPath(inTriPath, Color(0x33FFFFFF))
      drawPath(inTriPath, Color(0x668B6B32), style = Stroke(1.8f * density))

      // Right-Angle Corner Indicator ⊾
      val raSize = 14f * density
      drawLine(AcrylicTickColor, Offset(10f * density, heightPx - 10f * density - raSize), Offset(10f * density + raSize, heightPx - 10f * density - raSize), strokeWidth = 1.5f * density)
      drawLine(AcrylicTickColor, Offset(10f * density + raSize, heightPx - 10f * density - raSize), Offset(10f * density + raSize, heightPx - 10f * density), strokeWidth = 1.5f * density)

      // Scales on Vertical and Horizontal legs
      val textPaint = android.graphics.Paint().apply {
        isAntiAlias = true
        textAlign = android.graphics.Paint.Align.CENTER
        color = android.graphics.Color.parseColor("#261906")
        textSize = 10f * density * scale.coerceAtLeast(0.8f)
        typeface = android.graphics.Typeface.create(android.graphics.Typeface.SANS_SERIF, android.graphics.Typeface.BOLD)
      }

      val cmPx = 28f * density * scale
      // Vertical Leg cm ticks
      for (i in 1..6) {
        val y = heightPx - 10f * density - i * cmPx
        if (y > 18f * density) {
          drawLine(AcrylicTickColor, Offset(10f * density, y), Offset(22f * density, y), strokeWidth = 1.8f * density)
          canvas.drawText("$i", 30f * density, y + 4f * density, textPaint)
          for (m in 1..4) {
            val my = y + m * (cmPx / 5f)
            drawLine(AcrylicTickColor, Offset(10f * density, my), Offset(16f * density, my), strokeWidth = 1.1f * density)
          }
        }
      }

      // Horizontal Leg cm ticks
      for (i in 1..7) {
        val x = 10f * density + i * cmPx
        if (x < widthPx - 18f * density) {
          drawLine(AcrylicTickColor, Offset(x, heightPx - 10f * density), Offset(x, heightPx - 22f * density), strokeWidth = 1.8f * density)
          canvas.drawText("$i", x, heightPx - 26f * density, textPaint)
          for (m in 1..4) {
            val mx = x - m * (cmPx / 5f)
            drawLine(AcrylicTickColor, Offset(mx, heightPx - 10f * density), Offset(mx, heightPx - 16f * density), strokeWidth = 1.1f * density)
          }
        }
      }
    }

    // Top Vertex: Blue Rotate Button
    Box(
      modifier = Modifier
        .align(Alignment.TopStart)
        .offset(x = 0.dp, y = 0.dp)
        .size(32.dp)
        .shadow(3.dp, CircleShape)
        .clip(CircleShape)
        .background(Color(0xFF2563EB))
        .pointerInput(Unit) {
          detectDragGestures { change, drag ->
            change.consume()
            onRotateDelta(drag.x * 0.5f)
          }
        }
        .clickable { onRotateDelta(15f) },
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", tint = Color.White, modifier = Modifier.size(18.dp))
    }

    // Bottom-Right Vertex: Blue Resize Button
    Box(
      modifier = Modifier
        .align(Alignment.BottomEnd)
        .offset(x = 4.dp, y = 4.dp)
        .size(32.dp)
        .shadow(3.dp, CircleShape)
        .clip(CircleShape)
        .background(Color(0xFF2563EB))
        .pointerInput(Unit) {
          detectDragGestures { change, drag ->
            change.consume()
            onResizeScale((drag.x + drag.y) * 0.005f)
          }
        },
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.Add, contentDescription = "Resize", tint = Color.White, modifier = Modifier.size(18.dp))
    }

    // Hypotenuse Center: Red Close Button
    Box(
      modifier = Modifier
        .align(Alignment.Center)
        .offset(x = 10.dp, y = (-10).dp)
    ) {
      GeometryActionCircle(
        icon = Icons.Default.Close,
        contentDescription = "Close",
        backgroundColor = Color(0xFFEF4444),
        sizeDp = 30,
        onClick = onClose
      )
    }

    // Action Controls integrated inside the open triangular cutout window
    Surface(
      shape = RoundedCornerShape(12.dp),
      color = Color(0xEE0F172A),
      shadowElevation = 3.dp,
      modifier = Modifier
        .align(Alignment.Center)
        .offset(x = (-16).dp, y = 14.dp)
    ) {
      Row(
        modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0x33FFFFFF),
          modifier = Modifier.clickable(onClick = onToggleType)
        ) {
          Text(
            if (is45) "45°" else "30°-60°",
            color = Color(0xFFFDE047),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
          )
        }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0x33FFFFFF),
          modifier = Modifier.clickable(onClick = onDrawTriangle)
        ) {
          Text(
            "✏️ △",
            color = Color(0xFF4ADE80),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
          )
        }
        Surface(
          shape = RoundedCornerShape(6.dp),
          color = Color(0x33FFFFFF),
          modifier = Modifier.clickable(onClick = onDrawHypotenuse)
        ) {
          Text(
            "✏️ Hyp",
            color = Color(0xFF67E8F9),
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
          )
        }
      }
    }
  }
}

// ===== 2. Cobalt-Blue Header Floating Calculator (Resizable + Minimizable Bubble + Centered!) =====

@Composable
fun CalculatorOverlay(
  onPutOnBoard: (String) -> Unit,
  onClose: () -> Unit
) {
  val config = LocalConfiguration.current
  val density = LocalDensity.current
  val initWidthDp = if (config.screenWidthDp > config.screenHeightDp) 224f else 256f
  var widthDp by remember { mutableFloatStateOf(initWidthDp) }
  var offsetX by remember {
    mutableFloatStateOf(with(density) { ((config.screenWidthDp - initWidthDp) / 2f).coerceAtLeast(16f).dp.toPx() })
  }
  var offsetY by remember {
    mutableFloatStateOf(with(density) { (if (config.screenHeightDp < 500) 14f else 110f).dp.toPx() })
  }
  var expr by remember { mutableStateOf("") }
  var result by remember { mutableStateOf("0") }
  var scientific by remember { mutableStateOf(false) }
  var minimized by remember { mutableStateOf(false) }

  fun eval() {
    if (expr.isBlank()) { result = "0"; return }
    val v = ExpressionEvaluator.evaluate(expr)
    result = if (v != null) ExpressionEvaluator.format(v) else "Error"
  }

  if (minimized) {
    // Sleek draggable circular Cobalt-Blue Calculator bubble (tap to expand!)
    Surface(
      modifier = Modifier
        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        .size(56.dp)
        .pointerInput(Unit) {
          detectDragGestures { change, drag -> change.consume(); offsetX += drag.x; offsetY += drag.y }
        }
        .clickable { minimized = false },
      shape = CircleShape,
      color = Color(0xFF0020E0),
      shadowElevation = 10.dp
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(Icons.Default.Calculate, contentDescription = "Expand Calculator", tint = Color.White, modifier = Modifier.size(26.dp))
      }
    }
    return
  }

  val isLandscape = config.screenHeightDp < 500
  val maxWidthDp = if (isLandscape) 295f else 380f
  val scale = (widthDp / 256f).coerceIn(0.80f, 1.55f)
  val keyHeight = ((if (isLandscape) 30f else 36f) * scale).dp
  val keyFont = (15f * scale).sp

  Surface(
    modifier = Modifier
      .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
      .width(widthDp.dp)
      .pointerInput(Unit) {
        detectDragGestures { change, drag -> change.consume(); offsetX += drag.x; offsetY += drag.y }
      },
    shape = RoundedCornerShape(18.dp),
    shadowElevation = 12.dp,
    color = Color(0xFFF3F4F6)
  ) {
    Box {
      Column {
        // Vivid Cobalt Blue header bar (⋮⋮ Calculator  Σ  ∨  ×)
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF0020E0))
            .padding(horizontal = 8.dp, vertical = 5.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(Icons.Default.DragIndicator, contentDescription = null, tint = Color.White.copy(alpha = 0.85f), modifier = Modifier.size(17.dp))
          Spacer(Modifier.width(4.dp))
          Text(stringResource(R.string.tool_calculator), color = Color.White, fontWeight = FontWeight.Bold, fontSize = (13.5f * scale).coerceAtMost(17f).sp, modifier = Modifier.weight(1f))
          IconButton(onClick = { scientific = !scientific }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Functions, contentDescription = "Scientific", tint = if (scientific) Color(0xFFFDE047) else Color.White, modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = { minimized = true }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = Color.White, modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
          }
        }

        Column(modifier = Modifier.padding(start = 10.dp, end = 10.dp, top = 8.dp, bottom = 14.dp), verticalArrangement = Arrangement.spacedBy(5.dp)) {
          // White rounded display
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color.White,
            modifier = Modifier.fillMaxWidth().border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
          ) {
            Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = (8f * scale).dp), horizontalAlignment = Alignment.End) {
              if (expr.isNotEmpty()) {
                Text(expr, fontSize = (11.5f * scale).sp, color = Color(0xFF64748B), maxLines = 1)
              }
              Text(result, fontSize = (24f * scale).sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827), maxLines = 1)
            }
          }

          if (scientific) {
            listOf(listOf("sin(", "cos(", "tan(", "√("), listOf("ln(", "log(", "π", "^")).forEach { row ->
              Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
                row.forEach { k -> CalcPillKey(k, Modifier.weight(1f), bg = Color(0xFFE2E8F0), height = keyHeight, fontSize = (12.5f * scale).sp) { expr += k } }
              }
            }
          }

          val opBg = Color(0xFFE0E7FF)
          val opFg = Color(0xFF1D4ED8)
          val topBg = Color(0xFFE5E7EB)

          Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            CalcPillKey("C", Modifier.weight(1f), bg = topBg, height = keyHeight, fontSize = keyFont) { expr = ""; result = "0" }
            Surface(
              modifier = Modifier.weight(1f).height(keyHeight).clip(RoundedCornerShape(10.dp)).clickable { if (expr.isNotEmpty()) expr = expr.dropLast(1) },
              color = topBg
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(Icons.AutoMirrored.Filled.Backspace, contentDescription = "Backspace", tint = Color(0xFF1E293B), modifier = Modifier.size((17f * scale).dp))
              }
            }
            CalcPillKey("%", Modifier.weight(1f), bg = topBg, height = keyHeight, fontSize = keyFont) { expr += "%" }
            CalcPillKey("÷", Modifier.weight(1f), bg = opBg, fg = opFg, height = keyHeight, fontSize = keyFont) { expr += "÷" }
          }
          listOf(
            listOf("7", "8", "9", "×"),
            listOf("4", "5", "6", "−"),
            listOf("1", "2", "3", "+")
          ).forEach { row ->
            Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
              row.forEachIndexed { idx, key ->
                val isOp = idx == 3
                CalcPillKey(key, Modifier.weight(1f), bg = if (isOp) opBg else Color.White, fg = if (isOp) opFg else Color(0xFF111827), height = keyHeight, fontSize = keyFont) {
                  expr += if (key == "−") "-" else key
                }
              }
            }
          }
          Row(horizontalArrangement = Arrangement.spacedBy(5.dp), modifier = Modifier.fillMaxWidth()) {
            CalcPillKey("0", Modifier.weight(2.06f), bg = Color.White, height = keyHeight, fontSize = keyFont) { expr += "0" }
            CalcPillKey(".", Modifier.weight(1f), bg = Color.White, height = keyHeight, fontSize = keyFont) { expr += "." }
            CalcPillKey("=", Modifier.weight(1f), bg = Color(0xFF0020E0), fg = Color.White, height = keyHeight, fontSize = keyFont) { eval() }
          }

          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            TextButton(
              onClick = {
                eval()
                onPutOnBoard(if (expr.isNotBlank() && result != "Error") "$expr = $result" else result)
              },
              modifier = Modifier.height(if (isLandscape) 24.dp else 30.dp),
              contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp)
            ) {
              Text(stringResource(R.string.put_on_board), fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color(0xFF0020E0))
            }
          }
        }
      }

      // Bottom-right diagonal resize grip (///)
      ResizeCornerGrip(
        color = Color(0xFF94A3B8),
        modifier = Modifier.align(Alignment.BottomEnd),
        onDragDeltaPx = { dx ->
          val dDp = with(density) { dx.toDp().value }
          widthDp = (widthDp + dDp).coerceIn(205f, maxWidthDp)
        }
      )
    }
  }
}

@Composable
private fun CalcPillKey(
  label: String,
  modifier: Modifier,
  bg: Color,
  fg: Color = Color(0xFF111827),
  height: androidx.compose.ui.unit.Dp = 36.dp,
  fontSize: androidx.compose.ui.unit.TextUnit = 15.sp,
  onClick: () -> Unit
) {
  Surface(
    modifier = modifier.height(height).clip(RoundedCornerShape(10.dp)).clickable(onClick = onClick),
    color = bg,
    shadowElevation = 0.5.dp
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text(label, fontWeight = FontWeight.SemiBold, color = fg, fontSize = fontSize, maxLines = 1)
    }
  }
}

// ===== 3. Dark Floating Timer Card (Resizable + Minimizable Bubble + Centered!) =====

@Composable
fun TimerOverlay(onClose: () -> Unit) {
  val context = LocalContext.current
  val config = LocalConfiguration.current
  val density = LocalDensity.current
  val initWidthDp = if (config.screenWidthDp > config.screenHeightDp) 244f else 264f
  var widthDp by remember { mutableFloatStateOf(initWidthDp) }
  var offsetX by remember {
    mutableFloatStateOf(with(density) { ((config.screenWidthDp - initWidthDp) / 2f).coerceAtLeast(16f).dp.toPx() })
  }
  var offsetY by remember {
    mutableFloatStateOf(with(density) { (if (config.screenHeightDp < 500) 48f else 186f).dp.toPx() })
  }
  var totalSeconds by remember { mutableIntStateOf(30) }
  var remaining by remember { mutableIntStateOf(30) }
  var running by remember { mutableStateOf(false) }
  var soundOn by remember { mutableStateOf(true) }
  var minimized by remember { mutableStateOf(false) }

  LaunchedEffect(running) {
    while (running && remaining > 0) {
      delay(1000L)
      remaining--
      if (remaining == 0) {
        running = false
        if (soundOn) {
          runCatching {
            val uri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION)
            RingtoneManager.getRingtone(context, uri)?.play()
          }
        }
      }
    }
  }

  if (minimized) {
    // Sleek draggable circular Dark Charcoal Timer bubble (shows live countdown when running!)
    Surface(
      modifier = Modifier
        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        .size(56.dp)
        .pointerInput(Unit) {
          detectDragGestures { change, drag -> change.consume(); offsetX += drag.x; offsetY += drag.y }
        }
        .clickable { minimized = false },
      shape = CircleShape,
      color = Color(0xFF282B30),
      shadowElevation = 10.dp
    ) {
      Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
      ) {
        Icon(
          Icons.Default.Timer,
          contentDescription = "Expand Timer",
          tint = if (running) Color(0xFF4ADE80) else Color.White,
          modifier = Modifier.size(if (running) 20.dp else 26.dp)
        )
        if (running) {
          Text(
            String.format("%d:%02d", remaining / 60, remaining % 60),
            color = if (remaining <= 5) Color(0xFFF87171) else Color.White,
            fontSize = 10.sp,
            fontWeight = FontWeight.Bold
          )
        }
      }
    }
    return
  }

  val scale = (widthDp / 264f).coerceIn(0.80f, 1.50f)

  Surface(
    modifier = Modifier
      .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
      .width(widthDp.dp)
      .pointerInput(Unit) {
        detectDragGestures { change, drag -> change.consume(); offsetX += drag.x; offsetY += drag.y }
      },
    shape = RoundedCornerShape(18.dp),
    shadowElevation = 12.dp,
    color = Color(0xFF282B30)
  ) {
    Box {
      Column(modifier = Modifier.padding(start = 12.dp, end = 12.dp, top = 10.dp, bottom = 16.dp), verticalArrangement = Arrangement.spacedBy((9f * scale).dp)) {
        // Header: ⋮⋮ Timer   🔔   ∨   ×
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.DragIndicator, contentDescription = null, tint = Color(0xFF9CA3AF), modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text(stringResource(R.string.tool_timer), color = Color(0xFFD1D5DB), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, modifier = Modifier.weight(1f))
          IconButton(onClick = { soundOn = !soundOn }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Notifications, contentDescription = "Bell", tint = if (soundOn) Color.White else Color(0xFF6B7280), modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = { minimized = true }, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.KeyboardArrowDown, contentDescription = "Minimize", tint = Color(0xFFD1D5DB), modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFFD1D5DB), modifier = Modifier.size(18.dp))
          }
        }

        // -  00:30  +
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceEvenly) {
          IconButton(
            onClick = { if (!running && totalSeconds > 5) { totalSeconds -= 5; remaining = totalSeconds } },
            modifier = Modifier.size((36f * scale).dp).clip(CircleShape).background(Color(0xFF3C4048))
          ) {
            Icon(Icons.Default.Remove, contentDescription = "-5s", tint = Color.White, modifier = Modifier.size((18f * scale).dp))
          }
          Text(
            text = String.format("%02d:%02d", remaining / 60, remaining % 60),
            color = if (remaining <= 5 && remaining > 0) Color(0xFFF87171) else Color.White,
            fontSize = (31f * scale).sp,
            fontWeight = FontWeight.Bold
          )
          IconButton(
            onClick = { if (!running && totalSeconds < 300) { totalSeconds += 5; remaining = totalSeconds } },
            modifier = Modifier.size((36f * scale).dp).clip(CircleShape).background(Color(0xFF3C4048))
          ) {
            Icon(Icons.Default.Add, contentDescription = "+5s", tint = Color.White, modifier = Modifier.size((18f * scale).dp))
          }
        }

        // Progress bar
        LinearProgressIndicator(
          progress = { (remaining.toFloat() / totalSeconds.coerceAtLeast(1)).coerceIn(0f, 1f) },
          modifier = Modifier.fillMaxWidth().height(5.dp).clip(RoundedCornerShape(4.dp)),
          color = Color(0xFF4ADE80),
          trackColor = Color(0xFF4B5563)
        )

        val chipH = (31f * scale).dp
        // 2 rows of preset chips + bright green ▶ play pill!
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf(5, 10, 15, 20).forEach { sec ->
            TimerPresetPill(sec, totalSeconds == sec, Modifier.weight(1f), chipH) {
              totalSeconds = sec; remaining = sec; running = false
            }
          }
        }
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
          listOf(30, 45, 60).forEach { sec ->
            TimerPresetPill(sec, totalSeconds == sec, Modifier.weight(1f), chipH) {
              totalSeconds = sec; remaining = sec; running = false
            }
          }
          Surface(
            modifier = Modifier
              .weight(1f)
              .height(chipH)
              .clip(RoundedCornerShape(16.dp))
              .clickable {
                if (remaining == 0) remaining = totalSeconds
                running = !running
              },
            color = if (running) Color(0xFFEF4444) else Color(0xFF4ADE80)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(if (running) Icons.Default.Pause else Icons.Default.PlayArrow, contentDescription = "Start/Pause", tint = Color(0xFF0F172A), modifier = Modifier.size(18.dp))
            }
          }
        }
      }

      // Bottom-right diagonal resize grip (///)
      ResizeCornerGrip(
        color = Color(0xFF9CA3AF),
        modifier = Modifier.align(Alignment.BottomEnd),
        onDragDeltaPx = { dx ->
          val dDp = with(density) { dx.toDp().value }
          widthDp = (widthDp + dDp).coerceIn(205f, 380f)
        }
      )
    }
  }
}

@Composable
private fun ResizeCornerGrip(
  color: Color,
  modifier: Modifier = Modifier,
  onDragDeltaPx: (Float) -> Unit
) {
  Box(
    modifier = modifier
      .size(36.dp)
      .pointerInput(Unit) {
        detectDragGestures { change, drag ->
          change.consume()
          onDragDeltaPx((drag.x + drag.y) * 0.7f)
        }
      }
      .padding(start = 16.dp, top = 16.dp, end = 6.dp, bottom = 6.dp)
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      val w = size.width
      val h = size.height
      drawLine(color, Offset(w * 0.20f, h), Offset(w, h * 0.20f), strokeWidth = 2.6f)
      drawLine(color, Offset(w * 0.52f, h), Offset(w, h * 0.52f), strokeWidth = 2.6f)
      drawLine(color, Offset(w * 0.82f, h), Offset(w, h * 0.82f), strokeWidth = 2.6f)
    }
  }
}

@Composable
private fun TimerPresetPill(
  sec: Int,
  selected: Boolean,
  modifier: Modifier,
  height: androidx.compose.ui.unit.Dp = 32.dp,
  onClick: () -> Unit
) {
  Surface(
    modifier = modifier.height(height).clip(RoundedCornerShape(16.dp)).clickable(onClick = onClick),
    color = if (selected) Color.White else Color(0xFF3C4048)
  ) {
    Box(contentAlignment = Alignment.Center) {
      Text("$sec", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = if (selected) Color(0xFF111827) else Color(0xFFE5E7EB))
    }
  }
}

// ===== 4. Floating Media3 ExoPlayer Video Overlay (F-26) =====

@Composable
fun FloatingVideoOverlay(videoUri: Uri, onClose: () -> Unit) {
  val context = LocalContext.current
  var offsetX by remember { mutableFloatStateOf(40f) }
  var offsetY by remember { mutableFloatStateOf(160f) }
  var widthDp by remember { mutableFloatStateOf(270f) }

  val exoPlayer = remember(videoUri) {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(MediaItem.fromUri(videoUri))
      prepare()
      playWhenReady = true
    }
  }
  DisposableEffect(exoPlayer) { onDispose { exoPlayer.release() } }

  Surface(
    modifier = Modifier
      .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
      .width(widthDp.dp)
      .pointerInput(Unit) {
        detectDragGestures { change, drag -> change.consume(); offsetX += drag.x; offsetY += drag.y }
      },
    shape = RoundedCornerShape(14.dp),
    shadowElevation = 10.dp,
    color = Color.Black
  ) {
    Column {
      Row(
        modifier = Modifier.fillMaxWidth().background(TeachColors.DarkPillBg).padding(horizontal = 8.dp, vertical = 2.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(stringResource(R.string.tool_video), color = Color.White, fontSize = 12.sp, modifier = Modifier.weight(1f))
        TextButton(onClick = { widthDp = if (widthDp < 320f) 340f else 240f }) {
          Text(if (widthDp < 320f) "+" else "−", color = Color.White)
        }
        IconButton(onClick = onClose, modifier = Modifier.size(28.dp)) {
          Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(16.dp))
        }
      }
      AndroidView(
        modifier = Modifier.fillMaxWidth().height((widthDp * 0.58f).dp),
        factory = { ctx ->
          PlayerView(ctx).apply {
            player = exoPlayer
            useController = true
            layoutParams = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
          }
        },
        update = { it.player = exoPlayer }
      )
    }
  }
}

// ===== 5. CameraX Face Cam Bubble (F-27) =====

@Composable
fun FaceCamBubble(shape: FaceCamShape, onClose: () -> Unit) {
  val lifecycleOwner = LocalLifecycleOwner.current
  var offsetX by remember { mutableFloatStateOf(40f) }
  var offsetY by remember { mutableFloatStateOf(440f) }
  val clipShape = if (shape == FaceCamShape.ROUND) CircleShape else RoundedCornerShape(16.dp)

  var previewRef by remember { mutableStateOf<androidx.camera.view.PreviewView?>(null) }
  var cachedBitmap by remember { mutableStateOf<Bitmap?>(null) }

  androidx.compose.runtime.DisposableEffect(Unit) {
    com.teachboard.app.util.RecorderController.faceCamBitmapSupplier = { cachedBitmap }
    onDispose {
      com.teachboard.app.util.RecorderController.faceCamBitmapSupplier = null
    }
  }

  androidx.compose.runtime.LaunchedEffect(previewRef) {
    val pv = previewRef ?: return@LaunchedEffect
    while (isActive) {
      delay(66L)
      val bmp = runCatching { pv.bitmap }.getOrNull()
      if (bmp != null) cachedBitmap = bmp
    }
  }

  Box(
    modifier = Modifier
      .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
      .size(126.dp)
      .clip(clipShape)
      .border(2.5.dp, Color.White, clipShape)
      .pointerInput(Unit) {
        detectDragGestures { change, drag -> change.consume(); offsetX += drag.x; offsetY += drag.y }
      }
  ) {
    AndroidView(
      modifier = Modifier.fillMaxSize(),
      factory = { ctx ->
        PreviewView(ctx).apply {
          scaleType = PreviewView.ScaleType.FILL_CENTER
          previewRef = this
          val future = ProcessCameraProvider.getInstance(ctx)
          future.addListener({
            runCatching {
              val provider = future.get()
              val preview = Preview.Builder().build().also { it.surfaceProvider = surfaceProvider }
              provider.unbindAll()
              provider.bindToLifecycle(lifecycleOwner, CameraSelector.DEFAULT_FRONT_CAMERA, preview)
            }
          }, ContextCompat.getMainExecutor(ctx))
        }
      }
    )
    IconButton(
      onClick = onClose,
      modifier = Modifier.align(Alignment.TopEnd).padding(4.dp).size(24.dp).clip(CircleShape).background(Color(0x99000000))
    ) {
      Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(14.dp))
    }
  }
}

// ===== 6. Recording Floating Pill =====

@Composable
fun RecordingFloatingPill(
  elapsedSeconds: Int,
  isPaused: Boolean,
  onTogglePause: () -> Unit,
  onStop: () -> Unit
) {
  Surface(
    shape = RoundedCornerShape(24.dp),
    color = Color(0xCC0B1220),
    shadowElevation = 6.dp
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Box(
        modifier = Modifier
          .size(10.dp)
          .clip(CircleShape)
          .background(if (isPaused) Color(0xFFF59E0B) else Color(0xFFE11D48))
      )
      Text(
        String.format(java.util.Locale.US, "%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60),
        color = Color(0xFFFBBF24),
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
      )
      IconButton(onClick = onTogglePause, modifier = Modifier.size(28.dp)) {
        Icon(
          if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause,
          contentDescription = "Pause/Resume",
          tint = Color.White,
          modifier = Modifier.size(18.dp)
        )
      }
      IconButton(onClick = onStop, modifier = Modifier.size(28.dp)) {
        Icon(
          Icons.Default.Stop,
          contentDescription = "Stop",
          tint = Color(0xFFE11D48),
          modifier = Modifier.size(20.dp)
        )
      }
    }
  }
}

// ===== 7. Onboarding Tour Overlay (F-35) =====

@Composable
fun OnboardingTourDialog(onFinish: () -> Unit) {
  var step by remember { mutableIntStateOf(0) }
  val steps = listOf(
    stringResource(R.string.tour_step1_title) to stringResource(R.string.tour_step1_desc),
    stringResource(R.string.tour_step2_title) to stringResource(R.string.tour_step2_desc),
    stringResource(R.string.tour_step3_title) to stringResource(R.string.tour_step3_desc),
    stringResource(R.string.tour_step4_title) to stringResource(R.string.tour_step4_desc)
  )

  PastelModalDialog(
    title = "${stringResource(R.string.welcome_tour)} (${step + 1}/${steps.size})",
    icon = Icons.Default.Create,
    badgeColor = TeachColors.Primary,
    headerTint = TeachColors.HeaderSkyTint,
    onDismiss = onFinish,
    footer = {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TextButton(onClick = onFinish) { Text(stringResource(R.string.skip), color = Color(0xFF64748B)) }
        Button(onClick = { if (step < steps.lastIndex) step++ else onFinish() }) {
          Text(if (step < steps.lastIndex) stringResource(R.string.next) else stringResource(R.string.get_started))
        }
      }
    }
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text(steps[step].first, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
      Text(steps[step].second, style = MaterialTheme.typography.bodyLarge, color = Color(0xFF334155))
    }
  }
}

// ===== 8. Floating / Split / Fullscreen PDF Viewer Overlay (Continuous Vertical Scroll) =====

@Composable
fun PdfViewerOverlay(
  pdfFile: File,
  pageCount: Int,
  currentPage: Int,
  mode: PdfViewMode,
  activeBrushColor: Color = Color(0xFF38BDF8),
  activeTool: ToolType = ToolType.Pen,
  brushColor: Int = 0xFFFACC15.toInt(),
  strokeWidth: Float = 12f,
  pdfStrokes: Map<Int, List<PdfAnnotationStroke>> = emptyMap(),
  onAddStroke: (pageIndex: Int, tool: ToolType, color: Int, strokeWidth: Float, alpha: Float, points: List<StrokePoint>) -> Unit = { _, _, _, _, _, _ -> },
  onEraseStroke: (pageIndex: Int, normX: Float, normY: Float, normRadius: Float) -> Unit = { _, _, _, _ -> },
  onModeChange: (PdfViewMode) -> Unit,
  onPinPageToBoard: (Int) -> Unit,
  onPickNewPdf: () -> Unit,
  onClose: () -> Unit
) {
  val config = LocalConfiguration.current
  val density = LocalDensity.current
  val isLandscape = config.screenWidthDp > config.screenHeightDp

  // Window position & size state for WINDOW mode
  val defaultWidthDp = if (isLandscape) 440f else 320f
  val defaultHeightDp = if (isLandscape) 470f else 490f

  var widthDp by remember { mutableFloatStateOf(defaultWidthDp) }
  var heightDp by remember { mutableFloatStateOf(defaultHeightDp) }

  var offsetX by remember {
    mutableFloatStateOf(
      with(density) {
        if (isLandscape) 50.dp.toPx()
        else 16.dp.toPx()
      }
    )
  }
  var offsetY by remember {
    mutableFloatStateOf(
      with(density) {
        if (isLandscape) 60.dp.toPx()
        else 90.dp.toPx()
      }
    )
  }

  var minimized by remember { mutableStateOf(false) }
  val listState = rememberLazyListState()
  val scope = rememberCoroutineScope()

  val visiblePage = remember {
    derivedStateOf { listState.firstVisibleItemIndex }
  }

  if (minimized) {
    // Draggable circular Red PDF bubble (tap to restore!)
    Surface(
      modifier = Modifier
        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        .size(56.dp)
        .pointerInput(Unit) {
          detectDragGestures { change, drag ->
            change.consume()
            val maxW = with(density) { (config.screenWidthDp - 60).dp.toPx() }
            val maxH = with(density) { (config.screenHeightDp - 60).dp.toPx() }
            offsetX = (offsetX + drag.x).coerceIn(0f, maxW)
            offsetY = (offsetY + drag.y).coerceIn(0f, maxH)
          }
        }
        .clickable { minimized = false },
      shape = CircleShape,
      color = Color(0xFFEF4444),
      shadowElevation = 10.dp
    ) {
      Box(contentAlignment = Alignment.Center) {
        Icon(
          Icons.Default.PictureAsPdf,
          contentDescription = "Expand PDF",
          tint = Color.White,
          modifier = Modifier.size(26.dp)
        )
      }
    }
    return
  }

  val containerModifier = when (mode) {
    PdfViewMode.WINDOW -> {
      Modifier
        .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
        .size(widthDp.dp, heightDp.dp)
    }
    PdfViewMode.SPLIT_LEFT -> {
      Modifier
        .fillMaxHeight()
        .fillMaxWidth()
    }
    PdfViewMode.SPLIT_RIGHT -> {
      Modifier
        .fillMaxHeight()
        .fillMaxWidth()
    }
    PdfViewMode.FULLSCREEN -> {
      Modifier.fillMaxSize()
    }
  }

  val containerShape = when (mode) {
    PdfViewMode.WINDOW -> RoundedCornerShape(16.dp)
    PdfViewMode.SPLIT_LEFT -> RoundedCornerShape(topEnd = 16.dp, bottomEnd = 16.dp)
    PdfViewMode.SPLIT_RIGHT -> RoundedCornerShape(topStart = 16.dp, bottomStart = 16.dp)
    PdfViewMode.FULLSCREEN -> RoundedCornerShape(0.dp)
  }

  Surface(
    modifier = containerModifier,
    shape = containerShape,
    shadowElevation = if (mode == PdfViewMode.WINDOW) 12.dp else 4.dp,
    color = Color(0xFFF8FAFC),
    border = if (mode == PdfViewMode.WINDOW) androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE2E8F0)) else null
  ) {
    Column(modifier = Modifier.fillMaxSize()) {
      // ===== 1. Header Bar (draggable handle to move floating window) =====
      Surface(
        color = Color(0xFF0F172A),
        modifier = Modifier
          .fillMaxWidth()
          .then(
            if (mode == PdfViewMode.WINDOW) {
              Modifier.pointerInput(Unit) {
                detectDragGestures { change, drag ->
                  change.consume()
                  val maxW = with(density) { (config.screenWidthDp - 80).dp.toPx() }
                  val maxH = with(density) { (config.screenHeightDp - 80).dp.toPx() }
                  offsetX = (offsetX + drag.x).coerceIn(0f, maxW)
                  offsetY = (offsetY + drag.y).coerceIn(0f, maxH)
                }
              }
            } else Modifier
          )
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 6.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(
            Icons.Default.PictureAsPdf,
            contentDescription = null,
            tint = Color(0xFFF87171),
            modifier = Modifier.size(18.dp)
          )

          // Document title
          Text(
            pdfFile.nameWithoutExtension,
            color = Color.White,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f)
          )

          // Compact Page Stepper in Header: [▲] 1/5 [▼]
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = Color(0xFF1E293B)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(1.dp),
              modifier = Modifier.padding(horizontal = 4.dp, vertical = 1.dp)
            ) {
              IconButton(
                onClick = {
                  val target = (visiblePage.value - 1).coerceAtLeast(0)
                  scope.launch { listState.animateScrollToItem(target) }
                },
                enabled = visiblePage.value > 0,
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  Icons.Default.KeyboardArrowUp,
                  contentDescription = "Previous Page",
                  tint = if (visiblePage.value > 0) Color.White else Color(0xFF64748B),
                  modifier = Modifier.size(15.dp)
                )
              }

              Text(
                "${visiblePage.value + 1}/$pageCount",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(horizontal = 2.dp)
              )

              IconButton(
                onClick = {
                  val target = (visiblePage.value + 1).coerceAtMost(pageCount - 1)
                  scope.launch { listState.animateScrollToItem(target) }
                },
                enabled = visiblePage.value < pageCount - 1,
                modifier = Modifier.size(24.dp)
              ) {
                Icon(
                  Icons.Default.KeyboardArrowDown,
                  contentDescription = "Next Page",
                  tint = if (visiblePage.value < pageCount - 1) Color.White else Color(0xFF64748B),
                  modifier = Modifier.size(15.dp)
                )
              }
            }
          }

          // Pin current page to board
          IconButton(
            onClick = { onPinPageToBoard(visiblePage.value) },
            modifier = Modifier.size(26.dp)
          ) {
            Icon(
              Icons.Default.PushPin,
              contentDescription = "Pin to Board",
              tint = activeBrushColor,
              modifier = Modifier.size(15.dp)
            )
          }

          // Sleek Segmented Mode Switcher (Window | Split | Fullscreen)
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = Color(0xFF1E293B)
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              modifier = Modifier.padding(2.dp)
            ) {
              // 1. Window Mode
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (mode == PdfViewMode.WINDOW) activeBrushColor.copy(alpha = 0.25f) else Color.Transparent)
                  .clickable { onModeChange(PdfViewMode.WINDOW) },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.OpenInNew,
                  contentDescription = "Window Mode",
                  tint = if (mode == PdfViewMode.WINDOW) activeBrushColor else Color(0xFF94A3B8),
                  modifier = Modifier.size(13.dp)
                )
              }

              // 2. Split Mode
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (mode == PdfViewMode.SPLIT_LEFT || mode == PdfViewMode.SPLIT_RIGHT) activeBrushColor.copy(alpha = 0.25f) else Color.Transparent)
                  .clickable {
                    val nextSplit = if (mode == PdfViewMode.SPLIT_LEFT) PdfViewMode.SPLIT_RIGHT else PdfViewMode.SPLIT_LEFT
                    onModeChange(nextSplit)
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.VerticalSplit,
                  contentDescription = "Split Mode",
                  tint = if (mode == PdfViewMode.SPLIT_LEFT || mode == PdfViewMode.SPLIT_RIGHT) activeBrushColor else Color(0xFF94A3B8),
                  modifier = Modifier.size(13.dp)
                )
              }

              // 3. Fullscreen Mode
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(RoundedCornerShape(6.dp))
                  .background(if (mode == PdfViewMode.FULLSCREEN) activeBrushColor.copy(alpha = 0.25f) else Color.Transparent)
                  .clickable {
                    if (mode == PdfViewMode.FULLSCREEN) onModeChange(PdfViewMode.WINDOW)
                    else onModeChange(PdfViewMode.FULLSCREEN)
                  },
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  if (mode == PdfViewMode.FULLSCREEN) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                  contentDescription = "Fullscreen Mode",
                  tint = if (mode == PdfViewMode.FULLSCREEN) activeBrushColor else Color(0xFF94A3B8),
                  modifier = Modifier.size(14.dp)
                )
              }
            }
          }

          // Minimize button (WINDOW mode)
          if (mode == PdfViewMode.WINDOW) {
            IconButton(
              onClick = { minimized = true },
              modifier = Modifier.size(26.dp)
            ) {
              Icon(Icons.Default.Remove, contentDescription = "Minimize", tint = Color.White, modifier = Modifier.size(15.dp))
            }
          }

          // Close button
          IconButton(
            onClick = onClose,
            modifier = Modifier.size(26.dp)
          ) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(15.dp))
          }
        }
      }

      // ===== 2. Continuous Vertical Scroll Area (Swipe Up / Down) =====
      Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
        LazyColumn(
          state = listState,
          modifier = Modifier.fillMaxSize(),
          verticalArrangement = Arrangement.spacedBy(10.dp),
          contentPadding = PaddingValues(10.dp)
        ) {
          items(pageCount) { pIdx ->
            PdfContinuousPageItem(
              pdfFile = pdfFile,
              pageIndex = pIdx,
              activeTool = activeTool,
              brushColor = brushColor,
              strokeWidth = strokeWidth,
              pageStrokes = pdfStrokes[pIdx] ?: emptyList(),
              onAddStroke = onAddStroke,
              onEraseStroke = onEraseStroke
            )
          }
        }
      }

      // ===== 3. Bottom Utility Bar in Window Mode (Resize handle + actions) =====
      if (mode == PdfViewMode.WINDOW) {
        Surface(
          color = Color(0xFFF1F5F9),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            TextButton(onClick = onPickNewPdf) {
              Text("Open different PDF", fontSize = 11.sp, color = Color(0xFF0284C7))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
              TextButton(
                onClick = {
                  widthDp = if (widthDp < 500f) 540f else 420f
                  heightDp = if (heightDp < 500f) 580f else 460f
                }
              ) {
                Text(if (widthDp < 500f) "Enlarge" else "Standard", fontSize = 11.sp, color = Color(0xFF475569))
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun PdfContinuousPageItem(
  pdfFile: File,
  pageIndex: Int,
  activeTool: ToolType,
  brushColor: Int,
  strokeWidth: Float,
  pageStrokes: List<PdfAnnotationStroke>,
  onAddStroke: (pageIndex: Int, tool: ToolType, color: Int, strokeWidth: Float, alpha: Float, points: List<StrokePoint>) -> Unit,
  onEraseStroke: (pageIndex: Int, normX: Float, normY: Float, normRadius: Float) -> Unit
) {
  var bitmap by remember(pdfFile.absolutePath, pageIndex) { mutableStateOf<Bitmap?>(null) }
  var loading by remember(pdfFile.absolutePath, pageIndex) { mutableStateOf(true) }
  var livePoints by remember { mutableStateOf<List<StrokePoint>>(emptyList()) }

  LaunchedEffect(pdfFile.absolutePath, pageIndex) {
    loading = true
    val bmp = LazyPdfCache.renderPage(pdfFile, pageIndex, maxLongSide = 1200)
    bitmap = bmp
    loading = false
  }

  Surface(
    shape = RoundedCornerShape(8.dp),
    color = Color.White,
    shadowElevation = 2.dp,
    modifier = Modifier
      .fillMaxWidth()
      .wrapContentHeight()
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .wrapContentHeight(),
      contentAlignment = Alignment.Center
    ) {
      if (bitmap != null && !bitmap!!.isRecycled) {
        val bmp = bitmap!!
        val aspect = bmp.width.toFloat() / bmp.height.toFloat()
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .aspectRatio(aspect)
            .clip(RoundedCornerShape(8.dp))
        ) {
          // 1. Rendered PDF Page Background
          Image(
            bitmap = bmp.asImageBitmap(),
            contentDescription = "Page ${pageIndex + 1}",
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Fit
          )

          // 2. Interactive Annotation Layer (Pen, Highlighter, Eraser)
          val isDrawingTool = activeTool == ToolType.Pen || activeTool == ToolType.Highlighter || activeTool == ToolType.Eraser
          Canvas(
            modifier = Modifier
              .fillMaxSize()
              .then(
                if (isDrawingTool) {
                  Modifier.pointerInput(activeTool, brushColor, strokeWidth, pageIndex) {
                    detectDragGestures(
                      onDragStart = { offset ->
                        if (size.width > 0 && size.height > 0) {
                          val normX = (offset.x / size.width).coerceIn(0f, 1f)
                          val normY = (offset.y / size.height).coerceIn(0f, 1f)
                          if (activeTool == ToolType.Eraser) {
                            onEraseStroke(pageIndex, normX, normY, 28.dp.toPx() / size.width)
                          } else {
                            livePoints = listOf(StrokePoint(normX, normY))
                          }
                        }
                      },
                      onDrag = { change, _ ->
                        change.consume()
                        if (size.width > 0 && size.height > 0) {
                          val normX = (change.position.x / size.width).coerceIn(0f, 1f)
                          val normY = (change.position.y / size.height).coerceIn(0f, 1f)
                          if (activeTool == ToolType.Eraser) {
                            onEraseStroke(pageIndex, normX, normY, 28.dp.toPx() / size.width)
                          } else {
                            livePoints = livePoints + StrokePoint(normX, normY)
                          }
                        }
                      },
                      onDragEnd = {
                        if (activeTool != ToolType.Eraser && livePoints.isNotEmpty()) {
                          onAddStroke(
                            pageIndex,
                            activeTool,
                            brushColor,
                            strokeWidth,
                            if (activeTool == ToolType.Highlighter) 0.4f else 1f,
                            livePoints
                          )
                        }
                        livePoints = emptyList()
                      },
                      onDragCancel = {
                        livePoints = emptyList()
                      }
                    )
                  }
                } else Modifier
              )
          ) {
            val pageW = size.width
            val pageH = size.height

            // Render committed annotations on this page
            pageStrokes.forEach { stroke ->
              val isHighlighter = stroke.tool == ToolType.Highlighter
              val sw = if (isHighlighter) (stroke.strokeWidth * 2.2f).coerceAtLeast(18f) else stroke.strokeWidth
              val strokeCol = Color(stroke.color).copy(alpha = stroke.alpha)

              if (stroke.points.size >= 2) {
                val path = Path().apply {
                  moveTo(stroke.points[0].x * pageW, stroke.points[0].y * pageH)
                  for (i in 1 until stroke.points.size) {
                    lineTo(stroke.points[i].x * pageW, stroke.points[i].y * pageH)
                  }
                }
                drawPath(
                  path = path,
                  color = strokeCol,
                  style = Stroke(
                    width = sw,
                    cap = StrokeCap.Round,
                    join = StrokeJoin.Round
                  )
                )
              } else if (stroke.points.size == 1) {
                drawCircle(
                  color = strokeCol,
                  radius = (sw / 2f).coerceAtLeast(3f),
                  center = Offset(stroke.points[0].x * pageW, stroke.points[0].y * pageH)
                )
              }
            }

            // Render live stroke in progress
            if (livePoints.size >= 2) {
              val isHighlighter = activeTool == ToolType.Highlighter
              val sw = if (isHighlighter) (strokeWidth * 2.2f).coerceAtLeast(18f) else strokeWidth
              val strokeCol = Color(brushColor).copy(alpha = if (isHighlighter) 0.4f else 1f)

              val path = Path().apply {
                moveTo(livePoints[0].x * pageW, livePoints[0].y * pageH)
                for (i in 1 until livePoints.size) {
                  lineTo(livePoints[i].x * pageW, livePoints[i].y * pageH)
                }
              }
              drawPath(
                path = path,
                color = strokeCol,
                style = Stroke(
                  width = sw,
                  cap = StrokeCap.Round,
                  join = StrokeJoin.Round
                )
              )
            } else if (livePoints.size == 1) {
              val isHighlighter = activeTool == ToolType.Highlighter
              val sw = if (isHighlighter) (strokeWidth * 2.2f).coerceAtLeast(18f) else strokeWidth
              val strokeCol = Color(brushColor).copy(alpha = if (isHighlighter) 0.4f else 1f)

              drawCircle(
                color = strokeCol,
                radius = (sw / 2f).coerceAtLeast(3f),
                center = Offset(livePoints[0].x * pageW, livePoints[0].y * pageH)
              )
            }
          }
        }
      } else {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .background(Color(0xFFF8FAFC)),
          contentAlignment = Alignment.Center
        ) {
          if (loading) {
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.5.dp,
                color = Color(0xFFEF4444)
              )
              Text("Rendering Page ${pageIndex + 1}…", fontSize = 11.sp, color = Color(0xFF64748B))
            }
          } else {
            Text("Page ${pageIndex + 1}", fontSize = 12.sp, color = Color(0xFF94A3B8))
          }
        }
      }
    }
  }
}

@Composable
fun CountdownOverlay(
  count: Int,
  onCancel: () -> Unit
) {
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0x66000000))
      .clickable { onCancel() },
    contentAlignment = Alignment.Center
  ) {
    Surface(
      shape = CircleShape,
      color = Color(0xE6E11D48),
      shadowElevation = 12.dp,
      modifier = Modifier.size(130.dp)
    ) {
      Box(contentAlignment = Alignment.Center) {
        Text(
          text = count.toString(),
          fontSize = 68.sp,
          fontWeight = FontWeight.Black,
          color = Color.White
        )
      }
    }
  }
}
