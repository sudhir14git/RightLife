package com.example.rlapp.ai_package.ui.moveright

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.moveright.viewmodel.WorkoutViewModel
import com.example.rlapp.databinding.FragmentSearchWorkoutBinding
import com.google.android.material.tabs.TabLayout


class SearchWorkoutFragment : BaseFragment<FragmentSearchWorkoutBinding>() {

    private val workoutViewModel: WorkoutViewModel by activityViewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSearchWorkoutBinding
        get() = FragmentSearchWorkoutBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.gradient_color_background_workout)

        val tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        val searchEditText: EditText = view.findViewById(R.id.searchEditText)

        val tabTitles = arrayOf("All Workouts", "My Routine", "Frequently Logged")
        for (title in tabTitles) {
            val tab = tabLayout.newTab()
            val customView = LayoutInflater.from(context).inflate(R.layout.custom_search_tab, null) as TextView
            customView.text = title
            tab.customView = customView
            tabLayout.addTab(tab)
        }

        if (savedInstanceState == null) {
            replaceFragment(AllWorkoutFragment())
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToFragment(YourActivityFragment(), "LandingFragment")

            }
        })

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> replaceFragment(AllWorkoutFragment())
                    1 -> replaceFragment(MyRoutineFragment())
                    2 -> replaceFragment(FrequentlyLoggedSearchFragment())
                }
                updateTabColors()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })
        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                workoutViewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().replace(R.id.fragmentContainer, fragment).commit()
    }

    private fun updateTabColors() {
        for (i in 0 until bi.tabLayout.tabCount) {
            val tab = bi.tabLayout.getTabAt(i)
            val tabText = tab?.customView?.findViewById<TextView>(R.id.tabText)
            tabText?.setTextColor(
                if (tab?.isSelected == true) ContextCompat.getColor(requireContext(), R.color.white)
                else ContextCompat.getColor(requireContext(), R.color.dotted_red)
            )
        }
    }
    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }
}