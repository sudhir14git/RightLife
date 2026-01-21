package com.jetsynthesys.rightlife.ui.new_design

import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.customviews.WeightPickerView
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class WeightSelectionFragment : Fragment() {

    private lateinit var llSelectedWeight: LinearLayout
    private lateinit var tvSelectedWeight: TextView
    private var selectedWeight = "50 kg"
    private var tvDescription: TextView? = null
    private var selected_number_text: TextView? = null
    private lateinit var cardViewSelection: CardView
    private lateinit var scrollView: HorizontalScrollView
    private lateinit var pickerView: WeightPickerView
    private lateinit var kgOption: TextView
    private lateinit var lbsOption: TextView

    private enum class WeightUnit { KG, LBS }
    private var currentUnit = WeightUnit.KG
    private var currentWeightKg = 75.0
    private var currentWeightLbs = 165.0
    private var initialWeightKg = 75.0
    private var initialWeightLbs = 165.0
    private val itemSpacing = 20f

    companion object {
        fun newInstance(pageIndex: Int): WeightSelectionFragment {
            val fragment = WeightSelectionFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        val gender = SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest.gender

        if (gender == "Male" || gender == "M") {
            currentWeightKg = 75.0
            currentWeightLbs = 165.0
            initialWeightKg = 75.0
            initialWeightLbs = 165.0
        } else {
            currentWeightKg = 55.0
            currentWeightLbs = 120.0
            initialWeightKg = 55.0
            initialWeightLbs = 120.0
        }

        kgOption.setBackgroundResource(R.drawable.bg_left_selected)
        kgOption.setTextColor(Color.WHITE)
        lbsOption.setBackgroundResource(R.drawable.bg_right_unselected)
        lbsOption.setTextColor(Color.BLACK)

        currentUnit = WeightUnit.KG
        updateWeightDisplay()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_weight_selection, container, false)

        llSelectedWeight = view.findViewById(R.id.ll_selected_weight)
        tvSelectedWeight = view.findViewById(R.id.tv_selected_weight)
        tvDescription = view.findViewById(R.id.tv_description)
        selected_number_text = view.findViewById(R.id.selected_number_text)
        cardViewSelection = view.findViewById(R.id.card_view_age_selector)
        scrollView = view.findViewById(R.id.scrollView)
        pickerView = view.findViewById(R.id.pickerView)
        kgOption = view.findViewById(R.id.kgOption)
        lbsOption = view.findViewById(R.id.lbsOption)

        val sharedPreferenceManager = SharedPreferenceManager.getInstance(requireContext())
        AnalyticsLogger.logEvent(
            AnalyticsEvent.WEIGHT_SELECTION_VISIT,
            mapOf(
                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule,
            )
        )

        val onboardingQuestionRequest = SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
        val gender = onboardingQuestionRequest.gender

        if (gender == "Male" || gender == "M") {
            currentWeightKg = 75.0
            currentWeightLbs = 165.0
            initialWeightKg = 75.0
            initialWeightLbs = 165.0
        } else {
            currentWeightKg = 55.0
            currentWeightLbs = 120.0
            initialWeightKg = 55.0
            initialWeightLbs = 120.0
        }

        setupUnitButtons()
        setupPicker()
        updateWeightDisplay()

        val btnContinue = view.findViewById<Button>(R.id.btn_continue)
        btnContinue.setOnClickListener {
            if (validateInput()) {
                btnContinue.disableViewForSeconds()
                onboardingQuestionRequest.weight = selectedWeight
                SharedPreferenceManager.getInstance(requireContext()).saveOnboardingQuestionAnswer(onboardingQuestionRequest)

                cardViewSelection.visibility = GONE
                llSelectedWeight.visibility = VISIBLE
                tvSelectedWeight.text = selectedWeight

                AnalyticsLogger.logEvent(
                    AnalyticsEvent.WEIGHT_SELECTION,
                    mapOf(
                        AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                        AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                        AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                        AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule,
                        AnalyticsParam.GENDER to onboardingQuestionRequest.gender!!,
                        AnalyticsParam.AGE to onboardingQuestionRequest.age!!,
                        AnalyticsParam.HEIGHT to onboardingQuestionRequest.height!!,
                        AnalyticsParam.WEIGHT to selectedWeight
                    )
                )

                Handler(Looper.getMainLooper()).postDelayed({
                                                                activity?.let { act ->
                                                                    if (act is OnboardingQuestionnaireActivity && isAdded) {
                                                                        act.submitAnswer(onboardingQuestionRequest)
                                                                    }
                                                                }
                                                            }, 500)
            }
        }

        return view
    }

    private fun setupUnitButtons() {
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

        updateButtonStates()
    }

    private fun updateButtonStates() {
        if (currentUnit == WeightUnit.KG) {
            kgOption.setBackgroundResource(R.drawable.bg_left_selected)
            kgOption.setTextColor(Color.WHITE)
            lbsOption.setBackgroundResource(R.drawable.bg_right_unselected)
            lbsOption.setTextColor(Color.BLACK)
        } else {
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

                scrollView.postDelayed({
                                           val initialWeight = if (currentUnit == WeightUnit.KG) initialWeightKg else initialWeightLbs
                                           scrollToWeight(initialWeight)
                                       }, 100)
            }
        })

        scrollView.viewTreeObserver.addOnScrollChangedListener {
            val scrollX = scrollView.scrollX
            val screenWidth = scrollView.width
            val centerX = scrollX + screenWidth / 2f

            val minWeight = getDisplayMinWeight()
            val maxWeight = getDisplayMaxWeight()

            // total ticks in "tenths"
            val maxIndex = Math.round(((maxWeight - minWeight) * 10).toFloat()) // e.g. 30..200 => 1700

            val minCenterX = pickerView.sidePadding.toFloat()
            val maxCenterX = pickerView.sidePadding + maxIndex * itemSpacing

            // ✅ Clamp center to valid tick area
            val clampedCenterX = clampFloat(centerX, minCenterX, maxCenterX)

            // ✅ index in tenths (0.1 steps) from left
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

            // ✅ If user flings beyond ends, pull back immediately (prevents indicator leaving scale)
            if (centerX != clampedCenterX) {
                val targetScroll = (clampedCenterX - screenWidth / 2f).toInt()
                scrollView.scrollTo(targetScroll, 0)
            }
        }

        /*scrollView.viewTreeObserver.addOnScrollChangedListener {
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
        }*/

        scrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                snapToNearestWeight()
            }
            false
        }
    }

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

            scrollView.post {
                val weight = if (currentUnit == WeightUnit.KG) initialWeightKg else initialWeightLbs
                scrollToWeight(weight)
            }
        }

        updateWeightDisplay()
    }

    private fun scrollToWeight(weight: Double) {
        val minWeight = getDisplayMinWeight()
        val weightInTenths = Math.round(((weight - minWeight) * 10).toFloat())
        val screenWidth = scrollView.width
        val targetScroll =
            (pickerView.sidePadding + weightInTenths * itemSpacing - screenWidth / 2f).toInt()
        scrollView.scrollTo(targetScroll, 0)
    }

    /*private fun scrollToWeight(weight: Double) {
        val minWeight = getDisplayMinWeight()
        val weightInTenths = ((weight - minWeight) * 10).toInt()
        val screenWidth = scrollView.width
        val targetScroll = (pickerView.sidePadding + weightInTenths * itemSpacing - screenWidth / 2).toInt()
        scrollView.scrollTo(targetScroll, 0)
    }*/
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

  /*  private fun snapToNearestWeight() {
        scrollView.post {
            val scrollX = scrollView.scrollX
            val screenWidth = scrollView.width
            val centerX = scrollX + screenWidth / 2

            val minWeight = getDisplayMinWeight()
            val weightInTenths = Math.round((centerX - pickerView.sidePadding) / itemSpacing)

            val targetScroll = (pickerView.sidePadding + weightInTenths * itemSpacing - screenWidth / 2).toInt()
            scrollView.smoothScrollTo(targetScroll, 0)
        }
    }*/

    private fun updateWeightDisplay() {
        val displayWeight = if (currentUnit == WeightUnit.KG) currentWeightKg else currentWeightLbs
        val unit = if (currentUnit == WeightUnit.KG) "kg" else "lbs"
        selectedWeight = String.format("%.1f %s", displayWeight, unit)
        selected_number_text?.text = selectedWeight
    }

    private fun validateInput(): Boolean {
        val weightValue = if (currentUnit == WeightUnit.KG) currentWeightKg else currentWeightLbs

        return if (currentUnit == WeightUnit.KG) {
            if (weightValue in 30.0..300.0) {
                true
            } else {
                Toast.makeText(requireContext(), "Please select weight between 30 kg and 300 kg", Toast.LENGTH_SHORT).show()
                false
            }
        } else {
            if (weightValue in 66.0..660.0) {
                true
            } else {
                Toast.makeText(requireContext(), "Please select weight between 66 lbs and 660 lbs", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    private fun getDisplayMinWeight(): Double {
        return if (currentUnit == WeightUnit.KG) 30.0 else 66.0
    }

    private fun getDisplayMaxWeight(): Double {
        return if (currentUnit == WeightUnit.KG) 200.0 else 440.0
    }

    override fun onPause() {
        super.onPause()
        cardViewSelection.visibility = VISIBLE
        llSelectedWeight.visibility = GONE
    }


    ///  fix of last line scroll issue

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
