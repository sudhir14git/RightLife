package com.example.rlapp.ai_package.data.repository

import com.example.rlapp.ai_package.model.MealLogsResponseModel
import com.example.rlapp.ai_package.model.RecipeResponseModel
import com.example.rlapp.ai_package.model.WorkoutResponse
import com.example.rlapp.ai_package.model.WorkoutResponseModel
import com.example.rlapp.ai_package.ui.eatright.model.LandingPageResponse
import retrofit2.Call
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Path
import retrofit2.http.Query

interface ApiService {

    @GET("app/api/activityMaster")
    fun getWorkoutList(
        @Header("Authorization") authToken: String): Call<WorkoutResponseModel>

    @GET("app/api/meal-plan/meal-recipes-lists")
    fun getMealRecipesList(
        @Header("Authorization") authToken: String): Call<RecipeResponseModel>

    @GET("app/api/meal-plan/meal-logs")
     fun getMealLogLists(
        @Header("Authorization") authToken: String): Call<MealLogsResponseModel>

     @GET("app/api/meal-plan/meal-logs")
     fun getWorkout(
        @Header("Authorization") authToken: String): Call<MealLogsResponseModel>

    @GET("move/data/user_workouts/")
    suspend fun getUserWorkouts(
        @Query("user_id") userId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<WorkoutResponse>

    @GET("eat/landing-page/64763fe2fa0e40d9c0bc8264/")
    fun getMealSummary(
        @Query("date") startDate: String): Call<LandingPageResponse>
}


