package com.adel.wc26.feature.matches.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.LiveBadge
import com.adel.wc26.core.designsystem.component.WC26EmptyState
import com.adel.wc26.core.designsystem.component.WC26ErrorState
import com.adel.wc26.core.designsystem.component.WC26LoadingState
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.ui.toStringRes
import com.adel.wc26.core.util.WC26DateTime
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.domain.model.MatchStatus
import com.adel.wc26.feature.posts.domain.post.Post
import com.adel.wc26.feature.posts.ui.component.PostCard
import com.adel.wc26.feature.posts.ui.component.postsThread

/**
 * Match detail — stateful entry point.
 *
 * Top half: the match info. Bottom half: the posts thread (paged).
 * A compose FAB opens the post composer for this match.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailScreen(
    onBack: () -> Unit,
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onComposeClick: (matchId: Long) -> Unit,
    onSignInPrompt: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MatchDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val posts = viewModel.posts.collectAsLazyPagingItems()

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.match_detail_title)) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.action_back),
                        )
                    }
                },
            )
        },
        floatingActionButton = {
            state.match?.let { match ->
                ExtendedFloatingActionButton(
                    text = { Text(stringResource(R.string.match_detail_compose)) },
                    icon = { Icon(Icons.Filled.Add, contentDescription = null) },
                    onClick = {
                        if (state.isLoggedIn) onComposeClick(match.id)
                        else onSignInPrompt()
                    },
                )
            }
        },
    ) { padding ->
        MatchDetailContent(
            state = state,
            posts = posts,
            onRetryMatch = viewModel::loadMatch,
            onPostClick = onPostClick,
            onAuthorClick = onAuthorClick,
            modifier = Modifier.padding(padding),
        )
    }
}

/**
 * Match detail — stateless content. Renders the match header followed by
 * the paged posts thread.
 */
@Composable
fun MatchDetailContent(
    state: MatchDetailUiState,
    posts: LazyPagingItems<Post>,
    onRetryMatch: () -> Unit,
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    when {
        state.loading -> WC26LoadingState(modifier = modifier)

        state.error != null -> WC26ErrorState(
            message = stringResource(state.error.toStringRes()),
            onRetry = onRetryMatch,
            modifier = modifier,
        )

        state.match != null -> LazyColumn(
            modifier = modifier.fillMaxSize(),
        ) {
            // --- Header: the match itself ---
            item {
                MatchHeader(match = state.match)
                HorizontalDivider()
                Text(
                    text = stringResource(R.string.match_detail_thread),
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(Spacing.lg),
                )
            }

            // --- The posts thread ---
            postsThread(
                posts = posts,
                onPostClick = onPostClick,
                onAuthorClick = onAuthorClick,
            )
        }
    }
}

/** The match info header — teams, score/status, venue, kickoff. */
@Composable
private fun MatchHeader(match: Match) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.xl),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = match.stage,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Spacer(Modifier.height(Spacing.md))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = match.homeTeam,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
            Box(
                modifier = Modifier.padding(horizontal = Spacing.md),
                contentAlignment = Alignment.Center,
            ) {
                if (match.hasScore) {
                    Text(
                        text = "${match.homeScore} - ${match.awayScore}",
                        style = MaterialTheme.typography.displaySmall,
                        fontWeight = FontWeight.Bold,
                    )
                } else {
                    Text(
                        text = stringResource(R.string.match_vs),
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            Text(
                text = match.awayTeam,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.height(Spacing.md))

        when (match.status) {
            MatchStatus.LIVE -> LiveBadge(label = stringResource(R.string.match_live))
            MatchStatus.FINISHED -> Text(
                text = stringResource(R.string.match_full_time),
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            else -> Text(
                text = WC26DateTime.dateTime(match.kickoffAt.toString()),
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(Modifier.height(Spacing.sm))
        Text(
            text = match.venue,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}