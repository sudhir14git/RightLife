package com.jetsynthesys.rightlife.ai_package.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class ActivityModel(
    val userId :String?,
    val id: String,
    val source: String,
    val recordType: String,
    val workoutType: String,
    val workoutId: String?,
    val duration: String?,
    val averageHeartRate : Double,
    val caloriesBurned: String?,
    val caloriesUnit: String,
    val icon: String?,
    val intensity: String?,
    val isSynced : Boolean
) : Parcelable