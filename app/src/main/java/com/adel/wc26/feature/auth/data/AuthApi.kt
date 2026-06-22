package com.adel.wc26.feature.auth.data

import com.adel.wc26.feature.auth.data.dto.AuthResponse
import com.adel.wc26.feature.auth.data.dto.ForgotPasswordRequest
import com.adel.wc26.feature.auth.data.dto.LoginRequest
import com.adel.wc26.feature.auth.data.dto.MessageResponse
import com.adel.wc26.feature.auth.data.dto.RegisterRequest
import com.adel.wc26.feature.auth.data.dto.ResetPasswordRequest
import com.adel.wc26.feature.auth.data.dto.UpdateProfileRequest
import com.adel.wc26.feature.profile.data.dto.UserDto
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST

/**
 * Auth endpoints.
 *
 *
 *   POST /auth/register
 *   POST /auth/login
 *   GET  /auth/me        (requires the bearer token)
 *   POST /auth/forgot-password
 *   POST /auth/reset-password
 */
interface AuthApi {

    @POST("auth/register")
    suspend fun register(@Body body: RegisterRequest): AuthResponse

    @POST("auth/login")
    suspend fun login(@Body body: LoginRequest): AuthResponse

    @GET("auth/me")
    suspend fun me(): UserDto

    @PATCH("auth/me")
    suspend fun updateProfile(@Body body: UpdateProfileRequest): UserDto

    @POST("auth/forgot-password")
    suspend fun forgotPassword(@Body body: ForgotPasswordRequest): MessageResponse

    @POST("auth/reset-password")
    suspend fun resetPassword(@Body body: ResetPasswordRequest): MessageResponse
}