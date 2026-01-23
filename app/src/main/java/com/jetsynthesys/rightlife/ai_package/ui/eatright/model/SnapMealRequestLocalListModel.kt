package com.jetsynthesys.rightlife.ai_package.ui.eatright.model

import android.os.Parcelable
import com.google.gson.annotations.SerializedName
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealLogRequest
import kotlinx.parcelize.Parcelize

@Parcelize
data class SnapMealRequestLocalListModel(
    @SerializedName("data")
    var data: ArrayList<SnapMealLogRequest>
): Parcelable
