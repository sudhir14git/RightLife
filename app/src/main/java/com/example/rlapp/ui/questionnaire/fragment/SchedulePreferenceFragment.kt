package com.example.rlapp.ui.questionnaire.fragment

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rlapp.R
import com.example.rlapp.databinding.BottomsheetFoodScheduleReminderBinding
import com.example.rlapp.databinding.FragmentSchedulePreferenceBinding
import com.example.rlapp.ui.affirmation.ReminderReceiver
import com.example.rlapp.ui.questionnaire.QuestionnaireEatRightActivity
import com.example.rlapp.ui.questionnaire.adapter.ScheduleOptionAdapter
import com.example.rlapp.ui.questionnaire.pojo.Question
import com.example.rlapp.ui.questionnaire.pojo.ScheduleOption
import com.google.android.material.bottomsheet.BottomSheetDialog
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class SchedulePreferenceFragment : Fragment() {

    private var _binding: FragmentSchedulePreferenceBinding? = null
    private val binding get() = _binding!!

    private var selectedMorningTime: String? = ""
    private var selectedAfternoonTime: String? = ""
    private var selectedEveningTime: String? = ""

    private val scheduleOptions = listOf(
        ScheduleOption(R.drawable.ic_schedule_1, "Always", "I stick to a fixed schedule"),
        ScheduleOption(R.drawable.ic_schedule_2, "Mostly", "Small changes here and there"),
        ScheduleOption(R.drawable.ic_schedule_3, "Sometimes", "It depends on my day"),
        ScheduleOption(R.drawable.ic_schedule_4, "Rarely", "My timings are all over the place"),
        ScheduleOption(R.drawable.ic_schedule_5, "Never", "I don’t follow any pattern")
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): SchedulePreferenceFragment {
            val fragment = SchedulePreferenceFragment()
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
        _binding = FragmentSchedulePreferenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ScheduleOptionAdapter(scheduleOptions) { selectedOption ->
            showReminderBottomSheet()
        }
        binding.rvScheduleOptions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvScheduleOptions.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showReminderBottomSheet() {
        // Create and configure BottomSheetDialog
        val reminderBottomSheetDialog = BottomSheetDialog(requireContext())

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetFoodScheduleReminderBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root
        reminderBottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }
        reminderBottomSheetDialog.setCancelable(false)

        dialogBinding.ivDialogClose.setOnClickListener {
            reminderBottomSheetDialog.dismiss()
            QuestionnaireEatRightActivity.navigateToNextPage()
        }

        dialogBinding.llBreakFastTime.setOnClickListener {
            showTimePickerDialog(dialogBinding.tvTimeBreakfast, 1)
        }
        dialogBinding.llLunchTime.setOnClickListener {
            showTimePickerDialog(dialogBinding.tvTimeLunch, 2)
        }
        dialogBinding.llDinnerTime.setOnClickListener {
            showTimePickerDialog(dialogBinding.tvTimeDinner, 3)
        }

        dialogBinding.btnSetNow.setOnClickListener {
            if (!(selectedMorningTime.isNullOrEmpty() || selectedAfternoonTime.isNullOrEmpty() || selectedEveningTime.isNullOrEmpty())) {
                Toast.makeText(
                    requireContext(),
                    "Please at least one reminder!!", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else {

                if (!selectedMorningTime.isNullOrEmpty())
                    selectedMorningTime?.let { it1 -> setReminder(it1) }
                if (!selectedAfternoonTime.isNullOrEmpty())
                    selectedAfternoonTime?.let { it1 -> setReminder(it1) }
                if (!selectedEveningTime.isNullOrEmpty())
                    selectedEveningTime?.let { it1 -> setReminder(it1) }

                reminderBottomSheetDialog.dismiss()
                QuestionnaireEatRightActivity.navigateToNextPage()
            }
        }

        reminderBottomSheetDialog.show()
    }


    private fun showTimePickerDialog(textView: TextView, type: Int) {
        val currentTime: Calendar = Calendar.getInstance()
        val hour: Int = currentTime.get(Calendar.HOUR_OF_DAY)
        val minute: Int = currentTime.get(Calendar.MINUTE)
        val mTimePicker = TimePickerDialog(
            requireContext(),
            { timePicker, selectedHour, selectedMinute ->
                run {

                    val amPm = if (selectedHour >= 12) "PM" else "AM"
                    val hourFormatted = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                    val minuteFormatted = String.format("%02d", selectedMinute)
                    when (type) {
                        1 ->
                            if (selectedHour <= 12) {
                                textView.text = "$hourFormatted:$minuteFormatted $amPm"
                                selectedMorningTime = textView.text.toString()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Please select a morning time (AM only)",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@TimePickerDialog
                            }

                        2 ->
                            if (selectedHour in 12..18) {
                                textView.text = "$hourFormatted:$minuteFormatted $amPm"
                                selectedAfternoonTime = textView.text.toString()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Please select a time in the afternoon.",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@TimePickerDialog
                            }

                        3 ->
                            if (selectedHour >= 18) {
                                textView.text = "$hourFormatted:$minuteFormatted $amPm"
                                selectedEveningTime = textView.text.toString()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Please select a time in the evening (6:00 PM to 11:59 PM).",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                    }
                }
            },
            hour,
            minute,
            false
        ) // 12 hour time
        mTimePicker.setTitle("Select Time")
        mTimePicker.show()
    }

    private fun setReminder(time: String) {
        if (!checkPermission()) {
            return
        }

        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(requireContext(), ReminderReceiver::class.java).apply {
            action = "EAT_ALARM_TRIGGERED"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            100, // Unique code
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Parse "6:40 PM" properly
        val sdf = SimpleDateFormat("h:mm a", Locale.getDefault())
        val date = sdf.parse(time)

        val calendar = Calendar.getInstance()
        calendar.time = date!!
        // Set today's date
        val now = Calendar.getInstance()
        calendar.set(Calendar.YEAR, now.get(Calendar.YEAR))
        calendar.set(Calendar.MONTH, now.get(Calendar.MONTH))
        calendar.set(Calendar.DAY_OF_MONTH, now.get(Calendar.DAY_OF_MONTH))

        // If time already passed, schedule for tomorrow
        if (calendar.before(now)) {
            calendar.add(Calendar.DATE, 1)
        }

        val triggerTime = calendar.timeInMillis


        alarmManager.setExactAndAllowWhileIdle(
            AlarmManager.RTC_WAKEUP,
            triggerTime,
            pendingIntent
        )
    }


    private fun checkPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    requireActivity(),
                    arrayOf(Manifest.permission.POST_NOTIFICATIONS),
                    100
                )
                return false
            } else {
                return true
            }
        } else {
            // Permission not required before Android 13
            return true
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Toast.makeText(
                    requireContext(),
                    "Notification permission granted",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                Toast.makeText(
                    requireContext(),
                    "Notification permission denied",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}
