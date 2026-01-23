package com.jetsynthesys.rightlife.subscriptions.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class Price : Serializable {
    @SerializedName("INR")
    @Expose
    var inr: Double? = null

    @SerializedName("USD")
    @Expose
    var usd: Double? = null
}