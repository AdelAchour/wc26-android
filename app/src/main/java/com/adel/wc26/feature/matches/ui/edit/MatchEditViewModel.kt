package com.adel.wc26.feature.matches.ui.edit

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.core.util.TeamCodes
import com.adel.wc26.feature.matches.domain.MatchRepository
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.domain.model.MatchStatus
import com.adel.wc26.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

data class MatchEditUiState(
    val loading: Boolean = true,
    val saving: Boolean = false,
    val match: Match? = null,
    val error: AppError? = null,
    val success: Boolean = false,
    val selectedStatus: MatchStatus = MatchStatus.SCHEDULED,
    val updateScores: Boolean = false,
    val homeScore: Int = 0,
    val awayScore: Int = 0,
    val homeTeam: String? = null,
    val awayTeam: String? = null,
)

@HiltViewModel
class MatchEditViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val matchRepository: MatchRepository,
) : ViewModel() {

    private val matchId: Long =
        savedStateHandle.toRoute<Destinations.MatchEdit>().matchId

    private val _uiState = MutableStateFlow(MatchEditUiState())
    val uiState: StateFlow<MatchEditUiState> = _uiState.asStateFlow()

    init {
        loadMatch()
    }

    fun loadMatch() {
        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, error = null) }
            when (val result = matchRepository.getMatch(matchId)) {
                is DataResult.Success -> {
                    val match = result.data
                    _uiState.update {
                        it.copy(
                            loading = false,
                            match = match,
                            selectedStatus = match.status,
                            updateScores = false,
                            homeScore = match.homeScore ?: 0,
                            awayScore = match.awayScore ?: 0,
                            homeTeam = if (TeamCodes.fromTeamName(match.homeTeam) != null) match.homeTeam else null,
                            awayTeam = if (TeamCodes.fromTeamName(match.awayTeam) != null) match.awayTeam else null,
                        )
                    }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(loading = false, error = result.error) }
                }
            }
        }
    }

    fun onStatusSelected(status: MatchStatus) {
        _uiState.update { state ->
            val initialStatus = state.match?.status ?: MatchStatus.SCHEDULED

            val shouldUpdateScores = when (status) {
                MatchStatus.SCHEDULED -> false
                MatchStatus.LIVE, MatchStatus.FINISHED -> {
                    if (initialStatus == MatchStatus.SCHEDULED) {
                        // Changing from scheduled to live/finished forces score input
                        true
                    } else {
                        // Keep current toggle if it was already live/finished
                        state.updateScores
                    }
                }
            }

            state.copy(
                selectedStatus = status,
                updateScores = shouldUpdateScores
            )
        }
    }

    fun onUpdateScoresToggled(update: Boolean) {
        _uiState.update { state ->
            val match = state.match
            state.copy(
                updateScores = update,
                // Revert to initial scores when switch is toggled off
                homeScore = if (!update && match != null) (match.homeScore ?: 0) else state.homeScore,
                awayScore = if (!update && match != null) (match.awayScore ?: 0) else state.awayScore
            )
        }
    }

    fun onHomeScoreChanged(score: Int) {
        if (score >= 0) {
            _uiState.update { it.copy(homeScore = score) }
        }
    }

    fun onAwayScoreChanged(score: Int) {
        if (score >= 0) {
            _uiState.update { it.copy(awayScore = score) }
        }
    }

    fun onHomeTeamChanged(team: String?) {
        _uiState.update { it.copy(homeTeam = team) }
    }
    fun onAwayTeamChanged(team: String?) {
        _uiState.update { it.copy(awayTeam = team) }
    }

    fun saveChanges() {
        val state = _uiState.value
        val match = state.match ?: return
        val statusToUpdate = if (state.selectedStatus != match.status) state.selectedStatus else null
        val (homeScoreToUpdate, awayScoreToUpdate) = if (state.updateScores) {
            Pair(state.homeScore, state.awayScore)
        } else {
            Pair(null, null)
        }
        val initialHomeTeam = if (TeamCodes.fromTeamName(match.homeTeam) != null) match.homeTeam else null
        val initialAwayTeam = if (TeamCodes.fromTeamName(match.awayTeam) != null) match.awayTeam else null
        val homeTeamToUpdate = if (state.homeTeam != initialHomeTeam) state.homeTeam else null
        val awayTeamToUpdate = if (state.awayTeam != initialAwayTeam) state.awayTeam else null

        // Detect if changes actually occurred
        val statusChanged = statusToUpdate != null
        val scoresChanged = state.updateScores && (state.homeScore != (match.homeScore ?: -1) || state.awayScore != (match.awayScore ?: -1))
        val teamChanged = state.homeTeam != initialHomeTeam || state.awayTeam != initialAwayTeam

        if (!statusChanged && !scoresChanged && !teamChanged) {
            _uiState.update { it.copy(success = true) }
            return
        }
        viewModelScope.launch {
            _uiState.update { it.copy(saving = true, error = null) }
            val result = matchRepository.updateMatch(
                id = matchId,
                homeScore = homeScoreToUpdate,
                awayScore = awayScoreToUpdate,
                status = statusToUpdate,
                homeTeam = homeTeamToUpdate,
                awayTeam = awayTeamToUpdate,
            )
            when (result) {
                is DataResult.Success -> {
                    _uiState.update { it.copy(saving = false, success = true) }
                }
                is DataResult.Error -> {
                    _uiState.update { it.copy(saving = false, error = result.error) }
                }
            }
        }
    }
}