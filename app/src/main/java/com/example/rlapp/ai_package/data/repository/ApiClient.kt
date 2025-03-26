package com.example.rlapp.ai_package.data.repository

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    private const val BASE_URL = "http://18.159.113.191:8081/"
    private const val BASE_URL_FAST_API = "http://18.159.113.191:8000/"

    val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
    val retrofitFastApi: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL_FAST_API)
            .addConverterFactory(GsonConverterFactory.create())
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
}
