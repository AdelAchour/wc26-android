package com.adel.wc26.feature.posts.data

import com.adel.wc26.feature.posts.data.comment.dto.CommentDto
import com.adel.wc26.feature.posts.data.post.dto.PostAuthorDto
import com.adel.wc26.feature.posts.data.post.dto.PostDto
import com.adel.wc26.feature.posts.domain.comment.Comment
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.domain.post.PostAuthor

/**
 * Mappers: wire DTOs -> domain models for the posts feature.
 */

fun PostAuthorDto.toDomain(): PostAuthor = PostAuthor(
    id = id,
    username = username,
    displayName = displayName,
    avatarUrl = avatarUrl,
)

fun PostDto.toDomain(): Post = Post(
    id = id,
    matchId = matchId,
    author = author.toDomain(),
    content = content,
    likeCount = likeCount,
    commentCount = commentCount,
    likedByCurrentUser = likedByCurrentUser,
    createdAt = createdAt,
)

fun CommentDto.toDomain(): Comment = Comment(
    id = id,
    postId = postId,
    author = author.toDomain(),
    content = content,
    createdAt = createdAt,
)