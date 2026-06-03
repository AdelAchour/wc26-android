package com.adel.wc26.feature.posts.data.comment

import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.posts.data.LikeStatus
import com.adel.wc26.feature.posts.domain.comment.Comment
import com.adel.wc26.feature.posts.domain.comment.CommentRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class CommentLikeManager @Inject constructor(
    private val commentRepository: CommentRepository,
) {
    private val _likedStates = MutableStateFlow<Map<Long, LikeStatus>>(emptyMap())
    val likedStates: StateFlow<Map<Long, LikeStatus>> = _likedStates.asStateFlow()

    fun toggleLike(comment: Comment, scope: CoroutineScope) {
        val commentId = comment.id
        val currentState = _likedStates.value[commentId] ?: LikeStatus(
            likedByCurrentUser = comment.likedByCurrentUser,
            likeCount = comment.likeCount
        )

        val wasLiked = currentState.likedByCurrentUser
        val nextLiked = !wasLiked
        val nextCount = currentState.likeCount + if (nextLiked) 1 else -1

        val targetState = LikeStatus(
            likedByCurrentUser = nextLiked,
            likeCount = nextCount
        )

        // 1. Optimistic Update
        _likedStates.update { it + (commentId to targetState) }

        // 2. Network Call with Rollback on Failure
        scope.launch {
            val result = if (wasLiked) {
                commentRepository.unlikeComment(commentId)
            } else {
                commentRepository.likeComment(commentId)
            }

            if (result is DataResult.Error) {
                _likedStates.update { it + (commentId to currentState) }
            }
        }
    }
}