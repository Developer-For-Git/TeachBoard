package com.teachboard.app

import android.content.Context
import android.content.ContextWrapper
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Bundle
import androidx.activity.compose.LocalActivityResultRegistryOwner
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultRegistry
import androidx.activity.result.ActivityResultRegistryOwner
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.teachboard.app.data.UserPrefs
import com.teachboard.app.ui.canvas.CanvasScreen
import com.teachboard.app.ui.canvas.CanvasViewModel
import com.teachboard.app.ui.help.HelpScreen
import com.teachboard.app.ui.home.HistoryGalleryScreen
import com.teachboard.app.ui.home.HomeViewModel
import com.teachboard.app.ui.home.LandingHomeScreen
import com.teachboard.app.ui.home.SplashScreen
import com.teachboard.app.ui.settings.SettingsScreen
import com.teachboard.app.ui.theme.TeachBoardTheme
import java.util.Locale

/**
 * Custom ContextWrapper that delegates localized resources to the configuration context
 * while preserving activity identity and forwarding ActivityResultRegistryOwner.
 */
private class LocalizedActivityContext(
  base: Context,
  private val localizedResources: Resources,
  private val activity: MainActivity
) : ContextWrapper(base), ActivityResultRegistryOwner {
  override fun getResources(): Resources = localizedResources
  override val activityResultRegistry: ActivityResultRegistry
    get() = activity.activityResultRegistry
}

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val app = application as TeachBoardApp

    setContent {
      val prefs by app.prefs.prefs.collectAsState(initial = UserPrefs())

      val localizedConfig = remember(prefs.languageTag) {
        if (prefs.languageTag.isBlank()) {
          null
        } else {
          val locale = Locale.forLanguageTag(prefs.languageTag)
          Configuration(resources.configuration).apply {
            setLocale(locale)
            setLayoutDirection(locale)
          }
        }
      }

      val localizedActivityContext = remember(prefs.languageTag, localizedConfig) {
        if (localizedConfig == null) {
          this@MainActivity
        } else {
          val confContext = createConfigurationContext(localizedConfig)
          LocalizedActivityContext(
            base = this@MainActivity,
            localizedResources = confContext.resources,
            activity = this@MainActivity
          )
        }
      }

      CompositionLocalProvider(
        LocalContext provides localizedActivityContext,
        LocalConfiguration provides (localizedConfig ?: LocalConfiguration.current),
        LocalActivityResultRegistryOwner provides this@MainActivity
      ) {
        TeachBoardTheme(themeMode = prefs.themeMode) {
          val navController = rememberNavController()
          NavHost(
            navController = navController,
            startDestination = "splash",
            enterTransition = { fadeIn(animationSpec = tween(140)) },
            exitTransition = { fadeOut(animationSpec = tween(120)) },
            popEnterTransition = { fadeIn(animationSpec = tween(140)) },
            popExitTransition = { fadeOut(animationSpec = tween(120)) }
          ) {
            composable("splash") {
              SplashScreen(
                onFinished = {
                  navController.navigate("home") {
                    popUpTo("splash") { inclusive = true }
                  }
                }
              )
            }
            composable("home") {
              key(prefs.languageTag) {
                val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
                LandingHomeScreen(
                  app = app,
                  viewModel = vm,
                  onNewBoardCreated = { sessionId -> navController.navigate("canvas/$sessionId") },
                  onOpenHistory = { navController.navigate("history") },
                  onOpenSettings = { navController.navigate("settings") }
                )
              }
            }
            composable("history") {
              key(prefs.languageTag) {
                val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
                HistoryGalleryScreen(
                  viewModel = vm,
                  onBack = { navController.popBackStack() },
                  onOpenBoard = { sessionId -> navController.navigate("canvas/$sessionId") }
                )
              }
            }
            composable(
              route = "canvas/{sessionId}",
              arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
            ) { entry ->
              key(prefs.languageTag) {
                val sessionId = entry.arguments?.getString("sessionId") ?: return@composable
                val vm: CanvasViewModel = viewModel(
                  key = "canvas_$sessionId",
                  factory = CanvasViewModel.factory(app, sessionId)
                )
                CanvasScreen(
                  viewModel = vm,
                  onNavigateHome = { navController.popBackStack() },
                  onOpenHelp = { navController.navigate("help") }
                )
              }
            }
            composable("settings") {
              key(prefs.languageTag) {
                SettingsScreen(
                  app = app,
                  onBack = { navController.popBackStack() },
                  onOpenHelp = { navController.navigate("help") }
                )
              }
            }
            composable("help") {
              key(prefs.languageTag) {
                HelpScreen(onBack = { navController.popBackStack() })
              }
            }
          }
        }
      }
    }
  }
}
