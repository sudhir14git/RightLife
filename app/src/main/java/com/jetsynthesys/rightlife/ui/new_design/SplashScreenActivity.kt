package com.jetsynthesys.rightlife.ui.new_design

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.VideoView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity
import com.jetsynthesys.rightlife.newdashboard.HomeDashboardActivity
import com.jetsynthesys.rightlife.ui.new_design.pojo.LoggedInUser

class SplashScreenActivity : BaseActivity() {

    private lateinit var videoView: VideoView
    private lateinit var rlview1: RelativeLayout
    private lateinit var imgview2: ImageView
    private val SPLASH_DELAY: Long = 3000 // 3 seconds

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setChildContentView(R.layout.activity_splash_screen)
        videoView = findViewById(R.id.videoView)
        rlview1 = findViewById(R.id.rlview1)
        imgview2 = findViewById(R.id.imgview2)

        // Need this Dark Mode selection logic for next phase
        /*val appMode = sharedPreferenceManager.appMode
        if (appMode.equals("System", ignoreCase = true)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        } else if (appMode.equals("Dark", ignoreCase = true)) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }*/


        val authToken = sharedPreferenceManager.accessToken
        // Delay the transition to the next activity to allow the video to end properly
        Handler(Looper.getMainLooper()).postDelayed({
            if (authToken.isEmpty()) {
                val intent = Intent(this, DataControlActivity::class.java)
                startActivity(intent)
                finish()
            } else {
                var email = ""
                email = try {
                    sharedPreferenceManager.userProfile.userdata.email
                } catch (e: NullPointerException) {
                    sharedPreferenceManager.email
                }

                var loggedInUser: LoggedInUser? = null
                for (user in sharedPreferenceManager.loggedUserList) {
                    if (email == user.email) {
                        loggedInUser = user
                    }
                }
                if (loggedInUser?.isOnboardingComplete == true) {
                    val intent = Intent(this, HomeDashboardActivity::class.java)
                    startActivity(intent)
                } else {
                    if (!sharedPreferenceManager.createUserName) {
                        val intent = Intent(this, CreateUsernameActivity::class.java)
                        startActivity(intent)
                    } else if (sharedPreferenceManager.selectedWellnessFocus.isNullOrEmpty()
                        || sharedPreferenceManager.wellnessFocusTopics.isNullOrEmpty()
                        || !sharedPreferenceManager.unLockPower
                        ||!sharedPreferenceManager.thirdFiller
                        || !sharedPreferenceManager.interest) {
                        val intent = Intent(this, WellnessFocusActivity::class.java)
                        startActivity(intent)
                    }else if (!sharedPreferenceManager.allowPersonalization) {
                        val intent = Intent(this, PersonalisationActivity::class.java)
                        startActivity(intent)
                    } else if (!sharedPreferenceManager.onBoardingQuestion) {
                        val intent = Intent(this, OnboardingQuestionnaireActivity::class.java)
                        startActivity(intent)
                    }
                    else if (!sharedPreferenceManager.enableNotification){
                        val intent = Intent(this, EnableNotificationActivity::class.java)
                        startActivity(intent)
                    } else if (!sharedPreferenceManager.syncNow) {
                        val intent = Intent(this, SyncNowActivity::class.java)
                        startActivity(intent)
                    }else{
                        val intent = Intent(this, FreeTrialServiceActivity::class.java)
                        startActivity(intent)
                    }
                }
                finish()

            }
        }, SPLASH_DELAY)

        animateViews()
    }

    private fun animateViews() {
        val view1: View = findViewById(R.id.rlview1)
        val view2: View = findViewById(R.id.imgview2)
        // Fade out view1 and fade in view2
        view1.animate()
            .alpha(0.9f) // Fade out
            .setDuration(2000) // Animation duration in ms
            .setListener(object : AnimatorListenerAdapter() {
                override fun onAnimationEnd(animation: Animator) {
                    view1.visibility = View.GONE // Hide view1 after animation
                    view2.visibility = View.VISIBLE // Show view2 before animation
                    view2.alpha = 0f // Set initial alpha for fade in

                    view2.animate()
                        .alpha(1f) // Fade in
                        .setDuration(1000) // Animation duration in ms
                        .setListener(null)
                }
            })
    }
}