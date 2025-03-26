package com.example.rlapp.ai_package.ui.sleepright.fragment

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.home.HomeBottomTabFragment
import com.example.rlapp.databinding.FragmentSleepRightLandingBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet
import com.google.android.material.snackbar.Snackbar

class SleepRightLandingFragment : BaseFragment<FragmentSleepRightLandingBinding>(), WakeUpTimeDialogFragment.BottomSheetListener {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSleepRightLandingBinding
        get() = FragmentSleepRightLandingBinding::inflate
    var snackbar: Snackbar? = null

    lateinit var wakeTime : TextView
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val sleepChart = view.findViewById<LineChart>(R.id.sleepChart)
        val backButton = view.findViewById<ImageView>(R.id.img_back)
        val sleepBarChart = view.findViewById<BarChart>(R.id.sleepConsistentChart)
        val sleepInfo = view.findViewById<ImageView>(R.id.img_sleep_right)
        val editWakeup = view.findViewById<ImageView>(R.id.img_edit_wakeup_time)
        val sleepPerform = view.findViewById<ImageView>(R.id.img_sleep_perform_right)
        val sleepIdeal = view.findViewById<ImageView>(R.id.img_sleep_ideal_actual)
        val restoSleep = view.findViewById<ImageView>(R.id.img_resto_sleep)
        val consistencySleep = view.findViewById<ImageView>(R.id.img_consistency_right)
        wakeTime = view.findViewById<TextView>(R.id.tv_wakeup_time)

        sleepInfo.setOnClickListener {
            requireActivity().supportFragmentManager.beginTransaction().apply {
                val stepGoalFragment = SleepStagesFragment()
                val args = Bundle()
                stepGoalFragment.arguments = args
                replace(R.id.flFragment, stepGoalFragment, "Steps")
                addToBackStack(null)
                commit()
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToFragment(HomeBottomTabFragment(), "Home")

            }
        })

        backButton.setOnClickListener {
            navigateToFragment(HomeBottomTabFragment(),"Home")
        }

        sleepPerform.setOnClickListener {
            navigateToFragment(SleepPerformanceFragment(),"Performance")
        }

        sleepIdeal.setOnClickListener {
            navigateToFragment(SleepIdealActualFragment(),"IdealActual")
        }

        restoSleep.setOnClickListener {
            navigateToFragment(RestorativeSleepFragment(),"Restorative")
        }

        consistencySleep.setOnClickListener {
            navigateToFragment(SleepConsistencyFragment(),"Consistency")
        }

        setupBarChart(sleepBarChart)

        editWakeup.setOnClickListener {
           openBottomSheet()
        }

        // Sample data points for Ideal and Actual sleep times
        val weekdays = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

        // Sample data points for Ideal and Actual sleep times
        val idealEntries = listOf(
            Entry(0f, 8.3f), Entry(1f, 8.5f), Entry(2f, 9f),
            Entry(3f, 8.2f), Entry(4f, 8.7f), Entry(5f, 8.3f),
            Entry(6f, 8.2f)
        )
        val actualEntries = listOf(
            Entry(0f, 6f), Entry(1f, 6.5f), Entry(2f, 7f),
            Entry(3f, 6.2f), Entry(4f, 6.8f), Entry(5f, 6.3f),
            Entry(6f, 6.2f)
        )

        val idealDataSet = LineDataSet(idealEntries, "Ideal").apply {
            color = Color.GREEN
            valueTextSize = 12f
            setCircleColor(Color.GREEN)
            setDrawCircles(true)
            setDrawValues(false)
        }

        val actualDataSet = LineDataSet(actualEntries, "Actual").apply {
            color = Color.BLUE
            valueTextSize = 12f
            setCircleColor(Color.BLUE)
            setDrawCircles(true)
            setDrawValues(false)
        }

        val lineData = LineData(listOf<ILineDataSet>(idealDataSet, actualDataSet))
        sleepChart.data = lineData

        // Chart Customization
        sleepChart.apply {
            description.isEnabled = false
            axisRight.isEnabled = false
            axisLeft.axisMinimum = 0f
            xAxis.position = XAxis.XAxisPosition.BOTTOM
            xAxis.setDrawGridLines(false)
            xAxis.valueFormatter = IndexAxisValueFormatter(weekdays) // Set weekdays on X-axis
            xAxis.granularity = 1f
            xAxis.labelCount = weekdays.size
            invalidate() // Refresh chart
        }


        //code for bar chart

        val barChart = view.findViewById<BarChart>(R.id.barChart)

        val barEntries = ArrayList<BarEntry>()
        barEntries.add(BarEntry(1f, 2f))  // Small bar
        barEntries.add(BarEntry(3f, 4f))  // Medium bar
        barEntries.add(BarEntry(5f, 3f))  // Large bar
        barEntries.add(BarEntry(7f, 4f))  // Large bar
        barEntries.add(BarEntry(9f, 2f))  // Small bar

        val blueDataSet = BarDataSet(barEntries, "Sleep Blocks")
        blueDataSet.color = Color.parseColor("#4444DD") // Dark blue
      //  blueDataSet.barBorderRadius = 15f

        // Creating bars for wakefulness
        val wakeEntries = ArrayList<BarEntry>()
        wakeEntries.add(BarEntry(2f, 1f)) // Short wake time
        wakeEntries.add(BarEntry(4f, 2f))
        wakeEntries.add(BarEntry(6f, 2f))
        wakeEntries.add(BarEntry(8f, 3f))

        val lightBlueDataSet = BarDataSet(wakeEntries, "Wake Blocks")
        lightBlueDataSet.color = Color.parseColor("#66CCFF") // Light blue
       // lightBlueDataSet.barBorderRadius = 15f

        val dataSets = ArrayList<IBarDataSet>()
        dataSets.add(blueDataSet)
        dataSets.add(lightBlueDataSet)

        val data = BarData(dataSets)
        data.barWidth = 0.8f // Adjust the bar width to match the original image

        barChart.data = data
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setFitBars(true)
        barChart.animateY(1000)

        // Customizing X Axis
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.setDrawGridLines(false)
        xAxis.setDrawAxisLine(false)

        // Customizing Y Axis
        val leftAxis = barChart.axisLeft
        leftAxis.setDrawGridLines(false)
        leftAxis.axisMinimum = 0f
        leftAxis.setDrawAxisLine(false)

        val rightAxis = barChart.axisRight
        rightAxis.isEnabled = false

        barChart.invalidate()

        val sleepStagesView = view.findViewById<SleepChartViewLanding>(R.id.sleepStagesView)
        val sleepData = listOf(
            SleepSegmentModel(0.001f, 0.100f, resources.getColor(R.color.blue_bar), 110f),
            SleepSegmentModel(0.101f, 0.150f, resources.getColor(R.color.blue_bar), 110f),
            SleepSegmentModel(0.151f, 0.300f, resources.getColor(R.color.purple_bar), 110f),
            SleepSegmentModel(0.301f, 0.400f, resources.getColor(R.color.light_blue_bar), 110f),
            SleepSegmentModel(0.401f, 0.450f, resources.getColor(R.color.black), 110f),
            SleepSegmentModel(0.451f, 0.550f, resources.getColor(R.color.red_orange_bar), 110f),
        SleepSegmentModel(0.551f, 0.660f, resources.getColor(R.color.light_cyan_bar), 110f),
        SleepSegmentModel(0.661f, 0.690f, resources.getColor(R.color.bright_blue_bar), 110f),
        SleepSegmentModel(0.691f, 0.750f, resources.getColor(R.color.deep_purple_bar), 110f),
        SleepSegmentModel(0.751f, 0.860f, resources.getColor(R.color.sky_blue_bar), 110f),
        SleepSegmentModel(0.861f, 0.990f, resources.getColor(R.color.dark_purple_bar), 110f)
        )

        sleepStagesView.setSleepData(sleepData)

    }

    private fun setupBarChart(barChart: BarChart) {
        val sleepEntries = ArrayList<BarEntry>()

        // Example data: Start time (X-axis), duration in hours (Y-axis)
        sleepEntries.add(BarEntry(0f, floatArrayOf(2f, 6f)))  // Monday (Start at 2 AM, sleep 6 hours)
        sleepEntries.add(BarEntry(1f, floatArrayOf(12f, 6f))) // Tuesday
        sleepEntries.add(BarEntry(2f, floatArrayOf(10f, 6f))) // Wednesday
        sleepEntries.add(BarEntry(3f, floatArrayOf(11f, 6f))) // Thursday
        sleepEntries.add(BarEntry(4f, floatArrayOf(3f, 4f)))  // Friday
        sleepEntries.add(BarEntry(5f, floatArrayOf(1f, 7f)))  // Saturday
        sleepEntries.add(BarEntry(6f, floatArrayOf(3f, 5f)))  // Sunday (Highlighted in blue)

        val barDataSet = BarDataSet(sleepEntries, "Sleep Duration")
        barDataSet.colors = listOf(
            Color.parseColor("#B0D8FF"), // Light blue for normal days
            Color.parseColor("#B0D8FF"),
            Color.parseColor("#B0D8FF"),
            Color.parseColor("#B0D8FF"),
            Color.parseColor("#B0D8FF"),
            Color.parseColor("#B0D8FF"),
            Color.parseColor("#007AFF")  // Highlighted blue for Sunday
        )

        barDataSet.setDrawValues(false) // Hide values on bars
        barDataSet.stackLabels = arrayOf("Start Time", "Sleep Hours") // Labels for stacked bars

        val barData = BarData(barDataSet)
        barData.barWidth = 0.6f // Adjust bar width

        barChart.data = barData
        barChart.setFitBars(true)
        barChart.invalidate()

        // Customizing X-Axis Labels
        val xAxis = barChart.xAxis
        xAxis.position = XAxis.XAxisPosition.BOTTOM
        xAxis.valueFormatter = IndexAxisValueFormatter(
            listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        )
        xAxis.setDrawGridLines(false)
        xAxis.textSize = 12f

        // Customizing Y-Axis
        val yAxisLeft = barChart.axisLeft
        yAxisLeft.axisMinimum = 0f
        yAxisLeft.axisMaximum = 14f // Max hours on the y-axis
        yAxisLeft.setDrawGridLines(true)
        yAxisLeft.textSize = 12f

        val yAxisRight = barChart.axisRight
        yAxisRight.isEnabled = false // Hide right Y-axis

        // General chart settings
        barChart.description.isEnabled = false
        barChart.legend.isEnabled = false
        barChart.setTouchEnabled(false) // Disable touch
    }

    private fun openBottomSheet() {
        val bottomSheet = WakeUpTimeDialogFragment()
        bottomSheet.show(parentFragmentManager, "WakeUpTimeDialog")
    }

    override fun onDataReceived(data: String) {
        wakeTime.text = data
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }


}

class SleepChartViewLanding(context: android.content.Context, attrs: android.util.AttributeSet? = null) :
    View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val sleepData = mutableListOf<SleepSegmentModel>()

    fun setSleepData(data: List<SleepSegmentModel>) {
        sleepData.clear()
        sleepData.addAll(data)
        invalidate()
    }


    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val width = width.toFloat()
        val height = height.toFloat()

        val baselineY = height * 0.8f
        val maxBarHeight = height * 0.5f
        val cornerRadius = height * 0.1f  // Adjust the curve radius as needed

        for (segment in sleepData) {
            paint.color = segment.color
            val left = segment.start.coerceIn(0f, 1f) * width
            val right = segment.end.coerceIn(0f, 1f) * width
            val top = baselineY - (segment.height / maxBarHeight * maxBarHeight)
            val bottom = baselineY

            // Draw a rounded rectangle instead of a normal rectangle
            canvas.drawRoundRect(RectF(left, top, right, bottom), cornerRadius, cornerRadius, paint)
        }
    }

}

data class SleepSegmentModel(val start: Float, val end: Float, val color: Int, val height: Float)