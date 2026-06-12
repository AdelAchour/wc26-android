package com.adel.wc26.feature.posts.ui.component

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.outlined.FavoriteBorder
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.LiveBadge
import com.adel.wc26.core.designsystem.component.WC26Avatar
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.ui.format
import com.adel.wc26.core.util.WC26DateTime
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.domain.model.MatchStatus
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.domain.post.PostAuthor
import java.time.Instant
import android.content.Intent
import androidx.compose.foundation.layout.height
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.outlined.ChatBubbleOutline
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider

/**
 * A single post, rendered as a row. Used in the match thread, the global
 * feed, and profile lists.
 *
 * @param onClick        opens the post detail.
 * @param onLikeClick    toggles like (optimistic UI is handled by the caller).
 * @param onAuthorClick  opens the author's profile.
 * @param onMatchClick   opens the match details (optional banner).
 */
@Composable
fun PostCard(
    modifier: Modifier = Modifier,
    post: Post,
    onClick: () -> Unit,
    onLikeClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onMatchClick: (() -> Unit)? = null,
    canDelete: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onShareClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {

            // 1. Thread Header Banner (placed at the absolute top of the card)
            if (post.match != null && onMatchClick != null) {
                MatchThreadHeader(
                    match = post.match,
                    onClick = onMatchClick,
                    modifier = Modifier.padding(bottom = Spacing.xs)
                )
            }

            // 2. Main Row (Avatar + Content)
            Row(modifier = Modifier.fillMaxWidth()) {
                WC26Avatar(
                    displayName = post.author.displayName,
                    avatarUrl = post.author.avatarUrl,
                    size = 36.dp,
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

                    // Body Text
                    Text(
                        text = post.content,
                        style = MaterialTheme.typography.bodyLarge,
                    )

                    Spacer(Modifier.padding(top = Spacing.lg))

                    // Action row: like + comment, and share
                    Row(
                        modifier = Modifier.fillMaxWidth(0.85f),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween,
                    ) {
                        LikeAction(
                            liked = post.likedByCurrentUser,
                            count = post.likeCount,
                            onClick = onLikeClick,
                        )
                        CommentAction(
                            count = post.commentCount,
                            onClick = onClick,
                        )
                        ShareAction(
                            onClick = {
                                if (onShareClick != null) {
                                    onShareClick()
                                } else {
                                    val postUrl = "https://wc26.adelash.dev/posts/${post.id}"
                                    val shareText = context.getString(
                                        R.string.post_share_template,
                                        post.author.username,
                                        postUrl
                                    )
                                    val sendIntent = Intent().apply {
                                        action = Intent.ACTION_SEND
                                        putExtra(Intent.EXTRA_TEXT, shareText)
                                        type = "text/plain"
                                    }
                                    val shareIntent = Intent.createChooser(sendIntent, null)
                                    context.startActivity(shareIntent)
                                }
                            }
                        )
                    }
                }
            }
        }
    }
}

/**
 * A detailed post view designed specifically for the Post Detail screen.
 * Places the avatar and names on top, and displays content and full timestamp vertically.
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun DetailedPostCard(
    modifier: Modifier = Modifier,
    post: Post,
    onLikeClick: () -> Unit,
    onAuthorClick: () -> Unit,
    onMatchClick: (() -> Unit)? = null,
    canDelete: Boolean = false,
    onDeleteClick: () -> Unit = {},
    onShareClick: (() -> Unit)? = null,
    onCommentClick: () -> Unit = {},
    onLikeLongClick: (() -> Unit)? = null,
) {
    val context = LocalContext.current

    Surface(
        modifier = modifier.fillMaxWidth(),
        color = MaterialTheme.colorScheme.surface,
    ) {
        Column(modifier = Modifier.padding(horizontal = Spacing.lg, vertical = Spacing.md)) {

            // 1. Thread Header Banner (placed at the absolute top of the card)
            if (post.match != null && onMatchClick != null) {
                MatchThreadHeader(
                    match = post.match,
                    onClick = onMatchClick,
                    modifier = Modifier.padding(bottom = Spacing.md)
                )
            }

            // 2. Main Row (Avatar + Column with Display Name & Username centered)
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                WC26Avatar(
                    displayName = post.author.displayName,
                    avatarUrl = post.author.avatarUrl,
                    size = 40.dp, // Slightly larger avatar for detail view
                    modifier = Modifier.clickable(onClick = onAuthorClick),
                )

                Spacer(Modifier.width(Spacing.md))

                Column(
                    modifier = Modifier.weight(1f),
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = post.author.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        modifier = Modifier.clickable(onClick = onAuthorClick),
                    )
                    Text(
                        text = "@${post.author.username}",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.clickable(onClick = onAuthorClick),
                    )
                }

                if (canDelete) {
                    var showMenu by remember { mutableStateOf(false) }

                    Box {
                        IconButton(
                            onClick = { showMenu = true },
                            modifier = Modifier.size(24.dp)
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

            Spacer(Modifier.height(Spacing.md))

            // 3. Body Text (Starts at the same padding level as Avatar)
            Text(
                text = post.content,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(Modifier.height(Spacing.md))

            // 4. Full Detailed Time
            Text(
                text = WC26DateTime.detailedTimestamp(post.createdAt),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.md))

            // 5. Divider matching the content padding limits
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
            )

            Spacer(Modifier.height(Spacing.sm))

            // 6. Action row: like + comment, and share
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = Spacing.xs),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                LikeAction(
                    liked = post.likedByCurrentUser,
                    count = post.likeCount,
                    onClick = onLikeClick,
                    onLongClick = onLikeLongClick,
                )
                CommentAction(
                    count = post.commentCount,
                    onClick = onCommentClick,
                )
                ShareAction(
                    onClick = {
                        if (onShareClick != null) {
                            onShareClick()
                        } else {
                            val postUrl = "https://wc26.adelash.dev/posts/${post.id}"
                            val shareText = context.getString(
                                R.string.post_share_template,
                                post.author.username,
                                postUrl
                            )
                            val sendIntent = Intent().apply {
                                action = Intent.ACTION_SEND
                                putExtra(Intent.EXTRA_TEXT, shareText)
                                type = "text/plain"
                            }
                            val shareIntent = Intent.createChooser(sendIntent, null)
                            context.startActivity(shareIntent)
                        }
                    }
                )
            }
        }
    }
}

/**
 * A highly polished, clean mini-banner representing the parent match thread.
 */
@Composable
private fun MatchThreadHeader(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Surface(
        onClick = onClick,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ),
        shape = RoundedCornerShape(8.dp),
        modifier = modifier
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.md, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Spacer(Modifier.width(Spacing.xs))
                Text(
                    text = "${match.homeTeam} vs ${match.awayTeam}",
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Spacer(Modifier.width(Spacing.sm))

            // Score / Live Badge status
            when (match.status) {
                MatchStatus.LIVE -> {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(Spacing.xs)
                    ) {
                        if (match.hasScore) {
                            Text(
                                text = "${match.homeScore} - ${match.awayScore}",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.error
                            )
                        }
                        LiveBadge(label = stringResource(R.string.match_live))
                    }
                }
                MatchStatus.FINISHED -> {
                    Text(
                        text = if (match.hasScore) "${match.homeScore} - ${match.awayScore} FT" else "FT",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                else -> {
                    Text(
                        text = "Upcoming",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.outline,
                        fontWeight = FontWeight.Medium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun LikeAction(
    liked: Boolean,
    count: Int,
    onClick: () -> Unit,
    onLongClick: (() -> Unit)? = null,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = if (onLongClick != null) {
            Modifier.combinedClickable(
                onClick = onClick,
                onLongClick = onLongClick
            )
        } else {
            Modifier.clickable(onClick = onClick)
        },
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
private fun CommentAction(
    count: Int,
    onClick: () -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.clickable(onClick = onClick),
    ) {
        Icon(
            imageVector = Icons.Outlined.ChatBubbleOutline,
            contentDescription = "Comments",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun ShareAction(
    onClick: () -> Unit,
) {
    Box(
        modifier = Modifier.clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Share,
            contentDescription = "Share Post",
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.size(18.dp),
        )
    }
}

// ---- Preview ----

private fun previewMatch(status: MatchStatus, home: Int? = null, away: Int? = null) = Match(
    id = 1,
    gameNumber = 12,
    homeTeam = "Spain",
    awayTeam = "Germany",
    stage = "Group E",
    venue = "Al Bayt Stadium",
    countryCode = "QA",
    kickoffAt = Instant.parse("2026-11-27T19:00:00Z"),
    status = status,
    homeScore = home,
    awayScore = away,
    homeTeamCode = "es",
    awayTeamCode = "de",
)

@Preview(showBackground = true)
@Composable
private fun PostCardPreview() {
    WC26Theme {
        PostCard(
            post = Post(
                id = 1,
                matchId = 1,
                match = previewMatch(MatchStatus.LIVE, 1, 1),
                author = PostAuthor(1, "adel", "Adel", null),
                content = "What a goal! Spain looking sharp tonight. This is the kind of football we came for.",
                likeCount = 24,
                commentCount = 5,
                likedByCurrentUser = true,
                createdAt = "2026-05-25T19:30:00Z",
            ),
            onClick = {},
            onLikeClick = {},
            onAuthorClick = {},
            onMatchClick = {},
            canDelete = true
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun DetailedPostCardPreview() {
    WC26Theme {
        DetailedPostCard(
            post = Post(
                id = 1,
                matchId = 1,
                match = previewMatch(MatchStatus.LIVE, 1, 1),
                author = PostAuthor(1, "adel", "Adel", null),
                content = "What a goal! Spain looking sharp tonight. This is the kind of football we came for.",
                likeCount = 24,
                commentCount = 5,
                likedByCurrentUser = true,
                createdAt = "2026-05-25T19:30:00Z",
            ),
            onLikeClick = {},
            onAuthorClick = {},
            onMatchClick = {},
            canDelete = true
        )
    }
}