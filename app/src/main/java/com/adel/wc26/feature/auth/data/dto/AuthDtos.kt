package com.adel.wc26.feature.auth.data.dto

import kotlinx.serialization.Serializable

/** Request body for POST /auth/register. */
@Serializable
data class RegisterRequest(
    val email: String,
    val username: String,
    val password: String,
    val displayName: String,
)

/** Request body for POST /auth/login. */
@Serializable
data class LoginRequest(
    val email: String,
    val password: String,
)

/** Response from register/login — the token plus basic identity. */
@Serializable
data class AuthResponse(
    val token: String,
    val userId: Long,
    val email: String,
    val username: String,
    val displayName: String,
)