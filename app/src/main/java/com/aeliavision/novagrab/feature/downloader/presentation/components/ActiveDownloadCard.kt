package com.aeliavision.novagrab.feature.downloader.presentation.components


import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.Storage
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.ui.ObsidianButton
import com.aeliavision.novagrab.ui.ObsidianButtonStyle
import com.aeliavision.novagrab.ui.ObsidianCard
import com.aeliavision.novagrab.ui.ObsidianChip
import com.aeliavision.novagrab.ui.ObsidianProgressBar
import com.aeliavision.novagrab.ui.theme.PrimaryGradientBrush

@Composable
fun ActiveDownloadCard(
    fileName: String,
    status: DownloadStatus,
    progress: Float,
    speedText: String,
    etaText: String,
    sizeText: String,
    qualityText: String? = null,
    modifier: Modifier = Modifier,
    onPause: (() -> Unit)? = null,
    onResume: (() -> Unit)? = null,
    onCancel: (() -> Unit)? = null,
) {
    val shape = RoundedCornerShape(24.dp)

    ObsidianCard(
        modifier = modifier,
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
                    text = fileName,
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    ObsidianChip(
                        text = when (status) {
                            DownloadStatus.Queued -> "Queued"
                            DownloadStatus.Running -> "Downloading"
                            DownloadStatus.Paused -> "Paused"
                            DownloadStatus.Completed -> "Completed"
                            is DownloadStatus.Failed -> "Failed"
                            DownloadStatus.Cancelled -> "Cancelled"
                            DownloadStatus.Merging -> "Merging"
                        },
                        showRunningDot = status == DownloadStatus.Running,
                    )

                    if (!qualityText.isNullOrBlank()) {
                        ObsidianChip(text = qualityText)
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    MetaPill(icon = Icons.Outlined.Speed, text = speedText)
                    MetaPill(icon = Icons.Outlined.Schedule, text = etaText)
                    MetaPill(icon = Icons.Outlined.Storage, text = sizeText)
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp),
                ) {
                    if (status == DownloadStatus.Running && onPause != null) {
                        ObsidianButton(
                            text = "Pause",
                            onClick = onPause,
                            style = ObsidianButtonStyle.Primary,
                            modifier = Modifier.weight(1f),
                        )
                    } else if (status != DownloadStatus.Running && onResume != null) {
                        ObsidianButton(
                            text = "Resume",
                            onClick = onResume,
                            style = ObsidianButtonStyle.Primary,
                            modifier = Modifier.weight(1f),
                        )
                    }

                    if (onCancel != null) {
                        ObsidianButton(
                            text = "Cancel",
                            onClick = onCancel,
                            style = ObsidianButtonStyle.Secondary,
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }

            ObsidianProgressBar(
                progress = progress,
                height = 10.dp,
                fillBrush = PrimaryGradientBrush,
                cornerRadius = 24.dp,
            )
        }
    }
}

@Composable
private fun MetaPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    text: String,
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
        )
    }
}
