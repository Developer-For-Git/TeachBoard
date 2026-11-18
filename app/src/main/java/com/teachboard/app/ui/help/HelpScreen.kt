package com.teachboard.app.ui.help

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.teachboard.app.R
import com.teachboard.app.ui.theme.TeachColors

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(onBack: () -> Unit) {
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
}
