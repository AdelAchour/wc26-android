package com.adel.wc26.feature.profile.ui.userprofile

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26ErrorState
import com.adel.wc26.core.designsystem.component.WC26LoadingState
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.ui.toStringRes
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.ui.component.postsThread
import com.adel.wc26.feature.profile.ui.component.ProfileHeader

/**
 * UserProfile — stateful entry point. The public profile of another user:
 * their identity header followed by their posts.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserProfileScreen(
    onBack: () -> Unit,
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val posts = viewModel.posts.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        state.profile?.displayName
                            ?: stringResource(R.string.user_profile_title),
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
    ) { padding ->
        UserProfileContent(
            state = state,
            posts = posts,
            onRetry = viewModel::loadProfile,
            onPostClick = onPostClick,
            onAuthorClick = onAuthorClick,
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * UserProfile — stateless content: identity header + paged posts thread.
 */
@Composable
fun UserProfileContent(
    state: UserProfileUiState,
    posts: LazyPagingItems<Post>,
    onRetry: () -> Unit,
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.loading -> WC26LoadingState(modifier = modifier)

        state.error != null -> WC26ErrorState(
            message = stringResource(state.error.toStringRes()),
            onRetry = onRetry,
            modifier = modifier,
        )

        state.profile != null -> LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            item {
                ProfileHeader(
                    displayName = state.profile.displayName,
                    username = state.profile.username,
                    avatarUrl = state.profile.avatarUrl,
                    joinedAtIso = state.profile.joinedAt,
                )
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.user_profile_posts_header),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(Spacing.lg),
                )
            }

            postsThread(
                posts = posts,
                onPostClick = onPostClick,
                onAuthorClick = onAuthorClick,
            )
        }
    }
}