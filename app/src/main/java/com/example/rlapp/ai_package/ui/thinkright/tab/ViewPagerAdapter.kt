package com.example.rlapp.ai_package.ui.thinkright.tab

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.example.rlapp.ai_package.ui.thinkright.fragment.ThinkRightReportFragment

class ViewPagerAdapter(activity: ThinkRightReportFragment) : FragmentStateAdapter(activity) {
    override fun getItemCount(): Int = 3 // Number of pages

    override fun createFragment(position: Int): Fragment {
        return PageFragment.newInstance(position + 1)
    }
}