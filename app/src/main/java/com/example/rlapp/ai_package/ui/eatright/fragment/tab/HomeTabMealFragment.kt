package com.example.rlapp.ai_package.ui.eatright.fragment.tab

import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.example.rlapp.databinding.FragmentHomeTabMealBinding
import com.google.android.material.tabs.TabLayout


class HomeTabMealFragment : BaseFragment<FragmentHomeTabMealBinding>() {

    private lateinit var tabLayout : TabLayout

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeTabMealBinding
        get() = FragmentHomeTabMealBinding::inflate

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))
        tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)

        val tabTitles = arrayOf("Frequently Logged", "My Meal", "Meal Plan", "My Recipe")

        for (title in tabTitles) {
            val tab = tabLayout.newTab()
            val customView =
                LayoutInflater.from(context).inflate(R.layout.custom_tab, null) as TextView
            customView.text = title
            tab.customView = customView
            tabLayout.addTab(tab)
        }

        // Set default fragment
        if (savedInstanceState == null) {
            replaceFragment(FrequentlyLoggedFragment())
            updateTabColors()
        }

        // Handle tab clicks manually
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> replaceFragment(FrequentlyLoggedFragment())
                    1 -> replaceFragment(MyMealFragment())
                    2 -> replaceFragment(MealPlanFragment())
                    3 -> replaceFragment(MyRecipeFragment())
                }
                updateTabColors()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val fragment = YourMealLogsFragment()
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

    // Function to replace fragments
    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }

    // Function to update tab selection colors
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTabColors() {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val customView = tab?.customView
            val tabText = customView?.findViewById<TextView>(R.id.tabText)

            if (tab?.isSelected == true) {
                tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                val typeface = resources.getFont(R.font.dmsans_bold)
                tabText?.typeface = typeface
            } else {
                val typeface = resources.getFont(R.font.dmsans_regular)
                tabText?.typeface = typeface
                tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_unselected_text))
            }
        }
    }
}