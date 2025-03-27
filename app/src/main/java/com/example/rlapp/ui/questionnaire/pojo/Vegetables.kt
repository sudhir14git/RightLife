package com.example.rlapp.ui.questionnaire.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Vegetables {
    @SerializedName("servings")
    @Expose
    var servings: String? = null
}