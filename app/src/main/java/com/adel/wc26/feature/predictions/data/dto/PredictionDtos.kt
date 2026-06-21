package com.adel.wc26.feature.predictions.data.dto

import com.adel.wc26.core.network.dto.PageDto
import com.adel.wc26.feature.profile.data.dto.UserPublicDto
import kotlinx.serialization.Serializable

@Serializable
data class PredictionDto(
    val matchId: Long,
    val homeScore: Int,
    val awayScore: Int,
    val pointsAwarded: Int? = null,
    val updatedAt: String,
)

@Serializable
data class UpsertPredictionRequest(
    val homeScore: Int,
    val awayScore: Int,
)

@Serializable
data class LeaderboardEntryDto(
    val rank: Long,
    val user: UserPublicDto,
    val totalPoints: Int,
    val exactCount: Int,
)

@Serializable
data class MyRankDto(
    val rank: Long,
    val totalPoints: Int,
    val exactCount: Int,
)

@Serializable
data class LeaderboardResponseDto(
    val entries: PageDto<LeaderboardEntryDto>,
    val me: MyRankDto? = null,
)

@Serializable
data class PredictionStatsDto(
    val userId: Long,
    val rank: Long? = null,
    val totalPoints: Int,
    val exactCount: Int,
    val gradedCount: Int,
    val predictionsCount: Int,
)
