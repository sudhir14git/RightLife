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
import com.jetsynthesys.rightlife.ui.customviews.WeightPickerView

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
    private var currentWeightKg = 60.0
    private var currentWeightLbs = 132.0

    private var initialWeightKg = 60.0
    private var initialWeightLbs = 132.0
    
    private val kgMin = 30.0
    private val kgMax = 200.0
    private val lbsMin = 66.0
    private val lbsMax = 440.0
    private val itemSpacing = 20f
    
    private var onWeightSelected: ((Double, String) -> Unit)? = null
    
    companion object {
        fun newInstance(initialWeight: Double = 60.0, unit: String = "kg"): WeightPickerBottomSheet {
            val fragment = WeightPickerBottomSheet()
            val args = Bundle()
            args.putDouble("initialWeight", initialWeight)
            args.putString("unit", unit)
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
        
        arguments?.let {
            val initialWeight = it.getDouble("initialWeight", 60.0)
            val unit = it.getString("unit", "kg")
            
            if (unit == "kg") {
                currentUnit = WeightUnit.KG
                currentWeightKg = initialWeight
                initialWeightKg = initialWeight  // Store separately
            } else {
                currentUnit = WeightUnit.LBS
                currentWeightLbs = initialWeight
                initialWeightLbs = initialWeight  // Store separately
            }
        }
        
        setupUnitButtons()
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
        updateButtonStates()
        
        btnKg.setOnClickListener {
            if (currentUnit != WeightUnit.KG) {
                currentUnit = WeightUnit.KG
                updateButtonStates()
                switchToUnit()
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
            btnKg.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
            btnKg.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            btnLbs.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            btnLbs.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))

            kgOption.setBackgroundResource(R.drawable.bg_left_selected)
            kgOption.setTextColor(Color.WHITE)

            lbsOption.setBackgroundResource(R.drawable.bg_right_unselected)
            lbsOption.setTextColor(Color.BLACK)

        } else {
            btnLbs.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.holo_red_light))
            btnLbs.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
            btnKg.setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.transparent))
            btnKg.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.darker_gray))

            lbsOption.setBackgroundResource(R.drawable.bg_right_selected)
            lbsOption.setTextColor(Color.WHITE)

            kgOption.setBackgroundResource(R.drawable.bg_left_unselected)
            kgOption.setTextColor(Color.BLACK)
        }
    }

    private fun setupPicker() {
        scrollView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                scrollView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val screenWidth = scrollView.width
                pickerView.sidePadding = screenWidth / 2
                pickerView.itemSpacing = itemSpacing
                pickerView.updateForKg()
                pickerView.requestLayout()

                // Use postDelayed to ensure pickerView is fully measured
                scrollView.postDelayed({
                                           val initialWeight = if (currentUnit == WeightUnit.KG) initialWeightKg else initialWeightLbs
                                           scrollToWeight(initialWeight)
                                       }, 100)
            }
        })

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollX = scrollView.scrollX
            val screenWidth = scrollView.width
            val centerX = scrollX + screenWidth / 2

            val minWeight = getDisplayMinWeight()
            val weightInTenths = ((centerX - pickerView.sidePadding) / itemSpacing).toInt()
            val displayWeight = minWeight + (weightInTenths / 10.0)

            if (displayWeight >= getDisplayMinWeight() && displayWeight <= getDisplayMaxWeight()) {
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

    /*private fun setupPicker() {
        scrollView.post {
            val screenWidth = scrollView.width
            pickerView.sidePadding = screenWidth / 2
            pickerView.itemSpacing = itemSpacing
            pickerView.updateForKg()
            pickerView.requestLayout()
            
            pickerView.post {
                val initialWeight = if (currentUnit == WeightUnit.KG) currentWeightKg else currentWeightLbs
                scrollToWeight(initialWeight)
            }
        }
        
        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollX = scrollView.scrollX
            val screenWidth = scrollView.width
            val centerX = scrollX + screenWidth / 2
            
            val minWeight = getDisplayMinWeight()
            val weightInTenths = ((centerX - pickerView.sidePadding) / itemSpacing).toInt()
            val displayWeight = minWeight + (weightInTenths / 10.0)
            
            if (displayWeight >= getDisplayMinWeight() && displayWeight <= getDisplayMaxWeight()) {
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
            }
        }
       *//*
        scrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                snapToNearestWeight()
            }
            false
        }*//*
        scrollView.setOnTouchListener { v, event ->
            // Prevent BottomSheet from intercepting scroll
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
    }*/
    
    private fun switchToUnit() {
        if (currentUnit == WeightUnit.KG) {
            pickerView.updateForKg()
        } else {
            pickerView.updateForLbs()
        }
        
        scrollView.post {
            val screenWidth = scrollView.width
            pickerView.sidePadding = screenWidth / 2
            pickerView.requestLayout()
            
            pickerView.post {
                val weight = if (currentUnit == WeightUnit.KG) currentWeightKg else currentWeightLbs
                scrollToWeight(weight)
            }
        }
        
        updateWeightDisplay()
    }
    
    private fun scrollToWeight(weight: Double) {
        val minWeight = getDisplayMinWeight()
        val weightInTenths = ((weight - minWeight) * 10).toInt()
        val screenWidth = scrollView.width
        val targetScroll = (pickerView.sidePadding + weightInTenths * itemSpacing - screenWidth / 2).toInt()


        // Debug logs
        android.util.Log.d("WeightPicker", "scrollToWeight: $weight")
        android.util.Log.d("WeightPicker", "sidePadding: ${pickerView.sidePadding}")
        android.util.Log.d("WeightPicker", "screenWidth: $screenWidth")
        android.util.Log.d("WeightPicker", "weightInTenths: $weightInTenths")
        android.util.Log.d("WeightPicker", "targetScroll: $targetScroll")


        scrollView.scrollTo(targetScroll, 0)
    }

    private fun snapToNearestWeight() {
        scrollView.post {
            val scrollX = scrollView.scrollX
            val screenWidth = scrollView.width
            val centerX = scrollX + screenWidth / 2
            
            val minWeight = getDisplayMinWeight()
            val weightInTenths = Math.round((centerX - pickerView.sidePadding) / itemSpacing)
            
            val targetScroll = (pickerView.sidePadding + weightInTenths * itemSpacing - screenWidth / 2).toInt()
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
}
