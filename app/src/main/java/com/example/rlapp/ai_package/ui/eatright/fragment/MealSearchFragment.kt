package com.example.rlapp.ai_package.ui.eatright.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.eatright.adapter.MealSearchAdapter
import com.example.rlapp.databinding.FragmentMealSearchBinding

class MealSearchFragment : BaseFragment<FragmentMealSearchBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMealSearchBinding
            get() = FragmentMealSearchBinding::inflate

    private val mealSearchAdapter by lazy { MealSearchAdapter(requireContext(), 10) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
            super.onViewCreated(view, savedInstanceState)
        val mealSearchList = view.findViewById<RecyclerView>(R.id.rec_all_dishes)
        mealSearchList.layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
        mealSearchList.adapter = mealSearchAdapter
        }

}