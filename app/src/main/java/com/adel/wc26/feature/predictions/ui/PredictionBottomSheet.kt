package com.adel.wc26.feature.predictions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilledTonalIconButton
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adel.wc26.core.designsystem.component.TeamFlag
import com.adel.wc26.core.designsystem.component.WC26PrimaryButton
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.ui.toStringRes
import com.adel.wc26.core.util.WC26DateTime
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.predictions.domain.model.Prediction

/**
 * Shared bottom sheet for entering or editing a score prediction. Opened from
 * the match-detail strip (and, later, the matches-list chip). Self-contained:
 * seeds from [existing], submits via its own ViewModel, and reports the saved
 * prediction back through [onSaved].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionBottomSheet(
    match: Match,
    existing: Prediction?,
    onDismiss: () -> Unit,
    onSaved: (Prediction) -> Unit,
    viewModel: PredictionSheetViewModel = hiltViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    LaunchedEffect(match.id) {
        viewModel.configure(match.id, existing?.homeScore, existing?.awayScore)
    }
    LaunchedEffect(state.saved) {
        state.saved?.let {
            onSaved(it)
            viewModel.consumeSaved()
        }
    }

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.xl)
                .padding(bottom = Spacing.xl),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(com.adel.wc26.R.string.prediction_your_prediction),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
            )
            Spacer(Modifier.height(Spacing.xs))
            Text(
                text = stringResource(
                    com.adel.wc26.R.string.prediction_locks_at,
                    WC26DateTime.dateTime(match.kickoffAt.toString()),
                ),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            Spacer(Modifier.height(Spacing.xl))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Top,
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
            ) {
                TeamScoreStepper(
                    teamName = match.homeTeam,
                    teamCode = match.homeTeamCode,
                    value = state.home,
                    onIncrement = viewModel::incHome,
                    onDecrement = viewModel::decHome,
                    modifier = Modifier.weight(1f),
                )
                Text(
                    text = stringResource(com.adel.wc26.R.string.match_vs),
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 52.dp),
                )
                TeamScoreStepper(
                    teamName = match.awayTeam,
                    teamCode = match.awayTeamCode,
                    value = state.away,
                    onIncrement = viewModel::incAway,
                    onDecrement = viewModel::decAway,
                    modifier = Modifier.weight(1f),
                )
            }

            if (state.error != null) {
                Spacer(Modifier.height(Spacing.lg))
                Text(
                    text = stringResource(state.error!!.toStringRes()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                    textAlign = TextAlign.Center,
                )
            }

            Spacer(Modifier.height(Spacing.xl))

            WC26PrimaryButton(
                text = stringResource(
                    if (existing == null) com.adel.wc26.R.string.prediction_submit
                    else com.adel.wc26.R.string.prediction_update
                ),
                onClick = viewModel::submit,
                loading = state.submitting,
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(Spacing.md))
            Text(
                text = stringResource(com.adel.wc26.R.string.prediction_rules_footnote),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun TeamScoreStepper(
    teamName: String,
    teamCode: String?,
    value: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        TeamFlag(code = teamCode, size = DpSize(40.dp, 28.dp))
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = teamName,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            maxLines = 1,
        )
        Spacer(Modifier.height(Spacing.md))
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
        ) {
            FilledTonalIconButton(onClick = onDecrement, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.Remove,
                    contentDescription = stringResource(com.adel.wc26.R.string.prediction_decrease),
                )
            }
            Text(
                text = value.toString(),
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.width(36.dp),
                textAlign = TextAlign.Center,
            )
            FilledTonalIconButton(onClick = onIncrement, modifier = Modifier.size(40.dp)) {
                Icon(
                    Icons.Default.Add,
                    contentDescription = stringResource(com.adel.wc26.R.string.prediction_increase),
                )
            }
        }
    }
}
