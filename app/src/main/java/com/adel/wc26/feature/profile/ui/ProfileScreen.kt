package com.adel.wc26.feature.profile.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import androidx.paging.LoadState
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26ErrorState
import com.adel.wc26.core.designsystem.component.WC26LoadingState
import com.adel.wc26.core.designsystem.component.WC26PrimaryButton
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.ui.toStringRes
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.ui.component.postsThread
import com.adel.wc26.feature.profile.ui.component.ProfileHeader
import com.adel.wc26.feature.predictions.ui.PredictionStatsCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.adel.wc26.core.designsystem.component.WC26SecondaryButton
import com.adel.wc26.core.designsystem.component.WC26TextField
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.result.AppError
import com.adel.wc26.feature.profile.domain.UserProfile
import com.adel.wc26.feature.profile.domain.UserRole
import com.adel.wc26.feature.profile.ui.component.AvatarZoomDialog

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
    onMatchClick: (Long) -> Unit,
    onSignIn: () -> Unit,
    onEditAvatarClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: ProfileViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    val posts = viewModel.posts?.collectAsLazyPagingItems()
    val likes = viewModel.likes?.collectAsLazyPagingItems()

    var postToDelete by remember { mutableStateOf<Post?>(null) }
    var showEditProfileSheet by remember { mutableStateOf(false) }

    ProfileContent(
        state = state,
        posts = posts,
        likes = likes,
        onTabSelected = viewModel::onTabSelected,
        onRetry = viewModel::load,
        onRefresh = {
            viewModel.load(isRefresh = true)
            posts?.refresh()
            likes?.refresh()
        },
        onSignIn = onSignIn,
        onPostClick = onPostClick,
        onAuthorClick = onAuthorClick,
        onMatchClick = onMatchClick,
        onLikeClick = viewModel::toggleLike,
        onDeleteClick = { postToDelete = it },
        onEditAvatarClick = onEditAvatarClick,
        onEditProfileClick = { showEditProfileSheet = true },
        onSettingsClick = onSettingsClick,
        modifier = modifier,
    )

    // Render Edit Profile bottom sheet when active
    if (showEditProfileSheet && state.profile != null) {
        EditProfileBottomSheet(
            profile = state.profile!!,
            isSaving = state.isSavingProfile,
            profileError = state.profileError,
            onDismiss = { showEditProfileSheet = false },
            onSave = { displayName, bio ->
                viewModel.updateProfileInfo(displayName, bio) {
                    showEditProfileSheet = false
                }
            }
        )
    }

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

/**
 * Profile tab — stateless content. Logged-out prompt, loading, error, or
 * the loaded profile (header + tabbed paged lists).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileContent(
    state: ProfileUiState,
    posts: LazyPagingItems<Post>?,
    likes: LazyPagingItems<Post>?,
    onTabSelected: (ProfileTab) -> Unit,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onSignIn: () -> Unit,
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onMatchClick: (Long) -> Unit,
    onLikeClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    onEditAvatarClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onSettingsClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emptyMessage = when (state.selectedTab) {
        ProfileTab.POSTS -> stringResource(R.string.profile_empty_posts)
        ProfileTab.LIKES -> stringResource(R.string.profile_empty_likes)
    }
    var showAvatarZoom by remember { mutableStateOf(false) }

    when {
        state.loggedOut -> LoggedOutProfile(onSignIn = onSignIn, onSettingsClick = onSettingsClick, modifier = modifier)

        state.loading -> WC26LoadingState(modifier = modifier)

        state.error != null -> WC26ErrorState(
            message = stringResource(state.error.toStringRes()),
            onRetry = onRetry,
            modifier = modifier,
        )

        state.profile != null -> {
            val postsRefreshing = posts?.let { it.loadState.refresh is LoadState.Loading && it.itemCount > 0 } ?: false
            val likesRefreshing = likes?.let { it.loadState.refresh is LoadState.Loading && it.itemCount > 0 } ?: false
            val isRefreshing = state.isRefreshing || postsRefreshing || likesRefreshing

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = Spacing.lg + 96.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
                ) {
                    item {
                        Box(modifier = Modifier.fillMaxWidth()) {
                            ProfileHeader(
                                displayName = state.profile.displayName,
                                username = state.profile.username,
                                avatarUrl = state.profile.avatarUrl,
                                bio = state.profile.bio,
                                joinedAtIso = state.profile.joinedAt,
                                onAvatarClick = onEditAvatarClick,
                                onAvatarLongClick = { showAvatarZoom = true },
                                showEditIcon = true,
                                onEditProfileClick = onEditProfileClick,
                            )

                            IconButton(
                                onClick = onSettingsClick,
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(Spacing.md)
                            ) {
                                Icon(
                                    imageVector = Icons.Outlined.Settings,
                                    contentDescription = "Settings",
                                    tint = MaterialTheme.colorScheme.onSurface
                                )
                            }
                        }
                    }

                    state.predictionStats?.let { stats ->
                        item {
                            PredictionStatsCard(
                                stats = stats,
                                modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.sm),
                            )
                        }
                    }

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

                    val active = when (state.selectedTab) {
                        ProfileTab.POSTS -> posts
                        ProfileTab.LIKES -> likes
                    }
                    if (active != null) {
                        postsThread(
                            posts = active,
                            onPostClick = onPostClick,
                            onLikeClick = onLikeClick,
                            onAuthorClick = onAuthorClick,
                            onMatchClick = onMatchClick,
                            emptyMessage = emptyMessage,
                            currentUserId = state.profile.id, // Logged in profile ID matches the current user
                            onDeleteClick = onDeleteClick,
                        )
                    }
                }
            }

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

/** Shown when a logged-out user opens the Profile tab. */
@Composable
private fun LoggedOutProfile(
    onSignIn: () -> Unit,
    onSettingsClick: () -> Unit,
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
            textAlign = TextAlign.Center,
        )
        Column(
            modifier = Modifier.width(IntrinsicSize.Max)
        ) {
            WC26PrimaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.profile_sign_in),
                onClick = onSignIn,
            )
            WC26SecondaryButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.profile_go_to_settings),
                onClick = onSettingsClick,
                )

        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun EditProfileBottomSheet(
    profile: UserProfile,
    isSaving: Boolean,
    profileError: AppError?,
    onDismiss: () -> Unit,
    onSave: (String, String) -> Unit,
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    var localDisplayName by remember { mutableStateOf(profile.displayName) }
    var localBio by remember { mutableStateOf(profile.bio ?: "") }

    val isNameValid = localDisplayName.trim().length in 2..50
    val isBioValid = localBio.trim().length <= 100

    // Save button enabled rules
    val canSave = isNameValid && isBioValid && !isSaving &&
            (localDisplayName != profile.displayName || localBio != (profile.bio ?: ""))

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        dragHandle = { androidx.compose.material3.BottomSheetDefaults.DragHandle() }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg)
                .padding(bottom = Spacing.xxl),
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Text(
                text = stringResource(R.string.edit_profile),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            // Display Name TextField
            WC26TextField(
                value = localDisplayName,
                onValueChange = { localDisplayName = it },
                label = stringResource(R.string.field_display_name),
                singleLine = true,
                errorText = if (localDisplayName.isNotEmpty() && !isNameValid) {
                    stringResource(R.string.validation_display_name_rules)
                } else null
            )

            // Bio TextField + Counter
            Column {
                WC26TextField(
                    value = localBio,
                    onValueChange = { if (it.length <= 120) localBio = it }, // soft typing limit, hard validation limit
                    label = "Bio",
                    singleLine = false,
                    errorText = if (localBio.isNotEmpty() && !isBioValid) stringResource(R.string.validation_display_bio_rules) else null
                )

                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = "${localBio.length}/100",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isBioValid) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                    modifier = Modifier.align(Alignment.End)
                )
            }

            // Save Error Feedback
            if (profileError != null) {
                Text(
                    text = stringResource(profileError.toStringRes()),
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(Modifier.height(Spacing.md))

            // Save Button
            WC26PrimaryButton(
                text = "Save Changes",
                onClick = { onSave(localDisplayName, localBio) },
                enabled = canSave,
                loading = isSaving,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun EditProfileBottomSheetPreview() {
    WC26Theme {
        EditProfileBottomSheet(
            profile = UserProfile(
                id = 1,
                email = "",
                username = "",
                displayName = "",
                avatarUrl = "",
                role = UserRole.USER,
                bio = "",
                joinedAt = ""
            ),
            isSaving = false,
            profileError = null,
            onDismiss = {},
            onSave = { _, _ -> }
        )
    }
}