package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

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
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.R.*
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.response.WeightResponse
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.databinding.FragmentWeightTrackerBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class WeightTrackerFragment : BaseFragment<FragmentWeightTrackerBinding>() {

    private lateinit var lineChart: LineChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var stripsContainer: FrameLayout
    private lateinit var layoutLineChart: FrameLayout
    private lateinit var backwardImage: ImageView
    private lateinit var forwardImage: ImageView
    private lateinit var selectedDate: TextView
    private var selectedWeekDate: String = ""
    private var selectedMonthDate: String = ""
    private var selectedHalfYearlyDate: String = ""
    private lateinit var selectedItemDate : TextView
    private lateinit var selectHeartRateLayout : CardView
    private lateinit var selectedCalorieTv : TextView

    private val viewModel: HydrationViewModelNew by viewModels()


    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWeightTrackerBinding
        get() = FragmentWeightTrackerBinding::inflate


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), color.meal_log_background))

        lineChart = view.findViewById(R.id.heartLineChart)
        radioGroup = view.findViewById(R.id.tabGroup)
        stripsContainer = view.findViewById(R.id.stripsContainer)
        layoutLineChart = view.findViewById(R.id.lyt_line_chart)
        backwardImage = view.findViewById(R.id.backward_image_calorie)
        forwardImage = view.findViewById(R.id.forward_image_calorie)
        selectedDate = view.findViewById(R.id.selectedDate)
        selectHeartRateLayout = view.findViewById(R.id.selectHeartRateLayout)
        selectedItemDate = view.findViewById(R.id.selectedItemDate)
        selectedCalorieTv = view.findViewById(R.id.selectedCalorieTv)


        // Show Week data by default
        radioGroup.check(R.id.rbMonth)
        fetchWeightData("last_monthly")
        //setupLineChart()

        // Handle Radio Button Selection
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbWeek ->{
                    layoutLineChart.visibility =View.VISIBLE

                    fetchWeightData("last_weekly")
                }
                R.id.rbMonth ->{
                    layoutLineChart.visibility =View.VISIBLE

                    fetchWeightData("last_monthly")
                }
                R.id.rbSixMonths -> {
                    Toast.makeText(requireContext(),"Comming Soon",Toast.LENGTH_SHORT).show()
                    layoutLineChart.visibility =View.GONE
                    //fetchWaterIntakeData("last_six_months")
                }
            }
        }

        backwardImage.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            var selectedTab: String = "Month"
            if (selectedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(selectedId)
                selectedTab = selectedRadioButton.text.toString()
            }

            /*if (selectedTab.contentEquals("Week")) {
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
                fetchWeightData("last_weekly")
            } else*/ if (selectedTab.contentEquals("Month")) {
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
                fetchWeightData("last_monthly")
            } else {
            Toast.makeText(requireContext(),"Comming Soon",Toast.LENGTH_SHORT).show()
            layoutLineChart.visibility =View.GONE
                /*selectedHalfYearlyDate = ""
                fetchWeightData("last_six_months")*/
            }
        }

        forwardImage.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            var selectedTab: String = "Month"
            if (selectedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(selectedId)
                selectedTab = selectedRadioButton.text.toString()
            }
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val currentDate: String = currentDateTime.format(formatter)

            /*if (selectedTab.contentEquals("Week")) {
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
                    fetchWeightData("last_weekly")
                } else {
                    Toast.makeText(context, "Cannot select future date", Toast.LENGTH_SHORT).show()
                }
            } else*/ if (selectedTab.contentEquals("Month")) {
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
                    fetchWeightData("last_monthly")
                } else {
                    Toast.makeText(context, "Cannot select future date", Toast.LENGTH_SHORT).show()
                }
            } else {
            Toast.makeText(requireContext(),"Comming Soon",Toast.LENGTH_SHORT).show()
            layoutLineChart.visibility =View.GONE
                /*if (!selectedHalfYearlyDate.contentEquals(currentDate)) {
                    selectedHalfYearlyDate = ""
                    fetchWeightData("last_six_months")
                } else {
                    Toast.makeText(context, "Cannot select future date", Toast.LENGTH_SHORT).show()
                }*/
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToFragment(HomeBottomTabFragment(), "landingFragment")
        }
    }
   /* private fun fetchWeightData(period: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val response = ApiClient.apiServiceFastApi.getLogWeight(
                    userId = "67f6698fa213d14e22a47c2a",
                    period = period,
                    date = "2025-04-22"
                )


                if (response.isSuccessful) {
                    val weightData = response.body()
                    weightData?.let { data ->
                        val (entries, labels) = when (period) {
                            "last_monthly" -> {
                                val entries = data.weightTotals.mapIndexed { index, item ->
                                    Entry(index.toFloat(), item.weight.toFloat())
                                }

                                // Extract year and month from first item (assuming it's non-empty)
                                val firstDate = data.weightTotals.firstOrNull()?.date ?: ""
                                val dateParts = firstDate.split("-")
                                val year = dateParts.getOrNull(0) ?: ""
                                val month = dateParts.getOrNull(1) ?: ""
                                val monthName = convertMonth(month)

                                val labelsDate = mutableListOf<String>()
                                for (i in data.weightTotals.indices) {
                                    labelsDate.add(
                                        when (i) {
                                            in 0..6 -> "1-7 "
                                            in 7..13 -> "8-14 "
                                            in 14..20 -> "15-21 "
                                            in 21..27 -> "22-28 "
                                            else -> "29-31 "
                                        }
                                    )
                                }

                                Pair(entries, labelsDate)
                            }

                            "last_six_months" -> {
                                val entries = data.weightTotals.mapIndexed { index, item ->
                                    Entry(index.toFloat(), item.weight.toFloat())
                                }

                                val labels = data.weightTotals.map { item ->
                                    val dateParts = item.date.split("-")
                                    val year = dateParts.getOrNull(0) ?: ""
                                    val month = dateParts.getOrNull(1) ?: ""
                                    val monthName = convertMonth(month)
                                    "$monthName"
                                }

                                Pair(entries, labels)
                            }

                            else -> Pair(emptyList(), emptyList())
                        }

                        val averageWeight = data.weightTotals
                            .map { it.weight }
                            .average()

                        withContext(Dispatchers.Main) {
                            updateChart(entries, labels)
                            Log.d("AvgWeight", "Average Weight: $averageWeight")
                        }
                    } ?: withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No weight data received", Toast.LENGTH_SHORT).show()
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
    }*/
    private fun fetchWeightData(period: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val userId = "67f6698fa213d14e22a47c2a" // Replace with dynamic user ID if needed
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                var selectedDate: String

                if (period.contentEquals("last_weekly")) {
                    if (selectedWeekDate.isEmpty()) {
                        selectedDate = currentDateTime.format(formatter)
                        selectedWeekDate = selectedDate
                    } else {
                        selectedDate = selectedWeekDate
                    }
                    setSelectedDate(selectedWeekDate)
                } else if (period.contentEquals("last_monthly")) {
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
                        val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
                        selectedDate = firstDateOfMonth
                        selectedHalfYearlyDate = firstDateOfMonth
                    } else {
                        val firstDateOfMonth = getFirstDateOfMonth(selectedMonthDate, 1)
                        selectedDate = firstDateOfMonth
                    }
                    setSelectedDateMonth(selectedHalfYearlyDate, "Year")
                }

                val response = ApiClient.apiServiceFastApi.getLogWeight(
                    userId = userId,
                    period = period,
                    date = selectedDate
                )

                if (response.isSuccessful) {
                    val waterData = response.body()
                    waterData?.let { data ->
                        val (entries, labels) = when (period) {
                            "last_weekly" -> processWeeklyData(data, selectedDate)
                            "last_monthly" -> processMonthlyData(data, selectedDate)
                            "last_six_months" -> processSixMonthsData(data, selectedDate)
                            else -> Pair(emptyList(), emptyList())
                        }

                        withContext(Dispatchers.Main) {
                            if (data.weightTotals.size > 31) {
                                layoutLineChart.visibility = View.VISIBLE
                                lineChartForSixMonths()
                            } else {
                                layoutLineChart.visibility = View.VISIBLE
                                updateChart(entries, labels)
                            }
                        }
                    } ?: withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No water intake data received", Toast.LENGTH_SHORT).show()
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

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    private fun setupLineChart() {
        lineChart.apply {
            axisLeft.apply {
                axisMaximum = viewModel.yMax.toFloat() * 1.2f
                axisMinimum = 0f
                setDrawGridLines(true)
                textColor = Color.BLACK
                textSize = 10f
                setLabelCount(6, true)
                valueFormatter = object : IndexAxisValueFormatter() {
                    override fun getFormattedValue(value: Float): String {
                        return if (value == 0f) "0" else "${(value / 1000).toInt()}k"
                    }
                }
                setDrawAxisLine(true)
            }
            axisRight.isEnabled = false
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                textColor = Color.BLACK
                textSize = 10f
                setDrawGridLines(true)
                setDrawLabels(true)
                granularity = 1f
                setAvoidFirstLastClipping(true)
            }
            description.isEnabled = false
            setTouchEnabled(true)
            setDrawGridBackground(false)
            setDrawBorders(false)
            setNoDataText("Loading data...")
        }
    }

    private fun lineChartForSixMonths() {
        val entries = if (viewModel.filteredData.isNotEmpty()) {
            viewModel.filteredData.mapIndexed { index, item ->
                Entry(index.toFloat(), item.waterMl.toFloat())
            }
        } else {
            listOf(Entry(0f, 0f), Entry(1f, 0f))
        }
        Log.d("HydrationTracker", "lineChartForSixMonths: entries size = ${entries.size}")

        val dataSet = LineDataSet(entries, "Water Intake (ml)").apply {
            color = Color.rgb(255, 102, 128)
            lineWidth = 1f
            setDrawCircles(false)
            setDrawValues(false)
            mode = LineDataSet.Mode.LINEAR
            fillAlpha = 20
            fillColor = Color.rgb(255, 102, 128)
            setDrawFilled(true)
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(
            viewModel.monthlyGroups.map { formatDate(it.startDate, "LLL\nyyyy") }.ifEmpty { listOf("No Data", "No Data") }
        )
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            textColor = Color.BLACK
            textSize = 10f
            setDrawGridLines(true)
            setDrawLabels(true)
            granularity = 1f
            labelCount = viewModel.monthlyGroups.size.coerceAtLeast(2)
            setAvoidFirstLastClipping(true)
        }

        lineChart.axisLeft.apply {
            axisMaximum = viewModel.yMax.toFloat() * 1.2f
            axisMinimum = 0f
            setDrawGridLines(true)
            textColor = Color.BLACK
            textSize = 10f
            setLabelCount(6, true)
            valueFormatter = object : IndexAxisValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return if (value == 0f) "0" else "${(value / 1000).toInt()}k"
                }
            }
            setDrawAxisLine(true)
        }
        lineChart.axisRight.isEnabled = false

        lineChart.animateX(1000)
        lineChart.animateY(1000)
        lineChart.invalidate()

        lineChart.post {
            val transformer = lineChart.getTransformer(YAxis.AxisDependency.LEFT)
            val hMargin = 40f
            val vMargin = 20f
            if (viewModel.currentRange == RangeTypeChartsNew.SIX_MONTHS) {
                val overallStart = viewModel.filteredData.firstOrNull()?.date ?: viewModel.startDate
                val overallEnd = viewModel.filteredData.lastOrNull()?.date ?: viewModel.endDate
                val totalInterval = if (overallEnd.time > overallStart.time) overallEnd.time - overallStart.time.toDouble() else 1.0

                Log.d("HydrationTracker", "filteredData size: ${viewModel.filteredData.size}, overallStart: $overallStart, overallEnd: $overallEnd")

                stripsContainer.removeAllViews()
                viewModel.monthlyGroups.forEach { group ->
                    val monthEntries = viewModel.filteredData.filter { it.date >= group.startDate && it.date <= group.endDate }
                    if (monthEntries.isNotEmpty()) {
                        val startIndex = viewModel.filteredData.indexOfFirst { it.date >= group.startDate }
                        val endIndex = viewModel.filteredData.indexOfLast { it.date <= group.endDate }
                        if (startIndex >= 0 && endIndex >= 0) {
                            val xStartFraction = startIndex.toDouble() / viewModel.filteredData.size
                            val xEndFraction = endIndex.toDouble() / viewModel.filteredData.size
                            val yFraction = group.avgWater.toDouble() / viewModel.yMax
                            val pixelStart = transformer.getPixelForValues(
                                (xStartFraction * (lineChart.width - 2 * hMargin)).toFloat() + hMargin,
                                ((1 - yFraction) * (lineChart.height - 2 * vMargin) + vMargin).toFloat()
                            )
                            val pixelEnd = transformer.getPixelForValues(
                                (xEndFraction * (lineChart.width - 2 * hMargin)).toFloat() + hMargin,
                                ((1 - yFraction) * (lineChart.height - 2 * vMargin) + vMargin).toFloat()
                            )

                            // Draw red capsule
                            val capsuleView = View(requireContext()).apply {
                                val width = (pixelEnd.x - pixelStart.x).toInt().coerceAtLeast(20)
                                layoutParams = FrameLayout.LayoutParams(width, 8)
                                background = GradientDrawable().apply {
                                    shape = GradientDrawable.RECTANGLE
                                    cornerRadius = 4f
                                    setColor(Color.RED)
                                }
                                x = pixelStart.x.toFloat()
                                y = (pixelStart.y - 3).toFloat()
                            }
                            stripsContainer.addView(capsuleView)

                            // Average label
                            val avgText = TextView(requireContext()).apply {
                                text = "${group.avgWater / 1000}k"
                                setTextColor(Color.BLACK)
                                textSize = 12f
                                setPadding(6, 4, 6, 4)
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                x = ((pixelStart.x + pixelEnd.x) / 2 - 20).toFloat()
                                y = (pixelStart.y - 20).toFloat()
                            }
                            stripsContainer.addView(avgText)

                            // Difference label
                            val diffText = TextView(requireContext()).apply {
                                text = group.monthDiffString
                                setTextColor(if (group.isPositiveChange) Color.GREEN else Color.RED)
                                textSize = 12f
                                setPadding(6, 4, 6, 4)
                                layoutParams = FrameLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT
                                )
                                x = ((pixelStart.x + pixelEnd.x) / 2 - 20).toFloat()
                                y = (pixelStart.y + 20).toFloat()
                            }
                            stripsContainer.addView(diffText)
                        }
                    }
                }
            }
        }
    }

    private fun formatDate(date: Date, format: String): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }



    private fun updateChart(entries: List<Entry>, labels: List<String>) {
        val dataSet = LineDataSet(entries, "Water Intake (ml)")
        dataSet.color = context?.let { ContextCompat.getColor(it, R.color.border_green) }!!
        dataSet.valueTextColor = Color.BLACK
        dataSet.setCircleColor(context?.let { ContextCompat.getColor(it, R.color.border_green) }!!)
        dataSet.circleRadius = 5f
        dataSet.lineWidth = 2f
        dataSet.setDrawValues(false)

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 12f
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.BLACK
        xAxis.yOffset = 15f

        val leftYAxis: YAxis = lineChart.axisLeft
        leftYAxis.textSize = 12f
        leftYAxis.textColor = Color.BLACK
        leftYAxis.setDrawGridLines(true)
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                selectHeartRateLayout.visibility = View.VISIBLE
                if (e != null) {
                    val x = e.x.toInt()
                    val y = e.y
                    Log.d("ChartClick", "Clicked X: $x, Y: $y")
                    selectedItemDate.text = labels.get(x)
                    selectedCalorieTv.text = y.toInt().toString()
                }
            }
            override fun onNothingSelected() {
                Log.d("ChartClick", "Nothing selected")
                selectHeartRateLayout.visibility = View.INVISIBLE
            }
        })

        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false

        lineChart.invalidate()
    }

    private fun processWeeklyData(data: WeightResponse, currentDate: String): Pair<List<Entry>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val date = dateFormat.parse(currentDate)
        calendar.time = date!!
        calendar.add(Calendar.DAY_OF_YEAR, -6)

        val waterMap = mutableMapOf<String, Float>()
        val labels = mutableListOf<String>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        repeat(7) {
            val dateStr = dateFormat.format(calendar.time)
            waterMap[dateStr] = 0f
            labels.add(dayFormat.format(calendar.time))
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        data.weightTotals.forEach { intake ->
            val date = dateFormat.parse(intake.date)
            if (date != null) {
                val dayKey = dateFormat.format(date)
                waterMap[dayKey] = intake.weight.toFloat()
            }
        }

        val entries = waterMap.values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        return Pair(entries, labels)
    }

    private fun processMonthlyData(data: WeightResponse, currentDate: String): Pair<List<Entry>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val date = dateFormat.parse(currentDate)
        calendar.time = date!!
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        calendar.set(year, month, 1)

        val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
        val waterMap = mutableMapOf<String, Float>()

        // Initialize water map for all days in the month
        repeat(daysInMonth) {
            val dateStr = dateFormat.format(calendar.time)
            waterMap[dateStr] = 0f
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Populate water intake data
        data.weightTotals.forEach { intake ->
            val date = dateFormat.parse(intake.date)
            if (date != null) {
                val dayKey = dateFormat.format(date)
                waterMap[dayKey] = intake.weight.toFloat()
            }
        }

        // Group data into weeks (1-7, 8-14, 15-21, 22-28, 29-end)
        val weeklyWater = mutableListOf<Float>()
        val labels = mutableListOf<String>()
        var weekIndex = 0
        var weeklySum = 0f
        var dayCount = 0

        waterMap.entries.sortedBy { it.key }.forEachIndexed { index, entry ->
            weeklySum += entry.value
            dayCount++

            when (index + 1) {
                7 -> {
                    weeklyWater.add(weeklySum / dayCount) // Average for the week
                    labels.add("1-7")
                    weeklySum = 0f
                    dayCount = 0
                    weekIndex++
                }
                14 -> {
                    weeklyWater.add(weeklySum / dayCount)
                    labels.add("8-14")
                    weeklySum = 0f
                    dayCount = 0
                    weekIndex++
                }
                21 -> {
                    weeklyWater.add(weeklySum / dayCount)
                    labels.add("15-21")
                    weeklySum = 0f
                    dayCount = 0
                    weekIndex++
                }
                28 -> {
                    weeklyWater.add(weeklySum / dayCount)
                    labels.add("22-28")
                    weeklySum = 0f
                    dayCount = 0
                    weekIndex++
                }
                daysInMonth -> {
                    weeklyWater.add(weeklySum / dayCount)
                    labels.add("29-${daysInMonth}")
                }
            }
        }

        // Create entries for the chart
        val entries = weeklyWater.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        return Pair(entries, labels)
    }

    private fun processSixMonthsData(data: WeightResponse, currentDate: String): Pair<List<Entry>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 24)
        calendar.add(Calendar.MONTH, -5)

        val waterMap = mutableMapOf<Int, Float>()
        val labels = mutableListOf<String>()

        repeat(6) { month ->
            waterMap[month] = 0f
            labels.add(monthFormat.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
        }

        data.weightTotals.forEach { intake ->
            val date = dateFormat.parse(intake.date)
            if (date != null) {
                calendar.time = date
                val monthDiff = ((2025 - 1900) * 12 + Calendar.MARCH) - ((calendar.get(Calendar.YEAR) - 1900) * 12 + calendar.get(Calendar.MONTH))
                val monthIndex = 5 - monthDiff
                if (monthIndex in 0..5) {
                    waterMap[monthIndex] = waterMap[monthIndex]!! + intake.weight.toFloat()
                }
            }
        }

        val entries = waterMap.values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        return Pair(entries, labels)
    }

    private fun setSelectedDate(selectedWeekDate: String) {
        requireActivity().runOnUiThread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = selectedWeekDate
            val date = dateFormat.parse(dateString)
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -6)
            val dateStr = dateFormat.format(calendar.time)
            val dateView: String = convertDate(dateStr) + "-" + convertDate(selectedWeekDate) + "," + year.toString()
            this.selectedDate.text = dateView
            this.selectedDate.gravity = Gravity.CENTER
        }
    }

    private fun setSelectedDateMonth(selectedMonthDate: String, dateViewType: String) {
        activity?.runOnUiThread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = selectedMonthDate
            val date = dateFormat.parse(dateString)
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            if (dateViewType.contentEquals("Month")) {
                val lastDayOfMonth = getDaysInMonth(month + 1, year)
                val lastDateOfMonth = getFirstDateOfMonth(selectedMonthDate, lastDayOfMonth)
                val dateView: String = convertDate(selectedMonthDate) + "-" + convertDate(lastDateOfMonth) + "," + year.toString()
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

    private fun getDaysInMonth(month: Int, year: Int): Int {
        val calendar = Calendar.getInstance()
        calendar.set(year, month - 1, 1)
        return calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    private fun getFirstDateOfMonth(inputDate: String, value: Int): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val parsedDate = LocalDate.parse(inputDate, formatter)
        val firstDayOfMonth = parsedDate.withDayOfMonth(value)
        return firstDayOfMonth.format(formatter)
    }
}

class HydrationViewModelNew : ViewModel() {
    val allDailyWater = mutableListOf<DailyWaterNew>()
    var currentRange = RangeTypeChartsNew.SIX_MONTHS
    var startDate = Date()
    var endDate = Date()

    init {
        generateMockData()
        setInitialRange(RangeTypeChartsNew.SIX_MONTHS)
    }

    private fun generateMockData() {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val start = sdf.parse("2025/01/01") ?: Date()
        val end = sdf.parse("2025/07/01") ?: Date()

        var current = start
        while (current <= end) {
            val randomWater = (1000..3000).random()
            allDailyWater.add(DailyWaterNew(current, randomWater))
            calendar.time = current
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            current = calendar.time
        }
        Log.d("HydrationViewModel", "Generated ${allDailyWater.size} days of data from ${formatDate(start, "d MMM yyyy")} to ${formatDate(end, "d MMM yyyy")}")
    }

    val filteredData: List<DailyWaterNew>
        get() = allDailyWater.filter { it.date >= startDate && it.date <= endDate }

    val monthlyGroups: List<MonthGroupsNew>
        get() = if (currentRange != RangeTypeChartsNew.SIX_MONTHS || filteredData.isEmpty()) emptyList() else {
            val calendar = Calendar.getInstance()
            val grouped = filteredData.groupBy {
                calendar.time = it.date
                Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
            }
            val sorted = grouped.toList().sortedWith(compareBy({ it.first.first }, { it.first.second }))
            var results = mutableListOf<MonthGroupsNew>()
            var previousAvg: Int? = null
            for ((_, days) in sorted) {
                val sortedDays = days.sortedBy { it.date }
                val totalWater = sortedDays.sumOf { it.waterMl }
                val avg = if (sortedDays.isNotEmpty()) totalWater / sortedDays.size else 0
                val diffString = previousAvg?.let {
                    val delta = avg - it
                    val pct = (delta.toDouble() / it * 100).toInt()
                    if (pct >= 0) "+$pct%" else "$pct%"
                } ?: ""
                val isPositive = diffString.startsWith('+')
                previousAvg = avg

                val monthStart = calendar.apply {
                    time = sortedDays.first().date
                    set(Calendar.DAY_OF_MONTH, 1)
                }.time
                val monthEnd = calendar.apply {
                    time = sortedDays.last().date
                    set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                }.time
                val clampedStart = maxOf(monthStart, startDate)
                val clampedEnd = minOf(monthEnd, endDate)
                results.add(MonthGroupsNew(sortedDays, avg, diffString, isPositive, clampedStart, clampedEnd))
            }
            results
        }

    fun setInitialRange(range: RangeTypeChartsNew) {
        currentRange = range
        val calendar = Calendar.getInstance()
        val now = Date()
        when (range) {
            RangeTypeChartsNew.WEEK -> {
                val interval = calendar.getActualMinimum(Calendar.DAY_OF_WEEK)
                calendar.time = now
                calendar.set(Calendar.DAY_OF_WEEK, interval)
                startDate = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, 6)
                endDate = calendar.time
            }
            RangeTypeChartsNew.MONTH -> {
                calendar.time = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate = calendar.time
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                endDate = calendar.time
            }
            RangeTypeChartsNew.SIX_MONTHS -> {
                calendar.time = now
                calendar.add(Calendar.MONTH, -6)
                startDate = calendar.time
                endDate = now
            }
        }
        val minDate = allDailyWater.minByOrNull { it.date }?.date ?: startDate
        val maxDate = allDailyWater.maxByOrNull { it.date }?.date ?: endDate
        startDate = maxOf(startDate, minDate)
        endDate = minOf(endDate, maxDate)
        Log.d("HydrationViewModel", "Set range: $currentRange, startDate: ${formatDate(startDate, "d MMM yyyy")}, endDate: ${formatDate(endDate, "d MMM yyyy")}")
    }

    val yMax: Double
        get() = filteredData.maxOfOrNull { it.waterMl.toDouble() }?.times(1.15) ?: 3000.0

    private fun formatDate(date: Date, format: String): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }
}

data class DailyWaterNew(val date: Date, val waterMl: Int)
enum class RangeTypeChartsNew { WEEK, MONTH, SIX_MONTHS }
data class MonthGroupsNew(
    val days: List<DailyWaterNew>,
    val avgWater: Int,
    val monthDiffString: String,
    val isPositiveChange: Boolean,
    val startDate: Date,
    val endDate: Date
)