package com.example.rlapp.ai_package.ui.eatright.fragment

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View

class GlassWithWaterView(context: Context, attrs: AttributeSet?) : View(context, attrs) {

    private val glassPaint: Paint = Paint()
    private val waterPaint: Paint = Paint()
    private val waterLevelPaint: Paint = Paint()
    private val dashedLinePaint: Paint = Paint()
    private val checkmarkPaint: Paint = Paint()
    private val checkmarkIconPaint: Paint = Paint()

    private var waterHeight = 0f
    private val glassHeight = 120f // Increased from 95f to 120f (26% increase)
    private val glassTopWidth = 100f // Increased from 80f to 100f (25% increase)
    private val glassBottomWidth = 75f // Increased from 60f to 75f (25% increase)
    private var targetWaterLevel = 0.8f // Default to 80%
    private val glassPadding = 10f // Increased from 8f to 10f (25% increase)
    private val checkmarkRadius = 10f // Increased from 8f to 10f (25% increase)
    private val checkmarkMarginTop = 15f // Increased from 12f to 15f (25% increase)
    private val checkmarkMarginEnd = 5f // Increased from 4f to 5f (25% increase)

    init {
        glassPaint.color = Color.parseColor("#B0BEC5")
        glassPaint.style = Paint.Style.STROKE
        glassPaint.strokeWidth = 5f // Increased from 4f to 5f (25% increase)
        glassPaint.isAntiAlias = true

        waterPaint.color = Color.parseColor("#4CAF50")
        waterPaint.style = Paint.Style.FILL
        waterPaint.isAntiAlias = true

        waterLevelPaint.color = Color.parseColor("#4CAF50")
        waterLevelPaint.style = Paint.Style.STROKE
        waterLevelPaint.strokeWidth = 2.5f // Increased from 2f to 2.5f (25% increase)
        waterLevelPaint.isAntiAlias = true

        dashedLinePaint.color = Color.parseColor("#B0BEC5")
        dashedLinePaint.style = Paint.Style.STROKE
        dashedLinePaint.strokeWidth = 2.5f // Increased from 2f to 2.5f (25% increase)
        dashedLinePaint.pathEffect = DashPathEffect(floatArrayOf(12.5f, 12.5f), 0f) // Increased dash pattern from 10f to 12.5f (25% increase)
        dashedLinePaint.isAntiAlias = true

        checkmarkPaint.color = Color.parseColor("#4CAF50")
        checkmarkPaint.style = Paint.Style.FILL
        checkmarkPaint.isAntiAlias = true

        checkmarkIconPaint.color = Color.WHITE
        checkmarkIconPaint.style = Paint.Style.STROKE
        checkmarkIconPaint.strokeWidth = 2.5f // Increased from 2f to 2.5f (25% increase)
        checkmarkIconPaint.strokeCap = Paint.Cap.ROUND
        checkmarkIconPaint.strokeJoin = Paint.Join.ROUND
        checkmarkIconPaint.isAntiAlias = true
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = resolveSize((glassTopWidth + 25f).toInt(), widthMeasureSpec) // Increased from 20f to 25f (25% increase)
        val height = resolveSize(glassHeight.toInt(), heightMeasureSpec)
        setMeasuredDimension(width, height)
    }

    fun setTargetWaterLevel(waterIntake: Float, waterGoal: Float) {
        targetWaterLevel = (waterIntake / waterGoal).coerceIn(0f, 1f)
        startWaterAnimation()
    }

    fun startWaterAnimation() {
        val animator = ValueAnimator.ofFloat(0f, glassHeight * targetWaterLevel)
        animator.duration = 3000
        animator.addUpdateListener { animation ->
            waterHeight = animation.animatedValue as Float
            invalidate()
        }
        animator.start()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val glassLeft = (width - glassTopWidth) / 2f
        val glassRight = glassLeft + glassTopWidth
        val glassTop = 0f
        val glassBottom = glassHeight

        val glassPath = Path()
        glassPath.moveTo(glassLeft, glassTop)
        glassPath.lineTo(glassRight, glassTop)
        glassPath.lineTo(glassLeft + (glassTopWidth - glassBottomWidth) / 2 + glassBottomWidth, glassBottom)
        glassPath.lineTo(glassLeft + (glassTopWidth - glassBottomWidth) / 2, glassBottom)
        glassPath.close()

        canvas.drawPath(glassPath, glassPaint)

        val innerLeft = glassLeft + glassPadding
        val innerRight = glassRight - glassPadding
        val innerBottom = glassBottom - glassPadding
        val innerTopWidth = glassTopWidth - 2 * glassPadding
        val innerBottomWidth = glassBottomWidth - 2 * glassPadding

        val waterLevelY = glassBottom - waterHeight
        val waterWidthAtTop = innerTopWidth - ((innerTopWidth - innerBottomWidth) * (waterHeight / glassHeight))
        val waterWidthAtBottom = innerBottomWidth

        val waterPath = Path()
        waterPath.moveTo(innerLeft + (innerTopWidth - waterWidthAtTop) / 2, waterLevelY)
        waterPath.lineTo(innerLeft + (innerTopWidth + waterWidthAtTop) / 2, waterLevelY)
        waterPath.lineTo(innerLeft + (innerTopWidth - innerBottomWidth) / 2 + waterWidthAtBottom, innerBottom)
        waterPath.lineTo(innerLeft + (innerTopWidth - innerBottomWidth) / 2, innerBottom)
        waterPath.close()

        val clipPath = Path()
        clipPath.moveTo(innerLeft, glassTop + glassPadding)
        clipPath.lineTo(innerRight, glassTop + glassPadding)
        clipPath.lineTo(innerLeft + (innerTopWidth - innerBottomWidth) / 2 + innerBottomWidth, innerBottom)
        clipPath.lineTo(innerLeft + (innerTopWidth - innerBottomWidth) / 2, innerBottom)
        clipPath.close()

        canvas.save()
        canvas.clipPath(clipPath)

        canvas.drawPath(waterPath, waterPaint)

        val waveHeight = 5f // Increased from 4f to 5f (25% increase)
        val wavePath = Path()
        wavePath.moveTo(innerLeft + (innerTopWidth - waterWidthAtTop) / 2, waterLevelY)
        for (x in (innerLeft + (innerTopWidth - waterWidthAtTop) / 2).toInt() until (innerLeft + (innerTopWidth + waterWidthAtTop) / 2).toInt() step 10) { // Increased step from 8 to 10 (25% increase)
            val y = waterLevelY + (Math.sin(x * 0.1).toFloat() * waveHeight)
            wavePath.lineTo(x.toFloat(), y)
        }
        wavePath.lineTo(innerLeft + (innerTopWidth + waterWidthAtTop) / 2, waterLevelY)
        canvas.drawPath(wavePath, waterLevelPaint)

        canvas.restore()

        val dashedLineY = glassHeight * (1 - targetWaterLevel)
        canvas.drawLine(
            glassLeft - 12.5f, dashedLineY, // Increased from 10f to 12.5f (25% increase)
            glassRight + 12.5f, dashedLineY, // Increased from 10f to 12.5f (25% increase)
            dashedLinePaint
        )

        val checkmarkCenterX = glassRight + 12.5f + checkmarkRadius + checkmarkMarginEnd // Adjusted for new dashed line extension
        val checkmarkCenterY = dashedLineY - (checkmarkRadius + checkmarkMarginTop - dashedLineY)
        canvas.drawCircle(checkmarkCenterX, checkmarkCenterY, checkmarkRadius, checkmarkPaint)

        val checkmarkPath = Path()
        checkmarkPath.moveTo(checkmarkCenterX - 5f, checkmarkCenterY) // Increased from 4f to 5f (25% increase)
        checkmarkPath.lineTo(checkmarkCenterX - 1.25f, checkmarkCenterY + 3.75f) // Adjusted proportionally
        checkmarkPath.lineTo(checkmarkCenterX + 5f, checkmarkCenterY - 2.5f) // Adjusted proportionally
        canvas.drawPath(checkmarkPath, checkmarkIconPaint)
    }
}