package com.adel.wc26.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.adel.wc26.core.designsystem.theme.Spacing
import com.adel.wc26.core.designsystem.theme.WC26Theme

/**
 * A small "LIVE" badge with a pulsing dot — for matches in progress.
 * Uses the brand's "live" green from the extended theme tokens.
 */
@Composable
fun LiveBadge(
    label: String,
    modifier: Modifier = Modifier,
) {
    val live = WC26Theme.extended.live

    val transition = rememberInfiniteTransition(label = "live")
    val pulse by transition.animateFloat(
        initialValue = 1f,
        targetValue = 0.3f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "live-pulse",
    )

    Row(
        modifier = modifier
            .clip(RoundedCornerShape(999.dp))
            .background(live.copy(alpha = 0.14f))
            .padding(horizontal = Spacing.sm, vertical = Spacing.xxs),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
    ) {
        androidx.compose.foundation.layout.Box(
            modifier = Modifier
                .size(6.dp)
                .alpha(pulse)
                .clip(CircleShape)
                .background(live),
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = live,
        )
    }
}

@androidx.compose.ui.tooling.preview.Preview
@Composable
private fun LiveBadgePreview() {
    WC26Theme {
        androidx.compose.foundation.layout.Box(Modifier.padding(Spacing.lg)) {
            LiveBadge(label = "LIVE")
        }
    }
}