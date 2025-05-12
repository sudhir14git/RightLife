package com.jetsynthesys.rightlife.ai_package.ui.eatright.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientDetail
import kotlinx.parcelize.Parcelize

@Parcelize
data class IngredientLocalListModel(
    @SerializedName("data")
    var data: ArrayList<IngredientDetail>
): Parcelable
