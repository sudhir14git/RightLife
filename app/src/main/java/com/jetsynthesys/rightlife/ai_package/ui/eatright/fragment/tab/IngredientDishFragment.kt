package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MacroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MicroNutrientsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MacroNutrientsModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MicroNutrientsModel
import com.jetsynthesys.rightlife.databinding.FragmentDishBinding
import androidx.core.view.isVisible
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientDetail
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientDetailResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal.CreateRecipeFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.IngredientLocalListModel

class IngredientDishFragment : BaseFragment<FragmentDishBinding>() {

    private lateinit var macroItemRecyclerView : RecyclerView
    private lateinit var microItemRecyclerView : RecyclerView
    private lateinit var addToTheMealLayout : LinearLayoutCompat
    private lateinit var layoutMicroTitle : ConstraintLayout
    private lateinit var layoutMacroTitle : ConstraintLayout
    private lateinit var icMacroUP : ImageView
    private lateinit var microUP : ImageView
    private lateinit var imgFood : ImageView
    private lateinit var tvMealName : TextView
    private lateinit var addToTheMealTV : TextView
    private lateinit var searchType : String
    private lateinit var quantityEdit: EditText
    private lateinit var tvCheckOutRecipe: TextView
    private lateinit var tvChange: TextView
    private lateinit var ivEdit : ImageView
    private lateinit var tvMeasure :TextView
    private lateinit var backButton : ImageView
    private var ingredientLists : ArrayList<IngredientDetail> = ArrayList()
    private var ingredientLocalListModel : IngredientLocalListModel? = null
    private var recipeId : String = ""
    private var recipeName : String = ""
    private var serving : Double = 0.0
    private var mealQuantity = 1.0
    private var moduleName : String = ""
    private var selectedMealDate : String = ""
    private lateinit var mealType : String

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

        moduleName = arguments?.getString("ModuleName").toString()
        searchType = arguments?.getString("searchType").toString()
        recipeId = arguments?.getString("recipeId").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()
        serving = arguments?.getDouble("serving")?.toDouble() ?: 0.0
        mealType = arguments?.getString("mealType").toString()
        val ingredientName = arguments?.getString("ingredientName").toString()
        recipeName = arguments?.getString("recipeName").toString()
        val ingredientDetailResponse = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("ingredientDetailResponse", IngredientDetailResponse::class.java)
        } else {
            arguments?.getParcelable("ingredientDetailResponse")
        }

        val ingredientLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("ingredientLocalListModel", IngredientLocalListModel::class.java)
        } else {
            arguments?.getParcelable("ingredientLocalListModel")
        }

        if (ingredientDetailResponse != null){
//            if (ingredientDetailResponse.data != null){
//                ingredientLists.add(ingredientDetailResponse.data)
//            }
            if (ingredientLocalListModels != null) {
                ingredientLocalListModel = ingredientLocalListModels
                if (ingredientLocalListModel?.data != null){
                    if (ingredientLocalListModel!!.data.size > 0){
                        ingredientLists.addAll(ingredientLocalListModel!!.data)
                    }
                }
            }
        }else{
            if (ingredientLocalListModels != null) {
                ingredientLocalListModel = ingredientLocalListModels
                if (ingredientLocalListModel?.data != null){
                    if (ingredientLocalListModel!!.data.size > 0){
                        ingredientLists.addAll(ingredientLocalListModel!!.data)
                    }
                }
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
                if (searchType.contentEquals("searchIngredient")){
                    val fragment = SearchIngredientFragment()
                    val args = Bundle()
                    args.putString("recipeId", recipeId)
                    args.putString("recipeName", recipeName)
                    args.putDouble("serving", serving)
                    args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }else{
                    val fragment = CreateRecipeFragment()
                    val args = Bundle()
                    args.putString("recipeId", recipeId)
                    args.putString("recipeName", recipeName)
                    args.putDouble("serving", serving)
                    args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
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
            if (searchType.contentEquals("searchIngredient")){
                val fragment = SearchIngredientFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                args.putString("mealType", mealType)
                args.putString("recipeId", recipeId)
                args.putString("recipeName", recipeName)
                args.putDouble("serving", serving)
                args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }else{
                val fragment = CreateRecipeFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                args.putString("mealType", mealType)
                args.putString("recipeId", recipeId)
                args.putString("recipeName", recipeName)
                args.putDouble("serving", serving)
                args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        }

        if (ingredientDetailResponse != null){
            setDishData(ingredientDetailResponse.data, false)
            onMacroNutrientsList(ingredientDetailResponse.data, 1.0, 1.0)
            onMicroNutrientsList(ingredientDetailResponse.data, 1.0, 1.0)
        }else{
            if (ingredientLocalListModel != null){
                for (item in ingredientLocalListModel!!.data) {
                    if (item.food_name.contentEquals(ingredientName)) {
                        setDishData(item, false)
                        onMacroNutrientsList(item, 1.0, 1.0)
                        onMicroNutrientsList(item, 1.0, 1.0)
                        break
                    }
                }
            }
        }

        quantityEdit.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(update: Editable?) {
                if (update!!.isNotEmpty() && update.toString() != "."){
                    if (quantityEdit.text.toString().toDouble() > 0.0){
                        val targetValue : Double = quantityEdit.text.toString().toDouble()
                        if (ingredientDetailResponse != null){
                            setDishData(ingredientDetailResponse.data, true)
                            var ingredientQuantity = 0.0
                            if (ingredientDetailResponse.data.quantity > 0.0){
                                ingredientQuantity = ingredientDetailResponse.data.quantity
                            }else{
                                ingredientQuantity = 1.0
                            }
                            onMacroNutrientsList(ingredientDetailResponse.data, ingredientQuantity, targetValue)
                            onMicroNutrientsList(ingredientDetailResponse.data, ingredientQuantity, targetValue)
                        }else{
                            if (ingredientLocalListModel != null){
                                for (item in ingredientLocalListModel!!.data) {
                                    if (item.food_name.contentEquals(ingredientName)) {
                                        setDishData(item, true)
                                        var ingredientQuantity = 0.0
                                        if (item.quantity > 0.0){
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

        addToTheMealLayout.setOnClickListener {

            if (quantityEdit.text.toString().isNotEmpty() && quantityEdit.text.toString() != "."){
                if (quantityEdit.text.toString().toDouble() > 0.0){
                    if (searchType.contentEquals("searchIngredient")){
                        if (ingredientDetailResponse != null){
                            val foodData = ingredientDetailResponse.data
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
                            val ingredientData = IngredientDetail(
                                id = foodData.id,
                                food_code = foodData.food_code,
                                food_name = foodData.food_name,
                                food_category = foodData.food_category,
                                photo_url = foodData.photo_url,
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
                                quantity =  quantityEdit.text.toString().toDouble(),
                                measure = tvMeasure.text.toString()
                            )
                            ingredientLists.add(ingredientData)
                            ingredientLocalListModel = IngredientLocalListModel(ingredientLists)
                        }
                        Toast.makeText(activity, "Added To Meal", Toast.LENGTH_SHORT).show()
                        val fragment = CreateRecipeFragment()
                        val args = Bundle()
                        args.putString("ModuleName", moduleName)
                        args.putString("selectedMealDate", selectedMealDate)
                        args.putString("mealType", mealType)
                        args.putString("recipeId", recipeId)
                        args.putString("recipeName", recipeName)
                        args.putDouble("serving", serving)
                        args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
                        fragment.arguments = args
                        requireActivity().supportFragmentManager.beginTransaction().apply {
                            replace(R.id.flFragment, fragment, "mealLog")
                            addToBackStack("mealLog")
                            commit()
                        }
                    }else{
                        if (ingredientLocalListModel != null) {
                            if (ingredientLocalListModel!!.data.size > 0) {
                                for (item in ingredientLocalListModel!!.data) {
                                    if (item.food_name.contentEquals(ingredientName)) {
                                        val foodData = item
                                        var targetValue = 0.0
                                        if (quantityEdit.text.toString().toDouble() > 0.0){
                                            targetValue = quantityEdit.text.toString().toDouble()
                                        }else{
                                            targetValue = 0.0
                                        }
                                        val index = ingredientLocalListModel!!.data.indexOfFirst { it.food_name == ingredientName }
                                        var ingredientQuantity = 0.0
                                        if (foodData.quantity != null && foodData.quantity > 0.0){
                                            ingredientQuantity = foodData.quantity
                                        }else{
                                            ingredientQuantity = 1.0
                                        }
                                        val ingredientData = IngredientDetail(
                                            id = foodData.id,
                                            food_code = foodData.food_code,
                                            food_name = foodData.food_name,
                                            food_category = foodData.food_category,
                                            photo_url = foodData.photo_url,
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
                                            quantity =  quantityEdit.text.toString().toDouble(),
                                            measure = tvMeasure.text.toString()
                                        )
                                        if (index != -1) {
                                            ingredientLists[index] = ingredientData
                                        }
                                        ingredientLists.get(index)
                                        ingredientLocalListModel = IngredientLocalListModel(ingredientLists)
                                        Toast.makeText(activity, "Changes Save", Toast.LENGTH_SHORT).show()
                                        val fragment = CreateRecipeFragment()
                                        val args = Bundle()
                                        args.putString("ModuleName", moduleName)
                                        args.putString("selectedMealDate", selectedMealDate)
                                        args.putString("mealType", mealType)
                                        args.putString("recipeId", recipeId)
                                        args.putString("recipeName", recipeName)
                                        args.putDouble("serving", serving)
                                        args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
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

    private fun setDishData(snapRecipeData: IngredientDetail , isEdit: Boolean) {
        addToTheMealTV.text = "Add To The Recipe"
        val capitalized = snapRecipeData.food_name.toString().replaceFirstChar { it.uppercase() }
        tvMealName.text = capitalized
        if (snapRecipeData.measure != null){
            tvMeasure.text = snapRecipeData.measure
        }
        if (!isEdit){
            if (snapRecipeData.quantity != null ){
                if (snapRecipeData.quantity > 0.0){
                    quantityEdit.setText(snapRecipeData.quantity?.toString())
                }else{
                    quantityEdit.setText("1.0")
                }
            }
        }
        var imageUrl : String? = ""
        if (snapRecipeData.photo_url != null){
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

    private fun calculateValue(givenValue: Double?, defaultQuantity: Double, targetQuantity: Double): Double {
        return if (givenValue != null && defaultQuantity > 0.0) {
            (givenValue / defaultQuantity) * targetQuantity
        } else {
            0.0
        }
    }

    private fun onMacroNutrientsList(mealDetails: IngredientDetail,defaultValue: Double, targetValue: Double) {

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

    private fun onMicroNutrientsList(mealDetails: IngredientDetail, defaultValue: Double, targetValue: Double) {

//        val cholesterol = if (mealDetails.cholesterol != null){
//            calculateValue( mealDetails.cholesterol, defaultValue, targetValue).toInt().toString()
//        }else{
//            "0"
//        }

        val vitamin_A = if (mealDetails.vit_a_mcg != null){
            calculateValue( mealDetails.vit_a_mcg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val vitamin_C = if (mealDetails.vit_c_mg != null){
            calculateValue( mealDetails.vit_c_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val vitamin_k = if (mealDetails.vit_k_mcg != null){
            calculateValue( mealDetails.vit_k_mcg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val vitaminD = if (mealDetails.vit_d_mcg != null){
            calculateValue( mealDetails.vit_d_mcg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val folate = if (mealDetails.folate_b9_mcg != null){
            calculateValue( mealDetails.folate_b9_mcg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val iron_mg = if (mealDetails.iron_mg != null){
            calculateValue( mealDetails.iron_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val calcium = if (mealDetails.calcium_mg != null){
            calculateValue( mealDetails.calcium_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val magnesium = if (mealDetails.magnesium_mg != null){
            calculateValue( mealDetails.magnesium_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val potassium_mg = if (mealDetails.potassium_mg != null){
            calculateValue( mealDetails.potassium_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

//        val fiber_mg = if (mealDetails.fiber != null){
//            calculateValue( mealDetails.fiber, defaultValue, targetValue).toInt().toString()
//        }else{
//            "0"
//        }

        val zinc = if (mealDetails.zinc_mg != null){
            calculateValue( mealDetails.zinc_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val sodium = if (mealDetails.sodium_mg != null){
            calculateValue( mealDetails.sodium_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

//        val sugar_mg = if (mealDetails.sugar != null){
//            calculateValue( mealDetails.sugar, defaultValue, targetValue).toInt().toString()
//        }else{
//            "0"
//        }

        val vitB6 = if (mealDetails.vit_b6_mg != null){
            calculateValue( mealDetails.vit_b6_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val vitB12 = if (mealDetails.vit_b12_mcg != null){
            calculateValue( mealDetails.vit_b12_mcg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
        }

        val phosphorus = if (mealDetails.phosphorus_mg != null){
            calculateValue( mealDetails.phosphorus_mg, defaultValue, targetValue).toInt().toString()
        }else{
            "0"
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
            if (item.nutrientsValue != "0"){
                valueLists.add(item)
            }
        }
        val mealLogDateData: MicroNutrientsModel? = null
        microNutrientsAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMacroNutrientsItemClick(macroNutrientsModel: MacroNutrientsModel, position: Int, isRefresh: Boolean) {

    }

    private fun onMicroNutrientsItemClick(microNutrientsModel: MicroNutrientsModel, position: Int, isRefresh: Boolean) {

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