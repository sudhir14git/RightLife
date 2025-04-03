package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.FragmentEnergyLevelBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireEatRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.EnergyAnswer
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.MRQuestionSix
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question

class EnergyLevelFragment : Fragment() {
    private lateinit var energyCountTexts: Array<TextView>
    private lateinit var energyValues: Array<String>
    private var morningEneryValue: String = "Low"
    private var afternoonEneryValue: String = "Low"
    private var eveningEneryValue: String = "Low"

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

        energyCountTexts = arrayOf(
            binding.eneryCountText1,
            binding.eneryCountText2,
            binding.eneryCountText3,

            )
        energyValues = arrayOf(
            "Low",
            "Medium",
            "High"
        )

        binding.morningSliderView.setMinSteps(0)
        binding.morningSliderView.setMaxSteps(12000)
        binding.morningSliderView.setstepInterval(4000)
        binding.morningSliderView.setIntervalColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.steps_dark_color
            )
        )
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
        binding.afternoonSliderView.setIntervalColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.steps_dark_color
            )
        )
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
        binding.eveningSliderView.setIntervalColors(
            ContextCompat.getColor(
                requireContext(),
                R.color.steps_dark_color
            )
        )
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

        binding.btnContinue.setOnClickListener {
            //QuestionnaireEatRightActivity.navigateToNextPage()
            val energyAnswer = EnergyAnswer()
            energyAnswer.morning = morningEneryValue
            energyAnswer.evening = afternoonEneryValue
            energyAnswer.night = eveningEneryValue
            submit(energyAnswer)
        }
    }

    private fun submit(answer: EnergyAnswer) {
        val questionSix = MRQuestionSix()
        questionSix.answer = answer
        QuestionnaireEatRightActivity.questionnaireAnswerRequest.moveRight?.questionSix =
            questionSix
        QuestionnaireEatRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireEatRightActivity.questionnaireAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
