package com.example.rlapp.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rlapp.R
import com.example.rlapp.databinding.FragmentBeforeGoingToBedBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireThinkRightActivity
import com.example.rlapp.ui.questionnaire.adapter.RelaxAndWindAdapter
import com.example.rlapp.ui.questionnaire.pojo.Question
import com.example.rlapp.ui.questionnaire.pojo.StressReason

class BeforeGoingToBedFragment : Fragment() {

    private var _binding: FragmentBeforeGoingToBedBinding? = null
    private val binding get() = _binding!!

    private val selectedList: ArrayList<StressReason> = ArrayList()
    private lateinit var adapter: RelaxAndWindAdapter
    private val reasonList = listOf(
        StressReason("Reading a book", R.drawable.ic_before_g_b_1),
        StressReason("Scrolling through social media", R.drawable.ic_before_g_b_2),
        StressReason("Watching tv or online videos", R.drawable.ic_before_g_b_3),
        StressReason("Meditation or mindfulness", R.drawable.ic_before_g_b_4),
        StressReason("Journaling", R.drawable.ic_before_g_b_5),
        StressReason("Listening to Music", R.drawable.ic_before_g_b_6),
        StressReason("Having Caffeine", R.drawable.ic_before_g_b_7),
        StressReason("I don't have a specific bedtime routine", R.drawable.ic_before_g_b_8),
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): BeforeGoingToBedFragment {
            val fragment = BeforeGoingToBedFragment()
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
        _binding = FragmentBeforeGoingToBedBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = RelaxAndWindAdapter(reasonList, "SleepRight") { selectedItem ->
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
                QuestionnaireThinkRightActivity.navigateToNextPage()
            else
                Toast.makeText(requireContext(), "Please select at least one", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}