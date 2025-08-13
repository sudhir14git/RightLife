package com.jetsynthesys.rightlife.ui.context_screens

import android.content.Intent
import android.os.Bundle
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityJournalContextScreenBinding
import com.jetsynthesys.rightlife.ui.jounal.new_journal.JournalListActivity
import com.jetsynthesys.rightlife.ui.jounal.new_journal.JournalNewActivity

class JournalContextScreenActivity : BaseActivity() {
    private lateinit var binding: ActivityJournalContextScreenBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityJournalContextScreenBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        val isCreateJournal = intent.getBooleanExtra("IS_CREATE_JOURNAL", false)

        Glide.with(this)
            .asGif()
            .load(R.drawable.journal_context_screen) // or URL: "https://..."
            .into(binding.gifImageView)

        binding.iconBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnNext.setOnClickListener {
            val intent = if (isCreateJournal)
                Intent(this, JournalNewActivity::class.java)
            else
                Intent(this, JournalListActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}