package com.jetsynthesys.rightlife.ai_package.ui.eatright.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class MacroNutrientsModel(
    @SerializedName("nutrientsValue")
    val nutrientsValue: String?,
    @SerializedName("nutrientsUnit")
    val nutrientsUnit: String?,
    @SerializedName("nutrientsEnergy")
    var nutrientsEnergy: String?,
    @SerializedName("nutrientsIcon")
    var nutrientsIcon: Int?
):Parcelable