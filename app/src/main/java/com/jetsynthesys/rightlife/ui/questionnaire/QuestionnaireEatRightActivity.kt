package com.jetsynthesys.rightlife.ui.questionnaire

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
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
import com.jetsynthesys.rightlife.ui.questionnaire.adapter.QuestionnaireEatRightPagerAdapter
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.QuestionnaireAnswerRequest
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class QuestionnaireEatRightActivity : BaseActivity() {
    private lateinit var binding: ActivityQuestionnaireBinding


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionnaireBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        // Store activity reference
        instance = this

        binding.viewPagerQuestionnaire.isUserInputEnabled = false

        viewPager = binding.viewPagerQuestionnaire
        viewPager.offscreenPageLimit = 1

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
            "FoodPreferenceFragment",
            "MealPreferenceFragment",
            "SchedulePreferenceFragment",
            "FoodServingFragment",
            "WaterCaffeineIntakeFragment",
            "FastfoodPreferenceFragment",
            "EatAffectMoodFragment",
            "ExercisePreferenceFragment",
            "ActiveDuringSessionsFragment",
            "PhysicalActivitiesFragment",
            "ExerciseLocationFragment",
            "StepsTakenFragment",
            "EnergyLevelFragment",
            "BreaksToStretchFragment"
        )

        questionnairePagerAdapter = QuestionnaireEatRightPagerAdapter(this)
        questionnairePagerAdapter.setQuestionnaireData(fragmentList)
        binding.viewPagerQuestionnaire.adapter = questionnairePagerAdapter

    }

    private fun updateProgress(fragmentIndex: Int) {
        val progressPercentage =
            (((fragmentIndex + 1) / questionnairePagerAdapter.itemCount.toDouble()) * 100).toInt()
        binding.progressQuestionnaire.progress = progressPercentage
        binding.tvFragmentCount.text = "${fragmentIndex + 1}/${questionnairePagerAdapter.itemCount}"
    }


    companion object {

        private lateinit var viewPager: ViewPager2
        lateinit var questionnairePagerAdapter: QuestionnaireEatRightPagerAdapter
        private var instance: QuestionnaireEatRightActivity? = null // Store Activity reference
        val questionnaireAnswerRequest: QuestionnaireAnswerRequest = QuestionnaireAnswerRequest()

        fun navigateToPreviousPage() {
            if (viewPager.currentItem > 0) {
                viewPager.currentItem -= 1
            }
        }

        fun navigateToNextPage() {
            if (viewPager.currentItem < questionnairePagerAdapter.itemCount - 1) {
                viewPager.currentItem += 1
                Log.d("navigateToNextPage: ", "navigateToNextPage: "+viewPager.currentItem)
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
                        "Your fitness and food patterns are now part of your plan."
                    )
                }
            }

            val apiService = ApiClient.getClient(instance).create(ApiService::class.java)
            val call = apiService.submitERQuestionnaire(
                instance?.sharedPreferenceManager?.accessToken ?: "",
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
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        instance = null
    }
}