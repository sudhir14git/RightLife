package com.jetsynthesys.rightlife.ai_package.utils

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.graphics.Typeface
import android.text.TextPaint
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.renderer.YAxisRenderer
import com.github.mikephil.charting.utils.Transformer
import com.github.mikephil.charting.utils.ViewPortHandler

class BadgeLimitLineRenderer(
    viewPortHandler: ViewPortHandler,
    yAxis: YAxis,
    trans: Transformer
) : YAxisRenderer(viewPortHandler, yAxis, trans) {

    private val badgePaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 10f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#12B886") // Customize
        style = Paint.Style.FILL
    }

    private val cornerRadius = 10f

    override fun renderLimitLines(c: Canvas?) {
        super.renderLimitLines(c)
        val limitLines = mYAxis.limitLines
        if (limitLines.isNullOrEmpty()) return
        val positions = FloatArray(2)

        limitLines.forEach { line ->
            positions[1] = line.limit
            mTrans.pointValuesToPixel(positions)
            val label = line.label ?: return@forEach
            // Text bounds
            val textRect = Rect()
            mAxisLabelPaint.getTextBounds(label, 0, label.length, textRect)
            val left = mViewPortHandler.chartWidth - textRect.width() - 40f
            val top = positions[1] - textRect.height() - 10f
            val right = left + textRect.width() + 20f
            val bottom = positions[1] + 25f
            // Background color based on label
            badgePaint.color = if (label == "G") {
                Color.parseColor("#06B27B")
            } else {
                Color.parseColor("#707070")
            }
            // Draw rounded rectangle
            c?.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, badgePaint)
            // Draw label
            val baseline = positions[1] - (badgePaint.descent() + badgePaint.ascent()) / 2
            mAxisLabelPaint.color = Color.WHITE
            c?.drawText(label, left + 30f, baseline, mAxisLabelPaint)
        }
    }
}
