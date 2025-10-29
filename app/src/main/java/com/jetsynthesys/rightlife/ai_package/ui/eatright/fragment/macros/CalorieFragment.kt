package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.macros

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Typeface
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
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
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.renderer.YAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.ThinkRecomendedResponse
import com.jetsynthesys.rightlife.ai_package.model.response.ConsumedCaloriesResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.KValueFormatter
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.adapter.RecommendedAdapterSleep
import com.jetsynthesys.rightlife.ai_package.utils.BadgeLimitLineRenderer
import com.jetsynthesys.rightlife.databinding.FragmentCalorieBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CalorieFragment : BaseFragment<FragmentCalorieBinding>() {

    private lateinit var barChart: BarChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var backwardImage: ImageView
    private lateinit var forwardImage: ImageView
    private var selectedWeekDate: String = ""
    private var selectedMonthDate: String = ""
    private var selectedHalfYearlyDate: String = ""
    private lateinit var selectedDate: TextView
    private lateinit var calorie_description_heading: TextView
    private lateinit var calorie_description_text: TextView
    private lateinit var selectedItemDate: TextView
    private lateinit var selectHeartRateLayout: CardView
    private lateinit var selectedCalorieTv: TextView
    private lateinit var averageBurnCalorie: TextView
    private lateinit var averageHeading: TextView
    private lateinit var percentageTv: TextView
    private lateinit var percentageIc: TextView
    private lateinit var totalCalorie : TextView
    private lateinit var layoutLineChart: FrameLayout
    private lateinit var stripsContainer: FrameLayout
    private lateinit var averageGoalLayout : LinearLayoutCompat
    private lateinit var goalLayout : LinearLayoutCompat
    private lateinit var lineChart: LineChart
    private var loadingOverlay : FrameLayout? = null
    private lateinit var recomendationRecyclerView: RecyclerView
    private lateinit var thinkRecomendedResponse : ThinkRecomendedResponse
    private lateinit var recomendationAdapter: RecommendedAdapterSleep

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentCalorieBinding
        get() = FragmentCalorieBinding::inflate

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

        barChart = view.findViewById(R.id.heartRateChart)
        radioGroup = view.findViewById(R.id.tabGroup)
        backwardImage = view.findViewById(R.id.backward_image_calorie)
        forwardImage = view.findViewById(R.id.forward_image_calorie)
        selectedDate = view.findViewById(R.id.selectedDateCalorie)
        selectedItemDate = view.findViewById(R.id.selectedDate)
        selectHeartRateLayout = view.findViewById(R.id.selectCalorieLayout)
        selectedCalorieTv = view.findViewById(R.id.selectedCalorieTv)
        percentageTv = view.findViewById(R.id.percentage_text)
        averageBurnCalorie = view.findViewById(R.id.average_number)
        averageHeading = view.findViewById(R.id.averageHeading)
        percentageIc = view.findViewById(R.id.percentageIc)
        layoutLineChart = view.findViewById(R.id.lyt_line_chart)
        stripsContainer = view.findViewById(R.id.stripsContainer)
        lineChart = view.findViewById(R.id.heartLineChart)
        calorie_description_heading = view.findViewById(R.id.calorie_description_heading)
        calorie_description_text = view.findViewById(R.id.calorie_description_text)
        totalCalorie = view.findViewById(R.id.totalCalorie)
        goalLayout = view.findViewById(R.id.goalLayout)
        averageGoalLayout = view.findViewById(R.id.averageGoalLayout)
        recomendationRecyclerView = view.findViewById(R.id.recommendationRecyclerView)

        // Set default selection to Week
        radioGroup.check(R.id.rbWeek)
        fetchActiveCalories("last_weekly")
       /* fetchActiveCalories("last_weekly")
        setupLineChart()*/

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
                R.id.rbWeek -> fetchActiveCalories("last_weekly")
                R.id.rbMonth -> fetchActiveCalories("last_monthly")
                R.id.rbSixMonths -> {Toast.makeText(requireContext(),"Coming Soon",Toast.LENGTH_SHORT).show()}
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
                calendar.time = date!!
                val year = calendar.get(Calendar.YEAR)
                val month = calendar.get(Calendar.MONTH)
                val day = calendar.get(Calendar.DAY_OF_MONTH)
                calendar.set(year, month, day)
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val dateStr = dateFormat.format(calendar.time)
                selectedWeekDate = dateStr
                fetchActiveCalories("last_weekly")
            } else if (selectedTab == "Month") {
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
                fetchActiveCalories("last_monthly")
            } else {
                Toast.makeText(requireContext(),"Coming Soon",Toast.LENGTH_SHORT).show()
               /* selectedHalfYearlyDate = ""
                fetchActiveCalories("last_six_months")*/
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
                    calendar.time = date!!
                    val year = calendar.get(Calendar.YEAR)
                    val month = calendar.get(Calendar.MONTH)
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    calendar.set(year, month, day)
                    calendar.add(Calendar.DAY_OF_YEAR, +7)
                    val dateStr = dateFormat.format(calendar.time)
                    selectedWeekDate = dateStr
                    fetchActiveCalories("last_weekly")
                } else {
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            } else if (selectedTab == "Month") {
                if (selectedMonthDate != currentDate) {
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
                    fetchActiveCalories("last_monthly")
                } else {
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(),"Coming Soon",Toast.LENGTH_SHORT).show()
               /* if (selectedHalfYearlyDate != currentDate) {
                    selectedHalfYearlyDate = ""
                    fetchActiveCalories("last_six_months")
                } else {
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }*/
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToFragment(HomeBottomTabFragment(), "landingFragment")
        }

        fetchEatRecommendedData()
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        val args = Bundle()
        args.putString("ModuleName", "EatRight")
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    private fun updateChart(entries: List<BarEntry>, labels: List<String>, labelsDate: List<String>,  activeCaloriesResponse: ConsumedCaloriesResponse) {
        selectHeartRateLayout.visibility = View.VISIBLE
        val dataSet = BarDataSet(entries, "")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.light_green)
        dataSet.valueTextColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        dataSet.valueTextSize = 12f
        dataSet.setDrawValues(entries.size <= 7)
        dataSet.barShadowColor = Color.TRANSPARENT
        dataSet.highLightColor = ContextCompat.getColor(requireContext(), R.color.light_green)

        val barData = BarData(dataSet)
        barData.barWidth = 0.4f
        barChart.data = barData
        barChart.setFitBars(true)
        barChart.setScaleEnabled(false)
        barChart.isDoubleTapToZoomEnabled = false
        barChart.isHighlightPerTapEnabled = true
        barChart.isHighlightPerDragEnabled = false

        // X-axis label handling
        val combinedLabels = if (entries.size == 30) {
            labels
        } else {
            labels.take(entries.size).zip(labelsDate.take(entries.size)) { label, date ->
                val cleanedDate = date.substringBefore(",")
                "$label\n$cleanedDate"
            }
        }

        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(combinedLabels)
        xAxis.labelCount =  entries.size
        xAxis.setDrawLabels(true)
        xAxis.setAvoidFirstLastClipping(false)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.labelRotationAngle = 0f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        xAxis.yOffset = 15f
        xAxis.axisMinimum = -0.5f
        xAxis.axisMaximum = entries.size - 0.5f
        xAxis.setCenterAxisLabels(false)

        // Custom XAxis Renderer (multiline support)
        val customRenderer = MultilineXAxisRenderer(
            barChart.viewPortHandler,
            barChart.xAxis,
            barChart.getTransformer(YAxis.AxisDependency.LEFT)
        )
        (barChart as BarLineChartBase<*>).setXAxisRenderer(customRenderer)

        // Y-axis
        val leftYAxis: YAxis = barChart.axisLeft
        leftYAxis.textSize = 12f
        leftYAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        leftYAxis.setDrawGridLines(true)
        leftYAxis.axisMinimum = 0f
        leftYAxis.axisMaximum = entries.maxByOrNull { it.y }?.y?.plus(100f) ?: 1000f
        leftYAxis.granularity = 1f

        leftYAxis.granularity = 1000f
        leftYAxis.labelCount = ((leftYAxis.axisMaximum / 1000).toInt() + 1)
        leftYAxis.valueFormatter = KValueFormatter()

        barChart.rendererLeftYAxis = BadgeLimitLineRenderer(
            barChart.viewPortHandler,
            barChart.axisLeft,
            barChart.getTransformer(YAxis.AxisDependency.LEFT)
        )

        if (entries.size < 30){
            val minValue = minOf(
                entries.minOfOrNull { it.y } ?: 0f,
                activeCaloriesResponse.goal.toFloat(),
                activeCaloriesResponse.currentAvgCalories.toFloat()
            )
            val maxValue = maxOf(
                entries.maxOfOrNull { it.y } ?: 0f,
                activeCaloriesResponse.goal.toFloat(),
                activeCaloriesResponse.currentAvgCalories.toFloat()
            )
            // Include stepsGoal in max check
            val axisMax = maxOf(maxValue, activeCaloriesResponse.goal.toFloat())

            leftYAxis.axisMinimum = if (minValue < 0) minValue * 1.2f else 0f
            leftYAxis.axisMaximum = axisMax * 1.2f
            leftYAxis.setDrawZeroLine(true)
            // leftYAxis.zeroLineColor = Color.BLACK
            leftYAxis.zeroLineWidth = 1f

            val totalStepsLine = LimitLine(activeCaloriesResponse.goal.toFloat(), "G")
            totalStepsLine.lineColor = ContextCompat.getColor(requireContext(), R.color.green_minimal)
            totalStepsLine.lineWidth = 1f
            totalStepsLine.enableDashedLine(10f, 10f, 0f)
            totalStepsLine.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            totalStepsLine.textSize = 10f
            totalStepsLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP

            val avgStepsLine = LimitLine(activeCaloriesResponse.currentAvgCalories.toFloat(), "A")
            avgStepsLine.lineColor = ContextCompat.getColor(requireContext(), R.color.text_color_kcal)
            avgStepsLine.lineWidth = 1f
            avgStepsLine.enableDashedLine(10f, 10f, 0f)
            avgStepsLine.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            avgStepsLine.textSize = 10f
            avgStepsLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_BOTTOM

            leftYAxis.removeAllLimitLines()
            leftYAxis.addLimitLine(totalStepsLine)
            leftYAxis.addLimitLine(avgStepsLine)
            averageGoalLayout.visibility = View.VISIBLE
            goalLayout.visibility = View.VISIBLE
        }else{
            leftYAxis.removeAllLimitLines()
            averageGoalLayout.visibility = View.GONE
            goalLayout.visibility = View.GONE
        }

        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        // Optional chart description
        val description = Description().apply {
            text = ""
            textColor = Color.BLACK
            textSize = 14f
            setPosition(barChart.width / 2f, barChart.height.toFloat() - 10f)
        }
        barChart.description = description
        barChart.setExtraOffsets(0f, 0f, 0f, 25f)
        // Legend
        val legend = barChart.legend
        legend.setDrawInside(false)

        selectedItemDate.text = labelsDate.getOrNull(entries.size-1) ?: ""
        selectedCalorieTv.text = entries.get(entries.size-1).y.toInt().toString()

        // Chart selection listener
        barChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                selectHeartRateLayout.visibility = View.VISIBLE
                e?.let {
                    val x = e.x.toInt()
                    val y = e.y
                    Log.d("ChartClick", "Clicked X: $x, Y: $y")
                    selectedItemDate.text = labelsDate.getOrNull(x) ?: ""
                    selectedCalorieTv.text = y.toInt().toString()
                }
            }
            override fun onNothingSelected() {
                Log.d("ChartClick", "Nothing selected")
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
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            try {
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        showLoader(requireView())
                    }
                }
                val userId = SharedPreferenceManager.getInstance(requireActivity()).userId
                val currentDateTime = LocalDateTime.now()
                val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                var selectedDate = currentDateTime.format(formatter)
                // Run date selection + UI-safe calls on the Main thread
                withContext(Dispatchers.Main) {
                    when (period) {
                        "last_weekly" -> {
                            if (selectedWeekDate.isEmpty()) {
                                selectedWeekDate = selectedDate
                            } else {
                                selectedDate = selectedWeekDate
                            }
                            setSelectedDate(selectedWeekDate)
                        }
                        "last_monthly" -> {
                            if (selectedMonthDate.isEmpty()) {
                              //  val firstDate = getFirstDateOfMonth(selectedDate, 1)
                                selectedMonthDate = selectedDate
                              //  selectedDate = firstDate
                            } else {
                                selectedDate = selectedMonthDate//getFirstDateOfMonth(selectedMonthDate, 1)
                            }
                            setSelectedDateMonth(selectedMonthDate, "Month")
                        }
                        else -> { // last_six_months or default
                            if (selectedHalfYearlyDate.isEmpty()) {
                                //val firstDate = getFirstDateOfMonth(selectedDate, 1)
                                selectedHalfYearlyDate = selectedDate
                                //selectedDate = firstDate
                            } else {
                                selectedDate = selectedHalfYearlyDate//getFirstDateOfMonth(selectedHalfYearlyDate, 1)
                            }
                            setSelectedDateMonth(selectedHalfYearlyDate, "Year")
                        }
                    }
                }

                // Proceed with API call
                val response = ApiClient.apiServiceFastApiV2.getConsumedCalories(
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
                    val data = response.body()
                    if (data?.statusCode == 200) {
                        val (entries, labels, labelsDate) = when (period) {
                            "last_weekly" -> processWeeklyData(data, selectedDate)
                            "last_monthly" -> processMonthlyData(data, selectedDate)
                            "last_six_months" -> processSixMonthsData(data, selectedDate)
                            else -> Triple(getWeekData(), getWeekLabels(), getWeekLabelsDate())
                        }

                        // Ensure all UI updates are on the main thread
                        withContext(Dispatchers.Main) {
                            calorie_description_heading.text = data.heading
                            calorie_description_text.text = formatMarkdownBold(data.description)
                            if (data.consumedCalorieTotals.size > 31) {
                                barChart.visibility = View.GONE
                                layoutLineChart.visibility = View.VISIBLE
                            } else {
                                barChart.visibility = View.VISIBLE
                                layoutLineChart.visibility = View.GONE
                                updateChart(entries, labels, labelsDate, data) // Must be thread-safe (UI updates on main thread)
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

    private fun formatMarkdownBold(input: String): SpannableStringBuilder {
        val result = SpannableStringBuilder()
        val regex = Regex("\\*\\*(.*?)\\*\\*") // matches text between ** **
        var lastIndex = 0
        regex.findAll(input).forEach { match ->
            val range = match.range
            val boldText = match.groupValues[1]
            // Append text before the bold part
            result.append(input.substring(lastIndex, range.first))
            // Apply bold
            val start = result.length
            result.append(boldText)
            result.setSpan(
                StyleSpan(Typeface.BOLD),
                start,
                start + boldText.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            lastIndex = range.last + 1
        }
        // Append remaining text after the last match
        if (lastIndex < input.length) {
            result.append(input.substring(lastIndex))
        }
        return result
    }

    /** Process API data for last_weekly (7 days) */
    private fun processWeeklyData(
        activeCaloriesResponse: ConsumedCaloriesResponse,
        currentDate: String
    ): Triple<List<BarEntry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()
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
            val dateLabel = "${convertDate(dateStr)}, $year"
            val dayString = dayFormat.format(calendar.time)
            labels.add(dayString)
            labelsDate.add(dateLabel)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        // Aggregate calories by day
        if (activeCaloriesResponse.consumedCalorieTotals.isNotEmpty()) {
            activeCaloriesResponse.consumedCalorieTotals.forEach { calorie ->
                val startDate = dateFormat.parse(calorie.date)?.let { Date(it.time) }
                if (startDate != null) {
                    val dayKey = dateFormat.format(startDate)
                    calorieMap[dayKey] = calorieMap[dayKey]!! + (calorie.caloriesConsumed.toFloat() ?: 0f)
                }
            }
        }
        setLastAverageValue(activeCaloriesResponse, "% Past Week")
        val entries = calorieMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        return Triple(entries, labels, labelsDate)
    }

    /** Process API data for last_monthly (5 weeks) */
    private fun processMonthlyData(
        activeCaloriesResponse: ConsumedCaloriesResponse,
        currentDate: String
    ): Triple<List<BarEntry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
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

        val days = getDaysInMonth(month + 1, year)
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
        if (activeCaloriesResponse.consumedCalorieTotals.isNotEmpty()) {
            activeCaloriesResponse.consumedCalorieTotals.forEach { calorie ->
                val parsedDate = calorie.date.let { dateFormat.parse(it) }
                if (parsedDate != null) {
                    val dayKey = dateFormat.format(parsedDate)
                    if (calorieMap.containsKey(dayKey)) {
                        val existing = calorieMap[dayKey] ?: 0f
                        val consumed = calorie.caloriesConsumed.toFloat() ?: 0f
                        calorieMap[dayKey] = existing + consumed
                    }
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
    private fun processSixMonthsData(
        activeCaloriesResponse: ConsumedCaloriesResponse,
        currentDate: String
    ): Triple<List<BarEntry>, List<String>, List<String>> {
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

        if (activeCaloriesResponse.consumedCalorieTotals.isNotEmpty()) {
            // Aggregate calories by month
            activeCaloriesResponse.consumedCalorieTotals.forEach { calorie ->
                val startDate = dateFormat.parse(calorie.date)?.let { Date(it.time) }
                if (startDate != null) {
                    calendar.time = startDate
                    val monthDiff = ((2025 - 1900) * 12 + Calendar.MARCH) - ((calendar.get(Calendar.YEAR) - 1900) * 12 + calendar.get(Calendar.MONTH))
                    val monthIndex = 5 - monthDiff // Reverse to align with labels (0 = earliest month)
                    if (monthIndex in 0..5) {
                        calorieMap[monthIndex] = calorieMap[monthIndex]!! + (calorie.caloriesConsumed.toFloat() ?: 0f)
                    }
                }
            }
        }
        setLastAverageValue(activeCaloriesResponse, "% Past 6 Month")
        val entries = calorieMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        return Triple(entries, labels, labelsDate)
    }

    private fun setSelectedDate(selectedWeekDate: String) {
        // Called on main thread via fetchActiveCalories
        activity?.runOnUiThread {
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
            val dateView: String = "${convertDate(dateStr)}-${convertDate(selectedWeekDate)}, $year"
            selectedDate.text = dateView
            selectedDate.gravity = Gravity.CENTER
        }
    }

    private fun setSelectedDateMonth(selectedMonthDate: String, dateViewType: String) {
        // Called on main thread via fetchActiveCalories
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

    private fun setLastAverageValue(activeCaloriesResponse: ConsumedCaloriesResponse, type: String) {
        activity?.runOnUiThread {
            averageBurnCalorie.text = activeCaloriesResponse.currentAvgCalories.toInt().toString()
            totalCalorie.text = activeCaloriesResponse.totalCalories.toInt().toString()
            if (activeCaloriesResponse.progressSign == "plus") {
                percentageTv.text = "${activeCaloriesResponse.progressPercentage.toInt()} $type"
                // percentageIc.setImageResource(R.drawable.ic_up)
            } else if (activeCaloriesResponse.progressSign == "minus") {
                percentageTv.text = "${activeCaloriesResponse.progressPercentage.toInt()} $type"
                // percentageIc.setImageResource(R.drawable.ic_down)
            }
        }
    }

    private fun fetchEatRecommendedData() {
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
        val call = ApiClient.apiService.fetchThinkRecomended(token,"HOME","EAT_RIGHT")
        call.enqueue(object : Callback<ThinkRecomendedResponse> {
            override fun onResponse(call: Call<ThinkRecomendedResponse>, response: Response<ThinkRecomendedResponse>) {
                if (response.isSuccessful) {
                    // progressDialog.dismiss()
                    thinkRecomendedResponse = response.body()!!
                    if (thinkRecomendedResponse.data?.contentList?.isNotEmpty() == true) {
                        recomendationAdapter = RecommendedAdapterSleep(context!!, thinkRecomendedResponse.data?.contentList!!)
                        recomendationRecyclerView.layoutManager = LinearLayoutManager(requireContext())
                        recomendationRecyclerView.adapter = recomendationAdapter
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    //          Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    // progressDialog.dismiss()F
                }
            }
            override fun onFailure(call: Call<ThinkRecomendedResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                //          Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                //progressDialog.dismiss()
            }
        })
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

    private fun formatDate(date: Date, format: String): String {
        val formatter = SimpleDateFormat(format, Locale.getDefault())
        return formatter.format(date)
    }

    private fun updateDateRangeLabel() {
        val sdf = SimpleDateFormat("d MMM yyyy", Locale.getDefault())
        // dateRangeLabel.text = "${sdf.format(viewModel.startDate)} - ${sdf.format(viewModel.endDate)}"
    }

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }
}

class MultilineXAxisRenderer(
    viewPortHandler: ViewPortHandler,
    xAxis: XAxis,
    trans: Transformer
) : XAxisRenderer(viewPortHandler, xAxis, trans) {

    override fun drawLabel(
        c: Canvas,
        formattedLabel: String,
        x: Float,
        y: Float,
        anchor: MPPointF,
        angleDegrees: Float
    ) {
        val lines = formattedLabel.split("\n")

        val topMargin = 18f // 👈 Add your desired top margin in pixels

        for ((i, line) in lines.withIndex()) {
            val offsetY = i * mAxisLabelPaint.textSize * 1.1f
            c.drawText(line, x, y + topMargin + offsetY, mAxisLabelPaint)
        }
    }
}

class CustomYAxisRenderer(
    viewPortHandler: ViewPortHandler,
    yAxis: YAxis,
    trans: Transformer
) : YAxisRenderer(viewPortHandler, yAxis, trans) {

    private val labelBgPaint = Paint().apply {
        style = Paint.Style.FILL
        color = Color.parseColor("#D0F0C0") // light green background
        isAntiAlias = true
    }

    override fun renderLimitLines(c: Canvas) {
        val limitLines = mYAxis.limitLines
        if (limitLines == null || limitLines.isEmpty()) return

        for (limitLine in limitLines) {
            if (!limitLine.isEnabled) continue

            val limit = limitLine.limit
            val pts = floatArrayOf(0f, limit)
            mTrans.pointValuesToPixel(pts)

            // draw limit line
            mLimitLinePaint.style = Paint.Style.STROKE
            mLimitLinePaint.color = limitLine.lineColor
            mLimitLinePaint.strokeWidth = limitLine.lineWidth
            mLimitLinePaint.pathEffect = limitLine.dashPathEffect

            val y = pts[1]
            c.drawLine(mViewPortHandler.contentLeft(), y, mViewPortHandler.contentRight(), y, mLimitLinePaint)

            val label = limitLine.label ?: continue
            if (label.isEmpty()) continue

            // setup text paint
            mLimitLinePaint.style = Paint.Style.FILL
            mLimitLinePaint.color = limitLine.textColor
            mLimitLinePaint.textSize = limitLine.textSize
            mLimitLinePaint.typeface = limitLine.typeface

            // measure text
            val textWidth = mLimitLinePaint.measureText(label)
            val textHeight = mLimitLinePaint.fontMetrics.run { descent - ascent }

            // set label position
            val x = mViewPortHandler.contentRight() - textWidth - 10f
            val yLabel = y - textHeight / 2

            // draw background rect
            val rect = RectF(x - 8, yLabel - 4, x + textWidth + 8, yLabel + textHeight + 4)
            c.drawRoundRect(rect, 8f, 8f, labelBgPaint)

            // draw text
            c.drawText(label, x, yLabel + textHeight, mLimitLinePaint)
        }
    }
}

