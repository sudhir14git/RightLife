package com.jetsynthesys.rightlife.ui.customviews

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.R

class HeightPickerBottomSheet : BottomSheetDialogFragment() {

    private lateinit var heightTextFeet: TextView
    private lateinit var heightTextInches: TextView
    private lateinit var heightTextCm: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var pickerView: HeightPickerView
    private lateinit var btn_confirm: Button
    private lateinit var feetOption: TextView
    private lateinit var cmsOption: TextView

    private enum class HeightUnit { FEET_INCHES, CM }

    private var currentUnit = HeightUnit.FEET_INCHES
    private var currentTotalInches = 67
    private var currentCm = 170

    // Gender-based defaults
    private var defaultTotalInches = 68 // 5'8"
    private var defaultCm = 173

    private val itemSpacing = 30f

    private var isInitializing = true
    private var isSwitchingUnit = false

    private var onHeightSelected: ((String, String) -> Unit)? = null

    companion object {
        fun newInstance(
            height: String = "5'7\"",
            unit: String = "ft",
            gender: String = "Male"
        ): HeightPickerBottomSheet {
            val fragment = HeightPickerBottomSheet()
            val args = Bundle()
            args.putString("height", height)
            args.putString("unit", unit) // "ft" or "cm"
            args.putString("gender", gender)
            fragment.arguments = args
            return fragment
        }
    }

    fun setOnHeightSelectedListener(listener: (String, String) -> Unit) {
        onHeightSelected = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_height_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheet =
                (dialogInterface as? com.google.android.material.bottomsheet.BottomSheetDialog)
                    ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.state =
                    com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = true
            }
        }

        heightTextFeet = view.findViewById(R.id.heightTextFeet)
        heightTextInches = view.findViewById(R.id.heightTextInches)
        heightTextCm = view.findViewById(R.id.heightTextCm)
        scrollView = view.findViewById(R.id.scrollView)
        pickerView = view.findViewById(R.id.pickerView)
        btn_confirm = view.findViewById(R.id.btn_confirm)
        feetOption = view.findViewById(R.id.feetOption)
        cmsOption = view.findViewById(R.id.cmsOption)

        // ✅ Gender based defaults
        val gender = arguments?.getString("gender", "Male") ?: "Male"

        if (gender == "Male" || gender == "M") {
            defaultTotalInches = 68 // 5 ft 8 in
            defaultCm = 173
        } else {
            defaultTotalInches = 64 // 5 ft 4 in
            defaultCm = 163
        }

        // ✅ Always keep defaults set (NO conversion)
        currentTotalInches = defaultTotalInches
        currentCm = defaultCm

        // ✅ Minimal change: open tab based on received "unit" ("ft" / "cm")
        val unitArg = arguments?.getString("unit", "ft") ?: "ft"
        currentUnit = if (unitArg.equals("cm", true)) HeightUnit.CM else HeightUnit.FEET_INCHES

        setupUnitButtons()
        updateButtonStates()   // ✅ important: set tab UI before picker init
        setupPicker()
        updateHeightDisplay()

        btn_confirm.setOnClickListener {
            val height = if (currentUnit == HeightUnit.FEET_INCHES) {
                val feet = currentTotalInches / 12
                val inches = currentTotalInches % 12
                "$feet.$inches"
            } else {
                "$currentCm cm"
            }
            val unit = if (currentUnit == HeightUnit.FEET_INCHES) "ft" else "cm"
            onHeightSelected?.invoke(height, unit)
            dismiss()
        }
    }

    private fun setupUnitButtons() {
        feetOption.setOnClickListener {
            if (currentUnit != HeightUnit.FEET_INCHES) {
                currentUnit = HeightUnit.FEET_INCHES
                updateButtonStates()
                switchToUnitDefault()
            }
        }

        cmsOption.setOnClickListener {
            if (currentUnit != HeightUnit.CM) {
                currentUnit = HeightUnit.CM
                updateButtonStates()
                switchToUnitDefault()
            }
        }
    }

    private fun updateButtonStates() {
        if (currentUnit == HeightUnit.FEET_INCHES) {
            feetOption.setBackgroundResource(R.drawable.bg_left_selected)
            feetOption.setTextColor(Color.WHITE)
            cmsOption.setBackgroundResource(R.drawable.bg_right_unselected)
            cmsOption.setTextColor(Color.BLACK)

            heightTextFeet.visibility = TextView.VISIBLE
            heightTextInches.visibility = TextView.VISIBLE
            heightTextCm.visibility = TextView.GONE
        } else {
            cmsOption.setBackgroundResource(R.drawable.bg_right_selected)
            cmsOption.setTextColor(Color.WHITE)
            feetOption.setBackgroundResource(R.drawable.bg_left_unselected)
            feetOption.setTextColor(Color.BLACK)

            heightTextFeet.visibility = TextView.GONE
            heightTextInches.visibility = TextView.GONE
            heightTextCm.visibility = TextView.VISIBLE
        }
    }

    private fun clamp(v: Int, min: Int, max: Int): Int = when {
        v < min -> min
        v > max -> max
        else -> v
    }

    private fun clampFloat(v: Float, min: Float, max: Float): Float = when {
        v < min -> min
        v > max -> max
        else -> v
    }

    private fun setupPicker() {
        scrollView.post {
            val screenHeight = scrollView.height
            pickerView.sidePadding = screenHeight / 2
            pickerView.itemSpacing = itemSpacing

            // ✅ init picker according to currentUnit
            if (currentUnit == HeightUnit.FEET_INCHES) pickerView.updateForFeetInches()
            else pickerView.updateForCm()

            pickerView.requestLayout()

            pickerView.post {
                scrollView.overScrollMode = View.OVER_SCROLL_NEVER

                isInitializing = true
                // ✅ scroll to correct default according to currentUnit
                if (currentUnit == HeightUnit.FEET_INCHES) scrollToInches(defaultTotalInches)
                else scrollToCm(defaultCm)

                scrollView.post {
                    isInitializing = false
                    snapToNearest()
                }
            }
        }

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            if (isInitializing || isSwitchingUnit) return@addOnScrollChangedListener

            val scrollY = scrollView.scrollY
            val screenHeight = scrollView.height
            val centerY = scrollY + screenHeight / 2f

            if (currentUnit == HeightUnit.FEET_INCHES) {
                val minInches = 4 * 12
                val maxInches = 7 * 12
                val totalItems = (maxInches - minInches) + 1

                val minCenterY = pickerView.sidePadding.toFloat()
                val maxCenterY = pickerView.sidePadding + (totalItems - 1) * itemSpacing

                val clampedCenterY = clampFloat(centerY, minCenterY, maxCenterY)

                // ✅ REVERSED mapping: top = max, bottom = min
                val indexFromTop = ((clampedCenterY - pickerView.sidePadding) / itemSpacing).toInt()
                val totalInches = maxInches - indexFromTop

                if (totalInches != currentTotalInches) {
                    currentTotalInches = totalInches
                    updateHeightDisplay()
                }

                if (centerY != clampedCenterY) {
                    val targetScroll = (clampedCenterY - screenHeight / 2f).toInt()
                    scrollView.scrollTo(0, targetScroll)
                }
            } else {
                val minCm = 120
                val maxCm = 220
                val totalItems = (maxCm - minCm) + 1

                val minCenterY = pickerView.sidePadding.toFloat()
                val maxCenterY = pickerView.sidePadding + (totalItems - 1) * itemSpacing

                val clampedCenterY = clampFloat(centerY, minCenterY, maxCenterY)

                // ✅ REVERSED mapping
                val indexFromTop = ((clampedCenterY - pickerView.sidePadding) / itemSpacing).toInt()
                val cm = maxCm - indexFromTop

                if (cm != currentCm) {
                    currentCm = cm
                    updateHeightDisplay()
                }

                if (centerY != clampedCenterY) {
                    val targetScroll = (clampedCenterY - screenHeight / 2f).toInt()
                    scrollView.scrollTo(0, targetScroll)
                }
            }
        }

        scrollView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> v.parent.requestDisallowInterceptTouchEvent(true)
                MotionEvent.ACTION_UP -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    snapToNearest()
                }

                MotionEvent.ACTION_CANCEL -> v.parent.requestDisallowInterceptTouchEvent(false)
            }
            false
        }
    }

    // ✅ On unit switch: NO conversion, always jump to gender default
    private fun switchToUnitDefault() {
        isSwitchingUnit = true

        if (currentUnit == HeightUnit.FEET_INCHES) {
            pickerView.updateForFeetInches()
        } else {
            pickerView.updateForCm()
        }

        scrollView.post {
            val screenHeight = scrollView.height
            pickerView.sidePadding = screenHeight / 2
            pickerView.requestLayout()

            pickerView.post {
                if (currentUnit == HeightUnit.FEET_INCHES) {
                    currentTotalInches = defaultTotalInches
                    scrollToInches(defaultTotalInches)
                } else {
                    currentCm = defaultCm
                    scrollToCm(defaultCm)
                }

                updateHeightDisplay()

                scrollView.post {
                    isSwitchingUnit = false
                    snapToNearest()
                }
            }
        }
    }

    private fun scrollToInches(totalInches: Int) {
        val maxInches = 7 * 12
        val index = maxInches - totalInches // ✅ reversed
        val screenHeight = scrollView.height
        val targetScroll =
            (pickerView.sidePadding + index * itemSpacing - screenHeight / 2f).toInt()
        scrollView.scrollTo(0, targetScroll)
    }

    private fun scrollToCm(cm: Int) {
        val maxCm = 220
        val index = maxCm - cm // ✅ reversed
        val screenHeight = scrollView.height
        val targetScroll =
            (pickerView.sidePadding + index * itemSpacing - screenHeight / 2f).toInt()
        scrollView.scrollTo(0, targetScroll)
    }

    private fun snapToNearest() {
        scrollView.post {
            val scrollY = scrollView.scrollY
            val screenHeight = scrollView.height
            val centerY = scrollY + screenHeight / 2f

            val indexFloat = (centerY - pickerView.sidePadding) / itemSpacing
            val indexRounded = Math.round(indexFloat)

            val maxIndex = if (currentUnit == HeightUnit.FEET_INCHES) {
                ((7 * 12) - (4 * 12)) // 36
            } else {
                (220 - 120) // 100
            }

            val index = clamp(indexRounded, 0, maxIndex)

            val targetScroll =
                (pickerView.sidePadding + index * itemSpacing - screenHeight / 2f).toInt()

            scrollView.smoothScrollTo(0, targetScroll)
        }
    }

    private fun updateHeightDisplay() {
        if (currentUnit == HeightUnit.FEET_INCHES) {
            val feet = currentTotalInches / 12
            val inches = currentTotalInches % 12
            heightTextFeet.text = "$feet ft"
            heightTextInches.text = "$inches in"
        } else {
            heightTextCm.text = "$currentCm cm"
        }
    }
}