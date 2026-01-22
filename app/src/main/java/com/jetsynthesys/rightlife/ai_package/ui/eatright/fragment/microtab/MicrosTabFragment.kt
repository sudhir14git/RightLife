package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.microtab

import android.os.Build
import android.os.Bundle
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
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.google.android.material.tabs.TabLayout
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.databinding.FragmentMicosTabBinding

class MicrosTabFragment : BaseFragment<FragmentMicosTabBinding>() {

    private lateinit var tabLayout: TabLayout
    private lateinit var backIc: ImageView
    private val fragmentMap = mutableMapOf<String, Fragment>() // Cache fragments by tag
    private var currentFragmentTag: String? = null // Track current fragment
    private val tabTitles = arrayOf("Sugar", "Iron", "Magnesium", "Calcium", "Vitamin A", "Vitamin C", "Vitamin D", "Vitamin K", "Zinc", "Sodium")

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMicosTabBinding
        get() = FragmentMicosTabBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))
        tabLayout = view.findViewById(R.id.tabMicroLayout)
        backIc = view.findViewById(R.id.backIc)

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
            for (title in tabTitles) {
                childFragmentManager.findFragmentByTag(title)?.let {
                    fragmentMap[title] = it
                }
            }
        }

        if (currentFragmentTag == null) {
            currentFragmentTag = "Cholesterol"
            showFragment("Cholesterol")
            tabLayout.getTabAt(0)?.select()
        } else {
            showFragment(currentFragmentTag!!)
            tabLayout.getTabAt(tabTitles.indexOf(currentFragmentTag))?.select()
        }

        updateTabColors()

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

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
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
        })

        backIc.setOnClickListener {
            navigateToHome()
        }
    }

    private fun navigateToHome() {
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

    private fun showFragment(tag: String) {
        val transaction = childFragmentManager.beginTransaction()

        for (fragment in fragmentMap.values) {
            transaction.hide(fragment)
        }

        var fragment = fragmentMap[tag]
        if (fragment == null) {
            fragment = when (tag) {
               // "Cholesterol" -> CholesterolFragment()
                "Sugar" -> SugarFragment()
                "Iron" -> IronFragment()
                "Magnesium" -> MagnesiumFragment()
                "Calcium" -> CalciumFragment()
                "Vitamin A" -> VitaminAFragment()
                "Vitamin C" -> VitaminCFragment()
                "Vitamin D" -> VitaminDFragment()
                "Vitamin K" -> VitaminKFragment()
                "Zinc" -> ZincFragment()
                "Sodium" -> SodiumFragment()
                else -> SugarFragment()
            }
            fragmentMap[tag] = fragment
            transaction.add(R.id.fragmentMicrosContainer, fragment, tag)
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

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentFragmentTag", currentFragmentTag)
    }
}
