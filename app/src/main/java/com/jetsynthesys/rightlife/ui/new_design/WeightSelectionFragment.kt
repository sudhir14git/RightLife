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
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.utility.ConversionUtils
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import kotlin.math.floor


class WeightSelectionFragment : Fragment() {

    private lateinit var llSelectedWeight: LinearLayout
    private lateinit var tvSelectedWeight: TextView
    private var selectedWeight = "50 kg"
    private var tvDescription: TextView? = null
    private var selected_number_text: TextView? = null
    private lateinit var cardViewSelection: CardView
    private val numbers = mutableListOf<Float>()
    private lateinit var adapter: RulerAdapter
    private var selectedLabel: String = " kg"
    private lateinit var rulerView: RecyclerView
    private lateinit var kgOption: TextView
    private lateinit var lbsOption: TextView

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
        val gender =
            SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest.gender
        selectedWeight = if (gender == "Male" || gender == "M")
            "75 kg"
        else
            "55 kg"
        selected_number_text!!.text = selectedWeight

        kgOption.setBackgroundResource(R.drawable.bg_left_selected)
        kgOption.setTextColor(Color.WHITE)

        lbsOption.setBackgroundResource(R.drawable.bg_right_unselected)
        lbsOption.setTextColor(Color.BLACK)

        selectedLabel = " kg"
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
        cardViewSelection = view.findViewById(R.id.card_view_age_selector)
        if (!(activity as OnboardingQuestionnaireActivity).forProfileChecklist) {
            (activity as OnboardingQuestionnaireActivity).tvSkip.visibility = VISIBLE
        }

        kgOption = view.findViewById(R.id.kgOption)
        lbsOption = view.findViewById(R.id.lbsOption)

        //---------
        val recyclerView = view.findViewById<RecyclerView>(R.id.rulerView)
        selected_number_text = view.findViewById(R.id.selected_number_text)
        rulerView = view.findViewById(R.id.rulerView)
        val rlRulerContainer = view.findViewById<RelativeLayout>(R.id.rl_ruler_container)
        val colorStateList = ContextCompat.getColorStateList(requireContext(), R.color.menuselected)

        val onboardingQuestionRequest =
            SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
        val gender = onboardingQuestionRequest.gender

        selectedWeight = if (gender == "Male" || gender == "M")
            "75 kg"
        else
            "55 kg"

        selected_number_text!!.text = selectedWeight



        kgOption.setOnClickListener {
            kgOption.setBackgroundResource(R.drawable.bg_left_selected)
            kgOption.setTextColor(Color.WHITE)

            lbsOption.setBackgroundResource(R.drawable.bg_right_unselected)
            lbsOption.setTextColor(Color.BLACK)

            selectedLabel = " kg"
            selectedWeight = if (gender == "Male" || gender == "M")
                "75 kg"
            else
                "55 kg"
            setKgsValue()

            recyclerView.layoutManager?.scrollToPosition(if (gender == "Male" || gender == "M") 750 else 550)
            selected_number_text!!.text = selectedWeight
        }

        lbsOption.setOnClickListener {
            lbsOption.setBackgroundResource(R.drawable.bg_right_selected)
            lbsOption.setTextColor(Color.WHITE)

            kgOption.setBackgroundResource(R.drawable.bg_left_unselected)
            kgOption.setTextColor(Color.BLACK)

            selectedLabel = " lbs"
            selectedWeight = if (gender == "Male" || gender == "M")
            "165 lbs"
        else
            "120 lbs"
            setLbsValue()

            recyclerView.layoutManager?.scrollToPosition(if (gender == "Male" || gender == "M") 1650 else 1200)
            selected_number_text!!.text = selectedWeight
        }

        val btnContinue = view.findViewById<Button>(R.id.btn_continue)
        btnContinue.setOnClickListener {
            val w = selectedWeight.split(" ")
            if (selectedLabel == " kg") {
                if (w[0].toDouble() < 30) {
                    Toast.makeText(
                        requireContext(),
                        "Please select weight between 30 kg and 300 kg",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }

            if (selectedLabel == " lbs") {
                if (w[0].toDouble() < 66) {
                    Toast.makeText(
                        requireContext(),
                        "Please select weight between 66 lbs and 660 lbs",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setOnClickListener
                }
            }
            btnContinue.disableViewForSeconds()
            val onboardingQuestionRequest =
                SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
            onboardingQuestionRequest.weight = selectedWeight
            SharedPreferenceManager.getInstance(requireContext())
                .saveOnboardingQuestionAnswer(onboardingQuestionRequest)

            cardViewSelection.visibility = GONE
            llSelectedWeight.visibility = VISIBLE
            tvSelectedWeight.text = selectedWeight

            /*Handler(Looper.getMainLooper()).postDelayed({
                OnboardingQuestionnaireActivity.navigateToNextPage()
            }, 1000)*/
            (activity as OnboardingQuestionnaireActivity).submitAnswer(onboardingQuestionRequest)
        }


        val layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        recyclerView.layoutManager = layoutManager

        // Generate numbers with increments of 0.1

        for (i in 0..3000) {
            numbers.add(i / 10f) // Increment by 0.1
        }


        adapter = RulerAdapter(numbers) { number ->
            // Handle the selected number
        }
        recyclerView.adapter = adapter


        // Center number with snap alignment
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(recyclerView)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Get the currently snapped position
                    val snappedView = snapHelper.findSnapView(recyclerView.layoutManager)
                    if (snappedView != null) {
                        val position = recyclerView.layoutManager!!.getPosition(snappedView)
                        val snappedNumber = numbers[position]
                        //selected_number_text.setText("$snappedNumber Kg")
                        if (selected_number_text != null) {
                            selected_number_text!!.text = "$snappedNumber$selectedLabel"
                            selectedWeight = selected_number_text?.text.toString()
                            btnContinue.isEnabled = true
                            btnContinue.backgroundTintList = colorStateList
                        }
                    }
                }
            }
        })

        rlRulerContainer.post {
            // Get the width of the parent LinearLayout
            val parentWidth: Int = rlRulerContainer.width

            // Calculate horizontal padding (half of parent width)
            val paddingHorizontal = parentWidth / 2

            // Set horizontal padding programmatically
            rulerView.setPadding(
                paddingHorizontal,
                rulerView.paddingTop,
                paddingHorizontal,
                rulerView.paddingBottom
            )
        }
        rulerView.post {
            rulerView.scrollToPosition(if (gender == "Male" || gender == "M") 750 else 550)
        }
        return view
    }

    private fun setLbsValue() {
        numbers.clear()
        for (i in 0..6615) {
            numbers.add(i / 10f)
        }
        adapter.notifyDataSetChanged()
    }

    private fun setKgsValue() {
        numbers.clear()
        for (i in 0..3000) {
            numbers.add(i / 10f) // Increment by 0.1
        }
        adapter.notifyDataSetChanged()
    }

    override fun onPause() {
        super.onPause()
        cardViewSelection.visibility = VISIBLE
        llSelectedWeight.visibility = GONE
    }

}