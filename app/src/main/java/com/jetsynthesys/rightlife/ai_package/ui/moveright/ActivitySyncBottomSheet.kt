package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.widget.LinearLayoutCompat
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
import androidx.health.connect.client.records.Record
import androidx.health.connect.client.records.RespiratoryRateRecord
import androidx.health.connect.client.records.RestingHeartRateRecord
import androidx.health.connect.client.records.SleepSessionRecord
import androidx.health.connect.client.records.StepsRecord
import androidx.health.connect.client.records.TotalCaloriesBurnedRecord
import androidx.health.connect.client.records.WeightRecord
import androidx.health.connect.client.request.ReadRecordsRequest
import androidx.health.connect.client.time.TimeRangeFilter
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
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
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Locale
import kotlin.reflect.KClass

class ActivitySyncBottomSheet : BottomSheetDialogFragment() {
    private lateinit var healthConnectClient: HealthConnectClient
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
    private var weightRecord: List<WeightRecord>? = null
    private var distanceRecord: List<DistanceRecord>? = null
    private var bodyFatRecord: List<BodyFatRecord>? = null
    private var oxygenSaturationRecord: List<OxygenSaturationRecord>? = null
    private var respiratoryRateRecord: List<RespiratoryRateRecord>? = null
    private lateinit var layout_sync_now: LinearLayoutCompat
    private lateinit var progressBar: ProgressBar
    private lateinit var btn_sync_now: Button

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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        context?.let {
            if (HealthConnectClient.getSdkStatus(it) == HealthConnectClient.SDK_AVAILABLE) {
                healthConnectClient = HealthConnectClient.getOrCreate(it)
            }
        }
    }

    override fun onStart() {
        super.onStart()

        dialog?.let {
            val bottomSheet = it.findViewById<View>(com.google.android.material.R.id.design_bottom_sheet)
            bottomSheet?.let { sheet ->
                val behavior = BottomSheetBehavior.from(sheet)
                sheet.layoutParams.height = ViewGroup.LayoutParams.MATCH_PARENT
                behavior.state = BottomSheetBehavior.STATE_EXPANDED
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        return inflater.inflate(R.layout.bottom_sheet_activity_sync, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        layout_sync_now = view.findViewById(R.id.layout_sync_now)
        btn_sync_now = view.findViewById(R.id.btn_sync_now)
        progressBar = view.findViewById(R.id.progress_bar)
            ?: throw IllegalStateException("ProgressBar with ID progress_bar not found in layout")

        btn_sync_now.setOnClickListener {
            dismiss()
        }

        layout_sync_now.setOnClickListener {
            viewLifecycleOwner.lifecycleScope.launch {
                try {
                    if (!::healthConnectClient.isInitialized) {
                        context?.let {
                            if (HealthConnectClient.getSdkStatus(it) == HealthConnectClient.SDK_AVAILABLE) {
                                healthConnectClient = HealthConnectClient.getOrCreate(it)
                            } else {
                                withContext(Dispatchers.Main) {
                                    Toast.makeText(
                                        it,
                                        "Please install or update Health Connect from the Play Store.",
                                        Toast.LENGTH_LONG
                                    ).show()
                                }
                                return@launch
                            }
                        } ?: return@launch
                    }

                    if (isAdded) {
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.VISIBLE
                            layout_sync_now.isEnabled = false
                        }
                    }

                    Log.d("ActivitySyncBottomSheet", "Starting sync process")
                    requestPermissionsAndReadAllData()

                    if (isAdded) {
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.GONE
                            layout_sync_now.isEnabled = true
                            Toast.makeText(context, "Sync completed", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    }
                } catch (e: Exception) {
                    if (isAdded) {
                        withContext(Dispatchers.Main) {
                            progressBar.visibility = View.GONE
                            layout_sync_now.isEnabled = true
                            Toast.makeText(context, "Error during sync: ${e.message}", Toast.LENGTH_SHORT).show()
                            dismiss()
                        }
                    }
                    Log.e("ActivitySyncBottomSheet", "Sync error: ${e.message}", e)
                }
            }
        }
    }

    private suspend fun requestPermissionsAndReadAllData() {
        try {
            if (!isAdded) {
                Log.d("ActivitySyncBottomSheet", "Fragment not attached, skipping permissions check")
                return
            }
            Log.d("ActivitySyncBottomSheet", "Checking permissions")
            val granted = healthConnectClient.permissionController.getGrantedPermissions()
            if (allReadPermissions.all { it in granted }) {
                Log.d("ActivitySyncBottomSheet", "All permissions granted, fetching data")
                fetchAllHealthData()
            } else {
                Log.d("ActivitySyncBottomSheet", "Requesting permissions")
                requestPermissionsLauncher.launch(allReadPermissions)
            }
        } catch (e: Exception) {
            if (isAdded) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error checking permissions: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            Log.e("ActivitySyncBottomSheet", "Permission check error: ${e.message}", e)
        }
    }

    private val requestPermissionsLauncher = registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
        viewLifecycleOwner.lifecycleScope.launch {
            if (!isAdded) {
                Log.d("ActivitySyncBottomSheet", "Fragment not attached, skipping permission result")
                return@launch
            }
            Log.d("ActivitySyncBottomSheet", "Permission result: ${granted.size} permissions granted")
            if (granted.containsAll(allReadPermissions)) {
                Log.d("ActivitySyncBottomSheet", "All permissions granted")
                if (isAdded) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Permissions Granted", Toast.LENGTH_SHORT).show()
                    }
                }
            } else {
                Log.d("ActivitySyncBottomSheet", "Some permissions denied")
                if (isAdded) {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(context, "Some permissions denied, using available data", Toast.LENGTH_SHORT).show()
                    }
                }
            }
            fetchAllHealthData()
        }
    }

    fun convertUtcToInstant(utcString: String): Instant {
        val zonedDateTime = ZonedDateTime.parse(utcString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        return zonedDateTime.toInstant()
    }

    private suspend fun fetchAllHealthDatas() {
        try {
            if (!isAdded) {
                Log.d("ActivitySyncBottomSheet", "Fragment not attached, skipping fetch")
                return
            }
            val grantedPermissions = healthConnectClient.permissionController.getGrantedPermissions()
            val now = Instant.now()
            val syncTime = SharedPreferenceManager.getInstance(context?.let { it }).moveRightSyncTime.orEmpty()
            val startTime: Instant = if (syncTime.isBlank()) {
                // First-time sync: pull last 30 days
                now.minus(Duration.ofDays(30))
            } else {
                // Next sync: only fetch new data
                Instant.parse(syncTime)
            }
            val endTime: Instant = now
            // Trackers for incremental sync
            var latestModifiedTime: Instant? = null
            var recordsFound = false

            // Update function for lastModifiedTime
            fun updateLastSync(record: Record) {
                val modified = record.metadata.lastModifiedTime
                if (latestModifiedTime == null || modified.isAfter(latestModifiedTime)) {
                    latestModifiedTime = modified
                }
            }
//            var startTime = Instant.now()
//            val syncTime = SharedPreferenceManager.getInstance(context?.let { it }).moveRightSyncTime ?: ""
//            if (syncTime == "") {
//                endTime = Instant.now()
//                startTime = endTime.minus(Duration.ofDays(30))
//            }else{
//                endTime = Instant.now()
//                startTime = convertUtcToInstant(syncTime)
//            }
            if (HealthPermission.getReadPermission(TotalCaloriesBurnedRecord::class) in grantedPermissions) {
                if (syncTime == "") {
                    val totalCaloroieResponse = mutableListOf<TotalCaloriesBurnedRecord>()
                    val totalDuration = Duration.between(startTime, endTime)
                    val chunkDuration = totalDuration.dividedBy(15)
                    var chunkStart = startTime
                    repeat(15) { i ->
                        val chunkEnd = if (i == 14) endTime else chunkStart.plus(chunkDuration)
                        val response = healthConnectClient.readRecords(
                            ReadRecordsRequest(
                                recordType = TotalCaloriesBurnedRecord::class,
                                timeRangeFilter = TimeRangeFilter.between(chunkStart, chunkEnd)
                            )
                        )
                        totalCaloroieResponse.addAll(response.records)
                        Log.d("HealthData", "Chunk $i → ${response.records.size} Step records")
                        chunkStart = chunkEnd
                    }
                    totalCaloriesBurnedRecord = totalCaloroieResponse
                }else{
                    val caloriesResponse = healthConnectClient.readRecords(
                        ReadRecordsRequest(
                            recordType = TotalCaloriesBurnedRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                        )
                    )
                    totalCaloriesBurnedRecord = caloriesResponse.records
                }
                // Iterate each record individually
                totalCaloriesBurnedRecord?.forEach { record ->
                    val burnedCalories = record.energy.inKilocalories
                    val start = record.startTime
                    val end = record.endTime
                    recordsFound = true
                    updateLastSync(record)
                    Log.d("HealthData", "Total Calories Burned: $burnedCalories kcal | From: $start To: $end")
                }
            } else {
                totalCaloriesBurnedRecord = emptyList()
                Log.d("HealthData", "Total Calories Burned permission denied")
            }
            if (HealthPermission.getReadPermission(StepsRecord::class) in grantedPermissions) {
                if (syncTime == "") {
                    val stepsResponse = mutableListOf<StepsRecord>()
                    val totalDuration = Duration.between(startTime, endTime)
                    val chunkDuration = totalDuration.dividedBy(15)
                    var chunkStart = startTime
                    repeat(15) { i ->
                        val chunkEnd = if (i == 14) endTime else chunkStart.plus(chunkDuration)
                        val response = healthConnectClient.readRecords(
                            ReadRecordsRequest(
                                recordType = StepsRecord::class,
                                timeRangeFilter = TimeRangeFilter.between(chunkStart, chunkEnd)
                            )
                        )
                        stepsResponse.addAll(response.records)
                        Log.d("HealthData", "Chunk $i → ${response.records.size} Step records")
                        chunkStart = chunkEnd
                    }
                    stepsRecord = stepsResponse
                }else{
                    val stepsResponse = healthConnectClient.readRecords(
                        ReadRecordsRequest(
                            recordType = StepsRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                        )
                    )
                    stepsRecord = stepsResponse.records
                }
                stepsRecord?.forEach { record ->
                    recordsFound = true
                    updateLastSync(record)
                }
            } else {
                stepsRecord = emptyList()
                Log.d("HealthData", "Steps permission denied")
            }
            if (HealthPermission.getReadPermission(HeartRateRecord::class) in grantedPermissions) {
                if (syncTime == "") {
                    val results = mutableListOf<HeartRateRecord>()
                    val totalDuration = Duration.between(startTime, endTime)
                    val chunkDuration = totalDuration.dividedBy(15)
                    var chunkStart = startTime
                    repeat(15) { i ->
                        val chunkEnd = if (i == 14) endTime else chunkStart.plus(chunkDuration)
                        val response = healthConnectClient.readRecords(
                            ReadRecordsRequest(
                                recordType = HeartRateRecord::class,
                                timeRangeFilter = TimeRangeFilter.between(chunkStart, chunkEnd)
                            )
                        )
                        results.addAll(response.records)
                        Log.d("HealthData", "Chunk $i → ${results.size} HR records")
                        chunkStart = chunkEnd
                    }
                    heartRateRecord = results
                }else{
                    val response = healthConnectClient.readRecords(
                        ReadRecordsRequest(
                            recordType = HeartRateRecord::class,
                            timeRangeFilter = TimeRangeFilter.between(startTime, endTime)
                        )
                    )
                    heartRateRecord = response.records
                    Log.d("HealthData", "Total HR records fetched: ${response.records.size}")
                }
                heartRateRecord?.forEach { record ->
                    recordsFound = true
                    updateLastSync(record)
                }
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
                    recordsFound = true
                    updateLastSync(record)
                    Log.d("HealthData", "Resting Heart Rate: ${record.beatsPerMinute} bpm, Time: ${record.time}")
                }
            }else {
                restingHeartRecord = emptyList()
                Log.d("HealthData", "Resting Heart rate permission denied")
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
                    recordsFound = true
                    updateLastSync(record)
                    Log.d("HealthData", "Active Calories Burn Rate: ${record.energy} kCal, Time: ${record.startTime}")
                }
            }else {
                activeCalorieBurnedRecord = emptyList()
                Log.d("HealthData", "Active Calories burn permission denied")
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
                    Log.d("HealthData", "Basal Metabolic Rate: ${record.basalMetabolicRate}, Time: ${record.time}")
                }
            }else {
                basalMetabolicRateRecord = emptyList()
                Log.d("HealthData", "Basal Metabolic permission denied")
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
                    Log.d("HealthData", "Blood Pressure: ${record.systolic}, Time: ${record.time}")
                }
            }else {
                bloodPressureRecord = emptyList()
                Log.d("HealthData", "Blood Pressure  permission denied")
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
                    recordsFound = true
                    updateLastSync(record)
                    Log.d("HealthData", "Heart Rate Variability: ${record.heartRateVariabilityMillis}, Time: ${record.time}")
                }
            }else {
                heartRateVariability = emptyList()
                Log.d("HealthData", "Heart rate Variability permission denied")
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
                    recordsFound = true
                    updateLastSync(record)
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
                    recordsFound = true
                    updateLastSync(record)
                    Log.d("HealthData", "Exercise Session: Type: ${record.exerciseType}, Start: ${record.startTime}, End: ${record.endTime}")
                }
            } else {
                exerciseSessionRecord = emptyList()
                Log.d("HealthData", "Exercise session permission denied")
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
                Log.d("HealthData", "Body Fat permission denied")
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
                    recordsFound = true
                    updateLastSync(record)
                    Log.d("HealthData", "Respiratory Rate: ${record.rate} breaths/min, Time: ${record.time}")
                }
            } else {
                respiratoryRateRecord = emptyList()
                Log.d("HealthData", "Respiratory rate permission denied")
            }

            if (isAdded) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Health Data Fetched", Toast.LENGTH_SHORT).show()
                }
            }
            var dataOrigin = "android phone"
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
                        if (deviceInfo.manufacturer != "") {
                            SharedPreferenceManager.getInstance(context?.let { it }).saveDeviceName(deviceInfo.manufacturer)
                            Log.d("Device Info", """ Manufacturer: ${deviceInfo.manufacturer}
                Model: ${deviceInfo.model} Type: ${deviceInfo.type} """.trimIndent())
                            break
                        }else{
                            SharedPreferenceManager.getInstance(context?.let { it }).saveDeviceName(dataOrigin)
                            break
                        }
                    } else {
                        SharedPreferenceManager.getInstance(context?.let { it }).saveDeviceName(dataOrigin)
                        break
                    }
                }
            }
            if (recordsFound && latestModifiedTime != null) {
                context?.let {
                    SharedPreferenceManager.getInstance(it).saveMoveRightSyncTime(latestModifiedTime.toString())
                }
                Log.d("HealthSync", "✔ Saved new last sync time: $latestModifiedTime")

            } else {
                Log.d("HealthSync", "⚠ No new data found → NOT updating last sync time")
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
            Log.d("ActivitySyncBottomSheet", "Health data fetch completed")
        } catch (e: Exception) {
            e.printStackTrace()
            if (isAdded) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(context, "Error fetching health data: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
            Log.e("ActivitySyncBottomSheet", "Fetch error: ${e.message}", e)
        }
    }

    private suspend fun fetchAllHealthData() {
        try {

            val ctx = context ?: return
            val client = healthConnectClient

            val granted = try {
                client.permissionController.getGrantedPermissions()
            } catch (e: Exception) {
                Log.e("HealthSync", "Permission fetch failed", e)
                emptySet()
            }

            Log.d("HealthSync", "Granted permissions = $granted")

            val now = Instant.now()

            val savedSync = SharedPreferenceManager
                .getInstance(ctx)
                .moveRightSyncTime
                .orEmpty()

            val isFirstSync = savedSync.isBlank()

            val defaultStart = now.minus(Duration.ofDays(30))
            val lastSyncInstant = if (isFirstSync) null else runCatching {
                Instant.parse(savedSync)
            }.getOrNull()

            // ✅ TIMEZONE SAFE
            val todayStart = LocalDate.now()
                .atStartOfDay(ZoneId.systemDefault())
                .toInstant()

            val computedStartTime = when {
                isFirstSync -> defaultStart
                lastSyncInstant != null -> {
                    if (lastSyncInstant.isAfter(todayStart)) todayStart else lastSyncInstant
                }
                else -> defaultStart
            }

            val endTime = now

            Log.d("HealthSync", "StartTime = $computedStartTime")
            Log.d("HealthSync", "EndTime   = $endTime")

            var latestModified: Instant? = null
            var foundNewData = false

            fun markModified(record: Record) {
                foundNewData = true
                val modified = record.metadata.lastModifiedTime
                if (latestModified == null || modified.isAfter(latestModified)) {
                    latestModified = modified
                }
            }

            suspend fun <T : Record> fetchChunk(type: KClass<T>): List<T> {
                return try {
                    if (isFirstSync) {
                        fetchChunked(type, computedStartTime, endTime, 15)
                    } else {
                        client.readRecords(
                            ReadRecordsRequest(
                                type,
                                TimeRangeFilter.between(computedStartTime, endTime)
                            )
                        ).records
                    }
                } catch (e: Exception) {
                    Log.e("HealthSync", "Fetch failed for ${type.simpleName}", e)
                    emptyList()
                }
            }

            fun <T : Record> hasPermission(type: KClass<T>): Boolean {
                val perm = HealthPermission.getReadPermission(type)
                val grantedNow = perm in granted
                if (!grantedNow) {
                    Log.w("HealthSync", "Missing permission for ${type.simpleName}")
                }
                return grantedNow
            }

            suspend fun <T : Record> load(
                type: KClass<T>,
                assign: (List<T>) -> Unit
            ) {
                Log.d("HealthSync", "Loading ${type.simpleName}")

                if (!hasPermission(type)) {
                    assign(emptyList())
                    return
                }

                val records = fetchChunk(type)
                Log.d("HealthSync", "${type.simpleName} count = ${records.size}")

                assign(records)
                records.forEach { markModified(it) }
            }

            // ------------------------------
            // FETCH
            // ------------------------------
            load(TotalCaloriesBurnedRecord::class) { totalCaloriesBurnedRecord = it }
            load(StepsRecord::class) { stepsRecord = it }
            load(HeartRateRecord::class) { heartRateRecord = it }
            load(RestingHeartRateRecord::class) { restingHeartRecord = it }
            load(ActiveCaloriesBurnedRecord::class) { activeCalorieBurnedRecord = it }
            load(HeartRateVariabilityRmssdRecord::class) { heartRateVariability = it }
            load(SleepSessionRecord::class) { sleepSessionRecord = it }
            load(ExerciseSessionRecord::class) { exerciseSessionRecord = it }
            load(RespiratoryRateRecord::class) { respiratoryRateRecord = it }
            load(WeightRecord::class) { weightRecord = it }
            load(BodyFatRecord::class) { bodyFatRecord = it }
            load(DistanceRecord::class) { distanceRecord = it }
            load(OxygenSaturationRecord::class) { oxygenSaturationRecord = it }
            load(BasalMetabolicRateRecord::class) { basalMetabolicRateRecord = it }
            load(BloodPressureRecord::class) { bloodPressureRecord = it }

            // ------------------------------
            // DEVICE DETECTION
            // ------------------------------
            val devicePackage =
                stepsRecord?.firstOrNull()?.metadata?.dataOrigin?.packageName ?: "unknown"

            val deviceManufacturer =
                stepsRecord?.firstOrNull()?.metadata?.device?.manufacturer ?: devicePackage

            SharedPreferenceManager.getInstance(ctx).saveDeviceName(deviceManufacturer)

            // ------------------------------
            // SAVE SYNC TIME
            // ------------------------------
            if (foundNewData && latestModified != null) {
                SharedPreferenceManager
                    .getInstance(ctx)
                    .saveMoveRightSyncTime(latestModified.toString())

                Log.d("HealthSync", "Updated lastSync = $latestModified")
            } else {
                Log.d("HealthSync", "No new data. Sync time unchanged")
            }

            // ------------------------------
            // PUSH TO SERVER
            // ------------------------------
            when (devicePackage) {
                "com.google.android.apps.fitness" -> storeHealthData()
                "com.sec.android.app.shealth",
                "com.samsung.android.wear.shealth" -> storeSamsungHealthData()
                else -> storeHealthData()
            }

        } catch (e: Exception) {
            Log.e("HealthSync", "Fatal error", e)
        } finally {
           // hideLoaderSafe()
        }
    }

    private suspend fun <T : Record> fetchChunked(
        type: KClass<T>,
        start: Instant,
        end: Instant,
        chunks: Int
    ): List<T> {
        val output = mutableListOf<T>()
        val total = Duration.between(start, end)
        val chunk = total.dividedBy(chunks.toLong())

        var cursor = start
        repeat(chunks) { i ->
            val next = if (i == chunks - 1) end else cursor.plus(chunk)
            val response = healthConnectClient.readRecords(
                ReadRecordsRequest(type, TimeRangeFilter.between(cursor, next))
            )
            output.addAll(response.records)
            cursor = next
        }
        return output
    }

    private suspend fun storeHealthData() {
        if (!isAdded) {
            Log.d("ActivitySyncBottomSheet", "Fragment not attached, aborting storeHealthData")
            return
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                Log.d("ActivitySyncBottomSheet", "Storing health data")
                val context: Context = context ?: return@launch
                val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
                val timeZone = ZoneId.systemDefault().id
                val userId = sharedPreferenceManager.userId ?: ""

                var activeEnergyBurned : List<EnergyBurnedRequest>? = null
                if (activeCalorieBurnedRecord!!.isNotEmpty()){
                    activeEnergyBurned = activeCalorieBurnedRecord?.mapNotNull { record ->
                        if (record.energy.inKilocalories > 0) {
                            EnergyBurnedRequest(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "ActiveEnergyBurned",
                                unit = "kcal",
                                value = record.energy.inKilocalories.toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    } ?: emptyList()
                }else{
                    activeEnergyBurned = totalCaloriesBurnedRecord?.mapNotNull { record ->
                        if (record.energy.inKilocalories > 0) {
                            EnergyBurnedRequest(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "ActiveEnergyBurned",
                                unit = "kcal",
                                value = record.energy.inKilocalories.toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    } ?: emptyList()
                }
                Log.d("ActivitySyncBottomSheet", "Prepared ${activeEnergyBurned.size} active energy records")

                val basalEnergyBurned = basalMetabolicRateRecord?.map { record ->
                    EnergyBurnedRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BasalMetabolic",
                        unit = "power",
                        value = record.basalMetabolicRate.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${basalEnergyBurned.size} basal energy records")

                val distanceWalkingRunning = distanceRecord?.mapNotNull { record ->
                    if (record.distance.inKilometers > 0) {
                        val km = record.distance.inKilometers
                        val safeKm = if (km.isFinite()) km else 0.0
                        Distance(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            record_type = "DistanceWalkingRunning",
                            unit = "km",
                            value = String.format(Locale.US, "%.2f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${distanceWalkingRunning.size} distance records")

                val stepCount = stepsRecord?.mapNotNull { record ->
                    if (record.count > 0) {
                        StepCountRequest(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            record_type = "StepCount",
                            unit = "count",
                            value = record.count.toString(),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${stepCount.size} step count records")

                val heartRate = heartRateRecord?.flatMap { record ->
                    record.samples.mapNotNull { sample ->
                        if (sample.beatsPerMinute > 0) {
                            HeartRateRequest(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "HeartRate",
                                unit = "bpm",
                                value = sample.beatsPerMinute.toInt().toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    }
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${heartRate.size} heart rate records")

                val heartRateVariability = heartRateVariability?.map { record ->
                    HeartRateVariabilityRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "HeartRateVariability",
                        unit = "double",
                        value = record.heartRateVariabilityMillis.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${heartRateVariability.size} heart rate variability records")

                val restingHeartRate = restingHeartRecord?.map { record ->
                    HeartRateRequest(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "RestingHeartRate",
                        unit = "bpm",
                        value = record.beatsPerMinute.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${restingHeartRate.size} resting heart rate records")

                val respiratoryRate = respiratoryRateRecord?.mapNotNull { record ->
                    if (record.rate > 0) {
                        val km = record.rate
                        val safeKm = if (km.isFinite()) km else 0.0
                        RespiratoryRate(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "RespiratoryRate",
                            unit = "breaths/min",
                            value = String.format(Locale.US,"%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${respiratoryRate.size} respiratory rate records")

                val oxygenSaturation = oxygenSaturationRecord?.mapNotNull { record ->
                    if (record.percentage.value > 0) {
                        val km = record.percentage.value
                        val safeKm = if (km.isFinite()) km else 0.0
                        OxygenSaturation(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "OxygenSaturation",
                            unit = "%",
                            value = String.format(Locale.US,"%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${oxygenSaturation.size} oxygen saturation records")

                val bloodPressureSystolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BloodPressureSystolic",
                        unit = "millimeterOfMercury",
                        value = record.systolic.inMillimetersOfMercury.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${bloodPressureSystolic.size} blood pressure systolic records")

                val bloodPressureDiastolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BloodPressureDiastolic",
                        unit = "millimeterOfMercury",
                        value = record.diastolic.inMillimetersOfMercury.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${bloodPressureDiastolic.size} blood pressure diastolic records")

                val bodyMass = weightRecord?.mapNotNull { record ->
                    if (record.weight.inKilograms > 0) {
                        val km = record.weight.inKilograms
                        val safeKm = if (km.isFinite()) km else 0.0
                        BodyMass(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "BodyMass",
                            unit = "kg",
                            value = String.format(Locale.US,"%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${bodyMass.size} body mass records")

                val bodyFatPercentage = bodyFatRecord?.mapNotNull { record ->
                    val km = record.percentage.value
                    val safeKm = if (km.isFinite()) km else 0.0
                    BodyFatPercentage(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BodyFat",
                        unit = "percentage",
                        value = String.format(Locale.US,"%.1f", safeKm * 100),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${bodyFatPercentage.size} body fat percentage records")

                val sleepStage = sleepSessionRecord?.flatMap { record ->
                    if (record.stages.isEmpty()) {
                        // No stages → return default "sleep"
                        listOf(
                            SleepStageJson(
                                start_datetime = convertToTargetFormat(record.startTime.toString()),
                                end_datetime = convertToTargetFormat(record.endTime.toString()),
                                record_type = "Asleep",
                                unit = "stage",
                                value = "Asleep",
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        )
                    } else {
                        // Map actual stages
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
                                    source_name = record.metadata.dataOrigin.packageName
                                )
                            }
                        }
                    }
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${sleepStage.size} sleep stage records")
                val workout = exerciseSessionRecord?.mapNotNull { record ->
                    val workoutType = when (record.exerciseType) {
                        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Running"
                        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Walking"
                        ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS -> "Gym"
                        ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT -> "Other Workout"
                        ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "Biking"
                        ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY -> "Biking Stationary"
                        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "Cycling"
                        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> "Swimming"
                        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "Strength Training"
                        ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "Yoga"
                        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "HIIT"
                        ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON -> "Badminton"
                        ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL -> "Basketball"
                        ExerciseSessionRecord.EXERCISE_TYPE_BASEBALL -> "Baseball"
                        else -> "Other"
                    }
                    val distance = record.metadata.dataOrigin?.let { 5.0 } ?: 0.0
                    val safeDistance = if (distance.isFinite()) distance else 0.0
                        WorkoutRequest(
                            start_datetime = convertToTargetFormat(record.startTime.toString()),
                            end_datetime = convertToTargetFormat(record.endTime.toString()),
                            source_name = record.metadata.dataOrigin.packageName,
                            record_type = "Workout",
                            workout_type = workoutType,
                            duration = ((record.endTime.toEpochMilli() - record.startTime.toEpochMilli()) / 1000 / 60).toString(),
                            calories_burned = "",
                            distance = String.format(Locale.US, "%.1f", safeDistance),
                            duration_unit = "minutes",
                            calories_unit = "kcal",
                            distance_unit = "km"
                        )
                } ?: emptyList()
                Log.d("ActivitySyncBottomSheet", "Prepared ${workout.size} workout records")

                val request = StoreHealthDataRequest(
                    user_id = userId,
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
                    workout = workout,
                    time_zone = timeZone
                )
                Log.d("ActivitySyncBottomSheet", "Sending store health data request for user_id: $userId")

                val gson = Gson()
                val allRecords = mutableListOf<Any>()
                allRecords.addAll(activeEnergyBurned)
                allRecords.addAll(basalEnergyBurned)
                allRecords.addAll(distanceWalkingRunning)
                allRecords.addAll(stepCount)
                allRecords.addAll(heartRate)
                allRecords.addAll(heartRateVariability)
                allRecords.addAll(restingHeartRate)
                allRecords.addAll(respiratoryRate)
                allRecords.addAll(oxygenSaturation)
                allRecords.addAll(bloodPressureSystolic)
                allRecords.addAll(bloodPressureDiastolic)
                allRecords.addAll(bodyMass)
                allRecords.addAll(bodyFatPercentage)
                allRecords.addAll(sleepStage)
                allRecords.addAll(workout)

                // Chunk upload variables
                var currentBatch = mutableListOf<Any>()
                var currentSize = 0
                val maxSize = 10 * 1024 * 1024 // 10MB

                suspend fun uploadBatch(batch: List<Any>) {
                    if (batch.isEmpty()) return
                    val req = StoreHealthDataRequest(
                        user_id = userId,
                        source = "android",
                        active_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>().filter { it.record_type == "ActiveEnergyBurned" },
                        basal_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>().filter { it.record_type == "BasalMetabolic" },
                        distance_walking_running = batch.filterIsInstance<Distance>(),
                        step_count = batch.filterIsInstance<StepCountRequest>(),
                        heart_rate = batch.filterIsInstance<HeartRateRequest>().filter { it.record_type == "HeartRate" },
                        heart_rate_variability_SDNN = batch.filterIsInstance<HeartRateVariabilityRequest>(),
                        resting_heart_rate = batch.filterIsInstance<HeartRateRequest>().filter { it.record_type == "RestingHeartRate" },
                        respiratory_rate = batch.filterIsInstance<RespiratoryRate>(),
                        oxygen_saturation = batch.filterIsInstance<OxygenSaturation>(),
                        blood_pressure_systolic = batch.filterIsInstance<BloodPressure>().filter { it.record_type == "BloodPressureSystolic" },
                        blood_pressure_diastolic = batch.filterIsInstance<BloodPressure>().filter { it.record_type == "BloodPressureDiastolic" },
                        body_mass = batch.filterIsInstance<BodyMass>(),
                        body_fat_percentage = batch.filterIsInstance<BodyFatPercentage>(),
                        sleep_stage = batch.filterIsInstance<SleepStageJson>(),
                        workout = batch.filterIsInstance<WorkoutRequest>(),
                        time_zone = timeZone
                    )
                    val response = ApiClient.apiServiceFastApi.storeHealthData(req)
                    if (!response.isSuccessful) {
                        throw Exception("Batch upload failed with code: ${response.code()}")
                    }
                }
                // Loop through all records and split into chunks
                for (record in allRecords) {
                    val json = gson.toJson(record)
                    val size = json.toByteArray().size
                    if (currentSize + size > maxSize) {
                        uploadBatch(currentBatch)
                        currentBatch = mutableListOf()
                        currentSize = 0
                    }
                    currentBatch.add(record)
                    currentSize += size
                }
                // Upload remaining batch
                if (currentBatch.isNotEmpty()) {
                    uploadBatch(currentBatch)
                }
                // ✅ Done, update sync time
                withContext(Dispatchers.Main) {
//                    context?.let {
//                        SharedPreferenceManager.getInstance(it).saveMoveRightSyncTime(Instant.now().toString())
//                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun storeSamsungHealthData() {
        if (!isAdded) {
            Log.d("ActivitySyncBottomSheet", "Fragment not attached, aborting storeHealthData")
            return
        }
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val sharedPreferenceManager = SharedPreferenceManager.getInstance(context)
                val timeZone = ZoneId.systemDefault().id
                val userid = SharedPreferenceManager.getInstance(requireActivity()).userId
                var activeEnergyBurned : List<EnergyBurnedRequest>? = null
                if (activeCalorieBurnedRecord!!.isNotEmpty()){
                    activeEnergyBurned = activeCalorieBurnedRecord?.mapNotNull { record ->
                        if (record.energy.inKilocalories > 0) {
                            EnergyBurnedRequest(
                                start_datetime = convertToSamsungFormat(record.startTime.toString()),
                                end_datetime = convertToSamsungFormat(record.endTime.toString()),
                                record_type = "ActiveEnergyBurned",
                                unit = "kcal",
                                value = record.energy.inKilocalories.toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    } ?: emptyList()
                }else{
                    activeEnergyBurned = totalCaloriesBurnedRecord?.mapNotNull { record ->
                        if (record.energy.inKilocalories > 0) {
                            EnergyBurnedRequest(
                                start_datetime = convertToSamsungFormat(record.startTime.toString()),
                                end_datetime = convertToSamsungFormat(record.endTime.toString()),
                                record_type = "ActiveEnergyBurned",
                                unit = "kcal",
                                value = record.energy.inKilocalories.toString(),
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        } else null
                    } ?: emptyList()
                }
                val basalEnergyBurned = basalMetabolicRateRecord?.map { record ->
                    EnergyBurnedRequest(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BasalMetabolic",
                        unit = "power",
                        value = record.basalMetabolicRate.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val distanceWalkingRunning = distanceRecord?.mapNotNull { record ->
                    if (record.distance.inKilometers > 0) {
                        val km = record.distance.inKilometers
                        val safeKm = if (km.isFinite()) km else 0.0
                        Distance(
                            start_datetime = convertToSamsungFormat(record.startTime.toString()),
                            end_datetime = convertToSamsungFormat(record.endTime.toString()),
                            record_type = "DistanceWalkingRunning",
                            unit = "km",
                            value = String.format(Locale.US,"%.2f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
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
                            source_name = record.metadata.dataOrigin.packageName
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
                                source_name = record.metadata.dataOrigin.packageName
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
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val restingHeartRate = restingHeartRecord?.map { record ->
                    HeartRateRequest(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "RestingHeartRate",
                        unit = "bpm",
                        value = record.beatsPerMinute.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val respiratoryRate = respiratoryRateRecord?.mapNotNull { record ->
                    if (record.rate > 0) {
                        val km = record.rate
                        val safeKm = if (km.isFinite()) km else 0.0
                        RespiratoryRate(
                            start_datetime = convertToSamsungFormat(record.time.toString()),
                            end_datetime = convertToSamsungFormat(record.time.toString()),
                            record_type = "RespiratoryRate",
                            unit = "breaths/min",
                            value = String.format(Locale.US,"%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val oxygenSaturation = oxygenSaturationRecord?.mapNotNull { record ->
                    if (record.percentage.value > 0) {
                        val km = record.percentage.value
                        val safeKm = if (km.isFinite()) km else 0.0
                        OxygenSaturation(
                            start_datetime = convertToSamsungFormat(record.time.toString()),
                            end_datetime = convertToSamsungFormat(record.time.toString()),
                            record_type = "OxygenSaturation",
                            unit = "%",
                            value = String.format(Locale.US,"%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
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
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val bloodPressureDiastolic = bloodPressureRecord?.mapNotNull { record ->
                    BloodPressure(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BloodPressureDiastolic",
                        unit = "millimeterOfMercury",
                        value = record.diastolic.inMillimetersOfMercury.toString(),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val bodyMass = weightRecord?.mapNotNull { record ->
                    if (record.weight.inKilograms > 0) {
                        val km = record.weight.inKilograms
                        val safeKm = if (km.isFinite()) km else 0.0
                        BodyMass(
                            start_datetime = convertToSamsungFormat(record.time.toString()),
                            end_datetime = convertToSamsungFormat(record.time.toString()),
                            record_type = "BodyMass",
                            unit = "kg",
                            value = String.format(Locale.US,"%.1f",safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val bodyFatPercentage = bodyFatRecord?.mapNotNull { record ->
                    val km = record.percentage.value
                    val safeKm = if (km.isFinite()) km else 0.0
                    BodyFatPercentage(
                        start_datetime = convertToSamsungFormat(record.time.toString()),
                        end_datetime = convertToSamsungFormat(record.time.toString()),
                        record_type = "BodyFat",
                        unit = "percentage",
                        value = String.format(Locale.US,"%.1f", safeKm),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
                val sleepStage = sleepSessionRecord?.flatMap { record ->
                    if (record.stages.isEmpty()) {
                        // No stages → return default "sleep"
                        listOf(
                            SleepStageJson(
                                start_datetime = convertToSamsungFormat(record.startTime.toString()),
                                end_datetime = convertToSamsungFormat(record.endTime.toString()),
                                record_type = "Asleep",
                                unit = "stage",
                                value = "Asleep",
                                source_name = record.metadata.dataOrigin.packageName
                            )
                        )
                    } else {
                        // Map actual stages
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
                                    source_name = record.metadata.dataOrigin.packageName
                                )
                            }
                        }
                    }
                } ?: emptyList()
                val workout = exerciseSessionRecord?.mapNotNull { record ->
                    val workoutType = when (record.exerciseType) {
                        ExerciseSessionRecord.EXERCISE_TYPE_RUNNING -> "Running"
                        ExerciseSessionRecord.EXERCISE_TYPE_WALKING -> "Walking"
                        ExerciseSessionRecord.EXERCISE_TYPE_GYMNASTICS -> "Gym"
                        ExerciseSessionRecord.EXERCISE_TYPE_OTHER_WORKOUT -> "Other Workout"
                        ExerciseSessionRecord.EXERCISE_TYPE_BIKING -> "Biking"
                        ExerciseSessionRecord.EXERCISE_TYPE_BIKING_STATIONARY -> "Biking Stationary"
                        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_POOL -> "Cycling"
                        ExerciseSessionRecord.EXERCISE_TYPE_SWIMMING_OPEN_WATER -> "Swimming"
                        ExerciseSessionRecord.EXERCISE_TYPE_STRENGTH_TRAINING -> "Strength Training"
                        ExerciseSessionRecord.EXERCISE_TYPE_YOGA -> "Yoga"
                        ExerciseSessionRecord.EXERCISE_TYPE_HIGH_INTENSITY_INTERVAL_TRAINING -> "HIIT"
                        ExerciseSessionRecord.EXERCISE_TYPE_BADMINTON -> "Badminton"
                        ExerciseSessionRecord.EXERCISE_TYPE_BASKETBALL -> "Basketball"
                        ExerciseSessionRecord.EXERCISE_TYPE_BASEBALL -> "Baseball"
                        else -> "Other"
                    }
                    val distance = record.metadata.dataOrigin?.let { 5.0 } ?: 0.0
                    val safeDistance = if (distance.isFinite()) distance else 0.0
                        WorkoutRequest(
                            start_datetime = convertToSamsungFormat(record.startTime.toString()),
                            end_datetime = convertToSamsungFormat(record.endTime.toString()),
                            source_name = record.metadata.dataOrigin.packageName,
                            record_type = "Workout",
                            workout_type = workoutType,
                            duration = ((record.endTime.toEpochMilli() - record.startTime.toEpochMilli()) / 1000 / 60).toString(),
                            calories_burned = "",
                            distance = String.format(Locale.US, "%.1f", safeDistance),
                            duration_unit = "minutes",
                            calories_unit = "kcal",
                            distance_unit = "km"
                        )
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
                    workout = workout,
                    time_zone = timeZone
                )
                val gson = Gson()
                val allRecords = mutableListOf<Any>()
                allRecords.addAll(activeEnergyBurned)
                allRecords.addAll(basalEnergyBurned)
                allRecords.addAll(distanceWalkingRunning)
                allRecords.addAll(stepCount)
                allRecords.addAll(heartRate)
                allRecords.addAll(heartRateVariability)
                allRecords.addAll(restingHeartRate)
                allRecords.addAll(respiratoryRate)
                allRecords.addAll(oxygenSaturation)
                allRecords.addAll(bloodPressureSystolic)
                allRecords.addAll(bloodPressureDiastolic)
                allRecords.addAll(bodyMass)
                allRecords.addAll(bodyFatPercentage)
                allRecords.addAll(sleepStage)
                allRecords.addAll(workout)

                // Chunk upload variables
                var currentBatch = mutableListOf<Any>()
                var currentSize = 0
                val maxSize = 10 * 1024 * 1024 // 10MB

                suspend fun uploadBatch(batch: List<Any>) {
                    if (batch.isEmpty()) return
                    val req = StoreHealthDataRequest(
                        user_id = userid,
                        source = "android",
                        active_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>().filter { it.record_type == "ActiveEnergyBurned" },
                        basal_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>().filter { it.record_type == "BasalMetabolic" },
                        distance_walking_running = batch.filterIsInstance<Distance>(),
                        step_count = batch.filterIsInstance<StepCountRequest>(),
                        heart_rate = batch.filterIsInstance<HeartRateRequest>().filter { it.record_type == "HeartRate" },
                        heart_rate_variability_SDNN = batch.filterIsInstance<HeartRateVariabilityRequest>(),
                        resting_heart_rate = batch.filterIsInstance<HeartRateRequest>().filter { it.record_type == "RestingHeartRate" },
                        respiratory_rate = batch.filterIsInstance<RespiratoryRate>(),
                        oxygen_saturation = batch.filterIsInstance<OxygenSaturation>(),
                        blood_pressure_systolic = batch.filterIsInstance<BloodPressure>().filter { it.record_type == "BloodPressureSystolic" },
                        blood_pressure_diastolic = batch.filterIsInstance<BloodPressure>().filter { it.record_type == "BloodPressureDiastolic" },
                        body_mass = batch.filterIsInstance<BodyMass>(),
                        body_fat_percentage = batch.filterIsInstance<BodyFatPercentage>(),
                        sleep_stage = batch.filterIsInstance<SleepStageJson>(),
                        workout = batch.filterIsInstance<WorkoutRequest>(),
                        time_zone = timeZone
                    )
                    val response = ApiClient.apiServiceFastApi.storeHealthData(req)
                    if (!response.isSuccessful) {
                        throw Exception("Batch upload failed with code: ${response.code()}")
                    }
                }
                // Loop through all records and split into chunks
                for (record in allRecords) {
                    val json = gson.toJson(record)
                    val size = json.toByteArray().size
                    if (currentSize + size > maxSize) {
                        uploadBatch(currentBatch)
                        currentBatch = mutableListOf()
                        currentSize = 0
                    }
                    currentBatch.add(record)
                    currentSize += size
                }
                // Upload remaining batch
                if (currentBatch.isNotEmpty()) {
                    uploadBatch(currentBatch)
                }
                // ✅ Done, update sync time
                withContext(Dispatchers.Main) {
//                    context?.let {
//                        SharedPreferenceManager.getInstance(it).saveMoveRightSyncTime(Instant.now().toString())
//                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun convertToTargetFormat(input: String): String {
        val possibleFormats = listOf(
            "yyyy-MM-dd'T'HH:mm:ssX",        // ISO with timezone
            "yyyy-MM-dd'T'HH:mm:ss.SSSX",    // ISO with milliseconds
            "yyyy-MM-dd'T'HH:mm:ss.SSSSSSSSSX", // ISO with nanoseconds
            "yyyy-MM-dd HH:mm:ss",           // Common DB format
            "yyyy/MM/dd HH:mm:ss",           // Slash format
            "dd-MM-yyyy HH:mm:ss",           // Day-Month-Year
            "MM/dd/yyyy HH:mm:ss"            // US format
        )

        if (input.matches(Regex("^\\d{18,}$"))) {
            return try {
                val nanos = input.toLong()
                val seconds = nanos / 1_000_000_000
                val nanoAdjustment = (nanos % 1_000_000_000).toInt()
                val instant = Instant.ofEpochSecond(seconds, nanoAdjustment.toLong())
                val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
                targetFormatter.format(instant)
            } catch (e: Exception) {
                ""
            }
        }

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

        return ""
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
                val targetFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
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
}