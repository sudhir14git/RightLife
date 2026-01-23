package com.jetsynthesys.rightlife.ai_package.model.response

data class MealLogDetailsResponse(
    val status_code: Int,
    val message: String,
    val data: SnapMealData
)

data class SnapMealData(
    val meal_details: SnapMealLogDetails
)

data class SnapMealLogDetails(
    val _id: String,
    val user_id: String,
    val meal_type: String,
    val meal_name: String,
    val date: String,
    val is_save: Boolean,
    val is_snapped: Boolean,
    val dish: List<IngredientRecipeDetails>,
    val createdAt: String,
    val updatedAt: String,
    val image_url : String,
    val meal_nutrition_summary: NutritionSummary
)

data class SnapDishLists(
    val name: String,
    val b12_mcg: Double,
    val b1_mg: Double,
    val b2_mg: Double,
    val b3_mg: Double,
    val b6_mg: Double,
    val calcium_mg: Double,
    val calories_kcal: Double,
    val carb_g: Double,
    val cholesterol_mg: Double,
    val copper_mg: Double,
    val fat_g: Double,
    val folate_mcg: Double,
    val fiber_g: Double,
    val iron_mg: Double,
    val is_beverage: Double,
    val magnesium_mg: Double,
    val mass_g: Double,
    val monounsaturated_g: Double,
    val omega_3_fatty_acids_g: Double,
    val omega_6_fatty_acids_g: Double,
    val percent_fruit: Double,
    val percent_legume_or_nuts: Double,
    val percent_vegetable: Double,
    val phosphorus_mg: Double,
    val polyunsaturated_g: Double,
    val potassium_mg: Double,
    val protein_g: Double,
    val saturated_fats_g: Double,
    val selenium_mcg: Double,
    val sodium_mg: Double,
    val source_urls: List<String>,
    val sugar_g: Double,
    val vitamin_a_mcg: Double,
    val vitamin_c_mg: Double,
    val vitamin_d_iu: Double,
    val vitamin_e_mg: Double,
    val vitamin_k_mcg: Double,
    val zinc_mg: Double,
    val _id: String
)

data class NutritionSummary(
    val calories_kcal: Double,
    val protein_g: Double,
    val fat_g: Double,
    val carbs_g: Double,
    val fiber_g: Double,
    val sugars_g: Double,
    val vit_a_mcg: Double,
    val vit_c_mg: Double,
    val vit_d_mcg: Double,
    val vit_e_mg: Double,
    val vit_k_mcg: Double,
    val thiamin_b1_mg: Double,
    val riboflavin_b2_mg: Double,
    val niacin_b3_mg: Double,
    val vit_b6_mg: Double,
    val folate_b9_mcg: Double,
    val vit_b12_mcg: Double,
    val calcium_mg: Double,
    val iron_mg: Double,
    val magnesium_mg: Double,
    val zinc_mg: Double,
    val potassium_mg: Double,
    val sodium_mg: Double,
    val phosphorus_mg: Double,
    val omega3_g: Double,
    val cholesterol_mg: Double,
    val active_cooking_time_min: Double,
    val quantity: Double,
    val servings: Double
)

