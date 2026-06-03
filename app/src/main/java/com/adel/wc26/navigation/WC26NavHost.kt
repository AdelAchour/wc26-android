package com.adel.wc26.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.adel.wc26.core.datastore.TokenStore
import com.adel.wc26.feature.auth.ui.splash.SplashRoute
import com.adel.wc26.feature.auth.ui.splash.SplashViewModel
import com.adel.wc26.feature.auth.ui.welcome.WelcomeScreen
import com.adel.wc26.feature.auth.ui.login.LoginScreen
import com.adel.wc26.feature.auth.ui.register.RegisterScreen
import com.adel.wc26.feature.profile.ui.ProfileScreen
import com.adel.wc26.feature.profile.ui.userprofile.UserProfileScreen
import com.adel.wc26.feature.settings.ui.SettingsScreen
import com.adel.wc26.feature.matches.ui.list.MatchesScreen
import com.adel.wc26.feature.matches.ui.detail.MatchDetailScreen
import com.adel.wc26.feature.matches.ui.edit.MatchEditScreen
import com.adel.wc26.feature.posts.ui.feed.FeedScreen
import com.adel.wc26.feature.posts.ui.composer.PostComposerScreen
import com.adel.wc26.feature.posts.ui.detail.PostDetailScreen

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
    tokenStore: TokenStore,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    // 1. Reactively observe the authentication status flow
    val isLoggedIn by tokenStore.isLoggedInFlow.collectAsStateWithLifecycle(initialValue = true)
    // 2. Automatically navigate to Welcome if the session is evicted mid-use
    LaunchedEffect(isLoggedIn) {
        if (!isLoggedIn) {
            val currentDest = navController.currentBackStackEntry?.destination

            // Check if we are currently inside the auth flow. If yes, do not redirect.
            val inAuthFlow = isInAuthFlow(currentDest)
            if (!inAuthFlow) {
                // Clear the back stack and force-navigate to Welcome
                navController.navigate(Destinations.Welcome) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
    }

    // Which destinations show the bottom bar — the four tabs only.
    val backStackEntry by navController.currentBackStackEntryAsState()
    val showBottomBar = TopLevelTab.entries.any { tab ->
        val dest = backStackEntry?.destination
        if (dest?.hasRoute(Destinations.Matches::class) == true) {
            val isPickerMode = runCatching {
                backStackEntry?.toRoute<Destinations.Matches>()?.isPickerMode == true
            }.getOrDefault(false)
            !isPickerMode
        } else {
            dest?.hasRoute(tab.route::class) == true
        }
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
                            navController.navigate(Destinations.Matches()) {
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
                WelcomeScreen(
                    onCreateAccount = { navController.navigate(Destinations.Register) },
                    onLogIn = { navController.navigate(Destinations.Login) },
                    onExplore = {
                        navController.navigate(Destinations.Matches) {
                            popUpTo(Destinations.Welcome) { inclusive = true }
                        }
                    },
                )
            }
            composable<Destinations.Login> {
                LoginScreen(
                    onLoggedIn = {
                        navController.navigate(Destinations.Matches) {
                            // Clear the whole auth flow from the back stack.
                            popUpTo<Destinations.Welcome> { inclusive = true }
                        }
                    },
                    onGoToRegister = {
                        navController.navigate(Destinations.Register) {
                            launchSingleTop = true
                        }
                    },
                )
            }
            composable<Destinations.Register> {
                RegisterScreen(
                    onRegistered = {
                        navController.navigate(Destinations.Matches) {
                            popUpTo<Destinations.Welcome> { inclusive = true }
                        }
                    },
                    onGoToLogin = {
                        navController.navigate(Destinations.Login) {
                            launchSingleTop = true
                        }
                    },
                )
            }

            // --- Top-level tabs (bottom bar visible) ---
            composable<Destinations.Matches> { backStackEntry ->
                val args = backStackEntry.toRoute<Destinations.Matches>()
                MatchesScreen(
                    isPickerMode = args.isPickerMode,
                    onBackClick = { navController.popBackStack() },
                    onMatchClick = { matchId ->
                        if (args.isPickerMode) {
                            navController.navigate(Destinations.PostComposer(matchId))
                        } else {
                            navController.navigate(Destinations.MatchDetail(matchId))
                        }
                    },
                )
            }
            composable<Destinations.Feed> {
                FeedScreen(
                    onPostClick = { postId ->
                        navController.navigate(Destinations.PostDetail(postId))
                    },
                    onAuthorClick = { userId ->
                        navController.navigate(Destinations.UserProfile(userId))
                    },
                    onMatchClick = { matchId ->
                        navController.navigate(Destinations.MatchDetail(matchId))
                    },
                    onComposeClick = {
                        navController.navigate(Destinations.Matches(isPickerMode = true))
                    },
                    onSignInPrompt = {
                        navController.navigate(Destinations.Login)
                    },
                )
            }
            composable<Destinations.Profile> {
                ProfileScreen(
                    onPostClick = { postId ->
                        navController.navigate(Destinations.PostDetail(postId))
                    },
                    onAuthorClick = { userId ->
                        navController.navigate(Destinations.UserProfile(userId))
                    },
                    onMatchClick = { matchId ->
                        navController.navigate(Destinations.MatchDetail(matchId))
                    },
                    onSignIn = {
                        navController.navigate(Destinations.Login)
                    },
                )
            }
            composable<Destinations.Settings> {
                SettingsScreen(
                    onLoggedOut = {
                        navController.navigate(Destinations.Welcome) {
                            // Clear everything — fresh start at Welcome.
                            popUpTo(0) { inclusive = true }
                        }
                    },
                )
            }

            // --- Detail screens (bottom bar hidden) ---
            composable<Destinations.MatchDetail> {
                MatchDetailScreen(
                    onBack = { navController.popBackStack() },
                    onPostClick = { postId ->
                        navController.navigate(Destinations.PostDetail(postId))
                    },
                    onAuthorClick = { userId ->
                        navController.navigate(Destinations.UserProfile(userId))
                    },
                    onComposeClick = { matchId ->
                        navController.navigate(Destinations.PostComposer(matchId))
                    },
                    onSignInPrompt = {
                        navController.navigate(Destinations.Login)
                    },
                    onEditClick = { matchId ->
                        navController.navigate(Destinations.MatchEdit(matchId))
                    },
                )
            }
            composable<Destinations.MatchEdit> {
                MatchEditScreen(
                    onBack = { navController.popBackStack() },
                )
            }
            composable<Destinations.PostComposer> {
                PostComposerScreen(
                    onClose = { navController.popBackStack() },
                    onPosted = {
                        val isFromPicker = runCatching {
                            navController.previousBackStackEntry?.toRoute<Destinations.Matches>()?.isPickerMode == true
                        }.getOrDefault(false)
                        if (isFromPicker) {
                            navController.popBackStack<Destinations.Feed>(inclusive = false)
                        } else {
                            navController.popBackStack()
                        }
                    },
                )
            }
            composable<Destinations.PostDetail> {
                PostDetailScreen(
                    onBack = { navController.popBackStack() },
                    onAuthorClick = { userId ->
                        navController.navigate(Destinations.UserProfile(userId))
                    },
                    onMatchClick = { matchId ->
                        navController.navigate(Destinations.MatchDetail(matchId))
                    },
                    onSignInPrompt = {
                        navController.navigate(Destinations.Login)
                    },
                )
            }
            composable<Destinations.UserProfile> {
                UserProfileScreen(
                    onBack = { navController.popBackStack() },
                    onPostClick = { postId ->
                        navController.navigate(Destinations.PostDetail(postId))
                    },
                    onAuthorClick = { userId ->
                        navController.navigate(Destinations.UserProfile(userId))
                    },
                    onMatchClick = { matchId ->
                        navController.navigate(Destinations.MatchDetail(matchId))
                    },
                )
            }
            composable<Destinations.Likers> { backStack ->
                val args = backStack.toRoute<Destinations.Likers>()
                PlaceholderScreen(title = "Likers · post #${args.postId}")
            }
        }
    }
}

private fun isInAuthFlow(currentDest: NavDestination?) : Boolean = currentDest?.hasRoute(Destinations.Splash::class) == true ||
        currentDest?.hasRoute(Destinations.Welcome::class) == true ||
        currentDest?.hasRoute(Destinations.Login::class) == true ||
        currentDest?.hasRoute(Destinations.Register::class) == true