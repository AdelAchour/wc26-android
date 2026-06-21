package com.adel.wc26.feature.predictions.ui.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.ui.toStringRes
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.predictions.ui.SparkGold

/** Scrollable bottom sheet of a user's finished-match prediction history. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionHistorySheet(
    userId: Long,
    onDismiss: () -> Unit,
    viewModel: PredictionHistoryViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(userId) { viewModel.load(userId) }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xxl),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                modifier = Modifier.padding(bottom = Spacing.lg),
            ) {
                Icon(
                    imageVector = Icons.Default.AutoAwesome,
                    contentDescription = null,
                    tint = SparkGold,
                    modifier = Modifier.size(22.dp),
                )
                Text(
                    text = stringResource(R.string.prediction_history_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            when {
                state.loading -> Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(Spacing.xl),
                    contentAlignment = Alignment.Center,
                ) { CircularProgressIndicator() }

                state.error != null -> Text(
                    text = stringResource(state.error!!.toStringRes()),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error,
                    modifier = Modifier.padding(vertical = Spacing.md),
                )

                state.items.isEmpty() -> Text(
                    text = stringResource(R.string.prediction_history_empty),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(vertical = Spacing.md),
                )

                else -> LazyColumn(modifier = Modifier.fillMaxWidth().heightIn(max = 480.dp)) {
                    items(state.items, key = { it.id }) { match -> HistoryRow(match) }
                }
            }
        }
    }
}

@Composable
private fun HistoryRow(match: Match) {
    val pick = match.prediction
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = Spacing.sm),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = "${match.homeTeam} – ${match.awayTeam}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (pick != null && match.homeScore != null && match.awayScore != null) {
                Text(
                    text = stringResource(
                        R.string.prediction_history_row,
                        pick.homeScore, pick.awayScore, match.homeScore, match.awayScore,
                    ),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
        pick?.pointsAwarded?.let { HistoryPointsPill(it) }
    }
}

@Composable
private fun HistoryPointsPill(points: Int) {
    val positive = points > 0
    val bg = if (positive) MaterialTheme.colorScheme.tertiaryContainer
    else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    val fg = if (positive) MaterialTheme.colorScheme.onTertiaryContainer
    else MaterialTheme.colorScheme.onSurfaceVariant
    Row(
        modifier = Modifier
            .background(bg, RoundedCornerShape(999.dp))
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
    ) {
        Text(
            text = if (positive) stringResource(R.string.prediction_points_earned, points)
            else stringResource(R.string.prediction_points_total, points),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}
