package com.adel.wc26.feature.profile.ui.userprofile

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
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
)

@HiltViewModel
class UserProfileViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val userRepository: UserRepository,
) : ViewModel() {

    private val userId: Long =
        savedStateHandle.toRoute<Destinations.UserProfile>().userId

    private val _uiState = MutableStateFlow(UserProfileUiState())
    val uiState: StateFlow<UserProfileUiState> = _uiState.asStateFlow()

    /** The user's posts, paged. */
    val posts: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            PostPagingSource { cursor ->
                userRepository.getUserPosts(userId, cursor)
            }
        },
    ).flow.cachedIn(viewModelScope)

    init {
        loadProfile()
    }

    fun loadProfile() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            when (val result = userRepository.getPublicProfile(userId)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(loading = false, profile = result.data) }
                is DataResult.Error ->
                    _uiState.update { it.copy(loading = false, error = result.error) }
            }
        }
    }
}