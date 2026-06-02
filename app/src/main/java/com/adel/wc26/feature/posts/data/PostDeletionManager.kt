package com.adel.wc26.feature.posts.data

import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.posts.domain.comment.CommentRepository
import com.adel.wc26.feature.posts.domain.post.PostRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PostDeletionManager @Inject constructor(
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
) {
    // Keep track of deleted post IDs in this session
    private val _deletedPostIds = MutableStateFlow<Set<Long>>(emptySet())
    val deletedPostIds: StateFlow<Set<Long>> = _deletedPostIds.asStateFlow()

    // Keep track of deleted comment IDs in this session
    private val _deletedCommentIds = MutableStateFlow<Set<Long>>(emptySet())
    val deletedCommentIds: StateFlow<Set<Long>> = _deletedCommentIds.asStateFlow()

    fun deletePost(postId: Long, scope: CoroutineScope, onFailure: () -> Unit = {}) {
        _deletedPostIds.update { it + postId }
        scope.launch {
            val result = postRepository.deletePost(postId)
            if (result is DataResult.Error) {
                _deletedPostIds.update { it - postId }
                onFailure()
            }
        }
    }

    fun deleteComment(commentId: Long, scope: CoroutineScope, onFailure: () -> Unit = {}) {
        _deletedCommentIds.update { it + commentId }
        scope.launch {
            val result = commentRepository.deleteComment(commentId)
            if (result is DataResult.Error) {
                _deletedCommentIds.update { it - commentId }
                onFailure()
            }
        }
    }
}