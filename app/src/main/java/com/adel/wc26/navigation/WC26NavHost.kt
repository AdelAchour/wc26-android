package com.adel.wc26.navigation

import android.util.Log
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.adel.wc26.feature.auth.ui.splash.SplashRoute
import com.adel.wc26.feature.auth.ui.splash.SplashViewModel

/**
 * The app's navigation host.
 *
 * Structure:
 *  - Splash is the start destination — it routes to either the auth
 *    flow or the tabs once the token check completes.
 *  - Top-level tab destinations show the bottom bar.
 *  - Auth and detail destinations hide it.
 *
 * The bottom bar visibility is derived from the current destination, so
 * it appears/disappears automatically as the user navigates.
 */
@Composable
fun WC26NavHost(
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    // Which destinations show the bottom bar — the four tabs only.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = TopLevelTab.entries.any { tab ->
        backStackEntry?.destination?.hasRoute(tab.route::class) == true
    }

    Scaffold(
        modifier = modifier,
        bottomBar = {
            if (showBottomBar) {
                WC26BottomBar(navController)
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.Splash,
            modifier = Modifier.padding(innerPadding),
        ) {
            // --- Splash / launch routing ---
            composable<Destinations.Splash> {
                val splashViewModel: SplashViewModel = hiltViewModel()
                val route by splashViewModel.route.collectAsStateWithLifecycle()

                LaunchedEffect(route) {
                    when (route) {
                        SplashRoute.Undecided -> Unit // wait
                        SplashRoute.LoggedIn -> {
                            navController.navigate(Destinations.Matches) {
                                popUpTo(Destinations.Splash) { inclusive = true }
                            }
                        }
                        SplashRoute.LoggedOut -> {
                            navController.navigate(Destinations.Welcome) {
                                popUpTo(Destinations.Splash) { inclusive = true }
                            }
                        }
                    }
                }
                // Splash shows nothing meaningful — routing is near-instant.
                PlaceholderScreen(title = "WC26")
            }

            // --- Auth flow (bottom bar hidden) ---
            composable<Destinations.Welcome> {
                PlaceholderScreen(title = "Welcome")
            }
            composable<Destinations.Login> {
                PlaceholderScreen(title = "Log in")
            }
            composable<Destinations.Register> {
                PlaceholderScreen(title = "Create account")
            }

            // --- Top-level tabs (bottom bar visible) ---
            composable<Destinations.Matches> {
                PlaceholderScreen(title = "Matches")
            }
            composable<Destinations.Feed> {
                PlaceholderScreen(title = "Feed")
            }
            composable<Destinations.Profile> {
                PlaceholderScreen(title = "Profile")
            }
            composable<Destinations.Settings> {
                PlaceholderScreen(title = "Settings")
            }

            // --- Detail screens (bottom bar hidden) ---
            composable<Destinations.MatchDetail> { backStack ->
                val args = backStack.toRoute<Destinations.MatchDetail>()
                PlaceholderScreen(title = "Match #${args.matchId}")
            }
            composable<Destinations.PostDetail> { backStack ->
                val args = backStack.toRoute<Destinations.PostDetail>()
                PlaceholderScreen(title = "Post #${args.postId}")
            }
            composable<Destinations.UserProfile> { backStack ->
                val args = backStack.toRoute<Destinations.UserProfile>()
                PlaceholderScreen(title = "User #${args.userId}")
            }
            composable<Destinations.Likers> { backStack ->
                val args = backStack.toRoute<Destinations.Likers>()
                PlaceholderScreen(title = "Likers · post #${args.postId}")
            }
        }
    }
}