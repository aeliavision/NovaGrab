package com.aeliavision.novagrab.core.preferences

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.preferencesDataStore
import com.aeliavision.novagrab.core.di.ApplicationScope
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "app_preferences")

class AppPreferences @Inject constructor(
    @param:ApplicationContext  private val context: Context,
    @ApplicationScope private val appScope: CoroutineScope,
) {
    @Volatile
    private var downloadPathCache: String? = null

    @Volatile
    private var downloadReadTimeoutMinutesCache: Int? = null

    @Volatile
    private var downloadConnectTimeoutSecondsCache: Int? = null

    init {
        appScope.launch {
            context.dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { it[PreferenceKeys.DOWNLOAD_PATH] }
                .collect { downloadPathCache = it }
        }

        appScope.launch {
            context.dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { it[PreferenceKeys.DOWNLOAD_READ_TIMEOUT_MINUTES] }
                .collect { downloadReadTimeoutMinutesCache = it }
        }

        appScope.launch {
            context.dataStore.data
                .catch { emit(emptyPreferences()) }
                .map { it[PreferenceKeys.DOWNLOAD_CONNECT_TIMEOUT_SECONDS] }
                .collect { downloadConnectTimeoutSecondsCache = it }
        }
    }

    val maxConcurrentDownloads: Flow<Int> = context.dataStore.data.map {
        it[PreferenceKeys.MAX_CONCURRENT_DOWNLOADS] ?: 3
    }

    val maxConcurrentDownloadsPerHost: Flow<Int> = context.dataStore.data.map {
        it[PreferenceKeys.MAX_CONCURRENT_DOWNLOADS_PER_HOST] ?: 1
    }

    val chunkCount: Flow<Int> = context.dataStore.data.map {
        it[PreferenceKeys.CHUNK_COUNT] ?: 4
    }

    val bufferSizeKb: Flow<Int> = context.dataStore.data.map {
        it[PreferenceKeys.BUFFER_SIZE_KB] ?: 8
    }

    val downloadReadTimeoutMinutes: Flow<Int> = context.dataStore.data.map {
        it[PreferenceKeys.DOWNLOAD_READ_TIMEOUT_MINUTES] ?: 60
    }

    val downloadConnectTimeoutSeconds: Flow<Int> = context.dataStore.data.map {
        it[PreferenceKeys.DOWNLOAD_CONNECT_TIMEOUT_SECONDS] ?: 30
    }

    val wifiOnlyDownload: Flow<Boolean> = context.dataStore.data.map {
        it[PreferenceKeys.WIFI_ONLY_DOWNLOAD] ?: false
    }

    val autoRetryOnReconnect: Flow<Boolean> = context.dataStore.data.map {
        it[PreferenceKeys.AUTO_RETRY_ON_RECONNECT] ?: true
    }

    val downloadPath: String?
        get() = downloadPathCache

    val downloadReadTimeoutMinutesValue: Int
        get() = downloadReadTimeoutMinutesCache ?: 60

    val downloadConnectTimeoutSecondsValue: Int
        get() = downloadConnectTimeoutSecondsCache ?: 30

    suspend fun setWifiOnlyDownload(enabled: Boolean) {
        context.dataStore.edit { it[PreferenceKeys.WIFI_ONLY_DOWNLOAD] = enabled }
    }

    suspend fun setDownloadPath(path: String?) {
        context.dataStore.edit {
            if (path == null) it.remove(PreferenceKeys.DOWNLOAD_PATH) else it[PreferenceKeys.DOWNLOAD_PATH] = path
        }
    }

    suspend fun setDownloadReadTimeoutMinutes(minutes: Int) {
        context.dataStore.edit { it[PreferenceKeys.DOWNLOAD_READ_TIMEOUT_MINUTES] = minutes }
    }

    suspend fun setDownloadConnectTimeoutSeconds(seconds: Int) {
        context.dataStore.edit { it[PreferenceKeys.DOWNLOAD_CONNECT_TIMEOUT_SECONDS] = seconds }
    }
}
