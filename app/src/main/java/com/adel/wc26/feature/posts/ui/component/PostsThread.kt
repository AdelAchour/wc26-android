package com.adel.wc26.feature.posts.ui.component

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.feature.posts.domain.post.Post

/**
 * Renders a paged list of posts inside a LazyColumn — shared by the match
 * thread and the global feed.
 *
 * Handles the Paging load states inline: an append spinner at the bottom,
 * an end-of-list message, and a simple empty hint. (The initial full-screen
 * loading/error is handled by the host screen.)
 */

fun LazyListScope.postsThread(
    posts: LazyPagingItems<Post>,
    onPostClick: (Long) -> Unit,
    onLikeClick: (Post) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onMatchClick: ((Long) -> Unit)? = null,
    emptyMessage: String? = null,
    currentUserId: Long? = null,
    onDeleteClick: ((Post) -> Unit)? = null,
) {
    items(
        count = posts.itemCount,
        key = { index -> posts[index]?.id ?: index },
    ) { index ->
        val post = posts[index]
        if (post != null) {
            PostCard(
                post = post,
                onClick = { onPostClick(post.id) },
                onLikeClick = { onLikeClick(post) },
                onAuthorClick = { onAuthorClick(post.author.id) },
                onMatchClick = if (onMatchClick != null) { { onMatchClick(post.matchId) } } else null,
                canDelete = currentUserId != null && currentUserId == post.author.id,
                onDeleteClick = { onDeleteClick?.invoke(post) },
            )
            HorizontalDivider()
        }
    }

    // Append-state footer (loading more / error).
    when (val append = posts.loadState.append) {
        is LoadState.Loading -> item { ThreadFooterLoading() }
        is LoadState.Error -> item {
            ThreadFooterMessage(text = stringResource(R.string.thread_load_more_error))
        }
        is LoadState.NotLoading ->
            if (append.endOfPaginationReached) {
                if (posts.itemCount > 0) {
                    item { ThreadFooterMessage(text = stringResource(R.string.thread_caught_up)) }
                } else if (emptyMessage != null) {
                    item { ThreadFooterMessage(text = emptyMessage) }
                }
            }
    }
}

@Composable
private fun ThreadFooterLoading() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        CircularProgressIndicator(modifier = Modifier.padding(Spacing.sm))
    }
}

@Composable
private fun ThreadFooterMessage(text: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.lg),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}