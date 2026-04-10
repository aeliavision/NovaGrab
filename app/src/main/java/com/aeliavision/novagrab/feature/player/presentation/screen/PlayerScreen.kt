package com.aeliavision.novagrab.feature.player.presentation.screen

import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import com.aeliavision.novagrab.ui.ObsidianIconButton
import com.aeliavision.novagrab.ui.ObsidianTopBar
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Slider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.aeliavision.novagrab.feature.player.presentation.PlayerIntent
import com.aeliavision.novagrab.feature.player.presentation.PlayerViewModel
import kotlinx.coroutines.delay

@Composable
fun PlayerScreen(
    onNavigateBack: () -> Unit,
    uriFromNav: String? = null,
    viewModel: PlayerViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(uriFromNav) {
        if (!uriFromNav.isNullOrBlank()) {
            viewModel.handleIntent(PlayerIntent.Play(uriFromNav))
        }
    }

    val player = remember {
        ExoPlayer.Builder(context).build()
    }

    var controlsVisible by remember { mutableStateOf(true) }
    var durationMs by remember { mutableLongStateOf(0L) }
    var positionMs by remember { mutableLongStateOf(0L) }
    var sliderValue by remember { mutableFloatStateOf(0f) }
    var isSeeking by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            player.release()
        }
    }

    LaunchedEffect(state.uri) {
        val uri = state.uri ?: return@LaunchedEffect
        player.setMediaItem(MediaItem.fromUri(Uri.parse(uri)))
        player.prepare()
        player.playWhenReady = true
    }

    LaunchedEffect(player) {
        while (true) {
            val d = player.duration
            durationMs = if (d == C.TIME_UNSET) 0L else d.coerceAtLeast(0L)
            if (!isSeeking) {
                positionMs = player.currentPosition.coerceAtLeast(0L)
                sliderValue = if (durationMs > 0) (positionMs.toFloat() / durationMs.toFloat()).coerceIn(0f, 1f) else 0f
            }
            delay(500)
        }
    }

    LaunchedEffect(controlsVisible, isSeeking) {
        if (controlsVisible && !isSeeking) {
            delay(2500)
            if (player.isPlaying) {
                controlsVisible = false
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        ObsidianTopBar(
            title = "Player",
            navigationIcon = {
                ObsidianIconButton(onClick = onNavigateBack) {
                    Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.onSurface)
                }
            }
        )

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .pointerInput(Unit) {
                    detectTapGestures(
                        onTap = { controlsVisible = !controlsVisible },
                        onDoubleTap = { offset ->
                            val seekBy = 10_000L
                            if (offset.x < size.width / 2f) {
                                player.seekTo((player.currentPosition - seekBy).coerceAtLeast(0L))
                            } else {
                                val target = player.currentPosition + seekBy
                                val end = if (durationMs > 0) durationMs else Long.MAX_VALUE
                                player.seekTo(target.coerceAtMost(end))
                            }
                            controlsVisible = true
                        },
                    )
                },
        ) {
            AndroidView(
                modifier = Modifier.fillMaxSize(),
                factory = {
                    PlayerView(it).apply {
                        this.player = player
                        useController = false
                    }
                },
                update = { view ->
                    view.player = player
                },
            )

            androidx.compose.animation.AnimatedVisibility(
                visible = controlsVisible,
                modifier = Modifier.align(androidx.compose.ui.Alignment.BottomCenter)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .background(
                            color = androidx.compose.ui.graphics.Color.Black.copy(alpha = 0.6f),
                            shape = RoundedCornerShape(24.dp),
                        )
                        .padding(16.dp),
                ) {
                    Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
                        IconButton(
                            onClick = {
                                if (player.isPlaying) player.pause() else player.play()
                            },
                        ) {
                            Icon(
                                imageVector = if (player.isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                                contentDescription = if (player.isPlaying) "Pause" else "Play",
                                tint = androidx.compose.ui.graphics.Color.White,
                            )
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        Text(
                            text = "${formatTime(positionMs)} / ${formatTime(durationMs)}",
                            color = androidx.compose.ui.graphics.Color.White,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Slider(
                        value = sliderValue,
                        onValueChange = {
                            isSeeking = true
                            sliderValue = it
                            if (durationMs > 0) {
                                positionMs = (durationMs * it).toLong().coerceIn(0L, durationMs)
                            }
                        },
                        onValueChangeFinished = {
                            if (durationMs > 0) {
                                player.seekTo((durationMs * sliderValue).toLong().coerceIn(0L, durationMs))
                            }
                            isSeeking = false
                        },
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

private fun formatTime(ms: Long): String {
    if (ms <= 0) return "0:00"
    val totalSeconds = ms / 1000
    val minutes = totalSeconds / 60
    val seconds = (totalSeconds % 60).toInt()
    return "$minutes:${seconds.toString().padStart(2, '0')}"
}
