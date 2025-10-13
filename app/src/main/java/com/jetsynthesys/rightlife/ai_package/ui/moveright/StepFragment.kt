package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.content.res.Resources
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
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.AxisBase
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.LimitLine
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
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.response.StepTrackerData
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.moveright.customProgressBar.BasicProgressBar
import com.jetsynthesys.rightlife.ai_package.ui.moveright.customProgressBar.SimpleProgressBar
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.RestorativeSleepFragment
import com.jetsynthesys.rightlife.ai_package.ui.steps.SetYourStepGoalFragment
import com.jetsynthesys.rightlife.databinding.FragmentStepBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.lang.Math.max
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.min

class StepFragment : BaseFragment<FragmentStepBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentStepBinding
        get() = FragmentStepBinding::inflate

    private lateinit var barChart: BarChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var backwardImage: ImageView
    private lateinit var forwardImage: ImageView
    private var selectedWeekDate: String = ""
    private var selectedMonthDate: String = ""
    private var selectedHalfYearlyDate: String = ""
    private lateinit var selectedDate: TextView
    private lateinit var selectedItemDate: TextView
    private lateinit var total_steps_count: TextView
    private lateinit var heart_rate_description_heading: TextView
    private lateinit var step_discreption: TextView
    private lateinit var selectHeartRateLayout: CardView
    private lateinit var layout_btn_log_meal: LinearLayoutCompat
    private lateinit var selectedCalorieTv: TextView
    private lateinit var averageBurnCalorie: TextView
    private lateinit var averageHeading: TextView
    private lateinit var percentageTv: TextView
    private lateinit var percentageIc: ImageView
    private lateinit var back_button_calorie_balance: ImageView
    private lateinit var perDayStepAverage : TextView
    private lateinit var valuePreviousWeek : TextView
    private lateinit var layoutLineChart: FrameLayout
    private lateinit var stripsContainer: FrameLayout
    private lateinit var lineChart: LineChart
    private var loadingOverlay : FrameLayout? = null
    private lateinit var customProgressPreviousWeek : BasicProgressBar
    private lateinit var customProgressBarFatBurn : SimpleProgressBar
    private lateinit var dottedLine: View
    private var currentGoal = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Set background
        view.setBackgroundResource(R.drawable.gradient_color_background_workout)

        // Initialize views
        barChart = view.findViewById(R.id.heartRateChart)
        radioGroup = view.findViewById(R.id.tabGroup)
        backwardImage = view.findViewById(R.id.backwardImage)
        forwardImage = view.findViewById(R.id.forwardImage)
        selectedDate = view.findViewById(R.id.selectedDate)
        selectedItemDate = view.findViewById(R.id.selectedItemDate)
        selectHeartRateLayout = view.findViewById(R.id.selectHeartRateLayout)
        selectedCalorieTv = view.findViewById(R.id.selectedCalorieTv)
        percentageTv = view.findViewById(R.id.percentageTv)
        heart_rate_description_heading = view.findViewById(R.id.heart_rate_description_heading)
        step_discreption = view.findViewById(R.id.step_discreption)
        layout_btn_log_meal = view.findViewById(R.id.layout_btn_log_meal)
        averageBurnCalorie = view.findViewById(R.id.averageBurnCalorie)
        averageHeading = view.findViewById(R.id.averageHeading)
        percentageIc = view.findViewById(R.id.percentageIc)
        layoutLineChart = view.findViewById(R.id.lyt_line_chart)
        stripsContainer = view.findViewById(R.id.stripsContainer)
        lineChart = view.findViewById(R.id.heartLineChart)
        total_steps_count = view.findViewById(R.id.total_steps_count)
        perDayStepAverage = view.findViewById(R.id.total_steps_progress_layout)
        valuePreviousWeek = view.findViewById(R.id.value_previous_week)
        customProgressPreviousWeek = view.findViewById(R.id.customProgressPreviousWeek)
        customProgressBarFatBurn = view.findViewById(R.id.customProgressBarFatBurn)
        dottedLine = view.findViewById(R.id.dottedLineView1)

        layout_btn_log_meal.setOnClickListener {
            val args = Bundle().apply {
                // Add your arguments here
                putInt("currentGoal", currentGoal) // Example: String argument
                    // Example: Int argument
                // Add more key-value pairs as needed
            }
            navigateToFragment(SetYourStepGoalFragment(), "HomeBottomTabFragment", args)
        }
        back_button_calorie_balance = view.findViewById(R.id.step_back)
        back_button_calorie_balance.setOnClickListener {
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
            //navigateToFragment(HomeBottomTabFragment(), "HomeBottomTabFragment")
        }

        // Update heading to reflect steps
        averageHeading.text = "Average"

        // Set default selection to Week
        radioGroup.check(R.id.rbWeek)
        fetchStepDetails("last_weekly")
        setupLineChart()

        // Handle Radio Button Selection
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
                R.id.rbWeek -> fetchStepDetails("last_weekly")
                R.id.rbMonth -> fetchStepDetails("last_monthly")
                R.id.rbSixMonths -> fetchStepDetails("last_six_months")
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
                fetchStepDetails("last_weekly")
            } else if (selectedTab.contentEquals("Month")) {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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
                fetchStepDetails("last_monthly")
            } else {
//                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                val calendar = Calendar.getInstance()
//                val dateString = selectedHalfYearlyDate
//                val date = dateFormat.parse(dateString)
//                calendar.time = date!!
//                val year = calendar.get(Calendar.YEAR)
//                val month = calendar.get(Calendar.MONTH)
//                val day = calendar.get(Calendar.DAY_OF_MONTH)
//                calendar.set(year, month - 6, day)
//                val dateStr = dateFormat.format(calendar.time)
                selectedHalfYearlyDate = ""
                fetchStepDetails("last_six_months")
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
                    fetchStepDetails("last_weekly")
                } else {
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
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
                    calendar.set(year, month, day)
                    calendar.add(Calendar.DAY_OF_YEAR, +30)
                    val dateStr = dateFormat.format(calendar.time)
                    //  val firstDateOfMonth = getFirstDateOfMonth(dateStr, 1)
                    selectedMonthDate = dateStr
                    fetchStepDetails("last_monthly")
                } else {
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            } else {
                if (!selectedHalfYearlyDate.contentEquals(currentDate)) {
//                    val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
//                    val calendar = Calendar.getInstance()
//                    val dateString = selectedHalfYearlyDate
//                    val date = dateFormat.parse(dateString)
//                    calendar.time = date!!
//                    val year = calendar.get(Calendar.YEAR)
//                    val month = calendar.get(Calendar.MONTH)
//                    val day = calendar.get(Calendar.DAY_OF_MONTH)
//                    calendar.set(year, month + 6, day)
//                    val dateStr = dateFormat.format(calendar.time)
                    selectedHalfYearlyDate = ""
                    fetchStepDetails("last_six_months")
                } else {
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

    private fun navigateToFragment(fragment: Fragment, tag: String, args: Bundle? = null) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            // Set arguments if provided
            args?.let { fragment.arguments = it }
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    private fun updateChart(entries: List<BarEntry>, labels: List<String>, labelsDate: List<String>, stepData: StepTrackerData) {
        Log.d("StepFragment", "Entries size: ${entries.size}, Labels size: ${labels.size}, LabelsDate size: ${labelsDate.size}")
        Log.d("StepFragment", "Entries: $entries")
        Log.d("StepFragment", "Labels: $labels")
        Log.d("StepFragment", "LabelsDate: $labelsDate")

        val dataSet = BarDataSet(entries, "")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.moveright)
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        dataSet.valueTextSize = 12f
        dataSet.setDrawValues(entries.size <= 7)
        dataSet.barShadowColor = Color.TRANSPARENT
        dataSet.highLightColor = ContextCompat.getColor(requireContext(), R.color.light_orange)
        val typeface = ResourcesCompat.getFont(requireContext(), R.font.dmsans_bold)
        dataSet.valueTypeface = typeface

        val barData = BarData(dataSet)
        barData.barWidth = 0.4f
        barChart.data = barData
        barChart.setFitBars(true)
        barChart.setScaleEnabled(false)
        barChart.isDoubleTapToZoomEnabled = false
        barChart.isHighlightPerTapEnabled = true
        barChart.isHighlightPerDragEnabled = false

        val leftYAxis: YAxis = barChart.axisLeft
        leftYAxis.textSize = 12f
        leftYAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        leftYAxis.setDrawGridLines(true)

        val minValue = minOf(
            entries.minOfOrNull { it.y } ?: 0f,
            stepData.totalStepsCount.toFloat(),
            stepData.totalStepsAvg.toFloat()
        )

        val maxValue = maxOf(
            entries.maxOfOrNull { it.y } ?: 0f,
            stepData.totalStepsCount.toFloat(),
            stepData.totalStepsAvg.toFloat()
        )

        // Set axisMaximum to nearest 1000 multiple, ensuring it covers goal steps
        val axisMax = maxOf(maxValue, stepData.stepsGoal.toFloat()) // Ensure green line is covered
        val adjustedMax = ((axisMax / 1000).toInt() + 1) * 1000 // Round up to next 1000 multiple

        // Set axisMinimum to 0 to ensure bars start from zero
        leftYAxis.axisMinimum = 0f
        leftYAxis.axisMaximum = adjustedMax.toFloat()
        leftYAxis.setDrawZeroLine(true)
        leftYAxis.zeroLineColor = Color.BLACK
        leftYAxis.zeroLineWidth = 1f

        // Custom formatter for dynamic intervals (exact below 1000, 1k above)
        leftYAxis.valueFormatter = object : ValueFormatter() {
            override fun getAxisLabel(value: Float, axis: AxisBase?): String {
                return when {
                    value == 0f -> "0"
                    value < 1000f -> value.toInt().toString() // Exact values below 1000
                    else -> {
                        val kiloValue = (value / 1000f).toInt()
                        "${kiloValue}k"
                    }
                }
            }
        }
        total_steps_count.text = "Total Steps Taken This Week: ${stepData.totalStepsCount.toInt()}"
        // Set granularity to 500f for finer control, adjust based on max value
        leftYAxis.granularity = if (adjustedMax <= 5000f) 500f else 1000f // 500 for small range, 1000 for larger

        val avgStepsLine = LimitLine(stepData.totalStepsAvg.toFloat(), "A")
        avgStepsLine.lineColor = ContextCompat.getColor(requireContext(), R.color.text_color_kcal)
        avgStepsLine.lineWidth = 1f
        avgStepsLine.enableDashedLine(10f, 10f, 0f)
        avgStepsLine.textColor = ContextCompat.getColor(requireContext(), R.color.text_color_kcal)
        avgStepsLine.textSize = 10f
        avgStepsLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP

// Goal Steps Line बनाएं
        val goalStepsLine = LimitLine(stepData.stepsGoal.toFloat(), "G")
        goalStepsLine.lineColor = ContextCompat.getColor(requireContext(), R.color.green_text)
        goalStepsLine.lineWidth = 1f
        goalStepsLine.enableDashedLine(10f, 10f, 0f)
        goalStepsLine.textColor = ContextCompat.getColor(requireContext(), R.color.green_text)
        goalStepsLine.textSize = 10f
        goalStepsLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP

// सभी पुरानी lines हटाएं सिर्फ एक बार
        leftYAxis.removeAllLimitLines()

// दोनों lines add करें
        leftYAxis.addLimitLine(avgStepsLine)
        leftYAxis.addLimitLine(goalStepsLine)
        currentGoal = stepData.stepsGoal

        // Multiline X-axis labels
        val combinedLabels: List<String> = if (entries.size == 30) {
            labels
            /*List(30) { index ->
                when (index) {
                    3 -> "1-7\nJun"
                    10 -> "8-14\nJun"
                    17 -> "15-21\nJun"
                    24 -> "22-28\nJun"
                    28 -> "29-30\nJun"
                    else -> "" // Empty label for spacing
                }
            }*/
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
        val customRenderer = object : RestorativeSleepFragment.MultilineXAxisRenderer(
            barChart.viewPortHandler,
            barChart.xAxis,
            barChart.getTransformer(YAxis.AxisDependency.LEFT)
        ) {
            // Override if needed, but default should work for multiline
        }
        (barChart as BarLineChartBase<*>).setXAxisRenderer(customRenderer)

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.setExtraOffsets(0f, 0f, 0f, 0f) // Ensure no extra padding

        val legend = barChart.legend
        legend.isEnabled = true // Enable legend
        legend.setDrawInside(false) // Keep outside but reduce box
        legend.form = Legend.LegendForm.NONE // Remove the box, keep labels if any
        legend.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals) // Match text color

        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                selectHeartRateLayout.visibility = View.VISIBLE
                if (e != null  && h != null) {
                    val x = e.x.toInt()
                    val y = e.y
                    Log.d("ChartClick", "Clicked X: $x, Steps: $y, LabelsDate size: ${labelsDate.size}")
                    if (x in 0 until labelsDate.size) {
                        selectedItemDate.text = labelsDate[x]
                        selectedCalorieTv.text = y.toInt().toString()
                        val pos = FloatArray(2)
                        pos[0] = h.x
                        pos[1] = 0f
                        // Convert chart value to pixel position
                        barChart.getTransformer(YAxis.AxisDependency.LEFT).pointValuesToPixel(pos)
                        // Ensure chart is drawn before setting position
                        barChart.post {
                            dottedLine.visibility = View.VISIBLE
                            // Use translationX to move the line smoothly
                            dottedLine.translationX = pos[0]
                            // selectHeartRateLayout.translationX = pos[0] - 450
                        }
                        val barX = h.xPx
                        val chartWidth = barChart.width.toFloat()
                        val cardWidth = selectHeartRateLayout.width.toFloat()
                        val targetX = barX - (cardWidth / 2f)
                        val margin = 10 * Resources.getSystem().displayMetrics.density
                        val clampedX = max(margin, min(targetX, chartWidth - cardWidth - margin))
                        selectHeartRateLayout.x = clampedX
                    } else {
                        Log.e("ChartClick", "Index $x out of bounds for labelsDate size ${labelsDate.size}")
                        selectHeartRateLayout.visibility = View.INVISIBLE
                    }
                }
            }

            override fun onNothingSelected() {
                Log.d("ChartClick", "Nothing selected")
                selectHeartRateLayout.visibility = View.INVISIBLE
                dottedLine.visibility = View.GONE
            }
        })

        barChart.animateY(1000)
        barChart.invalidate()
    }
    /** Fetch and update chart with API data */
    private fun fetchStepDetails(period: String) {
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
//                        val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
//                        selectedDate = firstDateOfMonth
                        selectedMonthDate = selectedDate
                    } else {
                      //  val firstDateOfMonth = getFirstDateOfMonth(selectedMonthDate, 1)
                        selectedDate = selectedMonthDate
                    }
                    setSelectedDateMonth(selectedMonthDate, "Month")
                } else {
                    if (selectedHalfYearlyDate.contentEquals("")) {
                        selectedDate = currentDateTime.format(formatter)
//                        val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
//                        selectedDate = firstDateOfMonth
                        selectedHalfYearlyDate = selectedDate
                    } else {
                        //val firstDateOfMonth = getFirstDateOfMonth(selectedHalfYearlyDate, 1)
                        selectedDate = selectedHalfYearlyDate
                    }
                    setSelectedDateMonth(selectedHalfYearlyDate, "Year")
                }

                val response = ApiClient.apiServiceFastApi.getStepsDetail(
                    userId = userId,
                    period = period,
                    date = selectedDate
                )
                if (response.isSuccessful) {
                    if (isAdded  && view != null){
                        requireActivity().runOnUiThread {
                            dismissLoader(requireView())
                        }
                    }
                    val stepTrackerResponse = response.body()
                    if (stepTrackerResponse?.statusCode == 200 && stepTrackerResponse.data.isNotEmpty()) {
                        val stepData = stepTrackerResponse.data[0] // Assuming single data entry
                        withContext(Dispatchers.Main) {
                            if (period == "last_six_months") {
                                barChart.visibility = View.GONE
                                layoutLineChart.visibility = View.VISIBLE
                                lineChartForSixMonths(stepData)
                            } else {
                                barChart.visibility = View.VISIBLE
                                layoutLineChart.visibility = View.GONE
                                val (entries, labels, labelsDate) = when (period) {
                                    "last_weekly" -> processWeeklyData(stepData, selectedDate)
                                    "last_monthly" -> processMonthlyData(stepData, selectedDate)
                                    else -> processWeeklyData(stepData, selectedDate) // Fallback
                                }
                                updateChart(entries, labels, labelsDate, stepData)
                            }
                            // Update average and comparison UI
                            setStepStats(stepData, period)
                            heart_rate_description_heading.text = stepTrackerResponse.data.get(0).heading.toString()
                            step_discreption.text = stepTrackerResponse.data.get(0).description.toString()

                        }
                    } else {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(requireContext(), "No step data available", Toast.LENGTH_SHORT).show()
                            if (isAdded  && view != null){
                                requireActivity().runOnUiThread {
                                    dismissLoader(requireView())
                                }
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "Step data  ${response.message()}", Toast.LENGTH_SHORT).show()
                        barChart.visibility = View.GONE
                        averageBurnCalorie.text = "0"
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

    /** Process API data for last_weekly period (7 days) */
    private fun processWeeklyData(stepData: StepTrackerData, currentDate: String): Triple<List<BarEntry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateString = currentDate
        val date = dateFormat.parse(dateString)
        calendar.time = date!!
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)
        calendar.set(year, month, day)
        calendar.add(Calendar.DAY_OF_YEAR, -6) // Start of the week
        val stepMap = mutableMapOf<String, Float>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

        // Initialize 7 days with 0 steps
        repeat(7) {
            val dateStr = dateFormat.format(calendar.time)
            stepMap[dateStr] = 0f
            val dateLabel = (convertDate(dateStr) + "," + year)
            val dayString = dayFormat.format(calendar.time)
            labels.add(dayString)
            labelsDate.add(dateLabel)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        // Aggregate steps by day using record_details
        stepData.recordDetails.forEach { record ->
            val dayKey = record.date
            stepMap[dayKey] = record.totalStepsCountPerDay.toFloat()
        }

        val entries = stepMap.entries.mapIndexed { index, entry -> BarEntry(index.toFloat(), entry.value) }
        return Triple(entries, labels, labelsDate)
    }

    /** Process API data for monthly period (5 weeks) */
    private fun processMonthlyData(stepData: StepTrackerData, currentDate: String): Triple<List<BarEntry>, List<String>, List<String>> {
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
        if ( stepData.recordDetails.isNotEmpty()){
            stepData.recordDetails.forEach { calorie ->
                val startDate = dateFormat.parse(calorie.date)?.let { Date(it.time) }
                if (startDate != null) {
                    val dayKey = dateFormat.format(startDate)
                    calorieMap[dayKey] = calorieMap[dayKey]!! + (calorie.totalStepsCountPerDay.toFloat() ?: 0f)
                }
            }
        }
        //setLastAverageValue(activeCaloriesResponse, "% Past Month")
        val entries = calorieMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        return Triple(entries, weeklyLabels, labelsDate)
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

    /** Process API data for six_monthly period (6 months) */
    private fun processSixMonthsData(stepData: StepTrackerData, currentDate: String): Triple<List<BarEntry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val yearMonthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        val dateString = currentDate
        val date = dateFormat.parse(dateString)
        calendar.time = date!!
        calendar.add(Calendar.MONTH, -5) // Start 6 months back

        val stepMap = mutableMapOf<Int, Float>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()

        // Initialize 6 months
        repeat(6) { month ->
            stepMap[month] = 0f
            labels.add(monthFormat.format(calendar.time))
            labelsDate.add(convertMonth(dateFormat.format(calendar.time)) + "," + calendar.get(Calendar.YEAR))
            calendar.add(Calendar.MONTH, 1)
        }

        // Aggregate steps by month using record_details
        stepData.recordDetails.forEach { record ->
            val recordDate = dateFormat.parse(record.date)
            if (recordDate != null) {
                calendar.time = recordDate
                val recordYearMonth = yearMonthFormat.format(recordDate)
                calendar.time = dateFormat.parse(currentDate)
                calendar.add(Calendar.MONTH, -5) // Reset to start of 6-month period
                for (monthIndex in 0..5) {
                    val periodYearMonth = yearMonthFormat.format(calendar.time)
                    if (recordYearMonth == periodYearMonth) {
                        stepMap[monthIndex] = stepMap[monthIndex]?.plus(record.totalStepsCountPerDay.toFloat()) ?: record.totalStepsCountPerDay.toFloat()
                    }
                    calendar.add(Calendar.MONTH, 1)
                }
            }
        }

        val entries = stepMap.entries.mapIndexed { index, entry -> BarEntry(index.toFloat(), entry.value) }
        return Triple(entries, labels, labelsDate)
    }

    /** Update UI with step stats (average and comparison) */
    private fun setStepStats(stepData: StepTrackerData, period: String) {
        activity?.runOnUiThread {
            // Set average steps
            averageBurnCalorie.visibility = View.VISIBLE
            averageBurnCalorie.text = stepData.totalStepsAvg.toInt().toString()

            // Set comparison percentage
            val currentAverage = stepData.comparison.currentAverageStepsPerDay
            val previousAverage = stepData.comparison.previousAverageStepsPerDay
            val percentage = if (previousAverage != 0.0) ((currentAverage - previousAverage) / previousAverage * 100).toInt() else 0
            val periodLabel = when (period) {
                "last_weekly" -> "% Past Week"
                "last_monthly" -> "% Past Month"
                "last_six_months" -> "% Past 6 Months"
                else -> "%"
            }
            if (percentage > 0) {
                percentageTv.text = "+$percentage$periodLabel"
                percentageIc.setImageResource(R.drawable.ic_up)
            } else if (percentage < 0) {
                percentageTv.text = "$percentage$periodLabel"
                percentageIc.setImageResource(R.drawable.ic_down)
            } else {
                percentageTv.text = "0$periodLabel"
                percentageIc.setImageResource(R.drawable.ic_up)
            }
            perDayStepAverage.text = stepData.comparison.currentAverageStepsPerDay.toInt().toString()
            valuePreviousWeek.text = stepData.comparison.previousAverageStepsPerDay.toInt().toString()

            val currentPercentage = (stepData.comparison.currentAverageStepsPerDay.toFloat() / stepData.stepsGoal.toFloat()).coerceIn(0f, 1f)

            val previousPercentage = (stepData.comparison.previousAverageStepsPerDay.toFloat() / stepData.stepsGoal.toFloat()).coerceIn(0f, 1f)

            customProgressBarFatBurn.progress = currentPercentage
            customProgressPreviousWeek.progress = previousPercentage
        }
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
            val dateView: String = convertDate(dateStr) + "-" + convertDate(selectedWeekDate) + "," + year
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

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    private fun setupLineChart() {
        lineChart.apply {
            axisLeft.apply {
                axisMinimum = 0f
                setDrawGridLines(true)
                textColor = Color.BLACK
                textSize = 10f
                setLabelCount(6, true)
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

    private fun lineChartForSixMonths(stepData: StepTrackerData) {
        // Aggregate steps by month
        val monthFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())
        val monthLabelFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val stepMap = mutableMapOf<String, Float>()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Initialize 6 months
        calendar.time = dateFormat.parse(stepData.startDate)!!
        repeat(6) {
            val monthKey = monthFormat.format(calendar.time)
            stepMap[monthKey] = 0f
            calendar.add(Calendar.MONTH, 1)
        }

        // Aggregate steps by month
        stepData.recordDetails.forEach { record ->
            val recordDate = dateFormat.parse(record.date)
            if (recordDate != null) {
                val monthKey = monthFormat.format(recordDate)
                stepMap[monthKey] = stepMap[monthKey]?.plus(record.totalStepsCountPerDay.toFloat()) ?: record.totalStepsCountPerDay.toFloat()
            }
        }

        val entries = stepMap.entries.sortedBy { it.key }.mapIndexed { index, entry ->
            Entry(index.toFloat(), entry.value)
        }.ifEmpty { listOf(Entry(0f, 0f), Entry(1f, 0f)) }

        val dataSet = LineDataSet(entries, "Steps").apply {
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

        val labels = stepMap.keys.sorted().map { monthLabelFormat.format(monthFormat.parse(it)!!) }
            .ifEmpty { listOf("No Data", "No Data") }
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        lineChart.xAxis.apply {
            labelCount = labels.size.coerceAtLeast(2)
        }

        lineChart.axisLeft.apply {
            axisMaximum = (entries.maxOfOrNull { it.y }?.times(1.2f) ?: 10000f) // Default max for steps
            valueFormatter = object : IndexAxisValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    return value.toInt().toString()
                }
            }
        }

        lineChart.animateX(1000)
        lineChart.animateY(1000)
        lineChart.invalidate()

        lineChart.post {
            val transformer = lineChart.getTransformer(YAxis.AxisDependency.LEFT)
            val hMargin = 40f
            val vMargin = 20f
            stripsContainer.removeAllViews()
            stepMap.entries.sortedBy { it.key }.forEachIndexed { index, entry ->
                val monthSteps = entry.value
                val monthLabel = labels[index]
                val xStartFraction = index.toFloat() / (stepMap.size - 1)
                val xEndFraction = index.toFloat() / (stepMap.size - 1)
                val yFraction = monthSteps / (entries.maxOfOrNull { it.y }?.toFloat() ?: 1f)
                val pixelStart = transformer.getPixelForValues(
                    (xStartFraction * (lineChart.width - 2 * hMargin)) + hMargin,
                    ((1 - yFraction) * (lineChart.height - 2 * vMargin) + vMargin).toFloat()
                )
                val pixelEnd = transformer.getPixelForValues(
                    (xEndFraction * (lineChart.width - 2 * hMargin)) + hMargin,
                    ((1 - yFraction) * (lineChart.height - 2 * vMargin) + vMargin).toFloat()
                )

                // Draw capsule
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
                    text = "${monthSteps.toInt()}"
                    setTextColor(Color.BLACK)
                    textSize = 12f
                    setPadding(6, 4, 6, 4)
                    layoutParams = FrameLayout.LayoutParams(
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT
                    )
                    x = (((pixelStart.x + pixelEnd.x) / 2 - 20).toFloat())
                    y = (pixelStart.y - 20).toFloat()
                }
                stripsContainer.addView(avgText)
            }
        }
    }
}