package com.jetsynthesys.rightlife.ai_package.model.request

data class CreateMealRequest(
    val meal_name: String,
    val meal_type: String,
    val isFavourite: Boolean,
    val recipes: List<MealRecipe>,
    val ingredients: List<MealIngredient>
)

data class MealRecipe(
    val recipe_id: String?,
    val meal_quantity: Double,
    val selected_serving_type: String?,
    val selected_serving_value: Double?
)

data class MealIngredient(
    val ingredient_id: String?,
    val meal_quantity: Double,
    val standard_serving_size: String?
)

