package com.adel.wc26.feature.auth.ui.resetpassword

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.auth.domain.AuthRepository
import com.adel.wc26.feature.auth.ui.AuthValidation
import com.adel.wc26.feature.auth.ui.ValidationError
import com.adel.wc26.navigation.Destinations
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Reset Password screen.
 *
 * [codeError] / [passwordError] are inline per-field validation errors;
 * [formError] is the failed-submission error (invalid code, network, etc.).
 * [success] flips true once the password is reset — the screen observes
 * it to navigate back to login.
 */
data class ResetPasswordUiState(
    val email: String = "",
    val code: String = "",
    val newPassword: String = "",
    val codeError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val formError: AppError? = null,
    val loading: Boolean = false,
    val success: Boolean = false,
) {
    val canSubmit: Boolean
        get() = code.isNotBlank() && newPassword.isNotBlank() && !loading
}

@HiltViewModel
class ResetPasswordViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val args = savedStateHandle.toRoute<Destinations.ResetPassword>()

    private val _uiState = MutableStateFlow(ResetPasswordUiState(email = args.email))
    val uiState: StateFlow<ResetPasswordUiState> = _uiState.asStateFlow()

    fun onCodeChange(value: String) {
        _uiState.update { it.copy(code = value, codeError = null, formError = null) }
    }

    fun onPasswordChange(value: String) {
        _uiState.update { it.copy(newPassword = value, passwordError = null, formError = null) }
    }

    fun submit() {
        val state = _uiState.value

        val codeError = AuthValidation.codeError(state.code)
        val passwordError = AuthValidation.passwordError(state.newPassword)

        if (codeError != null || passwordError != null) {
            _uiState.update {
                it.copy(codeError = codeError, passwordError = passwordError)
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, formError = null) }
            when (val result = authRepository.resetPassword(
                email = state.email,
                code = state.code,
                newPassword = state.newPassword,
            )) {
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
