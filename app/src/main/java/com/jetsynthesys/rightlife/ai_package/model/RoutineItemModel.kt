package com.jetsynthesys.rightlife.ai_package.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class WorkoutRoutineItem(
    val routineId: String,
    val routineName: String,
                               val activityName: String,
                               val duration: String,
                               val caloriesBurned: String,
                               val intensity: String,
                               val activityId: String,
                               val userId: String,
                               val isSelected: Boolean = false ):Parcelable
