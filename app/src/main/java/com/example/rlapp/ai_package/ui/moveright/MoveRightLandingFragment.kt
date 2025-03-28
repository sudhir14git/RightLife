package com.example.rlapp.ai_package.ui.moveright

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.*
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.data.repository.ApiClient
import com.example.rlapp.ai_package.model.CardItem
import com.example.rlapp.ai_package.model.GridItem
import com.example.rlapp.ai_package.model.MealLogsResponseModel
import com.example.rlapp.ai_package.ui.adapter.CarouselAdapter
import com.example.rlapp.ai_package.ui.adapter.GridAdapter
import com.example.rlapp.ai_package.ui.eatright.fragment.HalfCurveProgressBar
import com.example.rlapp.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.example.rlapp.ai_package.ui.moveright.graphs.LineGrapghViewSteps
import com.example.rlapp.ai_package.ui.sleepright.fragment.SleepRightLandingFragment
import com.example.rlapp.ai_package.utils.AppPreference
import com.example.rlapp.databinding.FragmentLandingBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.ZoneId
import kotlin.math.abs

class MoveRightLandingFragment : BaseFragment<FragmentLandingBinding>() {

    private lateinit var carouselViewPager: ViewPager2
    private lateinit var dotsLayout: LinearLayout
    private lateinit var dots: Array<ImageView?>
    private lateinit var totalCaloriesBurnedRecord : List<TotalCaloriesBurnedRecord>
    private lateinit var stepsRecord : List<StepsRecord>
    private lateinit var healthConnectClient: HealthConnectClient
    private lateinit var tvBurnValue : TextView
    private lateinit var stepLineGraphView : LineGrapghViewSteps
    private lateinit var stepsTv : TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var appPreference: AppPreference



    //    // Define all required read permissions
    private val allReadPermissions = setOf(
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(SpeedRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class)
)

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentLandingBinding
        get() = FragmentLandingBinding::inflate

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        appPreference = AppPreference(requireContext())
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        appPreference = AppPreference(requireContext())
        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Loading")
        progressDialog.setCancelable(false)
        fetchUserWorkouts()
        val workoutImageicon = view.findViewById<ImageView>(R.id.workout_forward_icon)
        val activityFactorImageicon = view.findViewById<ImageView>(R.id.activity_forward_icon)
        val logmealbutton = view.findViewById<ConstraintLayout>(R.id.log_meal_button)
        val layoutAddWorkout = view.findViewById<ConstraintLayout>(R.id.lyt_snap_meal)
        carouselViewPager = view.findViewById(R.id.carouselViewPager)
        dotsLayout = view.findViewById(R.id.dotsLayout)
        tvBurnValue = view.findViewById(R.id.textViewBurnValue)
        val cardItems = listOf(
            CardItem("Functional Strength Training", "This is the first card."),
            CardItem("Functional Strength Training", "This is the second card."),
            CardItem("Functional Strength Training", "This is the third card.")
        )
        val adapter = CarouselAdapter(cardItems) { cardItem, position ->
            navigateToFragment(WorkoutAnalyticsFragment(), "workoutanaysisFragment")
        }
        carouselViewPager.adapter = adapter
        addDotsIndicator(cardItems.size)
        carouselViewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateDots(position)
            }
        })
        carouselViewPager.setPageTransformer { page, position ->
            val offset = abs(position)
            page.scaleY = 1 - (offset * 0.1f)
        }

        val availabilityStatus = HealthConnectClient.getSdkStatus(requireContext())
        if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
             healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
            lifecycleScope.launch {
                requestPermissionsAndReadSteps()
            }
        } else {
            // Show a message or guide user to install/update Health Connect
            Toast.makeText(context, "Please install or update Health Connect from the Play Store.", Toast.LENGTH_LONG).show()
//            val intent = Intent(Intent.ACTION_VIEW).apply {
//                data = Uri.parse("market://details?id=com.google.android.apps.healthdata")
//            }
//            startActivity(intent)
        }
//        if (isSamsungDevice()) {
//            println("This is a Samsung device.")
//            healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
//            lifecycleScope.launch {
//                requestPermissionsAndReadSteps()
//            }
//        } else {
//            println("This is NOT a Samsung device.")
//            // Initialize Health Connect Client
//            healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
//            lifecycleScope.launch {
//                requestPermissionsAndReadSteps()
//            }
//        }

        workoutImageicon.setOnClickListener {
            navigateToFragment(YourActivityFragment(), "YourActivityFragment")
        }

        layoutAddWorkout.setOnClickListener {
            navigateToFragment(SleepRightLandingFragment(), "SleepRightLandingFragment")
        }
        activityFactorImageicon.setOnClickListener {
            // Add click listener if needed
        }

        logmealbutton.setOnClickListener {
            navigateToFragment(YourMealLogsFragment(), "YourMealLogs")
        }
        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
        val circleIndicator = view.findViewById<View>(R.id.circleIndicator)
        val transparentOverlay = view.findViewById<View>(R.id.transparentOverlay)
        val belowTransparent = view.findViewById<ImageView>(R.id.imageViewBelowOverlay)
        val progressBarLayout = view.findViewById<ConstraintLayout>(R.id.progressBarLayout)
        stepLineGraphView = view.findViewById<LineGrapghViewSteps>(R.id.line_graph_steps)
        stepsTv = view.findViewById(R.id.steps_text)

        progressBar.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                progressBar.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val progressBarWidth = progressBar.width.toFloat()
                val overlayPositionPercentage = 0.6f
                val progress = progressBar.progress
                val max = progressBar.max
                val progressPercentage = progress.toFloat() / max
                println("Progress Percentage: $progressPercentage")
                val constraintSet = ConstraintSet()
                constraintSet.clone(progressBarLayout)
                constraintSet.setGuidelinePercent(R.id.circleIndicatorGuideline, progressPercentage)
                constraintSet.setGuidelinePercent(R.id.overlayGuideline, overlayPositionPercentage)
                constraintSet.applyTo(progressBarLayout)
            }
        })

        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val items = listOf(
            GridItem("RHR", R.drawable.rhr_icon, "bpm", "64"),
            GridItem("Avg HR", R.drawable.rhr_icon, "bpm", "84"),
            GridItem("HRV", R.drawable.hrv_icon, "ms", "71"),
            GridItem("Burn", R.drawable.burn_icon, "Kcal", "844")
        )

        val adapter1 = GridAdapter(items) { itemName ->
            when (itemName) {
                "RHR" -> {
                    navigateToFragment(AverageHeartRateFragment(), "AverageHeartRateFragment")
                }

                "Avg HR" -> {
                    navigateToFragment(RestingHeartRateFragment(), "RestingHeartRateFragment")
                }

                "HRV" -> {
                    navigateToFragment(
                        HeartRateVariabilityFragment(),
                        "HeartRateVariabilityFragment"
                    )
                }

                "Burn" -> {
                    navigateToFragment(BurnFragment(), "BurnFragment")
                }
            }
        }
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = adapter1

        val todaySteps = floatArrayOf(100f, 200f, 100f, 300f, 50f, 400f, 100f)
        val averageSteps = floatArrayOf(150f, 250f, 350f, 450f, 550f, 650f, 750f)
        val goalSteps = floatArrayOf(700f, 700f, 700f, 700f, 700f, 700f, 700f)
        stepLineGraphView.addDataSet(todaySteps, 0xFFFD6967.toInt())
        stepLineGraphView.addDataSet(averageSteps, 0xFF707070.toInt())
        stepLineGraphView.addDataSet(goalSteps, 0xFF03B27B.toInt())
        stepLineGraphView.invalidate()

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })
    }

    private fun addDotsIndicator(count: Int) {
        dots = arrayOfNulls(count)
        for (i in 0 until count) {
            dots[i] = ImageView(requireContext()).apply {
                setImageDrawable(
                    ContextCompat.getDrawable(
                        requireContext(),
                        R.drawable.dot_unselected
                    )
                )
                val params = LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
                )
                params.setMargins(8, 0, 8, 0)
                layoutParams = params
            }
            dotsLayout.addView(dots[i])
        }
        updateDots(0)
    }

    private fun updateDots(position: Int) {
        for (i in dots.indices) {
            val drawable = if (i == position) {
                R.drawable.dot_selected
            } else {
                R.drawable.dot_unselected
            }
            dots[i]?.setImageResource(drawable)
        }
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun requestPermissionsAndReadSteps() {
        lifecycleScope.launch {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (allReadPermissions.all { it in granted }) {
                retrieveStepsData()
                fetchActiveCaloriesBurned()
                fetchHeartRateData()
            } else {
                requestPermissionsLauncher.launch(allReadPermissions.toTypedArray())
            }
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(context, "Permissions Denied", Toast.LENGTH_SHORT).show()
            }
        }

    private suspend fun retrieveStepsData() {
        try {
            // Set the date range for the query (last 7 days)
            val endTime = Instant.now()
            val startTime = endTime.minusSeconds( 24 * 60 * 60)
            // Read steps data within the specified time range
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(
                    recordType = StepsRecord::class,
                    timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                )
            )
            stepsRecord = response.records
            // Process and print the retrieved steps data

            for (record in response.records) {
                val startDateTime = record.startTime.atZone(ZoneId.systemDefault())
                val endDateTime = record.endTime.atZone(ZoneId.systemDefault())
                val steps = record.count
                println("Steps: $steps, from $startDateTime to $endDateTime")
            }
            val stepCountArray = fillFloatArrayFromSteps(stepsRecord)

            val todaySteps = stepCountArray
            val averageSteps = stepCountArray
            val goalSteps = stepCountArray
            stepLineGraphView.addDataSet(todaySteps, 0xFFFD6967.toInt())
            stepLineGraphView.addDataSet(averageSteps, 0xFF707070.toInt())
            stepLineGraphView.addDataSet(goalSteps, 0xFF03B27B.toInt())
            stepLineGraphView.invalidate()
            println(stepCountArray)

            println("Steps data retrieved successfully!")

            val stepsToday = fetchTodaySteps(healthConnectClient)
            stepsTv.text = stepsToday.toString()
            println("Total steps today: $stepsToday")
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchActiveCaloriesBurned() {
        val timeRangeFilter = TimeRangeFilter.between(
            Instant.now().minusSeconds(24 * 60 * 60),  // 24 hours ago
            Instant.now()  // Now
        )
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = TotalCaloriesBurnedRecord::class,
                timeRangeFilter = timeRangeFilter
            )
        )
        totalCaloriesBurnedRecord = response.records
        println(totalCaloriesBurnedRecord)
        response.records.forEach { record ->
            println("Calories burned: ${record.energy.inKilocalories} kcal")
            println("Start time: ${record.startTime}")
            println("End time: ${record.endTime}")
            tvBurnValue.text = record.energy.inKilocalories.toInt().toString()
        }
    }

    private suspend fun fetchHeartRateData() {
     //   val healthConnectClient = HealthConnectClient.getOrCreate(context)
        // Define the time range (e.g., past 7 days)
        val endTime = Instant.now()
        val startTime = endTime.minusSeconds(7 * 24 * 60 * 60)
        // Read steps data within the specified time range
        // Query for HeartRateRecord
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = HeartRateRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        // Handle the retrieved heart rate records
        for (record in response.records) {
            Log.d(
                "HeartRateRecord",
                "Start: ${record.startTime}, End: ${record.endTime}, " +
                        "BPM: ${record.samples.map { it.beatsPerMinute }}"
            )
        }
    }

    private fun fillFloatArrayFromSteps(stepsRecords: List<StepsRecord>): FloatArray {
        return FloatArray(stepsRecords.size) { index ->
            stepsRecords[index].count.toFloat()  // Accessing step count and converting to Float
        }
    }

    private suspend fun fetchTodaySteps(healthConnectClient: HealthConnectClient): Long {
        // Define time range for the past 24 hours
        val endTime = Instant.now()
        val startTime = endTime.minusSeconds(24 * 60 * 60)

        // Try fetching the steps data within the specified time range
        val response = healthConnectClient.readRecords(
            ReadRecordsRequest(
                recordType = StepsRecord::class,
                timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
            )
        )
        // Calculate total step count by summing all the steps from the retrieved records
        val totalSteps = response.records.sumOf { it.count }
        return totalSteps
    }

    fun isSamsungDevice(): Boolean {
        return Build.MANUFACTURER.equals("Samsung", ignoreCase = true)
    }
    fun fetchUserWorkouts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiServiceFastApi.getUserWorkouts(
                    userId = "64763fe2fa0e40d9c0bc8264",
                    startDate = "2025-03-17",
                    endDate = "2025-03-25",
                    page = 1,
                    limit = 10
                )

                if (response.isSuccessful) {
                    val workouts = response.body()
                    workouts?.let {
                        // Calculate total calories burned from synced and unsynced workouts
                        val totalSyncedCalories = it.syncedWorkouts.sumOf { workout ->
                            workout.caloriesBurned.toIntOrNull() ?: 0
                        }
                        val totalUnsyncedCalories = it.unsyncedWorkouts.sumOf { it.caloriesBurned }
                        val totalCalories = totalSyncedCalories + totalUnsyncedCalories

                        // Optionally populate trendData for each HeartRateData (placeholder logic)
                        it.syncedWorkouts.forEach { workout ->
                            workout.heartRateData.forEach { heartRateData ->
                                // Example: Populate trendData with dummy 7-day data
                                // In a real app, you'd calculate this based on historical data
                                heartRateData.trendData.addAll(
                                    listOf("110", "112", "115", "118", "120", "122", "125")
                                )
                            }
                        }

                        // Update the UI on the main thread
                        withContext(Dispatchers.Main) {
                            // Set the progress bar values (assuming a max value of 3000 kcal as an example)
                            val maxCalories = 3000
                            //progressBar.setValues(totalCalories, maxCalories)

                            // Animate the progress
                            val progressPercentage =
                                (totalCalories.toFloat() / maxCalories.toFloat() * 100f).coerceIn(
                                    0f,
                                    100f
                                )
                            //progressBar.setProgress(progressPercentage)
                        }
                    }
                } else {
                    // Handle error response
                    withContext(Dispatchers.Main) {
                        println("Error: ${response.code()} - ${response.message()}")
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
                withContext(Dispatchers.Main) {
                    println("Exception: ${e.message}")
                }
            }
        }
    }
}