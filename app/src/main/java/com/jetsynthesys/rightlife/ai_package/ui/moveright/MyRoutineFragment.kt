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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.WorkoutRoutineItem
import com.jetsynthesys.rightlife.ai_package.model.response.WorkoutPlanResponse
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.adapter.MyRoutineMainListAdapter
import com.jetsynthesys.rightlife.databinding.FragmentMyRoutineBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MyRoutineFragment : BaseFragment<FragmentMyRoutineBinding>() {

    private lateinit var myRoutineRecyclerView: RecyclerView
    private lateinit var layoutBtnLogWorkout: LinearLayoutCompat

    private val myRoutineListAdapter by lazy {
        MyRoutineMainListAdapter(
            requireContext(), arrayListOf(), -1, null, false,
            onCirclePlusClick = { workoutRoutineModel, position ->
                val fragment = AddWorkoutSearchFragment()
                val args = Bundle().apply {
                    putParcelable("WORKOUT_MODEL", workoutRoutineModel)
                    putString("edit_routine","edit_routine")
                }
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "workoutDetails")
                    addToBackStack("workoutDetails")
                    commit() } }, ::onWorkoutItemClick,)
    }

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyRoutineBinding
        get() = FragmentMyRoutineBinding::inflate

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.TRANSPARENT)

        layoutBtnLogWorkout = view.findViewById(R.id.layout_btn_log_meal)
        myRoutineRecyclerView = view.findViewById(R.id.recyclerview_my_meals_item)
        myRoutineRecyclerView.layoutManager = LinearLayoutManager(context)
        myRoutineRecyclerView.adapter = myRoutineListAdapter


        layoutBtnLogWorkout.setOnClickListener {
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
                    ?: "64763fe2fa0e40d9c0bc8264"
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
                                myRoutineRecyclerView.visibility = View.VISIBLE
                                myRoutineListAdapter.addAll(workoutList, -1, null, false)
                                Log.d("FetchRoutines", "Displaying ${workoutList.size} merged routines")
                            } else {
                                myRoutineRecyclerView.visibility = View.GONE
                                Log.d("FetchRoutines", "No routines to display")
                            }
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "No error details"
                    withContext(Dispatchers.Main) {
                        if (isAdded && view != null) {
                            Log.e("FetchRoutines", "API Error ${response.code()}: $errorBody")
                            Toast.makeText(
                                context,
                                "Failed to fetch routines: ${response.code()}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    if (isAdded && view != null) {
                        Log.e("FetchRoutines", "Exception: ${e.message}", e)
                        Toast.makeText(
                            context,
                            "Error fetching routines: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        }
    }

    private fun onWorkoutItemClick(workout: WorkoutRoutineItem, position: Int, isRefresh: Boolean) {
        Log.d("MyRoutineFragment", "Workout clicked: ${workout.activityName}, Position: $position")
        // Add click handling logic here if needed
    }
}