package com.adel.wc26.feature.matches.domain.model

import java.time.Instant

/**
 * Match status. The backend sends a raw string; the mapper parses it into
 * this enum so the UI works with a closed, exhaustive type.
 *
 *
 */
enum class MatchStatus(val value: String) {
    SCHEDULED("scheduled"),
    LIVE("live"),
    FINISHED("finished");
    companion object {
        fun fromString(value: String): MatchStatus =
            entries.find { it.value.equals(value, ignoreCase = true) }
                ?: throw IllegalArgumentException("Unknown match status: $value")
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
    val homeTeamCode: String?,
    val awayTeamCode: String?,
) {
    /** True when both scores are present — i.e. there's a result to show. */
    val hasScore: Boolean
        get() = homeScore != null && awayScore != null
}