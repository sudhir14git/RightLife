package com.jetsynthesys.rightlife.ai_package.model.response

data class EatRightLandingPageDataResponse(
    val message: String,
    val total_calories: Double,
    val total_protein: Double,
    val total_fat: Double,
    val total_carbs: Double,
    val max_calories: Double,
    val max_protein: Double,
    val max_carbs: Double,
    val max_fat: Double,
    val total_water_ml: Double,
    val last_weight_log: LastWeightLog?,
    val other_recipes_you_might_like: List<OtherRecipe>,
    val insight: InsightDetails,
    val micros: MicrosDetails
)

data class LastWeightLog(
    val weight: Double,
    val type: String,
    val date: String
)

data class OtherRecipe(
    val _id: String,
    val meal_name: String,
    val calories_kcal: Double,
    val protein_g: Double,
    val fat_g: Double,
    val carbs_g: Double,
    val image: String
)

data class MicrosDetails(
    val micros_message: String,
    val title: String,
    val value: Double,
    val micros_name: String,
    val unit: String,
)

data class InsightDetails(
    val macros_message: String,
    val heading: String
)
