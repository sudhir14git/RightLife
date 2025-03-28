package com.example.rlapp.ai_package.ui.moveright.customProgressBar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class CustomProgressBar(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val progressBarBackgroundPaint = Paint().apply {
        color = Color.parseColor("#F5F5F5") // Light Gray Background
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val progressPaint = Paint().apply {
        color = Color.parseColor("#FF5C5C") // Progress color
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    private val textPaint = Paint().apply {
        color = Color.BLACK
        textSize = 20f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
        isAntiAlias = true
    }

    private val indicatorPaint = Paint().apply {
        color = Color.parseColor("#FF5C5C") // Red indicator
        style = Paint.Style.FILL
        isAntiAlias = true
    }

    var progress: Float = 0.0f
        set(value) {
            field = value.coerceIn(0f, 1f)
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val padding = 40f // Reduced padding
        val barHeight = 30f
        val startX = padding
        val endX = width - padding
        val centerY = height / 2f + 20f

        // Draw Background Bar
        val backgroundRect = RectF(startX, centerY - barHeight / 2, endX, centerY + barHeight / 2)
        canvas.drawRoundRect(backgroundRect, barHeight / 2, barHeight / 2, progressBarBackgroundPaint)

        // Draw Progress Indicator (Big Red Circle at Start)
        val progressX = startX + (endX - startX) * progress
        canvas.drawCircle(progressX, centerY, 25f, indicatorPaint)

        // Labels and Indicator Dots
        val labels = arrayOf("Low", "Moderate", "High", "Very High")
        val indicatorCount = labels.size
        val textOffsetX = 10f  // Offset to keep text from being cut

        for (i in labels.indices) {
            val indicatorX = startX + i * (endX - startX) / (indicatorCount - 1)

            // Ensure last text aligns properly
            val textX = when (i) {
                0 -> indicatorX + textOffsetX  // Align 'Low' slightly right
                labels.lastIndex -> indicatorX - textOffsetX // Align 'Very High' left
                else -> indicatorX
            }

            // Draw small red dots
            canvas.drawCircle(indicatorX, centerY, 8f, indicatorPaint)

            // Draw Text Labels above the bar
            canvas.drawText(labels[i], textX, centerY - 40f, textPaint)
        }
    }
}
