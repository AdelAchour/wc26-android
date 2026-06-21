package com.adel.wc26.feature.predictions.ui

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.predictions.domain.model.Prediction

/**
 * Prediction status block near the top of the match-detail screen. Always leads
 * with the gold spark so it reads as the prediction feature. Accent CTA when
 * open & unpredicted; quieter informational states otherwise. Tapping (only
 * when open) opens the prediction bottom sheet.
 */
@Composable
fun PredictionStrip(
    match: Match,
    prediction: Prediction?,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase = remember(match.status, match.kickoffAt, match.homeTeamCode, match.awayTeamCode) {
        match.predictionPhase()
    }

    when (phase) {
        PredictionPhase.NOT_PREDICTABLE -> Unit

        PredictionPhase.OPEN -> if (prediction != null) {
            StripCard(
                accent = false,
                title = stringResource(R.string.prediction_your_pick, prediction.homeScore, prediction.awayScore),
                subtitle = stringResource(R.string.prediction_editable_until_kickoff),
                trailing = { TrailingIcon(Icons.Default.Edit, MaterialTheme.colorScheme.onSurfaceVariant) },
                onClick = onClick,
                modifier = modifier,
            )
        } else {
            StripCard(
                accent = true,
                title = stringResource(R.string.prediction_open_cta),
                subtitle = stringResource(R.string.prediction_open_tap),
                trailing = { TrailingIcon(Icons.AutoMirrored.Filled.KeyboardArrowRight, MaterialTheme.colorScheme.onPrimaryContainer) },
                onClick = onClick,
                modifier = modifier,
            )
        }

        PredictionPhase.LOCKED -> if (prediction != null) {
            StripCard(
                accent = false,
                title = stringResource(R.string.prediction_your_pick, prediction.homeScore, prediction.awayScore),
                subtitle = stringResource(R.string.prediction_locked),
                trailing = { TrailingIcon(Icons.Default.Lock, MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = modifier,
            )
        } else {
            StripCard(
                accent = false,
                title = stringResource(R.string.prediction_closed_title),
                subtitle = stringResource(R.string.prediction_didnt_pick),
                trailing = { TrailingIcon(Icons.Default.Lock, MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = modifier,
            )
        }

        PredictionPhase.FINISHED -> if (prediction != null) {
            StripCard(
                accent = false,
                title = stringResource(R.string.prediction_your_pick, prediction.homeScore, prediction.awayScore),
                subtitle = null,
                trailing = { PointsPill(prediction.pointsAwarded) },
                modifier = modifier,
            )
        } else {
            StripCard(
                accent = false,
                title = stringResource(R.string.prediction_closed_title),
                subtitle = stringResource(R.string.prediction_didnt_pick),
                trailing = { TrailingIcon(Icons.Default.Lock, MaterialTheme.colorScheme.onSurfaceVariant) },
                modifier = modifier,
            )
        }
    }
}

@Composable
private fun StripCard(
    accent: Boolean,
    title: String,
    subtitle: String?,
    trailing: (@Composable () -> Unit)?,
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
) {
    val container = if (accent) {
        MaterialTheme.colorScheme.primaryContainer
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
    }
    val onContainer = if (accent) {
        MaterialTheme.colorScheme.onPrimaryContainer
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = container),
        border = if (accent) null else BorderStroke(
            1.dp, MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        ),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = Spacing.lg, vertical = Spacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(Spacing.md),
        ) {
            // Always the spark — signals "this is your prediction".
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = SparkGold,
                modifier = Modifier.size(20.dp),
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = onContainer,
                )
                if (subtitle != null) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = if (accent) onContainer.copy(alpha = 0.8f)
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            trailing?.invoke()
        }
    }
}

@Composable
private fun TrailingIcon(icon: ImageVector, tint: Color) {
    Icon(
        imageVector = icon,
        contentDescription = null,
        tint = tint,
        modifier = Modifier.size(18.dp),
    )
}

@Composable
private fun PointsPill(points: Int?) {
    if (points == null) return
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
            else stringResource(R.string.prediction_points_zero),
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Bold,
            color = fg,
        )
    }
}
