package com.adel.wc26.feature.posts.data.post.dto

import kotlinx.serialization.Serializable

/**
 * Public author view embedded in posts and comments.
 * Shared by PostDto and CommentDto.
 */
@Serializable
data class PostAuthorDto(
    val id: Long,
    val username: String,
    val displayName: String,
    val avatarUrl: String? = null,
)

/** Post wire format — mirrors the backend's PostDto. */
@Serializable
data class PostDto(
    val id: Long,
    val matchId: Long,
    val author: PostAuthorDto,
    val content: String,
    val likeCount: Int,
    val commentCount: Int,
    val likedByCurrentUser: Boolean,
    val createdAt: String,
)

/** Request body for creating a post under a match. */
@Serializable
data class CreatePostRequest(
    val content: String,
)