package com.jetsynthesys.rightlife.fcm

import android.app.Service

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceConstants
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager

class MyFirebaseMessagingService : FirebaseMessagingService() {

    private val sharedPreferenceManager by lazy {
        SharedPreferenceManager.getInstance(applicationContext)
    }
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "New token: $token")
        // TODO: Send token to your backend server
        sendTokenToServer(token)
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        try {
            val title = remoteMessage.notification?.title
                    ?: remoteMessage.data["title"]
                    ?: getString(R.string.app_name)
            val body = remoteMessage.notification?.body
                    ?: remoteMessage.data["body"]
                    ?: ""
            val data = remoteMessage.data

            Log.d("FCM_MESSAGE", "Title: $title, Body: $body")

            NotificationHelper(applicationContext).showNotification(
                    id = System.currentTimeMillis().toInt(),
                    title = title,
                    body = body,
                    data = data
            )
        } catch (e: Exception) {
            Log.e("FCM_ERROR", "Error handling message", e)
        }
    }

    private fun sendTokenToServer(token: String) {
        // Implement API call to send token to your backend
        Log.d("FCM_TOKEN", "Token should be sent to server: $token")
        sharedPreferenceManager.saveString(SharedPreferenceConstants.FCM_TOKEN,token)

        CommonAPICall.sendTokenToServer(applicationContext, token)
    }




}
