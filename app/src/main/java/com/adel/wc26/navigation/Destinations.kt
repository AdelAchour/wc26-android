package com.adel.wc26.navigation

import kotlinx.serialization.Serializable

/**
 * Type-safe navigation destinations.
 *
 * Each destination is a @Serializable type. Object = no arguments;
 * data class = carries typed arguments. Navigation Compose (2.8+)
 * serializes these into the back stack, so navigation is compile-checked
 * and arguments are real typed properties — no string parsing.
 *
 * Destinations are grouped: top-level tabs, auth flow, detail screens.
 */
object Destinations {

    // --- Launch ---
    /** Transient routing decision on launch — token check → tabs or auth. */
    @Serializable
    data object Splash

    // --- Auth flow ---
    @Serializable
    data object Welcome

    @Serializable
    data object Login

    @Serializable
    data object Register

    // --- Top-level tabs (bottom nav) ---
    @Serializable
    data object Matches

    @Serializable
    data object Feed

    @Serializable
    data object Profile

    @Serializable
    data object Settings

    // --- Detail screens (bottom nav hidden) ---
    @Serializable
    data class MatchDetail(val matchId: Long)

    @Serializable
    data class PostDetail(val postId: Long)

    @Serializable
    data class UserProfile(val userId: Long)

    @Serializable
    data class Likers(val postId: Long)
}