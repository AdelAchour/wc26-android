package com.adel.wc26.feature.posts.domain.post

/**
 * The author of a post or comment — public identity only.
 */
data class PostAuthor(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String?,
)

/**
 * A post — the domain model.
 *
 * [createdAt] stays an ISO string here; relative-time formatting happens
 * at the UI layer via WC26DateTime. [likedByCurrentUser] is false for
 * unauthenticated requests.
 */
data class Post(
    val id: Long,
    val matchId: Long,
    val author: PostAuthor,
    val content: String,
    val likeCount: Int,
    val commentCount: Int,
    val likedByCurrentUser: Boolean,
    val createdAt: String,
)