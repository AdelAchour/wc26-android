package com.adel.wc26.feature.posts.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26ErrorState
import com.adel.wc26.core.designsystem.component.WC26LoadingState
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.ui.toStringRes
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.ui.platform.LocalContext
import com.adel.wc26.feature.posts.domain.comment.Comment
import com.adel.wc26.feature.posts.ui.component.DetailedPostCard
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.tooling.preview.Preview
import com.adel.wc26.core.designsystem.component.WC26PrimaryButton
import com.adel.wc26.core.designsystem.theme.WC26Theme


/**
 * Post detail — stateful entry point. Shows a post, its comments, and an
 * inline comment composer pinned to the bottom.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailScreen(
    onBack: () -> Unit,
    onAuthorClick: (Long) -> Unit,
    onMatchClick: (Long) -> Unit,
    onSignInPrompt: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PostDetailViewModel = hiltViewModel(),
) {
    val context = LocalContext.current

    val state by viewModel.uiState.collectAsStateWithLifecycle()

    // Dialog state management
    var showPostDeleteConfirm by remember { mutableStateOf(false) }
    var commentIdToDelete by remember { mutableStateOf<Long?>(null) }
    var showLikersBottomSheet by remember { mutableStateOf(false) }

    // If the post is optimistically deleted, navigate back automatically!
    LaunchedEffect(state.post) {
        if (!state.loading && state.post == null && state.error == null) {
            onBack()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.post_detail_title)) },
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
        bottomBar = {
            if (state.isLoggedIn) {
                CommentComposerBar(
                    input = state.commentInput,
                    charsLeft = state.commentCharsLeft,
                    canSend = state.canSendComment,
                    sending = state.sendingComment,
                    onInputChange = viewModel::onCommentInputChange,
                    onSend = viewModel::sendComment,
                )
            } else {
                SignInToCommentBar(onSignIn = onSignInPrompt)
            }
        },
    ) { padding ->
        PostDetailContent(
            state = state,
            onRetry = viewModel::loadPost,
            onRefresh = viewModel::refresh,
            onLikeClick = viewModel::toggleLike,
            onAuthorClick = onAuthorClick,
            onMatchClick = onMatchClick,
            onDeletePostClick = { showPostDeleteConfirm = true },
            onDeleteCommentClick = { commentIdToDelete = it },
            onLikeCommentClick = viewModel::toggleLikeComment,
            onLikeLongClick = if (state.currentUserId == state.post?.author?.id) {
                {
                    viewModel.openPostLikes()
                    showLikersBottomSheet = true
                }
            } else {
                null
            },
            onLikeCommentLongClick = { comment ->
                viewModel.openCommentLikes(comment.id)
                showLikersBottomSheet = true
            },
            modifier = Modifier.padding(padding),
        )

        // Post Deletion Confirmation Dialog
        if (showPostDeleteConfirm) {
            AlertDialog(
                onDismissRequest = { showPostDeleteConfirm = false },
                title = { Text(stringResource(R.string.post_delete_title)) },
                text = { Text(stringResource(R.string.post_delete_title)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            showPostDeleteConfirm = false
                            val errorMsg = context.getString(R.string.post_delete_error)
                            viewModel.deletePost(
                                onFailure = {
                                    Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                            )
                        }
                    ) {
                        Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { showPostDeleteConfirm = false }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        // Comment Deletion Confirmation Dialog
        if (commentIdToDelete != null) {
            AlertDialog(
                onDismissRequest = { commentIdToDelete = null },
                title = { Text(stringResource(R.string.comment_delete_title)) },
                text = { Text(stringResource(R.string.comment_delete_message)) },
                confirmButton = {
                    TextButton(
                        onClick = {
                            val id = commentIdToDelete
                            commentIdToDelete = null
                            val errorMsg = context.getString(R.string.comment_delete_error)
                            if (id != null) {
                                viewModel.deleteComment(
                                    commentId = id,
                                    onFailure = {
                                        Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
                                    }
                                )
                            }
                        }
                    ) {
                        Text(stringResource(R.string.action_delete), color = MaterialTheme.colorScheme.error)
                    }
                },
                dismissButton = {
                    TextButton(onClick = { commentIdToDelete = null }) {
                        Text(stringResource(R.string.action_cancel))
                    }
                }
            )
        }

        if (showLikersBottomSheet) {
            LikersBottomSheet(
                likers = state.likers,
                loading = state.likersLoading,
                error = state.likersError,
                nextCursor = state.likersNextCursor,
                onDismissRequest = { showLikersBottomSheet = false },
                onLoadMore = viewModel::loadMoreLikers,
                onUserClick = { userId ->
                    showLikersBottomSheet = false
                    onAuthorClick(userId)
                }
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PostDetailContent(
    state: PostDetailUiState,
    onRetry: () -> Unit,
    onRefresh: () -> Unit,
    onLikeClick: () -> Unit,
    onAuthorClick: (Long) -> Unit,
    onMatchClick: (Long) -> Unit,
    onDeletePostClick: () -> Unit,
    onDeleteCommentClick: (Long) -> Unit,
    onLikeCommentClick: (Comment) -> Unit,
    modifier: Modifier = Modifier,
    onLikeLongClick: (() -> Unit)? = null,
    onLikeCommentLongClick: ((Comment) -> Unit)? = null,
) {
    when {
        state.loading -> WC26LoadingState(modifier = modifier)

        state.error != null -> WC26ErrorState(
            message = stringResource(state.error.toStringRes()),
            onRetry = onRetry,
            modifier = modifier,
        )

        state.post != null -> {
            PullToRefreshBox(
                isRefreshing = state.isRefreshing,
                onRefresh = onRefresh,
                modifier = modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    item {
                        DetailedPostCard(
                            post = state.post,
                            onLikeClick = onLikeClick,
                            onAuthorClick = { onAuthorClick(state.post.author.id) },
                            onMatchClick = { onMatchClick(state.post.matchId) },
                            canDelete = state.currentUserId == state.post.author.id,
                            onDeleteClick = onDeletePostClick,
                            onCommentClick = { /* Optional: focus comment input or no-op */ },
                            onLikeLongClick = onLikeLongClick,
                        )
                        HorizontalDivider()
                        Text(
                            text = stringResource(
                                R.string.post_detail_comments_header,
                                state.post.commentCount,
                            ),
                            style = MaterialTheme.typography.titleMedium,
                            modifier = Modifier.padding(Spacing.lg),
                        )
                    }

                    if (state.comments.isEmpty() && !state.commentsLoading) {
                        item {
                            Text(
                                text = stringResource(R.string.post_detail_no_comments),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(Spacing.lg),
                                textAlign = TextAlign.Center,
                            )
                        }
                    } else {
                        items(
                            items = state.comments,
                            key = { it.id },
                        ) { comment ->
                            // Comment owner OR parent post owner can delete
                            val canDelete = state.currentUserId == comment.author.id ||
                                    state.currentUserId == state.post.author.id

                            CommentRow(
                                comment = comment,
                                onAuthorClick = { onAuthorClick(comment.author.id) },
                                canDelete = canDelete,
                                onDeleteClick = { onDeleteCommentClick(comment.id) },
                                onLikeClick = { onLikeCommentClick(comment) },
                                onLikeLongClick = if (onLikeCommentLongClick != null && state.currentUserId == comment.author.id) {
                                    { onLikeCommentLongClick(comment) }
                                } else {
                                    null
                                }
                            )
                            HorizontalDivider()
                        }
                    }
                }
            }
        }
    }
}

/** The inline comment input bar pinned to the bottom of the screen. */
@Composable
private fun CommentComposerBar(
    input: String,
    charsLeft: Int,
    canSend: Boolean,
    sending: Boolean,
    onInputChange: (String) -> Unit,
    onSend: () -> Unit,
) {
    // Rotates from -90 degrees (looking left) to 0 degrees (looking top)
    val rotationAngle by animateFloatAsState(
        targetValue = if (canSend) 0f else -90f,
        animationSpec = tween(durationMillis = 300, easing = FastOutSlowInEasing),
        label = "SendButtonRotation"
    )

    // Smooth transition for the circular button background color
    val buttonColor by animateColorAsState(
        targetValue = if (canSend) {
            MaterialTheme.colorScheme.primary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "SendButtonColor"
    )

    // Smooth transition for the send icon color
    val iconColor by animateColorAsState(
        targetValue = if (canSend) {
            MaterialTheme.colorScheme.onPrimary
        } else {
            MaterialTheme.colorScheme.onSurface.copy(alpha = 0.38f)
        },
        animationSpec = tween(durationMillis = 300),
        label = "SendIconColor"
    )

    Surface(
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 0.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // A light modern divider at the top of the composer bar
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .imePadding()
                    .padding(Spacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    // Rounded corner rectangle container for the text field
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(
                                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .border(
                                width = 1.dp,
                                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = Spacing.md, vertical = Spacing.md),
                        contentAlignment = Alignment.CenterStart
                    ) {
                        BasicTextField(
                            value = input,
                            onValueChange = onInputChange,
                            modifier = Modifier.fillMaxWidth(),
                            textStyle = MaterialTheme.typography.bodyLarge.copy(
                                color = MaterialTheme.colorScheme.onSurface,
                            ),
                            decorationBox = { inner ->
                                if (input.isEmpty()) {
                                    Text(
                                        text = stringResource(R.string.post_detail_comment_hint),
                                        style = MaterialTheme.typography.bodyLarge,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                }
                                inner()
                            },
                        )
                    }

                    // Character counter placed directly below the text field
                    if (charsLeft <= 50) {
                        Text(
                            text = charsLeft.toString(),
                            style = MaterialTheme.typography.labelSmall,
                            color = if (charsLeft < 0) MaterialTheme.colorScheme.error
                            else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.padding(start = Spacing.sm, top = 2.dp),
                        )
                    }
                }

                Spacer(modifier = Modifier.width(Spacing.md))

                // Compact circle send button on the right side
                IconButton(
                    onClick = onSend,
                    enabled = canSend,
                    modifier = Modifier
                        .size(36.dp)
                        .background(
                            color = buttonColor,
                            shape = CircleShape
                        )
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = stringResource(R.string.post_detail_send_comment),
                        tint = iconColor,
                        modifier = Modifier
                            .size(24.dp) // Balanced icon size
                            .rotate(rotationAngle)
                    )
                }
            }
        }
    }
}

/** Shown in place of the composer when the user is signed out. */
@Composable
private fun SignInToCommentBar(onSignIn: () -> Unit) {
    Surface(
        color = MaterialTheme.colorScheme.surface,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // A light modern divider at the top of the composer bar
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )

            WC26PrimaryButton(
                text = stringResource(R.string.post_detail_sign_in_to_comment),
                onClick = onSignIn,
                modifier = Modifier.fillMaxWidth().padding(Spacing.md),
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CommentComposerBarPreview() {
    WC26Theme {
        CommentComposerBar(
            input = "",
            charsLeft = 55,
            canSend = true,
            sending = false,
            onInputChange = {},
            onSend = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
fun SignInToCommentBarPreview() {
    WC26Theme {
        SignInToCommentBar(onSignIn = {})
    }
}