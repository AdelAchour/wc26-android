package com.adel.wc26.feature.profile.domain

import com.adel.wc26.core.result.CursorPage
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.posts.domain.post.Post

/**
 * The signed-in user's full profile — the private view, includes email.
 * Sourced from GET /auth/me.
 */
data class UserProfile(
    val id: Long,
    val email: String,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val role: String,
    val joinedAt: String,
)

/**
 * Another user's public profile — no email, no role.
 * Sourced from GET /users/{id}.
 */
data class PublicProfile(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
    val joinedAt: String,
)

/**
 * User/profile data. Covers the signed-in user's own profile, public
 * lookups of other users, and a user's posts / liked posts (both
 * cursor-paginated).
 */
interface UserRepository {

    /** The signed-in user's own profile. */
    suspend fun getMyProfile(): DataResult<UserProfile>

    /** Any user's public profile by id. */
    suspend fun getPublicProfile(userId: Long): DataResult<PublicProfile>

    /** A page of posts authored by the given user. */
    suspend fun getUserPosts(userId: Long, cursor: String?): DataResult<CursorPage<Post>>

    /** A page of posts the given user has liked. */
    suspend fun getUserLikes(userId: Long, cursor: String?): DataResult<CursorPage<Post>>
}