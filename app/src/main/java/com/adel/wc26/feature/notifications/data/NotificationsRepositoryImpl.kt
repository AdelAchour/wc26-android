package com.adel.wc26.feature.notifications.data

import com.adel.wc26.core.network.apiCall
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.core.result.map
import com.adel.wc26.feature.notifications.domain.NotificationsRepository
import com.adel.wc26.feature.notifications.domain.Notification
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsRepositoryImpl @Inject constructor(
    private val notificationsApi: NotificationsApi,
) : NotificationsRepository {

    override suspend fun getNotifications(
        limit: Int,
        offset: Long,
    ): DataResult<List<Notification>> =
        apiCall { notificationsApi.getNotifications(limit, offset) }
            .map { page -> page.items.map { it.toDomain() } }

    override suspend fun getUnreadCount(): DataResult<Int> =
        apiCall { notificationsApi.getUnreadCount() }
            .map { it.unreadCount }

    override suspend fun markAllAsRead(): DataResult<Unit> =
        apiCall { notificationsApi.markAllAsRead() }

    override suspend fun markAsRead(id: Long): DataResult<Unit> =
        apiCall { notificationsApi.markAsRead(id) }
}