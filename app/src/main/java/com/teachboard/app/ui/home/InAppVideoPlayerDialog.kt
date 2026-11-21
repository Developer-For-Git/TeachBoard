package com.teachboard.app.ui.home

import android.net.Uri
import android.view.ViewGroup
import androidx.annotation.OptIn
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Forward10
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Replay10
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.teachboard.app.ui.theme.TeachColors
import kotlinx.coroutines.delay
import java.util.Locale

/**
 * Immersive Full-Screen In-App Video Playback Studio.
 * Designed matching the user's reference screenshot and the Pastel Playful Expressive style:
 * - Edge-to-edge full screen playback with tap-to-toggle overlaid controls
 * - Floating center controls: 10s Rewind, Play/Pause, 10s Fast-Forward
 * - Sleek floating timeline capsule: live elapsed/total timestamps, slider scrubber, speed toggle
 * - Bottom action dock with prominent pastel blue Share/Save button and quick action pills
 */
@OptIn(UnstableApi::class)
@Composable
fun InAppVideoPlayerDialog(
  recording: RecordingItem,
  onDismiss: () -> Unit,
  onShare: (RecordingItem) -> Unit,
  onDelete: ((RecordingItem) -> Unit)? = null
) {
  InAppVideoPlayerDialog(
    uri = recording.uri,
    title = recording.title.ifBlank { recording.displayName }.ifBlank { "TeachBoard Recording" },
    durationMs = recording.durationMs,
    onDismiss = onDismiss,
    onShare = { onShare(recording) },
    onDelete = onDelete?.let { del -> { del(recording) } }
  )
}

@OptIn(UnstableApi::class)
@Composable
fun InAppVideoPlayerDialog(
  uri: Uri,
  title: String = "TeachBoard Screen Recording",
  durationMs: Long = 0L,
  onDismiss: () -> Unit,
  onShare: () -> Unit,
  onDelete: (() -> Unit)? = null
) {
  val context = LocalContext.current

  var isPlaying by remember { mutableStateOf(true) }
  var isMuted by remember { mutableStateOf(false) }
  var controlsVisible by remember { mutableStateOf(true) }
  var currentPositionMs by remember { mutableLongStateOf(0L) }
  var videoDurationMs by remember { mutableLongStateOf(durationMs.coerceAtLeast(1_000L)) }
  var currentSpeed by remember { mutableFloatStateOf(1.0f) }
  var isSeeking by remember { mutableStateOf(false) }
  var seekPositionRatio by remember { mutableFloatStateOf(0f) }

  val exoPlayer = remember(uri) {
    ExoPlayer.Builder(context).build().apply {
      setMediaItem(MediaItem.fromUri(uri))
      prepare()
      playWhenReady = true
    }
  }

  DisposableEffect(exoPlayer) {
    val listener = object : Player.Listener {
      override fun onIsPlayingChanged(playing: Boolean) {
        isPlaying = playing
      }

      override fun onPlaybackStateChanged(playbackState: Int) {
        if (playbackState == Player.STATE_READY) {
          val dur = exoPlayer.duration
          if (dur > 0L) videoDurationMs = dur
        } else if (playbackState == Player.STATE_ENDED) {
          isPlaying = false
          controlsVisible = true
        }
      }
    }
    exoPlayer.addListener(listener)
    onDispose {
      exoPlayer.removeListener(listener)
      exoPlayer.release()
    }
  }

  // Position ticker: updates position every 200ms when playing
  LaunchedEffect(isPlaying, isSeeking) {
    while (isPlaying && !isSeeking) {
      currentPositionMs = exoPlayer.currentPosition
      val dur = exoPlayer.duration
      if (dur > 0L) videoDurationMs = dur
      delay(200L)
    }
  }

  // Auto-hide controls after 4 seconds of uninterrupted playback
  LaunchedEffect(controlsVisible, isPlaying, isSeeking) {
    if (controlsVisible && isPlaying && !isSeeking) {
      delay(4000L)
      controlsVisible = false
    }
  }

  Dialog(
    onDismissRequest = onDismiss,
    properties = DialogProperties(usePlatformDefaultWidth = false, decorFitsSystemWindows = false)
  ) {
    Box(
      modifier = Modifier
        .fillMaxSize()
        .background(Color(0xFF0A0B0E))
    ) {
      // 1. Edge-to-Edge Whiteboard Video Viewport
      Box(
        modifier = Modifier
          .fillMaxSize()
          .clickable(
            interactionSource = remember { MutableInteractionSource() },
            indication = null
          ) {
            controlsVisible = !controlsVisible
          },
        contentAlignment = Alignment.Center
      ) {
        AndroidView(
          modifier = Modifier.fillMaxSize(),
          factory = { ctx ->
            PlayerView(ctx).apply {
              player = exoPlayer
              useController = false
              resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
              layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT
              )
            }
          },
          update = { it.player = exoPlayer }
        )
      }

      // 2. Cinematic Gradient Vignettes for optimal text & control legibility
      if (controlsVisible) {
        // Top dark gradient
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .align(Alignment.TopCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color(0xD9000000), Color(0x66000000), Color.Transparent)
              )
            )
        )

        // Bottom dark gradient
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .height(260.dp)
            .align(Alignment.BottomCenter)
            .background(
              Brush.verticalGradient(
                colors = listOf(Color.Transparent, Color(0x88000000), Color(0xEE000000))
              )
            )
        )
      }

      // 3. Overlaid Top Navigation Bar
      AnimatedVisibility(
        visible = controlsVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { -it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { -it }),
        modifier = Modifier.align(Alignment.TopCenter)
      ) {
        Row(
          modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(horizontal = 16.dp, vertical = 8.dp),
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.SpaceBetween
        ) {
          // Circular Back Pill Button
          Surface(
            shape = CircleShape,
            color = Color(0x881E1F2E),
            modifier = Modifier
              .size(44.dp)
              .border(1.dp, Color(0x33FFFFFF), CircleShape)
              .clickable(onClick = onDismiss)
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Back",
                tint = Color.White,
                modifier = Modifier.size(22.dp)
              )
            }
          }

          // Video Title & Metadata (centered)
          Column(
            modifier = Modifier
              .weight(1f)
              .padding(horizontal = 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
          ) {
            Text(
              text = title,
              fontSize = 15.sp,
              fontWeight = FontWeight.Bold,
              color = Color.White,
              maxLines = 1,
              overflow = TextOverflow.Ellipsis
            )
            Spacer(Modifier.height(2.dp))
            Row(
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
              Text(
                text = "TeachBoard Screen Recording",
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xB3FFFFFF)
              )
              Surface(
                shape = RoundedCornerShape(9999.dp),
                color = Color(0x44FFFFFF)
              ) {
                Text(
                  text = "1080p",
                  fontSize = 10.sp,
                  fontWeight = FontWeight.Bold,
                  color = Color.White,
                  modifier = Modifier.padding(horizontal = 6.dp, vertical = 1.dp)
                )
              }
            }
          }

          // Acceleration / Speed Selector Pill Button
          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = TeachColors.PastelMint,
            modifier = Modifier
              .clickable {
                val nextSpeed = when (currentSpeed) {
                  1.0f -> 1.25f
                  1.25f -> 1.5f
                  1.5f -> 2.0f
                  else -> 1.0f
                }
                currentSpeed = nextSpeed
                exoPlayer.setPlaybackSpeed(nextSpeed)
              }
          ) {
            Row(
              modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(3.dp)
            ) {
              Icon(
                Icons.Default.Speed,
                contentDescription = null,
                tint = TeachColors.PastelMintGlyph,
                modifier = Modifier.size(15.dp)
              )
              Text(
                text = "${currentSpeed}x",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = TeachColors.PastelMintGlyph
              )
            }
          }
        }
      }

      // 4. Overlaid Center Transport Controls (Rewind 10s, Large Play/Pause, Forward 10s)
      AnimatedVisibility(
        visible = controlsVisible,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = Modifier.align(Alignment.Center)
      ) {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          horizontalArrangement = Arrangement.spacedBy(28.dp)
        ) {
          // Rewind 10s Circular Button
          Surface(
            shape = CircleShape,
            color = Color(0x88000000),
            modifier = Modifier
              .size(56.dp)
              .border(1.dp, Color(0x44FFFFFF), CircleShape)
              .clickable {
                val target = (exoPlayer.currentPosition - 10_000L).coerceAtLeast(0L)
                exoPlayer.seekTo(target)
                currentPositionMs = target
              }
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.Default.Replay10,
                contentDescription = "Rewind 10s",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
              )
            }
          }

          // Central Large Play / Pause Elevated Circle
          Surface(
            shape = CircleShape,
            color = TeachColors.Primary,
            shadowElevation = 6.dp,
            modifier = Modifier
              .size(76.dp)
              .border(2.dp, Color(0x66FFFFFF), CircleShape)
              .clickable {
                if (isPlaying) exoPlayer.pause() else exoPlayer.play()
              }
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                contentDescription = if (isPlaying) "Pause" else "Play",
                tint = Color.White,
                modifier = Modifier.size(42.dp)
              )
            }
          }

          // Forward 10s Circular Button
          Surface(
            shape = CircleShape,
            color = Color(0x88000000),
            modifier = Modifier
              .size(56.dp)
              .border(1.dp, Color(0x44FFFFFF), CircleShape)
              .clickable {
                val target = (exoPlayer.currentPosition + 10_000L).coerceAtMost(videoDurationMs)
                exoPlayer.seekTo(target)
                currentPositionMs = target
              }
          ) {
            Box(contentAlignment = Alignment.Center) {
              Icon(
                Icons.Default.Forward10,
                contentDescription = "Forward 10s",
                tint = Color.White,
                modifier = Modifier.size(32.dp)
              )
            }
          }
        }
      }

      // 5. Bottom Controls Group (Timeline Capsule + Bottom Action Deck)
      AnimatedVisibility(
        visible = controlsVisible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it }),
        exit = fadeOut() + slideOutVertically(targetOffsetY = { it }),
        modifier = Modifier.align(Alignment.BottomCenter)
      ) {
        Column(
          modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding()
            .padding(horizontal = 16.dp, vertical = 12.dp),
          verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
          // A. Floating Timeline Scrubber Capsule (matching reference screenshot)
          Surface(
            shape = RoundedCornerShape(9999.dp),
            color = Color(0xCC181A22),
            shadowElevation = 4.dp,
            modifier = Modifier
              .fillMaxWidth()
              .border(1.dp, Color(0x33FFFFFF), RoundedCornerShape(9999.dp))
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 6.dp),
              verticalAlignment = Alignment.CenterVertically,
              horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
              // Current elapsed timestamp
              val displayCurrent = if (isSeeking) (seekPositionRatio * videoDurationMs).toLong() else currentPositionMs
              Text(
                text = formatDuration(displayCurrent),
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )

              // Timeline slider with white thumb & soft blue active track
              val progressRatio = if (isSeeking) seekPositionRatio else {
                if (videoDurationMs > 0L) (currentPositionMs.toFloat() / videoDurationMs.toFloat()).coerceIn(0f, 1f) else 0f
              }

              Slider(
                value = progressRatio,
                onValueChange = {
                  isSeeking = true
                  seekPositionRatio = it
                },
                onValueChangeFinished = {
                  val targetMs = (seekPositionRatio * videoDurationMs).toLong()
                  exoPlayer.seekTo(targetMs)
                  currentPositionMs = targetMs
                  isSeeking = false
                },
                colors = SliderDefaults.colors(
                  thumbColor = Color.White,
                  activeTrackColor = Color(0xFFA5B4FC),
                  inactiveTrackColor = Color(0x44FFFFFF)
                ),
                modifier = Modifier
                  .weight(1f)
                  .height(28.dp)
              )

              // Total duration timestamp
              Text(
                text = formatDuration(videoDurationMs),
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                color = Color(0xCCFFFFFF)
              )

              // Volume / Mute toggle button
              IconButton(
                onClick = {
                  isMuted = !isMuted
                  exoPlayer.volume = if (isMuted) 0f else 1f
                },
                modifier = Modifier.size(32.dp)
              ) {
                Icon(
                  imageVector = if (isMuted) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                  contentDescription = if (isMuted) "Unmute" else "Mute",
                  tint = Color.White,
                  modifier = Modifier.size(18.dp)
                )
              }
            }
          }

          // B. Bottom Action Deck (Large pill button on left + circular actions on right)
          Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
          ) {
            // Large Pastel Sky Pill Button: "Share Recording" (matches the screenshot's prominent bottom pill)
            Surface(
              shape = RoundedCornerShape(9999.dp),
              color = Color(0xFFDCE7FE),
              shadowElevation = 3.dp,
              modifier = Modifier
                .weight(1f)
                .height(52.dp)
                .clickable { onShare() }
            ) {
              Row(
                modifier = Modifier.fillMaxSize(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Icon(
                  Icons.Default.Share,
                  contentDescription = null,
                  tint = Color(0xFF264EA5),
                  modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text(
                  text = "Share Recording",
                  color = Color(0xFF264EA5),
                  fontSize = 14.sp,
                  fontWeight = FontWeight.ExtraBold
                )
              }
            }

            // Quick Speed Toggle Pill Circle
            Surface(
              shape = CircleShape,
              color = Color(0xCC1E202B),
              shadowElevation = 2.dp,
              modifier = Modifier
                .size(52.dp)
                .border(1.dp, Color(0x33FFFFFF), CircleShape)
                .clickable {
                  val nextSpeed = when (currentSpeed) {
                    1.0f -> 1.25f
                    1.25f -> 1.5f
                    1.5f -> 2.0f
                    else -> 1.0f
                  }
                  currentSpeed = nextSpeed
                  exoPlayer.setPlaybackSpeed(nextSpeed)
                }
            ) {
              Box(contentAlignment = Alignment.Center) {
                Text(
                  text = "${currentSpeed}x",
                  color = TeachColors.PastelMint,
                  fontSize = 13.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }

            // Quick Delete / Discard Circle (if deletion callback is provided)
            if (onDelete != null) {
              Surface(
                shape = CircleShape,
                color = Color(0xCC1E202B),
                shadowElevation = 2.dp,
                modifier = Modifier
                  .size(52.dp)
                  .border(1.dp, Color(0x33FFFFFF), CircleShape)
                  .clickable { onDelete() }
              ) {
                Box(contentAlignment = Alignment.Center) {
                  Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete",
                    tint = Color(0xFFFF6B81),
                    modifier = Modifier.size(22.dp)
                  )
                }
              }
            }
          }
        }
      }
    }
  }
}

private fun formatDuration(ms: Long): String {
  val totalSec = (ms / 1000L).coerceAtLeast(0L)
  val m = totalSec / 60L
  val s = totalSec % 60L
  return String.format(Locale.US, "%d:%02d", m, s)
}
