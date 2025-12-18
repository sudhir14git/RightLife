package com.jetsynthesys.rightlife

import android.app.Application
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.os.bundleOf

import com.facebook.FacebookSdk
import com.facebook.LoggingBehavior
import com.facebook.appevents.AppEventsLogger

import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.jetsynthesys.rightlife.ui.affirmation.ReminderReceiver
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.BuildConfig   // ✅ use app BuildConfig
import kotlin.math.log


class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AnalyticsLogger.init(this)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        FirebaseAnalytics.getInstance(this).setAnalyticsCollectionEnabled(true);
// --- THIS IS THE CONDITIONAL LOGIC ---
        // Use the build config flag to enable or disable Crashlytics data collection.
        // For 'release' builds, BuildConfig.ENABLE_CRASHLYTICS will be true.
        // For 'debug' and 'qa' builds, it will be false.
        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(BuildConfig.ENABLE_CRASHLYTICS)

        // You can add a log to confirm which mode it's in
        if (BuildConfig.ENABLE_CRASHLYTICS) {
            Log.d("AppSetup", "Crashlytics collection is ENABLED.")
        } else {
            Log.d("AppSetup", "Crashlytics collection is DISABLED for this build.")
        }

        // ✅ Enable / disable Crashlytics based on build type
        FirebaseCrashlytics.getInstance()
                .setCrashlyticsCollectionEnabled(BuildConfig.ENABLE_CRASHLYTICS)

        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        // Initialize Meta SDK for install tracking
        initializeMetaSDK()
        ReminderReceiver.ringtone?.stop()


        // debug mode firebase

        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)

// Check if the current build is a debug/QA build
        if (true) {
            // 1. Log the property setting for confirmation
            Log.d("FirebaseSetup", "Setting Firebase Analytics Debug Mode.")

            // 2. The key step: set the debug mode property on the app
            // Setting this property enables DebugView for this app instance.
            val params = Bundle()
            params.putString(FirebaseAnalytics.Param.SOURCE, "debug_build")
            firebaseAnalytics.logEvent("app_debug_mode_enabled", params)

            // Although the previous logEvent often works, this is the explicit
            // way to set the user property that triggers DebugView.
            // However, the previous method usually suffices for simple enabling.

            // You can use a more reliable method by setting the user property:
            firebaseAnalytics.setUserProperty("google_debug", "1")
            // Toggle collection to "wake up" the debug stream
            firebaseAnalytics.setAnalyticsCollectionEnabled(false)
            firebaseAnalytics.setAnalyticsCollectionEnabled(true)

            // Log a specific event to 'ping' the console
            val bundle = Bundle().apply {
                putString("debug_mode", "active")
                putString("tester_name", "QA_Internal")
            }
            firebaseAnalytics.logEvent("debug_ping", bundle)

            Log.d("FirebaseDebug", "Debug mode forced for build.")
        }
        val params = Bundle()
        params.putLong(FirebaseAnalytics.Param.VALUE, 1)
        FirebaseAnalytics.getInstance(this).logEvent("enable_debug_view", params)

        //Toast.makeText(this, "Ping sent to Firebase. Check DebugView!", Toast.LENGTH_SHORT).show()
    }

    /**
     * Initialize Meta SDK for marketing attribution
     * Tracks app installs and opens automatically
     */
  /*  private fun initializeMetaSDK() {
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

            FacebookSdk.setIsDebugEnabled(BuildConfig.DEBUG)
            AppEventsLogger.setPushNotificationsRegistrationId("DEBUG")

            AppEventsLogger.initializeLib(this)
            FacebookSdk.setAutoLogAppEventsEnabled(true)
            FacebookSdk.setAutoInitEnabled(true)
            Log.d("MetaSDK", "Device ID: " + AppEventsLogger.getAnonymousAppDeviceGUID(this))

        } catch (e: Exception) {
            // Log error but don't crash the app
            Log.e("MetaSDK", "Failed to initialize Meta SDK", e)
        }
    }*/
    /*private fun initializeMetaSDK() {
        try {
            // 1. Set configurations BEFORE initialization
            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.setAutoLogAppEventsEnabled(true)
            FacebookSdk.setAdvertiserIDCollectionEnabled(true)

            // 2. Set debug mode for all builds during debugging
            FacebookSdk.setIsDebugEnabled(true) // Force debug mode

            // 3. Initialize the SDK
            FacebookSdk.sdkInitialize(applicationContext)
            FacebookSdk.fullyInitialize()

            // 4. Log initialization
            Log.d("MetaSDK", "Meta SDK v${FacebookSdk.getSdkVersion()} initialized")
            Log.d("MetaSDK", "AutoLogAppEvents: ${FacebookSdk.getAutoLogAppEventsEnabled()}")
            Log.d("MetaSDK", "AutoInit: ${FacebookSdk.getAutoInitEnabled()}")

            // 5. Create logger and log test event
            val logger = AppEventsLogger.newLogger(this)

            // Log test events with parameters
            val params = Bundle().apply {
                putString("test_param", "debug_value")
                putString("app_version", BuildConfig.VERSION_NAME)
                putString("build_type", if (BuildConfig.DEBUG) "debug" else "release")
            }

            logger.logEvent("test_event_debug_mode", params)

            // Also log the automatic activate_app event manually
            logger.logEvent("fb_mobile_activate_app")

            // 6. Get and log debug information
            val debugId = AppEventsLogger.getAnonymousAppDeviceGUID(this)
            Log.d("MetaSDK", "Debug Device ID: $debugId")

            // 7. Force flush events immediately (for debugging)
            //AppEventsLogger.flush()

            Log.d("MetaSDK", "Meta SDK initialization complete")

        } catch (e: Exception) {
            Log.e("MetaSDK", "Failed to initialize Meta SDK", e)
            // Don't crash, but log detailed error
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }*/

    private fun initializeMetaSDK() {
        try {
            FacebookSdk.setAutoInitEnabled(true)
            FacebookSdk.setAutoLogAppEventsEnabled(true)
            FacebookSdk.setAdvertiserIDCollectionEnabled(true)

            // Debug logs on
            FacebookSdk.setIsDebugEnabled(true)
            FacebookSdk.addLoggingBehavior(LoggingBehavior.APP_EVENTS)


//            FacebookSdk.sdkInitialize(applicationContext)
            FacebookSdk.fullyInitialize()

            Log.d("MetaSDK", "Meta SDK v${FacebookSdk.getSdkVersion()} initialized")


        } catch (e: Exception) {
            Log.e("MetaSDK", "Failed to initialize Meta SDK", e)
            FirebaseCrashlytics.getInstance().recordException(e)
        }
    }


    override fun attachBaseContext(base: Context) {
        // Just pass base here; do not wrap yet.
        super.attachBaseContext(base)
    }
}
