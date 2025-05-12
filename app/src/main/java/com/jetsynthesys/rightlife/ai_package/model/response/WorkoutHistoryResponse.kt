package com.jetsynthesys.rightlife.ai_package.model.response

data class WorkoutHistoryResponse(
    val status_code: Int,
    val data: WorkoutSummaryData
)

data class WorkoutSummaryData(
    val start_date: String,
    val end_date: String,
    val record_details: List<WorkoutRecord>
)

data class WorkoutRecord(
    val date: String,
    val is_available_workout: Boolean,
    val calories_burned: Double
)
