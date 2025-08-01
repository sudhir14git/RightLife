package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.content.Context
import android.graphics.Rect
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
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
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.createmeal.SearchDishesAdapter
import com.jetsynthesys.rightlife.ai_package.model.RecipeList
import com.jetsynthesys.rightlife.ai_package.model.response.RecipeResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SearchResultItem
import com.jetsynthesys.rightlife.ai_package.model.response.SearchResultsResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealRecipeResponseModel
import com.jetsynthesys.rightlife.ai_package.model.response.SnapRecipeData
import com.jetsynthesys.rightlife.ai_package.model.response.SnapRecipeList
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.SnapSearchDishesAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.HomeTabMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.DishLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MealLogItems
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SelectedMealLogList
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SnapDishLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SnapMealRequestLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.viewmodel.DishesViewModel
import com.jetsynthesys.rightlife.ai_package.utils.AppPreference
import com.jetsynthesys.rightlife.databinding.FragmentSearchDishBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SearchDishToLogFragment : BaseFragment<FragmentSearchDishBinding>() {

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
    private var mealLogRequests : SelectedMealLogList? = null
    private lateinit var backButton : ImageView
    private lateinit var currentPhotoPathsecound : Uri
    private lateinit var mealType : String
    private var snapMealLogRequests : SelectedMealLogList? = null
    private var searchMealList : ArrayList<SearchResultItem> = ArrayList()
    private var snapMealRequestLocalListModel : SnapMealRequestLocalListModel? = null
    private var loadingOverlay : FrameLayout? = null
    private var moduleName : String = ""
    private var selectedMealDate : String = ""

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

        moduleName = arguments?.getString("ModuleName").toString()
        searchType = arguments?.getString("searchType").toString()
        mealType = arguments?.getString("mealType").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()

        val imagePathString = arguments?.getString("ImagePathsecound")
        if (imagePathString != null){
            currentPhotoPathsecound = imagePathString?.let { Uri.parse(it) }!!
        }

        if (searchType.contentEquals("HomeTabMeal")){
            allDishesRecyclerview.layoutManager = LinearLayoutManager(context)
            allDishesRecyclerview.adapter = snapSearchDishAdapter
        }else{
            allDishesRecyclerview.layoutManager = LinearLayoutManager(context)
            allDishesRecyclerview.adapter = snapSearchDishAdapter
        }

        val snapDishLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", SnapDishLocalListModel::class.java)
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

        if (snapDishLocalListModels != null){
            snapDishLocalListModel = snapDishLocalListModels
        }

        if (selectedMealLogListModels != null){
            mealLogRequests = selectedMealLogListModels
        }

        if (selectedSnapMealLogListModels != null){
            snapMealLogRequests = selectedSnapMealLogListModels
        }

        searchEditText.post {
            searchEditText.requestFocus()
            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT)
        }


        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val fragment = YourMealLogsFragment()
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
            })

        backButton.setOnClickListener {
            val fragment = YourMealLogsFragment()
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

//        dishesViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
//            filterDishes(query)
//        }

        cancel.setOnClickListener {
            if (searchEditText.text.toString().isNotEmpty()){
                dishesViewModel.setSearchQuery("")
                searchEditText.setText("")
                searchMealList.clear()
            }
            val fragment = HomeTabMealFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealType", mealType)
            args.putString("selectedMealDate", selectedMealDate)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dishesViewModel.setSearchQuery(s.toString())
                if (s!!.length > 1){
                    searchResultLayout.visibility = View.VISIBLE
                    tvSearchResult.visibility = View.VISIBLE
                    cancel.visibility = View.VISIBLE
                    getSearchMealList(s.toString())
                }else if (s!!.length == 0){
                        requireActivity().runOnUiThread {
                            searchResultLayout.visibility = View.VISIBLE
                            tvSearchResult.visibility = View.GONE
                            cancel.visibility = View.GONE
                            searchMealList.clear()
                            onSnapSearchDishItemRefresh()

                            view.listenKeyboardVisibility { isVisible ->
                                if (isVisible) {
                                    Log.d("Keyboard", "Opened")
                                } else {
                                    Log.d("Keyboard", "Closed")
                                    val fragment = HomeTabMealFragment()
                                    val args = Bundle()
                                    args.putString("ModuleName", moduleName)
                                    args.putString("mealType", mealType)
                                    args.putString("selectedMealDate", selectedMealDate)
                                    fragment.arguments = args
                                    requireActivity().supportFragmentManager.beginTransaction().apply {
                                        replace(R.id.flFragment, fragment, "mealLog")
                                        addToBackStack("mealLog")
                                        commit()
                                    }
                                }
                            }
                        }
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })

//        if (searchType.contentEquals("HomeTabMeal")){
//            getSnapMealRecipesList()
//            onSnapSearchDishItemRefresh()
//        }else{
//            getSnapMealRecipesList()
//            onSnapSearchDishItemRefresh()
//        }
    }

    fun View.listenKeyboardVisibility(onChanged: (Boolean) -> Unit) {
        var isKeyboardVisibleOld = false

        viewTreeObserver.addOnGlobalLayoutListener {
            val r = Rect()
            getWindowVisibleDisplayFrame(r)
            val screenHeight = rootView.height
            val keypadHeight = screenHeight - r.bottom
            val isVisible = keypadHeight > screenHeight * 0.15

            if (isVisible != isKeyboardVisibleOld) {
                onChanged(isVisible)
                isKeyboardVisibleOld = isVisible
            }
        }
    }


    private fun onSnapSearchDishItemRefresh() {

        val valueLists : ArrayList<SearchResultItem> = ArrayList()
        valueLists.addAll(searchMealList as Collection<SearchResultItem>)
        val mealLogDateData: SearchResultItem? = null
        snapSearchDishAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onSearchDishItem(recipesModel: SearchResultItem, position: Int, isRefresh: Boolean) {

//        val valueLists : ArrayList<SearchResultItem> = ArrayList()
//        valueLists.addAll(recipesList as Collection<SearchResultItem>)
//        searchDishAdapter.addAll(valueLists, position, recipesModel, isRefresh)
       // getSnapMealRecipesDetails(recipesModel._id)
    }

    private fun onSnapSearchDishItem(searchResultItem: SearchResultItem, position: Int, isRefresh: Boolean) {

//        val valueLists : ArrayList<SearchResultItem> = ArrayList()
//        valueLists.addAll(recipesList as Collection<SearchResultItem>)
//        snapSearchDishAdapter.addAll(valueLists, position, searchResultItem, isRefresh)
      //  getSnapMealRecipesDetails(recipesModel.id)

        requireActivity().supportFragmentManager.beginTransaction().apply {
            val snapMealFragment = DishToLogFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("searchType", searchType)
            args.putString("mealType", mealType)
            args.putString("selectedMealDate", selectedMealDate)
            args.putParcelable("searchResultItem", searchResultItem)
            args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
            args.putParcelable("selectedMealLogList", mealLogRequests)
            args.putParcelable("selectedSnapMealLogList", snapMealLogRequests)
            args.putParcelable("snapMealRequestLocalListModel", snapMealRequestLocalListModel)
            snapMealFragment.arguments = args
            replace(R.id.flFragment, snapMealFragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun filterDishes(query: String) {

        if (searchType.contentEquals("HomeTabMeal")){
            val filteredList = if (query.isEmpty()) searchMealList
            else searchMealList.filter { it.name.contains(query, ignoreCase = true) }
            snapSearchDishAdapter.updateList(filteredList)
            if (query.isNotEmpty()) {
                searchResultLayout.visibility = View.VISIBLE
                tvSearchResult.visibility = View.VISIBLE
                cancel.visibility = View.VISIBLE
              //  tvSearchResult.text = "Search Result: ${filteredList.size}"
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
              //  tvSearchResult.text = "Search Result: ${filteredList.size}"
            } else {
                searchResultLayout.visibility = View.VISIBLE
                tvSearchResult.visibility = View.GONE
                cancel.visibility = View.GONE
            }
        }
    }

    private fun getSearchMealList(keyword : String) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val call = ApiClient.apiServiceFastApi.getSearchMealList(keyword)
        call.enqueue(object : Callback<SearchResultsResponse> {
            override fun onResponse(call: Call<SearchResultsResponse>, response: Response<SearchResultsResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val searchData = response.body()?.data
                    if (searchData != null){
                        if (searchData.results.size > 0){
                            //snapRecipesList.addAll(mealPlanLists)
                            requireActivity().runOnUiThread {
                                searchMealList.clear()
                                tvSearchResult.text = "Search Result:"
                                searchMealList.addAll(searchData.results)
                                tvSearchResult.text = "Search Result: " + searchData.total_found.toString()
                                onSnapSearchDishItemRefresh()
                            }
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
            override fun onFailure(call: Call<SearchResultsResponse>, t: Throwable) {
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