package com.jetsynthesys.rightlife.ui

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.widget.Toast
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.ai_package.model.AddToolRequest
import com.jetsynthesys.rightlife.ai_package.model.BaseResponse
import com.jetsynthesys.rightlife.ai_package.model.request.MindfullRequest
import com.jetsynthesys.rightlife.ui.settings.pojo.NotificationData
import com.jetsynthesys.rightlife.ui.settings.pojo.NotificationsResponse
import com.jetsynthesys.rightlife.ui.therledit.EpisodeTrackRequest
import com.jetsynthesys.rightlife.ui.therledit.ViewCountRequest
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import kotlin.math.roundToInt


object CommonAPICall {
    fun addToToolKit(
        context: Context, moduleId: String?, isSelectedModule: Boolean
    ) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
        val authToken = sharedPreferenceManager.accessToken
        val apiService = ApiClient.getClient(context).create(ApiService::class.java)

        val toolKitRequest = AddToolRequest(moduleId, isSelectedModule)
        val call = apiService.addToToolKit(authToken, toolKitRequest)
        call.enqueue(object : Callback<CommonResponse> {
            override fun onResponse(
                call: Call<CommonResponse>,
                response: Response<CommonResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    Toast.makeText(
                        context,
                        response.body()!!.successMessage,
                        Toast.LENGTH_SHORT
                    ).show()
                } else {
                    Toast.makeText(
                        context,
                        "Server Error: " + response.code(),
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                Toast.makeText(
                    context,
                    "Network Error: " + t.message,
                    Toast.LENGTH_SHORT
                ).show()
            }
        })
    }

    fun uploadImageToPreSignedUrl(
        context: Context,
        file: File,
        preSignedUrl: String,
        onResult: (Boolean) -> Unit
    ) {
        val client = OkHttpClient()

        val mediaType = "image/png".toMediaTypeOrNull() // or "image/jpeg"
        val requestBody = file.asRequestBody(mediaType)

        val request = Request.Builder()
            .url(preSignedUrl)
            .put(requestBody)
            .addHeader("x-amz-acl", "public-read") // if required
            .build()

        client.newCall(request).enqueue(object : okhttp3.Callback {
            override fun onFailure(call: okhttp3.Call, e: IOException) {
                e.printStackTrace()
                Handler(Looper.getMainLooper()).post {
                    onResult(false)
                }
            }

            override fun onResponse(call: okhttp3.Call, response: okhttp3.Response) {
                response.use {
                    Handler(Looper.getMainLooper()).post {
                        onResult(response.isSuccessful)
                    }
                }
            }

        })
    }

    fun updateNotificationSettings(
        context: Context,
        requestBody: Map<String, Boolean>,
        onResult: (Boolean, String) -> Unit
    ) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
        val authToken = sharedPreferenceManager.accessToken
        val apiService = ApiClient.getClient(context).create(ApiService::class.java)
        apiService.updateNotificationSettings(authToken, requestBody)
            .enqueue(object : Callback<CommonResponse> {
                override fun onResponse(
                    call: Call<CommonResponse>,
                    response: Response<CommonResponse>
                ) {
                    Handler(Looper.getMainLooper()).post {
                        onResult(response.isSuccessful, response.body()?.successMessage!!)
                    }
                }

                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    Handler(Looper.getMainLooper()).post {
                        onResult(false, t.message!!)
                    }
                }
            })
    }

    fun getNotificationSettings(
        context: Context,
        onResult: (NotificationData) -> Unit
    ) {
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
        val authToken = sharedPreferenceManager.accessToken
        val apiService = ApiClient.getClient(context).create(ApiService::class.java)
        apiService.getNotificationSettings(authToken)
            .enqueue(object : Callback<NotificationsResponse> {
                override fun onResponse(
                    call: Call<NotificationsResponse>,
                    response: Response<NotificationsResponse>
                ) {
                    if (response.isSuccessful && response.body() != null)
                        Handler(Looper.getMainLooper()).post {
                            response.body()?.data?.let { onResult(it) }
                        } else
                        Toast.makeText(
                            context,
                            response.message(),
                            Toast.LENGTH_SHORT
                        ).show()
                }

                override fun onFailure(call: Call<NotificationsResponse>, t: Throwable) {
                    Toast.makeText(
                        context,
                        t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })
    }


    fun updateChecklistStatus(
        context: Context,
        type: String,
        status: String,
        responseListener: ((success: Boolean) -> Unit)? = null
    ) {
        val authToken = SharedPreferenceManager.getInstance(context).accessToken
        val apiService = ApiClient.getClient(context).create(ApiService::class.java)

        val body = mapOf(type to status)

        apiService.updateCheckListStatus(authToken, body)
            .enqueue(object : Callback<CommonResponse> {
                override fun onResponse(
                    call: Call<CommonResponse>,
                    response: Response<CommonResponse>
                ) {
                    if (response.isSuccessful)
                        responseListener?.invoke(true)
                    else
                        responseListener?.invoke(false)
                }

                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    t.message?.let { responseListener?.invoke(false) }
                }

            })
    }


    data class CmConversionResult(
        val cmText: String,
        val cmIndex: Int,
        val inchIndex: Int
    )

    fun convertFeetInchToCmWithIndex(height: String): CmConversionResult {
        val regex = Regex("(\\d+)\\s*Ft\\s*(\\d+)\\s*In", RegexOption.IGNORE_CASE)
        val match = regex.find(height.trim())

        if (match != null && match.groupValues.size >= 3) {
            val feet = match.groupValues[1].toInt()
            val inch = match.groupValues[2].toInt()

            val totalInches = (feet * 12) + inch
            val cm = totalInches * 2.54
            val cmRounded = cm.roundToInt()

            return CmConversionResult(
                cmText = "$cmRounded cms",
                cmIndex = cmRounded,
                inchIndex = totalInches
            )
        } else {
            throw IllegalArgumentException("Invalid height format. Expected format: '5 Ft 10 In'")
        }
    }


    data class HeightConversionResult(
        val feetInchText: String,
        val inchIndex: Int,
        val cmIndex: Int
    )

    fun convertCmToFeetInchWithIndex(height: String): HeightConversionResult {
        val regex = Regex("(\\d+)")
        val match = regex.find(height.trim())

        if (match != null) {
            val cm = match.groupValues[1].toDouble()
            val totalInches = cm / 2.54
            val feet = totalInches.toInt() / 12
            val inch = (totalInches % 12).roundToInt()

            val totalInchIndex = (feet * 12) + inch
            val cmIndex = cm.roundToInt()

            val formatted = "$feet Ft $inch In"
            return HeightConversionResult(
                feetInchText = formatted,
                inchIndex = totalInchIndex,
                cmIndex = cmIndex
            )
        } else {
            throw IllegalArgumentException("Invalid height format. Expected format: '182 cms'")
        }
    }

    fun trackEpisodeOrContent(context: Context, episodeTrackRequest: EpisodeTrackRequest) {
        val authToken = SharedPreferenceManager.getInstance(context).accessToken
        val apiService = ApiClient.getClient(context).create(ApiService::class.java)

        //val episodeTrackRequest = EpisodeTrackRequest()


        val call = apiService.trackEpisode(
            authToken,
            episodeTrackRequest
        )

        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {

                if (response.isSuccessful && response.body() != null) {
                    Log.d("AAAA", "status = " + response.body().toString())
                } else {
                    Log.d("AAAA", "status = " + response.body().toString())
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                Toast.makeText(context, "Network Error: " + t.message, Toast.LENGTH_SHORT).show()
            }

        })
    }

    fun updateViewCount(context: Context, viewCountRequest: ViewCountRequest) {
        val authToken = SharedPreferenceManager.getInstance(context).accessToken
        val apiService = ApiClient.getClient(context).create(ApiService::class.java)
        val call = apiService.updateViewCount(authToken, viewCountRequest)

        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    try {
                        val jsonString = response.body()!!.string()
                        Log.d("API_RESPONSE", "View Count content: $jsonString")
                    } catch (e: IOException) {
                        throw RuntimeException(e)
                    }
                } else {
                    Log.e("API_ERROR", "Error: " + response.errorBody())
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                Log.e("Failuer", "Error: " + t.message)
            }
        })
    }

    fun postMindFullData(context: Context, type: String, startDate: String, endDate: String) {
        val mindFullRequest =
            MindfullRequest(type = type, startDate = startDate, endDate = endDate)
        val authToken = SharedPreferenceManager.getInstance(context).accessToken
        val apiService = ApiClient.getClient(context).create(ApiService::class.java)
        val call = apiService.postMindFull(authToken, mindFullRequest)
        call.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {

            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {

            }
        })
    }

    data class WellnessStreakRequest(
        val status: String,
        val serviceName: String
    )

    fun postWellnessStreak(context: Context, serviceName: String, status: String = "COMPLETE") {
        val wellnessStreakRequest =
            WellnessStreakRequest(status, serviceName)
        val authToken = SharedPreferenceManager.getInstance(context).accessToken
        val apiService = ApiClient.getClient(context).create(ApiService::class.java)
        val call = apiService.postWellnessStreak(authToken, wellnessStreakRequest)
        call.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {

            }

            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {

            }
        })
    }


}