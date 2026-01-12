package com.jetsynthesys.rightlife.ui.utility

object AnalyticsEvent {

    // Splash and Onboarding
    const val SPLASH_SCREEN_OPEN = "SplashScreen_Open"
    const val SPLASH_SCREEN_FIRST_OPEN = "SplashScreen_First_Open"
    const val USER_LOGIN = "user_login"
    const val ACTIVE_USER = "active_user"
    const val USER_RETENTION = "user_retention"
    const val ONBOARDING_COMPLETE = "onboarding_complete"

    // Checklist
    const val CHECKLIST_PROFILE_COMPLETE = "checklist_profile_complete"
    const val CHECKLIST_COMPLETE = "checklist_complete"

    // Scans
    const val FACE_SCAN_COMPLETE = "face_scan_complete"
    const val MEAL_SCAN_COMPLETE = "meal_scan_complete"
    const val MEAL_SCAN_RATING = "meal_scan_rating"

    // Manual Entries
    const val MANUAL_MEAL_LOGGED = "manual_meal_logged"
    const val MANUAL_WORKOUT_LOGGED = "manual_workout_logged"
    const val MANUAL_SLEEP_LOGGED = "manual_sleep_logged"

    // Session Durations
    const val AVG_SESSION_DURATION = "avg_session_duration"
    const val TOTAL_SESSION_DURATION = "total_session_duration"
    const val AVG_REPORT_PAGE_DURATION = "avg_report_page_duration"
    const val TOTAL_REPORT_PAGE_DURATION = "total_report_page_duration"

    // Articles & Videos
    const val ARTICLE_OPENED = "article_opened"
    const val ARTICLE_FINISHED = "article_finished"
    const val VIDEO_OPENED = "video_opened"
    const val VIDEO_WATCHED_PERCENT = "video_watched_percent"
    const val VIDEO_WATCHED_10_50 = "video_watched_10_50"
    const val VIDEO_WATCHED_50_90 = "video_watched_50_90"
    const val VIDEO_WATCHED_100 = "video_watched_100"
    const val VIDEO_REPEAT_WATCH = "video_repeat_watch"

    // Audio
    const val AUDIO_OPENED = "audio_opened"
    const val AUDIO_LISTENED_PERCENT = "audio_listened_percent"
    const val AUDIO_LISTENED_10_50 = "audio_listened_10_50"
    const val AUDIO_LISTENED_50_90 = "audio_listened_50_90"
    const val AUDIO_LISTENED_100 = "audio_listened_100"
    const val AUDIO_REPEAT_LISTEN = "audio_repeat_listen"

    // User Account
    const val USER_SIGN_OUT = "user_signout"
    const val ACCOUNT_DELETED = "account_deleted"
    const val DATA_EXPORT_REQUESTED = "data_export_requested"

    // Feedback
    const val APP_RATING_CLICK = "app_rating_click"
    const val USER_FEEDBACK_CLICK = "user_feedback_click"

    // Purchases
    const val FACE_SCAN_PURCHASE_SINGLE = "face_scan_purchase_single"
    const val FACE_SCAN_PURCHASE_PACK = "face_scan_purchase_pack"
    const val SUBSCRIPTION_MONTHLY_PLAN_PURCHASE = "subscription_monthly_plan_purchase"
    const val SUBSCRIPTION_YEARLY_PLAN_PURCHASE = "subscription_yearly_plan_purchase"

    const val FACE_SCAN_PURCHASE_COMPLETED = "face_scan_purchase_completed"
    const val SUBSCRIPTION_PURCHASE_COMPLETED = "subscription_purchase_completed"

    // Journal & Affirmation
    const val JOURNAL_ENTRY_CREATED = "journal_entry_created"
    const val JOURNAL_ENTRY_USER = "journal_entry_user"
    const val AFFIRMATION_STARTED = "affirmation_started"
    const val AFFIRMATION_ADDED = "affirmation_added"
    const val AFFIRMATION_PLAYLIST_CREATED = "affirmation_playlist_created"
    const val AFFIRMATION_PLAYLIST_PRACTISE = "affirmation_practise"

    // Breathing & Mind
    const val BREATHING_SESSION_STARTED = "breathing_session_started"
    const val BREATHING_SESSION_COMPLETED = "breathing_session_completed"
    const val MIND_AUDIT_STARTED = "mind_audit_started"
    const val MIND_AUDIT_COMPLETED = "mind_audit_completed"
    const val BREATHING_EXERCISE_STARTED = "breathing_exercise_started"

    const val WEARABLE_SYNC_BUTTON_CLICKED = "wearable_sync_button_clicked"
    const val WEARABLE_SYNC_BUTTON_SKIPED = "wearable_sync_button_skiped"

    const val DATA_CONTROL_SCREEN_VISIT = "data_control_screen_visit"
    const val LOGIN_SCREEN_VISIT = "login_screen_visit"
    const val CONTINUE_WITH_GOOGLE_CLICK = "LoginPage_Google_Tap"
    const val CONTINUE_WITH_PHONE_NUMBER = "LoginPage_PhoneNumber_Tap"
    const val CREATE_USER_SCREEN_VISIT = "create_user_screen_visit"

    const val GOAL_SELECTION_VISIT = "goal_selection_visit"
    const val GOAL_SELECTION = "goal_selection"
    const val SUB_GOAL_SELECTION_VISIT = "sub_goal_selection_visit"
    const val SUB_GOAL_SELECTION = "sub_goal_selection"

    const val UNLOCK_POWER_VISIT = "unlock_power_visit"
    const val THREE_TIER_SCREEN_VISIT = "three_tier_screen_visit"
    const val INTEREST_SELECTION_VISIT = "interest_selection_visit"
    const val SAVE_INTEREST = "save_interest"

    const val ALLOW_PERSONALISATION_VISIT = "allow_personalisation_visit"
    const val ALLOW_PERSONALISATION_CLICK = "allow_personalisation_click"
    const val ALLOW_PERSONALISATION_SKIP_CLICK = "allow_personalisation_skip_click"

    const val GENDER_SELECTION_VISIT = "gender_selection_visit"
    const val GENDER_SELECTION = "gender_selection"
    const val GENDER_SELECTION_SKIP_CLICK = "gender_selection_skip_click"

    const val AGE_SELECTION_VISIT = "age_selection_visit"
    const val AGE_SELECTION = "age_selection"
    const val AGE_SELECTION_SKIP = "age_selection_skip"

    const val HEIGHT_SELECTION_VISIT = "height_selection_visit"
    const val HEIGHT_SELECTION = "height_selection"
    const val HEIGHT_SELECTION_SKIP = "height_selection_skip"

    const val WEIGHT_SELECTION_VISIT = "weight_selection_visit"
    const val WEIGHT_SELECTION = "weight_selection"
    const val WEIGHT_SELECTION_SKIP = "weight_selection_skip"

    const val BODY_FAT_SELECTION_VISIT = "body_fat_selection_visit"
    const val BODY_FAT_SELECTION = "body_fat_selection"
    const val BODY_FAT_SELECTION_SKIP = "body_fat_selection_skip"

    const val STRESS_MANAGEMENT_VISIT = "stress_management_visit"
    const val STRESS_MANAGEMENT_SELECTION = "stress_management_selection"
    const val STRESS_MANAGEMENT_SKIP = "stress_management_skip"

    const val ACHIEVE_HEALTH_GOALS_VISIT = "acheive_health_goals_visit"
    const val ACHIEVE_HEALTH_GOALS_SELECTION = "acheive_health_goals_selection"
    const val ACHIEVE_HEALTH_GOALS_SKIP = "acheive_health_goals_skip"

    const val ENABLE_NOTIFICATION_CLOSE = "NotificationPage_Close_Tap"
    const val ENABLE_NOTIFICATION_CLICK = "NotificationPage_EnableNotification_Tap"


    const val SYNC_NOW_VISIT = "sync_now_visit"
    const val SYNC_NOW_CLICK = "sync_now_click"
    const val SKIP_FOR_NOW_CLICK = "skip_for_now_click"

    const val BEGIN_FREE_TRIAL_VISIT = "begin_free_trial_visit"
    const val BEGIN_FREE_TRIAL_CLICK = "begin_free_trial_click"
    const val CRAFTING_PERSONALISE_VISIT = "crafting_personalise_visit"
    const val AWESOME_SCREEN_VISIT = "awesome_screen_visit"

    const val HOME_DASHBOARD_VISIT = "home_dashboard_visit"

    const val HOME_PAGE_FIRST_OPEN = "Home_FirstOpen"

    const val CHECKLIST_STATUS = "checklist_status"
    const val PROFILE_STATUS = "profile_status"
    const val SNAP_MEAL_STATUS = "snap_meal_status"
    const val ER_MR_ASSESSMENT_STATUS = "ER_MR_assessment_status"
    const val TR_SR_ASSESSMENT_STATUS = "TR_SR_assessment_status"
    const val SYNC_DATA_STATUS = "sync_data_status"
    const val FACIAL_SCAN_STATUS = "facial_scan_status"

    const val WHY_CHECKLIST_CLICK = "why_checklist_click"
    const val FINISH_TO_UNLOCK_CLICK = "finish_to_unlock_click"
    const val SUBSCRIBE_RIGHT_LIFE_CLICK = "subscribe_rightlife_click"

    const val LYA_FOOD_LOG_CLICK = "lya_food_log_click"
    const val LYA_ACTIVITY_LOG_CLICK = "lya_activity_log_click"
    const val LYA_SLEEP_LOG_CLICK = "lya_sleep_log_click"
    const val LYA_WEIGHT_LOG_CLICK = "lya_weight_log_click"
    const val LYA_WATER_LOG_CLICK = "lya_water_log_click"

    const val EOS_FACE_SCAN_CLICK = "eos_face_scan_click"
    const val EOS_SNAP_MEAL_CLICK = "eos_snap_meal_click"
    const val EOS_SLEEP_SOUNDS = "eos_sleep_sounds"
    const val EOS_AFFIRMATION_CLICK = "eos_affirmation_click"
    const val EOS_JOURNALING_CLICK = "eos_journaling_click"
    const val EOS_BREATH_WORK_CLICK = "eos_breathwork_click"

    const val MOVE_RIGHT_CLICK = "move_right_click"
    const val EAT_RIGHT_CLICK = "eat_right_click"
    const val SLEEP_RIGHT_CLICK = "sleep_right_click"
    const val THINK_RIGHT_CLICK = "think_right_click"

    const val ALL_HEALTH_DATA_CLICK = "all_health_data_click"
    const val DISCOVER_CLICK = "discover_click"
    const val ER_REPORT_PAGE_OPEN = "ER_Report_PageOpen"
    const val ER_HYDRATION_LOG_CONFIRM_TAP = "ER_Hydration_Log_Confirm_Tap"
    const val ER_LOGWEIGHT_CONTINUE_TAP = "ER_LogWeight_Continue_Tap"
    const val MEALSNAP_RESULTPAGE_FIRSTLOG = "MealSnap_ResultPage_FirstLog"
    const val MEALSNAP_RESULTPAGE_ADDTOLOG = "MealSnap_ResultPage_AddToLog"
    const val MEALSNAP_RATING_SUBMIT = "MealSnap_Rating_Submit"
    const val ER_MEALLOG_LOGYOURMEAL = "ER_MealLog_LogYourMeal"
    const val MR_Workout_AddToLog = "MR_Workout_AddToLog"
    const val MR_Routine_AddToLog = "MR_Routine_AddToLog"
    const val MR_Report_PageOpen = "MR_Report_PageOpen"
    const val SR_LogYourSleep_Yesterday_Save = "SR_LogYourSleep_Yesterday_Save"
    const val SR_Report_PageOpen = "SR_Report_PageOpen"
    const val SR_ReportPage_LogYourSleep_Save = "SR_ReportPage_LogYourSleep_Save"

    // New Phase 1 Events
    const val NAMEPAGE_CONTINUE_TAP = "NamePage_Continue_Tap"

    const val GET_OTP_TAP = "Login_GetOTP_Tap"

    const val VERIFY_OTP_TAP = "Login_OTP_Verify_Tap"

    const val RESEND_OTP_TAP = "Login_OTP_Resend_Tap"

    const val OTP_SUCCESS = "Login_OTP_Success"

    const val OTP_FAILED = "Login_OTP_Failed"
    const val FREETRIALUNlOCKED_POPUP_OPEN = "FreeTrialUnlocked_PopUp_Open"
    const val MyHealth_Dashboard_FirstOpen = "MyHealth_Dashboard_FirstOpen"
    const val RL_AI_Report_PageOpen = "RL_AI_Report_PageOpen"
    const val RL_AI_Report_Rating_Submit = "RL_AI_Report_Rating_Submit"

    // FaceScan Events
    const val FaceScan_ProceedtoScan_Tap = "FaceScan_ProceedtoScan_Tap"
    const val FaceScan_Cam_PageOpen = "FaceScan_Cam_PageOpen"

    // Content Events
    const val Video_Open = "Video_Open"
    const val Audio_Open = "Audio_Open"
    const val Article_Open = "Article_Open"
    const val SeriesEpisode_Open = "SeriesEpisode_Open"

    // purchase events
    const val Subscription_Monthly_Tap = "Subscription_Monthly_Tap"
    const val Subscription_Annual_Tap = "Subscription_Annual_Tap"
    const val Subscription_CancelSubscription_Tap = "Subscription_CancelSubscription_Tap"
    const val Booster_FaceScan12_Tap = "Booster_FaceScan12_Tap"
    const val Booster_FaceScan1_Tap = "Booster_FaceScan1_Tap"
    const val Checkout_Razorpay_Continue = "Checkout_Razorpay_Continue"
    const val Checkout_GooglePlay_Continue = "Checkout_GooglePlay_Continue"

    const val ManageSubs_CancelSubs = "ManageSubs_CancelSubs"
    const val Subs_Monthly_Purchased = "Subs_Monthly_Purchased"
    const val Subs_Annual_Purchased = "Subs_Annual_Purchased"
    const val FaceScan12_Purchased = "FaceScan12_Purchased"
    const val FaceScan1_Purchased = "FaceScan1_Purchased"

    const val RL_AI_Report_Card_Tap = "RL_AI_Report_Card_Tap"
    const val Checklist_MRER_Completed = "Checklist_MRER_Completed"
    const val Checklist_TRSR_Completed = "Checklist_TRSR_Completed"
    const val Checklist_Profile_Completed = "Checklist_Profile_Completed"






    const val ManageSubs_Explore = "ManageSubs_Explore"


    //logoutEvent
    const val Settings_Logout_Confirm = "Settings_Logout_Confirm"
    const val Settings_DataExport_Success = "Settings_DataExport_Success"
    const val DeleteAccount_ExportData_Continue = "DeleteAccount_ExportData_Continue"

    // Affirmation new
    const val TR_AffirmationPlaylist_PageOpen = "TR_AffirmationPlaylist_PageOpen"
    const val TR_AffirmationPlaylist_Completion_PageOpen =
        "TR_AffirmationPlaylist_Completion_PageOpen"
    const val TR_AffirmationPlaylist_Completion =
            "TR_AffirmationPlaylist_Completion"

    const val TR_Report_PageOpen = "TR_Report_PageOpen"
    const val TR_AffirmationPageGen_Open = "TR_AffirmationPageGen_Open"


    // journal Events new
    const val TR_Freeform_Journal_Save_Tap = "TR_Freeform_Journal_Save_Tap"
    const val TR_Grief_Journal_Save_Tap = "TR_Grief_Journal_Save_Tap"
    const val TR_Gratitude_Journal_Save_Tap = "TR_Gratitude_Journal_Save_Tap"
    const val TR_Bullet_Journal_Save_Tap = "TR_Bullet_Journal_Save_Tap"

    // Breathing Events new
    const val TR_CustomBreathing_Completed = "TR_CustomBreathing_Completed"
    const val TR_BoxBreathing_Completed = "TR_BoxBreathing_Completed"
    const val TR_478Breathing_Completed = "TR_478Breathing_Completed"
    const val TR_AlternateNostril_Completed = "TR_AlternateNostril_Completed"

    // Mind Audit Events new
    const val TR_MindAudit_OHQ_Submit_Tap = "TR_MindAudit_OHQ_Submit_Tap"
    const val TR_MindAudit_DASS21_Submit = "TR_MindAudit_DASS21_Submit"
    const val TR_MindAudit_CAS_Submit = "TR_MindAudit_CAS_Submit"
    const val TR_MindAudit_PHQ9_Submit = "TR_MindAudit_PHQ9_Submit"
    const val TR_MindAudit_GAD7_Submit = "TR_MindAudit_GAD7_Submit"

    const val MR_Number_Format_Exception = "mr_number_format_crash"

    const val SR_SleepSound_Player_Opened = "SR_SleepSound_Player_Opened"

    // Sleep Sounds and player


    //new Events 1b - 16, 18, 19, 20
    const val GoalSelection_SaveButton_Tap = "GoalSelection_SaveButton_Tap"
    const val NOTIFICATION_ENABLE_SUCCESS = "Notification_Enable_Success"
    const val Checklist_FirstTime_Open = "Checklist_FirstTime_Open"
    const val Checklist_SyncHealthConnect_Tap = "Checklist_SyncHealthConnect_Tap"

    const val ChallengeCard_Join_Tap = "ChallengeCard_Join_Tap"

    const val Challenge_info_open = "Challenge_info_open"

    const val Checklist_FaceScan_Completed = "Checklist_FaceScan_Completed"
    const val HealthSync_Success = "HealthSync_Success"





    // Challenege Events
    const val ChlCard_Join_Tap = "ChlCard_Join_Tap"
    const val Chl_CompleteChecklist = "Chl_CompleteChecklist"
    const val Chl_DailyScoreCard_Tap = "Chl_DailyScoreCard_Tap"
    const val Chl_ViewLeaderboard_Tap = "Chl_ViewLeaderboard_Tap"
    const val Chl_ReferNow_Tap = "Chl_ReferNow_Tap"
    const val Chl_PageOpen = "Chl_PageOpen"
    const val Chl_EntersGood = "Chl_EntersGood"
    const val Chl_EntersExcellent = "Chl_EntersExcellent"
    const val Chl_EntersChamp = "Chl_EntersChamp"
    const val FreeTrialEnded = "FreeTrialEnded"

    //Extra Events
    const val Chl_OpenApp_Claimed = "Chl_OpenApp_Claimed"
    const val Chl_LogSleep_Claimed = "Chl_LogSleep_Claimed"
    const val Chl_LogMeal_Claimed = "Chl_LogMeal_Claimed"
    const val Chl_LogMove_Claimed = "Chl_LogMove_Claimed"
    const val Chl_Mindful_Claimed = "Chl_Mindful_Claimed"
    const val Chl_FullDayBonus_Claimed = "Chl_FullDayBonus_Claimed"
    const val Chl_BT_HQSleep_Claim = "Chl_BT_HQSleep_Claim"
    const val Chl_BT_BalEat_Claim = "Chl_BT_BalEat_Claim"
    const val Chl_BT_MoveGoal_Claim = "Chl_BT_MoveGoal_Claim"
    const val Chl_BT_Mindful_Claim = "Chl_BT_Mindful_Claim"
    const val Chl_BT_AllBonus_Claim = "Chl_BT_AllBonus_Claim"
}
