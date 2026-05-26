package com.adel.wc26.feature.posts.ui.detail

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adel.wc26.core.designsystem.component.WC26Avatar
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.ui.format
import com.adel.wc26.core.util.WC26DateTime
import com.adel.wc26.feature.posts.domain.comment.Comment
import com.adel.wc26.feature.posts.domain.post.PostAuthor

/**
 * A single comment — avatar, author line, body. Tapping the avatar/name
 * opens the author's profile.
 */
@Composable
fun CommentRow(
    comment: Comment,
    onAuthorClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg, vertical = Spacing.md),
    ) {
        WC26Avatar(
            displayName = comment.author.displayName,
            avatarUrl = comment.author.avatarUrl,
            size = 36.dp,
            modifier = Modifier.clickable(onClick = onAuthorClick),
        )

        Spacer(Modifier.width(Spacing.md))

        Column(modifier = Modifier.fillMaxWidth()) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = comment.author.displayName,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                )
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    text = "· ${WC26DateTime.relative(comment.createdAt)?.format() ?: ""}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            Spacer(Modifier.padding(top = Spacing.xxs))
            Text(
                text = comment.content,
                style = MaterialTheme.typography.bodyMedium,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun CommentRowPreview() {
    WC26Theme {
        CommentRow(
            comment = Comment(
                id = 1,
                postId = 1,
                author = PostAuthor(2, "sara", "Sara M.", null),
                content = "Totally agree — that midfield press was relentless.",
                createdAt = "2026-06-14T19:45:00Z",
            ),
            onAuthorClick = {},
        )
    }
}