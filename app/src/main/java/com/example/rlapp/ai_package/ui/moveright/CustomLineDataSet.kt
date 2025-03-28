package com.example.rlapp.ai_package.ui.moveright

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import java.text.SimpleDateFormat
import java.util.Locale


class CustomHeartRateGraph @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dataPoints = mutableListOf<HRDataPoint>()
    private val linePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 6f
    }
    private val axisPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }
    private val gridPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.LTGRAY
        style = Paint.Style.STROKE
        strokeWidth = 1f
    }
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 36f
    }
    private val labelBackgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        style = Paint.Style.FILL
        setShadowLayer(8f, 0f, 4f, Color.argb(80, 0, 0, 0)) // Add shadow for floating effect
    }
    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 32f // Adjusted to match the image
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val heartIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val path = Path()
    private val minBpm = 70f
    private val maxBpm = 200f
    private val bpmRange = maxBpm - minBpm
    private val labelCountY = 5
    private val granularityX = 10f
    private val labelCountX = 6
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    // Variables to store the selected data point
    private var selectedDataPoint: HRDataPoint? = null
    private var showLabel = false

    fun setData(points: List<HRDataPoint>) {
        dataPoints.clear()
        dataPoints.addAll(points)
        invalidate()
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        val paddingLeft = 120f
        val paddingRight = 20f
        val graphWidth = width - paddingLeft - paddingRight
        val totalPoints = dataPoints.size

        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val touchX = event.x
                if (touchX >= paddingLeft && touchX <= width - paddingRight) {
                    val normalizedX = (touchX - paddingLeft) / graphWidth
                    val closestIndex = (normalizedX * (totalPoints - 1)).toInt().coerceIn(0, totalPoints - 1)
                    selectedDataPoint = dataPoints[closestIndex]
                    showLabel = true
                } else {
                    showLabel = false
                }
                invalidate()
                return true
            }
            MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                showLabel = false
                invalidate()
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        if (dataPoints.isEmpty()) return

        val paddingLeft = 120f
        val paddingBottom = 80f
        val paddingTop = 20f
        val paddingRight = 20f

        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom

        // Draw axes
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, axisPaint)
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, axisPaint)

        // Draw Y-axis labels and grid lines
        val bpmStep = bpmRange / (labelCountY - 1)
        for (i in 0 until labelCountY) {
            val bpm = minBpm + i * bpmStep
            val y = height - paddingBottom - (bpm - minBpm) / bpmRange * graphHeight
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint)
            val label = bpm.toInt().toString()
            val labelWidth = textPaint.measureText(label)
            canvas.drawText(label, paddingLeft - labelWidth - 10f, y + textPaint.textSize / 3, textPaint)
        }

        // Draw X-axis labels
        val totalPoints = dataPoints.size
        val xStep = totalPoints / (labelCountX - 1)
        for (i in 0 until labelCountX) {
            val index = (i * xStep).coerceAtMost(totalPoints - 1)
            if (index % granularityX.toInt() != 0) continue
            val x = paddingLeft + (index.toFloat() / (totalPoints - 1)) * graphWidth
            val time = timeFormat.format(dataPoints[index].time)
            val labelWidth = textPaint.measureText(time)
            canvas.drawText(time, x - labelWidth / 2, height - paddingBottom + textPaint.textSize + 10f, textPaint)
        }

        // Draw the heart rate line
        drawHeartRateLine(canvas, paddingLeft, paddingTop, graphWidth, graphHeight)

        // Draw the selected bpm label at the top if a point is selected
        if (showLabel && selectedDataPoint != null) {
            drawBpmLabel(canvas, selectedDataPoint!!)
        }
    }

    private fun drawHeartRateLine(canvas: Canvas, paddingLeft: Float, paddingTop: Float, graphWidth: Float, graphHeight: Float) {
        val totalPoints = dataPoints.size
        if (totalPoints < 2) return
        for (i in 0 until totalPoints - 1) {
            val startPoint = dataPoints[i]
            val endPoint = dataPoints[i + 1]
            val startX = paddingLeft + (i.toFloat() / (totalPoints - 1)) * graphWidth
            val startY = height - paddingTop - ((startPoint.bpm - minBpm) / bpmRange) * graphHeight
            val endX = paddingLeft + ((i + 1).toFloat() / (totalPoints - 1)) * graphWidth
            val endY = height - paddingTop - ((endPoint.bpm - minBpm) / bpmRange) * graphHeight
            val avgBpm = (startPoint.bpm + endPoint.bpm) / 2f
            val color = when {
                avgBpm <= 118 -> Color.BLUE
                avgBpm in 121.0..134.0 -> Color.GREEN
                avgBpm in 134.1..155.0 -> Color.YELLOW
                avgBpm > 155 -> Color.RED
                else -> Color.BLUE
            }
            linePaint.color = color
            path.reset()
            path.moveTo(startX, startY)
            path.lineTo(endX, endY)
            canvas.drawPath(path, linePaint)
        }
    }

    private fun drawBpmLabel(canvas: Canvas, dataPoint: HRDataPoint) {
        val bpmText = "${dataPoint.bpm.toInt()} bpm"
        val timeText = timeFormat.format(dataPoint.time)
        val bpmTextWidth = labelTextPaint.measureText(bpmText)
        val timeTextWidth = labelTextPaint.measureText(timeText)
        val heartIconSize = 24f // Adjusted to match the image
        val paddingHorizontal = 16f // Horizontal padding inside the box
        val paddingVertical = 8f // Vertical padding inside the box
        val labelHeight = 48f // Adjusted to match the image
        val labelMarginTop = 10f // Margin from the top of the view
        val cornerRadius = 12f // Rounded corner radius

        // Calculate the total width of the label (including bpm, heart icon, time, and padding)
        val totalLabelWidth = bpmTextWidth + heartIconSize + timeTextWidth + 3 * paddingHorizontal
        val labelLeft = (width - totalLabelWidth) / 2
        val labelTop = labelMarginTop
        val labelRight = labelLeft + totalLabelWidth
        val labelBottom = labelTop + labelHeight

        // Draw the background (white rounded rectangle with shadow)
        val labelRect = RectF(labelLeft, labelTop, labelRight, labelBottom)
        canvas.drawRoundRect(labelRect, cornerRadius, cornerRadius, labelBackgroundPaint)

        // Draw the bpm text
        val bpmTextX = labelLeft + paddingHorizontal
        val textY = labelTop + labelHeight / 2 + labelTextPaint.textSize / 3
        canvas.drawText(bpmText, bpmTextX, textY, labelTextPaint)

        // Draw the heart icon
        val heartIconX = bpmTextX + bpmTextWidth + paddingHorizontal
        val heartIconY = labelTop + (labelHeight - heartIconSize) / 2
        drawHeartIcon(canvas, heartIconX, heartIconY, heartIconSize)

        // Draw the time text
        val timeTextX = heartIconX + heartIconSize + paddingHorizontal
        canvas.drawText(timeText, timeTextX, textY, labelTextPaint)
    }

    private fun drawHeartIcon(canvas: Canvas, x: Float, y: Float, size: Float) {
        val path = Path()
        val halfSize = size / 2
        val quarterSize = size / 4

        path.moveTo(x + halfSize, y + size)
        path.cubicTo(x + halfSize, y + size, x, y + quarterSize, x, y + quarterSize)
        path.cubicTo(x - quarterSize, y - quarterSize, x + quarterSize, y - quarterSize, x + halfSize, y + quarterSize)
        path.cubicTo(x + halfSize, y + quarterSize, x + size, y - quarterSize, x + size + quarterSize, y + quarterSize)
        path.cubicTo(x + size, y + quarterSize, x + halfSize, y + size, x + halfSize, y + size)
        path.close()

        canvas.drawPath(path, heartIconPaint)
    }
}