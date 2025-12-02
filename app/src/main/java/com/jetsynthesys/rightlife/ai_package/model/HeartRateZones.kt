package com.jetsynthesys.rightlife.ai_package.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HeartRateZones(
    @SerializedName("Light Zone")
    val lightZone: List<Int>,
    @SerializedName("Fat Burn Zone")
    val fatBurnZone: List<Int>,
    @SerializedName("Cardio Zone")
    val cardioZone: List<Int>,
    @SerializedName("Peak Zone")
    val peakZone: List<Int>
): Parcelable
