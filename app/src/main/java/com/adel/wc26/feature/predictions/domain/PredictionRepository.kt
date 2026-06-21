package com.adel.wc26.feature.predictions.domain

import com.adel.wc26.core.result.DataResult
import com.adel.wc26.feature.matches.domain.model.Match
import com.adel.wc26.feature.predictions.domain.model.LeaderboardPage
import com.adel.wc26.feature.predictions.domain.model.Prediction
import com.adel.wc26.feature.predictions.domain.model.PredictionStats

interface PredictionRepository {

    /** All of the signed-in user's predictions, for hydrating the match list / detail. */
    suspend fun getMyPredictions(): DataResult<List<Prediction>>

    /**
     * Create or update the signed-in user's prediction for a match.
     * Fails with [com.adel.wc26.core.result.AppError.Forbidden] if locked (past kickoff)
     * and [com.adel.wc26.core.result.AppError.Conflict] if the teams aren't confirmed yet.
     */
    suspend fun upsertPrediction(matchId: Long, homeScore: Int, awayScore: Int): DataResult<Prediction>

    /** A page of the leaderboard, plus the viewer's own rank. */
    suspend fun getLeaderboard(limit: Int = 20, offset: Long = 0): DataResult<LeaderboardPage>

    /** A user's prediction stats for their profile. */
    suspend fun getUserStats(userId: Long): DataResult<PredictionStats>

    /** A user's prediction history — finished matches (each carrying their prediction). */
    suspend fun getUserPredictionHistory(userId: Long): DataResult<List<Match>>
}
