package com.teachboard.app.ui.settings

import android.content.Intent
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Backup
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Restore
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.teachboard.app.R
import com.teachboard.app.TeachBoardApp
import com.teachboard.app.data.UserPrefs
import com.teachboard.app.domain.export.Exporters
import com.teachboard.app.domain.model.BackgroundStyle
import com.teachboard.app.ui.theme.AppConfig
import com.teachboard.app.ui.theme.TeachColors
import com.teachboard.app.ui.theme.ThemeMode
import kotlinx.coroutines.launch

private val AppLanguages = listOf(
  "" to "System Default",
  "en" to "English",
  "es" to "Español",
  "fr" to "Français",
  "de" to "Deutsch",
  "pt" to "Português",
  "it" to "Italiano",
  "ru" to "Русский",
  "tr" to "Türkçe",
  "ar" to "العربية (RTL)",
  "hi" to "हिन्दी",
  "bn" to "বাংলা",
  "ur" to "اردو (RTL)",
  "id" to "Bahasa Indonesia",
  "vi" to "Tiếng Việt",
  "zh-CN" to "简体中文",
  "ja" to "日本語",
  "ko" to "한국어",
  "sw" to "Kiswahili"
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  app: TeachBoardApp,
  onBack: () -> Unit,
  onOpenHelp: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val prefs by app.prefs.prefs.collectAsState(initial = UserPrefs())
  val snackbar = remember { SnackbarHostState() }
  var showLangDialog by remember { mutableStateOf(false) }

  // F-32 Backup to Zip & Restore from Zip via Storage Access Framework
  val backupLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri ->
    if (uri != null) {
      scope.launch {
        val ok = Exporters.exportBackupZip(app, app.database, uri)
        snackbar.showSnackbar(if (ok) "Backup saved successfully" else "Backup failed")
      }
    }
  }

  val restoreLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
    if (uri != null) {
      scope.launch {
        val ok = Exporters.restoreBackupZip(app, app.database, uri)
        snackbar.showSnackbar(if (ok) "Backup restored successfully" else "Restore failed — invalid archive")
      }
    }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.settings_title), fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        }
      )
    },
    snackbarHost = { SnackbarHost(snackbar) }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
      // Appearance & Language
      SectionTitle(stringResource(R.string.settings_appearance))
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        ThemeMode.entries.forEach { mode ->
          FilterChip(
            selected = prefs.themeMode == mode,
            onClick = { scope.launch { app.prefs.setThemeMode(mode) } },
            label = { Text(mode.name.lowercase().replaceFirstChar { it.uppercase() }) }
          )
        }
      }

      val currentLangName = AppLanguages.firstOrNull { it.first == prefs.languageTag }?.second ?: "System Default"
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(12.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .clickable { showLangDialog = true }
          .padding(14.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(Icons.Default.Language, contentDescription = null, tint = TeachColors.Primary)
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
          Text(stringResource(R.string.language_title), fontWeight = FontWeight.SemiBold)
          Text(currentLangName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      HorizontalDivider()

      // Input & Drawing
      SectionTitle(stringResource(R.string.settings_drawing))
      ToggleRow(
        title = stringResource(R.string.palm_rejection_title),
        subtitle = stringResource(R.string.palm_rejection_desc),
        checked = prefs.palmRejection,
        onCheckedChange = { scope.launch { app.prefs.setPalmRejection(it) } }
      )
      ToggleRow(
        title = stringResource(R.string.stylus_only_title),
        subtitle = stringResource(R.string.stylus_only_desc),
        checked = prefs.stylusOnly,
        onCheckedChange = { scope.launch { app.prefs.setStylusOnly(it) } }
      )
      ToggleRow(
        title = stringResource(R.string.smart_shapes_title),
        subtitle = stringResource(R.string.smart_shapes_desc),
        checked = prefs.smartShapesEnabled,
        onCheckedChange = { scope.launch { app.prefs.setSmartShapesEnabled(it) } }
      )
      ToggleRow(
        title = stringResource(R.string.auto_save_title),
        subtitle = stringResource(R.string.auto_save_desc),
        checked = prefs.autoSaveEnabled,
        onCheckedChange = { scope.launch { app.prefs.setAutoSave(it) } }
      )

      Text(stringResource(R.string.default_board_color), fontWeight = FontWeight.Medium, fontSize = 13.sp)
      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        TeachColors.ClassicBoardPalette.forEach { (_, c) ->
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(Color(c))
              .border(if (prefs.defaultBackgroundColor == c) 3.dp else 1.dp, if (prefs.defaultBackgroundColor == c) TeachColors.Primary else MaterialTheme.colorScheme.outline, CircleShape)
              .clickable { scope.launch { app.prefs.setDefaultBackground(c, prefs.defaultBackgroundStyle) } }
          )
        }
      }

      Row(
        modifier = Modifier.fillMaxWidth().horizontalScroll(rememberScrollState()),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        BackgroundStyle.entries.forEach { style ->
          FilterChip(
            selected = prefs.defaultBackgroundStyle == style,
            onClick = { scope.launch { app.prefs.setDefaultBackground(prefs.defaultBackgroundColor, style) } },
            label = { Text(style.name.lowercase().replaceFirstChar { it.uppercase() }, fontSize = 11.sp) }
          )
        }
      }

      HorizontalDivider()

      // Backup & Restore (F-32)
      SectionTitle(stringResource(R.string.backup_restore_title))
      Text(stringResource(R.string.backup_restore_desc), fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
      Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
        Button(
          onClick = { backupLauncher.launch("TeachBoard_Backup_${System.currentTimeMillis()}.zip") },
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Backup, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text(stringResource(R.string.backup_zip))
        }
        OutlinedButton(
          onClick = { restoreLauncher.launch(arrayOf("application/zip", "application/octet-stream")) },
          modifier = Modifier.weight(1f)
        ) {
          Icon(Icons.Default.Restore, contentDescription = null, modifier = Modifier.size(18.dp))
          Spacer(Modifier.width(6.dp))
          Text(stringResource(R.string.restore_zip))
        }
      }

      HorizontalDivider()

      // Help, Tour & About
      SectionTitle(stringResource(R.string.about_section))
      OutlinedButton(
        onClick = {
          scope.launch {
            app.prefs.resetHintsAndTour()
            snackbar.showSnackbar("Onboarding tour & hints reset")
          }
        },
        modifier = Modifier.fillMaxWidth()
      ) {
        Text(stringResource(R.string.reset_tour_hints))
      }
      Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        OutlinedButton(onClick = onOpenHelp, modifier = Modifier.weight(1f)) {
          Text(stringResource(R.string.tool_help))
        }
        OutlinedButton(
          onClick = {
            val share = Intent(Intent.ACTION_SEND).apply {
              type = "text/plain"
              putExtra(Intent.EXTRA_TEXT, "${AppConfig.APP_NAME} — 100% Free Offline Teaching Whiteboard")
            }
            context.startActivity(Intent.createChooser(share, "Share"))
          },
          modifier = Modifier.weight(1f)
        ) {
          Text(stringResource(R.string.share_app))
        }
      }
      Text(
        "${AppConfig.APP_NAME} v1.0.0 · 100% Free & Offline · Zero Ads",
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }

  if (showLangDialog) {
    AlertDialog(
      onDismissRequest = { showLangDialog = false },
      title = { Text(stringResource(R.string.language_title)) },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          AppLanguages.forEach { (tag, name) ->
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .clickable {
                  showLangDialog = false
                  scope.launch { app.prefs.setLanguageTag(tag) }
                  val locales = if (tag.isBlank()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag)
                  AppCompatDelegate.setApplicationLocales(locales)
                }
                .padding(vertical = 10.dp, horizontal = 4.dp),
              verticalAlignment = Alignment.CenterVertically
            ) {
              Text(name, fontWeight = if (prefs.languageTag == tag) FontWeight.Bold else FontWeight.Normal, color = if (prefs.languageTag == tag) TeachColors.Primary else MaterialTheme.colorScheme.onSurface)
            }
          }
        }
      },
      confirmButton = {
        TextButton(onClick = { showLangDialog = false }) { Text(stringResource(R.string.close)) }
      }
    )
  }
}

@Composable
private fun SectionTitle(title: String) {
  Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TeachColors.Primary)
}

@Composable
private fun ToggleRow(
  title: String,
  subtitle: String,
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit
) {
  Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
    Column(modifier = Modifier.weight(1f)) {
      Text(title, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
      Text(subtitle, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
    Switch(checked = checked, onCheckedChange = onCheckedChange)
  }
}
