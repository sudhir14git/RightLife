package com.example.rlapp.ai_package.ui.eatright.model

data class LandingPageResponse(
    val message: String,
    val total_calories: Int,
    val total_protein: Int,
    val total_fat: Int,
    val total_carbs: Int,
    val meals: List<Meal>,
    val next_meal_suggestion: MealSuggestion,
    val other_recipes_you_might_like: List<RecipeSuggestion>
)

data class Meal(
    val _id: String,
    val user_id: String,
    val meal: String,
    val date: String,  // You may use LocalDate if you parse the date.
    val quantity: Double,
    val name: String,
    val image: String,
    val mealType: String,
    val mealQuantity: Double,
    val cookingTime: String,
    val numOfServings: Int,
    val calories: Int,
    val protein: Int,
    val fats: Int,
    val fiber: Int,
    val saturatedFat: Int,
    val carbs: Int,
    val sugar: Int,
    val calcium: Int,
    val iron: Int,
    val potassium: Int,
    val sodium: Int,
    val vitaminD: Int,
    val cholesterol: Int,
    val transFat: Int,
    val ingredients: List<String>,
    val instructions: List<String>,
    val unit: String,
    val isConsumed: Boolean,
    val isAteSomethingElse: Boolean,
    val isSkipped: Boolean,
    val isSwapped: Boolean,
    val isRepeat: Boolean,
    val isFavourite: Boolean,
    val notes: List<String>
)

data class MealSuggestion(
    val meal_name: String,
    val calories: Int,
    val protein: Int,
    val fats: Int,
    val carbs: Int
)

data class RecipeSuggestion(
    val meal_name: String,
    val calories: Int,
    val protein: Int,
    val fats: Int,
    val carbs: Int
)
