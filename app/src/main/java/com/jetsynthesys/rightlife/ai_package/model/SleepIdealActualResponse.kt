package com.jetsynthesys.rightlife.ai_package.model

import com.google.gson.annotations.SerializedName

data class SleepIdealActualResponse(
    @SerializedName("status_code")
    var statusCode : Int? ,
    @SerializedName("message")
    var message    : String? ,
    @SerializedName("data")
    var data       : SleepData?
)

data class SleepData(
    @SerializedName("user_id")
    var userId            : String?,
    @SerializedName("source")
    var source            : String?,
    @SerializedName("start_date")
    var startDate         : String?,
    @SerializedName("end_date")
    var endDate           : String?,
    @SerializedName("average_sleep")
    var averageSleep      : Double?,
    @SerializedName("average_needed")
    var averageNeeded     : Double?,
    @SerializedName("sleep_insight_detail" )
    var sleepInsightDetail              : IdealActualInsightDetail?,
    @SerializedName("progress_detail" )
    var progress_detail              : ProgressDetail?,
    @SerializedName("time_data_breakdown" )
    var timeDataBreakdown : ArrayList<TimeDataBreakdown> = arrayListOf()
)

data class IdealActualInsightDetail(
    @SerializedName("message")
    var message : String? ,
    @SerializedName("title")
    var title  : String? ,
    @SerializedName("action")
    var action  : String?
)

data class ProgressInfo(
    @SerializedName("progress_percentage")
    val progress_percentage: Float,
    @SerializedName("progress_sign")
    val progress_sign: String
)
data class ProgressDetail(
    @SerializedName("actual_sleep")
    val actual_sleep: ProgressInfo,
    @SerializedName("needed_sleep")
    val needed_sleep: ProgressInfo
)

data class TimeDataBreakdown (
    @SerializedName("date")
    var date             : String? ,
    @SerializedName("actual_sleep_hours")
    var actualSleepHours : Double? ,
    @SerializedName("ideal_sleep_hours")
    var idealSleepHours  : Double?
)