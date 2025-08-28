package com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment

import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.os.Bundle
import android.os.Handler
import android.os.Looper
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
import java.time.LocalDateTime
import java.time.OffsetDateTime
import java.time.ZoneId
import kotlin.math.min

class LogYourNapDialogFragment(private val requireContext: Context, private val listener: OnLogYourNapSelectedListener) : BottomSheetDialogFragment() {
    private lateinit var tvStartTime: TextView
    private lateinit var tvEndTime: TextView
    private lateinit var tvDuration: TextView
    private lateinit var tvDate: TextView
    private lateinit var tvRemindTime: TextView
    private val mContext = requireContext

    private var startTime: LocalTime = LocalTime.of(22, 0)
    private var endTime: LocalTime = LocalTime.of(7, 30)
    private var selectedDate: LocalDate = LocalDate.now()
    private var selectedWakeDate: LocalDate = LocalDate.now()
    private var selectedSleepDateTime : LocalDateTime = LocalDateTime.of(LocalDate.now().minusDays(1), LocalTime.of(22, 0))
    private var selectedWakeDateTime : LocalDateTime = LocalDateTime.of(LocalDate.now(), LocalTime.of(7, 30))
    var startHour = 22
    var startMinute = 0
    var endHour = 6
    var endMinute = 30

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
        tvRemindTime = view.findViewById(R.id.tvRemindTime)

      //  updateDuration()
        updateDurationNew()

        view.findViewById<View>(R.id.btnDatePicker).setOnClickListener {
          //  showDatePicker()
            showDatePicker(requireContext(),{day ->
              //  selectedSleepDateTime = day.withHour(22).withMinute(0)
                tvDate.text = day.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
               // selectedDate = day.format(DateTimeFormatter.ofPattern("dd MM yyyy"))
            })
        }

        view.findViewById<View>(R.id.startTimeContainer).setOnClickListener {
            /*if (SleepRightLandingFragment.dialogStartTime == LocalTime.of(startHour, startMinute)) {
                TimePickerDialogFragment(requireContext, startHour, startMinute, selectedDate, 0) { hour, minute ->
                    val formatted = LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("hh:mm a"))
                    startHour = hour
                    startMinute = minute
                    tvStartTime.text = formatted
                    startTime = LocalTime.of(hour, minute)
                    SleepRightLandingFragment.dialogStartTime = startTime
                    updateDuration()
                }.show(childFragmentManager, "TimePickerDialog")
            }else{
                TimePickerDialogFragment(requireContext, SleepRightLandingFragment.dialogStartTime.hour, SleepRightLandingFragment.dialogStartTime.minute, selectedDate, 0) { hour, minute ->
                    val formatted = LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("hh:mm a"))
                    startHour = hour
                    startMinute = minute
                    tvStartTime.text = formatted
                    startTime = LocalTime.of(hour, minute)
                    SleepRightLandingFragment.dialogStartTime = startTime
                    updateDuration()
                }.show(childFragmentManager, "TimePickerDialog")
            }*/
            showDateTimePicker(requireContext,{ date ->
                val mDate = date
                selectedSleepDateTime = date
                updateDurationNew()
            })
        }

        view.findViewById<View>(R.id.endTimeContainer).setOnClickListener {
            /*if (SleepRightLandingFragment.dialogEndTime == LocalTime.of(endHour, endMinute)) {
                TimePickerDialogFragment(requireContext, endHour, endMinute, selectedWakeDate, 1) { hour, minute ->
                    val formatted = LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("hh:mm a"))
                    endHour = hour
                    endMinute = minute
                    tvEndTime.text = formatted
                    endTime = LocalTime.of(hour, minute)
                    SleepRightLandingFragment.dialogEndTime = endTime
                    updateDuration()
                }.show(childFragmentManager, "TimePickerDialog")
            }else{
                TimePickerDialogFragment(requireContext,
                    SleepRightLandingFragment.dialogEndTime.hour, SleepRightLandingFragment.dialogEndTime.minute, selectedWakeDate, 1) { hour, minute ->
                    val formatted = LocalTime.of(hour, minute).format(DateTimeFormatter.ofPattern("hh:mm a"))
                    endHour = hour
                    endMinute = minute
                    tvEndTime.text = formatted
                    endTime = LocalTime.of(hour, minute)
                    SleepRightLandingFragment.dialogEndTime = endTime
                    updateDuration()
                }.show(childFragmentManager, "TimePickerDialog")
            }*/
            showDateTimeWakePicker(requireContext,{ date ->
                val mDate = date
                selectedWakeDateTime = date
                updateDurationNew()
            })
        }

        view.findViewById<View>(R.id.btnSaveLog).setOnClickListener {
            val duration = tvDuration.text.toString()
            var hasNegative = false
            var totalDuration = 0

            val regex = Regex("-?\\d+")
            val matches = regex.findAll(duration).map { it.value.toInt() }.toList()

            for (number in matches) {
                if (number < 0) {
                    hasNegative = true
                }
                totalDuration += number
            }

            if (hasNegative) {
                Toast.makeText(context, "Sleep duration cannot be negative", Toast.LENGTH_SHORT).show()
            } else if (totalDuration == 0) {
                Toast.makeText(context, "Sleep duration cannot be 0", Toast.LENGTH_SHORT).show()
            } else {
                val sleepTime = tvStartTime.text.toString()
                val inputFmt  = DateTimeFormatter.ofPattern("hh:mm a")
                val outputFmt = DateTimeFormatter.ofPattern("HH:mm:ss")
                val time24h = LocalTime.parse(sleepTime, inputFmt).format(outputFmt)
                // val formatStartDate = "$selectedDate"+"T"+"$time24h"
                val deviceZone  = ZoneId.systemDefault()          // phone / emulator zone
                //  val ldt         = LocalDateTime.parse(formatStartDate)      // parses without zone
                val instantStart     = selectedSleepDateTime.atZone(deviceZone).toInstant().toString()
                val wakeTime =  tvEndTime.text.toString()
                val inputFmt1  = DateTimeFormatter.ofPattern("hh:mm a")
                val outputFmt1 = DateTimeFormatter.ofPattern("HH:mm:ss")
                val time24h1 = LocalTime.parse(wakeTime, inputFmt1).format(outputFmt1)
                //   val formatWakeDate = "${SleepRightLandingFragment.dialogWakeDate}"+"T"+"$time24h1"
                //    val ldt1         = LocalDateTime.parse(formatWakeDate)      // parses without zone
                val instantEnd     = selectedWakeDateTime.atZone(deviceZone).toInstant().toString()
                val utcOdt   = OffsetDateTime.parse(instantStart)    // parse as an OffsetDateTime (UTC)
                val systemZone1: ZoneId = ZoneId.systemDefault()       // or ZoneId.systemDefault()
                val istOdt   = utcOdt.atZoneSameInstant(systemZone1)   // same instant, new zone
                val startSleepTime  = istOdt.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                val utcOdt1   = OffsetDateTime.parse(instantEnd)    // parse as an OffsetDateTime (UTC)
                val systemZone: ZoneId = ZoneId.systemDefault()     // or ZoneId.systemDefault()
                val istOdt1   = utcOdt1.atZoneSameInstant(systemZone)   // same instant, new zone
                val endSleepTime  = istOdt1.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME)
                logNap(startSleepTime,endSleepTime)
            }

        }
        view.findViewById<View>(R.id.btnClose).setOnClickListener {
            dismiss()
        }
    }

    private fun showDatePicker(context: Context, onDateSelected: (LocalDate) -> Unit) {
        val now = LocalDateTime.now()
        //  val today = LocalDate.now()
        //   val hour = 22
        //    val minute = "00"

        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                selectedDate = LocalDate.of(year, month + 1, dayOfMonth)
                onDateSelected(selectedDate)

            },
            selectedDate.year,
            selectedDate.monthValue - 1,
            selectedDate.dayOfMonth
        )
        val today1 = System.currentTimeMillis()
        val sevenDaysAgo = today1 - 6 * 24 * 60 * 60 * 1000L // 6 days ago in milliseconds
        datePickerDialog.datePicker.maxDate = today1
        datePickerDialog.datePicker.minDate = sevenDaysAgo
        datePickerDialog.show()
    }

    private fun showDateTimePicker(context: Context, onDateTimeSelected: (LocalDateTime) -> Unit) {
        val now = LocalDateTime.now()
      //  val today = LocalDate.now()
     //   val hour = 22
    //    val minute = "00"

        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)

                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val selectedDateTime = LocalDateTime.of(selectedDate, LocalTime.of(hourOfDay, minute))
                        // 🚫 Check if selected DateTime is in the future
                        if (selectedDateTime.isAfter(now)) {
                            Toast.makeText(context, "Cannot select a future time", Toast.LENGTH_SHORT).show()
                            // Optionally: reopen time picker with current time
                            Handler(Looper.getMainLooper()).post {
                                showTimePickerAgain(context, selectedDate, onDateTimeSelected)
                            }
                        } else {
                            onDateTimeSelected(selectedDateTime)
                        }
                    },
                    selectedSleepDateTime.hour,
                    selectedSleepDateTime.minute,
                    false
                )
                timePickerDialog.show()
            },
            selectedSleepDateTime.year,
            selectedSleepDateTime.monthValue - 1,
            selectedSleepDateTime.dayOfMonth
        )
        val today1 = System.currentTimeMillis()
        val sevenDaysAgo = today1 - 6 * 24 * 60 * 60 * 1000L // 6 days ago in milliseconds
        datePickerDialog.datePicker.maxDate = today1
        datePickerDialog.datePicker.minDate = sevenDaysAgo
        datePickerDialog.show()
    }

    private fun showTimePickerAgain(context: Context, selectedDate: LocalDate, onDateTimeSelected: (LocalDateTime) -> Unit) {
        val now = LocalDateTime.now()
        val hour = 22
        val minute = "00"
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val selectedDateTime = LocalDateTime.of(selectedDate, LocalTime.of(hourOfDay, minute))
                if (selectedDateTime.isAfter(now)) {
                    Toast.makeText(context, "Still future! Try again.", Toast.LENGTH_SHORT).show()
                } else {
                    onDateTimeSelected(selectedDateTime)
                }
            },
            hour,
            minute.toInt(),
            false
        )
        timePickerDialog.show()
    }

    private fun showDateTimeWakePicker(context: Context, onDateTimeSelected: (LocalDateTime) -> Unit) {
        val now = LocalDateTime.now()
        val today = LocalDate.now()
        val hour = "07"
        val minute = "30"

        val datePickerDialog = DatePickerDialog(
            context,
            { _, year, month, dayOfMonth ->
                val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)

                val timePickerDialog = TimePickerDialog(
                    context,
                    { _, hourOfDay, minute ->
                        val selectedDateTime = LocalDateTime.of(selectedDate, LocalTime.of(hourOfDay, minute))
                        // 🚫 Check if selected DateTime is in the future
                    //    if (selectedDateTime.isAfter(now)) {
                    //        Toast.makeText(context, "Cannot select a future time", Toast.LENGTH_SHORT).show()
                            // Optionally: reopen time picker with current time
                    //        Handler(Looper.getMainLooper()).post {
                    //            showTimePickerWakeAgain(context, selectedDate, onDateTimeSelected)
                    //        }
                    //    } else {
                            onDateTimeSelected(selectedDateTime)
                   //     }
                    },
                    hour.toInt(),
                    minute.toInt(),
                    false
                )
                timePickerDialog.show()
            },
            now.year,
            now.monthValue - 1,
            now.dayOfMonth
        )
        // 🚫 Disable future dates
        val today1 = System.currentTimeMillis()
        val sevenDaysAgo = today1 - 6 * 24 * 60 * 60 * 1000L // 6 days ago in milliseconds
        datePickerDialog.datePicker.maxDate = today1
        datePickerDialog.datePicker.minDate = sevenDaysAgo
        datePickerDialog.show()
    }

    private fun showTimePickerWakeAgain(
        context: Context,
        selectedDate: LocalDate,
        onDateTimeSelected: (LocalDateTime) -> Unit
    ) {
        val now = LocalDateTime.now()
        val timePickerDialog = TimePickerDialog(
            context,
            { _, hourOfDay, minute ->
                val selectedDateTime = LocalDateTime.of(selectedDate, LocalTime.of(hourOfDay, minute))
               // if (selectedDateTime.isAfter(now)) {
              //      Toast.makeText(context, "Still future! Try again.", Toast.LENGTH_SHORT).show()
             //   } else {
                    onDateTimeSelected(selectedDateTime)
             //   }
            },
            now.hour,
            now.minute,
            false
        )
        timePickerDialog.show()
    }

    private fun logNap(sleepTime: String, wakeTime: String) {
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val source = "android"
     //   val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
      //  val date = selectedDate.format(dateFormatter)
        val dateTime = OffsetDateTime.parse(wakeTime)
        val formattedDate = dateTime.toLocalDate().toString()
          val call = ApiClient.apiServiceFastApi.logNap(userId, source, formattedDate, LogNapRequest(sleepTime, wakeTime, set_reminder = 0, reminder_value = tvRemindTime.text.toString()))
        call.enqueue(object : Callback<BaseResponse> {
            override fun onResponse(call: Call<BaseResponse>, response: Response<BaseResponse>) {
                if (response.isSuccessful) {
                    Toast.makeText(mContext, "Log Saved Successfully!", Toast.LENGTH_SHORT).show()
                    listener.onLogTimeSelected("OK")
                    dismiss()

                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<BaseResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(mContext, "Failure", Toast.LENGTH_SHORT).show()
            }
        })
    }

    fun getTodayDate(): LocalDate {
        return LocalDate.now()
    }

    private fun updateDuration() {
        val start = startTime.atDate(selectedDate)
        val end = if (endTime < startTime) endTime.atDate(selectedDate.plusDays(1)) else endTime.atDate(selectedDate)
        var duration = Duration.between(start, end)
        var hours = duration.toHours()
        var minutes = duration.minusHours(hours).toMinutes()

        if (hours <= 15) {
            if (startTime == SleepRightLandingFragment.dialogStartTime){
                tvStartTime.text = startTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
            }else{
                 duration = Duration.between(SleepRightLandingFragment.dialogStartTime, LocalTime.of(end.hour,end.minute))
                 hours = duration.toHours()
                 minutes = duration.minusHours(hours).toMinutes()
                tvStartTime.text = SleepRightLandingFragment.dialogStartTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
            }
            if (endTime == SleepRightLandingFragment.dialogEndTime) {
                tvEndTime.text = endTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
            }else{
                duration = Duration.between(LocalTime.of(start.hour,start.minute), SleepRightLandingFragment.dialogEndTime)
                hours = duration.toHours()
                minutes = duration.minusHours(hours).toMinutes()
                tvEndTime.text = SleepRightLandingFragment.dialogEndTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
            }
            tvDuration.text = "$hours hr $minutes mins"
            if (SleepRightLandingFragment.dialogSleepDate == selectedDate) {
                tvDate.text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            }else{
                tvDate.text = SleepRightLandingFragment.dialogSleepDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
            }
        }else{
            Toast.makeText(requireContext,"Sleep time cannot be more than 15 hours", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateDurationNew() {
        tvDate.text = selectedDate.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
        tvStartTime.text = selectedSleepDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        tvEndTime.text = selectedWakeDateTime.format(DateTimeFormatter.ofPattern("hh:mm a"))
        var duration = Duration.between(selectedSleepDateTime, selectedWakeDateTime)
        var hours = duration.toHours()
        var minutes = duration.minusHours(hours).toMinutes()
        if (duration <= Duration.ofHours(15)) {
            tvDuration.text = "$hours hr $minutes mins"
        }else{
            Toast.makeText(requireContext,"Sleep time cannot be more than 15 hours", Toast.LENGTH_SHORT).show()
        }
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
                SleepRightLandingFragment.dialogSleepDate = selectedDate
                updateDuration()
            },
            SleepRightLandingFragment.dialogSleepDate.year,
            SleepRightLandingFragment.dialogSleepDate.monthValue - 1,
            SleepRightLandingFragment.dialogSleepDate.dayOfMonth
        )
        val today = System.currentTimeMillis()
        val sevenDaysAgo = today - 6 * 24 * 60 * 60 * 1000L // 6 days ago in milliseconds
        datePicker.datePicker.maxDate = today
        datePicker.datePicker.minDate = sevenDaysAgo
        datePicker.show()
    }
}