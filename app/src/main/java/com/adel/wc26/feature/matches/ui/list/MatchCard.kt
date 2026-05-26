package com.adel.wc26.feature.matches.ui.list

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.LiveBadge
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.util.WC26DateTime
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.domain.model.MatchStatus
import java.time.Instant

/**
 * A single match, as a tappable card.
 *
 * Layout: a header strip (stage · kickoff / live badge), then the two
 * teams with — when there's a result — the score between them, then the
 * venue footer.
 */
@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant,
        ),
    ) {
        Column(modifier = Modifier.padding(Spacing.lg)) {

            // --- Header: stage + kickoff/live ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = match.stage,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                when (match.status) {
                    MatchStatus.LIVE -> LiveBadge(label = stringResource(R.string.match_live))
                    MatchStatus.FINISHED -> Text(
                        text = stringResource(R.string.match_full_time),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                    else -> Text(
                        text = WC26DateTime.dateTime(match.kickoffAt.toString()),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.padding(top = Spacing.md))

            // --- Teams + score ---
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TeamName(
                    name = match.homeTeam,
                    align = TextAlign.Start,
                    modifier = Modifier.weight(1f),
                )
                ScoreOrVs(match = match)
                TeamName(
                    name = match.awayTeam,
                    align = TextAlign.End,
                    modifier = Modifier.weight(1f),
                )
            }

            Spacer(Modifier.padding(top = Spacing.md))

            // --- Venue footer ---
            Text(
                text = match.venue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun TeamName(
    name: String,
    align: TextAlign,
    modifier: Modifier = Modifier,
) {
    Text(
        text = name,
        style = MaterialTheme.typography.titleMedium,
        fontWeight = FontWeight.SemiBold,
        textAlign = align,
        modifier = modifier,
    )
}

/** The score (if the match has one) or a neutral "vs" separator. */
@Composable
private fun ScoreOrVs(match: Match) {
    if (match.hasScore) {
        Text(
            text = "${match.homeScore}  -  ${match.awayScore}",
            style = MaterialTheme.typography.titleLarge,
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

// ---- Previews ----

private fun sampleMatch(
    status: MatchStatus,
    home: Int? = null,
    away: Int? = null,
) = Match(
    id = 1,
    gameNumber = 1,
    homeTeam = "Canada",
    awayTeam = "Mexico",
    stage = "Group A",
    venue = "BMO Field, Toronto",
    countryCode = "CA",
    kickoffAt = Instant.parse("2026-06-14T19:00:00Z"),
    status = status,
    homeScore = home,
    awayScore = away,
)

@Preview(showBackground = true)
@Composable
private fun MatchCardUpcomingPreview() {
    WC26Theme {
        MatchCard(match = sampleMatch(MatchStatus.UPCOMING), onClick = {},
            modifier = Modifier.padding(Spacing.lg))
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchCardLivePreview() {
    WC26Theme {
        MatchCard(match = sampleMatch(MatchStatus.LIVE, 1, 0), onClick = {},
            modifier = Modifier.padding(Spacing.lg))
    }
}

@Preview(showBackground = true)
@Composable
private fun MatchCardFinishedPreview() {
    WC26Theme {
        MatchCard(match = sampleMatch(MatchStatus.FINISHED, 2, 1), onClick = {},
            modifier = Modifier.padding(Spacing.lg))
    }
}