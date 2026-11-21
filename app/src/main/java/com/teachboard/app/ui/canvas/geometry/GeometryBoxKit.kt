package com.teachboard.app.ui.canvas.geometry

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import java.util.Locale
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.asin
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

// -----------------------------------------------------------------------------
// Public model
// -----------------------------------------------------------------------------

/** Minimal point type so the kit has zero dependencies. */
data class P(val x: Float, val y: Float) {
    operator fun plus(o: P) = P(x + o.x, y + o.y)
    operator fun minus(o: P) = P(x - o.x, y - o.y)
    operator fun times(k: Float) = P(x * k, y * k)
    fun len() = hypot(x, y)
    fun unit(): P { val l = len().coerceAtLeast(1e-4f); return P(x / l, y / l) }
}

enum class GeoInstrument(val id: Int, val label: String) {
    Ruler(10, "Ruler"),
    Protractor(11, "Protractor"),
    Compass(12, "Compass"),
    SetSquare(13, "Set square");

    companion object {
        /** Tool ids as used by the reference app (10–13). */
        fun fromId(id: Int) = entries.firstOrNull { it.id == id }
    }
}

enum class GeoStampKind { Angle, Arc, Sector }

/**
 * Live instrument state. Only the fields relevant to [instrument] are used.
 *
 *  Ruler        ox,oy = left-edge centre · angle · spanCm · uiScale
 *  Protractor   ox,oy = flat-baseline centre · angle · radius · armDeg
 *  Compass      ox,oy = needle · hx,hy = hinge · px,py = pencil · legPx = legLen
 *  SetSquare    ox,oy = right-angle vertex · angle · legCm · uiScale
 */
data class GeoState(
    val instrument: GeoInstrument,
    val ox: Float,
    val oy: Float,
    val angle: Float = 0f,
    val uiScale: Float = 1f,
    val snapped: Boolean = false,
    val snapPulse: Int = 0,
    val spanCm: Float = 12.7f,
    val radius: Float = 240f,
    val armDeg: Float = 55f,
    val legPx: Float = 200f,
    val opening: Float = 1.10f,
    val legCm: Int = 6,
    val hint: String? = null,
    val sweep: Pair<Float, Float>? = null,
    val activeEdgeLine: Pair<P, P>? = null,
    val hx: Float = 0f,
    val hy: Float = 0f,
    val px: Float = 0f,
    val py: Float = 0f,
    val pxPerCm: Float = 37.795277f,
    val sweepFromDeg: Float? = null,
    val sweepArcDeg: Float = 0f,
) {
    val angleDeg: Float get() = Math.toDegrees(angle.toDouble()).toFloat()
    val g: Float get() = legPx / 160f
    val compassNeedle: P get() = P(ox, oy)
    val compassHinge: P get() = P(hx, hy)
    val compassPencil: P get() = P(px, py)
    val compassRadius: Float get() = hypot(px - ox, py - oy)
    val moveHandle: P get() = compassHinge + (compassHinge - compassNeedle).unit() * (45.8f * g)
    val closeButton: P get() = P(hx + 36f * g, hy + 28f * g)

    fun compassReadoutText(): String {
        val r = compassRadius / pxPerCm.coerceAtLeast(1e-4f)
        return String.format(Locale.US, "r %.1f cm  ·  d %.1f cm", r, r * 2f)
    }

    val setSquareLegPx: Float get() = legCm * 37.795277f
    val setSquareInradius: Float get() = setSquareLegPx / 3.4142137f
    val setSquareNotch: Float get() = 0.54f * setSquareInradius

    fun setSquareToBoard(lx: Float, ly: Float): P {
        val c = cos(angle); val s = sin(angle)
        val x = lx * uiScale; val y = ly * uiScale
        return P(ox + x * c - y * s, oy + y * c + x * s)
    }

    fun setSquareToLocal(p: P): P {
        val c = cos(angle); val s = sin(angle)
        val dx = p.x - ox; val dy = p.y - oy
        val k = uiScale.coerceAtLeast(1e-4f)
        return P((dy * s + dx * c) / k, (dy * c - dx * s) / k)
    }

    fun setSquareToBoard(p: P): P = setSquareToBoard(p.x, p.y)

    fun setSquareCentroid(): P = setSquareToBoard(setSquareLegPx / 3f, -setSquareLegPx / 3f)

    fun setSquareCloseButton(): P {
        val i = GeoKit.clamp(setSquareNotch * 0.5f, 12f, 22f) * 0.70710677f
        return setSquareToBoard(setSquareLegPx / 2f - i, -setSquareLegPx / 2f + i)
    }

    fun setSquareRotateButton(): P = setSquareToBoard(0f, -setSquareLegPx - 26f)
    fun setSquareResizeButton(): P = setSquareToBoard(setSquareLegPx + 28f, 0f)
}

/** Colours and sizes. Defaults reproduce the reference app. */
data class GeoStyle(
    val tan: Int = 0xFFFFCE54.toInt(),
    val brown: Int = 0xFF6E4D0A.toInt(),
    val dark: Int = 0xFF5A4408.toInt(),
    val ink: Int = 0xFF3A2F10.toInt(),
    val accent: Int = 0xFFB45309.toInt(),
    val legBlue: Int = 0xFF2563EB.toInt(),
    val closeRed: Int = 0xFFEF4444.toInt(),
    val angleTeal: Int = 0xFF0F766E.toInt(),
    val arcViolet: Int = 0xFF7C3AED.toInt(),
    val sectorAmber: Int = 0xFFB45309.toInt(),
    val zoomYellow: Int = 0xFFCA8A04.toInt(),
    val resizePink: Int = 0xFFDB2777.toInt(),
    val rotateBlue: Int = 0xFF2563EB.toInt(),
    val compassBody: Int = 0xFF3F4654.toInt(),
    val compassAccent: Int = 0xFF1D4ED8.toInt(),
    val bodyAlpha: Float = 0.45f,
)

/** Readout formats — same keys as the reference `board_*_readout` strings. */
data class GeoStrings(
    val rulerSpan: String = "%1\$.1f cm  ·  %2\$.1f in%3\$s",
    val rulerMax: String = "  MAX",
    val rulerDraw: String = "%1\$.1f cm  ·  %2\$.2f in",
    val setSquare: String = "leg %1\$d cm  ·  hyp %2\$.1f cm%3\$s",
    val compass: String = "r %1\$.1f cm  ·  d %2\$.1f cm",
)

interface GeoCallbacks {
    fun onDismiss()
    fun onStampLine(a: P, b: P)
    fun onStampArc(needle: P, start: P, end: P)
    fun onStampArc(needle: P, start: P, end: P, sweepDeg: Float, strokeWidth: Float) {
        onStampArc(needle, start, end)
    }
    fun onStampAngle(pivot: P, armEnd: P, baseEnd: P, kind: GeoStampKind)
}

fun clampD(v: Double, lo: Double, hi: Double) = if (v < lo) lo else if (v > hi) hi else v

fun bearing(a: P, b: P) =
    Math.toDegrees(atan2((b.y - a.y).toDouble(), (b.x - a.x).toDouble())).toFloat()

fun rotateAbout(p: P, deg: Float, pivot: P): P {
    val r = Math.toRadians(deg.toDouble())
    val c = cos(r).toFloat(); val s = sin(r).toFloat()
    val dx = p.x - pivot.x; val dy = p.y - pivot.y
    return P(pivot.x + dx * c - dy * s, pivot.y + dy * c + dx * s)
}

fun wrapDeg(d: Float): Float {
    var v = d
    while (v > 180f) v -= 360f
    while (v < -180f) v += 360f
    return v
}

fun wrap360(deg: Float): Float {
    var d = deg % 360f
    if (d < 0f) d += 360f
    return d
}

fun inTriangle(local: P, leg: Float, tol: Float = 1.5f): Boolean =
    local.x >= -tol && local.y <= tol && local.x - local.y <= leg + tol

fun edgeLength(leg: Float, edge: Int): Float = if (edge == 2) leg * 1.4142135f else leg

fun edgePoint(leg: Float, edge: Int, t: Float): P = when (edge) {
    0 -> P(t, 0f)
    1 -> P(0f, -t)
    else -> P(leg - t / 1.4142135f, -t / 1.4142135f)
}

fun projectOnEdge(leg: Float, edge: Int, p: P): Float = when (edge) {
    0 -> GeoKit.clamp(p.x, 0f, leg)
    1 -> GeoKit.clamp(-p.y, 0f, leg)
    else -> GeoKit.clamp((leg - p.x - p.y) / 1.4142135f, 0f, leg * 1.4142135f)
}

fun distToEdge(leg: Float, edge: Int, p: P): Float {
    val t = projectOnEdge(leg, edge, p)
    val q = edgePoint(leg, edge, t)
    return hypot(p.x - q.x, p.y - q.y)
}

fun nearestSetSquareEdge(leg: Float, p: P): Int {
    val d0 = distToEdge(leg, 0, p)
    val d1 = distToEdge(leg, 1, p)
    val d2 = distToEdge(leg, 2, p)
    return if (d0 <= d1 && d0 <= d2) 0 else if (d1 <= d2) 1 else 2
}

fun offsetOutside(leg: Float, edge: Int, t: Float, d: Float): P = when (edge) {
    0 -> P(t, d)
    1 -> P(-d, -t)
    else -> P(leg + d / 1.4142135f - t / 1.4142135f, -d / 1.4142135f - t / 1.4142135f)
}

// -----------------------------------------------------------------------------
// Maths — every number here is from the original
// -----------------------------------------------------------------------------

object GeoKit {
    const val PX_PER_CM = 37.795277f
    const val MM = 3.7795277f
    const val RULER_HEAD = 50f
    const val RULER_TAIL = 36f
    const val MIN_SPAN = 4f
    const val MAX_SPAN = 25.4f

    fun clamp(v: Float, lo: Float, hi: Float) = max(lo, min(hi, v))

    fun rulerLen(s: GeoState) = s.spanCm * PX_PER_CM + RULER_HEAD + RULER_TAIL
    fun setSquareLeg(s: GeoState) = s.legCm * PX_PER_CM
    fun protractorScale(s: GeoState) = s.radius / 160f * s.uiScale

    fun world(s: GeoState, local: P): P {
        val r = rotate(local.x * s.uiScale, local.y * s.uiScale, s.angle)
        return P(s.ox + r.x, s.oy + r.y)
    }

    fun rotate(x: Float, y: Float, rad: Float): P {
        val c = cos(rad); val n = sin(rad)
        return P(x * c - y * n, x * n + y * c)
    }

    fun compassHinge(s: GeoState) = if (s.hx != 0f || s.hy != 0f) P(s.hx, s.hy) else P(s.ox + s.legPx * cos(s.angle), s.oy + s.legPx * sin(s.angle))

    /** Hinge + leg rotated by the apex angle away from the needle leg. */
    fun compassPencil(s: GeoState): P {
        if (s.px != 0f || s.py != 0f) return P(s.px, s.py)
        val h = compassHinge(s)
        val dx = -cos(s.angle); val dy = -sin(s.angle)
        val c = cos(s.opening); val n = sin(s.opening)
        return P(h.x + s.legPx * (dx * c + dy * n), h.y + s.legPx * (-dx * n + dy * c))
    }

    fun compassRadius(s: GeoState): Float {
        val p = compassPencil(s); return hypot(p.x - s.ox, p.y - s.oy)
    }

    /** 15° grid, ±3.5° (±5.6° once snapped). Returns (degrees, snappedNow). */
    fun snapRotation(deg: Float, wasSnapped: Boolean): Pair<Float, Boolean> {
        val tol = if (wasSnapped) 5.6f else 3.5f
        val nearest = (deg / 15f).roundToInt() * 15f
        return if (abs(deg - nearest) <= tol) nearest to true else deg to false
    }

    /** Activation defaults — mirrors the reference `Y0()` init block. */
    fun initial(inst: GeoInstrument, w: Float, h: Float, zoom: Float, panX: Float = 0f, panY: Float = 0f, strings: GeoStrings): GeoState {
        val z = zoom.coerceAtLeast(0.01f)
        val mn = min(w, h)
        val cx = (w / 2f - panX) / z
        val cy = (h / 2f - panY) / z
        return when (inst) {
            GeoInstrument.Ruler -> {
                val scale = clamp((mn * 0.88f) / z / (PX_PER_CM * 12.7f + RULER_HEAD + RULER_TAIL), 0.5f, 8f)
                val total = (PX_PER_CM * 12.7f + RULER_HEAD + RULER_TAIL) * scale
                GeoState(inst, cx - total / 2f, cy, uiScale = scale).let {
                    it.copy(hint = rulerReadout(it, strings))
                }
            }
            GeoInstrument.Protractor -> {
                val r = clamp(mn * 0.4f, 120f * z, 320f * z) / z
                GeoState(inst, cx, cy + 0.42f * r, radius = r, armDeg = 55f)
            }
            GeoInstrument.Compass -> {
                val leg = clamp(0.34f * mn, 330f, 632.5f) / z
                val chord = min(max(0.26f * mn, 66f) / z, 1.88f * leg)
                val needle = P(cx, cy)
                val hinge = P(cx, cy - leg)
                val apex = 2.0 * asin(clampD((chord / (2f * leg)).toDouble(), 0.0, 0.985))
                val dir = Math.PI / 2.0 - apex
                val pencil = P(
                    hinge.x + (cos(dir) * leg).toFloat(),
                    hinge.y + (sin(dir) * leg).toFloat()
                )
                val pxPerCm = 37.795277f * clamp(((mn * 0.88f) / z) / 566f, 0.5f, 8f)
                GeoState(
                    instrument = inst,
                    ox = needle.x,
                    oy = needle.y,
                    hx = hinge.x,
                    hy = hinge.y,
                    px = pencil.x,
                    py = pencil.y,
                    legPx = leg,
                    pxPerCm = pxPerCm,
                    snapPulse = 0,
                    hint = null,
                    sweepFromDeg = null,
                    sweepArcDeg = 0f
                )
            }
            GeoInstrument.SetSquare -> {
                val ui = clamp(((mn * 0.88f) / z) / 566f, 0.5f, 8f)
                val ppc = 37.795277f * ui
                GeoState(
                    instrument = inst,
                    ox = cx - 2f * ppc,
                    oy = cy + 2f * ppc,
                    angle = 0f,
                    uiScale = ui,
                    legCm = 6,
                    snapPulse = 0,
                    hint = null
                )
            }
        }
    }

    fun rulerReadout(s: GeoState, str: GeoStrings): String {
        val suffix = if (s.spanCm >= MAX_SPAN - 0.01f) str.rulerMax else ""
        return String.format(Locale.US, str.rulerSpan, s.spanCm, s.spanCm / 2.54f, suffix)
    }

    fun setSquareReadout(s: GeoState, str: GeoStrings): String =
        String.format(Locale.US, str.setSquare, s.legCm, s.legCm * 1.4142135f, if (s.snapped) "  ✓" else "")

    fun compassReadout(s: GeoState, str: GeoStrings): String {
        val r = compassRadius(s) / PX_PER_CM
        return String.format(Locale.US, str.compass, r, r * 2f)
    }

    /**
     * Protractor stamp points. `armDeg` is relative to the instrument body, so the
     * world angle is armDeg + body rotation.
     */
    fun protractorStamp(s: GeoState): Triple<P, P, P> {
        val armRad = Math.toRadians(s.armDeg.toDouble()).toFloat()
        val arm = s.radius * 1.02f
        val baseLen = s.radius * 0.92f
        // Local coordinates: base is (baseLen, 0), arm is (cos(armRad)*arm, -sin(armRad)*arm) because -y is up into dome
        val rArm = rotate(cos(armRad) * arm, -sin(armRad) * arm, s.angle)
        val rBase = rotate(baseLen, 0f, s.angle)
        val pivot = P(s.ox, s.oy)
        return Triple(
            pivot,
            P(s.ox + rArm.x, s.oy + rArm.y),
            P(s.ox + rBase.x, s.oy + rBase.y),
        )
    }
}

// -----------------------------------------------------------------------------
// Controller — state + gestures + rendering
// -----------------------------------------------------------------------------

class GeoController(
    private val cb: GeoCallbacks,
    private val style: GeoStyle = GeoStyle(),
    private val strings: GeoStrings = GeoStrings(),
) {
    var state: GeoState? = null
        private set

    val active: Boolean get() = state != null

    private var gesture: G = G.None

    // ---------------- lifecycle ----------------

    fun open(inst: GeoInstrument, viewportW: Float, viewportH: Float, zoom: Float, panX: Float = 0f, panY: Float = 0f) {
        state = GeoKit.initial(inst, viewportW, viewportH, zoom, panX, panY, strings)
        gesture = G.None
    }

    fun close() {
        state = null
        gesture = G.None
    }

    var brushWidth: Float = 4f
    var brushColor: Int = 0xFF000000.toInt()

    fun cancelGesture() {
        if (gesture !== G.None) {
            gesture = G.None
            val s = state ?: return
            val defaultHint = when (s.instrument) {
                GeoInstrument.Ruler -> GeoKit.rulerReadout(s, strings)
                else -> null
            }
            state = s.copy(hint = defaultHint, activeEdgeLine = null)
        }
    }

    fun draw(c: Canvas) {
        val s = state ?: return
        GeoRenderer(style, strings, brushColor, brushWidth).draw(c, s)
    }

    // ---------------- input ----------------

    fun pad(s: GeoState, extra: Float, zoom: Float): Float =
        10f / zoom.coerceAtLeast(0.01f) + s.g * extra

    fun padSquare(s: GeoState, extra: Float, zoom: Float): Float =
        9f / zoom.coerceAtLeast(0.01f) + extra * s.uiScale

    fun contains(x: Float, y: Float, zoom: Float): Boolean {
        val s = state ?: return false
        val p = P(x, y)
        return when (s.instrument) {
            GeoInstrument.SetSquare -> {
                val local = s.setSquareToLocal(p)
                val leg = s.setSquareLegPx
                val dist = minOf(
                    distToEdge(leg, 0, local),
                    distToEdge(leg, 1, local),
                    distToEdge(leg, 2, local)
                )
                (p - s.setSquareCloseButton()).len() <= padSquare(s, 20f, zoom) ||
                (p - s.setSquareRotateButton()).len() <= padSquare(s, 26f, zoom) ||
                (p - s.setSquareResizeButton()).len() <= padSquare(s, 22f, zoom) ||
                inTriangle(local, leg) ||
                dist <= 40f
            }
            GeoInstrument.Compass -> {
                val padClose = pad(s, 21f, zoom)
                val padHandle = pad(s, 17f, zoom)
                val padHinge = pad(s, 32f, zoom)
                val padTip = pad(s, 26f, zoom)
                val padLeg = pad(s, 18.2f, zoom)
                (p - s.closeButton).len() <= padClose ||
                (p - s.moveHandle).len() <= padHandle ||
                (p - s.compassHinge).len() <= padHinge ||
                (p - s.compassPencil).len() <= padTip ||
                distToSegment(p, s.compassHinge, s.compassPencil) <= padLeg ||
                (p - s.compassNeedle).len() <= padTip ||
                distToSegment(p, s.compassNeedle, s.compassHinge) <= padLeg
            }
            else -> true
        }
    }

    fun onDown(x: Float, y: Float, zoom: Float, width: Float = brushWidth, color: Int = brushColor): Boolean {
        this.brushWidth = width
        this.brushColor = color
        val s = state ?: return false
        val p = P(x, y)
        val hit = 22f / zoom
        gesture = when (s.instrument) {
            GeoInstrument.Ruler -> beginRuler(s, p, hit)
            GeoInstrument.Protractor -> beginProtractor(s, p, hit)
            GeoInstrument.Compass -> beginCompass(s, p, zoom)
            GeoInstrument.SetSquare -> beginSetSquare(s, p, zoom)
        }
        val g = gesture
        if (g is G.Dismiss) {
            gesture = G.None
            return true
        }
        if (g is G.DrawRuler) {
            val perp = g.side * (32f + (g.brushWidth / 2f + 1f) / s.uiScale)
            val pStart = world(s, P(g.startAlong, perp))
            state = s.copy(
                activeEdgeLine = pStart to pStart,
                hint = String.format(Locale.US, strings.rulerDraw, 0f, 0f)
            )
        }
        if (g is G.DrawSetSquareEdge) {
            g.hasDragged = false
            val p1 = s.setSquareToBoard(offsetOutside(s.setSquareLegPx, g.edge, g.startT, 2.5f))
            state = s.copy(activeEdgeLine = p1 to p1)
        }
        return gesture !== G.None
    }

    fun onMove(x: Float, y: Float, zoom: Float): Boolean {
        if (gesture === G.None) return false
        state = apply(gesture, P(x, y))
        return true
    }

    fun onUp(x: Float, y: Float, zoom: Float): Boolean {
        if (gesture === G.None) return false
        finish(gesture, P(x, y))
        gesture = G.None
        if (state?.instrument == GeoInstrument.SetSquare && state?.hint != null) {
            state = state?.copy(hint = null)
        }
        return true
    }

    // ---------------- mode selection ----------------

    private fun beginRuler(s: GeoState, p: P, hit: Float): G {
        val local = GeoKit.rotate(p.x - s.ox, p.y - s.oy, -s.angle)
        val lx = local.x / s.uiScale
        val ly = local.y / s.uiScale
        val len = GeoKit.rulerLen(s)
        val head = GeoKit.RULER_HEAD
        val spanPx = s.spanCm * GeoKit.PX_PER_CM

        // 1. Close button (✕) INSIDE the ruler head margin at (20f, 0f)
        val closeBtn = world(s, P(20f, 0f))
        if (hypot(p.x - closeBtn.x, p.y - closeBtn.y) <= 22f + hit || hypot(lx - 20f, ly) <= hit / s.uiScale + 14f) {
            close()
            cb.onDismiss()
            return G.Dismiss
        }

        // 2. Rotate knob near right end at (len + 30f, 0f)
        val rotateBtn = P(s.ox, s.oy) + GeoKit.rotate((len + 30f) * s.uiScale, 0f, s.angle)
        if (hypot(p.x - rotateBtn.x, p.y - rotateBtn.y) <= 26f + hit || hypot(lx - (len + 30f), ly) <= hit / s.uiScale + 14f) {
            return G.Rotate(s, bearing(s, p))
        }

        // 3. On the ruler body: 0 <= lx <= len && abs(ly) <= 32f
        if (lx in 0f..len && abs(ly) <= 32f) {
            // Resize grip at right end (lx >= len - 34f)
            if (lx >= len - 34f) {
                return G.ResizeRuler(s)
            }
            // Anywhere else on the body moves the ruler
            return G.Move(s, p - P(s.ox, s.oy))
        }

        // 4. Any other touch anywhere on the board -> DRAW mode (Teardown Rules #2, #3, #4, #5)
        // Latched side: -1 for upper edge (ly < 0), +1 for lower edge (ly >= 0)
        val side = if (ly < 0f) -1f else 1f
        // Start point along-axis clamped to printed scale marks [head, head + spanPx] and frozen
        val startAlong = lx.coerceIn(head, head + spanPx)

        return G.DrawRuler(
            start = s,
            side = side,
            startAlong = startAlong,
            endAlong = startAlong,
            brushWidth = brushWidth
        )
    }

    private fun beginProtractor(s: GeoState, p: P, hit: Float): G {
        val k = GeoKit.protractorScale(s)
        val local = GeoKit.rotate(p.x - s.ox, p.y - s.oy, -s.angle)
        val lx = local.x / k
        val ly = local.y / k

        if (ly in 8f..48f) {
            for (i in 0..5) {
                if (hypot(lx - (i * 52f - 130f), ly - 28f) <= 20f) {
                    when (i) {
                        0 -> { close(); cb.onDismiss(); return G.None }
                        1 -> stampProtractor(s, GeoStampKind.Angle)
                        2 -> stampProtractor(s, GeoStampKind.Arc)
                        3 -> stampProtractor(s, GeoStampKind.Sector)
                        4 -> return G.Zoom(s, p.y)
                        5 -> return G.Rotate(s, bearing(s, p))
                    }
                    return G.None
                }
            }
        }
        val arm = Math.toRadians(s.armDeg.toDouble()).toFloat()
        if (hypot(lx - cos(arm) * 186f, ly + sin(arm) * 186f) <= hit / k + 14f) return G.Swing(s, armDeg(p, s))
        if (abs(lx) <= 170f && ly in -170f..60f && (ly >= 0f || hypot(lx, ly) <= 170f)) {
            return G.Move(s, p - P(s.ox, s.oy))
        }
        return G.None
    }

    private fun beginCompass(s: GeoState, p: P, zoom: Float): G {
        val needle = s.compassNeedle
        val hinge = s.compassHinge
        val pencil = s.compassPencil

        // 1. Close button (pad 21)
        if ((p - s.closeButton).len() <= pad(s, 21f, zoom)) {
            close()
            cb.onDismiss()
            return G.None
        }

        // 2. Move handle capsule (pad 17)
        if ((p - s.moveHandle).len() <= pad(s, 17f, zoom)) {
            return G.CompassMove(s, p, needle, hinge, pencil)
        }

        // 3. Hinge sweep (pad 32)
        if ((p - hinge).len() <= pad(s, 32f, zoom)) {
            return G.CompassSweep(
                start = s,
                lastBearing = bearing(needle, p),
                total = 0f,
                lastBucket = 0,
                sweepFrom = bearing(needle, pencil)
            )
        }

        // 4. Pencil tip (pad 26) or pencil leg (pad 18.2)
        if ((p - pencil).len() <= pad(s, 26f, zoom) || distToSegment(p, hinge, pencil) <= pad(s, 18.2f, zoom)) {
            val rel = wrapDeg(bearing(hinge, pencil) - bearing(hinge, needle))
            return G.CompassRadius(s, if (rel >= 0f) 1f else -1f)
        }

        // 5. Needle tip (pad 26) or needle leg (pad 18.2)
        if ((p - needle).len() <= pad(s, 26f, zoom) || distToSegment(p, needle, hinge) <= pad(s, 18.2f, zoom)) {
            return G.CompassMove(s, p, needle, hinge, pencil)
        }

        return G.None
    }

    private fun beginSetSquare(s: GeoState, p: P, zoom: Float): G {
        if ((p - s.setSquareCloseButton()).len() <= padSquare(s, 20f, zoom)) {
            close()
            cb.onDismiss()
            return G.None
        }
        if ((p - s.setSquareRotateButton()).len() <= padSquare(s, 26f, zoom)) {
            val pivot = s.setSquareCentroid()
            val angleOffset = s.angle - atan2(p.y - pivot.y, p.x - pivot.x)
            return G.RotateSetSquare(s, pivot, angleOffset)
        }
        if ((p - s.setSquareResizeButton()).len() <= padSquare(s, 22f, zoom)) {
            return G.ResizeSetSquare(s, s.legCm)
        }
        val local = s.setSquareToLocal(p)
        val leg = s.setSquareLegPx
        val edge = nearestSetSquareEdge(leg, local)
        val dist = distToEdge(leg, edge, local)
        if (dist <= 26f || (!inTriangle(local, leg) && dist <= 40f)) {
            val t0 = projectOnEdge(leg, edge, local)
            return G.DrawSetSquareEdge(s, edge, t0, t0)
        }
        if (inTriangle(local, leg)) {
            return G.Move(s, p - P(s.ox, s.oy))
        }
        return G.None
    }

    // ---------------- gesture application ----------------

    private fun apply(g: G, p: P): GeoState? {
        val s = state ?: return null
        return when (g) {
            is G.Move -> s.copy(ox = p.x - g.grab.x, oy = p.y - g.grab.y)

            is G.Rotate -> {
                val raw = g.start.angleDeg + (bearing(P(g.start.ox, g.start.oy), p) - g.downDeg)
                val (deg, snapped) = GeoKit.snapRotation(raw, g.start.snapped)
                s.copy(
                    angle = Math.toRadians(deg.toDouble()).toFloat(),
                    snapped = snapped,
                    snapPulse = if (snapped && !g.start.snapped) g.start.snapPulse + 1 else g.start.snapPulse,
                )
            }

            is G.ResizeRuler -> {
                val local = GeoKit.rotate(p.x - s.ox, p.y - s.oy, -s.angle)
                val raw = (local.x / s.uiScale - GeoKit.RULER_HEAD - GeoKit.RULER_TAIL) / GeoKit.PX_PER_CM
                val snapped = nearest(raw, 1f, 0.6f).coerceIn(GeoKit.MIN_SPAN, GeoKit.MAX_SPAN)
                s.copy(spanCm = snapped).let { it.copy(hint = GeoKit.rulerReadout(it, strings)) }
            }

            is G.RotateSetSquare -> {
                val deg = Math.toDegrees(
                    (atan2(p.y - g.pivot.y, p.x - g.pivot.x) + g.angleOffset).toDouble()
                ).toFloat()
                val snapped = Math.round(deg / 15f) * 15f
                val onSnap = abs(deg - snapped) <= 3.5f
                val bucket = if (onSnap) (snapped / 15f).toInt() else null
                var pulse = s.snapPulse
                if (onSnap && bucket != g.lastBucket) { pulse++; g.lastBucket = bucket }
                if (!onSnap) g.lastBucket = null

                val use = if (onSnap) snapped else deg
                val rad = Math.toRadians(use.toDouble()).toFloat()
                val ox = (s.setSquareLegPx / 3f) * s.uiScale
                val oy = (-s.setSquareLegPx / 3f) * s.uiScale
                val c = cos(rad); val sn = sin(rad)
                val vx = g.pivot.x - (ox * c - oy * sn)
                val vy = g.pivot.y - (oy * c + ox * sn)

                val degNorm = wrap360(use).toInt()
                val check = if (onSnap) "  ✓" else ""
                s.copy(
                    ox = vx,
                    oy = vy,
                    angle = rad,
                    snapPulse = pulse,
                    hint = "$degNorm°$check"
                )
            }

            is G.ResizeSetSquare -> {
                val k = s.uiScale.coerceAtLeast(1e-4f)
                val raw = Math.round(((p - P(s.ox, s.oy)).len() - 28f * k) / (k * 37.795277f))
                val leg = raw.coerceIn(4, 8)
                var pulse = s.snapPulse
                if (leg != g.lastLeg) { g.lastLeg = leg; pulse++ }
                val hyp = leg * 1.4142135f
                val max = if (leg == 8) "  MAX" else ""
                s.copy(
                    legCm = leg,
                    snapPulse = pulse,
                    hint = String.format(Locale.US, "leg %d cm  ·  hyp %.1f cm%s", leg, hyp, max)
                )
            }

            is G.Swing -> s.copy(armDeg = normalizeDeg(armDeg(p, s)))

            is G.Zoom -> s.copy(radius = (g.start.radius * (1f + (p.y - g.downY) / 260f)).coerceIn(90f, 480f))

            is G.CompassMove -> {
                val d = p - g.down
                s.copy(
                    ox = g.sNeedle.x + d.x,
                    oy = g.sNeedle.y + d.y,
                    hx = g.sHinge.x + d.x,
                    hy = g.sHinge.y + d.y,
                    px = g.sPencil.x + d.x,
                    py = g.sPencil.y + d.y
                )
            }

            is G.CompassSweep -> {
                val needle = s.compassNeedle
                val b = bearing(needle, p)
                val delta = wrapDeg(b - g.lastBearing)
                g.lastBearing = b

                val next = GeoKit.clamp(g.total + delta, -360f, 360f)
                val applied = next - g.total
                g.total = next

                val hinge = rotateAbout(P(s.hx, s.hy), applied, needle)
                val pencil = rotateAbout(P(s.px, s.py), applied, needle)

                val bucket = (abs(g.total) / 90f).toInt()
                var pulse = s.snapPulse
                if (bucket != g.lastBucket) {
                    g.lastBucket = bucket
                    pulse++
                }

                val a = abs(g.total)
                val tail = if (a >= 359.5f) "  ·  ⭕ 360°" else "  ·  ${a.toInt()}°"
                val moved = s.copy(
                    hx = hinge.x,
                    hy = hinge.y,
                    px = pencil.x,
                    py = pencil.y,
                    snapPulse = pulse,
                    sweepFromDeg = g.sweepFrom,
                    sweepArcDeg = g.total
                )
                moved.copy(hint = moved.compassReadoutText() + tail)
            }

            is G.CompassRadius -> {
                val hinge = s.compassHinge
                val needle = s.compassNeedle
                val base = Math.toRadians(bearing(hinge, needle).toDouble())
                val rel = Math.toRadians(
                    wrapDeg(bearing(hinge, p) - bearing(hinge, needle)).toDouble()
                )
                val minA = 2.0 * asin(clampD(24.0 / (2.0 * s.legPx), 0.0, 1.0))
                val maxA = 2.0 * asin(0.985)
                val ang = g.radiusSign * clampD(abs(rel), minA, maxA) + base
                val pencil = P(
                    hinge.x + (cos(ang) * s.legPx).toFloat(),
                    hinge.y + (sin(ang) * s.legPx).toFloat()
                )
                val moved = s.copy(px = pencil.x, py = pencil.y)
                moved.copy(hint = moved.compassReadoutText())
            }

            is G.DrawRuler -> {
                val local = GeoKit.rotate(p.x - s.ox, p.y - s.oy, -s.angle)
                val lx = local.x / s.uiScale
                val head = GeoKit.RULER_HEAD
                val spanPx = s.spanCm * GeoKit.PX_PER_CM
                val endAlong = lx.coerceIn(head, head + spanPx)
                g.endAlong = endAlong

                val perp = g.side * (32f + (g.brushWidth / 2f + 1f) / s.uiScale)
                val pStart = world(s, P(g.startAlong, perp))
                val pEnd = world(s, P(endAlong, perp))

                val now = System.currentTimeMillis()
                val lenRulerUnits = abs(endAlong - g.startAlong)
                val lenCm = lenRulerUnits / GeoKit.PX_PER_CM
                val lenIn = lenCm / 2.54f

                val hintText = if (now - g.lastReadoutTime >= 16L || g.lastReadoutStr.isEmpty()) {
                    val str = String.format(Locale.US, strings.rulerDraw, lenCm, lenIn)
                    g.lastReadoutTime = now
                    g.lastReadoutStr = str
                    str
                } else {
                    g.lastReadoutStr
                }

                s.copy(
                    hint = hintText,
                    activeEdgeLine = pStart to pEnd
                )
            }

            is G.DrawSetSquareEdge -> {
                g.hasDragged = true
                val leg = s.setSquareLegPx
                val local = s.setSquareToLocal(p)
                val currentT = projectOnEdge(leg, g.edge, local)
                g.currentT = currentT
                val p1 = s.setSquareToBoard(offsetOutside(leg, g.edge, g.startT, 2.5f))
                val p2 = s.setSquareToBoard(offsetOutside(leg, g.edge, currentT, 2.5f))
                val lenLocal = abs(currentT - g.startT)
                val lenCm = lenLocal / 37.795277f
                s.copy(
                    hint = String.format(Locale.US, "%.1f cm", lenCm),
                    activeEdgeLine = p1 to p2
                )
            }

            G.Dismiss,
            G.None -> null
        }
    }

    private fun finish(g: G, p: P) {
        val s = state ?: return
        when (g) {
            is G.DrawRuler -> {
                val lenRulerUnits = abs(g.endAlong - g.startAlong)
                if (lenRulerUnits >= 6.0f) {
                    val perp = g.side * (32f + (g.brushWidth / 2f + 1f) / s.uiScale)
                    val pStart = world(s, P(g.startAlong, perp))
                    val pEnd = world(s, P(g.endAlong, perp))
                    cb.onStampLine(pStart, pEnd)
                }
                state = s.copy(hint = GeoKit.rulerReadout(s, strings), activeEdgeLine = null)
            }
            is G.DrawSetSquareEdge -> {
                val leg = s.setSquareLegPx
                val p1 = s.setSquareToBoard(offsetOutside(leg, g.edge, g.startT, 2.5f))
                val p2 = s.setSquareToBoard(offsetOutside(leg, g.edge, g.currentT, 2.5f))
                val lenLocal = abs(g.currentT - g.startT)
                if (g.hasDragged && lenLocal >= 6f) {
                    cb.onStampLine(p1, p2)
                }
                state = s.copy(hint = null, activeEdgeLine = null)
            }
            is G.CompassSweep -> {
                val needle = s.compassNeedle
                val r = s.compassRadius
                if (abs(g.total) >= 4f && r >= 1f) {
                    val strokeW = GeoKit.clamp(4f * s.g, 3f, 14f)
                    val a0 = Math.toRadians(g.sweepFrom.toDouble())
                    val a1 = Math.toRadians((g.sweepFrom + g.total).toDouble())
                    val startP = P(needle.x + (cos(a0) * r).toFloat(), needle.y + (sin(a0) * r).toFloat())
                    val endP = P(needle.x + (cos(a1) * r).toFloat(), needle.y + (sin(a1) * r).toFloat())
                    cb.onStampArc(needle, startP, endP, g.total, strokeW)
                }
                state = s.copy(hint = null, sweepFromDeg = null, sweepArcDeg = 0f)
            }
            is G.CompassRadius -> {
                state = s.copy(hint = null)
            }
            else -> Unit
        }
    }

    private fun stampProtractor(s: GeoState, kind: GeoStampKind) {
        val (pivot, arm, base) = GeoKit.protractorStamp(s)
        cb.onStampAngle(pivot, arm, base, kind)
    }

    // ---------------- helpers ----------------

    private fun world(s: GeoState, local: P): P {
        val r = GeoKit.rotate(local.x * s.uiScale, local.y * s.uiScale, s.angle)
        return P(s.ox + r.x, s.oy + r.y)
    }

    private fun bearing(origin: P, p: P) =
        Math.toDegrees(atan2((p.y - origin.y).toDouble(), (p.x - origin.x).toDouble())).toFloat()

    private fun bearing(s: GeoState, p: P) = bearing(P(s.ox, s.oy), p)

    private fun armDeg(p: P, s: GeoState): Float {
        val local = GeoKit.rotate(p.x - s.ox, p.y - s.oy, -s.angle)
        var deg = Math.toDegrees(atan2(-local.y.toDouble(), local.x.toDouble())).toFloat()
        if (deg < 0f) deg += 360f
        return deg.coerceIn(0f, 180f)
    }

    private fun normalizeDeg(d: Float): Float {
        var v = d % 360f
        if (v < 0) v += 360f
        return v
    }

    private fun nearest(v: Float, step: Float, tol: Float): Float {
        val n = (v / step).roundToInt() * step
        return if (abs(v - n) <= tol) n else v
    }

    private fun project(p: P, a: P, b: P): Float {
        val dx = b.x - a.x; val dy = b.y - a.y
        val len2 = dx * dx + dy * dy
        if (len2 < 1e-6f) return 0f
        return ((p.x - a.x) * dx + (p.y - a.y) * dy) / len2
    }

    private fun distToSegment(p: P, a: P, b: P): Float {
        val q = lerp(a, b, project(p, a, b).coerceIn(0f, 1f))
        return hypot(p.x - q.x, p.y - q.y)
    }

    private fun lerp(a: P, b: P, t: Float) = P(a.x + (b.x - a.x) * t, a.y + (b.y - a.y) * t)

    // ---------------- gesture types ----------------

    private sealed interface G {
        val start: GeoState
        data class Move(override val start: GeoState, val grab: P) : G
        data class Rotate(override val start: GeoState, val downDeg: Float) : G
        data class ResizeRuler(override val start: GeoState) : G
        data class RotateSetSquare(
            override val start: GeoState,
            val pivot: P,
            val angleOffset: Float,
            var lastBucket: Int? = null,
        ) : G
        data class ResizeSetSquare(
            override val start: GeoState,
            var lastLeg: Int = start.legCm,
        ) : G
        data class Swing(override val start: GeoState, val downDeg: Float) : G
        data class Zoom(override val start: GeoState, val downY: Float) : G
        data class CompassMove(
            override val start: GeoState,
            val down: P,
            val sNeedle: P,
            val sHinge: P,
            val sPencil: P,
        ) : G
        data class CompassSweep(
            override val start: GeoState,
            var lastBearing: Float,
            var total: Float,
            var lastBucket: Int,
            val sweepFrom: Float,
        ) : G
        data class CompassRadius(
            override val start: GeoState,
            val radiusSign: Float,
        ) : G
        data class DrawRuler(
            override val start: GeoState,
            val side: Float,
            val startAlong: Float,
            var endAlong: Float,
            val brushWidth: Float,
            var lastReadoutTime: Long = 0L,
            var lastReadoutStr: String = "",
        ) : G
        data object Dismiss : G {
            override val start: GeoState get() = error("dismiss")
        }
        data class DrawSetSquareEdge(
            override val start: GeoState,
            val edge: Int,
            val startT: Float,
            var currentT: Float,
            var hasDragged: Boolean = false,
        ) : G
        data object None : G {
            override val start: GeoState get() = error("no gesture")
        }
    }
}

// -----------------------------------------------------------------------------
// Renderer — a 1:1 port of the four reference renderers
// -----------------------------------------------------------------------------

internal class GeoRenderer(
    private val st: GeoStyle,
    private val str: GeoStrings,
    private val brushColor: Int = st.ink,
    private val brushWidth: Float = 4f
) {

    fun draw(c: Canvas, s: GeoState) {
        when (s.instrument) {
            GeoInstrument.Ruler -> ruler(c, s)
            GeoInstrument.Protractor -> protractor(c, s)
            GeoInstrument.Compass -> compass(c, s)
            GeoInstrument.SetSquare -> setSquare(c, s)
        }
        s.activeEdgeLine?.let { (a, b) ->
            val p = stroke(tint(brushColor, 0.95f), brushWidth.coerceAtLeast(2.5f))
            c.drawLine(a.x, a.y, b.x, b.y, p)
        }
        if (s.instrument != GeoInstrument.Compass && s.instrument != GeoInstrument.SetSquare) {
            s.hint?.let { hint(c, s, it) }
        }
    }

    private fun tint(c: Int, a: Float): Int =
        (c and 0x00FFFFFF) or ((a.coerceIn(0f, 1f) * 255f).toInt() shl 24)

    private fun fill(c: Int) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL; color = c
    }

    private fun stroke(c: Int, w: Float) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE; color = c; strokeWidth = w
        strokeCap = Paint.Cap.ROUND; strokeJoin = Paint.Join.ROUND
    }

    private fun text(size: Float, c: Int, bold: Boolean = true) = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textSize = size; color = c; textAlign = Paint.Align.CENTER
        typeface = if (bold) Typeface.DEFAULT_BOLD else Typeface.DEFAULT
        isFakeBoldText = bold
    }

    // ---- ruler ----

    private fun ruler(c: Canvas, s: GeoState) {
        val len = GeoKit.rulerLen(s)
        c.save()
        c.translate(s.ox, s.oy)
        c.rotate(s.angleDeg)
        c.scale(s.uiScale, s.uiScale)

        val body = RectF(0f, -32f, len, 32f)
        c.drawRoundRect(body, 9f, 9f, fill(tint(st.tan, st.bodyAlpha)))
        c.drawRoundRect(body, 9f, 9f, stroke(tint(st.brown, 0.55f), 1.5f))
        c.drawLine(6f, -32f, len - 6f, -32f, stroke(tint(st.dark, 0.85f), 2f))
        c.drawLine(6f, 32f, len - 6f, 32f, stroke(tint(st.dark, 0.85f), 2f))

        val tick = stroke(st.ink, 1f)
        val label = text(15f, st.ink)

        val cmTicks = (s.spanCm * 10f).roundToInt()
        for (i in 0..cmTicks) {
            val x = i * GeoKit.MM + 50f
            if (x > len - 6f) break
            val h = if (i % 10 == 0) 16f else if (i % 5 == 0) 11f else 6f
            c.drawLine(x, 31f, x, 31f - h, tick)
        }
        for (i in 0..s.spanCm.toInt()) {
            val x = i * GeoKit.PX_PER_CM + 50f
            if (x > len - 14f) break
            c.drawText(i.toString(), x, 13f, label)
        }

        val eighth = (s.spanCm * GeoKit.PX_PER_CM / 12f).toInt()
        for (i in 0..eighth) {
            val x = i * 12f + 50f
            if (x > len - 6f) break
            val h = when {
                i % 8 == 0 -> 16f
                i % 4 == 0 -> 12f
                i % 2 == 0 -> 9f
                else -> 6f
            }
            c.drawLine(x, -31f, x, -31f + h, tick)
        }
        for (i in 0..(s.spanCm * GeoKit.PX_PER_CM / 96f).toInt()) {
            val x = i * 96f + 50f
            if (x > len - 14f) break
            c.drawText(i.toString(), x, -3f, label)
        }

        val small = text(9f, st.ink).apply { textAlign = Paint.Align.LEFT }
        c.drawText("in", 36f, -17f, small)
        c.drawText("cm", 36f, 23f, small)

        // Close button (✕) placed INSIDE the ruler head margin (matching competitor screenshot)
        button(c, P(20f, 0f), 13.5f, st.closeRed, "close", 14f)

        val grip = RectF(len - 32f, -27f, len - 5f, 27f)
        c.drawRoundRect(grip, 7f, 7f, fill(tint(st.brown, 0.22f)))
        c.drawRoundRect(grip, 7f, 7f, stroke(tint(st.dark, 0.85f), 1.2f))
        for (k in -1..1) {
            val x = len - 18.5f + k * 6f
            c.drawLine(x, -18f, x, 18f, stroke(tint(st.dark, 0.85f), 2f))
        }
        c.restore()

        val knob = P(s.ox, s.oy) + GeoKit.rotate((len + 30f) * s.uiScale, 0f, s.angle)
        button(c, knob, 17f, st.rotateBlue, "rotate", 17f)
    }

    // ---- set square ----

    private fun setSquare(c: Canvas, s: GeoState) {
        val L = s.setSquareLegPx
        val r = s.setSquareInradius
        val notch = s.setSquareNotch

        c.save()
        c.translate(s.ox, s.oy)
        c.rotate(s.angleDeg)
        c.scale(s.uiScale, s.uiScale)

        val outer = trianglePoints(L, 1f, r)
        val inner = trianglePoints(L, 0.46f, r)
        val bevel = trianglePoints(L, 0.955f, r)

        val frame = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            moveTo(outer[0], outer[1]); lineTo(outer[2], outer[3]); lineTo(outer[4], outer[5]); close()
            moveTo(inner[0], inner[1]); lineTo(inner[2], inner[3]); lineTo(inner[4], inner[5]); close()
        }
        c.drawPath(frame, fill(0x73FFCE54.toInt()))
        c.drawPath(polyPath(bevel), stroke(0xCCFFF1C7.toInt(), 1.2f))
        c.drawPath(polyPath(outer), stroke(0xD95A4408.toInt(), 2.2f))
        c.drawPath(polyPath(inner), stroke(0x806E530A.toInt(), 1.5f))

        val ink = 0xFF3A2F10.toInt()
        val mark = stroke(ink, 1.4f)
        c.drawLine(15f, 0f, 15f, -15f, mark)
        c.drawLine(15f, -15f, 0f, -15f, mark)

        setSquareTicks(c, true, L, notch, s.legCm, ink)
        setSquareTicks(c, false, L, notch, s.legCm, ink)

        val inset = GeoKit.clamp(notch * 0.5f, 12f, 22f) * 0.70710677f
        button(c, P(L / 2f - inset, -L / 2f + inset), 11f, 0xFFEF4444.toInt(), "close", 14f)
        button(c, P(0f, -L - 26f), 18f, 0xFF2563EB.toInt(), "rotate", 20f)
        button(c, P(L + 28f, 0f), 14f, 0xFF2563EB.toInt(), "resize", 17f)

        c.restore()

        s.hint?.let { text ->
            val size = 13f * s.uiScale
            val p = text(size, 0xFFFFFFFF.toInt(), bold = true).apply {
                textAlign = Paint.Align.CENTER
            }
            val cp = s.setSquareCentroid()
            val halfW = p.measureText(text) / 2f + 7f * s.uiScale
            val halfH = 3.5f * s.uiScale + size * 0.72f
            val bg = fill(0xF03A2F10.toInt())
            c.drawRoundRect(
                RectF(cp.x - halfW, cp.y - halfH, cp.x + halfW, cp.y + halfH),
                7f * s.uiScale, 7f * s.uiScale, bg
            )
            val fm = p.fontMetrics
            c.drawText(text, cp.x, cp.y - (fm.ascent + fm.descent) / 2f, p)
        }
    }

    private fun trianglePoints(leg: Float, k: Float, r: Float): FloatArray {
        val cx = r
        val cy = -r
        val vx = floatArrayOf(0f, leg, 0f)
        val vy = floatArrayOf(0f, 0f, -leg)
        val out = FloatArray(6)
        for (i in 0..2) {
            out[i * 2] = (vx[i] - cx) * k + cx
            out[i * 2 + 1] = (vy[i] - cy) * k + cy
        }
        return out
    }

    private fun polyPath(p: FloatArray): Path = Path().apply {
        moveTo(p[0], p[1]); lineTo(p[2], p[3]); lineTo(p[4], p[5]); close()
    }

    private fun setSquareTicks(
        c: Canvas, horizontal: Boolean, leg: Float,
        notch: Float, legCm: Int, ink: Int
    ) {
        val count = Math.round(legCm * 10f)
        val labelSize = GeoKit.clamp(0.052f * leg, 8f, 12f)
        val tickBase = GeoKit.clamp(0.28f * notch, 6f, 12f)
        val labelInset = GeoKit.clamp(
            0.55f * notch,
            tickBase + 6f,
            notch - 0.6f * labelSize - 2f
        )

        val p = stroke(ink, 1f)
        for (i in 0..count) {
            val pos = 3.7795277f * i
            val len = min(
                if (i % 10 == 0) tickBase
                else if (i % 5 == 0) 0.64f * tickBase
                else 0.4f * tickBase,
                leg - pos - 1f
            )
            if (len < 1f) continue
            if (horizontal) c.drawLine(pos, 0f, pos, -len, p)
            else c.drawLine(0f, -pos, len, -pos, p)
        }

        val tp = text(Math.round(labelSize).toFloat(), ink, bold = true).apply {
            textAlign = Paint.Align.CENTER
            isFakeBoldText = true
        }
        val fm = tp.fontMetrics
        val vCenter = -(fm.ascent + fm.descent) / 2f
        for (i in 1 until legCm) {
            val pos = min(i * 37.795277f, leg - labelInset - 6f)
            if (pos < 6f) continue
            if (horizontal) c.drawText("$i", pos, -labelInset + vCenter, tp)
            else c.drawText("$i", labelInset, -pos + vCenter, tp)
        }
    }

    // ---- protractor ----

    private fun protractor(c: Canvas, s: GeoState) {
        val k = GeoKit.protractorScale(s)
        val arm = Math.toRadians(s.armDeg.toDouble()).toFloat()
        c.save()
        c.translate(s.ox, s.oy)
        c.rotate(s.angleDeg)
        c.scale(k, k)

        val body = Path().apply {
            moveTo(-160f, 0f); lineTo(160f, 0f); lineTo(160f, 42f)
            arcTo(RectF(132f, 28f, 160f, 56f), 0f, 90f, false)
            lineTo(-146f, 56f)
            arcTo(RectF(-160f, 28f, -132f, 56f), 90f, 90f, false)
            close()
        }
        c.drawPath(body, fill(tint(st.tan, st.bodyAlpha)))
        c.drawPath(body, stroke(st.brown, 1.5f))

        val ring = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            addArc(-160f, -160f, 160f, 160f, 180f, 180f)
            addArc(-105.6f, -105.6f, 105.6f, 105.6f, 0f, -180f)
            close()
        }
        c.drawPath(ring, fill(tint(st.tan, st.bodyAlpha)))
        c.drawArc(-160f, -160f, 160f, 160f, 180f, 180f, false, stroke(st.dark, 2f))
        c.drawArc(-105.6f, -105.6f, 105.6f, 105.6f, 180f, 180f, false, stroke(st.brown, 1f))
        c.drawArc(-124.8f, -124.8f, 124.8f, 124.8f, 180f, 180f, false, stroke(tint(st.brown, 0.47f), 1f))
        c.drawLine(-158f, 0f, 158f, 0f, stroke(st.dark, 1.5f))

        val dashed = stroke(st.accent, 1.6f).apply { pathEffect = DashPathEffect(floatArrayOf(7f, 5f), 0f) }
        c.drawLine(0f, -158f, 0f, 0f, dashed)

        val degreeTick = stroke(st.ink, 1f)
        val majorTick = stroke(st.accent, 2.4f)
        for (i in 0..180) {
            val len = if (i % 10 == 0) 15f else if (i % 5 == 0) 10f else 6f
            val a = Math.toRadians(i.toDouble()).toFloat()
            c.drawLine(
                cos(a) * 158f, -sin(a) * 158f,
                cos(a) * (158f - len), -sin(a) * (158f - len),
                if (i == 90) majorTick else degreeTick,
            )
        }

        val label = text(12.5f, st.ink)
        for (i in 0..180 step 10) {
            val a = Math.toRadians(i.toDouble()).toFloat()
            label.textSize = 12.5f; label.color = if (i == 90) st.accent else st.ink
            c.drawText(i.toString(), cos(a) * 130f, -sin(a) * 130f + 4.5f, label)
            label.textSize = 9.5f; label.color = st.ink
            c.drawText((180 - i).toString(), cos(a) * 112f, -sin(a) * 112f + 3.5f, label)
        }

        val pivot = Path().apply { addArc(RectF(-20.8f, -20.8f, 20.8f, 20.8f), 180f, 180f); close() }
        c.drawPath(pivot, fill(tint(st.tan, st.bodyAlpha)))
        c.drawPath(pivot, stroke(st.ink, 1.2f))
        c.drawLine(-10f, 0f, 10f, 0f, stroke(st.ink, 1.5f))
        c.drawLine(0f, -10f, 0f, 3f, stroke(st.ink, 1.5f))
        c.drawCircle(0f, 0f, 3f, fill(st.ink))

        val ax = cos(arm) * 186f; val ay = -sin(arm) * 186f
        c.drawLine(0f, 0f, ax, ay, stroke(st.brown, 3.5f))
        // Hollow ring handle at arm tip matching competitor
        c.drawCircle(ax, ay, 6.5f, stroke(st.brown, 4.2f))

        val buttons = listOf(
            Triple(0, st.closeRed, "close"),
            Triple(1, st.angleTeal, "angle"),
            Triple(2, st.arcViolet, "arc"),
            Triple(3, st.sectorAmber, "sector"),
            Triple(4, st.resizePink, "vertical_resize"),
            Triple(5, st.rotateBlue, "rotate"),
        )
        for ((i, color, icon) in buttons) button(c, P(i * 52f - 130f, 28f), 12.3f, color, icon, 15f)
        c.restore()

        // upright readout pill, 54.4 units along the instrument's up axis
        val d = 54.4f * k
        val px = s.ox + sin(s.angle) * d
        val py = s.oy - cos(s.angle) * d
        val pr = 18f * k
        c.drawCircle(px, py, pr, fill(st.brown))
        c.drawText("${s.armDeg.roundToInt()}°", px, py + pr * 0.35f, text(0.95f * pr, 0xFFFFFFFF.toInt()))
    }

    // ---- compass ----

    private fun compass(c: Canvas, s: GeoState) {
        val g = s.g
        val needle = s.compassNeedle
        val hinge = s.compassHinge
        val pencil = s.compassPencil
        val r = s.compassRadius

        val body = 0xFF3F4654.toInt()
        val pencilOuter = 0xFF2563EB.toInt()
        val pencilDot = 0xFF1D4ED8.toInt()
        val needleArrow = 0xFF334155.toInt()
        val accent = 0xFF1F6FE0.toInt()
        val arcPreview = 0x801F6FE0.toInt()
        val hingeDisc = 0xFF374151.toInt()
        val hingeRing = 0xE6FFFFFF.toInt()
        val closeBtnColor = 0xFFE06B8B.toInt()
        val white = 0xFFFFFFFF.toInt()

        // 1. Preview arc
        s.sweepFromDeg?.let { from ->
            val rect = RectF(needle.x - r, needle.y - r, needle.x + r, needle.y + r)
            c.drawArc(rect, from, s.sweepArcDeg, false, stroke(arcPreview, 4f * g))
        }

        // 2. Needle leg (needle -> hinge)
        c.drawLine(needle.x, needle.y, hinge.x, hinge.y, stroke(body, 11f * g))

        // 3. Pencil leg (hinge -> pencil)
        c.drawLine(hinge.x, hinge.y, pencil.x, pencil.y, stroke(body, 11f * g))

        // 4. Pencil leg blue section (hinge + 62% -> pencil)
        val blueFrom = P(
            hinge.x + (pencil.x - hinge.x) * 0.62f,
            hinge.y + (pencil.y - hinge.y) * 0.62f
        )
        c.drawLine(blueFrom.x, blueFrom.y, pencil.x, pencil.y, stroke(pencilOuter, 11f * g))

        // 5. Needle arrowhead
        val u = (hinge - needle).unit()
        val n = P(-u.y, u.x)
        val base = needle + u * (14f * g)
        val tri = Path().apply {
            moveTo(needle.x, needle.y)
            lineTo(base.x + n.x * 4.5f * g, base.y + n.y * 4.5f * g)
            lineTo(base.x - n.x * 4.5f * g, base.y - n.y * 4.5f * g)
            close()
        }
        c.drawPath(tri, fill(needleArrow))

        // 6. Needle dot
        c.drawCircle(needle.x, needle.y, 3f * g, fill(accent))

        // 7. Pencil dot
        c.drawCircle(pencil.x, pencil.y, 5f * g, fill(pencilDot))

        // 8. Needle glyph (at needle + 0.30*(hinge - needle), r = 18g)
        glyph(c, needle + (hinge - needle) * 0.30f, 18f * g, true)

        // 9. Pencil glyph (at hinge + 0.42*(pencil - hinge), r = 18g)
        glyph(c, hinge + (pencil - hinge) * 0.42f, 18f * g, false)

        // 10. Hinge disc
        c.drawCircle(hinge.x, hinge.y, 22f * g, fill(hingeDisc))

        // 11. Hinge ring
        c.drawCircle(hinge.x, hinge.y, 22f * g, stroke(hingeRing, 2f * g))

        // 12. Hinge rotate button / dial (clean circular rotate arrow matching opponent app)
        val dialR = 8.4f * g
        c.save()
        c.translate(hinge.x, hinge.y)
        c.drawArc(RectF(-dialR, -dialR, dialR, dialR), -80f, 290f, false, stroke(white, 2.4f * g))
        val arrowAngleRad = Math.toRadians(-80.0)
        val arrowX = (cos(arrowAngleRad) * dialR).toFloat()
        val arrowY = (sin(arrowAngleRad) * dialR).toFloat()
        c.save()
        c.translate(arrowX, arrowY)
        c.rotate(15f)
        val pointer = Path().apply {
            moveTo(3.5f * g, 0f)
            lineTo(-2f * g, -2.8f * g)
            lineTo(-2f * g, 2.8f * g)
            close()
        }
        c.drawPath(pointer, fill(white))
        c.restore()
        c.restore()

        // 13. Move capsule
        val a = s.moveHandle
        c.save()
        c.translate(a.x, a.y)
        c.rotate(bearing(needle, hinge) + 90f)
        c.drawRoundRect(RectF(-10f * g, -17f * g, 10f * g, 17f * g), 9f * g, 9f * g, fill(body))
        c.restore()

        // 14. Close button
        val b = s.closeButton
        c.drawCircle(b.x, b.y, 13f * g, fill(closeBtnColor))
        val xPaint = stroke(white, 2.2f * g)
        c.drawLine(b.x - 4f * g, b.y - 4f * g, b.x + 4f * g, b.y + 4f * g, xPaint)
        c.drawLine(b.x + 4f * g, b.y - 4f * g, b.x - 4f * g, b.y + 4f * g, xPaint)

        // 15. Prominent Blue Readout Pill (matching opponent app Image 1)
        val textStr = s.hint ?: s.compassReadoutText()
        val textPaint = text(max(13f * g, 16f), white, bold = true)
        val textW = textPaint.measureText(textStr)
        val pillPadX = 14f * g
        val pillW = textW + pillPadX * 2f
        val pillH = max(24f * g, 32f)
        val pillU = (needle - hinge).unit()
        val pillCenter = hinge + pillU * (17f * g)
        val pillRect = RectF(
            pillCenter.x - pillW / 2f,
            pillCenter.y - pillH / 2f,
            pillCenter.x + pillW / 2f,
            pillCenter.y + pillH / 2f
        )
        val pillRadius = 11f * g
        c.drawRoundRect(pillRect, pillRadius, pillRadius, fill(0xFF3D67DE.toInt()))
        c.drawText(textStr, pillCenter.x, pillCenter.y + (textPaint.textSize * 0.35f), textPaint)
    }

    private fun glyph(c: Canvas, at: P, r: Float, needle: Boolean) {
        c.drawCircle(at.x, at.y, r / 2f, fill(0xFF1F2733.toInt()))
        c.save()
        c.translate(at.x, at.y)
        c.scale(r / 18f, r / 18f)
        val p = stroke(0xFFFFFFFF.toInt(), 1.6f)
        if (needle) {
            c.drawLine(-3.6f, -3.5f, -3.6f, 3.5f, p)
            c.drawLine(3.6f, -3.5f, 3.6f, 3.5f, p)
        } else {
            c.drawLine(-4.5f, 0f, 4.0f, 0f, p)
            c.drawLine(1.5f, -3.0f, 4.5f, 0f, p)
            c.drawLine(4.5f, 0f, 1.5f, 3.0f, p)
        }
        c.restore()
    }

    // ---- shared widgets ----

    private fun button(c: Canvas, at: P, radius: Float, color: Int, icon: String, iconSize: Float) {
        c.drawCircle(at.x, at.y + 1.1f, radius + 1.4f, fill(tint(0x000000, 0.18f)))
        c.drawCircle(at.x, at.y, radius, fill(color))
        c.drawCircle(at.x, at.y, radius, stroke(0xFFFFFFFF.toInt(), radius * 0.14f))
        icon(c, at, iconSize, icon)
    }

    private fun icon(c: Canvas, at: P, size: Float, name: String) {
        val p = stroke(0xFFFFFFFF.toInt(), 2.4f)
        val f = fill(0xFFFFFFFF.toInt())
        c.save()
        c.translate(at.x, at.y)
        val k = size / 24f
        c.scale(k, k)
        c.translate(-12f, -12f)
        when (name) {
            "close" -> { c.drawLine(6f, 6f, 18f, 18f, p); c.drawLine(18f, 6f, 6f, 18f, p) }
            "rotate" -> {
                c.drawPath(Path().apply { addArc(4.8f, 4.8f, 19.2f, 19.2f, -87.1f, 291.6f) }, p)
                c.save()
                c.translate(12f + cos(-1.5208f) * 7.2f, 12f + sin(-1.5208f) * 7.2f)
                c.rotate(2.87f)
                c.drawPath(Path().apply {
                    moveTo(3.2f, 0f); lineTo(-1.7f, -2.6f); lineTo(-1.7f, 2.6f); close()
                }, f)
                c.restore()
            }
            "resize" -> {
                c.drawLine(7f, 17f, 17f, 7f, p)
                c.drawLine(7f, 11.5f, 7f, 17f, p); c.drawLine(7f, 17f, 12.5f, 17f, p)
                c.drawLine(17f, 12.5f, 17f, 7f, p); c.drawLine(17f, 7f, 11.5f, 7f, p)
            }
            "angle" -> {
                c.drawLine(5f, 18f, 19f, 18f, p)
                c.drawLine(5f, 18f, 17f, 6f, p)
                c.drawArc(0f, 2f, 20f, 22f, -53f, 53f, false, stroke(0xFFFFFFFF.toInt(), 2f))
            }
            "arc" -> {
                c.drawArc(4f, 8f, 20f, 24f, 180f, 180f, false, p)
                c.drawCircle(4f, 20f, 2f, f); c.drawCircle(20f, 20f, 2f, f)
            }
            "sector" -> {
                c.drawPath(Path().apply {
                    moveTo(7f, 17f)
                    lineTo(7f, 7f)
                    arcTo(RectF(-3f, 7f, 17f, 27f), 270f, 90f, false)
                    close()
                }, f)
            }
            "vertical_resize" -> {
                c.drawLine(12f, 5.5f, 12f, 18.5f, p)
                c.drawLine(8f, 9.5f, 12f, 5.5f, p)
                c.drawLine(16f, 9.5f, 12f, 5.5f, p)
                c.drawLine(8f, 14.5f, 12f, 18.5f, p)
                c.drawLine(16f, 14.5f, 12f, 18.5f, p)
            }
            "zoom" -> {
                c.drawCircle(11f, 11f, 6f, p)
                c.drawLine(15.5f, 15.5f, 20f, 20f, p)
                c.drawLine(8f, 11f, 14f, 11f, p); c.drawLine(11f, 8f, 11f, 14f, p)
            }
        }
        c.restore()
    }

    private fun hint(c: Canvas, s: GeoState, label: String) {
        val paint = text(13f, 0xFFFFFFFF.toInt())
        val half = paint.measureText(label) / 2f + 12f
        val at = when (s.instrument) {
            GeoInstrument.Ruler -> {
                val spanPx = s.spanCm * GeoKit.PX_PER_CM
                val midX = GeoKit.RULER_HEAD + spanPx / 2f
                GeoKit.world(s, P(midX, -48f))
            }
            GeoInstrument.SetSquare -> {
                val leg = GeoKit.setSquareLeg(s) * s.uiScale
                val r = GeoKit.rotate(leg / 3f, -leg / 3f, s.angle)
                P(s.ox + r.x, s.oy + r.y)
            }
            GeoInstrument.Compass -> {
                val h = s.compassHinge
                P(h.x, h.y - 48f * s.g)
            }
            else -> P(s.ox, s.oy + 90f)
        }
        c.drawRoundRect(RectF(at.x - half, at.y - 15f, at.x + half, at.y + 15f), 10f, 10f, fill(tint(0x000000, 0.72f)))
        c.drawText(label, at.x, at.y + 5f, paint)
    }
}

// -----------------------------------------------------------------------------
// Rendering the COMMITTED stamps later (in your normal stroke renderer)
// -----------------------------------------------------------------------------

object GeoStampPath {

    /** Keep sweeps in (-180°, 180°] so arcs go the short way. */
    fun normSweep(sweep: Float): Float {
        var v = sweep % 360f
        if (v > 180f) v -= 360f
        if (v < -180f) v += 360f
        return v
    }

    /**
     * Compass sweep. points = [needle, start, end]; the radius comes from |start - needle|.
     *
     *   val path = GeoStampPath.arc(points)
     *   canvas.drawPath(path, strokePaint)      // width = clamp(r * 0.04, 3, 14)
     */
    fun arc(points: List<P>): Path {
        val out = Path()
        if (points.size < 3) return out
        val o = points[0]; val a = points[1]; val b = points[2]
        val r = hypot(a.x - o.x, a.y - o.y)
        val a0 = Math.toDegrees(atan2((a.y - o.y).toDouble(), (a.x - o.x).toDouble())).toFloat()
        val a1 = Math.toDegrees(atan2((b.y - o.y).toDouble(), (b.x - o.x).toDouble())).toFloat()
        out.addArc(RectF(o.x - r, o.y - r, o.x + r, o.y + r), a0, normSweep(a1 - a0))
        return out
    }

    /**
     * Protractor stamp. points = [pivot, armEnd, baseEnd].
     *
     *  Angle  -> arc + both arms       (draw it stroked)
     *  Arc    -> arc only              (draw it stroked)
     *  Sector -> closed pie slice      (fill it, then stroke)
     */
    fun protractorPoints(kind: GeoStampKind, points: List<P>): Path {
        val out = Path()
        if (points.size < 3) return out
        val o = points[0]; val a = points[1]; val b = points[2]
        val r = hypot(a.x - o.x, a.y - o.y)
        val rect = RectF(o.x - r, o.y - r, o.x + r, o.y + r)
        val a0 = Math.toDegrees(atan2((a.y - o.y).toDouble(), (a.x - o.x).toDouble())).toFloat()
        val a1 = Math.toDegrees(atan2((b.y - o.y).toDouble(), (b.x - o.x).toDouble())).toFloat()
        val sweep = normSweep(a1 - a0)
        when (kind) {
            GeoStampKind.Angle -> {
                out.addArc(rect, a0, sweep)
                out.moveTo(o.x, o.y); out.lineTo(a.x, a.y)
                out.moveTo(o.x, o.y); out.lineTo(b.x, b.y)
            }
            GeoStampKind.Arc -> out.addArc(rect, a0, sweep)
            GeoStampKind.Sector -> {
                out.moveTo(o.x, o.y)
                out.arcTo(rect, a0, sweep, false)
                out.close()
            }
        }
        return out
    }
}
