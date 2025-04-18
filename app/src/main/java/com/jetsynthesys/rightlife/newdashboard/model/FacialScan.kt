package com.jetsynthesys.rightlife.newdashboard.model

import com.google.gson.annotations.SerializedName


data class FacialScan(
    @SerializedName("totalCount") val totalCount: Int?,
    @SerializedName("data") val data: ArrayList<ScanData>?,
    @SerializedName("key") val key: String?,
    @SerializedName("minValue") val minValue: Double?,
    @SerializedName("maxValue") val maxValue: Double?,
    @SerializedName("avgValue") val avgValue: Double?,
    @SerializedName("avgIndicator") val avgIndicator: String?,
    @SerializedName("avgUnit") val avgUnit: String?,
    @SerializedName("avgParameter") val avgParameter: String?
)

