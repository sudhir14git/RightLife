package com.example.rlapp.ui.new_design

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.rlapp.R

class UnLockWellnessActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_unlock_wellness)

        val btnUnlockWellness = findViewById<Button>(R.id.btn_unlock)
        btnUnlockWellness.setOnClickListener {
            // onclick code here for UnLock Button
        }

        val tvNoThanks = findViewById<TextView>(R.id.tv_no_thanks)
        tvNoThanks.setOnClickListener {
            val intent = Intent()
            startActivity(intent)
            finish()
        }
    }
}