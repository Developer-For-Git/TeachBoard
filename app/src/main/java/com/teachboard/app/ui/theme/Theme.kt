package com.teachboard.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/**
 * [PLACEHOLDER] Single place for app configuration & brand flags (PRD 17 & 19.0).
 */
object AppConfig {
  const val APP_NAME = "TeachBoard"
  const val PACKAGE_NAME = "com.teachboard.app"
  const val FILE_PROVIDER_AUTHORITY = "com.teachboard.app.fileprovider"
  const val ALBUM_NAME = "TeachBoard"
  const val CONTACT_EMAIL = "support@teachboard.example.com"
  const val PRIVACY_POLICY_URL = "https://teachboard.example.com/privacy"
  const val PLAY_STORE_URL = "https://play.google.com/store/apps/details?id=$PACKAGE_NAME"
  const val USER_AGENT = "TeachBoard/1.0.0 ($CONTACT_EMAIL)"

  const val IS_PRO_ENABLED = false
  const val ADS_ENABLED = false

  const val MAX_UNDO_STEPS = 60
  const val MAX_LAYERS_PER_BOARD = 10
  const val MAX_OCR_HISTORY = 20
  const val MAX_SHORTCUTS = 10
  const val MIN_ZOOM = 0.3f
  const val MAX_ZOOM = 6.0f
  const val PDF_CACHE_MAX_PAGES = 3
  const val SMART_WRITING_PAUSE_MS = 1500L
  const val CURRENT_TOUR_VERSION = 1
}

interface RecordingLimitPolicy {
  val maxDurationSeconds: Int?
}

object FreeV1RecordingLimitPolicy : RecordingLimitPolicy {
  override val maxDurationSeconds: Int? = null
}

enum class ThemeMode {
  SYSTEM, LIGHT, DARK
}

/**
 * [PLACEHOLDER] All named color tokens from PRD Section 11 live here.
 */
object TeachColors {
  val Primary = Color(0xFF2563EB)
  val OnPrimary = Color(0xFFFFFFFF)
  val Secondary = Color(0xFF0D9488)
  val OnSecondary = Color(0xFFFFFFFF)
  val Surface = Color(0xFFFFFFFF)
  val SurfaceVariant = Color(0xFFF3F4F6)
  val Background = Color(0xFFF8FAFC)
  val OnSurface = Color(0xFF111827)
  val OnSurfaceMuted = Color(0xFF6B7280)
  val Outline = Color(0xFFE5E7EB)
  val Danger = Color(0xFFDC2626)
  val Success = Color(0xFF16A34A)

  val PrimaryDark = Color(0xFF60A5FA)
  val OnPrimaryDark = Color(0xFF0F172A)
  val SecondaryDark = Color(0xFF2DD4BF)
  val OnSecondaryDark = Color(0xFF042F2E)
  val SurfaceDark = Color(0xFF1E2430)
  val SurfaceVariantDark = Color(0xFF2B3242)
  val BackgroundDark = Color(0xFF11151E)
  val OnSurfaceDark = Color(0xFFF3F4F6)
  val OnSurfaceMutedDark = Color(0xFF9CA3AF)
  val OutlineDark = Color(0xFF374151)

  val ToolbarBg = Color(0xFFF1F3F5)
  val ToolbarBgDark = Color(0xFF1F2430)
  val ToolbarIcon = Color(0xFF374151)
  val ToolbarIconDark = Color(0xFFE5E7EB)
  val ToolbarSelectedBg = Color(0xFFDCE4F2)
  val ToolbarSelectedBgDark = Color(0xFF334155)

  val CanvasButtonBg = Color(0xCC262A34)
  val CanvasButtonFg = Color(0xFFFFFFFF)
  val DarkPillBg = Color(0xE61C2029)
  val DarkPillFg = Color(0xFFFFFFFF)
  val SavedToastBg = Color(0xE6166534)
  val SavedToastFg = Color(0xFFFFFFFF)
  val BadgeAiBg = Color(0xFF7C3AED)
  val BadgeAiFg = Color(0xFFFFFFFF)
  val RecordRed = Color(0xFFDC2626)

  val AccentTeal = Color(0xFF0D9488)
  val AccentTealTint = Color(0xFFE6F7F5)
  val AccentTealTintDark = Color(0xFF133835)

  val AccentPurple = Color(0xFF7C3AED)
  val AccentPurpleTint = Color(0xFFF3EEFF)
  val AccentPurpleTintDark = Color(0xFF2C1E4A)

  val AccentBlue = Color(0xFF2563EB)
  val AccentBlueTint = Color(0xFFEBF2FF)
  val AccentBlueTintDark = Color(0xFF1A2C4E)

  val AccentOrange = Color(0xFFEA580C)
  val AccentOrangeTint = Color(0xFFFFF1E8)
  val AccentOrangeTintDark = Color(0xFF432312)

  // Classic Boards (ARGB Ints for Room + Android Canvas)
  val BoardWhite = 0xFFFFFFFF.toInt()
  val BoardCream = 0xFFFFFBF0.toInt()
  val BoardChalkboard = 0xFF1E4631.toInt()
  val BoardBlack = 0xFF121212.toInt()
  val BoardSlate = 0xFF2D3748.toInt()
  val BoardNavy = 0xFF1A2744.toInt()

  // Light Tints (6 pastel board colors)
  val BoardPastelYellow = 0xFFFFE58F.toInt()
  val BoardPastelPink = 0xFFFFC1CC.toInt()
  val BoardPastelBlue = 0xFFBFDBFE.toInt()
  val BoardPastelGreen = 0xFFBBF7D0.toInt()
  val BoardPastelOrange = 0xFFFED7AA.toInt()
  val BoardPastelPurple = 0xFFDDD6FE.toInt()

  // Deep Tones (6 rich dark board colors — audited from reference Board Color panel)
  val BoardDeepForest = 0xFF1B5E20.toInt()
  val BoardDeepTeal = 0xFF004D40.toInt()
  val BoardDeepNavy = 0xFF0D254C.toInt()
  val BoardDeepPlum = 0xFF4A144C.toInt()
  val BoardDeepBurgundy = 0xFF5D1919.toInt()
  val BoardDeepEspresso = 0xFF3E2723.toInt()

  // Soft Pastel Modal Header Tints & Grouped Trays
  val HeaderSkyTint = Color(0xFFE6F4FF)
  val HeaderTealTint = Color(0xFFE2F6F3)
  val HeaderPurpleTint = Color(0xFFF1E9FF)
  val HeaderIndigoTint = Color(0xFFECEEFF)
  val HeaderRoseTint = Color(0xFFFFE5EC)
  val HeaderPinkTint = Color(0xFFFFE6F0)
  val HeaderMintTint = Color(0xFFE4F8EC)
  val HeaderPeachTint = Color(0xFFFFEFE6)
  val HeaderAmberTint = Color(0xFFFEF3C7)
  val HeaderCyanTint = Color(0xFFE3F7FB)
  val HeaderSlateTint = Color(0xFFEEF1F5)
  val TrayBg = Color(0xFFF3F6FA)
  val TrayBorder = Color(0xFFE2E8F0)
  val AccentRose = Color(0xFFE11D48)
  val AccentPink = Color(0xFFEC4899)
  val AccentEmerald = Color(0xFF16A34A)
  val AccentAmber = Color(0xFFB45309)
  val AccentIndigo = Color(0xFF4F46E5)
  val AccentSky = Color(0xFF0EA5E9)
  val AccentCyan = Color(0xFF0891B2)
  val AccentSlate = Color(0xFF475569)

  // Brush Swatches
  val BrushBlack = 0xFF111827.toInt()
  val BrushRed = 0xFFEF4444.toInt()
  val BrushOrange = 0xFFF97316.toInt()
  val BrushYellow = 0xFFEAB308.toInt()
  val BrushGreen = 0xFF22C55E.toInt()
  val BrushBlue = 0xFF2563EB.toInt()
  val BrushPurple = 0xFF9333EA.toInt()
  val BrushWhite = 0xFFFFFFFF.toInt()

  // PDF Quick Colors (PRD 18.6)
  val PdfYellow = 0xFFFACC15.toInt()
  val PdfGreen = 0xFF4ADE80.toInt()
  val PdfBlue = 0xFF60A5FA.toInt()
  val PdfPink = 0xFFF472B6.toInt()
  val PdfOrange = 0xFFFB923C.toInt()

  // Laser Colors (PRD 4.6b)
  val LaserRed = 0xFFEF4444.toInt()
  val LaserGreen = 0xFF22C55E.toInt()
  val LaserBlue = 0xFF3B82F6.toInt()
  val LaserYellow = 0xFFFACC15.toInt()
  val LaserMagenta = 0xFFEC4899.toInt()
  val LaserCyan = 0xFF06B6D4.toInt()
  val LaserWhite = 0xFFFFFFFF.toInt()

  // Geometry Wood/Beige Overlays (PRD 18.4)
  val GeometryWoodFill = Color(0xDCF3E2C0)
  val GeometryWoodBorder = Color(0xFF9A7B4F)
  val GeometryTickMark = Color(0xFF4A3720)
  val GeometryAngleArm = Color(0xFF2563EB)

  // Canvas helper colors
  val SelectionAccent = 0xFF2563EB.toInt()
  val SelectionFill = 0x1F2563EB
  val NotebookMargin = 0x99EF4444.toInt()
  val TextureLineLightBg = 0x3364748B
  val TextureLineDarkBg = 0x44E2E8F0
  val TextureMajorLightBg = 0x55475569
  val TextureMajorDarkBg = 0x66F8FAFC
  val EraserCursorFill = 0x33EF4444
  val EraserCursorStroke = 0xCCEF4444.toInt()

  val ClassicBoardPalette = listOf(
    "white" to BoardWhite,
    "cream" to BoardCream,
    "chalkboard" to BoardChalkboard,
    "black" to BoardBlack,
    "slate" to BoardSlate,
    "navy" to BoardNavy
  )

  val LightTintBoardPalette = listOf(
    "pastel_yellow" to BoardPastelYellow,
    "pastel_pink" to BoardPastelPink,
    "pastel_blue" to BoardPastelBlue,
    "pastel_green" to BoardPastelGreen,
    "pastel_orange" to BoardPastelOrange,
    "pastel_purple" to BoardPastelPurple
  )

  val DeepToneBoardPalette = listOf(
    "deep_forest" to BoardDeepForest,
    "deep_teal" to BoardDeepTeal,
    "deep_navy" to BoardDeepNavy,
    "deep_plum" to BoardDeepPlum,
    "deep_burgundy" to BoardDeepBurgundy,
    "deep_espresso" to BoardDeepEspresso
  )

  val BrushSwatches = listOf(
    BrushBlack, BrushRed, BrushOrange, BrushYellow,
    BrushGreen, BrushBlue, BrushPurple, BrushWhite
  )

  val PdfQuickColors = listOf(PdfYellow, PdfGreen, PdfBlue, PdfPink, PdfOrange)

  val LaserColors = listOf(
    LaserRed, LaserGreen, LaserBlue, LaserYellow, LaserMagenta, LaserCyan, LaserWhite
  )

  fun isDarkColor(argb: Int): Boolean {
    val r = ((argb shr 16) and 0xFF) / 255.0
    val g = ((argb shr 8) and 0xFF) / 255.0
    val b = (argb and 0xFF) / 255.0
    return (0.2126 * r + 0.7152 * g + 0.0722 * b) < 0.45
  }
}

private val LightColorScheme = lightColorScheme(
  primary = TeachColors.Primary,
  onPrimary = TeachColors.OnPrimary,
  secondary = TeachColors.Secondary,
  onSecondary = TeachColors.OnSecondary,
  surface = TeachColors.Surface,
  onSurface = TeachColors.OnSurface,
  surfaceVariant = TeachColors.SurfaceVariant,
  onSurfaceVariant = TeachColors.OnSurfaceMuted,
  background = TeachColors.Background,
  onBackground = TeachColors.OnSurface,
  outline = TeachColors.Outline,
  error = TeachColors.Danger
)

private val DarkColorScheme = darkColorScheme(
  primary = TeachColors.PrimaryDark,
  onPrimary = TeachColors.OnPrimaryDark,
  secondary = TeachColors.SecondaryDark,
  onSecondary = TeachColors.OnSecondaryDark,
  surface = TeachColors.SurfaceDark,
  onSurface = TeachColors.OnSurfaceDark,
  surfaceVariant = TeachColors.SurfaceVariantDark,
  onSurfaceVariant = TeachColors.OnSurfaceMutedDark,
  background = TeachColors.BackgroundDark,
  onBackground = TeachColors.OnSurfaceDark,
  outline = TeachColors.OutlineDark,
  error = TeachColors.Danger
)

val TeachTypography = Typography(
  headlineMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Bold, fontSize = 24.sp, lineHeight = 32.sp),
  titleLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 20.sp, lineHeight = 28.sp),
  titleMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.SemiBold, fontSize = 16.sp, lineHeight = 22.sp),
  bodyLarge = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 15.sp, lineHeight = 22.sp),
  bodyMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Normal, fontSize = 13.sp, lineHeight = 18.sp),
  labelMedium = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 12.sp, lineHeight = 16.sp),
  labelSmall = TextStyle(fontFamily = FontFamily.SansSerif, fontWeight = FontWeight.Medium, fontSize = 10.sp, lineHeight = 13.sp)
)

@Immutable
data class ExtendedColors(
  val toolbarBg: Color,
  val toolbarIcon: Color,
  val toolbarSelectedBg: Color,
  val accentTealTint: Color,
  val accentPurpleTint: Color,
  val accentBlueTint: Color,
  val accentOrangeTint: Color,
  val isDark: Boolean
)

val LocalExtendedColors = staticCompositionLocalOf {
  ExtendedColors(
    toolbarBg = TeachColors.ToolbarBg,
    toolbarIcon = TeachColors.ToolbarIcon,
    toolbarSelectedBg = TeachColors.ToolbarSelectedBg,
    accentTealTint = TeachColors.AccentTealTint,
    accentPurpleTint = TeachColors.AccentPurpleTint,
    accentBlueTint = TeachColors.AccentBlueTint,
    accentOrangeTint = TeachColors.AccentOrangeTint,
    isDark = false
  )
}

@Composable
fun TeachBoardTheme(
  themeMode: ThemeMode = ThemeMode.SYSTEM,
  content: @Composable () -> Unit
) {
  val useDark = when (themeMode) {
    ThemeMode.SYSTEM -> isSystemInDarkTheme()
    ThemeMode.LIGHT -> false
    ThemeMode.DARK -> true
  }
  val scheme = if (useDark) DarkColorScheme else LightColorScheme
  val extended = if (useDark) {
    ExtendedColors(
      toolbarBg = TeachColors.ToolbarBgDark,
      toolbarIcon = TeachColors.ToolbarIconDark,
      toolbarSelectedBg = TeachColors.ToolbarSelectedBgDark,
      accentTealTint = TeachColors.AccentTealTintDark,
      accentPurpleTint = TeachColors.AccentPurpleTintDark,
      accentBlueTint = TeachColors.AccentBlueTintDark,
      accentOrangeTint = TeachColors.AccentOrangeTintDark,
      isDark = true
    )
  } else {
    ExtendedColors(
      toolbarBg = TeachColors.ToolbarBg,
      toolbarIcon = TeachColors.ToolbarIcon,
      toolbarSelectedBg = TeachColors.ToolbarSelectedBg,
      accentTealTint = TeachColors.AccentTealTint,
      accentPurpleTint = TeachColors.AccentPurpleTint,
      accentBlueTint = TeachColors.AccentBlueTint,
      accentOrangeTint = TeachColors.AccentOrangeTint,
      isDark = false
    )
  }

  CompositionLocalProvider(LocalExtendedColors provides extended) {
    MaterialTheme(
      colorScheme = scheme,
      typography = TeachTypography,
      content = content
    )
  }
}
