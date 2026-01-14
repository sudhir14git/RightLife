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
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.customviews.HeightPickerView

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
    private var currentTotalInches = 67
    private var currentCm = 170

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

        // Fix: Expand BottomSheet fully, keep it draggable
        dialog?.setOnShowListener { dialogInterface ->
            val bottomSheet = (dialogInterface as? com.google.android.material.bottomsheet.BottomSheetDialog)
                ?.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let {
                val behavior = com.google.android.material.bottomsheet.BottomSheetBehavior.from(it)
                behavior.state = com.google.android.material.bottomsheet.BottomSheetBehavior.STATE_EXPANDED
                behavior.isDraggable = true // Keep draggable for empty areas
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

      /*  btnSave.setOnClickListener {
            val height = if (currentUnit == HeightUnit.FEET_INCHES) {
                val feet = currentTotalInches / 12
                val inches = currentTotalInches % 12
                "$feet'$inches\""
            } else {
                "$currentCm cm"
            }
            val unit = if (currentUnit == HeightUnit.FEET_INCHES) "ft" else "cm"
            onHeightSelected?.invoke(height, unit)
            dismiss()
        }*/
        btn_confirm.setOnClickListener {
            val height = if (currentUnit == HeightUnit.FEET_INCHES) {
                val feet = currentTotalInches / 12
                val inches = currentTotalInches % 12
                "$feet'$inches\""
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

      /*  btnFeet.setOnClickListener {
            if (currentUnit != HeightUnit.FEET_INCHES) {
                currentUnit = HeightUnit.FEET_INCHES
                updateButtonStates()
                switchToUnit()
            }
        }*/
        feetOption.setOnClickListener {
            if (currentUnit != HeightUnit.FEET_INCHES) {
                currentUnit = HeightUnit.FEET_INCHES
                updateButtonStates()
                switchToUnit()
            }
        }

      /*  btnCm.setOnClickListener {
            if (currentUnit != HeightUnit.CM) {
                currentUnit = HeightUnit.CM
                updateButtonStates()
                switchToUnit()
            }
        }*/

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
            feetOption.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
            feetOption.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            cmsOption.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            cmsOption.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))

            feetOption.setBackgroundResource(R.drawable.bg_left_selected)
            feetOption.setTextColor(Color.WHITE)

            cmsOption.setBackgroundResource(R.drawable.bg_right_unselected)
            cmsOption.setTextColor(Color.BLACK)

            heightTextFeet.visibility = TextView.VISIBLE
            heightTextInches.visibility = TextView.VISIBLE
            heightTextCm.visibility = TextView.GONE
        } else {
            cmsOption.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
            cmsOption.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            feetOption.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            feetOption.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))

            cmsOption.setBackgroundResource(R.drawable.bg_right_selected)
            cmsOption.setTextColor(Color.WHITE)

            feetOption.setBackgroundResource(R.drawable.bg_left_unselected)
            feetOption.setTextColor(Color.BLACK)


            heightTextFeet.visibility = TextView.GONE
            heightTextInches.visibility = TextView.GONE
            heightTextCm.visibility = TextView.VISIBLE
        }
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
            }
        }

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollY = scrollView.scrollY
            val screenHeight = scrollView.height
            val centerY = scrollY + screenHeight / 2

            if (currentUnit == HeightUnit.FEET_INCHES) {
                val minInches = 4 * 12
                val totalInches = minInches + ((centerY - pickerView.sidePadding) / itemSpacing).toInt()

                if (totalInches in (4 * 12)..(7 * 12) && totalInches != currentTotalInches) {
                    currentTotalInches = totalInches
                    updateHeightDisplay()
                }
            } else {
                val minCm = 120
                val cm = minCm + ((centerY - pickerView.sidePadding) / itemSpacing).toInt()

                if (cm in 120..215 && cm != currentCm) {
                    currentCm = cm
                    updateHeightDisplay()
                }
            }
        }

        scrollView.setOnTouchListener { v, event ->
            // Prevent BottomSheet from intercepting scroll
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
        val minInches = 4 * 12
        val index = totalInches - minInches
        val screenHeight = scrollView.height
        val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2).toInt()
        scrollView.scrollTo(0, targetScroll)
    }

    private fun scrollToCm(cm: Int) {
        val minCm = 120
        val index = cm - minCm
        val screenHeight = scrollView.height
        val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2).toInt()
        scrollView.scrollTo(0, targetScroll)
    }

    private fun snapToNearest() {
        scrollView.post {
            val scrollY = scrollView.scrollY
            val screenHeight = scrollView.height
            val centerY = scrollY + screenHeight / 2

            val index = Math.round((centerY - pickerView.sidePadding) / itemSpacing)
            val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2).toInt()

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
