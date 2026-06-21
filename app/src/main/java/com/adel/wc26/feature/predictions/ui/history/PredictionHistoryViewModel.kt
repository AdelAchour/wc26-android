package com.adel.wc26.feature.predictions.ui.history

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.predictions.domain.PredictionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/** Loads a user's prediction history (finished matches) for the history sheet. */
@HiltViewModel
class PredictionHistoryViewModel @Inject constructor(
    private val repository: PredictionRepository,
) : ViewModel() {

    data class UiState(
        val loading: Boolean = true,
        val error: AppError? = null,
        val items: List<Match> = emptyList(),
    )

    private val _state = MutableStateFlow(UiState())
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun load(userId: Long) {
        viewModelScope.launch {
            _state.update { it.copy(loading = true, error = null) }
            when (val result = repository.getUserPredictionHistory(userId)) {
                is DataResult.Success -> _state.update { it.copy(loading = false, items = result.data) }
                is DataResult.Error -> _state.update { it.copy(loading = false, error = result.error) }
            }
        }
    }
}
