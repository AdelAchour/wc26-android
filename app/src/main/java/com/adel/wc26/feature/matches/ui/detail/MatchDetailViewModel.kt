package com.adel.wc26.feature.matches.ui.detail

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.matches.domain.MatchRepository
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.posts.data.post.PostPagingSource
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.domain.post.PostRepository
import com.adel.wc26.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import androidx.navigation.toRoute
import javax.inject.Inject

/**
 * State for the match-detail header (the match info itself).
 * The posts thread below is a separate paged Flow.
 */
data class MatchDetailUiState(
    val loading: Boolean = true,
    val match: Match? = null,
    val error: AppError? = null,
    val isLoggedIn: Boolean = false,
)

@HiltViewModel
class MatchDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val matchRepository: MatchRepository,
    private val postRepository: PostRepository,
    tokenStore: TokenStore,
) : ViewModel() {

    /** The matchId comes from the type-safe MatchDetail route. */
    private val matchId: Long =
        savedStateHandle.toRoute<Destinations.MatchDetail>().matchId

    private val _uiState = MutableStateFlow(MatchDetailUiState())
    val uiState: StateFlow<MatchDetailUiState> = _uiState.asStateFlow()

    /**
     * The match's posts thread, as a paged Flow. cachedIn keeps the paged
     * data alive across configuration changes.
     */
    val posts: Flow<PagingData<Post>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            PostPagingSource { cursor ->
                postRepository.getMatchPosts(matchId, cursor)
            }
        },
    ).flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoggedIn = tokenStore.getToken() != null) }
        }
        loadMatch()
    }

    fun loadMatch() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            when (val result = matchRepository.getMatch(matchId)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(loading = false, match = result.data) }
                is DataResult.Error ->
                    _uiState.update { it.copy(loading = false, error = result.error) }
            }
        }
    }
}