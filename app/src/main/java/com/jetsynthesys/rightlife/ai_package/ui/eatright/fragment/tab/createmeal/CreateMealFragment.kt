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
import com.jetsynthesys.rightlife.ai_package.model.request.DishLog
import com.jetsynthesys.rightlife.ai_package.model.request.MealLog
import com.jetsynthesys.rightlife.ai_package.model.request.MealPlanRequest
import com.jetsynthesys.rightlife.ai_package.model.request.MealSaveRequest
import com.jetsynthesys.rightlife.ai_package.model.request.UpdateMealRequest
import com.jetsynthesys.rightlife.ai_package.model.response.MealPlanResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SearchResultItem
import com.jetsynthesys.rightlife.ai_package.model.response.SnapRecipeData
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.createmeal.DishListAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.DeleteDishBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.HomeTabMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SnapDishLocalListModel
import com.jetsynthesys.rightlife.ai_package.utils.LoaderUtil
import com.jetsynthesys.rightlife.databinding.FragmentCreateMealBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class CreateMealFragment : BaseFragment<FragmentCreateMealBinding>() {

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
  //  var mealLogLists : ArrayList<MealLists> = ArrayList()
    private var dishLists : ArrayList<SearchResultItem> = ArrayList()
    private  var snapDishLocalListModel : SnapDishLocalListModel? = null
    private var mealId : String = ""
    private lateinit var mealType : String
    private var mealName : String = ""
    private var loadingOverlay : FrameLayout? = null

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

        mealId = arguments?.getString("mealId").toString()
        mealType = arguments?.getString("mealType").toString()
        mealName = arguments?.getString("mealName").toString()

       val dishLocalListModels = if (Build.VERSION.SDK_INT >= 33) {
            arguments?.getParcelable("snapDishLocalListModel", SnapDishLocalListModel::class.java)
        } else {
            arguments?.getParcelable("snapDishLocalListModel")
        }

        if (dishLocalListModels != null){
            snapDishLocalListModel = dishLocalListModels
            dishLists.addAll(snapDishLocalListModel!!.data)
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
                args.putString("mealType", mealType)
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "landing")
                    addToBackStack("landing")
                    commit()
                }
            }
        })

        backButton.setOnClickListener {
            val fragment = HomeTabMealFragment()
            val args = Bundle()
            args.putString("mealType", mealType)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
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
            addMealNameLayout.visibility = View.VISIBLE
            continueLayout.visibility = View.VISIBLE
            addedMealListLayout.visibility = View.GONE
            saveMealLayout.visibility = View.GONE
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
        }

        editMeal.setOnClickListener {
            addMealNameLayout.visibility = View.VISIBLE
            addedMealListLayout.visibility = View.GONE
            saveMealLayout.visibility = View.GONE
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
            val fragment = SearchDishFragment()
            val args = Bundle()
            args.putString("searchType", "createMeal")
            args.putString("mealId", mealId)
            args.putString("mealType", mealType)
            args.putString("mealName", addedNameTv.text.toString())
            args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
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

        val valueLists : ArrayList<SearchResultItem> = ArrayList()
        valueLists.addAll(dishLists as Collection<SearchResultItem>)
        val mealLog: SearchResultItem? = null
        dishListAdapter.addAll(valueLists, -1, mealLog, false)
    }

    private fun onMealLogClickItem(mealLog: SearchResultItem, position: Int, isRefresh: Boolean) {

        val valueLists : ArrayList<SearchResultItem> = ArrayList()
        valueLists.addAll(dishLists as Collection<SearchResultItem>)
        dishListAdapter.addAll(valueLists, position, mealLog, isRefresh)
    }

    private fun onMealLogDeleteItem(mealItem: SearchResultItem, position: Int, isRefresh: Boolean) {

        val valueLists : ArrayList<SearchResultItem> = ArrayList()
        valueLists.addAll(dishLists as Collection<SearchResultItem>)
        dishListAdapter.addAll(valueLists, position, mealItem, isRefresh)

        deleteMealDialog(mealItem)
    }

    private fun onMealLogEditItem(mealItem: SearchResultItem, position: Int, isRefresh: Boolean) {

        val valueLists : ArrayList<SearchResultItem> = ArrayList()
        valueLists.addAll(dishLists as Collection<SearchResultItem>)
        dishListAdapter.addAll(valueLists, position, mealItem, isRefresh)

        requireActivity().supportFragmentManager.beginTransaction().apply {
            val fragment = DishFragment()
            val args = Bundle()
            args.putString("searchType", "createMeal")
            args.putString("mealId", mealId)
            args.putString("mealType", mealType)
            args.putString("mealName", addedNameTv.text.toString())
            args.putString("snapRecipeName", mealItem.name)
            args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
            fragment.arguments = args
            replace(R.id.flFragment, fragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun deleteMealDialog(mealItem: SearchResultItem){

        val deleteDishBottomSheet = DeleteDishBottomSheet()
        deleteDishBottomSheet.isCancelable = true
        val args = Bundle()
        args.putBoolean("test",false)
        args.putString("mealId", mealId)
        args.putString("mealType", mealType)
        args.putString("mealName", addedNameTv.text.toString())
        args.putString("snapRecipeName", mealItem.name)
        args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
        deleteDishBottomSheet.arguments = args
        activity?.supportFragmentManager?.let { deleteDishBottomSheet.show(it, "DeleteDishBottomSheet") }
    }

    private fun getMealList() {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
         val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjdhNWZhZTkxOTc5OTI1MTFlNzFiMWM4Iiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiJBZGl0eWEiLCJsYXN0TmFtZSI6IlR5YWdpIiwiZGV2aWNlSWQiOiJCNkRCMTJBMy04Qjc3LTRDQzEtOEU1NC0yMTVGQ0U0RDY5QjQiLCJtYXhEZXZpY2VSZWFjaGVkIjpmYWxzZSwidHlwZSI6ImFjY2Vzcy10b2tlbiJ9LCJpYXQiOjE3MzkxNzE2NjgsImV4cCI6MTc1NDg5NjQ2OH0.koJ5V-vpGSY1Irg3sUurARHBa3fArZ5Ak66SkQzkrxM"
       // val userId = "64763fe2fa0e40d9c0bc8264"
        val startDate = "2025-04-10"
        val call = ApiClient.apiServiceFastApi.getMealList(userId, startDate)
        call.enqueue(object : Callback<MealsResponse> {
            override fun onResponse(call: Call<MealsResponse>, response: Response<MealsResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealPlanLists = response.body()?.meals ?: emptyList()
                  //  mealLogLists.addAll(mealPlanLists)
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
            override fun onFailure(call: Call<MealsResponse>, t: Throwable) {
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

    private fun createMeal(mealDetails: ArrayList<SnapRecipeData>) {
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
        val dishIds = mutableListOf<String>()

        for (item in mealDetails){
            dishIds.add(item.id!!)
        }

        val mealLogRequest = MealPlanRequest(
            meal_plan_name = addedNameTv.text.toString(),
            dish_ids = dishIds,
            date = formattedDate
        )

//        val mealLogRequest = MealLogRequest(
//            mealId = mealDetails._id,
//            userId = "64763fe2fa0e40d9c0bc8264",
//            meal = mealDetails.name,
//            date = formattedDate,
//            image = mealDetails.image,
//            mealType = mealDetails.mealType,
//            mealQuantity = mealDetails.mealQuantity,
//            unit = mealDetails.unit,
//            isRepeat = mealDetails.isRepeat,
//            isFavourite = mealDetails.isFavourite,
//            isLogged = true
//        )
        val call = ApiClient.apiServiceFastApi.createLogMeal(userId, mealLogRequest)
        call.enqueue(object : Callback<MealPlanResponse> {
            override fun onResponse(call: Call<MealPlanResponse>, response: Response<MealPlanResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val mealData = response.body()?.message
                    Toast.makeText(activity, mealData, Toast.LENGTH_SHORT).show()
                    val fragment = CreateMealFragment()
                    val args = Bundle()
                    args.putParcelable("snapDishLocalListModel", snapDishLocalListModel)
                    fragment.arguments = args
                    requireActivity().supportFragmentManager.beginTransaction().apply {
                        replace(R.id.flFragment, fragment, "mealLog")
                        addToBackStack("mealLog")
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
            override fun onFailure(call: Call<MealPlanResponse>, t: Throwable) {
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

    private fun createMealsSave(snapRecipeList : ArrayList<SearchResultItem>) {
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
        snapRecipeList?.forEach { snapRecipe ->
            val mealLogData = MealLog(
                receipe_id = snapRecipe.id,
             meal_quantity = 1,
             unit = "g",
             measure = "Bowl"
            )
            mealLogList.add(mealLogData)
        }
        val mealLogRequest = MealSaveRequest(
            meal_type = addedNameTv.text.toString(),
            meal_name = addedNameTv.text.toString(),
            meal_log = mealLogList
        )
        val call = ApiClient.apiServiceFastApi.createMealsSave(userId, mealLogRequest)
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
                    val fragment = HomeTabMealFragment()
                    val args = Bundle()
                    args.putString("mealType", mealType)
                    args.putString("tabType", "MyMeal")
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

    private fun updateMealsSave(snapRecipeList : ArrayList<SearchResultItem>) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val mealLogList : ArrayList<DishLog> = ArrayList()
        snapRecipeList?.forEach { snapRecipe ->
            val mealLogData = DishLog(
                receipe_id = snapRecipe.id,
                meal_quantity = snapRecipe.mealQuantity,
                unit = snapRecipe.unit,
                measure = "Bowl"
            )
            mealLogList.add(mealLogData)
        }
        val updateMealRequest = UpdateMealRequest(
            meal_name = addedNameTv.text.toString(),
            meal_log = mealLogList
        )
        val call = ApiClient.apiServiceFastApi.updateSaveMeal(mealId, userId, updateMealRequest)
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
                    val fragment = HomeTabMealFragment()
                    val args = Bundle()
                    args.putString("tabType", "MyMeal")
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

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }
}