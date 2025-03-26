package com.example.rlapp.ai_package.ui.eatright.fragment.tab.mealplan

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.model.DailyRecipe
import com.example.rlapp.ai_package.ui.eatright.adapter.MealLogDateListAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.tab.MealPlanAdapter
import com.example.rlapp.ai_package.ui.eatright.fragment.MealLogCalenderFragment
import com.example.rlapp.ai_package.ui.eatright.fragment.tab.MealPlanFragment
import com.example.rlapp.ai_package.ui.eatright.model.MealLogDateModel
import com.example.rlapp.ai_package.ui.eatright.model.MealPlanModel
import com.example.rlapp.databinding.FragmentMealPlanListBinding


class MealPlanListFragment : BaseFragment<FragmentMealPlanListBinding>() {

    private lateinit var mealPlanRecyclerView : RecyclerView
    private lateinit var layoutNoMealPlan : LinearLayoutCompat
    private lateinit var layoutCreateMealPlan : LinearLayoutCompat
    private lateinit var mealLogDateListAdapter : RecyclerView
    private lateinit var imageCalender : ImageView

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMealPlanListBinding
        get() = FragmentMealPlanListBinding::inflate

    private val mealLogDateAdapter by lazy { MealLogDateListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onMealLogDateItem) }

    private val mealPlanAdapter by lazy { MealPlanAdapter(requireContext(), arrayListOf(), -1, null, false, :: onMealPlanDateItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        mealPlanRecyclerView = view.findViewById(R.id.recyclerview_meal_plan_item)
          layoutNoMealPlan = view.findViewById(R.id.layout_no_meal_plan)
         layoutCreateMealPlan = view.findViewById(R.id.layout_create_meal_plan)
        mealLogDateListAdapter = view.findViewById(R.id.recyclerview_calender)
        imageCalender = view.findViewById(R.id.image_calender)

        mealLogDateListAdapter.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mealLogDateListAdapter.adapter = mealLogDateAdapter

        mealPlanRecyclerView.layoutManager = LinearLayoutManager(context)
        mealPlanRecyclerView.adapter = mealPlanAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = MealPlanFragment()
                val args = Bundle()

                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        })

        onMealLogDateItemRefresh()
        onMealPlanItemRefresh()

        imageCalender.setOnClickListener {
            val fragment = MealLogCalenderFragment()
            val args = Bundle()

            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }
    }

    private fun onMealPlanItemRefresh (){

        val meal = listOf(
            MealPlanModel("Breakfast", "Poha", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            MealPlanModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )

        if (meal.size > 0){
            mealPlanRecyclerView.visibility = View.VISIBLE
            layoutNoMealPlan.visibility = View.GONE
        }else{
            layoutNoMealPlan.visibility = View.VISIBLE
            mealPlanRecyclerView.visibility = View.GONE
        }

        val valueLists : ArrayList<MealPlanModel> = ArrayList()
        valueLists.addAll(meal as Collection<MealPlanModel>)
        val mealPlanData: MealPlanModel? = null
        mealPlanAdapter.addAll(valueLists, -1, mealPlanData, false)
    }

    private fun onMealPlanDateItem(mealLogDateModel: MealPlanModel, position: Int, isRefresh: Boolean) {

        val mealLogs = listOf(
            MealPlanModel("Breakfast", "Poha", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            MealPlanModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )

        val valueLists : ArrayList<MealPlanModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MealPlanModel>)
        //  mealLogDateAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun onMealLogDateItemRefresh (){

        val mealLogs = listOf(
            MealLogDateModel("01", "M", true),
            MealLogDateModel("02", "T", false),
            MealLogDateModel("03", "W", true),
            MealLogDateModel("04", "T", false),
            MealLogDateModel("05", "F", true),
            MealLogDateModel("06", "S", true),
            MealLogDateModel("07", "S", true)
        )

        val valueLists : ArrayList<DailyRecipe> = ArrayList()
        valueLists.addAll(mealLogs as Collection<DailyRecipe>)
        val mealLogDateData: DailyRecipe? = null
        mealLogDateAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMealLogDateItem(mealLogDateModel: DailyRecipe, position: Int, isRefresh: Boolean) {

        val mealLogs = listOf(
            MealLogDateModel("01", "M", true),
            MealLogDateModel("02", "T", false),
            MealLogDateModel("03", "W", true),
            MealLogDateModel("04", "T", false),
            MealLogDateModel("05", "F", true),
            MealLogDateModel("06", "S", true),
            MealLogDateModel("07", "S", true)
        )

        val valueLists : ArrayList<DailyRecipe> = ArrayList()
        valueLists.addAll(mealLogs as Collection<DailyRecipe>)
        mealLogDateAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }
}