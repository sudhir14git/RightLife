package com.example.rlapp.ui.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class DateConverter {

    public static String convertToDate(String inputDate) {
        // Define the input and output formats
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd MMM");

        // Set the time zone for input format to UTC
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            // Parse the input date string into a Date object
            Date date = inputFormat.parse(inputDate);

            // Format the Date object into the desired output format
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static String convertToTime(String inputDate) {
        // Define the input and output formats
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        SimpleDateFormat outputFormat = new SimpleDateFormat("hh:mm a"); // 12-hour format with AM/PM

        // Set the time zone for input format to UTC
        inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            // Parse the input date string into a Date object
            Date date = inputFormat.parse(inputDate);

            // Format the Date object into the desired output format
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return null;
        }
    }




}
