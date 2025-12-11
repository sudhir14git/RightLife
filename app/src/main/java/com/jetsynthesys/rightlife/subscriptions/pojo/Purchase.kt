package com.jetsynthesys.rightlife.subscriptions.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Purchase : Serializable {
    @SerializedName("planName")
    @Expose
    var planName: String? = null
}