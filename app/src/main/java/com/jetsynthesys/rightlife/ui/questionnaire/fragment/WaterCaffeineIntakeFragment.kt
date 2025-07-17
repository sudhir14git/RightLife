package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.databinding.FragmentWaterCaffeineIntakeBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireEatRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.AnswerWaterCoffee
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Coffee
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.ERQuestionFive
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Water
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class WaterCaffeineIntakeFragment : Fragment() {

    private lateinit var waterCountTexts: Array<TextView>
    private lateinit var coffeeCountTexts: Array<TextView>
    private var _binding: FragmentWaterCaffeineIntakeBinding? = null
    private val binding get() = _binding!!

    private var question: Question? = null
    private val water = Water()
    val coffee = Coffee()

    companion object {
        fun newInstance(question: Question): WaterCaffeineIntakeFragment {
            val fragment = WaterCaffeineIntakeFragment()
            val args = Bundle().apply {
                putSerializable("question", question)
            }
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            question = it.getSerializable("question") as? Question
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWaterCaffeineIntakeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        waterCountTexts = arrayOf(
            binding.waterCountText1,
            binding.waterCountText2,
            binding.waterCountText3,
            binding.waterCountText4,
            binding.waterCountText5,
            binding.waterCountText6
        )
        water.cups = waterCountTexts[0].text.toString()
        water.quantity = (250).toString() + " ml"

        coffeeCountTexts = arrayOf(
            binding.coffeeCountText1,
            binding.coffeeCountText2,
            binding.coffeeCountText3,
            binding.coffeeCountText4,
            binding.coffeeCountText5,
            binding.coffeeCountText6
        )

        coffee.cups = coffeeCountTexts[0].text.toString()
        coffee.quantity = (125 * 1).toString() + " ml"

        //binding.waterView.setMinSteps(0)
        //binding.waterView.setMaxSteps(12000)
        //binding.waterView.setIntervalColors(ContextCompat.getColor(requireContext(), R.color.water_dark_color))
        binding.waterView.setBackgroundColorInt(Color.parseColor("#E6F0FE"))
        binding.waterView.setFillColorInt(Color.parseColor("#1292E5"))
        binding.waterView.setOnValueChangeListener { stepCount ->

            // Reset all TextViews to normal
            for (textView in waterCountTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 2000 - 1
            if (index >= 0 && index < waterCountTexts.size) {
                waterCountTexts[index].setTypeface(null, Typeface.BOLD)
                //binding.tvWaterQuantity.text = (500*(index+1)).toString() + " ml"
                water.cups = waterCountTexts[index].text.toString()
                //water.quantity = (500*(index+1)).toString() + " ml"
            }
            if (stepCount == 0) {
                binding.tvWaterQuantity.text = "0 ml"
            } else {
                var value = stepCount / 1000
                Log.d("Steps ", ": $stepCount - " + value)
                water.quantity = (250 * (value)).toString() + " ml"
                binding.tvWaterQuantity.text = water.quantity
            }
        }

        //binding.coffeeView.setMinSteps(0)
        //binding.coffeeView.setMaxSteps(8000)
        //binding.coffeeView.setIntervalColors(ContextCompat.getColor(requireContext(), R.color.coffee_dark_color))
        //binding.coffeeView.setStepIntervalCount(1000)
        binding.coffeeView.setBackgroundColorInt(Color.parseColor("#F0ECEC"))
        binding.coffeeView.setFillColorInt(Color.parseColor("#6C4C3F"))
        binding.coffeeView.setupDefaultStepColors("#6C4C3F")
        binding.coffeeView.setOnValueChangeListener { stepCount ->
            //binding.coffeeView.setStepIntervalCount(2000)

            // Reset all TextViews to normal
            for (textView in coffeeCountTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 2000 - 1
            if (index >= 0 && index < coffeeCountTexts.size) {
                coffeeCountTexts.get(index).setTypeface(null, Typeface.BOLD)
                binding.tvCoffeeQuantity.text = (125 * (index + 1)).toString() + " ml"
                /* if (index==0) {
                     binding.tvCoffeeQuantity.text = (125 *1).toString() + " ml"
                 }else if (index==1){
                     binding.tvCoffeeQuantity.text =  (125 *3).toString() + " ml"
                 }else if (index==2){
                     binding.tvCoffeeQuantity.text =  (125 *5).toString() + " ml"
                 }else if (index==3){
                     binding.tvCoffeeQuantity.text =  (125 *7).toString() + " ml"
                 }*/
                coffee.cups = coffeeCountTexts[index].text.toString()
                coffee.quantity = binding.tvCoffeeQuantity.text.toString()
            }
            if (stepCount == 0) {
                binding.tvCoffeeQuantity.text = "0 ml"
            } else {
                var value = stepCount / 1000
                Log.d("Steps ", ": $stepCount - " + value)
                coffee.quantity = (125 * (value)).toString() + " ml"
                binding.tvCoffeeQuantity.text = coffee.quantity
            }

        }

        binding.btnContinue.setOnClickListener {
            //QuestionnaireEatRightActivity.navigateToNextPage()
            val answerWaterCoffee = AnswerWaterCoffee()
            answerWaterCoffee.water = water
            answerWaterCoffee.coffee = coffee
            if (binding.tvWaterQuantity.text.toString().equals("0 ml", ignoreCase = true)) {
                Toast.makeText(requireContext(), "Please select water intake", Toast.LENGTH_SHORT).show()
            }else {
                binding.btnContinue.disableViewForSeconds()
                submit(answerWaterCoffee)
            }
        }
    }

    private fun submit(answer: AnswerWaterCoffee) {
        val questionFive = ERQuestionFive()
        questionFive.answer = answer
        QuestionnaireEatRightActivity.questionnaireAnswerRequest.eatRight?.questionFive =
            questionFive
        QuestionnaireEatRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireEatRightActivity.questionnaireAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
