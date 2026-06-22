package com.adel.wc26.feature.auth.ui.forgotpassword

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.auth.domain.AuthRepository
import com.adel.wc26.feature.auth.ui.AuthValidation
import com.adel.wc26.feature.auth.ui.ValidationError
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Forgot Password screen.
 *
 * [emailError] is the inline per-field validation error;
 * [formError] is the failed-submission error (network, etc.).
 * [success] flips true once the reset code request succeeds —
 * the screen observes it to navigate to the Reset Password screen.
 */
data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: ValidationError? = null,
    val formError: AppError? = null,
    val loading: Boolean = false,
    val success: Boolean = false,
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && !loading
}

@HiltViewModel
class ForgotPasswordViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, formError = null) }
    }

    fun submit() {
        val state = _uiState.value

        val emailError = AuthValidation.emailError(state.email)
        if (emailError != null) {
            _uiState.update { it.copy(emailError = emailError) }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, formError = null) }
            when (val result = authRepository.forgotPassword(state.email)) {
                is DataResult.Success ->
                    _uiState.update { it.copy(loading = false, success = true) }
                is DataResult.Error ->
                    _uiState.update {
                        it.copy(loading = false, formError = result.error)
                    }
            }
        }
    }
}
