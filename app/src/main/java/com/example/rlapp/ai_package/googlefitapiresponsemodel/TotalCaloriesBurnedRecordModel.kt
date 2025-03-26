package com.example.rlapp.ai_package.googlefitapiresponsemodel

data class TotalCaloriesBurnedRecordModel(
    val startTime: String,
    val startZoneOffset: String?, // Nullable as it can be null
    val endTime: String,
    val endZoneOffset: String?, // Nullable as it can be null
    val energy: Double, // kcal value as Double
    val metadata: MetadataModel // Nested Metadata object
)
