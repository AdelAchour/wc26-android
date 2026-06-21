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
import androidx.compose.material3.FloatingActionButton
import androidx.compose.foundation.shape.CircleShape
import com.adel.wc26.core.designsystem.component.StylusNote
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
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
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
import com.adel.wc26.feature.posts.ui.component.postsThread
import com.adel.wc26.feature.predictions.domain.model.Prediction
import com.adel.wc26.feature.predictions.ui.PredictionBottomSheet
import com.adel.wc26.feature.predictions.ui.PredictionStrip
import android.widget.Toast
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.TextButton
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.ui.unit.DpSize
import com.adel.wc26.core.designsystem.component.TeamFlag
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.background
import androidx.compose.foundation.shape.RoundedCornerShape

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
    onEditClick: (matchId: Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MatchDetailViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val posts = viewModel.posts.collectAsLazyPagingItems()
    val currentUserId by viewModel.currentUserId.collectAsStateWithLifecycle(initialValue = null)
    val context = LocalContext.current

    var postToDelete by remember { mutableStateOf<Post?>(null) }
    var showPredictionSheet by remember { mutableStateOf(false) }

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
                actions = {
                    if (state.isAdmin && state.match != null) {
                        var showMenu by remember { mutableStateOf(false) }
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Match options"
                                )
                            }
                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                DropdownMenuItem(
                                    text = { Text(stringResource(R.string.admin_edit_match_title)) },
                                    onClick = {
                                        showMenu = false
                                        onEditClick(state.match!!.id)
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Edit,
                                            contentDescription = null
                                        )
                                    }
                                )
                            }
                        }
                    }
                }
            )
        },
        floatingActionButton = {
            state.match?.let { match ->
                FloatingActionButton(
                    onClick = {
                        if (state.isLoggedIn) onComposeClick(match.id)
                        else onSignInPrompt()
                    },
                    shape = CircleShape,
                ) {
                    Icon(
                        imageVector = StylusNote,
                        contentDescription = stringResource(R.string.match_detail_compose)
                    )
                }
            }
        },
    ) { padding ->
        MatchDetailContent(
            state = state,
            posts = posts,
            currentUserId = currentUserId,
            onRetryMatch = viewModel::loadMatch,
            onRefresh = {
                viewModel.loadMatch(isRefresh = true)
                posts.refresh()
            },
            onPostClick = onPostClick,
            onAuthorClick = onAuthorClick,
            onLikeClick = viewModel::toggleLike,
            onDeleteClick = { postToDelete = it },
            prediction = state.prediction,
            onPredictClick = {
                if (state.isLoggedIn) showPredictionSheet = true else onSignInPrompt()
            },
            modifier = Modifier.padding(padding),
        )

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
                            if (post != null) {
                                viewModel.deletePost(post)
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

        if (showPredictionSheet) {
            state.match?.let { match ->
                PredictionBottomSheet(
                    match = match,
                    existing = state.prediction,
                    onDismiss = { showPredictionSheet = false },
                    onSaved = {
                        viewModel.onPredictionSaved(it)
                        showPredictionSheet = false
                    },
                )
            }
        }
    }
}

/**
 * Match detail — stateless content. Renders the match header followed by
 * the paged posts thread.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchDetailContent(
    state: MatchDetailUiState,
    posts: LazyPagingItems<Post>,
    currentUserId: Long?,
    onRetryMatch: () -> Unit,
    onRefresh: () -> Unit,
    onPostClick: (Long) -> Unit,
    onAuthorClick: (Long) -> Unit,
    onLikeClick: (Post) -> Unit,
    onDeleteClick: (Post) -> Unit,
    prediction: Prediction?,
    onPredictClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val emptyMessage = stringResource(R.string.match_detail_thread_empty)

    when {
        state.loading -> WC26LoadingState(modifier = modifier)

        state.error != null -> WC26ErrorState(
            message = stringResource(state.error.toStringRes()),
            onRetry = onRetryMatch,
            modifier = modifier,
        )

        state.match != null -> {
            val isRefreshing = state.isRefreshing || (posts.loadState.refresh is LoadState.Loading && posts.itemCount > 0)

            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = onRefresh,
                modifier = modifier.fillMaxSize()
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    // --- Header: the match itself ---
                    item {
                        MatchHeader(match = state.match)
                        PredictionStrip(
                            match = state.match,
                            prediction = prediction,
                            onClick = onPredictClick,
                            modifier = Modifier
                                .padding(horizontal = Spacing.lg)
                                .padding(bottom = Spacing.md),
                        )
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
                        onLikeClick = onLikeClick,
                        onAuthorClick = onAuthorClick,
                        emptyMessage = emptyMessage,
                        currentUserId = currentUserId,
                        onDeleteClick = onDeleteClick,
                    )
                }
            }
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
            verticalAlignment = Alignment.Top,
        ) {
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TeamFlag(code = match.homeTeamCode, size = DpSize(40.dp, 28.dp))
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = match.homeTeam,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.CenterVertically)
                    .padding(horizontal = Spacing.md),
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
            Column(
                modifier = Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TeamFlag(code = match.awayTeamCode, size = DpSize(40.dp, 28.dp))
                Spacer(Modifier.height(Spacing.xs))
                Text(
                    text = match.awayTeam,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
            }
        }

        Spacer(Modifier.height(Spacing.md))

        MatchStatusBadge(status = match.status)

        Spacer(Modifier.height(Spacing.sm))

        Text(
            text = stringResource(R.string.match_kickoff_label, WC26DateTime.dateTime(match.kickoffAt.toString())),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(Spacing.sm))
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            TeamFlag(code = match.countryCode, size = DpSize(16.dp, 11.dp))
            Spacer(modifier = Modifier.width(Spacing.xs))
            Text(
                text = match.venue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun MatchStatusBadge(
    status: MatchStatus,
    modifier: Modifier = Modifier,
) {
    when (status) {
        MatchStatus.LIVE -> {
            LiveBadge(label = stringResource(R.string.match_live), modifier = modifier)
        }
        MatchStatus.FINISHED -> {
            Row(
                modifier = modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f))
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.match_ft).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        else -> {
            Row(
                modifier = modifier
                    .clip(RoundedCornerShape(999.dp))
                    .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.5f))
                    .padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.match_upcoming).uppercase(),
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSecondaryContainer,
                )
            }
        }
    }
}