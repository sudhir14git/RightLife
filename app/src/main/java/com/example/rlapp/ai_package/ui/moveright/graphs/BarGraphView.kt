package com.example.rlapp.ai_package.ui.moveright.graphs

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class BarGraphView : View {
    private var barPaint: Paint? = null // Paint for the bars
    private var dataPoints = floatArrayOf(30f, 60f, 4f, 80f, 40f, 90f) // Example data points
    private var spacing = 10f // Space between bars (in pixels)

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    private fun init() {
        // Initialize bar paint
        barPaint = Paint()
        barPaint!!.color = -0x4eebec // Bar color
        barPaint!!.style = Paint.Style.FILL // Solid bars
        barPaint!!.isAntiAlias = true // Smooth edges
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Get the width and height of the view
        val width = width.toFloat()
        val height = height.toFloat()

        // Find the minimum and maximum values in the data points
        val minValue = 0f // Bars start from 0
        var maxValue = Float.MIN_VALUE
        for (value in dataPoints) {
            if (value > maxValue) maxValue = value
        }


        val scaleY = height / maxValue
        val totalSpacing = spacing * (dataPoints.size - 1)
        val availableWidth = width - totalSpacing
        val barWidth = availableWidth / dataPoints.size


        for (i in dataPoints.indices) {
            val barHeight = dataPoints[i] * scaleY
            val left = i * (barWidth + spacing)
            val top = height - barHeight
            val right = left + barWidth
            val bottom = height

            // Draw the bar
            canvas.drawRect(left, top, right, bottom, barPaint!!)
        }
    }

    /**
     * Set the data points for the bar graph.
     *
     * @param dataPoints Array of values to be displayed as bars.
     */
    fun setDataPoints(dataPoints: FloatArray) {
        this.dataPoints = dataPoints
        invalidate() // Redraw the view
    }

    /**
     * Set the spacing between bars.
     *
     * @param spacing Spacing between bars in pixels.
     */
    fun setSpacing(spacing: Float) {
        this.spacing = spacing
        invalidate() // Redraw the view
    }
}