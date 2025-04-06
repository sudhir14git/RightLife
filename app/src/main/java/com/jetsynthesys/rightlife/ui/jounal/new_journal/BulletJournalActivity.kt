package com.jetsynthesys.rightlife.ui.jounal.new_journal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.jetsynthesys.rightlife.databinding.ActivityFreeformBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager

class BulletJournalActivity : AppCompatActivity() {

    private lateinit var binding: ActivityFreeformBinding
    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    private var journalItem: JournalItem? = JournalItem()
    private var journalEntry: JournalEntry? = JournalEntry()
    private var previousText = ""
    private var hasStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFreeformBinding.inflate(layoutInflater)
        setContentView(binding.root)
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)
        val name = sharedPreferenceManager.userProfile.userdata.firstName
        binding.tvGreeting.text = "Hello $name,\nWhat’s on your mind?"

        journalItem = intent.getSerializableExtra("Section") as? JournalItem
        journalEntry = intent.getSerializableExtra("JournalEntry") as? JournalEntry

        journalEntry?.let {
            binding.etJournalEntry.setText(it.answer)
        }

        binding.btnSave.setTextColor(
            if (binding.etJournalEntry.text.isNotEmpty()) 0xFF984C01.toInt() else 0xFFBFBFBF.toInt()
        )
        binding.btnSave.isEnabled = binding.etJournalEntry.text.isNotEmpty()
        hasStarted = binding.etJournalEntry.text.isNotEmpty()

        setupListeners()
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnInfo.setOnClickListener {
            // Show tooltip or info dialog
        }

        binding.etJournalEntry.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val hasText = (s?.trim()?.length ?: 0) > 0
                binding.btnSave.isEnabled = hasText
                binding.btnSave.setTextColor(
                    if (hasText) 0xFF984C01.toInt() else 0xFFBFBFBF.toInt()
                )

                val currentText = s.toString()

                // Check if Enter was just pressed
                if (currentText.length > previousText.length &&
                    currentText.endsWith("\n")
                ) {
                    val lines = currentText.lines().filter { it.isNotBlank() }
                    var nextNumber = lines.size + 1
                    val bullet = "$nextNumber. "

                    // Append the bullet instead of a blank line
                    binding.etJournalEntry.removeTextChangedListener(this)

                    // Replace last "\n" with "\nX. "
                    val newText = currentText.dropLast(1) + "\n$bullet"
                    binding.etJournalEntry.setText(newText)
                    binding.etJournalEntry.setSelection(newText.length)

                    binding.etJournalEntry.addTextChangedListener(this)


                }

                if ((s?.trim()?.length ?: 0) == 5000) {
                    Toast.makeText(
                        this@BulletJournalActivity,
                        "Maximum character limit reached!!",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                previousText = s.toString()
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })



        binding.etJournalEntry.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus && !hasStarted) {
                binding.etJournalEntry.setText("1. ")
                binding.etJournalEntry.setSelection(binding.etJournalEntry.text.length)
                hasStarted = true
            }
        }



        binding.btnSave.setOnClickListener {
            val intent =
                Intent(this@BulletJournalActivity, Journal4QuestionsActivity::class.java).apply {
                    putExtra("Section", journalItem)
                    putExtra("Answer", binding.etJournalEntry.text.toString())
                    putExtra("JournalEntry", journalEntry)
                }
            startActivity(intent)

        }
    }
}
