package com.jetsynthesys.rightlife.ai_package.model.response

data class IngredientResponse(
    val status_code: Int,
    val meassge: String,
    val data: List<IngredientLists>
)

data class IngredientLists(
    val id: String,
    val food_code: String,
    val food_name: String,
    val food_category: String,
    val photo_url: String,
    val standard_serving_size: String,
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
    val omega3_g: Double
)



