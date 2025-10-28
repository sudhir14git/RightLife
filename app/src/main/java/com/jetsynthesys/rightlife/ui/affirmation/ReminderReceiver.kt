package com.jetsynthesys.rightlife.ui.affirmation

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Handler
import android.os.Looper

class ReminderReceiver : BroadcastReceiver() {

    companion object {
        var ringtone: Ringtone? = null
    }

    override fun onReceive(context: Context, intent: Intent) {

        val alarmSound = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
        ringtone = RingtoneManager.getRingtone(context, alarmSound)
        ringtone?.play()

        Handler(Looper.getMainLooper()).postDelayed({
            ringtone?.stop()
        }, 10000)

        // Show notification or perform reminder action
        when (intent.action) {
            "PRACTICE_ALARM_TRIGGERED" ->
                NotificationHelper.showNotification(
                    context,
                    "Right Life Reminder",
                    "It's time for your scheduled affirmation practise!"
                )

            "EAT_ALARM_TRIGGERED" ->
                NotificationHelper.showNotification(
                    context,
                    "Right Life Reminder",
                    "It's time for your scheduled meal!"
                )
        }
        val time = intent.getStringExtra("Time") ?: ""
        NotificationHelper.setReminder(context, intent.action!!, time)
    }
}
