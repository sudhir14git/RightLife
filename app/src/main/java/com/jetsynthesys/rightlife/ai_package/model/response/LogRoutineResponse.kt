package com.jetsynthesys.rightlife.ai_package.model.response

import com.google.gson.annotations.SerializedName

data class LogRoutineResponse(
    @SerializedName("status_code") val statusCode: Int,
    @SerializedName("message") val message: String,
    @SerializedName("workouts") val workouts: List<Workout>
)

data class Workout(
    @SerializedName("user_id") val userId: String,
    @SerializedName("date") val date: String,
    @SerializedName("activityId") val activityId: String,
    @SerializedName("intensity") val intensity: String,
    @SerializedName("duration_min") val durationMin: Double,
    @SerializedName("calories_burned") val caloriesBurned: Double,
    @SerializedName("routine_id") val routineId: String
)