package com.jetsynthesys.rightlife

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
import com.google.firebase.FirebaseApp
import com.jetsynthesys.rightlife.ui.affirmation.ReminderReceiver
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.sondeservices.edge.init.SondeEdgeSdk

class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        AnalyticsLogger.init(this)
        if (FirebaseApp.getApps(this).isEmpty()) {
            FirebaseApp.initializeApp(this)
        }
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        sondeSdk(this)
        ReminderReceiver.ringtone?.stop()
    }
    private fun sondeSdk(context: Context) {
        SondeEdgeSdk.init(
            context,
            BuildConfig.CLIENT_ID_SONDE,
            BuildConfig.CLIENT_SECRET_SONDE,
            object : SondeEdgeSdk.SondeInitCallback {
                override fun onError(error: Exception) {
                    Log.d("onError","error")
                }
                override fun onSuccess() {
                    Log.d("SDK init onSuccess","onSuccess")
                }
            }
        )
    }

    override fun attachBaseContext(base: Context) {
        // Just pass base here; do not wrap yet.
        super.attachBaseContext(base)
    }
}
