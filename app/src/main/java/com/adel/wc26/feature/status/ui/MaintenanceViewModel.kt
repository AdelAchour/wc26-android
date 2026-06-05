package com.adel.wc26.feature.status.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.adel.wc26.core.network.AppStatus
import com.adel.wc26.core.network.AppStatusManager
import com.adel.wc26.core.network.SystemApi
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MaintenanceViewModel @Inject constructor(
    private val systemApi: SystemApi,
    private val appStatusManager: AppStatusManager,
) : ViewModel() {

    private val _isChecking = MutableStateFlow(false)
    val isChecking = _isChecking.asStateFlow()

    fun checkStatus() {
        viewModelScope.launch {
            _isChecking.value = true
            try {
                val status = systemApi.getSystemStatus()
                if (!status.maintenanceMode) {
                    appStatusManager.updateStatus(AppStatus.Normal)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isChecking.value = false
            }
        }
    }
}