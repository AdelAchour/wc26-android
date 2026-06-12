package com.adel.wc26.feature.posts.data.comment

import com.adel.wc26.core.network.apiCall
import com.adel.wc26.core.result.CursorPage
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.core.result.map
import com.adel.wc26.feature.posts.data.comment.dto.CreateCommentRequest
import com.adel.wc26.feature.posts.data.toDomain
import com.adel.wc26.feature.posts.domain.comment.Comment
import com.adel.wc26.feature.posts.domain.comment.CommentRepository
import com.adel.wc26.feature.profile.domain.PublicProfile
import com.adel.wc26.feature.profile.data.toDomain as toProfileDomain
import javax.inject.Inject
import javax.inject.Singleton

/**
 * CommentRepository implementation — talks to CommentApi.
 */
@Singleton
class CommentRepositoryImpl @Inject constructor(
    private val commentApi: CommentApi,
) : CommentRepository {

    override suspend fun getComments(
        postId: Long,
        limit: Int,
        offset: Long,
    ): DataResult<List<Comment>> =
        apiCall { commentApi.getComments(postId, limit, offset) }
            .map { page -> page.items.map { it.toDomain() } }

    override suspend fun getCommentLikes(
        commentId: Long,
        cursor: String?,
    ): DataResult<CursorPage<PublicProfile>> =
        apiCall { commentApi.getCommentLikes(commentId = commentId, cursor = cursor, limit = 20) }
            .map { dto ->
                CursorPage(
                    items = dto.items.map { it.toProfileDomain() },
                    nextCursor = dto.nextCursor,
                )
            }

    override suspend fun createComment(
        postId: Long,
        content: String,
    ): DataResult<Comment> =
        apiCall {
            commentApi.createComment(postId, CreateCommentRequest(content = content))
        }.map { it.toDomain() }

    override suspend fun likeComment(commentId: Long): DataResult<Unit> =
        apiCall { commentApi.likeComment(commentId) }

    override suspend fun unlikeComment(commentId: Long): DataResult<Unit> =
        apiCall { commentApi.unlikeComment(commentId) }

    override suspend fun deleteComment(commentId: Long): DataResult<Unit> =
        apiCall { commentApi.deleteComment(commentId) }
}