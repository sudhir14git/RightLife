package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.app.Dialog
import android.content.Context
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
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
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
    private lateinit var workoutDateTv: TextView
    private lateinit var nextWeekBtn: ImageView
    private lateinit var prevWeekBtn: ImageView
    private lateinit var layout_btn_addWorkout: LinearLayoutCompat
    private lateinit var healthConnectSyncButton: LinearLayoutCompat
    private var workoutWeeklyDayList: List<WorkoutWeeklyDayModel> = ArrayList()
    private var currentWeekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)
    private var workoutHistoryResponse: WorkoutHistoryResponse? = null
    private var workoutLogHistory: ArrayList<WorkoutRecord> = ArrayList()
    private var loadingOverlay: FrameLayout? = null
    private var activityList: ArrayList<ActivityModel> = ArrayList()

    private val handler = Handler(Looper.getMainLooper())
    private var tooltipRunnable1: Runnable? = null
    private var tooltipRunnable2: Runnable? = null
    private var isTooltipShown = false
    private var lastDayOfCurrentWeek : String = ""

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentYourActivityBinding
        get() = FragmentYourActivityBinding::inflate
    var snackbar: Snackbar? = null

    private val yourActivitiesWeeklyListAdapter by lazy {
        YourActivitiesWeeklyListAdapter(
            requireContext(),
            arrayListOf(),
            -1,
            null,
            false,
            ::onWorkoutLogDateItem
        )
    }

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
                    putString("edit", "edit")
                }
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "workoutDetails")
                    addToBackStack("workoutDetails")
                    commit()
                }
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
            val args = Bundle().apply {
                putString("ModuleName", "MoveRight")
            }
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                addToBackStack(null)
                commit()
            }
        }

        myActivityRecyclerView.layoutManager = LinearLayoutManager(context)
        myActivityRecyclerView.adapter = myActivityAdapter

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
                    val args = Bundle().apply {
                        putString("ModuleName", "MoveRight")
                    }
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                        addToBackStack(null)
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
        lastDayOfCurrentWeek = workoutWeeklyDayList.get(workoutWeeklyDayList.size - 1).fullDate.toString()
        onWorkoutLogWeeklyDayList(workoutWeeklyDayList, workoutLogHistory)
        val current = LocalDate.parse(formattedDate, formatter)
        val updated = LocalDate.parse(lastDayOfCurrentWeek, formatter)
        if (current > updated){
            nextWeekBtn.setImageResource(R.drawable.forward_activity)
        }else{
            nextWeekBtn.setImageResource(R.drawable.right_arrow_grey)
        }
        prevWeekBtn.setOnClickListener {
            currentWeekStart = currentWeekStart.minusWeeks(1)
            workoutWeeklyDayList = getWeekFrom(currentWeekStart)
            lastDayOfCurrentWeek = workoutWeeklyDayList.get(workoutWeeklyDayList.size - 1).fullDate.toString()
            onWorkoutLogWeeklyDayList(workoutWeeklyDayList, workoutLogHistory)
            getWorkoutLogHistory(currentWeekStart.toString())
            nextWeekBtn.setImageResource(R.drawable.forward_activity)
        }
        nextWeekBtn.setOnClickListener {
            val current = LocalDate.parse(formattedDate, formatter)
            val updated = LocalDate.parse(lastDayOfCurrentWeek, formatter)
            if (current > updated){
                currentWeekStart = currentWeekStart.plusWeeks(1)
                workoutWeeklyDayList = getWeekFrom(currentWeekStart)
                lastDayOfCurrentWeek = workoutWeeklyDayList.get(workoutWeeklyDayList.size - 1).fullDate.toString()
                onWorkoutLogWeeklyDayList(workoutWeeklyDayList, workoutLogHistory)
                getWorkoutLogHistory(currentWeekStart.toString())
                nextWeekBtn.setImageResource(R.drawable.forward_activity)
            }else{
                Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                nextWeekBtn.setImageResource(R.drawable.right_arrow_grey)
            }

        }
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
            val args = Bundle().apply {
                putParcelableArrayList("ACTIVITY_LIST", activityList)
            }
            fragment.arguments = args
            Log.d("YourActivityFragment", "Sending ${activityList.size} activities to CreateRoutineFragment")
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "CreateRoutineFragment")
                addToBackStack("CreateRoutineFragment")
                commit()
            }
        }

        val prefs = requireContext().getSharedPreferences("TooltipPrefs", Context.MODE_PRIVATE)
        isTooltipShown = prefs.getBoolean("hasShownTooltips", false)
    }

    override fun onResume() {
        super.onResume()
        if (!isTooltipShown) {
            showTooltipsSequentially()
        }
    }

    override fun onPause() {
        super.onPause()
        tooltipRunnable1?.let { handler.removeCallbacks(it) }
        tooltipRunnable2?.let { handler.removeCallbacks(it) }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeCallbacksAndMessages(null)
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }

    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun fetchCalories(formattedDate: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                // Skip if fragment is not attached or not visible
                if (!isAdded || view == null || !isVisible) {
                    Log.w("FetchCalories", "Fragment not attached or not visible for date $formattedDate, skipping")
                    return@launch
                }

                withContext(Dispatchers.Main) {
                    showLoader(requireView())
                }

                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                    ?: "64763fe2fa0e40d9c0bc8264" // Fallback userId
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
                    Log.d("FetchCalories", "Date: $formattedDate, Received ${caloriesResponse?.data?.size ?: 0} workouts")

                    // Create ArrayList to match adapter's expected type
                    val newActivities = ArrayList<ActivityModel>()
                    caloriesResponse?.data?.forEachIndexed { index, workout ->
                        Log.d("FetchCalories", "Workout $index - Type: ${workout.workoutType}, Duration: ${workout.duration}, Calories: ${workout.caloriesBurned}, ID: ${workout.activity_id}")
                        val activity = ActivityModel(
                            activityType = workout.workoutType,
                            activity_id = workout.activity_id,
                            duration = "${workout.duration} min",
                            caloriesBurned = "${workout.caloriesBurned.toInt()} kcal",
                            icon = workout.icon,
                            intensity = workout.intensity,
                            calorieId = workout.id,
                            userId = userId
                        )
                        println(newActivities)
                        newActivities.add(activity)
                    }

                    withContext(Dispatchers.Main) {
                        if (!isAdded || view == null || !isVisible) {
                            Log.w("FetchCalories", "Fragment not attached after API call for date $formattedDate, skipping UI update")
                            return@withContext
                        }

                        // Update activityList and adapter
                        activityList.clear()
                        activityList.addAll(newActivities)

                        Log.d("FetchCalories", "Updated activityList with ${activityList.size} activities for date $formattedDate")

                        // Clear adapter (fallback if clear() is not available)
                       /* try {
                            myActivityAdapter.clear() // Try calling clear() if it exists
                        } catch (e: NoSuchMethodError) {
                            Log.w("FetchCalories", "Adapter clear() method not found, clearing manually")
                            myActivityAdapter.addAll(ArrayList(), -1, null, false) // Pass empty list to clear
                        }*/

                        // Update adapter with new data
                        myActivityAdapter.addAll(newActivities, -1, null, false)
                        myActivityAdapter.notifyDataSetChanged()

                        // Update RecyclerView visibility
                        if (activityList.isNotEmpty()) {
                            myActivityRecyclerView.visibility = View.VISIBLE
                            btnLogMeal.visibility = View.VISIBLE
                            Log.d("FetchCalories", "Adapter updated with ${newActivities.size} activities, RecyclerView visible for date $formattedDate")
                        } else {
                            myActivityRecyclerView.visibility = View.GONE
                            btnLogMeal.visibility = View.GONE
                            Log.d("FetchCalories", "No activities to display for date $formattedDate")
                        }

                        btnLogMeal.isEnabled = true
                        dismissLoader(requireView())
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    withContext(Dispatchers.Main) {
                        if (!isAdded || view == null || !isVisible) return@withContext
                        Log.e("FetchCalories", "API Error ${response.code()} for date $formattedDate: $errorBody")
                        myActivityRecyclerView.visibility = View.GONE
                        Toast.makeText(context, "Failed to fetch calories: ${response.code()}", Toast.LENGTH_LONG).show()
                        dismissLoader(requireView())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (!isAdded || view == null || !isVisible) return@withContext
                    Log.e("FetchCalories", "Exception for date $formattedDate: ${e.message}", e)
                    myActivityRecyclerView.visibility = View.GONE
                    Toast.makeText(context, "Error fetching calories: ${e.message}", Toast.LENGTH_LONG).show()
                    dismissLoader(requireView())
                }
            }
        }
    }

    private fun showTooltipsSequentially() {
        tooltipRunnable1 = Runnable {
            if (isResumed) {
                showTooltipDialogSync(activitySync, "You can sync to google health connect \n from here.")
            }
        }
        handler.postDelayed(tooltipRunnable1!!, 1000)

        tooltipRunnable2 = Runnable {
            if (isResumed) {
                showTooltipDialog(imageCalender, "You can access calendar \n view from here.")
            }
        }
        handler.postDelayed(tooltipRunnable2!!, 5000)

        val prefs = requireContext().getSharedPreferences("TooltipPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean("hasShownTooltips", true).apply()
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

    private fun onWorkoutItemClick(workoutModel: ActivityModel, position: Int, isRefresh: Boolean) {
        Log.d("WorkoutClick", "Clicked on ${workoutModel.activityType} at position $position")
    }

    private fun getWorkoutLogHistory(formattedDate: String) {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val call = ApiClient.apiServiceFastApi.getActivityLogHistory(userId, "google", formattedDate)
        call.enqueue(object : Callback<WorkoutHistoryResponse> {
            override fun onResponse(call: Call<WorkoutHistoryResponse>, response: Response<WorkoutHistoryResponse>) {
                if (response.isSuccessful) {
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    if (response.body() != null) {
                        workoutHistoryResponse = response.body()
                        if (workoutHistoryResponse?.data?.record_details!!.size > 0) {
                            workoutLogHistory.addAll(workoutHistoryResponse!!.data.record_details)
                            onWorkoutLogWeeklyDayList(workoutWeeklyDayList, workoutLogHistory)
                        }
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<WorkoutHistoryResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                if (isAdded && view != null) {
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    private fun onWorkoutLogWeeklyDayList(weekList: List<WorkoutWeeklyDayModel>, workoutLogHistory: ArrayList<WorkoutRecord>) {
        val today = LocalDate.now()
        val weekLists: ArrayList<WorkoutWeeklyDayModel> = ArrayList()
        if (workoutLogHistory.size > 0 && weekList.isNotEmpty()) {
            workoutLogHistory.forEach { workoutLog ->
                for (item in weekList) {
                    if (item.fullDate.toString() == workoutLog.date) {
                        if (workoutLog.is_available_workout == true) {
                            item.is_available = true
                        }
                    }
                }
            }
        }

        if (weekList.isNotEmpty()) {
            weekLists.addAll(weekList as Collection<WorkoutWeeklyDayModel>)
            var workoutLogDateData: WorkoutWeeklyDayModel? = null
            var isClick = false
            var index = -1
            for (currentDay in weekLists) {
                if (currentDay.fullDate == today) {
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

        val weekLists: ArrayList<WorkoutWeeklyDayModel> = ArrayList()
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
                dayName = date.dayOfWeek.name.take(1),
                dayNumber = date.dayOfMonth.toString(),
                fullDate = date,
            )
        }
    }
}