package com.jetsynthesys.rightlife.ai_package.ui

import android.os.Bundle
import android.widget.Toast
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseActivity
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.SnapMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.ViewSnapMealInsightsFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.moveright.SearchWorkoutFragment
import com.jetsynthesys.rightlife.databinding.ActivityMainAiBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainAIActivity : BaseActivity() {

    lateinit var bi: ActivityMainAiBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        bi = ActivityMainAiBinding.inflate(layoutInflater)
        setContentView(bi.root)

        val moduleName = intent.getStringExtra("ModuleName")
        val bottomSeatName = intent.getStringExtra("BottomSeatName")
        val snapMealId = intent.getStringExtra("snapMealId")?: ""

        if (bottomSeatName.contentEquals("SnapMealTypeEat")){
            if (snapMealId != "" && snapMealId != "null"){
                val fragment = ViewSnapMealInsightsFragment()
                val args = Bundle()
                args.putString("snapMealId", snapMealId)
                fragment.arguments = args
                supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "viewMeal")
                    addToBackStack("viewMeal")
                    commit()
                }
            }else{
                supportFragmentManager.beginTransaction().apply {
                    val mealSearchFragment = SnapMealFragment()
                    val args = Bundle()
                    args.putString("ModuleName", "HomeDashboard")
                    mealSearchFragment.arguments = args
                    replace(R.id.flFragment, mealSearchFragment, "Steps")
                    addToBackStack(null)
                    commit()
                }
            }
        }else  if (bottomSeatName.contentEquals("MealLogTypeEat")){
            supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = YourMealLogsFragment()
                val args = Bundle()
                args.putString("ModuleName", "HomeDashboard")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }else if (bottomSeatName.contentEquals("SearchActivityLogMove")) {
            supportFragmentManager.beginTransaction().apply {
                val searchWorkoutFragment = SearchWorkoutFragment()
                val args = Bundle()
                args.putString("ModuleName", "HomeDashboard")
                searchWorkoutFragment.arguments = args
                replace(R.id.flFragment, searchWorkoutFragment, "searchWorkoutFragment")
                addToBackStack(null)
                commit()
            }
        }else if (bottomSeatName.contentEquals("RecordEmotionMoodTracThink")) {
            Toast.makeText(this,"Coming Soon", Toast.LENGTH_SHORT).show()
            /*supportFragmentManager.beginTransaction().apply {
                val homeBottomTabFragment = MoodTrackerFragment("", 0,"")
                val args = Bundle()
                args.putString("ModuleName", "HomeDashboard")
                homeBottomTabFragment.arguments = args
                replace(R.id.flFragment, homeBottomTabFragment, "homeBottom")
                addToBackStack(null)
                commit()
            }*/
        }else{
            supportFragmentManager.beginTransaction().apply {
                val homeBottomTabFragment = HomeBottomTabFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("BottomSeatName", bottomSeatName)
                homeBottomTabFragment.arguments = args
                replace(R.id.flFragment, homeBottomTabFragment, "homeBottom")
                addToBackStack(null)
                commit()
            }
        }
    }
}
