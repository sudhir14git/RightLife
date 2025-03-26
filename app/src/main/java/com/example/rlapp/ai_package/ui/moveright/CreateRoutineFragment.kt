package com.example.rlapp.ai_package.ui.moveright

import android.graphics.drawable.Drawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.adapter.CreateRoutineListAdapter
import com.example.rlapp.ai_package.ui.eatright.model.MyMealModel
import com.example.rlapp.databinding.FragmentCreateRoutineBinding

class CreateRoutineFragment : BaseFragment<FragmentCreateRoutineBinding>() {
    private lateinit var editText : EditText
    private lateinit var textViewRoutine : TextView
    private lateinit var createRoutineRecyclerView : RecyclerView
    private lateinit var layoutBtnLog : LinearLayoutCompat
    private lateinit var addNameLayout : ConstraintLayout
    private lateinit var createListRoutineLayout : ConstraintLayout

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCreateRoutineBinding
        get() = FragmentCreateRoutineBinding::inflate
    private val myMealListAdapter by lazy { CreateRoutineListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onMealLogDateItem) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.gradient_color_background_workout)

        createRoutineRecyclerView = view.findViewById(R.id.recyclerview_my_meals_item)

        editText = view.findViewById(R.id.editText)
        textViewRoutine = view.findViewById(R.id.name_routine_text_view)
        layoutBtnLog  = view.findViewById(R.id.layout_btn_log)
        addNameLayout = view.findViewById(R.id.add_name_layout)
        createListRoutineLayout = view.findViewById(R.id.list_create_routine_layout)
        addNameLayout.visibility = View.VISIBLE
        createListRoutineLayout.visibility = View.GONE
        val defaultBackground: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.add_cart_button_background)
        val filledBackground: Drawable? = ContextCompat.getDrawable(requireContext(), R.drawable.button_background_filled)
        createRoutineRecyclerView.layoutManager = LinearLayoutManager(context)
        createRoutineRecyclerView.adapter = myMealListAdapter
        onMyMealItemRefresh()

        editText.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s.isNullOrEmpty()) {
                    layoutBtnLog.background = filledBackground
                    layoutBtnLog.isEnabled = false
                } else {
                    layoutBtnLog.background = defaultBackground
                    layoutBtnLog.isEnabled = true
                }
            }
        })
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToFragment(YourworkOutsFragment(), "LandingFragment")

            }
        })
        layoutBtnLog.setOnClickListener {
            addNameLayout.visibility = View.GONE
            createListRoutineLayout.visibility = View.VISIBLE
            textViewRoutine.text = editText.text


        }

    }
    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }
    private fun onMyMealItemRefresh (){

        val meal = listOf(
            MyMealModel("Functional Strength Training", "Poha, Sev", "min", "337", "134", "308", "17", false),
            MyMealModel("Functional Strength Training", "Poha, Sev", "min", "337", "134", "308", "17", false)
        )

        if (meal.size > 0){
            createRoutineRecyclerView.visibility = View.VISIBLE
            //layoutNoMeals.visibility = View.GONE
        }else{
            // layoutNoMeals.visibility = View.VISIBLE
            createRoutineRecyclerView.visibility = View.GONE
        }

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(meal as Collection<MyMealModel>)
        val mealLogDateData: MyMealModel? = null
        myMealListAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMealLogDateItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {

        val mealLogs = listOf(
            MyMealModel("Functional Strength Training", "Poha, Sev", "min", "337", "134", "308", "17", false),
            MyMealModel("Functional Strength Training", "Poha, Sev", "min", "337", "134", "308", "17",false)
        )

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        //  mealLogDateAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

}