package com.example.rlapp.ai_package.ui.eatright.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.databinding.FragmentSnapMealDescriptionBinding
import com.google.android.material.snackbar.Snackbar

class SnapMealDescFragment : BaseFragment<FragmentSnapMealDescriptionBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSnapMealDescriptionBinding
        get() = FragmentSnapMealDescriptionBinding::inflate
    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnProceed = view.findViewById<TextView>(R.id.btn_proceed)

        btnProceed.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealScanResultFragment = EatRightLandingFragment()
                val args = Bundle()
                mealScanResultFragment.arguments = args
                replace(R.id.flFragment, mealScanResultFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }


    }
}