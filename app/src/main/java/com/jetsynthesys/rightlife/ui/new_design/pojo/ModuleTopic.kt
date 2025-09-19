package com.jetsynthesys.rightlife.ui.new_design.pojo

import com.google.gson.annotations.Expose
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ModuleTopic(
    @SerializedName("_id") @Expose val id: String?,
    @SerializedName("moduleName") @Expose val moduleName: String?,
    @SerializedName("moduleTopic") @Expose val moduleTopic: String?,
    @SerializedName("moduleThumbnail") @Expose val moduleThumbnail: String?,
    @SerializedName("moduleDarkThemeThumbnail") @Expose val moduleDarkThemeThumbnail: String?,
    @SerializedName("order") @Expose val order: Int?,
    @SerializedName("isActive") @Expose val isActive: Boolean?,
    @SerializedName("createdAt") @Expose val createdAt: String?,
    @SerializedName("updatedAt") @Expose val updatedAt: String?,
    @SerializedName("__v") @Expose val v: Int?,
    @SerializedName("isSelectedModule") @Expose var isSelected: Boolean = false
) : Serializable