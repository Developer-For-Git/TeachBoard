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
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
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
import androidx.compose.foundation.layout.WindowInsets
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
import androidx.compose.foundation.lazy.itemsIndexed
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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.ui.text.TextStyle
import androidx.compose.material.icons.filled.KeyboardArrowDown
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
      Surface(shape = RoundedCornerShape(26.dp), color = TeachColors.Primary, modifier = Modifier.size(88.dp)) {
        Box(contentAlignment = Alignment.Center) {
          Icon(Icons.Default.Draw, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
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

@Composable
fun Modifier.bouncyClickable(
  enabled: Boolean = true,
  onClick: () -> Unit
): Modifier {
  val interactionSource = remember { MutableInteractionSource() }
  val isPressed by interactionSource.collectIsPressedAsState()
  val scale by animateFloatAsState(
    targetValue = if (isPressed) 0.94f else 1.0f,
    animationSpec = spring(
      dampingRatio = Spring.DampingRatioMediumBouncy,
      stiffness = Spring.StiffnessMedium
    ),
    label = "bouncyScale"
  )
  return this
    .graphicsLayer {
      scaleX = scale
      scaleY = scale
    }
    .clickable(
      interactionSource = interactionSource,
      indication = null,
      enabled = enabled,
      onClick = onClick
    )
}

/**
 * Playful organic sine-wave line accent matching the Pastel Playful Expressive design system.
 */
@Composable
fun WavyAccentDivider(
  modifier: Modifier = Modifier,
  color: Color = TeachColors.Primary,
  waveCount: Int = 5,
  amplitudeDp: Float = 3f,
  strokeWidthDp: Float = 3.5f
) {
  Canvas(modifier = modifier) {
    val wavePath = Path()
    val totalWidth = size.width
    val waveLength = totalWidth / waveCount
    val amplitude = amplitudeDp * density
    val midY = size.height / 2f

    wavePath.moveTo(0f, midY)
    for (i in 0 until waveCount) {
      val startX = i * waveLength
      wavePath.cubicTo(
        startX + waveLength * 0.25f, midY - amplitude,
        startX + waveLength * 0.75f, midY + amplitude,
        startX + waveLength, midY
      )
    }

    drawPath(
      path = wavePath,
      color = color,
      style = Stroke(width = strokeWidthDp * density, cap = StrokeCap.Round)
    )
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
      // Top-left Language pill (Pastel pill capsule with bouncy animation)
      Surface(
        modifier = Modifier
          .align(Alignment.TopStart)
          .clip(RoundedCornerShape(9999.dp))
          .bouncyClickable { showLangDialog = true },
        color = MaterialTheme.colorScheme.surfaceVariant
      ) {
        Row(
          modifier = Modifier.padding(horizontal = 16.dp, vertical = 9.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
          Icon(Icons.Default.Language, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
          Text(currentLangLabel, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, color = MaterialTheme.colorScheme.onSurface)
        }
      }

      // Top-right Settings gear (Tactile circular pill)
      Surface(
        modifier = Modifier
          .align(Alignment.TopEnd)
          .size(44.dp)
          .clip(CircleShape)
          .tourAnchor("home_settings")
          .bouncyClickable(onClick = onOpenSettings),
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = CircleShape
      ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings_title), modifier = Modifier.size(22.dp), tint = MaterialTheme.colorScheme.onSurfaceVariant)
        }
      }

      val rateAndShareRow: @Composable () -> Unit = {
        Row(
          horizontalArrangement = Arrangement.spacedBy(14.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Playful Blush Pill for Rate CTA
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(9999.dp))
              .bouncyClickable {
                runCatching {
                  context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(AppConfig.PLAY_STORE_URL)))
                }
              },
            color = if (isDarkBg) Color(0xFF3D2028) else TeachColors.PastelBlush
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(Icons.Default.Star, contentDescription = null, tint = Color(0xFFF59E0B), modifier = Modifier.size(18.dp))
              Text(
                stringResource(R.string.rate_cta),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkBg) Color(0xFFFFD1DC) else TeachColors.PastelBlushGlyph
              )
            }
          }

          // Playful Periwinkle Pill for Share CTA
          Surface(
            modifier = Modifier
              .clip(RoundedCornerShape(9999.dp))
              .bouncyClickable {
                val share = Intent(Intent.ACTION_SEND).apply {
                  type = "text/plain"
                  putExtra(Intent.EXTRA_TEXT, "${AppConfig.APP_NAME} — 100% Free Offline Teaching Whiteboard")
                }
                context.startActivity(Intent.createChooser(share, "Share"))
              },
            color = if (isDarkBg) Color(0xFF1E2840) else TeachColors.PastelPeriwinkle
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 20.dp, vertical = 10.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
              Icon(
                Icons.Default.Share,
                contentDescription = null,
                tint = if (isDarkBg) Color(0xFFBFDBFE) else TeachColors.PastelPeriwinkleGlyph,
                modifier = Modifier.size(18.dp)
              )
              Text(
                stringResource(R.string.tool_share),
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                color = if (isDarkBg) Color(0xFFBFDBFE) else TeachColors.PastelPeriwinkleGlyph
              )
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
          // Primary Bold Royal Blue "+ New Board" pill capsule button (with tactile spring bounce)
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .height(if (isLandscape) 58.dp else 66.dp)
              .clip(RoundedCornerShape(9999.dp))
              .tourAnchor("home_new")
              .bouncyClickable { viewModel.createNewBoard(onNewBoardCreated) },
            color = TeachColors.Primary,
            shape = RoundedCornerShape(9999.dp),
            shadowElevation = 8.dp
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
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          // Secondary Expressive Soft Violet "History" pill capsule button
          Surface(
            modifier = Modifier
              .fillMaxWidth()
              .height(if (isLandscape) 58.dp else 66.dp)
              .clip(RoundedCornerShape(9999.dp))
              .bouncyClickable(onClick = onOpenHistory),
            color = if (isDarkBg) Color(0xFF2E243A) else TeachColors.PastelViolet,
            shape = RoundedCornerShape(9999.dp),
            shadowElevation = 3.dp
          ) {
            Row(
              modifier = Modifier.fillMaxSize().padding(horizontal = 24.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                Icons.Default.History,
                contentDescription = null,
                tint = if (isDarkBg) Color(0xFFDDD6FE) else TeachColors.PastelVioletGlyph,
                modifier = Modifier.size(24.dp)
              )
              Spacer(Modifier.width(10.dp))
              Text(
                text = stringResource(R.string.history_cta),
                fontSize = 19.sp,
                fontWeight = FontWeight.Bold,
                color = if (isDarkBg) Color(0xFFDDD6FE) else TeachColors.PastelVioletGlyph
              )
              val boardCount = sessions.size
              if (boardCount > 0) {
                Spacer(Modifier.width(10.dp))
                // Pastel Mint pill badge for counter
                Surface(
                  shape = RoundedCornerShape(9999.dp),
                  color = if (isDarkBg) Color(0xFF133830) else TeachColors.PastelMint
                ) {
                  Text(
                    text = "$boardCount",
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isDarkBg) Color(0xFF86EFAC) else TeachColors.PastelMintGlyph,
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
                  )
                }
              }
            }
          }
        }
      }

      if (isLandscape) {
        // 2-column Landscape Home Hub (cleanly balanced side-by-side with playful wavy divider)
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
          ) {
            Text(
              text = stringResource(R.string.app_name),
              fontSize = 44.sp,
              fontWeight = FontWeight.ExtraBold,
              color = MaterialTheme.colorScheme.onBackground,
              letterSpacing = (-0.5).sp
            )
            // Playful sine-wave divider
            WavyAccentDivider(
              modifier = Modifier.width(140.dp).height(10.dp),
              color = TeachColors.Primary
            )
            rateAndShareRow()
          }
          Spacer(Modifier.width(28.dp))
          ctaButtonsColumn(Modifier.weight(1f).widthIn(max = 340.dp))
        }
      } else {
        // Portrait vertical hero CTA stack with playful sine-wave divider
        Column(
          modifier = Modifier
            .align(Alignment.Center)
            .fillMaxWidth()
            .padding(horizontal = 24.dp),
          horizontalAlignment = Alignment.CenterHorizontally
        ) {
          Text(
            text = stringResource(R.string.app_name),
            fontSize = 46.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground,
            letterSpacing = (-0.5).sp
          )
          Spacer(Modifier.height(10.dp))
          // Playful sine-wave divider
          WavyAccentDivider(
            modifier = Modifier.width(150.dp).height(12.dp),
            color = TeachColors.Primary
          )
          Spacer(Modifier.height(36.dp))
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
  var activePlayerRecording by remember { mutableStateOf<RecordingItem?>(null) }
  var selectedSessionForRecordings by remember { mutableStateOf<BoardSession?>(null) }

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

  val playRecording = { rec: RecordingItem ->
    activePlayerRecording = rec
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

  LaunchedEffect(Unit) {
    viewModel.refreshRecordings()
  }

  LaunchedEffect(msg) {
    msg?.let { snackbar.showSnackbar(it); viewModel.clearMessage() }
  }

  if (selectedSessionForRecordings != null) {
    val activeSession = selectedSessionForRecordings!!
    val activeRecordings = sessionRecordingsMap[activeSession.id] ?: emptyList()
    BoardRecordingsDetailScreen(
      session = activeSession,
      recordings = activeRecordings,
      onBack = { selectedSessionForRecordings = null },
      onPlayRecording = playRecording,
      onShareRecording = shareRecording,
      onDeleteRecording = { deleteRecTarget = it },
      onOpenBoard = { sessionId ->
        selectedSessionForRecordings = null
        onOpenBoard(sessionId)
      }
    )
  } else {
    Scaffold(
      containerColor = TeachColors.Background,
      contentWindowInsets = WindowInsets(0, 0, 0, 0),
      floatingActionButton = {
        FloatingActionButton(
          onClick = { viewModel.createNewBoard(onOpenBoard) },
          containerColor = TeachColors.Primary,
          contentColor = Color.White,
          shape = RoundedCornerShape(22.dp),
          modifier = Modifier.navigationBarsPadding().padding(bottom = 8.dp)
        ) {
          Icon(Icons.Default.Add, contentDescription = stringResource(R.string.new_whiteboard), modifier = Modifier.size(28.dp))
        }
      },
      snackbarHost = { SnackbarHost(snackbar) }
    ) { _ ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(horizontal = 16.dp)
    ) {
      // 1. Top Action Controls Row (Back pill squircle + Sort dropdown pill)
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Back Pill Squircle
        Surface(
          shape = CircleShape,
          color = Color.White,
          shadowElevation = 2.dp,
          modifier = Modifier
            .size(44.dp)
            .border(1.dp, Color(0xFFEBEBF5), CircleShape)
            .bouncyClickable(onClick = onBack)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = TeachColors.OnSurface,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Sort Filter Pill Button
        Box {
          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = Color.White,
            shadowElevation = 2.dp,
            modifier = Modifier
              .height(40.dp)
              .border(1.dp, Color(0xFFEBEBF5), RoundedCornerShape(9999.dp))
              .bouncyClickable { sortMenuOpen = true }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Icon(
                Icons.AutoMirrored.Filled.Sort,
                contentDescription = null,
                tint = TeachColors.Primary,
                modifier = Modifier.size(16.dp)
              )
              Text(
                text = when (sort) {
                  SortOption.NEWEST -> stringResource(R.string.sort_newest)
                  SortOption.OLDEST -> stringResource(R.string.sort_oldest)
                  SortOption.NAME_AZ -> stringResource(R.string.sort_name)
                },
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TeachColors.OnSurface
              )
              Icon(
                Icons.Default.KeyboardArrowDown,
                contentDescription = null,
                tint = TeachColors.OnSurfaceMuted,
                modifier = Modifier.size(16.dp)
              )
            }
          }

          DropdownMenu(
            expanded = sortMenuOpen,
            onDismissRequest = { sortMenuOpen = false },
            modifier = Modifier
              .background(Color.White)
              .clip(RoundedCornerShape(16.dp))
          ) {
            DropdownMenuItem(
              text = {
                Text(
                  stringResource(R.string.sort_newest),
                  fontWeight = if (sort == SortOption.NEWEST) FontWeight.Bold else FontWeight.Normal,
                  color = if (sort == SortOption.NEWEST) TeachColors.Primary else TeachColors.OnSurface
                )
              },
              onClick = { viewModel.sortOption.value = SortOption.NEWEST; sortMenuOpen = false }
            )
            DropdownMenuItem(
              text = {
                Text(
                  stringResource(R.string.sort_oldest),
                  fontWeight = if (sort == SortOption.OLDEST) FontWeight.Bold else FontWeight.Normal,
                  color = if (sort == SortOption.OLDEST) TeachColors.Primary else TeachColors.OnSurface
                )
              },
              onClick = { viewModel.sortOption.value = SortOption.OLDEST; sortMenuOpen = false }
            )
            DropdownMenuItem(
              text = {
                Text(
                  stringResource(R.string.sort_name),
                  fontWeight = if (sort == SortOption.NAME_AZ) FontWeight.Bold else FontWeight.Normal,
                  color = if (sort == SortOption.NAME_AZ) TeachColors.Primary else TeachColors.OnSurface
                )
              },
              onClick = { viewModel.sortOption.value = SortOption.NAME_AZ; sortMenuOpen = false }
            )
          }
        }
      }

      // 2. Title & Dynamic Count Pill Badge & Wavy Accent Divider
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 14.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = stringResource(R.string.my_whiteboards),
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TeachColors.OnSurface,
            letterSpacing = (-0.5).sp
          )
          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = TeachColors.PastelMint,
            modifier = Modifier.padding(top = 2.dp)
          ) {
            Text(
              text = stringResource(R.string.gallery_boards_count, sessions.size),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = TeachColors.PastelMintGlyph,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
          }
        }

        WavyAccentDivider(
          modifier = Modifier
            .width(96.dp)
            .height(10.dp),
          color = TeachColors.Primary,
          waveCount = 4,
          amplitudeDp = 2.5f,
          strokeWidthDp = 3f
        )
      }

      // 3. Pill Search Field (Full capsule shape matching Stitch design)
      Surface(
        shape = RoundedCornerShape(9999.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .height(52.dp)
          .border(1.dp, Color(0xFFECEEF6), RoundedCornerShape(9999.dp))
      ) {
        Row(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Icon(
            Icons.Default.Search,
            contentDescription = null,
            tint = TeachColors.Primary,
            modifier = Modifier.size(20.dp)
          )
          BasicTextField(
            value = query,
            onValueChange = { viewModel.searchQuery.value = it },
            modifier = Modifier.weight(1f),
            singleLine = true,
            textStyle = TextStyle(
              fontSize = 14.sp,
              fontWeight = FontWeight.Medium,
              color = TeachColors.OnSurface
            ),
            decorationBox = { innerTextField ->
              if (query.isEmpty()) {
                Text(
                  stringResource(R.string.search_boards_hint),
                  fontSize = 14.sp,
                  color = Color(0xFF8F92A1)
                )
              }
              innerTextField()
            }
          )
          if (query.isNotEmpty()) {
            IconButton(
              onClick = { viewModel.searchQuery.value = "" },
              modifier = Modifier.size(28.dp)
            ) {
              Icon(
                Icons.Default.Close,
                contentDescription = "Clear",
                tint = TeachColors.OnSurfaceMuted,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }

      Spacer(Modifier.height(16.dp))

      if (sessions.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
          Column(horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(Icons.Default.Draw, contentDescription = null, modifier = Modifier.size(60.dp), tint = TeachColors.Primary.copy(alpha = 0.6f))
            Text(stringResource(R.string.empty_boards_title), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
            Text(stringResource(R.string.empty_boards_desc), style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
          }
        }
      } else {
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
              onDeleteRecording = { deleteRecTarget = it },
              onOpenRecordings = { selectedSessionForRecordings = session }
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
      title = { Text(stringResource(R.string.delete_board_title)) },
      text = { Text(stringResource(R.string.delete_board_confirm, target.title)) },
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

  activePlayerRecording?.let { rec ->
    InAppVideoPlayerDialog(
      recording = rec,
      onDismiss = { activePlayerRecording = null },
      onShare = { shareRecording(rec) },
      onDelete = { deleteRecTarget = rec; activePlayerRecording = null }
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
  onDeleteRecording: (RecordingItem) -> Unit,
  onOpenRecordings: () -> Unit
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
      .clip(RoundedCornerShape(26.dp))
      .border(1.dp, Color(0xFFEBEBF5), RoundedCornerShape(26.dp))
      .bouncyClickable(onClick = onClick),
    color = Color.White,
    shadowElevation = 3.dp
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      // 1. Whiteboard Preview Box
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .aspectRatio(1.65f)
          .clip(RoundedCornerShape(18.dp))
          .background(Color(session.backgroundColor))
          .border(1.dp, Color(0xFFECEFF8), RoundedCornerShape(18.dp))
      ) {
        if (thumbBitmap != null) {
          Image(
            bitmap = thumbBitmap.asImageBitmap(),
            contentDescription = session.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
          ) {
            Icon(
              Icons.Default.Draw,
              contentDescription = null,
              tint = TeachColors.Primary.copy(alpha = 0.25f),
              modifier = Modifier.size(40.dp)
            )
          }
        }

        // Top-left: Page Count Pill Badge (Pastel Periwinkle)
        Surface(
          shape = RoundedCornerShape(9999.dp),
          color = TeachColors.PastelPeriwinkle,
          shadowElevation = 1.dp,
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(10.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(4.dp)
          ) {
            if (!session.pdfPath.isNullOrBlank()) {
              Icon(
                Icons.Default.PictureAsPdf,
                contentDescription = null,
                tint = Color(0xFFE11D48),
                modifier = Modifier.size(12.dp)
              )
            }
            Text(
              text = "${session.pageCount} page${if (session.pageCount > 1) "s" else ""}${if (!session.pdfPath.isNullOrBlank()) " · PDF" else ""}",
              color = TeachColors.PastelPeriwinkleGlyph,
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold
            )
          }
        }
      }

      Spacer(Modifier.height(12.dp))

      // 2. Title & Date & 3-dots Menu Row
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 2.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Column(modifier = Modifier.weight(1f)) {
          Text(
            text = session.title,
            fontWeight = FontWeight.Bold,
            fontSize = 16.sp,
            color = TeachColors.OnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Spacer(Modifier.height(2.dp))
          Text(
            text = stringResource(R.string.board_modified_label, dateStr),
            fontSize = 12.sp,
            color = TeachColors.OnSurfaceMuted,
            fontWeight = FontWeight.Medium
          )
        }

        Box {
          IconButton(
            onClick = { menuOpen = true },
            modifier = Modifier.size(36.dp)
          ) {
            Icon(
              Icons.Default.MoreVert,
              contentDescription = "Options",
              tint = TeachColors.OnSurfaceMuted,
              modifier = Modifier.size(20.dp)
            )
          }

          DropdownMenu(
            expanded = menuOpen,
            onDismissRequest = { menuOpen = false },
            modifier = Modifier
              .background(Color.White)
              .clip(RoundedCornerShape(16.dp))
          ) {
            DropdownMenuItem(
              text = { Text(stringResource(R.string.rename), fontWeight = FontWeight.Medium) },
              onClick = { menuOpen = false; onRename() }
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.duplicate), fontWeight = FontWeight.Medium) },
              onClick = { menuOpen = false; onDuplicate() }
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.export_pdf_title), fontWeight = FontWeight.Medium) },
              onClick = { menuOpen = false; onExportPdf() }
            )
            DropdownMenuItem(
              text = { Text(stringResource(R.string.delete), color = TeachColors.Danger, fontWeight = FontWeight.Bold) },
              onClick = { menuOpen = false; onDelete() }
            )
          }
        }
      }

      // 3. Screen Recording Sub-Card (Pastel Blush with dedicated recordings gallery launcher)
      if (recordings.isNotEmpty()) {
        Spacer(Modifier.height(10.dp))

        Surface(
          shape = RoundedCornerShape(18.dp),
          color = TeachColors.PastelBlush,
          modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, Color(0xFFFAD0DC), RoundedCornerShape(18.dp))
            .bouncyClickable { onOpenRecordings() }
        ) {
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            // Left: Video Icon + Count & Duration
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp),
              modifier = Modifier.weight(1f, fill = false)
            ) {
              Box(
                modifier = Modifier
                  .size(34.dp)
                  .clip(CircleShape)
                  .background(Color(0xFFE54874)),
                contentAlignment = Alignment.Center
              ) {
                Icon(
                  Icons.Default.Videocam,
                  contentDescription = null,
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
              Column {
                Text(
                  text = "${recordings.size} Screen Recording${if (recordings.size > 1) "s" else ""}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = TeachColors.PastelBlushGlyph
                )
                if (totalDurationAndSize.isNotBlank()) {
                  Text(
                    text = totalDurationAndSize,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    color = TeachColors.PastelBlushGlyph.copy(alpha = 0.8f)
                  )
                }
              }
            }

            // Right: "Watch ▶" / "See All" pill button + chevron
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(9999.dp),
                color = TeachColors.Primary,
                shadowElevation = 2.dp,
                modifier = Modifier
                  .bouncyClickable { onOpenRecordings() }
              ) {
                Row(
                  modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
                  verticalAlignment = Alignment.CenterVertically,
                  horizontalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                  Text(
                    text = if (recordings.size > 1) "See All (${recordings.size})" else "Watch",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                  )
                  Icon(
                    if (recordings.size > 1) Icons.AutoMirrored.Filled.KeyboardArrowRight else Icons.Default.PlayArrow,
                    contentDescription = null,
                    tint = Color.White,
                    modifier = Modifier.size(14.dp)
                  )
                }
              }

              Icon(
                Icons.AutoMirrored.Filled.KeyboardArrowRight,
                contentDescription = "Open recordings",
                tint = TeachColors.PastelBlushGlyph,
                modifier = Modifier.size(20.dp)
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
    shape = RoundedCornerShape(16.dp),
    color = Color.White,
    shadowElevation = 1.dp,
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, Color(0xFFECEFF8), RoundedCornerShape(16.dp))
      .bouncyClickable(onClick = onPlay)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
      Box(
        modifier = Modifier
          .size(width = 68.dp, height = 46.dp)
          .clip(RoundedCornerShape(10.dp))
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
          color = TeachColors.OnSurface,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis
        )
        Spacer(Modifier.height(2.dp))
        Text(
          listOfNotNull(durationStr.ifBlank { null }, formattedSize.ifBlank { null }).joinToString(" · "),
          fontSize = 11.sp,
          color = TeachColors.OnSurfaceMuted
        )
      }

      Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
        IconButton(onClick = onShare, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Share, contentDescription = "Share", tint = TeachColors.OnSurfaceMuted, modifier = Modifier.size(16.dp))
        }
        IconButton(onClick = onDelete, modifier = Modifier.size(32.dp)) {
          Icon(Icons.Default.Delete, contentDescription = "Delete", tint = TeachColors.Danger, modifier = Modifier.size(16.dp))
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
      .clip(RoundedCornerShape(26.dp))
      .border(1.dp, Color(0xFFEBEBF5), RoundedCornerShape(26.dp)),
    color = Color.White,
    shadowElevation = 2.dp
  ) {
    Column(modifier = Modifier.padding(14.dp)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .bouncyClickable { expanded = !expanded },
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
      ) {
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
          Box(
            modifier = Modifier
              .size(34.dp)
              .clip(CircleShape)
              .background(Color(0xFFE54874)),
            contentAlignment = Alignment.Center
          ) {
            Icon(Icons.Default.Videocam, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
          }
          Column {
            Text(
              "Other Screen Recordings (${recordings.size})",
              fontWeight = FontWeight.Bold,
              fontSize = 14.sp,
              color = TeachColors.OnSurface
            )
            Text(
              "Recordings from previous sessions",
              fontSize = 11.sp,
              color = TeachColors.OnSurfaceMuted
            )
          }
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
          Text(
            if (expanded) "Hide" else "See recordings",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFE54874)
          )
          Icon(
            if (expanded) Icons.Default.KeyboardArrowUp else Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = Color(0xFFE54874),
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

/**
 * Dedicated Modern Screen for browsing, previewing, and playing all screen recordings
 * for a specific whiteboard and its slides.
 * Strictly designed according to the Pastel Playful Expressive aesthetic.
 */
@Composable
fun BoardRecordingsDetailScreen(
  session: BoardSession,
  recordings: List<RecordingItem>,
  onBack: () -> Unit,
  onPlayRecording: (RecordingItem) -> Unit,
  onShareRecording: (RecordingItem) -> Unit,
  onDeleteRecording: (RecordingItem) -> Unit,
  onOpenBoard: (String) -> Unit
) {
  val context = LocalContext.current
  BackHandler(onBack = onBack)

  val boardThumbBitmap = remember(session.id, session.updatedAt) {
    val file = Exporters.thumbnailFile(context, session.id)
    if (file.exists()) runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull() else null
  }

  val totalBytes = remember(recordings) { recordings.sumOf { it.sizeBytes } }
  val totalDurMs = remember(recordings) { recordings.sumOf { it.durationMs } }
  val totalDurFormatted = remember(totalDurMs) {
    val sec = totalDurMs / 1000
    if (sec > 0) String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60) else ""
  }
  val totalSizeFormatted = remember(totalBytes) {
    when {
      totalBytes <= 0L -> "0 B"
      totalBytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", totalBytes / 1024.0)
      totalBytes < 1024L * 1024L * 1024L -> String.format(Locale.US, "%.1f MB", totalBytes / (1024.0 * 1024.0))
      else -> String.format(Locale.US, "%.2f GB", totalBytes / (1024.0 * 1024.0 * 1024.0))
    }
  }

  Scaffold(
    containerColor = TeachColors.Background,
    contentWindowInsets = WindowInsets(0, 0, 0, 0)
  ) { _ ->
    Column(
      modifier = Modifier
        .fillMaxSize()
        .statusBarsPadding()
        .padding(horizontal = 16.dp)
    ) {
      // 1. Top Navigation Row: Back button pill + "Open Canvas" action
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 4.dp, bottom = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        // Back Pill
        Surface(
          shape = CircleShape,
          color = Color.White,
          shadowElevation = 2.dp,
          modifier = Modifier
            .size(44.dp)
            .border(1.dp, Color(0xFFEBEBF5), CircleShape)
            .bouncyClickable(onClick = onBack)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              Icons.AutoMirrored.Filled.ArrowBack,
              contentDescription = "Back",
              tint = TeachColors.OnSurface,
              modifier = Modifier.size(20.dp)
            )
          }
        }

        // Open Canvas Button
        Surface(
          shape = RoundedCornerShape(9999.dp),
          color = TeachColors.PastelPeriwinkle,
          shadowElevation = 1.dp,
          modifier = Modifier
            .border(1.dp, Color(0xFFC8DAFD), RoundedCornerShape(9999.dp))
            .bouncyClickable { onOpenBoard(session.id) }
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(6.dp)
          ) {
            Icon(
              Icons.Default.Draw,
              contentDescription = null,
              tint = TeachColors.PastelPeriwinkleGlyph,
              modifier = Modifier.size(16.dp)
            )
            Text(
              text = stringResource(R.string.mode_draw),
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = TeachColors.PastelPeriwinkleGlyph
            )
          }
        }
      }

      // 2. Title Section (Screen Recordings title with wavy accent)
      Column(
        modifier = Modifier
          .fillMaxWidth()
          .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(4.dp)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
          Text(
            text = stringResource(R.string.record_screen_title),
            fontSize = 24.sp,
            fontWeight = FontWeight.ExtraBold,
            color = TeachColors.OnSurface,
            letterSpacing = (-0.5).sp
          )
          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = TeachColors.PastelBlush,
            modifier = Modifier.padding(top = 2.dp)
          ) {
            Text(
              text = "${recordings.size} video${if (recordings.size != 1) "s" else ""}",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = TeachColors.PastelBlushGlyph,
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 3.dp)
            )
          }
        }

        WavyAccentDivider(
          modifier = Modifier
            .width(110.dp)
            .height(10.dp),
          color = TeachColors.Primary,
          waveCount = 4,
          amplitudeDp = 2.5f,
          strokeWidthDp = 3f
        )
      }

      // 3. Hero Whiteboard Summary Card
      Surface(
        shape = RoundedCornerShape(22.dp),
        color = Color.White,
        shadowElevation = 2.dp,
        modifier = Modifier
          .fillMaxWidth()
          .border(1.dp, Color(0xFFEBEBF5), RoundedCornerShape(22.dp))
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .padding(12.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
          // Board Preview Thumbnail
          Box(
            modifier = Modifier
              .size(width = 84.dp, height = 64.dp)
              .clip(RoundedCornerShape(14.dp))
              .background(Color(session.backgroundColor))
              .border(1.dp, Color(0xFFECEFF8), RoundedCornerShape(14.dp))
          ) {
            if (boardThumbBitmap != null) {
              Image(
                bitmap = boardThumbBitmap.asImageBitmap(),
                contentDescription = session.title,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
              )
            } else {
              Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Icon(
                  Icons.Default.Draw,
                  contentDescription = null,
                  tint = TeachColors.Primary.copy(alpha = 0.35f),
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          }

          // Board Info & Slide count
          Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(3.dp)
          ) {
            Text(
              text = session.title,
              fontWeight = FontWeight.Bold,
              fontSize = 15.sp,
              color = TeachColors.OnSurface,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )

            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Surface(
                shape = RoundedCornerShape(9999.dp),
                color = TeachColors.PastelMint
              ) {
                Text(
                  text = "${session.pageCount} slide${if (session.pageCount > 1) "s" else ""}",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold,
                  color = TeachColors.PastelMintGlyph,
                  modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
                )
              }

              if (totalDurFormatted.isNotBlank()) {
                Text(
                  text = "$totalDurFormatted · $totalSizeFormatted",
                  fontSize = 12.sp,
                  fontWeight = FontWeight.Medium,
                  color = TeachColors.OnSurfaceMuted
                )
              }
            }
          }
        }
      }

      Spacer(Modifier.height(14.dp))

      // 4. Recordings List
      if (recordings.isEmpty()) {
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .weight(1f),
          contentAlignment = Alignment.Center
        ) {
          Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            Icon(
              Icons.Default.Videocam,
              contentDescription = null,
              modifier = Modifier.size(54.dp),
              tint = TeachColors.Primary.copy(alpha = 0.35f)
            )
            Text(
              text = "No recordings for this board",
              fontWeight = FontWeight.Bold,
              fontSize = 16.sp,
              color = TeachColors.OnSurface
            )
            Surface(
              shape = RoundedCornerShape(9999.dp),
              color = TeachColors.Primary,
              modifier = Modifier.bouncyClickable(onClick = onBack)
            ) {
              Text(
                text = "Back to Whiteboards",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 13.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
              )
            }
          }
        }
      } else {
        LazyColumn(
          contentPadding = PaddingValues(bottom = 32.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          itemsIndexed(recordings, key = { _, it -> it.id }) { index, rec ->
            RecordingLargeCard(
              index = index,
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

/**
 * Modern High-Impact Video Recording Card for the Board Recordings screen.
 * Displays video thumbnail, floating play action, duration badge,
 * format chips, and direct playback/share/delete action pills.
 */
@Composable
private fun RecordingLargeCard(
  index: Int,
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
          context.contentResolver.loadThumbnail(recording.uri, Size(640, 360), null)
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

  val formattedDuration = remember(recording.durationMs) {
    val sec = recording.durationMs / 1000
    if (sec > 0) String.format(Locale.US, "%02d:%02d", sec / 60, sec % 60) else "00:00"
  }

  val formattedSize = remember(recording.sizeBytes) {
    when {
      recording.sizeBytes <= 0L -> ""
      recording.sizeBytes < 1024L * 1024L -> String.format(Locale.US, "%.1f KB", recording.sizeBytes / 1024.0)
      recording.sizeBytes < 1024L * 1024L * 1024L -> String.format(Locale.US, "%.1f MB", recording.sizeBytes / (1024.0 * 1024.0))
      else -> String.format(Locale.US, "%.2f GB", recording.sizeBytes / (1024.0 * 1024.0 * 1024.0))
    }
  }

  val dateStr = remember(recording.dateModifiedSec) {
    SimpleDateFormat("MMM dd, yyyy · HH:mm", Locale.getDefault()).format(Date(recording.dateModifiedSec * 1000L))
  }

  Surface(
    shape = RoundedCornerShape(22.dp),
    color = Color.White,
    shadowElevation = 2.dp,
    modifier = Modifier
      .fillMaxWidth()
      .border(1.dp, Color(0xFFEBEBF5), RoundedCornerShape(22.dp))
  ) {
    Column {
      // 1. Video Thumbnail Showcase (Clickable to launch modern video player)
      Box(
        modifier = Modifier
          .fillMaxWidth()
          .height(175.dp)
          .clip(RoundedCornerShape(topStart = 22.dp, topEnd = 22.dp))
          .background(Color(0xFF141724))
          .bouncyClickable(onClick = onPlay),
        contentAlignment = Alignment.Center
      ) {
        if (thumbBitmap != null) {
          Image(
            bitmap = thumbBitmap!!.asImageBitmap(),
            contentDescription = recording.title,
            contentScale = ContentScale.Crop,
            modifier = Modifier.fillMaxSize()
          )
        } else {
          Icon(
            Icons.Default.Videocam,
            contentDescription = null,
            tint = Color.White.copy(alpha = 0.35f),
            modifier = Modifier.size(48.dp)
          )
        }

        // Center Floating Play Circle
        Surface(
          shape = CircleShape,
          color = TeachColors.Primary.copy(alpha = 0.92f),
          shadowElevation = 6.dp,
          modifier = Modifier.size(52.dp)
        ) {
          Box(contentAlignment = Alignment.Center) {
            Icon(
              Icons.Default.PlayArrow,
              contentDescription = "Play recording",
              tint = Color.White,
              modifier = Modifier.size(28.dp)
            )
          }
        }

        // Top-Left: Index Badge
        Surface(
          shape = RoundedCornerShape(9999.dp),
          color = Color.Black.copy(alpha = 0.65f),
          modifier = Modifier
            .align(Alignment.TopStart)
            .padding(10.dp)
        ) {
          Text(
            text = "Recording #${index + 1}",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
            modifier = Modifier.padding(horizontal = 9.dp, vertical = 3.dp)
          )
        }

        // Bottom-Right: Duration Pill
        if (formattedDuration.isNotBlank()) {
          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = Color.Black.copy(alpha = 0.72f),
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(10.dp)
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
              Text(
                text = formattedDuration,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
        }
      }

      // 2. Info & Metadata Block
      Column(
        modifier = Modifier.padding(14.dp),
        verticalArrangement = Arrangement.spacedBy(10.dp)
      ) {
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
          Text(
            text = recording.title.ifBlank { recording.displayName }.ifBlank { "TeachBoard Recording #${index + 1}" },
            fontWeight = FontWeight.Bold,
            fontSize = 15.sp,
            color = TeachColors.OnSurface,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
          )
          Text(
            text = dateStr,
            fontSize = 12.sp,
            color = TeachColors.OnSurfaceMuted
          )
        }

        // Metadata Chips Row (Format, Size)
        Row(
          horizontalArrangement = Arrangement.spacedBy(6.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = TeachColors.PastelPeriwinkle
          ) {
            Text(
              text = "1080p FHD",
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = TeachColors.PastelPeriwinkleGlyph,
              modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
            )
          }

          if (formattedSize.isNotBlank()) {
            Surface(
              shape = RoundedCornerShape(9999.dp),
              color = Color(0xFFF1F3F9)
            ) {
              Text(
                text = formattedSize,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = TeachColors.OnSurfaceMuted,
                modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp)
              )
            }
          }
        }

        // 3. Action Buttons Deck
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(8.dp),
          verticalAlignment = Alignment.CenterVertically
        ) {
          // Play Fullscreen (Primary)
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = TeachColors.Primary,
            shadowElevation = 2.dp,
            modifier = Modifier
              .weight(1f)
              .height(42.dp)
              .bouncyClickable(onClick = onPlay)
          ) {
            Row(
              modifier = Modifier.fillMaxSize(),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.Center
            ) {
              Icon(
                Icons.Default.PlayArrow,
                contentDescription = null,
                tint = Color.White,
                modifier = Modifier.size(18.dp)
              )
              Spacer(Modifier.width(6.dp))
              Text(
                text = "Play",
                fontSize = 13.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }

          // Share
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = TeachColors.PastelPeriwinkle,
            modifier = Modifier
              .size(width = 46.dp, height = 42.dp)
              .border(1.dp, Color(0xFFD0DEF8), RoundedCornerShape(14.dp))
              .bouncyClickable(onClick = onShare)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.Default.Share,
                contentDescription = "Share",
                tint = TeachColors.PastelPeriwinkleGlyph,
                modifier = Modifier.size(18.dp)
              )
            }
          }

          // Delete
          Surface(
            shape = RoundedCornerShape(14.dp),
            color = TeachColors.PastelBlush,
            modifier = Modifier
              .size(width = 46.dp, height = 42.dp)
              .border(1.dp, Color(0xFFFAD0DC), RoundedCornerShape(14.dp))
              .bouncyClickable(onClick = onDelete)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.Default.Delete,
                contentDescription = "Delete",
                tint = TeachColors.Danger,
                modifier = Modifier.size(18.dp)
              )
            }
          }
        }
      }
    }
  }
}
