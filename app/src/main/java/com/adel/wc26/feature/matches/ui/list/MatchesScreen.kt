package com.adel.wc26.feature.matches.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26EmptyState
import com.adel.wc26.core.designsystem.component.WC26ErrorState
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.ui.toStringRes
import com.adel.wc26.feature.matches.domain.MatchFilter
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.domain.model.MatchStatus
import java.time.Instant

/**
 * Matches tab — stateful entry point. Collects the ViewModel state and
 * delegates to [MatchesContent].
 *
 * @param onMatchClick navigates to the match detail screen.
 */
@Composable
fun MatchesScreen(
    onMatchClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MatchesViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    MatchesContent(
        state = state,
        onFilterSelected = viewModel::onFilterSelected,
        onRetry = viewModel::retry,
        onMatchClick = onMatchClick,
        modifier = modifier,
    )
}

/**
 * Matches tab — stateless content. Header, filter row, then one of:
 * loading skeletons, error, empty, or the list of match cards.
 */
@Composable
fun MatchesContent(
    state: MatchesUiState,
    onFilterSelected: (MatchFilter) -> Unit,
    onRetry: () -> Unit,
    onMatchClick: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(modifier = modifier.fillMaxSize()) {

        // Screen title.
        Text(
            text = stringResource(R.string.matches_title),
            style = MaterialTheme.typography.displaySmall,
            modifier = Modifier.padding(
                start = Spacing.lg,
                end = Spacing.lg,
                top = Spacing.lg,
                bottom = Spacing.md,
            ),
        )

        // Filter row.
        MatchFilterRow(
            selected = state.filter,
            onSelect = onFilterSelected,
        )

        // Body.
        when {
            state.loading -> MatchListSkeleton(
                modifier = Modifier.padding(top = Spacing.md),
            )

            state.error != null -> WC26ErrorState(
                message = stringResource(state.error.toStringRes()),
                onRetry = onRetry,
            )

            state.isEmpty -> WC26EmptyState(
                title = stringResource(R.string.matches_empty_title),
                description = stringResource(R.string.matches_empty_desc),
            )

            else -> LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(Spacing.lg),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                items(
                    items = state.matches,
                    key = { it.id },
                ) { match ->
                    MatchCard(
                        match = match,
                        onClick = { onMatchClick(match.id) },
                    )
                }
            }
        }
    }
}

// ---- Previews ----

private fun previewMatches(): List<Match> = listOf(
    Match(
        id = 1, gameNumber = 1, homeTeam = "Canada", awayTeam = "Mexico",
        stage = "Group A", venue = "BMO Field, Toronto", countryCode = "CA",
        kickoffAt = Instant.parse("2026-06-14T19:00:00Z"),
        status = MatchStatus.UPCOMING, homeScore = null, awayScore = null,
    ),
    Match(
        id = 2, gameNumber = 2, homeTeam = "USA", awayTeam = "Wales",
        stage = "Group B", venue = "SoFi Stadium, Los Angeles", countryCode = "US",
        kickoffAt = Instant.parse("2026-06-14T22:00:00Z"),
        status = MatchStatus.LIVE, homeScore = 1, awayScore = 1,
    ),
    Match(
        id = 3, gameNumber = 3, homeTeam = "Argentina", awayTeam = "Brazil",
        stage = "Group C", venue = "Estadio Azteca, Mexico City", countryCode = "MX",
        kickoffAt = Instant.parse("2026-06-13T20:00:00Z"),
        status = MatchStatus.FINISHED, homeScore = 2, awayScore = 0,
    ),
)

@Preview(showBackground = true)
@Composable
private fun MatchesContentListPreview() {
    WC26Theme {
        MatchesContent(
            state = MatchesUiState(
                filter = MatchFilter.UPCOMING,
                matches = previewMatches(),
                loading = false,
            ),
            onFilterSelected = {},
            onRetry = {},
            onMatchClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchesContentLoadingPreview() {
    WC26Theme {
        MatchesContent(
            state = MatchesUiState(loading = true),
            onFilterSelected = {},
            onRetry = {},
            onMatchClick = {},
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchesContentEmptyPreview() {
    WC26Theme {
        MatchesContent(
            state = MatchesUiState(
                filter = MatchFilter.LIVE,
                matches = emptyList(),
                loading = false,
            ),
            onFilterSelected = {},
            onRetry = {},
            onMatchClick = {},
        )
    }
}