package com.aeliavision.novagrab.feature.downloader.data.mapper

import com.aeliavision.novagrab.core.database.converters.DownloadStatusConverter
import com.aeliavision.novagrab.feature.downloader.data.entity.DownloadTaskEntity
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadTask

private val statusConverter = DownloadStatusConverter()

fun DownloadTaskEntity.toDomain(): DownloadTask {
    return DownloadTask(
        id = id,
        url = url,
        fileName = fileName,
        mimeType = mimeType,
        format = format,
        totalSizeBytes = totalSizeBytes,
        downloadedBytes = downloadedBytes,
        status = statusConverter.toStatus(status),
        headers = headers,
        sourcePageUrl = sourcePageUrl,
        createdAt = createdAt,
        completedAt = completedAt,
        savedUri = savedUri,
        error = error,
        retryCount = retryCount,
    )
}

fun DownloadTask.toEntity(): DownloadTaskEntity {
    return DownloadTaskEntity(
        id = id,
        url = url,
        fileName = fileName,
        mimeType = mimeType,
        format = format,
        totalSizeBytes = totalSizeBytes,
        downloadedBytes = downloadedBytes,
        status = statusConverter.fromStatus(status),
        headers = headers,
        sourcePageUrl = sourcePageUrl,
        createdAt = createdAt,
        completedAt = completedAt,
        savedUri = savedUri,
        error = error,
        retryCount = retryCount,
    )
}

fun DownloadStatus.toDbString(): String {
    return statusConverter.fromStatus(this)
}
