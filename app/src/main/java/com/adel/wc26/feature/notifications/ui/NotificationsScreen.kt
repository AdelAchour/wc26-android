package com.adel.wc26.feature.notifications.ui

import android.widget.Toast
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DoneAll
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26EmptyState
import com.adel.wc26.core.designsystem.component.WC26ErrorState
import com.adel.wc26.core.designsystem.component.WC26LoadingState
import com.adel.wc26.core.designsystem.component.WC26PrimaryButton
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.feature.notifications.domain.Notification
import com.adel.wc26.feature.notifications.domain.NotificationComment
import com.adel.wc26.feature.notifications.domain.NotificationPost
import com.adel.wc26.feature.notifications.domain.NotificationType
import com.adel.wc26.feature.notifications.ui.component.NotificationItem
import com.adel.wc26.feature.posts.domain.post.PostAuthor
import kotlinx.coroutines.flow.flowOf

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.pulltorefresh.PullToRefreshBox

@Composable
fun NotificationsScreen(
    onNotificationClick: (Long) -> Unit,
    onSenderClick: (Long) -> Unit,
    onSignInPrompt: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: NotificationsViewModel = hiltViewModel(),
) {
    val isLoggedIn by viewModel.isLoggedIn.collectAsStateWithLifecycle(initialValue = false)
    val notifications = if (isLoggedIn) viewModel.notifications.collectAsLazyPagingItems() else null
    val context = LocalContext.current
    val errorMsg = stringResource(R.string.notifications_error_mark_all_read)

    NotificationsContent(
        isLoggedIn = isLoggedIn,
        notifications = notifications,
        onNotificationClick = { notification ->
            viewModel.markAsRead(notification)
            onNotificationClick(notification.post.id)
        },
        onSenderClick = onSenderClick,
        onSignInPrompt = onSignInPrompt,
        onMarkAllAsRead = {
            viewModel.markAllAsRead(onFailure = {
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            })
        },
        onRefresh = {
            viewModel.refreshCount()
            notifications?.refresh()
        },
        modifier = modifier
    )
}

/**
 * Stateless screen layout to enable robust Compose previews.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NotificationsContent(
    isLoggedIn: Boolean,
    notifications: LazyPagingItems<Notification>?,
    onNotificationClick: (Notification) -> Unit,
    onSenderClick: (Long) -> Unit,
    onSignInPrompt: () -> Unit,
    onMarkAllAsRead: () -> Unit,
    onRefresh: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = Spacing.lg,
                    bottom = Spacing.md,
                ),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = stringResource(R.string.notifications_title),
                style = MaterialTheme.typography.displaySmall,
            )

            if (isLoggedIn) {
                IconButton(onClick = onMarkAllAsRead) {
                    Icon(
                        imageVector = Icons.Filled.DoneAll,
                        contentDescription = stringResource(R.string.notifications_action_mark_all_read)
                    )
                }
            }
        }

        Box(modifier = Modifier.fillMaxSize()) {
            if (!isLoggedIn) {
                LoggedOutNotifications(onSignIn = onSignInPrompt)
            } else if (notifications != null) {
                val isRefreshing = notifications.loadState.refresh is LoadState.Loading && notifications.itemCount > 0

                PullToRefreshBox(
                    isRefreshing = isRefreshing,
                    onRefresh = onRefresh,
                    modifier = Modifier.fillMaxSize()
                ) {
                    if (notifications.loadState.refresh is LoadState.Loading && notifications.itemCount == 0) {
                        WC26LoadingState()
                    } else if (notifications.loadState.refresh is LoadState.Error && notifications.itemCount == 0) {
                        WC26ErrorState(
                            message = stringResource(R.string.error_unknown),
                            onRetry = { notifications.retry() }
                        )
                    } else if (notifications.itemCount == 0) {
                        WC26EmptyState(
                            title = stringResource(R.string.notifications_empty_title),
                            description = stringResource(R.string.notifications_empty_desc)
                        )
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize(),
                            contentPadding = PaddingValues(
                                bottom = Spacing.lg + 96.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                            )
                        ) {
                            items(notifications.itemCount) { index ->
                                val notification = notifications[index]
                                if (notification != null) {
                                    NotificationItem(
                                        notification = notification,
                                        onClick = { onNotificationClick(notification) },
                                        onSenderClick = { onSenderClick(notification.sender.id) }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/** Shown when a logged-out user opens the Notifications tab. */
@Composable
private fun LoggedOutNotifications(
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
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.profile_logged_out_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = Spacing.sm, bottom = Spacing.lg),
            textAlign = TextAlign.Center,
        )
        WC26PrimaryButton(
            text = stringResource(R.string.profile_sign_in),
            onClick = onSignIn,
            modifier = Modifier.fillMaxWidth(0.6f),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsContentLoggedOutPreview() {
    WC26Theme {
        NotificationsContent(
            isLoggedIn = false,
            notifications = null,
            onNotificationClick = {},
            onSignInPrompt = {},
            onMarkAllAsRead = {},
            onRefresh = {},
            onSenderClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationsContentLoggedInPreview() {
    val mockList = listOf(
        Notification(
            id = 1,
            receiverId = 10,
            type = NotificationType.REPLY_POST,
            isRead = false,
            createdAt = "2026-06-05T14:50:00Z",
            sender = PostAuthor(
                id = 45,
                username = "leomessi",
                displayName = "Lionel Messi",
                avatarUrl = "preset://avatar_soccer_ball"
            ),
            post = NotificationPost(
                id = 8,
                content = "What an opening match in Los Angeles!"
            ),
            comment = NotificationComment(
                id = 234,
                content = "Argentina definitely going to defend the title! 🇦🇷"
            )
        ),
        Notification(
            id = 2,
            receiverId = 10,
            type = NotificationType.LIKE_POST,
            isRead = true,
            createdAt = "2026-06-05T14:45:00Z",
            sender = PostAuthor(
                id = 89,
                username = "kmbappe",
                displayName = "Kylian Mbappé",
                avatarUrl = "preset://avatar_jersey_blue"
            ),
            post = NotificationPost(
                id = 8,
                content = "What an opening match in Los Angeles!"
            ),
            comment = null
        )
    )
    val pagingFlow = flowOf(PagingData.from(mockList))
    val pagingItems = pagingFlow.collectAsLazyPagingItems()

    WC26Theme {
        NotificationsContent(
            isLoggedIn = true,
            notifications = pagingItems,
            onNotificationClick = {},
            onSignInPrompt = {},
            onMarkAllAsRead = {},
            onRefresh = {},
            onSenderClick = {}
        )
    }
}