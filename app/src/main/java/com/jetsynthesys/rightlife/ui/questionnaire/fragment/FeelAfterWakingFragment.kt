package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.FragmentFeelAfterWakingBinding
import com.jetsynthesys.rightlife.ui.jounal.new_journal.JournalMoodAdapter
import com.jetsynthesys.rightlife.ui.jounal.new_journal.Mood
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireThinkRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.SRQuestionFive
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class FeelAfterWakingFragment : Fragment() {

    private var _binding: FragmentFeelAfterWakingBinding? = null
    private val binding get() = _binding!!

    private var question: Question? = null
    private var mSelectedMood: Mood = Mood("Refreshed", R.drawable.ic_happy_layered)

    companion object {
        fun newInstance(question: Question): FeelAfterWakingFragment {
            val fragment = FeelAfterWakingFragment()
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
        _binding = FragmentFeelAfterWakingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setUpMoodList()

        binding.btnContinue.setOnClickListener {
            binding.btnContinue.disableViewForSeconds()
            //QuestionnaireThinkRightActivity.navigateToNextPage()
            submit(mSelectedMood.name)
        }
    }

    private fun submit(answer: String) {
        val questionFive = SRQuestionFive()
        questionFive.answer = answer
        QuestionnaireThinkRightActivity.questionnaireAnswerRequest.sleepRight?.questionFive =
            questionFive
        QuestionnaireThinkRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireThinkRightActivity.questionnaireAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpMoodList() {
        val moodList = listOf(
            Mood("Refreshed", R.drawable.ic_happy, isSelected = true),
            Mood("Rested", R.drawable.ic_relaxed),
            Mood("Neutral", R.drawable.ic_unsure),
            Mood("Exhausted", R.drawable.ic_stressed),
            Mood("Drained", R.drawable.ic_sad)
        )

        val adapter = JournalMoodAdapter(moodList, 0) { selectedMood ->
            mSelectedMood = selectedMood
            when (selectedMood.name) {
                "Refreshed" -> {
                    binding.ivSelectedImage.setImageResource(R.drawable.ic_happy_layered)

                }

                "Rested" -> {
                    binding.ivSelectedImage.setImageResource(R.drawable.ic_relaxed_layered)

                }

                "Neutral" -> {
                    binding.ivSelectedImage.setImageResource(R.drawable.ic_unsure_layered)

                }

                "Exhausted" -> {
                    binding.ivSelectedImage.setImageResource(R.drawable.ic_stressed_layered)

                }

                "Drained" -> {
                    binding.ivSelectedImage.setImageResource(R.drawable.ic_sad_layered)

                }
            }
        }

        binding.moodRecyclerView.adapter = adapter
        binding.moodRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.ivSelectedImage.setImageResource(R.drawable.ic_happy_layered)
    }
}