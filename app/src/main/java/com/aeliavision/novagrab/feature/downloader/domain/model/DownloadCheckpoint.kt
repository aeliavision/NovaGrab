package com.aeliavision.novagrab.feature.downloader.domain.model

data class DownloadCheckpoint(
    val bytesDownloaded: Long,
    val chunkOffsets: List<Long>,
)
