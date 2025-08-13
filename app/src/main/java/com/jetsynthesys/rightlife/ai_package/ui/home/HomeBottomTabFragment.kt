package com.jetsynthesys.rightlife.ai_package.ui.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.EatRightLandingFragment
import com.jetsynthesys.rightlife.ai_package.ui.moveright.MoveRightLandingFragment
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.SleepRightLandingFragment
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.fragment.ThinkRightReportFragment
import com.jetsynthesys.rightlife.databinding.HomeBottomTabFragmentAiBinding
import com.jetsynthesys.rightlife.ui.ActivityUtils
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class
HomeBottomTabFragment : BaseFragment<HomeBottomTabFragmentAiBinding>() {

    private lateinit var layoutButton: LinearLayout
    private lateinit var tabThink: LinearLayout
    private lateinit var tabMove: LinearLayout
    private lateinit var tabEat: LinearLayout
    private lateinit var tabSleep: LinearLayout
    private lateinit var icThink: ImageView
    private lateinit var icMove: ImageView
    private lateinit var icEat: ImageView
    private lateinit var icSleep: ImageView
    private lateinit var tvThink: TextView
    private lateinit var tvMove: TextView
    private lateinit var tvEat: TextView
    private lateinit var tvSleep: TextView
    var homeBottomArgument: String = ""

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> HomeBottomTabFragmentAiBinding
        get() = HomeBottomTabFragmentAiBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        tabThink = view.findViewById(R.id.tabThink)
        tabMove = view.findViewById(R.id.tabMove)
        tabEat = view.findViewById(R.id.tabEat)
        tabSleep = view.findViewById(R.id.tabSleep)
        icThink = view.findViewById(R.id.icThink)
        icMove = view.findViewById(R.id.icMove)
        icEat = view.findViewById(R.id.icEat)
        icSleep = view.findViewById(R.id.icSleep)
        tvThink = view.findViewById(R.id.tvThink)
        tvMove = view.findViewById(R.id.tvMove)
        tvEat = view.findViewById(R.id.tvEat)
        tvSleep = view.findViewById(R.id.tvSleep)


        homeBottomArgument = arguments?.getString("HomeBottomTabFragment").toString()
        val moduleName = arguments?.getString("ModuleName").toString()
        val bottomSeatName = arguments?.getString("BottomSeatName").toString()

        tabThink.setOnClickListener {
            switchFragment(
                ThinkRightReportFragment(),
                tabThink,
                "Think",
                bottomSeatName
            )
        }
        tabMove.setOnClickListener {
            switchFragment(
                MoveRightLandingFragment(),
                tabMove,
                "Move",
                bottomSeatName
            )
        }
        tabEat.setOnClickListener {
            switchFragment(
                EatRightLandingFragment(),
                tabEat,
                "Eat",
                bottomSeatName
            )
        }
        tabSleep.setOnClickListener {
            switchFragment(
                SleepRightLandingFragment(),
                tabSleep,
                "Sleep",
                bottomSeatName
            )
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    requireActivity().finish()
                }
            })


        if (moduleName.contentEquals("MoveRight")) {
            switchFragment(MoveRightLandingFragment(), tabMove, "Move", bottomSeatName)
        } else if (moduleName.contentEquals("EatRight")) {
            switchFragment(EatRightLandingFragment(), tabEat, "Eat", bottomSeatName)
        } else if (moduleName.contentEquals("SleepRight")) {
            switchFragment(SleepRightLandingFragment(), tabSleep, "Sleep", bottomSeatName)
        } else if (moduleName.contentEquals("ThinkRight")) {
            switchFragment(ThinkRightReportFragment(), tabThink, "Think", bottomSeatName)
        }
    }

    private fun switchFragment(
        fragment: Fragment,
        selectedTab: LinearLayout,
        selectedTabName: String,
        bottomSeatName: String
    ) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            val args = Bundle()
            args.putString("BottomSeatName", bottomSeatName)
            fragment.arguments = args
            replace(R.id.fragment_container, fragment, selectedTabName)
            addToBackStack(null)
            commit()
        }
        // Reset all tabs to unselected background
        tabThink.setBackgroundResource(R.drawable.tab_unselected_bg)
        tabMove.setBackgroundResource(R.drawable.tab_unselected_bg)
        tabEat.setBackgroundResource(R.drawable.tab_unselected_bg)
        tabSleep.setBackgroundResource(R.drawable.tab_unselected_bg)

        // Highlight selected tab
        if (selectedTabName.contentEquals("Think")) {
            ActivityUtils.startThinkRightReportsActivity(requireContext(), bottomSeatName, true)
            selectedTab.setBackgroundResource(R.drawable.think_tab_selected_bg)
            tvThink.visibility = View.VISIBLE
            tvMove.visibility = View.GONE
            tvEat.visibility = View.GONE
            tvSleep.visibility = View.GONE
            icThink.setImageResource(R.drawable.ic_think)
            icMove.setImageResource(R.drawable.ic_move)
            icEat.setImageResource(R.drawable.ic_eat)
            icSleep.setImageResource(R.drawable.ic_sleep)
        } else if (selectedTabName.contentEquals("Move")) {
            ActivityUtils.startMoveRightReportsActivity(requireContext(), bottomSeatName, true)
            selectedTab.setBackgroundResource(R.drawable.move_tab_selected_bg)
            tvThink.visibility = View.GONE
            tvMove.visibility = View.VISIBLE
            tvEat.visibility = View.GONE
            tvSleep.visibility = View.GONE
            icThink.setImageResource(R.drawable.ic_think)
            icMove.setImageResource(R.drawable.ic_move_white)
            icEat.setImageResource(R.drawable.ic_eat)
            icSleep.setImageResource(R.drawable.ic_sleep)
        } else if (selectedTabName.contentEquals("Eat")) {
            ActivityUtils.startEatRightReportsActivity(requireContext(), bottomSeatName, isFromAIDashBoard = true)
            selectedTab.setBackgroundResource(R.drawable.eat_tab_selected_bg)
            tvThink.visibility = View.GONE
            tvMove.visibility = View.GONE
            tvEat.visibility = View.VISIBLE
            tvSleep.visibility = View.GONE
            icThink.setImageResource(R.drawable.ic_think)
            icMove.setImageResource(R.drawable.ic_move)
            icEat.setImageResource(R.drawable.ic_eat_white)
            icSleep.setImageResource(R.drawable.ic_sleep)
        } else {
            ActivityUtils.startSleepRightReportsActivity(requireContext(), bottomSeatName, true)
            selectedTab.setBackgroundResource(R.drawable.sleep_tab_selected_bg)
            tvThink.visibility = View.GONE
            tvMove.visibility = View.GONE
            tvEat.visibility = View.GONE
            tvSleep.visibility = View.VISIBLE
            icThink.setImageResource(R.drawable.ic_think)
            icMove.setImageResource(R.drawable.ic_move)
            icEat.setImageResource(R.drawable.ic_eat)
            icSleep.setImageResource(R.drawable.ic_sleep_white)
        }
    }
}

