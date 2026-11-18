package com.teachboard.app.domain.calc

import kotlin.math.E
import kotlin.math.PI
import kotlin.math.abs
import kotlin.math.cos
import kotlin.math.ln
import kotlin.math.log10
import kotlin.math.pow
import kotlin.math.sin
import kotlin.math.sqrt
import kotlin.math.tan

/**
 * Pure-Kotlin scientific expression evaluator (PRD 19.3):
 * Supports + - * / % ^, parentheses, π, e, sin, cos, tan, ln, log, sqrt, abs.
 */
object ExpressionEvaluator {

  fun evaluate(raw: String): Double? {
    val cleaned = raw.replace(" ", "").replace("×", "*").replace("÷", "/")
    if (cleaned.isEmpty()) return null
    return runCatching {
      val v = Parser(cleaned).parseExpression()
      if (v.isNaN() || v.isInfinite()) null else v
    }.getOrNull()
  }

  fun format(value: Double): String {
    if (value.isNaN() || value.isInfinite()) return "Error"
    val asLong = value.toLong()
    return if (value == asLong.toDouble()) asLong.toString()
    else String.format(java.util.Locale.US, "%.6g", value).trimEnd('0').trimEnd('.')
  }

  private class Parser(private val s: String) {
    private var pos = 0

    fun parseExpression(): Double {
      var x = parseTerm()
      while (pos < s.length) {
        when (s[pos]) {
          '+' -> { pos++; x += parseTerm() }
          '-' -> { pos++; x -= parseTerm() }
          else -> return x
        }
      }
      return x
    }

    private fun parseTerm(): Double {
      var x = parsePower()
      while (pos < s.length) {
        when (s[pos]) {
          '*' -> { pos++; x *= parsePower() }
          '/' -> { pos++; x /= parsePower() }
          '%' -> { pos++; x %= parsePower() }
          else -> return x
        }
      }
      return x
    }

    private fun parsePower(): Double {
      var x = parseUnary()
      if (pos < s.length && s[pos] == '^') {
        pos++
        x = x.pow(parsePower())
      }
      return x
    }

    private fun parseUnary(): Double {
      if (pos < s.length && s[pos] == '+') { pos++; return parseUnary() }
      if (pos < s.length && s[pos] == '-') { pos++; return -parseUnary() }
      return parseFactor()
    }

    private fun parseFactor(): Double {
      if (pos >= s.length) throw IllegalArgumentException("Unexpected end")
      val ch = s[pos]
      if (ch == '(') {
        pos++
        val v = parseExpression()
        if (pos < s.length && s[pos] == ')') pos++
        return v
      }
      if (ch == 'π') { pos++; return PI }
      if (ch == 'e') { pos++; return E }
      if (ch == '√') { pos++; return sqrt(parseFactor()) }
      if (ch.isDigit() || ch == '.') {
        val start = pos
        while (pos < s.length && (s[pos].isDigit() || s[pos] == '.')) pos++
        return s.substring(start, pos).toDouble()
      }
      if (ch.isLetter()) {
        val start = pos
        while (pos < s.length && s[pos].isLetter()) pos++
        val fn = s.substring(start, pos).lowercase()
        val arg = parseFactor()
        return when (fn) {
          "sin" -> sin(Math.toRadians(arg))
          "cos" -> cos(Math.toRadians(arg))
          "tan" -> tan(Math.toRadians(arg))
          "log" -> log10(arg)
          "ln" -> ln(arg)
          "sqrt" -> sqrt(arg)
          "abs" -> abs(arg)
          else -> throw IllegalArgumentException("Unknown function: $fn")
        }
      }
      throw IllegalArgumentException("Unexpected char: $ch")
    }
  }
}
