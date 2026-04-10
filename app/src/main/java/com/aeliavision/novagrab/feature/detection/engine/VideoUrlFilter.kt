package com.aeliavision.novagrab.feature.detection.engine

import com.aeliavision.novagrab.feature.detection.domain.model.VideoFormat
import javax.inject.Inject

class VideoUrlFilter @Inject constructor() {

    private val videoExtensions = setOf(
        "mp4", "m4v", "mov", "avi", "mkv", "webm",
        "flv", "wmv", "3gp", "m3u8", "mpd"
    )

    private val videoMimeTypes = setOf(
        "video/mp4", "video/webm", "video/ogg",
        "application/x-mpegurl", "application/vnd.apple.mpegurl",
        "application/dash+xml"
    )

    private val mediaPathPatterns = listOf(
        Regex("""/(video|stream|hls|dash)/""", RegexOption.IGNORE_CASE),
        Regex("""/v\d+[_-]"""),
        Regex("""quality=\d+p"""),
        Regex("""bitrate=\d+"""),
        Regex("""\.(m3u8|mpd|mp4)(\?|$)""", RegexOption.IGNORE_CASE),
    )

    private val exclusionPatterns = listOf(
        Regex("""(analytics|tracking|pixel|beacon|telemetry)""", RegexOption.IGNORE_CASE),
        Regex("""\.gif(\?|$)"""),
        Regex("""/_next/"""),
        Regex("""/wp-content/(?!uploads/.*\.(mp4|webm|m3u8))""", RegexOption.IGNORE_CASE),
        Regex("""/wp-includes/"""),
        Regex("""/cdn-cgi/"""),
        Regex("""/static/(js|css|chunks)/""", RegexOption.IGNORE_CASE),
        Regex("""\.(js)(\?|$)"""),
        Regex("""\.(css)(\?|$)"""),
        Regex("""\.woff2?(\?|$)"""),
        Regex("""\.(png)(\?|$)"""),
        Regex("""\.(jpe?g)(\?|$)"""),
        Regex("""\.(svg)(\?|$)"""),
        Regex("""\.(ico)(\?|$)"""),
        Regex("""\.(map)(\?|$)"""),
        Regex("""\.(ts)(\?|$)"""),
        Regex("""\.(m4s)(\?|$)"""),
        Regex("""(segment|chunk|frag)[\d_]""", RegexOption.IGNORE_CASE),
    )

    data class ScoredUrl(
        val url: String,
        val score: Int,
        val format: VideoFormat,
        val estimatedSize: Long = -1L,
    )

    fun score(url: String, mimeType: String? = null, contentLength: Long = -1L): ScoredUrl? {
        if (exclusionPatterns.any { it.containsMatchIn(url) }) return null

        var score = 0
        val format: VideoFormat

        val ext = url.substringAfterLast('.').substringBefore('?').lowercase()
        when {
            ext in videoExtensions -> {
                score += 60
                format = VideoFormat.fromExtension(ext)
            }

            mimeType != null && mimeType in videoMimeTypes -> {
                score += 55
                format = VideoFormat.fromMimeType(mimeType)
            }

            mediaPathPatterns.any { it.containsMatchIn(url) } -> {
                score += 30
                format = VideoFormat.Unknown
            }

            else -> return null
        }

        if (contentLength > 0) {
            when {
                contentLength > 5_000_000 -> score += 20
                contentLength > 1_000_000 -> score += 15
                contentLength > 500_000 -> score += 5
                else -> {
                    if (format != VideoFormat.HLS && format != VideoFormat.DASH) return null
                }
            }
        }
        if (url.contains("cdn", ignoreCase = true)) score += 5
        if (url.startsWith("https://")) score += 5

        return if (score >= 40) ScoredUrl(url, score, format, contentLength) else null
    }
}
