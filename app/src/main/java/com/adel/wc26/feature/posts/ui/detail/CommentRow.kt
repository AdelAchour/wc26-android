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
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource
import com.adel.wc26.R

/**
 * A single comment — avatar, author line, body. Tapping the avatar/name
 * opens the author's profile.
 */
@Composable
fun CommentRow(
    modifier: Modifier = Modifier,
    comment: Comment,
    onAuthorClick: () -> Unit,
    canDelete: Boolean = false,
    onDeleteClick: () -> Unit = {},
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
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
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

                if (canDelete) {
                    Spacer(modifier = Modifier.weight(1f))

                    var showMenu by remember { mutableStateOf(false) }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(18.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.MoreVert,
                                contentDescription = "Comment options",
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = Icons.Default.Delete,
                                            contentDescription = null,
                                            tint = MaterialTheme.colorScheme.error,
                                            modifier = Modifier.size(16.dp)
                                        )
                                        Spacer(modifier = Modifier.width(Spacing.sm))
                                        Text(
                                            text = stringResource(R.string.action_delete),
                                            color = MaterialTheme.colorScheme.error
                                        )
                                    }
                                },
                                onClick = {
                                    showMenu = false
                                    onDeleteClick()
                                }
                            )
                        }
                    }
                }
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
            canDelete = true,
        )
    }
}