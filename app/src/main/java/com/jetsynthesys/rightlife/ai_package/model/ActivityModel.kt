package com.jetsynthesys.rightlife.ai_package.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class ActivityModel(
    val activityType: String?,
    val activity_id: String?,
    val duration: String?,
    val caloriesBurned: String?,
    val icon: String?,
    val intensity: String?,
    val calorieId: String?,
    val userId :String?
) : Parcelable