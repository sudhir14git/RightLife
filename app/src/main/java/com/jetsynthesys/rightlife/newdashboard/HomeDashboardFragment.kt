package com.jetsynthesys.rightlife.newdashboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.constraintlayout.widget.ConstraintSet
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseFragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.SleepSegmentModel
import com.jetsynthesys.rightlife.databinding.BottomsheetTrialEndedBinding
import com.jetsynthesys.rightlife.databinding.FragmentHomeDashboardBinding
import com.jetsynthesys.rightlife.newdashboard.model.AiDashboardResponseMain
import com.jetsynthesys.rightlife.newdashboard.model.ChecklistResponse
import com.jetsynthesys.rightlife.newdashboard.model.DashboardChecklistManager
import com.jetsynthesys.rightlife.newdashboard.model.DashboardChecklistResponse
import com.jetsynthesys.rightlife.newdashboard.model.DiscoverDataItem
import com.jetsynthesys.rightlife.newdashboard.model.SleepStage
import com.jetsynthesys.rightlife.newdashboard.model.UpdatedModule
import com.jetsynthesys.rightlife.runWhenAttached
import com.jetsynthesys.rightlife.subscriptions.SubscriptionPlanListActivity
import com.jetsynthesys.rightlife.ui.ActivityUtils
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.healthcam.NewHealthCamReportActivity
import com.jetsynthesys.rightlife.ui.new_design.OnboardingQuestionnaireActivity
import com.jetsynthesys.rightlife.ui.scan_history.PastReportActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import kotlin.math.roundToInt
import kotlin.math.roundToLong

class HomeDashboardFragment : BaseFragment() {
    private var _binding: FragmentHomeDashboardBinding? = null
    private val binding get() = _binding!!

    private val remData: ArrayList<Float> = arrayListOf()
    private val awakeData: ArrayList<Float> = arrayListOf()
    private val coreData: ArrayList<Float> = arrayListOf()
    private val deepData: ArrayList<Float> = arrayListOf()
    private val formatters = DateTimeFormatter.ISO_DATE_TIME

    private lateinit var healthConnectClient: HealthConnectClient
    private var checklistComplete = true
    private var checkListCount = 0
    private var snapMealId: String = ""

    private val allReadPermissions = setOf(
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(SpeedRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class)
    )

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false)
        AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.HOME_DASHBOARD_VISIT)
        return binding.root
    }

    override fun onResume() {
        super.onResume()
        fetchDashboardData()
        getDashboardChecklist()
        getDashboardChecklistStatus()
        getAiDashboard()
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        (requireActivity() as? HomeNewActivity)?.showHeader(true)
        fetchDashboardData()
        getAiDashboard()

        val swipeRefreshLayout = activity?.findViewById<SwipeRefreshLayout>(R.id.swipeRefreshLayout)

        swipeRefreshLayout?.setOnRefreshListener {
            // Call your refresh logic
            fetchDashboardData()
            getDashboardChecklist()
            getDashboardChecklistStatus()
            getAiDashboard()
            swipeRefreshLayout.isRefreshing = false // Stop the spinner
        }

        binding.includeChecklist.rlChecklistEatright.setOnClickListener {
            ActivityUtils.startEatRightQuestionnaireActivity(requireContext())
        }
        binding.includeChecklist.rlChecklistSleepright.setOnClickListener {
            ActivityUtils.startThinkRightQuestionnaireActivity(requireContext())
        }

        binding.rlViewPastReports.setOnClickListener {
            startActivity(Intent(requireContext(), PastReportActivity::class.java))
        }
        binding.includeChecklist.imgQuestionmarkChecklist.setOnClickListener {
            DialogUtils.showCheckListQuestionCommonDialog(requireContext(), "Why Checklist?")
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.WHY_CHECKLIST_CLICK)
            /*startActivity(Intent(this@HomeDashboardActivity, SubscriptionPlanListActivity::class.java).apply {
                //putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
                //putExtra("SUBSCRIPTION_TYPE", "FACIAL_SCAN")
            })*/
        }
        binding.includeChecklist.rlChecklistWhyThisDialog.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.FINISH_TO_UNLOCK_CLICK)
            DialogUtils.showCheckListQuestionCommonDialog(requireContext())
        }

        binding.includeChecklist.rlChecklistSynchealth.setOnClickListener {
            val availabilityStatus = HealthConnectClient.getSdkStatus(requireContext())
            if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
                healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
                lifecycleScope.launch {
                    requestPermissionsAndReadAllData()

                }
            } else {
                installHealthConnect(requireContext())
            }
        }
        binding.includeChecklist.rlChecklistProfile.setOnClickListener {
            val intent = Intent(requireContext(), OnboardingQuestionnaireActivity::class.java)
            intent.putExtra("forProfileChecklist", true)
            startActivity(intent)
        }
        binding.includeChecklist.rlChecklistSnapmeal.setOnClickListener {
            ActivityUtils.startMealScanActivity(requireContext(), snapMealId ?: "")
        }
        binding.includeChecklist.rlChecklistFacescan.setOnClickListener {
            val activity = requireActivity() as HomeNewActivity
            /*val isHealthCamFree = activity.isHealthCamFree*/

            var isHealthCamFree = sharedPreferenceManager.userProfile?.homeServices
                ?.find { it.title == "HealthCam" }
                ?.isFree ?: false

            val isFacialScanService = sharedPreferenceManager.userProfile.facialScanService ?: false

            if (isFacialScanService) {
                if (DashboardChecklistManager.facialScanStatus) {
                    startActivity(
                        Intent(
                            requireContext(), NewHealthCamReportActivity::class.java
                        )
                    )
                } else {
                    ActivityUtils.startFaceScanActivity(requireContext())
                }
            } else {
                activity.showSwitchAccountDialog(requireContext(), "", "")
            }
        }

        binding.cardThinkrightMain.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.THINK_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startThinkRightReportsActivity(requireContext(), "Not")
            }
        }
        binding.cardEatrightMain.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.EAT_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startEatRightReportsActivity(requireContext(), "Not")
            }
        }

        binding.cardMoverightMain.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.MOVE_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startMoveRightReportsActivity(requireContext(), "Not")
            }
        }
        binding.cardSleeprightMain.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.SLEEP_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startSleepRightReportsActivity(requireContext(), "Not")
            }
        }
        binding.cardSleepMainIdeal.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.SLEEP_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startSleepRightReportsActivity(requireContext(), "Not")
            }
        }
        binding.cardSleepMainLog.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.SLEEP_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startSleepRightReportsActivity(requireContext(), "Not")
            }
        }


        // for no data card
        binding.cardThinkrightMainNodata.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.THINK_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startThinkRightReportsActivity(requireContext(), "Not")
            }
        }
        binding.cardEatrightMainNodata.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.EAT_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startEatRightReportsActivity(requireContext(), "Not")
            }
        }

        binding.cardMoverightMainNodata.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.MOVE_RIGHT_CLICK)
                ActivityUtils.startMoveRightReportsActivity(requireContext(), "Not")
            }
        }
        binding.cardSleeprightMainNodata.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.SLEEP_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startSleepRightReportsActivity(requireContext(), "Not")
            }
        }

        binding.cardEatright.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.EAT_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startEatRightReportsActivity(requireContext(), "Not")
            }
        }
        binding.cardSleepright.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.SLEEP_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startSleepRightReportsActivity(requireContext(), "Not")
            }
        }
        binding.cardThinkright.setOnClickListener {
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.THINK_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startThinkRightReportsActivity(requireContext(), "Not")
            }
        }
        binding.cardMoveright.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.MOVE_RIGHT_CLICK)
                ActivityUtils.startMoveRightReportsActivity(requireContext(), "Not")
            }
        }

        val todayDate = SimpleDateFormat(
            "EEEE, d MMMM", Locale.getDefault()
        ).format(Calendar.getInstance().time)

        binding.tvDateDashboard.text = todayDate

        /*binding.trialExpiredLayout.trialExpiredLayout.visibility =
            if ((requireActivity() as? HomeNewActivity)?.isTrialExpired == true) View.VISIBLE else View.GONE*/

    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private suspend fun requestPermissionsAndReadAllData() {
        try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (allReadPermissions.all { it in granted }) {
//                fetchAllHealthData()
                CommonAPICall.updateChecklistStatus(
                    requireContext(), "sync_health_data", AppConstants.CHECKLIST_COMPLETED
                ) { status ->
                    if (status)
                        lifecycleScope.launch {
                            getDashboardChecklist()
                        }
                }

            } else {
                requestPermissionsLauncher.launch(allReadPermissions.toTypedArray())
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private val requestPermissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            if (permissions.values.all { it }) {
                lifecycleScope.launch {
                    CommonAPICall.updateChecklistStatus(
                        requireContext(), "sync_health_data", AppConstants.CHECKLIST_COMPLETED
                    ) { status ->
                        if (status)
                            lifecycleScope.launch {
                                getDashboardChecklist()
                            }
                    }
                }
                Toast.makeText(requireContext(), "Permissions Granted", Toast.LENGTH_SHORT).show()
            } else {
                Toast.makeText(requireContext(), "Permissions Denied", Toast.LENGTH_SHORT).show()
            }
        }

    //getDashboardChecklist
    private fun getDashboardChecklist() {
        // Make the API call
        val call = apiService.getDashboardChecklist(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    val promotionResponse2 = response.body()!!.string()
                    val gson = Gson()
                    val checklistResponse = gson.fromJson(
                        promotionResponse2, ChecklistResponse::class.java
                    )
                    runWhenAttached { handleChecklistResponse(checklistResponse) }
                    checkListCount = 0
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t) // Print the full stack trace for more details
            }
        })
    }

    private fun handleChecklistResponse(checklistResponse: ChecklistResponse?) {
        // profile
        setStatusOfChecklist(
            checklistResponse?.data?.profile!!,
            binding.includeChecklist.imgCheck,
            binding.includeChecklist.rlChecklistProfile
        )
        AnalyticsLogger.logEvent(
            requireContext(), AnalyticsEvent.PROFILE_STATUS,
            mapOf(AnalyticsParam.PROFILE_STATUS to checklistResponse.data.profile)
        )
        //snap Meal
        setStatusOfChecklist(
            checklistResponse.data.meal_snap,
            binding.includeChecklist.imgCheckSnapmeal,
            binding.includeChecklist.rlChecklistSnapmeal,
            false
        )
        AnalyticsLogger.logEvent(
            requireContext(), AnalyticsEvent.SNAP_MEAL_STATUS,
            mapOf(AnalyticsEvent.SNAP_MEAL_STATUS to checklistResponse.data.meal_snap)
        )
        //sync health
        setStatusOfChecklist(
            checklistResponse.data.sync_health_data,
            binding.includeChecklist.imgCheckSynchealth,
            binding.includeChecklist.rlChecklistSynchealth
        )
        AnalyticsLogger.logEvent(
            requireContext(), AnalyticsEvent.SYNC_DATA_STATUS,
            mapOf(AnalyticsEvent.SYNC_DATA_STATUS to checklistResponse.data.meal_snap)
        )
        // face Scan
        setStatusOfChecklist(
            checklistResponse.data.vital_facial_scan,
            binding.includeChecklist.imgCheckFacescan,
            binding.includeChecklist.rlChecklistFacescan,
            false
        )
        AnalyticsLogger.logEvent(
            requireContext(), AnalyticsEvent.FACIAL_SCAN_STATUS,
            mapOf(AnalyticsEvent.FACIAL_SCAN_STATUS to checklistResponse.data.vital_facial_scan)
        )
        // sleep right question
        setStatusOfChecklist(
            checklistResponse.data.unlock_sleep,
            binding.includeChecklist.imgCheckSleepright,
            binding.includeChecklist.rlChecklistSleepright
        )
        AnalyticsLogger.logEvent(
            requireContext(), AnalyticsEvent.TR_SR_ASSESSMENT_STATUS,
            mapOf(AnalyticsEvent.TR_SR_ASSESSMENT_STATUS to checklistResponse.data.meal_snap)
        )
        // Eat right question
        setStatusOfChecklist(
            checklistResponse.data.discover_eating,
            binding.includeChecklist.imgCheckEatright,
            binding.includeChecklist.rlChecklistEatright
        )
        AnalyticsLogger.logEvent(
            requireContext(), AnalyticsEvent.ER_MR_ASSESSMENT_STATUS,
            mapOf(AnalyticsEvent.ER_MR_ASSESSMENT_STATUS to checklistResponse.data.meal_snap)
        )
        binding.includeChecklist.tvChecklistNumber.text = "$checkListCount of 6 tasks completed"
        // Chceklist completion logic
        if (DashboardChecklistManager.checklistStatus) {
            binding.llDashboardMainData.visibility = View.VISIBLE
            binding.includeChecklist.llLayoutChecklist.visibility = View.GONE
            AnalyticsLogger.logEvent(
                requireContext(),
                AnalyticsEvent.CHECKLIST_COMPLETE,
                mapOf(AnalyticsParam.CHECKLIST_COMPLETE to true)
            )

            val activity = requireActivity() as HomeNewActivity
            activity.getUserDetails()
        } else {
            binding.llDashboardMainData.visibility = View.GONE
            binding.includeChecklist.llLayoutChecklist.visibility = View.VISIBLE
        }
        checklistResponse.data.snap_mealId.let { snapMealId ->
            sharedPreferenceManager.saveSnapMealId(snapMealId)
            this.snapMealId = snapMealId
        }
    }

    private fun setStatusOfChecklist(
        profile: String,
        imageView: ImageView,
        relativeLayout: RelativeLayout,
        disableclick: Boolean = true,
    ) {
        when (profile) {
            "INITIAL" -> {
                imageView.setImageResource(R.drawable.ic_checklist_tick_bg)
                checklistComplete = false
            }

            "INPROGRESS" -> {
                imageView.setImageResource(R.drawable.ic_checklist_pending)
                checklistComplete = false
            }

            "COMPLETED" -> {
                imageView.setImageResource(R.drawable.ic_checklist_complete)
                if (profile.equals("COMPLETED") && profile.equals("COMPLETED")) {

                }
                if (disableclick) {
                    relativeLayout.setOnClickListener(null)
                }
                checkListCount++
            }

            else -> {
                imageView.setImageResource(R.drawable.ic_checklist_tick_bg)
                checklistComplete = false
            }
        }
    }

    // New api Requested by backend team to be added in app need to discuss with them
    private fun fetchDashboardData() {
        val userId = sharedPreferenceManager.userId ?: return

        val date = DateTimeUtils.formatDateForOneApi()

        val call = apiServiceFastApi.getLandingDashboardData(userId, date, "android", true)
        call.enqueue(object : Callback<ResponseBody> {
            override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>) {
                if (response.isSuccessful && response.body() != null) {
                    val jsonString = response.body()!!.string()
                    try {
                        Log.d("DashboardRaw", jsonString)
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {
                    // showToast("Failed: ${response.message()}")
                }
            }

            override fun onFailure(call: Call<ResponseBody>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    // get AI Dashboard Data
    private fun getAiDashboard() {
        // Make the API call
        val date = DateTimeUtils.formatDateForOneApi()
        val call = apiService.getAiDashboard(sharedPreferenceManager.accessToken, date)
        call.enqueue(object : Callback<ResponseBody?> {
            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {
                if (response.isSuccessful && response.body() != null) {
                    val promotionResponse2 = response.body()!!.string()

                    val gson = Gson()

                    val aiDashboardResponseMain = gson.fromJson(
                        promotionResponse2, AiDashboardResponseMain::class.java
                    )
                    runWhenAttached {
                        handleSelectedModule(aiDashboardResponseMain)
                        handleDescoverList(aiDashboardResponseMain)
                        binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                        binding.recyclerView.adapter =
                            HeartRateAdapter(
                                aiDashboardResponseMain.data?.facialScan,
                                requireContext()
                            )
                    }
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun getDashboardChecklistStatus() {
        apiService.getdashboardChecklistStatus(sharedPreferenceManager.accessToken)
            .enqueue(object : Callback<DashboardChecklistResponse> {
                override fun onResponse(
                    call: Call<DashboardChecklistResponse>,
                    response: Response<DashboardChecklistResponse>
                ) {
                    if (response.isSuccessful && response.body() != null) {
                        response.body()?.data?.let {
                            DashboardChecklistManager.updateFrom(it)
                        }
                        runWhenAttached {
                            if (DashboardChecklistManager.isDataLoaded) {
                                // Chceklist completion logic
                                if (DashboardChecklistManager.checklistStatus) {
                                    binding.llDashboardMainData.visibility = View.VISIBLE
                                    binding.includeChecklist.llLayoutChecklist.visibility =
                                        View.GONE
                                    binding.llDiscoverLayout.visibility = View.GONE
                                    ActivityUtils.startChecklistCompleteActivity(requireContext())
                                } else {
                                    binding.llDashboardMainData.visibility = View.GONE
                                    binding.includeChecklist.llLayoutChecklist.visibility =
                                        View.VISIBLE
                                    binding.llDiscoverLayout.visibility = View.VISIBLE
                                }
                                (requireActivity() as? HomeNewActivity)?.showSubsribeLayout(
                                    DashboardChecklistManager.paymentStatus
                                )
                            } else {
                                Toast.makeText(
                                    requireContext(),
                                    "Server Error: " + response.code(),
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                    }
                }

                override fun onFailure(call: Call<DashboardChecklistResponse>, t: Throwable) {
                    handleNoInternetView(t)
                }

            })
    }

    private fun handleSelectedModule(aiDashboardResponseMain: AiDashboardResponseMain?) {
        val modules = aiDashboardResponseMain?.data?.updatedModules

        binding.cardMoverightMain.visibility = View.GONE
        binding.cardSleeprightMain.visibility = View.GONE
        binding.cardSleepMainIdeal.visibility = View.GONE
        binding.cardSleepMainLog.visibility = View.GONE

        binding.cardEatrightMain.visibility = View.GONE
        binding.cardThinkrightMain.visibility = View.GONE

        if (modules.isNullOrEmpty()) {
            binding.llNodataMain.visibility = View.VISIBLE
            var moduleId = sharedPreferenceManager.selectedOnboardingModule
            sharedPreferenceManager.selectedOnboardingModule = moduleId
            if (moduleId.isEmpty()) {
                moduleId = "EAT_RIGHT"
            }
            when (moduleId) {
                "MOVE_RIGHT" -> {
                    // MOVE_RIGHT logic here
                    binding.cardMoverightMainNodata.visibility = View.VISIBLE
                    binding.cardMoveright.visibility = View.GONE
                    binding.cardEatright.visibility = View.VISIBLE
                    binding.cardThinkright.visibility = View.VISIBLE
                    binding.cardSleepright.visibility = View.VISIBLE
                }

                "THINK_RIGHT" -> {
                    // THINK_RIGHT logic here
                    binding.cardThinkrightMainNodata.visibility = View.VISIBLE
                    binding.cardThinkright.visibility = View.GONE
                    binding.cardMoveright.visibility = View.VISIBLE
                    binding.cardEatright.visibility = View.VISIBLE
                    binding.cardSleepright.visibility = View.VISIBLE
                }

                "EAT_RIGHT" -> {
                    // EAT_RIGHT logic here
                    binding.cardEatrightMainNodata.visibility = View.VISIBLE
                    binding.cardEatright.visibility = View.GONE
                    binding.cardThinkright.visibility = View.VISIBLE
                    binding.cardMoveright.visibility = View.VISIBLE
                    binding.cardSleepright.visibility = View.VISIBLE
                }

                "SLEEP_RIGHT" -> {
                    // SLEEP_RIGHT logic here
                    binding.cardSleeprightMainNodata.visibility = View.VISIBLE
                    binding.cardSleepright.visibility = View.GONE
                    binding.cardEatright.visibility = View.VISIBLE
                    binding.cardThinkright.visibility = View.VISIBLE
                    binding.cardMoveright.visibility = View.VISIBLE
                }

                else -> {
                    // Default case logic here
                }
            }

        } else {
            binding.llNodataMain.visibility = View.GONE
            binding.cardThinkright.visibility = View.VISIBLE
            binding.cardMoveright.visibility = View.VISIBLE
            binding.cardEatright.visibility = View.VISIBLE
            binding.cardSleepright.visibility = View.VISIBLE


            //for (module in aiDashboardResponseMain?.data?.updatedModules!!) {
            aiDashboardResponseMain.data.updatedModules.forEach { module ->
                val moduleId = module.moduleId
                val isSelected = module.isSelectedModule
                if (isSelected == true)
                    sharedPreferenceManager.selectedWellnessFocus = moduleId

                when (moduleId) {
                    "MOVE_RIGHT" -> {

                        if (isSelected == true) {
                            binding.cardMoverightMain.visibility = View.VISIBLE
                            binding.cardMoveright.visibility = View.GONE
                            binding.cardEatright.visibility = View.VISIBLE
                            binding.cardThinkright.visibility = View.VISIBLE
                            binding.cardSleepright.visibility = View.VISIBLE
                        }
                        //set data on card once response works
                        binding.tvCaloryValue.text = module.calorieBalance.toString()
                        binding.tvCaloryIntake.text = module.intake.toString()
                        binding.tvCaloryBurn.text = module.burned.toString()

                        setIfNotNullOrBlankWithCalories(
                            binding.tvModuleValueEatright,
                            module.calories?.toString()
                        )
                        setIfNotNullOrBlank(
                            binding.tvModuleValueSleepright,
                            module.sleepPerformanceDetail?.actualSleepData?.actualSleepDurationHours?.let {
                                DateTimeUtils.formatSleepDuration(it)
                            } ?: "0 hr 0 min"
                        )


                        setIfNotNullOrBlank(
                            binding.tvModuleValueThinkright, module.mindfulTime?.toString()
                        )
                        setIfNotNullOrBlankWithCalories(
                            binding.tvModuleValueMoveright, module.activeBurn?.toString()
                        )

                        setProgressBarMoveright(module)

                    }

                    "THINK_RIGHT" -> {
                        if (isSelected == true) {
                            binding.cardThinkrightMain.visibility = View.VISIBLE
                            binding.cardThinkright.visibility = View.GONE
                            binding.cardMoveright.visibility = View.VISIBLE
                            binding.cardEatright.visibility = View.VISIBLE
                            binding.cardSleepright.visibility = View.VISIBLE
                        }
                        binding.tvMinutesTextValue.text = module.mindfulnessMinutes
                        binding.tvDaysTextValue.text = module.wellnessDays

                        setIfNotNullOrBlankWithCalories(
                            binding.tvModuleValueEatright,
                            module.calories?.toString()
                        )
                        setIfNotNullOrBlank(
                            binding.tvModuleValueSleepright,
                            module.sleepPerformanceDetail?.actualSleepData?.actualSleepDurationHours?.let {
                                DateTimeUtils.formatSleepDuration(it)
                            } ?: "0 hr 0 min"

                        )
                        setIfNotNullOrBlank(
                            binding.tvModuleValueThinkright, module.mindfulTime?.toString()
                        )
                        setIfNotNullOrBlankWithCalories(
                            binding.tvModuleValueMoveright, module.activeBurn?.toString()
                        )
                    }

                    "EAT_RIGHT" -> {
                        if (isSelected == true) {
                            binding.cardEatrightMain.visibility = View.VISIBLE
                            binding.cardEatright.visibility = View.GONE
                            binding.cardThinkright.visibility = View.VISIBLE
                            binding.cardMoveright.visibility = View.VISIBLE
                            binding.cardSleepright.visibility = View.VISIBLE
                        }

                        val (proteinValue, proteinTotal) = extractNumericValues(module.protein.toString())
                        val (carbsValue, carbsTotal) = extractNumericValues(module.carbs.toString())
                        val (fatsValue, fatsTotal) = extractNumericValues(module.fats.toString())

                        binding.tvProtienValue.text =  formatValue(proteinValue.toDouble())// "" + proteinValue.toDouble().roundToLong().toInt()
                        binding.proteinUnitTv.text = "/" + proteinTotal.toDoubleOrNull()?.roundToInt()?.toString() +"g" ?: "0" + " g"
                        binding.proteinProgressBar.max = proteinTotal.toDouble().toInt()
                        binding.proteinProgressBar.progress = proteinValue.toDouble().toInt()


                        binding.tvCarbsValue.text = formatValue(carbsValue.toDouble())//"" + carbsValue.toDouble().roundToLong().toInt()
                        binding.carbsUnitTv.text = "/" + carbsTotal.toDoubleOrNull()?.roundToInt()?.toString() +"g" ?: "0" + " g"
                        binding.carbsProgressBar.max = carbsTotal.toDouble().toInt()
                        binding.carbsProgressBar.progress = carbsValue.toDouble().toInt()

                        binding.tvFatsValue.text = formatValue(fatsValue.toDouble())//"" + fatsValue.toDouble().roundToLong().toInt()
                        binding.fatsUnitTv.text = "/" + fatsTotal.toDoubleOrNull()?.roundToInt()?.toString() +"g" ?: "0" + " g"
                        binding.fatsProgressBar.max = fatsTotal.toDouble().toInt()
                        binding.fatsProgressBar.progress = fatsValue.toDouble().toInt()


                        binding.halfCurveProgressBar.setProgress(0f)
                        binding.halfCurveProgressBar.setValues(0, 0)
                        // value is wrong for eatright progress let backend correct then uncomment below
                        /*val (curent, max) = extractNumericValues(module.calories.toString())
                            binding.halfCurveProgressBar.setValues(curent.toInt(), max.toInt())
                            val percentage = calculatePercentage(curent.toInt(), max.toInt())
                            binding.halfCurveProgressBar.setProgress(percentage.toFloat())*/
                        val (curentStr, maxStr) = extractNumericValues(module.calories.toString())

                        val current = curentStr.toDoubleOrNull()?.toInt() ?: 0
                        val max = maxStr.toDoubleOrNull()?.toInt() ?: 0

                        binding.halfCurveProgressBar.setValues(current, max)

                        val percentage = calculatePercentage(current, max)
                        binding.halfCurveProgressBar.setProgress(percentage.toFloat())


                        setIfNotNullOrBlankWithCalories(
                            binding.tvModuleValueEatright,
                            module.calories?.toString()
                        )
                        setIfNotNullOrBlank(
                            binding.tvModuleValueSleepright,
                            module.sleepPerformanceDetail?.actualSleepData?.actualSleepDurationHours?.let {
                                DateTimeUtils.formatSleepDuration(it)
                            } ?: "0 hr 0 min"
                        )
                        setIfNotNullOrBlank(
                            binding.tvModuleValueThinkright, module.mindfulTime?.toString()
                        )
                        setIfNotNullOrBlankWithCalories(
                            binding.tvModuleValueMoveright, module.activeBurn?.toString()
                        )
                    }

                    "SLEEP_RIGHT" -> {
                        if (isSelected == true) {
                            binding.cardSleeprightMain.visibility = View.VISIBLE
                            binding.cardSleepright.visibility = View.GONE
                            binding.cardEatright.visibility = View.VISIBLE
                            binding.cardThinkright.visibility = View.VISIBLE
                            binding.cardMoveright.visibility = View.VISIBLE
                            checkTimeAndSetVisibility(module)
                        }


                        setIfNotNullOrBlankWithCalories(
                            binding.tvModuleValueEatright,
                            module.calories?.toString()
                        )
                        setIfNotNullOrBlank(
                            binding.tvModuleValueSleepright,
                            module.sleepPerformanceDetail?.actualSleepData?.actualSleepDurationHours?.let {
                                DateTimeUtils.formatSleepDuration(it)
                            } ?: "0 hr 0 min"
                        )
                        setIfNotNullOrBlank(
                            binding.tvModuleValueThinkright, module.mindfulTime?.toString()
                        )
                        setIfNotNullOrBlankWithCalories(
                            binding.tvModuleValueMoveright, module.activeBurn?.toString()
                        )

                        aiDashboardResponseMain.data.updatedModules

                        val sleepModule =
                            aiDashboardResponseMain.data.updatedModules.find { it.moduleId == "SLEEP_RIGHT" }
                        sleepModule?.let {
                            setStageGraphFromSleepRightModule(
                                rem = (it.rem ?: "0min").toString(),
                                core = (it.core ?: "0min").toString(),
                                deep = (it.deep ?: "0min").toString(),
                                awake = (it.awake ?: "0min").toString()
                            )
                        }
                    }

                    else -> {
                        binding.cardMoverightMain.visibility = View.VISIBLE
                        binding.cardMoveright.visibility = View.GONE
                    }
                }


            }
        }
    }

    private fun setIfNotNullOrBlank(textView: TextView, value: String?) {
        if (!value.isNullOrBlank()) {
            textView.text = value
        }
    }

    private fun setIfNotNullOrBlankWithCalories(textView: TextView, value: String?) {
        if (!value.isNullOrBlank()) {
            val formattedValue = if (value.contains("kcal", ignoreCase = true)) {
                value
            } else {
                "$value Kcal"
            }
            textView.text = formattedValue
        }
    }

    private fun handleDescoverList(aiDashboardResponseMain: AiDashboardResponseMain?) {
        if (aiDashboardResponseMain?.data?.discoverData != null) {
            setHealthNoDataCardAdapter(aiDashboardResponseMain.data.discoverData)
        } else {
            binding.llDiscoverLayout.visibility = View.GONE
        }
    }

    private fun setStageGraphFromSleepRightModule(
        rem: String, core: String, deep: String, awake: String
    ) {
        val sleepData: ArrayList<SleepSegmentModel> = arrayListOf()
        val durations = mapOf(
            "REM" to parseSleepDuration(rem),
            "Core" to parseSleepDuration(core),
            "Deep" to parseSleepDuration(deep),
            "Awake" to parseSleepDuration(awake)
        )

        val totalDuration = durations.values.sum()
        var currentPosition = 0f

        durations.forEach { (stage, duration) ->
            val start = currentPosition / totalDuration
            val end = (currentPosition + duration) / totalDuration
            val color = when (stage) {
                "REM" -> Color.parseColor("#63D4FE")
                "Deep" -> Color.parseColor("#5E5CE6")
                "Core" -> Color.parseColor("#0B84FF")
                "Awake" -> Color.parseColor("#FF6650")
                else -> Color.GRAY
            }
            sleepData.add(SleepSegmentModel(start, end, color, 110f))
            currentPosition += duration
        }
    }

    private fun parseSleepDuration(durationStr: String): Float {
        val hourRegex = Regex("(\\d+)h")
        val minRegex = Regex("(\\d+)min")

        val hours = hourRegex.find(durationStr)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
        val minutes = minRegex.find(durationStr)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f

        return hours * 60 + minutes
    }

    private fun setHealthNoDataCardAdapter(discoverData: List<DiscoverDataItem>?) {
        binding.healthCardRecyclerView.apply {
            layoutManager =
                LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = HealthCardAdapter(discoverData)
            isNestedScrollingEnabled = false // 👈 important for smooth scroll
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        if (discoverData.isNullOrEmpty()) {
            binding.llDiscoverLayout.visibility = View.GONE
        } else {
            binding.llDiscoverLayout.visibility = View.VISIBLE
        }
    }

    private fun setProgressBarMoveright(module: UpdatedModule) {
        val intakeStr = module.intake ?: "0.0"
        val burnedStr = module.burned ?: "0.0"

        val intake = intakeStr.toFloatOrNull() ?: 0f
        val burned = burnedStr.toFloatOrNull() ?: 0f

        binding.progressBarOnboarding.viewTreeObserver.addOnGlobalLayoutListener(object :
            ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                binding.progressBarOnboarding.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val progressBarWidth = binding.progressBarOnboarding.width.toFloat()

                val maxValue = (intake + burned).toInt()
                val progressValue = intake.toInt()

                binding.progressBarOnboarding.max = maxValue
                binding.progressBarOnboarding.progress = progressValue

                val calorieBurnTarget = module.burned?.toDouble() ?: 0.0

                val rangeStart = module.calorieRange?.getOrNull(0)?.toDouble()
                val rangeEnd = module.calorieRange?.getOrNull(1)?.toDouble()

                val percentage =
                    if (rangeStart != null && rangeEnd != null && rangeEnd != rangeStart) {
                        ((calorieBurnTarget - rangeStart) / (rangeEnd - rangeStart)).toFloat()
                    } else {
                        0f
                    }


                //val percentage = binding.progressBarOnboarding.progress / binding.progressBarOnboarding.max.toFloat()
                val value = (percentage / 10)
                val overlayPositionPercentage: Float = String.format("%.1f", value).toFloat()

                val progress = binding.progressBarOnboarding.progress
                val max = binding.progressBarOnboarding.max
                val progressPercentage = progress.toFloat() / max

                val constraintSet = ConstraintSet()
                constraintSet.clone(binding.progressBarLayout)
                constraintSet.setGuidelinePercent(R.id.circleIndicatorGuideline, progressPercentage)
                constraintSet.setGuidelinePercent(R.id.overlayGuideline, overlayPositionPercentage)
                constraintSet.applyTo(binding.progressBarLayout)

                binding.transparentOverlay.let { overlay ->
                    // Use the progressBarLayout width to calculate proportional widths
                    val parentWidth = binding.progressBarOnboarding.width
                    var isWeightGainZone = false
                    if (rangeStart != null) {
                        val isWeightGainZone = calorieBurnTarget < rangeStart
                    }
                    val overlayWidth = if (isWeightGainZone) {
                        binding.weightLossZoneText.text = "Weight Gain Zone"
                        (parentWidth * 0.6).toInt() // 40% of parent width for Weight Gain Zone
                    } else {
                        binding.weightLossZoneText.text = "Weight Loss Zone"
                        (parentWidth * 0.08).toInt() // 20% of parent width for Weight Loss Zone
                    }
                    // Update the layout params to set the new width
                    val layoutParams = overlay.layoutParams
                    layoutParams.width = overlayWidth
                    overlay.layoutParams = layoutParams
                    overlay.visibility = View.VISIBLE // Ensure overlay is visible
                }
            }
        })

        val balance = module.calorieBalance?.toString()?.toFloatOrNull()?.toInt() ?: 0
        val upperRange =
            module.calorieRange?.getOrNull(1)?.toString()?.toFloatOrNull()?.toInt() ?: 0

        val color = when (module.goal) {
            "weight_loss" -> {
                binding.weightLossZoneText.text = "Weight Loss Zone"
                if (intake < upperRange) R.color.color_eat_right else R.color.red
            }

            "weight_gain" -> {
                binding.weightLossZoneText.text = "Weight Gain Zone"
                if (intake < upperRange) R.color.red else R.color.color_eat_right
            }

            else -> {
                binding.weightLossZoneText.text = "Weight Loss Zone"
                R.color.color_eat_right
            }
        }

        binding.tvCaloryValue.setTextColor(ContextCompat.getColor(requireContext(), color))
    }

    // new sleep time implementation
    private fun checkTimeAndSetVisibility(module: UpdatedModule) {
        val calendar = Calendar.getInstance()
        val currentHour = calendar.get(Calendar.HOUR_OF_DAY) // Get hour in 24-hour format (0-23)

        // Check if currentHour is between 18 (6 PM) and 1 (1 AM)
        // OR if currentHour is 0 (12 AM) or 1 (1 AM) or 2 (2 AM) for the next day
        val isVisible = (currentHour >= 18 && currentHour <= 23) || // 6 PM to 11 PM
                (currentHour >= 0 && currentHour <= 3)      // 12 AM (midnight) to 2 AM

        if (isVisible) {
            binding.cardSleepMainIdeal.visibility = View.VISIBLE
            if (module.sleepTime.equals("00:00")) {
                binding.tvTodaysSleepStartTime.text =
                    DateTimeUtils.getSleepTime12HourFormat("2025-05-14T15:30:00.000Z")  //module.sleepTime ?: "00:00"
            } else {
                binding.tvTodaysSleepStartTime.text = DateTimeUtils.getSleepTime12HourFormat(
                    module.idealSleepRequirementData?.sleepDatetime ?: "2025-05-14T15:30:00.000Z"
                )  //module.sleepTime ?: "00:00"
            }
            if (module.wakeUpTime.equals("00:00")) {
                binding.tvTodaysWakeupTime.text =
                    DateTimeUtils.getSleepTime12HourFormat("2025-05-15T23:30:00.000Z") //module.wakeUpTime ?: "00:00"
            } else {
                binding.tvTodaysWakeupTime.text = DateTimeUtils.getSleepTime12HourFormat(
                    module.idealSleepRequirementData?.wakeupDatetime ?: "2025-05-15T23:30:00.000Z"
                )
            }

            binding.tvTodaysSleepTimeRequirement.text =
                DateTimeUtils.convertDecimalHoursToHrMinFormat(
                    module.idealSleepRequirementData?.currentRequirement ?: 0.0
                )

            binding.cardSleepMainLog.visibility = View.GONE
            binding.cardSleeprightMain.visibility = View.GONE
        } else {
            try {
                // Check if all specified sleep-related fields are "0min"
                var isAllZero = module.rem == 0.toDouble() &&
                        module.core == 0.toDouble() &&
                        module.deep == 0.toDouble() &&
                        module.awake == 0.toDouble()

                if (isAllZero) {
                    binding.cardSleepMainIdeal.visibility = View.GONE
                    binding.cardSleepMainLog.visibility = View.VISIBLE
                    binding.cardSleeprightMain.visibility = View.GONE
                    // sleeo log card is visible, you can show a message or prompt the user to log their sleep
                    binding.tvPerformSleepDuration.text =
                        module.sleepPerformanceDetail?.actualSleepData?.actualSleepDurationHours?.let {
                            DateTimeUtils.formatSleepDuration(it)
                        } ?: "0 hr 0 min"// (module.sleepDuration ?: "0min").toString()
                    binding.tvPerformIdealDuration.text =
                        module.sleepPerformanceDetail?.idealSleepDuration?.let {
                            DateTimeUtils.formatSleepDuration(
                                it
                            )
                        } ?: (module.totalSleepDurationMinutes?.let {
                            DateTimeUtils.formatSleepDurationforidealSleep(
                                it
                            )
                        }
                            ?: "0min").toString()
                    binding.tvPerformSleepPercent.text =
                        (module.sleepPerformanceDetail?.sleepPerformanceData?.sleepPerformance
                            ?: "0").toString()
                    //(module.sleepDuration ?: "0").toString()
                } else {
                    // sleep data available  // For example, update your UI elements with sleepData.rem, sleepData.core, etc.
                    binding.cardSleepMainIdeal.visibility = View.GONE
                    binding.cardSleepMainLog.visibility = View.GONE
                    // Update UI elements here
                    binding.cardSleeprightMain.visibility = View.VISIBLE
                    binding.tvRem.text = DateTimeUtils.formatSleepDurationforidealSleep(
                        module.rem ?: 0.0
                    )//module.rem.toString()
                    binding.tvCore.text = DateTimeUtils.formatSleepDurationforidealSleep(
                        module.core ?: 0.0
                    )//module.core.toString()
                    binding.tvDeep.text = DateTimeUtils.formatSleepDurationforidealSleep(
                        module.deep ?: 0.0
                    )//module.deep.toString()
                    binding.tvAwake.text =
                        DateTimeUtils.formatSleepDurationforidealSleep(module.awake ?: 0.0)
                    //module.awake.toString()
                    binding.tvSleepTime.text =
                        convertTo12HourZoneFormat(module.sleepTime.toString())
                    binding.tvWakeupTime.text =
                        convertTo12HourZoneFormat(module.wakeUpTime.toString())

                    val sleepData = listOf(
                        SleepSegmentModel(
                            0.001f, 0.100f, resources.getColor(R.color.blue_bar), 110f
                        ), SleepSegmentModel(
                            0.101f, 0.150f, resources.getColor(R.color.blue_bar), 110f
                        ), SleepSegmentModel(
                            0.151f, 0.300f, resources.getColor(R.color.blue_bar), 110f
                        ), SleepSegmentModel(
                            0.301f, 0.400f, resources.getColor(R.color.blue_bar), 110f
                        ), SleepSegmentModel(
                            0.401f, 0.450f, resources.getColor(R.color.blue_bar), 110f
                        ), SleepSegmentModel(
                            0.451f, 0.550f, resources.getColor(R.color.sky_blue_bar), 110f
                        ), SleepSegmentModel(
                            0.551f, 0.660f, resources.getColor(R.color.sky_blue_bar), 110f
                        ), SleepSegmentModel(
                            0.661f, 0.690f, resources.getColor(R.color.sky_blue_bar), 110f
                        ), SleepSegmentModel(
                            0.691f, 0.750f, resources.getColor(R.color.deep_purple_bar), 110f
                        ), SleepSegmentModel(
                            0.751f, 0.860f, resources.getColor(R.color.deep_purple_bar), 110f
                        ), SleepSegmentModel(
                            0.861f, 0.990f, resources.getColor(R.color.red_orange_bar), 110f
                        )
                    )

                    //binding.sleepStagesView.setSleepData(sleepData)
                    // newSleepStagesHandling(module.sleepStages ?: emptyList())
// Set Sleep Stages Data
                    val sleepStageData: ArrayList<SleepStage> = arrayListOf()
                    if (module.sleepStages != null) {
                        for (i in 0 until module.sleepStages.size) {
                            module.sleepStages.getOrNull(i)?.let {
                                sleepStageData.add(it)
                            }
                        }
                        setSleepRightStageData(sleepStageData)

                    }

                    //setSleepRightStageData(module.sleepStages ?: emptyList())
                }
            } catch (e: Exception) {
                // Handle JSON parsing errors (e.g., malformed JSON)
                Toast.makeText(
                    requireContext(),
                    "Error parsing sleep data: ${e.message}",
                    Toast.LENGTH_LONG
                )
                    .show()
                e.printStackTrace() // Log the error for debugging
            }

        }
    }


    private fun setSleepRightStageData(sleepStageResponse: ArrayList<SleepStage>) {
        if (sleepStageResponse.size > 0) {

            remData.clear()
            awakeData.clear()
            coreData.clear()
            deepData.clear()

            var totalRemDuration = 0f
            var totalAwakeDuration = 0f
            var totalCoreDuration = 0f
            var totalDeepDuration = 0f

            for (i in 0 until sleepStageResponse.size) {
                val startDateTime =
                    LocalDateTime.parse(sleepStageResponse[i].startDatetime, formatters)
                val endDateTime = LocalDateTime.parse(sleepStageResponse[i].endDatetime, formatters)
                val duration = Duration.between(startDateTime, endDateTime).toMinutes()
                    .toFloat() / 60f // Convert to hours

                when (sleepStageResponse[i].stage) {
                    "REM Sleep" -> {
                        remData.add(duration)
                        totalRemDuration += duration
                    }

                    "Deep Sleep" -> {
                        deepData.add(duration)
                        totalDeepDuration += duration
                    }

                    "Light Sleep" -> {
                        coreData.add(duration)
                        totalCoreDuration += duration
                    }

                    "Awake" -> {
                        awakeData.add(duration)
                        totalAwakeDuration += duration
                    }
                }
            }
            binding.tvCore.text = formatDuration(totalCoreDuration)
            binding.tvAwake.text = formatDuration(totalAwakeDuration)
            binding.tvDeep.text = formatDuration(totalDeepDuration)
            binding.tvRem.text = formatDuration(totalRemDuration)

            setStageGraph(sleepStageResponse)
        }
    }


    private fun setStageGraph(sleepStageResponse: ArrayList<SleepStage>) {
        val sleepData: ArrayList<SleepSegmentModel> = arrayListOf()
        var currentPosition = 0f
        val totalDuration = sleepStageResponse.sumOf {
            Duration.between(
                LocalDateTime.parse(it.startDatetime, formatters),
                LocalDateTime.parse(it.endDatetime, formatters)
            ).toMinutes().toDouble()
        }.toFloat()

        sleepStageResponse.forEach { stage ->
            val startDateTime = LocalDateTime.parse(stage.startDatetime, formatters)
            val endDateTime = LocalDateTime.parse(stage.endDatetime, formatters)
            val duration = Duration.between(startDateTime, endDateTime).toMinutes().toFloat()
            val start = currentPosition / totalDuration
            val end = (currentPosition + duration) / totalDuration
            val color = when (stage.stage) {
                "REM Sleep" -> resources.getColor(R.color.light_blue_bar) // Light blue
                "Deep Sleep" -> resources.getColor(R.color.purple_bar) // Dark blue
                "Light Sleep" -> resources.getColor(R.color.blue_bar) // Medium blue
                "Awake" -> resources.getColor(R.color.red_orange_bar) // Red
                else -> Color.GRAY
            }
            sleepData.add(SleepSegmentModel(start, end, color, 110f))
            currentPosition += duration
        }

        binding.sleepStagesView.setSleepData(sleepData)
    }

    private fun formatDuration(durationHours: Float): String {
        val hours = durationHours.toInt()
        val minutes = ((durationHours - hours) * 60).toInt()
        return if (hours > 0) {
            "$hours hr $minutes mins"
        } else {
            "$minutes mins"
        }
    }

    private fun newSleepStagesHandling(sleepStages: List<SleepStage>?) {
        val totalDuration = sleepStages?.sumOf {
            it.durationMinutes ?: 0.0
        }
        println("Total Sleep Duration (in minutes): $totalDuration")


        val sleepData = mutableListOf<SleepSegmentModel>()
        var currentPosition = 0f

        sleepStages?.forEach { stageData ->
            var duration = stageData.durationMinutes?.toFloat() ?: 0f
            var start = currentPosition / (totalDuration?.toFloat() ?: 1f)
            var end = (currentPosition + duration) / (totalDuration?.toFloat() ?: 1f)

            val color = when {
                stageData.stage?.contains(
                    "REM",
                    ignoreCase = true
                ) == true -> Color.parseColor("#63D4FE")

                stageData.stage?.contains(
                    "Deep",
                    ignoreCase = true
                ) == true -> Color.parseColor("#5E5CE6")

                stageData.stage?.contains(
                    "Light",
                    ignoreCase = true
                ) == true -> Color.parseColor("#FF6650")

                stageData.stage?.contains(
                    "Awake",
                    ignoreCase = true
                ) == true -> Color.parseColor("#0B84FF")

                else -> Color.GRAY
            }

            sleepData.add(SleepSegmentModel(start, end, color, 110f))
            currentPosition += duration
        }

        binding.sleepStagesView.setSleepData(sleepData)

    }

    private fun extractNumericValues(input: String): Pair<String, String> {
        val parts = input.split("/")

        if (parts.size < 2) {
            throw IllegalArgumentException("Invalid format: $input")
        }

        val firstValue = parts[0].trim().replace(Regex("[^0-9.]"), "")
        val secondValue = parts[1].trim().replace(Regex("[^0-9.]"), "")

        return Pair(firstValue, secondValue)
    }

    private fun calculatePercentage(current: Int, total: Int): Int {
        if (total == 0) return 0 // Avoid division by zero
        return ((current.toFloat() / total.toFloat()) * 100).toInt()
    }

    private fun checkTrailEndedAndShowDialog(): Boolean {
        return if (!DashboardChecklistManager.paymentStatus) {
            showTrailEndedBottomSheet()
            false // Return false if condition is true and dialog is shown
        } else {
            if (!DashboardChecklistManager.checklistStatus) {
                AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.FINISH_TO_UNLOCK_CLICK)
                DialogUtils.showCheckListQuestionCommonDialog(requireContext())
                false
            } else {
                true // Return true if condition is false
            }
        }
        return true
    }

    private fun showTrailEndedBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetTrialEndedBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(requireContext(), R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnExplorePlan.setOnClickListener {
            bottomSheetDialog.dismiss()
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.SUBSCRIBE_RIGHT_LIFE_CLICK)
            startActivity(Intent(requireContext(), SubscriptionPlanListActivity::class.java).apply {
                putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
            })
            //finish()
        }

        bottomSheetDialog.show()
    }

    private fun installHealthConnect(context: Context) {
        val uri =
            "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun convertTo12HourZoneFormat(input: String): String {
        lateinit var inputFormatter: DateTimeFormatter
        if (input.length > 21) {
            inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        } else {
            inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        }
        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a") // 12-hour format with AM/PM

        // Parse as LocalDateTime (no time zone info)
        val utcDateTime = LocalDateTime.parse(input, inputFormatter)

        // Convert to UTC ZonedDateTime
        val utcZoned = utcDateTime.atZone(ZoneId.of("UTC"))

        // Convert to system local time zone
        val localZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault())

        return outputFormatter.format(localZoned)
    }

    fun formatValue(value: Double): String {
        return if (value >= 1000) {
            String.format("%.1fk", value / 1000) // 1 decimal ke saath
        } else {
            value.toInt().toString() // normal integer
        }
    }
}