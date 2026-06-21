package com.adel.wc26.feature.predictions.domain.model

import com.adel.wc26.feature.profile.domain.PublicProfile

/** One ranked row of the leaderboard. */
data class LeaderboardEntry(
    val rank: Long,
    val user: PublicProfile,
    val totalPoints: Int,
    val exactCount: Int,
)

/** The viewer's own position, for the sticky "you" row. */
data class MyRank(
    val rank: Long,
    val totalPoints: Int,
    val exactCount: Int,
)

/** A page of the leaderboard plus the viewer's rank (null if not logged in / unranked). */
data class LeaderboardPage(
    val entries: List<LeaderboardEntry>,
    val total: Long,
    val me: MyRank?,
)
