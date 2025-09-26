package com.jetsynthesys.rightlife.newdashboard

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityBeginMyFreeTrialBinding
import com.jetsynthesys.rightlife.subscriptions.adapter.PlanSliderAdapter
import com.jetsynthesys.rightlife.ui.CommonResponse
import com.jetsynthesys.rightlife.ui.context_screens.WelcomeRightLifeContextScreenActivity
import com.jetsynthesys.rightlife.ui.utility.NetworkUtils
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.zhpan.indicator.enums.IndicatorSlideMode
import com.zhpan.indicator.enums.IndicatorStyle
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class BeginMyFreeTrialActivity : BaseActivity() {
    private lateinit var binding: ActivityBeginMyFreeTrialBinding
    private lateinit var runnable: Runnable
    private val timeDurationForImageSlider = 2000L

    private val images = listOf(
        R.drawable.planinfo1,
        R.drawable.planinfo2,
        R.drawable.planinfo3,
        R.drawable.planinfo4,
        R.drawable.planinfo5,
        R.drawable.planinfo6,
        R.drawable.planinfo
    )

    private val headers = listOf(
        "Comprehensive health insights with only a phone",
        "Monitor your vitals effortlessly",
        "Smart nutrition tracking, personalised for you",
        "Optimise your activity and performance",
        "Strengthen your mind with powerful tools",
        "Better sleep, deeper recovery, every night",
        "Seamless health tracking, all in one place"
    )

    private val descriptions = listOf(
        "Everyone deserves best-in-class health guidance.\nLog your stats, and we’ll handle the insights for you.",
        "Scan your face for BP, stress and heart health insights and get expert guidance",
        "Log meals effortlessly and track macros in real.\nStay hydrated and monitor weight trends with ease.",
        "Monitor daily activity(heart rates, steps,calories) and analyze workout/vital data for enhanced recovery.",
        "Track moods, journal, and practice guided breathing.\nUse affirmations and audits to build mental resilience.",
        "Track your sleep stages and consistency, then relax with guided sounds and personalised sleep insights.",
        "Automatically sync data from your wearable.\nMake your data more actionable with deeper insight."
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBeginMyFreeTrialBinding.inflate(layoutInflater)
        setChildContentView(binding.root)


        binding.ivDialogClose.setOnClickListener {
            finish()
        }

        binding.btnClaimFreeTrial.setOnClickListener {
            if (NetworkUtils.isInternetAvailable(this)) {
                beginFreeTrial()
            } else {
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

        // Set up the auto-slide functionality
        val handler = Handler(mainLooper)
        runnable = Runnable {
            val currentItem = binding.viewPager.currentItem
            val nextItem = if (currentItem < images.size - 1) currentItem + 1 else 0
            binding.viewPager.setCurrentItem(nextItem, true)
            handler.postDelayed(runnable, timeDurationForImageSlider)
        }


    }

    private fun showInternetError() {
        Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
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
                        binding.llClaimBottomSheet.visibility = View.GONE
                        binding.llCompleteBottomSheet.visibility = View.VISIBLE
                        Handler(mainLooper).postDelayed({
                            startActivity(
                                Intent(
                                    this@BeginMyFreeTrialActivity,
                                    WelcomeRightLifeContextScreenActivity::class.java
                                )
                            )
                            finish()
                        }, 1000)
                    } else {
                        showToast(response.code().toString())
                    }
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