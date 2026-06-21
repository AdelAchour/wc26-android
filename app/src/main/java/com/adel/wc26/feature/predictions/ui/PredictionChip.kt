package com.adel.wc26.feature.predictions.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.predictions.domain.model.Prediction
import androidx.compose.foundation.background

/**
 * Compact prediction status on a match card. Accent "Predict" CTA when open and
 * unpicked (the only loud state); a quiet pick/points pill otherwise. Only the
 * open states are tappable — tapping opens the prediction sheet via [onPredictClick].
 * Renders nothing when the match isn't predictable / the user didn't pick.
 */
/** Gold tint for the prediction spark — reads on both light and dark pill backgrounds. */
private val SparkGold = Color(0xFFFFC107)

@Composable
fun PredictionChip(
    match: Match,
    prediction: Prediction?,
    onPredictClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val phase = remember(match.status, match.kickoffAt, match.homeTeamCode, match.awayTeamCode) {
        match.predictionPhase()
    }
    val scheme = MaterialTheme.colorScheme

    when (phase) {
        PredictionPhase.NOT_PREDICTABLE -> Unit

        PredictionPhase.OPEN -> if (prediction == null) {
            Pill(
                text = stringResource(R.string.prediction_predict_cta),
                background = scheme.primaryContainer,
                foreground = scheme.onPrimaryContainer,
                leadingIcon = Icons.Default.AutoAwesome,
                onClick = onPredictClick,
                modifier = modifier,
            )
        } else {
            Pill(
                text = stringResource(R.string.prediction_score_short, prediction.homeScore, prediction.awayScore),
                background = scheme.secondaryContainer,
                foreground = scheme.onSecondaryContainer,
                leadingIcon = Icons.Default.AutoAwesome,
                onClick = onPredictClick,
                modifier = modifier,
            )
        }

        PredictionPhase.LOCKED -> if (prediction != null) {
            Pill(
                text = stringResource(R.string.prediction_score_short, prediction.homeScore, prediction.awayScore),
                background = scheme.surfaceVariant.copy(alpha = 0.5f),
                foreground = scheme.onSurfaceVariant,
                leadingIcon = Icons.Default.AutoAwesome,
                onClick = null,
                modifier = modifier,
            )
        }

        PredictionPhase.FINISHED -> if (prediction != null) {
            val points = prediction.pointsAwarded
            if (points != null && points > 0) {
                Pill(
                    text = stringResource(R.string.prediction_points_short, points),
                    background = scheme.tertiaryContainer,
                    foreground = scheme.onTertiaryContainer,
                    leadingIcon = Icons.Default.AutoAwesome,
                    onClick = null,
                    modifier = modifier,
                )
            } else {
                Pill(
                    text = stringResource(R.string.prediction_score_short, prediction.homeScore, prediction.awayScore),
                    background = scheme.surfaceVariant.copy(alpha = 0.5f),
                    foreground = scheme.onSurfaceVariant,
                    leadingIcon = Icons.Default.AutoAwesome,
                    onClick = null,
                    modifier = modifier,
                )
            }
        }
    }
}

@Composable
private fun Pill(
    text: String,
    background: Color,
    foreground: Color,
    leadingIcon: ImageVector?,
    onClick: (() -> Unit)?,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .background(background)
            .padding(horizontal = Spacing.md, vertical = Spacing.xs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xxs),
    ) {
        if (leadingIcon != null) {
            Icon(
                imageVector = leadingIcon,
                contentDescription = null,
                tint = SparkGold,
                modifier = Modifier.size(14.dp),
            )
        }
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.SemiBold,
            color = foreground,
        )
    }
}
