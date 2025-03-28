package com.example.rlapp.ai_package.ui.moveright.customProgressBar

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class StripedProgressBar @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) : View(context, attrs, defStyleAttr) {
    private val stripeHeight = 5f
    private val stripeAngle = -25f
    private val backgroundPaint = Paint().apply {
        color = Color.LTGRAY
        style = Paint.Style.FILL
    }
    private val whiteStripePaint = Paint().apply {
        color = Color.WHITE
        style = Paint.Style.FILL
    }
    private val progressPaint = Paint().apply {
        color = Color.RED
        style = Paint.Style.FILL
    }
    private val cornerRadius = 20f
    var progress: Float = 50f
        set(value) {
            field = value.coerceIn(0f, 100f)
            invalidate()
        }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val widthProgress = width * (progress / 100)
        canvas.save()
        canvas.rotate(stripeAngle)
        drawStripes(canvas)
        canvas.restore()
        val path = Path().apply {
            addRoundRect(0f, 0f, widthProgress, height.toFloat(), cornerRadius, cornerRadius, Path.Direction.CW)
        }
        canvas.clipPath(path)
        canvas.drawRoundRect(0f, 0f, widthProgress, height.toFloat(), cornerRadius, cornerRadius, progressPaint)
    }

    private fun drawStripes(canvas: Canvas) {
        var isWhite = true
        val stripeSpacing = stripeHeight / Math.cos(Math.toRadians(stripeAngle.toDouble())).toFloat()
        val cornerRadius = stripeHeight / 2f
        var i = -width.toFloat()
        while (i < height + width) {
            val stripePaint = if (isWhite) whiteStripePaint else backgroundPaint
            val rect = RectF(
                -width.toFloat(),
                i,
                width.toFloat() * 2,
                i + stripeSpacing
            )
            canvas.drawRoundRect(rect, cornerRadius, cornerRadius, stripePaint)
            isWhite = !isWhite
            i += stripeSpacing
        }
    }
}