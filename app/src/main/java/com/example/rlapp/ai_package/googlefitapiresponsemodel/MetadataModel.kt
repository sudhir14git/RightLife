package com.example.rlapp.ai_package.googlefitapiresponsemodel

data class MetadataModel(
    val id: String,
    val dataOrigin: DataOrigin,
    val lastModifiedTime: String,
    val clientRecordId: String?,
    val clientRecordVersion: Int,
    val device: String?,
    val recordingMethod: Int
)

data class DataOrigin(
    val packageName: String
)
