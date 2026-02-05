package com.jetsynthesys.rightlife.ui.challenge

import android.annotation.SuppressLint
import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.OvershootInterpolator
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
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
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.PermissionManager
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
import com.jetsynthesys.rightlife.databinding.ActivityChallengeBinding
import com.jetsynthesys.rightlife.newdashboard.model.DashboardChecklistManager
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.AppLoader
import com.jetsynthesys.rightlife.ui.challenge.ChallengeBottomSheetHelper.showChallengeInfoBottomSheet
import com.jetsynthesys.rightlife.ui.challenge.ChallengeBottomSheetHelper.showTaskInfoBottomSheet
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.getDayFromDate
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.getDaySuffix
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.isFirstDateAfter
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.isOlderThan7Days
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.isToday
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.toApiDate
import com.jetsynthesys.rightlife.ui.challenge.ScoreColorHelper.getColorCode
import com.jetsynthesys.rightlife.ui.challenge.ScoreColorHelper.getImageBasedOnStatus
import com.jetsynthesys.rightlife.ui.challenge.ScoreColorHelper.setSeekBarProgressColor
import com.jetsynthesys.rightlife.ui.challenge.adapters.CalendarChallengeAdapter
import com.jetsynthesys.rightlife.ui.challenge.pojo.ChallengeStreakResponse
import com.jetsynthesys.rightlife.ui.challenge.pojo.DailyChallengeResponse
import com.jetsynthesys.rightlife.ui.challenge.pojo.DailyScoreResponse
import com.jetsynthesys.rightlife.ui.challenge.pojo.DailyTaskResponse
import com.jetsynthesys.rightlife.ui.healthcam.NewHealthCamReportActivity
import com.jetsynthesys.rightlife.ui.jounal.new_journal.CalendarDay
import com.jetsynthesys.rightlife.ui.jounal.new_journal.SpacingItemDecoration
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.util.Calendar
import java.util.Locale
import kotlin.reflect.KClass

class ChallengeActivity : BaseActivity() {
    private lateinit var binding: ActivityChallengeBinding
    private val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
    private var calendarDays = mutableListOf<CalendarDay>()
    private lateinit var calendarAdapter: CalendarChallengeAdapter
    private val calendar = Calendar.getInstance()
    private var selectedDate: CalendarDay? = null


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
    private lateinit var healthConnectClient: HealthConnectClient
    private lateinit var permissionManager: PermissionManager
    // Add this variable at the class level
    private var wasSyncing = false

    @SuppressLint("ClickableViewAccessibility")
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
        binding = ActivityChallengeBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        binding.btnBack.setOnClickListener {
            finish()
        }
        onBackPressedDispatcher.addCallback(this) {
            finish()
        }

        setupCalenderView()
        setupScoreCardListener()
        setupListenerForTasks()
        setupLogListeners()
        setupShareListeners()


        binding.llStreak.setOnClickListener {
            it.disableViewForSeconds()
            startActivity(Intent(this@ChallengeActivity, DailyStreakActivity::class.java))
        }

        if (sharedPreferenceManager.challengeState == 4) {
            binding.challengeOverCard.challengeOverCard.visibility = View.VISIBLE
            binding.logItems.llLogItems.visibility = View.GONE
        } else {
            binding.challengeOverCard.challengeOverCard.visibility = View.GONE
            binding.logItems.llLogItems.visibility = View.VISIBLE
        }

        AnalyticsLogger.logEvent(
            this@ChallengeActivity,
            AnalyticsEvent.Chl_PageOpen
        )
        binding.scoreCard.tvResync.setOnClickListener {
            it.disableViewForSeconds()
            fetchHealthDataFromHealthConnect()
        }

// Add this variable at the class level

        /*isSyncing.observe(this) { syncing ->
            if (isFinishing || isDestroyed) return@observe

            // 1. Update your text view normally
            updateResyncTextView(syncing)

            if (syncing) {
                wasSyncing = true
                showCompactSyncView()
            } else {
                // 2. ONLY call complete if we were actually syncing before
                if (wasSyncing) {
                    onSyncComplete()
                    wasSyncing = false // Reset the flag
                } else {
                    // Ensure views are hidden if we just started the app and no sync is happening
                    //hideSyncViewsDirectly()
                }
            }
        }*/

    }

    override fun onResume() {
        super.onResume()
        val rawDate = when {
            sharedPreferenceManager.challengeState == 4 ->
                sharedPreferenceManager.challengeEndDate
                    ?.split(",")
                    ?.firstOrNull()
                    ?.takeIf { it.isNotBlank() }

            !selectedDate?.dateString.isNullOrBlank() ->
                selectedDate?.dateString

            else -> null
        }

        val date = rawDate?.let { toApiDate(it) }
            ?: DateHelper.getTodayDate()

        getDailyChallengeData(date)
        loadStreak()

    }

    private fun setupScoreCardListener() {
        binding.scoreCard.apply {
            imgInfo.setOnClickListener {
                it.disableViewForSeconds()
                ChallengeBottomSheetHelper.showScoreInfoBottomSheet(this@ChallengeActivity)
            }
            scoreSeekBar.apply {
                setOnTouchListener { _, _ -> true }
                splitTrack = false
                isFocusable = false
            }
        }
    }

    private fun setupCalenderView() {
        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        binding.rvCalendar.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCalendar.addItemDecoration(SpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.spacing)))
        calendarAdapter = CalendarChallengeAdapter(calendarDays) { selectedDay ->
            // Toggle selection
            if (isFirstDateAfter(
                    selectedDay.dateString,
                    toApiDate(sharedPreferenceManager.challengeEndDate)
                ) || isFirstDateAfter(
                    toApiDate(sharedPreferenceManager.challengeStartDate), selectedDay.dateString
                )
            ) {
                showCustomToast("No challenges available for this day.")
                return@CalendarChallengeAdapter
            }
            if (!isFutureDate(selectedDay.dateString) || isToday(selectedDay.dateString)) {
                if (selectedDay.dateString != selectedDate?.dateString) {
                    calendarDays.forEach { it.isSelected = false }
                    selectedDay.isSelected = true

                    calendarAdapter.updateData(calendarDays)
                    selectedDate = selectedDay
                    getDailyTasks(selectedDay.dateString)
                    binding.tvSelectedDate.text =
                        SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                            .format(apiFormat.parse(selectedDay.dateString)!!)
                }
            } else showCustomToast("Hold up, we haven’t lived that day yet!")
        }

        binding.rvCalendar.adapter = calendarAdapter
    }

    private fun setupLogListeners() {
        binding.logItems.apply {
            llLogToolKit.setOnClickListener {
                it.disableViewForSeconds()
                startActivity(Intent(this@ChallengeActivity, MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "ThinkRight")
                    putExtra("BottomSeatName", "Not")
                })
            }
            llLogMeal.setOnClickListener {
                it.disableViewForSeconds()
                startActivity(Intent(this@ChallengeActivity, MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "EatRight")
                    putExtra("BottomSeatName", "SnapMealTypeEat")
                    putExtra("snapMealId", "")
                })
            }
            llLogSleep.setOnClickListener {
                it.disableViewForSeconds()
                startActivity(Intent(this@ChallengeActivity, MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "SleepRight")
                    putExtra("BottomSeatName", "LogLastNightSleep")
                })
            }
            llLogWorkout.setOnClickListener {
                it.disableViewForSeconds()
                startActivity(Intent(this@ChallengeActivity, MainAIActivity::class.java).apply {
                    putExtra("ModuleName", "MoveRight")
                    putExtra("BottomSeatName", "SearchActivityLogMove")
                })
            }
        }
    }

    private fun setupShareListeners() {
        binding.sharingCard.apply {
            btnReferNow.setOnClickListener {
                it.disableViewForSeconds()
                val shareIntent = Intent(Intent.ACTION_SEND).apply {
                    type = "text/plain"
                    putExtra(
                        Intent.EXTRA_TEXT,
                        "Been using this app called RightLife that tracks food, workouts, sleep, and mindfulness. Super simple, no wearable needed. Sign up between 1st Feb - 28th Feb to use the app free for the full period and join the Health Challenge.\n" +
                                "App Link: https://onelink.to/rightlife"
                    )
                }

                startActivity(Intent.createChooser(shareIntent, "Refer via"))

                AnalyticsLogger.logEvent(
                    this@ChallengeActivity,
                    AnalyticsEvent.Chl_ReferNow_Tap
                )
            }
        }
    }

    private fun setupListenerForTasks() {
        binding.challengeTasks.apply {
            rlAboutChallenge.setOnClickListener {
                it.disableViewForSeconds()
                showChallengeInfoBottomSheet(this@ChallengeActivity)
            }
            rlWhyTaskComplete.setOnClickListener {
                it.disableViewForSeconds()
                showTaskInfoBottomSheet(this@ChallengeActivity)
            }
            imgQuestionChallenge.setOnClickListener {
                it.disableViewForSeconds()
                showTaskInfoBottomSheet(this@ChallengeActivity)
            }
        }
    }

    private fun updateWeekView(date: String, isChecked: ArrayList<Boolean>) {

        var isFutureDatePresent = false
        var isPreviousDatePresent = false
        calendarDays.clear()

        val apiFormat = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)

        // Base calendar = selected date
        val baseCal = Calendar.getInstance().apply {
            time = apiFormat.parse(date)!!
            firstDayOfWeek = Calendar.SUNDAY
            set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY) // move to week start
        }


        // Check if provided date falls in this week
        val weekDates = mutableListOf<String>()

        // Move to Sunday of the provided date's week
        val weekStartCal = baseCal.clone() as Calendar
        weekStartCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        for (i in 0..6) {
            val cal = weekStartCal.clone() as Calendar
            cal.add(Calendar.DAY_OF_MONTH, i)
            weekDates.add(apiFormat.format(cal.time))
        }

        val startDate = toApiDate(sharedPreferenceManager.challengeStartDate)
        val endDate = toApiDate(sharedPreferenceManager.challengeEndDate)
        val today = DateHelper.getTodayDate() // yyyy-MM-dd

        val isStartDateInWeek = weekDates.contains(startDate)
        val isEndDateInWeek = weekDates.contains(endDate)
        val isTodayInWeek = weekDates.contains(today)

// today must be between start & end (inclusive)
        val isTodayBetween =
            !isFirstDateAfter(startDate, today) &&
                    !isFirstDateAfter(today, endDate)

        val finalSelectedDate = when {
            isTodayInWeek && isTodayBetween -> today
            isStartDateInWeek -> startDate
            isEndDateInWeek -> endDate
            else -> weekDates.first() // Sunday fallback
        }


        // Build week (Sunday → Saturday)
        for (i in 0..6) {
            val cal = baseCal.clone() as Calendar
            cal.add(Calendar.DAY_OF_MONTH, i)

            val dateString = apiFormat.format(cal.time)

            val calendarDay = CalendarDay(
                day = cal.getDisplayName(
                    Calendar.DAY_OF_WEEK,
                    Calendar.SHORT,
                    Locale.ENGLISH
                )!!.first().toString(),
                date = cal.get(Calendar.DAY_OF_MONTH),
                isSelected = dateString == finalSelectedDate,
                dateString = dateString,
                isChecked = isChecked.getOrNull(i) ?: false
            )

            calendarDays.add(calendarDay)

            // ---- Future date logic ----
            if (sharedPreferenceManager.challengeState == 4) {
                if (isFirstDateAfter(
                        dateString,
                        toApiDate(sharedPreferenceManager.challengeEndDate)
                    )
                ) {
                    isFutureDatePresent = true
                }
            } else if (isFutureDate(dateString)) {
                isFutureDatePresent = true
            }

            // ---- Previous date logic ----
            if (isFirstDateAfter(
                    toApiDate(sharedPreferenceManager.challengeStartDate),
                    dateString
                )
            ) {
                isPreviousDatePresent = true
            }
        }

        // Selected date label (actual selected date, not Sunday)
        binding.tvSelectedDate.text =
            SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())
                .format(apiFormat.parse(finalSelectedDate)!!)

        calendarAdapter.updateData(calendarDays)

        // Arrow enable / disable
        binding.ivNext.isEnabled = !isFutureDatePresent
        binding.ivPrev.isEnabled = !isPreviousDatePresent

        binding.ivNext.setImageResource(
            if (isFutureDatePresent)
                R.drawable.right_arrow_journal
            else
                R.drawable.right_arrow_journal_enabled
        )

        binding.ivPrev.setImageResource(
            if (isPreviousDatePresent)
                R.drawable.ic_arrow_left_disabled
            else
                R.drawable.left_arrow_journal
        )

        // 🔙 Previous Week → Sunday
        binding.ivPrev.setOnClickListener {
            val cal = baseCal.clone() as Calendar
            cal.add(Calendar.WEEK_OF_YEAR, -1)
            getDailyChallengeData(apiFormat.format(cal.time))
        }

        // 🔜 Next Week → Sunday
        binding.ivNext.setOnClickListener {
            val cal = baseCal.clone() as Calendar
            cal.add(Calendar.WEEK_OF_YEAR, 1)
            getDailyChallengeData(apiFormat.format(cal.time))
        }
    }


    private fun isFutureDate(dateString: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false

            val inputDate = dateFormat.parse(dateString) ?: return false

            val today = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
            // true if date is today OR in future
            !inputDate.before(today.time)

        } catch (e: Exception) {
            false
        }
    }


    private fun getDailyChallengeData(date: String) {
        AppLoader.show(this)
        apiService.dailyChallengeData(sharedPreferenceManager.accessToken, date)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody?>, response: Response<ResponseBody?>
                ) {
                    AppLoader.hide(this@ChallengeActivity)
                    if (response.isSuccessful) {
                        val gson = Gson()
                        val jsonResponse = response.body()?.string()
                        val responseObj =
                            gson.fromJson(jsonResponse, DailyChallengeResponse::class.java)
                        val isCheckedList = ArrayList<Boolean>()
                        responseObj.data.calendar.forEachIndexed { index, calendar ->

                            val isLast = index == responseObj.data.calendar.lastIndex

                            val day = CalendarDay(
                                day = calendar.day.first().toString(),
                                date = getDayFromDate(calendar.date).toInt(),
                                isSelected = calendar.date == date,
                                dateString = calendar.date,
                                isChecked = calendar.isCompleted
                            )

                            isCheckedList.add(calendar.isCompleted)
                            calendarDays.add(day)

                            if (sharedPreferenceManager.challengeState == 4 && isLast) {
                                selectedDate = day
                            }
                        }


                        calendarAdapter.updateData(calendarDays)
                        updateWeekView(date, isCheckedList)

                        getDailyTasks(date)

                    } else {
                        showCustomToast("Something went wrong!", false)
                    }
                }

                override fun onFailure(
                    call: Call<ResponseBody?>, t: Throwable
                ) {
                    AppLoader.hide(this@ChallengeActivity)
                    handleNoInternetView(t)
                }

            })
    }

    private fun getDailyScore(date: String) {
        AppLoader.show(this)
        apiService.dailyScore(sharedPreferenceManager.accessToken, date)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody?>, response: Response<ResponseBody?>
                ) {
                    AppLoader.hide(this@ChallengeActivity)
                    if (response.isSuccessful && response.body() != null) {
                        binding.scoreCard.layoutScoreCard.visibility = View.VISIBLE
                        val gson = Gson()
                        val jsonResponse = response.body()?.string()
                        val responseObj =
                            gson.fromJson(jsonResponse, DailyScoreResponse::class.java)
                        val scoreData = responseObj.data
                        binding.scoreCard.apply {
                            tvCountDownDays.text = scoreData.totalScore.toString()
                            scoreSeekBar.progress = scoreData.totalScore.takeIf { it != 0 } ?: 2
                            setSeekBarProgressColor(
                                scoreSeekBar, getColorCode(scoreData.performance)
                            )
                            imgScoreColor.imageTintList = ColorStateList.valueOf(
                                Color.parseColor(
                                    getColorCode(
                                        scoreData.performance
                                    )
                                )
                            )
                            tvScoreLabel.text = scoreData.performance
                            tvMessage.text = scoreData.message

                            tvDailyTasksCompleted.text = scoreData.daily.completed.toString()
                            tvDailyToatal.text = "/${scoreData.daily.total}"

                            tvBonusTasksCompleted.text = scoreData.bonus.completed.toString()
                            tvBonusTotal.text = "/${scoreData.bonus.total}"
                            tvRank.text = scoreData.rank.toString()
                            tvRankSuffix.text = getDaySuffix(scoreData.rank)

                            //rank code here
                            setUpRankCard(scoreData.allRank, getDaySuffix(scoreData.allRank))
                            setupFaceScanCard(responseObj.data.lastReportDate)
                        }
                        // Log an event based on the user's performance tier.
                        when (responseObj?.data?.performance) {
                            "Good" -> {
                                AnalyticsLogger.logEvent(
                                    this@ChallengeActivity,
                                    AnalyticsEvent.Chl_EntersGood
                                )
                            }

                            "Excellent" -> {
                                AnalyticsLogger.logEvent(
                                    this@ChallengeActivity,
                                    AnalyticsEvent.Chl_EntersExcellent
                                )
                            }

                            "Champ" -> {
                                AnalyticsLogger.logEvent(
                                    this@ChallengeActivity,
                                    AnalyticsEvent.Chl_EntersChamp
                                )
                            }
                        }

                    } else {
                        showCustomToast("Something went wrong!", false)
                    }
                }

                override fun onFailure(
                    call: Call<ResponseBody?>, t: Throwable
                ) {
                    AppLoader.hide(this@ChallengeActivity)
                    handleNoInternetView(t)
                }

            })
    }

    private fun getDailyTasks(date: String) {
        AppLoader.show(this)
        apiService.dailyTask(sharedPreferenceManager.accessToken, date)
            .enqueue(object : Callback<ResponseBody> {
                override fun onResponse(
                    call: Call<ResponseBody?>, response: Response<ResponseBody?>
                ) {
                    AppLoader.hide(this@ChallengeActivity)
                    if (response.isSuccessful && response.body() != null) {
                        val gson = Gson()
                        val jsonResponse = response.body()?.string()
                        val responseObj = gson.fromJson(jsonResponse, DailyTaskResponse::class.java)
                        val dailyTasks = responseObj.data.dailyTasks
                        val bonusTasks = responseObj.data.bonusTasks

                        binding.challengeTasks.apply {
                            tvDailyTaskCount.text = "${responseObj.data.completedDaily}/6"
                            tvBonusTaskCount.text = "${responseObj.data.completedBonus}/5"

                            dailyTasks.forEach { item ->
                                run {
                                    when (item.key) {
                                        "OPEN_APP" -> {
                                            imgOpenAppStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlOpenApp.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                            if (item.status == "COMPLETED") {
                                                AnalyticsLogger.logEvent(
                                                    this@ChallengeActivity,
                                                    AnalyticsEvent.Chl_OpenApp_Claimed
                                                )
                                            }
                                        }

                                        "LOG_SLEEP" -> {
                                            imgLogSleepStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlLogSleep.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                            if (item.status == "COMPLETED") {
                                                AnalyticsLogger.logEvent(
                                                    this@ChallengeActivity,
                                                    AnalyticsEvent.Chl_LogSleep_Claimed
                                                )
                                            }
                                        }

                                        "LOG_MEAL" -> {
                                            imgLogMealStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlLogMeal.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                            if (item.status == "COMPLETED") {
                                                AnalyticsLogger.logEvent(
                                                    this@ChallengeActivity,
                                                    AnalyticsEvent.Chl_LogMeal_Claimed
                                                )
                                            }
                                        }

                                        "LOG_MOVEMENT" -> {
                                            imgLogMovementStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlLogMovement.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                            if (item.status == "COMPLETED") {
                                                AnalyticsLogger.logEvent(
                                                    this@ChallengeActivity,
                                                    AnalyticsEvent.Chl_LogMove_Claimed
                                                )
                                            }
                                        }

                                        "MINDFULNESS" -> {
                                            imgPracticeMindfulnessStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlPractiseMindFullness.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                            if (item.status == "COMPLETED") {
                                                AnalyticsLogger.logEvent(
                                                    this@ChallengeActivity,
                                                    AnalyticsEvent.Chl_Mindful_Claimed
                                                )
                                            }
                                        }

                                        "FULL_DAY_BONUS" -> {
                                            imgFullDayBonusStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlFullDayBonus.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                            if (item.status == "COMPLETED") {
                                                AnalyticsLogger.logEvent(
                                                    this@ChallengeActivity,
                                                    AnalyticsEvent.Chl_FullDayBonus_Claimed
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                            bonusTasks.forEach { item ->
                                run {
                                    when (item.key) {
                                        "HIGH_QUALITY_SLEEP" -> {
                                            imgHighQualitySleepStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlHighQualitySleep.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                            if (item.status == "COMPLETED") {
                                                AnalyticsLogger.logEvent(
                                                    this@ChallengeActivity,
                                                    AnalyticsEvent.Chl_BT_HQSleep_Claim
                                                )
                                            }
                                        }

                                        "BALANCED_EATING" -> {
                                            imgBalancedEatingStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlBalancedEating.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                            if (item.status == "COMPLETED") {
                                                AnalyticsLogger.logEvent(
                                                    this@ChallengeActivity,
                                                    AnalyticsEvent.Chl_BT_BalEat_Claim
                                                )
                                            }
                                        }

                                        "DAILY_MOVEMENT_GOAL" -> {
                                            imgDailyMovementGoalStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlDailyMovementGoal.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                            if (item.status == "COMPLETED") {
                                                AnalyticsLogger.logEvent(
                                                    this@ChallengeActivity,
                                                    AnalyticsEvent.Chl_BT_MoveGoal_Claim
                                                )
                                            }
                                        }

                                        "MINDFUL_MOMENTS" -> {
                                            imgMindfulMomentsStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlMindfulMoments.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                            if (item.status == "COMPLETED") {
                                                AnalyticsLogger.logEvent(
                                                    this@ChallengeActivity,
                                                    AnalyticsEvent.Chl_BT_Mindful_Claim
                                                )
                                            }
                                        }

                                        "ALL_ROUND_WIN" -> {
                                            imgAllRoundWinStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlAllRoundWin.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                            if (item.status == "COMPLETED") {
                                                AnalyticsLogger.logEvent(
                                                    this@ChallengeActivity,
                                                    AnalyticsEvent.Chl_BT_AllBonus_Claim
                                                )
                                            }
                                        }
                                    }
                                }
                            }

                        }
                        if (responseObj.data.completedDaily == 6) {
                            AnalyticsLogger.logEvent(
                                this@ChallengeActivity,
                                AnalyticsEvent.Chl_FullDayBonus_Claimed
                            )
                        }
                        if (responseObj.data.completedBonus == 5) {
                            AnalyticsLogger.logEvent(
                                this@ChallengeActivity,
                                AnalyticsEvent.Chl_BT_AllBonus_Claim
                            )
                        }
                    } else {
                        showCustomToast("Something went wrong!", false)
                    }
                    getDailyScore(date)
                }

                override fun onFailure(
                    call: Call<ResponseBody?>, t: Throwable
                ) {
                    AppLoader.hide(this@ChallengeActivity)
                    handleNoInternetView(t)
                }

            })
    }

    private fun setUpRankCard(rank: Int, suffix: String) {
        if (sharedPreferenceManager.challengeState == 4) {
            binding.rankingCardTop.rlRanking.visibility = View.VISIBLE
            binding.rankingCard.rlRanking.visibility = View.GONE
            binding.rankingCardTop.apply {
                btnViewLeaderBoard.setOnClickListener {
                    it.disableViewForSeconds()
                    startActivity(Intent(this@ChallengeActivity, LeaderboardActivity::class.java))
                    AnalyticsLogger.logEvent(
                        this@ChallengeActivity,
                        AnalyticsEvent.Chl_ViewLeaderboard_Tap
                    )
                }
                tvRankNumber.text = rank.toString()
                tvRankSuffix.text = suffix
                when (rank) {
                    1 -> {
                        tvRankSuffix.setTextColor(Color.parseColor("#FFFFFF"))
                        tvRankNumber.setTextColor(Color.parseColor("#FFFFFF"))
                        tvRanking.setTextColor(Color.parseColor("#FFFFFF"))
                        imgRankBg.setImageResource(R.drawable.rank1)
                        imgChallenge.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                    }

                    2 -> {
                        tvRankSuffix.setTextColor(Color.parseColor("#984C01"))
                        tvRankNumber.setTextColor(Color.parseColor("#984C01"))
                        tvRanking.setTextColor(Color.parseColor("#984C01"))
                        imgRankBg.setImageResource(R.drawable.rank3)
                        imgChallenge.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#984C01"))
                    }

                    3 -> {
                        tvRankSuffix.setTextColor(Color.parseColor("#2A3A5E"))
                        tvRankNumber.setTextColor(Color.parseColor("#2A3A5E"))
                        tvRanking.setTextColor(Color.parseColor("#2A3A5E"))
                        imgRankBg.setImageResource(R.drawable.rank2)
                        imgChallenge.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#2A3A5E"))
                    }

                    else -> {
                        tvRankSuffix.setTextColor(Color.parseColor("#0B1215"))
                        tvRankNumber.setTextColor(Color.parseColor("#0B1215"))
                        tvRanking.setTextColor(Color.parseColor("#F5B829"))
                        imgRankBg.setImageResource(R.drawable.rank4)
                        imgChallenge.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#F5B829"))
                    }
                }
            }
        } else {
            binding.rankingCardTop.rlRanking.visibility = View.GONE
            binding.rankingCard.rlRanking.visibility = View.VISIBLE
            binding.rankingCard.apply {
                btnViewLeaderBoard.setOnClickListener {
                    it.disableViewForSeconds()
                    startActivity(Intent(this@ChallengeActivity, LeaderboardActivity::class.java))
                    AnalyticsLogger.logEvent(
                        this@ChallengeActivity,
                        AnalyticsEvent.Chl_ViewLeaderboard_Tap
                    )
                }
                tvRankNumber.text = rank.toString()
                tvRankSuffix.text = suffix
                when (rank) {
                    1 -> {
                        tvRankSuffix.setTextColor(Color.parseColor("#FFFFFF"))
                        tvRankNumber.setTextColor(Color.parseColor("#FFFFFF"))
                        tvRanking.setTextColor(Color.parseColor("#FFFFFF"))
                        imgRankBg.setImageResource(R.drawable.rank1)
                        imgChallenge.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                    }

                    2 -> {
                        tvRankSuffix.setTextColor(Color.parseColor("#984C01"))
                        tvRankNumber.setTextColor(Color.parseColor("#984C01"))
                        tvRanking.setTextColor(Color.parseColor("#984C01"))
                        imgRankBg.setImageResource(R.drawable.rank3)
                        imgChallenge.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#984C01"))
                    }

                    3 -> {
                        tvRankSuffix.setTextColor(Color.parseColor("#2A3A5E"))
                        tvRankNumber.setTextColor(Color.parseColor("#2A3A5E"))
                        tvRanking.setTextColor(Color.parseColor("#2A3A5E"))
                        imgRankBg.setImageResource(R.drawable.rank2)
                        imgChallenge.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#2A3A5E"))
                    }

                    else -> {
                        tvRankSuffix.setTextColor(Color.parseColor("#0B1215"))
                        tvRankNumber.setTextColor(Color.parseColor("#0B1215"))
                        tvRanking.setTextColor(Color.parseColor("#F5B829"))
                        imgRankBg.setImageResource(R.drawable.rank4)
                        imgChallenge.imageTintList =
                            ColorStateList.valueOf(Color.parseColor("#F5B829"))
                    }
                }
            }
        }

    }

    private fun setupFaceScanCard(lastFaceScandate: String) {
        if (isOlderThan7Days(lastFaceScandate)) {
            //show FaceScan Card
            binding.faceScanCard.rlFaceScanCard.visibility = View.VISIBLE
            binding.faceScanCard.apply {
                if (!lastFaceScandate.isNullOrBlank()) {
                    val parts = lastFaceScandate.split(",")

                    tvLastReportDate.text = parts.getOrNull(0)?.trim() ?: "-"
                    tvLastReportTime.text = parts.getOrNull(1)?.trim() ?: "-"
                } else {
                    tvLastReportDate.text = "-"
                    tvLastReportTime.text = "-"
                }

                llNextArrow.setOnClickListener {
                    startActivity(
                        Intent(
                            this@ChallengeActivity, NewHealthCamReportActivity::class.java
                        )
                    )
                }
            }

        } else
            binding.faceScanCard.rlFaceScanCard.visibility = View.GONE
    }

    private fun loadStreak() {
        val call = apiService.getChallengeStreak(sharedPreferenceManager.accessToken)

        call.enqueue(object : Callback<ResponseBody?> {

            override fun onResponse(call: Call<ResponseBody?>, response: Response<ResponseBody?>) {

                if (response.isSuccessful && response.body() != null) {
                    try {
                        val gson = Gson()
                        val jsonString = response.body()!!.string()

                        val responseObj =
                            gson.fromJson(jsonString, ChallengeStreakResponse::class.java)

                        if (responseObj.success) {
                            binding.tvStreakSmallCount.text = responseObj.data.streak.toString()
                            binding.imgStreakSmall.imageTintList = if (responseObj.data.streak > 0)
                                ColorStateList.valueOf("#FD6967".toColorInt()) else
                                ColorStateList.valueOf("#B5B5B5".toColorInt())
                        } else {
                            Toast.makeText(
                                this@ChallengeActivity,
                                "Failed to load streak",
                                Toast.LENGTH_SHORT
                            ).show()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(
                            this@ChallengeActivity,
                            "Streak parse error",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                } else {
                    Toast.makeText(
                        this@ChallengeActivity,
                        "Streak API error: ${response.code()}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onFailure(call: Call<ResponseBody?>, t: Throwable) {
                handleNoInternetView(t)
            }
        })
    }

    fun fetchHealthDataFromHealthConnect() {
        updateResyncTextView(true)
        showCompactSyncView()
        val availabilityStatus = HealthConnectClient.getSdkStatus(this)
        if (availabilityStatus == HealthConnectClient.SDK_AVAILABLE) {
            healthConnectClient = HealthConnectClient.getOrCreate(this)
            lifecycleScope.launch {
                requestPermissionsAndReadAllData()
            }
        } else {
            Toast.makeText(
                this@ChallengeActivity,
                "Please install or update health connect from the Play Store.",
                Toast.LENGTH_LONG
            ).show()
            onSyncComplete()
            updateResyncTextView(false)
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
                Toast.makeText(
                    this@ChallengeActivity,
                    "Error checking permissions: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private val requestPermissionsLauncher =
        registerForActivityResult(PermissionController.createRequestPermissionResultContract()) { granted ->
            lifecycleScope.launch {
                if (granted.containsAll(allReadPermissions)) {
                    fetchAllHealthData()
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@ChallengeActivity,
                            "Permissions Granted",
                            Toast.LENGTH_SHORT
                        ).show()
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
            val ctx = this@ChallengeActivity ?: return
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
            //hideLoaderSafe()
            onSyncComplete()
            updateResyncTextView(false)
            val isValidState = sharedPreferenceManager.challengeState in listOf(3)

            if (
                isValidState &&
                sharedPreferenceManager.challengeParticipatedDate.isNotEmpty() &&
                DashboardChecklistManager.checklistStatus
            ) {
                getDailyTasks(DateHelper.getTodayDate())
            }
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

    private fun storeHealthData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val timeZone = ZoneId.systemDefault().id
                val userid = SharedPreferenceManager.getInstance(this@ChallengeActivity).userId
                var activeEnergyBurned: List<EnergyBurnedRequest>? = null
                if (activeCalorieBurnedRecord!!.isNotEmpty()) {
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
                } else {
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
                val respiratoryRate = respiratoryRateRecord?.mapNotNull { record ->
                    if (record.rate > 0) {
                        val km = record.rate
                        val safeKm = if (km.isFinite()) km else 0.0
                        RespiratoryRate(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "RespiratoryRate",
                            unit = "breaths/min",
                            value = String.format(Locale.US, "%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val oxygenSaturation = oxygenSaturationRecord?.mapNotNull { record ->
                    if (record.percentage.value > 0) {
                        val km = record.percentage.value
                        val safeKm = if (km.isFinite()) km else 0.0
                        OxygenSaturation(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "OxygenSaturation",
                            unit = "%",
                            value = String.format(Locale.US, "%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
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
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
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
                val bodyMass = weightRecord?.mapNotNull { record ->
                    if (record.weight.inKilograms > 0) {
                        val km = record.weight.inKilograms
                        val safeKm = if (km.isFinite()) km else 0.0
                        BodyMass(
                            start_datetime = convertToTargetFormat(record.time.toString()),
                            end_datetime = convertToTargetFormat(record.time.toString()),
                            record_type = "BodyMass",
                            unit = "kg",
                            value = String.format(Locale.US, "%.1f", safeKm),
                            source_name = record.metadata.dataOrigin.packageName
                        )
                    } else null
                } ?: emptyList()
                val bodyFatPercentage = bodyFatRecord?.mapNotNull { record ->
                    val km = record.percentage.value
                    val safeKm = if (km.isFinite()) km else 0.0
                    BodyFatPercentage(
                        start_datetime = convertToTargetFormat(record.time.toString()),
                        end_datetime = convertToTargetFormat(record.time.toString()),
                        record_type = "BodyFat",
                        unit = "percentage",
                        value = String.format(Locale.US, "%.1f", safeKm),
                        source_name = record.metadata.dataOrigin.packageName
                    )
                } ?: emptyList()
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
                    val distance = record.metadata.dataOrigin.let { 5.0 }
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
                StoreHealthDataRequest(
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
                        active_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>()
                            .filter { it.record_type == "ActiveEnergyBurned" },
                        basal_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>()
                            .filter { it.record_type == "BasalMetabolic" },
                        distance_walking_running = batch.filterIsInstance<Distance>(),
                        step_count = batch.filterIsInstance<StepCountRequest>(),
                        heart_rate = batch.filterIsInstance<HeartRateRequest>()
                            .filter { it.record_type == "HeartRate" },
                        heart_rate_variability_SDNN = batch.filterIsInstance<HeartRateVariabilityRequest>(),
                        resting_heart_rate = batch.filterIsInstance<HeartRateRequest>()
                            .filter { it.record_type == "RestingHeartRate" },
                        respiratory_rate = batch.filterIsInstance<RespiratoryRate>(),
                        oxygen_saturation = batch.filterIsInstance<OxygenSaturation>(),
                        blood_pressure_systolic = batch.filterIsInstance<BloodPressure>()
                            .filter { it.record_type == "BloodPressureSystolic" },
                        blood_pressure_diastolic = batch.filterIsInstance<BloodPressure>()
                            .filter { it.record_type == "BloodPressureDiastolic" },
                        body_mass = batch.filterIsInstance<BodyMass>(),
                        body_fat_percentage = batch.filterIsInstance<BodyFatPercentage>(),
                        sleep_stage = batch.filterIsInstance<SleepStageJson>(),
                        workout = batch.filterIsInstance<WorkoutRequest>(),
                        time_zone = timeZone
                    )
                    val response =
                        com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient.apiServiceFastApi.storeHealthData(
                            req
                        )
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
//                    SharedPreferenceManager.getInstance(this@HomeNewActivity)
//                        .saveMoveRightSyncTime(Instant.now().toString())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ChallengeActivity,
                        "Exception: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    private fun storeSamsungHealthData() {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val timeZone = ZoneId.systemDefault().id
                val userid = SharedPreferenceManager.getInstance(this@ChallengeActivity).userId
                var activeEnergyBurned: List<EnergyBurnedRequest>? = null
                if (activeCalorieBurnedRecord!!.isNotEmpty()) {
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
                } else {
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
                            value = String.format(Locale.US, "%.2f", safeKm),
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
                            value = String.format(Locale.US, "%.1f", safeKm),
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
                            value = String.format(Locale.US, "%.1f", safeKm),
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
                            value = String.format(Locale.US, "%.1f", safeKm),
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
                        value = String.format(Locale.US, "%.1f", safeKm),
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
                    val distance = record.metadata.dataOrigin.let { 5.0 }
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
                StoreHealthDataRequest(
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
                        active_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>()
                            .filter { it.record_type == "ActiveEnergyBurned" },
                        basal_energy_burned = batch.filterIsInstance<EnergyBurnedRequest>()
                            .filter { it.record_type == "BasalMetabolic" },
                        distance_walking_running = batch.filterIsInstance<Distance>(),
                        step_count = batch.filterIsInstance<StepCountRequest>(),
                        heart_rate = batch.filterIsInstance<HeartRateRequest>()
                            .filter { it.record_type == "HeartRate" },
                        heart_rate_variability_SDNN = batch.filterIsInstance<HeartRateVariabilityRequest>(),
                        resting_heart_rate = batch.filterIsInstance<HeartRateRequest>()
                            .filter { it.record_type == "RestingHeartRate" },
                        respiratory_rate = batch.filterIsInstance<RespiratoryRate>(),
                        oxygen_saturation = batch.filterIsInstance<OxygenSaturation>(),
                        blood_pressure_systolic = batch.filterIsInstance<BloodPressure>()
                            .filter { it.record_type == "BloodPressureSystolic" },
                        blood_pressure_diastolic = batch.filterIsInstance<BloodPressure>()
                            .filter { it.record_type == "BloodPressureDiastolic" },
                        body_mass = batch.filterIsInstance<BodyMass>(),
                        body_fat_percentage = batch.filterIsInstance<BodyFatPercentage>(),
                        sleep_stage = batch.filterIsInstance<SleepStageJson>(),
                        workout = batch.filterIsInstance<WorkoutRequest>(),
                        time_zone = timeZone
                    )
                    val response =
                        com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient.apiServiceFastApi.storeHealthData(
                            req
                        )
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
//                    SharedPreferenceManager.getInstance(this@HomeNewActivity)
//                        .saveMoveRightSyncTime(Instant.now().toString())
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(
                        this@ChallengeActivity,
                        "Exception: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }
    }

    fun convertUtcToInstant(utcString: String): Instant {
        val zonedDateTime = ZonedDateTime.parse(utcString, DateTimeFormatter.ISO_ZONED_DATE_TIME)
        return zonedDateTime.toInstant()
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
                val targetFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(
                        ZoneOffset.UTC
                    )
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
                val targetFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
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
                val targetFormatter =
                    DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'").withZone(ZoneOffset.UTC)
                return targetFormatter.format(instant)
            } catch (e: DateTimeParseException) {
                // Try next format
            }
        }

        return "" // Unable to parse
    }

    private fun showCompactSyncView() {
        //isSyncing.value = true
        binding.compactSyncIndicator.apply {
            visibility = View.VISIBLE
            alpha = 0f
            scaleX = 0.6f
            scaleY = 0.6f
            animate()
                .alpha(1f).scaleX(1f).scaleY(1f)
                .setDuration(400)
                .setInterpolator(OvershootInterpolator(1.5f))
                .start()
        }
        startHeartPulse(binding.compactHeartIcon, false)
    }

    private fun onSyncComplete() {
        // 1. Define colors for Success State
        //isSyncing.value = false
        val colorGreen = ContextCompat.getColor(this, R.color.color_green)
        val colorStateList = ColorStateList.valueOf(colorGreen)
        val colorRed = ContextCompat.getColor(this, R.color.red)
        val colorStateListRed = ColorStateList.valueOf(colorRed)


        // --- Compact View Completion ---
        compactHeartAnimator?.cancel()

        binding.compactSyncIndicator.apply {
            visibility = View.VISIBLE
            alpha = 1f
            scaleX = 1f
            scaleY = 1f
        }

        binding.compactRotatingArc.visibility = View.GONE
        binding.compactHeartIcon.apply {
            imageTintList = colorStateList
            scaleX = 1f
            scaleY = 1f
        }

        // 3. Auto-hide with Shrink animation after 2.5 seconds
        binding.root.postDelayed({
            binding.compactSyncIndicator.animate()
                .scaleX(0f)
                .scaleY(0f)
                .alpha(0f)
                .setDuration(400)
                .withEndAction {
                    binding.compactSyncIndicator.visibility = View.GONE
                    binding.compactRotatingArc.visibility = View.VISIBLE
                    binding.compactHeartIcon.apply {
                        imageTintList = colorStateListRed
                        scaleX = 0f
                        scaleY = 0f
                    }
                }
                .start()
        }, 2500)
    }

    private fun updateResyncTextView(isSyncing: Boolean) {
        val tvResync = binding.scoreCard.tvResync

        // Prevent updating detached view
        if (!tvResync.isAttachedToWindow) return

        val color = if (isSyncing)
            R.color.gray_past_price
        else
            R.color.red

        val background = if (isSyncing)
            R.drawable.rounded_corner_article_gray
        else
            R.drawable.rounded_red_border

        tvResync.setTextColor(ContextCompat.getColor(this, color))
        tvResync.compoundDrawableTintList =
            ColorStateList.valueOf(ContextCompat.getColor(this, color))
        tvResync.background = ContextCompat.getDrawable(this, background)
    }



}