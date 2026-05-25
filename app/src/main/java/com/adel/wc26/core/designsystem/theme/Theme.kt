package com.adel.wc26.core.designsystem.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

/**
 * Brand colors that have no natural home in Material 3's ColorScheme.
 * M3 has no "success" / "live" role, so we carry it in a small companion
 * object exposed via a CompositionLocal — access with WC26Theme.extended.
 */
data class WC26ExtendedColors(
    val live: Color,        // the "LIVE match" / success green
    val onLive: Color,
    val hairline: Color,    // explicit hairline border color
)

private val LocalExtendedColors = staticCompositionLocalOf {
    WC26ExtendedColors(
        live = BrandColors.Good,
        onLive = BrandColors.White,
        hairline = BrandColors.Rule,
    )
}

/**
 * LIGHT scheme — the warm, editorial daytime identity from the website.
 * - background / surface: warm off-white
 * - primary: deep navy (used for primary actions, prominent text)
 * - secondary: the signal red, used as a true accent (sparingly)
 */
private val LightColors = lightColorScheme(
    primary = BrandColors.Navy,
    onPrimary = BrandColors.WarmWhite,
    primaryContainer = BrandColors.WarmElevated,
    onPrimaryContainer = BrandColors.Navy,

    secondary = BrandColors.AccentRed,
    onSecondary = BrandColors.White,
    secondaryContainer = Color(0xFFFBE3E5),
    onSecondaryContainer = BrandColors.AccentDeep,

    tertiary = BrandColors.NavySoft,
    onTertiary = BrandColors.WarmWhite,

    background = BrandColors.WarmWhite,
    onBackground = BrandColors.Navy,
    surface = BrandColors.WarmWhite,
    onSurface = BrandColors.Navy,
    surfaceVariant = BrandColors.WarmElevated,
    onSurfaceVariant = BrandColors.NavySoft,

    outline = BrandColors.Rule,
    outlineVariant = BrandColors.Rule,

    error = BrandColors.AccentDeep,
    onError = BrandColors.White,

    scrim = BrandColors.Black,
)

/**
 * DARK scheme — keeps the navy DNA: near-black navy ground, warm-white
 * text, the red accent lifted for contrast.
 */
private val DarkColors = darkColorScheme(
    primary = BrandColors.WarmWhite,
    onPrimary = BrandColors.Navy,
    primaryContainer = BrandColors.DarkElevated,
    onPrimaryContainer = BrandColors.WarmWhite,

    secondary = BrandColors.AccentRedLifted,
    onSecondary = BrandColors.Navy,
    secondaryContainer = Color(0xFF4A1F25),
    onSecondaryContainer = BrandColors.AccentRedLifted,

    tertiary = BrandColors.WarmWhiteSoft,
    onTertiary = BrandColors.Navy,

    background = BrandColors.DarkBg,
    onBackground = BrandColors.WarmWhite,
    surface = BrandColors.DarkBg,
    onSurface = BrandColors.WarmWhite,
    surfaceVariant = BrandColors.DarkElevated,
    onSurfaceVariant = BrandColors.WarmWhiteSoft,

    outline = BrandColors.DarkRule,
    outlineVariant = BrandColors.DarkRule,

    error = BrandColors.AccentRedLifted,
    onError = BrandColors.Navy,

    scrim = BrandColors.Black,
)

private val LightExtended = WC26ExtendedColors(
    live = BrandColors.Good,
    onLive = BrandColors.White,
    hairline = BrandColors.Rule,
)

private val DarkExtended = WC26ExtendedColors(
    live = BrandColors.Good,
    onLive = BrandColors.White,
    hairline = BrandColors.DarkRule,
)

/**
 * The WC26 theme. Wrap the whole app in this.
 *
 * Note: we deliberately do NOT use Material You dynamic color — the brand
 * identity is fixed, and a portfolio app should look the same on every
 * device, not adopt the user's wallpaper palette.
 *
 * @param darkTheme follows the system setting by default.
 */
@Composable
fun WC26Theme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit,
) {
    val colorScheme = if (darkTheme) DarkColors else LightColors
    val extended = if (darkTheme) DarkExtended else LightExtended

    // Match the system status-bar icons to the theme brightness.
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view)
                .isAppearanceLightStatusBars = !darkTheme
        }
    }

    androidx.compose.runtime.CompositionLocalProvider(
        LocalExtendedColors provides extended,
    ) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = WC26Typography,
            shapes = WC26Shapes,
            content = content,
        )
    }
}

/**
 * Accessor for WC26's extended (non-M3) tokens.
 * Usage: WC26Theme.extended.live
 */
object WC26Theme {
    val extended: WC26ExtendedColors
        @Composable get() = LocalExtendedColors.current
}