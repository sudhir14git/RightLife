package com.jetsynthesys.rightlife.ui.challenge

import android.os.Bundle
import androidx.activity.addCallback
import androidx.recyclerview.widget.LinearLayoutManager
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.databinding.ActivityChallengeBinding
import com.jetsynthesys.rightlife.showCustomToast
import com.jetsynthesys.rightlife.ui.challenge.ChallengeBottomSheetHelper.showChallengeInfoBottomSheet
import com.jetsynthesys.rightlife.ui.challenge.ChallengeBottomSheetHelper.showTaskInfoBottomSheet
import com.jetsynthesys.rightlife.ui.jounal.new_journal.CalendarDay
import com.jetsynthesys.rightlife.ui.jounal.new_journal.SpacingItemDecoration
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class ChallengeActivity : BaseActivity() {
    private lateinit var binding: ActivityChallengeBinding
    private val daysOfWeek = listOf("M", "T", "W", "T", "F", "S", "S")
    private var calendarDays = mutableListOf<CalendarDay>()

    private var selectedCalendarDays = mutableListOf<CalendarDay>()
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
        setUpListenerForTasks()
        setupLogListeners()


    }

    private fun setupScoreCardListener() {
        binding.scoreCard.apply {
            imgInfo.setOnClickListener {
                ChallengeBottomSheetHelper.showScoreInfoBottomSheet(this@ChallengeActivity)
            }
        }
    }

    private fun setupCalenderView() {
        binding.rvCalendar.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        binding.rvCalendar.addItemDecoration(SpacingItemDecoration(resources.getDimensionPixelSize(R.dimen.spacing)))
        calendarAdapter =
            CalendarChallengeAdapter(calendarDays, selectedCalendarDays) { selectedDay ->
                // Toggle selection
                if (!isFutureDate(selectedDay.dateString)) {
                    calendarDays.forEach { it.isSelected = false }
                    selectedDay.isSelected = true

                    selectedDay.isChecked = !selectedDay.isChecked

                    calendarAdapter.updateData(calendarDays)
                    selectedDate = selectedDay
                    //getJournalList(selectedDay.dateString)
                } else
                    showCustomToast("Hold up, we haven’t lived that day yet!")
            }

        binding.rvCalendar.adapter = calendarAdapter

        updateWeekView()
    }

    private fun setupLogListeners() {
        binding.logItems.apply {

        }
    }

    private fun setUpListenerForTasks() {
        binding.challengeTasks.apply {
            rlAboutChallenge.setOnClickListener {
                showChallengeInfoBottomSheet(this@ChallengeActivity)
            }
            rlWhyTaskComplete.setOnClickListener {
                showTaskInfoBottomSheet(this@ChallengeActivity)
            }
            imgQuestionChallenge.setOnClickListener {
                showTaskInfoBottomSheet(this@ChallengeActivity)
            }
        }
    }

    private fun updateWeekView() {
        val (currentWeekString, currentWeek) = getWeekDates()
        var isFutureDatePresent = false
        calendarDays.clear()

        for (i in daysOfWeek.indices) {
            val isToday = currentWeek[i] == Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
            val cal = Calendar.getInstance()
            cal.set(Calendar.DAY_OF_MONTH, currentWeek[i])
            cal.set(Calendar.MONTH, calendar.get(Calendar.MONTH))
            calendarDays.add(
                CalendarDay(
                    daysOfWeek[i], currentWeek[i], isSelected = isToday,
                    currentWeekString[i]
                )
            )
            if (isFutureDate(calendarDays[i].dateString)) {
                isFutureDatePresent = true
            }
        }

        binding.tvSelectedDate.text =
            SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault()).format(calendar.time)
        calendarAdapter.updateData(calendarDays)
        binding.ivNext.isEnabled = !isFutureDatePresent
        binding.ivNext.setImageResource(if (isFutureDatePresent) R.drawable.right_arrow_journal else R.drawable.right_arrow_journal_enabled)

        binding.ivPrev.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, -1)
            updateWeekView()
        }

        binding.ivNext.setOnClickListener {
            calendar.add(Calendar.WEEK_OF_YEAR, 1)
            updateWeekView()
        }
    }

    private fun getWeekDates(): Pair<List<String>, List<Int>> {
        val calendarCopy = Calendar.getInstance()
        calendarCopy.time = calendar.time // Copy original calendar's time

        // Start week from Monday
        calendarCopy.firstDayOfWeek = Calendar.MONDAY
        calendarCopy.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)

        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        val dateStrings = mutableListOf<String>()
        val dayOfMonths = mutableListOf<Int>()

        repeat(7) {
            dateStrings.add(dateFormat.format(calendarCopy.time))
            dayOfMonths.add(calendarCopy.get(Calendar.DAY_OF_MONTH))
            calendarCopy.add(Calendar.DAY_OF_MONTH, 1)
        }

        return dateStrings to dayOfMonths
    }

    private fun isFutureDate(dateString: String): Boolean {
        return try {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateFormat.isLenient = false

            val inputDate = dateFormat.parse(dateString)
            val today = Calendar.getInstance()
            today.set(Calendar.HOUR_OF_DAY, 0)
            today.set(Calendar.MINUTE, 0)
            today.set(Calendar.SECOND, 0)
            today.set(Calendar.MILLISECOND, 0)

            inputDate?.after(today.time) ?: false
        } catch (e: Exception) {
            false // return false for invalid date format
        }
    }
}