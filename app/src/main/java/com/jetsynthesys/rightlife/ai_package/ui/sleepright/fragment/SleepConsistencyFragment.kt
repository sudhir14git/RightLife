package com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.Paint
import android.graphics.RectF
import android.os.Build
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.annotation.RequiresApi
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.SleepConsistencyDetail
import com.jetsynthesys.rightlife.ai_package.model.SleepConsistencyResponse
import com.jetsynthesys.rightlife.ai_package.model.SleepDetails
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.databinding.FragmentSleepConsistencyBinding
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import kotlinx.coroutines.runBlocking
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale
import kotlin.math.min
import kotlin.math.roundToInt

class SleepConsistencyFragment : BaseFragment<FragmentSleepConsistencyBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSleepConsistencyBinding
        get() = FragmentSleepConsistencyBinding::inflate
    var snackbar: Snackbar? = null

    private lateinit var barChart: SleepGraphView
    private lateinit var radioGroup: RadioGroup
    private lateinit var progressDialog: ProgressDialog
    private lateinit var sleepConsistencyResponse: SleepConsistencyResponse
    private lateinit var btnPrevious: ImageView
    private lateinit var btnNext: ImageView
    private lateinit var dateRangeText: TextView
    private lateinit var averageBedTime: TextView
    private lateinit var averageSleepTime: TextView
    private lateinit var averageWakeupTime: TextView
    private lateinit var sleepTime: TextView
    private lateinit var sleepDate: TextView
    private lateinit var consistencyTitle: TextView
    private lateinit var consistencyMessage: TextView
    private lateinit var percentageIcon: ImageView
    private lateinit var percentageText: TextView
    private lateinit var consistencyCard: CardView
    private lateinit var consistencyNoDataCard: CardView
    private var currentTab = 0 // 0 = Week, 1 = Month, 2 = 6 Months
    private var currentDateWeek: LocalDate = LocalDate.now() // today
    private var currentDateMonth: LocalDate = LocalDate.now() // today
    private var mStartDate = ""
    private var mEndDate = ""

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundResource(R.drawable.sleep_stages_bg)

        barChart = view.findViewById(R.id.sleepConsistencyChart)
        radioGroup = view.findViewById(R.id.tabGroup)
        btnPrevious = view.findViewById(R.id.btn_prev)
        btnNext = view.findViewById(R.id.btn_next)
        dateRangeText = view.findViewById(R.id.tv_selected_date)
        averageBedTime = view.findViewById(R.id.tv_average_bed_time)
        averageSleepTime = view.findViewById(R.id.tv_average_sleep_time)
        averageWakeupTime = view.findViewById(R.id.tv_average_wakeup_time)
        consistencyTitle = view.findViewById(R.id.consistency_title)
        consistencyMessage = view.findViewById(R.id.consistency_message)
        percentageIcon = view.findViewById(R.id.percentage_icon)
        percentageText = view.findViewById(R.id.percentage_text)
        consistencyCard = view.findViewById(R.id.lyt_consistency_card)
        consistencyNoDataCard = view.findViewById(R.id.lyt_consistency_nocard)
        sleepTime = view.findViewById(R.id.tv_sleep_time)
        sleepDate = view.findViewById(R.id.tv_date)
        consistencyNoDataCard = view.findViewById(R.id.lyt_consistency_nocard)
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
                    fetchSleepData(endDate,"weekly")
                }
                R.id.rbMonth -> {
                    currentTab = 1
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val startDate = getOneMonthEarlierDate().format(dateFormatter)
                    val endDate = getTodayDate().format(dateFormatter)
                    val formatter = DateTimeFormatter.ofPattern("d MMM")
                    dateRangeText.text = "${getOneMonthEarlierDate().format(formatter)} - ${getTodayDate().format(formatter)}, ${currentDateMonth.year}"
                    fetchSleepData(endDate,"monthly")
                }
                R.id.rbSixMonths -> {
                    currentTab = 2
                    val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
                    val startDate = getSixMonthsEarlierDate().format(dateFormatter)
                    val endDate = getTodayDate().format(dateFormatter)
                    val formatter = DateTimeFormatter.ofPattern("d MMM")
                    dateRangeText.text = "${getSixMonthsEarlierDate().format(formatter)} - ${getTodayDate().format(formatter)}, ${currentDateMonth.year}"
                    fetchSleepData(endDate,"monthly")
                }
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
            }        }
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

    @RequiresApi(Build.VERSION_CODES.VANILLA_ICE_CREAM)
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

    private fun loadWeekData() {
        val endOfWeek = currentDateWeek
        val startOfWeek = endOfWeek.minusDays(6)
        val formatter = DateTimeFormatter.ofPattern("d MMM")
        dateRangeText.text = "${startOfWeek.format(formatter)} - ${endOfWeek.format(formatter)}, ${currentDateWeek.year}"
        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fetchSleepData(endOfWeek.format(formatter1),"weekly")
    }

    private fun loadMonthData() {
        val endOfMonth = currentDateMonth
        val startOfMonth = endOfMonth.minusMonths(1)
        val formatter = DateTimeFormatter.ofPattern("d MMM")
        dateRangeText.text = "${startOfMonth.format(formatter)} - ${endOfMonth.format(formatter)}, ${currentDateMonth.year}"
        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fetchSleepData(endOfMonth.format(formatter1),"monthly")
    }

    private fun loadSixMonthsData() {
        val startDate = getSixMonthsEarlierDate()
        val endDate = getTodayDate()
        val formatter = DateTimeFormatter.ofPattern("MMM yyyy")
        dateRangeText.text = "${startDate.format(formatter)} - ${endDate.format(formatter)}"
        val formatter1 = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        fetchSleepData(endDate.format(formatter1),"monthly")
    }

    private fun fetchSleepData(mEndDate: String,period: String) {
        progressDialog.show()
        val userid = SharedPreferenceManager.getInstance(requireActivity()).userId ?: ""
        val source = "android"
        val date = mEndDate
        val call = ApiClient.apiServiceFastApi.fetchSleepConsistencyDetail(userid, source, period,mEndDate)
        call.enqueue(object : Callback<SleepConsistencyResponse> {
            override fun onResponse(call: Call<SleepConsistencyResponse>, response: Response<SleepConsistencyResponse>) {
                progressDialog.dismiss()
                if (response.isSuccessful) {
                    if (response.body()!=null) {
                        sleepConsistencyResponse = response.body()!!
                        if (sleepConsistencyResponse.message != "No sleep consistency data found.") {
                            consistencyTitle.visibility = View.VISIBLE
                            consistencyMessage.visibility = View.VISIBLE
                            consistencyCard.visibility = View.VISIBLE
                            consistencyNoDataCard.visibility = View.GONE
                            if (sleepConsistencyResponse.data?.sleepDetails?.isNotEmpty() == true) {
                                sleepConsistencyResponse.data?.sleepDetails?.let {
                                    setData(it)
                                }
                            }
                            sleepTime.text = convertDecimalHoursToHrMinFormat(sleepConsistencyResponse.data?.sleepConsistencyDetail?.averageSleepDurationHours!!)
                            sleepDate.text = convertDateToNormalDate(sleepConsistencyResponse.data?.sleepDetails?.getOrNull(sleepConsistencyResponse.data?.sleepDetails?.size?.minus(1) ?: 0)?.date!!)
                            setSleepAverageData(sleepConsistencyResponse.data?.sleepConsistencyDetail)
                            consistencyTitle.setText(sleepConsistencyResponse.data?.sleepInsightDetail?.title)
                            consistencyMessage.setText(sleepConsistencyResponse.data?.sleepInsightDetail?.message)
                            if (sleepConsistencyResponse.data?.progress_detail?.progress_sign == "plus"){
                              //  percentageIcon.visibility = View.VISIBLE
                                percentageIcon.setImageResource(R.drawable.ic_up)
                             //   percentageText.visibility = View.VISIBLE
                                percentageText.text = " "+ sleepConsistencyResponse.data?.progress_detail?.progress_percentage + " past week"
                            }else{
                           //     percentageIcon.visibility = View.VISIBLE
                           //     percentageText.visibility = View.VISIBLE
                                percentageIcon.setImageResource(R.drawable.ic_down)
                                percentageIcon.setBackgroundColor(resources.getColor(R.color.red))
                                percentageText.text = " "+ sleepConsistencyResponse.data?.progress_detail?.progress_percentage + " past week"
                            }
                        }else{
                            consistencyCard.visibility = View.GONE
                            consistencyNoDataCard.visibility = View.VISIBLE
                            consistencyTitle.visibility = View.GONE
                            consistencyMessage.visibility = View.GONE
                        }
                    }

                }else if(response.code() == 400){
                    progressDialog.dismiss()
                    Toast.makeText(activity, "Record Not Found", Toast.LENGTH_SHORT).show()
                }
            }
            override fun onFailure(call: Call<SleepConsistencyResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }

    fun convertDateToNormalDate(dateStr: String): String{
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        val outputFormatter = DateTimeFormatter.ofPattern("EEEE dd MMM, yyyy", Locale.ENGLISH)
        val date = LocalDate.parse(dateStr, inputFormatter)
        val formatted = date.format(outputFormatter)
        return formatted
    }

    private fun setSleepAverageData(detail: SleepConsistencyDetail?) {
        if (detail?.averageSleepDurationHours != 0.0 && detail?.averageSleepDurationHours != null) {
            averageBedTime.setText(convertDecimalHoursToHrMinFormat(detail.averageSleepDurationHours!!))
        }
        if (detail?.averageSleepStartTime?.isNotEmpty() == true && detail.averageWakeTime?.isNotEmpty() == true) {
            val sleepTime = convertUtcToLocal(detail.averageSleepStartTime!!)
            val wakeTime = convertUtcToLocal(detail.averageWakeTime!!)
            averageSleepTime.setText(convertTo12HourTime(sleepTime))
            averageWakeupTime.setText(convertTo12HourTime(wakeTime))
        }
    }

    private fun convertUtcToLocal(utcTimeString: String): String {
        // Parse the UTC date-time string
        val utcFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val utcLocalDateTime = LocalDateTime.parse(utcTimeString, utcFormatter)
        // Attach UTC time zone
        val utcZoned = utcLocalDateTime.atZone(ZoneId.of("UTC"))
        // Convert to local time zone
        val localZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault())
        // Format the local time as desired
        val outputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return localZoned.format(outputFormatter)
    }

    fun convertTo12HourTime(timeStr: String): String {
        val inputFormat = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val outputFormat = DateTimeFormatter.ofPattern("hh:mm a") // 12-hour with AM/PM
        val dateTime = LocalDateTime.parse(timeStr, inputFormat)
        return dateTime.format(outputFormat)
    }

    fun convertTo12HourLocalTime(input: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val localDateTime = LocalDateTime.parse(input, inputFormatter)
        val utcZoned = localDateTime.atZone(ZoneId.of("UTC"))
        val localZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault())
        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a") // 12-hour with AM/PM
        return localZoned.format(outputFormatter)
    }

    private fun formatUtcToLocal12HourTime(utcDateTime: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val utcLocalDateTime = LocalDateTime.parse(utcDateTime, inputFormatter)
        val utcZoned = utcLocalDateTime.atZone(ZoneId.of("UTC"))
        val localZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault())
        // Output in 12-hour time format: 07:57 PM
        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a")
        return localZoned.format(outputFormatter)
    }

    fun convertTo12HourFormat(datetimeStr: String): String {
        val inputFormatter = DateTimeFormatter.ISO_LOCAL_DATE_TIME
        val outputFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val dateTime = LocalDateTime.parse(datetimeStr, inputFormatter)
        return dateTime.format(outputFormatter)
    }

    private fun convertDecimalHoursToHrMinFormat(hoursDecimal: Double): String {
        val totalMinutes = (hoursDecimal * 60).toInt()
        val hours = totalMinutes / 60
        val minutes = totalMinutes % 60
        return String.format("%02dhr %02dmins", hours, minutes)
    }

    fun setData(parseSleepData: ArrayList<SleepDetails>) = runBlocking {
        /*val result = async {
            parseSleepData(parseSleepData)
        }.await()
        barChart.setSleepData(result)*/
        val entries = parseSleepData.toSleepEntries()               // skips the 0-hour rows
        barChart.setSleepData(entries)
        barChart.setOnBarClickListener { entry ->
            sleepDate.text = entry.endLocal.format(DateTimeFormatter.ofPattern("EEEE d MMM, yyyy"))
            val dur = Duration.ofMinutes((entry.durationHrs * 60).roundToInt().toLong())
            val hrs = dur.toHours()
            val mins = dur.minusHours(hrs).toMinutes()
            sleepTime.text = "${hrs}hr ${mins}mins"
        }
    }

   /* private fun parseSleepData(sleepDetails: List<SleepDetails>): List<SleepEntry> {
        val sleepSegments = mutableListOf<SleepEntry>()
        for (sleepEntry in sleepDetails) {
            val startTime = sleepEntry.sleepStartTime ?: ""
            val endTime = sleepEntry.sleepEndTime ?: ""
            val duration = sleepEntry.sleepDurationHours?.toFloat()!!
            sleepSegments.add(SleepEntry(startTime, endTime, duration))
        }
        return sleepSegments
    }*/

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }
}

fun interface OnBarClickListener {
    fun onBarClick(entry: SleepEntry)
}

class SleepGraphView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    /* ── paints (unchanged from previous version) ─────────────────── */
    private val gridPaint       = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E5E5E5"); strokeWidth = dp(1f) }
    private val barPaint        = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#70A1FF"); style = Paint.Style.FILL }
    private val lastBarPaint    = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#007BFF"); style = Paint.Style.FILL }
    private val labelPaint      = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#7A7A7A"); textSize = sp(12f); textAlign = Paint.Align.CENTER }
    private val hourPaint       = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#9B9B9B"); textSize = sp(11f); textAlign = Paint.Align.LEFT }
    private val weekDividerPaint= Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#E5E5E5"); strokeWidth = dp(1f) }
    private val todayDashPaint  = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.parseColor("#9B9B9B"); strokeWidth = dp(1f); pathEffect = DashPathEffect(floatArrayOf(8f, 8f), 0f) }

    /* ── data ─────────────────────────────────────────────────────── */
    private val entries = mutableListOf<SleepEntry>()
    fun setSleepData(list: List<SleepEntry>) {
        entries.clear()
        entries.addAll(list.sortedBy { it.startLocal })
        invalidate()
    }

    /* register an optional listener */
    private var barClickListener: OnBarClickListener? = null
    fun setOnBarClickListener(l: OnBarClickListener?) { barClickListener = l }

    /* keep track of each bar’s hit-box */
    private data class BarHit(val rect: RectF, val entry: SleepEntry)
    private val barHits = mutableListOf<BarHit>()

    /* paddings */
    private val leftPad     = dp(18f)
    private val topPad      = dp(12f)
    private val bottomPadW  = dp(40f)
    private val bottomPadM  = dp(36f)
    private var selectedEntry: SleepEntry? = null

    private val selectedBarPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#007BFF")
        style = Paint.Style.FILL
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        if (entries.isEmpty()) return

        barHits.clear()                       // rebuild hit-boxes each frame
        if (entries.size <= 7) drawWeek(canvas) else drawMonth(canvas)
    }

    /* ───────────────────────── WEEK ─────────────────────────────── */
    private fun drawWeek(c: Canvas) {
        val chartH = height - topPad - bottomPadW
        val chartW = width  - leftPad
        val slotW  = chartW / entries.size
        val barW   = slotW * .6f
        val radius = dp(12f)

        drawHourGrid(c, chartH)


        entries.forEachIndexed { i, e ->
            val left = leftPad + i * slotW + (slotW - barW) / 2
            val start = startFrac(e)
            val duration = e.durationHrs

            val topY = topPad + chartH / 16f * start
            val botY = min(topY + chartH / 16f * duration, topPad + chartH)
          //  val paint = if (i == entries.lastIndex) lastBarPaint else barPaint
            val paint = when {
                e == selectedEntry     -> selectedBarPaint   // NEW
               // i == entries.lastIndex -> lastBarPaint
                else                       -> barPaint
            }
            val rect = RectF(left, topY, left + barW, botY)
            c.drawRoundRect(rect, radius, radius, paint)
            barHits += BarHit(rect, e)        // store for click detection

            val day  = e.endLocal.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
            val date = e.endLocal.format(DateTimeFormatter.ofPattern("d MMM"))
            val cx   = rect.centerX()
            c.drawText(day,  cx, height - bottomPadW / 1.6f, labelPaint)
            c.drawText(date, cx, height - dp(4f),           labelPaint)
        }
    }

    /* ───────────────────────── MONTH ────────────────────────────── */
    private fun drawMonth(c: Canvas) {
        val chartH = height - topPad - bottomPadM
        val chartW = width  - leftPad
        val weekCnt = (entries.size + 6) / 7
        val gap     = dp(8f)
        val dayW    = (chartW - gap * (weekCnt - 1)) / entries.size
        val barW    = dayW * .5f
        val radius  = dp(8f)

        drawHourGrid(c, chartH)

        /* week guides */
        repeat(weekCnt - 1) { wk ->
            val x = leftPad + (wk + 1) * 7 * dayW + wk * gap
            c.drawLine(x, topPad, x, topPad + chartH, weekDividerPaint)
        }

        /* bars */
        entries.forEachIndexed { i, e ->
            val wk   = i / 7
            val left = leftPad + i * dayW + wk * gap + (dayW - barW) / 2
            val start = startFrac(e)
            val duration = e.durationHrs

            val topY = topPad + chartH / 16f * start
            val botY = min(topY + chartH / 16f * duration, topPad + chartH)
         //   val paint = if (i == entries.lastIndex) lastBarPaint else barPaint
            val paint = when {
                e == selectedEntry     -> selectedBarPaint   // NEW
                // i == entries.lastIndex -> lastBarPaint
                else                       -> barPaint
            }
            val rect = RectF(left, topY, left + barW, botY)
            c.drawRoundRect(rect, radius, radius, paint)
            barHits += BarHit(rect, e)
        }

        /* dashed marker */
        entries.lastOrNull()?.let {
            val idx = entries.lastIndex
            val wk  = idx / 7
            val x   = leftPad + idx * dayW + wk * gap + dayW / 2
            c.drawLine(x, topPad, x, topPad + chartH, todayDashPaint)
        }

        /* week-range labels */
        repeat(weekCnt) { wk ->
            val first = wk * 7
            val last  = min(first + 6, entries.lastIndex)
            val fD    = entries[first].startLocal.toLocalDate()
            val lD    = entries[last ].startLocal.toLocalDate()
            val range = "${fD.dayOfMonth}-${lD.dayOfMonth}"
            val month = fD.month.getDisplayName(TextStyle.SHORT, Locale.getDefault())

            val blockL = leftPad + first * dayW + wk * gap
            val blockR = leftPad + (last + 1) * dayW + wk * gap
            val cx     = (blockL + blockR) / 2
            c.drawText(range, cx, height - bottomPadM / 1.6f, labelPaint)
            c.drawText(month, cx, height - dp(4f),            labelPaint)
        }
    }

    /* ───────── helper: hour grid + AM/PM labels ─────────── */
    private fun drawHourGrid(c: Canvas, chartH: Float) {
        for (step in 0..16 step 2) {
            val y = topPad + chartH / 16f * step  // Now spans full 16 hours

            // Draw grid line
            c.drawLine(leftPad, y, width.toFloat(), y, gridPaint)

            // Compute and draw time label correctly
            val h24 = (20 + step) % 24
            val h12 = if (h24 == 0) 12 else if (h24 > 12) h24 - 12 else h24
            val ampm = if (h24 < 12) "am" else "pm"

            c.drawText("$h12", dp(4f), y + hourPaint.textSize / 3, hourPaint)
            c.drawText(ampm, dp(4f), y + hourPaint.textSize * 1.4f, hourPaint)
        }
    }

    /** Offset (0‥12) from the 20:00 grid line where the bar starts. */
    private fun startFrac(e: SleepEntry): Float {
        // minutes since midnight, including seconds as a fraction
        val minutesOfDay =
            e.startLocal.hour * 60 +
                    e.startLocal.minute +
                    e.startLocal.second / 60f

        // Offset from 20:00 (8 PM)
        val offsetMin = (minutesOfDay - 20 * 60 + 24 * 60) % (24 * 60)
        return offsetMin / 60f  // Ranges from 0.0 (8 PM) to 16.0 (12 PM)
    }

    /* ───────── TOUCH HANDLING ───────── */
    /*override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_UP -> {
                val x = e.x
                val y = e.y
                barHits.firstOrNull { it.rect.contains(x, y) }?.let { hit ->
                    barClickListener?.onBarClick(hit.entry) ?: showToast(hit.entry)
                    return true
                }
            }
        }
        return true            // allow further events if needed
    }*/
    override fun onTouchEvent(e: MotionEvent): Boolean {
        when (e.action) {
            MotionEvent.ACTION_UP -> {
                barHits.firstOrNull { it.rect.contains(e.x, e.y) }?.let { hit ->
                    selectedEntry = hit.entry          // NEW
                    invalidate()                       // NEW
                    barClickListener?.onBarClick(hit.entry) ?: showToast(hit.entry)
                    return true
                }
                // tap in empty space → clear selection
                if (selectedEntry != null) {
                    selectedEntry = null
                    invalidate()
                }
            }
        }
        return true
    }

    private fun showToast(entry: SleepEntry) {
        // "Sunday 9 Feb, 2025"
        val dateStr = entry.startLocal.format(
            DateTimeFormatter.ofPattern("EEEE d MMM, yyyy", Locale.getDefault())
        )
        val dur = Duration.ofMinutes((entry.durationHrs * 60).roundToInt().toLong())
        val hrs = dur.toHours()
        val mins = dur.minusHours(hrs).toMinutes()
        //val msg = "$dateStr · ${hrs}hr ${mins}mins"
    }

    /* px helpers */
    private fun dp(v: Float) = v * resources.displayMetrics.density
    private fun sp(v: Float) = v * resources.displayMetrics.scaledDensity
}

/* ── EXTENSION: build list from your raw SleepDetails ────────────────────
   Only nights with >0.0 duration will be graphed.                         */
fun List<SleepDetails>.toSleepEntries(): List<SleepEntry> =
    filter { it.sleepDurationHours!! >= 0.0 }           // ignore the zero-length nights
        .map { SleepEntry.fromUtcIso(it.sleepStartTime!!,it.sleepEndTime!!, it.sleepDurationHours!!) }

data class SleepEntry(
    val startLocal: LocalDateTime,
    val endLocal: LocalDateTime,
    val durationHrs: Float
) {
    companion object {

        /**
         * Create a SleepEntry from a **UTC** ISO-8601 timestamp, e.g.
         *  • 2025-06-22T02:11:00Z
         *  • 2025-06-22T02:11:00+00:00
         *
         * If the string has *no* 'Z' or offset we assume it is UTC, append 'Z',
         * then convert to the device’s [ZoneId.systemDefault].
         */
        fun fromUtcIso(startutcIso: String, endutcIso: String, duration: Double): SleepEntry {
            val fixed = startutcIso.takeIf { it.endsWith('Z') || it.contains('+') || it.contains('-') }
                ?: "${startutcIso}Z"

            val instant: Instant = runCatching { Instant.parse(fixed) }
                .getOrElse {
                    // fallback: parse as OffsetDateTime without offset → add UTC offset
                    val noZone = LocalDateTime.parse(startutcIso)
                    noZone.toInstant(ZoneOffset.UTC)
                }

            val local = instant.atZone(ZoneId.systemDefault()).toLocalDateTime()

            val fixedend = endutcIso.takeIf { it.endsWith('Z') || it.contains('+') || it.contains('-') }
                ?: "${endutcIso}Z"
            val instantend: Instant = runCatching { Instant.parse(fixedend) }
                .getOrElse {
                    // fallback: parse as OffsetDateTime without offset → add UTC offset
                    val noZone = LocalDateTime.parse(endutcIso)
                    noZone.toInstant(ZoneOffset.UTC)
                }
            val localend = instantend.atZone(ZoneId.systemDefault()).toLocalDateTime()
            return SleepEntry(local,localend, duration.toFloat())
        }
    }
}
