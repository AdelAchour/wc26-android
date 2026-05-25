package com.adel.wc26.feature.posts.data.comment.dto

import com.adel.wc26.feature.posts.data.post.dto.PostAuthorDto
import kotlinx.serialization.Serializable

/**
 * Comment wire format — mirrors the backend's CommentDto.
 * Reuses [PostAuthorDto] for the author, matching the backend.
 */
@Serializable
data class CommentDto(
    val id: Long,
    val postId: Long,
    val author: PostAuthorDto,
    val content: String,
    val createdAt: String,
)

/** Request body for creating a comment on a post. */
@Serializable
data class CreateCommentRequest(
    val content: String,
)