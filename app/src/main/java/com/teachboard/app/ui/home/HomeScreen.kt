package com.teachboard.app.ui.home

import android.content.ContentUris
import android.content.Intent
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Size
import java.io.File
import androidx.activity.compose.BackHandler
import androidx.appcompat.app.AppCompatDelegate
import com.teachboard.app.ui.tour.PrefsTourStore
import com.teachboard.app.ui.tour.TeachBoardTour
import com.teachboard.app.ui.tour.TourOverlay
import com.teachboard.app.ui.tour.rememberTourState
import com.teachboard.app.ui.tour.tourAnchor
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.produceState
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.os.LocaleListCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.teachboard.app.R
import com.teachboard.app.TeachBoardApp
import com.teachboard.app.data.UserPrefs
import com.teachboard.app.data.local.toEntity
import com.teachboard.app.domain.export.Exporters
import com.teachboard.app.domain.model.BoardLayer
import com.teachboard.app.domain.model.BoardSession
import com.teachboard.app.domain.model.SortOption
import com.teachboard.app.ui.theme.AppConfig
import com.teachboard.app.ui.theme.TeachColors
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.UUID

private val QuickLanguages = listOf(
  "" to "English",
  "en" to "English",
  "es" to "Español",
  "fr" to "Français",
  "de" to "Deutsch",
  "pt" to "Português",
  "it" to "Italiano",
  "ru" to "Русский",
  "tr" to "Türkçe",
  "ar" to "العربية",
  "hi" to "हिन्दी",
  "bn" to "বাংলা",
  "ur" to "اردو",
  "id" to "Indonesia",
  "vi" to "Tiếng Việt",
  "zh-CN" to "简体中文",
  "ja" to "日本語",
  "ko" to "한국어",
  "sw" to "Kiswahili"
)

data class RecordingItem(
  val id: Long,
  val uri: Uri,
  val displayName: String,
  val title: String,
  val sizeBytes: Long,
  val durationMs: Long,
  val dateModifiedSec: Long
)

class HomeViewModel(private val app: TeachBoardApp) : ViewModel() {

  private val db = app.database
  private val prefs = app.prefs

  val searchQuery = MutableStateFlow("")
  val sortOption = MutableStateFlow(SortOption.NEWEST)
  val message = MutableStateFlow<String?>(null)

  private val _recordingsTrigger = MutableStateFlow(0L)
  val recordings: StateFlow<List<RecordingItem>> = combine(
    _recordingsTrigger,
    searchQuery,
    sortOption
  ) { _, q, sort ->
    val list = withContext(Dispatchers.IO) { loadRecordingsFromStorage() }
    val filtered = if (q.isBlank()) list else list.filter {
      it.title.contains(q.trim(), ignoreCase = true) || it.displayName.contains(q.trim(), ignoreCase = true)
    }
    when (sort) {
      SortOption.NEWEST -> filtered.sortedByDescending { it.dateModifiedSec }
      SortOption.OLDEST -> filtered.sortedBy { it.dateModifiedSec }
      SortOption.NAME_AZ -> filtered.sortedBy { it.title.lowercase() }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun refreshRecordings() {
    _recordingsTrigger.value = System.currentTimeMillis()
  }

  fun deleteRecording(item: RecordingItem) {
    viewModelScope.launch(Dispatchers.IO) {
      runCatching {
        app.contentResolver.delete(item.uri, null, null)
      }
      runCatching {
        val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val file = File(File(moviesDir, AppConfig.ALBUM_NAME), item.displayName)
        if (file.exists()) file.delete()
      }
      refreshRecordings()
      message.value = "Recording deleted"
    }
  }

  private fun loadRecordingsFromStorage(): List<RecordingItem> {
    val items = mutableListOf<RecordingItem>()
    val seenNames = mutableSetOf<String>()

    val collection = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL)
    } else {
      MediaStore.Video.Media.EXTERNAL_CONTENT_URI
    }

    val projection = arrayOf(
      MediaStore.Video.Media._ID,
      MediaStore.Video.Media.DISPLAY_NAME,
      MediaStore.Video.Media.SIZE,
      MediaStore.Video.Media.DURATION,
      MediaStore.Video.Media.DATE_MODIFIED
    )

    val selection = "${MediaStore.Video.Media.DISPLAY_NAME} LIKE ?"
    val selectionArgs = arrayOf("TeachBoard_%.mp4")
    val sortOrder = "${MediaStore.Video.Media.DATE_MODIFIED} DESC"

    runCatching {
      app.contentResolver.query(collection, projection, selection, selectionArgs, sortOrder)?.use { cursor ->
        val idCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media._ID)
        val nameCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME)
        val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)
        val durCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)
        val dateCol = cursor.getColumnIndexOrThrow(MediaStore.Video.Media.DATE_MODIFIED)

        while (cursor.moveToNext()) {
          val id = cursor.getLong(idCol)
          val name = cursor.getString(nameCol) ?: "Recording"
          val size = cursor.getLong(sizeCol)
          val dur = cursor.getLong(durCol)
          val date = cursor.getLong(dateCol)
          val uri = ContentUris.withAppendedId(MediaStore.Video.Media.EXTERNAL_CONTENT_URI, id)

          seenNames.add(name)
          items.add(
            RecordingItem(
              id = id,
              uri = uri,
              displayName = name,
              title = formatRecordingTitle(name, date * 1000L),
              sizeBytes = size,
              durationMs = dur,
              dateModifiedSec = date
            )
          )
        }
      }
    }

    runCatching {
      val moviesDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
      val teachBoardDir = File(moviesDir, AppConfig.ALBUM_NAME)
      if (teachBoardDir.exists() && teachBoardDir.isDirectory) {
        val files = teachBoardDir.listFiles { f -> f.extension.equals("mp4", ignoreCase = true) } ?: emptyArray()
        for (f in files) {
          if (!seenNames.contains(f.name)) {
            seenNames.add(f.name)
            val uri = Uri.fromFile(f)
            items.add(
              RecordingItem(
                id = f.name.hashCode().toLong(),
                uri = uri,
                displayName = f.name,
                title = formatRecordingTitle(f.name, f.lastModified()),
                sizeBytes = f.length(),
                durationMs = 0L,
                dateModifiedSec = f.lastModified() / 1000L
              )
            )
          }
        }
      }
    }

    return items
  }

  private fun formatRecordingTitle(fileName: String, timestampMs: Long): String {
    val datePattern = Regex("""TeachBoard_([a-zA-Z0-9]+_)?(\d{4})(\d{2})(\d{2})_(\d{2})(\d{2})(\d{2})""", RegexOption.IGNORE_CASE)
    val match = datePattern.find(fileName)
    if (match != null) {
      val month = match.groupValues[3]
      val day = match.groupValues[4]
      val hour = match.groupValues[5]
      val min = match.groupValues[6]
      return "TeachBoard $day/$month $hour:$min"
    }

    val rawMillisPattern = Regex("""TeachBoard_(\d{12,14})""", RegexOption.IGNORE_CASE)
    val millisMatch = rawMillisPattern.find(fileName)
    if (millisMatch != null) {
      val millis = millisMatch.groupValues[1].toLongOrNull()
      if (millis != null && millis > 0L) {
        val stamp = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(millis))
        return "TeachBoard $stamp"
      }
    }

    if (timestampMs > 0L) {
      val stamp = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date(timestampMs))
      return "TeachBoard $stamp"
    }

    return fileName.removeSuffix(".mp4").replace('_', ' ')
  }

  val sessions: StateFlow<List<BoardSession>> = combine(
    db.sessionDao().observeAll().map { list -> list.map { it.toDomain() } },
    searchQuery,
    sortOption
  ) { list, q, sort ->
    val filtered = if (q.isBlank()) list else list.filter { it.title.contains(q.trim(), ignoreCase = true) }
    when (sort) {
      SortOption.NEWEST -> filtered.sortedByDescending { it.updatedAt }
      SortOption.OLDEST -> filtered.sortedBy { it.updatedAt }
      SortOption.NAME_AZ -> filtered.sortedBy { it.title.lowercase() }
    }
  }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

  fun createNewBoard(onCreated: (String) -> Unit) {
    viewModelScope.launch {
      val p = prefs.prefs.first()
      val stamp = SimpleDateFormat("dd/MM HH:mm", Locale.getDefault()).format(Date())
      val session = BoardSession(
        id = UUID.randomUUID().toString(),
        title = "${AppConfig.APP_NAME} $stamp",
        backgroundColor = p.defaultBackgroundColor,
        backgroundStyle = p.defaultBackgroundStyle
      )
      val layer = BoardLayer(sessionId = session.id, name = "Layer 1", zOrder = 0)
      withContext(Dispatchers.IO) {
        db.sessionDao().upsert(session.toEntity())
        db.layerDao().upsert(layer.toEntity())
      }
      onCreated(session.id)
    }
  }

  fun renameBoard(session: BoardSession, newTitle: String) {
    val clean = newTitle.trim().ifBlank { return }
    viewModelScope.launch(Dispatchers.IO) {
      db.sessionDao().rename(session.id, clean, System.currentTimeMillis())
    }
  }

  fun duplicateBoard(session: BoardSession) {
    viewModelScope.launch(Dispatchers.IO) {
      val newId = UUID.randomUUID().toString()
      val now = System.currentTimeMillis()
      val copy = session.copy(id = newId, title = "${session.title} (Copy)", createdAt = now, updatedAt = now)
      db.sessionDao().upsert(copy.toEntity())

      val oldLayers = db.layerDao().getForSession(session.id)
      val layerMap = mutableMapOf<String, String>()
      val newLayers = oldLayers.map { l ->
        val nlId = UUID.randomUUID().toString()
        layerMap[l.id] = nlId
        l.copy(id = nlId, sessionId = newId)
      }
      if (newLayers.isNotEmpty()) db.layerDao().upsertAll(newLayers)

      val oldObjs = db.canvasObjectDao().getForSession(session.id)
      val newObjs = oldObjs.map { o ->
        o.copy(
          id = 0L,
          objectUid = UUID.randomUUID().toString(),
          sessionId = newId,
          layerId = layerMap[o.layerId] ?: newLayers.firstOrNull()?.id.orEmpty()
        )
      }
      if (newObjs.isNotEmpty()) db.canvasObjectDao().upsertAll(newObjs)

      val oldThumb = Exporters.thumbnailFile(app, session.id)
      if (oldThumb.exists()) oldThumb.copyTo(Exporters.thumbnailFile(app, newId), overwrite = true)
    }
  }

  fun deleteBoard(session: BoardSession) {
    viewModelScope.launch(Dispatchers.IO) {
      db.canvasObjectDao().deleteForSession(session.id)
      db.layerDao().deleteForSession(session.id)
      db.sessionDao().deleteById(session.id)
      Exporters.thumbnailFile(app, session.id).delete()
    }
  }

  fun exportBoardPdf(session: BoardSession) {
    viewModelScope.launch {
      message.value = "Exporting ${session.title} to PDF…"
      val file = Exporters.exportPdf(app, db, session, (0 until session.pageCount).toList(), saveToDownloads = true)
      message.value = if (file != null) "Saved PDF to Downloads/${AppConfig.ALBUM_NAME}" else "PDF export failed"
    }
  }

  fun clearMessage() { message.value = null }

  companion object {
    fun factory(app: TeachBoardApp): ViewModelProvider.Factory = object : ViewModelProvider.Factory {
      @Suppress("UNCHECKED_CAST")
      override fun <T : ViewModel> create(modelClass: Class<T>): T = HomeViewModel(app) as T
    }
  }
}

@Composable
fun SplashScreen(onFinished: () -> Unit) {
  LaunchedEffect(Unit) {
    delay(450L)
    onFinished()
  }
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(Color(0xFF141414)),
    contentAlignment = Alignment.Center
  ) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
      Surface(shape = RoundedCornerShape(24.dp), color = Color(0xFF2B7FFF), modifier = Modifier.size(84.dp)) {
        Box(contentAlignment = Alignment.Center) {
          Icon(Icons.Default.Draw, contentDescription = null, tint = Color.White, modifier = Modifier.size(46.dp))
        }
      }
      Spacer(Modifier.height(18.dp))
      Text(
        text = stringResource(R.string.app_name),
        fontSize = 34.sp,
        color = Color.White,
        fontWeight = FontWeight.ExtraBold
      )
    }
  }
}

/**
 * Clean Landing Home Hub matching the audited reference home screen (zero ads, zero login, zero paywalls).
 */
@Composable
fun LandingHomeScreen(
  app: TeachBoardApp,
  viewModel: HomeViewModel,
  onNewBoardCreated: (String) -> Unit,
  onOpenHistory: () -> Unit,
  onOpenSettings: () -> Unit
) {
  val context = LocalContext.current
  val scope = rememberCoroutineScope()
  val prefs by app.prefs.prefs.collectAsState(initial = UserPrefs())
  val sessions by viewModel.sessions.collectAsState()
  val recordings by viewModel.recordings.collectAsState()
  var showLangDialog by remember { mutableStateOf(false) }

  LaunchedEffect(Unit) {
    viewModel.refreshRecordings()
  }

  val currentLangLabel = QuickLanguages.firstOrNull { it.first == prefs.languageTag }?.second ?: "English"
  val config = LocalConfiguration.current
  val isLandscape = config.screenWidthDp > config.screenHeightDp
  val isDarkBg = MaterialTheme.colorScheme.background == TeachColors.BackgroundDark

  val tourStore = remember { PrefsTourStore(context, TeachBoardTour.PREFS_HOME, TeachBoardTour.TOUR_VERSION) }
  val tourState = rememberTourState(TeachBoardTour.homeSteps(), tourStore)

  LaunchedEffect(Unit) {
    if (tourStore.shouldShow()) {
      delay(350L)
      tourState.start()
    }
  }

  BackHandler(tourState.visible) {
    if (tourState.isLast) tourState.finish() else tourState.next()
  }

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
    Box(modifier = Modifier.fillMaxSize()) {
      Box(
        modifier = Modifier
          .fillMaxSize()
          .statusBarsPadding()
          .navigationBarsPadding()
          .padding(horizontal = 20.dp, vertical = 14.dp)
      ) {
      // Top-left Language pill
      Surface(
        modifier = Modifier
          .align(Alignment.TopStart)
          .clip(RoundedCornerShape(24.dp))
          .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
          .clickable { showLangDialog = true },
        color = Color.Transparent
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 14.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(currentLangLabel, fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
        }
      }

      // Top-right Settings gear
      IconButton(
        onClick = onOpenSettings,
        modifier = Modifier
          .align(Alignment.TopEnd)
          .tourAnchor("home_settings")
      ) {
        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title), modifier = Modifier.size(26.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
      }

      val rateAndShareRow: @Composable () -> Unit = {
        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(24.dp))
              .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
              .clickable {
                runCatching {
                  context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.PLAY_STORE_URL)))
                }
              },
            color = Color.Transparent
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFFACC15), modifier = Modifier.size(18.dp))
              Text(stringResource(R.string.rate_cta), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
          }
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(24.dp))
              .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(24.dp))
              .clickable {
                val share = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_TEXT, "${AppConfig.APP_NAME} — 100% Free Offline Teaching Whiteboard")
                }
                context.startActivity(Intent.createChooser(share, "Share"))
              },
            color = Color.Transparent
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 22.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(18.dp))
              Text(stringResource(R.string.tool_share), fontSize = 14.sp, color = MaterialTheme.colorScheme.onSurface)
            }
          }
        }
      }

      val ctaButtonsColumn: @Composable (Modifier) -> Unit = { mod ->
        Column(
          modifier = mod,
          verticalArrangement = Arrangement.spacedBy(18.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          // Primary Blue "+ New Board" button
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .height(if (isLandscape) 68.dp else 78.dp)
              .clip(RoundedCornerShape(22.dp))
              .tourAnchor("home_new")
              .clickable { viewModel.createNewBoard(onNewBoardCreated) },
            color = Color(0xFF2B7FFF),
            shadowElevation = 4.dp
          ) {
            Row(
              modifier = Modifier.fillMaxSize(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(Icons.Default.Add, contentDescription = null, tint = Color.White, modifier = Modifier.size(26.dp))
              Spacer(Modifier.width(10.dp))
              Text(
                text = stringResource(R.string.new_board_cta),
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
            }
          }

          // Secondary Charcoal / Neutral "History" button
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .height(if (isLandscape) 68.dp else 78.dp)
              .clip(RoundedCornerShape(22.dp))
              .clickable(onClick = onOpenHistory),
            color = if (isDarkBg) Color(0xFF424242) else Color(0xFFE5E7EB),
            shadowElevation = 2.dp
          ) {
            Row(
              modifier = Modifier.fillMaxSize(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = if (isDarkBg) Color.White else TeachColors.OnSurface,
                modifier = Modifier.size(24.dp)
              )
              Spacer(Modifier.width(10.dp))
              val boardCount = sessions.size
              Text(
                text = if (boardCount == 0) stringResource(R.string.history_cta) else "${stringResource(R.string.history_cta)} ($boardCount)",
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkBg) Color.White else TeachColors.OnSurface
              )
            }
          }
        }
      }

      if (isLandscape) {
        // 2-column Landscape Home Hub (cleanly balanced side-by-side!)
        Row(
          modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth()
            .padding(horizontal = 36.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceEvenly
        ) {
          Column(
            modifier = Modifier.weight(1f),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(20.dp)
          ) {
            Text(
              text = stringResource(R.string.app_name),
              fontSize = 42.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onBackground,
              letterSpacing = (-0.5).sp
            )
            rateAndShareRow()
          }
          Spacer(Modifier.width(28.dp))
          ctaButtonsColumn(Modifier.weight(1f).widthIn(max = 320.dp))
        }
      } else {
        // Portrait vertical hero CTA stack
        Column(
          modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = stringResource(R.string.app_name),
            fontSize = 44.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
          )
          Spacer(Modifier.height(44.dp))
          ctaButtonsColumn(Modifier.fillMaxWidth().widthIn(max = 360.dp))
          Spacer(Modifier.height(34.dp))
          rateAndShareRow()
        }
      }

      }

      TourOverlay(state = tourState)
    }
  }

  if (showLangDialog) {
    AlertDialog(
      onDismissRequest = { showLangDialog = false },
      title = { Text(stringResource(R.string.language_title)) },
      text = {
        Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
          QuickLanguages.distinctBy { it.second }.forEach { (tag, name) ->
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
              Text(
                name,
                fontWeight = if (prefs.languageTag == tag) FontWeight.Bold else FontWeight.Normal,
                color = if (prefs.languageTag == tag) TeachColors.Primary else MaterialTheme.colorScheme.onSurface
              )
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

/**
 * Full-width single-column "My Whiteboards" History Gallery.
 * Each board card displays an expandable recording section at the bottom so the user knows
 * exactly how many recordings belong to that specific board.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryGalleryScreen(
  viewModel: HomeViewModel,
  onBack: () -> Unit,
  onOpenBoard: (String) -> Unit
) {
  val context = LocalContext.current
  val sessions by viewModel.sessions.collectAsState()
  val recordings by viewModel.recordings.collectAsState()
  val query by viewModel.searchQuery.collectAsState()
  val sort by viewModel.sortOption.collectAsState()
  val msg by viewModel.message.collectAsState()
  val snackbar = remember { SnackbarHostState() }

  var sortMenuOpen by remember { mutableStateOf(false) }
  var renameTarget by remember { mutableStateOf<BoardSession?>(null) }
  var deleteTarget by remember { mutableStateOf<BoardSession?>(null) }
  var deleteRecTarget by remember { mutableStateOf<RecordingItem?>(null) }

  LaunchedEffect(Unit) {
    viewModel.refreshRecordings()
  }

  LaunchedEffect(msg) {
    msg?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
  }

  Scaffold(
    topBar = {
      TopAppBar(
        navigationIcon = {
          IconButton(onClick = onBack) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
          }
        },
        title = {
          Column {
            Text(stringResource(R.string.my_whiteboards), fontWeight = FontWeight.Bold, fontSize = 20.sp)
            Text(
              stringResource(R.string.gallery_boards_count, sessions.size),
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.onSurfaceVariant
            )
          }
        },
        actions = {
          Box {
            IconButton(onClick = { sortMenuOpen = true }) {
              Icon(Icons.AutoMirrored.Filled.Sort, contentDescription = "Sort")
            }
            DropdownMenu(expanded = sortMenuOpen, onDismissRequest = { sortMenuOpen = false }) {
              DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_newest), fontWeight = if (sort == SortOption.NEWEST) FontWeight.Bold else FontWeight.Normal) },
                onClick = { viewModel.sortOption.value = SortOption.NEWEST; sortMenuOpen = false }
              )
              DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_oldest), fontWeight = if (sort == SortOption.OLDEST) FontWeight.Bold else FontWeight.Normal) },
                onClick = { viewModel.sortOption.value = SortOption.OLDEST; sortMenuOpen = false }
              )
              DropdownMenuItem(
                text = { Text(stringResource(R.string.sort_name), fontWeight = if (sort == SortOption.NAME_AZ) FontWeight.Bold else FontWeight.Normal) },
                onClick = { viewModel.sortOption.value = SortOption.NAME_AZ; sortMenuOpen = false }
              )
            }
          }
        }
      )
    },
    floatingActionButton = {
      FloatingActionButton(
        onClick = { viewModel.createNewBoard(onOpenBoard) },
        containerColor = Color(0xFF2B7FFF),
        contentColor = Color.White,
        shape = RoundedCornerShape(16.dp)
      ) {
        Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_whiteboard), modifier = Modifier.size(28.dp))
      }
    },
    snackbarHost = { SnackbarHost(snackbar) }
  ) { padding ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .padding(padding)
        .padding(horizontal = 16.dp)
    ) {
      OutlinedTextField(
        value = query,
        onValueChange = { viewModel.searchQuery.value = it },
        modifier = Modifier.fillMaxWidth(),
        singleLine = true,
        shape = RoundedCornerShape(16.dp),
        placeholder = { Text(stringResource(R.string.search_boards_hint)) },
        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
        trailingIcon = {
          if (query.isNotEmpty()) {
            IconButton(onClick = { viewModel.searchQuery.value = "" }) {
              Icon(Icons.Default.Close, contentDescription = "Clear")
            }
          }
        }
      )

      Spacer(Modifier.height(14.dp))

      if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(60.dp), tint = TeachColors.Primary.copy(alpha = 0.6f))
            Text(stringResource(R.string.empty_boards_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.empty_boards_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      } else {
        val playRecording = { rec: RecordingItem ->
          try {
            val playIntent = Intent(Intent.ACTION_VIEW).apply {
              setDataAndType(rec.uri, "video/mp4")
              addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(playIntent)
          } catch (e: Exception) {
            viewModel.message.value = "No video player found. Open it from your gallery."
          }
        }

        val shareRecording = { rec: RecordingItem ->
          try {
            val shareIntent = Intent(Intent.ACTION_SEND).apply {
              type = "video/mp4"
              putExtra(Intent.EXTRA_STREAM, rec.uri)
              addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            context.startActivity(Intent.createChooser(shareIntent, "Share recording"))
          } catch (e: Exception) {
            viewModel.message.value = "Could not share video"
          }
        }

        val (sessionRecordingsMap, unassignedRecordings) = remember(sessions, recordings) {
          val map = mutableMapOf<String, MutableList<RecordingItem>>()
          sessions.forEach { map[it.id] = mutableListOf() }
          val unassigned = mutableListOf<RecordingItem>()

          val hexIdRegex = Regex("""TeachBoard_([a-fA-F0-9]{8})_""", RegexOption.IGNORE_CASE)

          for (rec in recordings) {
            val hexMatch = hexIdRegex.find(rec.displayName)
            val extractedPrefix = hexMatch?.groupValues?.get(1)?.lowercase()

            if (extractedPrefix != null) {
              // 1. Strict 1-to-1 match by embedded session ID prefix
              val targetSession = sessions.firstOrNull {
                it.id.replace("-", "").lowercase().startsWith(extractedPrefix)
              }
              if (targetSession != null) {
                map[targetSession.id]?.add(rec)
              } else {
                // Board was deleted or not found
                unassigned.add(rec)
              }
              continue
            }

            // 2. Legacy recordings: assign to AT MOST ONE closest session within 15 minutes
            val recTimeMs = rec.dateModifiedSec * 1000L
            val closestSession = sessions
              .map { s -> s to kotlin.math.abs(s.updatedAt - recTimeMs) }
              .filter { it.second <= 15 * 60 * 1000L }
              .minByOrNull { it.second }
              ?.first

            if (closestSession != null) {
              map[closestSession.id]?.add(rec)
            } else {
              unassigned.add(rec)
            }
          }
          map to unassigned
        }

        LazyColumn(
          contentPadding = PaddingValues(bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          items(sessions, key = { it.id }) { session ->
            val boardRecordings = sessionRecordingsMap[session.id] ?: emptyList()
            FullWidthBoardCard(
              session = session,
              recordings = boardRecordings,
              onClick = { onOpenBoard(session.id) },
              onRename = { renameTarget = session },
              onDuplicate = { viewModel.duplicateBoard(session) },
              onExportPdf = { viewModel.exportBoardPdf(session) },
              onDelete = { deleteTarget = session },
              onPlayRecording = playRecording,
              onShareRecording = shareRecording,
              onDeleteRecording = { deleteRecTarget = it }
            )
          }

          if (unassignedRecordings.isNotEmpty()) {
            item {
              UnassignedRecordingsCard(
                recordings = unassignedRecordings,
                onPlayRecording = playRecording,
                onShareRecording = shareRecording,
                onDeleteRecording = { deleteRecTarget = it }
              )
            }
          }
        }
      }
    }
  }

  renameTarget?.let { target ->
    var titleText by remember(target.id) { mutableStateOf(target.title) }
    AlertDialog(
      onDismissRequest = { renameTarget = null },
      title = { Text(stringResource(R.string.rename_board)) },
      text = {
        OutlinedTextField(
          value = titleText,
          onValueChange = { titleText = it },
          singleLine = true,
          modifier = Modifier.fillMaxWidth()
        )
      },
      confirmButton = {
        TextButton(onClick = { viewModel.renameBoard(target, titleText); renameTarget = null }) {
          Text(stringResource(R.string.save))
        }
      },
      dismissButton = {
        TextButton(onClick = { renameTarget = null }) { Text(stringResource(R.string.cancel)) }
      }
    )
  }

  deleteTarget?.let { target ->
    AlertDialog(
      onDismissRequest = { deleteTarget = null },
      title = { Text(stringResource(R.string.delete_board_title)) },
      text = { Text(stringResource(R.string.delete_board_confirm, target.title)) },
      confirmButton = {
        TextButton(onClick = { viewModel.deleteBoard(target); deleteTarget = null }) {
          Text(stringResource(R.string.delete), color = TeachColors.Danger)
        }
      },
      dismissButton = {
        TextButton(onClick = { deleteTarget = null }) { Text(stringResource(R.string.cancel)) }
      }
    )
  }

  deleteRecTarget?.let { target ->
    AlertDialog(
      onDismissRequest = { deleteRecTarget = null },
      title = { Text("Delete recording?") },
      text = { Text("This will permanently remove \"${target.title}\" from your gallery. This cannot be undone.") },
      confirmButton = {
        TextButton(onClick = { viewModel.deleteRecording(target); deleteRecTarget = null }) {
          Text(stringResource(R.string.delete), color = TeachColors.Danger)
        }
      },
      dismissButton = {
        TextButton(onClick = { deleteRecTarget = null }) { Text(stringResource(R.string.cancel)) }
      }
    )
  }
}

@Composable
private fun FullWidthBoardCard(
  session: BoardSession,
  recordings: List<RecordingItem>,
  onClick: () -> Unit,
  onRename: () -> Unit,
  onDuplicate: () -> Unit,
  onExportPdf: () -> Unit,
  onDelete: () -> Unit,
  onPlayRecording: (RecordingItem) -> Unit,
  onShareRecording: (RecordingItem) -> Unit,
  onDeleteRecording: (RecordingItem) -> Unit
) {
  val context = LocalContext.current
  var menuOpen by remember { mutableStateOf(false) }
  var expandedRecordings by remember { mutableStateOf(false) }

  val thumbBitmap = remember(session.id, session.updatedAt) {
    val file = Exporters.thumbnailFile(context, session.id)
    if (file.exists()) runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull() else null
  }
  val dateStr = remember(session.updatedAt) {
    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(session.updatedAt))
  }

  val totalDurationAndSize = remember(recordings) {
    if (recordings.isEmpty()) ""
    else {
      val totalBytes = recordings.sumOf { it.sizeBytes }
      val totalDurMs = recordings.sumOf { it.durationMs }
      val durSec = totalDurMs / 1000
      val durStr = if (durSec > 0) String.format(Locale.US, "%02d:%02d", durSec / 60, durSec % 60) else ""
      val sizeStr = when {
        totalBytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", totalBytes / 1024.0)
        totalBytes < 1024L * 1024L * 1024L -> String.format(Locale.US, "%.1f MB", totalBytes / (1024.0 * 1024.0))
        else -> String.format(Locale.US, "%.2f GB", totalBytes / (1024.0 * 1024.0 * 1024.0))
      }
      if (durStr.isNotBlank()) "$durStr · $sizeStr" else sizeStr
    }
  }

  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(20.dp))
      .clickable(onClick = onClick),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
  ) {
    Column(modifier = Modifier.padding(12.dp)) {
      // Whiteboard Preview Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1.55f)
          .clip(RoundedCornerShape(14.dp))
          .background(Color(session.backgroundColor))
      ) {
        if (thumbBitmap != null) {
          Image(
            bitmap = thumbBitmap.asImageBitmap(),
            contentDescription = session.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }

        // Top-right: Pages & PDF badge
        if (session.pageCount > 1 || !session.pdfPath.isNullOrBlank()) {
          Surface(
            shape = RoundedCornerShape(10.dp),
            color = TeachColors.DarkPillBg,
            modifier = Modifier.align(Alignment.TopEnd).padding(8.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              if (!session.pdfPath.isNullOrBlank()) {
                Icon(Icons.Default.PictureAsPdf, contentDescription = null, tint = Color(0xFFF87171), modifier = Modifier.size(13.dp))
              }
              Text("${session.pageCount} pages", color = Color.White, fontSize = 11.sp, fontWeight = FontWeight.Medium)
            }
          }
        }
      }

      Spacer(Modifier.height(10.dp))

      // Title & Date Row
      Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(session.title, fontWeight = FontWeight.Bold, fontSize = 16.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
          Spacer(Modifier.height(2.dp))
          Text(
            stringResource(R.string.board_modified_label, dateStr),
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
          )
        }
        Box {
          IconButton(onClick = { menuOpen = true }) {
            Icon(Icons.Default.MoreVert, contentDescription = "More")
          }
          DropdownMenu(expanded = menuOpen, onDismissRequest = { menuOpen = false }) {
            DropdownMenuItem(text = { Text(stringResource(R.string.rename)) }, onClick = { menuOpen = false; onRename() })
            DropdownMenuItem(text = { Text(stringResource(R.string.duplicate)) }, onClick = { menuOpen = false; onDuplicate() })
            DropdownMenuItem(text = { Text(stringResource(R.string.export_pdf_title)) }, onClick = { menuOpen = false; onExportPdf() })
            DropdownMenuItem(
              text = { Text(stringResource(R.string.delete), color = TeachColors.Danger) },
              onClick = { menuOpen = false; onDelete() }
            )
          }
        }
      }

      // Bottom Section: Option to see recordings for this specific board
      if (recordings.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))
        Box(modifier = Modifier.fillMaxWidth().height(1.dp).background(MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f)))
        Spacer(Modifier.height(8.dp))

        Surface(
          shape = RoundedCornerShape(12.dp),
          color = Color(0xFFFFF1F2),
          modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, Color(0xFFFECDD3), RoundedCornerShape(12.dp))
            .clickable { expandedRecordings = !expandedRecordings }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 9.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Box(
                modifier = Modifier
                  .size(28.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFE11D48)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Videocam,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(16.dp)
                )
              }
              Column {
                Text(
                  "${recordings.size} Screen Recording${if (recordings.size > 1) "s" else ""}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color(0xFF9F1239)
                )
                if (totalDurationAndSize.isNotBlank()) {
                  Text(
                    totalDurationAndSize,
                    fontSize = 11.sp,
                    color = Color(0xFFBE123C)
                  )
                }
              }
            }
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                if (expandedRecordings) "Hide" else "See recordings",
                fontWeight = FontWeight.Bold,
                fontSize = 12.sp,
                color = Color(0xFFE11D48)
              )
              Icon(
                if (expandedRecordings) Icons.Default.KeyboardArrowUp else Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = null,
                tint = Color(0xFFE11D48),
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }

        if (expandedRecordings) {
          Spacer(Modifier.height(8.dp))
          Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            recordings.forEach { rec ->
              RecordingMiniItem(
                recording = rec,
                onPlay = { onPlayRecording(rec) },
                onShare = { onShareRecording(rec) },
                onDelete = { onDeleteRecording(rec) }
              )
            }
          }
        }
      }
    }
  }
}

@Composable
private fun RecordingMiniItem(
  recording: RecordingItem,
  onPlay: () -> Unit,
  onShare: () -> Unit,
  onDelete: () -> Unit
) {
  val context = LocalContext.current
  val thumbBitmap by produceState<android.graphics.Bitmap?>(initialValue = null, recording.uri) {
    value = withContext(Dispatchers.IO) {
      runCatching {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
          context.contentResolver.loadThumbnail(recording.uri, Size(320, 200), null)
        } else null
      }.getOrNull() ?: runCatching {
        val retriever = MediaMetadataRetriever()
        retriever.setDataSource(context, recording.uri)
        val bmp = retriever.getFrameAtTime(500_000)
        retriever.release()
        bmp
      }.getOrNull()
    }
  }

  val formattedSize = remember(recording.sizeBytes) {
    when {
      recording.sizeBytes <= 0L -> ""
      recording.sizeBytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", recording.sizeBytes / 1024.0)
      recording.sizeBytes < 1024L * 1024L * 1024L -> String.format(Locale.US, "%.1f MB", recording.sizeBytes / (1024.0 * 1024.0))
      else -> String.format(Locale.US, "%.2f GB", recording.sizeBytes / (1024.0 * 1024.0 * 1024.0))
    }
  }

  val durationStr = remember(recording.durationMs) {
    if (recording.durationMs > 0) {
      val sec = recording.durationMs / 1000
      String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60)
    } else ""
  }

  Surface(
    shape = RoundedCornerShape(12.dp),
    color = Color(0xFFF8FAFC),
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
      .clickable(onClick = onPlay)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      // Video thumbnail with small play badge
      Box(
        modifier = Modifier
          .size(width = 72.dp, height = 48.dp)
          .clip(RoundedCornerShape(8.dp))
          .background(Color(0xFF0F172A)),
        contentAlignment = Alignment.Center
      ) {
        if (thumbBitmap != null) {
          Image(
            bitmap = thumbBitmap!!.asImageBitmap(),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        }
        Surface(
          shape = CircleShape,
          color = Color(0xCC000000),
          modifier = Modifier.size(24.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(Icons.Default.PlayArrow, contentDescription = "Play", tint = Color.White, modifier = Modifier.size(16.dp))
          }
        }
      }

      Column(modifier = Modifier.weight(1f)) {
        Text(
          recording.title,
          fontWeight = FontWeight.SemiBold,
          fontSize = 13.sp,
          color = Color(0xFF0F172A),
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
          listOfNotNull(durationStr.ifBlank { null }, formattedSize.ifBlank { null }).joinToString(" · "),
          fontSize = 11.sp,
          color = Color(0xFF64748B)
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Share, contentDescription = "Share", tint = Color(0xFF475569), modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color(0xFFEF4444), modifier = Modifier.size(16.dp))
        }
      }
    }
  }
}

@Composable
private fun UnassignedRecordingsCard(
  recordings: List<RecordingItem>,
  onPlayRecording: (RecordingItem) -> Unit,
  onShareRecording: (RecordingItem) -> Unit,
  onDeleteRecording: (RecordingItem) -> Unit
) {
  var expanded by remember { mutableStateOf(false) }
  Surface(
    modifier = Modifier
      .fillMaxWidth()
      .clip(RoundedCornerShape(20.dp))
      .border(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.7f), RoundedCornerShape(20.dp)),
    color = MaterialTheme.colorScheme.surface,
    tonalElevation = 2.dp
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .clickable { expanded = !expanded },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Box(
            modifier = Modifier
              .size(32.dp)
              .clip(CircleShape)
              .background(Color(0xFFE11D48)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
          }
          Column {
            Text(
              "Other Screen Recordings (${recordings.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = Color(0xFF0F172A)
            )
            Text(
              "Recordings from previous sessions",
              fontSize = 11.sp,
              color = Color(0xFF64748B)
            )
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            if (expanded) "Hide" else "See recordings",
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            color = Color(0xFFE11D48)
          )
          Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFE11D48),
            modifier = Modifier.size(18.dp)
          )
        }
      }

      if (expanded) {
        Spacer(Modifier.height(10.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
          recordings.forEach { rec ->
            RecordingMiniItem(
              recording = rec,
              onPlay = { onPlayRecording(rec) },
              onShare = { onShareRecording(rec) },
              onDelete = { onDeleteRecording(rec) }
            )
          }
        }
      }
    }
  }
}
