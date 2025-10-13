package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.annotations.SerializedName
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.AddWorkoutResponse
import com.jetsynthesys.rightlife.ai_package.model.PlanExerciseWorkout
import com.jetsynthesys.rightlife.ai_package.model.WorkoutRoutineItem
import com.jetsynthesys.rightlife.ai_package.model.request.AddWorkoutLogRequest
import com.jetsynthesys.rightlife.ai_package.model.response.PlanExercise
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter.MyRoutineMainListAdapter
import com.jetsynthesys.rightlife.databinding.FragmentMyRoutineBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.format.DateTimeFormatter

class MyRoutineFragment(mSelectedDate: String) : BaseFragment<FragmentMyRoutineBinding>() {

    private val selectedDate = mSelectedDate

    private lateinit var myRoutineRecyclerView: RecyclerView
    private lateinit var layoutBtnLogWorkout: LinearLayoutCompat
    private lateinit var dataFilledMyRoutine : ConstraintLayout
    private lateinit var noDataMyRoutine : ConstraintLayout
    private lateinit var btnCreateRoutine : LinearLayoutCompat

    private val myRoutineListAdapter by lazy {
        MyRoutineMainListAdapter(
            requireContext(), arrayListOf(), -1, null, false,
            onCirclePlusClick = { workoutRoutineModel, position ->
                val fragment = CreateRoutineFragment()
                val args = Bundle().apply {
                    putParcelable("WORKOUT_MODEL", workoutRoutineModel)
                    putString("edit_routine","edit_routine")
                }
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "workoutDetails")
                    addToBackStack("workoutDetails")
                    commit() } },::onWorkoutAddToLog, ::onWorkoutItemClick)
    }

    fun getTodayDate(): LocalDate {
        return LocalDate.now()
    }

    private fun onWorkoutAddToLog(workoutItem:WorkoutRoutineItem) {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val date = getTodayDate().format(dateFormatter)
                Log.d("FetchRoutines", "Fetching routines for userId: $userId")
                val call = ApiClient.apiServiceFastApi.addWorkoutLog(AddWorkoutLogRequest(userId,selectedDate,workoutItem.routineId))
                call.enqueue(object : Callback<AddWorkoutResponse> {
                    override fun onResponse(call: Call<AddWorkoutResponse>, response: Response<AddWorkoutResponse>) {
                        if (response.isSuccessful) {
                            if (response.body() != null) {
                                Toast.makeText(activity, "Workout Added Successfully.", Toast.LENGTH_SHORT).show()
                                navigateToFragment(YourActivityFragment(), "AllWorkoutFragment")
                              //  val bottomSheet = LoggedBottomSheet()
                              //     bottomSheet.show((context as AppCompatActivity).supportFragmentManager, "EditWorkoutBottomSheet")
                            }
                        }
                    }

                    override fun onFailure(call: Call<AddWorkoutResponse>, t: Throwable) {
                        Log.e("Error", "API call failed: ${t.message}")
                        Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                    }
                })
    }


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyRoutineBinding
        get() = FragmentMyRoutineBinding::inflate

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.TRANSPARENT)

        layoutBtnLogWorkout = view.findViewById(R.id.layoutCreateWorkRoutine)
        myRoutineRecyclerView = view.findViewById(R.id.myRoutineRecyclerView)
        dataFilledMyRoutine = view.findViewById(R.id.dataFilledMyRoutine)
        noDataMyRoutine = view.findViewById(R.id.noDataMyRoutine)
       btnCreateRoutine = view.findViewById(R.id.layoutBtnCreateRoutine)

        myRoutineRecyclerView.layoutManager = LinearLayoutManager(context)
        myRoutineRecyclerView.adapter = myRoutineListAdapter

        layoutBtnLogWorkout.setOnClickListener {
            openAddWorkoutFragment()
        }

        btnCreateRoutine.setOnClickListener {
            openAddWorkoutFragment()
        }

        fetchRoutines()
    }

    private fun openAddWorkoutFragment() {
        val fragment = CreateRoutineFragment()
        val args = Bundle().apply {
            putString("myRoutine", "myRoutine")

        }
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment, "addWorkoutFragment")
            .addToBackStack("addWorkoutFragment")
            .commit()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun fetchRoutines() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                Log.d("FetchRoutines", "Fetching routines for userId: $userId")
                val response = ApiClient.apiServiceFastApi.getRoutines(userId)
                if (response.isSuccessful) {
                    val routineResponse = response.body()
                    Log.d("FetchRoutines", "Received ${routineResponse?.routines?.size ?: 0} routines")
                    val workoutList = ArrayList<WorkoutRoutineItem>()
                    routineResponse?.routines?.forEach { plan ->
                        if (plan.workouts.isNotEmpty()) {
                            // Merge workouts
                            val mergedActivityName = plan.workouts.joinToString(" | ") { it.activityName }
                            val totalDurationMin = plan.workouts.sumOf { it.durationMin }.toInt()
                            val totalCaloriesBurned = plan.workouts.sumOf { it.caloriesBurned }
                            val firstIntensity = plan.workouts.first().intensity
                            val firstActivityId = plan.workouts.first().activityId

                            val workoutItem = WorkoutRoutineItem(
                                routineId = plan.routineId,
                                routineName = plan.routineName,
                                activityName = mergedActivityName,
                                duration = "$totalDurationMin min",
                                caloriesBurned = String.format("%.1f", totalCaloriesBurned),
                                intensity = firstIntensity,
                                workoutList = getWorkoutList(plan.workouts),
                                activityId = firstActivityId, // Using first workout's activityId
                                userId = userId
                            )
                            workoutList.add(workoutItem)
                            Log.d("FetchRoutines", "Merged routine ${plan.routineName}: activities=$mergedActivityName, duration=$totalDurationMin min, calories=${totalCaloriesBurned}, intensity=$firstIntensity")
                        } else {
                            Log.d("FetchRoutines", "Routine ${plan.routineName} has no workouts, skipping")
                        }
                    }
                    withContext(Dispatchers.Main) {
                        if (isAdded && view != null) {
                            if (workoutList.isNotEmpty()) {
                                dataFilledMyRoutine.visibility = View.VISIBLE
                               // myRoutineRecyclerView.visibility = View.VISIBLE
                                layoutBtnLogWorkout.visibility = View.VISIBLE
                                noDataMyRoutine.visibility = View.GONE
                                myRoutineListAdapter.addAll(workoutList, -1, null, false)
                                Log.d("FetchRoutines", "Displaying ${workoutList.size} merged routines")
                            } else {
                                noDataMyRoutine.visibility = View.VISIBLE
                                dataFilledMyRoutine.visibility = View.GONE
                              //  myRoutineRecyclerView.visibility = View.GONE
                                layoutBtnLogWorkout.visibility = View.GONE
                                Log.d("FetchRoutines", "No routines to display")
                            }
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    withContext(Dispatchers.Main) {
                        if (isAdded && view != null) {
                            Log.e("FetchRoutines", "API Error ${response.code()}: $errorBody")
                            Toast.makeText(context, "Failed to fetch routines: ${response.code()}", Toast.LENGTH_LONG).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && view != null) {
                        Log.e("FetchRoutines", "Exception: ${e.message}", e)
                        Toast.makeText(context, "Error fetching routines: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                }
            }
        }
    }
    private fun getWorkoutList(workouts: List<PlanExercise>):List<PlanExerciseWorkout>{
        val modelBList = mutableListOf<PlanExerciseWorkout>()

        for (item in workouts) {
            val newItem = PlanExerciseWorkout(
                activityId = item.activityId,
                activityName = item.activityName,
                intensity = item.intensity,
                durationMin = item.durationMin,
                icon = item.icon,
                caloriesBurned = item.caloriesBurned
            )
            modelBList.add(newItem)
        }
        return modelBList
    }

    private fun onWorkoutItemClick(workout: WorkoutRoutineItem, position: Int, isRefresh: Boolean) {
        Log.d("MyRoutineFragment", "Workout clicked: ${workout.activityName}, Position: $position")
        // Add click handling logic here if needed
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        val args = Bundle()
        args.putString("selected_date", selectedDate) // Put the string in the bundle
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }
}