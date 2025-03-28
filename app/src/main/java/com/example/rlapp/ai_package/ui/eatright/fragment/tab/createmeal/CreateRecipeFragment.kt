package com.example.rlapp.ai_package.ui.eatright.fragment.tab.createmeal

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.model.DailyRecipe
import com.example.rlapp.ai_package.ui.eatright.adapter.MealLogDateListAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.YourBreakfastMealLogsAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.YourDinnerMealLogsAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.YourLunchMealLogsAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.tab.FrequentlyLoggedListAdapter
import com.example.rlapp.ai_package.ui.eatright.fragment.tab.HomeTabMealFragment
import com.example.rlapp.ai_package.ui.eatright.model.BreakfastMealModel
import com.example.rlapp.ai_package.ui.eatright.model.DinnerMealModel
import com.example.rlapp.ai_package.ui.eatright.model.LunchMealModel
import com.example.rlapp.ai_package.ui.eatright.model.MealLogDateModel
import com.example.rlapp.ai_package.ui.eatright.model.MyMealModel
import com.example.rlapp.databinding.FragmentCreateRecipeBinding

class CreateRecipeFragment : BaseFragment<FragmentCreateRecipeBinding>() {

    private lateinit var progressBarConfirmation :ProgressBar
    private lateinit var mealLogDateRecyclerView : RecyclerView
    private lateinit var breakfastMealRecyclerView : RecyclerView
    private lateinit var lunchMealRecyclerView : RecyclerView
    private lateinit var addedIngredientsItemRecyclerview : RecyclerView
    private lateinit var etAddName : EditText
    private lateinit var tvContinue : TextView
    private lateinit var editRecipe : ImageView
    private lateinit var dinnerDotMenu : ImageView
    private lateinit var btnAddLayout : LinearLayoutCompat
    private lateinit var editDeleteBreakfast : CardView
    private lateinit var editDeleteLunch : CardView
    private lateinit var addedRecipeListLayout : LinearLayoutCompat
    private lateinit var saveRecipeLayout : LinearLayoutCompat
    private lateinit var layoutNoIngredients: LinearLayoutCompat
    private lateinit var addRecipeNameLayout : LinearLayoutCompat
    private lateinit var continueLayout : LinearLayoutCompat

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCreateRecipeBinding
        get() = FragmentCreateRecipeBinding::inflate

    private val mealLogDateAdapter by lazy { MealLogDateListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onMealLogDateItem) }
    private val breakfastMealLogsAdapter by lazy { YourBreakfastMealLogsAdapter(requireContext(), arrayListOf(), -1, null, false, :: onBreakfastMealLogItem) }
    private val lunchMealLogsAdapter by lazy { YourLunchMealLogsAdapter(requireContext(), arrayListOf(), -1, null, false, :: onLunchMealLogItem) }
    private val dinnerMealLogsAdapter by lazy { YourDinnerMealLogsAdapter(requireContext(), arrayListOf(), -1, null, false, :: onDinnerMealLogItem) }
    private val frequentlyLoggedListAdapter by lazy { FrequentlyLoggedListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onFrequentlyLoggedItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

      //  view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        addRecipeNameLayout = view.findViewById(R.id.layout_add_recipe_name)
        etAddName = view.findViewById(R.id.et_add_name)
        continueLayout = view.findViewById(R.id.layout_continue)
        tvContinue = view.findViewById(R.id.tv_continue)
        addedIngredientsItemRecyclerview = view.findViewById(R.id.recyclerview_added_ingredients_item)
        layoutNoIngredients = view.findViewById(R.id.layout_noIngredients)
        saveRecipeLayout = view.findViewById(R.id.layout_save_recipe)
        editRecipe = view.findViewById(R.id.ic_edit)
        addedRecipeListLayout = view.findViewById(R.id.layout_added_recipe_list)
        btnAddLayout = view.findViewById(R.id.layout_btnAdd)
//        editDeleteLunch = view.findViewById(R.id.btn_edit_delete_lunch)
//        editDeleteDinner = view.findViewById(R.id.btn_edit_delete_dinner)
//        layoutMain = view.findViewById(R.id.layout_main)
//        layoutDelete = view.findViewById(R.id.layout_delete)
//        layoutViewFood = view.findViewById(R.id.layout_view_food)
  //      val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
     //   val circleIndicator = view.findViewById<View>(R.id.circleIndicator)
        continueLayout.isEnabled = false
        continueLayout.setBackgroundResource(R.drawable.light_green_bg)

        addedIngredientsItemRecyclerview.layoutManager = LinearLayoutManager(context)
        addedIngredientsItemRecyclerview.adapter = frequentlyLoggedListAdapter
//
//        breakfastMealRecyclerView.layoutManager = LinearLayoutManager(context)
//        breakfastMealRecyclerView.adapter = breakfastMealLogsAdapter
//
//        lunchMealRecyclerView.layoutManager = LinearLayoutManager(context)
//        lunchMealRecyclerView.adapter = lunchMealLogsAdapter
//
//        dinnerMealRecyclerView.layoutManager = LinearLayoutManager(context)
//        dinnerMealRecyclerView.adapter = dinnerMealLogsAdapter

        etAddName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                if (s!!.length > 0){
                    continueLayout.isEnabled = true
                    continueLayout.setBackgroundResource(R.drawable.green_meal_bg)
                }else{
                    continueLayout.isEnabled = false
                    continueLayout.setBackgroundResource(R.drawable.light_green_bg)
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = CreateRecipeFragment()
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

        saveRecipeLayout.setOnClickListener {

            val fragment = HomeTabMealFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        continueLayout.setOnClickListener {
            addRecipeNameLayout.visibility = View.GONE
            continueLayout.visibility = View.GONE
            addedRecipeListLayout.visibility = View.VISIBLE
            saveRecipeLayout.visibility = View.VISIBLE

            if (layoutNoIngredients.visibility == View.GONE){
                saveRecipeLayout.isEnabled = true
                saveRecipeLayout.setBackgroundResource(R.drawable.green_meal_bg)
            }else{
                saveRecipeLayout.isEnabled = false
                saveRecipeLayout.setBackgroundResource(R.drawable.light_green_bg)
            }
        }

        editRecipe.setOnClickListener {
            addRecipeNameLayout.visibility = View.VISIBLE
            addedRecipeListLayout.visibility = View.GONE
            saveRecipeLayout.visibility = View.GONE
            continueLayout.visibility = View.VISIBLE
            if (etAddName.text.length > 0){
                continueLayout.isEnabled = true
                continueLayout.setBackgroundResource(R.drawable.green_meal_bg)
            }else{
                continueLayout.isEnabled = false
                continueLayout.setBackgroundResource(R.drawable.light_green_bg)
            }
        }

//        dinnerDotMenu.setOnClickListener {
//            if (editDeleteDinner.visibility == View.GONE){
//              //  layoutMain.setBackgroundColor(Color.parseColor("#0A1214"))
//                editDeleteDinner.visibility = View.VISIBLE
//            }else{
//              //  layoutMain.setBackgroundColor(Color.parseColor("#F0FFFA"))
//                editDeleteDinner.visibility = View.GONE
//            }
//        }

        btnAddLayout.setOnClickListener {
            val fragment = SearchDishFragment()
            val args = Bundle()
            args.putString("searchType", "createRecipe")
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }
    }

    private fun onFrequentlyLoggedItemRefresh (){

        val meal = listOf(
            MyMealModel("Breakfast", "Poha", "1", "1,157", "8", "308", "17", true),
            MyMealModel("Breakfast", "Dal", "1", "1,157", "8", "308", "17", false)
        )

        if (meal.size > 0){
            addedIngredientsItemRecyclerview.visibility = View.VISIBLE
            layoutNoIngredients.visibility = View.GONE
            saveRecipeLayout.visibility = View.VISIBLE
        }else{
            layoutNoIngredients.visibility = View.VISIBLE
            addedIngredientsItemRecyclerview.visibility = View.GONE
            saveRecipeLayout.visibility = View.GONE
        }

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(meal as Collection<MyMealModel>)
        val mealLogDateData: MyMealModel? = null
        frequentlyLoggedListAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onFrequentlyLoggedItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {

        val mealLogs = listOf(
            MyMealModel("Breakfast", "Poha", "1", "1,157", "8", "308", "17", true),
            MyMealModel("Breakfast", "Dal", "1", "1,157", "8", "308", "17", false)
        )

        val valueLists : ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MyMealModel>)
        frequentlyLoggedListAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun onLunchMealLogItemRefresh() {
        val mealLogs = listOf(
            LunchMealModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            LunchMealModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )
        val valueLists : ArrayList<LunchMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<LunchMealModel>)
        val lunchMealData: LunchMealModel? = null
      //  lunchMealLogsAdapter.addAll(valueLists, -1, lunchMealData, false)
    }

    private fun onDinnerMealLogItemRefresh() {
        val mealLogs = listOf(
            DinnerMealModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            DinnerMealModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )
        val valueLists : ArrayList<DinnerMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<DinnerMealModel>)
        val dinnerMealData: DinnerMealModel? = null
      //  dinnerMealLogsAdapter.addAll(valueLists, -1, dinnerMealData, false)
    }

    private fun onMealLogDateItem(mealLogDateModel: DailyRecipe, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            MealLogDateModel("01", "M", true),
            MealLogDateModel("02", "T", false),
            MealLogDateModel("03", "W", true),
            MealLogDateModel("04", "T", false),
            MealLogDateModel("05", "F", true),
            MealLogDateModel("06", "S", true),
            MealLogDateModel("07", "S", true)
        )
        val valueLists : ArrayList<DailyRecipe> = ArrayList()
        valueLists.addAll(mealLogs as Collection<DailyRecipe>)
        mealLogDateAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun onBreakfastMealLogItem(mealLogDateModel: BreakfastMealModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            BreakfastMealModel("Breakfast", "Poha", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            BreakfastMealModel("Breakfast", "Apple", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
        )
        val valueLists : ArrayList<BreakfastMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<BreakfastMealModel>)
       // breakfastMealLogsAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun onLunchMealLogItem(mealLogDateModel: LunchMealModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            LunchMealModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            LunchMealModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )
        val valueLists : ArrayList<LunchMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<LunchMealModel>)
     //   lunchMealLogsAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun onDinnerMealLogItem(mealLogDateModel: DinnerMealModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            LunchMealModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            DinnerMealModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )
        val valueLists : ArrayList<DinnerMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<DinnerMealModel>)
      //  dinnerMealLogsAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

//    private fun deleteMealDialog(){
//
//        deleteBottomSheetFragment = DeleteMealBottomSheet()
//        deleteBottomSheetFragment.isCancelable = true
//        val bundle = Bundle()
//        bundle.putBoolean("test",false)
//        deleteBottomSheetFragment.arguments = bundle
//        activity?.supportFragmentManager?.let { deleteBottomSheetFragment.show(it, "DeleteMealBottomSheet") }
//    }
}