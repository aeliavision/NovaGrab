package com.aeliavision.novagrab.feature.downloader.domain.usecase

import com.aeliavision.novagrab.feature.detection.domain.model.VideoFormat
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadStatus
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadTask
import com.aeliavision.novagrab.feature.downloader.repository.DownloadRepository
import com.aeliavision.novagrab.core.di.DownloadClient
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request

class EnqueueDownloadUseCase @Inject constructor(
    private val downloadRepository: DownloadRepository,
    @DownloadClient private val okHttpClient: OkHttpClient,
) {
    suspend operator fun invoke(
        url: String,
        fileName: String,
        mimeType: String,
        format: VideoFormat,
        headers: Map<String, String> = emptyMap(),
        sourcePageUrl: String? = null,
        totalSizeBytes: Long = -1L,
    ): DownloadTask {
        val resolvedSize = if (
            totalSizeBytes <= 0L &&
            format != VideoFormat.HLS &&
            format != VideoFormat.DASH
        ) {
            fetchContentLength(url, headers, sourcePageUrl)
        } else {
            totalSizeBytes
        }

        val task = DownloadTask(
            url = url,
            fileName = fileName,
            mimeType = mimeType,
            format = format,
            totalSizeBytes = resolvedSize,
            status = DownloadStatus.Queued,
            headers = headers,
            sourcePageUrl = sourcePageUrl,
        )
        downloadRepository.enqueue(task)
        return task
    }

    private suspend fun fetchContentLength(
        url: String,
        headers: Map<String, String>,
        sourcePageUrl: String?,
    ): Long = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(url)
                .head()
                .apply {
                    headers.forEach { (k, v) -> addHeader(k, v) }

                    if (sourcePageUrl?.isNotBlank() == true) {
                        if (!headers.keys.any { it.equals("Referer", ignoreCase = true) }) {
                            addHeader("Referer", sourcePageUrl)
                        }
                        if (!headers.keys.any { it.equals("Origin", ignoreCase = true) }) {
                            runCatching {
                                val uri = android.net.Uri.parse(sourcePageUrl)
                                "${uri.scheme}://${uri.host}"
                            }.getOrNull()?.let { origin ->
                                if (origin.isNotBlank()) addHeader("Origin", origin)
                            }
                        }
                    }
                }
                .build()

            okHttpClient.newCall(request).execute().use { response ->
                response.header("Content-Length")?.toLongOrNull() ?: -1L
            }
        } catch (_: Exception) {
            -1L
        }
    }
}
