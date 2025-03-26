package com.example.rlapp.ai_package.ui.eatright.fragment.tab

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.R.*
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.eatright.adapter.tab.FrequentlyLoggedListAdapter
import com.example.rlapp.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.example.rlapp.ai_package.ui.eatright.fragment.tab.frequentlylogged.LogMealFragment
import com.example.rlapp.ai_package.ui.eatright.fragment.tab.frequentlylogged.LoggedBottomSheet
import com.example.rlapp.ai_package.ui.eatright.model.MyMealModel
import com.example.rlapp.databinding.FragmentFrequentlyLoggedBinding
import com.google.android.flexbox.FlexboxLayout

class FrequentlyLoggedFragment : BaseFragment<FragmentFrequentlyLoggedBinding>() {

    private lateinit var frequentlyLoggedRecyclerView : RecyclerView
    private lateinit var layoutNoMeals : LinearLayoutCompat
    private lateinit var layoutCreateMeal : LinearLayoutCompat
    private lateinit var loggedBottomSheetFragment : LoggedBottomSheet
    private lateinit var flexboxLayout : FlexboxLayout
    private lateinit var addDishBottomSheet : LinearLayout
   // private val ingredientsList = mutableListOf("Poha")
    val ingredientsList : ArrayList<MyMealModel> = ArrayList()


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFrequentlyLoggedBinding
        get() = FragmentFrequentlyLoggedBinding::inflate

    private val frequentlyLoggedListAdapter by lazy { FrequentlyLoggedListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onFrequentlyLoggedItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), color.meal_log_background))

        frequentlyLoggedRecyclerView = view.findViewById(R.id.recyclerview_frequently_logged_item)
      //  layoutNoMeals = view.findViewById(R.id.layout_no_meals)
       // layoutCreateMeal = view.findViewById(R.id.layout_create_meal)
        flexboxLayout = view.findViewById(R.id.flexboxLayout)
        val btnAdd: LinearLayoutCompat = view.findViewById(R.id.layout_btnAdd)
        val btnLogMeal: LinearLayoutCompat = view.findViewById(R.id.layout_btnLogMeal)
        val layoutTitle = view.findViewById<LinearLayout>(R.id.layout_title)
        val checkCircle = view.findViewById<ImageView>(R.id.check_circle_icon)
        val loggedSuccess = view.findViewById<TextView>(R.id.tv_logged_success)
         addDishBottomSheet = view.findViewById<LinearLayout>(R.id.layout_add_dish_bottom_sheet)


        frequentlyLoggedRecyclerView.layoutManager = LinearLayoutManager(context)
        frequentlyLoggedRecyclerView.adapter = frequentlyLoggedListAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = YourMealLogsFragment()
                val args = Bundle()

                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        })

        onFrequentlyLoggedItemRefresh()

  //      layoutCreateMeal.setOnClickListener {
//            val fragment = MealLogCalenderFragment()
//            val args = Bundle()
//
//            fragment.arguments = args
//            requireActivity().supportFragmentManager.beginTransaction().apply {
//                replace(R.id.flFragment, fragment, "mealLog")
//                addToBackStack("mealLog")
//                commit()
//            }
  //      }

        updateIngredientChips()

        // Add button clicked (For demonstration, adding a dummy ingredient)
        btnAdd.setOnClickListener {
//            val newIngredient = "New Item ${ingredientsList.size + 1}"
//            ingredientsList.add(newIngredient)
//            updateIngredientChips()
            val fragment = LogMealFragment()
            val args = Bundle()

            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        // Log Meal button click
        btnLogMeal.setOnClickListener {
            //   Toast.makeText(context, "Meal Logged Successfully!", Toast.LENGTH_SHORT).show()
            addDishBottomSheet.visibility = View.GONE
            loggedBottomSheetFragment = LoggedBottomSheet()
            loggedBottomSheetFragment.isCancelable = true
            val bundle = Bundle()
            bundle.putBoolean("test",false)
            loggedBottomSheetFragment.arguments = bundle
            activity?.supportFragmentManager?.let { loggedBottomSheetFragment.show(it, "LoggedBottomSheet") }
        }
    }

    private fun onFrequentlyLoggedItemRefresh (){

        val meal = listOf(
            MyMealModel("Breakfast", "Poha", "1", "1,157", "8", "308", "17", true),
            MyMealModel("Breakfast", "Dal", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Rice", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Roti", "1", "1,157", "8", "308", "17", false)
        )

        if (meal.size > 0){
            frequentlyLoggedRecyclerView.visibility = View.VISIBLE
         //   layoutNoMeals.visibility = View.GONE
        }else{
        //    layoutNoMeals.visibility = View.VISIBLE
            frequentlyLoggedRecyclerView.visibility = View.GONE
        }

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(meal as Collection<MyMealModel>)
        val mealLogDateData: MyMealModel? = null
        frequentlyLoggedListAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onFrequentlyLoggedItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {

        val mealLogs = listOf(
            MyMealModel("Breakfast", "Poha", "1", "1,157", "8", "308", "17", true),
            MyMealModel("Breakfast", "Dal", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Rice", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Roti", "1", "1,157", "8", "308", "17", false)
        )

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        frequentlyLoggedListAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
        addDishBottomSheet.visibility = View.VISIBLE

        val newIngredient = mealLogDateModel
        for (ingredient in valueLists) {
            if (ingredient.isAddDish == true){
                ingredientsList.add(newIngredient)
                updateIngredientChips()
            }
        }
    }

    // Function to update Flexbox with chips
    private fun updateIngredientChips() {
        flexboxLayout.removeAllViews() // Clear existing chips

        for (ingredient in ingredientsList) {
            val chipView = LayoutInflater.from(context).inflate(layout.chip_ingredient, flexboxLayout, false)
            val tvIngredient: TextView = chipView.findViewById(R.id.tvIngredient)
            val btnRemove: ImageView = chipView.findViewById(R.id.btnRemove)

            tvIngredient.text = ingredient.mealName
            btnRemove.setOnClickListener {
                ingredientsList.remove(ingredient)
                updateIngredientChips()
            }
            flexboxLayout.addView(chipView)
        }
    }
}