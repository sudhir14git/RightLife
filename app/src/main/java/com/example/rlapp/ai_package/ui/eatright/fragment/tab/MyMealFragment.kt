package com.example.rlapp.ai_package.ui.eatright.fragment.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.eatright.adapter.tab.MyMealListAdapter
import com.example.rlapp.ai_package.ui.eatright.fragment.DeleteMealBottomSheet
import com.example.rlapp.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.example.rlapp.ai_package.ui.eatright.fragment.tab.createmeal.CreateMealFragment
import com.example.rlapp.ai_package.ui.eatright.fragment.tab.frequentlylogged.LoggedBottomSheet
import com.example.rlapp.ai_package.ui.eatright.model.MyMealModel
import com.example.rlapp.databinding.FragmentMyMealBinding
import com.google.android.material.snackbar.Snackbar

class MyMealFragment : BaseFragment<FragmentMyMealBinding>() {

    private lateinit var myMealRecyclerView : RecyclerView
    private lateinit var layoutNoMeals : LinearLayoutCompat
    private lateinit var layoutBottomCreateMeal : LinearLayoutCompat
    private lateinit var layoutCreateMeal : LinearLayoutCompat
    private lateinit var loggedBottomSheetFragment : LoggedBottomSheet
    private lateinit var deleteBottomSheetFragment: DeleteMealBottomSheet
    private lateinit var mealPlanTitleLayout : ConstraintLayout

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyMealBinding
        get() = FragmentMyMealBinding::inflate
    var snackbar: Snackbar? = null

    private val myMealListAdapter by lazy { MyMealListAdapter(requireContext(), arrayListOf(), -1, null,
        false, :: onMealDeleteItem, :: onMealLogItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        myMealRecyclerView = view.findViewById(R.id.recyclerview_my_meals_item)
        layoutNoMeals = view.findViewById(R.id.layout_no_meals)
        layoutBottomCreateMeal = view.findViewById(R.id.layout_bottom_create_meal)
        layoutCreateMeal = view.findViewById(R.id.layout_create_meal)
        mealPlanTitleLayout = view.findViewById(R.id.layout_meal_plan_title)

        myMealRecyclerView.layoutManager = LinearLayoutManager(context)
        myMealRecyclerView.adapter = myMealListAdapter

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

        onMyMealItemRefresh()

        layoutCreateMeal.setOnClickListener {
            val fragment = CreateMealFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        layoutBottomCreateMeal.setOnClickListener {
            val fragment = CreateMealFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }
    }

    private fun onMyMealItemRefresh (){
        val meal = listOf(
            MyMealModel("Breakfast", "Poha, Sev", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Poha, Sev", "1", "1,157", "8", "308", "17", false)
        )
        if (meal.size < 0){
            myMealRecyclerView.visibility = View.VISIBLE
            layoutBottomCreateMeal.visibility = View.VISIBLE
            mealPlanTitleLayout.visibility = View.VISIBLE
            layoutNoMeals.visibility = View.GONE
        }else{
            layoutNoMeals.visibility = View.VISIBLE
            myMealRecyclerView.visibility = View.GONE
            layoutBottomCreateMeal.visibility = View.GONE
            mealPlanTitleLayout.visibility = View.GONE
        }
        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(meal as Collection<MyMealModel>)
        val mealLogDateData: MyMealModel? = null
        myMealListAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMealDeleteItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            MyMealModel("Breakfast", "Poha, Sev", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Poha, Sev", "1", "1,157", "8", "308", "17",false)
        )
        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        myMealListAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
        deleteBottomSheetFragment = DeleteMealBottomSheet()
        deleteBottomSheetFragment.isCancelable = true
        val bundle = Bundle()
        bundle.putBoolean("test",false)
        deleteBottomSheetFragment.arguments = bundle
        activity?.supportFragmentManager?.let { deleteBottomSheetFragment.show(it, "DeleteMealBottomSheet") }
    }

    private fun onMealLogItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            MyMealModel("Breakfast", "Poha, Sev", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Poha, Sev", "1", "1,157", "8", "308", "17",false)
        )
        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        myMealListAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
        loggedBottomSheetFragment = LoggedBottomSheet()
        loggedBottomSheetFragment.isCancelable = true
        val bundle = Bundle()
        bundle.putBoolean("test",false)
        loggedBottomSheetFragment.arguments = bundle
        activity?.supportFragmentManager?.let { loggedBottomSheetFragment.show(it, "LoggedBottomSheet") }
    }
}