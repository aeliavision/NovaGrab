package com.aeliavision.novagrab.feature.detection.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aeliavision.novagrab.feature.detection.domain.model.DetectedVideo
import com.aeliavision.novagrab.ui.ObsidianButton
import com.aeliavision.novagrab.ui.ObsidianButtonStyle
import com.aeliavision.novagrab.ui.ObsidianCard
import com.aeliavision.novagrab.ui.ObsidianChip

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetectionVideoCard(
    video: DetectedVideo,
    isSelected: Boolean,
    isMultiSelectMode: Boolean,
    modifier: Modifier = Modifier,
    onDownload: (DetectedVideo) -> Unit,
    onLongClick: () -> Unit,
    onToggleSelection: () -> Unit,
) {
    val isBlob = video.videoUrl.startsWith("blob:")

    ObsidianCard(
        modifier = modifier
            .fillMaxWidth()
            .combinedClickable(
                enabled = !isBlob,
                onClick = {
                    if (isMultiSelectMode) onToggleSelection() else onDownload(video)
                },
                onLongClick = onLongClick,
            ),
        cornerRadius = 24.dp,
        ghostBorder = true,
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                Text(
                    text = video.title ?: video.format.extension.uppercase(),
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )

                if (isMultiSelectMode) {
                    ObsidianChip(
                        text = if (isSelected) "Selected" else "Select",
                        modifier = Modifier.padding(bottom = 2.dp)
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ObsidianChip(text = video.format.extension.uppercase())

                    val sizeText = if (video.estimatedSizeBytes > 0) {
                        "${(video.estimatedSizeBytes / (1024 * 1024))} MB"
                    } else {
                        "---"
                    }
                    ObsidianChip(text = sizeText)

                    val qualityLabel = when {
                        video.width >= 3840 -> "4K"
                        video.width >= 2560 -> "1440p"
                        video.width >= 1920 -> "1080p"
                        video.width >= 1280 -> "720p"
                        video.width > 0 -> "${video.width}w"
                        else -> null
                    }
                    if (qualityLabel != null) {
                        ObsidianChip(text = qualityLabel)
                    }
                }

                Text(
                    text = video.videoUrl,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                if (isBlob) {
                    Text(
                        text = "Stream (blob) — not downloadable",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error,
                    )
                }

                Spacer(modifier = Modifier.height(2.dp))

                ObsidianButton(
                    text = if (isMultiSelectMode) "Add" else "Download",
                    onClick = { onDownload(video) },
                    enabled = !isBlob,
                    style = ObsidianButtonStyle.Primary,
                    modifier = Modifier.fillMaxWidth(),
                )
            }
        }
    }
}
