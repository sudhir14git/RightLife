package com.example.rlapp.ai_package.ui.eatright.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MealLogDateModel(
    @SerializedName("date")
    val date: String?,
    @SerializedName("day")
    val day: String?,
    @SerializedName("status")
    var status: Boolean?
):Parcelable