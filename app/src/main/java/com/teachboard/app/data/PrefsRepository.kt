package com.teachboard.app.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.teachboard.app.domain.model.BackgroundStyle
import com.teachboard.app.domain.model.ToolType
import com.teachboard.app.domain.model.ToolbarAction
import com.teachboard.app.ui.theme.TeachColors
import com.teachboard.app.ui.theme.ThemeMode
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.settingsDataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

data class UserPrefs(
  val palmRejection: Boolean = false,
  val stylusOnly: Boolean = false,
  val defaultBackgroundColor: Int = TeachColors.BoardWhite,
  val defaultBackgroundStyle: BackgroundStyle = BackgroundStyle.PLAIN,
  val defaultBrush: ToolType = ToolType.Pen,
  val defaultBrushColor: Int = TeachColors.BrushBlack,
  val defaultBrushSize: Float = 12f,
  val defaultEraserSize: Float = 28f,
  val themeMode: ThemeMode = ThemeMode.SYSTEM,
  val languageTag: String = "",
  val autoSaveEnabled: Boolean = true,
  val smartWritingLanguage: String = "en-US",
  val smartShapesEnabled: Boolean = false,
  val tourVersionCompleted: Int = 0,
  val hintsDismissed: Set<String> = emptySet(),
  val shortcutTools: List<ToolbarAction> = emptyList(),
  val toolbarOrder: List<ToolbarAction> = ToolbarAction.entries,
  val hiddenToolbarTools: Set<ToolbarAction> = emptySet()
)

/**
 * DataStore "settings" repository (PRD 19.3 & 19.6). Owned by TeachBoardApp (no Hilt).
 */
class PrefsRepository(context: Context) {
  private val dataStore = context.applicationContext.settingsDataStore

  private object Keys {
    val PALM_REJECTION = booleanPreferencesKey("palm_rejection")
    val STYLUS_ONLY = booleanPreferencesKey("stylus_only")
    val DEFAULT_BG_COLOR = intPreferencesKey("default_bg_color")
    val DEFAULT_BG_STYLE = stringPreferencesKey("default_bg_style")
    val DEFAULT_BRUSH = stringPreferencesKey("default_brush")
    val DEFAULT_BRUSH_COLOR = intPreferencesKey("default_brush_color")
    val DEFAULT_BRUSH_SIZE = floatPreferencesKey("default_brush_size")
    val DEFAULT_ERASER_SIZE = floatPreferencesKey("default_eraser_size")
    val THEME_MODE = stringPreferencesKey("theme_mode")
    val LANGUAGE_TAG = stringPreferencesKey("language_tag")
    val AUTO_SAVE = booleanPreferencesKey("auto_save")
    val SMART_WRITING_LANG = stringPreferencesKey("smart_writing_lang")
    val SMART_SHAPES_ENABLED = booleanPreferencesKey("smart_shapes_enabled")
    val TOUR_VERSION = intPreferencesKey("tour_version")
    val HINTS_DISMISSED = stringPreferencesKey("hints_dismissed")
    val SHORTCUT_TOOLS = stringPreferencesKey("shortcut_tools")
    val TOOLBAR_ORDER = stringPreferencesKey("toolbar_order")
    val HIDDEN_TOOLBAR_TOOLS = stringPreferencesKey("hidden_toolbar_tools")
  }

  val prefs: Flow<UserPrefs> = dataStore.data.map { p ->
    val savedOrder = p[Keys.TOOLBAR_ORDER]?.split(",")?.mapNotNull { name ->
      runCatching { ToolbarAction.valueOf(name) }.getOrNull()
    }.orEmpty()
    val mergedOrder = if (savedOrder.isEmpty()) {
      ToolbarAction.entries
    } else if (ToolbarAction.PENCIL !in savedOrder && ToolbarAction.PEN in savedOrder) {
      val penIdx = savedOrder.indexOf(ToolbarAction.PEN)
      val withPencil = savedOrder.toMutableList().apply { add(penIdx + 1, ToolbarAction.PENCIL) }
      withPencil + ToolbarAction.entries.filterNot { it in withPencil }
    } else {
      (savedOrder + ToolbarAction.entries.filterNot { it in savedOrder })
    }
    val hiddenSet = p[Keys.HIDDEN_TOOLBAR_TOOLS]?.split(",")?.mapNotNull { name ->
      runCatching { ToolbarAction.valueOf(name) }.getOrNull()
    }?.toSet() ?: emptySet()

    UserPrefs(
      palmRejection = p[Keys.PALM_REJECTION] ?: false,
      stylusOnly = p[Keys.STYLUS_ONLY] ?: false,
      defaultBackgroundColor = p[Keys.DEFAULT_BG_COLOR] ?: TeachColors.BoardWhite,
      defaultBackgroundStyle = p[Keys.DEFAULT_BG_STYLE]?.let { BackgroundStyle.fromNameOrDefault(it) }
        ?: BackgroundStyle.PLAIN,
      defaultBrush = p[Keys.DEFAULT_BRUSH]?.let { ToolType.fromNameOrDefault(it) } ?: ToolType.Pen,
      defaultBrushColor = p[Keys.DEFAULT_BRUSH_COLOR] ?: TeachColors.BrushBlack,
      defaultBrushSize = p[Keys.DEFAULT_BRUSH_SIZE] ?: 12f,
      defaultEraserSize = p[Keys.DEFAULT_ERASER_SIZE] ?: 28f,
      themeMode = p[Keys.THEME_MODE]?.let { runCatching { ThemeMode.valueOf(it) }.getOrNull() }
        ?: ThemeMode.SYSTEM,
      languageTag = p[Keys.LANGUAGE_TAG] ?: "",
      autoSaveEnabled = p[Keys.AUTO_SAVE] ?: true,
      smartWritingLanguage = p[Keys.SMART_WRITING_LANG] ?: "en-US",
      smartShapesEnabled = p[Keys.SMART_SHAPES_ENABLED] ?: false,
      tourVersionCompleted = p[Keys.TOUR_VERSION] ?: 0,
      hintsDismissed = p[Keys.HINTS_DISMISSED]?.split(",")?.filter { it.isNotBlank() }?.toSet()
        ?: emptySet(),
      shortcutTools = p[Keys.SHORTCUT_TOOLS]?.split(",")?.mapNotNull { name ->
        runCatching { ToolbarAction.valueOf(name) }.getOrNull()
      } ?: emptyList(),
      toolbarOrder = mergedOrder,
      hiddenToolbarTools = hiddenSet
    )
  }

  suspend fun setPalmRejection(enabled: Boolean) {
    dataStore.edit { it[Keys.PALM_REJECTION] = enabled }
  }

  suspend fun setStylusOnly(enabled: Boolean) {
    dataStore.edit { it[Keys.STYLUS_ONLY] = enabled }
  }

  suspend fun setDefaultBackground(color: Int, style: BackgroundStyle) {
    dataStore.edit {
      it[Keys.DEFAULT_BG_COLOR] = color
      it[Keys.DEFAULT_BG_STYLE] = style.name
    }
  }

  suspend fun setDefaultBrush(brush: ToolType, color: Int, size: Float) {
    dataStore.edit {
      it[Keys.DEFAULT_BRUSH] = brush.name
      it[Keys.DEFAULT_BRUSH_COLOR] = color
      it[Keys.DEFAULT_BRUSH_SIZE] = size
    }
  }

  suspend fun setEraserSize(size: Float) {
    dataStore.edit {
      it[Keys.DEFAULT_ERASER_SIZE] = size
    }
  }

  suspend fun setThemeMode(mode: ThemeMode) {
    dataStore.edit { it[Keys.THEME_MODE] = mode.name }
  }

  suspend fun setLanguageTag(tag: String) {
    dataStore.edit { it[Keys.LANGUAGE_TAG] = tag }
  }

  suspend fun setAutoSave(enabled: Boolean) {
    dataStore.edit { it[Keys.AUTO_SAVE] = enabled }
  }

  suspend fun setSmartWritingLanguage(tag: String) {
    dataStore.edit { it[Keys.SMART_WRITING_LANG] = tag }
  }

  suspend fun setSmartShapesEnabled(enabled: Boolean) {
    dataStore.edit { it[Keys.SMART_SHAPES_ENABLED] = enabled }
  }

  suspend fun setTourCompleted(version: Int) {
    dataStore.edit { it[Keys.TOUR_VERSION] = version }
  }

  suspend fun dismissHint(hintKey: String) {
    dataStore.edit { p ->
      val current = p[Keys.HINTS_DISMISSED]?.split(",")?.filter { it.isNotBlank() }?.toMutableSet()
        ?: mutableSetOf()
      current.add(hintKey)
      p[Keys.HINTS_DISMISSED] = current.joinToString(",")
    }
  }

  suspend fun resetHintsAndTour() {
    dataStore.edit {
      it[Keys.TOUR_VERSION] = 0
      it[Keys.HINTS_DISMISSED] = ""
    }
  }

  suspend fun setShortcutTools(tools: List<ToolbarAction>) {
    dataStore.edit { it[Keys.SHORTCUT_TOOLS] = tools.joinToString(",") { t -> t.name } }
  }

  suspend fun setToolbarCustomization(order: List<ToolbarAction>, hidden: Set<ToolbarAction>) {
    dataStore.edit {
      it[Keys.TOOLBAR_ORDER] = order.joinToString(",") { t -> t.name }
      it[Keys.HIDDEN_TOOLBAR_TOOLS] = hidden.joinToString(",") { t -> t.name }
    }
  }

  suspend fun resetToolbarCustomization() {
    dataStore.edit {
      it.remove(Keys.TOOLBAR_ORDER)
      it.remove(Keys.HIDDEN_TOOLBAR_TOOLS)
    }
  }
}
