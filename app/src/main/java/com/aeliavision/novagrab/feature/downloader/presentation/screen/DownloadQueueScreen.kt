package com.aeliavision.novagrab.feature.downloader.presentation.screen

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Pause
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.aeliavision.novagrab.feature.downloader.presentation.DownloadEffect
import com.aeliavision.novagrab.feature.downloader.presentation.DownloadIntent
import com.aeliavision.novagrab.feature.downloader.presentation.DownloadViewModel
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.feature.downloader.presentation.components.ActiveDownloadCard
import com.aeliavision.novagrab.feature.downloader.presentation.components.DownloadStatsGrid
import com.aeliavision.novagrab.feature.downloader.presentation.components.QueueListItem
import com.aeliavision.novagrab.ui.ObsidianEmptyState
import com.aeliavision.novagrab.ui.ObsidianIconButton
import com.aeliavision.novagrab.ui.ObsidianTopBar

@Composable
fun DownloadQueueScreen(
    onNavigateBack: () -> Unit,
    onOpenHistory: () -> Unit,
    viewModel: DownloadViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DownloadEffect.OpenPlayer -> Unit
                is DownloadEffect.ShowMessage -> Unit
                is DownloadEffect.ShareDownload -> Unit
            }
        }
    }

    val running = state.active.filter { it.status == DownloadStatus.Running }
    val queuedOrPaused = state.active.filter { it.status != DownloadStatus.Running }

    Column(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        ObsidianTopBar(
            title = "Downloads",
            navigationIcon = {
                ObsidianIconButton(onClick = onNavigateBack) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
            actions = {
                ObsidianIconButton(onClick = onOpenHistory) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.History,
                        contentDescription = "History",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )

        if (state.active.isEmpty()) {
            ObsidianEmptyState(
                title = "No downloads",
                subtitle = "Your queue is empty.",
                modifier = Modifier
                    .padding(top = 40.dp),
            )
            return
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = androidx.compose.foundation.layout.Arrangement.spacedBy(12.dp),
        ) {
            if (running.isNotEmpty()) {
                item {
                    Text(
                        text = "ACTIVE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(running, key = { it.id }) { task ->
                val total = task.totalSizeBytes
                val downloaded = task.downloadedBytes
                val progress = if (total > 0) (downloaded.toFloat() / total.toFloat()).coerceIn(0f, 1f) else 0f

                val approxSpeedBytesPerSec = state.speedBytesPerSecByTaskId[task.id] ?: 0L
                val speedText = if (approxSpeedBytesPerSec > 0) formatBytesPerSec(approxSpeedBytesPerSec) else "—"

                val sizeText = if (total > 0) {
                    "${(downloaded / (1024 * 1024))}MB / ${(total / (1024 * 1024))}MB"
                } else {
                    "${(downloaded / (1024 * 1024))}MB"
                }

                ActiveDownloadCard(
                    fileName = task.fileName,
                    status = task.status,
                    progress = progress,
                    speedText = speedText,
                    etaText = "—",
                    sizeText = sizeText,
                    qualityText = task.quality?.label,
                    onPause = { viewModel.handleIntent(DownloadIntent.Pause(task.id)) },
                    onCancel = { viewModel.handleIntent(DownloadIntent.Cancel(task.id)) },
                )
            }

            if (queuedOrPaused.isNotEmpty()) {
                item {
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "QUEUE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            items(queuedOrPaused, key = { it.id }) { task ->
                val meta = when (task.status) {
                    DownloadStatus.Paused -> "Paused"
                    DownloadStatus.Queued -> "Queued"
                    DownloadStatus.Completed -> "Completed"
                    DownloadStatus.Running -> "Running"
                    is DownloadStatus.Failed -> "Failed"
                    DownloadStatus.Cancelled -> "Cancelled"
                    DownloadStatus.Merging -> "Merging"
                }

                val actionIcon = when (task.status) {
                    DownloadStatus.Paused, DownloadStatus.Queued -> Icons.Outlined.PlayArrow
                    DownloadStatus.Running -> Icons.Outlined.Pause
                    DownloadStatus.Completed -> null
                    DownloadStatus.Cancelled -> Icons.Outlined.Close
                    DownloadStatus.Merging -> Icons.Outlined.Close
                    is DownloadStatus.Failed -> Icons.Outlined.Close
                }

                val onAction: (() -> Unit)? = when (task.status) {
                    DownloadStatus.Paused, DownloadStatus.Queued -> ({ viewModel.handleIntent(DownloadIntent.Resume(task.id)) })
                    DownloadStatus.Running -> ({ viewModel.handleIntent(DownloadIntent.Pause(task.id)) })
                    else -> null
                }

                val statusIcon = when (task.status) {
                    DownloadStatus.Completed -> Icons.Outlined.PlayArrow
                    is DownloadStatus.Failed, DownloadStatus.Cancelled -> Icons.Outlined.Close
                    else -> null
                }

                val statusIconContentDescription = when (task.status) {
                    DownloadStatus.Completed -> "Completed"
                    is DownloadStatus.Failed -> "Failed"
                    DownloadStatus.Cancelled -> "Cancelled"
                    else -> null
                }

                val actionIconContentDescription = when (task.status) {
                    DownloadStatus.Paused, DownloadStatus.Queued -> "Resume"
                    DownloadStatus.Running -> "Pause"
                    is DownloadStatus.Failed -> "Error"
                    DownloadStatus.Cancelled -> "Cancelled"
                    DownloadStatus.Merging -> "Merging"
                    DownloadStatus.Completed -> null
                }

                QueueListItem(
                    fileName = task.fileName,
                    meta = meta,
                    statusIcon = statusIcon,
                    statusIconContentDescription = statusIconContentDescription,
                    actionIcon = actionIcon,
                    actionIconContentDescription = actionIconContentDescription,
                    onAction = onAction,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { },
                )
            }

            item {
                Spacer(modifier = Modifier.height(8.dp))
                DownloadStatsGrid(
                    storageText = formatBytes(state.totalStorageUsedBytes),
                    speedText = if (state.averageSpeedBytesPerSec > 0) formatBytesPerSec(state.averageSpeedBytesPerSec) else "—",
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}

private fun formatBytes(bytes: Long): String {
    if (bytes <= 0L) return "—"
    val kb = bytes / 1024.0
    if (kb < 1024.0) return String.format("%.0f KB", kb)
    val mb = kb / 1024.0
    if (mb < 1024.0) return String.format("%.0f MB", mb)
    val gb = mb / 1024.0
    return String.format("%.1f GB", gb)
}

private fun formatBytesPerSec(bytesPerSec: Long): String {
    val kb = bytesPerSec / 1024.0
    if (kb < 1024.0) return String.format("%.0f KB/s", kb)
    val mb = kb / 1024.0
    return String.format("%.1f MB/s", mb)
}
