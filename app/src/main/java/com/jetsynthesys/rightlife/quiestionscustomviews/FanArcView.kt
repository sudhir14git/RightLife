package com.jetsynthesys.rightlife.quiestionscustomviews

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kotlin.math.min

class FanArcView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null
) : View(context, attrs) {

    private val paint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
    }

    private var numberOfSegments = 3
    private var arcsPerSegment = 5

    // Ratios → adjust stroke thickness & spacing
    private var arcStrokeRatio = 0.08f   // 7% of size
    private var arcSpacingRatio = 0.03f  // was 0.01 → increased spacing between arcs

    private var segmentColors: List<List<Int>> = listOf(
        listOf(Color.LTGRAY, Color.YELLOW, Color.BLUE, Color.CYAN, Color.GREEN),
        listOf(Color.LTGRAY, Color.BLUE, Color.CYAN, Color.GREEN, Color.GREEN),
        listOf(Color.RED, Color.YELLOW, Color.BLUE, Color.CYAN, Color.LTGRAY)
    )

    private val startAngles = listOf(180f, 250f, 320f)
    private val sweepAngles = listOf(45f, 45f, 45f)

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val size = min(width, height).toFloat()
        val centerX = width / 2f
        val centerY = height * 0.9f

        val arcStrokeWidth = size * arcStrokeRatio
        val arcSpacing = size * arcSpacingRatio
        val maxRadius = size / 1.5f

        for (segmentIndex in 0 until numberOfSegments) {
            for (arcIndex in 0 until arcsPerSegment) {
                val radius = maxRadius - (arcsPerSegment - 1 - arcIndex) * (arcStrokeWidth + arcSpacing)
                paint.strokeWidth = arcStrokeWidth
                paint.color = segmentColors.getOrNull(segmentIndex)?.getOrNull(arcIndex) ?: Color.LTGRAY

                val rect = RectF(
                    centerX - radius,
                    centerY - radius,
                    centerX + radius,
                    centerY + radius
                )

                val startAngle = startAngles[segmentIndex] - (arcIndex * 2) + 1f
                val sweepAngle = sweepAngles[segmentIndex] + (arcIndex * 2) - 2f

                canvas.drawArc(rect, startAngle, sweepAngle, false, paint)
            }
        }
    }

    fun setSegmentColors(colors: List<List<Int>>) {
        segmentColors = colors
        invalidate()
    }
}
