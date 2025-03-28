package com.example.rlapp.ui.questionnaire.fragment

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.rlapp.databinding.FragmentActiveDuringSessionBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireEatRightActivity
import com.example.rlapp.ui.questionnaire.pojo.Question
import com.shawnlin.numberpicker.NumberPicker
import java.util.Locale

class ActiveDuringSessionsFragment : Fragment() {
    private var selectedActiveTime: String = ""
    private var _binding: FragmentActiveDuringSessionBinding? = null
    private val binding get() = _binding!!

    private val activeTimes = arrayOf(
        "<15 mins",
        "15-30 mins",
        "30-60 mins",
        "60-90 mins",
        ">90 mins"
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): ActiveDuringSessionsFragment {
            val fragment = ActiveDuringSessionsFragment()
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
        _binding = FragmentActiveDuringSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.numberPicker.minValue = 1
        binding.numberPicker.maxValue = activeTimes.size
        binding.numberPicker.displayedValues = activeTimes
        binding.numberPicker.value = 3
        binding.numberPicker.wheelItemCount = 5

        selectedActiveTime = activeTimes[2]

        // OnScrollListener
        binding.numberPicker.setOnScrollListener { view, scrollState ->
            if (scrollState == NumberPicker.OnScrollListener.SCROLL_STATE_IDLE) {
                Log.d(
                    ContentValues.TAG,
                    String.format(Locale.US, "newVal: %d", view.value)
                )
                Log.d("Selected age : ", activeTimes[view.value - 1] + "")
                selectedActiveTime = activeTimes[view.value - 1]
            }
        }

        binding.btnContinue.setOnClickListener {
            QuestionnaireEatRightActivity.navigateToNextPage()
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}