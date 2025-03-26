package com.example.rlapp.ai_package.ui.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.databinding.FragmentBarGraphBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.google.android.material.snackbar.Snackbar

class BarChartFragment : BaseFragment<FragmentBarGraphBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentBarGraphBinding
        get() = FragmentBarGraphBinding::inflate
    var snackbar: Snackbar? = null
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find the BarChart from the layout
        val barChart: BarChart = view.findViewById(R.id.barChart)

        // Sample data: positive and negative values
        val entries = ArrayList<BarEntry>()
        entries.add(BarEntry(0f, 4f))  // Positive value
        entries.add(BarEntry(1f, -3f)) // Negative value
        entries.add(BarEntry(2f, 6f))  // Positive value
        entries.add(BarEntry(3f, -2f)) // Negative value

        // Create a dataset from the entries
        val barDataSet = BarDataSet(entries, "Sample Data")
        barDataSet.color = resources.getColor(android.R.color.holo_blue_light)  // Set bar color
        barDataSet.setDrawValues(true)  // Show values on top of bars
        barDataSet.setBarBorderWidth(0f)  // No border

        // Create BarData and set it to the BarChart
        val barData = BarData(barDataSet)
        barChart.data = barData

        // Customize X-axis labels
        val labels = listOf("A", "B", "C", "D")
        barChart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)

        // Set other chart properties (optional)
        barChart.setFitBars(true)  // Make the bars fit in the chart
        barChart.invalidate()  // Refresh the chart
    }
}