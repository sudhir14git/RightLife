package com.jetsynthesys.rightlife.ui.new_design

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.RelativeLayout
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
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import java.text.DecimalFormat

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
            "5 Ft 8 In"
        else
            "5 Ft 4 In"

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
        if (!(activity as OnboardingQuestionnaireActivity).forProfileChecklist) {
            (activity as OnboardingQuestionnaireActivity).tvSkip.visibility = VISIBLE
        }

        rulerView = view.findViewById(R.id.rulerView)
        val markerView = view.findViewById<View>(R.id.markerView)
        val rlRulerContainer = view.findViewById<RelativeLayout>(R.id.rl_ruler_container)
        val colorStateList = ContextCompat.getColorStateList(requireContext(), R.color.menuselected)

        val onboardingQuestionRequest =
            SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
        val gender = onboardingQuestionRequest.gender
        selectedHeight = if (gender == "Male" || gender == "M")
            "5 Ft 8 In"
        else
            "5 Ft 4 In"

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
                "5 Ft 8 In"
            else
                "5 Ft 4 In"
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

            selectedLabel = " cms"

            selectedHeight = if (gender == "Male" || gender == "M")
                "173 cms"
            else
                "163 cms"
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

                (activity as OnboardingQuestionnaireActivity).submitAnswer(onboardingQuestionRequest)
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
                                selected_number_text!!.text = "$ft Ft $remainingInches In"
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
            if (gender == "Male" || gender == "M") {
                rulerView.scrollToPosition(68)
            } else {
                rulerView.scrollToPosition(64)
            }
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
                    "Height should be in between 120 Cms to 220 cms",
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

}