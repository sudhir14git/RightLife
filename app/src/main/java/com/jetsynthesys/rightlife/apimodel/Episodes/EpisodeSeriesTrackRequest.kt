package com.jetsynthesys.rightlife.apimodel.Episodes

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class EpisodeSeriesTrackRequest(
    @SerializedName("watchDuration")
    val watchDuration: String,

    @SerializedName("episodeId")
    val episodeId: String,

    @SerializedName("contentId")
    val contentId: String,

    @SerializedName("userId")
    val userId: String,

    @SerializedName("moduleId")
    val moduleId: String,

    @SerializedName("duration")
    val duration: String,

    @SerializedName("contentType")
    val contentType: String
) : Serializable
