package com.adel.wc26.feature.profile.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.result.AppError
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.profile.domain.UserProfile
import com.adel.wc26.feature.profile.domain.UserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Profile tab.
 *
 * The screen has three meaningful shapes:
 *  - loggedOut          → show a "sign in" prompt
 *  - loading            → fetching the profile
 *  - profile / error    → result of the fetch
 */
data class ProfileUiState(
    val loggedOut: Boolean = false,
    val loading: Boolean = true,
    val profile: UserProfile? = null,
    val error: AppError? = null,
)

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val userRepository: UserRepository,
    private val tokenStore: TokenStore,
) : ViewModel() {

    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            val loggedIn = tokenStore.getToken() != null
            if (!loggedIn) {
                _uiState.value = ProfileUiState(loggedOut = true, loading = false)
                return@launch
            }

            _uiState.update { it.copy(loading = true, error = null, loggedOut = false) }
            when (val result = userRepository.getMyProfile()) {
                is DataResult.Success ->
                    _uiState.update {
                        it.copy(loading = false, profile = result.data, error = null)
                    }
                is DataResult.Error ->
                    _uiState.update {
                        it.copy(loading = false, error = result.error)
                    }
            }
        }
    }
}