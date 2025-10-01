package com.jetsynthesys.rightlife.ui.profile_new.pojo

import com.google.gson.annotations.SerializedName
import com.jetsynthesys.rightlife.apimodel.rlpagemodels.continuemodela.EpisodeDetails
import java.io.Serializable

data class BookmarkResponse(
    @SerializedName("success")
    val success: Boolean?,

    @SerializedName("statusCode")
    val statusCode: Int?,

    @SerializedName("data")
    val data: BookmarkData?
) : Serializable

data class BookmarkData(
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

    // Article can be [] (empty array) or list of objects → use List<ArticleItem>
    @SerializedName("article")
    val article: List<Article>?,

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
    @SerializedName("name")
    val name: String?,

    @SerializedName("_id")
    val id: String?
) : Serializable

data class Artist(
    @SerializedName("firstName")
    val firstName: String?,

    @SerializedName("lastName")
    val lastName: String?,

    @SerializedName("_id")
    val id: String?
) : Serializable

data class Thumbnail(
    @SerializedName("url")
    val url: String?,

    @SerializedName("title")
    val title: String?
) : Serializable

data class Article(
    @SerializedName("htmlContent")
    val htmlContent: String?,

    @SerializedName("thumbnail")
    val thumbnail: String?,

    @SerializedName("recommendedProduct")
    val recommendedProduct: String?,

    @SerializedName("recommendedService")
    val recommendedService: String?,

    @SerializedName("recommendedArticle")
    val recommendedArticle: String?,

    @SerializedName("recommendedLive")
    val recommendedLive: String?,

    @SerializedName("funFacts")
    val funFacts: FunFacts?,

    @SerializedName("_id")
    val id: String?
) : Serializable

data class FunFacts(
    @SerializedName("heading")
    val heading: String?,

    @SerializedName("description")
    val description: String?,

    @SerializedName("_id")
    val id: String?
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
