package com.adel.wc26.feature.profile.ui.userprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import androidx.paging.filter
import androidx.paging.map
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.posts.data.PostDeletionManager
import com.adel.wc26.feature.posts.data.PostLikeManager
import com.adel.wc26.feature.posts.data.post.PostPagingSource
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.profile.domain.PublicProfile
import com.adel.wc26.feature.profile.domain.UserRepository
import com.adel.wc26.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * State for the public user-profile header (the identity itself).
 * The user's posts below are a separate paged Flow.
 */
data class UserProfileUiState(
    val loading: Boolean = true,
    val profile: PublicProfile? = null,
    val error: AppError? = null,
    val isRefreshing: Boolean = false,
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
    private val postLikeManager: PostLikeManager,
    private val postDeletionManager: PostDeletionManager,
    private val tokenStore: TokenStore,
) : ViewModel() {

    // Expose the current user's ID for showing delete actions
    val currentUserId: Flow<Long?> = flow {
        emit(tokenStore.getUserId())
    }

    private val userId: Long =
        savedStateHandle.toRoute<Destinations.UserProfile>().userId

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    /** The user's posts, paged. */
    private val pagingFlow = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            PostPagingSource { cursor ->
                userRepository.getUserPosts(userId, cursor)
            }
        },
    ).flow.cachedIn(viewModelScope)
    val posts: Flow<PagingData<Post>> = pagingFlow
        .combine(postLikeManager.likedStates) { pagingData, likedStates ->
            pagingData.map { post ->
                likedStates[post.id]?.let { status ->
                    post.copy(
                        likedByCurrentUser = status.likedByCurrentUser,
                        likeCount = status.likeCount
                    )
                } ?: post
            }
        }
        .combine(postDeletionManager.deletedPostIds) { pagingData, deletedIds ->
            // Filter out deleted posts dynamically
            pagingData.filter { post -> post.id !in deletedIds }
        }

    init {
        loadProfile()
    }

    fun loadProfile(isRefresh: Boolean = false) {
        viewModelScope.launch {
            if (isRefresh) {
                _uiState.update { it.copy(isRefreshing = true) }
            } else {
                _uiState.update { it.copy(loading = true, error = null) }
            }
            when (val result = userRepository.getPublicProfile(userId)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(loading = false, profile = result.data, isRefreshing = false) }
                is DataResult.Error ->
                    _uiState.update { it.copy(loading = false, error = result.error, isRefreshing = false) }
            }
        }
    }

    fun toggleLike(post: Post) {
        postLikeManager.toggleLike(post, viewModelScope)
    }

    fun deletePost(post: Post, onFailure: () -> Unit = {}) {
        postDeletionManager.deletePost(post.id, viewModelScope, onFailure)
    }
}