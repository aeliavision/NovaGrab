package com.aeliavision.novagrab.feature.downloader.engine

import android.net.Uri
import androidx.media3.common.C
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.dash.manifest.DashManifestParser as Media3DashManifestParser
import androidx.media3.exoplayer.dash.manifest.Representation
import androidx.media3.exoplayer.dash.manifest.RangedUri
import androidx.media3.exoplayer.dash.DashSegmentIndex
import com.aeliavision.novagrab.core.di.DownloadClient
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.IOException

data class DashManifest(
    val variants: List<DashVariant>,
    val baseUrl: String,
)

data class DashVariant(
    val id: String?,
    val bandwidth: Int,
    val width: Int?,
    val height: Int?,
    val codecs: String?,
    val segments: List<DashSegment>,
) {
    val label: String
        get() = if (height != null) "${height}p" else "${bandwidth / 1000}kbps"
}

data class DashSegment(
    val url: String,
    val duration: Long,
)

@Singleton
class DashManifestParser @Inject constructor(
    @DownloadClient private val okHttpClient: OkHttpClient,
) {
    suspend fun parse(url: String): Result<DashManifest> = withContext(Dispatchers.IO) {
        try {
            val uri = Uri.parse(url)
            val mpdStream = fetchStream(url)
            val media3Manifest = Media3DashManifestParser().parse(uri, mpdStream)

            val periodIndex = 0
            val period = media3Manifest.getPeriod(periodIndex)
            val baseUrl = url.substringBeforeLast('/')

            val periodDurationUs = when {
                media3Manifest.durationMs != C.TIME_UNSET -> Util.msToUs(media3Manifest.durationMs)
                else -> media3Manifest.getPeriodDurationUs(periodIndex)
            }

            val variants = buildList {
                period.adaptationSets.forEach { adaptationSet ->
                    if (adaptationSet.type != C.TRACK_TYPE_VIDEO) return@forEach

                    adaptationSet.representations.forEach { rep ->
                        add(rep.toVariant(baseUrl, periodDurationUs))
                    }
                }
            }

            Result.success(DashManifest(variants = variants, baseUrl = baseUrl))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun Representation.toVariant(baseUrlFallback: String, periodDurationUs: Long): DashVariant {
        val format = format
        val resolvedBase = baseUrls.firstOrNull()?.url ?: baseUrlFallback

        val segments = buildSegments(
            representation = this,
            baseUrl = resolvedBase,
            periodDurationUs = periodDurationUs,
        )

        return DashVariant(
            id = format.id,
            bandwidth = format.bitrate.coerceAtLeast(0),
            width = if (format.width > 0) format.width else null,
            height = if (format.height > 0) format.height else null,
            codecs = format.codecs,
            segments = segments,
        )
    }

    private fun buildSegments(
        representation: Representation,
        baseUrl: String,
        periodDurationUs: Long,
    ): List<DashSegment> {
        val index: DashSegmentIndex = representation.getIndex()
            ?: throw IOException("DASH representation has no segment index")

        val out = ArrayList<DashSegment>(256)

        val initUri: RangedUri? = representation.getInitializationUri()
        if (initUri != null) {
            out.add(DashSegment(url = resolve(baseUrl, initUri), duration = 0L))
        }

        val first = index.getFirstSegmentNum()
        val durationUsForIndex = if (periodDurationUs == C.TIME_UNSET) Long.MAX_VALUE else periodDurationUs
        val count = index.getSegmentCount(durationUsForIndex)

        val shouldIterateUnbounded =
            periodDurationUs == C.TIME_UNSET ||
                count == DashSegmentIndex.INDEX_UNBOUNDED.toLong() ||
                count <= 0L

        if (shouldIterateUnbounded) {
            var segNum = first
            var safety = 0
            while (safety < 5000) {
                val timeUs = index.getTimeUs(segNum)
                if (timeUs == C.TIME_UNSET) break
                if (timeUs >= durationUsForIndex) break

                val ranged = index.getSegmentUrl(segNum)
                out.add(
                    DashSegment(
                        url = resolve(baseUrl, ranged),
                        duration = Util.usToMs(index.getDurationUs(segNum, durationUsForIndex)),
                    )
                )
                segNum++
                safety++
            }

            return out
        }

        val endExclusive = first + count
        var segNum = first
        while (segNum < endExclusive) {
            val ranged = index.getSegmentUrl(segNum)
            out.add(
                DashSegment(
                    url = resolve(baseUrl, ranged),
                    duration = Util.usToMs(index.getDurationUs(segNum, durationUsForIndex)),
                )
            )
            segNum++
        }

        return out
    }

    private fun resolve(baseUrl: String, rangedUri: RangedUri): String {
        return rangedUri.resolveUriString(baseUrl)
    }

    private fun fetchStream(url: String) = run {
        val request = Request.Builder().url(url).build()
        val response = okHttpClient.newCall(request).execute()
        val body = response.body ?: run {
            response.close()
            throw IOException("Empty body")
        }

        // The caller (Media3 parser) will read the stream. We'll close response when stream closes.
        body.byteStream().also {
            // OkHttp will close response when body is closed.
        }
    }
}
