package com.gaatho.rent.core.utils

import kotlinx.datetime.LocalDate

/**
 * Standardized ISO-8601 Date Utility (YYYY-MM-DD).
 *
 * Ensures all dates in the database are deterministic and sortable.
 */
object IsoDateUtil {

    /**
     * Normalizes a date string to YYYY-MM-DD.
     * Returns null if invalid instead of throwing.
     */
    fun normalize(date: String?): String? {
        if (date == null) return null
        return try {
            // Attempt to parse to verify format
            val parsed = LocalDate.parse(date)
            parsed.toString() // Returns YYYY-MM-DD
        } catch (e: Exception) {
            null
        }
    }

    /**
     * Formats a [LocalDate] to the database standard string.
     */
    fun format(date: LocalDate): String = date.toString()

    /**
     * Checks if a string is a valid ISO date.
     */
    fun isValid(date: String?): Boolean = normalize(date) != null
}
