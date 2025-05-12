package com.jetsynthesys.rightlife.ai_package.data.repository

import com.jetsynthesys.rightlife.ai_package.model.ActiveCaloriesResponse
import com.jetsynthesys.rightlife.ai_package.model.AddEmojiRequest
import com.jetsynthesys.rightlife.ai_package.model.AddToolRequest
import com.jetsynthesys.rightlife.ai_package.model.AffirmationPlaylistResponse
import com.jetsynthesys.rightlife.ai_package.model.MealLogsResponseModel
import com.jetsynthesys.rightlife.ai_package.model.MealsResponse
import com.jetsynthesys.rightlife.ai_package.model.NutritionResponse
import com.jetsynthesys.rightlife.ai_package.model.RecipeResponseModel
import com.jetsynthesys.rightlife.ai_package.model.RestorativeSleepResponse
import com.jetsynthesys.rightlife.ai_package.model.AnalysisRequest
import com.jetsynthesys.rightlife.ai_package.model.AssignRoutineRequest
import com.jetsynthesys.rightlife.ai_package.model.AssignRoutineResponse
import com.jetsynthesys.rightlife.ai_package.model.BaseResponse
import com.jetsynthesys.rightlife.ai_package.model.CalculateCaloriesRequest
import com.jetsynthesys.rightlife.ai_package.model.CalculateCaloriesResponse
import com.jetsynthesys.rightlife.ai_package.model.CreateRoutineRequest
import com.jetsynthesys.rightlife.ai_package.model.CreateRoutineResponse
import com.jetsynthesys.rightlife.ai_package.model.DeleteCalorieResponse
import com.jetsynthesys.rightlife.ai_package.model.FitnessResponse
import com.jetsynthesys.rightlife.ai_package.model.FoodDetailsResponse
import com.jetsynthesys.rightlife.ai_package.model.MealLogRequest
import com.jetsynthesys.rightlife.ai_package.model.MealLogResponse
import com.jetsynthesys.rightlife.ai_package.model.FrequentlyLoggedResponse
import com.jetsynthesys.rightlife.ai_package.model.HeartRateFitDataResponse
import com.jetsynthesys.rightlife.ai_package.model.HeartRateResponse
import com.jetsynthesys.rightlife.ai_package.model.HeartRateVariabilityResponse
import com.jetsynthesys.rightlife.ai_package.model.JournalAnswerResponse
import com.jetsynthesys.rightlife.ai_package.model.LogNapRequest
import com.jetsynthesys.rightlife.ai_package.model.MindfullResponse
import com.jetsynthesys.rightlife.ai_package.model.ModuleResponse
import com.jetsynthesys.rightlife.ai_package.model.MoodTrackerMonthlyResponse
import com.jetsynthesys.rightlife.ai_package.model.MoodTrackerWeeklyResponse
import com.jetsynthesys.rightlife.ai_package.model.RestingHeartRateResponse
import com.jetsynthesys.rightlife.ai_package.model.RoutineResponse
import com.jetsynthesys.rightlife.ai_package.model.ScanMealNutritionResponse
import com.jetsynthesys.rightlife.ai_package.model.SleepConsistencyResponse
import com.jetsynthesys.rightlife.ai_package.model.SleepIdealActualResponse
import com.jetsynthesys.rightlife.ai_package.model.SleepJsonRequest
import com.jetsynthesys.rightlife.ai_package.model.SleepLandingResponse
import com.jetsynthesys.rightlife.ai_package.model.SleepPerformanceResponse
import com.jetsynthesys.rightlife.ai_package.model.SleepStageResponse
import com.jetsynthesys.rightlife.ai_package.model.StoreHealthDataRequest
import com.jetsynthesys.rightlife.ai_package.model.StoreHealthDataResponse
import com.jetsynthesys.rightlife.ai_package.model.ThinkQuoteResponse
import com.jetsynthesys.rightlife.ai_package.model.ToolsGridResponse
import com.jetsynthesys.rightlife.ai_package.model.ToolsResponse
import com.jetsynthesys.rightlife.ai_package.model.UpdateCalorieRequest
import com.jetsynthesys.rightlife.ai_package.model.UpdateCalorieResponse
import com.jetsynthesys.rightlife.ai_package.model.WakeupTimeResponse
import com.jetsynthesys.rightlife.ai_package.model.WorkoutMoveMainResponseRoutine
import com.jetsynthesys.rightlife.ai_package.model.WorkoutMoveResponseRoutine
import com.jetsynthesys.rightlife.ai_package.model.WorkoutResponse
import com.jetsynthesys.rightlife.ai_package.model.WorkoutResponseModel
import com.jetsynthesys.rightlife.ai_package.model.WorkoutResponseRoutine
import com.jetsynthesys.rightlife.ai_package.model.request.CreateRecipeRequest
import com.jetsynthesys.rightlife.ai_package.model.request.CreateWorkoutRequest
import com.jetsynthesys.rightlife.ai_package.model.request.LogRoutineRequest
import com.jetsynthesys.rightlife.ai_package.model.request.MealPlanLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.MealPlanRequest
import com.jetsynthesys.rightlife.ai_package.model.request.MealSaveRequest
import com.jetsynthesys.rightlife.ai_package.model.request.SaveDishLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.SaveSnapMealLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateCaloriesRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateMealRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateRoutineRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateSnapMealRequest
import com.jetsynthesys.rightlife.ai_package.model.request.WaterIntakeRequest
import com.jetsynthesys.rightlife.ai_package.model.request.WeightIntakeRequest
import com.jetsynthesys.rightlife.ai_package.model.response.CalorieAnalysisResponse
import com.jetsynthesys.rightlife.ai_package.model.response.ConsumedCaloriesResponse
import com.jetsynthesys.rightlife.ai_package.model.response.ConsumedCarbsResponse
import com.jetsynthesys.rightlife.ai_package.model.response.ConsumedCholesterolResponse
import com.jetsynthesys.rightlife.ai_package.model.response.ConsumedFatResponse
import com.jetsynthesys.rightlife.ai_package.model.response.ConsumedIronResponse
import com.jetsynthesys.rightlife.ai_package.model.response.ConsumedMagnesiumResponse
import com.jetsynthesys.rightlife.ai_package.model.response.ConsumedProteinResponse
import com.jetsynthesys.rightlife.ai_package.model.response.ConsumedSugarResponse
import com.jetsynthesys.rightlife.ai_package.model.response.DeleteCaloriesResponse
import com.jetsynthesys.rightlife.ai_package.model.response.EatRightLandingPageDataResponse
import com.jetsynthesys.rightlife.ai_package.model.response.FitnessData
import com.jetsynthesys.rightlife.ai_package.model.response.FrequentRecipesResponse
import com.jetsynthesys.rightlife.ai_package.model.response.GetCaloriesResponse
import com.jetsynthesys.rightlife.ai_package.model.response.LogRoutineResponse
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientDetailResponse
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientResponse
import com.jetsynthesys.rightlife.ai_package.model.response.LogWaterResponse
import com.jetsynthesys.rightlife.ai_package.model.response.LogWeightResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealLogDataResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealLogPlanResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealLogsHistoryResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealPlanResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealsLogResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MoveRoutineResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MyMealsSaveResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MyRecipeResponse
import com.jetsynthesys.rightlife.ai_package.model.response.RecipeResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SearchResultsResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SetStepsGoalResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealLogResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealRecipeResponseModel
import com.jetsynthesys.rightlife.ai_package.model.response.StepTrackerResponse
import com.jetsynthesys.rightlife.ai_package.model.response.UpdateCaloriesResponse
import com.jetsynthesys.rightlife.ai_package.model.response.UpdateRoutineResponse
import com.jetsynthesys.rightlife.ai_package.model.response.WaterIntakeResponse
import com.jetsynthesys.rightlife.ai_package.model.response.WeightResponse
import com.jetsynthesys.rightlife.ai_package.model.response.WorkoutHistoryResponse
import com.jetsynthesys.rightlife.ai_package.model.response.WorkoutPlanResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.ActivityFactorResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MoveDashboardResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeResponseNew
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.model.AssessmentResponse
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.Query
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Url

interface ApiService {

    @GET("app/api/activityMaster")
    fun getWorkoutList(
        @Header("Authorization") authToken: String): Call<WorkoutResponseModel>

    @GET("app/api/meal-plan/meal-recipes-lists")
    fun getMealRecipesList(
        @Header("Authorization") authToken: String): Call<RecipeResponseModel>

    @GET("eat/recipes/names")
    fun getSnapMealRecipesList(
       ): Call<SnapMealRecipeResponseModel>

    @GET("eat/recipes/{id}")
    fun getSnapMealRecipeById(
        @Path("id") recipeId: String
    ): Call<RecipeResponseNew>

    @GET("eat/recipes/names")
    fun getRecipesList(
        @Query("meal_type") mealType: String? = null,
        @Query("food_type") foodType: String? = null,
        @Query("cuisine") cuisine: String? = null
    ): Call<SnapMealRecipeResponseModel>

    @GET("eat/recipes/{foodId}")
    fun getSnapMealRecipesDetails(
        @Path("foodId") foodId: String): Call<RecipeResponse>

    @GET("eat/fetch_ingredient_detail/")
    fun getRecipesDetails(
        @Query("ingredient_id") ingredientId: String): Call<IngredientDetailResponse>

    @GET("eat/recipes/frequently_more_than_five/{userId}")
    fun getFrequentlyLog(
        @Path("userId") userId: String): Call<FrequentRecipesResponse>

    @GET("app/api/meal-plan/meal-recipes-lists/{foodId}")
    fun getMealRecipesDetails(
        @Path("foodId") foodId: String,
        @Header("Authorization") authToken: String): Call<FoodDetailsResponse>

    @GET("app/api/tools")
    fun getToolList(
        @Header("Authorization") authToken: String,
        @Query("userId") userId: String
    ): Call<ModuleResponse>

    @GET("app/api/meal-plan/meal-logs")
    fun getMealLogLists(
        @Header("Authorization") authToken: String): Call<MealLogsResponseModel>

    @GET("eat/get-meals/")
    fun getMealList(@Query("user_id") userId: String,
                @Query("date") startDate: String): Call<MealsResponse>

    @GET("eat/meals/get_search/")
    fun getSearchMealList(@Query("keyword") keyword: String): Call<SearchResultsResponse>

    @GET("eat/fetch_ingredient_list/")
    fun getSearchIngredientList(@Query("limit") limit: String): Call<IngredientResponse>

    @POST("eat/log-meal/")
    fun createLogDish(@Body request: MealLogRequest): Call<MealLogResponse>

    @POST("eat/meal-plans/")
    fun createLogMeal(@Query("user_id") userId: String,@Body request: MealPlanRequest): Call<MealPlanResponse>

    @POST("eat/meals/create_meal/")
    fun createMealsSave(@Query("user_id") userId: String,@Body request: MealSaveRequest): Call<MealUpdateResponse>

    @POST("eat/recipes/create/")
    fun createRecipe(@Query("user_id") userId: String,@Body request: CreateRecipeRequest): Call<MealUpdateResponse>

    @PUT("eat/recipes/update")
    fun updateRecipe(@Query("recipe_id") recipeId: String, @Query("user_id") userId: String,@Body request: CreateRecipeRequest): Call<MealUpdateResponse>

    @POST("eat/meals/log_meal/")
    fun createSaveMealsToLog(@Query("user_id") userId: String,
                             @Query("date") startDate: String,@Body request: SaveDishLogRequest
    ): Call<MealUpdateResponse>

    @PUT("eat/meals/update_meal/")
    fun updateSaveMeal(@Query("meal_id") mealId: String, @Query("user_id") userId: String,@Body request: UpdateMealRequest
    ): Call<MealUpdateResponse>

    @PUT("eat/meals/update_meal/")
    fun updateSnapSaveMeal(@Query("meal_id") mealId: String, @Query("user_id") userId: String,@Body request: UpdateSnapMealRequest
    ): Call<MealUpdateResponse>

    @POST("eat/meals/log_saved_meal/")
    fun createSaveSnapMealsToLog(@Query("user_id") userId: String,
                             @Query("date") startDate: String,@Body request: SaveSnapMealLogRequest
    ): Call<MealUpdateResponse>

    @DELETE("eat/meals/delete_meal/")
    fun deleteMyMeal(@Query("user_id") userId: String, @Query("meal_id") mealId: String): Call<MealUpdateResponse>

    @DELETE("eat/recipes/delete/")
    fun deleteMyRecipe(@Query("recipe_id") mealId: String, @Query("user_id") userId: String): Call<MealUpdateResponse>

    @GET("eat/meals/get_log_meals/")
    fun getMealsLogList(@Query("user_id") userId: String,
                    @Query("date") startDate: String): Call<MealsLogResponse>

    @GET("eat/meals/get_log_meals_byDate/")
    fun getMealsLogByDate(@Query("user_id") userId: String,
                        @Query("date") startDate: String): Call<MealLogDataResponse>

    @GET("eat/meals/get_logs_history/")
    fun getMealsLogHistoryCalender(@Query("user_id") userId: String,
                          @Query("date_range") dateRange: String): Call<MealLogsHistoryResponse>

    @GET("eat/meals/get_logs_history/")
    fun getMealsLogHistory(@Query("user_id") userId: String,
                           @Query("date") startDate: String): Call<MealLogsHistoryResponse>

    @POST("eat/snap_meals_log/")
    fun createSnapMealLog(@Body request: SnapMealLogRequest): Call<SnapMealLogResponse>

    @GET("eat/meal-plans/")
    fun getLogMealList(@Query("user_id") userId: String): Call<MealLogPlanResponse>

    @GET("eat/meals/get_mymeal_list/")
    fun getMyMealList(@Query("user_id") userId: String): Call<MyMealsSaveResponse>

    @GET("eat/recipes/my_recipes_list/")
    fun getMyRecipeList(@Query("limit") limit: String, @Query("user_id") userId: String): Call<MyRecipeResponse>

    @POST("eat/meal-plans/log/")
    fun createMealPlanLog(@Query("user_id") userId: String,@Query("meal_plan_id") mealPlanId: String,@Body request: MealPlanLogRequest): Call<MealPlanResponse>

    @GET("move/calendar_view_activity_details/")
    fun getActivityLogHistoryCalender(@Query("user_id") userId: String,
                                      @Query("source") source: String,
                                   @Query("date_range") dateRange: String): Call<WorkoutHistoryResponse>

    @GET("move/calendar_view_activity_details/")
    fun getActivityLogHistory(@Query("user_id") userId: String,
                              @Query("source") source: String,
                           @Query("date") startDate: String): Call<WorkoutHistoryResponse>

    @GET("move/data/user_workouts/")
    suspend fun getUserWorkouts(
        @Query("user_id") userId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<WorkoutResponse>

    @GET("move/data/new_user_workouts/")
    suspend fun getNewUserWorkouts(
        @Query("user_id") userId: String,
        @Query("start_date") start_date: String,
        @Query("end_date") end_date: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<WorkoutResponse>

    @POST("move/store/")
    suspend fun storeHealthData(
        @Body request: StoreHealthDataRequest
    ): Response<StoreHealthDataResponse>

    @POST("move/store/")
    suspend fun storeSleepData(
        @Body request: SleepJsonRequest
    ): Response<StoreHealthDataResponse>

    @POST("move/routine/create/")
    suspend fun createRoutine(
        @Body request: CreateRoutineRequest
    ): Response<CreateRoutineResponse>

    @GET("move/data/get_calories/")
    suspend fun getCalories(
        @Query("user_id") userId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("include_stats") includeStats: Boolean
    ): Response<GetCaloriesResponse>

    @POST("move/data/calculate_calories/")
    suspend fun createWorkout(
        @Body request: CreateWorkoutRequest
    ): Response<CalculateCaloriesResponse>

    @PUT("move/data/update_calories/")
    suspend fun updateCalories(
        @Query("calorie_id") calorieId: String,
        @Body request: UpdateCaloriesRequest
    ): Response<UpdateCaloriesResponse>

    @GET("move/fetch_active_burned/")
    suspend fun getActiveCalories(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<ActiveCaloriesResponse>

    @GET("move/fetch_calorie_analysis/")
    suspend fun getCalorieAnalysis(
        @Query("user_id") userId: String,
        @Query("date") date: String,
        @Query("period") period: String
    ): Response<CalorieAnalysisResponse>

    @GET("move/steps_detail_view/")
    suspend fun getStepsDetail(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<StepTrackerResponse>

    @POST("move/set_steps_goal/")
    suspend fun setStepsGoal(
        @Query("user_id") userId: String,
        @Query("source") source: String,
        @Query("steps_goal") stepsGoal: Int
    ): Response<SetStepsGoalResponse>

    @PUT("move/routine/update/")
    suspend fun updateRoutine(
        @Body request: UpdateRoutineRequest
    ): Response<UpdateRoutineResponse>

    @GET("eat/calories/consumed/")
      suspend fun getConsumedCalories(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<ConsumedCaloriesResponse>

    @GET("eat/protein/consumed/")
     suspend fun getConsumedProtiens(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<ConsumedProteinResponse>


    @GET("eat/carbs/consumed/")
    suspend fun getConsumedCarbs(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<ConsumedCarbsResponse>


    @POST("eat/log_water/")
    fun logWaterIntake(
        @Body request: WaterIntakeRequest
    ): Call<LogWaterResponse>

    @POST("eat/log_weight/")
    fun logWeightIntake(
        @Body request: WeightIntakeRequest
    ): Call<LogWeightResponse>


    @GET("eat/fat/consumed/")
    suspend fun getConsumedFats(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<ConsumedFatResponse>


    @GET("eat/cholesterol/consumed/")
    suspend fun getConsumedCholesterol(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<ConsumedCholesterolResponse>

    @GET("eat/sugar/consumed/")
    suspend fun getConsumedSugar(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<ConsumedSugarResponse>

    @GET("eat/iron/consumed/")
    suspend fun getConsumedIron(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<ConsumedIronResponse>
    @GET("eat/magnesium/consumed/")
    suspend fun getConsumedMagnesium(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<ConsumedMagnesiumResponse>

    @GET("move/steps_detail_view/")
    suspend fun getStepsDetail(
        @Query("user_id") userId: String,
        @Query("source") source: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<HeartRateFitDataResponse>

    @GET("move/fetch_heart_rate_variabililty/")
    suspend fun getHeartRateVariability(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<HeartRateVariabilityResponse>

    @GET("move/fetch_resting_heart_rate/")
    suspend fun getRestingHeartRate(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<RestingHeartRateResponse>

    @GET("move/fetch_heart_rate/")
    suspend fun getHeartRate(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Response<HeartRateResponse>

    @GET("move/fetch-activity-factor")
    suspend fun getActivityFactor(
        @Query("user_id") userId: String,
        @Query("date") date: String,
        @Query("time_range") timeRange: String
    ): Response<ActivityFactorResponse>

    @POST("move/data/fetch_calculate_calories/")
    suspend fun calculateCalories(
        @Body request: CalculateCaloriesRequest
    ): Response<CalculateCaloriesResponse>

    @GET("move/data/user_workouts/frequent/")
    suspend fun getUserFrequentlyWorkouts(
        @Query("user_id") userId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("min_count") minCount: Int? = null
    ): Response<FrequentlyLoggedResponse>

    @GET("eat/landing-page/")
    fun getMealLandingSummary(
        @Query("user_id") userId: String, @Query("date") date: String): Call<EatRightLandingPageDataResponse>

    @GET("sleep/fetch_sleep_stage/")
    fun fetchSleepStage(
        @Query("user_id") userId: String,
        @Query("source") source: String,
        @Query("date") date: String
    ): Call<SleepStageResponse>

    @GET("app/api/quoteOfDay")
    fun quoteOfDay(@Header("Authorization") authToken: String): Call<ThinkQuoteResponse>

    @GET("app/api/mind-audit/q/get-assessment-result")
    fun getAssessmentResult(@Header("Authorization") authToken: String): Call<AssessmentResponse>

    @GET("app/api/affirmationPlaylist")
    fun getAffirmationPlaylist(@Header("Authorization") authToken: String): Call<AffirmationPlaylistResponse>

    @GET("app/api/tools")
    fun fetchToolsList(@Header("Authorization") authToken: String,@Query("userId") userId: String,@Query("filteredKey") filteredKey: String): Call<ToolsResponse>

    @GET("app/api/tools")
    fun fetchToolsListAll(@Header("Authorization") authToken: String,@Query("filteredKey") filteredKey: String): Call<ToolsResponse>

    @POST("app/api/tools")
    fun selectTools(@Header("Authorization") authToken: String, @Body addToolRequest: AddToolRequest,): Call<BaseResponse>

    @GET("app/api/tools")
    fun thinkTools(@Header("Authorization") authToken: String): Call<ToolsGridResponse>

    @GET("app/api/journalNew/journalAnswer")
    fun fetchJournalAnswer(@Header("Authorization") authToken: String,@Query("date") date: String): Call<JournalAnswerResponse>

    @GET("app/api/mindFull")
    fun fetchMindFull(@Header("Authorization") authToken: String,@Query("startDate") startDate: String,
                      @Query("endDate") endDate: String): Call<MindfullResponse>

    @GET("sleep/fetch_sleep_time")
    fun fetchWakeupTime(@Query("user_id") userId: String,
                        @Query("source") source: String): Call<WakeupTimeResponse>

    @POST("sleep/set_nap_log/")
    fun logNap(@Query("user_id") userId: String,
               @Query("source") source: String, @Query("date") date: String, @Body logNapRequest: LogNapRequest): Call<BaseResponse>

    @PUT("sleep/set_wakeup_time/")
    fun updateWakeupTime(@Query("user_id") userId: String, @Query("source") source: String, @Query("record_id") record_id: String,
                         @Query("timer_value") timer_value: String): Call<WakeupTimeResponse>

    @GET("sleep/fetch_sleep_performance_data/")
    fun fetchSleepPerformance(
        @Query("user_id") userId: String,
        @Query("source") source: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Call<SleepPerformanceResponse>

    @GET("sleep/ideal_vs_actual_sleepTime_detail/")
    fun fetchSleepIdealActual(
        @Query("user_id") userId: String,
        @Query("source") source: String,
        @Query("period") period: String
    ): Call<SleepIdealActualResponse>

    @GET("sleep/sleep_consistency_details/")
    fun fetchSleepConsistencyDetail(
        @Query("user_id") userId: String,
        @Query("source") source: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Call<SleepConsistencyResponse>

    @GET("sleep/restorative_sleep_detail/")
    fun fetchSleepRestorativeDetail(
        @Query("user_id") userId: String,
        @Query("source") source: String,
        @Query("period") period: String,
        @Query("date") date: String
    ): Call<RestorativeSleepResponse>

    @GET("app/api/journalNew/recordEmotion")
    fun fetchMoodTrackerPercentage(@Header("Authorization") authToken: String,
        @Query("type") type: String,
        @Query("startDate") startDate: String,
        @Query("endDate") endDate: String
    ): Call<MoodTrackerWeeklyResponse>

    @GET("app/api/journalNew/recordEmotion")
    fun fetchMoodTrackerMonthly(@Header("Authorization") authToken: String, @Query("type") type: String,
                                   @Query("startDate") startDate: String, @Query("endDate") endDate: String
    ): Call<MoodTrackerMonthlyResponse>

    @POST("app/api/journalNew")
    fun addThinkJournalEmoji(@Header("Authorization") authToken: String, @Body addEmojiRequest: AddEmojiRequest): Call<BaseResponse>

    @GET("sleep/landing_page/")
    fun fetchSleepLandingPage(
        @Query("user_id") userId: String,
        @Query("source") source: String,
        @Query("date") date: String,
        @Query("user_preferences") preferences: String,
    ): Call<SleepLandingResponse>

    @GET("move/landing_page/")
    suspend fun getMoveLanding(
        @Query("user_id") userId: String,
        @Query("date") date: String
    ): Response<MoveDashboardResponse>

    @GET("eat/log_weight/")
    suspend fun getLogWeight(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<WeightResponse>

    @GET("eat/water_intake/")
    suspend fun getWaterIntake(
        @Query("user_id") userId: String,
        @Query("period") period: String,
        @Query("date") date: String,
        @Query("skip") skip: Int = 0,
        @Query("limit") limit: Int = 100
    ): Response<WaterIntakeResponse>


    @DELETE("move/data/delete_calories/")
    suspend fun deleteCalorie(
        @Query("calorie_id") calorieId: String,
        @Query("user_id") userId: String
    ): Response<DeleteCalorieResponse>

    @PUT("move/data/update_calories/")
    suspend fun updateCalorie(
        @Query("calorie_id") calorieId: String,
        @Body request: UpdateCalorieRequest
    ): Response<UpdateCalorieResponse>

    @GET("move/routine/")
    suspend fun getMoveRoutine(
        @Query("user_id") userId: String,
       // @Query("provided_date") providedDate: String
    ): Response<WorkoutPlanResponse>

    @DELETE("move/routine/delete/")
    fun deleteRoutines(
        @Query("routine_id") routineId: String,
        @Query("user_id") userId: String
    ): Call<DeleteCaloriesResponse>

    @GET("move/routine/")
    suspend fun getRoutines(
        @Query("user_id") userId: String
    ): Response<WorkoutPlanResponse>

    @POST("move/workout_list/log_routine/")
    suspend fun logRoutine(
        @Body request: LogRoutineRequest
    ): LogRoutineResponse

    @GET("move/landing_page/")
    suspend fun getLandingPageData(
        @Query("user_id") userId: String,
        @Query("date") date: String
    ): Response<FitnessData>

    @POST("move/assign_routine/")
    suspend fun assignRoutine(
        @Query("_id") id: String,
        @Query("user_id") userId: String,
        @Body request: AssignRoutineRequest
    ): Response<AssignRoutineResponse>

    @GET("move/data/get_calories/")
    suspend fun getFetchWorkouts(
        @Query("user_id") userId: String,
        @Query("start_date") startDate: String,
        @Query("end_date") endDate: String,
        @Query("page") page: Int,
        @Query("limit") limit: Int,
        @Query("include_stats") includeStats: Boolean // <-- Added this
    ): Response<WorkoutMoveResponseRoutine>

    @GET("move/fetch_calorie_analysis/")
    suspend fun getFetchCalorieAnalysis(
        @Query("user_id") userId: String,
        @Query("source") source: String,
        @Query("period") period: String
    ): Response<WorkoutMoveMainResponseRoutine>

    @Multipart
    @POST("food/images/analyze/")
    fun uploadFoodFile(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Query("apiKey") apiKey: String
    ): Call<NutritionResponse>

    @Multipart
    @POST("analysis/")
    fun uploadFoodImageFile(
        @Part file: MultipartBody.Part,
        @Part("description") description: RequestBody,
        @Query("apiKey") apiKey: String
    ): Call<NutritionResponse>

    @PUT
    fun analyzeFoodImage(
        @Url url: String,
        @Body request: AnalysisRequest
    ): Call<ScanMealNutritionResponse>
}


