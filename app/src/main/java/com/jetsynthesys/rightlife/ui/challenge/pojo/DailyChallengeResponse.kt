package com.jetsynthesys.rightlife.ui.challenge.pojo

data class DailyChallengeResponse(
    val success: Boolean,
    val statusCode: Int,
    val data: DailyChallengeData
)

data class DailyChallengeData(
    val date: String,
    val calendar: List<DailyChallengeCalendar>
)

data class DailyChallengeCalendar(
    val date: String,
    val day: String,
    val isCompleted: Boolean
)
