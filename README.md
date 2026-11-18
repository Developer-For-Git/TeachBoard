# TeachBoard — Offline Android Teaching Whiteboard

**TeachBoard** (`com.teachboard.app`) is a 100% free, 100% offline-first digital teaching whiteboard for Android phones and tablets — built with Kotlin, Jetpack Compose, Material 3, Room, DataStore, Media3, CameraX, and on-device Google ML Kit.

- **Zero Ads · Zero Subscriptions · Zero Login · Zero Servers**
- The only optional network requests are Wikimedia Commons image search and on-demand ML Kit language model downloads.

## Highlights (37 Built-In Features)
- **Drawing & Brushes:** Smooth Pen, Soft Brush, Marker, Multiply Highlighter, 45° Calligraphy Nib, Pressure-sensitive Stylus & S-Pen eraser button, Palm Rejection & Stylus-Only mode, plus Ink/Pixel Eraser (`PorterDuff.Mode.CLEAR` inside an ink-only layer that never erases board colors, textures, or PDFs) and Whole-Stroke Eraser.
- **Smart Shapes (15 Classes):** Pure-geometry offline detector for Lines, Circles, Ovals, Triangles, Squares, Rectangles, Rhombuses, Parallelograms, Trapezoids, Kites, Pentagons, Hexagons, Heptagons, and Octagons with instant candidate chips + *"Keep as drawn"*.
- **Smart Writing (18 Languages):** On-device ML Kit Digital Ink Recognition converting handwriting to text after a 1.5s pause across all 18 UI languages.
- **45 Shapes Library:** 20 2D Shapes, 10 3D Shapes (with dashed hidden edges), 7 Math & Science graphs/axes, and 8 Diagram tools with fill color & opacity.
- **12 Board Colors & 11 Textures:** Plain, Grid, Lines, Dots, Chalk, Isometric, Graph, Music Staff, Diamond, Notebook, and Whiteboard + custom RGB color picker.
- **Rich Classroom Objects:** Multi-line styled Text, LaTeX Math Formula builder with 40-symbol quick palette, Editable Tables up to 10×10, Up to 10 Layers, Multi-page Boards, Gallery/Camera Images, Wikimedia Commons Image Search with attribution, 18 General Stickers, and 150+ procedural Science Stickers (Lab, Biology, Chemistry, Physics, Bohr Atoms & Periodic Elements).
- **Teaching Overlays:** Wood-style Geometry Box (Ruler, Protractor with live angle arm, Compass, 30-60-90 & 45-45-90 Set Squares), Standard & Scientific Calculator, Countdown Timer, Media3 ExoPlayer Floating Video Window, 1-second fading Laser Pointer, and Unlimited H.264/AAC Screen Recording with draggable CameraX Face Cam bubble.
- **PDF Annotator & Exporters:** Lazy 3-page `PdfRenderer` cache (opens 50-page PDFs without OOM), bookmarks, page hide toggle, quick PDF annotation bar, 1.5× PNG Gallery export, PDF export (this page / all pages / custom range), and full **Zip Backup & Restore**.
- **18 Languages & RTL Support:** Automatic in-app locale switching via `AppCompatDelegate.setApplicationLocales` including full RTL layout for Arabic & Urdu.

## Prerequisites
- **JDK 21** (e.g. Eclipse Temurin 21)
- **Android SDK** with Platform 36 (`compileSdk = 36`, `targetSdk = 35`, `minSdk = 26`)

## Build & Run Commands
```bash
# Run JVM unit tests (Serialization codecs, Smart Shapes, Expression Evaluator, Formula Parser)
./gradlew :app:testDebugUnitTest

# Assemble debug APK
./gradlew :app:assembleDebug

# Install & launch on a connected device via ADB
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n com.teachboard.app/.MainActivity

# Build Play Store App Bundle (R8 minified, ABI/density/language splits)
./gradlew :app:bundleRelease
```

## Branding & Theme Customization
- App name and localized strings: [`app/src/main/res/values/strings.xml`](app/src/main/res/values/strings.xml)
- All color tokens & configuration constants: [`app/src/main/java/com/teachboard/app/ui/theme/Theme.kt`](app/src/main/java/com/teachboard/app/ui/theme/Theme.kt)
