package com.jetsynthesys.rightlife.newdashboard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityBeginMyFreeTrialBinding
import com.jetsynthesys.rightlife.subscriptions.adapter.PlanSliderAdapter
import com.jetsynthesys.rightlife.ui.CommonResponse
import com.jetsynthesys.rightlife.ui.context_screens.WelcomeRightLifeContextScreenActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.FeatureFlags
import com.jetsynthesys.rightlife.ui.utility.NetworkUtils
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BeginMyFreeTrialActivity : BaseActivity()
{
    private val entryDest by lazy {
        intent.getStringExtra(FeatureFlags.EXTRA_ENTRY_DEST).orEmpty()
    }
    private lateinit var binding: ActivityBeginMyFreeTrialBinding
    private lateinit var runnable: Runnable
    private val timeDurationForImageSlider = 2000L
    private var sliderHandler: Handler? = null // Handler for scheduling auto-slide
    private var sliderRunnable: Runnable? = null

    private val images = listOf(
            R.drawable.planinfo1,
            R.drawable.planinfo3,
            R.drawable.planinfo2,
            R.drawable.planinfo4,
            R.drawable.planinfo6,
            R.drawable.planinfo,
            R.drawable.planinfo5
    )

    private val headers = listOf(
            "Comprehensive health insights with only a phone",
            "Smart nutrition tracking, personalized for you.",
            "Monitor your vitals effortlessly.",
            "Optimise your activity and performance",
            "Better sleep, deeper recovery, every night.",
            "Seamless health tracking, all in one place.",
            "Strengthen your mind with powerful tools."
    )

    private val descriptions = listOf(
            "Everyone deserves best-in-class health guidance.\nLog your stats, and we’ll handle the insights for you.",
            "Log meals effortlessly and track macros in real time.\nStay hydrated and monitor weight trends with ease.",
            "Use our face scan to check BP, stress, and heart health.\nGet expert-backed guidance to optimize your vitals.",
            "Monitor daily activity(heart rates, steps,calories) and analyze workout/vital data for enhanced recovery.",
            "Monitor sleep stages, consistency, and ideal rest.\nRelax with guided sleep sounds and personalized insights.",
            "Automatically sync data from your wearable.\n Make your data more actionable with deeper insights.",
            "Track moods, journal, and practice guided breathing.\nUse affirmations and audits to build mental resilience."
    )

    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityBeginMyFreeTrialBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        sliderHandler = Handler(Looper.getMainLooper())
        sliderRunnable = object : Runnable
        {
            override fun run()
            {
                val currentItem = binding.viewPager.currentItem
                val nextItem: Int = if (currentItem < images.size - 1) currentItem + 1 else 0
                binding.viewPager.setCurrentItem(nextItem, true)
                sliderHandler?.removeCallbacks(sliderRunnable!!)
                sliderHandler?.postDelayed(this, timeDurationForImageSlider)
            }
        }
        sliderHandler?.postDelayed(sliderRunnable!!, timeDurationForImageSlider)

        binding.ivDialogClose.setOnClickListener {
            finish()
        }

        binding.btnClaimFreeTrial.setOnClickListener {
            it.disableViewForSeconds()
            if (NetworkUtils.isInternetAvailable(this))
            {
                beginFreeTrial()
            } else
            {
                showInternetError()
            }
        }

        //setup viewPager
        binding.viewPager.adapter = PlanSliderAdapter(this, images, headers, descriptions)
        val selectedColor = ContextCompat.getColor(this, R.color.menuselected)
        val unselectedColor = ContextCompat.getColor(this, R.color.gray)

        binding.indicatorViewPager.apply {
            setSliderColor(unselectedColor, selectedColor)
            setIndicatorDrawable(R.drawable.dot_1, R.drawable.selected_dot)
            setIndicatorGap(resources.getDimensionPixelOffset(R.dimen.grid_spacing))
            //setIndicatorSize(dp20, dp20, dp20, dp20)

            setSlideMode(IndicatorSlideMode.SCALE)
            setIndicatorStyle(IndicatorStyle.ROUND_RECT)
            setupWithViewPager(binding.viewPager)
        }
    }

    private fun showInternetError()
    {
        Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
    }

    private fun beginFreeTrial()
    {

        val body = mapOf("deviceId" to Utils.getDeviceId(this))

        apiService.getFreeTrialService(sharedPreferenceManager.accessToken, body)
                .enqueue(object : Callback<CommonResponse>
                         {
                             override fun onResponse(
                                     call: Call<CommonResponse>,
                                     response: Response<CommonResponse>
                             )
                             {
                                 if (response.isSuccessful)
                                 {
                                     binding.llClaimBottomSheet.visibility = View.GONE
                                     binding.llCompleteBottomSheet.visibility = View.VISIBLE
                                     Handler(mainLooper).postDelayed({
                                                                         startActivity(
                                                                                 Intent(
                                                                                         this@BeginMyFreeTrialActivity,
                                                                                         WelcomeRightLifeContextScreenActivity::class.java
                                                                                 ).apply {
                                                                                     // 🔁 forward the incoming flag to the next screen
                                                                                     putExtra(FeatureFlags.EXTRA_ENTRY_DEST, entryDest)
                                                                                 }
                                                                         )
                                                                         finish()
                                                                     }, 1000)
                                     AnalyticsLogger.logEvent(this@BeginMyFreeTrialActivity,
                                             AnalyticsEvent.FREETRIALUNlOCKED_POPUP_OPEN,
                                             mapOf(
                                                     AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                                                     AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                                             )
                                     )
                                 } else
                                 {
                                     showToast(response.code().toString())
                                 }
                             }

                             override fun onFailure(call: Call<CommonResponse>, t: Throwable)
                             {
                                 handleNoInternetView(t)
                             }

                         })
    }

    private fun showToast(message: String)
    {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

}