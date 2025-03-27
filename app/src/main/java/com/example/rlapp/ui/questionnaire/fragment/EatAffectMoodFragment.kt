package com.example.rlapp.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rlapp.R
import com.example.rlapp.databinding.FragmentEatAffectMoodBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireEatRightActivity
import com.example.rlapp.ui.questionnaire.adapter.ScheduleOptionAdapter
import com.example.rlapp.ui.questionnaire.pojo.ERQuestionSeven
import com.example.rlapp.ui.questionnaire.pojo.Question
import com.example.rlapp.ui.questionnaire.pojo.ScheduleOption

class EatAffectMoodFragment : Fragment() {

    private var _binding: FragmentEatAffectMoodBinding? = null
    private val binding get() = _binding!!

    private val scheduleOptions = listOf(
        ScheduleOption(R.drawable.ic_eat_affect_1, "Never", "I eat only when I’m hungry"),
        ScheduleOption(R.drawable.ic_eat_affect_2, "Rarely", "I control my emotions"),
        ScheduleOption(R.drawable.ic_eat_affect_3, "Sometimes", "it depends on the day"),
        ScheduleOption(R.drawable.ic_eat_affect_4, "Always", "I eat based on my mood"),
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): EatAffectMoodFragment {
            val fragment = EatAffectMoodFragment()
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
        _binding = FragmentEatAffectMoodBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ScheduleOptionAdapter(scheduleOptions) { selectedOption ->
            /*Handler(Looper.getMainLooper()).postDelayed({
                QuestionnaireEatRightActivity.navigateToNextPage()
            }, 500)*/
            submit(selectedOption.title)
        }
        binding.rvScheduleOptions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvScheduleOptions.adapter = adapter
    }

    private fun submit(answer: String) {
        val questionSeven = ERQuestionSeven()
        questionSeven.answer = answer
        QuestionnaireEatRightActivity.eatRightAnswerRequest.questionSeven = questionSeven
        QuestionnaireEatRightActivity.submitEatRightAnswerRequest(
            QuestionnaireEatRightActivity.eatRightAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
