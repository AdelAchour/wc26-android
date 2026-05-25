package com.adel.wc26.feature.profile.data.dto

import kotlinx.serialization.Serializable

/**
 * Full user view — only ever returned for the signed-in user's own
 * profile (GET /auth/me). Includes private fields: email, role.
 */
@Serializable
data class UserDto(
    val id: Long,
    val email: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val role: String,
    val createdAt: String,
)

/**
 * Public user view — returned for any other user lookup.
 * Excludes email and role.
 */
@Serializable
data class UserPublicDto(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
    val createdAt: String,
)