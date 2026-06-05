package com.adel.wc26.feature.notifications.data

import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.notifications.domain.NotificationsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationsManager @Inject constructor(
    private val repository: NotificationsRepository,
) {
    private val _unreadCount = MutableStateFlow(0)
    val unreadCount: StateFlow<Int> = _unreadCount.asStateFlow()

    suspend fun refreshUnreadCount() {
        when (val result = repository.getUnreadCount()) {
            is DataResult.Success -> {
                _unreadCount.value = result.data
            }
            is DataResult.Error -> {
                // Ignore failure (e.g. offline or unauthorized)
            }
        }
    }

    fun setUnreadCount(count: Int) {
        _unreadCount.value = count
    }

    fun decrementUnreadCount() {
        if (_unreadCount.value > 0) {
            _unreadCount.value -= 1
        }
    }

    fun clearCount() {
        _unreadCount.value = 0
    }
}