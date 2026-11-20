package com.teachboard.app.ui.tour

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.outlined.Architecture
import androidx.compose.material.icons.outlined.AutoFixOff
import androidx.compose.material.icons.outlined.Brush
import androidx.compose.material.icons.outlined.Calculate
import androidx.compose.material.icons.outlined.Category
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.DeleteSweep
import androidx.compose.material.icons.outlined.DocumentScanner
import androidx.compose.material.icons.outlined.Draw
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EmojiEmotions
import androidx.compose.material.icons.outlined.FormatColorFill
import androidx.compose.material.icons.outlined.FormatShapes
import androidx.compose.material.icons.outlined.Functions
import androidx.compose.material.icons.outlined.Gesture
import androidx.compose.material.icons.outlined.Highlight
import androidx.compose.material.icons.outlined.Image
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Language
import androidx.compose.material.icons.outlined.Layers
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material.icons.outlined.NearMe
import androidx.compose.material.icons.outlined.PanTool
import androidx.compose.material.icons.outlined.PhotoCamera
import androidx.compose.material.icons.outlined.PictureAsPdf
import androidx.compose.material.icons.outlined.PlayCircle
import androidx.compose.material.icons.outlined.Redo
import androidx.compose.material.icons.outlined.Refresh
import androidx.compose.material.icons.outlined.Save
import androidx.compose.material.icons.outlined.Science
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material.icons.outlined.TextFields
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material.icons.outlined.Tune
import androidx.compose.material.icons.outlined.Undo
import androidx.compose.material.icons.outlined.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.BlendMode
import androidx.compose.ui.graphics.Brush as GraphicsBrush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.CompositingStrategy
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.boundsInRoot
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInRoot
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlin.math.roundToInt

/* ----------------------------------------------------------------------------------------------
 * 1. Palette + metrics (reversed from com.teachingboard.aitricker v2.9.47)
 * -------------------------------------------------------------------------------------------- */

object TourTokens {
    val Scrim = Color(0xFF0B1220)
    const val ScrimAlpha = 0.62f

    val Accent = Color(0xFF011EF0)      // active dot, gradient start
    val Accent2 = Color(0xFF01B2E9)     // pulse ring, gradient end
    val Title = Color(0xFF15213B)       // card title, label chips
    val Body = Color(0xFF5A6478)        // card body, Skip
    val DotIdle = Color(0xFFE2E8F0)
    val Card = Color(0xFFFFFFFF)

    val Sheet = Color(0xFF1E1E1E)
    val SheetAccent = Color(0xFF2979FF)

    val HoleRadius = 16.dp
    val HoleInset = 6.dp
    val HoleBreath = 22.dp
    val RingGrow = 5.dp
    val RingStroke = 3.dp
    val HoleBorder = 1.5.dp
    const val PulseMs = 1300
    const val RevealMs = 520
}

/* ----------------------------------------------------------------------------------------------
 * 2. Model
 * -------------------------------------------------------------------------------------------- */

enum class TourHighlight { CANVAS, TOOLBAR, TOP_CONTROLS }

data class TourStep(
    val id: String,
    val title: String,
    val description: String,
    val highlight: TourHighlight = TourHighlight.TOOLBAR,
    val toolIndex: Int = -1,
    val targetKeys: List<String> = emptyList(),
    val labels: Map<String, String> = emptyMap(),
)

/* ----------------------------------------------------------------------------------------------
 * 3. Anchor registry
 * -------------------------------------------------------------------------------------------- */

object TourAnchors {
    private val rects = mutableStateMapOf<String, Rect>()

    fun get(key: String): Rect? = rects[key]

    fun snapshot(): Map<String, Rect> = rects.toMap()

    fun clear() = rects.clear()

    internal fun record(key: String, rect: Rect) {
        if (rect.width <= 0f || rect.height <= 0f) return
        if (rects[key] != rect) rects[key] = rect
    }
}

fun Modifier.tourAnchor(key: String): Modifier = this.onGloballyPositioned { coordinates ->
    TourAnchors.record(key, coordinates.boundsInRoot())
}

/* ----------------------------------------------------------------------------------------------
 * 4. Persistence
 * -------------------------------------------------------------------------------------------- */

interface TourStore {
    fun shouldShow(): Boolean
    fun markCompleted()
    fun reset()
}

class PrefsTourStore(
    context: android.content.Context,
    name: String = "tour_prefs",
    private val version: Int = TOUR_VERSION,
) : TourStore {

    private val prefs = context.getSharedPreferences(name, android.content.Context.MODE_PRIVATE)

    override fun shouldShow(): Boolean =
        !prefs.getBoolean("tour_completed", false) || prefs.getInt("tour_version", 0) < version

    override fun markCompleted() {
        prefs.edit().putBoolean("tour_completed", true).putInt("tour_version", version).apply()
    }

    override fun reset() {
        prefs.edit().putBoolean("tour_completed", false).putInt("tour_version", 0).apply()
    }

    companion object {
        const val TOUR_VERSION = 3
    }
}

/* ----------------------------------------------------------------------------------------------
 * 5. Controller
 * -------------------------------------------------------------------------------------------- */

@Stable
class TourState internal constructor(
    val steps: List<TourStep>,
    private val store: TourStore?,
) {
    var visible by mutableStateOf(false)
        private set
    var index by mutableIntStateOf(0)
        private set

    val step: TourStep? get() = steps.getOrNull(index)
    val isLast: Boolean get() = index == steps.lastIndex
    val count: Int get() = steps.size

    fun start() {
        index = 0
        visible = steps.isNotEmpty()
    }

    fun next() {
        if (!visible) return
        if (index >= steps.lastIndex) finish() else index++
    }

    fun back() {
        if (index > 0) index--
    }

    fun skip() = finish()

    fun finish() {
        visible = false
        store?.markCompleted()
    }
}

@Composable
fun rememberTourState(steps: List<TourStep>, store: TourStore? = null): TourState =
    remember(steps, store) { TourState(steps, store) }

/* ----------------------------------------------------------------------------------------------
 * 6. The overlay
 * -------------------------------------------------------------------------------------------- */

@Composable
fun TourOverlay(
    state: TourState,
    modifier: Modifier = Modifier,
    onFinished: () -> Unit = {},
) {
    val step = state.step ?: return
    if (!state.visible) return

    val pulse = rememberInfiniteTransition(label = "tourPulse")
    val pulseValue by pulse.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(TourTokens.PulseMs, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "pulse",
    )

    val reveal = remember(state.index) { Animatable(0f) }
    LaunchedEffect(state.index) {
        reveal.snapTo(0f)
        reveal.animateTo(1f, tween(TourTokens.RevealMs))
    }
    val alpha = reveal.value

    var overlayOffset by remember { mutableStateOf(Offset.Zero) }

    BoxWithConstraints(
        modifier = modifier
            .fillMaxSize()
            .onGloballyPositioned { overlayOffset = it.positionInRoot() },
    ) {
        val targets: List<Pair<String, Rect>> = step.targetKeys.mapNotNull { key ->
            TourAnchors.get(key)?.let { rect ->
                key to rect.translate(-overlayOffset.x, -overlayOffset.y)
            }
        }
        val density = LocalDensity.current
        val widthPx = with(density) { maxWidth.toPx() }
        val heightPx = with(density) { maxHeight.toPx() }
        val breathPx = with(density) { (TourTokens.HoleInset + TourTokens.HoleBreath * (1f - alpha)).toPx() }
        val radiusPx = with(density) { TourTokens.HoleRadius.toPx() }

        // ---- 1. scrim with punched-out holes -------------------------------------------------
        Canvas(
            Modifier
                .fillMaxSize()
                .graphicsLayer { compositingStrategy = CompositingStrategy.Offscreen },
        ) {
            drawRect(TourTokens.Scrim.copy(alpha = TourTokens.ScrimAlpha * alpha))
            targets.forEach { (_, r) ->
                drawRoundRect(
                    color = Color.Black,
                    topLeft = Offset(r.left - breathPx, r.top - breathPx),
                    size = Size(r.width + breathPx * 2f, r.height + breathPx * 2f),
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    blendMode = BlendMode.Clear,
                )
            }
        }

        // ---- 2. outline + pulsing ring -------------------------------------------------------
        Canvas(Modifier.fillMaxSize()) {
            val ringPx = TourTokens.RingGrow.toPx() * pulseValue
            val strokePx = TourTokens.RingStroke.toPx()
            targets.forEach { (_, r) ->
                val hole = Rect(
                    r.left - breathPx,
                    r.top - breathPx,
                    r.right + breathPx,
                    r.bottom + breathPx,
                )
                drawRoundRect(
                    color = TourTokens.Accent2.copy(alpha = alpha * (1f - pulseValue) * 0.3f),
                    topLeft = Offset(hole.left - ringPx, hole.top - ringPx),
                    size = Size(hole.width + ringPx * 2f, hole.height + ringPx * 2f),
                    cornerRadius = CornerRadius(radiusPx + ringPx, radiusPx + ringPx),
                    style = Stroke(width = strokePx),
                )
                drawRoundRect(
                    color = Color.White.copy(alpha = alpha * 0.95f),
                    topLeft = hole.topLeft,
                    size = hole.size,
                    cornerRadius = CornerRadius(radiusPx, radiusPx),
                    style = Stroke(width = TourTokens.HoleBorder.toPx()),
                )
            }
        }

        // ---- 3. label chips ------------------------------------------------------------------
        if (step.labels.isNotEmpty()) {
            Box(Modifier.fillMaxSize()) {
                targets.forEach { (key, r) ->
                    val label = step.labels[key] ?: return@forEach
                    val onRightHalf = r.center.x > widthPx / 2f
                    Box(
                        Modifier
                            .align(if (onRightHalf) Alignment.TopEnd else Alignment.TopStart)
                            .offset {
                                val gutter = 12.dp.roundToPx()
                                val x = if (onRightHalf) {
                                    -(widthPx - r.left).roundToInt() - gutter
                                } else {
                                    r.right.roundToInt() + gutter
                                }
                                val y = r.center.y.roundToInt() - 14.dp.roundToPx()
                                IntOffset(x, y)
                            }
                            .graphicsLayer { this.alpha = alpha }
                            .clip(CircleShape)
                            .background(Color.White)
                            .padding(horizontal = 12.dp, vertical = 6.dp),
                    ) {
                        Text(
                            text = label,
                            color = TourTokens.Title,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                        )
                    }
                }
            }
        }

        // ---- 4. tap blocker ------------------------------------------------------------------
        Box(
            Modifier
                .fillMaxSize()
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                ) { /* swallow taps while the tour is up */ },
        )

        // ---- 5. card -------------------------------------------------------------------------
        TourCardHost(
            state = state,
            step = step,
            targets = targets,
            screenHeight = heightPx,
        )
    }
}

@Composable
private fun TourCardHost(
    state: TourState,
    step: TourStep,
    targets: List<Pair<String, Rect>>,
    screenHeight: Float,
) {
    val alignment: Alignment
    val offsetY: Int

    val density = LocalDensity.current
    val gutterPx = with(density) { 24.dp.roundToPx() }
    val cardMaxHeightPx = with(density) { 240.dp.roundToPx() }

    if (targets.isEmpty() || step.id == "controls") {
        alignment = Alignment.Center
        offsetY = 0
    } else {
        val centerAvg = targets.map { it.second.center.y }.average().toFloat()
        if (centerAvg > screenHeight / 2f) {
            alignment = Alignment.BottomCenter
            val bottomTargets = targets.filter { it.second.center.y > screenHeight / 2f }
            val targetTop = bottomTargets.minOfOrNull { it.second.top } ?: (screenHeight * 0.75f)
            val neededBottomMargin = (screenHeight - targetTop).roundToInt() + gutterPx
            offsetY = -neededBottomMargin.coerceIn(gutterPx, (screenHeight.roundToInt() - cardMaxHeightPx - gutterPx))
        } else {
            alignment = Alignment.TopCenter
            val topTargets = targets.filter { it.second.center.y <= screenHeight / 2f }
            val targetBottom = topTargets.maxOfOrNull { it.second.bottom } ?: (screenHeight * 0.25f)
            val neededTopMargin = targetBottom.roundToInt() + gutterPx
            offsetY = neededTopMargin.coerceIn(gutterPx, (screenHeight.roundToInt() - cardMaxHeightPx - gutterPx))
        }
    }

    Box(
        Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp),
        contentAlignment = alignment,
    ) {
        AnimatedContent(
            targetState = state.index,
            transitionSpec = {
                (slideInVertically(tween(320, delayMillis = 120)) { it / 6 } + fadeIn(tween(380)))
                    .togetherWith(fadeOut(tween(140)))
            },
            label = "tourCard",
            modifier = Modifier.offset { IntOffset(0, offsetY) },
        ) { _ ->
            TourCard(
                step = step,
                index = state.index,
                count = state.count,
                isLast = state.isLast,
                onNext = state::next,
                onSkip = state::skip,
            )
        }
    }
}

@Composable
private fun TourCard(
    step: TourStep,
    index: Int,
    count: Int,
    isLast: Boolean,
    onNext: () -> Unit,
    onSkip: () -> Unit,
) {
    Column(
        Modifier
            .widthIn(max = 420.dp)
            .shadow(18.dp, RoundedCornerShape(22.dp))
            .background(TourTokens.Card, RoundedCornerShape(22.dp))
            .padding(start = 20.dp, top = 18.dp, end = 16.dp, bottom = 14.dp),
    ) {
        Text(
            text = step.title,
            color = TourTokens.Title,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(4.dp))
        Text(
            text = step.description,
            color = TourTokens.Body,
            fontSize = 14.sp,
            lineHeight = 20.sp,
        )
        Spacer(Modifier.height(16.dp))

        Row(
            Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp),
            ) {
                repeat(count) { i ->
                    val active = i == index
                    Box(
                        Modifier
                            .height(6.dp)
                            .width(if (active) 18.dp else 6.dp)
                            .clip(CircleShape)
                            .background(if (active) TourTokens.Accent else TourTokens.DotIdle),
                    )
                }
            }

            Spacer(Modifier.weight(1f))

            if (!isLast) {
                Text(
                    text = "Skip",
                    color = TourTokens.Body,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .clickable(
                            interactionSource = remember { MutableInteractionSource() },
                            indication = null,
                            onClick = onSkip,
                        )
                        .padding(horizontal = 12.dp, vertical = 10.dp),
                )
            }

            Box(
                Modifier
                    .padding(start = 6.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        GraphicsBrush.linearGradient(
                            listOf(TourTokens.Accent, TourTokens.Accent2),
                        ),
                    )
                    .clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onNext,
                    )
                    .padding(horizontal = 18.dp, vertical = 10.dp),
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (isLast) "Start drawing" else "Next",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Medium,
                    )
                    if (!isLast) {
                        Spacer(Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                            contentDescription = null,
                            tint = Color.White,
                            modifier = Modifier.size(16.dp),
                        )
                    }
                }
            }
        }
    }
}

/* ----------------------------------------------------------------------------------------------
 * 7. Tool guide ("what is what") — the second onboarding surface
 * -------------------------------------------------------------------------------------------- */

data class ToolGuideEntry(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
)

data class ToolGuideGroup(
    val title: String,
    val entries: List<ToolGuideEntry>,
)

data class ToolHelpEntry(
    val name: String,
    val description: String,
)

object TourIcons {
    val Pen = Icons.Outlined.Edit
    val Brush = Icons.Outlined.Brush
    val Marker = Icons.Outlined.Draw
    val Highlighter = Icons.Outlined.Highlight
    val Calligraphy = Icons.Outlined.Gesture
    val Shapes = Icons.Outlined.Category
    val Text = Icons.Outlined.TextFields
    val Formula = Icons.Outlined.Functions
    val Laser = Icons.Outlined.NearMe
    val Geometry = Icons.Outlined.Architecture
    val Video = Icons.Outlined.PlayCircle
    val Calculator = Icons.Outlined.Calculate
    val Timer = Icons.Outlined.Timer
    val Record = Icons.Outlined.Videocam
    val Lasso = Icons.Outlined.FormatShapes
    val Hand = Icons.Outlined.PanTool
    val Eraser = Icons.Outlined.AutoFixOff
    val BoardColor = Icons.Outlined.FormatColorFill
    val Table = Icons.Outlined.TableChart
    val Layers = Icons.Outlined.Layers
    val ImportImage = Icons.Outlined.Image
    val WebImage = Icons.Outlined.Language
    val Camera = Icons.Outlined.PhotoCamera
    val Ocr = Icons.Outlined.DocumentScanner
    val Pdf = Icons.Outlined.PictureAsPdf
    val Stickers = Icons.Outlined.EmojiEmotions
    val Science = Icons.Outlined.Science
    val Customize = Icons.Outlined.Tune
    val Save = Icons.Outlined.Save
    val Share = Icons.Outlined.Share
    val Undo = Icons.Outlined.Undo
    val Redo = Icons.Outlined.Redo
    val Clear = Icons.Outlined.DeleteSweep
}

object ToolGuideCatalog {

    val groups: List<ToolGuideGroup> = listOf(
        ToolGuideGroup(
            title = "Drawing Tools",
            entries = listOf(
                ToolGuideEntry("pen", "Pen", "Draw smooth and precise lines on the board", TourIcons.Pen),
                ToolGuideEntry("brush", "Brush", "Paint with soft, natural brush strokes", TourIcons.Brush),
                ToolGuideEntry("marker", "Marker", "Draw bold and thick strokes like a real marker", TourIcons.Marker),
                ToolGuideEntry("highlighter", "Highlighter", "Highlight text or areas with semi-transparent color", TourIcons.Highlighter),
                ToolGuideEntry("calligraphy", "Calligraphy", "Artistic lettering with varying stroke width", TourIcons.Calligraphy),
            ),
        ),
        ToolGuideGroup(
            title = "Main Tools",
            entries = listOf(
                ToolGuideEntry("shapes", "Shapes", "Draw geometric shapes — rectangles, circles, triangles and more", TourIcons.Shapes),
                ToolGuideEntry("text", "Text", "Add typed text to the board with custom color and size", TourIcons.Text),
                ToolGuideEntry("formula", "Formula", "Write mathematical formulas and equations", TourIcons.Formula),
                ToolGuideEntry("laser", "Laser Pointer", "Point at areas temporarily — lines fade automatically", TourIcons.Laser),
                ToolGuideEntry("geometry", "Geometry Box", "Interactive geometry set: ruler, protractor, compass and set square", TourIcons.Geometry),
                ToolGuideEntry("video", "Video", "Play a video from your gallery in a floating window — drag, resize or minimize it (pauses) while you draw", TourIcons.Video),
                ToolGuideEntry("calculator", "Calculator", "Floating basic and scientific calculator — drag, resize or minimize it while you draw", TourIcons.Calculator),
                ToolGuideEntry("timer", "Timer", "Countdown from 5 to 60 seconds for quick answers — tap a number to start; a bell rings and the panel turns red when time is up", TourIcons.Timer),
                ToolGuideEntry("record", "Record", "Record your lesson as a video with your voice. Unlimited and saved to your gallery", TourIcons.Record),
                ToolGuideEntry("lasso", "Lasso Select", "Freehand selection to move or resize objects", TourIcons.Lasso),
                ToolGuideEntry("hand", "Hand / Pan", "Move around canvas without drawing. Pinch to zoom", TourIcons.Hand),
                ToolGuideEntry("eraser", "Eraser", "Remove drawings from the canvas. Adjust size for precision", TourIcons.Eraser),
            ),
        ),
        ToolGuideGroup(
            title = "Canvas & Media",
            entries = listOf(
                ToolGuideEntry("board_color", "Board Color", "Change background color or add textures", TourIcons.BoardColor),
                ToolGuideEntry("table", "Table", "Insert a table with custom rows and columns onto the board", TourIcons.Table),
                ToolGuideEntry("layers", "Layers", "Manage multiple layers — show/hide them", TourIcons.Layers),
                ToolGuideEntry("import_image", "Import Image", "Add a photo from gallery onto the board", TourIcons.ImportImage),
                ToolGuideEntry("web_image", "Web Image", "Search and add images from the web directly to the board", TourIcons.WebImage),
                ToolGuideEntry("camera", "Camera", "Take a photo and add it to the board", TourIcons.Camera),
                ToolGuideEntry("ocr", "OCR Scanner", "Scan handwriting or printed text from photos using AI and add it to your board", TourIcons.Ocr),
                ToolGuideEntry("import_pdf", "Import PDF", "Open a PDF file to view and annotate on the board", TourIcons.Pdf),
                ToolGuideEntry("stickers", "Stickers", "Add fun emoji stickers to your board", TourIcons.Stickers),
                ToolGuideEntry("science", "Science", "Add science diagrams and symbols", TourIcons.Science),
                ToolGuideEntry("customize", "Customize", "Rearrange, show or hide tools to personalize your toolbar", TourIcons.Customize),
            ),
        ),
        ToolGuideGroup(
            title = "Actions",
            entries = listOf(
                ToolGuideEntry("save", "Save", "Save the page as an image, or pages as a PDF", TourIcons.Save),
                ToolGuideEntry("share", "Share", "Share the page as an image, or pages as a PDF", TourIcons.Share),
                ToolGuideEntry("undo", "Undo", "Reverse your last action", TourIcons.Undo),
                ToolGuideEntry("redo", "Redo", "Bring back an action you undid", TourIcons.Redo),
                ToolGuideEntry("clear", "Clear", "Remove everything from the board", TourIcons.Clear),
            ),
        ),
    )

    fun all(): List<ToolGuideEntry> = groups.flatMap { it.entries }
    fun byId(id: String): ToolGuideEntry? = all().firstOrNull { it.id == id }
}

object ToolHelpCatalog {

    val entries: Map<String, ToolHelpEntry> = linkedMapOf(
        "Pen" to ToolHelpEntry("Pen", "Draw smooth and precise lines on the board"),
        "Brush" to ToolHelpEntry("Brush", "Paint with soft, natural brush strokes"),
        "Marker" to ToolHelpEntry("Marker", "Draw bold and thick strokes like a real marker"),
        "Highlighter" to ToolHelpEntry("Highlighter", "Highlight text or areas with semi-transparent color"),
        "Calligraphy" to ToolHelpEntry("Calligraphy", "Write with varying stroke width for artistic lettering"),
        "Callig." to ToolHelpEntry("Calligraphy", "Write with varying stroke width for artistic lettering"),
        "Highlight" to ToolHelpEntry("Highlighter", "Highlight text or areas with semi-transparent color"),
        "Brushes" to ToolHelpEntry("Brushes", "Choose from Pen, Brush, Marker, Highlighter or Calligraphy"),
        "Eraser" to ToolHelpEntry("Eraser", "Remove drawings from the canvas. Adjust size for precision"),
        "Shapes" to ToolHelpEntry("Shapes", "Draw perfect geometric shapes — rectangles, circles, triangles and more"),
        "Text" to ToolHelpEntry("Text", "Add typed text to the board with custom color and size"),
        "Formula" to ToolHelpEntry("Formula", "Write mathematical formulas and equations on the board"),
        "Laser" to ToolHelpEntry("Laser Pointer", "Point at areas temporarily — lines fade away automatically"),
        "Ruler" to ToolHelpEntry("Ruler", "Draw perfectly straight lines between two points"),
        "Geometry" to ToolHelpEntry("Geometry Box", "Open the geometry set: interactive ruler, protractor, compass and set square"),
        "Video" to ToolHelpEntry("Video", "Play a video from your gallery in a floating window. Drag it, resize it or minimize it (it pauses) while you draw"),
        "Calculator" to ToolHelpEntry("Calculator", "Open a floating calculator — basic or scientific. Drag it, resize it or minimize it while you draw; long-press the result to copy"),
        "Timer" to ToolHelpEntry("Timer", "Open a countdown timer (5 to 60 seconds) for quick answers. Tap a number to start; a bell rings when time is up. Drag, resize or minimize it while you draw"),
        "Record" to ToolHelpEntry("Record", "Record your lesson as a video with your voice: the board, Timer, Calculator and Video window. Unlimited and saved to your gallery"),
        "Lasso" to ToolHelpEntry("Lasso Select", "Draw a freehand selection around objects to move or resize them"),
        "Smart" to ToolHelpEntry("Smart", "Write by hand and it is turned into clean typed text"),
        "OCR" to ToolHelpEntry("OCR Text Scanner", "Scan handwriting or printed text from photos using AI and add it to your board"),
        "Hand/Pan" to ToolHelpEntry("Hand / Pan", "Move around the canvas without drawing. Pinch to zoom"),
        "Save" to ToolHelpEntry("Save", "Save the page as an image in your Gallery, or pages as a PDF in Downloads"),
        "Share" to ToolHelpEntry("Share", "Share the page on the screen as an image, or pages as a PDF"),
        "Import PDF" to ToolHelpEntry("Import PDF", "Open a PDF file to view and annotate on the board"),
        "Undo" to ToolHelpEntry("Undo", "Reverse your last action on the board"),
        "Redo" to ToolHelpEntry("Redo", "Bring back an action that you undid"),
        "Clear" to ToolHelpEntry("Clear Canvas", "Remove everything from the current board"),
        "Board" to ToolHelpEntry("Board Color", "Change background color or add textures like grid, dots, lines"),
        "Layers" to ToolHelpEntry("Layers", "Manage multiple layers — draw on separate layers and show/hide them"),
        "Image" to ToolHelpEntry("Import Image", "Add a photo or image from your gallery onto the board"),
        "Camera" to ToolHelpEntry("Camera", "Take a photo with your camera and add it to the board"),
        "Stickers" to ToolHelpEntry("Stickers", "Add fun emoji stickers to your board"),
        "Science" to ToolHelpEntry("Science Stickers", "Add science diagrams — circuits, lab equipment, biology symbols"),
        "Collapse" to ToolHelpEntry("Collapse Toolbar", "Hide the toolbar to get more drawing space"),
        "Customize" to ToolHelpEntry("Customize Toolbar", "Rearrange, show or hide tools to personalize your toolbar"),
        "Help" to ToolHelpEntry("Help Mode", "Tap any tool to learn what it does. Tap again to exit help mode"),
        "Back" to ToolHelpEntry("Go Back", "Return to the home screen. Your board is auto-saved"),
        "Stylus" to ToolHelpEntry("Palm Rejection / Stylus Mode", "When ON, only stylus input draws — touch is ignored to prevent accidental marks"),
        "Pages" to ToolHelpEntry("Pages", "Navigate between board pages. Tap to expand page controls"),
    )

    fun get(label: String): ToolHelpEntry? = entries[label]
}

const val TOOL_GUIDE_TIP = "Tip: Long press any tool for a quick description"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolGuideSheet(
    onDismiss: () -> Unit,
    onReplayTour: (() -> Unit)? = null,
    groups: List<ToolGuideGroup> = ToolGuideCatalog.groups,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TourTokens.Sheet,
        dragHandle = {
            Box(
                Modifier
                    .padding(vertical = 12.dp)
                    .size(width = 36.dp, height = 4.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.25f)),
            )
        },
    ) {
        Column(
            Modifier
                .fillMaxWidth(0.92f)
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
        ) {
            GuideHeader(onDismiss)

            Column(
                Modifier
                    .fillMaxWidth()
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 14.dp, vertical = 12.dp),
            ) {
                if (onReplayTour != null) {
                    ReplayTourRow(onReplayTour)
                }
                groups.forEach { group ->
                    ToolGuideGroupBlock(group)
                }
                GuideHintStrip()
            }
        }
    }
}

@Composable
private fun GuideHeader(onDismiss: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(
                RoundedCornerShape(
                    topStart = 20.dp,
                    topEnd = 20.dp,
                    bottomEnd = 0.dp,
                    bottomStart = 12.dp,
                ),
            )
            .background(TourTokens.SheetAccent)
            .padding(horizontal = 20.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.MenuBook,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(22.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Tool guide",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Close",
                tint = Color.White,
                modifier = Modifier.size(20.dp),
            )
        }
    }
}

@Composable
private fun ReplayTourRow(onReplay: () -> Unit) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TourTokens.SheetAccent.copy(alpha = 0.6f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onReplay,
            )
            .padding(horizontal = 14.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Refresh,
            contentDescription = null,
            tint = Color.White,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = "Replay the quick tour",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
        )
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun ToolGuideGroupBlock(group: ToolGuideGroup) {
    Text(
        text = group.title,
        color = TourTokens.SheetAccent,
        fontSize = 14.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 10.dp, bottom = 8.dp),
    )
    Column(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(Color.White.copy(alpha = 0.04f)),
    ) {
        group.entries.forEachIndexed { i, entry ->
            ToolGuideRow(entry)
            if (i < group.entries.lastIndex) {
                HorizontalDivider(
                    thickness = 1.dp,
                    color = Color.White.copy(alpha = 0.06f),
                    modifier = Modifier.padding(start = 12.dp),
                )
            }
        }
    }
    Spacer(Modifier.height(12.dp))
}

@Composable
private fun ToolGuideRow(entry: ToolGuideEntry) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(Color.White.copy(alpha = 0.08f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = entry.icon,
                contentDescription = null,
                tint = Color.White.copy(alpha = 0.9f),
                modifier = Modifier.size(20.dp),
            )
        }
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(
                text = entry.name,
                color = Color.White,
                fontSize = 15.sp,
                fontWeight = FontWeight.Medium,
            )
            Text(
                text = entry.description,
                color = Color.White.copy(alpha = 0.6f),
                fontSize = 13.sp,
                lineHeight = 17.sp,
                textAlign = TextAlign.Start,
            )
        }
    }
}

@Composable
private fun GuideHintStrip() {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TourTokens.SheetAccent.copy(alpha = 0.12f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = Icons.Outlined.Info,
            contentDescription = null,
            tint = TourTokens.SheetAccent,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(10.dp))
        Text(
            text = TOOL_GUIDE_TIP,
            color = Color.White.copy(alpha = 0.7f),
            fontSize = 13.sp,
            lineHeight = 17.sp,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ToolHelpSheet(
    toolLabel: String,
    onDismiss: () -> Unit,
    entries: Map<String, ToolHelpEntry> = ToolHelpCatalog.entries,
) {
    val entry = entries[toolLabel] ?: return
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = TourTokens.Sheet,
    ) {
        Column(
            Modifier
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, top = 8.dp, bottom = 32.dp),
        ) {
            Text(
                text = entry.name,
                color = Color.White,
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(6.dp))
            Text(
                text = entry.description,
                color = Color.White.copy(alpha = 0.7f),
                fontSize = 14.sp,
                lineHeight = 20.sp,
            )
        }
    }
}

/* ----------------------------------------------------------------------------------------------
 * 8. Wiring host
 * -------------------------------------------------------------------------------------------- */

@Composable
fun TourHost(
    state: TourState,
    guideOpen: Boolean = false,
    onGuideDismiss: () -> Unit = {},
    onReplayTour: (() -> Unit)? = null,
    helpLabel: String? = null,
    onHelpDismiss: () -> Unit = {},
) {
    if (state.visible) {
        TourOverlay(state = state)
    }
    if (guideOpen) {
        ToolGuideSheet(onDismiss = onGuideDismiss, onReplayTour = onReplayTour)
    }
    if (helpLabel != null) {
        ToolHelpSheet(toolLabel = helpLabel, onDismiss = onHelpDismiss)
    }
}

/* ----------------------------------------------------------------------------------------------
 * 9. Default shipped steps
 * -------------------------------------------------------------------------------------------- */

fun defaultTourSteps(): List<TourStep> = listOf(
    TourStep(
        id = "controls",
        title = "Quick controls",
        description = "Pages, shortcuts and stylus mode live up here.",
        highlight = TourHighlight.TOP_CONTROLS,
        toolIndex = 0,
        targetKeys = listOf("pages", "shortcuts", "stylus"),
        labels = mapOf(
            "pages" to "Pages",
            "shortcuts" to "Shortcuts",
            "stylus" to "Stylus mode",
        ),
    ),
    TourStep(
        id = "toolbar",
        title = "25+ tools, one swipe away",
        description = "Slide the bar to find every tool.",
        highlight = TourHighlight.TOOLBAR,
        toolIndex = -2,
        targetKeys = listOf("toolbar"),
    ),
)
