package com.jetsynthesys.rightlife.ui.context_screens

import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityMindAuditContextScreenBinding
import com.jetsynthesys.rightlife.ui.mindaudit.MindAuditFromActivity

class MindAuditContextScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityMindAuditContextScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMindAuditContextScreenBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        Glide.with(this)
            .asGif()
            .load(R.drawable.mind_audit_context_screen) // or URL: "https://..."
            .into(binding.gifImageView)

        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        val isFromThinkRight = intent.getBooleanExtra("FROM_THINK_RIGHT", false)

        binding.btnNext.setOnClickListener {
            startActivity(Intent(this, MindAuditFromActivity::class.java).apply {
                putExtra("FROM_THINK_RIGHT", isFromThinkRight)
            })
            finish()
        }

    }
}