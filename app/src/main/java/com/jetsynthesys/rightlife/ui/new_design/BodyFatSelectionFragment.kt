package com.jetsynthesys.rightlife.ui.new_design

import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.View.GONE
import android.view.View.VISIBLE
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.FragmentBodyFatSelectionBinding
import com.jetsynthesys.rightlife.ui.new_design.pojo.BodyFat
import com.jetsynthesys.rightlife.ui.utility.DecimalDigitsInputFilter
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class BodyFatSelectionFragment : Fragment() {

    private lateinit var adapter: BodyFatAdapter
    private lateinit var gendar: String
    private lateinit var binding: FragmentBodyFatSelectionBinding
    private lateinit var colorStateListSelected: ColorStateList


    companion object {
        fun newInstance(pageIndex: Int): BodyFatSelectionFragment {
            val fragment = BodyFatSelectionFragment()
            val args = Bundle()
            fragment.arguments = args
            return fragment
        }
    }

    override fun onResume() {
        super.onResume()
        binding.edtBodyFat.text.clear()
        setAdapter()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBodyFatSelectionBinding.inflate(layoutInflater)
        val view: View = binding.root


        colorStateListSelected =
            ContextCompat.getColorStateList(requireContext(), R.color.menuselected)!!
        val colorStateList = ContextCompat.getColorStateList(requireContext(), R.color.rightlife)

        gendar =
            SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest.gender.toString()
        if (!(activity as OnboardingQuestionnaireActivity).forProfileChecklist) {
            (activity as OnboardingQuestionnaireActivity).tvSkip.visibility = VISIBLE
        }

        binding.edtBodyFat.filters = arrayOf(DecimalDigitsInputFilter())

        binding.iconMinus.setOnClickListener {
            var fatValue = binding.edtBodyFat.text.toString().toDouble()
            if (fatValue > 5) {
                fatValue = binding.edtBodyFat.text.toString().toDouble() - 0.5
            }
            binding.edtBodyFat.setText(fatValue.toString())
            binding.edtBodyFat.setSelection(binding.edtBodyFat.text.length)
            binding.edtBodyFat.requestFocus()
        }

        binding.iconPlus.setOnClickListener {
            var fatValue = binding.edtBodyFat.text.toString().toDouble()
            if (fatValue < 60) {
                fatValue = binding.edtBodyFat.text.toString().toDouble() + 0.5
            }
            binding.edtBodyFat.setText(fatValue.toString())
            binding.edtBodyFat.setSelection(binding.edtBodyFat.text.length)
            binding.edtBodyFat.requestFocus()
        }

        binding.edtBodyFat.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.length?.let {
                    if (it > 0) {
                        binding.iconMinus.visibility = VISIBLE
                        binding.iconPlus.visibility = VISIBLE
                        binding.tvPercent.visibility = VISIBLE
                        binding.btnContinue.isEnabled = true
                        binding.btnContinue.backgroundTintList = colorStateListSelected
                        setSelection(gendar, s.toString().toDouble())
                    } else {
                        binding.iconMinus.visibility = GONE
                        binding.iconPlus.visibility = GONE
                        binding.tvPercent.visibility = GONE
                        binding.btnContinue.isEnabled = false
                        binding.btnContinue.backgroundTintList = colorStateList
                        adapter.clearSelection()
                    }
                }
            }
        })

        val gridLayoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvBodyFat.setLayoutManager(gridLayoutManager)

        setAdapter()

        binding.btnContinue.setOnClickListener {
            if (binding.edtBodyFat.text.toString().toDouble() in 5.0..60.0) {

                binding.tvSelectedBodyFat.text = "${binding.edtBodyFat.text}%"
                binding.llSelectedBodyFat.visibility = VISIBLE
                binding.cardViewBodyFat.visibility = GONE
                binding.btnContinue.disableViewForSeconds()
                val onboardingQuestionRequest =
                    SharedPreferenceManager.getInstance(requireContext()).onboardingQuestionRequest
                onboardingQuestionRequest.bodyFat = binding.edtBodyFat.text.toString()
                SharedPreferenceManager.getInstance(requireContext())
                    .saveOnboardingQuestionAnswer(onboardingQuestionRequest)

                (activity as OnboardingQuestionnaireActivity).submitAnswer(onboardingQuestionRequest)
            } else
                Toast.makeText(
                    requireContext(),
                    "Please select fat between 5% to 60%",
                    Toast.LENGTH_SHORT
                ).show()
            return@setOnClickListener


        }


        return view
    }

    private fun setAdapter() {
        adapter = BodyFatAdapter(requireContext(), getBodyFatList(gendar)) { bodyFat ->
            binding.btnContinue.isEnabled = true
            binding.btnContinue.backgroundTintList = colorStateListSelected
            binding.edtBodyFat.setText(average(bodyFat.bodyFatNumber).toString())
            binding.edtBodyFat.setSelection(binding.edtBodyFat.text.length)
            binding.iconMinus.visibility = VISIBLE
            binding.iconPlus.visibility = VISIBLE
            binding.tvPercent.visibility = VISIBLE
        }

        binding.rvBodyFat.adapter = adapter
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
        binding.llSelectedBodyFat.visibility = GONE
        binding.cardViewBodyFat.visibility = VISIBLE
    }

}