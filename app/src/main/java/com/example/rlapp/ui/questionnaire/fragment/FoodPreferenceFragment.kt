package com.example.rlapp.ui.questionnaire.fragment

import FoodOptionAdapter
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import com.example.rlapp.R
import com.example.rlapp.databinding.FragmentFoodPreferenceBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireEatRightActivity
import com.example.rlapp.ui.questionnaire.pojo.FoodOption
import com.example.rlapp.ui.questionnaire.pojo.Question

class FoodPreferenceFragment : Fragment() {

    private var _binding: FragmentFoodPreferenceBinding? = null
    private val binding get() = _binding!!

    private val foodOptions = arrayListOf(
        FoodOption("Vegan", "", R.drawable.ic_vegan),
        FoodOption("Vegetarian", "", R.drawable.ic_vegeterian),
        FoodOption("Eggetarian", "(Eggs Only)", R.drawable.ic_eggs),
        FoodOption("Pescatarian", "(Fish And Vegetarian)", R.drawable.ic_chicken),
        FoodOption("Non-Vegetarian", "(Chicken And Fish Only)", R.drawable.ic_meat),
        FoodOption("Non-Vegetarian", "(All Types Of Meat)", R.drawable.ic_fish)
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

        val adapter = FoodOptionAdapter(foodOptions) { selectedOption ->
            Handler(Looper.getMainLooper()).postDelayed({
                QuestionnaireEatRightActivity.navigateToNextPage()
            }, 500)
        }

        binding.rvFoodOptions.layoutManager = GridLayoutManager(requireContext(), 2)
        binding.rvFoodOptions.adapter = adapter
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
