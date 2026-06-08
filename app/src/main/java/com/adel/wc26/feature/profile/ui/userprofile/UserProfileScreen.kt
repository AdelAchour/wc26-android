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
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.paging.PagingData
import kotlinx.coroutines.flow.flowOf
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.adel.wc26.core.designsystem.component.AvatarPreset
import com.adel.wc26.core.designsystem.component.isAvatarPreset
import com.adel.wc26.core.designsystem.component.removePresetPrefix
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.feature.profile.domain.PublicProfile
import com.adel.wc26.feature.profile.ui.component.AvatarZoomDialog

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
    onMatchClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: UserProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val posts = viewModel.posts.collectAsLazyPagingItems()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle(initialValue = null)

    var postToDelete by remember { mutableStateOf<Post?>(null) }

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
            currentUserId = currentUserId,
            onRetry = viewModel::loadProfile,
            onPostClick = onPostClick,
            onAuthorClick = onAuthorClick,
            onMatchClick = onMatchClick,
            onLikeClick = viewModel::toggleLike,
            onDeleteClick = { postToDelete = it },
            modifier = Modifier.padding(padding),
        )

        if (postToDelete != null) {
            AlertDialog(
                onDismissRequest = { postToDelete = null },
                title = { Text(stringResource(R.string.post_delete_title)) },
                text = { Text(stringResource(R.string.post_delete_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val post = postToDelete
                            postToDelete = null
                            if (post != null) {
                                viewModel.deletePost(post)
                            }
                        }
                    ) {
                        Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { postToDelete = null }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }
    }
}

/**
 * UserProfile — stateless content: identity header + paged posts thread.
 */
@Composable
fun UserProfileContent(
    state: UserProfileUiState,
    posts: LazyPagingItems<Post>,
    currentUserId: Long?,
    onRetry: () -> Unit,
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onMatchClick: (Long) -> Unit,
    onLikeClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    modifier: Modifier = Modifier,
) {
    val emptyMessage = stringResource(R.string.profile_empty_posts)
    var showAvatarZoom by remember { mutableStateOf(false) }

    when {
        state.loading -> WC26LoadingState(modifier = modifier)

        state.error != null -> WC26ErrorState(
            message = stringResource(state.error.toStringRes()),
            onRetry = onRetry,
            modifier = modifier,
        )

        state.profile != null -> {
            LazyColumn(modifier = modifier.fillMaxSize()) {
                item {
                    ProfileHeader(
                        displayName = state.profile.displayName,
                        username = state.profile.username,
                        avatarUrl = state.profile.avatarUrl,
                        bio = state.profile.bio,
                        joinedAtIso = state.profile.joinedAt,
                        onAvatarClick = { showAvatarZoom = true },
                        showEditIcon = false
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
                    onLikeClick = onLikeClick,
                    onAuthorClick = onAuthorClick,
                    onMatchClick = onMatchClick,
                    emptyMessage = emptyMessage,
                    currentUserId = currentUserId,
                    onDeleteClick = onDeleteClick,
                )
            }

            // Render the Zoom Dialog when active
            if (showAvatarZoom) {
                AvatarZoomDialog(
                    displayName = state.profile.displayName,
                    avatarUrl = state.profile.avatarUrl,
                    onDismiss = { showAvatarZoom = false }
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun UserProfileContentPreview() {
    val profile = PublicProfile(
        id = 1,
        username = "adel",
        displayName = "Adel",
        avatarUrl = "preset://the_pitch",
        bio = "no bio",
        joinedAt = "2026-05-01T10:00:00Z"
    )

    WC26Theme {
        UserProfileContent(
            state = UserProfileUiState(loading = false, profile = profile, error = null),
            posts = flowOf(PagingData.empty<Post>()).collectAsLazyPagingItems(),
            currentUserId = null,
            onRetry = {},
            onPostClick = {},
            onAuthorClick = {},
            onMatchClick = {},
            onLikeClick = {},
            onDeleteClick = {},
            )
    }
}

@Preview(showBackground = true)
@Composable
private fun AvatarZoomDialogPreview() {
    WC26Theme {
        AvatarZoomDialog(
            displayName = "Adel",
            avatarUrl = "preset://the_var",
            onDismiss = {}
        )
    }
}