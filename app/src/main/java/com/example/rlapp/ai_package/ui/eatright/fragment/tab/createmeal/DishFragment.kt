package com.example.rlapp.ai_package.ui.eatright.fragment.tab.createmeal

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.eatright.adapter.MacroNutientsListAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.YourDinnerMealLogsAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.YourLunchMealLogsAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.tab.FrequentlyLoggedListAdapter
import com.example.rlapp.ai_package.ui.eatright.model.BreakfastMealModel
import com.example.rlapp.ai_package.ui.eatright.model.DinnerMealModel
import com.example.rlapp.ai_package.ui.eatright.model.LunchMealModel
import com.example.rlapp.ai_package.ui.eatright.model.MacroNutrientsModel
import com.example.rlapp.ai_package.ui.eatright.model.MyMealModel
import com.example.rlapp.databinding.FragmentDishBinding
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DishFragment : BaseFragment<FragmentDishBinding>() {

    private lateinit var progressBarConfirmation :ProgressBar
    private lateinit var macroItemRecyclerView : RecyclerView
    private lateinit var microItemRecyclerView : RecyclerView
    private lateinit var lunchMealRecyclerView : RecyclerView
    private lateinit var dinnerMealRecyclerView : RecyclerView
    private lateinit var addToTheMealLayout : LinearLayoutCompat
    private lateinit var imageCalender : ImageView
    private lateinit var breakfastDotMenu : ImageView
    private lateinit var lunchDotMenu : ImageView
    private lateinit var imgFood : ImageView
    private lateinit var btnLogMeal : LinearLayoutCompat
    private lateinit var editDeleteBreakfast : CardView
    private lateinit var tvMealName : TextView
    private lateinit var addToTheMealTV : TextView
    private lateinit var layoutMain : ConstraintLayout
    private lateinit var searchType : String
    private lateinit var recipeName : String
    private lateinit var recipeImage : String

    private lateinit var tvCheckOutRecipe: TextView
    private lateinit var tvChange: TextView
    private lateinit var tvQuantity: TextView
    private var quantity = 1
    private lateinit var tvMeasure :TextView

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDishBinding
        get() = FragmentDishBinding::inflate

    private val macroNutientsAdapter by lazy { MacroNutientsListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onMealLogDateItem) }
    private val microNutientsAdapter by lazy { MacroNutientsListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onMealLogDateItem) }
    private val lunchMealLogsAdapter by lazy { YourLunchMealLogsAdapter(requireContext(), arrayListOf(), -1, null, false, :: onLunchMealLogItem) }
    private val dinnerMealLogsAdapter by lazy { YourDinnerMealLogsAdapter(requireContext(), arrayListOf(), -1, null, false, :: onDinnerMealLogItem) }
    private val frequentlyLoggedListAdapter by lazy { FrequentlyLoggedListAdapter(requireContext(), arrayListOf(), -1, null, false, :: onFrequentlyLoggedItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        macroItemRecyclerView = view.findViewById(R.id.recyclerview_macro_item)
        microItemRecyclerView = view.findViewById(R.id.recyclerview_micro_item)
        addToTheMealLayout = view.findViewById(R.id.layout_addToTheMeal)
        tvCheckOutRecipe = view.findViewById(R.id.tv_CheckOutRecipe)
        tvChange = view.findViewById(R.id.tv_change)
        tvQuantity = view.findViewById(R.id.tvQuantity)
        tvMeasure = view.findViewById(R.id.tvMeasure)
        addToTheMealTV = view.findViewById(R.id.tv_addToTheMeal)
        tvMealName = view.findViewById(R.id.tvMealName)
        imgFood = view.findViewById(R.id.imgFood)

        searchType = arguments?.getString("searchType").toString()
        recipeName = arguments?.getString("recipeName").toString()
        recipeImage = arguments?.getString("recipeImage").toString()

//        view.findViewById<ImageView>(R.id.ivDatePicker).setOnClickListener {
//            // Open Date Picker
//            showDatePicker()
//        }

//        view.findViewById<ImageView>(R.id.ivMealDropdown).setOnClickListener {
//            // Open Meal Selection Dialog
//            showMealSelection()
//        }

//        view.findViewById<ImageView>(R.id.ivDecrease).setOnClickListener {
//            if (quantity > 1) {
//                quantity--
//                tvQuantity.text = quantity.toString()
//            }
//        }

        view.findViewById<ImageView>(R.id.ivIncrease).setOnClickListener {
            quantity++
            tvQuantity.text = quantity.toString()
        }

        macroItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        macroItemRecyclerView.adapter = macroNutientsAdapter
//
        microItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        microItemRecyclerView.adapter = microNutientsAdapter
//
//        lunchMealRecyclerView.layoutManager = LinearLayoutManager(context)
//        lunchMealRecyclerView.adapter = lunchMealLogsAdapter
//
//        dinnerMealRecyclerView.layoutManager = LinearLayoutManager(context)
//        dinnerMealRecyclerView.adapter = dinnerMealLogsAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = SearchDishFragment()
                val args = Bundle()

                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        })

        setDishData()

        onMealLogDateItemRefresh()
//        onBreakfastMealLogItemRefresh()
//        onLunchMealLogItemRefresh()
//        onDinnerMealLogItemRefresh()
//
//        imageCalender.setOnClickListener {
//            val fragment = MealLogCalenderFragment()
//            val args = Bundle()
//
//            fragment.arguments = args
//            requireActivity().supportFragmentManager.beginTransaction().apply {
//                replace(R.id.flFragment, fragment, "mealLog")
//                addToBackStack("mealLog")
//                commit()
//            }
//        }
//
//        btnLogMeal.setOnClickListener {
//            val fragment = HomeTabMealFragment()
//            val args = Bundle()
//            fragment.arguments = args
//            requireActivity().supportFragmentManager.beginTransaction().apply {
//                replace(R.id.flFragment, fragment, "mealLog")
//                addToBackStack("mealLog")
//                commit()
//            }
//        }
//
//        breakfastDotMenu.setOnClickListener {
//            if (editDeleteBreakfast.visibility == View.GONE){
//                //layoutMain.setBackgroundColor(Color.parseColor("#0A1214"))
//                editDeleteBreakfast.visibility = View.VISIBLE
//            }else{
//               // layoutMain.setBackgroundColor(Color.parseColor("#F0FFFA"))
//                editDeleteBreakfast.visibility = View.GONE
//            }
//        }
//
//        lunchDotMenu.setOnClickListener {
//            if (editDeleteLunch.visibility == View.GONE){
//              //  layoutMain.setBackgroundColor(Color.parseColor("#0A1214"))
//                editDeleteLunch.visibility = View.VISIBLE
//            }else{
//              //  layoutMain.setBackgroundColor(Color.parseColor("#F0FFFA"))
//                editDeleteLunch.visibility = View.GONE
//            }
//        }
//
//        dinnerDotMenu.setOnClickListener {
//            if (editDeleteDinner.visibility == View.GONE){
//              //  layoutMain.setBackgroundColor(Color.parseColor("#0A1214"))
//                editDeleteDinner.visibility = View.VISIBLE
//            }else{
//              //  layoutMain.setBackgroundColor(Color.parseColor("#F0FFFA"))
//                editDeleteDinner.visibility = View.GONE
//            }
//        }
//
//        layoutDelete.setOnClickListener {
//            deleteMealDialog()
//        }
        onFrequentlyLoggedItemRefresh()
    }

    private fun setDishData(){
        if (searchType.contentEquals("createRecipe")){
            addToTheMealTV.text = "Add To The Recipe"
        }else{
            addToTheMealTV.text = "Add To The Meal"
            tvMealName.text = recipeName
            Glide.with(this)
                .load(recipeImage)
                .placeholder(R.drawable.ic_breakfast)
                .error(R.drawable.ic_breakfast)
                .into(imgFood)
        }
    }

    private fun onMealLogDateItemRefresh (){

        val mealLogs = listOf(
            MacroNutrientsModel("1285", "kcal", "calorie", R.drawable.ic_cal),
            MacroNutrientsModel("11", "g", "protien", R.drawable.ic_proteins),
            MacroNutrientsModel("338", "g", "carbs", R.drawable.ic_carbs),
            MacroNutrientsModel("25", "g", "fats", R.drawable.ic_fats),
        )

        val valueLists : ArrayList<MacroNutrientsModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MacroNutrientsModel>)
        val mealLogDateData: MacroNutrientsModel? = null
        macroNutientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
        microNutientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onFrequentlyLoggedItemRefresh (){

        val meal = listOf(
            MyMealModel("Breakfast", "Poha", "1", "1,157", "8", "308", "17", true),
            MyMealModel("Breakfast", "Dal", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Rice", "1", "1,157", "8", "308", "17", false),
            MyMealModel("Breakfast", "Roti", "1", "1,157", "8", "308", "17", false)
        )

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
    }

    private fun onBreakfastMealLogItemRefresh() {
        val mealLogs = listOf(
            BreakfastMealModel("Breakfast", "Poha", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            BreakfastMealModel("Breakfast", "Apple", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
        )
        val valueLists : ArrayList<BreakfastMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<BreakfastMealModel>)
        val breakfastMealData: BreakfastMealModel? = null
      //  macroNutientsAdapter.addAll(valueLists, -1, breakfastMealData, false)
    }

    private fun onLunchMealLogItemRefresh() {
        val mealLogs = listOf(
            LunchMealModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            LunchMealModel("Lunch", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )
        val valueLists : ArrayList<LunchMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<LunchMealModel>)
        val lunchMealData: LunchMealModel? = null
    //    lunchMealLogsAdapter.addAll(valueLists, -1, lunchMealData, false)
    }

    private fun onDinnerMealLogItemRefresh() {
        val mealLogs = listOf(
            DinnerMealModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            DinnerMealModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )
        val valueLists : ArrayList<DinnerMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<DinnerMealModel>)
        val dinnerMealData: DinnerMealModel? = null
     //   dinnerMealLogsAdapter.addAll(valueLists, -1, dinnerMealData, false)
    }

    private fun onMealLogDateItem(mealLogDateModel: MacroNutrientsModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            MacroNutrientsModel("1285", "kcal", "calorie", R.drawable.ic_cal),
            MacroNutrientsModel("11", "g", "protien", R.drawable.ic_proteins),
            MacroNutrientsModel("338", "g", "carbs", R.drawable.ic_carbs),
            MacroNutrientsModel("25", "g", "fats", R.drawable.ic_fats),
        )
        val valueLists : ArrayList<MacroNutrientsModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MacroNutrientsModel>)
        macroNutientsAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
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
     //   dinnerMealLogsAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

//    private fun showDatePicker() {
//        val calendar = Calendar.getInstance()
//        val datePicker = DatePickerDialog(
//            context!!, { _, year, month, dayOfMonth ->
//                val date = "$dayOfMonth ${getMonthName(month)} $year"
//                tvSelectedDate.text = date
//            },
//            calendar.get(Calendar.YEAR),
//            calendar.get(Calendar.MONTH),
//            calendar.get(Calendar.DAY_OF_MONTH)
//        )
//        datePicker.show()
//    }

    private fun getMonthName(month: Int): String {
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(Date(0, month, 0))
    }

//    private fun showMealSelection() {
//        val meals = arrayOf("Breakfast", "Lunch", "Dinner")
//        val builder = AlertDialog.Builder(context!!)
//        builder.setItems(meals) { _, which ->
//            tvMealType.text = meals[which]
//        }
//        builder.show()
//    }
}