package com.jetsynthesys.rightlife.ui.context_screens

import android.content.Intent
import android.os.Bundle
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity
import com.jetsynthesys.rightlife.databinding.ActivityWelcomeThinkRightContextScreenBinding

class WelcomeThinkRightContextScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityWelcomeThinkRightContextScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeThinkRightContextScreenBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        val moduleName = intent.getStringExtra("ModuleName")
        val bottomSheetName = intent.getStringExtra("BottomSeatName")
        val isFromAIDashBoard = intent.getBooleanExtra("isFromAIDashBoard", false)

        binding.btnNext.setOnClickListener {
            if (!isFromAIDashBoard)
                startActivity(Intent(this, MainAIActivity::class.java).apply {
                    putExtra("ModuleName", moduleName)
                    putExtra("BottomSeatName", bottomSheetName)
                })
            finish()
        }
    }
}