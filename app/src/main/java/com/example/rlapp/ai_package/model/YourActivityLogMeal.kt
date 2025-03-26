package com.example.rlapp.ai_package.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class YourActivityLogMeal(
    @SerializedName("date")
    val date: String?,
    @SerializedName("day")
    val day: String?,
    @SerializedName("status")
    var status: Boolean?
): Parcelable