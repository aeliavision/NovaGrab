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
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import java.math.BigInteger

@Singleton
class HlsDownloader @Inject constructor(
    @DownloadClient private val okHttpClient: OkHttpClient,
    private val storageManager: StorageManager,
    private val manifestParser: HlsManifestParser,
    private val mergeWorker: HlsMergeWorker,
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
            val media = manifestParser.parseMedia(task.url).getOrElse { return@withContext Result.failure(it) }
            val effectiveHeaders = buildEffectiveHeaders(task)
            val taskWithHeaders = task.copy(headers = effectiveHeaders)

            val encryptionInfo = media.encryptionInfo
            val isEncrypted = encryptionInfo != null && !encryptionInfo.method.equals("NONE", ignoreCase = true)
            if (isEncrypted) {
                if (!encryptionInfo!!.method.equals("AES-128", ignoreCase = true)) {
                    return@withContext Result.failure(Exception("Unsupported HLS encryption: ${encryptionInfo.method}"))
                }
                if (encryptionInfo.keyUrl.isNullOrBlank()) {
                    return@withContext Result.failure(Exception("AES-128 key URL missing"))
                }
            }

            val encryptionKeyBytes = if (isEncrypted) {
                fetchAes128Key(encryptionInfo!!.keyUrl!!, taskWithHeaders.headers)
            } else {
                null
            }

            val tempDir = storageManager.getTempDirForTask(task.id)
            val segmentDir = File(tempDir, "segments").also { it.mkdirs() }

            val totalSegments = media.segments.size.coerceAtLeast(1)
            var downloadedBytesTotal = 0L
            val speedometer = Speedometer()

            media.segments.forEachIndexed { index, seg ->
                currentCoroutineContext().ensureActive()
                val segFile = File(segmentDir, "seg_${index}.ts")

                if (segFile.exists() && segFile.length() > 0) {
                    downloadedBytesTotal += segFile.length()
                    val speed = speedometer.update(downloadedBytesTotal)
                    val segmentProgress = index.toFloat() / totalSegments.toFloat()
                    onProgress(
                        DownloadProgress(
                            taskId = taskWithHeaders.id,
                            downloadedBytes = downloadedBytesTotal,
                            totalBytes = -1L,
                            percentage = (segmentProgress * 100f).toInt().coerceIn(0, 99),
                            speedBytesPerSec = speed,
                        )
                    )
                    return@forEachIndexed
                }

                retryPolicy.withRetry {
                    val priorTotal = downloadedBytesTotal
                    try {
                        downloadSegment(seg.url, taskWithHeaders.headers, segFile) { bytes ->
                            downloadedBytesTotal += bytes
                            val speed = speedometer.update(downloadedBytesTotal)

                            val segmentProgress = index.toFloat() / totalSegments.toFloat()
                            onProgress(
                                DownloadProgress(
                                    taskId = taskWithHeaders.id,
                                    downloadedBytes = downloadedBytesTotal,
                                    totalBytes = -1L, // Usually unknown for HLS
                                    percentage = (segmentProgress * 100f).toInt().coerceIn(0, 99),
                                    speedBytesPerSec = speed,
                                )
                            )
                        }

                        if (isEncrypted) {
                            val ivBytes = buildAes128Iv(
                                ivHex = encryptionInfo!!.iv,
                                segmentSequence = seg.sequence.toLong(),
                            )
                            decryptAes128(
                                file = segFile,
                                keyBytes = encryptionKeyBytes!!,
                                ivBytes = ivBytes,
                            )
                        }
                    } catch (e: Exception) {
                        downloadedBytesTotal = priorTotal
                        throw e
                    }
                }
            }

            val downloadedSegments = segmentDir.listFiles { f -> f.extension == "ts" }?.size ?: 0
            if (downloadedSegments < totalSegments) {
                return@withContext Result.failure(
                    Exception("Incomplete download: $downloadedSegments/$totalSegments segments")
                )
            }

            mergeWorker.mergeSegments(
                taskId = task.id,
                segmentDir = segmentDir,
                outputFileName = task.fileName,
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

    private fun buildAes128Iv(ivHex: String?, segmentSequence: Long): ByteArray {
        if (!ivHex.isNullOrBlank()) {
            val cleaned = ivHex.removePrefix("0x").removePrefix("0X")
            val bi = runCatching { BigInteger(cleaned, 16) }.getOrNull() ?: BigInteger.ZERO
            return bi.toByteArray().let { raw ->
                when {
                    raw.size == 16 -> raw
                    raw.size > 16 -> raw.copyOfRange(raw.size - 16, raw.size)
                    else -> ByteArray(16 - raw.size) + raw
                }
            }
        }

        val iv = ByteArray(16)
        var v = segmentSequence
        for (i in 15 downTo 8) {
            iv[i] = (v and 0xFF).toByte()
            v = v ushr 8
        }
        return iv
    }

    private suspend fun fetchAes128Key(url: String, headers: Map<String, String>): ByteArray {
        val request = Request.Builder()
            .url(url)
            .apply { headers.forEach { (k, v) -> addHeader(k, v) } }
            .build()

        val bytes = okHttpClient.newCall(request).execute().use { response ->
            if (!response.isSuccessful) throw IOException("Failed to fetch HLS key: HTTP ${response.code}")
            response.body?.bytes() ?: throw IOException("Empty body when fetching HLS key")
        }

        if (bytes.size != 16) {
            throw IOException("Invalid AES-128 key length: ${bytes.size} bytes")
        }
        return bytes
    }

    private fun decryptAes128(file: File, keyBytes: ByteArray, ivBytes: ByteArray) {
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        val keySpec = SecretKeySpec(keyBytes, "AES")
        val ivSpec = IvParameterSpec(ivBytes)
        cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec)

        val tempFile = File(file.parent, "${file.name}.decrypted.tmp")
        try {
            val encrypted = file.readBytes()
            val decrypted = cipher.doFinal(encrypted)
            FileOutputStream(tempFile, false).use { fos ->
                fos.write(decrypted)
                fos.flush()
                fos.fd.sync()
            }
            if (!tempFile.renameTo(file)) {
                throw IOException("Failed to rename decrypted file")
            }
        } finally {
            if (tempFile.exists()) tempFile.delete()
        }
    }
}
