package com.aeliavision.novagrab.core.network

import okhttp3.Cache
import okhttp3.ConnectionPool
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import java.util.concurrent.TimeUnit

object OkHttpProvider {
    fun create(
        userAgentInterceptor: UserAgentInterceptor,
        rangeRequestInterceptor: RangeRequestInterceptor,
        loggingInterceptor: HttpLoggingInterceptor,
        enableLogging: Boolean,
        cache: Cache? = null,
        connectionPool: ConnectionPool? = null,
    ): OkHttpClient {
        val builder = OkHttpClient.Builder()
            .addInterceptor(userAgentInterceptor)
            .addInterceptor(rangeRequestInterceptor)
            .apply {
                if (enableLogging) addInterceptor(loggingInterceptor)
                if (cache != null) cache(cache)
                if (connectionPool != null) connectionPool(connectionPool)
            }
            .connectTimeout(15, TimeUnit.SECONDS)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(30, TimeUnit.SECONDS)
            .followRedirects(true)
            .followSslRedirects(true)
            
        return builder.build()
    }
}
