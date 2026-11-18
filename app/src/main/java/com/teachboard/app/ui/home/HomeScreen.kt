package com.teachboard.app.ui.home

import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.appcompat.app.AppCompatDelegate
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
import androidx.compose.material.icons.automirrored.filled.Sort
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Draw
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Star
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
import androidx.compose.runtime.mutableStateOf
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

class HomeViewModel(private val app: TeachBoardApp) : ViewModel() {

  private val db = app.database
  private val prefs = app.prefs

  val searchQuery = MutableStateFlow("")
  val sortOption = MutableStateFlow(SortOption.NEWEST)
  val message = MutableStateFlow<String?>(null)

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
  var showLangDialog by remember { mutableStateOf(false) }

  val currentLangLabel = QuickLanguages.firstOrNull { it.first == prefs.languageTag }?.second ?: "English"
  val config = LocalConfiguration.current
  val isLandscape = config.screenWidthDp > config.screenHeightDp
  val isDarkBg = MaterialTheme.colorScheme.background == TeachColors.BackgroundDark

  Surface(
    modifier = Modifier.fillMaxSize(),
    color = MaterialTheme.colorScheme.background
  ) {
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
        modifier = Modifier.align(Alignment.TopEnd)
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
              Text(
                text = if (sessions.isEmpty()) stringResource(R.string.history_cta) else "${stringResource(R.string.history_cta)} (${sessions.size})",
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
 * Full-width single-column "My Whiteboards" History Gallery matching ref_02_history.png.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryGalleryScreen(
  viewModel: HomeViewModel,
  onBack: () -> Unit,
  onOpenBoard: (String) -> Unit
) {
  val sessions by viewModel.sessions.collectAsState()
  val query by viewModel.searchQuery.collectAsState()
  val sort by viewModel.sortOption.collectAsState()
  val msg by viewModel.message.collectAsState()
  val snackbar = remember { SnackbarHostState() }

  var sortMenuOpen by remember { mutableStateOf(false) }
  var renameTarget by remember { mutableStateOf<BoardSession?>(null) }
  var deleteTarget by remember { mutableStateOf<BoardSession?>(null) }

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
        LazyColumn(
          contentPadding = PaddingValues(bottom = 96.dp),
          verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
          items(sessions, key = { it.id }) { session ->
            FullWidthBoardCard(
              session = session,
              onClick = { onOpenBoard(session.id) },
              onRename = { renameTarget = session },
              onDuplicate = { viewModel.duplicateBoard(session) },
              onExportPdf = { viewModel.exportBoardPdf(session) },
              onDelete = { deleteTarget = session }
            )
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
}

@Composable
private fun FullWidthBoardCard(
  session: BoardSession,
  onClick: () -> Unit,
  onRename: () -> Unit,
  onDuplicate: () -> Unit,
  onExportPdf: () -> Unit,
  onDelete: () -> Unit
) {
  val context = LocalContext.current
  var menuOpen by remember { mutableStateOf(false) }

  val thumbBitmap = remember(session.id, session.updatedAt) {
    val file = Exporters.thumbnailFile(context, session.id)
    if (file.exists()) runCatching { BitmapFactory.decodeFile(file.absolutePath) }.getOrNull() else null
  }
  val dateStr = remember(session.updatedAt) {
    SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault()).format(Date(session.updatedAt))
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
    }
  }
}
