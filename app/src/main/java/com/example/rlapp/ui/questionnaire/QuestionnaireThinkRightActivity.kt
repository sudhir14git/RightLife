package com.example.rlapp.ui.questionnaire

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.AppCompatActivity
import androidx.viewpager2.widget.ViewPager2
import androidx.viewpager2.widget.ViewPager2.OnPageChangeCallback
import com.example.rlapp.databinding.ActivityQuestionnaireBinding
import com.example.rlapp.ui.questionnaire.adapter.QuestionnaireThinkRightPagerAdapter
import com.example.rlapp.ui.utility.SharedPreferenceManager

class QuestionnaireThinkRightActivity : AppCompatActivity() {
    private lateinit var binding: ActivityQuestionnaireBinding
    private lateinit var sharedPreferenceManager: SharedPreferenceManager


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityQuestionnaireBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)
        //binding.viewPagerQuestionnaire.isUserInputEnabled = false

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
        binding.tvFragmentCount.text = "${fragmentIndex + 1}/8"
    }


    companion object {

        private lateinit var viewPager: ViewPager2
        private lateinit var questionnairePagerAdapter: QuestionnaireThinkRightPagerAdapter

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
    }
}