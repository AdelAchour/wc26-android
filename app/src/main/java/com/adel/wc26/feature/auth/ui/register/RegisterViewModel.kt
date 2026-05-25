package com.adel.wc26.feature.auth.ui.register

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
 * UI state for the register screen.
 *
 * Per-field errors are [ValidationError]s; [formError] is an [AppError]
 * from a failed submission. Both are semantic — the screen resolves them
 * to text via stringResource.
 */
data class RegisterUiState(
    val email: String = "",
    val username: String = "",
    val displayName: String = "",
    val password: String = "",
    val emailError: ValidationError? = null,
    val usernameError: ValidationError? = null,
    val displayNameError: ValidationError? = null,
    val passwordError: ValidationError? = null,
    val formError: AppError? = null,
    val loading: Boolean = false,
    val success: Boolean = false,
) {
    val canSubmit: Boolean
        get() = email.isNotBlank() && username.isNotBlank() &&
                displayName.isNotBlank() && password.isNotBlank() && !loading
}

@HiltViewModel
class RegisterViewModel @Inject constructor(
    private val authRepository: AuthRepository,
) : ViewModel() {

    private val _uiState = MutableStateFlow(RegisterUiState())
    val uiState: StateFlow<RegisterUiState> = _uiState.asStateFlow()

    fun onEmailChange(v: String) =
        _uiState.update { it.copy(email = v, emailError = null, formError = null) }

    fun onUsernameChange(v: String) =
        _uiState.update { it.copy(username = v, usernameError = null, formError = null) }

    fun onDisplayNameChange(v: String) =
        _uiState.update { it.copy(displayName = v, displayNameError = null, formError = null) }

    fun onPasswordChange(v: String) =
        _uiState.update { it.copy(password = v, passwordError = null, formError = null) }

    fun submit() {
        val state = _uiState.value

        // Full client-side validation, mirroring the backend rules.
        val emailError = AuthValidation.emailError(state.email)
        val usernameError = AuthValidation.usernameError(state.username)
        val displayNameError = AuthValidation.displayNameError(state.displayName)
        val passwordError = AuthValidation.passwordError(state.password)

        if (listOf(emailError, usernameError, displayNameError, passwordError)
                .any { it != null }
        ) {
            _uiState.update {
                it.copy(
                    emailError = emailError,
                    usernameError = usernameError,
                    displayNameError = displayNameError,
                    passwordError = passwordError,
                )
            }
            return
        }

        viewModelScope.launch {
            _uiState.update { it.copy(loading = true, formError = null) }
            val result = authRepository.register(
                email = state.email,
                username = state.username,
                password = state.password,
                displayName = state.displayName,
            )
            when (result) {
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