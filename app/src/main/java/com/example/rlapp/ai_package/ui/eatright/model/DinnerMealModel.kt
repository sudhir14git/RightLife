package com.example.rlapp.ai_package.ui.eatright.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class DinnerMealModel(
    @SerializedName("mealType")
    val mealType: String?,
    @SerializedName("mealName")
    val mealName: String?,
    @SerializedName("mealVegNonVeg")
    val mealVegNonVeg: String?,
    @SerializedName("eatTime")
    val eatTime: String?,
    @SerializedName("serve")
    var serve: String?,
    @SerializedName("cal")
    var cal: String?,
    @SerializedName("subtraction")
    var subtraction: String?,
    @SerializedName("baguette")
    var baguette: String?,
    @SerializedName("dewpoint")
    var dewpoint: String?
):Parcelable