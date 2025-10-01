package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.R.color.transparent
import android.content.Context
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.response.MealDetailsLog
import com.jetsynthesys.rightlife.ai_package.model.response.MealLogDataResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealNutritionSummary
import com.jetsynthesys.rightlife.ai_package.model.response.MergedLogsMealItem
import com.jetsynthesys.rightlife.ai_package.model.response.RegularRecipeEntry
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMeal
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourBreakfastMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourDinnerMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourEveningSnacksMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourLunchMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourMorningSnackMealLogsAdapter
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MealLogCalenderBottomSheet : BottomSheetDialogFragment() {

    private var loadingOverlay : FrameLayout? = null
    private lateinit var breakfastMealRecyclerView : RecyclerView
    private lateinit var morningSnackMealsRecyclerView : RecyclerView
    private lateinit var lunchMealRecyclerView : RecyclerView
    private lateinit var eveningSnacksMealRecyclerView : RecyclerView
    private lateinit var dinnerMealRecyclerView : RecyclerView
    private lateinit var noMealLogsLayout : LinearLayoutCompat
    private lateinit var breakfastListLayout : ConstraintLayout
    private lateinit var morningSnackListLayout : ConstraintLayout
    private lateinit var lunchListLayout : ConstraintLayout
    private lateinit var eveningSnacksListLayout : ConstraintLayout
    private lateinit var dinnerListLayout : ConstraintLayout
    private lateinit var calValueTv : TextView
    private lateinit var calValueMorningSnackTv : TextView
    private lateinit var calValueLunchTv : TextView
    private lateinit var calValueEveningSnacksTv : TextView
    private lateinit var calValueDinnerTv : TextView
    private val breakfastCombinedList = ArrayList<MergedLogsMealItem>()
    private val morningSnackCombinedList = ArrayList<MergedLogsMealItem>()
    private val lunchCombinedList = ArrayList<MergedLogsMealItem>()
    private val eveningSnacksCombinedList = ArrayList<MergedLogsMealItem>()
    private val dinnerCombinedList = ArrayList<MergedLogsMealItem>()
    private val breakfastMealNutritionSummary = ArrayList<MealNutritionSummary>()
    private val morningSnackMealNutritionSummary = ArrayList<MealNutritionSummary>()
    private val lunchMealNutritionSummary = ArrayList<MealNutritionSummary>()
    private val eveningSnacksMealNutritionSummary = ArrayList<MealNutritionSummary>()
    private val dinnerMealNutritionSummary = ArrayList<MealNutritionSummary>()
    private var breakFastMealDetailsLog : MealDetailsLog? = null
    private var morningSnackMealDetailsLog : MealDetailsLog? = null
    private var lunchMealDetailsLog : MealDetailsLog? = null
    private var eveningSnackMealDetailsLog : MealDetailsLog? = null
    private var dinnerMealDetailsLog : MealDetailsLog? = null

    private val breakfastMealLogsAdapter by lazy { YourBreakfastMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null, false, ::onBreakFastRegularRecipeDeleteItem,
        :: onBreakFastRegularRecipeEditItem, :: onBreakFastSnapMealDeleteItem, :: onBreakFastSnapMealEditItem, true, true) }
    private val morningSnackMealLogsAdapter by lazy { YourMorningSnackMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null,false, :: onMSRegularRecipeDeleteItem, :: onMSRegularRecipeEditItem,
        :: onMSSnapMealDeleteItem, :: onMSSnapMealEditItem, true, true) }
    private val lunchMealLogsAdapter by lazy { YourLunchMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null,false, :: onLunchRegularRecipeDeleteItem, :: onLunchRegularRecipeEditItem,
        :: onLunchSnapMealDeleteItem, :: onLunchSnapMealEditItem, true, true) }
    private val eveningSnacksMealLogsAdapter by lazy { YourEveningSnacksMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null,false, :: onESRegularRecipeDeleteItem, :: onESRegularRecipeEditItem,
        :: onESSnapMealDeleteItem, :: onESSnapMealEditItem, true, true) }
    private val dinnerMealLogsAdapter by lazy { YourDinnerMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null,false, :: onDinnerRegularRecipeDeleteItem, :: onDinnerRegularRecipeEditItem,
        :: onDinnerSnapMealDeleteItem, :: onDinnerSnapMealEditItem, true, true) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_meal_log_calender_bottom_sheet, container, false)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val dialog = BottomSheetDialog(requireContext(), R.style.LoggedBottomSheetDialogTheme)
        dialog.setContentView(R.layout.fragment_frequently_logged)
        dialog.window?.setBackgroundDrawableResource(transparent)
        val title = view.findViewById<TextView>(R.id.title)
        val close = view.findViewById<ImageView>(R.id.close)
        noMealLogsLayout = view.findViewById(R.id.noMealLogsLayout)
        breakfastListLayout = view.findViewById(R.id.layout_breakfast_list)
        morningSnackListLayout = view.findViewById(R.id.layoutMorningSnackList)
        lunchListLayout = view.findViewById(R.id.layout_lunch_list)
        eveningSnacksListLayout = view.findViewById(R.id.layout_eveningSnacks_list)
        dinnerListLayout = view.findViewById(R.id.layout_dinner_list)
        calValueTv = view.findViewById(R.id.tv_cal_value)
        calValueMorningSnackTv = view.findViewById(R.id.tvCalValueMorningSnack)
        calValueLunchTv = view.findViewById(R.id.tv_lunch_cal_value)
        calValueEveningSnacksTv = view.findViewById(R.id.tv_cal_valueeveningSnacks)
        calValueDinnerTv = view.findViewById(R.id.tv_dinner_cal_value)
        breakfastMealRecyclerView = view.findViewById(R.id.recyclerview_breakfast_meals_item)
        morningSnackMealsRecyclerView = view.findViewById(R.id.recyclerviewMorningSnackMealsItem)
        lunchMealRecyclerView = view.findViewById(R.id.recyclerview_lunch_meals_item)
        eveningSnacksMealRecyclerView = view.findViewById(R.id.recyclerview_eveningSnacks_meals_item)
        dinnerMealRecyclerView = view.findViewById(R.id.recyclerview_dinner_meals_item)

        breakfastMealRecyclerView.layoutManager = LinearLayoutManager(context)
        breakfastMealRecyclerView.adapter = breakfastMealLogsAdapter

        morningSnackMealsRecyclerView.layoutManager = LinearLayoutManager(context)
        morningSnackMealsRecyclerView.adapter = morningSnackMealLogsAdapter

        lunchMealRecyclerView.layoutManager = LinearLayoutManager(context)
        lunchMealRecyclerView.adapter = lunchMealLogsAdapter

        eveningSnacksMealRecyclerView.layoutManager = LinearLayoutManager(context)
        eveningSnacksMealRecyclerView.adapter = eveningSnacksMealLogsAdapter

        dinnerMealRecyclerView.layoutManager = LinearLayoutManager(context)
        dinnerMealRecyclerView.adapter = dinnerMealLogsAdapter

        title.text = "Your Meal Log"
        val selectedDate = arguments?.getString("SelectedDate").toString()

        if (selectedDate != "null" && selectedDate != ""){
            getMealsLogList(selectedDate)
        }

        close.setOnClickListener {
            dismiss()
        }
    }

    private fun getMealsLogList(formattedDate: String) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
            val call = ApiClient.apiServiceFastApiV2.getMealsLogByDate(userId, formattedDate)
            call.enqueue(object : Callback<MealLogDataResponse> {
                override fun onResponse(call: Call<MealLogDataResponse>, response: Response<MealLogDataResponse>) {
                    if (response.isSuccessful) {
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                        if (response.body()?.data != null){
                            val breakfastRecipes = response.body()?.data!!.meal_detail["breakfast"]?.regular_receipes
                            val morningSnackRecipes = response.body()?.data!!.meal_detail["morning_snack"]?.regular_receipes
                            val lunchSnapRecipes = response.body()?.data!!.meal_detail["lunch"]?.regular_receipes
                            val eveningSnacksRecipes = response.body()?.data!!.meal_detail["evening_snack"]?.regular_receipes
                            val dinnerRecipes = response.body()?.data!!.meal_detail["dinner"]?.regular_receipes

                            val breakfastSnapMeals = response.body()?.data!!.meal_detail["breakfast"]?.snap_meals
                            val morningSnackSnapMeals = response.body()?.data!!.meal_detail["morning_snack"]?.snap_meals
                            val lunchSnapSnapMeals = response.body()?.data!!.meal_detail["lunch"]?.snap_meals
                            val eveningSnacksSnapMeals = response.body()?.data!!.meal_detail["evening_snack"]?.snap_meals
                            val dinnerSnapMeals = response.body()?.data!!.meal_detail["dinner"]?.snap_meals

                            val breakfastMealNutrition = response.body()?.data!!.meal_detail["breakfast"]?.meal_nutrition_summary
                            val morningSnackMealNutrition = response.body()?.data!!.meal_detail["morning_snack"]?.meal_nutrition_summary
                            val lunchMealNutrition = response.body()?.data!!.meal_detail["lunch"]?.meal_nutrition_summary
                            val eveningSnacksMealNutrition = response.body()?.data!!.meal_detail["evening_snack"]?.meal_nutrition_summary
                            val dinnerMealNutrition = response.body()?.data!!.meal_detail["dinner"]?.meal_nutrition_summary
                            val breakFastViewItem = response.body()?.data!!.meal_detail["breakfast"]
                            val morningSnackViewItem = response.body()?.data!!.meal_detail["morning_snack"]
                            val lunchViewItem = response.body()?.data!!.meal_detail["lunch"]
                            val eveningSnackViewItem = response.body()?.data!!.meal_detail["evening_snack"]
                            val dinnerViewItem = response.body()?.data!!.meal_detail["dinner"]
                            if (breakFastViewItem != null){
                                breakFastMealDetailsLog = breakFastViewItem
                            }
                            if (morningSnackViewItem != null){
                                morningSnackMealDetailsLog = morningSnackViewItem
                            }
                            if (lunchViewItem != null){
                                lunchMealDetailsLog = lunchViewItem
                            }
                            if (eveningSnackViewItem != null){
                                eveningSnackMealDetailsLog = eveningSnackViewItem
                            }
                            if (dinnerViewItem != null){
                                dinnerMealDetailsLog = dinnerViewItem
                            }
                            breakfastCombinedList.clear()
                            morningSnackCombinedList.clear()
                            lunchCombinedList.clear()
                            eveningSnacksCombinedList.clear()
                            dinnerCombinedList.clear()
                            breakfastMealNutritionSummary.clear()
                            morningSnackMealNutritionSummary.clear()
                            lunchMealNutritionSummary.clear()
                            eveningSnacksMealNutritionSummary.clear()
                            dinnerMealNutritionSummary.clear()

                            if (breakfastMealNutrition != null) {
                                if (breakfastMealNutrition.size > 0) {
                                    breakfastMealNutritionSummary.addAll(breakfastMealNutrition)
                                }
                            }

                            if (morningSnackMealNutrition != null) {
                                if (morningSnackMealNutrition.size > 0) {
                                    morningSnackMealNutritionSummary.addAll(morningSnackMealNutrition)
                                }
                            }

                            if (lunchMealNutrition != null) {
                                if (lunchMealNutrition.size > 0) {
                                    lunchMealNutritionSummary.addAll(lunchMealNutrition)
                                }
                            }

                            if (eveningSnacksMealNutrition != null) {
                                if (eveningSnacksMealNutrition.size > 0) {
                                    eveningSnacksMealNutritionSummary.addAll(eveningSnacksMealNutrition)
                                }
                            }

                            if (dinnerMealNutrition != null) {
                                if (dinnerMealNutrition.size > 0) {
                                    dinnerMealNutritionSummary.addAll(dinnerMealNutrition)
                                }
                            }

                            if (breakfastRecipes != null){
                                if (breakfastRecipes.size > 0){
                                    breakfastCombinedList.addAll(breakfastRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                                }
                            }
                            if (breakfastSnapMeals != null){
                                if (breakfastSnapMeals.size > 0){
                                    breakfastCombinedList.addAll(breakfastSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                                }
                            }
                            if (morningSnackRecipes != null){
                                if (morningSnackRecipes.size > 0){
                                    morningSnackCombinedList.addAll(morningSnackRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                                }
                            }
                            if (morningSnackSnapMeals != null){
                                if (morningSnackSnapMeals.size > 0){
                                    morningSnackCombinedList.addAll(morningSnackSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                                }
                            }
                            if (lunchSnapRecipes != null){
                                if (lunchSnapRecipes.size > 0){
                                    lunchCombinedList.addAll(lunchSnapRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                                }
                            }
                            if (lunchSnapSnapMeals != null){
                                if (lunchSnapSnapMeals.size > 0){
                                    lunchCombinedList.addAll(lunchSnapSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                                }
                            }
                            if (eveningSnacksRecipes != null){
                                if (eveningSnacksRecipes.size > 0){
                                    eveningSnacksCombinedList.addAll(eveningSnacksRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                                }
                            }
                            if (eveningSnacksSnapMeals != null){
                                if (eveningSnacksSnapMeals.size > 0){
                                    eveningSnacksCombinedList.addAll(eveningSnacksSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                                }
                            }
                            if (dinnerRecipes != null) {
                                if (dinnerRecipes.size > 0){
                                    dinnerCombinedList.addAll(dinnerRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                                }
                            }
                            if (dinnerSnapMeals != null){
                                if (dinnerSnapMeals.size > 0){
                                    dinnerCombinedList.addAll(dinnerSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                                }
                            }
                            val fullDaySummary = response.body()?.data!!.full_day_summary

                            requireActivity()?.runOnUiThread {
                                if (fullDaySummary.calories_kcal != null){
                                    noMealLogsLayout.visibility = View.GONE
                                   // dailyCalorieGraphLayout.visibility = View.VISIBLE
                                   // setGraphValue(fullDaySummary)
                                    if (response.body()?.data!!.meal_detail.isNotEmpty()){
                                        setDayLogsList()
                                    }
                                }else{
                                    noMealLogsLayout.visibility = View.VISIBLE
                                   // dailyCalorieGraphLayout.visibility = View.GONE
                                    breakfastListLayout.visibility = View.GONE
                                    morningSnackListLayout.visibility = View.GONE
                                    lunchListLayout.visibility = View.GONE
                                    eveningSnacksListLayout.visibility = View.GONE
                                    dinnerListLayout.visibility = View.GONE
                                }
                            }
                        }
                    } else {
                        Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                        activity?.runOnUiThread {
                            Toast.makeText(activity, "No meal logs found for this user", Toast.LENGTH_SHORT).show()
                           // dailyCalorieGraphLayout.visibility = View.GONE
                            breakfastListLayout.visibility = View.GONE
                            morningSnackListLayout.visibility = View.GONE
                            lunchListLayout.visibility = View.GONE
                            eveningSnacksListLayout.visibility = View.GONE
                            dinnerListLayout.visibility = View.GONE
                            noMealLogsLayout.visibility = View.VISIBLE
                            if (isAdded  && view != null){
                                requireActivity().runOnUiThread {
                                    dismissLoader(requireView())
                                }
                            }
                        }
                    }
                }
                override fun onFailure(call: Call<MealLogDataResponse>, t: Throwable) {
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
    }

    private fun setDayLogsList(){
        val regularRecipeData : RegularRecipeEntry? = null
        val snapMealData : SnapMeal? = null
        activity?.runOnUiThread {
            if (breakfastCombinedList.size > 0) {
                breakfastListLayout.visibility = View.VISIBLE
                if (breakfastMealNutritionSummary.size > 0) {
                    calValueTv.text =
                        breakfastMealNutritionSummary.get(0).calories_kcal.toInt().toString()
                }
                breakfastMealLogsAdapter.updateList(breakfastCombinedList, -1, regularRecipeData, snapMealData, false)
            } else {
                breakfastListLayout.visibility = View.GONE
            }
        }
        activity?.runOnUiThread {
            if (morningSnackCombinedList.size > 0) {
                morningSnackListLayout.visibility = View.VISIBLE
                morningSnackMealLogsAdapter.addAll(morningSnackCombinedList, -1, regularRecipeData, snapMealData, false)
                if (morningSnackMealNutritionSummary.size > 0) {
                    calValueMorningSnackTv.text =
                        morningSnackMealNutritionSummary.get(0).calories_kcal.toInt().toString()
                }
            } else {
                morningSnackListLayout.visibility = View.GONE
            }
        }
        activity?.runOnUiThread {
            if (lunchCombinedList.size > 0) {
                lunchListLayout.visibility = View.VISIBLE
                lunchMealLogsAdapter.addAll(lunchCombinedList, -1, regularRecipeData, snapMealData, false)
                if (lunchMealNutritionSummary.size > 0) {
                    calValueLunchTv.text =
                        lunchMealNutritionSummary.get(0).calories_kcal.toInt().toString()
                }
            } else {
                lunchListLayout.visibility = View.GONE
            }
        }
        activity?.runOnUiThread {
            if (eveningSnacksCombinedList.size > 0) {
                eveningSnacksListLayout.visibility = View.VISIBLE
                eveningSnacksMealLogsAdapter.addAll(eveningSnacksCombinedList, -1, regularRecipeData, snapMealData, false)
                if (eveningSnacksMealNutritionSummary.size > 0) {
                    calValueEveningSnacksTv.text =
                        eveningSnacksMealNutritionSummary.get(0).calories_kcal.toInt().toString()
                }
            } else {
                eveningSnacksListLayout.visibility = View.GONE
            }
        }
        activity?.runOnUiThread {
            if  (dinnerCombinedList.size > 0){
                dinnerListLayout.visibility = View.VISIBLE
                dinnerMealLogsAdapter.addAll(dinnerCombinedList, -1, regularRecipeData, snapMealData, false)
                if (dinnerMealNutritionSummary.size > 0){
                    calValueDinnerTv.text = dinnerMealNutritionSummary.get(0).calories_kcal.toInt().toString()
                }
            }else{
                dinnerListLayout.visibility = View.GONE
            }
        }
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    private fun onBreakFastRegularRecipeDeleteItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastRegularRecipeEditItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastSnapMealDeleteItem(mealItem: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastSnapMealEditItem(mealItem: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onMSRegularRecipeDeleteItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onMSRegularRecipeEditItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onMSSnapMealDeleteItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onMSSnapMealEditItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onLunchRegularRecipeDeleteItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onLunchRegularRecipeEditItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onLunchSnapMealDeleteItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onLunchSnapMealEditItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onESRegularRecipeDeleteItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onESRegularRecipeEditItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onESSnapMealDeleteItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onESSnapMealEditItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onDinnerRegularRecipeDeleteItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onDinnerRegularRecipeEditItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onDinnerSnapMealDeleteItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onDinnerSnapMealEditItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }

    companion object {
        const val TAG = "LoggedBottomSheet"
        @JvmStatic
        fun newInstance() = MealLogCalenderBottomSheet().apply {
            arguments = Bundle().apply {
            }
        }
    }
    override fun onDetach() {
        super.onDetach()
    }
}

