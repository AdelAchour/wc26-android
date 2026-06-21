package com.adel.wc26.feature.predictions.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.outlined.History
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.feature.predictions.domain.model.PredictionStats

/**
 * Prediction stats block for the profile screens — points, rank, exact-score
 * count. Self-hides if the user has never made a prediction.
 */
@Composable
fun PredictionStatsCard(
    stats: PredictionStats,
    modifier: Modifier = Modifier,
    onHistoryClick: (() -> Unit)? = null,
) {
    if (stats.predictionsCount == 0) return

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = Spacing.sm),
        ) {
            Icon(
                imageVector = Icons.Default.AutoAwesome,
                contentDescription = null,
                tint = SparkGold,
                modifier = Modifier.size(18.dp),
            )
            Spacer(Modifier.width(Spacing.xs))
            Text(
                text = stringResource(R.string.prediction_stats_header),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f),
            )
            if (onHistoryClick != null) {
                IconButton(onClick = onHistoryClick, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.History,
                        contentDescription = stringResource(R.string.prediction_history_open),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(20.dp),
                    )
                }
            }
        }

        Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
            StatCell(
                label = stringResource(R.string.prediction_stats_points),
                value = stats.totalPoints.toString(),
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = stringResource(R.string.prediction_stats_rank),
                value = stats.rank?.let { "#$it" } ?: stringResource(R.string.prediction_stats_none),
                modifier = Modifier.weight(1f),
            )
            StatCell(
                label = stringResource(R.string.prediction_stats_exact),
                value = stats.exactCount.toString(),
                modifier = Modifier.weight(1f),
            )
        }
    }
}

@Composable
private fun StatCell(
    label: String,
    value: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                RoundedCornerShape(16.dp),
            )
            .padding(vertical = Spacing.md),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
