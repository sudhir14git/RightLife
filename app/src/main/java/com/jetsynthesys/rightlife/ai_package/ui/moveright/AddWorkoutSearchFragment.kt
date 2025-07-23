package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.graphics.Color
import android.graphics.Typeface
import android.media.Image
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.os.BundleCompat
import androidx.fragment.app.setFragmentResult
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.ActivityModel
import com.jetsynthesys.rightlife.ai_package.model.CalculateCaloriesRequest
import com.jetsynthesys.rightlife.ai_package.model.CalculateCaloriesResponse
import com.jetsynthesys.rightlife.ai_package.model.WorkoutList
import com.jetsynthesys.rightlife.ai_package.model.WorkoutRoutineItem
import com.jetsynthesys.rightlife.ai_package.model.WorkoutSessionRecord
import com.jetsynthesys.rightlife.ai_package.model.request.CreateWorkoutRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateCaloriesRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateRoutineRequest
import com.jetsynthesys.rightlife.ai_package.model.request.Workout
import com.jetsynthesys.rightlife.ai_package.ui.moveright.customProgressBar.CustomProgressBar
import com.jetsynthesys.rightlife.databinding.FragmentAddWorkoutSearchBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.shawnlin.numberpicker.NumberPicker
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class AddWorkoutSearchFragment : BaseFragment<FragmentAddWorkoutSearchBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAddWorkoutSearchBinding
        get() = FragmentAddWorkoutSearchBinding::inflate

    private val handler = Handler(Looper.getMainLooper())
    private lateinit var intensityProgressBar: CustomProgressBar
    private lateinit var caloriesText: TextView
    private lateinit var addLog: LinearLayoutCompat
    private var selectedTime: String = "1 hr 0 min"
    private var selectedIntensity: String = "Low" // Default to API-accepted value
    private var edit: String = ""
    private var calorieBurnedNew: Double = 0.0
    private var edit_routine: String = ""
    private var workout: WorkoutList? = null
    private var activityModel: ActivityModel? = null
    private var workoutModel: WorkoutRoutineItem? = null
    private var lastWorkoutRecord: WorkoutSessionRecord? = null
    private var routine: String = ""
    private var routineName: String = ""
    private lateinit var workoutName : TextView
    private lateinit var workoutIcon : ImageView
    private var workoutListRoutine = ArrayList<WorkoutSessionRecord>()

    // Normalize intensity to match API's expected values
    private fun normalizeIntensity(intensity: String): String {
        return when (intensity.lowercase()) {
            "low" -> "Low"
            "medium", "moderate" -> "Moderate"
            "high" -> "High"
            "very high", "veryhigh" -> "Very High"
            else -> "Low" // Default to Low if unknown
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Retrieve workout from arguments
        routine = arguments?.getString("routine").toString()
        routineName = arguments?.getString("routineName").toString()
        workoutListRoutine = arguments?.getParcelableArrayList("workoutList") ?: ArrayList()
        activityModel = arguments?.getParcelable("ACTIVITY_MODEL")
        workoutModel = arguments?.getParcelable("WORKOUT_MODEL")
        edit = arguments?.getString("edit") ?: ""
        edit_routine = arguments?.getString("edit_routine") ?: ""
        val allworkout = arguments?.getString("allworkout") ?: ""
        workout = arguments?.getParcelable("workout")
        caloriesText = view.findViewById(R.id.calories_text)
        val hourPicker = view.findViewById<NumberPicker>(R.id.hourPicker)
        val minutePicker = view.findViewById<NumberPicker>(R.id.minutePicker)
        addLog = view.findViewById<LinearLayoutCompat>(R.id.layout_btn_log_meal)
        val addSearchFragmentBackButton = view.findViewById<ImageView>(R.id.back_button)
        workoutName = view.findViewById(R.id.workoutName)
        workoutIcon = view.findViewById(R.id.workoutIcon)
        intensityProgressBar = view.findViewById(R.id.customSeekBar)
        hourPicker.minValue = 0
        hourPicker.maxValue = 23
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        if (edit == "edit") {
            if (activityModel == null) {
                Toast.makeText(requireContext(), "No activity data provided for editing", Toast.LENGTH_SHORT).show()
                Log.e("AddWorkoutSearch", "ActivityModel is null in edit mode")
                return
            } else {
                caloriesText.text = activityModel?.caloriesBurned
                val timeStr = activityModel?.duration!!
                val regex = Regex("\\d+")
                val numbers = regex.findAll(timeStr).map { it.value.toInt() }.toList()
                hourPicker.value = numbers.getOrNull(0)!!
                minutePicker.value = numbers.getOrNull(1)!!
                Log.d("AddWorkoutSearch", "Editing activity: ${activityModel?.workoutType}, Calorie ID: ${activityModel?.id}")
            }
        } else if(edit_routine == "edit_routine"){
            if (workoutModel == null) {
                Toast.makeText(requireContext(), "No activity data provided for editing", Toast.LENGTH_SHORT).show()
                Log.e("AddWorkoutSearch", "ActivityModel is null in edit mode")
                return
            } else {
                Log.d("AddWorkoutSearch", "Editing activity: ${activityModel?.workoutType}, Calorie ID: ${activityModel?.id}")
            }
        }
        else {
            if (workout == null) {
                Toast.makeText(requireContext(), "No workout selected", Toast.LENGTH_SHORT).show()
                Log.e("AddWorkoutSearch", "Workout is null")
                navigateToFragment(AllWorkoutFragment(), "AllWorkoutFragment")
                return
            } else {
                Log.d("AddWorkoutSearch", "Workout ID: ${workout?._id}, Title: ${workout?.title}")
            }
        }

        lastWorkoutRecord = arguments?.let { BundleCompat.getParcelable(it, "workoutRecord", WorkoutSessionRecord::class.java) }

        // Initially disable the addLog button until conditions are met
        addLog.isEnabled = false

        addSearchFragmentBackButton.setOnClickListener {
            if (edit.equals("edit")) {
                navigateToFragment(YourActivityFragment(), "AllWorkoutFragment")
            } else if(edit_routine.equals("edit_routine")){
                navigateToFragment(SearchWorkoutFragment(), "SearchWorkoutFragment")
            } else if (routine.equals("routine"))  {
                // Send updated workoutListRoutine to AllWorkoutFragment
                setFragmentResult("workoutListUpdate", Bundle().apply {
                    putParcelableArrayList("workoutList", workoutListRoutine)
                })
                val fragment = CreateRoutineFragment()
                val args = Bundle().apply {
                    putParcelableArrayList("workoutList", workoutListRoutine)
                    putString("routine", routine)
                    putString("routineName", routineName)
                }
                fragment.arguments = args
                navigateToFragment(fragment, "CreateRoutineFragment")
            } else if (allworkout.equals("allworkout")){
                navigateToFragment(SearchWorkoutFragment(), "SearchWorkoutFragment")
            }else{
                navigateToFragment(YourActivityFragment(), "AllWorkoutFragment")
            }
        }

        addLog.setOnClickListener {
            val hours = hourPicker.value
            val minutes = minutePicker.value
            val durationMinutes = hours * 60 + minutes
            if (edit == "edit") {
                activityModel?.id?.let { calorieId ->
                    val normalizedIntensity = normalizeIntensity(selectedIntensity)
                    updateCalories(calorieId, durationMinutes, normalizedIntensity)
                } ?: run {
                    Toast.makeText(requireContext(), "Calorie ID is missing", Toast.LENGTH_SHORT).show()
                }
            } else if (routine.equals("routine")) {
                // Update only the last entry in workoutListRoutine
                if (durationMinutes > 0) {
                    updateLastEntryCalories(durationMinutes, normalizeIntensity(selectedIntensity))
                } else {
                    Toast.makeText(requireContext(), "Please select a duration", Toast.LENGTH_SHORT).show()
                }
            }else if(edit_routine.equals("edit_routine")){
                workoutModel?.activityId?.let { activityId ->
                    val normalizedIntensity = normalizeIntensity(selectedIntensity)
                    updateWorkoutRoutine(workoutModel!!.routineId,
                        workoutModel!!.activityId, durationMinutes, normalizedIntensity,calorieBurnedNew)
                } ?: run {
                    Toast.makeText(requireContext(), "Calorie ID is missing", Toast.LENGTH_SHORT).show()
                }
            } else {
                workout?.let { workout ->
                    if (durationMinutes > 0) {
                        val normalizedIntensity = normalizeIntensity(selectedIntensity)
                        val newWorkoutRecord = WorkoutSessionRecord(
                            userId =  SharedPreferenceManager.getInstance(requireActivity()).userId,
                            activityId = workout._id,
                            durationMin = durationMinutes,
                            intensity = normalizedIntensity,
                            sessions = 1,
                            message = lastWorkoutRecord?.message,
                            caloriesBurned = lastWorkoutRecord?.caloriesBurned,
                            activityFactor = lastWorkoutRecord?.activityFactor,
                            moduleName = workout.title.toString()
                        )

                        lastWorkoutRecord = newWorkoutRecord
                        if (lastWorkoutRecord?.caloriesBurned == null) {
                            Toast.makeText(requireContext(), "Calculating calories...", Toast.LENGTH_SHORT).show()
                            val activityId = workout._id ?: activityModel?.id
                            if (activityId != null) {
                                calculateUserCalories(durationMinutes, normalizedIntensity, activityId)
                            } else {
                                Toast.makeText(requireContext(), "Activity ID is missing", Toast.LENGTH_SHORT).show()
                            }
                        }
                        createWorkout(lastWorkoutRecord)
                    } else {
                        Toast.makeText(requireContext(), "Please select a duration", Toast.LENGTH_SHORT).show()
                    }
                } ?: run {
                    Toast.makeText(requireContext(), "Please select a workout", Toast.LENGTH_SHORT).show()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (edit.equals("edit")) {
                    navigateToFragment(YourActivityFragment(), "AllWorkoutFragment")
                } else if(edit_routine.equals("edit_routine")){
                    navigateToFragment(SearchWorkoutFragment(), "SearchWorkoutFragment")
                } else if (routine.equals("routine"))  {
                    // Send updated workoutListRoutine to AllWorkoutFragment
                    setFragmentResult("workoutListUpdate", Bundle().apply {
                        putParcelableArrayList("workoutList", workoutListRoutine)
                    })
                    val fragment = CreateRoutineFragment()
                    val args = Bundle().apply {
                        putParcelableArrayList("workoutList", workoutListRoutine)
                        putString("routine", routine)
                        putString("routineName", routineName)
                    }
                    fragment.arguments = args
                    navigateToFragment(fragment, "CreateRoutineFragment")
                } else if (allworkout.equals("allworkout")){
                    navigateToFragment(SearchWorkoutFragment(), "SearchWorkoutFragment")
                }else{
                    navigateToFragment(YourActivityFragment(), "AllWorkoutFragment")
                }
            }
        })

        // Prefill data in edit mode
        if (edit == "edit" && activityModel != null) {
            val timeStr = activityModel?.duration!!
            val regex = Regex("\\d+")
            val numbers = regex.findAll(timeStr).map { it.value.toInt() }.toList()
            hourPicker.value = numbers.getOrNull(0)!!
            minutePicker.value = numbers.getOrNull(1)!!
            selectedIntensity = normalizeIntensity(activityModel?.intensity ?: "Low")
            // Set progress based on intensity (progress range is 0 to 1)
            when (selectedIntensity) {
                "Low" -> intensityProgressBar.progress = 0.0f
                "Moderate" -> intensityProgressBar.progress = 0.3333f
                "High" -> intensityProgressBar.progress = 0.6666f
                "Very High" -> intensityProgressBar.progress = 1.0f
            }
            Log.d("AddWorkoutSearch", "Edit mode - Initial intensity: $selectedIntensity, Progress: ${intensityProgressBar.progress}")
            selectedTime = "${numbers.getOrNull(0)!!} hr ${numbers.getOrNull(1)!!} min"
            // Trigger initial calorie calculation in edit mode
            activityModel?.id?.let { calorieId ->
             //   calculateUserCalories(durationMin, selectedIntensity, calorieId)
                addLog.isEnabled = true
            }
        }else if(edit_routine.equals("edit_routine")&& workoutModel != null){
            val durationMin = workoutModel?.duration?.replace(" mins", "")?.toIntOrNull() ?: 0
            val hours = durationMin / 60
            val minutes = durationMin % 60
            hourPicker.value = hours
            minutePicker.value = minutes
            selectedIntensity = normalizeIntensity(workoutModel?.intensity ?: "Low")
            // Set progress based on intensity (progress range is 0 to 1)
            when (selectedIntensity) {
                "Low" -> intensityProgressBar.progress = 0.0f
                "Moderate" -> intensityProgressBar.progress = 0.3333f
                "High" -> intensityProgressBar.progress = 0.6666f
                "Very High" -> intensityProgressBar.progress = 1.0f
            }
            Log.d("AddWorkoutSearch", "Edit mode - Initial intensity: $selectedIntensity, Progress: ${intensityProgressBar.progress}")
            selectedTime = "$hours hr ${minutes.toString().padStart(2, '0')} min"
            // Trigger initial calorie calculation in edit mode
            workoutModel?.activityId?.let { activityId ->
              //  calculateUserCalories(durationMin, selectedIntensity, activityId)
                addLog.isEnabled = true
            }
        }

        fun updateNumberPickerText(picker: NumberPicker) {
            try {
                val field = NumberPicker::class.java.getDeclaredField("mInputText")
                field.isAccessible = true
                val editText = field.get(picker) as EditText
                editText.setTextColor(Color.RED)
                editText.textSize = 24f
                editText.typeface = Typeface.DEFAULT_BOLD
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        fun refreshPickers() {
            handler.post {
                updateNumberPickerText(hourPicker)
                updateNumberPickerText(minutePicker)
            }
        }

        val timeListener = NumberPicker.OnValueChangeListener { _, _, _ ->
            val hours = hourPicker.value
            val minutes = minutePicker.value
            selectedTime = "$hours hr ${minutes.toString().padStart(2, '0')} min"
            val durationMinutes = hours * 60 + minutes
            if (durationMinutes > 0) {
                val activityId = if (edit == "edit") activityModel?.activityId else if (edit_routine.equals("edit_routine")) workoutModel?.activityId else workout?._id
                if (activityId != null) {
                    val normalizedIntensity = normalizeIntensity(selectedIntensity)
                    calculateUserCalories(durationMinutes, normalizedIntensity, activityId)
                    addLog.isEnabled = true
                } else {
                    Toast.makeText(requireContext(), "Activity ID is missing", Toast.LENGTH_SHORT).show()
                }
            } else {
                addLog.isEnabled = false
            }
            refreshPickers()
        }

        hourPicker.setOnValueChangedListener(timeListener)
        minutePicker.setOnValueChangedListener(timeListener)
        refreshPickers()

        // Set initial progress for non-edit mode
        if (edit != "edit") {
            intensityProgressBar.progress = 0.0f // Default to "Low"
        }else if (edit_routine.equals("edit_routine")){
            intensityProgressBar.progress = 0.0f // Default to "Low"
        }

        intensityProgressBar.setOnProgressChangedListener { progress ->
            // Use the getCurrentIntensity() method from CustomProgressBar to get the intensity
            selectedIntensity = intensityProgressBar.getCurrentIntensity()
            Log.d("AddWorkoutSearch", "Progress: $progress, Intensity set to: $selectedIntensity")
            val hours = hourPicker.value
            val minutes = minutePicker.value
            val durationMinutes = hours * 60 + minutes
            if (durationMinutes > 0) {
                val activityId = if (edit == "edit") activityModel?.activityId else if (edit_routine.equals("edit_routine")) workoutModel?.activityId else workout?._id
                if (activityId != null) {
                    //caloriesText.text = "Calculating..."
                    calculateUserCalories(durationMinutes, selectedIntensity, activityId)
                    addLog.isEnabled = true
                } else {
                    Toast.makeText(requireContext(), "Activity ID is missing", Toast.LENGTH_SHORT).show()
                    addLog.isEnabled = false
                }
            } else {
                Toast.makeText(requireContext(), "Please select a duration", Toast.LENGTH_SHORT).show()
                addLog.isEnabled = false
            }
        }

        if (workout != null){
            workoutName.text = workout?.title
            val imageBaseUrl = "https://d1sacaybzizpm5.cloudfront.net/" + workout?.iconUrl
            Glide.with(requireContext())
                .load(imageBaseUrl)
                .placeholder(R.drawable.athelete_search)
                .error(R.drawable.athelete_search)
                .into(workoutIcon)
        }else if (activityModel != null){
            workoutName.text = activityModel?.workoutType
            val imageBaseUrl = activityModel?.icon
            Glide.with(requireContext())
                .load(imageBaseUrl)
                .placeholder(R.drawable.athelete_search)
                .error(R.drawable.athelete_search)
                .into(workoutIcon)
        }else if (workoutModel != null){
            workoutName.text = workoutModel?.activityName
        }

        // Initial API call with default values if in non-edit mode
        if (edit != "edit") {
            workout?.let { workout ->
                calculateUserCalories(60, selectedIntensity, workout.activityId) // 1 hr default
                addLog.isEnabled = true
            }
        }else if (edit_routine.equals("edit_routine")){
            workoutModel?.let { workout ->
                calculateUserCalories(60, selectedIntensity, workout.activityId) // 1 hr default
                addLog.isEnabled = true
            }
        }
    }

    private fun updateLastEntryCalories(durationMinutes: Int, normalizedIntensity: String) {
        if (workoutListRoutine.isEmpty()) {
            Toast.makeText(requireContext(), "Workout list is empty", Toast.LENGTH_SHORT).show()
            navigateToCreateRoutineFragment()
            return
        }

        // Get the last entry in workoutListRoutine
        val lastIndex = workoutListRoutine.size - 1
        val lastEntry = workoutListRoutine[lastIndex]

        if (lastEntry.activityId == null) {
            Toast.makeText(requireContext(), "Activity ID missing for last workout", Toast.LENGTH_SHORT).show()
            navigateToCreateRoutineFragment()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                val request = CalculateCaloriesRequest(
                    userId = userId,
                    activityId = lastEntry.activityId!!,
                    durationMin = durationMinutes,
                    intensity = selectedIntensity,
                    sessions = lastEntry.sessions,
                    date = getCurrentDate()
                )
                val response: Response<CalculateCaloriesResponse> = ApiClient.apiServiceFastApi.calculateCalories(request)
                if (response.isSuccessful) {
                    val caloriesResponse = response.body()
                    if (caloriesResponse != null) {
                        // Update the last entry in workoutListRoutine
                        workoutListRoutine[lastIndex] = lastEntry.copy(
                            durationMin = durationMinutes,
                            intensity = normalizedIntensity,
                            message = caloriesResponse.message,
                            caloriesBurned = caloriesResponse.caloriesBurned,
                            activityFactor = caloriesResponse.activityFactor
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "Updated calories for ${lastEntry.moduleName}: ${caloriesResponse.caloriesBurned} kcal",
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToCreateRoutineFragment()
                        }
                    } else {
                        // Update the last entry without calories data
                        workoutListRoutine[lastIndex] = lastEntry.copy(
                            durationMin = durationMinutes,
                            intensity = normalizedIntensity
                        )
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                requireContext(),
                                "No calories data received for ${lastEntry.moduleName}",
                                Toast.LENGTH_SHORT
                            ).show()
                            navigateToCreateRoutineFragment()
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("CalculateCalories", "Error for ${lastEntry.moduleName}: ${response.code()} - ${response.message()}, Body: $errorBody")
                    // Update the last entry without calories data
                    workoutListRoutine[lastIndex] = lastEntry.copy(
                        durationMin = durationMinutes,
                        intensity = normalizedIntensity
                    )
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error for ${lastEntry.moduleName}: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_LONG
                        ).show()
                        navigateToCreateRoutineFragment()
                    }
                }
            } catch (e: Exception) {
                // Update the last entry without calories data
                workoutListRoutine[lastIndex] = lastEntry.copy(
                    durationMin = durationMinutes,
                    intensity = normalizedIntensity
                )
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Exception for ${lastEntry.moduleName}: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    navigateToCreateRoutineFragment()
                }
                Log.e("CalculateCalories", "Exception for ${lastEntry.moduleName}: ${e.stackTraceToString()}")
            }
        }
    }

    private fun navigateToCreateRoutineFragment() {
        // Log the data being sent
        Log.d("AddWorkoutSearchFragment", "Sending workoutList to CreateRoutineFragment: $workoutListRoutine")

        // Send updated workoutListRoutine to AllWorkoutFragment
        setFragmentResult("workoutListUpdate", Bundle().apply {
            putParcelableArrayList("workoutList", workoutListRoutine)
        })

        // Navigate to CreateRoutineFragment and pass the updated ArrayList
        val createRoutineFragment = CreateRoutineFragment()
        val args = Bundle().apply {
            putParcelableArrayList("workoutList", workoutListRoutine)
            putString("routine", routine)
            putString("routineName", routineName)
        }
        createRoutineFragment.arguments = args
        navigateToFragment(createRoutineFragment, "CreateRoutineFragment")
    }

    private fun getCurrentDate(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return dateFormat.format(Date())
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun createWorkout(workoutSession: WorkoutSessionRecord?) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                val currentDate = getCurrentDate()
                val request = workoutSession?.let {
                    CreateWorkoutRequest(
                        userId = userId,
                        activityId = it.activityId,
                        durationMin = it.durationMin,
                        intensity = it.intensity,
                        sessions = 1,
                        date = currentDate
                    )
                } ?: run {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Workout data is missing", Toast.LENGTH_SHORT).show()
                    }
                    return@launch
                }

                val response = ApiClient.apiServiceFastApi.createWorkout(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        responseBody?.let {
                            val fragment = YourActivityFragment()
                            requireActivity().supportFragmentManager.beginTransaction().apply {
                                replace(R.id.flFragment, fragment, "YourActivityFragment")
                                addToBackStack("YourActivityFragment")
                                commit()
                            }
                            Toast.makeText(requireContext(), "Workout Created Successfully", Toast.LENGTH_SHORT).show()
                        } ?: Toast.makeText(
                            requireContext(),
                            "Empty response",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error creating workout: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Exception: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("AddWorkoutSearch", "Exception in createWorkout: ${e.stackTraceToString()}")
            }
        }
    }

    private fun calculateUserCalories(durationMinutes: Int, selectedIntensity: String, activityId: String, navigateToRoutine: Boolean = false) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                val request = CalculateCaloriesRequest(
                    userId = userId,
                    activityId = activityId,
                    durationMin = durationMinutes,
                    intensity = selectedIntensity,
                    sessions = 1,
                    date = getCurrentDate()
                )
                val requestJson = Gson().toJson(request)
                Log.d("CalculateCalories", "Request: $requestJson")

                val response: Response<CalculateCaloriesResponse> = ApiClient.apiServiceFastApi.calculateCalories(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val caloriesResponse = response.body()
                        if (caloriesResponse != null) {
                            lastWorkoutRecord = workout?.title?.let {
                                WorkoutSessionRecord(
                                    userId = request.userId,
                                    activityId = request.activityId,
                                    durationMin = request.durationMin,
                                    intensity = request.intensity,
                                    sessions = request.sessions,
                                    message = caloriesResponse.message,
                                    caloriesBurned = caloriesResponse.caloriesBurned,
                                    activityFactor = caloriesResponse.activityFactor,
                                    moduleName = it
                                )
                            }
                            calorieBurnedNew = caloriesResponse.caloriesBurned
                            caloriesText.text = caloriesResponse.caloriesBurned.toInt().toString()
                           /* Toast.makeText(
                                requireContext(),
                                "Calories Burned: ${String.format("%.2f", caloriesResponse.caloriesBurned)} kcal",
                                Toast.LENGTH_SHORT
                            ).show()*/
                            // Navigate to CreateRoutineFragment if required
                            if (navigateToRoutine) {
                                navigateToCreateRoutineFragment()
                            }
                        } else {
                            lastWorkoutRecord = workout?.let {
                                WorkoutSessionRecord(
                                    userId = request.userId,
                                    activityId = request.activityId,
                                    durationMin = request.durationMin,
                                    intensity = request.intensity,
                                    sessions = request.sessions,
                                    moduleName = it.title
                                )
                            }
                            //caloriesText.text = "N/A"
                           // Toast.makeText(requireContext(), "No calories data received", Toast.LENGTH_SHORT).show()
                            // Navigate even if API response is empty, as lastWorkoutRecord is set
                            if (navigateToRoutine) {
                                navigateToCreateRoutineFragment()
                            }
                        }
                    } else {
                        lastWorkoutRecord = workout?.let {
                            WorkoutSessionRecord(
                                userId = request.userId,
                                activityId = request.activityId,
                                durationMin = request.durationMin,
                                intensity = request.intensity,
                                sessions = request.sessions,
                                moduleName = it.title
                            )
                        }
                        val errorBody = response.errorBody()?.string()
                        Log.e("CalculateCalories", "Error: ${response.code()} - ${response.message()}, Body: $errorBody")
                        //caloriesText.text = "Error"
                        Toast.makeText(requireContext(), "Error: ${response.code()} - ${response.message()}\nDetails: $errorBody", Toast.LENGTH_LONG).show()
                        // Navigate even if API fails, as lastWorkoutRecord is set
                        if (navigateToRoutine) {
                            navigateToCreateRoutineFragment()
                        }
                    }
                }
            } catch (e: Exception) {
//                lastWorkoutRecord = workout?.let {
//                    WorkoutSessionRecord(userId = "64763fe2fa0e40d9c0bc8264", activityId = activityId, durationMin = durationMinutes, intensity = selectedIntensity, sessions = 1, moduleName = it.title)
//                }
                withContext(Dispatchers.Main) {
                    //caloriesText.text = "Error"
                    Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                    // Navigate even if an exception occurs, as lastWorkoutRecord is set
                    if (navigateToRoutine) {
                        navigateToCreateRoutineFragment()
                    }
                }
                Log.e("CalculateCalories", "Exception: ${e.stackTraceToString()}")
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun updateCalories(calorieId: String, durationMinutes: Int, intensity: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                    ?: "64763fe2fa0e40d9c0bc8264"

                val currentDate = getCurrentDate()
                val request = UpdateCaloriesRequest(
                    userId = userId,
                    durationMin = durationMinutes,
                    intensity = intensity,
                    sessions = 1,
                    date = currentDate
                )

                val response = ApiClient.apiServiceFastApi.updateCalories(calorieId, request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        responseBody?.let {
                            val fragment = YourActivityFragment()
                            requireActivity().supportFragmentManager.beginTransaction().apply {
                                replace(R.id.flFragment, fragment, "YourActivityFragment")
                                addToBackStack("YourActivityFragment")
                                commit()
                            }
                            Toast.makeText(requireContext(), "Calories Updated Successfully", Toast.LENGTH_SHORT).show()
                        } ?: Toast.makeText(
                            requireContext(),
                            "Empty response",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error updating calories: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Exception: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e("UpdateCalories", "Exception in updateCalories: ${e.stackTraceToString()}")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun updateWorkoutRoutine(
        routineId: String,
        activityId: String,
        durationMinutes: Int,
        intensity: String,
        caloriesBurned: Double
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                    ?: "680790d0a8d2c1b4456e5c7d"

                val workouts = listOf(
                    Workout(
                        activityId = activityId,
                        intensity = intensity,
                        duration = durationMinutes,
                        calories_burned = caloriesBurned
                    )
                )

                val request = UpdateRoutineRequest(
                    user_id = userId,
                    routine_id = routineId,
                    workouts = workouts
                )

                val response = ApiClient.apiServiceFastApi.updateRoutine(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        responseBody?.let {
                            val fragment =
                                SearchWorkoutFragment() // Replace with the fragment you want to navigate to
                            requireActivity().supportFragmentManager.beginTransaction().apply {
                                replace(R.id.flFragment, fragment, "MyRoutineFragment")
                                addToBackStack("MyRoutineFragment")
                                commit()
                            }
                            Toast.makeText(
                                requireContext(),
                                "Routine Updated Successfully",
                                Toast.LENGTH_SHORT
                            ).show()
                        } ?: Toast.makeText(
                            requireContext(),
                            "Empty response",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error updating routine: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        requireContext(),
                        "Exception: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
                Log.e(
                    "UpdateRoutine",
                    "Exception in updateWorkoutRoutine: ${e.stackTraceToString()}"
                )
            }
        }
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }
}