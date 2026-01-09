package com.jetsynthesys.rightlife.ui.challenge.pojo

data class DailyTaskResponse(
    val success: Boolean,
    val statusCode: Int,
    val data: DailyTaskData
)

data class DailyTaskData(
    val date: String,
    val dailyTasks: List<TaskItem>,
    val bonusTasks: List<TaskItem>,
    val dailyScore: Int,
    val bonusScore: Int,
    val totalScore: Int,
    val completedDaily: Int,
    val completedBonus: Int
)

data class TaskItem(
    val key: String,
    val status: String,
    val points: Int
)
