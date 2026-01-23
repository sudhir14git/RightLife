package com.jetsynthesys.rightlife.ui.challenge.pojo

data class ChallengeStreakResponse(
    val success: Boolean,
    val statusCode: Int,
    val data: ChallengeStreakData
)

data class ChallengeStreakData(
    val streak: Int,
    val journey: List<ChallengeStreakJourney>
)

data class ChallengeStreakJourney(
    val label: String,
    val date: String,
    val status: String // "COMPLETED" | "MISSED"
)
