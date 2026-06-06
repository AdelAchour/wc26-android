package com.adel.wc26.core.network

import com.adel.wc26.BuildConfig
import com.adel.wc26.core.datastore.TokenStore
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.Response
import java.nio.charset.Charset
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attaches the bearer token and X-App-Version to outgoing requests,
 * and intercepts response codes globally to handle auth session evictions,
 * force updates (426), and maintenance mode (503).
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
    private val appStatusManager: AppStatusManager,
    private val json: Json,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenStore.getToken() }

        // Inject the X-App-Version header to all outgoing requests
        val request = chain.request().newBuilder()
            .header("User-Agent", "WC26-Android/${BuildConfig.VERSION_NAME}")
            .addHeader("X-App-Version", BuildConfig.VERSION_CODE.toString())
            .apply {
                if (token != null) {
                    addHeader("Authorization", "Bearer $token")
                }
            }
            .build()

        val response = chain.proceed(request)

        // 1. Handle auth session evictions (401)
        if (response.code == 401 && token != null) {
            runBlocking {
                tokenStore.clear()
            }
        }

        // 2. Handle Force Update triggers (426)
        if (response.code == 426) {
            val responseBody = response.body
            val source = responseBody?.source()
            source?.request(Long.MAX_VALUE) // Buffer the entire body
            val buffer = source?.buffer
            val bodyString = buffer?.clone()?.readString(Charset.forName("UTF-8"))
            if (bodyString != null) {
                val updateError = runCatching {
                    json.decodeFromString<ForceUpdateErrorDto>(bodyString)
                }.getOrNull()
                val url = updateError?.androidUpdateUrl ?: "https://play.google.com/store/apps/details?id=com.adel.wc26"
                val minVersion = updateError?.minAndroidVersion ?: 1
                appStatusManager.updateStatus(AppStatus.ForceUpdate(updateUrl = url, minVersion = minVersion))
            }
        }

        // 3. Handle Maintenance Mode triggers (503)
        if (response.code == 503) {
            val responseBody = response.body
            val source = responseBody?.source()
            source?.request(Long.MAX_VALUE) // Buffer the entire body
            val buffer = source?.buffer
            val bodyString = buffer?.clone()?.readString(Charset.forName("UTF-8"))
            if (bodyString != null && bodyString.contains("\"maintenance_mode\":true")) {
                appStatusManager.updateStatus(AppStatus.Maintenance)
            }
        }

        return response
    }
}