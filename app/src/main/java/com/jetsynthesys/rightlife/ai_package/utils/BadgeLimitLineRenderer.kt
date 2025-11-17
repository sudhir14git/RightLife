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

    private val labelPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.WHITE
        textSize = 26f
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    private val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.parseColor("#12B886")
        style = Paint.Style.FILL
    }

    private val cornerRadius = 13f

    override fun renderLimitLines(c: Canvas?) {
        super.renderLimitLines(c)
        val limitLines = mYAxis.limitLines
        if (limitLines.isNullOrEmpty()) return

        val pos = FloatArray(2)

        limitLines.forEach { line ->

            // Convert limit value to pixel position
            pos[0] = 0f
            pos[1] = line.limit
            mTrans.pointValuesToPixel(pos)

            val yPixel = pos[1]

            // Use label text width/height based on labelPaint
            val text = line.label ?: return@forEach

            val paddingH = 10f   // horizontal padding (reduced)
            val paddingV = 20f   // vertical padding (same height)
            val textWidth = labelPaint.measureText(text)
            val textHeight = labelPaint.fontMetrics.run { bottom - top }

            val badgeWidth = textWidth + paddingH * 2
            val badgeHeight = textHeight + paddingV

            // X position (right-aligned inside content)
            val right = mViewPortHandler.contentRight()
            val left = right - badgeWidth
            val top = yPixel - badgeHeight / 2
            val bottom = yPixel + badgeHeight / 2

            // Set color by label
            bgPaint.color = when (text) {
                "G" -> Color.parseColor("#06B27B")
                else -> Color.parseColor("#707070")
            }

            // Draw background
            c?.drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, bgPaint)

            // Draw centered label
            val baseline = (top + bottom) / 2 - (labelPaint.descent() + labelPaint.ascent()) / 2
            c?.drawText(text, (left + right) / 2, baseline, labelPaint)
        }
    }
}

