package com.adel.wc26.feature.matches.ui.edit

import android.widget.Toast
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.WC26ErrorState
import com.adel.wc26.core.designsystem.component.WC26LoadingState
import com.adel.wc26.core.designsystem.component.WC26PrimaryButton
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.ui.toStringRes
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.domain.model.MatchStatus
import java.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MatchEditScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
    viewModel: MatchEditViewModel = hiltViewModel(),
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.success) {
        if (state.success) {
            Toast.makeText(
                context,
                context.getString(R.string.admin_edit_match_success),
                Toast.LENGTH_SHORT
            ).show()
            onBack()
        }
    }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.admin_edit_match_title)) },
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
    ) { padding ->
        val innerModifier = Modifier.padding(padding)

        when {
            state.loading -> {
                WC26LoadingState(modifier = innerModifier)
            }
            state.error != null -> {
                WC26ErrorState(
                    message = stringResource(state.error!!.toStringRes()),
                    onRetry = viewModel::loadMatch,
                    modifier = innerModifier,
                )
            }
            state.match != null -> {
                MatchEditContent(
                    match = state.match!!,
                    selectedStatus = state.selectedStatus,
                    updateScores = state.updateScores,
                    homeScore = state.homeScore,
                    awayScore = state.awayScore,
                    saving = state.saving,
                    onStatusSelected = viewModel::onStatusSelected,
                    onUpdateScoresToggled = viewModel::onUpdateScoresToggled,
                    onHomeScoreChanged = viewModel::onHomeScoreChanged,
                    onAwayScoreChanged = viewModel::onAwayScoreChanged,
                    onSave = viewModel::saveChanges,
                    modifier = innerModifier,
                )
            }
        }
    }
}

@Composable
private fun MatchEditContent(
    match: Match,
    selectedStatus: MatchStatus,
    updateScores: Boolean,
    homeScore: Int,
    awayScore: Int,
    saving: Boolean,
    onStatusSelected: (MatchStatus) -> Unit,
    onUpdateScoresToggled: (Boolean) -> Unit,
    onHomeScoreChanged: (Int) -> Unit,
    onAwayScoreChanged: (Int) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.lg)
    ) {
        // Match Context Card
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
            ),
            border = BorderStroke(
                1.dp,
                MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            )
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(Spacing.lg),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = match.stage,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(Spacing.sm))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = match.homeTeam,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.End,
                        modifier = Modifier.weight(1f)
                    )
                    ScoreOrVs(match = match)
                    Text(
                        text = match.awayTeam,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Start,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }

        // Section: Match Status Chips
        Column(verticalArrangement = Arrangement.spacedBy(Spacing.xs)) {
            Text(
                text = stringResource(R.string.admin_edit_match_status),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(Spacing.sm)
            ) {
                StatusChip(
                    label = stringResource(R.string.match_filter_upcoming),
                    icon = Icons.Default.CalendarToday,
                    selected = selectedStatus == MatchStatus.SCHEDULED,
                    onClick = { onStatusSelected(MatchStatus.SCHEDULED) },
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    label = stringResource(R.string.match_filter_live),
                    icon = Icons.Default.PlayArrow,
                    selected = selectedStatus == MatchStatus.LIVE,
                    onClick = { onStatusSelected(MatchStatus.LIVE) },
                    modifier = Modifier.weight(1f)
                )
                StatusChip(
                    label = stringResource(R.string.match_filter_finished),
                    icon = Icons.Default.CheckCircle,
                    selected = selectedStatus == MatchStatus.FINISHED,
                    onClick = { onStatusSelected(MatchStatus.FINISHED) },
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Section: Match Scores Switch + Inputs
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            ),
            border = CardDefaults.outlinedCardBorder()
        ) {
            Column(
                modifier = Modifier.padding(Spacing.md),
                verticalArrangement = Arrangement.spacedBy(Spacing.md)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.admin_edit_match_scores),
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.SemiBold
                        )
                        Text(
                            text = if (selectedStatus == MatchStatus.SCHEDULED) {
                                "Upcoming matches cannot have scores"
                            } else {
                                "Both scores must be set and non-negative"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = updateScores,
                        onCheckedChange = onUpdateScoresToggled,
                        enabled = selectedStatus != MatchStatus.SCHEDULED // <-- Disable switch for SCHEDULED
                    )
                }

                if (updateScores) {
                    HorizontalDivider(modifier = Modifier.padding(vertical = Spacing.xs))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceEvenly,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        ScoreStepper(
                            teamName = match.homeTeam,
                            score = homeScore,
                            onScoreChanged = onHomeScoreChanged
                        )

                        VerticalDivider(modifier.fillMaxHeight())


                        ScoreStepper(
                            teamName = match.awayTeam,
                            score = awayScore,
                            onScoreChanged = onAwayScoreChanged
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Save Button logic check
        val hasChanges = selectedStatus != match.status ||
                (updateScores && (homeScore != (match.homeScore ?: -1) || awayScore != (match.awayScore ?: -1)))

        WC26PrimaryButton(
            text = stringResource(R.string.admin_edit_match_save),
            onClick = onSave,
            enabled = hasChanges,
            loading = saving,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

/** The score (if the match has one) or a neutral "vs" separator. */
@Composable
private fun ScoreOrVs(match: Match) {
    if (match.hasScore) {
        Text(
            text = "${match.homeScore}  -  ${match.awayScore}",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier
                .width(80.dp)
                .padding(horizontal = Spacing.sm),
            textAlign = TextAlign.Center,
        )
    } else {
        Text(
            text = stringResource(R.string.match_vs),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .width(80.dp)
                .padding(horizontal = Spacing.sm),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun StatusChip(
    label: String,
    icon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val containerColor = if (selected) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }

    val contentColor = if (selected) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurfaceVariant
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .border(width = 1.dp, color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            .background(containerColor)
            .clickable(onClick = onClick)
            .padding(vertical = Spacing.md, horizontal = Spacing.sm),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.xs)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(18.dp)
            )
            Text(
                text = label,
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                color = contentColor,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun ScoreStepper(
    teamName: String,
    score: Int,
    onScoreChanged: (Int) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(Spacing.xs),
        modifier = modifier
    ) {
        Text(
            text = teamName,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Medium,
            textAlign = TextAlign.Center,
            maxLines = 1,
            modifier = Modifier.widthIn(max = 120.dp)
                .padding(bottom = Spacing.md)
        )
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            FilledIconButton(
                onClick = { onScoreChanged(score - 1) },
                enabled = score > 0,
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Remove,
                    contentDescription = "Decrement",
                    modifier = Modifier.size(18.dp)
                )
            }

            Text(
                text = score.toString(),
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.widthIn(min = 32.dp),
                textAlign = TextAlign.Center
            )

            FilledIconButton(
                onClick = { onScoreChanged(score + 1) },
                colors = IconButtonDefaults.filledIconButtonColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                ),
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "Increment",
                    modifier = Modifier.size(18.dp)
                )
            }
        }
    }
}

private fun previewMatch() = Match(
    id = 1,
    gameNumber = 12,
    homeTeam = "Spain",
    awayTeam = "Germany",
    stage = "Group E",
    venue = "Vancouver Stadium",
    countryCode = "QA",
    kickoffAt = Instant.parse("2026-11-27T19:00:00Z"),
    status = MatchStatus.SCHEDULED,
    homeScore = 2,
    awayScore = 1
)

@Preview(showBackground = true)
@Composable
private fun MatchEditContentPreview() {
    WC26Theme {
        MatchEditContent(
            match = previewMatch(),
            selectedStatus = MatchStatus.SCHEDULED,
            updateScores = true,
            homeScore = 1,
            awayScore = 2,
            saving = false,
            onStatusSelected = {},
            onUpdateScoresToggled = {},
            onHomeScoreChanged = {},
            onAwayScoreChanged = {},
            onSave = {}
        )
    }
}