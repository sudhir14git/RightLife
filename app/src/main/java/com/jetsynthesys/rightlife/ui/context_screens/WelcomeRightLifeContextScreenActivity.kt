package com.jetsynthesys.rightlife.ui.context_screens

import android.content.Intent
import android.os.Bundle
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity
import com.jetsynthesys.rightlife.databinding.ActivityWelcomeRightLifeContextScreenBinding
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.ui.ActivityUtils
import com.jetsynthesys.rightlife.ui.utility.FeatureFlags

class WelcomeRightLifeContextScreenActivity : BaseActivity()
{
    private lateinit var binding: ActivityWelcomeRightLifeContextScreenBinding
    override fun onCreate(savedInstanceState: Bundle?)
    {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeRightLifeContextScreenBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        binding.btnNext.setOnClickListener {
            navigatePostTrial(
                    intent.getStringExtra(FeatureFlags.EXTRA_ENTRY_DEST) ?: ""
            )
        }
    }

    private fun navigatePostTrial(dest: String)
    {
        when (dest)
        {
            FeatureFlags.MEAL_SCAN ->
            {
                // Never pass null snapMealId (use empty)
                //val snapId = SharedPreferenceManager.getInstance(this).snapMealId ?: ""
                //ActivityUtils.startEatRightReportsActivity(this, "SnapMealTypeEat", "")
                startActivity(Intent(this, MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "EatRight")
                    putExtra("BottomSeatName", "SnapMealTypeEat")
                    putExtra("snapMealId", "")
                })
            }

            FeatureFlags.FACE_SCAN ->
            {
                ActivityUtils.startFaceScanActivity(this)
            }

            else ->
            {
                val intent = Intent(this, HomeNewActivity::class.java).apply {
                    putExtra("OPEN_MY_HEALTH", true)
                    // Optional: Clear back stack
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(intent)
                finish()
            }
        }
        // Optionally finish this screen so back doesn’t return here
         finish()
    }
}