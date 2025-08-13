package com.jetsynthesys.rightlife.ui.context_screens

import android.content.Intent
import android.os.Bundle
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity
import com.jetsynthesys.rightlife.databinding.ActivityWelcomeSleepRightContextScreenBinding

class WelcomeSleepRightContextScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityWelcomeSleepRightContextScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeSleepRightContextScreenBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        val moduleName = intent.getStringExtra("ModuleName")
        val bottomSheetName = intent.getStringExtra("BottomSeatName")
        val isFromAIDashBoard = intent.getBooleanExtra("isFromAIDashBoard", false)

        binding.btnNext.setOnClickListener {
            if (!isFromAIDashBoard) startActivity(Intent(this, MainAIActivity::class.java).apply {
                putExtra("ModuleName", moduleName)
                putExtra("BottomSeatName", bottomSheetName)
            })
            finish()
        }
    }
}