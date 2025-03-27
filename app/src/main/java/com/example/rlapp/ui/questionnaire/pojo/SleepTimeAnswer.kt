package com.example.rlapp.ui.questionnaire.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class SleepTimeAnswer {
    @SerializedName("bedTime")
    @Expose
    var bedTime: String? = null

    @SerializedName("wakeTime")
    @Expose
    var wakeTime: String? = null

    @SerializedName("sleepDuration")
    @Expose
    var sleepDuration: String? = null
}