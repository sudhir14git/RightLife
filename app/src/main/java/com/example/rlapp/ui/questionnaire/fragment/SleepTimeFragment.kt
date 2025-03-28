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
import com.example.rlapp.databinding.FragmentSleepTimeBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireThinkRightActivity
import com.example.rlapp.ui.questionnaire.adapter.ScheduleOptionAdapter
import com.example.rlapp.ui.questionnaire.pojo.Question
import com.example.rlapp.ui.questionnaire.pojo.ScheduleOption

class SleepTimeFragment : Fragment() {

    private var _binding: FragmentSleepTimeBinding? = null
    private val binding get() = _binding!!

    private val scheduleOptions = listOf(
        ScheduleOption(R.drawable.ic_st_1, "I always sleep at the same time", ""),
        ScheduleOption(R.drawable.ic_st_2, "I’m mostly consistent, with slight variations", ""),
        ScheduleOption(R.drawable.ic_3_4_times, "I hardly follow a routine", ""),
        ScheduleOption(R.drawable.ic_daily, "My bedtime is reliant on external factors", ""),
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): SleepTimeFragment {
            val fragment = SleepTimeFragment()
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
        _binding = FragmentSleepTimeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ScheduleOptionAdapter(scheduleOptions, "SleepRight") { selectedOption ->
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
