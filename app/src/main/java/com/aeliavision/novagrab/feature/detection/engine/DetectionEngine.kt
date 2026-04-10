package com.aeliavision.novagrab.feature.detection.engine

import com.aeliavision.novagrab.core.di.ApplicationScope
import com.aeliavision.novagrab.feature.browser.domain.tabs.BrowserTabStore
import com.aeliavision.novagrab.feature.detection.data.dao.DetectedVideoDao
import com.aeliavision.novagrab.feature.detection.data.entity.DetectedVideoEntity
import com.aeliavision.novagrab.feature.detection.domain.model.DetectedVideo
import com.aeliavision.novagrab.feature.detection.domain.model.VideoFormat
import android.net.Uri
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.util.concurrent.ConcurrentHashMap
import java.util.UUID

@Singleton
class DetectionEngine @Inject constructor(
    @param:ApplicationScope private val scope: CoroutineScope,
    private val browserTabStore: BrowserTabStore,
    private val networkInterceptorDetector: NetworkInterceptorDetector,
    private val jsInjectionDetector: JsInjectionDetector,
    private val hlsManifestDetector: HlsManifestDetector,
    private val blobUrlResolver: BlobUrlResolver,
    private val detectedVideoDao: DetectedVideoDao,
) {

    data class LongPressDownloadEvent(
        val url: String,
        val tabId: String,
    )

    private val detectionChannel = Channel<VideoDetectionEvent>(Channel.UNLIMITED)

    private val _detectionsFlowByTab = ConcurrentHashMap<String, StateFlow<List<DetectedVideo>>>()

    private val _longPressDownloadEvents = MutableSharedFlow<LongPressDownloadEvent>(extraBufferCapacity = 16)
    val longPressDownloadEvents: SharedFlow<LongPressDownloadEvent> = _longPressDownloadEvents.asSharedFlow()

    init {
        scope.launch {
            detectionChannel.receiveAsFlow()
                .onEach { event -> processDetection(event) }
                .catch { }
                .collect()
        }
    }

    fun detectionsForTab(tabId: String): StateFlow<List<DetectedVideo>> {
        return _detectionsFlowByTab.getOrPut(tabId) {
            detectedVideoDao.getByTabAsFlow(tabId)
                .map { entities -> entities.map { it.toDomain() } }
                .stateIn(
                    scope = scope,
                    started = SharingStarted.WhileSubscribed(5000),
                    initialValue = emptyList()
                )
        }
    }

    fun clearPageDetections(tabId: String) {
        scope.launch {
            detectedVideoDao.clearTab(tabId)
        }
    }

    fun analyzeNetworkRequest(url: String, headers: Map<String, String>, tabId: String) {
        val detection = networkInterceptorDetector.detect(url, headers) ?: return
        val pageUrl = currentPageUrl(tabId)

        submitDetection(
            VideoDetectionEvent.VideoFound(
                url = detection.scoredUrl.url,
                mimeType = detection.scoredUrl.format.mimeType,
                width = 0,
                height = 0,
                tabId = tabId,
                source = DetectionSource.NETWORK_INTERCEPT,
                pageUrl = pageUrl,
                capturedHeaders = detection.capturedHeaders,
            )
        )
    }

    fun onJsVideoDetected(
        url: String,
        mimeType: String,
        width: Int,
        height: Int,
        tabId: String,
        title: String? = null,
    ) {
        val pageUrl = currentPageUrl(tabId)
        submitDetection(
            VideoDetectionEvent.VideoFound(
                url = url,
                mimeType = mimeType,
                width = width,
                height = height,
                tabId = tabId,
                source = DetectionSource.JS_INJECTION,
                pageUrl = pageUrl,
                title = title,
            )
        )
    }

    fun submitDetection(event: VideoDetectionEvent) {
        detectionChannel.trySend(event)
    }

    fun triggerLongPressDownload(url: String, tabId: String) {
        scope.launch {
            val pageUrl = currentPageUrl(tabId)
            _longPressDownloadEvents.emit(LongPressDownloadEvent(url = url, tabId = tabId))
            submitDetection(
                VideoDetectionEvent.VideoFound(
                    url = url,
                    mimeType = "application/octet-stream",
                    width = 0,
                    height = 0,
                    tabId = tabId,
                    source = DetectionSource.JS_INJECTION,
                    pageUrl = pageUrl,
                )
            )
        }
    }

    fun onMseSourceBuffer(mimeType: String, tabId: String) {
        scope.launch {
            val pageUrl = currentPageUrl(tabId)
            val url = "mse:$mimeType"
            val id = stableDetectionId(tabId, url)
            detectedVideoDao.insert(
                DetectedVideoEntity(
                    id = id,
                    pageUrl = pageUrl,
                    videoUrl = url,
                    format = VideoFormat.Unknown,
                    mimeType = mimeType.ifBlank { "application/octet-stream" },
                    requestHeaders = emptyMap(),
                    estimatedSizeBytes = -1L,
                    width = 0,
                    height = 0,
                    detectedAt = System.currentTimeMillis(),
                    tabId = tabId,
                    confidence = 10,
                    title = "MSE stream",
                    thumbnailUrl = null,
                )
            )
        }
    }

    fun analyzeEmbedUrl(embedUrl: String, tabId: String) {
        scope.launch {
            val uri = runCatching { Uri.parse(embedUrl) }.getOrNull() ?: return@launch
            val pageUrl = currentPageUrl(tabId)
            val candidates = listOf("url", "src", "video", "stream", "redirect", "u")
                .mapNotNull { key -> uri.getQueryParameter(key) }

            val nested = candidates.firstOrNull { it.startsWith("https://") || it.startsWith("http://") }
                ?: return@launch

            val looksLikeMedia = nested.contains(".m3u8", ignoreCase = true) ||
                nested.contains(".mpd", ignoreCase = true) ||
                nested.contains(".mp4", ignoreCase = true) ||
                nested.contains(".ts", ignoreCase = true)

            if (!looksLikeMedia) return@launch

            submitDetection(
                VideoDetectionEvent.VideoFound(
                    url = nested,
                    mimeType = "application/octet-stream",
                    width = 0,
                    height = 0,
                    tabId = tabId,
                    source = DetectionSource.JS_INJECTION,
                    pageUrl = pageUrl,
                )
            )
        }
    }

    fun onBlobUrlDetected(
        url: String,
        mimeType: String,
        width: Int,
        height: Int,
        tabId: String,
    ) {
        val pageUrl = currentPageUrl(tabId)
        submitDetection(
            VideoDetectionEvent.BlobUrlDetected(
                url = url,
                mimeType = mimeType,
                width = width,
                height = height,
                tabId = tabId,
                source = DetectionSource.JS_INJECTION,
                pageUrl = pageUrl,
            )
        )
    }

    private suspend fun processDetection(event: VideoDetectionEvent) {
        when (event) {
            is VideoDetectionEvent.VideoFound -> {
                val scored = jsInjectionDetector.detect(
                    url = event.url,
                    mimeType = event.mimeType,
                    width = event.width,
                    height = event.height,
                ) ?: return

                val pageUrl = event.pageUrl.ifBlank { currentPageUrl(event.tabId) }
                val id = stableDetectionId(event.tabId, scored.url)

                val detection = DetectedVideo(
                    id = id,
                    pageUrl = pageUrl,
                    videoUrl = scored.url,
                    format = scored.format,
                    mimeType = event.mimeType,
                    requestHeaders = event.capturedHeaders,
                    estimatedSizeBytes = scored.estimatedSize,
                    width = event.width,
                    height = event.height,
                    tabId = event.tabId,
                    confidence = scored.score,
                    detectedAt = System.currentTimeMillis(),
                    title = event.title
                )

                detectedVideoDao.insert(detection.toEntity())
            }

            is VideoDetectionEvent.BlobUrlDetected -> {
                if (!blobUrlResolver.canResolve(event.url)) return

                val pageUrl = event.pageUrl.ifBlank { currentPageUrl(event.tabId) }
                val id = stableDetectionId(event.tabId, event.url)

                val detection = DetectedVideo(
                    id = id,
                    pageUrl = pageUrl,
                    videoUrl = event.url,
                    format = VideoFormat.Unknown,
                    mimeType = event.mimeType.ifBlank { "application/octet-stream" },
                    estimatedSizeBytes = -1L,
                    width = event.width,
                    height = event.height,
                    tabId = event.tabId,
                    confidence = 0,
                    detectedAt = System.currentTimeMillis()
                )

                detectedVideoDao.insert(detection.toEntity())
            }
            else -> Unit
        }
    }

    private fun currentPageUrl(tabId: String): String {
        val state = browserTabStore.state.value
        return state.tabs.firstOrNull { it.id == tabId }?.url ?: ""
    }

    private fun stableDetectionId(tabId: String, url: String): String {
        val normalized = normalizeUrl(url)
        return UUID.nameUUIDFromBytes("$tabId|$normalized".toByteArray()).toString()
    }

    private fun normalizeUrl(url: String): String {
        return try {
            val uri = Uri.parse(url)
            val essentialParams = setOf("v", "id", "quality", "format", "itag")
            val builder = uri.buildUpon().clearQuery()

            uri.queryParameterNames
                .filter { it.lowercase() in essentialParams }
                .forEach { param ->
                    builder.appendQueryParameter(param, uri.getQueryParameter(param))
                }

            builder.build().toString()
        } catch (_: Exception) {
            url.substringBefore('?')
        }
    }

    private fun DetectedVideoEntity.toDomain(): DetectedVideo {
        return DetectedVideo(
            id = id,
            pageUrl = pageUrl,
            videoUrl = videoUrl,
            format = format,
            mimeType = mimeType,
            requestHeaders = requestHeaders,
            estimatedSizeBytes = estimatedSizeBytes,
            width = width,
            height = height,
            detectedAt = detectedAt,
            tabId = tabId,
            confidence = confidence,
            title = title,
            thumbnailUrl = thumbnailUrl
        )
    }

    private fun DetectedVideo.toEntity(): DetectedVideoEntity {
        return DetectedVideoEntity(
            id = id,
            pageUrl = pageUrl,
            videoUrl = videoUrl,
            format = format,
            mimeType = mimeType,
            requestHeaders = requestHeaders,
            estimatedSizeBytes = estimatedSizeBytes,
            width = width,
            height = height,
            detectedAt = detectedAt,
            tabId = tabId,
            confidence = confidence,
            title = title,
            thumbnailUrl = thumbnailUrl
        )
    }
}
