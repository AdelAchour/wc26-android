package com.adel.wc26.feature.matches.data.dto

import kotlinx.serialization.Serializable

/**
 * Match wire format — mirrors the backend's MatchDto exactly.
 *
 * Note the deliberate fidelity to the wire: [gameNumber], [homeScore],
 * [awayScore] are Short because that is what the backend sends. The domain
 * model widens these to Int and parses [kickoffAt]/[status] into proper
 * types; the DTO stays a faithful mirror of the JSON.
 */
@Serializable
data class MatchDto(
    val id: Long,
    val gameNumber: Short,
    val homeTeam: String,
    val awayTeam: String,
    val stage: String,
    val venue: String,
    val countryCode: String,
    val kickoffAt: String,
    val status: String,
    val homeScore: Short? = null,
    val awayScore: Short? = null,
)