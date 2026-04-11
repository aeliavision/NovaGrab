package com.aeliavision.novagrab.feature.downloader.engine

import android.net.Uri
import com.aeliavision.novagrab.core.common.Speedometer
import com.aeliavision.novagrab.core.preferences.AppPreferences
import com.aeliavision.novagrab.core.storage.StorageManager
import com.aeliavision.novagrab.core.di.DownloadClient
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadCheckpoint
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadProgress
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadTask
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.concurrent.atomic.AtomicLong
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.withContext
import kotlinx.coroutines.flow.first
import okhttp3.OkHttpClient
import okhttp3.Request
import okio.Buffer

@Singleton
class HttpRangeDownloader @Inject constructor(
    @DownloadClient private val okHttpClient: OkHttpClient,
    private val storageManager: StorageManager,
    private val checkpointManager: CheckpointManager,
    private val retryPolicy: RetryPolicy,
    private val appPreferences: AppPreferences,
) {

    companion object {
        private const val MIN_VIDEO_SIZE_BYTES = 10_000L
        private const val RANGE_NOT_SUPPORTED_MARKER = "RANGE_NOT_SUPPORTED"
    }

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
        val chunkCount = appPreferences.chunkCount.first().coerceIn(1, 16)
        val bufferSizeBytes = (appPreferences.bufferSizeKb.first().coerceIn(4, 1024) * 1024)
        val totalBytes = task.totalSizeBytes
        val effectiveHeaders = buildEffectiveHeaders(task)
        val taskWithHeaders = task.copy(headers = effectiveHeaders)

        if (totalBytes <= 0) {
            return@withContext downloadSequential(taskWithHeaders, bufferSizeBytes, onProgress)
        }

        val checkpoint: DownloadCheckpoint? = checkpointManager.load(task.id)
        var tempDir = storageManager.getTempDirForTask(task.id)
        if (checkpoint == null) {
            val existingFiles = tempDir.listFiles()
            if (!existingFiles.isNullOrEmpty()) {
                storageManager.deleteTempDirForTask(task.id)
                tempDir = storageManager.getTempDirForTask(task.id)
            }
        }

        val chunkRanges = splitIntoChunks(totalBytes, chunkCount)
            .map { range ->
                val chunkFile = File(tempDir, "chunk_${range.index}.part")
                val actualFileSize = if (chunkFile.exists()) chunkFile.length() else 0L
                val maxForChunk = (range.endByte - range.startByte + 1)
                val downloaded = actualFileSize.coerceAtMost(maxForChunk)
                ChunkRange(
                    index = range.index,
                    startByte = range.startByte,
                    endByte = range.endByte,
                    downloadedBytes = downloaded,
                )
            }

        val downloadedTotal = AtomicLong(chunkRanges.sumOf { it.downloadedBytes })
        val lastCheckpointSavedAt = AtomicLong(downloadedTotal.get())
        val speedometer = Speedometer()

        val chunkDownloaded = java.util.concurrent.atomic.AtomicLongArray(chunkCount)
        chunkRanges.forEach { chunk ->
            chunkDownloaded.set(chunk.index, chunk.downloadedBytes)
        }

        val attemptedChunks = chunkRanges.count { it.downloadedBytes < it.size }

        val results = coroutineScope {
            chunkRanges
                .filter { it.downloadedBytes < it.size }
                .map { chunk ->
                    async {
                        runCatching {
                            retryPolicy.withRetry {
                                downloadChunk(
                                    url = taskWithHeaders.url,
                                    headers = taskWithHeaders.headers,
                                    chunk = chunk,
                                    tempDir = tempDir,
                                    bufferSizeBytes = bufferSizeBytes,
                                    onProgress = { bytesRead ->
                                        if (bytesRead == 0L) return@downloadChunk
                                        val current = downloadedTotal.addAndGet(bytesRead)
                                        chunkDownloaded.addAndGet(chunk.index, bytesRead)
                                        val speed = speedometer.update(current)

                                        onProgress(
                                            DownloadProgress(
                                                taskId = taskWithHeaders.id,
                                                downloadedBytes = current,
                                                totalBytes = totalBytes,
                                                percentage = (current * 100 / totalBytes).toInt(),
                                                speedBytesPerSec = speed,
                                            )
                                        )

                                        val snapshotOffsets = (0 until chunkCount).map { i -> chunkDownloaded.get(i) }
                                        val snapshotTotal = snapshotOffsets.sum()
                                        val lastSaved = lastCheckpointSavedAt.get()

                                        if (snapshotTotal - lastSaved >= (1024 * 1024)) {
                                            if (lastCheckpointSavedAt.compareAndSet(lastSaved, snapshotTotal)) {
                                                checkpointManager.save(
                                                    taskWithHeaders.id,
                                                    DownloadCheckpoint(
                                                        bytesDownloaded = snapshotTotal,
                                                        chunkOffsets = snapshotOffsets,
                                                    ),
                                                )
                                            }
                                        }
                                    },
                                )
                            }
                        }
                    }
                }
                .awaitAll()
        }

        if (results.any { it.isFailure }) {
            val allRangeNotSupported = attemptedChunks > 0 && results.all { result ->
                result.exceptionOrNull() is RangeNotSupportedException
            }

            if (allRangeNotSupported) {
                return@withContext downloadSequential(taskWithHeaders, bufferSizeBytes, onProgress)
            }

            return@withContext Result.failure(Exception("One or more chunks failed"))
        }

        val finalBytes = downloadedTotal.get()
        if (finalBytes < MIN_VIDEO_SIZE_BYTES) {
            return@withContext Result.failure(
                IOException("Downloaded only $finalBytes bytes — likely blocked by anti-hotlinking")
            )
        }

        val finalUri = storageManager.mergeChunksToMediaStore(
            chunkFiles = chunkRanges.map { storageManager.getChunkFile(taskWithHeaders.id, it.index) },
            fileName = taskWithHeaders.fileName,
            mimeType = taskWithHeaders.mimeType,
        )

        checkpointManager.delete(taskWithHeaders.id)
        Result.success(finalUri)
    }

    private suspend fun downloadSequential(
        task: DownloadTask,
        bufferSizeBytes: Int,
        onProgress: suspend (DownloadProgress) -> Unit,
    ): Result<Uri> = withContext(Dispatchers.IO) {
        try {
            val tempDir = storageManager.getTempDirForTask(task.id)
            val outFile = File(tempDir, "single.part")

            var downloaded = outFile.length().takeIf { outFile.exists() } ?: 0L
            val speedometer = Speedometer()

            retryPolicy.withRetry {
                val request = Request.Builder()
                    .url(task.url)
                    .apply {
                        task.headers.forEach { (k, v) -> addHeader(k, v) }
                        if (downloaded > 0) addHeader("Range", "bytes=$downloaded-")
                    }
                    .build()

                okHttpClient.newCall(request).execute().use { response ->
                    if (!response.isSuccessful && response.code != 206) {
                        throw IOException("Unexpected response: ${response.code}")
                    }

                    if (downloaded > 0 && response.code != 206) {
                        downloaded = 0L
                    }

                    val contentType = response.header("Content-Type") ?: ""
                    validateResponseLooksLikeMedia(contentType)

                    val total = response.body?.contentLength()?.let { cl ->
                        if (response.code == 206) downloaded + cl else cl
                    } ?: -1L

                    response.body?.source()?.use { source ->
                        FileOutputStream(outFile, downloaded > 0).use { fos ->
                            val buffer = Buffer()
                            while (source.read(buffer, bufferSizeBytes.toLong()) != -1L) {
                                currentCoroutineContext().ensureActive()
                                val bytes = buffer.readByteArray()
                                fos.write(bytes)
                                downloaded += bytes.size
                                val speed = speedometer.update(downloaded)
                                val pct = if (total > 0) ((downloaded * 100) / total).toInt() else 0
                                onProgress(
                                    DownloadProgress(
                                        taskId = task.id,
                                        downloadedBytes = downloaded,
                                        totalBytes = total,
                                        percentage = pct,
                                        speedBytesPerSec = speed,
                                    )
                                )
                            }
                            fos.flush()
                            fos.fd.sync()
                        }
                    }
                }
            }

            if (downloaded < MIN_VIDEO_SIZE_BYTES) {
                return@withContext Result.failure(
                    IOException("Downloaded only $downloaded bytes — likely blocked by anti-hotlinking")
                )
            }

            val uri = storageManager.mergeChunksToMediaStore(
                chunkFiles = listOf(outFile),
                fileName = task.fileName,
                mimeType = task.mimeType,
            )
            Result.success(uri)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun downloadChunk(
        url: String,
        headers: Map<String, String>,
        chunk: ChunkRange,
        tempDir: File,
        bufferSizeBytes: Int,
        onProgress: suspend (Long) -> Unit,
    ) {
        if (chunk.downloadedBytes >= chunk.size) {
            onProgress(0L)
            return
        }

        val resumeStart = chunk.startByte + chunk.downloadedBytes
        val request = Request.Builder()
            .url(url)
            .addHeader("Range", "bytes=$resumeStart-${chunk.endByte}")
            .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
            .build()

        val chunkFile = File(tempDir, "chunk_${chunk.index}.part")

        okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw IOException("Unexpected response: ${response.code}")
            }

            if (response.code != 206 && response.header("Content-Range").isNullOrBlank()) {
                throw RangeNotSupportedException(
                    "$RANGE_NOT_SUPPORTED_MARKER: Server did not honor Range request (bytes=$resumeStart-${chunk.endByte}). " +
                        "Expected 206 Partial Content but got ${response.code}."
                )
            }

            val contentType = response.header("Content-Type") ?: ""
            validateResponseLooksLikeMedia(contentType)

            response.body?.source()?.use { source ->
                FileOutputStream(chunkFile, chunk.downloadedBytes > 0).use { fos ->
                    val buffer = Buffer()
                    while (source.read(buffer, bufferSizeBytes.toLong()) != -1L) {
                        currentCoroutineContext().ensureActive()
                        val bytes = buffer.readByteArray()
                        fos.write(bytes)
                        chunk.downloadedBytes += bytes.size
                        onProgress(bytes.size.toLong())
                    }
                    fos.flush()
                    fos.fd.sync()
                }
            }
        }
    }

    private fun splitIntoChunks(totalBytes: Long, chunkCount: Int): List<ChunkRange> {
        if (totalBytes <= 0) return emptyList()

        val chunkSize = (totalBytes / chunkCount).coerceAtLeast(1L)
        return (0 until chunkCount).map { index ->
            val start = index * chunkSize
            val end = if (index == chunkCount - 1) totalBytes - 1 else (start + chunkSize - 1)
            ChunkRange(
                index = index,
                startByte = start,
                endByte = end,
                downloadedBytes = 0L,
            )
        }
    }

    private class ChunkRange(
        val index: Int,
        val startByte: Long,
        val endByte: Long,
        var downloadedBytes: Long,
    ) {
        val size: Long get() = (endByte - startByte + 1)
    }
}
