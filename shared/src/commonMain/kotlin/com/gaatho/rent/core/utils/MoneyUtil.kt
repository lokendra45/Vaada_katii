package com.gaatho.rent.core.utils

/**
 * Production-grade Money Utility.
 *
 * Rules:
 * 1. Never use Double/Float for financial math.
 * 2. Smallest unit is Long (e.g., Paisa in NPR).
 */
object MoneyUtil {

    private const val MULTIPLIER = 100L

    /**
     * Converts major unit (Rupees) to smallest unit (Paisa).
     */
    fun toSmallestUnit(amount: Double): Long {
        return (amount * MULTIPLIER).toLong()
    }

    /**
     * Converts smallest unit (Paisa) to major unit (Rupees).
     */
    fun toMajorUnit(smallestUnit: Long): Double {
        return smallestUnit.toDouble() / MULTIPLIER
    }

    /**
     * Formats smallest unit for display using [CurrencyUtil].
     */
    fun format(smallestUnit: Long, includeSymbol: Boolean = true): String {
        return CurrencyUtil.formatNpr(toMajorUnit(smallestUnit), includeSymbol)
    }
}
