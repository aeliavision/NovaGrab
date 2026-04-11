package com.aeliavision.novagrab.feature.downloader.engine

import android.net.Uri
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadTask
import java.io.IOException

internal object DownloadRequestUtils {
    fun buildEffectiveHeaders(task: DownloadTask): Map<String, String> {
        val headers = task.headers.toMutableMap()

        if (!headers.keys.any { it.equals("Referer", ignoreCase = true) }) {
            task.sourcePageUrl
                ?.takeIf { it.isNotBlank() }
                ?.let { headers["Referer"] = it }
        }

        if (!headers.keys.any { it.equals("Origin", ignoreCase = true) }) {
            task.sourcePageUrl
                ?.takeIf { it.isNotBlank() }
                ?.let { pageUrl ->
                    runCatching {
                        val uri = Uri.parse(pageUrl)
                        "${uri.scheme}://${uri.host}"
                    }.getOrNull()?.let { origin ->
                        if (origin.isNotBlank()) headers["Origin"] = origin
                    }
                }
        }

        return headers
    }

    fun validateResponseLooksLikeMedia(contentType: String) {
        if (contentType.isBlank() || contentType.equals("application/octet-stream", ignoreCase = true)) return

        val isHtml = contentType.startsWith("text/html", ignoreCase = true)
        val isPlainText = contentType.startsWith("text/plain", ignoreCase = true)
        val isJson = contentType.startsWith("application/json", ignoreCase = true)
        val isXml = contentType.startsWith("application/xml", ignoreCase = true) ||
                   contentType.startsWith("text/xml", ignoreCase = true)

        val isM3U8 = contentType.contains("mpegurl", ignoreCase = true) ||
                    contentType.contains("apple.mpegurl", ignoreCase = true)

        if ((isHtml || isPlainText || isJson || isXml) && !isM3U8) {
            throw IOException(
                "Server returned $contentType instead of media — likely blocked by anti-hotlinking or expired URL"
            )
        }
    }
}
