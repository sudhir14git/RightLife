package com.jetsynthesys.rightlife.ui.context_screens

import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivitySleepSoundsContextScreenBinding
import com.jetsynthesys.rightlife.ui.NewSleepSounds.NewSleepSoundActivity

class SleepSoundsContextScreenActivity : BaseActivity() {
    private lateinit var binding: ActivitySleepSoundsContextScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySleepSoundsContextScreenBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        Glide.with(this)
            .asGif()
            .load(R.drawable.sleep_sound_context_screen)
            .into(binding.gifImageView)

        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, NewSleepSoundActivity::class.java))
            finish()
        }
    }
}