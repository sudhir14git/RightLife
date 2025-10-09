package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

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
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiService
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.request.SnapDish
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealLogItem
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealLogItems
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateSnapMealLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateSnapMealRequest
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealLogResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealNutrientsResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.RatingMealBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MacroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MicroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.SnapMealScanResultAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.HomeTabMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal.SearchDishFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MacroNutrientsModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MicroNutrientsModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.databinding.FragmentMealScanResultsBinding
import com.jetsynthesys.rightlife.newdashboard.HomeNewActivity
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.profile_new.pojo.PreSignedUrlData
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
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

    lateinit var apiService: ApiService
    private var preSignedUrlData: PreSignedUrlData? = null
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
    private lateinit var tvAddToLog: TextView
    private lateinit var deleteSnapMealBottomSheet: DeleteSnapMealBottomSheet
    private var recipeDetailsLocalListModel: RecipeDetailsLocalListModel? = null
    private var snapRecipesList: ArrayList<IngredientRecipeDetails> = ArrayList()
    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    private lateinit var selectedMealType: String
    private var mealId: String = ""
    private var mealName: String = ""
    private var snapImageUrl: String = ""
    private var moduleName: String = ""
    private var quantity = 1
    private var loadingOverlay : FrameLayout? = null
    private var mealType : String = ""
    private var snapMealLog : String = ""
    private var snapMyMeal : String = ""
    private var homeTab : String = ""
    private var selectedMealDate : String = ""
    private var isSaveClick : Boolean = false

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMealScanResultsBinding
        get() = FragmentMealScanResultsBinding::inflate

    private val macroNutrientsAdapter by lazy {
        MacroNutrientsAdapter(requireContext(), arrayListOf(), -1, null, false, ::onMealLogDateItem)
    }
    private val microNutrientsAdapter by lazy {
        MicroNutrientsAdapter(requireContext(), arrayListOf(), -1, null, false, ::onMicroNutrientsItem)
    }
    private val mealListAdapter by lazy {
        SnapMealScanResultAdapter(requireContext(), arrayListOf(), -1, null, false, ::onMenuEditItem, ::onMenuDeleteItem)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
        apiService = com.jetsynthesys.rightlife.RetrofitData.ApiClient.getClient(requireContext()).create(ApiService::class.java)
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
        tvAddToLog = view.findViewById(R.id.tvAddToLog)
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

        homeTab = arguments?.getString("homeTab").toString()
        moduleName = arguments?.getString("ModuleName").toString()
        mealId = arguments?.getString("mealId").toString()
        mealName = arguments?.getString("mealName").toString()
        mealType = arguments?.getString("mealType").toString()
        snapMealLog = arguments?.getString("snapMealLog").toString()
        snapMyMeal = arguments?.getString("snapMyMeal").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()
        snapImageUrl = arguments?.getString("snapImageUrl").toString()

        val dishLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", RecipeDetailsLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapDishLocalListModel")
        }

        val foodDataResponses = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("foodDataResponses", SnapMealNutrientsResponse::class.java)
        } else {
            arguments?.getParcelable("foodDataResponses")
        }

        currentPhotoPath = arguments?.get("ImagePath").toString()

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formatFullDate = DateTimeFormatter.ofPattern("d MMMM yyyy")
        tvSelectedDate.text = currentDateTime.format(formatFullDate)

        descriptionName = arguments?.getString("description").toString()
        checkBox.setOnCheckedChangeListener { buttonView, isChecked ->
            isSaveClick = true
            if (isChecked) {
                if (mealId != "null" && mealId != null) {
                    updateSnapMealsSave((snapRecipesList))
                }else{
                   // currentPhotoPathsecound?.let { getUrlFromURI(it) }
                    createSnapMealLog(snapRecipesList, true)
                }
            }
        }

        if (dishLocalListModels != null) {
            recipeDetailsLocalListModel = dishLocalListModels
            snapRecipesList.addAll(recipeDetailsLocalListModel!!.data)
            onFrequentlyLoggedItemRefresh(snapRecipesList)
            onMicroNutrientsList(snapRecipesList)
            onMacroNutrientsList(snapRecipesList)
            setFoodDataFromDish(recipeDetailsLocalListModel!!)
        }

        // Data for Spinner
        val items = arrayOf("Breakfast", "Morning Snack", "Lunch", "Evening Snacks", "Dinner")
        // Create Adapter
        val adapter =
            ArrayAdapter(requireActivity(), R.layout.snap_mealtype_spinner, items)
        adapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinner.adapter = adapter

        if (snapMealLog.equals("snapMealLog")) {
            saveMealLayout.visibility = View.GONE
            tvAddToLog.text = "Update To Log"
            addLayout.isEnabled = false
            addLayout.setBackgroundResource(R.drawable.light_green_bg)
            // Set default selection to "Lunch"
            val defaultIndex = items.indexOf(mealType)
            if (defaultIndex != -1) {
                spinner.setSelection(defaultIndex)
                selectedMealType = items[defaultIndex]
            }
            // Disable user interaction (disable dropdown)
            spinner.isEnabled = false
            spinner.isClickable = false
        }else if (homeTab.equals("homeTab")){
            saveMealLayout.visibility = View.VISIBLE
            val defaultIndex = items.indexOf(formatMealTypeName(mealType))
            if (defaultIndex != -1) {
                spinner.setSelection(defaultIndex)
                selectedMealType = items[defaultIndex]
            }
        }else {
            saveMealLayout.visibility = View.VISIBLE
        }

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

        if (!snapMealLog.equals("snapMealLog")) {
            view.findViewById<LinearLayoutCompat>(R.id.datePickerLayout).setOnClickListener {
                // Open Date Picker
                showDatePicker()
            }
        }

        if (foodDataResponses?.data != null) {
            setFoodData(foodDataResponses)
            val snapRecipesListScan: ArrayList<IngredientRecipeDetails> = ArrayList()
            if (foodDataResponses.data.dish.size > 0) {
                val items = foodDataResponses.data.dish
                snapRecipesListScan.addAll(items)
                snapRecipesList = snapRecipesListScan
                onFrequentlyLoggedItemRefresh(snapRecipesList)
                onMicroNutrientsList(snapRecipesList)
                onMacroNutrientsList(snapRecipesList)
                recipeDetailsLocalListModel = RecipeDetailsLocalListModel(snapRecipesList)
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
            } else if (snapMealLog.equals("snapMealLog")) {
                val fragment = YourMealLogsFragment()
                val args = Bundle()
                args.putString("selectedMealDate", selectedMealDate)
                args.putString("ModuleName", moduleName)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }else if (homeTab.equals("homeTab")){
                val fragment = HomeTabMealFragment()
                val args = Bundle()
                args.putString("selectedMealDate", selectedMealDate)
                args.putString("ModuleName", moduleName)
                args.putString("mealType", mealType)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
        } else{
                startActivity(Intent(context, HomeNewActivity::class.java))
                requireActivity().finish()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() { if (moduleName.contentEquals("EatRight")) {
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    val snapMealFragment = HomeBottomTabFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    snapMealFragment.arguments = args
                    replace(R.id.flFragment, snapMealFragment, "Steps")
                    addToBackStack(null)
                    commit()
                }
            } else if (snapMealLog.equals("snapMealLog")) {
                val fragment = YourMealLogsFragment()
                val args = Bundle()
                args.putString("selectedMealDate", selectedMealDate)
                args.putString("ModuleName", moduleName)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }else if (homeTab.equals("homeTab")){
                    val fragment = HomeTabMealFragment()
                    val args = Bundle()
                    args.putString("selectedMealDate", selectedMealDate)
                    args.putString("ModuleName", moduleName)
                    args.putString("mealType", mealType)
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
        } else{
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
            args.putString("mealName", foodNameEdit.text.toString())
            args.putString("snapImageUrl", snapImageUrl)
            args.putString("mealType", mealType)
            args.putString("homeTab", homeTab)
            args.putString("snapMyMeal", snapMyMeal)
            args.putString("selectedMealDate", selectedMealDate)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        saveMealLayout.setOnClickListener {
//            Toast.makeText(context, "Save Meal", Toast.LENGTH_SHORT).show()
//            if (moduleName.contentEquals("EatRight")){
//                requireActivity().supportFragmentManager.beginTransaction().apply {
//                    val snapMealFragment = HomeBottomTabFragment()
//                    val args = Bundle()
//                    args.putString("ModuleName", moduleName)
//                    snapMealFragment.arguments = args
//                    replace(R.id.flFragment, snapMealFragment, "Steps")
//                    addToBackStack(null)
//                    commit()
//                }
//            }else{
//                startActivity(Intent(context, HomeNewActivity::class.java))
//                requireActivity().finish()
//            }
        }

        addToLogLayout.setOnClickListener {
            isSaveClick = false
            if (moduleName.contentEquals("EatRight")) {
                // ratingMealLogDialog()
                // sharedPreferenceManager.setFirstTimeUserForSnapMealRating(true)
                if (mealId != "null" && mealId != null) {
                    updateSnapMealsSave((snapRecipesList))
                } else {
                  //  currentPhotoPathsecound?.let { getUrlFromURI(it) }
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
                if (snapMealLog.equals("snapMealLog")) {
                    updateSnapMealLog(mealId, snapRecipesList)
                } else {
                   // currentPhotoPathsecound?.let { getUrlFromURI(it) }
                    createSnapMealLog(snapRecipesList, false)
                }
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

    private fun onMicroNutrientsList(nutrition: ArrayList<IngredientRecipeDetails>) {

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
            totalVitaminD += (item.vit_d_mcg ?: 0.0)
            totalB12 += (item.vit_b12_mcg ?: 0.0)
            totalFolate += (item.folate_b9_mcg ?: 0.0)
            totalVitaminC += (item.vit_c_mg ?: 0.0)
            totalVitaminA += (item.vit_a_mcg ?: 0.0)
            totalVitaminK += (item.vit_k_mcg ?: 0.0)
            totalIron += (item.iron_mg ?: 0.0)
            totalCalcium += (item.calcium_mg ?: 0.0)
            totalMagnesium += (item.magnesium_mg ?: 0.0)
            totalZinc += (item.zinc_mg ?: 0.0)
            totalOmega3 += (item.omega3_g ?: 0.0)
            totalSodium += (item.sodium_mg ?: 0.0)
           // totalCholesterol += (item.nutrients.micros.Cholesterol ?: 0.0)
            totalSugar += (item.sugars_g ?: 0.0)
            totalPhosphorus += (item.phosphorus_mg ?: 0.0)
            totalPotassium += (item.potassium_mg ?: 0.0)
        }

        val vitaminD = if (totalVitaminD != null) {
            String.format("%.1f", totalVitaminD)
        } else {
            "0.0"
        }

        val b12_mcg = if (totalB12 != null) {
            String.format("%.1f", totalB12)
        } else {
            "0.0"
        }

        val folate = if (totalFolate != null) {
            String.format("%.1f", totalFolate)
        } else {
            "0.0"
        }

        val vitaminC = if (totalVitaminC != null) {
            String.format("%.1f", totalVitaminC)
        } else {
            "0.0"
        }

        val vitaminA = if (totalVitaminA != null) {
            String.format("%.1f", totalVitaminA)
        } else {
            "0.0"
        }

        val vitaminK = if (totalVitaminK != null) {
            String.format("%.1f", totalVitaminK)
        } else {
            "0.0"
        }

        val iron_mg = if (totalIron != null) {
            String.format("%.1f", totalIron)
        } else {
            "0.0"
        }

        val calcium = if (totalCalcium != null) {
            String.format("%.1f", totalCalcium)
        } else {
            "0.0"
        }

        val magnesium_mg = if (totalMagnesium != null) {
            String.format("%.1f", totalMagnesium)
        } else {
            "0.0"
        }

        val zinc_mg = if (totalZinc != null) {
            String.format("%.1f", totalZinc)
        } else {
            "0.0"
        }

        val omega3 = if (totalOmega3 != null) {
            String.format("%.1f", totalOmega3)
        } else {
            "0.0"
        }

        val sodium = if (totalSodium != null) {
            String.format("%.1f", totalSodium)
        } else {
            "0.0"
        }

        val cholesterol = if (totalCholesterol != null) {
            String.format("%.1f", totalCholesterol)
        } else {
            "0.0"
        }

        val sugar = if (totalSugar != null) {
            String.format("%.1f", totalSugar)
        } else {
            "0.0"
        }

        val phosphorus_mg = if (totalPhosphorus != null) {
            String.format("%.1f", totalPhosphorus)
        } else {
            "0.0"
        }
        val potassium_mg = if (totalPotassium != null) {
            String.format("%.1f", totalPotassium)
        } else {
            "0.0"
        }

        val mealLogs = listOf(
//            MicroNutrientsModel(phosphorus_mg, "mg", "Phasphorus", R.drawable.ic_fats),
            MicroNutrientsModel(calcium, "mg", "Calcium", R.drawable.ic_fats),
            MicroNutrientsModel(cholesterol, "mg", "Cholesterol", R.drawable.ic_fats),
            MicroNutrientsModel(folate, "μg", "Folate", R.drawable.ic_fats),
            MicroNutrientsModel(iron_mg, "mg", "Iron", R.drawable.ic_fats),
            MicroNutrientsModel(magnesium_mg, "mg", "Magnesium", R.drawable.ic_fats),
            MicroNutrientsModel(omega3, "mg", "Omega-3", R.drawable.ic_fats),
            MicroNutrientsModel(potassium_mg, "mg", "Potassium", R.drawable.ic_fats),
            MicroNutrientsModel(sodium, "mg", "Sodium", R.drawable.ic_fats),
            MicroNutrientsModel(sugar, "g", "Sugar", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminA, "IU", "Vitamin A", R.drawable.ic_fats),
            MicroNutrientsModel(b12_mcg, "μg", "Vitamin B12", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminC, "mg", "Vitamin C", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminD, "μg", "Vitamin D", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminK, "μg", "Vitamin K", R.drawable.ic_fats),
            MicroNutrientsModel(zinc_mg, "mg", "Zinc", R.drawable.ic_fats)
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
    }

    private fun onMealLogDateItem(
        mealLogDateModel: MacroNutrientsModel,
        position: Int,
        isRefresh: Boolean
    ) {
    }

    private fun onMacroNutrientsList(nutritionList: ArrayList<IngredientRecipeDetails>) {

        val totalCalories = nutritionList.sumOf { it.calories_kcal ?: 0.0 }
        val totalProtein = nutritionList.sumOf { it.protein_g ?: 0.0 }
        val totalCarbs = nutritionList.sumOf { it.carbs_g ?: 0.0 }
        val totalFat = nutritionList.sumOf { it.fat_g ?: 0.0 }

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

    private fun onFrequentlyLoggedItemRefresh(recipes: List<IngredientRecipeDetails>) {
        if (recipes.size > 0) {
            frequentlyLoggedRecyclerView.visibility = View.VISIBLE
            //   layoutNoMeals.visibility = View.GONE
        } else {
            //    layoutNoMeals.visibility = View.VISIBLE
            frequentlyLoggedRecyclerView.visibility = View.GONE
        }
        val valueLists: ArrayList<IngredientRecipeDetails> = ArrayList()
        valueLists.addAll(recipes as Collection<IngredientRecipeDetails>)
        val mealLogDateData: IngredientRecipeDetails? = null
        mealListAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMenuEditItem(
        snapRecipeData: IngredientRecipeDetails,
        position: Int,
        isRefresh: Boolean
    ) {

        requireActivity().supportFragmentManager.beginTransaction().apply {
            val snapMealFragment = SnapDishFragment()
            val args = Bundle()
            args.putString("mealId", mealId)
            args.putString("mealName", foodNameEdit.text.toString())
            args.putString("snapImageUrl", snapImageUrl)
            args.putString("ModuleName", moduleName)
            args.putString("searchType", "MealScanResult")
            args.putString("mealType", mealType)
            args.putString("homeTab", homeTab)
            args.putString("selectedMealDate", selectedMealDate)
            args.putString("snapMealLog", snapMealLog)
            args.putString("snapMyMeal", snapMyMeal)
            args.putString("mealQuantity", snapRecipeData.quantity.toString())
            args.putString("snapRecipeName", snapRecipeData.food_name)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            snapMealFragment.arguments = args
            replace(R.id.flFragment, snapMealFragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun onMenuDeleteItem(
        snapRecipeData: IngredientRecipeDetails,
        position: Int,
        isRefresh: Boolean
    ) {
        deleteSnapMealBottomSheet = DeleteSnapMealBottomSheet()
        deleteSnapMealBottomSheet.isCancelable = true
        val args = Bundle()
        args.putBoolean("test", false)
        args.putString("ModuleName", moduleName)
        args.putString("mealId", mealId)
        args.putString("mealName", foodNameEdit.text.toString())
        args.putString("snapImageUrl", snapImageUrl)
        args.putString("mealType", mealType)
        args.putString("homeTab", homeTab)
        args.putString("selectedMealDate", selectedMealDate)
        args.putString("snapMealLog", snapMealLog)
        args.putString("snapMyMeal", snapMyMeal)
        args.putString("snapRecipeName", snapRecipeData.food_name)
        args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
        deleteSnapMealBottomSheet.arguments = args
        activity?.supportFragmentManager?.let {
            deleteSnapMealBottomSheet.show(
                it,
                "DeleteMealBottomSheet"
            )
        }
    }

    private fun setFoodData(nutritionResponse: SnapMealNutrientsResponse) {
        if (nutritionResponse.data != null) {
            if (nutritionResponse.data.image_url != null) {
                try {
                    snapImageUrl = nutritionResponse.data.image_url
                    imageFood.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(nutritionResponse.data.image_url)
                        .placeholder(R.drawable.ic_view_meal_place)
                        .error(R.drawable.ic_view_meal_place)
                        .into(imageFood)
                } catch (e: Exception) {
                    Log.e("ImageLoad", "Error loading image from file path: $currentPhotoPath", e)
                }
            }
            // Set food name
                val capitalized =
                    nutritionResponse.data.meal_name.replaceFirstChar { it.uppercase() }
                if (!mealName.equals("null") && !mealName.equals("")){
                    foodNameEdit.setText(mealName.replaceFirstChar { it.uppercase() })
                }else{
                    mealName = capitalized
                    foodNameEdit.setText(capitalized)
                }
        }
    }

    private fun setFoodDataFromDish(snapRecipeData: RecipeDetailsLocalListModel) {
        if (snapRecipeData.data != null) {
            if (!snapImageUrl.equals("") && !snapImageUrl.equals("null")){
                imageFood.visibility = View.VISIBLE
                Glide.with(requireContext())
                    .load(snapImageUrl)
                    .placeholder(R.drawable.ic_view_meal_place)
                    .error(R.drawable.ic_view_meal_place)
                    .into(imageFood)
            }
            // Set food name
            if (!mealName.equals("null") && !mealName.equals("")) {
                val capitalized =
                    mealName.replaceFirstChar { it.uppercase() }
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

    private fun getRealPathFromURI(context: Context, uri: Uri): String? {
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

    private fun createSnapMealLog(snapRecipeList: ArrayList<IngredientRecipeDetails>, isSave: Boolean) {
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
        if (snapRecipeList.size > 0) {
            val snapMealLogRequest = SnapMealLogRequest(
                user_id = userId,
                meal_type = formatMealType(selectedMealType),
                meal_name = foodNameEdit.text.toString(),
                is_save = isSave,
                is_snapped = true,
                date = currentDateUtc,
                dish = snapRecipeList,
                image_url = snapImageUrl
            )
            val gson = Gson()
            val jsonString =
                gson.toJson(snapMealLogRequest) // snapMealLogRequest is your model instance
            Log.d("JSON Output", jsonString)
            val call = ApiClient.apiServiceFastApiV2.createSnapMealLog(snapMealLogRequest)
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
                        }else if (homeTab.equals("homeTab")){
                            val fragment = YourMealLogsFragment()
                            val args = Bundle()
                            args.putString("selectedMealDate", selectedMealDate)
                            args.putString("ModuleName", moduleName)
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
                            var productId = ""
                            sharedPreferenceManager.userProfile.subscription.forEach { subscription ->
                                if (subscription.status) {
                                    productId = subscription.productId
                                }
                            }

                            AnalyticsLogger.logEvent(
                                requireContext(),
                                AnalyticsEvent.MEAL_SCAN_COMPLETE,
                                mapOf(AnalyticsParam.MEAL_SCAN_COMPLETE to true)
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

    private fun formatMealTypeName(meal: String): String {
        return when (meal.lowercase()) {
            "breakfast" -> "Breakfast"
            "morning_snack" -> "Morning Snack"
            "lunch" -> "Lunch"
            "evening_snack" -> "Evening Snacks"
            "dinner" -> "Dinner"
            else -> meal.replaceFirstChar { it.uppercase() } // fallback
        }
    }

    private fun updateSnapMealsSave(snapRecipeList: ArrayList<IngredientRecipeDetails>) {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val updateMealRequest = UpdateSnapMealRequest(
            meal_name = foodNameEdit.text.toString(),
            image_url = snapImageUrl,
            meal_log = snapRecipeList
        )
        val call = ApiClient.apiServiceFastApiV2.updateSnapSaveMeal(mealId, userId, updateMealRequest)
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

    private fun updateSnapMealLog(mealId: String, snapRecipeList: ArrayList<IngredientRecipeDetails>) {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val updateMealRequest = UpdateSnapMealLogRequest(
            meal_name = foodNameEdit.text.toString(),
            meal_log = snapRecipeList
        )
        val call = ApiClient.apiServiceFastApiV2.updateSnapLogMeal(userId, mealId, updateMealRequest)
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
                    Toast.makeText(context, mealData, Toast.LENGTH_SHORT).show()
                    val fragment = YourMealLogsFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(context, "Something went wrong", Toast.LENGTH_SHORT).show()
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
            override fun onFailure(call: Call<MealUpdateResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(context, "Failure", Toast.LENGTH_SHORT).show()
                if (isAdded && view != null) {
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }
}