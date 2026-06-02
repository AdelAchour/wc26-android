package com.adel.wc26.feature.posts.data

import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.posts.domain.like.LikeRepository
import com.adel.wc26.feature.posts.domain.post.Post
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject
import javax.inject.Singleton

data class LikeStatus(
    val likedByCurrentUser: Boolean,
    val likeCount: Int
)

@Singleton
class PostLikeManager @Inject constructor(
    private val likeRepository: LikeRepository,
) {
    // Keep track of modified post liked states: postId -> LikeStatus
    private val _likedStates = MutableStateFlow<Map<Long, LikeStatus>>(emptyMap())
    val likedStates: StateFlow<Map<Long, LikeStatus>> = _likedStates.asStateFlow()

    fun toggleLike(post: Post, scope: CoroutineScope) {
        val postId = post.id
        // Get the current active state (or fallback to the post's initial attributes)
        val currentState = _likedStates.value[postId] ?: LikeStatus(
            likedByCurrentUser = post.likedByCurrentUser,
            likeCount = post.likeCount
        )

        val wasLiked = currentState.likedByCurrentUser
        val nextLiked = !wasLiked
        val nextCount = currentState.likeCount + if (nextLiked) 1 else -1

        val targetState = LikeStatus(
            likedByCurrentUser = nextLiked,
            likeCount = nextCount
        )

        // 1. Optimistic Update (UI updates instantly)
        _likedStates.update { it + (postId to targetState) }

        // 2. Network Call (Rollback on failure)
        scope.launch {
            val result = if (wasLiked) {
                likeRepository.unlike(postId)
            } else {
                likeRepository.like(postId)
            }

            if (result is DataResult.Error) {
                // Roll back to the previous state on network failure
                _likedStates.update { it + (postId to currentState) }
            }
        }
    }
}