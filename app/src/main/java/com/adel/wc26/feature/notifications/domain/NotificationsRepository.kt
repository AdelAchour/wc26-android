package com.adel.wc26.feature.notifications.domain

import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.notifications.domain.Notification

interface NotificationsRepository {
    suspend fun getNotifications(limit: Int, offset: Long): DataResult<List<Notification>>
    suspend fun getUnreadCount(): DataResult<Int>
    suspend fun markAllAsRead(): DataResult<Unit>
    suspend fun markAsRead(id: Long): DataResult<Unit>
    suspend fun registerPushToken(token: String): DataResult<Unit>
    suspend fun unregisterPushToken(token: String): DataResult<Unit>
}