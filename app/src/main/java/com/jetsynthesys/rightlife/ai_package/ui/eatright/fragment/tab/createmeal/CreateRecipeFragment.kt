package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.request.CreateRecipeRequest
import com.jetsynthesys.rightlife.ai_package.model.request.IngredientEntry
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.IngredientListAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.DeleteIngredientBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.HomeTabMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.IngredientDishFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.SearchIngredientFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.IngredientLocalListModel
import com.jetsynthesys.rightlife.databinding.FragmentCreateRecipeBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.core.view.isGone
import com.jetsynthesys.rightlife.ai_package.model.response.MyRecipeDetailsResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.MealSaveQuitBottomSheet

class CreateRecipeFragment : BaseFragment<FragmentCreateRecipeBinding>(), MealSaveQuitBottomSheet.OnMealSaveQuitListener {

    private lateinit var backButton : ImageView
    private lateinit var addedIngredientsRecyclerview : RecyclerView
    private lateinit var etAddName : EditText
    private lateinit var tvContinue : TextView
    private lateinit var editRecipe : ImageView
    private lateinit var addedNameTv : TextView
    private lateinit var servingTv : TextView
    private lateinit var btnAddLayout : LinearLayoutCompat
    private lateinit var addedRecipeListLayout : LinearLayoutCompat
    private lateinit var saveRecipeLayout : LinearLayoutCompat
    private lateinit var layoutNoIngredients: LinearLayoutCompat
    private lateinit var addRecipeNameLayout : LinearLayoutCompat
    private lateinit var continueLayout : LinearLayoutCompat
    private var ingredientLists : ArrayList<IngredientRecipeDetails> = ArrayList()
    private var ingredientLocalListModel : IngredientLocalListModel? = null
    private var recipeId : String = ""
    private var recipeName : String = ""
    private var serving : Double = 0.0
    private var quantity = 1
    private var loadingOverlay : FrameLayout? = null
    private var moduleName : String = ""
    private var selectedMealDate : String = ""
    private lateinit var mealType : String

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCreateRecipeBinding
        get() = FragmentCreateRecipeBinding::inflate

    private val ingredientListAdapter by lazy { IngredientListAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onIngredientClickItem, :: onIngredientDeleteItem, :: onIngredientEditItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addRecipeNameLayout = view.findViewById(R.id.layout_add_recipe_name)
        etAddName = view.findViewById(R.id.et_add_name)
        continueLayout = view.findViewById(R.id.layout_continue)
        tvContinue = view.findViewById(R.id.tv_continue)
        addedIngredientsRecyclerview = view.findViewById(R.id.recyclerview_added_ingredients_item)
        layoutNoIngredients = view.findViewById(R.id.layout_noIngredients)
        saveRecipeLayout = view.findViewById(R.id.layout_save_recipe)
        editRecipe = view.findViewById(R.id.ic_edit)
        addedRecipeListLayout = view.findViewById(R.id.layout_added_recipe_list)
        btnAddLayout = view.findViewById(R.id.layout_btnAdd)
        addedNameTv = view.findViewById(R.id.addedNameTv)
        backButton = view.findViewById(R.id.backButton)
        servingTv = view.findViewById(R.id.servingTv)
        continueLayout.isEnabled = false
        continueLayout.setBackgroundResource(R.drawable.light_green_bg)

        addedIngredientsRecyclerview.layoutManager = LinearLayoutManager(context)
        addedIngredientsRecyclerview.adapter = ingredientListAdapter

        moduleName = arguments?.getString("ModuleName").toString()
        recipeId = arguments?.getString("recipeId").toString()
        recipeName = arguments?.getString("recipeName").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()
        serving = arguments?.getDouble("serving")?.toDouble() ?: 0.0
        mealType = arguments?.getString("mealType").toString()
        val updateMyRecipe = arguments?.getString("updateMyRecipe").toString()

        if (updateMyRecipe != "null"){
            getMyRecipesDetails(recipeId)
        }

        val ingredientLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("ingredientLocalListModel", IngredientLocalListModel::class.java)
        } else {
            arguments?.getParcelable("ingredientLocalListModel")
        }

        if (ingredientLocalListModels != null){
            ingredientLocalListModel = ingredientLocalListModels
            ingredientLists.addAll(ingredientLocalListModel!!.data)
            addRecipeNameLayout.visibility = View.GONE
            continueLayout.visibility = View.GONE
            addedRecipeListLayout.visibility = View.VISIBLE
            saveRecipeLayout.visibility = View.VISIBLE
            onIngredientList()
        }else{
            if (updateMyRecipe == "null"){
                addRecipeNameLayout.visibility = View.VISIBLE
                continueLayout.visibility = View.VISIBLE
                addedRecipeListLayout.visibility = View.GONE
                saveRecipeLayout.visibility = View.GONE
            }
            onIngredientList()
        }

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
                if (ingredientLists.size > 0){
                    mealSaveQuitDialog()
                }else{
                    val fragment = HomeTabMealFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    args.putString("selectedMealDate", selectedMealDate)
                    args.putString("mealType", mealType)
                    args.putString("tabType", "MyRecipe")
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }
            }
        })

        view.findViewById<ImageView>(R.id.ivDecrease).setOnClickListener {
            if (quantity > 1) {
                quantity--
                servingTv.text = quantity.toString()
            }
        }

        view.findViewById<ImageView>(R.id.ivIncrease).setOnClickListener {
            quantity++
            servingTv.text = quantity.toString()
        }

        saveRecipeLayout.setOnClickListener {

            if (ingredientLists.isNotEmpty()){
                if (recipeId != "null"){
                    updateRecipe(ingredientLists)
                }else{
                createRecipe(ingredientLists)
               }
            }
        }

        continueLayout.setOnClickListener {
            addRecipeNameLayout.visibility = View.GONE
            continueLayout.visibility = View.GONE
            addedRecipeListLayout.visibility = View.VISIBLE
            saveRecipeLayout.visibility = View.VISIBLE
            addedNameTv.text = etAddName.text
            if (layoutNoIngredients.isGone){
                saveRecipeLayout.isEnabled = true
                saveRecipeLayout.setBackgroundResource(R.drawable.green_meal_bg)
            }else{
                saveRecipeLayout.isEnabled = false
                saveRecipeLayout.setBackgroundResource(R.drawable.light_green_bg)
            }
        }

//        if (ingredientLists.isNotEmpty()){
//            addRecipeNameLayout.visibility = View.GONE
//            continueLayout.visibility = View.GONE
//            addedRecipeListLayout.visibility = View.VISIBLE
//            saveRecipeLayout.visibility = View.VISIBLE
//        }else{
//            addRecipeNameLayout.visibility = View.VISIBLE
//            continueLayout.visibility = View.VISIBLE
//            addedRecipeListLayout.visibility = View.GONE
//            saveRecipeLayout.visibility = View.GONE
//        }

        if (etAddName.text.length > 0){
            continueLayout.isEnabled = true
            continueLayout.setBackgroundResource(R.drawable.green_meal_bg)
        }else{
            continueLayout.isEnabled = false
            continueLayout.setBackgroundResource(R.drawable.light_green_bg)
        }

        editRecipe.setOnClickListener {
            addRecipeNameLayout.visibility = View.VISIBLE
            addedRecipeListLayout.visibility = View.GONE
            saveRecipeLayout.visibility = View.GONE
            continueLayout.visibility = View.VISIBLE
            etAddName.setText( addedNameTv.text)
            if (etAddName.text.length > 0){
                continueLayout.isEnabled = true
                continueLayout.setBackgroundResource(R.drawable.green_meal_bg)
            }else{
                continueLayout.isEnabled = false
                continueLayout.setBackgroundResource(R.drawable.light_green_bg)
            }
        }

        btnAddLayout.setOnClickListener {
            val fragment = SearchIngredientFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("selectedMealDate", selectedMealDate)
            args.putString("mealType", mealType)
            args.putString("searchType", "createRecipe")
            args.putString("recipeId", recipeId)
            args.putDouble("serving", servingTv.text.toString().toDouble())
            args.putString("recipeName", addedNameTv.text.toString())
            args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        backButton.setOnClickListener {
            if (ingredientLists.size > 0){
                mealSaveQuitDialog()
            }else{
                val fragment = HomeTabMealFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                args.putString("mealType", mealType)
                args.putString("tabType", "MyRecipe")
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        }
    }

    private fun onIngredientList (){

        if (ingredientLists.size > 0){
            addedIngredientsRecyclerview.visibility = View.VISIBLE
            layoutNoIngredients.visibility = View.GONE
            saveRecipeLayout.visibility = View.VISIBLE
        }else{
            layoutNoIngredients.visibility = View.VISIBLE
            addedIngredientsRecyclerview.visibility = View.GONE
            saveRecipeLayout.visibility = View.GONE
        }

        if (recipeName != "null"){
            addedNameTv.text = recipeName
        }else{
            addedNameTv.text = etAddName.text
        }

        if (serving > 0.0){
            servingTv.text = serving.toString()
        }
        if (layoutNoIngredients.isGone){
            saveRecipeLayout.isEnabled = true
            saveRecipeLayout.setBackgroundResource(R.drawable.green_meal_bg)
        }else{
            saveRecipeLayout.isEnabled = false
            saveRecipeLayout.setBackgroundResource(R.drawable.light_green_bg)
        }

        val valueLists : ArrayList<IngredientRecipeDetails> = ArrayList()
        valueLists.addAll(ingredientLists as Collection<IngredientRecipeDetails>)
        val mealLog: IngredientRecipeDetails? = null
        ingredientListAdapter.addAll(valueLists, -1, mealLog, false)
    }

    private fun onIngredientClickItem(mealLogDateModel: IngredientRecipeDetails, position: Int, isRefresh: Boolean) {

    }

    private fun onIngredientDeleteItem(mealItem: IngredientRecipeDetails, position: Int, isRefresh: Boolean) {

        val valueLists : ArrayList<IngredientRecipeDetails> = ArrayList()
        valueLists.addAll(ingredientLists as Collection<IngredientRecipeDetails>)
        ingredientListAdapter.addAll(valueLists, position, mealItem, isRefresh)
        deleteIngredientDialog(mealItem)
    }

    private fun onIngredientEditItem(mealItem: IngredientRecipeDetails, position: Int, isRefresh: Boolean) {
        val valueLists : ArrayList<IngredientRecipeDetails> = ArrayList()
        valueLists.addAll(ingredientLists as Collection<IngredientRecipeDetails>)
        ingredientListAdapter.addAll(valueLists, position, mealItem, isRefresh)

        requireActivity().supportFragmentManager.beginTransaction().apply {
            val fragment = IngredientDishFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("selectedMealDate", selectedMealDate)
            args.putString("mealType", mealType)
            args.putString("searchType", "createMeal")
            args.putString("recipeId", recipeId)
            args.putDouble("serving", servingTv.text.toString().toDouble())
            args.putString("recipeName", addedNameTv.text.toString())
            args.putString("ingredientName", mealItem.food_name)
            args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
            fragment.arguments = args
            replace(R.id.flFragment, fragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun deleteIngredientDialog(ingredientItem: IngredientRecipeDetails){

        val deleteDishBottomSheet = DeleteIngredientBottomSheet()
        deleteDishBottomSheet.isCancelable = true
        val args = Bundle()
        args.putString("ModuleName", moduleName)
        args.putString("selectedMealDate", selectedMealDate)
        args.putString("mealType", mealType)
        args.putString("recipeId", recipeId)
        args.putDouble("serving", servingTv.text.toString().toDouble())
        args.putString("recipeName", addedNameTv.text.toString())
        args.putString("ingredientName", ingredientItem.food_name)
        args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
        deleteDishBottomSheet.arguments = args
        activity?.supportFragmentManager?.let { deleteDishBottomSheet.show(it, "DeleteDishBottomSheet") }
    }

    private fun createRecipe(ingredientList : ArrayList<IngredientRecipeDetails>) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val ingredientLists : ArrayList<IngredientEntry> = ArrayList()
        ingredientList?.forEach { ingredient ->
            val ingredientData = IngredientEntry(
                ingredient_id = ingredient.id,
                quantity = ingredient.quantity?.toInt(),
                standard_serving_size = ingredient.selected_serving?.type
            )
            ingredientLists.add(ingredientData)
        }
        val recipeRequest = CreateRecipeRequest(
            recipe_name = addedNameTv.text.toString(),
            ingredients = ingredientLists,
            servings = servingTv.text.toString().toDouble()
        )
        val call = ApiClient.apiServiceFastApiV2.createRecipe(userId, recipeRequest)
        call.enqueue(object : Callback<MealUpdateResponse> {
            override fun onResponse(call: Call<MealUpdateResponse>, response: Response<MealUpdateResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealData = response.body()?.message
                    Toast.makeText(activity, mealData, Toast.LENGTH_SHORT).show()
                    val fragment = HomeTabMealFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    args.putString("selectedMealDate", selectedMealDate)
                    args.putString("mealType", mealType)
                    args.putString("tabType", "MyRecipe")
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
            override fun onFailure(call: Call<MealUpdateResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    private fun updateRecipe(ingredientList : ArrayList<IngredientRecipeDetails>) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val ingredientLists : ArrayList<IngredientEntry> = ArrayList()
        ingredientList?.forEach { ingredient ->
            val ingredientData = IngredientEntry(
                ingredient_id = ingredient.id,
                quantity = ingredient.quantity?.toInt(),
                standard_serving_size = ingredient.selected_serving?.type
            )
            ingredientLists.add(ingredientData)
        }
        val updateRecipeRequest = CreateRecipeRequest(
            recipe_name = addedNameTv.text.toString(),
            ingredients = ingredientLists,
            servings = servingTv.text.toString().toDouble(),
        )
        val call = ApiClient.apiServiceFastApiV2.updateRecipe(recipeId, userId, updateRecipeRequest)
        call.enqueue(object : Callback<MealUpdateResponse> {
            override fun onResponse(call: Call<MealUpdateResponse>, response: Response<MealUpdateResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealData = response.body()?.message
                    Toast.makeText(activity, mealData, Toast.LENGTH_SHORT).show()
                    val fragment = HomeTabMealFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    args.putString("selectedMealDate", selectedMealDate)
                    args.putString("mealType", mealType)
                    args.putString("tabType", "MyRecipe")
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
            override fun onFailure(call: Call<MealUpdateResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    private fun getMyRecipesDetails(recipeId : String) {
        Log.d("RecipeDetails", "getMyRecipesDetails called with recipeId: $recipeId")

        if (isAdded  && view != null){
            Log.d("RecipeDetails", "Fragment is added and view is not null, showing loader")
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        } else {
            Log.w("RecipeDetails", "Fragment not added or view is null, skipping loader")
        }

        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        Log.d("RecipeDetails", "UserId retrieved: $userId")

        val call = ApiClient.apiServiceFastApiV2.getMyRecipesDetails(userId, recipeId)
        Log.d("RecipeDetails", "API call initiated for userId: $userId, recipeId: $recipeId")

        call.enqueue(object : Callback<MyRecipeDetailsResponse> {
            override fun onResponse(call: Call<MyRecipeDetailsResponse>, response: Response<MyRecipeDetailsResponse>) {
                Log.d("RecipeDetails", "onResponse received - isSuccessful: ${response.isSuccessful}, code: ${response.code()}")

                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        Log.d("RecipeDetails", "Dismissing loader after successful response")
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }

                    if (response.body()?.data != null){
                        val ingredientList = response.body()?.data!!.ingredients
                        Log.d("RecipeDetails", "Recipe data received with ${ingredientList.size} ingredients")

                        val myIngredientLists : ArrayList<IngredientRecipeDetails> = ArrayList()

                        ingredientList.forEachIndexed { index, foodData ->
                            Log.d("RecipeDetails", "Processing ingredient $index: ${foodData.food_name}, quantity: ${foodData.quantity}")

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
                                calories_kcal = foodData.calories_kcal,
                                protein_g = foodData.protein_g,
                                fat_g = foodData.fat_g,
                                carbs_g = foodData.carbs_g,
                                fiber_g = foodData.fiber_g,
                                sugars_g = foodData.sugars_g,
                                vit_a_mcg = foodData.vit_a_mcg,
                                vit_c_mg = foodData.vit_c_mg,
                                vit_d_mcg = foodData.vit_d_mcg,
                                vit_e_mg = foodData.vit_e_mg,
                                vit_k_mcg = foodData.vit_k_mcg,
                                thiamin_b1_mg = foodData.thiamin_b1_mg,
                                riboflavin_b2_mg = foodData.riboflavin_b2_mg,
                                niacin_b3_mg = foodData.niacin_b3_mg,
                                vit_b6_mg = foodData.vit_b6_mg,
                                folate_b9_mcg = foodData.folate_b9_mcg,
                                vit_b12_mcg = foodData.vit_b12_mcg,
                                calcium_mg = foodData.calcium_mg,
                                iron_mg = foodData.iron_mg,
                                magnesium_mg = foodData.magnesium_mg,
                                zinc_mg = foodData.zinc_mg,
                                potassium_mg = foodData.potassium_mg,
                                sodium_mg = foodData.sodium_mg,
                                phosphorus_mg = foodData.phosphorus_mg,
                                omega3_g = foodData.omega3_g,
                                ingredients = foodData.ingredients,
                                preparation_notes = foodData.preparation_notes,
                                active_cooking_time_min = foodData.active_cooking_time_min,
                                allergy_groups_restricted_from_consuming = foodData.allergy_groups_restricted_from_consuming,
                                tags = foodData.tags,
                                typical_1person_serving = foodData.typical_1person_serving,
                                household_measure_1_serving = foodData.household_measure_1_serving,
                                photo_url = foodData.photo_url,
                                selected_serving = foodData.selected_serving,
                                default_serving = foodData.default_serving,
                                available_serving = foodData.available_serving,
                                source = foodData.source,
                                quantity =  foodData.quantity,
                                servings = foodData.servings
                            )
                            myIngredientLists.add(ingredientData)
                        }

                        Log.d("RecipeDetails", "Total ingredients processed: ${myIngredientLists.size}")

                        ingredientLocalListModel = IngredientLocalListModel(myIngredientLists)
                        ingredientLists.addAll(myIngredientLists)

                        Log.d("RecipeDetails", "ingredientLists updated, total count: ${ingredientLists.size}")
                        Log.d("RecipeDetails", "Updating UI visibility")

                        addRecipeNameLayout.visibility = View.GONE
                        continueLayout.visibility = View.GONE
                        addedRecipeListLayout.visibility = View.VISIBLE
                        saveRecipeLayout.visibility = View.VISIBLE

                        Log.d("RecipeDetails", "Calling onIngredientList()")
                        onIngredientList()
                        Log.d("RecipeDetails", "Recipe details loaded successfully")
                    } else {
                        Log.w("RecipeDetails", "Response body or data is null")
                    }
                } else {
                    Log.e("RecipeDetails", "Response not successful: ${response.code()}")
                    Log.e("RecipeDetails", "Error body: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()

                    if (isAdded  && view != null){
                        Log.d("RecipeDetails", "Dismissing loader after error response")
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<MyRecipeDetailsResponse>, t: Throwable) {
                Log.e("RecipeDetails", "API call failed: ${t.message}")
                Log.e("RecipeDetails", "Exception: ${t.javaClass.simpleName}")
                t.printStackTrace()

                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()

                if (isAdded  && view != null){
                    Log.d("RecipeDetails", "Dismissing loader after API failure")
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    private fun mealSaveQuitDialog() {
        val mealSaveQuitBottomSheet = MealSaveQuitBottomSheet()
        mealSaveQuitBottomSheet.isCancelable = true
        parentFragment.let { mealSaveQuitBottomSheet.show(childFragmentManager, "MealSaveQuitBottomSheet") }
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    override fun onMealSaveQuit(mealData: String) {
        val fragment = HomeTabMealFragment()
        val args = Bundle()
        args.putString("ModuleName", moduleName)
        args.putString("selectedMealDate", selectedMealDate)
        args.putString("mealType", mealType)
        args.putString("tabType", "MyRecipe")
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, "landing")
            addToBackStack("landing")
            commit()
        }
    }
}