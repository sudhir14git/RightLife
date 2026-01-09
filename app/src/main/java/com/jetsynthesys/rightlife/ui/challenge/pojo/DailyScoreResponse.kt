package com.jetsynthesys.rightlife.ui.challenge.pojo

data class DailyScoreResponse(
    val success: Boolean,
    val statusCode: Int,
    val data: DailyScoreData
)

data class DailyScoreData(
    val date: String,
    val dailyScore: Int,
    val bonusScore: Int,
    val totalScore: Int,
    val performance: String,
    val message: String,
    val rank: Int,
    val daily: ScoreProgress,
    val bonus: ScoreProgress
)

data class ScoreProgress(
    val completed: Int,
    val total: Int
)
