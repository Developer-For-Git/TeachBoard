package com.teachboard.app.ui.canvas

import android.media.RingtoneManager
import android.net.Uri
import android.view.ViewGroup
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material.icons.filled.Straighten
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.Button
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
import com.teachboard.app.domain.model.Contour
import com.teachboard.app.domain.model.FaceCamShape
import com.teachboard.app.domain.model.GeometryToolType
import com.teachboard.app.domain.model.StrokePoint
import com.teachboard.app.ui.theme.TeachColors
import kotlinx.coroutines.delay
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.roundToInt
import kotlin.math.sin

// ===== 1. Geometry Box Bottom-Docked Card (Matching ref_21_geometry.png!) =====

@Composable
fun GeometryPickerDialog(
  activeTool: GeometryToolType?,
  onSelect: (GeometryToolType?) -> Unit,
  onDismiss: () -> Unit
) {
  BottomDockCard(
    title = stringResource(R.string.geometry_box_title),
    icon = Icons.Default.SquareFoot,
    badgeColor = TeachColors.AccentAmber,
    headerTint = TeachColors.HeaderAmberTint,
    onDismiss = onDismiss
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
      val items = listOf(
        Triple(GeometryToolType.RULER, "Ruler", Icons.Default.Straighten),
        Triple(GeometryToolType.PROTRACTOR, "Protractor", Icons.Default.SquareFoot),
        Triple(GeometryToolType.COMPASS, "Compass", Icons.Default.Create),
        Triple(
          if (activeTool == GeometryToolType.SET_SQUARE_30_60) GeometryToolType.SET_SQUARE_45_45 else GeometryToolType.SET_SQUARE_30_60,
          "Set Square",
          Icons.Default.SquareFoot
        )
      )
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items.forEach { (type, label, icon) ->
          val isSel = activeTool == type || (label == "Set Square" && (activeTool == GeometryToolType.SET_SQUARE_30_60 || activeTool == GeometryToolType.SET_SQUARE_45_45))
          Surface(
            modifier = Modifier
              .weight(1f)
              .height(78.dp)
              .clip(RoundedCornerShape(14.dp))
              .border(if (isSel) 2.dp else 1.dp, if (isSel) TeachColors.AccentAmber else TeachColors.TrayBorder, RoundedCornerShape(14.dp))
              .clickable {
                onSelect(if (isSel && label != "Set Square") null else type)
                onDismiss()
              },
            color = if (isSel) TeachColors.HeaderAmberTint else Color.White
          ) {
            Column(
              modifier = Modifier.fillMaxSize().padding(6.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(icon, contentDescription = label, tint = TeachColors.AccentAmber, modifier = Modifier.size(24.dp))
              Spacer(Modifier.height(6.dp))
              Text(label, fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color(0xFF1E293B), maxLines = 1)
            }
          }
        }
      }
      if (activeTool != null) {
        TextButton(onClick = { onSelect(null); onDismiss() }, modifier = Modifier.align(Alignment.CenterHorizontally)) {
          Text(stringResource(R.string.hide_geometry_tool), color = TeachColors.AccentAmber, fontSize = 12.sp)
        }
      }
    }
  }
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
  var offsetX by remember { mutableFloatStateOf(90f) }
  var offsetY by remember { mutableFloatStateOf(260f) }
  var rotationDeg by remember { mutableFloatStateOf(0f) }
  var angleArmDeg by remember { mutableFloatStateOf(60f) }
  var compassRadius by remember { mutableFloatStateOf(160f) }

  fun screenToWorld(sx: Float, sy: Float): StrokePoint =
    StrokePoint((sx - panX) / zoom.coerceAtLeast(0.1f), (sy - panY) / zoom.coerceAtLeast(0.1f))

  Box(
    modifier = Modifier
      .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
      .pointerInput(tool) {
        detectDragGestures { change, drag -> change.consume(); offsetX += drag.x; offsetY += drag.y }
      }
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Box(modifier = Modifier.rotate(rotationDeg)) {
        when (tool) {
          GeometryToolType.RULER -> {
            Canvas(modifier = Modifier.width(300.dp).height(62.dp)) {
              drawRect(TeachColors.GeometryWoodFill)
              drawRect(TeachColors.GeometryWoodBorder, style = Stroke(3f))
              for (i in 0..30) {
                val x = 14f + i * (size.width - 28f) / 30f
                val h = if (i % 5 == 0) 24f else 13f
                drawLine(TeachColors.GeometryTickMark, Offset(x, 0f), Offset(x, h), strokeWidth = 2f)
              }
            }
          }
          GeometryToolType.PROTRACTOR -> {
            Canvas(modifier = Modifier.width(240.dp).height(130.dp)) {
              val r = size.width / 2f - 8f
              val cx = size.width / 2f; val cy = size.height - 10f
              drawArc(TeachColors.GeometryWoodFill, 180f, 180f, true, Offset(cx - r, cy - r), Size(r * 2, r * 2))
              drawArc(TeachColors.GeometryWoodBorder, 180f, 180f, true, Offset(cx - r, cy - r), Size(r * 2, r * 2), style = Stroke(3f))
              for (deg in 0..180 step 10) {
                val rad = Math.toRadians(180.0 + deg)
                val inner = if (deg % 30 == 0) r * 0.80f else r * 0.88f
                drawLine(TeachColors.GeometryTickMark, Offset(cx + cos(rad).toFloat() * inner, cy + sin(rad).toFloat() * inner), Offset(cx + cos(rad).toFloat() * r, cy + sin(rad).toFloat() * r), strokeWidth = 2f)
              }
              val armRad = Math.toRadians(180.0 + angleArmDeg)
              drawLine(TeachColors.GeometryAngleArm, Offset(cx, cy), Offset(cx + cos(armRad).toFloat() * r, cy + sin(armRad).toFloat() * r), strokeWidth = 4f)
            }
          }
          GeometryToolType.COMPASS -> {
            Canvas(modifier = Modifier.size(210.dp)) {
              val cx = size.width / 2f; val cy = size.height / 2f
              val r = (compassRadius * 0.5f).coerceIn(30f, size.width * 0.46f)
              drawCircle(TeachColors.GeometryWoodFill.copy(alpha = 0.35f), r, Offset(cx, cy))
              drawCircle(TeachColors.GeometryWoodBorder, r, Offset(cx, cy), style = Stroke(3f))
              drawLine(TeachColors.GeometryAngleArm, Offset(cx, cy), Offset(cx + r, cy), strokeWidth = 3f)
              drawCircle(TeachColors.GeometryTickMark, 6f, Offset(cx, cy))
            }
          }
          GeometryToolType.SET_SQUARE_30_60, GeometryToolType.SET_SQUARE_45_45 -> {
            val is45 = tool == GeometryToolType.SET_SQUARE_45_45
            Canvas(modifier = Modifier.width(if (is45) 180.dp else 210.dp).height(180.dp)) {
              val path = Path().apply { moveTo(8f, size.height - 8f); lineTo(size.width - 8f, size.height - 8f); lineTo(8f, 8f); close() }
              drawPath(path, TeachColors.GeometryWoodFill)
              drawPath(path, TeachColors.GeometryWoodBorder, style = Stroke(3f))
            }
          }
        }
      }

      Surface(shape = RoundedCornerShape(20.dp), color = TeachColors.DarkPillBg, modifier = Modifier.padding(top = 6.dp)) {
        Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          when (tool) {
            GeometryToolType.PROTRACTOR -> {
              Text("${angleArmDeg.roundToInt()}°", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Slider(value = angleArmDeg, onValueChange = { angleArmDeg = it }, valueRange = 0f..180f, modifier = Modifier.width(100.dp))
            }
            GeometryToolType.COMPASS -> {
              Text("r=${compassRadius.roundToInt()}", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
              Slider(value = compassRadius, onValueChange = { compassRadius = it }, valueRange = 50f..320f, modifier = Modifier.width(100.dp))
            }
            else -> Text("${rotationDeg.roundToInt()}°", color = Color.White, fontSize = 12.sp)
          }
          IconButton(onClick = { rotationDeg = (rotationDeg + 15f) % 360f }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.AutoMirrored.Filled.RotateRight, contentDescription = "Rotate", tint = Color.White, modifier = Modifier.size(18.dp))
          }
          IconButton(
            onClick = {
              val rad = Math.toRadians(rotationDeg.toDouble())
              val contour = when (tool) {
                GeometryToolType.COMPASS -> {
                  val cx = offsetX + 260f; val cy = offsetY + 260f
                  val pts = (0..64).map { i ->
                    val a = 2.0 * PI * i / 64.0
                    screenToWorld(cx + cos(a).toFloat() * compassRadius, cy + sin(a).toFloat() * compassRadius)
                  }
                  Contour(closed = true, points = pts)
                }
                GeometryToolType.PROTRACTOR -> {
                  val cx = offsetX + 300f; val cy = offsetY + 280f; val r = 240f
                  val a1 = Math.toRadians(rotationDeg.toDouble()); val a2 = Math.toRadians(rotationDeg.toDouble() - angleArmDeg.toDouble())
                  Contour(false, listOf(screenToWorld(cx + cos(a1).toFloat() * r, cy + sin(a1).toFloat() * r), screenToWorld(cx, cy), screenToWorld(cx + cos(a2).toFloat() * r, cy + sin(a2).toFloat() * r)))
                }
                else -> {
                  val cx = offsetX + 360f; val cy = offsetY + 120f; val half = 260f
                  Contour(false, listOf(screenToWorld(cx - cos(rad).toFloat() * half, cy - sin(rad).toFloat() * half), screenToWorld(cx + cos(rad).toFloat() * half, cy + sin(rad).toFloat() * half)))
                }
              }
              onDrawContour(contour)
            },
            modifier = Modifier.size(32.dp)
          ) {
            Icon(Icons.Default.Create, contentDescription = "Draw", tint = Color(0xFF4ADE80), modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = onClose, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White, modifier = Modifier.size(18.dp))
          }
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
  Surface(shape = RoundedCornerShape(24.dp), color = TeachColors.DarkPillBg, shadowElevation = 6.dp) {
    Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
      Box(modifier = Modifier.size(10.dp).clip(CircleShape).background(if (isPaused) TeachColors.AccentOrange else TeachColors.RecordRed))
      Text(String.format("%02d:%02d", elapsedSeconds / 60, elapsedSeconds % 60), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
      IconButton(onClick = onTogglePause, modifier = Modifier.size(28.dp)) {
        Icon(if (isPaused) Icons.Default.PlayArrow else Icons.Default.Pause, contentDescription = "Pause/Resume", tint = Color.White, modifier = Modifier.size(18.dp))
      }
      IconButton(onClick = onStop, modifier = Modifier.size(28.dp)) {
        Icon(Icons.Default.Stop, contentDescription = "Stop", tint = TeachColors.RecordRed, modifier = Modifier.size(18.dp))
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
