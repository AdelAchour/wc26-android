package com.adel.wc26.feature.matches.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.matches.data.MatchUpdateNotifier
import com.adel.wc26.feature.matches.domain.MatchFilter
import com.adel.wc26.feature.matches.domain.MatchRepository
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.domain.model.MatchStatus
import com.adel.wc26.feature.predictions.domain.PredictionRepository
import com.adel.wc26.feature.predictions.domain.model.Prediction
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
    val isLoggedIn: Boolean = false,
    val predictions: Map<Long, Prediction> = emptyMap(),
) {
    /** True when a successful load returned no matches for this filter. */
    val isEmpty: Boolean
        get() = !loading && error == null && matches.isEmpty()
}

@HiltViewModel
class MatchesViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val matchUpdateNotifier: MatchUpdateNotifier,
    private val predictionRepository: PredictionRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(MatchesUiState())
    val uiState: StateFlow<MatchesUiState> = _uiState.asStateFlow()

    init {
        load(MatchFilter.UPCOMING)

        // Track login + load the user's predictions for the per-card chips.
        viewModelScope.launch {
            tokenStore.tokenFlow.collect { token ->
                val loggedIn = token != null
                _uiState.update { it.copy(isLoggedIn = loggedIn) }
                if (loggedIn) loadPredictions()
                else _uiState.update { it.copy(predictions = emptyMap()) }
            }
        }

        // Listen for live updates and update the matching item in the list
        // Listen for live updates and update the matching item in the list
        viewModelScope.launch {
            matchUpdateNotifier.matchUpdated.collect { updatedMatch ->
                _uiState.update { state ->
                    val updatedList = state.matches.mapNotNull { match ->
                        if (match.id == updatedMatch.id) {
                            // Only keep the match in the list if it matches the current filter
                            if (state.filter.matchesStatus(updatedMatch.status)) {
                                updatedMatch
                            } else {
                                null // Removed from list
                            }
                        } else {
                            match
                        }
                    }
                    state.copy(matches = updatedList)
                }
            }
        }
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

    /** Reflect a freshly-saved prediction in the chips without a round-trip. */
    fun onPredictionSaved(prediction: Prediction) {
        _uiState.update { it.copy(predictions = it.predictions + (prediction.matchId to prediction)) }
    }

    private fun loadPredictions() {
        viewModelScope.launch {
            when (val result = predictionRepository.getMyPredictions()) {
                is DataResult.Success ->
                    _uiState.update { it.copy(predictions = result.data.associateBy { p -> p.matchId }) }
                is DataResult.Error -> Unit // non-critical; chips just won't show picks
            }
        }
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

private fun MatchFilter.matchesStatus(status: MatchStatus): Boolean = when (this) {
    MatchFilter.ALL -> true
    MatchFilter.UPCOMING -> status == MatchStatus.SCHEDULED
    MatchFilter.LIVE -> status == MatchStatus.LIVE
    MatchFilter.FINISHED -> status == MatchStatus.FINISHED
}