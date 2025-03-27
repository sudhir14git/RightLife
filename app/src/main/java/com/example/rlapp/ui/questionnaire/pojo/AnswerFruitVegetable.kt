package com.example.rlapp.ui.questionnaire.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AnswerFruitVegetable {
    @SerializedName("fruits")
    @Expose
    var fruits: Fruits? = null

    @SerializedName("vegetables")
    @Expose
    var vegetables: Vegetables? = null
}