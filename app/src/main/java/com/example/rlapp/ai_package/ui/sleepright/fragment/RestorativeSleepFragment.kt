package com.example.rlapp.ai_package.ui.sleepright.fragment

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Path
import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioGroup
import androidx.activity.OnBackPressedCallback
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.databinding.FragmentRestorativeSleepBinding
import com.github.mikephil.charting.animation.ChartAnimator
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.renderer.BarChartRenderer
import com.github.mikephil.charting.utils.ViewPortHandler
import com.google.android.material.snackbar.Snackbar

class RestorativeSleepFragment: BaseFragment<FragmentRestorativeSleepBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentRestorativeSleepBinding
        get() = FragmentRestorativeSleepBinding::inflate
    var snackbar: Snackbar? = null

    private lateinit var barChart: BarChart
    private lateinit var radioGroup: RadioGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setBackgroundResource(R.drawable.sleep_stages_bg)

        barChart = view.findViewById(R.id.heartRateChart)
        radioGroup = view.findViewById(R.id.tabGroup)

        // Show Week data by default
        updateChart(getWeekData(), getWeekLabels())

        // Set default selection to Week
        radioGroup.check(R.id.rbWeek)

        // Handle Radio Button Selection
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbWeek ->{
                    setupBarChart(barChart)
                   // updateChart(getWeekData(), getWeekLabels())
                }
                R.id.rbMonth ->{
                    updateChart(getMonthData(), getMonthLabels())
                }
                R.id.rbSixMonths ->{
                    updateChart(getSixMonthData(), getSixMonthLabels())
                }
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToFragment(SleepRightLandingFragment(), "SleepRightLandingFragment")

            }
        })

        val backBtn = view.findViewById<ImageView>(R.id.img_back)

        backBtn.setOnClickListener {
            navigateToFragment(SleepRightLandingFragment(), "SleepRightLandingFragment")
        }

    }

    private fun setupBarChart(barChart: BarChart) {
        // Base bars (lighter color)
        val baseEntries = listOf(
            BarEntry(0f, 3f),
            BarEntry(1f, 2.5f),
            BarEntry(2f, 2f),
            BarEntry(3f, 3f),
            BarEntry(4f, 1f),
            BarEntry(5f, 3.5f),
            BarEntry(6f, 2f)
        )

        // Top bars (darker color, overlapping)
        val topEntries = listOf(
            BarEntry(0f, 1f),
            BarEntry(1f, 1f),
            BarEntry(2f, 2f),
            BarEntry(3f, 1.5f),
            BarEntry(4f, 0.5f),
            BarEntry(5f, 1.5f),
            BarEntry(6f, 1f)
        )

        // Create datasets
        val baseDataSet = BarDataSet(baseEntries, "Base").apply {
            color = Color.parseColor("#90CAF9") // Light blue
            setDrawValues(false)
        }

        val topDataSet = BarDataSet(topEntries, "Top").apply {
            color = Color.parseColor("#3F51B5") // Dark blue
            setDrawValues(false)
        }

        val barData = BarData(baseDataSet, topDataSet)
        barData.barWidth = 0.4f // Adjust for overlap

        // Apply data
        barChart.data = barData
        barChart.description.isEnabled = false
        barChart.setFitBars(true)
        barChart.legend.isEnabled = false

        // Customize X-axis
        val labels = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
        barChart.xAxis.apply {
            position = XAxis.XAxisPosition.BOTTOM
            granularity = 1f
            setDrawGridLines(false)
            valueFormatter = IndexAxisValueFormatter(labels)
        }

        // Disable right axis, format left axis
        barChart.axisRight.isEnabled = false
        barChart.axisLeft.setDrawGridLines(false)

        // Apply custom rounded renderer
        barChart.renderer = RoundedBarChartRenderer(barChart, ChartAnimator(), ViewPortHandler())

        barChart.invalidate() // Refresh the chart
    }

    private fun updateChart(entries: List<BarEntry>, labels: List<String>) {
        val dataSet = BarDataSet(entries, "Calories Burned")
        dataSet.color = resources.getColor(R.color.sleep_duration_blue)
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

    private fun getWeekData(): List<BarEntry> {
        return listOf(
            BarEntry(0f, 200f),
            BarEntry(1f, 350f),
            BarEntry(2f, 270f),
            BarEntry(3f, 400f),
            BarEntry(4f, 320f),
            BarEntry(5f, 500f),
            BarEntry(6f, 450f)
        )
    }

    /** X-Axis Labels for Week */
    private fun getWeekLabels(): List<String> {
        return listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    }

    /** Sample Data for Month (4 weeks) */
    private fun getMonthData(): List<BarEntry> {
        return listOf(
            BarEntry(0f, 1500f), // 1-7 Jan
            BarEntry(1f, 1700f), // 8-14 Jan
            BarEntry(2f, 1400f), // 15-21 Jan
            BarEntry(3f, 1800f), // 22-28 Jan
            BarEntry(4f, 1200f)  // 29-31 Jan
        )
    }

    /** X-Axis Labels for Month */
    private fun getMonthLabels(): List<String> {
        return listOf("1-7 Jan", "8-14 Jan", "15-21 Jan", "22-28 Jan", "29-31 Jan")
    }

    /** Sample Data for 6 Months */
    private fun getSixMonthData(): List<BarEntry> {
        return listOf(
            BarEntry(0f, 9000f), // Jan
            BarEntry(1f, 8500f), // Feb
            BarEntry(2f, 8700f), // Mar
            BarEntry(3f, 9100f), // Apr
            BarEntry(4f, 9400f), // May
            BarEntry(5f, 8800f)  // Jun
        )
    }

    /** X-Axis Labels for 6 Months */
    private fun getSixMonthLabels(): List<String> {
        return listOf("Jan", "Feb", "Mar", "Apr", "May", "Jun")
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }
}
