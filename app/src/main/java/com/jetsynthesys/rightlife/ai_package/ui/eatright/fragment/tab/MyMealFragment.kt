package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.tab.MyMealListAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.DeleteMealBottomSheet
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.YourMealLogsFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.createmeal.CreateMealFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.tab.frequentlylogged.LoggedBottomSheet
import com.jetsynthesys.rightlife.databinding.FragmentMyMealBinding
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.request.SnapDish
import com.jetsynthesys.rightlife.ai_package.model.request.SnapMealLogRequest
import com.jetsynthesys.rightlife.ai_package.model.response.IngredientRecipeDetails
import com.jetsynthesys.rightlife.ai_package.model.response.MealDetails
import com.jetsynthesys.rightlife.ai_package.model.response.MergedMealItem
import com.jetsynthesys.rightlife.ai_package.model.response.MyMealsSaveResponse
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMealDetail
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.MealScanResultFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MealLogItems
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.SelectedMealLogList
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.RecipeDetailsLocalListModel
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Instant
import java.time.format.DateTimeFormatter

class MyMealFragment : BaseFragment<FragmentMyMealBinding>(), DeleteMealBottomSheet.OnMealDeletedListener {

    private lateinit var myMealRecyclerView : RecyclerView
    private lateinit var layoutNoMeals : LinearLayoutCompat
    private lateinit var layoutBottomCreateMeal : LinearLayoutCompat
    private lateinit var layoutCreateMeal : LinearLayoutCompat
    private lateinit var loggedBottomSheetFragment : LoggedBottomSheet
    private lateinit var mealPlanTitleLayout : ConstraintLayout
    private lateinit var addLayout : LinearLayoutCompat
    private lateinit var mealType : String
    private val mergedList = mutableListOf<MergedMealItem>()
    private  var recipeDetailsLocalListModel : RecipeDetailsLocalListModel? = null
    private var loadingOverlay : FrameLayout? = null
    private var moduleName : String = ""
    private var selectedMealDate : String = ""
    private val sharedViewModel: SharedMealViewModel by activityViewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMyMealBinding
        get() = FragmentMyMealBinding::inflate

    private val myMealListAdapter by lazy { MyMealListAdapter(requireContext(), arrayListOf(), -1, null,null,
        false, :: onDeleteMealItem, :: onAddMealLogItem, :: onEditMealLogItem, :: onDeleteSnapMealItem,
        :: onAddSnapMealLogItem, :: onEditSnapMealLogItem) }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        myMealRecyclerView = view.findViewById(R.id.recyclerview_my_meals_item)
        layoutNoMeals = view.findViewById(R.id.layout_no_meals)
        layoutBottomCreateMeal = view.findViewById(R.id.layout_bottom_create_meal)
        layoutCreateMeal = view.findViewById(R.id.layout_create_meal)
        mealPlanTitleLayout = view.findViewById(R.id.layout_meal_plan_title)
        addLayout = view.findViewById(R.id.addLayout)

        moduleName = arguments?.getString("ModuleName").toString()
        mealType = arguments?.getString("mealType").toString()
        selectedMealDate = arguments?.getString("selectedMealDate").toString()

        myMealRecyclerView.layoutManager = LinearLayoutManager(context)
        myMealRecyclerView.adapter = myMealListAdapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
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

        getMyMealList()

        addLayout.setOnClickListener {
            val fragment = CreateMealFragment()
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

        layoutCreateMeal.setOnClickListener {
            val fragment = CreateMealFragment()
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

        layoutBottomCreateMeal.setOnClickListener {
            val fragment = CreateMealFragment()
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

    private fun myMealsList(mealData: MutableList<MergedMealItem>) {
        activity?.runOnUiThread {
            if (mealData.size > 0){
                myMealRecyclerView.visibility = View.VISIBLE
                layoutBottomCreateMeal.visibility = View.GONE
                mealPlanTitleLayout.visibility = View.VISIBLE
                layoutNoMeals.visibility = View.GONE
            }else{
                layoutNoMeals.visibility = View.VISIBLE
                myMealRecyclerView.visibility = View.GONE
                layoutBottomCreateMeal.visibility = View.GONE
                mealPlanTitleLayout.visibility = View.GONE
            }
            val valueLists : ArrayList<MergedMealItem> = ArrayList()
            valueLists.addAll(mealData as Collection<MergedMealItem>)
            val mealDetails: MealDetails? = null
            val snapMealDetail: SnapMealDetail? = null
            myMealListAdapter.addAll(valueLists, -1, mealDetails, snapMealDetail, false)

            val snapMealsLists = valueLists.filterIsInstance<MergedMealItem.SnapMeal>()
                .map { it.data }

            val savedMealsLists = valueLists.filterIsInstance<MergedMealItem.SavedMeal>()
                .map { it.data }

            sharedViewModel.mealLogData.observe(viewLifecycleOwner) { mealDataList ->
                if (savedMealsLists.isNotEmpty() && mealDataList.isNotEmpty()) {
                    val bothTypeLists : ArrayList<MergedMealItem> = ArrayList()
                    val mealLists : ArrayList<MealDetails> = ArrayList()
                 //   val snapMealLists : ArrayList<SnapMealDetail> = ArrayList()
                    for (item in savedMealsLists) {
                        // check if this item matches ANY meal in mealDataList
                        val matchFound = mealDataList.any { meal ->
                            item._id == meal._id && item.meal_name == meal.meal_name
                        }
                        if (matchFound) {
                            item.isMealLog = true
                        }else{
                            item.isMealLog = false
                        }
                        mealLists.add(item)
                    }
                    snapMealsLists.forEach { snap ->
                        bothTypeLists.add(MergedMealItem.SnapMeal(snap))
                    }
                    mealLists.forEach { saved ->
                        bothTypeLists.add(MergedMealItem.SavedMeal(saved))
                    }
                    myMealListAdapter.addAll(bothTypeLists, -1, null, null, false)
                }else if (savedMealsLists.isNotEmpty() && mealDataList.isEmpty()){
                    val bothTypeLists : ArrayList<MergedMealItem> = ArrayList()
                    val mealLists : ArrayList<MealDetails> = ArrayList()
                    for (item in savedMealsLists) {
                        item.isMealLog = false
                        mealLists.add(item)
                    }
                    snapMealsLists.forEach { snap ->
                        bothTypeLists.add(MergedMealItem.SnapMeal(snap))
                    }
                    mealLists.forEach { saved ->
                        bothTypeLists.add(MergedMealItem.SavedMeal(saved))
                    }
                    myMealListAdapter.addAll(bothTypeLists, -1, null, null, false)
                }
            }

            sharedViewModel.snapMealData.observe(viewLifecycleOwner) { snapMealDataList ->
                if (snapMealsLists.isNotEmpty() && snapMealDataList.isNotEmpty()) {
                    val bothTypeLists : ArrayList<MergedMealItem> = ArrayList()
                    val snapMealFilterLists : ArrayList<SnapMealDetail> = ArrayList()
                    for (item in snapMealsLists) {
                        val matchFound = snapMealDataList.any { meal ->
                            item._id == meal._id && item.meal_name == meal.meal_name
                        }
                        if (matchFound) {
                            item.isSnapMealLog = true
                        }else{
                            item.isSnapMealLog = false
                        }
                        snapMealFilterLists.add(item)
                    }
                    snapMealFilterLists.forEach { snap ->
                        bothTypeLists.add(MergedMealItem.SnapMeal(snap))
                    }
                    savedMealsLists.forEach { saved ->
                        bothTypeLists.add(MergedMealItem.SavedMeal(saved))
                    }
                    myMealListAdapter.addAll(bothTypeLists, -1, null, null, false)
                }else if (snapMealsLists.isNotEmpty() && snapMealDataList.isEmpty()){
                    val bothTypeLists : ArrayList<MergedMealItem> = ArrayList()
                    val snapMealFilterLists : ArrayList<SnapMealDetail> = ArrayList()
                    for (item in snapMealsLists) {
                        item.isSnapMealLog = false
                        snapMealFilterLists.add(item)
                    }
                    snapMealFilterLists.forEach { snap ->
                        bothTypeLists.add(MergedMealItem.SnapMeal(snap))
                    }
                    savedMealsLists.forEach { saved ->
                        bothTypeLists.add(MergedMealItem.SavedMeal(saved))
                    }
                    myMealListAdapter.addAll(bothTypeLists, -1, null, null, false)
                }
            }
        }
    }

    private fun onDeleteMealItem(mealDetails: MealDetails, position: Int, isRefresh: Boolean) {
        val deleteBottomSheetFragment = DeleteMealBottomSheet()
        deleteBottomSheetFragment.isCancelable = true
        val args = Bundle()
        args.putString("ModuleName", moduleName)
        args.putString("mealId",mealDetails._id)
        args.putString("mealName", mealDetails.meal_name)
        args.putString("deleteType", "MyMeal")
        args.putString("selectedMealDate", selectedMealDate)
        deleteBottomSheetFragment.arguments = args
        parentFragment.let { deleteBottomSheetFragment.show(childFragmentManager, "DeleteMealBottomSheet") }
    }

    private fun onAddMealLogItem(mealDetails: MealDetails, position: Int, isRefresh: Boolean) {
        val valueLists : ArrayList<MergedMealItem> = ArrayList()
        valueLists.addAll(mergedList as Collection<MergedMealItem>)
        val snapMealDetail: SnapMealDetail? = null
        myMealListAdapter.addAll(valueLists, position, mealDetails, snapMealDetail, isRefresh)
         val mealLogList : ArrayList<MealLogItems> = ArrayList()
        val dishList = mealDetails.receipe_data
        dishList?.forEach { selectedDish ->
            val mealLogData = MealLogItems(
                meal_id = selectedDish.id,
                recipe_name =  selectedDish.recipe.takeIf { r -> !r.isNullOrBlank() } ?: selectedDish.food_name,
                meal_quantity = selectedDish.selected_serving?.value,
                source = selectedDish.source,
                measure = selectedDish.selected_serving?.type,
                isMealLogSelect = mealDetails.isMealLog
            )
            mealLogList.add(mealLogData)
        }
        val mealLogRequest = SelectedMealLogList(
            meal_name =  mealDetails.meal_name,
            meal_type = mealDetails.meal_name,
            meal_log = mealLogList,
            isMealLog = mealDetails.isMealLog,
            _id = mealDetails._id
        )
        val parent = parentFragment as? HomeTabMealFragment
        parent?.setSelectedFrequentlyLog(null, false, mealLogRequest, null)
    }

    private fun onEditMealLogItem(mealDetails: MealDetails, position: Int, isRefresh: Boolean){
        if (mealDetails != null){
            val dishList = mealDetails.receipe_data
            val dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
            dishLists.addAll(dishList)
            recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            val fragment = CreateMealFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("mealType", mealType)
            args.putString("mealId", mealDetails._id)
            args.putString("mealName", mealDetails.meal_name)
            args.putString("selectedMealDate", selectedMealDate)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }
    }

    private fun onDeleteSnapMealItem(snapMealDetail: SnapMealDetail, position: Int, isRefresh: Boolean) {
        val deleteBottomSheetFragment = DeleteMealBottomSheet()
        deleteBottomSheetFragment.isCancelable = true
        val args = Bundle()
        args.putString("ModuleName", moduleName)
        args.putString("mealId",snapMealDetail._id)
        args.putString("deleteType", "MyMeal")
        args.putString("selectedMealDate", selectedMealDate)
        deleteBottomSheetFragment.arguments = args
        parentFragment.let { deleteBottomSheetFragment.show(childFragmentManager, "DeleteMealBottomSheet") }
    }

    private fun onAddSnapMealLogItem(snapMealDetail: SnapMealDetail, position: Int, isRefresh: Boolean) {
        val valueLists: ArrayList<MergedMealItem> = ArrayList()
        valueLists.addAll(mergedList as Collection<MergedMealItem>)
        val mealDetails: MealDetails? = null
        myMealListAdapter.addAll(valueLists, position, mealDetails, snapMealDetail, isRefresh)
        val mealLogData = MealLogItems(
            meal_id = snapMealDetail._id,
            recipe_name = snapMealDetail.meal_name,
            meal_quantity = 1.0,
            source = "snap",
            measure = "Bowl",
            isMealLogSelect = snapMealDetail.isSnapMealLog
        )
        val currentDateUtc: String = DateTimeFormatter.ISO_INSTANT.format(Instant.now())
        if (snapMealDetail.dish.isNotEmpty()) {
            val snapMealLogRequest = SnapMealLogRequest(
                user_id = snapMealDetail.user_id,
                meal_type = mealType,
                meal_name = snapMealDetail.meal_name,
                is_save = false,
                is_snapped = true,
                date = currentDateUtc,
                dish = snapMealDetail.dish,
                image_url = "",
                isSnapMealLogSelect = snapMealDetail.isSnapMealLog,
                _id = snapMealDetail._id
            )
            val parent = parentFragment as? HomeTabMealFragment
            parent?.setSelectedFrequentlyLog(mealLogData, true, null, snapMealLogRequest)
        }
    }

    private fun onEditSnapMealLogItem(snapMealDetail: SnapMealDetail, position: Int, isRefresh: Boolean){
        if (snapMealDetail != null){
            val dishList = snapMealDetail.dish
            val dishLists : ArrayList<IngredientRecipeDetails> = ArrayList()
            dishLists.addAll(dishList)
            recipeDetailsLocalListModel = RecipeDetailsLocalListModel(dishLists)
            val fragment = MealScanResultFragment()
            val args = Bundle()
            args.putString("ModuleName", moduleName)
            args.putString("selectedMealDate", selectedMealDate)
            args.putString("mealId", snapMealDetail._id)
            args.putString("snapMyMeal", "snapMyMeal")
            args.putString("mealName", snapMealDetail.meal_name)
            args.putString("snapImageUrl", snapMealDetail.image_url)
            args.putParcelable("snapDishLocalListModel", recipeDetailsLocalListModel)
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "mealLog")
                addToBackStack("mealLog")
                commit()
            }
        }
    }

     private fun getMyMealList() {
         if (isAdded  && view != null){
             requireActivity().runOnUiThread {
                 showLoader(requireView())
             }
         }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val call = ApiClient.apiServiceFastApiV2.getMyMealList(userId)
        call.enqueue(object : Callback<MyMealsSaveResponse> {
            override fun onResponse(call: Call<MyMealsSaveResponse>, response: Response<MyMealsSaveResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    if (response.body() != null){
                        val myMealsSaveList = response.body()!!.data
                        mergedList.clear()
                        myMealsSaveList.snap_meal_detail.forEach { snap ->
                            mergedList.add(MergedMealItem.SnapMeal(snap))
                        }
                        myMealsSaveList.meal_details.forEach { saved ->
                            mergedList.add(MergedMealItem.SavedMeal(saved))
                        }
                        myMealsList(mergedList)
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
            override fun onFailure(call: Call<MyMealsSaveResponse>, t: Throwable) {
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

    override fun onMealDeleted(deleted: String) {
        getMyMealList()
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