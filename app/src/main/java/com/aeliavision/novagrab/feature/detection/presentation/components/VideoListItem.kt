package com.aeliavision.novagrab.feature.detection.presentation.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.aeliavision.novagrab.feature.detection.domain.model.DetectedVideo

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun VideoListItem(
    video: DetectedVideo,
    isSelected: Boolean = false,
    isMultiSelectMode: Boolean = false,
    onDownload: (DetectedVideo) -> Unit,
    onLongClick: () -> Unit = {},
    onToggleSelection: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    DetectionVideoCard(
        video = video,
        isSelected = isSelected,
        isMultiSelectMode = isMultiSelectMode,
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 8.dp),
        onDownload = onDownload,
        onLongClick = onLongClick,
        onToggleSelection = onToggleSelection,
    )
}
