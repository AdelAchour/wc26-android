package com.adel.wc26.feature.auth.ui.splash

import android.content.res.Configuration
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.adel.wc26.R
import com.adel.wc26.core.designsystem.theme.WC26Theme

@Composable
fun SplashScreen(
    modifier: Modifier = Modifier
) {
    // Check if the app is currently in Dark Theme
    val isDark = WC26Theme.isDark

    // Choose the logo dynamically based on the theme
    val logoResId = if (isDark) {
        R.drawable.wc26_logo_white
    } else {
        R.drawable.wc26_logo
    }

    // Dynamic theme-derived gradient colors
    val startColor = MaterialTheme.colorScheme.surface
    val endColor = MaterialTheme.colorScheme.surfaceVariant

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(startColor, endColor)
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Branded Logo
            Image(
                painter = painterResource(id = logoResId),
                contentDescription = "WC26 Logo",
                modifier = Modifier
                    .width(180.dp)
                    .aspectRatio(1f)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Animated rolling soccer ball loader
            RollingBallLoader(
                ballSize = 56.dp,
                trailLength = 120.dp
            )
        }
    }
}

@Composable
fun RollingBallLoader(
    modifier: Modifier = Modifier,
    ballSize: Dp = 56.dp,
    trailLength: Dp = 120.dp
) {
    val transition = rememberInfiniteTransition(label = "rolling_ball")

    // 1. Clockwise Rotation (0 to 360 degrees)
    val rotation by transition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 700, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "ball_rotation"
    )

    // 2. Dash phase offset to shift the speed lines from right to left
    val dashLength = 30f
    val dashSpacing = 20f
    val totalDashPeriod = dashLength + dashSpacing
    val phase by transition.animateFloat(
        initialValue = 0f,
        targetValue = totalDashPeriod,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 500, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "trail_phase"
    )

    // 3. Subtle bounce to simulate uneven ground
    val bounceY by transition.animateFloat(
        initialValue = 0f,
        targetValue = 3f, // movement range in dp
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 180, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ball_bounce"
    )

    val density = LocalDensity.current

    // Theme-derived colors
    val accentColor = MaterialTheme.colorScheme.secondary
    val groundColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f)

    Box(
        modifier = modifier
            .width(trailLength + ballSize)
            .height(ballSize + 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val ballSizePx = with(density) { ballSize.toPx() }
        val bounceYPx = with(density) { bounceY.dp.toPx() }

        // Canvas for the speed lines and ground floor line
        Canvas(
            modifier = Modifier.fillMaxSize()
        ) {
            val width = size.width
            val height = size.height

            // Center of the ball aligns at the right side of the track
            val ballCenterX = width - ballSizePx / 2
            val ballCenterY = height / 2

            // Native path effect utilizing the animated phase
            val pathEffect = PathEffect.dashPathEffect(
                intervals = floatArrayOf(dashLength, dashSpacing),
                phase = phase
            )

            // Speed Line 1 (Mid height, trailing right behind the ball's center-left)
            drawLine(
                color = accentColor.copy(alpha = 0.4f),
                start = Offset(x = ballCenterX - ballSizePx * 1.5f, y = ballCenterY - ballSizePx * 0.15f),
                end = Offset(x = ballCenterX - ballSizePx * 0.55f, y = ballCenterY - ballSizePx * 0.15f),
                strokeWidth = 4f,
                pathEffect = pathEffect,
                cap = StrokeCap.Round
            )

            // Speed Line 2 (Upper height, shorter high wind trail)
            drawLine(
                color = accentColor.copy(alpha = 0.25f),
                start = Offset(x = ballCenterX - ballSizePx * 1.3f, y = ballCenterY - ballSizePx * 0.4f),
                end = Offset(x = ballCenterX - ballSizePx * 0.7f, y = ballCenterY - ballSizePx * 0.4f),
                strokeWidth = 3f,
                pathEffect = pathEffect,
                cap = StrokeCap.Round
            )

            // Ground Floor Line (Ground contact surface moving backward under the ball)
            val groundY = ballCenterY + ballSizePx / 2
            drawLine(
                color = groundColor,
                start = Offset(x = 0f, y = groundY),
                end = Offset(x = ballCenterX + ballSizePx * 0.4f, y = groundY),
                strokeWidth = 4f,
                pathEffect = pathEffect,
                cap = StrokeCap.Round
            )
        }

        // Soccer Ball Image (scaled and rotated/bounced programmatically)
        Image(
            painter = painterResource(id = R.drawable.soccer_ball),
            contentDescription = "Rolling Ball",
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .size(ballSize)
                .graphicsLayer {
                    rotationZ = rotation
                    translationY = bounceYPx
                }
        )
    }
}

@Preview(showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SplashScreenPreview() {
    WC26Theme {
        SplashScreen(modifier = Modifier.fillMaxSize())
    }
}