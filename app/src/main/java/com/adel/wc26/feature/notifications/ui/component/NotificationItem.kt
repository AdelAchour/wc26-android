package com.adel.wc26.feature.notifications.ui.component

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26Avatar
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.ui.format
import com.adel.wc26.core.util.WC26DateTime
import com.adel.wc26.feature.notifications.domain.Notification
import com.adel.wc26.feature.notifications.domain.NotificationComment
import com.adel.wc26.feature.notifications.domain.NotificationPost
import com.adel.wc26.feature.notifications.domain.NotificationType
import com.adel.wc26.feature.posts.domain.post.PostAuthor

@Composable
fun NotificationItem(
    notification: Notification,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val unreadBg = MaterialTheme.colorScheme.primary.copy(alpha = 0.06f)
    val itemModifier = modifier
        .fillMaxWidth()
        .then(
            if (notification.isRead) Modifier
            else Modifier.background(unreadBg)
        )
        .clickable(onClick = onClick)
        .padding(horizontal = Spacing.lg, vertical = Spacing.md)

    Row(
        modifier = itemModifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        WC26Avatar(
            displayName = notification.sender.displayName,
            avatarUrl = notification.sender.avatarUrl,
            size = 40.dp
        )

        Spacer(modifier = Modifier.width(Spacing.md))

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.Center
        ) {
            // Build localized name + action string
            val displayName = notification.sender.displayName
            val baseText = when (notification.type) {
                NotificationType.LIKE_POST -> stringResource(R.string.notifications_liked_post, displayName)
                NotificationType.REPLY_POST -> stringResource(R.string.notifications_replied_post, displayName)
                NotificationType.LIKE_COMMENT -> stringResource(R.string.notifications_liked_comment, displayName)
            }

            val startIndex = baseText.indexOf(displayName)
            val annotatedText = buildAnnotatedString {
                if (startIndex >= 0) {
                    append(baseText.take(startIndex))
                    withStyle(SpanStyle(fontWeight = FontWeight.Bold)) {
                        append(displayName)
                    }
                    append(baseText.substring(startIndex + displayName.length))
                } else {
                    append(baseText)
                }
            }

            Text(
                text = annotatedText,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )

            val snippet = when (notification.type) {
                NotificationType.LIKE_POST -> notification.post.content
                NotificationType.REPLY_POST -> notification.comment?.content ?: ""
                NotificationType.LIKE_COMMENT -> notification.comment?.content ?: ""
            }

            if (snippet.isNotEmpty()) {
                Text(
                    text = snippet,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.padding(top = Spacing.xxs)
                )
            }
        }

        Spacer(modifier = Modifier.width(Spacing.sm))

        Column(
            horizontalAlignment = Alignment.End,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.align(Alignment.Top)
        ) {
            val relativeTime = WC26DateTime.relative(notification.createdAt)
            val timeText = relativeTime?.format() ?: ""
            Text(
                text = timeText,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            if (!notification.isRead) {
                Box(
                    modifier = Modifier
                        .padding(top = Spacing.sm)
                        .size(8.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationItemUnreadPreview() {
    WC26Theme {
        NotificationItem(
            notification = Notification(
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
                    content = "What an opening match in Los Angeles! Who is going all the way?"
                ),
                comment = NotificationComment(
                    id = 234,
                    content = "Argentina definitely going to defend the title! 🇦🇷"
                )
            ),
            onClick = {}
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun NotificationItemReadPreview() {
    WC26Theme {
        NotificationItem(
            notification = Notification(
                id = 2,
                receiverId = 10,
                type = NotificationType.LIKE_POST,
                isRead = true,
                createdAt = "2026-06-05T14:45:00Z",
                sender = PostAuthor(
                    id = 89,
                    username = "kmbappe",
                    displayName = "Kylian Mbappé",
                    avatarUrl = "preset://avatar_trophy"
                ),
                post = NotificationPost(
                    id = 8,
                    content = "What an opening match in Los Angeles! Who is going all the way?"
                ),
                comment = null
            ),
            onClick = {}
        )
    }
}