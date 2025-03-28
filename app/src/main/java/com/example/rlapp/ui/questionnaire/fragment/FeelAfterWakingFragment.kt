package com.example.rlapp.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rlapp.R
import com.example.rlapp.databinding.FragmentFeelAfterWakingBinding
import com.example.rlapp.ui.jounal.new_journal.JournalMoodAdapter
import com.example.rlapp.ui.jounal.new_journal.Mood
import com.example.rlapp.ui.questionnaire.QuestionnaireThinkRightActivity
import com.example.rlapp.ui.questionnaire.pojo.Question

class FeelAfterWakingFragment : Fragment() {

    private var _binding: FragmentFeelAfterWakingBinding? = null
    private val binding get() = _binding!!

    private var question: Question? = null

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
            QuestionnaireThinkRightActivity.navigateToNextPage()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun setUpMoodList() {
        val moodList = listOf(
            Mood("Refreshed", R.drawable.ic_happy),
            Mood("Rested", R.drawable.ic_relaxed),
            Mood("Neutral", R.drawable.ic_unsure),
            Mood("Exhausted", R.drawable.ic_stressed),
            Mood("Drained", R.drawable.ic_sad)
        )

        val adapter = JournalMoodAdapter(moodList, -1) { selectedMood ->
            when (selectedMood.name) {
                "Refreshed" -> binding.ivSelectedImage.setImageResource(R.drawable.ic_happy)
                "Rested" -> binding.ivSelectedImage.setImageResource(R.drawable.ic_relaxed)
                "Neutral" -> binding.ivSelectedImage.setImageResource(R.drawable.ic_unsure)
                "Exhausted" -> binding.ivSelectedImage.setImageResource(R.drawable.ic_stressed)
                "Drained" -> binding.ivSelectedImage.setImageResource(R.drawable.ic_sad)
            }

        }

        binding.moodRecyclerView.adapter = adapter
        binding.moodRecyclerView.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
    }
}