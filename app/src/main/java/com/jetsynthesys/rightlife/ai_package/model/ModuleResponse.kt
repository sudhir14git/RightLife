package com.jetsynthesys.rightlife.ai_package.model

import com.google.gson.annotations.SerializedName

data class ModuleResponse(
    @SerializedName("success")
    var success : Boolean?,
    @SerializedName("statusCode")
    var statusCode : Int?,
    @SerializedName("data")
    var data : ArrayList<ModuleData> = arrayListOf()
)
data class ModuleData(
    @SerializedName("_id")
    var _id : String?,
    @SerializedName("title")
    var title : String?,
    @SerializedName("desc")
    var desc : String?,
    @SerializedName("image")
    var image : String?,
    @SerializedName("type")
    var type : String?,
    @SerializedName("isSelectedModule")
    var isSelectedModule : Boolean?,
    @SerializedName("breathInhaleTime")
    var breathInhaleTime : String?,
    @SerializedName("breathExhaleTime")
    var breathExhaleTime : String?,
    @SerializedName("breathHoldTime")
    var breathHoldTime : String?,
    @SerializedName("totalTime")
    var totalTime : String?
)
