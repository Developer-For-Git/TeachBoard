package com.teachboard.app.domain.model

import java.util.Locale
import kotlin.math.roundToInt

/**
 * Compact text codecs required by PRD Section 19.6 (unit-tested in TeachBoardUnitTest):
 *
 * - `points`:        `x:y,x:y,...` (absolute board coordinates, max 2 decimals)
 * - `geometry`:      `C0:0,100:0,...|O...` (contours separated by `|`, prefix `C` = closed, `O` = open, max 4000 pts)
 * - `eraserStrokes`: `width;p1,p2|width;p1,p2` (eraser swipe journal re-applied on load)
 */
object Serialization {

  private const val MAX_GEOMETRY_POINTS = 4000

  fun encodePoints(points: List<StrokePoint>): String {
    if (points.isEmpty()) return ""
    val sb = StringBuilder(points.size * 12)
    points.forEachIndexed { idx, p ->
      if (idx > 0) sb.append(',')
      sb.append(formatNum(p.x)).append(':').append(formatNum(p.y))
    }
    return sb.toString()
  }

  fun decodePoints(encoded: String?): List<StrokePoint> {
    if (encoded.isNullOrBlank()) return emptyList()
    val result = ArrayList<StrokePoint>()
    var start = 0
    val len = encoded.length
    while (start < len) {
      val comma = encoded.indexOf(',', start).let { if (it == -1) len else it }
      val colon = encoded.indexOf(':', start)
      if (colon in (start + 1) until comma) {
        val x = encoded.substring(start, colon).toFloatOrNull()
        val y = encoded.substring(colon + 1, comma).toFloatOrNull()
        if (x != null && y != null) {
          result.add(StrokePoint(x, y))
        }
      }
      start = comma + 1
    }
    return result
  }

  fun encodeGeometry(contours: List<Contour>): String? {
    if (contours.isEmpty()) return null
    val sb = StringBuilder()
    var budget = MAX_GEOMETRY_POINTS
    contours.forEachIndexed { idx, c ->
      if (budget <= 0 || c.points.isEmpty()) return@forEachIndexed
      if (idx > 0 && sb.isNotEmpty()) sb.append('|')
      sb.append(if (c.closed) 'C' else 'O')
      val pts = if (c.points.size <= budget) c.points else downsample(c.points, budget)
      sb.append(encodePoints(pts))
      budget -= pts.size
    }
    return sb.toString().ifEmpty { null }
  }

  fun decodeGeometry(encoded: String?): List<Contour> {
    if (encoded.isNullOrBlank()) return emptyList()
    return encoded.split('|').mapNotNull { seg ->
      if (seg.length < 2) return@mapNotNull null
      val prefix = seg[0]
      val closed = prefix == 'C'
      val body = if (prefix == 'C' || prefix == 'O') seg.substring(1) else seg
      val pts = decodePoints(body)
      if (pts.isEmpty()) null else Contour(closed = closed, points = pts)
    }
  }

  fun encodeEraserStrokes(swipes: List<EraserSwipe>): String? {
    if (swipes.isEmpty()) return null
    return swipes.joinToString("|") { s ->
      "${formatNum(s.width)};${encodePoints(s.points)}"
    }
  }

  fun decodeEraserStrokes(encoded: String?): List<EraserSwipe> {
    if (encoded.isNullOrBlank()) return emptyList()
    return encoded.split('|').mapNotNull { seg ->
      val semi = seg.indexOf(';')
      if (semi <= 0) return@mapNotNull null
      val width = seg.substring(0, semi).toFloatOrNull() ?: return@mapNotNull null
      val pts = decodePoints(seg.substring(semi + 1))
      if (pts.isEmpty()) null else EraserSwipe(width, pts)
    }
  }

  fun encodeIntSetCsv(set: Set<Int>): String = set.sorted().joinToString(",")

  fun decodeIntSetCsv(csv: String?): Set<Int> {
    if (csv.isNullOrBlank()) return emptySet()
    return csv.split(',').mapNotNull { it.trim().toIntOrNull() }.toSet()
  }

  /**
   * Compact table cell payload stored in `textContent` for ToolType.Table objects.
   * Format: `rows|cols|cell00\u001Fcell01...\u001Erow1...`
   */
  fun encodeTablePayload(rows: Int, cols: Int, cells: List<List<String>>): String {
    val body = cells.joinToString("\u001E") { row -> row.joinToString("\u001F") }
    return "$rows|$cols|$body"
  }

  fun decodeTablePayload(payload: String?): Triple<Int, Int, List<List<String>>> {
    if (payload.isNullOrBlank()) return Triple(3, 3, List(3) { List(3) { "" } })
    val first = payload.indexOf('|')
    val second = if (first >= 0) payload.indexOf('|', first + 1) else -1
    if (first <= 0 || second <= first) return Triple(3, 3, List(3) { List(3) { "" } })
    val rows = payload.substring(0, first).toIntOrNull()?.coerceIn(1, 10) ?: 3
    val cols = payload.substring(first + 1, second).toIntOrNull()?.coerceIn(1, 10) ?: 3
    val rawRows = payload.substring(second + 1).split("\u001E")
    val grid = List(rows) { r ->
      val rawCols = rawRows.getOrNull(r)?.split("\u001F").orEmpty()
      List(cols) { c -> rawCols.getOrNull(c).orEmpty() }
    }
    return Triple(rows, cols, grid)
  }

  /**
   * Text styling metadata encoded alongside textContent when rich formatting is used.
   * Format: `\u0001font|bold|italic|align\u0002actualText`
   */
  data class RichTextMeta(
    val text: String,
    val fontFamily: String = "SANS",
    val bold: Boolean = false,
    val italic: Boolean = false,
    val align: String = "START"
  )

  fun encodeRichText(meta: RichTextMeta): String {
    val b = if (meta.bold) "1" else "0"
    val i = if (meta.italic) "1" else "0"
    return "\u0001${meta.fontFamily}|$b|$i|${meta.align}\u0002${meta.text}"
  }

  fun decodeRichText(raw: String?): RichTextMeta {
    if (raw.isNullOrEmpty()) return RichTextMeta("")
    if (!raw.startsWith("\u0001")) return RichTextMeta(raw)
    val sep = raw.indexOf('\u0002')
    if (sep <= 1) return RichTextMeta(raw)
    val parts = raw.substring(1, sep).split('|')
    val text = raw.substring(sep + 1)
    return RichTextMeta(
      text = text,
      fontFamily = parts.getOrNull(0) ?: "SANS",
      bold = parts.getOrNull(1) == "1",
      italic = parts.getOrNull(2) == "1",
      align = parts.getOrNull(3) ?: "START"
    )
  }

  private fun downsample(points: List<StrokePoint>, maxCount: Int): List<StrokePoint> {
    if (points.size <= maxCount) return points
    val step = points.size.toFloat() / maxCount.toFloat()
    return List(maxCount) { i ->
      points[(i * step).toInt().coerceAtMost(points.lastIndex)]
    }
  }

  private fun formatNum(value: Float): String {
    val scaled = (value * 100f).roundToInt()
    if (scaled % 100 == 0) return (scaled / 100).toString()
    val s = String.format(Locale.US, "%.2f", scaled / 100f)
    return s.trimEnd('0').trimEnd('.')
  }
}
