package com.example.rlapp.ai_package.ui.moveright.graphs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class LineGraphView extends View {

    private Paint linePaint; // Paint for the line
    private Paint circlePaint; // Paint for the circles
    private Path path;
    private float[] dataPoints = {30, 60, 4, 80, 40, 90}; // Example data points
    private float padding = 20f; // Padding to avoid cutting off the graph

    public LineGraphView(Context context) {
        super(context);
        init();
    }

    public LineGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize line paint
        linePaint = new Paint();
        linePaint.setColor(0xFFB11414); // Line color
        linePaint.setStrokeWidth(3f); // Thinner line for small view
        linePaint.setStyle(Paint.Style.STROKE);

        // Initialize circle paint
        circlePaint = new Paint();
        circlePaint.setColor(0xFFB11414); // Circle color (same as line)
        circlePaint.setStrokeWidth(3f); // Thinner stroke for small view
        circlePaint.setStyle(Paint.Style.STROKE); // Hollow circle
        circlePaint.setAntiAlias(true); // Smooth edges

        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Get the width and height of the view
        float width = getWidth() - 2 * padding; // Account for padding
        float height = getHeight() - 2 * padding; // Account for padding

        // Find the minimum and maximum values in the data points
        float minValue = Float.MAX_VALUE;
        float maxValue = Float.MIN_VALUE;
        for (float value : dataPoints) {
            if (value < minValue) minValue = value;
            if (value > maxValue) maxValue = value;
        }

        // Scale the data points to fit within the view's height
        float scaleY = height / (maxValue - minValue);
        float scaleX = width / (dataPoints.length - 1);

        // Draw the line and circles
        path.reset();
        for (int i = 0; i < dataPoints.length; i++) {
            float x = padding + i * scaleX; // Add padding to X
            float y = padding + height - ((dataPoints[i] - minValue) * scaleY); // Add padding to Y

            if (i == 0) {
                path.moveTo(x, y); // Move to the first point
            } else {
                path.lineTo(x, y); // Draw line to the next point
            }

            // Draw a hollow circle at each data point
            canvas.drawCircle(x, y, 5, circlePaint); // 5 is the radius of the circle
        }

        // Draw the line
        canvas.drawPath(path, linePaint);
    }
}