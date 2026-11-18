package com.teachboard.app

import com.teachboard.app.domain.calc.ExpressionEvaluator
import com.teachboard.app.domain.formula.FormulaParser
import com.teachboard.app.domain.model.Contour
import com.teachboard.app.domain.model.EraserSwipe
import com.teachboard.app.domain.model.Serialization
import com.teachboard.app.domain.model.StrokePoint
import com.teachboard.app.domain.model.ToolType
import com.teachboard.app.domain.smartshapes.ShapeClassifier
import com.teachboard.app.domain.stickers.StickerCatalog
import com.teachboard.app.ui.canvas.ObjectPainter
import com.teachboard.app.ui.canvas.parsePageRange
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

class TeachBoardUnitTest {

  // ===== 1. Serialization Codecs (PRD 19.6) =====

  @Test
  fun testPointsRoundTrip() {
    val pts = listOf(
      StrokePoint(0f, 0f),
      StrokePoint(12.345f, 56.789f),
      StrokePoint(-100.5f, 200f)
    )
    val enc = Serialization.encodePoints(pts)
    assertEquals("0:0,12.35:56.79,-100.5:200", enc)
    val dec = Serialization.decodePoints(enc)
    assertEquals(3, dec.size)
    assertEquals(12.35f, dec[1].x, 0.01f)
    assertEquals(56.79f, dec[1].y, 0.01f)
    assertEquals(-100.5f, dec[2].x, 0.01f)
  }

  @Test
  fun testGeometryContoursAndPointCap() {
    val c1 = Contour(closed = true, points = listOf(StrokePoint(0f, 0f), StrokePoint(100f, 0f), StrokePoint(100f, 100f)))
    val c2 = Contour(closed = false, points = listOf(StrokePoint(10f, 20f), StrokePoint(30f, 40f)))
    val enc = Serialization.encodeGeometry(listOf(c1, c2))
    assertNotNull(enc)
    assertTrue(enc!!.startsWith("C0:0,100:0,100:100|O10:20,30:40"))

    val dec = Serialization.decodeGeometry(enc)
    assertEquals(2, dec.size)
    assertTrue(dec[0].closed)
    assertFalse(dec[1].closed)
    assertEquals(3, dec[0].points.size)
    assertEquals(2, dec[1].points.size)

    // Verify 4000-point budget cap
    val huge = Contour(closed = false, points = List(6000) { StrokePoint(it.toFloat(), it.toFloat()) })
    val cappedDec = Serialization.decodeGeometry(Serialization.encodeGeometry(listOf(huge)))
    assertTrue(cappedDec.first().points.size <= 4000)
  }

  @Test
  fun testEraserStrokesCodec() {
    val swipes = listOf(
      EraserSwipe(48f, listOf(StrokePoint(1f, 2f), StrokePoint(3f, 4f))),
      EraserSwipe(24.5f, listOf(StrokePoint(10f, 20f), StrokePoint(30f, 40f)))
    )
    val enc = Serialization.encodeEraserStrokes(swipes)
    assertNotNull(enc)
    val dec = Serialization.decodeEraserStrokes(enc)
    assertEquals(2, dec.size)
    assertEquals(48f, dec[0].width, 0.01f)
    assertEquals(24.5f, dec[1].width, 0.01f)
    assertEquals(2, dec[1].points.size)
  }

  @Test
  fun testTableAndRichTextCodecs() {
    val cells = listOf(listOf("A", "B"), listOf("1", "2"))
    val encoded = Serialization.encodeTablePayload(2, 2, cells)
    val (r, c, grid) = Serialization.decodeTablePayload(encoded)
    assertEquals(2, r)
    assertEquals(2, c)
    assertEquals("B", grid[0][1])
    assertEquals("2", grid[1][1])

    val meta = Serialization.RichTextMeta("Hello\nWorld", fontFamily = "SERIF", bold = true, italic = true, align = "CENTER")
    val decMeta = Serialization.decodeRichText(Serialization.encodeRichText(meta))
    assertEquals(meta, decMeta)
  }

  // ===== 2. Smart Shapes Classifier (PRD 19.9) =====

  @Test
  fun testShapeClassifierLine() {
    val line = List(30) { i -> StrokePoint(100f + i * 10f, 200f + i * 2f) }
    val cands = ShapeClassifier.classify(line)
    assertTrue(cands.isNotEmpty())
    assertEquals(ToolType.Line, cands.first().toolType)
  }

  @Test
  fun testShapeClassifierCircleAndOval() {
    val circle = (0..64).map { i ->
      val a = 2.0 * PI * i / 64.0
      StrokePoint(300f + cos(a).toFloat() * 120f, 300f + sin(a).toFloat() * 120f)
    }
    val cands = ShapeClassifier.classify(circle)
    assertTrue(cands.any { it.toolType == ToolType.Circle })

    val oval = (0..64).map { i ->
      val a = 2.0 * PI * i / 64.0
      StrokePoint(300f + cos(a).toFloat() * 200f, 300f + sin(a).toFloat() * 80f)
    }
    val ovalCands = ShapeClassifier.classify(oval)
    assertTrue(ovalCands.any { it.toolType == ToolType.Oval })
  }

  @Test
  fun testShapeClassifierPolygons() {
    fun tracePoly(vertices: List<Pair<Float, Float>>, stepsPerEdge: Int = 16): List<StrokePoint> {
      val pts = mutableListOf<StrokePoint>()
      for (i in vertices.indices) {
        val a = vertices[i]
        val b = vertices[(i + 1) % vertices.size]
        for (s in 0 until stepsPerEdge) {
          val t = s.toFloat() / stepsPerEdge
          pts.add(StrokePoint(a.first + (b.first - a.first) * t, a.second + (b.second - a.second) * t))
        }
      }
      pts.add(StrokePoint(vertices.first().first, vertices.first().second))
      return pts
    }

    val square = tracePoly(listOf(100f to 100f, 300f to 100f, 300f to 300f, 100f to 300f))
    assertTrue(ShapeClassifier.classify(square).any { it.toolType == ToolType.Square || it.toolType == ToolType.Rect })

    val rect = tracePoly(listOf(100f to 100f, 420f to 100f, 420f to 240f, 100f to 240f))
    assertTrue(ShapeClassifier.classify(rect).any { it.toolType == ToolType.Rect })

    val tri = tracePoly(listOf(200f to 100f, 350f to 360f, 50f to 360f))
    assertTrue(ShapeClassifier.classify(tri).any { it.toolType == ToolType.Triangle || it.toolType == ToolType.IsoscelesTriangle })

    val hex = tracePoly((0 until 6).map { i ->
      val a = 2.0 * PI * i / 6.0
      (300f + cos(a).toFloat() * 140f) to (300f + sin(a).toFloat() * 140f)
    })
    assertTrue(ShapeClassifier.classify(hex).any { it.toolType == ToolType.Hexagon })
  }

  // ===== 3. Expression Evaluator (PRD 19.3 & F-25) =====

  @Test
  fun testExpressionEvaluator() {
    assertEquals(14.0, ExpressionEvaluator.evaluate("2 + 3 * 4")!!, 1e-6)
    assertEquals(20.0, ExpressionEvaluator.evaluate("(2 + 3) * 4")!!, 1e-6)
    assertEquals(8.0, ExpressionEvaluator.evaluate("2 ^ 3")!!, 1e-6)
    assertEquals(3.0, ExpressionEvaluator.evaluate("sqrt(9)")!!, 1e-6)
    assertEquals(1.0, ExpressionEvaluator.evaluate("sin(90)")!!, 1e-6)
    assertEquals(2.0, ExpressionEvaluator.evaluate("log(100)")!!, 1e-6)
    assertEquals(1.0, ExpressionEvaluator.evaluate("ln(e)")!!, 1e-6)
    assertEquals(5.0, ExpressionEvaluator.evaluate("abs(-5)")!!, 1e-6)
    assertNull(ExpressionEvaluator.evaluate("10 / 0"))
  }

  // ===== 4. Formula Parser, Stickers, Simplification & Page Ranges =====

  @Test
  fun testFormulaParserAndStickerCounts() {
    assertEquals(40, FormulaParser.symbolPalette40.size)
    val ast = FormulaParser.parse("\\frac{-b \\pm \\sqrt{b^2 - 4ac}}{2a}")
    assertTrue(ast is FormulaParser.MathNode.Fraction || ast is FormulaParser.MathNode.Row)

    assertEquals(18, StickerCatalog.generalStickers.size)
    assertTrue(StickerCatalog.scienceStickers.size >= 150)

    assertEquals(45, ToolType.shapesSheet2D.size + ToolType.shapesSheet3D.size + ToolType.shapesSheetMath.size + ToolType.shapesSheetDiagram.size)
  }

  @Test
  fun testSimplifyAndPageRanges() {
    val straight = List(50) { i -> StrokePoint(i * 5f, 100f) }
    val simplified = ObjectPainter.simplifyPoints(straight, epsilon = 1.5f)
    assertEquals(2, simplified.size)

    assertEquals(listOf(0, 1, 2, 4), parsePageRange("1-3, 5", 10))
    assertEquals(listOf(0, 1), parsePageRange("1-99", 2))
  }
}
