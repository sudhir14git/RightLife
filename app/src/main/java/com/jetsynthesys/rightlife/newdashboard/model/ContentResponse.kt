package com.jetsynthesys.rightlife.newdashboard.model

import com.google.gson.annotations.SerializedName

class ContentResponse {
    @SerializedName("success")
    val isSuccess: Boolean = false

    @SerializedName("statusCode")
    val statusCode: Int = 0

    @SerializedName("data")
    val data: ContentJumpBackInData? = null
}
