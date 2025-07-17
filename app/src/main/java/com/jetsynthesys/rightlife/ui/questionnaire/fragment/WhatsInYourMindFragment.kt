package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.BottomsheetAwakeNightBinding
import com.jetsynthesys.rightlife.databinding.FragmentWhatsInYourMindBinding
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireThinkRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.adapter.StressReasonAdapter
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.StressReason
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.TRQuestionThree
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds

class WhatsInYourMindFragment : Fragment() {

    private var _binding: FragmentWhatsInYourMindBinding? = null
    private val binding get() = _binding!!
    private val selectedList: ArrayList<StressReason> = ArrayList()

    private lateinit var adapter: StressReasonAdapter
    private val reasonList = listOf(
        StressReason("Work Stress", R.drawable.ic_stress_reason_1),
        StressReason("Money Problems", R.drawable.ic_stress_reason_2),
        StressReason("Relationship Troubles", R.drawable.ic_stress_reason_3),
        StressReason("Health Issues", R.drawable.ic_stress_reason_4),
        StressReason("Family Duties", R.drawable.ic_stress_reason_5),
        StressReason("Personal Goals And Expectations", R.drawable.ic_stress_reason_6),
        StressReason("Social Media Pressure", R.drawable.ic_stress_reason_7),
        StressReason("Fear Of Failure", R.drawable.ic_stress_reason_8),
        StressReason("Something else", R.drawable.ic_stress_reason_9),
        StressReason("None", R.drawable.ic_stress_reason_10)
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): WhatsInYourMindFragment {
            val fragment = WhatsInYourMindFragment()
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
        _binding = FragmentWhatsInYourMindBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        adapter = StressReasonAdapter(reasonList) { selectedItem ->
            // Handle selected items here
            if (selectedItem.isSelected)
                selectedList.add(selectedItem)
            else
                selectedList.remove(selectedItem)

        }
        binding.recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.recyclerView.adapter = adapter

        binding.btnContinue.setOnClickListener {
            if (selectedList.isNotEmpty()) {
                binding.btnContinue.disableViewForSeconds()
                showAwakeNightBottomSheet()
            } else
                Toast.makeText(requireContext(), "Please select at least one", Toast.LENGTH_SHORT)
                    .show()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun showAwakeNightBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetAwakeNightBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.btnNO.setOnClickListener {
            dialogBinding.btnNO.disableViewForSeconds()
            bottomSheetDialog.dismiss()
            submit(selectedList[0].title)
            //QuestionnaireThinkRightActivity.navigateToNextPage()
        }

        dialogBinding.btnYes.setOnClickListener {
            dialogBinding.btnYes.disableViewForSeconds()
            bottomSheetDialog.dismiss()
            //QuestionnaireThinkRightActivity.navigateToNextPage()
            submit(selectedList[0].title)
        }
        bottomSheetDialog.show()
    }

    private fun submit(answer: String) {
        val questionThree = TRQuestionThree()
        questionThree.answer = answer
        QuestionnaireThinkRightActivity.questionnaireAnswerRequest.thinkRight?.questionThree =
            questionThree
        QuestionnaireThinkRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireThinkRightActivity.questionnaireAnswerRequest
        )
    }
}