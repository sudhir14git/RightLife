package com.jetsynthesys.rightlife.ai_package.model.request

data class CreateRecipeRequest(
    val recipe_name: String,
    val ingredients: List<IngredientEntry>,
    val total_time: String,
    val course_one_or_more: String,
    val tags_optional: String,
    val cuisine_optional: String,
    val photo_url: String
)

data class IngredientEntry(
    val ingredient_id: String?,
    val quantity: Int?,
    val measure: String?
)
