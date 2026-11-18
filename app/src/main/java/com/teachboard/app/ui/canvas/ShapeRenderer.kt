package com.teachboard.app.ui.canvas

import android.graphics.Canvas
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import com.teachboard.app.domain.model.Contour
import com.teachboard.app.domain.model.ToolType
import kotlin.math.PI
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Procedural shape renderer for all 45 shapes in PRD 18.2 + Smart Shapes (PRD 19.3 & 19.9).
 * Supports solid outline, dashed hidden 3D edges, fill color & alpha, and saved Contour geometry.
 */
object ShapeRenderer {

  private val strokePaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeCap = Paint.Cap.ROUND
      strokeJoin = Paint.Join.ROUND
    }
  }
  private val dashedPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeCap = Paint.Cap.ROUND
      strokeJoin = Paint.Join.ROUND
      pathEffect = DashPathEffect(floatArrayOf(10f, 8f), 0f)
    }
  }
  private val fillPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL }
  }

  fun drawContours(
    canvas: Canvas,
    contours: List<Contour>,
    color: Int,
    strokeWidth: Float,
    isFilled: Boolean,
    fillColor: Int?,
    alpha: Float
  ) {
    val aInt = (alpha.coerceIn(0.05f, 1f) * 255f).toInt()
    strokePaint.color = color
    strokePaint.alpha = aInt
    strokePaint.strokeWidth = strokeWidth.coerceAtLeast(2f)
    fillPaint.color = fillColor ?: color
    fillPaint.alpha = (aInt * 0.38f).toInt()

    for (c in contours) {
      if (c.points.size < 2) continue
      val path = Path().apply {
        moveTo(c.points[0].x, c.points[0].y)
        for (i in 1 until c.points.size) lineTo(c.points[i].x, c.points[i].y)
        if (c.closed) close()
      }
      if (isFilled && c.closed) canvas.drawPath(path, fillPaint)
      canvas.drawPath(path, strokePaint)
    }
  }

  fun drawShape(
    canvas: Canvas,
    type: ToolType,
    left: Float,
    top: Float,
    right: Float,
    bottom: Float,
    color: Int,
    strokeWidth: Float,
    isFilled: Boolean,
    fillColor: Int? = null,
    alpha: Float = 1f
  ) {
    val aInt = (alpha.coerceIn(0.05f, 1f) * 255f).toInt()
    strokePaint.color = color
    strokePaint.alpha = aInt
    strokePaint.strokeWidth = strokeWidth.coerceAtLeast(2f)
    dashedPaint.color = color
    dashedPaint.alpha = (aInt * 0.65f).toInt()
    dashedPaint.strokeWidth = (strokeWidth * 0.8f).coerceAtLeast(1.5f)
    fillPaint.color = fillColor ?: color
    fillPaint.alpha = (aInt * 0.36f).toInt()

    val rect = RectF(min(left, right), min(top, bottom), maxOf(left, right), maxOf(top, bottom))
    val w = rect.width().coerceAtLeast(4f)
    val h = rect.height().coerceAtLeast(4f)
    val cx = rect.centerX()
    val cy = rect.centerY()

    when (type) {
      ToolType.Line -> canvas.drawLine(left, top, right, bottom, strokePaint)
      ToolType.Arrow -> drawArrow(canvas, left, top, right, bottom, doubleEnded = false)
      ToolType.Rect, ToolType.FlowchartBox, ToolType.Quadrilateral -> {
        if (isFilled) canvas.drawRect(rect, fillPaint)
        canvas.drawRect(rect, strokePaint)
      }
      ToolType.Square -> {
        val side = min(w, h)
        val sq = RectF(cx - side / 2, cy - side / 2, cx + side / 2, cy + side / 2)
        if (isFilled) canvas.drawRect(sq, fillPaint)
        canvas.drawRect(sq, strokePaint)
      }
      ToolType.Circle -> {
        val r = min(w, h) / 2f
        if (isFilled) canvas.drawCircle(cx, cy, r, fillPaint)
        canvas.drawCircle(cx, cy, r, strokePaint)
      }
      ToolType.Oval -> {
        if (isFilled) canvas.drawOval(rect, fillPaint)
        canvas.drawOval(rect, strokePaint)
      }
      ToolType.Triangle, ToolType.IsoscelesTriangle -> drawPath(canvas, isFilled) {
        moveTo(cx, rect.top); lineTo(rect.right, rect.bottom); lineTo(rect.left, rect.bottom); close()
      }
      ToolType.RightTriangle -> drawPath(canvas, isFilled) {
        moveTo(rect.left, rect.top); lineTo(rect.left, rect.bottom); lineTo(rect.right, rect.bottom); close()
      }
      ToolType.Star -> drawStar(canvas, cx, cy, min(w, h) / 2f, isFilled)
      ToolType.Pentagon -> drawRegularPoly(canvas, cx, cy, min(w, h) / 2f, 5, isFilled)
      ToolType.Hexagon -> drawRegularPoly(canvas, cx, cy, min(w, h) / 2f, 6, isFilled)
      ToolType.Heptagon -> drawRegularPoly(canvas, cx, cy, min(w, h) / 2f, 7, isFilled)
      ToolType.Octagon -> drawRegularPoly(canvas, cx, cy, min(w, h) / 2f, 8, isFilled)
      ToolType.Diamond, ToolType.DecisionDiamond, ToolType.Rhombus -> drawPath(canvas, isFilled) {
        moveTo(cx, rect.top); lineTo(rect.right, cy); lineTo(cx, rect.bottom); lineTo(rect.left, cy); close()
      }
      ToolType.Kite -> drawPath(canvas, isFilled) {
        moveTo(cx, rect.top); lineTo(rect.right, rect.top + h * 0.35f)
        lineTo(cx, rect.bottom); lineTo(rect.left, rect.top + h * 0.35f); close()
      }
      ToolType.Parallelogram -> drawPath(canvas, isFilled) {
        val skew = w * 0.22f
        moveTo(rect.left + skew, rect.top); lineTo(rect.right, rect.top)
        lineTo(rect.right - skew, rect.bottom); lineTo(rect.left, rect.bottom); close()
      }
      ToolType.Trapezoid -> drawPath(canvas, isFilled) {
        val inset = w * 0.20f
        moveTo(rect.left + inset, rect.top); lineTo(rect.right - inset, rect.top)
        lineTo(rect.right, rect.bottom); lineTo(rect.left, rect.bottom); close()
      }
      ToolType.Heart -> drawPath(canvas, isFilled) {
        moveTo(cx, rect.bottom)
        cubicTo(rect.left - w * 0.05f, cy + h * 0.15f, rect.left, rect.top - h * 0.05f, cx, rect.top + h * 0.3f)
        cubicTo(rect.right, rect.top - h * 0.05f, rect.right + w * 0.05f, cy + h * 0.15f, cx, rect.bottom)
        close()
      }
      ToolType.Cross -> drawPath(canvas, isFilled) {
        val t = min(w, h) * 0.3f
        val x1 = cx - t / 2; val x2 = cx + t / 2; val y1 = cy - t / 2; val y2 = cy + t / 2
        moveTo(x1, rect.top); lineTo(x2, rect.top); lineTo(x2, y1); lineTo(rect.right, y1); lineTo(rect.right, y2)
        lineTo(x2, y2); lineTo(x2, rect.bottom); lineTo(x1, rect.bottom); lineTo(x1, y2); lineTo(rect.left, y2)
        lineTo(rect.left, y1); lineTo(x1, y1); close()
      }
      ToolType.Semicircle -> drawPath(canvas, isFilled) {
        arcTo(RectF(rect.left, rect.top, rect.right, rect.bottom + h), 180f, 180f); close()
      }
      ToolType.Sector -> drawPath(canvas, isFilled) {
        moveTo(rect.left, rect.bottom)
        arcTo(RectF(rect.left - w, rect.top, rect.right, rect.bottom + h), 270f, 90f); close()
      }
      ToolType.RightAngle -> {
        canvas.drawLine(rect.left, rect.top, rect.left, rect.bottom, strokePaint)
        canvas.drawLine(rect.left, rect.bottom, rect.right, rect.bottom, strokePaint)
        val sq = min(w, h) * 0.22f
        canvas.drawLine(rect.left, rect.bottom - sq, rect.left + sq, rect.bottom - sq, strokePaint)
        canvas.drawLine(rect.left + sq, rect.bottom - sq, rect.left + sq, rect.bottom, strokePaint)
      }
      // 3D Shapes with dashed hidden edges
      ToolType.Cube, ToolType.Cuboid -> drawBox3D(canvas, rect, type == ToolType.Cube, isFilled)
      ToolType.Cylinder -> drawCylinder(canvas, rect, isFilled)
      ToolType.Cone -> drawCone(canvas, rect, isFilled)
      ToolType.Sphere -> drawSphere(canvas, rect, isFilled)
      ToolType.Pyramid -> drawPyramid(canvas, rect, isFilled)
      ToolType.Torus -> drawTorus(canvas, rect, isFilled)
      ToolType.Prism -> drawPrism(canvas, rect, isFilled)
      ToolType.Hemisphere -> drawHemisphere(canvas, rect, isFilled)
      ToolType.Frustum -> drawFrustum(canvas, rect, isFilled)
      // Math & Science
      ToolType.XYAxes -> drawXYAxes(canvas, rect)
      ToolType.NumberLine -> drawNumberLine(canvas, rect)
      ToolType.Sine -> drawSine(canvas, rect)
      ToolType.Parabola -> drawParabola(canvas, rect)
      ToolType.DoubleArrow -> drawArrow(canvas, left, top, right, bottom, doubleEnded = true)
      ToolType.CurvedArrow -> drawCurvedArrow(canvas, rect)
      ToolType.Graph -> drawGraph(canvas, rect)
      // Diagram
      ToolType.Venn2 -> drawVenn2(canvas, rect, isFilled)
      ToolType.Venn3 -> drawVenn3(canvas, rect, isFilled)
      ToolType.RoundedBox -> {
        val rad = min(w, h) * 0.2f
        if (isFilled) canvas.drawRoundRect(rect, rad, rad, fillPaint)
        canvas.drawRoundRect(rect, rad, rad, strokePaint)
      }
      ToolType.ConnectorLine -> {
        val mx = (left + right) / 2f
        val p = Path().apply { moveTo(left, top); lineTo(mx, top); lineTo(mx, bottom); lineTo(right, bottom) }
        canvas.drawPath(p, strokePaint)
      }
      ToolType.ConnectorArrow -> {
        val mx = (left + right) / 2f
        val p = Path().apply { moveTo(left, top); lineTo(mx, top); lineTo(mx, bottom); lineTo(right, bottom) }
        canvas.drawPath(p, strokePaint); drawArrowHead(canvas, mx, bottom, right, bottom)
      }
      ToolType.Bracket -> {
        val p = Path().apply {
          moveTo(rect.right, rect.top)
          cubicTo(rect.left + w * 0.4f, rect.top, rect.right - w * 0.4f, cy, rect.left, cy)
          cubicTo(rect.right - w * 0.4f, cy, rect.left + w * 0.4f, rect.bottom, rect.right, rect.bottom)
        }
        canvas.drawPath(p, strokePaint)
      }
      else -> Unit
    }
  }

  private inline fun drawPath(canvas: Canvas, fill: Boolean, block: Path.() -> Unit) {
    val p = Path().apply(block)
    if (fill) canvas.drawPath(p, fillPaint)
    canvas.drawPath(p, strokePaint)
  }

  private fun drawRegularPoly(canvas: Canvas, cx: Float, cy: Float, r: Float, sides: Int, fill: Boolean) {
    drawPath(canvas, fill) {
      for (i in 0 until sides) {
        val a = -PI / 2.0 + 2.0 * PI * i / sides
        val x = cx + (r * cos(a)).toFloat(); val y = cy + (r * sin(a)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
      }
      close()
    }
  }

  private fun drawStar(canvas: Canvas, cx: Float, cy: Float, outerR: Float, fill: Boolean) {
    val innerR = outerR * 0.42f
    drawPath(canvas, fill) {
      for (i in 0 until 10) {
        val r = if (i % 2 == 0) outerR else innerR
        val a = -PI / 2.0 + PI * i / 5.0
        val x = cx + (r * cos(a)).toFloat(); val y = cy + (r * sin(a)).toFloat()
        if (i == 0) moveTo(x, y) else lineTo(x, y)
      }
      close()
    }
  }

  private fun drawArrow(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float, doubleEnded: Boolean) {
    canvas.drawLine(x1, y1, x2, y2, strokePaint)
    drawArrowHead(canvas, x1, y1, x2, y2)
    if (doubleEnded) drawArrowHead(canvas, x2, y2, x1, y1)
  }

  private fun drawArrowHead(canvas: Canvas, x1: Float, y1: Float, x2: Float, y2: Float) {
    val angle = atan2((y2 - y1).toDouble(), (x2 - x1).toDouble())
    val len = (strokePaint.strokeWidth * 3.2f).coerceIn(16f, 44f)
    val a1 = angle - Math.toRadians(28.0); val a2 = angle + Math.toRadians(28.0)
    canvas.drawLine(x2, y2, x2 - (len * cos(a1)).toFloat(), y2 - (len * sin(a1)).toFloat(), strokePaint)
    canvas.drawLine(x2, y2, x2 - (len * cos(a2)).toFloat(), y2 - (len * sin(a2)).toFloat(), strokePaint)
  }

  private fun drawBox3D(canvas: Canvas, raw: RectF, isCube: Boolean, fill: Boolean) {
    val rect = if (isCube) {
      val s = min(raw.width(), raw.height())
      RectF(raw.centerX() - s / 2, raw.centerY() - s / 2, raw.centerX() + s / 2, raw.centerY() + s / 2)
    } else raw
    val dx = rect.width() * 0.25f; val dy = rect.height() * 0.22f
    val f = RectF(rect.left, rect.top + dy, rect.right - dx, rect.bottom)
    val b = RectF(rect.left + dx, rect.top, rect.right, rect.bottom - dy)
    if (fill) canvas.drawRect(f, fillPaint)
    canvas.drawLine(b.left, b.top, b.left, b.bottom, dashedPaint)
    canvas.drawLine(b.left, b.bottom, b.right, b.bottom, dashedPaint)
    canvas.drawLine(f.left, f.bottom, b.left, b.bottom, dashedPaint)
    canvas.drawRect(f, strokePaint)
    canvas.drawLine(b.left, b.top, b.right, b.top, strokePaint)
    canvas.drawLine(b.right, b.top, b.right, b.bottom, strokePaint)
    canvas.drawLine(f.left, f.top, b.left, b.top, strokePaint)
    canvas.drawLine(f.right, f.top, b.right, b.top, strokePaint)
    canvas.drawLine(f.right, f.bottom, b.right, b.bottom, strokePaint)
  }

  private fun drawCylinder(canvas: Canvas, rect: RectF, fill: Boolean) {
    val ry = (rect.height() * 0.14f).coerceAtLeast(8f)
    val topO = RectF(rect.left, rect.top, rect.right, rect.top + ry * 2)
    val botO = RectF(rect.left, rect.bottom - ry * 2, rect.right, rect.bottom)
    if (fill) { canvas.drawRect(rect.left, rect.top + ry, rect.right, rect.bottom - ry, fillPaint); canvas.drawOval(topO, fillPaint) }
    canvas.drawOval(topO, strokePaint)
    canvas.drawLine(rect.left, rect.top + ry, rect.left, rect.bottom - ry, strokePaint)
    canvas.drawLine(rect.right, rect.top + ry, rect.right, rect.bottom - ry, strokePaint)
    canvas.drawArc(botO, 0f, 180f, false, strokePaint)
    canvas.drawArc(botO, 180f, 180f, false, dashedPaint)
  }

  private fun drawCone(canvas: Canvas, rect: RectF, fill: Boolean) {
    val ry = (rect.height() * 0.15f).coerceAtLeast(8f)
    val botO = RectF(rect.left, rect.bottom - ry * 2, rect.right, rect.bottom)
    if (fill) {
      val tri = Path().apply { moveTo(rect.centerX(), rect.top); lineTo(rect.right, rect.bottom - ry); lineTo(rect.left, rect.bottom - ry); close() }
      canvas.drawPath(tri, fillPaint); canvas.drawOval(botO, fillPaint)
    }
    canvas.drawLine(rect.centerX(), rect.top, rect.left, rect.bottom - ry, strokePaint)
    canvas.drawLine(rect.centerX(), rect.top, rect.right, rect.bottom - ry, strokePaint)
    canvas.drawArc(botO, 0f, 180f, false, strokePaint)
    canvas.drawArc(botO, 180f, 180f, false, dashedPaint)
  }

  private fun drawSphere(canvas: Canvas, rect: RectF, fill: Boolean) {
    val r = min(rect.width(), rect.height()) / 2f; val cx = rect.centerX(); val cy = rect.centerY()
    if (fill) canvas.drawCircle(cx, cy, r, fillPaint)
    canvas.drawCircle(cx, cy, r, strokePaint)
    val eq = RectF(cx - r, cy - r * 0.34f, cx + r, cy + r * 0.34f)
    canvas.drawArc(eq, 0f, 180f, false, strokePaint); canvas.drawArc(eq, 180f, 180f, false, dashedPaint)
  }

  private fun drawPyramid(canvas: Canvas, rect: RectF, fill: Boolean) {
    val ax = rect.centerX(); val ay = rect.top
    val flx = rect.left; val fly = rect.bottom
    val frx = rect.right - rect.width() * 0.22f; val fry = rect.bottom
    val brx = rect.right; val bry = rect.bottom - rect.height() * 0.24f
    val blx = rect.left + rect.width() * 0.22f
    if (fill) {
      val p = Path().apply { moveTo(ax, ay); lineTo(flx, fly); lineTo(frx, fry); lineTo(brx, bry); close() }
      canvas.drawPath(p, fillPaint)
    }
    canvas.drawLine(blx, bry, flx, fly, dashedPaint); canvas.drawLine(blx, bry, brx, bry, dashedPaint); canvas.drawLine(ax, ay, blx, bry, dashedPaint)
    canvas.drawLine(ax, ay, flx, fly, strokePaint); canvas.drawLine(ax, ay, frx, fry, strokePaint); canvas.drawLine(ax, ay, brx, bry, strokePaint)
    canvas.drawLine(flx, fly, frx, fry, strokePaint); canvas.drawLine(frx, fry, brx, bry, strokePaint)
  }

  private fun drawTorus(canvas: Canvas, rect: RectF, fill: Boolean) {
    if (fill) canvas.drawOval(rect, fillPaint)
    canvas.drawOval(rect, strokePaint)
    val hole = RectF(rect.left + rect.width() * 0.26f, rect.top + rect.height() * 0.34f, rect.right - rect.width() * 0.26f, rect.bottom - rect.height() * 0.34f)
    canvas.drawArc(hole, 15f, 150f, false, strokePaint)
    canvas.drawArc(RectF(hole.left, hole.top + hole.height() * 0.25f, hole.right, hole.bottom + hole.height() * 0.25f), 200f, 140f, false, strokePaint)
  }

  private fun drawPrism(canvas: Canvas, rect: RectF, fill: Boolean) {
    val dx = rect.width() * 0.28f; val dy = rect.height() * 0.22f
    val ftx = rect.left + (rect.width() - dx) / 2f; val fty = rect.top + dy
    val flx = rect.left; val fly = rect.bottom; val frx = rect.right - dx
    val btx = ftx + dx; val bty = rect.top; val blx = flx + dx; val bly = rect.bottom - dy; val brx = rect.right
    if (fill) {
      val p = Path().apply { moveTo(ftx, fty); lineTo(flx, fly); lineTo(frx, fly); lineTo(brx, bly); lineTo(btx, bty); close() }
      canvas.drawPath(p, fillPaint)
    }
    canvas.drawLine(blx, bly, flx, fly, dashedPaint); canvas.drawLine(blx, bly, btx, bty, dashedPaint); canvas.drawLine(blx, bly, brx, bly, dashedPaint)
    canvas.drawLine(ftx, fty, flx, fly, strokePaint); canvas.drawLine(flx, fly, frx, fly, strokePaint); canvas.drawLine(frx, fly, ftx, fty, strokePaint)
    canvas.drawLine(ftx, fty, btx, bty, strokePaint); canvas.drawLine(frx, fly, brx, bly, strokePaint); canvas.drawLine(btx, bty, brx, bly, strokePaint)
  }

  private fun drawHemisphere(canvas: Canvas, rect: RectF, fill: Boolean) {
    val r = min(rect.width(), rect.height() * 1.6f) / 2f; val cx = rect.centerX(); val baseY = rect.bottom - r * 0.35f
    val dome = RectF(cx - r, baseY - r, cx + r, baseY + r)
    val base = RectF(cx - r, baseY - r * 0.32f, cx + r, baseY + r * 0.32f)
    if (fill) { canvas.drawArc(dome, 180f, 180f, true, fillPaint); canvas.drawOval(base, fillPaint) }
    canvas.drawArc(dome, 180f, 180f, false, strokePaint)
    canvas.drawArc(base, 0f, 180f, false, strokePaint); canvas.drawArc(base, 180f, 180f, false, dashedPaint)
  }

  private fun drawFrustum(canvas: Canvas, rect: RectF, fill: Boolean) {
    val inset = rect.width() * 0.22f
    val ryt = (rect.height() * 0.10f).coerceAtLeast(6f); val ryb = (rect.height() * 0.14f).coerceAtLeast(8f)
    val tO = RectF(rect.left + inset, rect.top, rect.right - inset, rect.top + ryt * 2)
    val bO = RectF(rect.left, rect.bottom - ryb * 2, rect.right, rect.bottom)
    if (fill) { canvas.drawOval(tO, fillPaint); canvas.drawOval(bO, fillPaint) }
    canvas.drawOval(tO, strokePaint)
    canvas.drawLine(tO.left, tO.centerY(), bO.left, bO.centerY(), strokePaint)
    canvas.drawLine(tO.right, tO.centerY(), bO.right, bO.centerY(), strokePaint)
    canvas.drawArc(bO, 0f, 180f, false, strokePaint); canvas.drawArc(bO, 180f, 180f, false, dashedPaint)
  }

  private fun drawXYAxes(canvas: Canvas, rect: RectF) {
    val cx = rect.centerX(); val cy = rect.centerY()
    drawArrow(canvas, rect.left, cy, rect.right, cy, true)
    drawArrow(canvas, cx, rect.bottom, cx, rect.top, true)
    val t = min(rect.width(), rect.height()) * 0.03f
    for (i in -3..3) if (i != 0) {
      canvas.drawLine(cx + i * rect.width() / 8f, cy - t, cx + i * rect.width() / 8f, cy + t, strokePaint)
      canvas.drawLine(cx - t, cy + i * rect.height() / 8f, cx + t, cy + i * rect.height() / 8f, strokePaint)
    }
  }

  private fun drawNumberLine(canvas: Canvas, rect: RectF) {
    val cy = rect.centerY()
    drawArrow(canvas, rect.left, cy, rect.right, cy, true)
    val th = (rect.height() * 0.16f).coerceIn(8f, 22f)
    for (i in 1 until 8) canvas.drawLine(rect.left + rect.width() * i / 8f, cy - th, rect.left + rect.width() * i / 8f, cy + th, strokePaint)
  }

  private fun drawSine(canvas: Canvas, rect: RectF) {
    val cy = rect.centerY(); val amp = rect.height() * 0.44f; val p = Path()
    for (i in 0..64) {
      val t = i / 64f; val x = rect.left + t * rect.width(); val y = cy - (sin(t * 2.0 * PI) * amp).toFloat()
      if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    canvas.drawLine(rect.left, cy, rect.right, cy, dashedPaint); canvas.drawPath(p, strokePaint)
  }

  private fun drawParabola(canvas: Canvas, rect: RectF) {
    val p = Path()
    for (i in 0..48) {
      val u = (i / 48f) * 2f - 1f
      val x = rect.centerX() + u * rect.width() / 2f; val y = rect.bottom - u * u * rect.height()
      if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
    }
    canvas.drawPath(p, strokePaint)
  }

  private fun drawCurvedArrow(canvas: Canvas, rect: RectF) {
    val p = Path().apply { moveTo(rect.left, rect.bottom); quadTo(rect.left, rect.top, rect.right, rect.top + rect.height() * 0.25f) }
    canvas.drawPath(p, strokePaint); drawArrowHead(canvas, rect.centerX(), rect.top, rect.right, rect.top + rect.height() * 0.25f)
  }

  private fun drawGraph(canvas: Canvas, rect: RectF) {
    val g = Paint(dashedPaint).apply { pathEffect = null; alpha = 85 }
    for (i in 0..6) {
      val x = rect.left + rect.width() * i / 6f; val y = rect.top + rect.height() * i / 6f
      canvas.drawLine(x, rect.top, x, rect.bottom, g); canvas.drawLine(rect.left, y, rect.right, y, g)
    }
    drawXYAxes(canvas, rect)
  }

  private fun drawVenn2(canvas: Canvas, rect: RectF, fill: Boolean) {
    val r = min(rect.width() * 0.32f, rect.height() * 0.46f); val cy = rect.centerY()
    val c1 = rect.centerX() - r * 0.55f; val c2 = rect.centerX() + r * 0.55f
    if (fill) { canvas.drawCircle(c1, cy, r, fillPaint); canvas.drawCircle(c2, cy, r, fillPaint) }
    canvas.drawCircle(c1, cy, r, strokePaint); canvas.drawCircle(c2, cy, r, strokePaint)
  }

  private fun drawVenn3(canvas: Canvas, rect: RectF, fill: Boolean) {
    val r = min(rect.width(), rect.height()) * 0.30f; val cx = rect.centerX(); val cy = rect.centerY()
    listOf(cx to cy - r * 0.55f, cx - r * 0.52f to cy + r * 0.38f, cx + r * 0.52f to cy + r * 0.38f).forEach { (x, y) ->
      if (fill) canvas.drawCircle(x, y, r, fillPaint)
      canvas.drawCircle(x, y, r, strokePaint)
    }
  }
}
