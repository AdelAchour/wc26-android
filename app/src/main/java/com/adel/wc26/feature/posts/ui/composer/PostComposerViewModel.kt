package com.adel.wc26.feature.posts.ui.composer

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.posts.domain.post.PostRepository
import com.adel.wc26.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the post composer.
 *
 * The backend caps post content at 500 chars; [MAX_LENGTH] mirrors that
 * so the UI can show a counter and block over-length submits.
 */
data class ComposerUiState(
    val text: String = "",
    val submitting: Boolean = false,
    val error: AppError? = null,
    val done: Boolean = false,
) {
    val charsLeft: Int get() = MAX_LENGTH - text.length
    val canSubmit: Boolean
        get() = text.isNotBlank() && text.length <= MAX_LENGTH && !submitting

    companion object {
        const val MAX_LENGTH = 500
    }
}

@HiltViewModel
class PostComposerViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val postRepository: PostRepository,
) : ViewModel() {

    private val matchId: Long =
        savedStateHandle.toRoute<Destinations.PostComposer>().matchId

    private val _uiState = MutableStateFlow(ComposerUiState())
    val uiState: StateFlow<ComposerUiState> = _uiState.asStateFlow()

    fun onTextChange(value: String) {
        // Hard cap so the field can't exceed the limit.
        if (value.length <= ComposerUiState.MAX_LENGTH) {
            _uiState.update { it.copy(text = value, error = null) }
        }
    }

    fun submit() {
        val state = _uiState.value
        if (!state.canSubmit) return

        viewModelScope.launch {
            _uiState.update { it.copy(submitting = true, error = null) }
            when (val result = postRepository.createPost(matchId, state.text.trim())) {
                is DataResult.Success ->
                    _uiState.update { it.copy(submitting = false, done = true) }
                is DataResult.Error ->
                    _uiState.update { it.copy(submitting = false, error = result.error) }
            }
        }
    }
}