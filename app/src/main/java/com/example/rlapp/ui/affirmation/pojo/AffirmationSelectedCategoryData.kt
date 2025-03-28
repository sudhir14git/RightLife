package com.example.rlapp.ui.affirmation.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName

class AffirmationSelectedCategoryData {
    @SerializedName("_id")
    @Expose
    var id: String? = null

    @SerializedName("title")
    @Expose
    var title: String? = null

    @SerializedName("image")
    @Expose
    var image: String? = null

    @SerializedName("textColor")
    @Expose
    var textColor: String? = null

    @SerializedName("bgColor")
    @Expose
    var bgColor: String? = null

    @SerializedName("artist")
    @Expose
    var artist: Artist? = null

    @SerializedName("catagoryId")
    @Expose
    var catagoryId: String? = null

    @SerializedName("isActive")
    @Expose
    var isActive: Boolean? = null

    @SerializedName("createdAt")
    @Expose
    var createdAt: String? = null

    @SerializedName("updatedAt")
    @Expose
    var updatedAt: String? = null

    @SerializedName("__v")
    @Expose
    var v: Int? = null

    @SerializedName("categoryName")
    @Expose
    var categoryName: String? = null


}