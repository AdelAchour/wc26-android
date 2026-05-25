package com.adel.wc26.feature.auth.data

import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.network.apiCall
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.core.result.map
import com.adel.wc26.feature.auth.data.dto.LoginRequest
import com.adel.wc26.feature.auth.data.dto.RegisterRequest
import com.adel.wc26.feature.auth.data.dto.AuthResponse
import com.adel.wc26.feature.auth.domain.AuthRepository
import com.adel.wc26.feature.auth.domain.AuthUser
import javax.inject.Inject
import javax.inject.Singleton

/**
 * AuthRepository implementation — talks to AuthApi, and on success
 * persists the session token via TokenStore so the user stays signed in.
 */
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authApi: AuthApi,
    private val tokenStore: TokenStore,
) : AuthRepository {

    override suspend fun register(
        email: String,
        username: String,
        password: String,
        displayName: String,
    ): DataResult<AuthUser> {
        val result = apiCall {
            authApi.register(
                body = RegisterRequest(
                    email = email,
                    username = username,
                    password = password,
                    displayName = displayName,
                )
            )
        }
        return result.persistThenMap()
    }

    override suspend fun login(
        email: String,
        password: String,
    ): DataResult<AuthUser> {
        val result = apiCall {
            authApi.login(LoginRequest(email = email, password = password))
        }
        return result.persistThenMap()
    }

    override suspend fun logout() {
        tokenStore.clear()
    }

    /**
     * On success, save the token + user id, then map the wire response
     * to the domain [AuthUser]. On error, pass it straight through.
     */
    private suspend fun DataResult<AuthResponse>.persistThenMap(): DataResult<AuthUser> {
        if (this is DataResult.Success) {
            tokenStore.saveSession(token = data.token, userId = data.userId)
        }
        return map { response ->
            AuthUser(
                userId = response.userId,
                email = response.email,
                username = response.username,
                displayName = response.displayName,
            )
        }
    }
}