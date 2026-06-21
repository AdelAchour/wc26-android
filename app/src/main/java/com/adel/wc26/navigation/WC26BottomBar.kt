package com.adel.wc26.navigation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.EmojiEvents
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.SportsSoccer
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination.Companion.hierarchy
import com.adel.wc26.core.designsystem.theme.Spacing

/**
 * The five top-level tabs shown in the bottom navigation bar.
 * Each pairs a label + icon with its type-safe destination.
 */
enum class TopLevelTab(
    val label: String,
    val icon: ImageVector,
    val route: Any,
) {
    MATCHES("Matches", Icons.Outlined.SportsSoccer, Destinations.Matches()),
    FEED("Feed", Icons.Outlined.ChatBubbleOutline, Destinations.Feed),
    PREDICTIONS("Predictions", Icons.Outlined.EmojiEvents, Destinations.Predictions),
    NOTIFICATIONS("Notifications", Icons.Outlined.Notifications, Destinations.Notifications),
    PROFILE("Profile", Icons.Outlined.AccountCircle, Destinations.Profile),
}

/**
 * The bottom navigation bar. Rendered only on top-level tab destinations.
 */
@Composable
fun WC26BottomBar(
    navController: NavHostController,
    unreadCount: Int,
    onTabSelected: (TopLevelTab) -> Unit,
) {
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentDestination = backStackEntry?.destination

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .navigationBarsPadding() // Float above system bar
            .padding(horizontal = Spacing.xl, vertical = Spacing.lg),
        contentAlignment = Alignment.Center
    ) {
        NavigationBar(
            modifier = Modifier
                .height(64.dp)
                .shadow(8.dp, RoundedCornerShape(percent = 50))
                .clip(RoundedCornerShape(percent = 50))
                // Add a subtle border that catches light
                .border(
                    BorderStroke(
                        width = 0.5.dp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    ),
                    shape = RoundedCornerShape(percent = 50)
                ),
            containerColor = MaterialTheme.colorScheme.surfaceContainer.copy(alpha = 0.90f),
            windowInsets = WindowInsets(0.dp) // Reset bottom padding inside
        ) {
            TopLevelTab.entries.forEach { tab ->
                val selected = currentDestination?.hierarchy?.any {
                    it.hasRoute(tab.route::class)
                } == true

                NavigationBarItem(
                    selected = selected,
                    onClick = {
                        navController.navigate(tab.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                        onTabSelected(tab)
                    },
                    icon = {
                        if (tab == TopLevelTab.NOTIFICATIONS && unreadCount > 0) {
                            BadgedBox(
                                badge = {
                                    Badge {
                                        Text(if (unreadCount > 99) "99+" else unreadCount.toString())
                                    }
                                }
                            ) {
                                Icon(tab.icon, contentDescription = tab.label)
                            }
                        } else {
                            Icon(tab.icon, contentDescription = tab.label)
                        }
                    },
                )
            }
        }
    }
}