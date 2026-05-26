package com.adel.wc26.feature.matches.domain.model

import java.time.Instant

/**
 * Match status. The backend sends a raw string; the mapper parses it into
 * this enum so the UI works with a closed, exhaustive type.
 *
 * [UNKNOWN] is a safety net — if the backend ever adds a status the app
 * doesn't know, it degrades gracefully rather than crashing.
 */
enum class MatchStatus {
    UPCOMING,
    LIVE,
    FINISHED,
    UNKNOWN;

    companion object {
        /** Parse the backend's status string. Case-insensitive, null-safe. */
        fun fromApi(value: String): MatchStatus = when (value.lowercase()) {
            "upcoming", "scheduled" -> UPCOMING
            "live", "in_progress" -> LIVE
            "finished", "completed", "final" -> FINISHED
            else -> UNKNOWN
        }
    }
}

/**
 * A World Cup match — the domain model.
 *
 * Unlike MatchDto (the wire format), this holds parsed, app-friendly types:
 * a real [Instant] for kickoff, a [MatchStatus] enum, and Int scores.
 * The DTO->domain mapper does that conversion once, in the data layer.
 */
data class Match(
    val id: Long,
    val gameNumber: Int,
    val homeTeam: String,
    val awayTeam: String,
    val stage: String,
    val venue: String,
    val countryCode: String,
    val kickoffAt: Instant,
    val status: MatchStatus,
    val homeScore: Int?,
    val awayScore: Int?,
) {
    /** True when both scores are present — i.e. there's a result to show. */
    val hasScore: Boolean
        get() = homeScore != null && awayScore != null
}