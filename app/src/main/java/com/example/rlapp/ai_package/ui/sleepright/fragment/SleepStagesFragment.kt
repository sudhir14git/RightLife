package com.example.rlapp.ai_package.ui.sleepright.fragment

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import androidx.activity.OnBackPressedCallback
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.rlapp.R
import com.example.rlapp.ai_package.base.BaseFragment
import com.example.rlapp.ai_package.ui.moveright.customProgressBar.CardioStrippedProgressBar
import com.example.rlapp.ai_package.ui.moveright.customProgressBar.FatBurnStrippedProgressBar
import com.example.rlapp.ai_package.ui.moveright.customProgressBar.LightStrippedprogressBar
import com.example.rlapp.ai_package.ui.moveright.customProgressBar.StripedProgressBar
import com.example.rlapp.databinding.FragmentSleepStagesBinding
import com.google.android.material.snackbar.Snackbar

class SleepStagesFragment : BaseFragment<FragmentSleepStagesBinding>() {

    private lateinit var customProgressBar: StripedProgressBar
    private lateinit var transparentOverlay: View
    private lateinit var customProgressBarCardio: CardioStrippedProgressBar
    private lateinit var transparentOverlayCardio: View
    private lateinit var customProgressBarLight: LightStrippedprogressBar
    private lateinit var transparentOverlayLight: View
    private lateinit var customProgressBarFatBurn: FatBurnStrippedProgressBar
    private lateinit var transparentOverlayFatBurn: View

    override val bindingInflater: (LayoutInflater, ViewGroup?, Boolean) -> FragmentSleepStagesBinding
        get() = FragmentSleepStagesBinding::inflate
    var snackbar: Snackbar? = null

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val sleepChart = view.findViewById<SleepChartView>(R.id.sleepChart)
        val sleepData = listOf(
            SleepSegment(0.001f, 0.100f, resources.getColor(R.color.red_orange_bar), 110f,Position.UPPER),
            SleepSegment(0.101f, 0.150f, resources.getColor(R.color.blue_bar), 110f,Position.MIDDLE2),
            SleepSegment(0.151f, 0.300f, resources.getColor(R.color.purple_bar), 110f,Position.LOWER),
            SleepSegment(0.301f, 0.410f, resources.getColor(R.color.light_blue_bar), 110f,Position.MIDDLE1),
            SleepSegment(0.401f, 0.440f, resources.getColor(R.color.purple_bar), 110f,Position.LOWER),
            SleepSegment(0.451f, 0.550f, resources.getColor(R.color.blue_bar), 110f,Position.MIDDLE2),
            SleepSegment(0.551f, 0.660f, resources.getColor(R.color.light_blue_bar), 110f,Position.MIDDLE1),
            SleepSegment(0.661f, 0.690f, resources.getColor(R.color.red_orange_bar), 110f,Position.UPPER),
            SleepSegment(0.691f, 0.700f, resources.getColor(R.color.blue_bar), 110f,Position.MIDDLE2),
            SleepSegment(0.701f, 0.860f, resources.getColor(R.color.purple_bar), 110f,Position.LOWER),
            SleepSegment(0.861f, 0.990f, resources.getColor(R.color.light_blue_bar), 110f,Position.MIDDLE1)
        )

        sleepChart.setSleepData(sleepData)

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

        val backBtn = view.findViewById<ImageView>(R.id.img_back)

        backBtn.setOnClickListener {
            navigateToFragment(SleepRightLandingFragment(), "SleepRightLandingFragment")
        }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                navigateToFragment(SleepRightLandingFragment(), "SleepRightLandingFragment")
            }
        })
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
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textSize = 32f
        textAlign = Paint.Align.CENTER
    }
    private val sleepData = mutableListOf<SleepSegment>()

    fun setSleepData(data: List<SleepSegment>) {
        sleepData.clear()
        sleepData.addAll(data)
        invalidate()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val w = width.toFloat()
        val h = height.toFloat()

        val middle1Y = h * 0.45f
        val middle2Y = h * 0.65f
        val upperY = h * 0.3f
        val lowerY = h * 0.8f
        val barHeight = h * 0.15f
        val cornerRadius = barHeight / 5f

        for (segment in sleepData) {
            paint.color = segment.color
            val left = segment.start * w
            val right = segment.end * w
            val top = when (segment.position) {
                Position.UPPER -> upperY - barHeight
                Position.MIDDLE1 -> middle1Y - barHeight / 2
                Position.MIDDLE2 -> middle2Y - barHeight / 2
                Position.LOWER -> lowerY
            }
            val bottom = top + barHeight

            canvas.drawRoundRect(RectF(left, top, right, bottom), cornerRadius, cornerRadius, paint)
        }
    }
}
data class SleepSegment(val start: Float, val end: Float, val color: Int,val height: Float, val position: Position)
enum class Position { UPPER, MIDDLE1, MIDDLE2, LOWER }