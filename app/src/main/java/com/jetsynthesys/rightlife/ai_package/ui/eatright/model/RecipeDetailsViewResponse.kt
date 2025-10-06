package com.jetsynthesys.rightlife.ai_package.ui.eatright.model

import android.os.Parcelable
import com.jetsynthesys.rightlife.ai_package.model.response.Serving
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipeDetailsViewResponse(
    val status_code: Int,
    val data: RecipeDetailsViewData
): Parcelable

@Parcelize
data class RecipeDetailsViewData(
    val id: String,
    val recipe_id: String,
    val recipe: String,
    val meal_type: String,
    val cuisine: String,
    val regional_split: String,
    val category: String,
    val flag: String,
    val serving_size_for_calorific_breakdown: String,
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
    val ingredients: List<String>,
    val preparation_notes: List<String>,
    val active_cooking_time_min: Int,
    val allergy_groups_restricted_from_consuming: String,
    val tags: String,
    val typical_1person_serving: String,
    val household_measure_1_serving: String,
    val photo_url: String,
    val selected_serving: Serving,
    val default_serving: Serving,
    val available_serving: List<Serving>
):Parcelable


