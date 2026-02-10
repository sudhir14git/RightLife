package com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment

import android.app.ProgressDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.github.mikephil.charting.animation.ChartAnimator
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.databinding.FragmentSleepPerformanceBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
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
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import com.google.android.material.snackbar.Snackbar
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.SleepPerformanceAllData
import com.jetsynthesys.rightlife.ai_package.model.SleepPerformanceList
import com.jetsynthesys.rightlife.ai_package.model.SleepPerformanceResponse
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.RestorativeSleepFragment.MultilineXAxisRenderer
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.fragment.MindfullChartRenderer
import com.jetsynthesys.rightlife.ai_package.utils.BadgeLimitLineRenderer
import com.jetsynthesys.rightlife.ui.utility.AnalyticsEvent
import com.jetsynthesys.rightlife.ui.utility.AnalyticsLogger
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.pow
import kotlin.math.round

class SleepPerformanceFragment : BaseFragment<FragmentSleepPerformanceBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSleepPerformanceBinding
        get() = FragmentSleepPerformanceBinding::inflate
    var snackbar: Snackbar? = null

    private lateinit var btnPrevious: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var dateRangeText: TextView
    private lateinit var tvSleepAverage: TextView
    private lateinit var performTitle: TextView
    private lateinit var performMessage: TextView
    private lateinit var performSubtitle: TextView
    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var layoutLineChart: FrameLayout
    private lateinit var stripsContainer: FrameLayout
    private lateinit var lytPerformCard: CardView
    private lateinit var lytPerformNoCard: CardView
    private lateinit var cardPercent: CardView
    private lateinit var tvBarPercent: TextView
    private lateinit var tvBarDate: TextView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sleepPerformanceResponse: SleepPerformanceResponse
    private lateinit var percentageIcon: ImageView
    private lateinit var percentageText: TextView
    private lateinit var averageGoalLayout : LinearLayoutCompat
    private var currentTab = 0 // 0 = Week, 1 = Month, 2 = 6 Months
    private var currentDateWeek: LocalDate = LocalDate.now() // today
    private var currentDateMonth: LocalDate = LocalDate.now() // today
    private var mStartDate = ""
    private var mEndDate = ""

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundResource(R.drawable.sleep_stages_bg)

        barChart = view.findViewById(R.id.heartRateChart)
        layoutLineChart = view.findViewById(R.id.lyt_line_chart)
        stripsContainer = view.findViewById(R.id.stripsContainer)
        lineChart = view.findViewById(R.id.heartLineChart)
        radioGroup = view.findViewById(R.id.tabGroup)
        btnPrevious = view.findViewById(R.id.btn_prev)
        averageGoalLayout = view.findViewById(R.id.averageGoalLayout)
        btnNext = view.findViewById(R.id.btn_next)
        cardPercent = view.findViewById(R.id.card_percent)
        tvBarPercent = view.findViewById(R.id.tv_bar_percent)
        tvBarDate = view.findViewById(R.id.tv_bar_date)
        dateRangeText = view.findViewById(R.id.tv_selected_date)
        tvSleepAverage = view.findViewById(R.id.tv_sleep_perform_average)
        performTitle = view.findViewById(R.id.perform_title)
        performMessage = view.findViewById(R.id.perform_message)
        performSubtitle = view.findViewById(R.id.perform_subTitle)
        lytPerformCard = view.findViewById(R.id.sleep_perform_card)
        lytPerformNoCard = view.findViewById(R.id.sleep_perform_nocard)
        percentageIcon = view.findViewById(R.id.percentage_icon)
        percentageText = view.findViewById(R.id.percentage_text)
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
        fetchSleepData(mEndDate,"weekly")

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

        val backBtn = view.findViewById<ImageView>(R.id.img_back)

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

        context?.let { it1 ->
            AnalyticsLogger.logEvent(
                it1, AnalyticsEvent.SR_SleepPerformance_PageOpen
            )
        }
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

    private fun loadWeekData() {
        val endOfWeek = currentDateWeek
        val startOfWeek = endOfWeek.minusDays(6)
        val formatter = DateTimeFormatter.ofPattern("d MMM")
        dateRangeText.text = "${startOfWeek.format(formatter)} - ${endOfWeek.format(formatter)}, ${currentDateWeek.year}"
        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fetchSleepData(endOfWeek.format(formatter1), "weekly")
    }

    private fun loadMonthData() {
        val endOfMonth = currentDateMonth
        val startOfMonth = endOfMonth.minusMonths(1)
        val formatter = DateTimeFormatter.ofPattern("d MMM")
        dateRangeText.text = "${startOfMonth.format(formatter)} - ${endOfMonth.format(formatter)}, ${currentDateMonth.year}"
        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fetchSleepData(endOfMonth.format(formatter1), "monthly")
      //  val weekRanges = listOf("1", "2", "3", "4", "5","6", "7", "8", "9", "10","11", "12", "13", "14", "15","16", "17", "18", "19", "20","21", "22", "23", "24", "25","26", "27", "28", "29", "30")
     //   updateChart(entries, weekRanges)
    }

    private fun loadSixMonthsData() {
        val startDate = getSixMonthsEarlierDate()
        val endDate = getTodayDate()
        val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
        dateRangeText.text = "${startDate.format(formatter)} - ${endDate.format(formatter)}"
        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fetchSleepData(endDate.format(formatter1), "monthly")
    }

    private fun updateChart(entries: List<BarEntry>, labels: List<String>) {
        val dataSet = BarDataSet(entries, "Performance")
        dataSet.color = Color.parseColor("#87CEFA")
        barChart.data = BarData(dataSet)
        barChart.xAxis.valueFormatter = object : ValueFormatter() {
            override fun getFormattedValue(value: Float): String {
                return if (value.toInt() in labels.indices) labels[value.toInt()] else ""
            }
        }

        barChart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        barChart.axisLeft.axisMinimum = 0f
        barChart.axisLeft.axisMaximum = 100f
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.invalidate()
    }

    private fun fetchSleepData(endDate: String, period: String) {
        progressDialog.show()
        val userid = SharedPreferenceManager.getInstance(requireActivity()).userId
        val source = "android"
        val call = ApiClient.apiServiceFastApi.fetchSleepPerformance(userid, source, period,endDate)
        call.enqueue(object : Callback<SleepPerformanceResponse> {
            override fun onResponse(call: Call<SleepPerformanceResponse>, response: Response<SleepPerformanceResponse>) {
                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    sleepPerformanceResponse = response.body()!!
                    if (sleepPerformanceResponse.message!="No sleep performance data found.") {
                        lytPerformNoCard.visibility = View.GONE
                        lytPerformCard.visibility = View.VISIBLE
                        performTitle.visibility = View.VISIBLE
                        performSubtitle.visibility = View.VISIBLE
                        performMessage.visibility = View.VISIBLE
                        setSleepRightPerformanceData(sleepPerformanceResponse.sleepPerformanceAllData, period)
                    }else{
                        lytPerformNoCard.visibility = View.VISIBLE
                        lytPerformCard.visibility = View.GONE
                        performTitle.visibility = View.GONE
                        performSubtitle.visibility = View.GONE
                        performMessage.visibility = View.GONE
                    }
                }else if(response.code() == 400){
                    progressDialog.dismiss()
                    Toast.makeText(activity, "Record Not Found", Toast.LENGTH_SHORT).show()
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
            override fun onFailure(call: Call<SleepPerformanceResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }

    private fun setSleepRightPerformanceData(
        sleepPerformanceResponse: SleepPerformanceAllData?,
        period: String
    ) {
        tvSleepAverage.setText(""+sleepPerformanceResponse?.sleepPerformanceAverage?.roundToDecimals(2)+"%")
        performTitle.setText(sleepPerformanceResponse?.sleepInsightDetail?.title)
        performSubtitle.setText(sleepPerformanceResponse?.sleepInsightDetail?.subtitle)
        performMessage.setText(sleepPerformanceResponse?.sleepInsightDetail?.message)
      //  updateChart()
        if (sleepPerformanceResponse?.sleepPerformanceList?.isNotEmpty() == true) {
            if (sleepPerformanceResponse.sleepPerformanceList.size < 9 ) {
                averageGoalLayout.visibility = View.VISIBLE
                setupWeeklyBarChart(barChart, sleepPerformanceResponse.sleepPerformanceList, sleepPerformanceResponse.endDatetime!!)
            }else{
                averageGoalLayout.visibility = View.GONE
                setupMonthlyBarChart(barChart, sleepPerformanceResponse.sleepPerformanceList, sleepPerformanceResponse.startDatetime!!,sleepPerformanceResponse.endDatetime!!)
            }
        }
        var labelText = "Week"
        if (period != "weekly"){
            labelText = "Month"
        }
        if (sleepPerformanceResponse?.progress_detail?.progress_sign == "plus"){
          //  percentageIcon.visibility = View.VISIBLE
            percentageIcon.setImageResource(R.drawable.ic_up)
           // percentageText.visibility = View.VISIBLE
            percentageText.text = " "+ sleepPerformanceResponse?.progress_detail?.progress_percentage + "% Past " + labelText
        }else{
        //    percentageIcon.visibility = View.VISIBLE
        //    percentageText.visibility = View.VISIBLE
            percentageIcon.setImageResource(R.drawable.ic_down)
            percentageText.text = " "+ sleepPerformanceResponse?.progress_detail?.progress_percentage + "% Past " + labelText
        }
    }

    fun Double.roundToDecimals(decimals: Int): Double {
        val factor = 10.0.pow(decimals)
        return round(this * factor) / factor
    }

    fun getOneMonthBack(dateString: String): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val date = LocalDate.parse(dateString, formatter)
        val oneMonthBack = date.minusMonths(1)
        return oneMonthBack.format(formatter)
    }

    fun convertToLocalDate(input: String): LocalDate {
        val offsetDateTime = OffsetDateTime.parse(input)
        return offsetDateTime
            .atZoneSameInstant(ZoneId.systemDefault()) // Convert to local zone
            .toLocalDate()                             // Extract local date
    }

    fun getDateLabels(
        startDate: LocalDate,                // already a LocalDate
        days: Int = 30,
        locale: Locale = Locale.getDefault() // use Locale.ENGLISH for fixed English names
    ): List<String> {
        val formatter = DateTimeFormatter.ofPattern("dd MMM, yyyy", locale)
        return List(days) { offset ->
            startDate.plusDays(offset.toLong()).format(formatter)
        }
    }

    fun setupMonthlyBarChart(chart: BarChart, data: List<SleepPerformanceList>?, startTime:String, endDateStr:String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        var startDate =convertToLocalDate(startTime)
        val mEndDate1 =convertToLocalDate(endDateStr)
        var selectedEntry: SleepEntry? = null
        val labels = mutableListOf<String>()
        val monthLabel = getDateLabels(startDate,32)
        val defaultColor   = Color.parseColor("#C5DEF9")
        val highlightColor = Color.parseColor("#0B84FF")
        val daysBetween = ChronoUnit.DAYS.between(startDate, mEndDate1).toInt() + 1
        val entries = ArrayList<BarEntry>()
        // val labels = ArrayList<String>()
        val monthFormatter = DateTimeFormatter.ofPattern("MMM") // For 'Jun', 'Feb', etc.

        for (i in 0 until daysBetween) {
            val currentDate = startDate.plusDays(i.toLong())
            // Calculate group index (each group is 7 days long)
            val groupIndex = i / 7
            val groupStartDate = startDate.plusDays(groupIndex * 7L)
            val groupEndDate = groupStartDate.plusDays(6).coerceAtMost(mEndDate1)
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
            labels.add(label)
        }
        data?.forEachIndexed { index, item ->
            entries.add(BarEntry(index.toFloat(), item.sleepPerformanceData?.sleepPerformance?.toFloat() ?: 0f))
        }

        val dataSet = BarDataSet(entries, "Sleep Performance").apply {
            setColors(List(entries.size) { defaultColor })      // one color per bar
            setDrawValues(false)
            valueTextSize = 9f
            highLightAlpha = 0                                  // hide the grey overlay
        }

        val barData = BarData(dataSet)
        barData.barWidth = 0.4f
        chart.data = barData
        val customRenderer = MindfullChartRenderer(chart, chart.animator, chart.viewPortHandler)
        customRenderer.initBuffers()
        chart.renderer = customRenderer
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            labelCount = labels.size
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            labelRotationAngle = -30f
            textSize = 10f
        }

        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = 100f
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.isHighlightPerTapEnabled = true
        chart.isHighlightPerDragEnabled = false
        chart.legend.isEnabled = false

        chart.setVisibleXRangeMaximum(labels.size.toFloat()) // Show all bars
        chart.setFitBars(true)
        chart.setDrawValueAboveBar(false)
        barChart.rendererLeftYAxis = BadgeLimitLineRenderer(
            barChart.viewPortHandler,
            barChart.axisLeft,
            barChart.getTransformer(YAxis.AxisDependency.LEFT)
        )
        cardPercent.visibility = View.VISIBLE
        tvBarDate.text = monthLabel.getOrNull(entries.size-1) ?: ""
        tvBarPercent.text = entries.get(entries.size-1).y.toInt().toString()
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                if (e != null) {
                    val index = h?.x?.toInt() ?: return
                    val x = e.x.toInt()
                    val y = e.y
                    Log.d("ChartClick", "Clicked X: $x, Y: $y")
                    val newColors = MutableList(entries.size) { defaultColor }
                    newColors[index] = highlightColor
                    dataSet.colors = newColors

                    chart.invalidate()
                    tvBarDate.text = monthLabel.getOrNull(x) ?: ""
                    tvBarPercent.text = y.toInt().toString()
                }
            }
            override fun onNothingSelected() {
                Log.d("ChartClick", "Nothing selected")
                cardPercent.visibility = View.INVISIBLE
                dataSet.colors = List(entries.size) { defaultColor }
                chart.invalidate()
            }
        })
        chart.invalidate()
    }

    fun getWeekDayNames(endOfWeek: LocalDate): List<String> {
        val labels = mutableListOf<String>()
          //  val date = LocalDate.parse(label, DateTimeFormatter.ISO_LOCAL_DATE)
           // val top = date.format(DateTimeFormatter.ofPattern("EEE"))      // e.g., "Fri"
         //   val bottom = date.format(DateTimeFormatter.ofPattern("d MMM"))
        //    labels.add("$top\n$bottom")
        val startOfWeek = endOfWeek.minusDays(6)
        for (i in 0..6) {
            val top = startOfWeek.plusDays(i.toLong()).format(DateTimeFormatter.ofPattern("EEE"))      // e.g., "Fri"
            val bottom = startOfWeek.plusDays(i.toLong()).format(DateTimeFormatter.ofPattern("d MMM"))
            labels.add("$top\n$bottom")
        }
        return labels
    }

    fun getSlectedWeekDayNames(endOfWeek: LocalDate): List<String> {
        val labels = mutableListOf<String>()
        //  val date = LocalDate.parse(label, DateTimeFormatter.ISO_LOCAL_DATE)
        // val top = date.format(DateTimeFormatter.ofPattern("EEE"))      // e.g., "Fri"
        //   val bottom = date.format(DateTimeFormatter.ofPattern("d MMM"))
        //    labels.add("$top\n$bottom")
        val startOfWeek = endOfWeek.minusDays(6)
        for (i in 0..6) {
           // val top = startOfWeek.plusDays(i.toLong()).format(DateTimeFormatter.ofPattern("EEE"))      // e.g., "Fri"
            val bottom = startOfWeek.plusDays(i.toLong()).format(DateTimeFormatter.ofPattern("d MMM, yyyy"))
            labels.add(bottom)
        }
        return labels
    }

    fun setupWeeklyBarChart(chart: BarChart, data: List<SleepPerformanceList>?, endDate: String) {
        val entries = ArrayList<BarEntry>()
        val trimmedDate = endDate.substring(0, 10)  // "2025-05-01"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val localDate = LocalDate.parse(trimmedDate, formatter)
        val labels = getWeekDayNames(localDate)
        val selectedDate = getSlectedWeekDayNames(localDate)
        val defaultColor   = Color.parseColor("#C5DEF9")
        val highlightColor = Color.parseColor("#0B84FF")
        data?.forEachIndexed { index, item ->
            entries.add(BarEntry(index.toFloat(), item.sleepPerformanceData?.sleepPerformance?.toFloat() ?: 0f))
        }
        val dataSet = BarDataSet(entries, "Sleep Performance").apply {
            setColors(List(entries.size) { defaultColor })      // one color per bar
            setDrawValues(false)
            valueTextSize = 11f
            highLightAlpha = 0                                  // hide the grey overlay
        }
        val barData = BarData(dataSet)
        chart.data = barData
        chart.setFitBars(true)
        val customRenderer = RoundedBarChartRenderer(barChart, barChart.animator, barChart.viewPortHandler)
        customRenderer.initBuffers()
        chart.renderer = customRenderer
        val leftYAxis: YAxis = barChart.axisLeft
        if (entries.size < 30){
            val minValue = minOf(
                entries.minOfOrNull { it.y } ?: 0f,
              //  sleepPerformanceResponse.sleepPerformanceAllData?.sleepPerformanceAverage?.toFloat(),
                sleepPerformanceResponse.sleepPerformanceAllData?.sleepPerformanceAverage!!.toFloat()
            )
            val maxValue = maxOf(
                entries.maxOfOrNull { it.y } ?: 0f,
                //activeCaloriesResponse.goal.toFloat(),
                sleepPerformanceResponse.sleepPerformanceAllData?.sleepPerformanceAverage!!.toFloat()
            )
            // Include stepsGoal in max check
            val axisMax = maxOf(maxValue, sleepPerformanceResponse.sleepPerformanceAllData?.sleepPerformanceAverage!!.toFloat())
            val avgStepsLine = LimitLine(sleepPerformanceResponse.sleepPerformanceAllData?.sleepPerformanceAverage!!.toFloat(), "A")
            avgStepsLine.lineColor = ContextCompat.getColor(requireContext(), R.color.text_color_kcal)
            avgStepsLine.lineWidth = 1f
            avgStepsLine.enableDashedLine(10f, 10f, 0f)
            avgStepsLine.textColor = ContextCompat.getColor(requireContext(), R.color.white)
            avgStepsLine.textSize = 9f
            avgStepsLine.labelPosition = LimitLine.LimitLabelPosition.RIGHT_TOP
            leftYAxis.removeAllLimitLines()
            leftYAxis.addLimitLine(avgStepsLine)
            averageGoalLayout.visibility = View.VISIBLE
        }else{
            leftYAxis.removeAllLimitLines()
            averageGoalLayout.visibility = View.GONE
        }
        chart.xAxis.apply {
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
        }

        chart.setScaleEnabled(false)
        chart.isDoubleTapToZoomEnabled = false
        chart.axisLeft.axisMinimum = 0f
        chart.axisLeft.axisMaximum = 100f
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.isHighlightPerTapEnabled = true
        chart.isHighlightPerDragEnabled = false

        chart.setExtraBottomOffset(24f)
        chart.legend.isEnabled = false
        chart.setScaleEnabled(false)
        chart.setXAxisRenderer(
            MultilineXAxisRenderer(
                chart.viewPortHandler,
                chart.xAxis,
                chart.getTransformer(YAxis.AxisDependency.LEFT)
            )
        )
        chart.post {
            chart.rendererLeftYAxis = BadgeLimitLineRenderer(
                chart.viewPortHandler,
                chart.axisLeft,
                chart.getTransformer(YAxis.AxisDependency.LEFT)
            )
            chart.invalidate()
        }

        cardPercent.visibility = View.VISIBLE
        tvBarDate.text = selectedDate.getOrNull(entries.size-1) ?: ""
        tvBarPercent.text = entries.get(entries.size-1).y.toInt().toString()
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                cardPercent.visibility = View.VISIBLE
                if (e != null) {
                    val index = h?.x?.toInt() ?: return
                    val x = e.x.toInt()
                    val y = e.y
                    Log.d("ChartClick", "Clicked X: $x, Y: $y")
                    val newColors = MutableList(entries.size) { defaultColor }
                    newColors[index] = highlightColor
                    dataSet.colors = newColors
                    chart.invalidate()
                    tvBarDate.text = selectedDate.getOrNull(x)?.replace("\n", " ") ?: ""
                    tvBarPercent.text = y.toInt().toString()
                }
            }
            override fun onNothingSelected() {
                Log.d("ChartClick", "Nothing selected")
                cardPercent.visibility = View.INVISIBLE
                dataSet.colors = List(entries.size) { defaultColor }
                chart.invalidate()
            }
        })
        chart.invalidate()
    }

    private fun lineChartForSixMonths(){
        val entries = listOf(
            Entry(0f, 72f), // Jan
            Entry(1f, 58f), // Feb
            Entry(2f, 68f), // Mar
            Entry(3f, 86f), // Apr
            Entry(4f, 72f), // May
            Entry(5f, 0f)   // Jun (Dummy data for axis alignment)
        )

        val dataSet = LineDataSet(entries, "Performance").apply {
            color = Color.BLUE
            valueTextSize = 12f
            setCircleColor(Color.BLUE)
            setDrawCircleHole(false)
            setDrawValues(false)
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // Define months from Jan to Jun
        val months = listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")

        // Chart configurations
        lineChart.apply {
            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                valueFormatter = IndexAxisValueFormatter(months)
                textSize = 12f
                granularity = 1f // Ensures each month is evenly spaced
                setDrawGridLines(false)
            }
            axisRight.isEnabled = false
            invalidate()
        }

        // Wait until the chart is drawn to get correct positions
        lineChart.post {
            val viewPortHandler: ViewPortHandler = lineChart.viewPortHandler
            val transformer: Transformer = lineChart.getTransformer(YAxis.AxisDependency.LEFT)

            for (entry in entries) {
                // Ignore dummy entry for June (y=0)
                if (entry.x >= 5) continue

                // Convert chart values (data points) into pixel coordinates
                val pixelValues = transformer.getPixelForValues(entry.x, entry.y)

                val xPosition = pixelValues.x // X coordinate on screen
                val yPosition = pixelValues.y // Y coordinate on screen

                // Create a rounded strip dynamically
                val stripView = View(requireContext()).apply {
                    layoutParams = FrameLayout.LayoutParams(100, 12) // Wider height for smooth curves

                    // Create a rounded background for the strip
                    background = GradientDrawable().apply {
                        shape = GradientDrawable.RECTANGLE
                        cornerRadius = 6f // Round all corners
                        setColor(if (entry.y >= 70) Color.GREEN else Color.RED)
                    }

                    x = (xPosition).toFloat()  // Adjust X to center the strip
                    y = (yPosition - 6).toFloat()   // Adjust Y so it overlaps correctly
                }

                // Add the strip overlay dynamically
                stripsContainer.addView(stripView)
            }
        }
    }

    private fun setupChart() {
        barChart.setRenderer(RoundedBarChartRenderer(barChart, barChart.animator, barChart.viewPortHandler))
        barChart.description.isEnabled = false
        barChart.setDrawValueAboveBar(true)
        barChart.setDrawBarShadow(false)
        barChart.setPinchZoom(false)
        barChart.setDrawGridBackground(false)
        barChart.setScaleEnabled(false) // disables pinch and double-tap zoom
        barChart.isDoubleTapToZoomEnabled = false // disables zoom on double tap

        barChart.isDragEnabled = false
        barChart.isHighlightPerTapEnabled = false

        barChart.axisRight.isEnabled = false
        barChart.legend.isEnabled = false

        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.granularity = 1f
        xAxis.labelCount = 7

        val leftAxis = barChart.axisLeft
        leftAxis.axisMinimum = 0f
        leftAxis.axisMaximum = 100f
        leftAxis.setDrawGridLines(true)
        leftAxis.granularity = 20f
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }
}

class RoundedBarChartRenderer(
    chart: BarChart,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val mBarRect = RectF()
    private val radius = 20f  // change this for more or less curve

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans = mChart.getTransformer(dataSet.axisDependency)
        mBarBorderPaint.color = dataSet.barBorderColor
        mBarBorderPaint.strokeWidth = dataSet.barBorderWidth
        val drawBorder = dataSet.barBorderWidth > 0f
        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY
        // initialize the buffer
        mBarBuffers[index].setPhases(phaseX, phaseY)
        mBarBuffers[index].setDataSet(index)
        mBarBuffers[index].setInverted(mChart.isInverted(dataSet.axisDependency))
        mBarBuffers[index].setBarWidth(0.55f)
        mBarBuffers[index].feed(dataSet)
        trans.pointValuesToPixel(mBarBuffers[index].buffer)
        val isSingleColor = dataSet.colors.size == 1
        if (isSingleColor) {
            mRenderPaint.color = dataSet.color
        }

        val buffer = mBarBuffers[index].buffer

        for (j in buffer.indices step 4) {
            if (!mViewPortHandler.isInBoundsLeft(buffer[j + 2])) continue
            if (!mViewPortHandler.isInBoundsRight(buffer[j])) break

            if (!isSingleColor) {
                mRenderPaint.color = dataSet.getColor(j / 4)
            }
            mBarRect.set(buffer[j], buffer[j + 1], buffer[j + 2], buffer[j + 3])
            val path = Path()
            path.addRoundRect(
                mBarRect,
                floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f),
                Path.Direction.CW
            )
            c.drawPath(path, mRenderPaint)
            if (drawBorder) {
                c.drawPath(path, mBarBorderPaint)
            }
        }
    }
    override fun drawHighlighted(c: Canvas, indices: Array<Highlight>) {
        val barData = mChart.barData ?: return

        for (high in indices) {
            val set = barData.getDataSetByIndex(high.dataSetIndex) as? IBarDataSet ?: continue
            if (!set.isHighlightEnabled) continue
            val e = set.getEntryForXValue(high.x, high.y) ?: continue
            val trans = mChart.getTransformer(set.axisDependency)
            mHighlightPaint.color = Color.TRANSPARENT // or any color you want
            mHighlightPaint.alpha = 0 // fully transparent
            mBarRect.set(e.x - 0.5f + high.stackIndex, 0f, e.x + 0.5f + high.stackIndex, e.y)
            trans.rectValueToPixel(mBarRect)
            // Draw a rounded rect with transparent paint, effectively removing highlight
            val path = Path()
            path.addRoundRect(
                mBarRect,
                floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f),
                Path.Direction.CW
            )
            c.drawPath(path, mHighlightPaint)
        }
    }
}
