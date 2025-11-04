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

        // 4) Put defaults only if caller didn't provide them
        out.putIfAbsent(AnalyticsParam.USER_ID, userId)
        out.putIfAbsent(AnalyticsParam.PLATFORM_TYPE, "android")
        out.putIfAbsent(AnalyticsParam.LOGIN_TYPE, loginType)                 // don’t hardcode "Google"
        out.putIfAbsent(AnalyticsParam.USER_TYPE, if (isSubscribed) "paid" else "free")
        gender?.let { out.putIfAbsent(AnalyticsParam.GENDER, it) }            // avoid !!
        age?.let { out.putIfAbsent(AnalyticsParam.AGE, it) }
        goal?.let { out.putIfAbsent(AnalyticsParam.GOAL, it.lowercase()) }
        subGoal?.let { out.putIfAbsent(AnalyticsParam.SUB_GOAL, it.lowercase()) }
        if (productId.isNotEmpty()) out.putIfAbsent(AnalyticsParam.USER_PLAN, productId)

        // Firebase already timestamps events, but if you want your own key, keep it as Long epoch ms
        out.putIfAbsent(AnalyticsParam.TIMESTAMP, System.currentTimeMillis())

        return out
    }




    /*fun logEvent(context: Context, eventName: String, params: Map<String, Any>? = null) {
        addParams(context, params)
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

    private fun addParams(context: Context, params: Map<String, Any>?): Map<String, Any> {
        val newParams = (params ?: emptyMap()).toMutableMap()
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
        var productId = ""
        sharedPreferenceManager.userProfile?.subscription?.forEach { subscription ->
            if (subscription.status) {
                productId = subscription.productId
            }
        }

        newParams[AnalyticsParam.USER_ID] = sharedPreferenceManager.userId
        newParams[AnalyticsParam.PLATFORM_TYPE] = "android"
        newParams[AnalyticsParam.LOGIN_TYPE] = "Google"
        newParams[AnalyticsParam.USER_TYPE] = if (sharedPreferenceManager.userProfile?.isSubscribed == true) "Paid User" else "free User"
        newParams[AnalyticsParam.GENDER] = sharedPreferenceManager.userProfile?.userdata?.gender!!
        newParams[AnalyticsParam.AGE] = sharedPreferenceManager.userProfile?.userdata?.age!!
        newParams[AnalyticsParam.GOAL] = sharedPreferenceManager.selectedOnboardingModule
        newParams[AnalyticsParam.SUB_GOAL] = sharedPreferenceManager.selectedOnboardingSubModule
        newParams[AnalyticsParam.USER_PLAN] = productId
        newParams[AnalyticsParam.TIMESTAMP] = System.currentTimeMillis()
        return newParams
    }*/
}
