package com.example.rlapp.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rlapp.databinding.FragmentAnexityPowerBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireThinkRightActivity
import com.example.rlapp.ui.questionnaire.pojo.Question

class AnexityPowerFragment : Fragment() {

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
        binding.btnContinue.setOnClickListener {
            QuestionnaireThinkRightActivity.navigateToNextPage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}