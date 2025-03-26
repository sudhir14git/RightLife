package com.example.rlapp.ai_package.ui.moveright

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.model.YourActivityLogMeal
import com.example.rlapp.ai_package.ui.adapter.YourActivitiesListAdapter
import com.example.rlapp.ai_package.ui.adapter.YourWorkoutsListAdapter
import com.example.rlapp.ai_package.ui.eatright.model.MyMealModel
import com.example.rlapp.databinding.FragmentYourworkOutsBinding


class YourworkOutsFragment : BaseFragment<FragmentYourworkOutsBinding>() {
    private lateinit var mealLogDateRecyclerView : RecyclerView
    private lateinit var myMealRecyclerView : RecyclerView
    private lateinit var saveWorkoutRoutine : LinearLayoutCompat

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentYourworkOutsBinding
        get() = FragmentYourworkOutsBinding::inflate
    private val mealLogDateAdapter by lazy { YourActivitiesListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onMealLogDateItem) }
    private val myMealListAdapter by lazy { YourWorkoutsListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onMealLogDateItem) }
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.gradient_color_background_workout)
        myMealRecyclerView = view.findViewById(R.id.recyclerview_my_meals_item)
        saveWorkoutRoutine = view.findViewById(R.id.layout_btn_log)
        mealLogDateRecyclerView = view.findViewById(R.id.recyclerview_calender)
        mealLogDateRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mealLogDateRecyclerView.adapter = mealLogDateAdapter
        myMealRecyclerView.layoutManager = LinearLayoutManager(context)
        myMealRecyclerView.adapter = myMealListAdapter
        onMealLogDateItemRefresh()
        onMyMealItemRefresh()
        saveWorkoutRoutine.setOnClickListener {
            val fragment = CreateRoutineFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.flFragment, fragment)
                .addToBackStack(null) // Allows back navigation
                .commit()
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToFragment(AddWorkoutSearchFragment(), "LandingFragment")

            }
        })



    }
    private fun onMealLogDateItemRefresh (){

        val mealLogs = listOf(
            YourActivityLogMeal("01", "M", true),
            YourActivityLogMeal("02", "T", false),
            YourActivityLogMeal("03", "W", true),
            YourActivityLogMeal("04", "T", false),
            YourActivityLogMeal("05", "F", true),
            YourActivityLogMeal("06", "S", true),
            YourActivityLogMeal("07", "S", true)
        )

        val valueLists : ArrayList<YourActivityLogMeal> = ArrayList()
        valueLists.addAll(mealLogs as Collection<YourActivityLogMeal>)
        val mealLogDateData: YourActivityLogMeal? = null
        mealLogDateAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMealLogDateItem(mealLogDateModel: YourActivityLogMeal, position: Int, isRefresh: Boolean) {

        val mealLogs = listOf(
            YourActivityLogMeal("01", "M", true),
            YourActivityLogMeal("02", "T", false),
            YourActivityLogMeal("03", "W", true),
            YourActivityLogMeal("04", "T", false),
            YourActivityLogMeal("05", "F", true),
            YourActivityLogMeal("06", "S", true),
            YourActivityLogMeal("07", "S", true)
        )

        val valueLists : ArrayList<YourActivityLogMeal> = ArrayList()
        valueLists.addAll(mealLogs as Collection<YourActivityLogMeal>)
        mealLogDateAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }
    private fun onMyMealItemRefresh (){

        val meal = listOf(
            MyMealModel("Functional Strength Training", "Poha, Sev", "min", "337", "Low Intensity", "308", "17", false),
            MyMealModel("Functional Strength Training", "Poha, Sev", "min", "337", "Low Intensity", "308", "17", false)
        )

        if (meal.size > 0){
            myMealRecyclerView.visibility = View.VISIBLE
            //layoutNoMeals.visibility = View.GONE
        }else{
           // layoutNoMeals.visibility = View.VISIBLE
            myMealRecyclerView.visibility = View.GONE
        }

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(meal as Collection<MyMealModel>)
        val mealLogDateData: MyMealModel? = null
        myMealListAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMealLogDateItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {

        val mealLogs = listOf(
            MyMealModel("Functional Strength Training", "Poha, Sev", "min", "337", "Low Intensity", "308", "17", false),
            MyMealModel("Functional Strength Training", "Poha, Sev", "min", "337", "Low Intensity", "308", "17",false)
        )

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        //  mealLogDateAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }
    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }


}