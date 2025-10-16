package com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.SleepIdealActualResponse
import com.jetsynthesys.rightlife.databinding.FragmentIdealActualSleepTimeBinding
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.google.android.material.snackbar.Snackbar
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.formatter.ValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.RestorativeSleepFragment.MultilineXAxisRenderer
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Calendar
import java.util.Locale
import kotlin.math.floor

class SleepIdealActualFragment : BaseFragment<FragmentIdealActualSleepTimeBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentIdealActualSleepTimeBinding
        get() = FragmentIdealActualSleepTimeBinding::inflate
    var snackbar: Snackbar? = null
    private lateinit var sixMonthGraph: SixMonthGraphView
    private lateinit var lineChart:LineChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var progressDialog: ProgressDialog
    private lateinit var idealActualResponse: SleepIdealActualResponse
    private var currentTab = 0 // 0 = Week, 1 = Month, 2 = 6 Months
    private lateinit var btnPrevious: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var dateRangeText: TextView
    private lateinit var percentage_text_average: TextView
    private lateinit var percentage_text: TextView
    private lateinit var tvAverageSleep: TextView
    private lateinit var tvAverageNeeded: TextView
    private lateinit var tvIdealTitle: TextView
    private lateinit var sleep_actual_time_box: ConstraintLayout
    private lateinit var tvIdealMessage: TextView
    private lateinit var ideal_extra_text: TextView
    private lateinit var tv_ideal_actual_date: TextView
    private lateinit var tv_ideal_time: TextView
    private lateinit var tv_actual_time: TextView
    private lateinit var sleepCard: CardView
    private lateinit var sleepNoCard: CardView
    private var currentDateWeek: LocalDate = LocalDate.now() // today
    private var currentDateMonth: LocalDate = LocalDate.now() // today
    private var mStartDate = ""
    private var mEndDate = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.sleep_stages_bg)

        lineChart = view.findViewById(R.id.idealActualChart)
        radioGroup = view.findViewById(R.id.tabGroup)
        sleepCard = view.findViewById(R.id.sleep_ideal_card)
        sleepNoCard = view.findViewById(R.id.sleep_ideal_nocard)
        dateRangeText = view.findViewById(R.id.tv_selected_date)
        sixMonthGraph = view.findViewById(R.id.sixMonthGraph)
        btnPrevious = view.findViewById(R.id.btn_prev)
        btnNext = view.findViewById(R.id.btn_next)
        percentage_text_average = view.findViewById(R.id.percentage_text_average)
        percentage_text = view.findViewById(R.id.percentage_text)
        sleep_actual_time_box = view.findViewById(R.id.sleep_actual_time_box)
        tv_ideal_actual_date = view.findViewById(R.id.tv_ideal_actual_date)
        tv_ideal_time = view.findViewById(R.id.tv_ideal_time)
        tv_actual_time = view.findViewById(R.id.tv_actual_time)
        tvAverageSleep = view.findViewById(R.id.tv_average_sleep_time)
        tvAverageNeeded = view.findViewById(R.id.tv_average_needed_time)
        tvIdealTitle = view.findViewById(R.id.ideal_title)
        tvIdealMessage = view.findViewById(R.id.ideal_message)
        ideal_extra_text = view.findViewById(R.id.ideal_extra_text)
        val backBtn = view.findViewById<ImageView>(R.id.img_back)
        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Loading")
        progressDialog.setCancelable(false)
        radioGroup.check(R.id.rbWeek)
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        mStartDate = getOneWeekEarlierDate().format(dateFormatter)
        mEndDate = getTodayDate().format(dateFormatter)
        val endOfWeek = currentDateWeek
        val startOfWeek = endOfWeek.minusDays(7)

        val formatter = DateTimeFormatter.ofPattern("d MMM")
        dateRangeText.text = "${startOfWeek.format(formatter)} - ${endOfWeek.format(formatter)}, ${currentDateWeek.year}"
        setupListeners()
        fetchSleepData(mEndDate,"weekly")
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
                    currentTab = 0
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val startDate = getOneWeekEarlierDate().format(dateFormatter)
                    val endDate = getTodayDate().format(dateFormatter)
                    val formatter = DateTimeFormatter.ofPattern("d MMM")
                    dateRangeText.text = "${getOneWeekEarlierDate().format(formatter)} - ${getTodayDate().format(formatter)}, ${currentDateWeek.year}"
                    fetchSleepData(endDate, "weekly")
                }
                R.id.rbMonth -> {
                    currentTab = 1
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val startDate = getOneMonthEarlierDate().format(dateFormatter)
                    val endDate = getTodayDate().format(dateFormatter)
                    val formatter = DateTimeFormatter.ofPattern("d MMM")
                    dateRangeText.text = "${getOneMonthEarlierDate().format(formatter)} - ${getTodayDate().format(formatter)}, ${currentDateMonth.year}"
                    fetchSleepData(endDate, "monthly")
                }
                R.id.rbSixMonths -> {
                    currentTab = 2
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val startDate = getSixMonthsEarlierDate().format(dateFormatter)
                    val endDate = getTodayDate().format(dateFormatter)
                    val formatter = DateTimeFormatter.ofPattern("d MMM")
                    dateRangeText.text = "${getSixMonthsEarlierDate().format(formatter)} - ${getTodayDate().format(formatter)}, ${currentDateMonth.year}"
                    fetchSleepData(endDate, "monthly")
                }
            }
        }

        backBtn.setOnClickListener {
            val fragment = HomeBottomTabFragment()
            val args = Bundle().apply {
                putString("ModuleName", "SleepRight")
            }
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                addToBackStack(null)
                commit()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = HomeBottomTabFragment()
                val args = Bundle().apply {
                    putString("ModuleName", "SleepRight")
                }
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                    addToBackStack(null)
                    commit()
                }

            }
        })
    }

    fun getTodayDate(): LocalDate {
        return LocalDate.now()
    }

    fun getOneWeekEarlierDate(): LocalDate {
        return LocalDate.now().minusWeeks(1)
    }
    fun getOneMonthEarlierDate(): LocalDate {
        return LocalDate.now().minusMonths(1).minusDays(1)
    }
    fun getSixMonthsEarlierDate(): LocalDate {
        return LocalDate.now().minusMonths(6)
    }

    private fun setupListeners() {
      /*  radioGroup.setOnCheckedChangeListener { _, checkedId ->

        }*/

        btnPrevious.setOnClickListener {
            when (currentTab) {
                0 -> {
                    currentDateWeek = currentDateWeek.minusWeeks(1)
                    loadWeekData()
                }
                1 -> {
                    currentDateMonth = currentDateMonth.minusMonths(1)
                    loadMonthData()
                }
                2 -> {
                    currentDateMonth = currentDateMonth.minusMonths(6)
                    loadSixMonthsData()
                }
            }
        }

        btnNext.setOnClickListener {
            when (currentTab) {
                0 -> {
                    if (currentDateWeek < LocalDate.now()) {
                        currentDateWeek = currentDateWeek.plusWeeks(1)
                        loadWeekData()
                    }
                }
                1 -> {
                    if (currentDateMonth < LocalDate.now()) {
                        currentDateMonth = currentDateMonth.plusMonths(1)
                        loadMonthData()
                    }
                }
                2 -> {
                    currentDateMonth = currentDateMonth.plusMonths(6)
                    loadSixMonthsData()
                }
            }
        }
    }

    private fun loadSixMonthsData() {
        val startDate = getSixMonthsEarlierDate()
        val endDate = getTodayDate()

        val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
        dateRangeText.text = "${startDate.format(formatter)} - ${endDate.format(formatter)}"

        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        fetchSleepData(endDate.format(formatter1), "monthly")
    }



    private fun loadMonthData() {
        val endOfMonth = currentDateMonth
        val startOfMonth = endOfMonth.minusMonths(1)
        val formatter = DateTimeFormatter.ofPattern("d MMM")
        dateRangeText.text = "${startOfMonth.format(formatter)} - ${endOfMonth.format(formatter)}, ${currentDateMonth.year}"

        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        fetchSleepData(endOfMonth.format(formatter1), "monthly")

     //   val weekRanges = listOf("1", "2", "3", "4", "5","6", "7", "8", "9", "10","11", "12", "13", "14", "15","16", "17", "18", "19", "20","21", "22", "23", "24", "25","26", "27", "28", "29", "30")

   //     setGraphDataFromSleepList(monthList,weekRanges)
    }

    private fun loadWeekData() {

        val endOfWeek = currentDateWeek
        val startOfWeek = endOfWeek.minusDays(6)

        val formatter = DateTimeFormatter.ofPattern("d MMM")
        dateRangeText.text = "${startOfWeek.format(formatter)} - ${endOfWeek.format(formatter)}, ${currentDateWeek.year}"

        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        fetchSleepData(endOfWeek.format(formatter1), "weekly")

      //  val weekRanges = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

      //  setGraphDataFromSleepList(weekList, weekRanges)
    }

    fun getOneMonthBack(dateString: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(dateString, formatter)
        val oneMonthBack = date.minusMonths(1)
        return oneMonthBack.format(formatter)
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

    private fun setGraphDataFromSleepList(sleepData: List<SleepGraphData>?, period: String, endDate: String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        var startDateStr = ""
        if (period != "weekly"){
            startDateStr = getOneMonthBack(endDate)
        }

        var startDate = LocalDate.parse("2025-05-30")
        val mEndDate = LocalDate.parse(endDate, formatter)
        if (startDateStr != ""){
            startDate = LocalDate.parse(startDateStr, formatter)
        }
        val idealEntries = ArrayList<Entry>()
        val actualEntries = ArrayList<Entry>()
        val labels = mutableListOf<String>()


        sleepData?.forEachIndexed { index, data ->
            idealEntries.add(Entry(index.toFloat(), data.idealSleep))
            actualEntries.add(Entry(index.toFloat(), data.actualSleep))
        }


        if (sleepData?.size!! > 8 ) {
            labels.addAll(generateLabeled30DayListWithEmpty(startDateStr))
            /*val daysBetween = ChronoUnit.DAYS.between(startDate, mEndDate).toInt() + 1
            val entries = ArrayList<BarEntry>()
           // val labels = ArrayList<String>()

            val monthFormatter = DateTimeFormatter.ofPattern("MMM") // For 'Jun', 'Feb', etc.

            for (i in 0 until daysBetween) {
                val currentDate = startDate.plusDays(i.toLong())

                // Calculate group index (each group is 7 days long)
                val groupIndex = i / 7
                val groupStartDate = startDate.plusDays(groupIndex * 7L)
                val groupEndDate = groupStartDate.plusDays(6).coerceAtMost(mEndDate)

                // Label for the group (shown only once per 7-day group)
                val label = if (i % 7 == 0) {
                    val dayRange = "${groupStartDate.dayOfMonth}–${groupEndDate.dayOfMonth}"
                    val month = groupEndDate.format(monthFormatter)

                    // Center month by adding padding spaces (rough estimation)
                    val spaces = " ".repeat((dayRange.length - month.length).coerceAtLeast(0) / 2)
                    "$dayRange\n$spaces$month"
                } else {
                    ""
                }

                labels.add(label)*/
            val idealLineSet = LineDataSet(idealEntries, "Ideal").apply {
                color = Color.parseColor("#00C853") // green
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 2f
                isHighlightEnabled = true
            }

            // Only last point - Ideal
            val idealLastPoint = idealEntries.lastOrNull()
            val idealLastPointSet = LineDataSet(listOfNotNull(idealLastPoint), "").apply {
                color = Color.parseColor("#00C853")
                setCircleColor(color)
                circleRadius = 5f
                setDrawCircles(true)
                setDrawValues(false)
                setDrawHighlightIndicators(false)
                lineWidth = 0f
                isHighlightEnabled = true
            }

            // Line without circles - Actual
            val actualLineSet = LineDataSet(actualEntries, "Actual").apply {
                color = Color.parseColor("#2979FF") // blue
                setDrawCircles(false)
                setDrawValues(false)
                lineWidth = 2f
                isHighlightEnabled = true
            }

            // Only last point - Actual
            val actualLastPoint = actualEntries.lastOrNull()
            val actualLastPointSet = LineDataSet(listOfNotNull(actualLastPoint), "").apply {
                color = Color.parseColor("#2979FF")
                setCircleColor(color)
                circleRadius = 5f
                setDrawCircles(true)
                setDrawValues(false)
                setDrawHighlightIndicators(false)
                lineWidth = 0f
                isHighlightEnabled = true
            }
            lineChart.data = LineData(idealLineSet, actualLineSet, idealLastPointSet, actualLastPointSet)
        }else{
            sleepData?.forEachIndexed { index, (label, durations) ->
                val date = LocalDate.parse(label, DateTimeFormatter.ISO_LOCAL_DATE)
                val top = date.format(DateTimeFormatter.ofPattern("EEE"))      // e.g., "Fri"
                val bottom = date.format(DateTimeFormatter.ofPattern("d MMM"))
                labels.add("$top\n$bottom")
            }
            val idealLineSet = LineDataSet(idealEntries, "Ideal").apply {
                color = Color.parseColor("#00C853") // green
                setDrawCircles(true)
                setDrawValues(false)
                lineWidth = 2f
                isHighlightEnabled = true
            }

            // Only last point - Ideal
            val idealLastPoint = idealEntries.lastOrNull()
            val idealLastPointSet = LineDataSet(listOfNotNull(idealLastPoint), "").apply {
                color = Color.parseColor("#00C853")
                setCircleColor(color)
                circleRadius = 5f
                setDrawCircles(true)
                setDrawValues(false)
                setDrawHighlightIndicators(false)
                lineWidth = 0f
                isHighlightEnabled = true
            }

            // Line without circles - Actual
            val actualLineSet = LineDataSet(actualEntries, "Actual").apply {
                color = Color.parseColor("#2979FF") // blue
                setDrawCircles(true)
                setDrawValues(false)
                lineWidth = 2f
                isHighlightEnabled = true
            }

            // Only last point - Actual
            val actualLastPoint = actualEntries.lastOrNull()
            val actualLastPointSet = LineDataSet(listOfNotNull(actualLastPoint), "").apply {
                color = Color.parseColor("#2979FF")
                setCircleColor(color)
                circleRadius = 5f
                setDrawCircles(true)
                setDrawValues(false)
                setDrawHighlightIndicators(false)
                lineWidth = 0f
                isHighlightEnabled = true
            }
            lineChart.data = LineData(idealLineSet, actualLineSet, idealLastPointSet, actualLastPointSet)

        }

        /*lineChart.xAxis.apply {
            valueFormatter = object : ValueFormatter() {
                override fun getFormattedValue(value: Float): String {
                    val index = value.toInt()
                    return if (index in labels.indices) labels[index] else ""
                }
            }
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            labelRotationAngle = 0f
            textSize = 10f
        }*/
        lineChart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            labelCount = labels.size
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            labelRotationAngle = -30f
            textSize = 10f
        }

        lineChart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 20f
            granularity = 1f
            textSize = 12f
        }

        lineChart.axisRight.isEnabled = false
        lineChart.description.isEnabled = false
        lineChart.legend.textSize = 12f
        lineChart.setExtraBottomOffset(24f)
        lineChart.setPinchZoom(false)
        lineChart.setDrawGridBackground(false)
        lineChart.setScaleEnabled(false) // disables pinch and double-tap zoom
        lineChart.isDoubleTapToZoomEnabled = false // disables zoom on double tap

        //lineChart.isDragEnabled = false
        //lineChart.isHighlightPerTapEnabled = false
        lineChart.isDragEnabled = true // Enable drag for better interaction
        lineChart.isHighlightPerTapEnabled = true // Enable touch highlighting
        lineChart.setTouchEnabled(true) // Explicitly enable touch
        lineChart.setXAxisRenderer(
            MultilineXAxisRenderer(
                lineChart.viewPortHandler,
                lineChart.xAxis,
                lineChart.getTransformer(YAxis.AxisDependency.LEFT)
            )
        )
        lineChart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                Log.d("TouchEvent", "onValueSelected triggered, e=$e, h=$h")

                if (e != null && h != null) {
                    val xIndex = e.x.toInt()
                    val idealValue = idealEntries.getOrNull(xIndex)?.y ?: 0f
                    val actualValue = actualEntries.getOrNull(xIndex)?.y ?: 0f
                    val (idealHours, idealMinutes) = idealValue.toHoursAndMinutes()
                    val (actualHours, actualMinutes) = actualValue.toHoursAndMinutes()
                    Log.d("TouchEvent", "Selected: xIndex=$xIndex, idealValue=$idealValue, actualValue=$actualValue")
                    sleep_actual_time_box.visibility = View.VISIBLE
                    tv_ideal_time.text = String.format("%d hr %d mins", idealHours, idealMinutes)
                    tv_actual_time.text = convertDecimalHoursToHrMinFormat(actualEntries.getOrNull(xIndex)?.y?.toDouble()!!) //String.format("%d hr %d mins", actualHours, actualMinutes)
                   // Log.d("TouchEvent", "Setting tvIdealTime to ${tvIdealTime.text}, tvActualTime to ${tvActualTime.text}")

                    // Update tv_ideal_actual_date with formatted date
                    if (xIndex in labels.indices) {
                        val fullDate = LocalDate.parse(sleepData[xIndex].date, DateTimeFormatter.ISO_LOCAL_DATE)
                        val formattedDate = fullDate.format(DateTimeFormatter.ofPattern("EEEE dd MMMM, yyyy"))
                        tv_ideal_actual_date.text = formattedDate
                        Log.d("TouchEvent", "Setting tv_ideal_actual_date to $formattedDate")
                    }
                } else {
                    Log.d("TouchEvent", "Entry or Highlight is null")
                }
            }

            override fun onNothingSelected() {
                sleep_actual_time_box.visibility = View.GONE
               /* tvActualTime.text = ""
                tvIdealTime.text = ""
                tv_ideal_actual_date.text = ""*/
                Log.d("TouchEvent", "Nothing selected, cleared TextViews")
            }
        })

        lineChart.invalidate()
    }

    private fun convertDecimalHoursToHrMinFormat(hoursDecimal: Double): String {
        var result = "0"
        if (hoursDecimal == 0.0 || hoursDecimal == null){
            val totalMinutes = (hoursDecimal * 60).toInt()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            result = "0 mins"
        }else {
            // val totalMinutes = (hoursDecimal * 60).toInt()
            val minutesDecimal = hoursDecimal * 60
            val totalMinutes = if (minutesDecimal - floor(minutesDecimal) > 0.5) {
                floor(minutesDecimal).toInt() + 1
            } else {
                floor(minutesDecimal).toInt()
            }
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            result = String.format("%02dhr %02dmins", hours, minutes)
        }
        return result
    }
    private fun Float.toHoursAndMinutes(): Pair<Int, Int> {
        val totalMinutes = (this * 60).toInt() // Convert to minutes
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return Pair(hours, minutes)
    }

        private fun fetchSleepData(endDate: String,period: String) {
            progressDialog.show()
            //val userid = "685a482b5e75643139c79905"
            val userid = SharedPreferenceManager.getInstance(requireActivity()).userId
            val source = "android"
            val call = ApiClient.apiServiceFastApi.fetchSleepIdealActual(userid, source, period,endDate)
            call.enqueue(object : Callback<SleepIdealActualResponse> {
                override fun onResponse(call: Call<SleepIdealActualResponse>, response: Response<SleepIdealActualResponse>) {
                    if (response.isSuccessful) {
                        progressDialog.dismiss()
                        if (response.body()!=null) {
                            idealActualResponse = response.body()!!
                            if (idealActualResponse.message != "No sleep time data retrieved successfully") {
                                tvIdealTitle.visibility = View.VISIBLE
                                tvIdealMessage.visibility = View.VISIBLE
                                ideal_extra_text.visibility = View.VISIBLE
                                sleepCard.visibility = View.VISIBLE
                                sleepNoCard.visibility = View.GONE
                                percentage_text.text = "${response.body()!!.data?.progress_detail?.actual_sleep?.progress_percentage?.toInt().toString()}% of past week"
                                percentage_text_average.text = "${response.body()!!.data?.progress_detail?.needed_sleep?.progress_percentage?.toInt().toString()}% of past week"
                                setSleepRightData(period,endDate)
                            }else{
                                sleepCard.visibility = View.GONE
                                sleepNoCard.visibility = View.VISIBLE
                                tvIdealTitle.visibility = View.GONE
                                ideal_extra_text.visibility = View.GONE
                                tvIdealMessage.visibility = View.GONE
                            }
                        }
                    }else if(response.code() == 400){
                        progressDialog.dismiss()
                        sleepCard.visibility = View.GONE
                        sleepNoCard.visibility = View.VISIBLE
                        tvIdealTitle.visibility = View.GONE
                        ideal_extra_text.visibility = View.GONE
                        tvIdealMessage.visibility = View.GONE
                        Toast.makeText(activity, "Record Not Found", Toast.LENGTH_SHORT).show()
                    }else {
                        Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                        Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                        progressDialog.dismiss()
                    }
                }

                override fun onFailure(call: Call<SleepIdealActualResponse>, t: Throwable) {
                    Log.e("Error", "API call failed: ${t.message}")
                    Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            })
        }

    private fun setupChart(chart: LineChart, newData: List<SleepGraphData>) {
        val idealEntries = ArrayList<Entry>()
        val actualEntries = ArrayList<Entry>()

        for ((index, item) in newData.withIndex()) {
            idealEntries.add(Entry(index.toFloat(), item.idealSleep))
            actualEntries.add(Entry(index.toFloat(), item.actualSleep))
        }

        val idealSet = LineDataSet(idealEntries, "Ideal Sleep").apply {
            color = Color.GREEN
            setCircleColor(Color.GREEN)
            valueTextSize = 10f
        }

        val actualSet = LineDataSet(actualEntries, "Actual Sleep").apply {
            color = Color.BLUE
            setCircleColor(Color.BLUE)
            valueTextSize = 10f
        }

        val lineData = LineData(idealSet, actualSet) // ✅ Convert to LineData

        chart.apply {
            data = lineData // ✅ Assign LineData to Chart
            description.isEnabled = false
            xAxis.setDrawGridLines(false)
            legend.verticalAlignment = Legend.LegendVerticalAlignment.TOP
            legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            invalidate() // Refresh chart
        }
    }

    private fun formatDate(dateTime: String): String {
      //  val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
     //   val outputFormat = SimpleDateFormat("EEE dd MMM", Locale.getDefault())
        return dateTime
    }

    private fun setSleepRightData(period: String, endDate: String) {
        tvIdealTitle.setText(idealActualResponse.data?.sleepInsightDetail?.title)
        ideal_extra_text.setText(idealActualResponse.data?.sleepInsightDetail?.action)
        tvIdealMessage.setText(idealActualResponse.data?.sleepInsightDetail?.message)
        if (idealActualResponse.data?.averageSleep!=null && idealActualResponse.data?.averageNeeded!=null) {
            tvAverageSleep.setText(convertDecimalHoursToHrMinFormat(idealActualResponse.data?.averageSleep!!))
            tvAverageNeeded.setText(convertDecimalHoursToHrMinFormat(idealActualResponse.data?.averageNeeded!!))
        }
        var sleepDataList: List<SleepGraphData>? = arrayListOf()
        if (idealActualResponse.data?.timeDataBreakdown?.isNotEmpty() == true) {
            sleepDataList = idealActualResponse.data?.timeDataBreakdown?.map { detail ->
                val formattedDate = detail.date?.let { formatDate(it) }
                return@map formattedDate?.let {
                    detail.idealSleepHours?.toFloat()?.let { it1 ->
                        detail.actualSleepHours?.toFloat()?.let { it2 ->
                            SleepGraphData(date = it, idealSleep = it1, actualSleep = it2)
                        }
                    }
                }!!
            }
        }
        if (sleepDataList?.isNotEmpty() == true) {
            if (sleepDataList.size < 9 == true) {
                val weekRanges = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
                setGraphDataFromSleepList(sleepDataList, period,endDate)
            }else{
                val monthlyRanges = listOf("1", "2", "3", "4", "5","6", "7", "8", "9", "10","11", "12", "13", "14", "15","16", "17", "18", "19", "20","21", "22", "23", "24", "25","26", "27", "28", "29", "30")
                setGraphDataFromSleepList(sleepDataList, period, endDate)
            }
        }
    }

    /*private fun convertDecimalHoursToHrMinFormat(hoursDecimal: Double): String {
        val totalMinutes = (hoursDecimal * 60).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return String.format("%02dhr %02dmins", hours, minutes)
    }*/


    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }
}

class SixMonthGraphView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    var data: List<SleepSummary> = listOf()

    private val axisPaint = Paint().apply {
        color = Color.LTGRAY
        strokeWidth = 2f
    }

    private val labelPaint = Paint().apply {
        color = Color.DKGRAY
        textSize = 28f
        textAlign = Paint.Align.CENTER
    }

    private val boldLabelPaint = Paint(labelPaint).apply {
        isFakeBoldText = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (data.isEmpty()) return

        val paddingLeft = 100f
        val paddingRight = 50f
        val paddingTop = 50f
        val paddingBottom = 100f

        val contentWidth = width - paddingLeft - paddingRight
        val contentHeight = height - paddingTop - paddingBottom

        val maxY = 14f
        val minY = 2f
        val scaleY = contentHeight / (maxY - minY)

        val barSpacing = contentWidth / data.size
        val barWidth = barSpacing * 0.5f

        // Draw horizontal grid and Y-axis labels
        for (i in 2..14 step 2) {
            val y = height - paddingBottom - (i - minY) * scaleY
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, axisPaint)
            canvas.drawText("$i", paddingLeft - 30f, y + 10f, labelPaint)
        }

        // Draw bars and data
        data.forEachIndexed { index, item ->
            val centerX = paddingLeft + index * barSpacing + barSpacing / 2

            // Ideal shaded range
            val idealTop = height - paddingBottom - (item.idealRange.second - minY) * scaleY
            val idealBottom = height - paddingBottom - (item.idealRange.first - minY) * scaleY
            val idealRect = RectF(centerX - barWidth / 2, idealTop, centerX + barWidth / 2, idealBottom)
            canvas.drawRect(idealRect, Paint().apply {
                color = Color.parseColor("#AA00C853") // green
                style = Paint.Style.FILL
            })

            // Actual shaded range
            val actualTop = height - paddingBottom - (item.actualRange.second - minY) * scaleY
            val actualBottom = height - paddingBottom - (item.actualRange.first - minY) * scaleY
            val actualRect = RectF(centerX - barWidth / 2, actualTop, centerX + barWidth / 2, actualBottom)
            canvas.drawRect(actualRect, Paint().apply {
                color = Color.parseColor("#AA2962FF") // blue
                style = Paint.Style.FILL
            })

            // Ideal average text
            canvas.drawText(
                "%.2f".format(item.idealAverage),
                centerX,
                idealTop - 10,
                boldLabelPaint
            )

            // Actual average text
            canvas.drawText(
                "%.2f".format(item.actualAverage),
                centerX,
                actualBottom + 30,
                boldLabelPaint
            )

            // Percentage change
            val pctColor = if (item.percentageChange >= 0) Color.parseColor("#4CAF50") else Color.RED
            canvas.drawText(
                "%+.2f%%".format(item.percentageChange),
                centerX,
                actualBottom + 60,
                Paint(labelPaint).apply { color = pctColor }
            )

            // X-axis label (week)
            canvas.drawText(
                item.weekLabel,
                centerX,
                height - 30f,
                labelPaint
            )
        }

        val idealPath = Path()
        val actualPath = Path()

        data.forEachIndexed { index, item ->
            val centerX = paddingLeft + index * barSpacing + barSpacing / 2
            val idealY = height - paddingBottom - (item.idealAverage - minY) * scaleY
            val actualY = height - paddingBottom - (item.actualAverage - minY) * scaleY

            if (index == 0) {
                idealPath.moveTo(centerX, idealY)
                actualPath.moveTo(centerX, actualY)
            } else {
                idealPath.lineTo(centerX, idealY)
                actualPath.lineTo(centerX, actualY)
            }
        }

// Draw ideal average line (green)
        val idealLinePaint = Paint().apply {
            color = Color.parseColor("#00C853")
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawPath(idealPath, idealLinePaint)

// Draw actual average line (blue)
        val actualLinePaint = Paint().apply {
            color = Color.parseColor("#2962FF")
            strokeWidth = 4f
            style = Paint.Style.STROKE
            isAntiAlias = true
        }
        canvas.drawPath(actualPath, actualLinePaint)

        // Draw Y axis
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, axisPaint)
    }
}

data class SleepGraphData(val date: String, val idealSleep: Float, val actualSleep: Float)
data class SleepSummary(
    val weekLabel: String,
    val idealAverage: Float,
    val actualAverage: Float,
    val idealRange: Pair<Float, Float>,  // min to max for shaded area
    val actualRange: Pair<Float, Float>,
    val percentageChange: Float          // vs previous week
)