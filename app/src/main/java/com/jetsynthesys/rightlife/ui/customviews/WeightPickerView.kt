package com.jetsynthesys.rightlife.ui.customviews
import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View

class WeightPickerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    var itemSpacing = 20f // Spacing for each 0.1 unit
    var sidePadding = 0
    var isKgMode = true
    private val kgMin = 30.0
    private val kgMax = 200.0
    private val lbsMin = 66.0
    private val lbsMax = 440.0

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
        textAlign = Paint.Align.CENTER
    }

    fun updateForKg() {
        isKgMode = true
        requestLayout()
        invalidate()
    }

    fun updateForLbs() {
        isKgMode = false
        requestLayout()
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val totalItems = if (isKgMode) {
            ((kgMax - kgMin) * 10).toInt() + 1 // Each 0.1 kg
        } else {
            ((lbsMax - lbsMin) * 10).toInt() + 1 // Each 0.1 lbs
        }
        val width = (totalItems * itemSpacing).toInt() + (sidePadding * 2)
        val height = 320
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (isKgMode) {
            drawKg(canvas)
        } else {
            drawLbs(canvas)
        }
    }

    private fun drawKg(canvas: Canvas) {
        val startY = 0f
        val endYShort = 100f
        val endYLong = 160f

        val minValue = (kgMin * 10).toInt()
        val maxValue = (kgMax * 10).toInt()

        for (value in minValue..maxValue) {
            val index = value - minValue
            val x = sidePadding + index * itemSpacing
            val actualWeight = value / 10.0

            // Draw longer line every 10 units (every whole kg)
            if (value % 10 == 0) {
                canvas.drawLine(x, startY, x, endYLong, majorLinePaint)
                // Draw kg number below
                canvas.drawText(actualWeight.toInt().toString(), x, endYLong + 70f, textPaint)
            } else {
                // Shorter lines for 0.1 kg increments
                canvas.drawLine(x, startY, x, endYShort, linePaint)
            }
        }
    }

    private fun drawLbs(canvas: Canvas) {
        val startY = 0f
        val endYShort = 100f
        val endYLong = 160f

        val minValue = (lbsMin * 10).toInt()
        val maxValue = (lbsMax * 10).toInt()

        for (value in minValue..maxValue) {
            val index = value - minValue
            val x = sidePadding + index * itemSpacing
            val actualWeight = value / 10.0

            // Draw longer line every 10 units (every whole lbs)
            if (value % 10 == 0) {
                canvas.drawLine(x, startY, x, endYLong, majorLinePaint)
                canvas.drawText(actualWeight.toInt().toString(), x, endYLong + 70f, textPaint)
            } else {
                // Shorter lines for 0.1 lbs increments
                canvas.drawLine(x, startY, x, endYShort, linePaint)
            }
        }
    }
}
