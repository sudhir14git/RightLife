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
            startActivity(Intent(this, HomeNewActivity::class.java))
            finish()
        }
    }
}