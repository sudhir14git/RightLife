package com.example.rlapp.ai_package.model

import com.google.gson.annotations.SerializedName

data class HeartRateZonePercentages(
    @SerializedName("Below Light")
    val belowLight: Float,

    @SerializedName("Light Zone")
    val lightZone: Float,

    @SerializedName("Fat Burn Zone")
    val fatBurnZone: Float,

    @SerializedName("Cardio Zone")
    val cardioZone: Float,

    @SerializedName("Peak Zone")
    val peakZone: Float
)
