package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.content.Context
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.InputType
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.TabContentAdapter
import com.jetsynthesys.rightlife.ai_package.model.response.RecipeListResponse
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeList
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.RecipeSearchAdapter
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.utils.AppPreference
import com.jetsynthesys.rightlife.databinding.FragmentRecipeSearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import androidx.core.view.isVisible
import com.jetsynthesys.rightlife.ai_package.ui.eatright.viewmodel.RecipesSearchViewModel
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger

class RecipesSearchFragment : BaseFragment<FragmentRecipeSearchBinding>() {

    private lateinit var searchLayout: LinearLayoutCompat
    private lateinit var searchEditText: EditText
    private lateinit var cancel: TextView
    private lateinit var searchResultLayout: LinearLayout
    private lateinit var tabLayout: TabLayout
    private lateinit var searchResultListLayout: ConstraintLayout
    private lateinit var tvAllDishes: TextView
    private lateinit var allDishesRecyclerview: RecyclerView
    private lateinit var tabContentRecyclerView: RecyclerView
    private lateinit var spinner: Spinner
    private lateinit var searchType: String
    private lateinit var appPreference: AppPreference
    private lateinit var tabContentCard: CardView
    private val recipesSearchViewModel: RecipesSearchViewModel by activityViewModels()
    private var snapRecipesList: ArrayList<IngredientRecipeList> = ArrayList()
    private var mealTypeList: ArrayList<String> = ArrayList()
    private var foodTypeList: ArrayList<String> = ArrayList()
    private var cuisineList: ArrayList<String> = ArrayList()
    private lateinit var backButton: ImageView
    private lateinit var tabSelectedTitle : TextView
    private lateinit var mealType: String
    private val tabTitles = arrayOf("Meal Type", "Dish Type", "Cuisines")
    private val foodTypeLists = arrayOf("Vegetarian", "Non-Vegetarian", "Eggetarian", "Vegan", "Pescatarian")
    private var currentFragmentTag: String? = null
    private lateinit var tabContentAdapter: TabContentAdapter
    private var selectedMealType: String? = null
    private var selectedFoodType: String? = null
    private var selectedCuisine: String? = null
    private var loadingOverlay : FrameLayout? = null
    // Inside your fragment (class scope)
    private var expandedTabIndex: Int? = null   // track which tab's card is expanded

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRecipeSearchBinding
        get() = FragmentRecipeSearchBinding::inflate

    private val recipeSearchAdapter by lazy {
        RecipeSearchAdapter(requireContext(), arrayListOf(), -1, null, false, ::onSnapSearchDishItem)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        appPreference = AppPreference(requireContext())

        searchLayout = view.findViewById(R.id.layout_search)
        searchEditText = view.findViewById(R.id.et_search)
        cancel = view.findViewById(R.id.tv_cancel)
        searchResultListLayout = view.findViewById(R.id.layout_search_resultList)
        allDishesRecyclerview = view.findViewById(R.id.recyclerView_all_dishes)
        backButton = view.findViewById(R.id.backButton)
        tabLayout = view.findViewById(R.id.tabMacroLayout)
        tabContentCard = view.findViewById(R.id.tabContentCard)
        tabContentRecyclerView = view.findViewById(R.id.tabContentRecyclerView)
        spinner = view.findViewById(R.id.spinner)
        tabSelectedTitle = view.findViewById(R.id.tabSelectedTitle)

        tabContentRecyclerView.layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL,
            false)
        tabContentAdapter = TabContentAdapter { item, position, isClose ->
            if (position < tabContentAdapter.itemCount) {
                when (currentFragmentTag) {
                    "Meal Type" -> {
                        if (isClose) {
                            tabContentAdapter.deselectedUpdateItems(mealTypeList, -1)
                            selectedMealType = null
                            selectedMealType = null
                            recipesSearchViewModel.setSelectedMealType(null)
                            getFilterRecipesList(null, selectedFoodType, selectedCuisine)
                            updateTabColors(false) // Update colors when deselected
                            Log.d("RecipesSearchFragment", "Deselected Meal Type")
                        } else {
                            selectedMealType = item
                            selectedMealType = item
                            recipesSearchViewModel.setSelectedMealType(item)
                            getFilterRecipesList(selectedMealType, selectedFoodType, selectedCuisine)
                            updateTabColors(true) // Update colors when selected
                            Log.d("RecipesSearchFragment", "Selected Meal Type: $selectedMealType")
                        }
                    }
                    "Dish Type" -> {
                        if (isClose) {
                            tabContentAdapter.deselectedUpdateItems(foodTypeList, -1)
                            selectedFoodType = null
                            selectedFoodType = null
                            recipesSearchViewModel.setSelectedFoodType(null)
                            getFilterRecipesList(selectedMealType, null, selectedCuisine)
                            updateTabColors(false) // Update colors when deselected
                            Log.d("RecipesSearchFragment", "Deselected Dish Type")
                        } else {
                            selectedFoodType = item
                            recipesSearchViewModel.setSelectedFoodType(item)
                            selectedFoodType = item
                            getFilterRecipesList(selectedMealType, selectedFoodType, selectedCuisine)
                            updateTabColors(true) // Update colors when selected
                            Log.d("RecipesSearchFragment", "Selected Dish Type: $selectedFoodType")
                        }
                    }
                    "Cuisines" -> {
                        if (isClose) {
                            tabContentAdapter.deselectedUpdateItems(cuisineList, -1)
                            selectedCuisine = null
                            selectedCuisine = null
                            recipesSearchViewModel.setSelectedCuisine(null)
                            getFilterRecipesList(selectedMealType, selectedFoodType, null)
                            updateTabColors(false) // Update colors when deselected
                            Log.d("RecipesSearchFragment", "Deselected Cuisines")
                        } else {
                            selectedCuisine = item
                            recipesSearchViewModel.setSelectedCuisine(item)
                            selectedCuisine = item
                            getFilterRecipesList(selectedMealType, selectedFoodType, selectedCuisine)
                            updateTabColors(true) // Update colors when selected
                            Log.d("RecipesSearchFragment", "Selected Cuisines: $selectedCuisine")
                        }
                    }
                }
              //  tabContentAdapter.setSelectedPosition(if (isClose) -1 else position)
            }
        }
        tabContentRecyclerView.adapter = tabContentAdapter

        for (title in tabTitles) {
            val tab = tabLayout.newTab()
            val customView = LayoutInflater.from(context).inflate(R.layout.custom_tab_recipe_search, null) as ConstraintLayout
            val tabTextView = customView.findViewById<TextView>(R.id.tabText)
            tabTextView.text = title
            tab.customView = customView
            tabLayout.addTab(tab, false)
        }
        tabLayout.clearOnTabSelectedListeners()
        updateTabColors(false)
        tabContentCard.visibility = View.GONE

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    tabContentCard.visibility = View.VISIBLE
                    recipesSearchViewModel.setSelectedTabIndex(position) // <-- Save in VM
                    val tag = tabTitles[position]
                    currentFragmentTag = tag
                    // mark UI for selected tab & others
                    for (i in 0 until tabLayout.tabCount) {
                        tabLayout.getTabAt(i)?.let { otherTab ->
                            if (i == position) setTabSelectedUI(otherTab) else setTabUnselectedUI(otherTab)
                        }
                    }
                    // Check if the selected tab has content selected
                    val isContentSelected = when (tag) {
                        "Meal Type" -> selectedMealType != null
                        "Dish Type" -> selectedFoodType != null
                        "Cuisines" -> selectedCuisine != null
                        else -> false
                    }

                    // if switching to a different tab -> expand the card and load content
                    if (expandedTabIndex == position) {
                        // already the expanded tab — just ensure card visible
                        tabContentCard.visibility = View.VISIBLE
                    } else {
                        expandedTabIndex = position
                        tabContentCard.visibility = View.VISIBLE
                        updateCardViewContent(tabTitles[position])
                    }

                    updateTabColors(isContentSelected)
                    //  updateCardViewContent(tag)
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {
                tab?.let {
//                    val customView = it.customView
//                    val tabLayoutRoot = customView?.findViewById<View>(R.id.rootTabLayout)
//                    val tabText = customView?.findViewById<TextView>(R.id.tabText)
//                    val tabUpDownIcon = customView?.findViewById<ImageView>(R.id.imageArrow)
//                    tabLayoutRoot?.setBackgroundResource(R.drawable.tab_unselected_bg)
//                    tabUpDownIcon?.setImageResource(R.drawable.ic_chevron_down)
//                    val typeface = resources.getFont(R.font.dmsans_regular)
//                    tabText?.typeface = typeface
//                    tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_unselected_text))
                    // tabContentCard.visibility = View.GONE
                    setTabUnselectedUI(tab)
                }
            }
            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    val tag = tabTitles[position]
                    currentFragmentTag = tag

                    if (expandedTabIndex == position && tabContentCard.isVisible) {
                        tabContentCard.visibility = View.GONE
                        setTabUnselectedUI(tab)
                        expandedTabIndex = null
                    } else {
                        recipesSearchViewModel.setSelectedTabIndex(tab.position)
                        expandedTabIndex = position
                        tabContentCard.visibility = View.VISIBLE
                        setTabSelectedUI(tab)

                        updateCardViewContent(tag)
                    }
                    // Check if the reselected tab has content selected
                    val isContentSelected = when (tag) {
                        "Meal Type" -> selectedMealType != null
                        "Dish Type" -> selectedFoodType != null
                        "Cuisines" -> selectedCuisine != null
                        else -> false
                    }
                    updateTabColors(isContentSelected)
                }
            }
        })

        searchType = arguments?.getString("searchType").toString()
        mealType = arguments?.getString("mealType").toString()

        allDishesRecyclerview.layoutManager = GridLayoutManager(context, 2)
        allDishesRecyclerview.adapter = recipeSearchAdapter

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    recipesSearchViewModel.setSearchQuery("") // use null instead of "" for consistency
                    recipesSearchViewModel.setSelectedMealType(null)
                    recipesSearchViewModel.setSelectedFoodType(null)
                    recipesSearchViewModel.setSelectedCuisine(null)
                    recipesSearchViewModel.setSelectedTabIndex(null)
                    // clear local variables
                    selectedMealType = null
                    selectedFoodType = null
                    selectedCuisine = null
                    expandedTabIndex = null
                    // clear UI
                    searchEditText.setText("")
                    snapRecipesList.clear()
                    mealTypeList.clear()
                    foodTypeList.clear()
                    cuisineList.clear()
                    tabContentCard.visibility = View.GONE
                    // reset tab visuals
                    for (i in 0 until tabLayout.tabCount) {
                        tabLayout.getTabAt(i)?.let { setTabUnselectedUI(it) }
                    }
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle()
                    args.putString("ModuleName", "EatRight")
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }
            })

        backButton.setOnClickListener {
            recipesSearchViewModel.setSearchQuery("") // use null instead of "" for consistency
            recipesSearchViewModel.setSelectedMealType(null)
            recipesSearchViewModel.setSelectedFoodType(null)
            recipesSearchViewModel.setSelectedCuisine(null)
            recipesSearchViewModel.setSelectedTabIndex(null)
            // clear local variables
            selectedMealType = null
            selectedFoodType = null
            selectedCuisine = null
            expandedTabIndex = null
            // clear UI
            searchEditText.setText("")
            snapRecipesList.clear()
            mealTypeList.clear()
            foodTypeList.clear()
            cuisineList.clear()
            //  tabContentCard.visibility = View.GONE
            // reset tab visuals
            for (i in 0 until tabLayout.tabCount) {
                tabLayout.getTabAt(i)?.let { setTabUnselectedUI(it) }
            }
            val fragment = HomeBottomTabFragment()
            val args = Bundle()
            args.putString("ModuleName", "EatRight")
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        recipesSearchViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            filterDishes(query)
        }
//        cancel.setOnClickListener {
//            if (searchEditText.text.toString().isNotEmpty()){
//                recipesSearchViewModel.setSearchQuery("")
//                searchEditText.setText("")
//                snapRecipesList.clear()
//            }
//        }
        cancel.setOnClickListener {
            // clear ViewModel state
            recipesSearchViewModel.setSearchQuery("") // use null instead of "" for consistency
            recipesSearchViewModel.setSelectedMealType(null)
            recipesSearchViewModel.setSelectedFoodType(null)
            recipesSearchViewModel.setSelectedCuisine(null)
            recipesSearchViewModel.setSelectedTabIndex(null)
            // clear local variables
            selectedMealType = null
            selectedFoodType = null
            selectedCuisine = null
            expandedTabIndex = null
            // clear UI
            searchEditText.setText("")
            snapRecipesList.clear()
            mealTypeList.clear()
            foodTypeList.clear()
            cuisineList.clear()
            tabContentCard.visibility = View.GONE
            // reset tab visuals
            for (i in 0 until tabLayout.tabCount) {
                tabLayout.getTabAt(i)?.let { setTabUnselectedUI(it) }
            }
            // fetch fresh data
            getRecipesList()
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                recipesSearchViewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
        searchEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
        searchEditText.imeOptions = EditorInfo.IME_ACTION_DONE
        searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Hide keyboard when done is pressed
                val imm = context?.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(searchEditText.windowToken, 0)
                searchEditText.clearFocus()
                true
            } else {
                false
            }
        }

        getRecipesList()  // this will handle tab restore on success

        context?.let { it1 ->
            AnalyticsLogger.logEvent(
                it1, AnalyticsEvent.ER_ReceipeListingPage_Open
            )
        }
    }

    // Helper: set selected look
    private fun setTabSelectedUI(tab: TabLayout.Tab) {
        val customView = tab.customView
        val tabLayoutRoot = customView?.findViewById<View>(R.id.rootTabLayout)
        val tabText = customView?.findViewById<TextView>(R.id.tabText)
        val tabUpDownIcon = customView?.findViewById<ImageView>(R.id.imageArrow)

        tabLayoutRoot?.setBackgroundResource(R.drawable.tab_selected_bg)
        tabUpDownIcon?.setImageResource(R.drawable.ic_chevron_up)
        tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        tabText?.typeface = resources.getFont(R.font.dmsans_bold)
        tabSelectedTitle.text = tabText?.text.toString()
    }

    // Helper: set unselected look
    private fun setTabUnselectedUI(tab: TabLayout.Tab) {
        val customView = tab.customView
        val tabLayoutRoot = customView?.findViewById<View>(R.id.rootTabLayout)
        val tabText = customView?.findViewById<TextView>(R.id.tabText)
        val tabUpDownIcon = customView?.findViewById<ImageView>(R.id.imageArrow)

        tabLayoutRoot?.setBackgroundResource(R.drawable.tab_unselected_bg)
        tabUpDownIcon?.setImageResource(R.drawable.ic_chevron_down)
        tabText?.typeface = resources.getFont(R.font.dmsans_regular)
        tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_unselected_text))
    }

    private fun updateCardViewContent(tag: String) {
        tabContentRecyclerView.visibility = View.GONE
        spinner.visibility = View.GONE
        when (tag) {
            "Meal Type" -> {
                tabContentRecyclerView.visibility = View.VISIBLE
                tabContentAdapter.updateItems(mealTypeList)
                val positions = selectedMealType
                    ?.let { mealTypeList.indexOf(it) }
                    ?.takeIf { it != -1 }
                    ?.let { listOf(it) }
                    ?: emptyList()

                tabContentAdapter.setSelectedPosition(positions)
                refreshRecipesList()
            }
            "Dish Type" -> {
                tabContentRecyclerView.visibility = View.VISIBLE
                tabContentAdapter.updateItems(foodTypeList)
                val positions = selectedFoodType
                    ?.let { foodTypeList.indexOf(it) }
                    ?.takeIf { it != -1 }
                    ?.let { listOf(it) }
                    ?: emptyList()

                tabContentAdapter.setSelectedPosition(positions)
                refreshRecipesList()
            }
            "Cuisines" -> {
                tabContentRecyclerView.visibility = View.VISIBLE
                tabContentAdapter.updateItems(cuisineList)
                val positions = selectedCuisine
                    ?.let { cuisineList.indexOf(it) }
                    ?.takeIf { it != -1 }
                    ?.let { listOf(it) }
                    ?: emptyList()

                tabContentAdapter.setSelectedPosition(positions)
                refreshRecipesList()
//                spinner.visibility = View.VISIBLE
//                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cuisineList)
//                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//                spinner.adapter = adapter
//                spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
//                    override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
//                        selectedCuisine = cuisineList[position]
//                        getFilterRecipesList(null, null, selectedCuisine)
//                        Log.d("RecipesSearchFragment", "Selected cuisine: $selectedCuisine")
//                        refreshRecipesList()
//                    }
//                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
//                }
//                spinner.performClick()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTabColors(isTabContentSelected: Boolean) {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val customView = tab?.customView
            val tabText = customView?.findViewById<TextView>(R.id.tabText)
            val circleText = customView?.findViewById<TextView>(R.id.circleText)
            val imageArrow = customView?.findViewById<ImageView>(R.id.imageArrow)
            // Determine if the tab has selected content
            val isContentSelected = when (tabTitles[i]) {
                "Meal Type" -> selectedMealType != null
                "Dish Type" -> selectedFoodType != null
                "Cuisines" -> selectedCuisine != null
                else -> false
            }

            if (tab?.isSelected == true) {
//                tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
//                val typeface = resources.getFont(R.font.dmsans_bold)
//                tabText?.typeface = typeface
//                tabSelectedTitle.text = tabText?.text.toString()
                //  imageArrow?.setImageResource(R.drawable.ic_chevron_up)
                // Show circle only if content is selected for this tab
                if (isContentSelected) {
                    circleText?.setBackgroundResource(R.drawable.circle_white_background)
                    circleText?.visibility = View.VISIBLE
                } else {
                    circleText?.visibility = View.GONE
                }
            } else {
//                val typeface = resources.getFont(R.font.dmsans_regular)
//                tabText?.typeface = typeface
//                tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_unselected_text))
                //     imageArrow?.setImageResource(R.drawable.ic_chevron_down)
                // Show circle for unselected tabs if they have selected content
                if (isContentSelected) {
                    circleText?.setBackgroundResource(R.drawable.green_circle_background)
                    circleText?.visibility = View.VISIBLE
                    tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.border_green))
                } else {
                    circleText?.visibility = View.GONE
                }
            }
        }
    }

    private fun onSnapSearchDishItemRefresh() {
        val valueLists: ArrayList<IngredientRecipeList> = ArrayList()
        valueLists.addAll(snapRecipesList as Collection<IngredientRecipeList>)
        val mealLogDateData: IngredientRecipeList? = null
        recipeSearchAdapter.addAll(valueLists, -1, mealLogDateData, false)
        refreshRecipesList()
    }

    private fun onSnapSearchDishItem(recipesModel: IngredientRecipeList, position: Int, isRefresh: Boolean) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            val snapMealFragment = RecipeDetailsFragment()
            val args = Bundle()
            args.putString("searchType", searchType)
            args.putString("mealType", mealType)
            args.putString("recipeId", recipesModel.id)
            snapMealFragment.arguments = args
            replace(R.id.flFragment, snapMealFragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun filterDishes(query: String) {
        val filteredList = snapRecipesList.filter { recipe ->
            val matchesQuery =
                query.isBlank() || recipe.recipe.contains(query, ignoreCase = true)
            val matchesMealType = selectedMealType?.let { selected ->
                recipe.meal_type
                    ?.split(",")
                    ?.map { it.trim().lowercase() }
                    ?.any { it.contains(selected.lowercase()) }  // FIXED
                    ?: false
            } ?: true
            val matchesFoodType =
                selectedFoodType?.let { selected ->
                    recipe.tags
                        ?.split(",")
                        ?.map { it.trim().lowercase() }
                        ?.contains(selected.lowercase())
                        ?: false
                } ?: true
            val matchesCuisine =
                selectedCuisine?.let { recipe.cuisine == it } ?: true
            matchesQuery && matchesMealType && matchesFoodType && matchesCuisine
        }
        val mealLogDateData: IngredientRecipeList? = null
        recipeSearchAdapter.addAll(ArrayList(filteredList), -1, mealLogDateData, false)
        cancel.visibility = if (query.isNotEmpty()) View.VISIBLE else View.GONE
    }

    private fun refreshRecipesList() {
        val currentQuery = recipesSearchViewModel.searchQuery.value ?: ""
        filterDishes(currentQuery)
    }

    private fun getFilterRecipesList(
        mealType: String? = selectedMealType,
        foodType: String? = selectedFoodType,
        cuisine: String? = selectedCuisine
    ) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val call = ApiClient.apiServiceFastApiV2.getRecipesListWithFilter(
            mealType = mealType,
            foodType = foodType,
            cuisine = cuisine
        )
        call.enqueue(object : Callback<RecipeListResponse> {
            override fun onResponse(call: Call<RecipeListResponse>, response: Response<RecipeListResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealPlanLists = response.body()?.data ?: emptyList()
                    snapRecipesList.clear()
                    snapRecipesList.addAll(mealPlanLists)
                    Log.d("RecipesSearchFragment", "Meal Types: $mealTypeList")
                    Log.d("RecipesSearchFragment", "Dish Types: $foodTypeList")
                    Log.d("RecipesSearchFragment", "Cuisines: $cuisineList")
                    onSnapSearchDishItemRefresh()
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
            override fun onFailure(call: Call<RecipeListResponse>, t: Throwable) {
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

    private fun getRecipesList() {
        if (isAdded && view != null) {
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }

        Log.d("RecipesSearchFragment", "getRecipesList called → starting API fetch")

        val call = ApiClient.apiServiceFastApiV2.getRecipesList()
        Log.d("RecipesSearchFragment", "API call enqueued: getRecipesList")

        call.enqueue(object : Callback<RecipeListResponse> {
            override fun onResponse(call: Call<RecipeListResponse>, response: Response<RecipeListResponse>) {
                Log.d("RecipesSearchFragment", "onResponse received → isSuccessful: ${response.isSuccessful}, code: ${response.code()}")

                if (response.isSuccessful) {
                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }

                    val mealPlanLists = response.body()?.data ?: emptyList()
                    Log.d("RecipesSearchFragment", "Success → received ${mealPlanLists.size} recipes")

                    snapRecipesList.clear()
                    snapRecipesList.addAll(mealPlanLists)
                    Log.d("RecipesSearchFragment", "snapRecipesList updated → size now: ${snapRecipesList.size}")

                    mealTypeList.clear()
                    foodTypeList.clear()
                    cuisineList.clear()

                    val mealTypeLists = snapRecipesList
                        .mapNotNull { it.meal_type }
                        .flatMap { it.split(",", "/") }
                        .map { it.trim() }
                        .filter { it.isNotEmpty() }
                        .distinct()
                        .sorted()

                    mealTypeList.addAll(mealTypeLists)
                    Log.d("RecipesSearchFragment", "Meal Types extracted & added → count: ${mealTypeList.size}")


                    /*  val foodTypeLists = snapRecipesList
                          .mapNotNull { it.tags}
                          .flatMap { it.split(",", "/") }
                          .map { it.trim() }
                          .filter { it.isNotEmpty() }
                          .distinct()
                          .sorted()*/
                    foodTypeList.addAll(foodTypeLists)
                    Log.d("RecipesSearchFragment", "Food Types (tags) → current size: ${foodTypeList.size} (was commented in original)")

                    cuisineList.addAll(snapRecipesList.map { it.cuisine }.filterNotNull().distinct().sorted())
                    Log.d("RecipesSearchFragment", "Cuisines extracted & added → count: ${cuisineList.size}")

                    Log.d("RecipesSearchFragment", "Meal Types final: $mealTypeList")
                    Log.d("RecipesSearchFragment", "Dish Types final: $foodTypeList")
                    Log.d("RecipesSearchFragment", "Cuisines final: $cuisineList")

                    onSnapSearchDishItemRefresh()
                    Log.d("RecipesSearchFragment", "onSnapSearchDishItemRefresh() called")

                    // ✅ restore after data ready
                    restoreTabStateAfterData()
                    Log.d("RecipesSearchFragment", "restoreTabStateAfterData() called")
                } else {
                    val errorBody = response.errorBody()?.string()
                    Log.e("RecipesSearchFragment", "API error → code: ${response.code()}, error body: $errorBody")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()

                    if (isAdded && view != null) {
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }

            override fun onFailure(call: Call<RecipeListResponse>, t: Throwable) {
                Log.e("RecipesSearchFragment", "API call failed completely", t)
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()

                if (isAdded && view != null) {
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        })
    }

    private fun restoreTabStateAfterData() {
        val savedIndex = recipesSearchViewModel.selectedTabIndex.value
        if (savedIndex != null && savedIndex >= 0 && savedIndex < tabLayout.tabCount) {
            tabLayout.getTabAt(savedIndex)?.select()

            expandedTabIndex = savedIndex
            tabContentCard.visibility = View.VISIBLE
            updateCardViewContent(tabTitles[savedIndex])

            for (i in 0 until tabLayout.tabCount) {
                tabLayout.getTabAt(i)?.let { t ->
                    if (i == savedIndex) setTabSelectedUI(t) else setTabUnselectedUI(t)
                }
            }
        } else {
            expandedTabIndex = null
            tabContentCard.visibility = View.GONE
            updateTabColors(false)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putString("currentFragmentTag", currentFragmentTag)
        outState.putString("selectedMealType", selectedMealType)
        outState.putString("selectedFoodType", selectedFoodType)
        outState.putString("selectedCuisine", selectedCuisine)
    }

    override fun onViewStateRestored(savedInstanceState: Bundle?) {
        super.onViewStateRestored(savedInstanceState)
        savedInstanceState?.let {
            selectedMealType = it.getString("selectedMealType")
            selectedFoodType = it.getString("selectedFoodType")
            selectedCuisine = it.getString("selectedCuisine")
        }
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()

        recipesSearchViewModel.searchQuery.value?.let {
            searchEditText.setText(it)
            filterDishes(it)
        }
        selectedMealType = recipesSearchViewModel.selectedMealType.value
        selectedFoodType = recipesSearchViewModel.selectedFoodType.value
        selectedCuisine = recipesSearchViewModel.selectedCuisine.value

        refreshRecipesList()
    }
}