package com.example.rlapp.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rlapp.R
import com.example.rlapp.databinding.FragmentFastfoodPreferenceBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireEatRightActivity
import com.example.rlapp.ui.questionnaire.adapter.ScheduleOptionAdapter
import com.example.rlapp.ui.questionnaire.pojo.ERQuestionSix
import com.example.rlapp.ui.questionnaire.pojo.Question
import com.example.rlapp.ui.questionnaire.pojo.ScheduleOption

class FastfoodPreferenceFragment : Fragment() {

    private var _binding: FragmentFastfoodPreferenceBinding? = null
    private val binding get() = _binding!!

    private val scheduleOptions = listOf(
        ScheduleOption(R.drawable.ic_never, "Never", ""),
        ScheduleOption(R.drawable.ic_1_2__times, "1-2 times a week", ""),
        ScheduleOption(R.drawable.ic_3_4_times, "3-4 times a week", ""),
        ScheduleOption(R.drawable.ic_daily, "Daily", ""),
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): FastfoodPreferenceFragment {
            val fragment = FastfoodPreferenceFragment()
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
        _binding = FragmentFastfoodPreferenceBinding.inflate(inflater, container, false)
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
        val questionSix = ERQuestionSix()
        questionSix.answer = answer
        QuestionnaireEatRightActivity.eatRightAnswerRequest.questionSix = questionSix
        QuestionnaireEatRightActivity.submitEatRightAnswerRequest(
            QuestionnaireEatRightActivity.eatRightAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
