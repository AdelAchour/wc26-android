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

/** Request body for PATCH /auth/me. */
@Serializable
data class UpdateProfileRequest(
    val displayName: String? = null,
    val avatarUrl: String? = null,
    val bio: String? = null,
)

/** Request body for POST /auth/forgot-password. */
@Serializable
data class ForgotPasswordRequest(val email: String)

/** Request body for POST /auth/reset-password. */
@Serializable
data class ResetPasswordRequest(
    val email: String,
    val code: String,
    val newPassword: String,
)

/** Generic success response carrying a message. */
@Serializable
data class MessageResponse(val message: String)

/** Error response body with an error field. */
@Serializable
data class ErrorResponse(val error: String)