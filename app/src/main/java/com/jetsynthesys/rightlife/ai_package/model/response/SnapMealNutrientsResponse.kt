package com.jetsynthesys.rightlife.ai_package.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class SnapMealNutrientsResponse(
    val status_code: Int,
    val message: String,
    val data: MealNutrientsData
): Parcelable

@Parcelize
data class MealNutrientsData(
    val meal_name: String,
    val image_url: String,
    val dish: List<IngredientRecipeDetails>
): Parcelable