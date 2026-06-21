package com.adel.wc26.feature.predictions.data

import com.adel.wc26.feature.predictions.data.dto.LeaderboardResponseDto
import com.adel.wc26.feature.predictions.data.dto.PredictionDto
import com.adel.wc26.feature.predictions.data.dto.PredictionStatsDto
import com.adel.wc26.feature.predictions.data.dto.UpsertPredictionRequest
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Prediction endpoints (served under the /api/ base path).
 *
 *   GET  me/predictions
 *   PUT  predictions/{matchId}
 *   GET  leaderboard?limit=&offset=
 *   GET  users/{id}/prediction-stats
 */
interface PredictionApi {

    @GET("me/predictions")
    suspend fun getMyPredictions(): List<PredictionDto>

    @PUT("predictions/{matchId}")
    suspend fun upsertPrediction(
        @Path("matchId") matchId: Long,
        @Body body: UpsertPredictionRequest,
    ): PredictionDto

    @GET("leaderboard")
    suspend fun getLeaderboard(
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Long = 0,
    ): LeaderboardResponseDto

    @GET("users/{id}/prediction-stats")
    suspend fun getUserStats(@Path("id") userId: Long): PredictionStatsDto
}
