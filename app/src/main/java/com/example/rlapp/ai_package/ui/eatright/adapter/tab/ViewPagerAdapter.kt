package com.example.rlapp.ai_package.ui.eatright.adapter.tab

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter


class ViewPagerAdapter(fragmentActivity: FragmentActivity) : FragmentStateAdapter(fragmentActivity) {

    private val mFragmentList: MutableList<Fragment> = ArrayList()
    private val mFragmentTitleList: MutableList<String> = ArrayList()

    fun addFragment(fragment: Fragment, title: String) {
        mFragmentList.add(fragment)
        mFragmentTitleList.add(title)
    }

    fun getTabTitle(position: Int): String {
        return mFragmentTitleList[position]
    }

    override fun getItemCount(): Int {
        return mFragmentList.size
    }

    override fun createFragment(position: Int): Fragment {
        return mFragmentList[position]
    }

//    override fun getItemCount(): Int = 4 // Three tabs
//
//    override fun createFragment(position: Int): Fragment {
//        return when (position) {
//            0 -> MyMealFragment()
//            1 -> FrequentlyLoggedFragment()
//            2 -> MealPlanFragment()
//            3 -> MyRecipeFragment()
//            else -> MyMealFragment()
//        }
//    }
}


