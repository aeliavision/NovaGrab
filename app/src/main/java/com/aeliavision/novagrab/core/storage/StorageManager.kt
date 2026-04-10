package com.aeliavision.novagrab.core.storage

import android.content.Context
import android.net.Uri
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import com.aeliavision.novagrab.core.preferences.AppPreferences

class StorageManager @Inject constructor(
    @param:ApplicationContext  private val context: Context,
    private val mediaStoreHelper: MediaStoreHelper,
    private val appPreferences: AppPreferences
) {
    private val baseDownloadDir: File
        get() {
            val customPath = appPreferences.downloadPath
            return if (customPath != null) {
                File(customPath)
            } else {
                context.getExternalFilesDir(Environment.DIRECTORY_MOVIES)
                    ?: context.filesDir
            }
        }

    fun getTempDirForTask(taskId: String): File {
        val base = context.getExternalFilesDir(null) ?: context.cacheDir
        return File(base, "downloads/tmp/$taskId").also { it.mkdirs() }
    }

    suspend fun deleteTempDirForTask(taskId: String) = withContext(Dispatchers.IO) {
        runCatching {
            getTempDirForTask(taskId).deleteRecursively()
        }
    }

    suspend fun cleanupStaleTempDirs(olderThanDays: Int = 7) = withContext(Dispatchers.IO) {
        val base = context.getExternalFilesDir(null) ?: context.cacheDir
        val tmpRoot = File(base, "downloads/tmp")
        if (!tmpRoot.exists()) return@withContext

        val cutoffMs = System.currentTimeMillis() - olderThanDays * 24L * 60L * 60L * 1000L
        tmpRoot.listFiles()?.forEach { dir ->
            if (!dir.isDirectory) return@forEach
            val last = dir.lastModified()
            if (last <= 0L || last < cutoffMs) {
                runCatching { dir.deleteRecursively() }
            }
        }
    }

    fun getChunkFile(taskId: String, chunkIndex: Int): File {
        return File(getTempDirForTask(taskId), "chunk_$chunkIndex.part")
    }

    suspend fun mergeChunksToMediaStore(
        chunkFiles: List<File>,
        fileName: String,
        mimeType: String
    ): Uri = withContext(Dispatchers.IO) {
        mediaStoreHelper.createVideoFile(fileName, mimeType) { outputStream ->
            chunkFiles.forEach { chunk ->
                chunk.inputStream().use { it.copyTo(outputStream) }
            }
        }
    }

    suspend fun deleteFile(uri: Uri): Boolean {
        return mediaStoreHelper.deleteFile(uri)
    }

    fun getAvailableSpaceBytes(): Long {
        val stat = StatFs(baseDownloadDir.absolutePath)
        return stat.availableBlocksLong * stat.blockSizeLong
    }
}
