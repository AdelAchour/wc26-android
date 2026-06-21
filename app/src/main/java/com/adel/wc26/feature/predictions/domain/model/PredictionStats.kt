package com.adel.wc26.feature.predictions.domain.model

/** A user's prediction stats, for profile screens. [rank] is null until graded. */
data class PredictionStats(
    val userId: Long,
    val rank: Long?,
    val totalPoints: Int,
    val exactCount: Int,
    val gradedCount: Int,
    val predictionsCount: Int,
)
