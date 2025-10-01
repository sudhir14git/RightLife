package com.jetsynthesys.rightlife.ai_package.model.response

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class IngredientRecipeListResponse(
    val status_code: Int,
    val message: String,
    val data: List<IngredientRecipeList>
):Parcelable
