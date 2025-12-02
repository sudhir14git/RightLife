package com.jetsynthesys.rightlife.ai_package.model

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize


@Parcelize
data class CardItem(
    val title: String, // e.g., "Running"
    val duration: String, // e.g., "1 hr 03 mins"
    val caloriesBurned: String, // e.g., "514 cal"
    val avgHeartRate: String, // e.g., "120 bpm"
    val icon: String, // e.g., "120 bpm"
    val heartRateData: List<HeartRateDataWorkout>, // Heart rate data for the graph
    val heartRateZones: HeartRateZones, // e.g., {"Light Zone": [112, 124], ...}
    val heartRateZoneMinutes: HeartRateZoneMinutes, // e.g., {"Below Light": 338, ...}
    val heartRateZonePercentages: HeartRateZonePercentages ,// e.g., {"Below Light": 54.96, ...}
    val isSynced: Boolean
) : Parcelable