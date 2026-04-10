package com.aeliavision.novagrab.core.preferences

import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey

object PreferenceKeys {
    val MAX_CONCURRENT_DOWNLOADS = intPreferencesKey("max_concurrent_downloads")
    val MAX_CONCURRENT_DOWNLOADS_PER_HOST = intPreferencesKey("max_concurrent_downloads_per_host")
    val CHUNK_COUNT = intPreferencesKey("chunk_count")
    val BUFFER_SIZE_KB = intPreferencesKey("buffer_size_kb")
    val DOWNLOAD_CONNECT_TIMEOUT_SECONDS = intPreferencesKey("download_connect_timeout_seconds")
    val DOWNLOAD_READ_TIMEOUT_MINUTES = intPreferencesKey("download_read_timeout_minutes")
    val WIFI_ONLY_DOWNLOAD = booleanPreferencesKey("wifi_only_download")
    val AUTO_RETRY_ON_RECONNECT = booleanPreferencesKey("auto_retry_on_reconnect")

    val DOWNLOAD_PATH = stringPreferencesKey("download_path")
}
