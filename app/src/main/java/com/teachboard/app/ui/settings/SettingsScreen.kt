package com.teachboard.app.ui.settings

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.Brightness4
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
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
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import com.teachboard.app.R
import com.teachboard.app.TeachBoardApp
import com.teachboard.app.data.UserPrefs
import com.teachboard.app.ui.home.LanguagePickerDialog
import com.teachboard.app.ui.home.SupportedLanguages
import com.teachboard.app.ui.theme.AppConfig
import com.teachboard.app.ui.theme.TeachColors
import com.teachboard.app.ui.theme.ThemeMode
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
  app: TeachBoardApp,
  onBack: () -> Unit,
  onOpenHelp: () -> Unit = {}
) {
  val scope = rememberCoroutineScope()
  val prefs by app.prefs.prefs.collectAsState(initial = UserPrefs())
  var showLangDialog by remember { mutableStateOf(false) }
  var showPrivacyDialog by remember { mutableStateOf(false) }

  val currentLang = SupportedLanguages.firstOrNull { it.tag == prefs.languageTag }
    ?: SupportedLanguages.first()

  Scaffold(
    topBar = {
      TopAppBar(
        title = {
          Text(
            stringResource(R.string.settings_title),
            fontWeight = FontWeight.Bold,
            fontSize = 20.sp
          )
        },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = stringResource(R.string.action_back))
          }
        }
      )
    }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .verticalScroll(rememberScrollState())
        .padding(horizontal = 20.dp, vertical = 16.dp),
      verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
      // 1. App Language Card
      SettingCard(
        icon = Icons.Default.Language,
        iconTint = TeachColors.Primary,
        iconBg = TeachColors.Primary.copy(alpha = 0.12f),
        title = stringResource(R.string.language_title),
        subtitle = "${currentLang.nativeName} (${currentLang.englishName})",
        onClick = { showLangDialog = true }
      )

      // 2. Dark Mode Card (Home Screen Only)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .clip(RoundedCornerShape(22.dp))
          .background(MaterialTheme.colorScheme.surfaceVariant)
          .padding(18.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier.fillMaxWidth()
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(Color(0xFF6366F1).copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Brightness4,
              contentDescription = null,
              tint = Color(0xFF6366F1),
              modifier = Modifier.size(22.dp)
            )
          }

          Spacer(Modifier.width(14.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = stringResource(R.string.settings_theme),
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = MaterialTheme.colorScheme.onSurface
            )
            Text(
              text = stringResource(R.string.theme_system),
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        }

        Spacer(Modifier.height(14.dp))

        Row(
          horizontalArrangement = Arrangement.spacedBy(10.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          val modes = listOf(
            ThemeMode.LIGHT to stringResource(R.string.theme_light),
            ThemeMode.DARK to stringResource(R.string.theme_dark),
            ThemeMode.SYSTEM to stringResource(R.string.theme_system)
          )
          modes.forEach { (mode, label) ->
            val isSelected = prefs.themeMode == mode
            FilterChip(
              selected = isSelected,
              onClick = { scope.launch { app.prefs.setThemeMode(mode) } },
              label = {
                Text(
                  text = label,
                  fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                  fontSize = 13.sp
                )
              },
              shape = RoundedCornerShape(14.dp),
              colors = FilterChipDefaults.filterChipColors(
                selectedContainerColor = TeachColors.Primary,
                selectedLabelColor = Color.White
              ),
              modifier = Modifier.weight(1f)
            )
          }
        }
      }

      // 3. Privacy Policy Card
      SettingCard(
        icon = Icons.Default.Security,
        iconTint = Color(0xFF10B981),
        iconBg = Color(0xFF10B981).copy(alpha = 0.12f),
        title = stringResource(R.string.settings_privacy_policy),
        subtitle = "100% Offline & Private · Zero Data Collection",
        onClick = { showPrivacyDialog = true }
      )

      Spacer(Modifier.height(12.dp))

      // 4. App Info Footer (Competitor Minimalist Style)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(vertical = 12.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Icon(
          Icons.Default.Info,
          contentDescription = null,
          modifier = Modifier.size(16.dp),
          tint = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
        )
        Spacer(Modifier.width(8.dp))
        Text(
          "${AppConfig.APP_NAME} v1.0.6 · Offline Digital Whiteboard",
          fontSize = 12.sp,
          color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f),
          fontWeight = FontWeight.Medium
        )
      }
    }
  }

  // Language Selection Dialog (No Emojis, Clean UI)
  if (showLangDialog) {
    LanguagePickerDialog(
      currentTag = prefs.languageTag,
      onSelectLanguage = { tag ->
        scope.launch { app.prefs.setLanguageTag(tag) }
        val locales = if (tag.isBlank()) LocaleListCompat.getEmptyLocaleList() else LocaleListCompat.forLanguageTags(tag)
        AppCompatDelegate.setApplicationLocales(locales)
      },
      onDismiss = { showLangDialog = false }
    )
  }

  // In-App Privacy Policy Dialog
  if (showPrivacyDialog) {
    PrivacyPolicyDialog(onDismiss = { showPrivacyDialog = false })
  }
}

@Composable
private fun SettingCard(
  icon: androidx.compose.ui.graphics.vector.ImageVector,
  iconTint: Color,
  iconBg: Color,
  title: String,
  subtitle: String,
  onClick: () -> Unit
) {
  Row(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(22.dp))
      .background(MaterialTheme.colorScheme.surfaceVariant)
      .clickable(onClick = onClick)
      .padding(18.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    Box(
      modifier = Modifier
        .size(42.dp)
        .clip(CircleShape)
        .background(iconBg),
      contentAlignment = Alignment.Center
    ) {
      Icon(
        imageVector = icon,
        contentDescription = null,
        tint = iconTint,
        modifier = Modifier.size(22.dp)
      )
    }

    Spacer(Modifier.width(14.dp))

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = 16.sp,
        color = MaterialTheme.colorScheme.onSurface
      )
      Spacer(Modifier.height(2.dp))
      Text(
        text = subtitle,
        fontSize = 12.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
      )
    }
  }
}

@Composable
private fun PrivacyPolicyDialog(onDismiss: () -> Unit) {
  AlertDialog(
    onDismissRequest = onDismiss,
    title = {
      Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
          Icons.Default.Security,
          contentDescription = null,
          tint = Color(0xFF10B981),
          modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(10.dp))
        Text(
          text = stringResource(R.string.settings_privacy_policy),
          fontWeight = FontWeight.Bold,
          fontSize = 18.sp
        )
      }
    },
    text = {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .verticalScroll(rememberScrollState()),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        PolicySection(
          title = "1. Completely Offline",
          body = "TeachBoard is built as an offline digital whiteboard. All your sketches, equations, notes, and boards remain exclusively on your device."
        )
        PolicySection(
          title = "2. Zero Data Collection",
          body = "We do not collect, monitor, track, sell, or transmit any personal data, drawing content, or usage information to any remote servers."
        )
        PolicySection(
          title = "3. No Advertisements & No Tracking",
          body = "TeachBoard contains no third-party advertisements, tracking pixels, or telemetry SDKs. Your creative workspace is 100% private."
        )
        PolicySection(
          title = "4. Device Permissions",
          body = "Camera and microphone permissions are requested solely for optional on-device actions (inserting photos or recording video lessons with voice). Files are saved directly to your local storage."
        )
      }
    },
    confirmButton = {
      TextButton(onClick = onDismiss) {
        Text(
          stringResource(R.string.action_close),
          fontWeight = FontWeight.Bold,
          color = TeachColors.Primary
        )
      }
    },
    shape = RoundedCornerShape(24.dp)
  )
}

@Composable
private fun PolicySection(title: String, body: String) {
  Column {
    Text(
      text = title,
      fontWeight = FontWeight.Bold,
      fontSize = 13.sp,
      color = MaterialTheme.colorScheme.onSurface
    )
    Spacer(Modifier.height(2.dp))
    Text(
      text = body,
      fontSize = 12.sp,
      color = MaterialTheme.colorScheme.onSurfaceVariant,
      lineHeight = 17.sp
    )
  }
}
