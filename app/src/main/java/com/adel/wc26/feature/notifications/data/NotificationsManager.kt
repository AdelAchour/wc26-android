package com.adel.wc26.feature.notifications.data

import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.notifications.domain.NotificationsRepository
import com.google.android.gms.tasks.Tasks
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.withContext
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

    suspend fun registerCurrentToken() = withContext(Dispatchers.IO) {
        try {
            val token = Tasks.await(FirebaseMessaging.getInstance().token)
            repository.registerPushToken(token)
        } catch (e: Exception) {
            // Fails silently if Firebase is unconfigured or Play Services are missing
        }
    }

    suspend fun unregisterCurrentToken() = withContext(Dispatchers.IO) {
        try {
            val token = Tasks.await(FirebaseMessaging.getInstance().token)
            repository.unregisterPushToken(token)
            Tasks.await(FirebaseMessaging.getInstance().deleteToken())
        } catch (e: Exception) {
            // Fails silently if Firebase is unconfigured or Play Services are missing
        }
    }
}