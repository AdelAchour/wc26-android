package com.adel.wc26.core.network

import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

sealed interface AppStatus {
    object Normal : AppStatus
    data class ForceUpdate(val updateUrl: String, val minVersion: Int) : AppStatus
    object Maintenance : AppStatus
}

@Singleton
class AppStatusManager @Inject constructor() {
    private val _appStatus = MutableStateFlow<AppStatus>(AppStatus.Normal)
    val appStatus: StateFlow<AppStatus> = _appStatus.asStateFlow()

    fun updateStatus(status: AppStatus) {
        _appStatus.value = status
    }
}