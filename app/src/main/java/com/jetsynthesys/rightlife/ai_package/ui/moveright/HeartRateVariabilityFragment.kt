package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.graphics.Color
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
import com.github.mikephil.charting.charts.BarLineChartBase
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.databinding.FragmentHeartRateVariabilityBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Description
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.jetsynthesys.rightlife.ai_package.model.HeartRateVariabilityResponse
import com.jetsynthesys.rightlife.ai_package.model.RestingHeartRateResponse
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.RestorativeSleepFragment
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

class HeartRateVariabilityFragment : BaseFragment<FragmentHeartRateVariabilityBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentHeartRateVariabilityBinding
        get() = FragmentHeartRateVariabilityBinding::inflate

    private lateinit var lineChart: LineChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var heart_rate_variability_back_image:ImageView
    private lateinit var selectedDate : TextView
    private lateinit var selectedItemDate : TextView
    private lateinit var selectHeartRateLayout : CardView
    private lateinit var selectedCalorieTv : TextView
    private lateinit var averageBurnCalorie : TextView
    private lateinit var averageHeading : TextView
    private lateinit var percentageTv : TextView
    private lateinit var percentageIc : ImageView
    private lateinit var backwardImage : ImageView
    private lateinit var forwardImage : ImageView
    private lateinit var heartRateDescriptionHeading : TextView
    private lateinit var heartRateDescription : TextView
    private var selectedWeekDate : String = ""
    private var selectedMonthDate : String = ""
    private var selectedHalfYearlyDate : String = ""
    private var loadingOverlay : FrameLayout? = null

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
        selectedCalorieTv = view.findViewById(R.id.selectedCalorieTv)
        percentageTv = view.findViewById(R.id.percentage_text)
        averageBurnCalorie = view.findViewById(R.id.average_number)
        averageHeading = view.findViewById(R.id.average_text_heading)
        percentageIc = view.findViewById(R.id.percentage_icon)
        heartRateDescriptionHeading = view.findViewById(R.id.heartRateDescriptionHeading)
        heartRateDescription = view.findViewById(R.id.heartRateDescription)
        heart_rate_variability_back_image = view.findViewById(R.id.heart_rate_variability_back_image)
        heart_rate_variability_back_image.setOnClickListener {
            val fragment = HomeBottomTabFragment()
            val args = Bundle().apply {
                putString("ModuleName", "MoveRight")
            }
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                addToBackStack(null)
                commit()
            }        }
        // Show Week data by default
       // updateChart(getWeekData(), getWeekLabels())
        radioGroup.check(R.id.rbWeek)
        fetchHeartRateVariability("last_weekly") // Fetch data on load

        // Set default selection to Week


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
                fetchHeartRateVariability("last_weekly")
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
                fetchHeartRateVariability("last_monthly")
            }else{
                selectedHalfYearlyDate = ""
                fetchHeartRateVariability("last_six_months")
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
                    fetchHeartRateVariability("last_weekly")
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
                    fetchHeartRateVariability("last_monthly")
                }else{
                    Toast.makeText(context, "Not selected future date", Toast.LENGTH_SHORT).show()
                }
            }else{
                if (!selectedHalfYearlyDate.contentEquals(currentDate)){
                    selectedHalfYearlyDate = ""
                    fetchHeartRateVariability("last_six_months")
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

    /** Update chart with new data and X-axis labels */
    private fun updateChart(entries: List<Entry>, labels: List<String>, labelsDate: List<String>) {
        val dataSet = LineDataSet(entries, "Heart Rate Variability (bpm)")
        dataSet.color = ContextCompat.getColor(requireContext(), R.color.moveright)
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f
        dataSet.setCircleColor(Color.RED)
        dataSet.circleRadius = 5f
        dataSet.lineWidth = 2f
        dataSet.setDrawValues(entries.size <= 7)

        val lineData = LineData(dataSet)
        lineChart.data = lineData
        val combinedLabels = labels.zip(labelsDate) { label, date ->
            val cleanedDate = date.substringBefore(",") // removes ,2025
            "$label\n$cleanedDate"
        }
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(combinedLabels)
        lineChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        lineChart.xAxis.textSize = 12f
        lineChart.xAxis.granularity = 1f
        lineChart.xAxis.setDrawGridLines(false)
        lineChart.xAxis.textColor = Color.BLACK
        lineChart.xAxis.yOffset = 15f
        val description = Description().apply {
            text = "Heart Rate Variability (bpm)"
            textColor = Color.BLACK
            textSize = 14f
            setPosition(lineChart.width / 2f, lineChart.height.toFloat() - 10f)
        }
        lineChart.description = description
        lineChart.setExtraOffsets(0f, 0f, 0f, 25f)

        // ⛏️ Custom XAxisRenderer set
        val customRenderer = RestorativeSleepFragment.MultilineXAxisRenderer(
            lineChart.viewPortHandler,
            lineChart.xAxis,
            lineChart.getTransformer(YAxis.AxisDependency.LEFT)
        )
        (lineChart as BarLineChartBase<*>).setXAxisRenderer(customRenderer)

        // Y-axis customization
        val leftYAxis: YAxis = lineChart.axisLeft
        leftYAxis.textSize = 12f
        leftYAxis.textColor = Color.BLACK
        leftYAxis.setDrawGridLines(true)
        leftYAxis.axisMinimum = 0f // Set minimum to 0 to avoid negative values
        leftYAxis.axisMaximum = 150f // Optional: Max for realistic heart rate range
        leftYAxis.granularity = 10f // Optional: Cleaner labels (0, 10, 20, ...)

        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false

        // Chart click listener
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
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

    /** Fetch and update chart with API data */
    private fun fetchHeartRateVariability(period: String) {
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
                        // val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
                        // selectedDate = firstDateOfMonth
                        selectedMonthDate = selectedDate
                    }else{
                        // val firstDateOfMonth = getFirstDateOfMonth(selectedMonthDate, 1)
                        selectedDate = selectedMonthDate
                    }
                    setSelectedDateMonth(selectedMonthDate, "Month")
                }else{
                    if (selectedHalfYearlyDate.contentEquals("")){
                        selectedDate = currentDateTime.format(formatter)
                        //  val firstDateOfMonth = getFirstDateOfMonth(selectedDate, 1)
                        //selectedDate = firstDateOfMonth
                        selectedHalfYearlyDate = selectedDate
                    }else{
                        //   val firstDateOfMonth = getFirstDateOfMonth(selectedMonthDate, 1)
                        selectedDate = selectedHalfYearlyDate
                    }
                    setSelectedDateMonth(selectedHalfYearlyDate, "Year")
                }
                val response = ApiClient.apiServiceFastApi.getHeartRateVariability(
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
                    val heartRateVariability = response.body()
                    heartRateVariability?.let { data ->
                        val (entries, labels, labelsDate) = when (period) {
                            "last_weekly" -> processWeeklyData(data, selectedDate)
                            "last_monthly" -> processMonthlyData(data, selectedDate)
                            "last_six_months" -> processSixMonthsData(data)
                            else -> Triple(getWeekData(), getWeekLabels(), getWeekLabelsDate()) // Fallback
                        }
                        val averageHrv = data.heartRateVariability
                            .mapNotNull { it.hrv.toDouble() }
                            .average()
                        withContext(Dispatchers.Main) {
                            updateChart(entries, labels, labelsDate)
                            /*Toast.makeText(
                                requireContext(),
                                "Average HRV: ${String.format("%.2f", averageHrv)} ms",
                                Toast.LENGTH_LONG
                            ).show()*/
                        }
                    } ?: withContext(Dispatchers.Main) {
                        Toast.makeText(requireContext(), "No HRV data received", Toast.LENGTH_SHORT).show()
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

    /** Process API data for last_weekly (7 days) */
    private fun processWeeklyData(data: HeartRateVariabilityResponse, currentDate: String):
            Triple<List<Entry>, List<String>, List<String>> {
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

        val hrvMap = mutableMapOf<String, Float>()
        val labels = mutableListOf<String>()
        val dayFormat = SimpleDateFormat("EEE", Locale.getDefault()) // e.g., "Mon"
        val labelsDate = mutableListOf<String>()

        // Initialize 7 days
        repeat(7) {
            val dateStr = dateFormat.format(calendar.time)
            hrvMap[dateStr] = 0f
            val dateLabel = (convertDate(dateStr) + "," + year)
            val dayString = (dayFormat.format(calendar.time))
            labels.add(dayString)
            labelsDate.add(dateLabel)
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        // Aggregate RHR by day
        if (data.heartRateVariability.isNotEmpty()){
            data.heartRateVariability.forEach { rhr ->
                val startDate = dateFormat.parse(rhr.date)?.let { Date(it.time) }
                if (startDate != null) {
                    val dayKey = dateFormat.format(startDate)
                    hrvMap[dayKey] = hrvMap[dayKey]!! + (rhr.hrv.toFloat() ?: 0f)
                }
            }
        }
        // Average RHR per day
        setLastAverageValue(data,  "% Past Week")
        val entries = hrvMap.values.mapIndexed { index, values ->
            Entry(index.toFloat(), values)
        }
        return Triple(entries, labels, labelsDate)
    }
    /** Process API data for last_six_months (6 months) */
    private fun processMonthlyData(restingHeartRateResponse: HeartRateVariabilityResponse, currentDate: String
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
        val dateStrLabel = dateFormat.format(calendar.time)
        val hrvMap = mutableMapOf<String, Float>()
        val weeklyLabels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()

        val days = getDaysInMonth(month+1, year)
        repeat(30) {
            val dateStr = dateFormat.format(calendar.time)
            hrvMap[dateStr] = 0f
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }
        for (i in 0 until 30) {
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
            val dateLabel = (convertMonth(dateStrLabel.toString()) + "," + year)
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
        }
        // Aggregate calories by week
        if ( restingHeartRateResponse.heartRateVariability.isNotEmpty()){
            restingHeartRateResponse.heartRateVariability.forEach { calorie ->
                val startDate = dateFormat.parse(calorie.date)?.let { Date(it.time) }
                if (startDate != null) {
                    val dayKey = dateFormat.format(startDate)
                    hrvMap[dayKey] = hrvMap[dayKey]!! + (calorie.hrv.toFloat() ?: 0f)
                }
            }
        }
        setLastAverageValue(restingHeartRateResponse, "% Past Month")
        val entries = hrvMap.values.mapIndexed { index, value -> BarEntry(index.toFloat(), value) }
        return Triple(entries, weeklyLabels, labelsDate)
    }
    private fun processSixMonthsData(data: HeartRateVariabilityResponse): Triple<List<Entry>, List<String>, List<String>> {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
        val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
        val calendar = Calendar.getInstance()
        calendar.set(2025, Calendar.MARCH, 24)
        calendar.add(Calendar.MONTH, -5) // Start 6 months back (Oct 2024)

        val hrvMap = mutableMapOf<Int, MutableList<Float>>()
        val labels = mutableListOf<String>()
        val labelsDate = mutableListOf<String>()

        // Initialize 6 months
        repeat(6) { month ->
            hrvMap[month] = mutableListOf()
            labels.add(monthFormat.format(calendar.time))
            calendar.add(Calendar.MONTH, 1)
        }

        // Aggregate HRV by month
        data.heartRateVariability.forEach { hrv ->
            val startDate = dateFormat.parse(hrv.date)?.let { Date(it.time) }
            if (startDate != null) {
                calendar.time = startDate
                val monthDiff = ((2025 - 1900) * 12 + Calendar.MARCH) - ((calendar.get(Calendar.YEAR) - 1900) * 12 + calendar.get(Calendar.MONTH))
                val monthIndex = 5 - monthDiff // Reverse to align with labels
                if (monthIndex in 0..5) {
                    hrvMap[monthIndex]?.add(hrv.hrv.toFloat() ?: 0f)
                }
            }
        }

        // Average HRV per month
        val entries = hrvMap.values.mapIndexed { index, values ->
            val average = if (values.isNotEmpty()) values.average().toFloat() else 0f
            Entry(index.toFloat(), average)
        }
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

    private fun setLastAverageValue(restingHeartRateResponse: HeartRateVariabilityResponse, type: String) {
        activity?.runOnUiThread {
            heartRateDescriptionHeading.text = restingHeartRateResponse.heading
            heartRateDescription.text = restingHeartRateResponse.description
            averageBurnCalorie.text = restingHeartRateResponse.currentAvgHrv.toInt().toString()
            if (restingHeartRateResponse.progressSign.contentEquals("plus")){
                percentageTv.text = (restingHeartRateResponse.progressPercentage.toInt().toString() + type)
                percentageIc.setImageResource(R.drawable.ic_up)
            }else if (restingHeartRateResponse.progressSign.contentEquals("minus")){
                percentageTv.text = (restingHeartRateResponse.progressPercentage.toInt().toString() + type)
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
}