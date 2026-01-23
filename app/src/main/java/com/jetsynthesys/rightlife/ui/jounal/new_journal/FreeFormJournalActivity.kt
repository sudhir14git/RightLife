package com.jetsynthesys.rightlife.ui.jounal.new_journal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityFreeformBinding
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import java.time.Instant
import java.time.format.DateTimeFormatter

class FreeFormJournalActivity : BaseActivity() {

    private var isFromThinkRight = false
    private lateinit var binding: ActivityFreeformBinding
    private var journalItem: JournalItem? = JournalItem()
    private var journalEntry: JournalEntry? = JournalEntry()
    private var startDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFreeformBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)
        val name = sharedPreferenceManager.userProfile.userdata.firstName
        binding.tvGreeting.text = "Hello $name,\nWhat’s on your mind?"

        journalEntry = intent.getSerializableExtra("JournalEntry") as? JournalEntry
        isFromThinkRight = intent.getBooleanExtra("FROM_THINK_RIGHT", false)

        journalItem = intent.getSerializableExtra("Section") as? JournalItem

        startDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

        journalEntry?.let {
            binding.etJournalEntry.setText(it.answer)
        }

        binding.btnSave.setTextColor(
            if (binding.etJournalEntry.text.isNotEmpty()) 0xFF984C01.toInt() else 0xFFBFBFBF.toInt()
        )
        binding.btnSave.isEnabled = binding.etJournalEntry.text.isNotEmpty()

        setupListeners()
    }

    private fun setupListeners() {

        val htmlText = """
        <p>Free Form Journaling is all about flow. There are no rules, no structure—just your thoughts, as they come.</p>
        <p>You can write a few lines or fill a page. It’s your space to vent, dream, reflect, or ramble.</p>
        <p>Let go of how it should sound and focus on what you feel.</p>
    """.trimIndent()

        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnInfo.setOnClickListener {
            it.disableViewForSeconds()
            DialogUtils.showJournalCommonDialog(this, "Free Form", htmlText)
        }

        binding.etJournalEntry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val hasText = (s?.trim()?.length ?: 0) > 0
                binding.btnSave.isEnabled = hasText
                binding.btnSave.setTextColor(
                    if (hasText) 0xFF984C01.toInt() else 0xFFBFBFBF.toInt()
                )

                if ((s?.trim()?.length ?: 0) == 5000) {
                    Toast.makeText(
                        this@FreeFormJournalActivity,
                        "Maximum character limit reached!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })


        binding.btnSave.setOnClickListener {
            it.disableViewForSeconds()
            // Save logic here
            val intent =
                Intent(this@FreeFormJournalActivity, Journal4QuestionsActivity::class.java).apply {
                    putExtra("Section", journalItem)
                    putExtra("Answer", binding.etJournalEntry.text.toString())
                    putExtra("JournalEntry", journalEntry)
                    putExtra("StartDate", startDate)
                    putExtra("StartDate", startDate)
                    putExtra("FROM_THINK_RIGHT", isFromThinkRight)
                }
            startActivity(intent)

        }
    }
}
