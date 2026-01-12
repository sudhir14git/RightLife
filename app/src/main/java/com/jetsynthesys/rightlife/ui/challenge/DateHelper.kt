package com.jetsynthesys.rightlife.ui.challenge

import java.text.SimpleDateFormat
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateHelper {

    fun getTodayDate(): String {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.ENGLISH)
        return sdf.format(Date())
    }

    fun getDayFromDate(date: String): String {
        return LocalDate.parse(date).dayOfMonth.toString().padStart(2, '0')
    }

    fun getDaySuffix(day: Int): String {
        return when {
            day in 11..13 -> "th"
            day % 10 == 1 -> "st"
            day % 10 == 2 -> "nd"
            day % 10 == 3 -> "rd"
            else -> "th"
        }
    }

    fun getChallengeDateRange(start: String, end: String): String {
        val inputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        val outputFormat = SimpleDateFormat("MMM d", Locale.ENGLISH)
        val yearFormat = SimpleDateFormat("yyyy", Locale.ENGLISH)

        val startDate = inputFormat.parse(start)
        val endDate = inputFormat.parse(end)

        return "${outputFormat.format(startDate)} - ${outputFormat.format(endDate)}, ${
            yearFormat.format(
                endDate
            )
        }"
    }

    fun getDaysFromToday(dateString: String): Int {
        val inputFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.ENGLISH)
        inputFormat.isLenient = false

        return try {
            val targetDate = inputFormat.parse(dateString) ?: return 0

            // Clear time part for accurate day calculation
            val todayCal = Calendar.getInstance().apply {
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val targetCal = Calendar.getInstance().apply {
                time = targetDate
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }

            val diffMillis = targetCal.timeInMillis - todayCal.timeInMillis
            (diffMillis / (1000 * 60 * 60 * 24)).toInt()

        } catch (e: Exception) {
            0
        }
    }

}