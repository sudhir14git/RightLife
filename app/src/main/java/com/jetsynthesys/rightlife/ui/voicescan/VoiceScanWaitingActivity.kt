package com.jetsynthesys.rightlife.ui.voicescan

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R



class VoiceScanWaitingActivity : BaseActivity() {

    private lateinit var timeTextView: TextView
    private lateinit var tvTopic: TextView
    private var seconds = 3 // Start from 3 seconds
    private val handler: Handler = Handler() // To run code on the UI thread
    private var runnable: Runnable? = null
    private var answerId: String? = null
    private var description: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voicescan_waiting)

        answerId = intent.getStringExtra("answerId")
        description = intent.getStringExtra("description")


        timeTextView = findViewById(R.id.tv_time_left)
        tvTopic = findViewById(R.id.tv_topic_description)

        tvTopic.text = "Be prepared to talk about:\n$description"


        // Create a Runnable to update the time every second
        runnable = object : Runnable {
            override fun run() {
                // Update the TextView with the current time in seconds
                timeTextView.text = seconds.toString()
                seconds-- // Decrease the seconds

                if (seconds >= 0) {
                    // Continue the countdown every second
                    handler.postDelayed(this, 1000)
                } else {
                    // When the countdown finishes, stop the timer
                  /*  val intent = Intent(
                        this@VoiceScanWaitingActivity,
                        VoiceRecorderActivity::class.java
                    )
                    intent.putExtra("answerId", answerId)
                    intent.putExtra("description", description)
                    startActivity(intent)
                    finish()*/

                }
            }
        }


        // Start the countdown timer
        handler.post(runnable as Runnable)
    }

    override fun onDestroy() {
        super.onDestroy()
        runnable?.let { handler.removeCallbacks(it) }  // Stop the handler when the activity is destroyed
    }
}