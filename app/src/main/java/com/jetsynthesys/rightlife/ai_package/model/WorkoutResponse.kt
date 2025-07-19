package com.jetsynthesys.rightlife.ai_package.model

import com.google.gson.annotations.SerializedName

data class WorkoutResponse(
    @SerializedName("message")
    val message: String,
    @SerializedName("range_type")
    val rangeType: String? = null,
    @SerializedName("start_date")
    val startDate: String? = null,
    @SerializedName("end_date")
    val endDate: String? = null,
    @SerializedName("synced_workouts")
    val syncedWorkouts: List<SyncedWorkout>,
    @SerializedName("unsynced_workouts")
    val unsyncedWorkouts: List<UnsyncedWorkout>,
    @SerializedName("total_synced")
    val totalSynced: Int,
    @SerializedName("total_unsynced")
    val totalUnsynced: Int,
    @SerializedName("page")
    val page: Int,
    @SerializedName("limit")
    val limit: Int
)
