package com.example.rlapp.ai_package.ui.steps


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.eatright.fragment.SnapMealFragment
import com.example.rlapp.ai_package.ui.moveright.MoveRightLandingFragment
import com.example.rlapp.databinding.FragmentSetYourStepGoalBinding
import com.google.android.material.snackbar.Snackbar


class SetYourStepGoalFragment : BaseFragment<FragmentSetYourStepGoalBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSetYourStepGoalBinding
        get() = FragmentSetYourStepGoalBinding::inflate
    var snackbar: Snackbar? = null

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val setTargetButton = view.findViewById<TextView>(R.id.tv_set_target)

        /*setTargetButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val stepGoalFragment = StepGoalFragment()
                val args = Bundle()
                stepGoalFragment.arguments = args
                replace(R.id.flFragment, stepGoalFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }*/

        setTargetButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val stepGoalFragment = SnapMealFragment()
                val args = Bundle()
                stepGoalFragment.arguments = args
                replace(R.id.flFragment, stepGoalFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = MoveRightLandingFragment()
                val args = Bundle()

                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        })
    }
}