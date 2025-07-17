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
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseFragment
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity
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
import com.jetsynthesys.rightlife.subscriptions.SubscriptionPlanListActivity
import com.jetsynthesys.rightlife.ui.CommonAPICall
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.healthcam.HealthCamActivity
import com.jetsynthesys.rightlife.ui.healthcam.NewHealthCamReportActivity
import com.jetsynthesys.rightlife.ui.new_design.OnboardingQuestionnaireActivity
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireEatRightActivity
import com.jetsynthesys.rightlife.ui.questionnaire.QuestionnaireThinkRightActivity
import com.jetsynthesys.rightlife.ui.scan_history.PastReportActivity
import com.jetsynthesys.rightlife.ui.utility.AppConstants
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils
import kotlinx.coroutines.launch
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class HomeDashboardFragment : BaseFragment() {
    private var _binding: FragmentHomeDashboardBinding? = null
    private val binding get() = _binding!!

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

        binding.includeChecklist.rlChecklistEatright.setOnClickListener {
            startActivity(Intent(requireContext(), QuestionnaireEatRightActivity::class.java))
        }
        binding.includeChecklist.rlChecklistSleepright.setOnClickListener {
            startActivity(Intent(requireContext(), QuestionnaireThinkRightActivity::class.java))
        }

        binding.rlViewPastReports.setOnClickListener {
            startActivity(Intent(requireContext(), PastReportActivity::class.java))
        }

        binding.includeChecklist.rlChecklistWhyThisDialog.setOnClickListener {
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
            startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                putExtra("ModuleName", "EatRight")
                putExtra("BottomSeatName", "SnapMealTypeEat")
                putExtra("snapMealId", snapMealId)
            })
        }
        binding.includeChecklist.rlChecklistFacescan.setOnClickListener {
            if (DashboardChecklistManager.facialScanStatus) {
                startActivity(
                    Intent(
                        requireContext(), NewHealthCamReportActivity::class.java
                    )
                )
            } else {
                startActivity(Intent(requireContext(), HealthCamActivity::class.java))
            }
        }

        binding.cardThinkrightMain.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "ThinkRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }
        binding.cardEatrightMain.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "EatRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }

        binding.cardMoverightMain.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "MoveRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }
        binding.cardSleeprightMain.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "SleepRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }
        binding.cardSleepMainIdeal.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "SleepRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }
        binding.cardSleepMainLog.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "SleepRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }


        // for no data card
        binding.cardThinkrightMainNodata.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "ThinkRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }
        binding.cardEatrightMainNodata.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "EatRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }

        binding.cardMoverightMainNodata.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "MoveRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }
        binding.cardSleeprightMainNodata.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "SleepRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }

        binding.cardEatright.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "EatRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }
        binding.cardSleepright.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "SleepRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }
        binding.cardThinkright.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "ThinkRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }
        binding.cardMoveright.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                startActivity(Intent(requireContext(), MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "MoveRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
        }

        val todayDate = SimpleDateFormat(
            "EEEE, d MMMM", Locale.getDefault()
        ).format(Calendar.getInstance().time)

        binding.tvDateDashboard.text = todayDate

        binding.trialExpiredLayout.trialExpiredLayout.visibility =
            if ((requireActivity() as? HomeNewActivity)?.isTrialExpired == true) View.VISIBLE else View.GONE

    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private suspend fun requestPermissionsAndReadAllData() {
        try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (allReadPermissions.all { it in granted }) {
//                fetchAllHealthData()
                CommonAPICall.updateChecklistStatus(
                    requireContext(), "sync_health_data", AppConstants.CHECKLIST_COMPLETED
                ){ status ->
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
                    ){ status ->
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
                    handleChecklistResponse(checklistResponse)
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
        //snap Meal
        setStatusOfChecklist(
            checklistResponse.data.meal_snap,
            binding.includeChecklist.imgCheckSnapmeal,
            binding.includeChecklist.rlChecklistSnapmeal,
            false
        )
        //sync health
        setStatusOfChecklist(
            checklistResponse.data.sync_health_data,
            binding.includeChecklist.imgCheckSynchealth,
            binding.includeChecklist.rlChecklistSynchealth
        )
        // face Scan
        setStatusOfChecklist(
            checklistResponse.data.vital_facial_scan,
            binding.includeChecklist.imgCheckFacescan,
            binding.includeChecklist.rlChecklistFacescan,
            false
        )
        // sleep right question
        setStatusOfChecklist(
            checklistResponse.data.unlock_sleep,
            binding.includeChecklist.imgCheckSleepright,
            binding.includeChecklist.rlChecklistSleepright
        )
        // Eat right question
        setStatusOfChecklist(
            checklistResponse.data.discover_eating,
            binding.includeChecklist.imgCheckEatright,
            binding.includeChecklist.rlChecklistEatright
        )
        binding.includeChecklist.tvChecklistNumber.text = "$checkListCount of 6 tasks completed"
        // Chceklist completion logic
        if (DashboardChecklistManager.checklistStatus) {
            binding.llDashboardMainData.visibility = View.VISIBLE
            binding.includeChecklist.llLayoutChecklist.visibility = View.GONE
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
                    handleSelectedModule(aiDashboardResponseMain)
                    handleDescoverList(aiDashboardResponseMain)
                    binding.recyclerView.layoutManager = LinearLayoutManager(requireContext())
                    binding.recyclerView.adapter =
                        HeartRateAdapter(aiDashboardResponseMain.data?.facialScan, requireContext())
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
                        if (DashboardChecklistManager.isDataLoaded) {
                            // Chceklist completion logic
                            if (DashboardChecklistManager.checklistStatus) {
                                binding.llDashboardMainData.visibility = View.VISIBLE
                                binding.includeChecklist.llLayoutChecklist.visibility = View.GONE
                                binding.llDiscoverLayout.visibility = View.GONE
                            } else {
                                binding.llDashboardMainData.visibility = View.GONE
                                binding.includeChecklist.llLayoutChecklist.visibility = View.VISIBLE
                                binding.llDiscoverLayout.visibility = View.VISIBLE
                            }
                        } else {
                            Toast.makeText(
                                requireContext(),
                                "Server Error: " + response.code(),
                                Toast.LENGTH_SHORT
                            ).show()
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
                }

                "THINK_RIGHT" -> {
                    // THINK_RIGHT logic here
                    binding.cardThinkrightMainNodata.visibility = View.VISIBLE
                    binding.cardThinkright.visibility = View.GONE
                }

                "EAT_RIGHT" -> {
                    // EAT_RIGHT logic here
                    binding.cardEatrightMainNodata.visibility = View.VISIBLE
                    binding.cardEatright.visibility = View.GONE
                }

                "SLEEP_RIGHT" -> {
                    // SLEEP_RIGHT logic here
                    binding.cardSleeprightMainNodata.visibility = View.VISIBLE
                    binding.cardSleepright.visibility = View.GONE
                }

                else -> {
                    // Default case logic here
                }
            }

        } else {
            binding.llNodataMain.visibility = View.GONE


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
                            DateTimeUtils.formatSleepDurationforidealSleep(
                                module.totalSleepDurationMinutes ?: 0.0
                            )
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
                        }
                        binding.tvMinutesTextValue.text = module.mindfulnessMinutes
                        binding.tvDaysTextValue.text = module.wellnessDays

                        setIfNotNullOrBlankWithCalories(
                            binding.tvModuleValueEatright,
                            module.calories?.toString()
                        )
                        setIfNotNullOrBlank(
                            binding.tvModuleValueSleepright,
                            DateTimeUtils.formatSleepDurationforidealSleep(
                                module.totalSleepDurationMinutes ?: 0.0
                            )
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
                        }

                        val (proteinValue, proteinTotal) = extractNumericValues(module.protein.toString())
                        val (carbsValue, carbsTotal) = extractNumericValues(module.carbs.toString())
                        val (fatsValue, fatsTotal) = extractNumericValues(module.fats.toString())

                        binding.tvSubtractionCalValue.text = proteinValue
                        binding.tvSubtractionCalUnit.text = "/" + proteinTotal + " g"
                        binding.carbsProgressBar.max = proteinTotal.toDouble().toInt()
                        binding.carbsProgressBar.progress = proteinValue.toDouble().toInt()


                        binding.tvSubtractionCarbsValue.text = carbsValue
                        binding.tvSubtractionCarbsUnit.text = "/" + carbsTotal + " g"
                        binding.protienProgressBar.max = carbsTotal.toDouble().toInt()
                        binding.protienProgressBar.progress = carbsValue.toDouble().toInt()

                        binding.tvSubtractionFatsValue.text = fatsValue
                        binding.tvSubtractionFatsUnit.text = "/" + fatsTotal + " g"
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
                            DateTimeUtils.formatSleepDurationforidealSleep(
                                module.totalSleepDurationMinutes ?: 0.0
                            )
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
                            checkTimeAndSetVisibility(module)
                        }


                        setIfNotNullOrBlankWithCalories(
                            binding.tvModuleValueEatright,
                            module.calories?.toString()
                        )
                        setIfNotNullOrBlank(
                            binding.tvModuleValueSleepright,
                            DateTimeUtils.formatSleepDurationforidealSleep(
                                module.totalSleepDurationMinutes ?: 0.0
                            )
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
                        } ?: "0 hr"// (module.sleepDuration ?: "0min").toString()
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
                    binding.tvRem.text = module.rem.toString()
                    binding.tvCore.text = module.core.toString()
                    binding.tvDeep.text = module.deep.toString()
                    binding.tvAwake.text = module.awake.toString()
                    binding.tvSleepTime.text = module.sleepTime.toString()
                    binding.tvWakeupTime.text = module.wakeUpTime.toString()

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
                    newSleepStagesHandling(module.sleepStages ?: emptyList())
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
}