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
import com.example.rlapp.databinding.FragmentStepsTakenBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireEatRightActivity
import com.example.rlapp.ui.questionnaire.pojo.MRQuestionFive
import com.example.rlapp.ui.questionnaire.pojo.MRQuestionFour
import com.example.rlapp.ui.questionnaire.pojo.Question

class StepsTakenFragment : Fragment() {

    private lateinit var stepsCountTexts: Array<TextView>

    private var _binding: FragmentStepsTakenBinding? = null
    private val binding get() = _binding!!

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): StepsTakenFragment {
            val fragment = StepsTakenFragment()
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
        _binding = FragmentStepsTakenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {


        stepsCountTexts = arrayOf<TextView>(
            binding.stepCountText1,
            binding.stepCountText2,
            binding.stepCountText3,
            binding.stepCountText4,
            binding.stepCountText5,
            binding.stepCountText6
        )
        binding.btnContinue.setOnClickListener {
            //QuestionnaireEatRightActivity.navigateToNextPage()
            submit("")
        }

        binding.stepsSliderView.setMinSteps(0)
        binding.stepsSliderView.setMaxSteps(12000)
        binding.stepsSliderView.setIntervalColors(ContextCompat.getColor(requireContext(), R.color.steps_dark_color))
        binding.stepsSliderView.setOnStepCountChangeListener { stepCount ->

            // Reset all TextViews to normal
            for (textView in stepsCountTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 2000 - 1
            if (index >= 0 && index < stepsCountTexts.size) {
                stepsCountTexts.get(index).setTypeface(null, Typeface.BOLD)

            }
        }
    }

    private fun submit(answer: String) {
        val questionFive = MRQuestionFive()
        questionFive.answer = answer
        QuestionnaireEatRightActivity.moveRightAnswerRequest.questionFive = questionFive
        QuestionnaireEatRightActivity.submitSMoveRightAnswerRequest(
            QuestionnaireEatRightActivity.moveRightAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
