package com.jetsynthesys.rightlife.ui

import com.google.gson.annotations.SerializedName
import com.jetsynthesys.rightlife.ui.profile_new.pojo.Artist
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.continuemodela.EpisodeDetails
import com.jetsynthesys.rightlife.ui.profile_new.pojo.Meta
import com.jetsynthesys.rightlife.ui.profile_new.pojo.SubCategory
import com.jetsynthesys.rightlife.ui.profile_new.pojo.Thumbnail

data class CategoryListResponse(
    @SerializedName("success")
    val success: Boolean? = null,

    @SerializedName("statusCode")
    val statusCode: Int? = null,

    @SerializedName("data")
    val data: CategoryListData? = null
)

data class CategoryListData(
    @SerializedName("count")
    val count: Int? = null,

    @SerializedName("contentList")
    val contentList: List<CategoryListItem>? = null
)

data class CategoryListItem(
    @SerializedName("_id")
    val id: String? = null,

    @SerializedName("contentType")
    val contentType: String? = null,

    @SerializedName("url")
    val url: String? = null,

    @SerializedName("previewUrl")
    val previewUrl: String? = null,

    @SerializedName("moduleId")
    val moduleId: String? = null,

    @SerializedName("categoryId")
    val categoryId: String? = null,

    @SerializedName("subCategories")
    val subCategories: List<SubCategory>?,

    @SerializedName("artist")
    val artist: List<Artist>?,

    @SerializedName("title")
    val title: String? = null,

    @SerializedName("pricing")
    val pricing: String? = null,

    @SerializedName("thumbnail")
    val thumbnail: Thumbnail?,

    @SerializedName("desc")
    val desc: String? = null,

    /*@SerializedName("tags")
    val tags: List<String>? = null,*/

    @SerializedName("episodeCount")
    val episodeCount: Int? = null,

    @SerializedName("isPromoted")
    val isPromoted: Boolean? = null,

    @SerializedName("youtubeUrl")
    val youtubeUrl: String? = null,

    @SerializedName("seriesType")
    val seriesType: String? = null,

    @SerializedName("meta")
    val meta: Meta?,

    @SerializedName("viewCount")
    val viewCount: Int? = null,

    @SerializedName("order")
    val order: Int? = null,

    @SerializedName("isActive")
    val isActive: Boolean? = null,

    @SerializedName("readingTime")
    val readingTime: String? = null,

    @SerializedName("createdAt")
    val createdAt: String? = null,

    @SerializedName("updatedAt")
    val updatedAt: String? = null,

    @SerializedName("shareUrl")
    val shareUrl: String? = null,

    @SerializedName("moduleName")
    val moduleName: String? = null,

    @SerializedName("categoryName")
    val categoryName: String? = null,

    @SerializedName("isWatched")
    val isWatched: Boolean = false,

    @SerializedName("isAffirmated")
    val isAffirmated: Boolean = false,

    @SerializedName("isFavourited")
    val isFavourited: Boolean = false,

    @SerializedName("isAlarm")
    val isAlarm: Boolean = false,

    @SerializedName("leftDuration")
    val leftDuration: String?,

    @SerializedName("leftDurationINT")
    val leftDurationINT: Int?,

    @SerializedName("date")
    val date: String?,

    @SerializedName("episodeDetails")
    val episodeDetails: EpisodeDetails?,

    @SerializedName("isBookmarked")
    var isBookmarked: Boolean = false
)