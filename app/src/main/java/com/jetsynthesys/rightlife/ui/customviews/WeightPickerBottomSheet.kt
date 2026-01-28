package com.jetsynthesys.rightlife.ui.customviews

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.TextView
import androidx.core.content.ContextCompat
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.jetsynthesys.rightlife.R

class WeightPickerBottomSheet : BottomSheetDialogFragment() {

    private lateinit var weightText: TextView
    private lateinit var scrollView: HorizontalScrollView
    private lateinit var pickerView: WeightPickerView
    private lateinit var btnKg: Button
    private lateinit var btnLbs: Button
    private lateinit var btnSave: Button
    private lateinit var kgOption: TextView
    private lateinit var lbsOption: TextView

    private enum class WeightUnit { KG, LBS }

    private var currentUnit = WeightUnit.KG
    private var currentWeightKg = 75.0
    private var currentWeightLbs = 165.0

    private var initialWeightKg = 75.0
    private var initialWeightLbs = 165.0

    private val kgMin = 30.0
    private val kgMax = 300.0
    private val lbsMin = 66.0
    private val lbsMax = 660.0
    private val itemSpacing = 20f

    // ✅ same idea as height: prevent listener overriding initial scroll / unit switch scroll
    private var isInitializing = true
    private var isSwitchingUnit = false

    private var onWeightSelected: ((Double, String) -> Unit)? = null

    companion object {
        fun newInstance(
            initialWeight: Double = 60.0,
            unit: String = "kg",
            gender: String = "Male"
        ): WeightPickerBottomSheet {
            val fragment = WeightPickerBottomSheet()
            val args = Bundle()
            args.putDouble("initialWeight", initialWeight)
            args.putString("unit", unit) // "kg" or "lbs"
            args.putString("gender", gender)
            fragment.arguments = args
            return fragment
        }
    }

    fun setOnWeightSelectedListener(listener: (Double, String) -> Unit) {
        onWeightSelected = listener
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.bottomsheet_weight_picker, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        weightText = view.findViewById(R.id.weightText)
        scrollView = view.findViewById(R.id.scrollView)
        pickerView = view.findViewById(R.id.pickerView)
        btnKg = view.findViewById(R.id.btnKg)
        btnLbs = view.findViewById(R.id.btnLbs)
        btnSave = view.findViewById(R.id.btnSave)
        kgOption = view.findViewById(R.id.kgOption)
        lbsOption = view.findViewById(R.id.lbsOption)

        // ✅ Minimal change: choose initial tab based on received unit
        arguments?.let {
            it.getDouble("initialWeight", 60.0)
            val unit = it.getString("unit", "kg") ?: "kg"
            val gender = it.getString("gender", "Male") ?: "Male"

            currentUnit = if (unit.equals("lbs", true)) {
                WeightUnit.LBS
            } else {
                WeightUnit.KG
            }
            if (gender == "Male" || gender == "M") {
                currentWeightLbs = 165.0
                initialWeightLbs = 165.0
                currentWeightKg = 75.0
                initialWeightKg = 75.0
            } else {
                currentWeightLbs = 121.0
                initialWeightLbs = 121.0
                currentWeightKg = 55.0
                initialWeightKg = 55.0
            }
        }

        setupUnitButtons()
        updateButtonStates()   // ✅ set UI first
        setupPicker()
        updateWeightDisplay()

        btnSave.setOnClickListener {
            val weight = if (currentUnit == WeightUnit.KG) currentWeightKg else currentWeightLbs
            val unit = if (currentUnit == WeightUnit.KG) "kg" else "lbs"
            onWeightSelected?.invoke(weight, unit)
            dismiss()
        }
    }

    private fun setupUnitButtons() {
        btnKg.setOnClickListener {
            if (currentUnit != WeightUnit.KG) {
                currentUnit = WeightUnit.KG
                updateButtonStates()
                switchToUnit() // keep your existing behavior: show same numeric value for that unit (no forced reset)
            }
        }

        btnLbs.setOnClickListener {
            if (currentUnit != WeightUnit.LBS) {
                currentUnit = WeightUnit.LBS
                updateButtonStates()
                switchToUnit()
            }
        }

        kgOption.setOnClickListener {
            if (currentUnit != WeightUnit.KG) {
                currentUnit = WeightUnit.KG
                updateButtonStates()
                switchToUnit()
            }
        }

        lbsOption.setOnClickListener {
            if (currentUnit != WeightUnit.LBS) {
                currentUnit = WeightUnit.LBS
                updateButtonStates()
                switchToUnit()
            }
        }
    }

    private fun updateButtonStates() {
        if (currentUnit == WeightUnit.KG) {
            btnKg.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.holo_red_light
                )
            )
            btnKg.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            btnLbs.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )
            )
            btnLbs.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.darker_gray
                )
            )

            kgOption.setBackgroundResource(R.drawable.bg_left_selected)
            kgOption.setTextColor(Color.WHITE)

            lbsOption.setBackgroundResource(R.drawable.bg_right_unselected)
            lbsOption.setTextColor(Color.BLACK)
        } else {
            btnLbs.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.holo_red_light
                )
            )
            btnLbs.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            btnKg.setBackgroundColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.transparent
                )
            )
            btnKg.setTextColor(
                ContextCompat.getColor(
                    requireContext(),
                    android.R.color.darker_gray
                )
            )

            lbsOption.setBackgroundResource(R.drawable.bg_right_selected)
            lbsOption.setTextColor(Color.WHITE)

            kgOption.setBackgroundResource(R.drawable.bg_left_unselected)
            kgOption.setTextColor(Color.BLACK)
        }
    }

    private fun setupPicker() {
        scrollView.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                scrollView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val screenWidth = scrollView.width
                pickerView.sidePadding = screenWidth / 2
                pickerView.itemSpacing = itemSpacing

                if (currentUnit == WeightUnit.KG) pickerView.updateForKg() else pickerView.updateForLbs()
                pickerView.requestLayout()

                scrollView.post {
                    // ✅ prevent scroll listener from fighting the initial scroll
                    isInitializing = true

                    val initialWeight =
                        if (currentUnit == WeightUnit.KG) initialWeightKg else initialWeightLbs
                    scrollToWeight(initialWeight)

                    scrollView.post {
                        isInitializing = false
                        snapToNearestWeight()
                    }
                }
            }
        })

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            if (isInitializing || isSwitchingUnit) return@addOnScrollChangedListener

            val scrollX = scrollView.scrollX
            val screenWidth = scrollView.width
            val centerX = scrollX + screenWidth / 2f

            val minWeight = getDisplayMinWeight()
            val maxWeight = getDisplayMaxWeight()

            // Total ticks in tenths (0.1 steps)
            val maxIndex = Math.round(((maxWeight - minWeight) * 10).toFloat())

            val minCenterX = pickerView.sidePadding.toFloat()
            val maxCenterX = pickerView.sidePadding + maxIndex * itemSpacing

            // ✅ Clamp so indicator never goes beyond last/first tick (fast fling)
            val clampedCenterX = clampFloat(centerX, minCenterX, maxCenterX)

            val indexTenths = ((clampedCenterX - pickerView.sidePadding) / itemSpacing).toInt()
            val safeIndexTenths = clamp(indexTenths, 0, maxIndex)

            val displayWeight = minWeight + (safeIndexTenths / 10.0)

            if (currentUnit == WeightUnit.KG) {
                if (displayWeight != currentWeightKg) {
                    currentWeightKg = displayWeight
                    updateWeightDisplay()
                }
            } else {
                if (displayWeight != currentWeightLbs) {
                    currentWeightLbs = displayWeight
                    updateWeightDisplay()
                }
            }

            // ✅ If user flings beyond limits, pull back immediately
            if (centerX != clampedCenterX) {
                val targetScroll = (clampedCenterX - screenWidth / 2f).toInt()
                scrollView.scrollTo(targetScroll, 0)
            }
        }

        scrollView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                }

                MotionEvent.ACTION_UP -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                    snapToNearestWeight()
                }

                MotionEvent.ACTION_CANCEL -> {
                    v.parent.requestDisallowInterceptTouchEvent(false)
                }
            }
            false
        }
    }

    // ✅ Unit switch: just load that unit UI + scroll to that unit's "initialWeight" if available
    // (same pattern as height: respect received unit without making this complicated)
    private fun switchToUnit() {
        isSwitchingUnit = true

        if (currentUnit == WeightUnit.KG) pickerView.updateForKg() else pickerView.updateForLbs()

        scrollView.post {
            val screenWidth = scrollView.width
            pickerView.sidePadding = screenWidth / 2
            pickerView.requestLayout()

            pickerView.post {
                val weight =
                    if (currentUnit == WeightUnit.KG) initialWeightKg else initialWeightLbs
                scrollToWeight(weight)

                updateWeightDisplay()

                scrollView.post {
                    isSwitchingUnit = false
                    snapToNearestWeight()
                }
            }
        }
    }

    private fun scrollToWeight(weight: Double) {
        val minWeight = getDisplayMinWeight()
        val maxWeight = getDisplayMaxWeight()

        val maxIndex = Math.round(((maxWeight - minWeight) * 10).toFloat())
        val weightInTenths = Math.round(((weight - minWeight) * 10).toFloat())
        val safeTenths = clamp(weightInTenths, 0, maxIndex)

        val screenWidth = scrollView.width
        val targetScroll =
            (pickerView.sidePadding + safeTenths * itemSpacing - screenWidth / 2f).toInt()

        scrollView.scrollTo(targetScroll, 0)
    }

    private fun snapToNearestWeight() {
        scrollView.post {
            val scrollX = scrollView.scrollX
            val screenWidth = scrollView.width
            val centerX = scrollX + screenWidth / 2f

            val minWeight = getDisplayMinWeight()
            val maxWeight = getDisplayMaxWeight()
            val maxIndex = Math.round(((maxWeight - minWeight) * 10).toFloat())

            val indexFloat = (centerX - pickerView.sidePadding) / itemSpacing
            val indexRounded = Math.round(indexFloat)
            val index = clamp(indexRounded, 0, maxIndex)

            val targetScroll =
                (pickerView.sidePadding + index * itemSpacing - screenWidth / 2f).toInt()

            scrollView.smoothScrollTo(targetScroll, 0)
        }
    }

    private fun updateWeightDisplay() {
        val displayWeight = if (currentUnit == WeightUnit.KG) currentWeightKg else currentWeightLbs
        val unit = if (currentUnit == WeightUnit.KG) "kg" else "lbs"
        weightText.text = String.format("%.1f %s", displayWeight, unit)
    }

    private fun getDisplayMinWeight(): Double {
        return if (currentUnit == WeightUnit.KG) kgMin else lbsMin
    }

    private fun getDisplayMaxWeight(): Double {
        return if (currentUnit == WeightUnit.KG) kgMax else lbsMax
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
}