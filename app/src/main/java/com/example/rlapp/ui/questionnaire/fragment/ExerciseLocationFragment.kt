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
import com.example.rlapp.databinding.FragmentFastfoodPreferenceBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireEatRightActivity
import com.example.rlapp.ui.questionnaire.adapter.ScheduleOptionAdapter
import com.example.rlapp.ui.questionnaire.pojo.Question
import com.example.rlapp.ui.questionnaire.pojo.ScheduleOption

class ExerciseLocationFragment : Fragment() {

    private var _binding: FragmentFastfoodPreferenceBinding? = null
    private val binding get() = _binding!!

    private val scheduleOptions = listOf(
        ScheduleOption(R.drawable.ic_gym, "Gym", ""),
        ScheduleOption(R.drawable.ic_home_exercise, "At home", ""),
        ScheduleOption(R.drawable.ic_work, "At work", ""),
        ScheduleOption(R.drawable.ic_online_live, "Online/Virtual classes", ""),
        ScheduleOption(R.drawable.ic_dont_fix_place, "I don’t have a fixed place", ""),
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): ExerciseLocationFragment {
            val fragment = ExerciseLocationFragment()
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
        val adapter = ScheduleOptionAdapter(scheduleOptions, "MoveRight") { selectedOption ->
            Handler(Looper.getMainLooper()).postDelayed({
                QuestionnaireEatRightActivity.navigateToNextPage()
            }, 500)
        }
        binding.rvScheduleOptions.layoutManager = LinearLayoutManager(requireContext())
        binding.rvScheduleOptions.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
