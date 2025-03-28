package com.example.rlapp.ai_package.ui.moveright

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.databinding.FragmentAddWorkoutBinding


class AddWorkoutFragment : BaseFragment<FragmentAddWorkoutBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAddWorkoutBinding
        get() = FragmentAddWorkoutBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(Color.TRANSPARENT)


    }


}