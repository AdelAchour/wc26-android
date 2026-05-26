package com.adel.wc26.feature.posts.data.post

import com.adel.wc26.core.network.apiCall
import com.adel.wc26.core.result.CursorPage
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.core.result.map
import com.adel.wc26.feature.posts.data.post.dto.CreatePostRequest
import com.adel.wc26.feature.posts.data.toDomain
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.domain.post.PostRepository
import javax.inject.Inject
import javax.inject.Singleton

/**
 * PostRepository implementation — talks to PostApi, maps DTOs to domain.
 */
@Singleton
class PostRepositoryImpl @Inject constructor(
    private val postApi: PostApi,
) : PostRepository {

    private companion object {
        const val PAGE_SIZE = 20
    }

    override suspend fun getGlobalFeed(cursor: String?): DataResult<CursorPage<Post>> =
        apiCall { postApi.getGlobalFeed(cursor = cursor, limit = PAGE_SIZE) }
            .map { dto ->
                CursorPage(
                    items = dto.items.map { it.toDomain() },
                    nextCursor = dto.nextCursor,
                )
            }

    override suspend fun getMatchPosts(
        matchId: Long,
        cursor: String?,
    ): DataResult<CursorPage<Post>> =
        apiCall { postApi.getMatchPosts(matchId = matchId, cursor = cursor, limit = PAGE_SIZE) }
            .map { dto ->
                CursorPage(
                    items = dto.items.map { it.toDomain() },
                    nextCursor = dto.nextCursor,
                )
            }

    override suspend fun getPost(postId: Long): DataResult<Post> =
        apiCall { postApi.getPost(postId) }.map { it.toDomain() }

    override suspend fun createPost(matchId: Long, content: String): DataResult<Post> =
        apiCall {
            postApi.createPost(matchId, CreatePostRequest(content = content))
        }.map { it.toDomain() }

    override suspend fun deletePost(postId: Long): DataResult<Unit> =
        apiCall { postApi.deletePost(postId) }
}