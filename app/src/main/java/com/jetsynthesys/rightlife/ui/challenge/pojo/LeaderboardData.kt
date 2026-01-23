package com.jetsynthesys.rightlife.ui.challenge.pojo


data class LeaderboardData(
    val type: String,
    val yourRank: LeaderboardUser?,
    val leaderboard: List<LeaderboardUser>
)