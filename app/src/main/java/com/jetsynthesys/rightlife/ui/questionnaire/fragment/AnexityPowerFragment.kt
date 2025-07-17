package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.databinding.FragmentAnexityPowerBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireThinkRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.TRQuestionFour
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class AnexityPowerFragment : Fragment() {

    private lateinit var anxietyLevelTexts: Array<TextView>
    private lateinit var selectedAnxiety: String
    private var _binding: FragmentAnexityPowerBinding? = null
    private val binding get() = _binding!!

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): AnexityPowerFragment {
            val fragment = AnexityPowerFragment()
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
        _binding = FragmentAnexityPowerBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        anxietyLevelTexts = arrayOf(
            binding.anxietyLevelText1,
            binding.anxietyLevelText2,
            binding.anxietyLevelText3,
            binding.anxietyLevelText4,
            binding.anxietyLevelText5,
        )

        selectedAnxiety = binding.anxietyLevelText1.text.toString()
        val colorMap = hashMapOf(
            0 to Color.parseColor("#78DD49"),
            1000 to Color.parseColor("#78DD49"),
            2000 to Color.parseColor("#ED8D5D"),
            3000 to Color.parseColor("#FFC143"),
            4000 to Color.parseColor("#6E61B8"),
            5000 to Color.parseColor("#FF4243")
        )

        binding.anxietySliderView.setStepColorMap(colorMap)
        binding.anxietySliderView.setOnValueChangeListener { stepCount ->

            // Reset all TextViews to normal
            for (textView in anxietyLevelTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 1000 - 1
            if (index >= 0 && index < anxietyLevelTexts.size) {
                anxietyLevelTexts[index].setTypeface(null, Typeface.BOLD)
                selectedAnxiety = anxietyLevelTexts[index].text.toString()
            }
        }

        binding.btnContinue.setOnClickListener {
            //QuestionnaireThinkRightActivity.navigateToNextPage()
            binding.btnContinue.disableViewForSeconds()
            submit(selectedAnxiety)
        }
    }

    private fun submit(answer: String) {
        val questionFour = TRQuestionFour()
        questionFour.answer = answer
        QuestionnaireThinkRightActivity.questionnaireAnswerRequest.thinkRight?.questionFour =
            questionFour
        QuestionnaireThinkRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireThinkRightActivity.questionnaireAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}