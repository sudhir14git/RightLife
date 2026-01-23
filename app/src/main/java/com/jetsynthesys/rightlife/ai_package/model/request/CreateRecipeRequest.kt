package com.jetsynthesys.rightlife.ai_package.model.request

data class CreateRecipeRequest(
    val recipe_name: String,
    val ingredients: List<IngredientEntry>,
    val servings : Double
)

data class IngredientEntry(
    val ingredient_id: String?,
    val quantity: Int?,
    val standard_serving_size: String?
)
