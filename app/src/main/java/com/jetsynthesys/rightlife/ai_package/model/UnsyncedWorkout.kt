package com.jetsynthesys.rightlife.ai_package.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class UnsyncedWorkout(
    @SerializedName("creation_datetime")
    val creationDatetime: String? = null,
    @SerializedName("start_datetime")
    val startDatetime: String,
    @SerializedName("end_datetime")
    val endDatetime: String,
    @SerializedName("source_version")
    val sourceVersion: String? = null,
    @SerializedName("source")
    val source: String,
    @SerializedName("source_name")
    val sourceName: String,
    @SerializedName("record_type")
    val recordType: String,
    @SerializedName("workout_type")
    val workoutType: String,
    @SerializedName("duration")
    val duration: String,
    @SerializedName("weight_kg")
    val weightKg: Float,
    @SerializedName("age")
    val age: Int,
    @SerializedName("intensity")
    val intensity: String,
    @SerializedName("gender")
    val gender: String,
    @SerializedName("activity_factor")
    val activityFactor: Float,
    @SerializedName("calculated_activity")
    val calculatedActivity: Float?,
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
    @SerializedName("workout_id")
    val workoutId: String?,
    @SerializedName("heart_rate_data")
    private val rawHeartRateData: List<Map<String, String>>,
    @SerializedName("_id")
    val id: String,
    @SerializedName("icon")
    val icon: String,
    @SerializedName("user_id")
    val userId: String,
    @SerializedName("heart_rate_zones")
    val heartRateZones: HeartRateZones,
    @SerializedName("heart_rate_zone_minutes")
    val heartRateZoneMinutes: HeartRateZoneMinutes,
    @SerializedName("heart_rate_zone_percentages")
    val heartRateZonePercentages: HeartRateZonePercentages
) : Serializable {
    val heartRateData: List<HeartRateDataWorkout>
        get() = rawHeartRateData.mapNotNull { entry ->
            try {
                HeartRateDataWorkout(
                    heartRate = entry["heart_rate"]?.toDoubleOrNull()?.toInt() ?: return@mapNotNull null,
                    date = entry["timestamp"] ?: return@mapNotNull null,
                    unit = entry["unit"] ?: return@mapNotNull null,
                    trendData = ArrayList()
                )
            } catch (e: Exception) {
                null
            }
        }.filter { it.date.isNotEmpty() && it.unit.isNotEmpty() }
}