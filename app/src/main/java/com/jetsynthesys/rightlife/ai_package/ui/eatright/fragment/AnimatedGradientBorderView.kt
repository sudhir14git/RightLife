package com.jetsynthesys.rightlife.ai_package.ui.eatright.fragment

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.animation.ValueAnimator
import android.view.animation.AccelerateDecelerateInterpolator

class AnimatedGlowGradientBorderView @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : View(context, attrs) {

    private val cornerRadius = 28f
    private val strokeWidth = 6f
    private var angle = 0f
    private var blurRadius = 10f

    private val gradientColors = intArrayOf(
        Color.parseColor("#FAD2C0"),
        Color.parseColor("#F48FB1"),
        Color.parseColor("#9FA8DA"),
        Color.parseColor("#90CAF9")
    )

    private val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.FILL
        color = Color.WHITE
    }

    private val strokePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@AnimatedGlowGradientBorderView.strokeWidth
    }

    private val glowPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeWidth = this@AnimatedGlowGradientBorderView.strokeWidth
        maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.OUTER)
    }

    private val rect = RectF()
    private val matrix = Matrix()

    init {
        setLayerType(LAYER_TYPE_HARDWARE, null)
        // Angle rotation
        ValueAnimator.ofFloat(0f, 360f).apply {
            duration = 8000L
            repeatCount = ValueAnimator.INFINITE
            interpolator = LinearInterpolator()
            addUpdateListener {
                angle = it.animatedValue as Float
                invalidate()
            }
            start()
        }
        // Blur pulsing
        ValueAnimator.ofFloat(8f, 16f).apply {
            duration = 2000L
            repeatMode = ValueAnimator.REVERSE
            repeatCount = ValueAnimator.INFINITE
            interpolator = AccelerateDecelerateInterpolator()
            addUpdateListener {
                blurRadius = it.animatedValue as Float
                glowPaint.maskFilter = BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.OUTER)
                invalidate()
            }
            start()
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        val inset = strokeWidth / 2
        rect.set(inset, inset, width - inset, height - inset)
        // === Static solid white background ===
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, fillPaint) // <- Solid base (e.g., white)
        // === Create rotating angular (sweep) gradient ===
        val sweepGradient = SweepGradient(
            width / 2f, height / 2f,
            gradientColors, null
        )
        matrix.setRotate(angle, width / 2f, height / 2f)
        sweepGradient.setLocalMatrix(matrix)
        // === Fill with semi-blurred animated gradient for glow ===
        glowPaint.shader = sweepGradient
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, glowPaint)
        // === Optional: Add inner animated gradient fill layer ===
        val animatedFillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            style = Paint.Style.FILL
            shader = sweepGradient
            alpha = 50 // slight transparency over solid white
        }
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, animatedFillPaint)
        // === Stroke Border ===
        strokePaint.shader = sweepGradient
        canvas.drawRoundRect(rect, cornerRadius, cornerRadius, strokePaint)
    }
}


