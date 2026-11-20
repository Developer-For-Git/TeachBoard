package com.teachboard.app.ui.help

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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.MenuBook
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.teachboard.app.R
import com.teachboard.app.ui.theme.TeachColors
import com.teachboard.app.ui.tour.ToolGuideSheet

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
  var showGuideSheet by remember { mutableStateOf(false) }

  val topics = listOf(
    stringResource(R.string.help_zoom_title) to stringResource(R.string.help_zoom_desc),
    stringResource(R.string.help_brushes_title) to stringResource(R.string.help_brushes_desc),
    stringResource(R.string.help_smart_shapes_title) to stringResource(R.string.help_smart_shapes_desc),
    stringResource(R.string.help_smart_writing_title) to stringResource(R.string.help_smart_writing_desc),
    stringResource(R.string.help_ocr_title) to stringResource(R.string.help_ocr_desc),
    stringResource(R.string.help_pdf_title) to stringResource(R.string.help_pdf_desc),
    stringResource(R.string.help_recording_title) to stringResource(R.string.help_recording_desc),
    stringResource(R.string.help_backup_title) to stringResource(R.string.help_backup_desc)
  )

  Scaffold(
    topBar = {
      TopAppBar(
        title = { Text(stringResource(R.string.help_title), fontWeight = FontWeight.Bold) },
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
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
        .padding(16.dp),
      verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
      // Hero Card: Visual Tool Guide (33 tools categorized)
      Surface(
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF1E293B),
        shadowElevation = 4.dp,
        modifier = Modifier
          .fillMaxWidth()
          .clickable { showGuideSheet = true }
      ) {
        Row(
          modifier = Modifier.padding(16.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = CircleShape,
            color = Color(0xFF2563EB),
            modifier = Modifier.size(46.dp)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(Icons.Outlined.MenuBook, contentDescription = null, tint = Color.White, modifier = Modifier.size(24.dp))
            }
          }
          Spacer(Modifier.width(14.dp))
          Column(modifier = Modifier.weight(1f)) {
            Text(
              "Visual Tool Guide",
              color = Color.White,
              fontSize = 16.sp,
              fontWeight = FontWeight.Bold
            )
            Spacer(Modifier.height(2.dp))
            Text(
              "Browse icons and descriptions for all 33 tools across 4 categories",
              color = Color(0xFF94A3B8),
              fontSize = 12.sp,
              lineHeight = 16.sp
            )
          }
          Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color(0xFF94A3B8))
        }
      }

      topics.forEach { (title, desc) ->
        Surface(
          shape = RoundedCornerShape(14.dp),
          color = MaterialTheme.colorScheme.surfaceVariant,
          modifier = Modifier.fillMaxWidth()
        ) {
          Column(modifier = Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            Text(title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = TeachColors.Primary)
            Text(desc, style = MaterialTheme.typography.bodyMedium)
          }
        }
      }
    }
  }

  if (showGuideSheet) {
    ToolGuideSheet(onDismiss = { showGuideSheet = false })
  }
}
