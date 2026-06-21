package com.adel.wc26.feature.predictions.ui

import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.domain.model.MatchStatus
import java.time.Instant

/**
 * Where a match sits in the prediction lifecycle, derived from its status,
 * kickoff time, and whether its teams are confirmed. Drives the strip/chip UI.
 *
 * Mirrors the server's rules: a match is only predictable while upcoming with
 * confirmed teams; it locks at kickoff and is graded once finished.
 */
enum class PredictionPhase {
    /** Upcoming, teams confirmed, before kickoff — editable. */
    OPEN,

    /** Kicked off or live — read-only. */
    LOCKED,

    /** Finished — shows pick vs result + points. */
    FINISHED,

    /** Teams not confirmed yet (knockout TBD) — can't predict. */
    NOT_PREDICTABLE,
}

fun Match.predictionPhase(now: Instant = Instant.now()): PredictionPhase {
    val teamsConfirmed = homeTeamCode != null && awayTeamCode != null
    return when {
        !teamsConfirmed -> PredictionPhase.NOT_PREDICTABLE
        status == MatchStatus.FINISHED -> PredictionPhase.FINISHED
        status == MatchStatus.LIVE -> PredictionPhase.LOCKED
        kickoffAt.isBefore(now) -> PredictionPhase.LOCKED
        else -> PredictionPhase.OPEN
    }
}
