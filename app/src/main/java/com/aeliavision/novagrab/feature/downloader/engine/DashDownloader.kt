package com.aeliavision.novagrab.feature.downloader.engine

import android.net.Uri
import com.aeliavision.novagrab.core.common.Speedometer
import com.aeliavision.novagrab.core.storage.StorageManager
import com.aeliavision.novagrab.core.di.DownloadClient
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadProgress
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadTask
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

@Singleton
class DashDownloader @Inject constructor(
    @DownloadClient private val okHttpClient: OkHttpClient,
    private val storageManager: StorageManager,
    private val manifestParser: DashManifestParser,
    private val mergeWorker: HlsMergeWorker, // Reuse same FFmpeg merger for segments
    private val retryPolicy: RetryPolicy,
) {

    private fun validateResponseLooksLikeMedia(contentType: String) {
        DownloadRequestUtils.validateResponseLooksLikeMedia(contentType)
    }

    private fun buildEffectiveHeaders(task: DownloadTask): Map<String, String> {
        return DownloadRequestUtils.buildEffectiveHeaders(task)
    }

    suspend fun download(
        task: DownloadTask,
        onProgress: suspend (DownloadProgress) -> Unit,
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val manifest = manifestParser.parse(task.url).getOrElse { return@withContext Result.failure(it) }
            val effectiveHeaders = buildEffectiveHeaders(task)
            val taskWithHeaders = task.copy(headers = effectiveHeaders)
            
            // Choose the best representation for now
            val representation = manifest.variants.maxByOrNull { it.bandwidth } 
                ?: return@withContext Result.failure(Exception("No representations found in MPD"))

            val tempDir = storageManager.getTempDirForTask(task.id)
            val segmentDir = File(tempDir, "segments").also { it.mkdirs() }

            val totalSegments = representation.segments.size.coerceAtLeast(1)
            var downloadedBytesTotal = 0L
            val speedometer = Speedometer()

            representation.segments.forEachIndexed { index, seg ->
                currentCoroutineContext().ensureActive()
                val segFile = File(segmentDir, "seg_${index}.m4s") // DASH segments are usually m4s

                retryPolicy.withRetry {
                    val priorTotal = downloadedBytesTotal
                    try {
                        downloadSegment(seg.url, taskWithHeaders.headers, segFile) { bytes ->
                            downloadedBytesTotal += bytes
                            val speed = speedometer.update(downloadedBytesTotal)

                            onProgress(
                                DownloadProgress(
                                    taskId = taskWithHeaders.id,
                                    downloadedBytes = downloadedBytesTotal,
                                    totalBytes = -1L,
                                    percentage = (index * 100 / totalSegments).coerceIn(0, 100),
                                    speedBytesPerSec = speed,
                                )
                            )
                        }
                    } catch (e: Exception) {
                        downloadedBytesTotal = priorTotal
                        throw e
                    }
                }
            }

            mergeWorker.mergeSegments(
                taskId = taskWithHeaders.id,
                segmentDir = segmentDir,
                outputFileName = taskWithHeaders.fileName,
                onProgress = { },
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun downloadSegment(
        url: String, 
        headers: Map<String, String>, 
        outFile: File,
        onProgress: suspend (Long) -> Unit
    ): Long {
        val request = Request.Builder()
            .url(url)
            .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
            .build()

        return okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Unexpected response: ${response.code}")

            val contentType = response.header("Content-Type") ?: ""
            validateResponseLooksLikeMedia(contentType)

            val body = response.body ?: throw IOException("Empty body")

            var written = 0L
            body.source().use { source ->
                FileOutputStream(outFile, false).use { fos ->
                    val buffer = okio.Buffer()
                    while (source.read(buffer, 64 * 1024L) != -1L) {
                        val bytes = buffer.readByteArray()
                        fos.write(bytes)
                        written += bytes.size
                        onProgress(bytes.size.toLong())
                    }
                    fos.flush()
                    fos.fd.sync()
                }
            }
            written
        }
    }
}
