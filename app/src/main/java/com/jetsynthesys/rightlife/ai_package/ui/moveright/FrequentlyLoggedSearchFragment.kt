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
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MyMealModel
import com.jetsynthesys.rightlife.databinding.FragmentFrequentlyLoggedSearchBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class FrequentlyLoggedSearchFragment : BaseFragment<FragmentFrequentlyLoggedSearchBinding>() {
    private lateinit var myMealRecyclerView: RecyclerView

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFrequentlyLoggedSearchBinding
        get() = FragmentFrequentlyLoggedSearchBinding::inflate
    private val myMealListAdapter by lazy { FrequentltLoggedSearchAdapter(requireContext(), arrayListOf(), -1, null, false, ::onMealLogDateItem) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.TRANSPARENT)
        myMealRecyclerView = view.findViewById(R.id.recyclerview_my_meals_item)
        myMealRecyclerView.layoutManager = LinearLayoutManager(context)
        myMealRecyclerView.adapter = myMealListAdapter
        onMyMealItemRefresh()
        fetchUserWorkouts()
    }

    private fun onMyMealItemRefresh() {
        val meal = listOf(
            MyMealModel("Others", "Functional Strength Training | Core Training | Others | Functional…", "min", "337", "Low Intensity", "308", "17", false),
            MyMealModel("Functional Strength Training", "Functional Strength Training | Core Training | Others | Functional…", "min", "337", "Low Intensity", "308", "17", false)
        )

        if (meal.isNotEmpty()) {
            myMealRecyclerView.visibility = View.VISIBLE
            //layoutNoMeals.visibility = View.GONE
        } else {
            // layoutNoMeals.visibility = View.VISIBLE
            myMealRecyclerView.visibility = View.GONE
        }

        val valueLists: ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(meal as Collection<MyMealModel>)
        val mealLogDateData: MyMealModel? = null
        myMealListAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMealLogDateItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            MyMealModel("Others", "Functional Strength Training | Core Training | Others | Functional…", "min", "337", "Low Intensity", "308", "17", false),
            MyMealModel("Functional Strength Training", "Functional Strength Training | Core Training | Others | Functional…", "min", "337", "Low Intensity", "308", "17", false)
        )

        val valueLists: ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        // mealLogDateAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun fetchUserWorkouts() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiServiceFastApi.getUserFrequentlyWorkouts(
                    userId = "64763fe2fa0e40d9c0bc8264",
                    startDate = "2025-03-17",
                    endDate = "2025-03-25",
                    page = 1,
                    limit = 10,
                    minCount = 2
                )

                if (response.isSuccessful) {
                    val workouts = response.body()
                    if (workouts != null) {
                        
                        val mealList = workouts.frequentWorkouts.map { workoutItem ->
                            MyMealModel(
                                mealName = workoutItem.workoutType,
                                mealType = "Functional Strength Training | Core Training | Others | Functional…", serve =  "min", cal = "337", subtraction = "Low Intensity", baguette = "308", dewpoint = "17", isAddDish =  false
                            )
                        }

                        val valueLists: ArrayList<MyMealModel> = ArrayList(mealList)
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