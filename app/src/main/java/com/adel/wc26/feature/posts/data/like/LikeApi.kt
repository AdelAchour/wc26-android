package com.adel.wc26.feature.posts.data.like

import com.adel.wc26.core.network.dto.CursorPageDto
import com.adel.wc26.feature.posts.data.post.dto.PostAuthorDto
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Like endpoints. The likers list uses CURSOR pagination.
 *
 * Like / unlike are idempotent on the backend (INSERT ON CONFLICT DO
 * NOTHING / DELETE), so repeated calls are safe — no need to check
 * current state before calling.
 *
 *   POST   /posts/{postId}/likes           (like — idempotent)
 *   DELETE /posts/{postId}/likes           (unlike — idempotent)
 *   GET    /posts/{postId}/likes?cursor=&limit=   (who liked it)
 */
interface LikeApi {

    @POST("posts/{postId}/like")
    suspend fun like(@Path("postId") postId: Long)

    @DELETE("posts/{postId}/like")
    suspend fun unlike(@Path("postId") postId: Long)

    @GET("posts/{postId}/like")
    suspend fun getLikers(
        @Path("postId") postId: Long,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 30,
    ): CursorPageDto<PostAuthorDto>
}