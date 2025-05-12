package com.jetsynthesys.rightlife.ai_package.model

import java.time.LocalDate

data class WorkoutWeeklyDayModel(
    val dayName: String, // e.g., "M", "T"
    val dayNumber: String, // e.g., "6"
    val fullDate: LocalDate,
    val status : Boolean = false,
    var is_available: Boolean = false
)
