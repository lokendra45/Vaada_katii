package com.gaatho.rent.core.utils

/**
 * Production-grade Money Utility.
 *
 * Rules:
 * 1. Never use Double/Float for financial math.
 * 2. Uses Long to represent the major unit (e.g., Rupees in NPR) as fractional amounts are rarely used for rent.
 */
object MoneyUtil {

    /**
     * Formats the amount (Rupees) for display using [CurrencyUtil].
     */
    fun format(amount: Long, includeSymbol: Boolean = true): String {
        return CurrencyUtil.formatNpr(amount.toDouble(), includeSymbol)
    }
}
