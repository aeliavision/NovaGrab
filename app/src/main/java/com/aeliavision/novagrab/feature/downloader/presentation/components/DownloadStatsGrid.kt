package com.aeliavision.novagrab.feature.downloader.presentation.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aeliavision.novagrab.ui.ObsidianStatCard

@Composable
fun DownloadStatsGrid(
    storageText: String,
    speedText: String,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        ObsidianStatCard(
            label = "Storage",
            value = storageText,
            unit = "",
            modifier = Modifier.weight(1f),
        )
        ObsidianStatCard(
            label = "Speed",
            value = speedText,
            unit = "",
            modifier = Modifier.weight(1f),
        )
    }
}
