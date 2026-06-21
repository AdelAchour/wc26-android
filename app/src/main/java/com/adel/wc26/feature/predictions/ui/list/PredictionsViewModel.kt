package com.adel.wc26.feature.predictions.ui.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.matches.domain.MatchFilter
import com.adel.wc26.feature.matches.domain.MatchRepository
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.domain.model.MatchStatus
import com.adel.wc26.feature.predictions.data.PredictionUpdateNotifier
import com.adel.wc26.feature.predictions.domain.PredictionRepository
import com.adel.wc26.feature.predictions.domain.model.LeaderboardEntry
import com.adel.wc26.feature.predictions.domain.model.MyRank
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import javax.inject.Inject

/**
 * Drives the Predictions tab: today's open picks (derived from the embedded
 * match prediction) + the leaderboard.
 */
@HiltViewModel
class PredictionsViewModel @Inject constructor(
    private val matchRepository: MatchRepository,
    private val predictionRepository: PredictionRepository,
    private val predictionUpdateNotifier: PredictionUpdateNotifier,
    private val tokenStore: TokenStore,
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val refreshing: Boolean = false,
        val error: AppError? = null,
        val todayPicks: List<Match> = emptyList(),
        val leaderboard: List<LeaderboardEntry> = emptyList(),
        val me: MyRank? = null,
        val isLoggedIn: Boolean = false,
    )

    private val _uiState = MutableStateFlow(UiState())
    val uiState: StateFlow<UiState> = _uiState.asStateFlow()

    init {
        load()

        // Reload when auth changes so the leaderboard "you" row + picks refresh.
        viewModelScope.launch {
            var previous: Boolean? = null
            tokenStore.tokenFlow.collect { token ->
                val loggedIn = token != null
                _uiState.update { it.copy(isLoggedIn = loggedIn) }
                if (previous != null && previous != loggedIn) load()
                previous = loggedIn
            }
        }

        // A freshly predicted match drops out of today's open picks.
        viewModelScope.launch {
            predictionUpdateNotifier.predictionUpdated.collect { prediction ->
                _uiState.update { state ->
                    state.copy(todayPicks = state.todayPicks.filterNot { it.id == prediction.matchId })
                }
            }
        }
    }

    fun refresh() = load(isRefresh = true)

    fun retry() = load()

    private fun load(isRefresh: Boolean = false) {
        viewModelScope.launch {
            _uiState.update {
                if (isRefresh) it.copy(refreshing = true) else it.copy(loading = true, error = null)
            }

            val picks = when (val matches = matchRepository.getMatches(MatchFilter.UPCOMING)) {
                is DataResult.Success -> matches.data.filterTodayOpenUnpicked()
                is DataResult.Error -> emptyList()
            }

            when (val leaderboard = predictionRepository.getLeaderboard(limit = LEADERBOARD_LIMIT)) {
                is DataResult.Success -> _uiState.update {
                    it.copy(
                        loading = false,
                        refreshing = false,
                        error = null,
                        todayPicks = picks,
                        leaderboard = leaderboard.data.entries,
                        me = leaderboard.data.me,
                    )
                }
                is DataResult.Error -> _uiState.update {
                    it.copy(loading = false, refreshing = false, error = leaderboard.error)
                }
            }
        }
    }

    /** Today's matches that are still open and not yet predicted. */
    private fun List<Match>.filterTodayOpenUnpicked(): List<Match> {
        val zone = ZoneId.systemDefault()
        val today = LocalDate.now(zone)
        val now = Instant.now()
        return filter { m ->
            m.prediction == null &&
                m.status == MatchStatus.SCHEDULED &&
                m.homeTeamCode != null && m.awayTeamCode != null &&
                m.kickoffAt.isAfter(now) &&
                m.kickoffAt.atZone(zone).toLocalDate() == today
        }
    }

    private companion object {
        const val LEADERBOARD_LIMIT = 50
    }
}
