package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.databinding.FragmentEnergyLevelBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireEatRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.EnergyAnswer
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.MRQuestionSix
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class EnergyLevelFragment : Fragment() {
    private lateinit var energyCountTexts: Array<TextView>
    private lateinit var energyValues: Array<String>
    private var morningEneryValue: String = "Medium"
    private var afternoonEneryValue: String = "Medium"
    private var eveningEneryValue: String = "Medium"

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


        val colorMap = hashMapOf(
            0 to Color.parseColor("#FB9900"),
            1000 to Color.parseColor("#FB9900"),
            2000 to Color.parseColor("#66B12D"),
            3000 to Color.parseColor("#1292E5")
        )

        binding.morningSliderView.setStepColorMap(colorMap)


        binding.morningSliderView.setOnValueChangeListener { stepCount ->

            // Reset all TextViews to normal
            for (textView in energyCountTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 1000 - 1
            if (index >= 0 && index < energyCountTexts.size) {
                energyCountTexts[index].setTypeface(null, Typeface.BOLD)
                morningEneryValue = energyValues[index]
            }
        }


        binding.afternoonSliderView.setStepColorMap(colorMap)

        binding.afternoonSliderView.setOnValueChangeListener { stepCount ->

            // Reset all TextViews to normal
            for (textView in energyCountTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 1000 - 1
            if (index >= 0 && index < energyCountTexts.size) {
                energyCountTexts[index].setTypeface(null, Typeface.BOLD)
                afternoonEneryValue = energyValues[index]
            }
        }

        binding.eveningSliderView.setStepColorMap(colorMap)

        binding.eveningSliderView.setOnValueChangeListener { stepCount ->

            // Reset all TextViews to normal
            for (textView in energyCountTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 1000 - 1
            if (index >= 0 && index < energyCountTexts.size) {
                energyCountTexts[index].setTypeface(null, Typeface.BOLD)
                eveningEneryValue = energyValues[index]
            }
        }

        binding.btnContinue.setOnClickListener {
            //QuestionnaireEatRightActivity.navigateToNextPage()
            binding.btnContinue.disableViewForSeconds()
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
