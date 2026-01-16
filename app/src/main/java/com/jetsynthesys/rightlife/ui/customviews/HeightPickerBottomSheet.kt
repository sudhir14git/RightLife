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
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager

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
        fun newInstance(height: String = "5'7\"", unit: String = "ft"): HeightPickerBottomSheet {
            val fragment = HeightPickerBottomSheet()
            val args = Bundle()
            args.putString("height", height)
            args.putString("unit", unit)
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
            val bottomSheet = (dialogInterface as? com.google.android.material.bottomsheet.BottomSheetDialog)
                ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
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
        val gender = SharedPreferenceManager
            .getInstance(requireContext())
            .onboardingQuestionRequest
            .gender

        if (gender == "Male" || gender == "M") {
            defaultTotalInches = 68 // 5 ft 8 in
            defaultCm = 173
        } else {
            defaultTotalInches = 64 // 5 ft 4 in
            defaultCm = 163
        }

        // ✅ First open should land on gender default in FEET
        currentUnit = HeightUnit.FEET_INCHES
        currentTotalInches = defaultTotalInches
        currentCm = defaultCm

        setupUnitButtons()
        setupPicker()
        updateButtonStates()
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
            pickerView.updateForFeetInches()
            pickerView.requestLayout()

            pickerView.post {
                scrollView.overScrollMode = View.OVER_SCROLL_NEVER

                // ✅ Prevent listener from overriding default during first layout
                isInitializing = true
                scrollToInches(defaultTotalInches)

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

                // ✅ REVERSED picker mapping: top = max, bottom = min
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

                // ✅ REVERSED picker mapping
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
        val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2f).toInt()
        scrollView.scrollTo(0, targetScroll)
    }

    private fun scrollToCm(cm: Int) {
        val maxCm = 220
        val index = maxCm - cm // ✅ reversed
        val screenHeight = scrollView.height
        val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2f).toInt()
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

/*package com.jetsynthesys.rightlife.ui.customviews
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.customviews.HeightPickerView
import kotlin.text.toFloat

class HeightPickerBottomSheet : BottomSheetDialogFragment() {

    private lateinit var heightTextFeet: TextView
    private lateinit var heightTextInches: TextView
    private lateinit var heightTextCm: TextView
    private lateinit var scrollView: ScrollView
    private lateinit var pickerView: HeightPickerView
    private lateinit var btnFeet: Button
    private lateinit var btnCm: Button
    private lateinit var btnSave: Button
    private lateinit var btn_confirm: Button
    private lateinit var feetOption: TextView
    private lateinit var cmsOption: TextView

    private enum class HeightUnit { FEET_INCHES, CM }

    private var currentUnit = HeightUnit.FEET_INCHES
    private var currentTotalInches = 69
    private var currentCm = 178

    private val itemSpacing = 30f

    private var onHeightSelected: ((String, String) -> Unit)? = null

    companion object {
        fun newInstance(height: String = "5'7\"", unit: String = "ft"): HeightPickerBottomSheet {
            val fragment = HeightPickerBottomSheet()
            val args = Bundle()
            args.putString("height", height)
            args.putString("unit", unit)
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
            val bottomSheet = (dialogInterface as? com.google.android.material.bottomsheet.BottomSheetDialog)
                ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = true
            }
        }

        heightTextFeet = view.findViewById(R.id.heightTextFeet)
        heightTextInches = view.findViewById(R.id.heightTextInches)
        heightTextCm = view.findViewById(R.id.heightTextCm)
        scrollView = view.findViewById(R.id.scrollView)
        pickerView = view.findViewById(R.id.pickerView)
        btnSave = view.findViewById(R.id.btnSave)
        btn_confirm = view.findViewById(R.id.btn_confirm)
        feetOption = view.findViewById(R.id.feetOption)
        cmsOption = view.findViewById(R.id.cmsOption)

        setupUnitButtons()
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
        updateButtonStates()

        feetOption.setOnClickListener {
            if (currentUnit != HeightUnit.FEET_INCHES) {
                currentUnit = HeightUnit.FEET_INCHES
                updateButtonStates()
                switchToUnit()
            }
        }

        cmsOption.setOnClickListener {
            if (currentUnit != HeightUnit.CM) {
                currentUnit = HeightUnit.CM
                updateButtonStates()
                switchToUnit()
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
            pickerView.updateForFeetInches()
            pickerView.requestLayout()

            pickerView.post {
                scrollToInches(currentTotalInches)
                scrollView.overScrollMode = View.OVER_SCROLL_NEVER

            }
        }


        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            val screenHeight = scrollView.height
            val centerY = scrollY + screenHeight / 2f

            if (currentUnit == HeightUnit.FEET_INCHES) {
                val minInches = 4 * 12
                val maxInches = 7 * 12
                val totalItems = (maxInches - minInches) + 1

                val minCenterY = pickerView.sidePadding.toFloat()
                val maxCenterY = pickerView.sidePadding + (totalItems - 1) * itemSpacing

                // Clamp so indicator never goes into padding area
                val clampedCenterY = clampFloat(centerY, minCenterY, maxCenterY)

                // index from top (0 at top). Because picker is REVERSED, top = MAX value
                val indexFromTop = ((clampedCenterY - pickerView.sidePadding) / itemSpacing).toInt()

                // ✅ REVERSED mapping: top = max, bottom = min
                val totalInches = maxInches - indexFromTop

                if (totalInches != currentTotalInches) {
                    currentTotalInches = totalInches
                    updateHeightDisplay()
                }

                // Pull back if user flings beyond limits
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

                val indexFromTop = ((clampedCenterY - pickerView.sidePadding) / itemSpacing).toInt()

                // ✅ REVERSED mapping
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


        *//*  scrollView.viewTreeObserver.addOnScrollChangedListener {
              val scrollY = scrollView.scrollY
              val screenHeight = scrollView.height
              val centerY = scrollY + screenHeight / 2

              if (currentUnit == HeightUnit.FEET_INCHES) {
                  val maxInches = 7 * 12
                  // REVERSED CALCULATION
                  val totalInches = maxInches - ((centerY - pickerView.sidePadding) / itemSpacing).toInt()

                  if (totalInches in (4 * 12)..(7 * 12) && totalInches != currentTotalInches) {
                      currentTotalInches = totalInches
                      updateHeightDisplay()
                  }
              } else {
                  val maxCm = 220
                  // REVERSED CALCULATION
                  val cm = maxCm - ((centerY - pickerView.sidePadding) / itemSpacing).toInt()

                  if (cm in 120..220 && cm != currentCm) {
                      currentCm = cm
                      updateHeightDisplay()
                  }
              }
          }*//*

        scrollView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }
                MotionEvent.ACTION_UP -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    snapToNearest()
                }
                MotionEvent.ACTION_CANCEL -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }

    private fun switchToUnit() {
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
                    scrollToInches(currentTotalInches)
                } else {
                    scrollToCm(currentCm)
                }
            }
        }

        updateHeightDisplay()
    }

    private fun scrollToInches(totalInches: Int) {
        val maxInches = 7 * 12
        // REVERSED CALCULATION
        val index = maxInches - totalInches

        val screenHeight = scrollView.height
        val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2).toInt()
        scrollView.scrollTo(0, targetScroll)
    }

    private fun scrollToCm(cm: Int) {
        val maxCm = 220
        // REVERSED CALCULATION
        val index = maxCm - cm

        val screenHeight = scrollView.height
        val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2).toInt()
        scrollView.scrollTo(0, targetScroll)
    }

    *//*private fun snapToNearest() {
        scrollView.post {
            val scrollY = scrollView.scrollY
            val screenHeight = scrollView.height
            val centerY = scrollY + screenHeight / 2

            val index = Math.round((centerY - pickerView.sidePadding) / itemSpacing)
            val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2).toInt()

            scrollView.smoothScrollTo(0, targetScroll)
        }
    }*//*
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
}*/

