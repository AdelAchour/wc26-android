package com.adel.wc26.feature.posts.ui.detail

import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
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
import com.adel.wc26.feature.posts.ui.component.PostCard
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.LaunchedEffect
import android.widget.Toast
import androidx.compose.ui.platform.LocalContext


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
            onLikeClick = viewModel::toggleLike,
            onAuthorClick = onAuthorClick,
            onMatchClick = onMatchClick,
            onDeletePostClick = { showPostDeleteConfirm = true },
            onDeleteCommentClick = { commentIdToDelete = it },
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
    }
}

/**
 * Post detail — stateless content: the post header + the comments thread.
 */
@Composable
fun PostDetailContent(
    state: PostDetailUiState,
    onRetry: () -> Unit,
    onLikeClick: () -> Unit,
    onAuthorClick: (Long) -> Unit,
    onMatchClick: (Long) -> Unit,
    onDeletePostClick: () -> Unit,
    onDeleteCommentClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.loading -> WC26LoadingState(modifier = modifier)

        state.error != null -> WC26ErrorState(
            message = stringResource(state.error.toStringRes()),
            onRetry = onRetry,
            modifier = modifier,
        )

        state.post != null -> LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            item {
                PostCard(
                    post = state.post,
                    onClick = { /* already on detail */ },
                    onLikeClick = onLikeClick,
                    onAuthorClick = { onAuthorClick(state.post.author.id) },
                    onMatchClick = { onMatchClick(state.post.matchId) },
                    canDelete = state.currentUserId == state.post.author.id,
                    onDeleteClick = onDeletePostClick
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
                        onDeleteClick = { onDeleteCommentClick(comment.id) }
                    )
                    HorizontalDivider()
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
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding()
                .padding(Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            BasicTextField(
                value = input,
                onValueChange = onInputChange,
                modifier = Modifier.weight(1f).padding(Spacing.sm),
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

            // Counter shows only when getting close to the limit.
            if (charsLeft <= 50) {
                Text(
                    text = charsLeft.toString(),
                    style = MaterialTheme.typography.labelSmall,
                    color = if (charsLeft < 0) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(horizontal = Spacing.xs),
                )
            }

            IconButton(
                onClick = onSend,
                enabled = canSend,
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Send,
                    contentDescription = stringResource(R.string.post_detail_send_comment),
                    tint = if (canSend) MaterialTheme.colorScheme.secondary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/** Shown in place of the composer when the user is signed out. */
@Composable
private fun SignInToCommentBar(onSignIn: () -> Unit) {
    Surface(
        tonalElevation = 3.dp,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onSignIn),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.lg),
            horizontalArrangement = Arrangement.Center,
        ) {
            Text(
                text = stringResource(R.string.post_detail_sign_in_to_comment),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.secondary,
            )
        }
    }
}