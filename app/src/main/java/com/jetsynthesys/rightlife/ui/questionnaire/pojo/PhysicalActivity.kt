package com.jetsynthesys.rightlife.ui.questionnaire.pojo

data class PhysicalActivityResponse(
    val status: Int,
    val message: String,
    val data: List<PhysicalActivity>
)

data class PhysicalActivity(
    val id: String,
    val title: String,
    val description: String?,
    val type: String?,
    val metValues: String?,
    val workoutFactors: String?,
    val isResistance: Boolean?,
    val isWeightBased: Boolean?,
    val caloriesBasedOn: String?,
    val imageUrl: String?,
    val duration: Int?,
    val userFriendlyName: String?,
    val createdAt: String?,
    val updatedAt: String?,
    val v: Int?,
    val translatedLanguage: String?,
    var isSelected: Boolean = false
)
