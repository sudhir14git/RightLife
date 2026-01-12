package com.jetsynthesys.rightlife.ui.challenge

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.graphics.toColorInt
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.ui.MainAIActivity
import com.jetsynthesys.rightlife.databinding.ActivityChallengeBinding
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.AppLoader
import com.jetsynthesys.rightlife.ui.challenge.ChallengeBottomSheetHelper.showChallengeInfoBottomSheet
import com.jetsynthesys.rightlife.ui.challenge.ChallengeBottomSheetHelper.showTaskInfoBottomSheet
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.getDayFromDate
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.getDaySuffix
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.isOlderThan7Days
import com.jetsynthesys.rightlife.ui.challenge.DateHelper.isToday
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
import com.jetsynthesys.rightlife.ui.utility.disableViewForSeconds
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChallengeActivity : BaseActivity() {
    private lateinit var binding: ActivityChallengeBinding
    private val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
    private var calendarDays = mutableListOf<CalendarDay>()
    private lateinit var calendarAdapter: CalendarChallengeAdapter
    private val calendar = Calendar.getInstance()
    private var selectedDate: CalendarDay? = null

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

        binding.challengeOverCard.challengeOverCard.visibility =
            if (sharedPreferenceManager.challengeState == 4) View.VISIBLE else View.GONE
        binding.logItems.llLogToolKit.visibility =
            if (sharedPreferenceManager.challengeState == 4) View.VISIBLE else View.GONE

    }

    override fun onResume() {
        super.onResume()
        val date = selectedDate?.dateString ?: DateHelper.getTodayDate()
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
        binding.rvCalendar.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCalendar.addItemDecoration(SpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.spacing)))
        calendarAdapter = CalendarChallengeAdapter(calendarDays) { selectedDay ->
            // Toggle selection
            if (!isFutureDate(selectedDay.dateString) || isToday(selectedDay.dateString)) {
                if (selectedDay.dateString != selectedDate?.dateString) {
                    calendarDays.forEach { it.isSelected = false }
                    selectedDay.isSelected = true

                    calendarAdapter.updateData(calendarDays)
                    selectedDate = selectedDay
                    //getDailyChallengeData(selectedDay.dateString)
                    getDailyTasks(selectedDay.dateString)
                    getDailyScore(selectedDay.dateString)
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
                        "Been using this app called RightLife that tracks food, workouts, sleep, and mood. Super simple, no wearable needed. Try it and get 7 days for free without any credit card details.\n" +
                                "Here’s the link:\n" +
                                "https://onelink.to/rightlife"
                    )
                }

                startActivity(Intent.createChooser(shareIntent, "Refer via"))
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
        calendarDays.clear()

        // Base calendar = selected date
        val baseCal = Calendar.getInstance()
        baseCal.time = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).parse(date)!!

        // Move to Sunday of the current week
        baseCal.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY)

        for (i in 0..6) {

            val cal = baseCal.clone() as Calendar
            cal.add(Calendar.DAY_OF_MONTH, i)

            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val dateString = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH).format(cal.time)

            val isSelected = dateString == date

            val dayLetter = cal.getDisplayName(
                Calendar.DAY_OF_WEEK, Calendar.SHORT, Locale.ENGLISH
            )?.substring(0, 1) ?: ""

            val calendarDay = CalendarDay(
                day = dayLetter,
                date = dayOfMonth,
                isSelected = isSelected,
                dateString = dateString,
                isChecked = isChecked.getOrNull(i) ?: false
            )

            calendarDays.add(calendarDay)

            if (isFutureDate(dateString)) {
                isFutureDatePresent = true
            }
        }

        // Selected date label
        binding.tvSelectedDate.text =
            SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(baseCal.time)

        calendarAdapter.updateData(calendarDays)

        // Next button handling
        binding.ivNext.isEnabled = !isFutureDatePresent
        binding.ivNext.setImageResource(
            if (isFutureDatePresent) R.drawable.right_arrow_journal
            else R.drawable.right_arrow_journal_enabled
        )

        // Previous week
        binding.ivPrev.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            getDailyChallengeData(formatter.format(calendar.time))
        }

        // Next week
        binding.ivNext.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            val formatter = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
            getDailyChallengeData(formatter.format(calendar.time))
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
                    AppLoader.hide()
                    if (response.isSuccessful) {
                        val gson = Gson()
                        val jsonResponse = response.body()?.string()
                        val responseObj =
                            gson.fromJson(jsonResponse, DailyChallengeResponse::class.java)
                        val isCheckedList = ArrayList<Boolean>()
                        responseObj.data.calendar.forEach { calendar ->

                            val day = CalendarDay(
                                day = calendar.day.first().toString(),
                                date = getDayFromDate(calendar.date).toInt(),
                                isSelected = calendar.date == date,
                                dateString = calendar.date,
                                isChecked = calendar.isCompleted
                            )
                            isCheckedList.add(calendar.isCompleted)
                            calendarDays.add(day)
                        }

                        calendarAdapter.updateData(calendarDays)
                        updateWeekView(date, isCheckedList)

                        getDailyScore(date)

                        getDailyTasks(date)

                    } else {
                        showCustomToast("Something went wrong!", false)
                    }
                }

                override fun onFailure(
                    call: Call<ResponseBody?>, t: Throwable
                ) {
                    AppLoader.hide()
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
                    AppLoader.hide()
                    if (response.isSuccessful && response.body() != null) {
                        val gson = Gson()
                        val jsonResponse = response.body()?.string()
                        val responseObj =
                            gson.fromJson(jsonResponse, DailyScoreResponse::class.java)
                        val scoreData = responseObj.data
                        binding.scoreCard.apply {
                            tvCountDownDays.text = scoreData.totalScore.toString()
                            scoreSeekBar.progress = scoreData.totalScore
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
                            setUpRankCard(scoreData.rank, getDaySuffix(scoreData.rank))
                            setupFaceScanCard(responseObj.data.lastReportDate)
                        }

                    } else {
                        showCustomToast("Something went wrong!", false)
                    }
                }

                override fun onFailure(
                    call: Call<ResponseBody?>, t: Throwable
                ) {
                    AppLoader.hide()
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
                    AppLoader.hide()
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
                                        }

                                        "LOG_SLEEP" -> {
                                            imgLogSleepStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlLogSleep.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                        }

                                        "LOG_MEAL" -> {
                                            imgLogMealStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlLogMeal.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                        }

                                        "LOG_MOVEMENT" -> {
                                            imgLogMovementStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlLogMovement.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                        }

                                        "MINDFULNESS" -> {
                                            imgPracticeMindfulnessStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlPractiseMindFullness.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                        }

                                        "FULL_DAY_BONUS" -> {
                                            imgFullDayBonusStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlFullDayBonus.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
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
                                        }

                                        "BALANCED_EATING" -> {
                                            imgBalancedEatingStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlBalancedEating.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                        }

                                        "DAILY_MOVEMENT_GOAL" -> {
                                            imgDailyMovementGoalStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlDailyMovementGoal.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                        }

                                        "MINDFUL_MOMENTS" -> {
                                            imgMindfulMomentsStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlMindfulMoments.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                        }

                                        "ALL_ROUND_WIN" -> {
                                            imgAllRoundWinStatus.setImageResource(
                                                getImageBasedOnStatus(item.status)
                                            )
                                            rlAllRoundWin.setBackgroundResource(if (item.status == "COMPLETED") R.drawable.task_challenge_selected_bg else R.drawable.bg_gray_border_radius_small)
                                        }
                                    }
                                }
                            }
                        }

                    } else {
                        showCustomToast("Something went wrong!", false)
                    }
                }

                override fun onFailure(
                    call: Call<ResponseBody?>, t: Throwable
                ) {
                    AppLoader.hide()
                    handleNoInternetView(t)
                }

            })
    }

    private fun setUpRankCard(rank: Int, suffix: String) {
        binding.rankingCard.apply {
            btnViewLeaderBoard.setOnClickListener {
                it.disableViewForSeconds()
                startActivity(Intent(this@ChallengeActivity, LeaderboardActivity::class.java))
            }
            tvRankNumber.text = rank.toString()
            tvRankSuffix.text = suffix
            when (rank) {
                1 -> {
                    tvRankSuffix.setTextColor(Color.parseColor("#FFFFFF"))
                    tvRankNumber.setTextColor(Color.parseColor("#FFFFFF"))
                    tvRanking.setTextColor(Color.parseColor("#FFFFFF"))
                    imgRankBg.setImageResource(R.drawable.rank1)
                    imgChallenge.imageTintList = ColorStateList.valueOf(Color.parseColor("#FFFFFF"))
                }

                2 -> {
                    tvRankSuffix.setTextColor(Color.parseColor("#984C01"))
                    tvRankNumber.setTextColor(Color.parseColor("#984C01"))
                    tvRanking.setTextColor(Color.parseColor("#984C01"))
                    imgRankBg.setImageResource(R.drawable.rank2)
                    imgChallenge.imageTintList = ColorStateList.valueOf(Color.parseColor("#984C01"))
                }

                3 -> {
                    tvRankSuffix.setTextColor(Color.parseColor("#2A3A5E"))
                    tvRankNumber.setTextColor(Color.parseColor("#2A3A5E"))
                    tvRanking.setTextColor(Color.parseColor("#2A3A5E"))
                    imgRankBg.setImageResource(R.drawable.rank3)
                    imgChallenge.imageTintList = ColorStateList.valueOf(Color.parseColor("#2A3A5E"))
                }

                else -> {
                    tvRankSuffix.setTextColor(Color.parseColor("#0B1215"))
                    tvRankNumber.setTextColor(Color.parseColor("#0B1215"))
                    tvRanking.setTextColor(Color.parseColor("#F5B829"))
                    imgRankBg.setImageResource(R.drawable.rank4)
                    imgChallenge.imageTintList = ColorStateList.valueOf(Color.parseColor("#F5B829"))
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

}