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
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.SwitchCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import java.text.DecimalFormat


import com.jetsynthesys.rightlife.ui.customviews.HeightPickerView


class HeightSelectionFragment : Fragment() {

    private lateinit var llSelectedHeight: LinearLayout
    private lateinit var tvSelectedHeight: TextView
    private var selectedHeight = "5 Ft 10 In"
    private var tvDescription: TextView? = null
    private var selected_number_text: TextView? = null
    private lateinit var cardViewSelection: CardView
    private lateinit var scrollView: ScrollView
    private lateinit var pickerView: HeightPickerView
    private lateinit var feetOption: TextView
    private lateinit var cmsOption: TextView

    private enum class HeightUnit { FEET_INCHES, CM }
    private var currentUnit = HeightUnit.FEET_INCHES
    private var currentTotalInches = 68
    private var currentCm = 173
    private val itemSpacing = 30f

    companion object {
        fun newInstance(pageIndex: Int): HeightSelectionFragment {
            val fragment = HeightSelectionFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        val gender = SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest.gender

        if (gender == "Male" || gender == "M") {
            currentTotalInches = 68
            currentCm = 173
        } else {
            currentTotalInches = 64
            currentCm = 163
        }

        feetOption.setBackgroundResource(R.drawable.bg_left_selected)
        feetOption.setTextColor(Color.WHITE)
        cmsOption.setBackgroundResource(R.drawable.bg_right_unselected)
        cmsOption.setTextColor(Color.BLACK)

        currentUnit = HeightUnit.FEET_INCHES
        updateHeightDisplay()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_height_selection, container, false)

        llSelectedHeight = view.findViewById(R.id.ll_selected_height)
        tvSelectedHeight = view.findViewById(R.id.tv_selected_height)
        tvDescription = view.findViewById(R.id.tv_description)
        selected_number_text = view.findViewById(R.id.selected_number_text)
        cardViewSelection = view.findViewById(R.id.card_view_age_selector)
        scrollView = view.findViewById(R.id.scrollView)
        pickerView = view.findViewById(R.id.pickerView)
        feetOption = view.findViewById(R.id.feetOption)
        cmsOption = view.findViewById(R.id.cmsOption)

        val sharedPreferenceManager = SharedPreferenceManager.getInstance(requireContext())
        AnalyticsLogger.logEvent(
            AnalyticsEvent.HEIGHT_SELECTION_VISIT,
            mapOf(
                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule
            )
        )

        val onboardingQuestionRequest = SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
        val gender = onboardingQuestionRequest.gender

        if (gender == "Male" || gender == "M") {
            currentTotalInches = 68
            currentCm = 173
        } else {
            currentTotalInches = 64
            currentCm = 163
        }

        setupUnitButtons()
        setupPicker()
        updateHeightDisplay()

        val btnContinue = view.findViewById<Button>(R.id.btn_continue)
        btnContinue.setOnClickListener {
            if (validateInput()) {
                btnContinue.disableViewForSeconds()
                onboardingQuestionRequest.height = selectedHeight
                SharedPreferenceManager.getInstance(requireContext()).saveOnboardingQuestionAnswer(onboardingQuestionRequest)

                cardViewSelection.visibility = GONE
                llSelectedHeight.visibility = VISIBLE
                tvSelectedHeight.text = selectedHeight

                AnalyticsLogger.logEvent(
                    AnalyticsEvent.HEIGHT_SELECTION,
                    mapOf(
                        AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                        AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                        AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                        AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule,
                        AnalyticsParam.GENDER to onboardingQuestionRequest.gender!!,
                        AnalyticsParam.AGE to onboardingQuestionRequest.age!!,
                        AnalyticsParam.HEIGHT to selectedHeight
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

        updateButtonStates()
    }

    private fun updateButtonStates() {
        if (currentUnit == HeightUnit.FEET_INCHES) {
            feetOption.setBackgroundResource(R.drawable.bg_left_selected)
            feetOption.setTextColor(Color.WHITE)
            cmsOption.setBackgroundResource(R.drawable.bg_right_unselected)
            cmsOption.setTextColor(Color.BLACK)
        } else {
            cmsOption.setBackgroundResource(R.drawable.bg_right_selected)
            cmsOption.setTextColor(Color.WHITE)
            feetOption.setBackgroundResource(R.drawable.bg_left_unselected)
            feetOption.setTextColor(Color.BLACK)
        }
    }

    private fun setupPicker() {
        scrollView.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                scrollView.viewTreeObserver.removeOnGlobalLayoutListener(this)

                val screenHeight = scrollView.height
                pickerView.sidePadding = screenHeight / 2
                pickerView.itemSpacing = itemSpacing
                pickerView.updateForFeetInches()
                pickerView.requestLayout()

                scrollView.postDelayed({
                                           scrollToInches(currentTotalInches)
                                       }, 100)
            }
        })

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

        scrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                snapToNearest()
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
            selectedHeight = "$feet ft $inches in"
        } else {
            selectedHeight = "$currentCm cm"
        }
        selected_number_text?.text = selectedHeight
    }

    private fun validateInput(): Boolean {
        return if (currentUnit == HeightUnit.FEET_INCHES) {
            val feet = currentTotalInches / 12
            if (feet in 4..7) {
                true
            } else {
                Toast.makeText(requireActivity(), "Height should be between 4 feet to 7 feet", Toast.LENGTH_SHORT).show()
                false
            }
        } else {
            if (currentCm in 120..220) {
                true
            } else {
                Toast.makeText(requireActivity(), "Height should be between 120 cm to 220 cm", Toast.LENGTH_SHORT).show()
                false
            }
        }
    }

    override fun onPause() {
        super.onPause()
        cardViewSelection.visibility = VISIBLE
        llSelectedHeight.visibility = GONE
    }
}


/*
class HeightSelectionFragment : Fragment() {

    private lateinit var llSelectedHeight: LinearLayout
    private lateinit var tvSelectedHeight: TextView
    private var selectedHeight = "5 Ft 10 In"
    private var tvDescription: TextView? = null
    private var selected_number_text: TextView? = null
    private lateinit var cardViewSelection: CardView
    private val numbers = mutableListOf<Float>()
    private lateinit var adapter: RulerAdapterVertical
    private var selectedLabel: String = " feet"
    private val decimalFormat = DecimalFormat("###.##")
    private lateinit var rulerView: RecyclerView
    private lateinit var feetOption: TextView
    private lateinit var cmsOption: TextView

    companion object {
        fun newInstance(pageIndex: Int): HeightSelectionFragment {
            val fragment = HeightSelectionFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        val gender =
            SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest.gender
        selectedHeight = if (gender == "Male" || gender == "M")
            "5 ft 8 in"
        else
            "5 ft 4 in"

        feetOption.setBackgroundResource(R.drawable.bg_left_selected)
        feetOption.setTextColor(Color.WHITE)

        cmsOption.setBackgroundResource(R.drawable.bg_right_unselected)
        cmsOption.setTextColor(Color.BLACK)
        setFtIn()

        selectedLabel = " feet"

        selected_number_text!!.text = selectedHeight
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_height_selection, container, false)

        llSelectedHeight = view.findViewById(R.id.ll_selected_height)
        tvSelectedHeight = view.findViewById(R.id.tv_selected_height)
        tvDescription = view.findViewById(R.id.tv_description)
        selected_number_text = view.findViewById(R.id.selected_number_text)
        cardViewSelection = view.findViewById(R.id.card_view_age_selector)
        */
/*if (!(activity as OnboardingQuestionnaireActivity).forProfileChecklist) {
            (activity as OnboardingQuestionnaireActivity).tvSkip.visibility = VISIBLE
        }*//*


        val sharedPreferenceManager = SharedPreferenceManager.getInstance(requireContext())
        AnalyticsLogger.logEvent(
            AnalyticsEvent.HEIGHT_SELECTION_VISIT,
            mapOf(
                AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule
            )
        )

        rulerView = view.findViewById(R.id.rulerView)
        val markerView = view.findViewById<View>(R.id.markerView)
        val rlRulerContainer = view.findViewById<RelativeLayout>(R.id.rl_ruler_container)
        val colorStateList = ContextCompat.getColorStateList(requireContext(), R.color.menuselected)

        val onboardingQuestionRequest =
            SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
        val gender = onboardingQuestionRequest.gender
        selectedHeight = if (gender == "Male" || gender == "M")
            "5 ft 8 in"
        else
            "5 ft 4 in"

        selected_number_text!!.text = selectedHeight

        val layoutManager = LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, true)
        rulerView.layoutManager = layoutManager

        // Generate numbers with increments of 0.1
        for (i in 0..250) {
            numbers.add(i * 1f) // Increment by 0.1  numbers.add(i * 1f)
        }

        feetOption = view.findViewById(R.id.feetOption)
        cmsOption = view.findViewById(R.id.cmsOption)

        feetOption.setOnClickListener {
            feetOption.setBackgroundResource(R.drawable.bg_left_selected)
            feetOption.setTextColor(Color.WHITE)

            cmsOption.setBackgroundResource(R.drawable.bg_right_unselected)
            cmsOption.setTextColor(Color.BLACK)

            selectedLabel = " feet"

            selectedHeight = if (gender == "Male" || gender == "M")
                "5 ft 8 in"
            else
                "5 ft 4 in"
            setFtIn()

            rulerView.post {
                if (gender == "Male" || gender == "M") {
                    rulerView.scrollToPosition(68)
                } else {
                    rulerView.scrollToPosition(64)
                }
            }
            selected_number_text!!.text = selectedHeight
        }

        cmsOption.setOnClickListener {
            cmsOption.setBackgroundResource(R.drawable.bg_right_selected)
            cmsOption.setTextColor(Color.WHITE)

            feetOption.setBackgroundResource(R.drawable.bg_left_unselected)
            feetOption.setTextColor(Color.BLACK)

            selectedLabel = " cm"

            selectedHeight = if (gender == "Male" || gender == "M")
                "173 cm"
            else
                "163 cm"
            setCms()

            rulerView.post {
                if (gender == "Male" || gender == "M") {
                    rulerView.scrollToPosition(173)
                } else {
                    rulerView.scrollToPosition(163)
                }
            }
            selected_number_text!!.text = selectedHeight
        }

        val btnContinue = view.findViewById<Button>(R.id.btn_continue)

        btnContinue.setOnClickListener {
            if (validateInput()) {
                btnContinue.disableViewForSeconds()
                val onboardingQuestionRequest =
                    SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
                onboardingQuestionRequest.height = selectedHeight
                SharedPreferenceManager.getInstance(requireContext())
                    .saveOnboardingQuestionAnswer(onboardingQuestionRequest)

                cardViewSelection.visibility = GONE
                llSelectedHeight.visibility = VISIBLE
                tvSelectedHeight.text = selectedHeight

                AnalyticsLogger.logEvent(
                    AnalyticsEvent.HEIGHT_SELECTION,
                    mapOf(
                        AnalyticsParam.USER_ID to sharedPreferenceManager.userId,
                        AnalyticsParam.TIMESTAMP to System.currentTimeMillis(),
                        AnalyticsParam.GOAL to sharedPreferenceManager.selectedOnboardingModule,
                        AnalyticsParam.SUB_GOAL to sharedPreferenceManager.selectedOnboardingSubModule,
                        AnalyticsParam.GENDER to onboardingQuestionRequest.gender!!,
                        AnalyticsParam.AGE to onboardingQuestionRequest.age!!,
                        AnalyticsParam.HEIGHT to selectedHeight
                    )
                )

                Handler(Looper.getMainLooper()).postDelayed({
                    // Add null safety check
                    activity?.let { act ->
                        if (act is OnboardingQuestionnaireActivity && isAdded) {
                            act.submitAnswer(onboardingQuestionRequest)
                        }
                    }
                }, 500)
            }
        }


        adapter = RulerAdapterVertical(numbers) { number ->
            // Handle the selected number
        }
        adapter.setType("feet")
        rulerView.adapter = adapter

        // Attach a LinearSnapHelper for center alignment
        val snapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(rulerView)

        // Add scroll listener to handle snapping logic
        rulerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    val snappedView = snapHelper.findSnapView(recyclerView.layoutManager)
                    if (snappedView != null) {
                        val position =
                            recyclerView.layoutManager?.getPosition(snappedView) ?: return
                        val snappedNumber = numbers[position]
                        if (selected_number_text != null) {
                            selected_number_text!!.text =
                                "${decimalFormat.format(snappedNumber)}$selectedLabel"
                            if (selectedLabel == " feet") {
                                val feet = decimalFormat.format(snappedNumber / 12)
                                val remainingInches = snappedNumber.toInt() % 12
                                val h = (feet).toString().split(".")
                                val ft = h[0]
                                var inch = "0"
                                if (h.size > 1) {
                                    inch = h[1]
                                }
                                selected_number_text!!.text = "$ft ft $remainingInches in"
                            }
                            selectedHeight = selected_number_text?.text.toString()
                            btnContinue.isEnabled = true
                            btnContinue.backgroundTintList = colorStateList
                        }

                    }
                }
            }
        })

        // Set vertical padding for the marker view programmatically
        rlRulerContainer.post {
            val parentHeight = rlRulerContainer.height
            val paddingVertical = parentHeight / 2
            rulerView.setPadding(
                rulerView.paddingLeft,
                paddingVertical,
                rulerView.paddingRight,
                paddingVertical
            )
        }

        rulerView.post {
            val layoutManager = rulerView.layoutManager as LinearLayoutManager
            val targetPosition = if (gender == "Male" || gender == "M") 68 else 64

            // Compute the vertical center offset
            val viewHeight = rulerView.height
            val itemView = layoutManager.findViewByPosition(targetPosition)
            val itemHeight = itemView?.height ?: 0
            val offset = (viewHeight / 2) - (itemHeight / 2)

            layoutManager.scrollToPositionWithOffset(targetPosition, offset)
        }


        return view
    }

    private fun validateInput(): Boolean {
        var returnValue: Boolean
        if (selectedLabel == " feet") {
            val h = selectedHeight.split(" ")
            val feet = "${h[0]}.${h[2]}".toDouble()
            if (feet in 4.0..7.0) {
                returnValue = true
            } else {
                returnValue = false
                Toast.makeText(
                    requireActivity(),
                    "Height should be in between 4 feet to 7 feet",
                    Toast.LENGTH_SHORT
                ).show()
            }
        } else {
            val w = selectedHeight.split(" ")
            val height = w[0].toDouble()
            if (height in 120.0..220.0) {
                returnValue = true
            } else {
                returnValue = false
                Toast.makeText(
                    requireActivity(),
                    "Height should be in between 120 cm to 220 cm",
                    Toast.LENGTH_SHORT
                ).show()
            }

        }
        return returnValue
    }

    private fun setCms() {
        numbers.clear()
        for (i in 0..250) {
            numbers.add(i * 1f) // Increment by 0.1  numbers.add(i * 1f)
        }
        adapter.setType("cms")
        adapter.notifyDataSetChanged()
    }

    private fun setFtIn() {
        numbers.clear()
        for (i in 0..250) {
            numbers.add(i * 1f) // Increment by 0.1  numbers.add(i * 1f)
        }
        adapter.setType("feet")
        adapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        cardViewSelection.visibility = VISIBLE
        llSelectedHeight.visibility = GONE
    }

}*/
