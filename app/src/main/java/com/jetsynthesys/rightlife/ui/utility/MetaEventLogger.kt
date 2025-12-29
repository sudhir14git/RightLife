package com.jetsynthesys.rightlife.ui.utility


import android.content.Context
import android.os.Bundle
import android.util.Log
import com.facebook.FacebookSdk
import com.facebook.appevents.AppEventsLogger
import com.jetsynthesys.rightlife.BuildConfig

object MetaEventLogger {

    private const val TAG = "MetaEventLogger"

    // Call anywhere: MetaEventLogger.log(context, "event_name")
    fun log(context: Context?, eventName: String, params: Map<String, Any?>? = null) {
        if (context == null || eventName.isBlank()) return

        try {
            // 🛡️ 1. Ensure Meta SDK is initialized
            if (!isMetaSdkReady()) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "Meta SDK not initialized. Skipping event: $eventName")
                }
                return
            }

            // 🛡️ 2. Always use applicationContext
            val logger = AppEventsLogger.newLogger(context.applicationContext)

            val bundle = Bundle()
            params?.forEach { (key, value) ->
                if (key.isBlank() || value == null) return@forEach
                when (value) {
                    is String -> bundle.putString(key, value)
                    is Int -> bundle.putInt(key, value)
                    is Long -> bundle.putLong(key, value)
                    is Double -> bundle.putDouble(key, value)
                    is Float -> bundle.putFloat(key, value)
                    is Boolean -> bundle.putBoolean(key, value)
                    else -> bundle.putString(key, value.toString())
                }
            }

            logger.logEvent(eventName, bundle)

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Meta event logged: $eventName → $params")
                // AppEventsLogger.flush() // enable temporarily for debugging
            }

        } catch (t: Throwable) {
            // 🛡️ 3. NEVER crash app
            Log.e(TAG, "Meta event failed safely: $eventName", t)
        }
    }

    // For events with valueToSum (e.g. revenue)
    fun logWithValue(
            context: Context?,
            eventName: String,
            valueToSum: Double,
            params: Map<String, Any?>? = null
    ) {
        if (context == null || eventName.isBlank()) return

        try {
            if (!isMetaSdkReady()) {
                if (BuildConfig.DEBUG) {
                    Log.w(TAG, "Meta SDK not initialized. Skipping value event: $eventName")
                }
                return
            }

            val logger = AppEventsLogger.newLogger(context.applicationContext)

            val bundle = Bundle()
            params?.forEach { (k, v) ->
                if (k.isBlank() || v == null) return@forEach
                when (v) {
                    is String -> bundle.putString(k, v)
                    is Int -> bundle.putInt(k, v)
                    is Long -> bundle.putLong(k, v)
                    is Double -> bundle.putDouble(k, v)
                    is Float -> bundle.putFloat(k, v)
                    is Boolean -> bundle.putBoolean(k, v)
                    else -> bundle.putString(k, v.toString())
                }
            }

            logger.logEvent(eventName, valueToSum, bundle)

            if (BuildConfig.DEBUG) {
                Log.d(TAG, "Meta value event logged: $eventName → $valueToSum")
                // AppEventsLogger.flush()
            }

        } catch (t: Throwable) {
            Log.e(TAG, "Meta value event failed safely: $eventName", t)
        }
    }

    // 🔹 Central Meta SDK readiness check
    private fun isMetaSdkReady(): Boolean {
        return try {
            FacebookSdk.isInitialized()
        } catch (t: Throwable) {
            false
        }
    }
}


