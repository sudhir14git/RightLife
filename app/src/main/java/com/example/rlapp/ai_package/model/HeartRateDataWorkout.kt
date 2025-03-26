package com.example.rlapp.ai_package.model

import com.google.gson.annotations.SerializedName

import java.io.Serializable

data class HeartRateDataWorkout(
    @SerializedName("heart_rate")
    val heartRate: Int, // We'll handle the String-to-Int conversion in deserialization

    @SerializedName("timestamp")
    val date: String, // Map "timestamp" from JSON to "date"

    @SerializedName("unit")
    val unit: String, // Include the unit field from JSON

    val trendData: ArrayList<String> = ArrayList() // Default to empty list; can be populated later
) : Serializable
