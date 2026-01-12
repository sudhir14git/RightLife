package com.jetsynthesys.rightlife.ui.challenge.pojo


data class LeaderboardUser(
    val totalScore: Int,
    val userId: String,
    val name: String,
    val avatar: String?,
    val rank: Int
)