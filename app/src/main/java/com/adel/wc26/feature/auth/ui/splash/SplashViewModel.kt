package com.adel.wc26.feature.auth.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adel.wc26.BuildConfig
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.network.AppStatus
import com.adel.wc26.core.network.AppStatusManager
import com.adel.wc26.core.network.SystemApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

enum class SplashRoute { Undecided, LoggedIn, LoggedOut }

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val tokenStore: TokenStore,
    private val systemApi: SystemApi,
    private val appStatusManager: AppStatusManager,
) : ViewModel() {

    private val _route = MutableStateFlow(SplashRoute.Undecided)
    val route: StateFlow<SplashRoute> = _route.asStateFlow()

    init {
        viewModelScope.launch {
            // 1. Fetch system status first
            try {
                val status = systemApi.getSystemStatus()
                if (status.maintenanceMode) {
                    appStatusManager.updateStatus(AppStatus.Maintenance)
                    return@launch // Stop initialization flow
                }
                if (BuildConfig.VERSION_CODE < status.minAndroidVersion) {
                    appStatusManager.updateStatus(AppStatus.ForceUpdate(updateUrl = status.androidUpdateUrl, minVersion = status.minAndroidVersion))
                    return@launch // Stop initialization flow
                }
            } catch (e: Exception) {
                // If offline or system-status fails, proceed gracefully
                e.printStackTrace()
            }

            // 2. Normal session validation
            val isValid = tokenStore.hasValidSession()
            if (!isValid) {
                tokenStore.clear()
                _route.value = SplashRoute.LoggedOut
            } else {
                _route.value = SplashRoute.LoggedIn
            }
        }
    }
}