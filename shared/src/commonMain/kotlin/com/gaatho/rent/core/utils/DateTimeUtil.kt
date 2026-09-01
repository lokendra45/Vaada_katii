package com.gaatho.rent.core.utils

import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.daysUntil
import kotlinx.datetime.toLocalDateTime

/**
 * Common date and time utility backed by [kotlinx.datetime].
 *
 * Provides standardized ISO-8601 timestamps and user-friendly formatting across all KMP targets
 * without relying on platform-specific `java.time` or `NSDate`.
 */
object DateTimeUtil {

    /**
     * Returns the current UTC timestamp as an ISO-8601 string (e.g., `2026-07-17T12:30:00.123Z`).
     * Use this for database `created_at` and `updated_at` columns.
     */
    fun nowIsoString(): String = kotlin.time.Clock.System.now().toString()

    /**
     * Returns the current system time as an [Instant].
     */
    fun nowInstant(): kotlin.time.Instant = kotlin.time.Clock.System.now()

    /**
     * Returns the current system time in milliseconds since the UNIX epoch.
     */
    fun nowEpochMillis(): Long = kotlin.time.Clock.System.now().toEpochMilliseconds()

    /**
     * Formats an ISO-8601 timestamp (`2026-07-17T12:30:00Z` or `2026-07-17`) into a readable UI date (`17 Jul 2026`).
     *
     * @param isoString The date string from the database or API.
     * @return Formatted human-readable date, or `"N/A"` if invalid or null.
     */
    fun formatReadableDate(isoString: String?): String {
        if (isoString.isNullOrBlank()) return "N/A"
        return try {
            val instant = try {
                Instant.parse(isoString)
            } catch (e: Exception) {
                // Try parsing as simple LocalDate (YYYY-MM-DD)
                val date = LocalDate.parse(isoString)
                return formatLocalDate(date)
            }
            val localDateTime = instant.toLocalDateTime(TimeZone.currentSystemDefault())
            formatLocalDate(localDateTime.date)
        } catch (e: Exception) {
            isoString // Fallback to raw string if parsing fails completely
        }
    }

    private fun formatLocalDate(date: LocalDate): String {
        val monthNames = arrayOf(
            "Jan", "Feb", "Mar", "Apr", "May", "Jun",
            "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
        )
        val monthIdx = date.month.ordinal + 1
        val month = monthNames.getOrNull(monthIdx - 1) ?: monthIdx.toString()
        val day = date.day.toString().padStart(2, '0')
        return "$day $month ${date.year}"
    }

    /**
     * Calculates the number of days between two date strings (`YYYY-MM-DD` or ISO timestamps).
     * Useful for lease duration and overdue rent calculations.
     */
    fun daysBetween(fromIso: String, toIso: String): Int {
        return try {
            val fromDate = parseToLocalDate(fromIso)
            val toDate = parseToLocalDate(toIso)
            fromDate.daysUntil(toDate)
        } catch (e: Exception) {
            0
        }
    }

    private fun parseToLocalDate(isoString: String): LocalDate {
        return try {
            Instant.parse(isoString).toLocalDateTime(TimeZone.currentSystemDefault()).date
        } catch (e: Exception) {
            LocalDate.parse(isoString.take(10))
        }
    }

    /**
     * Formats current date for the dashboard header: e.g. "TUESDAY, 24 JUNE 2025"
     */
    fun formatDashboardHeaderDate(): String {
        val date = nowInstant().toLocalDateTime(TimeZone.currentSystemDefault()).date
        val dayOfWeek = date.dayOfWeek.name
        val month = date.month.name
        return "$dayOfWeek, ${date.dayOfMonth} $month ${date.year}".uppercase()
    }

    /**
     * Formats an ISO date (`2023-10-18`) in the Figma style: `Oct 18, 2023`.
     * Returns the input unchanged when it cannot be parsed.
     */
    fun formatDisplayDate(iso: String): String {
        if (iso.isBlank()) return ""
        return try {
            val date = LocalDate.parse(iso.take(10))
            val months = arrayOf(
                "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"
            )
            val month = months.getOrNull(date.month.ordinal) ?: "?"
            "$month ${date.day}, ${date.year}"
        } catch (e: Exception) {
            iso
        }
    }

    /**
     * Returns start and end dates (YYYY-MM-DD) for a given number of months ago.
     * 0 = this month, 1 = last month, 2 = two months ago, etc.
     */
    fun getMonthsAgoDates(monthsAgo: Int): Pair<String, String> {
        val today = nowInstant().toLocalDateTime(TimeZone.currentSystemDefault()).date

        var targetYear = today.year
        var targetMonthOrdinal = today.month.ordinal - monthsAgo

        while (targetMonthOrdinal < 0) {
            targetYear -= 1
            targetMonthOrdinal += 12
        }

        val start = LocalDate(targetYear, targetMonthOrdinal + 1, 1)

        val nextMonthOrdinal = targetMonthOrdinal + 1
        val nextMonthYear = if (nextMonthOrdinal > 11) targetYear + 1 else targetYear
        val nextMonth = LocalDate(nextMonthYear, (nextMonthOrdinal % 12) + 1, 1)

        val end = LocalDate.fromEpochDays(nextMonth.toEpochDays() - 1)

        return start.toString() to end.toString()
    }

    /**
     * Returns a greeting based on the current system time.
     */
    fun getGreeting(): String {
        val hour = nowInstant().toLocalDateTime(TimeZone.currentSystemDefault()).hour
        return when (hour) {
            in 5..11 -> "Good morning"
            in 12..16 -> "Good afternoon"
            else -> "Good evening"
        }
    }
}