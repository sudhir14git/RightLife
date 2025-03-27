package com.example.rlapp.ui.questionnaire.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.rlapp.R
import com.example.rlapp.databinding.FragmentWaterCaffeineIntakeBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireEatRightActivity
import com.example.rlapp.ui.questionnaire.pojo.AnswerWaterCoffee
import com.example.rlapp.ui.questionnaire.pojo.Coffee
import com.example.rlapp.ui.questionnaire.pojo.ERQuestionFive
import com.example.rlapp.ui.questionnaire.pojo.Question
import com.example.rlapp.ui.questionnaire.pojo.Water

class WaterCaffeineIntakeFragment : Fragment() {

    var intervalColors: IntArray = intArrayOf(
       R.color.red,  // 0-2k
        Color.parseColor("#F8F836"),  // 2-4k
        Color.parseColor("#84D348"),  // 4-6k
        Color.parseColor("#49A2DB"),  // 6-8k
        Color.parseColor("#49A2DB"),  // 8-10k
        Color.parseColor("#49A2DB") // 10-12k
    )


    private lateinit var waterCountTexts: Array<TextView>
    private lateinit var coffeeCountTexts: Array<TextView>
    private var _binding: FragmentWaterCaffeineIntakeBinding? = null
    private val binding get() = _binding!!

    private var question: Question? = null

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

        waterCountTexts = arrayOf<TextView>(
            binding.waterCountText1,
            binding.waterCountText2,
            binding.waterCountText3,
            binding.waterCountText4,
            binding.waterCountText5,
            binding.waterCountText6
        )

        coffeeCountTexts = arrayOf<TextView>(
            binding.coffeeCountText1,
            binding.coffeeCountText2,
            binding.coffeeCountText3,
            binding.coffeeCountText4

        )

        binding.btnContinue.setOnClickListener {
            //QuestionnaireEatRightActivity.navigateToNextPage()
            val answerWaterCoffee = AnswerWaterCoffee()
            val water = Water()
            water.cups = ""
            water.quantity = ""
            answerWaterCoffee.water = water
            val coffee = Coffee()
            coffee.cups = ""
            coffee.quantity = ""
            answerWaterCoffee.coffee = coffee
            submit(answerWaterCoffee)
        }
        binding.waterView.setMinSteps(0)
        binding.waterView.setMaxSteps(12000)
        binding.waterView.setIntervalColors(ContextCompat.getColor(requireContext(), R.color.water_dark_color))
        binding.waterView.setOnStepCountChangeListener { stepCount ->

            // Reset all TextViews to normal
            for (textView in waterCountTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 2000 - 1
            if (index >= 0 && index < waterCountTexts.size) {
                waterCountTexts.get(index).setTypeface(null, Typeface.BOLD)
                binding.tvWaterQuantity.text = (500*(index+1)).toString() + " ml"
            }
        }

        binding.coffeeView.setMinSteps(0)
        binding.coffeeView.setMaxSteps(8000)
        binding.coffeeView.setIntervalColors(ContextCompat.getColor(requireContext(), R.color.coffee_dark_color))
        //binding.coffeeView.setStepIntervalCount(1000)
        binding.coffeeView.setOnStepCountChangeListener { stepCount ->
            //binding.coffeeView.setStepIntervalCount(2000)

            // Reset all TextViews to normal
            for (textView in coffeeCountTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 2000 - 1
            if (index >= 0 && index < coffeeCountTexts.size) {
                coffeeCountTexts.get(index).setTypeface(null, Typeface.BOLD)
                if (index==0) {
                    binding.tvCoffeeQuantity.text = (125 *1).toString() + " ml"
                }else if (index==1){
                    binding.tvCoffeeQuantity.text =  (125 *3).toString() + " ml"
                }else if (index==2){
                    binding.tvCoffeeQuantity.text =  (125 *5).toString() + " ml"
                }else if (index==3){
                    binding.tvCoffeeQuantity.text =  (125 *7).toString() + " ml"
                }
            }
        }
    }

    private fun submit(answer: AnswerWaterCoffee) {
        val questionFive = ERQuestionFive()
        questionFive.answer = answer
        QuestionnaireEatRightActivity.eatRightAnswerRequest.questionFive = questionFive
        QuestionnaireEatRightActivity.submitEatRightAnswerRequest(
            QuestionnaireEatRightActivity.eatRightAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
