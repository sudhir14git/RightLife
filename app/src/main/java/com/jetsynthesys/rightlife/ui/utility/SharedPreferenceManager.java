package com.jetsynthesys.rightlife.ui.utility;

import android.content.Context;
import android.content.SharedPreferences;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse;
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditAssessmentSaveRequest;
import com.jetsynthesys.rightlife.ui.mindaudit.UserEmotions;
import com.jetsynthesys.rightlife.ui.new_design.pojo.InterestDataList;
import com.jetsynthesys.rightlife.ui.new_design.pojo.LoggedInUser;
import com.jetsynthesys.rightlife.ui.new_design.pojo.ModuleTopic;
import com.jetsynthesys.rightlife.ui.new_design.pojo.OnboardingQuestionRequest;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class SharedPreferenceManager {

    private static final String PREF_NAME = "app_shared_prefs"; // File name for SharedPreferences
    private static SharedPreferenceManager instance;
    private final SharedPreferences sharedPreferences;

    private SharedPreferenceManager(Context context) {
        sharedPreferences = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    // Singleton instance
    public static SharedPreferenceManager getInstance(Context context) {
        if (instance == null) {
            instance = new SharedPreferenceManager(context.getApplicationContext());
        }
        return instance;
    }

    // Method to save the access token
    public void saveAccessToken(String token) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.ACCESS_TOKEN, token);
        editor.apply(); // Apply changes asynchronously
    }

    // Method to retrieve the access token
    public String getAccessToken() {
        return sharedPreferences.getString(SharedPreferenceConstants.ACCESS_TOKEN, "");
    }

    public void saveUserFirstVisit(String visit) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.FIRST_VISIT, visit);
        editor.apply(); // Apply changes asynchronously
    }

    // Method to retrieve the access token
    public String getUserFirstVisit() {
        return sharedPreferences.getString(SharedPreferenceConstants.FIRST_VISIT, "");
    }

    // Method to save the user ID
    public void saveUserId(String userId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.USER_ID, userId);
        editor.apply();
    }

    // Method to retrieve the user ID
    public String getUserId() {
        return sharedPreferences.getString(SharedPreferenceConstants.USER_ID, "");
    }

    public void saveDeviceName(String deviceName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.DEVICE_NAME, deviceName);
        editor.apply();
    }

    // Method to retrieve the user ID
    public String getDeviceName() {
        return sharedPreferences.getString(SharedPreferenceConstants.DEVICE_NAME, "");
    }

    public void saveMoveRightSyncTime(String time) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.SYNC_TIME, time);
        editor.apply();
    }

    // Method to retrieve the user ID
    public String getMoveRightSyncTime() {
        return sharedPreferences.getString(SharedPreferenceConstants.SYNC_TIME, "");
    }

    // Clear the access token and user ID (for example, when logging out)
    public void clearData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SharedPreferenceConstants.ACCESS_TOKEN);
        editor.remove(SharedPreferenceConstants.USER_ID);
        editor.clear();
        editor.apply();
    }

    public void saveUserProfile(UserProfileResponse userProfileResponse) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userProfileResponse);
        prefsEditor.putString(SharedPreferenceConstants.USER_PROFILE, json);
        prefsEditor.apply();
    }

    public UserEmotions getUserEmotions() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SharedPreferenceConstants.USER_EMOTIONS, "");
        UserEmotions obj = gson.fromJson(json, UserEmotions.class);
        return obj;
    }

    public void saveUserEmotions(UserEmotions userEmotions) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(userEmotions);
        prefsEditor.putString(SharedPreferenceConstants.USER_EMOTIONS, json);
        prefsEditor.apply();
    }

    public UserProfileResponse getUserProfile() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SharedPreferenceConstants.USER_PROFILE, "");
        UserProfileResponse obj = gson.fromJson(json, UserProfileResponse.class);
        return obj;
    }

    public void saveVoiceScanAnswerId(String answerId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.VOICE_SCAN_ANSWER_ID, answerId);
        editor.apply();
    }

    public String getVoiceScanAnswerId() {
        return sharedPreferences.getString(SharedPreferenceConstants.VOICE_SCAN_ANSWER_ID, "");
    }

    public void saveOnboardingQuestionAnswer(OnboardingQuestionRequest onboardingQuestionRequest) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(onboardingQuestionRequest);
        prefsEditor.putString(SharedPreferenceConstants.ON_BOARDING_QUESTIONS, json);
        prefsEditor.apply();
    }

    public OnboardingQuestionRequest getOnboardingQuestionRequest() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SharedPreferenceConstants.ON_BOARDING_QUESTIONS, "");
        OnboardingQuestionRequest obj = gson.fromJson(json, OnboardingQuestionRequest.class);
        if (obj == null) {
            obj = new OnboardingQuestionRequest();
        }
        return obj;
    }

    public void clearOnboardingQuestionRequest() {
        sharedPreferences.edit().remove(SharedPreferenceConstants.ON_BOARDING_QUESTIONS).apply();
    }

    public String getSelectedOnboardingModule() {
        return sharedPreferences.getString(SharedPreferenceConstants.ON_BOARDING_SELECTED_MODULE, "");
    }

    public void setSelectedOnboardingModule(String moduleName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.ON_BOARDING_SELECTED_MODULE, moduleName);
        editor.apply();
    }

    public String getSelectedOnboardingSubModule() {
        return sharedPreferences.getString(SharedPreferenceConstants.ON_BOARDING_SELECTED_SUB_MODULE, "");
    }

    public void setSelectedOnboardingSubModule(String moduleName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.ON_BOARDING_SELECTED_SUB_MODULE, moduleName);
        editor.apply();
    }

    public void saveMindAuditRequest(MindAuditAssessmentSaveRequest mindAuditAssessmentSaveRequest) {
        SharedPreferences.Editor prefsEditor = sharedPreferences.edit();
        Gson gson = new Gson();
        String json = gson.toJson(mindAuditAssessmentSaveRequest);
        prefsEditor.putString(SharedPreferenceConstants.MIND_AUDIT_FEELINGS, json);
        prefsEditor.apply();
    }

    public MindAuditAssessmentSaveRequest getMindAuditRequest() {
        Gson gson = new Gson();
        String json = sharedPreferences.getString(SharedPreferenceConstants.MIND_AUDIT_FEELINGS, "");
        MindAuditAssessmentSaveRequest obj = gson.fromJson(json, MindAuditAssessmentSaveRequest.class);
        if (obj == null) {
            obj = new MindAuditAssessmentSaveRequest();
        }
        return obj;
    }

    public void clearMindAuditRequest() {
        sharedPreferences.edit().remove(SharedPreferenceConstants.MIND_AUDIT_FEELINGS).apply();
    }

    public void saveAppMode(String mode) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.APP_MODE, mode);
        editor.apply();
    }

    public String getAppMode() {
        return sharedPreferences.getString(SharedPreferenceConstants.APP_MODE, "System");
    }

    public <LoggedInUser> void setLoggedInUsers(ArrayList<LoggedInUser> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.LOGGED_IN_USER, json);
        editor.apply();
    }

    public ArrayList<LoggedInUser> getLoggedUserList() {
        ArrayList<LoggedInUser> arrayItems = new ArrayList<>();
        String serializedObject = sharedPreferences.getString(SharedPreferenceConstants.LOGGED_IN_USER, null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<LoggedInUser>>() {
            }.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }
        return arrayItems;
    }

    public String getUserName() {
        return sharedPreferences.getString(SharedPreferenceConstants.USERNAME, "");
    }

    public void setUserName(String username) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.USERNAME, username);
        editor.apply();
    }

    public String getDisplayName() {
        return sharedPreferences.getString(SharedPreferenceConstants.DISPLAY_NAME, "");
    }

    public void setDisplayName(String displayName) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.DISPLAY_NAME, displayName);
        editor.apply();
    }

    public String getEmail() {
        return sharedPreferences.getString(SharedPreferenceConstants.USER_EMAIL, "");
    }

    public void setEmail(String email) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.USER_EMAIL, email);
        editor.apply();
    }

    public String getSelectedWellnessFocus() {
        return sharedPreferences.getString(SharedPreferenceConstants.SELECTED_WELLNESS_FOCUS, "");
    }

    public void setSelectedWellnessFocus(String wellnessFocus) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.SELECTED_WELLNESS_FOCUS, wellnessFocus);
        editor.apply();
    }

    public ArrayList<ModuleTopic> getWellnessFocusTopics() {
        ArrayList<ModuleTopic> arrayItems = new ArrayList<>();
        String serializedObject = sharedPreferences.getString(SharedPreferenceConstants.SELECTED_WELLNESS_FOCUS_TOPICS, null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<ModuleTopic>>() {
            }.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }
        return arrayItems;
    }

    public <ModuleTopic> void setWellnessFocusTopics(ArrayList<ModuleTopic> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.SELECTED_WELLNESS_FOCUS_TOPICS, json);
        editor.apply();
    }

    public Boolean getUnLockPower() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.UNLOCK_POWER, false);
    }

    public void setUnLockPower(boolean isUnlock) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.UNLOCK_POWER, isUnlock);
        editor.apply();
    }

    public Boolean getCreateUserName() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.CREATE_USERNAME, false);
    }

    public void setCreateUserName(boolean isCreateUser) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.CREATE_USERNAME, isCreateUser);
        editor.apply();
    }

    public Boolean getThirdFiller() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.THIRD_FILLER, false);
    }

    public void setThirdFiller(boolean isThirdFiller) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.THIRD_FILLER, isThirdFiller);
        editor.apply();
    }

    public Boolean getInterest() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.INTEREST, false);
    }

    public void setInterest(boolean isThirdFiller) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.INTEREST, isThirdFiller);
        editor.apply();
    }

    public ArrayList<InterestDataList> getSavedInterest() {
        ArrayList<InterestDataList> arrayItems = new ArrayList<>();
        String serializedObject = sharedPreferences.getString(SharedPreferenceConstants.SAVED_INTEREST, null);
        if (serializedObject != null) {
            Gson gson = new Gson();
            Type type = new TypeToken<List<InterestDataList>>() {
            }.getType();
            arrayItems = gson.fromJson(serializedObject, type);
        }
        return arrayItems;
    }

    public <InterestDataList> void setSavedInterest(ArrayList<InterestDataList> list) {
        Gson gson = new Gson();
        String json = gson.toJson(list);

        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.SAVED_INTEREST, json);
        editor.apply();
    }

    public Boolean getAllowPersonalization() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.PERSONALIZATION, false);
    }

    public void setAllowPersonalization(boolean isPersonalisation) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.PERSONALIZATION, isPersonalisation);
        editor.apply();
    }

    public Boolean getEnableNotification() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.ENABLE_NOTIFICATION, false);
    }

    public void setEnableNotification(boolean enableNotification) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.ENABLE_NOTIFICATION, enableNotification);
        editor.apply();
    }

    public Boolean getSyncNow() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.SYNC_NOW, false);
    }

    public void setSyncNow(boolean isSyncNow) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.SYNC_NOW, isSyncNow);
        editor.apply();
    }

    public int getCurrentQuestion() {
        return sharedPreferences.getInt(SharedPreferenceConstants.CURRENT_QUESTION, 0);
    }

    public void setCurrentQuestion(int position) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferenceConstants.CURRENT_QUESTION, position);
        editor.apply();
    }

    public Boolean getOnBoardingQuestion() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.ONBOARDING_QUESTION, false);
    }

    public void setOnBoardingQuestion(boolean isOnBoardingQuestion) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.ONBOARDING_QUESTION, isOnBoardingQuestion);
        editor.apply();
    }

    public void clearOnboardingData() {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove(SharedPreferenceConstants.USER_EMAIL).apply();
        editor.remove(SharedPreferenceConstants.USERNAME).apply();
        editor.remove(SharedPreferenceConstants.DISPLAY_NAME).apply();
        editor.remove(SharedPreferenceConstants.SELECTED_WELLNESS_FOCUS).apply();
        editor.remove(SharedPreferenceConstants.SELECTED_WELLNESS_FOCUS_TOPICS).apply();
        editor.remove(SharedPreferenceConstants.UNLOCK_POWER).apply();
        editor.remove(SharedPreferenceConstants.THIRD_FILLER).apply();
        editor.remove(SharedPreferenceConstants.SAVED_INTEREST).apply();
        editor.remove(SharedPreferenceConstants.INTEREST).apply();
        editor.remove(SharedPreferenceConstants.PERSONALIZATION).apply();
        editor.remove(SharedPreferenceConstants.SYNC_NOW).apply();
        editor.remove(SharedPreferenceConstants.CURRENT_QUESTION).apply();
        editor.remove(SharedPreferenceConstants.ONBOARDING_QUESTION).apply();
    }

    public Boolean getFirstTimeUserForAffirmation() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FIRST_TIME_AFFIRMATION, true);
    }

    public void setFirstTimeUserForAffirmation(boolean isUnlock) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FIRST_TIME_AFFIRMATION, isUnlock);
        editor.apply();
    }

    public void saveTooltip(String prefKey, boolean isShowed) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(prefKey, isShowed);
        editor.apply();
    }

    public Boolean isTooltipShowed(String prefKey) {
        return sharedPreferences.getBoolean(prefKey, false);
    }

    public Boolean getFirstTimeUserForSnapMealVideo() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FIRST_TIME_SNAP_MEAL_VIDEO, false);
    }

    public void setFirstTimeUserForSnapMealVideo(boolean isVideoUi) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FIRST_TIME_SNAP_MEAL_VIDEO, isVideoUi);
        editor.apply();
    }

    public Boolean getFirstTimeUserForSnapMealRating() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.FIRST_TIME_SNAP_MEAL_RATING, false);
    }

    public void setFirstTimeUserForSnapMealRating(boolean isVideoUi) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.FIRST_TIME_SNAP_MEAL_RATING, isVideoUi);
        editor.apply();
    }

    public int getMaxCalories() {
        return sharedPreferences.getInt(SharedPreferenceConstants.EAT_RIGHT_MAX_CALORIES, 0);
    }

    public void setMaxCalories(int maxCalories) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferenceConstants.EAT_RIGHT_MAX_CALORIES, maxCalories);
        editor.apply();
    }

    public int getMaxCarbs() {
        return sharedPreferences.getInt(SharedPreferenceConstants.EAT_RIGHT_MAX_CARBS, 0);
    }

    public void setMaxCarbs(int maxCarbs) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferenceConstants.EAT_RIGHT_MAX_CARBS, maxCarbs);
        editor.apply();
    }

    public int getMaxProtein() {
        return sharedPreferences.getInt(SharedPreferenceConstants.EAT_RIGHT_MAX_PROTEIN, 0);
    }

    public void setMaxProtein(int maxProtein) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferenceConstants.EAT_RIGHT_MAX_PROTEIN, maxProtein);
        editor.apply();
    }

    public int getMaxFats() {
        return sharedPreferences.getInt(SharedPreferenceConstants.EAT_RIGHT_MAX_FATS, 0);
    }

    public void setMaxFats(int maxFats) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putInt(SharedPreferenceConstants.EAT_RIGHT_MAX_FATS, maxFats);
        editor.apply();
    }

    public void saveMealCalenderTooltip(String prefKey, boolean isShowed) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(prefKey, isShowed);
        editor.apply();
    }

    public Boolean isMealCalenderTooltipShowed(String prefKey) {
        return sharedPreferences.getBoolean(prefKey, false);
    }

    // Method to save the user ID
    public void saveSnapMealId(String snapMealId) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(SharedPreferenceConstants.SNAP_MEAL_ID, snapMealId);
        editor.apply();
    }

    // Method to retrieve the user ID
    public String getSnapMealId() {
        return sharedPreferences.getString(SharedPreferenceConstants.SNAP_MEAL_ID, "");
    }

    public Boolean getAIReportGeneratedView() {
        return sharedPreferences.getBoolean(SharedPreferenceConstants.AI_REPORT_GENERATED, false);
    }

    public void setAIReportGeneratedView(boolean isReportGenerated) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putBoolean(SharedPreferenceConstants.AI_REPORT_GENERATED, isReportGenerated);
        editor.apply();
    }
}

