package com.adel.wc26

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.navigation.WC26NavHost
import dagger.hilt.android.AndroidEntryPoint
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.core.datastore.ThemeStore
import com.adel.wc26.core.datastore.DarkThemeConfig
import com.adel.wc26.core.network.AppStatusManager
import com.adel.wc26.feature.notifications.data.NotificationsManager
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableSharedFlow

/**
 * The single Activity. Hosts the entire Compose UI and the navigation graph.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var tokenStore: TokenStore
    @Inject
    lateinit var themeStore: ThemeStore
    @Inject
    lateinit var appStatusManager: AppStatusManager
    @Inject
    lateinit var notificationsManager: NotificationsManager
    // Bridges new deep-link intents into the Compose nav graph.
    private val deepLinkIntents = MutableSharedFlow<Intent>(extraBufferCapacity = 1)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        requestNotificationPermission()
        // Normalize a notification's postId extra into a deep-link URI before
        // the NavHost composes and the auto-handler evaluates activity.intent.
        intent?.applyNotificationDeepLink()
        setContent {
            val themeConfig by themeStore.themeFlow.collectAsState(initial = DarkThemeConfig.FOLLOW_SYSTEM)
            val useDarkTheme = when (themeConfig) {
                DarkThemeConfig.FOLLOW_SYSTEM -> isSystemInDarkTheme()
                DarkThemeConfig.LIGHT -> false
                DarkThemeConfig.DARK -> true
            }

            WC26Theme(darkTheme = useDarkTheme) {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    WC26NavHost(
                        tokenStore = tokenStore,
                        appStatusManager = appStatusManager,
                        notificationsManager = notificationsManager,
                        deepLinkIntents = deepLinkIntents,
                    )
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        intent.applyNotificationDeepLink()
        setIntent(intent)
        deepLinkIntents.tryEmit(intent)
    }

    /**
     * FCM delivers its data payload as Intent extras, not as a URI. Translate a
     * "postId" extra into the deep-link URI the nav graph already matches, so push
     * routing reuses the same declarative navDeepLink path as browser/app links.
     * No-op if the intent is already a URI deep link or carries no postId.
     */
    private fun Intent.applyNotificationDeepLink() {
        if (data != null) return
        getStringExtra(EXTRA_POST_ID)?.toLongOrNull()?.let {
            data = Uri.parse("wc26://posts/$it")
            return
        }
        getStringExtra(EXTRA_MATCH_ID)?.toLongOrNull()?.let {
            data = Uri.parse("wc26://matches/$it")
        }
    }

    private fun requestNotificationPermission() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            val permission = android.Manifest.permission.POST_NOTIFICATIONS
            if (checkSelfPermission(permission) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                requestPermissions(arrayOf(permission), 101)
            }
        }
    }

    companion object {
        private const val EXTRA_POST_ID = "postId"
        private const val EXTRA_MATCH_ID = "matchId"
    }
}