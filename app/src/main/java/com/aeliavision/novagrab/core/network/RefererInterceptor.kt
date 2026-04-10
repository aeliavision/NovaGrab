package com.aeliavision.novagrab.core.network

import okhttp3.Interceptor
import okhttp3.Response

class RefererInterceptor : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        if (request.header("Referer") != null) {
            return chain.proceed(request)
        }

        val url = request.url
        val origin = "${url.scheme}://${url.host}"

        val updated = request.newBuilder()
            .addHeader("Referer", origin)
            .build()

        return chain.proceed(updated)
    }
}
