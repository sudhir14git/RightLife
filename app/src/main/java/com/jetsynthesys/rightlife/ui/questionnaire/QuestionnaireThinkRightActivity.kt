package com.jetsynthesys.rightlife.ui.questionnaire

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.databinding.ActivityQuestionnaireBinding
import com.jetsynthesys.rightlife.ui.CommonResponse
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.questionnaire.adapter.QuestionnaireThinkRightPagerAdapter
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.QuestionnaireAnswerRequest
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuestionnaireThinkRightActivity : BaseActivity() {
    private lateinit var binding: ActivityQuestionnaireBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionnaireBinding.inflate(layoutInflater)
        setContentView(binding.root)
        instance = this
        binding.viewPagerQuestionnaire.isUserInputEnabled = false

        viewPager = binding.viewPagerQuestionnaire

        binding.iconBack.setOnClickListener {
            val currentItem = binding.viewPagerQuestionnaire.currentItem
            if (currentItem == 0) {
                finish()
            } else {
                navigateToPreviousPage()
            }
        }

        binding.viewPagerQuestionnaire.registerOnPageChangeCallback(object :
            OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                updateProgress(position)
            }
        })

        onBackPressedDispatcher.addCallback(object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.viewPagerQuestionnaire.currentItem == 0) finish() else navigateToPreviousPage()
            }
        })

        val fragmentList = arrayListOf(
            "EmotionsPastWeekFragment",
            "ShakeOffBadDayFragment",
            "WhatsInYourMindFragment",
            "AnexityPowerFragment",
            "OverthinkingYourselfFragment",
            "SocialInteractionFragment",
            "RelaxAndUnwindFragment",
            "QualityOfSleepFragment",
            "SleepTimeFragment",
            "BedWakeupTimeFragment",
            "SleepSelectionFragment",
            "FeelAfterWakingFragment",
            "BeforeGoingToBedFragment"
        )

        questionnairePagerAdapter = QuestionnaireThinkRightPagerAdapter(this)
        questionnairePagerAdapter.setQuestionnaireData(fragmentList)
        binding.viewPagerQuestionnaire.adapter = questionnairePagerAdapter

    }

    private fun updateProgress(fragmentIndex: Int) {
        // Set progress percentage based on the current fragment (out of 8)
        val progressPercentage =
            (((fragmentIndex + 1) / questionnairePagerAdapter.itemCount.toDouble()) * 100).toInt()
        binding.progressQuestionnaire.progress = progressPercentage
        binding.tvFragmentCount.text = "${fragmentIndex + 1}/${questionnairePagerAdapter.itemCount}"
    }


    companion object {

        private lateinit var viewPager: ViewPager2
        private lateinit var questionnairePagerAdapter: QuestionnaireThinkRightPagerAdapter
        private var instance: QuestionnaireThinkRightActivity? = null // Store Activity reference
        val questionnaireAnswerRequest = QuestionnaireAnswerRequest()

        fun navigateToPreviousPage() {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }

        fun navigateToNextPage() {
            if (viewPager.currentItem < questionnairePagerAdapter.itemCount - 1) {
                viewPager.currentItem += 1
            }
        }

        fun submitQuestionnaireAnswerRequest(questionnaireAnswerRequest: QuestionnaireAnswerRequest) {
            if (viewPager.currentItem != questionnairePagerAdapter.itemCount - 1) {
                Handler(Looper.getMainLooper()).postDelayed({
                    navigateToNextPage()
                }, 500)
            } else {
                instance?.let {
                    DialogUtils.showSuccessDialog(
                        it,
                        "You’re All Set",
                        "Your sleep habits and mental patterns are now part of your wellness path."
                    )
                    AnalyticsLogger.logEvent(it , AnalyticsEvent.Checklist_TRSR_Completed)
                }
            }
            val apiService = ApiClient.getClient(instance).create(ApiService::class.java)
            val call = apiService.submitERQuestionnaire(
                instance?.sharedPreferenceManager?.accessToken,
                questionnaireAnswerRequest
            )

            call.enqueue(object : Callback<CommonResponse> {
                override fun onResponse(
                    call: Call<CommonResponse>,
                    response: Response<CommonResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        if (viewPager.currentItem == questionnairePagerAdapter.itemCount - 1) {

                        }

                    } else {
                        Toast.makeText(
                            instance,
                            "Server Error: " + response.code(),
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun onFailure(call: Call<CommonResponse>, t: Throwable) {
                    Toast.makeText(
                        instance,
                        "Network Error: " + t.message,
                        Toast.LENGTH_SHORT
                    ).show()
                }

            })
            /*if (viewPager.currentItem == questionnairePagerAdapter.itemCount - 1)
                instance?.finish() // Finish activity safely
            else
                navigateToNextPage()*/
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }

}