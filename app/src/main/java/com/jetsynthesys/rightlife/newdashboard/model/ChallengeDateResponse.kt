package com.jetsynthesys.rightlife.newdashboard.model

data class ChallengeDateResponse(
    val success: Boolean,
    val statusCode: Int,
    val data: ChallengeDateData
)

data class ChallengeDateData(
    val challengeStartDate: String,
    val challengeEndDate: String,
    val participateDate: String,
    val challengeLiveDate: String,
    val challengeStatus: Int
)
