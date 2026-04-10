package com.aeliavision.novagrab.core.di

import android.content.Context
import com.aeliavision.novagrab.BuildConfig
import com.aeliavision.novagrab.core.network.NetworkMonitor
import com.aeliavision.novagrab.core.network.OkHttpProvider
import com.aeliavision.novagrab.core.network.RangeRequestInterceptor
import com.aeliavision.novagrab.core.network.UserAgentInterceptor
import com.aeliavision.novagrab.core.preferences.AppPreferences
import com.aeliavision.novagrab.feature.browser.presentation.webview.SmartUserAgent
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.io.File
import java.util.concurrent.TimeUnit

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideNetworkMonitor(
        @ApplicationContext  context: Context,
    ): NetworkMonitor = NetworkMonitor(context)

    @Provides
    @Singleton
    fun provideUserAgentInterceptor(): UserAgentInterceptor =
        UserAgentInterceptor(userAgent = SmartUserAgent.CHROME_LATEST)

    @Provides
    @Singleton
    fun provideRangeRequestInterceptor(): RangeRequestInterceptor = RangeRequestInterceptor()

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor()

    @Provides
    @Singleton
    fun provideOkHttpCache(@ApplicationContext  context: Context): Cache {
        val cacheSize = 50 * 1024 * 1024L // 50 MiB
        val cacheDir = File(context.cacheDir, "http_cache")
        return Cache(cacheDir, cacheSize)
    }

    @Provides
    @Singleton
    fun provideConnectionPool(): ConnectionPool {
        return ConnectionPool(10, 2, TimeUnit.MINUTES)
    }

    @Provides
    @Singleton
    fun provideOkHttpClient(
        userAgentInterceptor: UserAgentInterceptor,
        rangeRequestInterceptor: RangeRequestInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        cache: Cache,
        connectionPool: ConnectionPool,
    ): OkHttpClient = OkHttpProvider.create(
        userAgentInterceptor = userAgentInterceptor,
        rangeRequestInterceptor = rangeRequestInterceptor,
        loggingInterceptor = loggingInterceptor,
        enableLogging = BuildConfig.DEBUG,
        cache = cache,
        connectionPool = connectionPool,
    )

    @Provides
    @Singleton
    @DownloadClient
    fun provideDownloadOkHttpClient(
        userAgentInterceptor: UserAgentInterceptor,
        rangeRequestInterceptor: RangeRequestInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        appPreferences: AppPreferences,
    ): OkHttpClient {
        val downloadPool = ConnectionPool(20, 5, TimeUnit.MINUTES)
        return OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(rangeRequestInterceptor)
            .apply {
                if (BuildConfig.DEBUG) addInterceptor(loggingInterceptor)
                connectionPool(downloadPool)
                cache(null)
            }
            .connectTimeout(appPreferences.downloadConnectTimeoutSecondsValue.toLong(), TimeUnit.SECONDS)
            .readTimeout(appPreferences.downloadReadTimeoutMinutesValue.toLong(), TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            .build()
    }
}
