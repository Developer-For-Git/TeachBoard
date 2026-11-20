package com.teachboard.app.ui.canvas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.FormatAlignLeft
import androidx.compose.material.icons.automirrored.filled.FormatAlignRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AutoFixHigh
import androidx.compose.material.icons.filled.Bookmark
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Create
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.DocumentScanner
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.DragIndicator
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.FormatAlignCenter
import androidx.compose.material.icons.filled.FormatBold
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.FormatItalic
import androidx.compose.material.icons.filled.Functions
import androidx.compose.material.icons.filled.Highlight
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.TableChart
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.teachboard.app.R
import com.teachboard.app.domain.formula.FormulaParser
import com.teachboard.app.domain.ink.InkLanguage
import com.teachboard.app.domain.ink.InkModelState
import com.teachboard.app.domain.model.BackgroundStyle
import com.teachboard.app.domain.model.BoardLayer
import com.teachboard.app.domain.model.EraserMode
import com.teachboard.app.domain.model.FaceCamShape
import com.teachboard.app.domain.model.GeometryToolType
import com.teachboard.app.domain.model.OcrScanRecord
import com.teachboard.app.domain.model.RecordingQuality
import com.teachboard.app.domain.model.Serialization
import com.teachboard.app.domain.model.ToolType
import com.teachboard.app.domain.model.ToolbarAction
import com.teachboard.app.domain.ocr.OcrOutput
import com.teachboard.app.domain.ocr.OcrScript
import com.teachboard.app.domain.search.WikimediaImage
import com.teachboard.app.domain.search.WikimediaSearch
import com.teachboard.app.domain.stickers.StickerCatalog
import com.teachboard.app.domain.stickers.StickerGroup
import com.teachboard.app.ui.theme.AppConfig
import com.teachboard.app.ui.theme.TeachColors
import kotlinx.coroutines.launch
import kotlin.math.sin

// ===== Unified Soft-Pastel Modal Chrome (Dark-Mode Safe & Landscape Responsive!) =====

private val LightModalColorScheme = lightColorScheme(
  primary = Color(0xFF2563EB),
  onPrimary = Color.White,
  surface = Color.White,
  onSurface = Color(0xFF111827),
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = Color(0xFF475569),
  background = Color.White,
  onBackground = Color(0xFF111827),
  outline = Color(0xFFCBD5E1)
)

@Composable
fun PastelModalDialog(
  title: String,
  icon: ImageVector,
  badgeColor: Color,
  headerTint: Color,
  onDismiss: () -> Unit,
  footer: (@Composable () -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val config = LocalConfiguration.current
  val isLandscape = config.screenWidthDp > config.screenHeightDp
  val widthFrac = if (isLandscape) 0.62f else 0.92f
  val maxH = if (isLandscape) (config.screenHeightDp - 24).coerceAtLeast(280).dp else 690.dp

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    MaterialTheme(colorScheme = LightModalColorScheme) {
      Surface(
        modifier = Modifier
          .fillMaxWidth(widthFrac)
          .heightIn(max = maxH),
        shape = RoundedCornerShape(24.dp),
        color = Color.White,
        contentColor = Color(0xFF111827),
        shadowElevation = 12.dp
      ) {
        Column {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Brush.verticalGradient(listOf(headerTint, headerTint.copy(alpha = 0.45f))))
              .padding(horizontal = 16.dp, vertical = if (isLandscape) 10.dp else 13.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(30.dp)
                .clip(RoundedCornerShape(9.dp))
                .background(badgeColor),
              contentAlignment = Alignment.Center
            ) {
              Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(
              text = title,
              fontSize = 17.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF111827),
              modifier = Modifier.weight(1f)
            )
            IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
              Icon(Icons.Default.Close, contentDescription = stringResource(R.string.close), tint = Color(0xFF64748B), modifier = Modifier.size(20.dp))
            }
          }
          HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.8.dp)
          Box(modifier = Modifier.weight(1f, fill = false).padding(if (isLandscape) 12.dp else 16.dp)) {
            content()
          }
          if (footer != null) {
            HorizontalDivider(color = Color(0xFFE5E7EB), thickness = 0.8.dp)
            Box(modifier = Modifier.padding(horizontal = 16.dp, vertical = if (isLandscape) 8.dp else 12.dp)) {
              footer()
            }
          }
        }
      }
    }
  }
}

@Composable
fun AccentPanelDialog(
  title: String,
  accentColor: Color,
  onDismiss: () -> Unit,
  content: @Composable () -> Unit
) {
  PastelModalDialog(
    title = title,
    icon = Icons.Default.AutoFixHigh,
    badgeColor = accentColor,
    headerTint = accentColor.copy(alpha = 0.14f),
    onDismiss = onDismiss,
    content = content
  )
}

@Composable
fun BottomDockCard(
  title: String,
  icon: ImageVector,
  badgeColor: Color,
  headerTint: Color,
  onDismiss: () -> Unit,
  content: @Composable () -> Unit
) {
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clickable(onClick = onDismiss)
        .padding(bottom = 86.dp, start = 16.dp, end = 16.dp),
      contentAlignment = Alignment.BottomCenter
    ) {
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clickable(enabled = false) {},
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 12.dp
      ) {
        Column {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(Brush.verticalGradient(listOf(headerTint, headerTint.copy(alpha = 0.35f))))
              .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(badgeColor),
              contentAlignment = Alignment.Center
            ) {
              Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color(0xFF111827))
          }
          Box(modifier = Modifier.padding(16.dp)) {
            content()
          }
        }
      }
    }
  }
}

@Composable
private fun SectionLabel(text: String) {
  Text(
    text = text.uppercase(),
    fontSize = 11.sp,
    fontWeight = FontWeight.Bold,
    color = Color(0xFF64748B),
    letterSpacing = 0.8.sp
  )
}

@Composable
private fun CheckBadge(color: Color, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(18.dp)
      .clip(CircleShape)
      .background(color),
    contentAlignment = Alignment.Center
  ) {
    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(12.dp))
  }
}

@Composable
private fun RainbowWheelButton(sizeDp: Int = 32, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .size(sizeDp.dp)
      .clip(CircleShape)
      .background(
        Brush.sweepGradient(
          listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
        )
      )
      .clickable(onClick = onClick),
    contentAlignment = Alignment.Center
  ) {
    Icon(Icons.Default.Add, contentDescription = "Custom color", tint = Color.White, modifier = Modifier.size(16.dp))
  }
}

@Composable
private fun ColorSwatchTray(
  colors: List<Int>,
  selectedColor: Int,
  accentColor: Color,
  onSelect: (Int) -> Unit,
  onToggleCustom: (() -> Unit)? = null
) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = TeachColors.TrayBg,
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp))
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 10.dp, vertical = 10.dp),
      horizontalArrangement = Arrangement.SpaceBetween,
      verticalAlignment = Alignment.CenterVertically
    ) {
      for (c in colors) {
        val selected = c == selectedColor
        Box(
          modifier = Modifier
            .size(27.dp)
            .clip(CircleShape)
            .then(
              if (selected) Modifier.border(2.2.dp, accentColor, CircleShape).padding(2.5.dp)
              else Modifier
            )
            .clip(CircleShape)
            .background(Color(c))
            .border(1.dp, Color(0x26000000), CircleShape)
            .clickable { onSelect(c) }
        )
      }
      if (onToggleCustom != null) {
        RainbowWheelButton(27, onToggleCustom)
      }
    }
  }
}

@Composable
private fun CustomRgbSliders(color: Int, onColorChange: (Int) -> Unit) {
  var r by remember(color) { mutableFloatStateOf(((color shr 16) and 0xFF).toFloat()) }
  var g by remember(color) { mutableFloatStateOf(((color shr 8) and 0xFF).toFloat()) }
  var b by remember(color) { mutableFloatStateOf((color and 0xFF).toFloat()) }

  fun emit() {
    val argb = (0xFF shl 24) or (r.toInt() shl 16) or (g.toInt() shl 8) or b.toInt()
    onColorChange(argb)
  }

  Surface(
    shape = RoundedCornerShape(14.dp),
    color = TeachColors.TrayBg,
    modifier = Modifier.fillMaxWidth().padding(top = 4.dp)
  ) {
    Column(modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)) {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("R", modifier = Modifier.width(18.dp), fontSize = 12.sp, color = Color.Red, fontWeight = FontWeight.Bold)
        Slider(value = r, onValueChange = { r = it; emit() }, valueRange = 0f..255f, modifier = Modifier.weight(1f))
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("G", modifier = Modifier.width(18.dp), fontSize = 12.sp, color = Color(0xFF16A34A), fontWeight = FontWeight.Bold)
        Slider(value = g, onValueChange = { g = it; emit() }, valueRange = 0f..255f, modifier = Modifier.weight(1f))
      }
      Row(verticalAlignment = Alignment.CenterVertically) {
        Text("B", modifier = Modifier.width(18.dp), fontSize = 12.sp, color = Color(0xFF2563EB), fontWeight = FontWeight.Bold)
        Slider(value = b, onValueChange = { b = it; emit() }, valueRange = 0f..255f, modifier = Modifier.weight(1f))
      }
    }
  }
}

// ===== 1. Board Color & Texture Dialog (Matching ref_10_board.png!) =====

@Composable
fun BoardColorDialog(
  currentColor: Int,
  currentStyle: BackgroundStyle,
  onApply: (Int, BackgroundStyle) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedColor by remember { mutableIntStateOf(currentColor) }
  var selectedStyle by remember { mutableStateOf(currentStyle) }
  var showCustom by remember { mutableStateOf(false) }

  val colorLabel = remember(selectedColor) {
    (TeachColors.ClassicBoardPalette + TeachColors.LightTintBoardPalette + TeachColors.DeepToneBoardPalette)
      .firstOrNull { it.second == selectedColor }?.first
      ?.replace("pastel_", "")?.replace("deep_", "")
      ?.replaceFirstChar { it.uppercase() } ?: "Custom"
  }
  val styleLabel = selectedStyle.name.lowercase().replaceFirstChar { it.uppercase() }

  PastelModalDialog(
    title = stringResource(R.string.board_color_title),
    icon = Icons.Default.FormatColorFill,
    badgeColor = TeachColors.AccentTeal,
    headerTint = TeachColors.HeaderTealTint,
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Live preview card with y = 2x + 1 + "White · Plain" label
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(94.dp)
          .clip(RoundedCornerShape(16.dp))
          .border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp))
      ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
          val rect = android.graphics.RectF(0f, 0f, size.width, size.height)
          Background.draw(drawContext.canvas.nativeCanvas, rect, selectedColor, selectedStyle)
          val fg = if (TeachColors.isDarkColor(selectedColor)) 0xFFFFFFFF.toInt() else 0xFF111827.toInt()
          FormulaParser.drawFormula(drawContext.canvas.nativeCanvas, "y = 2x + 1", fg, 34f, size.width * 0.65f, size.height)
        }
        Text(
          text = "$colorLabel · $styleLabel",
          fontSize = 12.sp,
          fontWeight = FontWeight.SemiBold,
          color = if (TeachColors.isDarkColor(selectedColor)) Color.White.copy(alpha = 0.85f) else Color(0xFF1E293B),
          modifier = Modifier.align(Alignment.BottomEnd).padding(horizontal = 12.dp, vertical = 8.dp)
        )
      }

      // CLASSIC BOARDS
      SectionLabel(stringResource(R.string.classic_boards))
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = TeachColors.TrayBg,
        modifier = Modifier.fillMaxWidth().border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp))
      ) {
        Row(
          modifier = Modifier.fillMaxWidth().padding(10.dp),
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          TeachColors.ClassicBoardPalette.forEach { (name, col) ->
            val sel = col == selectedColor
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.weight(1f).clickable { selectedColor = col; onApply(selectedColor, selectedStyle) }
            ) {
              Box(
                modifier = Modifier
                  .size(44.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .then(if (sel) Modifier.border(2.5.dp, TeachColors.AccentTeal, RoundedCornerShape(12.dp)).padding(3.dp) else Modifier)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(col))
                  .border(1.dp, Color(0x22000000), RoundedCornerShape(10.dp)),
                contentAlignment = Alignment.Center
              ) {
                if (sel) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = if (TeachColors.isDarkColor(col)) Color.White else Color(0xFF111827), modifier = Modifier.size(18.dp))
                }
              }
              Spacer(Modifier.height(4.dp))
              Text(
                name.replaceFirstChar { it.uppercase() },
                fontSize = 10.sp,
                fontWeight = if (sel) FontWeight.Bold else FontWeight.Normal,
                color = if (sel) TeachColors.AccentTeal else Color(0xFF475569),
                maxLines = 1
              )
            }
          }
        }
      }

      // LIGHT TINTS
      SectionLabel(stringResource(R.string.light_tints))
      BoardTileRow(TeachColors.LightTintBoardPalette.map { it.second }, selectedColor) {
        selectedColor = it; onApply(selectedColor, selectedStyle)
      }

      // DEEP TONES
      SectionLabel(stringResource(R.string.deep_tones))
      BoardTileRow(TeachColors.DeepToneBoardPalette.map { it.second }, selectedColor) {
        selectedColor = it; onApply(selectedColor, selectedStyle)
      }

      // TEXTURE — 6-column visual miniature thumbnail grid!
      SectionLabel(stringResource(R.string.textures))
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = TeachColors.TrayBg,
        modifier = Modifier.fillMaxWidth().border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp))
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          BackgroundStyle.entries.chunked(6).forEach { rowItems ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(6.dp)) {
              rowItems.forEach { style ->
                val sel = style == selectedStyle
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (sel) TeachColors.HeaderTealTint else Color.White)
                    .border(if (sel) 2.dp else 1.dp, if (sel) TeachColors.AccentTeal else TeachColors.TrayBorder, RoundedCornerShape(12.dp))
                    .clickable { selectedStyle = style; onApply(selectedColor, selectedStyle) }
                    .padding(vertical = 6.dp, horizontal = 3.dp)
                ) {
                  Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                    Canvas(
                      modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .border(0.8.dp, Color(0x22000000), RoundedCornerShape(6.dp))
                    ) {
                      Background.draw(
                        drawContext.canvas.nativeCanvas,
                        android.graphics.RectF(0f, 0f, size.width, size.height),
                        0xFFFFFFFF.toInt(),
                        style
                      )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                      style.name.lowercase().replaceFirstChar { it.uppercase() },
                      fontSize = 9.sp,
                      fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                      color = if (sel) TeachColors.AccentTeal else Color(0xFF334155),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  if (sel) {
                    CheckBadge(TeachColors.AccentTeal, Modifier.align(Alignment.TopEnd).padding(2.dp))
                  }
                }
              }
              repeat(6 - rowItems.size) { Spacer(Modifier.weight(1f)) }
            }
          }
        }
      }

      // Custom Color full-width pill button
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(14.dp))
          .clickable { showCustom = !showCustom },
        color = Color(0xFFFAFAFA)
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 11.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          RainbowWheelButton(28) { showCustom = !showCustom }
          Text(stringResource(R.string.custom_color), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111827))
        }
      }
      if (showCustom) {
        CustomRgbSliders(selectedColor) { c -> selectedColor = c; onApply(selectedColor, selectedStyle) }
      }
    }
  }
}

@Composable
private fun BoardTileRow(colors: List<Int>, selectedColor: Int, onSelect: (Int) -> Unit) {
  Surface(
    shape = RoundedCornerShape(16.dp),
    color = TeachColors.TrayBg,
    modifier = Modifier.fillMaxWidth().border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp))
  ) {
    Row(
      modifier = Modifier.fillMaxWidth().padding(10.dp),
      horizontalArrangement = Arrangement.SpaceBetween
    ) {
      colors.forEach { col ->
        val sel = col == selectedColor
        Box(
          modifier = Modifier
            .size(44.dp)
            .clip(RoundedCornerShape(12.dp))
            .then(if (sel) Modifier.border(2.5.dp, TeachColors.AccentTeal, RoundedCornerShape(12.dp)).padding(3.dp) else Modifier)
            .clip(RoundedCornerShape(10.dp))
            .background(Color(col))
            .border(1.dp, Color(0x22000000), RoundedCornerShape(10.dp))
            .clickable { onSelect(col) },
          contentAlignment = Alignment.Center
        ) {
          if (sel) {
            Icon(Icons.Default.Check, contentDescription = null, tint = if (TeachColors.isDarkColor(col)) Color.White else Color(0xFF111827), modifier = Modifier.size(18.dp))
          }
        }
      }
    }
  }
}

// ===== 2. Brushes Dialog (Matching ref_05_brushes.png!) =====

@Composable
fun BrushDialog(
  currentTool: ToolType,
  currentColor: Int,
  currentSize: Float,
  currentAlpha: Float,
  smartShapesEnabled: Boolean,
  onSelectBrush: (ToolType) -> Unit,
  onColorChange: (Int) -> Unit,
  onSizeChange: (Float) -> Unit,
  onAlphaChange: (Float) -> Unit,
  onSmartShapesChange: (Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  var showCustom by remember { mutableStateOf(false) }
  val brushes = listOf(
    Triple(ToolType.Pen, stringResource(R.string.brush_pen), Icons.Default.Edit),
    Triple(ToolType.Brush, stringResource(R.string.brush_brush), Icons.Default.Brush),
    Triple(ToolType.Marker, stringResource(R.string.brush_marker), Icons.Default.Create),
    Triple(ToolType.Highlighter, stringResource(R.string.brush_highlight), Icons.Default.Highlight),
    Triple(ToolType.Calligraphy, "Callig.", Icons.Default.Draw)
  )

  PastelModalDialog(
    title = stringResource(R.string.brushes_title),
    icon = Icons.Default.Brush,
    badgeColor = TeachColors.AccentSky,
    headerTint = TeachColors.HeaderSkyTint,
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Live smooth sine-wave stroke preview card
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(74.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(TeachColors.TrayBg)
          .border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
      ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp)) {
          val path = Path()
          val steps = 48
          for (i in 0..steps) {
            val t = i.toFloat() / steps
            val x = t * size.width
            val y = size.height / 2f - sin(t * Math.PI * 2.4).toFloat() * (size.height * 0.30f)
            if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
          }
          val effAlpha = if (currentTool == ToolType.Highlighter) currentAlpha * 0.42f else currentAlpha
          drawPath(
            path = path,
            color = Color(currentColor).copy(alpha = effAlpha.coerceIn(0.18f, 1f)),
            style = Stroke(width = currentSize.coerceIn(2f, 40f), cap = StrokeCap.Round)
          )
        }
      }

      // 5 equal-width Brush cards with blue selection outline + top-right checkmark!
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        brushes.forEach { (tool, label, icon) ->
          val sel = currentTool == tool
          Box(
            modifier = Modifier
              .weight(1f)
              .height(76.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(if (sel) TeachColors.HeaderSkyTint else Color.White)
              .border(if (sel) 2.dp else 1.dp, if (sel) TeachColors.AccentSky else TeachColors.TrayBorder, RoundedCornerShape(14.dp))
              .clickable { onSelectBrush(tool) }
          ) {
            Column(
              modifier = Modifier.fillMaxSize().padding(4.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.Center
            ) {
              Icon(icon, contentDescription = label, tint = if (sel) TeachColors.AccentSky else Color(0xFF1E293B), modifier = Modifier.size(22.dp))
              Spacer(Modifier.height(6.dp))
              Text(label, fontSize = 11.sp, fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium, color = if (sel) TeachColors.AccentSky else Color(0xFF1E293B), maxLines = 1)
            }
            if (sel) {
              CheckBadge(TeachColors.AccentSky, Modifier.align(Alignment.TopEnd).padding(4.dp))
            }
          }
        }
      }

      SectionLabel(stringResource(R.string.color_label))
      ColorSwatchTray(
        colors = TeachColors.BrushSwatches,
        selectedColor = currentColor,
        accentColor = TeachColors.AccentSky,
        onSelect = onColorChange,
        onToggleCustom = { showCustom = !showCustom }
      )
      if (showCustom) CustomRgbSliders(currentColor, onColorChange)

      // SIZE · N grouped tray with 5 cards (4, 8, 12, 24, 48) + blue slider
      SectionLabel("${stringResource(R.string.size_label)}  ·  ${currentSize.toInt()}")
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = TeachColors.TrayBg,
        modifier = Modifier.fillMaxWidth().border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(4f, 8f, 12f, 24f, 48f).forEach { preset ->
              val sel = kotlin.math.abs(currentSize - preset) < 1.5f
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (sel) TeachColors.HeaderSkyTint else Color.White)
                  .border(if (sel) 2.dp else 1.dp, if (sel) TeachColors.AccentSky else TeachColors.TrayBorder, RoundedCornerShape(12.dp))
                  .clickable { onSizeChange(preset) }
              ) {
                Text(
                  text = "${preset.toInt()}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 15.sp,
                  color = if (sel) TeachColors.AccentSky else Color(0xFF1E293B),
                  modifier = Modifier.align(Alignment.Center)
                )
                if (sel) {
                  CheckBadge(TeachColors.AccentSky, Modifier.align(Alignment.TopEnd).padding(3.dp))
                }
              }
            }
          }
          Slider(
            value = currentSize,
            onValueChange = onSizeChange,
            valueRange = 2f..64f,
            colors = SliderDefaults.colors(
              thumbColor = TeachColors.AccentSky,
              activeTrackColor = TeachColors.AccentSky,
              inactiveTrackColor = Color(0xFFDCE3EC)
            )
          )
        }
      }
    }
  }
}

// ===== 2b. Smart Quick 2-Card Floating Popup (Matching ref_06_smart.png!) =====

@Composable
fun SmartQuickPopup(
  selectedLanguageTag: String,
  isWritingActive: Boolean,
  isSmartShapesActive: Boolean,
  onSelectWriting: () -> Unit,
  onToggleSmartShapes: () -> Unit,
  onOpenLanguagePicker: () -> Unit,
  onDismiss: () -> Unit
) {
  val shortCode = selectedLanguageTag.substringBefore("-").uppercase().take(2).ifBlank { "EN" }

  val config = LocalConfiguration.current
  val isLandscape = config.screenWidthDp > config.screenHeightDp
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    MaterialTheme(colorScheme = LightModalColorScheme) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clickable(onClick = onDismiss)
          .padding(bottom = if (isLandscape) 68.dp else 86.dp, start = 28.dp, end = 28.dp),
        contentAlignment = Alignment.BottomCenter
      ) {
        Surface(
          modifier = Modifier.fillMaxWidth(if (isLandscape) 0.56f else 1f).clickable(enabled = false) {},
          shape = RoundedCornerShape(22.dp),
          color = Color.White,
          contentColor = Color(0xFF111827),
          shadowElevation = 12.dp
        ) {
        Row(
          modifier = Modifier.padding(12.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Left card: Writing (Aa -> Aa + language pill!)
          Box(
            modifier = Modifier
              .weight(1f)
              .height(148.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (isWritingActive) TeachColors.HeaderSkyTint else Color(0xFFFAFAFC))
              .border(if (isWritingActive) 2.dp else 1.dp, if (isWritingActive) TeachColors.Primary else TeachColors.TrayBorder, RoundedCornerShape(16.dp))
              .clickable { onSelectWriting(); onDismiss() }
              .padding(10.dp)
          ) {
            // Top-left clickable language pill (🌐 EN >)
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color.White,
              modifier = Modifier
                .align(Alignment.TopStart)
                .border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(14.dp))
                .clickable { onOpenLanguagePicker() }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(Icons.Default.Language, contentDescription = null, tint = TeachColors.Primary, modifier = Modifier.size(13.dp))
                Text("$shortCode ›", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = TeachColors.Primary)
              }
            }
            Column(
              modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Aa", fontSize = 30.sp, color = Color(0xFF475569), fontWeight = FontWeight.Light)
                Text(" → ", fontSize = 18.sp, color = Color(0xFF94A3B8))
                Text("Aa", fontSize = 26.sp, color = TeachColors.Primary, fontWeight = FontWeight.ExtraBold)
              }
              Spacer(Modifier.height(6.dp))
              Text(stringResource(R.string.smart_writing_short), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
            }
          }

          // Right card: Smart Shapes (rough triangle -> clean blue triangle!)
          Box(
            modifier = Modifier
              .weight(1f)
              .height(148.dp)
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSmartShapesActive) TeachColors.HeaderSkyTint else Color(0xFFFAFAFC))
              .border(if (isSmartShapesActive) 2.dp else 1.dp, if (isSmartShapesActive) TeachColors.Primary else TeachColors.TrayBorder, RoundedCornerShape(16.dp))
              .clickable { onToggleSmartShapes(); onDismiss() }
              .padding(10.dp)
          ) {
            if (isSmartShapesActive) {
              CheckBadge(TeachColors.Primary, Modifier.align(Alignment.TopEnd))
            }
            Column(
              modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 4.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Canvas(modifier = Modifier.width(126.dp).height(46.dp)) {
                val w = size.width
                val h = size.height
                // Hand-drawn rough triangle on the left
                val rough = Path().apply {
                  moveTo(w * 0.21f, h * 0.12f)
                  lineTo(w * 0.37f, h * 0.86f)
                  lineTo(w * 0.05f, h * 0.88f)
                  close()
                }
                drawPath(
                  rough,
                  Color(0xFF475569),
                  style = Stroke(width = h * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
                // Arrow in the center
                val midY = h * 0.54f
                drawLine(Color(0xFF94A3B8), Offset(w * 0.43f, midY), Offset(w * 0.57f, midY), strokeWidth = h * 0.055f, cap = StrokeCap.Round)
                drawLine(Color(0xFF94A3B8), Offset(w * 0.52f, midY - h * 0.12f), Offset(w * 0.57f, midY), strokeWidth = h * 0.055f, cap = StrokeCap.Round)
                drawLine(Color(0xFF94A3B8), Offset(w * 0.52f, midY + h * 0.12f), Offset(w * 0.57f, midY), strokeWidth = h * 0.055f, cap = StrokeCap.Round)
                // Clean blue geometric triangle on the right
                val clean = Path().apply {
                  moveTo(w * 0.79f, h * 0.10f)
                  lineTo(w * 0.96f, h * 0.88f)
                  lineTo(w * 0.62f, h * 0.88f)
                  close()
                }
                drawPath(
                  clean,
                  TeachColors.Primary,
                  style = Stroke(width = h * 0.075f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                )
              }
              Spacer(Modifier.height(8.dp))
              Text(stringResource(R.string.tool_shapes), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = Color(0xFF111827))
            }
          }
        }
      }
    }
    }
  }
}

// ===== 3. Shapes Dialog (Matching ref_08_shapes.png: 5-col continuous scroll across all 45 shapes!) =====

@Composable
fun ShapesDialog(
  selectedShape: ToolType,
  isFilled: Boolean,
  strokeColor: Int,
  fillColor: Int?,
  alpha: Float,
  onSelectShape: (ToolType) -> Unit,
  onFillToggle: (Boolean) -> Unit,
  onFillColorChange: (Int?) -> Unit,
  onAlphaChange: (Float) -> Unit,
  onDismiss: () -> Unit
) {
  val sections = listOf(
    stringResource(R.string.shapes_2d) to ToolType.shapesSheet2D,
    stringResource(R.string.shapes_3d) to ToolType.shapesSheet3D,
    stringResource(R.string.shapes_math) to ToolType.shapesSheetMath,
    stringResource(R.string.shapes_diagram) to ToolType.shapesSheetDiagram
  )

  PastelModalDialog(
    title = stringResource(R.string.shapes_title),
    icon = Icons.Default.Category,
    badgeColor = TeachColors.AccentPurple,
    headerTint = TeachColors.HeaderPurpleTint,
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Fill Shape bordered card row
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = Color(0xFFFAFAFC),
        modifier = Modifier.fillMaxWidth().border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(14.dp))
      ) {
        Column(modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.FormatColorFill, contentDescription = null, tint = Color(0xFF475569), modifier = Modifier.size(20.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.fill_shape), fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color(0xFF111827), modifier = Modifier.weight(1f))
            Switch(
              checked = isFilled,
              onCheckedChange = onFillToggle,
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = TeachColors.AccentPurple)
            )
          }
          if (isFilled) {
            Spacer(Modifier.height(6.dp))
            ColorSwatchTray(TeachColors.BrushSwatches, fillColor ?: strokeColor, TeachColors.AccentPurple, { onFillColorChange(it) })
            Slider(
              value = alpha,
              onValueChange = onAlphaChange,
              valueRange = 0.2f..1f,
              colors = SliderDefaults.colors(thumbColor = TeachColors.AccentPurple, activeTrackColor = TeachColors.AccentPurple, inactiveTrackColor = Color(0xFFDCE3EC))
            )
          }
        }
      }

      // All 4 sections stacked cleanly in 5-column grids
      sections.forEach { (secName, shapes) ->
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
          Text(
            text = secName.uppercase(),
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF64748B),
            letterSpacing = 0.8.sp,
            modifier = Modifier.padding(horizontal = 10.dp)
          )
          HorizontalDivider(modifier = Modifier.weight(1f), color = Color(0xFFE5E7EB))
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          shapes.chunked(5).forEach { rowShapes ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
              rowShapes.forEach { shape ->
                val sel = shape == selectedShape
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(68.dp)
                    .clip(RoundedCornerShape(13.dp))
                    .background(if (sel) TeachColors.HeaderPurpleTint else Color.White)
                    .border(if (sel) 2.dp else 1.dp, if (sel) TeachColors.AccentPurple else TeachColors.TrayBorder, RoundedCornerShape(13.dp))
                    .clickable { onSelectShape(shape); onDismiss() }
                    .padding(4.dp)
                ) {
                  Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                  ) {
                    Canvas(modifier = Modifier.size(28.dp)) {
                      val pad = 2f
                      ShapeRenderer.drawShape(
                        canvas = drawContext.canvas.nativeCanvas,
                        type = shape,
                        left = pad,
                        top = pad,
                        right = size.width - pad,
                        bottom = size.height - pad,
                        color = if (sel) TeachColors.AccentPurple.toArgb() else 0xFF1E293B.toInt(),
                        strokeWidth = 2.6f,
                        isFilled = isFilled,
                        fillColor = fillColor ?: TeachColors.AccentPurple.toArgb(),
                        alpha = 1f
                      )
                    }
                    Spacer(Modifier.height(4.dp))
                    Text(
                      text = shape.name.replace("Triangle", "△").replace("Angle", "∠").replace("Parallelogram", "Parallel").replace(Regex("([a-z])([A-Z])"), "$1 $2"),
                      fontSize = 9.sp,
                      fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                      color = if (sel) TeachColors.AccentPurple else Color(0xFF334155),
                      maxLines = 1,
                      overflow = TextOverflow.Ellipsis
                    )
                  }
                  if (sel) {
                    CheckBadge(TeachColors.AccentPurple, Modifier.align(Alignment.TopEnd))
                  }
                }
              }
              repeat(5 - rowShapes.size) { Spacer(Modifier.weight(1f)) }
            }
          }
        }
      }
    }
  }
}

// ===== 4. Eraser Size Dialog (Matching ref_09_eraser.png!) =====

@Composable
fun EraserDialog(
  mode: EraserMode,
  size: Float,
  onModeChange: (EraserMode) -> Unit,
  onSizeChange: (Float) -> Unit,
  onDismiss: () -> Unit
) {
  PastelModalDialog(
    title = stringResource(R.string.eraser_size_title),
    icon = Icons.Default.CleaningServices,
    badgeColor = TeachColors.AccentRose,
    headerTint = TeachColors.HeaderRoseTint,
    onDismiss = onDismiss
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
      SectionLabel("QUICK SIZE")
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(10f, 20f, 40f, 60f, 80f).forEach { preset ->
          val sel = kotlin.math.abs(size - preset) < 3f
          Box(
            modifier = Modifier
              .weight(1f)
              .height(50.dp)
              .clip(RoundedCornerShape(13.dp))
              .background(if (sel) TeachColors.HeaderRoseTint else Color.White)
              .border(if (sel) 2.dp else 1.dp, if (sel) TeachColors.AccentRose else TeachColors.TrayBorder, RoundedCornerShape(13.dp))
              .clickable { onSizeChange(preset) }
          ) {
            Text(
              "${preset.toInt()}",
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = if (sel) TeachColors.AccentRose else Color(0xFF1E293B),
              modifier = Modifier.align(Alignment.Center)
            )
            if (sel) CheckBadge(TeachColors.AccentRose, Modifier.align(Alignment.TopEnd).padding(3.dp))
          }
        }
      }

      SectionLabel("CUSTOM  ·  ${size.toInt()}")
      Slider(
        value = size.coerceIn(6f, 80f),
        onValueChange = onSizeChange,
        valueRange = 6f..80f,
        colors = SliderDefaults.colors(thumbColor = TeachColors.AccentRose, activeTrackColor = TeachColors.AccentRose, inactiveTrackColor = Color(0xFFDCE3EC))
      )

      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        FilterChip(
          selected = mode == EraserMode.PIXEL_INK,
          onClick = { onModeChange(EraserMode.PIXEL_INK) },
          label = { Text(stringResource(R.string.eraser_pixel)) }
        )
        FilterChip(
          selected = mode == EraserMode.STROKE,
          onClick = { onModeChange(EraserMode.STROKE) },
          label = { Text(stringResource(R.string.eraser_stroke)) }
        )
      }
    }
  }
}

// ===== 5. Smart Writing 18-Language Dialog =====

@Composable
fun SmartWritingDialog(
  languages: List<InkLanguage>,
  selectedTag: String,
  downloadedTags: Set<String>,
  modelState: InkModelState,
  bufferedStrokeCount: Int,
  onSelectLanguage: (String) -> Unit,
  onConvertNow: () -> Unit,
  onClearBuffer: () -> Unit,
  onDismiss: () -> Unit
) {
  PastelModalDialog(
    title = stringResource(R.string.smart_writing_title),
    icon = Icons.Default.AutoFixHigh,
    badgeColor = TeachColors.AccentPurple,
    headerTint = TeachColors.HeaderPurpleTint,
    onDismiss = onDismiss
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Text(stringResource(R.string.smart_writing_hint), fontSize = 13.sp, color = Color(0xFF475569))

      if (modelState == InkModelState.DOWNLOADING) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          CircularProgressIndicator(modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
          Text(stringResource(R.string.downloading_model), fontSize = 13.sp, color = Color(0xFF111827))
        }
      }

      if (bufferedStrokeCount > 0) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
          Button(onClick = { onConvertNow(); onDismiss() }, modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.convert_now))
          }
          OutlinedButton(onClick = onClearBuffer) { Text(stringResource(R.string.clear)) }
        }
      }

      LazyColumn(modifier = Modifier.heightIn(max = 330.dp)) {
        items(languages) { lang ->
          val isSel = lang.tag == selectedTag
          val isReady = lang.tag in downloadedTags
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(10.dp))
              .background(if (isSel) TeachColors.HeaderPurpleTint else Color.Transparent)
              .clickable { onSelectLanguage(lang.tag) }
              .padding(horizontal = 8.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            RadioButton(selected = isSel, onClick = { onSelectLanguage(lang.tag) })
            Text(lang.label, modifier = Modifier.weight(1f), color = Color(0xFF111827), fontWeight = if (isSel) FontWeight.Bold else FontWeight.Normal)
            if (isReady) {
              Icon(Icons.Default.Check, contentDescription = null, tint = TeachColors.Success, modifier = Modifier.size(18.dp))
            } else {
              Icon(Icons.Default.Download, contentDescription = null, tint = Color(0xFF94A3B8), modifier = Modifier.size(18.dp))
            }
          }
        }
      }
    }
  }
}

// ===== 6. Add Text Dialog (Matching ref_11_text.png!) =====

@Composable
fun TextEditorDialog(
  initialMeta: Serialization.RichTextMeta,
  initialColor: Int,
  initialFontSize: Float,
  onConfirm: (Serialization.RichTextMeta, Int, Float) -> Unit,
  onDismiss: () -> Unit
) {
  var text by remember { mutableStateOf(initialMeta.text) }
  var fontFamily by remember { mutableStateOf(initialMeta.fontFamily) }
  var bold by remember { mutableStateOf(initialMeta.bold) }
  var italic by remember { mutableStateOf(initialMeta.italic) }
  var align by remember { mutableStateOf(initialMeta.align) }
  var color by remember { mutableIntStateOf(initialColor) }
  var fontSize by remember { mutableFloatStateOf(initialFontSize.coerceIn(20f, 110f)) }
  var showCustom by remember { mutableStateOf(false) }

  PastelModalDialog(
    title = stringResource(R.string.add_text_title),
    icon = Icons.Default.TextFields,
    badgeColor = TeachColors.Primary,
    headerTint = TeachColors.HeaderSkyTint,
    onDismiss = onDismiss,
    footer = {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(12.dp)) {
          Text(stringResource(R.string.cancel), color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
        }
        Button(
          onClick = {
            onConfirm(Serialization.RichTextMeta(text, fontFamily, bold, italic, align), color, fontSize)
            onDismiss()
          },
          enabled = text.isNotBlank(),
          modifier = Modifier.weight(1f).height(46.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TeachColors.Primary)
        ) {
          Text(stringResource(R.string.add_action), fontWeight = FontWeight.Bold)
        }
      }
    }
  ) {
    Column(
      modifier = Modifier.verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      OutlinedTextField(
        value = text,
        onValueChange = { text = it },
        modifier = Modifier.fillMaxWidth().heightIn(min = 104.dp),
        shape = RoundedCornerShape(14.dp),
        placeholder = { Text(stringResource(R.string.text_placeholder), color = Color(0xFF94A3B8)) }
      )

      Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
        listOf("SANS" to "Sans", "SERIF" to "Serif", "MONO" to "Mono").forEach { (k, label) ->
          FilterChip(selected = fontFamily == k, onClick = { fontFamily = k }, label = { Text(label, fontSize = 11.sp) })
        }
        Spacer(Modifier.weight(1f))
        IconButton(onClick = { bold = !bold }, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.FormatBold, contentDescription = "Bold", tint = if (bold) TeachColors.Primary else Color(0xFF64748B))
        }
        IconButton(onClick = { italic = !italic }, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.FormatItalic, contentDescription = "Italic", tint = if (italic) TeachColors.Primary else Color(0xFF64748B))
        }
        IconButton(onClick = { align = when (align) { "START" -> "CENTER"; "CENTER" -> "END"; else -> "START" } }, modifier = Modifier.size(32.dp)) {
          Icon(
            when (align) { "CENTER" -> Icons.Default.FormatAlignCenter; "END" -> Icons.AutoMirrored.Filled.FormatAlignRight; else -> Icons.AutoMirrored.Filled.FormatAlignLeft },
            contentDescription = "Align",
            tint = TeachColors.Primary
          )
        }
      }

      SectionLabel(stringResource(R.string.color_label))
      ColorSwatchTray(TeachColors.BrushSwatches, color, TeachColors.Primary, { color = it }, { showCustom = !showCustom })
      if (showCustom) CustomRgbSliders(color) { color = it }

      SectionLabel("${stringResource(R.string.size_label)}  ·  ${fontSize.toInt()}")
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = TeachColors.TrayBg,
        modifier = Modifier.fillMaxWidth().border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp))
      ) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(36f, 48f, 72f, 100f).forEach { sz ->
              val sel = kotlin.math.abs(fontSize - sz) < 3f
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(46.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(if (sel) TeachColors.HeaderSkyTint else Color.White)
                  .border(if (sel) 2.dp else 1.dp, if (sel) TeachColors.Primary else TeachColors.TrayBorder, RoundedCornerShape(12.dp))
                  .clickable { fontSize = sz }
              ) {
                Text("${sz.toInt()}", fontWeight = FontWeight.Bold, color = if (sel) TeachColors.Primary else Color(0xFF1E293B), modifier = Modifier.align(Alignment.Center))
                if (sel) CheckBadge(TeachColors.Primary, Modifier.align(Alignment.TopEnd).padding(3.dp))
              }
            }
          }
          Slider(
            value = fontSize,
            onValueChange = { fontSize = it },
            valueRange = 18f..110f,
            colors = SliderDefaults.colors(thumbColor = TeachColors.Primary, activeTrackColor = TeachColors.Primary, inactiveTrackColor = Color(0xFFDCE3EC))
          )
        }
      }
    }
  }
}

// ===== 7. Math Formula Dialog (Matching ref_12_formula.png: 4-col Quick Insert!) =====

@Composable
fun FormulaDialog(
  initialLatex: String,
  initialColor: Int,
  initialSize: Float,
  onConfirm: (String, Int, Float) -> Unit,
  onDismiss: () -> Unit
) {
  var latex by remember { mutableStateOf(initialLatex) }
  var color by remember { mutableIntStateOf(initialColor) }
  var fontSize by remember { mutableFloatStateOf(initialSize.coerceIn(24f, 96f)) }
  var showCustom by remember { mutableStateOf(false) }

  PastelModalDialog(
    title = stringResource(R.string.math_formula_title),
    icon = Icons.Default.Functions,
    badgeColor = TeachColors.AccentIndigo,
    headerTint = TeachColors.HeaderIndigoTint,
    onDismiss = onDismiss,
    footer = {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f).height(46.dp), shape = RoundedCornerShape(12.dp)) {
          Text(stringResource(R.string.cancel), color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
        }
        Button(
          onClick = { onConfirm(latex, color, fontSize); onDismiss() },
          enabled = latex.isNotBlank(),
          modifier = Modifier.weight(1f).height(46.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TeachColors.AccentIndigo)
        ) {
          Text(stringResource(R.string.add_action), fontWeight = FontWeight.Bold)
        }
      }
    }
  ) {
    Column(
      modifier = Modifier.verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Preview box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(92.dp)
          .clip(RoundedCornerShape(16.dp))
          .background(Color.White)
          .border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
      ) {
        if (latex.isBlank()) {
          Text(stringResource(R.string.preview_placeholder), color = Color(0xFF94A3B8), fontSize = 14.sp)
        } else {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val previewColor = if (color == TeachColors.BrushWhite) TeachColors.BrushBlack else color
            FormulaParser.drawFormula(drawContext.canvas.nativeCanvas, latex, previewColor, 38f, size.width, size.height)
          }
        }
      }

      OutlinedTextField(
        value = latex,
        onValueChange = { latex = it },
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(14.dp),
        placeholder = { Text(stringResource(R.string.tool_formula), color = Color(0xFF94A3B8)) },
        maxLines = 3
      )

      SectionLabel(stringResource(R.string.color_label))
      ColorSwatchTray(TeachColors.BrushSwatches, color, TeachColors.AccentIndigo, { color = it }, { showCustom = !showCustom })
      if (showCustom) CustomRgbSliders(color) { color = it }

      SectionLabel("FONT SIZE  ·  ${fontSize.toInt()}")
      Surface(
        shape = RoundedCornerShape(14.dp),
        color = TeachColors.TrayBg,
        modifier = Modifier.fillMaxWidth().border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(14.dp))
      ) {
        Slider(
          value = fontSize,
          onValueChange = { fontSize = it },
          valueRange = 24f..96f,
          modifier = Modifier.padding(horizontal = 12.dp),
          colors = SliderDefaults.colors(thumbColor = TeachColors.AccentIndigo, activeTrackColor = TeachColors.AccentIndigo, inactiveTrackColor = Color(0xFFDCE3EC))
        )
      }

      SectionLabel("QUICK INSERT")
      Column(verticalArrangement = Arrangement.spacedBy(7.dp)) {
        FormulaParser.symbolPalette40.chunked(4).forEach { row ->
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
            row.forEach { sym ->
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .height(38.dp)
                  .clip(RoundedCornerShape(11.dp))
                  .border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(11.dp))
                  .clickable { latex += sym.snippet },
                color = Color(0xFFFAFAFC)
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(sym.label, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = Color(0xFF1E293B))
                }
              }
            }
          }
        }
      }
    }
  }
}

// ===== 8. Table Dark Interactive 10x10 Grid Popover (Drag & Tap + Landscape Safe!) =====

@Composable
fun TableGridPopover(
  onInsertTable: (rows: Int, cols: Int) -> Unit,
  onDismiss: () -> Unit
) {
  val config = LocalConfiguration.current
  val isLandscape = config.screenWidthDp > config.screenHeightDp
  val popoverWidth = if (isLandscape) 226.dp else 304.dp
  var rows by remember { mutableIntStateOf(3) }
  var cols by remember { mutableIntStateOf(3) }
  var gridSize by remember { mutableStateOf(IntSize.Zero) }

  fun updateFromOffset(pos: Offset) {
    if (gridSize.width <= 0 || gridSize.height <= 0) return
    val c = ((pos.x / gridSize.width) * 10f).toInt().coerceIn(0, 9) + 1
    val r = ((pos.y / gridSize.height) * 10f).toInt().coerceIn(0, 9) + 1
    rows = r
    cols = c
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clickable(onClick = onDismiss)
        .padding(bottom = if (isLandscape) 66.dp else 86.dp, end = 16.dp),
      contentAlignment = Alignment.BottomEnd
    ) {
      Surface(
        modifier = Modifier.width(popoverWidth).clickable(enabled = false) {},
        shape = RoundedCornerShape(18.dp),
        color = Color(0xFF232528),
        shadowElevation = 12.dp
      ) {
        Column(modifier = Modifier.padding(if (isLandscape) 10.dp else 14.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
              "$rows rows × $cols columns",
              color = Color.White,
              fontWeight = FontWeight.Bold,
              fontSize = if (isLandscape) 12.sp else 14.sp,
              modifier = Modifier.weight(1f)
            )
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = Color(0xFF2196F3),
              modifier = Modifier.clickable { onInsertTable(rows, cols); onDismiss() }
            ) {
              Text("OK", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp))
            }
          }

          Column(
            modifier = Modifier
              .fillMaxWidth()
              .onSizeChanged { gridSize = it }
              .pointerInput(Unit) {
                detectTapGestures { offset -> updateFromOffset(offset) }
              }
              .pointerInput(Unit) {
                detectDragGestures(
                  onDragStart = { offset -> updateFromOffset(offset) },
                  onDrag = { change, _ ->
                    change.consume()
                    updateFromOffset(change.position)
                  }
                )
              },
            verticalArrangement = Arrangement.spacedBy(if (isLandscape) 2.5.dp else 4.dp)
          ) {
            for (r in 1..10) {
              Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 2.5.dp else 4.dp)) {
                for (c in 1..10) {
                  val active = r <= rows && c <= cols
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .aspectRatio(1f)
                      .clip(RoundedCornerShape(3.5.dp))
                      .background(if (active) Color(0xFF2196F3) else Color(0xFF33363B))
                      .border(0.7.dp, if (active) Color(0xFF60A5FA) else Color(0xFF474B52), RoundedCornerShape(3.5.dp))
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

// Full Table Cell Editor (when tapping Edit or double-tapping an existing Table on the board!)
@Composable
fun TableDialog(
  initialPayload: String?,
  initialColor: Int,
  onConfirm: (Int, Int, List<List<String>>, Int) -> Unit,
  onDismiss: () -> Unit
) {
  val config = LocalConfiguration.current
  val isLandscape = config.screenHeightDp < 500
  val decoded = remember(initialPayload) { Serialization.decodeTablePayload(initialPayload) }
  var rows by remember(initialPayload) { mutableIntStateOf(decoded.first) }
  var cols by remember(initialPayload) { mutableIntStateOf(decoded.second) }
  var borderColor by remember(initialPayload, initialColor) { mutableIntStateOf(initialColor) }
  var showCustomColor by remember { mutableStateOf(false) }
  val grid = remember(initialPayload) {
    mutableStateListOf<MutableList<String>>().apply {
      for (r in 0 until 10) add(MutableList(10) { c -> decoded.third.getOrNull(r)?.getOrNull(c).orEmpty() })
    }
  }

  PastelModalDialog(
    title = stringResource(R.string.tool_table),
    icon = Icons.Default.TableChart,
    badgeColor = TeachColors.AccentTeal,
    headerTint = TeachColors.HeaderTealTint,
    onDismiss = onDismiss,
    footer = {
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(
          onClick = onDismiss,
          modifier = Modifier.weight(1f).height(44.dp),
          shape = RoundedCornerShape(12.dp)
        ) {
          Text(stringResource(R.string.cancel), color = Color(0xFF1E293B), fontWeight = FontWeight.SemiBold)
        }
        Button(
          onClick = {
            onConfirm(rows, cols, List(rows) { r -> List(cols) { c -> grid[r][c] } }, borderColor)
            onDismiss()
          },
          modifier = Modifier.weight(1f).height(44.dp),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TeachColors.AccentTeal)
        ) { Text(stringResource(R.string.save), fontWeight = FontWeight.Bold) }
      }
    }
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 10.dp)) {
      // Compact Rows & Cols stepper row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(TeachColors.TrayBg)
          .border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(12.dp))
          .padding(horizontal = 10.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("${stringResource(R.string.rows)}: $rows", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF111827))
          IconButton(onClick = { if (rows > 1) rows-- }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Remove, contentDescription = "Remove row", tint = Color(0xFF1E293B), modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = { if (rows < 10) rows++ }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Add row", tint = Color(0xFF1E293B), modifier = Modifier.size(18.dp))
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically) {
          Text("${stringResource(R.string.columns)}: $cols", fontWeight = FontWeight.SemiBold, fontSize = 13.sp, color = Color(0xFF111827))
          IconButton(onClick = { if (cols > 1) cols-- }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Remove, contentDescription = "Remove column", tint = Color(0xFF1E293B), modifier = Modifier.size(18.dp))
          }
          IconButton(onClick = { if (cols < 10) cols++ }, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Add, contentDescription = "Add column", tint = Color(0xFF1E293B), modifier = Modifier.size(18.dp))
          }
        }
      }

      // Responsive full-width Table Cell Grid (fills width cleanly for <=5 cols; horizontal scroll for >5 cols)
      val cellHeight = if (isLandscape) 38.dp else 44.dp
      val maxGridHeight = if (isLandscape) 128.dp else 220.dp
      val headerBg = Color(borderColor).copy(alpha = 0.14f)
      val useFillWidth = cols <= 5
      val hScroll = rememberScrollState()
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = maxGridHeight)
          .verticalScroll(rememberScrollState())
          .let { if (useFillWidth) it else it.horizontalScroll(hScroll) },
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        for (r in 0 until rows) {
          Row(
            modifier = if (useFillWidth) Modifier.fillMaxWidth() else Modifier,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            for (c in 0 until cols) {
              var cellVal by remember(initialPayload, r, c) { mutableStateOf(grid[r][c]) }
              val cellMod = (if (useFillWidth) Modifier.weight(1f) else Modifier.width(92.dp))
                .height(cellHeight)
                .clip(RoundedCornerShape(8.dp))
                .background(if (r == 0) headerBg else Color.White)
                .border(1.dp, Color(borderColor).copy(alpha = 0.45f), RoundedCornerShape(8.dp))
                .padding(horizontal = 8.dp)
              Box(modifier = cellMod, contentAlignment = Alignment.CenterStart) {
                if (cellVal.isEmpty()) {
                  Text(
                    text = if (r == 0) "Header ${c + 1}" else "",
                    fontSize = 12.sp,
                    color = Color(0xFF94A3B8),
                    maxLines = 1
                  )
                }
                BasicTextField(
                  value = cellVal,
                  onValueChange = { v -> cellVal = v; grid[r][c] = v },
                  singleLine = true,
                  textStyle = TextStyle(
                    color = Color(0xFF111827),
                    fontSize = 13.sp,
                    fontWeight = if (r == 0) FontWeight.Bold else FontWeight.Normal
                  ),
                  cursorBrush = SolidColor(TeachColors.AccentTeal),
                  modifier = Modifier.fillMaxWidth()
                )
              }
            }
          }
        }
      }

      // Always-visible Table Color picker (updates border, cell text, and header row tint!)
      SectionLabel(stringResource(R.string.color_label))
      ColorSwatchTray(
        colors = TeachColors.BrushSwatches,
        selectedColor = borderColor,
        accentColor = TeachColors.AccentTeal,
        onSelect = { borderColor = it },
        onToggleCustom = { showCustomColor = !showCustomColor }
      )
      if (showCustomColor) CustomRgbSliders(borderColor) { borderColor = it }
    }
  }
}

// ===== 9. Layers Right-Side Slide-Out Drawer (Matching test_layers.png!) =====

@Composable
fun LayersDialog(
  layers: List<BoardLayer>,
  activeLayerId: String,
  onSelect: (String) -> Unit,
  onAdd: () -> Unit,
  onToggleVisible: (BoardLayer) -> Unit,
  onToggleLock: (BoardLayer) -> Unit,
  onReorder: (BoardLayer, Boolean) -> Unit,
  onClear: (BoardLayer) -> Unit,
  onDelete: (BoardLayer) -> Unit,
  onDismiss: () -> Unit
) {
  val config = LocalConfiguration.current
  val drawerWidth = if (config.screenWidthDp > config.screenHeightDp) 0.44f else 0.74f
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    MaterialTheme(colorScheme = LightModalColorScheme) {
      Box(modifier = Modifier.fillMaxSize().clickable(onClick = onDismiss)) {
        Surface(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxWidth(drawerWidth)
            .fillMaxHeight()
            .clickable(enabled = false) {},
          shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
          color = Color.White,
          contentColor = Color(0xFF111827),
          shadowElevation = 14.dp
        ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(TeachColors.HeaderSlateTint)
              .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(TeachColors.AccentSlate),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Layers, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.tool_layers), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF111827), modifier = Modifier.weight(1f))
            IconButton(onClick = onAdd, enabled = layers.size < AppConfig.MAX_LAYERS_PER_BOARD, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.Add, contentDescription = "Add layer", tint = Color(0xFF1E293B))
            }
            IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
            }
          }
          HorizontalDivider(color = Color(0xFFE5E7EB))

          LazyColumn(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(layers.sortedByDescending { it.zOrder }, key = { it.id }) { layer ->
              val isSel = layer.id == activeLayerId
              Surface(
                modifier = Modifier
                  .fillMaxWidth()
                  .clip(RoundedCornerShape(14.dp))
                  .border(if (isSel) 2.dp else 1.dp, if (isSel) TeachColors.AccentSlate else TeachColors.TrayBorder, RoundedCornerShape(14.dp))
                  .clickable { onSelect(layer.id) },
                color = if (isSel) TeachColors.TrayBg else Color.White
              ) {
                Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp)) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(onClick = { onToggleVisible(layer) }, modifier = Modifier.size(30.dp)) {
                      Icon(if (layer.visible) Icons.Default.Visibility else Icons.Default.VisibilityOff, contentDescription = "Visible", tint = Color(0xFF334155), modifier = Modifier.size(18.dp))
                    }
                    Box(modifier = Modifier.size(7.dp).clip(CircleShape).background(if (isSel) TeachColors.Primary else Color(0xFF94A3B8)))
                    Spacer(Modifier.width(8.dp))
                    Text(layer.name, modifier = Modifier.weight(1f), fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = Color(0xFF111827))
                    IconButton(onClick = { onToggleLock(layer) }, modifier = Modifier.size(28.dp)) {
                      Icon(if (layer.locked) Icons.Default.Lock else Icons.Default.LockOpen, contentDescription = "Lock", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                  }
                  Row(horizontalArrangement = Arrangement.End, modifier = Modifier.fillMaxWidth()) {
                    IconButton(onClick = { onReorder(layer, true) }, modifier = Modifier.size(28.dp)) {
                      Icon(Icons.Default.ArrowUpward, contentDescription = "Up", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onReorder(layer, false) }, modifier = Modifier.size(28.dp)) {
                      Icon(Icons.Default.ArrowDownward, contentDescription = "Down", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                    IconButton(onClick = { onClear(layer) }, modifier = Modifier.size(28.dp)) {
                      Icon(Icons.Default.DeleteSweep, contentDescription = "Clear", tint = Color(0xFF64748B), modifier = Modifier.size(16.dp))
                    }
                    if (layers.size > 1) {
                      IconButton(onClick = { onDelete(layer) }, modifier = Modifier.size(28.dp)) {
                        Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TeachColors.Danger, modifier = Modifier.size(16.dp))
                      }
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
    }
  }
}

// ===== 10. Pages Dialog =====

@Composable
fun PagesDialog(
  pageCount: Int,
  currentPage: Int,
  bookmarks: Set<Int>,
  onSelectPage: (Int) -> Unit,
  onAddPage: () -> Unit,
  onDuplicatePage: () -> Unit,
  onDeletePage: (Int) -> Unit,
  onDismiss: () -> Unit
) {
  PastelModalDialog(
    title = "${stringResource(R.string.pages_title)} ($pageCount)",
    icon = Icons.Default.Layers,
    badgeColor = TeachColors.Primary,
    headerTint = TeachColors.HeaderSkyTint,
    onDismiss = onDismiss
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { onAddPage(); onDismiss() }, modifier = Modifier.weight(1f)) {
          Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(4.dp))
          Text(stringResource(R.string.new_page))
        }
        OutlinedButton(onClick = { onDuplicatePage(); onDismiss() }, modifier = Modifier.weight(1f)) {
          Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(16.dp))
          Spacer(Modifier.width(4.dp))
          Text(stringResource(R.string.duplicate))
        }
      }
      LazyColumn(modifier = Modifier.heightIn(max = 340.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
        items((0 until pageCount).toList()) { idx ->
          val isCur = idx == currentPage
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .clip(RoundedCornerShape(12.dp))
              .border(if (isCur) 2.dp else 1.dp, if (isCur) TeachColors.Primary else TeachColors.TrayBorder, RoundedCornerShape(12.dp))
              .clickable { onSelectPage(idx); onDismiss() },
            color = if (isCur) TeachColors.HeaderSkyTint else TeachColors.TrayBg
          ) {
            Row(modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp), verticalAlignment = Alignment.CenterVertically) {
              Text("${stringResource(R.string.page)} ${idx + 1}", modifier = Modifier.weight(1f), fontWeight = if (isCur) FontWeight.Bold else FontWeight.Medium, color = Color(0xFF111827))
              if (idx in bookmarks) {
                Icon(Icons.Default.Bookmark, contentDescription = null, tint = TeachColors.AccentOrange, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(6.dp))
              }
              if (pageCount > 1) {
                IconButton(onClick = { onDeletePage(idx) }, modifier = Modifier.size(30.dp)) {
                  Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TeachColors.Danger, modifier = Modifier.size(18.dp))
                }
              }
            }
          }
        }
      }
    }
  }
}

// ===== 11. OCR Text Scanner Card (Matching ref_25_ocr.png — 100% Free Unlimited!) =====

@Composable
fun OcrDialog(
  selectedScript: OcrScript,
  onSelectScript: (OcrScript) -> Unit,
  ocrResult: OcrOutput?,
  isProcessing: Boolean,
  recentScans: List<OcrScanRecord>,
  onTakePhoto: () -> Unit,
  onChooseGallery: () -> Unit,
  onInsertText: (String) -> Unit,
  onCopyText: (String) -> Unit,
  onShareText: (String) -> Unit,
  onDeleteScan: (Long) -> Unit,
  onDismiss: () -> Unit
) {
  var showHistory by remember { mutableStateOf(false) }
  var editableText by remember(ocrResult?.text) { mutableStateOf(ocrResult?.text.orEmpty()) }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.88f).heightIn(max = 650.dp),
      shape = RoundedCornerShape(26.dp),
      color = Color.White,
      shadowElevation = 14.dp
    ) {
      Column(
        modifier = Modifier.verticalScroll(rememberScrollState()).padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        Icon(Icons.Default.DocumentScanner, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(40.dp))
        Text(stringResource(R.string.ocr_title), fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color(0xFF111827))
        Text(stringResource(R.string.ocr_offline_note), fontSize = 12.sp, color = Color(0xFF64748B), textAlign = TextAlign.Center)
        HorizontalDivider(color = Color(0xFFE5E7EB))

        if (!showHistory) {
          // Script selector
          Row(
            modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            OcrScript.entries.forEach { script ->
              FilterChip(selected = selectedScript == script, onClick = { onSelectScript(script) }, label = { Text(script.label, fontSize = 11.sp) })
            }
          }
          if (!selectedScript.supportedOnDevice) {
            Text(stringResource(R.string.ocr_script_not_supported), color = TeachColors.Danger, fontSize = 12.sp)
          }

          // Clean 3 action rows matching ref_25_ocr.png!
          OcrActionRow(Icons.Default.CameraAlt, stringResource(R.string.take_photo), onTakePhoto)
          OcrActionRow(Icons.Default.PhotoLibrary, stringResource(R.string.ocr_choose_gallery), onChooseGallery)
          OcrActionRow(Icons.Default.History, "${stringResource(R.string.ocr_scan_history)} (${recentScans.size})") { showHistory = true }

          if (isProcessing) {
            CircularProgressIndicator(modifier = Modifier.padding(12.dp))
          } else if (ocrResult != null) {
            ocrResult.message?.let { Text(it, color = TeachColors.AccentOrange, fontSize = 12.sp) }
            OutlinedTextField(
              value = editableText,
              onValueChange = { editableText = it },
              modifier = Modifier.fillMaxWidth().heightIn(min = 100.dp, max = 190.dp),
              shape = RoundedCornerShape(14.dp),
              placeholder = { Text(stringResource(R.string.ocr_result_hint)) }
            )
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
              Button(
                onClick = { onInsertText(editableText); onDismiss() },
                enabled = editableText.isNotBlank(),
                modifier = Modifier.weight(1f)
              ) { Text(stringResource(R.string.insert_on_board)) }
              IconButton(onClick = { if (editableText.isNotBlank()) onCopyText(editableText) }) {
                Icon(Icons.Default.ContentCopy, contentDescription = "Copy", tint = Color(0xFF334155))
              }
              IconButton(onClick = { if (editableText.isNotBlank()) onShareText(editableText) }) {
                Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF334155))
              }
            }
          }
        } else {
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            TextButton(onClick = { showHistory = false }) { Text("← ${stringResource(R.string.ocr_scan)}") }
            Spacer(Modifier.weight(1f))
            Text("${recentScans.size}/20", fontSize = 12.sp, color = Color(0xFF64748B))
          }
          if (recentScans.isEmpty()) {
            Text(stringResource(R.string.no_ocr_history), color = Color(0xFF64748B), modifier = Modifier.padding(24.dp))
          } else {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
              recentScans.forEach { scan ->
                Surface(shape = RoundedCornerShape(12.dp), color = TeachColors.TrayBg, modifier = Modifier.fillMaxWidth()) {
                  Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                    Column(modifier = Modifier.weight(1f)) {
                      Text(scan.language, fontSize = 11.sp, color = TeachColors.Primary, fontWeight = FontWeight.Bold)
                      Text(scan.recognizedText, maxLines = 3, overflow = TextOverflow.Ellipsis, fontSize = 13.sp, color = Color(0xFF111827))
                    }
                    TextButton(onClick = { onInsertText(scan.recognizedText); onDismiss() }) { Text(stringResource(R.string.insert)) }
                    IconButton(onClick = { onDeleteScan(scan.id) }, modifier = Modifier.size(28.dp)) {
                      Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TeachColors.Danger, modifier = Modifier.size(16.dp))
                    }
                  }
                }
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun OcrActionRow(icon: ImageVector, label: String, onClick: () -> Unit) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(14.dp))
      .clickable(onClick = onClick),
    color = Color(0xFFFAFAFC)
  ) {
    Row(
      modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      Icon(icon, contentDescription = null, tint = Color(0xFF2563EB), modifier = Modifier.size(22.dp))
      Text(label, fontSize = 15.sp, fontWeight = FontWeight.Medium, color = Color(0xFF111827))
    }
  }
}

// ===== 12. Wikimedia Image Search Right-Side Slide-Out Drawer (Matching ref_26_web_image.png!) =====

@Composable
fun WikimediaSearchDialog(
  onPickImage: (WikimediaImage) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  var query by remember { mutableStateOf("") }
  var results by remember { mutableStateOf<List<WikimediaImage>>(emptyList()) }
  var loading by remember { mutableStateOf(false) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var offset by remember { mutableIntStateOf(0) }

  fun runSearch(qOverride: String = query, reset: Boolean = true) {
    val q = qOverride.trim()
    if (q.isBlank()) return
    query = q
    if (!WikimediaSearch.isOnline(context)) {
      errorMsg = context.getString(R.string.offline_error)
      return
    }
    scope.launch {
      loading = true
      errorMsg = null
      val nextOffset = if (reset) 0 else offset + 30
      val found = WikimediaSearch.search(q, offset = nextOffset)
      results = if (reset) found else results + found
      offset = nextOffset
      loading = false
      if (results.isEmpty()) errorMsg = context.getString(R.string.no_results)
    }
  }

  val config = LocalConfiguration.current
  val drawerWidth = if (config.screenWidthDp > config.screenHeightDp) 0.48f else 0.76f
  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    MaterialTheme(colorScheme = LightModalColorScheme) {
      Box(modifier = Modifier.fillMaxSize().clickable(onClick = onDismiss)) {
        Surface(
          modifier = Modifier
            .align(Alignment.CenterEnd)
            .fillMaxWidth(drawerWidth)
            .fillMaxHeight()
            .clickable(enabled = false) {},
          shape = RoundedCornerShape(topStart = 20.dp, bottomStart = 20.dp),
          color = Color.White,
          contentColor = Color(0xFF111827),
          shadowElevation = 14.dp
        ) {
        Column(modifier = Modifier.fillMaxSize().statusBarsPadding().navigationBarsPadding()) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(TeachColors.HeaderCyanTint)
              .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier.size(30.dp).clip(RoundedCornerShape(9.dp)).background(TeachColors.AccentCyan),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Search, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.image_search_title), fontWeight = FontWeight.Bold, fontSize = 17.sp, color = Color(0xFF111827), modifier = Modifier.weight(1f))
            IconButton(onClick = onDismiss, modifier = Modifier.size(30.dp)) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = Color(0xFF64748B))
            }
          }
          HorizontalDivider(color = Color(0xFFE5E7EB))

          Column(modifier = Modifier.weight(1f).padding(12.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            OutlinedTextField(
              value = query,
              onValueChange = { query = it },
              modifier = Modifier.fillMaxWidth(),
              singleLine = true,
              shape = RoundedCornerShape(14.dp),
              placeholder = { Text(stringResource(R.string.search_wikimedia_hint), fontSize = 13.sp) },
              leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, tint = TeachColors.AccentCyan) },
              trailingIcon = {
                if (query.isNotBlank()) {
                  IconButton(onClick = { runSearch(query, reset = true) }) {
                    Icon(Icons.Default.Check, contentDescription = "Go", tint = TeachColors.AccentCyan)
                  }
                }
              }
            )

            errorMsg?.let { Text(it, color = TeachColors.Danger, fontSize = 12.sp) }

            if (loading && results.isEmpty()) {
              Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = TeachColors.AccentCyan)
              }
            } else if (results.isEmpty()) {
              Column(
                modifier = Modifier.fillMaxWidth().weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Text("Try searching:", fontSize = 12.sp, color = Color(0xFF64748B))
                Spacer(Modifier.height(8.dp))
                listOf("solar system", "human heart", "triangle", "plant cell", "map of india").forEach { topic ->
                  TextButton(onClick = { runSearch(topic, reset = true) }) {
                    Text("• $topic", color = TeachColors.AccentCyan, fontSize = 13.sp)
                  }
                }
              }
            } else {
              Text("${results.size} ${stringResource(R.string.results_tap_add)}", fontSize = 11.sp, color = Color(0xFF64748B))
              LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
              ) {
                items(results) { img ->
                  Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = TeachColors.TrayBg,
                    modifier = Modifier.clickable { onPickImage(img); onDismiss() }
                  ) {
                    Box(modifier = Modifier.aspectRatio(1f)) {
                      AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                          .data(img.thumbUrl)
                          .setHeader("User-Agent", AppConfig.USER_AGENT)
                          .crossfade(true)
                          .build(),
                        contentDescription = img.title,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                      )
                      Box(
                        modifier = Modifier.align(Alignment.TopEnd).padding(5.dp).size(26.dp).clip(CircleShape).background(TeachColors.AccentCyan),
                        contentAlignment = Alignment.Center
                      ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(16.dp))
                      }
                    }
                  }
                }
              }
              if (!loading) {
                OutlinedButton(onClick = { runSearch(query, reset = false) }, modifier = Modifier.fillMaxWidth()) {
                  Text(stringResource(R.string.load_more))
                }
              }
            }
          }

          HorizontalDivider(color = Color(0xFFE5E7EB))
          Text(
            "Images from Wikimedia Commons (CC)",
            fontSize = 11.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp)
          )
        }
      }
    }
    }
  }
}

// ===== 13. Stickers & Science Stickers (Matching ref_18_stickers.png & ref_19_science.png!) =====

@Composable
fun StickerPickerDialog(
  group: StickerGroup,
  initialColor: Int,
  onPick: (String, Int) -> Unit,
  onDismiss: () -> Unit
) {
  val all = remember(group) { StickerCatalog.byGroup(group) }
  val categories = remember(all) { all.map { it.category }.distinct() }
  var selectedCat by remember(group) { mutableStateOf(if (group == StickerGroup.SCIENCE) "Atoms" else categories.first()) }
  var search by remember { mutableStateOf("") }

  val filtered = remember(all, selectedCat, search) {
    all.filter { spec ->
      (search.isNotBlank() && (spec.label.contains(search, true) || spec.category.contains(search, true))) ||
        (search.isBlank() && spec.category == selectedCat)
    }
  }

  val isGeneral = group == StickerGroup.GENERAL
  val accent = if (isGeneral) TeachColors.AccentPink else TeachColors.AccentEmerald
  val headerTint = if (isGeneral) TeachColors.HeaderPinkTint else TeachColors.HeaderMintTint

  PastelModalDialog(
    title = if (isGeneral) stringResource(R.string.stickers_title) else stringResource(R.string.tool_science),
    icon = if (isGeneral) Icons.Default.EmojiEmotions else Icons.Default.Science,
    badgeColor = accent,
    headerTint = headerTint,
    onDismiss = onDismiss
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      // Bold pill category tabs
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categories.forEach { cat ->
          val sel = cat == selectedCat && search.isBlank()
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = if (sel) accent else Color.White,
            modifier = Modifier
              .border(1.dp, if (sel) accent else TeachColors.TrayBorder, RoundedCornerShape(14.dp))
              .clickable { selectedCat = cat; search = "" }
          ) {
            Text(
              cat,
              fontSize = 13.sp,
              fontWeight = FontWeight.Bold,
              color = if (sel) Color.White else Color(0xFF1E293B),
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp)
            )
          }
        }
      }

      if (!isGeneral) {
        OutlinedTextField(
          value = search,
          onValueChange = { search = it },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          placeholder = { Text(stringResource(R.string.search_stickers_hint), fontSize = 12.sp) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) }
        )
      }

      val catIdx = (categories.indexOf(selectedCat) + 1).coerceAtLeast(1)
      Text(
        "$catIdx / ${categories.size}  ·  ${filtered.size} items",
        fontSize = 11.sp,
        color = Color(0xFF64748B),
        modifier = Modifier.align(Alignment.CenterHorizontally)
      )

      // 3-column large crisp cards!
      LazyVerticalGrid(
        columns = GridCells.Fixed(3),
        modifier = Modifier.heightIn(max = 370.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        items(filtered, key = { it.id }) { spec ->
          Surface(
            modifier = Modifier
              .aspectRatio(1f)
              .clip(RoundedCornerShape(16.dp))
              .border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp))
              .clickable { onPick(spec.id, initialColor); onDismiss() },
            color = Color.White
          ) {
            Box(
              modifier = Modifier
                .fillMaxSize()
                .padding(7.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(if (!isGeneral) Color(0xFFEDF2F7) else Color.White),
              contentAlignment = Alignment.Center
            ) {
              Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
                val previewCol = if (initialColor == TeachColors.BrushWhite) TeachColors.BrushBlack else initialColor
                StickerCatalog.drawSticker(drawContext.canvas.nativeCanvas, spec.id, previewCol, size.width, size.height)
              }
            }
          }
        }
      }
    }
  }
}

// ===== 14. Laser Pointer Bottom Card (Matching ref_20_laser.png!) =====

@Composable
fun LaserDialog(
  currentColor: Int,
  currentSize: Float,
  onColorChange: (Int) -> Unit,
  onSizeChange: (Float) -> Unit,
  onDismiss: () -> Unit
) {
  BottomDockCard(
    title = stringResource(R.string.laser_pointer_title),
    icon = Icons.Default.FlashlightOn,
    badgeColor = TeachColors.AccentOrange,
    headerTint = TeachColors.HeaderPeachTint,
    onDismiss = onDismiss
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      SectionLabel(stringResource(R.string.color_label))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        TeachColors.LaserColors.forEach { c ->
          val sel = c == currentColor
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .then(if (sel) Modifier.border(2.5.dp, TeachColors.AccentOrange, CircleShape).padding(3.dp) else Modifier)
              .clip(CircleShape)
              .background(Color(c))
              .border(1.dp, Color(0x26000000), CircleShape)
              .clickable { onColorChange(c) }
          )
        }
      }

      SectionLabel(stringResource(R.string.size_label))
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        listOf(4f, 8f, 16f, 24f, 32f).forEach { s ->
          val sel = kotlin.math.abs(currentSize - s) < 1.5f
          Box(
            modifier = Modifier
              .weight(1f)
              .height(48.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(if (sel) TeachColors.HeaderPeachTint else Color.White)
              .border(if (sel) 2.dp else 1.dp, if (sel) TeachColors.AccentOrange else TeachColors.TrayBorder, RoundedCornerShape(12.dp))
              .clickable { onSizeChange(s) }
          ) {
            Text("${s.toInt()}", fontWeight = FontWeight.Bold, color = if (sel) TeachColors.AccentOrange else Color(0xFF1E293B), modifier = Modifier.align(Alignment.Center))
            if (sel) CheckBadge(TeachColors.AccentOrange, Modifier.align(Alignment.TopEnd).padding(3.dp))
          }
        }
      }

      SectionLabel("CUSTOM  ·  ${currentSize.toInt()}")
      Slider(
        value = currentSize,
        onValueChange = onSizeChange,
        valueRange = 4f..36f,
        colors = SliderDefaults.colors(thumbColor = TeachColors.AccentOrange, activeTrackColor = TeachColors.AccentOrange, inactiveTrackColor = Color(0xFFDCE3EC))
      )
    }
  }
}

// ===== 15. Screen Recorder Dialog (Matching ref_23_record.png — 100% Free Unlimited!) =====

@Composable
fun ScreenRecorderDialog(
  onStartRecording: (quality: RecordingQuality, recordAudio: Boolean, showFaceCam: Boolean, faceCamShape: FaceCamShape) -> Unit,
  onDismiss: () -> Unit
) {
  var quality by remember { mutableStateOf(RecordingQuality.P1080) }
  var audio by remember { mutableStateOf(true) }
  var faceCam by remember { mutableStateOf(false) }
  var camShape by remember { mutableStateOf(FaceCamShape.ROUND) }

  PastelModalDialog(
    title = stringResource(R.string.record_screen_title),
    icon = Icons.Default.RadioButtonChecked,
    badgeColor = TeachColors.AccentRose,
    headerTint = TeachColors.HeaderRoseTint,
    onDismiss = onDismiss
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      // Card 1: Record my voice
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth().border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp))
      ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Mic, contentDescription = null, tint = TeachColors.AccentRose, modifier = Modifier.size(22.dp))
          Spacer(Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.recorder_voice), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF111827))
            Text("Your microphone is recorded.", fontSize = 12.sp, color = Color(0xFF64748B))
          }
          Switch(checked = audio, onCheckedChange = { audio = it }, colors = SwitchDefaults.colors(checkedTrackColor = TeachColors.AccentRose))
        }
      }

      // Card 2: Face cam
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth().border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp))
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFF64748B), modifier = Modifier.size(22.dp))
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(stringResource(R.string.recorder_facecam), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF111827))
              Text("Show your face in the video.", fontSize = 12.sp, color = Color(0xFF64748B))
            }
            Switch(checked = faceCam, onCheckedChange = { faceCam = it }, colors = SwitchDefaults.colors(checkedTrackColor = TeachColors.AccentRose))
          }
          if (faceCam) {
            Spacer(Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              FilterChip(selected = camShape == FaceCamShape.ROUND, onClick = { camShape = FaceCamShape.ROUND }, label = { Text(stringResource(R.string.round)) })
              FilterChip(selected = camShape == FaceCamShape.SQUARE, onClick = { camShape = FaceCamShape.SQUARE }, label = { Text(stringResource(R.string.square)) })
            }
          }
        }
      }

      // Card 3: Video quality
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        modifier = Modifier.fillMaxWidth().border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(16.dp))
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Text(stringResource(R.string.video_quality), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = Color(0xFF111827))
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecordingQuality.entries.forEach { q ->
              val sel = quality == q
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .height(40.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .border(1.dp, if (sel) TeachColors.AccentRose else TeachColors.TrayBorder, RoundedCornerShape(12.dp))
                  .clickable { quality = q },
                color = if (sel) TeachColors.AccentRose else Color.White
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(if (q == RecordingQuality.P1080) "Max" else q.label, fontWeight = FontWeight.Bold, color = if (sel) Color.White else Color(0xFF1E293B))
                }
              }
            }
          }
          Text(
            "${quality.label} · ${quality.fps} fps · about ${quality.mbPerMinEstimate}",
            fontSize = 12.sp,
            color = Color(0xFF64748B),
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      // 100% Free Unlimited Start Recording Button!
      Button(
        onClick = { onStartRecording(quality, audio, faceCam, camShape); onDismiss() },
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(16.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TeachColors.AccentRose)
      ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
          Text(stringResource(R.string.start_recording), fontWeight = FontWeight.Bold, fontSize = 15.sp)
          Text("Unlimited · 100% Free · No Watermark", fontSize = 10.sp, color = Color.White.copy(alpha = 0.88f))
        }
      }
    }
  }
}

// ===== 16. Share & PDF Export Range Dialog =====

@Composable
fun ShareExportDialog(
  pageCount: Int,
  currentPage: Int,
  onSavePng: () -> Unit,
  onSharePng: () -> Unit,
  onExportPdf: (List<Int>, Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  var rangeMode by remember { mutableIntStateOf(0) }
  var customRangeText by remember { mutableStateOf("1-$pageCount") }

  fun resolveIndices(): List<Int> = when (rangeMode) {
    0 -> listOf(currentPage)
    1 -> (0 until pageCount).toList()
    else -> parsePageRange(customRangeText, pageCount).ifEmpty { listOf(currentPage) }
  }

  PastelModalDialog(
    title = stringResource(R.string.export_share_title),
    icon = Icons.Default.Share,
    badgeColor = TeachColors.Primary,
    headerTint = TeachColors.HeaderSkyTint,
    onDismiss = onDismiss
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(onClick = { onSavePng(); onDismiss() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
          Icon(Icons.Default.Image, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(4.dp))
          Text(stringResource(R.string.save_png))
        }
        OutlinedButton(onClick = { onSharePng(); onDismiss() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(4.dp))
          Text(stringResource(R.string.share_png))
        }
      }

      HorizontalDivider(color = Color(0xFFE5E7EB))
      SectionLabel(stringResource(R.string.export_pdf_title))

      Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        FilterChip(selected = rangeMode == 0, onClick = { rangeMode = 0 }, label = { Text(stringResource(R.string.this_page)) })
        FilterChip(selected = rangeMode == 1, onClick = { rangeMode = 1 }, label = { Text("${stringResource(R.string.all_pages)} ($pageCount)") })
        if (pageCount > 1) {
          FilterChip(selected = rangeMode == 2, onClick = { rangeMode = 2 }, label = { Text(stringResource(R.string.page_range)) })
        }
      }
      if (rangeMode == 2) {
        OutlinedTextField(
          value = customRangeText,
          onValueChange = { customRangeText = it },
          modifier = Modifier.fillMaxWidth(),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          label = { Text(stringResource(R.string.page_range_hint)) }
        )
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Button(
          onClick = { onExportPdf(resolveIndices(), false); onDismiss() },
          modifier = Modifier.weight(1f),
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = TeachColors.AccentTeal)
        ) {
          Icon(Icons.Default.PictureAsPdf, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(4.dp))
          Text(stringResource(R.string.save_pdf))
        }
        OutlinedButton(onClick = { onExportPdf(resolveIndices(), true); onDismiss() }, modifier = Modifier.weight(1f), shape = RoundedCornerShape(12.dp)) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(4.dp))
          Text(stringResource(R.string.share_pdf))
        }
      }
    }
  }
}

fun parsePageRange(input: String, maxPages: Int): List<Int> {
  val out = linkedSetOf<Int>()
  input.split(",").forEach { token ->
    val t = token.trim()
    if (t.contains("-")) {
      val parts = t.split("-")
      val a = parts.getOrNull(0)?.trim()?.toIntOrNull()
      val b = parts.getOrNull(1)?.trim()?.toIntOrNull()
      if (a != null && b != null) {
        val lo = minOf(a, b).coerceIn(1, maxPages)
        val hi = maxOf(a, b).coerceIn(1, maxPages)
        for (p in lo..hi) out.add(p - 1)
      }
    } else {
      val n = t.toIntOrNull()
      if (n != null && n in 1..maxPages) out.add(n - 1)
    }
  }
  return out.toList()
}

// ===== 17. Dark Add Shortcuts Modal (Matching ref_16_shortcuts.png!) =====

@Composable
fun ShortcutsDialog(
  selectedTools: List<ToolbarAction>,
  onSave: (List<ToolbarAction>) -> Unit,
  onDismiss: () -> Unit
) {
  val chosen = remember { mutableStateListOf<ToolbarAction>().apply { addAll(selectedTools) } }
  val candidates = remember {
    ToolbarAction.entries.filter { it != ToolbarAction.CUSTOMIZE && it != ToolbarAction.HELP }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.90f).heightIn(max = 580.dp),
      shape = RoundedCornerShape(22.dp),
      color = Color(0xFF1F2124),
      shadowElevation = 14.dp
    ) {
      Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Text(stringResource(R.string.shortcuts_title), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
          Text("${chosen.size}/${AppConfig.MAX_SHORTCUTS}", color = Color(0xFF9CA3AF), fontSize = 13.sp)
          TextButton(onClick = onDismiss) { Text(stringResource(R.string.cancel), color = Color(0xFF9CA3AF)) }
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = Color(0xFF2196F3),
            modifier = Modifier.clickable { onSave(chosen.toList()); onDismiss() }
          ) {
            Text(stringResource(R.string.done), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp, modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp))
          }
        }
        Text("Select tools for quick access (max ${AppConfig.MAX_SHORTCUTS})", color = Color(0xFF9CA3AF), fontSize = 12.sp)

        LazyColumn(modifier = Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(candidates) { action ->
            val checked = action in chosen
            Surface(
              shape = RoundedCornerShape(12.dp),
              color = Color(0xFF2A2D31),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  if (checked) chosen.remove(action)
                  else if (chosen.size < AppConfig.MAX_SHORTCUTS) chosen.add(action)
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(toolbarIconFor(action), contentDescription = null, tint = Color(0xFFD1D5DB), modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                  action.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                  color = Color.White,
                  fontSize = 14.sp,
                  modifier = Modifier.weight(1f)
                )
                Checkbox(
                  checked = checked,
                  onCheckedChange = {
                    if (it && chosen.size < AppConfig.MAX_SHORTCUTS) chosen.add(action)
                    else if (!it) chosen.remove(action)
                  },
                  colors = CheckboxDefaults.colors(checkedColor = Color(0xFF2196F3), uncheckedColor = Color(0xFF9CA3AF))
                )
              }
            }
          }
        }
      }
    }
  }
}

// ===== 18. Dark Customize Toolbar Dialog (Reorder + Enable/Disable + Reset) =====

private val EssentialToolbarActions = setOf(
  ToolbarAction.SAVE,
  ToolbarAction.SHARE,
  ToolbarAction.UNDO,
  ToolbarAction.REDO,
  ToolbarAction.CLEAR,
  ToolbarAction.PEN,
  ToolbarAction.ERASER,
  ToolbarAction.BOARD
)

@Composable
fun CustomizeToolbarDialog(
  currentOrder: List<ToolbarAction>,
  hiddenTools: Set<ToolbarAction>,
  onSave: (List<ToolbarAction>, Set<ToolbarAction>) -> Unit,
  onReset: () -> Unit,
  onDismiss: () -> Unit
) {
  val customizable = remember(currentOrder) {
    mutableStateListOf<ToolbarAction>().apply {
      addAll(currentOrder.filter { it != ToolbarAction.CUSTOMIZE && it != ToolbarAction.HELP })
    }
  }
  var hidden by remember(hiddenTools) { mutableStateOf(hiddenTools) }

  fun persist() {
    onSave(customizable.toList() + listOf(ToolbarAction.CUSTOMIZE, ToolbarAction.HELP), hidden)
  }

  Dialog(
    onDismissRequest = { persist(); onDismiss() },
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier.fillMaxSize(),
      color = Color(0xFF17181A)
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Text(
            "Customize Toolbar",
            color = Color.White,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            modifier = Modifier.weight(1f)
          )
          Surface(
            shape = RoundedCornerShape(20.dp),
            color = Color(0xFF3A2817),
            modifier = Modifier.clickable {
              customizable.clear()
              customizable.addAll(ToolbarAction.entries.filter { it != ToolbarAction.CUSTOMIZE && it != ToolbarAction.HELP })
              hidden = emptySet()
              onReset()
            }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 7.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(5.dp)
            ) {
              Icon(Icons.Default.Refresh, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(16.dp))
              Text("Reset", color = Color(0xFFF59E0B), fontWeight = FontWeight.SemiBold, fontSize = 13.sp)
            }
          }
          Spacer(Modifier.width(8.dp))
          IconButton(onClick = { persist(); onDismiss() }) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White)
          }
        }

        Text("Reorder tools with arrows · Enable/disable optional tools", color = Color(0xFF9CA3AF), fontSize = 13.sp)

        LazyColumn(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          items(customizable.size) { index ->
            val action = customizable[index]
            val isEssential = action in EssentialToolbarActions
            val isEnabled = isEssential || action !in hidden
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = Color(0xFF232528),
              modifier = Modifier.fillMaxWidth()
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(Icons.Default.DragIndicator, contentDescription = null, tint = Color(0xFF6B7280), modifier = Modifier.size(20.dp))
                IconButton(
                  onClick = {
                    if (index > 0) {
                      val item = customizable.removeAt(index)
                      customizable.add(index - 1, item)
                      persist()
                    }
                  },
                  enabled = index > 0,
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(Icons.Default.ArrowUpward, contentDescription = "Move Up", tint = if (index > 0) Color(0xFF9CA3AF) else Color(0xFF3F434A), modifier = Modifier.size(16.dp))
                }
                IconButton(
                  onClick = {
                    if (index < customizable.lastIndex) {
                      val item = customizable.removeAt(index)
                      customizable.add(index + 1, item)
                      persist()
                    }
                  },
                  enabled = index < customizable.lastIndex,
                  modifier = Modifier.size(28.dp)
                ) {
                  Icon(Icons.Default.ArrowDownward, contentDescription = "Move Down", tint = if (index < customizable.lastIndex) Color(0xFF9CA3AF) else Color(0xFF3F434A), modifier = Modifier.size(16.dp))
                }
                Spacer(Modifier.width(4.dp))
                Icon(toolbarIconFor(action), contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                Spacer(Modifier.width(12.dp))
                Text(
                  text = when (action) {
                    ToolbarAction.CLEAR -> "Clear Canvas"
                    ToolbarAction.PEN -> "Pen / Brushes"
                    ToolbarAction.BOARD -> "Board Color"
                    ToolbarAction.WEB_IMAGE -> "Web Image"
                    ToolbarAction.IMPORT_PDF -> "Import PDF"
                    ToolbarAction.HAND -> "Hand / Pan"
                    else -> action.name.lowercase().replaceFirstChar { it.uppercase() }
                  },
                  color = Color.White,
                  fontSize = 15.sp,
                  modifier = Modifier.weight(1f)
                )
                if (isEssential) {
                  Text("Essential", color = Color(0xFF22C55E), fontSize = 12.sp, fontWeight = FontWeight.Medium)
                  Spacer(Modifier.width(10.dp))
                }
                Switch(
                  checked = isEnabled,
                  enabled = !isEssential,
                  onCheckedChange = { on ->
                    hidden = if (on) hidden - action else hidden + action
                    persist()
                  },
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = Color(0xFF22C55E),
                    disabledCheckedThumbColor = Color.White,
                    disabledCheckedTrackColor = Color(0xFF9CA3AF)
                  )
                )
              }
            }
          }
        }
      }
    }
  }
}

// ===== 19. Animated Interactive Feature Guides ("Hold to move" & "Smart Shapes") =====

@Composable
fun HoldToMoveGuideDialog(onDismiss: () -> Unit) {
  val infinite = rememberInfiniteTransition(label = "holdGuide")
  val phase by infinite.animateFloat(
    initialValue = 0f,
    targetValue = 4f,
    animationSpec = infiniteRepeatable(tween(6400, easing = LinearEasing), RepeatMode.Restart),
    label = "holdPhase"
  )
  val activeStep = (phase.toInt() % 4) + 1
  val steps = listOf(
    "Write on it: it stays in place",
    "Hold it for half a second",
    "Drag to move, resize or copy it",
    "Tap the Pen to write again"
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.90f),
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF222428),
      shadowElevation = 16.dp
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Text("Hold to move", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 20.sp)

        // Live animated notebook card demo
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(172.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color.White)
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            // Ruled notebook lines
            for (i in 1..5) {
              val y = h * (i / 6f)
              drawLine(Color(0xFFE5E7EB), Offset(0f, y), Offset(w, y), strokeWidth = 2f)
            }
            // Object shifts smoothly during step 3
            val dragProgress = if (activeStep == 3) (phase - 2f).coerceIn(0f, 1f) else if (activeStep == 4) 1f else 0f
            val cardW = w * 0.36f
            val cardH = h * 0.50f
            val left = w * 0.18f + dragProgress * (w * 0.24f)
            val top = h * 0.18f

            // Sample landscape card
            drawRoundRect(
              color = Color(0xFF93C5FD),
              topLeft = Offset(left, top),
              size = Size(cardW, cardH),
              cornerRadius = CornerRadius(14f, 14f)
            )
            drawCircle(Color(0xFFFACC15), radius = cardH * 0.18f, center = Offset(left + cardW * 0.72f, top + cardH * 0.30f))
            val hill = Path().apply {
              moveTo(left, top + cardH)
              lineTo(left, top + cardH * 0.65f)
              quadraticTo(left + cardW * 0.30f, top + cardH * 0.42f, left + cardW * 0.60f, top + cardH * 0.72f)
              quadraticTo(left + cardW * 0.80f, top + cardH * 0.54f, left + cardW, top + cardH * 0.68f)
              lineTo(left + cardW, top + cardH)
              close()
            }
            drawPath(hill, Color(0xFF4ADE80))

            // Step 1: Circle drawn around the sun while card stays put
            if (activeStep == 1) {
              val t = phase.coerceIn(0f, 1f)
              drawArc(
                color = Color(0xFF1E3A8A),
                startAngle = -90f,
                sweepAngle = 360f * t,
                useCenter = false,
                topLeft = Offset(left + cardW * 0.50f, top + cardH * 0.08f),
                size = Size(cardH * 0.44f, cardH * 0.44f),
                style = Stroke(width = 5f, cap = StrokeCap.Round)
              )
            }
            // Step 2: Hold pulse ring
            if (activeStep == 2) {
              val t = (phase - 1f).coerceIn(0f, 1f)
              drawCircle(
                color = Color(0xFF2563EB).copy(alpha = 0.28f * (1f - t * 0.5f)),
                radius = 18f + t * 28f,
                center = Offset(left + cardW * 0.5f, top + cardH * 0.5f)
              )
              drawCircle(Color(0xFF2563EB), radius = 10f, center = Offset(left + cardW * 0.5f, top + cardH * 0.5f))
            }
            // Step 2 late + Step 3: Dashed selection box + Copy/Lock/Delete dots
            if ((activeStep == 2 && phase > 1.45f) || activeStep == 3) {
              drawRect(
                color = Color(0xFF2563EB),
                topLeft = Offset(left - 6f, top - 6f),
                size = Size(cardW + 12f, cardH + 12f),
                style = Stroke(width = 3.5f, pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f)))
              )
              drawCircle(Color(0xFF2563EB), 10f, Offset(left + cardW * 0.35f, top - 20f))
              drawCircle(Color(0xFF2563EB), 10f, Offset(left + cardW * 0.52f, top - 20f))
              drawCircle(Color(0xFFEF4444), 10f, Offset(left + cardW * 0.69f, top - 20f))
            }
            // Bottom mini toolbar strip
            val barTop = h * 0.78f
            drawRoundRect(
              color = Color(0xFFF1F5F9),
              topLeft = Offset(12f, barTop),
              size = Size(w - 24f, h - barTop - 8f),
              cornerRadius = CornerRadius(18f, 18f)
            )
            val penColor = if (activeStep == 4) Color(0xFF2563EB) else Color(0xFF475569)
            drawCircle(penColor, radius = 9f, center = Offset(w * 0.14f, barTop + (h - barTop - 8f) / 2f))
            for (k in 1..4) {
              drawCircle(Color(0xFFCBD5E1), radius = 6f, center = Offset(w * (0.14f + k * 0.18f), barTop + (h - barTop - 8f) / 2f))
            }
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          steps.forEachIndexed { idx, text ->
            val num = idx + 1
            val active = num == activeStep
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
              Box(
                modifier = Modifier
                  .size(22.dp)
                  .clip(CircleShape)
                  .background(if (active) Color(0xFF2563EB) else Color(0xFF33363C)),
                contentAlignment = Alignment.Center
              ) {
                Text("$num", color = if (active) Color.White else Color(0xFF9CA3AF), fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
              Text(
                text,
                color = if (active) Color.White else Color(0xFF9CA3AF),
                fontSize = 14.sp,
                fontWeight = if (active) FontWeight.SemiBold else FontWeight.Normal
              )
            }
          }
        }

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          Button(
            onClick = onDismiss,
            shape = RoundedCornerShape(14.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
          ) {
            Text("Got it", fontWeight = FontWeight.Bold, modifier = Modifier.padding(horizontal = 16.dp))
          }
        }
      }
    }
  }
}

@Composable
fun SmartShapesGuideDialog(onDismiss: () -> Unit) {
  val infinite = rememberInfiniteTransition(label = "smartShapeGuide")
  val t by infinite.animateFloat(
    initialValue = 0f,
    targetValue = 1f,
    animationSpec = infiniteRepeatable(tween(2600, easing = LinearEasing), RepeatMode.Restart),
    label = "shapeT"
  )

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier.fillMaxWidth(0.84f),
      shape = RoundedCornerShape(24.dp),
      color = Color(0xFF24262A),
      shadowElevation = 16.dp
    ) {
      Column(modifier = Modifier.padding(20.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Icon(Icons.Default.Category, contentDescription = null, tint = Color(0xFF3B82F6), modifier = Modifier.size(20.dp))
          Spacer(Modifier.width(8.dp))
          Text("Smart Shapes", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp, modifier = Modifier.weight(1f))
          Text("NEW", color = Color(0xFF60A5FA), fontWeight = FontWeight.ExtraBold, fontSize = 11.sp)
        }

        // Animated rough-to-perfect triangle & circle preview
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(142.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFF1A1C1F))
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val snapped = t > 0.52f
            val stroke = Stroke(width = 6f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            if (!snapped) {
              val drawP = (t / 0.52f).coerceIn(0f, 1f)
              val tri = Path().apply {
                val p0 = Offset(w * 0.28f, h * 0.20f)
                val p1 = Offset(w * 0.42f, h * 0.78f)
                val p2 = Offset(w * 0.14f, h * 0.80f)
                moveTo(p0.x, p0.y)
                if (drawP < 0.35f) {
                  val f = drawP / 0.35f
                  lineTo(p0.x + (p1.x - p0.x) * f, p0.y + (p1.y - p0.y) * f)
                } else if (drawP < 0.70f) {
                  lineTo(p1.x, p1.y)
                  val f = (drawP - 0.35f) / 0.35f
                  lineTo(p1.x + (p2.x - p1.x) * f, p1.y + (p2.y - p1.y) * f)
                } else {
                  lineTo(p1.x, p1.y)
                  lineTo(p2.x, p2.y)
                  val f = (drawP - 0.70f) / 0.30f
                  lineTo(p2.x + (p0.x - p2.x) * f, p2.y + (p0.y - p2.y) * f)
                }
              }
              drawPath(tri, Color.White, style = stroke)
            } else {
              // Crisp snapped triangle & circle!
              val tri = Path().apply {
                moveTo(w * 0.28f, h * 0.18f)
                lineTo(w * 0.43f, h * 0.80f)
                lineTo(w * 0.13f, h * 0.80f)
                close()
              }
              drawPath(tri, Color.White, style = stroke)
              drawCircle(Color.White, radius = h * 0.30f, center = Offset(w * 0.72f, h * 0.50f), style = stroke)
            }
          }
        }

        Text(
          "Draw a shape and lift your finger. It turns into a perfect shape you can move, resize and rotate.",
          color = Color(0xFFD1D5DB),
          fontSize = 13.5.sp,
          lineHeight = 20.sp
        )

        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
          TextButton(onClick = onDismiss) {
            Text("Got it", color = Color(0xFF3B82F6), fontWeight = FontWeight.Bold, fontSize = 15.sp)
          }
        }
      }
    }
  }
}
