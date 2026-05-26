package com.adel.wc26.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.posts.data.post.PostPagingSource
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.profile.domain.UserProfile
import com.adel.wc26.feature.profile.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Which tab of the own-profile screen is selected. */
enum class ProfileTab { POSTS, LIKES }

/**
 * UI state for the Profile tab.
 *
 * The screen has these shapes:
 *  - loggedOut        -> show a "sign in" prompt
 *  - loading          -> fetching the profile
 *  - profile / error  -> result of the fetch
 *
 * Once the profile loads, [selectedTab] drives whether the Posts or Likes
 * paged list is shown.
 */
data class ProfileUiState(
    val loggedOut: Boolean = false,
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val error: AppError? = null,
    val selectedTab: ProfileTab = ProfileTab.POSTS,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    // The posts/likes pagers depend on the user's own id, which is only
    // known after getMyProfile() succeeds. They're created lazily by
    // buildPagers() and exposed as nullable Flows until then.
    private var _posts: Flow<PagingData<Post>>? = null
    private var _likes: Flow<PagingData<Post>>? = null

    val posts: Flow<PagingData<Post>>? get() = _posts
    val likes: Flow<PagingData<Post>>? get() = _likes

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val loggedIn = tokenStore.getToken() != null
            if (!loggedIn) {
                _uiState.value = ProfileUiState(loggedOut = true, loading = false)
                return@launch
            }

            _uiState.update { it.copy(loading = true, error = null, loggedOut = false) }
            when (val result = userRepository.getMyProfile()) {
                is DataResult.Success -> {
                    buildPagers(result.data.id)
                    _uiState.update {
                        it.copy(loading = false, profile = result.data, error = null)
                    }
                }
                is DataResult.Error ->
                    _uiState.update { it.copy(loading = false, error = result.error) }
            }
        }
    }

    fun onTabSelected(tab: ProfileTab) {
        _uiState.update { it.copy(selectedTab = tab) }
    }

    /** Build the Posts and Likes pagers once the user's id is known. */
    private fun buildPagers(userId: Long) {
        if (_posts == null) {
            _posts = Pager(
                config = PagingConfig(pageSize = 20, enablePlaceholders = false),
                pagingSourceFactory = {
                    PostPagingSource { cursor ->
                        userRepository.getUserPosts(userId, cursor)
                    }
                },
            ).flow.cachedIn(viewModelScope)
        }
        if (_likes == null) {
            _likes = Pager(
                config = PagingConfig(pageSize = 20, enablePlaceholders = false),
                pagingSourceFactory = {
                    PostPagingSource { cursor ->
                        userRepository.getUserLikes(userId, cursor)
                    }
                },
            ).flow.cachedIn(viewModelScope)
        }
    }
}