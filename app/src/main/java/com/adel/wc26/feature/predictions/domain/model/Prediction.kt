package com.adel.wc26.feature.predictions.domain.model

/**
 * A user's score prediction for a match.
 *
 * [pointsAwarded] is null until the match finishes and the prediction is
 * graded; afterwards it holds the points earned (5 exact, 3 result, 0 wrong).
 */
data class Prediction(
    val matchId: Long,
    val homeScore: Int,
    val awayScore: Int,
    val pointsAwarded: Int?,
)
