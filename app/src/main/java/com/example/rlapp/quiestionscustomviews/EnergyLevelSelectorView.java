package com.example.rlapp.quiestionscustomviews;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.RectF;
import android.graphics.Shader;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

public class EnergyLevelSelectorView extends View {
    private Paint thumbPaint;
    private Paint fillPaint;
    private Paint backgroundPaint;
    private RectF capillaryRect;
    private float fillPercentage = 0f;
    private int[] intervalColors = {
            Color.RED,       // 0-2k
            Color.RED,       // 2-4k
            Color.RED,       // 4-6k
            Color.RED,      // 6-8k
            Color.RED,       // 8-10k
            Color.RED,      // 10-12k
    };
    private int backgroundColor = Color.LTGRAY;

    private int minSteps = 0;
    private int maxSteps = 12000;
    private int stepInterval = 2000;
    private OnStepCountChangeListener listener;

    private float thumbRadius;
    private float thumbY;

    public EnergyLevelSelectorView(Context context) {
        super(context);
        init();
    }

    public EnergyLevelSelectorView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public EnergyLevelSelectorView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    private void init() {
        fillPaint = new Paint();
        fillPaint.setStyle(Paint.Style.FILL);

        backgroundPaint = new Paint();
        backgroundPaint.setColor(backgroundColor);
        backgroundPaint.setStyle(Paint.Style.FILL);

        capillaryRect = new RectF();

        thumbPaint = new Paint();
        thumbPaint.setColor(Color.WHITE); // Always white thumb
        thumbPaint.setStyle(Paint.Style.FILL);
        thumbPaint.setAntiAlias(true); // Smooth edges
        thumbRadius = getResources().getDisplayMetrics().density * 15; // Adjust thumb size
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int padding = w / 10;
        capillaryRect.set(padding, padding, w - padding, h - padding);
        thumbY = calculateYFromSteps(0); // Initial thumb position at 2k steps
        createGradientShader();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Draw rounded slider
        canvas.drawRoundRect(capillaryRect, capillaryRect.width() / 2, capillaryRect.width() / 2, backgroundPaint);

        // Draw filled region
        float fillHeight = fillPercentage * capillaryRect.height();
        RectF fillRect = new RectF(capillaryRect.left, capillaryRect.bottom - fillHeight, capillaryRect.right, capillaryRect.bottom);
        canvas.drawRoundRect(fillRect, capillaryRect.width() / 2, capillaryRect.width() / 2, fillPaint);

        // Draw thumb
        canvas.drawCircle(capillaryRect.centerX(), thumbY, thumbRadius, thumbPaint);
    }

    private void createGradientShader() {
        float[] positions = new float[intervalColors.length + 1];
        for (int i = 0; i < positions.length; i++) {
            positions[i] = (float) i / intervalColors.length;
        }
        int[] extendedColors = new int[intervalColors.length + 1];
        System.arraycopy(intervalColors, 0, extendedColors, 0, intervalColors.length);
        extendedColors[intervalColors.length] = intervalColors[intervalColors.length - 1]; // Repeat last color
        LinearGradient gradient = new LinearGradient(
                0, capillaryRect.top,
                0, capillaryRect.bottom,
                extendedColors,
                positions,
                Shader.TileMode.CLAMP
        );
        fillPaint.setShader(gradient);
    }

    private float calculateYFromSteps(int steps) {
        float percentage = (float) (steps - minSteps) / (maxSteps - minSteps);
        return capillaryRect.bottom - percentage * capillaryRect.height();
    }

    private int calculateStepsFromY(float y) {
        float percentage = (capillaryRect.bottom - y) / capillaryRect.height();
        int steps = (int) (minSteps + percentage * (maxSteps - minSteps));
        return Math.round((float) steps / stepInterval) * stepInterval;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (isThumbTouched(event.getX(), event.getY()) || event.getAction() == MotionEvent.ACTION_MOVE) {
                    // Ensure the thumb stays within the vertical limits of the view
                    float newY = Math.max(capillaryRect.top + thumbRadius, Math.min(capillaryRect.bottom - thumbRadius, event.getY()));

                    int steps = calculateStepsFromY(newY);
                    float oldThumbY = thumbY; // Store the old thumbY value
                    thumbY = newY; // Assign constrained Y value
                    fillPercentage = (float) (steps - minSteps) / (maxSteps - minSteps);
                    invalidate();

                    // Log the new thumb position only if it has changed
                    if (oldThumbY != thumbY) {
                        Log.d("CapillaryView", "Thumb moved from Y: " + oldThumbY + " to Y: " + thumbY);
                        updateStepCount(steps);
                    }

                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                return isThumbTouched(event.getX(), event.getY());
        }
        return super.onTouchEvent(event);
    }

 /*   @Override
    public boolean onTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
                if (isThumbTouched(event.getX(), event.getY()) || event.getAction() == MotionEvent.ACTION_MOVE) {
                    float newY = Math.max(capillaryRect.top, Math.min(capillaryRect.bottom, event.getY()));
                    int steps = calculateStepsFromY(newY);
                    float oldThumbY = thumbY; // Store the old thumbY value
                    thumbY = calculateYFromSteps(steps);
                    fillPercentage = (float) (steps - minSteps) / (maxSteps - minSteps);
                    invalidate();
                    // Log the new thumb position only if it has changed
                    if (oldThumbY != thumbY) {
                        Log.d("CapillaryView", "Thumb moved from Y: " + oldThumbY + " to Y: " + thumbY);
                        updateStepCount(steps);
                    }


                    return true;
                }
                break;
            case MotionEvent.ACTION_UP:
                return isThumbTouched(event.getX(), event.getY());
        }
        return super.onTouchEvent(event);
    }*/

    private boolean isThumbTouched(float x, float y) {
        return Math.sqrt(Math.pow(x - capillaryRect.centerX(), 2) + Math.pow(y - thumbY, 2)) <= thumbRadius;
    }

    public void setIntervalColors(int color) {
        intervalColors = new int[]{color, color, color, color, color, color};
        createGradientShader();
        invalidate();
        /*if (colors.length == 6) {
            intervalColors = colors;
            createGradientShader();
            invalidate();
        } else {
            throw new IllegalArgumentException("Exactly 6 colors are required.");
        }*/
    }
    public void setstepInterval(int stepIntervalValue){
        stepInterval = stepIntervalValue;
    }
    public void setBackgroundColor(int color) {
        backgroundColor = color;
        backgroundPaint.setColor(backgroundColor);
        invalidate();
    }

    public void setFillPercentage(float percentage) {
        fillPercentage = Math.max(0f, Math.min(1f, percentage));
        int steps = (int) (minSteps + (maxSteps - minSteps) * fillPercentage);
        thumbY = calculateYFromSteps(steps);
        invalidate();
        //updateStepCount(steps);
    }

    public void setMinSteps(int minSteps) {
        this.minSteps = minSteps;
        thumbY = calculateYFromSteps(minSteps);
        invalidate();
        //updateStepCount(minSteps);
    }

    public void setMaxSteps(int maxSteps) {
        this.maxSteps = maxSteps;
        createGradientShader();
        invalidate();
    }

    public void setOnStepCountChangeListener(OnStepCountChangeListener listener) {
        this.listener = listener;
    }

    private void updateStepCount(int stepCount) {
        if (listener != null) {
            listener.onStepCountChanged(stepCount);
        }
    }

    public interface OnStepCountChangeListener {
        void onStepCountChanged(int stepCount);
    }
}
