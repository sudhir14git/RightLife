package com.jetsynthesys.rightlife.ui.profile_new.pojo

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class BookmarkResponse(
    @SerializedName("success")
    val success: Boolean?,

    @SerializedName("statusCode")
    val statusCode: Int?,

    @SerializedName("data")
    val data: BookMarkContentData?
) : Serializable

data class BookMarkContentData(
    @SerializedName("contentDetails")
    val contentDetails: List<BookMarkContentDetails>?,

    @SerializedName("count")
    val count: Int?,

    @SerializedName("skip")
    val skip: Int?,

    @SerializedName("limit")
    val limit: Int?
) : Serializable

data class BookMarkContentDetails(
    @SerializedName("_id")
    val id: String?,

    @SerializedName("contentType")
    val contentType: String?,

    @SerializedName("url")
    val url: String?,

    @SerializedName("previewUrl")
    val previewUrl: String?,

    @SerializedName("moduleId")
    val moduleId: String?,

    @SerializedName("categoryId")
    val categoryId: String?,

    @SerializedName("subCategories")
    val subCategories: List<SubCategory>?,

    @SerializedName("artist")
    val artist: List<Artist>?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("pricing")
    val pricing: String?,

    @SerializedName("thumbnail")
    val thumbnail: Thumbnail?,

    @SerializedName("desc")
    val desc: String?,

    @SerializedName("article")
    val article: List<String>?,

    @SerializedName("tags")
    val tags: List<Tag>?,

    @SerializedName("youtubeUrl")
    val youtubeUrl: String?,

    @SerializedName("meta")
    val meta: Meta?,

    @SerializedName("viewCount")
    val viewCount: Int?,

    @SerializedName("tableOfContents")
    val tableOfContents: List<String>?,

    @SerializedName("readingTime")
    val readingTime: String?,

    @SerializedName("summary")
    val summary: String?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("shareUrl")
    val shareUrl: String?,

    @SerializedName("categoryName")
    val categoryName: String?,

    @SerializedName("isFavourited")
    val isFavourited: Boolean?,

    @SerializedName("isAffirmated")
    val isAffirmated: Boolean?,

    @SerializedName("isAlarm")
    val isAlarm: Boolean?,

    @SerializedName("isWatched")
    val isWatched: Boolean?,

    @SerializedName("isSubscribed")
    val isSubscribed: Boolean?,

    @SerializedName("isAffirmation")
    val isAffirmation: Boolean?,

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
) : Serializable

data class SubCategory(
    @SerializedName("_id")
    val id: String?,

    @SerializedName("name")
    val name: String?
) : Serializable

data class Artist(
    @SerializedName("_id")
    val id: String?,

    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?,

    @SerializedName("profilePicture")
    val profilePicture: String?
) : Serializable

data class Thumbnail(
    @SerializedName("url")
    val url: String?,

    @SerializedName("title")
    val title: String?
) : Serializable

data class Tag(
    @SerializedName("_id")
    val id: String?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("appId")
    val appId: String?,

    @SerializedName("isDeleted")
    val isDeleted: Boolean?,

    @SerializedName("default")
    val default: Boolean?,

    @SerializedName("createdAt")
    val createdAt: Long?,

    @SerializedName("updatedAt")
    val updatedAt: Long?,

    @SerializedName("__v")
    val v: Int?
) : Serializable

data class Meta(
    @SerializedName("duration")
    val duration: Int?,

    @SerializedName("size")
    val size: String?,

    @SerializedName("sizeBytes")
    val sizeBytes: Long?
) : Serializable

data class EpisodeDetails(
    @SerializedName("_id")
    val id: String?,

    @SerializedName("contentId")
    val contentId: String?,

    @SerializedName("type")
    val type: String?,

    @SerializedName("title")
    val title: String?,

    @SerializedName("episodeNumber")
    val episodeNumber: Int?,

    @SerializedName("desc")
    val desc: String?,

    @SerializedName("artist")
    val artist: List<String>?,

    @SerializedName("thumbnail")
    val thumbnail: Thumbnail?,

    @SerializedName("youtubeUrl")
    val youtubeUrl: String?,

    @SerializedName("pricingTier")
    val pricingTier: String?,

    @SerializedName("tags")
    val tags: List<String>?,

    @SerializedName("isDeleted")
    val isDeleted: Boolean?,

    @SerializedName("meta")
    val meta: Meta?,

    @SerializedName("viewCount")
    val viewCount: Int?,

    @SerializedName("order")
    val order: Int?,

    @SerializedName("isActive")
    val isActive: Boolean?,

    @SerializedName("createdAt")
    val createdAt: String?,

    @SerializedName("updatedAt")
    val updatedAt: String?,

    @SerializedName("__v")
    val v: Int?,

    @SerializedName("tagDetails")
    val tagDetails: List<Tag>?,

    @SerializedName("artistDetails")
    val artistDetails: List<Artist>?
) : Serializable
