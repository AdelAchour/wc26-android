package com.adel.wc26.feature.posts.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.posts.data.PostDeletionManager
import com.adel.wc26.feature.posts.data.PostLikeManager
import com.adel.wc26.feature.posts.data.comment.CommentLikeManager
import com.adel.wc26.feature.posts.domain.comment.Comment
import com.adel.wc26.feature.posts.domain.comment.CommentRepository
import com.adel.wc26.feature.posts.domain.like.LikeRepository
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.domain.post.PostRepository
import com.adel.wc26.feature.profile.domain.PublicProfile
import com.adel.wc26.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.async
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
    val currentUserId: Long? = null,
    val isRefreshing: Boolean = false,
    // Post likers
    val likers: List<PublicProfile> = emptyList(),
    val likersLoading: Boolean = false,
    val likersNextCursor: String? = null,
    val likersError: AppError? = null,
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
    private val postLikeManager: PostLikeManager,
    private val postDeletionManager: PostDeletionManager,
    private val commentLikeManager: CommentLikeManager,
    tokenStore: TokenStore,
) : ViewModel() {

    private val postId: Long =
        savedStateHandle.toRoute<Destinations.PostDetail>().postId

    private var activeCommentIdForLikes: Long? = null

    private val _uiState = MutableStateFlow(PostDetailUiState())
    val uiState: StateFlow<PostDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoggedIn = tokenStore.getToken() != null,
                    currentUserId = tokenStore.getUserId()
                )
            }
        }
        loadPost()
        loadComments()

        // Dynamically synchronize local post details with global like updates
        viewModelScope.launch {
            postLikeManager.likedStates.collect { likedStates ->
                _uiState.update { state ->
                    val currentPost = state.post ?: return@update state
                    likedStates[currentPost.id]?.let { status ->
                        state.copy(
                            post = currentPost.copy(
                                likedByCurrentUser = status.likedByCurrentUser,
                                likeCount = status.likeCount
                            )
                        )
                    } ?: state
                }
            }
        }
        // Dynamically synchronize local comments with global deletions
        viewModelScope.launch {
            postDeletionManager.deletedCommentIds.collect { deletedCommentIds ->
                _uiState.update { state ->
                    val updatedComments = state.comments.filter { it.id !in deletedCommentIds }
                    val diff = state.comments.size - updatedComments.size
                    state.copy(
                        comments = updatedComments,
                        post = if (diff > 0) state.post?.copy(
                            commentCount = (state.post.commentCount - diff).coerceAtLeast(0)
                        ) else state.post
                    )
                }
            }
        }
        // Dynamically synchronize local comments with global like updates
        viewModelScope.launch {
            commentLikeManager.likedStates.collect { likedStates ->
                _uiState.update { state ->
                    val updatedComments = state.comments.map { comment ->
                        likedStates[comment.id]?.let { status ->
                            comment.copy(
                                likedByCurrentUser = status.likedByCurrentUser,
                                likeCount = status.likeCount
                            )
                        } ?: comment
                    }
                    state.copy(comments = updatedComments)
                }
            }
        }
        // Dynamically monitor if this parent post itself gets deleted
        viewModelScope.launch {
            postDeletionManager.deletedPostIds.collect { deletedPostIds ->
                if (postId in deletedPostIds) {
                    _uiState.update { it.copy(post = null) }
                }
            }
        }
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

    fun refresh() {
        viewModelScope.launch {
            _uiState.update { it.copy(isRefreshing = true) }
            val postDeferred = async { postRepository.getPost(postId) }
            val commentsDeferred = async {
                commentRepository.getComments(postId = postId, limit = 100, offset = 0)
            }
            val postResult = postDeferred.await()
            val commentsResult = commentsDeferred.await()

            _uiState.update { state ->
                var nextState = state.copy(isRefreshing = false)

                when (postResult) {
                    is DataResult.Success -> nextState = nextState.copy(post = postResult.data, error = null)
                    is DataResult.Error -> nextState = nextState.copy(error = postResult.error)
                }

                when (commentsResult) {
                    is DataResult.Success -> nextState = nextState.copy(comments = commentsResult.data, commentsError = null)
                    is DataResult.Error -> nextState = nextState.copy(commentsError = commentsResult.error)
                }

                nextState
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
        postLikeManager.toggleLike(current, viewModelScope)
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

    fun deletePost(onFailure: () -> Unit = {}) {
        postDeletionManager.deletePost(postId, viewModelScope, onFailure)
    }

    fun deleteComment(commentId: Long, onFailure: () -> Unit = {}) {
        postDeletionManager.deleteComment(commentId, viewModelScope, onFailure)
    }

    fun toggleLikeComment(comment: Comment) {
        commentLikeManager.toggleLike(comment, viewModelScope)
    }

    fun openPostLikes() {
        val post = _uiState.value.post ?: return
        val currentUserId = _uiState.value.currentUserId
        if (currentUserId == null || currentUserId != post.author.id) return

        activeCommentIdForLikes = null
        _uiState.update {
            it.copy(
                likers = emptyList(),
                likersNextCursor = null,
                likersLoading = true,
                likersError = null
            )
        }
        loadPostLikes(postId = post.id, cursor = null)
    }

    fun openCommentLikes(commentId: Long) {
        val currentUserId = _uiState.value.currentUserId
        val comment = _uiState.value.comments.find { it.id == commentId } ?: return
        if (currentUserId == null || currentUserId != comment.author.id) return

        activeCommentIdForLikes = commentId
        _uiState.update {
            it.copy(
                likers = emptyList(),
                likersNextCursor = null,
                likersLoading = true,
                likersError = null
            )
        }
        loadCommentLikes(commentId = commentId, cursor = null)
    }

    fun loadMoreLikers() {
        val cursor = _uiState.value.likersNextCursor ?: return
        if (_uiState.value.likersLoading) return

        _uiState.update { it.copy(likersLoading = true) }
        val commentId = activeCommentIdForLikes
        if (commentId != null) {
            loadCommentLikes(commentId = commentId, cursor = cursor)
        } else {
            val post = _uiState.value.post ?: return
            loadPostLikes(postId = post.id, cursor = cursor)
        }
    }

    private fun loadPostLikes(postId: Long, cursor: String?) {
        viewModelScope.launch {
            val result = postRepository.getPostLikes(postId = postId, cursor = cursor)
            _uiState.update { state ->
                if (activeCommentIdForLikes != null || state.post?.id != postId) return@update state
                when (result) {
                    is DataResult.Success -> {
                        val newItems = result.data.items
                        val updatedList = if (cursor == null) newItems else state.likers + newItems
                        state.copy(
                            likers = updatedList,
                            likersNextCursor = result.data.nextCursor,
                            likersLoading = false,
                            likersError = null
                        )
                    }
                    is DataResult.Error -> {
                        state.copy(
                            likersLoading = false,
                            likersError = result.error
                        )
                    }
                }
            }
        }
    }

    private fun loadCommentLikes(commentId: Long, cursor: String?) {
        viewModelScope.launch {
            val result = commentRepository.getCommentLikes(commentId = commentId, cursor = cursor)
            _uiState.update { state ->
                if (activeCommentIdForLikes != commentId) return@update state
                when (result) {
                    is DataResult.Success -> {
                        val newItems = result.data.items
                        val updatedList = if (cursor == null) newItems else state.likers + newItems
                        state.copy(
                            likers = updatedList,
                            likersNextCursor = result.data.nextCursor,
                            likersLoading = false,
                            likersError = null
                        )
                    }
                    is DataResult.Error -> {
                        state.copy(
                            likersLoading = false,
                            likersError = result.error
                        )
                    }
                }
            }
        }
    }
}