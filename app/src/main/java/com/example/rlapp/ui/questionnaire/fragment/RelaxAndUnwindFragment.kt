package com.example.rlapp.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rlapp.R
import com.example.rlapp.databinding.FragmentRelaxAndUnwindBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireThinkRightActivity
import com.example.rlapp.ui.questionnaire.adapter.RelaxAndWindAdapter
import com.example.rlapp.ui.questionnaire.pojo.Question
import com.example.rlapp.ui.questionnaire.pojo.StressReason
import com.example.rlapp.ui.questionnaire.pojo.TRQuestionSeven
import com.example.rlapp.ui.questionnaire.pojo.TRQuestionSix

class RelaxAndUnwindFragment : Fragment() {

    private var _binding: FragmentRelaxAndUnwindBinding? = null
    private val binding get() = _binding!!

    private val selectedList: ArrayList<StressReason> = ArrayList()
    private lateinit var adapter: RelaxAndWindAdapter
    private val reasonList = listOf(
        StressReason("Listening to music or podcasts", R.drawable.ic_rau_1),
        StressReason("Watching TV shows or movies", R.drawable.ic_rau_2),
        StressReason("Spending time with family or friends", R.drawable.ic_rau_3),
        StressReason("Exercising or doing yoga", R.drawable.ic_rau_4),
        StressReason("Reading a book or journaling", R.drawable.ic_rau_5),
        StressReason("Medicating or practising mindfulness", R.drawable.ic_rau_6),
        StressReason("Playing Games(video or board games)", R.drawable.ic_rau_7),
        StressReason("Indulging in hobbies(art, gardening, etc) ", R.drawable.ic_rau_8),
        StressReason("Going for walking or being in nature", R.drawable.ic_rau_9),
        StressReason("Browsing social media or online shopping", R.drawable.ic_rau_10)
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): RelaxAndUnwindFragment {
            val fragment = RelaxAndUnwindFragment()
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
        _binding = FragmentRelaxAndUnwindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = RelaxAndWindAdapter(reasonList) { selectedItem ->
            // Handle selected items here
            if (selectedItem.isSelected)
                selectedList.add(selectedItem)
            else
                selectedList.remove(selectedItem)

        }
        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.recyclerView.adapter = adapter

        binding.btnContinue.setOnClickListener {
            if (selectedList.isNotEmpty())
                submit(selectedList[0].title)
                //QuestionnaireThinkRightActivity.navigateToNextPage()
            else
                Toast.makeText(requireContext(), "Please select at least one", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    private fun submit(answer: String) {
        val questionSeven = TRQuestionSeven()
        questionSeven.answer = answer
        QuestionnaireThinkRightActivity.thinkRightAnswerRequest.questionSeven = questionSeven
        QuestionnaireThinkRightActivity.submitThinkRightRightAnswerRequest(
            QuestionnaireThinkRightActivity.thinkRightAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}