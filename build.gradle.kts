// Top-level build file for TeachBoard (no Hilt, no Firebase, no ads, 100% local).
plugins {
  alias(libs.plugins.android.application) apply false
  alias(libs.plugins.compose.compiler) apply false
  alias(libs.plugins.ksp) apply false
}
