package com.example.rlapp.ui.questionnaire.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.rlapp.R
import com.example.rlapp.databinding.FragmentServingBinding
import com.example.rlapp.ui.questionnaire.QuestionnaireEatRightActivity
import com.example.rlapp.ui.questionnaire.adapter.ServingAdapter
import com.example.rlapp.ui.questionnaire.pojo.AnswerFruitVegetable
import com.example.rlapp.ui.questionnaire.pojo.ERQuestionFour
import com.example.rlapp.ui.questionnaire.pojo.ERQuestionThree
import com.example.rlapp.ui.questionnaire.pojo.Fruits
import com.example.rlapp.ui.questionnaire.pojo.Question
import com.example.rlapp.ui.questionnaire.pojo.ServingItem
import com.example.rlapp.ui.questionnaire.pojo.Vegetables

class FoodServingFragment : Fragment() {

    private var _binding: FragmentServingBinding? = null
    private val binding get() = _binding!!
    private var fruitCount = 0
    private var vegetableCount = 0

    private val foodList = arrayListOf(
        ServingItem(R.drawable.ic_fruits, "Fruits", "Serving per day", 1),
        ServingItem(R.drawable.ic_vegitables, "Vegetables", "Per day", 1)
    )

    private var question: Question? = null

    companion object {
        fun newInstance(question: Question): FoodServingFragment {
            val fragment = FoodServingFragment()
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
        _binding = FragmentServingBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        val adapter = ServingAdapter(foodList) { servingItem ->
            when (servingItem.title) {
                "Fruits" -> fruitCount = servingItem.count
                "Vegetables" -> vegetableCount = servingItem.count
            }
        }
        binding.recyclerViewServing.adapter = adapter
        binding.recyclerViewServing.layoutManager = LinearLayoutManager(requireContext())

        binding.btnContinue.setOnClickListener {
            //QuestionnaireEatRightActivity.navigateToNextPage()
            val answerFruitVegetable = AnswerFruitVegetable()
            val vegetable = Vegetables()
            vegetable.servings = vegetableCount.toString()
            answerFruitVegetable.vegetables = vegetable
            val fruit = Fruits()
            fruit.servings = fruitCount.toString()
            answerFruitVegetable.fruits = fruit
            submit(answerFruitVegetable)
        }
    }

    private fun submit(answer: AnswerFruitVegetable) {
        val questionFour = ERQuestionFour()
        questionFour.answer = answer
        QuestionnaireEatRightActivity.eatRightAnswerRequest.questionFour = questionFour
        QuestionnaireEatRightActivity.submitEatRightAnswerRequest(
            QuestionnaireEatRightActivity.eatRightAnswerRequest
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
