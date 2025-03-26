package com.example.rlapp.ai_package.ui.eatright.fragment

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import com.example.rlapp.R

class HalfCurveProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = Color.LTGRAY
        strokeCap = Paint.Cap.ROUND
    }

    private val progressPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = 20f
        color = resources.getColor(R.color.border_green)
        strokeCap = Paint.Cap.ROUND
    }

    private val textPaintMain = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 70f
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val textPaintSecondary = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 30f
        color = Color.LTGRAY
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
    }

    private val labelPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 18f
        color = resources.getColor(R.color.border_green)
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
    }

    private val rectF = RectF()
    private var currentValue: Int = 2285
    private var maxValue: Int = 2000
    private var progress: Float = 0f
    private var animatedProgress: Float = 0f
    private val startAngle = 150f
    private val sweepAngleMax = 240f
    private val arcRadiusDp = 130f

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.HalfCurveProgressBar, defStyleAttr, 0)
        currentValue = typedArray.getInt(R.styleable.HalfCurveProgressBar_currentValue, 2285)
        maxValue = typedArray.getInt(R.styleable.HalfCurveProgressBar_maxValue, 2000)
        backgroundPaint.color = typedArray.getColor(R.styleable.HalfCurveProgressBar_backgroundColor, Color.LTGRAY)
        progressPaint.color = typedArray.getColor(R.styleable.HalfCurveProgressBar_progressColor, resources.getColor(R.color.border_green))
        backgroundPaint.strokeWidth = typedArray.getDimension(R.styleable.HalfCurveProgressBar_strokeWidth, 40f)
        progressPaint.strokeWidth = typedArray.getDimension(R.styleable.HalfCurveProgressBar_strokeWidth, 40f)
        textPaintMain.textSize = typedArray.getDimension(R.styleable.HalfCurveProgressBar_textSizeMain, 70f)
        typedArray.recycle()

        updateProgress()
    }

    fun setValues(current: Int, max: Int) {
        currentValue = current
        maxValue = max
        updateProgress()
        invalidate()
    }

    fun setProgress(progress: Float) {
        animatedProgress = progress.coerceIn(0f, this.progress)
        invalidate()
    }

    private fun updateProgress() {
        progress = if (maxValue > 0) {
            (currentValue.toFloat() / maxValue.toFloat() * 100f).coerceIn(0f, 100f)
        } else {
            0f
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val density = resources.displayMetrics.density
        val arcRadius = arcRadiusDp * density
        val arcDiameter = 2 * arcRadius
        val strokeWidth = backgroundPaint.strokeWidth
        val desiredWidth = ((arcDiameter + strokeWidth) + 0f).toInt()
        val desiredHeight = (arcRadius + strokeWidth + 100f).toInt()
        val width = resolveSize(desiredWidth, widthMeasureSpec)
        val height = resolveSize(desiredHeight, heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val width = width.toFloat()
        val height = height.toFloat()
        val centerX = width / 2f
        val density = resources.displayMetrics.density
        val radius = arcRadiusDp * density
        val paddingTop = 20f // Padding from the top
        val centerY = radius + paddingTop + backgroundPaint.strokeWidth / 2 // Center the arc vertically

        // Set the RectF for the arc
        rectF.set(
            centerX - radius,
            centerY - radius,
            centerX + radius,
            centerY + radius
        )

        // Draw the background arc
        canvas.drawArc(rectF, startAngle, sweepAngleMax, false, backgroundPaint)

        // Draw the progress arc
        val sweepAngle = (animatedProgress / 100f) * sweepAngleMax
        canvas.drawArc(rectF, startAngle, sweepAngle, false, progressPaint)

        // Draw the text (adjust positions based on the new centerY)
        drawMainText(canvas, centerX, centerY)
        drawLabel(canvas, centerX, centerY + radius / 2 + 40f + 30f)
    }

    private fun drawMainText(canvas: Canvas, centerX: Float, centerY: Float) {
        val textY = centerY - 40f // Adjusted to move the text up due to larger arc
        canvas.drawText("$currentValue", centerX, textY, textPaintMain)
        canvas.drawText("/ $maxValue KCal", centerX, textY + 40f, textPaintSecondary)
    }

    private fun drawLabel(canvas: Canvas, centerX: Float, yPosition: Float) {
        canvas.drawText("Calories", centerX, yPosition, labelPaint)
    }
}