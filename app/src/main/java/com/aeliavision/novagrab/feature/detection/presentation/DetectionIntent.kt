package com.aeliavision.novagrab.feature.detection.presentation

sealed class DetectionIntent {
    data object ToggleSheet : DetectionIntent()
    data class DownloadVideo(val videoUrl: String) : DetectionIntent()
    data class SelectHlsVariant(val masterUrl: String, val variantUrl: String) : DetectionIntent()
    data object DismissQualityPicker : DetectionIntent()
    data class ToggleSelection(val videoId: String) : DetectionIntent()
    data object DownloadSelected : DetectionIntent()
    data class DownloadVideoWithName(val videoUrl: String, val fileName: String) : DetectionIntent()
    data class ShowRenameDialog(val videoUrl: String) : DetectionIntent()
    data object DismissRenameDialog : DetectionIntent()
    data object ClearSelection : DetectionIntent()
}
