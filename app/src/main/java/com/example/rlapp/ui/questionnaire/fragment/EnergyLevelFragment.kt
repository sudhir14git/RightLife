package com.example.rlapp.ui.questionnaire.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.example.rlapp.R
import com.example.rlapp.databinding.FragmentEnergyLevelBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireEatRightActivity
import com.example.rlapp.ui.questionnaire.pojo.EnergyAnswer
import com.example.rlapp.ui.questionnaire.pojo.MRQuestionFive
import com.example.rlapp.ui.questionnaire.pojo.MRQuestionSix
import com.example.rlapp.ui.questionnaire.pojo.Question

class EnergyLevelFragment : Fragment() {
    private lateinit var energyCountTexts: Array<TextView>
    private lateinit var energyValues: Array<String>
    private var morningEneryValue:String = "Low"
    private var afternoonEneryValue:String = "Low"
    private var eveningEneryValue:String = "Low"


    private var _binding: FragmentEnergyLevelBinding? = null
    private val binding get() = _binding!!

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): EnergyLevelFragment {
            val fragment = EnergyLevelFragment()
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
        _binding = FragmentEnergyLevelBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        energyCountTexts = arrayOf<TextView>(
            binding.eneryCountText1,
            binding.eneryCountText2,
            binding.eneryCountText3,

        )
        energyValues = arrayOf<String>(
            "Low",
            "Medium",
            "High"
        )

        binding.btnContinue.setOnClickListener {
            //QuestionnaireEatRightActivity.navigateToNextPage()
            val energyAnswer = EnergyAnswer()
            energyAnswer.night = ""
            energyAnswer.evening = ""
            energyAnswer.morning = ""
            submit(energyAnswer)
        }
        binding.morningSliderView.setMinSteps(0)
        binding.morningSliderView.setMaxSteps(12000)
        binding.morningSliderView.setstepInterval(4000)
        binding.morningSliderView.setIntervalColors(ContextCompat.getColor(requireContext(), R.color.steps_dark_color))
        binding.morningSliderView.setOnStepCountChangeListener { stepCount ->

            // Reset all TextViews to normal
            for (textView in energyCountTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 4000 - 1
            if (index >= 0 && index < energyCountTexts.size) {
                energyCountTexts.get(index).setTypeface(null, Typeface.BOLD)
                morningEneryValue = energyValues.get(index)
            }
        }


        binding.afternoonSliderView.setMinSteps(0)
        binding.afternoonSliderView.setMaxSteps(12000)
        binding.afternoonSliderView.setstepInterval(4000)
        binding.afternoonSliderView.setIntervalColors(ContextCompat.getColor(requireContext(), R.color.steps_dark_color))
        binding.afternoonSliderView.setOnStepCountChangeListener { stepCount ->

            // Reset all TextViews to normal
            for (textView in energyCountTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 4000 - 1
            if (index >= 0 && index < energyCountTexts.size) {
                energyCountTexts.get(index).setTypeface(null, Typeface.BOLD)
                afternoonEneryValue = energyValues.get(index)
            }
        }


        binding.eveningSliderView.setMinSteps(0)
        binding.eveningSliderView.setMaxSteps(12000)
        binding.eveningSliderView.setstepInterval(4000)
        binding.eveningSliderView.setIntervalColors(ContextCompat.getColor(requireContext(), R.color.steps_dark_color))
        binding.eveningSliderView.setOnStepCountChangeListener { stepCount ->

            // Reset all TextViews to normal
            for (textView in energyCountTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 4000 - 1
            if (index >= 0 && index < energyCountTexts.size) {
                energyCountTexts.get(index).setTypeface(null, Typeface.BOLD)
                eveningEneryValue = energyValues.get(index)
            }
        }
    }

    private fun submit(answer: EnergyAnswer) {
        val questionSix = MRQuestionSix()
        questionSix.answer = answer
        QuestionnaireEatRightActivity.moveRightAnswerRequest.questionSix = questionSix
        QuestionnaireEatRightActivity.submitSMoveRightAnswerRequest(
            QuestionnaireEatRightActivity.moveRightAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
