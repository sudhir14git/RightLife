package com.jetsynthesys.rightlife.ai_package.model

import com.google.gson.annotations.SerializedName
import com.jetsynthesys.rightlife.ai_package.model.response.Tags

data class ThinkRecomendedResponse(
    @SerializedName("success")
    var success    : Boolean?,
    @SerializedName("statusCode")
    var statusCode : Int?  ,
    @SerializedName("data")
    var data       : RecomendedAllData?
)

data class RecomendedAllData (
    @SerializedName("count")
    var count       : Int?  ,
    @SerializedName("contentList")
    var contentList : ArrayList<ContentList> = arrayListOf()
)

data class ContentList (
    @SerializedName("_id")
    var Id            : String?,
    @SerializedName("contentType")
    var contentType   : String?,
    @SerializedName("url")
    var url           : String?,
    @SerializedName("previewUrl")
    var previewUrl    : String?,
    @SerializedName("moduleId")
    var moduleId      : String?,
    @SerializedName("categoryId" )
    var categoryId    : String?,
    @SerializedName("subCategories" )
    var subCategories : ArrayList<SubCategories> = arrayListOf(),
    @SerializedName("artist"        )
    var artist        : ArrayList<Artist>        = arrayListOf(),
    @SerializedName("title"         )
    var title         : String?,
    @SerializedName("pricing"       )
    var pricing       : String?,
    @SerializedName("thumbnail"     )
    var thumbnail     : Thumbnail?,
    @SerializedName("desc"          )
    var desc          : String?,
    @SerializedName("tags"          )
    var tags          : ArrayList<Tags>        = arrayListOf(),
    @SerializedName("episodeCount"  )
    var episodeCount  : Int?,
    @SerializedName("readingTime"  )
    var readingTime  : String?,
    @SerializedName("isPromoted"    )
    var isPromoted    : Boolean?,
    @SerializedName("youtubeUrl"    )
    var youtubeUrl    : String?,
    @SerializedName("seriesType"    )
    var seriesType    : String?,
    @SerializedName("meta"          )
    var meta          : Meta?,
    @SerializedName("viewCount"     )
    var viewCount     : Int?,
    @SerializedName("order"         )
    var order         : Int?,
    @SerializedName("isActive"      )
    var isActive      : Boolean?,
    @SerializedName("createdAt"     )
    var createdAt     : String?,
    @SerializedName("updatedAt"     )
    var updatedAt     : String?,
    @SerializedName("promotedAt"    )
    var promotedAt    : String?,
    @SerializedName("moduleName"    )
    var moduleName    : String?,
    @SerializedName("categoryName"  )
    var categoryName  : String?,
    @SerializedName("isWatched"     )
    var isWatched     : Boolean?,
    @SerializedName("isAffirmated"  )
    var isAffirmated  : Boolean?,
    @SerializedName("isFavourited"  )
    var isFavourited  : Boolean?,
    @SerializedName("isAlarm"       )
    var isAlarm       : Boolean?
)

data class Meta (
    @SerializedName("duration"  )
    var duration  : Int?  ,
    @SerializedName("size"      )
    var size      : String? ,
    @SerializedName("sizeBytes" )
    var sizeBytes : Int?
)

data class Thumbnail (
    @SerializedName("url"   )
    var url   : String? ,
    @SerializedName("title" )
    var title : String?
)

data class Artist (
    @SerializedName("firstName")
    var firstName      : String?,
    @SerializedName("lastName")
    var lastName       : String? ,
    @SerializedName("profilePicture")
    var profilePicture : String? ,
    @SerializedName("_id")
    var Id             : String?
)

data class SubCategories (
    @SerializedName("name" )
    var name : String? ,
    @SerializedName("_id"  )
    var Id   : String?
)