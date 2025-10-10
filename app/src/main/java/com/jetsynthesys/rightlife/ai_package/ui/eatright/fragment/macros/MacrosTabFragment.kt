package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.macros

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.ThinkRecomendedResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.adapter.RecommendedAdapterSleep
import com.jetsynthesys.rightlife.databinding.FragmentMacosTabBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MacrosTabFragment : BaseFragment<FragmentMacosTabBinding>() {

    private lateinit var tabLayout: TabLayout
    private lateinit var backIc: ImageView
    private val fragmentMap = mutableMapOf<String, Fragment>() // Cache fragments by tag
    private var currentFragmentTag: String? = null // Track the current fragment tag
    private val tabTitles = arrayOf("Calorie", "Protein", "Carbs", "Fats")

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMacosTabBinding
        get() = FragmentMacosTabBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))
        tabLayout = view.findViewById(R.id.tabMacroLayout)
        backIc = view.findViewById(R.id.backIc)

        // Add tabs with custom views
        for (title in tabTitles) {
            val tab = tabLayout.newTab()
            val customView = LayoutInflater.from(context).inflate(R.layout.custom_tab, null) as TextView
            customView.text = title
            tab.customView = customView
            tabLayout.addTab(tab)
        }

        // Restore or set default fragment
        if (savedInstanceState != null) {
            currentFragmentTag = savedInstanceState.getString("currentFragmentTag")
            // Restore fragments from childFragmentManager
            for (title in tabTitles) {
                val fragment = childFragmentManager.findFragmentByTag(title)
                if (fragment != null) {
                    fragmentMap[title] = fragment
                }
            }
        }

        // If no saved state or no current fragment, set default to Calorie
        if (currentFragmentTag == null) {
            currentFragmentTag = "Calorie"
            showFragment("Calorie")
            tabLayout.getTabAt(0)?.select()
        } else {
            showFragment(currentFragmentTag!!)
            tabLayout.getTabAt(tabTitles.indexOf(currentFragmentTag))?.select()
        }

        updateTabColors()

        // Handle tab clicks manually
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    val tag = tabTitles[position]
                    currentFragmentTag = tag
                    showFragment(tag)
                    updateTabColors()
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        backIc.setOnClickListener {
            val fragment = HomeBottomTabFragment()
            val args = Bundle()
            args.putString("ModuleName", "EatRight")
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }
    }

    private fun showFragment(tag: String) {
        val transaction = childFragmentManager.beginTransaction()
        // Hide all existing fragments
        for (fragment in fragmentMap.values) {
            transaction.hide(fragment)
        }
        // Show or create the fragment
        var fragment = fragmentMap[tag]
        if (fragment == null) {
            fragment = when (tag) {
                "Calorie" -> CalorieFragment()
                "Protein" -> ProteinFragment()
                "Carbs" -> CarbsFragment()
                "Fats" -> FatsFragment()
                else -> CalorieFragment()
            }
            fragmentMap[tag] = fragment
            transaction.add(R.id.fragmentMacrosContainer, fragment, tag)
        } else {
            transaction.show(fragment)
        }
        transaction.commit()
    }

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

    fun handleBackPressed() {
        val fragment = HomeBottomTabFragment()
        val args = Bundle()
        args.putString("ModuleName", "EatRight")
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment, "landing")
            .addToBackStack("landing")
            .commit()
    }


    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentFragmentTag", currentFragmentTag)
    }
}