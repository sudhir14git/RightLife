package com.example.rlapp.ui.questionnaire.fragment

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TimePicker
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.example.rlapp.databinding.FragmentBedWakeupTimeBinding
import com.example.rlapp.ui.questionnaire.pojo.Question
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
        binding.timePickerBedtime.hour = 21 // 9 PM
        binding.timePickerBedtime.minute = 10
        binding.timePickerWakeTime.hour = 6 // 6 AM
        binding.timePickerWakeTime.minute = 10


        updateSleepDuration()

        // Listen changes
        binding.timePickerBedtime.setOnTimeChangedListener { _, _, _ -> updateSleepDuration() }
        binding.timePickerWakeTime.setOnTimeChangedListener { _, _, _ -> updateSleepDuration() }

        binding.btnContinue.setOnClickListener {
            Toast.makeText(requireContext(), "Continue Clicked!", Toast.LENGTH_SHORT).show()
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

    private fun getSelectedTime(timePicker: TimePicker): Pair<Int, Int> {
        val hour = timePicker.hour
        val minute = timePicker.minute
        return Pair(hour, minute)
    }


}