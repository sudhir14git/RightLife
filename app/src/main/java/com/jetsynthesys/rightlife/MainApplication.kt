package com.jetsynthesys.rightlife

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.facebook.BuildConfig
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.FirebaseApp
import com.jetsynthesys.rightlife.ui.affirmation.ReminderReceiver
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger


class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AnalyticsLogger.init(this)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initialize Meta SDK for install tracking
        initializeMetaSDK()
        ReminderReceiver.ringtone?.stop()
    }

    /**
     * Initialize Meta SDK for marketing attribution
     * Tracks app installs and opens automatically
     */
    private fun initializeMetaSDK() {
        try {
            // Initialize Facebook SDK
            FacebookSdk.sdkInitialize(applicationContext)

            // Enable debug mode only in debug builds
            FacebookSdk.setIsDebugEnabled(BuildConfig.DEBUG)

            // Log initialization success
            Log.d("MetaSDK", "Meta SDK initialized successfully")

            // Optional: Log app activation event manually
            val logger = AppEventsLogger.newLogger(this)
            logger.logEvent("fb_mobile_activate_app")

        } catch (e: Exception) {
            // Log error but don't crash the app
            Log.e("MetaSDK", "Failed to initialize Meta SDK", e)
        }
    }


    override fun attachBaseContext(base: Context) {
        // Just pass base here; do not wrap yet.
        super.attachBaseContext(base)
    }
}
