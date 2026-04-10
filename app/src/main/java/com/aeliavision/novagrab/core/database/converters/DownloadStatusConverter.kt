package com.aeliavision.novagrab.core.database.converters

import androidx.room.TypeConverter
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus

class DownloadStatusConverter {
    @TypeConverter
    fun fromStatus(status: DownloadStatus?): String = when (status) {
        is DownloadStatus.Queued -> "QUEUED"
        is DownloadStatus.Running -> "RUNNING"
        is DownloadStatus.Paused -> "PAUSED"
        is DownloadStatus.Completed -> "COMPLETED"
        is DownloadStatus.Cancelled -> "CANCELLED"
        is DownloadStatus.Merging -> "MERGING"
        is DownloadStatus.Failed -> "FAILED:${status.reason}"
        null -> "QUEUED"
    }

    @TypeConverter
    fun toStatus(value: String?): DownloadStatus {
        val v = value ?: return DownloadStatus.Queued
        return when {
            v == "QUEUED" -> DownloadStatus.Queued
            v == "RUNNING" -> DownloadStatus.Running
            v == "PAUSED" -> DownloadStatus.Paused
            v == "COMPLETED" -> DownloadStatus.Completed
            v == "CANCELLED" -> DownloadStatus.Cancelled
            v == "MERGING" -> DownloadStatus.Merging
            v.startsWith("FAILED:") -> DownloadStatus.Failed(v.removePrefix("FAILED:"))
            v == "FAILED" -> DownloadStatus.Failed("Unknown error")
            else -> DownloadStatus.Failed(v)
        }
    }
}
