package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
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
import androidx.cardview.widget.CardView
import androidx.core.view.isGone
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.MealsResponse
import com.jetsynthesys.rightlife.ai_package.model.request.CreateMealRequest
import com.jetsynthesys.rightlife.ai_package.model.request.DishLog
import com.jetsynthesys.rightlife.ai_package.model.request.MealIngredient
import com.jetsynthesys.rightlife.ai_package.model.request.MealLog
import com.jetsynthesys.rightlife.ai_package.model.request.MealPlanRequest
import com.jetsynthesys.rightlife.ai_package.model.request.MealRecipe
import com.jetsynthesys.rightlife.ai_package.model.request.MealSaveRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateMealRequest
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import com.jetsynthesys.rightlife.ai_package.model.response.MealPlanResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMeal
import com.jetsynthesys.rightlife.ai_package.model.response.SnapRecipeData
import com.jetsynthesys.rightlife.ai_package.ui.eatright.MealSaveQuitBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.MealSaveQuitBottomSheet.OnMealSaveQuitListener
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.createmeal.DishListAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.DeleteDishBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.DeleteLogDishBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.HomeTabMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsLocalListModel
import com.jetsynthesys.rightlife.databinding.FragmentCreateMealBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CreateMealFragment : BaseFragment<FragmentCreateMealBinding>(), MealSaveQuitBottomSheet.OnMealSaveQuitListener {

    private lateinit var addedDishItemRecyclerview : RecyclerView
    private lateinit var etAddName : EditText
    private lateinit var tvContinue : TextView
    private lateinit var editMeal : ImageView
    private lateinit var backButton : ImageView
    private lateinit var btnAddLayout : LinearLayoutCompat
    private lateinit var editDeleteBreakfast : CardView
    private lateinit var editDeleteLunch : CardView
    private lateinit var addedMealListLayout : LinearLayoutCompat
    private lateinit var saveMealLayout : LinearLayoutCompat
    private lateinit var layoutNoDishes: LinearLayoutCompat
    private lateinit var addMealNameLayout : LinearLayoutCompat
    private lateinit var continueLayout : LinearLayoutCompat
    private lateinit var addedNameTv : TextView
    private var dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
    private  var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
    private var mealId : String = ""
    private lateinit var mealType : String
    private var mealName : String = ""
    private var loadingOverlay : FrameLayout? = null
    private var moduleName : String = ""
    private var selectedMealDate : String = ""
    private var currentToast: Toast? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCreateMealBinding
        get() = FragmentCreateMealBinding::inflate

    private val dishListAdapter by lazy { DishListAdapter(requireContext(), arrayListOf(), -1,
        null, false, :: onMealLogClickItem, :: onMealLogDeleteItem, :: onMealLogEditItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        addMealNameLayout = view.findViewById(R.id.layout_add_meal_name)
        etAddName = view.findViewById(R.id.et_add_name)
        continueLayout = view.findViewById(R.id.layout_continue)
        tvContinue = view.findViewById(R.id.tv_continue)
        addedDishItemRecyclerview = view.findViewById(R.id.recyclerview_added_dish_item)
        layoutNoDishes = view.findViewById(R.id.layout_no_dishes)
        saveMealLayout = view.findViewById(R.id.layout_save_meal)
        editMeal = view.findViewById(R.id.ic_edit)
        addedMealListLayout = view.findViewById(R.id.layout_added_meal_list)
        btnAddLayout = view.findViewById(R.id.layout_btnAdd)
        addedNameTv = view.findViewById(R.id.addedNameTv)
        backButton = view.findViewById(R.id.back_button)
//        editDeleteDinner = view.findViewById(R.id.btn_edit_delete_dinner)
//        layoutMain = view.findViewById(R.id.layout_main)
//        layoutDelete = view.findViewById(R.id.layout_delete)
//        layoutViewFood = view.findViewById(R.id.layout_view_food)
  //      val progressBar = view.findViewById<ProgressBar>(R.id.progressBar)
     //   val circleIndicator = view.findViewById<View>(R.id.circleIndicator)
        continueLayout.isEnabled = false
        continueLayout.setBackgroundResource(R.drawable.light_green_bg)

        addedDishItemRecyclerview.layoutManager = LinearLayoutManager(context)
        addedDishItemRecyclerview.adapter = dishListAdapter

        moduleName = arguments?.getString("ModuleName").toString()
        mealId = arguments?.getString("mealId").toString()
        mealType = arguments?.getString("mealType").toString()
        mealName = arguments?.getString("mealName").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()

       val dishLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", RecipeDetailsLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapDishLocalListModel")
        }

        if (dishLocalListModels != null){
            recipeDetailsLocalListModel = dishLocalListModels
            dishLists.addAll(recipeDetailsLocalListModel!!.data)
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
                if (dishLists.size > 0){
                    mealSaveQuitDialog()
                }else{
                    val fragment = HomeTabMealFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    args.putString("mealType", mealType)
                    args.putString("selectedMealDate", selectedMealDate)
                    args.putString("tabType", "MyMeal")
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
            if (dishLists.size > 0){
                mealSaveQuitDialog()
            }else{
                val fragment = HomeTabMealFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("mealType", mealType)
                args.putString("selectedMealDate", selectedMealDate)
                args.putString("tabType", "MyMeal")
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        }

        saveMealLayout.setOnClickListener {
            if (dishLists.isNotEmpty()){
                if (mealId != "null" && mealId != null){
                    updateMealsSave(dishLists)
                }else{
                    createMealsSave(dishLists)
                }
            }
        }

        continueLayout.setOnClickListener {
            addMealNameLayout.visibility = View.GONE
            continueLayout.visibility = View.GONE
            addedMealListLayout.visibility = View.VISIBLE
            saveMealLayout.visibility = View.VISIBLE
            addedNameTv.text = etAddName.text
            if (layoutNoDishes.isGone){
                saveMealLayout.isEnabled = true
                saveMealLayout.setBackgroundResource(R.drawable.green_meal_bg)
            }else{
                saveMealLayout.isEnabled = false
                saveMealLayout.setBackgroundResource(R.drawable.light_green_bg)
            }
        }

        if (dishLists.isNotEmpty()){
            addMealNameLayout.visibility = View.GONE
            continueLayout.visibility = View.GONE
            addedMealListLayout.visibility = View.VISIBLE
            saveMealLayout.visibility = View.VISIBLE
            if (mealName != "null"){
                addedNameTv.text = mealName
            }else{
                addedNameTv.text = etAddName.text
            }
//            if (serving > 0.0){
//                servingTv.text = serving.toString()
//            }
            if (layoutNoDishes.isGone){
                saveMealLayout.isEnabled = true
                saveMealLayout.setBackgroundResource(R.drawable.green_meal_bg)
            }else{
                saveMealLayout.isEnabled = false
                saveMealLayout.setBackgroundResource(R.drawable.light_green_bg)
            }
        }else{
            if (mealName != "null"){
                addedNameTv.text = mealName
                addMealNameLayout.visibility = View.GONE
                continueLayout.visibility = View.GONE
                addedMealListLayout.visibility = View.VISIBLE
                saveMealLayout.visibility = View.VISIBLE
            }else{
                addedNameTv.text = etAddName.text
                addMealNameLayout.visibility = View.VISIBLE
                continueLayout.visibility = View.VISIBLE
                addedMealListLayout.visibility = View.GONE
                saveMealLayout.visibility = View.GONE
            }
//            if (serving > 0.0){
//                servingTv.text = serving.toString()
//            }
            if (layoutNoDishes.isGone){
                saveMealLayout.isEnabled = true
                saveMealLayout.setBackgroundResource(R.drawable.green_meal_bg)
            }else{
                saveMealLayout.isEnabled = false
                saveMealLayout.setBackgroundResource(R.drawable.light_green_bg)
            }
        }

        editMeal.setOnClickListener {
            addMealNameLayout.visibility = View.VISIBLE
            addedMealListLayout.visibility = View.GONE
            saveMealLayout.visibility = View.GONE
            continueLayout.visibility = View.VISIBLE
            etAddName.setText(addedNameTv.text.toString())
            if (etAddName.text.length > 0){
                continueLayout.isEnabled = true
                continueLayout.setBackgroundResource(R.drawable.green_meal_bg)
            }else{
                continueLayout.isEnabled = false
                continueLayout.setBackgroundResource(R.drawable.light_green_bg)
            }
        }

        btnAddLayout.setOnClickListener {
            val fragment = SearchDishFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("selectedMealDate", selectedMealDate)
            args.putString("searchType", "createMeal")
            args.putString("mealId", mealId)
            args.putString("mealType", mealType)
            args.putString("mealName", addedNameTv.text.toString())
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }
        onMealLoggedList()
    }

    private fun onMealLoggedList (){
        if (dishLists.size > 0){
            addedDishItemRecyclerview.visibility = View.VISIBLE
            layoutNoDishes.visibility = View.GONE
            saveMealLayout.visibility = View.VISIBLE
        }else{
            layoutNoDishes.visibility = View.VISIBLE
            addedDishItemRecyclerview.visibility = View.GONE
            saveMealLayout.visibility = View.GONE
        }

        val valueLists : ArrayList<IngredientRecipeDetails> = ArrayList()
        valueLists.addAll(dishLists as Collection<IngredientRecipeDetails>)
        val mealLog: IngredientRecipeDetails? = null
        dishListAdapter.addAll(valueLists, -1, mealLog, false)
    }

    private fun onMealLogClickItem(mealLog: IngredientRecipeDetails, position: Int, isRefresh: Boolean) {

        val valueLists : ArrayList<IngredientRecipeDetails> = ArrayList()
        valueLists.addAll(dishLists as Collection<IngredientRecipeDetails>)
        dishListAdapter.addAll(valueLists, position, mealLog, isRefresh)
    }

    private fun onMealLogDeleteItem(mealItem: IngredientRecipeDetails, position: Int, isRefresh: Boolean) {

        val valueLists : ArrayList<IngredientRecipeDetails> = ArrayList()
        valueLists.addAll(dishLists as Collection<IngredientRecipeDetails>)
        dishListAdapter.addAll(valueLists, position, mealItem, isRefresh)
        deleteMealDialog(mealItem)
    }

    private fun onMealLogEditItem(mealItem: IngredientRecipeDetails, position: Int, isRefresh: Boolean) {

        val valueLists : ArrayList<IngredientRecipeDetails> = ArrayList()
        valueLists.addAll(dishLists as Collection<IngredientRecipeDetails>)
        dishListAdapter.addAll(valueLists, position, mealItem, isRefresh)

        requireActivity().supportFragmentManager.beginTransaction().apply {
            val fragment = DishFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("searchType", "createMeal")
            args.putString("selectedMealDate", selectedMealDate)
            args.putString("mealId", mealId)
            args.putString("mealType", mealType)
            args.putString("mealName", addedNameTv.text.toString())
            args.putString("snapRecipeName", mealItem.recipe)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            replace(R.id.flFragment, fragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun deleteMealDialog(mealItem: IngredientRecipeDetails){

        val deleteDishBottomSheet = DeleteDishBottomSheet()
        deleteDishBottomSheet.isCancelable = true
        val args = Bundle()
        args.putString("ModuleName", moduleName)
        args.putString("selectedMealDate", selectedMealDate)
        args.putString("mealId", mealId)
        args.putString("mealType", mealType)
        args.putString("mealName", addedNameTv.text.toString())
        args.putString("snapRecipeName", mealItem.recipe)
        args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
        deleteDishBottomSheet.arguments = args
        activity?.supportFragmentManager?.let { deleteDishBottomSheet.show(it, "DeleteDishBottomSheet") }
    }

    private fun createMealsSave(snapRecipeList : ArrayList<IngredientRecipeDetails>) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val mealLogList : ArrayList<MealLog> = ArrayList()
        val recipes: ArrayList<MealRecipe> = ArrayList()
        val ingredients: ArrayList<MealIngredient> = ArrayList()
        snapRecipeList?.forEach { mealItem ->
            if (mealItem.source.equals("recipe")){
                val mealRecipeData = MealRecipe(
                    recipe_id = mealItem.id,
                    meal_quantity = mealItem.quantity,
                    selected_serving_type = mealItem.selected_serving?.type,
                    selected_serving_value = mealItem.selected_serving?.value
                )
                recipes.add(mealRecipeData)
            }
            if (mealItem.source.equals("ingredient")){
                val mealIngredientData = MealIngredient(
                    ingredient_id = mealItem.id,
                    meal_quantity = mealItem.quantity,
                    standard_serving_size = mealItem.selected_serving?.type
                )
                ingredients.add(mealIngredientData)
            }
        }
        val createMealRequest = CreateMealRequest(
            meal_type = mealType,
            meal_name = addedNameTv.text.toString(),
            isFavourite = false,
            recipes = recipes,
            ingredients = ingredients
        )
        val call = ApiClient.apiServiceFastApiV2.createMealRequest(userId, createMealRequest)
        call.enqueue(object : Callback<MealUpdateResponse> {
            override fun onResponse(call: Call<MealUpdateResponse>, response: Response<MealUpdateResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealData = response.body()?.message
                    showCustomToast(requireContext(), mealData)
                   // Toast.makeText(activity, mealData, Toast.LENGTH_SHORT).show()
                    val fragment = HomeTabMealFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    args.putString("mealType", mealType)
                    args.putString("tabType", "MyMeal")
                    args.putString("selectedMealDate", selectedMealDate)
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

    private fun updateMealsSave(snapRecipeList : ArrayList<IngredientRecipeDetails>) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val recipes: ArrayList<MealRecipe> = ArrayList()
        val ingredients: ArrayList<MealIngredient> = ArrayList()
        snapRecipeList?.forEach { mealItem ->
            if (mealItem.source.equals("recipe")){
                val mealRecipeData = MealRecipe(
                    recipe_id = mealItem.id,
                    meal_quantity = mealItem.quantity,
                    selected_serving_type = mealItem.selected_serving?.type,
                    selected_serving_value = mealItem.selected_serving?.value
                )
                recipes.add(mealRecipeData)
            }
            if (mealItem.source.equals("ingredient")){
                val mealIngredientData = MealIngredient(
                    ingredient_id = mealItem.id,
                    meal_quantity = mealItem.quantity,
                    standard_serving_size = mealItem.selected_serving?.type
                )
                ingredients.add(mealIngredientData)
            }
        }
        val updateMealRequest = CreateMealRequest(
            meal_type = mealType,
            meal_name = addedNameTv.text.toString(),
            isFavourite = false,
            recipes = recipes,
            ingredients = ingredients
        )
        val call = ApiClient.apiServiceFastApiV2.updateSaveMeal(mealId, userId, updateMealRequest)
        call.enqueue(object : Callback<MealUpdateResponse> {
            override fun onResponse(call: Call<MealUpdateResponse>, response: Response<MealUpdateResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealData = response.body()?.message
                    showCustomToast(requireContext(), mealData)
                   // Toast.makeText(activity, mealData, Toast.LENGTH_SHORT).show()
                    val fragment = HomeTabMealFragment()
                    val args = Bundle()
                    args.putString("ModuleName", moduleName)
                    args.putString("tabType", "MyMeal")
                    args.putString("selectedMealDate", selectedMealDate)
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

    private fun showCustomToast(context: Context, message: String?) {
        // Cancel any old toast
        currentToast?.cancel()
        val inflater = LayoutInflater.from(context)
        val toastLayout = inflater.inflate(R.layout.custom_toast_ai_eat, null)
        val textView = toastLayout.findViewById<TextView>(R.id.toast_message)
        textView.text = message
        // ✅ Wrap layout inside FrameLayout to apply margins
        val container = FrameLayout(context)
        val params = FrameLayout.LayoutParams(
            FrameLayout.LayoutParams.MATCH_PARENT,
            FrameLayout.LayoutParams.WRAP_CONTENT
        )
        val marginInPx = (20 * context.resources.displayMetrics.density).toInt()
        params.setMargins(marginInPx, 0, marginInPx, 0)
        toastLayout.layoutParams = params
        container.addView(toastLayout)
        val toast = Toast(context)
        toast.duration = Toast.LENGTH_SHORT
        toast.view = container
        toast.setGravity(Gravity.BOTTOM or Gravity.FILL_HORIZONTAL, 0, 100)
        currentToast = toast
        toast.show()
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
        args.putString("mealType", mealType)
        args.putString("selectedMealDate", selectedMealDate)
        args.putString("tabType", "MyMeal")
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, "landing")
            addToBackStack("landing")
            commit()
        }
    }
}