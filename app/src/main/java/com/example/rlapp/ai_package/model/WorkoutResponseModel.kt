package com.example.rlapp.ai_package.model

data class WorkoutResponseModel(
    val success: Boolean,
    val statusCode: Int,
    val data: List<WorkoutList>
)

data class WorkoutList(
    val _id: String,
    val title: String,
    val iconUrl: String,
    val moduleId: String,
    val order: Int,
    val isActive: Boolean,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int
)