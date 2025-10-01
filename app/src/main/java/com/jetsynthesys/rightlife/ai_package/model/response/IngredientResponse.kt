package com.jetsynthesys.rightlife.ai_package.model.response

data class IngredientResponse(
    val status_code: Int,
    val meassge: String,
    val data: List<IngredientRecipeList>
)



