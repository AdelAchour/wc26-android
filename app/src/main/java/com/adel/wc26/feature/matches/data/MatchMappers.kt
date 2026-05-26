package com.adel.wc26.feature.matches.data

import com.adel.wc26.feature.matches.data.dto.MatchDto
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.matches.domain.model.MatchStatus
import java.time.Instant

/**
 * Maps MatchDto (wire format) -> Match (domain model).
 *
 * This is where the wire-format quirks are ironed out:
 *  - kickoffAt String -> parsed Instant
 *  - status String    -> MatchStatus enum
 *  - Short fields     -> Int
 *
 * A malformed kickoff timestamp falls back to Instant.EPOCH rather than
 * throwing — one bad row shouldn't break the whole list.
 */
fun MatchDto.toDomain(): Match = Match(
    id = id,
    gameNumber = gameNumber.toInt(),
    homeTeam = homeTeam,
    awayTeam = awayTeam,
    stage = stage,
    venue = venue,
    countryCode = countryCode,
    kickoffAt = runCatching { Instant.parse(kickoffAt) }
        .getOrDefault(Instant.EPOCH),
    status = MatchStatus.fromApi(status),
    homeScore = homeScore?.toInt(),
    awayScore = awayScore?.toInt(),
)