package com.jetsynthesys.rightlife.ai_package.model.response

data class MyMealsSaveResponse(
    val status_code: Int,
    val message: String,
    val data: MealsData
)

data class MealsData(
    val snap_meal_detail: List<SnapMealDetail>,
    val meal_details: List<MealDetails>,
    val combined_totals: CombinedTotals
)

data class SnapMealDetail(
    val _id: String,
    val user_id: String,
    val meal_type: String?,
    val meal_name: String,
    val date: String?,
    val dish: List<IngredientRecipeDetails>,
    val is_save: Boolean?,
    val is_snapped: Boolean?,
    val createdAt: String,
    val updatedAt: String,
    val total_servings: Double,
    val total_calories: Double,
    val total_carbs: Double,
    val total_protein: Double,
    val total_fat: Double,
    val total_sugar: Double,
    val total_cholesterol: Double,
    val total_iron: Double,
    val total_magnesium: Double,
    val image_url : String,
    var isSnapMealLog : Boolean = false
)

data class MealDetails(
    val _id: String,
    val user_id: String,
    val meal_type: String?,
    val meal_name: String,
    val createdAt: String,
    val isFavourite: Boolean,
    val receipe_data: List<IngredientRecipeDetails>,
    val total_servings: Double,
    val total_calories: Double,
    val total_carbs: Double,
    val total_protein: Double,
    val total_fat: Double,
    val total_sugar: Double,
    val total_cholesterol: Double,
    val total_iron: Double,
    val total_magnesium: Double,
    var isMealLog : Boolean = false
)

data class CombinedTotals(
    val total_servings: Double,
    val total_calories: Double,
    val total_carbs: Double,
    val total_protein: Double,
    val total_fat: Double,
    val total_sugar: Double,
    val total_cholesterol: Double,
    val total_iron: Double,
    val total_magnesium: Double
)
