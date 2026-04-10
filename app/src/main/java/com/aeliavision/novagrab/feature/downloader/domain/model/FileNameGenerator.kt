package com.aeliavision.novagrab.feature.downloader.domain.model

object FileNameGenerator {

    private val INVALID_CHARS = Regex("[\\\\/:*?\"<>|\\x00-\\x1F]")
    private const val MAX_LENGTH = 120

    /**
     * Build a filename from the video's title (or page title fallback).
     * Falls back to a short hash-based name if nothing useful.
     */
    fun generate(
        title: String?,
        extension: String,
        videoId: String,
    ): String {
        val base = title
            ?.let { sanitize(it) }
            ?.takeIf { it.isNotBlank() }
            ?: "download_${videoId.take(8)}"

        return "$base.$extension"
    }

    fun sanitize(raw: String): String {
        return raw
            .replace(INVALID_CHARS, "")
            .trim()
            .take(MAX_LENGTH)
    }
}
