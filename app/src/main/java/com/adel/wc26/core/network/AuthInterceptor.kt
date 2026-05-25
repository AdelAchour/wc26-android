package com.adel.wc26.core.network

import com.adel.wc26.core.datastore.TokenStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Attaches the bearer token to outgoing requests when the user is signed in.
 *
 * The backend treats most read endpoints as optional-auth: a request works
 * without a token, but including one unlocks per-user data (e.g. the
 * likedByCurrentUser flag). Write endpoints require it. So we attach the
 * token whenever we have one and let the backend decide.
 *
 * runBlocking is acceptable here: OkHttp interceptors run on a background
 * thread from its dispatcher, and the DataStore read is a fast local lookup.
 */
@Singleton
class AuthInterceptor @Inject constructor(
    private val tokenStore: TokenStore,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val token = runBlocking { tokenStore.getToken() }

        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }

        return chain.proceed(request)
    }
}