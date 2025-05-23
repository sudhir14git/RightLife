package com.jetsynthesys.rightlife.ai_package.model.response

import com.google.gson.annotations.SerializedName

data class WaterIntakeResponse(
    @SerializedName("status_code")
    val statusCode: Int,

    @SerializedName("message")
    val message: String,

    @SerializedName("start_date")
    val startDate: String,

    @SerializedName("end_date")
    val endDate: String,

    @SerializedName("water_intake_totals")
    val waterIntakeTotals: List<WaterIntakeTotal>,

    @SerializedName("total_water")
    val totalWater: Double,

    @SerializedName("current_avg_water")
    val currentAvgWater: Double,

    @SerializedName("progress_percentage")
    val progressPercentage: Double,

    @SerializedName("progress_sign")
    val progressSign: String,

    @SerializedName("heading")
    val heading: String,

    @SerializedName("description")
    val description: String,

    @SerializedName("todays_water_log")
    val todaysWaterLog: TodayWaterLog
)

data class WaterIntakeTotal(
    @SerializedName("water_ml")
    val waterMl: Double,

    @SerializedName("date")
    val date: String
)
data class TodayWaterLog(
    @SerializedName("total_water")
    val totalWater: Float,

    @SerializedName("date")
    val date: String,

    @SerializedName("goal")
    val goal: Float
)
