package com.example.rlapp.ai_package.ui.eatright.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.eatright.adapter.MacroNutientsListAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.MicroNutientsListAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.YourDinnerMealLogsAdapter
import com.example.rlapp.ai_package.ui.eatright.adapter.YourLunchMealLogsAdapter
import com.example.rlapp.ai_package.ui.eatright.model.BreakfastMealModel
import com.example.rlapp.ai_package.ui.eatright.model.DinnerMealModel
import com.example.rlapp.ai_package.ui.eatright.model.LunchMealModel
import com.example.rlapp.ai_package.ui.eatright.model.MacroNutrientsModel
import com.example.rlapp.ai_package.ui.eatright.model.MicroNutrientsModel
import com.example.rlapp.databinding.FragmentViewMealInsightsBinding

class ViewMealInsightsFragment : BaseFragment<FragmentViewMealInsightsBinding>() {

    private lateinit var progressBarConfirmation :ProgressBar
    private lateinit var macroItemRecyclerView : RecyclerView
    private lateinit var microItemRecyclerView : RecyclerView
    private lateinit var lunchMealRecyclerView : RecyclerView
    private lateinit var dinnerMealRecyclerView : RecyclerView
    private lateinit var imageCalender : ImageView
    private lateinit var breakfastDotMenu : ImageView
    private lateinit var lunchDotMenu : ImageView
    private lateinit var dinnerDotMenu : ImageView
    private lateinit var btnLogMeal : LinearLayoutCompat
    private lateinit var editDeleteBreakfast : CardView
    private lateinit var editDeleteLunch : CardView
    private lateinit var editDeleteDinner : CardView
    private lateinit var layoutMain : ConstraintLayout
    private lateinit var deleteBottomSheetFragment: DeleteMealBottomSheet
    private lateinit var layoutDelete :LinearLayoutCompat

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentViewMealInsightsBinding
        get() = FragmentViewMealInsightsBinding::inflate

    private val macroNutientsAdapter by lazy { MacroNutientsListAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMacroNutrientsItem) }
    private val microNutientsAdapter by lazy { MicroNutientsListAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMicroNutrientsItem) }
    private val lunchMealLogsAdapter by lazy { YourLunchMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onLunchMealLogItem) }
    private val dinnerMealLogsAdapter by lazy { YourDinnerMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onDinnerMealLogItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        macroItemRecyclerView = view.findViewById(R.id.recyclerview_macro_item)
        microItemRecyclerView = view.findViewById(R.id.recyclerview_micro_item)
//        lunchMealRecyclerView = view.findViewById(R.id.recyclerview_lunch_meals_item)
//        dinnerMealRecyclerView = view.findViewById(R.id.recyclerview_dinner_meals_item)
//        imageCalender = view.findViewById(R.id.image_calender)
//        btnLogMeal = view.findViewById(R.id.layout_btn_log_meal)
//        breakfastDotMenu = view.findViewById(R.id.image_dot_menu)
//        lunchDotMenu = view.findViewById(R.id.image_lunch_dot_menu)
//        dinnerDotMenu = view.findViewById(R.id.image_dinner_dot_menu)
//        editDeleteBreakfast = view.findViewById(R.id.btn_edit_delete)
//        editDeleteLunch = view.findViewById(R.id.btn_edit_delete_lunch)
//        editDeleteDinner = view.findViewById(R.id.btn_edit_delete_dinner)
//        layoutMain = view.findViewById(R.id.layout_main)
//        layoutDelete = view.findViewById(R.id.layout_delete)
//        val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
     //   val circleIndicator = view.findViewById<View>(R.id.circleIndicator)
 //       val transparentOverlay = view.findViewById<View>(R.id.transparentOverlay)

//        progressBar.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
//            override fun onGlobalLayout() {
//                progressBar.viewTreeObserver.removeOnGlobalLayoutListener(this)
//                val progressBarWidth = progressBar.width.toFloat()
//                val overlayPosition = 0.7f * progressBarWidth
//                val progress = 1285
//                val max = progressBar.max
//                val circlePosition = (progress.toFloat() / max) * progressBarWidth
////                val circleRadius = circleIndicator.width / 2f
////                circleIndicator.x = circlePosition - circleRadius
////                circleIndicator.y = progressBar.y + (progressBar.height - circleIndicator.height) / 2f
//                val overlayRadius = transparentOverlay.width / 2f
//                transparentOverlay.x = overlayPosition - overlayRadius
//                transparentOverlay.y = progressBar.y + (progressBar.height - transparentOverlay.height) / 2f
//            }
//        })

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

        onMacroNutrientsItemRefresh()
        onMicroNutrientsItemRefresh()
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
    }

    private fun onMacroNutrientsItemRefresh (){

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
    }

    private fun onMicroNutrientsItemRefresh (){

        val mealLogs = listOf(
            MicroNutrientsModel("0.1", "mg", "Vitamin B", R.drawable.ic_cal),
            MicroNutrientsModel("3", "mg", "Iron", R.drawable.ic_proteins),
            MicroNutrientsModel("25", "mg", "Magnesium", R.drawable.ic_carbs),
            MicroNutrientsModel("65", "mg", "Phasphorus", R.drawable.ic_fats),
            MicroNutrientsModel("150", "mg", "Potassium", R.drawable.ic_fats),
            MicroNutrientsModel("1", "mg", "Zinc", R.drawable.ic_fats)
        )

        val valueLists : ArrayList<MicroNutrientsModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MicroNutrientsModel>)
        val mealLogDateData: MicroNutrientsModel? = null
        microNutientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
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
       // dinnerMealLogsAdapter.addAll(valueLists, -1, dinnerMealData, false)
    }

    private fun onMacroNutrientsItem(macroNutrientsModel: MacroNutrientsModel, position: Int, isRefresh: Boolean) {
        val macroNutrients = listOf(
            MacroNutrientsModel("1285", "kcal", "calorie", R.drawable.ic_cal),
            MacroNutrientsModel("11", "g", "protien", R.drawable.ic_proteins),
            MacroNutrientsModel("338", "g", "carbs", R.drawable.ic_carbs),
            MacroNutrientsModel("25", "g", "fats", R.drawable.ic_fats),
        )
        val valueLists : ArrayList<MacroNutrientsModel> = ArrayList()
        valueLists.addAll(macroNutrients as Collection<MacroNutrientsModel>)
        macroNutientsAdapter.addAll(valueLists, position, macroNutrientsModel, isRefresh)
    }

    private fun onMicroNutrientsItem(microNutrientsModel: MicroNutrientsModel, position: Int, isRefresh: Boolean) {

        val microNutrients = listOf(
            MicroNutrientsModel("0.1", "mg", "Vitamin B", R.drawable.ic_cal),
            MicroNutrientsModel("3", "mg", "Iron", R.drawable.ic_proteins),
            MicroNutrientsModel("25", "mg", "Magnesium", R.drawable.ic_carbs),
            MicroNutrientsModel("65", "mg", "Phasphorus", R.drawable.ic_fats),
            MicroNutrientsModel("150", "mg", "Potassium", R.drawable.ic_fats),
            MicroNutrientsModel("1", "mg", "Zinc", R.drawable.ic_fats)
        )
        val valueLists : ArrayList<MicroNutrientsModel> = ArrayList()
        valueLists.addAll(microNutrients as Collection<MicroNutrientsModel>)
        microNutientsAdapter.addAll(valueLists, position, microNutrientsModel, isRefresh)
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
      //  lunchMealLogsAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun onDinnerMealLogItem(mealLogDateModel: DinnerMealModel, position: Int, isRefresh: Boolean) {
        val mealLogs = listOf(
            LunchMealModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17"),
            DinnerMealModel("Dinner", "Dal,Rice,Chapati,Spinach,Paneer,Gulab Jamun", "Vegeterian" ,"25", "1", "1,157", "8", "308", "17")
        )
        val valueLists : ArrayList<DinnerMealModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<DinnerMealModel>)
       // dinnerMealLogsAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun deleteMealDialog(){

        deleteBottomSheetFragment = DeleteMealBottomSheet()
        deleteBottomSheetFragment.isCancelable = true
        val bundle = Bundle()
        bundle.putBoolean("test",false)
        deleteBottomSheetFragment.arguments = bundle
        activity?.supportFragmentManager?.let { deleteBottomSheetFragment.show(it, "DeleteMealBottomSheet") }
    }
}