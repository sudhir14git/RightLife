package com.example.rlapp.ai_package.ui.eatright.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.eatright.adapter.FrequentlyLoggedMealScanResultAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.MacroNutientsListAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.MicroNutientsListAdapter
import com.example.rlapp.ai_package.ui.eatright.model.MacroNutrientsModel
import com.example.rlapp.ai_package.ui.eatright.model.MicroNutrientsModel
import com.example.rlapp.ai_package.ui.eatright.model.MyMealModel
import com.example.rlapp.ai_package.ui.home.HomeBottomTabFragment
import com.example.rlapp.databinding.FragmentMealScanResultsBinding
import com.google.android.material.snackbar.Snackbar

class MealScanResultFragment: BaseFragment<FragmentMealScanResultsBinding>() {
    private lateinit var macroItemRecyclerView : RecyclerView
    private lateinit var microItemRecyclerView : RecyclerView
    private lateinit var frequentlyLoggedRecyclerView : RecyclerView


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMealScanResultsBinding
        get() = FragmentMealScanResultsBinding::inflate
    var snackbar: Snackbar? = null
    private val microNutientsAdapter by lazy { MicroNutientsListAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMicroNutrientsItem) }
    private val frequentlyLoggedListAdapter by lazy { FrequentlyLoggedMealScanResultAdapter(requireContext(), arrayListOf(), -1, null, false, :: onFrequentlyLoggedItem) }

    private val macroNutientsAdapter by lazy { MacroNutientsListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onMealLogDateItem) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        macroItemRecyclerView = view.findViewById(R.id.recyclerview_macro_item)
        microItemRecyclerView = view.findViewById(R.id.recyclerview_micro_item)
        frequentlyLoggedRecyclerView = view.findViewById(R.id.recyclerview_frequently_logged_item)
        var btnChange = view.findViewById<TextView>(R.id.change_btn)
        frequentlyLoggedRecyclerView.layoutManager = LinearLayoutManager(context)
        frequentlyLoggedRecyclerView.adapter = frequentlyLoggedListAdapter
        onFrequentlyLoggedItemRefresh()

        macroItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        macroItemRecyclerView.adapter = macroNutientsAdapter
        onMealLogDateItemRefresh()
        microItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        microItemRecyclerView.adapter = microNutientsAdapter
        onMicroNutrientsItemRefresh()
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToFragment(HomeBottomTabFragment(), "LandingFragment")
            }
        })

        btnChange.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = MealSearchFragment()
                val args = Bundle()
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }
    }
    private fun onMicroNutrientsItemRefresh (){

        val mealLogs = listOf(
            MicroNutrientsModel("0.1", "mg", "Vitamin B", R.drawable.ic_cal),
            MicroNutrientsModel("3", "mg", "Iron", R.drawable.ic_proteins),
            MicroNutrientsModel("25", "mg", "Magnesium", R.drawable.ic_carbs),
            MicroNutrientsModel("65", "mg", "Phasphorus", R.drawable.ic_fats),
            MicroNutrientsModel("150", "mg", "Potassium", R.drawable.ic_fats),
            MicroNutrientsModel("1", "mg", "Zinc", R.drawable.ic_fats)
        )

        val valueLists : ArrayList<MicroNutrientsModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MicroNutrientsModel>)
        val mealLogDateData: MicroNutrientsModel? = null
        microNutientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }
    private fun onMicroNutrientsItem(microNutrientsModel: MicroNutrientsModel, position: Int, isRefresh: Boolean) {

        val microNutrients = listOf(
            MicroNutrientsModel("0.1", "mg", "Vitamin B", R.drawable.ic_cal),
            MicroNutrientsModel("3", "mg", "Iron", R.drawable.ic_proteins),
            MicroNutrientsModel("25", "mg", "Magnesium", R.drawable.ic_carbs),
            MicroNutrientsModel("65", "mg", "Phasphorus", R.drawable.ic_fats),
            MicroNutrientsModel("150", "mg", "Potassium", R.drawable.ic_fats),
            MicroNutrientsModel("1", "mg", "Zinc", R.drawable.ic_fats)
        )
        val valueLists : ArrayList<MicroNutrientsModel> = ArrayList()
        valueLists.addAll(microNutrients as Collection<MicroNutrientsModel>)
        microNutientsAdapter.addAll(valueLists, position, microNutrientsModel, isRefresh)
    }
    private fun onMealLogDateItem(mealLogDateModel: MacroNutrientsModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            MacroNutrientsModel("1285", "kcal", "calorie", R.drawable.ic_cal),
            MacroNutrientsModel("11", "g", "protien", R.drawable.ic_proteins),
            MacroNutrientsModel("338", "g", "carbs", R.drawable.ic_carbs),
            MacroNutrientsModel("25", "g", "fats", R.drawable.ic_fats),
        )
        val valueLists : ArrayList<MacroNutrientsModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MacroNutrientsModel>)
        macroNutientsAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }
    private fun onMealLogDateItemRefresh (){

        val mealLogs = listOf(
            MacroNutrientsModel("1285", "kcal", "calorie", R.drawable.ic_cal),
            MacroNutrientsModel("11", "g", "protien", R.drawable.ic_proteins),
            MacroNutrientsModel("338", "g", "carbs", R.drawable.ic_carbs),
            MacroNutrientsModel("25", "g", "fats", R.drawable.ic_fats),
        )

        val valueLists : ArrayList<MacroNutrientsModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MacroNutrientsModel>)
        val mealLogDateData: MacroNutrientsModel? = null
        macroNutientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
        //microNutientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }
    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }
    private fun onFrequentlyLoggedItemRefresh (){

        val meal = listOf(
            MyMealModel("Breakfast", "Poha", "1", "1,157", "8", "308", "17", true),
            MyMealModel("Breakfast", "Dal", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Rice", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Roti", "1", "1,157", "8", "308", "17", false)
        )

        if (meal.size > 0){
            frequentlyLoggedRecyclerView.visibility = View.VISIBLE
            //   layoutNoMeals.visibility = View.GONE
        }else{
            //    layoutNoMeals.visibility = View.VISIBLE
            frequentlyLoggedRecyclerView.visibility = View.GONE
        }

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(meal as Collection<MyMealModel>)
        val mealLogDateData: MyMealModel? = null
        frequentlyLoggedListAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onFrequentlyLoggedItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {

        val mealLogs = listOf(
            MyMealModel("Breakfast", "Poha", "1", "1,157", "8", "308", "17", true),
            MyMealModel("Breakfast", "Dal", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Rice", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Roti", "1", "1,157", "8", "308", "17", false)
        )

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        frequentlyLoggedListAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }
}