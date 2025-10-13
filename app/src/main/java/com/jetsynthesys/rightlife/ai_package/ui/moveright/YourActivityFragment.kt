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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.ActivityModel
import com.jetsynthesys.rightlife.ai_package.model.CardItem
import com.jetsynthesys.rightlife.ai_package.model.HeartRateZoneMinutes
import com.jetsynthesys.rightlife.ai_package.model.HeartRateZonePercentages
import com.jetsynthesys.rightlife.ai_package.model.HeartRateZones
import com.jetsynthesys.rightlife.ai_package.model.WorkoutWeeklyDayModel
import com.jetsynthesys.rightlife.ai_package.model.response.WorkoutHistoryResponse
import com.jetsynthesys.rightlife.ai_package.model.response.WorkoutRecord
import com.jetsynthesys.rightlife.ai_package.ui.adapter.YourActivitiesAdapter
import com.jetsynthesys.rightlife.ai_package.ui.adapter.YourActivitiesWeeklyListAdapter
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.databinding.FragmentYourActivityBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
class YourActivityFragment : BaseFragment<FragmentYourActivityBinding>() {

    private lateinit var usernameReset: EditText
    private lateinit var createWorkout: TextView
    private lateinit var activitySync: ImageView
    private lateinit var yourActivityBackButton: ImageView
    private lateinit var confirmResetBtn: AppCompatButton
    private lateinit var progressBarConfirmation: ProgressBar
    private lateinit var mealLogDateListAdapter: RecyclerView
    private lateinit var myActivityRecyclerView: RecyclerView
    private lateinit var imageCalender: ImageView
    private lateinit var layoutAddWorkout: LinearLayoutCompat
    private lateinit var layoutSaveWorkout : LinearLayoutCompat
    private lateinit var workoutDateTv: TextView
    private lateinit var nextWeekBtn: ImageView
    private lateinit var prevWeekBtn: ImageView
    private lateinit var layoutNoDataCard : LinearLayoutCompat
    private lateinit var layoutWorkoutData : ConstraintLayout
    private lateinit var layout_btn_addWorkout: LinearLayoutCompat
    private lateinit var healthConnectSyncButton: LinearLayoutCompat
    private var workoutWeeklyDayList: List<WorkoutWeeklyDayModel> = ArrayList()
    private var currentWeekStart: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)
    private var workoutHistoryResponse: WorkoutHistoryResponse? = null
    private var workoutLogHistory: ArrayList<WorkoutRecord> = ArrayList()
    private var loadingOverlay: FrameLayout? = null
    private var activityList: ArrayList<ActivityModel> = ArrayList()
    private var newActivityList: ArrayList<ActivityModel> = ArrayList()
    private val handler = Handler(Looper.getMainLooper())
    private var tooltipRunnable1: Runnable? = null
    private var tooltipRunnable2: Runnable? = null
    private var isTooltipShown = false
    private var lastDayOfCurrentWeek : String = ""
    private var moduleName : String = ""
    private var selectedDate: String? = null
    private var isTodayDate : Boolean = false
    private var currentWeekStartItem: LocalDate = LocalDate.now().with(DayOfWeek.MONDAY)

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentYourActivityBinding
        get() = FragmentYourActivityBinding::inflate
    var snackbar: Snackbar? = null

    private val yourActivitiesWeeklyListAdapter by lazy {
        YourActivitiesWeeklyListAdapter(requireContext(), arrayListOf(), -1, null, false, ::onWorkoutLogDateItem)
    }

    private val myActivityAdapter by lazy {
        YourActivitiesAdapter(requireContext(), arrayListOf() , arrayListOf(), -1, null, false,::onDateRecyclerRefresh, ::onWorkoutItemClick,
            onCirclePlusClick = { activityModel, position ->
                val fragment = AddWorkoutSearchFragment()
                val args = Bundle().apply {
                    putParcelable("ACTIVITY_MODEL", activityModel)
                    putString("edit", "edit")
                    putString("selected_date", selectedDate) // Put the string in the bundle
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
        selectedDate = arguments?.getString("selected_date")
        mealLogDateListAdapter = view.findViewById(R.id.recyclerview_calender)
        myActivityRecyclerView = view.findViewById(R.id.recyclerview_my_meals_item)
        imageCalender = view.findViewById(R.id.image_calender)
        layoutAddWorkout = view.findViewById(R.id.layoutAddWorkout)
        layoutSaveWorkout = view.findViewById(R.id.layoutSaveWorkout)
        activitySync = view.findViewById(R.id.activities_sync)
        healthConnectSyncButton = view.findViewById(R.id.health_connect_sync_button)
        yourActivityBackButton = view.findViewById(R.id.back_button)
        layout_btn_addWorkout = view.findViewById(R.id.layout_btn_addWorkout)
        workoutDateTv = view.findViewById(R.id.workoutDateTv)
        nextWeekBtn = view.findViewById(R.id.nextWeekBtn)
        prevWeekBtn = view.findViewById(R.id.prevWeekBtn)
        layoutNoDataCard = view.findViewById(R.id.layoutNoDataCard)
        layoutWorkoutData = view.findViewById(R.id.layoutWorkoutData)
        createWorkout = view.findViewById(R.id.tvCreateWorkout)

        myActivityRecyclerView.layoutManager = LinearLayoutManager(context)
        myActivityRecyclerView.adapter = myActivityAdapter

        activitySync.setOnClickListener {
            val bottomSheet = ActivitySyncBottomSheet()
            bottomSheet.show(parentFragmentManager, "ActivitySyncBottomSheet")
        }

        healthConnectSyncButton.setOnClickListener {
            // AddWorkoutSearchFragment navigation (commented as per original)
        }
        moduleName = arguments?.getString("ModuleName").toString()

        mealLogDateListAdapter.layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        mealLogDateListAdapter.adapter = yourActivitiesWeeklyListAdapter

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if(moduleName.equals("HomeDashboard")){
                        activity?.finish()
                    }else{
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

                }
            })

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val formatFullDate = DateTimeFormatter.ofPattern("E, d MMM yyyy")

        if (selectedDate == null || selectedDate.equals("")){
            selectedDate = formattedDate
        }

        currentWeekStart = getStartOfWeek(LocalDate.parse(selectedDate, formatter))
        workoutDateTv.text = LocalDate.parse(selectedDate, formatter).format(formatFullDate)
        getWorkoutLogHistory(selectedDate!!)
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
            workoutDateTv.text = currentWeekStart.format(formatFullDate)
            selectedDate = currentWeekStart.toString()
            getWorkoutLogHistory(currentWeekStart.toString())
            onWorkoutLogWeeklyDayList(workoutWeeklyDayList, workoutLogHistory)
            nextWeekBtn.setImageResource(R.drawable.forward_activity)
        }
        nextWeekBtn.setOnClickListener {
            val current = LocalDate.parse(formattedDate, formatter)
            val updated = LocalDate.parse(lastDayOfCurrentWeek, formatter)
            if (current > updated){
                currentWeekStart = currentWeekStart.plusWeeks(1)
                workoutWeeklyDayList = getWeekFrom(currentWeekStart)
                lastDayOfCurrentWeek = workoutWeeklyDayList.get(workoutWeeklyDayList.size - 1).fullDate.toString()
                workoutDateTv.text = currentWeekStart.format(formatFullDate)
                if (currentWeekStart == currentWeekStartItem){
                    selectedDate = formattedDate
                }else{
                    selectedDate = currentWeekStart.toString()
                }
                getWorkoutLogHistory(currentWeekStart.toString())
                onWorkoutLogWeeklyDayList(workoutWeeklyDayList, workoutLogHistory)
                nextWeekBtn.setImageResource(R.drawable.forward_activity)
            }else{
                Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                nextWeekBtn.setImageResource(R.drawable.right_arrow_grey)
            }
        }

        layout_btn_addWorkout.setOnClickListener {
            val fragment = SearchWorkoutFragment()
            val args = Bundle()
            args.putString("selected_date", selectedDate)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "searchWorkoutFragment")
                addToBackStack("searchWorkoutFragment")
                commit()
            }
        }

        layoutAddWorkout.setOnClickListener {
           // val formatter = DateTimeFormatter.ofPattern("EEE, d MMM yyyy", Locale.ENGLISH)
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val date = LocalDate.parse(selectedDate, formatter)
            val currentDate = LocalDate.now()
            if (date <= currentDate) {
                val fragment = SearchWorkoutFragment()
                val args = Bundle()
                args.putString("selected_date", selectedDate) // Put the string in the bundle
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "searchWorkoutFragment")
                    addToBackStack("searchWorkoutFragment")
                    commit()
                }
            }else{
                Toast.makeText(requireContext(), "Workout cannot be logged on future date", Toast.LENGTH_SHORT).show()
            }
        }

        yourActivityBackButton.setOnClickListener {
            if(moduleName.equals("HomeDashboard")){
                activity?.finish()
            }else{
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
        }

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

        layoutSaveWorkout.setOnClickListener {
            val fragment = CreateRoutineFragment()
            val args = Bundle().apply {
                putParcelableArrayList("ACTIVITY_LIST", newActivityList)
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
    private fun onDateRecyclerRefresh(){
        // Refresh current date and workout history
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val formatFullDate = DateTimeFormatter.ofPattern("E, d MMM yyyy")
        workoutDateTv.text = currentDateTime.format(formatFullDate)

        if (selectedDate == null || selectedDate.equals("")){
            selectedDate = formattedDate
        }

        // Refresh workout history for current selected date
        getWorkoutLogHistory(selectedDate!!)

        // Also refresh the weekly calendar view
        workoutWeeklyDayList = getWeekFrom(currentWeekStart)
        lastDayOfCurrentWeek = workoutWeeklyDayList.get(workoutWeeklyDayList.size - 1).fullDate.toString()
        onWorkoutLogWeeklyDayList(workoutWeeklyDayList, workoutLogHistory)
    }
    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }

    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    private fun fetchUserWorkouts(formattedDate: String) {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userid = SharedPreferenceManager.getInstance(requireActivity()).userId
                val currentDate = LocalDate.now().format(DateTimeFormatter.ISO_LOCAL_DATE)
                val response = ApiClient.apiServiceFastApi.getNewUserWorkouts(
                    userId = userid,
                    start_date = formattedDate,
                    end_date = formattedDate,
                    page = 1,
                    limit = 10
                )
                if (response.isSuccessful) {
                    val workouts = response.body()
                    workouts?.let {
                        // Map syncedWorkouts to CardItem objects
                        val defaultHeartRateZones = HeartRateZones(
                            lightZone = emptyList(),
                            fatBurnZone = emptyList(),
                            cardioZone = emptyList(),
                            peakZone = emptyList()
                        )
                        val defaultHeartRateZoneMinutes = HeartRateZoneMinutes(
                            belowLight = 0,
                            lightZone = 0,
                            fatBurnZone = 0,
                            cardioZone = 0,
                            peakZone = 0
                        )
                        val defaultHeartRateZonePercentages = HeartRateZonePercentages(
                            belowLight = 0f,
                            lightZone = 0f,
                            fatBurnZone = 0f,
                            cardioZone = 0f,
                            peakZone = 0f
                        )
                        val syncedCardItems = it.syncedWorkouts.map { workout ->
                            val durationDouble = workout.duration.toDoubleOrNull() ?: 0.0
                            val durationMinutes = durationDouble.toInt()
                            val hours = durationMinutes / 60
                            val minutes = durationMinutes % 60
                            val durationText = if (hours > 0) "$hours hr ${minutes.toString().padStart(2, '0')} mins" else "$minutes mins"
                            val caloriesText = "${workout.caloriesBurned}"
                            val avgHeartRate = if (workout.heartRateData.isNotEmpty()) {
                                val totalHeartRate = workout.heartRateData.sumOf { it.heartRate }
                                val count = workout.heartRateData.size
                                "${(totalHeartRate / count).toInt()} bpm"
                            } else "N/A"
                            workout.heartRateData.forEach { heartRateData ->
                                heartRateData.trendData.addAll(listOf(listOf(110, 112, 115, 118, 120, 122, 125).toString()))
                            }
                            ActivityModel(
                                userId = workout.userId,
                                id = workout.id,
                                source = workout.source,
                                recordType = workout.recordType,
                                workoutType = workout.workoutType,
                                workoutId = workout.workoutId,
                                duration = durationText,
                                averageHeartRate = workout.averageHeartRate,
                                caloriesBurned = caloriesText,
                                caloriesUnit = workout.caloriesUnit,
                                icon = "",
                                intensity = "",
                                isSynced = true,
                                activityId = workout.activity_id ?: "" // Handle null activity_id
                            )
                        }

                        // Map unsyncedWorkouts to CardItem objects
                        val unsyncedCardItems = it.unsyncedWorkouts.map { workout ->
                            val durationDouble = workout.duration.toDoubleOrNull() ?: 0.0
                            val durationMinutes = durationDouble.toInt()
                            val hours = durationMinutes / 60
                            val minutes = durationMinutes % 60
                            val durationText = if (hours > 0) "$hours hr ${minutes.toString().padStart(2, '0')} mins" else "$minutes mins"
                            val caloriesDouble = workout.caloriesBurned.toDoubleOrNull() ?: 0.0
                            val caloriesText = "${caloriesDouble.toInt()}"
                            ActivityModel(
                                userId = workout.userId,
                                id = workout.id,
                                source = workout.source,
                                recordType = workout.recordType,
                                workoutType = workout.workoutType,
                                workoutId = workout.workoutId,
                                duration = durationText,
                                averageHeartRate = 0.0,
                                caloriesBurned = caloriesText,
                                caloriesUnit = workout.caloriesUnit,
                                icon = workout.icon,
                                intensity = workout.intensity,
                                isSynced = false,
                                activityId = workout.activity_id ?: "" // Handle null activity_id
                            )
                        }

                        val syncedCardItemsView = it.syncedWorkouts.map { workout ->
                            val durationDouble = workout.duration.toDoubleOrNull() ?: 0.0
                            val durationMinutes = durationDouble.toInt()
                            val hours = durationMinutes / 60
                            val minutes = durationMinutes % 60
                            val durationText = if (hours > 0) "$hours hr ${minutes.toString().padStart(2, '0')} mins" else "$minutes mins"
                            val caloriesText = "${workout.caloriesBurned}"
                            val avgHeartRate = if (workout.heartRateData.isNotEmpty()) {
                                val totalHeartRate = workout.heartRateData.sumOf { it.heartRate }
                                val count = workout.heartRateData.size
                                "${(totalHeartRate / count).toInt()} bpm"
                            } else "N/A"
                            workout.heartRateData.forEach { heartRateData ->
                                heartRateData.trendData.addAll(listOf(listOf(110, 112, 115, 118, 120, 122, 125).toString()))
                            }
                            CardItem(
                                title = workout.workoutType,
                                duration = durationText,
                                caloriesBurned = caloriesText,
                                icon = "",
                                avgHeartRate = avgHeartRate,
                                heartRateData = workout.heartRateData,
                                heartRateZones = workout.heartRateZones ?: defaultHeartRateZones,
                                heartRateZoneMinutes = workout.heartRateZoneMinutes ?: defaultHeartRateZoneMinutes,
                                heartRateZonePercentages = workout.heartRateZonePercentages ?: defaultHeartRateZonePercentages,
                                isSynced = true
                            )
                        }
                        // Combine synced and unsynced CardItems
                        val newActivities = ArrayList<ActivityModel>()
                        val allCardItems = syncedCardItems + unsyncedCardItems
                        withContext(Dispatchers.Main) {
                            if (!isAdded || view == null || !isVisible) {
                                Log.w("FetchCalories", "Fragment not attached after API call for date $formattedDate, skipping UI update")
                                return@withContext
                            }
                            // Update activityList and adapter
                            newActivities.addAll(allCardItems)
                            activityList.clear()
                            newActivityList.clear()
                            activityList.addAll(newActivities)
                            newActivityList.addAll(unsyncedCardItems)
                            Log.d("FetchCalories", "Updated activityList with ${activityList.size} activities for date $formattedDate")
                            myActivityAdapter.addAll(newActivities, syncedCardItemsView,-1, null, false)
                            myActivityAdapter.notifyDataSetChanged()
                            // Update RecyclerView visibility
                            if (activityList.isNotEmpty()) {
                                myActivityRecyclerView.visibility = View.VISIBLE
                                layoutSaveWorkout.visibility = View.VISIBLE
                                layoutAddWorkout.visibility = View.GONE
                                layoutWorkoutData.visibility = View.VISIBLE
                                layoutNoDataCard.visibility = View.GONE
                                Log.d("FetchCalories", "Adapter updated with ${newActivities.size} activities, RecyclerView visible for date $formattedDate")
                            } else {
                                myActivityRecyclerView.visibility = View.GONE
                                layoutAddWorkout.visibility = View.VISIBLE
                                layoutSaveWorkout.visibility = View.GONE
                                layoutWorkoutData.visibility = View.GONE
                                layoutNoDataCard.visibility = View.VISIBLE
                                Log.d("FetchCalories", "No activities to display for date $formattedDate")
                            }
                            layoutSaveWorkout.isEnabled = true
                            if (isAdded && view != null) {
                                requireActivity().runOnUiThread {
                                    dismissLoader(requireView())
                                }
                            }
                        }
                    } ?: withContext(Dispatchers.Main) {
                        myActivityRecyclerView.visibility = View.GONE
                        layoutAddWorkout.visibility = View.VISIBLE
                        layoutSaveWorkout.visibility = View.GONE
                        layoutWorkoutData.visibility = View.GONE
                        layoutNoDataCard.visibility = View.VISIBLE
                        Toast.makeText(requireContext(), "No workout data received", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        myActivityRecyclerView.visibility = View.GONE
                        Toast.makeText(requireContext(), "Error: ${response.code()} - ${response.message()}", Toast.LENGTH_SHORT).show()
                        if (isAdded && view != null) {
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    myActivityRecyclerView.visibility = View.GONE
                    Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
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

      //  tooltipRunnable2 = Runnable {
      //      if (isResumed) {
      //          showTooltipDialog(imageCalender, "You can access calendar \n view from here.")
     //       }
    //    }
     //   handler.postDelayed(tooltipRunnable2!!, 5000)

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
        Log.d("WorkoutClick", "Clicked on ${workoutModel.workoutType} at position $position")
    }

    private fun getWorkoutLogHistory(formattedDate: String) {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val call = ApiClient.apiServiceFastApiV2.getActivityLogHistory(userId, "android", formattedDate)
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
                            workoutLogHistory.clear()
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

            val selectedDateItem = LocalDate.parse(selectedDate)
            val weekLists: ArrayList<WorkoutWeeklyDayModel> = ArrayList()
            if (workoutLogHistory.size > 0 && weekList.isNotEmpty()) {
                workoutLogHistory.forEach { workoutLog ->
                    for (item in weekList) {
                        if (item.fullDate.toString() == workoutLog.date) {
                            if (workoutLog.is_available_workout == true) {
                                item.is_available = true
                            }else{
                                item.is_available = workoutLog.is_available_workout
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
                    if (currentDay.fullDate == selectedDateItem) {
                        workoutLogDateData = currentDay
                        isClick = true
                        index = weekLists.indexOfFirst { it.fullDate == currentDay.fullDate }
                        break
                    }
                }
                yourActivitiesWeeklyListAdapter.addAll(weekLists, index, workoutLogDateData, isClick)
            }

        if (selectedDate != null){
            fetchUserWorkouts(selectedDate!!)
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
       // fetchCalories(formattedDate)
        fetchUserWorkouts(formattedDate)
        selectedDate = formattedDate
    }

    private fun getStartOfWeek(date: LocalDate): LocalDate {
        return date.with(java.time.DayOfWeek.MONDAY)
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