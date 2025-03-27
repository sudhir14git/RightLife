package com.example.rlapp.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.rlapp.databinding.FragmentExercisePreferenceBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireEatRightActivity
import com.example.rlapp.ui.questionnaire.pojo.MRQuestionOne
import com.example.rlapp.ui.questionnaire.pojo.Question

class ExercisePreferenceFragment : Fragment() {
    private var _binding: FragmentExercisePreferenceBinding? = null
    private val binding get() = _binding!!

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): ExercisePreferenceFragment {
            val fragment = ExercisePreferenceFragment()
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
        _binding = FragmentExercisePreferenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.btnAdd.setOnClickListener {
            val times = binding.inputTimes.text.toString()
            if (times.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter value", Toast.LENGTH_SHORT).show()
            } else {
                //QuestionnaireEatRightActivity.navigateToNextPage()
                submit(times)
            }
        }

    }

    private fun submit(answer: String) {
        val questionOne = MRQuestionOne()
        questionOne.answer = answer
        QuestionnaireEatRightActivity.moveRightAnswerRequest.questionOne = questionOne
        QuestionnaireEatRightActivity.submitSMoveRightAnswerRequest(
            QuestionnaireEatRightActivity.moveRightAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}