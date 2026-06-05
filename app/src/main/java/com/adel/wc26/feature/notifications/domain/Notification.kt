package com.adel.wc26.feature.notifications.domain

import com.adel.wc26.feature.posts.domain.post.PostAuthor

enum class NotificationType {
    LIKE_POST,
    REPLY_POST,
    LIKE_COMMENT
}

data class NotificationPost(
    val id: Long,
    val content: String,
)

data class NotificationComment(
    val id: Long,
    val content: String,
)

data class Notification(
    val id: Long,
    val receiverId: Long,
    val type: NotificationType,
    val isRead: Boolean,
    val createdAt: String,
    val sender: PostAuthor,
    val post: NotificationPost,
    val comment: NotificationComment?,
)