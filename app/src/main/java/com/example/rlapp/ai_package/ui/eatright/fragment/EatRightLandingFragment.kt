package com.example.rlapp.ai_package.ui.eatright.fragment

import android.animation.ValueAnimator
import android.app.ProgressDialog
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.data.repository.ApiClient
import com.example.rlapp.ai_package.model.RecipeList
import com.example.rlapp.ai_package.model.RecipeResponseModel
import com.example.rlapp.ai_package.ui.eatright.adapter.MealPlanEatLandingAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.OtherRecepieEatLandingAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.tab.FrequentlyLoggedListAdapter
import com.example.rlapp.ai_package.ui.eatright.model.LandingPageResponse
import com.example.rlapp.ai_package.ui.eatright.model.MealPlanModel
import com.example.rlapp.ai_package.ui.eatright.model.MyMealModel
import com.example.rlapp.ai_package.utils.AppPreference
import com.example.rlapp.databinding.FragmentEatRightLandingBinding
import com.google.android.material.snackbar.Snackbar
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class EatRightLandingFragment : BaseFragment<FragmentEatRightLandingBinding>() {

    private lateinit var mealPlanRecyclerView: RecyclerView
    private lateinit var frequentlyLoggedRecyclerView: RecyclerView
    private lateinit var otherReciepeRecyclerView: RecyclerView
    private lateinit var todayMacrosWithDataLayout: ConstraintLayout
    private lateinit var todayMacroNoDataLayout: ConstraintLayout
    private lateinit var todayMicrosWithDataLayout: ConstraintLayout
    private lateinit var todayMacroNoDataLayoutOne: ConstraintLayout
    private lateinit var todayMealLogNoDataHeading: ConstraintLayout
    private lateinit var log_your_meal_balance_layout: CardView
    private lateinit var tv_water_quantity: TextView
    private lateinit var last_logged_no_data: TextView
    private lateinit var text_heading_calories: TextView
    private lateinit var text_heading_calories_unit: TextView
    private lateinit var other_reciepie_might_like_with_data: LinearLayout
    private lateinit var new_improvement_layout: LinearLayout
    private  var newBoolean: Boolean = false
    private lateinit var appPreference: AppPreference
    private lateinit var progressDialog: ProgressDialog
    private lateinit var landingPageResponse : LandingPageResponse

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentEatRightLandingBinding
        get() = FragmentEatRightLandingBinding::inflate

    var snackbar: Snackbar? = null
    private val mealPlanAdapter by lazy { MealPlanEatLandingAdapter(requireContext(), arrayListOf(), -1, null, false, ::onMealPlanDateItem) }
    private val otherReciepeAdapter by lazy { OtherRecepieEatLandingAdapter(requireContext(), arrayListOf(), -1, null, false, ::onOtherReciepeDateItem) }
    private val frequentlyLoggedListAdapter by lazy { FrequentlyLoggedListAdapter(requireContext(), arrayListOf(), -1, null, false, ::onFrequentlyLoggedItem) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        newBoolean = true
        appPreference = AppPreference(requireContext())
        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Loading")
        progressDialog.setCancelable(false)

        val halfCurveProgressBar = view.findViewById<HalfCurveProgressBar>(R.id.halfCurveProgressBar)
        val snapMealBtn = view.findViewById<ConstraintLayout>(R.id.lyt_snap_meal)
        val mealLogLayout = view.findViewById<LinearLayout>(R.id.layout_meal_log)
        mealPlanRecyclerView = view.findViewById(R.id.recyclerview_meal_plan_item)
        todayMacrosWithDataLayout = view.findViewById(R.id.today_macros_with_data_layout)
        todayMacroNoDataLayout = view.findViewById(R.id.today_macro_no_data_layout)
        todayMicrosWithDataLayout = view.findViewById(R.id.today_micros_with_data_layout)
        todayMacroNoDataLayoutOne = view.findViewById(R.id.today_macro_no_data_layout_one)
        todayMealLogNoDataHeading = view.findViewById(R.id.today_meal_log_no_data)
        log_your_meal_balance_layout = view.findViewById(R.id.log_your_meal_balance_layout)
        other_reciepie_might_like_with_data = view.findViewById(R.id.other_reciepie_might_like_with_data)
        tv_water_quantity = view.findViewById(R.id.tv_water_quantity)
        text_heading_calories = view.findViewById(R.id.text_heading_calories)
        last_logged_no_data = view.findViewById(R.id.last_logged_no_data)
        text_heading_calories_unit = view.findViewById(R.id.text_heading_calories_unit)
        new_improvement_layout = view.findViewById(R.id.new_improvement_layout)

        frequentlyLoggedRecyclerView = view.findViewById(R.id.recyclerview_frequently_logged_item)
        otherReciepeRecyclerView = view.findViewById(R.id.recyclerview_other_reciepe_item)
        otherReciepeRecyclerView.layoutManager = LinearLayoutManager(context)
        otherReciepeRecyclerView.adapter = otherReciepeAdapter


        getMealRecipesList()
        onOtherReciepeDateItemRefresh()
        mealPlanRecyclerView.layoutManager = LinearLayoutManager(context)
        mealPlanRecyclerView.adapter = mealPlanAdapter
        onMealPlanItemRefresh()
        frequentlyLoggedRecyclerView.layoutManager = LinearLayoutManager(context)
        frequentlyLoggedRecyclerView.adapter = frequentlyLoggedListAdapter
        onFrequentlyLoggedItemRefresh()
        halfCurveProgressBar.setProgress(50f)
        val animator = ValueAnimator.ofFloat(0f, 70f)
        animator.duration = 2000
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            halfCurveProgressBar.setProgress(value)
        }
        animator.start()
        val glassWithWaterView = view.findViewById<GlassWithWaterView>(R.id.glass_with_water_view)
        val waterIntake = 1000f
        val waterGoal = 3000f
        glassWithWaterView.setTargetWaterLevel(waterIntake, waterGoal)

        snapMealBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = MealScanResultFragment()
                val args = Bundle()
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        mealLogLayout.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = YourMealLogsFragment()
                val args = Bundle()
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }
        if(newBoolean){
            todayMacrosWithDataLayout.visibility = View.VISIBLE
            todayMacroNoDataLayout.visibility = View.GONE
            todayMicrosWithDataLayout.visibility = View.VISIBLE
            todayMacroNoDataLayoutOne.visibility = View.GONE
            todayMealLogNoDataHeading.visibility = View.GONE
            mealPlanRecyclerView.visibility =View.VISIBLE
            log_your_meal_balance_layout.visibility = View.VISIBLE
            other_reciepie_might_like_with_data.visibility = View.VISIBLE
            otherReciepeRecyclerView.visibility = View.VISIBLE
            tv_water_quantity.text = "400"
            last_logged_no_data.visibility = View.GONE
            text_heading_calories.visibility = View.VISIBLE
            text_heading_calories_unit.visibility = View.VISIBLE
            new_improvement_layout.visibility = View.VISIBLE


        }else{
            todayMacroNoDataLayout.visibility = View.VISIBLE
            todayMacrosWithDataLayout.visibility = View.GONE
            todayMicrosWithDataLayout.visibility = View.GONE
            todayMacroNoDataLayoutOne.visibility = View.VISIBLE
            todayMealLogNoDataHeading.visibility = View.VISIBLE
            mealPlanRecyclerView.visibility = View.GONE
            log_your_meal_balance_layout.visibility = View.GONE
            other_reciepie_might_like_with_data.visibility = View.GONE
            otherReciepeRecyclerView.visibility = View.GONE
            tv_water_quantity.text = "0"
            last_logged_no_data.visibility = View.VISIBLE
            text_heading_calories.visibility = View.GONE
            text_heading_calories_unit.visibility = View.GONE
            new_improvement_layout.visibility = View.GONE

        }
    }

    private fun onFrequentlyLoggedItemRefresh() {
        val meal = listOf(
            MyMealModel("Breakfast", "Poha", "1", "1,157", "8", "308", "17", true),
            MyMealModel("Breakfast", "Dal", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Rice", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Roti", "1", "1,157", "8", "308", "17", false)
        )

        if (meal.size > 0) {
            frequentlyLoggedRecyclerView.visibility = View.VISIBLE
        } else {
            frequentlyLoggedRecyclerView.visibility = View.GONE
        }

        val valueLists: ArrayList<MyMealModel> = ArrayList()
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

        val valueLists: ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        frequentlyLoggedListAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)

        val newIngredient = mealLogDateModel
        for (ingredient in valueLists) {
            if (ingredient.isAddDish == true) {
                // Handle ingredient addition if needed
            }
        }
    }

    private fun onMealPlanItemRefresh() {
        val meal = listOf(
            MealPlanModel("Breakfast", "Poha", "Vegeterian", "25", "1", "1,157", "8", "308", "17"),
            MealPlanModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian", "25", "1", "1,157", "8", "308", "17"),
            MealPlanModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian", "25", "1", "1,157", "8", "308", "17")
        )

        val valueLists: ArrayList<MealPlanModel> = ArrayList()
        valueLists.addAll(meal as Collection<MealPlanModel>)
        val mealPlanData: MealPlanModel? = null
        mealPlanAdapter.addAll(valueLists, -1, mealPlanData, false)
    }

    private fun onMealPlanDateItem(mealLogDateModel: MealPlanModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            MealPlanModel("Breakfast", "Poha", "Vegeterian", "25", "1", "1,157", "8", "308", "17"),
            MealPlanModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian", "25", "1", "1,157", "8", "308", "17"),
            MealPlanModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian", "25", "1", "1,157", "8", "308", "17")
        )

        val valueLists: ArrayList<MealPlanModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MealPlanModel>)
    }

    private fun onOtherReciepeDateItemRefresh() {
        val meal = listOf(
            MyMealModel("Breakfast", "Poha", "1", "1,157", "8", "308", "17", true),
            MyMealModel("Breakfast", "Dal", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Rice", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Roti", "1", "1,157", "8", "308", "17", false)
        )

        if (meal.size > 0) {
            frequentlyLoggedRecyclerView.visibility = View.VISIBLE
        } else {
            frequentlyLoggedRecyclerView.visibility = View.GONE
        }

        val valueLists: ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(meal as Collection<MyMealModel>)
        val mealLogDateData: MyMealModel? = null
        otherReciepeAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onOtherReciepeDateItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            MyMealModel("Breakfast", "Poha", "1", "1,157", "8", "308", "17", true),
            MyMealModel("Breakfast", "Dal", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Rice", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Roti", "1", "1,157", "8", "308", "17", false)
        )

        val valueLists: ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        otherReciepeAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)

        val newIngredient = mealLogDateModel
        for (ingredient in valueLists) {
            if (ingredient.isAddDish == true) {
                // Handle ingredient addition if needed
            }
        }
    }

    private fun getMealRecipesList() {
        progressDialog.show()
       // val userId = appPreference.getUserId().toString()
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdhNWZhZTkxOTc5OTI1MTFlNzFiMWM4Iiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiJBZGl0eWEiLCJsYXN0TmFtZSI6IlR5YWdpIiwiZGV2aWNlSWQiOiJCNkRCMTJBMy04Qjc3LTRDQzEtOEU1NC0yMTVGQ0U0RDY5QjQiLCJtYXhEZXZpY2VSZWFjaGVkIjpmYWxzZSwidHlwZSI6ImFjY2Vzcy10b2tlbiJ9LCJpYXQiOjE3MzkxNzE2NjgsImV4cCI6MTc1NDg5NjQ2OH0.koJ5V-vpGSY1Irg3sUurARHBa3fArZ5Ak66SkQzkrxM"
        val userId = "64763fe2fa0e40d9c0bc8264"
        val startDate = "2025-03-22"
        val call = ApiClient.apiServiceFastApi.getMealSummary(startDate)
        call.enqueue(object : Callback<LandingPageResponse> {
            override fun onResponse(call: Call<LandingPageResponse>, response: Response<LandingPageResponse>) {
                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    landingPageResponse = response.body()!!
                    println(landingPageResponse)
//                    val mealPlanLists = response.body()?.data ?: emptyList()
//                    recipesList.addAll(mealPlanLists)
                    setMealSummaryData(landingPageResponse)
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
            override fun onFailure(call: Call<LandingPageResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }

    private fun setMealSummaryData(landingPageResponse: LandingPageResponse){

    }
}