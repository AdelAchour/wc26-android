package com.adel.wc26.feature.auth.ui.login

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
 * UI state for the login screen.
 *
 * [emailError] / [passwordError] are inline per-field validation errors;
 * [formError] is the failed-submission error (wrong credentials, network).
 * Both are semantic types, resolved to text by the screen via stringResource.
 * [success] flips true once login succeeds — the screen observes it to
 * navigate away.
 */
data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val emailError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val formError: AppError? = null,
    val loading: Boolean = false,
    val success: Boolean = false,
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && password.isNotBlank() && !loading
}

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.update { it.copy(email = value, emailError = null, formError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(password = value, passwordError = null, formError = null) }
    }

    fun submit() {
        val state = _uiState.value

        // Light client-side check — just "is it filled in / shaped right".
        // The backend gives the real "incorrect email or password" check.
        val emailError = AuthValidation.emailError(state.email)
        val passwordError = AuthValidation.passwordError(state.password)

        if (emailError != null || passwordError != null) {
            _uiState.update {
                it.copy(emailError = emailError, passwordError = passwordError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, formError = null) }
            when (val result = authRepository.login(state.email, state.password)) {
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