package com.example.rlapp.ai_package.ui.moveright

import android.graphics.Canvas
import android.graphics.Paint
import com.github.mikephil.charting.charts.BarLineChartBase
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.renderer.XAxisRenderer
import com.github.mikephil.charting.utils.MPPointF
import com.github.mikephil.charting.utils.ViewPortHandler

class CustomXAxisRenderer(
    viewPortHandler: ViewPortHandler,
    private val xAxis: XAxis,
    trans: com.github.mikephil.charting.utils.Transformer
) : XAxisRenderer(viewPortHandler, xAxis, trans) {

    private val days = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")
    private val dates = listOf("3 Feb", "4 Feb", "5 Feb", "6 Feb", "7 Feb", "8 Feb", "9 Feb")

    override fun drawLabels(canvas: Canvas, pos: Float, anchor: MPPointF) {
        val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = xAxis.textColor
            textSize = xAxis.textSize
            textAlign = Paint.Align.CENTER
        }

        val labelHeight = labelPaint.textSize
        val offset = labelHeight + 10f // Space between day and date

        for (i in xAxis.mEntries.indices) {
            val xPos = mTrans.getPixelForValues(xAxis.mEntries[i], 0f).x
            if (xPos >= mViewPortHandler.offsetLeft() && xPos <= mViewPortHandler.chartWidth) {
                val index = i % days.size
                canvas.drawText(days[index], xPos.toFloat(), pos, labelPaint)
                canvas.drawText(dates[index], xPos.toFloat(), pos + offset, labelPaint)
            }
        }
    }
}
