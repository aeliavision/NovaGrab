package com.aeliavision.novagrab.feature.detection.presentation

import com.aeliavision.novagrab.feature.detection.domain.model.DetectedVideo
import com.aeliavision.novagrab.feature.downloader.engine.HlsVariant

data class DetectionState(
    val tabId: String = "",
    val videos: List<DetectedVideo> = emptyList(),
    val showSheet: Boolean = false,
    val showQualityPicker: Boolean = false,
    val qualityMasterUrl: String? = null,
    val qualityVariants: List<HlsVariant> = emptyList(),
    val selectedIds: Set<String> = emptySet(),
    val isMultiSelectMode: Boolean = false,
    val showRenameDialog: Boolean = false,
    val renameVideoUrl: String? = null,
    val renameInitialName: String = "",
)
