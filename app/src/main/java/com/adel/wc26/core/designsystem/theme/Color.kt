package com.adel.wc26.core.designsystem.theme

import androidx.compose.ui.graphics.Color

/**
 * WC26 brand palette — the raw color tokens, ported from the web design
 * system (adelash.dev / wc26.adelash.dev).
 *
 * These are the source-of-truth colors. They get MAPPED onto Material 3's
 * ColorScheme roles in Theme.kt — this file is just the palette, not the
 * semantic assignment.
 *
 * Web reference:
 *   --bg        #FAF7F2   warm off-white
 *   --ink       #0F1A2E   deep navy
 *   --accent    #E63946   signal red
 */
object BrandColors {

    // --- Core brand ---
    val WarmWhite = Color(0xFFFAF7F2)   // primary light background
    val WarmElevated = Color(0xFFF2EDE4) // elevated surface on light
    val Navy = Color(0xFF0F1A2E)        // primary ink / dark background
    val NavySoft = Color(0xFF3A4658)    // secondary text on light
    val NavyFaint = Color(0xFF6B7585)   // tertiary / muted text
    val Rule = Color(0xFFD7CFC3)        // hairline borders on light

    val AccentRed = Color(0xFFE63946)   // the signal accent
    val AccentDeep = Color(0xFFB82D38)  // pressed / deep accent
    val Good = Color(0xFF2A9D5F)        // success / "live" green

    // --- Dark theme surfaces ---
    // The dark theme keeps the navy DNA: near-black navy backgrounds,
    // warm-white text, the same red accent (slightly lifted for contrast).
    val DarkBg = Color(0xFF0B1320)      // primary dark background
    val DarkElevated = Color(0xFF141F33) // elevated surface on dark
    val DarkRule = Color(0xFF2A3650)    // hairline borders on dark
    val WarmWhiteSoft = Color(0xFFC9C3B8) // secondary text on dark
    val WarmWhiteFaint = Color(0xFF8B93A0) // tertiary text on dark
    val AccentRedLifted = Color(0xFFFF5C68) // accent on dark (more luminous)

    // --- Fixed ---
    val Black = Color(0xFF000000)
    val White = Color(0xFFFFFFFF)
}