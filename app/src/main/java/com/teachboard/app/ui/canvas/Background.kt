package com.teachboard.app.ui.canvas

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.teachboard.app.domain.model.BackgroundStyle
import com.teachboard.app.ui.theme.TeachColors
import kotlin.math.floor
import kotlin.math.sqrt

/**
 * Shared board color & 11-texture painter used by BOTH CanvasBoard and PNG/PDF Exporters (PRD 19.3).
 * Automatically switches texture lines to light contrast on dark boards (PRD 4.4).
 */
object Background {

  private val linePaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE } }
  private val majorPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.STROKE } }
  private val dotPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL } }
  private val marginPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      color = TeachColors.NotebookMargin
      strokeWidth = 2.5f
    }
  }
  private val bitmapPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG) }

  fun draw(
    canvas: Canvas,
    visibleWorld: RectF,
    backgroundColor: Int,
    style: BackgroundStyle,
    pdfBitmap: Bitmap? = null,
    pdfBoundsWorld: RectF? = null
  ) {
    canvas.drawColor(backgroundColor)

    val isDark = TeachColors.isDarkColor(backgroundColor)
    linePaint.color = if (isDark) TeachColors.TextureLineDarkBg else TeachColors.TextureLineLightBg
    linePaint.strokeWidth = 1.4f
    majorPaint.color = if (isDark) TeachColors.TextureMajorDarkBg else TeachColors.TextureMajorLightBg
    majorPaint.strokeWidth = 2.4f
    dotPaint.color = majorPaint.color

    when (style) {
      BackgroundStyle.PLAIN -> Unit
      BackgroundStyle.GRID -> drawGrid(canvas, visibleWorld, 48f, majorEvery = 0)
      BackgroundStyle.GRAPH -> drawGrid(canvas, visibleWorld, 32f, majorEvery = 5)
      BackgroundStyle.LINES -> drawHorizontal(canvas, visibleWorld, 56f)
      BackgroundStyle.NOTEBOOK -> {
        drawHorizontal(canvas, visibleWorld, 56f)
        canvas.drawLine(140f, visibleWorld.top, 140f, visibleWorld.bottom, marginPaint)
      }
      BackgroundStyle.DOTS -> drawDots(canvas, visibleWorld, 48f)
      BackgroundStyle.CHALK -> drawChalk(canvas, visibleWorld, isDark)
      BackgroundStyle.ISOMETRIC -> drawIsometric(canvas, visibleWorld, 56f)
      BackgroundStyle.MUSIC -> drawMusic(canvas, visibleWorld)
      BackgroundStyle.DIAMOND -> drawDiamond(canvas, visibleWorld, 56f)
      BackgroundStyle.WHITEBOARD -> drawWhiteboard(canvas, visibleWorld, isDark)
    }

    if (pdfBitmap != null && !pdfBitmap.isRecycled) {
      val dest = pdfBoundsWorld ?: RectF(0f, 0f, pdfBitmap.width.toFloat(), pdfBitmap.height.toFloat())
      canvas.drawBitmap(pdfBitmap, null, dest, bitmapPaint)
    }
  }

  private fun drawGrid(canvas: Canvas, world: RectF, step: Float, majorEvery: Int) {
    val sx = floor(world.left / step).toInt(); val ex = floor(world.right / step).toInt() + 1
    for (i in sx..ex) {
      val x = i * step
      canvas.drawLine(x, world.top, x, world.bottom, if (majorEvery > 0 && i % majorEvery == 0) majorPaint else linePaint)
    }
    val sy = floor(world.top / step).toInt(); val ey = floor(world.bottom / step).toInt() + 1
    for (j in sy..ey) {
      val y = j * step
      canvas.drawLine(world.left, y, world.right, y, if (majorEvery > 0 && j % majorEvery == 0) majorPaint else linePaint)
    }
  }

  private fun drawHorizontal(canvas: Canvas, world: RectF, step: Float) {
    val sy = floor(world.top / step).toInt(); val ey = floor(world.bottom / step).toInt() + 1
    for (j in sy..ey) canvas.drawLine(world.left, j * step, world.right, j * step, linePaint)
  }

  private fun drawDots(canvas: Canvas, world: RectF, step: Float) {
    val sx = floor(world.left / step).toInt(); val ex = floor(world.right / step).toInt() + 1
    val sy = floor(world.top / step).toInt(); val ey = floor(world.bottom / step).toInt() + 1
    val stride = if ((ex - sx) * (ey - sy) > 5000) 2 else 1
    var xi = sx
    while (xi <= ex) {
      var yi = sy
      while (yi <= ey) {
        canvas.drawCircle(xi * step, yi * step, 2.3f, dotPaint)
        yi += stride
      }
      xi += stride
    }
  }

  private fun drawChalk(canvas: Canvas, world: RectF, isDark: Boolean) {
    val p = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.FILL
      color = if (isDark) 0x16FFFFFF else 0x12000000
    }
    val step = 64f
    val sx = floor(world.left / step).toInt(); val ex = floor(world.right / step).toInt() + 1
    val sy = floor(world.top / step).toInt(); val ey = floor(world.bottom / step).toInt() + 1
    val stride = if ((ex - sx) * (ey - sy) > 4000) 2 else 1
    var xi = sx
    while (xi <= ex) {
      var yi = sy
      while (yi <= ey) {
        val h = (xi * 73856093 xor yi * 19349663) and 0xFF
        canvas.drawCircle(xi * step + (h and 0xF) * 3.5f, yi * step + ((h shr 4) and 0xF) * 3.5f, 1.3f + (h % 3) * 0.6f, p)
        yi += stride
      }
      xi += stride
    }
  }

  private fun drawIsometric(canvas: Canvas, world: RectF, step: Float) {
    val hStep = step * sqrt(3f) / 2f
    val sx = floor(world.left / hStep).toInt() - 1; val ex = floor(world.right / hStep).toInt() + 1
    for (i in sx..ex) canvas.drawLine(i * hStep, world.top, i * hStep, world.bottom, linePaint)
    val slope = 1f / sqrt(3f)
    val spanX = world.width()
    val sk = floor((world.top - spanX * slope - step * 2) / step).toInt()
    val ek = floor((world.bottom + spanX * slope + step * 2) / step).toInt() + 1
    for (k in sk..ek) {
      val by = k * step
      canvas.drawLine(world.left, by, world.right, by + spanX * slope, linePaint)
      canvas.drawLine(world.left, by, world.right, by - spanX * slope, linePaint)
    }
  }

  private fun drawMusic(canvas: Canvas, world: RectF) {
    val gap = 18f; val period = 180f
    val ss = floor(world.top / period).toInt() - 1; val es = floor(world.bottom / period).toInt() + 1
    for (st in ss..es) {
      val baseY = st * period + 40f
      for (i in 0 until 5) canvas.drawLine(world.left, baseY + i * gap, world.right, baseY + i * gap, majorPaint)
    }
  }

  private fun drawDiamond(canvas: Canvas, world: RectF, step: Float) {
    val spanX = world.width()
    val sk = floor((world.top - spanX - step * 2) / step).toInt()
    val ek = floor((world.bottom + spanX + step * 2) / step).toInt() + 1
    for (k in sk..ek) {
      val by = k * step
      canvas.drawLine(world.left, by, world.right, by + spanX, linePaint)
      canvas.drawLine(world.left, by, world.right, by - spanX, linePaint)
    }
  }

  private fun drawWhiteboard(canvas: Canvas, world: RectF, isDark: Boolean) {
    val faint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE; strokeWidth = 1f
      color = if (isDark) 0x16FFFFFF else 0x1494A3B8
    }
    val step = 72f
    val sx = floor(world.left / step).toInt(); val ex = floor(world.right / step).toInt() + 1
    for (i in sx..ex) canvas.drawLine(i * step, world.top, i * step, world.bottom, faint)
    val sy = floor(world.top / step).toInt(); val ey = floor(world.bottom / step).toInt() + 1
    for (j in sy..ey) canvas.drawLine(world.left, j * step, world.right, j * step, faint)
  }
}
