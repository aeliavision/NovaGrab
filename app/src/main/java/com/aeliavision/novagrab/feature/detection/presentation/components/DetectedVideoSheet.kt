package com.aeliavision.novagrab.feature.detection.presentation.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.aeliavision.novagrab.feature.detection.domain.model.DetectedVideo
import com.aeliavision.novagrab.ui.ObsidianButton
import com.aeliavision.novagrab.ui.ObsidianButtonStyle

@Composable
fun DetectedVideoSheet(
    videos: List<DetectedVideo>,
    selectedIds: Set<String>,
    isMultiSelectMode: Boolean,
    onDownload: (DetectedVideo) -> Unit,
    onToggleSelection: (String) -> Unit,
    onDownloadSelected: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant)
            .padding(bottom = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp),
        ) {
            Box(
                modifier = Modifier
                    .align(androidx.compose.ui.Alignment.Center)
                    .height(5.dp)
                    .fillMaxWidth(0.18f)
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.18f)),
            )
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = "Detected videos",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
                Text(
                    text = "${videos.size} found",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }

            if (isMultiSelectMode) {
                ObsidianButton(
                    text = "Download ${selectedIds.size}",
                    onClick = onDownloadSelected,
                    style = ObsidianButtonStyle.Primary,
                )
            }
        }

        if (videos.isEmpty()) {
            Text(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                text = "No videos detected on this page yet.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.height(12.dp))
            return
        }

        LazyColumn(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            items(videos, key = { it.id }) { video ->
                DetectionVideoCard(
                    video = video,
                    isSelected = video.id in selectedIds,
                    isMultiSelectMode = isMultiSelectMode,
                    onDownload = onDownload,
                    onLongClick = { onToggleSelection(video.id) },
                    onToggleSelection = { onToggleSelection(video.id) },
                )
            }

            item {
                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
