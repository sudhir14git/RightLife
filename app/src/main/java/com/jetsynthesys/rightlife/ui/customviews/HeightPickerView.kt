package com.jetsynthesys.rightlife.ui.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class HeightPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var itemSpacing = 30f // Spacing for each inch
    var sidePadding = 0
    var isFeetInchesMode = true
    private val minCm = 120
    private val maxCm = 220

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF333333.toInt()
        strokeWidth = 1.5f
    }

    private val majorLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF333333.toInt()
        strokeWidth = 3f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF333333.toInt()
        textSize = 48f
        textAlign = Paint.Align.LEFT
    }

    fun updateForFeetInches() {
        isFeetInchesMode = true
        requestLayout()
        invalidate()
    }

    fun updateForCm() {
        isFeetInchesMode = false
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = 280
        val totalItems = if (isFeetInchesMode) {
            (7 * 12) - (4 * 12) + 1 // Total inches from 4'0" to 7'0"
        } else {
            maxCm - minCm + 1
        }
        val height = (totalItems * itemSpacing).toInt() + (sidePadding * 2)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isFeetInchesMode) {
            drawFeetInches(canvas)
        } else {
            drawCentimeters(canvas)
        }
    }

    private fun drawFeetInches(canvas: Canvas) {
        val startX = 1f
        val endXShort = 140f
        val endXLong = 190f

        val totalItems = (7 * 12) - (4 * 12) + 1

        for (totalInch in (4 * 12)..(7 * 12)) {
            val index = totalInch - (4 * 12)
            // REVERSED: Higher values at top (lower y), lower values at bottom (higher y)
            val y = sidePadding + (totalItems - 1 - index) * itemSpacing

            val feet = totalInch / 12
            val inches = totalInch % 12

            // Draw longer line every 12 inches (every foot)
            if (inches == 0) {
                canvas.drawLine(startX, y, endXLong, y, majorLinePaint)
                // Draw feet number on the right
                canvas.drawText(feet.toString(), endXLong + 20f, y + 15f, textPaint)
            } else {
                // Shorter lines for inches
                canvas.drawLine(startX, y, endXShort, y, linePaint)
            }
        }
    }

    private fun drawCentimeters(canvas: Canvas) {
        val startX = 10f
        val endXShort = 140f
        val endXLong = 180f

        val totalItems = maxCm - minCm + 1

        for (cm in minCm..maxCm) {
            val index = cm - minCm
            // REVERSED: Higher values at top (lower y), lower values at bottom (higher y)
            val y = sidePadding + (totalItems - 1 - index) * itemSpacing

            // Draw longer line every 10 cm
            if (cm % 10 == 0) {
                canvas.drawLine(startX, y, endXLong, y, majorLinePaint)
                canvas.drawText(cm.toString(), endXLong + 20f, y + 15f, textPaint)
            } else if (cm % 5 == 0) {
                // Medium line every 5 cm
                canvas.drawLine(startX, y, endXShort, y, linePaint)
            } else {
                // Short line for every cm
                val endXVeryShort = 120f
                canvas.drawLine(startX, y, endXVeryShort, y, linePaint)
            }
        }
    }
}


/*
package com.jetsynthesys.rightlife.ui.customviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class HeightPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var itemSpacing = 30f // Spacing for each inch
    var sidePadding = 0
    var isFeetInchesMode = true
    private val minCm = 120
    private val maxCm = 220

    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF333333.toInt()
        strokeWidth = 1.5f
    }

    private val majorLinePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF333333.toInt()
        strokeWidth = 3f
    }

    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = 0xFF333333.toInt()
        textSize = 48f
        textAlign = Paint.Align.LEFT
    }

    fun updateForFeetInches() {
        isFeetInchesMode = true
        requestLayout()
        invalidate()
    }

    fun updateForCm() {
        isFeetInchesMode = false
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = 280
        val totalItems = if (isFeetInchesMode) {
            (7 * 12) - (4 * 12) + 1 // Total inches from 4'0" to 7'0"
        } else {
            maxCm - minCm + 1
        }
        val height = (totalItems * itemSpacing).toInt() + (sidePadding * 2)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isFeetInchesMode) {
            drawFeetInches(canvas)
        } else {
            drawCentimeters(canvas)
        }
    }

    private fun drawFeetInches(canvas: Canvas) {
        val startX = 1f
        val endXShort = 140f
        val endXLong = 190f

        for (totalInch in (4 * 12)..(7 * 12)) {
            val index = totalInch - (4 * 12)
            val y = sidePadding + index * itemSpacing

            val feet = totalInch / 12
            val inches = totalInch % 12

            // Draw longer line every 12 inches (every foot)
            if (inches == 0) {
                canvas.drawLine(startX, y, endXLong, y, majorLinePaint)
                // Draw feet number on the right
                canvas.drawText(feet.toString(), endXLong + 20f, y + 15f, textPaint)
            } else {
                // Shorter lines for inches
                canvas.drawLine(startX, y, endXShort, y, linePaint)
            }
        }
    }

    private fun drawCentimeters(canvas: Canvas) {
        val startX = 10f
        val endXShort = 140f
        val endXLong = 180f

        for (cm in minCm..maxCm) {
            val index = cm - minCm
            val y = sidePadding + index * itemSpacing

            // Draw longer line every 10 cm
            if (cm % 10 == 0) {
                canvas.drawLine(startX, y, endXLong, y, majorLinePaint)
                canvas.drawText(cm.toString(), endXLong + 20f, y + 15f, textPaint)
            } else if (cm % 5 == 0) {
                // Medium line every 5 cm
                canvas.drawLine(startX, y, endXShort, y, linePaint)
            } else {
                // Short line for every cm
                val endXVeryShort = 120f
                canvas.drawLine(startX, y, endXVeryShort, y, linePaint)
            }
        }
    }
}*/
