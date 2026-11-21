package com.teachboard.app.ui.canvas

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.CropSquare
import androidx.compose.material.icons.filled.Dashboard
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.Science
import androidx.compose.material.icons.filled.SquareFoot
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.ViewInAr
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush as GBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.teachboard.app.R
import com.teachboard.app.domain.model.BackgroundStyle
import com.teachboard.app.domain.model.EraserMode
import com.teachboard.app.domain.model.RecordingQuality
import com.teachboard.app.domain.model.Serialization
import com.teachboard.app.domain.model.ToolType
import com.teachboard.app.domain.stickers.StickerCatalog
import com.teachboard.app.domain.stickers.StickerGroup
import com.teachboard.app.domain.stickers.StickerSpec
import com.teachboard.app.ui.theme.TeachColors

// =========================================================================
// 0. Neutral Design Tokens & Shared Palette Constants
// =========================================================================

object LandscapeTokens {
  val Surface = Color.White
  val Hairline = Color(0xFFE2E8F0)
  val Ink = Color(0xFF0F172A)
  val Muted = Color(0xFF64748B)

  // Standard flat 7 swatches + rainbow disc for tools
  val StandardPalette = listOf(
    0xFF000000.toInt(), // Black
    0xFFDC2626.toInt(), // Red
    0xFF2563EB.toInt(), // Blue
    0xFF16A34A.toInt(), // Green
    0xFFEA580C.toInt(), // Orange
    0xFF9333EA.toInt(), // Purple
    0xFF0891B2.toInt()  // Cyan
  )

  // Formula 9-cell palette (8 flat + 1 neutral/white + rainbow)
  val FormulaPalette = listOf(
    0xFF000000.toInt(),
    0xFFDC2626.toInt(),
    0xFF2563EB.toInt(),
    0xFF16A34A.toInt(),
    0xFFEA580C.toInt(),
    0xFF9333EA.toInt(),
    0xFF0891B2.toInt(),
    0xFF4F46E5.toInt(),
    0xFFFFFFFF.toInt() // Neutral disc
  )
}

// =========================================================================
// 1. Shared UI Primitives
// =========================================================================

@Composable
fun LandscapePanelFrame(
  widthFraction: Float = 0.62f,
  maxHeightFraction: Float = 0.85f,
  hardMaxWidthDp: Dp? = null,
  accent: Color = TeachColors.Primary,
  icon: ImageVector? = null,
  title: String? = null,
  onDismiss: () -> Unit,
  headerTrailingContent: (@Composable () -> Unit)? = null,
  content: @Composable () -> Unit
) {
  val config = LocalConfiguration.current
  val boardWidth = config.screenWidthDp.dp
  val boardHeight = config.screenHeightDp.dp

  val targetWidth = if (hardMaxWidthDp != null) {
    (boardWidth * widthFraction).coerceAtMost(hardMaxWidthDp)
  } else {
    boardWidth * widthFraction
  }
  val targetMaxHeight = boardHeight * maxHeightFraction

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clickable(onClick = onDismiss)
        .padding(bottom = 68.dp, top = 12.dp, start = 12.dp, end = 12.dp),
      contentAlignment = Alignment.Center
    ) {
      Surface(
        modifier = Modifier
          .width(targetWidth)
          .heightIn(max = targetMaxHeight)
          .clickable(enabled = false) {},
        shape = RoundedCornerShape(20.dp),
        color = LandscapeTokens.Surface,
        border = BorderStroke(1.dp, LandscapeTokens.Hairline),
        shadowElevation = 18.dp
      ) {
        Column(modifier = Modifier.fillMaxWidth()) {
          // 1.3 Header Band (48 dp)
          if (title != null) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
                .background(accent.copy(alpha = 0.08f))
                .padding(horizontal = 14.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              if (icon != null) {
                Box(
                  modifier = Modifier
                    .size(28.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(accent),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                }
                Spacer(Modifier.width(10.dp))
              }

              Text(
                text = title,
                fontSize = 15.sp,
                fontWeight = FontWeight.SemiBold,
                color = LandscapeTokens.Ink,
                modifier = Modifier.weight(1f)
              )

              if (headerTrailingContent != null) {
                headerTrailingContent()
                Spacer(Modifier.width(8.dp))
              }

              IconButton(
                onClick = onDismiss,
                modifier = Modifier.size(36.dp)
              ) {
                Icon(
                  imageVector = Icons.Default.Close,
                  contentDescription = "Close",
                  tint = LandscapeTokens.Muted,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
            HorizontalDivider(thickness = 1.dp, color = LandscapeTokens.Hairline)
          }

          // Body Content (12 dp padding)
          Box(
            modifier = Modifier
              .fillMaxWidth()
              .padding(12.dp)
          ) {
            content()
          }
        }
      }
    }
  }
}

@Composable
fun LandscapeSectionLabel(
  text: String,
  valueSuffix: String? = null,
  modifier: Modifier = Modifier
) {
  val displayText = if (valueSuffix != null) "$text · $valueSuffix" else text
  Text(
    text = displayText.uppercase(),
    fontSize = 10.5.sp,
    fontWeight = FontWeight.SemiBold,
    letterSpacing = 0.8.sp,
    color = LandscapeTokens.Muted,
    modifier = modifier.padding(bottom = 6.dp, top = 4.dp)
  )
}

@Composable
fun LandscapeSizeChip(
  label: String,
  selected: Boolean,
  accent: Color = TeachColors.Primary,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .height(44.dp)
      .widthIn(min = 44.dp)
      .clip(RoundedCornerShape(11.dp))
      .background(if (selected) accent.copy(alpha = 0.08f) else Color.White)
      .border(
        BorderStroke(
          if (selected) 1.5.dp else 1.dp,
          if (selected) accent else LandscapeTokens.Hairline
        ),
        RoundedCornerShape(11.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 10.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 14.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      color = if (selected) accent else LandscapeTokens.Ink
    )

    if (selected) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .offset(x = 4.dp, y = (-4).dp)
          .size(14.dp)
          .clip(CircleShape)
          .background(accent),
        contentAlignment = Alignment.Center
      ) {
        Icon(
          imageVector = Icons.Default.Check,
          contentDescription = null,
          tint = Color.White,
          modifier = Modifier.size(10.dp)
        )
      }
    }
  }
}

@Composable
fun LandscapeSwatchRow(
  palette: List<Int> = LandscapeTokens.StandardPalette,
  selectedColor: Int,
  accent: Color = TeachColors.Primary,
  showRainbow: Boolean = true,
  onSelectColor: (Int) -> Unit,
  onCustomClick: () -> Unit = {}
) {
  Row(
    horizontalArrangement = Arrangement.spacedBy(8.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    palette.forEach { c ->
      val isSelected = selectedColor == c
      val isWhite = c == 0xFFFFFFFF.toInt()
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(Color(c))
          .border(
            width = if (isSelected) 2.dp else (if (isWhite) 1.dp else 0.dp),
            color = if (isSelected) accent else (if (isWhite) LandscapeTokens.Hairline else Color.Transparent),
            shape = CircleShape
          )
          .clickable { onSelectColor(c) },
        contentAlignment = Alignment.Center
      ) {
        if (isSelected) {
          Box(
            modifier = Modifier
              .size(8.dp)
              .clip(CircleShape)
              .background(if (isWhite) accent else Color.White)
          )
        }
      }
    }

    if (showRainbow) {
      // Rainbow custom color circle
      Box(
        modifier = Modifier
          .size(28.dp)
          .clip(CircleShape)
          .background(
            GBrush.sweepGradient(
              listOf(Color.Red, Color.Yellow, Color.Green, Color.Cyan, Color.Blue, Color.Magenta, Color.Red)
            )
          )
          .border(1.dp, LandscapeTokens.Hairline, CircleShape)
          .clickable(onClick = onCustomClick),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .size(12.dp)
            .clip(CircleShape)
            .background(Color.White)
        )
      }
    }
  }
}

@Composable
fun LandscapeSlider(
  value: Float,
  valueRange: ClosedFloatingPointRange<Float>,
  accent: Color = TeachColors.Primary,
  onValueChange: (Float) -> Unit
) {
  Slider(
    value = value,
    onValueChange = onValueChange,
    valueRange = valueRange,
    colors = SliderDefaults.colors(
      thumbColor = accent,
      activeTrackColor = accent,
      inactiveTrackColor = LandscapeTokens.Hairline
    ),
    modifier = Modifier.fillMaxWidth()
  )
}

@Composable
fun LandscapeCategoryPill(
  label: String,
  selected: Boolean,
  accent: Color = TeachColors.Primary,
  onClick: () -> Unit
) {
  Box(
    modifier = Modifier
      .height(36.dp)
      .clip(RoundedCornerShape(18.dp))
      .background(if (selected) accent else Color.White)
      .border(
        BorderStroke(1.dp, if (selected) accent else LandscapeTokens.Hairline),
        RoundedCornerShape(18.dp)
      )
      .clickable(onClick = onClick)
      .padding(horizontal = 14.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 13.sp,
      fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Medium,
      color = if (selected) Color.White else LandscapeTokens.Ink
    )
  }
}

@Composable
fun LandscapePagerStrip(
  currentPage: Int,
  totalPages: Int
) {
  Text(
    text = "$currentPage / $totalPages  ·  swipe for more >",
    fontSize = 11.sp,
    color = LandscapeTokens.Muted,
    textAlign = TextAlign.Center,
    modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
  )
}

// =========================================================================
// 2. Tool 1: Brushes (Wide Card, Two Columns)
// =========================================================================

@Composable
fun LandscapeBrushesPanel(
  currentTool: ToolType,
  currentColor: Int,
  currentSize: Float,
  onSelectBrush: (ToolType) -> Unit,
  onColorChange: (Int) -> Unit,
  onSizeChange: (Float) -> Unit,
  onDismiss: () -> Unit
) {
  val accent = TeachColors.Primary
  val brushList = listOf(
    ToolType.Pen to "Pen",
    ToolType.Brush to "Brush",
    ToolType.Marker to "Marker",
    ToolType.Highlighter to "Highlight",
    ToolType.Calligraphy to "Callig."
  )
  val sizePresets = listOf(4f, 8f, 12f, 24f, 48f)

  LandscapePanelFrame(
    widthFraction = 0.62f,
    maxHeightFraction = 0.72f,
    accent = accent,
    icon = Icons.Default.Edit,
    title = stringResource(R.string.brushes_title),
    onDismiss = onDismiss
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Left Column (45%)
      Column(
        modifier = Modifier.weight(0.45f),
        verticalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Preview Card (64 dp)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(64.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(14.dp))
            .padding(8.dp),
          contentAlignment = Alignment.Center
        ) {
          Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val path = Path().apply {
              moveTo(w * 0.15f, h * 0.5f)
              cubicTo(w * 0.35f, h * 0.15f, w * 0.65f, h * 0.85f, w * 0.85f, h * 0.5f)
            }
            drawPath(
              path = path,
              color = Color(currentColor),
              style = Stroke(
                width = currentSize.coerceIn(2f, 32f),
                cap = androidx.compose.ui.graphics.StrokeCap.Round
              )
            )
          }
        }

        // 5 Brush Tiles in row
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          brushList.forEach { (tool, label) ->
            val isSelected = currentTool == tool
            Box(
              modifier = Modifier
                .weight(1f)
                .height(56.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(if (isSelected) accent.copy(alpha = 0.08f) else Color.White)
                .border(
                  BorderStroke(
                    if (isSelected) 1.5.dp else 1.dp,
                    if (isSelected) accent else LandscapeTokens.Hairline
                  ),
                  RoundedCornerShape(11.dp)
                )
                .clickable { onSelectBrush(tool) },
              contentAlignment = Alignment.Center
            ) {
              Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
              ) {
                Icon(
                  imageVector = when (tool) {
                    ToolType.Pen -> Icons.Default.Edit
                    ToolType.Brush -> Icons.Default.Brush
                    ToolType.Marker -> Icons.Default.Edit
                    ToolType.Highlighter -> Icons.Default.Brush
                    else -> Icons.Default.Edit
                  },
                  contentDescription = label,
                  tint = if (isSelected) accent else LandscapeTokens.Ink,
                  modifier = Modifier.size(22.dp)
                )
                Spacer(Modifier.height(2.dp))
                Text(
                  text = label,
                  fontSize = 10.5.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) accent else LandscapeTokens.Ink,
                  maxLines = 1
                )
              }

              if (isSelected) {
                Box(
                  modifier = Modifier
                    .align(Alignment.TopEnd)
                    .offset(x = 3.dp, y = (-3).dp)
                    .size(13.dp)
                    .clip(CircleShape)
                    .background(accent),
                  contentAlignment = Alignment.Center
                ) {
                  Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(9.dp))
                }
              }
            }
          }
        }
      }

      // Right Column (55%)
      Column(
        modifier = Modifier.weight(0.55f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // COLOR Section
        LandscapeSectionLabel(stringResource(R.string.label_color))
        LandscapeSwatchRow(
          selectedColor = currentColor,
          accent = accent,
          onSelectColor = onColorChange
        )

        // SIZE Section
        LandscapeSectionLabel(stringResource(R.string.label_size), valueSuffix = currentSize.toInt().toString())
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          sizePresets.forEach { p ->
            LandscapeSizeChip(
              label = p.toInt().toString(),
              selected = currentSize == p,
              accent = accent,
              onClick = { onSizeChange(p) }
            )
          }
        }

        // 1-100 Slider
        LandscapeSlider(
          value = currentSize,
          valueRange = 1f..100f,
          accent = accent,
          onValueChange = onSizeChange
        )
      }
    }
  }
}

// =========================================================================
// 3. Tool 2: Smart (Popover, 2 Cards Side-by-Side, No Header)
// =========================================================================

@Composable
fun LandscapeSmartPopover(
  selectedLanguageTag: String,
  isWritingActive: Boolean,
  isSmartShapesActive: Boolean,
  onSelectWriting: () -> Unit,
  onToggleSmartShapes: () -> Unit,
  onOpenLanguagePicker: () -> Unit,
  onDismiss: () -> Unit
) {
  val accent = Color(0xFF2563EB)
  val config = LocalConfiguration.current
  val boardWidth = config.screenWidthDp.dp
  val popoverWidth = (boardWidth * 0.32f).coerceAtLeast(300.dp)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clickable(onClick = onDismiss)
        .padding(bottom = 68.dp),
      contentAlignment = Alignment.BottomCenter
    ) {
      Surface(
        modifier = Modifier
          .width(popoverWidth)
          .height(115.dp)
          .clickable(enabled = false) {},
        shape = RoundedCornerShape(20.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LandscapeTokens.Hairline),
        shadowElevation = 16.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(10.dp),
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Card 1: Writing Card
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .clip(RoundedCornerShape(16.dp))
              .background(if (isWritingActive) accent.copy(alpha = 0.06f) else Color(0xFFF8FAFC))
              .border(
                BorderStroke(
                  if (isWritingActive) 1.5.dp else 1.dp,
                  if (isWritingActive) accent else LandscapeTokens.Hairline
                ),
                RoundedCornerShape(16.dp)
              )
              .clickable(onClick = onSelectWriting)
              .padding(8.dp)
          ) {
            Column(
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.SpaceBetween,
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              // Language pill
              Row(
                modifier = Modifier
                  .height(24.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.White)
                  .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(12.dp))
                  .clickable(onClick = onOpenLanguagePicker)
                  .padding(horizontal = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                Icon(Icons.Default.Language, contentDescription = null, tint = accent, modifier = Modifier.size(12.dp))
                Text(
                  text = if (selectedLanguageTag.isBlank()) "AUTO" else selectedLanguageTag.uppercase().take(4),
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = LandscapeTokens.Ink
                )
                Text("▾", fontSize = 10.sp, color = LandscapeTokens.Muted)
              }

              // Aa -> Aa
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Aa", fontSize = 22.sp, fontWeight = FontWeight.Normal, color = LandscapeTokens.Muted)
                Text(" → ", fontSize = 14.sp, color = LandscapeTokens.Muted)
                Text("Aa", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = accent)
              }

              Text(
                text = stringResource(R.string.smart_writing_short),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isWritingActive) accent else LandscapeTokens.Ink
              )
            }

            if (isWritingActive) {
              Box(
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(accent),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
              }
            }
          }

          // Card 2: Shapes Card
          Box(
            modifier = Modifier
              .weight(1f)
              .fillMaxHeight()
              .clip(RoundedCornerShape(16.dp))
              .background(if (isSmartShapesActive) accent.copy(alpha = 0.06f) else Color(0xFFF8FAFC))
              .border(
                BorderStroke(
                  if (isSmartShapesActive) 1.5.dp else 1.dp,
                  if (isSmartShapesActive) accent else LandscapeTokens.Hairline
                ),
                RoundedCornerShape(16.dp)
              )
              .clickable(onClick = onToggleSmartShapes)
              .padding(8.dp)
          ) {
            Column(
              modifier = Modifier.fillMaxSize(),
              verticalArrangement = Arrangement.SpaceBetween,
              horizontalAlignment = Alignment.CenterHorizontally
            ) {
              Spacer(Modifier.height(4.dp))
              // Triangle outline -> Triangle solid
              Row(verticalAlignment = Alignment.CenterVertically) {
                Text("△", fontSize = 24.sp, color = LandscapeTokens.Muted)
                Text(" → ", fontSize = 14.sp, color = LandscapeTokens.Muted)
                Text("▲", fontSize = 24.sp, color = accent)
              }

              Text(
                text = stringResource(R.string.tool_shapes),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isSmartShapesActive) accent else LandscapeTokens.Ink
              )
              Spacer(Modifier.height(2.dp))
            }

            if (isSmartShapesActive) {
              Box(
                modifier = Modifier
                  .align(Alignment.TopEnd)
                  .size(16.dp)
                  .clip(CircleShape)
                  .background(accent),
                contentAlignment = Alignment.Center
              ) {
                Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(11.dp))
              }
            }
          }
        }
      }
    }
  }
}

// =========================================================================
// 4. Tool 3: Shapes (Narrow Card, 5 Columns, Scrollable)
// =========================================================================

@Composable
fun LandscapeShapesPanel(
  selectedShape: ToolType,
  isFilled: Boolean,
  onSelectShape: (ToolType) -> Unit,
  onFillToggle: (Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  val accent = Color(0xFF7C3AED)
  val shapes2D = ToolType.shapesSheet2D
  val shapes3D = ToolType.shapesSheet3D

  LandscapePanelFrame(
    widthFraction = 0.36f,
    maxHeightFraction = 0.85f,
    accent = accent,
    icon = Icons.Default.Category,
    title = stringResource(R.string.shapes_title),
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .verticalScroll(rememberScrollState()),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Toggle Row: Fill Shape
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(Color(0xFFF8FAFC))
          .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(14.dp))
          .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.FormatColorFill, contentDescription = null, tint = accent, modifier = Modifier.size(20.dp))
        Spacer(Modifier.width(10.dp))
        Text(
          text = stringResource(R.string.fill_shape),
          fontSize = 14.sp,
          fontWeight = FontWeight.SemiBold,
          color = LandscapeTokens.Ink,
          modifier = Modifier.weight(1f)
        )
        Switch(
          checked = isFilled,
          onCheckedChange = onFillToggle,
          colors = SwitchDefaults.colors(
            checkedThumbColor = Color.White,
            checkedTrackColor = accent
          )
        )
      }

      // 2D SHAPES (5 Columns)
      LandscapeSectionLabel(stringResource(R.string.shapes_2d))
      val rows2D = shapes2D.chunked(5)
      rows2D.forEach { rowShapes ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          rowShapes.forEach { shape ->
            ShapeTileItem(
              shape = shape,
              isSelected = selectedShape == shape,
              accent = accent,
              modifier = Modifier.weight(1f),
              onClick = { onSelectShape(shape) }
            )
          }
          // Pad empty columns if last row has less than 5
          for (i in 0 until (5 - rowShapes.size)) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }

      // 3D SHAPES (5 Columns)
      LandscapeSectionLabel(stringResource(R.string.shapes_3d))
      val rows3D = shapes3D.chunked(5)
      rows3D.forEach { rowShapes ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          rowShapes.forEach { shape ->
            ShapeTileItem(
              shape = shape,
              isSelected = selectedShape == shape,
              accent = accent,
              modifier = Modifier.weight(1f),
              onClick = { onSelectShape(shape) }
            )
          }
          for (i in 0 until (5 - rowShapes.size)) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}

@Composable
private fun ShapeTileItem(
  shape: ToolType,
  isSelected: Boolean,
  accent: Color,
  modifier: Modifier = Modifier,
  onClick: () -> Unit
) {
  Box(
    modifier = modifier
      .height(58.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(if (isSelected) accent.copy(alpha = 0.08f) else Color.White)
      .border(
        BorderStroke(
          if (isSelected) 1.5.dp else 1.dp,
          if (isSelected) accent else LandscapeTokens.Hairline
        ),
        RoundedCornerShape(12.dp)
      )
      .clickable(onClick = onClick)
      .padding(4.dp),
    contentAlignment = Alignment.Center
  ) {
    Column(
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.Center
    ) {
      Icon(
        imageVector = when (shape) {
          ToolType.Rect, ToolType.Square -> Icons.Default.CropSquare
          ToolType.Circle, ToolType.Oval -> Icons.Default.RadioButtonChecked
          ToolType.Cube, ToolType.Cuboid -> Icons.Default.ViewInAr
          else -> Icons.Default.Category
        },
        contentDescription = shape.name,
        tint = if (isSelected) accent else LandscapeTokens.Ink,
        modifier = Modifier.size(20.dp)
      )
      Spacer(Modifier.height(2.dp))
      Text(
        text = shape.name.take(5),
        fontSize = 10.sp,
        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
        color = if (isSelected) accent else LandscapeTokens.Ink,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis
      )
    }

    if (isSelected) {
      Box(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .size(13.dp)
          .clip(CircleShape)
          .background(accent),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(9.dp))
      }
    }
  }
}

// =========================================================================
// 5. Tool 4: Eraser (Narrow Card, Quick Size + Custom Slider)
// =========================================================================

@Composable
fun LandscapeEraserPanel(
  mode: EraserMode,
  size: Float,
  onModeChange: (EraserMode) -> Unit,
  onSizeChange: (Float) -> Unit,
  onDismiss: () -> Unit
) {
  val accent = Color(0xFFEA580C)
  val presets = listOf(10f, 20f, 40f, 60f, 80f)

  LandscapePanelFrame(
    widthFraction = 0.30f,
    maxHeightFraction = 0.50f,
    accent = accent,
    icon = Icons.Default.AutoAwesome,
    title = stringResource(R.string.eraser_size_title),
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // QUICK SIZE Chips
      LandscapeSectionLabel("QUICK SIZE")
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        presets.forEach { p ->
          LandscapeSizeChip(
            label = p.toInt().toString(),
            selected = size == p,
            accent = accent,
            onClick = { onSizeChange(p) }
          )
        }
      }

      // CUSTOM · value
      LandscapeSectionLabel("CUSTOM", valueSuffix = size.toInt().toString())
      LandscapeSlider(
        value = size,
        valueRange = 5f..100f,
        accent = accent,
        onValueChange = onSizeChange
      )
    }
  }
}

// =========================================================================
// 6. Tool 5: Board Color (Wide Card, Two Columns)
// =========================================================================

@Composable
fun LandscapeBoardColorPanel(
  currentColor: Int,
  currentStyle: BackgroundStyle,
  onApply: (Int, BackgroundStyle) -> Unit,
  onDismiss: () -> Unit
) {
  val accent = Color(0xFF0D9488)
  var selectedColor by remember { mutableIntStateOf(currentColor) }
  var selectedStyle by remember { mutableStateOf(currentStyle) }

  val classicBoards = listOf(
    0xFFFFFFFF.toInt() to "White",
    0xFFFAF6EE.toInt() to "Cream",
    0xFF1B4332.toInt() to "Chalk",
    0xFF121212.toInt() to "Black",
    0xFF2D3748.toInt() to "Slate",
    0xFF0F172A.toInt() to "Navy"
  )
  val lightTints: List<Int> = listOf(
    0xFFB6F0E4.toInt(),
    0xFFE8DEFC.toInt(),
    0xFFDCE7FE.toInt(),
    0xFFFDE0E8.toInt(),
    0xFFFCE4D6.toInt(),
    0xFFD8F3DC.toInt(),
    0xFFFEF9C3.toInt(),
    0xFFFFE4E6.toInt()
  )
  val textures = BackgroundStyle.entries

  LandscapePanelFrame(
    widthFraction = 0.78f,
    maxHeightFraction = 0.85f,
    accent = accent,
    icon = Icons.Default.Palette,
    title = stringResource(R.string.board_color_title),
    onDismiss = onDismiss
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Left Column (45%)
      Column(
        modifier = Modifier
          .weight(0.45f)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Preview Card (80 dp)
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(72.dp)
            .clip(RoundedCornerShape(14.dp))
            .background(Color(selectedColor))
            .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(14.dp))
            .padding(10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "y = 2x + 1",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = if (selectedColor == 0xFF121212.toInt() || selectedColor == 0xFF1B3B2B.toInt() || selectedColor == 0xFF0F1E36.toInt()) Color.White else Color(0xFF0F172A)
          )
          Text(
            text = "${selectedStyle.name.lowercase().replaceFirstChar { it.uppercase() }}",
            fontSize = 10.sp,
            color = LandscapeTokens.Muted,
            modifier = Modifier.align(Alignment.BottomEnd)
          )
        }

        // CLASSIC BOARDS
        LandscapeSectionLabel(stringResource(R.string.classic_boards))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          classicBoards.forEach { (c, name) ->
            val isSelected = selectedColor == c
            Column(
              horizontalAlignment = Alignment.CenterHorizontally,
              modifier = Modifier.weight(1f)
            ) {
              Box(
                modifier = Modifier
                  .size(40.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(Color(c))
                  .border(
                    BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accent else LandscapeTokens.Hairline),
                    RoundedCornerShape(10.dp)
                  )
                  .clickable {
                    selectedColor = c
                    onApply(selectedColor, selectedStyle)
                  },
                contentAlignment = Alignment.Center
              ) {
                if (isSelected) {
                  Icon(
                    Icons.Default.Check,
                    contentDescription = null,
                    tint = if (c == 0xFFFFFFFF.toInt() || c == 0xFFFAF7EE.toInt()) accent else Color.White,
                    modifier = Modifier.size(16.dp)
                  )
                }
              }
              Spacer(Modifier.height(2.dp))
              Text(
                text = name,
                fontSize = 9.5.sp,
                color = if (isSelected) accent else LandscapeTokens.Muted,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                maxLines = 1
              )
            }
          }
        }

        // LIGHT TINTS
        LandscapeSectionLabel(stringResource(R.string.light_tints))
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          lightTints.forEach { c ->
            val isSelected = selectedColor == c
            Box(
              modifier = Modifier
                .weight(1f)
                .height(38.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(Color(c))
                .border(
                  BorderStroke(if (isSelected) 2.dp else 1.dp, if (isSelected) accent else LandscapeTokens.Hairline),
                  RoundedCornerShape(10.dp)
                )
                .clickable {
                  selectedColor = c
                  onApply(selectedColor, selectedStyle)
                },
              contentAlignment = Alignment.Center
            ) {
              if (isSelected) {
                Icon(Icons.Default.Check, contentDescription = null, tint = accent, modifier = Modifier.size(16.dp))
              }
            }
          }
        }
      }

      // Right Column (55%)
      Column(
        modifier = Modifier
          .weight(0.55f)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // TEXTURE (5-column grid)
        LandscapeSectionLabel("TEXTURE")
        val textureRows = textures.chunked(5)
        textureRows.forEach { rowStyles ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            rowStyles.forEach { style ->
              val isSelected = selectedStyle == style
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(42.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSelected) accent.copy(alpha = 0.08f) else Color.White)
                  .border(
                    BorderStroke(if (isSelected) 1.5.dp else 1.dp, if (isSelected) accent else LandscapeTokens.Hairline),
                    RoundedCornerShape(10.dp)
                  )
                  .clickable {
                    selectedStyle = style
                    onApply(selectedColor, selectedStyle)
                  }
                  .padding(2.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = style.name.take(4).lowercase().replaceFirstChar { it.uppercase() },
                  fontSize = 10.sp,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSelected) accent else LandscapeTokens.Ink,
                  maxLines = 1
                )
                if (isSelected) {
                  Box(
                    modifier = Modifier
                      .align(Alignment.TopEnd)
                      .size(11.dp)
                      .clip(CircleShape)
                      .background(accent),
                    contentAlignment = Alignment.Center
                  ) {
                    Icon(Icons.Default.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(8.dp))
                  }
                }
              }
            }
            for (i in 0 until (5 - rowStyles.size)) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }
  }
}

// =========================================================================
// 7. Tool 6: Text Bar (Single Horizontal 56 dp Inline Bar)
// =========================================================================

@Composable
fun LandscapeTextBar(
  initialColor: Int,
  initialFontSize: Float,
  onConfirm: (Serialization.RichTextMeta, Int, Float) -> Unit,
  onDismiss: () -> Unit
) {
  val accent = Color(0xFF2563EB)
  var text by remember { mutableStateOf("") }
  var textColor by remember { mutableIntStateOf(initialColor) }
  var fontSize by remember { mutableFloatStateOf(initialFontSize.coerceIn(36f, 100f)) }

  val sizeOptions = listOf(36f, 48f, 72f, 100f)
  val config = LocalConfiguration.current
  val barWidth = (config.screenWidthDp.dp * 0.72f).coerceAtLeast(480.dp)

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .clickable(onClick = onDismiss)
        .padding(bottom = 68.dp),
      contentAlignment = Alignment.BottomCenter
    ) {
      Surface(
        modifier = Modifier
          .width(barWidth)
          .height(56.dp)
          .clickable(enabled = false) {},
        shape = RoundedCornerShape(16.dp),
        color = Color.White,
        border = BorderStroke(1.dp, LandscapeTokens.Hairline),
        shadowElevation = 18.dp
      ) {
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          // Close ✕
          Icon(
            imageVector = Icons.Default.Close,
            contentDescription = "Close",
            tint = LandscapeTokens.Muted,
            modifier = Modifier
              .size(18.dp)
              .clickable(onClick = onDismiss)
          )

          // Tool Badge [T]
          Box(
            modifier = Modifier
              .size(24.dp)
              .clip(RoundedCornerShape(7.dp))
              .background(accent),
            contentAlignment = Alignment.Center
          ) {
            Text("T", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold)
          }

          // Input Field
          Box(
            modifier = Modifier
              .width(180.dp)
              .height(36.dp)
              .clip(RoundedCornerShape(10.dp))
              .background(Color(0xFFF8FAFC))
              .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(10.dp))
              .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart
          ) {
            BasicTextField(
              value = text,
              onValueChange = { text = it },
              singleLine = true,
              textStyle = TextStyle(fontSize = 13.sp, color = LandscapeTokens.Ink, fontWeight = FontWeight.Medium),
              cursorBrush = SolidColor(accent),
              modifier = Modifier.fillMaxWidth(),
              decorationBox = { inner ->
                if (text.isEmpty()) {
                  Text("Type here...", fontSize = 13.sp, color = LandscapeTokens.Muted)
                }
                inner()
              }
            )
          }

          // Swatches
          LandscapeSwatchRow(
            selectedColor = textColor,
            accent = accent,
            showRainbow = false,
            onSelectColor = { textColor = it }
          )

          // Size Chips: 36, 48, 72, 100
          Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            sizeOptions.forEach { s ->
              val isSel = fontSize == s
              Box(
                modifier = Modifier
                  .height(34.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSel) accent.copy(alpha = 0.08f) else Color.White)
                  .border(
                    BorderStroke(if (isSel) 1.5.dp else 1.dp, if (isSel) accent else LandscapeTokens.Hairline),
                    RoundedCornerShape(10.dp)
                  )
                  .clickable { fontSize = s }
                  .padding(horizontal = 8.dp),
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = s.toInt().toString(),
                  fontSize = 12.sp,
                  fontWeight = if (isSel) FontWeight.Bold else FontWeight.Medium,
                  color = if (isSel) accent else LandscapeTokens.Ink
                )
              }
            }
          }

          Spacer(Modifier.weight(1f))

          // Add Button
          Button(
            onClick = {
              if (text.isNotBlank()) {
                onConfirm(Serialization.RichTextMeta(text), textColor, fontSize)
                onDismiss()
              }
            },
            enabled = text.isNotBlank(),
            shape = RoundedCornerShape(10.dp),
            colors = ButtonDefaults.buttonColors(
              containerColor = accent,
              disabledContainerColor = accent.copy(alpha = 0.25f)
            ),
            modifier = Modifier.height(38.dp)
          ) {
            Text(stringResource(R.string.add_action), fontSize = 13.sp, fontWeight = FontWeight.Bold)
          }
        }
      }
    }
  }
}

// =========================================================================
// 8. Tool 7: Math Formula (Wide Card, Quick Insert Grid)
// =========================================================================

@Composable
fun LandscapeFormulaPanel(
  initialLatex: String,
  initialColor: Int,
  initialSize: Float,
  onConfirm: (String, Int, Float) -> Unit,
  onDismiss: () -> Unit
) {
  val accent = Color(0xFF4F46E5)
  var latex by remember { mutableStateOf(initialLatex) }
  var color by remember { mutableIntStateOf(initialColor) }
  var fontSize by remember { mutableFloatStateOf(initialSize.coerceIn(12f, 96f)) }

  val quickKeys = listOf(
    "a/b" to "\\frac{a}{b}", "√x" to "\\sqrt{x}", "x²" to "x^2", "xₙ" to "x_n",
    "∫" to "\\int", "Σ" to "\\sum", "()" to "()", "=" to "=",
    "+" to "+", "−" to "-", "×" to "\\times", "÷" to "\\div",
    "≤" to "\\le", "≥" to "\\ge", "≠" to "\\ne", "≈" to "\\approx",
    "π" to "\\pi", "θ" to "\\theta", "α" to "\\alpha", "β" to "\\beta"
  )

  LandscapePanelFrame(
    widthFraction = 0.52f,
    maxHeightFraction = 0.78f,
    accent = accent,
    title = stringResource(R.string.math_formula_title),
    onDismiss = onDismiss,
    headerTrailingContent = {
      Button(
        onClick = {
          if (latex.isNotBlank()) {
            onConfirm(latex, color, fontSize)
            onDismiss()
          }
        },
        enabled = latex.isNotBlank(),
        shape = RoundedCornerShape(10.dp),
        colors = ButtonDefaults.buttonColors(containerColor = accent),
        modifier = Modifier.height(32.dp),
        contentPadding = PaddingValues(horizontal = 12.dp)
      ) {
        Text(stringResource(R.string.add_action), fontSize = 12.sp, fontWeight = FontWeight.Bold)
      }
    }
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Inputs Row: 2 cards (80 dp)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Formula input
        Box(
          modifier = Modifier
            .weight(1f)
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(12.dp))
            .padding(10.dp),
          contentAlignment = Alignment.TopStart
        ) {
          BasicTextField(
            value = latex,
            onValueChange = { latex = it },
            textStyle = TextStyle(fontSize = 13.sp, color = LandscapeTokens.Ink, fontWeight = FontWeight.Medium),
            cursorBrush = SolidColor(accent),
            modifier = Modifier.fillMaxSize(),
            decorationBox = { inner ->
              if (latex.isEmpty()) {
                Text(stringResource(R.string.formula_latex_hint), fontSize = 12.sp, color = LandscapeTokens.Muted)
              }
              inner()
            }
          )
        }

        // Preview box
        Box(
          modifier = Modifier
            .weight(1f)
            .height(72.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(12.dp))
            .padding(10.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = if (latex.isBlank()) stringResource(R.string.preview_placeholder) else latex,
            fontSize = 13.sp,
            color = if (latex.isBlank()) LandscapeTokens.Muted else Color(color),
            fontWeight = FontWeight.SemiBold,
            maxLines = 2
          )
        }
      }

      // Split: Left (Color + Font size) & Right (Quick Insert Grid)
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
      ) {
        // Left Column (45%)
        Column(
          modifier = Modifier.weight(0.45f),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          LandscapeSectionLabel(stringResource(R.string.label_color))
          LandscapeSwatchRow(
            palette = LandscapeTokens.FormulaPalette,
            selectedColor = color,
            accent = accent,
            showRainbow = false,
            onSelectColor = { color = it }
          )

          LandscapeSectionLabel("FONT SIZE", valueSuffix = fontSize.toInt().toString())
          LandscapeSlider(
            value = fontSize,
            valueRange = 12f..96f,
            accent = accent,
            onValueChange = { fontSize = it }
          )
        }

        // Right Column (55%): Quick Insert Grid (4 columns)
        Column(
          modifier = Modifier.weight(0.55f),
          verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
          LandscapeSectionLabel("QUICK INSERT")
          val keyRows = quickKeys.chunked(4)
          keyRows.forEach { r ->
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              r.forEach { (label, sym) ->
                Box(
                  modifier = Modifier
                    .weight(1f)
                    .height(34.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(Color.White)
                    .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(10.dp))
                    .clickable { latex += sym },
                  contentAlignment = Alignment.Center
                ) {
                  Text(text = label, fontSize = 12.sp, fontWeight = FontWeight.Bold, color = LandscapeTokens.Ink)
                }
              }
            }
          }
        }
      }
    }
  }
}

// =========================================================================
// 9. Tool 8: OCR Text Scanner (Narrow Card, Clean Actions)
// =========================================================================

@Composable
fun LandscapeOcrPanel(
  onTakePhoto: () -> Unit,
  onChooseGallery: () -> Unit,
  onOpenHistory: () -> Unit,
  onDismiss: () -> Unit
) {
  val accent = Color(0xFF059669)

  LandscapePanelFrame(
    widthFraction = 0.32f,
    maxHeightFraction = 0.80f,
    hardMaxWidthDp = 340.dp,
    accent = accent,
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(vertical = 4.dp),
      horizontalAlignment = Alignment.CenterHorizontally,
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Centered Scan Badge
      Box(
        modifier = Modifier
          .size(44.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(accent.copy(alpha = 0.1f))
          .border(1.5.dp, accent, RoundedCornerShape(14.dp)),
        contentAlignment = Alignment.Center
      ) {
        Icon(Icons.Default.PhotoCamera, contentDescription = null, tint = accent, modifier = Modifier.size(22.dp))
      }

      Text(
        text = stringResource(R.string.ocr_title),
        fontSize = 15.sp,
        fontWeight = FontWeight.Bold,
        color = LandscapeTokens.Ink
      )
      Text(
        text = stringResource(R.string.ocr_offline_note),
        fontSize = 11.sp,
        color = LandscapeTokens.Muted
      )

      HorizontalDivider(color = LandscapeTokens.Hairline, thickness = 1.dp)

      // 3 Action Rows
      OcrActionRow(icon = Icons.Default.PhotoCamera, title = stringResource(R.string.ocr_take_photo), accent = accent, onClick = { onDismiss(); onTakePhoto() })
      OcrActionRow(icon = Icons.Default.Image, title = stringResource(R.string.ocr_choose_gallery), accent = accent, onClick = { onDismiss(); onChooseGallery() })
      OcrActionRow(icon = Icons.Default.History, title = stringResource(R.string.ocr_scan_history), accent = accent, onClick = onOpenHistory)

      HorizontalDivider(color = LandscapeTokens.Hairline, thickness = 1.dp)

      // 100% Free On-Device Badge Button
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(40.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(accent.copy(alpha = 0.12f)),
        contentAlignment = Alignment.Center
      ) {
        Text("✨ 100% Free · Unlimited Offline Scans", fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold, color = accent)
      }
    }
  }
}

@Composable
private fun OcrActionRow(
  icon: ImageVector,
  title: String,
  accent: Color,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .height(44.dp)
      .clip(RoundedCornerShape(12.dp))
      .background(Color(0xFFF8FAFC))
      .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(12.dp))
      .clickable(onClick = onClick)
      .padding(horizontal = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Icon(icon, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
    Spacer(Modifier.width(10.dp))
    Text(text = title, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LandscapeTokens.Ink)
  }
}

// =========================================================================
// 10. Tool 9: Stickers (Narrow Card, Category Pills, 3 Columns)
// =========================================================================

@Composable
fun LandscapeStickersPanel(
  onPickSticker: (String, Int) -> Unit,
  onDismiss: () -> Unit
) {
  val accent = Color(0xFFD97706)
  val allStickers = StickerCatalog.generalStickers
  val categories = remember { allStickers.map { it.category }.distinct() }
  var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }

  val filteredStickers = remember(selectedCategory) {
    allStickers.filter { it.category == selectedCategory }
  }

  LandscapePanelFrame(
    widthFraction = 0.38f,
    maxHeightFraction = 0.82f,
    accent = accent,
    icon = Icons.Default.AutoAwesome,
    title = stringResource(R.string.stickers_title),
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      // Category tabs horizontally scrollable
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categories.forEach { cat ->
          LandscapeCategoryPill(
            label = cat,
            selected = cat == selectedCategory,
            accent = accent,
            onClick = { selectedCategory = cat }
          )
        }
      }

      LandscapePagerStrip(currentPage = 1, totalPages = 1)

      // 3 Columns Grid
      val rows = filteredStickers.chunked(3)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 280.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        rows.forEach { r ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            r.forEach { spec ->
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(64.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.White)
                  .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(12.dp))
                  .clickable {
                    onPickSticker(spec.id, 0xFF2563EB.toInt())
                    onDismiss()
                  }
                  .padding(4.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Canvas(modifier = Modifier.size(34.dp)) {
                    StickerCatalog.drawSticker(drawContext.canvas.nativeCanvas, spec.id, 0xFF2563EB.toInt(), size.width, size.height)
                  }
                  Text(text = spec.label, fontSize = 9.sp, color = LandscapeTokens.Muted, maxLines = 1)
                }
              }
            }
            for (i in 0 until (3 - r.size)) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }
  }
}

// =========================================================================
// 11. Tool 10: Science (Narrow Card, Category Pills, 3 Columns)
// =========================================================================

@Composable
fun LandscapeSciencePanel(
  onPickSticker: (String, Int) -> Unit,
  onDismiss: () -> Unit
) {
  val accent = Color(0xFF16A34A)
  val allStickers = StickerCatalog.scienceStickers
  val categories = remember { allStickers.map { it.category }.distinct() }
  var selectedCategory by remember { mutableStateOf(categories.firstOrNull() ?: "") }

  val filteredStickers = remember(selectedCategory) {
    allStickers.filter { it.category == selectedCategory }
  }

  LandscapePanelFrame(
    widthFraction = 0.38f,
    maxHeightFraction = 0.82f,
    accent = accent,
    icon = Icons.Default.Science,
    title = stringResource(R.string.tool_science),
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        categories.forEach { cat ->
          LandscapeCategoryPill(
            label = cat,
            selected = cat == selectedCategory,
            accent = accent,
            onClick = { selectedCategory = cat }
          )
        }
      }

      LandscapePagerStrip(currentPage = 1, totalPages = (filteredStickers.size / 6).coerceAtLeast(1))

      val rows = filteredStickers.chunked(3)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 280.dp)
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        rows.forEach { r ->
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            r.forEach { spec ->
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(64.dp)
                  .clip(RoundedCornerShape(12.dp))
                  .background(Color.White)
                  .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(12.dp))
                  .clickable {
                    onPickSticker(spec.id, 0xFF16A34A.toInt())
                    onDismiss()
                  }
                  .padding(4.dp),
                contentAlignment = Alignment.Center
              ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                  Canvas(modifier = Modifier.size(34.dp)) {
                    StickerCatalog.drawSticker(drawContext.canvas.nativeCanvas, spec.id, 0xFF16A34A.toInt(), size.width, size.height)
                  }
                  Text(text = spec.label, fontSize = 9.sp, color = LandscapeTokens.Muted, maxLines = 1)
                }
              }
            }
            for (i in 0 until (3 - r.size)) {
              Spacer(modifier = Modifier.weight(1f))
            }
          }
        }
      }
    }
  }
}

// =========================================================================
// 12. Tool 11: Laser (Narrow Card, Max Width 320 dp)
// =========================================================================

@Composable
fun LandscapeLaserPanel(
  currentColor: Int,
  currentSize: Float,
  onColorChange: (Int) -> Unit,
  onSizeChange: (Float) -> Unit,
  onDismiss: () -> Unit
) {
  val accent = Color(0xFFDC2626)
  val presets = listOf(4f, 8f, 16f, 24f, 32f)

  LandscapePanelFrame(
    widthFraction = 0.30f,
    maxHeightFraction = 0.62f,
    hardMaxWidthDp = 320.dp,
    accent = accent,
    icon = Icons.Default.SquareFoot,
    title = stringResource(R.string.laser_pointer_title),
    onDismiss = onDismiss
  ) {
    Column(
      modifier = Modifier.fillMaxWidth(),
      verticalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // COLOR: 7 flat cells
      LandscapeSectionLabel(stringResource(R.string.label_color))
      LandscapeSwatchRow(
        selectedColor = currentColor,
        accent = accent,
        showRainbow = false,
        onSelectColor = onColorChange
      )

      // SIZE Chips: 4, 8, 16, 24, 32
      LandscapeSectionLabel(stringResource(R.string.label_size))
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        presets.forEach { p ->
          LandscapeSizeChip(
            label = p.toInt().toString(),
            selected = currentSize == p,
            accent = accent,
            onClick = { onSizeChange(p) }
          )
        }
      }

      // CUSTOM · 8
      LandscapeSectionLabel("CUSTOM", valueSuffix = currentSize.toInt().toString())
      LandscapeSlider(
        value = currentSize,
        valueRange = 4f..32f,
        accent = accent,
        onValueChange = onSizeChange
      )
    }
  }
}

// =========================================================================
// 13. Tool 12: Screen Recorder (Wide Card, 2 Columns)
// =========================================================================

@Composable
fun LandscapeRecorderPanel(
  onStartRecording: (RecordingQuality, Boolean, Boolean) -> Unit,
  onDismiss: () -> Unit
) {
  val accent = Color(0xFFE11D48)
  var recordVoice by remember { mutableStateOf(true) }
  var faceCam by remember { mutableStateOf(false) }
  var quality by remember { mutableStateOf(RecordingQuality.P720) }

  LandscapePanelFrame(
    widthFraction = 0.74f,
    maxHeightFraction = 0.66f,
    accent = accent,
    icon = Icons.Default.RadioButtonChecked,
    title = stringResource(R.string.record_screen_title),
    onDismiss = onDismiss
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Left Column (55%)
      Column(
        modifier = Modifier.weight(0.55f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Card 1: Voice + Face Cam
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(12.dp))
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Mic, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.recorder_voice), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LandscapeTokens.Ink, modifier = Modifier.weight(1f))
            Switch(
              checked = recordVoice,
              onCheckedChange = { recordVoice = it },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent)
            )
          }

          HorizontalDivider(thickness = 1.dp, color = LandscapeTokens.Hairline)

          Row(
            modifier = Modifier
              .fillMaxWidth()
              .height(48.dp)
              .padding(horizontal = 12.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Icon(Icons.Default.Videocam, contentDescription = null, tint = accent, modifier = Modifier.size(18.dp))
            Spacer(Modifier.width(10.dp))
            Text(stringResource(R.string.recorder_facecam), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LandscapeTokens.Ink, modifier = Modifier.weight(1f))
            Switch(
              checked = faceCam,
              onCheckedChange = { faceCam = it },
              colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = accent)
            )
          }
        }

        // Card 2: Video quality (HD badge, 480p, 720p)
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, LandscapeTokens.Hairline, RoundedCornerShape(12.dp))
            .padding(10.dp),
          verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
              modifier = Modifier
                .clip(RoundedCornerShape(4.dp))
                .background(accent)
                .padding(horizontal = 5.dp, vertical = 2.dp)
            ) {
              Text("HD", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(Modifier.width(8.dp))
            Text(stringResource(R.string.video_quality), fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = LandscapeTokens.Ink)
          }

          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
          ) {
            listOf(RecordingQuality.P480, RecordingQuality.P720).forEach { q ->
              val isSel = quality == q
              Box(
                modifier = Modifier
                  .weight(1f)
                  .height(36.dp)
                  .clip(RoundedCornerShape(10.dp))
                  .background(if (isSel) accent else Color.White)
                  .border(
                    BorderStroke(1.5.dp, if (isSel) accent else LandscapeTokens.Hairline),
                    RoundedCornerShape(10.dp)
                  )
                  .clickable { quality = q },
                contentAlignment = Alignment.Center
              ) {
                Text(
                  text = q.label,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold,
                  color = if (isSel) Color.White else LandscapeTokens.Ink
                )
              }
            }
          }
          Text(
            text = "${quality.label} · 24 fps · ${quality.mbPerMinEstimate}",
            fontSize = 11.sp,
            color = LandscapeTokens.Muted
          )
        }
      }

      // Right Column (45%)
      Column(
        modifier = Modifier.weight(0.45f),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        // Free Use card
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White)
            .border(1.5.dp, accent, RoundedCornerShape(12.dp))
            .padding(12.dp),
          contentAlignment = Alignment.Center
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("Free Unlimited Recording", fontSize = 14.sp, fontWeight = FontWeight.Bold, color = LandscapeTokens.Ink)
            Spacer(Modifier.height(2.dp))
            Text("No watermark · Free for life", fontSize = 11.sp, color = LandscapeTokens.Muted)
          }
        }

        // Start Recording button
        Button(
          onClick = {
            onStartRecording(quality, recordVoice, faceCam)
            onDismiss()
          },
          shape = RoundedCornerShape(12.dp),
          colors = ButtonDefaults.buttonColors(containerColor = accent),
          modifier = Modifier
            .fillMaxWidth()
            .height(52.dp)
        ) {
          Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(stringResource(R.string.start_recording), fontSize = 14.sp, fontWeight = FontWeight.Bold)
            Text("Saved to Movies/TeachBoard", fontSize = 10.sp, color = Color.White.copy(alpha = 0.85f))
          }
        }
      }
    }
  }
}
