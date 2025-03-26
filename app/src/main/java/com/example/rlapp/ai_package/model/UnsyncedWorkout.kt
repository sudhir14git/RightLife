package com.example.rlapp.ai_package.model

import com.google.gson.annotations.SerializedName

data class UnsyncedWorkout(
    @SerializedName("_id")
    val id: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("record_type")
    val recordType: String,

    @SerializedName("workout_type")
    val workoutType: String,

    @SerializedName("duration")
    val duration: Int,

    @SerializedName("weight_kg")
    val weightKg: Int,

    @SerializedName("age")
    val age: Int,

    @SerializedName("gender")
    val gender: String,

    @SerializedName("activity_factor")
    val activityFactor: Float,

    @SerializedName("calories_burned")
    val caloriesBurned: Int,

    @SerializedName("timestamp")
    val timestamp: String,

    @SerializedName("created_at")
    val createdAt: String
)
