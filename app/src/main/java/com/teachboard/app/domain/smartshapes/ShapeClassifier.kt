package com.teachboard.app.domain.smartshapes

import com.teachboard.app.domain.model.Contour
import com.teachboard.app.domain.model.StrokePoint
import com.teachboard.app.domain.model.ToolType
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.acos
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.math.sqrt

data class SmartShapeCandidate(
  val toolType: ToolType,
  val label: String,
  val confidence: Float,
  val geometry: List<Contour>
)

/**
 * Pure-geometry Smart Shapes Classifier covering 15 shape classes (PRD Section 19.9):
 * 1. Line, 2. Circle, 3. Oval, 4. Equilateral Triangle, 5. Right Triangle, 6. Isosceles Triangle,
 * 7. Square, 8. Rectangle, 9. Rhombus, 10. Parallelogram, 11. Trapezoid, 12. Kite,
 * 13. Quadrilateral, 14. Pentagon, 15. Hexagon (+ Heptagon & Octagon).
 */
object ShapeClassifier {

  fun classify(rawPoints: List<StrokePoint>): List<SmartShapeCandidate> {
    if (rawPoints.size < 2) return emptyList()
    val resampled = resample(rawPoints, targetCount = 64)
    val bounds = computeBounds(resampled)
    val bw = bounds.maxX - bounds.minX
    val bh = bounds.maxY - bounds.minY
    val diag = hypot(bw, bh)
    if (diag < 32f) return emptyList()

    val startEndGap = hypot(
      resampled.last().x - resampled.first().x,
      resampled.last().y - resampled.first().y
    )
    val isClosed = rawPoints.size >= 4 && startEndGap < diag * 0.28f

    if (!isClosed) {
      val straightness = computeLineScore(resampled)
      if (straightness >= 0.85f) {
        val a = resampled.first()
        val b = resampled.last()
        val lineContour = Contour(closed = false, points = listOf(a, b))
        val angle = atan2((b.y - a.y).toDouble(), (b.x - a.x).toDouble())
        val headLen = (diag * 0.16f).coerceIn(18f, 44f)
        val wing1 = StrokePoint(
          b.x - headLen * cos(angle - PI / 6.0).toFloat(),
          b.y - headLen * sin(angle - PI / 6.0).toFloat()
        )
        val wing2 = StrokePoint(
          b.x - headLen * cos(angle + PI / 6.0).toFloat(),
          b.y - headLen * sin(angle + PI / 6.0).toFloat()
        )
        val arrowContours = listOf(lineContour, Contour(closed = false, points = listOf(wing1, b, wing2)))
        return listOf(
          SmartShapeCandidate(ToolType.Line, "Line", straightness, listOf(lineContour)),
          SmartShapeCandidate(ToolType.Arrow, "Arrow", straightness * 0.9f, arrowContours)
        )
      }
      return emptyList()
    }

    val candidates = mutableListOf<SmartShapeCandidate>()

    // 1. Circle & Oval fit
    val circleScore = computeCircleScore(resampled, bounds)
    val aspectRatio = min(bw, bh) / max(bw, bh).coerceAtLeast(1f)
    if (circleScore >= 0.86f && aspectRatio >= 0.78f) {
      val r = (bw + bh) / 4f
      val cx = (bounds.minX + bounds.maxX) / 2f
      val cy = (bounds.minY + bounds.maxY) / 2f
      candidates.add(SmartShapeCandidate(ToolType.Circle, "Circle", circleScore, listOf(sampleEllipse(cx, cy, r, r))))
    }
    if (circleScore > 0.72f) {
      val cx = (bounds.minX + bounds.maxX) / 2f
      val cy = (bounds.minY + bounds.maxY) / 2f
      val ovalConf = if (aspectRatio < 0.78f) circleScore else circleScore * 0.88f
      candidates.add(SmartShapeCandidate(ToolType.Oval, "Oval", ovalConf, listOf(sampleEllipse(cx, cy, bw / 2f, bh / 2f))))
    }

    // 2. Corner detection by turning-angle grouping (~±10° tolerance)
    val corners = detectCorners(resampled, diag)
    when (corners.size) {
      3 -> classifyTriangle(corners, candidates)
      4 -> classifyQuadrilateral(corners, bounds, candidates)
      5 -> candidates.add(SmartShapeCandidate(ToolType.Pentagon, "Pentagon", 0.88f, listOf(regularPolygon(bounds, 5))))
      6 -> candidates.add(SmartShapeCandidate(ToolType.Hexagon, "Hexagon", 0.88f, listOf(regularPolygon(bounds, 6))))
      7 -> candidates.add(SmartShapeCandidate(ToolType.Heptagon, "Heptagon", 0.85f, listOf(regularPolygon(bounds, 7))))
      8 -> candidates.add(SmartShapeCandidate(ToolType.Octagon, "Octagon", 0.85f, listOf(regularPolygon(bounds, 8))))
    }

    return candidates
      .distinctBy { it.toolType }
      .sortedByDescending { it.confidence }
      .take(3)
  }

  private fun classifyTriangle(corners: List<StrokePoint>, out: MutableList<SmartShapeCandidate>) {
    val s0 = dist(corners[0], corners[1])
    val s1 = dist(corners[1], corners[2])
    val s2 = dist(corners[2], corners[0])
    val sides = listOf(s0, s1, s2).sorted()
    val maxS = sides[2].coerceAtLeast(1f)
    val angles = listOf(
      cornerAngleDeg(corners[2], corners[0], corners[1]),
      cornerAngleDeg(corners[0], corners[1], corners[2]),
      cornerAngleDeg(corners[1], corners[2], corners[0])
    )
    val hasRight = angles.any { abs(it - 90f) <= 13f }
    val allEqual = (sides[2] - sides[0]) / maxS <= 0.15f
    val twoEqual = abs(sides[0] - sides[1]) / maxS <= 0.14f || abs(sides[1] - sides[2]) / maxS <= 0.14f
    val poly = Contour(closed = true, points = corners)

    when {
      hasRight -> {
        out.add(SmartShapeCandidate(ToolType.RightTriangle, "Right Triangle", 0.91f, listOf(poly)))
        out.add(SmartShapeCandidate(ToolType.Triangle, "Triangle", 0.80f, listOf(poly)))
      }
      allEqual -> {
        out.add(SmartShapeCandidate(ToolType.Triangle, "Equilateral Triangle", 0.93f, listOf(poly)))
      }
      twoEqual -> {
        out.add(SmartShapeCandidate(ToolType.IsoscelesTriangle, "Isosceles Triangle", 0.88f, listOf(poly)))
        out.add(SmartShapeCandidate(ToolType.Triangle, "Triangle", 0.82f, listOf(poly)))
      }
      else -> {
        out.add(SmartShapeCandidate(ToolType.Triangle, "Triangle", 0.85f, listOf(poly)))
      }
    }
  }

  private fun classifyQuadrilateral(
    corners: List<StrokePoint>,
    bounds: Bounds,
    out: MutableList<SmartShapeCandidate>
  ) {
    val sides = (0 until 4).map { i -> dist(corners[i], corners[(i + 1) % 4]) }
    val angles = (0 until 4).map { i ->
      cornerAngleDeg(corners[(i + 3) % 4], corners[i], corners[(i + 1) % 4])
    }
    val allRight = angles.all { abs(it - 90f) <= 18f }
    val maxS = sides.max().coerceAtLeast(1f)
    val allEqual = (sides.max() - sides.min()) / maxS <= 0.18f
    val oppEqual = abs(sides[0] - sides[2]) / maxS <= 0.18f && abs(sides[1] - sides[3]) / maxS <= 0.18f
    val adjPairsEqual = (abs(sides[0] - sides[1]) / maxS <= 0.16f && abs(sides[2] - sides[3]) / maxS <= 0.16f) ||
      (abs(sides[1] - sides[2]) / maxS <= 0.16f && abs(sides[3] - sides[0]) / maxS <= 0.16f)

    val p02 = areParallel(corners[0], corners[1], corners[3], corners[2])
    val p13 = areParallel(corners[1], corners[2], corners[0], corners[3])

    val rectContour = Contour(
      closed = true,
      points = listOf(
        StrokePoint(bounds.minX, bounds.minY),
        StrokePoint(bounds.maxX, bounds.minY),
        StrokePoint(bounds.maxX, bounds.maxY),
        StrokePoint(bounds.minX, bounds.maxY)
      )
    )
    val poly = Contour(closed = true, points = corners)

    when {
      allRight && allEqual -> {
        val cx = (bounds.minX + bounds.maxX) / 2f
        val cy = (bounds.minY + bounds.maxY) / 2f
        val half = ((bounds.maxX - bounds.minX) + (bounds.maxY - bounds.minY)) / 4f
        val sq = Contour(
          closed = true,
          points = listOf(
            StrokePoint(cx - half, cy - half),
            StrokePoint(cx + half, cy - half),
            StrokePoint(cx + half, cy + half),
            StrokePoint(cx - half, cy + half)
          )
        )
        out.add(SmartShapeCandidate(ToolType.Square, "Square", 0.94f, listOf(sq)))
        out.add(SmartShapeCandidate(ToolType.Rect, "Rectangle", 0.84f, listOf(rectContour)))
      }
      allRight -> {
        out.add(SmartShapeCandidate(ToolType.Rect, "Rectangle", 0.92f, listOf(rectContour)))
      }
      allEqual -> {
        out.add(SmartShapeCandidate(ToolType.Rhombus, "Rhombus", 0.90f, listOf(poly)))
        out.add(SmartShapeCandidate(ToolType.Diamond, "Diamond", 0.85f, listOf(poly)))
      }
      oppEqual && p02 && p13 -> {
        out.add(SmartShapeCandidate(ToolType.Parallelogram, "Parallelogram", 0.88f, listOf(poly)))
      }
      p02 || p13 -> {
        out.add(SmartShapeCandidate(ToolType.Trapezoid, "Trapezoid", 0.86f, listOf(poly)))
      }
      adjPairsEqual -> {
        out.add(SmartShapeCandidate(ToolType.Kite, "Kite", 0.85f, listOf(poly)))
      }
      else -> {
        out.add(SmartShapeCandidate(ToolType.Quadrilateral, "Quadrilateral", 0.80f, listOf(poly)))
        out.add(SmartShapeCandidate(ToolType.Rect, "Rectangle", 0.72f, listOf(rectContour)))
      }
    }
  }

  private fun computeLineScore(points: List<StrokePoint>): Float {
    val a = points.first()
    val b = points.last()
    val chord = dist(a, b)
    if (chord < 24f) return 0f
    var pathLen = 0f
    for (i in 1 until points.size) pathLen += dist(points[i - 1], points[i])
    return (chord / pathLen.coerceAtLeast(1f)).coerceIn(0f, 1f)
  }

  private fun computeCircleScore(points: List<StrokePoint>, bounds: Bounds): Float {
    val cx = (bounds.minX + bounds.maxX) / 2f
    val cy = (bounds.minY + bounds.maxY) / 2f
    val rx = ((bounds.maxX - bounds.minX) / 2f).coerceAtLeast(1f)
    val ry = ((bounds.maxY - bounds.minY) / 2f).coerceAtLeast(1f)
    var errSum = 0.0
    for (p in points) {
      val nx = (p.x - cx) / rx
      val ny = (p.y - cy) / ry
      val r = sqrt((nx * nx + ny * ny).toDouble())
      errSum += abs(r - 1.0)
    }
    val meanErr = (errSum / points.size).toFloat()
    return (1f - meanErr * 1.45f).coerceIn(0f, 1f)
  }

  private fun detectCorners(points: List<StrokePoint>, diag: Float): List<StrokePoint> {
    // Douglas-Peucker simplification + turning-angle merge (±10° tolerance)
    val closedPts = points + points.first()
    val simplified = rdp(closedPts, epsilon = diag * 0.045f).dropLast(1)
    if (simplified.size < 3) return simplified
    val merged = mutableListOf<StrokePoint>()
    for (i in simplified.indices) {
      val prev = simplified[(i + simplified.size - 1) % simplified.size]
      val cur = simplified[i]
      val next = simplified[(i + 1) % simplified.size]
      val interior = cornerAngleDeg(prev, cur, next)
      if (interior < 168f) { // ignore nearly collinear vertices (~±12°)
        if (merged.isEmpty() || dist(merged.last(), cur) > diag * 0.08f) {
          merged.add(cur)
        }
      }
    }
    if (merged.size > 1 && dist(merged.first(), merged.last()) < diag * 0.08f) {
      merged.removeAt(merged.lastIndex)
    }
    return merged
  }

  private fun resample(points: List<StrokePoint>, targetCount: Int): List<StrokePoint> {
    var totalLen = 0f
    for (i in 1 until points.size) totalLen += dist(points[i - 1], points[i])
    if (totalLen <= 0.001f) return points
    val step = totalLen / (targetCount - 1)
    val out = ArrayList<StrokePoint>(targetCount)
    out.add(points.first())
    var accum = 0f
    var prev = points.first()
    var i = 1
    while (i < points.size && out.size < targetCount) {
      val cur = points[i]
      val d = dist(prev, cur)
      if (accum + d >= step && d > 0f) {
        val t = (step - accum) / d
        val np = StrokePoint(prev.x + t * (cur.x - prev.x), prev.y + t * (cur.y - prev.y))
        out.add(np)
        prev = np
        accum = 0f
      } else {
        accum += d
        prev = cur
        i++
      }
    }
    while (out.size < targetCount) out.add(points.last())
    return out
  }

  private fun rdp(points: List<StrokePoint>, epsilon: Float): List<StrokePoint> {
    if (points.size <= 2) return points
    var maxD = 0f
    var idx = 0
    val a = points.first()
    val b = points.last()
    for (i in 1 until points.lastIndex) {
      val d = perpDist(points[i], a, b)
      if (d > maxD) {
        maxD = d
        idx = i
      }
    }
    return if (maxD > epsilon) {
      rdp(points.subList(0, idx + 1), epsilon).dropLast(1) + rdp(points.subList(idx, points.size), epsilon)
    } else {
      listOf(a, b)
    }
  }

  private fun perpDist(p: StrokePoint, a: StrokePoint, b: StrokePoint): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val lenSq = dx * dx + dy * dy
    if (lenSq == 0f) return dist(p, a)
    val t = (((p.x - a.x) * dx + (p.y - a.y) * dy) / lenSq).coerceIn(0f, 1f)
    return hypot(p.x - (a.x + t * dx), p.y - (a.y + t * dy))
  }

  private fun cornerAngleDeg(a: StrokePoint, b: StrokePoint, c: StrokePoint): Float {
    val ux = a.x - b.x; val uy = a.y - b.y
    val vx = c.x - b.x; val vy = c.y - b.y
    val mag = (hypot(ux, uy) * hypot(vx, vy)).coerceAtLeast(1e-4f)
    val cosA = ((ux * vx + uy * vy) / mag).coerceIn(-1f, 1f)
    return Math.toDegrees(acos(cosA.toDouble())).toFloat()
  }

  private fun areParallel(a1: StrokePoint, a2: StrokePoint, b1: StrokePoint, b2: StrokePoint): Boolean {
    val angA = atan2((a2.y - a1.y).toDouble(), (a2.x - a1.x).toDouble())
    val angB = atan2((b2.y - b1.y).toDouble(), (b2.x - b1.x).toDouble())
    var diff = abs(Math.toDegrees(angA - angB)) % 180.0
    if (diff > 90.0) diff = 180.0 - diff
    return diff <= 16.0
  }

  private fun sampleEllipse(cx: Float, cy: Float, rx: Float, ry: Float, count: Int = 48): Contour {
    val pts = (0 until count).map { i ->
      val a = 2.0 * PI * i / count
      StrokePoint(cx + (rx * cos(a)).toFloat(), cy + (ry * sin(a)).toFloat())
    }
    return Contour(closed = true, points = pts)
  }

  private fun regularPolygon(bounds: Bounds, sides: Int): Contour {
    val cx = (bounds.minX + bounds.maxX) / 2f
    val cy = (bounds.minY + bounds.maxY) / 2f
    val r = min(bounds.maxX - bounds.minX, bounds.maxY - bounds.minY) / 2f
    val pts = (0 until sides).map { i ->
      val a = -PI / 2.0 + 2.0 * PI * i / sides
      StrokePoint(cx + (r * cos(a)).toFloat(), cy + (r * sin(a)).toFloat())
    }
    return Contour(closed = true, points = pts)
  }

  private data class Bounds(val minX: Float, val minY: Float, val maxX: Float, val maxY: Float)

  private fun computeBounds(points: List<StrokePoint>): Bounds {
    var minX = Float.POSITIVE_INFINITY
    var minY = Float.POSITIVE_INFINITY
    var maxX = Float.NEGATIVE_INFINITY
    var maxY = Float.NEGATIVE_INFINITY
    for (p in points) {
      if (p.x < minX) minX = p.x
      if (p.y < minY) minY = p.y
      if (p.x > maxX) maxX = p.x
      if (p.y > maxY) maxY = p.y
    }
    return Bounds(minX, minY, maxX, maxY)
  }

  private fun dist(a: StrokePoint, b: StrokePoint): Float = hypot(a.x - b.x, a.y - b.y)
}
