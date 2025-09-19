package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.model.response.MealDetailsLog
import com.jetsynthesys.rightlife.ai_package.model.response.MealNutritionSummary
import com.jetsynthesys.rightlife.ai_package.model.response.MergedLogsMealItem
import com.jetsynthesys.rightlife.ai_package.model.response.RegularRecipeEntry
import com.jetsynthesys.rightlife.ai_package.model.response.SearchResultItem
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMeal
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MacroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MicroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourBreakfastMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.BreakfastMealModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MacroNutrientsModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MicroNutrientsModel
import com.jetsynthesys.rightlife.databinding.FragmentViewMealInsightsBinding
import kotlin.math.round

class ViewMealInsightsFragment : BaseFragment<FragmentViewMealInsightsBinding>() {

    private lateinit var macroItemRecyclerView : RecyclerView
    private lateinit var microItemRecyclerView : RecyclerView
    private lateinit var dishesItemRecyclerview : RecyclerView
    private lateinit var backButton : ImageView
    private lateinit var layoutMicroTitle : ConstraintLayout
    private lateinit var layoutMacroTitle : ConstraintLayout
    private lateinit var icMacroUP : ImageView
    private lateinit var microUP : ImageView
    private lateinit var imgFood : ImageView
    private lateinit var tvMealName : TextView
    private lateinit var tvDishes : TextView
    private val mealNutritionSummary = ArrayList<MealNutritionSummary>()
    private val mealCombinedList = ArrayList<MergedLogsMealItem>()
    private var mealNames = emptyList<String>()
    private var mealSnapNames = emptyList<String>()
    private var mealNameList = ArrayList<String>()
    private var selectedMealDate : String = ""

    private var mealDetailsLog : MealDetailsLog? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentViewMealInsightsBinding
        get() = FragmentViewMealInsightsBinding::inflate

    private val macroNutrientsAdapter by lazy { MacroNutrientsAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMacroNutrientsItemClick) }
    private val microNutrientsAdapter by lazy { MicroNutrientsAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMicroNutrientsItemClick) }
    private val mealLogsAdapter by lazy { MealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null, false, ::onBreakFastRegularRecipeDeleteItem,
        :: onBreakFastRegularRecipeEditItem, :: onBreakFastSnapMealDeleteItem, :: onBreakFastSnapMealEditItem, true) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        macroItemRecyclerView = view.findViewById(R.id.recyclerview_macro_item)
        microItemRecyclerView = view.findViewById(R.id.recyclerview_micro_item)
        dishesItemRecyclerview = view.findViewById(R.id.dishesItemRecyclerview)
        backButton = view.findViewById(R.id.back_button)
        tvMealName = view.findViewById(R.id.tvMealName)
        imgFood = view.findViewById(R.id.imgFood)
        layoutMicroTitle = view.findViewById(R.id.layoutMicroTitle)
        layoutMacroTitle = view.findViewById(R.id.layoutMacroTitle)
        microUP = view.findViewById(R.id.microUP)
        icMacroUP = view.findViewById(R.id.icMacroUP)
        tvDishes = view.findViewById(R.id.tvDishes)

        macroItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        macroItemRecyclerView.adapter = macroNutrientsAdapter
        microItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        microItemRecyclerView.adapter = microNutrientsAdapter
        dishesItemRecyclerview.layoutManager = LinearLayoutManager(context)
        dishesItemRecyclerview.adapter = mealLogsAdapter

        val moduleName = arguments?.getString("ModuleName").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()
        val mealDetailsLogResponse = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("mealDetailsLog", MealDetailsLog::class.java)
        } else {
            arguments?.getParcelable("mealDetailsLog")
        }

        if (mealDetailsLogResponse != null){
            mealDetailsLog = mealDetailsLogResponse
          //  setDishData(foodDetailsResponse)
            mealNutritionSummary.addAll(mealDetailsLog!!.meal_nutrition_summary)
            if (mealNutritionSummary.size > 0){
                onMacroNutrientsList(mealNutritionSummary.get(0), 1)
                onMicroNutrientsList(mealNutritionSummary.get(0), 1)
            }
            val breakfastRecipes = mealDetailsLog!!.regular_receipes
            if (breakfastRecipes != null){
                if (breakfastRecipes.isNotEmpty()){
                    mealCombinedList.addAll(breakfastRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                    mealNames = breakfastRecipes.map { it.receipe.recipe_name }
                    val imageUrl = breakfastRecipes.get(0).receipe.photo_url
                    setDishData(imageUrl)
                }
            }
            val breakfastSnapMeals = mealDetailsLog!!.snap_meals
            if (breakfastSnapMeals != null){
                if (breakfastSnapMeals.isNotEmpty()){
                    mealCombinedList.addAll(breakfastSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                    mealSnapNames = breakfastSnapMeals.map { it.meal_name!! }
                    setSnapDishData(breakfastSnapMeals?.getOrNull(0)?.image_url)
                }
            }
            setMealLogsList()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = YourMealLogsFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "mealLog")
                    addToBackStack("mealLog")
                    commit()
                }
            }
        })

        backButton.setOnClickListener {
            val fragment = YourMealLogsFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("selectedMealDate", selectedMealDate)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        layoutMacroTitle.setOnClickListener {
            if (macroItemRecyclerView.isVisible){
                macroItemRecyclerView.visibility = View.GONE
                icMacroUP.setImageResource(R.drawable.ic_down)
                view.findViewById<View>(R.id.view_macro).visibility = View.GONE
            }else{
                macroItemRecyclerView.visibility = View.VISIBLE
                icMacroUP.setImageResource(R.drawable.ic_up)
                view.findViewById<View>(R.id.view_macro).visibility = View.VISIBLE
            }
        }

        layoutMicroTitle.setOnClickListener {
            if (microItemRecyclerView.isVisible){
                microItemRecyclerView.visibility = View.GONE
                microUP.setImageResource(R.drawable.ic_down)
                view.findViewById<View>(R.id.view_micro).visibility = View.GONE
            }else{
                microItemRecyclerView.visibility = View.VISIBLE
                microUP.setImageResource(R.drawable.ic_up)
                view.findViewById<View>(R.id.view_micro).visibility = View.VISIBLE
            }
        }
    }

    private fun setDishData(snapRecipeData: String?) {
        val imageUrl = snapRecipeData?.let { getDriveImageUrl(it) }
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_view_meal_place)
            .error(R.drawable.ic_view_meal_place)
            .into(imgFood)
    }

    private fun setSnapDishData(snapRecipeData: String?) {
        Glide.with(this)
            .load(snapRecipeData)
            .placeholder(R.drawable.ic_view_meal_place)
            .error(R.drawable.ic_view_meal_place)
            .into(imgFood)
    }

    private fun setMealLogsList() {

        val regularRecipeData: RegularRecipeEntry? = null
        val snapMealData: SnapMeal? = null
        activity?.runOnUiThread {
            if (mealCombinedList.size > 0) {
                tvDishes.visibility = View.VISIBLE
                dishesItemRecyclerview.visibility = View.VISIBLE
                mealNameList.addAll(mealNames)
                mealNameList.addAll(mealSnapNames)
                val name = mealNameList.joinToString(", ")
                val capitalized = name.toString().replaceFirstChar { it.uppercase() }
                tvMealName.text = capitalized
                mealLogsAdapter.addAll(mealCombinedList, -1, regularRecipeData, snapMealData, false)
            } else {
                tvDishes.visibility = View.GONE
                dishesItemRecyclerview.visibility = View.GONE
            }
        }
    }

    private fun onMacroNutrientsList(mealDetails: MealNutritionSummary, value: Int) {

        val calories_kcal : String = round(mealDetails.calories.times(value))?.toInt().toString()?: "NA"
        val protein_g : String = round(mealDetails.protein.times(value))?.toInt().toString()?: "NA"
        val carb_g : String = round(mealDetails.carbs.times(value))?.toInt().toString()?: "NA"
        val fat_g : String = round(mealDetails.fats.times(value))?.toInt().toString()?: "NA"

        val mealLogs = listOf(
            MacroNutrientsModel(calories_kcal, "kcal", "Calorie", R.drawable.ic_cal),
            MacroNutrientsModel(protein_g, "g", "Protein", R.drawable.ic_protein),
            MacroNutrientsModel(carb_g, "g", "Carbs", R.drawable.ic_cabs),
            MacroNutrientsModel(fat_g, "g", "Fats", R.drawable.ic_fats),
        )

        val valueLists : ArrayList<MacroNutrientsModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MacroNutrientsModel>)
        val mealLogDateData: MacroNutrientsModel? = null
        macroNutrientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMicroNutrientsList(mealDetails: MealNutritionSummary, value: Int) {

        var totalVitaminD = 0.0
        var totalB12 = 0.0
        var totalFolate = 0.0
        var totalVitaminC = 0.0
        var totalVitaminA = 0.0
        var totalVitaminK = 0.0
        var totalIron = 0.0
        var totalCalcium = 0.0
        var totalMagnesium = 0.0
        var totalZinc = 0.0
        var totalOmega3 = 0.0
        var totalSodium = 0.0
        var totalCholesterol = 0.0
        var totalSugar = 0.0
        var totalPhosphorus = 0.0
        var totalPotassium = 0.0

            totalVitaminD = mealDetails.vitamin_d_iu ?: 0.0
            totalB12 = mealDetails.b12_mcg ?: 0.0
            totalFolate = mealDetails.folate_mcg ?: 0.0
            totalVitaminC = mealDetails.vitamin_c_mg ?: 0.0
            totalVitaminA = mealDetails.vitamin_a_mcg ?: 0.0
            totalVitaminK = mealDetails.vitamin_k_mcg ?: 0.0
            totalIron = mealDetails.iron ?: 0.0
            totalCalcium = mealDetails.calcium_mg ?: 0.0
            totalMagnesium = mealDetails.magnesium ?: 0.0
            totalZinc = mealDetails.zinc_mg ?: 0.0
            totalOmega3 = mealDetails.omega_3_fatty_acids_g ?: 0.0
            totalSodium = mealDetails.sodium ?: 0.0
            totalCholesterol = mealDetails.cholesterol ?: 0.0
            totalSugar = mealDetails.sugar ?: 0.0
            totalPhosphorus = mealDetails.phosphorus_mg ?: 0.0
            totalPotassium = mealDetails.potassium ?: 0.0

        val vitaminD = if (totalVitaminD != null) {
            String.format("%.1f", totalVitaminD)
        } else {
            "0.0"
        }

        val b12_mcg = if (totalB12 != null) {
            String.format("%.1f", totalB12)
        } else {
            "0.0"
        }

        val folate = if (totalFolate != null) {
            String.format("%.1f", totalFolate)
        } else {
            "0.0"
        }

        val vitaminC = if (totalVitaminC != null) {
            String.format("%.1f", totalVitaminC)
        } else {
            "0.0"
        }

        val vitaminA = if (totalVitaminA != null) {
            String.format("%.1f", totalVitaminA)
        } else {
            "0.0"
        }

        val vitaminK = if (totalVitaminK != null) {
            String.format("%.1f", totalVitaminK)
        } else {
            "0.0"
        }

        val iron_mg = if (totalIron != null) {
            String.format("%.1f", totalIron)
        } else {
            "0.0"
        }

        val calcium = if (totalCalcium != null) {
            String.format("%.1f", totalCalcium)
        } else {
            "0.0"
        }

        val magnesium_mg = if (totalMagnesium != null) {
            String.format("%.1f", totalMagnesium)
        } else {
            "0.0"
        }

        val zinc_mg = if (totalZinc != null) {
            String.format("%.1f", totalZinc)
        } else {
            "0.0"
        }

        val omega3 = if (totalOmega3 != null) {
            String.format("%.1f", totalOmega3)
        } else {
            "0.0"
        }

        val sodium = if (totalSodium != null) {
            String.format("%.1f", totalSodium)
        } else {
            "0.0"
        }

        val cholesterol = if (totalCholesterol != null) {
            String.format("%.1f", totalCholesterol)
        } else {
            "0.0"
        }

        val sugar = if (totalSugar != null) {
            String.format("%.1f", totalSugar)
        } else {
            "0.0"
        }

        val phosphorus_mg = if (totalPhosphorus != null) {
            String.format("%.1f", totalPhosphorus)
        } else {
            "0.0"
        }
        val potassium_mg = if (totalPotassium != null) {
            String.format("%.1f", totalPotassium)
        } else {
            "0.0"
        }

        val mealLogs = listOf(
       //     MicroNutrientsModel(fiber_mg, "mg", "Fiber", R.drawable.ic_fats),
            MicroNutrientsModel(calcium, "mg", "Calcium", R.drawable.ic_fats),
            MicroNutrientsModel(cholesterol, "mg", "Cholesterol", R.drawable.ic_fats),
            MicroNutrientsModel(folate, "μg", "Folate", R.drawable.ic_fats),
            MicroNutrientsModel(iron_mg, "mg", "Iron", R.drawable.ic_fats),
            MicroNutrientsModel(magnesium_mg, "mg", "Magnesium", R.drawable.ic_fats),
            MicroNutrientsModel(omega3, "mg", "Omega-3", R.drawable.ic_fats),
            MicroNutrientsModel(potassium_mg, "mg", "Potassium", R.drawable.ic_fats),
            MicroNutrientsModel(sodium, "mg", "Sodium", R.drawable.ic_fats),
            MicroNutrientsModel(sugar, "g", "Sugar", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminA, "IU", "Vitamin A", R.drawable.ic_fats),
            MicroNutrientsModel(b12_mcg, "μg", "Vitamin B12", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminC, "mg", "Vitamin C", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminD, "μg", "Vitamin D", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminK, "μg", "Vitamin K", R.drawable.ic_fats),
            MicroNutrientsModel(zinc_mg, "mg", "Zinc", R.drawable.ic_fats)
        )

        val valueLists : ArrayList<MicroNutrientsModel> = ArrayList()
        for (item in mealLogs){
            if (item.nutrientsValue != "0.0"){
                valueLists.add(item)
            }
        }
        val mealLogDateData: MicroNutrientsModel? = null
        microNutrientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onBreakfastMealLogItemRefresh() {
        val mealLogs = listOf(
            BreakfastMealModel("Breakfast", "Poha", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            BreakfastMealModel("Breakfast", "Apple", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
        )
        val valueLists : ArrayList<BreakfastMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<BreakfastMealModel>)
        val breakfastMealData: BreakfastMealModel? = null
      //  macroNutientsAdapter.addAll(valueLists, -1, breakfastMealData, false)
    }

    private fun onMacroNutrientsItemClick(macroNutrientsModel: MacroNutrientsModel, position: Int, isRefresh: Boolean) {
    }
    private fun onMicroNutrientsItemClick(microNutrientsModel: MicroNutrientsModel, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastRegularRecipeDeleteItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastRegularRecipeEditItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastSnapMealDeleteItem(mealItem: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastSnapMealEditItem(mealItem: SnapMeal, position: Int, isRefresh: Boolean) {
    }

    fun getDriveImageUrl(originalUrl: String): String? {
        val regex = Regex("(?<=/d/)(.*?)(?=/|$)")
        val matchResult = regex.find(originalUrl)
        val fileId = matchResult?.value
        return if (!fileId.isNullOrEmpty()) {
            "https://drive.google.com/uc?export=view&id=$fileId"
        } else {
            null
        }
    }
}