package com.example.rlapp.ai_package.model

import com.google.gson.annotations.SerializedName

data class WorkoutResponse(
    @SerializedName("message")
    val message: String,

    @SerializedName("synced_workouts")
    val syncedWorkouts: List<SyncedWorkout>,

    @SerializedName("unsynced_workouts")
    val unsyncedWorkouts: List<UnsyncedWorkout>
)
