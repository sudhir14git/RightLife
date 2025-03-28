package com.example.rlapp.ai_package.model

import com.google.gson.annotations.SerializedName

data class HeartRateZones(
    @SerializedName("Light Zone")
    val lightZone: List<Int>,

    @SerializedName("Fat Burn Zone")
    val fatBurnZone: List<Int>,

    @SerializedName("Cardio Zone")
    val cardioZone: List<Int>,

    @SerializedName("Peak Zone")
    val peakZone: List<Int>
)
