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
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import com.github.mikephil.charting.animation.ChartAnimator
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.databinding.FragmentSleepPerformanceBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
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
import com.jetsynthesys.rightlife.ai_package.model.FormattedData
import com.jetsynthesys.rightlife.ai_package.model.SleepPerformanceAllData
import com.jetsynthesys.rightlife.ai_package.model.SleepPerformanceList
import com.jetsynthesys.rightlife.ai_package.model.SleepPerformanceResponse
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.RestorativeSleepFragment.MultilineXAxisRenderer
import com.jetsynthesys.rightlife.ai_package.ui.thinkright.fragment.MindfullChartRenderer
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.LocalDate
import java.time.OffsetDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
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
        val startOfWeek = endOfWeek.minusDays(6)

        val formatter = DateTimeFormatter.ofPattern("d MMM")
        dateRangeText.text = "${startOfWeek.format(formatter)} - ${endOfWeek.format(formatter)}, ${currentDateWeek.year}"
        setupListeners()
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

    //    setupChart()

    }

    fun getTodayDate(): LocalDate {
        return LocalDate.now()
    }

    fun getOneWeekEarlierDate(): LocalDate {
        return LocalDate.now().minusWeeks(1)
    }
    fun getOneMonthEarlierDate(): LocalDate {
        return LocalDate.now().minusMonths(1)
    }
    fun getSixMonthsEarlierDate(): LocalDate {
        return LocalDate.now().minusMonths(6)
    }

    private fun setupListeners() {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
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
                        setSleepRightPerformanceData(sleepPerformanceResponse.sleepPerformanceAllData)
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

    private fun setSleepRightPerformanceData(sleepPerformanceResponse: SleepPerformanceAllData?) {
        tvSleepAverage.setText(""+sleepPerformanceResponse?.sleepPerformanceAverage?.roundToDecimals(2)+"%")
        performTitle.setText(sleepPerformanceResponse?.sleepInsightDetail?.title)
        performSubtitle.setText(sleepPerformanceResponse?.sleepInsightDetail?.subtitle)
        performMessage.setText(sleepPerformanceResponse?.sleepInsightDetail?.message)
      //  updateChart()
        if (sleepPerformanceResponse?.sleepPerformanceList?.isNotEmpty() == true) {
            if (sleepPerformanceResponse.sleepPerformanceList.size < 9 ) {
                setupWeeklyBarChart(barChart, sleepPerformanceResponse.sleepPerformanceList, sleepPerformanceResponse.endDatetime!!)
            }else{
                setupMonthlyBarChart(barChart, sleepPerformanceResponse.sleepPerformanceList, sleepPerformanceResponse.startDatetime!!,sleepPerformanceResponse.endDatetime!!)
            }
        }
        if (sleepPerformanceResponse?.progress_detail?.progress_sign == "plus"){
            percentageIcon.visibility = View.VISIBLE
            percentageIcon.setImageResource(R.drawable.ic_up)
            percentageText.visibility = View.VISIBLE
            percentageText.text = " "+ sleepPerformanceResponse?.progress_detail?.progress_percentage + " past week"
        }else{
            percentageIcon.visibility = View.VISIBLE
            percentageText.visibility = View.VISIBLE
            percentageIcon.setImageResource(R.drawable.ic_down)
            percentageIcon.setBackgroundColor(resources.getColor(R.color.red))
            percentageText.text = " "+ sleepPerformanceResponse?.progress_detail?.progress_percentage + " past week"
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

    fun setupMonthlyBarChart(chart: BarChart, data: List<SleepPerformanceList>?, startTime:String, endDateStr:String) {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")

        var startDate =convertToLocalDate(startTime)
        val mEndDate1 =convertToLocalDate(endDateStr)


        val labels = mutableListOf<String>()

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

        val dataSet = BarDataSet(entries, "Sleep Performance")
        dataSet.setColors(Color.parseColor("#4593FB"))
        dataSet.setDrawValues(false)
        dataSet.valueTextSize = 9f

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
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                cardPercent.visibility = View.VISIBLE
                if (e != null) {
                    val x = e.x.toInt()
                    val y = e.y
                    Log.d("ChartClick", "Clicked X: $x, Y: $y")
                    tvBarDate.text = labels.getOrNull(x) ?: ""
                    tvBarPercent.text = y.toInt().toString()
                }
            }

            override fun onNothingSelected() {
                Log.d("ChartClick", "Nothing selected")
                cardPercent.visibility = View.INVISIBLE
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

    fun setupWeeklyBarChart(chart: BarChart, data: List<SleepPerformanceList>?, endDate: String) {
        val entries = ArrayList<BarEntry>()
        val trimmedDate = endDate.substring(0, 10)  // "2025-05-01"
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val localDate = LocalDate.parse(trimmedDate, formatter)
        val labels = getWeekDayNames(localDate)

        data?.forEachIndexed { index, item ->
            entries.add(BarEntry(index.toFloat(), item.sleepPerformanceData?.sleepPerformance?.toFloat() ?: 0f))
        }

        val dataSet = BarDataSet(entries, "SleepPerformance")
        dataSet.setColors(Color.parseColor("#4593FB"))
        dataSet.setDrawValues(false)
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        chart.data = barData
        val customRenderer = RoundedBarChartRenderer(barChart, barChart.animator, barChart.viewPortHandler)
        customRenderer.initBuffers()
        chart.renderer = customRenderer

        /*chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            labelRotationAngle = -30f
            textSize = 10f
        }*/
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
        chart.invalidate()
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                cardPercent.visibility = View.VISIBLE
                if (e != null) {
                    val x = e.x.toInt()
                    val y = e.y
                    Log.d("ChartClick", "Clicked X: $x, Y: $y")
                    tvBarDate.text = labels.getOrNull(x)?.replace("\n", " ") ?: ""
                    tvBarPercent.text = y.toInt().toString()
                }
            }

            override fun onNothingSelected() {
                Log.d("ChartClick", "Nothing selected")
                cardPercent.visibility = View.INVISIBLE
            }
        })
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
