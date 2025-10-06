package com.jetsynthesys.rightlife.ai_package.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipeListResponse(
    val success: Boolean,
    val message: String,
    val status_code: Int,
    val data: List<IngredientRecipeList>
):Parcelable

@Parcelize
data class IngredientRecipeList(
    val id: String,
    val recipe: String,
    val photo_url: String,
    val servings: Int,
    val active_cooking_time_min: Double,
    val calories_kcal: Double,
    val meal_type: String,
    val tags: String,
    val cuisine: String,
    val source : String
):Parcelable