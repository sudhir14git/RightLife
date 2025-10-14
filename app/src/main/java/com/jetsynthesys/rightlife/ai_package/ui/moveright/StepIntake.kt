package com.jetsynthesys.rightlife.ai_package.ui.moveright

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.DashPathEffect
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.RectF
import android.graphics.Shader
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import androidx.core.content.ContextCompat
import kotlin.math.max
import kotlin.math.min

class StepIntake : View {
    private var thumbPaint: Paint? = null
    private var fillPaint: Paint? = null
    private var backgroundPaint: Paint? = null
    private var capillaryRect: RectF? = null
    private var fillPercentage = 0f
    private var intervalColors = IntArray(10) { Color.GREEN } // Green fill
    private var backgroundColor = Color.LTGRAY

    private var minSteps = 0
    private var maxSteps = 20000
    private val stepInterval = 2000 // For gradient only
    private var listener: OnStepCountChangeListener? = null

    private var lineThickness = 0f
    private var lineGap = 0f
    private var touchAreaHeight = 0f

    private val optimalSteps = 12000
    private val stepIncrement = 500

    private var lastStepCount = 0
    private var markerDrawable: android.graphics.drawable.Drawable? = null
    private var textPaint: Paint? = null
    private var dashedLinePaint: Paint? = null

    constructor(context: Context?) : super(context) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context, attrs, defStyleAttr
    ) {
        init()
    }

    private fun init() {
        fillPaint = Paint().apply {
            style = Paint.Style.FILL
        }

        backgroundPaint = Paint().apply {
            color = backgroundColor
            style = Paint.Style.FILL
        }

        capillaryRect = RectF()

        thumbPaint = Paint().apply {
            color = Color.WHITE
            style = Paint.Style.STROKE
            isAntiAlias = true
        }

        // Text paint
        textPaint = Paint().apply {
            color = Color.WHITE
            textSize = 12f * resources.displayMetrics.density // Reduced for better fit
            textAlign = Paint.Align.LEFT
            isAntiAlias = true
        }

        // Dashed line paint
        dashedLinePaint = Paint().apply {
            style = Paint.Style.STROKE
            strokeWidth = 2f * resources.displayMetrics.density
            pathEffect = DashPathEffect(floatArrayOf(10f, 10f), 0f) // Dashed pattern
            isAntiAlias = true
        }

        val density = resources.displayMetrics.density
        lineThickness = 2f * density // Original thickness unchanged
        lineGap = 4 * density
        touchAreaHeight = (lineThickness * 2 + lineGap) * 4f
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        val padding = w / 10
        capillaryRect!![padding.toFloat(), padding.toFloat(), (w - padding).toFloat()] =
            (h - padding).toFloat()
        createGradientShader()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw background
        canvas.drawRoundRect(
            capillaryRect!!,
            capillaryRect!!.width() / 4,
            capillaryRect!!.width() / 4,
            backgroundPaint!!
        )

        // Draw fill
        val fillHeight = fillPercentage * capillaryRect!!.height()
        val fillRect = RectF(
            capillaryRect!!.left,
            capillaryRect!!.bottom - fillHeight,
            capillaryRect!!.right,
            capillaryRect!!.bottom
        )
        canvas.drawRoundRect(
            fillRect,
            capillaryRect!!.width() / 4,
            capillaryRect!!.width() / 4,
            fillPaint!!
        )

        // Draw thumb lines with further reduced width and shifted lower
        thumbPaint!!.strokeWidth = lineThickness
        val verticalOffset = 12f * resources.displayMetrics.density // Shift lines 12dp lower (was 5dp)
        val lineY1 = fillRect.top - lineGap / 2 - lineThickness / 2 + verticalOffset
        val lineY2 = fillRect.top + lineGap / 2 + lineThickness / 2 + verticalOffset
        val lineInset = capillaryRect!!.width() * 0.15f // 15% inset from each side (30% total reduction)
        canvas.drawLine(
            capillaryRect!!.left + lineInset, // Start x inset
            lineY1,
            capillaryRect!!.right - lineInset, // End x inset
            lineY1,
            thumbPaint!!
        )
        canvas.drawLine(
            capillaryRect!!.left + lineInset, // Start x inset
            lineY2,
            capillaryRect!!.right - lineInset, // End x inset
            lineY2,
            thumbPaint!!
        )

        // Draw "Your Goal" text and step count above fill level
        val yourGoalY = fillRect.top - lineGap * 2
        textPaint!!.color = Color.WHITE
        canvas.drawText(
            "",
            capillaryRect!!.centerX(),
            yourGoalY,
            textPaint!!
        )
        canvas.drawText(
            "", // Full number, e.g., "10500"
            capillaryRect!!.centerX(),
            yourGoalY - textPaint!!.textSize * 1.5f,
            textPaint!!
        )

        // Draw 12,000 Steps marker (optimalSteps)
        val optimalY = calculateYFromSteps(optimalSteps)
        dashedLinePaint!!.color = Color.RED
        textPaint!!.color = Color.RED
        canvas.save() // Save canvas state
        canvas.translate(capillaryRect!!.right + 5f, 0f) // Move right with small offset
        canvas.restore() // Restore canvas state

        // Draw 10,000 Steps (Average) marker
        val averageY = calculateYFromSteps(10000)
        dashedLinePaint!!.color = Color.GRAY
        textPaint!!.color = Color.GRAY
        canvas.save() // Save canvas state
        canvas.translate(capillaryRect!!.right + 5f, 0f) // Move right with small offset
        canvas.restore() // Restore canvas state

        // Optional: Remove if not needed
        markerDrawable?.let { drawable ->
            val markerY = calculateYFromSteps(optimalSteps)
            val markerX = capillaryRect!!.centerX() - drawable.bounds.width() / 2
            canvas.save()
            canvas.translate(markerX, markerY - drawable.bounds.height() / 2)
            drawable.draw(canvas)
            canvas.restore()
            Log.d("StepIntake", "Marker drawn at x=$markerX, y=$markerY")
        }
    }

    private fun createGradientShader() {
        val positions = FloatArray(intervalColors.size + 1)
        for (i in positions.indices) {
            positions[i] = i.toFloat() / intervalColors.size
        }
        val extendedColors = IntArray(intervalColors.size + 1)
        System.arraycopy(intervalColors, 0, extendedColors, 0, intervalColors.size)
        extendedColors[intervalColors.size] = intervalColors[intervalColors.size - 1]
        val gradient = LinearGradient(
            0f,
            capillaryRect!!.top,
            0f,
            capillaryRect!!.bottom,
            extendedColors,
            positions,
            Shader.TileMode.CLAMP
        )
        fillPaint!!.shader = gradient
    }

    private fun calculateYFromSteps(steps: Int): Float {
        val percentage = (steps - minSteps).toFloat() / (maxSteps - minSteps)
        return capillaryRect!!.bottom - percentage * capillaryRect!!.height()
    }

    private fun calculateStepsFromY(y: Float): Int {
        val percentage = (capillaryRect!!.bottom - y) / capillaryRect!!.height()
        val steps = (minSteps + percentage * (maxSteps - minSteps)).toInt()
        return max(minSteps, min(maxSteps, steps))
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val newY = max(
                    capillaryRect!!.top.toDouble(),
                    min(capillaryRect!!.bottom.toDouble(), event.y.toDouble())
                ).toFloat()
                val steps = calculateStepsFromY(newY)
                if (kotlin.math.abs(steps - lastStepCount) >= 1) {
                    lastStepCount = steps
                    fillPercentage = (steps - minSteps).toFloat() / (maxSteps - minSteps)
                    Log.d("StepIntake", "Touch: y=$newY, steps=$steps, fillPercentage=$fillPercentage")
                    invalidate()
                    updateStepCount(steps)
                }
                return true
            }
            MotionEvent.ACTION_UP -> return true
        }
        return super.onTouchEvent(event)
    }

    private fun isThumbTouched(x: Float, y: Float): Boolean {
        val fillHeight = fillPercentage * capillaryRect!!.height()
        val fillTop = capillaryRect!!.bottom - fillHeight
        val touchTop = fillTop - (lineThickness + lineGap / 2)
        val touchBottom = fillTop + (lineThickness + lineGap / 2)
        return x >= capillaryRect!!.left &&
                x <= capillaryRect!!.right &&
                y >= touchTop - touchAreaHeight / 2 &&
                y <= touchBottom + touchAreaHeight / 2
    }

    fun setIntervalColors(color: Int) {
        intervalColors = IntArray((maxSteps - minSteps) / stepInterval) { color }
        createGradientShader()
        invalidate()
    }

    override fun setBackgroundColor(color: Int) {
        backgroundColor = color
        backgroundPaint!!.color = backgroundColor
        invalidate()
    }

    fun setFillPercentage(percentage: Float) {
        fillPercentage = max(0.0, min(1.0, percentage.toDouble())).toFloat()
        lastStepCount = (minSteps + fillPercentage * (maxSteps - minSteps)).toInt()
        invalidate()
    }

    fun setMinSteps(minSteps: Int) {
        this.minSteps = minSteps
        intervalColors = IntArray((maxSteps - minSteps) / stepInterval) { Color.GREEN }
        createGradientShader()
        invalidate()
    }

    fun setMaxSteps(maxSteps: Int) {
        this.maxSteps = maxSteps
        intervalColors = IntArray((maxSteps - minSteps) / stepInterval) { Color.GREEN }
        createGradientShader()
        invalidate()
    }

    fun setOnStepCountChangeListener(listener: OnStepCountChangeListener?) {
        this.listener = listener
    }

    fun getRecommendedSteps(currentSteps: Int): Int {
        return if (currentSteps < optimalSteps) {
            min(currentSteps + stepIncrement, optimalSteps)
        } else {
            optimalSteps
        }
    }

    private fun updateStepCount(stepCount: Int) {
        Log.d("StepIntake", "Step count updated: $stepCount")
        val recommendedSteps = getRecommendedSteps(stepCount)
        listener?.onStepCountChanged(stepCount, recommendedSteps)
    }

    interface OnStepCountChangeListener {
        fun onStepCountChanged(stepCount: Int, recommendedSteps: Int)
    }
}