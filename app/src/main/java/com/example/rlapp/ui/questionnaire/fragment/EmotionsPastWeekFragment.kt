package com.example.rlapp.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.example.rlapp.R
import com.example.rlapp.databinding.FragmentEmotionsPastWeekBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireThinkRightActivity
import com.example.rlapp.ui.questionnaire.pojo.Question
import com.example.rlapp.ui.questionnaire.pojo.TRQuestionOne

class EmotionsPastWeekFragment : Fragment() {


    private lateinit var emotionSelected: String
    private val EmotionsList = arrayOf(
        "Chaotic",
        "UnStable",
        "Uneven",
        "Steady",
        "Calm"

    )
    private val EmotionsBgCards = arrayOf(
        R.drawable.chaotic_bg_card,
        R.drawable.unstable_bg_card,
        R.drawable.uneven_bg_card,
        R.drawable.steady_bg_card,
        R.drawable.calm_bg_card

    )

    private var _binding: FragmentEmotionsPastWeekBinding? = null
    private val binding get() = _binding!!
    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): EmotionsPastWeekFragment {
            val fragment = EmotionsPastWeekFragment()
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
        _binding = FragmentEmotionsPastWeekBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {

        binding.btnContinue.setOnClickListener {
            submit("")
        }

        //default color
        binding.tvTitle.text = EmotionsList[0]
        emotionSelected =  EmotionsList[0]
        binding.cardView.setBackgroundResource(EmotionsBgCards[0])
        // Set SeekBar listener to change background color dynamically
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvTitle.text = EmotionsList[progress]
                emotionSelected = EmotionsList[progress]
                binding.cardView.setBackgroundResource(EmotionsBgCards[progress])
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })
    }

    private fun submit(answer: String) {
        val questionOne = TRQuestionOne()
        questionOne.answer = answer
        QuestionnaireThinkRightActivity.thinkRightAnswerRequest.questionOne = questionOne
        QuestionnaireThinkRightActivity.submitThinkRightRightAnswerRequest(
            QuestionnaireThinkRightActivity.thinkRightAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
