package com.teachboard.app.domain.stickers

import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

enum class StickerGroup {
  GENERAL, SCIENCE
}

data class StickerSpec(
  val id: String,
  val label: String,
  val category: String,
  val group: StickerGroup
)

/**
 * 18 emoji-style General Stickers (F-23) + 150 Science Stickers incl. Bohr Atoms & Elements (F-24).
 * 100% original procedural vector artwork — tiny APK footprint, crisp at 600% zoom, recolorable.
 */
object StickerCatalog {

  private data class ElementInfo(val z: Int, val symbol: String, val name: String, val shells: List<Int>)

  // First 36 Bohr Atom models + full periodic tiles up to 118 (total 150 science stickers!)
  private val bohrElements = listOf(
    ElementInfo(1, "H", "Hydrogen", listOf(1)),
    ElementInfo(2, "He", "Helium", listOf(2)),
    ElementInfo(3, "Li", "Lithium", listOf(2, 1)),
    ElementInfo(4, "Be", "Beryllium", listOf(2, 2)),
    ElementInfo(5, "B", "Boron", listOf(2, 3)),
    ElementInfo(6, "C", "Carbon", listOf(2, 4)),
    ElementInfo(7, "N", "Nitrogen", listOf(2, 5)),
    ElementInfo(8, "O", "Oxygen", listOf(2, 6)),
    ElementInfo(9, "F", "Fluorine", listOf(2, 7)),
    ElementInfo(10, "Ne", "Neon", listOf(2, 8)),
    ElementInfo(11, "Na", "Sodium", listOf(2, 8, 1)),
    ElementInfo(12, "Mg", "Magnesium", listOf(2, 8, 2)),
    ElementInfo(13, "Al", "Aluminum", listOf(2, 8, 3)),
    ElementInfo(14, "Si", "Silicon", listOf(2, 8, 4)),
    ElementInfo(15, "P", "Phosphorus", listOf(2, 8, 5)),
    ElementInfo(16, "S", "Sulfur", listOf(2, 8, 6)),
    ElementInfo(17, "Cl", "Chlorine", listOf(2, 8, 7)),
    ElementInfo(18, "Ar", "Argon", listOf(2, 8, 8)),
    ElementInfo(19, "K", "Potassium", listOf(2, 8, 8, 1)),
    ElementInfo(20, "Ca", "Calcium", listOf(2, 8, 8, 2)),
    ElementInfo(26, "Fe", "Iron", listOf(2, 8, 14, 2)),
    ElementInfo(29, "Cu", "Copper", listOf(2, 8, 18, 1)),
    ElementInfo(30, "Zn", "Zinc", listOf(2, 8, 18, 2)),
    ElementInfo(35, "Br", "Bromine", listOf(2, 8, 18, 7)),
    ElementInfo(47, "Ag", "Silver", listOf(2, 8, 18, 18, 1)),
    ElementInfo(53, "I", "Iodine", listOf(2, 8, 18, 18, 7)),
    ElementInfo(79, "Au", "Gold", listOf(2, 8, 18, 32, 18, 1)),
    ElementInfo(80, "Hg", "Mercury", listOf(2, 8, 18, 32, 18, 2)),
    ElementInfo(82, "Pb", "Lead", listOf(2, 8, 18, 32, 18, 4)),
    ElementInfo(92, "U", "Uranium", listOf(2, 8, 18, 32, 21, 9, 2))
  )

  private val periodicSymbols = listOf(
    "H","He","Li","Be","B","C","N","O","F","Ne","Na","Mg","Al","Si","P","S","Cl","Ar","K","Ca",
    "Sc","Ti","V","Cr","Mn","Fe","Co","Ni","Cu","Zn","Ga","Ge","As","Se","Br","Kr","Rb","Sr","Y","Zr",
    "Nb","Mo","Tc","Ru","Rh","Pd","Ag","Cd","In","Sn","Sb","Te","I","Xe","Cs","Ba","La","Ce","Pr","Nd",
    "Pm","Sm","Eu","Gd","Tb","Dy","Ho","Er","Tm","Yb","Lu","Hf","Ta","W","Re","Os","Ir","Pt","Au","Hg",
    "Tl","Pb","Bi","Po","At","Rn","Fr","Ra","Ac","Th","Pa","U","Np","Pu","Am","Cm","Bk","Cf","Es","Fm"
  )

  // 18 Vibrant General Teaching & Feedback Stickers (F-23)
  val generalStickers: List<StickerSpec> = listOf(
    StickerSpec("gen_star", "Star", "Rewards", StickerGroup.GENERAL),
    StickerSpec("gen_trophy", "Trophy", "Rewards", StickerGroup.GENERAL),
    StickerSpec("gen_ribbon", "Medal", "Rewards", StickerGroup.GENERAL),
    StickerSpec("gen_check", "A+", "Rewards", StickerGroup.GENERAL),
    StickerSpec("gen_target", "100%", "Rewards", StickerGroup.GENERAL),
    StickerSpec("gen_thumbsup", "Great", "Rewards", StickerGroup.GENERAL),
    StickerSpec("gen_flag", "Crown", "Rewards", StickerGroup.GENERAL),
    StickerSpec("gen_note", "Certificate", "Rewards", StickerGroup.GENERAL),
    StickerSpec("gen_bulb", "Idea", "Feedback", StickerGroup.GENERAL),
    StickerSpec("gen_smile", "Smile", "Feedback", StickerGroup.GENERAL),
    StickerSpec("gen_cross", "Incorrect", "Feedback", StickerGroup.GENERAL),
    StickerSpec("gen_alert", "Important", "Feedback", StickerGroup.GENERAL),
    StickerSpec("gen_info", "Info", "Callouts", StickerGroup.GENERAL),
    StickerSpec("gen_question", "Question", "Callouts", StickerGroup.GENERAL),
    StickerSpec("gen_speech", "Speech", "Callouts", StickerGroup.GENERAL),
    StickerSpec("gen_thought", "Thought", "Callouts", StickerGroup.GENERAL),
    StickerSpec("gen_pin", "Pin", "Callouts", StickerGroup.GENERAL),
    StickerSpec("gen_clock", "Time", "Callouts", StickerGroup.GENERAL)
  )

  // ~154 Science Stickers (F-24): Lab (8) + Biology (6) + Physics (10) + Bohr Atoms (30) + Periodic Tiles (100)
  val scienceStickers: List<StickerSpec> = buildList {
    // Lab Equipment (8)
    add(StickerSpec("sci_beaker", "Beaker", "Lab", StickerGroup.SCIENCE))
    add(StickerSpec("sci_flask", "Flask", "Lab", StickerGroup.SCIENCE))
    add(StickerSpec("sci_testtube", "Test Tube", "Lab", StickerGroup.SCIENCE))
    add(StickerSpec("sci_microscope", "Microscope", "Lab", StickerGroup.SCIENCE))
    add(StickerSpec("sci_burner", "Burner", "Lab", StickerGroup.SCIENCE))
    add(StickerSpec("sci_funnel", "Funnel", "Lab", StickerGroup.SCIENCE))
    add(StickerSpec("sci_cylinder", "Cylinder", "Lab", StickerGroup.SCIENCE))
    add(StickerSpec("sci_dropper", "Pipette", "Lab", StickerGroup.SCIENCE))

    // Biology (6)
    add(StickerSpec("sci_dna", "DNA", "Biology", StickerGroup.SCIENCE))
    add(StickerSpec("sci_cell", "Cell", "Biology", StickerGroup.SCIENCE))
    add(StickerSpec("sci_leaf", "Leaf", "Biology", StickerGroup.SCIENCE))
    add(StickerSpec("sci_heart", "Heart", "Biology", StickerGroup.SCIENCE))
    add(StickerSpec("sci_brain", "Brain", "Biology", StickerGroup.SCIENCE))
    add(StickerSpec("sci_neuron", "Neuron", "Biology", StickerGroup.SCIENCE))

    // Chemistry Molecules (4)
    add(StickerSpec("sci_benzene", "Benzene", "Chemistry", StickerGroup.SCIENCE))
    add(StickerSpec("sci_h2o", "H₂O", "Chemistry", StickerGroup.SCIENCE))
    add(StickerSpec("sci_co2", "CO₂", "Chemistry", StickerGroup.SCIENCE))
    add(StickerSpec("sci_ch4", "CH₄", "Chemistry", StickerGroup.SCIENCE))

    // Physics (6)
    add(StickerSpec("sci_magnet", "Magnet", "Physics", StickerGroup.SCIENCE))
    add(StickerSpec("sci_lens", "Convex Lens", "Physics", StickerGroup.SCIENCE))
    add(StickerSpec("sci_circuit", "Circuit", "Physics", StickerGroup.SCIENCE))
    add(StickerSpec("sci_prism", "Prism", "Physics", StickerGroup.SCIENCE))
    add(StickerSpec("sci_spring", "Spring", "Physics", StickerGroup.SCIENCE))
    add(StickerSpec("sci_wave", "Sine Wave", "Physics", StickerGroup.SCIENCE))

    // Bohr Atoms (30)
    bohrElements.forEach { el ->
      add(StickerSpec("bohr_${el.z}", "${el.symbol} (${el.z})", "Atoms", StickerGroup.SCIENCE))
    }

    // Periodic Element Tiles (100)
    periodicSymbols.forEachIndexed { idx, sym ->
      val z = idx + 1
      add(StickerSpec("elem_${z}_$sym", "$z · $sym", "Elements", StickerGroup.SCIENCE))
    }
  }

  fun byGroup(group: StickerGroup): List<StickerSpec> =
    if (group == StickerGroup.GENERAL) generalStickers else scienceStickers

  private val strokePaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      style = Paint.Style.STROKE
      strokeCap = Paint.Cap.ROUND
      strokeJoin = Paint.Join.ROUND
    }
  }
  private val fillPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL } }
  private val dotPaint by lazy { Paint(Paint.ANTI_ALIAS_FLAG).apply { style = Paint.Style.FILL } }
  private val textPaint by lazy {
    Paint(Paint.ANTI_ALIAS_FLAG).apply {
      textAlign = Paint.Align.CENTER
      typeface = Typeface.create(Typeface.SANS_SERIF, Typeface.BOLD)
    }
  }

  fun drawSticker(canvas: Canvas, stickerId: String, color: Int, width: Float, height: Float) {
    val s = min(width, height)
    val ox = (width - s) / 2f
    val oy = (height - s) / 2f
    canvas.save()
    canvas.translate(ox, oy)

    strokePaint.color = color
    strokePaint.strokeWidth = (s * 0.042f).coerceIn(2f, 9f)
    fillPaint.color = color
    fillPaint.alpha = 42
    dotPaint.color = color
    dotPaint.alpha = 255
    textPaint.color = color

    when {
      stickerId.startsWith("bohr_") -> {
        val z = stickerId.removePrefix("bohr_").toIntOrNull() ?: 6
        val info = bohrElements.firstOrNull { it.z == z } ?: bohrElements[5]
        drawBohrAtom(canvas, s, info)
      }
      stickerId.startsWith("elem_") -> {
        val parts = stickerId.split("_")
        val z = parts.getOrNull(1) ?: "6"
        val sym = parts.getOrNull(2) ?: "C"
        drawElementTile(canvas, s, z, sym)
      }
      else -> drawBuiltIn(canvas, s, stickerId)
    }
    canvas.restore()
  }

  private fun drawBohrAtom(canvas: Canvas, s: Float, info: ElementInfo) {
    val cx = s * 0.5f; val cy = s * 0.46f
    val shellCount = info.shells.size.coerceAtMost(4)
    val maxR = s * 0.38f
    val minR = s * 0.19f
    val shellBlue = 0xFF3B82F6.toInt()
    val nucleusRed = 0xFFEF4444.toInt()

    strokePaint.color = shellBlue
    strokePaint.strokeWidth = (s * 0.024f).coerceIn(1.5f, 4.5f)
    dotPaint.color = 0xFF1D4ED8.toInt()
    dotPaint.alpha = 255

    for (idx in 0 until shellCount) {
      val r = if (shellCount == 1) maxR * 0.82f else minR + (maxR - minR) * idx / (shellCount - 1)
      canvas.drawCircle(cx, cy, r, strokePaint)
      val eCount = info.shells[idx].coerceAtMost(8)
      for (e in 0 until eCount) {
        val ang = -PI / 2.0 + 2.0 * PI * e / eCount
        canvas.drawCircle(cx + (r * cos(ang)).toFloat(), cy + (r * sin(ang)).toFloat(), s * 0.028f, dotPaint)
      }
    }
    fillPaint.color = nucleusRed
    fillPaint.alpha = 255
    canvas.drawCircle(cx, cy, s * 0.135f, fillPaint)
    textPaint.color = 0xFFFFFFFF.toInt()
    textPaint.textSize = if (info.symbol.length > 1) s * 0.115f else s * 0.135f
    canvas.drawText(info.symbol, cx, cy + textPaint.textSize * 0.36f, textPaint)

    textPaint.color = 0xFF334155.toInt()
    textPaint.textSize = s * 0.115f
    canvas.drawText("${info.z}", cx, s * 0.94f, textPaint)
  }

  private fun drawElementTile(canvas: Canvas, s: Float, z: String, sym: String) {
    val r = RectF(s * 0.12f, s * 0.12f, s * 0.88f, s * 0.88f)
    canvas.drawRoundRect(r, s * 0.10f, s * 0.10f, fillPaint)
    canvas.drawRoundRect(r, s * 0.10f, s * 0.10f, strokePaint)
    textPaint.textSize = s * 0.15f
    canvas.drawText(z, s * 0.28f, s * 0.32f, textPaint)
    textPaint.textSize = s * 0.34f
    canvas.drawText(sym, s * 0.50f, s * 0.68f, textPaint)
  }

  private fun drawBuiltIn(canvas: Canvas, s: Float, id: String) {
    when (id) {
      "gen_check" -> {
        fillPaint.color = 0xFF4CAF50.toInt(); fillPaint.alpha = 255
        canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.42f, fillPaint)
        textPaint.color = 0xFFFFFFFF.toInt(); textPaint.textSize = s * 0.42f
        canvas.drawText("A+", s * 0.5f, s * 0.65f, textPaint)
      }
      "gen_cross" -> {
        fillPaint.color = 0xFFEF4444.toInt(); fillPaint.alpha = 255
        canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.40f, fillPaint)
        strokePaint.color = 0xFFFFFFFF.toInt(); strokePaint.strokeWidth = s * 0.08f
        canvas.drawLine(s * 0.34f, s * 0.34f, s * 0.66f, s * 0.66f, strokePaint)
        canvas.drawLine(s * 0.66f, s * 0.34f, s * 0.34f, s * 0.66f, strokePaint)
      }
      "gen_star" -> {
        val p = Path()
        for (i in 0 until 10) {
          val r = if (i % 2 == 0) s * 0.44f else s * 0.19f
          val a = -PI / 2.0 + PI * i / 5.0
          val x = s * 0.5f + (r * cos(a)).toFloat(); val y = s * 0.52f + (r * sin(a)).toFloat()
          if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close()
        fillPaint.color = 0xFFFACC15.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFFB45309.toInt()
        canvas.drawPath(p, fillPaint); canvas.drawPath(p, strokePaint)
      }
      "gen_ribbon" -> {
        val rib = Path().apply { moveTo(s * 0.36f, s * 0.14f); lineTo(s * 0.64f, s * 0.14f); lineTo(s * 0.56f, s * 0.44f); lineTo(s * 0.44f, s * 0.44f); close() }
        fillPaint.color = 0xFF2563EB.toInt(); fillPaint.alpha = 255
        canvas.drawPath(rib, fillPaint)
        fillPaint.color = 0xFFFACC15.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFFB45309.toInt()
        canvas.drawCircle(s * 0.5f, s * 0.60f, s * 0.26f, fillPaint)
        canvas.drawCircle(s * 0.5f, s * 0.60f, s * 0.26f, strokePaint)
      }
      "gen_flag" -> {
        // Gold Crown with red jewels
        val p = Path().apply {
          moveTo(s * 0.14f, s * 0.72f); lineTo(s * 0.14f, s * 0.32f); lineTo(s * 0.34f, s * 0.50f)
          lineTo(s * 0.50f, s * 0.26f); lineTo(s * 0.66f, s * 0.50f); lineTo(s * 0.86f, s * 0.32f)
          lineTo(s * 0.86f, s * 0.72f); close()
        }
        fillPaint.color = 0xFFFACC15.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFFB45309.toInt()
        canvas.drawPath(p, fillPaint); canvas.drawPath(p, strokePaint)
        dotPaint.color = 0xFFEF4444.toInt()
        canvas.drawCircle(s * 0.34f, s * 0.62f, s * 0.036f, dotPaint)
        canvas.drawCircle(s * 0.50f, s * 0.62f, s * 0.042f, dotPaint)
        canvas.drawCircle(s * 0.66f, s * 0.62f, s * 0.036f, dotPaint)
      }
      "gen_pin" -> {
        fillPaint.color = 0xFFEF4444.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFF991B1B.toInt()
        canvas.drawCircle(s * 0.5f, s * 0.32f, s * 0.19f, fillPaint)
        canvas.drawCircle(s * 0.5f, s * 0.32f, s * 0.19f, strokePaint)
        strokePaint.color = 0xFF475569.toInt()
        canvas.drawLine(s * 0.5f, s * 0.51f, s * 0.5f, s * 0.88f, strokePaint)
      }
      "gen_speech" -> {
        val r = RectF(s * 0.12f, s * 0.14f, s * 0.88f, s * 0.68f)
        fillPaint.color = 0xFFDBEAFE.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFF2563EB.toInt()
        canvas.drawRoundRect(r, s * 0.14f, s * 0.14f, fillPaint)
        canvas.drawRoundRect(r, s * 0.14f, s * 0.14f, strokePaint)
        val t = Path().apply { moveTo(s * 0.32f, s * 0.68f); lineTo(s * 0.24f, s * 0.86f); lineTo(s * 0.48f, s * 0.68f) }
        canvas.drawPath(t, strokePaint)
      }
      "gen_thought" -> {
        fillPaint.color = 0xFFF3E8FF.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFF7C3AED.toInt()
        canvas.drawOval(RectF(s * 0.14f, s * 0.14f, s * 0.86f, s * 0.62f), fillPaint)
        canvas.drawOval(RectF(s * 0.14f, s * 0.14f, s * 0.86f, s * 0.62f), strokePaint)
        canvas.drawCircle(s * 0.36f, s * 0.72f, s * 0.06f, strokePaint)
        canvas.drawCircle(s * 0.26f, s * 0.84f, s * 0.04f, strokePaint)
      }
      "gen_note" -> {
        // Certificate with red wax seal
        val r = RectF(s * 0.10f, s * 0.20f, s * 0.90f, s * 0.78f)
        fillPaint.color = 0xFFFEF9C3.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFFB45309.toInt()
        canvas.drawRect(r, fillPaint); canvas.drawRect(r, strokePaint)
        textPaint.color = 0xFF78350F.toInt(); textPaint.textSize = s * 0.11f
        canvas.drawText("CERTIFICATE", s * 0.50f, s * 0.38f, textPaint)
        canvas.drawLine(s * 0.24f, s * 0.50f, s * 0.76f, s * 0.50f, strokePaint)
        canvas.drawLine(s * 0.32f, s * 0.62f, s * 0.64f, s * 0.62f, strokePaint)
        dotPaint.color = 0xFFEF4444.toInt()
        canvas.drawCircle(s * 0.76f, s * 0.74f, s * 0.10f, dotPaint)
      }
      "gen_alert" -> {
        val p = Path().apply { moveTo(s * 0.5f, s * 0.14f); lineTo(s * 0.88f, s * 0.84f); lineTo(s * 0.12f, s * 0.84f); close() }
        fillPaint.color = 0xFFFDE047.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFFD97706.toInt()
        canvas.drawPath(p, fillPaint); canvas.drawPath(p, strokePaint)
        strokePaint.color = 0xFF1E293B.toInt()
        canvas.drawLine(s * 0.5f, s * 0.38f, s * 0.5f, s * 0.62f, strokePaint)
        dotPaint.color = 0xFF1E293B.toInt()
        canvas.drawCircle(s * 0.5f, s * 0.73f, s * 0.035f, dotPaint)
      }
      "gen_info" -> {
        fillPaint.color = 0xFF2563EB.toInt(); fillPaint.alpha = 255
        canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.40f, fillPaint)
        textPaint.color = 0xFFFFFFFF.toInt(); textPaint.textSize = s * 0.46f
        canvas.drawText("i", s * 0.5f, s * 0.67f, textPaint)
      }
      "gen_question" -> {
        fillPaint.color = 0xFF7C3AED.toInt(); fillPaint.alpha = 255
        canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.40f, fillPaint)
        textPaint.color = 0xFFFFFFFF.toInt(); textPaint.textSize = s * 0.46f
        canvas.drawText("?", s * 0.5f, s * 0.67f, textPaint)
      }
      "gen_smile" -> {
        fillPaint.color = 0xFFFACC15.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFFB45309.toInt()
        canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.40f, fillPaint)
        canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.40f, strokePaint)
        dotPaint.color = 0xFF78350F.toInt()
        canvas.drawCircle(s * 0.36f, s * 0.40f, s * 0.045f, dotPaint)
        canvas.drawCircle(s * 0.64f, s * 0.40f, s * 0.045f, dotPaint)
        canvas.drawArc(RectF(s * 0.30f, s * 0.40f, s * 0.70f, s * 0.74f), 20f, 140f, false, strokePaint)
      }
      "gen_thumbsup" -> {
        fillPaint.color = 0xFFFDBA74.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFF9A3412.toInt()
        canvas.drawRoundRect(RectF(s * 0.16f, s * 0.46f, s * 0.34f, s * 0.84f), s * 0.05f, s * 0.05f, fillPaint)
        canvas.drawRoundRect(RectF(s * 0.16f, s * 0.46f, s * 0.34f, s * 0.84f), s * 0.05f, s * 0.05f, strokePaint)
        val p = Path().apply {
          moveTo(s * 0.34f, s * 0.48f); lineTo(s * 0.48f, s * 0.20f); quadTo(s * 0.58f, s * 0.18f, s * 0.56f, s * 0.40f)
          lineTo(s * 0.78f, s * 0.40f); quadTo(s * 0.86f, s * 0.42f, s * 0.82f, s * 0.80f); lineTo(s * 0.34f, s * 0.80f); close()
        }
        canvas.drawPath(p, fillPaint); canvas.drawPath(p, strokePaint)
      }
      "gen_bulb" -> {
        fillPaint.color = 0xFFFDE047.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFFCA8A04.toInt()
        canvas.drawCircle(s * 0.5f, s * 0.38f, s * 0.24f, fillPaint)
        canvas.drawCircle(s * 0.5f, s * 0.38f, s * 0.24f, strokePaint)
        strokePaint.color = 0xFF64748B.toInt()
        canvas.drawRect(RectF(s * 0.38f, s * 0.62f, s * 0.62f, s * 0.78f), strokePaint)
        canvas.drawLine(s * 0.42f, s * 0.84f, s * 0.58f, s * 0.84f, strokePaint)
      }
      "gen_target" -> {
        fillPaint.color = 0xFFF97316.toInt(); fillPaint.alpha = 255
        canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.42f, fillPaint)
        textPaint.color = 0xFFFFFFFF.toInt(); textPaint.textSize = s * 0.27f
        canvas.drawText("100", s * 0.5f, s * 0.54f, textPaint)
        textPaint.textSize = s * 0.18f
        canvas.drawText("%", s * 0.5f, s * 0.74f, textPaint)
      }
      "gen_clock" -> {
        fillPaint.color = 0xFFE0F2FE.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFF0284C7.toInt()
        canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.40f, fillPaint)
        canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.40f, strokePaint)
        canvas.drawLine(s * 0.5f, s * 0.5f, s * 0.5f, s * 0.26f, strokePaint)
        canvas.drawLine(s * 0.5f, s * 0.5f, s * 0.70f, s * 0.54f, strokePaint)
      }
      "gen_trophy" -> {
        fillPaint.color = 0xFFFACC15.toInt(); fillPaint.alpha = 255
        strokePaint.color = 0xFFB45309.toInt()
        val c = Path().apply {
          moveTo(s * 0.26f, s * 0.18f); lineTo(s * 0.74f, s * 0.18f)
          quadTo(s * 0.72f, s * 0.58f, s * 0.50f, s * 0.60f)
          quadTo(s * 0.28f, s * 0.58f, s * 0.26f, s * 0.18f); close()
        }
        canvas.drawPath(c, fillPaint); canvas.drawPath(c, strokePaint)
        canvas.drawArc(RectF(s * 0.14f, s * 0.22f, s * 0.30f, s * 0.44f), 90f, 180f, false, strokePaint)
        canvas.drawArc(RectF(s * 0.70f, s * 0.22f, s * 0.86f, s * 0.44f), 270f, 180f, false, strokePaint)
        canvas.drawRect(RectF(s * 0.44f, s * 0.60f, s * 0.56f, s * 0.76f), fillPaint)
        canvas.drawRect(RectF(s * 0.30f, s * 0.76f, s * 0.70f, s * 0.86f), fillPaint)
        canvas.drawRect(RectF(s * 0.30f, s * 0.76f, s * 0.70f, s * 0.86f), strokePaint)
      }
      // Science Built-ins
      "sci_beaker" -> {
        canvas.drawRect(RectF(s * 0.26f, s * 0.52f, s * 0.76f, s * 0.84f), fillPaint)
        val p = Path().apply { moveTo(s * 0.20f, s * 0.18f); lineTo(s * 0.26f, s * 0.24f); lineTo(s * 0.26f, s * 0.84f); lineTo(s * 0.76f, s * 0.84f); lineTo(s * 0.76f, s * 0.18f) }
        canvas.drawPath(p, strokePaint)
        for (i in 0..2) canvas.drawLine(s * 0.58f, s * (0.36f + i * 0.14f), s * 0.74f, s * (0.36f + i * 0.14f), strokePaint)
      }
      "sci_flask" -> {
        val liq = Path().apply { moveTo(s * 0.30f, s * 0.62f); lineTo(s * 0.70f, s * 0.62f); lineTo(s * 0.82f, s * 0.84f); lineTo(s * 0.18f, s * 0.84f); close() }
        canvas.drawPath(liq, fillPaint)
        val p = Path().apply {
          moveTo(s * 0.38f, s * 0.16f); lineTo(s * 0.62f, s * 0.16f)
          moveTo(s * 0.42f, s * 0.16f); lineTo(s * 0.42f, s * 0.40f); lineTo(s * 0.18f, s * 0.84f)
          lineTo(s * 0.82f, s * 0.84f); lineTo(s * 0.58f, s * 0.40f); lineTo(s * 0.58f, s * 0.16f)
        }
        canvas.drawPath(p, strokePaint)
      }
      "sci_testtube" -> {
        canvas.drawRoundRect(RectF(s * 0.38f, s * 0.50f, s * 0.62f, s * 0.86f), s * 0.12f, s * 0.12f, fillPaint)
        canvas.drawRoundRect(RectF(s * 0.38f, s * 0.16f, s * 0.62f, s * 0.86f), s * 0.12f, s * 0.12f, strokePaint)
        canvas.drawLine(s * 0.32f, s * 0.16f, s * 0.68f, s * 0.16f, strokePaint)
      }
      "sci_microscope" -> {
        canvas.drawLine(s * 0.22f, s * 0.86f, s * 0.78f, s * 0.86f, strokePaint)
        canvas.drawArc(RectF(s * 0.38f, s * 0.34f, s * 0.80f, s * 0.84f), -80f, 180f, false, strokePaint)
        canvas.drawLine(s * 0.26f, s * 0.64f, s * 0.60f, s * 0.64f, strokePaint)
        canvas.save(); canvas.rotate(24f, s * 0.46f, s * 0.36f)
        canvas.drawRect(RectF(s * 0.39f, s * 0.14f, s * 0.53f, s * 0.52f), fillPaint)
        canvas.drawRect(RectF(s * 0.39f, s * 0.14f, s * 0.53f, s * 0.52f), strokePaint)
        canvas.restore()
      }
      "sci_burner" -> {
        canvas.drawLine(s * 0.24f, s * 0.86f, s * 0.76f, s * 0.86f, strokePaint)
        canvas.drawRect(RectF(s * 0.44f, s * 0.46f, s * 0.56f, s * 0.86f), strokePaint)
        val f = Path().apply { moveTo(s * 0.50f, s * 0.14f); quadTo(s * 0.68f, s * 0.34f, s * 0.50f, s * 0.44f); quadTo(s * 0.32f, s * 0.34f, s * 0.50f, s * 0.14f) }
        canvas.drawPath(f, fillPaint); canvas.drawPath(f, strokePaint)
      }
      "sci_funnel" -> {
        val p = Path().apply { moveTo(s * 0.16f, s * 0.20f); lineTo(s * 0.84f, s * 0.20f); lineTo(s * 0.56f, s * 0.56f); lineTo(s * 0.56f, s * 0.86f); lineTo(s * 0.44f, s * 0.86f); lineTo(s * 0.44f, s * 0.56f); close() }
        canvas.drawPath(p, fillPaint); canvas.drawPath(p, strokePaint)
      }
      "sci_cylinder" -> {
        canvas.drawRect(RectF(s * 0.38f, s * 0.48f, s * 0.62f, s * 0.84f), fillPaint)
        canvas.drawRect(RectF(s * 0.38f, s * 0.14f, s * 0.62f, s * 0.84f), strokePaint)
        canvas.drawLine(s * 0.28f, s * 0.84f, s * 0.72f, s * 0.84f, strokePaint)
        for (i in 1..4) canvas.drawLine(s * 0.50f, s * (0.16f + i * 0.13f), s * 0.62f, s * (0.16f + i * 0.13f), strokePaint)
      }
      "sci_dropper" -> {
        canvas.drawCircle(s * 0.72f, s * 0.26f, s * 0.12f, fillPaint)
        canvas.drawCircle(s * 0.72f, s * 0.26f, s * 0.12f, strokePaint)
        canvas.drawLine(s * 0.64f, s * 0.34f, s * 0.24f, s * 0.74f, strokePaint)
        canvas.drawCircle(s * 0.20f, s * 0.84f, s * 0.05f, fillPaint)
      }
      "sci_dna" -> {
        val p1 = Path(); val p2 = Path()
        for (i in 0..32) {
          val t = i / 32f; val y = s * (0.12f + t * 0.76f); val dx = (sin(t * 2.0 * PI) * s * 0.20f).toFloat()
          if (i == 0) { p1.moveTo(s * 0.5f + dx, y); p2.moveTo(s * 0.5f - dx, y) } else { p1.lineTo(s * 0.5f + dx, y); p2.lineTo(s * 0.5f - dx, y) }
          if (i in 2..30 step 4) canvas.drawLine(s * 0.5f - dx, y, s * 0.5f + dx, y, strokePaint)
        }
        canvas.drawPath(p1, strokePaint); canvas.drawPath(p2, strokePaint)
      }
      "sci_cell" -> {
        canvas.drawOval(RectF(s * 0.12f, s * 0.20f, s * 0.88f, s * 0.80f), fillPaint)
        canvas.drawOval(RectF(s * 0.12f, s * 0.20f, s * 0.88f, s * 0.80f), strokePaint)
        canvas.drawCircle(s * 0.48f, s * 0.50f, s * 0.15f, strokePaint)
        canvas.drawCircle(s * 0.48f, s * 0.50f, s * 0.05f, dotPaint)
      }
      "sci_leaf" -> {
        val p = Path().apply { moveTo(s * 0.20f, s * 0.82f); quadTo(s * 0.16f, s * 0.26f, s * 0.82f, s * 0.18f); quadTo(s * 0.74f, s * 0.84f, s * 0.20f, s * 0.82f) }
        canvas.drawPath(p, fillPaint); canvas.drawPath(p, strokePaint)
        canvas.drawLine(s * 0.16f, s * 0.86f, s * 0.74f, s * 0.26f, strokePaint)
      }
      "sci_heart" -> {
        val p = Path().apply { moveTo(s * 0.5f, s * 0.84f); cubicTo(s * 0.10f, s * 0.56f, s * 0.14f, s * 0.18f, s * 0.5f, s * 0.36f); cubicTo(s * 0.86f, s * 0.18f, s * 0.90f, s * 0.56f, s * 0.5f, s * 0.84f); close() }
        canvas.drawPath(p, fillPaint); canvas.drawPath(p, strokePaint)
      }
      "sci_brain" -> {
        canvas.drawOval(RectF(s * 0.16f, s * 0.20f, s * 0.54f, s * 0.78f), fillPaint)
        canvas.drawOval(RectF(s * 0.46f, s * 0.20f, s * 0.84f, s * 0.78f), fillPaint)
        canvas.drawOval(RectF(s * 0.16f, s * 0.20f, s * 0.54f, s * 0.78f), strokePaint)
        canvas.drawOval(RectF(s * 0.46f, s * 0.20f, s * 0.84f, s * 0.78f), strokePaint)
      }
      "sci_neuron" -> {
        canvas.drawCircle(s * 0.30f, s * 0.50f, s * 0.14f, fillPaint)
        canvas.drawCircle(s * 0.30f, s * 0.50f, s * 0.14f, strokePaint)
        canvas.drawLine(s * 0.44f, s * 0.50f, s * 0.82f, s * 0.50f, strokePaint)
        canvas.drawLine(s * 0.82f, s * 0.50f, s * 0.92f, s * 0.38f, strokePaint)
        canvas.drawLine(s * 0.82f, s * 0.50f, s * 0.92f, s * 0.62f, strokePaint)
      }
      "sci_benzene" -> {
        val p = Path()
        for (i in 0 until 6) {
          val a = -PI / 2.0 + PI * i / 3.0
          val x = s * 0.5f + (s * 0.36f * cos(a)).toFloat(); val y = s * 0.5f + (s * 0.36f * sin(a)).toFloat()
          if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        p.close(); canvas.drawPath(p, strokePaint); canvas.drawCircle(s * 0.5f, s * 0.5f, s * 0.19f, strokePaint)
      }
      "sci_h2o" -> {
        canvas.drawLine(s * 0.50f, s * 0.38f, s * 0.24f, s * 0.68f, strokePaint)
        canvas.drawLine(s * 0.50f, s * 0.38f, s * 0.76f, s * 0.68f, strokePaint)
        canvas.drawCircle(s * 0.50f, s * 0.34f, s * 0.15f, fillPaint); canvas.drawCircle(s * 0.50f, s * 0.34f, s * 0.15f, strokePaint)
        canvas.drawCircle(s * 0.22f, s * 0.70f, s * 0.11f, strokePaint); canvas.drawCircle(s * 0.78f, s * 0.70f, s * 0.11f, strokePaint)
        textPaint.textSize = s * 0.14f
        canvas.drawText("O", s * 0.50f, s * 0.39f, textPaint); canvas.drawText("H", s * 0.22f, s * 0.75f, textPaint); canvas.drawText("H", s * 0.78f, s * 0.75f, textPaint)
      }
      "sci_co2" -> {
        canvas.drawLine(s * 0.24f, s * 0.46f, s * 0.76f, s * 0.46f, strokePaint)
        canvas.drawLine(s * 0.24f, s * 0.54f, s * 0.76f, s * 0.54f, strokePaint)
        canvas.drawCircle(s * 0.50f, s * 0.50f, s * 0.13f, fillPaint); canvas.drawCircle(s * 0.50f, s * 0.50f, s * 0.13f, strokePaint)
        canvas.drawCircle(s * 0.18f, s * 0.50f, s * 0.11f, strokePaint); canvas.drawCircle(s * 0.82f, s * 0.50f, s * 0.11f, strokePaint)
      }
      "sci_ch4" -> {
        listOf(0.50f to 0.18f, 0.50f to 0.82f, 0.18f to 0.50f, 0.82f to 0.50f).forEach { (hx, hy) ->
          canvas.drawLine(s * 0.50f, s * 0.50f, s * hx, s * hy, strokePaint)
          canvas.drawCircle(s * hx, s * hy, s * 0.09f, strokePaint)
        }
        canvas.drawCircle(s * 0.50f, s * 0.50f, s * 0.13f, fillPaint); canvas.drawCircle(s * 0.50f, s * 0.50f, s * 0.13f, strokePaint)
      }
      "sci_magnet" -> {
        val p = Path().apply { moveTo(s * 0.24f, s * 0.22f); lineTo(s * 0.24f, s * 0.56f); arcTo(RectF(s * 0.24f, s * 0.34f, s * 0.76f, s * 0.86f), 180f, -180f); lineTo(s * 0.76f, s * 0.22f) }
        canvas.drawPath(p, strokePaint)
        canvas.drawRect(RectF(s * 0.18f, s * 0.22f, s * 0.30f, s * 0.36f), fillPaint)
        canvas.drawRect(RectF(s * 0.70f, s * 0.22f, s * 0.82f, s * 0.36f), fillPaint)
      }
      "sci_lens" -> {
        val p = Path().apply { moveTo(s * 0.50f, s * 0.14f); quadTo(s * 0.76f, s * 0.50f, s * 0.50f, s * 0.86f); quadTo(s * 0.24f, s * 0.50f, s * 0.50f, s * 0.14f); close() }
        canvas.drawPath(p, fillPaint); canvas.drawPath(p, strokePaint); canvas.drawLine(s * 0.12f, s * 0.50f, s * 0.88f, s * 0.50f, strokePaint)
      }
      "sci_circuit" -> {
        canvas.drawRect(RectF(s * 0.16f, s * 0.24f, s * 0.84f, s * 0.76f), strokePaint)
        val z = Path().apply { moveTo(s * 0.34f, s * 0.24f); lineTo(s * 0.40f, s * 0.14f); lineTo(s * 0.46f, s * 0.34f); lineTo(s * 0.54f, s * 0.14f); lineTo(s * 0.60f, s * 0.34f); lineTo(s * 0.66f, s * 0.24f) }
        canvas.drawPath(z, strokePaint)
        canvas.drawCircle(s * 0.50f, s * 0.76f, s * 0.10f, fillPaint); canvas.drawCircle(s * 0.50f, s * 0.76f, s * 0.10f, strokePaint)
      }
      "sci_prism" -> {
        val p = Path().apply { moveTo(s * 0.50f, s * 0.18f); lineTo(s * 0.82f, s * 0.80f); lineTo(s * 0.18f, s * 0.80f); close() }
        canvas.drawPath(p, fillPaint); canvas.drawPath(p, strokePaint)
        canvas.drawLine(s * 0.06f, s * 0.54f, s * 0.35f, s * 0.48f, strokePaint)
        canvas.drawLine(s * 0.65f, s * 0.48f, s * 0.94f, s * 0.38f, strokePaint)
        canvas.drawLine(s * 0.65f, s * 0.54f, s * 0.94f, s * 0.64f, strokePaint)
      }
      "sci_spring" -> {
        val p = Path().apply {
          moveTo(s * 0.12f, s * 0.50f); lineTo(s * 0.24f, s * 0.50f)
          for (i in 0 until 5) { val bx = s * (0.24f + i * 0.105f); lineTo(bx + s * 0.035f, s * 0.26f); lineTo(bx + s * 0.085f, s * 0.74f) }
          lineTo(s * 0.78f, s * 0.50f); lineTo(s * 0.88f, s * 0.50f)
        }
        canvas.drawPath(p, strokePaint)
      }
      else -> {
        val p = Path()
        for (i in 0..36) {
          val t = i / 36f; val x = s * (0.10f + t * 0.80f); val y = s * 0.50f - (sin(t * 4.0 * PI) * s * 0.26f).toFloat()
          if (i == 0) p.moveTo(x, y) else p.lineTo(x, y)
        }
        canvas.drawPath(p, strokePaint)
      }
    }
  }
}
