package com.jetsynthesys.rightlife.ai_package.model.response

data class FrequentRecipesResponse(
    val status_code: Int,
    val message: String,
    val data: FrequentRecipesData
)

data class FrequentRecipesData(
    val user_id: String,
    val frequent_recipes: List<FrequentRecipe>
)

data class FrequentRecipe(
    val id: String?,
    val recipe_id: String?,
    val food_code: String?,
    val food_name: String?,
    val recipe: String,
    val meal_type: String?,
    val cuisine: String?,
    val regional_split: String?,
    val category: String?,
    val food_category: String?,
    val flag: String?,
    val serving_size_for_calorific_breakdown: String?,
    val standard_serving_size: String,
    val calories_kcal: Double?,
    val protein_g: Double?,
    val fat_g: Double?,
    val carbs_g: Double?,
    val fiber_g: Double?,
    val sugars_g: Double?,
    val vit_a_mcg: Double?,
    val vit_c_mg: Double?,
    val vit_d_mcg: Double?,
    val vit_e_mg: Double?,
    val vit_k_mcg: Double?,
    val thiamin_b1_mg: Double?,
    val riboflavin_b2_mg: Double?,
    val niacin_b3_mg: Double?,
    val vit_b6_mg: Double?,
    val folate_b9_mcg: Double?,
    val vit_b12_mcg: Double?,
    val calcium_mg: Double?,
    val iron_mg: Double?,
    val magnesium_mg: Double?,
    val zinc_mg: Double?,
    val potassium_mg: Double?,
    val sodium_mg: Double?,
    val phosphorus_mg: Double?,
    val omega3_g: Double?,
    val ingredients: List<String>,        // empty list in your sample
    val preparation_notes: List<String>,  // empty list in your sample
    val active_cooking_time_min: Int,
    val allergy_groups_restricted_from_consuming: String,
    val tags: String?,
    val typical_1person_serving: String?,
    val household_measure_1_serving: String?,
    val photo_url: String,
    val selected_serving: Serving?,       // {} → nullable
    val default_serving: Serving?,        // {} → nullable
    val available_serving: List<Serving>, // [] → list
    val source: String?,
    val quantity: Double,
    val servings: Double,
    var isFrequentLog : Boolean = false
)
