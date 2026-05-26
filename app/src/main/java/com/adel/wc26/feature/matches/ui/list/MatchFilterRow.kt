package com.adel.wc26.feature.matches.ui.list

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.annotation.StringRes
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.feature.matches.domain.MatchFilter

/**
 * The Upcoming / Live / Finished (and All) filter, as an M3 segmented
 * button row. Drives MatchesViewModel.onFilterSelected.
 */
@Composable
fun MatchFilterRow(
    selected: MatchFilter,
    onSelect: (MatchFilter) -> Unit,
    modifier: Modifier = Modifier,
) {
    // Order shown to the user.
    val filters = listOf(
        MatchFilter.UPCOMING,
        MatchFilter.LIVE,
        MatchFilter.FINISHED,
        MatchFilter.ALL,
    )

    SingleChoiceSegmentedButtonRow(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = Spacing.lg),
    ) {
        filters.forEachIndexed { index, filter ->
            SegmentedButton(
                selected = filter == selected,
                onClick = { onSelect(filter) },
                shape = SegmentedButtonDefaults.itemShape(
                    index = index,
                    count = filters.size,
                ),
            ) {
                Text(
                    text = stringResource(filter.labelRes()),
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }
    }
}

@StringRes
private fun MatchFilter.labelRes(): Int = when (this) {
    MatchFilter.ALL -> R.string.match_filter_all
    MatchFilter.UPCOMING -> R.string.match_filter_upcoming
    MatchFilter.LIVE -> R.string.match_filter_live
    MatchFilter.FINISHED -> R.string.match_filter_finished
}

@Preview(showBackground = true)
@Composable
private fun MatchFilterRowPreview() {
    WC26Theme {
        MatchFilterRow(
            selected = MatchFilter.UPCOMING,
            onSelect = {},
            modifier = Modifier.padding(vertical = Spacing.md),
        )
    }
}