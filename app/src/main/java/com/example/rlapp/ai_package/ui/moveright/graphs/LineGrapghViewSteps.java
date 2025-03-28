package com.example.rlapp.ai_package.ui.moveright.graphs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.DashPathEffect;
import android.util.AttributeSet;
import android.view.View;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class LineGrapghViewSteps extends View {

    private Paint linePaint; // Paint for the lines
    private Paint circlePaint; // Paint for the circles at the end of each line
    private Paint axisPaint; // Paint for the X-axis labels
    private List<float[]> dataSets; // List of datasets (each dataset represents a line)
    private List<Integer> lineColors; // Colors for each line
    private Path path;

    public LineGrapghViewSteps(Context context) {
        super(context);
        init();
    }

    public LineGrapghViewSteps(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        // Initialize line paint
        linePaint = new Paint();
        linePaint.setStrokeWidth(5f); // Thicker line to match the image
        linePaint.setStyle(Paint.Style.STROKE);

        // Initialize circle paint (for the dots at the end of each line)
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);

        // Initialize axis paint
        axisPaint = new Paint();
        axisPaint.setColor(0xFF000000); // Black color for the labels
        axisPaint.setTextSize(24f); // Reduced text size to fit all labels
        axisPaint.setTextAlign(Paint.Align.CENTER); // Default alignment (will adjust dynamically)

        // Initialize data sets and colors
        dataSets = new ArrayList<>();
        lineColors = new ArrayList<>();

        path = new Path();
    }

    // Method to add a dataset and its corresponding color
    public void addDataSet(float[] data, int color) {
        dataSets.add(data);
        lineColors.add(color);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Get the width and height of the view
        float width = getWidth();
        float height = getHeight();

        // Log the view dimensions to debug
        Log.d("LineGrapghViewSteps", "View Width: " + width + ", View Height: " + height);

        // Reserve space at the bottom for the X-axis labels
        float paddingBottom = 50f; // Space for X-axis labels
        float graphHeight = height - paddingBottom;

        // Add padding at the start and end of the graph
        float paddingHorizontal = 30f; // Padding on both sides (start and end)
        float effectiveWidth = width - (2 * paddingHorizontal); // Effective width after padding

        // Find the global minimum and maximum values across all datasets
        float minValue = Float.MAX_VALUE;
        float maxValue = Float.MIN_VALUE;
        for (float[] data : dataSets) {
            for (float value : data) {
                if (value < minValue) minValue = value;
                if (value > maxValue) maxValue = value;
            }
        }

        // Scale the data points to fit within the graph's height and effective width
        float scaleY = graphHeight / (maxValue - minValue);
        float scaleX = effectiveWidth / (dataSets.get(0).length - 1); // Scale based on effective width

        // Draw each dataset as a separate line
        for (int i = 0; i < dataSets.size(); i++) {
            float[] data = dataSets.get(i);
            int color = lineColors.get(i);

            // Set the line color
            linePaint.setColor(color);

            // Apply dotted effect if the line is green (0xFF03B27B)
            if (color == 0xFF03B27B) {
                linePaint.setPathEffect(new DashPathEffect(new float[]{10f, 10f}, 0f)); // Dotted pattern: 10px dash, 10px gap
            } else {
                linePaint.setPathEffect(null); // Solid line for other colors
            }

            // Draw the line
            path.reset();
            float lastX = 0f;
            float lastY = 0f;
            for (int j = 0; j < data.length; j++) {
                // Adjust the x position to account for the start padding
                float x = paddingHorizontal + (j * scaleX);
                float y = graphHeight - ((data[j] - minValue) * scaleY);

                if (j == 0) {
                    path.moveTo(x, y); // Move to the first point
                } else {
                    path.lineTo(x, y); // Draw line to the next point
                }

                // Store the last point for drawing the circle
                if (j == data.length - 1) {
                    lastX = x;
                    lastY = y;
                }
            }

            // Draw the line on the canvas
            canvas.drawPath(path, linePaint);

            // Draw a circle at the last point
            circlePaint.setColor(color);
            canvas.drawCircle(lastX, lastY, 6f, circlePaint); // 6f radius for the circle
        }

        // Draw X-axis labels (time in hours)
        String[] timeLabels = {"12 am", "6 am", "12 pm", "8 pm", "12 am"}; // Labels to match the image
        float labelSpacing = effectiveWidth / (timeLabels.length - 1); // Space between labels based on effective width

        // Log the label spacing to debug
        Log.d("LineGrapghViewSteps", "Label Spacing: " + labelSpacing);

        for (int i = 0; i < timeLabels.length; i++) {
            float x;
            // Adjust the x position to account for the start padding
            if (i == 0) {
                // First label ("12 am"): Align left
                axisPaint.setTextAlign(Paint.Align.LEFT);
                x = paddingHorizontal; // Start at the padding
            } else if (i == timeLabels.length - 1) {
                // Last label ("12 am"): Align right
                axisPaint.setTextAlign(Paint.Align.RIGHT);
                x = width - paddingHorizontal; // End at the padding
            } else {
                // Middle labels ("6 am", "12 pm", "8 pm"): Align center
                axisPaint.setTextAlign(Paint.Align.CENTER);
                x = paddingHorizontal + (i * labelSpacing); // Center position for middle labels
            }

            float y = graphHeight + 40f; // Position labels below the X-axis
            // Log the label position to debug
            Log.d("LineGrapghViewSteps", "Drawing label: " + timeLabels[i] + " at x: " + x + ", y: " + y);
            canvas.drawText(timeLabels[i], x, y, axisPaint);
        }
    }
}