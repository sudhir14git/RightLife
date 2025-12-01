package com.jetsynthesys.rightlife.subscriptions.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class DiscountPrice {
    @SerializedName("INR")
    @Expose
    var inr: Double? = null

    @SerializedName("USD")
    @Expose
    var usd: Double? = null
}