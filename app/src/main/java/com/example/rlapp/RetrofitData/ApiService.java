package com.example.rlapp.RetrofitData;

import com.example.rlapp.apimodel.CheckRegistrationResponse;
import com.example.rlapp.apimodel.LoginRequest;
import com.example.rlapp.apimodel.LoginResponse;
import com.example.rlapp.apimodel.LoginResponseMobile;
import com.example.rlapp.apimodel.SetPasswordRequest;
import com.example.rlapp.apimodel.SignupOtpRequest;
import com.example.rlapp.apimodel.SubmitLoginOtpRequest;
import com.example.rlapp.apimodel.SubmitOtpRequest;
import com.example.rlapp.apimodel.UploadImage;
import com.example.rlapp.apimodel.UserAuditAnswer.UserAnswerRequest;
import com.example.rlapp.apimodel.affirmations.updateAffirmation.AffirmationRequest;
import com.example.rlapp.apimodel.emaillogin.EmailLoginRequest;
import com.example.rlapp.apimodel.emaillogin.EmailOtpRequest;
import com.example.rlapp.apimodel.emaillogin.SubmitEmailOtpRequest;
import com.example.rlapp.apimodel.exploremodules.sleepsounds.SleepAidsRequest;
import com.example.rlapp.apimodel.userdata.Userdata;
import com.example.rlapp.ui.Articles.requestmodels.ArticleLikeRequest;
import com.example.rlapp.ui.NewSleepSounds.newsleepmodel.AddPlaylistResponse;
import com.example.rlapp.ui.NewSleepSounds.newsleepmodel.SleepCategoryResponse;
import com.example.rlapp.ui.NewSleepSounds.newsleepmodel.SleepCategorySoundListResponse;
import com.example.rlapp.ui.NewSleepSounds.userplaylistmodel.SleepSoundPlaylistResponse;
import com.example.rlapp.ui.SubCategoryResponse;
import com.example.rlapp.ui.affirmation.pojo.AffirmationCategoryListResponse;
import com.example.rlapp.ui.affirmation.pojo.AffirmationSelectedCategoryResponse;
import com.example.rlapp.ui.affirmation.pojo.CreateAffirmationPlaylistRequest;
import com.example.rlapp.ui.affirmation.pojo.GetAffirmationPlaylistResponse;
import com.example.rlapp.ui.affirmation.pojo.GetWatchedAffirmationPlaylistResponse;
import com.example.rlapp.ui.affirmation.pojo.WatchAffirmationPlaylistRequest;
import com.example.rlapp.ui.drawermenu.ChangePassword;
import com.example.rlapp.ui.drawermenu.PreferenceAnswer;
import com.example.rlapp.ui.healthcam.HealthCamFacialScanRequest;
import com.example.rlapp.ui.jounal.new_journal.JournalAddTagsRequest;
import com.example.rlapp.ui.jounal.new_journal.JournalDeleteTagRequest;
import com.example.rlapp.ui.jounal.new_journal.JournalListResponse;
import com.example.rlapp.ui.jounal.new_journal.JournalQuestionCreateRequest;
import com.example.rlapp.ui.jounal.new_journal.JournalQuestionsResponse;
import com.example.rlapp.ui.jounal.new_journal.JournalResponse;
import com.example.rlapp.ui.jounal.new_journal.JournalSectionResponse;
import com.example.rlapp.ui.jounal.new_journal.JournalTagsResponse;
import com.example.rlapp.ui.jounal.new_journal.JournalUpdateRequest;
import com.example.rlapp.ui.jounal.new_journal.JournalUpdateTagsRequest;
import com.example.rlapp.ui.mindaudit.MindAuditAssessmentSaveRequest;
import com.example.rlapp.ui.mindaudit.UserEmotions;
import com.example.rlapp.ui.mindaudit.curated.CuratedUserData;
import com.example.rlapp.ui.new_design.pojo.GetInterestResponse;
import com.example.rlapp.ui.new_design.pojo.GoogleLoginTokenResponse;
import com.example.rlapp.ui.new_design.pojo.GoogleSignInRequest;
import com.example.rlapp.ui.new_design.pojo.OnBoardingDataModuleResponse;
import com.example.rlapp.ui.new_design.pojo.OnBoardingModuleResponse;
import com.example.rlapp.ui.new_design.pojo.OnboardingModuleResultRequest;
import com.example.rlapp.ui.new_design.pojo.OnboardingModuleResultResponse;
import com.example.rlapp.ui.new_design.pojo.OnboardingQuestionRequest;
import com.example.rlapp.ui.new_design.pojo.SaveUserInterestRequest;
import com.example.rlapp.ui.new_design.pojo.SaveUserInterestResponse;
import com.example.rlapp.ui.payment.PaymentCardResponse;
import com.example.rlapp.ui.profile_new.pojo.OtpRequest;
import com.example.rlapp.ui.profile_new.pojo.VerifyOtpRequest;
import com.example.rlapp.ui.settings.pojo.FAQResponse;
import com.example.rlapp.ui.settings.pojo.GeneralInformationResponse;
import com.example.rlapp.ui.therledit.EpisodeTrackRequest;
import com.example.rlapp.ui.therledit.FavouriteRequest;
import com.example.rlapp.ui.therledit.StatiticsRequest;
import com.example.rlapp.ui.therledit.ViewCountRequest;
import com.example.rlapp.ui.voicescan.VoiceScanCheckInRequest;
import com.google.gson.JsonElement;

import java.util.Map;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.DELETE;
import retrofit2.http.GET;
import retrofit2.http.HTTP;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.POST;
import retrofit2.http.PUT;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface ApiService {

    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("auth/user/check-registration")
        // Assume the API endpoint is /login
    Call<CheckRegistrationResponse> checkUserRegistration(@Body LoginRequest request); // Send the request body

    @Headers("Content-Type: application/json") // Set content-type as application/json
    @PUT("user/set-password")
        // Assume the API endpoint is /login
    Call<CheckRegistrationResponse> SetEmailPassword(@Header("Authorization") String authToken, @Body SetPasswordRequest request); // Send the request body


    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("auth/mobile/generate-otp?type=signup")
        // Assume the API endpoint is /login
    Call<LoginResponse> generateOtpSignup(@Body SignupOtpRequest request); // Send the request body


    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("auth/mobile/generate-otp?type=login")
        // Assume the API endpoint is /login
    Call<LoginResponse> generateOtpLogin(@Body SignupOtpRequest request); // Send the request body


    //generate otp for Email signup
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("auth/email/generate-otp")
    // Assume the API endpoint is /login
    Call<LoginResponse> generateOtpEmail(@Body EmailOtpRequest request); // Send the request body


    // submit OTP Email
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("auth/email/verify-otp")
    // Assume the API endpoint is /login
    Call<LoginResponseMobile> submitOtpEmail(@Body SubmitEmailOtpRequest request); // Send the request body

    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("user/signup?signupType=phoneNumber")
        // Assume the API endpoint is /login
    Call<LoginResponse> submitOtpSignup(@Body SubmitOtpRequest request); // Send the request body

    // submit OTP Login
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("auth/mobile/login")
    // Assume the API endpoint is /login
    Call<LoginResponseMobile> submitOtpLogin(@Body SubmitLoginOtpRequest request); // Send the request body

    //Home Page
    // submit OTP Login
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("promotions?appId=THINK_RIGHT&position=TOP")
    // Assume the API endpoint is /login
    Call<LoginResponse> getPromotionsList(); // Send the request body

    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("promotions")
        // Assume the API endpoint is /login   //?appId=THINK_RIGHT&position=TOP
    Call<JsonElement> getPromotionList(
            @Header("Authorization") String authToken,
            @Query("appId") String appId,
            @Query("userId") String userId,
            @Query("position") String position
    );

    //ModuleService Pane
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("user")
    // Assume the API endpoint is /login
    Call<JsonElement> getUserDetais(
            @Header("Authorization") String authToken // Dynamic Authorization Header
    );

    //ModuleService Pane
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("service-pane")
    // Assume the API endpoint is /login
    Call<JsonElement> getPromotionList2(
            @Header("Authorization") String authToken // Dynamic Authorization Header
    );

    //Affrimation List
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("affirmation")
    // Assume the API endpoint is /login
    Call<JsonElement> getAffirmationList(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("userId") String userId,
            @Query("isSuggested") boolean isSuggested
    );

    //Affrimation create
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("affirmation")
    // Assume the API endpoint is /login
    Call<ResponseBody> postAffirmation(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Body AffirmationRequest affirmationRequest);


    /*Call<JsonElement> UpdateAffirmationList(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("userId") String userId,
            @Query("isSuggested") boolean isSuggested
    );*/

    //RightkLife Edit List
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("content/top")
    // Assume the API endpoint is /login
    Call<JsonElement> getRightlifeEdit(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("pageType") String pageType
    );


    // Upcoming Event List  -
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("upcomingEvent")
    // Assume the API endpoint is /login
    Call<JsonElement> getUpcomingEvent(
            @Header("Authorization") String authToken // Dynamic Authorization Header
    );


    // Welness PlayList - wellnessPlaylist
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("wellnessPlaylist")
    // Assume the API endpoint is /login
    Call<JsonElement> getWelnessPlaylistold(
            @Header("Authorization") String authToken // Dynamic Authorization Header
    );


    // Live Event List  -
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("event")
    // Assume the API endpoint is /login
    Call<JsonElement> getLiveEvent(
            @Header("Authorization") String authToken,
            @Query("pageType") String pageType
    );


    // Live Event List  -
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("event")
    // Assume the API endpoint is /login
    Call<JsonElement> getUpcomingLiveEvent(
            @Header("Authorization") String authToken,
            @Query("eventType") String eventType,
            @Query("status") String status,
            @Query("pageType") String pageType
    );
    // Curated List

    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("curated")
        // Assume the API endpoint is /login
    Call<JsonElement> getCuratedContent(
            @Header("Authorization") String authToken // Dynamic Authorization Header
    );

    // Module List
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("app/module")
    // Assume the API endpoint is /login
    Call<JsonElement> getmodule(
            @Header("Authorization") String authToken // Dynamic Authorization Header
    );

    // Module List
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("subModule")
    //?moduleId=THINK_RIGHT&type=CATEGORY&categoryId=ygjh----g
    Call<JsonElement> getsubmodule(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("moduleId") String moduleId, @Query("type") String type); //, @Query("categoryId") String categoryId


    // Module List
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("questionaries/list/")
    //?moduleId=THINK_RIGHT&type=CATEGORY&categoryId=ygjh----g
    Call<JsonElement> getsubmoduletest1(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("moduleId") String moduleId);

    // Module List
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("questionaries/list/{Module}")
    //?moduleId=THINK_RIGHT&type=CATEGORY&categoryId=ygjh----g
    Call<JsonElement> getsubmoduletest(
            @Header("Authorization") String authToken, @Path("Module") String category);


    // Module List
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("questionaries/useranswer/{type}")
    //?moduleId=THINK_RIGHT&type=CATEGORY&categoryId=ygjh----g
    Call<JsonElement> postAnswerRequest(
            @Header("Authorization") String authToken, @Path("type") String category, @Body UserAnswerRequest request);


    // get module content
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("app/type/content")
    Call<JsonElement> getContent(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("type") String type,
            @Query("moduleId") String moduleId,

            @Query("forMaster") boolean forMaster,
            @Query("isExist") boolean isExist
    );

    // get module content2
    //https://qa.rightlife.com/api/app/api/content/list?categoryId=THINK_RIGHT_POSITIVE_PSYCHOLOGY&limit=10&skip=0&moduleId=THINK_RIGHT
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("content/list")
    Call<ResponseBody> getContentdetailslist(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("categoryId") String categoryId,
            @Query("limit") int limit,
            @Query("skip") int skip,
            @Query("moduleId") String moduleId
    );

    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("content/list")
    Call<ResponseBody> getContentdetailslistBySubCategory(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("subCategoryId") String subCategoryId,
            @Query("limit") int limit,
            @Query("skip") int skip,
            @Query("moduleId") String moduleId
    );

    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("content/list")
    Call<ResponseBody> getContentdetailslist(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("limit") int limit,
            @Query("skip") int skip,
            @Query("moduleId") String moduleId
    );

    // get filterchip
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("app/type/content")
    Call<ResponseBody> getContentfilters(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("type") String type,
            @Query("moduleId") String moduleId,
            @Query("categoryId") String categoryId,
            @Query("isExist") boolean isExist,
            @Query("forMaster") boolean forMaster
    );


    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("content/{id}")
        // Rl edit details content
    Call<ResponseBody> getRLDetailpage(
            @Header("Authorization") String authToken, @Path("id") String id);


    // more like this content rl Edit
    @GET("content/like")
    Call<ResponseBody> getMoreLikeContent(
            @Header("Authorization") String authToken,
            @Query("contentId") String contentId,
            @Query("skip") int skip,
            @Query("limit") int limit
    );

    //getwellnesshome content
    @GET("content/list")
    Call<JsonElement> getWelnessPlaylist(
            @Header("Authorization") String authToken,
            @Query("contentType") String contentType,
            @Query("pageType") String pageType
    );

    //http://18.159.113.191:8081/app/api/series/64eef214fbed4a26cbd11c22?listEpisodes=true

    // Define the GET request with a dynamic ID path and optional query parameter
    @GET("series/{seriesId}")
    Call<JsonElement> getSeriesWithEpisodes(
            @Header("Authorization") String authToken,
            @Path("seriesId") String seriesId,
            @Query("listEpisodes") boolean listEpisodes
    );

    //Explore details page
    // more like this content rl Edit
    @GET("content/suggestion")
    Call<ResponseBody> getMightLikeContent(
            @Header("Authorization") String authToken,
            @Query("limit") int limit,
            @Query("skip") int skip,
            @Query("appId") String appId
    );

    // Curated/recomended this content
    @GET("content/recommended")
    Call<ResponseBody> getRecommendedLikeContent(
            @Header("Authorization") String authToken,
            @Query("limit") int limit,
            @Query("skip") int skip,
            @Query("appId") String appId
    );

    @Headers("Content-Type: application/json")
    @GET("mind-audit/q/get-all-emotions")
    Call<ResponseBody> getAllEmotions(@Header("Authorization") String authToken);

    @Headers("Content-Type: application/json")
    @POST("mind-audit/q/get-basic-screening-questions")
    Call<ResponseBody> getBasicScreeningQuestions(
            @Header("Authorization") String authToken,
            @Body UserEmotions emotions
    );

    @Headers("Content-Type: application/json")
    @POST("mind-audit/q/get-suggested-assessments")
    Call<ResponseBody> getSuggestedAssessment(
            @Header("Authorization") String authToken,
            @Body UserEmotions emotions
    );

    @Headers("Content-Type: application/json")
    @POST("recommendations")
    Call<ResponseBody> getCuratedAssessment(
            @Header("Authorization") String authToken,
            @Body CuratedUserData curatedUserData
    );

    @Headers("Content-Type: application/json")
    @GET("mind-audit/q/get-assessment-questionnaires")
    Call<ResponseBody> getAssessmentByType(
            @Header("Authorization") String authToken,
            @Query("assessment") String assessment
    );

    @Headers("Content-Type: application/json")
    @GET("mind-audit/q/get-assessment-result")
    Call<ResponseBody> getMindAuditAssessmentResult(
            @Header("Authorization") String authToken,
            @Query("assessment") String assessment
    );

    @Headers("Content-Type: application/json")
    @POST("mind-audit/c/save-assessment")
    Call<ResponseBody> saveMindAuditAssessment(
            @Header("Authorization") String authToken,
            @Body MindAuditAssessmentSaveRequest mindAuditAssessmentSaveRequest
    );

    @Headers("Content-Type: application/json")
    @POST("mind-audit/q/get-assessment-score")
    Call<ResponseBody> getMindAuditAssessmentScore(
            @Header("Authorization") String authToken,
            @Body Map<String, Object> requestData
    );


    @Headers("Content-Type: application/json")
    @GET("user/purchasehistory")
    Call<ResponseBody> getPurchaseHistory(
            @Header("Authorization") String authToken,
            @Query("status") String status,
            @Query("type") String type,
            @Query("skip") int skip,
            @Query("limit") int limit,
            @Query("sortBy") String sortBy,
            @Query("orderBy") String orderBy
    );

    @Headers("Content-Type: application/json")
    @GET("content/favourite")
    Call<ResponseBody> getFavouritesList(
            @Header("Authorization") String authToken,
            @Query("appId") String appId,
            @Query("skip") int skip,
            @Query("limit") int limit
    );


    @Headers("Content-Type: application/json")
    @POST("s3/presigned-url-generate")
    Call<ResponseBody> getPreSignedUrl(
            @Header("Authorization") String authToken,
            @Body UploadImage uploadImage);

    @Headers("Content-Type: application/json")
    @PUT("user")
    Call<ResponseBody> updateUser(
            @Header("Authorization") String authToken,
            @Body Userdata userdata);


    @Headers("Content-Type: application/json")
    @GET("prompts")
    Call<JsonElement> getPreferences(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @PUT("prompts")
    Call<ResponseBody> updatePreference(
            @Header("Authorization") String authToken,
            @Body PreferenceAnswer preferenceAnswer);

    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("sleep-aids")
        // Assume the API endpoint is /login
    Call<ResponseBody> postSleepAids(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Body SleepAidsRequest sleepAidsRequest);


    @Headers("Content-Type: application/json")
    @GET("sleep-aid-category")
    Call<ResponseBody> getSleepAidCategory(@Header("Authorization") String authToken);


    //Affirmation list Screen API
    // Curated/recomended this content
    @GET("content/affirmation")
    Call<ResponseBody> getAffrimationsListQuicklinks(
            @Header("Authorization") String authToken,
            @Query("limit") int limit,
            @Query("skip") int skip,
            @Query("isSuggested") boolean isSuggested
    );


    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("auth/email/login?userType=user")
        // Assume the API endpoint is /login
    Call<LoginResponseMobile> EmailPasswordLogin(@Body EmailLoginRequest request); // Send the request body


    // RL page APIs
    @Headers("Content-Type: application/json")
    @GET("myRLContent")
    Call<ResponseBody> getMyRLContent(@Header("Authorization") String authToken);

    // RL page APIs - health Audit
    @Headers("Content-Type: application/json")
    @GET("first-look-report")
    Call<ResponseBody> getMyRLFirstLookReport(@Header("Authorization") String authToken);

    // RL page APIs continue

    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("continue")
    Call<ResponseBody> getMyRLContinueWatching(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("pageType") String pageType,
            @Query("limit") int limit,
            @Query("skip") int skip

    );

    // RL page APIs continue

    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("continue")
    Call<ResponseBody> getMyRLRecentlyWatched(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("pageType") String pageType,
            @Query("limit") int limit,
            @Query("skip") int skip

    );

    // RL page APIs journal

    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("journal")
    Call<ResponseBody> getMyRLJournal(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("skip") int skip,
            @Query("limit") int limit

    );

    @Headers("Content-Type: application/json")
    @DELETE("journal/{journalId}")
    Call<ResponseBody> deleteMyRLJournal(
            @Header("Authorization") String authToken,
            @Path("journalId") String journalId
    );

    @Headers("Content-Type: application/json")
    @PUT("journal/{journalId}")
    Call<ResponseBody> updateJournal(
            @Header("Authorization") String authToken,
            @Path("journalId") String journalId,
            @Body Map<String, String> requestData);

    // RL page APIs - Health Cam SearchResult
    @Headers("Content-Type: application/json")
    @GET("facial-scan")
    Call<ResponseBody> getMyRLHealthCamResult(@Header("Authorization") String authToken);

    // RL page APIs - mind audit next assessment date
    @Headers("Content-Type: application/json")
    @GET("user/get-mind-audit-details")
    Call<ResponseBody> getMyRLGetMindAuditDate(@Header("Authorization") String authToken);

    // Search Response  - home screen
    @Headers("Content-Type: application/json")
    @GET("app/type/category")
    Call<ResponseBody> getSearchContent(@Header("Authorization") String authToken);


    @Headers("Content-Type: application/json")
    @PUT("common/changepassword")
    Call<ResponseBody> changePassword(
            @Header("Authorization") String authToken,
            @Body ChangePassword changePassword);

    @Headers("Content-Type: application/json")
    @GET("user/referral-code")
    Call<ResponseBody> getUserReferralCode(@Header("Authorization") String authToken);

    @Headers("Content-Type: application/json")
    @GET("search/history")
    Call<ResponseBody> getSearchHistory(@Header("Authorization") String authToken);

    @Headers("Content-Type: application/json")
    @GET("search")
    Call<ResponseBody> searchQuery(
            @Header("Authorization") String authToken,
            @Query("query") String query
    );

    @Headers("Content-Type: application/json")
    @GET("search")
    Call<ResponseBody> searchQuery(
            @Header("Authorization") String authToken,
            @Query("query") String query,
            @Query("modules") String[] modules
    );

    @Headers("Content-Type: application/json")
    @PUT("rightLife/viewCount")
    Call<ResponseBody> updateViewCount(
            @Header("Authorization") String authToken,
            @Body ViewCountRequest viewCountRequest);

    @Headers("Content-Type: application/json")
    @POST("statitics")
    Call<ResponseBody> updateStatiticsRecord(
            @Header("Authorization") String authToken,
            @Body StatiticsRequest statiticsRequest);

    @Headers("Content-Type: application/json")
    @GET("artist/{artistId}")
    Call<ResponseBody> getArtistDetails(
            @Header("Authorization") String authToken,
            @Path("artistId") String artistId
    );


    // create journal
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("journal")
    // Assume the API endpoint is /login
    Call<ResponseBody> createJournal(@Header("Authorization") String authToken, // Dynamic Authorization Header
                                     @Body Map<String, String> requestData);

    @GET("content/contentListByArtistId")
    Call<ResponseBody> getMoreLikeContentByArtistId(
            @Header("Authorization") String authToken,
            @Query("artistId") String artistId,
            @Query("skip") int skip,
            @Query("limit") int limit
    );

    @Headers("Content-Type: application/json")
    @PUT("content/{contentId}/favourite")
    Call<ResponseBody> updateFavourite(
            @Header("Authorization") String authToken,
            @Path("contentId") String contentId,
            @Body FavouriteRequest favouriteRequest);


    @Headers("Content-Type: application/json")
    @POST("check-in/create")
    Call<ResponseBody> voiceScanCheckInCreate(
            @Header("Authorization") String authToken,
            @Body VoiceScanCheckInRequest checkInRequest);

    @Headers("Content-Type: application/json")
    @GET("check-in")
    Call<ResponseBody> getVoiceScanResults(
            @Header("Authorization") String authToken,
            @Query("assessmentId") String answerId,
            @Query("isRecommended") boolean isRecommended,
            @Query("skip") int skip,
            @Query("limit") int limit
    );

    @Headers("Content-Type: application/json")
    @POST("wellnessStreak")
    Call<ResponseBody> postWellnessStreak(
            @Header("Authorization") String authToken,
            @Query("serviceName") String serviceName,
            @Query("status") String status
    );

    @Headers("Content-Type: application/json")
    @PUT("facial-scan")
    Call<ResponseBody> submitHealthCamReport(
            @Header("Authorization") String authToken,
            @Body HealthCamFacialScanRequest scanRequest
    );

    @Headers("Content-Type: application/json")
    @GET("facial-scan")
    Call<ResponseBody> getHealthCamByReportId(
            @Header("Authorization") String authToken,
            @Query("reportId") String reportId
    );

    @Headers("Content-Type: application/json")
    @GET("onboardingModule")
    Call<OnBoardingModuleResponse> getOnboardingModule(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @GET("onboardingDataModule")
    Call<OnBoardingDataModuleResponse> getOnboardingDataModule(
            @Header("Authorization") String authToken,
            @Query("moduleName") String moduleName
    );

    @Headers("Content-Type: application/json")
    @GET("userIntrest")
    Call<GetInterestResponse> getUserInterest(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @POST("onboardingModuleResult")
    Call<OnboardingModuleResultResponse> getOnboardingModuleResult(
            @Header("Authorization") String authToken,
            @Query("moduleName") String moduleName,
            @Body OnboardingModuleResultRequest onboardingModuleResultRequest
    );

    @Headers("Content-Type: application/json")
    @POST("userIntrestDetailSave")
    Call<SaveUserInterestResponse> saveUserInterest(
            @Header("Authorization") String authToken,
            @Body SaveUserInterestRequest saveUserInterestRequest
    );

    @Headers("Content-Type: application/json")
    @POST("questionable")
    Call<SaveUserInterestResponse> submitOnBoardingAnswers(
            @Header("Authorization") String authToken,
            @Body OnboardingQuestionRequest onboardingQuestionRequest
    );

    @Headers({
            "x-forwarded-for: 60.254.127.250",
            "Content-Type: application/json"
    })
    @POST("auth/google/login")
    Call<GoogleLoginTokenResponse> submitGoogleLogin(
            //@Header("Authorization") String authToken,
            @Query("platform") String platform,
            @Body GoogleSignInRequest googleSignInRequest
    );

    @Headers("Content-Type: application/json") // Set content-type as application/json
    @POST("auth/logout")
    Call<JsonElement> LogoutUser(
            @Header("Authorization") String authToken,
            @Body LogoutUserRequest logoutUserRequest
    );

    @Headers("Content-Type: application/json")
    @GET("payment/plan/{type}")
    Call<PaymentCardResponse> getPaymentPlan(
            @Header("Authorization") String authToken,
            @Path("type") String moduleType);


    //article details
    @Headers("Content-Type: application/json")
    @GET("content/{contentId}")
    // Use {contentId} as a path parameter
    Call<JsonElement> getArticleDetails(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Path("contentId") String contentId // Pass contentId as a path parameter

    );

    @POST("content/like")
    Call<ResponseBody> ArticleLikeRequest(@Header("Authorization") String authToken, @Body ArticleLikeRequest request);

    @Headers("Content-Type: application/json")
    @GET("app/type/content")
    Call<SubCategoryResponse> getSubCategoryList(
            @Header("Authorization") String authToken,
            @Query("type") String type,
            @Query("moduleId") String moduleId,
            @Query("categoryId") String categoryId,
            @Query("isExist") boolean isExist

    );

    // get RL page voice scan data // check api with backed is correct or not got it from debug

    @Headers("Content-Type: application/json")
    @GET("check-in")
    Call<ResponseBody> getVoiceScanCheckInData(
            @Header("Authorization") String authToken,
            @Query("isRecommended") boolean isRecommended,
            @Query("skip") int skip,
            @Query("limit") int limit
    );


    @Headers("Content-Type: application/json")
    @POST("rightEpisodeTrack")
    Call<ResponseBody> trackEpisode(
            @Header("Authorization") String authToken,
            @Body EpisodeTrackRequest request);

    @Headers("Content-Type: application/json")
    @GET("user/get-scan-past-report")
    Call<ResponseBody> getScanPastReport(
            @Header("Authorization") String authToken,
            @Query("scanType") String scanType
    );

    // API to Get single report for report id
    @Headers("Content-Type: application/json")
    @GET("facial-scan")
    Call<ResponseBody> getFacialScanReport(
            @Header("Authorization") String authToken,
            @Query("reportId") String reportId
    );


    // Sleep Sound APIs
    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("sleep-aids")
    Call<ResponseBody> getSleepSoundsList(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Query("limit") int limit,
            @Query("skip") int skip

    );


    @Headers("Content-Type: application/json") // Set content-type as application/json
    @PUT("promotions/viewCount")
        // for update banner view count on home
    Call<ResponseBody> UpdateBannerViewCount(
            @Header("Authorization") String authToken, // Dynamic Authorization Header
            @Body ViewCountRequest request);


    @Headers("Content-Type: application/json") // Set content-type as application/json
    @GET("content/{id}")
        // Thought of the details content
    Call<ResponseBody> getContentDetailpage(
            @Header("Authorization") String authToken, @Path("id") String id);

    @Headers("Content-Type: application/json")
    @GET("catagory")
    Call<AffirmationCategoryListResponse> getAffirmationCategoryList(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @GET("affirmationCatagory")
    Call<AffirmationSelectedCategoryResponse> getAffirmationSelectedCategoryData(
            @Header("Authorization") String authToken,
            @Query("id") String id
    );

    @Headers("Content-Type: application/json")
    @GET("affirmationPlaylist")
    Call<GetAffirmationPlaylistResponse> getAffirmationPlaylist(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @POST("affirmationPlaylist")
    Call<ResponseBody> createAffirmationPlaylist(
            @Header("Authorization") String authToken,
            @Body CreateAffirmationPlaylistRequest affirmationPlaylistRequest
    );

    // API TO get series episode detail
    // Define the GET request with a dynamic ID path and optional query parameter
    @GET("series/{seriesId}/episode/{episodeId}")
    Call<ResponseBody> getSeriesEpisodesDetails(
            @Header("Authorization") String authToken,
            @Path("seriesId") String seriesId,
            @Path("episodeId") String episodeId

    );

    @Headers("Content-Type: application/json")
    @POST("affirmationPlaylist/playlistWatch")
    Call<ResponseBody> updateAffirmationPlaylistWatch(
            @Header("Authorization") String authToken,
            @Body WatchAffirmationPlaylistRequest watchAffirmationPlaylistRequest
    );

    @Headers("Content-Type: application/json")
    @GET("affirmationPlaylist/playlistWatch")
    Call<GetWatchedAffirmationPlaylistResponse> getWatchedAffirmationPlaylist(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @GET("journalNew")
    Call<JournalResponse> getJournals(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @GET("journalNew")
    Call<JournalSectionResponse> getJournalSections(
            @Header("Authorization") String authToken,
            @Query("type") String type,
            @Query("id") String id
    );

    @Headers("Content-Type: application/json")
    @GET("journalNew")
    Call<JournalQuestionsResponse> getJournalQuestions(
            @Header("Authorization") String authToken,
            @Query("type") String type,
            @Query("id") String id
    );

    @Headers("Content-Type: application/json")
    @GET("journalNew/journalTag")
    Call<JournalTagsResponse> getJournalTags(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @GET("journalNew/userJournalTag")
    Call<JournalTagsResponse> getUserJournalTags(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @POST("journalNew/userJournalTag")
    Call<ResponseBody> addJournalTag(
            @Header("Authorization") String authToken,
            @Body JournalAddTagsRequest journalAddTagsRequest
    );

    @Headers("Content-Type: application/json")
    @PUT("journalNew/userJournalTag/Tag")
    Call<ResponseBody> updateJournalTag(
            @Header("Authorization") String authToken,
            @Body JournalUpdateTagsRequest journalUpdateTagsRequest
    );

    @Headers("Content-Type: application/json")
    @HTTP(method = "DELETE", path = "journalNew/userJournalTag/Tag", hasBody = true)
    Call<ResponseBody> deleteJournalTag(
            @Header("Authorization") String authToken,
            @Body JournalDeleteTagRequest journalDeleteTagRequest
    );

    @Headers("Content-Type: application/json")
    @POST("journalNew")
    Call<ResponseBody> createJournal(
            @Header("Authorization") String authToken,
            @Body JournalQuestionCreateRequest journalQuestionCreateRequest
    );

    @Headers("Content-Type: application/json")
    @GET("journalNew/journalAnswer")
    Call<JournalListResponse> getJournalList(
            @Header("Authorization") String authToken,
            @Query("date") String date
    );

    @DELETE("journalNew/{id}")
    Call<ResponseBody> deleteJournalEntry(
            @Header("Authorization") String authToken,
            @Path("id") String journalId
    );

    @PUT("journalNew/{id}")
    Call<ResponseBody> updateJournalEntry(
            @Header("Authorization") String authToken,
            @Path("id") String journalId,
            @Body JournalUpdateRequest request
    );

    @Headers("Content-Type: application/json")
    @GET("faq")
    Call<FAQResponse> getFAQData(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @GET("termsPrivacy")
    Call<GeneralInformationResponse> getGeneralInformation(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @GET("sleepCatagory")
    Call<SleepCategoryResponse> getSleepCategories(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @GET("sleepSound")
    Call<SleepCategorySoundListResponse> getSleepSoundsById(
            @Header("Authorization") String authToken,
            @Query("id") String categoryId,
            @Query("skip") int skip,
            @Query("limit") int limit,
            @Query("type") String type
    );

    @Headers("Content-Type: application/json")
    @POST("sleepSound/sleepSoundPlaylist")
    Call<AddPlaylistResponse> addToPlaylist(
            @Header("Authorization") String authToken,
            @Query("id") String songId
    );

    @Headers("Content-Type: application/json")
    @DELETE("sleepSound/sleepSoundPlaylist/{playlistId}")
    Call<AddPlaylistResponse> removeFromPlaylist(
            @Header("Authorization") String authToken,
            @Path("playlistId") String playlistId
    );

    // get user playlist
    @Headers("Content-Type: application/json")
    @GET("sleepSound/sleepSoundPlaylist")
    Call<SleepSoundPlaylistResponse> getUserCreatedPlaylist(
            @Header("Authorization") String authToken
    );

    @Headers("Content-Type: application/json")
    @POST("auth/mobile/generate-otp?type=changePhoneNumber")
    Call<ResponseBody> generateOtpForPhoneNumber(
            @Header("Authorization") String authToken,
            @Body OtpRequest otpRequest
    );

    @Headers("Content-Type: application/json")
    @POST("user/verifyPhoneNumber?actionType=verifyNewPhoneNumber")
    Call<ResponseBody> verifyOtpForPhoneNumber(
            @Header("Authorization") String authToken,
            @Body VerifyOtpRequest verifyOtpRequest
    );


}


//private static final String BASE_URL = "https://qa.rightlife.com/api/app/api/"; // Your API URL