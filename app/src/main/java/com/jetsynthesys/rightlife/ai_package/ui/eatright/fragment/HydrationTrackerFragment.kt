package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Html
import android.text.Spanned
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModel
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.LimitLine
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.response.ConsumedCaloriesResponse
import com.jetsynthesys.rightlife.ai_package.model.response.WaterIntakeResponse
import com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment.macros.MultilineXAxisRenderer
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.RestorativeSleepFragment
import com.jetsynthesys.rightlife.ai_package.utils.BadgeLimitLineRenderer
import com.jetsynthesys.rightlife.databinding.FragmentHydrationTrackerBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

class HydrationTrackerFragment : BaseFragment<FragmentHydrationTrackerBinding>() {

    private lateinit var lineChart: LineChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var stripsContainer: FrameLayout
    private lateinit var layoutLineChart: FrameLayout
    private lateinit var backwardImage: ImageView
    private lateinit var backwardImageHydration: ImageView
    private lateinit var today_water_intake_layout: CardView
    private lateinit var log_your_water_intake_filled : LinearLayout
    private lateinit var forwardImage: ImageView
    private lateinit var selectedDate: TextView
    private lateinit var waterQuantityTv: TextView
    private lateinit var glassWithWaterView: ImageView
    private lateinit var hydration_description_heading: TextView
    private lateinit var hydration_description_text: TextView
    private var selectedWeekDate: String = ""
    private var selectedMonthDate: String = ""
    private var selectedHalfYearlyDate: String = ""
    private lateinit var selectedItemDate : TextView
    private lateinit var selectHeartRateLayout : CardView
    private lateinit var selectedCalorieTv : TextView
    private lateinit var averageWaterIntake: TextView
    private lateinit var averageHeading: TextView
    private lateinit var percentageTv: TextView
    private lateinit var averageGoalLayout : LinearLayoutCompat
    private var loadingOverlay : FrameLayout? = null
    private var waterIntakeValue : Float = 0f

    private val viewModel: HydrationViewModel by viewModels()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHydrationTrackerBinding
        get() = FragmentHydrationTrackerBinding::inflate

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.meal_log_background))

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
        hydration_description_heading = view.findViewById(R.id.hydration_description_heading)
        hydration_description_text = view.findViewById(R.id.hydration_description_text)
        log_your_water_intake_filled = view.findViewById(R.id.log_your_water_intake_filled)
        waterQuantityTv = view.findViewById(R.id.tv_water_quantity)
        glassWithWaterView = view.findViewById<ImageView>(R.id.glass_with_water_view)
        today_water_intake_layout = view.findViewById(R.id.today_water_intake_layout)
        backwardImageHydration = view.findViewById(R.id.backIc)
        averageGoalLayout = view.findViewById(R.id.averageGoalLayout)
        percentageTv = view.findViewById(R.id.percentage_text)
        averageWaterIntake = view.findViewById(R.id.average_number)

        backwardImageHydration.setOnClickListener {
            val fragment = HomeBottomTabFragment()
            val args = Bundle()
            args.putString("ModuleName", "EatRight")
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }
        // Show Week data by default
        radioGroup.check(R.id.rbWeek)
        fetchWaterIntakeData("last_weekly")
        //today_water_intake_layout.visibility = View.VISIBLE

        // setupLineChart()
        log_your_water_intake_filled.setOnClickListener {
            showWaterIntakeBottomSheet()
        }
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
                R.id.rbWeek ->{
                    layoutLineChart.visibility =View.VISIBLE
                    fetchWaterIntakeData("last_weekly")
                    today_water_intake_layout.visibility = View.VISIBLE
                }
                R.id.rbMonth ->{
                    layoutLineChart.visibility =View.VISIBLE
                    fetchWaterIntakeData("last_monthly")
                    today_water_intake_layout.visibility = View.GONE
                }
                R.id.rbSixMonths -> {
                    Toast.makeText(requireContext(),"Comming Soon",Toast.LENGTH_SHORT).show()
                    layoutLineChart.visibility =View.GONE
                    today_water_intake_layout.visibility = View.GONE
                    //fetchWaterIntakeData("last_six_months")
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
                fetchWaterIntakeData("last_weekly")
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
                fetchWaterIntakeData("last_monthly")
            } else {
                Toast.makeText(requireContext(),"Comming Soon",Toast.LENGTH_SHORT).show()
                layoutLineChart.visibility =View.GONE
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
                    fetchWaterIntakeData("last_weekly")
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
                    calendar.set(year, month, day)
                    calendar.add(Calendar.DAY_OF_YEAR, +30)
                    val dateStr = dateFormat.format(calendar.time)
                    //  val firstDateOfMonth = getFirstDateOfMonth(dateStr, 1)
                    selectedMonthDate = dateStr
                    fetchWaterIntakeData("last_monthly")
                } else {
                    Toast.makeText(context, "Cannot select future date", Toast.LENGTH_SHORT).show()
                }
            } else {
                Toast.makeText(requireContext(),"Comming Soon",Toast.LENGTH_SHORT).show()
                layoutLineChart.visibility =View.GONE
               /* if (!selectedHalfYearlyDate.contentEquals(currentDate)) {
                    selectedHalfYearlyDate = ""
                    fetchWaterIntakeData("last_six_months")
                } else {
                    Toast.makeText(context, "Cannot select future date", Toast.LENGTH_SHORT).show()
                }*/
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            val fragment = HomeBottomTabFragment()
            val args = Bundle()
            args.putString("ModuleName", "EatRight")
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "landing")
                addToBackStack("landing")
                commit()
            }
        }
    }

    private fun fetchWaterIntakeData(period: String) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                if (isAdded  && view != null){
                    requireActivity().runOnUiThread {
                        showLoader(requireView())
                    }
                }
                val userId = SharedPreferenceManager.getInstance(requireContext()).userId // Replace with dynamic user ID if needed
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
//                        val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
//                        selectedDate = firstDateOfMonth
                        selectedMonthDate = selectedDate
                    } else {
                        //val firstDateOfMonth = getFirstDateOfMonth(selectedMonthDate, 1)
                        selectedDate = selectedMonthDate
                    }
                    setSelectedDateMonth(selectedMonthDate, "Month")
                } else {
                    if (selectedHalfYearlyDate.isEmpty()) {
                        selectedDate = currentDateTime.format(formatter)
//                        val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
//                        selectedDate = firstDateOfMonth
                        selectedHalfYearlyDate = selectedDate
                    } else {
                      //  val firstDateOfMonth = getFirstDateOfMonth(selectedMonthDate, 1)
                        selectedDate = selectedHalfYearlyDate
                    }
                    setSelectedDateMonth(selectedHalfYearlyDate, "Year")
                }
                val response = ApiClient.apiServiceFastApi.getWaterIntake(
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
                    val waterData = response.body()
                    waterData?.let { data ->
                        val (entries, labels, labelsDate) = when (period) {
                            "last_weekly" -> processWeeklyData(data, selectedDate)
                            "last_monthly" -> processMonthlyData(data, selectedDate)
                            "last_six_months" -> processSixMonthsData(data, selectedDate)
                            else -> Triple(emptyList(), emptyList(), emptyList())
                        }
                        withContext(Dispatchers.Main) {
                            waterQuantityTv.text =data.todaysWaterLog.totalWater.toString()
                            val waterIntake = data.todaysWaterLog.totalWater.toFloat()
                            waterIntakeValue = waterIntake
                            val waterGoal = data.todaysWaterLog.goal.toFloat()
                           // glassWithWaterView.setTargetWaterLevel(waterIntake, waterGoal)
                            val glassValue =   milestoneFor(waterIntake.toInt())
                            when (glassValue) {
                                25 -> {
                                    glassWithWaterView.setImageResource(R.drawable.glass_image_25)
                                }
                                50 -> {
                                    glassWithWaterView.setImageResource(R.drawable.glass_image_50)
                                }
                                75 -> {
                                    glassWithWaterView.setImageResource(R.drawable.glass_image_75)
                                }
                                100 -> {
                                    glassWithWaterView.setImageResource(R.drawable.glass_image_100)
                                }
                                else -> {
                                    glassWithWaterView.setImageResource(R.drawable.glass_image_0)
                                }
                            }
                            hydration_description_heading.text = markdownToBold(data.heading)
                            hydration_description_text.text = markdownToBold(data.description)
                            if (data.waterIntakeTotals.size > 31) {
                                layoutLineChart.visibility = View.VISIBLE
                                lineChartForSixMonths()
                            } else {
                                layoutLineChart.visibility = View.VISIBLE
                                updateChart(entries, labels, labelsDate, data)
                            }
                        }
                    } ?: withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No water intake data received", Toast.LENGTH_SHORT).show()
                        if (isAdded  && view != null){
                            requireActivity().runOnUiThread {
                                dismissLoader(requireView())
                            }
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            requireContext(),
                            "Error: ${response.code()} - ${response.message()}",
                            Toast.LENGTH_SHORT
                        ).show()
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

    private fun milestoneFor(value: Int): Int {
        val max = 5000
        val thresholds = listOf(
            0,                // 0%
            (max * 0.25).toInt(),  // 25%
            (max * 0.50).toInt(),  // 50%
            (max * 0.75).toInt(),  // 75%
            max               // 100%
        )
        return when {
            value <= thresholds[0] ->   0
            value <  thresholds[1] ->  25
            value <  thresholds[2] ->  50
            value <  thresholds[3] ->  75
            else                    -> 100
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
            if (viewModel.currentRange == RangeTypeCharts.SIX_MONTHS) {
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

    private fun updateChart(entries: List<Entry>, labels: List<String>, labelsDate: List<String>, data: WaterIntakeResponse) {
        selectHeartRateLayout.visibility = View.INVISIBLE
        val dataSet = LineDataSet(entries, "")
        dataSet.color = context?.let { ContextCompat.getColor(it, R.color.border_green) }!!
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.setCircleColor(context?.let { ContextCompat.getColor(it, R.color.border_green) }!!)
        dataSet.circleRadius = 5f
        dataSet.lineWidth = 2f
        dataSet.setDrawValues(entries.size <= 7)

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // X-axis label handling
        val combinedLabels = if (entries.size == 30) {
            labels
        } else {
            labels.take(entries.size).zip(labelsDate.take(entries.size)) { label, date ->
                val cleanedDate = date.substringBefore(",")
                "$label\n$cleanedDate"
            }
        }

        val xAxis = lineChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(combinedLabels)
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 10f
        xAxis.granularity = 1f
        xAxis.labelCount =  entries.size
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

        // Custom XAxis Renderer (multiline support)
        val customRenderer = MultilineXAxisRenderer(
            lineChart.viewPortHandler,
            lineChart.xAxis,
            lineChart.getTransformer(YAxis.AxisDependency.LEFT)
        )
        (lineChart as BarLineChartBase<*>).setXAxisRenderer(customRenderer)

        // Y-axis
        val leftYAxis: YAxis = lineChart.axisLeft
        leftYAxis.textSize = 12f
        leftYAxis.textColor = ContextCompat.getColor(requireContext(), R.color.black_no_meals)
        leftYAxis.setDrawGridLines(true)
        leftYAxis.axisMinimum = 0f
        leftYAxis.axisMaximum = entries.maxByOrNull { it.y }?.y?.plus(100f) ?: 1000f
        leftYAxis.granularity = 1f

        if (entries.size < 30){
            val minValue = minOf(
                entries.minOfOrNull { it.y } ?: 0f,
                data.todaysWaterLog.goal.toFloat(),
                data.currentAvgWater.toFloat()
            )
            val maxValue = maxOf(
                entries.maxOfOrNull { it.y } ?: 0f,
                data.todaysWaterLog.goal.toFloat(),
                data.currentAvgWater.toFloat()
            )

            // Include stepsGoal in max check
            val axisMax = maxOf(maxValue, data.todaysWaterLog.goal.toFloat())

            leftYAxis.axisMinimum = if (minValue < 0) minValue * 1.2f else 0f
            leftYAxis.axisMaximum = axisMax * 1.2f
            leftYAxis.setDrawZeroLine(true)
            // leftYAxis.zeroLineColor = Color.BLACK
            leftYAxis.zeroLineWidth = 1f

            val totalStepsLine = LimitLine(data.todaysWaterLog.goal.toFloat(), "G")
            totalStepsLine.lineColor = ContextCompat.getColor(requireContext(), R.color.border_green)
            totalStepsLine.lineWidth = 1f
            totalStepsLine.enableDashedLine(10f, 10f, 0f)
            totalStepsLine.textColor = ContextCompat.getColor(requireContext(), R.color.border_green)
            totalStepsLine.textSize = 10f
            totalStepsLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            lineChart.rendererLeftYAxis = BadgeLimitLineRenderer(
                lineChart.viewPortHandler,
                lineChart.axisLeft,
                lineChart.getTransformer(YAxis.AxisDependency.LEFT)
            )

            leftYAxis.removeAllLimitLines()
            leftYAxis.addLimitLine(totalStepsLine)
            averageGoalLayout.visibility = View.VISIBLE
        }else{
            leftYAxis.removeAllLimitLines()
            averageGoalLayout.visibility = View.GONE
        }

        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.setScaleEnabled(false)
        lineChart.isDoubleTapToZoomEnabled = false
        lineChart.isHighlightPerTapEnabled = true
        lineChart.isHighlightPerDragEnabled = false
        // Optional chart description
        val description = Description().apply {
            text = ""
            textColor = Color.BLACK
            textSize = 14f
            setPosition(lineChart.width / 2f, lineChart.height.toFloat() - 10f)
        }
        lineChart.description = description
        lineChart.setExtraOffsets(0f, 0f, 0f, 25f)
        // Legend
        val legend = lineChart.legend
        legend.setDrawInside(false)
        legend.isEnabled = false

        // Chart selection listener
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                selectHeartRateLayout.visibility = View.VISIBLE
                if (e != null) {
                    val x = e.x.toInt()
                    val y = e.y
                    Log.d("ChartClick", "Clicked X: $x, Y: $y")
                    selectedItemDate.text = labelsDate.get(x)
                    selectedCalorieTv.text = y.toInt().toString()
                }
            }
            override fun onNothingSelected() {
                Log.d("ChartClick", "Nothing selected")
                selectHeartRateLayout.visibility = View.INVISIBLE
            }
        })
        lineChart.animateY(1000)
        lineChart.invalidate()
    }

    private suspend fun processWeeklyData(data: WaterIntakeResponse, currentDate: String): Triple<List<Entry>, List<String>, List<String>> {
        return withContext(Dispatchers.Main) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = currentDate
            val date = dateFormat.parse(currentDate)
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -6)

            val waterMap = mutableMapOf<String, Float>()
            val labels = mutableListOf<String>()
            val labelsDate = mutableListOf<String>()
            val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())

            repeat(7) {
                val dateStr = dateFormat.format(calendar.time)
                waterMap[dateStr] = 0f
                val dateLabel = "${convertDate(dateStr)}, $year"
                val dayString = dayFormat.format(calendar.time)
                labels.add(dayString)
                labelsDate.add(dateLabel)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }
            if (data.waterIntakeTotals.isNotEmpty()) {
                data.waterIntakeTotals.forEach { intake ->
                    val startDate = dateFormat.parse(intake.date)?.let { Date(it.time) }
                    if (startDate != null) {
                        val dayKey = dateFormat.format(startDate)
                        waterMap[dayKey] = waterMap[dayKey]!! + (intake.waterMl.toFloat() ?: 0f)
                    }
                }
            }
            setLastAverageValue(data, "% Past Week")
            val entries = waterMap.values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
            Triple(entries, labels, labelsDate)
        }
    }

    private suspend fun processMonthlyData(data: WaterIntakeResponse, currentDate: String): Triple<List<Entry>, List<String>, List<String>> {
        return withContext(Dispatchers.Main) {
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val calendar = Calendar.getInstance()
            val dateString = currentDate
            val date = dateFormat.parse(currentDate)
            calendar.time = date!!
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)
            calendar.set(year, month, day)
            calendar.add(Calendar.DAY_OF_YEAR, -29)

            val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
            val waterMap = mutableMapOf<String, Float>()
            val dateList = mutableListOf<String>()
            val weeklyLabels = mutableListOf<String>()
            val labelsDate = mutableListOf<String>()

            // Initialize water map for all days in the month
            repeat(30) {
                val dateStr = dateFormat.format(calendar.time)
                waterMap[dateStr] = 0f
                dateList.add(dateStr)
                calendar.add(Calendar.DAY_OF_YEAR, 1)
            }

            val labelsWithEmpty = generateLabeled30DayListWithEmpty(dateList[0])
            val labels = generateWeeklyLabelsFor30Days(dateList[0])
            weeklyLabels.addAll(labelsWithEmpty)
            labelsDate.addAll(labels)

            if (data.waterIntakeTotals.isNotEmpty()) {
                data.waterIntakeTotals.forEach { intake ->
                    val startDate = dateFormat.parse(intake.date)?.let { Date(it.time) }
                    if (startDate != null) {
                        val dayKey = dateFormat.format(startDate)
                        waterMap[dayKey] = waterMap[dayKey]!! + (intake.waterMl.toFloat() ?: 0f)
                    }
                }
            }
            setLastAverageValue(data, "% Past Month")
            // Create entries for the chart
            val entries = waterMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
            Triple(entries, weeklyLabels, labelsDate)
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

    private fun showWaterIntakeBottomSheet() {
        val waterIntakeBottomSheet = WaterIntakeBottomSheet()
        waterIntakeBottomSheet.isCancelable = false
        waterIntakeBottomSheet.listener = object : OnWaterIntakeConfirmedListener {
            override fun onWaterIntakeConfirmed(amount: Int) {
                // 👇 Use the amount here
                //Toast.makeText(requireContext(), "Water Intake: $amount ml", Toast.LENGTH_SHORT).show()
                /*tv_water_quantity.text = amount.toString()
                val waterIntake = amount.toFloat()
                val waterGoal = 3000f
                glassWithWaterView.setTargetWaterLevel(waterIntake, waterGoal)*/
                // You can now call API or update UI etc.
                waterQuantityTv.text = amount.toString()
                val waterIntake = amount.toFloat()
                val waterGoal = 5000f
                waterIntakeValue = waterIntake
                //  glassWithWaterView.setTargetWaterLevel(waterIntake, waterGoal)
                val glassValue =   milestoneFor(waterIntakeValue.toInt())
                when (glassValue) {
                    25 -> {
                        glassWithWaterView.setImageResource(R.drawable.glass_image_25)
                    }
                    50 -> {
                        glassWithWaterView.setImageResource(R.drawable.glass_image_50)
                    }
                    75 -> {
                        glassWithWaterView.setImageResource(R.drawable.glass_image_75)
                    }
                    100 -> {
                        glassWithWaterView.setImageResource(R.drawable.glass_image_100)
                    }
                    else -> {
                        glassWithWaterView.setImageResource(R.drawable.glass_image_0)
                    }
                }
            }
        }
        val args = Bundle()
        args.putInt("waterIntakeValue", waterIntakeValue.toInt())
        waterIntakeBottomSheet.arguments = args
        waterIntakeBottomSheet.show(parentFragmentManager, WaterIntakeBottomSheet.TAG)
    }

    private fun processSixMonthsData(data: WaterIntakeResponse, currentDate: String): Triple<List<Entry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 24)
        calendar.add(Calendar.MONTH, -5)

        val waterMap = mutableMapOf<Int, Float>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()

        repeat(6) { month ->
            waterMap[month] = 0f
            labels.add(monthFormat.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
        }

        data.waterIntakeTotals.forEach { intake ->
            val date = dateFormat.parse(intake.date)
            if (date != null) {
                calendar.time = date
                val monthDiff = ((2025 - 1900) * 12 + Calendar.MARCH) - ((calendar.get(Calendar.YEAR) - 1900) * 12 + calendar.get(Calendar.MONTH))
                val monthIndex = 5 - monthDiff
                if (monthIndex in 0..5) {
                    waterMap[monthIndex] = waterMap[monthIndex]!! + intake.waterMl.toFloat()
                }
            }
        }

        val entries = waterMap.values.mapIndexed { index, value -> Entry(index.toFloat(), value) }
        return Triple(entries, labels, labelsDate)
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

    private fun setLastAverageValue(data: WaterIntakeResponse, type: String) {
        activity?.runOnUiThread {
            averageWaterIntake.text = data.currentAvgWater.toInt().toString()
           // totalCalorie.text = data.totalWater.toInt().toString()
            if (data.progressSign == "plus") {
                percentageTv.text = "${data.progressPercentage.toInt()} $type"
                // percentageIc.setImageResource(R.drawable.ic_up)
            } else if (data.progressSign == "minus") {
                percentageTv.text = "${data.progressPercentage.toInt()} $type"
                // percentageIc.setImageResource(R.drawable.ic_down)
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

    fun showLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.VISIBLE
    }
    fun dismissLoader(view: View) {
        loadingOverlay = view.findViewById(R.id.loading_overlay)
        loadingOverlay?.visibility = View.GONE
    }

    fun markdownToBold(text: String): Spanned {
        val htmlText = text
            .replace("\n", "<br>")
            .replace(Regex("\\*\\*(.*?)\\*\\*"), "<b>$1</b>")
        return Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
    }
}

class HydrationViewModel : ViewModel() {
    val allDailyWater = mutableListOf<DailyWater>()
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
            val randomWater = (1000..3000).random()
            allDailyWater.add(DailyWater(current, randomWater))
            calendar.time = current
            calendar.add(Calendar.DAY_OF_YEAR, 1)
            current = calendar.time
        }
        Log.d("HydrationViewModel", "Generated ${allDailyWater.size} days of data from ${formatDate(start, "d MMM yyyy")} to ${formatDate(end, "d MMM yyyy")}")
    }

    val filteredData: List<DailyWater>
        get() = allDailyWater.filter { it.date >= startDate && it.date <= endDate }

    val monthlyGroups: List<MonthGroups>
        get() = if (currentRange != RangeTypeCharts.SIX_MONTHS || filteredData.isEmpty()) emptyList() else {
            val calendar = Calendar.getInstance()
            val grouped = filteredData.groupBy {
                calendar.time = it.date
                Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH))
            }
            val sorted = grouped.toList().sortedWith(compareBy({ it.first.first }, { it.first.second }))
            var results = mutableListOf<MonthGroups>()
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

data class DailyWater(val date: Date, val waterMl: Int)
enum class RangeTypeCharts { WEEK, MONTH, SIX_MONTHS }
data class MonthGroups(
    val days: List<DailyWater>,
    val avgWater: Int,
    val monthDiffString: String,
    val isPositiveChange: Boolean,
    val startDate: Date,
    val endDate: Date
)