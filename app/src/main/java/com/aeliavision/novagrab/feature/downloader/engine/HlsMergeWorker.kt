package com.aeliavision.novagrab.feature.downloader.engine

import android.net.Uri
import com.aeliavision.novagrab.core.storage.StorageManager
import com.arthenica.ffmpegkit.FFmpegKit
import com.arthenica.ffmpegkit.ReturnCode
import java.io.File
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class HlsMergeWorker @Inject constructor(
    private val storageManager: StorageManager,
) {

    suspend fun mergeSegments(
        taskId: String,
        segmentDir: File,
        outputFileName: String,
        onProgress: (Int) -> Unit,
    ): Result<Uri> = withContext(Dispatchers.IO) {

        val concatFile = File(segmentDir, "concat.txt")
        val segments = segmentDir.listFiles { f -> f.extension == "ts" || f.extension == "m4s" }
            ?.sortedBy { it.nameWithoutExtension.substringAfterLast('_').toIntOrNull() ?: 0 }
            ?: return@withContext Result.failure(Exception("No segments found"))

        val isDash = segments.any { it.extension == "m4s" }

        concatFile.writeText(
            segments.joinToString("\n") { "file '${it.absolutePath}'" }
        )

        val outputFile = File(segmentDir, "$outputFileName.mp4")
        if (outputFile.exists()) outputFile.delete()

        val inputArgsFast = if (isDash) {
            val joined = segments.joinToString("|") { it.absolutePath }
            "-i \"concat:$joined\""
        } else {
            "-f concat -safe 0 -i \"${concatFile.absolutePath}\""
        }

        val inputArgsRemux = if (isDash) {
            val joined = segments.joinToString("|") { it.absolutePath }
            "-i \"concat:$joined\""
        } else {
            "-f concat -safe 0 -i \"${concatFile.absolutePath}\""
        }

        val commandFast = "$inputArgsFast -c copy -movflags +faststart \"${outputFile.absolutePath}\""

        val commandRemux = "$inputArgsRemux -fflags +genpts -avoid_negative_ts make_zero -movflags +faststart " +
            "\"${outputFile.absolutePath}\""

        val session = FFmpegKit.execute(commandFast)
        val finalSession = if (ReturnCode.isSuccess(session.getReturnCode())) {
            session
        } else {
            FFmpegKit.execute(commandRemux)
        }

        return@withContext if (ReturnCode.isSuccess(finalSession.getReturnCode())) {
            val uri = storageManager.mergeChunksToMediaStore(
                chunkFiles = listOf(outputFile),
                fileName = "$outputFileName.mp4",
                mimeType = "video/mp4",
            )
            Result.success(uri)
        } else {
            Result.failure(Exception("FFmpeg failed: ${finalSession.getAllLogsAsString()}"))
        }
    }
}
