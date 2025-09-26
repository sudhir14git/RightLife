package com.jetsynthesys.rightlife.newdashboard.model

import com.google.gson.annotations.SerializedName
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.continuemodela.Artist
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.continuemodela.EpisodeDetails
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.continuemodela.Meta
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.continuemodela.Tag
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.continuemodela.Thumbnail

class ContentDetails {
    @SerializedName("contentType")
    val contentType: String? = null

    @SerializedName("url")
    val url: String? = null

    @SerializedName("previewUrl")
    val previewUrl: String? = null

    @SerializedName("moduleId")
    val moduleId: String? = null

    @SerializedName("categoryId")
    val categoryId: String? = null

    @SerializedName("title")
    val title: String? = null

    @SerializedName("pricing")
    val pricing: String? = null

    @SerializedName("thumbnail")
    val thumbnail: Thumbnail? = null

    @SerializedName("desc")
    val desc: String? = null

    @SerializedName("tags")
    val tags: List<Tag>? = null

    @SerializedName("youtubeUrl")
    val youtubeUrl: String? = null

    @SerializedName("meta")
    val meta: Meta? = null

    @SerializedName("viewCount")
    val viewCount = 0

    @SerializedName("createdAt")
    val createdAt: String? = null

    @SerializedName("shareUrl")
    val shareUrl: String? = null

    @SerializedName("_id")
    val id: String? = null

    @SerializedName("categoryName")
    val categoryName: String? = null

    @SerializedName("isFavourited")
    val isFavourited = false

    @SerializedName("isAffirmated")
    val isAffirmated = false

    @SerializedName("isAlarm")
    val isAlarm = false

    @SerializedName("isWatched")
    val isWatched = false

    @SerializedName("isSubscribed")
    val isSubscribed = false

    @SerializedName("isBookmarked")
    var isBookmarked = false

    @SerializedName("isAffirmation")
    val isAffirmation = false

    @SerializedName("leftDuration")
    val leftDuration: String? = null

    @SerializedName("leftDurationINT")
    val leftDurationINT: Int? = null

    @SerializedName("date")
    val date: String? = null

    @SerializedName("episodeDetails")
    val episodeDetails: EpisodeDetails? = null

    @SerializedName("artist")
    val artist: List<Artist>? = null // Getters...
}
