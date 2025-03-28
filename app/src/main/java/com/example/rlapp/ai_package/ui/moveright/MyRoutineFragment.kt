package com.example.rlapp.ai_package.ui.moveright

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.eatright.model.MyMealModel
import com.example.rlapp.databinding.FragmentMyRoutineBinding


class MyRoutineFragment : BaseFragment<FragmentMyRoutineBinding>() {
    private lateinit var myMealRecyclerView : RecyclerView

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyRoutineBinding
        get() = FragmentMyRoutineBinding::inflate
    private val myMealListAdapter by lazy { MyRoutineListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onMealLogDateItem) }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.TRANSPARENT)
        myMealRecyclerView = view.findViewById(R.id.recyclerview_my_meals_item)
        myMealRecyclerView.layoutManager = LinearLayoutManager(context)
        myMealRecyclerView.adapter = myMealListAdapter
        onMyMealItemRefresh()

    }

    private fun onMyMealItemRefresh (){

        val meal = listOf(
            MyMealModel("Monday Routine", "Functional Strength Training | Core Training | Others | Functional…", "min", "337", "Low Intensity", "308", "17", false),
            MyMealModel("Monday Routine", "Functional Strength Training | Core Training | Others | Functional…", "min", "337", "Low Intensity", "308", "17", false)
        )

        if (meal.size > 0){
            myMealRecyclerView.visibility = View.VISIBLE
            //layoutNoMeals.visibility = View.GONE
        }else{
            // layoutNoMeals.visibility = View.VISIBLE
            myMealRecyclerView.visibility = View.GONE
        }

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(meal as Collection<MyMealModel>)
        val mealLogDateData: MyMealModel? = null
        myMealListAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMealLogDateItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {

        val mealLogs = listOf(
            MyMealModel("Monday Routine", "Functional Strength Training | Core Training | Others | Functional…", "min", "337", "Low Intensity", "308", "17", false),
            MyMealModel("Monday Routine", "Functional Strength Training | Core Training | Others | Functional…", "min", "337", "Low Intensity", "308", "17",false)
        )

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        //  mealLogDateAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

}