package com.teachboard.app.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.teachboard.app.ui.theme.TeachColors

data class AppLanguage(
  val tag: String,
  val nativeName: String,
  val englishName: String,
  val codeBadge: String
)

val SupportedLanguages: List<AppLanguage> = listOf(
  AppLanguage("", "System Default", "Follow Device Setting", "AUTO"),
  AppLanguage("en", "English", "English", "EN"),
  AppLanguage("es", "Español", "Spanish", "ES"),
  AppLanguage("fr", "Français", "French", "FR"),
  AppLanguage("de", "Deutsch", "German", "DE"),
  AppLanguage("pt", "Português", "Portuguese", "PT"),
  AppLanguage("it", "Italiano", "Italian", "IT"),
  AppLanguage("ru", "Русский", "Russian", "RU"),
  AppLanguage("tr", "Türkçe", "Turkish", "TR"),
  AppLanguage("ar", "العربية", "Arabic", "AR"),
  AppLanguage("hi", "हिन्दी", "Hindi", "HI"),
  AppLanguage("bn", "বাংলা", "Bengali", "BN"),
  AppLanguage("ur", "اردو", "Urdu", "UR"),
  AppLanguage("id", "Bahasa Indonesia", "Indonesian", "ID"),
  AppLanguage("vi", "Tiếng Việt", "Vietnamese", "VI"),
  AppLanguage("zh-CN", "简体中文", "Chinese", "ZH"),
  AppLanguage("ja", "日本語", "Japanese", "JA"),
  AppLanguage("ko", "한국어", "Korean", "KO"),
  AppLanguage("sw", "Kiswahili", "Swahili", "SW")
)

@Composable
fun LanguagePickerDialog(
  currentTag: String,
  onSelectLanguage: (String) -> Unit,
  onDismiss: () -> Unit
) {
  var searchQuery by remember { mutableStateOf("") }
  val config = LocalConfiguration.current
  val isLandscape = config.screenWidthDp > config.screenHeightDp

  val filteredLanguages = remember(searchQuery) {
    if (searchQuery.isBlank()) SupportedLanguages
    else SupportedLanguages.filter {
      it.nativeName.contains(searchQuery, ignoreCase = true) ||
        it.englishName.contains(searchQuery, ignoreCase = true) ||
        it.tag.contains(searchQuery, ignoreCase = true)
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false)
  ) {
    Surface(
      modifier = Modifier
        .fillMaxWidth(if (isLandscape) 0.78f else 0.92f)
        .heightIn(max = if (isLandscape) (config.screenHeightDp - 20).coerceAtLeast(300).dp else 680.dp),
      shape = RoundedCornerShape(28.dp),
      color = Color.White,
      border = BorderStroke(1.dp, Color(0xFFE2E8F0)),
      shadowElevation = 18.dp
    ) {
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(20.dp)
      ) {
        // 1. Header with Language Icon Pill, Titles and Close Button
        Row(
          modifier = Modifier.fillMaxWidth(),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Box(
            modifier = Modifier
              .size(42.dp)
              .clip(CircleShape)
              .background(Color(0xFFEEF2FF)),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Language,
              contentDescription = null,
              tint = TeachColors.Primary,
              modifier = Modifier.size(24.dp)
            )
          }

          Spacer(Modifier.width(12.dp))

          Column(modifier = Modifier.weight(1f)) {
            Text(
              text = androidx.compose.ui.res.stringResource(com.teachboard.app.R.string.language_title),
              fontSize = 20.sp,
              fontWeight = FontWeight.Bold,
              color = Color(0xFF0F172A)
            )
            Text(
              text = androidx.compose.ui.res.stringResource(com.teachboard.app.R.string.settings_language),
              fontSize = 12.sp,
              color = Color(0xFF64748B)
            )
          }

          Box(
            modifier = Modifier
              .size(36.dp)
              .clip(CircleShape)
              .background(Color(0xFFF1F5F9))
              .clickable(onClick = onDismiss),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              imageVector = Icons.Default.Close,
              contentDescription = "Close",
              tint = Color(0xFF475569),
              modifier = Modifier.size(18.dp)
            )
          }
        }

        Spacer(Modifier.height(14.dp))

        // 2. Search Input Bar
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(Color(0xFFF8FAFC))
            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(16.dp))
            .padding(horizontal = 12.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Icon(
            imageVector = Icons.Default.Search,
            contentDescription = null,
            tint = Color(0xFF94A3B8),
            modifier = Modifier.size(20.dp)
          )
          Spacer(Modifier.width(8.dp))
          BasicTextField(
            value = searchQuery,
            onValueChange = { searchQuery = it },
            singleLine = true,
            textStyle = TextStyle(
              fontSize = 14.sp,
              color = Color(0xFF0F172A),
              fontWeight = FontWeight.Medium
            ),
            cursorBrush = SolidColor(TeachColors.Primary),
            modifier = Modifier.weight(1f),
            decorationBox = { innerTextField ->
              if (searchQuery.isEmpty()) {
                Text(
                  text = "Search language or country...",
                  fontSize = 14.sp,
                  color = Color(0xFF94A3B8)
                )
              }
              innerTextField()
            }
          )
          if (searchQuery.isNotEmpty()) {
            Box(
              modifier = Modifier
                .size(20.dp)
                .clip(CircleShape)
                .background(Color(0xFFCBD5E1))
                .clickable { searchQuery = "" },
              contentAlignment = Alignment.Center
            ) {
              Icon(
                imageVector = Icons.Default.Close,
                contentDescription = "Clear",
                tint = Color.White,
                modifier = Modifier.size(12.dp)
              )
            }
          }
        }

        Spacer(Modifier.height(14.dp))

        // 3. Scrollable Language List / Grid
        Box(modifier = Modifier.weight(1f)) {
          if (filteredLanguages.isEmpty()) {
            Box(
              modifier = Modifier.fillMaxSize(),
              contentAlignment = Alignment.Center
            ) {
              Text(
                text = "No languages found",
                fontSize = 14.sp,
                color = Color(0xFF94A3B8)
              )
            }
          } else if (isLandscape) {
            LazyVerticalGrid(
              columns = GridCells.Fixed(2),
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              verticalArrangement = Arrangement.spacedBy(8.dp),
              contentPadding = PaddingValues(bottom = 6.dp)
            ) {
              items(filteredLanguages, key = { it.tag }) { lang ->
                LanguageCard(
                  lang = lang,
                  isSelected = (currentTag.isEmpty() && lang.tag.isEmpty()) || (currentTag == lang.tag),
                  onSelect = {
                    onSelectLanguage(lang.tag)
                    onDismiss()
                  }
                )
              }
            }
          } else {
            LazyColumn(
              verticalArrangement = Arrangement.spacedBy(8.dp),
              contentPadding = PaddingValues(bottom = 6.dp)
            ) {
              items(filteredLanguages, key = { it.tag }) { lang ->
                LanguageCard(
                  lang = lang,
                  isSelected = (currentTag.isEmpty() && lang.tag.isEmpty()) || (currentTag == lang.tag),
                  onSelect = {
                    onSelectLanguage(lang.tag)
                    onDismiss()
                  }
                )
              }
            }
          }
        }
      }
    }
  }
}

@Composable
private fun LanguageCard(
  lang: AppLanguage,
  isSelected: Boolean,
  onSelect: () -> Unit
) {
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(16.dp))
      .clickable(onClick = onSelect),
    shape = RoundedCornerShape(16.dp),
    color = if (isSelected) Color(0xFFEFF6FF) else Color(0xFFF8FAFC),
    border = BorderStroke(
      width = if (isSelected) 2.dp else 1.dp,
      color = if (isSelected) TeachColors.Primary else Color(0xFFE2E8F0)
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp, vertical = 10.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      // Clean Typography Language Badge (No Emojis)
      Box(
        modifier = Modifier
          .size(38.dp)
          .clip(RoundedCornerShape(12.dp))
          .background(if (isSelected) TeachColors.Primary.copy(alpha = 0.12f) else Color(0xFFF1F5F9))
          .border(1.dp, if (isSelected) TeachColors.Primary.copy(alpha = 0.35f) else Color(0xFFE2E8F0), RoundedCornerShape(12.dp)),
        contentAlignment = Alignment.Center
      ) {
        if (lang.tag.isEmpty()) {
          Icon(
            imageVector = Icons.Default.Language,
            contentDescription = null,
            tint = if (isSelected) TeachColors.Primary else Color(0xFF64748B),
            modifier = Modifier.size(20.dp)
          )
        } else {
          Text(
            text = lang.codeBadge,
            fontSize = 13.sp,
            fontWeight = FontWeight.Bold,
            color = if (isSelected) TeachColors.Primary else Color(0xFF475569)
          )
        }
      }

      Spacer(Modifier.width(12.dp))

      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = lang.nativeName,
          fontSize = 15.sp,
          fontWeight = if (isSelected) FontWeight.Bold else FontWeight.SemiBold,
          color = if (isSelected) TeachColors.Primary else Color(0xFF0F172A)
        )
        Text(
          text = lang.englishName,
          fontSize = 12.sp,
          color = if (isSelected) TeachColors.Primary.copy(alpha = 0.85f) else Color(0xFF64748B)
        )
      }

      // Checkmark Circle / Radio
      if (isSelected) {
        Box(
          modifier = Modifier
            .size(24.dp)
            .clip(CircleShape)
            .background(TeachColors.Primary),
          contentAlignment = Alignment.Center
        ) {
          Icon(
            imageVector = Icons.Default.Check,
            contentDescription = "Selected",
            tint = Color.White,
            modifier = Modifier.size(16.dp)
          )
        }
      } else {
        Box(
          modifier = Modifier
            .size(22.dp)
            .border(1.5.dp, Color(0xFFCBD5E1), CircleShape)
        )
      }
    }
  }
}
