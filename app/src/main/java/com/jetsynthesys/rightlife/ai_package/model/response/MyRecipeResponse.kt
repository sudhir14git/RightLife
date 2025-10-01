package com.jetsynthesys.rightlife.ai_package.model.response

data class MyRecipeResponse(
    val status_code: Int,
    val message: String,
    val data: List<MyRecipe>
)

data class MyRecipe(
    val _id: String,
    val user_id: String,
    val recipe: String,
    val category: String,
    val calories_kcal: Double,
    val carbs_g: Double,
    val protein_g: Double,
    val fat_g: Double,
    val servings: Double,
    val photo_url: String,
    val selected_serving: Serving?,
    val ingredients: List<Ingredient>,
    var isRecipeLog : Boolean = false
)

data class Ingredient(
    val ingredient_id: String,
    val ingredient_name: String,
    val quantity: Double,
    val standard_serving_size: String,
    val description: String,
    val food_category: String,
    val photo_url: String,
    val calories_kcal: Double,
    val carbs_g: Double,
    val sugars_g: Double,
    val fiber_g: Double,
    val protein_g: Double,
    val fat_g: Double
)


