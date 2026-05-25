package com.adel.wc26.core.designsystem.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.adel.wc26.R

/**
 * Typography for WC26 — Geist across the board, echoing the website's
 * body font. The serif display face (Fraunces) is intentionally NOT used
 * here; the app stays on one clean sans for a tighter mobile feel.
 *
 * FONT FILES REQUIRED — place these in app/src/main/res/font/ :
 *   geist_regular.ttf      (weight 400)
 *   geist_medium.ttf       (weight 500)
 *   geist_semibold.ttf     (weight 600)
 *   geist_bold.ttf         (weight 700)
 *
 * Downloaded Geist from https://vercel.com/font (OFL licensed). Used the
 * STATIC weights, not the variable font — Android's static-weight handling
 * is the least fuss. Filenames must be lowercase with underscores (res/
 * naming rules). If a weight is missing, Compose falls back to the
 * closest available.
 */

val Geist = FontFamily(
    Font(R.font.geist_regular, FontWeight.Normal),
    Font(R.font.geist_medium, FontWeight.Medium),
    Font(R.font.geist_semibold, FontWeight.SemiBold),
    Font(R.font.geist_bold, FontWeight.Bold),
)

/**
 * The M3 type scale, all set in Geist. Sizes tuned for a content-dense
 * social app — slightly tighter than M3 defaults, with deliberate weight
 * contrast between display/headline (bold) and body (regular).
 */
val WC26Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Bold,
        fontSize = 40.sp, lineHeight = 46.sp, letterSpacing = (-0.5).sp,
    ),
    displayMedium = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, lineHeight = 38.sp, letterSpacing = (-0.5).sp,
    ),
    displaySmall = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.SemiBold,
        fontSize = 26.sp, lineHeight = 32.sp, letterSpacing = (-0.25).sp,
    ),
    headlineLarge = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.SemiBold,
        fontSize = 24.sp, lineHeight = 30.sp, letterSpacing = (-0.25).sp,
    ),
    headlineMedium = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.SemiBold,
        fontSize = 20.sp, lineHeight = 26.sp,
    ),
    headlineSmall = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, lineHeight = 24.sp,
    ),
    titleLarge = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.SemiBold,
        fontSize = 17.sp, lineHeight = 23.sp,
    ),
    titleMedium = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Medium,
        fontSize = 15.sp, lineHeight = 21.sp, letterSpacing = 0.1.sp,
    ),
    titleSmall = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Medium,
        fontSize = 13.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp,
    ),
    bodyLarge = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Normal,
        fontSize = 16.sp, lineHeight = 24.sp, letterSpacing = 0.15.sp,
    ),
    bodyMedium = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Normal,
        fontSize = 14.sp, lineHeight = 20.sp, letterSpacing = 0.15.sp,
    ),
    bodySmall = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Normal,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.2.sp,
    ),
    labelLarge = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Medium,
        fontSize = 14.sp, lineHeight = 18.sp, letterSpacing = 0.1.sp,
    ),
    labelMedium = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Medium,
        fontSize = 12.sp, lineHeight = 16.sp, letterSpacing = 0.5.sp,
    ),
    labelSmall = TextStyle(
        fontFamily = Geist, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, lineHeight = 15.sp, letterSpacing = 0.5.sp,
    ),
)