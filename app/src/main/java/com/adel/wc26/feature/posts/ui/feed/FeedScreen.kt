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
import androidx.hilt.navigation.compose.hiltViewModel
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

/**
 * Feed tab — stateful entry point. The global stream of all posts.
 */
@Composable
fun FeedScreen(
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: FeedViewModel = hiltViewModel(),
) {
    val posts = viewModel.posts.collectAsLazyPagingItems()
    FeedContent(
        posts = posts,
        onPostClick = onPostClick,
        onAuthorClick = onAuthorClick,
        modifier = modifier,
    )
}

/**
 * Feed tab — stateless content. Handles the Paging refresh load states:
 * full-screen loading / error / empty, otherwise the paged list.
 */
@Composable
fun FeedContent(
    posts: LazyPagingItems<Post>,
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
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
                            onAuthorClick = onAuthorClick,
                        )
                    }
                }
            }
        }
    }
}