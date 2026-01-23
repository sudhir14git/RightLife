package com.jetsynthesys.rightlife.ai_package.ui.thinkright.fragment

import android.app.ProgressDialog
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.AbsoluteSizeSpan
import android.text.style.StyleSpan
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
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.buffer.BarBuffer
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.BarLineChartBase
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
import com.github.mikephil.charting.interfaces.dataprovider.BarDataProvider
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler
import com.google.android.material.snackbar.Snackbar
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.FormattedData
import com.jetsynthesys.rightlife.ai_package.model.MindfullResponse
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment.RestorativeSleepFragment.MultilineXAxisRenderer
import com.jetsynthesys.rightlife.databinding.FragmentMindfullGraphBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit
import java.util.Locale
import kotlin.math.roundToInt

class MindfulnessAnalysisFragment : BaseFragment<FragmentMindfullGraphBinding>() {

    private lateinit var barChart: BarChart
    private lateinit var lineChart: LineChart
    private lateinit var radioGroup: RadioGroup
    private lateinit var layoutLineChart: FrameLayout
    private lateinit var stripsContainer: FrameLayout
    private lateinit var progressDialog: ProgressDialog
    private lateinit var mindfullResponse: MindfullResponse
    private lateinit var btnPrevious: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var dateRangeText: TextView
    private lateinit var mindfullTitle: TextView
    private lateinit var mindfullDesc: TextView
    private lateinit var selectMindLayout: CardView
    private lateinit var selectedItemDate: TextView
    private lateinit var selectedCalorieTv: TextView
    private lateinit var recyclerView_mindfulness_analysis: RecyclerView
    private lateinit var average: TextView
    private var currentTab = 0 // 0 = Week, 1 = Month, 2 = 6 Months
    private var currentDateWeek: LocalDate = LocalDate.now() // today
    private var currentDateMonth: LocalDate = LocalDate.now() // today
    private var mStartDate = ""
    private var mEndDate = ""

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentMindfullGraphBinding
        get() = FragmentMindfullGraphBinding::inflate
    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        barChart = view.findViewById(R.id.heartRateChart)
        layoutLineChart = view.findViewById(R.id.lyt_line_chart)
        stripsContainer = view.findViewById(R.id.stripsContainer)
        lineChart = view.findViewById(R.id.heartLineChart)
        radioGroup = view.findViewById(R.id.tabGroup)
        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Loading")
        progressDialog.setCancelable(false)
        btnPrevious = view.findViewById(R.id.btn_prev)
        recyclerView_mindfulness_analysis = view.findViewById(R.id.recyclerView_mindfulness_analysis)
        btnNext = view.findViewById(R.id.btn_next)
        dateRangeText = view.findViewById(R.id.tv_date_range)
        mindfullTitle = view.findViewById(R.id.tv_mindfull_title)
        mindfullDesc = view.findViewById(R.id.tv_mindfull_desc)
        average = view.findViewById(R.id.tv_average_number)
        selectMindLayout = view.findViewById(R.id.selectCalorieLayout)
        selectedItemDate = view.findViewById(R.id.selectedDate)
        selectedCalorieTv = view.findViewById(R.id.selectedCalorieTv)
        radioGroup.check(R.id.rbWeek)
        val backBtn = view.findViewById<ImageView>(R.id.img_back)
        // Show Week data by default
        val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        mStartDate = getOneWeekWindowStart().format(dateFormatter)
        mEndDate = getTodayDate().format(dateFormatter)
        val endOfWeek = currentDateWeek
        val startOfWeek = endOfWeek.minusDays(6)

        val formatter = DateTimeFormatter.ofPattern("d MMM")
        dateRangeText.text = "${startOfWeek.format(formatter)} - ${endOfWeek.format(formatter)}, ${currentDateWeek.year}"
        setupListeners()
       // updateChart(getWeekData(), getWeekLabels())
        fetchMindfullnessData(mStartDate, mEndDate)


        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val fragment = HomeBottomTabFragment()
                val args = Bundle().apply {
                    putString("ModuleName", "ThinkRight")
                }
                fragment.arguments = args
                requireActivity().supportFragmentManager.beginTransaction().apply {
                    replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                    addToBackStack(null)
                    commit()
                }
            }
        })

        backBtn.setOnClickListener {
            val fragment = HomeBottomTabFragment()
            val args = Bundle().apply {
                putString("ModuleName", "ThinkRight")
            }
            fragment.arguments = args
            requireActivity().supportFragmentManager.beginTransaction().apply {
                replace(R.id.flFragment, fragment, "SearchWorkoutFragment")
                addToBackStack(null)
                commit()
            }
        }
    }

    fun getTodayDate(): LocalDate {
        return LocalDate.now()
    }

    fun getOneWeekWindowStart(): LocalDate {
        return LocalDate.now().minusDays(6) // today included, so 7 days total
    }

    fun getOneWeekEarlierDate(): LocalDate {
        return LocalDate.now().minusWeeks(1)
    }
    fun getOneMonthEarlierDate(): LocalDate {
        return LocalDate.now().minusDays(29)
    }
    fun getSixMonthsEarlierDate(): LocalDate {
        return LocalDate.now().minusMonths(6)
    }

    private fun setupListeners() {
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbWeek -> {
                    currentTab = 0
                    barChart.visibility = View.VISIBLE
                    layoutLineChart.visibility = View.GONE
                    val rbWeek = requireView().findViewById<RadioButton>(R.id.rbWeek)
                    val rbMonth = requireView().findViewById<RadioButton>(R.id.rbMonth)
                    rbWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    rbMonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val startDate = getOneWeekWindowStart().format(dateFormatter)
                    val endDate = getTodayDate().format(dateFormatter)
                    val formatter = DateTimeFormatter.ofPattern("d MMM")
                    dateRangeText.text = "${getOneWeekWindowStart().format(formatter)} - ${getTodayDate().format(formatter)}, ${currentDateWeek.year}"
                    fetchMindfullnessData(startDate,endDate)
                }
                R.id.rbMonth -> {
                    currentTab = 1
                    barChart.visibility = View.VISIBLE
                    layoutLineChart.visibility = View.GONE
                    val rbWeek = requireView().findViewById<RadioButton>(R.id.rbWeek)
                    val rbMonth = requireView().findViewById<RadioButton>(R.id.rbMonth)
                    rbWeek.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
                    rbMonth.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val startDate = getOneMonthEarlierDate().format(dateFormatter)
                    val endDate = getTodayDate().format(dateFormatter)
                    val formatter = DateTimeFormatter.ofPattern("d MMM")
                    dateRangeText.text = "${getOneMonthEarlierDate().format(formatter)} - ${getTodayDate().format(formatter)}, ${currentDateMonth.year}"
                    fetchMindfullnessData(startDate,endDate)
                }
                R.id.rbSixMonths -> {
                    currentTab = 2
                    barChart.visibility = View.GONE
                    layoutLineChart.visibility = View.VISIBLE
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val startDate = getSixMonthsEarlierDate().format(dateFormatter)
                    val endDate = getTodayDate().format(dateFormatter)
                    val formatter = DateTimeFormatter.ofPattern("d MMM")
                    dateRangeText.text = "${getSixMonthsEarlierDate().format(formatter)} - ${getTodayDate().format(formatter)}, ${currentDateMonth.year}"
                    fetchMindfullnessData(startDate,endDate)
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
                    currentDateMonth = currentDateMonth.minusDays(29)
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
                    if (currentDateWeek < LocalDate.now()){
                        currentDateWeek = currentDateWeek.plusWeeks(1)
                        loadWeekData()
                    }
                }
                1 -> {
                    if (currentDateMonth < LocalDate.now()){
                        currentDateMonth = currentDateMonth.plusDays(29)
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

        fetchMindfullnessData(startOfWeek.format(formatter1),endOfWeek.format(formatter1))
    //    val entries = mutableListOf<BarEntry>()
    //    updateChart(entries, listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"))
    }

    private fun formatDateToDayMonth(inputDate: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter = DateTimeFormatter.ofPattern("d MMM")
        val date = LocalDate.parse(inputDate, inputFormatter)
        return date.format(outputFormatter)
    }

    private fun loadMonthData() {
        val endOfMonth = currentDateMonth
        val startOfMonth = endOfMonth.minusDays(29)
        val formatter = DateTimeFormatter.ofPattern("d MMM")
        dateRangeText.text = "${startOfMonth.format(formatter)} - ${endOfMonth.format(formatter)}, ${currentDateMonth.year}"

        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        fetchMindfullnessData(startOfMonth.format(formatter1),endOfMonth.format(formatter1))

        /*val entries = mutableListOf<BarEntry>()
        val daysInMonth = endOfMonth.dayOfMonth
        for (i in 0 until daysInMonth) {
            entries.add(BarEntry(i.toFloat(), (50..100).random().toFloat()))
        }

        val weekRanges = listOf("1", "2", "3", "4", "5","6", "7", "8", "9", "10","11", "12", "13", "14", "15","16", "17", "18", "19", "20","21", "22", "23", "24", "25","26", "27", "28", "29", "30")

        updateChart(entries, weekRanges)*/
    }

    private fun loadSixMonthsData() {
     //   val startOfPeriod = currentDate.minusMonths(5).with(TemporalAdjusters.firstDayOfMonth())
     //   val endOfPeriod = currentDate.with(TemporalAdjusters.lastDayOfMonth())

        val startDate = getSixMonthsEarlierDate()
        val endDate = getTodayDate()

        val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
        dateRangeText.text = "${startDate.format(formatter)} - ${endDate.format(formatter)}"

        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")

        fetchMindfullnessData(startDate.format(formatter1),endDate.format(formatter1))

        /*val entries = mutableListOf<BarEntry>()
        for (i in 0..5) {
            entries.add(BarEntry(i.toFloat(), (50..100).random().toFloat()))
        }

        val months = listOf(
            startOfPeriod.month.name.take(3),
            startOfPeriod.plusMonths(1).month.name.take(3),
            startOfPeriod.plusMonths(2).month.name.take(3),
            startOfPeriod.plusMonths(3).month.name.take(3),
            startOfPeriod.plusMonths(4).month.name.take(3),
            startOfPeriod.plusMonths(5).month.name.take(3)
        )
        updateChart(entries, months)*/
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
            color = resources.getColor(R.color.eatright_text_color)
            valueTextSize = 12f
            setCircleColor(resources.getColor(R.color.eatright_text_color))
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

    private fun fetchMindfullnessData(startDate: String, endDate: String) {
        progressDialog.show()
        val token = SharedPreferenceManager.getInstance(requireActivity()).accessToken
       /* val StartTime = "2025-11-08"
        val EndTime = "2025-11-14"*/
       // val token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJkYXRhIjp7ImlkIjoiNjhlMzU0MDMyZTcyMDE0MjljODNmYzM3Iiwicm9sZSI6InVzZXIiLCJjdXJyZW5jeVR5cGUiOiJJTlIiLCJmaXJzdE5hbWUiOiJUdXNoYXIiLCJsYXN0TmFtZSI6IkdveWFsIiwiZGV2aWNlSWQiOiI5MjU5MDlBMS1EQzQ3LTQwRkQtQURFMC0yMzU2RjYzMEUzOTQiLCJtYXhEZXZpY2VSZWFjaGVkIjpmYWxzZSwidHlwZSI6ImFjY2Vzcy10b2tlbiJ9LCJpYXQiOjE3NjIxNDk3MTAsImV4cCI6MTc3Nzg3NDUxMH0.1lt-jn1Fyx4ABv3kOEK_8Rs3hlg6DfsqTuyhNurMWfM"
        val call = ApiClient.apiService.fetchMindFull(token,startDate, endDate)
        call.enqueue(object : Callback<MindfullResponse> {
            override fun onResponse(call: Call<MindfullResponse>, response: Response<MindfullResponse>) {
                if (response.isSuccessful) {
                    mindfullResponse = response.body()!!
                    progressDialog.dismiss()
                    val recyclerView = view?.findViewById<RecyclerView>(R.id.recyclerView_mindfulness_analysis)
                    recyclerView?.layoutManager = GridLayoutManager(requireContext(), 2) // 2 columns

                    val dataList = listOf(
                        MindfullChartRenderer.StatCard(
                            "Affirmations",
                            "Your Playlist",
                            mindfullResponse.data?.affirmationCount!!,
                            "affirmations practiced",
                            R.drawable.mindfulness_affirmation
                        ),
                        MindfullChartRenderer.StatCard("Journaling", "Your Entries", mindfullResponse.data?.journalCount!!, "entries saved",
                            R.drawable.journaling_ink_icon)
                    )

                    val adapter = StatCardAdapter(dataList)
                    recyclerView?.adapter = adapter
                    mindfullTitle.text = mindfullResponse?.data?.title
                    mindfullDesc.text = mindfullResponse?.data?.description
                    if (mindfullResponse?.data?.averageDuration != null) {
                        average.text = formatMinutesToHours(mindfullResponse?.data?.averageDuration!!.toInt())
                    }
                    if (mindfullResponse.data?.formattedData?.isNotEmpty() == true) {
                        if (mindfullResponse.data?.formattedData?.size!! > 8){
                            setupMonthlyBarChart(barChart,mindfullResponse.data?.formattedData,startDate,endDate)
                        }else{
                            setupWeeklyBarChart(barChart, mindfullResponse.data?.formattedData,endDate)
                        }
                    }
                } else {
                    Log.e("Error", "Response not successful: ${response.errorBody()?.string()}")
                    Toast.makeText(activity, "Something went wrong", Toast.LENGTH_SHORT).show()
                    progressDialog.dismiss()
                }
            }
            override fun onFailure(call: Call<MindfullResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }

    fun formatMinutesToHours(minutes: Int): String {
        val hours = minutes / 60
        val mins = minutes % 60
        return if (hours > 0) {
            // Show hours and minutes (with leading zeros for minutes)
            String.format("%d hr %02d mins", hours, mins)
        } else {
            // Only minutes
            String.format("%d mins", mins)
        }
    }


    fun setupMonthlyBarChart(chart: BarChart, data: List<FormattedData>?, startDateStr: String, endDateStr: String) {
        selectMindLayout.visibility = View.INVISIBLE
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val startDate = LocalDate.parse(startDateStr, formatter)
        val endDate = LocalDate.parse(endDateStr, formatter)

        val daysBetween = ChronoUnit.DAYS.between(startDate, endDate).toInt() + 1
        val entries = ArrayList<BarEntry>()
        val labels = ArrayList<String>()

        val monthFormatter = DateTimeFormatter.ofPattern("MMM")

        for (i in 0 until daysBetween) {
            val currentDate = startDate.plusDays(i.toLong())

            val groupIndex = i / 7
            val groupStartDate = startDate.plusDays(groupIndex * 7L)
            val groupEndDate = groupStartDate.plusDays(6).coerceAtMost(endDate)

            val label = if (i % 7 == 0) {
                val dayRange = "${groupStartDate.dayOfMonth}–${groupEndDate.dayOfMonth}"
                val month = groupEndDate.format(monthFormatter)
                val spaces = " ".repeat((dayRange.length - month.length).coerceAtLeast(0) / 2)
                "$dayRange\n$spaces$month"
            } else {
                ""
            }
            labels.add(label)
        }

        // CHANGE 1: Minutes → Hours
        data?.forEachIndexed { index, item ->
            val durationInHours = (item.duration ?: 0) / 60f
            entries.add(BarEntry(index.toFloat(), durationInHours))
        }

        val dataSet = BarDataSet(entries, "Monthly Mindfulness")
        dataSet.setColors(Color.parseColor("#FFD54F"))
        dataSet.valueTextSize = 9f

        val barData = BarData(dataSet)
        barData.barWidth = 0.4f
        barData.setDrawValues(false)
        chart.data = barData

        val customRenderer = MindfullChartRenderer(chart, chart.animator, chart.viewPortHandler)
        customRenderer.initBuffers()
        chart.renderer = customRenderer

        chart.apply {
            setFitBars(true)
            setScaleEnabled(false)
            isDoubleTapToZoomEnabled = false
            isHighlightPerTapEnabled = true
            isHighlightPerDragEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
        }

        // X-AXIS: BILKUL SAME JAISA PEHLE THA
        chart.xAxis.apply {
            valueFormatter = IndexAxisValueFormatter(labels)
            granularity = 1f
            labelCount = labels.size
            position = XAxis.XAxisPosition.BOTTOM
            setDrawGridLines(false)
            labelRotationAngle = -30f   // ← PEHLE JAISA HI
            textSize = 8f
        }

        // CHANGE 2: Y-axis fixed 0 to 24, step 4
        chart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 24f
            granularity = 4f
            setLabelCount(7, true) // 0,4,8,12,16,20,24
        }
        chart.axisRight.isEnabled = false

        chart.setVisibleXRangeMaximum(labels.size.toFloat())

        // CHANGE 3: Touch text formatting
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                selectMindLayout.visibility = View.VISIBLE
                e?.let {
                    val index = e.x.toInt()
                    val valueInHours = e.y
                    val selectedDate = data?.getOrNull(index)

                    selectedItemDate.text = convertDate(selectedDate?.date ?: "")

                    val totalMinutes = (valueInHours * 60).roundToInt()
                    val hours = totalMinutes / 60
                    val minutes = totalMinutes % 60

                    val text = String.format("%02d hr %02d min", hours, minutes)
                    val spannable = SpannableString(text)

                    // "01" → 32dp, bold
                    spannable.setSpan(AbsoluteSizeSpan(32, true), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(StyleSpan(Typeface.BOLD), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // "hr" → 10sp, normal
                    spannable.setSpan(AbsoluteSizeSpan(10, true), 3, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(StyleSpan(Typeface.NORMAL), 3, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // "49" → 32dp, bold
                    spannable.setSpan(AbsoluteSizeSpan(32, true), 6, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(StyleSpan(Typeface.BOLD), 6, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // "min" → 10sp, normal
                    spannable.setSpan(AbsoluteSizeSpan(10, true), 9, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(StyleSpan(Typeface.NORMAL), 9, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    selectedCalorieTv.text = spannable
                }
            }

            override fun onNothingSelected() {
                selectMindLayout.visibility = View.INVISIBLE
            }
        })

        chart.invalidate()
    }

    fun getWeekDayNames(endOfWeek: LocalDate): List<String> {
        val startOfWeek = endOfWeek.minusDays(6)
        val formatter = DateTimeFormatter.ofPattern("EEE") // "Mon", "Tue", etc.

        return (0..6).map { dayOffset ->
            startOfWeek.plusDays(dayOffset.toLong()).format(formatter)
        }
    }
    fun setupWeeklyBarChart(chart: BarChart, data: List<FormattedData>?, endDate: String) {
        selectMindLayout.visibility = View.INVISIBLE
        val entries = ArrayList<BarEntry>()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val localDate = LocalDate.parse(endDate, formatter)
        val labels = mutableListOf<String>()

        data?.forEachIndexed { index, (label, durations) ->
            val date = LocalDate.parse(label, DateTimeFormatter.ISO_LOCAL_DATE)
            val top = date.format(DateTimeFormatter.ofPattern("EEE"))      // e.g., "Fri"
            val bottom = date.format(DateTimeFormatter.ofPattern("d MMM"))
            labels.add("$top\n$bottom")
        }

        // Convert minutes → decimal hours
        data?.forEachIndexed { index, item ->
            val durationInMinutes = item.duration ?: 0
            val durationInHours = durationInMinutes / 60f  // 120 min → 2.0 hr
            entries.add(BarEntry(index.toFloat(), durationInHours))
        }

        val dataSet = BarDataSet(entries, "Mindfulness")
        dataSet.setColors(Color.parseColor("#FFD54F")) // Light Yellow
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barData.setDrawValues(false)
        chart.data = barData
        val customRenderer = MindfullChartRenderer(chart, chart.animator, chart.viewPortHandler)
        customRenderer.initBuffers()
        chart.renderer = customRenderer

        chart.apply {
            setFitBars(true)
            setScaleEnabled(false)
            isDoubleTapToZoomEnabled = false
            isHighlightPerTapEnabled = true
            isHighlightPerDragEnabled = false
            description.isEnabled = false
            legend.isEnabled = false
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

        // FIXED Y-AXIS: 0 to 24, step 4 → Labels: 0,4,8,12,16,20,24
        chart.axisLeft.apply {
            axisMinimum = 0f
            axisMaximum = 24f
            granularity = 4f
            setLabelCount(7, true)  // 0,4,8,12,16,20,24 → 7 labels
        }

        chart.axisRight.isEnabled = false
        chart.setExtraBottomOffset(24f)
        chart.setScaleEnabled(false)
        chart.setXAxisRenderer(
            MultilineXAxisRenderer(
                chart.viewPortHandler,
                chart.xAxis,
                chart.getTransformer(YAxis.AxisDependency.LEFT)
            )
        )
        fun Float.toHoursAndMinutesString(): String {
            val totalMinutes = (this * 60).toInt()
            val hours = totalMinutes / 60
            val minutes = totalMinutes % 60
            return String.format("%02d hr %02d min", hours, minutes)
        }

        // Chart selection listener - value in hours
        chart.setOnChartValueSelectedListener(object : OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                selectMindLayout.visibility = View.VISIBLE
                e?.let {
                    val index = e.x.toInt()
                    val valueInHours = e.y
                    val selectedDate = data?.getOrNull(index)

                    selectedItemDate.text = convertDate(selectedDate?.date ?: "")

                    // Convert to hours & minutes
                    val totalMinutes = (valueInHours * 60).roundToInt()
                    val hours = totalMinutes / 60
                    val minutes = totalMinutes % 60

                    // Build: "01 hr 49 min"
                    val text = String.format("%02d hr %02d min", hours, minutes)
                    val spannable = SpannableString(text)

                    // "01" → 32dp, bold
                    spannable.setSpan(AbsoluteSizeSpan(32, true), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(StyleSpan(Typeface.BOLD), 0, 2, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // "hr" → 10sp, normal
                    spannable.setSpan(AbsoluteSizeSpan(13, true), 3, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(StyleSpan(Typeface.NORMAL), 3, 5, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // "49" → 32dp, bold
                    spannable.setSpan(AbsoluteSizeSpan(32, true), 6, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(StyleSpan(Typeface.BOLD), 6, 8, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    // "min" → 10sp, normal
                    spannable.setSpan(AbsoluteSizeSpan(13, true), 9, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
                    spannable.setSpan(StyleSpan(Typeface.NORMAL), 9, 12, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                    selectedCalorieTv.text = spannable
                }
            }

            override fun onNothingSelected() {
                selectMindLayout.visibility = View.INVISIBLE
            }
        })

        chart.invalidate()
    }

    private fun convertDate(inputDate: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val outputFormat = SimpleDateFormat("d MMM, yyyy", Locale.getDefault())
        return try {
            val date = inputFormat.parse(inputDate)
            outputFormat.format(date!!)
        } catch (e: Exception) {
            "Invalid Date"
        }
    }

    fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.now().format(formatter)
    }

    fun getYesterdayDate(s: String): String {
        var dates = ""
        if(s=="week") {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            dates = LocalDate.now().minusDays(7).format(formatter)
        }else if(s=="month"){
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            dates = LocalDate.now().minusDays(31).format(formatter)
        }else if (s=="six"){
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            dates = LocalDate.now().minusDays(183).format(formatter)
        }
        return dates
    }

    private fun updateChart(entries: List<BarEntry>, labels: List<String>) {
        val dataSet = BarDataSet(entries, "Calories Burned")
        dataSet.color = resources.getColor(R.color.eatright_text_color)
        dataSet.valueTextColor = Color.BLACK
        dataSet.valueTextSize = 12f

        val barData = BarData(dataSet)
        barData.barWidth = 0.4f // Set bar width

        barChart.data = barData
        barChart.setFitBars(true)

        // Customize X-Axis
        val xAxis = barChart.xAxis
        xAxis.valueFormatter = IndexAxisValueFormatter(labels) // Set custom labels
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.textSize = 12f
        xAxis.granularity = 1f
        xAxis.setDrawGridLines(false)
        xAxis.textColor = Color.BLACK
        xAxis.yOffset = 15f // Move labels down

        // Customize Y-Axis
        val leftYAxis: YAxis = barChart.axisLeft
        leftYAxis.textSize = 12f
        leftYAxis.textColor = Color.BLACK
        leftYAxis.setDrawGridLines(true)

        // Disable right Y-axis
        barChart.axisRight.isEnabled = false
        barChart.description.isEnabled = false
        barChart.animateY(1000) // Smooth animation
        barChart.invalidate()
    }

    private fun getWeekLabels(): List<String> {
        return listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    private fun getMonthLabels(): List<String> {
        return listOf("1-7 Jan", "8-14 Jan", "15-21 Jan", "22-28 Jan", "29-31 Jan")
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String, moduleName: String = "Think") {
        val args = Bundle()
        args.putString("ModuleName", moduleName) // Pass the moduleName parameter
        fragment.arguments = args
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }
}

class MindfullChartRenderer(
    chart: BarDataProvider,
    animator: ChartAnimator,
    viewPortHandler: ViewPortHandler
) : BarChartRenderer(chart, animator, viewPortHandler) {

    private val radius = 20f

    override fun initBuffers() {
        val barData = mChart.barData
        mBarBuffers = Array(barData.dataSetCount) { i ->
            BarBuffer(
                barData.getDataSetByIndex(i).entryCount * 4,
                barData.dataSetCount,
                barData.getDataSetByIndex(i).isStacked
            )
        }
    }

    override fun drawDataSet(c: Canvas, dataSet: IBarDataSet, index: Int) {
        val trans = mChart.getTransformer(dataSet.axisDependency)
        val drawBorder = dataSet.barBorderWidth > 0f

        val phaseX = mAnimator.phaseX
        val phaseY = mAnimator.phaseY

        val barData = mChart.barData
        val buffer = mBarBuffers[index]
        buffer.setPhases(phaseX, phaseY)
        buffer.setDataSet(index)
        buffer.setInverted(mChart.isInverted(dataSet.axisDependency))
        buffer.setBarWidth(barData.barWidth)
        buffer.feed(dataSet)
        trans.pointValuesToPixel(buffer.buffer)

        for (j in buffer.buffer.indices step 4) {
            val left = buffer.buffer[j]
            val top = buffer.buffer[j + 1]
            val right = buffer.buffer[j + 2]
            val bottom = buffer.buffer[j + 3]

            val path = Path().apply {
                addRoundRect(
                    RectF(left, top, right, bottom),
                    floatArrayOf(radius, radius, radius, radius, 0f, 0f, 0f, 0f),
                    Path.Direction.CW
                )
            }

            mRenderPaint.color = dataSet.getColor(j / 4)
            c.drawPath(path, mRenderPaint)

            if (drawBorder) {
                c.drawPath(path, mBarBorderPaint)
            }
        }
    }
    data class StatCard(
        val title: String,
        val subtitle: String,
        val count: Int,
        val footer: String,
        val imageResId: Int
    )
}
