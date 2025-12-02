package com.jetsynthesys.rightlife.ai_package.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class HeartRateZoneMinutes(
    @SerializedName("Below Light")
    val belowLight: Int,
    @SerializedName("Light Zone")
    val lightZone: Int,
    @SerializedName("Fat Burn Zone")
    val fatBurnZone: Int,
    @SerializedName("Cardio Zone")
    val cardioZone: Int,
    @SerializedName("Peak Zone")
    val peakZone: Int
): Parcelable