package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import com.jetsynthesys.rightlife.ai_package.model.CardItem
import java.text.SimpleDateFormat
import java.util.Locale

class CustomHeartRateGraph @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val dataPoints = mutableListOf<HRDataPoint>()
    private var workoutData: CardItem? = null

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
        setShadowLayer(8f, 0f, 4f, Color.argb(80, 0, 0, 0))
    }
    private val labelTextPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 32f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }
    private val heartIconPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.RED
        style = Paint.Style.FILL
    }

    // Additional paints for enhanced display
    private val workoutInfoPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.GRAY
        textSize = 24f
    }
    private val workoutTitlePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.BLACK
        textSize = 28f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val path = Path()
    // Dynamic min/max based on data points
    private var minBpm = 60f
    private var maxBpm = 180f
    private var bpmRange = maxBpm - minBpm
    private val timeFormat = SimpleDateFormat("h:mm a", Locale.ENGLISH)

    private var selectedDataPoint: HRDataPoint? = null
    private var showLabel = false

    // Enhanced setData method that accepts CardItem
    fun setWorkoutData(cardItem: CardItem) {
        this.workoutData = cardItem
        dataPoints.clear()
        cardItem.heartRateData.forEach { heartRateData ->
            try {
                val zdt = java.time.ZonedDateTime.parse(heartRateData.date)
                val millis = zdt.toInstant().toEpochMilli()
                val bpm = heartRateData.heartRate.toInt()
                dataPoints.add(HRDataPoint(millis, bpm))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        // Calculate dynamic min/max with some padding
        if (dataPoints.isNotEmpty()) {
            val minDataBpm = dataPoints.minOf { it.bpm }.toFloat()
            val maxDataBpm = dataPoints.maxOf { it.bpm }.toFloat()
            val padding = (maxDataBpm - minDataBpm) * 0.1f // 10% padding
            minBpm = (minDataBpm - padding).coerceAtLeast(40f)
            maxBpm = (maxDataBpm + padding).coerceAtMost(220f)
            bpmRange = maxBpm - minBpm
        }

        invalidate()
    }

    // Keep existing setData method for backward compatibility
    fun setData(points: List<HRDataPoint>) {
        dataPoints.clear()
        dataPoints.addAll(points)

        // Calculate dynamic min/max with some padding
        if (dataPoints.isNotEmpty()) {
            val minDataBpm = dataPoints.minOf { it.bpm }.toFloat()
            val maxDataBpm = dataPoints.maxOf { it.bpm }.toFloat()
            val padding = (maxDataBpm - minDataBpm) * 0.1f // 10% padding
            minBpm = (minDataBpm - padding).coerceAtLeast(40f)
            maxBpm = (maxDataBpm + padding).coerceAtMost(220f)
            bpmRange = maxBpm - minBpm
        }

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
                if (touchX >= paddingLeft && touchX <= width - paddingRight && totalPoints > 0) {
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

        if (dataPoints.isEmpty()) {
            drawNoDataMessage(canvas)
            return
        }

        val paddingLeft = 120f
        val paddingBottom = 120f // Increased for workout info
        val paddingTop = 60f // Increased for title
        val paddingRight = 20f

        val graphWidth = width - paddingLeft - paddingRight
        val graphHeight = height - paddingTop - paddingBottom

        // Draw axes
        canvas.drawLine(paddingLeft, paddingTop, paddingLeft, height - paddingBottom, axisPaint)
        canvas.drawLine(paddingLeft, height - paddingBottom, width - paddingRight, height - paddingBottom, axisPaint)

        // Draw Y-axis labels and grid lines (dynamic based on data)
        drawYAxisLabels(canvas, paddingLeft, paddingTop, paddingRight, graphHeight, paddingBottom)

        // Draw the heart rate line
        drawHeartRateLine(canvas, paddingLeft, paddingTop, paddingBottom, graphWidth, graphHeight)

        // Draw X-axis labels (improved time labels)
        drawXAxisLabels(canvas, paddingLeft, paddingBottom, graphWidth)

        // Draw workout statistics at bottom
        workoutData?.let { workout ->
            drawWorkoutStatistics(canvas, workout, paddingBottom)
        }

        // Draw the selected bpm label if a point is selected
        if (showLabel && selectedDataPoint != null) {
            drawBpmLabel(canvas, selectedDataPoint!!)
        }
    }

    private fun drawYAxisLabels(canvas: Canvas, paddingLeft: Float, paddingTop: Float, paddingRight: Float, graphHeight: Float, paddingBottom: Float) {
        // Generate 6-8 evenly spaced labels between min and max
        val labelCount = 7
        val step = bpmRange / (labelCount - 1)

        for (i in 0 until labelCount) {
            val bpm = minBpm + (i * step)
            // Map BPM to Y position, ensuring minBpm aligns with X-axis at height - paddingBottom
            val y = (height - paddingBottom) - ((bpm - minBpm) / bpmRange) * graphHeight
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridPaint)
            val label = bpm.toInt().toString()
            val labelWidth = textPaint.measureText(label)
            canvas.drawText(label, paddingLeft - labelWidth - 10f, y + textPaint.textSize / 3, textPaint)
        }
    }

    private fun drawXAxisLabels(canvas: Canvas, paddingLeft: Float, paddingBottom: Float, graphWidth: Float) {
        val totalPoints = dataPoints.size
        if (totalPoints < 2) return

        // Show start, end, and 3-4 points in between (total 5-6 labels)
        val labelIndices = mutableListOf<Int>()

        // Always include start
        labelIndices.add(0)

        // Add 3-4 middle points
        val middleCount = 3
        for (i in 1..middleCount) {
            val index = (totalPoints * i / (middleCount + 1)).coerceAtMost(totalPoints - 2)
            if (index > labelIndices.last()) {
                labelIndices.add(index)
            }
        }

        // Always include end
        if (totalPoints > 1 && !labelIndices.contains(totalPoints - 1)) {
            labelIndices.add(totalPoints - 1)
        }

        // Draw the labels
        labelIndices.forEach { index ->
            val x = paddingLeft + (index.toFloat() / (totalPoints - 1)) * graphWidth
            val time = timeFormat.format(dataPoints[index].time)
            // Split time into hour-minute and AM/PM parts
            val parts = time.split(" ")
            val timePart = parts.getOrNull(0) ?: time
            val amPmPart = (parts.getOrNull(1) ?: "").uppercase(Locale.ENGLISH) // Capitalize AM/PM

            // Calculate text widths
            val timeWidth = textPaint.measureText(timePart)
            val amPmWidth = textPaint.measureText(amPmPart)

            // Adjust X position for the last label to prevent clipping
            val isLastLabel = index == totalPoints - 1
            val timeX = if (isLastLabel) {
                // Align so the right edge of the time part doesn't exceed the right boundary
                (width - paddingRight - timeWidth).coerceAtLeast(paddingLeft)
            } else {
                x - timeWidth / 2
            }

            // Draw time part (e.g., "9:23")
            canvas.drawText(timePart, timeX, height - paddingBottom + textPaint.textSize + 10f, textPaint)

            // Draw AM/PM part below time
            if (amPmPart.isNotEmpty()) {
                // Adjust X position for AM/PM part of the last label
                val amPmX = if (isLastLabel) {
                    // Align so the right edge of the AM/PM part doesn't exceed the right boundary
                    (width - paddingRight - amPmWidth).coerceAtLeast(paddingLeft)
                } else {
                    x - amPmWidth / 2
                }
                canvas.drawText(amPmPart, amPmX, height - paddingBottom + textPaint.textSize + 10f + textPaint.textSize, textPaint)
            }
        }
    }

    private fun drawWorkoutStatistics(canvas: Canvas, workout: CardItem, paddingBottom: Float) {
        val startY = height - paddingBottom + 20f
        val lineHeight = workoutInfoPaint.textSize + 6f

        val stats = mutableListOf<String>()

        stats.take(4).forEachIndexed { index, stat ->
            canvas.drawText(stat, 40f, startY + (index * lineHeight), workoutInfoPaint)
        }
    }

    private fun drawNoDataMessage(canvas: Canvas) {
        val message = "No workout data available"
        val messageWidth = textPaint.measureText(message)
        val messageX = (width - messageWidth) / 2
        val messageY = height / 2f
        canvas.drawText(message, messageX, messageY, textPaint)
    }

    private fun getColorForBpm(bpm: Float): Int {
        val heartRateZones = workoutData?.heartRateZones
        return when {
            heartRateZones == null -> {
                when {
                    bpm < 121f -> Color.rgb(255, 182, 193)
                    bpm in 121f..132f -> Color.BLUE
                    bpm in 132f..143f -> Color.GREEN
                    bpm in 143f..166f -> Color.YELLOW
                    bpm >= 166f -> Color.RED
                    else -> Color.BLUE
                }
            }
            bpm < (heartRateZones.lightZone.getOrNull(0)?.toFloat() ?: 121f) -> Color.rgb(255, 182, 193)
            heartRateZones.lightZone.size >= 2 && bpm in heartRateZones.lightZone[0].toFloat()..heartRateZones.lightZone[1].toFloat() -> Color.BLUE
            heartRateZones.fatBurnZone.size >= 2 && bpm in heartRateZones.fatBurnZone[0].toFloat()..heartRateZones.fatBurnZone[1].toFloat() -> Color.GREEN
            heartRateZones.cardioZone.size >= 2 && bpm in heartRateZones.cardioZone[0].toFloat()..heartRateZones.cardioZone[1].toFloat() -> Color.YELLOW
            heartRateZones.peakZone.size >= 1 && bpm >= heartRateZones.peakZone[0].toFloat() -> Color.RED
            else -> Color.BLUE
        }
    }

    private fun drawHeartRateLine(canvas: Canvas, paddingLeft: Float, paddingTop: Float, paddingBottom: Float, graphWidth: Float, graphHeight: Float) {
        val totalPoints = dataPoints.size
        if (totalPoints < 2) return

        // Get heart rate zones
        val heartRateZones = workoutData?.heartRateZones ?: return  // No zones, skip or draw default color

        // Collect all zone boundaries
        val boundaries = mutableSetOf<Float>()
        heartRateZones.lightZone.getOrNull(0)?.toFloat()?.let { boundaries.add(it) }
        heartRateZones.lightZone.getOrNull(1)?.toFloat()?.let { boundaries.add(it) }
        heartRateZones.fatBurnZone.getOrNull(0)?.toFloat()?.let { boundaries.add(it) }
        heartRateZones.fatBurnZone.getOrNull(1)?.toFloat()?.let { boundaries.add(it) }
        heartRateZones.cardioZone.getOrNull(0)?.toFloat()?.let { boundaries.add(it) }
        heartRateZones.cardioZone.getOrNull(1)?.toFloat()?.let { boundaries.add(it) }
        heartRateZones.peakZone.getOrNull(0)?.toFloat()?.let { boundaries.add(it) }
        heartRateZones.peakZone.getOrNull(1)?.toFloat()?.let { boundaries.add(it) }
        val zoneBoundaries = boundaries.toList().sorted()

        // Calculate X-axis position
        val xAxisY = height - paddingBottom

        for (i in 0 until totalPoints - 1) {
            val startPoint = dataPoints[i]
            val endPoint = dataPoints[i + 1]
            val startBpm = startPoint.bpm.toFloat()
            val endBpm = endPoint.bpm.toFloat()
            val startX = paddingLeft + (i.toFloat() / (totalPoints - 1)) * graphWidth

            // Fixed Y calculation - ensure line stays above X-axis
            val startYRatio = ((startBpm - minBpm) / bpmRange).coerceIn(0f, 1f)
            val startY = xAxisY - (startYRatio * graphHeight)

            val endX = paddingLeft + ((i + 1).toFloat() / (totalPoints - 1)) * graphWidth

            // Fixed Y calculation - ensure line stays above X-axis
            val endYRatio = ((endBpm - minBpm) / bpmRange).coerceIn(0f, 1f)
            val endY = xAxisY - (endYRatio * graphHeight)

            if (startBpm == endBpm) {
                linePaint.color = getColorForBpm(startBpm)
                canvas.drawLine(startX, startY, endX, endY, linePaint)
                continue
            }

            val minBpmSeg = minOf(startBpm, endBpm)
            val maxBpmSeg = maxOf(startBpm, endBpm)
            val crossingBpms = zoneBoundaries.filter { it > minBpmSeg && it < maxBpmSeg }

            val allBpms = (listOf(startBpm) + crossingBpms + listOf(endBpm)).distinct()
                .sortedWith(if (startBpm < endBpm) Comparator.naturalOrder() else Comparator.reverseOrder())

            for (j in 0 until allBpms.size - 1) {
                val thisBpm = allBpms[j]
                val nextBpm = allBpms[j + 1]
                val avgBpm = (thisBpm + nextBpm) / 2f
                linePaint.color = getColorForBpm(avgBpm)

                val tThis = if (endBpm - startBpm != 0f) (thisBpm - startBpm) / (endBpm - startBpm) else 0f
                val xThis = startX + tThis * (endX - startX)
                val yThis = startY + tThis * (endY - startY)

                val tNext = if (endBpm - startBpm != 0f) (nextBpm - startBpm) / (endBpm - startBpm) else 0f
                val xNext = startX + tNext * (endX - startX)
                val yNext = startY + tNext * (endY - startY)

                canvas.drawLine(xThis, yThis, xNext, yNext, linePaint)
            }
        }
    }

    private fun drawBpmLabel(canvas: Canvas, dataPoint: HRDataPoint) {
        val bpmText = "${dataPoint.bpm} bpm"
        val time = timeFormat.format(dataPoint.time)
        val parts = time.split(" ")
        val timePart = parts.getOrNull(0) ?: time
        val amPmPart = (parts.getOrNull(1) ?: "").uppercase(Locale.ENGLISH) // Capitalize AM/PM
        val timeText = "$timePart $amPmPart" // Reconstruct as "9:23 AM"
        val bpmTextWidth = labelTextPaint.measureText(bpmText)
        val timeTextWidth = labelTextPaint.measureText(timeText)
        val heartIconSize = 24f
        val paddingHorizontal = 16f
        val paddingVertical = 8f
        val labelHeight = 48f
        val labelMarginTop = 10f
        val cornerRadius = 12f

        val totalLabelWidth = bpmTextWidth + heartIconSize + timeTextWidth + 3 * paddingHorizontal
        val labelLeft = (width - totalLabelWidth) / 2
        val labelTop = labelMarginTop
        val labelRight = labelLeft + totalLabelWidth
        val labelBottom = labelTop + labelHeight

        val labelRect = RectF(labelLeft, labelTop, labelRight, labelBottom)
        canvas.drawRoundRect(labelRect, cornerRadius, cornerRadius, labelBackgroundPaint)

        val bpmTextX = labelLeft + paddingHorizontal
        val textY = labelTop + labelHeight / 2 + labelTextPaint.textSize / 3
        canvas.drawText(bpmText, bpmTextX, textY, labelTextPaint)

        val heartIconX = bpmTextX + bpmTextWidth + paddingHorizontal
        val heartIconY = labelTop + (labelHeight - heartIconSize) / 2
        drawHeartIcon(canvas, heartIconX, heartIconY, heartIconSize)

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