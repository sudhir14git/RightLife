package com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.AddToolRequest
import com.jetsynthesys.rightlife.ai_package.model.BaseResponse
import com.jetsynthesys.rightlife.ai_package.model.LogNapRequest
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.LocalDate
import java.time.LocalTime
import java.time.format.DateTimeFormatter

class LogYourNapDialogFragment : BottomSheetDialogFragment() {
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvDate: TextView

    private var startTime: LocalTime = LocalTime.of(21, 0)
    private var endTime: LocalTime = LocalTime.of(5, 30)
    private var selectedDate: LocalDate = LocalDate.now()

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.dialog_fragment_log_last_night_sleep, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        tvStartTime = view.findViewById(R.id.tvStartTime)
        tvEndTime = view.findViewById(R.id.tvEndTime)
        tvDuration = view.findViewById(R.id.tvDuration)
        tvDate = view.findViewById(R.id.tvDate)

        updateDuration()

        view.findViewById<View>(R.id.btnDatePicker).setOnClickListener {
            showDatePicker()
        }

        view.findViewById<View>(R.id.startTimeContainer).setOnClickListener {
            TimePickerDialogFragment(6, 30) { hour, minute ->
                // Handle selected time
                val formatted = LocalTime.of(hour, minute)
                    .format(DateTimeFormatter.ofPattern("hh:mm a"))
                tvStartTime.text = formatted
            }.show(childFragmentManager, "TimePickerDialog")
           // showTimePicker(isStart = true)
        }

        view.findViewById<View>(R.id.endTimeContainer).setOnClickListener {
            TimePickerDialogFragment(6, 30) { hour, minute ->
                // Handle selected time
                val formatted = LocalTime.of(hour, minute)
                    .format(DateTimeFormatter.ofPattern("hh:mm a"))
                tvEndTime.text = formatted
            }.show(childFragmentManager, "TimePickerDialog")
           // showTimePicker(isStart = false)
        }

        view.findViewById<View>(R.id.btnSaveLog).setOnClickListener {
            logNap()
            dismiss()
        }
        view.findViewById<View>(R.id.btnClose).setOnClickListener {
            dismiss()
        }
    }

    private fun logNap() {
        val userId = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val source = "apple"
          val call = ApiClient.apiServiceFastApi.logNap(userId, source, LogNapRequest(sleep_time = tvStartTime.text.toString(), wakeup_time = tvEndTime.text.toString(), required_sleep_duration = tvDuration.text.toString(), set_reminder = 0, reminder_value = "test"))
        call.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                if (response.isSuccessful) {
                      //  Toast.makeText(requireContext(), "Log Saved Successfully!", Toast.LENGTH_SHORT).show()

                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                   // Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
              //  Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateDuration() {
        val start = startTime.atDate(selectedDate)
        val end = if (endTime < startTime) endTime.atDate(selectedDate.plusDays(1)) else endTime.atDate(selectedDate)
        val duration = Duration.between(start, end)
        val hours = duration.toHours()
        val minutes = duration.minusHours(hours).toMinutes()

        tvStartTime.text = startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        tvEndTime.text = endTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        tvDuration.text = "$hours hr $minutes mins"
        tvDate.text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
    }

    private fun showTimePicker(isStart: Boolean) {
        val initial = if (isStart) startTime else endTime
        val timePicker = TimePickerDialog(
            requireContext(),
            { _, hour, minute ->
                if (isStart) startTime = LocalTime.of(hour, minute)
                else endTime = LocalTime.of(hour, minute)
                updateDuration()
            },
            initial.hour,
            initial.minute,
            false
        )
        timePicker.show()
    }

    private fun showDatePicker() {
        val datePicker = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                selectedDate = LocalDate.of(year, month + 1, day)
                updateDuration()
            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
        datePicker.show()
    }
}