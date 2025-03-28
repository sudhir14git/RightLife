package com.example.rlapp.ai_package.ui.moveright.graphs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

public class LineGraphViewWorkout extends View {

    private Paint linePaint; // Paint for the line
    private Path path;
    private float[] dataPoints = {30, 60, 4, 80, 40, 90, 50, 70, 20, 100, 60, 30};

    public LineGraphViewWorkout(Context context) {
        super(context);
        init();
    }

    public LineGraphViewWorkout(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize line paint
        linePaint = new Paint();
        linePaint.setColor(0xFFB11414); // Line color
        linePaint.setStrokeWidth(3f); // Thinner line for small view
        linePaint.setStyle(Paint.Style.STROKE);

        path = new Path();
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Get the width and height of the view
        float width = getWidth();
        float height = getHeight();

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

        // Draw the line
        path.reset();
        for (int i = 0; i < dataPoints.length; i++) {
            float x = i * scaleX;
            float y = height - ((dataPoints[i] - minValue) * scaleY);

            if (i == 0) {
                path.moveTo(x, y); // Move to the first point
            } else {
                path.lineTo(x, y); // Draw line to the next point
            }
        }

        // Draw the line
        canvas.drawPath(path, linePaint);
    }
}