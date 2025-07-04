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
                }
            }
            val breakfastSnapMeals = mealDetailsLog!!.snap_meals
            if (breakfastSnapMeals != null){
                if (breakfastSnapMeals.isNotEmpty()){
                    mealCombinedList.addAll(breakfastSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                    mealSnapNames = breakfastSnapMeals.map { it.meal_name!! }
                }
            }
            setMealLogsList()
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = YourMealLogsFragment()
                val args = Bundle()
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

    private fun setDishData(snapRecipeData: SearchResultItem) {

        val imageUrl = getDriveImageUrl(snapRecipeData.photo_url)
        Glide.with(this)
            .load(imageUrl)
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

        val calories_kcal : String = mealDetails.calories?.times(value)?.toInt().toString()?: "NA"
        val protein_g : String = mealDetails.protein?.times(value)?.toInt().toString()?: "NA"
        val carb_g : String = mealDetails.carbs?.times(value)?.toInt().toString()?: "NA"
        val fat_g : String = mealDetails.fat?.times(value)?.toInt().toString()?: "NA"

        val mealLogs = listOf(
            MacroNutrientsModel(calories_kcal, "kcal", "Calorie", R.drawable.ic_cal),
            MacroNutrientsModel(protein_g, "g", "Protein", R.drawable.ic_cabs),
            MacroNutrientsModel(carb_g, "g", "Carbs", R.drawable.ic_protein),
            MacroNutrientsModel(fat_g, "g", "Fats", R.drawable.ic_fats),
        )

        val valueLists : ArrayList<MacroNutrientsModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MacroNutrientsModel>)
        val mealLogDateData: MacroNutrientsModel? = null
        macroNutrientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMicroNutrientsList(mealDetails: MealNutritionSummary, value: Int) {

        val cholesterol = if (mealDetails.cholesterol != null){
            mealDetails.cholesterol.times(value)?.toInt().toString()
        }else{
            "0"
        }

//        val vitamin_A = if (mealDetails.nutrients.micros.Vitamin_A != null){
//            mealDetails.nutrients.micros.Vitamin_A.times(value)?.toInt().toString()
//        }else{
//            "0"
//        }

//        val vitamin_C = if (mealDetails.nutrients.micros.Vitamin_C != null){
//            mealDetails.nutrients.micros.Vitamin_C.times(value)?.toInt().toString()
//        }else{
//            "0"
//        }
//
//        val vitamin_k = if (mealDetails.nutrients.micros.Vitamin_K != null){
//            mealDetails.nutrients.micros.Vitamin_K.times(value)?.toInt().toString()
//        }else{
//            "0"
//        }
//
//        val vitaminD = if (mealDetails.nutrients.micros.Vitamin_D != null){
//            mealDetails.nutrients.micros.Vitamin_D.toInt().toString()
//        }else{
//            "0"
//        }

//        val folate = if (mealDetails.nutrients.micros.Folate != null){
//            mealDetails.nutrients.micros.Folate.toInt().toString()
//        }else{
//            "0"
//        }
//
//        val iron_mg = if (mealDetails.nutrients.micros.Iron != null){
//            mealDetails.nutrients.micros.Iron.toInt().toString()
//        }else{
//            "0"
//        }

//        val calcium = if (mealDetails.nutrients.micros.Calcium != null){
//            mealDetails.nutrients.micros.Calcium.toInt().toString()
//        }else{
//            "0"
//        }
//
//        val magnesium = if (mealDetails.nutrients.micros.Magnesium != null){
//            mealDetails.nutrients.micros.Magnesium.times(value)?.toInt().toString()
//        }else{
//            "0"
//        }

        val potassium_mg = if (mealDetails.potassium != null){
            mealDetails.potassium.times(value)?.toInt().toString()
        }else{
            "0"
        }

        val fiber_mg = if (mealDetails.fiber != null){
            mealDetails.fiber.times(value)?.toInt().toString()
        }else{
            "0"
        }
//
//        val zinc = if (mealDetails.nutrients.micros.Zinc != null){
//            mealDetails.nutrients.micros.Zinc.times(value)?.toInt().toString()
//        }else{
//            "0"
//        }

        val sodium = if (mealDetails.sodium != null){
            mealDetails.sodium.times(value)?.toInt().toString()
        }else{
            "0"
        }

        val sugar_mg = if (mealDetails.sugar != null){
            mealDetails.sugar.times(value)?.toInt().toString()
        }else{
            "0"
        }

        val mealLogs = listOf(
            MicroNutrientsModel(cholesterol, "mg", "Cholesterol", R.drawable.ic_fats),
//            MicroNutrientsModel(vitamin_A, "mg", "Vitamin A", R.drawable.ic_fats),
//            MicroNutrientsModel(vitamin_C, "mg", "Vitamin C", R.drawable.ic_fats),
//            MicroNutrientsModel(vitamin_k, "mg", "Vitamin K", R.drawable.ic_fats),
//            MicroNutrientsModel(vitaminD, "mg", "Vitamin D", R.drawable.ic_fats),
//            MicroNutrientsModel(folate, "mg", "Folate", R.drawable.ic_fats),
//            MicroNutrientsModel(iron_mg, "mg", "Iron", R.drawable.ic_fats),
//            MicroNutrientsModel(calcium, "mg", "Calcium", R.drawable.ic_fats),
//            MicroNutrientsModel(magnesium, "mg", "Magnesium", R.drawable.ic_fats),
            MicroNutrientsModel(potassium_mg, "mg", "Potassium", R.drawable.ic_fats),
            MicroNutrientsModel(fiber_mg, "mg", "Fiber", R.drawable.ic_fats),
 //           MicroNutrientsModel(zinc, "mg", "Zinc", R.drawable.ic_fats),
            MicroNutrientsModel(sodium, "mg", "Sodium", R.drawable.ic_fats),
            MicroNutrientsModel(sugar_mg, "g", "Sugar", R.drawable.ic_fats)
        )

        val valueLists : ArrayList<MicroNutrientsModel> = ArrayList()
        //  valueLists.addAll(mealLogs as Collection<MicroNutrientsModel>)
        for (item in mealLogs){
            if (item.nutrientsValue != "0"){
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