package com.jetsynthesys.rightlife.subscriptions.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

class PlanList: Serializable {
    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("applePay")
    @Expose
    var applePay: String? = null

    @SerializedName("category")
    @Expose
    var category: String? = null

    @SerializedName("desc")
    @Expose
    var desc: String? = null

    @SerializedName("googlePlay")
    @Expose
    var googlePlay: String? = null

    @SerializedName("price")
    @Expose
    var price: Price? = null

    @SerializedName("purchase")
    @Expose
    var purchase: Purchase? = null

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("discountPrice")
    @Expose
    var discountPrice: DiscountPrice? = null

    @SerializedName("discountPercent")
    @Expose
    var discountPercent: Int? = null

    @SerializedName("status")
    @Expose
    var status: String? = null

    @SerializedName("endDateTime")
    @Expose
    var endDateTime: String? = null
}