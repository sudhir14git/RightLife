package com.jetsynthesys.rightlife.ai_package.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class FrequentLoggedRoutine(
    @SerializedName("mealType")
    val mealType: String?,
    @SerializedName("mealName")
    val mealName: String?,
    @SerializedName("serve")
    var serve: String?,
    @SerializedName("cal")
    var cal: String?,
    @SerializedName("subtraction")
    var subtraction: String?,
    @SerializedName("baguette")
    var baguette: String?,
    @SerializedName("dewpoint")
    var dewpoint: String?,
    @SerializedName("icon")
    var icon: String?,
    @SerializedName("isAddDish")
    var isAddDish: Boolean?
):Parcelable
