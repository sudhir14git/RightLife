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
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientDetail
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.IngredientListAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.DeleteIngredientBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.HomeTabMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.IngredientDishFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.SearchIngredientFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.IngredientLocalListModel
import com.jetsynthesys.rightlife.ai_package.utils.LoaderUtil
import com.jetsynthesys.rightlife.databinding.FragmentCreateRecipeBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import androidx.core.view.isGone

class CreateRecipeFragment : BaseFragment<FragmentCreateRecipeBinding>() {

    private lateinit var backButton : ImageView
    private lateinit var addedIngredientsRecyclerview : RecyclerView
    private lateinit var etAddName : EditText
    private lateinit var tvContinue : TextView
    private lateinit var editRecipe : ImageView
    private lateinit var addedNameTv : TextView
    private lateinit var btnAddLayout : LinearLayoutCompat
    private lateinit var addedRecipeListLayout : LinearLayoutCompat
    private lateinit var saveRecipeLayout : LinearLayoutCompat
    private lateinit var layoutNoIngredients: LinearLayoutCompat
    private lateinit var addRecipeNameLayout : LinearLayoutCompat
    private lateinit var continueLayout : LinearLayoutCompat
    private var ingredientLists : ArrayList<IngredientDetail> = ArrayList()
    private var ingredientLocalListModel : IngredientLocalListModel? = null
    private var recipeId : String = ""
    private var recipeName : String = ""

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
        continueLayout.isEnabled = false
        continueLayout.setBackgroundResource(R.drawable.light_green_bg)

        addedIngredientsRecyclerview.layoutManager = LinearLayoutManager(context)
        addedIngredientsRecyclerview.adapter = ingredientListAdapter

        recipeId = arguments?.getString("recipeId").toString()
        recipeName = arguments?.getString("recipeName").toString()

        val ingredientLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("ingredientLocalListModel", IngredientLocalListModel::class.java)
        } else {
            arguments?.getParcelable("ingredientLocalListModel")
        }

        if (ingredientLocalListModels != null){
            ingredientLocalListModel = ingredientLocalListModels
            ingredientLists.addAll(ingredientLocalListModel!!.data)
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
                val fragment = HomeTabMealFragment()
                val args = Bundle()
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        })

        onIngredientList()

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

        if (ingredientLists.isNotEmpty()){
            addRecipeNameLayout.visibility = View.GONE
            continueLayout.visibility = View.GONE
            addedRecipeListLayout.visibility = View.VISIBLE
            saveRecipeLayout.visibility = View.VISIBLE
            if (recipeName != "null"){
                addedNameTv.text = recipeName
            }else{
                addedNameTv.text = etAddName.text
            }
            if (layoutNoIngredients.isGone){
                saveRecipeLayout.isEnabled = true
                saveRecipeLayout.setBackgroundResource(R.drawable.green_meal_bg)
            }else{
                saveRecipeLayout.isEnabled = false
                saveRecipeLayout.setBackgroundResource(R.drawable.light_green_bg)
            }
        }else{
            addRecipeNameLayout.visibility = View.VISIBLE
            continueLayout.visibility = View.VISIBLE
            addedRecipeListLayout.visibility = View.GONE
            saveRecipeLayout.visibility = View.GONE
            if (recipeName != "null"){
                addedNameTv.text = recipeName
            }else{
                addedNameTv.text = etAddName.text
            }
            if (layoutNoIngredients.isGone){
                saveRecipeLayout.isEnabled = true
                saveRecipeLayout.setBackgroundResource(R.drawable.green_meal_bg)
            }else{
                saveRecipeLayout.isEnabled = false
                saveRecipeLayout.setBackgroundResource(R.drawable.light_green_bg)
            }
        }

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
            args.putString("searchType", "createRecipe")
            args.putString("recipeId", recipeId)
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
            val fragment = HomeTabMealFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
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

        val valueLists : ArrayList<IngredientDetail> = ArrayList()
        valueLists.addAll(ingredientLists as Collection<IngredientDetail>)
        val mealLog: IngredientDetail? = null
        ingredientListAdapter.addAll(valueLists, -1, mealLog, false)
    }

    private fun onIngredientClickItem(mealLogDateModel: IngredientDetail, position: Int, isRefresh: Boolean) {

    }

    private fun onIngredientDeleteItem(mealItem: IngredientDetail, position: Int, isRefresh: Boolean) {

        val valueLists : ArrayList<IngredientDetail> = ArrayList()
        valueLists.addAll(ingredientLists as Collection<IngredientDetail>)
        ingredientListAdapter.addAll(valueLists, position, mealItem, isRefresh)

        deleteIngredientDialog(mealItem)
    }

    private fun onIngredientEditItem(mealItem: IngredientDetail, position: Int, isRefresh: Boolean) {
        val valueLists : ArrayList<IngredientDetail> = ArrayList()
        valueLists.addAll(ingredientLists as Collection<IngredientDetail>)
        ingredientListAdapter.addAll(valueLists, position, mealItem, isRefresh)

        requireActivity().supportFragmentManager.beginTransaction().apply {
            val fragment = IngredientDishFragment()
            val args = Bundle()
            args.putString("searchType", "createMeal")
            args.putString("recipeId", recipeId)
            args.putString("recipeName", addedNameTv.text.toString())
            args.putString("ingredientName", mealItem.ingredient_name)
            args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
            fragment.arguments = args
            replace(R.id.flFragment, fragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }
    private fun deleteIngredientDialog(ingredientItem: IngredientDetail){

        val deleteDishBottomSheet = DeleteIngredientBottomSheet()
        deleteDishBottomSheet.isCancelable = true
        val args = Bundle()
        args.putBoolean("test",false)
        args.putString("recipeId", recipeId)
        args.putString("recipeName", addedNameTv.text.toString())
        args.putString("ingredientName", ingredientItem.ingredient_name)
        args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
        deleteDishBottomSheet.arguments = args
        activity?.supportFragmentManager?.let { deleteDishBottomSheet.show(it, "DeleteDishBottomSheet") }
    }

    private fun createRecipe(ingredientList : ArrayList<IngredientDetail>) {
        LoaderUtil.showLoader(requireActivity())
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val ingredientLists : ArrayList<IngredientEntry> = ArrayList()
        ingredientList?.forEach { ingredient ->
            val ingredientData = IngredientEntry(
                ingredient_id = ingredient.id,
                quantity = ingredient.quantity?.toInt(),
                measure = ingredient.unit
            )
            ingredientLists.add(ingredientData)
        }
        val recipeRequest = CreateRecipeRequest(
            recipe_name = addedNameTv.text.toString(),
            ingredients = ingredientLists,
            total_time = "",
            course_one_or_more = "",
            tags_optional = "",
            cuisine_optional = "",
            photo_url = ""
        )
        val call = ApiClient.apiServiceFastApi.createRecipe(userId, recipeRequest)
        call.enqueue(object : Callback<MealUpdateResponse> {
            override fun onResponse(call: Call<MealUpdateResponse>, response: Response<MealUpdateResponse>) {
                if (response.isSuccessful) {
                    LoaderUtil.dismissLoader(requireActivity())
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
                    LoaderUtil.dismissLoader(requireActivity())
                }
            }
            override fun onFailure(call: Call<MealUpdateResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                LoaderUtil.dismissLoader(requireActivity())
            }
        })
    }

    private fun updateRecipe(ingredientList : ArrayList<IngredientDetail>) {
        LoaderUtil.showLoader(requireActivity())
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val ingredientLists : ArrayList<IngredientEntry> = ArrayList()
        ingredientList?.forEach { ingredient ->
            val ingredientData = IngredientEntry(
                ingredient_id = ingredient.id,
                quantity = ingredient.quantity?.toInt(),
                measure = ingredient.unit
            )
            ingredientLists.add(ingredientData)
        }
        val updateRecipeRequest = CreateRecipeRequest(
            recipe_name = addedNameTv.text.toString(),
            ingredients = ingredientLists,
            total_time = "",
            course_one_or_more = "",
            tags_optional = "",
            cuisine_optional = "",
            photo_url = ""
        )
        val call = ApiClient.apiServiceFastApi.updateRecipe(recipeId, userId, updateRecipeRequest)
        call.enqueue(object : Callback<MealUpdateResponse> {
            override fun onResponse(call: Call<MealUpdateResponse>, response: Response<MealUpdateResponse>) {
                if (response.isSuccessful) {
                    LoaderUtil.dismissLoader(requireActivity())
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
                    LoaderUtil.dismissLoader(requireActivity())
                }
            }
            override fun onFailure(call: Call<MealUpdateResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                LoaderUtil.dismissLoader(requireActivity())
            }
        })
    }
}