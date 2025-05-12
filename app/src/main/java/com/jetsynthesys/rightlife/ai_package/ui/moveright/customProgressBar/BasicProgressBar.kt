package com.jetsynthesys.rightlife.ai_package.ui.moveright.customProgressBar

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.RectF
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View

class BasicProgressBar @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : View(context, attrs, defStyleAttr) {

    private val backgroundPaint = Paint().apply {
        color = Color.parseColor("#D3D3D3") // Light gray background
        style = Paint.Style.FILL
    }

    private val progressPaint = Paint().apply {
        color = Color.BLACK // Black progress fill
        style = Paint.Style.FILL
    }

    private val cornerRadius = 20f
    var progress: Float = 0.5f // Progress set to 50% (0.5)
        set(value) {
            field = value.coerceIn(0f, 1f)
            onProgressChangedListener?.invoke(field)
            invalidate()
        }

    private var onProgressChangedListener: ((Float) -> Unit)? = null

    init {
        isClickable = true
        isFocusable = true
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        // Draw the background (light gray)
        canvas.drawRoundRect(
            0f,
            0f,
            width.toFloat(),
            height.toFloat(),
            cornerRadius,
            cornerRadius,
            backgroundPaint
        )

        // Draw the progress fill (black)
        val widthProgress = width * progress
        val path = Path().apply {
            addRoundRect(
                0f,
                0f,
                widthProgress,
                height.toFloat(),
                cornerRadius,
                cornerRadius,
                Path.Direction.CW
            )
        }
        canvas.clipPath(path)
        canvas.drawRoundRect(
            0f,
            0f,
            widthProgress,
            height.toFloat(),
            cornerRadius,
            cornerRadius,
            progressPaint
        )
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when (event.action) {
            MotionEvent.ACTION_DOWN, MotionEvent.ACTION_MOVE -> {
                val newProgress = (event.x / width).coerceIn(0f, 1f)
                progress = newProgress // Smooth progress without snapping
                return true
            }
        }
        return super.onTouchEvent(event)
    }

    fun setOnProgressChangedListener(listener: (Float) -> Unit) {
        this.onProgressChangedListener = listener
    }
}