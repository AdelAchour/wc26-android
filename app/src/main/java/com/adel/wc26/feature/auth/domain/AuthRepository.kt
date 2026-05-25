package com.adel.wc26.feature.auth.domain

import com.adel.wc26.core.result.DataResult

/**
 * The signed-in user's identity, as the app cares about it.
 * (A trimmed domain view — the full profile comes from the profile feature.)
 */
data class AuthUser(
    val userId: Long,
    val email: String,
    val username: String,
    val displayName: String,
)

/**
 * Auth operations. The implementation lives in the data layer; this
 * interface is the contract the UI/domain depends on.
 *
 * register/login return a [DataResult] — success carries the [AuthUser],
 * error carries a presentable message. On success the implementation has
 * already persisted the session token.
 */
interface AuthRepository {

    suspend fun register(
        email: String,
        username: String,
        password: String,
        displayName: String,
    ): DataResult<AuthUser>

    suspend fun login(
        email: String,
        password: String,
    ): DataResult<AuthUser>

    /** Clears the stored session. */
    suspend fun logout()
}