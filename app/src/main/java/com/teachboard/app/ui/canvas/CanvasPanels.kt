package com.teachboard.app.ui.canvas

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
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
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.lazy.grid.GridItemSpan
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
import androidx.compose.material.icons.filled.CheckCircle
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
import androidx.compose.material.icons.filled.Gesture
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
import androidx.compose.material.icons.filled.PlayArrow
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
import com.teachboard.app.domain.stickers.StickerSpec
import com.teachboard.app.ui.home.WavyAccentDivider
import com.teachboard.app.ui.theme.AppConfig
import com.teachboard.app.ui.theme.TeachColors
import kotlinx.coroutines.launch
import kotlin.math.sin

// ===== Unified Soft-Pastel Modal Chrome (Dark-Mode Safe & Landscape Responsive!) =====

private val LightModalColorScheme = lightColorScheme(
  primary = TeachColors.Primary,
  onPrimary = Color.White,
  surface = Color.White,
  onSurface = TeachColors.OnSurface,
  surfaceVariant = Color(0xFFF1F5F9),
  onSurfaceVariant = TeachColors.OnSurfaceMuted,
  background = Color.White,
  onBackground = TeachColors.OnSurface,
  outline = Color(0xFFECEFF8)
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
  val widthFrac = if (isLandscape) 0.88f else 0.94f
  val maxH = if (isLandscape) (config.screenHeightDp - 14).coerceAtLeast(280).dp else 710.dp

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    MaterialTheme(colorScheme = LightModalColorScheme) {
      Surface(
        modifier = Modifier
          .fillMaxWidth(widthFrac)
          .heightIn(max = maxH),
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFECEFF8)),
        contentColor = TeachColors.OnSurface,
        shadowElevation = 8.dp
      ) {
        Column {
          // Expressive Pastel Header Bar
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(TeachColors.Background)
              .padding(horizontal = 18.dp, vertical = if (isLandscape) 8.dp else 14.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(if (isLandscape) 34.dp else 40.dp)
                .clip(RoundedCornerShape(13.dp))
                .background(badgeColor.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(if (isLandscape) 18.dp else 22.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(
                text = title,
                fontSize = if (isLandscape) 15.sp else 17.sp,
                fontWeight = FontWeight.Bold,
                color = TeachColors.OnSurface
              )
              Spacer(Modifier.height(2.dp))
              WavyAccentDivider(
                modifier = Modifier
                  .width(if (isLandscape) 40.dp else 52.dp)
                  .height(if (isLandscape) 4.dp else 6.dp),
                color = badgeColor,
                waveCount = 3
              )
            }
            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(if (isLandscape) 32.dp else 36.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFECEFF8), CircleShape)
            ) {
              Icon(
                Icons.Default.Close,
                contentDescription = stringResource(R.string.close),
                tint = TeachColors.OnSurfaceMuted,
                modifier = Modifier.size(18.dp)
              )
            }
          }
          HorizontalDivider(color = Color(0xFFF1F3F9), thickness = 1.dp)
          Box(modifier = Modifier.weight(1f, fill = false).padding(if (isLandscape) 12.dp else 16.dp)) {
            content()
          }
          if (footer != null) {
            HorizontalDivider(color = Color(0xFFF1F3F9), thickness = 1.dp)
            Box(
              modifier = Modifier
                .background(TeachColors.Background.copy(alpha = 0.6f))
                .padding(horizontal = 16.dp, vertical = if (isLandscape) 8.dp else 12.dp)
            ) {
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
        shape = RoundedCornerShape(26.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFECEFF8)),
        shadowElevation = 10.dp
      ) {
        Column {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .background(TeachColors.Background)
              .padding(horizontal = 18.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(badgeColor.copy(alpha = 0.15f)),
              contentAlignment = Alignment.Center
            ) {
              Icon(icon, contentDescription = null, tint = badgeColor, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(title, fontWeight = FontWeight.Bold, fontSize = 16.sp, color = TeachColors.OnSurface)
              Spacer(Modifier.height(2.dp))
              WavyAccentDivider(
                modifier = Modifier
                  .width(44.dp)
                  .height(5.dp),
                color = badgeColor,
                waveCount = 3
              )
            }
            IconButton(
              onClick = onDismiss,
              modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(1.dp, Color(0xFFECEFF8), CircleShape)
            ) {
              Icon(Icons.Default.Close, contentDescription = "Close", tint = TeachColors.OnSurfaceMuted, modifier = Modifier.size(16.dp))
            }
          }
          HorizontalDivider(color = Color(0xFFF1F3F9), thickness = 1.dp)
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
  Row(
    verticalAlignment = Alignment.CenterVertically,
    horizontalArrangement = Arrangement.spacedBy(6.dp),
    modifier = Modifier.padding(vertical = 2.dp)
  ) {
    Box(
      modifier = Modifier
        .size(6.dp)
        .clip(CircleShape)
        .background(TeachColors.Primary)
    )
    Text(
      text = text,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = TeachColors.OnSurface,
      letterSpacing = 0.3.sp
    )
  }
}

@Composable
private fun CheckBadge(color: Color, modifier: Modifier = Modifier) {
  Box(
    modifier = modifier
      .size(20.dp)
      .clip(CircleShape)
      .background(Color.White)
      .border(1.dp, Color(0xFFECEFF8), CircleShape),
    contentAlignment = Alignment.Center
  ) {
    Box(
      modifier = Modifier
        .size(14.dp)
        .clip(CircleShape)
        .background(color),
      contentAlignment = Alignment.Center
    ) {
      Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(10.dp))
    }
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

// ===== 1. Board Surface, Color & Texture Studio (TeachBoard Pastel Expressive) =====

@Composable
private fun ColorSwatchRow(
  colors: List<Pair<String, Int>>,
  selectedColor: Int,
  onSelect: (Int) -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .horizontalScroll(rememberScrollState()),
    horizontalArrangement = Arrangement.spacedBy(8.dp)
  ) {
    colors.forEach { (name, col) ->
      val isSel = col == selectedColor
      Surface(
        shape = RoundedCornerShape(12.dp),
        color = Color.White,
        border = BorderStroke(if (isSel) 2.dp else 1.dp, if (isSel) TeachColors.Primary else Color(0xFFECEFF8)),
        shadowElevation = if (isSel) 2.dp else 0.dp,
        modifier = Modifier
          .clip(RoundedCornerShape(12.dp))
          .clickable { onSelect(col) }
      ) {
        Column(
          modifier = Modifier.padding(horizontal = 7.dp, vertical = 6.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(3.dp)
        ) {
          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(RoundedCornerShape(9.dp))
              .background(Color(col))
              .border(1.dp, Color(0x22000000), RoundedCornerShape(9.dp)),
            contentAlignment = Alignment.Center
          ) {
            if (isSel) {
              CheckBadge(
                color = if (TeachColors.isDarkColor(col)) Color.White else TeachColors.Primary,
                modifier = Modifier.size(16.dp)
              )
            }
          }
          Text(
            name,
            fontSize = 9.5.sp,
            fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
            color = if (isSel) TeachColors.Primary else TeachColors.OnSurface,
            maxLines = 1
          )
        }
      }
    }
  }
}

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
  var sampleFormulaIdx by remember { mutableIntStateOf(0) }

  val sampleFormulas = listOf(
    "y = ax² + bx + c",
    "F = ma  ·  E = mc²",
    "2H₂ + O₂ → 2H₂O",
    "Welcome to Class! ✨"
  )

  val classroomPalette = listOf(
    "Studio White" to 0xFFFFFFFF.toInt(),
    "Warm Ivory" to 0xFFFAF6EE.toInt(),
    "Chalkboard" to 0xFF1B4332.toInt(),
    "Blackboard" to 0xFF121212.toInt(),
    "Dark Slate" to 0xFF2D3748.toInt(),
    "Midnight Navy" to 0xFF0F172A.toInt(),
    "Corkboard" to 0xFFD7CCC8.toInt()
  )

  val pastelPalette = listOf(
    "Pastel Mint" to 0xFFB6F0E4.toInt(),
    "Lavender Mist" to 0xFFE8DEFC.toInt(),
    "Sky Periwinkle" to 0xFFDCE7FE.toInt(),
    "Soft Blush" to 0xFFFDE0E8.toInt(),
    "Peach Glow" to 0xFFFCE4D6.toInt(),
    "Matcha Cream" to 0xFFD8F3DC.toInt(),
    "Lemon Chiffon" to 0xFFFEF9C3.toInt(),
    "Powder Rose" to 0xFFFFE4E6.toInt()
  )

  val darkModernPalette = listOf(
    "Galaxy Violet" to 0xFF2E1065.toInt(),
    "Deep Abyss" to 0xFF1E1B4B.toInt(),
    "Emerald Dark" to 0xFF064E3B.toInt(),
    "Espresso" to 0xFF3B1E08.toInt(),
    "Royal Wine" to 0xFF4A0E17.toInt(),
    "Cyber Charcoal" to 0xFF18181B.toInt()
  )

  val vibrantPalette = listOf(
    "Royal Azure" to 0xFF1D4ED8.toInt(),
    "Teal Ocean" to 0xFF0D9488.toInt(),
    "Amber Gold" to 0xFFD97706.toInt(),
    "Coral Sunset" to 0xFFEA580C.toInt(),
    "Purple Orchid" to 0xFF7C3AED.toInt(),
    "Forest Pine" to 0xFF047857.toInt()
  )

  val colorLabel = remember(selectedColor) {
    (classroomPalette + pastelPalette + darkModernPalette + vibrantPalette)
      .firstOrNull { it.second == selectedColor }?.first ?: String.format("#%06X", 0xFFFFFF and selectedColor)
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
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Live Interactive Board Showcase with Teacher Subject Switcher
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFECEFF8)),
        shadowElevation = 2.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(104.dp)
              .clip(RoundedCornerShape(14.dp))
              .border(1.dp, Color(0xFFECEFF8), RoundedCornerShape(14.dp))
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val rect = android.graphics.RectF(0f, 0f, size.width, size.height)
              Background.draw(drawContext.canvas.nativeCanvas, rect, selectedColor, selectedStyle)
              val fg = if (TeachColors.isDarkColor(selectedColor)) 0xFFFFFFFF.toInt() else 0xFF111827.toInt()
              FormulaParser.drawFormula(
                drawContext.canvas.nativeCanvas,
                sampleFormulas[sampleFormulaIdx],
                fg,
                30f,
                size.width * 0.58f,
                size.height * 0.85f
              )
            }
            // Active Tag Pill
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = if (TeachColors.isDarkColor(selectedColor)) Color.Black.copy(alpha = 0.55f) else Color.White.copy(alpha = 0.90f),
              border = BorderStroke(0.8.dp, if (TeachColors.isDarkColor(selectedColor)) Color.White.copy(alpha = 0.2f) else Color(0xFFECEFF8)),
              modifier = Modifier.align(Alignment.BottomEnd).padding(8.dp)
            ) {
              Text(
                text = "$colorLabel · $styleLabel",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = if (TeachColors.isDarkColor(selectedColor)) Color.White else TeachColors.OnSurface,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
              )
            }
          }

          // Sample formula switcher chips
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            listOf("Math", "Physics", "Chemistry", "Notes").forEachIndexed { idx, title ->
              val isSel = sampleFormulaIdx == idx
              Surface(
                shape = RoundedCornerShape(8.dp),
                color = if (isSel) TeachColors.PastelMint else TeachColors.Background,
                border = BorderStroke(1.dp, if (isSel) TeachColors.Secondary else Color(0xFFECEFF8)),
                modifier = Modifier.weight(1f).clickable { sampleFormulaIdx = idx }
              ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.padding(vertical = 5.dp)) {
                  Text(
                    title,
                    fontSize = 10.5.sp,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSel) TeachColors.Secondary else TeachColors.OnSurfaceMuted
                  )
                }
              }
            }
          }
        }
      }

      // 2. 1-Tap Classroom Theme Presets
      SectionLabel("Quick Classroom Presets")
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(7.dp)
      ) {
        listOf(
          Triple("Chalkboard", 0xFF1B4332.toInt(), BackgroundStyle.PLAIN),
          Triple("Math Grid", 0xFF2D3748.toInt(), BackgroundStyle.GRID),
          Triple("Ruled Note", 0xFFFAF6EE.toInt(), BackgroundStyle.LINES),
          Triple("Clean Studio", 0xFFFFFFFF.toInt(), BackgroundStyle.PLAIN)
        ).forEach { (presetName, col, st) ->
          val isSel = selectedColor == col && selectedStyle == st
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSel) TeachColors.PastelMint else Color.White,
            border = BorderStroke(if (isSel) 2.dp else 1.dp, if (isSel) TeachColors.Secondary else Color(0xFFECEFF8)),
            modifier = Modifier.weight(1f).clickable {
              selectedColor = col
              selectedStyle = st
              onApply(col, st)
            }
          ) {
            Column(
              modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Box(
                modifier = Modifier
                  .size(24.dp)
                  .clip(CircleShape)
                  .background(Color(col))
                  .border(1.dp, Color(0x33000000), CircleShape)
              )
              Spacer(Modifier.height(4.dp))
              Text(
                presetName,
                fontSize = 10.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.SemiBold,
                color = if (isSel) TeachColors.Secondary else TeachColors.OnSurface,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
              )
            }
          }
        }
      }

      // 3. Classic Boards
      SectionLabel("Classic Boards")
      ColorSwatchRow(
        colors = classroomPalette,
        selectedColor = selectedColor,
        onSelect = {
          selectedColor = it
          onApply(selectedColor, selectedStyle)
        }
      )

      // 4. Light & Pastel Tones
      SectionLabel("Light & Pastel Tones")
      ColorSwatchRow(
        colors = pastelPalette,
        selectedColor = selectedColor,
        onSelect = {
          selectedColor = it
          onApply(selectedColor, selectedStyle)
        }
      )

      // 5. Deep & Dark Tones
      SectionLabel("Deep & Dark Tones")
      ColorSwatchRow(
        colors = darkModernPalette,
        selectedColor = selectedColor,
        onSelect = {
          selectedColor = it
          onApply(selectedColor, selectedStyle)
        }
      )

      // 6. Vibrant Classroom Colors
      SectionLabel("Vibrant Classroom Colors")
      ColorSwatchRow(
        colors = vibrantPalette,
        selectedColor = selectedColor,
        onSelect = {
          selectedColor = it
          onApply(selectedColor, selectedStyle)
        }
      )

      // 7. Paper & Teaching Grid Styles
      SectionLabel("Paper & Teaching Grid Styles")
      val textureNames = listOf(
        BackgroundStyle.PLAIN to "Plain",
        BackgroundStyle.GRID to "Grid",
        BackgroundStyle.LINES to "Lines",
        BackgroundStyle.DOTS to "Dots",
        BackgroundStyle.CHALK to "Chalk",
        BackgroundStyle.ISOMETRIC to "Isometric"
      )

      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        textureNames.forEach { (style, name) ->
          val isSel = style == selectedStyle
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (isSel) TeachColors.PastelMint else Color.White,
            border = BorderStroke(if (isSel) 2.dp else 1.dp, if (isSel) TeachColors.Secondary else Color(0xFFECEFF8)),
            modifier = Modifier
              .clip(RoundedCornerShape(12.dp))
              .clickable {
                selectedStyle = style
                onApply(selectedColor, selectedStyle)
              }
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 9.dp, vertical = 7.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(36.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(Color(selectedColor))
                  .border(1.dp, Color(0xFFCBD5E1), RoundedCornerShape(8.dp))
              ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                  Background.draw(
                    drawContext.canvas.nativeCanvas,
                    android.graphics.RectF(0f, 0f, size.width, size.height),
                    selectedColor,
                    style
                  )
                }
                if (isSel) {
                  CheckBadge(
                    color = TeachColors.Secondary,
                    modifier = Modifier.size(14.dp).align(Alignment.TopEnd).padding(2.dp)
                  )
                }
              }
              Text(
                name,
                fontSize = 10.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                color = if (isSel) TeachColors.Secondary else TeachColors.OnSurface,
                maxLines = 1
              )
            }
          }
        }
      }

      // 5. Custom Color Studio
      Surface(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(14.dp))
          .border(1.dp, Color(0xFFECEFF8), RoundedCornerShape(14.dp))
          .clickable { showCustom = !showCustom },
        color = Color.White
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            RainbowWheelButton(28) { showCustom = !showCustom }
            Column {
              Text(stringResource(R.string.custom_color), fontWeight = FontWeight.Bold, fontSize = 13.5.sp, color = TeachColors.OnSurface)
              Text("Fine-tune RGB channels & Hex value", fontSize = 11.sp, color = TeachColors.OnSurfaceMuted)
            }
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = TeachColors.Background,
            border = BorderStroke(1.dp, Color(0xFFECEFF8))
          ) {
            Text(
              String.format("#%06X", 0xFFFFFF and selectedColor),
              fontSize = 11.5.sp,
              fontWeight = FontWeight.Bold,
              color = TeachColors.Primary,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
            )
          }
        }
      }
      if (showCustom) {
        CustomRgbSliders(selectedColor) { c ->
          selectedColor = c
          onApply(selectedColor, selectedStyle)
        }
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

// ===== 2. Brushes Dialog =====

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
    Triple(ToolType.Pencil, "Pencil", Icons.Default.Draw),
    Triple(ToolType.Brush, stringResource(R.string.brush_brush), Icons.Default.Brush),
    Triple(ToolType.Marker, stringResource(R.string.brush_marker), Icons.Default.Create),
    Triple(ToolType.Highlighter, stringResource(R.string.brush_highlight), Icons.Default.Highlight),
    Triple(ToolType.Calligraphy, "Callig.", Icons.Default.Gesture)
  )

  PastelModalDialog(
    title = stringResource(R.string.brushes_title),
    icon = Icons.Default.Brush,
    badgeColor = TeachColors.Primary,
    headerTint = TeachColors.HeaderSkyTint,
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Live smooth sine-wave stroke preview card
      Surface(
        shape = RoundedCornerShape(20.dp),
        color = TeachColors.Background,
        border = BorderStroke(1.dp, Color(0xFFECEFF8)),
        modifier = Modifier.fillMaxWidth().height(78.dp)
      ) {
        Box(contentAlignment = Alignment.Center) {
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
      }

      // 5 equal-width Brush cards with squircle styling
      Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        brushes.forEach { (tool, label, icon) ->
          val sel = currentTool == tool
          Surface(
            modifier = Modifier
              .weight(1f)
              .height(78.dp),
            shape = RoundedCornerShape(16.dp),
            color = if (sel) TeachColors.PastelPeriwinkle else Color.White,
            border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) TeachColors.Primary else Color(0xFFECEFF8)),
            shadowElevation = if (sel) 2.dp else 0.dp,
            onClick = { onSelectBrush(tool) }
          ) {
            Box {
              Column(
                modifier = Modifier.fillMaxSize().padding(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Icon(
                  icon,
                  contentDescription = label,
                  tint = if (sel) TeachColors.Primary else Color(0xFF475569),
                  modifier = Modifier.size(24.dp)
                )
                Spacer(Modifier.height(6.dp))
                Text(
                  label,
                  fontSize = 11.sp,
                  fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                  color = if (sel) TeachColors.Primary else TeachColors.OnSurface,
                  maxLines = 1
                )
              }
              if (sel) {
                CheckBadge(TeachColors.Primary, Modifier.align(Alignment.TopEnd).padding(4.dp))
              }
            }
          }
        }
      }

      SectionLabel(stringResource(R.string.color_label))
      ColorSwatchTray(
        colors = TeachColors.BrushSwatches,
        selectedColor = currentColor,
        accentColor = TeachColors.Primary,
        onSelect = onColorChange,
        onToggleCustom = { showCustom = !showCustom }
      )
      if (showCustom) CustomRgbSliders(currentColor, onColorChange)

      // SIZE presets + smooth slider
      SectionLabel("${stringResource(R.string.size_label)}  ·  ${currentSize.toInt()} px")
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFECEFF8)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(4f, 8f, 12f, 24f, 48f).forEach { preset ->
              val sel = kotlin.math.abs(currentSize - preset) < 1.5f
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .height(48.dp),
                shape = RoundedCornerShape(14.dp),
                color = if (sel) TeachColors.PastelPeriwinkle else TeachColors.Background,
                border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) TeachColors.Primary else Color(0xFFECEFF8)),
                onClick = { onSizeChange(preset) }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    text = "${preset.toInt()}",
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp,
                    color = if (sel) TeachColors.Primary else TeachColors.OnSurface
                  )
                  if (sel) {
                    CheckBadge(TeachColors.Primary, Modifier.align(Alignment.TopEnd).padding(3.dp))
                  }
                }
              }
            }
          }
          Slider(
            value = currentSize,
            onValueChange = onSizeChange,
            valueRange = 2f..64f,
            colors = SliderDefaults.colors(
              thumbColor = TeachColors.Primary,
              activeTrackColor = TeachColors.Primary,
              inactiveTrackColor = Color(0xFFE2E8F0)
            )
          )
        }
      }
    }
  }
}

// ===== 2b. Smart Quick 2-Card Floating Popup =====

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
          .padding(bottom = if (isLandscape) 68.dp else 86.dp, start = 24.dp, end = 24.dp),
        contentAlignment = Alignment.BottomCenter
      ) {
        Surface(
          modifier = Modifier.fillMaxWidth(if (isLandscape) 0.58f else 1f).clickable(enabled = false) {},
          shape = RoundedCornerShape(26.dp),
          color = Color.White,
          border = BorderStroke(1.dp, Color(0xFFECEFF8)),
          contentColor = TeachColors.OnSurface,
          shadowElevation = 12.dp
        ) {
          Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Left card: Writing (Aa -> Aa + language pill!)
            Surface(
              modifier = Modifier
                .weight(1f)
                .height(152.dp),
              shape = RoundedCornerShape(18.dp),
              color = if (isWritingActive) TeachColors.PastelPeriwinkle else TeachColors.Background,
              border = BorderStroke(if (isWritingActive) 2.dp else 1.dp, if (isWritingActive) TeachColors.Primary else Color(0xFFECEFF8)),
              onClick = { onSelectWriting(); onDismiss() }
            ) {
              Box(modifier = Modifier.padding(10.dp)) {
                // Top-left clickable language pill
                Surface(
                  shape = RoundedCornerShape(12.dp),
                  color = Color.White,
                  border = BorderStroke(1.dp, Color(0xFFECEFF8)),
                  modifier = Modifier.align(Alignment.TopStart),
                  onClick = { onOpenLanguagePicker() }
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
                  modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Aa", fontSize = 28.sp, color = Color(0xFF64748B), fontWeight = FontWeight.Light)
                    Text(" → ", fontSize = 18.sp, color = Color(0xFF94A3B8))
                    Text("Aa", fontSize = 26.sp, color = TeachColors.Primary, fontWeight = FontWeight.ExtraBold)
                  }
                  Spacer(Modifier.height(6.dp))
                  Text(stringResource(R.string.smart_writing_short), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TeachColors.OnSurface)
                }
              }
            }

            // Right card: Smart Shapes
            Surface(
              modifier = Modifier
                .weight(1f)
                .height(152.dp),
              shape = RoundedCornerShape(18.dp),
              color = if (isSmartShapesActive) TeachColors.PastelMint else TeachColors.Background,
              border = BorderStroke(if (isSmartShapesActive) 2.dp else 1.dp, if (isSmartShapesActive) TeachColors.Secondary else Color(0xFFECEFF8)),
              onClick = { onToggleSmartShapes(); onDismiss() }
            ) {
              Box(modifier = Modifier.padding(10.dp)) {
                if (isSmartShapesActive) {
                  CheckBadge(TeachColors.Secondary, Modifier.align(Alignment.TopEnd))
                }
                Column(
                  modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 6.dp),
                  horizontalAlignment = Alignment.CenterHorizontally
                ) {
                  Canvas(modifier = Modifier.width(120.dp).height(44.dp)) {
                    val w = size.width
                    val h = size.height
                    val rough = Path().apply {
                      moveTo(w * 0.21f, h * 0.12f)
                      lineTo(w * 0.37f, h * 0.86f)
                      lineTo(w * 0.05f, h * 0.88f)
                      close()
                    }
                    drawPath(
                      rough,
                      Color(0xFF64748B),
                      style = Stroke(width = h * 0.065f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                    val midY = h * 0.54f
                    drawLine(Color(0xFF94A3B8), Offset(w * 0.43f, midY), Offset(w * 0.57f, midY), strokeWidth = h * 0.055f, cap = StrokeCap.Round)
                    drawLine(Color(0xFF94A3B8), Offset(w * 0.52f, midY - h * 0.12f), Offset(w * 0.57f, midY), strokeWidth = h * 0.055f, cap = StrokeCap.Round)
                    drawLine(Color(0xFF94A3B8), Offset(w * 0.52f, midY + h * 0.12f), Offset(w * 0.57f, midY), strokeWidth = h * 0.055f, cap = StrokeCap.Round)
                    val clean = Path().apply {
                      moveTo(w * 0.79f, h * 0.10f)
                      lineTo(w * 0.96f, h * 0.88f)
                      lineTo(w * 0.62f, h * 0.88f)
                      close()
                    }
                    drawPath(
                      clean,
                      TeachColors.Secondary,
                      style = Stroke(width = h * 0.075f, cap = StrokeCap.Round, join = StrokeJoin.Round)
                    )
                  }
                  Spacer(Modifier.height(8.dp))
                  Text(stringResource(R.string.tool_shapes), fontWeight = FontWeight.Bold, fontSize = 14.sp, color = TeachColors.OnSurface)
                }
              }
            }
          }
        }
      }
    }
  }
}

// ===== 3. Shapes Dialog =====

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
  var selectedTab by remember { mutableIntStateOf(-1) }

  PastelModalDialog(
    title = stringResource(R.string.shapes_title),
    icon = Icons.Default.Category,
    badgeColor = TeachColors.AccentPurple,
    headerTint = TeachColors.HeaderPurpleTint,
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Category filter pills row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        val tabs = listOf("All (45)", "2D (20)", "3D (10)", "Math (7)", "Diagram (8)")
        tabs.forEachIndexed { index, tabName ->
          val targetIdx = index - 1
          val sel = selectedTab == targetIdx
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (sel) TeachColors.AccentPurple else TeachColors.Background,
            border = BorderStroke(1.dp, if (sel) TeachColors.AccentPurple else Color(0xFFECEFF8)),
            onClick = { selectedTab = targetIdx }
          ) {
            Text(
              text = tabName,
              fontSize = 12.sp,
              fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
              color = if (sel) Color.White else TeachColors.OnSurface,
              modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
          }
        }
      }

      // Fill Shape card row
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = TeachColors.PastelMint.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, Color(0xFFB6F0E4)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(Color.White),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.FormatColorFill, contentDescription = null, tint = TeachColors.Secondary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(stringResource(R.string.fill_shape), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TeachColors.OnSurface)
              Text("Solid color fill & opacity", fontSize = 12.sp, color = TeachColors.OnSurfaceMuted)
            }
            Switch(
              checked = isFilled,
              onCheckedChange = onFillToggle,
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TeachColors.Secondary
              )
            )
          }
          if (isFilled) {
            Spacer(Modifier.height(10.dp))
            ColorSwatchTray(TeachColors.BrushSwatches, fillColor ?: strokeColor, TeachColors.Secondary, { onFillColorChange(it) })
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text("Opacity ${(alpha * 100).toInt()}%", fontSize = 12.sp, fontWeight = FontWeight.SemiBold, color = TeachColors.OnSurfaceMuted, modifier = Modifier.width(76.dp))
              Slider(
                value = alpha,
                onValueChange = onAlphaChange,
                valueRange = 0.2f..1f,
                colors = SliderDefaults.colors(thumbColor = TeachColors.Secondary, activeTrackColor = TeachColors.Secondary, inactiveTrackColor = Color(0xFFDCE3EC)),
                modifier = Modifier.weight(1f)
              )
            }
          }
        }
      }

      // Filtered sections stacked in 5-column grids
      val displayedSections = if (selectedTab == -1) sections else listOf(sections[selectedTab])
      displayedSections.forEach { (secName, shapes) ->
        Row(
          modifier = Modifier.fillMaxWidth().padding(top = 8.dp, bottom = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(secName, fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TeachColors.OnSurface)
          Box(
            modifier = Modifier
              .clip(RoundedCornerShape(8.dp))
              .background(TeachColors.PastelPeriwinkle)
              .padding(horizontal = 8.dp, vertical = 2.dp)
          ) {
            Text("${shapes.size} shapes", color = TeachColors.Primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
          }
        }

        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          shapes.chunked(5).forEach { rowShapes ->
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(7.dp)) {
              rowShapes.forEach { shape ->
                val sel = shape == selectedShape
                Surface(
                  modifier = Modifier
                    .weight(1f)
                    .height(72.dp),
                  shape = RoundedCornerShape(16.dp),
                  color = if (sel) TeachColors.PastelViolet else Color.White,
                  border = BorderStroke(if (sel) 2.dp else 1.dp, if (sel) TeachColors.AccentPurple else Color(0xFFECEFF8)),
                  shadowElevation = if (sel) 2.dp else 0.dp,
                  onClick = { onSelectShape(shape); onDismiss() }
                ) {
                  Box(modifier = Modifier.fillMaxSize().padding(4.dp)) {
                    Column(
                      modifier = Modifier.fillMaxSize(),
                      horizontalAlignment = Alignment.CenterHorizontally,
                      verticalArrangement = Arrangement.Center
                    ) {
                      Canvas(modifier = Modifier.size(30.dp)) {
                        val pad = 2f
                        ShapeRenderer.drawShape(
                          canvas = drawContext.canvas.nativeCanvas,
                          type = shape,
                          left = pad,
                          top = pad,
                          right = size.width - pad,
                          bottom = size.height - pad,
                          color = if (sel) TeachColors.AccentPurple.toArgb() else 0xFF334155.toInt(),
                          strokeWidth = 2.4f,
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
                        color = if (sel) TeachColors.AccentPurple else TeachColors.OnSurfaceMuted,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                      )
                    }
                    if (sel) {
                      CheckBadge(TeachColors.AccentPurple, Modifier.align(Alignment.TopEnd))
                    }
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

// ===== 4. Eraser Studio Dialog =====

@Composable
fun EraserDialog(
  mode: EraserMode,
  size: Float,
  onModeChange: (EraserMode) -> Unit,
  onSizeChange: (Float) -> Unit,
  onDismiss: () -> Unit
) {
  val presets = listOf(
    Triple("Fine Tip", 12f, "Detail & edges"),
    Triple("Writing", 24f, "Pencil & text"),
    Triple("Medium", 48f, "Duster swipe"),
    Triple("Broad", 76f, "Full board")
  )

  PastelModalDialog(
    title = stringResource(R.string.eraser_size_title),
    icon = Icons.Default.CleaningServices,
    badgeColor = TeachColors.AccentRose,
    headerTint = TeachColors.HeaderRoseTint,
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // 1. Dual Mode Selection Cards (Object/Stroke vs. Pixel Duster)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        val pixelSel = mode == EraserMode.PIXEL_INK
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onModeChange(EraserMode.PIXEL_INK) },
          shape = RoundedCornerShape(16.dp),
          color = if (pixelSel) TeachColors.PastelBlush else TeachColors.Background,
          border = BorderStroke(if (pixelSel) 2.dp else 1.dp, if (pixelSel) TeachColors.AccentRose else Color(0xFFECEFF8))
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (pixelSel) TeachColors.AccentRose else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.CleaningServices,
                  contentDescription = null,
                  tint = if (pixelSel) Color.White else TeachColors.OnSurfaceMuted,
                  modifier = Modifier.size(16.dp)
                )
              }
              if (pixelSel) {
                CheckBadge(TeachColors.AccentRose)
              }
            }
            Spacer(Modifier.height(2.dp))
            Text(
              "Pixel Duster",
              fontWeight = FontWeight.Bold,
              fontSize = 13.5.sp,
              color = if (pixelSel) TeachColors.AccentRose else TeachColors.OnSurface
            )
            Text(
              "Cleans touch area like real duster",
              fontSize = 11.sp,
              color = TeachColors.OnSurfaceMuted,
              lineHeight = 13.sp
            )
          }
        }

        val strokeSel = mode == EraserMode.STROKE
        Surface(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(16.dp))
            .clickable { onModeChange(EraserMode.STROKE) },
          shape = RoundedCornerShape(16.dp),
          color = if (strokeSel) TeachColors.PastelBlush else TeachColors.Background,
          border = BorderStroke(if (strokeSel) 2.dp else 1.dp, if (strokeSel) TeachColors.AccentRose else Color(0xFFECEFF8))
        ) {
          Column(
            modifier = Modifier.padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(RoundedCornerShape(8.dp))
                  .background(if (strokeSel) TeachColors.AccentRose else Color(0xFFE2E8F0)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.AutoFixHigh,
                  contentDescription = null,
                  tint = if (strokeSel) Color.White else TeachColors.OnSurfaceMuted,
                  modifier = Modifier.size(16.dp)
                )
              }
              if (strokeSel) {
                CheckBadge(TeachColors.AccentRose)
              }
            }
            Spacer(Modifier.height(2.dp))
            Text(
              "Stroke Eraser",
              fontWeight = FontWeight.Bold,
              fontSize = 13.5.sp,
              color = if (strokeSel) TeachColors.AccentRose else TeachColors.OnSurface
            )
            Text(
              "Deletes whole stroke with 1 tap",
              fontSize = 11.sp,
              color = TeachColors.OnSurfaceMuted,
              lineHeight = 13.sp
            )
          }
        }
      }

      // 2. Interactive Real-Scale Eraser Duster Preview Box
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = TeachColors.Background,
        border = BorderStroke(1.dp, Color(0xFFECEFF8))
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "Live Duster Preview",
              fontSize = 12.sp,
              fontWeight = FontWeight.SemiBold,
              color = TeachColors.OnSurfaceMuted
            )
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = TeachColors.PastelBlush
            ) {
              Text(
                "${size.toInt()} px radius",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = TeachColors.AccentRose,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }

          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(96.dp)
              .clip(RoundedCornerShape(12.dp))
              .background(Color.White)
              .border(1.dp, Color(0xFFF1F5F9), RoundedCornerShape(12.dp)),
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val cy = this.size.height / 2f
              drawLine(
                color = Color(0xFFCBD5E1),
                start = Offset(24.dp.toPx(), cy),
                end = Offset(this.size.width - 24.dp.toPx(), cy),
                strokeWidth = 6.dp.toPx(),
                cap = StrokeCap.Round
              )
            }

            val clampedPreview = size.coerceIn(12f, 82f).dp
            Box(
              modifier = Modifier
                .size(clampedPreview)
                .clip(CircleShape)
                .background(TeachColors.AccentRose.copy(alpha = 0.18f))
                .border(2.dp, TeachColors.AccentRose, CircleShape),
              contentAlignment = Alignment.Center
            ) {
              Box(
                modifier = Modifier
                  .size(4.dp)
                  .clip(CircleShape)
                  .background(TeachColors.AccentRose)
              )
            }
          }
        }
      }

      // 3. Teacher Size Presets
      SectionLabel("Expressive Duster Sizes")
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        presets.forEach { (name, presetVal, _) ->
          val isSel = kotlin.math.abs(size - presetVal) < 3f
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(14.dp))
              .clickable { onSizeChange(presetVal) },
            shape = RoundedCornerShape(14.dp),
            color = if (isSel) TeachColors.PastelBlush else Color.White,
            border = BorderStroke(if (isSel) 2.dp else 1.dp, if (isSel) TeachColors.AccentRose else Color(0xFFECEFF8))
          ) {
            Column(
              modifier = Modifier.padding(vertical = 10.dp, horizontal = 4.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Box(
                modifier = Modifier
                  .size((presetVal / 5f).coerceIn(6f, 18f).dp)
                  .clip(CircleShape)
                  .background(if (isSel) TeachColors.AccentRose else Color(0xFF94A3B8))
              )
              Spacer(Modifier.height(1.dp))
              Text(
                name,
                fontSize = 11.5.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                color = if (isSel) TeachColors.AccentRose else TeachColors.OnSurface,
                maxLines = 1
              )
              Text(
                "${presetVal.toInt()} px",
                fontSize = 10.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSel) TeachColors.AccentRose else TeachColors.OnSurfaceMuted
              )
            }
          }
        }
      }

      // 4. Custom Fine-Tuning Slider with Stepper Buttons
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        SectionLabel("Fine-Tune Duster Size")
        Text(
          "${size.toInt()} px",
          fontSize = 13.sp,
          fontWeight = FontWeight.Bold,
          color = TeachColors.AccentRose
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = CircleShape,
          color = TeachColors.Background,
          border = BorderStroke(1.dp, Color(0xFFECEFF8)),
          modifier = Modifier
            .size(36.dp)
            .clickable { onSizeChange((size - 4f).coerceAtLeast(6f)) }
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TeachColors.OnSurface, modifier = Modifier.size(16.dp))
          }
        }

        Slider(
          value = size.coerceIn(6f, 90f),
          onValueChange = onSizeChange,
          valueRange = 6f..90f,
          modifier = Modifier.weight(1f),
          colors = SliderDefaults.colors(
            thumbColor = TeachColors.AccentRose,
            activeTrackColor = TeachColors.AccentRose,
            inactiveTrackColor = Color(0xFFE2E8F0)
          )
        )

        Surface(
          shape = CircleShape,
          color = TeachColors.Background,
          border = BorderStroke(1.dp, Color(0xFFECEFF8)),
          modifier = Modifier
            .size(36.dp)
            .clickable { onSizeChange((size + 4f).coerceAtMost(90f)) }
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = TeachColors.OnSurface, modifier = Modifier.size(16.dp))
          }
        }
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

// ===== 12. Wikimedia Image Search (Responsive Landscape & Portrait) =====

@Composable
fun WikimediaSearchDialog(
  onPickImage: (WikimediaImage) -> Unit,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current
  val config = LocalConfiguration.current
  val isLandscape = config.screenWidthDp > config.screenHeightDp
  val dialogWidth = if (isLandscape) 0.88f else 0.94f
  val dialogMaxHeight = if (isLandscape) (config.screenHeightDp - 16).coerceAtLeast(300).dp else 660.dp

  val scope = rememberCoroutineScope()
  var query by remember { mutableStateOf("") }
  var results by remember { mutableStateOf<List<WikimediaImage>>(emptyList()) }
  var loading by remember { mutableStateOf(false) }
  var errorMsg by remember { mutableStateOf<String?>(null) }
  var offset by remember { mutableIntStateOf(0) }
  var selectedCategory by remember { mutableIntStateOf(0) }

  val educationalTopics = listOf(
    "All Topics" to listOf("Solar System", "Human Heart", "Plant Cell", "World Map", "Periodic Table", "Pythagoras"),
    "🔬 Science" to listOf("Human Heart", "Plant Cell", "DNA Double Helix", "Human Brain", "Photosynthesis", "Microscope"),
    "🪐 Astronomy" to listOf("Solar System", "Moon Phases", "Milky Way Galaxy", "Mars Planet", "Saturn Rings", "Black Hole"),
    "📐 Mathematics" to listOf("Pythagoras Theorem", "Right Triangle", "Parabola Graph", "Geometric Shapes", "Circle Angles"),
    "🗺️ Geography" to listOf("World Map", "Map of India", "Earth Layers", "Compass Rose", "Volcano Diagram", "Water Cycle"),
    "🧪 Physics & Chem" to listOf("Periodic Table", "Electric Circuit", "Water Molecule", "Atom Structure", "Light Spectrum")
  )

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

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(dialogWidth)
        .heightIn(max = dialogMaxHeight),
      shape = RoundedCornerShape(24.dp),
      color = Color.White,
      border = BorderStroke(1.dp, Color(0xFFECEFF8)),
      shadowElevation = 14.dp
    ) {
      Column(
        modifier = Modifier.padding(
          horizontal = if (isLandscape) 14.dp else 18.dp,
          vertical = if (isLandscape) 10.dp else 16.dp
        ),
        verticalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 12.dp)
      ) {
        // Modern Header
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(if (isLandscape) 28.dp else 34.dp)
              .clip(RoundedCornerShape(9.dp))
              .background(TeachColors.PastelPeriwinkle),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.PhotoLibrary, contentDescription = null, tint = TeachColors.Primary, modifier = Modifier.size(if (isLandscape) 16.dp else 19.dp))
          }
          Spacer(Modifier.width(8.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.image_search_title), fontWeight = FontWeight.Bold, fontSize = if (isLandscape) 15.sp else 17.sp, color = TeachColors.OnSurface)
            WavyAccentDivider(color = TeachColors.Primary, modifier = Modifier.width(44.dp).padding(top = 1.dp))
          }
          Surface(
            shape = RoundedCornerShape(8.dp),
            color = TeachColors.PastelPeriwinkle,
            modifier = Modifier.padding(end = 6.dp)
          ) {
            Text(
              "Free CC Visuals",
              color = TeachColors.Primary,
              fontWeight = FontWeight.Bold,
              fontSize = 10.sp,
              modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.5.dp)
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(28.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TeachColors.OnSurfaceMuted, modifier = Modifier.size(17.dp))
          }
        }

        // Modern Capsule Search Bar
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = TeachColors.Background,
          border = BorderStroke(1.dp, Color(0xFFECEFF8)),
          modifier = Modifier.fillMaxWidth()
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = if (isLandscape) 2.dp else 4.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Search, contentDescription = null, tint = TeachColors.Primary, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(8.dp))
            BasicTextField(
              value = query,
              onValueChange = { query = it },
              modifier = Modifier.weight(1f),
              singleLine = true,
              textStyle = TextStyle(fontSize = 13.5.sp, color = TeachColors.OnSurface, fontWeight = FontWeight.Medium),
              decorationBox = { innerTextField ->
                if (query.isEmpty()) {
                  Text("Search diagrams, anatomy, maps, formulas...", fontSize = 12.5.sp, color = TeachColors.OnSurfaceMuted)
                }
                innerTextField()
              }
            )
            if (query.isNotEmpty()) {
              IconButton(onClick = { query = ""; results = emptyList() }, modifier = Modifier.size(26.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Clear", tint = TeachColors.OnSurfaceMuted, modifier = Modifier.size(15.dp))
              }
            }
            Surface(
              shape = RoundedCornerShape(10.dp),
              color = TeachColors.Primary,
              modifier = Modifier
                .clickable { runSearch(query, reset = true) }
                .padding(start = 4.dp)
            ) {
              Text(
                "Search",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                modifier = Modifier.padding(horizontal = 11.dp, vertical = 6.dp)
              )
            }
          }
        }

        errorMsg?.let {
          Text(it, color = TeachColors.Danger, fontSize = 11.5.sp, modifier = Modifier.padding(horizontal = 4.dp))
        }

        if (loading && results.isEmpty()) {
          Box(modifier = Modifier.fillMaxWidth().weight(1f), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(8.dp)) {
              CircularProgressIndicator(color = TeachColors.Primary, strokeWidth = 2.5.dp)
              Text("Searching educational archives...", fontSize = 12.sp, color = TeachColors.OnSurfaceMuted)
            }
          }
        } else if (results.isEmpty()) {
          // Categorized Educational Discovery State
          Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
            // Subject Category Pills
            Row(
              modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              educationalTopics.forEachIndexed { idx, (catName, _) ->
                val isSel = idx == selectedCategory
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = if (isSel) TeachColors.Primary else TeachColors.Background,
                  border = BorderStroke(1.dp, if (isSel) TeachColors.Primary else Color(0xFFECEFF8)),
                  modifier = Modifier.clickable { selectedCategory = idx }
                ) {
                  Text(
                    catName,
                    color = if (isSel) Color.White else TeachColors.OnSurface,
                    fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 11.5.sp,
                    modifier = Modifier.padding(horizontal = 9.dp, vertical = 5.dp)
                  )
                }
              }
            }

            Text(
              "Quick Visual Topics  ·  Tap to search",
              fontSize = 11.sp,
              fontWeight = FontWeight.SemiBold,
              color = TeachColors.OnSurfaceMuted
            )

            // Topic Chips Grid
            val currentTopics = educationalTopics[selectedCategory].second
            val chunkCols = if (isLandscape) 3 else 2
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(6.dp),
              modifier = Modifier.weight(1f)
            ) {
              items(currentTopics.chunked(chunkCols)) { rowTopics ->
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  rowTopics.forEach { topic ->
                    Surface(
                      modifier = Modifier
                        .weight(1f)
                        .height(if (isLandscape) 36.dp else 42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { runSearch(topic, reset = true) },
                      shape = RoundedCornerShape(12.dp),
                      color = TeachColors.Background,
                      border = BorderStroke(1.dp, Color(0xFFECEFF8))
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                      ) {
                        Box(
                          modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(TeachColors.Primary)
                        )
                        Text(
                          topic,
                          fontSize = 11.5.sp,
                          fontWeight = FontWeight.SemiBold,
                          color = TeachColors.OnSurface,
                          maxLines = 1,
                          overflow = TextOverflow.Ellipsis
                        )
                      }
                    }
                  }
                  repeat(chunkCols - rowTopics.size) { Spacer(Modifier.weight(1f)) }
                }
              }
            }
          }
        } else {
          // Results view
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              "${results.size} ${stringResource(R.string.results_tap_add)}",
              fontSize = 11.5.sp,
              fontWeight = FontWeight.SemiBold,
              color = TeachColors.OnSurfaceMuted
            )
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = TeachColors.PastelPeriwinkle,
              modifier = Modifier.clickable { results = emptyList() }
            ) {
              Text(
                "Clear",
                fontSize = 10.5.sp,
                fontWeight = FontWeight.Bold,
                color = TeachColors.Primary,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }

          val gridCols = if (isLandscape) 4 else 2
          val thumbHeight = if (isLandscape) 82.dp else 110.dp

          LazyVerticalGrid(
            columns = GridCells.Fixed(gridCols),
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            items(results) { img ->
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = TeachColors.Background,
                border = BorderStroke(1.dp, Color(0xFFECEFF8)),
                modifier = Modifier
                  .clip(RoundedCornerShape(12.dp))
                  .clickable { onPickImage(img); onDismiss() }
              ) {
                Column {
                  Box(
                    modifier = Modifier
                      .fillMaxWidth()
                      .height(thumbHeight)
                      .background(Color(0xFFF1F5F9)),
                    contentAlignment = Alignment.Center
                  ) {
                    AsyncImage(
                      model = ImageRequest.Builder(LocalContext.current)
                        .data(img.thumbUrl)
                        .setHeader("User-Agent", AppConfig.USER_AGENT)
                        .crossfade(true)
                        .build(),
                      contentDescription = img.title,
                      contentScale = ContentScale.Fit,
                      modifier = Modifier
                        .fillMaxSize()
                        .padding(4.dp)
                    )
                    Surface(
                      shape = RoundedCornerShape(6.dp),
                      color = TeachColors.Primary,
                      modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(4.dp)
                    ) {
                      Row(
                        modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(2.dp)
                      ) {
                        Icon(Icons.Default.Add, contentDescription = "Add", tint = Color.White, modifier = Modifier.size(10.dp))
                        Text("Add", fontSize = 9.sp, fontWeight = FontWeight.Bold, color = Color.White)
                      }
                    }
                  }
                  Text(
                    text = img.title.substringBefore(".").replace("_", " "),
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TeachColors.OnSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                  )
                }
              }
            }

            if (!loading) {
              item(span = { GridItemSpan(gridCols) }) {
                Surface(
                  shape = RoundedCornerShape(10.dp),
                  color = TeachColors.PastelPeriwinkle,
                  border = BorderStroke(1.dp, TeachColors.Primary.copy(alpha = 0.2f)),
                  modifier = Modifier
                    .fillMaxWidth()
                    .clickable { runSearch(query, reset = false) }
                ) {
                  Box(modifier = Modifier.padding(vertical = 8.dp), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.load_more), color = TeachColors.Primary, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                  }
                }
              }
            }
          }
        }

        // Footer note
        Text(
          "Educational media from Wikimedia Commons (Creative Commons License)",
          fontSize = 10.sp,
          color = TeachColors.OnSurfaceMuted,
          textAlign = TextAlign.Center,
          modifier = Modifier.fillMaxWidth()
        )
      }
    }
  }
}

// ===== 13. Stickers & Science Stickers (Matching ref_18_stickers.png & ref_19_science.png!) =====

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun StickerPickerDialog(
  group: StickerGroup,
  initialColor: Int,
  onPick: (String, Int) -> Unit,
  onDismiss: () -> Unit
) {
  val config = LocalConfiguration.current
  val isLandscape = config.screenWidthDp > config.screenHeightDp
  val all = remember(group) { StickerCatalog.byGroup(group) }
  val isGeneral = group == StickerGroup.GENERAL
  val rawCategories = remember(all) { all.map { it.category }.distinct() }
  val categories = remember(rawCategories, isGeneral) {
    if (!isGeneral) listOf("All") + rawCategories else rawCategories
  }
  var selectedCat by remember(group) { mutableStateOf(if (group == StickerGroup.SCIENCE) "All" else categories.first()) }
  var search by remember { mutableStateOf("") }

  val filtered = remember(all, selectedCat, search) {
    all.filter { spec ->
      if (search.isNotBlank()) {
        spec.label.contains(search, true) || spec.category.contains(search, true)
      } else {
        selectedCat == "All" || spec.category == selectedCat
      }
    }
  }

  val accent = if (isGeneral) TeachColors.AccentPink else TeachColors.AccentEmerald
  val headerTint = if (isGeneral) TeachColors.HeaderPinkTint else TeachColors.HeaderMintTint
  val columnsCount = if (isLandscape) 6 else 3

  PastelModalDialog(
    title = if (isGeneral) stringResource(R.string.stickers_title) else stringResource(R.string.tool_science),
    icon = if (isGeneral) Icons.Default.EmojiEmotions else Icons.Default.Science,
    badgeColor = accent,
    headerTint = headerTint,
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(if (isLandscape) 6.dp else 10.dp)
    ) {
      // 1. Search Bar
      if (!isGeneral) {
        OutlinedTextField(
          value = search,
          onValueChange = { search = it },
          modifier = Modifier
            .fillMaxWidth()
            .height(if (isLandscape) 44.dp else 48.dp),
          singleLine = true,
          shape = RoundedCornerShape(12.dp),
          placeholder = { Text(stringResource(R.string.search_stickers_hint), fontSize = 12.sp) },
          leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
          trailingIcon = {
            if (search.isNotBlank()) {
              IconButton(onClick = { search = "" }, modifier = Modifier.size(24.dp)) {
                Icon(Icons.Default.Close, contentDescription = "Clear", modifier = Modifier.size(16.dp))
              }
            }
          }
        )
      }

      // 2. Bold Category Chips wrapped with FlowRow (all categories visible on front screen without side swiping!)
      FlowRow(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp),
        verticalArrangement = Arrangement.spacedBy(if (isLandscape) 4.dp else 6.dp)
      ) {
        categories.forEach { cat ->
          val sel = cat == selectedCat && search.isBlank()
          Surface(
            shape = RoundedCornerShape(12.dp),
            color = if (sel) accent else Color.White,
            modifier = Modifier
              .border(1.dp, if (sel) accent else TeachColors.TrayBorder, RoundedCornerShape(12.dp))
              .clickable { selectedCat = cat; search = "" }
          ) {
            val catPrefix = when (cat) {
              "All" -> "✨ "
              "Lab" -> "🧪 "
              "Biology" -> "🧬 "
              "Chemistry" -> "⚗️ "
              "Physics" -> "⚡ "
              "Atoms" -> "⚛️ "
              "Elements" -> "🧱 "
              else -> ""
            }
            Text(
              "$catPrefix$cat",
              fontSize = if (isLandscape) 11.sp else 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (sel) Color.White else Color(0xFF1E293B),
              modifier = Modifier.padding(horizontal = if (isLandscape) 8.dp else 10.dp, vertical = if (isLandscape) 4.dp else 6.dp)
            )
          }
        }
      }

      // 3. Status readout
      val catLabel = if (search.isNotBlank()) "Search results" else selectedCat
      Text(
        "$catLabel  ·  ${filtered.size} items",
        fontSize = 11.sp,
        fontWeight = FontWeight.Medium,
        color = Color(0xFF64748B),
        modifier = Modifier.align(Alignment.CenterHorizontally)
      )

      // 4. Dynamic Responsive Grid (Section Headers when "All" is active, Elements placed down below!)
      val gridMaxH = if (isLandscape) (config.screenHeightDp - 145).coerceAtLeast(180).dp else 420.dp
      LazyVerticalGrid(
        columns = GridCells.Fixed(columnsCount),
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = gridMaxH),
        horizontalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 10.dp),
        verticalArrangement = Arrangement.spacedBy(if (isLandscape) 8.dp else 10.dp)
      ) {
        if (!isGeneral && selectedCat == "All" && search.isBlank()) {
          rawCategories.forEach { cat ->
            val catItems = all.filter { it.category == cat }
            if (catItems.isNotEmpty()) {
              item(span = { GridItemSpan(maxLineSpan) }, key = "header_$cat") {
                Row(
                  modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 10.dp, bottom = 4.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                  val catIcon = when (cat) {
                    "Lab" -> "🧪"
                    "Biology" -> "🧬"
                    "Chemistry" -> "⚗️"
                    "Physics" -> "⚡"
                    "Atoms" -> "⚛️"
                    "Elements" -> "🧱"
                    else -> "🔬"
                  }
                  Text(
                    "$catIcon $cat",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF1E293B)
                  )
                  Text(
                    "(${catItems.size})",
                    fontSize = 11.sp,
                    color = Color(0xFF64748B)
                  )
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .height(1.dp)
                      .background(Color(0xFFE2E8F0))
                  )
                }
              }
              items(catItems, key = { it.id }) { spec ->
                StickerCardTile(spec, initialColor, isGeneral) {
                  onPick(spec.id, initialColor)
                  onDismiss()
                }
              }
            }
          }
        } else {
          items(filtered, key = { it.id }) { spec ->
            StickerCardTile(spec, initialColor, isGeneral) {
              onPick(spec.id, initialColor)
              onDismiss()
            }
          }
        }
      }
    }
  }
}

@Composable
private fun StickerCardTile(
  spec: StickerSpec,
  initialColor: Int,
  isGeneral: Boolean,
  onClick: () -> Unit
) {
  Surface(
    modifier = Modifier
      .aspectRatio(1f)
      .clip(RoundedCornerShape(14.dp))
      .border(1.dp, TeachColors.TrayBorder, RoundedCornerShape(14.dp))
      .clickable(onClick = onClick),
    color = Color.White
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .padding(5.dp)
        .clip(RoundedCornerShape(10.dp))
        .background(if (!isGeneral) Color(0xFFEDF2F7) else Color.White),
      contentAlignment = Alignment.Center
    ) {
      Canvas(modifier = Modifier.fillMaxSize().padding(6.dp)) {
        val previewCol = if (initialColor == TeachColors.BrushWhite) TeachColors.BrushBlack else initialColor
        StickerCatalog.drawSticker(drawContext.canvas.nativeCanvas, spec.id, previewCol, size.width, size.height)
      }
    }
  }
}

// ===== 14. Laser Pointer Studio Card =====

@Composable
fun LaserDialog(
  currentColor: Int,
  currentSize: Float,
  onColorChange: (Int) -> Unit,
  onSizeChange: (Float) -> Unit,
  onDismiss: () -> Unit
) {
  val laserSwatches = listOf(
    Triple("Crimson", 0xFFEF4444.toInt(), Color(0xFFEF4444)),
    Triple("Solar", 0xFFFACC15.toInt(), Color(0xFFFACC15)),
    Triple("Emerald", 0xFF22C55E.toInt(), Color(0xFF22C55E)),
    Triple("Cyan", 0xFF06B6D4.toInt(), Color(0xFF06B6D4)),
    Triple("Cobalt", 0xFF3B82F6.toInt(), Color(0xFF3B82F6)),
    Triple("Violet", 0xFFEC4899.toInt(), Color(0xFFEC4899)),
    Triple("Starlight", 0xFFFFFFFF.toInt(), Color(0xFFFFFFFF))
  )

  val sizePresets = listOf(
    Pair("Pinpoint", 6f),
    Pair("Lecture", 14f),
    Pair("Marker", 22f),
    Pair("Spotlight", 32f)
  )

  BottomDockCard(
    title = stringResource(R.string.laser_pointer_title),
    icon = Icons.Default.FlashlightOn,
    badgeColor = TeachColors.AccentOrange,
    headerTint = TeachColors.HeaderPeachTint,
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // 1. Live Neon Laser Beam Showcase
      val activeLaserColor = Color(currentColor)
      Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF0F172A),
        border = BorderStroke(1.dp, Color(0xFF1E293B))
      ) {
        Column(
          modifier = Modifier.padding(12.dp),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(8.dp)
                  .clip(CircleShape)
                  .background(activeLaserColor)
              )
              Text(
                "Live Neon Beam",
                color = Color(0xFF94A3B8),
                fontSize = 11.5.sp,
                fontWeight = FontWeight.Medium
              )
            }
            Surface(
              shape = RoundedCornerShape(8.dp),
              color = activeLaserColor.copy(alpha = 0.2f),
              border = BorderStroke(1.dp, activeLaserColor.copy(alpha = 0.5f))
            ) {
              Text(
                "${currentSize.toInt()} px beam",
                color = if (currentColor == 0xFFFFFFFF.toInt()) Color.White else activeLaserColor,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }

          // Beam Canvas with glowing multi-layer curve
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .height(58.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFF020617)),
            contentAlignment = Alignment.Center
          ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
              val w = size.width
              val h = size.height

              val path = Path().apply {
                moveTo(w * 0.1f, h * 0.7f)
                cubicTo(w * 0.35f, h * 0.2f, w * 0.65f, h * 0.85f, w * 0.88f, h * 0.35f)
              }

              // Layer 1: Wide diffused neon aura
              drawPath(
                path = path,
                color = activeLaserColor.copy(alpha = 0.25f),
                style = Stroke(width = (currentSize * 2.8f).coerceIn(12f, 70f), cap = StrokeCap.Round, join = StrokeJoin.Round)
              )
              // Layer 2: Medium glowing beam
              drawPath(
                path = path,
                color = activeLaserColor.copy(alpha = 0.65f),
                style = Stroke(width = (currentSize * 1.5f).coerceIn(6f, 40f), cap = StrokeCap.Round, join = StrokeJoin.Round)
              )
              // Layer 3: Intense core beam
              drawPath(
                path = path,
                color = if (currentColor == 0xFFFFFFFF.toInt()) Color.White else Color.White.copy(alpha = 0.85f),
                style = Stroke(width = (currentSize * 0.6f).coerceIn(2f, 18f), cap = StrokeCap.Round, join = StrokeJoin.Round)
              )

              // Head dot with glow rings
              val endPoint = Offset(w * 0.88f, h * 0.35f)
              drawCircle(
                color = activeLaserColor.copy(alpha = 0.4f),
                radius = (currentSize * 1.8f).coerceIn(8f, 32f),
                center = endPoint
              )
              drawCircle(
                color = Color.White,
                radius = (currentSize * 0.6f).coerceIn(3f, 12f),
                center = endPoint
              )
            }
          }
        }
      }

      // 2. Named Neon Swatches with Aura Rings
      SectionLabel("Neon Beam Colors")
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        laserSwatches.forEach { (name, colVal, col) ->
          val isSel = colVal == currentColor
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(14.dp))
              .clickable { onColorChange(colVal) },
            shape = RoundedCornerShape(14.dp),
            color = if (isSel) col.copy(alpha = 0.12f) else Color.White,
            border = BorderStroke(if (isSel) 2.dp else 1.dp, if (isSel) col else Color(0xFFECEFF8))
          ) {
            Column(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(30.dp)
                  .clip(CircleShape)
                  .background(col)
                  .then(
                    if (isSel) Modifier.border(2.5.dp, if (colVal == 0xFFFFFFFF.toInt()) Color(0xFF0F172A) else Color.White, CircleShape)
                    else Modifier.border(1.dp, Color(0x33000000), CircleShape)
                  ),
                contentAlignment = Alignment.Center
              ) {
                if (isSel) {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = if (colVal == 0xFFFFFFFF.toInt() || colVal == 0xFFFACC15.toInt()) Color.Black else Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
              Text(
                name,
                fontSize = 11.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                color = if (isSel) TeachColors.OnSurface else TeachColors.OnSurfaceMuted
              )
            }
          }
        }
      }

      // 3. Beam Thickness Presets
      SectionLabel("Beam Thickness Presets")
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        sizePresets.forEach { (label, presetSize) ->
          val isSel = kotlin.math.abs(currentSize - presetSize) < 2f
          Surface(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(12.dp))
              .clickable { onSizeChange(presetSize) },
            shape = RoundedCornerShape(12.dp),
            color = if (isSel) TeachColors.PastelPeach else Color.White,
            border = BorderStroke(if (isSel) 2.dp else 1.dp, if (isSel) TeachColors.AccentOrange else Color(0xFFECEFF8))
          ) {
            Column(
              modifier = Modifier.padding(vertical = 8.dp, horizontal = 2.dp),
              horizontalAlignment = Alignment.CenterHorizontally,
              verticalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Box(
                modifier = Modifier
                  .size((presetSize / 2f).coerceIn(6f, 16f).dp)
                  .clip(CircleShape)
                  .background(if (isSel) TeachColors.AccentOrange else Color(0xFF94A3B8))
              )
              Text(
                label,
                fontSize = 11.sp,
                fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                color = if (isSel) TeachColors.AccentOrange else TeachColors.OnSurface,
                maxLines = 1
              )
              Text(
                "${presetSize.toInt()} px",
                fontSize = 9.5.sp,
                color = if (isSel) TeachColors.AccentOrange else TeachColors.OnSurfaceMuted
              )
            }
          }
        }
      }

      // 4. Custom Fine-Tune Slider with Steppers
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        SectionLabel("Fine-Tune Beam Width")
        Text(
          "${currentSize.toInt()} px",
          fontSize = 12.5.sp,
          fontWeight = FontWeight.Bold,
          color = TeachColors.AccentOrange
        )
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Surface(
          shape = CircleShape,
          color = TeachColors.Background,
          border = BorderStroke(1.dp, Color(0xFFECEFF8)),
          modifier = Modifier
            .size(34.dp)
            .clickable { onSizeChange((currentSize - 2f).coerceAtLeast(4f)) }
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Remove, contentDescription = "Decrease", tint = TeachColors.OnSurface, modifier = Modifier.size(15.dp))
          }
        }

        Slider(
          value = currentSize.coerceIn(4f, 36f),
          onValueChange = onSizeChange,
          valueRange = 4f..36f,
          modifier = Modifier.weight(1f),
          colors = SliderDefaults.colors(
            thumbColor = TeachColors.AccentOrange,
            activeTrackColor = TeachColors.AccentOrange,
            inactiveTrackColor = Color(0xFFE2E8F0)
          )
        )

        Surface(
          shape = CircleShape,
          color = TeachColors.Background,
          border = BorderStroke(1.dp, Color(0xFFECEFF8)),
          modifier = Modifier
            .size(34.dp)
            .clickable { onSizeChange((currentSize + 2f).coerceAtMost(36f)) }
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.Add, contentDescription = "Increase", tint = TeachColors.OnSurface, modifier = Modifier.size(15.dp))
          }
        }
      }
    }
  }
}

// ===== 15. Screen Recorder Dialog =====

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
    badgeColor = TeachColors.Primary,
    headerTint = TeachColors.HeaderSkyTint,
    onDismiss = onDismiss
  ) {
    Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
      // Card 1: Record voice
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = TeachColors.PastelPeriwinkle.copy(alpha = 0.4f),
        border = BorderStroke(1.dp, Color(0xFFC7D7FE)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(38.dp)
              .clip(CircleShape)
              .background(Color.White),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Mic, contentDescription = null, tint = TeachColors.Primary, modifier = Modifier.size(20.dp))
          }
          Spacer(Modifier.width(12.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.recorder_voice), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TeachColors.OnSurface)
            Text("Capture your microphone and audio", fontSize = 12.sp, color = TeachColors.OnSurfaceMuted)
          }
          Switch(
            checked = audio,
            onCheckedChange = { audio = it },
            colors = SwitchDefaults.colors(
              checkedThumbColor = Color.White,
              checkedTrackColor = TeachColors.Primary,
              uncheckedTrackColor = Color(0xFFCBD5E1)
            )
          )
        }
      }

      // Card 2: Face cam
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = TeachColors.PastelMint.copy(alpha = 0.35f),
        border = BorderStroke(1.dp, Color(0xFFB6F0E4)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp)) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .size(38.dp)
                .clip(CircleShape)
                .background(Color.White),
              contentAlignment = Alignment.Center
            ) {
              Icon(Icons.Default.Videocam, contentDescription = null, tint = TeachColors.Secondary, modifier = Modifier.size(20.dp))
            }
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
              Text(stringResource(R.string.recorder_facecam), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TeachColors.OnSurface)
              Text("Overlay webcam bubble while teaching", fontSize = 12.sp, color = TeachColors.OnSurfaceMuted)
            }
            Switch(
              checked = faceCam,
              onCheckedChange = { faceCam = it },
              colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = TeachColors.Secondary,
                uncheckedTrackColor = Color(0xFFCBD5E1)
              )
            )
          }
          if (faceCam) {
            Spacer(Modifier.height(10.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
              val isRound = camShape == FaceCamShape.ROUND
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isRound) TeachColors.Secondary else Color.White,
                border = BorderStroke(1.dp, if (isRound) TeachColors.Secondary else Color(0xFFECEFF8)),
                onClick = { camShape = FaceCamShape.ROUND }
              ) {
                Text(
                  stringResource(R.string.round),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isRound) Color.White else TeachColors.OnSurface,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
              val isSquare = camShape == FaceCamShape.SQUARE
              Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (isSquare) TeachColors.Secondary else Color.White,
                border = BorderStroke(1.dp, if (isSquare) TeachColors.Secondary else Color(0xFFECEFF8)),
                onClick = { camShape = FaceCamShape.SQUARE }
              ) {
                Text(
                  stringResource(R.string.square),
                  fontSize = 12.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = if (isSquare) Color.White else TeachColors.OnSurface,
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
              }
            }
          }
        }
      }

      // Card 3: Video quality
      Surface(
        shape = RoundedCornerShape(18.dp),
        color = Color.White,
        border = BorderStroke(1.dp, Color(0xFFECEFF8)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(stringResource(R.string.video_quality), fontWeight = FontWeight.Bold, fontSize = 15.sp, color = TeachColors.OnSurface)
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(TeachColors.PastelPeriwinkle)
                .padding(horizontal = 8.dp, vertical = 2.dp)
            ) {
              Text(quality.label, color = TeachColors.Primary, fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
            }
          }
          Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            RecordingQuality.entries.forEach { q ->
              val sel = quality == q
              Surface(
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp),
                shape = RoundedCornerShape(12.dp),
                color = if (sel) TeachColors.Primary else TeachColors.Background,
                border = BorderStroke(1.dp, if (sel) TeachColors.Primary else Color(0xFFECEFF8)),
                onClick = { quality = q }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Text(
                    if (q == RecordingQuality.P1080) "1080p FHD" else if (q == RecordingQuality.P720) "720p HD" else q.label,
                    fontWeight = if (sel) FontWeight.Bold else FontWeight.Medium,
                    fontSize = 12.sp,
                    color = if (sel) Color.White else TeachColors.OnSurface
                  )
                }
              }
            }
          }
          Text(
            "${quality.label} · ${quality.fps} fps · about ${quality.mbPerMinEstimate} · Crystal Clear",
            fontSize = 12.sp,
            color = TeachColors.OnSurfaceMuted,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
          )
        }
      }

      // Start Recording Button
      Button(
        onClick = { onStartRecording(quality, audio, faceCam, camShape); onDismiss() },
        modifier = Modifier.fillMaxWidth().height(54.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = TeachColors.Primary)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Box(
            modifier = Modifier
              .size(10.dp)
              .clip(CircleShape)
              .background(Color(0xFFEF4444))
          )
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.start_recording), fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text("Unlimited · 100% Free · No Watermark", fontSize = 10.sp, color = Color.White.copy(alpha = 0.88f))
          }
        }
      }
    }
  }
}

@Composable
fun RecordingSavedDialog(
  uri: android.net.Uri,
  sizeBytes: Long,
  onPlay: () -> Unit,
  onShare: () -> Unit,
  onDelete: () -> Unit,
  onDismiss: () -> Unit
) {
  var showDeleteConfirm by remember { mutableStateOf(false) }

  val formattedSize = remember(sizeBytes) {
    when {
      sizeBytes <= 0L -> "Saved"
      sizeBytes < 1024L * 1024L -> String.format(java.util.Locale.US, "%.1f KB", sizeBytes / 1024.0)
      sizeBytes < 1024L * 1024L * 1024L -> String.format(java.util.Locale.US, "%.1f MB", sizeBytes / (1024.0 * 1024.0))
      else -> String.format(java.util.Locale.US, "%.2f GB", sizeBytes / (1024.0 * 1024.0 * 1024.0))
    }
  }

  PastelModalDialog(
    title = stringResource(R.string.recording_saved_title),
    icon = Icons.Default.CheckCircle,
    badgeColor = Color(0xFF16A34A),
    headerTint = Color(0xFFDCFCE7),
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFF8FAFC),
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
      ) {
        Column(
          modifier = Modifier.padding(16.dp),
          horizontalAlignment = Alignment.CenterHorizontally,
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          Icon(Icons.Default.Videocam, contentDescription = null, tint = Color(0xFFE11D48), modifier = Modifier.size(36.dp))
          Text(formattedSize, fontWeight = FontWeight.Bold, fontSize = 20.sp, color = Color(0xFF0F172A))
          Text("Saved in Movies/${AppConfig.ALBUM_NAME}", fontSize = 13.sp, color = Color(0xFF64748B))
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Button(
          onClick = onPlay,
          modifier = Modifier
            .weight(1f)
            .height(48.dp),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFE11D48))
        ) {
          Icon(Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text("Play", fontWeight = FontWeight.Bold)
        }

        Button(
          onClick = onShare,
          modifier = Modifier
            .weight(1f)
            .height(48.dp),
          shape = RoundedCornerShape(14.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB))
        ) {
          Icon(Icons.Default.Share, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text("Share", fontWeight = FontWeight.Bold)
        }
      }

      OutlinedButton(
        onClick = { showDeleteConfirm = true },
        modifier = Modifier
          .fillMaxWidth()
          .height(44.dp),
        shape = RoundedCornerShape(14.dp),
        border = BorderStroke(1.dp, Color(0xFFFCA5A5)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFDC2626))
      ) {
        Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color(0xFFDC2626))
        Spacer(Modifier.width(6.dp))
        Text("Delete", fontWeight = FontWeight.SemiBold, color = Color(0xFFDC2626))
      }
    }
  }

  if (showDeleteConfirm) {
    androidx.compose.material3.AlertDialog(
      onDismissRequest = { showDeleteConfirm = false },
      title = { Text("Delete this video?") },
      text = { Text("It will be removed from your gallery. This cannot be undone.") },
      confirmButton = {
        TextButton(
          onClick = {
            showDeleteConfirm = false
            onDelete()
            onDismiss()
          }
        ) {
          Text("Delete", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
        }
      },
      dismissButton = {
        TextButton(onClick = { showDeleteConfirm = false }) {
          Text("Cancel")
        }
      }
    )
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

// ===== 17. Add Shortcuts Dialog (Pastel Playful Expressive) =====

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
      modifier = Modifier.fillMaxWidth(0.92f).heightIn(max = 580.dp),
      shape = RoundedCornerShape(26.dp),
      color = Color.White,
      border = BorderStroke(1.dp, Color(0xFFECEFF8)),
      shadowElevation = 14.dp
    ) {
      Column(modifier = Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(TeachColors.PastelPeriwinkle),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Add, contentDescription = null, tint = TeachColors.Primary, modifier = Modifier.size(18.dp))
          }
          Spacer(Modifier.width(10.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(stringResource(R.string.shortcuts_title), color = TeachColors.OnSurface, fontWeight = FontWeight.Bold, fontSize = 17.sp)
            WavyAccentDivider(color = TeachColors.Primary, modifier = Modifier.width(54.dp).padding(top = 2.dp))
          }
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = TeachColors.PastelPeriwinkle,
            modifier = Modifier.padding(end = 8.dp)
          ) {
            Text(
              "${chosen.size}/${AppConfig.MAX_SHORTCUTS}",
              color = TeachColors.Primary,
              fontWeight = FontWeight.Bold,
              fontSize = 11.5.sp,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
          IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(Icons.Default.Close, contentDescription = "Close", tint = TeachColors.OnSurfaceMuted, modifier = Modifier.size(18.dp))
          }
        }

        Text("Select tools for quick access (max ${AppConfig.MAX_SHORTCUTS})", color = TeachColors.OnSurfaceMuted, fontSize = 12.sp)

        LazyColumn(modifier = Modifier.weight(1f, fill = false), verticalArrangement = Arrangement.spacedBy(7.dp)) {
          items(candidates) { action ->
            val checked = action in chosen
            Surface(
              shape = RoundedCornerShape(14.dp),
              color = if (checked) TeachColors.PastelPeriwinkle else TeachColors.Background,
              border = BorderStroke(if (checked) 1.5.dp else 1.dp, if (checked) TeachColors.Primary else Color(0xFFECEFF8)),
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  if (checked) chosen.remove(action)
                  else if (chosen.size < AppConfig.MAX_SHORTCUTS) chosen.add(action)
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  toolbarIconFor(action),
                  contentDescription = null,
                  tint = if (checked) TeachColors.Primary else TeachColors.OnSurfaceMuted,
                  modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(12.dp))
                Text(
                  action.name.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() },
                  color = if (checked) TeachColors.Primary else TeachColors.OnSurface,
                  fontWeight = if (checked) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 13.5.sp,
                  modifier = Modifier.weight(1f)
                )
                Checkbox(
                  checked = checked,
                  onCheckedChange = {
                    if (it && chosen.size < AppConfig.MAX_SHORTCUTS) chosen.add(action)
                    else if (!it) chosen.remove(action)
                  },
                  colors = CheckboxDefaults.colors(checkedColor = TeachColors.Primary, uncheckedColor = TeachColors.OutlineVariant)
                )
              }
            }
          }
        }

        Surface(
          modifier = Modifier
            .fillMaxWidth()
            .height(44.dp)
            .clip(RoundedCornerShape(14.dp))
            .clickable { onSave(chosen.toList()); onDismiss() },
          shape = RoundedCornerShape(14.dp),
          color = TeachColors.Primary,
          shadowElevation = 2.dp
        ) {
          Box(contentAlignment = Alignment.Center) {
            Text(stringResource(R.string.done), color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
          }
        }
      }
    }
  }
}

// ===== 18. Modern Pastel Playful Expressive Customize Toolbar Dialog =====

private val EssentialToolbarActions = setOf(
  ToolbarAction.SAVE,
  ToolbarAction.SHARE,
  ToolbarAction.UNDO,
  ToolbarAction.REDO,
  ToolbarAction.CLEAR,
  ToolbarAction.PEN,
  ToolbarAction.PENCIL,
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
      color = TeachColors.Background
    ) {
      Column(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // 1. Top Bar: Title & Accent + Reset Pill + Close Pill
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(
              text = "Customize Toolbar",
              color = TeachColors.OnSurface,
              fontWeight = FontWeight.ExtraBold,
              fontSize = 24.sp,
              letterSpacing = (-0.5).sp
            )

            Canvas(
              modifier = Modifier
                .width(100.dp)
                .height(8.dp)
            ) {
              val midY = size.height / 2f
              val waveCount = 4
              val waveLength = size.width / waveCount
              val amp = 2.5f * density
              val strokeWidth = 3f * density

              val path = Path().apply {
                moveTo(0f, midY)
                for (i in 0 until waveCount) {
                  val startX = i * waveLength
                  val cp1X = startX + waveLength * 0.25f
                  val cp1Y = midY - amp
                  val cp2X = startX + waveLength * 0.75f
                  val cp2Y = midY + amp
                  val endX = startX + waveLength
                  val endY = midY
                  cubicTo(cp1X, cp1Y, cp2X, cp2Y, endX, endY)
                }
              }
              drawPath(path = path, color = TeachColors.Primary, style = Stroke(width = strokeWidth, cap = StrokeCap.Round))
            }
          }

          Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            // Reset Pill Button
            Surface(
              shape = RoundedCornerShape(9999.dp),
              color = TeachColors.PastelPeach,
              shadowElevation = 1.dp,
              modifier = Modifier
                .border(1.dp, Color(0xFFF5C9B3), RoundedCornerShape(9999.dp))
                .clip(RoundedCornerShape(9999.dp))
                .clickable {
                  customizable.clear()
                  customizable.addAll(ToolbarAction.entries.filter { it != ToolbarAction.CUSTOMIZE && it != ToolbarAction.HELP })
                  hidden = emptySet()
                  onReset()
                }
            ) {
              Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 7.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(5.dp)
              ) {
                Icon(
                  Icons.Default.Refresh,
                  contentDescription = null,
                  tint = TeachColors.PastelPeachGlyph,
                  modifier = Modifier.size(15.dp)
                )
                Text(
                  text = "Reset",
                  color = TeachColors.PastelPeachGlyph,
                  fontWeight = FontWeight.Bold,
                  fontSize = 12.sp
                )
              }
            }

            // Close Circular Pill
            Surface(
              shape = CircleShape,
              color = Color.White,
              shadowElevation = 2.dp,
              modifier = Modifier
                .size(40.dp)
                .border(1.dp, Color(0xFFEBEBF5), CircleShape)
                .clip(CircleShape)
                .clickable { persist(); onDismiss() }
            ) {
              Box(contentAlignment = Alignment.Center) {
                Icon(
                  Icons.Default.Close,
                  contentDescription = "Close",
                  tint = TeachColors.OnSurface,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }
        }

        // 2. Subtitle & Stats Pill Row
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          Text(
            text = "Reorder tools with arrows · Toggle optional tools",
            color = TeachColors.OnSurfaceMuted,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium
          )

          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = TeachColors.PastelPeriwinkle
          ) {
            Text(
              text = "${customizable.size - hidden.size} active",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TeachColors.PastelPeriwinkleGlyph,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
            )
          }
        }

        // 3. Reorderable Tool List
        LazyColumn(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          items(customizable.size, key = { customizable[it].name }) { index ->
            val action = customizable[index]
            val isEssential = action in EssentialToolbarActions
            val isEnabled = isEssential || action !in hidden
            val canMoveUp = index > 0
            val canMoveDown = index < customizable.lastIndex

            val toolTitle = when (action) {
              ToolbarAction.SAVE -> "Save Board"
              ToolbarAction.SHARE -> "Share / Export"
              ToolbarAction.UNDO -> "Undo"
              ToolbarAction.REDO -> "Redo"
              ToolbarAction.CLEAR -> "Clear Canvas"
              ToolbarAction.PEN -> "Pen"
              ToolbarAction.PENCIL -> "Pencil (Pressure)"
              ToolbarAction.SMART -> "Smart Shapes"
              ToolbarAction.SHAPES -> "Geometric Shapes"
              ToolbarAction.ERASER -> "Eraser"
              ToolbarAction.BOARD -> "Board Color"
              ToolbarAction.TEXT -> "Text Tool"
              ToolbarAction.STICKERS -> "Stickers"
              ToolbarAction.OCR -> "Text Recognition (OCR)"
              ToolbarAction.FORMULA -> "Math Formula"
              ToolbarAction.TABLE -> "Grid / Table"
              ToolbarAction.LAYERS -> "Canvas Layers"
              ToolbarAction.IMAGE -> "Import Image"
              ToolbarAction.VIDEO -> "Video Player"
              ToolbarAction.WEB_IMAGE -> "Web Image Search"
              ToolbarAction.CAMERA -> "Camera to Board"
              ToolbarAction.LASER -> "Laser Pointer"
              ToolbarAction.SCIENCE -> "Science & Labs"
              ToolbarAction.IMPORT_PDF -> "Import PDF"
              ToolbarAction.GEOMETRY -> "Ruler & Protractor"
              ToolbarAction.CALCULATOR -> "Calculator"
              ToolbarAction.TIMER -> "Timer & Stopwatch"
              ToolbarAction.RECORD -> "Screen Recording"
              ToolbarAction.LASSO -> "Lasso Selection"
              ToolbarAction.HAND -> "Hand / Pan Canvas"
              else -> action.name.lowercase().replace('_', ' ').replaceFirstChar { it.uppercase() }
            }

            Surface(
              shape = RoundedCornerShape(18.dp),
              color = Color.White,
              shadowElevation = if (isEnabled) 1.5.dp else 0.5.dp,
              modifier = Modifier
                .fillMaxWidth()
                .border(
                  width = 1.dp,
                  color = if (isEnabled) Color(0xFFECEFF8) else Color(0xFFF1F3F9),
                  shape = RoundedCornerShape(18.dp)
                )
            ) {
              Row(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(horizontal = 10.dp, vertical = 9.dp),
                verticalAlignment = Alignment.CenterVertically
              ) {
                // Drag handle icon
                Icon(
                  Icons.Default.DragIndicator,
                  contentDescription = null,
                  tint = TeachColors.OutlineVariant,
                  modifier = Modifier.size(18.dp)
                )

                Spacer(Modifier.width(4.dp))

                // Up arrow pill
                Surface(
                  shape = CircleShape,
                  color = if (canMoveUp) TeachColors.PastelPeriwinkle else Color(0xFFF3F4F9),
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(enabled = canMoveUp) {
                      if (canMoveUp) {
                        val item = customizable.removeAt(index)
                        customizable.add(index - 1, item)
                        persist()
                      }
                    }
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      Icons.Default.ArrowUpward,
                      contentDescription = "Move Up",
                      tint = if (canMoveUp) TeachColors.Primary else Color(0xFFCBD5E1),
                      modifier = Modifier.size(15.dp)
                    )
                  }
                }

                Spacer(Modifier.width(4.dp))

                // Down arrow pill
                Surface(
                  shape = CircleShape,
                  color = if (canMoveDown) TeachColors.PastelPeriwinkle else Color(0xFFF3F4F9),
                  modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .clickable(enabled = canMoveDown) {
                      if (canMoveDown) {
                        val item = customizable.removeAt(index)
                        customizable.add(index + 1, item)
                        persist()
                      }
                    }
                ) {
                  Box(contentAlignment = Alignment.Center) {
                    Icon(
                      Icons.Default.ArrowDownward,
                      contentDescription = "Move Down",
                      tint = if (canMoveDown) TeachColors.Primary else Color(0xFFCBD5E1),
                      modifier = Modifier.size(15.dp)
                    )
                  }
                }

                Spacer(Modifier.width(8.dp))

                // Tool Icon Badge (Squircle)
                Box(
                  modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(11.dp))
                    .background(
                      when {
                        isEssential -> TeachColors.PastelPeriwinkle
                        isEnabled -> TeachColors.PastelMint
                        else -> Color(0xFFF1F3F9)
                      }
                    ),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    toolbarIconFor(action),
                    contentDescription = null,
                    tint = when {
                      isEssential -> TeachColors.Primary
                      isEnabled -> TeachColors.PastelMintGlyph
                      else -> TeachColors.OnSurfaceMuted
                    },
                    modifier = Modifier.size(19.dp)
                  )
                }

                Spacer(Modifier.width(10.dp))

                // Tool Title & Subtitle
                Column(modifier = Modifier.weight(1f)) {
                  Text(
                    text = toolTitle,
                    color = if (isEnabled) TeachColors.OnSurface else TeachColors.OnSurfaceMuted,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.5.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                  )
                  Text(
                    text = if (isEssential) "Always active" else if (isEnabled) "Visible on toolbar" else "Hidden from toolbar",
                    color = TeachColors.OnSurfaceMuted,
                    fontSize = 11.sp
                  )
                }

                Spacer(Modifier.width(8.dp))

                // "Essential" Chip
                if (isEssential) {
                  Surface(
                    shape = RoundedCornerShape(9999.dp),
                    color = TeachColors.PastelMint
                  ) {
                    Text(
                      text = "Essential",
                      color = TeachColors.PastelMintGlyph,
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                    )
                  }
                  Spacer(Modifier.width(8.dp))
                }

                // Switch Toggle
                Switch(
                  checked = isEnabled,
                  enabled = !isEssential,
                  onCheckedChange = { on ->
                    hidden = if (on) hidden - action else hidden + action
                    persist()
                  },
                  colors = SwitchDefaults.colors(
                    checkedThumbColor = Color.White,
                    checkedTrackColor = TeachColors.Primary,
                    uncheckedThumbColor = Color.White,
                    uncheckedTrackColor = Color(0xFFD8DADF),
                    disabledCheckedThumbColor = Color.White,
                    disabledCheckedTrackColor = Color(0xFFB4EEE2)
                  )
                )
              }
            }
          }
        }

        // 4. Helpful Footer Card
        Surface(
          shape = RoundedCornerShape(16.dp),
          color = Color.White,
          shadowElevation = 1.dp,
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFECEFF8), RoundedCornerShape(16.dp))
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
          ) {
            Box(
              modifier = Modifier
                .size(28.dp)
                .clip(CircleShape)
                .background(TeachColors.PastelMint),
              contentAlignment = Alignment.Center
            ) {
              Icon(
                Icons.Default.Check,
                contentDescription = null,
                tint = TeachColors.PastelMintGlyph,
                modifier = Modifier.size(16.dp)
              )
            }
            Text(
              text = "Essential tools (Pen, Eraser, Undo, Redo, Save, Clear) are always locked to keep drawing instant and dependable.",
              fontSize = 11.5.sp,
              color = TeachColors.OnSurfaceMuted,
              lineHeight = 15.sp,
              modifier = Modifier.weight(1f)
            )
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
