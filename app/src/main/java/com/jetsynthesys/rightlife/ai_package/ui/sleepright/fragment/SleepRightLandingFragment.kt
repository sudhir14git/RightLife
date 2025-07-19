package com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment

import android.Manifest
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.util.AttributeSet
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
import androidx.annotation.RequiresPermission
import androidx.cardview.widget.CardView
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
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
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.SleepLandingResponse
import com.jetsynthesys.rightlife.databinding.FragmentSleepRightLandingBinding
import com.jetsynthesys.rightlife.ui.NewSleepSounds.NewSleepSoundActivity
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jetsynthesys.rightlife.ai_package.model.BloodPressure
import com.jetsynthesys.rightlife.ai_package.model.BodyFatPercentage
import com.jetsynthesys.rightlife.ai_package.model.BodyMass
import com.jetsynthesys.rightlife.ai_package.model.Distance
import com.jetsynthesys.rightlife.ai_package.model.EnergyBurnedRequest
import com.jetsynthesys.rightlife.ai_package.model.HeartRateRequest
import com.jetsynthesys.rightlife.ai_package.model.HeartRateVariabilityRequest
import com.jetsynthesys.rightlife.ai_package.model.OxygenSaturation
import com.jetsynthesys.rightlife.ai_package.model.RespiratoryRate
import com.jetsynthesys.rightlife.ai_package.model.SleepConsistency
import com.jetsynthesys.rightlife.ai_package.model.SleepDetails
import com.jetsynthesys.rightlife.ai_package.model.SleepJson
import com.jetsynthesys.rightlife.ai_package.model.SleepJsonRequest
import com.jetsynthesys.rightlife.ai_package.model.SleepLandingAllData
import com.jetsynthesys.rightlife.ai_package.model.SleepPerformanceDetail
import com.jetsynthesys.rightlife.ai_package.model.SleepRestorativeDetail
import com.jetsynthesys.rightlife.ai_package.model.SleepStageJson
import com.jetsynthesys.rightlife.ai_package.model.SleepStagesData
import com.jetsynthesys.rightlife.ai_package.model.StepCountRequest
import com.jetsynthesys.rightlife.ai_package.model.StoreHealthDataRequest
import com.jetsynthesys.rightlife.ai_package.model.ThinkRecomendedResponse
import com.jetsynthesys.rightlife.ai_package.model.WakeupData
import com.jetsynthesys.rightlife.ai_package.model.WakeupTimeResponse
import com.jetsynthesys.rightlife.ai_package.model.WorkoutRequest
import com.jetsynthesys.rightlife.ai_package.model.response.SleepSoundResponse
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.adapter.RecommendedAdapterSleep
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.RestorativeSleepFragment.MultilineXAxisRenderer
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.fragment.SleepInfoDialogFragment
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import kotlin.math.max
import java.time.ZoneId
import kotlin.math.roundToInt

class SleepRightLandingFragment : BaseFragment<FragmentSleepRightLandingBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSleepRightLandingBinding
        get() = FragmentSleepRightLandingBinding::inflate

    private lateinit var todaysSleepRequirement: TextView
    private lateinit var tv_todays_sleep_time_requirement_top: TextView
    private lateinit var todaysSleepStartTime: TextView
    private lateinit var tv_todays_sleep_start_time_top: TextView
    private lateinit var tvStageSleepStartTime: TextView
    private lateinit var tvStageSleepEndTime: TextView
    private lateinit var tvStageSleepTotalTime: TextView
    private lateinit var tvStageCoreTime: TextView
    private lateinit var tvStageDeepTime: TextView
    private lateinit var tvStageRemTime: TextView
    private lateinit var tvStageAwakeTime: TextView
    private lateinit var todaysWakeupTime: TextView
    private lateinit var tvPerformStartTime: TextView
    private lateinit var tvPerformWakeTime: TextView
    private lateinit var tvPerformSleepPercent: TextView
    private lateinit var tvPerformSleepDuration: TextView
    private lateinit var tvPerformIdealDuration: TextView
    private lateinit var imgPerformAction: ImageView
    private lateinit var tvPerformAction: TextView
    private lateinit var tvPerformMessage: TextView
    private lateinit var tvIdealActualDate: TextView
    private lateinit var tvActualTime: TextView
    private lateinit var tvIdealTime: TextView
    private lateinit var tv_todays_wakeup_time_top: TextView
    private lateinit var tvRestoRemTime: TextView
    private lateinit var tvRestoDeepTime: TextView
    private lateinit var tvRestoStartTime: TextView
    private lateinit var tvRestoEndTime: TextView
    private lateinit var tvConsistencyDate: TextView
    private lateinit var tvConsistencyTime: TextView
    private lateinit var landingPageResponse: SleepLandingResponse
    private lateinit var landingAllData: SleepLandingAllData
    private lateinit var lineChart:LineChart
    private val remData: ArrayList<Float> = arrayListOf()
    private val awakeData: ArrayList<Float> = arrayListOf()
    private val coreData: ArrayList<Float> = arrayListOf()
    private val deepData: ArrayList<Float> = arrayListOf()
    private val formatters = DateTimeFormatter.ISO_DATE_TIME
    private lateinit var wakeupTimeResponse: WakeupTimeResponse
    private lateinit var sleepStagesView: SleepChartViewLanding
    private lateinit var sleepConsistencyChart: SleepGraphView
    private lateinit var sleepSoundResponse: SleepSoundResponse
    private lateinit var logYourNap : LinearLayout
    private lateinit var actualNoDataCardView : CardView
    private lateinit var cardSleepTimeRequirementTop : CardView
    private lateinit var sleepStageCardView : CardView
    private lateinit var stageNoDataCardView : CardView
    private lateinit var performCardView : CardView
    private lateinit var headerTop : LinearLayout
    private lateinit var imgIdealInfoTop : ImageView
    private lateinit var img_edit_wakeup_time_top : ImageView
    private lateinit var tvRequirementTop : TextView
    private lateinit var sleepTimeRequirementCardView : CardView
    private lateinit var performNoDataCardView : CardView
    private lateinit var restroDataCardView : CardView
    private lateinit var restroNoDataCardView : CardView
    private lateinit var consistencyNoDataCardView : CardView
    private lateinit var sleepSoundCardView : CardView
    private lateinit var mainView : LinearLayout
    private lateinit var downloadView: ImageView
    private lateinit var soundPlay1: ImageView
    private lateinit var soundPlay2: ImageView
    private lateinit var soundPlay3: ImageView
    private lateinit var sleepArrowView: ImageView
    private lateinit var sleepPerformView: ImageView
    private lateinit var imgSleepInfo: ImageView
    private lateinit var imgIdealInfo: ImageView
    private lateinit var sleepIdeal : ImageView
    private lateinit var consistencySleep : ImageView
    private lateinit var restorativeChart: SleepRestoChartView
    private var sleepSessionRecord: List<SleepSessionRecord>? = null
    private var totalCaloriesBurnedRecord: List<TotalCaloriesBurnedRecord>? = null
    private var activeCalorieBurnedRecord: List<ActiveCaloriesBurnedRecord>? = null
    private var stepsRecord: List<StepsRecord>? = null
    private var heartRateRecord: List<HeartRateRecord>? = null
    private var heartRateVariability: List<HeartRateVariabilityRmssdRecord>? = null
    private var restingHeartRecord: List<RestingHeartRateRecord>? = null
    private var basalMetabolicRateRecord: List<BasalMetabolicRateRecord>? = null
    private var bloodPressureRecord: List<BloodPressureRecord>? = null
    private var exerciseSessionRecord: List<ExerciseSessionRecord>? = null
    private var speedRecord: List<SpeedRecord>? = null
    private var weightRecord: List<WeightRecord>? = null
    private var distanceRecord: List<DistanceRecord>? = null
    private var bodyFatRecord: List<BodyFatRecord>? = null
    private var oxygenSaturationRecord: List<OxygenSaturationRecord>? = null
    private var respiratoryRateRecord: List<RespiratoryRateRecord>? = null
    private lateinit var healthConnectClient: HealthConnectClient
    private lateinit var btnSync : LinearLayout
    private lateinit var btnSleepSound : LinearLayout
    private var mWakeupTime = ""
    private var mRecordId = ""
    private var bottomSeatName = ""
    private var loadingOverlay : FrameLayout? = null
    private var mEditWakeTime = ""
    private lateinit var recomendationRecyclerView: RecyclerView
    private lateinit var thinkRecomendedResponse : ThinkRecomendedResponse
    private lateinit var recomendationAdapter: RecommendedAdapterSleep
    private var isRepeat : Boolean = false

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

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        bottomSeatName = arguments?.getString("BottomSeatName").toString()

        val sleepChart = view.findViewById<LineChart>(R.id.sleepChart)
        sleepArrowView = view.findViewById(R.id.img_sleep_arrow)
        sleepPerformView = view.findViewById(R.id.img_sleep_perform_arrow)
        btnSync = view.findViewById(R.id.lyt_sync_with_health)
        cardSleepTimeRequirementTop = view.findViewById(R.id.card_sleep_time_requirement_top)
        btnSleepSound = view.findViewById(R.id.btn_sleep_sound)
        headerTop = view.findViewById(R.id.header_top)
        img_edit_wakeup_time_top = view.findViewById(R.id.img_edit_wakeup_time_top)
        tv_todays_wakeup_time_top = view.findViewById(R.id.tv_todays_wakeup_time_top)
        tv_todays_sleep_time_requirement_top = view.findViewById(R.id.tv_todays_sleep_time_requirement_top)
        imgIdealInfoTop = view.findViewById(R.id.img_ideal_info_top)
        tvRequirementTop = view.findViewById(R.id.tv_requirement_top)
        sleepSoundCardView = view.findViewById(R.id.lyt_sleep_sound_card)
        soundPlay1 = view.findViewById(R.id.sound_play_1)
        soundPlay2 = view.findViewById(R.id.sound_play_2)
        soundPlay3 = view.findViewById(R.id.sound_play_3)
        lineChart = view.findViewById(R.id.sleepIdealActualChart)
        imgIdealInfo = view.findViewById(R.id.img_ideal_info)
        sleepStageCardView = view.findViewById(R.id.sleep_stage_layout)
        recomendationRecyclerView = view.findViewById(R.id.recommendationRecyclerView)
        val backButton = view.findViewById<ImageView>(R.id.img_back)
        sleepConsistencyChart = view.findViewById(R.id.sleepConsistencyChart)
        downloadView = view.findViewById(R.id.sleep_download_icon)
        mainView = view.findViewById(R.id.lyt_main_view)
        logYourNap = view.findViewById(R.id.btn_log_nap)
        sleepTimeRequirementCardView = view.findViewById(R.id.card_sleep_time_requirement)
        val sleepStageInfo = view.findViewById<ImageView>(R.id.img_sleep_right)
        val editWakeup = view.findViewById<ImageView>(R.id.img_edit_wakeup_time)
        val sleepPerform = view.findViewById<ImageView>(R.id.img_sleep_perform_right)
        sleepIdeal = view.findViewById(R.id.img_sleep_ideal_actual)
        val restoSleep = view.findViewById<ImageView>(R.id.img_resto_sleep)
        val restoSleepNoData = view.findViewById<ImageView>(R.id.img_resto_sleep_nodata)
        consistencySleep = view.findViewById(R.id.img_consistency_right)
        val openConsistency = view.findViewById<ImageView>(R.id.consistency_right_arrow)
        actualNoDataCardView = view.findViewById(R.id.ideal_actual_nodata_layout)
        restroNoDataCardView = view.findViewById(R.id.restro_nodata_layout)
        restroDataCardView = view.findViewById(R.id.lyt_restorative_card)
        performNoDataCardView = view.findViewById(R.id.perform_nodata_layout)
        performCardView = view.findViewById(R.id.sleep_perform_layout)
        stageNoDataCardView = view.findViewById(R.id.lyt_sleep_stage_no_data)
        consistencyNoDataCardView = view.findViewById(R.id.consistency_nodata_layout)
        todaysWakeupTime = view.findViewById(R.id.tv_todays_wakeup_time)
        todaysSleepRequirement = view.findViewById(R.id.tv_todays_sleep_time_requirement)
        todaysSleepStartTime = view.findViewById(R.id.tv_todays_sleep_start_time)
        tv_todays_sleep_start_time_top = view.findViewById(R.id.tv_todays_sleep_start_time_top)
        tvStageSleepStartTime = view.findViewById(R.id.tv_stage_start_sleep_time)
        tvStageSleepEndTime = view.findViewById(R.id.tv_stage_end_sleep_time)
        tvPerformStartTime = view.findViewById(R.id.tv_perform_start_time)
        tvPerformWakeTime = view.findViewById(R.id.tv_perform_wake_time)
        tvPerformSleepPercent = view.findViewById(R.id.tv_perform_sleep_percent)
        tvPerformSleepDuration = view.findViewById(R.id.tv_perform_sleep_duration)
        tvPerformIdealDuration = view.findViewById(R.id.tv_perform_ideal_duration)
        tvPerformAction = view.findViewById(R.id.tv_perform_action)
        imgPerformAction = view.findViewById(R.id.img_perform_action)
        tvPerformMessage = view.findViewById(R.id.tv_perform_message)
        tvIdealActualDate = view.findViewById(R.id.tv_ideal_actual_date)
        tvActualTime = view.findViewById(R.id.tv_actual_time)
        tvIdealTime = view.findViewById(R.id.tv_ideal_time)
        tvRestoStartTime = view.findViewById(R.id.tv_resto_start_time)
        tvRestoEndTime = view.findViewById(R.id.tv_resto_end_time)
        tvRestoRemTime = view.findViewById(R.id.tv_resto_rem_time)
        tvRestoDeepTime = view.findViewById(R.id.tv_resto_deep_time)
        tvConsistencyDate = view.findViewById(R.id.tv_consistency_date)
        tvConsistencyTime = view.findViewById(R.id.tv_consistency_time)
        tvStageSleepTotalTime = view.findViewById(R.id.tv_stage_total_time)
        tvStageRemTime = view.findViewById(R.id.tv_stage_rem_time)
        tvStageCoreTime = view.findViewById(R.id.tv_stage_core_time)
        tvStageDeepTime = view.findViewById(R.id.tv_stage_deep_time)
        tvStageAwakeTime = view.findViewById(R.id.tv_stage_awake_time)
        imgSleepInfo = view.findViewById(R.id.img_sleep_infos)

        fetchThinkRecomendedData()

        if (bottomSeatName.contentEquals("LogLastNightSleep")){
            val dialog = LogYourNapDialogFragment(
                requireContext = requireContext(),
                listener = object : OnLogYourNapSelectedListener {
                    override fun onLogTimeSelected(time: String) {
                        fetchSleepLandingData()
                    }
                }
            )
            dialog.show(parentFragmentManager, "LogYourNapDialogFragment")
        }

        val syncTime = SharedPreferenceManager.getInstance(requireContext()).moveRightSyncTime ?: ""
        if (syncTime != "") {
            btnSync.visibility = View.GONE
        }

        btnSync.setOnClickListener {
            val availabilityStatus = HealthConnectClient.getSdkStatus(requireContext())
            if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
                healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
                lifecycleScope.launch {
                    requestPermissionsAndReadAllData()
                }
            } else {
                Toast.makeText(context, "Please install or update samsung from the Play Store.", Toast.LENGTH_LONG).show()
            }
        }
        btnSleepSound.setOnClickListener {
            startActivity(Intent(requireContext(), NewSleepSoundActivity::class.java).apply {
                //putExtra("PlayList", "PlayList")
            })
        }

        sleepStageInfo.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val stepGoalFragment = SleepStagesFragment()
                val args = Bundle()
                stepGoalFragment.arguments = args
                replace(R.id.flFragment, stepGoalFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        imgIdealInfo.setOnClickListener {
            val dialog = IdealInfoDialogFragment.newInstance()
            dialog.show(parentFragmentManager, "IdealInfoDialogFragment")
        }
        imgIdealInfoTop.setOnClickListener {
            val dialog = IdealInfoDialogFragment.newInstance()
            dialog.show(parentFragmentManager, "IdealInfoDialogFragment")
        }

        downloadView.setOnClickListener {
            saveViewAsPdf(requireContext(),mainView,"SleepRight")
        }

        logYourNap.setOnClickListener {
            val dialog = LogYourNapDialogFragment(
                requireContext = requireContext(),
                listener = object : OnLogYourNapSelectedListener {
                    override fun onLogTimeSelected(time: String) {
                        fetchSleepLandingData()
                    }
                }
            )
            dialog.show(parentFragmentManager, "LogYourNapDialogFragment")
//            showDateTimePicker(requireContext()) { selectedDateTime ->
//                val formatted = selectedDateTime.format(DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a"))
//                Toast.makeText(requireContext(), "Selected: $formatted", Toast.LENGTH_SHORT).show()
//            }
        }

        imgSleepInfo.setOnClickListener {
            val dialog = SleepInfoDialogFragment.newInstance()
            dialog.show(parentFragmentManager, "SleepInfoDialogFragment")
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                requireActivity().finish()
            }
        })

        backButton.setOnClickListener {
            requireActivity().finish()
        }

//        sleepArrowView.setOnClickListener {
//            navigateToFragment(SleepStagesFragment(), "SleepStagesFragment")
//        }

        sleepPerformView.setOnClickListener {
            navigateToFragment(SleepPerformanceFragment(), "SleepPerformanceFragment")
        }

        sleepPerform.setOnClickListener {
            navigateToFragment(SleepPerformanceFragment(), "SleepPerformanceFragment")
        }

        sleepIdeal.setOnClickListener {
            navigateToFragment(SleepIdealActualFragment(), "IdealActualFragment")
        }

        restoSleep.setOnClickListener {
            navigateToFragment(RestorativeSleepFragment(), "RestorativeFragment")
        }

        restoSleepNoData.setOnClickListener {
            navigateToFragment(RestorativeSleepFragment(), "RestorativeFragment")
        }

        consistencySleep.setOnClickListener {
            navigateToFragment(SleepConsistencyFragment(), "SleepConsistencyFragment")
        }
//        openConsistency.setOnClickListener {
//            navigateToFragment(SleepConsistencyFragment(), "SleepConsistencyFragment")
//        }

        if (!isRepeat){
            fetchSleepLandingData()
        }
        fetchWakeupData()

        editWakeup.setOnClickListener {
            openBottomSheet()
        }
        img_edit_wakeup_time_top.setOnClickListener {
            openBottomSheet()
        }

        restorativeChart = view.findViewById(R.id.restorativeChart)
        sleepStagesView = view.findViewById<SleepChartViewLanding>(R.id.sleepStagesView)

        view.findViewById<LinearLayout>(R.id.play_now).setOnClickListener {
            startActivity(Intent(requireContext(), NewSleepSoundActivity::class.java).apply {
                putExtra("PlayList", "PlayList")
            })
        }
        view.findViewById<ImageView>(R.id.arrowSleepSound).setOnClickListener {
            startActivity(Intent(requireContext(), NewSleepSoundActivity::class.java).apply {
                putExtra("PlayList", "PlayList")
            })
        }
      //  storeData()
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
                Toast.makeText(context, "Error checking permissions: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
        lifecycleScope.launch {
            if (granted.containsAll(allReadPermissions)) {
                fetchAllHealthData()
//                storeHealthData()
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show()
                }
            } else {
                withContext(Dispatchers.Main) {
                 //   Toast.makeText(context, "Some permissions denied, using available data", Toast.LENGTH_SHORT).show()
                }
                fetchAllHealthData()
               // storeHealthData()
            }
        }
    }

    fun convertUtcToInstant(utcString: String): Instant {
        return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(utcString))
    }

    private suspend fun fetchAllHealthData() {
        val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
        try {
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
                            SharedPreferenceManager.getInstance(requireContext()).saveDeviceName(deviceInfo.manufacturer)
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
            val syncTime = SharedPreferenceManager.getInstance(requireContext()).moveRightSyncTime ?: ""
            if (syncTime == "") {
                endTime = Instant.now()
                startTime = endTime.minus(Duration.ofDays(30))
            }else{
                endTime = Instant.now()
                startTime = convertUtcToInstant(syncTime)
                btnSync.visibility = View.GONE
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
            withContext(Dispatchers.Main) {
                //  Toast.makeText(context, "Health Data Fetched", Toast.LENGTH_SHORT).show()
                storeHealthData()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            withContext(Dispatchers.Main) {
                Toast.makeText(context, "Error fetching health data: ${e.message}", Toast.LENGTH_SHORT).show()
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
            }
        }
    }

    private fun storeHealthData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userid = SharedPreferenceManager.getInstance(requireActivity()).userId
                val activeEnergyBurned = totalCaloriesBurnedRecord?.mapNotNull { record ->
                    if (record.energy.inKilocalories > 0) {
                        EnergyBurnedRequest(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            record_type = "ActiveEnergyBurned",
                            unit = "kcal",
                            value = record.energy.inKilocalories.toString(),
                            source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
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
                        source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
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
                            source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
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
                            source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
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
                                source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
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
                        source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
                    )
                } ?: emptyList()
                val restingHeartRate = restingHeartRecord?.map { record ->
                    HeartRateRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "RestingHeartRate",
                        unit = "bpm",
                        value = record.beatsPerMinute.toString(),
                        source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
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
                            source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
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
                            source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
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
                        source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
                    )
                } ?: emptyList()
                val bloodPressureDiastolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BloodPressureDiastolic",
                        unit = "millimeterOfMercury",
                        value = record.diastolic.inMillimetersOfMercury.toString(),
                        source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
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
                            source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
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
                        source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
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
                                source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung"
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
                    val calories = record.metadata.dataOrigin?.let { 300 } ?: 0
                    val distance = record.metadata.dataOrigin?.let { 5.0 } ?: 0.0
                    if (calories > 0) {
                        WorkoutRequest(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            source_name = SharedPreferenceManager.getInstance(requireActivity()).deviceName ?: "samsung",
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
                val response = ApiClient.apiServiceFastApi.storeHealthData(request)
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val todaysTime = Instant.now()
                       // val syncTime = convertToTargetFormat(todaysTime.toString())
                        val syncTime = ZonedDateTime.parse(todaysTime.toString(), DateTimeFormatter.ISO_DATE_TIME)
                        SharedPreferenceManager.getInstance(requireContext()).saveMoveRightSyncTime(syncTime.toString())
                          Toast.makeText(requireContext(), response.body()?.message ?: "Health data stored successfully", Toast.LENGTH_SHORT).show()
                        fetchSleepLandingData()
                        isRepeat = true
                    } else {
                         Toast.makeText(requireContext(), "Error storing data: ${response.code()} - ${response.message()}", Toast.LENGTH_SHORT).show()
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                    isRepeat = true
                }
            }
        }
    }

    private fun fetchSoundSleepData() {
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val skip = "0"
        val limit = "3"
        val type = "playlist"
        val call = ApiClient.apiService.fetchSleepSound(token,userId, skip,limit,type)
        call.enqueue(object : Callback<SleepSoundResponse> {
            override fun onResponse(call: Call<SleepSoundResponse>, response: Response<SleepSoundResponse>) {
                if (response.isSuccessful) {
                    sleepSoundResponse = response.body()!!
                    if (sleepSoundResponse.sleepSoundData != null){
                        if (sleepSoundResponse.sleepSoundData?.services!!.size > 0){
                            btnSleepSound.visibility = View.GONE
                            sleepSoundCardView.visibility = View.VISIBLE
                            view?.findViewById<TextView>(R.id.tv_sleep_sound)?.text = "Your Playlist"
                            view?.findViewById<TextView>(R.id.tv_sleep_sound_save)?.text = ""+ sleepSoundResponse.sleepSoundData?.services?.size + " Sleep sounds saved"
                            if (sleepSoundResponse.sleepSoundData?.services?.size == 1){
                                soundPlay1.visibility = View.VISIBLE
                                soundPlay2.visibility = View.GONE
                                soundPlay3.visibility = View.GONE
                                Glide.with(requireContext())
                                    .load("https://d1sacaybzizpm5.cloudfront.net/"+sleepSoundResponse.sleepSoundData?.services?.getOrNull(0)?.image)
                                    .placeholder(R.drawable.sleep_pillow)
                                    .into(soundPlay1)
                            }else if (sleepSoundResponse.sleepSoundData?.services?.size == 2){
                                soundPlay1.visibility = View.VISIBLE
                                soundPlay2.visibility = View.VISIBLE
                                soundPlay3.visibility = View.GONE
                                Glide.with(requireContext())
                                    .load("https://d1sacaybzizpm5.cloudfront.net/"+sleepSoundResponse.sleepSoundData?.services?.getOrNull(0)?.image)
                                    .placeholder(R.drawable.sleep_pillow)
                                    .into(soundPlay1)
                                Glide.with(requireContext())
                                    .load("https://d1sacaybzizpm5.cloudfront.net/"+sleepSoundResponse.sleepSoundData?.services?.getOrNull(1)?.image)
                                    .placeholder(R.drawable.sleep_pillow)
                                    .into(soundPlay2)
                            }else if (sleepSoundResponse.sleepSoundData?.services?.size == 3){
                                soundPlay1.visibility = View.VISIBLE
                                soundPlay2.visibility = View.VISIBLE
                                soundPlay3.visibility = View.VISIBLE
                                Glide.with(requireContext())
                                    .load("https://d1sacaybzizpm5.cloudfront.net/"+sleepSoundResponse.sleepSoundData?.services?.getOrNull(0)?.image)
                                    .placeholder(R.drawable.sleep_pillow)
                                    .into(soundPlay1)
                                Glide.with(requireContext())
                                    .load("https://d1sacaybzizpm5.cloudfront.net/"+sleepSoundResponse.sleepSoundData?.services?.getOrNull(1)?.image)
                                    .placeholder(R.drawable.sleep_pillow)
                                    .into(soundPlay2)
                                Glide.with(requireContext())
                                    .load("https://d1sacaybzizpm5.cloudfront.net/"+sleepSoundResponse.sleepSoundData?.services?.getOrNull(2)?.image)
                                    .placeholder(R.drawable.sleep_pillow)
                                    .into(soundPlay3)
                            }
                        }else {
                            btnSleepSound.visibility = View.VISIBLE
                            sleepSoundCardView.visibility = View.GONE
                        }
                    }else {
                        btnSleepSound.visibility = View.VISIBLE
                        sleepSoundCardView.visibility = View.GONE
                    }
                } else {
                    btnSleepSound.visibility = View.VISIBLE
                    sleepSoundCardView.visibility = View.GONE
                }
            }
            override fun onFailure(call: Call<SleepSoundResponse>, t: Throwable) {
                btnSleepSound.visibility = View.VISIBLE
                sleepSoundCardView.visibility = View.GONE
                Log.e("Error", "API call failed: ${t.message}")
            }
        })
    }

    private fun fetchWakeupData() {
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
        val date = getCurrentDate()
        val source = "android"
        val call = ApiClient.apiServiceFastApi.fetchWakeupTime(userId, source)
        call.enqueue(object : Callback<WakeupTimeResponse> {
            override fun onResponse(call: Call<WakeupTimeResponse>, response: Response<WakeupTimeResponse>) {
                if (response.isSuccessful) {
                    val currentTime = LocalDateTime.now(ZoneId.systemDefault()) // Device ke default time zone use
                    val sixPM = 18 * 60 // 1080 minutes (06:00 PM)
                    val twoAM = 2 * 60 // 120 minutes (02:00 AM next day)
                    val currentMinutes = currentTime.hour * 60 + currentTime.minute

// Check if current time is between 06:00 PM and 02:00 AM
                    if (currentMinutes >= sixPM && currentMinutes < twoAM + 24 * 60) {
                        cardSleepTimeRequirementTop.visibility = View.VISIBLE
                        sleepTimeRequirementCardView.visibility = View.GONE
                    } else {
                        cardSleepTimeRequirementTop.visibility = View.GONE
                        sleepTimeRequirementCardView.visibility = View.VISIBLE
                    }
                    //sleepTimeRequirementCardView.visibility = View.VISIBLE
                    wakeupTimeResponse = response.body()!!
                    setWakeupData(wakeupTimeResponse.data.getOrNull(0))
                }else if(response.code() == 400){
                    //sleepTimeRequirementCardView.visibility = View.GONE
                    val currentTime = LocalDateTime.now(ZoneId.systemDefault()) // Device ke default time zone use
                    val sixPM = 18 * 60 // 1080 minutes (06:00 PM)
                    val twoAM = 2 * 60 // 120 minutes (02:00 AM next day)
                    val currentMinutes = currentTime.hour * 60 + currentTime.minute

// Check if current time is between 06:00 PM and 02:00 AM
                    if (currentMinutes >= sixPM && currentMinutes < twoAM + 24 * 60) {
                        //view.visibility = View.GONE
                        cardSleepTimeRequirementTop.visibility = View.GONE
                        sleepTimeRequirementCardView.visibility = View.GONE
                    } else {
                        //view.visibility = View.VISIBLE
                        cardSleepTimeRequirementTop.visibility = View.GONE
                        sleepTimeRequirementCardView.visibility = View.GONE
                    }
                  //  Toast.makeText(activity, "Record Not Found", Toast.LENGTH_SHORT).show()
                } else {
                    val currentTime = LocalDateTime.now(ZoneId.systemDefault()) // Device ke default time zone use
                    val sixPM = 18 * 60
                    val twoAM = 2 * 60
                    val currentMinutes = currentTime.hour * 60 + currentTime.minute

                    if (currentMinutes >= sixPM && currentMinutes < twoAM + 24 * 60) {
                        //view.visibility = View.GONE
                        cardSleepTimeRequirementTop.visibility = View.VISIBLE
                        sleepTimeRequirementCardView.visibility = View.GONE
                    } else {
                        //view.visibility = View.VISIBLE
                        cardSleepTimeRequirementTop.visibility = View.GONE
                        sleepTimeRequirementCardView.visibility = View.VISIBLE
                    }
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
               //     Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<WakeupTimeResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
            }
        })
    }

    fun setWakeupData(wakeupData: WakeupData?){
        todaysSleepRequirement.setText(convertDecimalHoursToHrMinFormat(wakeupData?.currentRequirement!!))
        tv_todays_sleep_time_requirement_top.setText(convertDecimalHoursToHrMinFormat(wakeupData?.currentRequirement!!))
        todaysSleepStartTime.setText(convertTo12HourFormat(wakeupData.sleepDatetime!!))
        tv_todays_sleep_start_time_top.setText(convertTo12HourFormat(wakeupData.sleepDatetime!!))
        todaysWakeupTime.setText(convertTo12HourFormat(wakeupData.wakeupDatetime!!))
        tv_todays_wakeup_time_top.setText(convertTo12HourFormat(wakeupData.wakeupDatetime!!))
        mWakeupTime = wakeupData.wakeupDatetime!!
        mRecordId = wakeupData.Id!!
    }

    private fun convertDecimalHoursToHrMinFormat(hoursDecimal: Double): String {
        val totalMinutes = (hoursDecimal * 60).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return String.format("%02dhr %02dmins", hours, minutes)
    }

    fun convertDecimalMinutesToHrMinFormat(decimalMinutes: Double): String {
        val totalMinutes = decimalMinutes.toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60

        return when {
            hours > 0 && minutes > 0 -> "${hours} hr ${minutes} min"
            hours > 0 -> "${hours} hr"
            else -> "${minutes} min"
        }
    }

    fun formatIsoTo12Hour(timeStr: String): String {
        val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val dateTime = LocalDateTime.parse(timeStr, inputFormatter)
        return dateTime.format(outputFormatter)
    }

    fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.now().format(formatter)
    }

    private fun openBottomSheet() {
        val dialog = WakeUpTimeDialogFragment(
            context = requireContext(),
            wakeupTime = mWakeupTime,
            recordId = mRecordId,
            listener = object : OnWakeUpTimeSelectedListener {
                override fun onWakeUpTimeSelected(time: String) {
                 //   todaysWakeupTime.setText(time)
                //    mEditWakeTime = time
                //    val orgSleepTime = todaysSleepRequirement.text
                //    Toast.makeText(requireContext(),"$orgSleepTime", Toast.LENGTH_SHORT).show()
                    fetchWakeupData()
                 //   todaysSleepRequirement.setText()
                }
            }
        )
        dialog.show(parentFragmentManager, "WakeUpTimeDialog")
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    private fun fetchSleepLandingData() {
        if (isAdded  && view != null){
            requireActivity().runOnUiThread {
                showLoader(requireView())
            }
        }
        val userId = SharedPreferenceManager.getInstance(requireActivity()).userId ?: ""
        val date = getCurrentDate()
        val source = "android"
        val preferences = "nature_sounds"
        val call = ApiClient.apiServiceFastApi.fetchSleepLandingPage(userId, source, date, preferences)
        call.enqueue(object : Callback<SleepLandingResponse> {
            override fun onResponse(call: Call<SleepLandingResponse>, response: Response<SleepLandingResponse>) {
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    landingPageResponse = response.body()!!
                    setSleepRightLandingData(landingPageResponse)
                }else if(response.code() == 400){
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    Toast.makeText(activity, "Record Not Found", Toast.LENGTH_SHORT).show()
                    stageNoDataCardView.visibility = View.VISIBLE
                    sleepStagesView.visibility = View.GONE
                    performNoDataCardView.visibility = View.VISIBLE
                    performCardView.visibility = View.GONE
                    restroNoDataCardView.visibility = View.GONE
                    restroDataCardView.visibility = View.GONE
                    actualNoDataCardView.visibility = View.VISIBLE
                    sleepIdeal.visibility = View.GONE
                    lineChart.visibility = View.GONE
                    consistencyNoDataCardView.visibility = View.VISIBLE
                    consistencySleep.visibility = View.GONE
                    sleepConsistencyChart.visibility = View.GONE
                    if (!bottomSeatName.contentEquals("LogLastNightSleep") && !isRepeat){
                        val dialog = LogYourNapDialogFragment(
                            requireContext = requireContext(),
                            listener = object : OnLogYourNapSelectedListener {
                                override fun onLogTimeSelected(time: String) {
                                    fetchSleepLandingData()
                                }
                            }
                        )
                        dialog.show(parentFragmentManager, "LogYourNapDialogFragment")
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    stageNoDataCardView.visibility = View.VISIBLE
                    sleepStagesView.visibility = View.GONE
                    performNoDataCardView.visibility = View.VISIBLE
                    performCardView.visibility = View.GONE
                    restroNoDataCardView.visibility = View.GONE
                    restroDataCardView.visibility = View.GONE
                    actualNoDataCardView.visibility = View.VISIBLE
                    lineChart.visibility = View.GONE
                    sleepIdeal.visibility = View.GONE
                    consistencyNoDataCardView.visibility = View.VISIBLE
                    consistencySleep.visibility = View.GONE
                    sleepConsistencyChart.visibility = View.GONE
                    sleepStageCardView.visibility = View.GONE
                    if (!bottomSeatName.contentEquals("LogLastNightSleep") && !isRepeat){
                        val dialog = LogYourNapDialogFragment(
                            requireContext = requireContext(),
                            listener = object : OnLogYourNapSelectedListener {
                                override fun onLogTimeSelected(time: String) {
                                    fetchSleepLandingData()
                                }
                            }
                        )
                        dialog.show(parentFragmentManager, "LogYourNapDialogFragment")
                    }
                }
                if (!isRepeat){
                    val availabilityStatus = HealthConnectClient.getSdkStatus(requireContext())
                    if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
                        healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
                        lifecycleScope.launch {
                            requestPermissionsAndReadAllData()
                        }
                    } else {
                        Toast.makeText(context, "Please install or update samsung from the Play Store.", Toast.LENGTH_LONG).show()
                    }
                }
            }
            override fun onFailure(call: Call<SleepLandingResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        dismissLoader(requireView())
                    }
                }
                stageNoDataCardView.visibility = View.VISIBLE
                sleepStagesView.visibility = View.GONE
                performNoDataCardView.visibility = View.VISIBLE
                performCardView.visibility = View.GONE
                restroNoDataCardView.visibility = View.GONE
                restroDataCardView.visibility = View.GONE
                actualNoDataCardView.visibility = View.VISIBLE
                sleepIdeal.visibility = View.GONE
                lineChart.visibility = View.GONE
                consistencyNoDataCardView.visibility = View.VISIBLE
                consistencySleep.visibility = View.GONE
                sleepConsistencyChart.visibility = View.GONE
                if (!isRepeat){
                    val availabilityStatus = HealthConnectClient.getSdkStatus(requireContext())
                    if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
                        healthConnectClient = HealthConnectClient.getOrCreate(requireContext())
                        lifecycleScope.launch {
                            requestPermissionsAndReadAllData()
                        }
                    } else {
                        Toast.makeText(context, "Please install or update samsung from the Play Store.", Toast.LENGTH_LONG).show()
                    }
                }
            }
        })
    }

    private fun setSleepRightLandingData(sleepLandingResponse: SleepLandingResponse) {
        landingAllData = sleepLandingResponse.sleepLandingAllData!!
        landingAllData.userId = sleepLandingResponse.sleepLandingAllData?.userId ?: ""
        landingAllData.source = sleepLandingResponse.sleepLandingAllData?.source ?: ""
        landingAllData.endDate = sleepLandingResponse.sleepLandingAllData?.endDate ?: ""
        landingAllData.startDate = sleepLandingResponse.sleepLandingAllData?.startDate ?: ""
        landingAllData.recommendedSound = sleepLandingResponse.sleepLandingAllData?.recommendedSound ?: ""

        // Set Sleep Performance Data
        if (sleepLandingResponse.sleepLandingAllData?.sleepPerformanceDetail != null) {
            performNoDataCardView.visibility = View.GONE
            performCardView.visibility = View.VISIBLE
            setSleepPerformanceData(sleepLandingResponse.sleepLandingAllData?.sleepPerformanceDetail!!)
        }else{
            performNoDataCardView.visibility = View.VISIBLE
            performCardView.visibility = View.GONE
        }

        // Set Sleep Stages Data
        val sleepStageData: ArrayList<SleepStagesData> = arrayListOf()
        if (sleepLandingResponse.sleepLandingAllData?.sleepStagesDetail?.sleepStage != null) {
            for (i in 0 until sleepLandingResponse.sleepLandingAllData?.sleepStagesDetail?.sleepStage?.size!!) {
                sleepLandingResponse.sleepLandingAllData?.sleepStagesDetail?.sleepStage?.getOrNull(i)?.let {
                    sleepStageData.add(it)
                }
            }
            setSleepRightStageData(sleepStageData)
            tvStageSleepStartTime.setText(convertTo12HourZoneFormat(sleepLandingResponse.sleepLandingAllData?.sleepStagesDetail?.sleepStartTime!!))
            tvStageSleepEndTime.setText(convertTo12HourZoneFormat(sleepLandingResponse.sleepLandingAllData?.sleepStagesDetail?.sleepEndTime!!))
            tvStageSleepTotalTime.text = convertDecimalMinutesToHrMinFormat(sleepLandingResponse.sleepLandingAllData?.sleepStagesDetail?.totalSleepDurationMinutes!!)
        }else{
            stageNoDataCardView.visibility = View.VISIBLE
            sleepStagesView.visibility = View.GONE
            sleepStageCardView.visibility = View.GONE
        }

        //IdealActualResponse
        if (landingAllData.idealVsActualSleepTime.isNotEmpty()) {
            if (landingAllData.idealVsActualSleepTime.getOrNull(landingAllData.idealVsActualSleepTime.size.minus(1))?.actualSleepHours!=null && landingAllData.idealVsActualSleepTime.getOrNull(landingAllData.idealVsActualSleepTime.size.minus(1))?.idealSleepHours!=null){
                if (landingAllData.idealVsActualSleepTime.getOrNull(landingAllData.idealVsActualSleepTime.size.minus(1))?.actualSleepHours!=0.0 && landingAllData.idealVsActualSleepTime.getOrNull(landingAllData.idealVsActualSleepTime.size.minus(1))?.idealSleepHours!=0.0){
                    actualNoDataCardView.visibility = View.GONE
                    sleepIdeal.visibility = View.VISIBLE
                    lineChart.visibility = View.VISIBLE
                    tvActualTime.text = convertDecimalHoursToHrMinFormat(landingAllData.idealVsActualSleepTime.getOrNull(landingAllData.idealVsActualSleepTime.size.minus(1))?.actualSleepHours!!)
                    tvIdealTime.text = convertDecimalHoursToHrMinFormat(landingAllData.idealVsActualSleepTime.getOrNull(landingAllData.idealVsActualSleepTime.size.minus(1))?.idealSleepHours!!)
                    tvIdealActualDate.text = convertDateToNormalDate(landingAllData.idealVsActualSleepTime.getOrNull(landingAllData.idealVsActualSleepTime.size.minus(1))?.date!!)
                    var sleepDataList: List<SleepGraphData>? = arrayListOf()
                    actualNoDataCardView.visibility = View.GONE
                    sleepIdeal.visibility = View.VISIBLE
                    lineChart.visibility = View.VISIBLE
                    sleepDataList = landingAllData.idealVsActualSleepTime.map { detail ->
                        val formattedDate = detail.date?.let { formatIdealDate(it) }
                        return@map formattedDate?.let {
                            detail.idealSleepHours?.toFloat()?.let { it1 ->
                                detail.actualSleepHours?.toFloat()?.let { it2 ->
                                    SleepGraphData(date = it, idealSleep = it1, actualSleep = it2)
                                }
                            }
                        }!!
                    }
                    val weekRanges = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                    if (sleepDataList != null) {
                        setIdealGraphDataFromSleepList(sleepDataList, weekRanges)
                    }
                } else {
                    actualNoDataCardView.visibility = View.VISIBLE
                    sleepIdeal.visibility = View.GONE
                    lineChart.visibility = View.GONE
                }
                }
            }

        // Set Restorative Sleep Data
        if (sleepLandingResponse.sleepLandingAllData?.sleepRestorativeDetail != null) {
              //  restroNoDataCardView.visibility = View.GONE
            restroDataCardView.visibility = View.VISIBLE
                setRestorativeSleepData(sleepLandingResponse.sleepLandingAllData?.sleepRestorativeDetail)
            } else{
                restroNoDataCardView.visibility = View.GONE
            restroDataCardView.visibility = View.GONE
            }

        //Set Consistency Sleep Data
        if (sleepLandingResponse.sleepLandingAllData?.sleepConsistency != null) {
            if (landingAllData.sleepConsistency?.sleepConsistencyDetail?.averageSleepDurationHours != null) {
                if (landingAllData.sleepConsistency?.sleepConsistencyDetail?.averageSleepDurationHours != 0.0) {
                    consistencyNoDataCardView.visibility = View.GONE
                    consistencySleep.visibility = View.VISIBLE
                    sleepConsistencyChart.visibility = View.VISIBLE
                    tvConsistencyTime.text = convertDecimalHoursToHrMinFormat(landingAllData.sleepConsistency?.sleepConsistencyDetail?.averageSleepDurationHours!!)
                    tvConsistencyDate.text = convertDateToNormalDate(landingAllData.sleepConsistency?.sleepDetails?.getOrNull(landingAllData.sleepConsistency?.sleepDetails?.size?.minus(1) ?: 0)?.date!!)
                    setConsistencySleepData(sleepLandingResponse.sleepLandingAllData?.sleepConsistency)
                }else{
                    consistencyNoDataCardView.visibility = View.VISIBLE
                    consistencySleep.visibility = View.GONE
                    sleepConsistencyChart.visibility = View.GONE
                }
            }
        }

    }

    private fun setIdealGraphDataFromSleepList(sleepData: List<SleepGraphData>?, weekRanges: List<String>) {
        val idealEntries = ArrayList<Entry>()
        val actualEntries = ArrayList<Entry>()
        val labels = mutableListOf<String>()

        sleepData?.forEachIndexed { index, (label, _) ->
            val date = LocalDate.parse(label, DateTimeFormatter.ISO_LOCAL_DATE)
            val top = date.format(DateTimeFormatter.ofPattern("EEE"))
            val bottom = date.format(DateTimeFormatter.ofPattern("d MMM"))
            labels.add("$top\n$bottom")
        }

        sleepData?.forEachIndexed { index, data ->
            idealEntries.add(Entry(index.toFloat(), data.idealSleep))
            actualEntries.add(Entry(index.toFloat(), data.actualSleep))
        }

        // Line without circles - Ideal
        if (sleepData?.size!! > 8) {
            val idealLineSet = LineDataSet(idealEntries, "Ideal").apply {
                color = Color.parseColor("#00C853") // green
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 2f
                isHighlightEnabled = true // Enable highlighting for this dataset
            }

            // Only last point - Ideal
            val idealLastPoint = idealEntries.lastOrNull()
            val idealLastPointSet = LineDataSet(listOfNotNull(idealLastPoint), "").apply {
                color = Color.parseColor("#00C853")
                setCircleColor(color)
                circleRadius = 5f
                setDrawCircles(true)
                setDrawValues(false)
                setDrawHighlightIndicators(true)
                lineWidth = 0f
                isHighlightEnabled = true // Enable highlighting for this dataset
            }

            // Line without circles - Actual
            val actualLineSet = LineDataSet(actualEntries, "Actual").apply {
                color = Color.parseColor("#2979FF") // blue
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 2f
                isHighlightEnabled = true // Enable highlighting for this dataset
            }

            // Only last point - Actual
            val actualLastPoint = actualEntries.lastOrNull()
            val actualLastPointSet = LineDataSet(listOfNotNull(actualLastPoint), "").apply {
                color = Color.parseColor("#2979FF")
                setCircleColor(color)
                circleRadius = 5f
                setDrawCircles(true)
                setDrawValues(false)
                setDrawHighlightIndicators(true)
                lineWidth = 0f
                isHighlightEnabled = true // Enable highlighting for this dataset
            }
            lineChart.data = LineData(idealLineSet, actualLineSet, idealLastPointSet, actualLastPointSet)
        } else {
            val idealLineSet = LineDataSet(idealEntries, "Ideal").apply {
                color = Color.parseColor("#00C853") // green
                setDrawCircles(true)
                setDrawValues(false)
                lineWidth = 2f
                isHighlightEnabled = true // Enable highlighting for this dataset
            }

            // Only last point - Ideal
            val idealLastPoint = idealEntries.lastOrNull()
            val idealLastPointSet = LineDataSet(listOfNotNull(idealLastPoint), "").apply {
                color = Color.parseColor("#00C853")
                setCircleColor(color)
                circleRadius = 5f
                setDrawCircles(true)
                setDrawValues(false)
                setDrawHighlightIndicators(true)
                lineWidth = 0f
                isHighlightEnabled = true // Enable highlighting for this dataset
            }

            // Line without circles - Actual
            val actualLineSet = LineDataSet(actualEntries, "Actual").apply {
                color = Color.parseColor("#2979FF") // blue
                setDrawCircles(true)
                setDrawValues(false)
                lineWidth = 2f
                isHighlightEnabled = true // Enable highlighting for this dataset
            }

            // Only last point - Actual
            val actualLastPoint = actualEntries.lastOrNull()
            val actualLastPointSet = LineDataSet(listOfNotNull(actualLastPoint), "").apply {
                color = Color.parseColor("#2979FF")
                setCircleColor(color)
                circleRadius = 5f
                setDrawCircles(true)
                setDrawValues(false)
                setDrawHighlightIndicators(true)
                lineWidth = 0f
                isHighlightEnabled = true // Enable highlighting for this dataset
            }
            lineChart.data = LineData(idealLineSet, actualLineSet, idealLastPointSet, actualLastPointSet)
        }

        // X Axis
        lineChart.xAxis.apply {
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index in labels.indices) labels[index] else ""
                }
            }
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            labelRotationAngle = 0f
            textSize = 10f
        }

        // Y Axis Left
        lineChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 20f
            granularity = 1f
            textSize = 12f
        }

        // Y Axis Right
        lineChart.axisRight.isEnabled = false

        // Chart Settings
        lineChart.description.isEnabled = false
        lineChart.legend.textSize = 12f
        lineChart.setExtraBottomOffset(24f)
        lineChart.setPinchZoom(false)
        lineChart.setDrawGridBackground(false)
        lineChart.setScaleEnabled(false)
        lineChart.isDoubleTapToZoomEnabled = false
        lineChart.isDragEnabled = true // Enable drag for better interaction
        lineChart.isHighlightPerTapEnabled = true // Enable touch highlighting
        lineChart.setTouchEnabled(true) // Explicitly enable touch

        // Multi-line X axis labels
        lineChart.setXAxisRenderer(
            MultilineXAxisRenderer(
                lineChart.viewPortHandler,
                lineChart.xAxis,
                lineChart.getTransformer(YAxis.AxisDependency.LEFT)
            )
        )

        // Add touch listener for circle selection
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                Log.d("TouchEvent", "onValueSelected triggered, e=$e, h=$h")
                if (e != null && h != null) {
                    val xIndex = e.x.toInt()
                    val idealValue = idealEntries.getOrNull(xIndex)?.y ?: 0f
                    val actualValue = actualEntries.getOrNull(xIndex)?.y ?: 0f
                    val (idealHours, idealMinutes) = idealValue.toHoursAndMinutes()
                    val (actualHours, actualMinutes) = actualValue.toHoursAndMinutes()
                    Log.d("TouchEvent", "Selected: xIndex=$xIndex, idealValue=$idealValue, actualValue=$actualValue")

                    tvIdealTime.text = String.format("%d hr %d mins", idealHours, idealMinutes)
                    tvActualTime.text = String.format("%d hr %d mins", actualHours, actualMinutes)
                    Log.d("TouchEvent", "Setting tvIdealTime to ${tvIdealTime.text}, tvActualTime to ${tvActualTime.text}")

                    // Update tv_ideal_actual_date with formatted date
                    if (xIndex in labels.indices) {
                        val dateParts = labels[xIndex].split("\n")
                        val day = dateParts[0] // e.g., "Wed"
                        val dayMonth = dateParts[1] // e.g., "25 Jun"
                        val fullDate = LocalDate.parse(sleepData[xIndex].date.toString(), DateTimeFormatter.ISO_LOCAL_DATE)
                        val formattedDate = fullDate.format(DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy"))
                        tvIdealActualDate.text = formattedDate
                        Log.d("TouchEvent", "Setting tv_ideal_actual_date to $formattedDate")
                    }
                } else {
                    Log.d("TouchEvent", "Entry or Highlight is null")
                }
            }

            override fun onNothingSelected() {
              //  tvActualTime.text = ""
               // tvIdealTime.text = ""
               // tv_ideal_actual_date.text = ""
                Log.d("TouchEvent", "Nothing selected, cleared TextViews")
            }
        })

        lineChart.invalidate()
    }
    // Helper function to convert float (hours) to hours and minutes
    private fun Float.toHoursAndMinutes(): Pair<Int, Int> {
        val totalMinutes = (this * 60).toInt() // Convert to minutes
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return Pair(hours, minutes)
    }

    fun convertDateToNormalDate(dateStr: String): String{
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter = DateTimeFormatter.ofPattern("EEEE dd MMM, yyyy", Locale.ENGLISH)
        val date = LocalDate.parse(dateStr, inputFormatter)
        val formatted = date.format(outputFormatter)
        return formatted
    }

    fun convertTo12HourFormat(input: String): String {
         lateinit var inputFormatter : DateTimeFormatter
        if (input.length > 21) {
             inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        }else{
            inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        }
        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a") // 12-hour format with AM/PM
        val dateTime = LocalDateTime.parse(input, inputFormatter)
        return outputFormatter.format(dateTime)
    }

    fun convertTo12HourZoneFormat(input: String): String {
        lateinit var inputFormatter : DateTimeFormatter
        if (input.length > 21) {
            inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSSSSS")
        }else{
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

    private fun formatIdealDate(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE dd MMM", Locale.getDefault())
       // return outputFormat.format(inputFormat.parse(dateTime)!!)
        return dateTime
    }

    private fun formatDate(dateTime: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val outputFormat = SimpleDateFormat("EEE dd MMM", Locale.getDefault())
        return outputFormat.format(inputFormat.parse(dateTime)!!)
    }

    private fun setSleepRightStageData(sleepStageResponse: ArrayList<SleepStagesData>) {
        if (sleepStageResponse.size > 0) {
            stageNoDataCardView.visibility = View.GONE
            sleepStagesView.visibility = View.VISIBLE
            sleepStageCardView.visibility = View.VISIBLE
            restroNoDataCardView.visibility = View.GONE
            restroDataCardView.visibility = View.VISIBLE
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
            tvStageCoreTime.text = formatDuration(totalCoreDuration)
            tvStageAwakeTime.text = formatDuration(totalAwakeDuration)
            tvStageDeepTime.text = formatDuration(totalDeepDuration)
            tvStageRemTime.text = formatDuration(totalRemDuration)
            setStageGraph(sleepStageResponse)
        }else{
            stageNoDataCardView.visibility = View.VISIBLE
            sleepStagesView.visibility = View.GONE
            sleepStageCardView.visibility = View.GONE
        }
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
    private fun fetchThinkRecomendedData() {
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val call = ApiClient.apiService.fetchThinkRecomended(token,"HOME","SLEEP_RIGHT")
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

    private fun setStageGraph(sleepStageResponse: ArrayList<SleepStagesData>) {
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

        sleepStagesView.setSleepData(sleepData)
    }

    private fun setSleepPerformanceData(sleepPerformanceDetail: SleepPerformanceDetail) {
        if (sleepPerformanceDetail.sleepPerformanceData?.sleepPerformance != null) {
            if (sleepPerformanceDetail.sleepPerformanceData?.sleepPerformance!! > 0.0) {
                restroNoDataCardView.visibility = View.GONE
                restroDataCardView.visibility = View.GONE
                performNoDataCardView.visibility = View.GONE
                performCardView.visibility = View.VISIBLE
                tvPerformStartTime.text = convertTo12HourZoneFormat(sleepPerformanceDetail.actualSleepData?.sleepStartTime!!)
                tvPerformWakeTime.text = convertTo12HourZoneFormat(sleepPerformanceDetail.actualSleepData?.sleepEndTime!!)
                tvPerformSleepPercent.text = sleepPerformanceDetail.sleepPerformanceData?.sleepPerformance?.toString()
                tvPerformSleepDuration.text = convertDecimalHoursToHrMinFormat(sleepPerformanceDetail.actualSleepData?.actualSleepDurationHours!!)
                tvPerformIdealDuration.text = convertDecimalHoursToHrMinFormat(sleepPerformanceDetail.idealSleepDuration!!)
                if (sleepPerformanceDetail.sleepPerformanceData?.actionStep != null && sleepPerformanceDetail.sleepPerformanceData?.message != null) {
                    tvPerformAction.text = sleepPerformanceDetail.sleepPerformanceData?.actionStep
                    //tvPerformAction.text = ""
                    tvPerformMessage.text = sleepPerformanceDetail.sleepPerformanceData?.message
                   // imgPerformAction.setImageResource(R.drawable.yellow_info)
                }else{
                    tvPerformAction.text = ""
                    tvPerformMessage.text = ""
                //    imgPerformAction.setImageResource(0)
                }
            } else {
                performNoDataCardView.visibility = View.VISIBLE
                performCardView.visibility = View.GONE
                restroNoDataCardView.visibility = View.GONE
                restroDataCardView.visibility = View.GONE
                if (!bottomSeatName.contentEquals("LogLastNightSleep") && !isRepeat){
                    val dialog = LogYourNapDialogFragment(
                        requireContext = requireContext(),
                        listener = object : OnLogYourNapSelectedListener {
                            override fun onLogTimeSelected(time: String) {
                                fetchSleepLandingData()
                            }
                        }
                    )
                    dialog.show(parentFragmentManager, "LogYourNapDialogFragment")
                }
            }
        }else{
            performNoDataCardView.visibility = View.VISIBLE
            performCardView.visibility = View.GONE
        }
    }

    private fun setRestorativeSleepData(sleepRestorativeDetail: SleepRestorativeDetail?) {
        val remTime = addRemStageData(sleepRestorativeDetail?.sleepStagesData)
        val deepTime = addDeepStageData(sleepRestorativeDetail?.sleepStagesData)
        if (remTime != "00hr 00mins" && deepTime != "00hr 00mins") {
            restroNoDataCardView.visibility = View.GONE
            restroDataCardView.visibility = View.VISIBLE
            tvRestoRemTime.text = addRemStageData(sleepRestorativeDetail?.sleepStagesData)
            tvRestoDeepTime.text = addDeepStageData(sleepRestorativeDetail?.sleepStagesData)
            tvRestoStartTime.text = convertTo12HourZoneFormat(sleepRestorativeDetail?.sleepStartTime!!)
            tvRestoEndTime.text = convertTo12HourZoneFormat(sleepRestorativeDetail.sleepEndTime!!)
            restorativeChart.setSleepData(sleepRestorativeDetail.sleepStagesData)
        }else{
          //  restroNoDataCardView.visibility = View.VISIBLE
            restroDataCardView.visibility = View.GONE
        }
        }

    private fun addDeepStageData(sleepStagesData: ArrayList<SleepStagesData>?): String {
        var totalDeepDuration = 0.0
        if (sleepStagesData?.isNotEmpty() == true) {
            for (i in 0 until sleepStagesData?.size!!) {
                val startDateTime =
                    LocalDateTime.parse(sleepStagesData[i].startDatetime, formatters)
                val endDateTime = LocalDateTime.parse(sleepStagesData[i].endDatetime, formatters)
                val duration = Duration.between(startDateTime, endDateTime).toMinutes()
                    .toFloat() / 60 // Convert to hours

                when (sleepStagesData[i].stage) {
                    "Deep Sleep" -> {
                        deepData.add(duration)
                        totalDeepDuration += duration
                    }
                }
            }
        }
        return convertDecimalHoursToHrMinFormat(totalDeepDuration)
    }

    private fun addRemStageData(sleepStagesData: ArrayList<SleepStagesData>?): String {
        var totalRemDuration = 0.0
    if (sleepStagesData?.isNotEmpty() == true) {
      for (i in 0 until sleepStagesData?.size!!) {
        val startDateTime =
            LocalDateTime.parse(sleepStagesData[i].startDatetime, formatters)
        val endDateTime = LocalDateTime.parse(sleepStagesData[i].endDatetime, formatters)
        val duration = Duration.between(startDateTime, endDateTime).toMinutes()
            .toFloat() / 60 // Convert to hours

        when (sleepStagesData[i].stage) {
            "REM Sleep" -> {
                remData.add(duration)
                totalRemDuration += duration
            }
          }
       }
     }
        return convertDecimalHoursToHrMinFormat(totalRemDuration)
    }

    fun setConsistencySleepData(consistencyData: SleepConsistency?) = runBlocking {
        val parseSleepData: ArrayList<SleepDetails> = arrayListOf()
        if (consistencyData?.sleepDetails!=null) {
            for (sleepEntry in consistencyData.sleepDetails) {
                parseSleepData.add(sleepEntry)
            }
        }
        val entries = parseSleepData.toSleepEntries()               // skips the 0-hour rows
        sleepConsistencyChart.setSleepData(entries)
        sleepConsistencyChart.setOnBarClickListener { entry ->
            tvConsistencyDate.text = entry.endLocal.format(DateTimeFormatter.ofPattern("EEEE d MMM, yyyy"))
            val dur = Duration.ofMinutes((entry.durationHrs * 60).roundToInt().toLong())
            val hrs = dur.toHours()
            val mins = dur.minusHours(hrs).toMinutes()
            tvConsistencyTime.text = "${hrs}hr ${mins}mins"
        }
        /*val result = async {
            parseSleepData(parseSleepData)
        }.await()
        sleepConsistencyChart.setSleepData(result)*/
    }

    /*private fun parseSleepData(sleepDetails: List<SleepDetails>): List<SleepEntry> {
        val sleepSegments = mutableListOf<SleepEntry>()
        for (sleepEntry in sleepDetails) {
            val startTime = sleepEntry.sleepStartTime ?: ""
            val endTime = sleepEntry.sleepEndTime ?: ""
            val duration = sleepEntry.sleepDurationHours?.toFloat()!!
            sleepSegments.add(SleepEntry(startTime, endTime, duration))
        }
        return sleepSegments
    }*/

    fun convertToTargetFormat(input: String): String {
        val possibleFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ssX",        // ISO with timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",    // ISO with milliseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX", // ISO with nanoseconds
            "yyyy-MM-dd HH:mm:ss",           // Common DB format
            "yyyy/MM/dd HH:mm:ss",           // Slash format
            "dd-MM-yyyy HH:mm:ss",           // Day-Month-Year
            "MM/dd/yyyy HH:mm:ss"            // US format
        )

        // Check if it's a nanosecond timestamp (very large number)
        if (input.matches(Regex("^\\d{18,}$"))) {
            return try {
                val nanos = input.toLong()
                val seconds = nanos / 1_000_000_000
                val nanoAdjustment = (nanos % 1_000_000_000).toInt()
                val instant = Instant.ofEpochSecond(seconds, nanoAdjustment.toLong())
                val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .withZone(ZoneOffset.UTC)
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
                val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'")
                    .withZone(ZoneOffset.UTC)
                return targetFormatter.format(instant)
            } catch (e: DateTimeParseException) {
                // Try next format
            }
        }

        return "" // Unable to parse
    }

    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun saveViewAsPdf(context: Context, view: View, fileName: String) {
        val bitmap = getBitmapFromView(view)
        val document = PdfDocument()
        val pageInfo = PdfDocument.PageInfo.Builder(bitmap.width, bitmap.height, 1).create()
        val page = document.startPage(pageInfo)
        val canvas: Canvas = page.canvas
        canvas.drawBitmap(bitmap, 0f, 0f, null)
        document.finishPage(page)

        var fileUri: Uri? = null
        var success = false
        var outputStream: OutputStream? = null

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                val contentValues = ContentValues().apply {
                    put(MediaStore.MediaColumns.DISPLAY_NAME, "$fileName.pdf")
                    put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                    put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
                }
                val resolver = context.contentResolver
                val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
                if (uri != null) {
                    fileUri = uri
                    outputStream = resolver.openOutputStream(uri)
                }
            } else {
                val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
                val file = File(directory, "$fileName.pdf")
                fileUri = Uri.fromFile(file)
                outputStream = file.outputStream()
            }

            outputStream?.use {
                document.writeTo(it)
                success = true
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } finally {
            document.close()
            outputStream?.close()
        }

        if (success && fileUri != null) {
            showDownloadNotification(context, fileName, fileUri)
        }
    }

    fun getBitmapFromView(view: View): Bitmap {
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        view.draw(canvas)
        return bitmap
    }



    @RequiresPermission(Manifest.permission.POST_NOTIFICATIONS)
    fun showDownloadNotification(context: Context, fileName: String, fileUri: Uri) {
        val channelId = "download_channel"
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Downloads",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Download Notifications"
            }
            notificationManager.createNotificationChannel(channel)
        }

        // Intent to open the PDF file
        val openPdfIntent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(fileUri, "application/pdf")
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        val pendingIntent = PendingIntent.getActivity(
            context, 0, openPdfIntent, PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.stat_sys_download_done)
            .setContentTitle("Download Complete")
            .setContentText("$fileName.pdf saved to Downloads")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent) // Open PDF on click
            .setAutoCancel(true) // Dismiss when tapped
            .build()

        NotificationManagerCompat.from(context).notify(1, notification)
    }

    private fun storeData(){
        val jsonData : SleepJson = loadStepData()
        val fullList = jsonData.dataPoints
        val sleepJsonRequest = SleepJsonRequest(
            user_id = "68010b615a508d0cfd6ac9ca",
            source = "android",
            sleep_stage = fullList.map {
                SleepStageJson(
                    value = it.fitValue[0].value?.intVal.toString(),
                    record_type = it.fitValue[0].value?.intVal.toString(),
                    end_datetime = convertToTargetFormat(it.endTimeNanos.toString()),
                    unit = it.dataTypeName.toString(),
                    start_datetime = convertToTargetFormat(it.startTimeNanos.toString()),
                    source_name = "google"
                )
            } as ArrayList<SleepStageJson>
        )
        storeJsonHealthData(sleepJsonRequest)
    }

    fun convertNanoToFormattedDate(nanoTime: Long): String {
        val millis = nanoTime / 1_000_000
        val date = Date(millis)

        // Format the date
        val formatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.getDefault())
        formatter.timeZone = TimeZone.getTimeZone("UTC")
        return formatter.format(date)
    }

    @RequiresApi(Build.VERSION_CODES.UPSIDE_DOWN_CAKE)
    private fun storeJsonHealthData(sleepJsonRequest: SleepJsonRequest) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userid = SharedPreferenceManager.getInstance(requireActivity()).userId ?: ""

                val response = ApiClient.apiServiceFastApi.storeSleepData(sleepJsonRequest)

                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val responseBody = response.body()
                     //   Toast.makeText(requireContext(), responseBody?.message ?: "Health data stored successfully", Toast.LENGTH_SHORT).show()
                    } else {
                     //   Toast.makeText(requireContext(), "Error storing data: ${response.code()} - ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                 //   Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        fetchSoundSleepData()
    }

    private fun loadStepData(): SleepJson {
        val json = context?.assets?.open("sleep_raw_data.json")
            ?.bufferedReader().use { it?.readText() }
        return Gson().fromJson(json, object : TypeToken<SleepJson>() {}.type)
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }

    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    companion object{
        var dialogStartTime : LocalTime = LocalTime.of(22, 0)
        var dialogEndTime : LocalTime = LocalTime.of(6, 30)
        var dialogSleepDate : LocalDate =  LocalDate.now().minusDays(1)
        var dialogWakeDate : LocalDate =  LocalDate.now()
    }
}

private fun showDateTimePicker(context: Context, onDateTimeSelected: (LocalDateTime) -> Unit) {
    val now = LocalDateTime.now()
    val today = LocalDate.now()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            val selectedDate = LocalDate.of(year, month + 1, dayOfMonth)

            val timePickerDialog = TimePickerDialog(
                context,
                { _, hourOfDay, minute ->
                    val selectedDateTime = LocalDateTime.of(selectedDate, LocalTime.of(hourOfDay, minute))
                    // 🚫 Check if selected DateTime is in the future
                    if (selectedDateTime.isAfter(now)) {
                        Toast.makeText(context, "Cannot select a future time", Toast.LENGTH_SHORT).show()
                        // Optionally: reopen time picker with current time
                        Handler(Looper.getMainLooper()).post {
                            showTimePickerAgain(context, selectedDate, onDateTimeSelected)
                        }
                    } else {
                        onDateTimeSelected(selectedDateTime)
                    }
                },
                now.hour,
                now.minute,
                false
            )
            timePickerDialog.show()
        },
        now.year,
        now.monthValue - 1,
        now.dayOfMonth
    )
    // 🚫 Disable future dates
    datePickerDialog.datePicker.maxDate = System.currentTimeMillis()
    datePickerDialog.show()
}

private fun showTimePickerAgain(
    context: Context,
    selectedDate: LocalDate,
    onDateTimeSelected: (LocalDateTime) -> Unit
) {
    val now = LocalDateTime.now()
    val timePickerDialog = TimePickerDialog(
        context,
        { _, hourOfDay, minute ->
            val selectedDateTime = LocalDateTime.of(selectedDate, LocalTime.of(hourOfDay, minute))
            if (selectedDateTime.isAfter(now)) {
                Toast.makeText(context, "Still future! Try again.", Toast.LENGTH_SHORT).show()
            } else {
                onDateTimeSelected(selectedDateTime)
            }
        },
        now.hour,
        now.minute,
        false
    )
    timePickerDialog.show()
}


class SleepChartViewLanding(
    context: android.content.Context,
    attrs: android.util.AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sleepData = mutableListOf<SleepSegmentModel>()

    fun setSleepData(data: List<SleepSegmentModel>) {
        sleepData.clear()
        sleepData.addAll(data)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        val baselineY = height * 0.8f
        val maxBarHeight = height * 0.5f
        val cornerRadiusBase = height * 0.1f

        for (segment in sleepData) {
            paint.color = segment.color
            val left = segment.start.coerceIn(0f, 1f) * width
            val right = segment.end.coerceIn(0f, 1f) * width

            val barHeight = segment.height.coerceAtMost(maxBarHeight)
            val top = baselineY - barHeight
            val bottom = baselineY

            val cornerRadius = minOf(cornerRadiusBase, barHeight / 2)

            canvas.drawRoundRect(
                RectF(left, top, right, bottom),
                cornerRadius,
                cornerRadius,
                paint
            )
        }
    }
}


class SleepMarkerView1(context: Context, private val data: List<SleepEntry>) : MarkerView(context, R.layout.marker_view) {

    private val tvContent: TextView = findViewById(R.id.tv_content_type)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            val xIndex = e.x.toInt()

            // Find the corresponding data entry by index
            val selectedData = data.getOrNull(xIndex)

            selectedData?.let {
              //  val ideal = it.idealSleep
              //  val actual = it.actualSleep
             //   tvContent.text = "Ideal: $ideal hrs\nActual: $actual hrs"
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}

interface OnLogYourNapSelectedListener {
    fun onLogTimeSelected(time: String)
}

interface OnWakeUpTimeSelectedListener {
    fun onWakeUpTimeSelected(time: String)
}

class SleepMarkerView(context: Context, private val data: List<SleepGraphData>) : MarkerView(context, R.layout.marker_view) {

    private val tvContent: TextView = findViewById(R.id.tv_content_type)

    override fun refreshContent(e: Entry?, highlight: Highlight?) {
        if (e != null) {
            val xIndex = e.x.toInt()

            // Find the corresponding data entry by index
            val selectedData = data.getOrNull(xIndex)

            selectedData?.let {
                val ideal = it.idealSleep
                val actual = it.actualSleep

                tvContent.text = "Ideal: $ideal hrs\nActual: $actual hrs"
            }
        }
        super.refreshContent(e, highlight)
    }

    override fun getOffset(): MPPointF {
        return MPPointF(-(width / 2).toFloat(), -height.toFloat())
    }
}

class SleepRestoChartView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sleepSegments = mutableListOf<Segment>()

    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    private data class Segment(
        val position: Position1,
        val widthFraction: Float,
        val color: Int
    )

    fun setSleepData(data: List<SleepStagesData>) {
        sleepSegments.clear()

        if (data.isEmpty()) return

        // Parse timestamps and calculate total duration
        val parsedData = data.mapNotNull {
            try {
                val start = dateFormat.parse(it.startDatetime)?.time ?: return@mapNotNull null
                val end = dateFormat.parse(it.endDatetime)?.time ?: return@mapNotNull null
                val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(end - start).toInt()
                Triple(it.stage, durationMinutes, getPositionForRecordType(it.stage!!))
            } catch (e: Exception) {
                null
            }
        }

        val totalMinutes = parsedData.sumOf { it.second }

        parsedData.forEach { (recordType, duration, position) ->
            val fraction = duration.toFloat() / max(totalMinutes, 1)
            val color = getColorForRecordType(recordType!!)
            sleepSegments.add(Segment(position, fraction, color))
        }

        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val w = width.toFloat()
        val h = height.toFloat()

        // Set the background color (replace with desired color)
        canvas.drawColor(Color.parseColor("#F5F9FF")) // Example: light gray

        val barHeight = h * 0.25f
        val cornerRadius = barHeight / 4
        var currentX = 0f

        sleepSegments.forEach { segment ->
            paint.color = segment.color

            val top = when (segment.position) {
                Position1.UPPER -> h * 0.1f
                Position1.LOWER -> h * 0.35f
            }
            val bottom = top + barHeight
            val right = currentX + (segment.widthFraction * w)

            canvas.drawRoundRect(RectF(currentX, top, right, bottom), cornerRadius, cornerRadius, paint)
            currentX = right
        }
    }

    private fun getPositionForRecordType(type: String): Position1 {
        return when (type) {
            "REM Sleep" -> Position1.UPPER
            "Deep Sleep" -> Position1.LOWER
            else -> error("Unknown record type: $type")
        }
    }

    private fun getColorForRecordType(type: String): Int {
        return when (type) {
            "REM Sleep" -> resources.getColor(R.color.light_blue_bar)
            "Deep Sleep" -> resources.getColor(R.color.purple_bar)
            else -> error("Unknown record type: $type")
        }
    }
}

enum class Position1 {
    UPPER, LOWER
}

data class SleepSegmentModel(val start: Float, val end: Float, val color: Int, val height: Float)

