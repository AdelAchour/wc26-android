package com.adel.wc26.feature.posts.ui.feed

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.platform.LocalContext
import android.widget.Toast
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26EmptyState
import com.adel.wc26.core.designsystem.component.WC26ErrorState
import com.adel.wc26.core.designsystem.component.WC26LoadingState
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.ui.component.postsThread
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Alignment

/**
 * Feed tab — stateful entry point. The global stream of all posts.
 */
@Composable
fun FeedScreen(
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onMatchClick: (Long) -> Unit,
    onComposeClick: () -> Unit,
    onSignInPrompt: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val posts = viewModel.posts.collectAsLazyPagingItems()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current
    val isLoggedIn = currentUserId != null

    var postToDelete by remember { mutableStateOf<Post?>(null) }

    Box(modifier = modifier.fillMaxSize()) {
        FeedContent(
            posts = posts,
            currentUserId = currentUserId,
            onPostClick = onPostClick,
            onAuthorClick = onAuthorClick,
            onMatchClick = onMatchClick,
            onLikeClick = viewModel::toggleLike,
            onDeleteClick = { postToDelete = it },
            modifier = Modifier.fillMaxSize(),
        )

        ExtendedFloatingActionButton(
            text = { Text(stringResource(R.string.match_detail_compose)) },
            icon = { Icon(Icons.Filled.Add, contentDescription = null) },
            onClick = {
                if (isLoggedIn) onComposeClick()
                else onSignInPrompt()
            },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(Spacing.lg), // Standard FAB margin
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
                            val errorMsg = context.getString(R.string.post_delete_error)
                            if (post != null) {
                                viewModel.deletePost(post, onFailure = { Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show() })
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
 * Feed tab — stateless content. Handles the Paging refresh load states:
 * full-screen loading / error / empty, otherwise the paged list.
 */
@Composable
fun FeedContent(
    posts: LazyPagingItems<Post>,
    currentUserId: Long?,
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onMatchClick: (Long) -> Unit,
    onLikeClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {

        Text(
            text = stringResource(R.string.feed_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(Spacing.lg),
        )

        when (val refresh = posts.loadState.refresh) {
            is LoadState.Loading -> WC26LoadingState()

            is LoadState.Error -> WC26ErrorState(
                message = stringResource(R.string.error_unknown),
                onRetry = { posts.retry() },
            )

            is LoadState.NotLoading -> {
                if (posts.itemCount == 0) {
                    WC26EmptyState(
                        title = stringResource(R.string.feed_empty_title),
                        description = stringResource(R.string.feed_empty_desc),
                    )
                } else {
                    LazyColumn(modifier = Modifier.fillMaxSize()) {
                        postsThread(
                            posts = posts,
                            onPostClick = onPostClick,
                            onLikeClick = onLikeClick,
                            onAuthorClick = onAuthorClick,
                            onMatchClick = onMatchClick,
                            currentUserId = currentUserId,
                            onDeleteClick = onDeleteClick,
                        )
                    }
                }
            }
        }
    }
}