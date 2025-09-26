package com.jetsynthesys.rightlife.ai_package.model

import com.google.gson.annotations.SerializedName

data class ToolsData(
    @SerializedName("_id"       )
    var _id       : String?,
    @SerializedName("title"         )
    var title         : String?,
    @SerializedName("desc"       )
    var desc       : String?,
    @SerializedName("isSelectedModule" )
    var isSelectedModule : Boolean? ,
    @SerializedName("isPlaylist" )
    var isPlaylist : Boolean? ,
    @SerializedName("image"        )
    var image        : String?
)
