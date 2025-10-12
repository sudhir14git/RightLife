package com.jetsynthesys.rightlife.ui.new_design

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.new_design.pojo.LoggedInUser
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam

class OnboardingFinalActivity : BaseActivity() {

    private lateinit var imageFinalOnboarding: ImageView
    private lateinit var imageRefresh: ImageView
    private lateinit var textView1: TextView
    private lateinit var textView2: TextView
    private lateinit var tvRefresh: TextView

    private var currentRotation = 0f // Keeps track of total rotation

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_onboarding_final)

        imageFinalOnboarding = findViewById(R.id.img_final_onboarding)
        imageRefresh = findViewById(R.id.img_refresh)
        textView1 = findViewById(R.id.textview1)
        textView2 = findViewById(R.id.textview2)
        tvRefresh = findViewById(R.id.tv_refresh)

        val loggedInUsers = sharedPreferenceManager.loggedUserList

        var loggedInUser: LoggedInUser? = null
        val iterator = loggedInUsers.iterator()
        while (iterator.hasNext()) {
            val user = iterator.next()
            if (sharedPreferenceManager.email == user.email) {
                iterator.remove() // Safe removal
                user.isOnboardingComplete = true
                loggedInUser = user
            }
        }

        if (loggedInUser != null) {
            loggedInUsers.add(loggedInUser)
            sharedPreferenceManager.setLoggedInUsers(loggedInUsers)
        }
        sharedPreferenceManager.clearOnboardingData()

        AnalyticsLogger.logEvent(
            AnalyticsEvent.CRAFTING_PERSONALISE_VISIT,
            mapOf(
                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule,
            )
        )

        val handler = Handler(Looper.getMainLooper())
        handler.postDelayed({
            changeUI(0)
            handler.postDelayed({
                changeUI(1)
                handler.postDelayed({
                    changeUI(2)
                    //startActivity(Intent(this@OnboardingFinalActivity, WelcomeActivity::class.java))
                    handler.postDelayed({
                        changeUI(3)
                        startActivity(Intent(this@OnboardingFinalActivity, WelcomeActivity::class.java))
                    }, 1000)
                }, 1000)
            }, 1000)
        }, 1000)

    }

  /*  private fun changeUI(position: Int) {
        when (position) {
            1 -> {
                imageRefresh.setImageResource(R.drawable.icon_refresh50)
                tvRefresh.text = "50 %"
            }

            2 -> {
                imageRefresh.setImageResource(R.drawable.icon_refresh70)
                tvRefresh.text = "70 %"
                imageFinalOnboarding.setImageResource(R.drawable.final_onboarding3)
            }

            3 -> {
                imageRefresh.setImageResource(R.drawable.icon_refresh70)
                tvRefresh.text = "100 %"
                imageFinalOnboarding.setImageResource(R.drawable.final_onboarding4)
            }

        }

    }*/



    private var totalRotation = 0f // Keeps total cumulative rotation

    private fun changeUI(position: Int) {
        // Increment total rotation by 90° every time
        totalRotation += 90f

        // Animate always clockwise
        imageRefresh.animate()
            .rotation(totalRotation)
            .setDuration(300)
            .setInterpolator(LinearInterpolator()) // Optional: smooth consistent speed
            .start()

        // Update text or other UI
        when (position) {
            0 -> {
                tvRefresh.text = "20 %"
                imageFinalOnboarding.setImageResource(R.drawable.final_onboarding1)
            }
            1 -> {
                tvRefresh.text = "50 %"
                imageFinalOnboarding.setImageResource(R.drawable.final_onboarding2)
            }
            2 -> {
                tvRefresh.text = "70 %"
                imageFinalOnboarding.setImageResource(R.drawable.final_onboarding3)
            }
            3 -> {
                tvRefresh.text = "100 %"
                imageFinalOnboarding.setImageResource(R.drawable.final_onboarding4)
            }
        }
    }


}