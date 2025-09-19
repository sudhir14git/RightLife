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
    val photo_url: String
)



