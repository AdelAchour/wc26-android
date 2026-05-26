package com.adel.wc26.feature.matches.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.matches.domain.MatchFilter
import com.adel.wc26.feature.matches.domain.MatchRepository
import com.adel.wc26.feature.matches.domain.model.Match
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the matches list.
 *
 * [matches] is the list for the currently selected [filter]. [loading]
 * is true during a fetch; [error] is set if the fetch failed.
 */
data class MatchesUiState(
    val filter: MatchFilter = MatchFilter.UPCOMING,
    val matches: List<Match> = emptyList(),
    val loading: Boolean = true,
    val error: AppError? = null,
) {
    /** True when a successful load returned no matches for this filter. */
    val isEmpty: Boolean
        get() = !loading && error == null && matches.isEmpty()
}

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    init {
        load(MatchFilter.UPCOMING)
    }

    /** Switch the filter and reload. No-op if already on that filter. */
    fun onFilterSelected(filter: MatchFilter) {
        if (filter == _uiState.value.filter && !_uiState.value.loading) {
            // Already showing this filter — nothing to do.
            return
        }
        load(filter)
    }

    /** Retry the current filter (used by the error state). */
    fun retry() {
        load(_uiState.value.filter)
    }

    private fun load(filter: MatchFilter) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(filter = filter, loading = true, error = null)
            }
            when (val result = matchRepository.getMatches(filter)) {
                is DataResult.Success ->
                    _uiState.update {
                        it.copy(loading = false, matches = result.data, error = null)
                    }
                is DataResult.Error ->
                    _uiState.update {
                        it.copy(loading = false, error = result.error)
                    }
            }
        }
    }
}