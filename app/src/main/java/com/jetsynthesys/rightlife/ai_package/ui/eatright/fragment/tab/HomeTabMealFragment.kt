package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab

import android.graphics.PorterDuff
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
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
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.flexbox.FlexboxLayout
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.jetsynthesys.rightlife.databinding.FragmentHomeTabMealBinding
import com.google.android.material.tabs.TabLayout
import com.google.gson.Gson
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.request.DishLog
import com.jetsynthesys.rightlife.ai_package.model.request.MealLogItem
import com.jetsynthesys.rightlife.ai_package.model.request.SaveDishLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.SaveSnapMealLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.SnapDish
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealLogRequest
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SearchResultItem
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealDetailsResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealLogResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.SearchDishToLogFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.frequentlylogged.FrequentlyAddDishBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MealLogItems
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SelectedMealLogList
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SnapDishLocalListModel
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SnapMealRequestLocalListModel
import com.jetsynthesys.rightlife.ai_package.utils.LoaderUtil
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HomeTabMealFragment : BaseFragment<FragmentHomeTabMealBinding>() {

    private lateinit var tabLayout : TabLayout
    private lateinit var backIc : ImageView
    private lateinit var searchLayout : LinearLayoutCompat
    private lateinit var searchType : String
    private lateinit var mealType : String
    private lateinit var frequentlyAddDishBottomSheetLayout : ConstraintLayout
    private lateinit var flexboxLayout: FlexboxLayout
    private val ingredientsList = ArrayList<String>()
    private lateinit var layoutTitle : LinearLayout
    private lateinit var btnLogMeal: LinearLayoutCompat
    private lateinit var checkCircle : ImageView
    private lateinit var loggedSuccess : TextView
    private var dishLists : ArrayList<SearchResultItem> = ArrayList()
    private  var snapDishLocalListModel : SnapDishLocalListModel? = null
    private var mealLogRequests : SelectedMealLogList? = null
    private var selectedMealLogList : ArrayList<MealLogItems> = ArrayList()
    private var snapMealLogRequests : SelectedMealLogList? = null
    private var selectedSnapMealLogList : ArrayList<MealLogItems> = ArrayList()
    private var isSnaps : Boolean = false
    private var snapMealRequestLocalListModel : SnapMealRequestLocalListModel? = null
    private var snapMealLogRequestList : ArrayList<SnapMealLogRequest> = ArrayList()
    private var snapMealRequestCount : Int = 0
    private var loadingOverlay : FrameLayout? = null
    private var tabType : String = ""

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHomeTabMealBinding
        get() = FragmentHomeTabMealBinding::inflate

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        tabLayout = view.findViewById<TabLayout>(R.id.tabLayout)
        backIc = view.findViewById(R.id.backIc)
        searchLayout = view.findViewById(R.id.searchLayout)
        frequentlyAddDishBottomSheetLayout = view.findViewById(R.id.frequentlyAddDishBottomSheetLayout)
        flexboxLayout = view.findViewById(R.id.flexboxLayout)
        val btnAdd: LinearLayoutCompat = view.findViewById(R.id.layout_btnAdd)
        btnLogMeal = view.findViewById(R.id.layout_btnLogMeal)
        layoutTitle = view.findViewById(R.id.layout_title)
        checkCircle = view.findViewById<ImageView>(R.id.check_circle_icon)
        loggedSuccess = view.findViewById<TextView>(R.id.tv_logged_success)

        searchType = arguments?.getString("searchType").toString()
        mealType = arguments?.getString("mealType").toString()
        tabType = arguments?.getString("tabType").toString()
        val dishLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
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
            snapMealLogRequestList.addAll(snapMealRequestLocalListModel!!.data)
        }

        if (selectedMealLogListModels != null){
            mealLogRequests = selectedMealLogListModels
            selectedMealLogList.addAll(mealLogRequests!!.meal_log)
        }

        if (selectedSnapMealLogListModels != null){
            isSnaps = true
            snapMealLogRequests = selectedSnapMealLogListModels
            selectedSnapMealLogList.addAll(snapMealLogRequests!!.meal_log)
        }

        if (dishLocalListModels != null){
            snapDishLocalListModel = dishLocalListModels
            dishLists.addAll(snapDishLocalListModel!!.data)
        }

        val tabTitles = arrayOf("Frequently Logged", "My Meal", "My Recipe")

        for (title in tabTitles) {
            val tab = tabLayout.newTab()
            val customView =
                LayoutInflater.from(context).inflate(R.layout.custom_tab, null) as TextView
            customView.text = title
            tab.customView = customView
            tabLayout.addTab(tab)
        }

        val deleteType = arguments?.getString("deleteType").toString()?: ""

        if (deleteType.contentEquals("MyMeal")){
            // Set default fragment
            //if (savedInstanceState == null) {
                replaceFragment(MyMealFragment())
                updateTabColors()
          //  }
        }else{
            // Set default fragment
            if (tabType != null && tabType != "null"){
                if (tabType.contentEquals("MyMeal")){
                    replaceFragment(MyMealFragment())
                    tabLayout.getTabAt(1)?.select()
                    updateTabColors()
                }else if (tabType.contentEquals("MyRecipe")){
                    replaceFragment(MyRecipeFragment())
                    tabLayout.getTabAt(2)?.select()
                    updateTabColors()
                }
            }else{
                if (savedInstanceState == null) {
                    replaceFragment(FrequentlyLoggedFragment())
                    updateTabColors()
                }
            }
        }

        // Handle tab clicks manually
        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> replaceFragment(FrequentlyLoggedFragment())
                    1 -> replaceFragment(MyMealFragment())
                  //  2 -> replaceFragment(MealPlanFragment())
                    2 -> replaceFragment(MyRecipeFragment())
                }
                updateTabColors()
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    val fragment = YourMealLogsFragment()
                    val args = Bundle()
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "landing")
                        addToBackStack("landing")
                        commit()
                    }
                }
            })

        backIc.setOnClickListener {
            val fragment = YourMealLogsFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        searchLayout.setOnClickListener {
            if (snapMealLogRequestList.size > 0){
                snapMealRequestLocalListModel = SnapMealRequestLocalListModel(snapMealLogRequestList)
            }
            val fragment = SearchDishToLogFragment()
            val args = Bundle()
            args.putString("searchType", "HomeTabMeal")
            args.putString("mealType", mealType)
            args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
            args.putParcelable("selectedMealLogList", mealLogRequests)
            args.putParcelable("selectedSnapMealLogList", snapMealLogRequests)
            args.putParcelable("snapMealRequestLocalListModel", snapMealRequestLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        if (searchType.contentEquals("DishToLog")){
            if (snapDishLocalListModel != null){
                //loggedAddDish(snapDishLocalListModel)
                frequentlyAddDishBottomSheetLayout.visibility = View.VISIBLE
                flexboxLayout.visibility = View.VISIBLE
                layoutTitle.visibility = View.VISIBLE
                btnLogMeal.visibility = View.VISIBLE
                // Display default ingredients
                if (dishLists.size > 0){
                    for (dishItem in dishLists) {
                        ingredientsList.add(dishItem.name!!)
                    }
                }
                if (mealLogRequests != null) {
                    if (selectedMealLogList.size > 0) {
                        for (dishItem in selectedMealLogList) {
                            ingredientsList.add(dishItem.recipe_name!!)
                        }
                    }
                }

                if (snapMealLogRequests != null){
                    if (selectedSnapMealLogList.size > 0){
                        for (mealItem in selectedSnapMealLogList){
                            ingredientsList.add(mealItem.recipe_name!!)
                        }
                    }
                }

                if (ingredientsList.size > 0){
                    updateIngredientChips()
                }
            }
        }

        btnLogMeal.setOnClickListener {
            if (snapDishLocalListModel != null){
                if (mealType.isNotEmpty()){
                    if (dishLists.size > 0){
                        createDishLog()
                    }
                }
            }else{
                if (mealLogRequests != null){
                    if (mealType.isNotEmpty()){
                        if (selectedMealLogList.size > 0){
                            createDishLog()
                        }
                    }
                }
            }

            if (isSnaps){
                if (snapMealLogRequests != null){
                    if (mealType.isNotEmpty()){
                        if (selectedSnapMealLogList.size > 0){
                           // createSnapMealLog()
                            if (snapMealLogRequestList.size > 0){
                                snapMealLogRequestList?.forEach { snapDish ->
                                    createSnapMealLog(snapDish)
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    // Function to replace fragments
    private fun replaceFragment(fragment: Fragment) {
        childFragmentManager.beginTransaction().apply {
                val args = Bundle()
                args.putString("mealType", mealType)
                fragment.arguments = args
                replace(R.id.fragmentContainer, fragment, "homeTab")
                commit()
            }
    }

    // Function to update tab selection colors
    @RequiresApi(Build.VERSION_CODES.O)
    private fun updateTabColors() {
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val customView = tab?.customView
            val tabText = customView?.findViewById<TextView>(R.id.tabText)

            if (tab?.isSelected == true) {
                tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                val typeface = resources.getFont(R.font.dmsans_bold)
                tabText?.typeface = typeface
            } else {
                val typeface = resources.getFont(R.font.dmsans_regular)
                tabText?.typeface = typeface
                tabText?.setTextColor(ContextCompat.getColor(requireContext(), R.color.tab_unselected_text))
            }
        }
    }

    private fun loggedAddDish(snapDishLocalListModel: SnapDishLocalListModel?) {
        val frequentlyAddDishBottomSheet = FrequentlyAddDishBottomSheet()
        frequentlyAddDishBottomSheet.isCancelable = true
        val args = Bundle()
        args.putString("mealType", mealType)
        args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
        args.putBoolean("test",false)
        frequentlyAddDishBottomSheet.arguments = args
        activity?.supportFragmentManager?.let { frequentlyAddDishBottomSheet.show(it, "FrequentlyAddDishBottomSheet") }
    }

    private fun createDishLog() {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdhNWZhZTkxOTc5OTI1MTFlNzFiMWM4Iiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiJBZGl0eWEiLCJsYXN0TmFtZSI6IlR5YWdpIiwiZGV2aWNlSWQiOiJCNkRCMTJBMy04Qjc3LTRDQzEtOEU1NC0yMTVGQ0U0RDY5QjQiLCJtYXhEZXZpY2VSZWFjaGVkIjpmYWxzZSwidHlwZSI6ImFjY2Vzcy10b2tlbiJ9LCJpYXQiOjE3MzkxNzE2NjgsImV4cCI6MTc1NDg5NjQ2OH0.koJ5V-vpGSY1Irg3sUurARHBa3fArZ5Ak66SkQzkrxM"
        // val userId = "64763fe2fa0e40d9c0bc8264"
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)

        val dishLogList : ArrayList<DishLog> = ArrayList()
        val mealNamesString = dishLists.map { it.name ?: "" }.joinToString(", ")

        if (snapDishLocalListModel != null){
            if (dishLists.size > 0) {
                dishLists?.forEach { snapRecipe ->
                    val mealLogData = DishLog(
                        receipe_id = snapRecipe.id,
                        meal_quantity = 1.0,
                        unit = "g",
                        measure = "Bowl"
                    )
                    dishLogList.add(mealLogData)
                }
            }
        }

        if (mealLogRequests != null){
            if (selectedMealLogList.size > 0){
                selectedMealLogList?.forEach { selectedDish ->
                    val mealLogData = DishLog(
                        receipe_id = selectedDish.meal_id,
                        meal_quantity = 1.0,
                        unit = "g",
                        measure = "Bowl"
                    )
                    dishLogList.add(mealLogData)
                }
            }
        }
        val dishLogRequest = SaveDishLogRequest(
            meal_type = mealType,
            meal_log = dishLogList
        )
        val call = ApiClient.apiServiceFastApi.createSaveMealsToLog(userId, formattedDate, dishLogRequest)
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
                    flexboxLayout.visibility = View.GONE
                    layoutTitle.visibility = View.GONE
                    btnLogMeal.visibility = View.GONE
                    checkCircle.visibility = View.VISIBLE
                    loggedSuccess.visibility = View.VISIBLE
                    loggedSuccess.text = mealData
                    frequentlyAddDishBottomSheetLayout.visibility = View.GONE
                    val fragment = YourMealLogsFragment()
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

    private fun createSnapMealLog() {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdhNWZhZTkxOTc5OTI1MTFlNzFiMWM4Iiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiJBZGl0eWEiLCJsYXN0TmFtZSI6IlR5YWdpIiwiZGV2aWNlSWQiOiJCNkRCMTJBMy04Qjc3LTRDQzEtOEU1NC0yMTVGQ0U0RDY5QjQiLCJtYXhEZXZpY2VSZWFjaGVkIjpmYWxzZSwidHlwZSI6ImFjY2Vzcy10b2tlbiJ9LCJpYXQiOjE3MzkxNzE2NjgsImV4cCI6MTc1NDg5NjQ2OH0.koJ5V-vpGSY1Irg3sUurARHBa3fArZ5Ak66SkQzkrxM"
        // val userId = "64763fe2fa0e40d9c0bc8264"
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val snapMealLogList : ArrayList<MealLogItem> = ArrayList()
        if (snapMealLogRequests != null){
            if (selectedSnapMealLogList.size > 0){
                selectedSnapMealLogList?.forEach { selectedDish ->
                    val mealLogData = MealLogItem(
                        meal_id = selectedDish.meal_id,
                        meal_quantity = 1,
                        unit = "g",
                        measure = "Bowl"
                    )
                    snapMealLogList.add(mealLogData)
                }
            }
        }
        val snapMealLogRequest = SaveSnapMealLogRequest(
            meal_type = mealType,
            meal_name = "Meal1",
            meal_log = snapMealLogList
        )
        val call = ApiClient.apiServiceFastApi.createSaveSnapMealsToLog(userId, formattedDate, snapMealLogRequest)
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
                    flexboxLayout.visibility = View.GONE
                    layoutTitle.visibility = View.GONE
                    btnLogMeal.visibility = View.GONE
                    checkCircle.visibility = View.VISIBLE
                    loggedSuccess.visibility = View.VISIBLE
                    loggedSuccess.text = mealData
                    frequentlyAddDishBottomSheetLayout.visibility = View.GONE
                    val fragment = YourMealLogsFragment()
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

    // Function to update Flexbox with chips
    private fun updateIngredientChips() {
        flexboxLayout.removeAllViews() // Clear existing chips
        for (ingredient in ingredientsList) {
            val chipView = LayoutInflater.from(context).inflate(R.layout.chip_ingredient, flexboxLayout, false)
            val tvIngredient: TextView = chipView.findViewById(R.id.tvIngredient)
            val btnRemove: ImageView = chipView.findViewById(R.id.btnRemove)
            btnRemove.setColorFilter(ContextCompat.getColor(requireContext(), R.color.white), PorterDuff.Mode.SRC_IN)
            tvIngredient.text = ingredient
            btnRemove.setOnClickListener {
                ingredientsList.remove(ingredient)
                updateIngredientChips()
            }
            flexboxLayout.addView(chipView)
        }
    }

    fun setSelectedFrequentlyLog(mealLogRequest: MealLogItems?, isSnap: Boolean, mealLogRequest1: SelectedMealLogList?,
                                 snapMealLogRequest : SnapMealLogRequest?) {

        if (isSnap){
            isSnaps = isSnap
            if (mealLogRequest != null){
                frequentlyAddDishBottomSheetLayout.visibility = View.VISIBLE
                flexboxLayout.visibility = View.VISIBLE
                layoutTitle.visibility = View.VISIBLE
                btnLogMeal.visibility = View.VISIBLE
                ingredientsList.add(mealLogRequest.recipe_name!!)
                if (ingredientsList.size > 0){
                    updateIngredientChips()
                }
                selectedSnapMealLogList.add(mealLogRequest!!)
            }
            val selectedSnapMealList = SelectedMealLogList(
                meal_name = mealLogRequest!!.recipe_name,
                meal_type = mealType,
                meal_log = selectedSnapMealLogList
            )
            snapMealLogRequests = selectedSnapMealList
            if (snapMealLogRequest != null){
                snapMealLogRequestList.add(snapMealLogRequest)
            }
        }else{
            if (mealLogRequest != null){
                frequentlyAddDishBottomSheetLayout.visibility = View.VISIBLE
                flexboxLayout.visibility = View.VISIBLE
                layoutTitle.visibility = View.VISIBLE
                btnLogMeal.visibility = View.VISIBLE
                ingredientsList.add(mealLogRequest.recipe_name!!)
                if (ingredientsList.size > 0){
                    updateIngredientChips()
                }
                selectedMealLogList.add(mealLogRequest!!)
            }

            if (mealLogRequest1 != null){
                frequentlyAddDishBottomSheetLayout.visibility = View.VISIBLE
                flexboxLayout.visibility = View.VISIBLE
                layoutTitle.visibility = View.VISIBLE
                btnLogMeal.visibility = View.VISIBLE
                ingredientsList.add(mealLogRequest1.meal_name!!)
                if (ingredientsList.size > 0){
                    updateIngredientChips()
                }
                val selectedMealLog : ArrayList<MealLogItems> = ArrayList()
                val mealLogList = mealLogRequest1.meal_log
                mealLogList.forEach { selectedDish ->
                    val mealLogData = MealLogItems(
                        meal_id = selectedDish.meal_id,
                        recipe_name = selectedDish.recipe_name,
                        meal_quantity = selectedDish.meal_quantity,
                        unit = selectedDish.unit,
                        measure = selectedDish.measure
                    )
                    selectedMealLog.add(mealLogData)
                }
                selectedMealLogList.addAll(selectedMealLog)
            }

            val selectedMealList = SelectedMealLogList(
                meal_name = mealLogRequest1?.meal_name ?: mealLogRequest?.recipe_name,
                meal_type = mealType,
                meal_log = selectedMealLogList
            )
            mealLogRequests = selectedMealList
        }
    }

    private fun getMealDetails(selectedMealLogListModels: SelectedMealLogList?) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val call = ApiClient.apiServiceFastApi.fetchMealDetails(userId, "")
        call.enqueue(object : Callback<SnapMealDetailsResponse> {
            override fun onResponse(call: Call<SnapMealDetailsResponse>, response: Response<SnapMealDetailsResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealDetails = response.body()?.data
                    if (mealDetails != null){
                       println(mealDetails)
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
            override fun onFailure(call: Call<SnapMealDetailsResponse>, t: Throwable) {
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

    private fun createSnapMealLog(snapRecipeList: SnapMealLogRequest) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateUtc: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val snapDishList: ArrayList<SnapDish> = ArrayList()
        if (snapRecipeList.dish.isNotEmpty()) {
            val items = snapRecipeList.dish
            items?.forEach { snapDish ->
                snapDishList.add(snapDish)
            }
            val snapMealLogRequest = SnapMealLogRequest(
                user_id = userId,
                meal_type = mealType,
                meal_name = snapRecipeList.meal_name,
                is_save = false,
                is_snapped = true,
                date = currentDateUtc,
                dish = snapDishList
            )
            val gson = Gson()
            val jsonString = gson.toJson(snapMealLogRequest) // snapMealLogRequest is your model instance
            Log.d("JSON Output", jsonString)
            val call = ApiClient.apiServiceFastApi.createSnapMealLog(snapMealLogRequest)
            call.enqueue(object : Callback<SnapMealLogResponse> {
                override fun onResponse(
                    call: Call<SnapMealLogResponse>,
                    response: Response<SnapMealLogResponse>
                ) {
                    if (response.isSuccessful) {
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                        snapMealRequestCount++
                        val mealData = response.body()?.message
                        Toast.makeText(activity, mealData, Toast.LENGTH_SHORT).show()
                        if (snapMealLogRequestList.size == snapMealRequestCount){
                            flexboxLayout.visibility = View.GONE
                            layoutTitle.visibility = View.GONE
                            btnLogMeal.visibility = View.GONE
                            checkCircle.visibility = View.VISIBLE
                            loggedSuccess.visibility = View.VISIBLE
                            loggedSuccess.text = mealData
                            frequentlyAddDishBottomSheetLayout.visibility = View.GONE
                            val fragment = YourMealLogsFragment()
                            val args = Bundle()
                            fragment.arguments = args
                            requireActivity().supportFragmentManager.beginTransaction().apply {
                                replace(R.id.flFragment, fragment, "landing")
                                addToBackStack("landing")
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
                override fun onFailure(call: Call<SnapMealLogResponse>, t: Throwable) {
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