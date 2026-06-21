package com.adel.wc26.feature.predictions.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26Avatar
import com.adel.wc26.core.designsystem.component.WC26ErrorState
import com.adel.wc26.core.designsystem.component.WC26LoadingState
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.ui.toStringRes
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.ui.list.MatchCard
import com.adel.wc26.feature.predictions.domain.model.LeaderboardEntry
import com.adel.wc26.feature.predictions.domain.model.MyRank
import com.adel.wc26.feature.predictions.ui.PredictionBottomSheet

/**
 * Predictions tab — today's open picks + the leaderboard.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionsScreen(
    onMatchClick: (Long) -> Unit,
    onSignInPrompt: () -> Unit,
    onUserClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: PredictionsViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var sheetMatch by remember { mutableStateOf<Match?>(null) }

    Column(modifier = modifier.fillMaxSize()) {
        Text(
            text = stringResource(R.string.prediction_tab_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(start = Spacing.lg, end = Spacing.lg, top = Spacing.lg, bottom = Spacing.md),
        )

        when {
            state.loading -> WC26LoadingState(modifier = Modifier.fillMaxSize())

            state.error != null -> WC26ErrorState(
                message = stringResource(state.error!!.toStringRes()),
                onRetry = viewModel::retry,
            )

            else -> PullToRefreshBox(
                isRefreshing = state.refreshing,
                onRefresh = viewModel::refresh,
                modifier = Modifier.fillMaxSize(),
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = Spacing.lg,
                        end = Spacing.lg,
                        top = Spacing.sm,
                        bottom = Spacing.lg + 96.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding(),
                    ),
                    verticalArrangement = Arrangement.spacedBy(Spacing.md),
                ) {
                    // --- Today's open picks ---
                    if (state.todayPicks.isNotEmpty()) {
                        item {
                            SectionHeader(stringResource(R.string.prediction_today_header, state.todayPicks.size))
                        }
                        items(state.todayPicks, key = { it.id }) { match ->
                            MatchCard(
                                match = match,
                                onClick = { onMatchClick(match.id) },
                                prediction = match.prediction,
                                onPredictClick = {
                                    if (state.isLoggedIn) sheetMatch = match else onSignInPrompt()
                                },
                            )
                        }
                    }

                    // --- Leaderboard ---
                    item { SectionHeader(stringResource(R.string.prediction_leaderboard_header)) }

                    state.me?.let { me ->
                        item { YourRankRow(me) }
                    }

                    if (state.leaderboard.isEmpty()) {
                        item {
                            Text(
                                text = stringResource(R.string.prediction_leaderboard_empty),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(vertical = Spacing.md),
                            )
                        }
                    } else {
                        items(state.leaderboard, key = { it.user.id }) { entry ->
                            LeaderboardRow(entry, onClick = { onUserClick(entry.user.id) })
                        }
                    }
                }
            }
        }
    }

    sheetMatch?.let { match ->
        PredictionBottomSheet(
            match = match,
            existing = match.prediction,
            onDismiss = { sheetMatch = null },
            onSaved = { sheetMatch = null },
        )
    }
}

@Composable
private fun SectionHeader(text: String) {
    Text(
        text = text,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = Spacing.sm),
    )
}

@Composable
private fun YourRankRow(me: MyRank) {
    Surface(
        color = MaterialTheme.colorScheme.primaryContainer,
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            Text(
                text = stringResource(R.string.prediction_rank, me.rank),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
            Text(
                text = stringResource(R.string.prediction_you),
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.prediction_points_total, me.totalPoints),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Text(
            text = stringResource(R.string.prediction_rank, entry.rank),
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.End,
            // Fixed width keeps avatars aligned regardless of #1 vs #100.
            modifier = Modifier.width(40.dp),
        )
        WC26Avatar(displayName = entry.user.displayName, avatarUrl = entry.user.avatarUrl, size = 36.dp)
        Text(
            text = entry.user.displayName,
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1,
            modifier = Modifier.weight(1f),
        )
        Text(
            text = stringResource(R.string.prediction_points_total, entry.totalPoints),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
        )
    }
}
