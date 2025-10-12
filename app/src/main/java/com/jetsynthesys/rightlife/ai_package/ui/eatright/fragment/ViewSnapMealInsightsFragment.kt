package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
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
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import com.jetsynthesys.rightlife.ai_package.model.response.MealLogDetailsResponse
import com.jetsynthesys.rightlife.ai_package.model.response.NutritionSummary
import com.jetsynthesys.rightlife.ai_package.model.response.RegularRecipeEntry
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealLogDetails
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MacroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MicroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.SnapMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MacroNutrientsModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MicroNutrientsModel
import com.jetsynthesys.rightlife.databinding.FragmentViewMealInsightsBinding
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ViewSnapMealInsightsFragment : BaseFragment<FragmentViewMealInsightsBinding>() {

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
    private var mealNutritionSummary : NutritionSummary? = null
    private val mealCombinedList = ArrayList<IngredientRecipeDetails>()
    private var mealNames = emptyList<String>()
    private var mealSnapNames : String = ""
    private var mealNameList = ArrayList<String>()
    private var loadingOverlay : FrameLayout? = null

    private var mealLogDetails : SnapMealLogDetails? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentViewMealInsightsBinding
        get() = FragmentViewMealInsightsBinding::inflate

    private val macroNutrientsAdapter by lazy { MacroNutrientsAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMacroNutrientsItemClick) }
    private val microNutrientsAdapter by lazy { MicroNutrientsAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMicroNutrientsItemClick) }
    private val mealLogsAdapter by lazy { SnapMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onBreakFastSnapMealDeleteItem, :: onBreakFastSnapMealEditItem, true) }

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

        val snapMealId = arguments?.getString("snapMealId").toString()

        if (snapMealId != ""){
            getMealDetails(snapMealId)
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                /*startActivity(Intent(context, HomeNewActivity::class.java))
                requireActivity().finish()*/
                val intent = Intent(requireContext(), HomeNewActivity::class.java).apply {
                    addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT) // ✅ bring existing one to front
                }
                startActivity(intent)
                requireActivity().finish()
            }
        })

        backButton.setOnClickListener {
            /*startActivity(Intent(context, HomeNewActivity::class.java))
            requireActivity().finish()*/
            val intent = Intent(requireContext(), HomeNewActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
            }
            startActivity(intent)
            requireActivity().finish()
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

    private fun setDishData(snapRecipeData: SnapMealLogDetails) {

        var imageUrl : String? = ""
        imageUrl = if (snapRecipeData.image_url.contains("drive.google.com")) {
            getDriveImageUrl(snapRecipeData.image_url)
        }else{
            snapRecipeData.image_url
        }
        Glide.with(this)
            .load(imageUrl)
            .placeholder(R.drawable.ic_view_meal_place)
            .error(R.drawable.ic_view_meal_place)
            .into(imgFood)
    }

    private fun setMealLogsList() {

        val snapMealData: IngredientRecipeDetails? = null
        activity?.runOnUiThread {
            if (mealCombinedList.size > 0) {
                tvDishes.visibility = View.VISIBLE
                dishesItemRecyclerview.visibility = View.VISIBLE
               // mealNameList.addAll(mealNames)
              //  mealNameList.addAll(mealSnapNames)
             //   val name = mealNameList.joinToString(", ")
                val capitalized = mealSnapNames.toString().replaceFirstChar { it.uppercase() }
                tvMealName.text = capitalized
                mealLogsAdapter.addAll(mealCombinedList, -1, snapMealData, false)
            } else {
                tvDishes.visibility = View.GONE
                dishesItemRecyclerview.visibility = View.GONE
            }
        }
    }

    private fun onMacroNutrientsList(mealDetails: NutritionSummary?, value: Int) {

        val calories_kcal : String = mealDetails?.calories_kcal?.times(value)?.toInt().toString()?: "NA"
        val protein_g : String = mealDetails?.protein_g?.times(value)?.toInt().toString()?: "NA"
        val carb_g : String = mealDetails?.carbs_g?.times(value)?.toInt().toString()?: "NA"
        val fat_g : String = mealDetails?.fat_g?.times(value)?.toInt().toString()?: "NA"

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

    private fun onMicroNutrientsList(mealDetails: NutritionSummary?, value: Int) {

        val cholesterol = if (mealDetails?.cholesterol_mg != null){
            mealDetails.cholesterol_mg.times(value)?.toString()
        }else{
            "0.0"
        }

        val vitamin_A = if (mealDetails?.vit_a_mcg != null){
            mealDetails.vit_a_mcg.times(value)?.toString()
        }else{
            "0.0"
        }

        val vitamin_C = if (mealDetails?.vit_c_mg != null){
            mealDetails.vit_c_mg.times(value)?.toString()
        }else{
            "0.0"
        }

        val vitamin_k = if (mealDetails?.vit_k_mcg != null){
            mealDetails.vit_k_mcg.times(value)?.toString()
        }else{
            "0.0"
        }

        val vitaminD = if (mealDetails?.vit_d_mcg != null){
            mealDetails.vit_d_mcg.toString()
        }else{
            "0.0"
        }

        val folate = if (mealDetails?.folate_b9_mcg != null){
            mealDetails.folate_b9_mcg.toString()
        }else{
            "0.0"
        }

        val iron_mg = if (mealDetails?.iron_mg != null){
            mealDetails.iron_mg.toString()
        }else{
            "0.0"
        }

        val calcium = if (mealDetails?.calcium_mg != null){
            mealDetails.calcium_mg.toString()
        }else{
            "0.0"
        }

        val magnesium = if (mealDetails?.magnesium_mg != null){
            mealDetails.magnesium_mg.times(value)?.toString()
        }else{
            "0.0"
        }

        val potassium_mg = if (mealDetails?.potassium_mg != null){
            mealDetails.potassium_mg.times(value)?.toString()
        }else{
            "0.0"
        }

        val fiber_mg = if (mealDetails?.fiber_g != null){
            mealDetails.fiber_g.times(value)?.toString()
        }else{
            "0.0"
        }

        val zinc = if (mealDetails?.zinc_mg != null){
            mealDetails.zinc_mg.times(value)?.toString()
        }else{
            "0.0"
        }

        val sodium = if (mealDetails?.sodium_mg != null){
            mealDetails.sodium_mg.times(value)?.toString()
        }else{
            "0.0"
        }

        val sugar_mg = if (mealDetails?.sugars_g != null){
            mealDetails.sugars_g.times(value)?.toString()
        }else{
            "0.0"
        }

        val mealLogs = listOf(
            MicroNutrientsModel(cholesterol, "mg", "Cholesterol", R.drawable.ic_fats),
            MicroNutrientsModel(vitamin_A, "mg", "Vitamin A", R.drawable.ic_fats),
            MicroNutrientsModel(vitamin_C, "mg", "Vitamin C", R.drawable.ic_fats),
            MicroNutrientsModel(vitamin_k, "mg", "Vitamin K", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminD, "mg", "Vitamin D", R.drawable.ic_fats),
            MicroNutrientsModel(folate, "mg", "Folate", R.drawable.ic_fats),
            MicroNutrientsModel(iron_mg, "mg", "Iron", R.drawable.ic_fats),
            MicroNutrientsModel(calcium, "mg", "Calcium", R.drawable.ic_fats),
            MicroNutrientsModel(magnesium, "mg", "Magnesium", R.drawable.ic_fats),
            MicroNutrientsModel(potassium_mg, "mg", "Potassium", R.drawable.ic_fats),
            MicroNutrientsModel(fiber_mg, "mg", "Fiber", R.drawable.ic_fats),
            MicroNutrientsModel(zinc, "mg", "Zinc", R.drawable.ic_fats),
            MicroNutrientsModel(sodium, "mg", "Sodium", R.drawable.ic_fats),
            MicroNutrientsModel(sugar_mg, "g", "Sugar", R.drawable.ic_fats)
        )

        val valueLists : ArrayList<MicroNutrientsModel> = ArrayList()
        //  valueLists.addAll(mealLogs as Collection<MicroNutrientsModel>)
        for (item in mealLogs){
            if (item.nutrientsValue != "0.0"){
                valueLists.add(item)
            }
        }
        val mealLogDateData: MicroNutrientsModel? = null
        microNutrientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMacroNutrientsItemClick(macroNutrientsModel: MacroNutrientsModel, position: Int, isRefresh: Boolean) {
    }
    private fun onMicroNutrientsItemClick(microNutrientsModel: MicroNutrientsModel, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastRegularRecipeDeleteItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastRegularRecipeEditItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastSnapMealDeleteItem(mealItem: IngredientRecipeDetails, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastSnapMealEditItem(mealItem: IngredientRecipeDetails, position: Int, isRefresh: Boolean) {
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

    private fun getMealDetails(mealId: String) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val call = ApiClient.apiServiceFastApiV2.fetchSnapMealLogDetails(userId, mealId)
        call.enqueue(object : Callback<MealLogDetailsResponse> {
            override fun onResponse(call: Call<MealLogDetailsResponse>, response: Response<MealLogDetailsResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealDetails = response.body()?.data
                    if (mealDetails != null){
                        if (mealDetails.meal_details != null){
                            mealLogDetails = mealDetails.meal_details
                            mealSnapNames = mealLogDetails?.meal_name.toString()
                            mealNutritionSummary = mealLogDetails!!.meal_nutrition_summary
                            if (mealNutritionSummary != null){
                                onMacroNutrientsList(mealNutritionSummary, 1)
                                onMicroNutrientsList(mealNutritionSummary, 1)
                            }
                            val snapMealsDish = mealLogDetails!!.dish
                            if (snapMealsDish != null){
                                if (snapMealsDish.isNotEmpty()){
                                    mealCombinedList.addAll(snapMealsDish)
                                }
                            }
                            setMealLogsList()
                            setDishData(mealDetails.meal_details)
                        }
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
            override fun onFailure(call: Call<MealLogDetailsResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }
}