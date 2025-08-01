package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.FragmentSocialInteractionsBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireThinkRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.adapter.SocialInteractionAdapter
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.SocialInteraction
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.TRQuestionSix
import com.jetsynthesys.rightlife.ui.utility.runWithCooldown

class SocialInteractionFragment : Fragment() {

    private var _binding: FragmentSocialInteractionsBinding? = null
    private val binding get() = _binding!!

    private val socialInteractionsList = listOf(
        SocialInteraction("Very meaningful and satisfying", R.drawable.ic_si_1),
        SocialInteraction("Mostly positive", R.drawable.ic_si_2),
        SocialInteraction("Neutral", R.drawable.ic_si_3),
        SocialInteraction("Extremely challenging", R.drawable.ic_si_4)
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): SocialInteractionFragment {
            val fragment = SocialInteractionFragment()
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
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSocialInteractionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SocialInteractionAdapter(
            socialInteractionsList,
            { socialInteraction: SocialInteraction ->
                submit(socialInteraction.title)
            }.runWithCooldown()
        )
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    private fun submit(answer: String) {
        val questionSix = TRQuestionSix()
        questionSix.answer = answer
        QuestionnaireThinkRightActivity.questionnaireAnswerRequest.thinkRight?.questionSix =
            questionSix
        QuestionnaireThinkRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireThinkRightActivity.questionnaireAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}