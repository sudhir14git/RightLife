package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.databinding.FragmentOverthiningYourselfBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireThinkRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.TRQuestionFive
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class OverthinkingYourselfFragment : Fragment() {

    private var _binding: FragmentOverthiningYourselfBinding? = null
    private val binding get() = _binding!!

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): OverthinkingYourselfFragment {
            val fragment = OverthinkingYourselfFragment()
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
        _binding = FragmentOverthiningYourselfBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        // Set default state
        binding.tvTitle.text = EmotionsList[0]

        // SeekBar change listener
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (progress == 0) {
                    binding.seekBar.progress = 1 // Reset to 1
                    return
                }
                binding.tvTitle.text = EmotionsList[progress]
                binding.triangleView.setProgress(progress, binding.seekBar.max)
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
        binding.seekBar.progress = 1
        //binding.triangleView.setProgress(1, binding.seekBar.max)
        binding.triangleView.post {
            binding.triangleView.setProgress(1, binding.seekBar.max)
        }

        binding.btnContinue.setOnClickListener {
            //QuestionnaireThinkRightActivity.navigateToNextPage()
            binding.btnContinue.disableViewForSeconds()
            submit(binding.tvTitle.text.toString())
        }
    }

    private fun submit(answer: String) {
        val questionFive = TRQuestionFive()
        questionFive.answer = answer
        QuestionnaireThinkRightActivity.questionnaireAnswerRequest.thinkRight?.questionFive =
            questionFive
        QuestionnaireThinkRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireThinkRightActivity.questionnaireAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private val EmotionsList = arrayOf(
        "Never",
        "Never",
        "Rarely",
        "Sometimes",
        "All the time",
    )
}