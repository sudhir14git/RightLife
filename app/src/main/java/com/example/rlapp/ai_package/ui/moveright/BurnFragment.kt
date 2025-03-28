package com.example.rlapp.ai_package.ui.moveright

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import androidx.activity.addCallback
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.home.HomeBottomTabFragment
import com.example.rlapp.databinding.FragmentBurnBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter

class BurnFragment : BaseFragment<FragmentBurnBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBurnBinding
        get() = FragmentBurnBinding::inflate

    private lateinit var barChart: BarChart
    private lateinit var radioGroup: RadioGroup

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setBackgroundResource(R.drawable.gradient_color_background_workout)

        barChart = view.findViewById(R.id.heartRateChart)
        radioGroup = view.findViewById(R.id.tabGroup)

        // Show Week data by default
        updateChart(getWeekData(), getWeekLabels())

        // Set default selection to Week
        radioGroup.check(R.id.rbWeek)

        // Handle Radio Button Selection
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.rbWeek -> updateChart(getWeekData(), getWeekLabels())
                R.id.rbMonth -> updateChart(getMonthData(), getMonthLabels())
                R.id.rbSixMonths -> updateChart(getSixMonthData(), getSixMonthLabels())
            }
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            navigateToFragment(HomeBottomTabFragment(), "landingFragment")
        }
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    /** Update BarChart with new data */
    private fun updateChart(entries: List<BarEntry>, labels: List<String>) {
        val dataSet = BarDataSet(entries, "Calories Burned")
        dataSet.color = Color.RED
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

    /** Sample Data for Week */
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
}
