package com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.NumberPicker
import android.widget.Toast
import com.jetsynthesys.rightlife.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.WakeupTimeResponse
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter


class WakeUpTimeDialogFragment(private val context: Context, private val wakeupTime: String, private val recordId: String, private val listener: OnWakeUpTimeSelectedListener
) : BottomSheetDialogFragment() {

    private val handler = Handler(Looper.getMainLooper())

    private val mContext = context
    private val mWakeupTime = wakeupTime
    private val mRecordId = recordId
    private var mHour = 0
    private var mMinute = 0
    private var mAmPm = ""
    private lateinit var hourPicker : com.shawnlin.numberpicker.NumberPicker
    private lateinit var minutePicker : com.shawnlin.numberpicker.NumberPicker
    private lateinit var amPmPicker : com.shawnlin.numberpicker.NumberPicker

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.wakeup_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        hourPicker = view.findViewById<com.shawnlin.numberpicker.NumberPicker>(R.id.hourPickerWake)
        minutePicker = view.findViewById<com.shawnlin.numberpicker.NumberPicker>(R.id.minutePickerWake)
        amPmPicker = view.findViewById<com.shawnlin.numberpicker.NumberPicker>(R.id.amPmPickerWake)
        val imgClose = view.findViewById<ImageView>(R.id.img_close)

        mHour = getHourFromIso(mWakeupTime)
        mMinute = getMinuteFromIso(mWakeupTime)
        mAmPm = getAmPmFromIso(mWakeupTime)


        hourPicker.minValue = 1
        hourPicker.maxValue = 12
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        amPmPicker.minValue = 0
        amPmPicker.maxValue = 1
        amPmPicker.displayedValues = arrayOf("AM", "PM")
        hourPicker.value = mHour
        minutePicker.value = mMinute


        if (mAmPm == "AM"){
            amPmPicker.value = 0
        }else{
            amPmPicker.value = 1
        }

        imgClose.setOnClickListener {
            dismiss()
        }

        val btnSendData = view.findViewById<LinearLayout>(R.id.btn_confirm)
        btnSendData.setOnClickListener {
            var str1 = hourPicker.value.toString()
            var str2 = ":"+minutePicker.value.toString()
            var str3 = " "+amPmPicker.displayedValues[amPmPicker.value]
            val result = "$str1$str2$str3"
            updateWakeupTime(result)
            listener.onWakeUpTimeSelected(result)
            dismiss()
        }
         fun setNumberPickerTextSize(picker: NumberPicker, textSizeSp: Float) {
            try {
                val numberPickerFields = NumberPicker::class.java.declaredFields
                for (field in numberPickerFields) {
                    if (field.name == "mSelectorWheelPaint") {
                        field.isAccessible = true
                        val paint = field.get(picker) as Paint
                        paint.textSize = textSizeSp * picker.resources.displayMetrics.scaledDensity
                    }
                }

                for (i in 0 until picker.childCount) {
                    val child = picker.getChildAt(i)
                    if (child is EditText) {
                        child.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)
                        child.invalidate()
                    }
                }

                picker.invalidate()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }


        fun updateNumberPickerText(picker: com.shawnlin.numberpicker.NumberPicker) {
            picker.apply {
                textColor = Color.GRAY
                selectedTextColor = resources.getColor(R.color.sleep_duration_blue)
                textSize = 35f
                selectedTextSize = 45f
                typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                dividerColor = Color.TRANSPARENT
            }

            try {
                for (i in 0 until picker.childCount) {
                    val child = picker.getChildAt(i)
                    if (child is EditText) {
                        child.typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        fun refreshPickers() {
            handler.post {
                updateNumberPickerText(hourPicker)
                updateNumberPickerText(minutePicker)
                updateNumberPickerText(amPmPicker)
            }
        }


        val listener = com.shawnlin.numberpicker.NumberPicker.OnValueChangeListener { _, _, _ ->
            updateNumberPickerText(hourPicker)
            updateNumberPickerText(minutePicker)
            updateNumberPickerText(amPmPicker)
        }
        hourPicker.setOnValueChangedListener(listener)
        minutePicker.setOnValueChangedListener(listener)
        amPmPicker.setOnValueChangedListener(listener)
        refreshPickers()
    }

    fun parseIsoDateTime(isoDateTime: String): LocalDateTime {
        return LocalDateTime.parse(isoDateTime, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }


    fun getHourFromIso(isoDateTime: String): Int {
        val dateTime = parseIsoDateTime(isoDateTime)
        val hour = dateTime.hour % 12
        return if (hour == 0) 12 else hour
    }


    fun getMinuteFromIso(isoDateTime: String): Int {
        val dateTime = parseIsoDateTime(isoDateTime)
        return dateTime.minute
    }


    fun getAmPmFromIso(isoDateTime: String): String {
        val dateTime = parseIsoDateTime(isoDateTime)
        return if (dateTime.hour < 12) "AM" else "PM"
    }
    private fun setNumberPickerTextSize(picker: NumberPicker, textSizeSp: Float) {
        try {
            val numberPickerFields = NumberPicker::class.java.declaredFields
            for (field in numberPickerFields) {
                if (field.name == "mSelectorWheelPaint") {
                    field.isAccessible = true
                    val paint = field.get(picker) as Paint
                    paint.textSize = textSizeSp * picker.resources.displayMetrics.scaledDensity
                }
            }


            for (i in 0 until picker.childCount) {
                val child = picker.getChildAt(i)
                if (child is EditText) {
                    child.setTextSize(TypedValue.COMPLEX_UNIT_SP, textSizeSp)
                    child.invalidate()
                }
            }

            picker.invalidate()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun updateWakeupTime(result: String) {
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId ?: "68010b615a508d0cfd6ac9ca"
        val source = "android"
        val call = ApiClient.apiServiceFastApi.updateWakeupTime(userId, source, record_id =  mRecordId, timer_value = result )
        call.enqueue(object : Callback<WakeupTimeResponse> {
            override fun onResponse(call: Call<WakeupTimeResponse>, response: Response<WakeupTimeResponse>) {
                if (response.isSuccessful) {

                      Toast.makeText(mContext, "Log Saved Successfully!", Toast.LENGTH_SHORT).show()

                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                     Toast.makeText(mContext, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<WakeupTimeResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                  Toast.makeText(mContext, "Failure", Toast.LENGTH_SHORT).show()
            }
        })
    }
}