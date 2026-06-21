package com.adel.wc26.navigation

import android.content.Intent
import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.systemBars
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavDestination
import androidx.navigation.NavDestination.Companion.hasRoute
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navDeepLink
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
import com.adel.wc26.feature.predictions.ui.list.PredictionsScreen
import com.adel.wc26.feature.matches.ui.detail.MatchDetailScreen
import com.adel.wc26.feature.matches.ui.edit.MatchEditScreen
import com.adel.wc26.feature.posts.ui.feed.FeedScreen
import com.adel.wc26.feature.posts.ui.composer.PostComposerScreen
import com.adel.wc26.feature.posts.ui.detail.PostDetailScreen
import com.adel.wc26.feature.profile.ui.AvatarPickerScreen
import com.adel.wc26.feature.profile.ui.ProfileViewModel
import com.adel.wc26.core.network.AppStatus
import com.adel.wc26.core.network.AppStatusManager
import com.adel.wc26.feature.auth.ui.splash.SplashScreen
import com.adel.wc26.feature.notifications.data.NotificationsManager
import com.adel.wc26.feature.notifications.ui.NotificationsScreen
import com.adel.wc26.feature.status.ui.ForceUpdateScreen
import com.adel.wc26.feature.status.ui.MaintenanceScreen
import com.adel.wc26.feature.status.ui.MaintenanceViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

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
    appStatusManager: AppStatusManager,
    notificationsManager: NotificationsManager,
    deepLinkIntents: Flow<Intent>,
    modifier: Modifier = Modifier,
) {
    val navController = rememberNavController()

    // Re-fire deep links for warm starts (background/foreground). The library's
    // auto-handler only runs once per process via the deepLinkHandled flag, so we
    // drive it ourselves here. Reuses the per-destination navDeepLink registrations.
    LaunchedEffect(Unit) {
        deepLinkIntents.collect { intent ->
            navController.handleDeepLink(intent)
        }
    }

    // Observe app blocking status
    val appStatus by appStatusManager.appStatus.collectAsStateWithLifecycle()
    LaunchedEffect(appStatus) {
        when (appStatus) {
            is AppStatus.ForceUpdate -> {
                navController.navigate(Destinations.ForceUpdate(
                    updateUrl = (appStatus as AppStatus.ForceUpdate).updateUrl,
                    minVersion = (appStatus as AppStatus.ForceUpdate).minVersion
                )) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AppStatus.Maintenance -> {
                navController.navigate(Destinations.Maintenance) {
                    popUpTo(0) { inclusive = true }
                }
            }
            is AppStatus.Normal -> {
                val currentDest = navController.currentBackStackEntry?.destination
                val isCurrentlyBlocked = currentDest?.hasRoute(Destinations.Maintenance::class) == true ||
                        currentDest?.hasRoute(Destinations.ForceUpdate::class) == true
                if (isCurrentlyBlocked) {
                    navController.navigate(Destinations.Splash) {
                        popUpTo(0) { inclusive = true }
                    }
                }
            }
        }
    }

    // Reactively observe the authentication status flow
    val isLoggedIn by tokenStore.isLoggedInFlow.collectAsState(initial = false)
    // Tracks the last verified state to identify transitions
    var previousIsLoggedIn by remember { mutableStateOf<Boolean?>(null) }

    // Automatically navigate to Welcome ONLY if the session is evicted/changed mid-use (true -> false)
    LaunchedEffect(isLoggedIn) {
        if (previousIsLoggedIn == true && !isLoggedIn) {
            val currentDest = navController.currentBackStackEntry?.destination
            val inAuthFlow = isInAuthFlow(currentDest)
            if (!inAuthFlow) {
                // Clear the back stack and force-navigate to Welcome
                navController.navigate(Destinations.Welcome) {
                    popUpTo(0) { inclusive = true }
                }
            }
        }
        previousIsLoggedIn = isLoggedIn
    }

    // Fetch unread count initially on login/logout changes
    LaunchedEffect(isLoggedIn) {
        if (isLoggedIn) {
            notificationsManager.refreshUnreadCount()
            notificationsManager.registerCurrentToken()
        } else {
            notificationsManager.clearCount()
        }
    }

    // Lifecycle observer to refresh unread count whenever app resumes (foregrounded)
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    DisposableEffect(lifecycleOwner, isLoggedIn) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME && isLoggedIn) {
                coroutineScope.launch {
                    notificationsManager.refreshUnreadCount()
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Read count from manager to display badge in bottom bar
    val unreadCount by notificationsManager.unreadCount.collectAsStateWithLifecycle()

    // Which destinations show the bottom bar
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
            AnimatedVisibility(
                visible = showBottomBar,
                enter = slideInVertically(
                    initialOffsetY = { it }, // Slides up from the bottom edge
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300)),
                exit = slideOutVertically(
                    targetOffsetY = { it }, // Slide down off the screen
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            ) {
                WC26BottomBar(
                    navController = navController,
                    unreadCount = unreadCount,
                    onTabSelected = { tab ->
                        if (isLoggedIn) {
                            coroutineScope.launch {
                                notificationsManager.refreshUnreadCount()
                            }
                        }
                    }
                )
            }
        },
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = Destinations.Splash,
            modifier = Modifier
                .padding(
                    top = innerPadding.calculateTopPadding(),
                    start = innerPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr),
                    end = innerPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)
                )
                // We applied the top + horizontal system-bar insets above, so mark
                // them consumed — otherwise each screen's own TopAppBar re-applies
                // the status-bar inset and the top gap doubles. The bottom inset is
                // left available on purpose so content flows behind the floating
                // bottom bar (tab screens) and bottom bars own it (e.g. post detail).
                .consumeWindowInsets(
                    WindowInsets.systemBars.only(
                        WindowInsetsSides.Top + WindowInsetsSides.Horizontal
                    )
                ),
            enterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            exitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Left,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            },
            popEnterTransition = {
                slideIntoContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeIn(animationSpec = tween(300))
            },
            popExitTransition = {
                slideOutOfContainer(
                    towards = AnimatedContentTransitionScope.SlideDirection.Right,
                    animationSpec = tween(300)
                ) + fadeOut(animationSpec = tween(300))
            }
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
                SplashScreen()
            }

            // --- Auth flow (bottom bar hidden) ---
            composable<Destinations.Welcome> {
                WelcomeScreen(
                    onCreateAccount = { navController.navigate(Destinations.Register) },
                    onLogIn = { navController.navigate(Destinations.Login) },
                    onExplore = {
                        navController.navigate(Destinations.Matches()) {
                            popUpTo(Destinations.Welcome) { inclusive = true }
                        }
                    },
                )
            }
            composable<Destinations.Login> {
                LoginScreen(
                    onLoggedIn = {
                        navController.navigate(Destinations.Matches()) {
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
                        navController.navigate(Destinations.Matches()) {
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
                    onSignInPrompt = {
                        navController.navigate(Destinations.Login)
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
            composable<Destinations.Predictions> {
                PredictionsScreen(
                    onMatchClick = { matchId ->
                        navController.navigate(Destinations.MatchDetail(matchId))
                    },
                    onSignInPrompt = {
                        navController.navigate(Destinations.Login)
                    },
                    onUserClick = { userId ->
                        navController.navigate(Destinations.UserProfile(userId))
                    },
                )
            }
            composable<Destinations.Notifications> {
                NotificationsScreen(
                    onNotificationClick = { postId ->
                        navController.navigate(Destinations.PostDetail(postId))
                    },
                    onSenderClick = { userId ->
                        navController.navigate(Destinations.UserProfile(userId))
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
                    onEditAvatarClick = {
                        navController.navigate(Destinations.AvatarPicker)
                    },
                    onSettingsClick = {
                        navController.navigate(Destinations.Settings)
                    },
                )
            }
            composable<Destinations.AvatarPicker> { backStackEntry ->
                val parentEntry = remember(backStackEntry) {
                    navController.getBackStackEntry(Destinations.Profile)
                }
                val profileViewModel: ProfileViewModel = hiltViewModel(parentEntry)
                AvatarPickerScreen(
                    onBack = { navController.popBackStack() },
                    viewModel = profileViewModel,
                )
            }
            composable<Destinations.Settings> {
                SettingsScreen(
                    onBack = { navController.popBackStack() },
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
            composable<Destinations.PostDetail>(
                deepLinks = listOf(
                    navDeepLink<Destinations.PostDetail>(
                        basePath = "https://wc26.adelash.dev/posts"
                    ),
                    navDeepLink<Destinations.PostDetail>(
                        basePath = "wc26://posts"
                    )
                )
            ) {
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
            // --- Blocker routes ---
            composable<Destinations.ForceUpdate> { backStackEntry ->
                val args = backStackEntry.toRoute<Destinations.ForceUpdate>()
                ForceUpdateScreen(updateUrl = args.updateUrl, minVersion = args.minVersion)
            }
            composable<Destinations.Maintenance> {
                val maintenanceViewModel: MaintenanceViewModel = hiltViewModel()
                MaintenanceScreen(viewModel = maintenanceViewModel)
            }
        }
    }
}

private fun isInAuthFlow(currentDest: NavDestination?) : Boolean = currentDest?.hasRoute(Destinations.Splash::class) == true ||
        currentDest?.hasRoute(Destinations.Welcome::class) == true ||
        currentDest?.hasRoute(Destinations.Login::class) == true ||
        currentDest?.hasRoute(Destinations.Register::class) == true