package com.adel.wc26.feature.notifications.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.paging.cachedIn
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.notifications.data.NotificationsManager
import com.adel.wc26.feature.notifications.data.NotificationsPagingSource
import com.adel.wc26.feature.notifications.domain.NotificationsRepository
import com.adel.wc26.feature.notifications.domain.Notification
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class NotificationsViewModel @Inject constructor(
    private val repository: NotificationsRepository,
    private val notificationsManager: NotificationsManager,
    private val tokenStore: TokenStore,
) : ViewModel() {

    val isLoggedIn: Flow<Boolean> = tokenStore.isLoggedInFlow

    private var currentPagingSource: NotificationsPagingSource? = null

    val notifications: Flow<PagingData<Notification>> = Pager(
        config = PagingConfig(pageSize = 20, enablePlaceholders = false),
        pagingSourceFactory = {
            val source = NotificationsPagingSource { limit, offset ->
                repository.getNotifications(limit, offset)
            }
            currentPagingSource = source
            source
        }
    ).flow.cachedIn(viewModelScope)

    init {
        viewModelScope.launch {
            notificationsManager.refreshUnreadCount()
        }
    }

    fun refreshCount() {
        viewModelScope.launch {
            notificationsManager.refreshUnreadCount()
        }
    }

    fun markAsRead(notification: Notification) {
        if (notification.isRead) return
        viewModelScope.launch {
            notificationsManager.decrementUnreadCount()
            when (repository.markAsRead(notification.id)) {
                is DataResult.Success -> {
                    currentPagingSource?.invalidate()
                }
                is DataResult.Error -> {
                    notificationsManager.refreshUnreadCount()
                }
            }
        }
    }

    fun markAllAsRead(onFailure: () -> Unit = {}) {
        viewModelScope.launch {
            notificationsManager.clearCount()
            when (repository.markAllAsRead()) {
                is DataResult.Success -> {
                    currentPagingSource?.invalidate()
                }
                is DataResult.Error -> {
                    notificationsManager.refreshUnreadCount()
                    onFailure()
                }
            }
        }
    }
}