package com.example.rlapp.ai_package.ui.eatright.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.databinding.FragmentSnapMealBinding
import com.google.android.material.snackbar.Snackbar

class SnapMealFragment : BaseFragment<FragmentSnapMealBinding>() {
    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSnapMealBinding
        get() = FragmentSnapMealBinding::inflate
    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val btnProceed = view.findViewById<LinearLayout>(R.id.btn_proceed)

        btnProceed.setOnClickListener {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    val snapMealFragment = SnapMealDescFragment()
                    val args = Bundle()
                    snapMealFragment.arguments = args
                    replace(R.id.flFragment, snapMealFragment, "Steps")
                    addToBackStack(null)
                    commit()
                }
        }

    }
}