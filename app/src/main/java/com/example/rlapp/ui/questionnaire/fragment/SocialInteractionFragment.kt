package com.example.rlapp.ui.questionnaire.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rlapp.R
import com.example.rlapp.databinding.FragmentSocialInteractionsBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireThinkRightActivity
import com.example.rlapp.ui.questionnaire.adapter.SocialInteractionAdapter
import com.example.rlapp.ui.questionnaire.pojo.Question
import com.example.rlapp.ui.questionnaire.pojo.SocialInteraction

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
    ): View? {
        _binding = FragmentSocialInteractionsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = SocialInteractionAdapter(socialInteractionsList) {
            Handler(Looper.getMainLooper()).postDelayed({
                QuestionnaireThinkRightActivity.navigateToNextPage()
            }, 500)
        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}