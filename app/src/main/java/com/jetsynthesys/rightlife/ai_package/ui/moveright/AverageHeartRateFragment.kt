package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.HeartRateResponse
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.databinding.FragmentAverageHeartRateBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.jetsynthesys.rightlife.ai_package.model.HeartRateVariabilityResponse
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.*

class AverageHeartRateFragment : BaseFragment<FragmentAverageHeartRateBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentAverageHeartRateBinding
        get() = FragmentAverageHeartRateBinding::inflate

    private lateinit var lineChart: LineChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var backwardImage: ImageView
    private lateinit var forwardImage: ImageView
    private var selectedWeekDate: String = ""
    private var selectedMonthDate: String = ""
    private var selectedHalfYearlyDate: String = ""
    private lateinit var selectedDate: TextView
    private lateinit var selectedItemDate: TextView
    private lateinit var selectHeartRateLayout: CardView
    private lateinit var selectedHrvTv: TextView
    private lateinit var averageHrvTv: TextView
    private lateinit var percentageTv: TextView
    private lateinit var percentageIc: ImageView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.gradient_color_background_workout)

        lineChart = view.findViewById(R.id.heartRateChart)
        radioGroup = view.findViewById(R.id.tabGroup)
        backwardImage = view.findViewById(R.id.backward_image_heart_rate)
        forwardImage = view.findViewById(R.id.forward_image_heart_rate)
        selectedDate = view.findViewById(R.id.selectedDate)
        selectedItemDate = view.findViewById(R.id.selectedItemDate)
        selectHeartRateLayout = view.findViewById(R.id.selectHeartRateLayout)
        selectedHrvTv = view.findViewById(R.id.selectedCalorieTv)
        averageHrvTv = view.findViewById(R.id.average_number)
        percentageTv = view.findViewById(R.id.valueUnitTv)
        percentageIc = view.findViewById(R.id.percentageIc)

        // Show Week data by default
        updateChart(getWeekData(), getWeekLabels(), getWeekLabelsDate())
        fetchHeartRateVariability("last_weekly") // Fetch data on load

        // Set default selection to Week
        radioGroup.check(R.id.rbWeek)

        // Handle Radio Button Selection
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbWeek -> fetchHeartRateVariability("last_weekly")
                R.id.rbMonth -> fetchHeartRateVariability("last_monthly")
                R.id.rbSixMonths -> fetchHeartRateVariability("last_six_months")
            }
        }

        backwardImage.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            var selectedTab: String = "Week"
            if (selectedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(selectedId)
                selectedTab = selectedRadioButton.text.toString()
            }

            if (selectedTab.contentEquals("Week")) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val dateString = selectedWeekDate
                val date = dateFormat.parse(dateString)
                calendar.time = date!!
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                calendar.set(year, month, day)
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val dateStr = dateFormat.format(calendar.time)
                selectedWeekDate = dateStr
                fetchHeartRateVariability("last_weekly")
            } else if (selectedTab.contentEquals("Month")) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val dateString = selectedMonthDate
                val date = dateFormat.parse(dateString)
                calendar.time = date!!
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                calendar.set(year, month - 1, day)
                val dateStr = dateFormat.format(calendar.time)
                val firstDateOfMonth = getFirstDateOfMonth(dateStr, 1)
                selectedMonthDate = firstDateOfMonth
                fetchHeartRateVariability("last_monthly")
            } else {
                selectedHalfYearlyDate = ""
                fetchHeartRateVariability("last_six_months")
            }
        }

        forwardImage.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            var selectedTab: String = "Week"
            if (selectedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(selectedId)
                selectedTab = selectedRadioButton.text.toString()
            }
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val currentDate: String = currentDateTime.format(formatter)

            if (selectedTab.contentEquals("Week")) {
                if (!selectedWeekDate.contentEquals(currentDate)) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val dateString = selectedWeekDate
                    val date = dateFormat.parse(dateString)
                    calendar.time = date!!
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    calendar.set(year, month, day)
                    calendar.add(Calendar.DAY_OF_YEAR, +7)
                    val dateStr = dateFormat.format(calendar.time)
                    selectedWeekDate = dateStr
                    fetchHeartRateVariability("last_weekly")
                } else {
                    Toast.makeText(context, "Cannot select future date", Toast.LENGTH_SHORT).show()
                }
            } else if (selectedTab.contentEquals("Month")) {
                if (!selectedMonthDate.contentEquals(currentDate)) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val dateString = selectedMonthDate
                    val date = dateFormat.parse(dateString)
                    calendar.time = date!!
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    calendar.set(year, month + 1, day)
                    val dateStr = dateFormat.format(calendar.time)
                    val firstDateOfMonth = getFirstDateOfMonth(dateStr, 1)
                    selectedMonthDate = firstDateOfMonth
                    fetchHeartRateVariability("last_monthly")
                } else {
                    Toast.makeText(context, "Cannot select future date", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (!selectedHalfYearlyDate.contentEquals(currentDate)) {
                    selectedHalfYearlyDate = ""
                    fetchHeartRateVariability("last_six_months")
                } else {
                    Toast.makeText(context, "Cannot select future date", Toast.LENGTH_SHORT).show()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToFragment(HomeBottomTabFragment(), "landingFragment")
        }
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    /** Update chart with new data and X-axis labels */
    private fun updateChart(entries: List<Entry>, labels: List<String>, labelsDate: List<String>) {
        val dataSet = LineDataSet(entries, "Heart Rate Variability (ms)")
        dataSet.color = Color.RED
        dataSet.valueTextColor = Color.BLACK
        dataSet.setCircleColor(Color.RED)
        dataSet.circleRadius = 5f
        dataSet.lineWidth = 2f
        if (entries.size > 7) {
            dataSet.setDrawValues(false) // Hide values for larger datasets
        } else {
            dataSet.setDrawValues(true) // Show values for smaller datasets
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Customize X-Axis
        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels) // Set custom labels
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 10f
        xAxis.granularity = 1f
        xAxis.labelCount = labels.size
        xAxis.setDrawLabels(true)
        xAxis.labelRotationAngle = 0f // Optional, for vertical display
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        xAxis.yOffset = 15f // Move labels down

        // Customize Y-Axis
        val leftYAxis: YAxis = lineChart.axisLeft
        leftYAxis.textSize = 12f
        leftYAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        leftYAxis.setDrawGridLines(true)

        // Disable right Y-axis
        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.setExtraOffsets(0f, 0f, 0f, 0f)

        // Add click listener for chart points
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                selectHeartRateLayout.visibility = View.VISIBLE
                if (e != null) {
                    val x = e.x.toInt()
                    val y = e.y
                    selectedItemDate.text = labelsDate[x]
                    selectedHrvTv.text = y.toInt().toString()
                }
            }

            override fun onNothingSelected() {
                selectHeartRateLayout.visibility = View.INVISIBLE
            }
        })

        // Animate and refresh chart
        lineChart.animateY(1000)
        lineChart.invalidate()
    }

    /** Sample Data for Week */
    private fun getWeekData(): List<Entry> {
        return listOf(
            Entry(0f, 65f), Entry(1f, 75f), Entry(2f, 70f),
            Entry(3f, 85f), Entry(4f, 80f), Entry(5f, 60f), Entry(6f, 90f)
        )
    }

    private fun getWeekLabels(): List<String> {
        return listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    private fun getWeekLabelsDate(): List<String> {
        return listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    /** Sample Data for Month */
    private fun getMonthData(): List<Entry> {
        return listOf(
            Entry(0f, 40f), Entry(1f, 55f), Entry(2f, 35f),
            Entry(3f, 50f), Entry(4f, 30f)
        )
    }

    private fun getMonthLabels(): List<String> {
        return listOf("1-7", "8-14", "15-21", "22-28", "29-31")
    }

    private fun getMonthLabelsDate(): List<String> {
        return listOf("1-7", "8-14", "15-21", "22-28", "29-31")
    }

    /** Sample Data for 6 Months */
    private fun getSixMonthData(): List<Entry> {
        return listOf(
            Entry(0f, 45f), Entry(1f, 55f), Entry(2f, 50f),
            Entry(3f, 65f), Entry(4f, 70f), Entry(5f, 60f)
        )
    }

    private fun getSixMonthLabels(): List<String> {
        return listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    }

    private fun getSixMonthLabelsDate(): List<String> {
        return listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    }

    /** Fetch and update chart with API data */
    private fun fetchHeartRateVariability(period: String) {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                var selectedDate: String

                if (period.contentEquals("last_weekly")) {
                    if (selectedWeekDate.contentEquals("")) {
                        selectedDate = currentDateTime.format(formatter)
                        selectedWeekDate = selectedDate
                    } else {
                        selectedDate = selectedWeekDate
                    }
                    setSelectedDate(selectedWeekDate)
                } else if (period.contentEquals("last_monthly")) {
                    if (selectedMonthDate.contentEquals("")) {
                        selectedDate = currentDateTime.format(formatter)
                        val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
                        selectedDate = firstDateOfMonth
                        selectedMonthDate = firstDateOfMonth
                    } else {
                        val firstDateOfMonth = getFirstDateOfMonth(selectedMonthDate, 1)
                        selectedDate = firstDateOfMonth
                    }
                    setSelectedDateMonth(selectedMonthDate, "Month")
                } else {
                    if (selectedHalfYearlyDate.contentEquals("")) {
                        selectedDate = currentDateTime.format(formatter)
                        val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
                        selectedDate = firstDateOfMonth
                        selectedHalfYearlyDate = firstDateOfMonth
                    } else {
                        val firstDateOfMonth = getFirstDateOfMonth(selectedMonthDate, 1)
                        selectedDate = firstDateOfMonth
                    }
                    setSelectedDateMonth(selectedHalfYearlyDate, "Year")
                }

                val response = ApiClient.apiServiceFastApi.getHeartRate(
                    userId = "67f6698fa213d14e22a47c2a",
                    period = period,
                    date = selectedDate
                )

                if (response.isSuccessful) {
                    val heartRateVariability = response.body()
                    heartRateVariability?.let { data ->
                        val (entries, labels, labelsDate) = when (period) {
                            "last_weekly" -> processWeeklyData(data, selectedDate)
                            "last_monthly" -> processMonthlyData(data, selectedDate)
                            "last_six_months" -> processSixMonthsData(data, selectedDate)
                            else -> Triple(getWeekData(), getWeekLabels(), getWeekLabelsDate()) // Fallback
                        }
                        withContext(Dispatchers.Main) {
                            updateChart(entries, labels, labelsDate)
                            setLastAverageValue(data, period)
                        }
                    } ?: withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No HRV data received", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(requireContext(), "Exception: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    /** Process API data for last_weekly (7 days) */
    private fun processWeeklyData(data: HeartRateResponse, currentDate: String): Triple<List<Entry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateString = currentDate
        val date = dateFormat.parse(dateString) ?: return Triple(getWeekData(), getWeekLabels(), getWeekLabelsDate())
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(year, month, day)
        calendar.add(Calendar.DAY_OF_YEAR, -6)

        val hrvMap = mutableMapOf<String, Float>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        // Initialize 7 days
        repeat(7) {
            val dateStr = dateFormat.format(calendar.time)
            hrvMap[dateStr] = 0f
            val dateLabel = "${convertDate(dateStr)}, $year"
            val dayString = dayFormat.format(calendar.time)
            labels.add(dayString)
            labelsDate.add(dateLabel)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Aggregate HRV by day
        data.activeHeartRateTotals?.forEach { hrv ->
            val dayKey = hrv.date
            if (hrvMap.containsKey(dayKey)) {
                hrvMap[dayKey] = hrv.heartRate?.toFloat() ?: 0f
            }
        }

        // Create entries
        val entries = hrvMap.values.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }
        return Triple(entries, labels, labelsDate)
    }

    /** Process API data for last_monthly (5 weeks) */
    private fun processMonthlyData(data: HeartRateResponse, currentDate: String): Triple<List<Entry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateString = currentDate
        val date = dateFormat.parse(dateString) ?: return Triple(getMonthData(), getMonthLabels(), getMonthLabelsDate())
        calendar.time = date
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(year, month, day)

        val hrvMap = mutableMapOf<String, Float>()
        val weeklyLabels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()
        val days = getDaysInMonth(month + 1, year)

        repeat(days) {
            val dateStr = dateFormat.format(calendar.time)
            hrvMap[dateStr] = 0f
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        for (i in 0 until days) {
            weeklyLabels.add(
                when (i) {
                    2 -> "1-7"
                    9 -> "8-14"
                    15 -> "15-21"
                    22 -> "22-28"
                    29 -> "29-31"
                    else -> "" // empty string hides the label
                }
            )
            val dateLabel = "${convertMonth(dateString)}, $year"
            if (i < 7) {
                labelsDate.add("1-7 $dateLabel")
            } else if (i < 14) {
                labelsDate.add("8-14 $dateLabel")
            } else if (i < 21) {
                labelsDate.add("15-21 $dateLabel")
            } else if (i < 28) {
                labelsDate.add("22-28 $dateLabel")
            } else {
                labelsDate.add("29-31 $dateLabel")
            }
        }

        // Aggregate HRV by day
        data.activeHeartRateTotals?.forEach { hrv ->
            val dayKey = hrv.date
            if (hrvMap.containsKey(dayKey)) {
                hrvMap[dayKey] = hrv.heartRate?.toFloat() ?: 0f
            }
        }

        // Aggregate into weeks for chart
        val weeklyHrvMap = mutableMapOf<Int, Float>()
        repeat(5) { week ->
            weeklyHrvMap[week] = 0f
        }
        data.activeHeartRateTotals?.forEach { hrv ->
            val hrDate = dateFormat.parse(hrv.date) ?: return Triple(getMonthData(), getMonthLabels(), getMonthLabelsDate())
            calendar.time = hrDate
            val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
            val weekIndex = (dayOfMonth - 1) / 7 // 0-based week index
            if (weekIndex in 0..4) {
                weeklyHrvMap[weekIndex] = hrv.heartRate?.toFloat() ?: 0f
            }
        }

        val entries = weeklyHrvMap.values.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }
        return Triple(entries, weeklyLabels, labelsDate)
    }

    /** Process API data for last_six_months (6 months) */
    private fun processSixMonthsData(data: HeartRateResponse, currentDate: String): Triple<List<Entry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val startDateStr = data.startDate?.substring(0, 10) ?: currentDate
        val startDate = dateFormat.parse(startDateStr) ?: return Triple(getSixMonthData(), getSixMonthLabels(), getSixMonthLabelsDate())
        calendar.time = startDate
        calendar.add(Calendar.MONTH, -5) // Start 6 months back

        val hrvMap = mutableMapOf<Int, Float>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()

        // Initialize 6 months
        repeat(6) { month ->
            hrvMap[month] = 0f
            labels.add(monthFormat.format(calendar.time))
            labelsDate.add(monthFormat.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
        }

        // Aggregate HRV by month
        data.activeHeartRateTotals?.forEach { hrv ->
            val hrDate = dateFormat.parse(hrv.date) ?: return Triple(getSixMonthData(), getSixMonthLabels(), getSixMonthLabelsDate())
            calendar.time = hrDate
            val monthDiff = ((2025 - 1900) * 12 + Calendar.MARCH) - ((calendar.get(Calendar.YEAR) - 1900) * 12 + calendar.get(Calendar.MONTH))
            val monthIndex = 5 - monthDiff // Reverse to align with labels
            if (monthIndex in 0..5) {
                hrvMap[monthIndex] = hrv.heartRate?.toFloat() ?: 0f
            }
        }

        // Create entries
        val entries = hrvMap.values.mapIndexed { index, value ->
            Entry(index.toFloat(), value)
        }
        return Triple(entries, labels, labelsDate)
    }

    private fun setSelectedDate(selectedWeekDate: String) {
        requireActivity().runOnUiThread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = selectedWeekDate
            val date = dateFormat.parse(dateString) ?: return@runOnUiThread
            calendar.time = date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -6)
            val dateStr = dateFormat.format(calendar.time)
            val dateView: String = "${convertDate(dateStr)} - ${convertDate(selectedWeekDate)}, $year"
            selectedDate.text = dateView
            selectedDate.gravity = Gravity.CENTER
        }
    }

    private fun setSelectedDateMonth(selectedMonthDate: String, dateViewType: String) {
        activity?.runOnUiThread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = selectedMonthDate
            val date = dateFormat.parse(dateString) ?: return@runOnUiThread
            calendar.time = date
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            if (dateViewType.contentEquals("Month")) {
                val lastDayOfMonth = getDaysInMonth(month + 1, year)
                val lastDateOfMonth = getFirstDateOfMonth(selectedMonthDate, lastDayOfMonth)
                val dateView: String = "${convertDate(selectedMonthDate)} - ${convertDate(lastDateOfMonth)}, $year"
                selectedDate.text = dateView
                selectedDate.gravity = Gravity.CENTER
            } else {
                selectedDate.text = year.toString()
                selectedDate.gravity = Gravity.CENTER
            }
        }
    }

    private fun setLastAverageValue(data: HeartRateResponse, period: String) {
        activity?.runOnUiThread {
            val averageHrv = data.currentAverageHeartRate.toInt() ?: 0
            averageHrvTv.text = averageHrv.toString()
            val progressPercentage = data.progressPercentage.toInt() ?: 0
            val progressSign = data.progressSign ?: "neutral"
            val periodText = when (period) {
                "last_weekly" -> "% Past Week"
                "last_monthly" -> "% Past Month"
                "last_six_months" -> "% Past 6 Months"
                else -> ""
            }
            percentageTv.text = "$progressPercentage $periodText"
            when (progressSign) {
                "plus" -> percentageIc.setImageResource(R.drawable.ic_up)
                "minus" -> percentageIc.setImageResource(R.drawable.ic_down)
                else -> percentageIc.setImageResource(0) // Clear icon if neutral
            }
        }
    }

    private fun convertDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM", Locale.getDefault())
        return try {
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    private fun convertMonth(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("MMM", Locale.getDefault())
        return try {
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    private fun getDaysInMonth(month: Int, year: Int): Int {
        val yearMonth = YearMonth.of(year, month)
        return yearMonth.lengthOfMonth()
    }

    private fun getFirstDateOfMonth(inputDate: String, value: Int): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val parsedDate = LocalDate.parse(inputDate, formatter)
        val firstDayOfMonth = parsedDate.withDayOfMonth(value)
        return firstDayOfMonth.format(formatter)
    }
}