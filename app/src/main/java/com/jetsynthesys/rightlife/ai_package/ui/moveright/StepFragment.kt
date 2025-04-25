package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.IMarker
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.MarkerView
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.utils.MPPointF
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.HeartRateFitDataResponse
import com.jetsynthesys.rightlife.databinding.FragmentStepBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class StepFragment : BaseFragment<FragmentStepBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentStepBinding
        get() = FragmentStepBinding::inflate

    private lateinit var barChart: BarChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var lineChart: LineChart
    private lateinit var stepBck: ImageView
    private lateinit var layoutLineChart: FrameLayout
    private lateinit var stripsContainer: FrameLayout
    private lateinit var selectStepRateLayout: CardView
    private lateinit var selectedItemDate: TextView
    private lateinit var selectedCalorieTv: TextView
    private lateinit var backwardImage: ImageView
    private lateinit var forwardImage: ImageView
    private lateinit var selectedDate: TextView
    private var selectedWeekDate: String = ""
    private var selectedMonthDate: String = ""
    private var selectedHalfYearlyDate: String = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.gradient_color_background_workout)

        barChart = view.findViewById(R.id.heartRateChart)
        radioGroup = view.findViewById(R.id.tabGroup)
        layoutLineChart = view.findViewById(R.id.lyt_line_chart)
        stripsContainer = view.findViewById(R.id.stripsContainer)
        lineChart = view.findViewById(R.id.heartLineChart)
        stepBck = view.findViewById(R.id.step_back)
        selectedItemDate = view.findViewById(R.id.selectedItemDate)
        selectStepRateLayout = view.findViewById(R.id.selectStepRateLayout)
        selectedCalorieTv = view.findViewById(R.id.selectedCalorieTv)
        backwardImage = view.findViewById(R.id.backward_image_heart_rate)
        forwardImage = view.findViewById(R.id.forward_image_heart_rate)
        selectedDate = view.findViewById(R.id.selectedDate)

        stepBck.setOnClickListener {
            navigateToFragment(MoveRightLandingFragment(), "landingFragment")
        }

        // Initial chart setup with sample data
        fetchActiveCalories("weekly")

        // Set default selection to Week
        radioGroup.check(R.id.rbWeek)

        // Handle Radio Button Selection
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbWeek -> {
                    barChart.visibility = View.VISIBLE
                    layoutLineChart.visibility = View.GONE
                    fetchActiveCalories("weekly")
                }
                R.id.rbMonth -> {
                    barChart.visibility = View.VISIBLE
                    layoutLineChart.visibility = View.GONE
                    fetchActiveCalories("monthly")
                }
                R.id.rbSixMonths -> {
                    barChart.visibility = View.GONE
                    layoutLineChart.visibility = View.VISIBLE
                    setupCustomChart(requireContext(), lineChart)
                   // fetchActiveCalories("six_months")
                }
            }
        }

        backwardImage.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            var selectedTab: String = "Week"
            if (selectedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(selectedId)
                selectedTab = selectedRadioButton.text.toString()
            }

            if (selectedTab == "Week") {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val dateString = selectedWeekDate
                val date = dateFormat.parse(dateString)
                calendar.time = date ?: Calendar.getInstance().time
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                calendar.set(year, month, day)
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val dateStr = dateFormat.format(calendar.time)
                selectedWeekDate = dateStr
                fetchActiveCalories("weekly")
            } else if (selectedTab == "Month") {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                val calendar = Calendar.getInstance()
                val dateString = selectedMonthDate
                val date = dateFormat.parse(dateString)
                calendar.time = date ?: Calendar.getInstance().time
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                calendar.set(year, month - 1, day)
                val dateStr = dateFormat.format(calendar.time)
                val firstDateOfMonth = getFirstDateOfMonth(dateStr, 1)
                selectedMonthDate = firstDateOfMonth
                fetchActiveCalories("monthly")
            } else {
                selectedHalfYearlyDate = ""
                fetchActiveCalories("six_months")
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

            if (selectedTab == "Week") {
                if (selectedWeekDate != currentDate) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val dateString = selectedWeekDate
                    val date = dateFormat.parse(dateString)
                    calendar.time = date ?: Calendar.getInstance().time
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    calendar.set(year, month, day)
                    calendar.add(Calendar.DAY_OF_YEAR, 7)
                    val dateStr = dateFormat.format(calendar.time)
                    selectedWeekDate = dateStr
                    fetchActiveCalories("weekly")
                } else {
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            } else if (selectedTab == "Month") {
                if (selectedMonthDate != currentDate) {
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val dateString = selectedMonthDate
                    val date = dateFormat.parse(dateString)
                    calendar.time = date ?: Calendar.getInstance().time
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    calendar.set(year, month + 1, day)
                    val dateStr = dateFormat.format(calendar.time)
                    val firstDateOfMonth = getFirstDateOfMonth(dateStr, 1)
                    selectedMonthDate = firstDateOfMonth
                    fetchActiveCalories("monthly")
                } else {
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (selectedHalfYearlyDate != currentDate) {
                    selectedHalfYearlyDate = ""
                    fetchActiveCalories("six_months")
                } else {
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToFragment(MoveRightLandingFragment(), "landingFragment")
        }
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    /** Update BarChart with new data */
    private fun updateChart(entries: List<BarEntry>, labels: List<String>, labelsDate: List<String>, goal: Float) {
        val dataSet = BarDataSet(entries, "Steps Count")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.moveright)
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        dataSet.valueTextSize = 12f
        if (entries.size > 7) {
            dataSet.setDrawValues(false)
        } else {
            dataSet.setDrawValues(true)
        }
        dataSet.barShadowColor = Color.TRANSPARENT
        dataSet.highLightColor = ContextCompat.getColor(requireContext(), R.color.light_orange)
        val barData = BarData(dataSet)
        barData.barWidth = 0.4f
        barChart.data = barData
        barChart.setFitBars(true)

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 10f
        xAxis.granularity = 1f
        xAxis.labelCount = labels.size
        xAxis.setDrawLabels(true)
        xAxis.labelRotationAngle = 0f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        xAxis.yOffset = 15f

        val leftYAxis: YAxis = barChart.axisLeft
        leftYAxis.textSize = 12f
        leftYAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        leftYAxis.setDrawGridLines(true)

        // Add goal line based on steps_goal
        val goalLine = LimitLine(goal, "Goal: $goal steps")
        goalLine.lineWidth = 2f
        goalLine.lineColor = Color.BLACK
        goalLine.enableDashedLine(10f, 10f, 0f)
        goalLine.textColor = Color.BLACK
        goalLine.textSize = 12f
        leftYAxis.removeAllLimitLines()
        leftYAxis.addLimitLine(goalLine)

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setExtraOffsets(0f, 0f, 0f, 0f)

        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                selectStepRateLayout.visibility = View.VISIBLE
                if (e != null) {
                    val x = e.x.toInt()
                    val y = e.y
                    Log.d("ChartClick", "Clicked X: $x, Y: $y")
                    selectedItemDate.text = labelsDate.getOrNull(x) ?: ""
                    selectedCalorieTv.text = y.toInt().toString() + " steps"
                }
            }

            override fun onNothingSelected() {
                Log.d("ChartClick", "Nothing selected")
                selectStepRateLayout.visibility = View.INVISIBLE
            }
        })

        barChart.animateY(1000)
        barChart.invalidate()
    }
    fun setupCustomChart(context: Context, chart: LineChart) {
        val months = listOf("Jan 2025", "Feb 2025", "Mar 2025", "Apr 2025", "May 2025")
        val values = listOf(1.71f, 1.38f, 1.51f, 1.44f, 1.48f)
        val percentChanges = listOf("", "-0.33%", "+0.13%", "-0.07%", "+0.4%")
        val entries = values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        val lineDataSet = LineDataSet(entries, "Monthly Avg").apply {
            color = Color.BLACK
            setCircleColor(Color.BLACK)
            lineWidth = 2f
            circleRadius = 4f
            setDrawValues(false)
            mode = LineDataSet.Mode.CUBIC_BEZIER
        }
        val overallAvg = values.average().toFloat()
        val avgLine = LimitLine(overallAvg, "Avg ${"%.2f".format(overallAvg)}").apply {
            lineColor = Color.RED
            lineWidth = 1.5f
            textColor = Color.RED
            textSize = 12f
            enableDashedLine(10f, 10f, 0f)
        }
        chart.axisLeft.apply {
            removeAllLimitLines()
            addLimitLine(avgLine)
            axisMinimum = 0f
            axisMaximum = 2.8f
            textColor = Color.DKGRAY
        }
        chart.axisRight.isEnabled = false
        chart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(months)
            textColor = Color.DKGRAY
        }
        chart.apply {
            description.isEnabled = false
            legend.isEnabled = false
            data = LineData(lineDataSet)
            setTouchEnabled(false)
            invalidate()
        }
        chart.marker = object : MarkerView(context, R.layout.marker_layout), IMarker {
            private val tvMarker = findViewById<TextView>(R.id.tvMarker)
            override fun refreshContent(e: Entry?, highlight: Highlight?) {
                val index = e?.x?.toInt() ?: 0
                tvMarker.text = "${"%.2f".format(values[index])} ${percentChanges[index]}"
                tvMarker.setTextColor(if (percentChanges[index].contains("-")) Color.RED else Color.GREEN)
                super.refreshContent(e, highlight)
            }

            override fun draw(canvas: Canvas?, posX: Float, posY: Float) {
                TODO("Not yet implemented")
            }

            override fun getOffset(): MPPointF {
                return MPPointF((-width / 2).toFloat(), -height.toFloat())
            }

            override fun getOffsetForDrawingAtPoint(posX: Float, posY: Float): MPPointF {
                return MPPointF((-width / 2).toFloat(), -height.toFloat())
            }
        }
    }


    /** Fetch and update chart with API data */
    private fun fetchActiveCalories(period: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                var selectedDate: String

                if (period == "weekly") {
                    if (selectedWeekDate.isEmpty()) {
                        selectedDate = currentDateTime.format(formatter)
                        selectedWeekDate = selectedDate
                    } else {
                        selectedDate = selectedWeekDate
                    }
                    setSelectedDate(selectedWeekDate)
                } else if (period == "monthly") {
                    if (selectedMonthDate.isEmpty()) {
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
                    if (selectedHalfYearlyDate.isEmpty()) {
                        selectedDate = currentDateTime.format(formatter)
                        selectedHalfYearlyDate = selectedDate
                    } else {
                        selectedDate = selectedHalfYearlyDate
                    }
                    setSelectedDateMonth(selectedHalfYearlyDate, "Year")
                }

                val response = ApiClient.apiServiceFastApi.getStepsDetail(
                    userId = "67f6698fa213d14e22a47c2a",
                    source = "apple",
                    period = period,
                    date = "2025-04-07"
                )

                if (response.isSuccessful) {
                    val heartRateFitDataResponse = response.body()
                    heartRateFitDataResponse?.let { data ->
                        val (entries, labels, labelsDate) = when (period) {
                            "weekly" -> processWeeklyData(data)
                            "monthly" -> processMonthlyData(data)
                            "six_months" -> processSixMonthsData(data)
                            else -> processWeeklyData(data)
                        }
                        val goal = data.data.firstOrNull()?.stepsGoal?.toFloat() ?: 1000f
                        val totalSteps = data.data.firstOrNull()?.totalStepsCount?.toInt() ?: 0
                        withContext(Dispatchers.Main) {
                            if (period == "six_months") {
                                layoutLineChart.visibility = View.VISIBLE
                                barChart.visibility = View.GONE
                                lineChartForSixMonths(data)
                            } else {
                                barChart.visibility = View.VISIBLE
                                layoutLineChart.visibility = View.GONE
                                updateChart(entries, labels, labelsDate, goal)
                            }
                            Toast.makeText(
                                requireContext(),
                                "Total Steps: $totalSteps",
                                Toast.LENGTH_LONG
                            ).show()
                        }
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

    /** Process API data for weekly (7 days) */
    private fun processWeeklyData(data: HeartRateFitDataResponse): Triple<List<BarEntry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateString = selectedWeekDate.ifEmpty { "2025-04-07" }
        val date = dateFormat.parse(dateString) ?: Date()
        calendar.time = date
        calendar.add(Calendar.DAY_OF_YEAR, -6)

        val stepMap = mutableMapOf<String, Double>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        // Initialize 7 days with 0 steps
        repeat(7) {
            val dateStr = dateFormat.format(calendar.time)
            stepMap[dateStr] = 0.0
            labels.add(dayFormat.format(calendar.time))
            labelsDate.add(convertDate(dateStr) + "," + calendar.get(Calendar.YEAR))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Aggregate steps from record_details
        data.data.firstOrNull()?.recordDetails?.forEach { record ->
            val date = dateFormat.parse(record.date)
            if (date != null) {
                val dayKey = dateFormat.format(date)
                stepMap[dayKey] = (record.totalStepsCountPerDay ?: 0.0).toDouble()
            }
        }

        val entries = stepMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }
        return Triple(entries, labels, labelsDate)
    }

    /** Process API data for monthly (4-5 weeks) */
    private fun processMonthlyData(data: HeartRateFitDataResponse): Triple<List<BarEntry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateString = selectedMonthDate.ifEmpty { "2025-04-01" }
        val date = dateFormat.parse(dateString) ?: Date()
        calendar.time = date

        val stepMap = mutableMapOf<Int, Double>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()

        val days = getDaysInMonth(calendar.get(Calendar.MONTH) + 1, calendar.get(Calendar.YEAR))
        repeat(5) { week ->
            val startDay = week * 7 + 1
            val endDay = if (week == 4) days else startDay + 6
            stepMap[week] = 0.0
            labels.add("$startDay-$endDay")
            if (week == 0) labelsDate.add("1-7 ${convertMonth(dateString)},${calendar.get(Calendar.YEAR)}")
            else if (week == 1) labelsDate.add("8-14 ${convertMonth(dateString)},${calendar.get(Calendar.YEAR)}")
            else if (week == 2) labelsDate.add("15-21 ${convertMonth(dateString)},${calendar.get(Calendar.YEAR)}")
            else if (week == 3) labelsDate.add("22-28 ${convertMonth(dateString)},${calendar.get(Calendar.YEAR)}")
            else labelsDate.add("29-${days} ${convertMonth(dateString)},${calendar.get(Calendar.YEAR)}")
        }

        // Aggregate steps by week
        data.data.firstOrNull()?.recordDetails?.forEach { record ->
            val date = dateFormat.parse(record.date)
            if (date != null) {
                calendar.time = date
                val dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH)
                val weekIndex = (dayOfMonth - 1) / 7
                if (weekIndex in 0..4) {
                    stepMap[weekIndex] = stepMap[weekIndex]!! + (record.totalStepsCountPerDay ?: 0.0).toFloat()
                }
            }
        }

        val entries = stepMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }
        return Triple(entries, labels, labelsDate)
    }

    /** Process API data for six_months (6 months) */
    private fun processSixMonthsData(data: HeartRateFitDataResponse): Triple<List<BarEntry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateString = selectedHalfYearlyDate.ifEmpty { "2025-04-07" }
        val date = dateFormat.parse(dateString) ?: Date()
        calendar.time = date
        calendar.add(Calendar.MONTH, -5)

        val stepMap = mutableMapOf<Int, Double>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()

        repeat(6) { month ->
            stepMap[month] = 0.0
            val monthLabel = monthFormat.format(calendar.time)
            labels.add(monthLabel)
            labelsDate.add("${monthLabel},${calendar.get(Calendar.YEAR)}")
            calendar.add(Calendar.MONTH, 1)
        }

        // Aggregate steps by month
        data.data.firstOrNull()?.recordDetails?.forEach { record ->
            val date = dateFormat.parse(record.date)
            if (date != null) {
                calendar.time = date
                val monthDiff = ((calendar.get(Calendar.YEAR) - 1900) * 12 + calendar.get(Calendar.MONTH)) - ((2025 - 1900) * 12 + Calendar.APRIL)
                val monthIndex = monthDiff // Align with current month order
                if (monthIndex in 0..5) {
                    stepMap[monthIndex] = stepMap[monthIndex]!! + (record.totalStepsCountPerDay ?: 0.0).toFloat()
                }
            }
        }

        val entries = stepMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value.toFloat()) }
        return Triple(entries, labels, labelsDate)
    }

    private fun setSelectedDate(selectedWeekDate: String) {
        activity?.runOnUiThread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = selectedWeekDate
            val date = dateFormat.parse(dateString)
            calendar.time = date ?: Calendar.getInstance().time
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -6)
            val dateStr = dateFormat.format(calendar.time)
            val dateView: String = "${convertDate(dateStr)}-${convertDate(selectedWeekDate)},$year"
            selectedDate.text = dateView
            selectedDate.gravity = Gravity.CENTER
        }

    }

    private fun setSelectedDateMonth(selectedMonthDate: String, dateViewType: String) {
        activity?.runOnUiThread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = selectedMonthDate
            val date = dateFormat.parse(dateString)
            calendar.time = date ?: Calendar.getInstance().time
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            if (dateViewType == "Month") {
                val lastDayOfMonth = getDaysInMonth(month + 1, year)
                val lastDateOfMonth = getFirstDateOfMonth(selectedMonthDate, lastDayOfMonth)
                val dateView: String = "${convertDate(selectedMonthDate)}-${convertDate(lastDateOfMonth)},$year"
                selectedDate.text = dateView
                selectedDate.gravity = Gravity.CENTER
            } else {
                selectedDate.text = year.toString()
                selectedDate.gravity = Gravity.CENTER
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

    private fun lineChartForSixMonths(data: HeartRateFitDataResponse) {
        val recordDetails = data.data.firstOrNull()?.recordDetails ?: emptyList()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateString = selectedHalfYearlyDate.ifEmpty { "2025-04-07" }
        val date = dateFormat.parse(dateString) ?: Date()
        calendar.time = date
        calendar.add(Calendar.MONTH, -5)

        val stepMap = mutableMapOf<String, Double>()
        val months = mutableListOf<String>()

        repeat(6) {
            val monthKey = monthFormat.format(calendar.time)
            stepMap[monthKey] = 0.0
            months.add(monthKey)
            calendar.add(Calendar.MONTH, 1)
        }

        recordDetails.forEach { record ->
            val date = dateFormat.parse(record.date)
            if (date != null) {
                calendar.time = date
                val monthKey = monthFormat.format(date)
                stepMap[monthKey] = stepMap[monthKey]!! + (record.totalStepsCountPerDay ?: 0.0).toFloat()
            }
        }

        val entries = stepMap.map { Entry(months.indexOf(it.key).toFloat(), it.value.toFloat()) }

        val dataSet = LineDataSet(entries, "Steps Count").apply {
            color = Color.BLUE
            valueTextSize = 12f
            circleColors = listOf(Color.BLUE)
            setDrawCircleHole(false)
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.apply {
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(months)
                textSize = 12f
                granularity = 1f
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
            invalidate()
        }

        lineChart.post {
            val transformer = lineChart.getTransformer(YAxis.AxisDependency.LEFT)
            for (entry in entries) {
                if (entry.y == 0f) continue
                val pixelValues = transformer.getPixelForValues(entry.x, entry.y)
                val stripView = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(100, 12)
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 6f
                        setColor(if (entry.y >= 7000) Color.GREEN else Color.RED)
                    }
                    x = (pixelValues.x).toFloat()
                    y = (pixelValues.y - 6).toFloat()
                }
                stripsContainer.addView(stripView)
            }
        }
    }
}