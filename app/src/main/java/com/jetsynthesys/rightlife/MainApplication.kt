package com.jetsynthesys.rightlife

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.facebook.appevents.AppEventsLogger
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jetsynthesys.rightlife.ui.affirmation.ReminderReceiver
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        // ---------- App Theme ----------
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        CoroutineScope(Dispatchers.Default).launch {
            initHeavySdks()
        }

        // ---------- Cleanup ----------
        ReminderReceiver.ringtone?.stop()
    }

    private fun initHeavySdks() {
        // ---------- Analytics ----------
        AnalyticsLogger.init(this)

        // ---------- Firebase ----------
        initFirebase()

        // ---------- Meta / Facebook SDK ----------
        initializeMetaSDK()
    }

    /**
     * Firebase initialization (Analytics + Crashlytics)
     */
    private fun initFirebase() {
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }

        // Enable analytics always
        FirebaseAnalytics.getInstance(this)
            .setAnalyticsCollectionEnabled(true)

        // Enable Crashlytics ONLY for allowed builds
        FirebaseCrashlytics.getInstance().isCrashlyticsCollectionEnabled =
            BuildConfig.ENABLE_CRASHLYTICS

        if (BuildConfig.ENABLE_CRASHLYTICS) {
            Log.d("AppSetup", "Crashlytics ENABLED")
        } else {
            Log.d("AppSetup", "Crashlytics DISABLED (Debug / QA)")
        }

        // ---------------- DEBUG ONLY ----------------
        // Enable Firebase DebugView ONLY in debug builds
        if (BuildConfig.DEBUG) {
            enableFirebaseDebugMode()
        }
        // --------------------------------------------
    }

    /**
     * DEBUG ONLY
     * Enables Firebase DebugView for QA & development
     */
    private fun enableFirebaseDebugMode() {
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)

        Log.d("FirebaseDebug", "Firebase DebugView enabled")

        firebaseAnalytics.setUserProperty("google_debug", "1")

        // Wake up debug stream
        firebaseAnalytics.setAnalyticsCollectionEnabled(false)
        firebaseAnalytics.setAnalyticsCollectionEnabled(true)

        val bundle = Bundle().apply {
            putString("debug_mode", "active")
        }
        firebaseAnalytics.logEvent("debug_ping", bundle)
    }

    /**
     * Meta (Facebook) SDK initialization
     * Safe for production
     */
    private fun initializeMetaSDK() {
        try {
            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.setAutoLogAppEventsEnabled(true)
            FacebookSdk.setAdvertiserIDCollectionEnabled(true)

            // ---------------- DEBUG ONLY ----------------
            if (BuildConfig.DEBUG) {
                FacebookSdk.setIsDebugEnabled(true)
                FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)
            }
            // --------------------------------------------

            FacebookSdk.fullyInitialize()
            AppEventsLogger.activateApp(this)

            Log.d("MetaSDK", "Meta SDK initialized: ${FacebookSdk.getSdkVersion()}")

        } catch (e: Exception) {
            Log.e("MetaSDK", "Meta SDK init failed", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }

    override fun attachBaseContext(base: Context) {
        super.attachBaseContext(base)
    }
}
