package com.jetsynthesys.rightlife.ui.context_screens

import android.os.Bundle
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityAffirmationContextScreenBinding
import com.jetsynthesys.rightlife.databinding.ActivityMealscanContextScreenBinding

class MealScanContextScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityMealscanContextScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMealscanContextScreenBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        Glide.with(this)
            .asGif()
            .load(R.drawable.meam_scan_context_screen) // or URL: "https://..."
            .into(binding.gifImageView)

        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnNext.setOnClickListener {

        }
    }
}