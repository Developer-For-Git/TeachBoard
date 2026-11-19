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
 *  Compass      ox,oy = needle tip · angle = needle→hinge bearing · legPx · opening
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
    val armDeg: Float = 0f,
    val legPx: Float = 200f,
    val opening: Float = 1.10f,
    val legCm: Int = 6,
    val hint: String? = null,
    val sweep: Pair<Float, Float>? = null,
    val activeEdgeLine: Pair<P, P>? = null,
) {
    val angleDeg: Float get() = Math.toDegrees(angle.toDouble()).toFloat()
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
    fun onStampAngle(pivot: P, armEnd: P, baseEnd: P, kind: GeoStampKind)
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

    fun rotate(x: Float, y: Float, rad: Float): P {
        val c = cos(rad); val n = sin(rad)
        return P(x * c - y * n, x * n + y * c)
    }

    fun compassHinge(s: GeoState) = P(s.ox + s.legPx * cos(s.angle), s.oy + s.legPx * sin(s.angle))

    /** Hinge + leg rotated by the apex angle away from the needle leg. */
    fun compassPencil(s: GeoState): P {
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
                GeoState(inst, cx, cy + 0.42f * r, radius = r)
            }
            GeoInstrument.Compass -> {
                val leg = clamp(0.34f * mn, 120f * z, 230f * z) / z
                GeoState(
                    inst, cx, cy, angle = -PI.toFloat() / 2f, legPx = leg,
                    opening = 2f * asin(1.10f / 2f),
                ).let { it.copy(hint = compassReadout(it, strings)) }
            }
            GeoInstrument.SetSquare -> {
                val legPx = 6 * PX_PER_CM
                val scale = clamp((mn * 0.62f) / z / (legPx + 70f), 0.55f, 6f)
                GeoState(
                    inst, cx - legPx * scale / 2f, cy + legPx * scale / 2f,
                    uiScale = scale, legCm = 6,
                ).let { it.copy(hint = setSquareReadout(it, strings)) }
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

    fun draw(c: Canvas) {
        val s = state ?: return
        GeoRenderer(style, strings).draw(c, s)
    }

    // ---------------- input ----------------

    fun onDown(x: Float, y: Float, zoom: Float): Boolean {
        val s = state ?: return false
        val p = P(x, y)
        val hit = 22f / zoom
        gesture = when (s.instrument) {
            GeoInstrument.Ruler -> beginRuler(s, p, hit)
            GeoInstrument.Protractor -> beginProtractor(s, p, hit)
            GeoInstrument.Compass -> beginCompass(s, p, hit)
            GeoInstrument.SetSquare -> beginSetSquare(s, p, hit)
        }
        val g = gesture
        if (g is G.DrawEdge) {
            g.t1 = 1f
            g.hasDragged = false
            val startP = lerp(g.a, g.b, g.t0.coerceIn(0f, 1f))
            state = s.copy(activeEdgeLine = startP to g.b)
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
        return true
    }

    // ---------------- mode selection ----------------

    private fun beginRuler(s: GeoState, p: P, hit: Float): G {
        val local = GeoKit.rotate(p.x - s.ox, p.y - s.oy, -s.angle)
        val lx = local.x / s.uiScale
        val ly = local.y / s.uiScale
        val len = GeoKit.rulerLen(s)
        val scaleStart = GeoKit.RULER_HEAD
        val scaleEnd = GeoKit.RULER_HEAD + s.spanCm * GeoKit.PX_PER_CM

        // 1. Close button (✕) INSIDE the ruler head margin at (20f, 0f)
        val closeBtn = world(s, P(20f, 0f))
        if (hypot(p.x - closeBtn.x, p.y - closeBtn.y) <= 22f + hit || hypot(lx - 20f, ly) <= hit / s.uiScale + 14f) {
            close()
            cb.onDismiss()
            return G.None
        }

        // 2. Rotate knob at right end
        val rotateBtn = P(s.ox, s.oy) + GeoKit.rotate((len + 30f) * s.uiScale, 0f, s.angle)
        if (hypot(p.x - rotateBtn.x, p.y - rotateBtn.y) <= 24f + hit || hypot(lx - (len + 30f), ly) <= hit / s.uiScale + 12f) {
            return G.Rotate(s, bearing(s, p))
        }

        // 3. Resize grip at right end
        if (lx in (len - 40f)..(len + 12f) && abs(ly) <= 32f) return G.ResizeRuler(s)

        // 4. Middle strip for dragging / moving ruler
        if (lx in 35f..(len - 40f) && abs(ly) <= 14f) return G.Move(s, p - P(s.ox, s.oy))

        // 5. Line drawing along upward (top) and downward (bottom) edges, anchored from scale 0
        val hitMargin = hit / s.uiScale + 30f
        val isTopEdge = lx in (scaleStart - 20f)..(scaleEnd + 25f) && ly in (-32f - hitMargin)..(-14f)
        val isBottomEdge = lx in (scaleStart - 20f)..(scaleEnd + 25f) && ly in 14f..(32f + hitMargin)

        if (isTopEdge) {
            val a = world(s, P(scaleStart, -32f))
            val b = world(s, P(scaleEnd, -32f))
            return G.DrawEdge(s, a, b, 0f)
        }
        if (isBottomEdge) {
            val a = world(s, P(scaleStart, 32f))
            val b = world(s, P(scaleEnd, 32f))
            return G.DrawEdge(s, a, b, 0f)
        }

        if (lx in 0f..len && abs(ly) <= 32f) return G.Move(s, p - P(s.ox, s.oy))

        return G.None
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

    private fun beginCompass(s: GeoState, p: P, hit: Float): G {
        val needle = P(s.ox, s.oy)
        val hinge = GeoKit.compassHinge(s)
        val pencil = GeoKit.compassPencil(s)

        if (hypot(p.x - hinge.x, p.y - (hinge.y - 44f)) <= hit + 6f) {
            close(); cb.onDismiss(); return G.None
        }
        if (hypot(p.x - hinge.x, p.y - hinge.y) <= hit + 12f) return G.Move(s, p - hinge)
        if (hypot(p.x - needle.x, p.y - needle.y) <= hit + 8f) return G.Move(s, p - needle)

        val bearingNow = Math.toDegrees(atan2(pencil.y - needle.y, pencil.x - needle.x).toDouble()).toFloat()
        if (hypot(p.x - pencil.x, p.y - pencil.y) <= hit + 20f) return G.Sweep(s, bearingNow)

        val t = project(p, hinge, pencil)
        if (t in 0.3f..0.75f && distToSegment(p, hinge, pencil) <= hit + 8f) return G.Radius(s)
        return G.None
    }

    private fun beginSetSquare(s: GeoState, p: P, hit: Float): G {
        val local = GeoKit.rotate(p.x - s.ox, p.y - s.oy, -s.angle)
        val lx = local.x / s.uiScale
        val ly = local.y / s.uiScale
        val leg = GeoKit.setSquareLeg(s)
        val rim = leg / 3.4142137f
        val d = max(12f, min(22f, 0.54f * rim * 0.5f))
        val off = 0.70710677f * d

        if (hypot(lx - (leg / 2f - off), ly - (-leg / 2f + off)) <= hit / s.uiScale + 6f) {
            close(); cb.onDismiss(); return G.None
        }
        if (hypot(lx, ly - (-leg - 26f)) <= hit / s.uiScale + 14f) return G.Rotate(s, bearing(s, p))
        if (hypot(lx - (leg + 28f), ly) <= hit / s.uiScale + 8f) return G.ResizeSquare(s)

        if (lx >= 0f && ly <= 0f && (lx - ly) <= leg) return G.Move(s, p - P(s.ox, s.oy))

        val edges = listOf(
            world(s, P(0f, 0f)) to world(s, P(leg, 0f)),
            world(s, P(0f, 0f)) to world(s, P(0f, -leg)),
            world(s, P(leg, 0f)) to world(s, P(0f, -leg)),
        )
        var best: Pair<P, P>? = null
        var bestD = Float.MAX_VALUE
        for ((a, b) in edges) {
            val dd = distToSegment(p, a, b)
            if (dd < bestD) { bestD = dd; best = a to b }
        }
        val e = best
        if (e != null && bestD <= hit + 8f) return G.DrawEdge(s, e.first, e.second, project(p, e.first, e.second))
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

            is G.ResizeSquare -> {
                val local = GeoKit.rotate(p.x - s.ox, p.y - s.oy, -s.angle)
                val cm = (local.x / s.uiScale / GeoKit.PX_PER_CM).roundToInt().coerceIn(4, 8)
                s.copy(legCm = cm).let { it.copy(hint = GeoKit.setSquareReadout(it, strings)) }
            }

            is G.Swing -> s.copy(armDeg = normalizeDeg(armDeg(p, s)))

            is G.Zoom -> s.copy(radius = (g.start.radius * (1f + (p.y - g.downY) / 260f)).coerceIn(90f, 480f))

            is G.Radius -> {
                val chord = 2f * max(8f, hypot(p.x - s.ox, p.y - s.oy))
                val half = asin((chord / 2f / max(1f, s.legPx)).coerceIn(0f, 0.985f))
                s.copy(opening = (2f * half).coerceIn(0.05f, 2.9f))
                    .let { it.copy(hint = GeoKit.compassReadout(it, strings)) }
            }

            is G.Sweep -> {
                val needle = P(g.start.ox, g.start.oy)
                val now = Math.toDegrees(atan2(p.y - needle.y, p.x - needle.x).toDouble()).toFloat()
                s.copy(
                    angle = g.start.angle + Math.toRadians((now - g.startBearing).toDouble()).toFloat(),
                    sweep = g.startBearing to now,
                )
            }

            is G.DrawEdge -> {
                g.hasDragged = true
                val t = project(p, g.a, g.b).coerceIn(0f, 1f)
                g.t1 = if (t > 0.88f) 1f else t.coerceAtLeast(0.04f)
                val lineStart = lerp(g.a, g.b, g.t0.coerceIn(0f, 1f))
                val lineEnd = lerp(g.a, g.b, g.t1)
                val len = hypot(lineEnd.x - lineStart.x, lineEnd.y - lineStart.y)
                s.copy(
                    hint = String.format(Locale.US, strings.rulerDraw, len / GeoKit.PX_PER_CM, len / 96f),
                    activeEdgeLine = lineStart to lineEnd
                )
            }

            G.None -> null
        }
    }

    private fun finish(g: G, p: P) {
        val s = state ?: return
        when (g) {
            is G.DrawEdge -> {
                val lineStart = lerp(g.a, g.b, g.t0.coerceIn(0f, 1f))
                val lineEnd = if (g.hasDragged) lerp(g.a, g.b, g.t1) else g.b
                if (hypot(lineEnd.x - lineStart.x, lineEnd.y - lineStart.y) > 6f) {
                    cb.onStampLine(lineStart, lineEnd)
                }
                state = s.copy(hint = null, activeEdgeLine = null)
            }
            is G.Sweep -> {
                val needle = P(g.start.ox, g.start.oy)
                val now = Math.toDegrees(atan2(p.y - needle.y, p.x - needle.x).toDouble()).toFloat()
                val delta = now - g.startBearing
                val r = GeoKit.compassRadius(g.start)
                if (abs(delta) >= 4f && r > 8f) {
                    val a0 = Math.toRadians(g.startBearing.toDouble()).toFloat()
                    val a1 = Math.toRadians(now.toDouble()).toFloat()
                    cb.onStampArc(
                        needle,
                        P(needle.x + cos(a0) * r, needle.y + sin(a0) * r),
                        P(needle.x + cos(a1) * r, needle.y + sin(a1) * r),
                    )
                }
                // put the compass back where it started, keep the measured radius
                state = s.copy(angle = g.start.angle, sweep = null)
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
        data class ResizeSquare(override val start: GeoState) : G
        data class Swing(override val start: GeoState, val downDeg: Float) : G
        data class Zoom(override val start: GeoState, val downY: Float) : G
        data class Radius(override val start: GeoState) : G
        data class Sweep(override val start: GeoState, val startBearing: Float) : G
        data class DrawEdge(
            override val start: GeoState,
            val a: P,
            val b: P,
            val t0: Float = 0f,
        ) : G {
            var t1: Float = 1f
            var hasDragged: Boolean = false
        }
        data object None : G {
            override val start: GeoState get() = error("no gesture")
        }
    }
}

// -----------------------------------------------------------------------------
// Renderer — a 1:1 port of the four reference renderers
// -----------------------------------------------------------------------------

internal class GeoRenderer(private val st: GeoStyle, private val str: GeoStrings) {

    fun draw(c: Canvas, s: GeoState) {
        s.activeEdgeLine?.let { (a, b) ->
            val p = stroke(tint(st.ink, 0.95f), 3.5f * s.uiScale)
            c.drawLine(a.x, a.y, b.x, b.y, p)
        }
        when (s.instrument) {
            GeoInstrument.Ruler -> ruler(c, s)
            GeoInstrument.Protractor -> protractor(c, s)
            GeoInstrument.Compass -> compass(c, s)
            GeoInstrument.SetSquare -> setSquare(c, s)
        }
        s.hint?.let { hint(c, s, it) }
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
        val leg = GeoKit.setSquareLeg(s)
        val rim = leg / 3.4142137f
        val inset = 0.54f * rim
        c.save()
        c.translate(s.ox, s.oy)
        c.rotate(s.angleDeg)
        c.scale(s.uiScale, s.uiScale)

        val outer = tri(ssPoints(leg, inset, 1f))
        val hole = tri(ssPoints(leg, inset, 0.46f))
        val combined = Path().apply {
            fillType = Path.FillType.EVEN_ODD
            addPath(outer); addPath(hole)
        }
        c.drawPath(combined, fill(tint(st.tan, st.bodyAlpha)))
        c.drawPath(tri(ssPoints(leg, inset, 0.955f)), stroke(tint(st.dark, 0.85f), 1.2f))
        c.drawPath(outer, stroke(tint(st.dark, 0.95f), 2.2f))
        c.drawPath(hole, stroke(tint(st.brown, 0.95f), 1.5f))

        val mark = stroke(st.ink, 1.4f)
        c.drawLine(15f, 0f, 15f, -15f, mark)
        c.drawLine(15f, -15f, 0f, -15f, mark)

        ticks(c, true, leg, inset, s.legCm, st.ink)
        ticks(c, false, leg, inset, s.legCm, st.ink)

        val d = max(12f, min(22f, inset * 0.5f))
        val off = 0.70710677f * d
        button(c, P(leg / 2f - off, -leg / 2f + off), 11f, st.closeRed, "close", 14f)
        button(c, P(0f, -leg - 26f), 18f, st.rotateBlue, "rotate", 20f)
        button(c, P(leg + 28f, 0f), 14f, st.rotateBlue, "resize", 17f)
        c.restore()
    }

    private fun ssPoints(size: Float, inset: Float, t: Float): FloatArray {
        val xs = floatArrayOf(0f, size, 0f)
        val ys = floatArrayOf(0f, 0f, -size)
        val out = FloatArray(6)
        for (i in 0..2) {
            out[i * 2] = xs[i] * t + inset * (1f - t)
            out[i * 2 + 1] = ys[i] * t - inset * (1f - t)
        }
        return out
    }

    private fun tri(p: FloatArray) = Path().apply {
        moveTo(p[0], p[1]); lineTo(p[2], p[3]); lineTo(p[4], p[5]); close()
    }

    private fun ticks(c: Canvas, alongX: Boolean, len: Float, thickness: Float, cm: Int, color: Int) {
        val tickLen = GeoKit.clamp(0.28f * thickness, 6f, 12f)
        val labelAt = GeoKit.clamp(0.55f * thickness, tickLen + 6f, thickness - 0.6f * 12f - 2f)
        val paint = stroke(color, 1f)
        val mm = (len / GeoKit.MM).toInt().coerceAtMost(cm * 10)
        for (i in 0..mm) {
            val d = i * GeoKit.MM
            val l = min(if (i % 10 == 0) tickLen else if (i % 5 == 0) 0.64f * tickLen else 0.4f * tickLen, len - d - 1f)
            if (l < 1f) break
            if (alongX) c.drawLine(d, 0f, d, -l, paint) else c.drawLine(0f, -d, l, -d, paint)
        }
        val label = text(min(12f, max(8f, 0.052f * len)), color)
        for (i in 1 until cm) {
            val d = min(i * GeoKit.PX_PER_CM, len - labelAt - 6f)
            if (d < 6f) continue
            if (alongX) c.drawText(i.toString(), d, -labelAt + 4f, label)
            else c.drawText(i.toString(), labelAt, -d + 4f, label)
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
        c.drawCircle(ax, ay, 10f, fill(st.brown))
        c.drawCircle(ax, ay, 10f, stroke(0xFFFFFFFF.toInt(), 2f))
        c.drawCircle(ax, ay, 3.2f, fill(0xFFFFFFFF.toInt()))

        val buttons = listOf(
            Triple(0, st.closeRed, "close"),
            Triple(1, st.angleTeal, "angle"),
            Triple(2, st.arcViolet, "arc"),
            Triple(3, st.sectorAmber, "sector"),
            Triple(4, st.zoomYellow, "zoom"),
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
        c.drawCircle(px, py, pr, stroke(0xCD0000FF.toInt(), 1.2f))
        c.drawText("${s.armDeg.roundToInt()}°", px, py + pr * 0.35f, text(0.95f * pr, 0xFFFFFFFF.toInt()))
    }

    // ---- compass ----

    private fun compass(c: Canvas, s: GeoState) {
        val needle = P(s.ox, s.oy)
        val hinge = GeoKit.compassHinge(s)
        val pencil = GeoKit.compassPencil(s)
        val r = GeoKit.compassRadius(s)

        val legs = stroke(st.compassBody, 11f)
        c.drawLine(needle.x, needle.y, hinge.x, hinge.y, legs)
        c.drawLine(hinge.x, hinge.y, pencil.x, pencil.y, legs)

        val dx = hinge.x - needle.x; val dy = hinge.y - needle.y
        val n = max(hypot(dx, dy), 1e-4f)
        val ux = dx / n; val uy = dy / n
        val tip = P(needle.x + ux * 14f, needle.y + uy * 14f)
        val needleTri = Path().apply {
            moveTo(needle.x, needle.y)
            lineTo(tip.x - uy * 4.5f, tip.y + ux * 4.5f)
            lineTo(tip.x + uy * 4.5f, tip.y - ux * 4.5f)
            close()
        }
        c.drawPath(needleTri, fill(st.compassAccent))
        c.drawCircle(needle.x, needle.y, 3f, fill(st.compassAccent))
        c.drawCircle(pencil.x, pencil.y, 5f, fill(st.compassAccent))

        c.drawCircle(hinge.x, hinge.y, 22f, fill(st.compassBody))
        c.drawCircle(hinge.x, hinge.y, 22f, stroke(0xFFFFFFFF.toInt(), 2f))
        val arc = Path().apply {
            addArc(hinge.x - 8.4f, hinge.y - 8.4f, hinge.x + 8.4f, hinge.y + 8.4f, -95f, 250f)
        }
        c.drawPath(arc, stroke(st.ink, 2.4f))
        val ah = P(hinge.x + cos(-1.5208f) * 8.4f, hinge.y + sin(-1.5208f) * 8.4f)
        val head = Path().apply {
            moveTo(ah.x + 3.4f, ah.y); lineTo(ah.x - 1.8f, ah.y - 2.8f); lineTo(ah.x - 1.8f, ah.y + 2.8f); close()
        }
        c.drawPath(head, fill(st.ink))

        button(c, P(hinge.x, hinge.y - 44f), 16f, 0xFFE0608B.toInt(), "close", 16f)

        s.sweep?.let { (a0, a1) ->
            if (abs(a1 - a0) >= 0.5f) {
                val rect = RectF(needle.x - r, needle.y - r, needle.x + r, needle.y + r)
                c.drawArc(rect, a0, a1 - a0, false,
                    stroke(tint(st.compassAccent, 0.85f), max(3f, min(r * 0.04f, 14f))))
            }
        }
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
                    moveTo(4f, 20f); arcTo(RectF(4f, 4f, 20f, 20f), 180f, -90f, false); close()
                }, f)
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
                val len = GeoKit.rulerLen(s) * s.uiScale / 2f
                val r = GeoKit.rotate(len, 0f, s.angle)
                P(s.ox + r.x, s.oy + r.y - 52f)
            }
            GeoInstrument.SetSquare -> {
                val leg = GeoKit.setSquareLeg(s) * s.uiScale
                val r = GeoKit.rotate(leg / 3f, -leg / 3f, s.angle)
                P(s.ox + r.x, s.oy + r.y)
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
