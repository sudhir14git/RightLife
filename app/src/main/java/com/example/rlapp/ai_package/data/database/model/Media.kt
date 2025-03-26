package com.example.rlapp.ai_package.data.database.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import kotlinx.parcelize.Parcelize

@Parcelize
data class Media(
    @SerializedName("localPath")
    var localPath: String?,
    @SerializedName("remotePath")
    val remotePath: String?,
    @SerializedName("thumbnailPath")
    var thumbnailPath: String?,
    @SerializedName("faceCount")
    val faceCount: Int?,
    @SerializedName("consent")
    val consent: Boolean?,
    @SerializedName("blurOrderID")
    val blurOrderID: String?,
    @SerializedName("audioImprovedID")
    val audioImprovedID: String?,
    @SerializedName("approved")
    val approved: Boolean?
) : Parcelable