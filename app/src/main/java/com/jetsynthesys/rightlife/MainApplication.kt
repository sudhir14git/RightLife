package com.jetsynthesys.rightlife

import android.app.Application
import android.content.Context
import android.util.Log
import androidx.appcompat.app.AppCompatDelegate
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


        ReminderReceiver.ringtone?.stop()
    }


    override fun attachBaseContext(base: Context) {
        // Just pass base here; do not wrap yet.
        super.attachBaseContext(base)
    }
}
