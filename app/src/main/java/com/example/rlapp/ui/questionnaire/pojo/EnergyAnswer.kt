package com.example.rlapp.ui.questionnaire.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class EnergyAnswer {
    @SerializedName("morning")
    @Expose
    var morning: String? = null

    @SerializedName("evening")
    @Expose
    var evening: String? = null

    @SerializedName("night")
    @Expose
    var night: String? = null
}