package com.jetsynthesys.rightlife.ai_package.ui.moveright.graphs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.DashPathEffect;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import androidx.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class LineGrapghViewSteps extends View {
    private Paint linePaint; // Paint for the graph lines
    private Paint circlePaint; // Paint for the circles at the end of each line and intersections
    private Paint axisPaint; // Paint for the X-axis and Y-axis labels
    private Paint verticalLinePaint; // Paint for the vertical grey lines at labels
    private Paint maxValueLinePaint; // Paint for the vertical light grey line at max value
    private Paint gridLinePaint; // Paint for the horizontal grey grid lines
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

        // Initialize circle paint (for the dots at the end of each line and intersections)
        circlePaint = new Paint();
        circlePaint.setStyle(Paint.Style.FILL);

        // Initialize axis paint for X and Y labels
        axisPaint = new Paint();
        axisPaint.setColor(0xFF000000); // Black color for the labels
        axisPaint.setTextSize(18f); // Reduced text size to prevent overlap
        axisPaint.setTextAlign(Paint.Align.CENTER); // Default alignment (will adjust dynamically)

        // Initialize vertical line paint for labels
        verticalLinePaint = new Paint();
        verticalLinePaint.setColor(0xFFA7A7A7); // Grey color for vertical lines
        verticalLinePaint.setStrokeWidth(4f); // Thicker line
        verticalLinePaint.setStyle(Paint.Style.STROKE);

        // Initialize paint for max value vertical line
        maxValueLinePaint = new Paint();
        maxValueLinePaint.setColor(0xFFD9D9D9); // Light grey color
        maxValueLinePaint.setStrokeWidth(3f); // Slightly thinner than graph lines
        maxValueLinePaint.setStyle(Paint.Style.STROKE);

        // Initialize paint for horizontal grid lines
        gridLinePaint = new Paint();
        gridLinePaint.setColor(0xFFD9D9D9); // Light grey color for grid lines
        gridLinePaint.setStrokeWidth(2f); // Thin line for grid
        gridLinePaint.setStyle(Paint.Style.STROKE);
        gridLinePaint.setPathEffect(new DashPathEffect(new float[]{5f, 5f}, 0f)); // Dotted pattern for grid lines

        // Initialize data sets and colors
        dataSets = new ArrayList<>();
        lineColors = new ArrayList<>();
        path = new Path();
    }

    // Method to add a dataset and its corresponding color
    public void addDataSet(float[] data, int color) {
        dataSets.add(data);
        lineColors.add(color);
        invalidate(); // Trigger a redraw after adding data
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        // Get the width and height of the view
        float width = getWidth();
        float height = getHeight();

        // Log the view dimensions to debug
        Log.d("LineGrapghViewSteps", "View Width: " + width + ", View Height: " + height);

        if (dataSets.isEmpty()) {
            Log.w("LineGrapghViewSteps", "No datasets available, skipping draw.");
            return; // Exit if no data to draw
        }

        // Define padding
        float paddingBottom = 60f; // Increased space for X-axis labels to prevent overlap
        float paddingTop = 40f; // Increased padding at the top to prevent clipping
        float paddingLeft = 40f; // Increased left padding for graph
        float paddingRight = 120f; // Increased right padding for Y-axis labels
        float graphHeight = height - paddingBottom - paddingTop; // Adjusted graph height
        float effectiveWidth = width - paddingLeft - paddingRight; // Effective width after padding

        // Find the global maximum value for dynamic Y-axis scaling
        float minValue = 0f; // Fixed min value
        float globalMaxValue = Float.MIN_VALUE;
        int maxValueIndex = 0;
        for (int i = 0; i < dataSets.size(); i++) {
            float[] data = dataSets.get(i);
            if (data != null && data.length > 0) {
                for (int j = 0; j < data.length; j++) {
                    if (data[j] > globalMaxValue) {
                        globalMaxValue = data[j];
                        maxValueIndex = j;
                    }
                }
            }
        }
        // Set a default max value if no valid data
        if (globalMaxValue == Float.MIN_VALUE) {
            globalMaxValue = 2500f; // Default max to 2500
        }

        // Calculate dynamic maxValue (ceiling to nearest multiple of interval)
        float interval;
        if (globalMaxValue <= 2500f) {
            interval = 500f; // Interval of 500 for max value <= 2500
        } else if (globalMaxValue <= 5000f) {
            interval = 1000f; // Interval of 1000 for max value > 2500 and <= 5000
        } else {
            interval = 2500f; // Interval of 2500 for max value > 5000
        }
        float maxValue = (float) (Math.ceil(globalMaxValue / interval) * interval);
        if (maxValue < interval) maxValue = interval; // Ensure at least one increment
        float scaleY = graphHeight / (maxValue - minValue); // Scale for Y-axis

        // Generate dynamic Y-axis values based on interval
        List<Float> yAxisValues = new ArrayList<>();
        for (float value = 0f; value <= maxValue; value += interval) {
            yAxisValues.add(value);
        }

        // Scale X-axis
        float scaleX = effectiveWidth / (dataSets.get(0).length - 1); // Safe to access now

        // Draw the vertical light grey line at the max value's X position
        float maxLineX = paddingLeft + (maxValueIndex * scaleX);
        canvas.drawLine(maxLineX, paddingTop, maxLineX, height - paddingBottom, maxValueLinePaint);
        Log.d("LineGrapghViewSteps", "Max value line at x: " + maxLineX + ", max value: " + globalMaxValue + ", index: " + maxValueIndex);

        // Draw Y-axis labels and horizontal grey grid lines
        for (float yValue : yAxisValues) {
            // Calculate Y-coordinate for the label and grid line
            float y = (height - paddingBottom) - ((yValue - minValue) * scaleY);

            // Draw Y-axis label on the right side with better positioning
            axisPaint.setTextAlign(Paint.Align.LEFT);
            canvas.drawText(String.valueOf((int) yValue), width - paddingRight + 15f, y + 6f, axisPaint); // Better offset from right edge

            // Draw horizontal grid line
            canvas.drawLine(paddingLeft, y, width - paddingRight, y, gridLinePaint);
        }

        // Draw each dataset as a separate line
        for (int i = 0; i < dataSets.size(); i++) {
            float[] data = dataSets.get(i);
            int color = lineColors.get(i);

            // Set the line color
            linePaint.setColor(color);

            // Apply dotted effect only for green color (0xFF03B27B)
            if (color == 0xFF03B27B) {
                linePaint.setPathEffect(new DashPathEffect(new float[]{10f, 10f}, 0f)); // Dotted pattern for green
                Log.d("LineGrapghViewSteps", "Dotted line for green color: " + Integer.toHexString(color));
            } else {
                linePaint.setPathEffect(null); // Solid line for all other colors
                Log.d("LineGrapghViewSteps", "Solid line for color: " + Integer.toHexString(color));
            }

            // Draw the line
            path.reset();
            float lastX = 0f;
            float lastY = 0f;

            if (data != null && data.length > 0) {
                // For the red line, draw only up to maxValueIndex
                int drawLimit = (color == 0xFFFD6967) ? Math.min(maxValueIndex + 1, data.length) : data.length;
                for (int j = 0; j < drawLimit; j++) {
                    // Adjust the x position to account for the left padding
                    float x = paddingLeft + (j * scaleX);
                    float y = (height - paddingBottom) - ((data[j] - minValue) * scaleY); // Adjusted for paddingTop
                    if (j == 0) {
                        path.moveTo(x, y); // Move to the first point
                    } else {
                        path.lineTo(x, y); // Draw line to the next point
                    }
                    // Store the last point for drawing the circle
                    if (j == drawLimit - 1) {
                        lastX = x;
                        lastY = y;
                    }
                }
                // Draw the line on the canvas
                canvas.drawPath(path, linePaint);

                // Draw a circle at the last point (only if not already at maxValueIndex for red line)
                if (color != 0xFFFD6967 || maxValueIndex != data.length - 1) {
                    circlePaint.setColor(color);
                    canvas.drawCircle(lastX, lastY, 6f, circlePaint); // 6f radius for the circle
                }

                // Draw a circle at the intersection with the max value line for red, grey, and green lines
                if (color == 0xFFFD6967 || color == 0xFF707070 || color == 0xFF03B27B) {
                    float y = (height - paddingBottom) - ((data[maxValueIndex] - minValue) * scaleY);
                    circlePaint.setColor(color);
                    canvas.drawCircle(maxLineX, y, 6f, circlePaint);
                    Log.d("LineGrapghViewSteps", "Intersection circle at x: " + maxLineX + ", y: " + y + ", color: " + Integer.toHexString(color));
                }
            }
        }

        // Draw X-axis labels (time in hours) and vertical solid grey lines with better spacing
        String[] timeLabels = {"12 am", "6 am", "12 pm", "8 pm", "12 am"};
        float labelSpacing = effectiveWidth / (timeLabels.length - 1); // Space between labels based on effective width

        // Log the label spacing to debug
        Log.d("LineGrapghViewSteps", "Label Spacing: " + labelSpacing);

        for (int i = 0; i < timeLabels.length; i++) {
            float x;
            // Adjust the x position to account for the left padding
            if (i == 0) {
                // First label ("12 am"): Align left
                axisPaint.setTextAlign(Paint.Align.LEFT);
                x = paddingLeft; // Start at the left padding
            } else if (i == timeLabels.length - 1) {
                // Last label ("12 am"): Align right
                axisPaint.setTextAlign(Paint.Align.RIGHT);
                x = width - paddingRight; // End at the right padding
            } else {
                // Middle labels ("6 am", "12 pm", "8 pm"): Align center
                axisPaint.setTextAlign(Paint.Align.CENTER);
                x = paddingLeft + (i * labelSpacing); // Center position for middle labels
            }

            // Draw the label with better positioning to prevent overlap
            float labelY = height - 20f; // More space from bottom to prevent overlap
            canvas.drawText(timeLabels[i], x, labelY, axisPaint);

            // Draw a taller and thicker vertical solid grey line at the label's X position
            float lineX = x; // X-coordinate aligned with the label
            float lineStartY = height - paddingBottom; // Start at the bottom of the graph
            float lineEndY = (height - paddingBottom) - 35f; // Extended upward for better visibility
            canvas.drawLine(lineX, lineStartY, lineX, lineEndY, verticalLinePaint);
        }
    }

    // Optional: Clear all datasets
    public void clear() {
        dataSets.clear();
        lineColors.clear();
        invalidate(); // Trigger a redraw
    }
}