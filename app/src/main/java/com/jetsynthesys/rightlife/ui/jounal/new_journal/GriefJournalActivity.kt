package com.jetsynthesys.rightlife.ui.jounal.new_journal

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityGriefBinding
import com.jetsynthesys.rightlife.ui.showBalloonWithDim
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import java.time.Instant
import java.time.format.DateTimeFormatter

class GriefJournalActivity : BaseActivity() {

    private lateinit var binding: ActivityGriefBinding
    private var journalItem: JournalItem? = JournalItem()
    private var journalEntry: JournalEntry? = JournalEntry()
    private var questionsList: ArrayList<Question>? = ArrayList()
    private var position: Int = 0
    private var startDate = ""
    var isFromThinkRight: Boolean = false
    private var previousText = ""
    private var hasStarted = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityGriefBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        sharedPreferenceManager = SharedPreferenceManager.getInstance(this)

        journalItem = intent.getSerializableExtra("Section") as? JournalItem
        journalEntry = intent.getSerializableExtra("JournalEntry") as? JournalEntry
        questionsList = intent.getSerializableExtra("QuestionList") as? ArrayList<Question>
        position = intent.getIntExtra("Position", 0)
        startDate = intent.getStringExtra("StartDate").toString()
        isFromThinkRight = intent.getBooleanExtra("FROM_THINK_RIGHT", false)
        if (startDate.isEmpty())
            startDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())


        if (questionsList?.isNotEmpty() == true) {
            binding.tvPrompt.text = questionsList?.get(position)?.question
        } else {
            binding.tvPrompt.text = journalEntry?.question
        }

        journalEntry?.let {
            binding.etJournalEntry.setText(it.answer)
        }

        binding.btnSave.setTextColor(
            if (binding.etJournalEntry.text.isNotEmpty()) 0xFF984C01.toInt() else 0xFFBFBFBF.toInt()
        )
        binding.btnSave.isEnabled = binding.etJournalEntry.text.isNotEmpty()

        setupListeners()
        showBalloonWithDim(
            binding.ivRefresh,
            "Tap to swap your prompt.",
            "GriefJournalActivity",
            xOff = -200,
            yOff = 20
        )
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnInfo.setOnClickListener {
            // Show tooltip or info dialog
        }

        binding.ivRefresh.setOnClickListener {
            if (questionsList?.isNotEmpty() == true) {
                if (questionsList!!.size - 1 == position)
                    position = 0
                else
                    position += 1
                binding.tvPrompt.text = questionsList!![position].question
            }
        }

        if (journalItem?.title == "Bullet") {
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
                            this@GriefJournalActivity,
                            "Maximum character limit reached!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
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
        }else {
            binding.etJournalEntry.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val hasText = (s?.trim()?.length ?: 0) > 0
                    binding.btnSave.isEnabled = hasText
                    binding.btnSave.setTextColor(
                        if (hasText) 0xFF984C01.toInt() else 0xFFBFBFBF.toInt()
                    )
                    if ((s?.trim()?.length ?: 0) == 5000) {
                        Toast.makeText(
                            this@GriefJournalActivity,
                            "Maximum character limit reached!!",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }

                override fun beforeTextChanged(
                    s: CharSequence?,
                    start: Int,
                    count: Int,
                    after: Int
                ) {
                }

                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }

        binding.btnSave.setOnClickListener {
            // Save logic here
            val intent =
                Intent(this@GriefJournalActivity, Journal4QuestionsActivity::class.java).apply {
                    putExtra("Section", journalItem)
                    putExtra("Answer", binding.etJournalEntry.text.toString())
                    putExtra("FROM_THINK_RIGHT", isFromThinkRight)
                    if (journalEntry != null)
                        putExtra("QuestionId", journalEntry?.questionId)
                    else
                        putExtra("QuestionId", questionsList?.get(position)?.id)
                    putExtra("JournalEntry", journalEntry)
                    putExtra("StartDate", startDate)
                }
            startActivity(intent)

        }
    }
}
