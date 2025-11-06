package com.jetsynthesys.rightlife.ai_package.ui.sleepright.fragment

import android.app.ProgressDialog
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.jetsynthesys.rightlife.R
import com.jetsynthesys.rightlife.ai_package.base.BaseFragment
import com.jetsynthesys.rightlife.ai_package.data.repository.ApiClient
import com.jetsynthesys.rightlife.ai_package.model.SleepStageData
import com.jetsynthesys.rightlife.ai_package.model.SleepStageResponse
import com.jetsynthesys.rightlife.databinding.FragmentSleepStagesBinding
import com.google.android.material.snackbar.Snackbar
import com.jetsynthesys.rightlife.ai_package.model.SleepStageAllData
import com.jetsynthesys.rightlife.ai_package.ui.home.HomeBottomTabFragment
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.customprogressbar.AwakeProgressBar
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.customprogressbar.CoreProgressBar
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.customprogressbar.DeepprogressBar
import com.jetsynthesys.rightlife.ai_package.ui.sleepright.customprogressbar.RemProgressBar
import com.jetsynthesys.rightlife.ui.utility.SharedPreferenceManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.text.SimpleDateFormat
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale
import java.util.concurrent.TimeUnit
import kotlin.math.floor
import kotlin.math.max

class SleepStagesFragment : BaseFragment<FragmentSleepStagesBinding>() {

    private lateinit var customProgressBar: AwakeProgressBar
    private lateinit var transparentOverlay: View
    private lateinit var customProgressBarCardio: RemProgressBar
    private lateinit var transparentOverlayCardio: View
    private lateinit var customProgressBarLight: DeepprogressBar
    private lateinit var transparentOverlayLight: View
    private lateinit var customProgressBarFatBurn: CoreProgressBar
    private lateinit var transparentOverlayFatBurn: View
    private lateinit var sleepChart: SleepChartView
    private lateinit var progressDialog: ProgressDialog
    private lateinit var corePercent: TextView
    private lateinit var coreMinute: TextView
    private lateinit var deepPercent: TextView
    private lateinit var deepMinute: TextView
    private lateinit var awakePercent: TextView
    private lateinit var awakeMinute: TextView
    private lateinit var remPercent: TextView
    private lateinit var remMinute: TextView
    private lateinit var totalTime: TextView
    private lateinit var startTime: TextView
    private lateinit var endTime: TextView
    private lateinit var stageTitle: TextView
    private lateinit var stageMessage: TextView
    private lateinit var stageDataCard: CardView
    private lateinit var stageNoDataCard: CardView
    private lateinit var lytStageTitle: LinearLayout
    private lateinit var lytStagesList: ConstraintLayout
    val remData: ArrayList<Float> = arrayListOf()
    val awakeData: ArrayList<Float> = arrayListOf()
    val coreData: ArrayList<Float> = arrayListOf()
    val deepData: ArrayList<Float> = arrayListOf()
    val formatters = DateTimeFormatter.ISO_DATE_TIME

    private lateinit var sleepStageResponse: SleepStageResponse

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSleepStagesBinding
        get() = FragmentSleepStagesBinding::inflate
    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sleepChart = view.findViewById<SleepChartView>(R.id.sleepChart)

        progressDialog = ProgressDialog(activity)
        progressDialog.setTitle("Loading")
        progressDialog.setCancelable(false)

        fetchSleepData()

        customProgressBar = view.findViewById(R.id.customProgressBar)
        transparentOverlay = view.findViewById(R.id.transparentOverlay)
        customProgressBarCardio = view.findViewById(R.id.customProgressBarCardio)
        transparentOverlayCardio = view.findViewById(R.id.transparentOverlayCardio)
        customProgressBarLight = view.findViewById(R.id.customProgressBarLight)
        transparentOverlayLight = view.findViewById(R.id.transparentOverlayLight)
        customProgressBarFatBurn = view.findViewById(R.id.customProgressBarFatBurn)
        transparentOverlayFatBurn = view.findViewById(R.id.transparentOverlayFatBurn)
        lytStageTitle = view.findViewById(R.id.lyt_sleep_stage_title)
        lytStagesList = view.findViewById(R.id.lyt_sleep_stage_list)

        awakeMinute = view.findViewById(R.id.tv_sleep_awake_minute)
        awakePercent = view.findViewById(R.id.tv_sleep_awake_percent)
        coreMinute = view.findViewById(R.id.tv_sleep_core_minute)
        corePercent = view.findViewById(R.id.tv_sleep_core_percent)
        deepMinute = view.findViewById(R.id.tv_sleep_deep_minute)
        deepPercent = view.findViewById(R.id.tv_sleep_deep_percent)
        remMinute = view.findViewById(R.id.tv_sleep_rem_minute)
        remPercent = view.findViewById(R.id.tv_sleep_rem_percent)
        startTime = view.findViewById(R.id.tv_stage_start_time)
        endTime = view.findViewById(R.id.tv_stage_end_time)
        totalTime = view.findViewById(R.id.tv_stage_total_sleep)
        stageTitle = view.findViewById(R.id.tv_sleep_stage_title)
        stageMessage = view.findViewById(R.id.tv_sleep_stage_message)
        stageDataCard = view.findViewById(R.id.lyt_stage_card)
        stageNoDataCard = view.findViewById(R.id.lyt_stage_nocard)

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

        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
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

    fun convertTo12HourZoneFormat(input: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val outputFormatter = DateTimeFormatter.ofPattern("hh:mm a") // 12-hour format with AM/PM

        // Parse as LocalDateTime (no time zone info)
        val utcDateTime = LocalDateTime.parse(input, inputFormatter)

        // Convert to UTC ZonedDateTime
        val utcZoned = utcDateTime.atZone(ZoneId.of("UTC"))

        // Convert to system local time zone
        val localZoned = utcZoned.withZoneSameInstant(ZoneId.systemDefault())

        return outputFormatter.format(localZoned)
    }

    fun getCurrentDate(): String {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
        return LocalDate.now().format(formatter)
    }

    private fun fetchSleepData() {
        progressDialog.show()
        val userid =SharedPreferenceManager.getInstance(requireActivity()).userId
        val date = getCurrentDate()
        val source = "android"
        val call = ApiClient.apiServiceFastApi.fetchSleepStage(userid, source, date)
        call.enqueue(object : Callback<SleepStageResponse> {
            override fun onResponse(
                call: Call<SleepStageResponse>,
                response: Response<SleepStageResponse>
            ) {
                if (response.isSuccessful) {
                    progressDialog.dismiss()
                    sleepStageResponse = response.body()!!
                    if (sleepStageResponse.sleepStageAllData?.sleepStageData?.isNotEmpty() == true) {
                        stageDataCard.visibility = View.VISIBLE
                        stageNoDataCard.visibility = View.GONE
                        lytStagesList.visibility = View.VISIBLE
                        lytStageTitle.visibility = View.VISIBLE
                        sleepChart.setSleepData(sleepStageResponse.sleepStageAllData?.sleepStageData!!)
                        setProgressBarData(sleepStageResponse.sleepStageAllData)
                    }else{
                        stageDataCard.visibility = View.GONE
                        stageNoDataCard.visibility = View.VISIBLE
                        lytStagesList.visibility = View.GONE
                        lytStageTitle.visibility = View.GONE
                        progressDialog.dismiss()
                        Toast.makeText(activity, "Record Not Found", Toast.LENGTH_SHORT).show()
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
            override fun onFailure(call: Call<SleepStageResponse>, t: Throwable) {
                Log.e("Error", "API call failed: ${t.message}")
                Toast.makeText(activity, "Failure", Toast.LENGTH_SHORT).show()
                progressDialog.dismiss()
            }
        })
    }

    private fun setProgressBarData(data: SleepStageAllData?) {
        stageTitle.setText(data?.sleepInsightDetail?.title)
        stageMessage.setText(data?.sleepInsightDetail?.message)
        awakePercent.setText(""+ data?.sleepPercentages?.awake + "%")
        awakeMinute.setText(convertHoursToHHMMSS(data?.sleepSummary?.awake!!))
        corePercent.setText(""+ data?.sleepPercentages?.light + "%")
        coreMinute.setText(convertHoursToHHMMSS(data.sleepSummary?.light!!))
        deepPercent.setText(""+ data?.sleepPercentages?.deep + "%")
        deepMinute.setText(convertHoursToHHMMSS(data.sleepSummary?.deep!!))
        remPercent.setText(""+ data?.sleepPercentages?.rem + "%")
        remMinute.setText(convertHoursToHHMMSS(data.sleepSummary?.rem!!))
        totalTime.setText(convertDecimalHoursToHrMinFormat(data?.total_sleep_hours!!))
        startTime.setText(convertTo12HourZoneFormat(data.start_time!!))
        endTime.setText(convertTo12HourZoneFormat(data.end_time!!))

        customProgressBar.post {
            val progressBarWidth = customProgressBar.width
            val overlayWidth = (progressBarWidth*0.5).toInt()
            val layoutParams = transparentOverlay.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.width = overlayWidth
            transparentOverlay.layoutParams = layoutParams
        }

        customProgressBarCardio.post {
            val progressBarWidth = customProgressBarCardio.width
            val overlayWidth = (progressBarWidth*0.5).toInt()
            val layoutParams = transparentOverlayCardio.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.width = overlayWidth
            transparentOverlayCardio.layoutParams = layoutParams
        }

        customProgressBarLight.post {
            val progressBarWidth = customProgressBarLight.width
            val overlayWidth = (progressBarWidth*0.5).toInt()
            val layoutParams = transparentOverlayLight.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.width = overlayWidth
            transparentOverlayLight.layoutParams = layoutParams
        }

        customProgressBarFatBurn.post {
            val progressBarWidth = customProgressBarFatBurn.width
            val overlayWidth = (progressBarWidth*0.5).toInt()
            val layoutParams = transparentOverlayFatBurn.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.width = overlayWidth
            transparentOverlayFatBurn.layoutParams = layoutParams
        }
        customProgressBar.progress = data?.sleepPercentages?.awake!!.toFloat()
        customProgressBarLight.progress = data?.sleepPercentages?.deep!!.toFloat()
        customProgressBarFatBurn.progress = data?.sleepPercentages?.light!!.toFloat()
        customProgressBarCardio.progress = data?.sleepPercentages?.rem!!.toFloat()
    }

    fun convertTo12HourFormat(datetimeStr: String): String {
        val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        val outputFormatter = DateTimeFormatter.ofPattern("h:mm a")
        val dateTime = LocalDateTime.parse(datetimeStr, inputFormatter)
        return dateTime.format(outputFormatter)
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

    private fun convertMinutesToHHMMSS(minutes: Double): String {
        val totalSeconds = (minutes * 60).toInt()
        val hours = totalSeconds / 3600
        val minutesPart = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutesPart, seconds)
    }
    private fun convertHoursToHHMMSS(hoursInput: Double): String {
        val totalSeconds = (hoursInput * 3600).toInt()
        val hours = totalSeconds / 3600
        val minutesPart = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60
        return String.format("%02d:%02d:%02d", hours, minutesPart, seconds)
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }
}

class SleepChartView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sleepSegments = mutableListOf<Segment>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())

    private data class Segment(
        val position: Position,
        val widthFraction: Float,
        val color: Int
    )

    fun setSleepData(data: List<SleepStageData>) {
        sleepSegments.clear()
        if (data.isEmpty()) return
        // Parse timestamps and calculate total duration
        val parsedData = data.mapNotNull {
            try {
                val start = dateFormat.parse(it.startDatetime)?.time ?: return@mapNotNull null
                val end = dateFormat.parse(it.endDatetime)?.time ?: return@mapNotNull null
                val durationMinutes = TimeUnit.MILLISECONDS.toMinutes(end - start).toInt()
                Triple(it.recordType, durationMinutes, getPositionForRecordType(it.recordType!!))
            } catch (e: Exception) {
                null
            }
        }

        val totalMinutes = parsedData.sumOf { it.second }
        parsedData.forEach { (recordType, duration, position) ->
            val fraction = duration.toFloat() / max(totalMinutes, 1)
            val color = getColorForRecordType(recordType!!)
            sleepSegments.add(Segment(position, fraction, color))
        }
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()
        canvas.drawColor(Color.parseColor("#F5F9FF"))
        val barHeight = h * 0.19f
        val cornerRadius = barHeight / 4
        var currentX = 0f

        sleepSegments.forEach { segment ->
            paint.color = segment.color
            val top = when (segment.position) {
                Position.UPPER -> h * 0.05f
                Position.MIDDLE1 -> h * 0.28f
                Position.MIDDLE2 -> h * 0.51f
                Position.LOWER -> h * 0.74f
            }
            val bottom = top + barHeight
            val right = currentX + (segment.widthFraction * w)
            canvas.drawRoundRect(RectF(currentX, top, right, bottom), cornerRadius, cornerRadius, paint)
            currentX = right
        }
    }

    private fun getPositionForRecordType(type: String): Position {
        return when (type) {
            "Awake" -> Position.UPPER
            "REM Sleep" -> Position.MIDDLE1
            "Light Sleep" -> Position.MIDDLE2
            "Deep Sleep" -> Position.LOWER
            else -> error("Unknown record type: $type")
        }
    }
    private fun getColorForRecordType(type: String): Int {
        return when (type) {
            "Awake" -> resources.getColor(R.color.red_orange_bar)
            "REM Sleep" -> resources.getColor(R.color.light_blue_bar)
            "Light Sleep" -> resources.getColor(R.color.blue_bar)
            "Deep Sleep" -> resources.getColor(R.color.purple_bar)
            else -> error("Unknown record type: $type")
        }
    }
}

enum class Position {
    UPPER, MIDDLE1, MIDDLE2, LOWER
}

