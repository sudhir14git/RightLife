package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import android.media.ExifInterface
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.CheckBox
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.ScanMealNutritionResponse
import com.jetsynthesys.rightlife.ai_package.model.request.SnapDish
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealLogItem
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateSnapMealRequest
import com.jetsynthesys.rightlife.ai_package.model.response.Macros
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.model.response.Micros
import com.jetsynthesys.rightlife.ai_package.model.response.Nutrients
import com.jetsynthesys.rightlife.ai_package.model.response.SearchResultItem
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealLogResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.RatingMealBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.SnapMealScanResultAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MacroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MicroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.HomeTabMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal.SearchDishFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MacroNutrientsModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MicroNutrientsModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SnapDishLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.databinding.FragmentMealScanResultsBinding
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MealScanResultFragment : BaseFragment<FragmentMealScanResultsBinding>(),
    RatingMealBottomSheet.RatingSnapMealListener {

    private lateinit var macroItemRecyclerView: RecyclerView
    private lateinit var microItemRecyclerView: RecyclerView
    private lateinit var frequentlyLoggedRecyclerView: RecyclerView
    private var currentPhotoPath: String = ""
    private lateinit var descriptionName: String
    private lateinit var foodNameEdit: EditText
    private var currentPhotoPathsecound: Uri? = null
    private lateinit var tvFoodName: EditText
    private lateinit var imageFood: ImageView
    private lateinit var tvQuantity: TextView
    private lateinit var tvSelectedDate: TextView
    private lateinit var addToLogLayout: LinearLayoutCompat
    private lateinit var saveMealLayout: LinearLayoutCompat
    private lateinit var layoutMicroTitle: ConstraintLayout
    private lateinit var layoutMacroTitle: ConstraintLayout
    private lateinit var icMacroUP: ImageView
    private lateinit var backButton: ImageView
    private lateinit var microUP: ImageView
    private lateinit var addLayout: LinearLayoutCompat
    private lateinit var checkBox: CheckBox
    private lateinit var deleteSnapMealBottomSheet: DeleteSnapMealBottomSheet
    private var snapDishLocalListModel: SnapDishLocalListModel? = null
    private var snapRecipesList: ArrayList<SearchResultItem> = ArrayList()
    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    private lateinit var selectedMealType: String
    private var mealId: String = ""
    private var mealName: String = ""
    private var moduleName: String = ""
    private var quantity = 1
    private var loadingOverlay: FrameLayout? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMealScanResultsBinding
        get() = FragmentMealScanResultsBinding::inflate

    private val microNutrientsAdapter by lazy {
        MicroNutrientsAdapter(
            requireContext(), arrayListOf(), -1,
            null, false, ::onMicroNutrientsItem
        )
    }
    private val mealListAdapter by lazy {
        SnapMealScanResultAdapter(
            requireContext(), arrayListOf(), -1,
            null, false, ::onMenuEditItem, ::onMenuDeleteItem
        )
    }

    private val macroNutrientsAdapter by lazy {
        MacroNutrientsAdapter(
            requireContext(), arrayListOf(), -1,
            null, false, ::onMealLogDateItem
        )
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
        macroItemRecyclerView = view.findViewById(R.id.recyclerview_macro_item)
        microItemRecyclerView = view.findViewById(R.id.recyclerview_micro_item)
        frequentlyLoggedRecyclerView = view.findViewById(R.id.recyclerview_frequently_logged_item)
        var btnChange = view.findViewById<TextView>(R.id.change_btn)
        foodNameEdit = view.findViewById(R.id.foodNameEdit)
        imageFood = view.findViewById(R.id.imageFood)
        tvQuantity = view.findViewById(R.id.tvQuantity)
        tvSelectedDate = view.findViewById(R.id.tvSelectedDate)
        addToLogLayout = view.findViewById(R.id.addToLogLayout)
        saveMealLayout = view.findViewById(R.id.saveMealLayout)
        val spinner: Spinner = view.findViewById(R.id.spinner)
        layoutMicroTitle = view.findViewById(R.id.layoutMicroTitle)
        layoutMacroTitle = view.findViewById(R.id.layoutMacroTitle)
        microUP = view.findViewById(R.id.microUP)
        icMacroUP = view.findViewById(R.id.icMacroUP)
        addLayout = view.findViewById(R.id.addLayout)
        checkBox = view.findViewById(R.id.saveMealCheckBox)
        backButton = view.findViewById(R.id.backButton)
        frequentlyLoggedRecyclerView.layoutManager = LinearLayoutManager(context)
        frequentlyLoggedRecyclerView.adapter = mealListAdapter
        macroItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        macroItemRecyclerView.adapter = macroNutrientsAdapter
        microItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        microItemRecyclerView.adapter = microNutrientsAdapter
        val states = arrayOf(
            intArrayOf(android.R.attr.state_checked),
            intArrayOf(-android.R.attr.state_checked)
        )
        val colors = intArrayOf(
            ContextCompat.getColor(requireContext(), R.color.dark_green),   // when checked
            ContextCompat.getColor(requireContext(), R.color.dark_gray)  // when not checked
        )
        val colorStateList = ColorStateList(states, colors)
        checkBox.buttonTintList = colorStateList

        moduleName = arguments?.getString("ModuleName").toString()
        mealId = arguments?.getString("mealId").toString()
        mealName = arguments?.getString("mealName").toString()

        val dishLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", SnapDishLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapDishLocalListModel")
        }

        val foodDataResponses = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("foodDataResponses", ScanMealNutritionResponse::class.java)
        } else {
            arguments?.getParcelable("foodDataResponses")
        }

        currentPhotoPath = arguments?.get("ImagePath").toString()
        val imagePathString = arguments?.getString("ImagePathsecound")
        if (imagePathString != null) {
            currentPhotoPathsecound = imagePathString.let { Uri.parse(it) }!!
        } else {
            currentPhotoPathsecound = null
        }

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatFullDate = DateTimeFormatter.ofPattern("d MMMM yyyy")
        tvSelectedDate.text = currentDateTime.format(formatFullDate)

        //currentPhotoPathsecound = arguments?.get("ImagePathsecound") as Uri
        descriptionName = arguments?.getString("description").toString()
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                if (mealId != "null" && mealId != null) {
                    updateSnapMealsSave((snapRecipesList))
                } else {
                    ratingMealLogDialog(true)
                }
            }
        }

        if (dishLocalListModels != null) {
            snapDishLocalListModel = dishLocalListModels
            snapRecipesList.addAll(snapDishLocalListModel!!.data)
            onFrequentlyLoggedItemRefresh(snapRecipesList)
            onMicroNutrientsList(snapRecipesList)
            onMacroNutrientsList(snapRecipesList)
            setFoodDataFromDish(snapDishLocalListModel!!)
        }

        // Data for Spinner
        val items = arrayOf("Breakfast", "Morning Snack", "Lunch", "Evening Snacks", "Dinner")
        // Create Adapter
        val adapter =
            ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_dropdown_item, items)
        spinner.adapter = adapter

        // Handle item selection
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>,
                view: View?,
                position: Int,
                id: Long
            ) {
                val selectedItem = parent.getItemAtPosition(position).toString()
                selectedMealType = selectedItem
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                //  selectedText.text = "No selection"
            }
        }

        view.findViewById<LinearLayoutCompat>(R.id.datePickerLayout).setOnClickListener {
            // Open Date Picker
            showDatePicker()
        }

//        view.findViewById<ImageView>(R.id.ivDecrease).setOnClickListener {
//            if (quantity > 1) {
//                quantity--
//                tvQuantity.text = quantity.toString()
//            }
//        }
//            view.findViewById<ImageView>(R.id.ivIncrease).setOnClickListener {
//                quantity++
//                tvQuantity.text = quantity.toString()
//            }

        if (foodDataResponses?.data != null) {
            setFoodData(foodDataResponses)

            val snapRecipesListScan: ArrayList<SearchResultItem> = ArrayList()
            if (foodDataResponses.data.size > 0) {
                val items = foodDataResponses.data
                items.forEach { foodData ->
                    val macrosData = Macros(
                        Calories = foodData.selected_portion_nutrition.calories_kcal,
                        Carbs = foodData.selected_portion_nutrition.carb_g,
                        Fats = foodData.selected_portion_nutrition.fat_g,
                        Protein = foodData.selected_portion_nutrition.protein_g
                    )
                    val microsData = Micros(
                        Cholesterol = foodData.selected_portion_nutrition.cholesterol_mg,
                        Vitamin_A = foodData.selected_portion_nutrition.vitamin_a_mcg,
                        Vitamin_C = foodData.selected_portion_nutrition.vitamin_c_mg,
                        Vitamin_K = foodData.selected_portion_nutrition.vitamin_k_mcg,
                        Vitamin_D = foodData.selected_portion_nutrition.vitamin_d_iu,
                        Folate = foodData.selected_portion_nutrition.folate_mcg,
                        Iron = foodData.selected_portion_nutrition.iron_mg,
                        Calcium = foodData.selected_portion_nutrition.calcium_mg,
                        Magnesium = foodData.selected_portion_nutrition.magnesium_mg,
                        Potassium = foodData.selected_portion_nutrition.potassium_mg,
                        Fiber = foodData.selected_portion_nutrition.fiber_g,
                        Zinc = foodData.selected_portion_nutrition.zinc_mg,
                        Sodium = foodData.selected_portion_nutrition.sodium_mg,
                        Sugar = foodData.selected_portion_nutrition.sugar_g,
                        b12_mcg = foodData.selected_portion_nutrition.b12_mcg,
                        b1_mg = foodData.selected_portion_nutrition.b1_mg,
                        b2_mg = foodData.selected_portion_nutrition.b2_mg,
                        b5_mg = foodData.selected_portion_nutrition.b5_mg,
                        b3_mg = foodData.selected_portion_nutrition.b3_mg,
                        b6_mg = foodData.selected_portion_nutrition.b6_mg,
                        vitamin_e_mg = foodData.selected_portion_nutrition.vitamin_e_mg,
                        omega_3_fatty_acids_g = foodData.selected_portion_nutrition.omega_3_fatty_acids_g,
                        omega_6_fatty_acids_g = foodData.selected_portion_nutrition.omega_6_fatty_acids_g,
                        copper_mg = foodData.selected_portion_nutrition.copper_mg,
                        phosphorus_mg = foodData.selected_portion_nutrition.phosphorus_mg,
                        saturated_fats_g = foodData.selected_portion_nutrition.saturated_fats_g,
                        selenium_mcg = foodData.selected_portion_nutrition.selenium_mcg,
                        trans_fats_g = foodData.selected_portion_nutrition.trans_fats_g,
                        polyunsaturated_g = foodData.selected_portion_nutrition.polyunsaturated_g,
                        is_beverage = foodData.selected_portion_nutrition.is_beverage,
                        mass_g = foodData.selected_portion_nutrition.mass_g,
                        monounsaturated_g = foodData.selected_portion_nutrition.monounsaturated_g,
                        percent_fruit = foodData.selected_portion_nutrition.percent_fruit,
                        percent_vegetable = foodData.selected_portion_nutrition.percent_vegetable,
                        percent_legume_or_nuts = foodData.selected_portion_nutrition.percent_legume_or_nuts,
                        source_urls = foodData.selected_portion_nutrition.source_urls
                    )
                    val nutrientsData = Nutrients(
                        macros = macrosData,
                        micros = microsData
                    )
                    val snapRecipeData = SearchResultItem(
                        id = "",
                        name = foodData.name,
                        category = "",
                        photo_url = "",
                        servings = 0,
                        cooking_time_in_seconds = 0,
                        calories = 0.0,
                        nutrients = nutrientsData,
                        source = "",
                        unit = "",
                        mealQuantity = 0.0
                    )
                    snapRecipesListScan.add(snapRecipeData)
                }

                snapRecipesList = snapRecipesListScan
                onFrequentlyLoggedItemRefresh(snapRecipesList)
                onMicroNutrientsList(snapRecipesList)
                onMacroNutrientsList(snapRecipesList)
                snapDishLocalListModel = SnapDishLocalListModel(snapRecipesList)
            }
        }

        backButton.setOnClickListener {
            if (moduleName.contentEquals("EatRight")) {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    val snapMealFragment = HomeBottomTabFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    snapMealFragment.arguments = args
                    replace(R.id.flFragment, snapMealFragment, "Steps")
                    addToBackStack(null)
                    commit()
                }
            } else {
                startActivity(Intent(context, HomeNewActivity::class.java))
                requireActivity().finish()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (moduleName.contentEquals("EatRight")) {
                        requireActivity().supportFragmentManager.beginTransaction().apply {
                            val snapMealFragment = HomeBottomTabFragment()
                            val args = Bundle()
                            args.putString("ModuleName", moduleName)
                            snapMealFragment.arguments = args
                            replace(R.id.flFragment, snapMealFragment, "Steps")
                            addToBackStack(null)
                            commit()
                        }
                    } else {
                        startActivity(Intent(context, HomeNewActivity::class.java))
                        requireActivity().finish()
                    }
                }
            })

        btnChange.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = MealSearchFragment()
                val args = Bundle()
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        addLayout.setOnClickListener {
            val fragment = SearchDishFragment()
            val args = Bundle()
            args.putString("searchType", "mealScanResult")
            args.putString("ModuleName", moduleName)
            args.putString("mealId", mealId)
            args.putString("mealName", mealName)
            args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
            args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        saveMealLayout.setOnClickListener {
            Toast.makeText(context, "Save Meal", Toast.LENGTH_SHORT).show()
            if (moduleName.contentEquals("EatRight")) {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    val snapMealFragment = HomeBottomTabFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    snapMealFragment.arguments = args
                    replace(R.id.flFragment, snapMealFragment, "Steps")
                    addToBackStack(null)
                    commit()
                }
            } else {
                startActivity(Intent(context, HomeNewActivity::class.java))
                requireActivity().finish()
            }
        }

        addToLogLayout.setOnClickListener {
            //  Toast.makeText(context, "Added To Log", Toast.LENGTH_SHORT).show()
            if (moduleName.contentEquals("EatRight")) {
                // ratingMealLogDialog()
                // sharedPreferenceManager.setFirstTimeUserForSnapMealRating(true)
                if (mealId != "null" && mealId != null) {
                    updateSnapMealsSave((snapRecipesList))
                } else {
                    ratingMealLogDialog(false)
                }
//                requireActivity().supportFragmentManager.beginTransaction().apply {
//                    val snapMealFragment = HomeBottomTabFragment()
//                    val args = Bundle()
//                    args.putString("ModuleName", moduleName)
//                    snapMealFragment.arguments = args
//                    replace(R.id.flFragment, snapMealFragment, "Steps")
//                    addToBackStack(null)
//                    commit()
//                }
            } else {
                ratingMealLogDialog(false)
            }
        }

        layoutMacroTitle.setOnClickListener {
            if (macroItemRecyclerView.isVisible) {
                macroItemRecyclerView.visibility = View.GONE
                icMacroUP.setImageResource(R.drawable.ic_down)
                view.findViewById<View>(R.id.view_macro).visibility = View.GONE
            } else {
                macroItemRecyclerView.visibility = View.VISIBLE
                icMacroUP.setImageResource(R.drawable.ic_up)
                view.findViewById<View>(R.id.view_macro).visibility = View.VISIBLE
            }
        }

        layoutMicroTitle.setOnClickListener {
            if (microItemRecyclerView.isVisible) {
                microItemRecyclerView.visibility = View.GONE
                microUP.setImageResource(R.drawable.ic_down)
                view.findViewById<View>(R.id.view_micro).visibility = View.GONE
            } else {
                microItemRecyclerView.visibility = View.VISIBLE
                microUP.setImageResource(R.drawable.ic_up)
                view.findViewById<View>(R.id.view_micro).visibility = View.VISIBLE
            }
        }
    }

    private fun onMicroNutrientsList(nutrition: ArrayList<SearchResultItem>) {

        var totalVitaminD = 0.0
        var totalB12 = 0.0
        var totalFolate = 0.0
        var totalVitaminC = 0.0
        var totalVitaminA = 0.0
        var totalVitaminK = 0.0
        var totalIron = 0.0
        var totalCalcium = 0.0
        var totalMagnesium = 0.0
        var totalZinc = 0.0
        var totalOmega3 = 0.0
        var totalSodium = 0.0
        var totalCholesterol = 0.0
        var totalSugar = 0.0
        var totalPhosphorus = 0.0
        var totalPotassium = 0.0

        nutrition.forEach { item ->
            totalVitaminD += (item.nutrients.micros.Vitamin_D ?: 0.0)
            totalB12 += (item.nutrients.micros.b12_mcg ?: 0.0)
            totalFolate += (item.nutrients.micros.Folate ?: 0.0)
            totalVitaminC += (item.nutrients.micros.Vitamin_C ?: 0.0)
            totalVitaminA += (item.nutrients.micros.Vitamin_A ?: 0.0)
            totalVitaminK += (item.nutrients.micros.Vitamin_K ?: 0.0)
            totalIron += (item.nutrients.micros.Iron ?: 0.0)
            totalCalcium += (item.nutrients.micros.Calcium ?: 0.0)
            totalMagnesium += (item.nutrients.micros.Magnesium ?: 0.0)
            totalZinc += (item.nutrients.micros.Zinc ?: 0.0)
            totalOmega3 += (item.nutrients.micros.omega_3_fatty_acids_g ?: 0.0)
            totalSodium += (item.nutrients.micros.Sodium ?: 0.0)
            totalCholesterol += (item.nutrients.micros.Cholesterol ?: 0.0)
            totalSugar += (item.nutrients.micros.Sugar ?: 0.0)
            totalPhosphorus += (item.nutrients.micros.phosphorus_mg ?: 0.0)
            totalPotassium += (item.nutrients.micros.Potassium ?: 0.0)
        }


        val vitaminD = if (totalVitaminD != null) {
            String.format("%.1f", totalVitaminD)
        } else {
            "0"
        }

        val b12_mcg = if (totalB12 != null) {
            String.format("%.1f", totalB12)
        } else {
            "0"
        }

        val folate = if (totalFolate != null) {
            String.format("%.1f", totalFolate)
        } else {
            "0"
        }

        val vitaminC = if (totalVitaminC != null) {
            String.format("%.1f", totalVitaminC)
        } else {
            "0"
        }

        val vitaminA = if (totalVitaminA != null) {
            String.format("%.1f", totalVitaminA)
        } else {
            "0"
        }

        val vitaminK = if (totalVitaminK != null) {
            String.format("%.1f", totalVitaminK)
        } else {
            "0"
        }

        val iron_mg = if (totalIron != null) {
            String.format("%.1f", totalIron)
        } else {
            "0"
        }

        val calcium = if (totalCalcium != null) {
            String.format("%.1f", totalCalcium)
        } else {
            "0"
        }

        val magnesium_mg = if (totalMagnesium != null) {
            String.format("%.1f", totalMagnesium)
        } else {
            "0"
        }

        val zinc_mg = if (totalZinc != null) {
            String.format("%.1f", totalZinc)
        } else {
            "0"
        }

        val omega3 = if (totalOmega3 != null) {
            String.format("%.1f", totalOmega3)
        } else {
            "0"
        }

        val sodium = if (totalSodium != null) {
            String.format("%.1f", totalSodium)
        } else {
            "0"
        }

        val cholesterol = if (totalCholesterol != null) {
            String.format("%.1f", totalCholesterol)
        } else {
            "0"
        }

        val sugar = if (totalSugar != null) {
            String.format("%.1f", totalSugar)
        } else {
            "0"
        }

        val phosphorus_mg = if (totalPhosphorus != null) {
            String.format("%.1f", totalPhosphorus)
        } else {
            "0"
        }
        val potassium_mg = if (totalPotassium != null) {
            String.format("%.1f", totalPotassium)
        } else {
            "0"
        }

        val mealLogs = listOf(
//            MicroNutrientsModel(phosphorus_mg, "mg", "Phasphorus", R.drawable.ic_fats),
//            MicroNutrientsModel(potassium_mg, "mg", "Potassium", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminD, "μg", "Vitamin D", R.drawable.ic_fats),
            MicroNutrientsModel(b12_mcg, "μg", "Vitamin B12", R.drawable.ic_fats),
            MicroNutrientsModel(folate, "μg", "Folate", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminC, "mg", "Vitamin C", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminA, "IU", "Vitamin A", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminK, "μg", "Vitamin K", R.drawable.ic_fats),
            MicroNutrientsModel(iron_mg, "mg", "Iron", R.drawable.ic_fats),
            MicroNutrientsModel(calcium, "mg", "Calcium", R.drawable.ic_fats),
            MicroNutrientsModel(magnesium_mg, "mg", "Magnesium", R.drawable.ic_fats),
            MicroNutrientsModel(zinc_mg, "mg", "Zinc", R.drawable.ic_fats),
            MicroNutrientsModel(omega3, "mg", "Omega-3", R.drawable.ic_fats),
            MicroNutrientsModel(sodium, "mg", "Sodium", R.drawable.ic_fats),
            MicroNutrientsModel(cholesterol, "mg", "Cholesterol", R.drawable.ic_fats),
            MicroNutrientsModel(sugar, "g", "Sugar", R.drawable.ic_fats)
        )
        val valueLists: ArrayList<MicroNutrientsModel> = ArrayList()
        // valueLists.addAll(mealLogs as Collection<MicroNutrientsModel>)
        for (item in mealLogs) {
            if (item.nutrientsValue != "0.0") {
                valueLists.add(item)
            }
        }
        val mealLogDateData: MicroNutrientsModel? = null
        microNutrientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMicroNutrientsItem(
        microNutrientsModel: MicroNutrientsModel,
        position: Int,
        isRefresh: Boolean
    ) {

//        val microNutrients = listOf(
//            MicroNutrientsModel("NA", "mg", "Vitamin B", R.drawable.ic_cal),
//            MicroNutrientsModel("NA", "mg", "Iron", R.drawable.ic_cabs),
//            MicroNutrientsModel("NA", "mg", "Magnesium", R.drawable.ic_protein),
//            MicroNutrientsModel("NA", "mg", "Phasphorus", R.drawable.ic_fats),
//            MicroNutrientsModel("NA", "mg", "Potassium", R.drawable.ic_fats),
//            MicroNutrientsModel("NA", "mg", "Zinc", R.drawable.ic_fats)
//        )
//        val valueLists : ArrayList<MicroNutrientsModel> = ArrayList()
//        valueLists.addAll(microNutrients as Collection<MicroNutrientsModel>)
//        microNutientsAdapter.addAll(valueLists, position, microNutrientsModel, isRefresh)
    }

    private fun onMealLogDateItem(
        mealLogDateModel: MacroNutrientsModel,
        position: Int,
        isRefresh: Boolean
    ) {
//        val mealLogs = listOf(
//            MacroNutrientsModel("1285", "kcal", "calorie", R.drawable.ic_cal),
//            MacroNutrientsModel("11", "g", "protien", R.drawable.ic_cabs),
//            MacroNutrientsModel("338", "g", "carbs", R.drawable.ic_protein),
//            MacroNutrientsModel("25", "g", "fats", R.drawable.ic_fats),
//        )
//        val valueLists : ArrayList<MacroNutrientsModel> = ArrayList()
//        valueLists.addAll(mealLogs as Collection<MacroNutrientsModel>)
//        macroNutientsAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
    }

    private fun onMacroNutrientsList(nutritionList: ArrayList<SearchResultItem>) {

        val totalCalories = nutritionList.sumOf { it.nutrients.macros.Calories ?: 0.0 }
        val totalProtein = nutritionList.sumOf { it.nutrients.macros.Protein ?: 0.0 }
        val totalCarbs = nutritionList.sumOf { it.nutrients.macros.Carbs ?: 0.0 }
        val totalFat = nutritionList.sumOf { it.nutrients.macros.Fats ?: 0.0 }

        val calories_kcal: String = String.format("%.1f", totalCalories)
        val protein_g: String = String.format("%.1f", totalProtein)
        val carb_g: String = String.format("%.1f", totalCarbs)
        val fat_g: String = String.format("%.1f", totalFat)

        val mealLogs = listOf(
            MacroNutrientsModel(calories_kcal, "kcal", "Calorie", R.drawable.ic_cal),
            MacroNutrientsModel(protein_g, "g", "Protein", R.drawable.ic_protein),
            MacroNutrientsModel(carb_g, "g", "Carbs", R.drawable.ic_cabs),
            MacroNutrientsModel(fat_g, "g", "Fats", R.drawable.ic_fats),
        )

        val valueLists: ArrayList<MacroNutrientsModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MacroNutrientsModel>)
        val mealLogDateData: MacroNutrientsModel? = null
        macroNutrientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    private fun onFrequentlyLoggedItemRefresh(recipes: List<SearchResultItem>) {
        if (recipes.size > 0) {
            frequentlyLoggedRecyclerView.visibility = View.VISIBLE
            //   layoutNoMeals.visibility = View.GONE
        } else {
            //    layoutNoMeals.visibility = View.VISIBLE
            frequentlyLoggedRecyclerView.visibility = View.GONE
        }
        val valueLists: ArrayList<SearchResultItem> = ArrayList()
        valueLists.addAll(recipes as Collection<SearchResultItem>)
        val mealLogDateData: SearchResultItem? = null
        mealListAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMenuEditItem(
        snapRecipeData: SearchResultItem,
        position: Int,
        isRefresh: Boolean
    ) {

        requireActivity().supportFragmentManager.beginTransaction().apply {
            val snapMealFragment = SnapDishFragment()
            val args = Bundle()
            args.putString("mealId", mealId)
            args.putString("mealName", mealName)
            args.putString("ModuleName", moduleName)
            args.putString("searchType", "MealScanResult")
            args.putString("mealQuantity", snapRecipeData.mealQuantity.toString())
            args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
            args.putString("snapRecipeName", snapRecipeData.name)
            args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
            snapMealFragment.arguments = args
            replace(R.id.flFragment, snapMealFragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun onMenuDeleteItem(
        snapRecipeData: SearchResultItem,
        position: Int,
        isRefresh: Boolean
    ) {
        deleteSnapMealBottomSheet = DeleteSnapMealBottomSheet()
        deleteSnapMealBottomSheet.isCancelable = true
        val args = Bundle()
        args.putBoolean("test", false)
        args.putString("ModuleName", moduleName)
        args.putString("mealId", mealId)
        args.putString("mealName", mealName)
        args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
        args.putString("snapRecipeName", snapRecipeData.name)
        args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
        deleteSnapMealBottomSheet.arguments = args
        activity?.supportFragmentManager?.let {
            deleteSnapMealBottomSheet.show(
                it,
                "DeleteMealBottomSheet"
            )
        }
    }

    private fun setFoodData(nutritionResponse: ScanMealNutritionResponse) {
        if (nutritionResponse.data != null) {
            if (currentPhotoPathsecound != null) {
                try {
                    val path = getRealPathFromURI(requireContext(), currentPhotoPathsecound!!)
                    if (path != null) {
                        val scaledBitmap = decodeAndScaleBitmap(path, 1080, 1080)
                        if (scaledBitmap != null) {
                            val rotatedBitmap = rotateImageIfRequired(
                                requireContext(), scaledBitmap,
                                currentPhotoPathsecound!!
                            )
                            imageFood.visibility = View.VISIBLE
                            imageFood.setImageBitmap(rotatedBitmap)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed to decode image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.e("ImageCapture", "File does not exist at $currentPhotoPath")
                    }
                } catch (e: Exception) {
                    Log.e("ImageLoad", "Error loading image from file path: $currentPhotoPath", e)
                }
            }
            // Set food name
            if (nutritionResponse.data.size > 0) {
                val capitalized =
                    nutritionResponse.data.get(0).name.replaceFirstChar { it.uppercase() }
                foodNameEdit.setText(capitalized)
            }
        }
    }

    private fun setFoodDataFromDish(snapRecipeData: SnapDishLocalListModel) {
        if (snapRecipeData.data != null) {
            if (currentPhotoPathsecound != null) {
                try {
                    val path = getRealPathFromURI(requireContext(), currentPhotoPathsecound!!)
                    if (path != null) {
                        val scaledBitmap = decodeAndScaleBitmap(path, 1080, 1080)
                        if (scaledBitmap != null) {
                            val rotatedBitmap = rotateImageIfRequired(
                                requireContext(), scaledBitmap,
                                currentPhotoPathsecound!!
                            )
                            imageFood.visibility = View.VISIBLE
                            imageFood.setImageBitmap(rotatedBitmap)
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Failed to decode image",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Log.e("ImageCapture", "File does not exist at $currentPhotoPath")
                    }
                } catch (e: Exception) {
                    Log.e("ImageLoad", "Error loading image from file path: $currentPhotoPath", e)
                }
            }
            // Set food name
            if (snapRecipeData.data.size > 0) {
                val capitalized =
                    snapRecipeData.data.get(0).name.toString().replaceFirstChar { it.uppercase() }
                foodNameEdit.setText(capitalized)
            }
        }
    }

    private fun decodeAndScaleBitmap(filePath: String, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
        }
        BitmapFactory.decodeFile(filePath, options)
        options.inSampleSize = calculateInSampleSize(options, reqWidth, reqHeight)
        options.inJustDecodeBounds = false
        return BitmapFactory.decodeFile(filePath, options)
    }

    fun getFormattedDate(): String {
        val date = Date()
        val formatter = SimpleDateFormat("d MMMM yyyy", Locale.ENGLISH)
        return formatter.format(date)
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1
        if (height > reqHeight || width > reqWidth) {
            val halfHeight = height / 2
            val halfWidth = width / 2
            while ((halfHeight / inSampleSize) >= reqHeight && (halfWidth / inSampleSize) >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    private fun rotateImageIfRequired(context: Context, bitmap: Bitmap, imageUri: Uri): Bitmap {
        val inputStream = context.contentResolver.openInputStream(imageUri)
        val exifInterface = inputStream?.let { ExifInterface(it) }
        val orientation = exifInterface?.getAttributeInt(
            ExifInterface.TAG_ORIENTATION,
            ExifInterface.ORIENTATION_UNDEFINED
        )
        inputStream?.close()
        return when (orientation) {
            ExifInterface.ORIENTATION_ROTATE_90 -> rotateImage(bitmap, 90f)
            ExifInterface.ORIENTATION_ROTATE_180 -> rotateImage(bitmap, 180f)
            ExifInterface.ORIENTATION_ROTATE_270 -> rotateImage(bitmap, 270f)
            else -> bitmap
        }
    }

    fun getRealPathFromURI(context: Context, uri: Uri): String? {
        var filePath: String? = null
        // Try getting path from content resolver
        if (uri.scheme == "content") {
            val projection = arrayOf(MediaStore.Images.Media.DATA)
            val cursor = context.contentResolver.query(uri, projection, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndexOrThrow(MediaStore.Images.Media.DATA)
                    filePath = it.getString(columnIndex)
                }
            }
        } else if (uri.scheme == "file") {
            filePath = uri.path
        }
        return filePath
    }

    private fun rotateImage(source: Bitmap, angle: Float): Bitmap {
        val matrix = Matrix()
        matrix.postRotate(angle)
        return Bitmap.createBitmap(source, 0, 0, source.width, source.height, matrix, true)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(), { _, year, month, dayOfMonth ->
                val date = "$dayOfMonth ${getMonthName(month + 1)} $year"
                tvSelectedDate.text = date
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        // ✅ Disable future dates
        datePicker.datePicker.maxDate = System.currentTimeMillis()
        datePicker.show()
    }

    private fun getMonthName(month: Int): String {
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(Date(0, month, 0))
    }

    private fun showCustomDialog() {
        val dialogView = LayoutInflater.from(context).inflate(R.layout.dialog_item_snap_meal, null)
        val dialog = AlertDialog.Builder(context)
            .setView(dialogView)
            .create()
//        dialogView.findViewById<TextView>(R.id.dialogTitle).text = item.title
//        dialogView.findViewById<TextView>(R.id.dialogDescription).text = item.description
//        dialogView.findViewById<Button>(R.id.dialogOkBtn).setOnClickListener {
//            dialog.dismiss()
//        }
        dialog.show()
    }

    private fun createSnapMealLog(snapRecipeList: ArrayList<SearchResultItem>, isSave: Boolean) {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateUtc: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val snapDishList: ArrayList<SnapDish> = ArrayList()
        if (snapRecipeList.size > 0) {
            val items = snapRecipeList
            items.forEach { snapDish ->
                val snapDishRequest = SnapDish(
                    name = snapDish.name,
                    b12_mcg = snapDish.nutrients.micros.b12_mcg,
                    b1_mg = snapDish.nutrients.micros.b1_mg,
                    b2_mg = snapDish.nutrients.micros.b2_mg,
                    b3_mg = snapDish.nutrients.micros.b3_mg,
                    b6_mg = snapDish.nutrients.micros.b6_mg,
                    calcium_mg = snapDish.nutrients.micros.Calcium,
                    calories_kcal = snapDish.nutrients.macros.Calories,
                    carb_g = snapDish.nutrients.macros.Carbs,
                    cholesterol_mg = snapDish.nutrients.micros.Cholesterol,
                    copper_mg = snapDish.nutrients.micros.copper_mg,
                    fat_g = snapDish.nutrients.macros.Fats,
                    folate_mcg = snapDish.nutrients.micros.Folate,
                    fiber_g = snapDish.nutrients.micros.Fiber,
                    iron_mg = snapDish.nutrients.micros.Iron,
                    is_beverage = snapDish.nutrients.micros.is_beverage,
                    magnesium_mg = snapDish.nutrients.micros.Magnesium,
                    mass_g = snapDish.nutrients.micros.mass_g,
                    monounsaturated_g = snapDish.nutrients.micros.monounsaturated_g,
                    omega_3_fatty_acids_g = snapDish.nutrients.micros.omega_3_fatty_acids_g,
                    omega_6_fatty_acids_g = snapDish.nutrients.micros.omega_6_fatty_acids_g,
                    percent_fruit = snapDish.nutrients.micros.percent_fruit,
                    percent_legume_or_nuts = snapDish.nutrients.micros.percent_legume_or_nuts,
                    percent_vegetable = snapDish.nutrients.micros.percent_vegetable,
                    phosphorus_mg = snapDish.nutrients.micros.phosphorus_mg,
                    polyunsaturated_g = snapDish.nutrients.micros.polyunsaturated_g,
                    potassium_mg = snapDish.nutrients.micros.Potassium,
                    protein_g = snapDish.nutrients.macros.Protein,
                    saturated_fats_g = snapDish.nutrients.micros.saturated_fats_g,
                    selenium_mcg = snapDish.nutrients.micros.selenium_mcg,
                    sodium_mg = snapDish.nutrients.micros.Sodium,
                    source_urls = snapDish.nutrients.micros.source_urls,
                    sugar_g = snapDish.nutrients.micros.Sugar,
                    vitamin_a_mcg = snapDish.nutrients.micros.Vitamin_A,
                    vitamin_c_mg = snapDish.nutrients.micros.Vitamin_C,
                    vitamin_d_iu = snapDish.nutrients.micros.Vitamin_D,
                    vitamin_e_mg = snapDish.nutrients.micros.vitamin_e_mg,
                    vitamin_k_mcg = snapDish.nutrients.micros.Vitamin_K,
                    zinc_mg = snapDish.nutrients.micros.Zinc,
                    mealQuantity = snapDish.mealQuantity
                )
                snapDishList.add(snapDishRequest)
            }
            val snapMealLogRequest = SnapMealLogRequest(
                user_id = userId,
                meal_type = formatMealType(selectedMealType),
                meal_name = foodNameEdit.text.toString(),
                is_save = isSave,
                is_snapped = true,
                date = currentDateUtc,
                dish = snapDishList
            )
            val gson = Gson()
            val jsonString =
                gson.toJson(snapMealLogRequest) // snapMealLogRequest is your model instance
            Log.d("JSON Output", jsonString)
            val call = ApiClient.apiServiceFastApi.createSnapMealLog(snapMealLogRequest)
            call.enqueue(object : Callback<SnapMealLogResponse> {
                override fun onResponse(
                    call: Call<SnapMealLogResponse>,
                    response: Response<SnapMealLogResponse>
                ) {
                    if (response.isSuccessful) {
                        if (isAdded && view != null) {
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                        val mealData = response.body()?.message
                        Toast.makeText(activity, mealData, Toast.LENGTH_SHORT).show()
                        val moduleName = arguments?.getString("ModuleName").toString()
                        if (moduleName.contentEquals("EatRight")) {
                            val fragment = HomeBottomTabFragment()
                            val args = Bundle()
                            args.putString("ModuleName", "EatRight")
                            fragment.arguments = args
                            requireActivity().supportFragmentManager.beginTransaction().apply {
                                replace(R.id.flFragment, fragment, "landing")
                                addToBackStack("landing")
                                commit()
                            }
                        } else {
                            val mealId = response.body()?.inserted_ids?.meal_log_id ?: ""
                            CommonAPICall.updateChecklistStatus(
                                requireContext(),
                                "meal_snap",
                                AppConstants.CHECKLIST_COMPLETED
                            )
                            CommonAPICall.updateChecklistStatus(
                                requireContext(),
                                "snap_mealId",
                                mealId
                            )
                            startActivity(Intent(context, HomeNewActivity::class.java))
                            requireActivity().finish()
                        }
                    } else {
                        Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                        Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                        if (isAdded && view != null) {
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<SnapMealLogResponse>, t: Throwable) {
                    Log.e("Error", "API call failed: ${t.message}")
                    Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            })
        }
    }

    private fun formatMealType(input: String): String {
        return when (input.lowercase()) {
            "breakfast" -> "breakfast"
            "morning snack" -> "morning_snack"
            "lunch" -> "lunch"
            "evening snacks" -> "evening_snack"
            "dinner" -> "dinner"
            else -> input.lowercase().replace(" ", "_")
        }
    }

    private fun updateSnapMealsSave(snapRecipeList: ArrayList<SearchResultItem>) {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val snapMealLogList: ArrayList<SnapMealLogItem> = ArrayList()
        snapRecipeList.forEach { snapRecipe ->
            val mealLogData = SnapMealLogItem(
                name = snapRecipe.name,
                b12_mcg = snapRecipe.nutrients.micros.b12_mcg,
                b1_mg = snapRecipe.nutrients.micros.b1_mg,
                b2_mg = snapRecipe.nutrients.micros.b2_mg,
                b3_mg = snapRecipe.nutrients.micros.b3_mg,
                b6_mg = snapRecipe.nutrients.micros.b6_mg,
                calcium_mg = snapRecipe.nutrients.micros.Calcium,
                calories_kcal = snapRecipe.nutrients.macros.Calories,
                carb_g = snapRecipe.nutrients.macros.Carbs,
                cholesterol_mg = snapRecipe.nutrients.micros.Cholesterol,
                copper_mg = snapRecipe.nutrients.micros.copper_mg,
                fat_g = snapRecipe.nutrients.macros.Fats,
                folate_mcg = snapRecipe.nutrients.micros.Folate,
                fiber_g = snapRecipe.nutrients.micros.Fiber,
                iron_mg = snapRecipe.nutrients.micros.Iron,
                is_beverage = 0.0,
                magnesium_mg = snapRecipe.nutrients.micros.Magnesium,
                mass_g = snapRecipe.nutrients.micros.mass_g,
                monounsaturated_g = snapRecipe.nutrients.micros.monounsaturated_g,
                omega_3_fatty_acids_g = snapRecipe.nutrients.micros.omega_3_fatty_acids_g,
                omega_6_fatty_acids_g = snapRecipe.nutrients.micros.omega_6_fatty_acids_g,
                percent_fruit = snapRecipe.nutrients.micros.percent_fruit,
                percent_legume_or_nuts = snapRecipe.nutrients.micros.percent_legume_or_nuts,
                percent_vegetable = snapRecipe.nutrients.micros.percent_vegetable,
                phosphorus_mg = snapRecipe.nutrients.micros.phosphorus_mg,
                polyunsaturated_g = snapRecipe.nutrients.micros.polyunsaturated_g,
                potassium_mg = snapRecipe.nutrients.micros.Potassium,
                protein_g = snapRecipe.nutrients.macros.Protein,
                saturated_fats_g = snapRecipe.nutrients.micros.saturated_fats_g,
                selenium_mcg = snapRecipe.nutrients.micros.selenium_mcg,
                sodium_mg = snapRecipe.nutrients.micros.Sodium,
                source_urls = snapRecipe.nutrients.micros.source_urls,
                sugar_g = snapRecipe.nutrients.micros.Sugar,
                vitamin_a_mcg = snapRecipe.nutrients.micros.Vitamin_A,
                vitamin_c_mg = snapRecipe.nutrients.micros.Vitamin_C,
                vitamin_d_iu = snapRecipe.nutrients.micros.Vitamin_D,
                vitamin_e_mg = snapRecipe.nutrients.micros.vitamin_e_mg,
                vitamin_k_mcg = 0.0,
                zinc_mg = snapRecipe.nutrients.micros.Zinc,
                mealQuantity = snapRecipe.mealQuantity
            )
            snapMealLogList.add(mealLogData)
        }
        val updateMealRequest = UpdateSnapMealRequest(
            meal_name = mealName,
            meal_log = snapMealLogList
        )
        val call = ApiClient.apiServiceFastApi.updateSnapSaveMeal(mealId, userId, updateMealRequest)
        call.enqueue(object : Callback<MealUpdateResponse> {
            override fun onResponse(
                call: Call<MealUpdateResponse>,
                response: Response<MealUpdateResponse>
            ) {
                if (response.isSuccessful) {
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealData = response.body()?.message
                    Toast.makeText(activity, mealData, Toast.LENGTH_SHORT).show()
                    val fragment = HomeTabMealFragment()
                    val args = Bundle()
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<MealUpdateResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                if (isAdded && view != null) {
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    private fun ratingMealLogDialog(isSave: Boolean) {
        val ratingMealBottomSheet = RatingMealBottomSheet()
        ratingMealBottomSheet.isCancelable = true
        val bundle = Bundle()
        bundle.putBoolean("isSave", isSave)
        ratingMealBottomSheet.arguments = bundle
        parentFragment.let {
            ratingMealBottomSheet.show(
                childFragmentManager,
                "RatingMealBottomSheet"
            )
        }
        //   sharedPreferenceManager.setFirstTimeUserForSnapMealRating(true)
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }

    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    override fun onSnapMealRating(rating: Double, isSave: Boolean) {
        createSnapMealLog(snapRecipesList, isSave)
    }
}