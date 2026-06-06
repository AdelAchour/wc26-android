package com.adel.wc26

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.navigation.WC26NavHost
import dagger.hilt.android.AndroidEntryPoint
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.network.AppStatusManager
import com.adel.wc26.feature.notifications.data.NotificationsManager
import javax.inject.Inject

/**
 * The single Activity. Hosts the entire Compose UI and the navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenStore: TokenStore
    @Inject
    lateinit var appStatusManager: AppStatusManager
    @Inject
    lateinit var notificationsManager: NotificationsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WC26Theme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    WC26NavHost(
                        tokenStore = tokenStore,
                        appStatusManager = appStatusManager,
                        notificationsManager = notificationsManager
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent) // Required to deliver background deep links to NavController
    }
}