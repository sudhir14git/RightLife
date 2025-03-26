package com.example.rlapp.ai_package.ui.eatright.fragment

import android.app.Dialog
import android.app.ProgressDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.Window
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.data.repository.ApiClient
import com.example.rlapp.ai_package.model.DailyRecipe
import com.example.rlapp.ai_package.model.MealList
import com.example.rlapp.ai_package.model.MealLogData
import com.example.rlapp.ai_package.model.MealLogsResponseModel
import com.example.rlapp.ai_package.ui.eatright.adapter.MealLogDateListAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.YourBreakfastMealLogsAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.YourDinnerMealLogsAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.YourLunchMealLogsAdapter
import com.example.rlapp.ai_package.ui.eatright.model.BreakfastMealModel
import com.example.rlapp.ai_package.ui.eatright.model.DinnerMealModel
import com.example.rlapp.ai_package.ui.eatright.model.LunchMealModel
import com.example.rlapp.ai_package.ui.home.HomeBottomTabFragment
import com.example.rlapp.ai_package.utils.AppPreference
import com.example.rlapp.databinding.FragmentYourMealLogsBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class YourMealLogsFragment : BaseFragment<FragmentYourMealLogsBinding>() {

    private lateinit var layoutToolbar :ConstraintLayout
    private lateinit var mealLogDateRecyclerView : RecyclerView
    private lateinit var breakfastMealRecyclerView : RecyclerView
    private lateinit var lunchMealRecyclerView : RecyclerView
    private lateinit var dinnerMealRecyclerView : RecyclerView
    private lateinit var imageCalender : ImageView
    private lateinit var breakfastDotMenu : ImageView
    private lateinit var lunchDotMenu : ImageView
    private lateinit var dinnerDotMenu : ImageView
    private lateinit var backButton : ImageView
    private lateinit var btnLogMeal : LinearLayoutCompat
    private lateinit var editDeleteBreakfast : CardView
    private lateinit var editDeleteLunch : CardView
    private lateinit var editDeleteDinner : CardView
    private lateinit var layoutMain : ConstraintLayout
    private lateinit var deleteBottomSheetFragment: DeleteMealBottomSheet
    private lateinit var selectMealTypeBottomSheet: SelectMealTypeBottomSheet
    private lateinit var layoutDelete : LinearLayoutCompat
    private lateinit var layoutViewFood : LinearLayoutCompat
    private lateinit var addFoodLayout : LinearLayoutCompat
    private lateinit var appPreference: AppPreference
    private lateinit var progressDialog: ProgressDialog
    private lateinit var calValue : TextView
    private lateinit var carbsValue : TextView
    private lateinit var proteinsValue : TextView
    private lateinit var fatsValue : TextView
    private lateinit var caloriesProgressBar : ProgressBar
    private lateinit var carbsProgressBar : ProgressBar
    private lateinit var proteinsProgressBar : ProgressBar
    private lateinit var fatsProgressBar : ProgressBar
    private lateinit var transparentOverlay : View
    private var mealPlanData : ArrayList<MealLogData> = ArrayList()


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentYourMealLogsBinding
        get() = FragmentYourMealLogsBinding::inflate

    private val mealLogDateAdapter by lazy { MealLogDateListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onMealLogDateItem) }
    private val breakfastMealLogsAdapter by lazy { YourBreakfastMealLogsAdapter(requireContext(), arrayListOf(), -1, null, false, :: onBreakfastMealLogItem) }
    private val lunchMealLogsAdapter by lazy { YourLunchMealLogsAdapter(requireContext(), arrayListOf(), -1, null, false, :: onLunchMealLogItem) }
    private val dinnerMealLogsAdapter by lazy { YourDinnerMealLogsAdapter(requireContext(), arrayListOf(), -1, null, false, :: onDinnerMealLogItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        appPreference = AppPreference(requireContext())

    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appPreference = AppPreference(requireContext())
        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Loading")
        progressDialog.setCancelable(false)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        mealLogDateRecyclerView = view.findViewById(R.id.recyclerview_calender)
        breakfastMealRecyclerView = view.findViewById(R.id.recyclerview_breakfast_meals_item)
        lunchMealRecyclerView = view.findViewById(R.id.recyclerview_lunch_meals_item)
        dinnerMealRecyclerView = view.findViewById(R.id.recyclerview_dinner_meals_item)
        imageCalender = view.findViewById(R.id.image_calender)
        btnLogMeal = view.findViewById(R.id.layout_btn_log_meal)
        breakfastDotMenu = view.findViewById(R.id.image_dot_menu)
        lunchDotMenu = view.findViewById(R.id.image_lunch_dot_menu)
        dinnerDotMenu = view.findViewById(R.id.image_dinner_dot_menu)
        editDeleteBreakfast = view.findViewById(R.id.btn_edit_delete)
        editDeleteLunch = view.findViewById(R.id.btn_edit_delete_lunch)
        editDeleteDinner = view.findViewById(R.id.btn_edit_delete_dinner)
        layoutMain = view.findViewById(R.id.layout_main)
        layoutDelete = view.findViewById(R.id.layout_delete)
        layoutViewFood = view.findViewById(R.id.layout_view_food)
        calValue = view.findViewById(R.id.tv_meal_cal_value)
        carbsValue = view.findViewById(R.id.tv_subtraction_cal_value)
        proteinsValue = view.findViewById(R.id.tv_baguette_cal_value)
        fatsValue = view.findViewById(R.id.tv_dewpoint_cal_value)
        caloriesProgressBar = view.findViewById(R.id.cal_progressBar)
        carbsProgressBar = view.findViewById(R.id.carbs_progressBar)
        proteinsProgressBar = view.findViewById(R.id.protien_progressBar)
        fatsProgressBar = view.findViewById(R.id.fats_progressBar)
        backButton = view.findViewById(R.id.backButton)
        addFoodLayout = view.findViewById(R.id.layout_add_food)

        layoutToolbar = view.findViewById(R.id.layoutToolbar)
        transparentOverlay = view.findViewById(R.id.transparentOverlay)

        getMealPlanList()

        mealLogDateRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mealLogDateRecyclerView.adapter = mealLogDateAdapter

        breakfastMealRecyclerView.layoutManager = LinearLayoutManager(context)
        breakfastMealRecyclerView.adapter = breakfastMealLogsAdapter

        lunchMealRecyclerView.layoutManager = LinearLayoutManager(context)
        lunchMealRecyclerView.adapter = lunchMealLogsAdapter

        dinnerMealRecyclerView.layoutManager = LinearLayoutManager(context)
        dinnerMealRecyclerView.adapter = dinnerMealLogsAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = HomeBottomTabFragment()
                val args = Bundle()
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        })

        onBreakfastMealLogItemRefresh()
        onLunchMealLogItemRefresh()
        onDinnerMealLogItemRefresh()

        //Set Tooltip
        showTooltipDialog(layoutToolbar)

        imageCalender.setOnClickListener {
          //  showTooltipDialog( layoutToolbar,"You can access Calender \n view from here.")
            val fragment = MealLogCalenderFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }

//            val changePage = Intent(context, Main2Activity::class.java)
//            startActivity(changePage)
        }

        backButton.setOnClickListener {
            val fragment = HomeBottomTabFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        btnLogMeal.setOnClickListener {
//            val fragment = HomeTabMealFragment()
//            val args = Bundle()
//            fragment.arguments = args
//            requireActivity().supportFragmentManager.beginTransaction().apply {
//                replace(R.id.flFragment, fragment, "mealLog")
//                addToBackStack("mealLog")
//                commit()
//            }
            selectMealTypeBottomSheet = SelectMealTypeBottomSheet()
            selectMealTypeBottomSheet.isCancelable = true
            val bundle = Bundle()
            bundle.putBoolean("test",false)
            selectMealTypeBottomSheet.arguments = bundle
            activity?.supportFragmentManager?.let { selectMealTypeBottomSheet.show(it, "SelectMealTypeBottomSheet") }
        }

        breakfastDotMenu.setOnClickListener {
            if (editDeleteBreakfast.visibility == View.GONE){
                //layoutMain.setBackgroundColor(Color.parseColor("#0A1214"))
                editDeleteBreakfast.visibility = View.VISIBLE
            }else{
               // layoutMain.setBackgroundColor(Color.parseColor("#F0FFFA"))
                editDeleteBreakfast.visibility = View.GONE
            }
        }

        addFoodLayout.setOnClickListener {
//            val fragment = ViewMealInsightsFragment()
//            val args = Bundle()
//            fragment.arguments = args
//            requireActivity().supportFragmentManager.beginTransaction().apply {
//                replace(R.id.flFragment, fragment, "viewMeal")
//                addToBackStack("viewMeal")
//                commit()
//            }
        }

        lunchDotMenu.setOnClickListener {
            if (editDeleteLunch.visibility == View.GONE){
              //  layoutMain.setBackgroundColor(Color.parseColor("#0A1214"))
                editDeleteLunch.visibility = View.VISIBLE
            }else{
              //  layoutMain.setBackgroundColor(Color.parseColor("#F0FFFA"))
                editDeleteLunch.visibility = View.GONE
            }
        }

        dinnerDotMenu.setOnClickListener {
            if (editDeleteDinner.visibility == View.GONE){
              //  layoutMain.setBackgroundColor(Color.parseColor("#0A1214"))
                editDeleteDinner.visibility = View.VISIBLE
            }else{
              //  layoutMain.setBackgroundColor(Color.parseColor("#F0FFFA"))
                editDeleteDinner.visibility = View.GONE
            }
        }

        layoutDelete.setOnClickListener {
            deleteMealDialog()
        }

        layoutViewFood.setOnClickListener {
            val fragment = ViewMealInsightsFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "viewMeal")
                addToBackStack("viewMeal")
                commit()
            }
        }
    }

    private fun onMealLogDateItemRefresh (){

        val recipesLists : ArrayList<DailyRecipe> = ArrayList()
        if (mealPlanData.get(0).recipes != null){
            recipesLists.addAll(mealPlanData.get(0).recipes as Collection<DailyRecipe>)
            val mealLogDateData: DailyRecipe? = null
            mealLogDateAdapter.addAll(recipesLists, -1, mealLogDateData, false)
        }else{

        }
    }

    private fun onBreakfastMealLogItemRefresh() {
        val mealLogs = listOf(
            BreakfastMealModel("Breakfast", "Poha", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            BreakfastMealModel("Breakfast", "Apple", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
        )
        val valueLists : ArrayList<BreakfastMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<BreakfastMealModel>)
        val breakfastMealData: BreakfastMealModel? = null
       // breakfastMealLogsAdapter.addAll(valueLists, -1, breakfastMealData, false)
    }

    private fun onLunchMealLogItemRefresh() {
        val mealLogs = listOf(
            LunchMealModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            LunchMealModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )
        val valueLists : ArrayList<LunchMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<LunchMealModel>)
        val lunchMealData: LunchMealModel? = null
    //    lunchMealLogsAdapter.addAll(valueLists, -1, lunchMealData, false)
    }

    private fun onDinnerMealLogItemRefresh() {
        val mealLogs = listOf(
            DinnerMealModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            DinnerMealModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )
        val valueLists : ArrayList<DinnerMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<DinnerMealModel>)
        val dinnerMealData: DinnerMealModel? = null
       // dinnerMealLogsAdapter.addAll(valueLists, -1, dinnerMealData, false)
    }

    private fun onMealLogDateItem(dailyRecipe: DailyRecipe, position: Int, isRefresh: Boolean) {

        val recipesLists : ArrayList<DailyRecipe> = ArrayList()
        recipesLists.addAll(mealPlanData.get(0).recipes as Collection<DailyRecipe>)
        mealLogDateAdapter.addAll(recipesLists, position, dailyRecipe, isRefresh)

        setGraphValue(dailyRecipe)
    }

    private fun setGraphValue(dailyRecipe: DailyRecipe){

        calValue.text = dailyRecipe.calories.toInt().toString()
        carbsValue.text = dailyRecipe.carbs.toInt().toString()
        proteinsValue.text = dailyRecipe.proteins.toInt().toString()
        fatsValue.text = dailyRecipe.fats.toInt().toString()

        caloriesProgressBar.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                caloriesProgressBar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val progressBarWidth = caloriesProgressBar.width.toFloat()
                val overlayPosition = 0.7f * progressBarWidth
                val progress = dailyRecipe.calories.toInt()
                val max = caloriesProgressBar.max
                val circlePosition = (progress.toFloat() / max) * progressBarWidth
//                val circleRadius = circleIndicator.width / 2f
//                circleIndicator.x = circlePosition - circleRadius
//                circleIndicator.y = progressBar.y + (progressBar.height - circleIndicator.height) / 2f
                val overlayRadius = transparentOverlay.width / 2f
                transparentOverlay.x = overlayPosition - overlayRadius
                transparentOverlay.y = caloriesProgressBar.y + (caloriesProgressBar.height - transparentOverlay.height) / 2f
            }
        })
        // Set progress programmatically
        carbsProgressBar.max = 84  // Set maximum value
        carbsProgressBar.progress = dailyRecipe.carbs.toInt()

        // Set progress programmatically
        proteinsProgressBar.max = 500  // Set maximum value
        proteinsProgressBar.progress = dailyRecipe.proteins.toInt()

        // Set progress programmatically
        fatsProgressBar.max = 65  // Set maximum value
        fatsProgressBar.progress = dailyRecipe.fats.toInt()

        // Group meals by mealType
        val groupTourList = dailyRecipe.meals.groupBy { it.mealType }
        val value = groupTourList.values
        val key = groupTourList.keys

        val valueLists : ArrayList<ArrayList<MealList>> = ArrayList()
        valueLists.addAll(value as Collection<java.util.ArrayList<MealList>>)
        val mealList: MealList? = null
        for (item in key){
            if (item.contentEquals("breakfast")){
                breakfastMealLogsAdapter.addAll(valueLists.get(0), -1, mealList, false)
            }else if (item.contentEquals("lunch")){
                lunchMealLogsAdapter.addAll(valueLists.get(1), -1, mealList, false)
            }else if  (item.contentEquals("dinner")){
                dinnerMealLogsAdapter.addAll(valueLists.get(2), -1, mealList, false)
            }
        }
    }

    private fun onBreakfastMealLogItem(mealLogDateModel: BreakfastMealModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            BreakfastMealModel("Breakfast", "Poha", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            BreakfastMealModel("Breakfast", "Apple", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
        )
        val valueLists : ArrayList<BreakfastMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<BreakfastMealModel>)
      //  breakfastMealLogsAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun onLunchMealLogItem(mealLogDateModel: LunchMealModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            LunchMealModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            LunchMealModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )
        val valueLists : ArrayList<LunchMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<LunchMealModel>)
       // lunchMealLogsAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun onDinnerMealLogItem(mealLogDateModel: DinnerMealModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            LunchMealModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            DinnerMealModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )
        val valueLists : ArrayList<DinnerMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<DinnerMealModel>)
      //  dinnerMealLogsAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun deleteMealDialog(){

        deleteBottomSheetFragment = DeleteMealBottomSheet()
        deleteBottomSheetFragment.isCancelable = true
        val bundle = Bundle()
        bundle.putBoolean("test",false)
        deleteBottomSheetFragment.arguments = bundle
        activity?.supportFragmentManager?.let { deleteBottomSheetFragment.show(it, "DeleteMealBottomSheet") }
    }

    private fun showTooltipDialog(anchorView: View) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.tooltip_layout)
        val tvTooltip = dialog.findViewById<TextView>(R.id.tvTooltipText)
        tvTooltip.text = "You can access Calender \n view from here."
        // Set transparent background for rounded tooltip
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        // Get screen dimensions
        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        // Get anchor view position
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        // Tooltip width (adjust as needed)
        val tooltipWidth = 250
        // Set dialog position
        val params = dialog.window?.attributes
        // Align tooltip to the **right** of the button
        params?.x = (location[0] + anchorView.width) + tooltipWidth
        // Position tooltip **above** the button
        params?.y = location[1] - anchorView.height + 15  // Add some spacing
        dialog.window?.attributes = params
        dialog.window?.setGravity(Gravity.TOP)
        // Show the dialog
        dialog.show()
        // Auto dismiss after 3 seconds
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 3000)
    }

    private fun getMealPlanList() {
        progressDialog.show()
        val userId = appPreference.getUserId().toString()
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdhNWZhZTkxOTc5OTI1MTFlNzFiMWM4Iiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiJBZGl0eWEiLCJsYXN0TmFtZSI6IlR5YWdpIiwiZGV2aWNlSWQiOiJCNkRCMTJBMy04Qjc3LTRDQzEtOEU1NC0yMTVGQ0U0RDY5QjQiLCJtYXhEZXZpY2VSZWFjaGVkIjpmYWxzZSwidHlwZSI6ImFjY2Vzcy10b2tlbiJ9LCJpYXQiOjE3MzkxNzE2NjgsImV4cCI6MTc1NDg5NjQ2OH0.koJ5V-vpGSY1Irg3sUurARHBa3fArZ5Ak66SkQzkrxM"
        val call = ApiClient.apiService.getMealLogLists(token)
        call.enqueue(object : Callback<MealLogsResponseModel> {
            override fun onResponse(call: Call<MealLogsResponseModel>, response: Response<MealLogsResponseModel>) {
                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    val mealPlanLists = response.body()?.data ?: emptyList()
                    mealPlanData.addAll(mealPlanLists)
                    onMealLogDateItemRefresh()
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
            override fun onFailure(call: Call<MealLogsResponseModel>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }
}