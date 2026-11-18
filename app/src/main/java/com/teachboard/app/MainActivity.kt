package com.teachboard.app

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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

class MainActivity : AppCompatActivity() {

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()
    val app = application as TeachBoardApp

    setContent {
      val prefs by app.prefs.prefs.collectAsState(initial = UserPrefs())
      TeachBoardTheme(themeMode = prefs.themeMode) {
        val navController = rememberNavController()
        NavHost(navController = navController, startDestination = "splash") {
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
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
            LandingHomeScreen(
              app = app,
              viewModel = vm,
              onNewBoardCreated = { sessionId -> navController.navigate("canvas/$sessionId") },
              onOpenHistory = { navController.navigate("history") },
              onOpenSettings = { navController.navigate("settings") }
            )
          }
          composable("history") {
            val vm: HomeViewModel = viewModel(factory = HomeViewModel.factory(app))
            HistoryGalleryScreen(
              viewModel = vm,
              onBack = { navController.popBackStack() },
              onOpenBoard = { sessionId -> navController.navigate("canvas/$sessionId") }
            )
          }
          composable(
            route = "canvas/{sessionId}",
            arguments = listOf(navArgument("sessionId") { type = NavType.StringType })
          ) { entry ->
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
          composable("settings") {
            SettingsScreen(
              app = app,
              onBack = { navController.popBackStack() },
              onOpenHelp = { navController.navigate("help") }
            )
          }
          composable("help") {
            HelpScreen(onBack = { navController.popBackStack() })
          }
        }
      }
    }
  }
}
