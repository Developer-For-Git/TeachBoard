package com.teachboard.app.ui.tour

import android.content.Context
import androidx.compose.runtime.compositionLocalOf

val LocalToolHelp = compositionLocalOf<(String) -> Unit> { {} }

object TeachBoardTour {
    const val PREFS_HOME = "home_tour"
    const val PREFS_BOARD = "board_tour"
    const val TOUR_VERSION = 1

    fun homeSteps(): List<TourStep> = listOf(
        TourStep(
            id = "home_new",
            title = "Start with a blank board",
            description = "Create a new board to start writing, drawing, and teaching immediately. Boards save automatically on your device.",
            highlight = TourHighlight.CANVAS,
            targetKeys = listOf("home_new"),
            labels = mapOf("home_new" to "New Board"),
        ),
        TourStep(
            id = "home_settings",
            title = "Settings & preferences",
            description = "Customize preferences, switch between languages, or replay this walkthrough anytime.",
            highlight = TourHighlight.TOP_CONTROLS,
            targetKeys = listOf("home_settings"),
            labels = mapOf("home_settings" to "Settings"),
        ),
    )

    fun boardSteps(): List<TourStep> = listOf(
        TourStep(
            id = "controls",
            title = "Quick controls",
            description = "Pages, undo/redo, and canvas settings live up here.",
            highlight = TourHighlight.TOP_CONTROLS,
            targetKeys = listOf("pages", "undo", "export"),
            labels = mapOf(
                "pages" to "Pages",
                "undo" to "Undo",
                "export" to "Export",
            ),
        ),
        TourStep(
            id = "toolbar",
            title = "25+ tools, one swipe away",
            description = "Slide the bar horizontally to find pens, shapes, media, and tools.",
            highlight = TourHighlight.TOOLBAR,
            toolIndex = -2,
            targetKeys = listOf("toolbar"),
        ),
        TourStep(
            id = "guide",
            title = "The tool guide",
            description = "Tap the guide book anytime to browse descriptions of every tool in TeachBoard.",
            highlight = TourHighlight.TOP_CONTROLS,
            targetKeys = listOf("guide"),
            labels = mapOf("guide" to "Tool guide"),
        ),
    )

    fun resetAll(context: Context) {
        context.getSharedPreferences(PREFS_HOME, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("tour_completed", false)
            .putInt("tour_version", 0)
            .apply()

        context.getSharedPreferences(PREFS_BOARD, Context.MODE_PRIVATE)
            .edit()
            .putBoolean("tour_completed", false)
            .putInt("tour_version", 0)
            .apply()
    }
}
