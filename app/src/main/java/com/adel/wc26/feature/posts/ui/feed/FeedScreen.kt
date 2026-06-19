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
import com.adel.wc26.core.designsystem.component.StylusNote
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.ui.component.postsThread
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

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

        FloatingActionButton(
            onClick = {
                if (isLoggedIn) onComposeClick()
                else onSignInPrompt()
            },
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .navigationBarsPadding()
                .padding(end = Spacing.lg, bottom = 112.dp),
        ) {
            Icon(
                imageVector = StylusNote,
                contentDescription = stringResource(R.string.match_detail_compose)
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
 * Pull-to-refresh container wraps the lists / state screens.
 */
@OptIn(ExperimentalMaterial3Api::class)
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

        val isRefreshing = posts.loadState.refresh is LoadState.Loading && posts.itemCount > 0

        PullToRefreshBox(
            isRefreshing = isRefreshing,
            onRefresh = { posts.refresh() },
            modifier = Modifier.fillMaxSize()
        ) {
            if (posts.loadState.refresh is LoadState.Loading && posts.itemCount == 0) {
                WC26LoadingState()
            } else if (posts.loadState.refresh is LoadState.Error && posts.itemCount == 0) {
                WC26ErrorState(
                    message = stringResource(R.string.error_unknown),
                    onRetry = { posts.retry() },
                )
            } else if (posts.itemCount == 0) {
                WC26EmptyState(
                    title = stringResource(R.string.feed_empty_title),
                    description = stringResource(R.string.feed_empty_desc),
                )
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        bottom = 112.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                    )
                ) {
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