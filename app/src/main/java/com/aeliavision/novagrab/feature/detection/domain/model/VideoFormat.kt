package com.aeliavision.novagrab.feature.detection.domain.model

sealed class VideoFormat(val extension: String, val mimeType: String) {
    data object MP4 : VideoFormat("mp4", "video/mp4")
    data object WebM : VideoFormat("webm", "video/webm")
    data object MOV : VideoFormat("mov", "video/quicktime")
    data object AVI : VideoFormat("avi", "video/x-msvideo")
    data object MKV : VideoFormat("mkv", "video/x-matroska")
    data object HLS : VideoFormat("m3u8", "application/x-mpegurl")
    data object DASH : VideoFormat("mpd", "application/dash+xml")
    data object TS : VideoFormat("ts", "video/mp2t")
    data object FLV : VideoFormat("flv", "video/x-flv")
    data object Unknown : VideoFormat("", "application/octet-stream")

    companion object {
        fun fromExtension(ext: String): VideoFormat = when (ext.lowercase()) {
            "mp4", "m4v" -> MP4
            "webm" -> WebM
            "mov" -> MOV
            "avi" -> AVI
            "mkv" -> MKV
            "m3u8" -> HLS
            "mpd" -> DASH
            "ts" -> TS
            "flv" -> FLV
            else -> Unknown
        }

        fun fromMimeType(mime: String): VideoFormat = when {
            mime.contains("mp4") -> MP4
            mime.contains("webm") -> WebM
            mime.contains("quicktime") -> MOV
            mime.contains("x-msvideo") -> AVI
            mime.contains("matroska") -> MKV
            mime.contains("mpegurl") -> HLS
            mime.contains("dash") -> DASH
            mime.contains("mp2t") -> TS
            else -> Unknown
        }
    }
}
