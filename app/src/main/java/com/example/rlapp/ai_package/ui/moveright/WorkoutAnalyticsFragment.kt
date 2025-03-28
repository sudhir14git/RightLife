package com.example.rlapp.ai_package.ui.moveright

import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.home.HomeBottomTabFragment
import com.example.rlapp.ai_package.ui.moveright.customProgressBar.CardioStrippedProgressBar
import com.example.rlapp.ai_package.ui.moveright.customProgressBar.FatBurnStrippedProgressBar
import com.example.rlapp.ai_package.ui.moveright.customProgressBar.LightStrippedprogressBar
import com.example.rlapp.ai_package.ui.moveright.customProgressBar.StripedProgressBar
import com.example.rlapp.databinding.FragmentWorkoutAnalyticsBinding
import java.util.Calendar

data class HRDataPoint(val time: Long, val bpm: Int)

class WorkoutAnalyticsFragment : BaseFragment<FragmentWorkoutAnalyticsBinding>() {
    private lateinit var heartRateGraph: CustomHeartRateGraph
    private lateinit var customProgressBar: StripedProgressBar
    private lateinit var transparentOverlay: View
    private lateinit var customProgressBarCardio: CardioStrippedProgressBar
    private lateinit var transparentOverlayCardio: View
    private lateinit var customProgressBarLight: LightStrippedprogressBar
    private lateinit var transparentOverlayLight: View
    private lateinit var customProgressBarFatBurn: FatBurnStrippedProgressBar
    private lateinit var transparentOverlayFatBurn: View

    private val dataPoints = mutableListOf<HRDataPoint>()

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentWorkoutAnalyticsBinding
        get() = FragmentWorkoutAnalyticsBinding::inflate

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        heartRateGraph = view.findViewById(R.id.heartRateGraph)
        customProgressBar = view.findViewById(R.id.customProgressBar)
        transparentOverlay = view.findViewById(R.id.transparentOverlay)
        customProgressBarCardio = view.findViewById(R.id.customProgressBarCardio)
        transparentOverlayCardio = view.findViewById(R.id.transparentOverlayCardio)
        customProgressBarLight = view.findViewById(R.id.customProgressBarLight)
        transparentOverlayLight = view.findViewById(R.id.transparentOverlayLight)
        customProgressBarFatBurn = view.findViewById(R.id.customProgressBarFatBurn)
        transparentOverlayFatBurn = view.findViewById(R.id.transparentOverlayFatBurn)

        // Setup progress bars
        customProgressBar.post {
            val progressBarWidth = customProgressBar.width
            val overlayWidth = (progressBarWidth * 0.5).toInt()
            val layoutParams = transparentOverlay.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.width = overlayWidth
            transparentOverlay.layoutParams = layoutParams
        }

        customProgressBarCardio.post {
            val progressBarWidth = customProgressBarCardio.width
            val overlayWidth = (progressBarWidth * 0.5).toInt()
            val layoutParams = transparentOverlayCardio.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.width = overlayWidth
            transparentOverlayCardio.layoutParams = layoutParams
        }

        customProgressBarLight.post {
            val progressBarWidth = customProgressBarLight.width
            val overlayWidth = (progressBarWidth * 0.5).toInt()
            val layoutParams = transparentOverlayLight.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.width = overlayWidth
            transparentOverlayLight.layoutParams = layoutParams
        }

        customProgressBarFatBurn.post {
            val progressBarWidth = customProgressBarFatBurn.width
            val overlayWidth = (progressBarWidth * 0.5).toInt()
            val layoutParams = transparentOverlayFatBurn.layoutParams as ConstraintLayout.LayoutParams
            layoutParams.width = overlayWidth
            transparentOverlayFatBurn.layoutParams = layoutParams
        }

        // Back press handling
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToFragment(HomeBottomTabFragment(), "LandingFragment")
            }
        })

        // Generate heart rate data and set it on the custom graph
        generateSampleData()
        heartRateGraph.setData(dataPoints)
    }

    private fun generateSampleData() {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.HOUR_OF_DAY, 6)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val totalMinutes = 60 // 1 hour (6:00 AM to 7:00 AM)
        val peakMinute = 30 // Peak at 6:30 AM
        var currentBPM = 100 // Start BPM at 100

        for (minute in 0 until totalMinutes) {
            val time = calendar.timeInMillis
            dataPoints.add(HRDataPoint(time, currentBPM))

            // Increment time by 1 minute
            calendar.add(Calendar.MINUTE, 1)

            // Adjust BPM
            if (minute < peakMinute) {
                // Increase BPM until 6:30 AM
                if (minute % 2 == 0) {
                    currentBPM -= (1..10).random()
                } else {
                    currentBPM += (10..20).random()
                }
                if (currentBPM > 190) currentBPM = 190
            } else {
                // Decrease BPM after 6:30 AM
                if (minute % 2 == 0) {
                    currentBPM += (1..10).random()
                } else {
                    currentBPM -= (10..20).random()
                }
                if (currentBPM < 100) currentBPM = 100
            }
        }
    }

    private fun navigateToFragment(fragment: androidx.fragment.app.Fragment, tag: String) {
        requireActivity().supportFragmentManager.beginTransaction().apply {
            replace(R.id.flFragment, fragment, tag)
            addToBackStack(null)
            commit()
        }
    }

    private fun setProgressLineBarProgress(progressLineBar: View, progress: Int) {
        val progressPercentage = progress.coerceIn(0, 100)
        val totalWidth = progressLineBar.width
        val pinkWidth = (progressPercentage / 100.0 * totalWidth).toInt()
        val pinkDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.parseColor("#FFC0CB")) // Pink color
            cornerRadius = 0f
        }
        val whiteGreyDrawable = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            setColor(Color.WHITE)
            setStroke(2, Color.GRAY) // Grey border
            cornerRadius = 0f
        }
        val layerDrawable = LayerDrawable(arrayOf(pinkDrawable, whiteGreyDrawable))
        layerDrawable.setLayerInset(0, 0, 0, totalWidth - pinkWidth, 0)
        layerDrawable.setLayerInset(1, pinkWidth, 0, 0, 0)
        progressLineBar.background = layerDrawable
    }
}