package com.jetsynthesys.rightlife.newdashboard

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Html
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.highlight.Highlight
import com.github.mikephil.charting.listener.OnChartValueSelectedListener
import com.jetsynthesys.rightlife.BaseActivity
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.apimodel.newreportfacescan.HealthCamItem
import com.jetsynthesys.rightlife.databinding.ActivityFacialScanReportDetailsBinding
import com.jetsynthesys.rightlife.newdashboard.model.FacialScanRange
import com.jetsynthesys.rightlife.newdashboard.model.FacialScanReportData
import com.jetsynthesys.rightlife.newdashboard.model.FacialScanReportDataWrapper
import com.jetsynthesys.rightlife.newdashboard.model.FacialScanReportResponse
import com.jetsynthesys.rightlife.ui.healthcam.HealthCamRecommendationAdapter
import com.jetsynthesys.rightlife.ui.healthcam.ParameterModel
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone


class FacialScanReportDetailsActivity : BaseActivity() {
    private lateinit var binding: ActivityFacialScanReportDetailsBinding
    private lateinit var dateRange: String
    private lateinit var startDateAPI: String
    private lateinit var endDateAPI: String
    private var selectedRange: String? = "week"
    private var monthOffset = 0
    private var sixMonthOffset = 0
    private var weekOffsetDays = 0
    private var defaultPosition = 0
    var vitalKey = ""
    private lateinit var graphData: FacialScanReportData


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityFacialScanReportDetailsBinding.inflate(layoutInflater)
        setChildContentView(binding.root)

        binding.backButton.setOnClickListener {
            finish()
        }

        val healthCamItems: ArrayList<HealthCamItem>? =
            intent.getSerializableExtra("healthCamItemList") as ArrayList<HealthCamItem>?
        defaultPosition = intent.getIntExtra("position", 0)
        // Receive the UNIFIED_LIST
        val unifiedList = intent.getSerializableExtra("UNIFIED_LIST") as ArrayList<ParameterModel>?

        if (unifiedList != null) {
            // Use the list (e.g., log size or bind to RecyclerView)
            Log.d("UNIFIED_LIST_SIZE", unifiedList.size.toString())
        }

        if (healthCamItems != null) {
            // You can now use this list in the new activity
            for (item in healthCamItems) {
                // do something
            }
        }
        //updateChart(getWeekData(), getWeekLabels())

        binding.backwardImageHeartRate.setOnClickListener {
            if (selectedRange == "week") {
                weekOffsetDays -= 7
                dateRange = getWeekRange()
                binding.tvDateRange.text = dateRange
                fetchPastFacialScanReport(
                    convertVitalNameToKey(binding.tvWitale.text.toString()),
                    startDateAPI,
                    endDateAPI

                )
            } else {
                Toast.makeText(this, "Only week can be selected", Toast.LENGTH_SHORT).show()
            }
        }
        binding.forwardImageHeartRate.setOnClickListener {
            if (selectedRange == "week") {
                weekOffsetDays += 7
                dateRange = getWeekRange()
                binding.tvDateRange.text = dateRange
                fetchPastFacialScanReport(
                    convertVitalNameToKey(binding.tvWitale.text.toString()),
                    startDateAPI,
                    endDateAPI

                )
            } else {
                Toast.makeText(this, "Only week can be selected", Toast.LENGTH_SHORT).show()
            }
        }

        // Handle Radio Button Selection
        binding.tabGroup.setOnCheckedChangeListener { _, checkedId ->
            val date = dateRange.split("-")
            when (checkedId) {
                R.id.rbWeek -> {
                    selectedRange = "week"
                    dateRange = getWeekRange()
                    binding.tvDateRange.text = dateRange
                    fetchPastFacialScanReport(
                        convertVitalNameToKey(binding.tvWitale.text.toString()),
                        startDateAPI,
                        endDateAPI

                    )

                }

                R.id.rbMonth -> {
                    selectedRange = "month"
                    dateRange = getMonthRange()
                    binding.tvDateRange.text = dateRange
                    fetchPastFacialScanReport(
                        convertVitalNameToKey(binding.tvWitale.text.toString()),
                        startDateAPI,
                        endDateAPI
                    )

                }

                R.id.rbSixMonths -> {
                    selectedRange = "6months"
                    dateRange = getSixMonthRange()
                    binding.tvDateRange.text = dateRange
                    fetchPastFacialScanReport(
                        convertVitalNameToKey(binding.tvWitale.text.toString()),
                        startDateAPI,
                        endDateAPI
                    )

                }
            }
        }


        dateRange = getWeekRange()
        binding.tvDateRange.text = dateRange

        val date = dateRange.split("-")


        //fetchPastFacialScanReport("PULSE_RATE", startDateAPI, endDateAPI)

        selectedRange = "week"
        dateRange = getWeekRange()
        binding.tvDateRange.text = dateRange
        /*  fetchPastFacialScanReport(
              convertVitalNameToKey(binding.tvWitale.text.toString()),
              startDateAPI,
              endDateAPI

          )*/
        if (unifiedList != null) {
            selectDefaultVital(unifiedList)
        }
        binding.rlWitelsSelection.setOnClickListener {
            if (unifiedList != null) {
                showPopupWithRecycler(it, unifiedList)
            }
        }

        binding.heartRateChart.setOnChartValueSelectedListener(object :
            OnChartValueSelectedListener {
            override fun onValueSelected(e: Entry?, h: Highlight?) {
                e?.let {
                    val date = graphData.data?.get(0)?.createdAt?.let { it1 ->
                        formatIsoDateToReadable(
                            it1
                        )
                    }
                    val value = it.y

                    binding.reportDate.text = date.toString()
                    binding.reportDate.visibility = View.GONE
                    binding.reportNameAverageValue.text = value.toString()
                    binding.reportUnit.text = graphData.data?.get(0)?.unit
                    //  showToast("date = $date  and value = $value")
                    binding.llSelectedGraphItem.visibility = View.VISIBLE
                }
            }

            override fun onNothingSelected() {

            }

        })

        binding.rlRangesButton.setOnClickListener {
            if (binding.llRangeExpand.visibility == View.VISIBLE) {
                binding.llRangeExpand.visibility = View.GONE
                binding.iconArrowRanges.rotation = 360f // Rotate by 180 degrees
            } else {
                binding.llRangeExpand.visibility = View.VISIBLE
                binding.iconArrowRanges.rotation = 180f // Rotate by 180 degrees
            }
        }
    }

    private fun selectDefaultVital(healthCamItems: ArrayList<ParameterModel>?) {
        if (healthCamItems != null) {
            if (healthCamItems.isNotEmpty()) {
                val firstItem = healthCamItems[defaultPosition]
                binding.tvWitale.text = firstItem.name
                vitalKey = firstItem.key
                fetchPastFacialScanReport(
                    firstItem.key,
                    startDateAPI,
                    endDateAPI
                )
            }
        }
        Glide.with(this@FacialScanReportDetailsActivity)
            .load(getReportIconByType(vitalKey))
            .placeholder(R.drawable.ic_db_report_heart_rate)
            .error(R.drawable.ic_db_report_heart_rate)
            .into(binding.imgPopupMenu)
    }

    private fun fetchPastFacialScanReport(key: String, startDate: String, endDate: String) {
        binding.llSelectedGraphItem.visibility = View.GONE
        //Dummy data
        val entries = mutableListOf<Entry>()

        for (i in 1..7) {
            val entry = (60..120).random() // Random heart rate
            // Add entry to the list
            entries.add(Entry(i.toFloat(), entry.toFloat()))
        }
        //updateChart(entries, getWeekLabels())

        val call = apiService.getPastReport(
            sharedPreferenceManager.accessToken,
            startDate,
            endDate,
            vitalKey
        )

        call.enqueue(object : Callback<FacialScanReportResponse> {
            override fun onResponse(
                call: Call<FacialScanReportResponse>,
                response: Response<FacialScanReportResponse>
            ) {
                if (response.isSuccessful && response.body() != null) {
                    clearGraph(binding)
                    val responseBody = response.body()
                    graphData =
                        responseBody?.data?.result!! // ✅ now getting `result` object from `data`
                    val reportList =
                        graphData.data           // ✅ this is your ArrayList<FacialScanReport>

                    if (reportList.isNullOrEmpty()) {
                        return
                    }

                    binding.tvAverage.text = graphData.avgValue.toString()
                    binding.tvMaximum.text = graphData.maxValue.toString()
                    binding.tvMinimum.text = graphData.minValue.toString()
                    binding.tvDescription.text = graphData.deffination.toString()
                    // Graph plotting (using yAxisValue as dummy example)
                    val entries = mutableListOf<Entry>()
                    graphData.yAxisValue?.forEach { y ->
                        entries.add(Entry(-1f, y.toFloat()))
                    }

                    selectedRange?.let { plotGraph2(graphData, binding, it) }

                    var firstReport: FacialScanRange? = null
                    val rangeList = responseBody.data?.range?.toMutableList()
                    if (rangeList?.isNotEmpty() == true) {
                        // Set bottom card data
                        firstReport = rangeList[0]
                        binding.indicator.text = firstReport.indicator
                        binding.tvIndicatorExplain.text =
                            Html.fromHtml(firstReport.implication, Html.FROM_HTML_MODE_COMPACT)
                        binding.tvIndicatorValue.text = "${firstReport.value} ${firstReport.unit}"
                        binding.tvIndicatorValueBg.text =
                            "${firstReport.lowerRange}-${firstReport.upperRange} ${firstReport.unit}"

                        val colorHexString =
                            "#" + firstReport.colour // Construct the correct hex string
                        val colorInt = Color.parseColor(colorHexString)
                        val colorStateList = ColorStateList.valueOf(colorInt)
                        binding.tvIndicatorValueBg.backgroundTintList = colorStateList
                    }

                    if (firstReport != null && rangeList != null) {
                        val iterator = rangeList.iterator()
                        while (iterator.hasNext()) {
                            val item = iterator.next()
                            if (item.indicator == firstReport.indicator) {
                                iterator.remove() // safe removal
                                break
                            }
                        }
                    }

                    setupBottomCard(rangeList)
                    handleContinueWatchUI(responseBody.data)
                } else {
                    showToast("Server Error: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<FacialScanReportResponse>, t: Throwable) {
                handleNoInternetView(t)
            }

        })
    }

    private fun setupBottomCard(rangeList: List<FacialScanRange>?) {
        val adapter = FaceScanRangesAdapter(rangeList)
        binding.recyclerViewRanges.layoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        binding.recyclerViewRanges.adapter = adapter
    }


    private fun showToast(message: String) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }

    private fun dpToPx(context: Context, v: Int): Int {
        return (v * context.resources.displayMetrics.density).toInt()
    }

    private fun showPopupWithRecycler(anchor: View, options: ArrayList<ParameterModel>) {
        val context = anchor.context
        val popupView = LayoutInflater.from(context).inflate(R.layout.popup_vitals, null)

        val popupWindow = PopupWindow(
            popupView,
            dpToPx(this, 290), // Fixed width in pixels,
            ViewGroup.LayoutParams.WRAP_CONTENT,
            true
        )

        popupWindow.isOutsideTouchable = true
        popupWindow.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        val recyclerView = popupView.findViewById<RecyclerView>(R.id.recyclerOptions)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = PopupMenuAdapter(this, options) { selectedOption ->
            binding.tvWitale.text = selectedOption.name

            vitalKey = selectedOption.key
            fetchPastFacialScanReport(
                vitalKey,
                startDateAPI,
                endDateAPI
            )

            Glide.with(this@FacialScanReportDetailsActivity)
                .load(getReportIconByType(vitalKey))
                .placeholder(R.drawable.ic_db_report_heart_rate)
                .error(R.drawable.ic_db_report_heart_rate)
                .into(binding.imgPopupMenu)
            popupWindow.dismiss()
        }

        popupWindow.showAsDropDown(anchor, 0, -110)
    }

    private fun convertVitalNameToKey(vitalName: String): String {
        return when (vitalName) {
            "Breathing Rate", "Respiratory Rate" -> "BR_BPM"
            "Heart Rate Variability" -> "HRV_SDNN"
            "Cardiac Workload" -> "BP_RPP"
            "Cardiovascular Disease Risks" -> "BP_CVD"
            "Pulse Rate" -> "HR_BPM"
            "Stress Index", "Stress Levels" -> "MSI"
            "Body Mass Index" -> "BMI_CALC"
            "Diastolic Blood Pressure", "Blood Pressure" -> "BP_DIASTOLIC"
            "Systolic Blood Pressure" -> "BP_SYSTOLIC"
            "Overall Wellness Score" -> "HEALTH_SCORE"
            else -> ""
        }
    }

    private fun capToToday(inputDate: Date): Date {
        val today = Calendar.getInstance()
        today.set(Calendar.HOUR_OF_DAY, 0)
        today.set(Calendar.MINUTE, 0)
        today.set(Calendar.SECOND, 0)
        today.set(Calendar.MILLISECOND, 0)
        return if (inputDate.after(today.time)) today.time else inputDate
    }

    private fun getWeekRange(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Apply offset in days
        calendar.add(Calendar.DAY_OF_YEAR, weekOffsetDays)
        calendar.time = capToToday(calendar.time)

        val startDate = dateFormat.format(calendar.time)
        endDateAPI = startDate

        calendar.add(Calendar.DAY_OF_YEAR, -6)
        val endDate = dateFormat.format(calendar.time)
        startDateAPI = endDate

        return "$endDate - $startDate"
    }

    private fun getMonthRange(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Current date with offset applied
        calendar.add(Calendar.MONTH, monthOffset)
        val endDate = dateFormat.format(calendar.time)
        endDateAPI = endDate

        // Move back 1 month from that point
        calendar.add(Calendar.MONTH, -1)
        val startDate = dateFormat.format(calendar.time)
        startDateAPI = startDate

        return "$startDate - $endDate"
    }

    private fun getSixMonthRange(): String {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val calendar = Calendar.getInstance()

        // Current date with 6-month offset applied
        calendar.add(Calendar.MONTH, sixMonthOffset * 6)
        val endDate = dateFormat.format(calendar.time)
        endDateAPI = endDate

        // Go back 6 months from that point
        calendar.add(Calendar.MONTH, -6)
        val startDate = dateFormat.format(calendar.time)
        startDateAPI = startDate

        return "$startDate - $endDate"
    }


    fun plotGraph2(
        graphData: FacialScanReportData,
        binding: ActivityFacialScanReportDetailsBinding,
        duration: String
    ) {
        val lineChart = binding.heartRateChart
        val entries = mutableListOf<Entry>()
        val xAxisLabels = mutableListOf<String>()

        val sdfInput = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
        sdfInput.timeZone = TimeZone.getTimeZone("UTC")

        val dateList = mutableListOf<Date>()
        val parsedData = graphData.data?.mapNotNull {
            val date = it.createdAt?.let { sdfInput.parse(it) }
            if (date != null && it.value != null) {
                dateList.add(date)
                Pair(date, it.value!!.toFloat())
            } else null
        } ?: emptyList()

        when (duration.lowercase(Locale.ROOT)) {
            "week" -> {
                val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                // Parse the selected week range start and end dates
                val startDate = dateFormat.parse(startDateAPI)
                val endDate = dateFormat.parse(endDateAPI)

                if (startDate != null && endDate != null) {
                    val calendar = Calendar.getInstance().apply {
                        time = startDate
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }

                    // Pre-process data for faster lookups
                    val dataMap = parsedData.associateBy {
                        val cal = Calendar.getInstance().apply { time = it.first }
                        Pair(cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR))
                    }

                    val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                    var currentDayIndex = 0

                    // Loop from startDate to endDate (inclusive)
                    while (!calendar.time.after(endDate)) {
                        val day = calendar.time
                        val cal = Calendar.getInstance().apply { time = day }
                        val key = Pair(cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR))
                        val value = dataMap[key]?.second ?: 0f

                        entries.add(Entry(currentDayIndex.toFloat(), value))
                        xAxisLabels.add(dayFormat.format(day))

                        Log.d("WeekData", "Day: ${sdfInput.format(day)}, Value: $value")

                        calendar.add(Calendar.DAY_OF_MONTH, 1)
                        currentDayIndex++
                    }
                } else {
                    Log.e("WeekData", "Failed to parse startDateAPI or endDateAPI.")
                }
            }
            /*"week" -> {
                val today = Calendar.getInstance().apply {
                    time = Date() // Current date/time
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                val calendar = Calendar.getInstance().apply {
                    time = today.time
                    add(
                        Calendar.DAY_OF_MONTH,
                        -6
                    ) // Go back 6 days to get 7 days total (including today)
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Pre-process data for faster lookups
                val dataMap = parsedData.associateBy {
                    val cal = Calendar.getInstance().apply { time = it.first }
                    Pair(cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR))
                }

                val dayFormat =
                    SimpleDateFormat("EEE", Locale.getDefault()) // More informative format
                val xAxisPositions = mutableListOf<Float>()
                var currentDayIndex = 0

                // Loop through all 7 days
                repeat(7) {
                    val day = calendar.time
                    val cal = Calendar.getInstance().apply { time = day }
                    val key = Pair(cal.get(Calendar.YEAR), cal.get(Calendar.DAY_OF_YEAR))
                    val value = dataMap[key]?.second ?: 0f

                    entries.add(Entry(currentDayIndex.toFloat(), value))
                    xAxisLabels.add(dayFormat.format(day))

                    Log.d("DayComparison", "checking day: ${sdfInput.format(day)}")

                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                    currentDayIndex++
                }
            }*/
            "month" -> {
                val calendar = Calendar.getInstance().apply {
                    time = Date() // today
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // Go back 29 days to get 30 total (including today)
                calendar.add(Calendar.DAY_OF_MONTH, -29)

                // Normalize all parsedData timestamps to 00:00:00.000 and map for lookup
                val dateValueMap = parsedData.associate {
                    val cal = Calendar.getInstance().apply {
                        time = it.first
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    cal.time to it.second
                }

                val dayFormat = SimpleDateFormat("d MMM", Locale.getDefault())

                for (i in 0 until 30) {
                    val currentDay = calendar.time
                    val value = dateValueMap[currentDay] ?: 0f

                    entries.add(Entry(i.toFloat(), value))
                    xAxisLabels.add(dayFormat.format(currentDay))

                    calendar.add(Calendar.DAY_OF_MONTH, 1)
                }

                Log.d(
                    "MonthView",
                    "Plotted last 30 days: ${xAxisLabels.joinToString()}"
                )
            }


            /*"month" -> {
                // 1. Get today at midnight (00:00:00.000)
                val today = Calendar.getInstance().apply {
                    time = Date()
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // 2. Start from first day of current month (e.g., April 1)
                val calendar = Calendar.getInstance().apply {
                    time = today.time
                    set(Calendar.DAY_OF_MONTH, 1) // Reset to start of month
                }

                // 3. Pre-process data: Normalize timestamps to midnight & create lookup map
                val dateValueMap = parsedData.associate { entry ->
                    val cal = Calendar.getInstance().apply {
                        time = entry.first
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    cal.time to entry.second // Map normalized date -> value
                }

                // 4. Format for day labels (e.g., "19 Apr")
                val dayFormat = SimpleDateFormat("d MMM", Locale.getDefault())

                // 5. Generate entries for each day of the month
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                for (day in 1..daysInMonth) {
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    val currentDay = calendar.time

                    // 6. Get value for this day (0f if no data)
                    val dayValue = dateValueMap[currentDay] ?: 0f

                    // 7. Add to chart (x-position = day-1, e.g., April 1 = 0, April 2 = 1)
                    entries.add(Entry((day - 1).toFloat(), dayValue))
                    xAxisLabels.add(dayFormat.format(currentDay))
                }

                // DEBUG: Verify April 19 is included
                Log.d(
                    "MonthView",
                    "All dates in map: ${dateValueMap.keys.map { dayFormat.format(it) }}"
                )
            }*/
            /*"month" -> {
                // 1. Get today at midnight (00:00:00.000)
                val today = Calendar.getInstance().apply {
                    time = Date()
                    set(Calendar.HOUR_OF_DAY, 0)
                    set(Calendar.MINUTE, 0)
                    set(Calendar.SECOND, 0)
                    set(Calendar.MILLISECOND, 0)
                }

                // 2. Start from first day of current month (e.g., April 1)
                val calendar = Calendar.getInstance().apply {
                    time = today.time
                    set(Calendar.DAY_OF_MONTH, 1) // Reset to start of month
                }

                // 3. Pre-process data: Normalize timestamps to midnight & create lookup map
                val dateValueMap = parsedData.associate { entry ->
                    val cal = Calendar.getInstance().apply {
                        time = entry.first
                        set(Calendar.HOUR_OF_DAY, 0)
                        set(Calendar.MINUTE, 0)
                        set(Calendar.SECOND, 0)
                        set(Calendar.MILLISECOND, 0)
                    }
                    cal.time to entry.second // Map normalized date -> value
                }

                // 4. Format for day labels (e.g., "19 Apr")
                val dayFormat = SimpleDateFormat("d MMM", Locale.getDefault())

                // 5. Generate entries for each day of the month
                val daysInMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH)
                for (day in 1..daysInMonth) {
                    calendar.set(Calendar.DAY_OF_MONTH, day)
                    val currentDay = calendar.time

                    // 6. Get value for this day (0f if no data)
                    val dayValue = dateValueMap[currentDay] ?: 0f

                    // 7. Add to chart (x-position = day-1, e.g., April 1 = 0, April 2 = 1)
                    entries.add(Entry((day - 1).toFloat(), dayValue))
                    xAxisLabels.add(dayFormat.format(currentDay))
                }

                // DEBUG: Verify April 19 is included
                Log.d(
                    "MonthView",
                    "All dates in map: ${dateValueMap.keys.map { dayFormat.format(it) }}"
                )
            }*/
            // --- 6 Months: Group by month and average values ---
            "6months" -> {
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())

                val monthGrouped = parsedData.groupBy {
                    monthFormat.format(it.first)
                }

                val sortedMonths = monthGrouped.keys.sortedBy {
                    monthFormat.parse(it)
                }

                sortedMonths.forEachIndexed { index, month ->
                    val values = monthGrouped[month]?.map { it.second } ?: emptyList()
                    val avg = if (values.isNotEmpty()) values.average().toFloat() else 0f
                    entries.add(Entry(index.toFloat(), avg))
                    xAxisLabels.add(month)
                }
            }
        }

        val colorRange1 = if (graphData.data?.size!! > 0) graphData.data?.get(0) else null
        val colorRange2 = if (graphData.data?.size!! > 1) graphData.data?.get(1) else null
        val colorRange3 = if (graphData.data?.size!! > 2) graphData.data?.get(2) else null


        // Chart setup
        val circleColors = entries.map { entry ->
            var color = Color.parseColor("#05AB26")
            if (colorRange1 != null && entry.y >= colorRange1.lowerRange!! && entry.y <= colorRange1.upperRange!!)
                color =
                    runCatching { Color.parseColor("#${colorRange1.colour}") }.getOrElse { Color.GREEN }
            else if (colorRange2 != null && entry.y >= colorRange2.lowerRange!! && entry.y <= colorRange2.upperRange!!)
                color =
                    runCatching { Color.parseColor("#${colorRange2.colour}") }.getOrElse { Color.GREEN }
            else if (colorRange3 != null && entry.y >= colorRange3.lowerRange!! && entry.y <= colorRange3.upperRange!!)
                color =
                    runCatching { Color.parseColor("#${colorRange3.colour}") }.getOrElse { Color.GREEN }
            if (entry.y == 0f) Color.TRANSPARENT else color
        }

        val modifiedEntries = entries.map {
            if (it.y == 0f) Entry(it.x, Float.NaN) else it
        }

        val dataSet = LineDataSet(modifiedEntries, "Data Set").apply {
            color = Color.parseColor("#05AB26")
            setCircleColors(circleColors)
            lineWidth = 2f
            circleRadius = 4f
            valueTextSize = 10f
            mode = LineDataSet.Mode.LINEAR
            setDrawValues(false)
            setDrawHighlightIndicators(false)
        }

        val lineData = LineData(dataSet)
        lineChart.data = lineData

        // X-Axis config
        lineChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            valueFormatter = IndexAxisValueFormatter(xAxisLabels)
            setDrawGridLines(false)
            textColor = Color.BLACK
            labelRotationAngle = 0f
        }

        // Y-Axis config
        graphData.yAxisValue?.let { yValues ->
            lineChart.axisLeft.apply {
                axisMinimum = yValues.minOrNull()?.toFloat() ?: 0f
                axisMaximum = yValues.maxOrNull()?.toFloat() ?: 100f
                setLabelCount(5, true)
                textColor = Color.BLACK
            }
        }

        lineChart.axisRight.isEnabled = false

        // Final chart styling
        lineChart.apply {
            description.isEnabled = false
            setTouchEnabled(true)
            setPinchZoom(true)
            animateX(1000)
            legend.textColor = Color.BLACK
            invalidate()
        }
    }

    fun clearGraph(binding: ActivityFacialScanReportDetailsBinding) {
        val lineChart = binding.heartRateChart
        lineChart.data = null
        lineChart.xAxis.valueFormatter = IndexAxisValueFormatter(emptyList())
        lineChart.invalidate()
    }


    fun formatIsoDateToReadable(input: String): String {
        val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.ENGLISH)
        inputFormat.timeZone = TimeZone.getTimeZone("UTC")

        val outputFormat = SimpleDateFormat("dd MMM, yyyy", Locale.ENGLISH)
        val date = inputFormat.parse(input)
        return outputFormat.format(date!!)
    }

    private fun getReportIconByType(type: String): Int {
        return when (type) {
            "BMI_CALC" -> R.drawable.ic_db_report_bmi
            "BP_RPP" -> R.drawable.ic_db_report_cardiak_workload
            "BP_SYSTOLIC" -> R.drawable.ic_db_report_bloodpressure
            "BP_DIASTOLIC" -> R.drawable.ic_db_report_bloodpressure
            "BP_CVD" -> R.drawable.ic_db_report_cvdrisk
            "MSI" -> R.drawable.ic_db_report_stresslevel
            "BR_BPM" -> R.drawable.ic_db_report_respiratory_rate
            "HRV_SDNN" -> R.drawable.ic_db_report_heart_variability
            "HEALTH_SCORE" -> R.drawable.ic_wellness_man
            else -> R.drawable.ic_db_report_heart_rate
        }
    }

    private fun handleContinueWatchUI(facialReportResponseNew: FacialScanReportDataWrapper?) {
        if (facialReportResponseNew?.recommendation?.isNotEmpty() == true) {
            binding.cardViewRecommended.visibility = View.VISIBLE
            val adapter =
                HealthCamRecommendationAdapter(this, facialReportResponseNew.recommendation!!)
            val horizontalLayoutManager =
                LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
            binding.recyclerViewContinue.setLayoutManager(horizontalLayoutManager)
            binding.recyclerViewContinue.setAdapter(adapter)
        }else{
            binding.cardViewRecommended.visibility = View.GONE
        }
    }
}