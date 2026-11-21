package com.teachboard.app.ui.canvas

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PointF
import com.teachboard.app.domain.model.StrokePoint
import kotlin.math.*

/**
 * Pressure-sensitive stroke renderer implementing the exact pipeline from
 * scribble 0.10.x + perfect_freehand 2.x (decompiled from Whiteboard - Write & Draw 1.0.6).
 *
 * Renders a filled polygon ribbon with variable width, streamline resampling,
 * velocity simulation fallback for uniform pressure / touch, decimation, sharp corner
 * fans, and rounded caps.
 */
object FreehandStrokeRenderer {

  private const val STREAMLINE = 0.5f
  private const val THINNING = 0.5f
  private const val SMOOTHING = 0.5f
  private const val RATE_OF_PRESSURE_CHANGE = 0.275f

  private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    style = Paint.Style.FILL
  }

  // Pre-allocated reusable path to avoid GC churn during live gestures
  private val strokePath = Path()

  data class StreamlinePoint(
    val x: Float,
    val y: Float,
    var pressure: Float,
    var vecX: Float = 0f,
    var vecY: Float = 0f,
    val distance: Float = 0f,
    val len: Float = 0f
  )

  fun drawPencilStroke(
    canvas: Canvas,
    points: List<StrokePoint>,
    color: Int,
    alpha: Int,
    strokeWidth: Float
  ) {
    if (points.isEmpty()) return

    fillPaint.color = color
    fillPaint.alpha = alpha

    val size = strokeWidth * 2f

    // Single sample -> filled dot
    if (points.size == 1) {
      val p0 = points[0].pressure.coerceIn(0f, 1f)
      val r = size * (0.25f + 0.5f * p0)
      canvas.drawCircle(points[0].x, points[0].y, r.coerceAtLeast(0.5f), fillPaint)
      return
    }

    // 1. Detect simulation: if all points have identical pressure (e.g. finger, mouse)
    val firstPressure = points[0].pressure
    val simulatePressure = points.all { abs(it.pressure - firstPressure) < 0.001f }

    // 2. Resample (streamline)
    // t = 0.15 + (1 - streamline) * 0.85 = 0.575
    val t = 0.15f + (1f - STREAMLINE) * 0.85f
    val sp = ArrayList<StreamlinePoint>(points.size)
    sp.add(StreamlinePoint(points[0].x, points[0].y, points[0].pressure, distance = 0f, len = 0f))

    var prev = sp[0]
    var run = 0f
    var reached = false

    for (i in points.indices) {
      val raw = points[i]
      val px = prev.x + (raw.x - prev.x) * t
      val py = prev.y + (raw.y - prev.y) * t
      val pp = prev.pressure + (raw.pressure - prev.pressure) * t

      if (px == prev.x && py == prev.y && pp == prev.pressure) continue

      val d = hypot(px - prev.x, py - prev.y)
      run += d

      if (i < points.size - 1 && !reached) {
        if (run < size) continue // swallow fat start
        reached = true
      }

      val (vx, vy) = unit(prev.x - px, prev.y - py)
      val cur = StreamlinePoint(px, py, pp, vx, vy, d, run)
      prev = cur
      sp.add(cur)
    }

    if (sp.size > 1) {
      sp[0].vecX = sp[1].vecX
      sp[0].vecY = sp[1].vecY
    } else {
      val r = size * (0.25f + 0.5f * sp[0].pressure)
      canvas.drawCircle(sp[0].x, sp[0].y, r.coerceAtLeast(0.5f), fillPaint)
      return
    }

    // 3. Pressure seed for simulate mode
    var prevP = sp[0].pressure
    val seedLimit = min(10, sp.size)
    for (j in 1 until seedLimit) {
      if (simulatePressure) {
        val simP = simulateVelocity(sp[j].distance, size, prevP)
        prevP = (prevP + simP) / 2f
      } else {
        prevP = (prevP + sp[j].pressure) / 2f
      }
    }

    // 4. Left / Right outline walk
    val totalLen = sp.last().len
    val minD2 = (size * SMOOTHING) * (size * SMOOTHING)
    var firstRadius: Float? = null
    var radius = size * (0.25f + 0.5f * sp.last().pressure)

    val left = ArrayList<PointF>()
    val right = ArrayList<PointF>()
    var plX = sp[0].x
    var plY = sp[0].y
    var prX = sp[0].x
    var prY = sp[0].y
    var prevVecX = sp[0].vecX
    var prevVecY = sp[0].vecY
    var prevSharp = false

    for (i in sp.indices) {
      val s = sp[i]
      // Tail culling: remove jitter from lift-off
      if (i < sp.size - 1 && totalLen - s.len < size) continue

      if (THINNING != 0f) {
        var p = s.pressure
        if (simulatePressure) {
          p = simulateVelocity(s.distance, size, prevP)
          prevP = p
        }
        radius = size * (0.5f - THINNING * (0.5f - p))
      } else {
        radius = size / 2f
      }

      if (firstRadius == null) firstRadius = radius
      radius = max(0.01f, radius)

      val hasNext = i < sp.size - 1
      val nvX = if (hasNext) sp[i + 1].vecX else s.vecX
      val nvY = if (hasNext) sp[i + 1].vecY else s.vecY
      val nextDpr = if (hasNext) (s.vecX * nvX + s.vecY * nvY) else 1f
      val prevDpr = (s.vecX * prevVecX + s.vecY * prevVecY)

      if ((prevDpr < 0f && !prevSharp) || nextDpr < 0f) {
        // Sharp corner fan
        val offX = prevVecY * radius
        val offY = -prevVecX * radius
        for (k in 1..13) {
          val a = (PI * k / 13.0).toFloat()
          left.add(rotate(s.x - offX, s.y - offY, s.x, s.y, a))
          right.add(rotate(s.x + offX, s.y + offY, s.x, s.y, -a))
        }
        if (left.isNotEmpty()) {
          plX = left.last().x; plY = left.last().y
          prX = right.last().x; prY = right.last().y
        }
        prevSharp = nextDpr < 0f
        continue
      }
      prevSharp = false

      if (i == sp.size - 1) {
        val offX = s.vecY * radius
        val offY = -s.vecX * radius
        left.add(PointF(s.x - offX, s.y - offY))
        right.add(PointF(s.x + offX, s.y + offY))
        continue
      }

      val clampedNextDpr = nextDpr.coerceIn(0f, 1f)
      val blendX = nvX + (s.vecX - nvX) * clampedNextDpr
      val blendY = nvY + (s.vecY - nvY) * clampedNextDpr
      val (bX, bY) = unit(blendX, blendY)
      val offX = bY * radius
      val offY = -bX * radius

      val lx = s.x - offX
      val ly = s.y - offY
      val rx = s.x + offX
      val ry = s.y + offY

      if (i <= 1 || dist2(plX, plY, lx, ly) > minD2) {
        left.add(PointF(lx, ly))
        plX = lx; plY = ly
      }
      if (i <= 1 || dist2(prX, prY, rx, ry) > minD2) {
        right.add(PointF(rx, ry))
        prX = rx; prY = ry
      }
      prevVecX = s.vecX
      prevVecY = s.vecY
    }

    // 5. Caps
    if (left.isEmpty() || right.isEmpty()) {
      canvas.drawCircle(sp[0].x, sp[0].y, (firstRadius ?: radius).coerceAtLeast(0.5f), fillPaint)
      return
    }

    val startCap = ArrayList<PointF>(13)
    val rightFirst = right.first()
    val spFirst = sp.first()
    for (k in 1..13) {
      val a = (PI * k / 13.0).toFloat()
      startCap.add(rotate(rightFirst.x, rightFirst.y, spFirst.x, spFirst.y, a))
    }

    val spLast = sp.last()
    val endDirX = -spLast.vecY
    val endDirY = spLast.vecX
    val p0x = spLast.x + endDirX * radius
    val p0y = spLast.y + endDirY * radius
    val endCap = ArrayList<PointF>(29)
    for (k in 1..29) {
      val a = (3.0 * PI * k / 29.0).toFloat()
      endCap.add(rotate(p0x, p0y, spLast.x, spLast.y, a))
    }

    // 6. Polygon winding + midpoint quadratic path
    val polygon = ArrayList<PointF>(left.size + endCap.size + right.size + startCap.size)
    polygon.addAll(left)
    polygon.addAll(endCap)
    for (k in right.indices.reversed()) {
      polygon.add(right[k])
    }
    polygon.addAll(startCap)

    if (polygon.size >= 3) {
      strokePath.reset()
      strokePath.moveTo(polygon[0].x, polygon[0].y)
      for (i in 1 until polygon.size - 1) {
        val p0 = polygon[i]
        val p1 = polygon[i + 1]
        strokePath.quadTo(p0.x, p0.y, (p0.x + p1.x) / 2f, (p0.y + p1.y) / 2f)
      }
      strokePath.close()
      canvas.drawPath(strokePath, fillPaint)
    }
  }

  private fun simulateVelocity(distance: Float, size: Float, prevP: Float): Float {
    val spd = (distance / size).coerceIn(0f, 1f)
    val rp = (1f - spd).coerceIn(0f, 1f)
    return (prevP + (rp - prevP) * (spd * RATE_OF_PRESSURE_CHANGE)).coerceIn(0f, 1f)
  }

  private fun unit(dx: Float, dy: Float): Pair<Float, Float> {
    val d = hypot(dx, dy)
    return if (d < 1e-6f) 0f to 0f else (dx / d) to (dy / d)
  }

  private fun rotate(px: Float, py: Float, cx: Float, cy: Float, angle: Float): PointF {
    val cosA = cos(angle)
    val sinA = sin(angle)
    val dx = px - cx
    val dy = py - cy
    return PointF(cx + dx * cosA - dy * sinA, cy + dx * sinA + dy * cosA)
  }

  private fun dist2(ax: Float, ay: Float, bx: Float, by: Float): Float {
    val dx = ax - bx
    val dy = ay - by
    return dx * dx + dy * dy
  }
}
