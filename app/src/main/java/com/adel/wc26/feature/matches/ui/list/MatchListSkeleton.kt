package com.adel.wc26.feature.matches.ui.list

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adel.wc26.core.designsystem.component.SkeletonLine
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme

/**
 * A loading placeholder for the matches list — a column of shimmering
 * card-shaped skeletons. Feels faster and more intentional than a spinner.
 */
@Composable
fun MatchListSkeleton(
    modifier: Modifier = Modifier,
    itemCount: Int = 6,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding = PaddingValues(Spacing.lg),
        verticalArrangement = Arrangement.spacedBy(Spacing.md),
    ) {
        items(count = itemCount) {
            MatchCardSkeleton()
        }
    }
}

@Composable
private fun MatchCardSkeleton() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                SkeletonLine(height = 12.dp, modifier = Modifier.width(64.dp))
                SkeletonLine(height = 12.dp, modifier = Modifier.width(80.dp))
            }
            Spacer(Modifier.height(Spacing.lg))
            SkeletonLine(height = 20.dp, modifier = Modifier.fillMaxWidth())
            Spacer(Modifier.height(Spacing.md))
            SkeletonLine(height = 12.dp, modifier = Modifier.width(120.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchListSkeletonPreview() {
    WC26Theme {
        MatchListSkeleton()
    }
}