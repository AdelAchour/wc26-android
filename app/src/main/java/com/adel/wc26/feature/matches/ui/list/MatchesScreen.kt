package com.adel.wc26.feature.matches.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import com.adel.wc26.feature.predictions.domain.model.Prediction
import com.adel.wc26.feature.predictions.ui.PredictionBottomSheet
import java.time.Instant
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.IconButton
import androidx.compose.material3.Icon
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.ui.Alignment
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.ui.unit.dp

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
    isPickerMode: Boolean = false,
    onBackClick: () -> Unit = {},
    onSignInPrompt: () -> Unit = {},
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    var sheetMatch by remember { mutableStateOf<Match?>(null) }

    MatchesContent(
        state = state,
        onFilterSelected = viewModel::onFilterSelected,
        onRetry = viewModel::retry,
        onMatchClick = onMatchClick,
        isPickerMode = isPickerMode,
        onBackClick = onBackClick,
        // No predict chips in picker mode (it's for choosing a match to post about).
        onPredictClick = if (isPickerMode) null else { match ->
            if (state.isLoggedIn) sheetMatch = match else onSignInPrompt()
        },
        modifier = modifier,
    )

    sheetMatch?.let { match ->
        PredictionBottomSheet(
            match = match,
            existing = match.prediction,
            onDismiss = { sheetMatch = null },
            onSaved = {
                viewModel.onPredictionSaved(it)
                sheetMatch = null
            },
        )
    }
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
    isPickerMode: Boolean = false,
    onBackClick: () -> Unit = {},
    onPredictClick: ((Match) -> Unit)? = null,
) {
    Column(modifier = modifier.fillMaxSize()) {

        // Title row with optional back arrow
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    start = if (isPickerMode) Spacing.md else Spacing.lg,
                    end = Spacing.lg,
                    top = Spacing.lg,
                    bottom = Spacing.md,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            if (isPickerMode) {
                IconButton(onClick = onBackClick) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.action_back),
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
            }
            Text(
                text = if (isPickerMode) {
                    stringResource(R.string.matches_picker_title)
                } else {
                    stringResource(R.string.matches_title)
                },
                style = MaterialTheme.typography.displaySmall,
            )
        }

        // Filter row.
        MatchFilterRow(
            selected = state.filter,
            onSelect = onFilterSelected,
        )

        Spacer(Modifier.height(Spacing.md))

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f))

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
                contentPadding = PaddingValues(
                    start = Spacing.lg,
                    end = Spacing.lg,
                    top = Spacing.lg,
                    bottom = Spacing.lg + 96.dp + WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
                ),
                verticalArrangement = Arrangement.spacedBy(Spacing.md),
            ) {
                items(
                    items = state.matches,
                    key = { it.id },
                ) { match ->
                    MatchCard(
                        match = match,
                        onClick = { onMatchClick(match.id) },
                        prediction = match.prediction,
                        onPredictClick = onPredictClick?.let { cb -> { cb(match) } },
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
        status = MatchStatus.SCHEDULED, homeScore = null, awayScore = null,
        homeTeamCode = "ca", awayTeamCode = "mx",
    ),
    Match(
        id = 2, gameNumber = 2, homeTeam = "USA", awayTeam = "France",
        stage = "Group B", venue = "SoFi Stadium, Los Angeles", countryCode = "US",
        kickoffAt = Instant.parse("2026-06-14T22:00:00Z"),
        status = MatchStatus.LIVE, homeScore = 1, awayScore = 1,
        homeTeamCode = "us", awayTeamCode = "fr",
    ),
    Match(
        id = 3, gameNumber = 3, homeTeam = "Argentina", awayTeam = "Brazil",
        stage = "Group C", venue = "Estadio Azteca, Mexico City", countryCode = "MX",
        kickoffAt = Instant.parse("2026-06-13T20:00:00Z"),
        status = MatchStatus.FINISHED, homeScore = 2, awayScore = 0,
        homeTeamCode = "ar", awayTeamCode = "br",
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