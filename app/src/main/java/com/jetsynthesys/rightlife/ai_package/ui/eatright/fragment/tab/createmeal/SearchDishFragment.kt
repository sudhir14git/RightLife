package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal

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
import com.jetsynthesys.rightlife.ai_package.model.FoodDetailsResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.createmeal.SearchDishesAdapter
import com.jetsynthesys.rightlife.ai_package.model.RecipeList
import com.jetsynthesys.rightlife.ai_package.model.RecipeResponseModel
import com.jetsynthesys.rightlife.ai_package.model.response.RecipeResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SearchResultItem
import com.jetsynthesys.rightlife.ai_package.model.response.SearchResultsResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealRecipeResponseModel
import com.jetsynthesys.rightlife.ai_package.model.response.SnapRecipeList
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.SnapSearchDishesAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.MealScanResultFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.SnapDishFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.DishLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SnapDishLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.viewmodel.DishesViewModel
import com.jetsynthesys.rightlife.ai_package.utils.AppPreference
import com.jetsynthesys.rightlife.ai_package.utils.LoaderUtil
import com.jetsynthesys.rightlife.databinding.FragmentSearchDishBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchDishFragment : BaseFragment<FragmentSearchDishBinding>() {

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
    private var recipesList : ArrayList<RecipeList> = ArrayList()
    private var snapRecipesList : ArrayList<SnapRecipeList> = ArrayList()
    private var dishLocalListModel : DishLocalListModel? = null
    private var snapDishLocalListModel : SnapDishLocalListModel? = null
    private lateinit var backButton : ImageView
    private lateinit var currentPhotoPathsecound : Uri
    private var searchMealList : ArrayList<SearchResultItem> = ArrayList()
    private var mealId : String = ""
    private var mealName : String = ""

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSearchDishBinding
        get() = FragmentSearchDishBinding::inflate

    private val searchDishAdapter by lazy { SearchDishesAdapter(requireContext(), arrayListOf(), -1, null,
        false, :: onSearchDishItem) }

    private val snapSearchDishAdapter by lazy { SnapSearchDishesAdapter(requireContext(), arrayListOf(), -1, null,
        false, :: onSnapSearchDishItem) }

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
        mealId = arguments?.getString("mealId").toString()
        mealName = arguments?.getString("mealName").toString()

        val imagePathString = arguments?.getString("ImagePathsecound")
        if (imagePathString != null){
            currentPhotoPathsecound = imagePathString?.let { Uri.parse(it) }!!
        }

        if (searchType.contentEquals("mealScanResult")){
            allDishesRecyclerview.layoutManager = LinearLayoutManager(context)
            allDishesRecyclerview.adapter = snapSearchDishAdapter
        }else{
            allDishesRecyclerview.layoutManager = LinearLayoutManager(context)
            allDishesRecyclerview.adapter = snapSearchDishAdapter
        }

//        val dishLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
//            arguments?.getParcelable("dishLocalListModel", DishLocalListModel::class.java)
//        } else {
//            arguments?.getParcelable("dishLocalListModel")
//        }

        val snapDishLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", SnapDishLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapDishLocalListModel")
        }

        if (snapDishLocalListModels != null){
            snapDishLocalListModel = snapDishLocalListModels
        }

//        if (dishLocalListModels != null){
//            dishLocalListModel = dishLocalListModels
//        }

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    if (searchType.contentEquals("mealScanResult")){
                        val fragment = MealScanResultFragment()
                        val args = Bundle()
                        args.putString("mealId", mealId)
                        args.putString("mealName", mealName)
                        args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
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
                args.putString("mealId", mealId)
                args.putString("mealName", mealName)
                args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
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
                if (s!!.length > 1){
                    getSnapMealRecipesList(s.toString())
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

    private fun onSearchDishItemRefresh() {

        val valueLists : ArrayList<SearchResultItem> = ArrayList()
        valueLists.addAll(searchMealList as Collection<SearchResultItem>)
        val mealLogDateData: SearchResultItem? = null
        searchDishAdapter.addAll(valueLists, -1, mealLogDateData, false)

//        dishesViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
//            filterDishes(query)
//        }
    }

    private fun onSnapSearchDishItemRefresh() {

        val valueLists : ArrayList<SearchResultItem> = ArrayList()
        valueLists.addAll(searchMealList as Collection<SearchResultItem>)
        val mealLogDateData: SearchResultItem? = null
        snapSearchDishAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onSearchDishItem(recipesModel: SearchResultItem, position: Int, isRefresh: Boolean) {

//        val valueLists : ArrayList<SearchResultItem> = ArrayList()
//        valueLists.addAll(searchMealList as Collection<SearchResultItem>)
//        searchDishAdapter.addAll(valueLists, position, recipesModel, isRefresh)

        //getSnapMealRecipesDetails(recipesModel._id)
        if (searchType.contentEquals("mealScanResult")){
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val snapMealFragment = SnapDishFragment()
                val args = Bundle()
                args.putString("mealId", mealId)
                args.putString("mealName", mealName)
                args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
                args.putString("searchType", "searchScanResult")
                args.putParcelable("searchResultItem", recipesModel)
                args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
                snapMealFragment.arguments = args
                replace(R.id.flFragment, snapMealFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }else{
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val snapMealFragment = DishFragment()
                val args = Bundle()
                args.putString("searchType", searchType)
                args.putString("mealId", mealId)
                args.putParcelable("searchResultItem", recipesModel)
                args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
                snapMealFragment.arguments = args
                replace(R.id.flFragment, snapMealFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }
    }

    private fun onSnapSearchDishItem(recipesModel: SearchResultItem, position: Int, isRefresh: Boolean) {

//        val valueLists : ArrayList<SearchResultItem> = ArrayList()
//        valueLists.addAll(searchMealList as Collection<SearchResultItem>)
//        snapSearchDishAdapter.addAll(valueLists, position, recipesModel, isRefresh)
       // getSnapMealRecipesDetails(recipesModel.id)
        if (searchType.contentEquals("mealScanResult")){
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val snapMealFragment = SnapDishFragment()
                val args = Bundle()
                args.putString("mealId", mealId)
                args.putString("mealName", mealName)
                args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
                args.putString("searchType", "SearchDish")
                args.putParcelable("searchResultItem", recipesModel)
                args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
                snapMealFragment.arguments = args
                replace(R.id.flFragment, snapMealFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }else{
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val snapMealFragment = DishFragment()
                val args = Bundle()
                args.putString("searchType", "SearchDish")
                args.putString("mealId", mealId)
                args.putParcelable("searchResultItem", recipesModel)
                args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
                snapMealFragment.arguments = args
                replace(R.id.flFragment, snapMealFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }
    }

    private fun filterDishes(query: String) {

        if (searchType.contentEquals("mealScanResult")){
            val filteredList = if (query.isEmpty()) searchMealList
            else searchMealList.filter { it.name.contains(query, ignoreCase = true) }
            snapSearchDishAdapter.updateList(filteredList)
            if (query.isNotEmpty()) {
                searchResultLayout.visibility = View.VISIBLE
                tvSearchResult.visibility = View.VISIBLE
                cancel.visibility = View.VISIBLE
                //tvSearchResult.text = "Search Result: ${filteredList.size}"
            } else {
                searchResultLayout.visibility = View.VISIBLE
                tvSearchResult.visibility = View.GONE
                cancel.visibility = View.GONE
            }
        }else{
            val filteredList = if (query.isEmpty()) searchMealList
            else searchMealList.filter { it.name.contains(query, ignoreCase = true) }
            snapSearchDishAdapter.updateList(filteredList)
            if (query.isNotEmpty()) {
                searchResultLayout.visibility = View.VISIBLE
                tvSearchResult.visibility = View.VISIBLE
                cancel.visibility = View.VISIBLE
                //tvSearchResult.text = "Search Result: ${filteredList.size}"
            } else {
                searchResultLayout.visibility = View.VISIBLE
                tvSearchResult.visibility = View.GONE
                cancel.visibility = View.GONE
            }
//             val filteredList = if (query.isEmpty()) recipesList
//            else recipesList.filter { it.name.contains(query, ignoreCase = true) }
//            searchDishAdapter.updateList(filteredList)
//            if (query.isNotEmpty()) {
//                searchResultLayout.visibility = View.VISIBLE
//                tvSearchResult.visibility = View.VISIBLE
//                cancel.visibility = View.VISIBLE
//                tvSearchResult.text = "Search Result: ${filteredList.size}"
//            } else {
//                searchResultLayout.visibility = View.VISIBLE
//                tvSearchResult.visibility = View.GONE
//                cancel.visibility = View.GONE
//            }
        }
    }

    private fun getMealRecipesList() {
        LoaderUtil.showLoader(requireActivity())
        val userId = appPreference.getUserId().toString()
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdhNWZhZTkxOTc5OTI1MTFlNzFiMWM4Iiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiJBZGl0eWEiLCJsYXN0TmFtZSI6IlR5YWdpIiwiZGV2aWNlSWQiOiJCNkRCMTJBMy04Qjc3LTRDQzEtOEU1NC0yMTVGQ0U0RDY5QjQiLCJtYXhEZXZpY2VSZWFjaGVkIjpmYWxzZSwidHlwZSI6ImFjY2Vzcy10b2tlbiJ9LCJpYXQiOjE3MzkxNzE2NjgsImV4cCI6MTc1NDg5NjQ2OH0.koJ5V-vpGSY1Irg3sUurARHBa3fArZ5Ak66SkQzkrxM"
        val call = ApiClient.apiService.getMealRecipesList(token)
        call.enqueue(object : Callback<RecipeResponseModel> {
            override fun onResponse(call: Call<RecipeResponseModel>, response: Response<RecipeResponseModel>) {
                if (response.isSuccessful) {
                    LoaderUtil.dismissLoader(requireActivity())
                    val mealPlanLists = response.body()?.data ?: emptyList()
                    //recipesList.addAll(mealPlanLists)
                    onSearchDishItemRefresh()
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    LoaderUtil.dismissLoader(requireActivity())
                }
            }
            override fun onFailure(call: Call<RecipeResponseModel>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                LoaderUtil.dismissLoader(requireActivity())
            }
        })
    }

    private fun getMealRecipesDetails(foodId : String) {
        LoaderUtil.showLoader(requireActivity())
        val userId = appPreference.getUserId().toString()
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdhNWZhZTkxOTc5OTI1MTFlNzFiMWM4Iiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiJBZGl0eWEiLCJsYXN0TmFtZSI6IlR5YWdpIiwiZGV2aWNlSWQiOiJCNkRCMTJBMy04Qjc3LTRDQzEtOEU1NC0yMTVGQ0U0RDY5QjQiLCJtYXhEZXZpY2VSZWFjaGVkIjpmYWxzZSwidHlwZSI6ImFjY2Vzcy10b2tlbiJ9LCJpYXQiOjE3MzkxNzE2NjgsImV4cCI6MTc1NDg5NjQ2OH0.koJ5V-vpGSY1Irg3sUurARHBa3fArZ5Ak66SkQzkrxM"
        val call = ApiClient.apiService.getMealRecipesDetails(foodId, token)
        call.enqueue(object : Callback<FoodDetailsResponse> {
            override fun onResponse(call: Call<FoodDetailsResponse>, response: Response<FoodDetailsResponse>) {
                if (response.isSuccessful) {
                    LoaderUtil.dismissLoader(requireActivity())
                    if (response.body()?.data != null){
                        requireActivity().supportFragmentManager.beginTransaction().apply {
                           val snapMealFragment = DishFragment()
                           val args = Bundle()
                           args.putString("searchType", searchType)
                           args.putParcelable("foodDetailsResponse", response.body())
                            args.putParcelable("dishLocalListModel", dishLocalListModel)
                           snapMealFragment.arguments = args
                           replace(R.id.flFragment, snapMealFragment, "Steps")
                           addToBackStack(null)
                           commit()
                       }
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    LoaderUtil.dismissLoader(requireActivity())
                }
            }
            override fun onFailure(call: Call<FoodDetailsResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                LoaderUtil.dismissLoader(requireActivity())
            }
        })
    }

    private fun getSnapMealRecipesList(keyword : String) {
     //   LoaderUtil.showLoader(requireActivity())
        val userId = appPreference.getUserId().toString()
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdhNWZhZTkxOTc5OTI1MTFlNzFiMWM4Iiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiJBZGl0eWEiLCJsYXN0TmFtZSI6IlR5YWdpIiwiZGV2aWNlSWQiOiJCNkRCMTJBMy04Qjc3LTRDQzEtOEU1NC0yMTVGQ0U0RDY5QjQiLCJtYXhEZXZpY2VSZWFjaGVkIjpmYWxzZSwidHlwZSI6ImFjY2Vzcy10b2tlbiJ9LCJpYXQiOjE3MzkxNzE2NjgsImV4cCI6MTc1NDg5NjQ2OH0.koJ5V-vpGSY1Irg3sUurARHBa3fArZ5Ak66SkQzkrxM"
        val call = ApiClient.apiServiceFastApi.getSearchMealList(keyword)
        call.enqueue(object : Callback<SearchResultsResponse> {
            override fun onResponse(call: Call<SearchResultsResponse>, response: Response<SearchResultsResponse>) {
                if (response.isSuccessful) {
                  //  LoaderUtil.dismissLoader(requireActivity())
                    val searchData = response.body()?.data
                    if (searchData != null){
                        if (searchData.results.size > 0){
                            //snapRecipesList.addAll(mealPlanLists)
                            tvSearchResult.text = "Search Result: ${searchData.total_found}"
                            searchMealList.addAll(searchData.results)
                            onSnapSearchDishItemRefresh()
                        }
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                 //   LoaderUtil.dismissLoader(requireActivity())
                }
            }
            override fun onFailure(call: Call<SearchResultsResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
             //   LoaderUtil.dismissLoader(requireActivity())
            }
        })
    }

    private fun getSnapMealRecipesDetails(foodId : String) {
     //   LoaderUtil.showLoader(requireActivity())
        val userId = appPreference.getUserId().toString()
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdhNWZhZTkxOTc5OTI1MTFlNzFiMWM4Iiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiJBZGl0eWEiLCJsYXN0TmFtZSI6IlR5YWdpIiwiZGV2aWNlSWQiOiJCNkRCMTJBMy04Qjc3LTRDQzEtOEU1NC0yMTVGQ0U0RDY5QjQiLCJtYXhEZXZpY2VSZWFjaGVkIjpmYWxzZSwidHlwZSI6ImFjY2Vzcy10b2tlbiJ9LCJpYXQiOjE3MzkxNzE2NjgsImV4cCI6MTc1NDg5NjQ2OH0.koJ5V-vpGSY1Irg3sUurARHBa3fArZ5Ak66SkQzkrxM"
        val call = ApiClient.apiServiceFastApi.getSnapMealRecipesDetails(foodId)
        call.enqueue(object : Callback<RecipeResponse> {
            override fun onResponse(call: Call<RecipeResponse>, response: Response<RecipeResponse>) {
                if (response.isSuccessful) {
          //          LoaderUtil.dismissLoader(requireActivity())
                    if (response.body()?.data != null){
                        if (searchType.contentEquals("mealScanResult")){
                            requireActivity().supportFragmentManager.beginTransaction().apply {
                                val snapMealFragment = SnapDishFragment()
                                val args = Bundle()
                                args.putString("ImagePathsecound", currentPhotoPathsecound.toString())
                                args.putString("searchType", "searchScanResult")
                                args.putParcelable("recipeResponse", response.body())
                                args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
                                snapMealFragment.arguments = args
                                replace(R.id.flFragment, snapMealFragment, "Steps")
                                addToBackStack(null)
                                commit()
                            }
                        }else{
                            requireActivity().supportFragmentManager.beginTransaction().apply {
                                val snapMealFragment = DishFragment()
                                val args = Bundle()
                                args.putString("searchType", searchType)
                                args.putParcelable("recipeResponse", response.body())
                                args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
                                snapMealFragment.arguments = args
                                replace(R.id.flFragment, snapMealFragment, "Steps")
                                addToBackStack(null)
                                commit()
                            }
                        }
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
              //      LoaderUtil.dismissLoader(requireActivity())
                }
            }
            override fun onFailure(call: Call<RecipeResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
          //      LoaderUtil.dismissLoader(requireActivity())
            }
        })
    }
}