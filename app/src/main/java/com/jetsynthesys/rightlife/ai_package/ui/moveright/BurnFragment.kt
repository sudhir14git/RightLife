package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
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

        // Initial chart setup with sample data
        //updateChart(getWeekData(), getWeekLabels())
        // Set default selection to Week
        radioGroup.check(R.id.rbWeek)
        fetchActiveCalories("last_weekly")
       // setupLineChart()
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

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
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
       /* for (i in 0 until 30) {
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
            val dateLabel = (convertMonth(dateString) + "," + year)
            if (i < 7){
                labelsDate.add("1-7 $dateLabel")
            }else if (i < 14){
                labelsDate.add("8-14 $dateLabel")
            }else if (i < 21){
                labelsDate.add("15-21 $dateLabel")
            }else if (i < 28){
                labelsDate.add("22-28 $dateLabel")
            }else{
                labelsDate.add("29-31 $dateLabel")
            }
        }*/
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

    private fun generateWeeklyLabelsFor30Days(startDateStr: String): List<String> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val dayFormat = SimpleDateFormat("d", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

        val startDate = dateFormat.parse(startDateStr)!!
        val calendar = Calendar.getInstance()
        calendar.time = startDate

        val endDate = Calendar.getInstance().apply {
            time = startDate
            add(Calendar.DAY_OF_MONTH, 29)
        }.time

        val result = mutableListOf<String>()

        while (calendar.time <= endDate) {
            val weekStart = calendar.time
            val weekStartIndex = result.size
            calendar.add(Calendar.DAY_OF_MONTH, 6)
            val weekEnd = if (calendar.time.after(endDate)) endDate else calendar.time

            val startDay = dayFormat.format(weekStart)
            val endDay = dayFormat.format(weekEnd)
            val startMonth = monthFormat.format(weekStart)
            val endMonth = monthFormat.format(weekEnd)
            val dateItem = LocalDate.parse(startDateStr)
            val yearItem = dateItem.year

            val label = if (startMonth == endMonth) {
                "$startDay–$endDay $startMonth"+"," + yearItem.toString()
            } else {
                "$startDay $startMonth–$endDay $endMonth"+"," + yearItem.toString()
            }
            val daysInThisWeek = 7.coerceAtMost(30 - result.size)
            repeat(daysInThisWeek) {
                result.add(label)
            }
            calendar.add(Calendar.DAY_OF_MONTH, 1) // move to next week start
        }
        return result
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
//                val lastDayOfMonth = getDaysInMonth(month+1 , year)
//                val lastDateOfMonth = getFirstDateOfMonth(selectedMonthDate, lastDayOfMonth)
                //               val dateView : String = convertDate(selectedMonthDate) + "-" + convertDate(lastDateOfMonth)+","+ year.toString()
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
            heartRateDescription.text = activeCaloriesResponse.description
            averageBurnCalorie.text = activeCaloriesResponse.currentAvgCalories.toInt().toString()
            if (activeCaloriesResponse.progressSign.contentEquals("plus")){
                percentageTv.text = (activeCaloriesResponse.progressPercentage.toInt().toString() + type)
                percentageIc.setImageResource(R.drawable.ic_up)
            }else if (activeCaloriesResponse.progressSign.contentEquals("minus")){
                percentageTv.text = (activeCaloriesResponse.progressPercentage.toInt().toString() + type)
                percentageIc.setImageResource(R.drawable.ic_down)
            }else{
            }
        }
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

    private fun getFirstDateOfMonth(inputDate: String, value : Int): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val parsedDate = LocalDate.parse(inputDate, formatter)
        val firstDayOfMonth = parsedDate.withDayOfMonth(value)
        return firstDayOfMonth.format(formatter)
    }

//    private fun lineChartForSixMonths() {
//        val entries = if (viewModel.filteredData.isNotEmpty()) {
//            viewModel.filteredData.mapIndexed { index, item ->
//                Entry(index.toFloat(), item.steps.toFloat())
//            }
//        } else {
//            listOf(Entry(0f, 0f), Entry(1f, 0f))
//        }
//        Log.d("CalorieBalance", "lineChartForSixMonths: entries size = ${entries.size}")
//
//        val dataSet = LineDataSet(entries, "Steps").apply {
//            color = Color.rgb(255, 102, 128)
//            lineWidth = 1f
//            setDrawCircles(false)
//            setDrawValues(false)
//            mode = LineDataSet.Mode.LINEAR
//            fillAlpha = 20 // Slight fill for iOS-like effect
//            fillColor = Color.rgb(255, 102, 128)
//            setDrawFilled(true)
//        }
//
//        val lineData = LineData(dataSet)
//        lineChart.data = lineData
//
//        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(
//            viewModel.monthlyGroups.map { formatDate(it.startDate, "LLL\nyyyy") }.ifEmpty { listOf("No Data", "No Data") }
//        )
//        lineChart.xAxis.apply {
//            position = XAxis.XAxisPosition.BOTTOM
//            textColor = Color.BLACK
//            textSize = 10f
//            setDrawGridLines(true)
//            setDrawLabels(true)
//            granularity = 1f
//            labelCount = viewModel.monthlyGroups.size.coerceAtLeast(2)
//            setAvoidFirstLastClipping(true)
//        }
//
//        lineChart.axisLeft.apply {
//            axisMaximum = viewModel.yMax.toFloat() * 1.2f
//            axisMinimum = 0f
//            setDrawGridLines(true)
//            textColor = Color.BLACK
//            textSize = 10f
//            setLabelCount(6, true)
//            valueFormatter = object : IndexAxisValueFormatter() {
//                override fun getFormattedValue(value: Float): String {
//                    return if (value == 0f) "0" else "${(value / 1000).toInt()}k"
//                }
//            }
//            setDrawAxisLine(true)
//        }
//        lineChart.axisRight.isEnabled = false
//
//        lineChart.animateX(1000)
//        lineChart.animateY(1000)
//        lineChart.invalidate()
//
//        lineChart.post {
//            val transformer = lineChart.getTransformer(YAxis.AxisDependency.LEFT)
//            val hMargin = 40f
//            val vMargin = 20f
//            if (viewModel.currentRange == RangeTypeCharts.SIX_MONTHS) {
//                val overallStart = viewModel.filteredData.firstOrNull()?.date ?: viewModel.startDate
//                val overallEnd = viewModel.filteredData.lastOrNull()?.date ?: viewModel.endDate
//                val totalInterval = if (overallEnd.time > overallStart.time) overallEnd.time - overallStart.time.toDouble() else 1.0
//
//                Log.d("CalorieBalance", "filteredData size: ${viewModel.filteredData.size}, overallStart: $overallStart, overallEnd: $overallEnd")
//
//                stripsContainer.removeAllViews()
//                viewModel.monthlyGroups.forEach { group ->
//                    val monthEntries = viewModel.filteredData.filter { it.date >= group.startDate && it.date <= group.endDate }
//                    if (monthEntries.isNotEmpty()) {
//                        val startIndex = viewModel.filteredData.indexOfFirst { it.date >= group.startDate }
//                        val endIndex = viewModel.filteredData.indexOfLast { it.date <= group.endDate }
//                        if (startIndex >= 0 && endIndex >= 0) {
//                            val xStartFraction = startIndex.toDouble() / viewModel.filteredData.size
//                            val xEndFraction = endIndex.toDouble() / viewModel.filteredData.size
//                            val yFraction = group.avgSteps.toDouble() / viewModel.yMax
//                            val pixelStart = transformer.getPixelForValues(
//                                (xStartFraction * (lineChart.width - 2 * hMargin)).toFloat() + hMargin,
//                                ((1 - yFraction) * (lineChart.height - 2 * vMargin) + vMargin).toFloat()
//                            )
//                            val pixelEnd = transformer.getPixelForValues(
//                                (xEndFraction * (lineChart.width - 2 * hMargin)).toFloat() + hMargin,
//                                ((1 - yFraction) * (lineChart.height - 2 * vMargin) + vMargin).toFloat()
//                            )
//
//                            // Draw red capsule
//                            val capsuleView = View(requireContext()).apply {
//                                val width = (pixelEnd.x - pixelStart.x).toInt().coerceAtLeast(20)
//                                layoutParams = FrameLayout.LayoutParams(width, 8)
//                                background = GradientDrawable().apply {
//                                    shape = GradientDrawable.RECTANGLE
//                                    cornerRadius = 4f
//                                    setColor(Color.RED)
//                                }
//                                x = pixelStart.x.toFloat()
//                                y = (pixelStart.y - 3).toFloat()
//                            }
//                            stripsContainer.addView(capsuleView)
//
//                            // Average label
//                            val avgText = TextView(requireContext()).apply {
//                                text = "${group.avgSteps / 1000}k"
//                                setTextColor(Color.BLACK)
//                                textSize = 12f
//                                setPadding(6, 4, 6, 4)
//                                layoutParams = FrameLayout.LayoutParams(
//                                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                                    ViewGroup.LayoutParams.WRAP_CONTENT
//                                )
//                                x = ((pixelStart.x + pixelEnd.x) / 2 - 20).toFloat()
//                                y = (pixelStart.y - 20).toFloat()
//                            }
//                            stripsContainer.addView(avgText)
//
//                            // Difference label
//                            val diffText = TextView(requireContext()).apply {
//                                text = group.monthDiffString
//                                setTextColor(if (group.isPositiveChange) Color.GREEN else Color.RED)
//                                textSize = 12f
//                                setPadding(6, 4, 6, 4)
//                                layoutParams = FrameLayout.LayoutParams(
//                                    ViewGroup.LayoutParams.WRAP_CONTENT,
//                                    ViewGroup.LayoutParams.WRAP_CONTENT
//                                )
//                                x = ((pixelStart.x + pixelEnd.x) / 2 - 20).toFloat()
//                                y = (pixelStart.y + 20).toFloat()
//                            }
//                            stripsContainer.addView(diffText)
//                        }
//                    }
//                }
//            }
//        }
//    }

    private fun formatDate(date: Date, format: String): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }

//    private fun setupLineChart() {
//        lineChart.apply {
//            axisLeft.apply {
//                axisMaximum = viewModel.yMax.toFloat() * 1.2f
//                axisMinimum = 0f
//                setDrawGridLines(true)
//                textColor = Color.BLACK
//                textSize = 10f
//                setLabelCount(6, true)
//                valueFormatter = object : IndexAxisValueFormatter() {
//                    override fun getFormattedValue(value: Float): String {
//                        return if (value == 0f) "0" else "${(value / 1000).toInt()}k"
//                    }
//                }
//                setDrawAxisLine(true)
//            }
//            axisRight.isEnabled = false
//            xAxis.apply {
//                position = XAxis.XAxisPosition.BOTTOM
//                textColor = Color.BLACK
//                textSize = 10f
//                setDrawGridLines(true)
//                setDrawLabels(true)
//                granularity = 1f
//                setAvoidFirstLastClipping(true)
//            }
//            description.isEnabled = false
//            setTouchEnabled(true)
//            setDrawGridBackground(false)
//            setDrawBorders(false)
//            setNoDataText("Loading data...")
//        }
//    }

    private fun updateDateRangeLabel() {
        val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
     //   dateRangeLabel.text = "${sdf.format(viewModel.startDate)} - ${sdf.format(viewModel.endDate)}"
    }

//    private fun updateAverageStats() {
//        val avg = viewModel.filteredData.averageSteps().toInt().takeIf { it > 0 } ?: 0
//       // averageText.text = "$avg Steps"
//    }
}

class ActiveBurnViewModel : ViewModel() {
    val allDailySteps = mutableListOf<DailyStep>()
    val goalSteps = 10000
    var currentRange = RangeTypeCharts.SIX_MONTHS
    var startDate = Date()
    var endDate = Date()

    init {
        generateMockData()
        setInitialRange(RangeTypeCharts.SIX_MONTHS)
    }

    private fun generateMockData() {
        val calendar = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy/MM/dd", Locale.getDefault())
        val start = sdf.parse("2025/01/01") ?: Date()
        val end = sdf.parse("2025/07/01") ?: Date()

        var current = start
        while (current <= end) {
            val randomSteps = (2000..15000).random()
            allDailySteps.add(DailyStep(current, randomSteps))
            calendar.time = current
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            current = calendar.time
        }
        Log.d("StepsViewModel", "Generated ${allDailySteps.size} days of data from ${formatDate(start, "d MMM yyyy")} to ${formatDate(end, "d MMM yyyy")}")
    }

    val filteredData: List<DailyStep>
        get() = allDailySteps.filter { it.date >= startDate && it.date <= endDate }

    val monthlyGroups: List<MonthGroups>
        get() = if (currentRange != RangeTypeCharts.SIX_MONTHS || filteredData.isEmpty()) emptyList() else {
            val calendar = Calendar.getInstance()
            val grouped = filteredData.groupBy {
                calendar.time = it.date
                Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
            }
            // Sort by year and month using a custom comparison
            val sorted = grouped.toList().sortedWith(compareBy({ it.first.first }, { it.first.second }))
            var results = mutableListOf<MonthGroups>()
            var previousAvg: Int? = null
            for ((_, days) in sorted) {
                val sortedDays = days.sortedBy { it.date }
                val totalSteps = sortedDays.sumOf { it.steps }
                val avg = if (sortedDays.isNotEmpty()) totalSteps / sortedDays.size else 0
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
                results.add(MonthGroups(sortedDays, avg, diffString, isPositive, clampedStart, clampedEnd))
            }
            results
        }

    fun setInitialRange(range: RangeTypeCharts) {
        currentRange = range
        val calendar = Calendar.getInstance()
        val now = Date()
        when (range) {
            RangeTypeCharts.WEEK -> {
                val interval = calendar.getActualMinimum(Calendar.DAY_OF_WEEK)
                calendar.time = now
                calendar.set(Calendar.DAY_OF_WEEK, interval)
                startDate = calendar.time
                calendar.add(Calendar.DAY_OF_YEAR, 6)
                endDate = calendar.time
            }
            RangeTypeCharts.MONTH -> {
                calendar.time = now
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                startDate = calendar.time
                calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
                endDate = calendar.time
            }
            RangeTypeCharts.SIX_MONTHS -> {
                calendar.time = now
                calendar.add(Calendar.MONTH, -6)
                startDate = calendar.time
                endDate = now
            }
        }
        val minDate = allDailySteps.minByOrNull { it.date }?.date ?: startDate
        val maxDate = allDailySteps.maxByOrNull { it.date }?.date ?: endDate
        startDate = maxOf(startDate, minDate)
        endDate = minOf(endDate, maxDate)
        Log.d("StepsViewModel", "Set range: $currentRange, startDate: ${formatDate(startDate, "d MMM yyyy")}, endDate: ${formatDate(endDate, "d MMM yyyy")}")
    }

    fun goLeft() {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        when (currentRange) {
            RangeTypeCharts.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            RangeTypeCharts.MONTH -> calendar.add(Calendar.MONTH, -1)
            RangeTypeCharts.SIX_MONTHS -> calendar.add(Calendar.MONTH, -6)
        }
        startDate = calendar.time
        calendar.time = endDate
        when (currentRange) {
            RangeTypeCharts.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, -7)
            RangeTypeCharts.MONTH -> calendar.add(Calendar.MONTH, -1)
            RangeTypeCharts.SIX_MONTHS -> calendar.add(Calendar.MONTH, -6)
        }
        endDate = calendar.time
        val minDate = allDailySteps.minByOrNull { it.date }?.date ?: startDate
        val maxDate = allDailySteps.maxByOrNull { it.date }?.date ?: endDate
        startDate = maxOf(startDate, minDate)
        endDate = minOf(endDate, maxDate)
        Log.d("StepsViewModel", "Go left: startDate: ${formatDate(startDate, "d MMM yyyy")}, endDate: ${formatDate(endDate, "d MMM yyyy")}")
    }

    fun goRight() {
        val calendar = Calendar.getInstance()
        calendar.time = startDate
        when (currentRange) {
            RangeTypeCharts.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, 7)
            RangeTypeCharts.MONTH -> calendar.add(Calendar.MONTH, 1)
            RangeTypeCharts.SIX_MONTHS -> calendar.add(Calendar.MONTH, 6)
        }
        startDate = calendar.time
        calendar.time = endDate
        when (currentRange) {
            RangeTypeCharts.WEEK -> calendar.add(Calendar.DAY_OF_YEAR, 7)
            RangeTypeCharts.MONTH -> calendar.add(Calendar.MONTH, 1)
            RangeTypeCharts.SIX_MONTHS -> calendar.add(Calendar.MONTH, 6)
        }
        endDate = calendar.time
        val minDate = allDailySteps.minByOrNull { it.date }?.date ?: startDate
        val maxDate = allDailySteps.maxByOrNull { it.date }?.date ?: endDate
        startDate = maxOf(startDate, minDate)
        endDate = minOf(endDate, maxDate)
        Log.d("StepsViewModel", "Go right: startDate: ${formatDate(startDate, "d MMM yyyy")}, endDate: ${formatDate(endDate, "d MMM yyyy")}")
    }

    val yMax: Double
        get() = filteredData.maxOfOrNull { it.steps.toDouble() }?.times(1.15) ?: 15000.0

    private fun formatDate(date: Date, format: String): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }
}

data class DailyStep(val date: Date, val steps: Int)
enum class RangeTypeCharts { WEEK, MONTH, SIX_MONTHS }
data class MonthGroups(
    val days: List<DailyStep>,
    val avgSteps: Int,
    val monthDiffString: String,
    val isPositiveChange: Boolean,
    val startDate: Date,
    val endDate: Date
)

private fun List<DailyStep>.averageSteps(): Number {
    return if (isEmpty()) 0.0 else sumOf { it.steps } / size
}

class CurvedBarChartRenderer(
    chart: BarChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarChartRenderer(chart, animator, viewPortHandler) {

    init {
        // Ensure buffers are initialized to avoid null pointer
        initBuffers()
    }

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val buffer = mBarBuffers[index]
        if (buffer.buffer == null) return  // Safety check

        mRenderPaint.color = dataSet.color

        for (j in 0 until buffer.buffer.size step 4) {
            val left = buffer.buffer[j]
            val top = buffer.buffer[j + 1]
            val right = buffer.buffer[j + 2]
            val bottom = buffer.buffer[j + 3]

            val radius = 30f

            val path = Path().apply {
                moveTo(left, bottom)
                lineTo(left, top + radius)
                quadTo(left, top, left + radius, top)
                lineTo(right - radius, top)
                quadTo(right, top, right, top + radius)
                lineTo(right, bottom)
                close()
            }

            c.drawPath(path, mRenderPaint)
        }
    }
}
