package com.jetsynthesys.rightlife.ai_package.data.repository

import android.util.Log
import com.jetsynthesys.rightlife.BuildConfig
import kotlinx.serialization.json.Json
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

/*
    private const val BASE_URL = BuildConfig.BASE_URL//BuildConfig.BASE_URL//"http://18.159.113.191:8081/"
    private const val BASE_URL_FAST_API = BuildConfig.BASE_URL_AI  //BuildConfig.BASE_URL_AI*/
  
    /*private const val BASE_URL = "http://18.159.113.191:8081/"//BuildConfig.BASE_URL//"http://18.159.113.191:8081/"
    private const val BASE_URL_FAST_API = "http://18.159.113.191:8000/"  //BuildConfig.BASE_URL_AI*/
  
    private const val BASE_URL = BuildConfig.BASE_URL
    private const val BASE_URL_FAST_API = BuildConfig.BASE_URL_AI
    private const val BASE_URL_FAST_API_V2 = BuildConfig.BASE_URL_AI_V2

    private const val BASE_URL_FOOD_CAPTURE_API = "https://api.spoonacular.com/"
    private const val BASE_URL_FOOD_CAPTURE_NEW_API =
        "https://us-central1-snapcalorieb2bapi.cloudfunctions.net/"

    private val loggingInterceptor = HttpLoggingInterceptor { message ->
        when {
            message.startsWith("-->") -> Log.d("API_REQUEST", message)
            message.startsWith("<--") -> Log.d("API_RESPONSE", message)
            else -> Log.d("API_DETAIL", message)
        }
    }.apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(40, TimeUnit.SECONDS) // Set connection timeout (default: 10s)
        .readTimeout(40, TimeUnit.SECONDS)    // Set read timeout
        .writeTimeout(40, TimeUnit.SECONDS)   // Set write timeout
        .build()

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    private val json = Json {
        ignoreUnknownKeys = true // Ignore unknown keys in JSON
        coerceInputValues = true // Coerce invalid values to default (e.g., null for missing fields)
    }
    val retrofitFastApi: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_FAST_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val retrofitFastApiV2: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_FAST_API_V2)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // Attach custom OkHttpClient with timeouts
            .build()
    }

    val retrofitFoodCaptureApi: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_FOOD_CAPTURE_API)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val retrofitFoodCaptureImageApi: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_FOOD_CAPTURE_NEW_API)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient) // Attach custom OkHttpClient with timeouts
            .build()
    }
}

object ApiClient {
    val apiService: ApiService by lazy {
        RetrofitClient.retrofit.create(ApiService::class.java)
    }

    val apiServiceFastApi: ApiService by lazy {
        RetrofitClient.retrofitFastApi.create(ApiService::class.java)
    }

    val apiServiceFastApiV2: ApiService by lazy {
        RetrofitClient.retrofitFastApiV2.create(ApiService::class.java)
    }

    val apiServiceFoodCaptureApi: ApiService by lazy {
        RetrofitClient.retrofitFoodCaptureApi.create(ApiService::class.java)
    }

    val apiServiceFoodCaptureImageApi: ApiService by lazy {
        RetrofitClient.retrofitFoodCaptureImageApi.create(ApiService::class.java)
    }
}
