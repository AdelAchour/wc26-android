package com.adel.wc26.core.designsystem.component

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * A shimmering placeholder block. Compose skeleton screens from these —
 * they make a loading list feel faster and more intentional than a
 * single centered spinner.
 *
 * Usage: lay out grey [SkeletonBox]es in the same shape as the real
 * content (a fake card with a fake avatar circle and fake text lines).
 */
@Composable
fun SkeletonBox(
    modifier: Modifier = Modifier,
) {
    val transition = rememberInfiniteTransition(label = "skeleton")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton-alpha",
    )

    val base = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .clip(MaterialTheme.shapes.small)
            .background(
                Brush.horizontalGradient(
                    listOf(
                        base.copy(alpha = alpha),
                        base.copy(alpha = alpha * 0.6f),
                        base.copy(alpha = alpha),
                    )
                )
            )
    )
}

/** A skeleton line of the given height — convenience for text placeholders. */
@Composable
fun SkeletonLine(
    modifier: Modifier = Modifier,
    height: Dp = 14.dp,
) {
    SkeletonBox(modifier = modifier.height(height))
}

/** A circular skeleton — for avatar placeholders. */
@Composable
fun SkeletonCircle(
    modifier: Modifier = Modifier,
    size: Dp = 40.dp,
) {
    val transition = rememberInfiniteTransition(label = "skeleton-circle")
    val alpha by transition.animateFloat(
        initialValue = 0.3f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "skeleton-circle-alpha",
    )
    Box(
        modifier = modifier
            .size(size)
            .clip(androidx.compose.foundation.shape.CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = alpha))
    )
}