package com.example.rlapp.ui.questionnaire.adapter

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.rlapp.ui.questionnaire.fragment.ActiveDuringSessionsFragment
import com.example.rlapp.ui.questionnaire.fragment.BreaksToStretchFragment
import com.example.rlapp.ui.questionnaire.fragment.EatAffectMoodFragment
import com.example.rlapp.ui.questionnaire.fragment.EnergyLevelFragment
import com.example.rlapp.ui.questionnaire.fragment.ExerciseLocationFragment
import com.example.rlapp.ui.questionnaire.fragment.ExercisePreferenceFragment
import com.example.rlapp.ui.questionnaire.fragment.FastfoodPreferenceFragment
import com.example.rlapp.ui.questionnaire.fragment.FoodPreferenceFragment
import com.example.rlapp.ui.questionnaire.fragment.FoodServingFragment
import com.example.rlapp.ui.questionnaire.fragment.MealPreferenceFragment
import com.example.rlapp.ui.questionnaire.fragment.PhysicalActivitiesFragment
import com.example.rlapp.ui.questionnaire.fragment.SchedulePreferenceFragment
import com.example.rlapp.ui.questionnaire.fragment.StepsTakenFragment
import com.example.rlapp.ui.questionnaire.fragment.WaterCaffeineIntakeFragment
import com.example.rlapp.ui.questionnaire.pojo.Question

class QuestionnaireEatRightPagerAdapter(fragment: FragmentActivity) :
    FragmentStateAdapter(fragment) {
    private val fragmentList = ArrayList<String>()

    override fun getItemCount() = fragmentList.size

    override fun createFragment(position: Int): Fragment {
        val question = Question("Q1", "How do you to feel?", "EatRight")
        return when (position) {
            0 -> FoodPreferenceFragment.newInstance(question)
            1 -> MealPreferenceFragment.newInstance(question)
            2 -> SchedulePreferenceFragment.newInstance(question)
            3 -> FoodServingFragment.newInstance(question)
            4 -> WaterCaffeineIntakeFragment.newInstance(question)
            5 -> FastfoodPreferenceFragment.newInstance(question)
            6 -> EatAffectMoodFragment.newInstance(question)
            7 -> ExercisePreferenceFragment.newInstance(question)
            8 -> ActiveDuringSessionsFragment.newInstance(question)
            9 -> PhysicalActivitiesFragment.newInstance(question)
            10 -> ExerciseLocationFragment.newInstance(question)
            11 -> StepsTakenFragment.newInstance(question)
            12 -> EnergyLevelFragment.newInstance(question)
            13 -> BreaksToStretchFragment.newInstance(question)
            else -> throw IllegalStateException("Invalid position")
        }
    }

    fun setQuestionnaireData(list: ArrayList<String>) = fragmentList.addAll(list)

    fun removeItem(name: String) = fragmentList.remove(name)

}