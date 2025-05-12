package com.jetsynthesys.rightlife.ai_package.model.request

import com.google.gson.annotations.SerializedName

data class LogRoutineRequest(
    @SerializedName("user_id") val userId: String,
    @SerializedName("date") val date: String,
    @SerializedName("routine_id") val routineId: String
)