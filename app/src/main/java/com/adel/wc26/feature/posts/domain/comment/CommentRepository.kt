package com.adel.wc26.feature.posts.domain.comment

import com.adel.wc26.core.result.CursorPage
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.profile.domain.PublicProfile

/**
 * Comment data. Comments use offset pagination on the backend; the app
 * fetches them in bounded pages.
 */
interface CommentRepository {

    /** A page of a post's comments. */
    suspend fun getComments(
        postId: Long,
        limit: Int,
        offset: Long,
    ): DataResult<List<Comment>>

    /** A page of users who liked the comment. */
    suspend fun getCommentLikes(commentId: Long, cursor: String?): DataResult<CursorPage<PublicProfile>>

    /** Create a comment on a post. Returns the created comment. */
    suspend fun createComment(postId: Long, content: String): DataResult<Comment>

    /** Like a comment (idempotent). */
    suspend fun likeComment(commentId: Long): DataResult<Unit>

    /** Unlike a comment (idempotent). */
    suspend fun unlikeComment(commentId: Long): DataResult<Unit>

    /** Delete a comment. */
    suspend fun deleteComment(commentId: Long): DataResult<Unit>
}