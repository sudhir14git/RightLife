package com.jetsynthesys.rightlife.ui.questionnaire.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class Water {
    @SerializedName("cups")
    @Expose
    var cups: String? = null

    @SerializedName("quantity")
    @Expose
    var quantity: String? = null
}