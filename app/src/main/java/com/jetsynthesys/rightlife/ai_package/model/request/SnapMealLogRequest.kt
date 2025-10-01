package com.jetsynthesys.rightlife.ai_package.model.request

import android.os.Parcelable
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import com.jetsynthesys.rightlife.ai_package.model.response.Serving
import kotlinx.android.parcel.Parcelize

@Parcelize
data class SnapMealLogRequest(
    val user_id: String?,
    val meal_type: String?,
    val meal_name: String?,
    val is_save: Boolean?,
    val is_snapped: Boolean?,
    val date: String?,
    val dish: List<IngredientRecipeDetails>,
    val image_url: String,
    val isSnapMealLogSelect : Boolean = false,
    val _id: String = "",
):Parcelable

@Parcelize
data class SnapDish(
    val id: String? = null,   // optional, only present sometimes
    val food_name: String,
    val serving_size_for_calorific_breakdown: String,
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
    val cholesterol_mg: Double?,
    val quantity: Double,
    val servings: Int,
    val unit: String?,
    val source_urls: String?,
    val selected_serving: Serving? = null,
    val default_serving: Serving? = null,
    val available_serving: List<Serving>? = null
):Parcelable

