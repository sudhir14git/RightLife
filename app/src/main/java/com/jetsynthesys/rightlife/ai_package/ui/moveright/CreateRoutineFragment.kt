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
import com.jetsynthesys.rightlife.ai_package.model.WorkoutRoutineItem
import com.jetsynthesys.rightlife.ai_package.model.WorkoutSessionRecord
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateRoutineRequest
import com.jetsynthesys.rightlife.ai_package.model.request.Workout
import com.jetsynthesys.rightlife.ai_package.ui.adapter.RoutineWorkoutListAdapter
import com.jetsynthesys.rightlife.databinding.FragmentCreateRoutineBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class CreateRoutineFragment : BaseFragment<FragmentCreateRoutineBinding>() {
    private lateinit var editText: EditText
    private lateinit var textViewRoutine: TextView
    private lateinit var no_workout_heading: TextView
    private lateinit var no_workout_discription: TextView
    private lateinit var createRoutineBackButton: ImageView
    private lateinit var edit_icon_create_routine: ImageView
    private lateinit var createRoutineRecyclerView: RecyclerView
    private lateinit var layoutBtnLog: LinearLayoutCompat
    private lateinit var addBtnLog: LinearLayoutCompat
    private lateinit var save_workout_routine_btn: LinearLayoutCompat
    private lateinit var addNameLayout: ConstraintLayout
    private lateinit var createListRoutineLayout: ConstraintLayout
    private var workoutList = ArrayList<WorkoutSessionRecord>()
    private var workoutLists: WorkoutRoutineItem? = null
    private var routine: String = ""
    private var myroutine: String = ""
    private var routineName: String = ""
    private var editRoutine: String = ""
    private var routineIdworkout: String = ""
    private var newBooleanBack:Boolean = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCreateRoutineBinding
        get() = FragmentCreateRoutineBinding::inflate

    private val routineWorkoutListAdapter by lazy {
        RoutineWorkoutListAdapter(
            requireContext(),
            arrayListOf(),
            ::onWorkoutItemClick,
            ::onListEmptyCallback,
            ::onWorkoutItemRemove
        )
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.gradient_color_background_workout)
        routine = arguments?.getString("routine").toString()
        myroutine = arguments?.getString("myRoutine").toString()
        routineName = arguments?.getString("routineName").toString()
        editRoutine = arguments?.getString("edit_routine").toString()
        val  newroutineIdworkout = arguments?.getString("routineId").toString()
        workoutList = arguments?.getParcelableArrayList<WorkoutSessionRecord>("workoutList") ?: ArrayList()
        // Fetch activityList from YourActivityFragment
        val activityList = arguments?.getParcelableArrayList<ActivityModel>("ACTIVITY_LIST") ?: ArrayList()
        workoutLists = arguments?.getParcelable<WorkoutRoutineItem>("WORKOUT_MODEL")
        routineIdworkout = (workoutLists?.routineId?.takeIf { it.isNotEmpty() }
            ?: arguments?.getString("routineId")).toString()

        Log.d("CreateRoutineFragment", "Received ${routineIdworkout} activities from YourActivityFragment")

        // Map ActivityModel to WorkoutSessionRecord and append to workoutList
        if (activityList.isNotEmpty()) {
            mapActivityModelToWorkoutSessionRecord(activityList)
        }

        // Map PlanExerciseWorkout to WorkoutSessionRecord and append to workoutList
        if (workoutLists != null) {
            mapPlanExerciseWorkoutToWorkoutSessionRecord(workoutLists!!)
        }

        createRoutineRecyclerView = view.findViewById(R.id.recyclerview_my_meals_item)
        editText = view.findViewById(R.id.editText)
        edit_icon_create_routine = view.findViewById(R.id.edit_icon_create_routine)
        textViewRoutine = view.findViewById(R.id.name_routine_text_view)
        no_workout_heading = view.findViewById(R.id.no_workout_heading)
        no_workout_discription = view.findViewById(R.id.no_workout_discription)
        layoutBtnLog = view.findViewById(R.id.layout_btn_log)
        createRoutineBackButton = view.findViewById(R.id.back_button)
        addNameLayout = view.findViewById(R.id.add_name_layout)
        addBtnLog = view.findViewById(R.id.add_btn_log)
        save_workout_routine_btn = view.findViewById(R.id.save_workout_routine_btn)
        createListRoutineLayout = view.findViewById(R.id.list_create_routine_layout)
        edit_icon_create_routine.setOnClickListener {
            addNameLayout.visibility = View.VISIBLE
            newBooleanBack = true
            createListRoutineLayout.visibility = View.GONE
            editText.setText(textViewRoutine.text.toString())
        }

        // Show/hide layouts based on routine mode
        if (routine == "routine") {
            if (workoutList.isNotEmpty()) {
                addNameLayout.visibility = View.GONE
                createListRoutineLayout.visibility = View.VISIBLE
                newBooleanBack = true
                textViewRoutine.text = routineName
                // Map workoutList to RoutineWorkoutDisplayModel and update the adapter
                val routineWorkoutModels = mapWorkoutSessionRecordsToRoutineWorkoutModels(workoutList)
                routineWorkoutListAdapter.setData(routineWorkoutModels)
                createRoutineRecyclerView.visibility = View.VISIBLE
            } else {
                addNameLayout.visibility = View.GONE
                createListRoutineLayout.visibility = View.VISIBLE
                newBooleanBack = true
                textViewRoutine.text = routineName
                createRoutineRecyclerView.visibility = View.GONE
            }
        } else if(routine.equals("edit_routine")||editRoutine.equals("edit_routine")){
            if (workoutList.isNotEmpty()) {
                addNameLayout.visibility = View.GONE
                createListRoutineLayout.visibility = View.VISIBLE
                newBooleanBack = true
                //textViewRoutine.text = routineName
                textViewRoutine.text = workoutLists?.routineName?:routineName
                // Map workoutList to RoutineWorkoutDisplayModel and update the adapter
                val routineWorkoutModels = mapWorkoutSessionRecordsToRoutineWorkoutModels(workoutList)
                routineWorkoutListAdapter.setData(routineWorkoutModels)
                createRoutineRecyclerView.visibility = View.VISIBLE
            } else {
                addNameLayout.visibility = View.GONE
                createListRoutineLayout.visibility = View.VISIBLE
                newBooleanBack = true
                textViewRoutine.text = routineName
                createRoutineRecyclerView.visibility = View.GONE
            }
        } else{
            addNameLayout.visibility = View.VISIBLE
            createListRoutineLayout.visibility = View.GONE
            newBooleanBack = true
            routine = "routine"
        }
        if(workoutList.isEmpty()){
            no_workout_discription.visibility = View.VISIBLE
            no_workout_heading.visibility = View.VISIBLE
        }else{
            no_workout_discription.visibility = View.GONE
            no_workout_heading.visibility = View.GONE
        }

        /* if (workoutLists != null) {
             addNameLayout.visibility = View.GONE
             createListRoutineLayout.visibility = View.VISIBLE
             textViewRoutine.text = workoutLists?.routineName
             // Map workoutList to RoutineWorkoutDisplayModel and update the adapter
             val routineWorkoutModels = mapWorkoutSessionRecordsToRoutineWorkoutModels(workoutList)
             routineWorkoutListAdapter.setData(routineWorkoutModels)
             createRoutineRecyclerView.visibility = View.VISIBLE
         }*/

        addBtnLog.setOnClickListener {
            if (editRoutine == "edit_routine") {
                val fragment = SearchWorkoutFragment()
                val args = Bundle().apply {
                    putString("routine", "edit_routine")
                    putString("routineId",routineIdworkout)
                    putString("routineName", textViewRoutine.text.toString())
                    putParcelableArrayList("workoutList", workoutList)
                }
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                    addToBackStack(null)
                    commit()
                }
            } else {
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
        }

        save_workout_routine_btn.setOnClickListener {
            if (textViewRoutine.text.isNotEmpty()) {
                if (routine.equals("routine")&&workoutList.isNotEmpty()) {
                    createRoutine(textViewRoutine.text.toString())
                } else if (routine.equals("edit_routine")||editRoutine.equals("edit_routine")) {
                    updateWorkoutRoutine(textViewRoutine.text.toString())
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
            if (newBooleanBack) {
                val bottomSheet = ExitConfirmationBottomSheet {
                    // ✅ On "Yes" click — run your existing logic
                    if (routine.equals("edit_routine") || editRoutine.equals("edit_routine") || myroutine.equals("myRoutine")) {
                        val fragment = SearchWorkoutFragment()
                        val bundle = Bundle().apply { putInt("selectedTab", 1) }
                        fragment.arguments = bundle
                        requireActivity().supportFragmentManager.beginTransaction().apply {
                            replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                            addToBackStack("SearchWorkoutFragment")
                            commit()
                        }
                    } else {
                        navigateToFragment(YourActivityFragment(), "AllWorkoutFragment")
                    }
                }
                bottomSheet.isCancelable = false
                bottomSheet.show(parentFragmentManager, "CustomExitBottomSheet")
            } else {
                // Normal flow if newBooleanBack = false
                if (routine.equals("edit_routine") || editRoutine.equals("edit_routine") || myroutine.equals("myRoutine")) {
                    val fragment = SearchWorkoutFragment()
                    val bundle = Bundle().apply { putInt("selectedTab", 1) }
                    fragment.arguments = bundle
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                        addToBackStack("SearchWorkoutFragment")
                        commit()
                    }
                } else {
                    navigateToFragment(YourActivityFragment(), "AllWorkoutFragment")
                }
            }
        }


        editText.addTextChangedListener(object : TextWatcher {
            private val maxLength = 20
            override fun afterTextChanged(s: Editable?) {
                if (s != null && s.length > maxLength) {
                    s.replace(maxLength, s.length, "")
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
                if (routine.equals("edit_routine")||editRoutine.equals("edit_routine")){
                    val fragment = SearchWorkoutFragment()
                    val bundle = Bundle().apply {
                        putInt("selectedTab", 1) // My Routine tab
                    }
                    fragment.arguments = bundle
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                        addToBackStack("SearchWorkoutFragment")
                        commit()
                    }
                }else{
                    navigateToFragment(YourActivityFragment(), "AllWorkoutFragment")
                }
               // navigateToFragment(YourActivityFragment(), "AllWorkoutFragment")
            }
        })

        layoutBtnLog.setOnClickListener {
            addNameLayout.visibility = View.GONE
            createListRoutineLayout.visibility = View.VISIBLE
            newBooleanBack = true
            textViewRoutine.text = editText.text
        }
    }

    // New function to map PlanExerciseWorkout to WorkoutSessionRecord
    private fun mapPlanExerciseWorkoutToWorkoutSessionRecord(workoutRoutineItem: WorkoutRoutineItem) {
        workoutRoutineItem.workoutList.forEach { planExercise ->
            val workoutRecord = WorkoutSessionRecord(
                userId = workoutRoutineItem.userId,
                activityId = planExercise.activityId,
                moduleIcon = planExercise.icon,
                durationMin = planExercise.durationMin.toInt(),
                intensity = planExercise.intensity,
                sessions = 1,
                moduleName = planExercise.activityName,
                caloriesBurned = planExercise.caloriesBurned,
                message = null,
                activityFactor = null
            )
            workoutList.add(workoutRecord)
            Log.d("CreateRoutineFragment", "Added PlanExerciseWorkout: ${workoutRecord.moduleName} to workoutList")
        }
    }

    // Function to map ActivityModel to WorkoutSessionRecord
    private fun mapActivityModelToWorkoutSessionRecord(activityList: ArrayList<ActivityModel>) {
        activityList.forEach { activity ->
            val regex = Regex("(\\d+) hr (\\d+) mins")
            val matchResult = activity?.duration?.let { regex.find(it) }

            var totalMinutes = 0
            if (matchResult != null) {
                val (hours, minutes) = matchResult.destructured
                totalMinutes = hours.toInt() * 60 + minutes.toInt()
            }
            val caloriesValue = activity.caloriesBurned?.replace(Regex("[^0-9.]"), "")?.toDoubleOrNull()

            val workoutRecord = WorkoutSessionRecord(
                userId = activity.userId!!,
                activityId = activity.activityId!!,
                moduleIcon = activity.icon!!,
                durationMin = totalMinutes,
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
                id = record.activityId,
                name = record.moduleName,
                icon = record.moduleIcon,
                duration = "${record.durationMin} min",
                caloriesBurned = record.caloriesBurned?.toString() ?: "N/A",
                intensity = record.intensity
            )
        }.toCollection(ArrayList())
    }

    private fun mapWorkoutSessionRecordsToRoutine(records: WorkoutRoutineItem?): ArrayList<RoutineWorkoutDisplayModel> {
        return records?.workoutList?.map { record ->
            RoutineWorkoutDisplayModel(
                id = record.activityId,
                name = record.activityName,
                icon = record.icon,
                duration = "${record.durationMin} min",
                caloriesBurned = record.caloriesBurned?.toString() ?: "N/A",
                intensity = record.intensity
            )
        }?.toCollection(ArrayList()) ?: ArrayList()
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
        if (routine.equals("edit_routine")||editRoutine == "edit_routine") {
            val fragment = AddWorkoutSearchFragment()
            val args = Bundle().apply {
                putString("routine", "edit_routine")
                putString("routineName", textViewRoutine.text.toString())
                putString("routineId", routineIdworkout)
                putParcelableArrayList("workoutList", workoutList)
                putString("editworkoutRoutine", "editworkoutRoutine")
                putParcelable("workoutItem", workoutModel)
            }
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                addToBackStack(null)
                commit()
            }
        } else {
            val fragment = AddWorkoutSearchFragment()
            val args = Bundle().apply {
                putString("routine", "routine")
                putString("routineName", textViewRoutine.text.toString())
                putParcelableArrayList("workoutList", workoutList)
                putString("routineId",routineIdworkout)
                putString("editworkoutRoutine", "editworkoutRoutine")
                putParcelable("workoutItem", workoutModel)
            }
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                addToBackStack(null)
                commit()
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
    private fun onListEmptyCallback() {
        no_workout_discription.visibility = View.VISIBLE
        no_workout_heading.visibility = View.VISIBLE
        createRoutineRecyclerView.visibility = View.GONE
    }
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun updateWorkoutRoutine(routineName: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId

                val newRoutineId = routineIdworkout
                val routineId = workoutLists?.routineId ?:routineIdworkout
                ?: throw IllegalStateException("Routine ID is missing for edit")

                // Map workoutList to UpdateRoutineRequest.Workout
                val workouts = workoutList.map { record ->
                    Workout(
                        activityId = record.activityId,
                        intensity = record.intensity,
                        duration = record.durationMin,
                        calories_burned = record.caloriesBurned ?: 0.0
                    )
                }

                val request = UpdateRoutineRequest(
                    user_id = userId,
                    routine_id = routineIdworkout,
                    routine_name = routineName, // Include routine name in the request
                    workouts = workouts
                )

                val response = ApiClient.apiServiceFastApi.updateRoutine(request)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        responseBody?.let {
                            val fragment = SearchWorkoutFragment()
                            val bundle = Bundle().apply {
                                putInt("selectedTab", 1) // My Routine tab
                            }
                            fragment.arguments = bundle
                            requireActivity().supportFragmentManager.beginTransaction().apply {
                                replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                                addToBackStack("SearchWorkoutFragment")
                                commit()
                            }
                            Toast.makeText(requireContext(), "Routine Updated Successfully", Toast.LENGTH_SHORT).show()
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
                Log.e("UpdateRoutine", "Exception in updateWorkoutRoutine: ${e.stackTraceToString()}")
            }
        }
    }
    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun createRoutine(name: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userid = SharedPreferenceManager.getInstance(requireActivity()).userId
                    ?: ""

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