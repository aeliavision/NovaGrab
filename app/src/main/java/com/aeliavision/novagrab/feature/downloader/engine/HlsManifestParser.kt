package com.aeliavision.novagrab.feature.downloader.engine

import com.aeliavision.novagrab.core.di.DownloadClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

data class HlsMasterManifest(
    val variants: List<HlsVariant>,
    val baseUrl: String,
)

data class HlsVariant(
    val url: String,
    val bandwidth: Int,
    val resolution: String?,
    val codecs: String?,
    val framerate: Float?,
) {
    val label: String
        get() = resolution?.let { res ->
            val height = res.split("x").lastOrNull()?.toIntOrNull()
            when {
                height != null && height >= 2160 -> "4K (${height}p)"
                height != null && height >= 1080 -> "Full HD (${height}p)"
                height != null && height >= 720 -> "HD (${height}p)"
                height != null && height >= 480 -> "SD (${height}p)"
                else -> "${height}p"
            }
        } ?: "${bandwidth / 1000}kbps"
}

data class HlsMediaManifest(
    val segments: List<HlsSegment>,
    val targetDuration: Int,
    val totalDuration: Float,
    val isEndless: Boolean,
    val encryptionInfo: HlsEncryptionInfo?,
)

data class HlsSegment(
    val url: String,
    val duration: Float,
    val sequence: Int,
)

data class HlsEncryptionInfo(
    val method: String,
    val keyUrl: String?,
    val iv: String?,
)

@Singleton
class HlsManifestParser @Inject constructor(
    @DownloadClient private val okHttpClient: OkHttpClient,
) {

    suspend fun parseMaster(url: String): Result<HlsMasterManifest> = withContext(Dispatchers.IO) {
        try {
            val content = fetchContent(url)
            val baseUrl = url.substringBeforeLast('/')

            if (!content.contains("#EXTM3U")) {
                return@withContext Result.failure(Exception("Not a valid M3U8 file"))
            }

            if (!content.contains("#EXT-X-STREAM-INF")) {
                val variant = HlsVariant(
                    url = url,
                    bandwidth = 0,
                    resolution = null,
                    codecs = null,
                    framerate = null,
                )
                return@withContext Result.success(HlsMasterManifest(listOf(variant), baseUrl))
            }

            val variants = mutableListOf<HlsVariant>()
            val lines = content.lines()
            var i = 0
            while (i < lines.size) {
                val line = lines[i].trim()
                if (line.startsWith("#EXT-X-STREAM-INF:")) {
                    val attrs = parseAttributes(line.substringAfter(':'))
                    val variantUrl = lines.getOrNull(i + 1)?.trim() ?: ""
                    val resolvedUrl = resolveUrl(baseUrl, variantUrl)

                    variants.add(
                        HlsVariant(
                            url = resolvedUrl,
                            bandwidth = attrs["BANDWIDTH"]?.toIntOrNull() ?: 0,
                            resolution = attrs["RESOLUTION"],
                            codecs = attrs["CODECS"],
                            framerate = attrs["FRAME-RATE"]?.toFloatOrNull(),
                        )
                    )

                    i += 2
                    continue
                }
                i++
            }

            Result.success(
                HlsMasterManifest(
                    variants = variants.sortedByDescending { it.bandwidth },
                    baseUrl = baseUrl,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun parseMedia(url: String): Result<HlsMediaManifest> = withContext(Dispatchers.IO) {
        try {
            val content = fetchContent(url)
            val baseUrl = url.substringBeforeLast('/')
            val lines = content.lines()

            val segments = mutableListOf<HlsSegment>()
            var targetDuration = 0
            var totalDuration = 0f
            var encryptionInfo: HlsEncryptionInfo? = null
            var sequence = 0
            var currentDuration = 0f

            var i = 0
            while (i < lines.size) {
                val line = lines[i].trim()
                when {
                    line.startsWith("#EXT-X-TARGETDURATION:") -> {
                        targetDuration = line.substringAfter(':').toIntOrNull() ?: 0
                    }

                    line.startsWith("#EXT-X-MEDIA-SEQUENCE:") -> {
                        sequence = line.substringAfter(':').toIntOrNull() ?: 0
                    }

                    line.startsWith("#EXTINF:") -> {
                        currentDuration = line.substringAfter(':')
                            .substringBefore(',')
                            .toFloatOrNull() ?: 0f
                    }

                    line.startsWith("#EXT-X-KEY:") -> {
                        val attrs = parseAttributes(line.substringAfter(':'))
                        val rawUri = attrs["URI"]?.trim('"')
                        val resolvedKeyUrl = rawUri?.let { resolveUrl(baseUrl, it) }
                        encryptionInfo = HlsEncryptionInfo(
                            method = attrs["METHOD"] ?: "NONE",
                            keyUrl = resolvedKeyUrl,
                            iv = attrs["IV"],
                        )
                    }

                    !line.startsWith('#') && line.isNotEmpty() -> {
                        val segUrl = resolveUrl(baseUrl, line)
                        segments.add(
                            HlsSegment(
                                url = segUrl,
                                duration = currentDuration,
                                sequence = sequence++,
                            )
                        )
                        totalDuration += currentDuration
                        currentDuration = 0f
                    }
                }
                i++
            }

            val isEndless = !content.contains("#EXT-X-ENDLIST")

            Result.success(
                HlsMediaManifest(
                    segments = segments,
                    targetDuration = targetDuration,
                    totalDuration = totalDuration,
                    isEndless = isEndless,
                    encryptionInfo = encryptionInfo,
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseAttributes(attrs: String): Map<String, String> {
        val map = mutableMapOf<String, String>()
        val regex = Regex("""(\w[\w-]*)=(\"(?:[^\"]*)\"|[^,]*)""")
        regex.findAll(attrs).forEach { match ->
            map[match.groupValues[1]] = match.groupValues[2].trim('"')
        }
        return map
    }

    private fun resolveUrl(base: String, url: String): String {
        if (url.startsWith("http")) return url
        if (url.startsWith("//")) return "https:$url"
        if (url.startsWith("/")) {
            val origin = base.split("/").take(3).joinToString("/")
            return "$origin$url"
        }
        return "$base/$url"
    }

    private suspend fun fetchContent(url: String): String {
        val request = Request.Builder().url(url).build()
        return okHttpClient.newCall(request).execute().use {
            it.body?.string() ?: throw IOException("Empty body")
        }
    }
}
