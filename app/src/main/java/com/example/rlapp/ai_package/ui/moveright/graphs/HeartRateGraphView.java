package com.example.rlapp.ai_package.ui.moveright.graphs;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import androidx.annotation.Nullable;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class HeartRateGraphView extends View {
    private Paint linePaint, gridPaint, textPaint, boxPaint;
    private List<Float> bpmValues;
    private List<String> timeLabels;
    private float minBPM = 40, maxBPM = 200;
    private int hours = 1; // 1 hour (6:00 AM to 7:00 AM)
    private int pointsPerHour = 60; // 1 point per minute (60 points for 1 hour)
    private static final int COLOR_LOW = Color.GREEN; // Low BPM range
    private static final int COLOR_MID = Color.YELLOW; // Mid BPM range
    private static final int COLOR_HIGH = Color.RED; // High BPM range
    private static final float LOW_THRESHOLD = 60f; // BPM threshold for low range
    private static final float MID_THRESHOLD = 100f; // BPM threshold for mid range

    public HeartRateGraphView(Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        linePaint = new Paint();
        linePaint.setStrokeWidth(5f);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setAntiAlias(true);

        gridPaint = new Paint();
        gridPaint.setColor(Color.GRAY);
        gridPaint.setStrokeWidth(2f);
        gridPaint.setStyle(Paint.Style.STROKE);

        boxPaint = new Paint();
        boxPaint.setColor(Color.LTGRAY);
        boxPaint.setStyle(Paint.Style.FILL);
        boxPaint.setAlpha(50);

        textPaint = new Paint();
        textPaint.setColor(Color.BLACK);
        textPaint.setTextSize(30f);
        textPaint.setAntiAlias(true);

        generateData();
    }

    private void generateData() {
        bpmValues = new ArrayList<>();
        timeLabels = new ArrayList<>();

        int totalPoints = hours * pointsPerHour; // 60 points (1 per minute)
        int peakMinute = 30; // Peak at 6:30 AM (30 minutes into the hour)

        // Set the start time to 6:00 AM
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 6);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        // Start BPM at 100
        int currentBPM = 100;

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());

        for (int minute = 0; minute < totalPoints; minute++) {
            // Add the current BPM value
            bpmValues.add((float) currentBPM);

            // Add the time label (e.g., "06:00", "06:15", "06:30", "06:45", "07:00")
            if (minute % 15 == 0) { // Show label every 15 minutes
                timeLabels.add(timeFormat.format(calendar.getTime()));
            } else {
                timeLabels.add(""); // Empty label for intermediate points
            }

            // Increment the time by 1 minute
            calendar.add(Calendar.MINUTE, 1);

            // Adjust BPM based on the minute
            if (minute < peakMinute) {
                // Increase BPM until 6:30 AM
                if (minute % 2 == 0) {
                    currentBPM -= (int) (Math.random() * 10) + 1; // Random decrement between 1 and 10
                } else {
                    currentBPM += (int) (Math.random() * 11) + 10; // Random increment between 10 and 20
                }
                // Clamp to max BPM of 190
                if (currentBPM > 190) {
                    currentBPM = 190;
                }
            } else {
                // Decrease BPM after 6:30 AM
                if (minute % 2 == 0) {
                    currentBPM += (int) (Math.random() * 10) + 1; // Random increment between 1 and 10
                } else {
                    currentBPM -= (int) (Math.random() * 11) + 10; // Random decrement between 10 and 20
                }
                // Clamp to min BPM of 100
                if (currentBPM < 100) {
                    currentBPM = 100;
                }
            }
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        float width = getWidth();
        float height = getHeight();
        float padding = 50f;
        float graphWidth = width - padding * 2;
        float graphHeight = height - padding * 2;
        float scaleX = graphWidth / (bpmValues.size() - 1); // X-axis scaling
        float scaleY = graphHeight / (maxBPM - minBPM); // Y-axis scaling

        // Draw background boxes (for each 15-minute segment)
        for (int i = 0; i < bpmValues.size() - 1; i++) {
            float left = padding + (i * scaleX);
            float right = padding + ((i + 1) * scaleX);
            for (int j = 0; j < 4; j++) {
                float top = padding + (j * (graphHeight / 4));
                float bottom = padding + ((j + 1) * (graphHeight / 4));
                canvas.drawRect(left, top, right, bottom, boxPaint);
            }
        }

        // Draw vertical grid lines (every 15 minutes)
        for (int i = 0; i < bpmValues.size(); i += 15) { // Every 15 minutes
            float x = padding + (i * scaleX);
            canvas.drawLine(x, padding, x, height - padding, gridPaint);
        }

        // Draw horizontal grid lines (every 40 BPM)
        for (int i = 0; i <= 4; i++) {
            float y = padding + (i * (graphHeight / 4));
            canvas.drawLine(padding, y, width - padding, y, gridPaint);
        }

        // Draw time labels (every 15 minutes)
        for (int i = 0; i < bpmValues.size(); i += 15) {
            float x = padding + (i * scaleX);
            if (!timeLabels.get(i).isEmpty()) {
                canvas.drawText(timeLabels.get(i), x - 30, height - 10, textPaint);
            }
        }

        // Draw BPM labels (every 40 BPM)
        for (int i = 0; i <= 4; i++) {
            float bpmLabel = maxBPM - (i * (maxBPM - minBPM) / 4);
            float y = padding + (i * (graphHeight / 4));
            canvas.drawText(String.valueOf((int) bpmLabel), 10, y + 10, textPaint);
        }

        // Draw the heart rate line
        for (int i = 0; i < bpmValues.size() - 1; i++) {
            float x1 = padding + (i * scaleX);
            float y1 = height - padding - ((bpmValues.get(i) - minBPM) * scaleY);

            float x2 = padding + ((i + 1) * scaleX);
            float y2 = height - padding - ((bpmValues.get(i + 1) - minBPM) * scaleY);

            float currentBPM = bpmValues.get(i);
            if (currentBPM < LOW_THRESHOLD) {
                linePaint.setColor(COLOR_LOW);
            } else if (currentBPM < MID_THRESHOLD) {
                linePaint.setColor(COLOR_MID);
            } else {
                linePaint.setColor(COLOR_HIGH);
            }

            canvas.drawLine(x1, y1, x2, y2, linePaint);
        }
    }

    public void updateData(List<Float> newBpmValues) {
        bpmValues = newBpmValues;
        invalidate();
    }
}