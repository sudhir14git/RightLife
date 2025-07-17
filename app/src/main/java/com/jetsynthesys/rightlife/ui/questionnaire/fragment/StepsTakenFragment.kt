package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.FragmentStepsTakenBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireEatRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.MRQuestionFive
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class StepsTakenFragment : Fragment() {

    private lateinit var stepsCountTexts: Array<TextView>
    private lateinit var selectedSteps: String

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
      //  binding.root.background = ContextCompat.getDrawable(requireContext(), R.drawable.gradient_color_background_workout)

        stepsCountTexts = arrayOf(
            binding.stepCountText1,
            binding.stepCountText2,
            binding.stepCountText3,
            binding.stepCountText4,
            binding.stepCountText5,
            binding.stepCountText6
        )

        selectedSteps = binding.stepCountText1.text.toString()


        binding.stepsSliderView.setBackgroundColorInt(Color.parseColor("#F0ECEC"))
        binding.stepsSliderView.setFillColorInt(Color.parseColor("#FF4836"))
        binding.stepsSliderView.setupDefaultStepColors("#FF4836")


        val colorMap = hashMapOf(
            0 to Color.parseColor("#FF4836"),
            1000 to Color.parseColor("#FF4836"),
            2000 to Color.parseColor("#FF4836"),
            3000 to Color.parseColor("#F8F836"),
            4000 to Color.parseColor("#F8F836"),
            5000 to Color.parseColor("#84D348"),
            6000 to Color.parseColor("#84D348"),
            7000 to Color.parseColor("#49A2DB"),
            8000 to Color.parseColor("#49A2DB"),
            9000 to Color.parseColor("#49A2DB"),
            10000 to Color.parseColor("#49A2DB"),
            11000 to Color.parseColor("#49A2DB"),
            12000 to Color.parseColor("#49A2DB"),

        )

        binding.stepsSliderView.setStepColorMap(colorMap)

        binding.stepsSliderView.setOnValueChangeListener { stepCount ->

            // Reset all TextViews to normal
            for (textView in stepsCountTexts) {
                textView.setTypeface(null, Typeface.NORMAL)
            }
            // Determine which TextView to bold based on stepCount
            val index = stepCount / 2000 - 1
            if (index >= 0 && index < stepsCountTexts.size) {
                stepsCountTexts[index].setTypeface(null, Typeface.BOLD)
                selectedSteps = stepsCountTexts[index].text.toString()
            }
            binding.tvStepsQuantity.text = "$stepCount\nSteps"
        }

        binding.btnContinue.setOnClickListener {
            //QuestionnaireEatRightActivity.navigateToNextPage()
            binding.btnContinue.disableViewForSeconds()
            submit(selectedSteps)
        }
    }

    private fun submit(answer: String) {
        val questionFive = MRQuestionFive()
        questionFive.answer = answer
        QuestionnaireEatRightActivity.questionnaireAnswerRequest.moveRight?.questionFive =
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
