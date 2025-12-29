package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.FrequentLoggedRoutine
import com.jetsynthesys.rightlife.databinding.FragmentFrequentlyLoggedSearchBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class FrequentlyLoggedSearchFragment : BaseFragment<FragmentFrequentlyLoggedSearchBinding>() {
    private lateinit var myMealRecyclerView: RecyclerView

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFrequentlyLoggedSearchBinding
        get() = FragmentFrequentlyLoggedSearchBinding::inflate

    private val myMealListAdapter by lazy {
        FrequentltLoggedSearchAdapter(requireContext(), arrayListOf(), -1, null, false, ::onMealLogDateItem)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.TRANSPARENT)
        myMealRecyclerView = view.findViewById(R.id.recyclerview_my_meals_item)
        myMealRecyclerView.layoutManager = LinearLayoutManager(context)
        myMealRecyclerView.adapter = myMealListAdapter
        fetchUserWorkouts() // Fetch and display only API data
    }

    private fun onMealLogDateItem(mealLogDateModel: FrequentLoggedRoutine, position: Int, isRefresh: Boolean) {
        // This method is currently unused but required by the adapter's callback
        // You can implement it if needed for future functionality
    }

    private fun fetchUserWorkouts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                val formattedDate = currentDateTime.format(formatter)
                val startDate = currentDateTime.minusDays(30)
                val formattedStartDate = startDate.format(formatter)
                val response = ApiClient.apiServiceFastApi.getUserFrequentlyWorkouts(
                    userId = SharedPreferenceManager.getInstance(requireActivity()).userId,
                    startDate = formattedStartDate,
                    endDate = formattedDate,
                    page = 1,
                    limit = 10,
                    minCount = 5
                )

                if (response.isSuccessful) {
                    val workouts = response.body()
                    if (workouts != null) {
                        val mealList = workouts.unsyncedWorkouts.map { workoutItem ->
                            FrequentLoggedRoutine(
                                mealName = workoutItem.workoutType,
                                mealType = workoutItem.recordType,
                                serve = "min",
                                cal = workoutItem.caloriesBurned.toString(),
                                subtraction = workoutItem.intensity,
                                icon = workoutItem.icon,
                                baguette = workoutItem.duration.toString(), // These hardcoded values may need to be updated based on API data
                                dewpoint = "17",  // These hardcoded values may need to be updated based on API data
                                isAddDish = false
                            )
                        }

                        val valueLists: ArrayList<FrequentLoggedRoutine> = ArrayList(mealList)
                        withContext(Dispatchers.Main) {
                            if (valueLists.isNotEmpty()) {
                                myMealRecyclerView.visibility = View.VISIBLE
                                //layoutNoMeals.visibility = View.GONE
                            } else {
                                //layoutNoMeals.visibility = View.VISIBLE
                                myMealRecyclerView.visibility = View.GONE
                            }
                            myMealListAdapter.addAll(valueLists, -1, null, false)
                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            println("Response body is null")
                        }
                    }
                } else {
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