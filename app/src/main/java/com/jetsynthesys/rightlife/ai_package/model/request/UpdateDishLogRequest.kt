package com.jetsynthesys.rightlife.ai_package.model.request

data class UpdateDishLogRequest(
    val meal_name: String,
    val meal_log: List<DishLogItem>
)

data class DishLogItem(
    val receipe_id: String?,
    val meal_quantity: Double?,
    val unit: String?,
    val measure: String?
)