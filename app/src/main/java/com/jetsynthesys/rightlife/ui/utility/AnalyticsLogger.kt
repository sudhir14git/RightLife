package com.jetsynthesys.rightlife.ui.utility

import android.content.Context
import android.os.Bundle
import com.google.firebase.analytics.FirebaseAnalytics

object AnalyticsLogger {

    private var firebaseAnalytics: FirebaseAnalytics? = null
    val internalUserList  = listOf(
        "24kavitapawar@gmail.com",
        "a28280652@gmail.com",
        "adityatyagi7980@gmail.com",
        "aman.memon@jetsynthesys.com",
        "amanmemon21@gmail.com",
        "amantestingrl@gmail.com",
        "avicciii02@gmail.com",
        "bbvxv9604@gmail.com",
        "bhukkadbhau@gmail.com",
        "bjasprit23@gmail.com",
        "bryanshreshta777@gmail.com",
        "cesarthegoatsheep@gmail.com",
        "checktest272@gmail.com",
        "checktestting@gmail.com",
        "digitalcartels22@gmail.com",
        "goyaltushar578@gmail.com",
        "healthtesting80@gmail.com",
        "himannshukene09@gmail.com",
        "himanshukene@gmail.com",
        "himanshukene03@gmail.com",
        "himanshukene09@gmail.com",
        "himanshusolutions1@gmail.com",
        "hshshxgdhshs@gmail.com",
        "kavita31072023@gmail.com",
        "kaviuser05@gmail.com",
        "kaviuser101@gmail.com",
        "melagames11@gmail.com",
        "melatest03@gmail.com",
        "melatest04@gmail.com",
        "narangjasvin@gmail.com",
        "nikhilshinde0897@gmail.com",
        "girrajtomar10@gmail.com",
        "girraj@rightlife.com",
        "playizzonissues@gmail.com",
        "developer.suhas@gmail.com",
        "bramhi.bhandari@icloud.com",
        "pratikvelani@gmail.com",
        "praveenkumarj298@gmail.com",
        "rightlife.testing@gmail.com",
        "rightlife.testing@icloud.com",
        "rightlifetest01@gmail.com",
        "rightlifetest101@gmail.com",
        "rightlifetest102@gmail.com",
        "rightlifetest105@gmail.com",
        "rightlifetest3000@gmail.com",
        "rightlifetest3001@gmail.com",
        "rightlifetest4000@gmail.com",
        "rightlifetest5000@gmail.com",
        "rightlifetester@gmail.com",
        "righttesting3@gmail.com",
        "rlteamai02@gmail.com",
        "rltest246@gmail.com",
        "rltestuser50@gmail",
        "rluser0510@gmail.com",
        "rluser2025@gmail.com",
        "rluser5060@gmail.com",
        "rluser50709@gmail.com",
        "rluser737@gmail.com",
        "sailormanpopeye067@gmail.com",
        "shailesh06081974@gmail.com",
        "sundarw02@gmail.com",
        "sunny61090@gmail.com",
        "testdev2025dec@gmail.com",
        "testgirraj@gmail.com",
        "testingpayment69@gmail.com",
        "unicornsharma797@gmail.com",
        "xo1.test01@gmail.com",
        "xo1.test05@gmail.com",
        "xo1.test06@gmail.com",
        "yashvir8126@gmail.com",
        "yashvirsingh0505@gmail.com",
        "mamillamadhurii@gmail.com",
        "umeshrane0701@gmail.com",
        "umeshtest2025@gmail.com",
        "umeshsudhakarrane@gmail.com",
        "khyatirane115@gmail.com",
        "singhanurag1234@gmail.com",
        "madhurichinni294@gmail.com",
        "rightlifee@gmail.com",
        "aditm19@icloud.com",
        "kritarthasuvarna@gmail.com",
        "work.kritarth@gmail.com",
        "ngoloh911@gmail.com",
        "umesh.rane@rightlife.com",
        "chavan_pratik@icloud.com",
        "rishabh151096@gmail.com",
        "rishsip15@gmail.com",
        "rishabh@rightlife.com",
        "rishabh.jetsynthesys@gmail.com",
        "rishabh.sipani.min15@itbhu.ac.in",
        "7439302713",
        "8417891653",
        "8888027825",
        "sudhie.joshi@gmail.com",
        "mailsudhirjoshi@gmail.com",
        "justinjaju7@gmail.com",
        "pranjalsoni977@gmail.com",
        "developeranshusinghtech@gmail.com",
        "jetsynthesys.sa@gmail.com",
        "testdemo1920@gmail.com",
        "gurujihumai23@gmail.com",
        "p48527070@gmail.com",
        "9763467743",
        "8788162341",
        "shubhankar@rightlife.com",
        "9325583199",
        "xo1.test03@gmail.com",
        "rightlifeus1@gmail.com",
        "teamstandby52@gmail.com",
        "6374591589",
        "saishyam78@gmail.com",
        "saishyam7897@gmail.com",
        "iamgod261123@gmail.com",
        "sai.shyam@icloud.com",
        "kavita31072023@gamil.com",
        "rajannavani@gmail.com",
        "roshan.g.poojari@gmail.com",
        "amansms95@gmail.com",
        "m.nilanjan@gmail.com",
        "sujaur.rahman@gmail.com",
        "praveenkumarj1298@gmail.com",
        "pratikchavaan@gmail.com",
        "sudhier.joshi@gmail.com",
        "shubhankar.jetsynthesys@gmail.com",
        "9022677945"
    )


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
        val loginType =
            sp.userProfile?.userdata?.loginType?.trim()?.lowercase() ?: "google" // if you store it
        val userId = sp.userId
        //val userDaysCount = sp.userProfile?.userdata?.createdAt
        val userDaysCount = getUserDayCount(
            sp.userProfile?.userdata?.createdAt
        )


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
        out.putIfAbsent(
            AnalyticsParam.LOGIN_TYPE,
            loginType
        )                 // don’t hardcode "Google"
        out.putIfAbsent(
            AnalyticsParam.USER_TYPE,
            "New User"
        )// Old User//if (isSubscribed) "paid" else "free")
        out.putIfAbsent(AnalyticsParam.USER_PLAN, if (isSubscribed) "Premium" else "Free")
        gender?.let { out.putIfAbsent(AnalyticsParam.GENDER, it) }            // avoid !!
        age?.let { out.putIfAbsent(AnalyticsParam.AGE, it) }
        goal?.let { out.putIfAbsent(AnalyticsParam.GOAL, it.lowercase()) }
        subGoal?.let { out.putIfAbsent(AnalyticsParam.SUB_GOAL, it.lowercase()) }
        if (productId.isNotEmpty()) out.putIfAbsent(AnalyticsParam.SUBSCRIPTION_PLAN, productId)

        // Firebase already timestamps events, but if you want your own key, keep it as Long epoch ms
        out.putIfAbsent(AnalyticsParam.TIMESTAMP, System.currentTimeMillis())
        out.putIfAbsent(AnalyticsParam.DAY_FROM_LOGIN, userDaysCount)
        val trafficType =
            if (internalUserList.contains(sp.userProfile.userdata.email) || internalUserList.contains(
                    sp.userProfile.userdata.phoneNumber
                )
            ) "Internal User" else "External User"
        out.putIfAbsent(AnalyticsParam.Traffic_Type, trafficType)

        return out
    }

    fun getUserDayCount(createdAtMillis: Long?): Int {
        if (createdAtMillis == null || createdAtMillis <= 0L) return 0

        val millisInDay = 24 * 60 * 60 * 1000L
        val nowMillis = System.currentTimeMillis()

        val diffMillis = nowMillis - createdAtMillis
        if (diffMillis < 0) return 1   // device clock issue safety

        return (diffMillis / millisInDay).toInt() + 1
    }

}
