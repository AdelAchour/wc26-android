package com.adel.wc26.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26ErrorState
import com.adel.wc26.core.designsystem.component.WC26LoadingState
import com.adel.wc26.core.designsystem.component.WC26PrimaryButton
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.ui.toStringRes
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.ui.component.postsThread
import com.adel.wc26.feature.profile.ui.component.ProfileHeader

/**
 * Profile tab — stateful entry point. The signed-in user's own profile:
 * identity header + Posts / Likes tabs.
 *
 * @param onPostClick   opens a post's detail.
 * @param onAuthorClick opens an author's profile.
 * @param onSignIn      shown when the user is browsing as a guest.
 */
@Composable
fun ProfileScreen(
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // The paged flows are null until the profile (and thus the user id)
    // has loaded; collect them only when present.
    val posts = viewModel.posts?.collectAsLazyPagingItems()
    val likes = viewModel.likes?.collectAsLazyPagingItems()

    ProfileContent(
        state = state,
        posts = posts,
        likes = likes,
        onTabSelected = viewModel::onTabSelected,
        onRetry = viewModel::load,
        onSignIn = onSignIn,
        onPostClick = onPostClick,
        onAuthorClick = onAuthorClick,
        modifier = modifier,
    )
}

/**
 * Profile tab — stateless content. Logged-out prompt, loading, error, or
 * the loaded profile (header + tabbed paged lists).
 */
@Composable
fun ProfileContent(
    state: ProfileUiState,
    posts: LazyPagingItems<Post>?,
    likes: LazyPagingItems<Post>?,
    onTabSelected: (ProfileTab) -> Unit,
    onRetry: () -> Unit,
    onSignIn: () -> Unit,
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.loggedOut -> LoggedOutProfile(onSignIn = onSignIn, modifier = modifier)

        state.loading -> WC26LoadingState(modifier = modifier)

        state.error != null -> WC26ErrorState(
            message = stringResource(state.error.toStringRes()),
            onRetry = onRetry,
            modifier = modifier,
        )

        state.profile != null -> LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            // Identity header.
            item {
                ProfileHeader(
                    displayName = state.profile.displayName,
                    username = state.profile.username,
                    avatarUrl = state.profile.avatarUrl,
                    joinedAtIso = state.profile.joinedAt,
                )
            }

            // Posts / Likes tab row.
            item {
                TabRow(selectedTabIndex = state.selectedTab.ordinal) {
                    Tab(
                        selected = state.selectedTab == ProfileTab.POSTS,
                        onClick = { onTabSelected(ProfileTab.POSTS) },
                        text = { Text(stringResource(R.string.profile_tab_posts)) },
                    )
                    Tab(
                        selected = state.selectedTab == ProfileTab.LIKES,
                        onClick = { onTabSelected(ProfileTab.LIKES) },
                        text = { Text(stringResource(R.string.profile_tab_likes)) },
                    )
                }
            }

            // The selected tab's paged list.
            val active = when (state.selectedTab) {
                ProfileTab.POSTS -> posts
                ProfileTab.LIKES -> likes
            }
            if (active != null) {
                postsThread(
                    posts = active,
                    onPostClick = onPostClick,
                    onAuthorClick = onAuthorClick,
                )
            }
        }
    }
}

/** Shown when a logged-out user opens the Profile tab. */
@Composable
private fun LoggedOutProfile(
    onSignIn: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(
            text = stringResource(R.string.profile_logged_out_title),
            style = MaterialTheme.typography.titleLarge,
        )
        Text(
            text = stringResource(R.string.profile_logged_out_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.lg),
        )
        WC26PrimaryButton(
            text = stringResource(R.string.profile_sign_in),
            onClick = onSignIn,
        )
    }
}