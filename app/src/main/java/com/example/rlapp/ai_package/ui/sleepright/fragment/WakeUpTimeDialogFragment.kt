package com.example.rlapp.ai_package.ui.sleepright.fragment

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.NumberPicker
import com.example.rlapp.R
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class WakeUpTimeDialogFragment : BottomSheetDialogFragment() {

    private val handler = Handler(Looper.getMainLooper())

    interface BottomSheetListener {
        fun onDataReceived(data: String)
    }

    private var listener: BottomSheetListener? = null

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return BottomSheetDialog(requireContext(), R.style.BottomSheetDialogTheme)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.wakeup_dialog_fragment, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val bottomSheet = view.parent as View
        bottomSheet.backgroundTintMode = PorterDuff.Mode.CLEAR
        bottomSheet.backgroundTintList = ColorStateList.valueOf(Color.TRANSPARENT)
        bottomSheet.setBackgroundColor(Color.TRANSPARENT)
        val hourPicker = view.findViewById<NumberPicker>(R.id.hourPickerWake)
        val minutePicker = view.findViewById<NumberPicker>(R.id.minutePickerWake)
        val amPmPicker = view.findViewById<NumberPicker>(R.id.amPmPickerWake)

        hourPicker.minValue = 1
        hourPicker.maxValue = 12
        minutePicker.minValue = 0
        minutePicker.maxValue = 59
        amPmPicker.minValue = 0
        amPmPicker.maxValue = 1
        amPmPicker.displayedValues = arrayOf("AM", "PM")

        val btnSendData = view.findViewById<LinearLayout>(R.id.btn_confirm)
        btnSendData.setOnClickListener {
            listener?.onDataReceived("10:00 AM")
            dismiss()
        }

        fun updateNumberPickerText(picker: NumberPicker) {
            try {
                val field = NumberPicker::class.java.getDeclaredField("mInputText")
                field.isAccessible = true
                val editText = field.get(picker) as EditText
                editText.setTextColor( resources.getColor(R.color.sleep_duration_blue))
                editText.textSize = 24f
                editText.typeface = Typeface.DEFAULT_BOLD
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

        val listener = NumberPicker.OnValueChangeListener { _, _, _ -> refreshPickers() }
        hourPicker.setOnValueChangedListener(listener)
        minutePicker.setOnValueChangedListener(listener)
        amPmPicker.setOnValueChangedListener(listener)
        refreshPickers()
    }
}