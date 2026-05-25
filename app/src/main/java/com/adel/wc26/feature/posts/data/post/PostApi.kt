package com.adel.wc26.feature.posts.data.post

import com.adel.wc26.core.network.dto.CursorPageDto
import com.adel.wc26.feature.posts.data.post.dto.CreatePostRequest
import com.adel.wc26.feature.posts.data.post.dto.PostDto
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Post endpoints. Posts use CURSOR pagination (unbounded, infinite scroll).
 *
 *   GET    /posts?cursor=&limit=                 (global feed)
 *   GET    /matches/{matchId}/posts?cursor=&limit=  (per-match thread)
 *   GET    /posts/{id}
 *   POST   /matches/{matchId}/posts              (create — body: content)
 *   DELETE /posts/{id}
 */
interface PostApi {

    @GET("posts")
    suspend fun getGlobalFeed(
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
    ): CursorPageDto<PostDto>

    @GET("matches/{matchId}/posts")
    suspend fun getMatchPosts(
        @Path("matchId") matchId: Long,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
    ): CursorPageDto<PostDto>

    @GET("posts/{id}")
    suspend fun getPost(@Path("id") id: Long): PostDto

    @POST("matches/{matchId}/posts")
    suspend fun createPost(
        @Path("matchId") matchId: Long,
        @Body body: CreatePostRequest,
    ): PostDto

    @DELETE("posts/{id}")
    suspend fun deletePost(@Path("id") id: Long)
}