package com.example.rlapp.ai_package.model

import android.os.Parcelable
import kotlinx.parcelize.Parcelize


@Parcelize
data class MealLogsResponseModel(
    val success: Boolean,
    val statusCode: Int,
    val data: List<MealLogData>
): Parcelable

@Parcelize
data class MealLogData(
    val _id: String,
    val userId: String,
    val preferenceId: String,
    val startDate: String,
    val endDate: String,
    val recipes: List<DailyRecipe>,
    val mealPlanId: String,
    val createdAt: String,
    val updatedAt: String,
    val __v: Int,
    val trackWeight: List<WeightTrack>
):Parcelable

@Parcelize
data class DailyRecipe(
    val day: String,
    val date: String,
    val currentDay: String,
    val calories: Double,
    val carbs: Double,
    val proteins: Double,
    val fats: Double,
    val energyConsumption: EnergyConsumption,
    val trackedMeals: Int,
    val meals: List<MealList>,
    val _id: String,
    val status : Boolean = false
):Parcelable

@Parcelize
data class EnergyConsumption(
    val calories: Double,
    val proteins: Double,
    val fats: Double,
    val carbs: Double,
    val sugar: Double,
    val fiber: Double,
    val saturatedFat: Double,
    val transFat: Double,
    val cholesterol: Double,
    val sodium: Double,
    val potassium: Double
):Parcelable

@Parcelize
data class MealList(
    val name: String,
    val image: String,
    val mealType: String,
    val mealQuantity: Double,
    val cookingTime: String,
    val numOfServings: Int,
    val calories: Double,
    val protein: Double,
    val fats: Double,
    val fiber: Double,
    val saturatedFat: Double,
    val carbs: Double,
    val sugar: Double,
    val calcium: Double,
    val iron: Double,
    val potassium: Double,
    val sodium: Double,
    val vitaminD: Double,
    val cholesterol: Double,
    val transFat: Double,
    val ingredients: List<Ingredient>,
    val instructions: List<String>,
    val unit: String,
    val isConsumed: Boolean,
    val isAteSomethingElse: Boolean,
    val isSkipped: Boolean,
    val isSwapped: Boolean,
    val isRepeat: Boolean,
    val isFavourite: Boolean,
    val _id: String,
    val notes: List<String>
):Parcelable

@Parcelize
data class Ingredient(
    val name: String,
    val isChecked: Boolean,
    val _id: String
):Parcelable

@Parcelize
data class WeightTrack(
    val weight: Double,
    val unit: String,
    val createdAt: String
):Parcelable