package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab

import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.R.*
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.FrequentlyLoggedListAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.frequentlylogged.LogMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.frequentlylogged.LoggedBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MyMealModel
import com.jetsynthesys.rightlife.databinding.FragmentFrequentlyLoggedBinding
import com.google.android.flexbox.FlexboxLayout
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.response.FrequentRecipe
import com.jetsynthesys.rightlife.ai_package.model.response.FrequentRecipesResponse
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientDetailResponse
import com.jetsynthesys.rightlife.ai_package.model.response.RecipeDetailsResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.DishToLogFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.SnapDishFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal.CreateMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal.DishFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MealLogItems
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SelectedMealLogList
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SnapMealRequestLocalListModel
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class FrequentlyLoggedFragment : BaseFragment<FragmentFrequentlyLoggedBinding>() {

    private val sharedViewModel: SharedMealViewModel by activityViewModels()

    private lateinit var frequentlyLoggedRecyclerView : RecyclerView
    private lateinit var layoutNoMeals : LinearLayoutCompat
    private lateinit var layoutCreateMeal : LinearLayoutCompat
    private lateinit var loggedBottomSheetFragment : LoggedBottomSheet
    private lateinit var flexboxLayout : FlexboxLayout
    private lateinit var addDishBottomSheet : LinearLayout
   // private val ingredientsList = mutableListOf("Poha")
    val ingredientsList : ArrayList<MyMealModel> = ArrayList()
    private val mealLogList : ArrayList<MealLogItems> = ArrayList()
    private var frequentRecipeLogList : ArrayList<FrequentRecipe> = ArrayList()
    private var mealLogRequests : SelectedMealLogList? = null
    private lateinit var mealType : String
    private var loadingOverlay : FrameLayout? = null
    private var moduleName : String = ""
    private var selectedMealDate : String = ""
    private var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
    private var snapMealRequestLocalListModel : SnapMealRequestLocalListModel? = null
    private var snapMealLogRequests : SelectedMealLogList? = null

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentFrequentlyLoggedBinding
        get() = FragmentFrequentlyLoggedBinding::inflate

    private val frequentlyLoggedListAdapter by lazy { FrequentlyLoggedListAdapter(requireContext(), arrayListOf(),
        -1, null, false, :: onFrequentlyLoggedItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), color.meal_log_background))

        frequentlyLoggedRecyclerView = view.findViewById(R.id.recyclerview_frequently_logged_item)
        layoutNoMeals = view.findViewById(R.id.layout_no_meals)
        layoutCreateMeal = view.findViewById(R.id.layout_create_meal)
        flexboxLayout = view.findViewById(R.id.flexboxLayout)
        val btnAdd: LinearLayoutCompat = view.findViewById(R.id.layout_btnAdd)
        val btnLogMeal: LinearLayoutCompat = view.findViewById(R.id.layout_btnLogMeal)
        val layoutTitle = view.findViewById<LinearLayout>(R.id.layout_title)
        val checkCircle = view.findViewById<ImageView>(R.id.check_circle_icon)
        val loggedSuccess = view.findViewById<TextView>(R.id.tv_logged_success)
         addDishBottomSheet = view.findViewById<LinearLayout>(R.id.layout_add_dish_bottom_sheet)

        moduleName = arguments?.getString("ModuleName").toString()
        mealType = arguments?.getString("mealType").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()

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

        if (recipeDetailsLocalListModels != null){
            recipeDetailsLocalListModel = recipeDetailsLocalListModels
        }

        if (selectedMealLogListModels != null){
            mealLogRequests = selectedMealLogListModels
        }

        if (selectedSnapMealLogListModels != null){
            snapMealLogRequests = selectedSnapMealLogListModels
        }

        frequentlyLoggedRecyclerView.layoutManager = LinearLayoutManager(context)
        frequentlyLoggedRecyclerView.adapter = frequentlyLoggedListAdapter

//        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
//            override fun handleOnBackPressed() {
//                val fragment = YourMealLogsFragment()
//                val args = Bundle()
//                args.putString("ModuleName", moduleName)
//                args.putString("selectedMealDate", selectedMealDate)
//                fragment.arguments = args
//                requireActivity().supportFragmentManager.beginTransaction().apply {
//                    replace(R.id.flFragment, fragment, "landing")
//                    addToBackStack("landing")
//                    commit()
//                }
//            }
//        })

        getFrequentlyLog()

        layoutCreateMeal.setOnClickListener {
            val fragment = CreateMealFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("selectedMealDate", selectedMealDate)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }
        // Add button clicked (For demonstration, adding a dummy ingredient)
        btnAdd.setOnClickListener {
//            val newIngredient = "New Item ${ingredientsList.size + 1}"
//            ingredientsList.add(newIngredient)
//            updateIngredientChips()
            val fragment = LogMealFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("selectedMealDate", selectedMealDate)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        // Log Meal button click
        btnLogMeal.setOnClickListener {
            //   Toast.makeText(context, "Meal Logged Successfully!", Toast.LENGTH_SHORT).show()
            addDishBottomSheet.visibility = View.GONE
            loggedBottomSheetFragment = LoggedBottomSheet()
            loggedBottomSheetFragment.isCancelable = true
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("selectedMealDate", selectedMealDate)
            loggedBottomSheetFragment.arguments = args
            activity?.supportFragmentManager?.let { loggedBottomSheetFragment.show(it, "LoggedBottomSheet") }
        }
    }

    private fun onFrequentlyLoggedList (){
        if (!isAdded) return   // SAFETY

        if (frequentRecipeLogList.size > 0){
            frequentlyLoggedRecyclerView.visibility = View.VISIBLE
            layoutNoMeals.visibility = View.GONE
        }else{
            layoutNoMeals.visibility = View.VISIBLE
            frequentlyLoggedRecyclerView.visibility = View.GONE
        }
        val valueLists : ArrayList<FrequentRecipe> = ArrayList()
        valueLists.addAll(frequentRecipeLogList as Collection<FrequentRecipe>)
        val mealLogDateData: FrequentRecipe? = null
        frequentlyLoggedListAdapter.addAll(valueLists, -1, mealLogDateData, false)

        sharedViewModel.recipeLogAndFrequentlyData.observe(viewLifecycleOwner) { mealDataList ->
            // Update RecyclerView / UI with latest meal dat
            val mealLogDateData: FrequentRecipe? = null
            if (frequentRecipeLogList.isNotEmpty() && mealDataList.isNotEmpty()) {
                val valueLists : ArrayList<FrequentRecipe> = ArrayList()
                for (item in frequentRecipeLogList) {
                    // check if this item matches ANY meal in mealDataList
                    val matchFound = mealDataList.any { meal ->
                        item.id == meal.meal_id && item.recipe == meal.recipe_name
                    }
                    if (matchFound) {
                        item.isFrequentLog = true
                    }else{
                        item.isFrequentLog = false
                    }
                    valueLists.add(item)
                }
                frequentlyLoggedListAdapter.addAll(valueLists, -1, mealLogDateData, false)
            }else if (frequentRecipeLogList.isNotEmpty() && mealDataList.isEmpty()){
                val valueLists : ArrayList<FrequentRecipe> = ArrayList()
                for (item in frequentRecipeLogList) {
                    item.isFrequentLog = false
                    valueLists.add(item)
                }
                frequentlyLoggedListAdapter.addAll(valueLists, -1, mealLogDateData, false)
            }
        }
    }

    private fun onFrequentlyLoggedItem(frequentRecipe: FrequentRecipe, position: Int, isRefresh: Boolean) {

        if (frequentRecipe.source != null){
            when (frequentRecipe.source) {
                "recipe" -> getRecipesDetails(frequentRecipe.id)
                "ingredient" -> getIngredientDetails(frequentRecipe.id)
            }
        }
//        val valueLists : ArrayList<FrequentRecipe> = ArrayList()
//        valueLists.addAll(frequentRecipeLogList as Collection<FrequentRecipe>)
//        frequentlyLoggedListAdapter.addAll(valueLists, position, mealLogDateModel, isRefresh)
      //  addDishBottomSheet.visibility = View.VISIBLE
//        val newIngredient = mealLogDateModel
//        for (ingredient in valueLists) {
//            if (ingredient.isAddDish == true){
//                ingredientsList.add(newIngredient)
//                updateIngredientChips()
//            }
//        }
//        val mealLogData = MealLogItems(
//            meal_id = mealLogDateModel._id,
//            recipe_name = mealLogDateModel.recipe_name,
//            meal_quantity = 1,
//            unit = "g",
//            measure = "Bowl",
//            isMealLogSelect = mealLogDateModel.isFrequentLog
//        )
//        mealLogList.add(mealLogData)
//        val mealLogRequest = SelectedMealLogList(
//            meal_name = "",
//            meal_type = mealType,
//            meal_log = mealLogList
//        )
//        mealLogRequests = mealLogRequest
//        val parent = parentFragment as? HomeTabMealFragment
//        parent?.setSelectedFrequentlyLog(mealLogData, false, null, null)
//        requireActivity().supportFragmentManager.beginTransaction().apply {
//            val snapMealFragment = DishToLogFragment()
//            val args = Bundle()
//            args.putString("ModuleName", moduleName)
//            args.putString("searchType", searchType)
//            args.putString("mealType", mealType)
//            args.putString("selectedMealDate", selectedMealDate)
//            args.putParcelable("ingredientRecipesDetails", ingredientRecipesDetails)
//            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
//            args.putParcelable("selectedMealLogList", mealLogRequests)
//            args.putParcelable("selectedSnapMealLogList", snapMealLogRequests)
//            args.putParcelable("snapMealRequestLocalListModel", snapMealRequestLocalListModel)
//            snapMealFragment.arguments = args
//            replace(R.id.flFragment, snapMealFragment, "Steps")
//            addToBackStack(null)
//            commit()
//        }
    }

    // Function to update Flexbox with chips
    private fun updateIngredientChips() {
        flexboxLayout.removeAllViews() // Clear existing chips

        for (ingredient in ingredientsList) {
            val chipView = LayoutInflater.from(context).inflate(layout.chip_ingredient, flexboxLayout, false)
            val tvIngredient: TextView = chipView.findViewById(R.id.tvIngredient)
            val btnRemove: ImageView = chipView.findViewById(R.id.btnRemove)

            tvIngredient.text = ingredient.mealName
            btnRemove.setOnClickListener {
                ingredientsList.remove(ingredient)
                updateIngredientChips()
            }
            flexboxLayout.addView(chipView)
        }
    }

    private fun getFrequentlyLog() {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val call = ApiClient.apiServiceFastApiV2.getFrequentlyLog(userId)
        call.enqueue(object : Callback<FrequentRecipesResponse> {
            override fun onResponse(call: Call<FrequentRecipesResponse>, response: Response<FrequentRecipesResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    if (response.body()?.data?.frequent_recipes != null){
                        if (response.body()?.data?.frequent_recipes!!.isNotEmpty()){
                            frequentRecipeLogList.addAll(response.body()!!.data.frequent_recipes)
                            onFrequentlyLoggedList()
                        }
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
            override fun onFailure(call: Call<FrequentRecipesResponse>, t: Throwable) {
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

    private fun getIngredientDetails(ingredientId : String?) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val call = ApiClient.apiServiceFastApiV2.getIngredientDetails(ingredientId!!)
        call.enqueue(object : Callback<IngredientDetailResponse> {
            override fun onResponse(call: Call<IngredientDetailResponse>, response: Response<IngredientDetailResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val ingredientRecipesDetails = response.body()?.data
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        val snapMealFragment = DishToLogFragment()
                        val args = Bundle()
                        args.putString("ModuleName", moduleName)
                        args.putString("searchType", "Frequently")
                        args.putString("mealType", mealType)
                        args.putString("selectedMealDate", selectedMealDate)
                        args.putParcelable("ingredientRecipeDetails", ingredientRecipesDetails)
                        args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
                        args.putParcelable("selectedMealLogList", mealLogRequests)
                        args.putParcelable("selectedSnapMealLogList", snapMealLogRequests)
                        args.putParcelable("snapMealRequestLocalListModel", snapMealRequestLocalListModel)
                        snapMealFragment.arguments = args
                        replace(R.id.flFragment, snapMealFragment, "Steps")
                        addToBackStack(null)
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
            override fun onFailure(call: Call<IngredientDetailResponse>, t: Throwable) {
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

    private fun getRecipesDetails(recipeId : String?) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val call = ApiClient.apiServiceFastApiV2.getRecipeDetailsById(recipeId = recipeId!!)
        call.enqueue(object : Callback<RecipeDetailsResponse> {
            override fun onResponse(call: Call<RecipeDetailsResponse>, response: Response<RecipeDetailsResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val ingredientRecipesDetails = response.body()?.data
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        val snapMealFragment = DishToLogFragment()
                        val args = Bundle()
                        args.putString("ModuleName", moduleName)
                        args.putString("searchType", "Frequently")
                        args.putString("mealType", mealType)
                        args.putString("selectedMealDate", selectedMealDate)
                        args.putParcelable("ingredientRecipeDetails", ingredientRecipesDetails)
                        args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
                        args.putParcelable("selectedMealLogList", mealLogRequests)
                        args.putParcelable("selectedSnapMealLogList", snapMealLogRequests)
                        args.putParcelable("snapMealRequestLocalListModel", snapMealRequestLocalListModel)
                        snapMealFragment.arguments = args
                        replace(R.id.flFragment, snapMealFragment, "Steps")
                        addToBackStack(null)
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
            override fun onFailure(call: Call<RecipeDetailsResponse>, t: Throwable) {
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

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }
}