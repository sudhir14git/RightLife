package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.jetsynthesys.rightlife.databinding.FragmentExercisePreferenceBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireEatRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.MRQuestionOne
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.utility.Utils
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

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
            binding.btnAdd.disableViewForSeconds()
            if (times.isEmpty()) {
                Toast.makeText(requireContext(), "Please enter value", Toast.LENGTH_SHORT).show()
            } else if (times.toInt() == 0) {
                QuestionnaireEatRightActivity.questionnairePagerAdapter.removeItem("ActiveDuringSessionsFragment")
                QuestionnaireEatRightActivity.questionnairePagerAdapter.removeItem("PhysicalActivitiesFragment")
                QuestionnaireEatRightActivity.questionnairePagerAdapter.removeItem("ExerciseLocationFragment")
                QuestionnaireEatRightActivity.questionnairePagerAdapter.notifyDataSetChanged()
                Utils.hideSoftKeyboard(requireActivity())
                submit(times)
            } else if (times.toInt() > 15) {
                Toast.makeText(
                    requireContext(),
                    "It should not be more than 15",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                if (QuestionnaireEatRightActivity.questionnairePagerAdapter.itemCount == 11) {
                    QuestionnaireEatRightActivity.questionnairePagerAdapter.addItem(
                        8,
                        "ActiveDuringSessionsFragment"
                    )
                    QuestionnaireEatRightActivity.questionnairePagerAdapter.addItem(
                        9,
                        "PhysicalActivitiesFragment"
                    )
                    QuestionnaireEatRightActivity.questionnairePagerAdapter.addItem(
                        10,
                        "ExerciseLocationFragment"
                    )
                }
                QuestionnaireEatRightActivity.questionnairePagerAdapter.notifyDataSetChanged()
                submit(times)
                Utils.hideSoftKeyboard(requireActivity())
            }
        }

    }

    private fun submit(answer: String) {
        val questionOne = MRQuestionOne()
        questionOne.answer = answer
        QuestionnaireEatRightActivity.questionnaireAnswerRequest.moveRight?.questionOne =
            questionOne
        QuestionnaireEatRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireEatRightActivity.questionnaireAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}