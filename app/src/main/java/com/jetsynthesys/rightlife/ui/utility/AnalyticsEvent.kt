package com.jetsynthesys.rightlife.ui.utility

object AnalyticsEvent {

    // Splash and Onboarding
    const val SPLASH_SCREEN_OPEN = "splash_screen_open"
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
    const val VIDEO_WATCHED_10_50 = "video_watched_10_50"
    const val VIDEO_WATCHED_50_90 = "video_watched_50_90"
    const val VIDEO_WATCHED_100 = "video_watched_100"
    const val VIDEO_REPEAT_WATCH = "video_repeat_watch"

    // Audio
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
    const val BREATHING_EXERCISE_STARTED = "breathing_exercise_started"
}
