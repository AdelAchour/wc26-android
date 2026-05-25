package com.adel.wc26.feature.profile.data

import com.adel.wc26.core.network.dto.CursorPageDto
import com.adel.wc26.feature.posts.data.post.dto.PostDto
import com.adel.wc26.feature.profile.data.dto.UserPublicDto
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * User endpoints — public profile lookups and a user's posts/likes.
 * (The signed-in user's own full profile comes from AuthApi.me().)
 *
 *
 *   GET /users/{id}
 *   GET /users/{userId}/posts?cursor=&limit=
 *   GET /users/{userId}/likes?cursor=&limit=
 */
interface UserApi {

    @GET("users/{id}")
    suspend fun getUser(@Path("id") id: Long): UserPublicDto

    @GET("users/{userId}/posts")
    suspend fun getUserPosts(
        @Path("userId") userId: Long,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
    ): CursorPageDto<PostDto>

    @GET("users/{userId}/likes")
    suspend fun getUserLikes(
        @Path("userId") userId: Long,
        @Query("cursor") cursor: String? = null,
        @Query("limit") limit: Int = 20,
    ): CursorPageDto<PostDto>
}