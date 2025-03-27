package com.example.rlapp.ui.questionnaire.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AnswerWaterCoffee {
    @SerializedName("water")
    @Expose
    var water: Water? = null

    @SerializedName("coffee")
    @Expose
    var coffee: Coffee? = null
}