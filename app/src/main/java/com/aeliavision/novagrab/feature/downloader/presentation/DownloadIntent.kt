package com.aeliavision.novagrab.feature.downloader.presentation

sealed class DownloadIntent {
    data class Pause(val taskId: String) : DownloadIntent()
    data class Resume(val taskId: String) : DownloadIntent()
    data class Cancel(val taskId: String) : DownloadIntent()
    data class OpenPlayer(val taskId: String) : DownloadIntent()
    data class Share(val taskId: String) : DownloadIntent()
    data class Delete(val taskId: String) : DownloadIntent()
}
