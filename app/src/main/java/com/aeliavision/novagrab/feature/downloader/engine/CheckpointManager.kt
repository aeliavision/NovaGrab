package com.aeliavision.novagrab.feature.downloader.engine

import android.content.Context
import com.aeliavision.novagrab.feature.downloader.domain.model.DownloadCheckpoint
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.File
import javax.inject.Inject
import kotlinx.serialization.json.Json
import kotlinx.serialization.Serializable

class CheckpointManager @Inject constructor(
    @param:ApplicationContext  private val context: Context,
) {

    private val json = Json { ignoreUnknownKeys = true }

    fun save(taskId: String, checkpoint: DownloadCheckpoint) {
        val file = fileForTask(taskId)
        file.parentFile?.mkdirs()
        file.writeText(json.encodeToString(CheckpointDto.serializer(), checkpoint.toDto()))
    }

    fun load(taskId: String): DownloadCheckpoint? {
        val file = fileForTask(taskId)
        if (!file.exists()) return null
        return runCatching {
            val dto = json.decodeFromString(CheckpointDto.serializer(), file.readText())
            dto.toDomain()
        }.getOrNull()
    }

    fun delete(taskId: String) {
        fileForTask(taskId).delete()
    }

    private fun fileForTask(taskId: String): File {
        return File(context.filesDir, "checkpoints/$taskId.json")
    }

    @Serializable
    private data class CheckpointDto(
        val bytesDownloaded: Long,
        val chunkOffsets: List<Long>,
    )

    private fun DownloadCheckpoint.toDto(): CheckpointDto = CheckpointDto(
        bytesDownloaded = bytesDownloaded,
        chunkOffsets = chunkOffsets,
    )

    private fun CheckpointDto.toDomain(): DownloadCheckpoint = DownloadCheckpoint(
        bytesDownloaded = bytesDownloaded,
        chunkOffsets = chunkOffsets,
    )
}
