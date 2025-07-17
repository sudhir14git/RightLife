package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.databinding.FragmentBedWakeupTimeBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireThinkRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.SRQuestionThree
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.SleepTimeAnswer
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import java.time.Duration
import java.time.LocalTime

class BedWakeupTimeFragment : Fragment() {
    private lateinit var binding: FragmentBedWakeupTimeBinding

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): BedWakeupTimeFragment {
            val fragment = BedWakeupTimeFragment()
            val args = Bundle().apply {
                putSerializable("question", question)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            question = it.getSerializable("question") as? Question
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBedWakeupTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @SuppressLint("SetTextI18n")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize TimePickers
        binding.timePickerBedtime.setIs24HourView(false)
        binding.timePickerWakeTime.setIs24HourView(false)

        // Set default values
        binding.timePickerBedtime.hour = 22 // 9 PM
        binding.timePickerBedtime.minute = 0
        binding.timePickerWakeTime.hour = 6 // 6 AM
        binding.timePickerWakeTime.minute = 0


        updateSleepDuration()

        // Listen changes
        binding.timePickerBedtime.setOnTimeChangedListener { _, _, _ -> updateSleepDuration() }
        binding.timePickerWakeTime.setOnTimeChangedListener { _, _, _ -> updateSleepDuration() }

        binding.btnContinue.setOnClickListener {
            binding.btnContinue.disableViewForSeconds()
            val sleepTimeAnswer = SleepTimeAnswer()
            sleepTimeAnswer.bedTime = getSelectedTimeFromTimePicker(binding.timePickerBedtime)
            sleepTimeAnswer.wakeTime = getSelectedTimeFromTimePicker(binding.timePickerWakeTime)
            sleepTimeAnswer.sleepDuration = binding.tvSleepDuration.text.toString()
            submit(sleepTimeAnswer)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateSleepDuration() {
        val bedtime = getSelectedTime(binding.timePickerBedtime)
        val wakeTime = getSelectedTime(binding.timePickerWakeTime)

        val bedtimeLocal = LocalTime.of(bedtime.first, bedtime.second)
        val wakeTimeLocal = LocalTime.of(wakeTime.first, wakeTime.second)

        var duration = Duration.between(bedtimeLocal, wakeTimeLocal)
        if (duration.isNegative) {
            // Adjust for overnight sleep
            duration = duration.plusHours(24)
        }

        val hours = duration.toHours()
        val minutes = duration.toMinutes() % 60
        binding.tvSleepDuration.text = "Sleep duration: ${hours} hrs ${minutes} mins"
    }

    private fun getSelectedTimeFromTimePicker(timePicker: TimePicker): String {
        val hour = timePicker.hour

        val minute = timePicker.minute

        val amPm = if (hour >= 12) "PM" else "AM"

        // Convert to 12-hour format
        val formattedHour = if (hour % 12 == 0) 12 else hour % 12

        // Ensure two-digit format
        return String.format("%02d:%02d %s", formattedHour, minute, amPm)
    }


    private fun getSelectedTime(timePicker: TimePicker): Pair<Int, Int> {
        val hour = timePicker.hour
        val minute = timePicker.minute
        return Pair(hour, minute)
    }

    private fun submit(answer: SleepTimeAnswer) {
        val questionThree = SRQuestionThree()
        questionThree.answer = answer
        QuestionnaireThinkRightActivity.questionnaireAnswerRequest.sleepRight?.questionThree = questionThree
        QuestionnaireThinkRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireThinkRightActivity.questionnaireAnswerRequest
        )
    }


}