package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.Manifest
import android.app.TimePickerDialog
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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.BottomsheetFoodScheduleReminderBinding
import com.jetsynthesys.rightlife.databinding.FragmentSchedulePreferenceBinding
import com.jetsynthesys.rightlife.ui.affirmation.NotificationHelper
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireEatRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.adapter.ScheduleOptionAdapter
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.ERQuestionThree
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.ScheduleOption
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import com.jetsynthesys.rightlife.ui.utility.runWithCooldown

class SchedulePreferenceFragment : Fragment() {

    private var _binding: FragmentSchedulePreferenceBinding? = null
    private val binding get() = _binding!!

    private var selectedMorningTime: String? = "9:00 AM"
    private var selectedAfternoonTime: String? = "1:00 PM"
    private var selectedEveningTime: String? = "8:00 PM"

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
        val adapter =
            ScheduleOptionAdapter(scheduleOptions, "EatRight", { selectedOption: ScheduleOption ->
                if (selectedOption.title == "Rarely" || selectedOption.title == "Never")
                    showReminderBottomSheet(selectedOption.title)
                else {
                    submit(selectedOption.title)
                }
            }.runWithCooldown())
        binding.rvScheduleOptions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvScheduleOptions.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun submit(answer: String) {
        val questionThree = ERQuestionThree()
        questionThree.answer = answer
        QuestionnaireEatRightActivity.questionnaireAnswerRequest.eatRight?.questionThree =
            questionThree
        QuestionnaireEatRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireEatRightActivity.questionnaireAnswerRequest
        )
    }

    private fun showReminderBottomSheet(answer: String) {
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
            //QuestionnaireEatRightActivity.navigateToNextPage()
            submit(answer)
        }

        dialogBinding.llBreakFastTime.setOnClickListener {
            dialogBinding.llBreakFastTime.disableViewForSeconds()
            showTimePickerDialog(dialogBinding.tvTimeBreakfast, 1, 9)
        }
        dialogBinding.llLunchTime.setOnClickListener {
            dialogBinding.llLunchTime.disableViewForSeconds()
            showTimePickerDialog(dialogBinding.tvTimeLunch, 2, 13)
        }
        dialogBinding.llDinnerTime.setOnClickListener {
            dialogBinding.llDinnerTime.disableViewForSeconds()
            showTimePickerDialog(dialogBinding.tvTimeDinner, 3, 20)
        }

        dialogBinding.btnSetNow.setOnClickListener {
            if ((selectedMorningTime.isNullOrEmpty() && selectedAfternoonTime.isNullOrEmpty() && selectedEveningTime.isNullOrEmpty())) {
                Toast.makeText(
                    requireContext(),
                    "Please select at least one reminder!!", Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            } else {

                dialogBinding.btnSetNow.disableViewForSeconds()

                if (!selectedMorningTime.isNullOrEmpty())
                    selectedMorningTime?.let { it1 -> setReminder(it1) }
                if (!selectedAfternoonTime.isNullOrEmpty())
                    selectedAfternoonTime?.let { it1 -> setReminder(it1) }
                if (!selectedEveningTime.isNullOrEmpty())
                    selectedEveningTime?.let { it1 -> setReminder(it1) }

                reminderBottomSheetDialog.dismiss()
                //QuestionnaireEatRightActivity.navigateToNextPage()
                /*Toast.makeText(
                    requireContext(),
                    "Reminders Set  ", Toast.LENGTH_SHORT
                ).show()*/
                Utils.showCustomToast(requireContext(), "Reminder Time Saved")
                submit(answer)
            }
        }

        reminderBottomSheetDialog.show()
    }

    private fun showTimePickerDialog(
        textView: TextView,
        type: Int,
        hour: Int,
        minute: Int = 0
    ) {
        val mTimePicker = TimePickerDialog(
            requireContext(),
            { timePicker, selectedHour, selectedMinute ->
                run {

                    val amPm = if (selectedHour >= 12) "PM" else "AM"
                    val hourFormatted = if (selectedHour % 12 == 0) 12 else selectedHour % 12
                    val minuteFormatted = String.format("%02d", selectedMinute)
                    when (type) {
                        1 ->
                            if (selectedHour in 5..11) {
                                textView.text = "$hourFormatted:$minuteFormatted $amPm"
                                selectedMorningTime = textView.text.toString()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Please select a time between 5:00 AM to 11:00 AM",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@TimePickerDialog
                            }

                        2 ->
                            if (selectedHour in 11..15) {
                                textView.text = "$hourFormatted:$minuteFormatted $amPm"
                                selectedAfternoonTime = textView.text.toString()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Please select a time between 11:00 AM to 3:00 PM",
                                    Toast.LENGTH_SHORT
                                ).show()
                                return@TimePickerDialog
                            }

                        3 ->
                            if (selectedHour in 17..22) {
                                textView.text = "$hourFormatted:$minuteFormatted $amPm"
                                selectedEveningTime = textView.text.toString()
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Please select a time between 5:00 PM to 10:00 PM",
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
        NotificationHelper.setReminder(requireContext(), "EAT_ALARM_TRIGGERED", time)
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
