package com.example.rlapp.ai_package.model

import com.example.rlapp.newdashboard.HeartRateData
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SyncedWorkout(
    @SerializedName("creation_datetime")
    val creationDatetime: String,

    @SerializedName("start_datetime")
    val startDatetime: String,

    @SerializedName("end_datetime")
    val endDatetime: String,

    @SerializedName("source_version")
    val sourceVersion: String,

    @SerializedName("record_type")
    val recordType: String,

    @SerializedName("workout_type")
    val workoutType: String,

    @SerializedName("duration")
    val duration: String,

    @SerializedName("duration_unit")
    val durationUnit: String,

    @SerializedName("calories_burned")
    val caloriesBurned: String,

    @SerializedName("calories_unit")
    val caloriesUnit: String,

    @SerializedName("distance")
    val distance: String,

    @SerializedName("distance_unit")
    val distanceUnit: String,

    @SerializedName("heart_rate_data")
    private val rawHeartRateData: List<Any>, // Accept Any to handle [""]

    @SerializedName("_id")
    val id: String,

    @SerializedName("user_id")
    val userId: String,

    @SerializedName("heart_rate_zones")
    val heartRateZones: HeartRateZones,

    @SerializedName("heart_rate_zone_minutes")
    val heartRateZoneMinutes: HeartRateZoneMinutes,

    @SerializedName("heart_rate_zone_percentages")
    val heartRateZonePercentages: HeartRateZonePercentages
) : Serializable {
    // Filter out invalid heart rate data entries and map valid ones to HeartRateData
    val heartRateData: List<HeartRateDataWorkout>
        get() = rawHeartRateData
            .filterIsInstance<Map<String, String>>() // Keep only valid objects
            .mapNotNull { entry ->
                try {
                    HeartRateDataWorkout(
                        heartRate = entry["heart_rate"]?.toIntOrNull() ?: return@mapNotNull null,
                        date = entry["timestamp"] ?: "",
                        unit = entry["unit"] ?: "",
                        trendData = ArrayList() // Default empty; populate later if needed
                    )
                } catch (e: Exception) {
                    null // Skip entries that can't be parsed
                }
            }
            .filter { it.date.isNotEmpty() && it.unit.isNotEmpty() } // Ensure required fields are non-empty
}