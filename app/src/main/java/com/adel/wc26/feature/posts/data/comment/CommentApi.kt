package com.adel.wc26.feature.posts.data.comment

import com.adel.wc26.core.network.dto.CursorPageDto
import com.adel.wc26.core.network.dto.PageDto
import com.adel.wc26.feature.posts.data.comment.dto.CommentDto
import com.adel.wc26.feature.posts.data.comment.dto.CreateCommentRequest
import com.adel.wc26.feature.profile.data.dto.UserPublicDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Comment endpoints. Comments use OFFSET pagination.
 *
 *   GET    /posts/{postId}/comments?limit=&offset=
 *   POST   /posts/{postId}/comments        (body: content)
 *   DELETE /comments/{id}
 */
interface CommentApi {

    @GET("posts/{postId}/comments")
    suspend fun getComments(
        @Path("postId") postId: Long,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Long = 0,
    ): PageDto<CommentDto>

    @POST("posts/{postId}/comments")
    suspend fun createComment(
        @Path("postId") postId: Long,
        @Body body: CreateCommentRequest,
    ): CommentDto

    @POST("comments/{commentId}/like")
    suspend fun likeComment(@Path("commentId") commentId: Long)

    @DELETE("comments/{commentId}/like")
    suspend fun unlikeComment(@Path("commentId") commentId: Long)

    @GET("comments/{commentId}/likes")
    suspend fun getCommentLikes(
        @Path("commentId") commentId: Long,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
    ): CursorPageDto<UserPublicDto>

    @DELETE("comments/{id}")
    suspend fun deleteComment(@Path("id") id: Long)
}