package com.jetsynthesys.rightlife.newdashboard

import android.annotation.SuppressLint
import android.app.ComponentCaller
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.ColorDrawable
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.DecelerateInterpolator
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
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
import androidx.health.connect.client.records.SpeedRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import com.android.billingclient.api.AcknowledgePurchaseParams
import com.android.billingclient.api.BillingClient
import com.android.billingclient.api.BillingClientStateListener
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.ConsumeParams
import com.android.billingclient.api.PendingPurchasesParams
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.QueryPurchasesParams
import com.bumptech.glide.Glide
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import com.google.firebase.remoteconfig.remoteConfigSettings
import com.google.gson.Gson
import com.google.gson.JsonElement
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.BuildConfig
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.RetrofitData.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.BloodPressure
import com.jetsynthesys.rightlife.ai_package.model.BodyFatPercentage
import com.jetsynthesys.rightlife.ai_package.model.BodyMass
import com.jetsynthesys.rightlife.ai_package.model.Distance
import com.jetsynthesys.rightlife.ai_package.model.EnergyBurnedRequest
import com.jetsynthesys.rightlife.ai_package.model.HeartRateRequest
import com.jetsynthesys.rightlife.ai_package.model.HeartRateVariabilityRequest
import com.jetsynthesys.rightlife.ai_package.model.OxygenSaturation
import com.jetsynthesys.rightlife.ai_package.model.RespiratoryRate
import com.jetsynthesys.rightlife.ai_package.model.SleepStageJson
import com.jetsynthesys.rightlife.ai_package.model.StepCountRequest
import com.jetsynthesys.rightlife.ai_package.model.StoreHealthDataRequest
import com.jetsynthesys.rightlife.ai_package.model.WorkoutRequest
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity
import com.jetsynthesys.rightlife.apimodel.userdata.UserProfileResponse
import com.jetsynthesys.rightlife.databinding.ActivityHomeNewBinding
import com.jetsynthesys.rightlife.databinding.BottomsheetTrialEndedBinding
import com.jetsynthesys.rightlife.databinding.DialogForceUpdateBinding
import com.jetsynthesys.rightlife.databinding.DialogSwitchAccountBinding
import com.jetsynthesys.rightlife.newdashboard.model.DashboardChecklistManager
import com.jetsynthesys.rightlife.subscriptions.SubscriptionPlanListActivity
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentSuccessRequest
import com.jetsynthesys.rightlife.subscriptions.pojo.PaymentSuccessResponse
import com.jetsynthesys.rightlife.subscriptions.pojo.SdkDetail
import com.jetsynthesys.rightlife.ui.ActivityUtils
import com.jetsynthesys.rightlife.ui.DialogUtils
import com.jetsynthesys.rightlife.ui.aireport.AIReportWebViewActivity
import com.jetsynthesys.rightlife.ui.healthcam.NewHealthCamReportActivity
import com.jetsynthesys.rightlife.ui.jounal.new_journal.JournalListActivity
import com.jetsynthesys.rightlife.ui.profile_new.ProfileSettingsActivity
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.DateTimeUtils
import com.jetsynthesys.rightlife.ui.utility.NetworkUtils
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.concurrent.TimeUnit

class HomeNewActivity : BaseActivity() {
    private lateinit var binding: ActivityHomeNewBinding
    private var isAdd = true
    var isTrialExpired = false
    private var isCountDownVisible = false
    var isHealthCamFree = true
    private var totalCaloriesBurnedRecord: List<TotalCaloriesBurnedRecord>? = null
    private var activeCalorieBurnedRecord: List<ActiveCaloriesBurnedRecord>? = null
    private var stepsRecord: List<StepsRecord>? = null
    private var heartRateRecord: List<HeartRateRecord>? = null
    private var heartRateVariability: List<HeartRateVariabilityRmssdRecord>? = null
    private var restingHeartRecord: List<RestingHeartRateRecord>? = null
    private var basalMetabolicRateRecord: List<BasalMetabolicRateRecord>? = null
    private var bloodPressureRecord: List<BloodPressureRecord>? = null
    private var sleepSessionRecord: List<SleepSessionRecord>? = null
    private var exerciseSessionRecord: List<ExerciseSessionRecord>? = null
    private var speedRecord: List<SpeedRecord>? = null
    private var weightRecord: List<WeightRecord>? = null
    private var distanceRecord: List<DistanceRecord>? = null
    private var bodyFatRecord: List<BodyFatRecord>? = null
    private var oxygenSaturationRecord: List<OxygenSaturationRecord>? = null
    private var respiratoryRateRecord: List<RespiratoryRateRecord>? = null
    private lateinit var healthConnectClient: HealthConnectClient

    @SuppressLint("ClickableViewAccessibility")
    private val allReadPermissions = setOf(
        HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class),
        HealthPermission.getReadPermission(StepsRecord::class),
        HealthPermission.getReadPermission(HeartRateRecord::class),
        HealthPermission.getReadPermission(SleepSessionRecord::class),
        HealthPermission.getReadPermission(ExerciseSessionRecord::class),
        HealthPermission.getReadPermission(SpeedRecord::class),
        HealthPermission.getReadPermission(WeightRecord::class),
        HealthPermission.getReadPermission(DistanceRecord::class),
        HealthPermission.getReadPermission(OxygenSaturationRecord::class),
        HealthPermission.getReadPermission(RespiratoryRateRecord::class),
        HealthPermission.getReadPermission(BodyFatRecord::class),
        HealthPermission.getReadPermission(RestingHeartRateRecord::class),
        HealthPermission.getReadPermission(BasalMetabolicRateRecord::class),
        HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class),
        HealthPermission.getReadPermission(BloodPressureRecord::class)
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHomeNewBinding.inflate(layoutInflater)
        setChildContentView(binding.root)
        this?.let {
            if (HealthConnectClient.getSdkStatus(it) == HealthConnectClient.SDK_AVAILABLE) {
                healthConnectClient = HealthConnectClient.getOrCreate(it)
            }
        }

        // Load default fragment only on first launch
        if (savedInstanceState == null) {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, HomeDashboardFragment())
                .commit()
            updateMenuSelection(R.id.menu_home)
        } else {
            // 🟢 Restore menu highlight based on current fragment
            val currentFragment = supportFragmentManager.findFragmentById(R.id.fragmentContainer)
            when (currentFragment) {
                is HomeExploreFragment -> updateMenuSelection(R.id.menu_explore)
                is HomeDashboardFragment -> updateMenuSelection(R.id.menu_home)
            }
        }

        binding.scrollView.setOnScrollChangeListener { view, scrollX, scrollY, oldScrollX, oldScrollY ->
            binding.swipeRefreshLayout.isEnabled = scrollY <= 5
        }

        onBackPressedDispatcher.addCallback {
            if (binding.includedhomebottomsheet.bottomSheet.visibility == View.VISIBLE) {
                binding.includedhomebottomsheet.bottomSheet.visibility = View.GONE
                binding.includedhomebottomsheet.bottomSheetParent.setBackgroundColor(Color.TRANSPARENT)
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                when (currentFragment) {
                    is HomeExploreFragment -> updateMenuSelection(R.id.menu_explore)
                    is HomeDashboardFragment -> updateMenuSelection(R.id.menu_home)
                }

                binding.fab.setImageResource(R.drawable.icon_quicklink_plus) // Change back to add icon
                binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                    this@HomeNewActivity, R.color.white
                )
                binding.fab.imageTintList = ColorStateList.valueOf(
                    resources.getColor(
                        R.color.rightlife
                    )
                )
                isAdd = !isAdd // Toggle the state

            } else {
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                when (currentFragment) {
                    is HomeExploreFragment -> {
                        supportFragmentManager.beginTransaction()
                            .replace(R.id.fragmentContainer, HomeDashboardFragment())
                            .commit()
                        updateMenuSelection(R.id.menu_home)
                    }

                    is HomeDashboardFragment -> finish()
                }
            }
        }

        // Handle FAB click
        binding.fab.backgroundTintList = ContextCompat.getColorStateList(
            this, android.R.color.white
        )
        binding.fab.imageTintList = ColorStateList.valueOf(resources.getColor(R.color.black))
        val bottom_sheet = binding.includedhomebottomsheet.bottomSheet
        binding.fab.setOnClickListener { v ->

            if (binding.includedhomebottomsheet.bottomSheet.visibility == View.VISIBLE) {
                bottom_sheet.visibility = View.GONE
                binding.includedhomebottomsheet.bottomSheetParent.setBackgroundColor(Color.TRANSPARENT)
                val currentFragment =
                    supportFragmentManager.findFragmentById(R.id.fragmentContainer)
                when (currentFragment) {
                    is HomeExploreFragment -> updateMenuSelection(R.id.menu_explore)
                    is HomeDashboardFragment -> updateMenuSelection(R.id.menu_home)
                }
            } else {
                bottom_sheet.visibility = View.VISIBLE
                binding.includedhomebottomsheet.bottomSheetParent.setBackgroundColor(
                    Color.parseColor(
                        "#CC000000"
                    )
                )
            }
            v.isSelected = !v.isSelected

            binding.fab.animate().rotationBy(180f).setDuration(60)
                .setInterpolator(DecelerateInterpolator()).withEndAction {
                    // Change icon after rotation
                    if (isAdd) {
                        binding.fab.setImageResource(R.drawable.icon_quicklink_plus_black) // Change to close icon
                        binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.rightlife
                        )
                        binding.fab.imageTintList = ColorStateList.valueOf(
                            resources.getColor(
                                R.color.black
                            )
                        )
                    } else {
                        binding.fab.setImageResource(R.drawable.icon_quicklink_plus) // Change back to add icon
                        binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                            this, R.color.white
                        )
                        binding.fab.imageTintList = ColorStateList.valueOf(
                            resources.getColor(
                                R.color.rightlife
                            )
                        )
                    }
                    isAdd = !isAdd // Toggle the state
                }.start()
        }

        binding.profileImage.setOnClickListener {
            startActivity(Intent(this, ProfileSettingsActivity::class.java))
        }

        // Handle menu item clicks
        binding.menuHome.setOnClickListener {
            if (binding.includedhomebottomsheet.bottomSheet.visibility == View.VISIBLE) {
                bottom_sheet.visibility = View.GONE
                binding.includedhomebottomsheet.bottomSheetParent.setBackgroundColor(Color.TRANSPARENT)
                binding.fab.animate().rotationBy(180f).setDuration(60)
                    .setInterpolator(DecelerateInterpolator()).withEndAction {
                        // Change icon after rotation
                        if (isAdd) {
                            binding.fab.setImageResource(R.drawable.icon_quicklink_plus_black) // Change to close icon
                            binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                                this, R.color.rightlife
                            )
                            binding.fab.imageTintList = ColorStateList.valueOf(
                                resources.getColor(
                                    R.color.black
                                )
                            )
                        } else {
                            binding.fab.setImageResource(R.drawable.icon_quicklink_plus) // Change back to add icon
                            binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                                this, R.color.white
                            )
                            binding.fab.imageTintList = ColorStateList.valueOf(
                                resources.getColor(
                                    R.color.rightlife
                                )
                            )
                        }
                        isAdd = !isAdd // Toggle the state
                    }.start()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeDashboardFragment())
                    .commit()
                updateMenuSelection(R.id.menu_home)
            }
        }

        binding.menuExplore.setOnClickListener {
            if (binding.includedhomebottomsheet.bottomSheet.visibility == View.VISIBLE) {
                bottom_sheet.visibility = View.GONE
                binding.includedhomebottomsheet.bottomSheetParent.setBackgroundColor(Color.TRANSPARENT)
                binding.fab.animate().rotationBy(180f).setDuration(60)
                    .setInterpolator(DecelerateInterpolator()).withEndAction {
                        // Change icon after rotation
                        if (isAdd) {
                            binding.fab.setImageResource(R.drawable.icon_quicklink_plus_black) // Change to close icon
                            binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                                this, R.color.rightlife
                            )
                            binding.fab.imageTintList = ColorStateList.valueOf(
                                resources.getColor(
                                    R.color.black
                                )
                            )
                        } else {
                            binding.fab.setImageResource(R.drawable.icon_quicklink_plus) // Change back to add icon
                            binding.fab.backgroundTintList = ContextCompat.getColorStateList(
                                this, R.color.white
                            )
                            binding.fab.imageTintList = ColorStateList.valueOf(
                                resources.getColor(
                                    R.color.rightlife
                                )
                            )
                        }
                        isAdd = !isAdd // Toggle the state
                    }.start()
            } else {
                supportFragmentManager.beginTransaction()
                    .replace(R.id.fragmentContainer, HomeExploreFragment())
                    .commit()
                updateMenuSelection(R.id.menu_explore)
            }
        }

        with(binding) {
            includedhomebottomsheet.llJournal.setOnClickListener {
                AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.EOS_JOURNALING_CLICK)
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startJournalListActivity(this@HomeNewActivity)
                }
            }
            includedhomebottomsheet.llAffirmations.setOnClickListener {
                AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.EOS_AFFIRMATION_CLICK)
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startTodaysAffirmationActivity(this@HomeNewActivity)
                }
            }
            includedhomebottomsheet.llSleepsounds.setOnClickListener {
                AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.EOS_SLEEP_SOUNDS)
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startSleepSoundActivity(this@HomeNewActivity)
                }
            }
            includedhomebottomsheet.llBreathwork.setOnClickListener {
                AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.EOS_BREATH_WORK_CLICK)
                if (checkTrailEndedAndShowDialog()) {
                    ActivityUtils.startBreathWorkActivity(this@HomeNewActivity)
                }
            }
            includedhomebottomsheet.llHealthCamQl.setOnClickListener {
                AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.EOS_FACE_SCAN_CLICK)
                if (DashboardChecklistManager.facialScanStatus) {
                    startActivity(
                        Intent(
                            this@HomeNewActivity, NewHealthCamReportActivity::class.java
                        )
                    )
                } else {
                    ActivityUtils.startFaceScanActivity(this@HomeNewActivity)
                }
            }
            includedhomebottomsheet.llMealplan.setOnClickListener {
                AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.EOS_SNAP_MEAL_CLICK)
                ActivityUtils.startEatRightReportsActivity(
                    this@HomeNewActivity,
                    "SnapMealTypeEat",
                    sharedPreferenceManager.snapMealId.ifEmpty { "" })
            }

        }

        binding.includedhomebottomsheet.llFoodLog.setOnClickListener {
            AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.LYA_FOOD_LOG_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startEatRightReportsActivity(this@HomeNewActivity, "MealLogTypeEat")
            }
        }
        binding.includedhomebottomsheet.llActivityLog.setOnClickListener {
            AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.LYA_ACTIVITY_LOG_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startMoveRightReportsActivity(
                    this@HomeNewActivity,
                    "SearchActivityLogMove"
                )
            }
        }
        binding.includedhomebottomsheet.llMoodLog.setOnClickListener {
            if (checkTrailEndedAndShowDialog()) {
                Toast.makeText(this, "Coming Soon", Toast.LENGTH_SHORT).show()
            }
        }
        binding.includedhomebottomsheet.llSleepLog.setOnClickListener {
            AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.LYA_SLEEP_LOG_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startSleepRightReportsActivity(
                    this@HomeNewActivity,
                    "LogLastNightSleep"
                )
            }
        }
        binding.includedhomebottomsheet.llWeightLog.setOnClickListener {
            AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.LYA_WEIGHT_LOG_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startEatRightReportsActivity(this@HomeNewActivity, "LogWeightEat")
            }
        }
        binding.includedhomebottomsheet.llWaterLog.setOnClickListener {
            AnalyticsLogger.logEvent(this@HomeNewActivity, AnalyticsEvent.LYA_WATER_LOG_CLICK)
            if (checkTrailEndedAndShowDialog()) {
                ActivityUtils.startEatRightReportsActivity(
                    this@HomeNewActivity,
                    "LogWaterIntakeEat"
                )
            }
        }

        // Open AI Report WebView on click   // Also logic to hide this button if Report is not generated pending
        binding.rightLifeReportCard.setOnClickListener {
            var dynamicReportId = "" // This Is User ID
            dynamicReportId = SharedPreferenceManager.getInstance(applicationContext).userId
            if (dynamicReportId.isEmpty()) {
                // Some error handling if the ID is not available
            } else {
                val intent = Intent(this, AIReportWebViewActivity::class.java).apply {
                    // Put the dynamic ID as an extra
                    putExtra(AIReportWebViewActivity.EXTRA_REPORT_ID, dynamicReportId)
                }
                startActivity(intent)
            }
        }

        // Handling Subscribe to RightLife
        binding.trialExpiredLayout.btnSubscription.setOnClickListener {

            startActivity(
                Intent(
                    this,
                    SubscriptionPlanListActivity::class.java
                ).apply {
                    putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
                })
        }
    }

    override fun onResume() {
        super.onResume()
        checkForUpdate()
        getUserDetails()
        initBillingAndRecover()
    }

    override fun onNewIntent(intent: Intent, caller: ComponentCaller) {
        super.onNewIntent(intent, caller)
        if (intent.getBooleanExtra("start_journal", false)) {
            startActivity(Intent(this, JournalListActivity::class.java))
        } else if (intent.getBooleanExtra("start_profile", false)) {
            startActivity(Intent(this, ProfileSettingsActivity::class.java))
        } else if (intent.getBooleanExtra("finish_MindAudit", false)) {
            if (intent.getBooleanExtra("FROM_THINK_RIGHT", false)) {
                startActivity(Intent(this, MainAIActivity::class.java))
            }
        }
    }


    // get user details
    fun getUserDetails() {
        // Make the API call
        val call = apiService.getUserDetais(sharedPreferenceManager.accessToken)
        call.enqueue(object : Callback<JsonElement?> {
            override fun onResponse(call: Call<JsonElement?>, response: Response<JsonElement?>) {
                if (response.isSuccessful && response.body() != null) {
                    val gson = Gson()
                    val jsonResponse = gson.toJson(response.body())

                    val ResponseObj = gson.fromJson(
                        jsonResponse, UserProfileResponse::class.java
                    )
                    SharedPreferenceManager.getInstance(applicationContext)
                        .saveUserId(ResponseObj.userdata.id)
                    SharedPreferenceManager.getInstance(applicationContext)
                        .saveUserProfile(ResponseObj)

                    SharedPreferenceManager.getInstance(applicationContext)
                        .setAIReportGeneratedView(ResponseObj.reportView)

                    if (ResponseObj.userdata.profilePicture != null) {
                        Glide.with(this@HomeNewActivity)
                            .load(ApiClient.CDN_URL_QA + ResponseObj.userdata.profilePicture)
                            .placeholder(R.drawable.rl_profile).error(R.drawable.rl_profile)
                            .into(binding.profileImage)
                    }
                    binding.userName.text = ResponseObj.userdata.firstName
                    val tvGreetingText = findViewById<TextView>(R.id.greetingText)
                    tvGreetingText.text = "Good " + DateTimeUtils.getWishingMessage() + " ,"

                    val countDown = getCountDownDays(ResponseObj.userdata.createdAt)
                    if (countDown < 7) {
                        binding.tvCountDown.text = "${countDown + 1}/7"
                        binding.llCountDown.visibility = View.VISIBLE
                        binding.trialExpiredLayout.trialExpiredLayout.visibility = View.GONE
                        isTrialExpired = false
                        isCountDownVisible = true
                    } else {
                        binding.llCountDown.visibility = View.GONE
                        isCountDownVisible = false
                        if (!DashboardChecklistManager.paymentStatus) {
                            binding.trialExpiredLayout.trialExpiredLayout.visibility = View.VISIBLE
                            isTrialExpired = true
                        } else {
                            binding.trialExpiredLayout.trialExpiredLayout.visibility = View.GONE
                            isTrialExpired = false
                        }
                    }

                    if (ResponseObj.isReportGenerated && !ResponseObj.reportView) {
                        binding.rightLifeReportCard.visibility = View.VISIBLE
                    } else {
                        binding.rightLifeReportCard.visibility = View.GONE
                    }


                    /*if (!ResponseObj.reportView){
                        binding.rightLifeReportCard.visibility = View.VISIBLE
                    } else {
                        binding.rightLifeReportCard.visibility = View.GONE
                    }*/
                    /*if (ResponseObj.isFacialReport != null && ResponseObj.isFacialReport) {
                        showSwitchAccountDialog(this@HomeNewActivity,"","")
                    } else {

                    }*/

                    // Find HealthCam service and check if it's free
                    var isHealthCamFree = ResponseObj?.homeServices
                        ?.find { it.title == "HealthCam" }
                        ?.isFree ?: false
                    Log.d("isHealthCamFree", isHealthCamFree.toString())
                } else {
                    //  Toast.makeText(HomeActivity.this, "Server Error: " + response.code(), Toast.LENGTH_SHORT).show();
                }

                if (!DashboardChecklistManager.paymentStatus) {
                    binding.trialExpiredLayout.trialExpiredLayout.visibility = View.VISIBLE
                    isTrialExpired = true
                } else {
                    binding.trialExpiredLayout.trialExpiredLayout.visibility = View.GONE
                    isTrialExpired = false
                }


            }

            override fun onFailure(call: Call<JsonElement?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    private fun checkForUpdate() {
        val remoteConfig = Firebase.remoteConfig
        val configSettings = remoteConfigSettings {
            minimumFetchIntervalInSeconds = 3600
        }
        remoteConfig.setConfigSettingsAsync(configSettings)
        remoteConfig.setDefaultsAsync(
            mapOf(
                "force_update_current_version" to "1.0.0",
                "isForceUpdate" to false,
                "force_update_build_number" to "261"
            )
        )

        remoteConfig.fetchAndActivate().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val latestVersion = remoteConfig.getString("force_update_current_version")
                val isForceUpdate = remoteConfig.getBoolean("isForceUpdate")
                val forceUpdateBuildNumber = remoteConfig.getString("force_update_build_number")

                val currentVersion = BuildConfig.VERSION_NAME

                if (isForceUpdate && isVersionOutdated(currentVersion, latestVersion)) {
                    showForceUpdateDialog()
                }
            }
        }
    }

    private fun isVersionOutdated(current: String, latest: String): Boolean {
        return current != latest // or use a version comparator for more complex rules
    }

    private fun getCountDownDays(pastTimestamp: Long): Int {
        val currentTimestamp = System.currentTimeMillis()

        val diffInMillis = currentTimestamp - pastTimestamp
        val diffInDays = TimeUnit.MILLISECONDS.toDays(diffInMillis)
        return diffInDays.toInt()
    }

    private fun showForceUpdateDialog() {

        // Create the dialog
        val dialog = Dialog(this)
        val binding = DialogForceUpdateBinding.inflate(layoutInflater)

        dialog.setContentView(binding.root)
        dialog.setCancelable(false) // Prevent back press
        dialog.setCanceledOnTouchOutside(false) // Prevent outside tap
        dialog.window?.setBackgroundDrawableResource(android.R.color.transparent)

        val window = dialog.window
        window?.let {
            val layoutParams = it.attributes
            layoutParams.dimAmount = 0.7f
            it.attributes = layoutParams
        }

        binding.btnUpdate.setOnClickListener {
            startActivity(
                Intent(
                    Intent.ACTION_VIEW,
                    Uri.parse("https://play.google.com/store/apps/details?id=${packageName}")
                )
            )
            finish()
        }

        dialog.show()

    }

    private fun updateMenuSelection(selectedMenuId: Int) {
        // Reset both to unselected
        binding.iconHome.setImageResource(R.drawable.new_home_unselected_svg)
        binding.labelHome.setTextColor(ContextCompat.getColor(this, R.color.gray))
        binding.labelHome.setTypeface(null, Typeface.NORMAL)

        binding.iconExplore.setImageResource(R.drawable.new_explore_unselected_svg)
        binding.labelExplore.setTextColor(ContextCompat.getColor(this, R.color.gray))
        binding.labelExplore.setTypeface(null, Typeface.NORMAL)

        // Now highlight the selected one
        when (selectedMenuId) {
            R.id.menu_home -> {
                binding.iconHome.setImageResource(R.drawable.new_home_selected_svg)
                binding.labelHome.setTextColor(ContextCompat.getColor(this, R.color.rightlife))
                binding.labelHome.setTypeface(null, Typeface.BOLD)
            }

            R.id.menu_explore -> {
                binding.iconExplore.setImageResource(R.drawable.new_explore_selected_svg)
                binding.labelExplore.setTextColor(ContextCompat.getColor(this, R.color.rightlife))
                binding.labelExplore.setTypeface(null, Typeface.BOLD)
            }
        }
    }


    private fun checkTrailEndedAndShowDialog(): Boolean {
        return if (!DashboardChecklistManager.paymentStatus) {
            showTrailEndedBottomSheet()
            false // Return false if condition is true and dialog is shown
        } else {
            if (!DashboardChecklistManager.checklistStatus) {
                DialogUtils.showCheckListQuestionCommonDialog(this)
                false
            } else {
                true // Return true if condition is false
            }
        }
        return true
    }

    private fun showTrailEndedBottomSheet() {
        // Create and configure BottomSheetDialog
        val bottomSheetDialog = BottomSheetDialog(this)

        // Inflate the BottomSheet layout
        val dialogBinding = BottomsheetTrialEndedBinding.inflate(layoutInflater)
        val bottomSheetView = dialogBinding.root

        bottomSheetDialog.setContentView(bottomSheetView)


        bottomSheetDialog.setContentView(bottomSheetView)

        // Set up the animation
        val bottomSheetLayout = bottomSheetView.findViewById<LinearLayout>(R.id.design_bottom_sheet)
        if (bottomSheetLayout != null) {
            val slideUpAnimation: Animation =
                AnimationUtils.loadAnimation(this, R.anim.bottom_sheet_slide_up)
            bottomSheetLayout.animation = slideUpAnimation
        }

        dialogBinding.ivDialogClose.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        dialogBinding.btnExplorePlan.setOnClickListener {
            bottomSheetDialog.dismiss()
            if (NetworkUtils.isInternetAvailable(this)) {
                startActivity(Intent(this, SubscriptionPlanListActivity::class.java).apply {
                    putExtra("SUBSCRIPTION_TYPE", "SUBSCRIPTION_PLAN")
                })
            } else {
                showInternetError()
            }
            //finish()
        }

        bottomSheetDialog.show()
    }

    fun showHeader(show: Boolean) {
        binding.llCountDown.visibility = if (show && isCountDownVisible) View.VISIBLE else View.GONE
    }

    private fun showInternetError() {
        Toast.makeText(this, "No internet connection", Toast.LENGTH_SHORT).show()
    }

    fun showSubsribeLayout(show: Boolean) {
        if (!DashboardChecklistManager.paymentStatus) {
            binding.trialExpiredLayout.trialExpiredLayout.visibility = View.VISIBLE
            isTrialExpired = true
        } else {
            binding.trialExpiredLayout.trialExpiredLayout.visibility = View.GONE
            isTrialExpired = false
        }
    }

    private lateinit var billingClient: BillingClient


    private fun initBillingAndRecover() {

        billingClient = BillingClient.newBuilder(this)
            .enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()
            )
            .setListener { billingResult, purchases ->

                // Optional: React to new purchases here if needed

            }.enablePendingPurchases(
                PendingPurchasesParams.newBuilder()
                    .enableOneTimeProducts()
                    .build()

            )

            .build()



        billingClient.startConnection(object : BillingClientStateListener {

            override fun onBillingSetupFinished(billingResult: BillingResult) {

                if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {

                    // Query in-app purchases

                    recoverInAppPurchases()

                    // Query subscriptions

                    recoverSubscriptions()

                }

            }


            override fun onBillingServiceDisconnected() {

                // Retry connection if needed

            }

        })

    }


    private val TAG = "BillingRecovery"

    private fun recoverInAppPurchases() {
        Log.d(TAG, "Starting recoverInAppPurchases()...")

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.INAPP)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            Log.d(
                TAG,
                "INAPP queryPurchasesAsync() result: code=${billingResult.responseCode}, purchasesCount=${purchases.size}"
            )


            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    Log.d(
                        TAG,
                        "Found INAPP purchase: token=${purchase.purchaseToken}, state=${purchase.purchaseState}, acknowledged=${purchase.isAcknowledged}"
                    )

                    if (purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        Log.d(TAG, "Processing INAPP purchase: ${purchase.purchaseToken}")
                        consumeIfNeeded(purchase)
                    }
                }
            } else {
                Log.d(TAG, "INAPP purchase query failed: ${billingResult.debugMessage}")
            }
        }
    }

    private fun recoverSubscriptions() {
        Log.d(TAG, "Starting recoverSubscriptions()...")

        val params = QueryPurchasesParams.newBuilder()
            .setProductType(BillingClient.ProductType.SUBS)
            .build()

        billingClient.queryPurchasesAsync(params) { billingResult, purchases ->
            Log.d(
                TAG,
                "SUBS queryPurchasesAsync() result: code=${billingResult.responseCode}, purchasesCount=${purchases.size}"
            )

            if (billingResult.responseCode == BillingClient.BillingResponseCode.OK) {
                for (purchase in purchases) {
                    Log.d(
                        TAG,
                        "Found SUBS purchase: token=${purchase.purchaseToken}, state=${purchase.purchaseState}, acknowledged=${purchase.isAcknowledged}"
                    )

                    if (!purchase.isAcknowledged && purchase.purchaseState == Purchase.PurchaseState.PURCHASED) {
                        Log.d(TAG, "Acknowledging subscription: ${purchase.purchaseToken}")
                        acknowledgeSubscription(purchase)
                    } else if (purchase.isAcknowledged) {
                        Log.d(
                            TAG,
                            "Subscription already acknowledged: ${purchase.purchaseToken} — consider updating backend/UI"
                        )
                        updateBackendForPurchase(purchase)
                    }
                }
            } else {
                Log.d(TAG, "SUBS purchase query failed: ${billingResult.debugMessage}")
            }
        }
    }

    private fun consumeIfNeeded(purchase: Purchase) {
        Log.d(TAG, "consumeIfNeeded() called for token=${purchase.purchaseToken}")

        val consumeParams = ConsumeParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.consumeAsync(consumeParams) { result, _ ->
            Log.d(
                TAG,
                "consumeAsync() result: code=${result.responseCode} for token=${purchase.purchaseToken}"
            )

            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Purchase consumed successfully: ${purchase.purchaseToken}")
                updateBackendForPurchase(purchase)
            } else {
                Log.d(TAG, "Failed to consume purchase: ${result.debugMessage}")
            }
        }
    }

    private fun acknowledgeSubscription(purchase: Purchase) {
        Log.d(TAG, "acknowledgeSubscription() called for token=${purchase.purchaseToken}")

        val acknowledgeParams = AcknowledgePurchaseParams.newBuilder()
            .setPurchaseToken(purchase.purchaseToken)
            .build()

        billingClient.acknowledgePurchase(acknowledgeParams) { result ->
            Log.d(
                TAG,
                "acknowledgePurchase() result: code=${result.responseCode} for token=${purchase.purchaseToken}"
            )

            if (result.responseCode == BillingClient.BillingResponseCode.OK) {
                Log.d(TAG, "Subscription acknowledged successfully: ${purchase.purchaseToken}")
                updateBackendForPurchase(purchase)
            } else {
                Log.d(TAG, "Failed to acknowledge subscription: ${result.debugMessage}")
            }
        }
    }

    private fun updateBackendForPurchase(purchase: Purchase) {
        Log.d(
            TAG,
            "Updating backend for purchase: token=${purchase.purchaseToken}, products=${purchase.products}"
        )

        val paymentSuccessRequest = PaymentSuccessRequest().apply {
            planId = purchase.products.firstOrNull() ?: ""
            planName = purchase.products.firstOrNull() ?: ""
            paymentGateway = "googlePlay"
            orderId = purchase.orderId ?: ""
            environment = "payment"
            notifyType = "SDK"
            couponId = ""
            obfuscatedExternalAccountId = ""
            price = purchase.products.firstOrNull() ?: ""

            sdkDetail = SdkDetail().apply {
                price = ""
                orderId = purchase.orderId ?: ""
                title = ""
                environment = "payment"
                description = ""
                currencyCode = "INR"
                currencySymbol = "₹"
            }
        }

        saveSubscriptionSuccess(paymentSuccessRequest)
    }

    private fun saveSubscriptionSuccess(paymentSuccessRequest: PaymentSuccessRequest) {
        Log.d(
            TAG,
            "Calling saveSubscriptionSuccess() with planId=${paymentSuccessRequest.planId}, orderId=${paymentSuccessRequest.orderId}"
        )

        val call = apiService.savePaymentSuccess(
            sharedPreferenceManager.accessToken,
            paymentSuccessRequest
        )
        call.enqueue(object : Callback<PaymentSuccessResponse> {
            override fun onResponse(
                call: Call<PaymentSuccessResponse>,
                response: Response<PaymentSuccessResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    Log.d(TAG, "savePaymentSuccess() success: ${response.body()}")
                } else {
                    Log.d(
                        TAG,
                        "savePaymentSuccess() failed: code=${response.code()}, message=${response.message()}"
                    )
                }
            }

            override fun onFailure(call: Call<PaymentSuccessResponse>, t: Throwable) {
                Log.d(
                    TAG,
                    "savePaymentSuccess() failed due to network or server error: ${t.localizedMessage}"
                )
                handleNoInternetView(t)
            }
        })
    }


    fun showSwitchAccountDialog(context: Context, header: String, htmlText: String) {
        val dialog = Dialog(context)
        val binding = DialogSwitchAccountBinding.inflate(LayoutInflater.from(context))
        dialog.setContentView(binding.root)
        dialog.setCancelable(true)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        val window = dialog.window
        // Set the dim amount
        val layoutParams = window!!.attributes
        layoutParams.dimAmount = 0.7f // Adjust the dim amount (0.0 - 1.0)
        window.attributes = layoutParams
        binding.btnSwitchAccount.text = "Cancel"
        binding.btnSwitchAccount.visibility = View.GONE
        binding.tvTitle.text = "Free Service Already Used on This Device"
        binding.tvDescription.text =
            "Looks like this device has already claimed a free service under another account. To continue, log out and switch to a new device."

        // Handle close button click
        binding.btnOk.setOnClickListener {
            dialog.dismiss()
        }
        binding.btnSwitchAccount.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    public fun fetchHealthDataFromHealthConnect() {
            val availabilityStatus = HealthConnectClient.getSdkStatus(this)
            if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
                healthConnectClient = HealthConnectClient.getOrCreate(this)
                lifecycleScope.launch {
                    requestPermissionsAndReadAllData()
                }
            } else {
                Toast.makeText(this@HomeNewActivity, "Please install or update health connect from the Play Store.", Toast.LENGTH_LONG).show()
            }
    }

    private suspend fun requestPermissionsAndReadAllData() {
        try {
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (allReadPermissions.all { it in granted }) {
                fetchAllHealthData()
            } else {
                requestPermissionsLauncher.launch(allReadPermissions)
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                Toast.makeText(this@HomeNewActivity, "Error checking permissions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
        lifecycleScope.launch {
            if (granted.containsAll(allReadPermissions)) {
                fetchAllHealthData()
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeNewActivity, "Permissions Granted", Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                }
                fetchAllHealthData()
            }
        }
    }

    private suspend fun fetchAllHealthData() {
        try {
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            lifecycleScope.launch {
                if (HealthPermission.getReadPermission(SleepSessionRecord::class) in grantedPermissions) {
                    val response = healthConnectClient.readRecords(
                        ReadRecordsRequest(
                            recordType = SleepSessionRecord::class,
                            timeRangeFilter = TimeRangeFilter.after(Instant.EPOCH)
                        )
                    )
                    for (record in response.records) {
                        val deviceInfo = record.metadata.device
                        if (deviceInfo != null) {
                            SharedPreferenceManager.getInstance(this@HomeNewActivity).saveDeviceName(deviceInfo.manufacturer)
                            Log.d("Device Info", """ Manufacturer: ${deviceInfo.manufacturer}
                Model: ${deviceInfo.model} Type: ${deviceInfo.type} """.trimIndent())
                        } else {
                            Log.d("Device Info", "No device info available")
                        }
                    }
                }
            }
            var endTime = Instant.now()
            var startTime = Instant.now()
            val syncTime = SharedPreferenceManager.getInstance(this@HomeNewActivity).moveRightSyncTime ?: ""
            if (syncTime == "") {
                endTime = Instant.now()
                startTime = endTime.minus(Duration.ofDays(30))
            }else{
                endTime = Instant.now()
                startTime = convertUtcToInstant(syncTime)
            }
            if (HealthPermission.getReadPermission(StepsRecord::class) in grantedPermissions) {
                val stepsResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                if (stepsResponse.records.isEmpty()) {
                    Log.d("HealthData", "No steps data found")
                } else {
                    for (record in stepsResponse.records) {
                        val deviceInfo = record.metadata.device
                        if (deviceInfo != null) {
                            SharedPreferenceManager.getInstance(this@HomeNewActivity).saveDeviceName(deviceInfo.manufacturer)
                            Log.d("Device Info", """ Manufacturer: ${deviceInfo.manufacturer}
                Model: ${deviceInfo.model} Type: ${deviceInfo.type} """.trimIndent())
                        } else {
                            Log.d("Device Info", "No device info available")
                        }
                    }
                    stepsRecord = stepsResponse.records
                }
            } else {
                stepsRecord = emptyList()
                Log.d("HealthData", "Steps permission denied")
            }
            if (HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class) in grantedPermissions) {
                val caloriesResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = TotalCaloriesBurnedRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                totalCaloriesBurnedRecord = caloriesResponse.records
                val totalBurnedCalories = totalCaloriesBurnedRecord?.sumOf { it.energy.inKilocalories.toInt() } ?: 0
                Log.d("HealthData", "Total Burned Calories: $totalBurnedCalories kcal")
            } else {
                totalCaloriesBurnedRecord = emptyList()
                Log.d("HealthData", "Calories permission denied")
            }
            if (HealthPermission.getReadPermission(HeartRateRecord::class) in grantedPermissions) {
                val response = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = HeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                heartRateRecord = response.records
            }else {
                heartRateRecord = emptyList()
                Log.d("HealthData", "Heart rate permission denied")
            }
            if (HealthPermission.getReadPermission(RestingHeartRateRecord::class) in grantedPermissions) {
                val restingHRResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = RestingHeartRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                restingHeartRecord = restingHRResponse.records
                restingHeartRecord?.forEach { record ->
                    Log.d("HealthData", "Resting Heart Rate: ${record.beatsPerMinute} bpm, Time: ${record.time}")
                }
            }else {
                restingHeartRecord = emptyList()
                Log.d("HealthData", "Heart rate permission denied")
            }
            if (HealthPermission.getReadPermission(ActiveCaloriesBurnedRecord::class) in grantedPermissions) {
                val activeCalorieResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = ActiveCaloriesBurnedRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                activeCalorieBurnedRecord = activeCalorieResponse.records
                activeCalorieBurnedRecord?.forEach { record ->
                    Log.d("HealthData", "Resting Heart Rate: ${record.energy} kCal, Time: ${record.startTime}")
                }
            }else {
                activeCalorieBurnedRecord = emptyList()
                Log.d("HealthData", "Heart rate permission denied")
            }
            if (HealthPermission.getReadPermission(BasalMetabolicRateRecord::class) in grantedPermissions) {
                val basalMetabolic = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = BasalMetabolicRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                basalMetabolicRateRecord = basalMetabolic.records
                basalMetabolicRateRecord?.forEach { record ->
                    Log.d("HealthData", "Resting Heart Rate: ${record.basalMetabolicRate}, Time: ${record.time}")
                }
            }else {
                basalMetabolicRateRecord = emptyList()
                Log.d("HealthData", "Heart rate permission denied")
            }
            if (HealthPermission.getReadPermission(BloodPressureRecord::class) in grantedPermissions) {
                val bloodPressure = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = BloodPressureRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                bloodPressureRecord = bloodPressure.records
                bloodPressureRecord?.forEach { record ->
                    Log.d("HealthData", "Resting Heart Rate: ${record.systolic}, Time: ${record.time}")
                }
            }else {
                bloodPressureRecord = emptyList()
                Log.d("HealthData", "Heart rate permission denied")
            }
            if (HealthPermission.getReadPermission(HeartRateVariabilityRmssdRecord::class) in grantedPermissions) {
                val restingVresponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = HeartRateVariabilityRmssdRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                heartRateVariability = restingVresponse.records
                heartRateVariability?.forEach { record ->
                    Log.d("HealthData", "Resting Heart Rate: ${record.heartRateVariabilityMillis}, Time: ${record.time}")
                }
            }else {
                heartRateVariability = emptyList()
                Log.d("HealthData", "Heart rate permission denied")
            }
            if (HealthPermission.getReadPermission(SleepSessionRecord::class) in grantedPermissions) {
                val sleepResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = SleepSessionRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                sleepSessionRecord = sleepResponse.records
                sleepSessionRecord?.forEach { record ->
                    Log.d("HealthData", "Sleep Session: Start: ${record.startTime}, End: ${record.endTime}, Stages: ${record.stages}")
                }
            } else {
                sleepSessionRecord = emptyList()
                Log.d("HealthData", "Sleep session permission denied")
            }
            if (HealthPermission.getReadPermission(ExerciseSessionRecord::class) in grantedPermissions) {
                val exerciseResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = ExerciseSessionRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                exerciseSessionRecord = exerciseResponse.records
                exerciseSessionRecord?.forEach { record ->
                    Log.d("HealthData", "Exercise Session: Type: ${record.exerciseType}, Start: ${record.startTime}, End: ${record.endTime}")
                }
            } else {
                exerciseSessionRecord = emptyList()
                Log.d("HealthData", "Exercise session permission denied")
            }
            if (HealthPermission.getReadPermission(SpeedRecord::class) in grantedPermissions) {
                val speedResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = SpeedRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                speedRecord = speedResponse.records
                speedRecord?.forEach { record ->
                    Log.d("HealthData", "Speed: ${record.samples.joinToString { it.speed.inMetersPerSecond.toString() }} m/s")
                }
            } else {
                speedRecord = emptyList()
                Log.d("HealthData", "Speed permission denied")
            }
            if (HealthPermission.getReadPermission(WeightRecord::class) in grantedPermissions) {
                val weightResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = WeightRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                weightRecord = weightResponse.records
                weightRecord?.forEach { record ->
                    Log.d("HealthData", "Weight: ${record.weight.inKilograms} kg, Time: ${record.time}")
                }
            } else {
                weightRecord = emptyList()
                Log.d("HealthData", "Weight permission denied")
            }
            if (HealthPermission.getReadPermission(BodyFatRecord::class) in grantedPermissions) {
                val bodyFatResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = BodyFatRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                bodyFatRecord = bodyFatResponse.records
                bodyFatRecord?.forEach { record ->
                    Log.d("HealthData", "Body Fat: ${record.percentage.value * 100}%, Time: ${record.time}")
                }
            } else {
                bodyFatRecord = emptyList()
                Log.d("HealthData", "Weight permission denied")
            }
            if (HealthPermission.getReadPermission(DistanceRecord::class) in grantedPermissions) {
                val distanceResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = DistanceRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                distanceRecord = distanceResponse.records
                val totalDistance = distanceRecord?.sumOf { it.distance.inMeters } ?: 0.0
                Log.d("HealthData", "Total Distance: $totalDistance meters")
            } else {
                distanceRecord = emptyList()
                Log.d("HealthData", "Distance permission denied")
            }
            if (HealthPermission.getReadPermission(OxygenSaturationRecord::class) in grantedPermissions) {
                val oxygenSaturationResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = OxygenSaturationRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                oxygenSaturationRecord = oxygenSaturationResponse.records
                oxygenSaturationRecord?.forEach { record ->
                    Log.d("HealthData", "Oxygen Saturation: ${record.percentage.value}%, Time: ${record.time}")
                }
            } else {
                oxygenSaturationRecord = emptyList()
                Log.d("HealthData", "Oxygen saturation permission denied")
            }
            if (HealthPermission.getReadPermission(RespiratoryRateRecord::class) in grantedPermissions) {
                val respiratoryRateResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = RespiratoryRateRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                respiratoryRateRecord = respiratoryRateResponse.records
                respiratoryRateRecord?.forEach { record ->
                    Log.d("HealthData", "Respiratory Rate: ${record.rate} breaths/min, Time: ${record.time}")
                }
            } else {
                respiratoryRateRecord = emptyList()
                Log.d("HealthData", "Respiratory rate permission denied")
            }
            var dataOrigin = ""
            if (HealthPermission.getReadPermission(StepsRecord::class) in grantedPermissions) {
                val stepsResponse = healthConnectClient.readRecords(
                    ReadRecordsRequest(
                        recordType = StepsRecord::class,
                        timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                    )
                )
                for (record in stepsResponse.records) {
                    dataOrigin = record.metadata.dataOrigin.packageName
                    val deviceInfo = record.metadata.device
                    if (deviceInfo != null) {
                        SharedPreferenceManager.getInstance(this@HomeNewActivity).saveDeviceName(deviceInfo.manufacturer)
                        Log.d("Device Info", """ Manufacturer: ${deviceInfo.manufacturer}
                Model: ${deviceInfo.model} Type: ${deviceInfo.type} """.trimIndent())
                    } else {
                        Log.d("Device Info", "No device info available")
                    }
                }
            }
            if (dataOrigin.equals("com.google.android.apps.fitness")){
                storeHealthData()
            }else if(dataOrigin.equals("com.sec.android.app.shealth")){
                storeSamsungHealthData()
            }else if(dataOrigin.equals("com.samsung.android.wear.shealth")){
                storeSamsungHealthData()
            }else{
                storeHealthData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(this@HomeNewActivity, "Error fetching health data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun storeHealthData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userid = SharedPreferenceManager.getInstance(this@HomeNewActivity).userId
                val activeEnergyBurned = totalCaloriesBurnedRecord?.mapNotNull { record ->
                    if (record.energy.inKilocalories > 0) {
                        EnergyBurnedRequest(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            record_type = "ActiveEnergyBurned",
                            unit = "kcal",
                            value = record.energy.inKilocalories.toString(),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                        )
                    } else null
                } ?: emptyList()
                val basalEnergyBurned = basalMetabolicRateRecord?.map { record ->
                    EnergyBurnedRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BasalMetabolic",
                        unit = "power",
                        value = record.basalMetabolicRate.toString(),
                        source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                    )
                } ?: emptyList()
                val distanceWalkingRunning = distanceRecord?.mapNotNull { record ->
                    if (record.distance.inKilometers > 0) {
                        Distance(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            record_type = "DistanceWalkingRunning",
                            unit = "km",
                            value = String.format("%.2f", record.distance.inKilometers),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                        )
                    } else null
                } ?: emptyList()
                val stepCount = stepsRecord?.mapNotNull { record ->
                    if (record.count > 0) {
                        StepCountRequest(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            record_type = "StepCount",
                            unit = "count",
                            value = record.count.toString(),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                        )
                    } else null
                } ?: emptyList()
                val heartRate = heartRateRecord?.flatMap { record ->
                    record.samples.mapNotNull { sample ->
                        if (sample.beatsPerMinute > 0) {
                            HeartRateRequest(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "HeartRate",
                                unit = "bpm",
                                value = sample.beatsPerMinute.toInt().toString(),
                                source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                            )
                        } else null
                    }
                } ?: emptyList()
                val heartRateVariability = heartRateVariability?.map { record ->
                    HeartRateVariabilityRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "HeartRateVariability",
                        unit = "double",
                        value = record.heartRateVariabilityMillis.toString(),
                        source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                    )
                } ?: emptyList()
                val restingHeartRate = restingHeartRecord?.map { record ->
                    HeartRateRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "RestingHeartRate",
                        unit = "bpm",
                        value = record.beatsPerMinute.toString(),
                        source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                    )
                } ?: emptyList()
                val respiratoryRate = respiratoryRateRecord?.mapNotNull { record ->
                    if (record.rate > 0) {
                        RespiratoryRate(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "RespiratoryRate",
                            unit = "breaths/min",
                            value = String.format("%.1f", record.rate),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                        )
                    } else null
                } ?: emptyList()
                val oxygenSaturation = oxygenSaturationRecord?.mapNotNull { record ->
                    if (record.percentage.value > 0) {
                        OxygenSaturation(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "OxygenSaturation",
                            unit = "%",
                            value = String.format("%.1f", record.percentage.value),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                        )
                    } else null
                } ?: emptyList()
                val bloodPressureSystolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BloodPressureSystolic",
                        unit = "millimeterOfMercury",
                        value = record.systolic.inMillimetersOfMercury.toString(),
                        source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                    )
                } ?: emptyList()
                val bloodPressureDiastolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BloodPressureDiastolic",
                        unit = "millimeterOfMercury",
                        value = record.diastolic.inMillimetersOfMercury.toString(),
                        source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                    )
                } ?: emptyList()
                val bodyMass = weightRecord?.mapNotNull { record ->
                    if (record.weight.inKilograms > 0) {
                        BodyMass(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "BodyMass",
                            unit = "kg",
                            value = String.format("%.1f", record.weight.inKilograms),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                        )
                    } else null
                } ?: emptyList()
                val bodyFatPercentage = bodyFatRecord?.mapNotNull { record ->
                    BodyFatPercentage(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BodyFat",
                        unit = "percentage",
                        value = String.format("%.1f", record.percentage),
                        source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                    )
                } ?: emptyList()
                val sleepStage = sleepSessionRecord?.flatMap { record ->
                    record.stages.mapNotNull { stage ->
                        val stageValue = when (stage.stage) {
                            SleepSessionRecord.STAGE_TYPE_DEEP -> "Deep Sleep"
                            SleepSessionRecord.STAGE_TYPE_LIGHT -> "Light Sleep"
                            SleepSessionRecord.STAGE_TYPE_REM -> "REM Sleep"
                            SleepSessionRecord.STAGE_TYPE_AWAKE -> "Awake"
                            else -> null
                        }
                        stageValue?.let {
                            SleepStageJson(
                                start_datetime = convertToTargetFormat(stage.startTime.toString()),
                                end_datetime = convertToTargetFormat(stage.endTime.toString()),
                                record_type = it,
                                unit = "sleep_stage",
                                value = it,
                                source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName
                            )
                        }
                    }
                } ?: emptyList()
                val workout = exerciseSessionRecord?.mapNotNull { record ->
                    val workoutType = when (record.exerciseType) {
                        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Running"
                        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Walking"
                        else -> "Other"
                    }
                    val calories = totalCaloriesBurnedRecord?.filter {
                        it.startTime >= record.startTime && it.endTime <= record.endTime
                    }?.sumOf { it.energy.inKilocalories.toInt() } ?: 0
                    val distance = record.metadata.dataOrigin?.let { 5.0 } ?: 0.0
                    if (calories > 0) {
                        WorkoutRequest(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ,
                            record_type = "Workout",
                            workout_type = workoutType,
                            duration = ((record.endTime.toEpochMilli() - record.startTime.toEpochMilli()) / 1000 / 60).toString(),
                            calories_burned = calories.toString(),
                            distance = String.format("%.1f", distance),
                            duration_unit = "minutes",
                            calories_unit = "kcal",
                            distance_unit = "km"
                        )
                    } else null
                } ?: emptyList()
                val request = StoreHealthDataRequest(
                    user_id = userid,
                    source = "android",
                    active_energy_burned = activeEnergyBurned,
                    basal_energy_burned = basalEnergyBurned,
                    distance_walking_running = distanceWalkingRunning,
                    step_count = stepCount,
                    heart_rate = heartRate,
                    heart_rate_variability_SDNN = heartRateVariability,
                    resting_heart_rate = restingHeartRate,
                    respiratory_rate = respiratoryRate,
                    oxygen_saturation = oxygenSaturation,
                    blood_pressure_systolic = bloodPressureSystolic,
                    blood_pressure_diastolic = bloodPressureDiastolic,
                    body_mass = bodyMass,
                    body_fat_percentage = bodyFatPercentage,
                    sleep_stage = sleepStage,
                    workout = workout
                )
                val response = com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient.apiServiceFastApi.storeHealthData(request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val todaysTime = Instant.now()
                        val syncTime = ZonedDateTime.parse(todaysTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
                        SharedPreferenceManager.getInstance(this@HomeNewActivity).saveMoveRightSyncTime(syncTime.toString())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeNewActivity, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()

                }
            }
        }
    }

    private fun storeSamsungHealthData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userid = SharedPreferenceManager.getInstance(this@HomeNewActivity).userId
                val activeEnergyBurned = totalCaloriesBurnedRecord?.mapNotNull { record ->
                    if (record.energy.inKilocalories > 0) {
                        EnergyBurnedRequest(
                            start_datetime = convertToSamsungFormat(record.startTime.toString()),
                            end_datetime = convertToSamsungFormat(record.endTime.toString()),
                            record_type = "ActiveEnergyBurned",
                            unit = "kcal",
                            value = record.energy.inKilocalories.toString(),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                        )
                    } else null
                } ?: emptyList()
                val basalEnergyBurned = basalMetabolicRateRecord?.map { record ->
                    EnergyBurnedRequest(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BasalMetabolic",
                        unit = "power",
                        value = record.basalMetabolicRate.toString(),
                        source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                    )
                } ?: emptyList()
                val distanceWalkingRunning = distanceRecord?.mapNotNull { record ->
                    if (record.distance.inKilometers > 0) {
                        Distance(
                            start_datetime = convertToSamsungFormat(record.startTime.toString()),
                            end_datetime = convertToSamsungFormat(record.endTime.toString()),
                            record_type = "DistanceWalkingRunning",
                            unit = "km",
                            value = String.format("%.2f", record.distance.inKilometers),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                        )
                    } else null
                } ?: emptyList()
                val stepCount = stepsRecord?.mapNotNull { record ->
                    if (record.count > 0) {
                        StepCountRequest(
                            start_datetime = convertToSamsungFormat(record.startTime.toString()),
                            end_datetime = convertToSamsungFormat(record.endTime.toString()),
                            record_type = "StepCount",
                            unit = "count",
                            value = record.count.toString(),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                        )
                    } else null
                } ?: emptyList()
                val heartRate = heartRateRecord?.flatMap { record ->
                    record.samples.mapNotNull { sample ->
                        if (sample.beatsPerMinute > 0) {
                            HeartRateRequest(
                                start_datetime = convertToSamsungFormat(record.startTime.toString()),
                                end_datetime = convertToSamsungFormat(record.endTime.toString()),
                                record_type = "HeartRate",
                                unit = "bpm",
                                value = sample.beatsPerMinute.toInt().toString(),
                                source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                            )
                        } else null
                    }
                } ?: emptyList()
                val heartRateVariability = heartRateVariability?.map { record ->
                    HeartRateVariabilityRequest(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "HeartRateVariability",
                        unit = "double",
                        value = record.heartRateVariabilityMillis.toString(),
                        source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                    )
                } ?: emptyList()
                val restingHeartRate = restingHeartRecord?.map { record ->
                    HeartRateRequest(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "RestingHeartRate",
                        unit = "bpm",
                        value = record.beatsPerMinute.toString(),
                        source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                    )
                } ?: emptyList()
                val respiratoryRate = respiratoryRateRecord?.mapNotNull { record ->
                    if (record.rate > 0) {
                        RespiratoryRate(
                            start_datetime = convertToSamsungFormat(record.time.toString()),
                            end_datetime = convertToSamsungFormat(record.time.toString()),
                            record_type = "RespiratoryRate",
                            unit = "breaths/min",
                            value = String.format("%.1f", record.rate),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                        )
                    } else null
                } ?: emptyList()
                val oxygenSaturation = oxygenSaturationRecord?.mapNotNull { record ->
                    if (record.percentage.value > 0) {
                        OxygenSaturation(
                            start_datetime = convertToSamsungFormat(record.time.toString()),
                            end_datetime = convertToSamsungFormat(record.time.toString()),
                            record_type = "OxygenSaturation",
                            unit = "%",
                            value = String.format("%.1f", record.percentage.value),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                        )
                    } else null
                } ?: emptyList()
                val bloodPressureSystolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BloodPressureSystolic",
                        unit = "millimeterOfMercury",
                        value = record.systolic.inMillimetersOfMercury.toString(),
                        source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                    )
                } ?: emptyList()
                val bloodPressureDiastolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BloodPressureDiastolic",
                        unit = "millimeterOfMercury",
                        value = record.diastolic.inMillimetersOfMercury.toString(),
                        source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                    )
                } ?: emptyList()
                val bodyMass = weightRecord?.mapNotNull { record ->
                    if (record.weight.inKilograms > 0) {
                        BodyMass(
                            start_datetime = convertToSamsungFormat(record.time.toString()),
                            end_datetime = convertToSamsungFormat(record.time.toString()),
                            record_type = "BodyMass",
                            unit = "kg",
                            value = String.format("%.1f", record.weight.inKilograms),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                        )
                    } else null
                } ?: emptyList()
                val bodyFatPercentage = bodyFatRecord?.mapNotNull { record ->
                    BodyFatPercentage(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BodyFat",
                        unit = "percentage",
                        value = String.format("%.1f", record.percentage),
                        source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                    )
                } ?: emptyList()
                val sleepStage = sleepSessionRecord?.flatMap { record ->
                    record.stages.mapNotNull { stage ->
                        val stageValue = when (stage.stage) {
                            SleepSessionRecord.STAGE_TYPE_DEEP -> "Deep Sleep"
                            SleepSessionRecord.STAGE_TYPE_LIGHT -> "Light Sleep"
                            SleepSessionRecord.STAGE_TYPE_REM -> "REM Sleep"
                            SleepSessionRecord.STAGE_TYPE_AWAKE -> "Awake"
                            else -> null
                        }
                        stageValue?.let {
                            SleepStageJson(
                                start_datetime = convertToSamsungFormat(stage.startTime.toString()),
                                end_datetime = convertToSamsungFormat(stage.endTime.toString()),
                                record_type = it,
                                unit = "sleep_stage",
                                value = it,
                                source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung"
                            )
                        }
                    }
                } ?: emptyList()
                val workout = exerciseSessionRecord?.mapNotNull { record ->
                    val workoutType = when (record.exerciseType) {
                        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Running"
                        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Walking"
                        else -> "Other"
                    }
                    val calories = totalCaloriesBurnedRecord?.filter {
                        it.startTime >= record.startTime && it.endTime <= record.endTime
                    }?.sumOf { it.energy.inKilocalories.toInt() } ?: 0
                    val distance = record.metadata.dataOrigin?.let { 5.0 } ?: 0.0
                    if (calories > 0) {
                        WorkoutRequest(
                            start_datetime = convertToSamsungFormat(record.startTime.toString()),
                            end_datetime = convertToSamsungFormat(record.endTime.toString()),
                            source_name = SharedPreferenceManager.getInstance(this@HomeNewActivity).deviceName ?: "samsung",
                            record_type = "Workout",
                            workout_type = workoutType,
                            duration = ((record.endTime.toEpochMilli() - record.startTime.toEpochMilli()) / 1000 / 60).toString(),
                            calories_burned = calories.toString(),
                            distance = String.format("%.1f", distance),
                            duration_unit = "minutes",
                            calories_unit = "kcal",
                            distance_unit = "km"
                        )
                    } else null
                } ?: emptyList()
                val request = StoreHealthDataRequest(
                    user_id = userid,
                    source = "android",
                    active_energy_burned = activeEnergyBurned,
                    basal_energy_burned = basalEnergyBurned,
                    distance_walking_running = distanceWalkingRunning,
                    step_count = stepCount,
                    heart_rate = heartRate,
                    heart_rate_variability_SDNN = heartRateVariability,
                    resting_heart_rate = restingHeartRate,
                    respiratory_rate = respiratoryRate,
                    oxygen_saturation = oxygenSaturation,
                    blood_pressure_systolic = bloodPressureSystolic,
                    blood_pressure_diastolic = bloodPressureDiastolic,
                    body_mass = bodyMass,
                    body_fat_percentage = bodyFatPercentage,
                    sleep_stage = sleepStage,
                    workout = workout
                )
                val response = com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient.apiServiceFastApi.storeHealthData(request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val todaysTime = Instant.now()
                        val syncTime = ZonedDateTime.parse(todaysTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
                        SharedPreferenceManager.getInstance(this@HomeNewActivity).saveMoveRightSyncTime(syncTime.toString())
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeNewActivity, "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    fun convertUtcToInstant(utcString: String): Instant {
        return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(utcString))
    }

    fun convertToSamsungFormat(input: String): String {
        val possibleFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ssX",         // ISO with timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",     // ISO with milliseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX", // ISO with nanoseconds
            "yyyy-MM-dd HH:mm:ss",            // Common DB format
            "yyyy/MM/dd HH:mm:ss",            // Slash format
            "dd-MM-yyyy HH:mm:ss",            // Day-Month-Year
            "MM/dd/yyyy HH:mm:ss",            // US format
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )

        // Check if it's a nanosecond timestamp
        if (input.matches(Regex("^\\d{18,}$"))) {
            return try {
                val nanos = input.toLong()
                val seconds = nanos / 1_000_000_000
                val nanoAdjustment = (nanos % 1_000_000_000).toInt()
                val instant = Instant.ofEpochSecond(seconds, nanoAdjustment.toLong())
                val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(
                    ZoneOffset.UTC)
                targetFormatter.format(instant)
            } catch (e: Exception) {
                ""
            }
        }

        // Try known patterns
        for (pattern in possibleFormats) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern)

                return if (pattern.contains("X") || pattern.contains("'Z'")) {
                    // Pattern has timezone — parse as instant
                    val temporal = formatter.parse(input)
                    val instant = Instant.from(temporal)
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .withZone(ZoneOffset.UTC)
                        .format(instant)
                } else {
                    // No timezone info — treat as local time and convert to UTC
                    val localDateTime = LocalDateTime.parse(input, formatter)
                    val zonedDateTime = localDateTime.atZone(ZoneId.systemDefault())
                    val utcDateTime = zonedDateTime.withZoneSameInstant(ZoneOffset.UTC)
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                        .withZone(ZoneOffset.UTC)
                        .format(utcDateTime)
                }

            } catch (e: DateTimeParseException) {
                // Try next format
            }
        }

        return "" // Unable to parse
    }

    fun convertToTargetFormat(input: String): String {
        val possibleFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ssX",        // ISO with timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",    // ISO with milliseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX", // ISO with nanoseconds
            "yyyy-MM-dd HH:mm:ss",           // Common DB format
            "yyyy/MM/dd HH:mm:ss",           // Slash format
            "dd-MM-yyyy HH:mm:ss",           // Day-Month-Year
            "MM/dd/yyyy HH:mm:ss",            // US format
            "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"
        )

        // Check if it's a nanosecond timestamp (very large number)
        if (input.matches(Regex("^\\d{18,}$"))) {
            return try {
                val nanos = input.toLong()
                val seconds = nanos / 1_000_000_000
                val nanoAdjustment = (nanos % 1_000_000_000).toInt()
                val instant = Instant.ofEpochSecond(seconds, nanoAdjustment.toLong())
                val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
                return targetFormatter.format(instant)
            } catch (e: Exception) {
                ""
            }
        }

        // Try known patterns
        for (pattern in possibleFormats) {
            try {
                val formatter = DateTimeFormatter.ofPattern(pattern).withZone(ZoneOffset.UTC)
                val temporal = formatter.parse(input)
                val instant = Instant.from(temporal)
                val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
                return targetFormatter.format(instant)
            } catch (e: DateTimeParseException) {
                // Try next format
            }
        }

        return "" // Unable to parse
    }
}
