package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.FragmentEmotionsPastWeekBinding
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireThinkRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.TRQuestionOne
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import okio.FileNotFoundException

class EmotionsPastWeekFragment : Fragment() {


    private lateinit var emotionSelected: String
    private val EmotionsList = arrayOf(
        "Overwhelmed",
        "Unsettled",
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

        //default color
        binding.tvTitle.text = EmotionsList[0]
        emotionSelected = EmotionsList[0]
        binding.cardView.setBackgroundResource(EmotionsBgCards[0])
        // Set SeekBar listener to change background color dynamically
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                binding.tvTitle.text = EmotionsList[progress]
                emotionSelected = EmotionsList[progress]
                binding.cardView.setBackgroundResource(EmotionsBgCards[progress])
                try {
                    binding.lottieView.setAnimation("$emotionSelected.json")
                    binding.lottieView.playAnimation()
                }catch (e: FileNotFoundException){
                    e.printStackTrace()
                }

            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}
            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        binding.btnContinue.setOnClickListener {
            binding.btnContinue.disableViewForSeconds()
            submit(emotionSelected)
        }
    }

    private fun submit(answer: String) {
        val questionOne = TRQuestionOne()
        questionOne.answer = answer
        QuestionnaireThinkRightActivity.questionnaireAnswerRequest.thinkRight?.questionOne =
            questionOne
        QuestionnaireThinkRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireThinkRightActivity.questionnaireAnswerRequest
        )

        CommonAPICall.updateChecklistStatus(requireContext(), "unlock_sleep", AppConstants.CHECKLIST_INPROGRESS)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
