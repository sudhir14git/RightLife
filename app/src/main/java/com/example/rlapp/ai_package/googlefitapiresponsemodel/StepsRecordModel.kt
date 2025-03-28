package com.example.rlapp.ai_package.googlefitapiresponsemodel

data class StepsRecordModel(
    val startTime: String,
    val startZoneOffset: String?, // Nullable as it can be null
    val endTime: String,
    val endZoneOffset: String?, // Nullable as it can be null
    val count: Int, // Steps count as Int
    val metadata: MetadataModel // Nested Metadata object
)
