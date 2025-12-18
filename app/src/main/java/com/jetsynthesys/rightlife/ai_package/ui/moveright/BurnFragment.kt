package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.TextPaint
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebViewFragment
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
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.ActiveCaloriesResponse
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.RestorativeSleepFragment
import com.jetsynthesys.rightlife.databinding.FragmentBurnBinding
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
import java.util.*
import kotlin.math.ceil
import kotlin.math.max

class BurnFragment : BaseFragment<FragmentBurnBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBurnBinding
        get() = FragmentBurnBinding::inflate

    private lateinit var barChart: BarChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var backwardImage : ImageView
    private lateinit var forwardImage : ImageView
    private var selectedWeekDate : String = ""
    private var selectedMonthDate : String = ""
    private var selectedHalfYearlyDate : String = ""
    private lateinit var selectedDate : TextView
    private lateinit var selectedItemDate : TextView
    private lateinit var selectHeartRateLayout : CardView
    private lateinit var selectedCalorieTv : TextView
    private lateinit var averageBurnCalorie : TextView
    private lateinit var averageHeading : TextView
    private lateinit var percentageTv : TextView
    private lateinit var percentageIc : ImageView
    private lateinit var heartRateDescriptionHeading : TextView
    private lateinit var heartRateDescription : TextView
    private lateinit var burn_back_button_image : ImageView
    private lateinit var layoutLineChart: FrameLayout
    private lateinit var stripsContainer: FrameLayout
    private lateinit var lineChart: LineChart
    private var loadingOverlay : FrameLayout? = null
   // private val viewModel: ActiveBurnViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.gradient_color_background_workout)

        barChart = view.findViewById(R.id.heartRateChart)
        radioGroup = view.findViewById(R.id.tabGroup)
        backwardImage = view.findViewById(R.id.backwardImage)
        forwardImage = view.findViewById(R.id.forwardImage)
        selectedDate = view.findViewById(R.id.selectedDate)
        selectedItemDate = view.findViewById(R.id.selectedItemDate)
        selectHeartRateLayout = view.findViewById(R.id.selectHeartRateLayout)
        selectedCalorieTv = view.findViewById(R.id.selectedCalorieTv)
        percentageTv = view.findViewById(R.id.percentageTv)
        averageBurnCalorie = view.findViewById(R.id.averageBurnCalorie)
        averageHeading = view.findViewById(R.id.averageHeading)
        percentageIc = view.findViewById(R.id.percentageIc)
        layoutLineChart = view.findViewById(R.id.lyt_line_chart)
        stripsContainer = view.findViewById(R.id.stripsContainer)
        lineChart = view.findViewById(R.id.heartLineChart)
        burn_back_button_image = view.findViewById(R.id.burn_back_button_image)
        heartRateDescriptionHeading = view.findViewById(R.id.heartRateDescriptionHeading)
        heartRateDescription = view.findViewById(R.id.heartRateDescription)
        burn_back_button_image.setOnClickListener {
            val fragment = HomeBottomTabFragment()
            val args = Bundle().apply {
                putString("ModuleName", "MoveRight")
            }
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                addToBackStack(null)
                commit()
            }
        }

        radioGroup.check(R.id.rbWeek)
        fetchActiveCalories("last_weekly")
        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            for (i in 0 until group.childCount) {
                val radioButton = group.getChildAt(i) as RadioButton
                if (radioButton.id == checkedId) {
                    radioButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
                } else {
                    radioButton.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.black))
                }
            }
            when (checkedId) {
                R.id.rbWeek -> {
                    selectHeartRateLayout.visibility = View.INVISIBLE
                    fetchActiveCalories("last_weekly")}
                R.id.rbMonth -> {
                    selectHeartRateLayout.visibility = View.INVISIBLE
                    fetchActiveCalories("last_monthly")}
                R.id.rbSixMonths ->{
                    selectHeartRateLayout.visibility = View.INVISIBLE
                    fetchActiveCalories("last_six_months")}
            }
        }

        backwardImage.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            var selectedTab : String = "Week"
            if (selectedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(selectedId)
                selectedTab = selectedRadioButton.text.toString()
            }

            if (selectedTab.contentEquals("Week")){
                val dateFormat = SimpleDateFormat("yyyy-MM-dd",  Locale.getDefault())
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
                fetchActiveCalories("last_weekly")
            }else if (selectedTab.contentEquals("Month")){
                val dateFormat = SimpleDateFormat("yyyy-MM-dd",  Locale.getDefault())
                val calendar = Calendar.getInstance()
                val dateString = selectedMonthDate
                val date = dateFormat.parse(dateString)
                calendar.time = date!!
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                calendar.set(year, month, day)
                calendar.add(Calendar.DAY_OF_YEAR, -30)
                val dateStr = dateFormat.format(calendar.time)
                // val firstDateOfMonth = getFirstDateOfMonth(dateStr, 1)
                selectedMonthDate = dateStr
                fetchActiveCalories("last_monthly")
            }else{
                selectedHalfYearlyDate = ""
                fetchActiveCalories("last_six_months")
            }
        }

        forwardImage.setOnClickListener {
            val selectedId = radioGroup.checkedRadioButtonId
            var selectedTab : String = "Week"
            if (selectedId != -1) {
                val selectedRadioButton = view.findViewById<RadioButton>(selectedId)
                selectedTab = selectedRadioButton.text.toString()
            }
            val currentDateTime = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val currentDate : String =  currentDateTime.format(formatter)

            if (selectedTab.contentEquals("Week")){
                if (!selectedWeekDate.contentEquals(currentDate)){
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd",  Locale.getDefault())
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
                    fetchActiveCalories("last_weekly")
                }else{
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            }else if (selectedTab.contentEquals("Month")){
                if (!selectedMonthDate.contentEquals(currentDate)){
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd",  Locale.getDefault())
                    val calendar = Calendar.getInstance()
                    val dateString = selectedMonthDate
                    val date = dateFormat.parse(dateString)
                    calendar.time = date!!
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    calendar.set(year, month, day)
                    calendar.add(Calendar.DAY_OF_YEAR, +30)
                    val dateStr = dateFormat.format(calendar.time)
                    //  val firstDateOfMonth = getFirstDateOfMonth(dateStr, 1)
                    selectedMonthDate = dateStr
                    fetchActiveCalories("last_monthly")
                }else{
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            }else{
                if (!selectedHalfYearlyDate.contentEquals(currentDate)){
                    selectedHalfYearlyDate = ""
                    fetchActiveCalories("last_six_months")
                }else{
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = HomeBottomTabFragment()
            val args = Bundle().apply {
                putString("ModuleName", "MoveRight")
            }
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                addToBackStack(null)
                commit()
            }
        }
    }



    private fun updateChart(entries: List<BarEntry>, labels: List<String>, labelsDate: List<String>) {
        val dataSet = BarDataSet(entries, "")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.moveright)
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        dataSet.valueTextSize = 12f
        dataSet.setDrawValues(entries.size <= 7)
        dataSet.barShadowColor = Color.TRANSPARENT
        dataSet.highLightColor = ContextCompat.getColor(requireContext(), R.color.light_orange)

        val barData = BarData(dataSet)
        barData.barWidth = 0.4f
        barChart.data = barData
        barChart.setFitBars(true)

        // Multiline X-axis labels
        val combinedLabels: List<String> = if (entries.size == 30) {
            labels
        } else {
            labels.take(entries.size).zip(labelsDate.take(entries.size)) { label, date ->
                val cleanedDate = date.substringBefore(",")
                "$label\n$cleanedDate"
            }
        }

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(combinedLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 10f
        xAxis.granularity = 1f
        xAxis.labelCount = entries.size
        xAxis.setDrawLabels(true)
        xAxis.labelRotationAngle = 0f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        xAxis.yOffset = 15f

        if (entries.size == 30) {
            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = 29.5f
            xAxis.setCenterAxisLabels(false)
        } else {
            xAxis.axisMinimum = -0.5f
            xAxis.axisMaximum = entries.size - 0.5f
            xAxis.setCenterAxisLabels(false)
        }

        // Custom XAxisRenderer for multiline labels
        val customRenderer = RestorativeSleepFragment.MultilineXAxisRenderer(
            barChart.viewPortHandler,
            barChart.xAxis,
            barChart.getTransformer(YAxis.AxisDependency.LEFT)
        )
        (barChart as BarLineChartBase<*>).setXAxisRenderer(customRenderer)

        // Y-axis customization
        val leftYAxis: YAxis = barChart.axisLeft
        leftYAxis.textSize = 12f
        leftYAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        leftYAxis.setDrawGridLines(true)
        leftYAxis.axisMinimum = 0f
        leftYAxis.axisMaximum = max(7000f, ceil((entries.maxByOrNull { it.y }?.y?.plus(100f) ?: 1000f) / 1000f) * 1000f)
        leftYAxis.granularity = 1000f
        leftYAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return if (value == 0f) "0" else "${(value / 1000).toInt()}k"
            }
        }

        barChart.axisRight.isEnabled = false

        // YE DO LINES PURI TARAH SE HATAYI HAIN (No box, no "Calories Burned")
        barChart.description.isEnabled = false   // ← Description completely off
        // barChart.description = description     ← Yeh line bhi gayab

        barChart.setScaleEnabled(false)
        barChart.isDoubleTapToZoomEnabled = false
        barChart.isHighlightPerTapEnabled = true
        barChart.isHighlightPerDragEnabled = false

        // Legend off
        val legend = barChart.legend
        legend.isEnabled = false

        // Extra offsets
        barChart.setExtraOffsets(0f, 0f, 0f, 25f)

        // Chart click listener (same as before)
        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                selectHeartRateLayout.visibility = View.VISIBLE
                if (e != null) {
                    val x = e.x.toInt()
                    val y = e.y
                    selectedItemDate.text = labelsDate.getOrNull(x) ?: ""
                    selectedCalorieTv.text = y.toInt().toString()
                }
            }

            override fun onNothingSelected() {
                selectHeartRateLayout.visibility = View.INVISIBLE
                      }
        })

        barChart.animateY(1000)
        barChart.invalidate()
    }


    /** Sample Data for Week */
    private fun getWeekData(): List<BarEntry> {
        return listOf(
            BarEntry(0f, 200f), BarEntry(1f, 350f), BarEntry(2f, 270f),
            BarEntry(3f, 400f), BarEntry(4f, 320f), BarEntry(5f, 500f), BarEntry(6f, 450f)
        )
    }

    private fun getWeekLabels(): List<String> {
        return listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    private fun getWeekLabelsDate(): List<String> {
        return listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    /** Fetch and update chart with API data */
    private fun fetchActiveCalories(period: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        showLoader(requireView())
                    }
                }
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                var selectedDate : String

                if (period.contentEquals("last_weekly")){
                    if (selectedWeekDate.contentEquals("")){
                        selectedDate = currentDateTime.format(formatter)
                        selectedWeekDate = selectedDate
                    }else{
                        selectedDate = selectedWeekDate
                    }
                    setSelectedDate(selectedWeekDate)
                }else if (period.contentEquals("last_monthly")){
                    if (selectedMonthDate.contentEquals("")){
                        selectedDate = currentDateTime.format(formatter)
//                        val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
//                        selectedDate = firstDateOfMonth
                        selectedMonthDate = selectedDate
                    }else{
                       // val firstDateOfMonth = getFirstDateOfMonth(selectedMonthDate, 1)
                        selectedDate = selectedMonthDate
                    }
                    setSelectedDateMonth(selectedMonthDate, "Month")
                }else{
                    if (selectedHalfYearlyDate.contentEquals("")){
                        selectedDate = currentDateTime.format(formatter)
//                        val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
//                        selectedDate = firstDateOfMonth
                        selectedHalfYearlyDate = selectedDate
                    }else{
                       // val firstDateOfMonth = getFirstDateOfMonth(selectedMonthDate, 1)
                        selectedDate = selectedHalfYearlyDate
                    }
                    setSelectedDateMonth(selectedHalfYearlyDate, "Year")
                }
                val response = ApiClient.apiServiceFastApi.getActiveCalories(
                    userId = userId, period = period, date = selectedDate)
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val activeCaloriesResponse = response.body()
                    if (activeCaloriesResponse?.statusCode == 200){
                        activeCaloriesResponse.let { data ->
                            val (entries, labels, labelsDate) = when (period) {
                                "last_weekly" -> processWeeklyData(data, selectedDate)
                                "last_monthly" -> processMonthlyData(data, selectedDate)
                                "last_six_months" -> processSixMonthsData(data, selectedDate )
                                else -> Triple(getWeekData(), getWeekLabels(), getWeekLabelsDate()) // Fallback
                            }
                            val totalCalories = data.activeCaloriesTotals.sumOf { it.caloriesBurned ?: 0.0 }
                            withContext(Dispatchers.Main) {
                                if (data.activeCaloriesTotals.size > 31){
                                    barChart.visibility = View.GONE
                                    layoutLineChart.visibility = View.VISIBLE
                                    //lineChartForSixMonths()
                                }else{
                                    barChart.visibility = View.VISIBLE
                                    layoutLineChart.visibility = View.GONE
                                    updateChart(entries, labels, labelsDate)
                                }
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Error: ${response.code()} - ${response.message()}", Toast.LENGTH_SHORT).show()
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
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                }
            }
        }
    }

    /** Process API data for last_weekly (7 days) */
    private fun processWeeklyData(activeCaloriesResponse: ActiveCaloriesResponse, currentDate: String
    ): Triple<List<BarEntry>, List<String> , List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd",  Locale.getDefault())
        val calendar = Calendar.getInstance()
      //  val indexLastValue = activeCaloriesTotals.size - 1
        val dateString = currentDate
        val date = dateFormat.parse(dateString)
        calendar.time = date!!
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(year, month, day)
        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val calorieMap = mutableMapOf<String, Float>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        // Initialize 7 days with 0 calories
        repeat(7) {
            val dateStr = dateFormat.format(calendar.time)
            calorieMap[dateStr] = 0f
            val dateLabel = (convertDate(dateStr) + "," + year)
             val dayString = (dayFormat.format(calendar.time))
            labels.add(dayString)
            labelsDate.add(dateLabel)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        // Aggregate calories by day
        if (activeCaloriesResponse.activeCaloriesTotals.isNotEmpty()){
            activeCaloriesResponse.activeCaloriesTotals.forEach { calorie ->
                val startDate = dateFormat.parse(calorie.date)?.let { Date(it.time) }
                if (startDate != null) {
                    val dayKey = dateFormat.format(startDate)
                    calorieMap[dayKey] = calorieMap[dayKey]!! + (calorie.caloriesBurned.toFloat() ?: 0f)
                }
            }
        }
        setLastAverageValue(activeCaloriesResponse,  "% Past Week")
        val entries = calorieMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        return Triple(entries, labels, labelsDate)
    }

    /** Process API data for last_monthly (5 weeks) */
    private fun processMonthlyData(activeCaloriesResponse: ActiveCaloriesResponse, currentDate: String
    ): Triple<List<BarEntry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd",  Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateString = currentDate
        val date = dateFormat.parse(dateString)
        calendar.time = date!!
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(year, month, day)
        calendar.add(Calendar.DAY_OF_YEAR, -29)
        val calorieMap = mutableMapOf<String, Float>()
        val dateList = mutableListOf<String>()
        val weeklyLabels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()

        val days = getDaysInMonth(month+1, year)
        repeat(30) {
            val dateStr = dateFormat.format(calendar.time)
            calorieMap[dateStr] = 0f
            dateList.add(dateStr)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        val labelsWithEmpty = generateLabeled30DayListWithEmpty(dateList[0])
        val labels = formatDateList(dateList)
        weeklyLabels.addAll(labelsWithEmpty)
        labelsDate.addAll(labels)
        // Aggregate calories by week
        if ( activeCaloriesResponse.activeCaloriesTotals.isNotEmpty()){
            activeCaloriesResponse.activeCaloriesTotals.forEach { calorie ->
                val startDate = dateFormat.parse(calorie.date)?.let { Date(it.time) }
                if (startDate != null) {
                    val dayKey = dateFormat.format(startDate)
                    calorieMap[dayKey] = calorieMap[dayKey]!! + (calorie.caloriesBurned.toFloat() ?: 0f)
                }
            }
        }
        setLastAverageValue(activeCaloriesResponse, "% Past Month")
        val entries = calorieMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        return Triple(entries, weeklyLabels, labelsDate)
    }

    private fun formatDateList(dates: List<String>): List<String> {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM, yyyy", Locale.getDefault())
        return dates.mapNotNull { dateStr ->
            try {
                val parsedDate = inputFormat.parse(dateStr)
                parsedDate?.let { outputFormat.format(it) }
            } catch (e: Exception) {
                null
            }
        }
    }


    private fun generateLabeled30DayListWithEmpty(startDateStr: String): List<String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        val startDate = dateFormat.parse(startDateStr)!!
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val endDate = Calendar.getInstance().apply {
            time = startDate
            add(Calendar.DAY_OF_MONTH, 29) // total 30 days
        }.time

        val fullList = MutableList(30) { "" } // default 30 items with empty strings
        var labelIndex = 0
        var startIndex = 0

        while (calendar.time <= endDate && startIndex < 30) {
            val weekStart = calendar.time
            calendar.add(Calendar.DAY_OF_MONTH, 6)
            val weekEnd = if (calendar.time.after(endDate)) endDate else calendar.time
            val startDay = dayFormat.format(weekStart)
            val endDay = dayFormat.format(weekEnd)
            val startMonth = monthFormat.format(weekStart)
            val endMonth = monthFormat.format(weekEnd)
            val newLine = "\n"
            val label = if (startMonth == endMonth) {
                "$startDay–$endDay$newLine$startMonth"
            } else {
                "$startDay$startMonth–$endDay$newLine$endMonth"
            }
            fullList[startIndex] = label // set label at start of week
            // Move to next start index
            startIndex += 7
            calendar.add(Calendar.DAY_OF_MONTH, 1) // move past last week end
        }
        return fullList
    }


    /** Process API data for last_six_months (6 months) */
    private fun processSixMonthsData(activeCaloriesResponse: ActiveCaloriesResponse, currentDate: String):
            Triple<List<BarEntry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 24)
        calendar.add(Calendar.MONTH, -5) // Start 6 months back

        val calorieMap = mutableMapOf<Int, Float>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()

        // Initialize 6 months
        repeat(6) { month ->
            calorieMap[month] = 0f
            labels.add(monthFormat.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
        }

        if ( activeCaloriesResponse.activeCaloriesTotals.isNotEmpty()){
            // Aggregate calories by month
            activeCaloriesResponse.activeCaloriesTotals.forEach { calorie ->
                val startDate = dateFormat.parse(calorie.date)?.let { Date(it.time) }
                if (startDate != null) {
                    calendar.time = startDate
                    val monthDiff = ((2025 - 1900) * 12 + Calendar.MARCH) - ((calendar.get(Calendar.YEAR) - 1900) * 12 + calendar.get(Calendar.MONTH))
                    val monthIndex = 5 - monthDiff // Reverse to align with labels (0 = earliest month)
                    if (monthIndex in 0..5) {
                        calorieMap[monthIndex] = calorieMap[monthIndex]!! + (calorie.caloriesBurned.toFloat() ?: 0f)
                    }
                }
            }
        }
        setLastAverageValue(activeCaloriesResponse, "% Past 6 Month")
        val entries = calorieMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        return Triple(entries, labels, labelsDate)
    }

    private fun setSelectedDate(selectedWeekDate: String) {
        requireActivity().runOnUiThread(Runnable {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd",  Locale.getDefault())
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
            val dateView : String = convertDate(dateStr.toString()) + "-" + convertDate(selectedWeekDate)+","+ year.toString()
            selectedDate.text = dateView
            selectedDate.gravity = Gravity.CENTER
        })

    }

    private fun setSelectedDateMonth(selectedMonthDate: String, dateViewType: String) {
        activity?.runOnUiThread {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd",  Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = selectedMonthDate
            val date = dateFormat.parse(dateString)
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -29)
            val dateStr = dateFormat.format(calendar.time)
            if (dateViewType.contentEquals("Month")){
                val dateView : String = convertDate(dateStr.toString()) + "-" + convertDate(selectedMonthDate)+","+ year.toString()
                selectedDate.text = dateView
                selectedDate.gravity = Gravity.CENTER
            }else{
                selectedDate.text = year.toString()
                selectedDate.gravity = Gravity.CENTER
            }
        }
    }

    private fun setLastAverageValue(activeCaloriesResponse: ActiveCaloriesResponse, type: String) {
        activity?.runOnUiThread {
            heartRateDescriptionHeading.text = activeCaloriesResponse.heading
            val text = activeCaloriesResponse.description ?: ""

            // Markdown style [text](url) detect karo
            val markdownLinkPattern = "\\[([^\\]]+)\\]\\(([^\\)]+)\\)".toRegex()
            var processedText = text
            val spannableString = android.text.SpannableStringBuilder(text)

            val matches = markdownLinkPattern.findAll(text).toList()
            var offset = 0

            for (match in matches) {
                val linkText = match.groupValues[1]  // [ye text]
                val linkUrl = match.groupValues[2]   // (ye url)
                val matchStart = match.range.first - offset
                val matchEnd = match.range.last + 1 - offset

                // Replace markdown syntax with just the link text
                spannableString.replace(matchStart, matchEnd, linkText)

                val clickableSpan = object : ClickableSpan() {
                    override fun onClick(widget: View) {
                        // WebView Fragment open karo
                        openWebViewFragment(linkUrl)
                    }

                    override fun updateDrawState(ds: TextPaint) {
                        super.updateDrawState(ds)
                        ds.isUnderlineText = true
                        ds.color = ContextCompat.getColor(requireContext(), R.color.primary_color)
                    }
                }

                val newLinkEnd = matchStart + linkText.length
                spannableString.setSpan(
                    clickableSpan,
                    matchStart,
                    newLinkEnd,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                offset += (matchEnd - matchStart) - linkText.length
            }

            heartRateDescription.text = spannableString
            heartRateDescription.movementMethod = LinkMovementMethod.getInstance()

            averageBurnCalorie.text = activeCaloriesResponse.currentAvgCalories.toInt().toString()
            if (activeCaloriesResponse.progressSign.contentEquals("plus")) {
                percentageTv.text = (activeCaloriesResponse.progressPercentage.toInt().toString() + type)
                percentageIc.setImageResource(R.drawable.ic_up)
            } else if (activeCaloriesResponse.progressSign.contentEquals("minus")) {
                percentageTv.text = (activeCaloriesResponse.progressPercentage.toInt().toString() + type)
                percentageIc.setImageResource(R.drawable.ic_down)
            }
        }
    }

    // Fragment open karne ka function add karo
    private fun openWebViewFragment(url: String) {
        val fragment = com.jetsynthesys.rightlife.ai_package.ui.moveright.WebViewFragment.newInstance(url)

        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.flFragment, fragment)
            .addToBackStack(null)
            .commit()
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
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

}


data class DailyStep(val date: Date, val steps: Int)


private fun List<DailyStep>.averageSteps(): Number {
    return if (isEmpty()) 0.0 else sumOf { it.steps } / size
}


