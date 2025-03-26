package com.example.rlapp.ai_package.ui.steps

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver
import android.widget.ProgressBar
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.model.GridItem
import com.example.rlapp.databinding.FragmentStepGoalBinding
import com.google.android.material.snackbar.Snackbar

class StepGoalFragment : BaseFragment<FragmentStepGoalBinding>() {

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentStepGoalBinding
        get() = FragmentStepGoalBinding::inflate
    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val progressBarStep = view.findViewById<ProgressBar>(R.id.progressBar_step)
        val circleIndicator = view.findViewById<View>(R.id.circleIndicator_step)
        progressBarStep.viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                progressBarStep.viewTreeObserver.removeOnGlobalLayoutListener(this)
                val progress = 1285
                val max = progressBarStep.max
                val progressBarWidth = progressBarStep.width.toFloat()
                val circlePosition = (progress.toFloat() / max) * progressBarWidth
                val circleRadius = circleIndicator.width / 2f
                circleIndicator.x = circlePosition - circleRadius
                circleIndicator.y = progressBarStep.y + (progressBarStep.height - circleIndicator.height) / 2f
            }
        })
      //  val lineGraphView = view.findViewById<LineGrapghViewSteps>(R.id.line_graph_steps)
        val recyclerView: RecyclerView = view.findViewById(R.id.recyclerView)
        val items = listOf(
            GridItem("Burn", R.drawable.burn_icon, "Kcal","844")
        )

        recyclerView.layoutManager = LinearLayoutManager(requireContext()) // 2 Columns
        recyclerView.adapter = StepsGridAdapter(items)
        val todaySteps = floatArrayOf(100f, 200f, 100f, 300f, 50f, 400f, 100f)
        val averageSteps = floatArrayOf(150f, 250f, 350f, 450f, 550f, 650f, 750f)
        val goalSteps = floatArrayOf(700f, 700f, 700f, 700f, 700f, 700f, 700f)
      //  lineGraphView.addDataSet(todaySteps, 0xFFFD6967.toInt()) // Red for today's steps
     //   lineGraphView.addDataSet(averageSteps, 0xFF707070.toInt()) // Green for average steps
     //   lineGraphView.addDataSet(goalSteps, 0xFF03B27B.toInt()) // Blue for goals
     //   lineGraphView.invalidate()
    }
}