package com.aeliavision.novagrab.feature.downloader.presentation.screen

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.PlayArrow
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.paging.compose.collectAsLazyPagingItems
import com.aeliavision.novagrab.feature.downloader.presentation.DownloadEffect
import com.aeliavision.novagrab.feature.downloader.presentation.DownloadIntent
import com.aeliavision.novagrab.feature.downloader.presentation.DownloadViewModel
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.feature.downloader.presentation.components.QueueListItem
import com.aeliavision.novagrab.ui.ObsidianEmptyState
import com.aeliavision.novagrab.ui.ObsidianIconButton
import com.aeliavision.novagrab.ui.ObsidianTopBar

@Composable
fun DownloadHistoryScreen(
    onNavigateBack: () -> Unit,
    onOpenPlayer: (String) -> Unit,
    viewModel: DownloadViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsState()
    val items = viewModel.historyPaging.collectAsLazyPagingItems()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is DownloadEffect.OpenPlayer -> onOpenPlayer(effect.uri)
                is DownloadEffect.ShareDownload -> {
                    val intent = Intent(Intent.ACTION_SEND).apply {
                        type = effect.mimeType
                        putExtra(Intent.EXTRA_STREAM, Uri.parse(effect.uri))
                        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                    }
                    context.startActivity(Intent.createChooser(intent, "Share Video"))
                }
                else -> Unit
            }
        }
    }

    Column(
        modifier = Modifier
            .windowInsetsPadding(WindowInsets.safeDrawing),
    ) {
        ObsidianTopBar(
            title = "History",
            navigationIcon = {
                ObsidianIconButton(onClick = onNavigateBack) {
                    androidx.compose.material3.Icon(
                        imageVector = Icons.Default.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                    )
                }
            },
        )

        if (items.itemCount == 0) {
            ObsidianEmptyState(
                title = "No history",
                subtitle = "No completed downloads yet.",
                modifier = Modifier.padding(top = 40.dp),
            )
            return
        }

        LazyColumn(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(
                count = items.itemCount,
                key = { index -> items[index]?.id ?: index },
            ) { index ->
                val item = items[index]
                if (item != null) {
                    QueueListItem(
                        fileName = item.fileName,
                        meta = when (item.status) {
                            DownloadStatus.Queued -> "Queued"
                            DownloadStatus.Running -> "Running"
                            DownloadStatus.Paused -> "Paused"
                            DownloadStatus.Completed -> "Completed"
                            is DownloadStatus.Failed -> "Failed"
                            DownloadStatus.Cancelled -> "Cancelled"
                            DownloadStatus.Merging -> "Merging"
                        },
                        statusIcon = null,
                        actionIcon = null,
                        onAction = null,
                        modifier = Modifier.padding(0.dp),
                    )

                    if (!item.savedUri.isNullOrBlank()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        androidx.compose.foundation.layout.Row(
                            modifier = Modifier.padding(start = 8.dp),
                            horizontalArrangement = Arrangement.spacedBy(10.dp),
                        ) {
                            ObsidianIconButton(onClick = { viewModel.handleIntent(DownloadIntent.OpenPlayer(item.id)) }) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.PlayArrow,
                                    contentDescription = "Play",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            ObsidianIconButton(onClick = { viewModel.handleIntent(DownloadIntent.Share(item.id)) }) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.Share,
                                    contentDescription = "Share",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                            ObsidianIconButton(onClick = { viewModel.handleIntent(DownloadIntent.Delete(item.id)) }) {
                                androidx.compose.material3.Icon(
                                    imageVector = Icons.Outlined.Delete,
                                    contentDescription = "Delete",
                                    tint = MaterialTheme.colorScheme.onSurface,
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
