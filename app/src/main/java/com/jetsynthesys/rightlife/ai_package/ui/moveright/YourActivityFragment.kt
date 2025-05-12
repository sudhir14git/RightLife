package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.app.Dialog
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
import android.view.Window
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.ActivityModel
import com.jetsynthesys.rightlife.ai_package.model.DeleteCalorieResponse
import com.jetsynthesys.rightlife.ai_package.model.UpdateCalorieRequest
import com.jetsynthesys.rightlife.ai_package.model.UpdateCalorieResponse
import com.jetsynthesys.rightlife.ai_package.model.WorkoutWeeklyDayModel
import com.jetsynthesys.rightlife.ai_package.model.response.WorkoutHistoryResponse
import com.jetsynthesys.rightlife.ai_package.model.response.WorkoutRecord
import com.jetsynthesys.rightlife.ai_package.ui.adapter.YourActivitiesAdapter
import com.jetsynthesys.rightlife.ai_package.ui.adapter.YourActivitiesWeeklyListAdapter
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.utils.LoaderUtil
import com.jetsynthesys.rightlife.databinding.FragmentYourActivityBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class YourActivityFragment : BaseFragment<FragmentYourActivityBinding>() {
    private lateinit var usernameReset: EditText
    private lateinit var signupConfirm: TextView
    private lateinit var activitySync: ImageView
    private lateinit var yourActivityBackButton: ImageView
    private lateinit var confirmResetBtn: AppCompatButton
    private lateinit var progressBarConfirmation: ProgressBar
    private lateinit var mealLogDateListAdapter: RecyclerView
    private lateinit var myActivityRecyclerView: RecyclerView
    private lateinit var imageCalender: ImageView
    private lateinit var btnLogMeal: LinearLayoutCompat
    private lateinit var workoutDateTv : TextView
    private lateinit var nextWeekBtn : ImageView
    private lateinit var prevWeekBtn : ImageView
    private lateinit var layout_btn_addWorkout: LinearLayoutCompat
    private lateinit var healthConnectSyncButton: LinearLayoutCompat
    private var workoutWeeklyDayList : List<WorkoutWeeklyDayModel> = ArrayList()
    private var currentWeekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)
    private var workoutHistoryResponse : WorkoutHistoryResponse? = null
    private var  workoutLogHistory :  ArrayList<WorkoutRecord> = ArrayList()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentYourActivityBinding
        get() = FragmentYourActivityBinding::inflate
    var snackbar: Snackbar? = null

    private val yourActivitiesWeeklyListAdapter by lazy { YourActivitiesWeeklyListAdapter(requireContext(), arrayListOf(), -1,
        null, false, ::onWorkoutLogDateItem) }

    private val myActivityAdapter by lazy {
        YourActivitiesAdapter(
            requireContext(),
            arrayListOf(),
            -1,
            null,
            false,
            ::onWorkoutItemClick,
            onCirclePlusClick = { activityModel, position ->
                val fragment = AddWorkoutSearchFragment()
                val args = Bundle().apply {
                    putParcelable("ACTIVITY_MODEL", activityModel)
                   putString("edit","edit")
                }
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "workoutDetails")
                    addToBackStack("workoutDetails")
                    commit()
                }
                //showActivityDetailsDialog(activityModel, position)
//                println(activityModel)
//                println(position)
            }
        )
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundResource(R.drawable.gradient_color_background_workout)

        mealLogDateListAdapter = view.findViewById(R.id.recyclerview_calender)
        myActivityRecyclerView = view.findViewById(R.id.recyclerview_my_meals_item)
        imageCalender = view.findViewById(R.id.image_calender)
        btnLogMeal = view.findViewById(R.id.layout_btn_log_meal)
        activitySync = view.findViewById(R.id.activities_sync)
        healthConnectSyncButton = view.findViewById(R.id.health_connect_sync_button)
        yourActivityBackButton = view.findViewById(R.id.back_button)
        layout_btn_addWorkout = view.findViewById(R.id.layout_btn_addWorkout)
        workoutDateTv = view.findViewById(R.id.workoutDateTv)
        nextWeekBtn = view.findViewById(R.id.nextWeekBtn)
        prevWeekBtn = view.findViewById(R.id.prevWeekBtn)

        layout_btn_addWorkout.setOnClickListener {
            val fragment = SearchWorkoutFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "searchWorkoutFragment")
                addToBackStack("searchWorkoutFragment")
                commit()
            }
        }

        yourActivityBackButton.setOnClickListener {
            val fragment = HomeBottomTabFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        myActivityRecyclerView.layoutManager = LinearLayoutManager(context)
        myActivityRecyclerView.adapter = myActivityAdapter

        showTooltipsSequentially()

        activitySync.setOnClickListener {
            val bottomSheet = ActivitySyncBottomSheet()
            bottomSheet.show(parentFragmentManager, "ActivitySyncBottomSheet")
        }

        healthConnectSyncButton.setOnClickListener {

            // AddWorkoutSearchFragment navigation (commented as per original)
        }

        mealLogDateListAdapter.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mealLogDateListAdapter.adapter = yourActivitiesWeeklyListAdapter

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
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

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val formatFullDate = DateTimeFormatter.ofPattern("E, d MMM yyyy")
        workoutDateTv.text = currentDateTime.format(formatFullDate)
        getWorkoutLogHistory(formattedDate)

        workoutWeeklyDayList = getWeekFrom(currentWeekStart)
        onWorkoutLogWeeklyDayList(workoutWeeklyDayList, workoutLogHistory)
        prevWeekBtn.setOnClickListener {
            currentWeekStart = currentWeekStart.minusWeeks(1)
            workoutWeeklyDayList = getWeekFrom(currentWeekStart)
            onWorkoutLogWeeklyDayList(workoutWeeklyDayList, workoutLogHistory)
            getWorkoutLogHistory(currentWeekStart.toString())
        }
        nextWeekBtn.setOnClickListener {
            currentWeekStart = currentWeekStart.plusWeeks(1)
            workoutWeeklyDayList = getWeekFrom(currentWeekStart)
            onWorkoutLogWeeklyDayList(workoutWeeklyDayList, workoutLogHistory)
            getWorkoutLogHistory(currentWeekStart.toString())
        }
        //fetchActivities()
        fetchCalories(formattedDate)

        imageCalender.setOnClickListener {
            val fragment = ActivitySyncCalenderFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        btnLogMeal.setOnClickListener {
            val fragment = CreateRoutineFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "CreateRoutineFragment")
                addToBackStack("CreateRoutineFragment")
                commit()
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun fetchCalories(formattedDate: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                    ?: "64763fe2fa0e40d9c0bc8264"
               // Log.d("FetchCalories", "Fetching calories for userId: $userId, startDate: $startDate, endDate: $endDate")
                val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                val response = ApiClient.apiServiceFastApi.getCalories(
                    userId = userId,
                    startDate = formattedDate,
                    endDate = formattedDate,
                    page = 1,
                    limit = 10,
                    includeStats = false
                )

                if (response.isSuccessful) {
                    val caloriesResponse = response.body()
                    Log.d("FetchCalories", "Received ${caloriesResponse?.data?.size ?: 0} workouts")

                    val activityList = ArrayList<ActivityModel>()
                    activityList.clear()

                    caloriesResponse?.data?.forEachIndexed { index, workout ->
                        Log.d("FetchCalories", "Workout $index - ${workout.workoutType}, Duration: ${workout.duration}, Calories: ${workout.caloriesBurned}")

                        val activity = ActivityModel(
                            activityType = workout.workoutType,
                            activity_id = workout.activity_id,
                            duration = "${workout.duration} min",
                            caloriesBurned = "${workout.caloriesBurned.toInt()} kcal",
                            intensity = workout.intensity,
                            calorieId = workout.id,
                            userId = userId
                        )

                        activityList.add(activity)
                    }

                    withContext(Dispatchers.Main) {
                        if (isAdded && view != null) {
                            if (activityList.isNotEmpty()) {
                                myActivityRecyclerView.visibility = View.VISIBLE
                                Log.d("FetchCalories", "Displaying ${activityList.size} activities")
                            } else {
                                myActivityRecyclerView.visibility = View.GONE
                                Log.d("FetchCalories", "No activities to display")
                            }
                            myActivityAdapter.addAll(activityList, -1, null, false)
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    withContext(Dispatchers.Main) {
                        if (isAdded && view != null) {
                            Log.e("FetchCalories", "API Error ${response.code()}: $errorBody")
                            myActivityRecyclerView.visibility = View.GONE
                            Toast.makeText(
                                context,
                                "Failed to fetch calories: ${response.code()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && view != null) {
                        Log.e("FetchCalories", "Exception: ${e.message}", e)
                        myActivityRecyclerView.visibility = View.GONE
                        Toast.makeText(
                            context,
                            "Error fetching calories: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }


    private fun showTooltipsSequentially() {
        Handler(Looper.getMainLooper()).postDelayed({
            showTooltipDialogSync(activitySync, "You can sync to apple \n health / google health \n from here.")
        }, 1000)

        Handler(Looper.getMainLooper()).postDelayed({
            showTooltipDialog(imageCalender, "You can access calendar \n view from here.")
        }, 5000)
    }

    private fun showTooltipDialog(anchorView: View, tooltipText: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.tooltip_layout)
        val tvTooltip = dialog.findViewById<TextView>(R.id.tvTooltipText)
        tvTooltip.text = tooltipText

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val tooltipWidth = 250

        val params = dialog.window?.attributes
        params?.x = (location[0] + anchorView.width) + tooltipWidth
        params?.y = location[1] - anchorView.height + 15
        dialog.window?.attributes = params
        dialog.window?.setGravity(Gravity.TOP)

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 3000)
    }

    private fun showTooltipDialogSync(anchorView: View, tooltipText: String) {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.tooltip_sync_layout)
        val tvTooltip = dialog.findViewById<TextView>(R.id.tvTooltipText)
        tvTooltip.text = tooltipText

        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val displayMetrics = resources.displayMetrics
        val screenWidth = displayMetrics.widthPixels
        val location = IntArray(2)
        anchorView.getLocationOnScreen(location)
        val tooltipWidth = 250

        val params = dialog.window?.attributes
        params?.x = (location[0] + anchorView.width) + tooltipWidth
        params?.y = location[1] - anchorView.height + 15
        dialog.window?.attributes = params
        dialog.window?.setGravity(Gravity.TOP)

        dialog.show()

        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 3000)
    }

    private fun updateCalorieRecord() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val calorieId: String = "67e0f84505b80d8823623e27"
                val request = UpdateCalorieRequest(
                    userId = "64763fe2fa0e40d9c0bc8264",
                    activity = "Cricket",
                    durationMin = 60,
                    intensity = "High",
                    activityFactor = 1.2,
                    sessions = 1
                )

                val response: Response<UpdateCalorieResponse> = ApiClient.apiServiceFastApi.updateCalorie(
                    calorieId = calorieId,
                    request = request
                )

                if (response.isSuccessful) {
                    val updateResponse: UpdateCalorieResponse? = response.body()
                    if (updateResponse != null) {
                        withContext(Dispatchers.Main) {
                            Log.d("UpdateCalorie", "Success: ${updateResponse.message}")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.e("UpdateCalorie", "Response body is null")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    withContext(Dispatchers.Main) {
                        Log.e("UpdateCalorie", "Error: ${response.code()} - ${response.message()}, Body: $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("UpdateCalorie", "Exception: ${e.message}", e)
                }
            }
        }
    }

    private fun deleteCalorieRecord() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val calorieId: String = "67e1122a3051bc4fbd9c42aa"
                val userId: String = "64763fe2fa0e40d9c0bc8264"

                val response: Response<DeleteCalorieResponse> = ApiClient.apiServiceFastApi.deleteCalorie(
                    calorieId = calorieId,
                    userId = userId
                )

                if (response.isSuccessful) {
                    val deleteResponse: DeleteCalorieResponse? = response.body()
                    if (deleteResponse != null) {
                        withContext(Dispatchers.Main) {
                            Log.d("DeleteCalorie", "Success: ${deleteResponse.message}")
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Log.e("DeleteCalorie", "Response body is null")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    withContext(Dispatchers.Main) {
                        Log.e("DeleteCalorie", "Error: ${response.code()} - ${response.message()}, Body: $errorBody")
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Log.e("DeleteCalorie", "Exception: ${e.message}", e)
                }
            }
        }
    }

    private fun onWorkoutItemClick(workoutModel: ActivityModel, position: Int, isRefresh: Boolean) {
        Log.d("WorkoutClick", "Clicked on ${workoutModel.activityType} at position $position")
    }

    private fun getWorkoutLogHistory(formattedDate: String) {
        LoaderUtil.showLoader(requireActivity())
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val call = ApiClient.apiServiceFastApi.getActivityLogHistory(userId, "google", formattedDate)
        call.enqueue(object : Callback<WorkoutHistoryResponse> {
            override fun onResponse(call: Call<WorkoutHistoryResponse>, response: Response<WorkoutHistoryResponse>) {
                if (response.isSuccessful) {
                    LoaderUtil.dismissLoader(requireActivity())
                    if (response.body() != null){
                        workoutHistoryResponse = response.body()
                        if (workoutHistoryResponse?.data?.record_details!!.size > 0){
                            workoutLogHistory.addAll(workoutHistoryResponse!!.data.record_details)
                            onWorkoutLogWeeklyDayList(workoutWeeklyDayList, workoutLogHistory)
                        }
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    LoaderUtil.dismissLoader(requireActivity())
                }
            }
            override fun onFailure(call: Call<WorkoutHistoryResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                LoaderUtil.dismissLoader(requireActivity())
            }
        })
    }

    private fun onWorkoutLogWeeklyDayList(weekList: List<WorkoutWeeklyDayModel>, workoutLogHistory: ArrayList<WorkoutRecord>) {
        val today = LocalDate.now()
        val weekLists : ArrayList<WorkoutWeeklyDayModel> = ArrayList()
        if (workoutLogHistory.size > 0 && weekList.isNotEmpty()){
            workoutLogHistory.forEach { workoutLog ->
                for (item in weekList){
                    if (item.fullDate.toString() == workoutLog.date){
                        if (workoutLog.is_available_workout == true){
                            item.is_available = true
                        }
                    }
                }
            }
        }

        if (weekList.isNotEmpty()){
            weekLists.addAll(weekList as Collection<WorkoutWeeklyDayModel>)
            var workoutLogDateData: WorkoutWeeklyDayModel? = null
            var isClick = false
            var index = -1
            for (currentDay in weekLists){
                if (currentDay.fullDate == today){
                    workoutLogDateData = currentDay
                    isClick = true
                    index = weekLists.indexOfFirst { it.fullDate == currentDay.fullDate }
                    break
                }
            }
            yourActivitiesWeeklyListAdapter.addAll(weekLists, index, workoutLogDateData, isClick)
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun onWorkoutLogDateItem(mealLogWeeklyDayModel: WorkoutWeeklyDayModel, position: Int, isRefresh: Boolean) {

        val formatter = DateTimeFormatter.ofPattern("E, d MMM yyyy")
        workoutDateTv.text = mealLogWeeklyDayModel.fullDate.format(formatter)

        val weekLists : ArrayList<WorkoutWeeklyDayModel> = ArrayList()
        weekLists.addAll(workoutWeeklyDayList as Collection<WorkoutWeeklyDayModel>)
        yourActivitiesWeeklyListAdapter.addAll(weekLists, position, mealLogWeeklyDayModel, isRefresh)

        val seleteddate = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = mealLogWeeklyDayModel.fullDate.format(seleteddate)
        fetchCalories(formattedDate)
    }

    private fun getWeekFrom(startDate: LocalDate): List<WorkoutWeeklyDayModel> {
        return (0..6).map { i ->
            val date = startDate.plusDays(i.toLong())
            WorkoutWeeklyDayModel(
                dayName = date.dayOfWeek.name.take(1), // M, T, W...
                dayNumber = date.dayOfMonth.toString(),
                fullDate = date,
            )
        }
    }
}