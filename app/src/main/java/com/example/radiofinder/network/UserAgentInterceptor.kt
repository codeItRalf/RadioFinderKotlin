package com.example.radiofinder.network

import com.example.radiofinder.utils.AppInfo
import okhttp3.Interceptor
import okhttp3.Response

class UserAgentInterceptor : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val userAgent = "${AppInfo.appName}/${AppInfo.appVersion}"
        val newRequest = originalRequest.newBuilder()
            .header("User-Agent", userAgent)
            .build()
        return chain.proceed(newRequest)
    }
}