package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MacroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MicroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.FrequentlyLoggedListAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MacroNutrientsModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MicroNutrientsModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MyMealModel
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.databinding.FragmentDishBinding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import androidx.core.view.isVisible
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.MealsResponse
import com.jetsynthesys.rightlife.ai_package.model.response.Macros
import com.jetsynthesys.rightlife.ai_package.model.response.Micros
import com.jetsynthesys.rightlife.ai_package.model.response.Nutrients
import com.jetsynthesys.rightlife.ai_package.model.response.RecipeResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SearchResultItem
import com.jetsynthesys.rightlife.ai_package.model.response.SnapRecipeData
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal.SearchDishFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SnapDishLocalListModel
import com.jetsynthesys.rightlife.ai_package.utils.LoaderUtil
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SnapDishFragment : BaseFragment<FragmentDishBinding>() {

    private lateinit var macroItemRecyclerView : RecyclerView
    private lateinit var microItemRecyclerView : RecyclerView
    private lateinit var addToTheMealLayout : LinearLayoutCompat
    private lateinit var layoutMicroTitle : ConstraintLayout
    private lateinit var layoutMacroTitle : ConstraintLayout
    private lateinit var icMacroUP : ImageView
    private lateinit var microUP : ImageView
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
    private lateinit var quantityEdit: EditText
    private lateinit var ivEdit : ImageView
    private lateinit var backButton : ImageView
    private var mealQuantity = 1.0
    private lateinit var tvMeasure :TextView
    private var dishLists : ArrayList<SearchResultItem> = ArrayList()
    private lateinit var snapDishLocalListModel : SnapDishLocalListModel
    private var currentPhotoPathsecound : Uri? = null
    private var mealId : String = ""
    private var mealName : String = ""

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentDishBinding
        get() = FragmentDishBinding::inflate

    private val macroNutrientsAdapter by lazy { MacroNutrientsAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMacroNutrientsItemClick) }
    private val microNutrientsAdapter by lazy { MicroNutrientsAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMicroNutrientsItemClick) }

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
        quantityEdit = view.findViewById(R.id.quantityEdit)
        tvMeasure = view.findViewById(R.id.tvMeasure)
        addToTheMealTV = view.findViewById(R.id.tv_addToTheMeal)
        tvMealName = view.findViewById(R.id.tvMealName)
        imgFood = view.findViewById(R.id.imgFood)
        layoutMicroTitle = view.findViewById(R.id.layoutMicroTitle)
        layoutMacroTitle = view.findViewById(R.id.layoutMacroTitle)
        microUP = view.findViewById(R.id.microUP)
        icMacroUP = view.findViewById(R.id.icMacroUP)
        ivEdit = view.findViewById(R.id.ivEdit)
        backButton = view.findViewById(R.id.back_button)

        mealId = arguments?.getString("mealId").toString()
        mealName = arguments?.getString("mealName").toString()
        val mealQuantitys = arguments?.getString("mealQuantity").toString()
        if (mealQuantitys != "null"){
            if (mealQuantitys.toDouble() > 0.0){
                mealQuantity = mealQuantitys.toDouble()
            }else{
                mealQuantity = 1.0
            }
        }else{
            mealQuantity = 1.0
        }
        val imagePathString = arguments?.getString("ImagePathsecound")
        if (imagePathString != null){
            currentPhotoPathsecound = imagePathString?.let { Uri.parse(it) }!!
        }else{
            currentPhotoPathsecound = null
        }
        searchType = arguments?.getString("searchType").toString()
        val snapRecipeName = arguments?.getString("snapRecipeName").toString()
        val foodDetailsResponse = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("searchResultItem", SearchResultItem::class.java)
        } else {
            arguments?.getParcelable("searchResultItem")
        }

        val snapDishLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", SnapDishLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapDishLocalListModel")
        }

        if (foodDetailsResponse != null){
            if (snapDishLocalListModels != null) {
                snapDishLocalListModel = snapDishLocalListModels
                if (snapDishLocalListModel?.data != null){
                    if (snapDishLocalListModel.data.size > 0){
                        dishLists.addAll(snapDishLocalListModel.data)
                    }
                }
            }
        }else{
            if (snapDishLocalListModels != null) {
                snapDishLocalListModel = snapDishLocalListModels
                if (snapDishLocalListModel?.data != null){
                    if (snapDishLocalListModel.data.size > 0){
                        dishLists.addAll(snapDishLocalListModel.data)
                    }
                }
            }
        }

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

        layoutMacroTitle.setOnClickListener {
          if (macroItemRecyclerView.isVisible){
              macroItemRecyclerView.visibility = View.GONE
              icMacroUP.setImageResource(R.drawable.ic_down)
              view.findViewById<View>(R.id.view_macro).visibility = View.GONE
          }else{
              macroItemRecyclerView.visibility = View.VISIBLE
              icMacroUP.setImageResource(R.drawable.ic_up)
              view.findViewById<View>(R.id.view_macro).visibility = View.VISIBLE
          }
        }

        layoutMicroTitle.setOnClickListener {
            if (microItemRecyclerView.isVisible){
                microItemRecyclerView.visibility = View.GONE
                microUP.setImageResource(R.drawable.ic_down)
                view.findViewById<View>(R.id.view_micro).visibility = View.GONE
            }else{
                microItemRecyclerView.visibility = View.VISIBLE
                microUP.setImageResource(R.drawable.ic_up)
                view.findViewById<View>(R.id.view_micro).visibility = View.VISIBLE
            }
        }

//        view.findViewById<ImageView>(R.id.ivIncrease).setOnClickListener {
//            quantity++
//            tvQuantity.setText( quantity.toString())
//        }

        macroItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        macroItemRecyclerView.adapter = macroNutrientsAdapter
        microItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        microItemRecyclerView.adapter = microNutrientsAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchType.contentEquals("EatRight")){
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle()
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }else if (searchType.contentEquals("searchScanResult")){
                    val fragment = SearchDishFragment()
                    val args = Bundle()
                    fragment.arguments = args
                    args.putString("searchType", searchType)
                    args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }else{
                    val fragment = MealScanResultFragment()
                    val args = Bundle()
                    fragment.arguments = args
                    args.putString("searchType", searchType)
                    args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
                    args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
                    args.putString("ModuleName", arguments?.getString("ModuleName").toString())
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }
            }
        })

        backButton.setOnClickListener {
            if (searchType.contentEquals("EatRight")){
                val fragment = HomeBottomTabFragment()
                val args = Bundle()
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }else if (searchType.contentEquals("searchScanResult")){
                val fragment = SearchDishFragment()
                val args = Bundle()
                fragment.arguments = args
                args.putString("searchType", searchType)
                args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }else{
                val fragment = MealScanResultFragment()
                val args = Bundle()
                fragment.arguments = args
                args.putString("searchType", searchType)
                args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
                args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
                args.putString("ModuleName", arguments?.getString("ModuleName").toString())
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        }

        quantityEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(update: Editable?) {
                if (update!!.isNotEmpty() && update.toString() != "."){
                    if (quantityEdit.text.toString().toDouble() > 0.0){
                        val targetValue : Double = quantityEdit.text.toString().toDouble()
                        if (foodDetailsResponse != null){
                            setDishData(foodDetailsResponse, true)
                            onMacroNutrientsList(foodDetailsResponse, mealQuantity, targetValue)
                            onMicroNutrientsList(foodDetailsResponse, mealQuantity, targetValue)
                        }else{
                            if (snapDishLocalListModel != null){
                                for (item in snapDishLocalListModel.data) {
                                    if (item.name.contentEquals(snapRecipeName)) {
                                        setDishData(item, true)
                                        onMacroNutrientsList(item, mealQuantity, targetValue)
                                        onMicroNutrientsList(item, mealQuantity, targetValue)
                                        break
                                    }
                                }
                            }
                        }
                    }
                }
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {

            }
        })


//        ivEdit.setOnClickListener {
//          val value =  quantityEdit.text.toString().toInt()
//            if (foodDetailsResponse != null){
//                setDishData(foodDetailsResponse)
//                onMacroNutrientsList(foodDetailsResponse, value)
//                onMicroNutrientsList(foodDetailsResponse, value)
//                // onFrequentlyLoggedItemRefresh(foodDetailsResponse.data)
//            }else{
//                if (snapDishLocalListModel != null){
//                    for (item in snapDishLocalListModel.data) {
//                        if (item.name.contentEquals(snapRecipeName)) {
//                            setDishData(item)
//                            onMacroNutrientsList(item, value)
//                            onMicroNutrientsList(item, value)
//                            break
//                        }
//                    }
//                }
//            }
//        }

        if (foodDetailsResponse != null){
            setDishData(foodDetailsResponse, false)
            onMacroNutrientsList(foodDetailsResponse,1.0, 1.0)
            onMicroNutrientsList(foodDetailsResponse, 1.0, 1.0)
        }else{
            if (snapDishLocalListModel != null){
                for (item in snapDishLocalListModel.data) {
                    if (item.name.contentEquals(snapRecipeName)) {
                        setDishData(item, false)
                        onMacroNutrientsList(item, 1.0, 1.0)
                        onMicroNutrientsList(item, 1.0, 1.0)
                        break
                    }
                }
            }
        }

        addToTheMealLayout.setOnClickListener {

            if (quantityEdit.text.toString().isNotEmpty() && quantityEdit.text.toString() != "."){
                if (quantityEdit.text.toString().toDouble() > 0.0){
                    if (searchType.contentEquals("SearchDish")){
                        if (foodDetailsResponse != null){
                            val foodData = foodDetailsResponse
                            var targetValue = 0.0
                            if (quantityEdit.text.toString().toDouble() > 0.0){
                                targetValue = quantityEdit.text.toString().toDouble()
                            }else{
                                targetValue = 0.0
                            }
                            val macrosData = Macros(
                                Calories =  calculateValue(foodData.nutrients.macros.Calories, mealQuantity, targetValue),
                                Carbs = calculateValue(foodData.nutrients.macros.Carbs, mealQuantity, targetValue),
                                Fats = calculateValue(foodData.nutrients.macros.Fats, mealQuantity, targetValue),
                                Protein = calculateValue(foodData.nutrients.macros.Protein, mealQuantity, targetValue))
                            val microsData = Micros(
                                Cholesterol = calculateValue(foodData.nutrients.micros.Cholesterol, mealQuantity, targetValue),
                                Vitamin_A = calculateValue(foodData.nutrients.micros.Vitamin_A, mealQuantity, targetValue),
                                Vitamin_C = calculateValue(foodData.nutrients.micros.Vitamin_C, mealQuantity, targetValue),
                                Vitamin_K = calculateValue(foodData.nutrients.micros.Vitamin_K, mealQuantity, targetValue),
                                Vitamin_D = calculateValue(foodData.nutrients.micros.Vitamin_D, mealQuantity, targetValue),
                                Folate = calculateValue(foodData.nutrients.micros.Folate, mealQuantity, targetValue),
                                Iron = calculateValue(foodData.nutrients.micros.Iron, mealQuantity, targetValue),
                                Calcium = calculateValue(foodData.nutrients.micros.Calcium, mealQuantity, targetValue),
                                Magnesium = calculateValue(foodData.nutrients.micros.Magnesium, mealQuantity, targetValue),
                                Potassium = calculateValue(foodData.nutrients.micros.Potassium, mealQuantity, targetValue),
                                Fiber = calculateValue(foodData.nutrients.micros.Fiber, mealQuantity, targetValue),
                                Zinc = calculateValue(foodData.nutrients.micros.Zinc, mealQuantity, targetValue),
                                Sodium = calculateValue(foodData.nutrients.micros.Sodium, mealQuantity, targetValue),
                                Sugar = calculateValue(foodData.nutrients.micros.Sugar, mealQuantity, targetValue),
                                b12_mcg = calculateValue(foodData.nutrients.micros.b12_mcg, mealQuantity, targetValue),
                                b1_mg = calculateValue(foodData.nutrients.micros.b1_mg, mealQuantity, targetValue),
                                b2_mg = calculateValue(foodData.nutrients.micros.b2_mg, mealQuantity, targetValue),
                                b5_mg = calculateValue(foodData.nutrients.micros.b5_mg, mealQuantity, targetValue),
                                b3_mg = calculateValue(foodData.nutrients.micros.b3_mg, mealQuantity, targetValue),
                                b6_mg = calculateValue(foodData.nutrients.micros.b6_mg, mealQuantity, targetValue),
                                vitamin_e_mg = calculateValue(foodData.nutrients.micros.vitamin_e_mg, mealQuantity, targetValue),
                                omega_3_fatty_acids_g = calculateValue(foodData.nutrients.micros.omega_3_fatty_acids_g, mealQuantity, targetValue),
                                omega_6_fatty_acids_g = calculateValue(foodData.nutrients.micros.omega_6_fatty_acids_g, mealQuantity, targetValue),
                                copper_mg = calculateValue(foodData.nutrients.micros.copper_mg, mealQuantity, targetValue),
                                phosphorus_mg = calculateValue(foodData.nutrients.micros.phosphorus_mg, mealQuantity, targetValue),
                                saturated_fats_g = calculateValue(foodData.nutrients.micros.saturated_fats_g, mealQuantity, targetValue),
                                selenium_mcg = calculateValue(foodData.nutrients.micros.selenium_mcg, mealQuantity, targetValue),
                                trans_fats_g = calculateValue(foodData.nutrients.micros.trans_fats_g, mealQuantity, targetValue),
                                polyunsaturated_g = calculateValue(foodData.nutrients.micros.polyunsaturated_g, mealQuantity, targetValue),
                                is_beverage = foodData.nutrients.micros.is_beverage,
                                mass_g = calculateValue(foodData.nutrients.micros.mass_g, mealQuantity, targetValue),
                                monounsaturated_g = calculateValue(foodData.nutrients.micros.monounsaturated_g, mealQuantity, targetValue),
                                percent_fruit = calculateValue(foodData.nutrients.micros.percent_fruit, mealQuantity, targetValue),
                                percent_vegetable = calculateValue(foodData.nutrients.micros.percent_vegetable, mealQuantity, targetValue),
                                percent_legume_or_nuts = calculateValue(foodData.nutrients.micros.percent_legume_or_nuts, mealQuantity, targetValue),
                                source_urls = foodData.nutrients.micros.source_urls
                            )
                            val nutrientsData =  Nutrients(
                                macros = macrosData,
                                micros =  microsData)
                            val snapRecipeData = SearchResultItem(
                                id = foodData.id,
                                name = foodData.name,
                                category = foodData.category,
                                photo_url = foodData.photo_url,
                                servings = foodData.servings,
                                cooking_time_in_seconds = foodData.cooking_time_in_seconds,
                                calories = foodData.calories,
                                nutrients = nutrientsData,
                                source = foodData.source,
                                unit = foodData.unit,
                                mealQuantity = quantityEdit.text.toString().toDouble()
                            )
                            dishLists.add(snapRecipeData)
                            snapDishLocalListModel = SnapDishLocalListModel(dishLists)
                        }
                        Toast.makeText(activity, "Added To Meal", Toast.LENGTH_SHORT).show()
                        val fragment = MealScanResultFragment()
                        val args = Bundle()
                        args.putString("mealId", mealId)
                        args.putString("mealName", mealName)
                        args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
                        args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
                        args.putString("ModuleName", arguments?.getString("ModuleName").toString())
                        fragment.arguments = args
                        requireActivity().supportFragmentManager.beginTransaction().apply {
                            replace(R.id.flFragment, fragment, "mealLog")
                            addToBackStack("mealLog")
                            commit()
                        }
                    }else{
                        if (snapDishLocalListModel != null) {
                            if (snapDishLocalListModel.data.size > 0) {
                                for (item in snapDishLocalListModel.data) {
                                    if (item.name.contentEquals(snapRecipeName)) {
                                        val foodData = item
                                        var targetValue = 0.0
                                        if (quantityEdit.text.toString().toDouble() > 0.0){
                                            targetValue = quantityEdit.text.toString().toDouble()
                                        }else{
                                            targetValue = 0.0
                                        }
                                        val index = snapDishLocalListModel.data.indexOfFirst { it.name == snapRecipeName }
                                        val macrosData = Macros(
                                            Calories =  calculateValue(foodData.nutrients.macros.Calories, mealQuantity!!, targetValue),
                                            Carbs = calculateValue(foodData.nutrients.macros.Carbs, mealQuantity, targetValue),
                                            Fats = calculateValue(foodData.nutrients.macros.Fats, mealQuantity, targetValue),
                                            Protein = calculateValue(foodData.nutrients.macros.Protein, mealQuantity, targetValue))
                                        val microsData = Micros(
                                            Cholesterol = calculateValue(foodData.nutrients.micros.Cholesterol, mealQuantity, targetValue),
                                            Vitamin_A = calculateValue(foodData.nutrients.micros.Vitamin_A, mealQuantity, targetValue),
                                            Vitamin_C = calculateValue(foodData.nutrients.micros.Vitamin_C, mealQuantity, targetValue),
                                            Vitamin_K = calculateValue(foodData.nutrients.micros.Vitamin_K, mealQuantity, targetValue),
                                            Vitamin_D = calculateValue(foodData.nutrients.micros.Vitamin_D, mealQuantity, targetValue),
                                            Folate = calculateValue(foodData.nutrients.micros.Folate, mealQuantity, targetValue),
                                            Iron = calculateValue(foodData.nutrients.micros.Iron, mealQuantity, targetValue),
                                            Calcium = calculateValue(foodData.nutrients.micros.Calcium, mealQuantity, targetValue),
                                            Magnesium = calculateValue(foodData.nutrients.micros.Magnesium, mealQuantity, targetValue),
                                            Potassium = calculateValue(foodData.nutrients.micros.Potassium, mealQuantity, targetValue),
                                            Fiber = calculateValue(foodData.nutrients.micros.Fiber, mealQuantity, targetValue),
                                            Zinc = calculateValue(foodData.nutrients.micros.Zinc, mealQuantity, targetValue),
                                            Sodium = calculateValue(foodData.nutrients.micros.Sodium, mealQuantity, targetValue),
                                            Sugar = calculateValue(foodData.nutrients.micros.Sugar, mealQuantity, targetValue),
                                            b12_mcg = calculateValue(foodData.nutrients.micros.b12_mcg, mealQuantity, targetValue),
                                            b1_mg = calculateValue(foodData.nutrients.micros.b1_mg, mealQuantity, targetValue),
                                            b2_mg = calculateValue(foodData.nutrients.micros.b2_mg, mealQuantity, targetValue),
                                            b5_mg = calculateValue(foodData.nutrients.micros.b5_mg, mealQuantity, targetValue),
                                            b3_mg = calculateValue(foodData.nutrients.micros.b3_mg, mealQuantity, targetValue),
                                            b6_mg = calculateValue(foodData.nutrients.micros.b6_mg, mealQuantity, targetValue),
                                            vitamin_e_mg = calculateValue(foodData.nutrients.micros.vitamin_e_mg, mealQuantity, targetValue),
                                            omega_3_fatty_acids_g = calculateValue(foodData.nutrients.micros.omega_3_fatty_acids_g, mealQuantity, targetValue),
                                            omega_6_fatty_acids_g = calculateValue(foodData.nutrients.micros.omega_6_fatty_acids_g, mealQuantity, targetValue),
                                            copper_mg = calculateValue(foodData.nutrients.micros.copper_mg, mealQuantity, targetValue),
                                            phosphorus_mg = calculateValue(foodData.nutrients.micros.phosphorus_mg, mealQuantity, targetValue),
                                            saturated_fats_g = calculateValue(foodData.nutrients.micros.saturated_fats_g, mealQuantity, targetValue),
                                            selenium_mcg = calculateValue(foodData.nutrients.micros.selenium_mcg, mealQuantity, targetValue),
                                            trans_fats_g = calculateValue(foodData.nutrients.micros.trans_fats_g, mealQuantity, targetValue),
                                            polyunsaturated_g = calculateValue(foodData.nutrients.micros.polyunsaturated_g, mealQuantity, targetValue),
                                            is_beverage = foodData.nutrients.micros.is_beverage,
                                            mass_g = calculateValue(foodData.nutrients.micros.mass_g, mealQuantity, targetValue),
                                            monounsaturated_g = calculateValue(foodData.nutrients.micros.monounsaturated_g, mealQuantity, targetValue),
                                            percent_fruit = calculateValue(foodData.nutrients.micros.percent_fruit, mealQuantity, targetValue),
                                            percent_vegetable = calculateValue(foodData.nutrients.micros.percent_vegetable, mealQuantity, targetValue),
                                            percent_legume_or_nuts = calculateValue(foodData.nutrients.micros.percent_legume_or_nuts, mealQuantity, targetValue),
                                            source_urls = foodData.nutrients.micros.source_urls
                                        )
                                        val nutrientsData =  Nutrients(
                                            macros = macrosData,
                                            micros =  microsData)
                                        val snapRecipeData = SearchResultItem(
                                            id = foodData.id,
                                            name = foodData.name,
                                            category = foodData.category,
                                            photo_url = foodData.photo_url,
                                            servings = foodData.servings,
                                            cooking_time_in_seconds = foodData.cooking_time_in_seconds,
                                            calories = foodData.calories,
                                            nutrients = nutrientsData,
                                            source = foodData.source,
                                            unit = foodData.unit,
                                            mealQuantity = quantityEdit.text.toString().toDouble()
                                        )
                                        if (index != -1) {
                                            dishLists[index] = snapRecipeData
                                        }
                                        dishLists.get(index)
                                        snapDishLocalListModel = SnapDishLocalListModel(dishLists)
                                        Toast.makeText(activity, "Changes Save", Toast.LENGTH_SHORT).show()
                                        val fragment = MealScanResultFragment()
                                        val args = Bundle()
                                        args.putString("mealId", mealId)
                                        args.putString("mealName", mealName)
                                        args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
                                        args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
                                        args.putString("ModuleName", arguments?.getString("ModuleName").toString())
                                        fragment.arguments = args
                                        requireActivity().supportFragmentManager.beginTransaction().apply {
                                            replace(R.id.flFragment, fragment, "mealLog")
                                            addToBackStack("mealLog")
                                            commit()
                                        }
                                        break
                                    }
                                }
                            }
                        }
                    }
                }else{
                    Toast.makeText(activity, "Please input quantity greater than 0", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(activity, "Please input quantity greater than 0", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun calculateValue(givenValue: Double?, defaultQuantity: Double, targetQuantity: Double): Double {
        return if (givenValue != null && defaultQuantity > 0.0) {
            (givenValue / defaultQuantity) * targetQuantity
        } else {
            0.0
        }
    }

    private fun setDishData(snapRecipeData: SearchResultItem, isEdit : Boolean) {
        if (searchType.contentEquals("SearchDish")){
            addToTheMealTV.text = "Add To The Meal"
        }else {
            addToTheMealTV.text = "Save Changes"
        }
            val capitalized = snapRecipeData.name.toString().replaceFirstChar { it.uppercase() }
            tvMealName.text = capitalized
            if (snapRecipeData.unit != null && snapRecipeData.unit != ""){
                tvMeasure.text = snapRecipeData.unit
            }else{
                tvMeasure.text = "Bowl"
            }
        if (!isEdit){
            if (snapRecipeData.mealQuantity != null ){
                if (snapRecipeData.mealQuantity > 0.0){
                    quantityEdit.setText(snapRecipeData.mealQuantity?.toInt().toString())
                }else if (snapRecipeData.servings > 0){
                    quantityEdit.setText(snapRecipeData.servings?.toInt().toString())
                }else{
                    quantityEdit.setText("1")
                }
            }
        }
        var imageUrl : String? = ""
        imageUrl = if (snapRecipeData.photo_url.contains("drive.google.com")) {
            getDriveImageUrl(snapRecipeData.photo_url)
        }else{
            snapRecipeData.photo_url
        }
            Glide.with(this)
                .load(imageUrl)
                .placeholder(R.drawable.ic_view_meal_place)
                .error(R.drawable.ic_view_meal_place)
                .into(imgFood)

    }

    private fun onMacroNutrientsList(mealDetails: SearchResultItem, defaultValue: Double, targetValue: Double) {

        val calories = calculateValue(mealDetails.nutrients.macros.Calories!!, defaultValue, targetValue)
        val protein = calculateValue(mealDetails.nutrients.macros.Protein!!, defaultValue, targetValue)
        val carbs = calculateValue(mealDetails.nutrients.macros.Carbs!!, defaultValue, targetValue)
        val fats = calculateValue(mealDetails.nutrients.macros.Fats!!, defaultValue, targetValue)
        val calories_kcal : String = calories.toInt().toString()?: "NA"
        val protein_g : String = protein.toInt().toString()?: "NA"
        val carb_g : String = carbs.toInt().toString()?: "NA"
        val fat_g : String = fats.toInt().toString()?: "NA"

        val mealLogs = listOf(
            MacroNutrientsModel(calories_kcal, "kcal", "Calorie", R.drawable.ic_cal),
            MacroNutrientsModel(protein_g, "g", "Protein", R.drawable.ic_protein),
            MacroNutrientsModel(carb_g, "g", "Carbs", R.drawable.ic_cabs),
            MacroNutrientsModel(fat_g, "g", "Fats", R.drawable.ic_fats),
        )

        val valueLists : ArrayList<MacroNutrientsModel> = ArrayList()
        valueLists.addAll(mealLogs as Collection<MacroNutrientsModel>)
        val mealLogDateData: MacroNutrientsModel? = null
        macroNutrientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMicroNutrientsList(mealDetails: SearchResultItem, defaultValue: Double, targetValue: Double) {

        val cholesterol = if (mealDetails.nutrients.micros.Cholesterol != null){
            calculateValue( mealDetails.nutrients.micros.Cholesterol!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val vitamin_A = if (mealDetails.nutrients.micros.Vitamin_A != null){
            calculateValue(mealDetails.nutrients.micros.Vitamin_A!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val vitamin_C = if (mealDetails.nutrients.micros.Vitamin_C != null){
            calculateValue(mealDetails.nutrients.micros.Vitamin_C!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val vitamin_k = if (mealDetails.nutrients.micros.Vitamin_K != null){
            calculateValue(mealDetails.nutrients.micros.Vitamin_K!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val vitaminD = if (mealDetails.nutrients.micros.Vitamin_D != null){
            calculateValue(mealDetails.nutrients.micros.Vitamin_D!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val folate = if (mealDetails.nutrients.micros.Folate != null){
            calculateValue(mealDetails.nutrients.micros.Folate!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val iron_mg = if (mealDetails.nutrients.micros.Iron != null){
            calculateValue(mealDetails.nutrients.micros.Iron!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val calcium = if (mealDetails.nutrients.micros.Calcium != null){
            calculateValue(mealDetails.nutrients.micros.Calcium!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val magnesium = if (mealDetails.nutrients.micros.Magnesium != null){
            calculateValue(mealDetails.nutrients.micros.Magnesium!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val potassium_mg = if (mealDetails.nutrients.micros.Potassium != null){
            calculateValue(mealDetails.nutrients.micros.Potassium!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val fiber_mg = if (mealDetails.nutrients.micros.Fiber != null){
            calculateValue(mealDetails.nutrients.micros.Fiber!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val zinc = if (mealDetails.nutrients.micros.Zinc != null){
            calculateValue(mealDetails.nutrients.micros.Zinc!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val sodium = if (mealDetails.nutrients.micros.Sodium != null){
            calculateValue(mealDetails.nutrients.micros.Sodium!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val sugar_mg = if (mealDetails.nutrients.micros.Sugar != null){
            calculateValue(mealDetails.nutrients.micros.Sugar!!, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val mealLogs = listOf(
            MicroNutrientsModel(cholesterol, "mg", "Cholesterol", R.drawable.ic_fats),
            MicroNutrientsModel(vitamin_A, "mg", "Vitamin A", R.drawable.ic_fats),
            MicroNutrientsModel(vitamin_C, "mg", "Vitamin C", R.drawable.ic_fats),
            MicroNutrientsModel(vitamin_k, "mg", "Vitamin K", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminD, "mg", "Vitamin D", R.drawable.ic_fats),
            MicroNutrientsModel(folate, "mg", "Folate", R.drawable.ic_fats),
            MicroNutrientsModel(iron_mg, "mg", "Iron", R.drawable.ic_fats),
            MicroNutrientsModel(calcium, "mg", "Calcium", R.drawable.ic_fats),
            MicroNutrientsModel(magnesium, "mg", "Magnesium", R.drawable.ic_fats),
            MicroNutrientsModel(potassium_mg, "mg", "Potassium", R.drawable.ic_fats),
            MicroNutrientsModel(fiber_mg, "mg", "Fiber", R.drawable.ic_fats),
            MicroNutrientsModel(zinc, "mg", "Zinc", R.drawable.ic_fats),
            MicroNutrientsModel(sodium, "mg", "Sodium", R.drawable.ic_fats),
            MicroNutrientsModel(sugar_mg, "g", "Sugar", R.drawable.ic_fats)
        )

        val valueLists : ArrayList<MicroNutrientsModel> = ArrayList()
        //  valueLists.addAll(mealLogs as Collection<MicroNutrientsModel>)
        for (item in mealLogs){
            if (item.nutrientsValue != "0"){
                valueLists.add(item)
            }
        }
        val mealLogDateData: MicroNutrientsModel? = null
        microNutrientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onFrequentlyLoggedItem(myMealModel: MyMealModel, position: Int, isRefresh: Boolean) {

    }

    private fun onMacroNutrientsItemClick(macroNutrientsModel: MacroNutrientsModel, position: Int, isRefresh: Boolean) {

    }

    private fun onMicroNutrientsItemClick(microNutrientsModel: MicroNutrientsModel, position: Int, isRefresh: Boolean) {

    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePicker = DatePickerDialog(
            requireContext(), { _, year, month, dayOfMonth ->
                val date = "$dayOfMonth ${getMonthName(month)} $year"
             //   tvSelectedDate.text = date
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePicker.show()
    }

    private fun getMonthName(month: Int): String {
        return SimpleDateFormat("MMMM", Locale.getDefault()).format(Date(0, month, 0))
    }

    fun getDriveImageUrl(originalUrl: String): String? {
        val regex = Regex("(?<=/d/)(.*?)(?=/|$)")
        val matchResult = regex.find(originalUrl)
        val fileId = matchResult?.value
        return if (!fileId.isNullOrEmpty()) {
            "https://drive.google.com/uc?export=view&id=$fileId"
        } else {
            null
        }
    }

    private fun showMealSelection() {
        val meals = arrayOf("Breakfast", "Lunch", "Dinner")
        val builder = AlertDialog.Builder(requireContext())
        builder.setItems(meals) { _, which ->
        }
        builder.show()
    }
}