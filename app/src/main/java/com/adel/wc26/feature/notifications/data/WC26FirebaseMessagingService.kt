package com.adel.wc26.feature.notifications.data

import com.adel.wc26.feature.notifications.domain.NotificationsRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class WC26FirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var notificationsManager: NotificationsManager

    @Inject
    lateinit var repository: NotificationsRepository

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        serviceScope.launch {
            repository.registerPushToken(token)
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        // Refresh the unread count reactively when a push arrives
        serviceScope.launch {
            notificationsManager.refreshUnreadCount()
        }
    }
}
