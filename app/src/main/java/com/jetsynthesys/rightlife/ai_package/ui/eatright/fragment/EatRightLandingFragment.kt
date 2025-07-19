package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.animation.ValueAnimator
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.SnapHelper
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.ThinkRecomendedResponse
import com.jetsynthesys.rightlife.ai_package.model.request.DishLog
import com.jetsynthesys.rightlife.ai_package.model.request.SaveDishLogRequest
import com.jetsynthesys.rightlife.ai_package.model.request.WeightIntakeRequest
import com.jetsynthesys.rightlife.ai_package.model.response.EatRightLandingPageDataResponse
import com.jetsynthesys.rightlife.ai_package.model.response.LogWeightResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealLogDataResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MealUpdateResponse
import com.jetsynthesys.rightlife.ai_package.model.response.MergedLogsMealItem
import com.jetsynthesys.rightlife.ai_package.model.response.OtherRecipe
import com.jetsynthesys.rightlife.ai_package.model.response.RegularRecipeEntry
import com.jetsynthesys.rightlife.ai_package.model.response.SnapMeal
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.LogWeightRulerAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.MealSuggestionListAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.OtherRecipeEatLandingAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.TodayMealLogEatLandingAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourBreakfastMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourDinnerMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourEveningSnacksMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourLunchMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.adapter.YourMorningSnackMealLogsAdapter
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.macros.MacrosTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.microtab.MicrosTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.LandingPageResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.model.MyMealModel
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.adapter.RecommendedAdapterSleep
import com.jetsynthesys.rightlife.ai_package.utils.AppPreference
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse
import com.jetsynthesys.rightlife.apimodel.userdata.Userdata
import com.jetsynthesys.rightlife.databinding.BottomsheetLogWeightSelectionBinding
import com.jetsynthesys.rightlife.databinding.FragmentEatRightLandingBinding
import com.jetsynthesys.rightlife.ui.utility.ConversionUtils
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale
import kotlin.math.floor
import kotlin.math.round

class EatRightLandingFragment : BaseFragment<FragmentEatRightLandingBinding>(), SelectMealTypeEatLandingBottomSheet.OnOtherRecipeLogListener {

    private lateinit var todayMealLogsRecyclerView : RecyclerView
    private lateinit var loggedNextMealSuggestionRecyclerView : RecyclerView
    private lateinit var otherRecipeRecyclerView : RecyclerView
   // private lateinit var glassWithWaterView : GlassWithWaterView
    private lateinit var glassWithWaterView: ImageView
    private lateinit var todayMacrosWithDataLayout : ConstraintLayout
    private lateinit var todayMacroNoDataLayout : ConstraintLayout
    private lateinit var todayMicrosWithDataLayout : ConstraintLayout
    private lateinit var todayMacroNoDataLayoutOne : ConstraintLayout
    private lateinit var todayMealLogNoDataHeading : ConstraintLayout
    private lateinit var logNextMealSuggestionLayout: CardView
    private lateinit var waterQuantityTv : TextView
    private lateinit var lastLoggedNoData: TextView
    private lateinit var weightIntake: TextView
    private lateinit var thinkRecomendedResponse : ThinkRecomendedResponse
    private lateinit var recomendationAdapter: RecommendedAdapterSleep
    private lateinit var recomendationRecyclerView: RecyclerView
    private lateinit var weightIntakeUnit : TextView
    private lateinit var otherRecipeMightLikeWithData : LinearLayout
    private lateinit var newImprovementLayout : LinearLayout
    private  var newBoolean : Boolean = false
    private lateinit var appPreference : AppPreference
    private lateinit var tvProteinValue : TextView
    private lateinit var tvFatsValue : TextView
    private lateinit var tvCabsValue : TextView
    private lateinit var logYourWaterIntakeFilled : LinearLayout
    private lateinit var lossNewWeightFilled : LinearLayout
    private lateinit var tvCaloriesValue : TextView
    private lateinit var fatsProgressBar : ProgressBar
    private lateinit var proteinProgressBar : ProgressBar
    private lateinit var cabsProgressBar : ProgressBar
    private lateinit var imageBack : ImageView
    private lateinit var hydrationTrackerForwardImage : ImageView
    private lateinit var logWeightRulerAdapter : LogWeightRulerAdapter
    private val numbers = mutableListOf<Float>()
    private lateinit var macroIc : ImageView
    private lateinit var proteinUnitTv : TextView
    private lateinit var carbsUnitTv : TextView
    private lateinit var fatsUnitTv : TextView
    private lateinit var weightLastLogDateTv : TextView
    private lateinit var weightTrackerIc : ImageView
    private lateinit var logFirstMealLayout : LinearLayout
    private lateinit var logMealNoDataButton : ConstraintLayout
    private lateinit var snapMealNoData : ConstraintLayout
    private lateinit var microIc : ImageView
    private lateinit var microValueTv : TextView
    private lateinit var unitMicroTv : TextView
    private lateinit var energyTypeTv : TextView
    private lateinit var microsMessage : TextView
    private lateinit var recipesButton : ConstraintLayout
    private lateinit var yourMealsLogBtn : ImageView
    private lateinit var breakfastMealRecyclerView : RecyclerView
    private lateinit var morningSnackMealsRecyclerView : RecyclerView
    private lateinit var lunchMealRecyclerView : RecyclerView
    private lateinit var eveningSnacksMealRecyclerView : RecyclerView
    private lateinit var dinnerMealRecyclerView : RecyclerView
    private lateinit var landingPageResponse : EatRightLandingPageDataResponse
    private lateinit var breakfastListLayout : ConstraintLayout
    private lateinit var morningSnackListLayout : ConstraintLayout
    private lateinit var lunchListLayout : ConstraintLayout
    private lateinit var eveningSnacksListLayout : ConstraintLayout
    private lateinit var dinnerListLayout : ConstraintLayout
    private lateinit var greenTickIcon : ImageView
    private lateinit var macroTitle : TextView
    private lateinit var macroOnTrackTextLine : TextView
    private lateinit var eatRightInfo : ImageView
    private  var regularRecipesList : ArrayList<RegularRecipeEntry> = ArrayList()
    private val breakfastCombinedList = ArrayList<MergedLogsMealItem>()
    private val morningSnackCombinedList = ArrayList<MergedLogsMealItem>()
    private val lunchCombinedList = ArrayList<MergedLogsMealItem>()
    private val eveningSnacksCombinedList = ArrayList<MergedLogsMealItem>()
    private val dinnerCombinedList = ArrayList<MergedLogsMealItem>()
    private lateinit var halfCurveProgressBar : HalfCurveProgressBar
    private var loadingOverlay : FrameLayout? = null
    private var waterIntakeValue : Float = 0f

    private lateinit var userData: Userdata
    private lateinit var userDataResponse: UserProfileResponse
    private lateinit var sharedPreferenceManager: SharedPreferenceManager
    val viewModel: MasterCalculationsViewModel by viewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentEatRightLandingBinding
        get() = FragmentEatRightLandingBinding::inflate

    private val todayMealLogAdapter by lazy { TodayMealLogEatLandingAdapter(requireContext(), arrayListOf(), -1,
        null, false, ::onTodayMealLogItem) }
    private val otherRecipeAdapter by lazy { OtherRecipeEatLandingAdapter(requireContext(), arrayListOf(), -1,
        null, false, ::onOtherRecipeItem, :: onOtherRecipeLogItem) }
    private val mealSuggestionAdapter by lazy { MealSuggestionListAdapter(requireContext(), arrayListOf(),
        -1, null, false, ::onMealSuggestionItem) }
    private val breakfastMealLogsAdapter by lazy { YourBreakfastMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null, false, ::onBreakFastRegularRecipeDeleteItem,
        :: onBreakFastRegularRecipeEditItem, :: onBreakFastSnapMealDeleteItem, :: onBreakFastSnapMealEditItem, true) }
    private val morningSnackMealLogsAdapter by lazy { YourMorningSnackMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null,false, :: onMSRegularRecipeDeleteItem, :: onMSRegularRecipeEditItem,
        :: onMSSnapMealDeleteItem, :: onMSSnapMealEditItem, true) }
    private val lunchMealLogsAdapter by lazy { YourLunchMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null,false, :: onLunchRegularRecipeDeleteItem, :: onLunchRegularRecipeEditItem,
        :: onLunchSnapMealDeleteItem, :: onLunchSnapMealEditItem, true) }
    private val eveningSnacksMealLogsAdapter by lazy { YourEveningSnacksMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null,false, :: onESRegularRecipeDeleteItem, :: onESRegularRecipeEditItem,
        :: onESSnapMealDeleteItem, :: onESSnapMealEditItem, true) }
    private val dinnerMealLogsAdapter by lazy { YourDinnerMealLogsAdapter(requireContext(), arrayListOf(), -1,
        null, null,false, :: onDinnerRegularRecipeDeleteItem, :: onDinnerRegularRecipeEditItem,
        :: onDinnerSnapMealDeleteItem, :: onDinnerSnapMealEditItem, true) }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        newBoolean = true
        appPreference = AppPreference(requireContext())
        val bottomSeatName = arguments?.getString("BottomSeatName").toString()
        sharedPreferenceManager = SharedPreferenceManager.getInstance(context)

        userDataResponse = sharedPreferenceManager.userProfile
        userData = userDataResponse.userdata
        //viewModel.userResponse = userData

        halfCurveProgressBar = view.findViewById(R.id.halfCurveProgressBar)
        val snapMealBtn = view.findViewById<ConstraintLayout>(R.id.lyt_snap_meal)
        val mealLogLayout = view.findViewById<LinearLayout>(R.id.layout_meal_log)
        todayMealLogsRecyclerView = view.findViewById(R.id.todayMealLogsRecyclerView)
        todayMacrosWithDataLayout = view.findViewById(R.id.today_macros_with_data_layout)
        tvProteinValue = view.findViewById(R.id.tv_protien_value)
        hydrationTrackerForwardImage = view.findViewById(R.id.hydration_tracker_forward_image)
        tvFatsValue = view.findViewById(R.id.tv_fats_value)
        tvCabsValue = view.findViewById(R.id.tv_carbs_value)
        logYourWaterIntakeFilled = view.findViewById(R.id.log_your_water_intake_filled)
        weightTrackerIc = view.findViewById(R.id.weightTrackerIc)
        recomendationRecyclerView = view.findViewById(R.id.recommendationRecyclerView)
        fatsProgressBar = view.findViewById(R.id.fats_progressBar)
        proteinProgressBar = view.findViewById(R.id.protein_progressBar)
        cabsProgressBar = view.findViewById(R.id.carbs_progressBar)
        imageBack = view.findViewById(R.id.imageBack)
      //  tvCaloriesValue = view.findViewById(R.id.tvCaloriesValue)
        todayMacroNoDataLayout = view.findViewById(R.id.today_macro_no_data_layout)
        todayMicrosWithDataLayout = view.findViewById(R.id.today_micros_with_data_layout)
        todayMacroNoDataLayoutOne = view.findViewById(R.id.today_macro_no_data_layout_one)
        todayMealLogNoDataHeading = view.findViewById(R.id.today_meal_log_no_data)
        logNextMealSuggestionLayout = view.findViewById(R.id.logNextMealSuggestionLayout)
        otherRecipeMightLikeWithData = view.findViewById(R.id.other_reciepie_might_like_with_data)
        waterQuantityTv = view.findViewById(R.id.tv_water_quantity)
        weightIntake = view.findViewById(R.id.weightIntake)
        lastLoggedNoData = view.findViewById(R.id.last_logged_no_data)
        weightIntakeUnit = view.findViewById(R.id.weightIntakeUnit)
        newImprovementLayout = view.findViewById(R.id.new_improvement_layout)
        lossNewWeightFilled = view.findViewById(R.id.loss_new_weight_filled)
        macroIc = view.findViewById(R.id.macroIc)
        fatsUnitTv = view.findViewById(R.id.fatsUnitTv)
        proteinUnitTv = view.findViewById(R.id.proteinUnitTv)
        carbsUnitTv = view.findViewById(R.id.carbsUnitTv)
        logMealNoDataButton = view.findViewById(R.id.logMealNoDataButton)
        snapMealNoData = view.findViewById(R.id.lyt_snap_meal_no_data)
        microIc = view.findViewById(R.id.microIc)
        energyTypeTv = view.findViewById(R.id.energyTypeTv)
        microValueTv = view.findViewById(R.id.microValueTv)
        unitMicroTv = view.findViewById(R.id.unitMicroTv)
        microsMessage = view.findViewById(R.id.microsMessage)
        recipesButton = view.findViewById(R.id.recipes_button)
        yourMealsLogBtn = view.findViewById(R.id.yourMealsLogBtn)
        logFirstMealLayout = view.findViewById(R.id.logFirstMealLayout)
        weightLastLogDateTv = view.findViewById(R.id.weightLastLogDateTv)
        breakfastMealRecyclerView = view.findViewById(R.id.recyclerview_breakfast_meals_item)
        morningSnackMealsRecyclerView = view.findViewById(R.id.recyclerviewMorningSnackMealsItem)
        lunchMealRecyclerView = view.findViewById(R.id.recyclerview_lunch_meals_item)
        eveningSnacksMealRecyclerView = view.findViewById(R.id.recyclerview_eveningSnacks_meals_item)
        dinnerMealRecyclerView = view.findViewById(R.id.recyclerview_dinner_meals_item)
        breakfastListLayout = view.findViewById(R.id.layout_breakfast_list)
        morningSnackListLayout = view.findViewById(R.id.layoutMorningSnackList)
        lunchListLayout = view.findViewById(R.id.layout_lunch_list)
        eveningSnacksListLayout = view.findViewById(R.id.layout_eveningSnacks_list)
        dinnerListLayout = view.findViewById(R.id.layout_dinner_list)
        greenTickIcon = view.findViewById(R.id.green_tick_icon)
        macroOnTrackTextLine = view.findViewById(R.id.macroOnTrackTextLine)
        macroTitle = view.findViewById(R.id.macroTitle)
        eatRightInfo = view.findViewById(R.id.eatRightInfo)

        loggedNextMealSuggestionRecyclerView = view.findViewById(R.id.loggedNextMealSuggestionRecyclerView)
        otherRecipeRecyclerView = view.findViewById(R.id.recyclerview_other_reciepe_item)
        otherRecipeRecyclerView.layoutManager = LinearLayoutManager(context)
        otherRecipeRecyclerView.adapter = otherRecipeAdapter
        breakfastMealRecyclerView.layoutManager = LinearLayoutManager(context)
        breakfastMealRecyclerView.adapter = breakfastMealLogsAdapter
        morningSnackMealsRecyclerView.layoutManager = LinearLayoutManager(context)
        morningSnackMealsRecyclerView.adapter = morningSnackMealLogsAdapter
        lunchMealRecyclerView.layoutManager = LinearLayoutManager(context)
        lunchMealRecyclerView.adapter = lunchMealLogsAdapter
        eveningSnacksMealRecyclerView.layoutManager = LinearLayoutManager(context)
        eveningSnacksMealRecyclerView.adapter = eveningSnacksMealLogsAdapter
        dinnerMealRecyclerView.layoutManager = LinearLayoutManager(context)
        dinnerMealRecyclerView.adapter = dinnerMealLogsAdapter
        // onOtherReciepeDateItemRefresh()
        todayMealLogsRecyclerView.layoutManager = LinearLayoutManager(context)
        todayMealLogsRecyclerView.adapter = todayMealLogAdapter
        // onMealPlanItemRefresh()
        loggedNextMealSuggestionRecyclerView.layoutManager = LinearLayoutManager(context)
        loggedNextMealSuggestionRecyclerView.adapter = mealSuggestionAdapter
        // onFrequentlyLoggedItemRefresh()
        //  halfCurveProgressBar.setValues(2000,2000)
        glassWithWaterView = view.findViewById(R.id.glass_with_water_view)

        fetchEatRecommendedData()

        if (bottomSeatName.contentEquals("LogWeightEat")){
            showLogWeightBottomSheet()
        }else if (bottomSeatName.contentEquals("LogWaterIntakeEat")){
            showWaterIntakeBottomSheet()
        }
        lossNewWeightFilled.setOnClickListener {
            showLogWeightBottomSheet()
        }
        logYourWaterIntakeFilled.setOnClickListener {
            showWaterIntakeBottomSheet()
        }

        eatRightInfo.setOnClickListener {
            val mealSummaryInfoBottomSheet = MealSummaryInfoBottomSheet()
            mealSummaryInfoBottomSheet.isCancelable = true
            parentFragment.let { mealSummaryInfoBottomSheet.show(childFragmentManager, "MealSummaryInfoBottomSheet") }
        }

        getMealLandingSummary(halfCurveProgressBar)
        hydrationTrackerForwardImage.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = HydrationTrackerFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRight")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        weightTrackerIc.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = WeightTrackerFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRight")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        snapMealBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = SnapMealFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRight")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        imageBack.setOnClickListener {
            activity?.finish()
        }

        mealLogLayout.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = YourMealLogsFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRightLanding")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        logMealNoDataButton.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = YourMealLogsFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRightLanding")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        snapMealNoData.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = SnapMealFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRight")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        macroIc.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = MacrosTabFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRight")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        microIc.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = MicrosTabFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRight")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        recipesButton.setOnClickListener {
            val fragment = RecipesSearchFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }

        yourMealsLogBtn.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = YourMealLogsFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRightLandingWithoutPopup")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        otherRecipeMightLikeWithData.setOnClickListener {
            val fragment = RecipesSearchFragment()
            val args = Bundle()
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
//            val fragment = SearchDishToLogFragment()
//            val args = Bundle()
//            args.putString("searchType", "EatRight")
//            args.putParcelable("snapDishLocalListModel", null)
//            fragment.arguments = args
//            requireActivity().supportFragmentManager.beginTransaction().apply {
//                replace(R.id.flFragment, fragment, "landing")
//                addToBackStack("landing")
//                commit()
//            }
        }

        logFirstMealLayout.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val mealSearchFragment = YourMealLogsFragment()
                val args = Bundle()
                args.putString("ModuleName", "EatRightLanding")
                mealSearchFragment.arguments = args
                replace(R.id.flFragment, mealSearchFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }
    }

    private fun onMealSuggestionList(landingPageResponse : LandingPageResponse) {
        val meal = listOf(
            MyMealModel("Breakfast", landingPageResponse.next_meal_suggestion.meal_name, "1",
                landingPageResponse.next_meal_suggestion.calories.toString(),
                landingPageResponse.next_meal_suggestion.protein.toString(),
                landingPageResponse.next_meal_suggestion.carbs.toString(),
                landingPageResponse.next_meal_suggestion.fats.toString(),
                true),
        )
        if (meal.size > 0) {
            loggedNextMealSuggestionRecyclerView.visibility = View.VISIBLE
        } else {
            loggedNextMealSuggestionRecyclerView.visibility = View.GONE
        }
        val valueLists: ArrayList<MyMealModel> = ArrayList()
        valueLists.addAll(meal as Collection<MyMealModel>)
        val mealLogDateData: MyMealModel? = null
        mealSuggestionAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onMealSuggestionItem(mealLogDateModel: MyMealModel, position: Int, isRefresh: Boolean) {
    }

    private fun onTodayMealLogItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }

    private fun onOtherRecipeList(recipeSuggestion: List<OtherRecipe>) {

        if (recipeSuggestion.size > 0) {
            loggedNextMealSuggestionRecyclerView.visibility = View.VISIBLE
        } else {
            loggedNextMealSuggestionRecyclerView.visibility = View.GONE
        }
        val valueLists: ArrayList<OtherRecipe> = ArrayList()
        valueLists.addAll(recipeSuggestion as Collection<OtherRecipe>)
        val mealLogDateData: OtherRecipe? = null
        otherRecipeAdapter.addAll(valueLists, -1, mealLogDateData, false)
    }

    private fun onOtherRecipeItem(recipesModel: OtherRecipe, position: Int, isRefresh: Boolean) {

        requireActivity().supportFragmentManager.beginTransaction().apply {
            val snapMealFragment = RecipeDetailsFragment()
            val args = Bundle()
            args.putString("searchType", "EatRight")
            args.putString("mealType", "")
            args.putString("recipeId", recipesModel._id)
            snapMealFragment.arguments = args
            replace(R.id.flFragment, snapMealFragment, "Steps")
            addToBackStack(null)
            commit()
        }
    }

    private fun onOtherRecipeLogItem(recipesModel: OtherRecipe, position: Int, isRefresh: Boolean) {

        val selectMealTypeEatLandingBottomSheet = SelectMealTypeEatLandingBottomSheet()
        selectMealTypeEatLandingBottomSheet.isCancelable = true
        val bundle = Bundle()
        bundle.putString("recipeId", recipesModel._id)
        selectMealTypeEatLandingBottomSheet.arguments = bundle
        parentFragment.let { selectMealTypeEatLandingBottomSheet.show(childFragmentManager, "SelectMealTypeEatLandingBottomSheet") }
    }

    private fun getMealLandingSummary(halfCurveProgressBar: HalfCurveProgressBar) {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val call = ApiClient.apiServiceFastApi.getMealLandingSummary(userId, formattedDate)
        call.enqueue(object : Callback<EatRightLandingPageDataResponse> {
            override fun onResponse(call: Call<EatRightLandingPageDataResponse>, response: Response<EatRightLandingPageDataResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    landingPageResponse = response.body()!!
                    setMealSummaryData(landingPageResponse, halfCurveProgressBar)
                    getMealsLogList()
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
            override fun onFailure(call: Call<EatRightLandingPageDataResponse>, t: Throwable) {
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

    private fun fetchEatRecommendedData() {
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val call = ApiClient.apiService.fetchThinkRecomended(token,"HOME","EAT_RIGHT")
        call.enqueue(object : Callback<ThinkRecomendedResponse> {
            override fun onResponse(call: Call<ThinkRecomendedResponse>, response: Response<ThinkRecomendedResponse>) {
                if (response.isSuccessful) {
                    // progressDialog.dismiss()
                    thinkRecomendedResponse = response.body()!!
                    if (thinkRecomendedResponse.data?.contentList?.isNotEmpty() == true) {
                        recomendationAdapter = RecommendedAdapterSleep(context!!, thinkRecomendedResponse.data?.contentList!!)
                        recomendationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                        recomendationRecyclerView.adapter = recomendationAdapter
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    //          Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    // progressDialog.dismiss()F
                }
            }
            override fun onFailure(call: Call<ThinkRecomendedResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                //          Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                //progressDialog.dismiss()
            }
        })
    }

    private fun setMealSummaryData(landingPageResponse: EatRightLandingPageDataResponse, halfCurveProgressBar: HalfCurveProgressBar){

        if(landingPageResponse.total_calories.toInt() > 0){
            todayMacrosWithDataLayout.visibility = View.VISIBLE
            todayMacroNoDataLayout.visibility = View.GONE
            if (landingPageResponse.insight != null){
                macroTitle.text = landingPageResponse.insight.heading
                macroOnTrackTextLine.text = landingPageResponse.insight.macros_message
            }
            todayMicrosWithDataLayout.visibility = View.VISIBLE
            todayMacroNoDataLayoutOne.visibility = View.GONE
            microsMessage.text = landingPageResponse.micros.micros_message
            microValueTv.text = landingPageResponse.micros.value.toInt().toString()
            unitMicroTv.text = landingPageResponse.micros.unit
            energyTypeTv.text = landingPageResponse.micros.micros_name
        }else{
            todayMacrosWithDataLayout.visibility = View.GONE
            todayMacroNoDataLayout.visibility = View.VISIBLE
            todayMicrosWithDataLayout.visibility = View.GONE
            todayMacroNoDataLayoutOne.visibility = View.VISIBLE
        }

//        if(landingPageResponse.micros.value > 0){
//            todayMicrosWithDataLayout.visibility = View.VISIBLE
//            todayMacroNoDataLayoutOne.visibility = View.GONE
//            microsMessage.text = landingPageResponse.micros.micros_message
//            microValueTv.text = landingPageResponse.micros.value.toInt().toString()
//            unitMicroTv.text = landingPageResponse.micros.unit
//            energyTypeTv.text = landingPageResponse.micros.micros_name
//        }else{
//            todayMicrosWithDataLayout.visibility = View.GONE
//            todayMacroNoDataLayoutOne.visibility = View.VISIBLE
//        }

        if(landingPageResponse.other_recipes_you_might_like.size > 0){
            logNextMealSuggestionLayout.visibility = View.GONE
            otherRecipeMightLikeWithData.visibility = View.VISIBLE
            otherRecipeRecyclerView.visibility = View.VISIBLE
        }else{
            logNextMealSuggestionLayout.visibility = View.GONE
            otherRecipeMightLikeWithData.visibility = View.GONE
            otherRecipeRecyclerView.visibility = View.GONE
        }

        if(landingPageResponse.total_water_ml.toInt() > 0){
            waterQuantityTv.text = landingPageResponse.total_water_ml.toInt().toString()
            val waterIntake = landingPageResponse.total_water_ml.toFloat()
            val waterGoal = 5000f
            waterIntakeValue = waterIntake
//            glassWithWaterView.setTargetWaterLevel(waterIntake, waterGoal)
            val glassValue =   milestoneFor(waterIntakeValue.toInt())
            when (glassValue) {
                25 -> {
                    glassWithWaterView.setImageResource(R.drawable.glass_image_25)
                }
                50 -> {
                    glassWithWaterView.setImageResource(R.drawable.glass_image_50)
                }
                75 -> {
                    glassWithWaterView.setImageResource(R.drawable.glass_image_75)
                }
                100 -> {
                    glassWithWaterView.setImageResource(R.drawable.glass_image_100)
                }
                else -> {
                    glassWithWaterView.setImageResource(R.drawable.glass_image_0)
                }
            }
        }else{
            waterQuantityTv.text = "0"
        }

        if(landingPageResponse.last_weight_log != null){
            lastLoggedNoData.visibility = View.GONE
            weightIntake.visibility = View.VISIBLE
            weightIntakeUnit.visibility = View.VISIBLE
            newImprovementLayout.visibility = View.VISIBLE
            weightLastLogDateTv.visibility = View.VISIBLE
        }else{
            lastLoggedNoData.visibility = View.VISIBLE
            weightIntake.visibility = View.GONE
            weightIntakeUnit.visibility = View.GONE
            newImprovementLayout.visibility = View.GONE
            weightLastLogDateTv.visibility = View.GONE
        }

        weightIntakeUnit.text = landingPageResponse.last_weight_log?.type
        if (landingPageResponse.last_weight_log?.type.equals("lbs")){
            val selectedWeight = ConversionUtils.convertLbsToKgs(landingPageResponse.last_weight_log?.weight?.toFloat().toString())
            weightIntake.text = selectedWeight
        }else{
            weightIntake.text = landingPageResponse.last_weight_log?.weight?.toFloat().toString()
        }

        val convertedDate = convertDate(landingPageResponse.last_weight_log?.date.toString())
        weightLastLogDateTv.text = convertedDate

        if (landingPageResponse.total_protein.toInt() >  landingPageResponse.max_protein.toInt()) {
            tvProteinValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.macros_high_color))
        }else{
            tvProteinValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_no_meals))
        }

        if (landingPageResponse.total_carbs.toInt() >  landingPageResponse.max_carbs.toInt()) {
            tvCabsValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.macros_high_color))
        }else{
            tvCabsValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_no_meals))
        }

        if (landingPageResponse.total_fat.toInt() >  landingPageResponse.max_fat.toInt()) {
            tvFatsValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.macros_high_color))
        }else{
            tvFatsValue.setTextColor(ContextCompat.getColor(requireContext(), R.color.black_no_meals))
        }

      //  tvCaloriesValue.text = landingPageResponse.total_calories.toString()
        tvProteinValue.text = round(landingPageResponse.total_protein).toInt().toString()
        tvCabsValue.text = round(landingPageResponse.total_carbs).toInt().toString()
        tvFatsValue.text = round(landingPageResponse.total_fat).toInt().toString()
        carbsUnitTv.text = " / " + landingPageResponse.max_carbs.toInt().toString() +" g"
        proteinUnitTv.text = " / " + landingPageResponse.max_protein.toInt().toString() +" g"
        fatsUnitTv.text = " / " + landingPageResponse.max_fat.toInt().toString() +" g"

        SharedPreferenceManager.getInstance(requireActivity())
            .setMaxCalories(landingPageResponse.max_calories.toInt())

        SharedPreferenceManager.getInstance(requireActivity())
            .setMaxCarbs(landingPageResponse.max_carbs.toInt())

        SharedPreferenceManager.getInstance(requireActivity())
            .setMaxProtein(landingPageResponse.max_protein.toInt())

        SharedPreferenceManager.getInstance(requireActivity())
            .setMaxFats(landingPageResponse.max_fat.toInt())

        cabsProgressBar.max = landingPageResponse.max_carbs.toInt()
        cabsProgressBar.progress = landingPageResponse.total_carbs.toInt()
        proteinProgressBar.max = landingPageResponse.max_protein.toInt()
        proteinProgressBar.progress = landingPageResponse.total_protein.toInt()
        fatsProgressBar.max = landingPageResponse.max_fat.toInt()
        fatsProgressBar.progress = landingPageResponse.total_fat.toInt()

        halfCurveProgressBar.setValues(round(landingPageResponse.total_calories).toInt(),landingPageResponse.max_calories.toInt())
        halfCurveProgressBar.setProgress(100f)
        val animator = ValueAnimator.ofFloat(0f, 100f)
        animator.duration = 1000
        animator.addUpdateListener { animation ->
            val value = animation.animatedValue as Float
            halfCurveProgressBar.setProgress(value)
        }
        animator.start()
        onOtherRecipeList(landingPageResponse.other_recipes_you_might_like)
    }

    private fun setTodayMealLogsData(){

        val regularRecipeData : RegularRecipeEntry? = null
        val snapMealData : SnapMeal? = null

        if (breakfastCombinedList.size > 0){
            breakfastListLayout.visibility = View.VISIBLE
            breakfastMealLogsAdapter.updateList(breakfastCombinedList, -1, regularRecipeData, snapMealData, false)
        }else{
            breakfastListLayout.visibility = View.GONE
        }

        if (morningSnackCombinedList.size > 0){
            morningSnackListLayout.visibility = View.VISIBLE
            morningSnackMealLogsAdapter.addAll(morningSnackCombinedList, -1, regularRecipeData, snapMealData, false)
        }else{
            morningSnackListLayout.visibility = View.GONE
        }

        if (lunchCombinedList.size > 0){
            lunchListLayout.visibility = View.VISIBLE
            lunchMealLogsAdapter.addAll(lunchCombinedList, -1, regularRecipeData, snapMealData, false)
        }else{
            lunchListLayout.visibility = View.GONE
        }

        if (eveningSnacksCombinedList.size > 0){
            eveningSnacksListLayout.visibility = View.VISIBLE
            eveningSnacksMealLogsAdapter.addAll(eveningSnacksCombinedList, -1, regularRecipeData, snapMealData,false)
        }else{
            eveningSnacksListLayout.visibility = View.GONE
        }

        if  (dinnerCombinedList.size > 0){
            dinnerListLayout.visibility = View.VISIBLE
            dinnerMealLogsAdapter.addAll(dinnerCombinedList, -1, regularRecipeData, snapMealData, false)
        }else{
            dinnerListLayout.visibility = View.GONE
        }
    }

    fun convertDate(inputDate: String): String {
        return inputDate.substringBefore("T")
    }

    private fun onBreakFastRegularRecipeDeleteItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastRegularRecipeEditItem(mealItem: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastSnapMealDeleteItem(mealItem: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onBreakFastSnapMealEditItem(mealItem: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onMSRegularRecipeDeleteItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onMSRegularRecipeEditItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onMSSnapMealDeleteItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onMSSnapMealEditItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onLunchRegularRecipeDeleteItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onLunchRegularRecipeEditItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onLunchSnapMealDeleteItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onLunchSnapMealEditItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onESRegularRecipeDeleteItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onESRegularRecipeEditItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onESSnapMealDeleteItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onESSnapMealEditItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onDinnerRegularRecipeDeleteItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onDinnerRegularRecipeEditItem(mealLogDateModel: RegularRecipeEntry, position: Int, isRefresh: Boolean) {
    }
    private fun onDinnerSnapMealDeleteItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }
    private fun onDinnerSnapMealEditItem(mealLogDateModel: SnapMeal, position: Int, isRefresh: Boolean) {
    }

    private fun showLogWeightBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(requireActivity())
        var selectedLabel = " kg"
        var selectedWeight = weightIntake.text.toString() + " " +  weightIntakeUnit.text.toString()
        if (selectedWeight.isEmpty()) {
            selectedWeight = "50 kg"
        } else {
            val w = selectedWeight.split(" ")
            selectedLabel = " ${w[1]}"
        }
        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetLogWeightSelectionBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root
        bottomSheetDialog.setContentView(bottomSheetView)
        dialogBinding.selectedNumberText.text = selectedWeight

        val layoutManager =
            LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        dialogBinding.rulerView.layoutManager = layoutManager
        // Generate numbers with increments of 0.1

        for (i in 0..1000) {
            numbers.add(i / 10f) // Increment by 0.1
        }

        logWeightRulerAdapter = LogWeightRulerAdapter(numbers) { number ->
            // Handle the selected number
        }
        dialogBinding.rulerView.adapter = logWeightRulerAdapter

        if (selectedLabel == " kg"){
            dialogBinding.kgOption.setBackgroundResource(R.drawable.bg_weight_log_left_selected)
            dialogBinding.kgOption.setTextColor(Color.WHITE)
            dialogBinding.lbsOption.setBackgroundResource(R.drawable.bg_weight_log_right_unselected)
            dialogBinding.lbsOption.setTextColor(Color.BLACK)
            setKgsValue()
            dialogBinding.selectedNumberText.text = selectedWeight
        }else{
            dialogBinding.lbsOption.setBackgroundResource(R.drawable.bg_weight_log_right_selected)
            dialogBinding.lbsOption.setTextColor(Color.WHITE)
            dialogBinding.kgOption.setBackgroundResource(R.drawable.bg_weight_log_left_unselected)
            dialogBinding.kgOption.setTextColor(Color.BLACK)
            setLbsValue()
            dialogBinding.selectedNumberText.text = selectedWeight
        }

        dialogBinding.kgOption.setOnClickListener {
            dialogBinding.kgOption.setBackgroundResource(R.drawable.bg_weight_log_left_selected)
            dialogBinding.kgOption.setTextColor(Color.WHITE)
            dialogBinding.lbsOption.setBackgroundResource(R.drawable.bg_weight_log_right_unselected)
            dialogBinding.lbsOption.setTextColor(Color.BLACK)
            val w = selectedWeight.split(" ")
            selectedLabel = " kg"
            selectedWeight = ConversionUtils.convertKgToLbs(w[0])
            setKgsValue()
            dialogBinding.rulerView.layoutManager?.scrollToPosition(floor(selectedWeight.toDouble() * 10).toInt())
            selectedWeight += selectedLabel
            dialogBinding.selectedNumberText.text = selectedWeight
        }

        dialogBinding.lbsOption.setOnClickListener {
            dialogBinding.lbsOption.setBackgroundResource(R.drawable.bg_weight_log_right_selected)
            dialogBinding.lbsOption.setTextColor(Color.WHITE)
            dialogBinding.kgOption.setBackgroundResource(R.drawable.bg_weight_log_left_unselected)
            dialogBinding.kgOption.setTextColor(Color.BLACK)
            val w = selectedWeight.split(" ")
            selectedLabel = " lbs"
            selectedWeight = ConversionUtils.convertLbsToKgs(w[0])
            setLbsValue()
            dialogBinding.rulerView.layoutManager?.scrollToPosition(floor(selectedWeight.toDouble() * 10).toInt())
            selectedWeight += selectedLabel
            dialogBinding.selectedNumberText.text = selectedWeight
        }

        // Center number with snap alignment
        val snapHelper: SnapHelper = LinearSnapHelper()
        snapHelper.attachToRecyclerView(dialogBinding.rulerView)

        dialogBinding.rulerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)
                if (newState == RecyclerView.SCROLL_STATE_IDLE) {
                    // Get the currently snapped position
                    val snappedView = snapHelper.findSnapView(recyclerView.layoutManager)
                    if (snappedView != null) {
                        val position = recyclerView.layoutManager!!.getPosition(snappedView)
                        val snappedNumber = numbers[position]
                        //selected_number_text.setText("$snappedNumber Kg")
                        dialogBinding.selectedNumberText.text = "$snappedNumber $selectedLabel"
                        selectedWeight = dialogBinding.selectedNumberText.text.toString()
                    }
                }
            }
        })

        dialogBinding.rlRulerContainer.post {
            // Get the width of the parent LinearLayout
            val parentWidth: Int = dialogBinding.rlRulerContainer.width
            // Calculate horizontal padding (half of parent width)
            val paddingHorizontal = parentWidth / 2
            // Set horizontal padding programmatically
            dialogBinding.rulerView.setPadding(
                paddingHorizontal,
                dialogBinding.rulerView.paddingTop,
                paddingHorizontal,
                dialogBinding.rulerView.paddingBottom
            )
        }

        // Scroll to the center after layout is measured
        dialogBinding.rulerView.post {
            // Calculate the center position
            val itemCount =
                if (dialogBinding.rulerView.adapter != null) dialogBinding.rulerView.adapter!!.itemCount else 0
            val centerPosition = itemCount / 2
            // Scroll to the center position
            layoutManager.scrollToPositionWithOffset(centerPosition, 0)
        }

        dialogBinding.rulerView.post {
            dialogBinding.rulerView.layoutManager?.scrollToPosition(floor(selectedWeight.split(" ")[0].toDouble() * 10).toInt())
        }

        dialogBinding.btnConfirm.setOnClickListener {
           // bottomSheetDialog.dismiss()
            dialogBinding.rulerView.adapter = null
            val fullWeight = selectedWeight.trim()
            val parts = fullWeight.split(Regex("\\s+"))
            val weightValue = parts[0]   // "50.7"
            val weightUnit = parts.getOrElse(1) { "kg" }
            weightIntake.text = weightValue
            weightIntakeUnit.text = weightUnit
            lastLoggedNoData.visibility = View.GONE
            weightIntake.visibility = View.VISIBLE
            weightIntakeUnit.visibility = View.VISIBLE
            newImprovementLayout.visibility = View.VISIBLE
            weightLastLogDateTv.visibility = View.VISIBLE
            val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
            val currentDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
            val request = WeightIntakeRequest(
                userId = userId,
                source = "android",
                type = weightUnit,
                waterMl = weightValue.toFloat(),
                date = currentDate
            )
            val call = ApiClient.apiServiceFastApi.logWeightIntake(request)
            call.enqueue(object : Callback<LogWeightResponse> {
                override fun onResponse(
                    call: Call<LogWeightResponse>,
                    response: Response<LogWeightResponse>
                ) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                        bottomSheetDialog.dismiss()
                        dialogBinding.rulerView.adapter = null
                        val fullWeight = selectedWeight.trim()
                        val parts = fullWeight.split(Regex("\\s+"))
                        val weightValue = parts[0]   // "50.7"
                        val weightUnit = parts.getOrElse(1) { "kg" }
                        weightIntake.text = response.body()?.weight.toString()
                        weightIntakeUnit.text = weightUnit
                        Log.d("LogWaterAPI", "Success: $responseBody")
                        // You can do something with responseBody here
                    } else {
                        bottomSheetDialog.dismiss()
                        Log.e(
                            "LogWaterAPI",
                            "Error: ${response.code()} - ${response.errorBody()?.string()}"
                        )
                    }
                }
                override fun onFailure(call: Call<LogWeightResponse>, t: Throwable) {
                    Log.e("LogWaterAPI", "Failure: ${t.localizedMessage}")
                    bottomSheetDialog.dismiss()
                }
            })
         //   binding.tvWeight.text = selectedWeight
        }

        dialogBinding.closeIV.setOnClickListener {
            bottomSheetDialog.dismiss()
            dialogBinding.rulerView.adapter = null
        }
        bottomSheetDialog.show()
         fun logUserWaterIntake(
            userId: String,
            source: String,
            waterMl: Int,
            date: String
        ) {
        }
    }

    private fun setKgsValue() {
        numbers.clear()
        for (i in 0..1000) {
            numbers.add(i / 10f) // Increment by 0.1
        }
        logWeightRulerAdapter.notifyDataSetChanged()
    }

    private fun setLbsValue() {
        numbers.clear()
        for (i in 0..2204) {
            numbers.add(i / 10f)
        }
        logWeightRulerAdapter.notifyDataSetChanged()
    }

    private fun showWaterIntakeBottomSheet() {
        val waterIntakeBottomSheet = WaterIntakeBottomSheet()
        waterIntakeBottomSheet.isCancelable = false
        waterIntakeBottomSheet.listener = object : OnWaterIntakeConfirmedListener {
            override fun onWaterIntakeConfirmed(amount: Int) {
                // 👇 Use the amount here
                //Toast.makeText(requireContext(), "Water Intake: $amount ml", Toast.LENGTH_SHORT).show()
                waterQuantityTv.text = amount.toString()
                val waterIntake = amount.toFloat()
                val waterGoal = 5000f
                waterIntakeValue = waterIntake
                //  glassWithWaterView.setTargetWaterLevel(waterIntake, waterGoal)
                val glassValue =   milestoneFor(waterIntakeValue.toInt())
                when (glassValue) {
                    25 -> {
                        glassWithWaterView.setImageResource(R.drawable.glass_image_25)
                    }
                    50 -> {
                        glassWithWaterView.setImageResource(R.drawable.glass_image_50)
                    }
                    75 -> {
                        glassWithWaterView.setImageResource(R.drawable.glass_image_75)
                    }
                    100 -> {
                        glassWithWaterView.setImageResource(R.drawable.glass_image_100)
                    }
                    else -> {
                        glassWithWaterView.setImageResource(R.drawable.glass_image_0)
                    }
                }
            }
        }
        val args = Bundle()
        args.putInt("waterIntakeValue", waterIntakeValue.toInt())
        waterIntakeBottomSheet.arguments = args
        waterIntakeBottomSheet.show(parentFragmentManager, WaterIntakeBottomSheet.TAG)
    }

    private fun milestoneFor(value: Int): Int {
        val max = 5000
        val thresholds = listOf(
            0,                // 0%
            (max * 0.25).toInt(),  // 25%
            (max * 0.50).toInt(),  // 50%
            (max * 0.75).toInt(),  // 75%
            max               // 100%
        )
        return when {
            value <= thresholds[0] ->   0
            value <  thresholds[1] ->  25
            value <  thresholds[2] ->  50
            value <  thresholds[3] ->  75
            else                    -> 100
        }
    }

    private fun getMealsLogList() {
       // LoaderUtil.showLoader(requireView())
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val formattedDate = currentDateTime.format(formatter)
        val call = ApiClient.apiServiceFastApi.getMealsLogByDate(userId, formattedDate)
        call.enqueue(object : Callback<MealLogDataResponse> {
            override fun onResponse(call: Call<MealLogDataResponse>, response: Response<MealLogDataResponse>) {
                if (response.isSuccessful) {
               //     LoaderUtil.dismissLoader(requireView())
                    val breakfastRecipes = response.body()?.data!!.meal_detail["breakfast"]?.regular_receipes
                    val morningSnackRecipes = response.body()?.data!!.meal_detail["morning_snack"]?.regular_receipes
                    val lunchSnapRecipes = response.body()?.data!!.meal_detail["lunch"]?.regular_receipes
                    val eveningSnacksRecipes = response.body()?.data!!.meal_detail["evening_snack"]?.regular_receipes
                    val dinnerRecipes = response.body()?.data!!.meal_detail["dinner"]?.regular_receipes

                    val breakfastSnapMeals = response.body()?.data!!.meal_detail["breakfast"]?.snap_meals
                    val morningSnackSnapMeals = response.body()?.data!!.meal_detail["morning_snack"]?.snap_meals
                    val lunchSnapSnapMeals = response.body()?.data!!.meal_detail["lunch"]?.snap_meals
                    val eveningSnacksSnapMeals = response.body()?.data!!.meal_detail["evening_snack"]?.snap_meals
                    val dinnerSnapMeals = response.body()?.data!!.meal_detail["dinner"]?.snap_meals

                    if (breakfastRecipes != null){
                        breakfastCombinedList.addAll(breakfastRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                    }
                    if (breakfastSnapMeals != null){
                        breakfastCombinedList.addAll(breakfastSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                    }
                    if (morningSnackRecipes != null){
                        morningSnackCombinedList.addAll(morningSnackRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                    }
                    if (morningSnackSnapMeals != null){
                        morningSnackCombinedList.addAll(morningSnackSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                    }
                    if (lunchSnapRecipes != null){
                        lunchCombinedList.addAll(lunchSnapRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                    }
                    if (lunchSnapSnapMeals != null){
                        lunchCombinedList.addAll(lunchSnapSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                    }
                    if (eveningSnacksRecipes != null){
                        eveningSnacksCombinedList.addAll(eveningSnacksRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                    }
                    if (eveningSnacksSnapMeals != null){
                        eveningSnacksCombinedList.addAll(eveningSnacksSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                    }
                    if (dinnerRecipes != null) {
                        dinnerCombinedList.addAll(dinnerRecipes!!.map { MergedLogsMealItem.RegularRecipeList(it) })
                    }
                    if (dinnerSnapMeals != null){
                        dinnerCombinedList.addAll(dinnerSnapMeals!!.map { MergedLogsMealItem.SnapMealList(it) })
                    }

                    if (response.body()?.data != null) {
                        if (response.body()?.data!!.meal_detail.size > 0){
                            todayMealLogNoDataHeading.visibility = View.GONE
                            logFirstMealLayout.visibility = View.GONE
                            setTodayMealLogsData()
                        }else{
                            todayMealLogNoDataHeading.visibility = View.VISIBLE
                            logFirstMealLayout.visibility = View.VISIBLE
                        }
                    }else{
                        todayMealLogNoDataHeading.visibility = View.VISIBLE
                        logFirstMealLayout.visibility = View.VISIBLE
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, response.errorBody()?.string(), Toast.LENGTH_SHORT).show()
                   // LoaderUtil.dismissLoader(requireView())
                }
            }
            override fun onFailure(call: Call<MealLogDataResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
              //  LoaderUtil.dismissLoader(requireView())
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

    override fun onOtherRecipeLog(mealData: String) {
       // getMealLandingSummary(halfCurveProgressBar)
    }
}

class MasterCalculationsViewModel  : ViewModel() {

    private val _userResponse = MutableStateFlow<Userdata?>(null)
    val userResponse: StateFlow<Userdata?> = _userResponse
//    fun getUserDetails(callback: (Boolean, String?) -> Unit) {
//        viewModelScope.launch {
//            try {
//                val response = u // suspend function
//                _userResponse.value = response
//                callback(true, null)
//            } catch (e: Exception) {
//                val errorMessage = AppUtility.fetchErrorMessage(e)
//                callback(false, errorMessage ?: "Something went wrong.")
//            }
//        }
//    }

    fun calculateBMI(weight: Double, height: Double): Double {
        return weight / (height * height)
    }
}
