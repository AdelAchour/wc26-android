package com.adel.wc26.feature.notifications.data.dto

import com.adel.wc26.feature.posts.data.post.dto.PostAuthorDto
import kotlinx.serialization.Serializable

@Serializable
data class NotificationPostDto(
    val id: Long,
    val content: String,
)

@Serializable
data class NotificationCommentDto(
    val id: Long,
    val content: String,
)

@Serializable
data class NotificationDto(
    val id: Long,
    val receiverId: Long,
    val type: String, // "LIKE_POST", "REPLY_POST", "LIKE_COMMENT"
    val isRead: Boolean,
    val createdAt: String,
    val sender: PostAuthorDto,
    val post: NotificationPostDto,
    val comment: NotificationCommentDto? = null,
)

@Serializable
data class UnreadCountDto(
    val unreadCount: Int,
)

@Serializable
data class PushTokenRequest(
    val pushToken: String,
)