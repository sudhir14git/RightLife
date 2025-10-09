package com.jetsynthesys.rightlife.ui.questionnaire.fragment

import FoodOptionAdapter
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.FragmentFoodPreferenceBinding
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireEatRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.ERQuestionOne
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.FoodOption
import com.jetsynthesys.rightlife.ui.questionnaire.pojo.Question
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.runWithCooldown

class FoodPreferenceFragment : Fragment() {

    private var _binding: FragmentFoodPreferenceBinding? = null
    private val binding get() = _binding!!

    private val foodOptions = arrayListOf(
        FoodOption("Vegan", "(No Animal Products)", R.drawable.ic_vegan),
        FoodOption("Vegetarian", "(No Meat or Fish)", R.drawable.ic_vegeterian),
        FoodOption("Eggetarian", "(Vegetarian + Eggs)", R.drawable.ic_eggs),
        FoodOption("Pescatarian", " (Vegetarian + Fish)", R.drawable.ic_chicken),
        FoodOption("Non-Vegetarian", "(Chicken & Fish, No Red Meat)", R.drawable.ic_meat),
        FoodOption("Non-Vegetarian", "(All Meats)", R.drawable.ic_fish)
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): FoodPreferenceFragment {
            val fragment = FoodPreferenceFragment()
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
        _binding = FragmentFoodPreferenceBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val adapter = FoodOptionAdapter(foodOptions, { selectedOption: FoodOption ->
            submit(selectedOption.title)
        }.runWithCooldown())

        binding.rvFoodOptions.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvFoodOptions.adapter = adapter
    }

    private fun submit(answer: String) {
        val questionOne = ERQuestionOne()
        questionOne.answer = answer
        QuestionnaireEatRightActivity.questionnaireAnswerRequest.eatRight?.questionOne = questionOne
        QuestionnaireEatRightActivity.submitQuestionnaireAnswerRequest(
            QuestionnaireEatRightActivity.questionnaireAnswerRequest
        )

        CommonAPICall.updateChecklistStatus(
            requireContext(),
            "discover_eating",
            AppConstants.CHECKLIST_INPROGRESS
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
