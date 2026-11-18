package com.teachboard.app.domain.model

/**
 * Internal enum of every drawing, brush, eraser, object, and shape type (PRD 19.3 & 19.10 F-02).
 * Stored by enum name in Room `canvas_objects.type`.
 */
enum class ToolType(val category: ToolCategory = ToolCategory.DRAWING) {
  // Brushes & Strokes
  Pen(ToolCategory.DRAWING),
  Brush(ToolCategory.DRAWING),
  Marker(ToolCategory.DRAWING),
  Highlighter(ToolCategory.DRAWING),
  Calligraphy(ToolCategory.DRAWING),
  Eraser(ToolCategory.DRAWING),

  // Interactive Modes
  SmartWriting(ToolCategory.MODE),
  Laser(ToolCategory.MODE),
  Lasso(ToolCategory.MODE),
  Hand(ToolCategory.MODE),

  // Rich Objects
  Text(ToolCategory.OBJECT),
  Formula(ToolCategory.OBJECT),
  Table(ToolCategory.OBJECT),
  Image(ToolCategory.OBJECT),
  Sticker(ToolCategory.OBJECT),

  // ===== 2D Shapes (20 — PRD 18.2) =====
  Line(ToolCategory.SHAPE_2D),
  Arrow(ToolCategory.SHAPE_2D),
  Rect(ToolCategory.SHAPE_2D),
  Square(ToolCategory.SHAPE_2D),
  Circle(ToolCategory.SHAPE_2D),
  Oval(ToolCategory.SHAPE_2D),
  Triangle(ToolCategory.SHAPE_2D),
  RightTriangle(ToolCategory.SHAPE_2D),
  Star(ToolCategory.SHAPE_2D),
  Pentagon(ToolCategory.SHAPE_2D),
  Hexagon(ToolCategory.SHAPE_2D),
  Octagon(ToolCategory.SHAPE_2D),
  Diamond(ToolCategory.SHAPE_2D),
  Parallelogram(ToolCategory.SHAPE_2D),
  Trapezoid(ToolCategory.SHAPE_2D),
  Heart(ToolCategory.SHAPE_2D),
  Cross(ToolCategory.SHAPE_2D),
  Semicircle(ToolCategory.SHAPE_2D),
  Sector(ToolCategory.SHAPE_2D),
  RightAngle(ToolCategory.SHAPE_2D),

  // Extra Smart-Shape internal geometry types (PRD 19.1 #11 & 19.9)
  Heptagon(ToolCategory.SHAPE_EXTRA),
  Rhombus(ToolCategory.SHAPE_EXTRA),
  Kite(ToolCategory.SHAPE_EXTRA),
  IsoscelesTriangle(ToolCategory.SHAPE_EXTRA),
  Quadrilateral(ToolCategory.SHAPE_EXTRA),

  // ===== 3D Shapes (10 — PRD 18.2) =====
  Cube(ToolCategory.SHAPE_3D),
  Cylinder(ToolCategory.SHAPE_3D),
  Cone(ToolCategory.SHAPE_3D),
  Sphere(ToolCategory.SHAPE_3D),
  Pyramid(ToolCategory.SHAPE_3D),
  Torus(ToolCategory.SHAPE_3D),
  Prism(ToolCategory.SHAPE_3D),
  Cuboid(ToolCategory.SHAPE_3D),
  Hemisphere(ToolCategory.SHAPE_3D),
  Frustum(ToolCategory.SHAPE_3D),

  // ===== Math & Science (7 — PRD 18.2) =====
  XYAxes(ToolCategory.SHAPE_MATH),
  NumberLine(ToolCategory.SHAPE_MATH),
  Sine(ToolCategory.SHAPE_MATH),
  Parabola(ToolCategory.SHAPE_MATH),
  DoubleArrow(ToolCategory.SHAPE_MATH),
  CurvedArrow(ToolCategory.SHAPE_MATH),
  Graph(ToolCategory.SHAPE_MATH),

  // ===== Diagram Tools (8 — PRD 18.2) =====
  Venn2(ToolCategory.SHAPE_DIAGRAM),
  Venn3(ToolCategory.SHAPE_DIAGRAM),
  FlowchartBox(ToolCategory.SHAPE_DIAGRAM),
  DecisionDiamond(ToolCategory.SHAPE_DIAGRAM),
  RoundedBox(ToolCategory.SHAPE_DIAGRAM),
  ConnectorLine(ToolCategory.SHAPE_DIAGRAM),
  ConnectorArrow(ToolCategory.SHAPE_DIAGRAM),
  Bracket(ToolCategory.SHAPE_DIAGRAM);

  val isBrush: Boolean
    get() = this == Pen || this == Brush || this == Marker || this == Highlighter || this == Calligraphy

  val isShape: Boolean
    get() = category == ToolCategory.SHAPE_2D ||
      category == ToolCategory.SHAPE_3D ||
      category == ToolCategory.SHAPE_MATH ||
      category == ToolCategory.SHAPE_DIAGRAM ||
      category == ToolCategory.SHAPE_EXTRA

  companion object {
    val shapesSheet2D: List<ToolType> = entries.filter { it.category == ToolCategory.SHAPE_2D }
    val shapesSheet3D: List<ToolType> = entries.filter { it.category == ToolCategory.SHAPE_3D }
    val shapesSheetMath: List<ToolType> = entries.filter { it.category == ToolCategory.SHAPE_MATH }
    val shapesSheetDiagram: List<ToolType> = entries.filter { it.category == ToolCategory.SHAPE_DIAGRAM }

    fun fromNameOrDefault(name: String): ToolType =
      runCatching { valueOf(name) }.getOrDefault(Pen)
  }
}

enum class ToolCategory {
  DRAWING, MODE, OBJECT, SHAPE_2D, SHAPE_3D, SHAPE_MATH, SHAPE_DIAGRAM, SHAPE_EXTRA
}

enum class ToolBadge {
  NONE, AI, AD, PRO
}

/**
 * Toolbar items in exact left-to-right order from PRD Section 5.
 */
enum class ToolbarAction(
  val hasOptionsIndicator: Boolean = false,
  val badge: ToolBadge = ToolBadge.NONE
) {
  SAVE,
  SHARE,
  UNDO,
  REDO,
  CLEAR,
  PEN(hasOptionsIndicator = true),
  SMART(hasOptionsIndicator = true, badge = ToolBadge.AI),
  SHAPES(hasOptionsIndicator = true),
  ERASER(hasOptionsIndicator = true),
  BOARD,
  TEXT,
  FORMULA,
  TABLE,
  LAYERS,
  IMAGE,
  VIDEO,
  WEB_IMAGE,
  CAMERA,
  OCR(badge = ToolBadge.AI),
  IMPORT_PDF,
  STICKERS,
  SCIENCE,
  LASER(hasOptionsIndicator = true),
  GEOMETRY,
  CALCULATOR,
  TIMER,
  RECORD,
  LASSO,
  HAND,
  CUSTOMIZE,
  HELP
}

enum class GeometryToolType {
  RULER, PROTRACTOR, COMPASS, SET_SQUARE_30_60, SET_SQUARE_45_45
}

enum class RecordingQuality(
  val label: String,
  val heightPx: Int,
  val bitrateBps: Int,
  val fps: Int,
  val mbPerMinEstimate: String
) {
  P480("480p", 480, 2_000_000, 30, "1.9 MB/min"),
  P720("720p", 720, 4_500_000, 30, "3.6 MB/min"),
  P1080("1080p", 1080, 8_000_000, 30, "5.6 MB/min")
}

enum class FaceCamShape {
  ROUND, SQUARE
}

enum class SortOption {
  NEWEST, OLDEST, NAME_AZ
}

enum class EraserMode {
  PIXEL_INK, STROKE
}
