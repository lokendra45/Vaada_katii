package com.gaatho.rent.core.utils

import kotlin.time.Instant
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
}
