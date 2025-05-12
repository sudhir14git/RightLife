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
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientLists
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.IngredientSearchAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.MealScanResultFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.IngredientLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.viewmodel.DishesViewModel
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
    private val dishesViewModel: DishesViewModel by activityViewModels()
    private var ingredientLocalListModel : IngredientLocalListModel? = null
    private lateinit var backButton : ImageView
    private var searchIngredientList : ArrayList<IngredientLists> = ArrayList()
    private var recipeId : String = ""
    private var ingredientName : String = ""
    private var recipeName : String = ""

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

        searchType = arguments?.getString("searchType").toString()
        recipeId = arguments?.getString("recipeId").toString()
        ingredientName = arguments?.getString("ingredientName").toString()
        recipeName = arguments?.getString("recipeName").toString()

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
                    if (searchType.contentEquals("mealScanResult")){
                        val fragment = MealScanResultFragment()
                        val args = Bundle()
                        args.putString("recipeId", recipeId)
                        args.putString("recipeName", recipeName)
                        args.putString("ingredientName", ingredientName)
                        fragment.arguments = args
                        requireActivity().supportFragmentManager.beginTransaction().apply {
                            replace(R.id.flFragment, fragment, "landing")
                            addToBackStack("landing")
                            commit()
                        }
                    }else{
                        val fragment = YourMealLogsFragment()
                        val args = Bundle()
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
            if (searchType.contentEquals("mealScanResult")){
                val fragment = MealScanResultFragment()
                val args = Bundle()
                args.putString("recipeId", recipeId)
                args.putString("recipeName", recipeName)
                args.putString("ingredientName", ingredientName)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }else{
                val fragment = YourMealLogsFragment()
                val args = Bundle()
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        }

        dishesViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            filterDishes(query)
        }

        getRecipesList("100")

        cancel.setOnClickListener {
            if (searchEditText.text.toString().isNotEmpty()){
                dishesViewModel.setSearchQuery("")
                searchEditText.setText("")
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dishesViewModel.setSearchQuery(s.toString())
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

        val valueLists : ArrayList<IngredientLists> = ArrayList()
        valueLists.addAll(searchIngredientList as Collection<IngredientLists>)
        val mealLogDateData: IngredientLists? = null
        snapSearchDishAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onSearchIngredientItem(recipesModel: IngredientLists, position: Int, isRefresh: Boolean) {

        getRecipesDetails(recipesModel.id)
//        if (searchType.contentEquals("mealScanResult")){
//            requireActivity().supportFragmentManager.beginTransaction().apply {
//                val snapMealFragment = SnapDishFragment()
//                val args = Bundle()
//                args.putString("mealId", mealId)
//                args.putString("mealName", mealName)
//                args.putString("searchType", "SearchDish")
//                args.putParcelable("searchResultItem", recipesModel)
//                args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
//                snapMealFragment.arguments = args
//                replace(R.id.flFragment, snapMealFragment, "Steps")
//                addToBackStack(null)
//                commit()
//            }
//        }else{
//            requireActivity().supportFragmentManager.beginTransaction().apply {
//                val snapMealFragment = IngredientDishFragment()
//                val args = Bundle()
//                args.putString("searchType", "SearchDish")
//                args.putString("mealId", mealId)
//                args.putParcelable("searchResultItem", recipesModel)
//                args.putParcelable("ingredientLocalListModel", ingredientLocalListModel)
//                snapMealFragment.arguments = args
//                replace(R.id.flFragment, snapMealFragment, "Steps")
//                addToBackStack(null)
//                commit()
//            }
//        }
    }

    private fun filterDishes(query: String) {
        val filteredList = if (query.isEmpty()) searchIngredientList
        else searchIngredientList.filter { it.ingredient_name.contains(query, ignoreCase = true) }
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

    private fun getRecipesList(limit : String) {
     //   LoaderUtil.showLoader(requireActivity())
        val call = ApiClient.apiServiceFastApi.getSearchIngredientList(limit)
        call.enqueue(object : Callback<IngredientResponse> {
            override fun onResponse(call: Call<IngredientResponse>, response: Response<IngredientResponse>) {
                if (response.isSuccessful) {
                  //  LoaderUtil.dismissLoader(requireActivity())
                    val searchData = response.body()?.data
                    if (searchData != null){
                        if (searchData.size > 0){
                            //snapRecipesList.addAll(mealPlanLists)
                            tvSearchResult.text = "Search Result: ${searchData.size}"
                            searchIngredientList.addAll(searchData)
                            onSnapSearchDishItemRefresh()
                        }
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                 //   LoaderUtil.dismissLoader(requireActivity())
                }
            }
            override fun onFailure(call: Call<IngredientResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
             //   LoaderUtil.dismissLoader(requireActivity())
            }
        })
    }

    private fun getRecipesDetails(ingredientId : String) {
     //   LoaderUtil.showLoader(requireActivity())
        val call = ApiClient.apiServiceFastApi.getRecipesDetails(ingredientId)
        call.enqueue(object : Callback<IngredientDetailResponse> {
            override fun onResponse(call: Call<IngredientDetailResponse>, response: Response<IngredientDetailResponse>) {
                if (response.isSuccessful) {
          //          LoaderUtil.dismissLoader(requireActivity())
                    if (response.body()?.data != null){
                        requireActivity().supportFragmentManager.beginTransaction().apply {
                            val snapMealFragment = IngredientDishFragment()
                            val args = Bundle()
                            args.putString("searchType", "searchIngredient")
                            args.putString("recipeId", recipeId)
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
              //      LoaderUtil.dismissLoader(requireActivity())
                }
            }
            override fun onFailure(call: Call<IngredientDetailResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
          //      LoaderUtil.dismissLoader(requireActivity())
            }
        })
    }
}