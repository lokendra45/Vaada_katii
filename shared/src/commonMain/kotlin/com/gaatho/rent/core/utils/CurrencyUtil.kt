package com.gaatho.rent.core.utils

import kotlin.math.abs
import kotlin.math.roundToInt

/**
 * Common currency formatting utility specifically tailored for Nepalese Rupees (NPR).
 *
 * Provides standardized financial formatting across Rent Manager Nepal (e.g., `Rs. 25,000` or `Rs. 1.5L`).
 */
object CurrencyUtil {

    private const val CURRENCY_SYMBOL = "Rs."

    /**
     * Formats a financial amount into standard Nepalese/South Asian currency string (`Rs. 25,000`).
     *
     * @param amount Amount in NPR (`Double`).
     * @param includeSymbol Whether to prefix with `Rs.` (defaults to `true`).
     */
    fun formatNpr(amount: Double, includeSymbol: Boolean = true): String {
        val sign = if (amount < 0) "-" else ""
        val absVal = abs(amount).roundToInt()
        val formattedNumber = formatSouthAsianNumber(absVal.toLong())
        return if (includeSymbol) "$sign$CURRENCY_SYMBOL $formattedNumber" else "$sign$formattedNumber"
    }

    /**
     * Formats large currency amounts into short, readable badges (`Rs. 25K`, `Rs. 1.5L`).
     * Ideal for dashboard summary cards and quick overview widgets.
     */
    fun formatNprShort(amount: Double): String {
        val sign = if (amount < 0) "-" else ""
        val absVal = abs(amount)

        return when {
            absVal >= 1_00_00_000 -> {
                val crores = absVal / 1_00_00_000.0
                "$sign$CURRENCY_SYMBOL ${roundToTwoDecimals(crores)}Cr"
            }
            absVal >= 1_00_000 -> {
                val lakhs = absVal / 1_00_000.0
                "$sign$CURRENCY_SYMBOL ${roundToTwoDecimals(lakhs)}L"
            }
            absVal >= 1_000 -> {
                val thousands = absVal / 1_000.0
                "$sign$CURRENCY_SYMBOL ${roundToTwoDecimals(thousands)}K"
            }
            else -> "$sign$CURRENCY_SYMBOL ${absVal.roundToInt()}"
        }
    }

    /**
     * Formats numbers using South Asian numbering system (Lakhs / Crores: `3,2,2` digit grouping).
     * For example: `150000` -> `1,50,000`.
     */
    private fun formatSouthAsianNumber(number: Long): String {
        val str = number.toString()
        if (str.length <= 3) return str

        val lastThree = str.takeLast(3)
        val remaining = str.dropLast(3)

        val grouped = StringBuilder()
        var count = 0
        for (i in remaining.length - 1 downTo 0) {
            grouped.append(remaining[i])
            count++
            if (count % 2 == 0 && i != 0) {
                grouped.append(',')
            }
        }
        return "${grouped.reverse()},$lastThree"
    }

    private fun roundToTwoDecimals(value: Double): String {
        val rounded = (value * 100.0).roundToInt() / 100.0
        val str = rounded.toString()
        return if (str.endsWith(".0")) str.dropLast(2) else str
    }
}
