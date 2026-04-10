package com.aeliavision.novagrab.feature.downloader.domain.model

sealed class DownloadStatus {
    data object Queued : DownloadStatus()
    data object Running : DownloadStatus()
    data object Paused : DownloadStatus()
    data object Completed : DownloadStatus()
    data class Failed(val reason: String) : DownloadStatus()
    data object Cancelled : DownloadStatus()
    data object Merging : DownloadStatus()
}
