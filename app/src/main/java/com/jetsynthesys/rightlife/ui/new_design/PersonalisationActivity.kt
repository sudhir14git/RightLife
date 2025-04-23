package com.jetsynthesys.rightlife.ui.new_design

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager

class PersonalisationActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personalisation)

        var header = intent.getStringExtra("WellnessFocus")
        val sharedPreferenceManager = SharedPreferenceManager.getInstance(this)
        if (header.isNullOrEmpty()) {
            header = sharedPreferenceManager.selectedWellnessFocus
        }

        val tvSkip = findViewById<TextView>(R.id.tv_skip)
        tvSkip.setOnClickListener {
            sharedPreferenceManager.allowPersonalization = true
            sharedPreferenceManager.onBoardingQuestion = true
            val intent = Intent(this, EnableNotificationActivity::class.java)
            intent.putExtra("WellnessFocus", header)
            startActivity(intent)
        }

        val btnAllowPersonalisation = findViewById<Button>(R.id.btn_allow_personalisation)
        btnAllowPersonalisation.setOnClickListener {
            val intent = Intent(this, OnboardingQuestionnaireActivity::class.java)
            intent.putExtra("WellnessFocus", header)
            sharedPreferenceManager.allowPersonalization = true
            startActivity(intent)
        }
    }
}