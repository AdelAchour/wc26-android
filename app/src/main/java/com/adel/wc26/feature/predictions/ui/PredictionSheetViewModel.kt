package com.adel.wc26.feature.predictions.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.predictions.data.PredictionUpdateNotifier
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
 * Drives the shared prediction bottom sheet: the two score steppers and the
 * upsert call. Re-seeded each time the sheet opens via [configure].
 */
@HiltViewModel
class PredictionSheetViewModel @Inject constructor(
    private val repository: PredictionRepository,
    private val predictionUpdateNotifier: PredictionUpdateNotifier,
) : ViewModel() {

    data class UiState(
        val matchId: Long = 0,
        val home: Int = 0,
        val away: Int = 0,
        val submitting: Boolean = false,
        val error: AppError? = null,
        val saved: Prediction? = null,
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    /** Seed the steppers from an existing prediction (or 0–0 for a new one). */
    fun configure(matchId: Long, initialHome: Int?, initialAway: Int?) {
        _state.value = UiState(
            matchId = matchId,
            home = initialHome ?: 0,
            away = initialAway ?: 0,
        )
    }

    fun incHome() = _state.update { it.copy(home = (it.home + 1).coerceAtMost(MAX_GOALS), error = null) }
    fun decHome() = _state.update { it.copy(home = (it.home - 1).coerceAtLeast(0), error = null) }
    fun incAway() = _state.update { it.copy(away = (it.away + 1).coerceAtMost(MAX_GOALS), error = null) }
    fun decAway() = _state.update { it.copy(away = (it.away - 1).coerceAtLeast(0), error = null) }

    fun submit() {
        val current = _state.value
        viewModelScope.launch {
            _state.update { it.copy(submitting = true, error = null) }
            when (val result = repository.upsertPrediction(current.matchId, current.home, current.away)) {
                is DataResult.Success -> {
                    // Broadcast so every screen showing this match updates live.
                    predictionUpdateNotifier.notifyPredictionUpdated(result.data)
                    _state.update { it.copy(submitting = false, saved = result.data) }
                }
                is DataResult.Error -> _state.update { it.copy(submitting = false, error = result.error) }
            }
        }
    }

    /** Clear the one-shot saved signal after the host has consumed it. */
    fun consumeSaved() = _state.update { it.copy(saved = null) }

    private companion object {
        const val MAX_GOALS = 20
    }
}
