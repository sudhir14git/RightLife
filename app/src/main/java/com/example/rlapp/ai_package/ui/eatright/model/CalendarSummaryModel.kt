package com.example.rlapp.ai_package.ui.eatright.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class CalendarSummaryModel(
    @SerializedName("surplusType")
    val surplusType: String,
    @SerializedName("surplusValue")
    val surplusValue: String
): Parcelable