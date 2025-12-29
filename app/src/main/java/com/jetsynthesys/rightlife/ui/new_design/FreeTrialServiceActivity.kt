package com.jetsynthesys.rightlife.ui.new_design

import android.content.Intent
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Html
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityFreeTrialBinding
import com.jetsynthesys.rightlife.ui.CommonResponse
import com.jetsynthesys.rightlife.ui.settings.GeneralInformationActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.Utils
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response


class FreeTrialServiceActivity : BaseActivity() {
    private lateinit var binding: ActivityFreeTrialBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFreeTrialBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        AnalyticsLogger.logEvent(
            AnalyticsEvent.BEGIN_FREE_TRIAL_VISIT,
            mapOf(
                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule,
            )
        )

        val combinedHtmlText =
            "<p><b>Premium Features</b></p>" + "<p><b><img src='heart_icon' width='20' height='20' align='texttop'/> MyHealth</b></p>" + "<p><b>Face Scan:</b></p>" + "<p>Measure vitals like BP, stress, and heart health in seconds.</p>" + "<p>Wearable Sync & Analysis</p>" + "<p>Consolidate and analyse all your wearable data in one place.</p>" + "<p><b><img src='thinkright_icon' width='20' height='20' align='texttop'/> ThinkRight:</b></p>" + "<p>Mindfulness Minutes</p>" + "<p>Log your meditation and relaxation time for better mental well-being.</p>" + "<p>Mood Tracker</p>" + "<p>Record daily moods and recognise emotional trends.</p>" + "<p>Guided Journaling</p>" + "<p>Reflect and express with structured journaling options.</p>" + "<p>Affirmations</p>" + "<p>Boost motivation with daily positive affirmations.</p>" + "<p>Breathing Techniques</p>" + "<p>Practice four guided breathing exercises for stress relief.</p>" + "<p>Inspirational Quotes</p>" + "<p>Receive daily doses of motivation and wisdom.</p>" + "<p>Mind Audits</p>" + "<p>Assess mental well-being with standardised evaluations for stress, depression, and more.</p>" + "<p><b><img src='moveright_icon' width='20' height='20' align='texttop'/> MoveRight:</b></p>" + "<p>Calorie Analysis</p>" + "<p>Understand whether you're burning or consuming more calories daily.</p>" + "<p>Workout HR Analysis</p>" + "<p>Track heart rate performance during workouts for better training.</p>" + "<p>Step Tracker</p>" + "<p>Keep an eye on your daily step count to maintain activity levels.</p>" + "<p>Vitals Monitoring</p>" + "<p>Access key health metrics like HRV, RHR, Active Burn, and Average HR.</p>" + "<p><b><img src='eatright_icon' width='20' height='20' align='texttop'/> EatRight:</b></p>" + "<p>Daily Macro & Micro Nutrient Analysis</p>" + "<p>Get a complete breakdown of your daily intake to ensure balanced nutrition.</p>" + "<p>Personalised Meal Plans</p>" + "<p>Enjoy customised meal plans tailored to your goals and preferences.</p>" + "<p>Recipes</p>" + "<p>Explore thousands of healthy, goal-aligned recipes.</p>" + "<p>Snap-to-Log Calories</p>" + "<p>Instantly log meals by snapping a photo—fast, easy, and accurate.</p>" + "<p>Meal Logging</p>" + "<p>Track your food intake effortlessly to stay on top of your nutrition.</p>" + "<p>Meal Insights</p>" + "<p>Compare logged meals with your nutritional targets for better choices.</p>" + "<p>Hydration Tracker</p>" + "<p>Stay hydrated with daily water intake tracking.</p>" + "<p>Weight Tracker</p>" + "<p>Monitor weight trends and progress over time.</p>" + "<p><b><img src='sleepright_icon' width='20' height='20' align='texttop'/> SleepRight:</b></p>" + "<p>Sleep Music, Stories & Sounds</p>" + "<p>Relax and drift off with soothing audio aids.</p>" + "<p>Daily Ideal Sleep Needed</p>" + "<p>Get dynamic sleep duration recommendations based on daily activity.</p>" + "<p>Sleep Consistency Tracking</p>" + "<p>Monitor bedtime regularity and improve sleep patterns.</p>" + "<p>Sleep Stage Analysis</p>" + "<p>Understand your sleep cycles, from deep to REM sleep.</p>" + "<p><b>FAQs</b></p>" + "<p><b>1. What features do I get access to during my free trial?</b></p>" + "<p>You get full access to all our Premium Features listed above, including MyHealth, ThinkRight, MoveRight, EatRight, and SleepRight, for 7 days.</p>" + "<p><b>2. Do I need to add payment details to start my free trial?</b></p>" + "<p>No, you can start your 7-day free trial without entering any payment details.</p>" + "<p><b>3. Will I be charged automatically after the free trial ends?</b></p>" + "<p>No, you will not be charged automatically. You can choose to subscribe to a monthly or annual plan to continue enjoying premium features after the free trial period.</p>" + "<p><b>4. How do I track my free trial period and know when it ends?</b></p>" + "<p>A countdown bar at the top of the app keeps you updated on your trial period. You'll also receive notifications and alerts to remind you.</p>" + "<p><b>5. Can I cancel my free trial anytime?</b></p>" + "<p>Our trial is completely free, without any need to enter card details, so we recommend exploring all premium features first and then make a decision with no strings attached.</p>" + "<p><b>6. What happens to my data after the trial ends?</b></p>" + "<p>Your data remains safe and secure under GDPR & HIPAA compliance. You'll still be able to view past analytics, trends, and scores within the app that you access during your trial period.</p>" + "<p><b>7. If I don't subscribe after the trial, can I still use RightLife?</b></p>" + "<p>Yes, you will still have access to app content under the Explore tab, but Premium Features will be locked. To continue your health journey and make the best of RightLife, we recommend subscribing.</p>" + "<p><b>8. Can I extend my free trial period?</b></p>" + "<p>No, the maximum free trial period is 7 days at this time.</p>".trimIndent()


        binding.tvHtmlText.text =
            Html.fromHtml(combinedHtmlText, Html.FROM_HTML_MODE_LEGACY, CoilImageGetter(), null)

        binding.tvPrivacyPolicy.setOnClickListener {
            startActivity(Intent(this, GeneralInformationActivity::class.java).apply {
                putExtra("INFO", "Privacy Policy")
            })
        }
        binding.tvTermsCondition.setOnClickListener {
            startActivity(Intent(this, GeneralInformationActivity::class.java).apply {
                putExtra("INFO", "Terms & Conditions")
            })
        }

        binding.btnBeginFreeTrial.setOnClickListener {
            beginFreeTrial()
            AnalyticsLogger.logEvent(
                AnalyticsEvent.BEGIN_FREE_TRIAL_CLICK,
                mapOf(
                    AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                    AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                    AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                    AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule,
                )
            )
        }

    }

    inner class CoilImageGetter : Html.ImageGetter {
        override fun getDrawable(source: String): Drawable? {
            val resourceId = when (source) {
                "heart_icon" -> R.drawable.ic_heartbeat
                "thinkright_icon" -> R.drawable.thinkrightsvg
                "moveright_icon" -> R.drawable.moverightsvg
                "eatright_icon" -> R.drawable.eatright_headericon
                "sleepright_icon" -> R.drawable.sleeprightsvg
                else -> 0
            }

            if (resourceId != 0) {
                return ContextCompat.getDrawable(this@FreeTrialServiceActivity, resourceId)?.apply {
                    setBounds(0, 0, intrinsicWidth, intrinsicHeight)
                }
            }
            return null
        }
    }

    private fun beginFreeTrial() {

        val body = mapOf("deviceId" to Utils.getDeviceId(this))

        apiService.getFreeTrialService(sharedPreferenceManager.accessToken, body)
            .enqueue(object : Callback<CommonResponse> {
                override fun onResponse(
                    call: Call<CommonResponse>,
                    response: Response<CommonResponse>
                ) {
                    if (response.isSuccessful) {
                        response.body()?.successMessage?.let { showToast(it) }
                    } else {
                        showToast(response.code().toString())
                    }
                    startActivity(
                        Intent(
                            this@FreeTrialServiceActivity,
                            OnboardingFinalActivity::class.java
                        )
                    )
                }

                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    handleNoInternetView(t)
                }

            })
    }

    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}