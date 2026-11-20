plugins {
  alias(libs.plugins.android.application)
  alias(libs.plugins.compose.compiler)
  alias(libs.plugins.ksp)
}

android {
  namespace = "com.teachboard.app"
  compileSdk = 36

  defaultConfig {
    // [PLACEHOLDER] Owner can rename applicationId & app_name from one place
    applicationId = "com.teachboard.app"
    minSdk = 26
    targetSdk = 35
    versionCode = 1
    versionName = "1.0.0"

    testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
  }

  buildTypes {
    debug {
      ndk {
        abiFilters += listOf("arm64-v8a", "x86_64")
      }
    }
    release {
      isMinifyEnabled = true
      isShrinkResources = true
      proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
    }
  }

  bundle {
    abi { enableSplit = true }
    density { enableSplit = true }
    language { enableSplit = true }
  }

  compileOptions {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
  }

  buildFeatures {
    compose = true
    aidl = false
    buildConfig = true
    shaders = false
  }

  androidResources {
    localeFilters += listOf(
      "en", "es", "fr", "de", "pt", "it", "ru", "tr",
      "ar", "hi", "bn", "ur", "in", "vi", "zh-rCN", "ja", "ko", "sw"
    )
  }

  packaging {
    resources {
      excludes += "/META-INF/{AL2.0,LGPL2.1}"
    }
  }
}

kotlin {
  compilerOptions {
    jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
  }
}

dependencies {
  val composeBom = platform(libs.androidx.compose.bom)
  implementation(composeBom)

  // Core Android
  implementation(libs.androidx.core.ktx)
  implementation(libs.androidx.appcompat)
  implementation(libs.androidx.lifecycle.runtime.ktx)
  implementation(libs.androidx.activity.compose)
  implementation(libs.androidx.exifinterface)

  // Compose & Navigation
  implementation(libs.androidx.lifecycle.runtime.compose)
  implementation(libs.androidx.lifecycle.viewmodel.compose)
  implementation(libs.androidx.compose.ui)
  implementation(libs.androidx.compose.ui.tooling.preview)
  implementation(libs.androidx.compose.material3)
  implementation(libs.androidx.compose.material.icons.extended)
  implementation(libs.androidx.navigation.compose)
  debugImplementation(libs.androidx.compose.ui.tooling)

  // Room (local teachboard.db)
  implementation(libs.androidx.room.runtime)
  implementation(libs.androidx.room.ktx)
  ksp(libs.androidx.room.compiler)

  // DataStore
  implementation(libs.androidx.datastore.preferences)

  // Images & CanHub Cropper
  implementation(libs.coil.compose)
  implementation(libs.canhub.image.cropper)

  // Media3 ExoPlayer (floating video player)
  implementation(libs.androidx.media3.exoplayer)
  implementation(libs.androidx.media3.ui)

  // ML Kit (on-device, free, offline-capable)
  implementation(libs.mlkit.digital.ink)
  implementation(libs.mlkit.text.latin)
  implementation(libs.mlkit.text.chinese)
  implementation(libs.mlkit.text.devanagari)
  implementation(libs.mlkit.text.japanese)
  implementation(libs.mlkit.text.korean)

  // CameraX (Face Cam bubble during Screen Recording)
  implementation(libs.androidx.camera.core)
  implementation(libs.androidx.camera.camera2)
  implementation(libs.androidx.camera.lifecycle)
  implementation(libs.androidx.camera.view)

  // Coroutines
  implementation(libs.kotlinx.coroutines.android)
  implementation(libs.kotlinx.coroutines.play.services)

  // Unit Tests
  testImplementation(libs.junit)
  testImplementation(libs.kotlinx.coroutines.test)
}
