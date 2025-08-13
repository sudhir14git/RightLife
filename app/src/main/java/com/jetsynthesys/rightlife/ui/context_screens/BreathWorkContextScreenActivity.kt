package com.jetsynthesys.rightlife.ui.context_screens

import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityBreathWorkContextScreenBinding
import com.jetsynthesys.rightlife.ui.breathwork.BreathworkActivity

class BreathWorkContextScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityBreathWorkContextScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreathWorkContextScreenBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        Glide.with(this)
            .asGif()
            .load(R.drawable.breath_work_context_screen)
            .into(binding.gifImageView)

        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, BreathworkActivity::class.java))
            finish()
        }
    }
}