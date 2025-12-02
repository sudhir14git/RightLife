package com.jetsynthesys.rightlife.ai_package.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
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
): Parcelable
