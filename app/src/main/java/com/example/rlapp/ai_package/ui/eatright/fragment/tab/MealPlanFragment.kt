package com.example.rlapp.ai_package.ui.eatright.fragment.tab

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
import com.example.rlapp.ai_package.ui.eatright.adapter.tab.MealPlanAdapter
import com.example.rlapp.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.example.rlapp.ai_package.ui.eatright.fragment.tab.mealplan.MealPlanListFragment
import com.example.rlapp.ai_package.ui.eatright.model.MealPlanModel
import com.example.rlapp.databinding.FragmentMealPlanBinding

class MealPlanFragment : BaseFragment<FragmentMealPlanBinding>() {

    private lateinit var mealPlanRecyclerView : RecyclerView
    private lateinit var layoutNoMealPlan : LinearLayoutCompat
    private lateinit var layoutCreateMealPlan : LinearLayoutCompat
    private lateinit var rightArrow : ImageView

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMealPlanBinding
        get() = FragmentMealPlanBinding::inflate

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
        rightArrow = view.findViewById(R.id.image_right_arrow)

        mealPlanRecyclerView.layoutManager = LinearLayoutManager(context)
        mealPlanRecyclerView.adapter = mealPlanAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = YourMealLogsFragment()
                val args = Bundle()

                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        })

        onMealPlanItemRefresh()

        rightArrow.setOnClickListener {
            val fragment = MealPlanListFragment()
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
}