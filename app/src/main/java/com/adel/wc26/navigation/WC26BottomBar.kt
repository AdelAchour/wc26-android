package com.adel.wc26.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountCircle
import androidx.compose.material.icons.outlined.DateRange
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
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
 * The four top-level tabs shown in the bottom navigation bar.
 * Each pairs a label + icon with its type-safe destination.
 */
enum class TopLevelTab(
    val label: String,
    val icon: ImageVector,
    val route: Any,
) {
    MATCHES("Matches", Icons.Outlined.DateRange, Destinations.Matches),
    FEED("Feed", Icons.Outlined.Home, Destinations.Feed),
    PROFILE("Profile", Icons.Outlined.AccountCircle, Destinations.Profile),
    SETTINGS("Settings", Icons.Outlined.Settings, Destinations.Settings),
}

/**
 * The bottom navigation bar. Rendered only on top-level tab destinations
 * (see WC26NavHost — it's hidden on detail screens).
 *
 * Tab switching uses launchSingleTop + popUpTo(start) so tabs don't stack
 * endlessly on the back stack, and saves/restores each tab's own state.
 */
@Composable
fun WC26BottomBar(navController: NavHostController) {
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
                        // Pop up to the start destination to avoid building
                        // a deep stack of tabs.
                        popUpTo(navController.graph.startDestinationId) {
                            saveState = true
                        }
                        launchSingleTop = true
                        restoreState = true
                    }
                },
                icon = { Icon(tab.icon, contentDescription = tab.label) },
                label = { Text(tab.label) },
            )
        }
    }
}