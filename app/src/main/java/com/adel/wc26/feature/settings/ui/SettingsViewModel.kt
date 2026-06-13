package com.adel.wc26.feature.settings.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.datastore.ThemeStore
import com.adel.wc26.core.datastore.DarkThemeConfig
import com.adel.wc26.feature.auth.domain.AuthRepository
import com.adel.wc26.feature.notifications.data.NotificationsManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * UI state for the Settings tab.
 *
 * [loggedIn] decides whether the logout action is shown.
 * [loggedOut] flips true after logout completes — the screen observes it
 * to route back to the welcome flow.
 */
data class SettingsUiState(
    val loggedIn: Boolean = false,
    val loggedOut: Boolean = false,
    val themeConfig: DarkThemeConfig = DarkThemeConfig.FOLLOW_SYSTEM,
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val authRepository: AuthRepository,
    private val tokenStore: TokenStore,
    private val themeStore: ThemeStore,
    private val notificationsManager: NotificationsManager,
) : ViewModel() {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val loggedIn = tokenStore.getToken() != null
            _uiState.update { it.copy(loggedIn = loggedIn) }
        }
        viewModelScope.launch {
            themeStore.themeFlow.collect { config ->
                _uiState.update { it.copy(themeConfig = config) }
            }
        }
    }

    fun logout() {
        viewModelScope.launch {
            notificationsManager.unregisterCurrentToken()
            authRepository.logout()
            _uiState.update { it.copy(loggedOut = true) }
        }
    }

    fun setTheme(config: DarkThemeConfig) {
        viewModelScope.launch {
            themeStore.setTheme(config)
        }
    }
}