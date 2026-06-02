package com.adel.wc26.feature.posts.ui.component

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26Avatar
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.ui.format
import com.adel.wc26.core.util.WC26DateTime
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.domain.post.PostAuthor
import androidx.compose.foundation.layout.Box
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.IconButton
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.res.stringResource

/**
 * A single post, rendered as a row. Used in the match thread, the global
 * feed, and profile lists.
 *
 * @param onClick        opens the post detail.
 * @param onLikeClick    toggles like (optimistic UI is handled by the caller).
 * @param onAuthorClick  opens the author's profile.
 */
@Composable
fun PostCard(
    modifier: Modifier = Modifier,
    post: Post,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    onAuthorClick: () -> Unit,
    canDelete: Boolean = false,
    onDeleteClick: () -> Unit = {},
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Row(modifier = Modifier.padding(Spacing.lg)) {

            WC26Avatar(
                displayName = post.author.displayName,
                avatarUrl = post.author.avatarUrl,
                size = 44.dp,
                modifier = Modifier.clickable(onClick = onAuthorClick),
            )

            Spacer(Modifier.width(Spacing.md))

            Column(modifier = Modifier.fillMaxWidth()) {

                // Author line: display name · @username · time + 3-dots Menu
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = post.author.displayName,
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(
                        text = "@${post.author.username}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    Spacer(Modifier.width(Spacing.xs))
                    Text(
                        text = "· ${WC26DateTime.relative(post.createdAt)?.format() ?: ""}",
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
                                    contentDescription = "Post options",
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
                                                modifier = Modifier.size(18.dp)
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

                Spacer(Modifier.padding(top = Spacing.xs))

                // Body
                Text(
                    text = post.content,
                    style = MaterialTheme.typography.bodyLarge,
                )

                Spacer(Modifier.padding(top = Spacing.sm))

                // Action row: like + comment counts
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.lg),
                ) {
                    LikeAction(
                        liked = post.likedByCurrentUser,
                        count = post.likeCount,
                        onClick = onLikeClick,
                    )
                    CommentCount(count = post.commentCount)
                }
            }
        }
    }
}

@Composable
private fun LikeAction(
    liked: Boolean,
    count: Int,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = if (liked) Icons.Filled.Favorite
            else Icons.Outlined.FavoriteBorder,
            contentDescription = null,
            tint = if (liked) MaterialTheme.colorScheme.secondary
            else MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
        Spacer(Modifier.width(Spacing.xs))
        Text(
            text = count.toString(),
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}

@Composable
private fun CommentCount(count: Int) {
    Text(
        text = pluralStringResource(R.plurals.post_comment_count, count, count),
        style = MaterialTheme.typography.labelMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
    )
}

// ---- Preview ----

@Preview(showBackground = true)
@Composable
private fun PostCardPreview() {
    WC26Theme {
        PostCard(
            post = Post(
                id = 1,
                matchId = 1,
                author = PostAuthor(1, "adel", "Adel", null),
                content = "What a goal! Canada looking sharp tonight. This is the kind of football we came for.",
                likeCount = 24,
                commentCount = 5,
                likedByCurrentUser = true,
                createdAt = "2026-06-14T19:30:00Z",
            ),
            onClick = {},
            onLikeClick = {},
            onAuthorClick = {},
            canDelete = true
        )
    }
}