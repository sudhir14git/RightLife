package com.jetsynthesys.rightlife.ui.new_design

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ui.new_design.pojo.BodyFat
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager

class BodyFatSelectionFragment : Fragment() {

    private lateinit var llSelectedBodyFat: LinearLayout
    private lateinit var tvSelectedBodyFat: TextView
    private lateinit var tvDescription: TextView
    private lateinit var cardBodyFat: CardView
    private lateinit var edtBodyFat: EditText
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: BodyFatAdapter
    private lateinit var gendar: String


    companion object {
        fun newInstance(pageIndex: Int): BodyFatSelectionFragment {
            val fragment = BodyFatSelectionFragment()
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
        val view: View = inflater.inflate(R.layout.fragment_body_fat_selection, container, false)


        val colorStateListSelected =
            ContextCompat.getColorStateList(requireContext(), R.color.menuselected)
        val colorStateList = ContextCompat.getColorStateList(requireContext(), R.color.rightlife)

        gendar =
            SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest.gender.toString()

        (activity as OnboardingQuestionnaireActivity).tvSkip.visibility = VISIBLE

        llSelectedBodyFat = view.findViewById(R.id.ll_selected_body_fat)
        tvSelectedBodyFat = view.findViewById(R.id.tv_selected_body_fat)
        tvDescription = view.findViewById(R.id.tv_description)
        cardBodyFat = view.findViewById(R.id.card_view_body_fat)
        edtBodyFat = view.findViewById(R.id.edt_body_fat)
        recyclerView = view.findViewById(R.id.rv_body_fat)

        val btnContinue = view.findViewById<Button>(R.id.btn_continue)
        val iconPlus = view.findViewById<ImageView>(R.id.icon_plus)
        val iconMinus = view.findViewById<ImageView>(R.id.icon_minus)
        val tvPercentage = view.findViewById<TextView>(R.id.tv_percent)

        iconMinus.setOnClickListener {
            var fatValue = edtBodyFat.text.toString().toDouble()
            if (fatValue > 5) {
                fatValue = edtBodyFat.text.toString().toDouble() - 0.5
            }
            edtBodyFat.setText(fatValue.toString())
            edtBodyFat.setSelection(edtBodyFat.text.length)
            edtBodyFat.requestFocus()
        }

        iconPlus.setOnClickListener {
            var fatValue = edtBodyFat.text.toString().toDouble()
            if (fatValue < 60) {
                fatValue = edtBodyFat.text.toString().toDouble() + 0.5
            }
            edtBodyFat.setText(fatValue.toString())
            edtBodyFat.setSelection(edtBodyFat.text.length)
            edtBodyFat.requestFocus()
        }

        edtBodyFat.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.length?.let {
                    if (it > 0) {
                        iconMinus.visibility = VISIBLE
                        iconPlus.visibility = VISIBLE
                        tvPercentage.visibility = VISIBLE
                        btnContinue.isEnabled = true
                        btnContinue.backgroundTintList = colorStateListSelected
                        setSelection(gendar, s.toString().toDouble())
                    } else {
                        iconMinus.visibility = GONE
                        iconPlus.visibility = GONE
                        tvPercentage.visibility = GONE
                        btnContinue.isEnabled = false
                        btnContinue.backgroundTintList = colorStateList
                        adapter.clearSelection()
                    }
                }
            }
        })

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.setLayoutManager(gridLayoutManager)


        adapter = BodyFatAdapter(requireContext(), getBodyFatList(gendar)) { bodyFat ->
            btnContinue.isEnabled = true
            btnContinue.backgroundTintList = colorStateListSelected
            edtBodyFat.setText(average(bodyFat.bodyFatNumber).toString())
            edtBodyFat.setSelection(edtBodyFat.text.length)
            edtBodyFat.requestFocus()
            iconMinus.visibility = VISIBLE
            iconPlus.visibility = VISIBLE
            tvPercentage.visibility = VISIBLE
        }

        recyclerView.adapter = adapter

        btnContinue.setOnClickListener {
            tvSelectedBodyFat.text = "${edtBodyFat.text}%"
            llSelectedBodyFat.visibility = VISIBLE
            cardBodyFat.visibility = GONE

            if (edtBodyFat.text.toString().toDouble() < 3 && edtBodyFat.text.toString().toDouble() > 60){
                Toast.makeText(
                    requireContext(),
                    "Please select weight between 30 kg and 300 kg",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener
            }
            val onboardingQuestionRequest =
                SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
            onboardingQuestionRequest.bodyFat = edtBodyFat.text.toString()
            SharedPreferenceManager.getInstance(requireContext())
                .saveOnboardingQuestionAnswer(onboardingQuestionRequest)

            (activity as OnboardingQuestionnaireActivity).submitAnswer(onboardingQuestionRequest)
        }


        return view
    }

    private fun setSelection(gender: String, bodyFat: Double) {
        if (gender == "Male") {
            if (bodyFat in 5.0..14.9)
                adapter.setSelected(0)
            else if (bodyFat in 14.0..24.9)
                adapter.setSelected(1)
            else if (bodyFat in 25.0..33.9)
                adapter.setSelected(2)
            else if (bodyFat >= 34)
                adapter.setSelected(3)
        } else {
            if (bodyFat in 10.0..19.9)
                adapter.setSelected(0)
            else if (bodyFat in 20.0..29.9)
                adapter.setSelected(1)
            else if (bodyFat in 30.0..44.9)
                adapter.setSelected(2)
            else if (bodyFat >= 45)
                adapter.setSelected(3)
        }
    }

    private fun average(input: String): Double {
        val regex = "(\\d+)-(\\d+)".toRegex()

        val matchResult = regex.find(input)
        if (matchResult != null) {
            val num1 = matchResult.groupValues[1].toDouble() // Extracts 5
            val num2 = matchResult.groupValues[2].toDouble() // Extracts 14
            return (num1 + num2) / 2
        } else {
            return input.substring(0, 2).toDouble()
        }

    }

    private fun getBodyFatList(gender: String): ArrayList<BodyFat> {
        val bodyFatList = ArrayList<BodyFat>()
        if (gender == "Male") {
            bodyFatList.add(BodyFat(R.drawable.img_male_fat1, "5-14%"))
            bodyFatList.add(BodyFat(R.drawable.img_male_fat2, "15-24%"))
            bodyFatList.add(BodyFat(R.drawable.img_male_fat3, "25-33%"))
            bodyFatList.add(BodyFat(R.drawable.img_male_fat4, "34+%"))
        } else {
            bodyFatList.add(BodyFat(R.drawable.img_female_fat1, "10-19%"))
            bodyFatList.add(BodyFat(R.drawable.img_female_fat2, "20-29%"))
            bodyFatList.add(BodyFat(R.drawable.img_female_fat3, "30-44%"))
            bodyFatList.add(BodyFat(R.drawable.img_female_fat4, "45+%"))
        }

        return bodyFatList
    }

    override fun onPause() {
        super.onPause()
        llSelectedBodyFat.visibility = GONE
        cardBodyFat.visibility = VISIBLE
    }

}