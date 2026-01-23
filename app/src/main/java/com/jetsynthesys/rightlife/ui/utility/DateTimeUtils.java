package com.jetsynthesys.rightlife.ui.utility;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public class DateTimeUtils {
    public static String getDateTime() {
        Date c = Calendar.getInstance().getTime();

        SimpleDateFormat df = new SimpleDateFormat("EEE dd MMMM. hh:mm a", Locale.getDefault());

        return df.format(c);
    }

    public static String convertAPIDate(String date) {
        if (date == null)
            return date;
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            Date newDate = spf.parse(date);
            spf = new SimpleDateFormat("dd/MM/yyyy");
            date = spf.format(newDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String convert_ddMMyyyy_toAPIDate(String date) {
        if (date == null)
            return date;
        SimpleDateFormat spf = new SimpleDateFormat("dd/MM/yyyy");
        try {
            Date newDate = spf.parse(date);
            spf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
            date = spf.format(newDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String getWishingMessage() {
        Calendar c = Calendar.getInstance();
        int timeOfDay = c.get(Calendar.HOUR_OF_DAY);
        String message = "";

        if (timeOfDay >= 0 && timeOfDay < 12) {
            message = "Morning";
        } else if (timeOfDay >= 12 && timeOfDay < 16) {
            message = "Afternoon";
        } else if (timeOfDay >= 16 && timeOfDay < 21) {
            message = "Evening";
        } else if (timeOfDay >= 21 && timeOfDay < 24) {
            message = "Night";
        }
        return message;
    }


    public static String convertAPIDateMonthFormat(String date) {
        if (date == null)
            return date;
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        try {
            Date newDate = spf.parse(date);
            spf = new SimpleDateFormat("dd MMM yyyy");
            date = spf.format(newDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }
    public static String convertAPIDateMonthFormatWithTime(String date) {
        if (date == null)
            return date;
        SimpleDateFormat spf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'");
        spf.setTimeZone(TimeZone.getTimeZone("UTC"));
        try {
            Date newDate = spf.parse(date);
            // Define the desired output format
            spf = new SimpleDateFormat("dd MMMM yyyy | h.mm a", Locale.getDefault());
            spf.setTimeZone(TimeZone.getDefault());
            date = spf.format(newDate);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return date;
    }

    public static String convertAPIDateMonthFormatWithTimeFS(String date) {
        if (date == null) return date;

        try {
            // Input format (from API, in UTC)
            SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault());
            inputFormat.setTimeZone(TimeZone.getTimeZone("UTC"));

            Date parsedDate = inputFormat.parse(date);

            // Desired output format: 7 Jan, 2024 | 10:00 PM
            SimpleDateFormat outputFormat = new SimpleDateFormat("d MMM, yyyy | h:mm a", Locale.getDefault());
            outputFormat.setTimeZone(TimeZone.getDefault());

            return outputFormat.format(parsedDate).toUpperCase(Locale.getDefault());

        } catch (ParseException e) {
            e.printStackTrace();
            return date;
        }
    }



    public static String formatCurrentDate() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("EEE MMM dd | hh:mm a", Locale.ENGLISH);
        return dateFormat.format(new Date());
    }

    public static String getLocalTime12HourFormat(String utcTimestamp) {
        Instant instant = Instant.parse(utcTimestamp);
        ZonedDateTime zonedDateTime = instant.atZone(ZoneId.systemDefault());

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
        return zonedDateTime.format(formatter);
    }

    public static String formatSleepDuration(double hours) {
        int hr = (int) hours;
        int mins = (int) Math.round((hours - hr) * 60);
        return hr + " hr " + mins + " mins";
    }
    public static String formatSleepDurationforidealSleep(double totalMinutes) {
        int hours = (int) (totalMinutes / 60);
        int minutes = (int) Math.round(totalMinutes % 60);
        return hours + " hr " + minutes + " mins";
    }
    public static String formatSleepDurationint(int totalMinutes) {
        int hours = (int) (totalMinutes / 60);
        int minutes = (int) Math.round(totalMinutes % 60);
        return hours + " hr " + minutes + " mins";
    }

    public static String convertDecimalHoursToHrMinFormat(double hoursDecimal) {
        int totalMinutes = (int) (hoursDecimal * 60);
        int hours = totalMinutes / 60;
        int minutes = totalMinutes % 60;
        return String.format("%02dhr %02dmins", hours, minutes);
    }

    public static String formatDateForOneApi() {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH);
        return dateFormat.format(new Date());
    }

    public static String getSleepTime12HourFormat(String dateTimeStr) {
        try {
            // Parse as LocalDateTime since it has no timezone info
            DateTimeFormatter inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");
            LocalDateTime localDateTime = LocalDateTime.parse(dateTimeStr, inputFormatter);

            // Convert to desired format
            DateTimeFormatter outputFormatter = DateTimeFormatter.ofPattern("hh:mm a", Locale.ENGLISH);
            return outputFormatter.format(localDateTime);
        } catch (Exception e) {
            e.printStackTrace();
            return "";
        }
    }

    public static String formatDuration(Integer seconds) {
        if (seconds == null || seconds <= 0) {
            return "0 sec";
        }

        if (seconds < 60) {
            return seconds + " sec";
        } else {
            int minutes = seconds / 60;
            int remainingSec = seconds % 60;
            if (remainingSec == 0) {
                return minutes + " min";
            } else {
                double decimalMinutes = seconds / 60.0;
                return String.format("%.1f min", decimalMinutes);
            }
        }
    }


}
