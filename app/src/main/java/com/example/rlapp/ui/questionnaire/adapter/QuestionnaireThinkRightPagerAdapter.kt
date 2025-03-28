package com.example.rlapp.ui.questionnaire.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.rlapp.ui.questionnaire.fragment.AnexityPowerFragment
import com.example.rlapp.ui.questionnaire.fragment.BedWakeupTimeFragment
import com.example.rlapp.ui.questionnaire.fragment.BeforeGoingToBedFragment
import com.example.rlapp.ui.questionnaire.fragment.EmotionsPastWeekFragment
import com.example.rlapp.ui.questionnaire.fragment.FeelAfterWakingFragment
import com.example.rlapp.ui.questionnaire.fragment.OverthinkingYourselfFragment
import com.example.rlapp.ui.questionnaire.fragment.QualityOfSleepFragment
import com.example.rlapp.ui.questionnaire.fragment.RelaxAndUnwindFragment
import com.example.rlapp.ui.questionnaire.fragment.ShakeOffBadDayFragment
import com.example.rlapp.ui.questionnaire.fragment.SleepSelectionFragment
import com.example.rlapp.ui.questionnaire.fragment.SleepTimeFragment
import com.example.rlapp.ui.questionnaire.fragment.SocialInteractionFragment
import com.example.rlapp.ui.questionnaire.fragment.WhatsInYourMindFragment
import com.example.rlapp.ui.questionnaire.pojo.Question

class QuestionnaireThinkRightPagerAdapter(fragment: FragmentActivity) :
    FragmentStateAdapter(fragment) {
    private val fragmentList = ArrayList<String>()

    override fun getItemCount() = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        val question = Question("Q1", "How do you to feel?", "EatRight")
        return when (position) {
            0 -> EmotionsPastWeekFragment.newInstance(question)
            1 -> ShakeOffBadDayFragment.newInstance(question)
            2 -> WhatsInYourMindFragment.newInstance(question)
            3 -> AnexityPowerFragment.newInstance(question)
            4 -> OverthinkingYourselfFragment.newInstance(question)
            5 -> SocialInteractionFragment.newInstance(question)
            6 -> RelaxAndUnwindFragment.newInstance(question)
            7 -> QualityOfSleepFragment.newInstance(question)
            8 -> SleepTimeFragment.newInstance(question)
            9 -> BedWakeupTimeFragment.newInstance(question)
            10 -> SleepSelectionFragment.newInstance(question)
            11 -> FeelAfterWakingFragment.newInstance(question)
            12 -> BeforeGoingToBedFragment.newInstance(question)
            else -> throw IllegalStateException("Invalid position")
        }
    }

    fun setQuestionnaireData(list: ArrayList<String>) = fragmentList.addAll(list)

    fun removeItem(name: String) = fragmentList.remove(name)

}