package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
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
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.tabs.TabLayout
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.TabContentAdapter
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealRecipeResponseModel
import com.jetsynthesys.rightlife.ai_package.model.response.SnapRecipeList
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.RecipeSearchAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.viewmodel.DishesViewModel
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.utils.AppPreference
import com.jetsynthesys.rightlife.databinding.FragmentRecipeSearchBinding
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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
    private val dishesViewModel: DishesViewModel by activityViewModels()
    private var snapRecipesList: ArrayList<SnapRecipeList> = ArrayList()
    private var mealTypeList: ArrayList<String> = ArrayList()
    private var foodTypeList: ArrayList<String> = ArrayList()
    private var cuisineList: ArrayList<String> = ArrayList()
    private lateinit var backButton: ImageView
    private lateinit var mealType: String
    private val tabTitles = arrayOf("Meal Type", "Food Type", "Cuisine")
    private var currentFragmentTag: String? = null
    private lateinit var tabContentAdapter: TabContentAdapter
    private var selectedMealType: String? = null
    private var selectedFoodType: String? = null
    private var selectedCuisine: String? = null

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
        searchResultLayout = view.findViewById(R.id.layout_search_result)
        searchResultListLayout = view.findViewById(R.id.layout_search_resultList)
        tvAllDishes = view.findViewById(R.id.tv_all_dishes)
        allDishesRecyclerview = view.findViewById(R.id.recyclerView_all_dishes)
        backButton = view.findViewById(R.id.backButton)
        tabLayout = view.findViewById(R.id.tabMacroLayout)
        tabContentCard = view.findViewById(R.id.tabContentCard)
        tabContentRecyclerView = view.findViewById(R.id.tabContentRecyclerView)
        spinner = view.findViewById(R.id.spinner)

        tabContentRecyclerView.layoutManager = GridLayoutManager(context, 4)
        tabContentAdapter = TabContentAdapter { item, position ->
            if (position < tabContentAdapter.itemCount) {
                when (currentFragmentTag) {
                    "Meal Type" -> {
                        selectedMealType = item
                        getFilterRecipesList(selectedMealType, null, null)
                        Log.d("RecipesSearchFragment", "Selected Meal Type: $selectedMealType")
                    }
                    "Food Type" -> {
                        selectedFoodType = item
                        getFilterRecipesList(null, selectedFoodType, null)
                        Log.d("RecipesSearchFragment", "Selected Food Type: $selectedFoodType")
                    }
                    "Cuisine" -> {
                        selectedCuisine = item
                        getFilterRecipesList(null, null, selectedCuisine)
                        Log.d("RecipesSearchFragment", "Selected Cuisine: $selectedCuisine")
                    }
                }
                tabContentAdapter.setSelectedPosition(position)
                refreshRecipesList()
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
        updateTabColors()
        tabContentCard.visibility = View.GONE

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    val tag = tabTitles[position]
                    currentFragmentTag = tag
                    updateTabColors()
                    tabContentCard.visibility = View.VISIBLE
                    updateCardViewContent(tag)
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}

            override fun onTabReselected(tab: TabLayout.Tab?) {
                tab?.position?.let { position ->
                    val tag = tabTitles[position]
                    if (tabContentCard.visibility == View.VISIBLE) {
                        tabContentCard.visibility = View.GONE
                    } else {
                        tabContentCard.visibility = View.VISIBLE
                        updateCardViewContent(tag)
                    }
                    updateTabColors()
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
                    val fragment = HomeBottomTabFragment()
                    val args = Bundle()
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }
            })

        backButton.setOnClickListener {
            val fragment = HomeBottomTabFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                dishesViewModel.setSearchQuery(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })

        if (searchType.contentEquals("HomeTabMeal")) {
            getSnapMealRecipesList()
            onSnapSearchDishItemRefresh()
        } else {
            getSnapMealRecipesList()
            onSnapSearchDishItemRefresh()
        }
    }

    private fun updateCardViewContent(tag: String) {
        tabContentRecyclerView.visibility = View.GONE
        spinner.visibility = View.GONE

        when (tag) {
            "Meal Type" -> {
                tabContentRecyclerView.visibility = View.VISIBLE
                tabContentAdapter.updateItems(mealTypeList)
            }
            "Food Type" -> {
                tabContentRecyclerView.visibility = View.VISIBLE
                tabContentAdapter.updateItems(foodTypeList)
            }
            "Cuisine" -> {
                spinner.visibility = View.VISIBLE
                val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, cuisineList)
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                spinner.adapter = adapter
                spinner.onItemSelectedListener = object : android.widget.AdapterView.OnItemSelectedListener {
                    override fun onItemSelected(parent: android.widget.AdapterView<*>?, view: View?, position: Int, id: Long) {
                        selectedCuisine = cuisineList[position]
                        getFilterRecipesList(null, null, selectedCuisine)
                        Log.d("RecipesSearchFragment", "Selected cuisine: $selectedCuisine")
                        refreshRecipesList()
                    }
                    override fun onNothingSelected(parent: android.widget.AdapterView<*>?) {}
                }
                spinner.performClick()
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTabColors() {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val customView = tab?.customView
            val tabText = customView?.findViewById<TextView>(R.id.tabText)
            val circleText = customView?.findViewById<TextView>(R.id.circleText)

            if (tab?.isSelected == true) {
                tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                val typeface = resources.getFont(R.font.dmsans_bold)
                tabText?.typeface = typeface
                circleText?.visibility = View.VISIBLE
            } else {
                val typeface = resources.getFont(R.font.dmsans_regular)
                tabText?.typeface = typeface
                tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_unselected_text))
                circleText?.visibility = View.GONE
            }
        }
    }

    private fun onSnapSearchDishItemRefresh() {
        val valueLists: ArrayList<SnapRecipeList> = ArrayList()
        valueLists.addAll(snapRecipesList as Collection<SnapRecipeList>)
        val mealLogDateData: SnapRecipeList? = null
        recipeSearchAdapter.addAll(valueLists, -1, mealLogDateData, false)

        dishesViewModel.searchQuery.observe(viewLifecycleOwner) { query ->
            filterDishes(query)
        }
    }

    private fun onSnapSearchDishItem(recipesModel: SnapRecipeList, position: Int, isRefresh: Boolean) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            val snapMealFragment = RecipeDetailsFragment()
            val args = Bundle()
            args.putString("searchType", searchType)
            args.putString("mealType", mealType)
            args.putParcelable("snapRecipeList", recipesModel)
            snapMealFragment.arguments = args
            replace(R.id.flFragment, snapMealFragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun filterDishes(query: String) {
        val filteredList = snapRecipesList.filter { recipe ->
            val matchesQuery = if (query.isEmpty()) true else recipe.name.contains(query, ignoreCase = true)
            val matchesMealType = selectedMealType?.let { recipe.meal_type == it } ?: true
            val matchesFoodType = selectedFoodType?.let { recipe.food_type == it } ?: true
            val matchesCuisine = selectedCuisine?.let { recipe.cuisine == it } ?: true
            matchesQuery && matchesMealType && matchesFoodType && matchesCuisine
        }
        recipeSearchAdapter.updateList(filteredList)
        searchResultLayout.visibility = View.VISIBLE
    }

    private fun refreshRecipesList() {
        val currentQuery = dishesViewModel.searchQuery.value ?: ""
        filterDishes(currentQuery)
    }

    private fun getFilterRecipesList(
        mealType: String? = selectedMealType,
        foodType: String? = selectedFoodType,
        cuisine: String? = selectedCuisine
    ) {
        val call = ApiClient.apiServiceFastApi.getRecipesList(
            mealType = mealType,
            foodType = foodType,
            cuisine = cuisine
        )
        call.enqueue(object : Callback<SnapMealRecipeResponseModel> {
            override fun onResponse(call: Call<SnapMealRecipeResponseModel>, response: Response<SnapMealRecipeResponseModel>) {
                if (response.isSuccessful) {
                    val mealPlanLists = response.body()?.data ?: emptyList()
                    snapRecipesList.clear()
                    snapRecipesList.addAll(mealPlanLists)
                    Log.d("RecipesSearchFragment", "Meal Types: $mealTypeList")
                    Log.d("RecipesSearchFragment", "Food Types: $foodTypeList")
                    Log.d("RecipesSearchFragment", "Cuisines: $cuisineList")
                    onSnapSearchDishItemRefresh()
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<SnapMealRecipeResponseModel>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun getSnapMealRecipesList() {
        val call = ApiClient.apiServiceFastApi.getSnapMealRecipesList()
        call.enqueue(object : Callback<SnapMealRecipeResponseModel> {
            override fun onResponse(call: Call<SnapMealRecipeResponseModel>, response: Response<SnapMealRecipeResponseModel>) {
                if (response.isSuccessful) {
                    val mealPlanLists = response.body()?.data ?: emptyList()
                    snapRecipesList.clear()
                    snapRecipesList.addAll(mealPlanLists)
                    mealTypeList.clear()
                    foodTypeList.clear()
                    cuisineList.clear()
                    mealTypeList.addAll(snapRecipesList.map { it.meal_type }.distinct())
                    foodTypeList.addAll(snapRecipesList.map { it.food_type }.distinct())
                    cuisineList.addAll(snapRecipesList.map { it.cuisine }.distinct())
                    Log.d("RecipesSearchFragment", "Meal Types: $mealTypeList")
                    Log.d("RecipesSearchFragment", "Food Types: $foodTypeList")
                    Log.d("RecipesSearchFragment", "Cuisines: $cuisineList")
                    onSnapSearchDishItemRefresh()
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<SnapMealRecipeResponseModel>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
            }
        })
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
}