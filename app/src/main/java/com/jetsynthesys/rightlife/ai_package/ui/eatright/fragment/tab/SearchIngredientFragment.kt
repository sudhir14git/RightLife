package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab

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
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientDetailResponse
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeList
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.IngredientSearchAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal.CreateRecipeFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.IngredientLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.viewmodel.SearchIngredientsViewModel
import com.jetsynthesys.rightlife.ai_package.utils.AppPreference
import com.jetsynthesys.rightlife.databinding.FragmentSearchDishBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchIngredientFragment : BaseFragment<FragmentSearchDishBinding>() {

    private lateinit var searchLayout : LinearLayoutCompat
    private lateinit var searchEditText : EditText
    private lateinit var cancel : TextView
    private lateinit var searchResultLayout : LinearLayout
    private lateinit var tvSearchResult : TextView
    private lateinit var searchResultListLayout: ConstraintLayout
    private lateinit var tvAllDishes : TextView
    private lateinit var allDishesRecyclerview : RecyclerView
    private lateinit var searchType : String
    private lateinit var appPreference: AppPreference
    private val dishesViewModel: SearchIngredientsViewModel by activityViewModels()
    private var ingredientLocalListModel : IngredientLocalListModel? = null
    private lateinit var backButton : ImageView
    private var searchIngredientList : ArrayList<IngredientRecipeList> = ArrayList()
    private var recipeId : String = ""
    private var ingredientName : String = ""
    private var recipeName : String = ""
    private var serving : Double = 0.0
    private var loadingOverlay : FrameLayout? = null
    private var moduleName : String = ""
    private var selectedMealDate : String = ""
    private lateinit var mealType : String

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSearchDishBinding
        get() = FragmentSearchDishBinding::inflate

    private val snapSearchDishAdapter by lazy { IngredientSearchAdapter(requireContext(), arrayListOf(), -1, null,
        false, :: onSearchIngredientItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        appPreference = AppPreference(requireContext())

        searchLayout = view.findViewById(R.id.layout_search)
        searchEditText = view.findViewById(R.id.et_search)
        cancel = view.findViewById(R.id.tv_cancel)
        searchResultLayout = view.findViewById(R.id.layout_search_result)
        tvSearchResult = view.findViewById(R.id.tv_search_result)
        searchResultListLayout = view.findViewById(R.id.layout_search_resultList)
        tvAllDishes = view.findViewById(R.id.tv_all_dishes)
        allDishesRecyclerview = view.findViewById(R.id.recyclerView_all_dishes)
        backButton = view.findViewById(R.id.backButton)

        moduleName = arguments?.getString("ModuleName").toString()
        searchType = arguments?.getString("searchType").toString()
        recipeId = arguments?.getString("recipeId").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()
        serving = arguments?.getDouble("serving")?.toDouble() ?: 0.0
        ingredientName = arguments?.getString("ingredientName").toString()
        recipeName = arguments?.getString("recipeName").toString()
        mealType = arguments?.getString("mealType").toString()

        if (searchType.contentEquals("mealScanResult")){
            allDishesRecyclerview.layoutManager = LinearLayoutManager(context)
            allDishesRecyclerview.adapter = snapSearchDishAdapter
        }else{
            allDishesRecyclerview.layoutManager = LinearLayoutManager(context)
            allDishesRecyclerview.adapter = snapSearchDishAdapter
        }

        val ingredientLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("ingredientLocalListModel", IngredientLocalListModel::class.java)
        } else {
            arguments?.getParcelable("ingredientLocalListModel")
        }

        if (ingredientLocalListModels != null){
            ingredientLocalListModel = ingredientLocalListModels
        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
             //       if (searchType.contentEquals("createRecipe")){
                        val fragment = CreateRecipeFragment()
                        val args = Bundle()
                        args.putString("ModuleName", moduleName)
                        args.putString("selectedMealDate", selectedMealDate)
                        args.putString("mealType", mealType)
                        args.putString("recipeId", recipeId)
                        args.putString("recipeName", recipeName)
                        args.putDouble("serving", serving)
                        args.putString("ingredientName", ingredientName)
                        fragment.arguments = args
                        requireActivity().supportFragmentManager.beginTransaction().apply {
                            replace(R.id.flFragment, fragment, "landing")
                            addToBackStack("landing")
                            commit()
                        }
       //             }
                }
            })

        backButton.setOnClickListener {
        //    if (searchType.contentEquals("createRecipe")){
                val fragment = CreateRecipeFragment()
                val args = Bundle()
                args.putString("ModuleName", moduleName)
                args.putString("selectedMealDate", selectedMealDate)
                args.putString("mealType", mealType)
                args.putString("recipeId", recipeId)
                args.putString("recipeName", recipeName)
                args.putDouble("serving", serving)
                args.putString("ingredientName", ingredientName)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
      //      }
        }

        dishesViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            filterDishes(query)
        }

        cancel.setOnClickListener {
            if (searchEditText.text.toString().isNotEmpty()){
                dishesViewModel.setSearchQuery("")
                searchEditText.setText("")
              //  searchIngredientList.clear()
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dishesViewModel.setSearchQuery(s.toString())

                if (s!!.length > 1) {
                    searchResultLayout.visibility = View.VISIBLE
                    tvSearchResult.visibility = View.VISIBLE
                    cancel.visibility = View.VISIBLE
                  //  tvSearchResult.text = "Search Result: ${filteredList.size}"
                    getRecipesList(s.toString())
                }else if (s!!.length == 0){
                    searchResultLayout.visibility = View.VISIBLE
                    tvSearchResult.visibility = View.GONE
                    cancel.visibility = View.GONE
                    searchIngredientList.clear()
                    onSnapSearchDishItemRefresh()
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

//        if (searchType.contentEquals("mealScanResult")){
//            getSnapMealRecipesList()
//            onSnapSearchDishItemRefresh()
//        }else{
//            getSnapMealRecipesList()
//            onSnapSearchDishItemRefresh()
//        }
    }

    private fun onSnapSearchDishItemRefresh() {

        val valueLists : ArrayList<IngredientRecipeList> = ArrayList()
        valueLists.addAll(searchIngredientList as Collection<IngredientRecipeList>)
        val mealLogDateData: IngredientRecipeList? = null
        snapSearchDishAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onSearchIngredientItem(recipesModel: IngredientRecipeList, position: Int, isRefresh: Boolean) {

        getIngredientDetails(recipesModel.id)
    }

    private fun filterDishes(query: String) {
        val filteredList = if (query.isEmpty()) searchIngredientList
        else searchIngredientList.filter { it.recipe.contains(query, ignoreCase = true) }
        snapSearchDishAdapter.updateList(filteredList)
        if (query.isNotEmpty()) {
            searchResultLayout.visibility = View.VISIBLE
            tvSearchResult.visibility = View.VISIBLE
            cancel.visibility = View.VISIBLE
            tvSearchResult.text = "Search Result: ${filteredList.size}"
        } else {
            searchResultLayout.visibility = View.VISIBLE
            tvSearchResult.visibility = View.GONE
            cancel.visibility = View.GONE
        }
    }

    private fun getRecipesList(keyword : String) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val call = ApiClient.apiServiceFastApiV2.getSearchIngredientList(keyword)
        call.enqueue(object : Callback<IngredientResponse> {
            override fun onResponse(call: Call<IngredientResponse>, response: Response<IngredientResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val searchData = response.body()?.data
                    if (searchData != null){
                        if (searchData.size > 0){
                            //snapRecipesList.addAll(mealPlanLists)
                            searchIngredientList.clear()
                            tvSearchResult.text = "Search Result: ${searchData.size}"
                            searchIngredientList.addAll(searchData)
                            onSnapSearchDishItemRefresh()
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
            override fun onFailure(call: Call<IngredientResponse>, t: Throwable) {
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

    private fun getIngredientDetails(ingredientId : String) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val call = ApiClient.apiServiceFastApiV2.getIngredientDetails(ingredientId)
        call.enqueue(object : Callback<IngredientDetailResponse> {
            override fun onResponse(call: Call<IngredientDetailResponse>, response: Response<IngredientDetailResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    if (response.body()?.data != null){
                        requireActivity().supportFragmentManager.beginTransaction().apply {
                            val snapMealFragment = IngredientDishFragment()
                            val args = Bundle()
                            args.putString("ModuleName", moduleName)
                            args.putString("selectedMealDate", selectedMealDate)
                            args.putString("mealType", mealType)
                            args.putString("searchType", "searchIngredient")
                            args.putString("recipeId", recipeId)
                            args.putDouble("serving", serving)
                            args.putString("ingredientName", ingredientName)
                            args.putString("recipeName", recipeName)
                            args.putParcelable("ingredientDetailResponse", response.body())
                            args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
                            snapMealFragment.arguments = args
                            replace(R.id.flFragment, snapMealFragment, "Steps")
                            addToBackStack(null)
                            commit()
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

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }
}