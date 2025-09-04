package com.jetsynthesys.rightlife.ui.breathwork


import android.content.Intent
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.databinding.ActivityBreathworkSessionBinding
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.breathwork.pojo.BreathingData
import com.shawnlin.numberpicker.NumberPicker
import java.time.Instant
import java.time.format.DateTimeFormatter


class BreathworkSessionActivity : BaseActivity() {


    private lateinit var binding: ActivityBreathworkSessionBinding
    private var sessionCount = 1 // Default session count
    private var breathingData: BreathingData? = null
    private var startDate = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBreathworkSessionBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        // Retrieve the selected breathing practice from the intent

        breathingData = intent.getSerializableExtra("BREATHWORK") as BreathingData
        startDate = intent.getStringExtra("StartDate").toString()
        if (startDate.isEmpty())
            startDate = DateTimeFormatter.ISO_INSTANT.format(Instant.now())

        /*binding.ivPlus.setImageResource(
            if (breathingData?.isAddedToToolKit!!) R.drawable.greentick else R.drawable.ic_breathing_toolkit
        )*/
        setSaveImage(breathingData!!)

        setupUI()
        setupListeners(breathingData!!)
        calculateSessiontime()
        setBreathingTypeColors()
    }

    private fun setSaveImage(breathingData: BreathingData) {

        if (breathingData.isAddedToToolKit) {
            binding.ivPlus.setImageResource(R.drawable.ic_save_article_active)
            binding.ivPlus.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.color_eat_right
                    )
                )
        } else {
            binding.ivPlus.setImageResource(R.drawable.ic_save_article)
            binding.ivPlus.imageTintList =
                ColorStateList.valueOf(
                    ContextCompat.getColor(
                        this,
                        R.color.black
                    )
                )
        }

        val textColorRes = when (breathingData.title?.trim()) {
            "Box Breathing" -> R.color.box_breathing_card_color_text
            "Alternate Nostril Breathing" -> R.color.alternate_breathing_card_color_text
            "4-7-8 Breathing" -> R.color.four_seven_breathing_card_color_text
            "Custom" -> R.color.custom_breathing_card_color_text
            else -> R.color.custom_breathing_card_color_text
        }

        val textColor = ContextCompat.getColor(this, textColorRes)

        binding.ivPlus.imageTintList = ColorStateList.valueOf(textColor)
    }

    private fun setupUI() {
        // Set initial session count
        binding.tvSessionCount.text = sessionCount.toString()
        binding.tvTitle.text = breathingData?.title
        binding.tvDescription.text = breathingData?.subTitle

        Glide.with(this)
            .load(ApiClient.CDN_URL_QA + breathingData?.thumbnail)
            .placeholder(R.drawable.rl_placeholder)
            .error(R.drawable.rl_placeholder)
            .into(binding.ivBreathworkImage)


        if (breathingData?.title == "Custom") {
            binding.layoutCustomPicker.visibility = View.VISIBLE

            setupNumberPicker(binding.pickerInhale, 1, 16, 4)
            setupNumberPicker(binding.pickerHold, 1, 16, 4)
            setupNumberPicker(binding.pickerExhale, 1, 16, 4)

            // Update data when user changes picker
            binding.pickerInhale.setOnValueChangedListener { _, _, newVal ->
                breathingData?.breathInhaleTime = newVal.toString()
                calculateSessiontime()
            }
            binding.pickerHold.setOnValueChangedListener { _, _, newVal ->
                breathingData?.breathHoldTime = newVal.toString()
                calculateSessiontime()
            }
            binding.pickerExhale.setOnValueChangedListener { _, _, newVal ->
                breathingData?.breathExhaleTime = newVal.toString()
                calculateSessiontime()
            }
        } else {
            binding.layoutCustomPicker.visibility = View.GONE
        }
    }

    private fun setupNumberPicker(picker: NumberPicker, min: Int, max: Int, defaultVal: Int) {
        picker.minValue = min
        picker.maxValue = max
        picker.value = defaultVal
        picker.wrapSelectorWheel = false
    }

    private fun setupListeners(breathWorData: BreathingData) {
        binding.ivBack.setOnClickListener { finish() }

        binding.btnMinus.setOnClickListener {
            if (sessionCount > 1) {
                sessionCount--
                binding.tvSessionCount.text = sessionCount.toString()
                calculateSessiontime()
            }
        }

        binding.btnPlus.setOnClickListener {
            sessionCount++
            binding.tvSessionCount.text = sessionCount.toString()
            calculateSessiontime()
        }

        binding.btnContinue.setOnClickListener {
            val intent = Intent(this, BreathworkPracticeActivity::class.java)
            intent.putExtra("sessionCount", sessionCount * 4)
            intent.putExtra("BREATHWORK", breathingData)
            intent.putExtra("StartDate", startDate)
            //intent.putExtra("ITEM_DESCRIPTION", selectedItem.description)

            startActivity(intent)

        }

        binding.switchHaptic.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                Toast.makeText(this, "Haptic Feedback Enabled", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(this, "Haptic Feedback Disabled", Toast.LENGTH_SHORT).show()
            }
        }

        binding.ivPlus.setOnClickListener {
            CommonAPICall.addToToolKit(
                this@BreathworkSessionActivity,
                breathingData?.id,
                !breathingData?.isAddedToToolKit!!
            )
            breathingData?.isAddedToToolKit = !breathingData?.isAddedToToolKit!!
            setSaveImage(breathingData!!)
        }
    }

    fun calculateSessiontime() {
        var totalSets = sessionCount
        var inhaleTime = breathingData?.breathInhaleTime?.toLong()!! * 1000
        var exhaleTime = breathingData?.breathExhaleTime?.toLong()!! * 1000
        var holdTime = breathingData?.breathHoldTime?.toLong()!! * 1000

        // Calculate session duration based on the selected practice
        val cycleDuration = inhaleTime +
                holdTime +
                exhaleTime +
                (if (breathingData?.title == "Box Breathing") holdTime else 0L)
        var sessionDurationSeconds = (totalSets * cycleDuration / 1000).toInt()

        // Set initial values

        updateSessionTimer(sessionDurationSeconds * 4 * 1000L)
    }

    private fun updateSessionTimer(millisUntilFinished: Long) {
        val minutes = (millisUntilFinished / 1000) / 60
        val seconds = (millisUntilFinished / 1000) % 60
        binding.tvSettime.text = String.format("%02d:%02d", minutes, seconds)
    }


    private fun setBreathingTypeColors() {
        // Get the breathing type from the breathingData
        val breathingType = breathingData?.title?.trim() ?: ""

        // Get the color resource based on breathing type
        val colorResource = when (breathingType) {
            "Box Breathing" -> R.color.box_breathing_card_color
            "Alternate Nostril Breathing" -> R.color.alternate_breathing_card_color
            "4-7-8 Breathing" -> R.color.four_seven_breathing_card_color
            "Custom" -> R.color.custom_breathing_card_color
            else -> R.color.alternate_breathing_card_color
        }
        val colorResourceText = when (breathingType) {
            "Box Breathing" -> R.color.box_breathing_card_color_text
            "Alternate Nostril Breathing" -> R.color.alternate_breathing_card_color_text
            "4-7-8 Breathing" -> R.color.four_seven_breathing_card_color_text
            "Custom" -> R.color.custom_breathing_card_color_text
            else -> R.color.alternate_breathing_card_color_text
        }

        // Get the actual color value
        val mainColor = resources.getColor(colorResource, null)
        val textColor = resources.getColor(colorResourceText, null)
        // Apply color to the Continue button
        binding.btnContinue.backgroundTintList = ColorStateList.valueOf(mainColor)
        binding.btnContinue.setTextColor(resources.getColor(colorResourceText, null))
        binding.btnPlus.backgroundTintList = ColorStateList.valueOf(mainColor)
        binding.btnMinus.backgroundTintList = ColorStateList.valueOf(mainColor)
        binding.ivBreathworkImage.imageTintList = ColorStateList.valueOf(textColor)
        binding.tvSettime.setTextColor(resources.getColor(colorResourceText, null))
        binding.tvSessionCount.setTextColor(resources.getColor(colorResourceText, null))
        binding.tvTitle.setTextColor(resources.getColor(colorResourceText, null))
        binding.tvDescription.setTextColor(resources.getColor(colorResourceText, null))
        binding.tvSetInfo.setTextColor(resources.getColor(colorResourceText, null))
        binding.tvSessionSets.setTextColor(resources.getColor(colorResourceText, null))
        binding.tvHaptic.setTextColor(resources.getColor(colorResourceText, null))

        binding.tvInhale.setTextColor(resources.getColor(colorResourceText, null))
        binding.tvhold.setTextColor(resources.getColor(colorResourceText, null))
        binding.tvExhale.setTextColor(resources.getColor(colorResourceText, null))

    }
}
