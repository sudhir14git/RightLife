package com.jetsynthesys.rightlife.ui.new_design

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import com.shawnlin.numberpicker.NumberPicker
import java.util.Locale

class AgeSelectionFragment : Fragment() {

    private val years = arrayOf(
        "13 years",
        "14 years",
        "15 years",
        "16 years",
        "17 years",
        "18 years",
        "19 years",
        "20 years",
        "21 years",
        "22 years",
        "23 years",
        "24 years",
        "25 years",
        "26 years",
        "27 years",
        "28 years",
        "29 years",
        "30 years",
        "31 years",
        "32 years",
        "33 years",
        "34 years",
        "35 years",
        "36 years",
        "37 years",
        "38 years",
        "39 years",
        "40 years",
        "41 years",
        "42 years",
        "43 years",
        "44 years",
        "45 years",
        "46 years",
        "47 years",
        "48 years",
        "49 years",
        "50 years",
        "51 years",
        "52 years",
        "53 years",
        "54 years",
        "55 years",
        "56 years",
        "57 years",
        "58 years",
        "59 years",
        "60 years",
        "61 years",
        "62 years",
        "63 years",
        "64 years",
        "65 years",
        "66 years",
        "67 years",
        "68 years",
        "69 years",
        "70 years",
        "71 years",
        "72 years",
        "73 years",
        "74 years",
        "75 years",
        "76 years",
        "77 years",
        "78 years",
        "79 years",
        "80 years",
        "81 years",
        "82 years",
        "83 years",
        "84 years",
        "85 years",
        "86 years",
        "87 years",
        "88 years",
        "89 years",
        "90 years",
        "91 years",
        "92 years",
        "93 years",
        "94 years",
        "95 years",
        "96 years",
        "97 years",
        "98 years",
        "99 years",
        "100 years",
        "101 years",
        "102 years",
        "103 years",
        "104 years",
        "105 years",
        "106 years",
        "107 years",
        "108 years",
        "109 years",
        "110 years",
        "111 years",
        "112 years",
        "113 years",
        "114 years",
        "115 years",
        "116 years",
        "117 years",
        "118 years",
        "119 years",
        "120 years"
    )

    private lateinit var llSelectedAge: LinearLayout
    private lateinit var tvSelectedAge: TextView
    private var selectedAge = ""
    private var tvDescription: TextView? = null
    private lateinit var cardViewSelection: CardView


    companion object {
        fun newInstance(pageIndex: Int): AgeSelectionFragment {
            val fragment = AgeSelectionFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view: View = inflater.inflate(R.layout.fragment_age_selection, container, false)

        llSelectedAge = view.findViewById(R.id.ll_selected_age)
        tvSelectedAge = view.findViewById(R.id.tv_selected_age)
        tvDescription = view.findViewById(R.id.tv_description)
        cardViewSelection = view.findViewById(R.id.card_view_age_selector)
        if (!(activity as OnboardingQuestionnaireActivity).forProfileChecklist) {
            (activity as OnboardingQuestionnaireActivity).tvSkip.visibility = VISIBLE
        }

        val btnContinue = view.findViewById<Button>(R.id.btn_continue)
        btnContinue.setOnClickListener {
            btnContinue.disableViewForSeconds()
            val onboardingQuestionRequest =
                SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
            val age = selectedAge.split(" ")
            onboardingQuestionRequest.age = age[0].toInt()
            SharedPreferenceManager.getInstance(requireContext())
                .saveOnboardingQuestionAnswer(onboardingQuestionRequest)

            cardViewSelection.visibility = GONE
            llSelectedAge.visibility = VISIBLE
            tvSelectedAge.text = selectedAge

            (activity as OnboardingQuestionnaireActivity).submitAnswer(onboardingQuestionRequest)
        }

        // new number picker
        val numberPicker = view.findViewById<View>(R.id.number_picker) as NumberPicker
        numberPicker.minValue = 1
        numberPicker.maxValue = years.size
        numberPicker.displayedValues = years
        numberPicker.value = 15
        numberPicker.wheelItemCount = 7

        selectedAge = years[14]

        // OnScrollListener
        numberPicker.setOnScrollListener { view, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                Log.d(
                    ContentValues.TAG,
                    String.format(Locale.US, "newVal: %d", view.value)
                )
                Log.d("Selected age : ", years[view.value - 1] + "")
                selectedAge = years[view.value - 1]
            }
        }


        return view
    }

    override fun onPause() {
        super.onPause()
        cardViewSelection.visibility = VISIBLE
        llSelectedAge.visibility = GONE
    }

}