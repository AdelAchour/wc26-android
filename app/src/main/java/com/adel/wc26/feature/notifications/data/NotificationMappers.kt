package com.adel.wc26.feature.notifications.data

import com.adel.wc26.feature.notifications.data.dto.NotificationCommentDto
import com.adel.wc26.feature.notifications.data.dto.NotificationDto
import com.adel.wc26.feature.notifications.data.dto.NotificationPostDto
import com.adel.wc26.feature.notifications.domain.Notification
import com.adel.wc26.feature.notifications.domain.NotificationComment
import com.adel.wc26.feature.notifications.domain.NotificationPost
import com.adel.wc26.feature.notifications.domain.NotificationType
import com.adel.wc26.feature.posts.data.toDomain

fun NotificationPostDto.toDomain() = NotificationPost(
    id = id,
    content = content
)

fun NotificationCommentDto.toDomain() = NotificationComment(
    id = id,
    content = content
)

fun NotificationDto.toDomain() = Notification(
    id = id,
    receiverId = receiverId,
    type = try {
        NotificationType.valueOf(type)
    } catch (e: Exception) {
        NotificationType.LIKE_POST
    },
    isRead = isRead,
    createdAt = createdAt,
    sender = sender.toDomain(),
    post = post.toDomain(),
    comment = comment?.toDomain()
)