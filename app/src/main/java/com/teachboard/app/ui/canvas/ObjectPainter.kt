package com.teachboard.app.ui.canvas

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.RectF
import android.graphics.Typeface
import com.teachboard.app.domain.formula.FormulaParser
import com.teachboard.app.domain.model.CanvasObject
import com.teachboard.app.domain.model.Contour
import com.teachboard.app.domain.model.EraserSwipe
import com.teachboard.app.domain.model.Serialization
import com.teachboard.app.domain.model.StrokePoint
import com.teachboard.app.domain.model.ToolType
import com.teachboard.app.domain.stickers.StickerCatalog
import com.teachboard.app.ui.canvas.geometry.GeoStampPath
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.hypot
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sin

/**
 * Shared canvas painter for live view, cached bitmap, PNG export, and PDF export (PRD 19.3).
 * All Android Paint/Path instances are lazy so pure-math helpers remain JVM unit-test safe.
 */
object ObjectPainter {

  private val strokePaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeCap = Paint.Cap.ROUND
      strokeJoin = Paint.Join.ROUND
    }
  }

  private val eraserPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeCap = Paint.Cap.ROUND
      strokeJoin = Paint.Join.ROUND
      xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }
  }

  private val textPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
    }
  }

  private val tableBorderPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeWidth = 2.5f
    }
  }

  private val tableHeaderPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
    }
  }

  private val bitmapPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG)
  }

  private val multiplyMode by lazy { PorterDuffXfermode(PorterDuff.Mode.MULTIPLY) }

  fun drawObject(
    canvas: Canvas,
    obj: CanvasObject,
    isDarkBoard: Boolean,
    imageResolver: (String) -> Bitmap?
  ) {
    val hasPerObjectErasers = obj.eraserStrokes.isNotEmpty()
    val saveCount = if (hasPerObjectErasers) {
      val b = getBounds(obj)
      b.inset(-40f, -40f)
      canvas.saveLayer(b, null)
    } else -1

    try {
      when {
        obj.isEraser || obj.type == ToolType.Eraser -> {
          drawEraserStroke(canvas, obj.points, obj.strokeWidth * 4f)
        }
        obj.geometry.isNotEmpty() -> {
          ShapeRenderer.drawContours(
            canvas = canvas,
            contours = obj.geometry,
            color = obj.color,
            strokeWidth = obj.strokeWidth,
            isFilled = obj.isFilled,
            fillColor = obj.fillColor,
            alpha = obj.alpha
          )
          if (obj.type == ToolType.RightAngle || (!obj.textContent.isNullOrBlank() && obj.geometry.size >= 2)) {
            drawAngleLabel(canvas, obj, isDarkBoard)
          }
        }
        obj.type.isBrush -> {
          drawStrokePoints(
            canvas = canvas,
            tool = obj.type,
            points = obj.points,
            color = obj.color,
            strokeWidth = obj.strokeWidth,
            alpha = obj.alpha,
            isDarkBoard = isDarkBoard
          )
        }
        obj.type.isShape -> {
          val (l, t, r, b) = twoPointRect(obj.points)
          ShapeRenderer.drawShape(
            canvas = canvas,
            type = obj.type,
            left = l,
            top = t,
            right = r,
            bottom = b,
            color = obj.color,
            strokeWidth = obj.strokeWidth,
            isFilled = obj.isFilled,
            fillColor = obj.fillColor,
            alpha = obj.alpha
          )
        }
        obj.type == ToolType.Text -> drawTextObject(canvas, obj)
        obj.type == ToolType.Formula -> drawFormulaObject(canvas, obj)
        obj.type == ToolType.Table -> drawTableObject(canvas, obj, isDarkBoard)
        obj.type == ToolType.Image -> drawImageObject(canvas, obj, imageResolver, isDarkBoard)
        obj.type == ToolType.Sticker -> drawStickerObject(canvas, obj)
      }

      if (hasPerObjectErasers) {
        for (swipe in obj.eraserStrokes) {
          drawEraserStroke(canvas, swipe.points, swipe.width)
        }
      }
    } finally {
      if (saveCount >= 0) canvas.restoreToCount(saveCount)
    }
  }

  fun drawStrokePoints(
    canvas: Canvas,
    tool: ToolType,
    points: List<StrokePoint>,
    color: Int,
    strokeWidth: Float,
    alpha: Float,
    isDarkBoard: Boolean
  ) {
    if (points.isEmpty()) return
    if (tool == ToolType.Eraser) {
      drawEraserStroke(canvas, points, strokeWidth * 4f)
      return
    }

    val p = strokePaint
    p.reset()
    p.isAntiAlias = true
    p.style = Paint.Style.STROKE
    p.color = color
    val effectiveAlpha = when (tool) {
      ToolType.Highlighter -> (alpha.coerceIn(0.1f, 1f) * 0.38f * 255f).toInt()
      ToolType.Marker -> (alpha.coerceIn(0.1f, 1f) * 0.85f * 255f).toInt()
      else -> (alpha.coerceIn(0.05f, 1f) * 255f).toInt()
    }
    p.alpha = effectiveAlpha

    when (tool) {
      ToolType.Highlighter -> {
        p.strokeCap = Paint.Cap.SQUARE
        p.strokeJoin = Paint.Join.BEVEL
        p.strokeWidth = (strokeWidth * 2.2f).coerceAtLeast(10f)
        p.xfermode = if (isDarkBoard) null else multiplyMode
        drawSmoothPath(canvas, points, p)
        p.xfermode = null
      }
      ToolType.Marker -> {
        p.strokeCap = Paint.Cap.SQUARE
        p.strokeJoin = Paint.Join.ROUND
        p.strokeWidth = (strokeWidth * 1.45f).coerceAtLeast(4f)
        drawSmoothPath(canvas, points, p)
      }
      ToolType.Brush -> {
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        p.strokeWidth = (strokeWidth * 1.25f).coerceAtLeast(3f)
        val blur = (strokeWidth * 0.35f).coerceIn(1.5f, 14f)
        p.maskFilter = BlurMaskFilter(blur, BlurMaskFilter.Blur.NORMAL)
        drawSmoothPath(canvas, points, p)
        p.maskFilter = null
      }
      ToolType.Pencil -> {
        FreehandStrokeRenderer.drawPencilStroke(canvas, points, color, effectiveAlpha, strokeWidth)
      }
      ToolType.Calligraphy -> {
        drawCalligraphyStroke(canvas, points, color, effectiveAlpha, strokeWidth)
      }
      else -> {
        p.strokeCap = Paint.Cap.ROUND
        p.strokeJoin = Paint.Join.ROUND
        p.strokeWidth = strokeWidth.coerceAtLeast(1.5f)
        drawSmoothPath(canvas, points, p)
      }
    }
  }

  fun drawEraserStroke(canvas: Canvas, points: List<StrokePoint>, width: Float) {
    if (points.isEmpty()) return
    eraserPaint.strokeWidth = width.coerceAtLeast(12f)
    if (points.size == 1) {
      canvas.drawPoint(points[0].x, points[0].y, eraserPaint)
      return
    }
    val path = buildSmoothPath(points)
    canvas.drawPath(path, eraserPaint)
  }

  private fun drawSmoothPath(canvas: Canvas, points: List<StrokePoint>, paint: Paint) {
    if (points.size == 1) {
      val prevStyle = paint.style
      paint.style = Paint.Style.FILL
      canvas.drawCircle(points[0].x, points[0].y, paint.strokeWidth / 2f, paint)
      paint.style = prevStyle
      return
    }
    val path = buildSmoothPath(points)
    canvas.drawPath(path, paint)
  }

  private fun buildSmoothPath(points: List<StrokePoint>): Path {
    val path = Path()
    path.moveTo(points[0].x, points[0].y)
    if (points.size == 2) {
      path.lineTo(points[1].x, points[1].y)
      return path
    }
    for (i in 1 until points.lastIndex) {
      val xc = (points[i].x + points[i + 1].x) / 2f
      val yc = (points[i].y + points[i + 1].y) / 2f
      path.quadTo(points[i].x, points[i].y, xc, yc)
    }
    val last = points.last()
    path.lineTo(last.x, last.y)
    return path
  }

  private fun drawCalligraphyStroke(
    canvas: Canvas,
    points: List<StrokePoint>,
    color: Int,
    alphaInt: Int,
    baseWidth: Float
  ) {
    val p = strokePaint
    p.color = color
    p.alpha = alphaInt
    p.strokeCap = Paint.Cap.ROUND
    p.strokeJoin = Paint.Join.ROUND
    if (points.size == 1) {
      p.strokeWidth = baseWidth
      canvas.drawPoint(points[0].x, points[0].y, p)
      return
    }
    val nibAngle = PI / 4.0
    for (i in 0 until points.lastIndex) {
      val a = points[i]
      val b = points[i + 1]
      val segAngle = atan2((b.y - a.y).toDouble(), (b.x - a.x).toDouble())
      val factor = 0.25 + 0.95 * abs(sin(segAngle - nibAngle))
      p.strokeWidth = (baseWidth * factor.toFloat()).coerceAtLeast(1.5f)
      canvas.drawLine(a.x, a.y, b.x, b.y, p)
    }
  }

  private fun drawTextObject(canvas: Canvas, obj: CanvasObject) {
    val meta = Serialization.decodeRichText(obj.textContent)
    if (meta.text.isBlank()) return
    val origin = obj.points.firstOrNull() ?: StrokePoint(120f, 160f)
    val style = when {
      meta.bold && meta.italic -> Typeface.BOLD_ITALIC
      meta.bold -> Typeface.BOLD
      meta.italic -> Typeface.ITALIC
      else -> Typeface.NORMAL
    }
    val family = when (meta.fontFamily.uppercase()) {
      "SERIF" -> Typeface.SERIF
      "MONO" -> Typeface.MONOSPACE
      else -> Typeface.SANS_SERIF
    }
    textPaint.color = obj.color
    textPaint.alpha = (obj.alpha.coerceIn(0.1f, 1f) * 255f).toInt()
    textPaint.textSize = obj.fontSize.coerceIn(12f, 220f)
    textPaint.typeface = Typeface.create(family, style)
    textPaint.textAlign = when (meta.align.uppercase()) {
      "CENTER" -> Paint.Align.CENTER
      "END" -> Paint.Align.RIGHT
      else -> Paint.Align.LEFT
    }

    val lines = meta.text.split('\n')
    val lineHeight = textPaint.fontSpacing
    val bounds = getBounds(obj)
    val drawX = when (meta.align.uppercase()) {
      "CENTER" -> bounds.centerX()
      "END" -> bounds.right
      else -> origin.x
    }
    var y = origin.y - textPaint.fontMetrics.ascent
    for (line in lines) {
      canvas.drawText(line, drawX, y, textPaint)
      y += lineHeight
    }
  }

  private fun drawFormulaObject(canvas: Canvas, obj: CanvasObject) {
    val latex = obj.textContent?.takeIf { it.isNotBlank() } ?: return
    val bounds = getBounds(obj)
    canvas.save()
    canvas.translate(bounds.left, bounds.top)
    FormulaParser.drawFormula(
      canvas = canvas,
      latex = latex,
      color = obj.color,
      fontSizePx = obj.fontSize.coerceIn(16f, 180f),
      boxWidth = bounds.width().coerceAtLeast(80f),
      boxHeight = bounds.height().coerceAtLeast(48f)
    )
    canvas.restore()
  }

  private fun drawTableObject(canvas: Canvas, obj: CanvasObject, isDarkBoard: Boolean) {
    val (rows, cols, grid) = Serialization.decodeTablePayload(obj.textContent)
    val bounds = getBounds(obj)
    val cellW = bounds.width() / cols.coerceAtLeast(1)
    val cellH = bounds.height() / rows.coerceAtLeast(1)

    tableHeaderPaint.color = obj.fillColor ?: obj.color
    tableHeaderPaint.alpha = if (isDarkBoard) 55 else 34
    canvas.drawRect(bounds.left, bounds.top, bounds.right, bounds.top + cellH, tableHeaderPaint)

    tableBorderPaint.color = obj.color
    tableBorderPaint.alpha = (obj.alpha * 255f).toInt().coerceIn(40, 255)
    tableBorderPaint.strokeWidth = obj.strokeWidth.coerceIn(1.5f, 8f)
    canvas.drawRect(bounds, tableBorderPaint)

    for (r in 1 until rows) {
      val y = bounds.top + r * cellH
      canvas.drawLine(bounds.left, y, bounds.right, y, tableBorderPaint)
    }
    for (c in 1 until cols) {
      val x = bounds.left + c * cellW
      canvas.drawLine(x, bounds.top, x, bounds.bottom, tableBorderPaint)
    }

    textPaint.color = obj.color
    textPaint.alpha = (obj.alpha * 255f).toInt().coerceIn(80, 255)
    textPaint.textSize = (min(cellH * 0.42f, obj.fontSize)).coerceIn(14f, 48f)
    textPaint.textAlign = Paint.Align.CENTER
    val fm = textPaint.fontMetrics
    val textOffset = -(fm.ascent + fm.descent) / 2f

    for (r in 0 until rows) {
      textPaint.typeface = Typeface.create(
        Typeface.SANS_SERIF,
        if (r == 0) Typeface.BOLD else Typeface.NORMAL
      )
      for (c in 0 until cols) {
        val cellText = grid.getOrNull(r)?.getOrNull(c).orEmpty()
        if (cellText.isNotEmpty()) {
          val cx = bounds.left + (c + 0.5f) * cellW
          val cy = bounds.top + (r + 0.5f) * cellH + textOffset
          canvas.save()
          canvas.clipRect(
            bounds.left + c * cellW + 4f,
            bounds.top + r * cellH + 2f,
            bounds.left + (c + 1) * cellW - 4f,
            bounds.top + (r + 1) * cellH - 2f
          )
          canvas.drawText(cellText, cx, cy, textPaint)
          canvas.restore()
        }
      }
    }
  }

  private fun drawImageObject(
    canvas: Canvas,
    obj: CanvasObject,
    imageResolver: (String) -> Bitmap?,
    isDarkBoard: Boolean
  ) {
    val path = obj.imagePath ?: return
    val bmp = imageResolver(path) ?: return
    if (bmp.isRecycled) return
    val bounds = getBounds(obj)
    bitmapPaint.alpha = (obj.alpha.coerceIn(0.1f, 1f) * 255f).toInt()
    canvas.drawBitmap(bmp, null, bounds, bitmapPaint)

    val attribution = obj.textContent?.takeIf { it.isNotBlank() }
    if (attribution != null) {
      textPaint.color = if (isDarkBoard) 0xCCFFFFFF.toInt() else 0xAA374151.toInt()
      textPaint.textSize = 18f
      textPaint.typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.NORMAL)
      textPaint.textAlign = Paint.Align.LEFT
      canvas.drawText(attribution, bounds.left + 4f, bounds.bottom + 20f, textPaint)
    }
  }

  private fun drawStickerObject(canvas: Canvas, obj: CanvasObject) {
    val stickerId = obj.textContent ?: return
    val bounds = getBounds(obj)
    canvas.save()
    canvas.translate(bounds.left, bounds.top)
    StickerCatalog.drawSticker(
      canvas = canvas,
      stickerId = stickerId,
      color = obj.color,
      width = bounds.width().coerceAtLeast(40f),
      height = bounds.height().coerceAtLeast(40f)
    )
    canvas.restore()
  }

  private fun drawAngleLabel(canvas: Canvas, obj: CanvasObject, isDarkBoard: Boolean) {
    if (obj.geometry.isEmpty()) return
    val rays = obj.geometry.getOrNull(0)?.points ?: return
    if (rays.size < 3) return
    val baseEnd = rays[0]
    val pivot = rays[1]
    val armEnd = rays[2]

    val a0 = Math.toDegrees(atan2((baseEnd.y - pivot.y).toDouble(), (baseEnd.x - pivot.x).toDouble())).toFloat()
    val a1 = Math.toDegrees(atan2((armEnd.y - pivot.y).toDouble(), (armEnd.x - pivot.x).toDouble())).toFloat()
    val sweep = GeoStampPath.normSweep(a1 - a0)
    val deg = abs(sweep).roundToInt()
    val text = obj.textContent?.takeIf { it.isNotBlank() } ?: "$deg°"

    // Angle bisector ray between the base ray and arm ray
    val bisectorRad = Math.toRadians((a0 + sweep / 2.0))

    // Determine arc radius
    val indicatorPts = obj.geometry.getOrNull(1)?.points
    val arcR = if (!indicatorPts.isNullOrEmpty()) {
      hypot(indicatorPts[0].x - pivot.x, indicatorPts[0].y - pivot.y)
    } else {
      hypot(armEnd.x - pivot.x, armEnd.y - pivot.y) * 0.35f
    }.coerceAtLeast(18f)

    val fontSize = (if (obj.fontSize > 0) obj.fontSize else (arcR * 0.42f)).coerceIn(16f, 26f)

    // For acute angles (< 35°), push label slightly further out along bisector so it doesn't touch the rays
    val offsetFactor = if (deg < 35) 1.35f else 0.95f
    val labelDist = arcR + fontSize * offsetFactor
    val lx = pivot.x + (cos(bisectorRad) * labelDist).toFloat()
    val ly = pivot.y + (sin(bisectorRad) * labelDist).toFloat()

    val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textSize = fontSize
      typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
      textAlign = Paint.Align.CENTER
      color = if (isDarkBoard && (obj.color == 0xFF000000.toInt() || obj.color == 0xFF1E2024.toInt())) {
        0xFFEEEEEE.toInt()
      } else {
        obj.color
      }
    }

    val fm = textPaint.fontMetrics
    val textY = ly - (fm.ascent + fm.descent) / 2f

    // Crisp high-contrast halo so text is always 100% readable over background/lines
    val haloPaint = Paint(textPaint).apply {
      style = Paint.Style.STROKE
      strokeWidth = 3.5f
      color = if (isDarkBoard) 0xDD1E2024.toInt() else 0xDDFFFFFF.toInt()
    }

    canvas.drawText(text, lx, textY, haloPaint)
    canvas.drawText(text, lx, textY, textPaint)
  }

  private fun twoPointRect(points: List<StrokePoint>): Quad {
    if (points.isEmpty()) return Quad(0f, 0f, 120f, 120f)
    if (points.size == 1) return Quad(points[0].x, points[0].y, points[0].x + 120f, points[0].y + 120f)
    val a = points.first()
    val b = points.last()
    return Quad(a.x, a.y, b.x, b.y)
  }

  private data class Quad(val l: Float, val t: Float, val r: Float, val b: Float)

  fun getBounds(obj: CanvasObject): RectF {
    if (obj.geometry.isNotEmpty()) {
      var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
      var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
      for (c in obj.geometry) {
        for (p in c.points) {
          if (p.x < minX) minX = p.x; if (p.y < minY) minY = p.y
          if (p.x > maxX) maxX = p.x; if (p.y > maxY) maxY = p.y
        }
      }
      if (minX <= maxX) return RectF(minX, minY, maxX, maxY)
    }

    if (obj.type == ToolType.Text) {
      val origin = obj.points.firstOrNull() ?: StrokePoint(120f, 160f)
      val meta = Serialization.decodeRichText(obj.textContent)
      val lines = meta.text.split('\n')
      val maxChars = lines.maxOfOrNull { it.length }?.coerceAtLeast(1) ?: 1
      val estW = max(obj.fontSize * 0.62f * maxChars + 24f, 80f)
      val estH = max(lines.size * obj.fontSize * 1.28f + 16f, obj.fontSize * 1.3f)
      return if (obj.points.size >= 2) {
        val b = obj.points.last()
        RectF(min(origin.x, b.x), min(origin.y, b.y), max(origin.x, b.x).coerceAtLeast(origin.x + estW), max(origin.y, b.y).coerceAtLeast(origin.y + estH))
      } else {
        RectF(origin.x, origin.y, origin.x + estW, origin.y + estH)
      }
    }

    if (obj.type == ToolType.Formula) {
      val origin = obj.points.firstOrNull() ?: StrokePoint(120f, 160f)
      return if (obj.points.size >= 2) {
        val b = obj.points.last()
        RectF(min(origin.x, b.x), min(origin.y, b.y), max(origin.x, b.x), max(origin.y, b.y))
      } else {
        val (w, h) = FormulaParser.measure(obj.textContent.orEmpty(), obj.fontSize)
        RectF(origin.x, origin.y, origin.x + w, origin.y + h)
      }
    }

    if (obj.points.isEmpty()) return RectF(0f, 0f, 0f, 0f)
    var minX = Float.MAX_VALUE; var minY = Float.MAX_VALUE
    var maxX = -Float.MAX_VALUE; var maxY = -Float.MAX_VALUE
    for (p in obj.points) {
      if (p.x < minX) minX = p.x; if (p.y < minY) minY = p.y
      if (p.x > maxX) maxX = p.x; if (p.y > maxY) maxY = p.y
    }
    val pad = (obj.strokeWidth / 2f).coerceAtLeast(4f)
    return RectF(minX - pad, minY - pad, maxX + pad, maxY + pad)
  }

  fun hitTest(obj: CanvasObject, x: Float, y: Float, tolerance: Float = 24f): Boolean {
    if (obj.isEraser || obj.type == ToolType.Eraser) return false
    val b = getBounds(obj)
    val expanded = RectF(b.left - tolerance, b.top - tolerance, b.right + tolerance, b.bottom + tolerance)
    if (!expanded.contains(x, y)) return false
    if (obj.type.isBrush && obj.points.size >= 2) {
      val r = max(tolerance, obj.strokeWidth)
      for (i in 0 until obj.points.lastIndex) {
        if (pointToSegmentDistance(x, y, obj.points[i], obj.points[i + 1]) <= r) return true
      }
      return false
    }
    return true
  }

  fun translate(obj: CanvasObject, dx: Float, dy: Float): CanvasObject {
    return obj.copy(
      points = obj.points.map { it.copy(x = it.x + dx, y = it.y + dy) },
      geometry = obj.geometry.map { c ->
        Contour(c.closed, c.points.map { it.copy(x = it.x + dx, y = it.y + dy) })
      },
      eraserStrokes = obj.eraserStrokes.map { s ->
        EraserSwipe(s.width, s.points.map { it.copy(x = it.x + dx, y = it.y + dy) })
      },
      updatedAt = System.currentTimeMillis()
    )
  }

  fun scale(obj: CanvasObject, anchorX: Float, anchorY: Float, factor: Float): CanvasObject {
    val f = factor.coerceIn(0.25f, 4.0f)
    return obj.copy(
      points = obj.points.map {
        it.copy(
          x = anchorX + (it.x - anchorX) * f,
          y = anchorY + (it.y - anchorY) * f
        )
      },
      geometry = obj.geometry.map { c ->
        Contour(
          c.closed,
          c.points.map {
            it.copy(
              x = anchorX + (it.x - anchorX) * f,
              y = anchorY + (it.y - anchorY) * f
            )
          }
        )
      },
      fontSize = (obj.fontSize * f).coerceIn(14f, 200f),
      updatedAt = System.currentTimeMillis()
    )
  }

  fun rotate(obj: CanvasObject, centerX: Float, centerY: Float, degrees: Float): CanvasObject {
    val rad = Math.toRadians(degrees.toDouble())
    val c = kotlin.math.cos(rad).toFloat()
    val s = kotlin.math.sin(rad).toFloat()
    fun rotPt(p: StrokePoint): StrokePoint {
      val dx = p.x - centerX
      val dy = p.y - centerY
      return p.copy(x = centerX + dx * c - dy * s, y = centerY + dx * s + dy * c)
    }
    return obj.copy(
      points = obj.points.map(::rotPt),
      geometry = obj.geometry.map { contour -> Contour(contour.closed, contour.points.map(::rotPt)) },
      updatedAt = System.currentTimeMillis()
    )
  }

  fun scaleAxes(obj: CanvasObject, anchorX: Float, anchorY: Float, sx: Float, sy: Float): CanvasObject {
    val fx = sx.coerceIn(0.25f, 4.0f)
    val fy = sy.coerceIn(0.25f, 4.0f)
    fun scalePt(p: StrokePoint): StrokePoint = p.copy(
      x = anchorX + (p.x - anchorX) * fx,
      y = anchorY + (p.y - anchorY) * fy
    )
    return obj.copy(
      points = obj.points.map(::scalePt),
      geometry = obj.geometry.map { contour -> Contour(contour.closed, contour.points.map(::scalePt)) },
      fontSize = (obj.fontSize * ((fx + fy) / 2f)).coerceIn(14f, 200f),
      updatedAt = System.currentTimeMillis()
    )
  }

  fun flipHorizontal(obj: CanvasObject, centerX: Float): CanvasObject {
    fun flipPt(p: StrokePoint): StrokePoint = p.copy(x = 2f * centerX - p.x)
    return obj.copy(
      points = obj.points.map(::flipPt),
      geometry = obj.geometry.map { contour -> Contour(contour.closed, contour.points.map(::flipPt)) },
      updatedAt = System.currentTimeMillis()
    )
  }

  /**
   * Douglas-Peucker polyline simplification to keep stored stroke size compact (PRD 19.6).
   */
  fun simplifyPoints(points: List<StrokePoint>, epsilon: Float = 1.6f): List<StrokePoint> {
    if (points.size <= 3) return points
    var maxDist = 0f
    var index = 0
    val first = points.first()
    val last = points.last()
    for (i in 1 until points.lastIndex) {
      val d = pointToSegmentDistance(points[i].x, points[i].y, first, last)
      if (d > maxDist) {
        maxDist = d
        index = i
      }
    }
    return if (maxDist > epsilon) {
      val left = simplifyPoints(points.subList(0, index + 1), epsilon)
      val right = simplifyPoints(points.subList(index, points.size), epsilon)
      left.dropLast(1) + right
    } else {
      listOf(first, last)
    }
  }

  private fun pointToSegmentDistance(px: Float, py: Float, a: StrokePoint, b: StrokePoint): Float {
    val dx = b.x - a.x
    val dy = b.y - a.y
    val lenSq = dx * dx + dy * dy
    if (lenSq < 1e-4f) return hypot(px - a.x, py - a.y)
    val t = (((px - a.x) * dx + (py - a.y) * dy) / lenSq).coerceIn(0f, 1f)
    val projX = a.x + t * dx
    val projY = a.y + t * dy
    return hypot(px - projX, py - projY)
  }
}
