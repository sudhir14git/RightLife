package com.jetsynthesys.rightlife.ui.jounal.new_journal

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.widget.Toast
import androidx.core.graphics.ColorUtils
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.databinding.ActivityGriefBinding
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
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
        isFromThinkRight = intent.getBooleanExtra("FROM_THINK_RIGHT", false)
        startDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())


        if (questionsList?.isNotEmpty() == true) {
            binding.tvPrompt.text = questionsList?.get(position)?.question
        } else {
            binding.tvPrompt.text = journalEntry?.question
        }

        journalEntry?.let {
            binding.etJournalEntry.setText(it.answer)
        }
        val activeColor = Color.parseColor("#984C01")
        val disabledColor =
            ColorUtils.blendARGB(activeColor, Color.WHITE, 0.5f) // 50% blend to white
        binding.btnSave.setTextColor(
            if (binding.etJournalEntry.text.isNotEmpty()) activeColor else disabledColor
        )
        binding.btnSave.isEnabled = binding.etJournalEntry.text.isNotEmpty()

        setupListeners()
        /*showBalloonWithDim(
            binding.ivRefresh,
            "Tap to swap your prompt.",
            "GriefJournalActivity",
            xOff = -200,
            yOff = 20
        )*/
    }

    private fun setupListeners() {
        binding.btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        binding.btnInfo.setOnClickListener {
            it.disableViewForSeconds()
            val htmlText = when (journalItem?.title) {
                "Gratitude" -> {
                    """
    <p>This practice centers on noticing what’s going well—no matter how big or small.</p>
    <p>Gratitude Journaling has been shown to boost mood, shift perspective, and build emotional resilience.</p>
    <p>Even a few simple entries can help rewire your focus toward the positive.</p>
""".trimIndent()
                }

                "Grief" -> {
                    """
    <p>Grief Journaling is a safe place to hold pain, memories, questions, or anger.</p>
    <p>It’s for anyone navigating loss, change, or heartache.</p>
    <p>There’s no right way to grieve—this space is here to let your feelings breathe, however they show up.</p>
""".trimIndent()
                }

                "Bullet" -> {
                    """
    <p>Bullet Journaling helps organize your inner world in small, manageable pieces.</p>
    <p>Use it to list your moods, wins, worries, intentions—or anything else on your mind.</p>
    <p>It’s a great option when you don’t feel like writing full paragraphs but still want to check in with yourself.</p>
""".trimIndent()
                }

                else -> {
                    """
        <p>Free Form Journaling is all about flow. There are no rules, no structure—just your thoughts, as they come.</p>
        <p>You can write a few lines or fill a page. It’s your space to vent, dream, reflect, or ramble.</p>
        <p>Let go of how it should sound and focus on what you feel.</p>
    """.trimIndent()
                }
            }
            journalItem?.title?.let { it1 ->
                DialogUtils.showJournalCommonDialog(
                    this,
                    it1, htmlText
                )
            }
        }

        binding.ivRefresh.setOnClickListener {
            if (questionsList?.isNotEmpty() == true) {
                if (questionsList?.size?.minus(1) == position)
                    position = 0
                else
                    position += 1
                binding.tvPrompt.text = questionsList?.getOrNull(position)?.question ?: ""
            }
        }

        if (journalItem?.title == "Bullet") {
            binding.etJournalEntry.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val hasText = (s?.trim()?.length ?: 0) > 0
                    binding.btnSave.isEnabled = hasText
                    val activeColor = Color.parseColor("#984C01")
                    val disabledColor =
                        ColorUtils.blendARGB(activeColor, Color.WHITE, 0.5f) // 50% blend to white

                    binding.btnSave.setTextColor(
                        if (hasText) activeColor else disabledColor
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
        } else {
            binding.etJournalEntry.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    val hasText = (s?.trim()?.length ?: 0) > 0
                    binding.btnSave.isEnabled = hasText
                    val activeColor = Color.parseColor("#984C01")
                    val disabledColor =
                        ColorUtils.blendARGB(activeColor, Color.WHITE, 0.5f) // 50% blend to white

                    binding.btnSave.setTextColor(
                        if (hasText) activeColor else disabledColor
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
            it.disableViewForSeconds()
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
