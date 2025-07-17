package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.databinding.FragmentQualityOfSleepBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireThinkRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.SRQuestionOne
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class QualityOfSleepFragment : Fragment() {
    private lateinit var sleepQualityLevelTexts: Array<TextView>
    private lateinit var selectedSleepQuality: String
    private var _binding: FragmentQualityOfSleepBinding? = null
    private val binding get() = _binding!!

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): QualityOfSleepFragment {
            val fragment = QualityOfSleepFragment()
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
        _binding = FragmentQualityOfSleepBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        sleepQualityLevelTexts = arrayOf(
            binding.anxietyLevelText1,
            binding.anxietyLevelText2,
            binding.anxietyLevelText3,
            binding.anxietyLevelText4,
            binding.anxietyLevelText5,
        )

        selectedSleepQuality = binding.anxietyLevelText1.text.toString()
        val colorMap = hashMapOf(
            0 to Color.parseColor("#F25136"),
            1000 to Color.parseColor("#F25136"),
            2000 to Color.parseColor("#ED9120"),
            3000 to Color.parseColor("#DDC727"),
            4000 to Color.parseColor("#AFC61D"),
            5000 to Color.parseColor("#1CC675")
        )

        binding.sleepQualitySliderView.setStepColorMap(colorMap)
        binding.sleepQualitySliderView.setOnValueChangeListener { stepCount ->

            // Reset all TextViews to normal
            for (textView in sleepQualityLevelTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 1000 - 1
            if (index >= 0 && index < sleepQualityLevelTexts.size) {
                sleepQualityLevelTexts[index].setTypeface(null, Typeface.BOLD)
                selectedSleepQuality = sleepQualityLevelTexts[index].text.toString()
            }
        }

        binding.btnContinue.setOnClickListener {
            binding.btnContinue.disableViewForSeconds()
            //QuestionnaireThinkRightActivity.navigateToNextPage()
            submit(selectedSleepQuality)
        }
    }

    private fun submit(answer: String) {
        val questionOne = SRQuestionOne()
        questionOne.answer = answer
        QuestionnaireThinkRightActivity.questionnaireAnswerRequest.sleepRight?.questionOne =
            questionOne
        QuestionnaireThinkRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireThinkRightActivity.questionnaireAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}