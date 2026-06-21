package com.adel.wc26.feature.predictions.data

import com.adel.wc26.core.network.apiCall
import com.adel.wc26.core.result.DataResult
import com.adel.wc26.core.result.map
import com.adel.wc26.feature.predictions.data.dto.UpsertPredictionRequest
import com.adel.wc26.feature.predictions.domain.PredictionRepository
import com.adel.wc26.feature.predictions.domain.model.LeaderboardPage
import com.adel.wc26.feature.predictions.domain.model.Prediction
import com.adel.wc26.feature.predictions.domain.model.PredictionStats
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PredictionRepositoryImpl @Inject constructor(
    private val api: PredictionApi,
) : PredictionRepository {

    override suspend fun getMyPredictions(): DataResult<List<Prediction>> =
        apiCall { api.getMyPredictions() }.map { list -> list.map { it.toDomain() } }

    override suspend fun upsertPrediction(
        matchId: Long,
        homeScore: Int,
        awayScore: Int,
    ): DataResult<Prediction> =
        apiCall {
            api.upsertPrediction(matchId, UpsertPredictionRequest(homeScore, awayScore))
        }.map { it.toDomain() }

    override suspend fun getLeaderboard(limit: Int, offset: Long): DataResult<LeaderboardPage> =
        apiCall { api.getLeaderboard(limit, offset) }.map { it.toDomain() }

    override suspend fun getUserStats(userId: Long): DataResult<PredictionStats> =
        apiCall { api.getUserStats(userId) }.map { it.toDomain() }
}
