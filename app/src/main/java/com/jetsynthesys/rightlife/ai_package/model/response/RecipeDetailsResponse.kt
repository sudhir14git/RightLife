package com.jetsynthesys.rightlife.ai_package.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class RecipeDetailsResponse(
    val status_code: Int,
    val data: IngredientRecipeDetails
): Parcelable

