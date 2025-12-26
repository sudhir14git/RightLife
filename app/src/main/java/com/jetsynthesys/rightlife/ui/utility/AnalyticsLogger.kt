package com.jetsynthesys.rightlife.ui.utility

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsLogger {

    private var firebaseAnalytics: FirebaseAnalytics? = null

    fun init(context: Context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
        }
    }

    fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        val bundle = Bundle()
        params?.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                is Boolean -> bundle.putBoolean(key, value)
            }
        }
        firebaseAnalytics?.logEvent(eventName, bundle)
    }


    fun logEvent(context: Context, eventName: String, params: Map<String, Any>? = null) {
        val finalParams = addParams(context, params) // <-- use the merged map
        val bundle = Bundle()

        finalParams.forEach { (key, value) ->
            when (value) {
                is String -> bundle.putString(key, value)
                is Int -> bundle.putInt(key, value)
                is Long -> bundle.putLong(key, value)
                is Double -> bundle.putDouble(key, value)
                is Float -> bundle.putFloat(key, value)
                is Boolean -> bundle.putBoolean(key, value)
            }
        }

        firebaseAnalytics?.logEvent(eventName, bundle)
    }

    private fun addParams(context: Context, params: Map<String, Any>?): Map<String, Any> {
        val sp = SharedPreferenceManager.getInstance(context)
        val out = mutableMapOf<String, Any>()

        // 1) Start with caller params so explicit values WIN over defaults later
        if (params != null) out.putAll(params)

        // 2) Derive productId (active subscription)
        val productId = sp.userProfile?.subscription
                ?.firstOrNull { it.status }?.productId.orEmpty()

        // 3) Safe accessors
        val isSubscribed = sp.userProfile?.isSubscribed == true
        val gender = sp.userProfile?.userdata?.gender?.trim()?.lowercase()
        val age = sp.userProfile?.userdata?.age
        val goal = sp.selectedOnboardingModule?.trim()
        val subGoal = sp.selectedOnboardingSubModule?.trim()
        val loginType =sp.userProfile?.userdata?.loginType?.trim()?.lowercase() ?: "google" // if you store it
        val userId = sp.userId
        val userDaysCount = sp.userProfile?.daysCount?:"NA"


        // Device name
        val deviceName = "${android.os.Build.MANUFACTURER} ${android.os.Build.MODEL}".trim()

        // Human-readable plan name from SKU (adjust to your actual mapping)
        fun mapSkuToPlan(sku: String): String = when {
            sku.contains("monthly", ignoreCase = true) -> "monthly"
            sku.contains("year", ignoreCase = true) ||
                    sku.contains("annual", ignoreCase = true) -> "annual"
            sku.isBlank() -> "free"
            else -> "other"
        }


        // 4) Put defaults only if caller didn't provide them
        out.putIfAbsent(AnalyticsParam.DEVICE_NAME, deviceName)                // NEW
        out.putIfAbsent(AnalyticsParam.USER_ID, userId)
        out.putIfAbsent(AnalyticsParam.PLATFORM_TYPE, "android")
        out.putIfAbsent(AnalyticsParam.LOGIN_TYPE, loginType)                 // don’t hardcode "Google"
        out.putIfAbsent(AnalyticsParam.USER_TYPE, "New User")// Old User//if (isSubscribed) "paid" else "free")
        out.putIfAbsent(AnalyticsParam.USER_PLAN, if (isSubscribed) "Premium" else "Free")
        gender?.let { out.putIfAbsent(AnalyticsParam.GENDER, it) }            // avoid !!
        age?.let { out.putIfAbsent(AnalyticsParam.AGE, it) }
        goal?.let { out.putIfAbsent(AnalyticsParam.GOAL, it.lowercase()) }
        subGoal?.let { out.putIfAbsent(AnalyticsParam.SUB_GOAL, it.lowercase()) }
        if (productId.isNotEmpty()) out.putIfAbsent(AnalyticsParam.SUBSCRIPTION_PLAN, productId)

        // Firebase already timestamps events, but if you want your own key, keep it as Long epoch ms
        out.putIfAbsent(AnalyticsParam.TIMESTAMP, System.currentTimeMillis())
        out.putIfAbsent(AnalyticsParam.DAY_FROM_LOGIN, userDaysCount)

        return out
    }




//    /*fun logEvent(context: Context, eventName: String, params: Map<String, Any>? = null) {
//        addParams(context, params)
//        val bundle = Bundle()
//        params?.forEach { (key, value) ->
//            when (value) {
//                is String -> bundle.putString(key, value)
//                is Int -> bundle.putInt(key, value)
//                is Long -> bundle.putLong(key, value)
//                is Double -> bundle.putDouble(key, value)
//                is Float -> bundle.putFloat(key, value)
//                is Boolean -> bundle.putBoolean(key, value)
//            }
//        }
//        firebaseAnalytics?.logEvent(eventName, bundle)
//    }
//
//    private fun addParams(context: Context, params: Map<String, Any>?): Map<String, Any> {
//        val newParams = (params ?: emptyMap()).toMutableMap()
//        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
//        var productId = ""
//        sharedPreferenceManager.userProfile?.subscription?.forEach { subscription ->
//            if (subscription.status) {
//                productId = subscription.productId
//            }
//        }
//
//        newParams[AnalyticsParam.USER_ID] = sharedPreferenceManager.userId
//        newParams[AnalyticsParam.PLATFORM_TYPE] = "android"
//        newParams[AnalyticsParam.LOGIN_TYPE] = "Google"
//        newParams[AnalyticsParam.USER_TYPE] = if (sharedPreferenceManager.userProfile?.isSubscribed == true) "Paid User" else "free User"
//        newParams[AnalyticsParam.GENDER] = sharedPreferenceManager.userProfile?.userdata?.gender!!
//        newParams[AnalyticsParam.AGE] = sharedPreferenceManager.userProfile?.userdata?.age!!
//        newParams[AnalyticsParam.GOAL] = sharedPreferenceManager.selectedOnboardingModule
//        newParams[AnalyticsParam.SUB_GOAL] = sharedPreferenceManager.selectedOnboardingSubModule
//        newParams[AnalyticsParam.USER_PLAN] = productId
//        newParams[AnalyticsParam.TIMESTAMP] = System.currentTimeMillis()
//        return newParams
//    }*/




    /// check this code later

/*
    private const val MAX_KEY_LEN = 40
    private const val MAX_VAL_LEN = 100

    @Synchronized
    fun init(context: Context) {
        if (firebaseAnalytics == null) {
            firebaseAnalytics = FirebaseAnalytics.getInstance(context.applicationContext)
        }
    }

    private fun sanitizeKey(raw: String?): String =
            (raw?.trim().orEmpty()).take(MAX_KEY_LEN)

    private fun sanitizeValue(value: Any?): Any? = when (value) {
        null -> null
        is String -> value.trim().take(MAX_VAL_LEN)
        is Int, is Long, is Double, is Float, is Boolean -> value
        else -> null // ignore unsupported types to avoid ClassCast issues
    }

    */
/** Public, crash-proof logger (always safe) *//*

    fun logEvent(context: Context, eventName: String, params: Map<String, Any>? = null) {
        runCatching {
            val fa = firebaseAnalytics ?: return@runCatching // not initialized yet → skip silently
            val name = sanitizeKey(eventName)
            if (name.isEmpty()) return@runCatching

            val finalParams = addParams(context, params) // merged defaults + caller
            if (finalParams.isEmpty()) {
                // Still log an empty bundle (valid), or return if you prefer
            }

            val bundle = Bundle()
            for ((k, vRaw) in finalParams) {
                val key = sanitizeKey(k)
                val v = sanitizeValue(vRaw) ?: continue
                when (v) {
                    is String -> bundle.putString(key, v)
                    is Int -> bundle.putInt(key, v)
                    is Long -> bundle.putLong(key, v)
                    is Double -> bundle.putDouble(key, v)
                    is Float -> bundle.putFloat(key, v)
                    is Boolean -> bundle.putBoolean(key, v)
                }
            }

            fa.logEvent(name, bundle)
        }.onFailure {
            // Swallow to guarantee no crash; keep a log line for debugging if needed
            // Log.e("AnalyticsLogger", "logEvent failed", it)
        }
    }

    */
/** Deprecated no-context overload — keep but make it safe *//*

    @Deprecated("Use logEvent(context, ...) so auto-params are included")
    fun logEvent(eventName: String, params: Map<String, Any>? = null) {
        runCatching {
            val fa = firebaseAnalytics ?: return@runCatching
            val name = sanitizeKey(eventName)
            if (name.isEmpty()) return@runCatching

            val bundle = Bundle()
            params?.forEach { (k, vRaw) ->
                val key = sanitizeKey(k)
                val v = sanitizeValue(vRaw) ?: return@forEach
                when (v) {
                    is String -> bundle.putString(key, v)
                    is Int -> bundle.putInt(key, v)
                    is Long -> bundle.putLong(key, v)
                    is Double -> bundle.putDouble(key, v)
                    is Float -> bundle.putFloat(key, v)
                    is Boolean -> bundle.putBoolean(key, v)
                }
            }
            fa.logEvent(name, bundle)
        }
    }

    */
/** NPE-safe defaults merger — NO !! anywhere *//*

    private fun addParams(context: Context, params: Map<String, Any>?): Map<String, Any> {
        val out = mutableMapOf<String, Any>()
        // Caller params first → caller overrides defaults
        params?.forEach { (k, v) ->
            val key = sanitizeKey(k)
            val value = sanitizeValue(v)
            if (key.isNotEmpty() && value != null) out[key] = value
        }

        val sp = runCatching { SharedPreferenceManager.getInstance(context) }.getOrNull()
        // Defensive reads (everything nullable)
        val userProfile = sp?.userProfile
        val subscriptions = userProfile?.subscription ?: emptyList()
        val activeSku = subscriptions.firstOrNull { it?.status == true }?.productId.orEmpty()

        val isSubscribed = (userProfile?.isSubscribed == true)
        val gender = userProfile?.userdata?.gender?.trim()?.lowercase()
        val age = userProfile?.userdata?.age
        val goal = sp?.selectedOnboardingModule?.trim()
        val subGoal = sp?.selectedOnboardingSubModule?.trim()
        val loginType = userProfile?.userdata?.loginType?.trim()?.lowercase().orEmpty()
        val userId = sp?.userId.orEmpty()

        // Device name (always safe)
        val manufacturer = android.os.Build.MANUFACTURER?.trim().orEmpty()
        val model = android.os.Build.MODEL?.trim().orEmpty()
        val deviceName = (listOf(manufacturer, model).filter { it.isNotEmpty() }.joinToString(" ")
                .ifEmpty { "unknown" })

        // New vs Old lifecycle (derive from your own flag; default "old")
        val isNewUser = (sp?.isFirstOpen == true)
        val userLifecycle = if (isNewUser) "new" else "old"

        // Human-readable plan
        val subscriptionPlan = when {
            activeSku.contains("monthly", true) -> "monthly"
            activeSku.contains("year", true) || activeSku.contains("annual", true) -> "annual"
            activeSku.isBlank() -> "free"
            else -> "other"
        }

        // Put defaults ONLY if caller didn’t pass them
        if (userId.isNotEmpty()) out.putIfAbsent(AnalyticsParam.USER_ID, userId)
        out.putIfAbsent(AnalyticsParam.PLATFORM_TYPE, "android")
        if (loginType.isNotEmpty()) out.putIfAbsent(AnalyticsParam.LOGIN_TYPE, loginType)
        out.putIfAbsent(AnalyticsParam.USER_TYPE, if (isSubscribed) "paid" else "free")
        out.putIfAbsent(AnalyticsParam.USER_LIFECYCLE, userLifecycle)
        out.putIfAbsent(AnalyticsParam.DEVICE_NAME, deviceName)
        gender?.let { if (it.isNotEmpty()) out.putIfAbsent(AnalyticsParam.GENDER, it) }
        age?.let { out.putIfAbsent(AnalyticsParam.AGE, it) }
        goal?.let { if (it.isNotEmpty()) out.putIfAbsent(AnalyticsParam.GOAL, it.lowercase()) }
        subGoal?.let { if (it.isNotEmpty()) out.putIfAbsent(AnalyticsParam.SUB_GOAL, it.lowercase()) }
        if (activeSku.isNotEmpty()) out.putIfAbsent(AnalyticsParam.USER_PLAN, activeSku)
        out.putIfAbsent(AnalyticsParam.SUBSCRIPTION_PLAN, subscriptionPlan)
        out.putIfAbsent(AnalyticsParam.TIMESTAMP, System.currentTimeMillis())

        return out
    }
*/

}
