package com.adel.wc26.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.NavHostController
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.NavDestination.Companion.hierarchy

/**
 * The five top-level tabs shown in the bottom navigation bar.
 * Each pairs a label + icon with its type-safe destination.
 */
enum class TopLevelTab(
    val label: String,
    val icon: ImageVector,
    val route: Any,
) {
    MATCHES("Matches", Icons.Outlined.DateRange, Destinations.Matches()),
    FEED("Feed", Icons.Outlined.Home, Destinations.Feed),
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

    NavigationBar {
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
                label = {
                    Text(
                        text = tab.label,
                        maxLines = 1,
                        overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis
                    )
                },
            )
        }
    }
}