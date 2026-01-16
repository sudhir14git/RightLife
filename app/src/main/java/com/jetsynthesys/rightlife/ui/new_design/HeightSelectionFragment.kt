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
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.customviews.HeightPickerView
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

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
    private var initialTotalInches = 68
    private var initialCm = 173
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
            initialTotalInches = 68
            initialCm = 173
        } else {
            currentTotalInches = 64
            currentCm = 163
            initialTotalInches = 64
            initialCm = 163
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
            initialTotalInches = 68
            initialCm = 173
        } else {
            currentTotalInches = 64
            currentCm = 163
            initialTotalInches = 64
            initialCm = 163
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
                                           scrollToInches(initialTotalInches)
                                           scrollView.overScrollMode = View.OVER_SCROLL_NEVER
                                       }, 500)
            }
        })

        /*scrollView.viewTreeObserver.addOnScrollChangedListener {
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
        }*/


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

                val clampedCenterY = clampFloat(centerY, minCenterY, maxCenterY)

                // index from TOP (0 at top)
                val indexFromTop = ((clampedCenterY - pickerView.sidePadding) / itemSpacing).toInt()

                // ✅ REVERSED mapping: top = max, bottom = min
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


        /*  scrollView.viewTreeObserver.addOnScrollChangedListener {
              val scrollY = scrollView.scrollY
              val screenHeight = scrollView.height
              val centerY = scrollY + screenHeight / 2

              if (currentUnit == HeightUnit.FEET_INCHES) {
                  val minInches = 4 * 12
                  val maxInches = 7 * 12

                  // REVERSED CALCULATION:
                  val totalInches = maxInches - ((centerY - pickerView.sidePadding) / itemSpacing).toInt()

                  if (totalInches in (4 * 12)..(7 * 12) && totalInches != currentTotalInches) {
                      currentTotalInches = totalInches
                      updateHeightDisplay()
                  }
              } else {
                  val minCm = 120
                  val maxCm = 220

                  // REVERSED CALCULATION:
                  val cm = maxCm - ((centerY - pickerView.sidePadding) / itemSpacing).toInt()

                  if (cm in 120..220 && cm != currentCm) {
                      currentCm = cm
                      updateHeightDisplay()
                  }
              }
          }*/

        scrollView.setOnTouchListener { _, event ->
            if (event.action == MotionEvent.ACTION_UP) {
                snapToNearest()
            }
            false
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
                    scrollToInches(initialTotalInches)
                } else {
                    scrollToCm(initialCm)
                }
            }
        }

        updateHeightDisplay()
    }
    private fun scrollToInches(totalInches: Int) {
        val minInches = 4 * 12
        val maxInches = 7 * 12

        // REVERSED CALCULATION:
        val index = maxInches - totalInches

        val screenHeight = scrollView.height
        val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2).toInt()
        scrollView.scrollTo(0, targetScroll)
    }
/*    private fun scrollToInches(totalInches: Int) {
        val minInches = 4 * 12
        val index = totalInches - minInches
        val screenHeight = scrollView.height
        val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2).toInt()
        scrollView.scrollTo(0, targetScroll)
    }*/


    private fun scrollToCm(cm: Int) {
        val minCm = 120
        val maxCm = 220

        // REVERSED CALCULATION:
        val index = maxCm - cm

        val screenHeight = scrollView.height
        val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2).toInt()
        scrollView.scrollTo(0, targetScroll)
    }
  /*  private fun scrollToCm(cm: Int) {
        val minCm = 120
        val index = cm - minCm
        val screenHeight = scrollView.height
        val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2).toInt()
        scrollView.scrollTo(0, targetScroll)
    }*/

   /* private fun snapToNearest() {
        scrollView.post {
            val scrollY = scrollView.scrollY
            val screenHeight = scrollView.height
            val centerY = scrollY + screenHeight / 2

            val index = Math.round((centerY - pickerView.sidePadding) / itemSpacing)
            val targetScroll = (pickerView.sidePadding + index * itemSpacing - screenHeight / 2).toInt()

            scrollView.smoothScrollTo(0, targetScroll)
        }
    }*/
   private fun snapToNearest() {
       scrollView.post {
           val scrollY = scrollView.scrollY
           val screenHeight = scrollView.height
           val centerY = scrollY + screenHeight / 2f

           val indexFloat = (centerY - pickerView.sidePadding) / itemSpacing
           val indexRounded = Math.round(indexFloat)

           val maxIndex = if (currentUnit == HeightUnit.FEET_INCHES) {
               ((7 * 12) - (4 * 12))
           } else {
               (220 - 120)
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