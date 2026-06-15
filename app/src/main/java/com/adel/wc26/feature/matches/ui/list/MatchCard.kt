package com.adel.wc26.feature.matches.ui.list

import androidx.compose.foundation.BorderStroke
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
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.dp
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.component.LiveBadge
import com.adel.wc26.core.designsystem.component.TeamFlag
import com.adel.wc26.core.designsystem.component.TeamNameWithFlag
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme
import com.adel.wc26.core.util.WC26DateTime
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.domain.model.MatchStatus
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.runtime.getValue
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.painterResource
import java.time.Instant
import kotlin.math.cos
import kotlin.math.sin

/**
 * A single match, as a tappable card.
 *
 * Checks if this is the final match. If it is, it renders the animated
 * [FinalMatchCard] with a glowing gold border and a trophy; otherwise,
 * it renders the standard [NormalMatchCard].
 */
@Composable
fun MatchCard(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val isFinal = match.stage.equals("Final", ignoreCase = true)
    if (isFinal) {
        FinalMatchCard(match = match, onClick = onClick, modifier = modifier)
    } else {
        NormalMatchCard(match = match, onClick = onClick, modifier = modifier)
    }
}

@Composable
private fun NormalMatchCard(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3F),
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        MatchCardContent(match = match, isFinal = false)
    }
}

@Composable
private fun FinalMatchCard(
    match: Match,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Infinite transition for the rotating light effect
    val infiniteTransition = rememberInfiniteTransition(label = "goldBorderTransition")
    val angle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "goldBorderAngle"
    )

    Card(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .drawWithContent {
                // 1. Draw the card background & content
                drawContent()

                // 2. Compute rotating linear gradient coordinates around the center of the card
                val angleRad = Math.toRadians(angle.toDouble())
                val xOffset = cos(angleRad).toFloat()
                val yOffset = sin(angleRad).toFloat()

                // Metallic gold brush with a bright highlight reflection in the center
                val goldBrush = Brush.linearGradient(
                    colors = listOf(
                        Color(0xFFD4AF37), // Metallic Gold
                        Color(0xFFFFF8DC), // Cornsilk highlight
                        Color(0xFFD4AF37), // Metallic Gold
                        Color(0x00D4AF37), // Fade to transparent
                        Color(0x00D4AF37),
                        Color(0xFFD4AF37), // Metallic Gold
                    ),
                    start = Offset(
                        x = (0.5f + xOffset * 0.5f) * size.width,
                        y = (0.5f + yOffset * 0.5f) * size.height
                    ),
                    end = Offset(
                        x = (0.5f - xOffset * 0.5f) * size.width,
                        y = (0.5f - yOffset * 0.5f) * size.height
                    )
                )

                // 3. Draw the animated glowing border over the content
                // 12.dp matches the default Card corner radius
                drawRoundRect(
                    brush = goldBrush,
                    cornerRadius = CornerRadius(12.dp.toPx(), 12.dp.toPx()),
                    style = Stroke(width = 2.dp.toPx())
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3F),
        ),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
        )
    ) {
        MatchCardContent(match = match, isFinal = true)
    }
}

@Composable
private fun MatchCardContent(
    match: Match,
    isFinal: Boolean,
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
                    text = "${WC26DateTime.dateOnly(match.kickoffAt.toString())} · ${stringResource(R.string.match_ft)}",
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
            TeamNameWithFlag(
                name = match.homeTeam,
                code = match.homeTeamCode,
                isHome = true,
                modifier = Modifier.weight(1f),
            )
            ScoreOrVs(match = match, isFinal = isFinal)
            TeamNameWithFlag(
                name = match.awayTeam,
                code = match.awayTeamCode,
                isHome = false,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(Modifier.padding(top = Spacing.md))

        // --- Venue footer ---
        Row(
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = match.venue,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            Spacer(modifier = Modifier.width(Spacing.xs))
            TeamFlag(code = match.countryCode, size = DpSize(16.dp, 11.dp))
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
private fun ScoreOrVs(
    match: Match,
    isFinal: Boolean = false,
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
        modifier = Modifier
            .width(75.dp)
            .padding(horizontal = Spacing.sm)
    ) {
        if (isFinal) {
            Icon(
                painter = painterResource(id = R.drawable.wc_trophy),
                contentDescription = "World Cup Trophy",
                tint = Color.Unspecified, // Keep the webp's colorful details
                modifier = Modifier
                    .size(30.dp)
                    .padding(bottom = Spacing.xs)
            )
        }
        if (match.hasScore) {
            Text(
                text = "${match.homeScore}  -  ${match.awayScore}",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                textAlign = TextAlign.Center,
            )
        } else {
            Text(
                text = stringResource(R.string.match_vs),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )
        }
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
    homeTeamCode = "ca",
    awayTeamCode = "mx",
)

@Preview(showBackground = true)
@Composable
private fun MatchCardUpcomingPreview() {
    WC26Theme {
        MatchCard(match = sampleMatch(MatchStatus.SCHEDULED), onClick = {},
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

@Preview(showBackground = true)
@Composable
private fun MatchCardFinalPreview() {
    WC26Theme {
        MatchCard(
            match = sampleMatch(MatchStatus.SCHEDULED).copy(stage = "Final"),
            onClick = {},
            modifier = Modifier.padding(Spacing.lg)
        )
    }
}