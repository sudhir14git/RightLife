package com.jetsynthesys.rightlife

import android.content.Context
import android.content.Intent
import androidx.localbroadcastmanager.content.LocalBroadcastManager

object SyncManager {

    const val ACTION_SYNC_COMPLETED = "ACTION_SYNC_COMPLETED"

    fun completeHealthSync(context: Context) {
        Thread {
            // Simulate sync work
            Thread.sleep(1000)

            notifySyncCompleted(context)
        }.start()
    }

    private fun notifySyncCompleted(context: Context) {
        val intent = Intent(ACTION_SYNC_COMPLETED)
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
    }
}
