package com.adel.wc26.feature.predictions.data

import com.adel.wc26.feature.predictions.data.dto.LeaderboardEntryDto
import com.adel.wc26.feature.predictions.data.dto.LeaderboardResponseDto
import com.adel.wc26.feature.predictions.data.dto.MyRankDto
import com.adel.wc26.feature.predictions.data.dto.PredictionDto
import com.adel.wc26.feature.predictions.data.dto.PredictionStatsDto
import com.adel.wc26.feature.predictions.domain.model.LeaderboardEntry
import com.adel.wc26.feature.predictions.domain.model.LeaderboardPage
import com.adel.wc26.feature.predictions.domain.model.MyRank
import com.adel.wc26.feature.predictions.domain.model.Prediction
import com.adel.wc26.feature.predictions.domain.model.PredictionStats
import com.adel.wc26.feature.profile.data.toDomain

/** Mappers: wire DTOs -> domain models. */

fun PredictionDto.toDomain(): Prediction = Prediction(
    matchId = matchId,
    homeScore = homeScore,
    awayScore = awayScore,
    pointsAwarded = pointsAwarded,
)

fun LeaderboardEntryDto.toDomain(): LeaderboardEntry = LeaderboardEntry(
    rank = rank,
    user = user.toDomain(),
    totalPoints = totalPoints,
    exactCount = exactCount,
)

fun MyRankDto.toDomain(): MyRank = MyRank(
    rank = rank,
    totalPoints = totalPoints,
    exactCount = exactCount,
)

fun LeaderboardResponseDto.toDomain(): LeaderboardPage = LeaderboardPage(
    entries = entries.items.map { it.toDomain() },
    total = entries.total,
    me = me?.toDomain(),
)

fun PredictionStatsDto.toDomain(): PredictionStats = PredictionStats(
    userId = userId,
    rank = rank,
    totalPoints = totalPoints,
    exactCount = exactCount,
    gradedCount = gradedCount,
    predictionsCount = predictionsCount,
)
