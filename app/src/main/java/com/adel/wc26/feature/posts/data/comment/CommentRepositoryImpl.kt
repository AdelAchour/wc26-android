package com.adel.wc26.feature.posts.data.comment

import com.adel.wc26.core.network.apiCall
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.core.result.map
import com.adel.wc26.feature.posts.data.comment.dto.CreateCommentRequest
import com.adel.wc26.feature.posts.data.toDomain
import com.adel.wc26.feature.posts.domain.comment.Comment
import com.adel.wc26.feature.posts.domain.comment.CommentRepository
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

    override suspend fun createComment(
        postId: Long,
        content: String,
    ): DataResult<Comment> =
        apiCall {
            commentApi.createComment(postId, CreateCommentRequest(content = content))
        }.map { it.toDomain() }

    override suspend fun deleteComment(commentId: Long): DataResult<Unit> =
        apiCall { commentApi.deleteComment(commentId) }
}