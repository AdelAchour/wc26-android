package com.adel.wc26.feature.predictions.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.theme.Spacing

/** Explains how the prediction game works. Opened from the Predictions tab. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionRulesSheet(onDismiss: () -> Unit) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(onDismissRequest = onDismiss, sheetState = sheetState) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
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
                    text = stringResource(R.string.prediction_rules_title),
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.SemiBold,
                )
            }

            RuleSection(
                title = stringResource(R.string.prediction_rules_predict_title),
                body = stringResource(R.string.prediction_rules_predict_body),
            )
            RuleSection(
                title = stringResource(R.string.prediction_rules_lock_title),
                body = stringResource(R.string.prediction_rules_lock_body),
            )
            RuleSection(
                title = stringResource(R.string.prediction_rules_scoring_title),
                body = stringResource(R.string.prediction_rules_scoring_body),
            )
            RuleSection(
                title = stringResource(R.string.prediction_rules_et_title),
                body = stringResource(R.string.prediction_rules_et_body),
            )
            RuleSection(
                title = stringResource(R.string.prediction_rules_leaderboard_title),
                body = stringResource(R.string.prediction_rules_leaderboard_body),
            )
        }
    }
}

@Composable
private fun RuleSection(title: String, body: String) {
    Column(modifier = Modifier.padding(bottom = Spacing.lg)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.SemiBold,
        )
        Spacer(Modifier.height(Spacing.xs))
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
    }
}
