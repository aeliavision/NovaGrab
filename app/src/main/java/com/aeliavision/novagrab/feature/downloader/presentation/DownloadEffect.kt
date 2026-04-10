package com.aeliavision.novagrab.feature.downloader.presentation

sealed class DownloadEffect {
    data class ShowMessage(val message: String) : DownloadEffect()
    data class OpenPlayer(val uri: String) : DownloadEffect()
    data class ShareDownload(val uri: String, val mimeType: String) : DownloadEffect()
}
