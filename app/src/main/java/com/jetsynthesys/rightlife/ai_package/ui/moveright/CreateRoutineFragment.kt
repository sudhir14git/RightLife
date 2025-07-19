package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.graphics.drawable.Drawable
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.setFragmentResult
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.ActivityModel
import com.jetsynthesys.rightlife.ai_package.model.CreateRoutineRequest
import com.jetsynthesys.rightlife.ai_package.model.RoutineWorkoutDisplayModel
import com.jetsynthesys.rightlife.ai_package.model.WorkoutSessionRecord
import com.jetsynthesys.rightlife.ai_package.ui.adapter.RoutineWorkoutListAdapter
import com.jetsynthesys.rightlife.databinding.FragmentCreateRoutineBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CreateRoutineFragment : BaseFragment<FragmentCreateRoutineBinding>() {
    private lateinit var editText: EditText
    private lateinit var textViewRoutine: TextView
    private lateinit var createRoutineBackButton: ImageView
    private lateinit var edit_icon_create_routine: ImageView
    private lateinit var createRoutineRecyclerView: RecyclerView
    private lateinit var layoutBtnLog: LinearLayoutCompat
    private lateinit var addBtnLog: LinearLayoutCompat
    private lateinit var save_workout_routine_btn: LinearLayoutCompat
    private lateinit var addNameLayout: ConstraintLayout
    private lateinit var createListRoutineLayout: ConstraintLayout
    private var workoutList = ArrayList<WorkoutSessionRecord>()
    private var routine: String = ""
    private var routineName: String = ""

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCreateRoutineBinding
        get() = FragmentCreateRoutineBinding::inflate

    private val routineWorkoutListAdapter by lazy {
        RoutineWorkoutListAdapter(
            requireContext(),
            arrayListOf(),
            ::onWorkoutItemClick,
            ::onWorkoutItemRemove // Pass the new callback
        )
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.gradient_color_background_workout)
        routine = arguments?.getString("routine").toString()
        routineName = arguments?.getString("routineName").toString()
        workoutList = arguments?.getParcelableArrayList("workoutList") ?: ArrayList()

        // Fetch activityList from YourActivityFragment
        val activityList = arguments?.getParcelableArrayList<ActivityModel>("ACTIVITY_LIST") ?: ArrayList()
        Log.d("CreateRoutineFragment", "Received ${activityList.size} activities from YourActivityFragment")

        // Map ActivityModel to WorkoutSessionRecord and append to workoutList
        if (activityList.isNotEmpty()) {
            mapActivityModelToWorkoutSessionRecord(activityList)
        }

        createRoutineRecyclerView = view.findViewById(R.id.recyclerview_my_meals_item)
        editText = view.findViewById(R.id.editText)
        edit_icon_create_routine = view.findViewById(R.id.edit_icon_create_routine)
        textViewRoutine = view.findViewById(R.id.name_routine_text_view)
        layoutBtnLog = view.findViewById(R.id.layout_btn_log)
        createRoutineBackButton = view.findViewById(R.id.back_button)
        addNameLayout = view.findViewById(R.id.add_name_layout)
        addBtnLog = view.findViewById(R.id.add_btn_log)
        save_workout_routine_btn = view.findViewById(R.id.save_workout_routine_btn)
        createListRoutineLayout = view.findViewById(R.id.list_create_routine_layout)
        edit_icon_create_routine.setOnClickListener {
            addNameLayout.visibility = View.VISIBLE
            createListRoutineLayout.visibility = View.GONE
            editText.setText(textViewRoutine.text.toString())
        }

        // Show/hide layouts based on routine mode
        if (routine == "routine") {
            if (workoutList.isNotEmpty()) {
                addNameLayout.visibility = View.GONE
                createListRoutineLayout.visibility = View.VISIBLE
                textViewRoutine.text = routineName
                // Map workoutList to RoutineWorkoutDisplayModel and update the adapter
                val routineWorkoutModels = mapWorkoutSessionRecordsToRoutineWorkoutModels(workoutList)
                routineWorkoutListAdapter.setData(routineWorkoutModels)
                createRoutineRecyclerView.visibility = View.VISIBLE
            } else {
                addNameLayout.visibility = View.GONE
                createListRoutineLayout.visibility = View.VISIBLE
                textViewRoutine.text = routineName
                createRoutineRecyclerView.visibility = View.GONE
            }
        } else {
            addNameLayout.visibility = View.VISIBLE
            createListRoutineLayout.visibility = View.GONE
        }

        addBtnLog.setOnClickListener {
            val fragment = SearchWorkoutFragment()
            val args = Bundle().apply {
                putString("routine", "routine")
                putString("routineName", textViewRoutine.text.toString())
                putParcelableArrayList("workoutList", workoutList)
            }
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                addToBackStack(null)
                commit()
            }
        }

        save_workout_routine_btn.setOnClickListener {
            if (textViewRoutine.text.isNotEmpty()) {
                if (workoutList.isNotEmpty()) {
                    createRoutine(textViewRoutine.text.toString())
                } else {
                    Toast.makeText(requireContext(), "No workouts added to the routine", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(), "Routine name is empty", Toast.LENGTH_SHORT).show()
            }
        }

        val defaultBackground: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.add_cart_button_background)
        val filledBackground: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_filled)

        createRoutineRecyclerView.layoutManager = LinearLayoutManager(context)
        createRoutineRecyclerView.adapter = routineWorkoutListAdapter

        createRoutineBackButton.setOnClickListener {
            navigateToFragment(YourActivityFragment(), "AllWorkoutFragment")
        }

        editText.addTextChangedListener(object : TextWatcher {
            private val maxLength = 20
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > maxLength) {
                    // Truncate the text to maxLength
                    s.replace(maxLength, s.length, "")
                    // Show Toast on the main thread
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(editText.context, "Name cannot exceed $maxLength characters", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    layoutBtnLog.background = filledBackground
                    layoutBtnLog.isEnabled = false
                } else {
                    layoutBtnLog.background = defaultBackground
                    layoutBtnLog.isEnabled = true
                }
            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setFragmentResult("workoutListUpdate", Bundle().apply {
                    putParcelableArrayList("workoutList", workoutList)
                })
                navigateToFragment(YourActivityFragment(), "AllWorkoutFragment")
            }
        })

        layoutBtnLog.setOnClickListener {
            addNameLayout.visibility = View.GONE
            createListRoutineLayout.visibility = View.VISIBLE
            textViewRoutine.text = editText.text
        }
    }

    // Function to map ActivityModel to WorkoutSessionRecord
    private fun mapActivityModelToWorkoutSessionRecord(activityList: ArrayList<ActivityModel>) {
        activityList.forEach { activity ->
            val durationValue = activity.duration?.replace(Regex("[^0-9]"), "")?.toIntOrNull() ?: 0
            val caloriesValue = activity.caloriesBurned?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull()

            val workoutRecord = WorkoutSessionRecord(
                userId = activity.userId!!,
                activityId = activity.id!!,
                durationMin = durationValue,
                intensity = activity.intensity!!,
                sessions = 1,
                moduleName = activity.workoutType!!,
                caloriesBurned = caloriesValue,
                message = null,
                activityFactor = null
            )
            workoutList.add(workoutRecord)
            Log.d("CreateRoutineFragment", "Added workout: ${workoutRecord.moduleName} to workoutList")
            val routineWorkoutModels = mapWorkoutSessionRecordsToRoutineWorkoutModels(workoutList)
            routineWorkoutListAdapter.setData(routineWorkoutModels)
        }
    }

    // Function to map WorkoutSessionRecord to RoutineWorkoutDisplayModel
    private fun mapWorkoutSessionRecordsToRoutineWorkoutModels(records: ArrayList<WorkoutSessionRecord>): ArrayList<RoutineWorkoutDisplayModel> {
        return records.map { record ->
            RoutineWorkoutDisplayModel(
                name = record.moduleName,
                duration = "${record.durationMin} min",
                caloriesBurned = record.caloriesBurned?.toString() ?: "N/A",
                intensity = record.intensity
            )
        }.toCollection(ArrayList())
    }

    // New function to handle item removal from workoutList
    private fun onWorkoutItemRemove(position: Int) {
        if (position >= 0 && position < workoutList.size) {
            workoutList.removeAt(position)
            Log.d("CreateRoutineFragment", "Removed workout at position $position, new workoutList size: ${workoutList.size}")
            // Update adapter with new mapped list
            val routineWorkoutModels = mapWorkoutSessionRecordsToRoutineWorkoutModels(workoutList)
            routineWorkoutListAdapter.setData(routineWorkoutModels)
            // Update UI visibility
            if (workoutList.isEmpty()) {
                createRoutineRecyclerView.visibility = View.GONE
            }
        } else {
            Log.e("CreateRoutineFragment", "Invalid position for removal: $position")
        }
    }

    private fun onWorkoutItemClick(workoutModel: RoutineWorkoutDisplayModel, position: Int) {
        Log.d("WorkoutClick", "Clicked on ${workoutModel.name} at position $position")
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun createRoutine(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userid = SharedPreferenceManager.getInstance(requireActivity()).userId
                    ?: "64763fe2fa0e40d9c0bc8264"

                // Map workoutList to CreateRoutineRequest.Workout
                val workoutRequests = workoutList.map { record ->
                    CreateRoutineRequest.Workout(
                        activityId = record.activityId,
                        duration = record.durationMin,
                        intensity = record.intensity
                    )
                }

                val request = CreateRoutineRequest(
                    user_id = userid,
                    routine_name = name,
                    workouts = workoutRequests
                )

                val response = ApiClient.apiServiceFastApi.createRoutine(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        Toast.makeText(
                            requireContext(),
                            responseBody?.message ?: "Routine created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        val fragment = SearchWorkoutFragment()
                        val bundle = Bundle().apply {
                            putInt("selectedTab", 1) // My Routine tab
                        }
                        fragment.arguments = bundle
                        navigateToFragment(fragment, "SearchWorkoutFragment")
                    } else {
                        Toast.makeText(
                            requireContext(),
                            "Error creating routine: ${response.code()} - ${response.message()}",
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
            }
        }
    }
}