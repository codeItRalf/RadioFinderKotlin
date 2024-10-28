package app.codeitralf.radiofinder.network

import app.codeitralf.radiofinder.utils.AppInfo
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject

class UserAgentInterceptor @Inject constructor() : Interceptor {

    companion object {
        private const val HEADER_USER_AGENT = "User-Agent"
        private const val USER_AGENT_FORMAT = "%s/%s"
    }

    override fun intercept(chain: Interceptor.Chain): Response {
        val userAgent = USER_AGENT_FORMAT.format(AppInfo.appName, AppInfo.appVersion)

        val request = chain.request()
            .newBuilder()
            .header(HEADER_USER_AGENT, userAgent)
            .build()

        return chain.proceed(request)
    }
}