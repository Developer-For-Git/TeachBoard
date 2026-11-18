package com.teachboard.app.domain.formula

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Typeface
import kotlin.math.max

/**
 * Offline LaTeX subset parser, AST/token renderer & 40-symbol palette (PRD 19.3 & F-15).
 */
object FormulaParser {

  data class SymbolShortcut(val label: String, val snippet: String)

  // 40-symbol LaTeX quick palette (PRD 19.3 & F-15)
  val symbolPalette40: List<SymbolShortcut> = listOf(
    SymbolShortcut("a/b", "\\frac{}{}"),
    SymbolShortcut("√x", "\\sqrt{}"),
    SymbolShortcut("x²", "^2"),
    SymbolShortcut("xⁿ", "^{}"),
    SymbolShortcut("xₙ", "_{}"),
    SymbolShortcut("∑", "\\sum "),
    SymbolShortcut("∫", "\\int "),
    SymbolShortcut("∏", "\\prod "),
    SymbolShortcut("lim", "\\lim "),
    SymbolShortcut("±", "\\pm "),
    SymbolShortcut("∓", "\\mp "),
    SymbolShortcut("×", "\\times "),
    SymbolShortcut("÷", "\\div "),
    SymbolShortcut("·", "\\cdot "),
    SymbolShortcut("≤", "\\leq "),
    SymbolShortcut("≥", "\\geq "),
    SymbolShortcut("≠", "\\neq "),
    SymbolShortcut("≈", "\\approx "),
    SymbolShortcut("≡", "\\equiv "),
    SymbolShortcut("∞", "\\infty"),
    SymbolShortcut("→", "\\to "),
    SymbolShortcut("⇒", "\\Rightarrow "),
    SymbolShortcut("⇔", "\\Leftrightarrow "),
    SymbolShortcut("∂", "\\partial "),
    SymbolShortcut("∇", "\\nabla "),
    SymbolShortcut("°", "\\degree"),
    SymbolShortcut("∠", "\\angle "),
    SymbolShortcut("⊥", "\\perp "),
    SymbolShortcut("∥", "\\parallel "),
    SymbolShortcut("∈", "\\in "),
    SymbolShortcut("π", "\\pi"),
    SymbolShortcut("α", "\\alpha"),
    SymbolShortcut("β", "\\beta"),
    SymbolShortcut("γ", "\\gamma"),
    SymbolShortcut("θ", "\\theta"),
    SymbolShortcut("λ", "\\lambda"),
    SymbolShortcut("μ", "\\mu"),
    SymbolShortcut("σ", "\\sigma"),
    SymbolShortcut("Δ", "\\Delta"),
    SymbolShortcut("Ω", "\\Omega")
  )

  sealed class MathNode {
    data class Text(val value: String) : MathNode()
    data class Row(val children: List<MathNode>) : MathNode()
    data class Fraction(val num: MathNode, val den: MathNode) : MathNode()
    data class Sqrt(val body: MathNode) : MathNode()
    data class Script(val base: MathNode, val sup: MathNode?, val sub: MathNode?) : MathNode()
  }

  private data class Metrics(val width: Float, val ascent: Float, val descent: Float) {
    val height: Float get() = ascent + descent
  }

  private val symbols = mapOf(
    "alpha" to "α", "beta" to "β", "gamma" to "γ", "delta" to "δ", "Delta" to "Δ",
    "epsilon" to "ε", "theta" to "θ", "lambda" to "λ", "mu" to "μ", "pi" to "π",
    "sigma" to "σ", "Sigma" to "Σ", "omega" to "ω", "Omega" to "Ω", "phi" to "φ", "psi" to "ψ",
    "pm" to "±", "mp" to "∓", "times" to "×", "cdot" to "·", "div" to "÷",
    "leq" to "≤", "le" to "≤", "geq" to "≥", "ge" to "≥", "neq" to "≠", "ne" to "≠",
    "approx" to "≈", "equiv" to "≡", "infty" to "∞", "to" to "→", "rightarrow" to "→",
    "leftarrow" to "←", "Rightarrow" to "⇒", "Leftrightarrow" to "⇔",
    "sum" to "∑", "int" to "∫", "prod" to "∏", "partial" to "∂", "nabla" to "∇",
    "degree" to "°", "angle" to "∠", "perp" to "⊥", "parallel" to "∥", "in" to "∈",
    "sin" to "sin", "cos" to "cos", "tan" to "tan", "log" to "log", "ln" to "ln", "lim" to "lim"
  )

  fun parse(latex: String): MathNode = Parser(latex.ifBlank { "y = 2x + 1" }).parseSeq(false)

  fun measure(latex: String, fontSizePx: Float): Pair<Float, Float> {
    val node = parse(latex)
    val paint = createPaint(0xFF000000.toInt(), fontSizePx)
    val m = measureNode(node, paint, fontSizePx)
    return (m.width + 32f).coerceAtLeast(100f) to (m.height + 28f).coerceAtLeast(64f)
  }

  fun drawFormula(
    canvas: Canvas,
    latex: String,
    color: Int,
    fontSizePx: Float,
    boxWidth: Float,
    boxHeight: Float
  ) {
    val node = parse(latex)
    val paint = createPaint(color, fontSizePx)
    val m = measureNode(node, paint, fontSizePx)
    val startX = ((boxWidth - m.width) / 2f).coerceAtLeast(12f)
    val baselineY = (boxHeight - m.height) / 2f + m.ascent
    drawNode(canvas, node, startX, baselineY, paint, fontSizePx)
  }

  private fun createPaint(color: Int, textSize: Float): Paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
    this.color = color
    this.textSize = textSize
    this.typeface = Typeface.create(Typeface.SERIF, Typeface.ITALIC)
    this.strokeWidth = (textSize * 0.055f).coerceAtLeast(2f)
    this.strokeCap = Paint.Cap.ROUND
    this.strokeJoin = Paint.Join.ROUND
  }

  private fun measureNode(node: MathNode, paint: Paint, size: Float): Metrics {
    paint.textSize = size
    return when (node) {
      is MathNode.Text -> {
        val fm = paint.fontMetrics
        Metrics(paint.measureText(node.value), -fm.ascent, fm.descent)
      }
      is MathNode.Row -> {
        var w = 0f; var asc = size * 0.7f; var desc = size * 0.25f
        for (c in node.children) {
          val m = measureNode(c, paint, size)
          w += m.width; asc = max(asc, m.ascent); desc = max(desc, m.descent)
        }
        Metrics(w, asc, desc)
      }
      is MathNode.Fraction -> {
        val cs = size * 0.78f
        val nm = measureNode(node.num, paint, cs)
        val dm = measureNode(node.den, paint, cs)
        val w = max(nm.width, dm.width) + size * 0.3f
        val gap = size * 0.14f
        Metrics(w, nm.height + gap + size * 0.28f, (dm.height + gap - size * 0.20f).coerceAtLeast(size * 0.3f))
      }
      is MathNode.Sqrt -> {
        val inner = measureNode(node.body, paint, size)
        Metrics(size * 0.55f + inner.width + size * 0.12f, inner.ascent + size * 0.18f, inner.descent)
      }
      is MathNode.Script -> {
        val bm = measureNode(node.base, paint, size)
        val ss = size * 0.62f
        val supM = node.sup?.let { measureNode(it, paint, ss) }
        val subM = node.sub?.let { measureNode(it, paint, ss) }
        val extraW = max(supM?.width ?: 0f, subM?.width ?: 0f) + size * 0.04f
        val asc = max(bm.ascent, if (supM != null) bm.ascent * 0.55f + supM.ascent else 0f)
        val desc = max(bm.descent, if (subM != null) bm.descent + subM.descent * 0.8f else 0f)
        Metrics(bm.width + extraW, asc, desc)
      }
    }
  }

  private fun drawNode(canvas: Canvas, node: MathNode, x: Float, baselineY: Float, paint: Paint, size: Float): Float {
    paint.textSize = size
    paint.strokeWidth = (size * 0.055f).coerceAtLeast(1.8f)
    return when (node) {
      is MathNode.Text -> {
        paint.style = Paint.Style.FILL
        canvas.drawText(node.value, x, baselineY, paint)
        paint.measureText(node.value)
      }
      is MathNode.Row -> {
        var cx = x
        for (c in node.children) cx += drawNode(canvas, c, cx, baselineY, paint, size)
        cx - x
      }
      is MathNode.Fraction -> {
        val cs = size * 0.78f
        val nm = measureNode(node.num, paint, cs)
        val dm = measureNode(node.den, paint, cs)
        val w = max(nm.width, dm.width) + size * 0.3f
        val barY = baselineY - size * 0.28f
        val gap = size * 0.12f
        drawNode(canvas, node.num, x + (w - nm.width) / 2f, barY - gap - nm.descent, paint, cs)
        paint.style = Paint.Style.STROKE
        canvas.drawLine(x + size * 0.06f, barY, x + w - size * 0.06f, barY, paint)
        drawNode(canvas, node.den, x + (w - dm.width) / 2f, barY + gap + dm.ascent, paint, cs)
        w
      }
      is MathNode.Sqrt -> {
        val inner = measureNode(node.body, paint, size)
        val radW = size * 0.55f
        val topY = baselineY - inner.ascent - size * 0.12f
        val botY = baselineY + inner.descent * 0.55f
        val midY = (topY + botY) * 0.55f
        val path = Path().apply {
          moveTo(x + radW * 0.05f, midY)
          lineTo(x + radW * 0.30f, midY - size * 0.06f)
          lineTo(x + radW * 0.55f, botY)
          lineTo(x + radW * 0.92f, topY)
          lineTo(x + radW + inner.width + size * 0.08f, topY)
        }
        paint.style = Paint.Style.STROKE
        canvas.drawPath(path, paint)
        drawNode(canvas, node.body, x + radW, baselineY, paint, size)
        radW + inner.width + size * 0.12f
      }
      is MathNode.Script -> {
        val bm = measureNode(node.base, paint, size)
        drawNode(canvas, node.base, x, baselineY, paint, size)
        val ss = size * 0.62f
        val sx = x + bm.width + size * 0.02f
        var extra = 0f
        node.sup?.let { extra = max(extra, drawNode(canvas, it, sx, baselineY - bm.ascent * 0.52f, paint, ss)) }
        node.sub?.let { extra = max(extra, drawNode(canvas, it, sx, baselineY + bm.descent * 0.95f, paint, ss)) }
        bm.width + extra + size * 0.04f
      }
    }
  }

  private class Parser(private val src: String) {
    private var idx = 0
    fun parseSeq(stopAtBrace: Boolean): MathNode {
      val list = mutableListOf<MathNode>()
      while (idx < src.length) {
        if (stopAtBrace && src[idx] == '}') { idx++; break }
        val atom = parseAtom() ?: continue
        var sup: MathNode? = null; var sub: MathNode? = null
        while (idx < src.length && (src[idx] == '^' || src[idx] == '_')) {
          val op = src[idx++]
          val arg = parseGroup()
          if (op == '^') sup = arg else sub = arg
        }
        list.add(if (sup != null || sub != null) MathNode.Script(atom, sup, sub) else atom)
      }
      return if (list.size == 1) list[0] else MathNode.Row(list)
    }

    private fun parseAtom(): MathNode? {
      if (idx >= src.length) return null
      return when (val ch = src[idx]) {
        '{' -> { idx++; parseSeq(true) }
        '\\' -> parseCmd()
        ' ' -> { idx++; MathNode.Text(" ") }
        else -> {
          idx++
          MathNode.Text(when (ch) { '-' -> "−"; '*' -> "·"; else -> ch.toString() })
        }
      }
    }

    private fun parseGroup(): MathNode {
      while (idx < src.length && src[idx] == ' ') idx++
      if (idx >= src.length) return MathNode.Text("")
      return if (src[idx] == '{') { idx++; parseSeq(true) } else parseAtom() ?: MathNode.Text("")
    }

    private fun parseCmd(): MathNode {
      idx++
      val start = idx
      while (idx < src.length && src[idx].isLetter()) idx++
      val cmd = src.substring(start, idx)
      return when (cmd) {
        "frac" -> MathNode.Fraction(parseGroup(), parseGroup())
        "sqrt" -> MathNode.Sqrt(parseGroup())
        "" -> if (idx < src.length) MathNode.Text(src[idx++].toString()) else MathNode.Text("")
        else -> MathNode.Text(symbols[cmd] ?: cmd)
      }
    }
  }
}
