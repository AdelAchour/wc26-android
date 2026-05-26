package com.adel.wc26.feature.posts.domain.comment

import com.adel.wc26.feature.posts.domain.post.PostAuthor

/**
 * A comment on a post — the domain model.
 */
data class Comment(
    val id: Long,
    val postId: Long,
    val author: PostAuthor,
    val content: String,
    val createdAt: String,
)