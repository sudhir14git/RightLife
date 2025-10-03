package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.app.DatePickerDialog
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.get
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MacroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MicroNutrientsAdapter
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
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import com.jetsynthesys.rightlife.ai_package.model.response.RecipeDetailsResponse
import com.jetsynthesys.rightlife.ai_package.model.response.Serving
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.HomeTabMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SelectedMealLogList
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SnapMealRequestLocalListModel

class DishToLogFragment : BaseFragment<FragmentDishBinding>() {

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
    private lateinit var quantityEdit: EditText
    private lateinit var tvCheckOutRecipe: TextView
    private lateinit var tvChange: TextView
    private lateinit var tvQuantity: TextView
    private var quantity = 1
    private lateinit var backButton : ImageView
    private lateinit var ivEdit : ImageView
    private lateinit var tvMeasure :TextView
    private lateinit var spinner: Spinner
    private var measureType : String = ""
    private var dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
    private lateinit var recipeDetailsLocalListModel : RecipeDetailsLocalListModel
    private var mealLogRequests : SelectedMealLogList? = null
    private var snapMealLogRequests : SelectedMealLogList? = null
    private lateinit var mealType : String
    private var snapMealRequestLocalListModel : SnapMealRequestLocalListModel? = null
    private var moduleName : String = ""
    private var selectedMealDate : String = ""
    private var isSpinnerInitialized = false
    private var defaultServing: Serving? = null
    private var userSelectedServing: Serving? = null

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
     //   tvQuantity = view.findViewById(R.id.tvQuantity)
        tvMeasure = view.findViewById(R.id.tvMeasure)
        addToTheMealTV = view.findViewById(R.id.tv_addToTheMeal)
        tvMealName = view.findViewById(R.id.tvMealName)
        imgFood = view.findViewById(R.id.imgFood)
        layoutMicroTitle = view.findViewById(R.id.layoutMicroTitle)
        layoutMacroTitle = view.findViewById(R.id.layoutMacroTitle)
        microUP = view.findViewById(R.id.microUP)
        icMacroUP = view.findViewById(R.id.icMacroUP)
        quantityEdit = view.findViewById(R.id.quantityEdit)
        ivEdit = view.findViewById(R.id.ivEdit)
        backButton = view.findViewById(R.id.backButton)
        spinner = view.findViewById(R.id.spinner)

        moduleName = arguments?.getString("ModuleName").toString()
        searchType = arguments?.getString("searchType").toString()
        mealType = arguments?.getString("mealType").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()
        val snapRecipeName = arguments?.getString("snapRecipeName").toString()

        val searchResultItem = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("ingredientRecipeDetails", IngredientRecipeDetails::class.java)
        } else {
            arguments?.getParcelable("ingredientRecipeDetails")
        }

        val recipeDetailsLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", RecipeDetailsLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapDishLocalListModel")
        }

        val selectedMealLogListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("selectedMealLogList", SelectedMealLogList::class.java)
        } else {
            arguments?.getParcelable("selectedMealLogList")
        }

        val selectedSnapMealLogListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("selectedSnapMealLogList", SelectedMealLogList::class.java)
        } else {
            arguments?.getParcelable("selectedSnapMealLogList")
        }

        val snapMealRequestLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapMealRequestLocalListModel", SnapMealRequestLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapMealRequestLocalListModel")
        }

        if (snapMealRequestLocalListModels != null){
            snapMealRequestLocalListModel = snapMealRequestLocalListModels
        }

        if (selectedMealLogListModels != null){
            mealLogRequests = selectedMealLogListModels
        }

        if (selectedSnapMealLogListModels != null){
            snapMealLogRequests = selectedSnapMealLogListModels
        }

        if (recipeDetailsLocalListModels != null){
            recipeDetailsLocalListModel = recipeDetailsLocalListModels

            if (searchResultItem != null){
                if (recipeDetailsLocalListModel.data.size > 0){
                    dishLists.addAll(recipeDetailsLocalListModel.data)
                }
                dishLists.add(searchResultItem)
                recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            }
        }else{
            if (searchResultItem != null) {
             //   val data = searchResultItem
                dishLists.add(searchResultItem)
                recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            }
        }

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

        macroItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        macroItemRecyclerView.adapter = macroNutrientsAdapter
        microItemRecyclerView.layoutManager = GridLayoutManager(context, 4)
        microItemRecyclerView.adapter = microNutrientsAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (searchType.contentEquals("EatRight")){
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    args.putString("selectedMealDate", selectedMealDate)
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }else{
                    val fragment = SearchDishToLogFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    args.putString("selectedMealDate", selectedMealDate)
                    fragment.arguments = args
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
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }else{
                val fragment = SearchDishToLogFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                fragment.arguments = args
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
                        if (searchResultItem != null){
                            setDishData(searchResultItem, true)
                            var ingredientQuantity = 0.0
                            if (searchResultItem.quantity != null && searchResultItem.quantity > 0.0){
                                ingredientQuantity = searchResultItem.quantity
                            }else{
                                ingredientQuantity = 1.0
                            }
                            onMacroNutrientsList(searchResultItem, ingredientQuantity, targetValue)
                            onMicroNutrientsList(searchResultItem, ingredientQuantity, targetValue)
                        }else{
                            if (recipeDetailsLocalListModel != null){
                                for (item in recipeDetailsLocalListModel.data) {
                                    if (item.recipe.contentEquals(snapRecipeName)) {
                                        setDishData(item, true)
                                        var ingredientQuantity = 0.0
                                        if (item.quantity != null && item.quantity > 0.0){
                                            ingredientQuantity = item.quantity
                                        }else{
                                            ingredientQuantity = 1.0
                                        }
                                        onMacroNutrientsList(item, ingredientQuantity, targetValue)
                                        onMicroNutrientsList(item, ingredientQuantity, targetValue)
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

        if (searchResultItem != null){
            setDishData(searchResultItem, false)
            onMacroNutrientsList(searchResultItem, 1.0, 1.0)
            onMicroNutrientsList(searchResultItem, 1.0, 1.0)
            setupSpinner(searchResultItem.available_serving, searchResultItem.selected_serving)
        }

        addToTheMealLayout.setOnClickListener {

            if (quantityEdit.text.toString().isNotEmpty() && quantityEdit.text.toString() != "."){
                if (quantityEdit.text.toString().toDouble() > 0.0){
                    if (searchResultItem != null){
                        val foodData = searchResultItem
                        var targetValue = 0.0
                        if (quantityEdit.text.toString().toDouble() > 0.0){
                            targetValue = quantityEdit.text.toString().toDouble()
                        }else{
                            targetValue = 0.0
                        }
                        var ingredientQuantity = 0.0
                        if (foodData.quantity != null && foodData.quantity > 0.0){
                            ingredientQuantity = foodData.quantity
                        }else{
                            ingredientQuantity = 1.0
                        }
                        val selectedServing = Serving(
                            type = measureType,
                            value = quantityEdit.text.toString().toDouble())
                        val ingredientData = IngredientRecipeDetails(
                            id = foodData.id,
                            recipe_id = foodData.recipe_id,
                            food_code = foodData.food_code,
                            food_name = foodData.food_name,
                            recipe = foodData.recipe,
                            meal_type = foodData.meal_type,
                            cuisine = foodData.cuisine,
                            regional_split = foodData.regional_split,
                            category = foodData.category,
                            food_category = foodData.food_category,
                            flag = foodData.flag,
                            serving_size_for_calorific_breakdown = foodData.serving_size_for_calorific_breakdown,
                            standard_serving_size = foodData.standard_serving_size,
                            calories_kcal = calculateValue(foodData.calories_kcal, ingredientQuantity, targetValue),
                            carbs_g = calculateValue(foodData.carbs_g, ingredientQuantity, targetValue),
                            fiber_g = calculateValue(foodData.fiber_g, ingredientQuantity, targetValue),
                            sugars_g = calculateValue(foodData.sugars_g, ingredientQuantity, targetValue),
                            vit_b6_mg = calculateValue(foodData.vit_b6_mg, ingredientQuantity, targetValue),
                            vit_b12_mcg = calculateValue(foodData.vit_b12_mcg, ingredientQuantity, targetValue),
                            protein_g = calculateValue(foodData.protein_g, ingredientQuantity, targetValue),
                            fat_g = calculateValue(foodData.fat_g, ingredientQuantity, targetValue),
                            vit_a_mcg = calculateValue(foodData.vit_a_mcg, ingredientQuantity, targetValue),
                            vit_c_mg = calculateValue(foodData.vit_c_mg, ingredientQuantity, targetValue),
                            vit_d_mcg = calculateValue(foodData.vit_d_mcg, ingredientQuantity, targetValue),
                            vit_e_mg = calculateValue(foodData.vit_e_mg, ingredientQuantity, targetValue),
                            folate_b9_mcg = calculateValue(foodData.folate_b9_mcg, ingredientQuantity, targetValue),
                            vit_k_mcg = calculateValue(foodData.vit_k_mcg, ingredientQuantity, targetValue),
                            thiamin_b1_mg = calculateValue(foodData.thiamin_b1_mg, ingredientQuantity, targetValue),
                            riboflavin_b2_mg = calculateValue(foodData.riboflavin_b2_mg, ingredientQuantity, targetValue),
                            niacin_b3_mg = calculateValue(foodData.niacin_b3_mg, ingredientQuantity, targetValue),
                            iron_mg = calculateValue(foodData.iron_mg, ingredientQuantity, targetValue),
                            calcium_mg = calculateValue(foodData.calcium_mg, ingredientQuantity, targetValue),
                            magnesium_mg = calculateValue(foodData.magnesium_mg, ingredientQuantity, targetValue),
                            zinc_mg = calculateValue(foodData.zinc_mg, ingredientQuantity, targetValue),
                            potassium_mg = calculateValue(foodData.potassium_mg, ingredientQuantity, targetValue),
                            sodium_mg = calculateValue(foodData.sodium_mg, ingredientQuantity, targetValue),
                            phosphorus_mg = calculateValue(foodData.phosphorus_mg, ingredientQuantity, targetValue),
                            omega3_g = calculateValue(foodData.omega3_g, ingredientQuantity, targetValue),
                            ingredients = foodData.ingredients,
                            preparation_notes = foodData.preparation_notes,
                            active_cooking_time_min = foodData.active_cooking_time_min,
                            allergy_groups_restricted_from_consuming = foodData.allergy_groups_restricted_from_consuming,
                            tags = foodData.tags,
                            typical_1person_serving = foodData.typical_1person_serving,
                            household_measure_1_serving = foodData.household_measure_1_serving,
                            photo_url = foodData.photo_url,
                            selected_serving = selectedServing,
                            default_serving = foodData.default_serving,
                            available_serving = foodData.available_serving,
                            source = foodData.source,
                            quantity =  quantityEdit.text.toString().toDouble(),
                            servings = foodData.servings
                        )
                        dishLists.add(ingredientData)
                        recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
                    }
                    if (recipeDetailsLocalListModel?.data != null){
                        val fragment = HomeTabMealFragment()
                        val args = Bundle()
                        args.putString("mealQuantity", quantityEdit.text.toString())
                        args.putString("ModuleName", moduleName)
                        args.putString("searchType","DishToLog")
                        args.putString("mealType", mealType)
                        args.putString("selectedMealDate", selectedMealDate)
                        args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
                        args.putParcelable("selectedMealLogList", mealLogRequests)
                        args.putParcelable("selectedSnapMealLogList", snapMealLogRequests)
                        args.putParcelable("snapMealRequestLocalListModel", snapMealRequestLocalListModel)
                        fragment.arguments = args
                        requireActivity().supportFragmentManager.beginTransaction().apply {
                            replace(R.id.flFragment, fragment, "mealLog")
                            addToBackStack("mealLog")
                            commit()
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

    private fun setDishData(snapRecipeData: IngredientRecipeDetails,  isEdit: Boolean) {
        if (searchType.contentEquals("createRecipe")){
            addToTheMealTV.text = "Proceed"
        }else{
            addToTheMealTV.text = "Proceed"
            val capitalized = snapRecipeData.recipe.toString().replaceFirstChar { it.uppercase() }
            tvMealName.text = capitalized
//            if (snapRecipeData.standard_serving_size != null){
//                tvMeasure.text = snapRecipeData.standard_serving_size
//            }
//            val servingsList = mutableListOf<Serving>()
//            servingsList.addAll(snapRecipeData.available_serving)
//            val servingLabels = servingsList.map { it.type }
//            // Use ArrayAdapter
//            val adapter = ArrayAdapter(requireActivity(), android.R.layout.simple_spinner_item, servingLabels)
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            spinner.adapter = adapter
            if (!isEdit){
//                var isSpinnerInitialized = false
//                spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
//                    override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
//                        if (!isSpinnerInitialized) {
//                            isSpinnerInitialized = true
//                            return
//                        }
//                        val selectedServing = servingsList[position]
//                        val newQuantity = selectedServing.value.toString()
//                        // ✅ Update only if different
//                        if (quantityEdit.text.toString() != newQuantity) {
//                            quantityEdit.setText(newQuantity)
//                            measureType = selectedServing.type.toString()
//                        }
//                    }
//                    override fun onNothingSelected(parent: AdapterView<*>) {}
//                }
//                if (snapRecipeData.quantity != null ){
//                    if (snapRecipeData.quantity > 0.0){
//                        quantityEdit.setText(snapRecipeData.quantity.toInt().toString())
//                    }else{
//                        quantityEdit.setText("1")
//                    }
//                }
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
    }

    private fun setupSpinner(servingsList: List<Serving>, default: Serving?) {
        val adapter = ArrayAdapter(
            requireActivity(),
            android.R.layout.simple_spinner_item,
            servingsList.map {it.type }
        )
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        // Store default serving
        defaultServing = default
        // Pre-select default serving in spinner
        val defaultIndex = servingsList.indexOfFirst {
            it.type == default?.type
        }
        val safeIndex = if (defaultIndex >= 0) defaultIndex else 0
        spinner.setSelection(safeIndex)
        val defaultSelectedServing = servingsList[safeIndex]
        quantityEdit.setText(defaultSelectedServing.value.toString())
        measureType = defaultSelectedServing.type.toString()
        // Listener
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                if (!isSpinnerInitialized) {
                    isSpinnerInitialized = true
                    return
                }
                val selectedServing = servingsList[position]
                userSelectedServing = selectedServing  // ✅ track user choice
                val newQuantity = selectedServing.value.toString()
                if (quantityEdit.text.toString() != newQuantity) {
                    quantityEdit.setText(newQuantity)
                    measureType = selectedServing.type.toString()
                }
            }
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun onMacroNutrientsList(mealDetails: IngredientRecipeDetails, defaultValue: Double, targetValue: Double) {

        val calories = calculateValue(mealDetails.calories_kcal, defaultValue, targetValue)
        val protein = calculateValue(mealDetails.protein_g, defaultValue, targetValue)
        val carbs = calculateValue(mealDetails.carbs_g, defaultValue, targetValue)
        val fats = calculateValue(mealDetails.fat_g, defaultValue, targetValue)
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

    private fun onMicroNutrientsList(mealDetails: IngredientRecipeDetails, defaultValue: Double, targetValue: Double) {

//        val cholesterol = if (mealDetails.cholesterol != null){
//            calculateValue( mealDetails.cholesterol, defaultValue, targetValue).toInt().toString()
//        }else{
//            "0.0"
//        }

        val vitamin_A = if (mealDetails.vit_a_mcg != null){
            calculateValue( mealDetails.vit_a_mcg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

        val vitamin_C = if (mealDetails.vit_c_mg != null){
            calculateValue( mealDetails.vit_c_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

        val vitamin_k = if (mealDetails.vit_k_mcg != null){
            calculateValue( mealDetails.vit_k_mcg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

        val vitaminD = if (mealDetails.vit_d_mcg != null){
            calculateValue( mealDetails.vit_d_mcg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

        val folate = if (mealDetails.folate_b9_mcg != null){
            calculateValue( mealDetails.folate_b9_mcg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

        val iron_mg = if (mealDetails.iron_mg != null){
            calculateValue( mealDetails.iron_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

        val calcium = if (mealDetails.calcium_mg != null){
            calculateValue( mealDetails.calcium_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

        val magnesium = if (mealDetails.magnesium_mg != null){
            calculateValue( mealDetails.magnesium_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

        val potassium_mg = if (mealDetails.potassium_mg != null){
            calculateValue( mealDetails.potassium_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

//        val fiber_mg = if (mealDetails.fiber != null){
//            calculateValue( mealDetails.fiber, defaultValue, targetValue).toInt().toString()
//        }else{
//            "0"
//        }

        val zinc = if (mealDetails.zinc_mg != null){
            calculateValue( mealDetails.zinc_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

        val sodium = if (mealDetails.sodium_mg != null){
            calculateValue( mealDetails.sodium_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

//        val sugar_mg = if (mealDetails.sugar != null){
//            calculateValue( mealDetails.sugar, defaultValue, targetValue).toInt().toString()
//        }else{
//            "0.0"
//        }

        val vitB6 = if (mealDetails.vit_b6_mg != null){
            calculateValue( mealDetails.vit_b6_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

        val vitB12 = if (mealDetails.vit_b12_mcg != null){
            calculateValue( mealDetails.vit_b12_mcg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

        val phosphorus = if (mealDetails.phosphorus_mg != null){
            calculateValue( mealDetails.phosphorus_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0.0"
        }

        val mealLogs = listOf(
            //   MicroNutrientsModel(cholesterol, "mg", "Cholesterol", R.drawable.ic_fats),
            MicroNutrientsModel(vitamin_A, "mcg", "Vitamin A", R.drawable.ic_fats),
            MicroNutrientsModel(vitamin_C, "mg", "Vitamin C", R.drawable.ic_fats),
            MicroNutrientsModel(vitamin_k, "mcg", "Vitamin K", R.drawable.ic_fats),
            MicroNutrientsModel(vitaminD, "mcg", "Vitamin D", R.drawable.ic_fats),
            MicroNutrientsModel(vitB6, "mg", "Vitamin B6", R.drawable.ic_fats),
            MicroNutrientsModel(vitB12, "mcg", "Vitamin B12", R.drawable.ic_fats),
            MicroNutrientsModel(folate, "mcg", "Folate", R.drawable.ic_fats),
            MicroNutrientsModel(iron_mg, "mg", "Iron", R.drawable.ic_fats),
            MicroNutrientsModel(calcium, "mg", "Calcium", R.drawable.ic_fats),
            MicroNutrientsModel(magnesium, "mg", "Magnesium", R.drawable.ic_fats),
            MicroNutrientsModel(potassium_mg, "mg", "Potassium", R.drawable.ic_fats),
            //       MicroNutrientsModel(fiber_mg, "mg", "Fiber", R.drawable.ic_fats),
            MicroNutrientsModel(zinc, "mg", "Zinc", R.drawable.ic_fats),
            MicroNutrientsModel(sodium, "mg", "Sodium", R.drawable.ic_fats),
            //        MicroNutrientsModel(sugar_mg, "g", "Sugar", R.drawable.ic_fats)
            MicroNutrientsModel(phosphorus, "mg", "Phosphorus", R.drawable.ic_fats)
        )
        val valueLists : ArrayList<MicroNutrientsModel> = ArrayList()
        for (item in mealLogs){
            if (item.nutrientsValue != "0.0"){
                valueLists.add(item)
            }
        }
        val mealLogDateData: MicroNutrientsModel? = null
        microNutrientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun calculateValue(givenValue: Double?, defaultQuantity: Double, targetQuantity: Double): Double {
        return if (givenValue != null && defaultQuantity > 0.0) {
            (givenValue / defaultQuantity) * targetQuantity
        } else {
            0.0
        }
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
}