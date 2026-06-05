package com.adel.wc26.feature.notifications.data

import com.adel.wc26.core.network.dto.PageDto
import com.adel.wc26.feature.notifications.data.dto.NotificationDto
import com.adel.wc26.feature.notifications.data.dto.UnreadCountDto
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

interface NotificationsApi {

    @GET("notifications")
    suspend fun getNotifications(
        @Query("limit") limit: Int,
        @Query("offset") offset: Long,
    ): PageDto<NotificationDto>

    @GET("notifications/unread-count")
    suspend fun getUnreadCount(): UnreadCountDto

    @POST("notifications/read")
    suspend fun markAllAsRead()

    @PATCH("notifications/{id}/read")
    suspend fun markAsRead(
        @Path("id") id: Long,
    )
}