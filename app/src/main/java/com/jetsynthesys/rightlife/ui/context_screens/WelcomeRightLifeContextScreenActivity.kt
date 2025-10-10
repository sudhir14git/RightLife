package com.jetsynthesys.rightlife.ui.context_screens

import android.content.Intent
import android.os.Bundle
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityWelcomeRightLifeContextScreenBinding
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity

class WelcomeRightLifeContextScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityWelcomeRightLifeContextScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWelcomeRightLifeContextScreenBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        binding.btnNext.setOnClickListener {
            val intent = Intent(this, HomeNewActivity::class.java).apply {
                putExtra("OPEN_MY_HEALTH", true)
                // Optional: Clear back stack
                flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK
            }
            startActivity(intent)
            finish()
        }
    }
}