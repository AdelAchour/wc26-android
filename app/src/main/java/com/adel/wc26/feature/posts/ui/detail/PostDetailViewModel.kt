package com.adel.wc26.feature.posts.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.posts.domain.comment.Comment
import com.adel.wc26.feature.posts.domain.comment.CommentRepository
import com.adel.wc26.feature.posts.domain.like.LikeRepository
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.domain.post.PostRepository
import com.adel.wc26.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the post detail screen.
 *
 * The screen has a header (the post itself) and a comments thread below,
 * plus a comment composer pinned at the bottom.
 */
data class PostDetailUiState(
    val loading: Boolean = true,
    val post: Post? = null,
    val error: AppError? = null,
    // Comments
    val comments: List<Comment> = emptyList(),
    val commentsLoading: Boolean = false,
    val commentsError: AppError? = null,
    // Comment composer
    val commentInput: String = "",
    val sendingComment: Boolean = false,
    // Auth
    val isLoggedIn: Boolean = false,
) {
    val canSendComment: Boolean
        get() = commentInput.isNotBlank() &&
                commentInput.length <= COMMENT_MAX &&
                !sendingComment

    val commentCharsLeft: Int get() = COMMENT_MAX - commentInput.length

    companion object {
        const val COMMENT_MAX = 300
    }
}

@HiltViewModel
class PostDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository,
    private val commentRepository: CommentRepository,
    private val likeRepository: LikeRepository,
    tokenStore: TokenStore,
) : ViewModel() {

    private val postId: Long =
        savedStateHandle.toRoute<Destinations.PostDetail>().postId

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggedIn = tokenStore.getToken() != null) }
        }
        loadPost()
        loadComments()
    }

    fun loadPost() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            when (val result = postRepository.getPost(postId)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(loading = false, post = result.data) }
                is DataResult.Error ->
                    _uiState.update { it.copy(loading = false, error = result.error) }
            }
        }
    }

    fun loadComments() {
        viewModelScope.launch {
            _uiState.update { it.copy(commentsLoading = true, commentsError = null) }
            val result = commentRepository.getComments(
                postId = postId,
                limit = 100,
                offset = 0,
            )
            when (result) {
                is DataResult.Success ->
                    _uiState.update {
                        it.copy(commentsLoading = false, comments = result.data)
                    }
                is DataResult.Error ->
                    _uiState.update {
                        it.copy(commentsLoading = false, commentsError = result.error)
                    }
            }
        }
    }

    /**
     * Optimistic like toggle: flip the UI immediately, call the API, and
     * roll back if it fails. The backend's like/unlike are idempotent, so
     * a retried or duplicated call is harmless.
     */
    fun toggleLike() {
        val current = _uiState.value.post ?: return
        val wasLiked = current.likedByCurrentUser

        // 1. Optimistic update.
        val optimistic = current.copy(
            likedByCurrentUser = !wasLiked,
            likeCount = current.likeCount + if (wasLiked) -1 else 1,
        )
        _uiState.update { it.copy(post = optimistic) }

        // 2. Fire the API; roll back on failure.
        viewModelScope.launch {
            val result = if (wasLiked) {
                likeRepository.unlike(postId)
            } else {
                likeRepository.like(postId)
            }
            if (result is DataResult.Error) {
                // Roll back to the pre-tap state.
                _uiState.update { it.copy(post = current) }
            }
        }
    }

    fun onCommentInputChange(value: String) {
        if (value.length <= PostDetailUiState.COMMENT_MAX) {
            _uiState.update { it.copy(commentInput = value) }
        }
    }

    fun sendComment() {
        val state = _uiState.value
        if (!state.canSendComment) return

        viewModelScope.launch {
            _uiState.update { it.copy(sendingComment = true) }
            val result = commentRepository.createComment(
                postId = postId,
                content = state.commentInput.trim(),
            )
            when (result) {
                is DataResult.Success ->
                    _uiState.update {
                        it.copy(
                            sendingComment = false,
                            commentInput = "",
                            // Prepend the new comment; bump the post's count.
                            comments = listOf(result.data) + it.comments,
                            post = it.post?.copy(
                                commentCount = it.post.commentCount + 1,
                            ),
                        )
                    }
                is DataResult.Error ->
                    _uiState.update {
                        it.copy(sendingComment = false, commentsError = result.error)
                    }
            }
        }
    }
}