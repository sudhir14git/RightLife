package com.example.rlapp.ui.questionnaire.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class ERQuestionFive {
    @SerializedName("module")
    @Expose
    var module: String? = "EAT_RIGHT"

    @SerializedName("answer")
    @Expose
    var answer: AnswerWaterCoffee? = null
}