package com.jetsynthesys.rightlife.newdashboard

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.net.toUri
import androidx.health.connect.client.HealthConnectClient
import androidx.health.connect.client.PermissionController
import androidx.health.connect.client.permission.HealthPermission
import androidx.health.connect.client.records.ActiveCaloriesBurnedRecord
import androidx.health.connect.client.records.BasalMetabolicRateRecord
import androidx.health.connect.client.records.BloodPressureRecord
import androidx.health.connect.client.records.BodyFatRecord
import androidx.health.connect.client.records.DistanceRecord
import androidx.health.connect.client.records.ExerciseSessionRecord
import androidx.health.connect.client.records.HeartRateRecord
import androidx.health.connect.client.records.HeartRateVariabilityRmssdRecord
import androidx.health.connect.client.records.OxygenSaturationRecord
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
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
import com.jetsynthesys.rightlife.ai_package.PermissionManager
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.SleepSegmentModel
import com.jetsynthesys.rightlife.databinding.BottomsheetTrialEndedBinding
import com.jetsynthesys.rightlife.databinding.FragmentHomeDashboardBinding
import com.jetsynthesys.rightlife.newdashboard.model.AiDashboardResponseMain
import com.jetsynthesys.rightlife.newdashboard.model.ChecklistResponse
import com.jetsynthesys.rightlife.newdashboard.model.DashboardChecklistManager
import com.jetsynthesys.rightlife.newdashboard.model.DashboardChecklistResponse
import com.jetsynthesys.rightlife.newdashboard.model.DiscoverDataItem
import com.jetsynthesys.rightlife.runWhenAttached
import com.jetsynthesys.rightlife.subscriptions.SubscriptionPlanListActivity
import com.jetsynthesys.rightlife.ui.ActivityUtils
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.healthcam.NewHealthCamReportActivity
import com.jetsynthesys.rightlife.ui.healthcam.basicdetails.HealthCamBasicDetailsNewActivity
import com.jetsynthesys.rightlife.ui.new_design.OnboardingQuestionnaireActivity
import com.jetsynthesys.rightlife.ui.scan_history.PastReportActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.AnalyticsParam
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils
import com.jetsynthesys.rightlife.ui.utility.FeatureFlags
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale

class HomeDashboardFragment : BaseFragment()
{
    private var _binding: FragmentHomeDashboardBinding? = null
    private val binding get() = _binding!!

    private val remData: ArrayList<Float> = arrayListOf()
    private val awakeData: ArrayList<Float> = arrayListOf()
    private val coreData: ArrayList<Float> = arrayListOf()
    private val deepData: ArrayList<Float> = arrayListOf()
    private val formatters = DateTimeFormatter.ISO_DATE_TIME
    private lateinit var permissionManager: PermissionManager
    private val permissionLauncher = registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { result ->
        permissionManager.handlePermissionResult(result)
    }

    private lateinit var healthConnectClient: HealthConnectClient
    private var checklistComplete = true
    private var checkListCount = 0
    private var snapMealId: String = ""

    private val allReadPermissions = setOf(
            HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
            HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
            HealthPermission.getReadPermission(DistanceRecord::class),
            HealthPermission.getReadPermission(StepsRecord::class),
            HealthPermission.getReadPermission(HeartRateRecord::class),
            HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
            HealthPermission.getReadPermission(RestingHeartRateRecord::class),
            HealthPermission.getReadPermission(RespiratoryRateRecord::class),
            HealthPermission.getReadPermission(OxygenSaturationRecord::class),
            HealthPermission.getReadPermission(BloodPressureRecord::class),
            HealthPermission.getReadPermission(WeightRecord::class),
            HealthPermission.getReadPermission(BodyFatRecord::class),
            HealthPermission.getReadPermission(SleepSessionRecord::class),
            HealthPermission.getReadPermission(ExerciseSessionRecord::class),
    )

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View
    {
        _binding = FragmentHomeDashboardBinding.inflate(inflater, container, false)
        AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.HOME_DASHBOARD_VISIT)
        return binding.root
    }

    override fun onResume()
    {
        super.onResume()
        fetchDashboardData()
        getDashboardChecklist()
        getDashboardChecklistStatus()
        getAiDashboard()
        Handler(Looper.getMainLooper()).postDelayed({
                                                        getDashboardChecklist()
                                                        getDashboardChecklistStatus()
                                                    }, 1000)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?)
    {
        super.onViewCreated(view, savedInstanceState)

    //    (requireActivity() as? HomeNewActivity)?.showHeader(true)
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
            it.disableViewForSeconds()
            if (sharedPreferenceManager.userProfile?.user_sub_status == 0)
            {
                freeTrialDialogActivity()
            } else
            {
                ActivityUtils.startEatRightQuestionnaireActivity(requireContext())
            }
        }
        binding.includeChecklist.rlChecklistSleepright.setOnClickListener {
            it.disableViewForSeconds()
            if (sharedPreferenceManager.userProfile?.user_sub_status == 0)
            {
                freeTrialDialogActivity()
            } else
            {
                ActivityUtils.startThinkRightQuestionnaireActivity(requireContext())
            }
        }

        binding.rlViewPastReports.setOnClickListener {
            it.disableViewForSeconds()
            startActivity(Intent(requireContext(), PastReportActivity::class.java))
        }
        binding.includeChecklist.imgQuestionmarkChecklist.setOnClickListener {
            it.disableViewForSeconds()
            DialogUtils.showWhyChecklistMattersDialog(requireContext(), "Here’s Why It Matters")
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.WHY_CHECKLIST_CLICK)/*startActivity(Intent(this@HomeDashboardActivity, SubscriptionPlanListActivity::class.java).apply {
                //putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
                //putExtra("SUBSCRIPTION_TYPE", "FACIAL_SCAN")
            })*/
        }
        binding.includeChecklist.rlChecklistWhyThisDialog.setOnClickListener {
            it.disableViewForSeconds()
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.FINISH_TO_UNLOCK_CLICK)
            DialogUtils.showWhyChecklistMattersDialog(requireContext())
        }

        binding.includeChecklist.rlChecklistSynchealth.setOnClickListener {
            it.disableViewForSeconds()
            if (sharedPreferenceManager.userProfile?.user_sub_status == 0)
            {
                freeTrialDialogActivity()
            } else
            {
                val availabilityStatus = HealthConnectClient.getSdkStatus(requireContext())
                if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE)
                {
                    healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
                    lifecycleScope.launch {
                        requestPermissionsAndReadAllData()

                    }
                } else
                {
                    installHealthConnect(requireContext())
                }
            }
        }
        binding.includeChecklist.rlChecklistProfile.setOnClickListener {
            it.disableViewForSeconds()
            if (sharedPreferenceManager.userProfile?.user_sub_status == 0)
            {
                freeTrialDialogActivity()
            } else
            {
                val intent = Intent(requireContext(), OnboardingQuestionnaireActivity::class.java)
                intent.putExtra("forProfileChecklist", true)
                startActivity(intent)
            }
        }
        binding.includeChecklist.rlChecklistSnapmeal.setOnClickListener {
            it.disableViewForSeconds()
            if (sharedPreferenceManager.userProfile?.user_sub_status == 0)
            {
                freeTrialDialogActivity(FeatureFlags.MEAL_SCAN)
            } else
            {
                /*permissionManager = PermissionManager(activity = requireActivity(), launcher = permissionLauncher, onPermissionGranted = {
                    // If local value empty, try from shared prefs
                    val safeSnapMealId = if (snapMealId.isNotBlank()) snapMealId
                    else sharedPreferenceManager.snapMealId ?: ""

                    logAndOpenMeal(safeSnapMealId)
                }, onPermissionDenied = {
                    Toast.makeText(requireContext(), "Permission denied", Toast.LENGTH_SHORT).show()
                })
                permissionManager.checkAndRequestPermissions()*/
                // If local value empty, try from shared prefs
                val safeSnapMealId = if (snapMealId.isNotBlank()) snapMealId
                else sharedPreferenceManager.snapMealId ?: ""
                logAndOpenMeal(safeSnapMealId)
            }
        }

        binding.includeChecklist.rlChecklistFacescan.setOnClickListener {
            it.disableViewForSeconds()
            if (sharedPreferenceManager.userProfile?.user_sub_status == 0)
            {
                freeTrialDialogActivity(FeatureFlags.FACE_SCAN)
            } else
            {
                val activity = requireActivity() as HomeNewActivity/*val isHealthCamFree = activity.isHealthCamFree*/

                var isHealthCamFree = sharedPreferenceManager.userProfile?.homeServices?.find { it.title == "HealthCam" }?.isFree
                        ?: false

                val isFacialScanService = sharedPreferenceManager.userProfile.facialScanService
                        ?: false


                if (DashboardChecklistManager.facialScanStatus)
                {
                    startActivity(Intent(requireContext(), NewHealthCamReportActivity::class.java))
                } else
                {
                    if (isFacialScanService)
                    {

                        ActivityUtils.startFaceScanActivity(requireContext())
                    } else
                    {
                        activity.showSwitchAccountDialog(requireContext(), "", "")
                    }
                }

            }
        }

        binding.cardThinkRight.setOnClickListener {
            it.disableViewForSeconds()
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.THINK_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog())
            {
                ActivityUtils.startThinkRightReportsActivity(requireContext(), "Not")
            }
        }
        binding.cardEatRight.setOnClickListener {
            it.disableViewForSeconds()
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.EAT_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog())
            {
                ActivityUtils.startEatRightReportsActivity(requireContext(), "Not")
            }
        }

        binding.cardMoveRight.setOnClickListener {
            it.disableViewForSeconds()
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.MOVE_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog())
            {
                ActivityUtils.startMoveRightReportsActivity(requireContext(), "Not")
            }
        }
        binding.cardSleepRight.setOnClickListener {
            it.disableViewForSeconds()
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.SLEEP_RIGHT_CLICK)
            if (checkTrailEndedAndShowDialog())
            {
                ActivityUtils.startSleepRightReportsActivity(requireContext(), "Not")
            }
        }

        val todayDate = SimpleDateFormat("EEEE, d MMMM", Locale.getDefault()).format(Calendar.getInstance().time)

        binding.tvDateDashboard.text = todayDate

        /*binding.trialExpiredLayout.trialExpiredLayout.visibility =
            if ((requireActivity() as? HomeNewActivity)?.isTrialExpired == true) View.VISIBLE else View.GONE*/


    }

    /*  @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
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
          }*/

//    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
//    private suspend fun requestPermissionsAndReadAllData()
//    {
//        try
//        {
//            // Get already granted permissions
//            val granted = healthConnectClient.permissionController.getGrantedPermissions()
//
//            if (allReadPermissions.all { it in granted })
//            {
//                // ✅ All permissions already granted → mark checklist completed
//                markHealthSyncChecklistCompleted()
//            } else
//            {
//                // 🚀 Ask user for permissions
//                requestPermissionsLauncher.launch(allReadPermissions.toTypedArray())
//            }
//        } catch (e: Exception)
//        {
//            e.printStackTrace()
//            Toast.makeText(requireContext(), "Something went wrong", Toast.LENGTH_SHORT).show()
//        }
//    }

    private suspend fun requestPermissionsAndReadAllData() {
        try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (allReadPermissions.all { it in granted }) {
                markHealthSyncChecklistCompleted()
                /*val getVisit = SharedPreferenceManager.getInstance(requireContext()).syncFirstVisit
                if (getVisit == "") {
                    fetchSecondChunk()
                    fetchThirdChunk()
                    fetchForthChunk()
                    SharedPreferenceManager.getInstance(requireContext()).saveSyncFirstVisit("1")
                }*/
            } else {
                requestPermissionsLauncher.launch(allReadPermissions)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error checking permissions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
        try
        {
        lifecycleScope.launch {
            if (granted.containsAll(allReadPermissions)) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show()
                }
                markHealthSyncChecklistCompleted()
            } else {
                withContext(Dispatchers.Main) {
                }
                markHealthSyncChecklistCompleted()
            }
        }
        } catch (e: Exception)
        {
            e.printStackTrace()
        }
    }

    /**
     * ✅ Helper function to update checklist on server ONLY after permission confirmed.
     */
    private fun markHealthSyncChecklistCompleted()
    {
        lifecycleScope.launch {
            try
            {
                CommonAPICall.updateChecklistStatus(requireContext(), "sync_health_data", AppConstants.CHECKLIST_COMPLETED) { status ->
                    if (status)
                    {
                        // 🔁 Refresh checklist UI
                        lifecycleScope.launch {
                            getDashboardChecklist()
                        }
                        Toast.makeText(requireContext(), "Health data sync marked as completed", Toast.LENGTH_SHORT).show()
                    } else
                    {
                        Toast.makeText(requireContext(), "Failed to update checklist on server", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception)
            {
                e.printStackTrace()
                Toast.makeText(requireContext(), "Error updating checklist", Toast.LENGTH_SHORT).show()
            }
        }
    }

    //getDashboardChecklist
    private fun getDashboardChecklist()
    {
        // Make the API call
        val call = apiService.getDashboardChecklist(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<ResponseBody?>
                     {
                         override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>)
                         {
                             if (response.isSuccessful && response.body() != null)
                             {
                                 val promotionResponse2 = response.body()!!.string()
                                 val gson = Gson()
                                 val checklistResponse = gson.fromJson(promotionResponse2, ChecklistResponse::class.java)
                                 sharedPreferenceManager.saveChecklistResponse(checklistResponse)
                                 runWhenAttached { handleChecklistResponse(checklistResponse) }
                                 checkListCount = 0
                             } else
                             {
                                 //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                             }
                         }

                         override fun onFailure(call: Call<ResponseBody?>, t: Throwable)
                         {
                             handleNoInternetView(t) // Print the full stack trace for more details
                         }
                     })
    }

    private fun handleChecklistResponse(checklistResponse: ChecklistResponse?)
    {

        // profile
        setStatusOfChecklist(checklistResponse?.data?.profile!!, binding.includeChecklist.imgCheck, binding.includeChecklist.rlChecklistProfile)
        AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.PROFILE_STATUS, mapOf(AnalyticsParam.PROFILE_STATUS to checklistResponse.data.profile))
        //snap Meal
        setStatusOfChecklist(checklistResponse.data.meal_snap, binding.includeChecklist.imgCheckSnapmeal, binding.includeChecklist.rlChecklistSnapmeal, false)
        AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.SNAP_MEAL_STATUS, mapOf(AnalyticsEvent.SNAP_MEAL_STATUS to checklistResponse.data.meal_snap))
        //sync health
        setStatusOfChecklist(checklistResponse.data.sync_health_data, binding.includeChecklist.imgCheckSynchealth, binding.includeChecklist.rlChecklistSynchealth)
        AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.SYNC_DATA_STATUS, mapOf(AnalyticsEvent.SYNC_DATA_STATUS to checklistResponse.data.meal_snap))
        // face Scan
        setStatusOfChecklist(checklistResponse.data.vital_facial_scan, binding.includeChecklist.imgCheckFacescan, binding.includeChecklist.rlChecklistFacescan, false)
        AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.FACIAL_SCAN_STATUS, mapOf(AnalyticsEvent.FACIAL_SCAN_STATUS to checklistResponse.data.vital_facial_scan))
        // sleep right question
        setStatusOfChecklist(checklistResponse.data.unlock_sleep, binding.includeChecklist.imgCheckSleepright, binding.includeChecklist.rlChecklistSleepright)
        AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.TR_SR_ASSESSMENT_STATUS, mapOf(AnalyticsEvent.TR_SR_ASSESSMENT_STATUS to checklistResponse.data.meal_snap))
        // Eat right question
        setStatusOfChecklist(checklistResponse.data.discover_eating, binding.includeChecklist.imgCheckEatright, binding.includeChecklist.rlChecklistEatright)
        AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.ER_MR_ASSESSMENT_STATUS, mapOf(AnalyticsEvent.ER_MR_ASSESSMENT_STATUS to checklistResponse.data.meal_snap))
        binding.includeChecklist.tvChecklistNumber.text = "$checkListCount of 6 tasks completed"
        // Chceklist completion logic
        if (DashboardChecklistManager.checklistStatus)
        {
            binding.llDashboardMainData.visibility = View.VISIBLE
            binding.includeChecklist.llLayoutChecklist.visibility = View.GONE
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.CHECKLIST_COMPLETE, mapOf(AnalyticsParam.CHECKLIST_COMPLETE to true))/*
                        val activity = requireActivity() as HomeNewActivity
                        activity.getUserDetails()*/
            if (sharedPreferenceManager.getFirstTimeForHomeDashboard())
            {
                sharedPreferenceManager.firstTimeForHomeDashboard = false
                AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.MyHealth_Dashboard_FirstOpen)
            }

        } else
        {
            binding.llDashboardMainData.visibility = View.GONE
            binding.includeChecklist.llLayoutChecklist.visibility = View.VISIBLE
        }/*checklistResponse.data.snap_mealId.let { snapMealId ->
            sharedPreferenceManager.saveSnapMealId(snapMealId)
            this.snapMealId = snapMealId
        }*/
        val mealId = checklistResponse.data.snap_mealId
        sharedPreferenceManager.saveSnapMealId(mealId)
        this.snapMealId = mealId
        checklistResponse.data.snap_mealId.let { snapMealId ->
            if (!snapMealId.isNullOrEmpty())
            {
                sharedPreferenceManager.saveSnapMealId(snapMealId)
                this.snapMealId = snapMealId
            } else
            {
                sharedPreferenceManager.saveSnapMealId("")
                this.snapMealId = ""
            }
        }

        getDashboardChecklistStatus()
    }


    private fun setStatusOfChecklist(
            profile: String,
            imageView: ImageView,
            relativeLayout: RelativeLayout,
            disableclick: Boolean = true,
    )
    {
        when (profile)
        {
            "INITIAL" ->
            {
                imageView.setImageResource(R.drawable.ic_checklist_tick_bg)
                checklistComplete = false
            }

            "INPROGRESS" ->
            {
                imageView.setImageResource(R.drawable.ic_checklist_pending)
                checklistComplete = false
            }

            "COMPLETED" ->
            {
                imageView.setImageResource(R.drawable.ic_checklist_complete)
                if (profile.equals("COMPLETED") && profile.equals("COMPLETED"))
                {

                }
                if (disableclick)
                {
                    relativeLayout.setOnClickListener(null)
                }
                checkListCount++
            }

            else ->
            {
                imageView.setImageResource(R.drawable.ic_checklist_tick_bg)
                checklistComplete = false
            }
        }
    }

    // New api Requested by backend team to be added in app need to discuss with them
    private fun fetchDashboardData()
    {
        val userId = sharedPreferenceManager.userId ?: return

        val date = DateTimeUtils.formatDateForOneApi()

        val call = apiServiceFastApi.getLandingDashboardData(userId, date, "android", true)
        call.enqueue(object : Callback<ResponseBody>
                     {
                         override fun onResponse(call: Call<ResponseBody>, response: Response<ResponseBody>)
                         {
                             if (response.isSuccessful && response.body() != null)
                             {
                                 val jsonString = response.body()!!.string()
                                 try
                                 {
                                     Log.d("DashboardRaw", jsonString)
                                 } catch (e: Exception)
                                 {
                                     e.printStackTrace()
                                 }
                             } else
                             {
                                 // showToast("Failed: ${response.message()}")
                             }
                         }

                         override fun onFailure(call: Call<ResponseBody>, t: Throwable)
                         {
                             handleNoInternetView(t)
                         }
                     })
    }

    // get AI Dashboard Data
    private fun getAiDashboard()
    {
        // Make the API call
        val date = DateTimeUtils.formatDateForOneApi()
        val call = apiService.getAiDashboard(sharedPreferenceManager.accessToken, date)
        call.enqueue(object : Callback<ResponseBody?>
                     {
                         override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>)
                         {
                             if (response.isSuccessful && response.body() != null)
                             {
                                 val promotionResponse2 = response.body()!!.string()

                                 val gson = Gson()

                                 val aiDashboardResponseMain = gson.fromJson(promotionResponse2, AiDashboardResponseMain::class.java)
                                 runWhenAttached {
                                     //handleSelectedModule(aiDashboardResponseMain)
                                     handleDescoverList(aiDashboardResponseMain)
                                     binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                                     binding.recyclerView.adapter = HeartRateAdapter(aiDashboardResponseMain.data?.facialScan, requireContext())
                                 }
                             } else
                             {
                                 //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                             }
                         }

                         override fun onFailure(call: Call<ResponseBody?>, t: Throwable)
                         {
                             handleNoInternetView(t)
                         }
                     })
    }

    private fun getDashboardChecklistStatus()
    {
        apiService.getdashboardChecklistStatus(sharedPreferenceManager.accessToken).enqueue(object : Callback<DashboardChecklistResponse>
                                                                                            {
                                                                                                override fun onResponse(call: Call<DashboardChecklistResponse>, response: Response<DashboardChecklistResponse>)
                                                                                                {
                                                                                                    if (response.isSuccessful && response.body() != null)
                                                                                                    {
                                                                                                        response.body()?.data?.let {
                                                                                                            DashboardChecklistManager.updateFrom(it)
                                                                                                        }
                                                                                                        runWhenAttached {
                                                                                                            if (DashboardChecklistManager.isDataLoaded)
                                                                                                            {
                                                                                                                // Chceklist completion logic
                                                                                                                if (DashboardChecklistManager.checklistStatus)
                                                                                                                {
                                                                                                                    binding.llDashboardMainData.visibility = View.VISIBLE
                                                                                                                    binding.includeChecklist.llLayoutChecklist.visibility = View.GONE
                                                                                                                    binding.llDiscoverLayout.visibility = View.GONE/*val activity = requireActivity() as HomeNewActivity
                                                 activity.fetchHealthDataFromHealthConnect();*/
                                                                                                                } else
                                                                                                                {
                                                                                                                    binding.llDashboardMainData.visibility = View.GONE
                                                                                                                    binding.includeChecklist.llLayoutChecklist.visibility = View.VISIBLE
                                                                                                                    binding.llDiscoverLayout.visibility = View.VISIBLE
                                                                                                                }/*(requireActivity() as? HomeNewActivity)?.showSubsribeLayout(
                                                 DashboardChecklistManager.paymentStatus
                                             )*/
                                                                                                                (requireActivity() as? HomeNewActivity)?.getUserDetails()
                                                                                                                setChecklistRowArrow(DashboardChecklistManager.paymentStatus)
                                                                                                            } else
                                                                                                            {
                                                                                                                Toast.makeText(requireContext(), "Server Error: " + response.code(), Toast.LENGTH_SHORT).show()
                                                                                                            }
                                                                                                        }
                                                                                                    }
                                                                                                }

                                                                                                override fun onFailure(call: Call<DashboardChecklistResponse>, t: Throwable)
                                                                                                {
                                                                                                    handleNoInternetView(t)
                                                                                                }

                                                                                            })
    }

    private fun setIfNotNullOrBlank(textView: TextView, value: String?)
    {
        if (!value.isNullOrBlank())
        {
            textView.text = value
        }
    }

    private fun setIfNotNullOrBlankWithCalories(textView: TextView, value: String?)
    {
        if (!value.isNullOrBlank())
        {
            val formattedValue = if (value.contains("kcal", ignoreCase = true))
            {
                value
            } else
            {
                "$value Kcal"
            }
            textView.text = formattedValue
        }
    }

    private fun handleDescoverList(aiDashboardResponseMain: AiDashboardResponseMain?)
    {
        if (aiDashboardResponseMain?.data?.discoverData != null)
        {
            setHealthNoDataCardAdapter(aiDashboardResponseMain.data.discoverData)
        } else
        {
            binding.llDiscoverLayout.visibility = View.GONE
        }
    }

    private fun setStageGraphFromSleepRightModule(rem: String, core: String, deep: String, awake: String)
    {
        val sleepData: ArrayList<SleepSegmentModel> = arrayListOf()
        val durations = mapOf("REM" to parseSleepDuration(rem), "Core" to parseSleepDuration(core), "Deep" to parseSleepDuration(deep), "Awake" to parseSleepDuration(awake))

        val totalDuration = durations.values.sum()
        var currentPosition = 0f

        durations.forEach { (stage, duration) ->
            val start = currentPosition / totalDuration
            val end = (currentPosition + duration) / totalDuration
            val color = when (stage)
            {
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

    private fun parseSleepDuration(durationStr: String): Float
    {
        val hourRegex = Regex("(\\d+)h")
        val minRegex = Regex("(\\d+)min")

        val hours = hourRegex.find(durationStr)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f
        val minutes = minRegex.find(durationStr)?.groupValues?.get(1)?.toFloatOrNull() ?: 0f

        return hours * 60 + minutes
    }

    private fun setHealthNoDataCardAdapter(discoverData: List<DiscoverDataItem>?)
    {
        binding.healthCardRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
            adapter = HealthCardAdapter(discoverData) { item ->
                if (sharedPreferenceManager.userProfile?.user_sub_status == 0)
                {
                    freeTrialDialogActivity()
                } else
                {
                    val intent = if (DashboardChecklistManager.facialScanStatus)
                    {
                        Intent(requireContext(), NewHealthCamReportActivity::class.java)
                    } else
                    {
                        Intent(requireContext(), HealthCamBasicDetailsNewActivity::class.java)
                    }
                    startActivity(intent)
                }

                AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.DISCOVER_CLICK, mapOf(AnalyticsParam.DISCOVER_TYPE to item?.parameter!!))
            }
            isNestedScrollingEnabled = false // 👈 important for smooth scroll
            overScrollMode = View.OVER_SCROLL_NEVER
        }
        if (discoverData.isNullOrEmpty())
        {
            binding.llDiscoverLayout.visibility = View.GONE
        } else
        {
            binding.llDiscoverLayout.visibility = View.VISIBLE
        }
    }

    private fun formatDuration(durationHours: Float): String
    {
        val hours = durationHours.toInt()
        val minutes = ((durationHours - hours) * 60).toInt()
        return if (hours > 0)
        {
            "$hours hr $minutes mins"
        } else
        {
            "$minutes mins"
        }
    }

    private fun extractNumericValues(input: String): Pair<String, String>
    {
        val parts = input.split("/")

        if (parts.size < 2)
        {
            throw IllegalArgumentException("Invalid format: $input")
        }

        val firstValue = parts[0].trim().replace(Regex("[^0-9.]"), "")
        val secondValue = parts[1].trim().replace(Regex("[^0-9.]"), "")

        return Pair(firstValue, secondValue)
    }

    private fun calculatePercentage(current: Int, total: Int): Int
    {
        if (total == 0) return 0 // Avoid division by zero
        return ((current.toFloat() / total.toFloat()) * 100).toInt()
    }

    private fun checkTrailEndedAndShowDialog(): Boolean
    {
        return if (!DashboardChecklistManager.paymentStatus)
        {
            showTrailEndedBottomSheet()
            false // Return false if condition is true and dialog is shown
        } else
        {
            if (!DashboardChecklistManager.checklistStatus)
            {
                AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.FINISH_TO_UNLOCK_CLICK)
                DialogUtils.showCheckListQuestionCommonDialog(requireContext())
                false
            } else
            {
                true // Return true if condition is false
            }
        }
        return true
    }

    private fun showTrailEndedBottomSheet()
    {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(requireContext())

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetTrialEndedBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null)
        {
            val slideUpAnimation: Animation = AnimationUtils.loadAnimation(requireContext(), R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnExplorePlan.setOnClickListener {
            it.disableViewForSeconds()
            bottomSheetDialog.dismiss()
            AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.SUBSCRIBE_RIGHT_LIFE_CLICK)
            startActivity(Intent(requireContext(), SubscriptionPlanListActivity::class.java).apply {
                putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
            })
            //finish()
        }

        bottomSheetDialog.show()
    }

    private fun installHealthConnect(context: Context)
    {
        val uri = "https://play.google.com/store/apps/details?id=com.google.android.apps.healthdata".toUri()
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }

    override fun onDestroyView()
    {
        super.onDestroyView()
        _binding = null
    }

    fun convertTo12HourZoneFormat(input: String): String
    {
        lateinit var inputFormatter: DateTimeFormatter
        if (input.length > 21)
        {
            inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        } else
        {
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

    fun formatValue(value: Double): String
    {
        return if (value >= 1000)
        {
            String.format("%.1fk", value / 1000) // 1 decimal ke saath
        } else
        {
            value.toInt().toString() // normal integer
        }
    }

    private fun setChecklistRowArrow(isAccessible: Boolean)
    {
        if (sharedPreferenceManager.userProfile?.user_sub_status == 0)
        {
            binding.includeChecklist.imgChecklistProfileArrow.setImageResource(R.drawable.checklist_lock)
            binding.includeChecklist.imgChecklistSleepRightArrow.setImageResource(R.drawable.checklist_lock)
            binding.includeChecklist.imgChecklistSnapMealArrow.setImageResource(R.drawable.checklist_lock)
            binding.includeChecklist.imgChecklistEatRightArrow.setImageResource(R.drawable.checklist_lock)
            binding.includeChecklist.imgChecklistSyncDataArrow.setImageResource(R.drawable.checklist_lock)
            binding.includeChecklist.imgChecklistFaceScanArrow.setImageResource(R.drawable.checklist_lock)
        } else
        {
            binding.includeChecklist.imgChecklistProfileArrow.setImageResource(R.drawable.ic_checklist_smallarrow)
            binding.includeChecklist.imgChecklistSleepRightArrow.setImageResource(R.drawable.ic_checklist_smallarrow)
            binding.includeChecklist.imgChecklistSnapMealArrow.setImageResource(R.drawable.ic_checklist_smallarrow)
            binding.includeChecklist.imgChecklistEatRightArrow.setImageResource(R.drawable.ic_checklist_smallarrow)
            binding.includeChecklist.imgChecklistSyncDataArrow.setImageResource(R.drawable.ic_checklist_smallarrow)
            binding.includeChecklist.imgChecklistFaceScanArrow.setImageResource(R.drawable.ic_checklist_smallarrow)
        }
    }

    private fun freeTrialDialogActivity(featureFlag: String = "")
    {
        val intent = Intent(requireActivity(), BeginMyFreeTrialActivity::class.java).apply {
            putExtra(FeatureFlags.EXTRA_ENTRY_DEST, featureFlag)
        }
        startActivity(intent)
    }


    private fun logAndOpenMeal(snapId: String)
    {
        AnalyticsLogger.logEvent(requireContext(), AnalyticsEvent.EOS_SNAP_MEAL_CLICK)
        //ActivityUtils.startEatRightReportsActivity(requireContext(), "SnapMealTypeEat", snapId)
        startActivity(Intent(context, MainAIActivity::class.java).apply {
            putExtra("ModuleName", "EatRight")
            putExtra("BottomSeatName", "SnapMealTypeEat")
            putExtra("snapMealId", snapId)
        })
    }

}